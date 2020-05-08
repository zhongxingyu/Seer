 package org.omo.core;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Timer;
 import java.util.TimerTask;
 
 public class Feed<K, V> extends AbstractModel<K, V> {
 
 	// Feed
 	
 	public interface Strategy<K, V> {
 		
 		K getKey(V item);
 		Collection<V> createNewValues(Range<K> range);
 		V createNewVersion(V original);
 		
 	}
 	
 	protected final Map<K, V> vs = new HashMap<K, V>();
 	protected final Range<K> range;
 	protected final long delay;
 	protected final Strategy<K, V> strategy;
 	protected final Timer timer = new Timer();
 	protected final TimerTask task = new TimerTask() {
 		
 		@Override
 		public void run() {
 			K id = range.random();
 			V oldValue = vs.get(id);
 			V newValue = strategy.createNewVersion(oldValue);
 			vs.put(strategy.getKey(newValue), newValue);
 			logger.trace("{}: new version: {}", name, newValue);
 			notifyUpdates(empty, Collections.singleton(new Update<V>(oldValue, newValue)), empty);
 		}
 	};
 	
 	public Feed(String name, Metadata<K, V> metadata, Range<K> range, long delay, Strategy<K, V> feedStrategy) {
 		super(name, metadata);
 		this.range = range;
 		this.delay = delay;
 		this.strategy = feedStrategy;
 	}
 
 	// Lifecycle
 	
 	// IDEAS
 	// get range stuff going
 	// use topics as well as queues
 	// reference count listeners only
 	// make void invocations async - no return value
 	
 	protected Collection<Update<V>> empty = new ArrayList<Update<V>>();
 	
 	@Override
 	public void start() {
 		logger.info("creating values...");
 		Collection<V> newValues = strategy.createNewValues(range);
 		Collection<Update<V>> insertions = new ArrayList<Update<V>>();
 		for (V newValue : newValues) {
 			vs.put(strategy.getKey(newValue), newValue);
 			insertions.add(new Update<V>(null, newValue));
 		}
 		logger.info("notifying {} values...", newValues.size());
 		notifyUpdates(insertions, empty, empty);
 		logger.info("starting timer...");
		//timer.scheduleAtFixedRate(task, 0, delay);
 	}
 	
 	@Override
 	public void stop() {
 		timer.cancel();
 	}
 	
 	// Model
 	
 	protected Collection<V> getData() {
 		return new ArrayList<V>(vs.values());
 	}
 	
 }
