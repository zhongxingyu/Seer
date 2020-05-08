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
 import distconfig.Sha1Generator;
 import distfilelisting.FileObject;
 import distfilelisting.LocalPathList;
 import distfilelisting.UserManagement;
 import distnodelisting.NodeSearchTable;
 
 /**
  * When a client is trying to retrieve a file
  * @author paul
  *
  */
 class ServGetFile implements Runnable {
 
 	private Socket client = null;
 	private DistConfig distConfig;
 	private int is_backup = 0;
 	
 	public ServGetFile (Socket cli) {
 		this.client = cli;
 	}
 	
 	@Override
 	public void run() {
 		try {
 			System.out.println("In thread for get file");
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
             outStream.println(ConnectionCodes.GETFILE);
             outStream.flush();
             
             // Get file name and if this is the backup server
             String filename = inStream.readLine();
             is_backup = Integer.parseInt(inStream.readLine());
             
             // Get the hash for the file
             int fileHash = Sha1Generator.generate_Sha1(filename);
             
             // Get the search table
             NodeSearchTable nst = NodeSearchTable.get_Instance();
 
             // Check if this is the correct server
             boolean is_correct = false;
             // If this is not the backup server, and the file lies between the previous node
             // and this one, or equal to this nodes ID, then this is correct
             if (is_backup == 0 &&
             		(NodeSearchTable.is_between(fileHash, Integer.parseInt(nst.get_predecessorIPAddress()), Integer.parseInt(nst.get_ownID())) ||
             				fileHash == Integer.parseInt(nst.get_ownID()))) {
             	is_correct = true;
             }
             // If this is the backup server, and the file lies between this node and its successor
             // or is equal to the successors ID, then this is the correct node
             else if (is_backup != 0 &&
             		(NodeSearchTable.is_between(fileHash, Integer.parseInt(nst.get_ownID()), Integer.parseInt(nst.get_IDAt(0))) ||
             				fileHash == Integer.parseInt(nst.get_IDAt(0)))) {
             	is_correct = true;
             }
             
             // If this is the correct node
             if (is_correct) {
             	// Send the signal that this is correct
             	outStream.println(ConnectionCodes.CORRECTPOSITION);
             	outStream.flush();
             	
             	// Get the full path name for the file
                 String fullPathName = distConfig.get_rootPath() + filename;
                 
                 // Check if the file exists and if the user has permissions
                 FileObject fileobj = LocalPathList.get_Instance().get_file(filename);
                 boolean isAllowedAccess = false;
                 
                 // Test if the file doesn't exist
                 if (fileobj == null) {
                 	// Send the signal
                 	outStream.println(ConnectionCodes.FILEDOESNTEXIST);
                 	// If this is not the backup, send the IP of the backup server
                 	if (is_backup == 0)
                 		outStream.println(nst.get_predecessorIPAddress());
                 	// Otherwise, re-send the file doesn't exist signal
                 	else
                 		outStream.println(ConnectionCodes.FILEDOESNTEXIST);
                 	outStream.flush();
                 }
                 // Other wise continue with checking permissions
                 else {
                 	File toTransfer = new File (fullPathName);
                 	
                 	// Check to see if the file exists on the disk
                 	if (!toTransfer.exists()) {
                 		// If not, send the signal
                 		outStream.println(ConnectionCodes.FILEDOESNTEXIST);
                 		// if this is not the backup, send the IP of the backup server
                 		if (is_backup == 0)
                     		outStream.println(nst.get_predecessorIPAddress());
                 		// otherwise, send the file doesn't exist signal again
                     	else
                     		outStream.println(ConnectionCodes.FILEDOESNTEXIST);
                 		outStream.flush();
                 	}
                 	// If the file does exist on the disk
                 	else {
 	                	// Send the file exists signal
 	                	outStream.println(ConnectionCodes.FILEEXISTS);
 	                	outStream.flush();
 	                	
 	                	// Read in the username of the person trying to access the file
 	                	String username = inStream.readLine();
 	                	// Get the groups that user belongs to
 	                	Vector<String> groups = UserManagement.get_Instance().get_GroupsForUser(username);
 	                	
 	                	// Check the permissions
 	                	// Is the user the owner
 	                	if (username == fileobj.getOwner()) {
 	                		isAllowedAccess = true;
 	                	}
 	                	// Does one of the user's groups have permission to access the file
 	                	else if (groups.contains(fileobj.getGroup())) {
 	                		if (fileobj.getGroupPermision() >= 4) {
 	                			isAllowedAccess = true;
 	                		}
 	                	}
 	                	// Does the global community have permissions
 	                	else if (fileobj.getGlobalPermission() > 4) {
 	                		isAllowedAccess = true;
 	                	}
 	                	
 	                	// If the user does not have permission, send the signal
 	                	if (!isAllowedAccess) {
 	                    	outStream.println(ConnectionCodes.NOTAUTHORIZED);
 	                    	outStream.flush();
 	                    }
 	                	// Otherwise send the authorized signal, and begin the transfer
 	                    else {
 	                    	outStream.println(ConnectionCodes.AUTHORIZED);
 	                    	outStream.flush();
 	                    	
 	                    	// Get the file input stream
 	                        FileInputStream fis = new FileInputStream(toTransfer);
 	                        byte[] buffer = new byte[distConfig.getBufferSize()];
 	                        
 	                        // Send each set of bytes over
 	                        Integer bytesRead = 0;
 	                        while ((bytesRead = fis.read(buffer)) > 0) {
 	                        	oos.writeObject(bytesRead);
 	                        	oos.writeObject(Arrays.copyOf(buffer, buffer.length));
 	                        	oos.flush();
 	                        }
 	                        
 	                        // Get confirmation that it made it
 	                        inStream.readLine();
 	                    }
                 	}
                 }
             }
             
             // else locate next server to check
             else {
             	int nextCheckID = Integer.parseInt(nst.get_IDAt(0));
             	String nextCheckIP = nst.get_IPAt(0);
             	// For each element in the node search table
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
             	
             	// Send the next location to look
             	outStream.println(ConnectionCodes.WRONGPOSITION);
             	outStream.println(Integer.toString(nextCheckID));
             	outStream.println(nextCheckIP);
             	outStream.flush();
             	
             	// Get confirmation
             	inStream.readLine();
             }
             
             // close everything
             oos.close();
             outStream.close();
             inStream.close();
             client.close();
 		}
 		
 		catch (IOException ex) {
			System.out.println("Inside ServGetFile");
             Logger.getLogger(ServCheckPosition.class.getName()).log(Level.SEVERE, null, ex);
         }
 	}
 }
