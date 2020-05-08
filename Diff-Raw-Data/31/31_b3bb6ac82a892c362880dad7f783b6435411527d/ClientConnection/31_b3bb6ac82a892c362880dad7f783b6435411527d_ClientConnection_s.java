 package pw.svn.server;
 
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.net.Socket;
 
 public class ClientConnection implements Runnable {
 	
 	private final Socket socket;
 	private ObjectOutputStream output;
 	private ObjectInputStream input;
 
 	public ClientConnection(Socket socket) {
 		this.socket = socket;
 		new Thread(this).start();
 	}
 
 	@Override
 	public void run() {
 		this.initializeClientConnection();
 	}
 	
 	private void initializeClientConnection() {
 		
 		try {
			this.output = new ObjectOutputStream(socket.getOutputStream());
 			this.output.flush();
			this.input = new ObjectInputStream(socket.getInputStream());
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 }
