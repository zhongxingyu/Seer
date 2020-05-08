 package at.ac.tuwien.complang.carfactory.application;
 
 import java.util.Hashtable;
 import java.util.Map;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import org.mozartspaces.core.Capi;
 
 import org.mozartspaces.core.ContainerReference;
 
 public class FactoryFacade {
 	
 	//Static Fields
 	private static Map<ProducerType, FactoryFacade> factories;
 	
 	static {
 		factories = new Hashtable<ProducerType, FactoryFacade>();
 	}
 	
 	//Fields
 	private int count;
 	private boolean running = false;
 	private Thread thread;
 	private IProducer producer;
 	
 	private FactoryFacade(ProducerType type, Capi capi, ContainerReference cref) {
 		switch(type) {
 			case BODY: producer = new BodyFactory(capi, cref); break;
 			case WHEEL: producer = new WheelFactory(capi, cref); break;
 			case MOTOR: producer = new MotorFactory(capi, cref); break;
 			default: throw new IllegalArgumentException("Specificed ProducerType is not implemented");
 		}
 	}
 	
 	public static FactoryFacade getInstance(ProducerType type, Capi capi, ContainerReference cref) {
 		if(factories.get(type) == null) {
 			synchronized(FactoryFacade.class) {
 				if(factories.get(type) == null) {
 					FactoryFacade.factories.put(type, new FactoryFacade(type, capi, cref));
 				}
 			}
 		}
 		return factories.get(type);
 	}
 	
 	public void start() {
 		this.running = true;
 		//start the timer task and produce bodies
 		this.thread.start();
 	}
 	
 	public void stop() {
 		this.count = 0;
 		this.thread.interrupt();
 		this.running = false;
 	}
 	
 	public void init(int count) throws IllegalStateException {
 		if(running) {
 			throw new IllegalStateException("Factory must be stopped first");
 		}
 		
 		this.count = count;
 		
 		//Prepare TimerTask
 		thread = new Thread(new Producer());
 	}
 	
 	class Producer implements Runnable {
 
 		public void run() {
 			int originalCount = count;
 			int delay = 0;
 			int total = 0;
 			while(count > 0) {
 				//The producer sleeps for a random period between 1 and 3 seconds
 				delay = (int) (Math.random() * 3) + 1;
 				total += delay;
 				int millisecondsPerSecond = 1000;
 				try {
 					Thread.sleep(delay * millisecondsPerSecond);
 				} catch (InterruptedException e) {
 					System.err.println("Producer was interrupted.");
 				}
 				//TODO: Produce a Body...
 				count--;
 				producer.produce();
 				System.out.println("Time to produce was " + delay + " seconds. Parts remaining: " + count);
 			}
			System.out.println("All done. Average time to produce: " + total / (double) originalCount);
 		}
 
 	}
 
 }
