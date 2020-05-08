 package com.netradius.commons;
 
 import org.apache.log4j.xml.DOMConfigurator;
 import org.slf4j.LoggerFactory;
 
 /**
  * Base class for all unit tests.
  *
  * @author Erik R. Jensen
  */
 public class UnitTest {
 
 	static {
 		DOMConfigurator.configure(UnitTest.class.getResource("log4j.xml"));
 		LoggerFactory.getLogger(UnitTest.class).info("Logging initialized successfully.");
 	}
 }
