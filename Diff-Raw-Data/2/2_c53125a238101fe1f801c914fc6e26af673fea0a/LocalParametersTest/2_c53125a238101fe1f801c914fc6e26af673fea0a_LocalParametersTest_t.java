 /*-
  * Copyright © 2009 Diamond Light Source Ltd., Science and Technology
  * Facilities Council Daresbury Laboratory
  *
  * This file is part of GDA.
  *
  * GDA is free software: you can redistribute it and/or modify it under the
  * terms of the GNU General Public License version 3 as published by the Free
  * Software Foundation.
  *
  * GDA is distributed in the hope that it will be useful, but WITHOUT ANY
  * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
  * details.
  *
  * You should have received a copy of the GNU General Public License along
  * with GDA. If not, see <http://www.gnu.org/licenses/>.
  */
 
 package gda.util.persistence;
 
 import gda.util.TestUtils;
 
 import java.io.File;
 import java.util.Arrays;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Vector;
 import java.util.concurrent.CountDownLatch;
 import java.util.concurrent.atomic.AtomicReference;
 
 import junit.framework.TestCase;
 
 import org.apache.commons.configuration.FileConfiguration;
 
 /**
  * Tests for {@link LocalParameters}.
  */
 public class LocalParametersTest extends TestCase {
 
 	/**
 	 * Test creating a new configuration, which should have no properties.
 
 	 * @throws Exception
 	 */
 	public void testNewConfigurationFile() throws Exception {
 		FileConfiguration lp = LocalParameters.getXMLConfiguration();
 		List<Object> keys = getKeysFromXMLConfiguration(lp);
		assertEquals(0, keys.size());
 		assertEquals("", keys.get(0));
 	}
 	
 	/**
 	 * Test adding properties to a configuration.
 	 * 
 	 * @throws Exception
 	 */
 	public void testAddingParameters() throws Exception {
 		FileConfiguration lp = LocalParameters.getXMLConfiguration("newone");
 		lp.setProperty("gda.a.x", "value gda.a.x");
 		lp.setProperty("gda.a._11_", "blarghh");
 		lp.save();
 		lp.reload();
 		List<Object> keys = getKeysFromXMLConfiguration(lp);
 		assertTrue(keys.size() == 2 || keys.size() == 3);
 		assertTrue(keys.contains("gda.a.x"));
 		assertTrue(keys.contains("gda.a._11_"));
 		if (keys.size() == 3) {
 			assertTrue(keys.contains(""));
 		}
 	}
 	
 	/**
 	 * Tests that a comma in a property value is not treated as a delimiter.
 	 * 
 	 * @throws Exception
 	 */
 	public void testCommaInProperty() throws Exception {
 		final String configName = "commatest";
 		final File configFile = TestUtils.getResourceAsFile(LocalParametersTest.class, configName + ".xml");
 		final File configDirAsFile = configFile.getParentFile();
 		final String configDir = configDirAsFile.getAbsolutePath();
 		
 		FileConfiguration config = LocalParameters.getXMLConfiguration(configDir, configName, false);
 		assertEquals(2, getKeysFromXMLConfiguration(config).size());
 		// This needs to change if GDA-2492 is fixed
 		assertEquals(Arrays.asList(new String[] {"1","2","3"}), config.getProperty("not-escaped"));
 		assertEquals("1,2,3", config.getProperty("escaped"));
 	}
 
 	private static List<Object> getKeysFromXMLConfiguration(FileConfiguration config) {
 		List<Object> list = new Vector<Object>();
 		Iterator<?> iterator = config.getKeys();
 		while (iterator.hasNext()) {
 			list.add(iterator.next());
 		}
 		return list;
 	}
 
 	public void testThreadSafety() throws Exception {
 		final File testScratchDir = TestUtils.createClassScratchDirectory(LocalParametersTest.class);
 		final String configDir = testScratchDir.getAbsolutePath();
 		
 		final String configName = "threadsafety";
 		
 		// Delete config from disk, if it exists
 		final File configFile = new File(configDir, configName + ".xml");
 		configFile.delete();
 		
 		final AtomicReference<Exception> error = new AtomicReference<Exception>();
 		
 		final int numThreads = 4;
 		final long threadRunTimeInMs = 5000;
 		final CountDownLatch startLatch = new CountDownLatch(1);
 		final CountDownLatch finishLatch = new CountDownLatch(numThreads);
 		
 		for (int i=0; i<numThreads; i++) {
 			Thread t = new Thread() {
 				@Override
 				public void run() {
 					
 					try {
 						final FileConfiguration config = LocalParameters.getThreadSafeXmlConfiguration(configDir, configName, true);
 						
 						// Wait for signal to start
 						startLatch.await();
 						
 						final String propertyName = Thread.currentThread().getName();
 						
 						final long startTime = System.currentTimeMillis();
 						while (true) {
 							
 							// Finish if we've exceeded the run time
 							long elapsedTime = System.currentTimeMillis() - startTime;
 							if (elapsedTime >= threadRunTimeInMs) {
 								break;
 							}
 							
 							// Finish if another thread has generated an exception
 							if (error.get() != null) {
 								break;
 							}
 							
 							config.setProperty(propertyName, System.currentTimeMillis());
 							config.save();
 						}
 					} catch (Exception e) {
 						e.printStackTrace(System.err);
 						error.set(e);
 					}
 					
 					finishLatch.countDown();
 				}
 			};
 			t.start();
 		}
 		
 		// Start all threads
 		final long startTime = System.currentTimeMillis();
 		startLatch.countDown();
 		
 		// Wait for all threads to finish
 		finishLatch.await();
 		final long endTime = System.currentTimeMillis();
 		final long elapsedTime = (endTime - startTime);
 		System.out.printf("Finished after %dms%n", elapsedTime);
 		
 		// No error should have been thrown
 		assertNull("An exception was thrown by one of the threads", error.get());
 	}
 }
