 /**
  * @author paul
 */
 
 package distserver;
 
 import java.io.BufferedOutputStream;
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.PrintWriter;
 import java.net.Socket;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import distconfig.ConnectionCodes;
 import distnodelisting.NodeSearchTable;
 
 /**
  * Used to setup the search table of a new node
  * 
  * @author paul
  *
  */
 class ServSetupSearchTable implements Runnable {
 	
 	private Socket client = null;
 
 	/**
 	 * Constructor, sets the client
 	 * @param cli : The socket the client is connected to
 	 */
 	public ServSetupSearchTable (Socket cli) {
 		this.client = cli;
 	}
 	
 	/**
 	 * Used to run the server to get the ID and IP of the client
 	 * Uses these values to update the search table 
 	 */
 	@Override
 	public void run() {
 		
 		try {
 			System.out.println("Inside thread for ServSetupSearchTable");
 	        // Get the input stream for the client
 	        BufferedReader inStream = new BufferedReader (
 	                new InputStreamReader(client.getInputStream()));
 	        // Get the output stream for the client
 	        BufferedOutputStream bos = new BufferedOutputStream (
 	                client.getOutputStream());
 	        // Setup the writer to the client
 	        PrintWriter outStream = new PrintWriter(bos, false);
 	        
 	        // Send that the server is ready to receive
 	        System.out.println("Sending ack, in ServSetupSearchTable");
 	        outStream.println(ConnectionCodes.SETUPSEARCHTABLE);
 	        outStream.flush();
 	        
 	        System.out.println("Getting ID and IP, in ServSetupSearchTable");
 	        // Receive the ID and IP of the client
 	        int newID = Integer.parseInt(inStream.readLine());
 	        String newIP = inStream.readLine();
 	        
 	        // Get the node search table and the local ID
 	        NodeSearchTable nst = NodeSearchTable.get_Instance();
 	        int myID = Integer.parseInt(nst.get_ownID());
 	        int predecessorID = Integer.parseInt(nst.get_predecessorID());
 	        
 	        // Check if the new ID is between the current ID and its predecessor
 	        // First check if the new ID is less than the local ID and greater than the predecessor ID
 	        if (newID < myID && newID > predecessorID) {
 	    		nst.set_predicessor(Integer.toString(newID), newIP);
 	    	}
 	        // Else if the new ID is less than the local ID
 	        // and the new ID is less than the predecessor ID
 	        // and the predecessor is greater than the local ID
 	        else if (newID < myID && newID > predecessorID && myID < predecessorID) {
 	        	nst.set_predicessor(Integer.toString(newID), newIP);
 	        }
 	        // Else if the newID is greater than the local ID
 	        // and the new ID is greater than the predecessor ID
 	        else if (newID > myID && newID > predecessorID) {
 	        	nst.set_predicessor(Integer.toString(newID), newIP);
 	        }
 	    	
 	        // Loop through the search table and update it where needed
 	    	for (int index = 0; index < nst.size(); index++) {
 	    		// Get the potential ID for the slot in the table
 	    		int potID = NodeSearchTable.get_SlotPotentialID(index);
 	    		// Get the current ID held at that location
 	    		int currSearchID = Integer.parseInt(nst.get_IDAt(index));
 	    		
 	    		// if the new ID is between the potential ID and the current ID
 	    		// in the slot update it
	    		if (NodeSearchTable.is_between(newID, potID, currSearchID) || 
	    				newID == potID || currSearchID == myID) {
 	    			nst.set(index, Integer.toString(newID), newIP);
 	    		}
 	    	}
 	    	
 	    	// Send that the table has been updated
 	    	System.out.println("Sending ack, in ServSetupSearchTable");
 	    	outStream.println(ConnectionCodes.SETUPSEARCHTABLE);
 	    	outStream.flush();
 	    	
 	    	// Close all streams and the client
 	    	outStream.close();
 	    	bos.close();
 	    	inStream.close();
 	    	client.close();
 		}
 		
 		catch (IOException ex) {
             Logger.getLogger(ServCheckPosition.class.getName()).log(Level.SEVERE, null, ex);
         }
 		
 	}
 	
 }
