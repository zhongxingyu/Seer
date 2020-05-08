 /**
  * @author paul
  */
 
 package distserver;
 
 import distconfig.Sha1Generator;
 import distconfig.DistConfig;
 import distfilelisting.UserManagement;
 import distnodelisting.NodeSearchTable;
 import distnodelisting.GlobalNodeTable;
 
 import java.io.BufferedOutputStream;
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.ObjectOutputStream;
 import java.io.PrintWriter;
 import java.net.InetAddress;
 import java.net.Socket;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  *
  * @author paul
  */
 public class ServEnterNetwork implements Runnable {
     
     private Socket client = null;
     private DistConfig distConf = null;
     
     /**
      * 
      * @param cli The client that has already connected to the network 
      */
     public ServEnterNetwork (Socket cli) {
         this.client = cli;
         if (this.client == null) {
             // TODO: Throw an exception
         }
     }
 
     /**
      * Generates the new ID for the client and tells it where to look first for its next position
      */
     @Override
     public void run() {
         try {
         	System.out.println("In thread for connect to network");
             distConf = DistConfig.get_Instance();
             //Logger.getLogger(
             //        ServEnterNetwork.class.getName()).log(
             //        Level.INFO, null, 
             //        "In thread to connect a new node to the netwrok");
             
             // Get the input stream for the client
             BufferedReader inStream = new BufferedReader (
                     new InputStreamReader(client.getInputStream()));
             System.out.println("Got input stream");
             // Get the output stream for the client
             BufferedOutputStream bos = new BufferedOutputStream (
                     client.getOutputStream());
             // Setup the writer to the output stream
             PrintWriter outStream = new PrintWriter(bos, false);
             System.out.println("Got out stream");
             // Setup the writer for the object stream
             ObjectOutputStream oos = new ObjectOutputStream (bos);
             System.out.println("Got object output stream");
             
             // Send the configuration and user management object to the new client
             // This will make sure all nodes have the same config
             System.out.println("Sending the dist config");
             oos.writeObject(distConf);
             System.out.println("Sending user management");
             oos.writeObject(UserManagement.get_Instance());
             oos.flush();
             
             // Wait to receive an acknowledgment before sending the next item
             System.out.println("Receiving reply");
             inStream.readLine();
             
             // Get the client IP address and create the hash
             NodeSearchTable distConnTable = 
                     NodeSearchTable.get_Instance();
             InetAddress cliAddress = client.getInetAddress();
             int newClientID = 
                     Sha1Generator.generate_Sha1(cliAddress.getHostAddress());
             System.out.println("Sha1 is " + Integer.toString(newClientID));
             
             if (distConf.get_UseGlobalNodeTable()) {
                 GlobalNodeTable dgt = GlobalNodeTable.get_instance();
                 while (dgt.contains_ID(newClientID)) {
                     newClientID = (newClientID + 1) % distConf.get_MaxNodes();
                 }
             }
             else {
                 while (distConnTable.contains_ID(newClientID)) {
                     newClientID = (newClientID + 1) % distConf.get_MaxNodes();
                 }
             }
             
             // Send the client their new ID
             outStream.println(Integer.toString(newClientID));
             outStream.flush();
             
             // Wait to receive an acknowledgment and request for next item
             inStream.readLine();
             
             // If the configuration states to use the entire table,
             // Then send the entire table
             if (distConf.get_UseGlobalNodeTable()) {
                 GlobalNodeTable dgt = GlobalNodeTable.get_instance();
                 oos.writeObject(dgt);
                 oos.flush();
             }
             // Else if, the current server is the only node on the network
             else if (distConnTable.size() == 0) {
                 outStream.println(distConnTable.get_ownID());
                 outStream.println(distConnTable.get_ownIPAddress());
                 outStream.flush();
             }
             // If the configuration does not specify using the full table
             // Send the highest ID and IP less then the ID of the new node
             else {
                 // Check to see which two ID's in the connection table
                 // the new client ID is between
                 
                 // First, use this server's ID as the starting point
                 String ipAddress = distConnTable.get_ownIPAddress();
                 int id = Integer.parseInt(distConnTable.get_ownID());
                 boolean found = false;
                 
                 // Now loop through all of the ID's and check if the new
                 // ID lies between them
                 for (int index = 0; index < distConnTable.size(); index++) {
                     // Get the next ID
                     int nextID = Integer.parseInt(distConnTable.get_IDAt(index));
                     // Test if the new client is greater than or equal to the
                     // previous ID and less than the nextID
                     if (newClientID >= id && newClientID < nextID) {
                         found = true;
                     }
                     // Test if the new client is greater than or equal to the
                     // previous ID and greater than the next ID
                     else if (newClientID >= id && newClientID > nextID) {
                         found = true;
                     }
                     // Test if the new client is less than or equal to the
                     // previous ID and less than the next ID
                     else if (newClientID <= id && newClientID < nextID) {
                         found = true;
                     }
                     // If it is not between the two, set the id to the next
                     // id and the ip address to the next ip address
                     if (!found) {
                         id = nextID;
                         ipAddress = distConnTable.get_IPAt(index);
                     }
                 }
                                 
                 // Send the ID and IP Address to the client
                 outStream.println(Integer.toString(id));
                 outStream.println(ipAddress);
                 outStream.flush();
             }
             
             // Wait for acknowledgment of receipt
             inStream.readLine();
             
             // Finished setting up the new client, close all connections
             inStream.close();
             oos.close();
             outStream.close();
             bos.close();
             client.close();
             
         } catch (IOException ex) {
             try {
                 // Something went wrong with IO to the client
                 client.close();
             } catch (IOException ex1) {
                 Logger.getLogger(ServEnterNetwork.class.getName()).log(Level.SEVERE, null, ex1);
             }
             Logger.getLogger(ServEnterNetwork.class.getName()).log(Level.SEVERE, null, ex);
         }
     }
 }
