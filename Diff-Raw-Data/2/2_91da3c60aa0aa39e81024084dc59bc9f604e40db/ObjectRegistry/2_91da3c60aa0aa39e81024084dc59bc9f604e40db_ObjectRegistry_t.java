 package com.elegantmush.objectcounter;
 
 import static java.util.concurrent.TimeUnit.SECONDS;
 
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.ConcurrentMap;
 import java.util.concurrent.Executors;
 import java.util.concurrent.ScheduledExecutorService;
 
 public enum ObjectRegistry {
 
 	INSTANCE;
 
 	private static final int REPORT_DELAY_SECS = 2;
 
	private ConcurrentMap<String, Integer> registry = new ConcurrentHashMap<String, Integer>();
 
 	private final ScheduledExecutorService scheduler = Executors
 			.newScheduledThreadPool(1);
 
 	ObjectRegistry() {
 		reportPeriodically();
 	}
 
 	public Integer get(String key) {
 		return registry.get(key);
 	}
 
 	public Integer putIfAbsent(String key, Integer value) {
 		return registry.putIfAbsent(key, value);
 	}
 
 	public Integer put(String key, Integer value) {
 		return registry.put(key, value);
 	}
 
 	public void reportPeriodically() {
 
 		final Runnable reporter = new Runnable() {
 			public void run() {
 				System.out.println("======================================");
 				System.out.println("==========OBJECT COUNTER==============");
 				System.out.println("======================================");
 				for (String k : registry.keySet()) {
 					System.out.printf("object: %s count: %d%n", k,
 							registry.get(k));
 				}
 			}
 		};
 		scheduler.scheduleAtFixedRate(reporter, REPORT_DELAY_SECS,
 				REPORT_DELAY_SECS, SECONDS);
 	}
 }
