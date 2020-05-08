 package org.vpac.grisu.model;
 
 import java.util.List;
 import java.util.Set;
 
 public interface GridResource extends Comparable {
 
 	public abstract double getSiteLatitude();
 
 	public abstract double getSiteLongitude();
 
 	public abstract String getSiteName();
 
 	public abstract String getApplicationName();
 
 	public abstract List<String> getAvailableApplicationVersion();
 
 	public abstract int getFreeJobSlots();
 
 	public abstract int getRunningJobs();
 
 	public abstract int getWaitingJobs();
 
 	public abstract int getTotalJobs();
 
 	public abstract int getMainMemoryRAMSize();
 
 	public abstract int getMainMemoryVirtualSize();
 
 	public abstract int getSmpSize();
 
 	public abstract String getContactString();
 
 	public abstract String getJobManager();
 
 	public abstract boolean isDesiredSoftwareVersionInstalled();
 
 	public abstract int getRank();
 
 	public abstract String getQueueName();
 	
 	public abstract Set<String> getAllExecutables();
	
 }
