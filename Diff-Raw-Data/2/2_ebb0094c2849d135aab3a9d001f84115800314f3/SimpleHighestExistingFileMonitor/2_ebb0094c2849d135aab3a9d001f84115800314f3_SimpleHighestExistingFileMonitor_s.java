 /*-
  * Copyright © 2011 Diamond Light Source Ltd.
  *
  * This file is part of GDA.
  *
  * GDA is free software: you can redistribute it and/or modify it under the
  * terms of the GNU General Public License version 3 as published by the Free
  * Software Foundation.
  *
  * GDA is distributed in the hope that it will be useful, but WITHOUT ANY
  * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
  * details.
  *
  * You should have received a copy of the GNU General Public License along
  * with GDA. If not, see <http://www.gnu.org/licenses/>.
  */
 
 package gda.device.detectorfilemonitor.impl;
 
 import gda.device.detectorfilemonitor.HighestExistingFileMonitor;
 import gda.device.detectorfilemonitor.HighestExistingFileMonitorData;
 import gda.device.detectorfilemonitor.HighestExitingFileMonitorSettings;
 import gda.factory.Findable;
 import gda.observable.IObserver;
 import gda.observable.ObservableComponent;
 
 import java.io.File;
 import java.util.concurrent.Executors;
 import java.util.concurrent.ScheduledExecutorService;
 import java.util.concurrent.TimeUnit;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.InitializingBean;
 
 /**
  * Implementation of HighestExistingFileMonitor that checks for file existence.
  */
 public class SimpleHighestExistingFileMonitor implements HighestExistingFileMonitor, Findable, InitializingBean {
 	private static final Logger logger = LoggerFactory.getLogger(SimpleHighestExistingFileMonitor.class);
 	ObservableComponent obsComp = new ObservableComponent();
 
 	public SimpleHighestExistingFileMonitor(){}
 	private long delay = 1000; // in ms
 
 	HighestExitingFileMonitorSettings highestExitingFileMonitorSettings = null;
 	HighestExistingFileMonitorData highestExistingFileMonitorData = null;
 
 	@Override
 	public HighestExitingFileMonitorSettings getHighestExitingFileMonitorSettings() {
 		return highestExitingFileMonitorSettings;
 	}
 
 	@Override
 	public void setHighestExitingFileMonitorSettings(HighestExitingFileMonitorSettings highestExitingFileMonitorSettings) {
 		this.highestExitingFileMonitorSettings = highestExitingFileMonitorSettings;
 	}
 
 	@Override
 	public long getDelayInMS() {
 		return delay;
 	}
 
 	@Override
 	public void setDelayInMS(long delay) {
 		if (delay > 0) {
 			this.delay = delay;
 		}
 	}
 
 	Integer latestNumberFound = null;
 	boolean running = false;
 	private ScheduledExecutorService scheduler;
 
 	Runnable runnable = new Runnable() {
 
 		int numberToLookFor;
 		String fileToLookFor;
 		HighestExitingFileMonitorSettings highestExitingFileMonitorSettings_InUse = null;
 		Integer numberFound = null;
 		String templateInUse="";
 
 		@Override
 		public void run() {
 			try {
 				if (!running)
 					return;
 				if (highestExitingFileMonitorSettings_InUse != highestExitingFileMonitorSettings) {
 					latestNumberFound = null;
 					highestExitingFileMonitorSettings_InUse = highestExitingFileMonitorSettings;
 					numberToLookFor = highestExitingFileMonitorSettings_InUse.startNumber;
					final String templateInUse = highestExitingFileMonitorSettings_InUse.getFullTemplate();
 					numberFound = null;
 					if (highestExitingFileMonitorSettings_InUse == null) {
 						running = false;
 						fileToLookFor = null;
 					} else {
 						fileToLookFor = String.format(templateInUse,numberToLookFor);
 					}
 				}
 				if (fileToLookFor != null) {
 					while ((new File(fileToLookFor)).exists()) {
 						numberFound = numberToLookFor;
 						numberToLookFor++;
 						fileToLookFor = String.format(templateInUse,numberToLookFor);
 					}
 				}
 				setLatestNumberFound(numberFound);
 			} catch (Throwable th) {
 				logger.error("Error looking for file using template `"
 						+ highestExitingFileMonitorSettings_InUse.fileTemplate + "` number=" + numberToLookFor, th);
 			} finally{
 				if (running) {
 					scheduler.schedule(runnable, delay, TimeUnit.MILLISECONDS);
 				}
 			}
 		}
 	};
 
 	@Override
 	public void addIObserver(IObserver anIObserver) {
 		obsComp.addIObserver(anIObserver);
 	}
 
 	@Override
 	public void deleteIObserver(IObserver anIObserver) {
 		obsComp.deleteIObserver(anIObserver);
 	}
 
 	@Override
 	public void deleteIObservers() {
 		obsComp.deleteIObservers();
 	}
 
 	@Override
 	public boolean isRunning() {
 		return running;
 	}
 
 	@Override
 	public void setRunning(boolean running) {
 		if (!configured) {
 			this.running = running;
 			return;
 		}
 		if (isRunning() == running)
 			return; // do nothing
 
 		if (running) {
 			start();
 		} else {
 			stop();
 		}
 
 	}
 
 	private final Object stop_lock = new Object();
 
 	private void stop() {
 		synchronized (stop_lock) {
 			running = false;
 			if (scheduler != null) {
 				scheduler.shutdownNow();
 				scheduler = null;
 			}
 		}
 	}
 
 	private void start() {
 		stop();
 		if (scheduler == null) {
 			scheduler = Executors.newScheduledThreadPool(1);
 		}
 		running = true;
 		scheduler.submit(runnable);
 	}
 
 	protected void setLatestNumberFound(Integer numberFound) {
 		if (numberFound != latestNumberFound){
 			if (numberFound == null || !numberFound.equals(latestNumberFound)) {
 				latestNumberFound = numberFound;
 				obsComp.notifyIObservers(this, new HighestExistingFileMonitorData(highestExitingFileMonitorSettings,
 						latestNumberFound));
 			}
 		}
 	}
 
 	boolean configured = false;
 
 	@Override
 	public void afterPropertiesSet() throws Exception {
 		configured = true;
 		boolean start = isRunning();
 		if (start) {
 			running = false;
 			setRunning(true);
 		}
 	}
 
 	String name;
 
 	@Override
 	public String getName() {
 		return name;
 	}
 
 	@Override
 	public void setName(String name) {
 		this.name = name;
 	}
 
 	@Override
 	public HighestExistingFileMonitorData getHighestExistingFileMonitorData() {
 		return new HighestExistingFileMonitorData(highestExitingFileMonitorSettings, latestNumberFound);
 	}
 
 }
