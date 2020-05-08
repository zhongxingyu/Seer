 package p2pChat;
 
 import java.io.IOException;
 
 public class NextInput implements Runnable {
 	boolean reconnectDone;
 
 	
 	@SuppressWarnings("unused")
 	@Override
 	public void run() {
 		String housekeeping;
 		
 		//main execution loop
 		try {
 			while (!Peer.getQuit()) {
 				//try to read in a housekeeping message
 				if((housekeeping = Peer.nextIn.readLine()) != null)
 				{   
 					//debug code
 					if (Peer.debug) System.out.println("Housekeeping received: " + housekeeping);
 					
 					//if "hold" wait for second message with IP to connect to
 					if (housekeeping.toLowerCase().equals("hold"))
 				    {
 				    	Peer.setHold(true);
 				    	reconnectDone = false;
				    	while(Peer.getHold() && !reconnectDone) {
 				    		try {
 				    			String reconnectIP;
 				    			if((reconnectIP = Peer.nextIn.readLine()) != null) {
 				    					Peer.reconnectQueue.add(reconnectIP);
 				    					reconnectDone = true;
 				    			}
 				    		} catch (IOException e) {
 				    			/*
 				    			 * if we're trying to quit, break so thread can
 				    			 * close up; otherwise I/O exceptions are 
 				    			 * expected when we are reconnecting sockets
 				    			 */ 
 				    			if (Peer.getQuit())
 				    				break;
				    			//else System.out.println("I/O Exception in hold while loop");
 				    		}
 				    	}
 				    }
 					//debug code
 				    else if (Peer.debug && housekeeping.equals("test"))
 				    {
 				    		System.out.println("Test housekeeping message received.");
 				    } 
 				}
 			}
 		} catch (IOException e) {
 			if(Peer.debug) {
 				System.out.println(e.getClass().toString() + " caught in NextInput.java");
 				//e.printStackTrace();
 			}
 			if(!Peer.getQuit()) 
 			{
 				run();
 			}
 		}
 	}
 
 }
