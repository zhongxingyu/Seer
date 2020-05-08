 /**
  * @author paul
  */
 package distserver;
 
 import distconfig.ConnectionCodes;
 import distconfig.DistConfig;
 import distnodelisting.NodeSearchTable;
 
 import java.io.BufferedOutputStream;
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.ObjectOutputStream;
 import java.io.PrintWriter;
 import java.net.Socket;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  * Checks the clients position in the network
  * @author paul
  */
 public class ServCheckPosition implements Runnable {
     
     private Socket client = null;
     private DistConfig distConfig = null;
     
     /**
      * 
      * @param cli : The socket to which the client is connected
      */
     public ServCheckPosition (Socket cli) {
         this.client = cli;
     }
 
     /**
      * Checks the position of the client
      * If it is in the correct location, the server sends the new client
      * its new predecessor ID and IP and new successor ID and IP
      */
     @Override
     public void run() {
         
         try {
             System.out.println("In thread for check position");
             this.distConfig = DistConfig.get_Instance();
             
             // Get the input stream for the client
             BufferedReader inStream = new BufferedReader (
                     new InputStreamReader(client.getInputStream()));
             System.out.println("Got the input stream");
             // Get the output stream for the client
             BufferedOutputStream bos = new BufferedOutputStream (
                     client.getOutputStream());
             // Setup the writer to the client
             PrintWriter outStream = new PrintWriter(bos, false);
             System.out.println("Got the out stream");
             // Setup the object writer to the client
             ObjectOutputStream oos = new ObjectOutputStream (bos);
             System.out.println("Got the object output stream");
             
             // Send an acknowledgment that the server is connected
             // for checking the position
             System.out.println("Sending the connection code");
             outStream.println(ConnectionCodes.CHECKPOSITION);
             outStream.flush();
             
             // Receive the new node's ID
             System.out.println("Waiting for node ID");
             String newNodeID = (String)inStream.readLine();
             int newID = Integer.parseInt(newNodeID);
             
             NodeSearchTable dct = NodeSearchTable.get_Instance();
             
             // Get own ID
             int id = Integer.parseInt(dct.get_ownID());
             
             // If own ID = the new nodes ID, create a new ID for it
             if (newID == id) {
                 newID = (newID + 1) % distConfig.get_MaxNodes();
                 outStream.println(ConnectionCodes.NEWID);
                 outStream.println(Integer.toString(newID));
                 outStream.flush();
                 // Now continue with the check
             }
             
             // Check if the new node's ID is between the current ID and the next
             int nextID = Integer.parseInt(dct.get_IDAt(0));
             // If the new ID is between this ID and the next
             if ((id < newID && newID < nextID) || 
                     (nextID < id && id < newID) ||
                    (newID < nextID && nextID < id)) { 
                 // Send CORRECTPOSITION message
                 outStream.println(ConnectionCodes.CORRECTPOSITION);
                 outStream.flush();
                 // Send the string array of this id and ip
                 String[] ownInfo = { Integer.toString(id), 
                     dct.get_ownIPAddress() };
                 oos.writeObject(ownInfo);
                 // Send the string array of the next id and ip
                 String[] nextInfo = { Integer.toString(nextID), 
                     dct.get_IPAt(0) };
                 oos.writeObject(nextInfo);
                 // flush the output stream
                 oos.flush();
             }
             // Else, discover what two nodes it is between
             else {
                 // Check to see which two ID's in the connection table
                 // the new client ID is between
                 
                 // First, use this server's ID as the starting point
                 String ipAddress = dct.get_ownIPAddress();
                 id = Integer.parseInt(dct.get_ownID());
                 boolean found = false;
                 
                 // Now loop through all of the ID's and check if the new
                 // ID lies between them
                 for (int index = 0; index < dct.size(); index++) {
                     // Get the next ID
                     nextID = Integer.parseInt(dct.get_IDAt(index));
                     // Test if the new client is greater than or equal to the
                     // previous ID and less than the nextID
                     if (newID >= id && newID < nextID) {
                         found = true;
                     }
                     // Test if the new client is greater than or equal to the
                     // previous ID and greater than the next ID
                     else if (id > nextID && newID >= id && newID > nextID) {
                         found = true;
                     }
                     // Test if the new client is less than or equal to the
                     // previous ID and less than the next ID
                     else if (id > nextID && newID <= id && newID < nextID) {
                         found = true;
                     }
                     // If it is not between the two, set the id to the next
                     // id and the ip address to the next ip address
                     if (!found) {
                         id = nextID;
                         ipAddress = dct.get_IPAt(index);
                     }
                 }
                 
                 // Once found, send the wrong position message
                 outStream.println(ConnectionCodes.WRONGPOSITION);
                 // Send the new ID and IP of the next node to check
                 outStream.println(Integer.toString(id));
                 outStream.println(ipAddress);
                 outStream.flush();
             }
             
             oos.close();
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
