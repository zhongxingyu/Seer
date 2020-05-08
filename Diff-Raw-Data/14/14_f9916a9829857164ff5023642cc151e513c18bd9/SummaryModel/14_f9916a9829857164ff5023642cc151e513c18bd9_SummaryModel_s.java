 package com.fornacif.osgi.manager.internal.models;
 
import java.text.DateFormat;
import java.text.SimpleDateFormat;
 
 import javafx.beans.property.IntegerProperty;
 import javafx.beans.property.LongProperty;
 import javafx.beans.property.SimpleIntegerProperty;
 import javafx.beans.property.SimpleLongProperty;
 import javafx.beans.property.SimpleStringProperty;
 import javafx.beans.property.StringProperty;
 
 public class SummaryModel {
 	
 	private final StringProperty name = new SimpleStringProperty();
 	private final StringProperty arch = new SimpleStringProperty();
 	private final IntegerProperty availableProcessors = new SimpleIntegerProperty();
 	private final LongProperty upTime = new SimpleLongProperty();
 	private final IntegerProperty bundlesCount = new SimpleIntegerProperty();
 	private final IntegerProperty servicesCount = new SimpleIntegerProperty();
 	private final IntegerProperty standardServicesCount = new SimpleIntegerProperty();
 	private final IntegerProperty inUseServicesCount = new SimpleIntegerProperty();
 
 	public void setName(String name) {
 		this.name.set(name);
 	}
 	
 	public String getArch() {
 		return arch.get();
 	}
 
 	public void setArch(String arch) {
 		this.arch.set(arch);
 	}
 	
 	public Integer getAvailableProcessors() {
 		return availableProcessors.get();
 	}
 
 	public void setAvailableProcessors(Integer availableProcessors) {
 		this.availableProcessors.set(availableProcessors);
 	}
 	
 	public Long getUpTime() {
 		return upTime.get();
 	}
 	
 	public String getFormattedUpTime() {
		int days = (int) (upTime.get() / 86400000L);
		int hours = (int) (upTime.get() / 3600000L);
		int minutes = (int) (upTime.get() / 60000L);
		int seconds = (int) (upTime.get() / 1000L);
		return days + "d " + hours + "h " + minutes + "m " + seconds + "s";
 	}
 
 	public void setUpTime(Long upTime) {
 		this.upTime.set(upTime);
 	}
 	
 	public String getName() {
 		return name.get();
 	}
 
 	public Integer getBundlesCount() {
 		return bundlesCount.get();
 	}
 
 	public void setBundlesCount(Integer bundlesCount) {
 		this.bundlesCount.set(bundlesCount);
 	}
 
 	public Integer getServicesCount() {
 		return servicesCount.get();
 	}
 
 	public void setServicesCount(Integer servicesCount) {
 		this.servicesCount.set(servicesCount);
 	}
 	
 	public Integer getStandardServicesCount() {
 		return standardServicesCount.get();
 	}
 
 	public void setStandardServicesCount(Integer standardServicesCount) {
 		this.standardServicesCount.set(standardServicesCount);
 	}
 	
 	public Integer getInUseServicesCount() {
 		return inUseServicesCount.get();
 	}
 
 	public void setInUseServicesCount(Integer inUseServicesCount) {
 		this.inUseServicesCount.set(inUseServicesCount);
 	}
 
 }
