 package Network;
 
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.net.ServerSocket;
 import java.net.Socket;
 
 public class ServerThread implements Runnable{
 
 	MultiPlayer mp;
 
 	public ServerThread(MultiPlayer mp) {
 		this.mp = mp;
 	}
 
 	@Override
	public synchronized void run() {
 		System.out.println("Server gestartet. Auf Clients warten ...");
 		int port = Integer.parseInt(mp.serverPort.getText());
 
 
 		while (mp.serverClientThread == Thread.currentThread()) {
 
 			try (ServerSocket server = new ServerSocket(port)) {
 
 			Socket client = server.accept();
 			mp.serverIsConnectedToClient = true;
 			mp.chatInput.setEnabled(true);
 			System.out.println("Verbunden mit Client " + client.getLocalSocketAddress());
 
 			ObjectOutputStream out = new ObjectOutputStream(client.getOutputStream());
 			ObjectInputStream in = new ObjectInputStream(client.getInputStream());
 
 
 			ReaderThread reader = new ReaderThread(mp, in);
 			Thread readerThread = new Thread(reader);
 			readerThread.start();
 
 
 			mp.isOver = false;
 			mp.input = new Data();
 			mp.output = new Data();
 
 			while (!mp.isOver) {
 				// Daten schreiben
 				if (mp.messageToSend.length() > 0) {
					System.out.print("["+mp.messageToSend+"] ");
 					out.reset();
 					mp.output.message = mp.messageToSend;
 					if (mp.messageToSend.contains("/bye")) {
 						mp.output.closeConnection = true;
 						mp.isOver = true;
 					}
 					out.writeObject(mp.output);
					System.out.println("senden: " + mp.output.message);
 					mp.messageToSend = "";
 				}
 				// Schlaaaaaaaafen ... nur kurz
 				try {
 					Thread.sleep(200);
 				} catch (InterruptedException e) {}
 			} // isOver
 
 			} catch (IOException e) {
 				System.out.println("Serverfehler: " + e.getMessage());
 				break;
 			}
 
 			System.out.println("Client getrennt.");
 			mp.serverIsConnectedToClient = false;
 			mp.chatInput.setEnabled(false);
 
 		} // Ende von While
 
 		System.out.println("Server beendet.");
 		mp.stop();
 	}
 
 }
