 package ch2.ex3;
 
 import java.util.Date;
 import java.util.LinkedList;
 import java.util.List;
 
 public class EventStorage {
 
 	private int maxSize;
 	private List<Date> storage;
 
 	public EventStorage() {
 		maxSize = 10;
 		storage = new LinkedList<Date>();
 	}
 
 	public synchronized void set() {
 		while (storage.size() == maxSize) {
 			try {
 				System.out.println(maxSize);
 				wait();
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 			}
 		}
		((LinkedList<Date>) storage).offer(new Date());
		System.out.printf("Set: %d", storage.size());
		notifyAll();
 	}
	
 
 	public synchronized void get() {
 		while (storage.size() == 0) {
 			try {
 				System.out.println(maxSize);
 				wait();
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 			}
 		}
 		System.out.printf("Get: %d: %s", storage.size(),
 				((LinkedList<Date>) storage).poll());
 		notifyAll();
 	}
 }
