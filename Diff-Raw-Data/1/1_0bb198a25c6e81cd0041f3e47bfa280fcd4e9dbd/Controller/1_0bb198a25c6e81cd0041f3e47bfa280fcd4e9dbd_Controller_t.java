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
 	 */
 	private void runLogin() {
 		//make loginGUI the only thing that is visible, maintain the rep invariant
 		loginGUI.setVisible(true);
 		loginGUI.resetMessage();
 		if (docTableGUI != null) {
 			docTableGUI.setVisible(false);
 		}
 		
 		try {
 			for (String line = serverInput.readLine(); line!=null; line=serverInput.readLine()) {
 				//set the ID of the client
 				if (line.startsWith("id:")) {
 					String[] lineSplit = line.split(" ");
 					this.ID = Integer.valueOf(lineSplit[1]);
 				} 
 				//the user should log in 
 				else if (line.startsWith("loggedin")) {
 					String[] lineSplit = line.split(" ");
 					userName = lineSplit[1];
 					int ID = Integer.valueOf(lineSplit[2]);
 					
 					if (ID == this.ID) {
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
 				} 
 				//the user gets rejected 
 				else if (line.startsWith("notloggedin")){
 					loginGUI.failedLogin();
 				} 
 	        }
 		} catch (IOException e) {
 			throw new RuntimeException("IO Exception encountered");
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
 		try{
 			List<String[]> documentInfo = new ArrayList<String[]>();
 			for (String line = serverInput.readLine(); line!= null; line=serverInput.readLine()) {
 				if (line.startsWith("enddocinfo")){
 					docTableGUI.updateTable(documentInfo);
 					return ;
 				} else{
 					String[] lineSplit = line.split("\t");
 					String docName = lineSplit[0];
 					String docDate = lineSplit[1];
 					String docCollab = lineSplit[2];
 					documentInfo.add(new String[]{docName, docDate, docCollab});
 				}
 			}
 		} catch(IOException e){
 			throw new RuntimeException();
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
 		
 		try {
 			for (String line = serverInput.readLine(); line!=null; line=serverInput.readLine()) {
 				//if the user has created a new document, open that document
 				if (line.startsWith("created")) {
 					String[] lineSplit = line.split("\\|");
 					if (lineSplit.length == 5){
 						
 						String userName = lineSplit[1];
 						String docName = lineSplit[2];
 						String collaborators = lineSplit[3];
 						String date = lineSplit[4];
 						int version = 0;
 
 						String[] dataDoc = new String [3];
 						dataDoc[0] = docName;
 						dataDoc[1] = date;
 						dataDoc[2] = userName;
 						docTableGUI.addData(dataDoc);
 						
 						if(this.userName.equals(userName)){
 							this.currentDoc = new DocEdit(serverOutput, docName, userName, "", collaborators, version, "");							
 							Thread newThread = new Thread(new Runnable() {
 								@Override
 								public void run() {
 									runDocEdit();
 								}
 							});
 							newThread.start();
 							return;
 						}
 					}else{
 						throw new RuntimeException("Invalid format");
 					}					
 				} 
 				else if(line.startsWith("notcreatedduplicate")){
 					docTableGUI.setDuplicateErrorMessage();
 				}
 				
 				else if (line.startsWith("opened")) {
 					String[] lineSplit = line.split("\\|");
 					if (lineSplit.length == 7){
 						String userName = lineSplit[1];
 						String docName = lineSplit[2];
 						String docContent = lineSplit[3];
 						String collaborators = lineSplit[4];
 						int version = Integer.valueOf(lineSplit[5]);
 						String colors = lineSplit[6];
 						
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
 					}else{
 						throw new RuntimeException("Invalid format");
 					}					
 				} else if (line.startsWith("loggedout")) {
 					String[] lineSplit = line.split(" ");
 					if (lineSplit.length == 2) {
 						String userName = lineSplit[1];
 						if (this.userName.equals(userName)) {
 							Thread newThread = new Thread(new Runnable() {
 								@Override
 								public void run() {
 									runLogin();
 								}
 							});
 							newThread.start();
 							return;
 						}
 					}
 				}
 			}
 		} catch (IOException e) {
 			throw new RuntimeException("IO Exception encountered");
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
 		
 		try {
 			for (String line = serverInput.readLine(); line!=null; line=serverInput.readLine()) {
 				//if the user exits the document, fire a thread to run the docedit
 				if (line.startsWith("exiteddoc ")) {
 					String[] lineSplit = line.split(" ");
 					if (lineSplit.length == 3){
 						String userName = lineSplit[1];
 						updateDocTable();
 						if(this.userName.equals(userName)){							
 							Thread newThread = new Thread(new Runnable() {
 								@Override
 								public void run() {
 									runDocTable();
 								}
 							});
 							newThread.start();
 							return ;
 						}
 					}else{
 						throw new RuntimeException("Invalid format");
 					}					
 				} 
 				else if (line.startsWith("corrected")) {
 					String[] lineSplit = line.split("\\|");
 					if (lineSplit.length == 4) {
 						String userName = lineSplit[1];
 						String docName = lineSplit[2];
 						String newContent = lineSplit[3];
						newContent = newContent.replace("\t", "\n");
 						if (this.userName.equals(userName)) {
 							if (currentDoc.getName().equals(docName)) {
 								currentDoc.resetText(newContent);
 							}
 						}
 					}
 				}
 				//if the content of the document is changed, update the view for the user
 				else if (line.startsWith("changed")) {
 					String[] lineSplit = line.split("\\|");
 					if (lineSplit.length == 8) {
 						String userName = lineSplit[1];
 						String docName = lineSplit[2];
 						String change = lineSplit[3];
 						int position = Integer.valueOf(lineSplit[4]);
 						String[] colors = lineSplit[7].split(",");
 						Color color = new Color(Integer.parseInt(colors[0]), Integer.parseInt(colors[1]), Integer.parseInt(colors[2]));
 						int version = Integer.valueOf(lineSplit[6]);
 						change = change.replace("\t", "\n");
 						if (currentDoc.getName().equals(docName)) {
 							if (! this.userName.equals(userName)) {
 								currentDoc.insertContent(change, position, version, color);
 							}
 						}
 					} else if (lineSplit.length == 6) {
 						String userName = lineSplit[1];
 						String docName = lineSplit[2];
 						int position = Integer.valueOf(lineSplit[3]);
 						int length = Integer.valueOf(lineSplit[4]);
 						int version = Integer.valueOf(lineSplit[5]);						
 						if (currentDoc.getName().equals(docName)) {
 							if (! this.userName.equals(userName)) {
 								currentDoc.deleteContent(position,length, version);
 							}
 						}
 					}
 				} 
 				//if the list of collaborators is changed, update the list for the user
 				else if (line.startsWith("update")) {
 					String[] lineSplit = line.split("\\|");
 					if (lineSplit.length == 4) {
 						String docName = lineSplit[1];
 						String collaboratorNames = lineSplit[2];
 						String colors = lineSplit[3];
 						System.out.println(colors);
 						if (currentDoc.getName().equals(docName)) {
 							currentDoc.updateCollaborators(collaboratorNames, colors);
 						}
 					}
 					else{
 						System.out.println("Invalid format");
 					}
 				}
 			}
 		} catch (IOException e) {
 			throw new RuntimeException("IO Exception encountered");
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
 		
 		Thread newThread = new Thread(new Runnable() {
 			@Override
 			public void run() {
 				main.runLogin();
 			}
 		});
 		newThread.start();
 	}
 }
