 package org.mbj.performance;
 
 import java.util.List;
 import java.util.Observable;
 import java.util.Observer;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class Loader implements Observer {
 	private static Logger log = LoggerFactory.getLogger(Loader.class);
 	private List<Client> clients;
 	private int countDown;
 	private long totalTime;
 	private long totalNbOfExecutions;
 
 	public Loader(List<Client> clients) {
 		super();
 		this.clients = clients;
 		this.countDown = clients.size();
 		this.totalTime = 0;
 		this.totalNbOfExecutions = 0;
 	}
 
 	public long getTotalTime() {
		return totalTime;
 	}
 
 	public void start() {
 		for (Client client : clients) {
 			client.addObserver(this);
 			new Thread(client).start();
 			log.debug("Started client {}", client.getId());
 		}
 		synchronized (this) {
 			while (countDown > 0) {
 				try {
 					this.wait();
 				} catch (InterruptedException e) {
 					log.error("", e);
 				}
 			}
 		}
 		log.debug("Total time: {} ms.", totalTime);
 		log.debug("Total number of executions: {}.", totalNbOfExecutions);
 		log.debug("Average time/execution: {} ms.", (((double)totalTime) / ((double)totalNbOfExecutions)));
		log.debug("Average executions/s: {}.", (((double)totalNbOfExecutions) / ((double)totalTime)) * 1000);
 	}
 
 	public void update(Observable observable, Object time) {
 		synchronized (this) {
 			Client client = (Client)observable;
 			long clientTime = (Long) time;
 			log.debug("Finished client {} in {} ms.", client.getId(), clientTime);
 			totalTime += clientTime;
 			totalNbOfExecutions += client.getNbOfExecutions();
 			countDown--;
 			this.notifyAll();
 		}
 	}
 }
