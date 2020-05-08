 /*
  * LICENSE
  *
  * "THE BEER-WARE LICENSE" (Revision 42):
  * "Sven Strittmatter" <ich@weltraumschaf.de> wrote this file.
  * As long as you retain this notice you can do whatever you want with
  * this stuff. If we meet some day, and you think this stuff is worth it,
  * you can buy me a beer in return.
  */
 
 package org.jenkinsci.plugins.darcs;
 
 import org.jenkinsci.plugins.darcs.browsers.DarcsRepositoryBrowser;
 
 import hudson.EnvVars;
 import hudson.Extension;
 import hudson.FilePath;
 import hudson.FilePath.FileCallable;
 import hudson.Launcher;
 import hudson.Launcher.LocalLauncher;
 import hudson.Launcher.ProcStarter;
 import hudson.Util;
 import hudson.model.Hudson;
 import hudson.model.AbstractBuild;
 import hudson.model.AbstractProject;
 import hudson.model.BuildListener;
 import hudson.model.TaskListener;
 import hudson.remoting.VirtualChannel;
 import hudson.scm.ChangeLogParser;
 import hudson.scm.PollingResult;
 //import hudson.scm.PollingResult.Change;
 import hudson.scm.PollingResult.Change;
 import hudson.scm.RepositoryBrowsers;
 import hudson.scm.SCM;
 import hudson.scm.SCMRevisionState;
 import hudson.scm.SCMDescriptor;
 import hudson.util.FormValidation;
 import java.io.ByteArrayInputStream;
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
 import org.xml.sax.InputSource;
 import org.xml.sax.XMLReader;
 import org.xml.sax.helpers.XMLReaderFactory;
 
 /**
  * Darcs is a patch based distributed version control system.
  *
  * Contains the job configuration options as fields.
  *
  * @see http://darcs.net/
  *
  * @author Sven Strittmatter <ich@weltraumschaf.de>
  * @author Ralph Lange <Ralph.Lange@gmx.de>
  */
 public class DarcsScm extends SCM implements Serializable {
 
     private static final long serialVersionUID = 1L;
     private static final Logger LOGGER = Logger.getLogger(DarcsScm.class.getName());
     /**
      * Source repository URL from which we pull.
      */
     private final String source;
     /**
      * Whether to wipe the checked out repo.
      */
     private final boolean clean;
     DarcsRepositoryBrowser browser;
 
     public DarcsScm(String source) {
         this(source, false, null);
     }
 
     @DataBoundConstructor
     public DarcsScm(String source, boolean clean, DarcsRepositoryBrowser browser) {
         this.source  = source;
         this.clean   = clean;
         this.browser = browser;
     }
 
     public String getSource() {
         return source;
     }
 
     public boolean isClean() {
         return clean;
     }
 
     @Override
     public DarcsRepositoryBrowser getBrowser() {
         return browser;
     }
 
     @Override
     public boolean supportsPolling() {
         return false;
     }
 
     @Override
     public boolean requiresWorkspaceForPolling() {
         return false;
     }
 
     /**
      *
      * @param build
      * @param launcher
      * @param listener
      * @return
      * @throws IOException
      * @throws InterruptedException
      */
     @Override
     public DarcsRevisionState calcRevisionsFromBuild(AbstractBuild<?, ?> build, Launcher launcher, TaskListener listener) throws IOException, InterruptedException {
         PrintStream output = listener.getLogger();
         output.println("Getting local revision...");
         DarcsRevisionState local = getRevisionState(launcher,
                                                     listener,
                                                     build.getWorkspace().getRemote());
         output.println(local);
 
         return local;
     }
 
     @Override
     protected PollingResult compareRemoteRevisionWith(AbstractProject<?, ?> ap, Launcher launcher, FilePath fp, TaskListener listener, SCMRevisionState localRevisionState) throws IOException, InterruptedException {
        PrintStream output = listener.getLogger();
         final Change change;
         final DarcsRevisionState remote = getRevisionState(launcher, listener, source);
 
        output.printf("Getting current remote revision...");
        output.println(remote);
        output.printf("Baseline is %s.\n", localRevisionState);

         if ((SCMRevisionState.NONE == localRevisionState)
             // appears that other instances of None occur - its not a singleton.
             // so do a (fugly) class check.
             || (localRevisionState.getClass() != DarcsRevisionState.class)
             || (!remote.equals(localRevisionState))) {
             change = Change.SIGNIFICANT;
         } else {
             change = Change.NONE;
         }
 
         return new PollingResult(localRevisionState, remote, change);
     }
 
     /**
      * Calculates the revision state of a repository (local or remote).
      *
      * @param launcher
      * @param listener
      * @param repo
      * @return
      * @throws InterruptedException
      */
     private DarcsRevisionState getRevisionState(Launcher launcher, TaskListener listener, String repo) throws InterruptedException {
         DarcsRevisionState rev = null;
 
         if (null == launcher) {
             /* Create a launcher on master
              * todo better grab a launcher on 'any slave'
              */
             launcher = new LocalLauncher(listener);
         }
 
         DarcsCmd cmd = new DarcsCmd(launcher, EnvVars.masterEnvVars,
                                     getDescriptor().getDarcsExe());
 
         try {
             byte[] changes          = cmd.allChanges(repo).toByteArray();
             XMLReader       xr      = XMLReaderFactory.createXMLReader();
             DarcsSaxHandler handler = new DarcsSaxHandler();
 
             xr.setContentHandler(handler);
             xr.setErrorHandler(handler);
             xr.parse(new InputSource(new ByteArrayInputStream(changes)));
 
             rev = new DarcsRevisionState(new DarcsChangeSetList(null, handler.getChangeSets()));
         } catch (Exception e) {
             StringWriter w = new StringWriter();
             e.printStackTrace(new PrintWriter(w));
             LOGGER.log(Level.WARNING, "Failed to get revision state for repository: ", e);
         }
 
         return rev;
     }
 
     /**
      * Writes the changelog of the last numPatches to the changeLog file.
      *
      * @param launcher
      * @param numPatches
      * @param workspace
      * @param changeLog
      * @throws InterruptedException
      */
     private void createChangeLog(Launcher launcher, int numPatches, FilePath workspace, File changeLog, BuildListener listener) throws InterruptedException {
         if (0 == numPatches) {
             LOGGER.info("Creating empty changelog.");
             createEmptyChangeLog(changeLog, listener, "changelog");
             return;
         }
 
         DarcsCmd cmd = new DarcsCmd(launcher, EnvVars.masterEnvVars,
                                     getDescriptor().getDarcsExe());
 
         try {
             FileOutputStream fos          = new FileOutputStream(changeLog);
             ByteArrayOutputStream changes = cmd.lastSummarizedChanges(workspace.getRemote(), numPatches);
 
             changes.writeTo(fos);
             fos.close();
         } catch (Exception e) {
             StringWriter w = new StringWriter();
             e.printStackTrace(new PrintWriter(w));
             LOGGER.log(Level.WARNING, "Failed to get log from repository: ", e);
         }
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
      * Counts the patches in a repo.
      *
      * @param build
      * @param launcher
      * @param workspace
      * @param listener
      * @return int
      * @throws InterruptedException
      * @throws IOException
      */
     private int countPatches(AbstractBuild<?, ?> build, Launcher launcher, FilePath workspace, BuildListener listener) {
         try {
             DarcsCmd cmd = new DarcsCmd(launcher,
                                         build.getEnvironment(listener),
                                         getDescriptor().getDarcsExe());
 
             return cmd.countChanges(workspace.getRemote());
         } catch (Exception e) {
             listener.error("Failed to count patches in workspace repo:\n", e.toString());
             return 0;
         }
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
         LOGGER.log(Level.INFO, "Pulling repo from: {0}", source);
         int preCnt = 0, postCnt = 0;
         preCnt = countPatches(build, launcher, workspace, listener);
         LOGGER.log(Level.INFO, "Count of patches pre pulling is {0}", preCnt);
 
         try {
             DarcsCmd cmd = new DarcsCmd(launcher,
                                         build.getEnvironment(listener),
                                         getDescriptor().getDarcsExe());
             cmd.pull(workspace.getRemote(), source);
         } catch (Exception e) {
             listener.error("Failed to pull: " + e.toString());
 
             return false;
         }
 
         postCnt = countPatches(build, launcher, workspace, listener);
         LOGGER.log(Level.INFO, "Count of patches post pulling is {0}", preCnt);
         createChangeLog(launcher, postCnt - preCnt, workspace, changelogFile, listener);
 
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
     private boolean getRepo(AbstractBuild<?, ?> build, Launcher launcher, FilePath workspace, BuildListener listener, File changeLog) throws InterruptedException {
         LOGGER.log(Level.INFO, "Getting repo from: {0}", source);
 
         try {
             workspace.deleteRecursive();
         } catch (IOException e) {
             e.printStackTrace(listener.error("Failed to clean the workspace"));
             return false;
         }
 
         try {
             DarcsCmd cmd = new DarcsCmd(launcher,
                                         build.getEnvironment(listener),
                                         getDescriptor().getDarcsExe());
             cmd.get(workspace.getRemote(), source);
         } catch (Exception e) {
             e.printStackTrace(listener.error("Failed to get repo from " + source));
 
             return false;
         }
 
         return createEmptyChangeLog(changeLog, listener, "changelog");
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
      * Inner class of the SCM descriptor.
      *
      * Contains the global configuration options as fields.
      */
     public static final class DescriptorImpl extends SCMDescriptor<DarcsScm> {
 
         @Extension
         public static final DescriptorImpl DESCRIPTOR = new DescriptorImpl();
         private String darcsExe;
 
         private DescriptorImpl() {
             super(DarcsScm.class, DarcsRepositoryBrowser.class);
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
             scm.browser  = RepositoryBrowsers.createInstance(DarcsRepositoryBrowser.class,
                                                              req,
                                                              formData,
                                                              "browser");
 
             return scm;
         }
 
         @Override
         public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
             darcsExe = Util.fixEmpty(req.getParameter("darcs.darcsExe").trim());
             save();
 
             return true;
         }
 
         public FormValidation doDarcsExeCheck(@QueryParameter final String value) throws IOException, ServletException {
             return FormValidation.validateExecutable(value, new FormValidation.FileValidator() {
 
                 @Override
                 public FormValidation validate(File exe) {
                     try {
                         ByteBuffer  baos     = new ByteBuffer();
                         Launcher    launcher = Hudson.getInstance()
                                                      .createLauncher(TaskListener.NULL);
                         ProcStarter proc     = launcher.launch()
                                                        .cmds(exe, "--version")
                                                        .stdout(baos);
 
                         if (proc.join() == 0) {
                             return FormValidation.ok();
                         } else {
                             return FormValidation.warning("Could not locate the executable in path");
                         }
                     } catch (IOException e) {
                         // failed
                         LOGGER.log(Level.WARNING, e.toString());
                     } catch (InterruptedException e) {
                         // failed
                         LOGGER.log(Level.WARNING, e.toString());
                     }
 
                     return FormValidation.error("Unable to check darcs version");
                 }
             });
         }
     }
 }
