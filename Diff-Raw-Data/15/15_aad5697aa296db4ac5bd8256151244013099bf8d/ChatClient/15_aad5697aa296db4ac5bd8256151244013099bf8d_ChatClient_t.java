 package miniRSAChat;
 import java.io.*;
 import java.net.*;
 import java.util.Random;
 import java.util.Scanner;
 
 class ChatClient implements Runnable {
     
 	static Socket clientSocket = null;
 	static PrintStream os = null;
 	static BufferedReader is = null;
 	static BufferedReader inputLine = null;
 	static boolean closed = false;
 	static RSA.RSAKeyPair keys;
     
 	public static void main(String[] args) {
 		
 		int port_number = 1234;
 		String host = "localhost";
 		/*
 		 * Codes added for RSA
 		 */
 		Global.rand = new Random();
 		keys = RSA.genKeyPair(0);
         System.out.println("The client's public key is:" + String.valueOf(keys.publicKey.e) + " " +
         				 String.valueOf(keys.publicKey.N));
         System.out.print("Input the server's public key: ");
         Scanner scan = new Scanner(System.in);
         long rsaE = 0;
         long rsaN = 0;
         try {
             rsaE = scan.nextLong();
             rsaN = scan.nextLong();
         }
         catch (Exception ex) {
             System.out.println(ex);
         }
         
         RSA.PublicKey serverKey = new RSA.PublicKey(rsaN, rsaE);
         RSA.Encryptor encryptor = new RSA.Encryptor(serverKey);
         
         /*
          * End of modification
          */
 		try {
 			clientSocket = new Socket(host, port_number);
 			inputLine = new BufferedReader(new InputStreamReader(System.in));
 			os = new PrintStream(clientSocket.getOutputStream());
 			is = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
 		} catch (UnknownHostException e) {
 			System.err.println("Don't know about host " + host);
 		} catch (IOException e) {
 			System.err
             .println("Couldn't get I/O for the connection to the host "
                      + host);
 		}
         
 		if (clientSocket != null && os != null && is != null) {
 			try {
                 
 				// Create a thread to read from the server
                 
 				new Thread(new ChatClient()).start();
                 
 				while (!closed) {
 					System.out.print("Client: ");
 					String message = inputLine.readLine();
 					byte[] ciphertext = encryptor.encrypt(message);
 					os.write(ciphertext);
 					if (message.startsWith(".quit")) break;
 					if (message.startsWith(".brute force")) {
 						RSA.PrivateKey crackedOwnKey = RSA.bruteForce(keys.publicKey);
 				        System.out.println("The server's private key is: " + String.valueOf(keys.privateKey.d) + " " +
 								 String.valueOf(keys.privateKey.N));
 				        System.out.println("You cracked your own key!: " + String.valueOf(crackedOwnKey.d) + " " +
 								 String.valueOf(crackedOwnKey.N));
 				        
 				        RSA.PrivateKey crackedClientKey = RSA.bruteForce(serverKey);
 				        System.out.println("You cracked server's key!: " + String.valueOf(crackedClientKey.d) + " " +
 								 String.valueOf(crackedClientKey.N));
 					}					
 				}
                 
 				os.close();
 				is.close();
 				clientSocket.close();
 			} catch (IOException e) {
 				System.err.println("IOException:  " + e);
 			}
 		}
 	}
     
 	public void run() {
 		int bufferSize = 1024;
 		byte[] responseBuffer = new byte[bufferSize];
 		RSA.Decryptor decryptor = new RSA.Decryptor(keys.privateKey);
 		int responseSize = 0;
 		try {
 			while ((responseSize = clientSocket.getInputStream().read(responseBuffer)) != 0) {
 				byte[] response = new byte[responseSize];
 				System.arraycopy(responseBuffer, 0, response, 0, responseSize);
 				String cleartext = decryptor.decrypt(response);
 				System.out.println("\nServer: " + cleartext);
 				if(cleartext.startsWith(".quit")) break;
 			}
 			closed = true;
 		} catch (IOException e) {
 			//System.err.println("IOException:  " + e);
 		}
 		
 	}
 	//Test commit
 }
