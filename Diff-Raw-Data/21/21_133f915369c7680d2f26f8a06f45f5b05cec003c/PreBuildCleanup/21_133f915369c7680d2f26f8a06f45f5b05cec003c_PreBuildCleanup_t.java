 package org.jenkinsci.plugins.proccleaner;
 
 import static hudson.util.jna.GNUCLibrary.LIBC;
 import hudson.Extension;
 import hudson.Launcher;
 import hudson.model.BuildListener;
 import hudson.model.AbstractBuild;
 import hudson.model.Descriptor;
 import hudson.remoting.VirtualChannel;
 import hudson.tasks.BuildWrapper;
 
 import java.io.IOException;
 
 import org.kohsuke.stapler.DataBoundConstructor;
 
 
 public class PreBuildCleanup extends BuildWrapper {
 
 	private final ProcCleaner cleaner;
 
 	@DataBoundConstructor
 	public PreBuildCleanup(ProcCleaner cleaner) {
 		this.cleaner = cleaner;
 	}
 
 	public ProcCleaner getCleaner() {
 		return cleaner;
 	}
 	
 	/*@Override
 	public DescriptorImpl getDescriptor() {
 		return (DescriptorImpl) super.getDescriptor();
 	}*/
 
 	
 	 @Override 
 	 public Environment setUp( AbstractBuild build, Launcher launcher, BuildListener listener ) throws IOException, InterruptedException {
 		 
 		 listener.getLogger().println("[Process cleanup]");
 		 
 		 if(!launcher.isUnix()) {
 			 listener.getLogger().println("Not a Linux/Unix slave, skipping Process cleanup");
 			 return new NoopEnv();
 		 }
 		 
 		 cleaner.setLog(listener);
 		 //cleaner.doCleanup();
 		 VirtualChannel c = launcher.getChannel();
 		 try {
 			 System.out.println("[proc-cleanup] Calling remote cleaner");
 			 c.call(cleaner);
 		 } catch(Exception e) {
			 System.out.println("PROC-CLEANER EXCEPTION");
 			 e.printStackTrace();
 		 }
 		 
		 cleaner.setLog(null);
		 
 		 /*VirtualChannel c = launcher.getChannel();
 		 try {
 			 c.call(new KillCommand());
 		 } catch(Exception e) {
 			 e.printStackTrace();
 		 }
 		 int pid = LIBC.getpid();
 		 int ppid = LIBC.getppid();
 		 String[] cmd = {"ps","-u","vjuranek","-o","pid,ppid,args"};
 		 OutputStream out = new ByteArrayOutputStream();
 		 Channel c = launcher.getChannel().call(arg0);
 		 int ec = launcher.launch().cmds(cmd).stdout(out).start().join();
 		 System.out.println("PID je " + pid);
 		 System.out.println("PPID je " + ppid);
 		 //System.out.println("PROC jsou" + out.toString());
 */		 return new NoopEnv();
 	 }
 
 	@Extension
 	public static final class DescriptorImpl extends Descriptor<BuildWrapper> {
 
 		public String getDisplayName() {
 			return "Process cleanup";//Messages.PreBuildCleanup_Delete_workspace();
 		}
 
 	}
 
 	class NoopEnv extends Environment{
 	}
 
 }
