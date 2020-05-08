 package org.jenkinsci.plugins.proccleaner;
 
 import hudson.Extension;
 
 import java.io.IOException;
 
 import org.kohsuke.stapler.DataBoundConstructor;
 
 public class PsCleaner extends ProcCleaner {
 	
 	private final String killerType;
 	private final PsKiller killer;
 	
 	@DataBoundConstructor
 	public PsCleaner(String killerType) {
 		this.killerType = killerType;
 		this.killer = PsKiller.all().getDynamic(killerType);
		super.setLog(null);
 	}
 	
 	public String getKillerType() {
 		return killerType;
 	}
 	
 	public Void call() throws Exception {
 		System.out.println("[proc-cleanup] Calling killer");
 		try {
			System.out.println("pred kill, log je " + getLog());
 			killer.kill("test_hudson",getLog().getLogger());
			System.out.println("po kill");
 		} catch(IOException e) {
 			e.printStackTrace();
 		} catch(InterruptedException e) {
 			e.printStackTrace();
 		}
 		return null;
 	}
 	
 	/*public void doCleanup() {
 		try {
 			killer.kill("vjuranek",getLog());
 		} catch(IOException e) {
 			e.printStackTrace();
 		} catch(InterruptedException e) {
 			e.printStackTrace();
 		}
 	}*/
 	
 	@Extension
 	public static class PsCleanerDescriptor extends ProcCleanerDescriptor {
 		public String getDisplayName() {
 			return "Ps-based process cleaner";
 		}
 	}
 	
 }
