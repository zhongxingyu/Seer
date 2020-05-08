 import java.rmi.Naming;
 import java.rmi.RemoteException;
 import java.rmi.server.UnicastRemoteObject;
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import com.sun.org.apache.xml.internal.serializer.utils.Messages;
 
 
 public class ClientGUI implements ClientInterface {
 
 	private static ClientGUI mInstance;
 	private HashMap<String, MessageWindow> mMessageWindows;
 	private String mUserName, mUserPass;
 	private ServerInterface mServerInt;
 	private UserListWindow mListWindow;
 	private LoginWindow mLoginWindow;
 
 	/**
 	 * initialize components
 	 */
 	public ClientGUI() {
 		try {
 			UnicastRemoteObject.exportObject(this, 0);
 			mInstance = this;
 		} catch(RemoteException ex) {
 			System.out.println(ex.getMessage());
 		}
 		mUserName = "";
 		mUserPass = "";
 		mMessageWindows = new HashMap<String, MessageWindow>();
 		mListWindow = new UserListWindow(this);
 	}
 	
 	
 
 	public void setUser(String user) {
 		mUserName = user;
 	}
 	public String getUser() {
 		return mUserName;
 	}
 	public void setPass(String pass) {
 		mUserPass = pass;
 	}
 	public String getPass() {
 		return mUserPass;
 	}
 	
 	
 	
 	
 	/**
 	 * Delivers a message to the User, sent by another User and presents it in a MessageWindow
 	 * @param sender The sender of the Message
 	 * @param message The text of the Message
 	 */
 	@Override
 	public void notifyMessage(String sender, String message) throws RemoteException {
 		if(mMessageWindows.get(sender) == null) {
 			MessageWindow window = new MessageWindow(sender, this);
 			window.setVisible(true);
 			window.addMessage(message);
 			mMessageWindows.put(sender, window);
 		}
 		else {
 			mMessageWindows.get(sender).setVisible(true);
 			mMessageWindows.get(sender).addMessage(message);
 		}
 	}
 	
 	
 	
 	public boolean newBuddy(String name) {
 		boolean retVal = false;
 		// TODO hier gehts weiter
 		try {
 			retVal = mServerInt.addBuddy(mUserName, name, ServerInterface.CLIENT);
 		} catch(Exception ex){
 			if(connect())
 				return newBuddy(name);
 			System.out.println("An Error occured in addBuddy: " + ex.getMessage());
 			return false;
 		}
 		return true;
 	}
 	
 	/**
 	 * updates the list of online Users
 	 * @param users String-Array of usernames 
 	 */
 	@Override
 	public void updateUserList(String[] users) throws RemoteException {
 		mListWindow.updateList(users);
 	}
 	
 	
 	/**
 	 * displays the login-dialog
 	 */
 	public void showLogin() {
 		mLoginWindow = new LoginWindow(this);
 		mLoginWindow.setVisible(true);
 	}
 	
 	/**
 	 *try to connect to one of the server. 3 retries for each.
 	 *@return Returns true if the connection was successful, else the returnvalue is false
 	 */
 	public boolean connect() {
 		for(int i = 0; i < 3; i++) {
 			try {
 				mServerInt = (ServerInterface) Naming.lookup("rmi://127.0.0.1:9090/server1");
 				mServerInt.ping();
 				System.out.println("Connected to Server 1");
 				return true;
 			} catch(Exception ex) {
 				System.out.println("Server 1 is not responding. \nRetrying to connect ...");
 			}
 			try {
 				Thread.sleep(2000);
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 			}
 		}
 		for(int i = 0; i < 3; i++) {
 			try {
 				mServerInt = (ServerInterface) Naming.lookup("rmi://127.0.0.2:9090/server2");
 				mServerInt.ping();
 				System.out.println("Connected to Server 2");
 				return true;
 			} catch(Exception ex) {
 				System.out.println("Server 2 is not responding. \nRetrying to connect ...");
 			}		
 			try {
 				Thread.sleep(2000);
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 			}
 		}
 		return false;
 	}
 	
 	/**
 	 * tries to login on the server
 	 * @return Returns true if login was successful, otherwise the returnvalue is false
 	 */
 	public boolean login() {
 		boolean loggedIn = false;
 		try {
 			loggedIn = mServerInt.login(mUserName, mUserPass, mInstance, ServerInterface.CLIENT);
 		} catch(Exception ex){
 			if(connect())
 				return login();
 			System.out.println("A Login-Error occured: " + ex.getMessage());
 			return false;
 		}
 		
 		if(loggedIn) {
 			mListWindow.setTitle(mUserName);
 			mListWindow.setVisible(true);
 			HashMap<String, ArrayList<String>> messages = new HashMap<String, ArrayList<String>>();
 			try {
 				 messages = mServerInt.getMessages(mUserName);
 			} catch(Exception ex) {
 				System.out.println("Error while receiving messages: " + ex.getMessage());
 			}
 			
 			for(String sender : messages.keySet()) {
 				if(sender == null)
 					break;
 				
 				for(String message : messages.get(sender)) {
 					try {
 						notifyMessage(sender, message);
 						mServerInt.deleteMessage(sender, mUserName, message, ServerInterface.CLIENT);
 					} catch (RemoteException e) {
 						e.printStackTrace();
 					}
 				}
 			}
 		}
 		return loggedIn;
 	}
 	
 	
 	public boolean register() {
 		boolean registered = false;
 		
 		try {
 			registered = mServerInt.newUser(mUserName, mUserPass, ServerInterface.CLIENT);
 		} catch(Exception ex) {
 			if(connect())
 				return register();
 			System.out.println("A Login-Error occured: " + ex.getMessage());
 			return false;
 		}
 		return registered;
 	}
 	
 	
 	/**
 	 * action performed when the user explicitly logs out and doesnt want to exit the application.
 	 * the login-dialog is shown again.
 	 */
 	public void onLogout() {
 		try {
 			logout();
 		} catch (RemoteException e) {
 			if(connect()) {
 				exit();
 				return;
 			}
 			System.out.println("Error on logout");
 			System.out.println(e.getMessage());
 		}
 		clearConversations();
 		showLogin();
 	}
 	
 	
 	/**
 	 * deletes all existing conversations
 	 */
 	public void clearConversations() {
 		for(String user : mMessageWindows.keySet()) {
 			mMessageWindows.get(user).setVisible(false);
 		}
		mMessageWindows.clear();
 	}
 	
 	
 	/**
 	 * unregister the user
 	 */
 	public void logout() throws RemoteException {
 		mServerInt.logout(mUserName, ServerInterface.CLIENT);
 	}
 	
 	/**
 	 * Close the Connection and unregister the User
 	 */
 	public void exit() {
 		try {
 			UnicastRemoteObject.unexportObject(mInstance, true);
 			logout();
 		} catch(Exception ex) {
 			if(connect()) {
 				exit();
 				return;
 			}
 			System.out.println("Error on exit");
 			System.out.println(ex.getMessage());
 		}
 	}
 	
 	
 	/**
 	 * Send a message
 	 * @param receiver the receiver of the message
 	 * @param message A String that represents the Message
 	 */
 	public void sendMessage(String receiver, String message) {
 		
 		try {
 			mServerInt.sendMessage(mUserName, receiver, message, ServerInterface.CLIENT);
 		} catch (RemoteException e) {
 			if(connect()) {
 				sendMessage(receiver, message);
 				return;
 			}
 			System.out.println("Error while sending message");
 		}
 	}
 	
 	
 	/**
 	 * initiates a new conversation with a user
 	 * @param receiver the user for the conversation
 	 */
 	public void newConversation(String receiver) {
 		if(mMessageWindows.get(receiver) == null) {
 			MessageWindow window = new MessageWindow(receiver, this);
 			window.setVisible(true);
 			mMessageWindows.put(receiver, window);
 		}
 		else
 			mMessageWindows.get(receiver).setVisible(true);
 	}
 
 
 	public static void main(String[] args) {
 
 		ClientGUI client = new ClientGUI();
 		client.connect();
 		client.showLogin();
 		
 	}
 }
