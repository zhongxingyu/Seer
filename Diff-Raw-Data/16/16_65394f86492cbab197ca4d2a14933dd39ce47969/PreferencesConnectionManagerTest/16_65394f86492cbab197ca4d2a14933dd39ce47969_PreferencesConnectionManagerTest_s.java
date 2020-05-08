 /**
  * Copyright (c) 2008 The RCER Development Team.
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html. If redistributing this code,
  * this entire header must remain intact.
  *
  * $Id$
  */
 package net.sf.rcer.conn.preferences;
 
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertNull;
 import static org.junit.Assert.assertTrue;
 
 import java.text.MessageFormat;
 import java.util.Collection;
 import java.util.Set;
 
 import net.sf.rcer.conn.Activator;
 import net.sf.rcer.conn.connections.ConnectionData;
 import net.sf.rcer.conn.connections.ConnectionNotFoundException;
 import net.sf.rcer.conn.connections.IConnectionData;
 import net.sf.rcer.conn.locales.LocaleRegistry;
 
 import org.eclipse.core.runtime.Preferences;
 import org.junit.Before;
 import org.junit.Test;
 
 /**
  * Test case for the {@link PreferencesConnectionProvider}
  * @author vwegert
  *
  */
 public class PreferencesConnectionManagerTest implements IPreferenceConstants {
 
 	private final static String DESCRIPTION     = "Connection Description";
 	private final static String SYSTEM_ID       = "SID";
 	private final static String ROUTER          = "/H/foo/H/";
 	private final static String APP_SERVER      = "appserv1";
 	private final static int    SYSTEM_NUMBER   = 1;
 	private final static String MSG_SERVER      = "msgserv1";
 	private final static int    MSG_SERVER_PORT = 3456;
 	private final static String LB_GROUP        = "PUBLIC";
 	private final static String DEFAULT_USER    = "foo";
 	private final static String DEFAULT_CLIENT  = "123";
 	private final static String DEFAULT_LOCALE  = "DE";
 
 	/**
 	 * @throws java.lang.Exception
 	 */
 	@Before
 	public void setUp() throws Exception {
 
 		final Preferences prefs = Activator.getDefault().getPluginPreferences();
 
 		// remove all connection preferences
 		final String[] properties = prefs.propertyNames();
 		for (int i = 0; i < properties.length; i++) {
 			prefs.setToDefault(properties[i]);
 		}
 
 		prefs.setValue(CONNECTION_NUMBER, 4);
 
 		// a valid direct connection
 		prefs.setValue(CONNECTION_TYPE                 + ".1", CONNECTION_TYPE_DIRECT);
 		prefs.setValue(CONNECTION_DESCRIPTION          + ".1", DESCRIPTION);
 		prefs.setValue(CONNECTION_SYSTEM_ID            + ".1", SYSTEM_ID);
 		prefs.setValue(CONNECTION_ROUTER               + ".1", ROUTER);
 		prefs.setValue(CONNECTION_APPLICATION_SERVER   + ".1", APP_SERVER);
 		prefs.setValue(CONNECTION_SYSTEM_NUMBER        + ".1", SYSTEM_NUMBER);
 		prefs.setValue(CONNECTION_DEFAULT_USER         + ".1", DEFAULT_USER);
 		prefs.setValue(CONNECTION_DEFAULT_CLIENT       + ".1", DEFAULT_CLIENT);
 		prefs.setValue(CONNECTION_DEFAULT_LOCALE       + ".1", DEFAULT_LOCALE);
 
 		// a valid load-balancing connection 
 		prefs.setValue(CONNECTION_TYPE                 + ".2", CONNECTION_TYPE_LOAD_BALANCING);
 		prefs.setValue(CONNECTION_DESCRIPTION          + ".2", DESCRIPTION);
 		prefs.setValue(CONNECTION_SYSTEM_ID            + ".2", SYSTEM_ID);
 		prefs.setValue(CONNECTION_ROUTER               + ".2", ROUTER);
 		prefs.setValue(CONNECTION_MESSAGE_SERVER       + ".2", MSG_SERVER);
 		prefs.setValue(CONNECTION_MESSAGE_SERVER_PORT  + ".2", MSG_SERVER_PORT);
 		prefs.setValue(CONNECTION_LOAD_BALANCING_GROUP + ".2", LB_GROUP);
 		prefs.setValue(CONNECTION_DEFAULT_USER         + ".2", DEFAULT_USER);
 		prefs.setValue(CONNECTION_DEFAULT_CLIENT       + ".2", DEFAULT_CLIENT);
 		prefs.setValue(CONNECTION_DEFAULT_LOCALE       + ".2", DEFAULT_LOCALE);
 
 		// a semi-valid connection (wrong default locale, should be ignored) 
 		prefs.setValue(CONNECTION_TYPE                 + ".3", CONNECTION_TYPE_DIRECT);
 		prefs.setValue(CONNECTION_DESCRIPTION          + ".3", DESCRIPTION);
 		prefs.setValue(CONNECTION_SYSTEM_ID            + ".3", SYSTEM_ID);
 		prefs.setValue(CONNECTION_ROUTER               + ".3", ROUTER);
 		prefs.setValue(CONNECTION_APPLICATION_SERVER   + ".3", APP_SERVER);
 		prefs.setValue(CONNECTION_SYSTEM_NUMBER        + ".3", SYSTEM_NUMBER);
 		prefs.setValue(CONNECTION_DEFAULT_USER         + ".3", DEFAULT_USER);
 		prefs.setValue(CONNECTION_DEFAULT_CLIENT       + ".3", DEFAULT_CLIENT);
 		prefs.setValue(CONNECTION_DEFAULT_LOCALE       + ".3", "ZZZ_INVALID");
 
 		// an invalid connection (invalid type) that should not be loaded
 		prefs.setValue(CONNECTION_TYPE                 + ".4", "foobar");
 	}
 
 	/**
 	 * Tests the direct connection.
 	 * @throws Exception
 	 */
 	@Test
 	public void testDirectConnection() throws Exception {
 
 		final PreferencesConnectionManager manager = PreferencesConnectionManager.getInstance();
 		final String CONNECTION_ID = "1";
 
 		final Set<String> connectionIDs = manager.getConnectionIDs();
 		assertTrue("Direct connection ID is missing", 
 				connectionIDs.contains(CONNECTION_ID));
 
 		IConnectionData conn = manager.getConnectionData(CONNECTION_ID);
 		assertNotNull("Direct connection is missing", conn);
 		assertEquals("Connection ID of direct connection",      
 				CONNECTION_ID, conn.getConnectionID());
 		assertEquals("Description of direct connection", 
 				DESCRIPTION, conn.getDescription());
 		assertEquals("System ID of direct connection", 
 				SYSTEM_ID, conn.getSystemID());
 		assertTrue("Connection type of direct connection", 
 				conn.isDirectConnection());
 		assertEquals("Router of direct connection", 
 				ROUTER, conn.getRouter());
 		assertEquals("Application server of direct connection", 
 				APP_SERVER, conn.getApplicationServer());
 		assertEquals("System number of direct connection", 
 				SYSTEM_NUMBER, conn.getSystemNumber());
 		assertEquals("Default user of direct connection", 
 				DEFAULT_USER, conn.getDefaultUser());
 		assertTrue("Default user changeability of direct connection", 
 				conn.isDefaultUserEditable());
 		assertEquals("Default client of direct connection", 
 				DEFAULT_CLIENT, conn.getDefaultClient());
 		assertTrue("Default client changeability of direct connection",         
 				conn.isDefaultClientEditable());
 		assertEquals("Default locale of direct connection",  
 				LocaleRegistry.getInstance().getLocaleByISO(DEFAULT_LOCALE), conn.getDefaultLocale());
 		assertTrue("Default locale changeability of direct connection",         
 				conn.isDefaultLocaleEditable());
 
 	}
 
 	/**
 	 * Tests the load-balancing connection.
 	 * @throws Exception
 	 */
 	@Test
 	public void testLoadBalancingConnection() throws Exception {
 
 		final PreferencesConnectionManager manager = PreferencesConnectionManager.getInstance();
 		final String CONNECTION_ID = "2";
 
 		final Set<String> connectionIDs = manager.getConnectionIDs();
 		assertTrue("Load-balanced connection ID is missing", 
 				connectionIDs.contains(CONNECTION_ID));
 
 		IConnectionData conn = manager.getConnectionData(CONNECTION_ID);
 		assertNotNull("Load-balanced connection is missing", conn);
 		assertEquals("Connection ID of load-balanced connection", 
 				CONNECTION_ID, conn.getConnectionID());
 		assertEquals("Description of load-balanced connection", 
 				DESCRIPTION, conn.getDescription());
 		assertEquals("System ID of load-balanced connection", 
 				SYSTEM_ID, conn.getSystemID());
 		assertFalse("Connection type of load-balanced connection", 
 				conn.isDirectConnection());
 		assertEquals("Router of load-balanced connection", 
 				ROUTER, conn.getRouter());
 		assertEquals("Message server of load-balanced connection", 
 				MSG_SERVER, conn.getMessageServer());
 		assertEquals("Message server port of load-balanced connection", 
 				MSG_SERVER_PORT, conn.getMessageServerPort());
 		assertEquals("Load-balancing group of load-balanced connection", 
 				LB_GROUP, conn.getLoadBalancingGroup());
 		assertEquals("Default user of load-balanced connection", 
 				DEFAULT_USER, conn.getDefaultUser());
 		assertTrue("Default user changeability of load-balanced connection", 
 				conn.isDefaultUserEditable());
 		assertEquals("Default client of load-balanced connection", 
 				DEFAULT_CLIENT, conn.getDefaultClient());
 		assertTrue("Default client changeability of load-balanced connection",         
 				conn.isDefaultClientEditable());
 		assertEquals("Default locale of load-balanced connection",  
 				LocaleRegistry.getInstance().getLocaleByISO(DEFAULT_LOCALE), conn.getDefaultLocale());
 		assertTrue("Default locale changeability of load-balanced connection",         
 				conn.isDefaultLocaleEditable());
 
 	}
 
 	/**
 	 * Tests whether an invalid connection ID is handled correctly.
 	 * @throws Exception
 	 */
 	@Test(expected=ConnectionNotFoundException.class)
 	public void testInvalidConnectionIDNonNumeric() throws Exception {
 		PreferencesConnectionManager.getInstance().getConnectionData("may_only_contain_numbers");
 	}
 
 	/**
 	 * Tests whether an invalid connection ID is handled correctly.
 	 * @throws Exception
 	 */
 	@Test(expected=ConnectionNotFoundException.class)
 	public void testInvalidConnectionIDBelowZero() throws Exception {
 		PreferencesConnectionManager.getInstance().getConnectionData("-1");
 	}
 
 	/**
 	 * Tests whether an invalid connection ID is handled correctly.
 	 * @throws Exception
 	 */
 	@Test(expected=ConnectionNotFoundException.class)
 	public void testInvalidConnectionIDMaxExceeded() throws Exception {
 		PreferencesConnectionManager.getInstance().getConnectionData("9999");
 	}
 
 	/**
 	 * Tests whether a connection with an invalid type is handled correctly.
 	 * @throws Exception
 	 */
 	@Test(expected=ConnectionNotFoundException.class)
 	public void testInvalidConnectionInvalidType() throws Exception {
 		PreferencesConnectionManager.getInstance().getConnectionData("4");
 	}
 
 	/**
 	 * Tests whether a connection with an invalid locale is handled correctly (that is, whether the locale is ignored).
 	 * @throws Exception
 	 */
 	@Test
 	public void testInvalidConnectionInvalidLocale() throws Exception {
 		IConnectionData conn = PreferencesConnectionManager.getInstance().getConnectionData("3");
 		assertNotNull("Connection with invalid locale not loaded", conn);
 		assertNull("Default locale should not be set", conn.getDefaultLocale());
 	}
 
 	/**
 	 * Test whether the read method for all connections works.
 	 * @throws Exception
 	 */
 	@Test
 	public void testGetConnectionData() throws Exception {
 		final Collection<ConnectionData> connections = PreferencesConnectionManager.getInstance().getConnectionData();
 		assertEquals("Number of connections", 3, connections.size());
 	}
 
 	/**
 	 * Test for {@link PreferencesConnectionManager#getConnectionIDs()}
 	 * @throws Exception
 	 */
 	@Test
 	public void testGetConnectionIDs() throws Exception {
 		final Set<String> ids = PreferencesConnectionManager.getInstance().getConnectionIDs();
 		for(int i = 1; i <= 4; i++) {
 			assertTrue(MessageFormat.format("Connection {0} is missing.", i), ids.contains(Integer.toString(i)));
 		}
 	}
 
 }
