 package client;
 
 import shared.*;
 import java.util.*;
 import java.io.*;
 import java.net.*;
 
 public class ChatClient {
 
     /** client socket */
     Socket socket;
 
     /** cmd line input */
     Scanner stdIn;
 
     /** socket reader */
     BufferedReader input;
 
     /** socket writer */
     PrintWriter output;
 
     private UUID id;
     
     public String username;
 
 
     /**
      *  Constructor
      */
     public ChatClient(final String host, final int port, String username) {
 	try {
 	    socket = new Socket(host, port);
 	    stdIn = new Scanner(System.in);
 	    input =  new BufferedReader(new InputStreamReader(socket.getInputStream()));
 	    output = new PrintWriter(socket.getOutputStream(), true);
 	    id = UUID.randomUUID();
 	    send(id.toString()+ ":"+username);
 	} catch (Exception e) {
 	    e.printStackTrace();
 	    System.exit(-1);
 	}
     }
 
 
     /**
      *  User Input
      */
     public String userInput(String prompt) {
 	if (prompt != null) {
 	    prompt.trim();
 	    System.out.print(prompt + " ");
 	}
 	return stdIn.nextLine();
     }
     
 
     /**
      *  Send
      */
     public void send(String message) throws IOException{
 	output.println(message);
     }
 
     public void send(ClientMessage message) throws IOException {
 	send(message.toString());
     }
 
 
     /**
      *  receive
      */
     public String receive() throws IOException {
 	return input.readLine();
 
 
     }
 
     public UUID getID() {
 	return id;
     }
 
     /**
      *  Main
      */
     public static void main(String[] args) {	
 	
 	// new client
 	System.out.println("Enter server ip: ");
 	Scanner scanner = new Scanner(System.in);
 	String ip = scanner.nextLine();
 	System.out.println("Enter a username: ");
 	String username = scanner.nextLine();
 	ChatClient client = new ChatClient(ip, 8080, username);
 	Thread listen = new Thread(new ClientListener(client));
 	Thread speak = new Thread(new ClientSpeaker(client));
 	listen.start();
 	speak.start();
     }
 }
 
 
 /**
  * Client Listener
  */
 class ClientListener implements Runnable {
     
     ChatClient client;
     
     public ClientListener(ChatClient client) {
 	this.client = client;
     }
     
     public void run() {
 	while (true) {
 	    try {
 		String rawMessage = client.receive();
 		if (rawMessage == null) break;
 
 		ClientMessage message = ClientMessage.fromString(rawMessage);
 		if (message.action == ServerAction.list) {
 		    System.out.println("Users: ");
		    String names = message.text.split(".");
 		    for (int i = 0; i < names.length; i++) {
 			System.out.println(names[i]);
 		    }
 
 		} else {
 		    System.out.println(message.text);
 		}
 
 		
 		
 
 	    } catch (Exception e) {
 		e.printStackTrace();
 	    }
 	} 
 	System.out.println("Disconnected...");
 	System.exit(0);
     }
 }
 
 
 /**
  * Client Speaker 
  */
 class ClientSpeaker implements Runnable {
     
     ChatClient client;
     
     public ClientSpeaker(ChatClient client) {
 	this.client = client;
     }
     
     public void run() {
 
 	while (true) {
 	    try {
 		
 		String input = client.userInput(null);
 		ClientMessage message = InputParser.parse(client, input);
 		if (message != null) {
 		    client.send(message);
 		} else {
 		    System.out.println("Invalid input");
 		}
 		
 		
 	    } catch (Exception e) {
 		e.printStackTrace();
 	    }
 	    
 	}
     }
 }
