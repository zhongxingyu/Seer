 /**
  * @author paul
  */
 
 package distserver;
 
 import java.io.BufferedOutputStream;
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.ObjectInputStream;
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
  * This serves as the heartbeat and sink
  * @author paul
  */
 public class ServHeartBeat implements Runnable {
 
 	private boolean is_serv = false;
 	private Socket client = null;
 	private DistConfig distConfig = null;
 	private NodeSearchTable nst = null;
 	private LocalPathList lpl = null;
 	
 	/**
 	 * 
 	 * @param cli : The socket to which the client is connected
 	 * @param is_server : Boolean, is this the server or not
 	 */
 	public ServHeartBeat (Socket cli, boolean is_server) {
 		this.client = cli;
 		this.is_serv = is_server;
 	}
 	
 	@Override
 	public void run() {
 		if (is_serv)
 			this.runas_server(false);
 		
 		else
 			this.runas_client();
 		
 		try {
 			this.client.close();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	/**
 	 * This function will receive the heartbeat message,
 	 * Send the set of file contained on this server
 	 * Then send those necessary to be updated
 	 */
 	@SuppressWarnings("unchecked")
 	public void runas_server (boolean is_predecessor) {
 		// Setup global parameters
 		this.distConfig = DistConfig.get_Instance();
 		this.nst = NodeSearchTable.get_Instance();
 		this.lpl = LocalPathList.get_Instance();
 		
 		try {
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
 	        // Setup the object input stream
 	        ObjectInputStream ois = new ObjectInputStream (client.getInputStream());
 	        
 	        // Send acknowledgment
 	        outStream.println(ConnectionCodes.HEARTBEAT);
 	        outStream.flush();
 	        
 	        // Get ID of the connecting server
 	        int prevID = Integer.parseInt(inStream.readLine());
 	        
 	        Vector<FileObject> filestosend = new Vector<FileObject>();
 	        
 	        // Send the list of files contained on this server
 	        if (!is_predecessor) {
 	        	filestosend = lpl.get_filesBetween(prevID, Integer.parseInt(nst.get_ownID()));
 	        }
 	        else {
 	        	filestosend = lpl.get_filesBetween(Integer.parseInt(nst.get_ownID()), prevID);
 	        }
 	        oos.writeObject(filestosend);
 	        oos.flush();
 	        
 	        // Receive the updated vector of which files the client needs
 	        filestosend = (Vector<FileObject>)ois.readObject();
 	        
 	        String filename;
 	        // Loop through all the files to send
 	        for (int index = 0; index < filestosend.size(); index++) {
 	        	// First get and send the name of the file
 	        	filename = filestosend.get(index).getName();
 	        	outStream.println(filename);
 	        	outStream.flush();
 	        	
             	// Open the file and input stream to that file
             	File toTransfer = new File (distConfig.get_rootPath() + filename);
             	FileInputStream fis = new FileInputStream(toTransfer);
             	byte[] buffer = new byte[distConfig.getBufferSize()];
             	
             	Integer bytesRead = 0;
             	// Loop through the bytes of the file, reading them into the buffer
             	// and sending them to the new client
             	while ((bytesRead = fis.read(buffer)) > 0) {
             		oos.writeObject(bytesRead);
             		oos.writeObject(Arrays.copyOf(buffer, buffer.length));
             		oos.flush();
             	}
             	
             	// Get confirmation for each file sent
             	inStream.readLine();
 	        }
 	        
 	        // Close all streams
 	        oos.close();
 	        outStream.close();
 	        bos.close();
 	        ois.close();
 	        inStream.close();
 		}
 		
 		catch (ClassNotFoundException ex) {
 			Logger.getLogger(ServCheckPosition.class.getName()).log(Level.SEVERE, null, ex);
 		}
 		catch (IOException ex) {
             Logger.getLogger(ServCheckPosition.class.getName()).log(Level.SEVERE, null, ex);
         }
 	}
 	
 	/**
 	 * This function will send the heartbeat message,
 	 * Receive the set of files contained on the next server
 	 * Then receive those files necissary for updating
 	 */
 	@SuppressWarnings("unchecked")
 	public void runas_client () {
 		// Setup global parameters
 		this.distConfig = DistConfig.get_Instance();
 		this.nst = NodeSearchTable.get_Instance();
 		this.lpl = LocalPathList.get_Instance();
 		
 		try {
 			// Get the input stream for the server
 	        BufferedReader inStream = new BufferedReader (
 	                new InputStreamReader(client.getInputStream()));
 	        // Get the output stream for the server
 	        BufferedOutputStream bos = new BufferedOutputStream (
 	                client.getOutputStream());
 	        // Setup the writer to the server
 	        PrintWriter outStream = new PrintWriter(bos, false);
 	        
 	        // Setup the object input stream
 	        ObjectInputStream ois = new ObjectInputStream (client.getInputStream());
 	        // Setup the object writer to the server
 	        ObjectOutputStream oos = new ObjectOutputStream (bos);
 	        
 	        // Receive acknowledgment and send ID
 	        inStream.readLine();
 	        outStream.println(nst.get_ownID());
 	        outStream.flush();
 	        
 	        // Receive the list of files on the server
 	        Vector<FileObject> filesToReceive = (Vector<FileObject>)ois.readObject();
 	        
 	        // Get the list of files that need updating
 	        Vector<FileObject> tmpFilesToReceive = new Vector<FileObject>();
 	        FileObject tmpFO = null;
 	        for (int index = 0; index < filesToReceive.size(); index++) {
 	        	tmpFO = lpl.get_file(filesToReceive.get(index).getName());
 	        	// If the file currently doesn't exist on this node
 	        	if (tmpFO == null) {
 	        		tmpFilesToReceive.add(filesToReceive.get(index));
 	        	}
 	        	// Or if it is out of date
 	        	else if (tmpFO.getLastupdate().before(filesToReceive.get(index).getLastupdate())) {
 	        		tmpFilesToReceive.add(filesToReceive.get(index));
 	        	}
 	        }
 	        
 	        // Send the needed files
 	        oos.writeObject(tmpFilesToReceive);
 	        oos.flush();
 	        
 	        // loop through the files to receive
 	        for (int index = 0; index < tmpFilesToReceive.size(); index++) {
 	        	// Get the file name
 	        	String filename = inStream.readLine();
 	        	
 	        	// Open the file to write to it
 	        	File toReceive = new File (distConfig.get_rootPath() + filename);
 	        	FileOutputStream fos = new FileOutputStream(toReceive);
 	        	byte[] buffer = new byte[distConfig.getBufferSize()];
 	        	
 	        	Integer bytesRead = 0;
 	        	
 	        	// loop to gather all bytes and output them to the file
 	        	do {
 	        		bytesRead = (Integer)ois.readObject();
 	        		buffer = (byte[])ois.readObject();
 	        		fos.write(buffer, 0, bytesRead);
 	        	} while (bytesRead == distConfig.getBufferSize());
 	        	
 	        	// Update the lpl
 	        	lpl.set_file(tmpFilesToReceive.get(index));
 	        	
 	        	// Send acknowledgment that the file was received
 	        	outStream.println(ConnectionCodes.RECEIVEDFILE);
 	        	outStream.flush();
 	        }
 	        			        
 	        // Close all streams
 	        oos.close();
 	        outStream.close();
 	        bos.close();
 	        ois.close();
 	        inStream.close();
 		}
 		
 		catch (ClassNotFoundException ex) {
 			Logger.getLogger(ServCheckPosition.class.getName()).log(Level.SEVERE, null, ex);
 		}
 		catch (IOException ex) {
             Logger.getLogger(ServCheckPosition.class.getName()).log(Level.SEVERE, null, ex);
         }
 	}
 }
 
 
