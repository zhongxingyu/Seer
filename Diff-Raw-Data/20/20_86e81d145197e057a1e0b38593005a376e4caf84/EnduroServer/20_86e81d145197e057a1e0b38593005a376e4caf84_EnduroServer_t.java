 package enduro.network.server;
 
 import java.io.*;
 import java.net.*;
 
import enduro.racer.configuration.ConfigParser;

 /**
  * Server for EnduroProject, simple method that launches several threads in order for multiple clients to connect
  * 
  */
 public class EnduroServer implements Runnable{
 	ServerSocket serverSocket;
 /**
  * Sets up the server socket
  * @param port int containing port address
  */
 	public EnduroServer(int port){
 		try {
 			serverSocket = new ServerSocket(port);
 		} catch (IOException e) {
 			System.out.println("Could not listen on port: " + port);
 			e.printStackTrace();
 		}
 	}
 	/**
 	 * Main method to launch the server
 	 * @param args array containing one element, the port address
 	 * @throws IOException
 	 */
 	public static void main(String[] args) throws IOException {
//		if(args.length != 1){
//			System.out.println("Arguments must match \"port\"");
//			System.exit(-1);
//		}
		EnduroServer serv = new EnduroServer(ConfigParser.getInstance().getIntConf("serverport"));
//		EnduroServer serv = new EnduroServer(Integer.parseInt(args[0]));
 		serv.run();
 	}
 	/**
 	 * Method to shut down the server
 	 */
 	public void quit(){
 		try {
 			serverSocket.close();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 	/**
 	 * Method to run the server instantiates multiple threads
 	 */
 	public void run(){
 		System.out.println("Server is now running on port " + serverSocket.getLocalPort());
 		int counter = 0;
 		while (true)
 			try {
 				new EnduroServerThread(serverSocket.accept(), counter++).start();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 	}
 }
