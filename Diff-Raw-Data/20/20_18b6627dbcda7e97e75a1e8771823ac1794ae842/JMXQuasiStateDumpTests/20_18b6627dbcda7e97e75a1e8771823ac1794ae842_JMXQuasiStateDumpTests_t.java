 /*******************************************************************************
  * Copyright (c) 2008, 2011 VMware Inc.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *   VMware Inc. - initial contribution
  *******************************************************************************/
 package org.eclipse.virgo.kernel.userregion.internal.management;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.zip.ZipException;
 
 import org.eclipse.virgo.kernel.osgi.quasi.QuasiFramework;
 import org.eclipse.virgo.kernel.osgi.quasi.QuasiFrameworkFactory;
 import org.junit.Test;
 import static org.junit.Assert.*;
 
 /**
  * 
  *
  */
 public class JMXQuasiStateDumpTests {
 
 	private static final String TEST_DUMP = "src/test/resources/testDump";
 	
 	private final JMXQuasiStateDump quasiStateDumpMBean;
 	
 	public JMXQuasiStateDumpTests() {
 		quasiStateDumpMBean = new JMXQuasiStateDump(new QuasiFrameworkFactory() {
 			
 			@Override
 			public QuasiFramework create(File stateDump) throws ZipException, IOException {
			    if (File.separator.equals("/")) {
			        assertEquals(TEST_DUMP, stateDump.getPath());
			    } else {
			        assertEquals(TEST_DUMP, stateDump.getPath().replace('\\',  '/'));
			    }
 				return new StubQuasiFramework();
 			}
 			
 			@Override
 			public QuasiFramework create() {
 				return new StubQuasiFramework();
 			}
 		});
 	}
 	
 	@Test
 	public void testGetUnresolvedBundleFailures(){
 		JMXQuasiResolutionFailure[] unresolvedBundleFailures = this.quasiStateDumpMBean.getUnresolvedBundleFailures(TEST_DUMP);
 		assertEquals(1, unresolvedBundleFailures.length);
 		assertEquals("Description", unresolvedBundleFailures[0].getDescription());
 		assertEquals(StubQuasiFramework.TEST_BUNDLE_NAME, unresolvedBundleFailures[0].getSymbolicName());
 	}
 	
 	@Test
 	public void testListBundles(){
 		JMXQuasiMinimalBundle[] listBundles = this.quasiStateDumpMBean.listBundles(TEST_DUMP);
 		assertEquals(1, listBundles.length);
 		assertEquals(StubQuasiFramework.TEST_BUNDLE_NAME, listBundles[0].getSymbolicName());
 
 	}
 	
 	@Test
 	public void testGetBundle(){
 		JMXQuasiBundle bundle = this.quasiStateDumpMBean.getBundle(TEST_DUMP, 5l);
 		assertEquals(StubQuasiFramework.TEST_BUNDLE_NAME, bundle.getSymbolicName());
 		assertEquals(5l, bundle.getIdentifier());
 	}
 	
 }
