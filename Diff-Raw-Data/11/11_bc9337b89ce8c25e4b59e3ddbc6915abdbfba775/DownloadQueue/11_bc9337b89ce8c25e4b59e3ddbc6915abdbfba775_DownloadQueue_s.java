 package org.mcupdater;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.List;
 import java.util.Queue;
 import java.util.concurrent.ConcurrentLinkedQueue;
 import java.util.concurrent.ThreadPoolExecutor;
 import java.util.concurrent.atomic.AtomicInteger;
 
 public class DownloadQueue {
 
 	private final Queue<Downloadable> processQueue = new ConcurrentLinkedQueue<>();
 	private final List<Downloadable> fullList = Collections.synchronizedList(new ArrayList<Downloadable>());
 	private final List<Downloadable> failureList = Collections.synchronizedList(new ArrayList<Downloadable>());
 	private final List<Downloadable> successList = Collections.synchronizedList(new ArrayList<Downloadable>());
 	private final List<ProgressTracker> trackers = Collections.synchronizedList(new ArrayList<ProgressTracker>());
 	private final TrackerListener listener;
 	private final String name;
 	private final AtomicInteger threadPoolRemain = new AtomicInteger();
 	private final File basePath;
 	private final File cachePath;
 	private boolean active;
 	private String parent;
 	private String mcUser = null;
 
 	public DownloadQueue(String name, String parent, TrackerListener listener, Collection<Downloadable> queue, File basePath, File cachePath, String mcUser) {
 		this(name,parent,listener,queue,basePath,cachePath);
 		this.mcUser  = mcUser;
 	}
 	public DownloadQueue(String name, String parent, TrackerListener listener, Collection<Downloadable> queue, File basePath, File cachePath) {
 		this.name = name;
 		this.parent = parent;
 		this.listener = listener;
 		if (queue != null) {
 			addToQueue(queue);
 		}
 		this.basePath = basePath;
 		this.cachePath = cachePath;
 	}
 	
 	private void addToQueue(Collection<Downloadable> queue) {
 		if (this.active) {
 			throw new IllegalStateException("Download queue already active");
 		}
 		this.fullList.addAll(queue);
 		this.processQueue.addAll(queue);
 		for (Downloadable entry : queue) {
 			this.trackers.add(entry.getTracker());
 			entry.getTracker().setTotal(100000L);
 			entry.getTracker().setQueue(this);
 		}
 	}
 	
 	public void processQueue(ThreadPoolExecutor executor) {
 		if (this.active){
 			throw new IllegalStateException("Queue is already in progress");
 		}
 		this.active = true;
 		
 		if (this.fullList.isEmpty()) {
 			printMessage(parent + " - " + name + " - No files in queue");
 			this.threadPoolRemain.set(1);
 			executor.submit(new Runnable(){
 
 				@Override
 				public void run() {
 					DownloadQueue.this.iterateQueue();
					DownloadQueue.this.listener.printMessage(parent + " - " + name + " - Thread finished.");
 				}
 			});
 		} else {
 			int maxPool = executor.getMaximumPoolSize();
 			this.threadPoolRemain.set(maxPool);
 			//this.listener.printMessage("Pool size: " + maxPool);
 			for (int threadCount = 0; threadCount < maxPool; threadCount++) {
 				executor.submit(new Runnable(){
 					@Override
 					public void run() {
 						DownloadQueue.this.iterateQueue();
						DownloadQueue.this.listener.printMessage(parent + " - " + name + " - Thread finished.");
 					}
 				});
 			}
 		}
 	}
 
 	protected void iterateQueue() {
 		Downloadable entry;
 		while ((entry = this.processQueue.poll()) != null) {
 			this.listener.printMessage("Downloading: " + entry.getFriendlyName());
 			try {
 				entry.download(basePath, cachePath);
 				synchronized (this.successList) {
 					this.successList.add(entry);					
 				}
				this.listener.printMessage("Download success");
 			} catch (Exception e) {
 				this.listener.printMessage(entry.getFriendlyName() + " failed: " + e.getMessage());
 				this.failureList.add(entry);
 			}
 		}
 		if (this.threadPoolRemain.decrementAndGet() <= 0){
 			this.listener.onQueueFinished(this);
 		}
		this.listener.printMessage(this.parent + " - " + this.name + " - Remaining threads: " + this.threadPoolRemain.get());
 	}
 
 	public void updateProgress() {
 		this.listener.onQueueProgress(this);
 	}
 	
 	public boolean isActive() {
 		return this.active;
 	}
 	
 	public boolean isFinished() {
 		return this.active && (this.processQueue.isEmpty()) && (this.threadPoolRemain.get() == 0);
 	}
 	
 	public float getProgress() {
 		float current = 0.0F;
 		float total = 0.0F;
 		
 		synchronized (this.trackers) {
 			for (ProgressTracker tracker : this.trackers) {
 				total += (float)tracker.getTotal();
 				current += (float)tracker.getCurrent();
 			}
 		}
 		
 		float result = -1.0F;
 		if (total > 0.0F) {
 			result = current/total;
 		}
 		return result;
 	}
 
 	public String getName() {
 		return name;
 	}
 
 	public void printMessage(String msg) {
 		this.listener.printMessage(msg);
 	}
 	
 	public List<Downloadable> getFailures() {
 		return this.failureList;
 	}
 	
 	public List<Downloadable> getSuccesses() {
 		return this.successList;
 	}
 	
 	public int getTotalFileCount(){
 		synchronized(this.fullList) {
 			return this.fullList.size();
 		}
 	}
 	
 	public int getSuccessFileCount(){
 		synchronized(this.successList) {
 			return this.successList.size();
 		}
 	}
 	
 	public int getFailedFileCount(){
 		synchronized(this.failureList) {
 			return this.failureList.size();
 		}
 	}
 
 	public File getCachePath() {
 		return cachePath;
 	}
 
 	public String getParent() {
 		return parent;
 	}
 
 	public String getMCUser() {
 		return mcUser;
 	}
 }
