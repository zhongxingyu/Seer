 package network;
 
 import java.io.IOException;
 import java.net.ServerSocket;
 
 import logging.Logger;
 import logging.Message;
 import config.Configurations;
 import gui.Gui;
 
 public class ConnectionListener extends Thread {
 	Logger _logger ;
 	public ConnectionListener(Logger logger) {
 		_logger = logger;
 	}
 
 	public ConnectionListener(Runnable target) {
 		super(target);
 		// TODO Auto-generated constructor stub
 	}
 
 	public ConnectionListener(String name) {
 		super(name);
 		// TODO Auto-generated constructor stub
 	}
 
 	public void run(Gui g){
 		while(true){
 			try {
 				ServerSocket serverSocket = new ServerSocket(Client.SOCKET);
 				_logger.log(new Message(serverSocket.getLocalSocketAddress()+" connected",Message.Type.Report));
 				Client c = new Client(_logger);
 				c.init(serverSocket.accept());
 				c.setUsername(Configurations.getAttribute("Username"));
 				g.addChat(c);
				serverSocket.close();
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				System.out.print("?");
 			}
 			
 		}
 		
 	}
 
 }
