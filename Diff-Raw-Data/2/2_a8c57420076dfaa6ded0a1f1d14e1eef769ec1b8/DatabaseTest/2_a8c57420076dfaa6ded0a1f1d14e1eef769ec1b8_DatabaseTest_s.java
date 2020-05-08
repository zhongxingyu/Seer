 /*
  * This file is part of GreatmancodeTools.
  *
  * Copyright (c) 2013-2013, Greatman <http://github.com/greatman/>
  *
  * GreatmancodeTools is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * GreatmancodeTools is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public License
  * along with GreatmancodeTools.  If not, see <http://www.gnu.org/licenses/>.
  */
 package com.greatmancode.tools;
 
 import java.io.File;
 import java.net.URISyntaxException;
 
 import com.alta189.simplesave.exceptions.ConnectionException;
 import com.alta189.simplesave.exceptions.TableRegistrationException;
 import com.greatmancode.tools.database.DatabaseManager;
 import com.greatmancode.tools.database.interfaces.DatabaseType;
 import com.greatmancode.tools.database.throwable.InvalidDatabaseConstructor;
 import com.greatmancode.tools.tables.TestTable;
 
 import org.junit.Test;
 
 import static junit.framework.Assert.assertEquals;
 import static junit.framework.Assert.assertNotNull;
 
 public class DatabaseTest {
 	@Test
 	public void test() throws URISyntaxException, InvalidDatabaseConstructor, TableRegistrationException, ConnectionException {
		DatabaseManager dbManager = new DatabaseManager(DatabaseType.SQLite, "test_", new File(new File(ConfigurationTest.class.getProtectionDomain().getCodeSource().getLocation().toURI()), "testConfig.db"));
 		dbManager.registerTable(TestTable.class);
 		dbManager.connect();
 
 		TestTable table = new TestTable();
 		table.test = "wow";
 		dbManager.getDatabase().save(table);
 		table = dbManager.getDatabase().select(TestTable.class).where().equal("test", "wow").execute().findOne();
 		assertNotNull(table);
 		assertEquals("wow", table.test);
 	}
 }
 
