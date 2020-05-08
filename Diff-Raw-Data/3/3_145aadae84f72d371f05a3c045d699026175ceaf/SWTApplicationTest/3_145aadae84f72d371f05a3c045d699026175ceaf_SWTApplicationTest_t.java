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
 
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Shell;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 import edu.umd.cs.guitar.model.SWTApplication;
 import edu.umd.cs.guitar.ripper.SWTRipper;
 import edu.umd.cs.guitar.ripper.SWTRipperConfiguration;
 import edu.umd.cs.guitar.ripper.test.aut.SWTBasicApp;
 
 public class SWTApplicationTest {
 	
 	private final static String TEST_CLASS_NAME = SWTBasicApp.class.getName();
 	
 	private Display display;
 	
 	@Before
 	public void setUp() {
 		if (display == null || display.isDisposed()) {
 			display = new Display();
 		}
 	}
 	
 	@After
 	public void tearDown() {
 		display.dispose();
 	}
 	
 	@Test
 	public void testGetAllWindow() {
 		SWTRipperConfiguration config = new SWTRipperConfiguration();
 		config.setMainClass(TEST_CLASS_NAME);
 		SWTRipper ripper = new SWTRipper(config, Thread.currentThread());
 		
 		Shell shell1 = new Shell(display);
 		Shell shell2 = new Shell(display);
 		shell1.setVisible(true);
 		shell2.setVisible(true);
 		SWTApplication app = ripper.getMonitor().getApplication();
 		app.connect();
 		
 		assertEquals(2, app.getAllWindow().size());
 		
 		shell2.dispose();
 		assertEquals(1, app.getAllWindow().size());
 		
 		shell1.setVisible(false);
		shell1.redraw();
		shell1.update();
		display.update(); // this is probably redundant
 		assertEquals(0, app.getAllWindow().size());
 		
 		shell1.setVisible(true);
 		assertEquals(1, app.getAllWindow().size());
 		
 		shell1.dispose();
 		assertEquals(0, app.getAllWindow().size());
 	}
 }
