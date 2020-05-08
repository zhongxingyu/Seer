 /* Copyright (c) 2010 Olivier Girardot
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *   Olivier Girardot - initial contribution
  */
 
 package org.eclipse.virgo.kernel.config.internal;
 
 import static org.easymock.EasyMock.createMock;
 import static org.easymock.EasyMock.expect;
 import static org.easymock.EasyMock.replay;
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNull;
 
 import java.io.IOException;
 import java.util.Dictionary;
 import java.util.HashMap;
 import java.util.Hashtable;
 import java.util.Map;
 
 import org.eclipse.virgo.kernel.StubConfigurationAdmin;
 import org.eclipse.virgo.kernel.serviceability.Assert.FatalAssertionException;
 import org.junit.Test;
 import org.osgi.service.cm.Configuration;
 import org.osgi.service.cm.ConfigurationAdmin;
 
 /**
  * This class is for testing {@link ConfigurationAdminConfigurationInfo} class, 
  * an implementation of the {@link ConfigurationInfo} interface.
  */
 public class ConfigurationAdminConfigurationInfoTests {
     private static final String CONFIG_INFO_TEST_PID = "CUSTOM_PID";
     
     @Test
     public void testGetPid() {
         ConfigurationAdmin stubAdmin = new StubConfigurationAdmin();
         ConfigurationInfo configurationInfo = new ConfigurationAdminConfigurationInfo(stubAdmin, CONFIG_INFO_TEST_PID);
         assertEquals(CONFIG_INFO_TEST_PID, configurationInfo.getPid());
     }
     
     @Test
     public void testGetPropertiesFromEmptyConfiguration() {
         ConfigurationAdmin stubAdmin = new StubConfigurationAdmin();
         ConfigurationInfo configurationInfo = new ConfigurationAdminConfigurationInfo(stubAdmin, CONFIG_INFO_TEST_PID);
         assertEquals("Properties map should be empty.", new HashMap<String, String>(), configurationInfo.getProperties());
     }
         
     @Test
     public void testFailingConfigurationAdmin() throws IOException {
         ConfigurationAdmin mockAdmin = createMock(ConfigurationAdmin.class);
         expect(mockAdmin.getConfiguration(CONFIG_INFO_TEST_PID, null)).andThrow(new IOException("Error trying to load configuration"));
         replay(mockAdmin);
         
         ConfigurationInfo configurationInfo = new ConfigurationAdminConfigurationInfo(mockAdmin, CONFIG_INFO_TEST_PID);
         assertNull(configurationInfo.getProperties());
     }
     
     @Test(expected=FatalAssertionException.class)
     public void testFailingConstructorWithNullConfigurationAdmin() throws IOException {
         ConfigurationInfo configurationInfo = new ConfigurationAdminConfigurationInfo(null, CONFIG_INFO_TEST_PID);
     }
     
     @Test
     public void testGetPropertiesWithFilledConfiguration() throws IOException {
         ConfigurationAdmin stubAdmin = new StubConfigurationAdmin();
         Configuration config = stubAdmin.getConfiguration(CONFIG_INFO_TEST_PID, null);
 
         Dictionary<String, String> dict = new Hashtable<String, String>();
         dict.put("key", "value");
         
         config.update(dict);
                 
         ConfigurationInfo configurationInfo = new ConfigurationAdminConfigurationInfo(stubAdmin, CONFIG_INFO_TEST_PID);
         
         Map<String, String> expectedMap = new HashMap<String, String>();
         expectedMap.put("key", "value");
         assertEquals(expectedMap, configurationInfo.getProperties());
     }
 
 }
