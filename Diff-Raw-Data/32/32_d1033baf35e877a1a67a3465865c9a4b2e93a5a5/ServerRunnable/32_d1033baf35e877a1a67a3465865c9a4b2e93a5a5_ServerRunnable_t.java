 package game;
 
 import java.io.IOException;
 import java.util.concurrent.locks.*;
 
 public class ServerRunnable implements Runnable {
 	private Server theServer;
 	private int i;
 
 	public ServerRunnable(Server s, int i){
 		theServer = s;
 		this.i = i;
 	}
 
 	@Override
 	public void run() {
 
 		// gets Message from client
 		while(true){
 
 
 			theServer.getLock().lock();
 			try {
 				while(theServer.getCount() == 0){
 					theServer.getCondition().await();
 				}
 				Participant p = theServer.getParticipantList().get(i);
 				if (p.isActive()){ // Only try to read from active players;
 					// thread will be responsible for changing 
 					// back to active on reconnect
 					try {
 						p.readMessage();
 					}
 					catch (IOException e) {
 						System.err.println("Participant disconnected on reading message. Set to inactive. " + e.getMessage());
 						p.setActive(false);
 						theServer.setActivePlayerCount(theServer.getActivePlayerCount()-1);
 					}
 				}
 				theServer.setCount(theServer.getCount()-1);
 				if(theServer.getCount() == 0){
 					theServer.getCondition().signalAll();
 				}
 
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 			} finally {
 				theServer.getLock().unlock();
 			}
 
 
 			// sends Message to client
 			theServer.getLock().lock();
 			try {
 				while(theServer.getCount() == 0){
 					theServer.getCondition().await();
 				}
 				Participant p = theServer.getParticipantList().get(i);
 				if (p.isActive()){
 					try {
 						p.writeToClient(theServer.getGson().toJson(theServer.getMessage()));
 					}
 					catch (IOException e) {
 						System.err.println("Participant disconnected while writing message.  Set to inactive. " + e.getMessage());
 						p.setActive(false);
 						theServer.setActivePlayerCount(theServer.getActivePlayerCount()-1);
 					}
 				}
 				theServer.setCount(theServer.getCount()-1);
 				if(theServer.getCount() == 0){
 					theServer.getCondition().signalAll();
 				}
				
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 			} finally {
 				theServer.getLock().unlock();
 			}
 		}
 	}
 
 }
