 package ar.com.hgdeoro.daedalus.client;
 
 import java.net.URL;
 
 import junit.framework.Assert;
 import junit.framework.Test;
 import junit.framework.TestCase;
 import junit.framework.TestSuite;
 
 import org.apache.log4j.Logger;
 import org.apache.log4j.helpers.Loader;
 import org.apache.log4j.xml.DOMConfigurator;
 
 /* **********************************************************************
 
  daedalus - Centralized log server
  Copyright (C) 2012 - Horacio Guillermo de Oro <hgdeoro@gmail.com>
 
  This file is part of daedalus.
 
  daedalus is free software; you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation version 2.
 
  daedalus is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License version 2 for more details.
 
  You should have received a copy of the GNU General Public License
  along with daedalus; see the file LICENSE.txt.
 
  ********************************************************************** */
 
 /**
  * Unit test for simple Log4jSimpleAppender.
  * 
  * @author Horacio G. de Oro <hgdeoro@gmail.com>
  */
 public class Log4jSimpleAppenderTest extends TestCase {
 
 	/**
 	 * Create the test case
 	 * 
 	 * @param testName
 	 *            name of the test case
 	 */
 	public Log4jSimpleAppenderTest(String testName) {
 		super(testName);
 	}
 
 	/**
 	 * @return the suite of tests being tested
 	 */
 	public static Test suite() {
		return new TestSuite(DaedalusClientTest.class);
 	}
 
 	public void testAppender() {
 
 		final String resourceName = "ar/com/hgdeoro/daedalus/client/log4j-daedalus-client-test.xml";
 
 		final URL url = Loader.getResource(resourceName);
 		if (url == null)
 			Assert.fail("Couldn't get resource from file " + resourceName);
 
 		DOMConfigurator.configure(url);
 
 		final Logger logger = Logger.getLogger(Log4jSimpleAppenderTest.class);
 
 		logger.debug("This is a DEBUG message from Log4j");
 		logger.info("This is a INFO message from Log4j");
 		logger.warn("This is a WARN message from Log4j");
 		logger.error("This is a ERROR message from Log4j");
 
 	}
 
 }
