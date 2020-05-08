 package bank;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.net.SocketException;
 
 public class BankServer implements Runnable {
 	/** Underlying bank structure. */
 	private final Bank bank;
 
     /** Socket to accept client connections from. */
     private final ServerSocket serverSocket;
 
     /** Flag to know when to stop. */
     private boolean shouldRun = true;
 
 	/**
 	 * Creates a bank server wrapped around a bank.
 	 * @param bank The bank to alter.
 	 * @param port The port to listen on.
 	 * @throws IOException If there was an error starting the server.
 	 */
 	public BankServer(Bank bank, int port) throws IOException {
 		this.bank = bank;
 		serverSocket = new ServerSocket(port);
 	}
 
 	@Override
 	public void run() {
 		System.out.println("Listening for connections...");
 		while (shouldRun) {
 		    BufferedReader in = null;
 			try {
 			    // Listen for a connection.
 				Socket socket = serverSocket.accept();
 
 				// Wrap it in a BufferedReader.
 	            in = new BufferedReader(
 	            		new InputStreamReader(socket.getInputStream()));
 
 	            // Assume 1 command per line.
 	            String next = null;
 	            while ((next = in.readLine()) != null) {
 	            	String[] args = next.split(" ");
 	            	bank.update(Integer.parseInt(args[0]),
 	            			Integer.parseInt(args[1]),
 	            			Integer.parseInt(args[2]));
 	            }
 			} catch (SocketException e) {
 				System.out.println("Detected stop request.");
 			} catch (IOException e) {
 				e.printStackTrace();
 			} finally {
 	            // Clean up.
 	            try {
                    in.close();
                 } catch (IOException e) {
                     e.printStackTrace();
                 }
 			}
 		}
 		System.out.println("Server shutting down.");
 	}
 
 	/** Signals the server should stop. */
 	public void stop() {
 	    // Signal stop.
 		shouldRun = false;
 		try {
 		    // Close socket (will cause exception in run()).
 			serverSocket.close();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	/**
	 * Creates a new server on port 1234 and listens for connetions.
 	 * @param args
 	 * @throws InterruptedException
 	 */
 	public static void main(String[] args) throws InterruptedException {
 	    final int PORT = 1234;
 	    final int NUM_ACCOUNTS = 1000 * 1000;
 
 		BankServer server = null;
 		Thread serverThread = null;
 
 		Bank bank = new Bank(NUM_ACCOUNTS);
 		try {
 			server = new BankServer(bank, PORT);
 			serverThread = new Thread(server, "BankServer");
 		} catch (IOException e) {
 			e.printStackTrace();
 			System.exit(-1);
 		} finally {
     		serverThread.start();
    		Thread.sleep(1000);  // wait 10s
     		server.stop();
     		serverThread.join();
 		}
 	}
 }
