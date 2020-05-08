 /*
  * TeleStax, Open Source Cloud Communications.
  * Copyright 2012 and individual contributors by the @authors tag. 
  *
  * This is free software; you can redistribute it and/or modify it
  * under the terms of the GNU Lesser General Public License as
  * published by the Free Software Foundation; either version 2.1 of
  * the License, or (at your option) any later version.
  *
  * This software is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this software; if not, write to the Free
  * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
  * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
  */
 
 package org.mobicents.commons.congestion;
 
 import javolution.util.FastList;
 
 import org.apache.log4j.Logger;
 
 /**
  * This Congestion Monitor monitors whether or not the JVM memory usage has crossed 
  * the memoryThreshold and notifies its listeners. 
  * If it has then it monitors if the JVM memory usage has reduced
  * and come under the backToNormalMemoryThreshold and notifies its listeners
  * 
  * @author amit bhayani
  * @author jean.deruelle@gmail.com
  * 
  */
 public class MemoryCongestionMonitor implements CongestionMonitor {
 	private static final Logger logger = Logger.getLogger(MemoryCongestionMonitor.class);
 
 	private static final String SOURCE = "MEMORY";
 
 	private final FastList<CongestionListener> listeners = new FastList<CongestionListener>();
 
 	private double maxMemory;
 	private volatile double percentageOfMemoryUsed;
 
 	private volatile boolean memoryTooHigh = false;
 
 	private int backToNormalMemoryThreshold;
 
 	private int memoryThreshold;
 
 	public MemoryCongestionMonitor() {
 		maxMemory = Runtime.getRuntime().maxMemory() / (double) 1024;
 	}
 
 	/**
 	 * @param backToNormalPercentageOfMemoryUsed
 	 *            the backToNormalPercentageOfMemoryUsed to set
 	 */
 	public void setBackToNormalMemoryThreshold(int backToNormalMemoryThreshold) {
 		this.backToNormalMemoryThreshold = backToNormalMemoryThreshold;
 		if (logger.isInfoEnabled()) {
 			logger.info("Back To Normal Memory threshold set to " + backToNormalMemoryThreshold + "%");
 		}
 	}
 
 	/**
 	 * @return the backToNormalPercentageOfMemoryUsed
 	 */
 	public int getBackToNormalMemoryThreshold() {
 		return backToNormalMemoryThreshold;
 	}
 
 	/**
 	 * @param memoryThreshold
 	 *            the memoryThreshold to set
 	 */
 	public void setMemoryThreshold(int memoryThreshold) {
 		this.memoryThreshold = memoryThreshold;
 		if (logger.isInfoEnabled()) {
 			logger.info("Memory threshold set to " + this.memoryThreshold + "%");
 		}
 	}
 
 	/**
 	 * @return the memoryThreshold
 	 */
 	public int getMemoryThreshold() {
 		return memoryThreshold;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see org.mobicents.commons.congestion.CongestionMonitor#monitor()
 	 */
 	@Override
 	public void monitor() {
 		Runtime runtime = Runtime.getRuntime();
 
 		double allocatedMemory = runtime.totalMemory() / (double) 1024;
 		double freeMemory = runtime.freeMemory() / (double) 1024;
 
 		double totalFreeMemory = freeMemory + (maxMemory - allocatedMemory);
 
 		this.percentageOfMemoryUsed = (((double) 100) - ((totalFreeMemory / maxMemory) * ((double) 100)));
 
		if(logger.isTraceEnabled()) {
			logger.trace("Percentage of Memory Used = " + percentageOfMemoryUsed);
		}
 		if (this.memoryTooHigh) {
 			if (this.percentageOfMemoryUsed < this.backToNormalMemoryThreshold) {
 				logger.warn("Memory used: " + percentageOfMemoryUsed + "% < to the back to normal memory threshold : " + this.backToNormalMemoryThreshold);
 				this.memoryTooHigh = false;
 
 				// Lets notify the listeners
 				for (FastList.Node<CongestionListener> n = listeners.head(), end = listeners.tail(); (n = n.getNext()) != end;) {
 					CongestionListener listener = n.getValue();
 					listener.onCongestionFinish(SOURCE);
 				}
 			}
 		} else {
 			if (this.percentageOfMemoryUsed > memoryThreshold) {
 				logger.warn("Memory used: " + percentageOfMemoryUsed + "% > to the memory threshold : " + this.memoryThreshold);
 				this.memoryTooHigh = true;
 
 				// Lets notify the listeners
 				for (FastList.Node<CongestionListener> n = listeners.head(), end = listeners.tail(); (n = n.getNext()) != end;) {
 					CongestionListener listener = n.getValue();
 					listener.onCongestionStart(SOURCE);
 				}
 			}
 		}
 
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see org.mobicents.commons.congestion.CongestionMonitor#addCongestionListener(org.mobicents.commons.congestion.CongestionListener)
 	 */
 	@Override
 	public void addCongestionListener(CongestionListener listener) {
 		this.listeners.add(listener);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see org.mobicents.commons.congestion.CongestionMonitor#removeCongestionListener(org.mobicents.commons.congestion.CongestionListener)
 	 */
 	@Override
 	public void removeCongestionListener(CongestionListener listener) {
 		this.listeners.remove(listener);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see org.mobicents.commons.congestion.CongestionMonitor#getSource()
 	 */
 	@Override
 	public String getSource() {
 		return SOURCE;
 	}
 
 }
