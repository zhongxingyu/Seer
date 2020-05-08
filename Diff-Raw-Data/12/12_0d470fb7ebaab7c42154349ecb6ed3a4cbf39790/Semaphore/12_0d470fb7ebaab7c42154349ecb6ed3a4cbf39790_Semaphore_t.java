 package synchronisation_primitives;
 
 public class Semaphore {
 	private int count;
 
 	public Semaphore(int initial) {
 		count = initial;
 	}
 
 	public synchronized void up() {
		if (count < 0) {
 			this.notify();
 		}
		count++;
 	}
 
 	public synchronized void down() {
		count--;
		if (count < 0) {
 			try {
 				this.wait();
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 }
