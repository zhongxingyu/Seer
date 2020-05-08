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
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertTrue;
 
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.Arrays;
 
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Shell;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 import edu.umd.cs.guitar.model.SitarApplication;
 import edu.umd.cs.guitar.ripper.SitarRipper;
 import edu.umd.cs.guitar.ripper.SitarRipperConfiguration;
 import edu.umd.cs.guitar.ripper.test.aut.SWTBasicApp;
 
 /**
  * Tests for {@link SitarApplication}.
  */
 public class SitarApplicationTest {
 	
 	private final static String TEST_CLASS_NAME = SWTBasicApp.class.getName();
 	
 	private Display display;
 	
 	/**
 	 * Set up the display.
 	 */
 	@Before
 	public void setUp() {
 		if (display == null || display.isDisposed()) {
 			display = new Display();
 		}
 	}
 	
 	/**
 	 * Dispose the display.
 	 */
 	@After
 	public void tearDown() {
 		display.dispose();
 	}
 	
 	/**
 	 * Test {@link SitarApplication#getAllWindow()}
 	 */
 	@Test
 	public void testGetAllWindow() {
 		SitarRipperConfiguration config = new SitarRipperConfiguration();
 		config.setMainClass(TEST_CLASS_NAME);
 		SitarRipper ripper = new SitarRipper(config, Thread.currentThread());
 		
 		Shell shell1 = new Shell(display);
 		Shell shell2 = new Shell(display);
 		shell1.setVisible(true);
 		shell2.setVisible(true);
 		SitarApplication app = ripper.getMonitor().getApplication();
 		app.connect();
 		
 		assertEquals(2, app.getAllWindow().size());
 		
 		shell2.dispose();
 		assertEquals(1, app.getAllWindow().size());
 								
 		shell1.setVisible(true);
 		assertEquals(1, app.getAllWindow().size());
 		
 		shell1.dispose();
 		assertEquals(0, app.getAllWindow().size());
 	}
 	
 	/**
 	 * Test {@link SitarApplication#addURLs(URL[])}
 	 */
 	@Test
 	public void testAddURLs() throws MalformedURLException {
 		SitarRipperConfiguration config = new SitarRipperConfiguration();
 		config.setInitialWaitTime(200);
 		config.setMainClass(TEST_CLASS_NAME);
 		SitarRipper ripper = new SitarRipper(config, Thread.currentThread());
 		
 		SitarApplication app = ripper.getMonitor().getApplication();
 		URL[] urls = new URL[1];
 		urls[0] = new URL("http://www.google.com/");
 		
 		app.addURLs(urls);
 		
 		assertTrue(Arrays.asList(app.getURLs()).contains(urls[0]));
 	}
 }
