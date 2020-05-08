 /*	
  *  Copyright (c) 2011-@year@. The GUITAR group at the University of Maryland. Names of owners of this group may
  *  be obtained by sending an e-mail to atif@cs.umd.edu
  * 
  *  Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated 
  *  documentation files (the "Software"), to deal in the Software without restriction, including without 
  *  limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
  *	the Software, and to permit persons to whom the Software is furnished to do so, subject to the following 
  *	conditions:
  * 
  *	The above copyright notice and this permission notice shall be included in all copies or substantial 
  *	portions of the Software.
  *
  *	THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT 
  *	LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO 
  *	EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER 
  *	IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR 
  *	THE USE OR OTHER DEALINGS IN THE SOFTWARE. 
  */
 package edu.umd.cs.guitar.ripper.test;
 
 import static org.junit.Assert.assertArrayEquals;
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertTrue;
 
 import java.io.File;
 import java.net.MalformedURLException;
 import java.net.URL;
 
 import org.junit.Test;
 
 import edu.umd.cs.guitar.model.SitarApplicationStartException;
 import edu.umd.cs.guitar.ripper.SitarRipper;
 import edu.umd.cs.guitar.ripper.SitarRipperConfiguration;
 import edu.umd.cs.guitar.ripper.test.aut.SWTArgumentApp;
import edu.umd.cs.guitar.ripper.test.aut.SWTURLApp;
 
 /**
  * Tests for {@link SitarRipperConfigurationTest}.
  */
 public class SitarRipperConfigurationTest {
 
 	/**
 	 * Test {@link SitarRipperConfiguration#setLogFile(String)}.
 	 */
 	@Test
 	public void testSetLogFile() {
 		SitarRipperConfiguration config = new SitarRipperConfiguration();
 		config.setLogFile("foo");
 		assertEquals("foo", config.getLogFile());
 	}
 	
 	/**
 	 * Test {@link SitarRipperConfiguration#setLogWidgetFile(String)}.
 	 */
 	@Test
 	public void testSetLogWidgetFile() {
 		SitarRipperConfiguration config = new SitarRipperConfiguration();
 		config.setLogWidgetFile("foo");
 		assertEquals("foo", config.getLogWidgetFile());
 	}
 	
 	/**
 	 * Test {@link SitarRipperConfiguration#setInitialWaitTime(int)}.
 	 */
 	@Test
 	public void testSetInitialWaitingTime() {
 		SitarRipperConfiguration config = new SitarRipperConfiguration();
 		config.setInitialWaitTime(100);
 		assertEquals(100, config.getInitialWaitTime());
 	}
 		
 	/**
 	 * Test that arguments are correctly passed to the application under test.
 	 */
 	@Test
 	public void testArguments() {
 		SitarRipperConfiguration config = new SitarRipperConfiguration();
 		config.setMainClass(SWTArgumentApp.class.getName());
 		
 		String[] args = new String[0];
 		config.setArguments(args);
 
 		SitarRipper swtRipper = new SitarRipper(config);
 		assertArrayEquals(args, swtRipper.getApplication().getArgsToApp());
 
 		args = new String[] { "one" };
 		config.setArguments(args);
 		swtRipper = new SitarRipper(config);
 		assertArrayEquals(args, swtRipper.getApplication().getArgsToApp());
 		
 		args = new String[] { "one", "two" };
 		config.setArguments(args);
 		swtRipper = new SitarRipper(config);
 		assertArrayEquals(args, swtRipper.getApplication().getArgsToApp());
 	}
 
 	/**
 	 * Test {@link SitarRipperConfiguration#setUrls(URL[])}.
 	 * @throws SitarApplicationStartException
 	 * @throws MalformedURLException
 	 */
 	@Test
 	public void testURLs() throws SitarApplicationStartException, MalformedURLException {
 		final String fileName = "README";
 		
 		File file = new File(fileName);
 		assertTrue(file.exists());
 				
 		SitarRipperConfiguration config = new SitarRipperConfiguration();
		config.setMainClass(SWTURLApp.class.getName());
 		config.setArguments(new String[] { fileName });
 		config.setUrls(new URL[0]);
 		
 		SitarRipper ripper = new SitarRipper(config);
 		
		// SWTURLApp doesn't block forever, so can start it on this thread
 		ripper.getApplication().startGUI();
 				
 		config.setUrls(new URL[] { file.toURI().toURL()});
 		new SitarRipper(config);
 		
 		// TODO figure our how to test this
 				
 	}
 
 }
