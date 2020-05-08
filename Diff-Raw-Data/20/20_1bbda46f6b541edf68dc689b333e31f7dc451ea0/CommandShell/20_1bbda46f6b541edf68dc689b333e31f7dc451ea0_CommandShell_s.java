 package server;
 
 
 import java.util.*;
 import java.util.concurrent.*;
 import java.io.*;
 import java.net.*;
 
 class CommandShell implements Runnable {
 
     /** Server socket */
     ChatServer server;
 
     /**
      * Command List
      */
     public HashMap<String, ServerCommand> commands;
 
     Scanner stdIn;
     
 
     /**
      * Constructor
      */
     public CommandShell(ChatServer server) {
 	this.server = server;
 	commands = new HashMap<String, ServerCommand>();
 	stdIn = new Scanner(System.in);
 	initCommands();
     }
 
     public void initCommands() {
 	
 	//list command
 	commands.put("/list", new ServerCommand() {
 		public boolean run(ChatServer server) {
 		    HashMap<UUID, Client> clients = server.clients;
 		    if (clients == null) return false;
 		    for (UUID id : clients.keySet()) {
 			System.out.println(clients.get(id).username+": "+id.toString());
 		    }
 		    return true;
 		}
 	    });
 	
 	commands.put("/kick", new ServerCommand() {
 		public boolean run(ChatServer server) {
 		    String user = stdIn.next();
 		    HashMap<UUID, Client> clients = server.clients;
 		    for (Client c : clients.values()) {
 			if (c.username.equals(user)) {
 			    try{
 					c.close();
 					server.clients.remove(c.id);
 					System.out.println("We just removed that bastard " + c.username);
 				}
 				catch (Exception e){
 				    System.out.println("Weird things are ahappenin'");
 				}
 			    return true;
 			}
 		    }
 		    return false;
 		}
 	    });
     }
 
     
     /** 
      * Run 
      */
     public void run() {
 	try {
 	    while(true) {
 		String input = stdIn.next().toLowerCase().trim();
 		ServerCommand command = commands.get(input);
 		if (command != null) {
 		    command.run(server);
 		} else {
 		    System.out.println("\""+input+"\" is not a command");
 		}
 	    }
 	} catch (Exception e ) {
 	    e.printStackTrace();
 	}
     }
 
 
     
     /**
      * Command Interface
      */
     private interface ServerCommand {
 	public boolean run(ChatServer server);
 
     }
 }
  
