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
  * This is called when a new node becomes this nodes predecessor
  * @author paul
  *
  */
 public class ServNewPredecessor implements Runnable {
 	
 	private Socket client = null;
 	private DistConfig distConfig = null;
 	
 	/**
 	 * 
 	 * @param cli : The socket the client is attached to
 	 */
 	public ServNewPredecessor (Socket cli) {
 		this.client = cli;
 	}
 	
 	@Override
 	public void run() {
 		try {
 			System.out.println("In thread for new predecessor");
             this.distConfig = DistConfig.get_Instance();
             
             // Get the input stream for the client
             BufferedReader inStream = new BufferedReader (
                     new InputStreamReader(client.getInputStream()));
             // Get the output stream for the client
             BufferedOutputStream bos = new BufferedOutputStream (
                     client.getOutputStream());
             // Setup the writer to the client
             PrintWriter outStream = new PrintWriter(bos, false);
             // Setup the object writer to the client
             ObjectOutputStream oos = new ObjectOutputStream (bos);
             
             // Send acknowledgment everything is set
             outStream.println(ConnectionCodes.NEWPREDECESSOR);
             outStream.flush();
             
             // Get the ID of the new Predecessor
             int newPredID = Integer.parseInt(inStream.readLine());
             
             // Get the list of files that will be transfered to the new node
             Vector<FileObject> filesToTransfer = LocalPathList.get_Instance().get_filesBetween(
             		newPredID, 
             		Integer.parseInt(NodeSearchTable.get_Instance().get_IDAt(0)));
             
             // Send the list of files to be transfered
             oos.writeObject(filesToTransfer);
             oos.flush();
             
             // Wait for acknowledgment
             inStream.readLine();
             
             // Send each file individually
             // Loop through each file in the transfer set an send them to the new client
             for (FileObject f : filesToTransfer) {
             	// Get the name of the file to transfer
             	String fileName = f.getName();
             	
             	// Open the file and setup the input stream
             	File toTransfer = new File (distConfig.get_rootPath() + fileName);
             	FileInputStream fis = new FileInputStream(toTransfer);
             	byte[] buffer = new byte[distConfig.getBufferSize()];
             	
             	Integer bytesRead = 0;
             	// Read the bytes from the file, sending them to the client
             	while ((bytesRead = fis.read(buffer)) > 0) {
             		oos.writeObject(bytesRead);
             		oos.writeObject(Arrays.copyOf(buffer, buffer.length));
             		oos.flush();
             	}
             	
             	// Get confirmation that it made it
             	inStream.readLine();
             }
             
             // Remove the files from the directory and list
             LocalPathList lpl = LocalPathList.get_Instance();
             // Loop through the list of files to send, removing them from the
             // local disk and local list of files
             for (FileObject f : filesToTransfer) {
             	String fileName = f.getName();
             	File toDelete = new File (distConfig.get_rootPath() + fileName);
             	toDelete.delete();
             	lpl.remove(f);
             }
             
             // Add the new predecessor to search list
             NodeSearchTable nst = NodeSearchTable.get_Instance();
             nst.set_predicessor(Integer.toString(newPredID), client.getInetAddress().toString());
             
             // Send confirmation
             outStream.println(ConnectionCodes.NEWPREDECESSOR);
             outStream.flush();
             
             oos.close();
             outStream.close();
             inStream.close();
             client.close();
 		}
 		
 		catch (IOException ex) {
			System.out.println("Inside ServNewPredecessor");
             Logger.getLogger(ServCheckPosition.class.getName()).log(Level.SEVERE, null, ex);
         }
 		
 	}
 	
 }
