 package at.edu.hti.concurrency.prodcon;
 
 import at.edu.hti.concurrency.stores.LinkedListStore;
 
 public class ThreadProducerConsumerStarter implements ProducerConsumerStarter {
 
 	Configuration config = null;
 	LinkedListStore store = null;
 	
 	final Object locker = new Object(); 
 
 	long numberObjectsProduced = 0;
 	long numberObjectsConsumed = 0;
 
 	@Override
 	public void setUpTest(Configuration configuration) {
 		this.config = configuration;
 	}
 
 	@Override
 	public long runProducerConsumerTest() {
 		store = new LinkedListStore();
 		store.initMaxSize(config.getBufferSize());
 
 		numberObjectsConsumed = 0;
 		numberObjectsProduced = 0;
 
 		long startTime = System.currentTimeMillis();
 
 		for (int i = 0; i < config.getNumberOfConsumers(); i++) {
 			Consumer c = new Consumer();
 			Thread t = new Thread(c, "Consumer-" + i);
 			t.start();
 		}
 
 		for (int i = 0; i < config.getNumberOfProcuders(); i++) {
 			Producer p = new Producer();
 			Thread t = new Thread(p, "Producer-" + i);
 			t.start();
 		}
 		
		while (numberObjectsConsumed < config.getNumberOfItems()) {
 			try {
 				Thread.sleep(1);
 			} catch (InterruptedException e) {
 			}
 		}
 
 		return System.currentTimeMillis() - startTime;
 	}
 
 	class Producer implements Runnable {
 
 		@Override
 		public void run() {
 			while (numberObjectsProduced < config.getNumberOfItems()) {
 
 				synchronized (locker) {
 					if (store.size() < config.getBufferSize()) {
 						String produced = "String-" + numberObjectsProduced++;
 						System.out.println("Produced at "+Thread.currentThread().getName()+": " + produced);
 						store.addFirst(produced);
 						locker.notifyAll();
 					} else {
 						try {
 							locker.wait();
 						} catch (InterruptedException e) {
 						}
 					}
 
 				}
 			}
 		}
 
 	}
 
 	class Consumer implements Runnable {
 
 		@Override
 		public void run() {
 
 			while (numberObjectsConsumed < config.getNumberOfItems()) {
 				synchronized (locker) {
 					if (store.size() > 0) {
 						String consumed = store.removeLast();
 						numberObjectsConsumed++;
 						System.out.println("Consumed at "+Thread.currentThread().getName()+": " + consumed);
 						locker.notifyAll();
 					} else {
 						try {
 							locker.wait();
 						} catch (InterruptedException e) {
 						}
 					}
 				}
 			}
 		}
 
 	}
 
 	public static void main(String[] args) {
 		ProducerConsumerStarter test = new ThreadProducerConsumerStarter();
 
 		Configuration config = new Configuration(10, 10, 5, 1000);
 
 		test.setUpTest(config);
 		long time = test.runProducerConsumerTest();
 
 		System.out.println("Result: " + time + " : " + config);
 	}
 
 }
