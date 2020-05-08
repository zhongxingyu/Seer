 /**
  * This is the solution file for Assignment 1 Problem 4 for ECSE 414 Fall 2012.
  *
  * This class implements a multi-threaded HTTP 1.0-compliant web server. The root directory from which files are
  * served is the same directory from which this application is executed. When the server encounters an error, it
  * sends a response message with the appropriate HTML code so that the error information is displayed.
  *
  * @author michaelrabbat
  *
  */
 import java.io.*;
 import java.net.*;
 import java.util.*;
 import org.w3c.dom.*;
 import javax.xml.*;
 import javax.xml.parsers.*;
 import java.net.*;
 import java.io.FileNotFoundException;
 
 /**
  * This is the main class which runs the loop that listens for incoming requests
  * and spawns new threads to handle each request.
  *
  * @author michaelrabbat
  *
  */
 
 
 public final class WebServer {
 	public static File[] getXMLfiles(File path){
 		int xmlCount = 0;
 		for (File file: path.listFiles()){
 			String extension = file.getName().substring(file.getName().lastIndexOf('.'));
 			if(extension.equals(".xml") || extension.equals("xml")){
 				//System.out.println(file.getName());
 				xmlCount++;
 			}
 		}
 		File[] xmlFiles = new File[xmlCount];
 		for (File file: path.listFiles()){
 			String extension = file.getName().substring(file.getName().lastIndexOf('.'));
 			if(extension.equals(".xml") || extension.equals("xml")){
 				xmlFiles[xmlCount-1] = file;
 				xmlCount--;
 			}
 		}
 		return xmlFiles;
 	}
 
 	private static Document[] buildDOMs(File path){
 		File[] xmlFiles = getXMLfiles(path);
 		// DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
     try{
     	DocumentBuilder dBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
       Document[] doms = new Document [xmlFiles.length];
 			for (int i = 0; i<xmlFiles.length; i++){
 				doms[i] = dBuilder.parse(xmlFiles[i]);
 			}
 			return doms;
 		} catch(Exception e){
 			return null;
 		}
 	}
 
 	private static Hashtable<String,Document> buildTable(File path){
 		File[] xmlFiles = getXMLfiles(path);
 		// DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
     try{
     	DocumentBuilder dBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
       // Document[] doms = new Document [xmlFiles.length];
 
       Hashtable<String,Document> doms = new Hashtable<String,Document>();
 
 			for (int i = 0; i<xmlFiles.length; i++){
 				doms.put(xmlFiles[i].getName().substring(0, xmlFiles[i].getName().lastIndexOf('.')), dBuilder.parse(xmlFiles[i]));
 				// doms[i] = dBuilder.parse(xmlFiles[i]);
 			}
 			return doms;
 		} catch(Exception e){
 			return null;
 		}
 	}
 
 	public static void main(String argx[]) throws Exception {
 		// Set the port number (may not work with 80)
 		int port = 6789;
 		// Document[] doms = buildDOMs(new File("."));
 
 		Hashtable<String,Document> doms = buildTable(new File("."));
 
 		// System.out.println("Root element: " + doms[0].getDocumentElement().getNodeName());
 		// System.out.println("Root element: " + doms[1].getDocumentElement().getNodeName());
 
 		// Create the socket to listen for incoming connections
 		ServerSocket welcomeSocket = new ServerSocket(port);
 
 		// Enter an infinite loop and process incoming connections
 		// Use Ctrl-C to quit the application
 		while (true) {
 			// Listen for a new TCP connection request
 			Socket connectionSocket = welcomeSocket.accept();
 
 			// Construct an HttpRequest object to process the request message
 			HttpRequest request = new HttpRequest(connectionSocket, doms);
 
 			// Create a new thread to process the request
 			Thread thread = new Thread(request);
 
 			// Start the thread
 			thread.start();
 		}
 	}
 }
 
 /**
  * This is the helper class that
  *
  * @author michaelrabbat
  *
  */
 final class HttpRequest implements Runnable {
 	final static String CRLF = "\r\n";
 	Socket socket;
 	Hashtable doms;
 	/**
 	 * Constructor takes the socket for this request
 	 */
 	public HttpRequest(Socket socket, Hashtable doms) throws Exception
 	{
 		this.socket = socket;
 		this.doms = doms;
 	}
 
 	/**
 	 * Implement the run() method of the Runnable interface.
 	 */
 	@Override
 	public void run()
 	{
 		try {
 			processRequest();
 		} catch (Exception e) {
 			System.out.println(e);
 		}
 	}
 
 	// private Document getDOM(String rootElement) throws FileNotFoundException{
 	// 	for(Document dom : doms){
 	// 		if(dom.getNodeName().equals(rootElement)){
 	// 			return dom;
 
 	// 		}
 	// 	}
 	// 	throw new FileNotFoundException();
 	// }
 
 
 	private void getData(Node node, String[] routes, int r) throws FileNotFoundException{
     // do something with the current node instead of System.out
     System.out.println(node.getNodeName());
     NodeList nodeList = node.getChildNodes();
 	if(nodeList.getLength() == 1){
		System.out.println("OUTPUT = " + nodeList.item(0));
 		return;
 	}	else{
 		Node firstNode = nodeList.item(1);
 		
 	    for (int x = 0; x<nodeList.getLength(); x++){
 	    	try{
 	    		System.out.println("Node Name: " + nodeList.item(x).getNodeName());
 	    	} catch (Exception e){
 	    		//We have reached text
 	    		System.out.println("Output = " + nodeList.item(x-1).getNodeName());
 	    	}
 	    }
 		 // try{
 		 //    if(firstNode.getNodeType() == Node.ELEMENT_NODE)
 		 //    System.out.println("Node type " + firstNode.getNodeType());}catch(Exception e){
 		 //    	System.out.println("A");  //EXCEPTION HERE. NULLPOINT WHAT THE FUCK
 		 //    }
 	    if (firstNode.hasAttributes()){
 	    	System.out.println(node.getNodeName() + "'s CHILD HAS ATTRIBUTES");
 	    	String attribute = routes[r+1];
 	    	for (int i = 1; i < nodeList.getLength(); i=i+2) {
 
 	        	Node currentNode = nodeList.item(i);
 				if (currentNode.getAttributes().getNamedItem("id").getNodeValue().equals(attribute)){
 					getData(currentNode, routes, r+2);
 	    			return;
 	    		}
 	   		 }
 
 	    }else{ //if no attribute
 	    	System.out.println(node.getNodeName() + "' CHILD HAS NO ATTRIBUTES");
 	    	String tag = routes[r];
 	    	System.out.println("TAG = "+ tag);
 	    	Node currentNode;
 	    	for (int i = 0; i<nodeList.getLength(); i++){
 	    		currentNode = nodeList.item(i);
 	    		if (currentNode.getNodeName().equals(tag)){
 	    			getData(currentNode, routes, ++r);
 	    			return;
 	    		}
 
 	    	}
 	    	
 	    	throw new FileNotFoundException();
 	    	
 	    }
 
     }
 }
 
 	/**
 	 * This is where the action occurs
 	 * @throws Exception
 	 */
 	private void processRequest() throws Exception
 	{
 		// Get a reference to the socket's input and output streams
 		InputStream is = socket.getInputStream();
 		OutputStream os = socket.getOutputStream();
 
 		System.out.println("Root element: " + ((Document)doms.get("library")).getDocumentElement().getNodeName());
 		System.out.println("Root element: " + ((Document)doms.get("customers")).getDocumentElement().getNodeName());
 
 		// Set up input stream filters
 		BufferedReader inFromClient = new BufferedReader(new InputStreamReader(is));
 		DataOutputStream outToClient = new DataOutputStream(os);
 
 		// Get the request line of the HTTP request message
 		String requestLine = inFromClient.readLine();
 
 		// Display the request line
 		System.out.println();
 		// System.out.println(requestLine);
 
 		// Extract the filename from the request line
 		StringTokenizer tokens = new StringTokenizer(requestLine);
 		String requestMethod = tokens.nextToken();
 		String requestPath = tokens.nextToken();
 		String requestQuery = null;
 
 		URL url = new URL("http://local.dev" + requestPath);
 
 		requestPath = url.getPath();
 		requestQuery = url.getQuery();
 
 		// might want to decode after tokenizing path.
 		requestPath = URLDecoder.decode(requestPath, "UTF-8");
 
 		tokens = new StringTokenizer(requestPath, "/");
 
 		List<String> routes = new ArrayList<String>();
 		String next = null;
 		while (true) {
 			try {
 				next = tokens.nextToken();
 				if (next != null) {
 					routes.add(next);
 				}
 			} catch (Exception e) {
 				break;
 			}
 		}
 
 		Document xmlDOM = null;
 
 		try{
 			xmlDOM = (Document)doms.get(routes.get(0));
 			//xmlDOM.getDocumentElement().normalize();
 			String[] stringRoutes = new String[routes.size()];
 			stringRoutes = routes.toArray(stringRoutes);
 			getData(xmlDOM.getDocumentElement(), stringRoutes, 2);
 
 		}catch(Exception e){
 			//handle xml not found!
 			System.out.println(e);
 		}
 
 
 
 		//System.out.println(routes.get(1));
 		//System.out.println(routes.get(2));
 		//System.out.println(routes.get(3));
 		System.out.println();
 
 		// // Print the Request Method and Path
 		System.out.println("METHOD: " + requestMethod);
 		System.out.println("PATH: " + requestPath);
 		System.out.println("QUERY: " + requestQuery);
 		System.out.println();
 
 
 		// Construct the response message header
 		String statusLine = null;
 		String contentTypeLine = null;
 
 		// statusLine = "HTTP/1.0 200 OK" + CRLF;
 		// contentTypeLine = "Content-type: " + contentType(requestPath) + CRLF;
 
 		statusLine = "HTTP/1.0 404 Not Found" + CRLF;
 		contentTypeLine = "Content-type: text/html" + CRLF;
 
 		switch (requestMethod) {
 			// GET
 			case "GET":
 				System.out.println("get");
 				break;
 
 			// CREATE BUT NOT UPDATE AN EXISTING ONE
 			case "POST":
 				System.out.println("post");
 				break;
 
 			// CREATE AND UPDATE
 			case "PUT":
 				System.out.println("put");
 				break;
 
 			// DELETE
 			case "DELETE":
 				System.out.println("delete");
 				break;
 
 			// DEFAULT
 			default:
 				System.out.println("unrecognized method");
 				// return 501 Not Implemented
 				break;
 		}
 
 		// statusLine = "HTTP/1.0 200 OK" + CRLF;
 		// statusLine = "HTTP/1.0 201 Created" + CRLF;	// creation
 		// statusLine = "HTTP/1.0 202 Accepted" + CRLF;	// update
 		// statusLine = "HTTP/1.0 400 Bad Request" + CRLF;	// bad formed request
 		// statusLine = "HTTP/1.0 404 Not Found" + CRLF;
 		// statusLine = "HTTP/1.0 405 Method Not Allowed" + CRLF;
 		// statusLine = "HTTP/1.0 500 Server Error" + CRLF;
 		// statusLine = "HTTP/1.0 501 Not Implemented" + CRLF;
 
 		// Send the status line and our header (which only contains the content-type line)
 		outToClient.writeBytes(statusLine);
 		outToClient.writeBytes(contentTypeLine);
 		outToClient.writeBytes(CRLF);
 
 		// Send the body of the message (the web object)
 		// sendBytes(fis, outToClient);
 		outToClient.writeBytes("hello");
 
 		// Close the streams and sockets
 		os.close();
 		inFromClient.close();
 		socket.close();
 	}
 
 	/**
 	 * Private method that returns the appropriate MIME-type string based on the suffix of the appended file
 	 * @param fileName
 	 * @return
 	 */
 	private static String contentType(String fileName) {
 		if (fileName.endsWith(".htm") || fileName.endsWith(".html")) {
 			return "text/html";
 		}
 		if (fileName.endsWith(".gif")) {
 			return "image/gif";
 		}
 		if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")) {
 			return "image/jpeg";
 		}
 		return "application/octet-stream";
 	}
 
 	/**
 	 * Private helper method to read the file and send it to the socket
 	 * @param fis
 	 * @param os
 	 * @throws Exception
 	 */
 	private static void sendBytes(FileInputStream fis, OutputStream os) throws Exception {
 		// Allocate a 1k buffer to hold bytes on their way to the socket
 		byte[] buffer = new byte[1024];
 		int bytes = 0;
 
 		// Copy requested file into the socket's output stream
 		while ((bytes = fis.read(buffer)) != -1) {
 			os.write(buffer, 0, bytes);
 		}
 	}
 }
