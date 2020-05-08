 /**
  * 
  */
 package com.github.ribesg.javachat.client;
 
 import static com.github.ribesg.javachat.common.Constants.*;
 
 import java.util.Scanner;
 
 /**
  * @author Ribesg
  * 
  */
 public class MainClient {
 
 	private Client client;
 
 	/**
 	 * @param args
 	 *            Not used for now
 	 */
 	public static void main(final String[] args) {
 		new MainClient();
 	}
 
 	public MainClient() {
 
 		try {
 			client = new Client(SERVER_ADDRESS, SERVER_PORT);
 			Scanner scan = new Scanner(System.in);
 			String input = "";
 			while (!input.equalsIgnoreCase("exit")) {
 				input = scan.nextLine();
 				// client.sendMessage(input, System.currentTimeMillis());
 				client.sendPing();

 			}
 			scan.close();
 		} catch (Exception e) {
 			e.printStackTrace();
 			System.exit(42);
 		}
 	}
 
 }
