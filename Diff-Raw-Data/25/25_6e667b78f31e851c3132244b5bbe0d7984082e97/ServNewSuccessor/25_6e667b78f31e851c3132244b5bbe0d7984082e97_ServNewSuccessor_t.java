 /**
  * @author paul
 */
 
 package distserver;
 
 import java.io.BufferedOutputStream;
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.ObjectOutputStream;
 import java.io.PrintWriter;
 import java.net.Socket;
 import java.util.Arrays;
 import java.util.Vector;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import distconfig.ConnectionCodes;
 import distconfig.DistConfig;
 import distfilelisting.FileObject;
 import distfilelisting.LocalPathList;
 import distnodelisting.NodeSearchTable;
 
 /**
  * When the server now has a new successor, this part of the server is called
  * @author paul
  *
  */
 public class ServNewSuccessor implements Runnable {
 
 	
 	private Socket client = null;
 	private DistConfig distConfig = null;
 	private String outputformat = "%s, in ServNewSuccessor\n";
 	
 	/**
 	 * 
 	 * @param cli : The socket the client is connected to
 	 */
 	public ServNewSuccessor (Socket cli) {
 		this.client = cli;
 	}
 	
 	@Override
 	public void run() {
 		try {
 			System.out.println("In thread for new successor");
             this.distConfig = DistConfig.get_Instance();
             
             // Get the input stream for the client
             BufferedReader inStream = new BufferedReader (
                     new InputStreamReader(client.getInputStream()));
             System.out.printf(outputformat, "Got input stream");
             // Get the output stream for the client
             BufferedOutputStream bos = new BufferedOutputStream (
                     client.getOutputStream());
             // Setup the writer to the client
             PrintWriter outStream = new PrintWriter(bos, false);
             System.out.printf(outputformat, "Got output stream");
             // Setup the object writer to the client
             ObjectOutputStream oos = new ObjectOutputStream (bos);
             System.out.printf(outputformat, "Got object output stream");
             
             // Send acknowledgment everything is set
             outStream.println(ConnectionCodes.NEWSUCCESSOR);
             outStream.flush();
             
             // Get the ID of the new Successor
             int newSuccID = Integer.parseInt(inStream.readLine());
             
             // Get what files will need to be transfered to the new client
             Vector<FileObject> filesToTransfer = LocalPathList.get_Instance().get_filesBetween(
             		Integer.parseInt(NodeSearchTable.get_Instance().get_predecessorID()),
             		newSuccID);
             
             // Write the vector of files that will need to be transfered
             oos.writeObject(filesToTransfer);
             oos.flush();
             
             // Wait for acknowledgment
             inStream.readLine();
             
             // Send each file individually
             // Loop through each file in the vector and send it
             for (int index = 0; index < filesToTransfer.size(); index++) {
             	// Get the name of the file to send
             	String fileName = filesToTransfer.get(index).getName();
             	
             	// Open the file and input stream to that file
             	File toTransfer = new File (distConfig.get_rootPath() + fileName);
             	FileInputStream fis = new FileInputStream(toTransfer);
             	byte[] buffer = new byte[distConfig.getBufferSize()];
             	
             	Integer bytesRead = 0;
             	// Loop through the bytes of the file, reading them into the buffer
             	// and sending them to the new client
             	while ((bytesRead = fis.read(buffer)) > 0) {
             		oos.writeObject(bytesRead);
            		oos.flush();
             		oos.writeObject(Arrays.copyOf(buffer, buffer.length));
             		oos.flush();
             	}
             	
             	// Get confirmation for each file sent
             	inStream.readLine();
             }
             
             // Remove the files from the directory and list
             LocalPathList lpl = LocalPathList.get_Instance();
             // Loop through all the files that were transfered and delete them from
             // the directory and the list
             for (int index = 0; index < filesToTransfer.size(); index++) {
             	String fileName = filesToTransfer.get(index).getName();
             	File toDelete = new File (distConfig.get_rootPath() + fileName);
             	toDelete.delete();
             	lpl.remove(filesToTransfer.get(index));
             }
             
             // Add the new predecessor to search list
             NodeSearchTable nst = NodeSearchTable.get_Instance();
             nst.set(0, Integer.toString(newSuccID), client.getInetAddress().getHostAddress());
             
             // Send confirmation
             outStream.println(ConnectionCodes.NEWSUCCESSOR);
             outStream.flush();
             
             oos.close();
             outStream.close();
             inStream.close();
             client.close();
 		}
 		
 		catch (IOException ex) {
 			System.out.println("Inside ServNewSuccessor");
             Logger.getLogger(ServCheckPosition.class.getName()).log(Level.SEVERE, null, ex);
         }
 	}
 	
 }
