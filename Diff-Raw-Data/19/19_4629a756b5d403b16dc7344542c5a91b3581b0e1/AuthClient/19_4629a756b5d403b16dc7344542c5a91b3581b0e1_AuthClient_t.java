 /**
  * 
  */
 package blackdoor.auth;
 
 import java.io.BufferedInputStream;
 import java.io.BufferedOutputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.ObjectInput;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutput;
 import java.io.ObjectOutputStream;
 import java.io.OutputStream;
 import java.net.Socket;
 import java.net.SocketException;
 import java.net.UnknownHostException;
 
 import blackdoor.auth.AuthRequest.Operation;
 import blackdoor.auth.User.UserRight;
 
 /**
  * @author kAG0
  *
  */
 public class AuthClient {
 	private String server;
 	private int port;
 	private Socket socket;
 	private OutputStream outputBuffer;
 	private ObjectOutput outputObject;
 	private InputStream inputBuffer;
 	private ObjectInput inputObject;
 	
 //	AuthClient(){
 //		server = null;
 //		port = 0;
 //		socket = null;
 //		outputBuffer = null;
 //		outputObject = null;
 //		inputBuffer = null;
 //		inputObject = null;
 //	}
 	
 	AuthClient(String server, int port){
 		this.server = server;
 		this.port = port;
 		//openSocket();
 	}
 	public boolean checkUser(String userName, String password){
 		AuthRequest request = new AuthRequest(Operation.CHECK);
 		request.setUserName(userName);
 		request.setPasswordHash(Hash(password));
 		int id = request.getID();
 		AuthReply reply = null;
 		reply = exchange(request);
 		if(reply == null){
 			System.err.println("Reply from server not recieved.");
 			return false;
 		}
 			
 		if(reply.getId() == id){
 			return reply.isOperationCompleted();
 		}
 		else System.err.println("id of reply does not match id of sent request.");
 		return false;
 	}
 	public boolean changePassword(String userName, String oldPassword, String newPassword){
 		AuthRequest request = new AuthRequest(Operation.CHANGEPASSWORD);
 		request.setUserName(userName);
 		request.setPasswordHash(Hash(oldPassword));
 		request.setNewPasswordHash(Hash(newPassword));
 		int id = request.getID();
 		AuthReply reply = null;
 		reply = exchange(request);
		if(reply == null){
			System.err.println("Reply from server not recieved.");
			return false;
		}
 		if(reply.getId() == id){
 			return reply.isOperationCompleted();
 		}
 		else System.err.println("id of reply does not match id of sent request.");
 		return false;
 	}
 	public boolean addUser(String userName, String password,UserRight[] rights, String authUserName, String authPassword) {
 		AuthRequest request = new AuthRequest(Operation.ADD);
 		request.setUserName(userName);
 		request.setPasswordHash(Hash(password));
 		request.setRights(rights);
 		request.setAuthUserName(authUserName);
 		request.setAuthPasswordHash(Hash(authPassword));
 		int id = request.getID();
 		AuthReply reply = null;
 		reply = exchange(request);
		if(reply == null){
			System.err.println("Reply from server not recieved.");
			return false;
		}
 		if(reply.getId() == id){
 			return reply.isOperationCompleted();
 		}
 		else System.err.println("id of reply does not match id of sent request.");
 		return false;
 	}
 	public boolean removeUser(String userName, String authUserName, String authPassword) {
 		AuthRequest request = new AuthRequest(Operation.REMOVE);
 		request.setUserName(userName);
 		request.setAuthUserName(authUserName);
 		request.setAuthPasswordHash(Hash(authPassword));
 		int id = request.getID();
 		AuthReply reply = null;
 		reply = exchange(request);
		if(reply == null){
			System.err.println("Reply from server not recieved.");
			return false;
		}
 		if(reply.getId() == id){
 			return reply.isOperationCompleted();
 		}
 		else System.err.println("id of reply does not match id of sent request.");
 		return false;
 	}
 	public boolean changeUserName(String oldUserName, String newUserName, String authUserName, String authPassword){
 		AuthRequest request = new AuthRequest(Operation.CHANGENAME);
 		request.setUserName(oldUserName);
 		request.setNewUserName(newUserName);
 		request.setAuthUserName(authUserName);
 		request.setAuthPasswordHash(Hash(authPassword));
 		int id = request.getID();
 		AuthReply reply = null;
 		reply = exchange(request);
		if(reply == null){
			System.err.println("Reply from server not recieved.");
			return false;
		}
 		if(reply.getId() == id){
 			return reply.isOperationCompleted();
 		}
 		else System.err.println("id of reply does not match id of sent request.");
 		return false;
 	}
 
 	public AuthReply exchange(AuthRequest request) {
 		AuthReply reply = null;
 
 		try {
 			openSocket();
 		} catch (Exception e1) {
 			return null;
 		}
 		try {
 			sendRequest(request);
 			reply = reciveReply();
 		} catch (ClassNotFoundException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		} finally {
 			try {
 				closeSocket();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 		return reply;
 	}
 	
 	private byte[] Hash(String string){	
 		return blackdoor.util.Hash.getSHA1(string.getBytes());
 	}
 		
 	private void openSocket() throws Exception{
 		try {
 			socket = new Socket(server, port);
 			System.out.println("connected to " + server + ":" + port);
 			outputBuffer = new BufferedOutputStream(socket.getOutputStream());
 			outputObject = new ObjectOutputStream(outputBuffer);
 			inputBuffer = new BufferedInputStream(socket.getInputStream());
 			inputObject = new ObjectInputStream(inputBuffer);
 		}catch(SocketException e){
 			System.err.println("SocketException: " + e.getMessage());
 			throw new Exception(e.getMessage());
 		} catch (UnknownHostException e) {
 			//e.printStackTrace();
 			System.err.println("could not find " + server);
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 	private void sendRequest(AuthRequest request) throws IOException{
 		outputObject.writeObject(request);
 	}
 	private AuthReply reciveReply() throws ClassNotFoundException, IOException{
 		if(inputObject != null){
 			return (AuthReply) inputObject.readObject();
 		}
 		else return null;
 	}
 	private void closeSocket() throws IOException{
 		try{
 			inputObject.close();
 			inputBuffer.close();
 			outputObject.close();
 			outputBuffer.close();
 			socket.close();
 		}catch(NullPointerException e){
 			System.err.println("Couldn't close connections. Was the connection reset?");
 		}
 	}
 
 }
