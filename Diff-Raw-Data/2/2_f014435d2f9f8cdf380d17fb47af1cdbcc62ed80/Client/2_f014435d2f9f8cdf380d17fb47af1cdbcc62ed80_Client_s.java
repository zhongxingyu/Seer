 package mcgill.game;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.UUID;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 
 import com.google.gson.Gson;
 
 public class Client implements Runnable {
 	
 	private User user;
 	
 	private Gson gson;
 	private String session;
 	private ExecutorService executor;
 	private Notifications notifications;
 	
 	public static void main(String[] args) {
     	Client client = new Client(Config.REDIS_HOST, Config.REDIS_PORT);
     	client.run();
     }
 	
 	public Client(String host, int port) {
 		this.gson = new Gson();
 		this.session = UUID.randomUUID().toString();
 		this.executor = Executors.newSingleThreadExecutor();
 		this.notifications = new Notifications(this.session);
 	}
 	
 	public void createUser() {
 		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
 		
 		try {
 			System.out.print("Username: ");
 			String username = br.readLine();
 			System.out.print("Password: ");
 			String password = br.readLine();
 			
 			String[] args = {this.session, username, password};
 			
 			ServerCall server = new ServerCall(this.session);
 			String res = server.call(Config.REGISTER, args);
 			
 			System.out.println("Register Res is: " + res + "\n");
 			
 		} catch (IOException e) {
 			e.printStackTrace();
 			System.exit(1);
 		}
 	}
 	
 	public void login() {
 		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
 		
 		try {
 			System.out.print("Username: ");
 			String username = br.readLine();
 			System.out.print("Password: ");
 			String password = br.readLine();
 			
 			String[] args = {this.session, username, password};
 			
 			ServerCall server = new ServerCall(this.session);
 			String res = server.call(Config.LOGIN, args);
 			
 			System.out.println("Login Res is: " + res + "\n");
 			
 		} catch (IOException e) {
 			e.printStackTrace();
 			System.exit(1);
 		}
 	}
 	
 	public Boolean loginUI(String username, String password) {
 		String[] args = {this.session, username, password};
 		
 		ServerCall server = new ServerCall(this.session);
 		String result = server.call(Config.LOGIN, args);
 		
		if (result == "") {
 			return false;
 		}
 		
 		this.user = this.gson.fromJson(result, User.class);
 		return true;
 	}
 	
 	public void getFriends() {
 		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
 		
 		try {
 			System.out.print("Username: ");
 			String username = br.readLine();
 			
 			String[] args = {this.session, username};
 			
 			ServerCall server = new ServerCall(this.session);
 			String res = server.call(Config.GET_FRIENDS, args);
 			
 			System.out.println("Get Friends Res is: " + res + "\n");
 			
 		} catch (IOException e) {
 			e.printStackTrace();
 			System.exit(1);
 		}		
 	}
 	
 	public void addFriend() {
 		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
 		
 		try {
 			System.out.print("Username: ");
 			String username = br.readLine();
 			System.out.print("Friend: ");
 			String friend = br.readLine();
 			
 			String[] args = {this.session, username, friend};
 			
 			ServerCall server = new ServerCall(this.session);
 			String res = server.call(Config.ADD_FRIEND, args);
 			
 			System.out.println("Add Friends Res is: " + res + "\n");
 			
 		} catch (IOException e) {
 			e.printStackTrace();
 			System.exit(1);
 		}		
 	}
 	
 	public void getChats() {
 		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
 		
 		try {
 			System.out.print("Username: ");
 			String username = br.readLine();
 			
 			String[] args = {this.session, username};
 			
 			ServerCall server = new ServerCall(this.session);
 			String res = server.call(Config.GET_CHATS, args);
 			
 			System.out.println("Get Chats Res is: " + res + "\n");
 			
 		} catch (IOException e) {
 			e.printStackTrace();
 			System.exit(1);
 		}
 	}
 	
 	public void createChat() {
 		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
 		
 		try {
 			System.out.print("Username: ");
 			String username = br.readLine();
 			System.out.print("Friend: ");
 			String friend = br.readLine();
 			
 			String[] args = {this.session, username, friend};
 			
 			ServerCall server = new ServerCall(this.session);
 			String res = server.call(Config.CREATE_CHAT, args);
 			
 			System.out.println("Create Chat Res is: " + res + "\n");
 			
 		} catch (IOException e) {
 			e.printStackTrace();
 			System.exit(1);
 		}
 	}
 	
 	public void sendMessage() {
 		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
 		
 		try {
 			System.out.print("Username: ");
 			String username = br.readLine();
 			System.out.print("Message: ");
 			String message = br.readLine();
 			System.out.print("Chat ID: ");
 			String chat_id = br.readLine();
 			
 			String[] args = {this.session, username, message, chat_id};
 			
 			ServerCall server = new ServerCall(this.session);
 			String res = server.call(Config.MESSAGE, args);
 			
 			System.out.println("Message Res is: " + res + "\n");
 			
 		} catch (IOException e) {
 			e.printStackTrace();
 			System.exit(1);
 		}
 	}
 	
 	public void addCredits() {
 		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
 		
 		try {
 			System.out.print("Username: ");
 			String username = br.readLine();
 			
 			String[] args = {this.session, username};
 			
 			ServerCall server = new ServerCall(this.session);
 			String res = server.call(Config.ADD_CREDITS, args);
 			
 			System.out.println("Add Credits Res is: " + res + "\n");
 			
 		} catch (IOException e) {
 			e.printStackTrace();
 			System.exit(1);
 		}		
 	}
 	
 	public void getTables() {
 		String[] args = {this.session};
 		
 		ServerCall server = new ServerCall(this.session);
 		String res = server.call(Config.GET_TABLES, args);
 		
 		System.out.println("Get Tables Res is: " + res + "\n");
 	}
 	
 	public void createTable() {
 		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
 		
 		try {
 			System.out.print("Username: ");
 			String username = br.readLine();
 			System.out.print("Name: ");
 			String name = br.readLine();
 			
 			String[] args = {this.session, username, name};
 			
 			ServerCall server = new ServerCall(this.session);
 			String res = server.call(Config.CREATE_TABLE, args);
 			
 			System.out.println("Create Table Res is: " + res + "\n");
 			
 		} catch (IOException e) {
 			e.printStackTrace();
 			System.exit(1);
 		}		
 	}
 	
 	public void joinTable() {
 		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
 		
 		try {
 			System.out.print("Username: ");
 			String username = br.readLine();
 			System.out.print("Table ID: ");
 			String table_id = br.readLine();
 			
 			String[] args = {this.session, username, table_id};
 			
 			ServerCall server = new ServerCall(this.session);
 			String res = server.call(Config.JOIN_TABLE, args);
 			
 			System.out.println("Join Table Res is: " + res + "\n");
 			
 		} catch (IOException e) {
 			e.printStackTrace();
 			System.exit(1);
 		}		
 	}
 
 	public void startRound() {
 		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
 		
 		try {
 			System.out.print("Table ID: ");
 			String table_id = br.readLine();
 			
 			String[] args = {this.session, table_id};
 			
 			ServerCall server = new ServerCall(this.session);
 			String res = server.call(Config.START_ROUND, args);
 			
 			System.out.println("Start Round Res is: " + res + "\n");
 			
 		} catch (IOException e) {
 			e.printStackTrace();
 			System.exit(1);
 		}
 	}
 	
 	public void exit() {
 		System.exit(0);
 	}
 	
 	public void run() {
 		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
 		
 		this.executor.execute(this.notifications);
 		
 		while (true) {
 			System.out.println("Client Started, choose action:");
 			System.out.println("1. Login");
 			System.out.println("2. Create User");
 			System.out.println("3. Get Friends");
 			System.out.println("4. Add Friend");
 			System.out.println("5. Get Chats");
 			System.out.println("6. Create Chat");
 			System.out.println("7. Send Message");
 			System.out.println("8. Add Credits");
 			System.out.println("9. Get Tables");
 			System.out.println("10. Create Table");
 			System.out.println("11. Join Table");
 			System.out.println("12. Start Round");
 			System.out.println("13. Exit");
 			System.out.print("=> ");
 		
 			try {
 				String command = br.readLine();
 				
 				switch(Integer.parseInt(command)) {
 				case 1:
 					this.login();
 					break;
 					
 				case 2:
 					this.createUser();
 					break;
 					
 				case 3:
 					this.getFriends();
 					break;
 					
 				case 4:
 					this.addFriend();
 					break;
 					
 				case 5:
 					this.getChats();
 					break;
 					
 				case 6:
 					this.createChat();
 					break;
 					
 				case 7:
 					this.sendMessage();
 					break;
 					
 				case 8:
 					this.addCredits();
 					break;
 					
 				case 9:
 					this.getTables();
 					break;
 					
 				case 10:
 					this.createTable();
 					break;
 					
 				case 11:
 					this.joinTable();
 					break;
 					
 				case 12:
 					this.startRound();
 					break;
 					
 				case 13:
 					this.exit();
 					break;
 				}
 				
 			} catch (IOException e) {
 				e.printStackTrace();
 				System.exit(1);
 			}
 		}
 		
 	}
 	
 }
