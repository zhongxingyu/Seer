 /*
  * Copyright 2011 - 2012 by the CloudRAID Team
  * see AUTHORS for more details
  *
  * Licensed to the Apache Software Foundation (ASF) under one
  * or more contributor license agreements.  See the NOTICE file
  * distributed with this work for additional information
  * regarding copyright ownership.  The ASF licenses this file
  * to you under the Apache License, Version 2.0 (the
  * "License"); you may not use this file except in compliance
  * with the License.  You may obtain a copy of the License at
 
  * http://www.apache.org/licenses/LICENSE-2.0
 
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an
  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  * KIND, either express or implied.  See the License for the
  * specific language governing permissions and limitations
  * under the License.
  */
 
 package de.dhbw_mannheim.cloudraid.metadatamgr;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertNull;
 import static org.junit.Assert.assertTrue;
 
 import java.io.File;
 
 import org.junit.AfterClass;
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 import de.dhbw_mannheim.cloudraid.config.impl.Config;
 import de.dhbw_mannheim.cloudraid.config.ICloudRAIDConfig;
 import de.dhbw_mannheim.cloudraid.metadatamgr.impl.HSQLMetadataManager;
 
 /**
  * This JUnit test tests the {@link HSQLMetadataManager}.
  * 
  * @author Florian Bausch
  * 
  */
 public class TestHSQLDatabaseConnector {
 
 	private static HSQLMetadataManager dbc;
 	private static final String DATABASE_FILE = "testfiledb";
 	private static final String PATH = "path", PATH2 = "path2";
 	private static final String HASH = "hash", HASH2 = "hash2";
 	private static final long TIME = 100000L, TIME2 = 200000L;
 	private static int user1Id, user2Id;
 	private static ICloudRAIDConfig config;
 
 	@BeforeClass
 	public static void oneTimeSetUp() throws InstantiationException,
 			IllegalAccessException, ClassNotFoundException, ClassCastException {
 		config = new Config();
 		config.setCloudRAIDHome(System.getProperty("java.io.tmpdir")
 				+ File.separator + "cloudraid");
 		config.init("CloudRAID-unitTests");

 		dbc = new HSQLMetadataManager();
 		dbc.setConfig(config);

 		assertTrue(dbc.connect(config.getCloudRAIDHome() + DATABASE_FILE, "SA",
 				""));
 		assertTrue(dbc.initialize());

 		assertTrue(dbc.addUser("User1", "Password1"));
		config.put("allowUserAdd", false);
		assertFalse(dbc.addUser("User2", "Password2"));
		config.put("allowUserAdd", true);
 		assertTrue(dbc.addUser("User2", "Password2"));

 		user1Id = dbc.authUser("User1", "Password1");
 		user2Id = dbc.authUser("User2", "Password2");
 		assertTrue(user1Id >= 0);
 		assertTrue(user2Id >= 0);
 	}
 
 	@AfterClass
 	public static void oneTimeTearDown() {
 		assertTrue(dbc.disconnect());
 		new File(config.getCloudRAIDHome() + DATABASE_FILE + ".data").delete();
 		new File(config.getCloudRAIDHome() + DATABASE_FILE + ".properties")
 				.delete();
 		new File(config.getCloudRAIDHome() + DATABASE_FILE + ".script")
 				.delete();
 	}
 
 	@Test
 	public void testInsert() {
 		// Insert first file
 		assertTrue(dbc.fileNew(PATH, HASH, TIME, user1Id) >= 0);
 
 		assertEquals(PATH, dbc.getName(HASH, user1Id));
 		assertEquals(HASH, dbc.getHash(PATH, user1Id));
 		assertEquals(TIME, dbc.getLastMod(PATH, user1Id));
 		assertNull(dbc.getName(HASH, user2Id));
 		assertNull(dbc.getHash(PATH, user2Id));
 		assertEquals(-1L, dbc.getLastMod(PATH, user2Id));
 
 		// Update first file must fail!
 		assertTrue(dbc.fileNew(PATH, HASH, TIME2, user1Id) == -1);
 
 		assertEquals(PATH, dbc.getName(HASH, user1Id));
 		assertEquals(HASH, dbc.getHash(PATH, user1Id));
 		assertEquals(TIME, dbc.getLastMod(PATH, user1Id));
 		assertNull(dbc.getName(HASH, user2Id));
 		assertNull(dbc.getHash(PATH, user2Id));
 		assertEquals(-1L, dbc.getLastMod(PATH, user2Id));
 
 		// Insert second file for both users
 		assertTrue(dbc.fileNew(PATH2, HASH2, TIME2, user1Id) >= 0);
 		assertTrue(dbc.fileNew(PATH2, HASH2, TIME2, user2Id) >= 0);
 
 		assertEquals(PATH2, dbc.getName(HASH2, user1Id)); // User 1
 		assertEquals(HASH2, dbc.getHash(PATH2, user1Id));
 		assertEquals(TIME2, dbc.getLastMod(PATH2, user1Id));
 		assertEquals(PATH2, dbc.getName(HASH2, user2Id)); // User 2
 		assertEquals(HASH2, dbc.getHash(PATH2, user2Id));
 		assertEquals(TIME2, dbc.getLastMod(PATH2, user2Id));
 	}
 
 	@Test
 	public void testDelete() {
 		long time = System.currentTimeMillis();
 		String path = "path3";
 		String hash = "hash3";
 		int id = dbc.fileNew(path, hash, time, user1Id);
 		assertTrue(id >= 0);
 		assertEquals(dbc.fileDelete(id), 1);
 
 		assertNull(dbc.getName(hash, user1Id));
 		assertNull(dbc.getHash(path, user1Id));
 		assertEquals(dbc.getLastMod(path, user1Id), -1L);
 
 		assertEquals(dbc.fileDelete(id), 0);
 	}
 
 	@Test
 	public void testAuthUser() {
 		assertTrue(dbc.addUser("testuser", "testpw"));
 		assertEquals(dbc.authUser("testuser", "testpw"), 2);
 		assertEquals(dbc.authUser("testuser", "secondpw"), -1);
 	}
 
 	@Test
 	public void testAddUser() {
 		assertTrue(dbc.addUser("testuser2", "testpw"));
 		assertFalse(dbc.addUser("testuser2", "secondpw"));
 	}
 
 	@Test
 	public void testAccessToClosedConnection() {
 		assertTrue(dbc.disconnect());
 
 		assertTrue(dbc.disconnect());
 		assertNull(dbc.getHash(PATH, user1Id));
 		assertNull(dbc.getName(HASH, user1Id));
 		assertEquals(dbc.getLastMod(PATH, user1Id), -1L);
 		int id = dbc.fileNew(PATH, HASH, TIME, user1Id);
 		assertFalse(id >= 0);
 		assertFalse(dbc.initialize());
 		assertEquals(dbc.fileDelete(id), -1);
 
 		assertTrue(dbc.connect(config.getCloudRAIDHome() + DATABASE_FILE, "SA",
 				""));
 		assertTrue(dbc.initialize());
 	}
 
 }
