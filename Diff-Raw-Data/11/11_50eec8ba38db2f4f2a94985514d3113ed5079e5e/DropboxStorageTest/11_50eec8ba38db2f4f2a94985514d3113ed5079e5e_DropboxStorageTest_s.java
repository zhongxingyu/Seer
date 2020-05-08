 /**
  * Copyright (c) 2012 Eclipselab Eclipse Sync and others.
  * All rights reserved. This program and the accompanying materials 
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html 
  */
 package org.eclipselab.eclipsesync.tests.storage;
 
 import static junit.framework.Assert.assertEquals;
 import static junit.framework.Assert.assertNotNull;
 import static org.junit.Assert.assertNull;
 import static org.junit.Assert.fail;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.lang.reflect.Field;
 import java.util.Date;
 
 import org.eclipselab.eclipsesync.core.IStorageNode;
 import org.eclipselab.eclipsesync.core.StorageException;
 import org.eclipselab.eclipsesync.storage.dropbox.DropboxStorage;
 import org.eclipselab.eclipsesync.storage.dropbox.DropboxStorage.DropboxNode;
import org.junit.Before;
 import org.junit.Test;
 
 public class DropboxStorageTest {
 
 	private static final String SEPARATOR = DropboxStorage.SEPARATOR;
	private DropboxStorage storage = null;
 
 	// use the existing token and secret to avoid using Browser to manually authenticate
	@Before public void setUp() throws SecurityException, IllegalArgumentException, NoSuchFieldException, IllegalAccessException {
 		storage = new DropboxStorage();
 		if (!Messages.DropboxStorageTest_TestToken.startsWith("${")) { //$NON-NLS-1$
 			mockData(storage, "token", Messages.DropboxStorageTest_TestToken); //$NON-NLS-1$
 			mockData(storage, "secret", Messages.DropboxStorageTest_TestSecret); //$NON-NLS-1$
 		}
 	}
 
	private void mockData(DropboxStorage instance, String fieldName, String fieldValue) throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
 		Field field = instance.getClass().getDeclaredField(fieldName);
 		field.setAccessible(true);
 		field.set(instance, fieldValue);
 		field.setAccessible(false);
 	}
 
 	@Test public void createNode() throws StorageException {
 		final String nodeName = "testNode"; //$NON-NLS-1$
 		IStorageNode node = storage.createNode(nodeName, null); 
 		assertNotNull("0.2", node); //$NON-NLS-1$
 		assertEquals("0.3", SEPARATOR + nodeName, ((DropboxNode)node).getEntry().path); //$NON-NLS-1$ 
 		node = storage.createNode(nodeName, null);
 		assertNotNull("0.5", node); //$NON-NLS-1$
 	}
 
 	@Test public void createSubNode() throws StorageException {
 		final String parentNodeName = "parentNode"; //$NON-NLS-1$
 		IStorageNode parentNode = storage.createNode(parentNodeName, null); 
 		assertNotNull("0.3", parentNode); //$NON-NLS-1$
 		final String childNodeName = "childNode"; //$NON-NLS-1$
 		final DropboxNode childNode = (DropboxNode) storage.createNode(childNodeName, parentNode);
 		assertNotNull("0.5", childNode); //$NON-NLS-1$
 		assertEquals("0.6", SEPARATOR + parentNodeName + SEPARATOR + childNodeName, childNode.getEntry().path); //$NON-NLS-1$ 
 	}
 
 	@Test public void getNode() throws StorageException {
 		final String nodeName = "testNode"; //$NON-NLS-1$
 		IStorageNode node = storage.createNode(nodeName, null); 
 		assertNotNull("0.2", node); //$NON-NLS-1$
 		node = storage.getNode(nodeName, null);
 		assertNotNull("0.4", node); //$NON-NLS-1$
 		assertEquals("0.5", SEPARATOR + nodeName, ((DropboxNode)node).getEntry().path); //$NON-NLS-1$
 	}
 
 	@Test public void getNodeNotExisting() throws StorageException {
 		final String nodeName = "testNode" + new Date().getTime(); //$NON-NLS-1$
 		IStorageNode node = storage.getNode(nodeName, null); 
 		assertNull("0.2", node); //$NON-NLS-1$
 	}
 
 	@Test public void createAndGetConfig() throws StorageException, IOException {
 		final String nodeid = "testNode"; //$NON-NLS-1$
 		try {
 			storage.removeNode(nodeid, null);
 		} catch (StorageException e) {
 			if (e.getExceptionType() != StorageException.NodeNotExist)
 				throw e;
 		}
 		IStorageNode node = storage.createNode(nodeid, null);
 		assertNotNull("0.3", node); //$NON-NLS-1$
 		assertEquals("0.4", 0, node.listConfigs().length); //$NON-NLS-1$
 		final String configName = "configid"; //$NON-NLS-1$
 		OutputStream output = node.getStore(configName); 
 		assertNotNull("0.6", output); //$NON-NLS-1$
 		final String content = "test"; //$NON-NLS-1$
 		output.write(new String(content).getBytes()); 
 		output.close();
 		String[] configs = node.listConfigs();
 		assertEquals("0.7", 1, configs.length); //$NON-NLS-1$
 		assertEquals("0.8", configName, configs[0]); //$NON-NLS-1$
 		InputStream input = node.load(configs[0]);
 		byte[] buffer = new byte[1024];
 		StringBuilder sb = new StringBuilder();
 		while (input.read(buffer) > 0 ) {
 			sb.append(new String(buffer));
 		}
 		input.close();
 		assertEquals("0.9", content, sb.toString().trim()); //$NON-NLS-1$
 	}
 
 	@Test public void getNonExistingConfig() throws StorageException {
 		final String nodeid = "testNode"; //$NON-NLS-1$
 		try {
 			storage.removeNode(nodeid, null);
 		} catch (StorageException e) {
 			if (e.getExceptionType() != StorageException.NodeNotExist)
 				throw e;
 		}
 		assertNull("0.2", storage.getNode(nodeid, null)); //$NON-NLS-1$
 		IStorageNode node = storage.createNode(nodeid, null);
 		assertNotNull("0.3", node); //$NON-NLS-1$
 		assertEquals("0.4", 0, node.listConfigs().length); //$NON-NLS-1$
 		try {
 			node.load("noSuchConfig"); //$NON-NLS-1$
 			fail("0.6"); //$NON-NLS-1$
 		} catch (StorageException e) {
 			assertEquals("0.7", StorageException.ConfigNotFound, e.getExceptionType()); //$NON-NLS-1$
 		}
 	}
 }
