 package com.github.r1j0.statsd.server;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.LinkedBlockingQueue;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.github.r1j0.statsd.backend.Backend;
 import com.github.r1j0.statsd.backend.BackendWorker;
 import com.github.r1j0.statsd.utils.ThreadUtility;
 
 public class FlushThread extends Thread {
 
 	private final Logger log = LoggerFactory.getLogger(getClass());
 
 	private final LinkedBlockingQueue<String> queue;
 	private final List<Backend> backends;
	private final int flushInterval;
 
 
 	public FlushThread(StatsdConfiguration configuration, LinkedBlockingQueue<String> linkedBlockingQueue) {
 		super();
 
 		queue = linkedBlockingQueue;
 		backends = configuration.getBackends();
		flushInterval = configuration.getFlushInterval();
 	}
 
 
 	@Override
 	public void run() {
 		log.info("FlushThread started.");
 		List<String> messages = new ArrayList<String>();
 		final int backendsSize = backends.size();
 
 		while (true) {
 			messages.clear();
 			queueInformation();
 
 			String message = "";
 
 			while ((message = queue.poll()) != null) {
 				log.info("Message taken from the queue: " + message);
 				messages.add(message);
 			}
 
 			if (!messages.isEmpty()) {
 				final List<String> unmodifiableMessages = Collections.unmodifiableList(messages);
 				final ExecutorService executor = Executors.newFixedThreadPool(backendsSize);
 
 				for (Backend backend : backends) {
 					log.info("Notifying backend: " + backend.getClass().getSimpleName());
 
 					final Runnable backendWorker = new BackendWorker(backend, unmodifiableMessages);
 					executor.execute(backendWorker);
 				}
 
 				executor.shutdown();
 
 				while (!executor.isTerminated()) {
 					// No op
 				}
 
 				log.info("Finished backend threads");
 			}
 
			ThreadUtility.doSleep(flushInterval);
 		}
 	}
 
 
 	private void queueInformation() {
 		if (!queue.isEmpty()) {
 			log.info("Queue size is: " + queue.size());
 		} else {
 			log.info("Queue is empty");
 		}
 	}
 }
