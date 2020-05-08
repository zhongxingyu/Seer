 package server;
 
 import java.awt.Color;
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.PrintWriter;
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.concurrent.LinkedBlockingQueue;
 
 import view.ErrorMessage;
 
 import model.Document;
 
 /**
  * This class runs the server that hosts the documents and maintains connections with
  * clients. This class needs to be run before any clients connect to the server. This 
  * server can handle multiple clients connecting to it concurrently. 
  * 
  * The server interacts with the client by running two threads that continually pull 
  * from streams. One of the threads looks for new connections and attaches the socket 
  * of any new connection to the serverSocket of the server. The other thread handles
  * requests from the client. Clients can view, create, and modify documents and log in 
  * and log out, among other actions. This thread reads the request from each client 
  * and modifies the corresponding document. We keep track of the clients by assigning
  * each client a unique ID. 
  * 
  * Rep Invariants:
  * 		onlineUsers is a list of unique names
  * 		Each document has a unique name
  * 
  * 
  * Grammar of server --> client messages
  * 		UPDATE := "update|" + docName + "|" + collaborators + "|" + colors
  * 		OPENED := "opened|" + userName + "|" + docName + "|" + docContent + "|" + collaborators + "|" + version
  *      CHANGED := INSERTION | DELETION
  * 		INSERTION := "changed|" + userName + "|" + docName + "|" + change + "|" + position + "|" + length + "|" + versionNumber + "|" + color
  *		DELETION := "changed|" + userName + "|" + docName + "|" + position + "|" + length + "|" + versionNumber
  *		NEWDOC := CREATED | NOTCREATED
  *		CREATED := 	"created|" + userName + "|" + docName + "|" + userName + "|" + date + "|" + colors
  *		NOTCREATED := "notcreatedduplicate"
  *		UPDATECOLLAB :=  "update|" + docName + "|" + collaborators + "|" + colors 
  *		OPENDOC := "opened|" + userName + "|" + docName + "|" + docContent + "|" + collaborators + "|" + version + "|" colors
  * 		EXITDOC := "exitteddoc" userName docName //TODO this should be updated to be split up by pipes
  * 		DOCTABLEINFO := DOCINFO+ "enddocinfo" newline
  * 		DOCINFO := docName tab docDate tab docCollab newline
  * 		LOGGEDIN := "loggedin " userName ID  newline DOCTABLEINFO //TODO this should be changed to pipes
  * 		NOTLOGGEDIN := WRONGPASSWORD | DUPLICATEUSER
  * 		WRONGPASSWORD := "wrongpassword" ID //TODO split on pipes
  * 		DUPLICATEUSER := "notloggedin" //TODO include other information
  */
 public class Server {
 	//default port 
 	private static int port = 4444;
 	
 	//socket where clients will access the server
 	private final ServerSocket serverSocket;
 	
 	//colors for the text of each user
 	private final Color[] COLORS = new Color[] {Color.red, Color.blue, Color.green, Color.orange, Color.magenta, Color.lightGray};
 	private final int NUM_COLORS = COLORS.length;
 
 	//list of documents on the server
 	private List<Document> currentDocuments;
 	
 	//list of users who are online at the time
 	private Set<String> onlineUsers;
 	//these map usernames to both color and password
 	private Map<String, Color> userColorMappings;
 	
 	//mapping each socket ID to a user
 	private Map<Integer, String> socketUserMappings;
 	
 	//each output stream corresponds to a certain client
 	private List<PrintWriter> outputStreamWriters;
 	
 	//this queue contains the requests from the various clients
 	private LinkedBlockingQueue<ServerRequest> queue;
 	
 	
 	/**
 	 * Initializes the Server with the default port number
 	 * Calls the constructor that specifies the port number
 	 * @throws IOException If there is an error creating the server socket
 	 */
 	public Server() throws IOException {
 		this(port);
 	}
 	
 	/**
 	 * Initializes the Server with the given port number
 	 * This constructor basically just initializes all of the socket variables
 	 * Once this is ran, the server is ready to serve and is ready to accept
 	 * client connections and interact with those clients. The serve method
 	 * also needs to be called after this constructor is finished. 
 	 * @param port The port number to which the server publishes messages
 	 * @throws IOException If there is an error creating the server socket
 	 */
 	public Server(int givenPort) throws IOException {
 		serverSocket = new ServerSocket(port);
 		
 		currentDocuments = new ArrayList<Document>();
 
 		onlineUsers = new HashSet<String>();
 		userColorMappings = new HashMap<String, Color>();
 		socketUserMappings = new HashMap<Integer, String>();
 		
 		outputStreamWriters = new ArrayList<PrintWriter>();
 		
 		queue = new LinkedBlockingQueue<ServerRequest>();
 	}
 	
 	/**
      * Run the server, listening for client connections and handling them.  
      * Never returns unless an exception is thrown.
      * @throws IOException if the main server socket is broken
      * (IOExceptions from individual clients do *not* terminate serve()).
      */
     public void serve() throws IOException {
     	int ID = 0;
         while (true) {
             // block until a client connects
         	//TODO we might want to synchronize this so if multiple clients connect at the same time we will not have interleaving problems
             Socket socket = serverSocket.accept();
             ID++; //ID keeps track of the socket ID, to take care of users exiting
             Thread socketThread = new Thread(new ConnectionHandler(socket, ID));
             socketThread.start();
         }
     }
     
     /**
      * Class defined to help start a new thread every time a new client connects to the server
      * This class is run by the Server when it is serving a client. It just looks
      * at the stream and handles the connection. 
      */
     private class ConnectionHandler implements Runnable {
     	Socket socket;
     	int ID;
     	
     	public ConnectionHandler(Socket socket, int ID) {
     		this.socket = socket;
     		this.ID = ID;
     	}
     	
 		@Override
 		public void run() {
 			try {
 				handleConnection(socket, ID);
 			} catch (IOException e) {
 				e.printStackTrace();
 			}			
 		}
     }
     
     /**
      * Closes the server socket.
      */
     void shutDown() {
     	try {
 			serverSocket.close();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
     }
     
     
     /**
      * Handle a single client connection.  Returns when client disconnects.
      * @param socket socket where the client is connected
      * @param ID of the client 
      * @throws IOException if connection has an error or terminates unexpectedly
      */
 	public void handleConnection(Socket socket, int ID) throws IOException {
 		BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
         PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
         
         try {
         	synchronized (outputStreamWriters) {
         		outputStreamWriters.add(out);
         	}
         	//only print to the server which made the connection
         	out.println("id&id=" + ID + "&");
         	
         	//as the server gets request form the client it will add these requests to a queue
         	//these requests will be handles in order that they are added to the queue
 	        for (String line = in.readLine(); line!=null; line=in.readLine()) {
 	        	ServerRequest serverRequest = new ServerRequest(ID, line);
 	        	queue.add(serverRequest);
 	        }
         } catch (IOException e) {
         	if (socketUserMappings.containsKey(ID)) {
         		String userName = socketUserMappings.get(ID);
         		onlineUsers.remove(userName);
         		userColorMappings.remove(userName);
         		socketUserMappings.remove(ID);
         	}
         	outputStreamWriters.remove(out);
         	out.close();
         	in.close();
         }
 	}
 	
 	/**
 	 * Attends to the different requests made by the different clients.
 	 * @throws InterruptedException throws an interrupted exception when popping out of the block queue is interrupted
 	 */
 	public synchronized void attendRequest() throws InterruptedException {
 		//keep looping through the queue checking is there is a request in the queue
 		//we only want one of these methods running at once
 		while (true) {
 			ServerRequest serverRequest = queue.take();
 			
 			//handle the request 
 			//TODO do we want to synchronize handling the request so that we do not handle multiple at once
 			String response = handleRequest(serverRequest);
 			
 			//propagate the response of the request to all of clients
            for (PrintWriter outputStream : outputStreamWriters) {
            	outputStream.println(response);
            }
 		}
 	}
   
     /**
      * Handler for client input
      * Make requested mutations on the model if applicable, then return 
      * appropriate message to the user.
      * @param serverRequest ServerRequest object
      */
     private String handleRequest(ServerRequest serverRequest) {
     	//initialize a few commonly used strings
     	String userName = "";
     	String docName = "";	
     	
     	RequestType requestType = serverRequest.getType();
     	Map<String, String> requestMap = serverRequest.getMap();
     	int ID = serverRequest.getID();
     	
     	switch (requestType) {
     	case LOGIN:
     		//attempts to log the user in, checks if name is unique
 			userName = requestMap.get("userName");
 			return logIn(userName, ID);
 			
     	case NEWDOC:
 			//creates a new document if the input is formatted validly
 			userName = requestMap.get("userName");
 			docName = requestMap.get("docName");
 			return newDoc(userName, docName);
 			
 		case OPENDOC: 
 			//opens a document if the input is validly formatted
 			userName = requestMap.get("userName");
 			docName = requestMap.get("docName");
 			return openDoc(userName, docName);		
 	
 		case CHANGEDOC:
 			//passes off the input to a helper method
 			//this is called when a user inserts or deletes a character in a document
 			userName = requestMap.get("userName");
 			docName = requestMap.get("docName");
 			int position = Integer.valueOf(requestMap.get("position"));
 			int length = Integer.valueOf(requestMap.get("length"));
 			int version = Integer.valueOf(requestMap.get("version"));
 			String type = requestMap.get("type");
 			if (type.equals("insertion")) {
 				String change = requestMap.get("change");
 				return changeDoc(userName, docName, position, change, length, version);
 				
 			} else if (type.equals("deletion")) {
 				return changeDoc(userName, docName, position, length, version);
 			}
 			return "Invalid request";
 			
 		case EXITDOC:
 			//exits the document and returns the user to the document table screen
 			userName = requestMap.get("userName");
 			docName = requestMap.get("docName");
 			return exitDoc(userName, docName);
 			
 		case LOGOUT:
 			//logs the user out and returns them to the login page
 			userName = requestMap.get("userName");
 			return logOut(userName, String.valueOf(ID));
 			
 		case CORRECT_ERROR:
 			userName = requestMap.get("userName");
 			docName = requestMap.get("docName");
 			return correctError(userName, docName);
 			
 		default:
 			return "Invalid request";
     	}
 
 	}
     
     /**
      * Makes a change to the document, as per the instructions of the client
      * @param input which specifies the change as a client --> server message
      * @return message to the clients about the document changed
      */
     private synchronized String changeDoc(String userName, String docName, int position, String change, int length, int version) {
     	Document currentDocument = getDoc(docName);
 		Color actualColor = userColorMappings.get(userName);
 		
 		int colorRed = actualColor.getRed();
 		int colorBlue = actualColor.getBlue();
 		int colorGreen = actualColor.getGreen();
 		
 		String color = String.valueOf(colorRed) + "," + String.valueOf(colorGreen) + "," + String.valueOf(colorBlue);
 		
 		//update the model of the data
 		//a tab character represents a newline so that socket input is not broken over multiple lines
 		//the user is not able to enter tabs so we don't have to worry about how to represent tabs
 		if (change.equals("\t")) {
 			currentDocument.insertContent("\n", position, version);
 		} else {
 			currentDocument.insertContent(change, position, version);
 		}
 		currentDocument.setLastEditDateTime();
 		
 		//version updating is handled by the insertion/deletion of content
 		int versionNumber = currentDocument.getVersion();
 		
 		if (position != -1 && length != -1) {
 			return "changed&type=insertion&userName=" + userName + "&docName=" + docName + "&change=" + change + "&" +
 					"position=" + position + "&length=" + length + "&version=" + versionNumber + "&color=" + color + "&";
 		}
 		
 		return "Invalid request";
     }
     
     /**
      * Makes a change to the document, as per the instructions of the client
      * @param input which specifies the change as a client --> server message
      * @return message to the clients about the document changed
      */
     private synchronized String changeDoc(String userName, String docName, int position, int length, int version) {		
     	Document currentDocument = getDoc(docName);
     	
 		//update the model of the data
 		currentDocument.deleteContent(position, length, version);
 		
 		currentDocument.setLastEditDateTime();
 		
 		//version updating is handled by the insertion/deletion of content
 		int versionNumber = currentDocument.getVersion();
 		
 		if (position != -1 && length != -1) {
 			return "changed&type=deletion&userName=" + userName + "&docName=" + docName + "&position=" + position + "&" +
 					"length=" + length + "&version=" + versionNumber + "&";
 		}
 		
 		return "Invalid request";
     }
 
     
     /**
      * Logs the user in if they have a unique username
      * @param userName Username of the user who logs into the system
      * @param ID of the client connection
      * @return the response which encodes whether or not the login was successful
      */
     String logIn(String userName, int ID) {
 		//if the username already is logged in
     	if (onlineUsers.contains(userName)) {
 			return "notloggedin";
 		} 
 		
     	//otherwise, the user has a unique name
		else {				
 			onlineUsers.add(userName);
 			
 			//if user does not already have a color mapping
 			if (!userColorMappings.containsKey(userName)){
 				//choose a new colors
 				int numUsers = onlineUsers.size() % NUM_COLORS;
 				Color color = COLORS[numUsers];
 				
 				//assign that color
 				userColorMappings.put(userName, color);
 				System.out.println(userName + "-->" + color.toString());
 			}	
 			socketUserMappings.put(ID, userName);
 			
 			//this returns information about the user logged in 
 			//it then returns a list of documents and their corresponding names, dates, and collaborators
 			StringBuilder stringBuilder = new StringBuilder("loggedin&userName=" + userName + "&id=" + ID + "&");
 			stringBuilder.append("\n");
 			
 			stringBuilder.append(getDocumentInfo());
 			
 			return stringBuilder.toString();
 
 		}
     }
 
     /**
      * Gets the document info for doctable
      * @return
      */
     private String getDocumentInfo(){
     	StringBuilder stringBuilder = new StringBuilder();
 		for (Document document : currentDocuments){
 			stringBuilder.append("docinfo&");
 			stringBuilder.append("docName=");
 			stringBuilder.append(document.getName());
 			stringBuilder.append("&date=");
 			stringBuilder.append(document.getDate());
 			stringBuilder.append("&collab=");
 			stringBuilder.append(document.getCollab());
 			stringBuilder.append("&\n");
 		}
 		stringBuilder.append("enddocinfo");
 		return stringBuilder.toString();
     }
     
     /**
      * Logs the user out
      * @param userName The name of the user to be logged out
      * @return 
      */
     String logOut(String userName, String ID) {
 		onlineUsers.remove(userName);
 		socketUserMappings.remove(ID);
 		return "loggedout&userName=" + userName + "&";
     }
     
     /**
      * Creates a new document
      * @param userName The name of the user that creates the new document
      * @param docName The name of the newly created document
      * @return Response from the server to the client
      */
     synchronized String newDoc(String userName, String docName) {
     	for (Document doc : currentDocuments){
     		if(docName.equals(doc.getName())){
     			return "notcreatedduplicate";
     		}
     	}
     	Document newDoc = new Document(docName, userName);
 		currentDocuments.add(newDoc);
 		String date = newDoc.getDate();
 		
 		//TODO add color to this so when you open a doc you can see your color
 		String response =  "created&userName=" + userName + "&docName=" + docName + "&date=" + date + "&"; 
 		return response;
     }
     
     /**
      * Opens an existing document
      * @param userName The name of the user that opens the document
      * @param docName The name of the document that is being opened
      * @return Response from the server to the clients; all GUIs are updated
      */
     private String openDoc(String userName, String docName) {
 		Document currentDocument = getDoc(docName);
 		currentDocument.addCollaborator(userName);
 		String docContent = currentDocument.toString();
 		docContent = docContent.replace("\n", "\t");
 		String collaborators = currentDocument.getCollab();
 		int version = currentDocument.getVersion();
 		
 		String colors = "";
 		String color;
 		for (String username : currentDocument.getCollabList()){
 			Color actualColor = userColorMappings.get(username);
 			
 			int colorRed = actualColor.getRed();
 			int colorBlue = actualColor.getBlue();
 			int colorGreen = actualColor.getGreen();
 			
 			color = String.valueOf(colorRed) + "," + String.valueOf(colorGreen) + "," + String.valueOf(colorBlue) + " ";
 
 			colors += color;
 		}
 		for (String user : userColorMappings.keySet()){
 			System.out.println(user + "--->" + userColorMappings.get(user).toString());
 		}
 		//updates collaborators than opens the document
 		return "update&docName=" + docName + "&collaborators=" + collaborators + "&colors=" + colors + "&\n" +
 				"opened&userName=" + userName + "&docName=" + docName + "&docContent=" + docContent + 
 				"&collaborators=" + collaborators + "&version=" + version + "&colors=" + colors + "&"; 		
     }
     
     private String correctError(String userName, String docName) {
     	Document currentDocument = getDoc(docName);
     	String content = currentDocument.toString();
     	content = content.replace("\n", "\t");
     	return "corrected&userName=" + userName + "&docName=" + docName + "&content=" + content + "&";
     }
     
     /**
      * Exits the document
      * @param userName The name of the user that is exiting the document
      * @param docName The name of the document that is being exited
      * @return Response from the server to the client
      */
     private String exitDoc(String userName, String docName) {
 		StringBuilder stringBuilder = new StringBuilder("exiteddoc&userName=" + userName + "&docName=" + docName + "&");
 		stringBuilder.append("\n");
 		
 		stringBuilder.append(getDocumentInfo());
 						
 		//returns exitdoc response then the information about the document list for the document table
 		return stringBuilder.toString();
     }
 	
 	/**
 	 * Adds a new Document to the list of currently active documents
 	 * @param newDocument The document object of the new document being added to the collection
 	 */
 	public void addDocument(Document newDocument) {
 		currentDocuments.add(newDocument);
 	}
 	
 	/**
 	 * Returns the list of documents that are stored in the server
 	 * @return A List of document objects that are stored in the server
 	 */
 	public List<Document> getDocuments() {
 		return currentDocuments;
 	}
 	
 	/**
 	 * Returns the document object corresponding to the given document name
 	 * @param docName Name of the document object to be retrieved
 	 * @return An object of type Document
 	 */
 	private Document getDoc(String docName) {
 		for (Document document: currentDocuments) {
 			String name = document.getName();
 			if (docName.equals(name)) {
 				return document;
 			}
 		}
 		throw new RuntimeException("Document not found");
 	}
 	
 	/**
 	 * Starts the etherpad server
 	 * This server stores documents and can be accessed by clients running the controller. 
 	 * @param args Unused
 	 */
 	public static void main(String[] args) {
 		final Server serverInstance;
 		try {
 			if(args.length == 1){
 				serverInstance = new Server(Integer.parseInt(args[0]));
 				System.out.println("Started the server on port " + args[0]);
 			}
 			else
 				serverInstance = new Server();
 			
 			//Serving thread handles new connections made to the server
 			Thread servingThread = new Thread(new Runnable() {
 				@Override
 				public void run() {
 					try {
 						System.out.println("Listening for requests");
 						serverInstance.serve();
 					} catch (IOException e) {
 						ErrorMessage error = new ErrorMessage("Setting up server", "Error setting up the server. Check your IP.");
 						error.setVisible(true);
 						return;
 					}
 				}
 			});
 			
 			//Attending thread attends to the different requests made by the clients
 			Thread attendingThread = new Thread(new Runnable() {
 				@Override
 				public void run() {
 					try {
 						System.out.println("Attending requests");
 						serverInstance.attendRequest();
 					} catch (InterruptedException e) {
 						ErrorMessage error = new ErrorMessage("Attending requests", "Error attending requests");
 						error.setVisible(true);
 						return;
 					}
 				}
 			});
 			
 			servingThread.start();
 			attendingThread.start();
 			
 		} catch (IOException e1) {
 			e1.printStackTrace();
 			ErrorMessage error = new ErrorMessage("Setting up server", "Error setting up the server.\n Check your IP.");
 			error.setVisible(true);
 			return;
 
 		}
 	}
 }
