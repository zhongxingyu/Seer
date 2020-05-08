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
 import distconfig.DistConfig;
 import distfilelisting.UserManagement;
 import distnodelisting.NodeSearchTable;
 
 /**
  * When a new node is added, it sends to this part of the server
  * so that the server can update it's search table
  * @author paul
  *
  */
 public class ServNewNode implements Runnable {
 
 	private Socket client = null;
 	private NodeSearchTable nst = null;
 	
 	/**
 	 * 
 	 * @param cli : The socket to which the client is attached
 	 */
 	public ServNewNode (Socket cli) {
 		this.client = cli;
 	}
 	
 	@Override
 	public void run() {
 		try {
 			System.out.println("Inside thread for ServNewNode");
 	        // Get the input stream for the client
 	        BufferedReader inStream = new BufferedReader (
 	                new InputStreamReader(client.getInputStream()));
 	        System.out.println("Got input stream, in run in ServNewNode");
 	        // Get the output stream for the client
 	        BufferedOutputStream bos = new BufferedOutputStream (
 	                client.getOutputStream());
 	        // Setup the writer to the client
 	        PrintWriter outStream = new PrintWriter(bos, false);
 	        System.out.println("Got output stream, in run in ServNewNode");
 	        
 	        // Send confirmation of connection
 	        System.out.println("Sending the ack, in run in ServNewNode");
 	        outStream.println(ConnectionCodes.NEWNODE);
 	        outStream.flush();
 	        
 	        // Receive new ID and IP
 	        System.out.println("Receiving ID and IP and username, in run in ServNewNode");
 	        nst = NodeSearchTable.get_Instance();
 	        int newID = Integer.parseInt(inStream.readLine());
 	        String newIP = inStream.readLine();
 	        
 	        // Receive new user name
 	        String username = inStream.readLine();
 	        
 	        // Send received confirmation
 	        System.out.println("Sending ack, in run in ServNewNode");
 	        outStream.println(ConnectionCodes.NEWNODE);
 	        outStream.flush();
 	        
 	        // Close the connection
 	        bos.close();
 	        outStream.close();
 	        inStream.close();
 	        client.close();
 	        
 	        // Increment the amount of nodes now on the network
         	DistConfig.get_Instance().increment_CurrNodes();
 	        
         	// Update the user management
 	        UserManagement useManage = UserManagement.get_Instance();
 	        useManage.add_User(username, "1");
 	        
 	        int myID = Integer.parseInt(nst.get_ownID());
 	        // If the newID is not the same as this servers ID
 	        //		aka, it hasn't made it all the way around the network yet
 	        if (!(newID == myID)) {
 	        	// If the new ID is between my ID and the predecessor's ID,
 				// Set the new ID to be the predecessor
 				if (NodeSearchTable.is_between(newID, Integer.parseInt(nst.get_predecessorID()), myID) ||
 						myID == Integer.parseInt(nst.get_predecessorID())) {
 		    		nst.set_predicessor(Integer.toString(newID), newIP);
 		    	}
 		    	
 				// Loop through each element of the search table and check to see
 				// if the new ID fits in any of them
 		    	for (int index = 0; index < nst.size(); index++) {
 		    		int potID = NodeSearchTable.get_SlotPotentialID(index);
 		    		int currSearchID = Integer.parseInt(nst.get_IDAt(index));
 		    		
 		    		// If the new ID is between the potential ID for this slot
 		    		// and the current ID set to this slot, then set it.
		    		if (NodeSearchTable.is_between(newID, potID, currSearchID) ||
		    				newID == potID || currSearchID == myID) {
 		    			nst.set(index, Integer.toString(newID), newIP);
 		    		}
 		    	}
 		    	
 	        	// Alter the search table and send the newIP and newID along to the next server
 	        	this.pushNewIDAndIP(newID, newIP, username, myID);
 	        	// Send this node's information to the new node
 	        	this.sendOwnInfo(newIP);
 	        }
 		}
 		
 		catch (IOException ex) {
 			System.out.println("Inside run inside ServNewNode");
             Logger.getLogger(ServCheckPosition.class.getName()).log(Level.SEVERE, null, ex);
         }
 	}
 	
 	/**
 	 * This will update the local search table and pass the new ID and IP to the next successor
 	 * @param newID : The integer representation of the new node's ID
 	 * @param newIP : The string representation of the new node's IP
 	 * @param myID : The integer representation of this node's ID
 	 */
 	private void pushNewIDAndIP (int newID, String newIP, String newUser, int myID) {
 		try {
 	    	System.out.printf("Connecting to %s, in push in ServNewNode\n", nst.get_IPAt(0));
 	    	// Setup the socket to the next node, and the write and read buffers
 	    	Socket sock = new Socket(nst.get_IPAt(0), DistConfig.get_Instance().get_servPortNumber());
 	        sock.setSoTimeout(5000);
 	        // write buffer
 	        BufferedOutputStream bos = new BufferedOutputStream (sock.getOutputStream());
 			PrintWriter outStream = new PrintWriter(bos, false);
 			// read buffer
 			BufferedReader inStream = new BufferedReader (
 	                new InputStreamReader(sock.getInputStream()));
 					
 			// Sending connection code
 			outStream.println(ConnectionCodes.NEWNODE);
 			outStream.flush();
 			
 			// Receive Confirmation
 			inStream.readLine();
 			
 			// Send newID and newIP
 			System.out.println("Sending ID and IP and username, in push in ServNewNode");
 			outStream.println(Integer.toString(newID));
 			outStream.println(newIP);
 			outStream.println(newUser);
 			outStream.flush();
 			
 			// receive confirmation
 			inStream.readLine();
 			
 			// Close the connection
 			inStream.close();
 			bos.close();
 			outStream.close();
 			sock.close();
 		}
 		
 		catch (IOException ex) {
 			System.out.println("Inside push inside ServNewNode");
             Logger.getLogger(ServCheckPosition.class.getName()).log(Level.SEVERE, null, ex);
         }
 	}
 	
 	/**
 	 * 
 	 * @param newIP : The string representation of the IP for the new node
 	 */
 	private void sendOwnInfo(String newIP) {
 		try {
 			System.out.printf("Connecting to %s, in send in ServNewNode\n", newIP);
 			// Setup the socket for the new node and input and output buffers
 			Socket sock = new Socket(newIP, DistConfig.get_Instance().get_servPortNumber());
 	        sock.setSoTimeout(5000);
 	        // output buffer
 	        BufferedOutputStream bos = new BufferedOutputStream (sock.getOutputStream());
 			PrintWriter outStream = new PrintWriter(bos, false);
 			// input buffer
 			BufferedReader inStream = new BufferedReader (
 	                new InputStreamReader(sock.getInputStream()));
 					
 			// Sending connection code
 			outStream.println(ConnectionCodes.SETUPSEARCHTABLE);
 			outStream.flush();
 			
 			// Receive Confirmation
 			inStream.readLine();
 			
 			// Send newID and newIP
 			System.out.println("Sending ID and IP, in send in ServNewNode");
 			outStream.println(nst.get_ownID());
 			outStream.println(nst.get_ownIPAddress());
 			outStream.flush();
 			
 			// receive confirmation
 			System.out.println("Receiving ack, in send in ServNewNode");
 			inStream.readLine();
 			
 			// Close connection
 			inStream.close();
 			bos.close();
 			outStream.close();
 			sock.close();
 		}
 		
 		catch (IOException ex) {
 			System.out.println("Inside send ServNewNode");
             Logger.getLogger(ServCheckPosition.class.getName()).log(Level.SEVERE, null, ex);
         }
 	}
 }
