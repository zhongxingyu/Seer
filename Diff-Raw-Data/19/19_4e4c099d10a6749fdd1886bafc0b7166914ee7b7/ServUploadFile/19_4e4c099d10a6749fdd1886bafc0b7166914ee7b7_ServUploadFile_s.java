 /**
  * @author paul
  * 
  * ServUploadFile
  * 		Used by the server to upload a new file to the distributed file system.
 */
 
 package distserver;
 
 import java.io.BufferedOutputStream;
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.ObjectInputStream;
 import java.io.PrintWriter;
 import java.net.Socket;
 import java.util.Vector;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import distconfig.ConnectionCodes;
 import distconfig.DistConfig;
 import distconfig.Sha1Generator;
 import distfilelisting.FileObject;
 import distfilelisting.LocalPathList;
 import distfilelisting.UserManagement;
 import distnodelisting.NodeSearchTable;
 
 
 /**
  * 
  * @author paul
  *
  */
 public class ServUploadFile implements Runnable {
 
 	private Socket client = null;
 	private DistConfig distConfig = null;
 	
 	/**
 	 * Constructor, sets the client
 	 * @param cli : The socket to which the client is connected
 	 */
 	public ServUploadFile (Socket cli) {
 		this.client = cli;
 	}
 	
 	/**
 	 * Runs the upload server.
 	 * This will first check to see if this is the correct server. If not, it will send the next location that should be checked.
 	 * If this is the correct location, it will then check to see if the sender has the proper permissions
 	 * If the sender has the proper permissions, it will begin the upload.
 	 */
 	@Override
 	public void run() {
 		try {
 			System.out.println("In thread for uploading a file");
             this.distConfig = DistConfig.get_Instance();
             
             // Get the input stream for the client
             BufferedReader inStream = new BufferedReader (
                     new InputStreamReader(client.getInputStream()));
             // Create object input stream
             ObjectInputStream ois = new ObjectInputStream(client.getInputStream());
             // Get the output stream for the client
             BufferedOutputStream bos = new BufferedOutputStream (
                     client.getOutputStream());
             // Setup the writer to the client
             PrintWriter outStream = new PrintWriter(bos, false);
             
             // Send acknowledgment everything is set
             outStream.println(ConnectionCodes.UPLOADFILE);
             outStream.flush();
       
             // Get the file name
             String filename = inStream.readLine();
             
             // Get the hash for the file
             int fileHash = Sha1Generator.generate_Sha1(filename);
             
             // Get the search table
             NodeSearchTable nst = NodeSearchTable.get_Instance();
             
             
             // Check if this is the correct position.
             // Is the file hash between the predecessor ID and this ID
             // or equal to this ID
             if (NodeSearchTable.is_between(
             		fileHash, 
             		Integer.parseInt(nst.get_predecessorID()), 
             		Integer.parseInt(nst.get_ownID())) ||
             		fileHash == Integer.parseInt(nst.get_ownID())) {
             	
             	// Send that this is the correct server
             	outStream.println(ConnectionCodes.CORRECTPOSITION);
             	outStream.flush();
             	
             	// Get the user that is sending the file and the FileObject being sent
             	String username = (String)inStream.readLine();
             	FileObject filetoupload = (FileObject)ois.readObject();
             	
             	// Get the local file object if it exists
             	LocalPathList lpl = LocalPathList.get_Instance();
             	FileObject localFile = lpl.get_file(filename);
             	boolean isPermitted = false;
             	
             	// Check to see if the file exists on the current server
             	// If it does not, then permit the upload
             	if (localFile == null) {
             		isPermitted = true;
             	}
             	// If the file does exist
             	// Check to see if the user has permissions
             	else {
             		// Get the groups to which the user belongs
             		Vector<String> groups = UserManagement.get_Instance().get_GroupsForUser(username);
                 	
             		// If the user owns the file, permit the upload
                 	if (username == localFile.getOwner()) {
                 		isPermitted = true;
                 	}
                 	// If the user does not own the file, check if the user is in the proper group
                 	else if (groups.contains(localFile.getGroup())) {
                 		// If the user is in the group, check if they have permission to write
                 		if (localFile.getGroupPermision() >= 2 &&
                 				localFile.getGroupPermision() != 4 &&
                 				localFile.getGroupPermision() != 5) {
                 			isPermitted = true;
                 		}
                 	}
                 	// If the user is not in the group, check to see if global has permission to write
                 	else if (localFile.getGroupPermision() >= 2 && 
                 			localFile.getGroupPermision() != 4 && 
                 			localFile.getGroupPermision() != 5) {
                 		isPermitted = true;
                 	}
             	}
             	
             	// If the upload is not permitted, send the not authorized signal
             	if (!isPermitted) {
             		outStream.println(ConnectionCodes.NOTAUTHORIZED);
             		outStream.flush();
             	}
             	// If the upload is permitted, send the authorized signal and begin upload
             	else {
             		outStream.println(ConnectionCodes.AUTHORIZED);
             		outStream.flush();
             		
             		// Check if the file currently exists on the system, and delete it if it does
             		// It will be overwritten
             		File newUpload = new File(DistConfig.get_Instance().get_rootPath() + filename);
             		if (newUpload.exists()) {
             			newUpload.delete();
             		}
             		
             		// Setup the parameters needed for the upload
             		int bytesRead = 0;
             		byte [] buffer = new byte[distConfig.getBufferSize()];
             		FileOutputStream fos = new FileOutputStream (distConfig.get_rootPath() + filetoupload.getName());
             		
             		// Upload the file
             		do {
             			bytesRead = (Integer)ois.readObject();
             			buffer = (byte[])ois.readObject();
             			fos.write(buffer, 0, bytesRead);
             		} while (bytesRead == distConfig.getBufferSize());
             		
             		fos.close();
             		
             		// Send the received message and add the newly uploaded file to local file list
             		outStream.write(ConnectionCodes.RECEIVEDFILE);
             		outStream.flush();
             		lpl.add(filetoupload);
             	}
             }
             
             // else locate next server to check
             else {
             	// Get the next server to checks ID and IP address
             	int nextCheckID = Integer.parseInt(nst.get_IDAt(0));
             	String nextCheckIP = nst.get_IPAt(0);
             	// Loop through all of the servers in the search table
             	for (int index = 0; index < nst.size()-1; index++) {
             		// Get the next two IDs
             		int nextID = Integer.parseInt(nst.get_IDAt(index));
             		int secondID = Integer.parseInt(nst.get_IDAt(index + 1));
             		
             		// Check if the file hash lies between the two elements of the search table
             		// or if it is equal to one of them
             		if (NodeSearchTable.is_between(fileHash, nextID, secondID) || 
             				fileHash == nextID) {
             			nextCheckID = nextID;
             			nextCheckIP = nst.get_IPAt(index);
             			continue;
             		}
             		
             		// If it wasn't between, set the next check ID to the second ID
         			nextCheckID = secondID;
         			nextCheckIP = nst.get_IPAt(index + 1);
             	}
             	
             	// Send the wrong position signal
             	// Send the next location to look at
             	outStream.println(ConnectionCodes.WRONGPOSITION);
             	outStream.println(Integer.toString(nextCheckID));
             	outStream.println(nextCheckIP);
             	outStream.flush();
             	
             	// Read in confirmation that the client got the information
             	inStream.readLine();
             }
             
             // Close all streams
             ois.close();
             outStream.close();
             inStream.close();
             client.close();
 		}
 		
 		// Catch any potential errors
 		catch (IOException ex) {
 			Logger.getLogger(ServCheckPosition.class.getName()).log(Level.SEVERE, null, ex);
 		}
 		catch (ClassNotFoundException ex) {
             Logger.getLogger(ServCheckPosition.class.getName()).log(Level.SEVERE, null, ex);
         }
 	}
 }
