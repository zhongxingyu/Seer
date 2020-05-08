 import java.io.*;
 import java.net.*;
 import java.util.*;
 
 class WebRequestHandler {
 	
 	private ISHTTPSyncServer _server;
 
 	private Socket _connectionSocket;
 	private String _documentRoot;
 	private ServerCache _serverCache;
 	private BufferedReader _inFromClient;
 	private DataOutputStream _outToClient;
 
 	private String _urlName;
 	private String _fileName;
 	private File _fileInfo;
 	private String _contentType;
 	private byte[] _fileContents;
 
 	private boolean _ifModifiedSince;
 	private boolean _loadBalancer;
 
 	public WebRequestHandler(Socket connectionSocket, String documentRoot,
 		ServerCache serverCache, ISHTTPSyncServer server)
 		throws IOException, UnsupportedEncodingException
 	{
 		_server           = server;
 		_documentRoot     = documentRoot;
 		_connectionSocket = connectionSocket;
 		_serverCache      = serverCache;
 
 		_inFromClient = new BufferedReader(new InputStreamReader(
 			_connectionSocket.getInputStream(), "US-ASCII"));
 
 		_outToClient = new DataOutputStream(
 			_connectionSocket.getOutputStream());
 
 		_ifModifiedSince = false;
 		_loadBalancer = false;
 	}
 
 	public void processRequest()
 	{
 		SHTTPRequest request = null;
 
 		/* Parse request. */
 		try {
 			request = new SHTTPRequest(_inFromClient);
 		} catch (SHTTPRequestException se) {
 			sendErrorReply(se.getMessage(), se.getErrorCode());
 			return;
 		} catch (IOException ie) {
 			System.err.println("Cannot create new SHTTPRequest: " +
 				ie.getMessage());
 			return;
 		}
 
 		/* Try to map request to file. */
 		try {
 			mapURLToFile(request);
 		} catch (SHTTPRequestException se) {
 			sendErrorReply(se.getMessage(), se.getErrorCode());
 			return;
 		} catch (IOException ie) {
 			System.err.println("Could not read file " +
 				ie.getMessage());
 			sendErrorReply("Internal server error", 
 				Status.INTERNAL_SERVER_ERROR);
 			return;
 		}
 
 		/* If we are sending a load balancing reply, send it. */
 		if (_loadBalancer == true) {
 			if (_server.loadAvailable())
 				sendLoadReply(Status.OK);
 			else
 				sendLoadReply(Status.SERVICE_UNAVAILABLE);
 			return;
 		}
 
 		/* If we are sending a 304 not modified response, send it. */
 		if (_ifModifiedSince == true) {
 			sendNotModifiedReply();
 			return;
 		}
 
 		/* Otherwise, send contents of file. */
 		if (_fileContents == null) {
 			System.err.println("File contents should not be null here.");
 			sendErrorReply("Internal server error",
 				Status.INTERNAL_SERVER_ERROR);
 			return;
 		}
 
 		sendReply();	
 	}
 
 	public void mapURLToFile(SHTTPRequest request)
 		throws IOException, SHTTPRequestException
 	{
 		_urlName = request.getURL();

 		if (_urlName.startsWith("/")) {
 			_urlName = _urlName.substring(1);
 		}
 
 		/* If request is seeking load, set loadBalancer flag and return. */
 		if (_urlName.equals("load")) {
 			_loadBalancer = true;
 			return;
 		}
 
 		/* If file is unnamed, find out if user agent is mobile and map corredt
 		   index.html. */
 		_fileName = _documentRoot + _urlName;
 		if (_urlName.endsWith("/")) {
 			if (request.isMobile()) {
 				_fileName = _fileName + Const.DOC_MINDEX;
 			} else {
 				_fileName = _fileName + Const.DOC_INDEX;
 			}
 		}
 
 		/* Set content type based on file extension. */
 		if (_fileName.endsWith(".jpg"))
 			_contentType = "image/jpeg";
 		else if (_fileName.endsWith(".gif"))
 			_contentType = "image/gif";
 		else if (_fileName.endsWith(".html") || _fileName.endsWith(".htm"))
 			_contentType = "text/html";
 		else
 			_contentType = "text/plain";
 
 		/* Look for file. */
 		_fileInfo = new File(_fileName);
 		if (!_fileInfo.isFile()) {
 			_fileInfo = null;
 			throw new SHTTPRequestException("Not found", Status.NOT_FOUND);
 		}
 
 		/* If isModifiedSince is set and file's last modified time is before
 		   isModifiedSince, then set flag and return.*/
 		if (request.getIfModifiedSince() != null) {
 			long requestTime = request.getIfModifiedSince().getTime();
 			long fileTime = _fileInfo.lastModified();
 			if (fileTime < requestTime) {
 				_ifModifiedSince = true;
 				return;
 			}
 		}
 
 		/* If file is found in cache (by name), the just get the file contents
 		   from server cache. */
		_fileContents = _serverCache.getFile(_fileName).content();
		if (_fileContents != null)
 			return;
 
 		/* If file is not executable, get contents of file and return. */
 		if (!_fileInfo.canExecute()) {
 			_fileContents = new byte[(int) _fileInfo.length()];
 			InputStream in = new FileInputStream(_fileInfo);
 			in.read(_fileContents);
 			return;
 		}
 
 		/* Handle an executable file. */
 		Process proc = Runtime.getRuntime().exec(_fileName);
 		BufferedReader procOut = new BufferedReader(new InputStreamReader(
 			proc.getInputStream()));
 		StringBuffer procOutSb = new StringBuffer();
 		String line;
 		while ((line = procOut.readLine()) != null)
 			procOutSb.append(line);
 		procOut.close();
 		try {
 			proc.waitFor();
 		} catch (InterruptedException ie) {
 			Debug.DEBUG("Interrupted while waiting for proc to die. Derp.");
 		}
 
 		String contentsString = procOutSb.toString();
 		_fileContents = contentsString.getBytes("US-ASCII");
 	}
 
 	public void sendErrorReply(String message, int status)
 	{
 		SHTTPResponse response = new SHTTPResponse();
 
 		response.setStatus(status, message);
 		response.setContent(
 			"<html>" +
 			"<h1>" + status + ": " + message + "</h1>" +
 			"</html>");
 
 		try {
 			response.writeToStream(_outToClient);
 		} catch (IOException ie) {
 			System.err.println("Cannot write error reply to client: " +
 				ie.getMessage());
 		}
 	}
 
 	public void sendLoadReply(int status)
 	{
 		SHTTPResponse response = new SHTTPResponse();
 
 		response.setStatus(status, "Load status");
 		response.setContent("Load status: " + status);
 
 		try {
 			response.writeToStream(_outToClient);
 		} catch (IOException ie) {
 			System.err.println("Cannot write load reply to client: " +
 				ie.getMessage());
 		}
 	}
 
 	public void sendNotModifiedReply()
 	{
 		SHTTPResponse response = new SHTTPResponse();
 
 		response.setStatus(Status.NOT_MODIFIED, "Not modified");
 		response.setContent("File not modified");
 
 		try {
 			response.writeToStream(_outToClient);
 		} catch (IOException ie) {
 			System.err.println("Cannot write not modified reply to client: " +
 				ie.getMessage());
 		}
 	}
 
 	public void sendReply()
 	{
 		SHTTPResponse response = new SHTTPResponse();
 
 		response.setStatus(Status.OK, "Document Follows");
 		try {
 			response.setContent(new String(_fileContents, "US-ASCII"));
 		} catch (UnsupportedEncodingException uee) {
 			System.err.println("Wtf? UnsupportedEncodingException? " +
 				uee.getMessage());
 		}
 		response.setServerName("SHTTP 0.1");
 		response.setContentType(_contentType);
 
 		try {
 			response.writeToStream(_outToClient);
 		} catch (IOException ie) {
 			System.err.println("Cannot write reply to client: " +
 				ie.getMessage());
 		}
 	}	
 }
