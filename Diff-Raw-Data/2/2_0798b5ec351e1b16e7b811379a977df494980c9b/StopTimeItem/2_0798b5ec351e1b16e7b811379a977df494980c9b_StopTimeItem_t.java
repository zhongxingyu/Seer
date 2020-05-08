 package se.chalmers.kangaroo.model.item;
 
 import se.chalmers.kangaroo.model.Item;
 import se.chalmers.kangaroo.model.Kangaroo;
 
 /**
  * This item stops the time for a given interval.
  * 
  * @author pavlov
  * 
  */
 public class StopTimeItem implements Item {
 	private int seconds;
 	private Kangaroo k; 
 	/**
 	 * The constructor takes a Kangaroo and a time that the time is going to
 	 * stop.
 	 * 
 	 * @param k
 	 *            is the Kangaroo
 	 * @param time
 	 *            is the time that the time is going to stop
 	 */
	public StopTimeItem(int millis, int x, int y) {
 		this.seconds = millis * 1000;
 	}
 
 	/**
 	 * When Kangaroo picks up a StopTime-item, a new thread is created to stop
 	 * the time in the given interval.
 	 */
 	@Override
 	public void onPickup(Kangaroo k) {
 		this.k = k;
 		Thread stt = new Thread();
 		stt.setDaemon(true);
 		stt.start();
 
 	}
 
 	/**
 	 * The item will be in use even if Kangaroo pick up a new item and the item
 	 * will stop after given interval, therefore onDrop is never used.
 	 */
 	@Override
 	public void onDrop(Kangaroo k) {
 		// Do nothing, see javadocs.
 	}
 
 	/**
 	 * Because the item will be used on pickup, the onUse will never be used.
 	 */
 	@Override
 	public void onUse(Kangaroo k) {
 		// Do nothing, see javadocs.
 	}
 
 	/**
 	 * This is the thread that is used for StopTimeItem.
 	 * 
 	 * @author pavlov
 	 * 
 	 */
 	class StopTimeRun implements Runnable {
 
 		@Override
 		public void run() {
 			try {
 				k.getTime().pause();
 				Thread.sleep(seconds);
 			} catch (InterruptedException e) {
 				System.out.println("Error, interrupted while sleeping.");
 			} finally {
 				k.getTime().unpause();
 			}
 
 		}
 
 	}
 
 }
