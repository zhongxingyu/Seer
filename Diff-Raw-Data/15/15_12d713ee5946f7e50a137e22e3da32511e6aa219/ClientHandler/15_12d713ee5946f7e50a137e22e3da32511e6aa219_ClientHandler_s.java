 package edu.cmu.cs.cs214.hw9.backend;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.OutputStream;
 import java.io.PrintWriter;
 import java.net.InetAddress;
 import java.net.Socket;
 import java.net.UnknownHostException;
 import java.text.DateFormat;
 import java.text.ParseException;
 import java.util.ArrayList;
 import java.util.Date;
 
 public class ClientHandler {
 	
 	private int serverPort;
 	private String server;
 	Socket cSocket;
 	private BufferedReader reader;
 	private PrintWriter socketWriter;
 	
 	public ClientHandler() {
 		InputStream in = null;
 		OutputStream out = null;
 		chooseServer();
 	}
 	
 	public boolean register(String email, String name, String password) {
 		openConnection();
 		String requestType = "REGISTER";
 		String request = requestType + "____" + email + "____" + name + "____" + password; 
 		String response = handleSimpleMessages(request);	
 		closeConnection();
 		System.out.println("reg response:" +response);
 		if (!response.equals("OK")) {
 			return false;
 		}
 		else {
 			return true;
 		}
 		
 	}
 	
 	public boolean updateStatus(String email, String status) {
 		openConnection();
 		String requestType = "UPDATE_STATUS";
 		String request = requestType + "____" + email + "____" + status;
 		String response = handleSimpleMessages(request);
 		closeConnection();
 		
 		if (!response.equals("OK")) {
 			return false;
 		}
 		else {
 			return true;
 		}
 	}
 	
 	public boolean requestFriend(String email1, String email2) {
 		openConnection();
 		String requestType = "REQUEST_FRIEND";
 		String request = requestType + "____" + email1 + "____" + email2;
 		String response = handleSimpleMessages(request);
 		closeConnection();
 		
 		if (!response.equals("OK")) {
 			return false;
 		}
 		else {
 			return true;
 		}
 	}
 	
 	public boolean addFriend(String requesterEmail, String requesteeEmail) {
 		openConnection();
 		String requestType = "ADD_FRIEND";
 		String request = requestType + "____" + requesterEmail + "____" + requesteeEmail + "____" + "CLIENT";
 		String response = handleSimpleMessages(request);
 		closeConnection();
 		
 		if (!response.equals("OK")) {
 			return false;
 		}
 		else {
 			return true;
 		}
 	}
 	
 	public boolean removeFriend(String requesterEmail, String requesteeEmail) {
 		openConnection();
 		String requestType = "REMOVE_FRIEND";
 		String request = requestType + "____" + requesterEmail + "____" + requesteeEmail + "____" + "CLIENT";
 		String response = handleSimpleMessages(request);
 		closeConnection();
 		
 		if (!response.equals("OK")) {
 			return false;
 		}
 		else {
 			return true;
 		}
 	}
 	
 	public boolean login(String email, String password) {
 		openConnection();
 		String requestType = "LOGIN";
 		String request = requestType + "____" + email + "____" + password;
 		String response = handleSimpleMessages(request);
 		closeConnection();
 		
 		if (!response.equals("OK")) {
 			return false;
 		}
 		else {
 			return true;
 		}
 	}
 	
 	public boolean pendingFriendRequests(String email1, String email2) {
 		openConnection();
 		String requestType = "PENDING_FRIEND_REQUEST";
 		String request = requestType + "____" + email1 + "____" + email2;
 		String response = handleSimpleMessages(request);
 		closeConnection();
 		
 		if (response.equals("YES")) {
 			return true;
 		}
 		else {
 			return false;
 		}
 	}
 	
 	public String getUserInfo(String email) {
 		openConnection();
 		String requestType = "GET_USER_INFO";
 		String request = requestType + "____" + email;
 		
 		String responseLine;
 		String fullName = "";
 		try {
 			sendMessage(request);
 			responseLine = reader.readLine();
 			String[] args = parseMessage(responseLine);
 			System.out.println(responseLine);
 			if (args[0].equals("ERROR")) {
 				fullName = null;
 			}
 			else {
 				fullName = args[2];
 			}
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			fullName = null;
 			e.printStackTrace();
 		} 
 
 		closeConnection();
 		
 		return fullName;
 	}
 	
 	public ArrayList<Status> getStatuses(String email) {
 		openConnection();
 		String requestType = "GET_STATUSES";
 		String request = requestType + "____" + email;
 		ArrayList<Status>statusList = new ArrayList<Status>();
 		
 		try {
 			String responseLine;
 			sendMessage(request);
 			while ((responseLine = reader.readLine() ) != null) {
 				String[] args = parseMessage(responseLine);
 				String userEmail = args[0];
 				String message = args[1];
 				User u = new User(userEmail, getUserInfo(userEmail), "");
				Date d = DateFormat.getInstance().parse(args[2]);
 				Status status = new Status(message, u, d);
 				statusList.add(status);
 
 			}
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (ParseException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} 
 
 		closeConnection();
 		
 		return statusList;
 	}
 	
 	
 	
 	public ArrayList<User> getFriendRequests(String email) {
 		openConnection();
 		String requestType = "GET_FRIEND_REQUESTS";
 		String request = requestType + "____" + email;
 		
 		ArrayList<User>userList = new ArrayList<User>();
 		
 		try {
 			String responseLine;
 			sendMessage(request);
 			while ((responseLine = reader.readLine() ) != null) {
 				String[] args = parseMessage(responseLine);
 				String userEmail = args[0];
 				User u = new User(userEmail, getUserInfo(userEmail), "");
 				
 				userList.add(u);
 			}
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} 
 		
 		closeConnection();
 		
 		return userList;
 	}
 	
 	public ArrayList<User> getFriends(String email) {
 		openConnection();
 		String requestType = "GET_FRIENDS";
 		String request = requestType + "____" + email;
 		
 		ArrayList<User>userList = new ArrayList<User>();
 		
 		try {
 			String responseLine;
 			sendMessage(request);
 			while ((responseLine = reader.readLine() ) != null) {
 				String[] args = parseMessage(responseLine);
 				String userEmail = args[0];
 				User u = new User(userEmail, getUserInfo(userEmail), "");
 				
 				userList.add(u);
 			}
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		closeConnection();
 		
 		return userList;
 	}
 	
 	public ArrayList<Status> getFriendUpdates(String email) {
 		openConnection();
 		String requestType = "GET_FRIEND_UPDATES";
 		String request = requestType + "____" + email;
 		
 		
 		ArrayList<Status>statusList = new ArrayList<Status>();
 		
 		try {
 			String responseLine;
 			sendMessage(request);
 			while ((responseLine = reader.readLine() ) != null) {
 				String[] args = parseMessage(responseLine);
 				if (args.length != 3) continue;
 				String userEmail = args[0];
 				String message = args[1];
 				User u = new User(userEmail, getUserInfo(userEmail), "");
				Date d = DateFormat.getInstance().parse(args[2]);
 				Status status = new Status(message, u, d);
 				statusList.add(status);
 			
 			}
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (ParseException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} 
 		
 		closeConnection();
 		
 		return statusList;
 	}
 	
 	public boolean areFriends(String email1, String email2) {
 		openConnection();
 		String requestType = "ARE_FRIENDS";
 		String request = requestType + "____" + email1 + "____" + email2;
 		String response = handleSimpleMessages(request);
 		closeConnection();
 		
 		if (response.equals("YES")) {
 			return true;
 		}
 		else {
 			return false;
 		}
 	}
 
 	
 	public String[] parseMessage(String message) {
 		String myDelimiter = "____";
 		String[] result = message.split(myDelimiter);  
 		return result;
 	}
 	
 	public String handleSimpleMessages(String request) {
 		String responseLine = "NO RESPONSE";
 		String dummyString;
 		try {
 			sendMessage(request);
 			while ((dummyString = reader.readLine() ) != null) {
 				responseLine = dummyString;
 			}
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return responseLine;
 		
 	}
 	
 	//Open socket and read/write connection
 	public void openConnection() {
 		try {
 			cSocket = new Socket(InetAddress.getLocalHost(), serverPort);
 		} catch (UnknownHostException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		} catch (IOException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		}
 		
 		InputStream in = null;
 		OutputStream out = null;
 		try {
 			in = cSocket.getInputStream();
 			out = cSocket.getOutputStream();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		reader = new BufferedReader(new InputStreamReader(in));
 		socketWriter = new PrintWriter(out, true);
 
 	}
 	
 	//Close socket
 	public void closeConnection() {
 		try{
 			reader.close();
 			socketWriter.close();
 			cSocket.close();
 		}
 		catch(IOException ioException){
 			ioException.printStackTrace();
 		}	
 	}
 	
 	//Send a message to the server
 	public void sendMessage(String msg)
 	{
 		socketWriter.println(msg);
 		socketWriter.flush();
 	}
 	
 	//Get a server and port from the serverList 
 	public void chooseServer() {
 		int serverIndex = (int) (Math.random() * (ServerConstants.serverList.length));
 		serverPort = ServerConstants.servePort + serverIndex;
 		server = ServerConstants.serverList[serverIndex]+":"+serverPort;
 
 	}
 
 }
