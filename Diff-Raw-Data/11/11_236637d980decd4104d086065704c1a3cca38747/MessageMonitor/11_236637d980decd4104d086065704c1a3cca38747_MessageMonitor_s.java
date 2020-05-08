 package de.tr0llhoehle.buschtrommel.network;
 
 import java.util.Vector;
 
 import de.tr0llhoehle.buschtrommel.models.Message;
 
 public class MessageMonitor {
 	Vector<IMessageObserver> observers = new Vector<>();
 	
	/* (non-Javadoc)
	 * @see de.tr0llhoehle.buschtrommel.network.IMessageMonitor#registerObserver(de.tr0llhoehle.buschtrommel.network.IMessageObserver)
	 */
	@Override
 	public void registerObserver(IMessageObserver observer) {
 		this.observers.add(observer);
 	}
 	
	/* (non-Javadoc)
	 * @see de.tr0llhoehle.buschtrommel.network.IMessageMonitor#removeObserver(de.tr0llhoehle.buschtrommel.network.IMessageObserver)
	 */
	@Override
 	public void removeObserver(IMessageObserver observer) {
 		this.observers.remove(observer);
 	}
 	
 	/**
 	 * Send the specified message to each registered observer. Each operation gets his own thread.
 	 * @param message the specified message
 	 */
 	protected void sendMessageToObservers(Message message) {
 		for(IMessageObserver observer : observers) {
 			new MessageThread(message, observer).start();
 		}
 	}
 }
 
 class MessageThread extends Thread {
 	private Message message;
 	private IMessageObserver observer;
 	public MessageThread(Message message, IMessageObserver observer) {
 		this.message = message;
 		this.observer = observer;
 	}
 	
 	public void run() {
 		this.observer.receiveMessage(message);
 	}
 }
