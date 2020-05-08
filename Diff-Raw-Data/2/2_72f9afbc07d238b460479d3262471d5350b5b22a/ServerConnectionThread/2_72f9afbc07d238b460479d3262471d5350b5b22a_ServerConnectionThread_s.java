 package com.cs456.project.server;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.ObjectOutputStream;
 import java.io.OutputStream;
 import java.io.PrintWriter;
 import java.net.MalformedURLException;
 import java.net.Socket;
 import java.net.URL;
 import java.nio.channels.Channels;
 import java.nio.channels.ReadableByteChannel;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.Set;
 
 import org.apache.log4j.Logger;
 
 import com.cs456.project.common.ConnectionSettings;
 import com.cs456.project.common.Credentials;
 import com.cs456.project.common.FileListObject;
 import com.cs456.project.common.FileWrapper;
 import com.cs456.project.exceptions.AuthenticationException;
 import com.cs456.project.exceptions.InvalidRequestException;
 import com.cs456.project.exceptions.RegistrationException;
 import com.cs456.project.exceptions.RequestExecutionException;
 import com.cs456.project.server.database.DatabaseManager;
 import com.cs456.project.server.requests.DeleteRequest;
 import com.cs456.project.server.requests.DownloadRequest;
 import com.cs456.project.server.requests.FileExistanceRequest;
 import com.cs456.project.server.requests.FileListRequest;
 import com.cs456.project.server.requests.PasswordChangeRequest;
 import com.cs456.project.server.requests.PermissionChangeRequest;
 import com.cs456.project.server.requests.RemoteFileDownloadRequest;
 import com.cs456.project.server.requests.Request;
 import com.cs456.project.server.requests.UploadRequest;
 
 public class ServerConnectionThread extends Thread {
 	private static Logger logger = Logger.getLogger(ServerConnectionThread.class);
 	private Socket socket = null;
 	
 	private final String rootUploadDir = "upload";
 	
 	OutputStream out = null;
 	PrintWriter pw = null;
 	
 	DatabaseManager dbm = null;
 	
 	ServerConnectionThread(Socket socket) {
 		this.dbm = DatabaseManager.getInstance();
 		this.socket = socket;
 	}
 
 	@Override
 	public void run() {
 		Credentials credentials = null;
 		
 		try {
 			credentials = initiateClientConnection();
 			
 			if(credentials == null) {
 				logger.info("The client successfully registered a new account.  Closing the connection...");
 				closeClientConnection();
 				return;
 			}
 		} catch (IOException e) {
 			logger.info("An error occurred while initiating connection with the client", e);
 			closeClientConnection();
 			return;
 		} catch (SQLException e) {
 			logger.error("An error occurred while querying the database for the requested username and password", e);
 			closeClientConnection();
 			return;
 		} catch (AuthenticationException e) {
 			logger.info("The client was not authenticated and thus is being kicked out.  The attempted username and password were:" +
 					" username=" + e.getUsername() + " password=" + e.getPassword());
 			closeClientConnection();
 			return;
 		} catch (RegistrationException e) {
 			logger.info(e.getMessage());
 			closeClientConnection();
 			return;
 		} catch (InvalidRequestException e) {
 			logger.info("The user sent an invalid request during authentication/registration.\n"+ e.getMessage());
 		}
 		
 		Request request = null;
 		try {
 			request = getClientRequest(credentials);
 		} catch (InvalidRequestException e) {
 			logger.info("The client <" + socket.getInetAddress() + "> has sent an invalid request: <" + e.getInvalidRequest() + ">");
 			closeClientConnection();
 			return;
 		} catch (IOException e) {
 			logger.info("The client <" + socket.getInetAddress() + "> disconnected instead of sending a request the clients");
 			closeClientConnection();
 			return;
 		} 
 					
 		switch(request.getRequestType()) {
 		case DOWNLOAD:
 			boolean downloadSuccess = sendFile((DownloadRequest)request);
 			
 			if(!downloadSuccess) {
 				logger.info("Sending the requested file to the client was not successful");
 				closeClientConnection();
 				return;
 			}
 			
 			break;
 		case UPLOAD:
 			boolean uploadSuccess = receiveFile((UploadRequest)request);
 			
 			if(!uploadSuccess) {
 				logger.info("Did not receive the entire file being uploaded.  The client must reconnect and send the rest of it.");
 				closeClientConnection();
 				return;
 			}
 			
 			break;
 		case DELETE:
 			boolean success = deleteFile((DeleteRequest)request);
 			
 			if(!success) {
 				logger.info("Failed to delete the file");
 				closeClientConnection();
 				return;
 			}
 			break;
 		case REMOTE_FILE_DOWNLOAD:
 			boolean remoteSuccess = remoteFileDownload((RemoteFileDownloadRequest)request);
 			
 			if(!remoteSuccess) {
 				logger.info("Failed to remotely download the file");
 				closeClientConnection();
 				return;
 			}
 			break;
 		case PASSWORD_CHANGE:
 			boolean passSuccess = passwordChange((PasswordChangeRequest)request);
 			
 			if(!passSuccess) {
 				logger.info("Failed to change the user's password");
 				closeClientConnection();
 				return;
 			}
 			break;
 		case FILE_EXISTANCE:
 			fileExistance((FileExistanceRequest)request);
 			break;
 		case PERMISSION_CHANGE:
 			permissionChange((PermissionChangeRequest)request);
 			break;
 		case FILE_LIST:
 			fileList((FileListRequest)request);
 			break;
 		}
 		
 		closeClientConnection();		
 	}
 	
 	
 	private Credentials initiateClientConnection() throws IOException, SQLException, AuthenticationException, RegistrationException, InvalidRequestException {
 		out = socket.getOutputStream();
 		pw = new PrintWriter(out);
 
 		String line = null;
 
 		logger.info("Waiting for greeting from client");
 
 		line = readLine(socket);
 		
 		Credentials credentials = null;
 		
 		if(line != null && line.startsWith(ConnectionSettings.GREETING)) {
 			credentials = authenticateClient(line);
 		}
 		else if(line != null && line.startsWith(ConnectionSettings.REGISTRATION_REQUEST)) {
 			registerUser(line);
 		}
 		else {
 			logger.info("The client did not properly do the handshake, " +
 					"and thus the client <" + socket.getInetAddress() + "> is being rejected");
 			
 		}
 		
 		return credentials;
 	}
 	
 	private Credentials authenticateClient(String line) throws SQLException, AuthenticationException, InvalidRequestException {
 		String stringArguments = line.substring(ConnectionSettings.GREETING.length()).trim();
 		String[] arguments = stringArguments.split(" ");
 		
 		if(arguments.length != 2) {
 			pw.write(ConnectionSettings.BAD_AUTHENTICATION + "\n");
 			pw.flush();
 			
 			throw new InvalidRequestException("The user did not provide their username and/or password", line);
 		}
 		
 		String username = arguments[0].toUpperCase();
 		String password = arguments[1];
 		
 		String query = "Select * from USERS where upper(username)=upper('" + username + "')";
 		ResultSet rs = dbm.executeQuery(query);
 		
 		if(!rs.next()) {
 			logger.info("The client <" + socket.getInetAddress() + "> attempted to authenticate with the following invalid credentials, the username does not exist:" +
 					" username=" + username + " password=" + password);
 			
 			pw.write(ConnectionSettings.BAD_AUTHENTICATION + "\n");
 			pw.flush();
 			
 			throw new AuthenticationException("The username was not found in the database", username, password, false);
 		}
 		
 		if(!password.equals(rs.getString("password"))) {
 			logger.info("The client <" + socket.getInetAddress() + "> attempted to authenticate with the following invalid credentials, the password is incorrect:" +
 					" username=" + username + " password=" + password);
 			
 			pw.write(ConnectionSettings.BAD_AUTHENTICATION + "\n");
 			pw.flush();
 			
 			int numFail = rs.getInt("num_fail") + 1;
 			
 			if(numFail == 3) {
 				dbm.executeQuery("UPDATE USERS set num_fail='" + numFail + "', is_locked='Y' where upper(username)=upper('" + username + "')");
 			}
 			else {
 				dbm.executeQuery("UPDATE USERS set num_fail='" + numFail + "'where upper(username)=upper('" + username + "')");
 			}
 		    			
 			throw new AuthenticationException("The username/password combination was not found in the database", username, password, false);
 		}
 		
 		boolean isLocked = "Y".equals(rs.getString("is_locked"));
 		
 		if(isLocked) {
 			pw.write(ConnectionSettings.LOCKED_OUT + "\n");
 			pw.flush();
 			
 			throw new AuthenticationException("The user is locked out", username, password, true);
 		}	
 		
 		if(rs.getInt("num_fail") != 0) {
 			dbm.executeQuery("UPDATE USERS set num_fail='0' where upper(username)=upper('" + username + "')");
 		}
 
 		logger.info("The client <" + socket.getInetAddress() + "> has sent the appropriate greeting...returning the favour");
 
 		pw.write(ConnectionSettings.GREETING + "\n");
 		pw.flush();
 		
 		return new Credentials(username, password);
 	}
 	
 	private Request getClientRequest(Credentials credentials) throws InvalidRequestException, IOException {
 		Request request = null;
 		
 		logger.info("Waiting on client <" + socket.getInetAddress() + "> to send a request");
 
 		String line = readLine(socket);
 		
 		// UPLOAD
 		if (line != null && line.startsWith(ConnectionSettings.UPLOAD_REQUEST)) {
 			logger.info("The client <" + socket.getInetAddress() + "> has sent an upload request: <" + line + ">");
 			
 			String stringArguments = line.substring(ConnectionSettings.UPLOAD_REQUEST.length()).trim();
 			String[] arguments = stringArguments.split(" ");
 			
 			if(arguments.length != 4) {
 				pw.write(ConnectionSettings.UPLOAD_REJECT + "\n");
 				pw.flush();
 				
 				throw new InvalidRequestException("The user did not provide the correct number of arguments for their upload request", line);
 			}
 			
 			String nameOnServer = arguments[0].trim();
 			long fileSize = Long.parseLong(arguments[1].trim());
 			boolean isShared = FileWrapper.charToBoolean(arguments[2].trim());
 			long lastModified = Long.parseLong(arguments[3].trim());
 			
 			request = new UploadRequest(nameOnServer, fileSize, isShared, lastModified, credentials);			
 		}
 		// DOWNLOAD
 		else if (line != null && line.startsWith(ConnectionSettings.DOWNLOAD_REQUEST)) {
 			logger.info("The client <" + socket.getInetAddress() + "> has sent a download request: <" + line + ">");
 			
 			String stringArguments = line.substring(ConnectionSettings.DOWNLOAD_REQUEST.length()).trim();
 			String[] arguments = stringArguments.split(" ");
 			
 			if(arguments.length != 2) {
 				pw.write(ConnectionSettings.DOWNLOAD_REJECT + "\n");
 				pw.flush();
 				
 				throw new InvalidRequestException("The user did not provide the correct number of arguments for their download request", line);
 			}
 			
 			String downloadFilename = arguments[0].trim();
 			String owner = arguments[1].trim();
 			
 			request = new DownloadRequest(downloadFilename, owner, credentials);			
 		}
 		else if (line != null && line.startsWith(ConnectionSettings.DELETE_REQUEST)) {
 			String stringArguments = line.substring(ConnectionSettings.DELETE_REQUEST.length()).trim();
 			String[] arguments = stringArguments.split(" ");
 			
 			if(arguments.length != 1) {
 				pw.write(ConnectionSettings.DELETE_FAIL + "\n");
 				pw.flush();
 				
 				throw new InvalidRequestException("The user did not provide the correct number of arguments for their delete request", line);
 			}
 			
 			String filename = arguments[0];
 			
 			logger.info("The client <" + socket.getInetAddress() + "> has requested to delete file: " + filename);
 			
 			request = new DeleteRequest(filename, credentials);
 		}
 		else if (line != null && line.startsWith(ConnectionSettings.REMOTE_DOWNLOAD_REQUEST)) {
 			String stringArguments = line.substring(ConnectionSettings.REMOTE_DOWNLOAD_REQUEST.length()).trim();
 			String[] arguments = stringArguments.split(" ");
 			
 			if(arguments.length != 3) {
 				pw.write(ConnectionSettings.REMOTE_DOWNLOAD_DECLINE + "\n");
 				pw.flush();
 				
 				throw new InvalidRequestException("The user did not provide the correct number of arguments for their remote download request", line);
 			}
 			
 			String url = arguments[0];
 			String serverLocation = arguments[1];
 			boolean isShared = FileWrapper.charToBoolean(arguments[2]);
 			
 			logger.info("The client <" + socket.getInetAddress() + "> has requested to remotely download the file: " + url +
 					" and store it here: " + serverLocation);
 			
 			request = new RemoteFileDownloadRequest(url, serverLocation, isShared, credentials);
 		}
 		else if(line != null && line.startsWith(ConnectionSettings.PASSWORD_CHANGE_REQUEST)) {
 			String stringArguments = line.substring(ConnectionSettings.PASSWORD_CHANGE_REQUEST.length()).trim();
 			String[] arguments = stringArguments.split(" ");
 			
 			if(arguments.length != 2) {
 				pw.write(ConnectionSettings.PASSWORD_CHANGE_FAILED + "\n");
 				pw.flush();
 				
 				throw new InvalidRequestException("The user did not provide the correct number of arguments for their password change request", line);
 			}
 			
 			String oldPassword = arguments[0];
 			String newPassword = arguments[1];
 			
 			logger.info("The client <" + socket.getInetAddress() + "> has requested to change their password from: " + oldPassword +
 					" to: " + newPassword);
 			
 			request = new PasswordChangeRequest(oldPassword, newPassword, credentials);
 		}
 		else if(line != null && line.startsWith(ConnectionSettings.FILE_EXISTS_REQUEST)) {
 			String stringArguments = line.substring(ConnectionSettings.FILE_EXISTS_REQUEST.length()).trim();
 			String[] arguments = stringArguments.split(" ");
 			
 			if(arguments.length != 2) {
 				pw.write(ConnectionSettings.FILE_EXISTS_REJECT + "\n");
 				pw.flush();
 				
 				throw new InvalidRequestException("The user did not provide the correct number of arguments for their file existance request", line);
 			}
 			
 			String file = arguments[0];
 			String owner = arguments[1];
 			
 			logger.info("The client <" + socket.getInetAddress() + "> has asked whether the file: " + file + " exists on the server");
 			
 			request = new FileExistanceRequest(file, owner, credentials);
 		}
 		else if(line != null && line.startsWith(ConnectionSettings.PERMISSION_CHANGE_REQUEST)) {
 			String stringArguments = line.substring(ConnectionSettings.PERMISSION_CHANGE_REQUEST.length()).trim();
 			String[] arguments = stringArguments.split(" ");
 			
 			if(arguments.length != 2) {
 				pw.write(ConnectionSettings.PERMISSION_CHANGE_FAIL + "\n");
 				pw.flush();
 				
 				throw new InvalidRequestException("The user did not provide the correct number of arguments for their file existance permission change", line);
 			}
 			
 			String file = arguments[0];
 			boolean newPermissions = FileWrapper.charToBoolean(arguments[1]);
 			
 			logger.info("The client <" + socket.getInetAddress() + "> has requested to thange the file's: " + file + " permission to: " + arguments[1]);
 			
 			request = new PermissionChangeRequest(file, newPermissions, credentials);
 		}
 		else if(line != null && line.startsWith(ConnectionSettings.FILE_LIST_REQUEST)) {
 			String stringArguments = line.substring(ConnectionSettings.FILE_LIST_REQUEST.length()).trim();
 			String[] arguments = stringArguments.split(" ");
 			
 			if(arguments.length != 1) {
 				pw.write(ConnectionSettings.FILE_LIST_FAIL + "\n");
 				pw.flush();
 				
 				throw new InvalidRequestException("The user did not provide the correct number of arguments for their file list request", line);
 			}
 			
 			String rootPath = arguments[0];
 			
			if(!rootPath.endsWith("\\")) {
 				rootPath += "\\";
 			}
 			
 			logger.info("The client <" + socket.getInetAddress() + "> has requested to get the file list from root path: " + rootPath);
 			
 			request = new FileListRequest(rootPath, credentials);
 		}
 		else {
 			logger.info("The client <" + socket.getInetAddress() + "> has sent an invalid request: <" + line + ">");
 			throw new InvalidRequestException("An invalid client request occurred", line);
 		}
 		
 		return request;
 	}
 	
 	private boolean deleteFile(DeleteRequest request) {
 		if(request.getFileName().contains("..")) {
 			logger.warn("User: " + request.getUsername() + " is trying to delete a file into another user's directory by using \"..\"." +
 					" The path was: " + request.getFileName());
 			
 			pw.write(ConnectionSettings.DELETE_FAIL + "\n");
 			pw.flush();
 			return false;
 		}
 		
 		String homeDir = request.getUsername().toUpperCase() + "\\" + request.getFileName();
 		String root = rootUploadDir + "\\" + homeDir;
 		
 		FileWrapper wrapper;
 		try {
 			wrapper = dbm.getFile(homeDir, false);
 			
 			if(wrapper == null) {
 				logger.info("Unable to delete the file < " + homeDir + "> as the file does not exist");
 				
 				pw.write(ConnectionSettings.DELETE_FAIL + "\n");
 				pw.flush();
 			}
 			
 			if(!wrapper.getOwner().equals(request.getUsername())) {
 				logger.info("The user: " + request.getUsername() + " was attempting to delete the file: " + wrapper.getFilePath() + " which is owned by: " + wrapper.getOwner());
 				
 				pw.write(ConnectionSettings.DELETE_FAIL + "\n");
 				pw.flush();
 			}
 		} catch (SQLException e) {
 			logger.error("A SQL error occurred while deleting the file: " + homeDir, e);
 			
 			pw.write(ConnectionSettings.DELETE_FAIL + "\n");
 			pw.flush();
 			
 			return false;
 			
 		}
 		
 		File fileToDelete = new File(root);
 		
 		if(!fileToDelete.exists()) {
 			logger.info("Unable to delete the file < " + homeDir + "> as the file does not exist");
 			
 			pw.write(ConnectionSettings.DELETE_FAIL + "\n");
 			pw.flush();
 			
 			return false;
 		}
 		
 		
 		try {
 			FileWrapper deleteWrapper = new FileWrapper(wrapper.getFilePath(), wrapper.getOwner(), wrapper.isShared(), wrapper.isComplete(), true);
 			dbm.updateFile(deleteWrapper.getFilePath(), deleteWrapper);
 		} catch (SQLException e) {
 			logger.error("A SQL error occurred while deleting the file: " + homeDir, e);
 			
 			pw.write(ConnectionSettings.DELETE_FAIL + "\n");
 			pw.flush();
 			
 			return false;
 			
 		} catch (RequestExecutionException e) {
 			logger.info("The update of the file record: " + homeDir + " failed", e);
 			
 			pw.write(ConnectionSettings.DELETE_FAIL + "\n");
 			pw.flush();
 
 			return false;
 		}
 		
 		
 				
 		boolean safeToDelete = FileManager.getInstance().markForDeletion(homeDir);
 		
 		if(!safeToDelete) {
 			pw.write(ConnectionSettings.DELETE_DELAYED + "\n");
 			pw.flush();
 			
 			return true;
 		}
 		
 		boolean success = purgeDeleted(homeDir);
 		
 		if(success) {
 			pw.write(ConnectionSettings.DELETE_SUCCESS + "\n");
 			pw.flush();
 			
 			return true;
 		}
 		else {
 			pw.write(ConnectionSettings.DELETE_FAIL + "\n");
 			pw.flush();
 			
 			return false;
 		}
 	}
 	
 	private boolean sendFile(DownloadRequest request) {
 		if(request.getFileName().contains("..")) {
 			logger.warn("User: " + request.getUsername() + " is trying to download a file into another user's directory by using \"..\"." +
 					" The path was: " + request.getFileName());
 			
 			pw.write(ConnectionSettings.DOWNLOAD_REJECT + "\n");
 			pw.flush();
 			return false;
 		}
 		
 		String homePath = request.getOwner().toUpperCase() + "\\" + request.getFileName();
 		String rootPath = rootUploadDir + "\\" + homePath;
 		
 		File downloadFile = new File(rootPath);
 		
 		try {
 			FileWrapper wrapper = dbm.getFile(homePath, false);
 			
 			if(wrapper == null) {
 				logger.info("The file: " + homePath + " could not be found.");
 				pw.write(ConnectionSettings.DOWNLOAD_REJECT + "\n");
 				pw.flush();
 				
 				return false;
 			}
 			
 			if(!wrapper.getOwner().equals(request.getUsername()) && !wrapper.isShared()) {
 				logger.info("The user: " + request.getUsername() + " attempted to download the file: " + homePath + 
 						" which is owned by: " + wrapper.getOwner() + " and is not shared");
 				pw.write(ConnectionSettings.DOWNLOAD_REJECT + "\n");
 				pw.flush();
 				
 				return false;
 			}
 			
 			if(!wrapper.isComplete()) {
 				logger.info("The user: " + request.getUsername() + " attempted to download the file: " + homePath + 
 						" which is not complete");
 				pw.write(ConnectionSettings.DOWNLOAD_REJECT + "\n");
 				pw.flush();
 				
 				return false;
 			}
 			
 		} catch (SQLException e) {
 			logger.error("A SQL error occurred while retrieving the file: " + homePath + " from the database", e);
 			return false;		
 		} 	
 		
 		logger.info("The client <" + socket.getInetAddress() + "> is requesting the following file: " +
 				homePath + " <" +  downloadFile.length() + " bytes>");
 
 		FileInputStream fis = null;
 		
 		boolean safeToDownload = FileManager.getInstance().incrementFileInUse(homePath);
 		
 		if(!safeToDownload) {
 			logger.info("The file: " + homePath + " the user: " + request.getUsername() + " requested has been deleted.");
 			
 			pw.write(ConnectionSettings.DOWNLOAD_REJECT + "\n");
 			pw.flush();
 			
 			return false;
 		}
 		
 		try {
 			fis = new FileInputStream(downloadFile);
 			logger.info("Telling the client <" + socket.getInetAddress() + "> that their download request is going to be serviced");
 	
 			pw.write(ConnectionSettings.DOWNLOAD_OK + " " + downloadFile.length() + " " + downloadFile.lastModified() + "\n");
 			pw.flush();
 			
 			String line = readLine(socket);
 			if(line == null || !line.startsWith(ConnectionSettings.DOWNLOAD_OK)) {
 				logger.error("The user did not send the download_ok message for the download request.  Instead they sent: <" + line + ">");
 				
 				pw.write(ConnectionSettings.DOWNLOAD_REJECT + "\n");
 				pw.flush();
 				
 	        	return false;
 			}
 			
 			String stringArugments = line.substring(ConnectionSettings.DOWNLOAD_OK.length()).trim();
 	        String[] arguments = stringArugments.split(" ");
 	        
 	        if(arguments.length != 1) {
 	        	logger.error("The user did not provide the correct number of arguments for their download request: <" + line + ">");
 	        	
 	        	pw.write(ConnectionSettings.DOWNLOAD_REJECT + "\n");
 				pw.flush();
 	        	
 	        	return false;
 	        }
 	        
 	        pw.write(ConnectionSettings.DOWNLOAD_OK + "\n");
 			pw.flush();
 	        
 	        long startPosition = Long.parseLong(arguments[0]);
 	
 			byte[] buffer = new byte[64000];
 	
 			boolean readPartFile = startPosition != 0;
 			
 			long totalBytesRead = 0;
 			
 			while (true) {
 				int numBytesRead = 0;
 				numBytesRead = fis.read(buffer);
 				if (numBytesRead == -1)	break;
 				
 				totalBytesRead += numBytesRead;
 	
 				if(readPartFile) {
 					if(totalBytesRead > startPosition) {
 						int numBytesLeft = (int)(startPosition % 64000);
 						out.write(buffer, numBytesLeft, numBytesRead - numBytesLeft);
 						out.flush();
 						readPartFile = false;
 					}
 				}
 				else {
 					out.write(buffer, 0, numBytesRead);
 					out.flush();
 				}
 				
 				logger.debug("Sent " + totalBytesRead + "/" + downloadFile.length() + " bytes");
 			}
 
 			fis.close();
 
 			logger.info("The requested download file: " + homePath
 					+ " has been sent in its entirety");
 
 			line = readLine(socket);
 
 			if (!ConnectionSettings.DOWNLOAD_FINISHED.equals(line)) {
 				logger.warn("The client did not send the download finished message.  It sent: <" + line + ">");
 				boolean deletionRequired = FileManager.getInstance().decrementFileInUse(homePath);
 				
 				if(deletionRequired) {
 					purgeDeleted(homePath);
 				}
 				
 				return false;
 			}
 		} catch(IOException e) {
 			logger.info("The user: " + request.getUsername() + " has prematurely disconnected from downloding their file: " + homePath);
 			boolean deletionRequired = FileManager.getInstance().decrementFileInUse(homePath);
 			
 			if(deletionRequired) {
 				purgeDeleted(homePath);
 			}
 			
 			return false;
 		}
 		
 		boolean deletionRequired = FileManager.getInstance().decrementFileInUse(homePath);
 		
 		if(deletionRequired) {
 			purgeDeleted(homePath);
 		}
 		
 		return true;
 	}
 	
 	private boolean receiveFile(UploadRequest request) {
 		logger.info("The client <" + socket.getInetAddress() + "> has sent an upload request for a file of size " + 
 				request.getFileSize() + " bytes");
 		
 		if(request.getNameOnServer().contains("..")) {
 			logger.warn("User: " + request.getUsername() + " is trying to upload a file into another user's directory by using \"..\"." +
 					" The path was: " + request.getNameOnServer());
 			
 			pw.write(ConnectionSettings.UPLOAD_REJECT + "\n");
 			pw.flush();
 			return false;
 		}
 		
 		String homePath = request.getUsername().toUpperCase() + "\\" + request.getNameOnServer();
 		String rootPath = rootUploadDir + "\\" + homePath;
 		
 		File destinationPart = new File(rootPath + ".part");
 		File destination = new File(rootPath);
 		
 		if(destination.exists()) {
 			logger.info("Unable to upload the file as the file already exists: " + homePath);
 			
 			pw.write(ConnectionSettings.UPLOAD_REJECT + "\n");
 			pw.flush();
 			return false;
 		}
 		
 		if(destinationPart.exists() && destinationPart.lastModified() != request.getLastModified()) {
 			logger.info("Unable to upload the file: " + request.getNameOnServer() + " as the last modified date of the .part " +
 					"file on the server does not match that of the clients.");
 			
 			pw.write(ConnectionSettings.UPLOAD_OUT_OF_DATE + "\n");
 			pw.flush();
 			
 			return false;
 		}
 		
 		long partFileLength = destinationPart.exists() ? destinationPart.length() : 0;
 		long totalBytesRead = partFileLength;
 		
 		try {		
 			FileOutputStream fileOut = new FileOutputStream(destinationPart, partFileLength != 0);
 			
 			FileWrapper wrapper = new FileWrapper(homePath + ".part", request.getUsername(), request.isShared(), false, false);
 			if(partFileLength == 0) {
 				try {
 					dbm.addFile(wrapper);
 				} catch (SQLException e) {
 					logger.error("A SQL error occurred while adding a new file: " + wrapper.getFilePath() + " to the database", e);
 					
 					pw.write(ConnectionSettings.UPLOAD_FAIL + "\n");
 					pw.flush();
 	
 					return false;				
 				} catch (RequestExecutionException e) {
 					logger.info("The insertion of the new file record: " + wrapper.getFilePath() + " failed", e);
 					
 					pw.write(ConnectionSettings.UPLOAD_FAIL + "\n");
 					pw.flush();
 					
 					return false;
 				}
 			}
 			else {
 				try {
 					dbm.updateFile(wrapper.getFilePath(), wrapper);
 				} catch (SQLException e) {
 					logger.error("A SQL error occurred while updating the file: " + wrapper.getFilePath() + " in the database", e);
 					
 					pw.write(ConnectionSettings.UPLOAD_FAIL + "\n");
 					pw.flush();
 	
 					return false;
 				} catch (RequestExecutionException e) {
 					logger.info("The update of the file record: " + wrapper.getFilePath() + " failed", e);
 					
 					pw.write(ConnectionSettings.UPLOAD_FAIL + "\n");
 					pw.flush();
 	
 					return false;
 				}
 			} 			
 		
 			pw.write(ConnectionSettings.UPLOAD_OK + " " + partFileLength + "\n");
 			pw.flush();
 			
 			
 			byte[] buffer = new byte[64000];
 	
 			InputStream inFile = socket.getInputStream();
 	
 			logger.info("Starting the upload of the file");
 	
 			while (true) {
 				if (totalBytesRead == request.getFileSize())
 					break;
 				
 				int numBytesRead = inFile.read(buffer);
 				if (numBytesRead == -1)
 					break;
 				
 				fileOut.write(buffer, 0, numBytesRead);
 				fileOut.flush();
 	
 				totalBytesRead += numBytesRead;
 				
 				logger.debug("Received " + totalBytesRead + "/" + request.getFileSize() + " bytes of the file");
 			}
 			
 			fileOut.close();
 		} catch(IOException e) {
 			logger.info("The user: " + request.getUsername() + " has prematurely disconnected from downloding their file: " + homePath);
 			return false;			
 		}
 		
 		if (totalBytesRead != request.getFileSize()) {
 			logger.warn("Only received " + totalBytesRead + "/" + request.getFileSize() + " bytes of the uploaded file: " + homePath);
 			
 			pw.write(ConnectionSettings.UPLOAD_FAIL + " " + totalBytesRead + "\n");
 			pw.flush();
 			
 			return false;
 		}
 		
 		if(!destinationPart.renameTo(destination)) {
 			logger.error("Although the .part file is complete, could not rename the file to remove the .part..." +
 					"please fix the problem and run the upload again, or manually remove the .part extension");
 			
 			pw.write(ConnectionSettings.UPLOAD_FAIL + " " + totalBytesRead + "\n");
 			pw.flush();
 			return false;
 		}
 		
 		FileWrapper wrapper = new FileWrapper(homePath, request.getUsername(), request.isShared(), true, false);
 		try {
 			dbm.updateFile(wrapper.getFilePath() + ".part", wrapper);
 		} catch (SQLException e) {
 			logger.error("A SQL error occurred while updating the file: " + wrapper.getFilePath() + " in the database", e);
 			
 			pw.write(ConnectionSettings.UPLOAD_FAIL + "\n");
 			pw.flush();
 
 			return false;
 		} catch (RequestExecutionException e) {
 			logger.info("The update of the file record: " + wrapper.getFilePath() + " failed", e);
 			
 			pw.write(ConnectionSettings.UPLOAD_FAIL + "\n");
 			pw.flush();
 
 			return false;
 		}		
 
 		logger.info("Received the file in its entirety");
 
 		pw.write(ConnectionSettings.UPLOAD_FINISHED + "\n");
 		pw.flush();
 		
 		return true;
 	}
 	
 	private boolean remoteFileDownload(RemoteFileDownloadRequest request) {
 		if(request.getServerLocation().contains("..")) {
 			logger.warn("User: " + request.getUsername() + " is trying to remotely download a file into another user's directory by using \"..\"." +
 					" The path was: " + request.getServerLocation());
 			
 			pw.write(ConnectionSettings.REMOTE_DOWNLOAD_DECLINE + "\n");
 			pw.flush();
 			return false;
 		}
 		
 		String homeDir = request.getUsername().toUpperCase() + "\\" + request.getServerLocation();
 		String root = rootUploadDir + "\\" + homeDir;
 		
 		File serverFilePart = new File(root + ".part");
 		File serverFile = new File(root);
 		
 		if(serverFilePart.exists()) {
 			logger.info("Unable to download file as the part file already exists < " + homeDir + ">");
 			
 			pw.write(ConnectionSettings.REMOTE_DOWNLOAD_DECLINE + "\n");
 			pw.flush();
 			
 			return false;
 		}
 		
 		if(serverFile.exists()) {
 			logger.info("Unable to download file as the file already exists < " + homeDir + ">");
 			
 			pw.write(ConnectionSettings.REMOTE_DOWNLOAD_DECLINE + "\n");
 			pw.flush();
 			
 			return false;
 		}
 		
 		FileWrapper wrapper = new FileWrapper(homeDir + ".part", request.getUsername(), request.isShared(), false, false);
 		try {
 			dbm.addFile(wrapper);
 		} catch (SQLException e) {
 			logger.error("A SQL error occurred while adding a new file: " + wrapper.getFilePath() + " to the database", e);
 			pw.write(ConnectionSettings.REMOTE_DOWNLOAD_DECLINE + "\n");
 			pw.flush();
 			
 			return false;
 			
 		} catch (RequestExecutionException e) {
 			logger.info("The insertion of the new file record: " + wrapper.getFilePath() + " failed", e);
 			
 			pw.write(ConnectionSettings.REMOTE_DOWNLOAD_DECLINE + "\n");
 			pw.flush();
 			
 			return false;
 		}
 		
 		pw.write(ConnectionSettings.REMOTE_DOWNLOAD_ACCEPT + "\n");
 		pw.flush();
 		
 		try {
 			URL url = new URL(request.getUrl());
 		    ReadableByteChannel rbc = Channels.newChannel(url.openStream());
 		    FileOutputStream fos = new FileOutputStream(serverFilePart);
 		    fos.getChannel().transferFrom(rbc, 0, 1 << 24);
 		    fos.close();
 		    
 		    boolean success = serverFilePart.renameTo(serverFile);
 		    
 		    if(!success) {
 		    	logger.error("Although the .part file was fully downloaded, it could not be renamed to remove the .part..." +
 						"please manually remove the .part extension, or delete the .part file and request the remote download again");
 				return false;
 		    }
 		    
 		    wrapper = new FileWrapper(homeDir, request.getUsername(), request.isShared(), true, false);
 			try {
 				dbm.updateFile(homeDir + ".part", wrapper);
 			} catch (SQLException e) {
 				logger.info("A SQL error occurred while updating the file: " + homeDir + ".part" + " in the database", e);
 				
 				return false;
 				
 			} catch (RequestExecutionException e) {
 				logger.error("The update of the file record: " + homeDir + ".part" + " failed", e);
 				
 				return false;
 			}
 		    
 		} catch (MalformedURLException e) {
 			logger.error("Bad URL: " + request.getUrl(), e);
 			return false;
 		} catch (IOException e) {
 			logger.error("An error has occurred while downloading the file", e);
 			return false;
 		}
 		
 		logger.info("The remote file was successfully downloaded.");
 		
 		return true;
 	}
 	
 	private void registerUser(String line) throws RegistrationException, InvalidRequestException {
 		String stringArguments = line.substring(ConnectionSettings.REGISTRATION_REQUEST.length()).trim();
 		String[] arguments = stringArguments.split(" ");
 		
 		if(arguments.length != 2) {
 			throw new InvalidRequestException("The user did not provide the correct number of arguments for their registration request", line);
 		}
 		
 		String username = arguments[0];
 		String password = arguments[1];
 		
 		logger.info("The client <" + socket.getInetAddress() + "> has requested to register a new user with username: " + username +
 				" and password: " + password);
 		
 		try {
 			dbm.registerUser(username, password);
 		} catch (SQLException e) {
 			logger.error("A SQL error occurred while trying to register a new user with username: " + username + " and password: " + password, e);
 			
 			pw.write(ConnectionSettings.REGISTRATION_FAILED + "\n");
 			pw.flush();
 			
 			throw new RegistrationException("The registration was not successful due to an SQL error");
 		} catch (RequestExecutionException e) {
 			logger.info(e.getMessage());
 			
 			pw.write(ConnectionSettings.REGISTRATION_INVALID + "\n");
 			pw.flush();
 			
 			throw new RegistrationException("The registration was not successful as the username is already in use");
 		}
 		
 		pw.write(ConnectionSettings.REGISTRATION_OK + "\n");
 		pw.flush();
 		
 		logger.info("The new client: " + username + " was successfully registered");
 		
 		File directory = new File(rootUploadDir + "\\" + username.toUpperCase());
 		if(directory.exists()) {
 			logger.info("Attempted to create home directory for new user but it already exists: " + username);
 		}
 		else {
 			boolean success = directory.mkdir();
 			if(!success) {
 				logger.error("Unable to create the new user's home directory...otherwise the user is registered. <" + username.toUpperCase() + ">");
 			}
 		}
 	}
 	
 	private boolean passwordChange(PasswordChangeRequest request) {
 		try {
 			dbm.passwordChange(request.getUsername(), request.getOldPassword(), request.getNewPassword());
 		} catch (SQLException e) {
 			pw.write(ConnectionSettings.PASSWORD_CHANGE_FAILED + "\n");
 			pw.flush();
 			
 			logger.error("An SQL exception has occurred while attempting to change the user's password", e);
 			return false;
 		} catch (RequestExecutionException e) {
 			pw.write(ConnectionSettings.PASSWORD_CHANGE_FAILED + "\n");
 			pw.flush();
 			
 			logger.info("The old password the user provided was not correct");
 			return false;
 		}
 		
 		pw.write(ConnectionSettings.PASSWORD_CHANGE_OK + "\n");
 		pw.flush();
 		
 		logger.info("The user's password was successfully changed");
 		
 		return true;
 	}
 	
 	private void fileExistance(FileExistanceRequest request) {
 		if(request.getFileName().contains("..")) {
 			logger.warn("User: " + request.getUsername() + " is trying to look for a file in another user's directory by using \"..\"." +
 					" The path was: " + request.getFileName());
 			
 			pw.write(ConnectionSettings.FILE_EXISTS_NO + "\n");
 			pw.flush();
 			return;
 		}
 		
 		String homeDir = request.getOwner().toUpperCase() + "\\" + request.getFileName();
 		String root = rootUploadDir + "\\" + homeDir;
 		
 		File file = new File(root);
 		
 		try {
 			FileWrapper wrapper = dbm.getFile(homeDir, true);
 			
 			if(wrapper == null) {
 				logger.info("The server does not have the requested file: " + request.getFileName());
 				
 				pw.write(ConnectionSettings.FILE_EXISTS_NO + "\n");
 			}
 			else {
 				if(!wrapper.getOwner().equals(request.getUsername()) && !wrapper.isShared()) {
 					logger.warn("The user: " + request.getUsername() + " requests the existance of a file: " + request.getFileName() + "which was: " + wrapper.getOwner() + " and was not shared");
 					
 					pw.write(ConnectionSettings.FILE_EXISTS_NO + "\n");
 				}
 				else {
 					logger.info("The server has the requested file: " + request.getFileName());
 					
 					pw.write(ConnectionSettings.FILE_EXISTS_YES + "\n");
 				}
 			}
 			
 			pw.flush();
 			
 		} catch (SQLException e) {
 			logger.error("A SQL error occurred while requesting for the existance of file: " + file.getName(), e);
 			
 			pw.write(ConnectionSettings.FILE_EXISTS_NO + "\n");
 			pw.flush();
 		}
 	}
 	
 	private void permissionChange(PermissionChangeRequest request) {
 		if(request.getFileName().contains("..")) {
 			logger.warn("User: " + request.getUsername() + " is trying to look for a file in another user's directory by using \"..\"." +
 					" The path was: " + request.getFileName());
 			
 			pw.write(ConnectionSettings.PERMISSION_CHANGE_FAIL + "\n");
 			pw.flush();
 			return;
 		}
 		
 		String homeDir = request.getUsername().toUpperCase() + "\\" + request.getFileName();
 				
 		FileWrapper wrapper = new FileWrapper(homeDir, request.getUsername(), request.isShared(), true, false);
 		
 		try {
 			dbm.updatePermissions(wrapper);
 			
 			logger.info("The change of permissions to: " + request.isShared() + " was successful for file: " + request.getFileName());
 			
 			pw.write(ConnectionSettings.PERMISSION_CHANGE_SUCCESS + "\n");
 			pw.flush();
 			
 		} catch (SQLException e) {
 			logger.error("A SQL error occurred while requesting a change of permissions for the file: " + request.getFileName(), e);
 			
 			pw.write(ConnectionSettings.PERMISSION_CHANGE_FAIL + "\n");
 			pw.flush();
 		} catch (RequestExecutionException e) {
 			logger.info("The change of permissions request was rejected by the database for file: " + request.getFileName(), e);
 			
 			pw.write(ConnectionSettings.PERMISSION_CHANGE_FAIL + "\n");
 			pw.flush();
 		}
 	}
 	
 	private void fileList(FileListRequest request) {
 		if(request.getRootPath().contains("..")) {
 			logger.warn("User: " + request.getUsername() + " provided a root path in another user's directory by using \"..\"." +
 					" The path was: " + request.getRootPath());
 			
 			pw.write(ConnectionSettings.FILE_LIST_FAIL + "\n");
 			pw.flush();
 			return;
 		}
 			
 		try {
 			Set<FileListObject> fileList = dbm.getFileList(request.getRootPath(), request.getUsername());
 			
 			logger.info("The file list retrieval for the root directory: " + request.getRootPath() + " for user " + request.getUsername());
 						
 			pw.write(ConnectionSettings.FILE_LIST_SUCCESS + " " + fileList.size() + "\n");
 			pw.flush();
 			
 			try {
 				ObjectOutputStream oos = new ObjectOutputStream(out);
 				
 				for(FileListObject file : fileList) {
 					oos.writeObject(file);
 					oos.flush();
 				}
 				
 				oos.close();
 			} catch(IOException e) {}
 		} catch (SQLException e) {
 			logger.error("A SQL error occurred while retrieving the file list for the root directory: " + request.getRootPath() + " for user: " + request.getUsername(), e);
 			
 			pw.write(ConnectionSettings.FILE_LIST_FAIL + "\n");
 			pw.flush();
 		} 
 	}
 	
 	private boolean purgeDeleted(String filePath) {
 		logger.info("Perging the file: " + filePath + " which has been marked for deletion");
 		
 		try {
 			dbm.deleteFile(filePath);
 		} catch (SQLException e) {
 			logger.error("A SQL error occurred while deleting the file: " + filePath, e);
 			
 			return false;
 		} catch (RequestExecutionException e) {
 			logger.info("The deletion of the file record: " + filePath + " failed", e);
 			
 			return false;
 		}
 		
 		File fileToDelete = new File(rootUploadDir + "\\" + filePath);
 				
 		if (!fileToDelete.delete()) {
 			logger.error("Unable to delete the file < " + filePath + ">");
 			
 		    return false;
 		}
 		
 		logger.info("The deletion of the file: " + filePath + " was successful");
 
 		return true;
 	}
 	
 	private String readLine(Socket socket) throws IOException {
 		String line = new String();
 		int c;
 
 		while ((c = socket.getInputStream().read()) != '\n') {
 			if(c == -1) {
 				throw new IOException("The socket closed before being able to read the end of the line");
 			}
 			
 			line += (char) c;
 		}
 
 		return line.trim();
 	}
 	
 	private void closeClientConnection() {
 		try {
 			logger.info("Closing the client socket");
 			if(socket!=null) socket.close();
 		} catch (IOException e) {
 			logger.error("An error occurred while closing the client socket", e);
 		}
 	}
 	
 }
