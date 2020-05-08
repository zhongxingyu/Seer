 package org.haim.springframwork.stackoverflow;
 
 import org.apache.log4j.Logger;
 
 

 public class GenericDao<T> {
 	Logger logger = Logger.getRootLogger();
 	public void create(T record) {
 		logger.info("saving :" + record);
 	}
 }
