 package edu.cmu.cs.cs214.hw9.backend;
 
 import java.io.*;
 import java.net.InetAddress;
 import java.net.Socket;
 import java.net.UnknownHostException;
 import java.text.DateFormat;
 import java.text.ParseException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Date;
 import java.util.List;
 import java.util.Map;
 
 
 
 public class ServeThread extends Thread {
 	private final BufferedReader reader;
 	private PrintWriter writer;
 	private final PrintWriter socketWriter;
 	private final Backend db;
 	private final Map<String, Integer> userTable;
 	private final int serverID;
 	private final Cache cache;
 	private final Socket s;
 	/**
 	 * Creates a serverThread to handle requests to register or unregister
 	 * @param in The stream from the client
 	 * @param out The stream back to the client
 	 * @param b The database 
 	 */
 	public ServeThread(InputStream in, OutputStream out, Backend b, Map<String, Integer> userTable, int id, Cache c, Socket s) {
 		reader = new BufferedReader(new InputStreamReader(in));
 		socketWriter = new PrintWriter(out, true);
 		db = b;
 		this.userTable = userTable;
 		serverID = id;
 		this.cache = c;
 		this.s = s;
 	}
 	
 	/** Reads one line at a time and transfers to the writer */
 	public void run() {
 		try {
 			String line = reader.readLine();
 			StringWriter responseHolder = new StringWriter();
 			writer = new PrintWriter(responseHolder);
 
 			if (cache.requestInCache(line)) {
 				socketWriter.println(cache.getResponseForRequest(line));
 				return;
 			}
 			System.out.println(serverID + ": request = " + line);
 			handleRequest(line);
 			
 			writer.flush();
 			String response = responseHolder.toString();
 			System.out.println(serverID +": response = " + response);
 			cache.cacheRequest(line, response);
 			
 			socketWriter.write(response);
 			socketWriter.flush();
 			s.close();
 			
 		} catch (IOException e) {
 			e.printStackTrace();
 		}	
 	}
 	
 	private void handleRequest(String line) {
 		String[] args = line.split("____");
 		String requestType = args[0];
 		String email = args[1];
 		
 		//First handle requests that deal with adding new users to the userTable
 		if (requestType.equals("REGISTER")) {
 			//REGISTER (email) (username) (password)
 			if (args.length != 4) {
 				writer.println("ERROR____BAD_ARGUMENTS");
 				return;
 			}
 			if (userTable.get(args[1]) != null) {
 				writer.println("ERROR____USER_ALREADY_REGISTERED");
 				return;
 			}
 			
 			String name = args[2];
 			String password = args[3];
 			//register the user on this server
 			db.register(new User(email, name, password));
 			userTable.put(email, serverID);
 			updateUserTables(email);
 			
 			writer.println("OK");
 			return;
 		}
 		else if (requestType.equals("NEW_USER")) {
 			//NEW_USER (email) (fullName) (password) (serverID)
 			
 			db.register(new User(email, args[2], args[3]));
 			int serverID = Integer.parseInt(args[4]);
 			userTable.put(email, serverID);
 			writer.println("OK");
 			return;
 		}
 		
 		//Now check if the data requested is stored locally
 		if (userTable.get(email) == null) {
 			writer.println("ERROR____UNKNOWN_USER");
 			return;
 		}
 		
 		int locationID = userTable.get(email);
 		if (locationID != serverID) {
 			forwardRequestToServer(locationID, line, writer);
 			return;
 		}
 		
 		//The data must be local at this point
 		if (requestType.equals("UPDATE_STATUS")) {
 			//UPDATE_STATUS (email) (status)
 			User u = db.getUser(email);
 			String status = args[2];
 			//need to recombine the split status
 			for (int i = 3; i < args.length; i++) {
 				status = status + " " + args[i];
 			}
 			
 			db.storeStatus(new Status(status, u, new Date()));
 			writer.println("OK");
 			return;
 		}
 		else if (requestType.equals("REQUEST_FRIEND")) {
 			//REQUEST_FRIEND (requestee email) (requester email)
 			User u = db.getUser(email);
 			User u2 = new User(args[2]);
 			
 			int requesterID = userTable.get(u2.getEmail());
 			StringWriter responseWriter = new StringWriter();
 			forwardRequestToServer(requesterID, "PENDING_FRIEND_REQUEST____" + u2.getEmail() + "____" + u.getEmail(), responseWriter);
 			
 			String response = responseWriter.toString();
 			if (response.equals("YES")) {
 				//request has been made so just add them as friends
 				db.storeFriend(u, u2);
 				forwardRequestToServer(requesterID, "ADD_FRIEND____" + u2.getEmail() + "____" + u.getEmail(), new StringWriter());
 			}
 			else {
 				List<User> requests = db.getFriendRequests(u);
 				requests.add(u2);
 			}
 			writer.println("OK");
 			return;
 		}
 		else if (requestType.equals("ADD_FRIEND")) {
 			//ADD_FRIEND (requestee email) (requester email)
 			User u = db.getUser(email);
 			User u2 = new User(args[2]);
 			
 			List<User> requests = db.getFriendRequests(u);
 			requests.remove(u2);
 			
 			db.storeFriend(u, u2);
 			
 			writer.println("OK");
 			return;
 		}
 		else if (requestType.equals("REMOVE_FRIEND")) {
 			//REMOVE_FRIEND (email1) (email2) (CLIENT/SERVER)
 			User u = db.getUser(email);
 			User u2 = new User(args[2]);
 
 			if (args[3].equals("CLIENT")) {
 				//make sure the friend link is removed from the other server as well
 				forwardRequestToServer(userTable.get(args[2]), "REMOVE_FRIEND____" + args[2] + "____" + email + "____SERVER", new StringWriter());
 			}
 			
 			db.removeFriend(u, u2);
 			writer.println("OK");
 			return;
 		}
 		else if (requestType.equals("LOGIN")) {
 			//LOGIN (email) (password)
 			String pwd = args[2];
 			if (db.login(email, pwd)) {
 				writer.println("OK");
 			}
 			else {
 				writer.println("ERROR____BAD_LOGIN");
 			}
 			return;
 		}
 		else if (requestType.equals("PENDING_FRIEND_REQUEST")) {
 			//PENDING_FRIEND_REQUEST (email1) (email2)
 			User u = db.getUser(email);
 			User u2 = new User(args[2]);
 			if (db.getFriendRequests(u).contains(u2)) {
 				writer.println("YES");
 			}
 			else {
 				writer.println("NO");
 			}
 			return;
 		}
 		else if (requestType.equals("ARE_FRIENDS")) {
 			//ARE_FRIENDS (email1) (email2)
 			User u = db.getUser(email);
 			User u2 = new User(args[2]);
 			if (db.areFriends(u, u2)) {
 				writer.println("YES");
 			}
 			else {
 				writer.println("NO");
 			}
 		}
 		else if (requestType.equals("GET_FRIENDS")) {
 			//GET_FRIENDS (email)
 			User u = db.getUser(email);
 			List<User> friends = db.getFriends(u);
 			
 			for (User f : friends) {
 				writer.println(f.getEmail());
 			}
 		}
 		else if (requestType.equals("GET_USER_INFO")) {
 			//GET_USER_INFO (email)
 			User u = db.getUser(email);
 			writer.println("OK____" + u.getEmail() + "____" + u.getFullname());
 			return;
 		}
 		else if (requestType.equals("GET_STATUSES")) {
 			//GET_STATUSES (email)
 			User u = db.getUser(email);
 			List<Status> statuses = db.getStatuses(u);
 			for (Status s : statuses) {
 				writer.println(s.getPoster().getEmail() + "____" + s.getMessage() + "____" + s.getTimestamp().toString());
 			}
 			return;
 		}
 		else if (requestType.equals("GET_FRIEND_REQUESTS")) {
 			//GET_FRIEND_REQUESTS (email)
 			User u = db.getUser(email);
 			List<User> requests = db.getFriendRequests(u);
 			
 			for (User requester : requests) {
 				writer.println(requester.getEmail());
 			}
 			return;
 		}
 		else if (requestType.equals("GET_FRIEND_UPDATES")) {
 			//GET_FRIEND_UPDATES (email)
 			User u = db.getUser(email);
 			List<User> friends = db.getFriends(u);
 			List<Status> allStatuses = new ArrayList<Status>(10);
 			for (User f : friends) {
 				String fEmail = f.getEmail();
 				int friendLocationID = userTable.get(fEmail);
 				if (friendLocationID == serverID) {
 					allStatuses.addAll(db.getStatuses(db.getUser(fEmail)));
 				}
 				else {
 					allStatuses.addAll(getStatusesForUser(fEmail, friendLocationID));
 				}
 			}
 			
 			Collections.sort(allStatuses,((Database)db).new StatusCompare());
 			
 			if(allStatuses.size() > 10){
 				List<Status> statArray = new ArrayList<Status>();
 				for(int i = 0; i < 10; i ++){
 					statArray.add(allStatuses.get(i));
 				}
 				allStatuses = statArray;
 			}
 			else if (allStatuses.size() == 0) {
 				allStatuses.add(new Status("No statuses to display!", u, new Date()));
 			}
 			
 			for (Status s : allStatuses) {
 				writer.println(s.getPoster().getEmail() + "____" + s.getMessage() + "____" + s.getTimestamp().toString());
 			}
 			return;
 		}
 	}
 	
 	/**
 	 * Send a notification to all the other servers that a new user has been registered
 	 * @param email the users email
 	 */
 	private void updateUserTables(String email) {
 		User u = db.getUser(email);
 		String name = u.getFullname();
 		String pwd = u.getPassword();
 		for (int i = 0; i < ServerConstants.serverList.length; i++) {
 			//don't need to notify self
 			if (i == serverID) continue;
 			
 			forwardRequestToServer(i, "NEW_USER____" + email + "____" + name + "____" + pwd + "____" + serverID, new StringWriter());
 		}
 	}
 
 	/**
 	 * Forwards a request to a server that can handle it and writes what is returned
 	 * to the output stream of this thread.
 	 * 
 	 * @param destID The serverID that can handle the request
 	 * @param request The request string
 	 * @param responseWriter The stream to write any responses to.
 	 */
 	private void forwardRequestToServer(int destID, String request, Writer responseWriter) {
 		try {
 			Socket requestSock = new Socket(InetAddress.getLocalHost(), ServerConstants.servePort + destID);
 			PrintWriter w = new PrintWriter(requestSock.getOutputStream(), true);
 			BufferedReader r = new BufferedReader(new InputStreamReader(requestSock.getInputStream()));
 			w.println(request);
 			String responseLine;
 			while ((responseLine = r.readLine()) != null) {
 				responseWriter.write(responseLine + "\n");
 			}
 			
 		} catch (UnknownHostException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 	
 	/**
 	 * Performs a status fetch request and parses it into a list of statuses
 	 * 
 	 * @param email The users email
 	 * @param location The serverID the user is stored on.
 	 * @return Returns a list of all of the given users statuses.
 	 */
 	private List<Status> getStatusesForUser(String email, int location) {
 		ArrayList<Status> allStatus = new ArrayList<Status>();
 		StringWriter w = new StringWriter();
 		
 		forwardRequestToServer(location, "GET_STATUSES____" + email, w);
 		
 		BufferedReader r = new BufferedReader(new StringReader(w.toString()));
 		
 		String line;
 		try {
 			while ((line = r.readLine()) != null) {
 				String[] args = line.split("____");
 				User u = db.getUser(args[0]);
 				String status = args[1];
 				Date d = DateFormat.getInstance().parse(args[2]);
 				
 				Status s = new Status(status, u, d);
 				allStatus.add(s);
 			}
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (ParseException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		return allStatus;
 	}
 }
