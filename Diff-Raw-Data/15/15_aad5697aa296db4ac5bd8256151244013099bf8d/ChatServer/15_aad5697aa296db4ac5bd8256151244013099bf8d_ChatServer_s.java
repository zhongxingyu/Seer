 package miniRSAChat;
 
 import java.io.*;
 import java.net.*;
 import java.util.Random;
 import java.util.Scanner;
 
 class ChatServer implements Runnable {
<<<<<<< HEAD
  
=======
 	
>>>>>>> New code push
 	static ServerSocket serverSocket = null;
 	static Socket clientSocket = null;
 	static PrintStream os = null;
 	static BufferedReader is = null;
 	static BufferedReader inputLine = null;
 	static boolean closed = false;
 	static RSA.RSAKeyPair keys;
 
 	public static void main(String args[]) {
 		/*
 		 * Codes added for RSA
 		 */
         Global.rand = new Random();
         keys = RSA.genKeyPair(0);
         System.out.println("The server's public key is: " + String.valueOf(keys.publicKey.e) + " " +
         				 String.valueOf(keys.publicKey.N));        
         
         System.out.print("Input the client's public key: ");
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
 			serverSocket = new ServerSocket(1234);
 			System.out.println("Waiting for connection...");
 			clientSocket = serverSocket.accept();
 			System.out.print("Server has connected!\n");
 			inputLine = new BufferedReader(new InputStreamReader(System.in));
 			os = new PrintStream(clientSocket.getOutputStream());
 			is = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             
 		} catch (Exception e) {
 			System.err.print("Exception:  " + e);
 		}
 		
 		if (clientSocket != null && os != null && is != null) {
 			try {
                 
 				// Create a thread to read from the client
                 
 				new Thread(new ChatServer()).start();
                 
 				while (!closed) {
 					System.out.print("Server: ");
 					String message = inputLine.readLine();
 					/*
 					 * Encrypt
 					 */
 					byte[] ciphertext = encryptor.encrypt(message);
 					os.write(ciphertext);
 					// os.println(message);
 					/*
 					 * End of modification
 					 */
 					if(message.startsWith(".quit")) break;
 					if(message.startsWith(".brute force")) {
 						RSA.PrivateKey crackedOwnKey = RSA.bruteForce(keys.publicKey);
 				        System.out.println("The server's private key is: " + String.valueOf(keys.privateKey.d) + " " +
 								 String.valueOf(keys.privateKey.N));
 				        System.out.println("You cracked your own key!: " + String.valueOf(crackedOwnKey.d) + " " +
 								 String.valueOf(crackedOwnKey.N));
 				        
 				        RSA.PrivateKey crackedClientKey = RSA.bruteForce(serverKey);
 				        System.out.println("You cracked your client's key!: " + String.valueOf(crackedClientKey.d) + " " +
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
     
 	@Override
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
 				System.out.println("\nClient: " + cleartext);
 				if(cleartext.startsWith(".quit")) break;
 			}
 			closed = true;
 		} catch (IOException e) {
 			//System.err.println("IOException:  " + e);
 		}
 	}
 }
