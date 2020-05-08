 package com.carmatech.perfandra;
 
 import org.junit.runner.Description;
 import org.junit.runner.notification.RunListener;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class CassandraRunListener extends RunListener {
	private static final Logger LOGGER = LoggerFactory.getLogger(CassandraRunListener.class);
 
 	@Override
 	public void testRunStarted(Description description) {
		LOGGER.info("Load Cassandra data here...");
 	}
 }
