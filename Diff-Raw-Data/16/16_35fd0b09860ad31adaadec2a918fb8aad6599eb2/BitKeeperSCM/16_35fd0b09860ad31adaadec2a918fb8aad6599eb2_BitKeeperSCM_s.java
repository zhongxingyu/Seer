 package hudson.plugins.bitkeeper;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.io.PrintStream;
 import java.util.ArrayList;
 import java.util.Map;
 import java.util.Collections;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import javax.servlet.ServletException;
 
 import hudson.AbortException;
 import hudson.EnvVars;
 import hudson.FilePath;
 import hudson.Launcher;
 import hudson.Proc;
 import hudson.Util;
 import hudson.model.AbstractBuild;
 import hudson.model.AbstractProject;
 import hudson.model.BuildListener;
 import hudson.model.Hudson;
 import hudson.model.Run;
 import hudson.model.TaskListener;
 import hudson.scm.ChangeLogParser;
 import hudson.scm.SCM;
 import hudson.scm.SCMDescriptor;
 import hudson.util.ByteBuffer;
 import hudson.util.FormFieldValidator;
 import hudson.util.VersionNumber;
 
 import org.kohsuke.stapler.DataBoundConstructor;
 import org.kohsuke.stapler.StaplerRequest;
 import org.kohsuke.stapler.StaplerResponse;
 
 public class BitKeeperSCM extends SCM {
 	/**
      * Source repository URL from which we pull.
      */
     private final String parent;
 
     /**
      * Local name of the repository
      */
     private final String localRepository;
 
     /**
      * Whether we should use 'bk pull' to update the local repository
      * If not, we clean out the repo, and clone a fresh copy
      */
     private final boolean usePull;
     
     /**
      * Specifies whether pull and clone commands should run in quiet mode
      * By default, these commands print out all files that have changed.
      * Since cloning can be quite verbose, turning on quiet mode can make the console
      * output much more useful
      */
     private final boolean quiet;
     
     /** 
      * How many times to retry clone/pull operations before declaring the build a failure
      */
     private final int maxAttempts = 9;
     
     @DataBoundConstructor
     public BitKeeperSCM(String parent, String localRepo, boolean usePull, boolean quiet) {
         this.parent = parent;
         this.localRepository = localRepo;
         this.usePull = usePull;
         this.quiet = quiet;
     }
 
     /**
      * Gets the source repository path.
      * Either URL or local file path.
      */
     public String getParent() {
         return parent;
     }
     
     /**
      * Gets the local repository directory.
      * Must be a local file path.
      */
     public String getLocalRepository() {
         return localRepository;
     }
     
     public boolean isUsePull() {
     	return usePull;
     }
 
     public boolean isQuiet() {
     	return quiet;
     }
 
     @Override
 	public FilePath getModuleRoot(FilePath workspace) {
 		return workspace.child(this.localRepository);
 	}
 
     @Override
     public boolean checkout(AbstractBuild build, Launcher launcher,
         FilePath workspace, BuildListener listener, File changelogFile)
         throws IOException, InterruptedException {
        FilePath localRepo = workspace.child(localRepository);
         if(!this.usePull) {
       	    localRepo.deleteRecursive();
        }
        if(!localRepo.exists()) {
         	cloneLocalRepo(build, launcher, listener, workspace);
         } else {
             pullLocalRepo(build, launcher, listener, workspace);
         }
         
         saveChangelog(build, launcher, listener, changelogFile, localRepo);
 
         String mostRecent = 
             this.getLatestChangeset(
                 build.getEnvVars(), launcher, workspace, 
                 this.localRepository, listener
             );
         build.addAction(new BitKeeperTagAction(build, mostRecent));
         return true;
 	}
 
 	private void pullLocalRepo(AbstractBuild build, Launcher launcher, 
 			BuildListener listener, FilePath workspace) 
 	throws IOException, InterruptedException, AbortException {
 		FilePath localRepo = workspace.child(localRepository);
 		PrintStream output = listener.getLogger();
 		
     	ArrayList<String> args = new ArrayList<String>();
     	args.add(getDescriptor().getBkExe());
     	args.add("pull");
     	args.add("-u");
     	args.add("-c" + maxAttempts);
     	if(quiet) args.add("-q");
     	args.add(parent);
 		if(launcher.launch(
 		        args.toArray(new String[args.size()]),
 		        build.getEnvVars(), output,localRepo).join() != 0) 
 		{
 		        listener.error("Failed to pull from " + parent);
 		        throw new AbortException();        	
 		}
 		output.println("Pull completed");
 	}
 
 	private void saveChangelog(AbstractBuild build, Launcher launcher, BuildListener listener,
 			File changelogFile, FilePath localRepo)
 			throws IOException, InterruptedException, FileNotFoundException,
 			AbortException {
             OutputStream changelog = null;
             Run prevBuild = build.getPreviousBuild();
             BitKeeperTagAction tagAction = 
                 prevBuild == null ? null : prevBuild.getAction(BitKeeperTagAction.class);
             String recentCset = tagAction == null ? null : tagAction.getCsetkey();
             try {
                 changelog = new FileOutputStream(changelogFile);
                 if(recentCset == null || recentCset.equals("")) {
                     listener.error("No most recent changeset available for changelog");
                     return;
                 }
 
                 if(launcher.launch(
                     new String[]{
                         getDescriptor().getBkExe(),
                         "changes",
                 	"-v", 
                 	"-r" + recentCset + "..",
                 	"-d$if(:CHANGESET:){U :USER:\n$each(:C:){C (:C:)\n}}$unless(:CHANGESET:){F :GFILE:\n}"
                     },
                     build.getEnvVars(), changelog,localRepo).join() != 0) 
                 {
                     listener.error("Failed to save changelog");
                     throw new AbortException();        	
                 }
 	    } finally {
                 if(changelog != null)
                     changelog.close();
             }
             listener.getLogger().println("Changelog saved");
 	}
 
 	@Override
 	public ChangeLogParser createChangeLogParser() {
 		return new BitKeeperChangeLogParser();
 	}
 
 	@Override
 	public DescriptorImpl getDescriptor() {
 		return DescriptorImpl.DESCRIPTOR;
 	}
 	
 	@Override
 	public boolean requiresWorkspaceForPolling() {
 		return false;
 	}
 
 	@Override
 	public boolean pollChanges(AbstractProject project, Launcher launcher,
 			FilePath workspace, TaskListener listener) throws IOException,
 			InterruptedException {
         PrintStream output = listener.getLogger();
         Run lastBuild = project.getLastBuild();
         BitKeeperTagAction tagAction = 
             lastBuild == null ? null : lastBuild.getAction(BitKeeperTagAction.class);
         String recentCset = tagAction == null ? null : tagAction.getCsetkey();
         String cset = 
             this.getLatestChangeset(Collections.<String,String>emptyMap(), launcher, workspace, parent, listener);
         return !(cset.equals(recentCset));
     }
 	
 	private String getLatestChangeset(Map<String, String> env, Launcher launcher, 
 			FilePath workspace, String repository, TaskListener listener) 
 	throws IOException, InterruptedException 
 	{
 		ByteArrayOutputStream baos = new ByteArrayOutputStream();
     	if(launcher.launch(
                 new String[]{getDescriptor().getBkExe(),"changes","-r+", "-d:CSETKEY:", "-D", repository},
                 env, baos,workspace).join()!=0) {
     		// dump the output from bk to assist trouble-shooting.
             Util.copyStream(new ByteArrayInputStream(baos.toByteArray()),listener.getLogger());
             listener.error("Failed to check the latest changeset");
             throw new AbortException();
     	}
         // obtain the current changeset
         String rev = null;
         for( String line : Util.tokenize(new String(baos.toByteArray(), "ASCII"),"\r\n") ) {
             line = line.trim();
             rev = line;
             break;
         }
         if(rev==null) {
             Util.copyStream(new ByteArrayInputStream(baos.toByteArray()),listener.getLogger());
             listener.error("Failed to identify a revision");
             throw new AbortException();
         }
 
     	return rev;
 	}
 	
     private void cloneLocalRepo(AbstractBuild build, Launcher launcher, 
     		TaskListener listener, FilePath workspace) 
     throws InterruptedException, IOException 
     {
     	ArrayList<String> args = new ArrayList<String>();
     	args.add(getDescriptor().getBkExe());
     	args.add("clone");
     	if(quiet) args.add("-q");
     	args.add(parent);
     	args.add(localRepository);
     	PrintStream output = listener.getLogger();
     	
     	int attempt = 0;
     	int result = 0;
     	do {
     		if(result != 0) {
     			listener.error("Retrying clone");
     		}
     		result = launcher.launch(
     				args.toArray(new String[args.size()]),
     				build.getEnvVars(), output,workspace).join();
     	} while(++attempt < maxAttempts && result != 0);
     	
     	if(result != 0) {
     		listener.error("Failed to clone after " + maxAttempts + " attempts from " + this.parent);
     		throw new AbortException();
     	}
     	
     	output.println("New clone made");
     }
 
 	public static final class DescriptorImpl extends SCMDescriptor<BitKeeperSCM> {
         public static final DescriptorImpl DESCRIPTOR = new DescriptorImpl();
 
         private String bkExe;
 
         private  DescriptorImpl() {
             super(BitKeeperSCM.class, null);
             load();
         }
 
         public String getDisplayName() {
             return "BitKeeper";
         }
 
         /**
          * Path to BitKeeper executable.
          */
         public String getBkExe() {
             if(bkExe==null) return "bk";
             return bkExe;
         }
 
         public SCM newInstance(StaplerRequest req) throws FormException {
             return new BitKeeperSCM(
             		req.getParameter("bitkeeper.parent"),
             		req.getParameter("bitkeeper.localRepository"),
             		req.getParameter("bitkeeper.usePull")!=null,
             		req.getParameter("bitkeeper.quiet")!=null
             );
         }
 
         public boolean configure(StaplerRequest req) throws FormException {
             bkExe = req.getParameter("bitkeeper.bkExe");
             save();
             return true;
         }
 
         public void doBkExeCheck(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException {
             new FormFieldValidator.Executable(req,rsp) {
                 protected void checkExecutable(File exe) throws IOException, ServletException {
                     ByteBuffer baos = new ByteBuffer();
                     try {
                         Proc proc = Hudson.getInstance().createLauncher(TaskListener.NULL).launch(
                                 new String[]{getBkExe(), "version"}, new String[0], baos, null);
                         proc.join();
 
                         Matcher m = VERSION_STRING.matcher(baos.toString());
                         if(m.find()) {
                             try {
                                 if(new VersionNumber(m.group(1)).compareTo(V4_0_1)>=0) {
                                     ok(); // right version
                                 } else {
                                     error("This bk is version "+m.group(1)+" but we need 4.0.1+");
                                 }
                             } catch (IllegalArgumentException e) {
                                 warning("Hudson can't tell if this bk is 4.0.1 or later (detected version is %s)",m.group(1));
                             }
                             return;
                         }
                     } catch (IOException e) {
                         // failed
                     } catch (InterruptedException e) {
                         // failed
                     }
                     error("Unable to check bk version");
                 }
             }.process();
         }
 
         /**
          * Pattern matcher for the version number.
          */
         private static final Pattern VERSION_STRING = Pattern.compile("BitKeeper version is bk-([0-9.]+)");
 
         private static final VersionNumber V4_0_1 = new VersionNumber("4.0.1");
     }
 
 
 }
