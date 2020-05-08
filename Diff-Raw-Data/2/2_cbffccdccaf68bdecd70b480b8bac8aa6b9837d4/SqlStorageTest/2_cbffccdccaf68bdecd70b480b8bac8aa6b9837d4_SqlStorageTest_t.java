 /*
  * This file is part of Bolt.
  *
  * Copyright (c) 2012, AlmuraDev <http://www.almuramc.com/>
  * Bolt is licensed under the Almura Development License.
  *
  * Bolt is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * As an exception, all classes which do not reference GPL licensed code
  * are hereby licensed under the GNU Lesser Public License, as described
  * in the Almura Development License.
  *
  * Bolt is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License,
  * the GNU Lesser Public License (for classes that fulfill the exception)
  * and the Almura Development License along with this program. If not, see
  * <http://www.gnu.org/licenses/> for the GNU General Public License and
  * the GNU Lesser Public License.
  */
 package com.almuramc.bolt.storage;
 
 import java.io.File;
 import java.io.IOException;
 import java.nio.file.Files;
 import java.nio.file.Path;
 import java.util.UUID;
 
 import com.almuramc.bolt.lock.type.BasicLock;
 import com.almuramc.bolt.storage.sql.RegistryTable;
 import com.alta189.simplesave.DatabaseFactory;
 import com.alta189.simplesave.exceptions.ConnectionException;
 import com.alta189.simplesave.exceptions.TableRegistrationException;
 import com.alta189.simplesave.h2.H2Configuration;
 import com.alta189.simplesave.h2.H2Database;
 import com.alta189.simplesave.sqlite.SQLiteConfiguration;
 import com.alta189.simplesave.sqlite.SQLiteDatabase;
 
 import org.junit.Rule;
 import org.junit.Test;
 import org.junit.rules.TemporaryFolder;
 
 import static junit.framework.Assert.assertEquals;
 import static junit.framework.Assert.assertFalse;
 import static junit.framework.Assert.assertNotNull;
 import static junit.framework.Assert.assertNotSame;
 import static junit.framework.Assert.assertTrue;
 import static junit.framework.Assert.fail;
 
 public class SqlStorageTest {
	@Test
 	public void testSqlite() {
 		SQLiteConfiguration config = new SQLiteConfiguration();
 		File tmpfile = null;
 		try {
 			tmpfile = File.createTempFile("sqlite_test_", ".db");
 		} catch (IOException e) {
 			e.printStackTrace();
 			fail("IOException occured: " + e.toString());
 		}
 		assertNotNull(tmpfile);
 		config.setPath(tmpfile.getAbsolutePath().substring(0, tmpfile.getAbsolutePath().indexOf(".db")));
 		tmpfile.deleteOnExit();
 		SQLiteDatabase db = (SQLiteDatabase) DatabaseFactory.createNewDatabase(config);
 		try {
 			db.registerTable(RegistryTable.class);
 		} catch (TableRegistrationException e) {
 			e.printStackTrace();
 			fail("Exception occured too early! " + e.toString());
 		}
 		try {
 			db.connect();
 		} catch (ConnectionException e) {
 			fail("Failed to connect to database! " + e.toString());
 		}
 		final RegistryTable one = new RegistryTable();
 		one.setLock(new BasicLock("Charlie", null, UUID.randomUUID(), 1, 1, 1));
 		db.save(RegistryTable.class, one);
 		assertEquals(db.select(RegistryTable.class).execute().find().size(), 1);
 		try {
 			db.close();
 		} catch (ConnectionException e) {
 			fail("Failed to close database! " + e.toString());
 		}
 		tmpfile.delete();
 	}
 
 	@Test
 	public void testH2() {
 		final BasicLock test = new BasicLock("Charlie", null, UUID.randomUUID(), 1, 1, 1);
 		final BasicLock test2 = new BasicLock("Charlie", null, UUID.randomUUID(), 1, 1, 1);
 		H2Configuration h2 = new H2Configuration();
 		File tmpfile = null;
 		try {
 			tmpfile = File.createTempFile("h2_test_", ".db");
 		} catch (IOException e) {
 			e.printStackTrace();
 			fail("IOException occurred: " + e.toString());
 		}
 		assertNotNull(tmpfile);
 		h2.setDatabase(tmpfile.getAbsolutePath().substring(0, tmpfile.getAbsolutePath().indexOf(".db")));
 		tmpfile.deleteOnExit();
 		H2Database db = (H2Database) DatabaseFactory.createNewDatabase(h2);
 		try {
 			db.registerTable(RegistryTable.class);
 		} catch (TableRegistrationException e) {
 			e.printStackTrace();
 			fail("Exception occurred too early! " + e.toString());
 		}
 		try {
 			db.connect();
 		} catch (ConnectionException e) {
 			fail("Failed to connect to database! " + e.toString());
 		}
 		db.save(new RegistryTable(test));
 		db.save(new RegistryTable(test2));
 		assertEquals(db.select(RegistryTable.class).execute().find().size(), 2);
 		assertEquals(db.select(RegistryTable.class).where().equal("lock", test).execute().findOne().getLock(), test);
 		assertEquals(db.select(RegistryTable.class).where().equal("lock", test2).execute().findOne().getLock(), test2);
 		try {
 			db.close();
 		} catch (ConnectionException e) {
 			fail("Failed to close database! " + e.toString());
 		}
 		tmpfile.delete();
 	}
 
 	@Test
 	public void testBackend() {
 		Path test = null;
 		try {
 			test = Files.createTempDirectory("test");
 		} catch (IOException e) {
 			fail("Could not create temporary folder!");
 		}
 		SqlStorage storage = new SqlStorage(new H2Configuration(), test.toFile());
 		storage.onLoad();
 		BasicLock a = new BasicLock("Charlie", null, UUID.randomUUID(), 1, 1, 1);
 		BasicLock b = new BasicLock("Charlie", null, UUID.randomUUID(), 1, 1, 1);
 		storage.addLock(a);
 		storage.addLock(b);
 		assertEquals(storage.getAll().size(), 2);
 		storage.removeLock(b);
 		assertEquals(storage.getAll().size(), 1);
 		assertTrue(storage.getAll().contains(a));
 		storage.onUnLoad();
 	}
 }
