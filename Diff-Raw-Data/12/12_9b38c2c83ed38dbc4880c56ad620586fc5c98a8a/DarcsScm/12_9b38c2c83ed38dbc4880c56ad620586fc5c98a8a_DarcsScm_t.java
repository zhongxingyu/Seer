 
 package org.jvnet.hudson.plugins.darcs;
 
 import hudson.EnvVars;
 import hudson.Extension;
 import hudson.FilePath;
 import hudson.FilePath.FileCallable;
 import hudson.Launcher;
 import hudson.Launcher.ProcStarter;
 import hudson.model.Hudson;
 import hudson.model.AbstractBuild;
 import hudson.model.AbstractProject;
 import hudson.model.BuildListener;
 import hudson.model.TaskListener;
 import hudson.remoting.VirtualChannel;
 import hudson.scm.ChangeLogParser;
 import hudson.scm.PollingResult;
 import hudson.scm.PollingResult.Change;
 import hudson.scm.SCM;
 import hudson.scm.SCMRevisionState;
 import hudson.scm.SCMDescriptor;
 import hudson.util.ArgumentListBuilder;
 import hudson.util.FormValidation;
 
 import java.io.ByteArrayOutputStream;
 import java.io.PrintStream;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.io.Serializable;
 import java.io.StringWriter;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.servlet.ServletException;
 
 import net.sf.json.JSONObject;
 
 import org.kohsuke.stapler.DataBoundConstructor;
 import org.kohsuke.stapler.QueryParameter;
 import org.kohsuke.stapler.StaplerRequest;
 import org.kohsuke.stapler.framework.io.ByteBuffer;
 
 /**
  * Darcs is a patch based distributed version controll system.
  *
  * Contains the job configuration options as fields.
  * 
  * @see http://darcs.net/
  * 
  * @author Sven Strittmatter <ich@weltraumschaf.de>
  */
 public class DarcsScm extends SCM implements Serializable {
     private static final long serialVersionUID = 1L;
     private static final Logger logger = Logger.getLogger(DarcsScm.class.getName());
     
     /**
      * Source repository URL from which we pull.
      */
     private final String source;
     /**
      * Whether to wipe the checked out repo.
      */
     private final boolean clean;
 
     @DataBoundConstructor
     public DarcsScm(String source, boolean clean) {
         this.source = source;
         this.clean  = clean;
     }
 
     public String getSource() {
         return source;
     }
 
     public boolean isClean() {
         return clean;
     }
 
     /**
      * Writes the cahngelog of the last numPatches to the changeLog file.
      * 
      * @param launcher
      * @param numPatches
      * @param workspace
      * @param changeLog
      * @throws InterruptedException
      */
     private void getLog(Launcher launcher, int numPatches, FilePath workspace, File changeLog) throws InterruptedException {
         try {
             int ret;
             ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ProcStarter proc = launcher.launch()
                                        .cmds(getDescriptor().getDarcsExe(), "changes", "--last=" + numPatches)
                                        .envs(EnvVars.masterEnvVars)
                                        .stdout(baos)
                                        .pwd(workspace);
             ret = proc.join();
 
             if (ret != 0) {
                 logger.log(Level.WARNING, "darcs changes  --last=" + numPatches + " returned {0}", ret);
             } else {
                 FileOutputStream fos = new FileOutputStream(changeLog);
                 fos.write(baos.toByteArray());
                 fos.close();
             }
         } catch (IOException e) {
             StringWriter w = new StringWriter();
             e.printStackTrace(new PrintWriter(w));
             logger.log(Level.WARNING, "Failed to poll repository: ", e);
         }
     }
 
     @Override
     public boolean supportsPolling() {
         return false; // polling is not implemented yet
     }
 
 
     @Override
     public SCMRevisionState calcRevisionsFromBuild(AbstractBuild<?, ?> build, Launcher launcher, TaskListener listener) throws IOException, InterruptedException {
         PrintStream output = listener.getLogger();
         output.println("Getting local revision...");
 
         return new DarcsRevisionState();
     }
 
     @Override
     public boolean checkout(AbstractBuild<?, ?> build, Launcher launcher, FilePath workspace, BuildListener listener, File changelogFile) throws IOException, InterruptedException {
        boolean existsRepoinWorkspace = workspace.act(new FileCallable<Boolean>() {
 
             private static final long serialVersionUID = 1L;
 
             public Boolean invoke(File ws, VirtualChannel channel) throws IOException {
                 File file = new File(ws, "_darcs");
                 return file.exists();
             }
         });
 
        if (existsRepoinWorkspace && !isClean()) {
             return pullRepo(build, launcher, workspace, listener, changelogFile);
         } else {
             return getRepo(build, launcher, workspace, listener, changelogFile);
         }
     }
 
     /**
      * Counts the patches in an repo.
      * 
      * @param build
      * @param launcher
      * @param workspace
      * @param listener
      * @return int
      * @throws InterruptedException
      * @throws IOException
      */
     private int countPatches(AbstractBuild<?, ?> build, Launcher launcher, FilePath workspace, BuildListener listener) throws InterruptedException, IOException {
         ByteBuffer baos = new ByteBuffer();
         launcher.launch()
                 .cmds(getDescriptor().getDarcsExe(), "changes", "--count", "--repodir=" + workspace)
                 .envs(build.getEnvironment(listener))
                 .stdout(baos);
 
         return Integer.parseInt(baos.toString().trim());
     }
 
     /**
      * Pulls all patches from a remote repo in the workspace repo.
      *
      * @param build
      * @param launcher
      * @param workspace
      * @param listener
      * @param changelogFile
      * @return boolean
      * @throws InterruptedException
      * @throws IOException
      */
     private boolean pullRepo(AbstractBuild<?, ?> build, Launcher launcher, FilePath workspace, BuildListener listener, File changelogFile) throws InterruptedException, IOException {
        logger.log(Level.INFO, "Pulling repo from: {0}", source);
         int preCnt = 0, postCnt = 0;
 
         try {
             preCnt = countPatches(build, launcher, workspace, listener);
             ProcStarter proc = launcher.launch()
                                        .cmds(getDescriptor().getDarcsExe(), "pull", source, "--repodir=" + workspace )
                                        .envs(build.getEnvironment(listener))
                                        .stdout(listener.getLogger())
                                        .pwd(workspace);
 
             if (proc.join() != 0) {
                 listener.error("Failed to pull");
 
                 return false;
             }
 
             postCnt = countPatches(build, launcher, workspace, listener);
             getLog(launcher, postCnt - preCnt, workspace, changelogFile);
         } catch (IOException e) {
             listener.error("Failed to pull");
             
             return false;
         }
 
         return true;
     }
 
     /**
      * Gets a fresh copy of a remote repo.
      *
      * @param build
      * @param launcher
      * @param workspace
      * @param listener
      * @param changelogFile
      * @return boolean
      * @throws InterruptedException
      */
     private boolean getRepo(AbstractBuild<?, ?> build, Launcher launcher, FilePath workspace, BuildListener listener, File changelogFile) throws InterruptedException {
        logger.log(Level.INFO, "Getting repo from: {0}", source);
        
         try {
             workspace.deleteRecursive();
         } catch (IOException e) {
             e.printStackTrace(listener.error("Failed to clean the workspace"));
             return false;
         }
 
         ArgumentListBuilder args = new ArgumentListBuilder();
         args.add(getDescriptor().getDarcsExe(), "get");
         args.add(source, workspace.getRemote());
 
         try {
             ProcStarter proc = launcher.launch()
                                        .cmds(args)
                                        .envs(build.getEnvironment(listener))
                                        .stdout(listener.getLogger());
             
             if (proc.join() != 0) {
                 listener.error("Failed to get " + source);
 
                 return false;
             }
         } catch (IOException e) {
             e.printStackTrace(listener.error("Failed to get " + source));
             
             return false;
         }
 
         return createEmptyChangeLog(changelogFile, listener, "changelog");
     }
 
     @Override
     protected PollingResult compareRemoteRevisionWith(AbstractProject<?, ?> ap, Launcher launcher, FilePath fp, TaskListener listener, SCMRevisionState localRevisionState) throws IOException, InterruptedException {
         PrintStream output = listener.getLogger();
         output.printf("Getting current remote revision...");
 
         final DarcsRevisionState remoteRevisionState = new DarcsRevisionState();
         final Change change;
         
         if (true) { // is changed?
             change = Change.SIGNIFICANT;
         } else {
             change = Change.NONE;
         }
 
         return new PollingResult(localRevisionState, remoteRevisionState, change);
     }
 
     @Override
     public ChangeLogParser createChangeLogParser() {
         return new DarcsChangeLogParser();
     }
 
     @Override
     public DescriptorImpl getDescriptor() {
         return DescriptorImpl.DESCRIPTOR;
     }
 
     /**
      * Inner class of the SCM descitopr.
      *
      * Contains the global configuration options as fields.
      */
     public static final class DescriptorImpl extends SCMDescriptor<DarcsScm> {
         @Extension
         public static final DescriptorImpl DESCRIPTOR = new DescriptorImpl();
         private String darcsExe;
         private transient String version;
 
         private DescriptorImpl() {
             super(DarcsScm.class, null);
             load();
         }
 
         public String getDisplayName() {
             return "Darcs";
         }
 
         public String getDarcsExe() {
             return (null == darcsExe) ? "darcs" : darcsExe;
         }
 
         @Override
         public SCM newInstance(StaplerRequest req, JSONObject formData) throws FormException {
             DarcsScm scm = req.bindJSON(DarcsScm.class, formData);
             
             return scm;
         }
 
         @Override
         public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
             darcsExe = req.getParameter("darcs.darcsExe");
             version  = null;
             save();
 
             return true;
         }
 
         public FormValidation doDarcsExeCheck(@QueryParameter final String value) throws IOException, ServletException {
             return FormValidation.validateExecutable(value, new FormValidation.FileValidator() {
                 @Override public FormValidation validate(File exe) {
                     try {
                        ByteBuffer baos   = new ByteBuffer();
                        Launcher launcher = Hudson.getInstance().createLauncher(TaskListener.NULL);
                        ProcStarter proc  = launcher.launch()
                                                    .cmds(getDarcsExe(), "--version")
                                                    .stdout(baos);
                        if (proc.join() == 0) {
                           return FormValidation.ok();
                        } else {
                           return FormValidation.warning("Could not locate the executable in path");
                        }
                     } catch (IOException e) {
                         // failed
                         return FormValidation.error(e.toString());
                     } catch (InterruptedException e) {
                         // failed
                         return FormValidation.error(e.toString());
                     }
 
                     //return FormValidation.error("Unable to check darcs version");
                 }
             });
         }
     }
 }
