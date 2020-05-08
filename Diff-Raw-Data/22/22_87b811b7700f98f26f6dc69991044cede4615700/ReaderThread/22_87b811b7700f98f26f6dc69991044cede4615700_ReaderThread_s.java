 package Network;
 
 import java.io.IOException;
 import java.io.ObjectInputStream;
 
 public class ReaderThread implements Runnable {
 
 	private MultiPlayer mp;
 	private ObjectInputStream in;
 
 	public ReaderThread(MultiPlayer mp, ObjectInputStream in) {
 		this.mp = mp;
 		this.in = in;
 	}
 
 	@Override
	public synchronized void run() {
 		while (!mp.isOver) {
 			try {
 				if ((mp.input = (Data) in.readObject()) != null) {
 					if (mp.input.closeConnection) {
 						mp.isOver = true;
 					}
 					mp.appendChatMessage(mp.input.message, MultiPlayer.OPPONENT);
 				}
 			} catch (IOException | ClassNotFoundException e) {
 				mp.isOver = true;
 			}
 		}
 	}
 
 }
