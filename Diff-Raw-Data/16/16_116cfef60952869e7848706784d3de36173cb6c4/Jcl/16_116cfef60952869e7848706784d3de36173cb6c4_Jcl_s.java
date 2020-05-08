 package ua.com.globallobgic.cahcemechanism.server.jcl;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 
 import ua.com.globallobgic.cahcemechanism.server.interfaces.Server;
 
 /**
  * Command line for server instance
  * 
  * @author Constantin
  *
  */
 public class Jcl implements Runnable {
 
 	private Server server;
 	
 	public Jcl(Server server) {
 		
 		this.server = server;
 	}
 	
 	@Override
 	public void run() {
 		
 		String command;		
 		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
 		
 		while ( !Thread.currentThread().isInterrupted() ) {
 			
 			System.out.print("SERVER: ");
 			try {		
 				command = br.readLine();				
 				executeCommand(command);
 			} catch (IOException e) {
 				System.err.println("readLine exception");
 			}
 		}
 		System.out.println("\nJCL Stoped");
 	}
 
 	public boolean executeCommand(String command) {
 
 		String[] c = command.split(" ");
 
 		if (!checkArgs(c)) {
 			System.out.println("SERVER: UNKNOWN COMMAND");
 			return false;
 		}
 
 		switch (c[0].toLowerCase()) {
 
 		case "start":
 			if (server.start()) {
 				System.out.println("SERVER: STARTED");
 			} else {
 				System.out.println("SERVER: NOT STARTED");
 			}
 			break;
 
 		case "stop":
 
 			if (server.stop()) {
 				System.out.println("SERVER: STOPPED");
 			}
 			break;
 
 		case "create":
 
 			if (checkArgs(c)) {
 
 				server.createObject(Integer.parseInt(c[1], 10));
 				System.out.println("SERVER: CREATED\tid: "
 						+ Integer.parseInt(c[1], 10));
 			} else {
 				System.out.println("SERVER: INCORRECT ARGUMENTS");
 			}
 			break;
 			
 		case "update":
 
 			if (!checkArgs(c)) {
 				System.out.println("Invalid parameters");
 				return false;
 			}
 
 			if (server.updateObject(Integer.parseInt(c[1], 10))) {
 				System.out.println("SERVER: UPDATED\tid: "
 						+ Integer.parseInt(c[1], 10));
 			}
 			break;
 
 		case "show":
 			System.out.println("Collection:\n" + server.show());
 			break;
 
 		case "free":
 			server.free();
 			System.out.println("SERVER: MEMORY IS FREE");
 			break;
 
 		case "exit":
 			// Stop server
 			if (server.stop()) {
 				System.out.println("SERVER: STOPPED");
 			}
 
 			// Clear memory
 			server.free();
 
 			// Stop JCL thread
 			Thread.currentThread().interrupt();
 			break;
 
 		default:
			System.out.println("SERVER: COMMAND");
 			return false;
 		}
 		return true;
 	}
 
 	/**
 	 * Check if arguments are correct
 	 * 
 	 * @param args
 	 * @return <b>true</b> if arguments are correct or <b>false</b> if not
 	 */
 	public boolean checkArgs(String[] args) {
 
 		if (args.length < 1 || args.length > 2)
 			return false;
 		
 		if (args.length == 2) {
 			try {
 				if (Integer.parseInt(args[1], 10) < 0)
 					return false;
 			} catch (NumberFormatException ex) {
 
 				return false;
 			}
 		}
 		return true;
 	}
 }
