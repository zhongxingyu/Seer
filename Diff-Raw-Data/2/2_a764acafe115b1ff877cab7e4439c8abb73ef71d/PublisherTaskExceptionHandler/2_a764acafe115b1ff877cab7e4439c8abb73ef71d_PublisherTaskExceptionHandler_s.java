 package com.buschmais.tinkerforge4jenkins.client;
 
 import java.lang.Thread.UncaughtExceptionHandler;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
 * {@link UncaughtExceptionHandler} implementation for the {@link PublisherTask}
  * 
  * @author dirk.mahler
  */
 public class PublisherTaskExceptionHandler implements UncaughtExceptionHandler {
 
 	/**
 	 * Logger.
 	 */
 	private static final Logger LOGGER = LoggerFactory
 			.getLogger(PublisherTaskExceptionHandler.class);
 
 	@Override
 	public void uncaughtException(Thread t, Throwable e) {
 		LOGGER.error("Caught an unexpected exception in thread " + t, e);
 	}
 
 }
