 package Network;
 
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.net.Socket;
 
 public class ClientThread implements Runnable {
 
 	private MultiPlayer mp;
 
 	public ClientThread(MultiPlayer mp) {
 		this.mp = mp;
 	}
 
 	@Override
	public void run() {
 		String ip = mp.clientIp.getText();
 		int port = Integer.parseInt(mp.clientPort.getText());
 
 		try (Socket server = new Socket(ip, port)) {
 			System.out.println("Client verbunden.");
 			mp.chatInput.setEnabled(true);
 
 			ObjectOutputStream out = new ObjectOutputStream(server.getOutputStream());
 			ObjectInputStream in = new ObjectInputStream(server.getInputStream());
 
 
 			ReaderThread reader = new ReaderThread(mp, in);
 			Thread readerThread = new Thread(reader);
 			readerThread.start();
 
 
 			mp.isOver = false;
 			mp.input = new Data();
 			mp.output = new Data();
 
 			while (!mp.isOver) {
 				// Daten schreiben
 				if (mp.messageToSend.length() > 0) {
 					out.reset();
 					mp.output.message = mp.messageToSend;
 					if (mp.messageToSend.contains("/bye")) {
 						mp.output.closeConnection = true;
 						mp.isOver = true;
 					}
 					out.writeObject(mp.output);
 					mp.messageToSend = "";
 				}
 				// Schlaaaaaaaafen ... nur kurz
 				try {
 					Thread.sleep(200);
 				} catch (InterruptedException e) {}
 			} // isOver
 
 		} catch (IOException e) {
 			System.out.println("Clientfehler: " + e.getMessage());
 		}
 
 		System.out.println("Client getrennt!");
 		mp.chatInput.setEnabled(false);
 		mp.stop();
 	}
 
 }
