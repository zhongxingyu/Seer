 package synchronisation_primitives;
 
 public class Semaphore {
 	private int count;
 
 	public Semaphore(int initial) {
 		count = initial;
 	}
 
 	public synchronized void up() {
		if (count >= 0) {
			count++;
		} else {
 			this.notify();
 		}
 	}
 
 	public synchronized void down() {
		if (count > 0) {
			count++;
		} else {
 			try {
 				this.wait();
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 }
