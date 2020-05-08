 package com.kdcloud.server.rest.api;
 
 import org.restlet.resource.Delete;
 import org.restlet.resource.Put;
 
 import com.kdcloud.weka.core.Instances;
 
 
 /**
  * URI /data
  * @author vincenzo
  *
  */
 public interface UserDataResource {
 	
 	
 	public static final String URI = "/data";
 	
 	
 	/**
 	 * creates a new dataset
 	 * @return the dataset id
 	 */
 	@Put
 	public Long createDataset(Instances instances);
 	
 	
 	@Delete
 	public void deleteAllData();
 
 }
