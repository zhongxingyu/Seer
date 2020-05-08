 package com.digitaluppercut.dmssplugin;
 
 import java.util.logging.Logger;
 import java.lang.Thread;
 
 /**
 *
 * @author Clinton Alexander & Jake Clarke
 */
 public class DeadMansSwitchServer extends Plugin  {
 	protected static final Logger log = Logger.getLogger("Minecraft");
 	private String name = "DeadMansSwitchServer";
 	private String version = "0.1";
	private DMSControlThread ct = null;
 		
 	public void enable() {
 		
		if(ct == null || !ct.isAlive())
 		{
			ct = new DMSControlThread();
			ct.start();
 		}
 		
 	}
 	
 	public void disable() {
 		
		ct.haltThread();
 	}
 
 	public void initialize() {
 		log.info(name + " " + version + " initialized");
 	}
 
 	// Sends a message to all players!
 	public void broadcast(String message) {
 		for (Player p : etc.getServer().getPlayerList()) {
 			p.sendMessage(message);
 		}
 	}
 	
 	private class DMSControlThread extends Thread {
 		
 		boolean threadEnabled;
 		int waited = 0, waitMax = 6000;
 		final int waitMS = 1000;
 		
 		public void run()
 		{
 			
 			threadEnabled = true;
 			while(threadEnabled)
 			{
 				if(waitMax <= waited)
 				{
 					// run the check in code.
 					
					waited = 0;
 				}
 				
 				// We have finished running we should wait for a while. 
 				Thread.sleep(waitMS);
 				// add the amount of time waited to the total
 				waited += waitMS;
 			}
 			
 		}
 		
 		
 		public void haltThread()
 		{
 			threadEnabled = false;
 		
 		}
 	}
 	
 }
