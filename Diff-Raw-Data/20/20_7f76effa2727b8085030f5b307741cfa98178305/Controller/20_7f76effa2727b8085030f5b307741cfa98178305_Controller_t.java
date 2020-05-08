 package view;
 
 import java.awt.Color;
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.PrintWriter;
 import java.net.Socket;
 import java.net.UnknownHostException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.LinkedBlockingQueue;
 
 import view.ControllerRequest.Type;
 
 /**
  * This is the controller class. It makes requests from the server
  * and receives responses. It's requests are based off user interaction
  * in the GUI and with the responses it receives it modifies the view. 
  * It's request prompt the model to change.
  * 
  * Rep Invariants:
  * 		Only one GUI is visible at a time
  */
 public class Controller {
 	private Login loginGUI;
 	private DocTable docTableGUI = null;
 	
 	private Socket serverSocket;
 	
 	private BufferedReader serverInput;
 	private PrintWriter serverOutput;
 	
 	private String userName;
 	
 	//ID of the client which is given by the server
 	private int ID;
 	
 	private DocEdit currentDoc = null;
 	
 	//this thread contains the requests from the client to the server
 	private LinkedBlockingQueue<ControllerRequest> queue;
 
 	/**
 	 * This is the constructor for the controller. A client runs an instance of controller
 	 * in order to connect to the server. At this point we assume that the client and
 	 * server are on the same machine and that the port is 4444. 
 	 * @throws UnknownHostException
 	 * @throws IOException
 	 */
 	public Controller() throws UnknownHostException, IOException {
 		this(4444);
 	}
 	
 	/**
 	 * This is pretty much the same as the constructor above except that we are specifying a port. 
 	 * If the port is not valid, the constructor will throw an exception.  
 	 * @param port the user specifies 
 	 * @throws UnknownHostException
 	 * @throws IOException
 	 */
 	public Controller(int port) throws UnknownHostException, IOException {
 		this("127.0.0.1", port);
 	}
 
 	/**
 	 * This is pretty much the same as the two above except that you are also 
 	 * specifying an IP address in addition to a port. 
 	 * @param IP
 	 * @param port
 	 * @throws UnknownHostException
 	 * @throws IOException
 	 */
 	public Controller(String IP, int port) throws UnknownHostException, IOException {
 		this.serverSocket = new Socket(IP, port);
 		
 		//set the input and output streams
 		this.serverInput = new BufferedReader(new InputStreamReader(serverSocket.getInputStream()));
 		this.serverOutput = new PrintWriter(serverSocket.getOutputStream(), true);
 
 		this.loginGUI = new Login(serverOutput);
 		this.queue = new LinkedBlockingQueue<ControllerRequest>();
 	}
 	
 	/**
 	 * Runs the login GUI; makes all other GUI elements invisible, if they already exist.
 	 * The user sees the login page and can send requests to the server by interacting
 	 * with objects on that page. The server sends responses to the client and if the
 	 * client gets the appropriate response, the GUI will log the user in and pass
 	 * them on to the document table GUI. 
 	 * 
 	 * The GUI will progress to the document table page after the server sends both an ID
 	 * and a logged in message for a username. When the user logs in, a new thread is fired to 
 	 * handle the document table GUI. 
 	 * @throws InterruptedException throws an exception if taking from the queue is interrupted
 	 */
 	private void runLogin()  {
 		//make loginGUI the only thing that is visible, maintain the rep invariant
 		loginGUI.setVisible(true);
 		loginGUI.resetMessage();
 		if (docTableGUI != null) {
 			docTableGUI.setVisible(false);
 		}
 		
 		while(true){
 			ControllerRequest request;
 			try {
 				request = queue.take();
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 				ErrorMessage error = new ErrorMessage("Error handling messages", "Unable to pull things from queue in controller.");
 				error.setVisible(true);
 				return;
 			}
 			Map<String, String> requestMap = request.getMap();
 			Type type = request.getType();
 			switch(type){
 			case ID:
 				this.ID = Integer.valueOf(requestMap.get("id"));
 				break;
 			case LOGGEDIN:
 				userName = requestMap.get("userName");
 				int docID = Integer.valueOf(requestMap.get("id"));
 				if (docID == this.ID) {
 					docTableGUI = new DocTable(serverOutput, userName);
 					updateDocTable();
 					//fire off a new thread to handle the doctable
 					Thread newThread = new Thread(new Runnable() {
 						@Override
 						public void run() {
 							runDocTable();
 						}
 					});
 					newThread.start();
 					return;
 				}
 				break;
 			case NOTLOGGEDIN:
 				loginGUI.failedLogin();
 				break;
 			default:
 				System.out.println(request.getLine());
 				continue;
 			}
 		}
 	}
 	
 	/**
 	 * Updates the information stored in the document table. We run this 
 	 * when switching between different GUIs. We will not get information about updates
 	 * in the table when we are just staying in the docTable GUI. 
 	 * 
 	 * This method accesses the server to get a list of documents. With this list it updates
 	 * the data in the tableModel and refreshes the display. The method assumes that the 
 	 * next output from the server will be information about a list of documents. 
 	 */
 	private void updateDocTable() {
 		List<String[]> documentInfo = new ArrayList<String[]>();
 		while(true){
 			ControllerRequest request;
 			try {
 				request = queue.take();
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 				ErrorMessage error = new ErrorMessage("Error handling messages", "Unable to pull things from queue in controller.");
 				error.setVisible(true);
 				return;
 			}
 			Map<String, String> requestMap = request.getMap();
 			switch(request.getType()){
 			case ENDDOCINFO:				
 				docTableGUI.updateTable(documentInfo);
 				return;
 			case DOCINFO:
 				// Parses data containing information contained in the table; and then adds it to the document table
 				String docName = requestMap.get("docName");
 				String docDate = requestMap.get("date");
 				String docCollab = requestMap.get("collab");
 				documentInfo.add(new String[]{docName, docDate, docCollab});
 				break;
 			default:
 				continue;
 			}
 		}
 	}
 
 	/**
 	 * Runs the document table. Makes the document table GUI visible. All other GUI elements are made invisible.
 	 * This GUI can be accessed from both the docEdit and the login GUIs.
 	 * 
 	 * The GUI continuously reads from the server to detect any changes. The user can also send requests to the server
 	 * by interacting with elements on the page. It will fire off a new thread to run a different GUI if the user either
 	 * logs out or switches to a document. 
 	 */
 	private void runDocTable() {
 		//preserve the rep invariant that only one GUI is visible at a time
 		loginGUI.setVisible(false);
 		if (currentDoc != null)
 			currentDoc.setVisible(false);
 		docTableGUI.setVisible(true);
 		
 		while(true){
 			ControllerRequest request;
 			try {
 				request = queue.take();
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 				ErrorMessage error = new ErrorMessage("Error handling messages", "Unable to pull things from queue in controller.");
 				error.setVisible(true);
 				return;
 			}
 			Map<String, String> requestMap = request.getMap();
 			
 			String userName;
 			String docName;
 			String date;
 			int version;
 			System.out.println(request.getLine());
 			switch(request.getType()){
 			case CREATED:
 				docName = requestMap.get("docName");
 				userName = requestMap.get("userName");
 				date = requestMap.get("date");
 				version = 0;
 				
 				String[] dataDoc = new String [3];
 				dataDoc[0] = docName;
 				dataDoc[1] = date;
 				dataDoc[2] = userName;
 				docTableGUI.addData(dataDoc);
 				
 				if(this.userName.equals(userName)){
 					this.currentDoc = new DocEdit(serverOutput, docName, userName, "", userName, version, "");							
 					Thread newThread = new Thread(new Runnable() {
 						@Override
 						public void run() {
 							runDocEdit();
 						}
 					});
 					newThread.start();
 					return;
 				}
 				break;
 			case NOTCREATED:
 				docTableGUI.setDuplicateErrorMessage();
 				break;
 			case LOGGEDOUT:
 				String name = requestMap.get("userName");
 				if (this.userName.equals(name)) {
 					Thread loginThread = new Thread(new Runnable() {
 						@Override
 						public void run() {
 							runLogin();
 						}
 					});
 					loginThread.start();
 					return;
 				}
 				break;
 			case OPENED:
 				userName = requestMap.get("userName");
 				docName = requestMap.get("docName");
 				String docContent = requestMap.get("docContent");
 				String collaborators = requestMap.get("collaborators");
 				version = Integer.valueOf(requestMap.get("version"));
 				String colors = requestMap.get("colors");
 				docContent = docContent.replace("\t", "\n");
 				if(this.userName.equals(userName)){
 					this.currentDoc = new DocEdit(serverOutput, docName, userName, docContent, collaborators, version, colors);							
 					Thread newThread = new Thread(new Runnable() {
 						@Override
 						public void run() {
 							runDocEdit();
 						}
 					});
 					newThread.start();
 					return;
 				}
 				break;
 			default:
 				continue;
 			}
 		}
 	}
 	
 	/**
 	 * Runs the document editor. All other GUI elements are made invisible.
 	 * This GUI can be accessed from only the docEdit GUI.
 	 * 
 	 * The GUI continuously reads from the server to detect any changes. The user can also send requests to the server
 	 * by interacting with elements on the page. It will fire off a new thread for the docTable if the user exits the document . 
 	 */
 	private void runDocEdit() {
 		//this preserves the rep invariant that only one GUI element is seen
 		docTableGUI.setVisible(false);
 		currentDoc.setVisible(true);
 		
 		while(true){
 			ControllerRequest request;
 			try {
 				request = queue.take();
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 				ErrorMessage error = new ErrorMessage("Error handling messages", "Unable to pull things from queue in controller.");
 				error.setVisible(true);
 				return;
 			}
 			Map<String, String> requestMap = request.getMap();
 			
 			String userName;
 			String docName;
 			int version;
 			switch(request.getType()){
 			case EXITEDDOC:
 				userName = requestMap.get("userName");
 				updateDocTable();
 				if(this.userName.equals(userName)){							
 					Thread newThread = new Thread(new Runnable() {
 						@Override
 						public void run() {
 							runDocTable();
 						}
 					});
 					newThread.start();
 					return;
 				}
 				break;
 			case CORRECTED:
				String newContent = requestMap.get("content");
 				userName = requestMap.get("userName");
 				docName = requestMap.get("docName");
 				newContent = newContent.replace("\t", "\n");
 				if (this.userName.equals(userName)) {
 					if (currentDoc.getName().equals(docName)) {
 						currentDoc.resetText(newContent);
 					}
 				}
 				break;
 			case CHANGED:
 				String type = requestMap.get("type");
 				int position = Integer.valueOf(requestMap.get("position"));
 				userName = requestMap.get("userName");
 				docName = requestMap.get("docName");
 				version = Integer.valueOf(requestMap.get("version"));
 				if(type.equals("insertion")){						
 					String change = requestMap.get("change");
 					String[] colors = requestMap.get("color").split(",");
 					Color color = new Color(Integer.parseInt(colors[0]), Integer.parseInt(colors[1]), Integer.parseInt(colors[2]));
 					change = change.replace("\t", "\n");
 					if (currentDoc.getName().equals(docName)) {
 						if (! this.userName.equals(userName)) {
 							currentDoc.insertContent(change, position, version, color);
 						}
 					}
 				}
 				else if(type.equals("deletion")){
 					int length = Integer.valueOf(requestMap.get("length"));
 					if (currentDoc.getName().equals(docName)) {
 						if (! this.userName.equals(userName)) {
 							currentDoc.deleteContent(position, length, version);
 						}
 					}						
 				}
 				break;
 			case UPDATE:
 				String collaborators = requestMap.get("collaborators");
 				String colors = requestMap.get("colors");
 				docName = requestMap.get("docName");
 				if (currentDoc.getName().equals(docName)) {
 					currentDoc.updateCollaborators(collaborators, colors);
 				}
 				break;
 			default:
 				continue;
 			}
 		}
 	}
 	
 	private void manageQueue() {		
 		try {
 			for (String line = serverInput.readLine(); line!=null; line=serverInput.readLine()) {
 				ControllerRequest request = new ControllerRequest(line);
 				queue.add(request);
 			}
 		}
 		catch(Exception e){
 			e.printStackTrace();
 		}
 	}
 	
 	/**
 	 * This method should be run by clients to connect to the server. It uses the default constructor
 	 * and assumes that you are connecting on to a server which is on the same machine over port 4444. 
 	 * default IP is 127.0.0.1
 	 * default port is 4444
 	 * 
 	 * There are three different commandline options
 	 * 1. no arguments
 	 * 2. [port]
 	 * 3. [IP address] [port]
 	 * @param args are specified above, there are three different argument combos, they specify IP and port
 	 */
 	public static void main(final String[] args) {
 		final Controller main;
 		System.out.println("Beginning cleint...");
 		try {
 			if (args.length == 0){
 				main = new Controller();				
 			}
 			else if(args.length == 1){
 				int port = Integer.parseInt(args[0]);
 				main = new Controller(port);
 			}
 			else if(args.length == 2){
 				int port = Integer.parseInt(args[1]);
 				String IP = args[0];
 				main = new Controller(IP, port);
 			}
 			else{
 				ErrorMessage error = new ErrorMessage("Invalid Controller Arguments", "Please revise your arguments to the controller.");
 				error.setVisible(true);
 				return;
 			}
 		} catch (IOException e) {
 			ErrorMessage error = new ErrorMessage("Server not set up", "Set up a server before running the client");
 			error.setVisible(true);
 			return;
 		} 
 		
 		Thread mainThread = new Thread(new Runnable() {
 			@Override
 			public void run() {
 				System.out.println("Taking requests from queue");
 				main.runLogin();
 			}
 		});
 		
 		Thread queueThread = new Thread(new Runnable() {
 			@Override
 			public void run() {
 				main.manageQueue();
 			}
 		});
 		
 		mainThread.start();
 		queueThread.start();
 	}
 
 }
