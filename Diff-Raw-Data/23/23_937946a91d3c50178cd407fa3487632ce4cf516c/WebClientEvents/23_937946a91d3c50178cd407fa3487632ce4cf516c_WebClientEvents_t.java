 package webClient;
 
 import interfaces.Command;
 import interfaces.WebService;
 
 import java.util.ArrayList;
 
 import org.apache.log4j.Level;
 import org.apache.log4j.Logger;
 
 import data.Service;
 
 /**
  * WebServiceClient module implementation.
  * 
  * @author Paul Vlase <vlase.paul@gmail.com>
  */
 public class WebClientEvents extends Thread {
 	static Logger logger = Logger.getLogger(WebClientEvents.class);
 
 	private WebService			web;
 	private Object				monitor;
 	private Boolean				running;
 
 	private ArrayList<Command>	events;
 
 	public WebClientEvents(WebService web) {
 		// TODO: logger.setLevel(Level.OFF);
 		this.web = web;
 
 		events = new ArrayList<Command>();
 		monitor = new Object();
 	}
 
 	@Override
 	public void run() {
 		running = true;
 
 		while (running) {
 			try {
 				synchronized (monitor) {
 					monitor.wait();
 				}
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 			}
 			process();
 		}
 	}
 	
 	public synchronized void stopRunning() {
 		running = false;
 
 		synchronized (monitor) {
 			monitor.notify();
 		}
 	}
 
 	private void process() {
 		logger.debug("Begin");
 
 		for (Command event : events) {
 			event.execute();
 		}
		events.clear();
 
 		logger.debug("End");
 	}
 
 	public synchronized void publishService(Service service) {
 		logger.debug("Begin");
 
 		events.add(new WebAdapter(service, web));
 
 		synchronized (monitor) {
 			monitor.notify();
 		}
 
 		logger.debug("End");
 	}
 
 	public synchronized void publishServices(ArrayList<Service> services) {
 		logger.debug("Begin");
 
 		for (Service service : services) {
 			events.add(new WebAdapter(service, web));
 		}
 
 		synchronized (monitor) {
 			monitor.notify();
 		}
 
 		logger.debug("End");
 	}
 }
