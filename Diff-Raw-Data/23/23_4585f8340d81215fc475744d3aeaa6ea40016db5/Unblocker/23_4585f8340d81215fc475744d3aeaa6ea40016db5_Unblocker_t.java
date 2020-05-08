import java.io.PrintWriter;

 /**
  * <b>Unblocker Class</b>
  * <p>
  * Represents the runnable unblock task that unblocks any IPs from the server
  * that have been blocked for over a certain amount of time (time is set in 
  * Server as BLOCKTIME).
  * - Implements Runnable interface
  * @author James Wen - jrw2175
  */
 class Unblocker implements Runnable {
 	private Server server;
	private PrintWriter logger;
 	/**
 	 * <b>Unblocker constructor</b>
 	 * <p>
 	 * Creates Unblocker runnable task that works with a server and its 
 	 * blockedIPs list.
 	 * @param server - the Server that IPs are to be unblocked on
 	 */
	public Unblocker(Server server, PrintWriter logger){
 		this.server = server;
		this.logger = logger;
 	}
 		
 	/**
 	 * <b>run</b>
 	 * <p>
 	 * Runs the task that removes all IPs that have been blocked for longer than
 	 * the server's set BLOCKTIME from the server's blockedIP list 
 	 */
 	public void run(){
 		for(BlockedIP ip : server.getBlockedIPs()){
 			//If this IP address has been blocked for longer than server's BLOCKTIME
 			if(ip.toUnblock()){
 				server.removeBlockedIP(ip);
				logger.println("[ " + System.currentTimeMillis() + " ] - IP Address " + ip.getIP().toString() + " is now unblocked");
				logger.flush();
 			}
 		}
 	}
 }
