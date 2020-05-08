 package p2pChat;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.PrintStream;
 import java.net.Socket;
 import java.net.SocketException;
 
 public class NextOutput implements Runnable {
 	
 	/* doReconnect(String IP)
 	 * 
 	 * Closes the current next socket, and opens a new next socket to IP.
 	 */
 	protected static void doReconnect(String IP)
 	{
 		if (Peer.debug)
 			System.out.println("NextOutput attempting to connect to " + IP);
 	    try {
 		    Peer.nextOut.close();
 		    Peer.nextIn.close();
 		    Peer.next.close();
 			
 		    Peer.next = new Socket(IP, Peer.serverPort);
 			Peer.nextIn = new BufferedReader(new InputStreamReader(Peer.next.getInputStream()));
 			Peer.nextOut = new PrintStream(Peer.next.getOutputStream(), true);
 		
 			Peer.setHold(false);
 	    } catch (SocketException e) {
 			//do nothing. really! SocketExceptions are expected when 
 	    	//reconnecting.
 		} catch (IOException e) {
 			System.out.println("Something unexpected went wrong. Please try" +
 					"again.");
 			System.exit(1);
 		}
 	}
 	
 	
 	public void run() {
		String lastExitMsg = "";
 		//debug code
 		if (Peer.debug)
 			System.out.println("NEXT: " + Peer.next.toString());
 	    
 		//main execution loop
 		MAINLOOP:while (!Peer.getQuit())
 	    {
 			//if we are supposed to reconnect to someone, do it!
 			if (!Peer.reconnectQueue.isEmpty()) {
 				doReconnect(Peer.reconnectQueue.remove());
 			}
 			
 			//if we are not mid-reconnect, send the first message in the chat
 			//queue.
 			if (!Peer.chatQueue.isEmpty() && !Peer.hold)
 			{
 				String message = Peer.chatQueue.remove();
 				
				//checks any exit messages against the last exit message;
				//if it's a duplicate, discards it.
 				if (message.startsWith("  "))
 					if (message.equals(lastExitMsg)) 
 					{
 						continue MAINLOOP;
 					}
 					else lastExitMsg = message;
 					
 				//make sure it's not my message before printing to stdIn
 				if (!message.startsWith(Peer.myIP) &&
 						!message.substring(5).startsWith(Peer.myIP)) 
 					System.out.println(message);
 			    Peer.nextOut.println(message);
 			}
 	    }
 	}
 }
