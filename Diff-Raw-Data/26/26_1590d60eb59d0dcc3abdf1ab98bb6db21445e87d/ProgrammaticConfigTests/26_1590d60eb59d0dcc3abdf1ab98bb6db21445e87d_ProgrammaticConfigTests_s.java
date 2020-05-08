 /*
  * Licensed to the Apache Software Foundation (ASF) under one
  * or more contributor license agreements.  See the NOTICE file
  * distributed with this work for additional information
  * regarding copyright ownership.  The ASF licenses this file
  * to you under the Apache License, Version 2.0 (the
  * "License"); you may not use this file except in compliance
  * with the License.  You may obtain a copy of the License at
  * 
  *   http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an
  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  * KIND, either express or implied.  See the License for the
  * specific language governing permissions and limitations
  * under the License.    
  */
 package org.apache.tuscany.das.rdb.test;
 
 import java.sql.SQLException;
 import java.util.List;
 
 import org.apache.tuscany.das.rdb.Command;
 import org.apache.tuscany.das.rdb.ConfigHelper;
 import org.apache.tuscany.das.rdb.DAS;
 import org.apache.tuscany.das.rdb.config.Config;
 import org.apache.tuscany.das.rdb.config.ConnectionInfo;
 import org.apache.tuscany.das.rdb.config.Table;
 import org.apache.tuscany.das.rdb.test.data.BookData;
 import org.apache.tuscany.das.rdb.test.data.CustomerData;
 import org.apache.tuscany.das.rdb.test.data.OrderData;
 import org.apache.tuscany.das.rdb.test.framework.DasTest;
 
 import commonj.sdo.DataObject;
 
 /**
  * Tests the Converter framwork
  */
 public class ProgrammaticConfigTests extends DasTest {
 
     protected void setUp() throws Exception {
         super.setUp();
         new BookData(getAutoConnection()).refresh();
         new CustomerData(getAutoConnection()).refresh();
         new OrderData(getAutoConnection()).refresh();
     }
 
     protected void tearDown() throws Exception {
         super.tearDown();
     }
 
     /**
      * Simple read with command created programaticaly using the ConfigHelper.
      */
     public void test0() throws Exception {
         String commandName = "select book by id";
         String commandSQL = "SELECT * FROM BOOK WHERE BOOK_ID =?";
         // Create config programmatically
         ConfigHelper helper = new ConfigHelper();
         helper.addSelectCommand( commandName, commandSQL );
         helper.addPrimaryKey("BOOK.BOOK_ID");
         DAS das = DAS.FACTORY.createDAS(helper.getConfig(), getConnection());
 
         // Read a book instance
         Command select = das.getCommand("select book by id");
         select.setParameter(1, Integer.valueOf(1));
         DataObject root = select.executeQuery();
         DataObject book = root.getDataObject("BOOK[1]");
         // Change a field to mark the instance 'dirty'
         book.setInt("QUANTITY", 2);
 
         // Flush the change
 
         das.applyChanges(root);
 
         // Verify
         root = select.executeQuery();
         book = root.getDataObject("BOOK[1]");
         assertEquals(2, book.getInt("QUANTITY"));
     }
 
     /**
      * Simple read followed by a write. This should fail since there is no
      * config associaed with the applychanges command
      */
     public void test1() throws Exception {
         DAS das = DAS.FACTORY.createDAS(getConnection());
         // Read a book instance
         Command select = das.createCommand("SELECT * FROM BOOK WHERE BOOK_ID = 1");
         DataObject root = select.executeQuery();
 
         DataObject book = root.getDataObject("BOOK[1]");
         // Change a field to mark the instance 'dirty'
         book.setInt("QUANTITY", 2);
 
         try {
             das.applyChanges(root);
             fail("An exception should be thrown since here is no config to identify the primary key");
         } catch (RuntimeException ex) {
             // Expected
         }
     }
 
     /**
      * Simple read followed by a write. Config instance is generated
      * programmatically using the ConfigHelper.
      */
     public void test2() throws Exception {
         // Create config programmatically
         ConfigHelper helper = new ConfigHelper();
         helper.addPrimaryKey("BOOK.BOOK_ID");
         DAS das = DAS.FACTORY.createDAS(helper.getConfig(), getConnection());
 
         // Read a book instance
         Command select = das.createCommand("SELECT * FROM BOOK WHERE BOOK_ID = 1");
         DataObject root = select.executeQuery();
         DataObject book = root.getDataObject("BOOK[1]");
         // Change a field to mark the instance 'dirty'
         book.setInt("QUANTITY", 2);
 
         // Flush the change
 
         das.applyChanges(root);
 
         // Verify
         root = select.executeQuery();
         book = root.getDataObject("BOOK[1]");
         assertEquals(2, book.getInt("QUANTITY"));
     }
 
     /**
      * Test ability to read a compound graph (Read with Relationship)
      */
     public void test3() throws Exception {
 
         String statement = "SELECT * FROM CUSTOMER LEFT JOIN ANORDER ON CUSTOMER.ID = ANORDER.CUSTOMER_ID WHERE CUSTOMER.ID = 1";
 
         // Read some customers and related orders
         // Create relationship config programmatically
         ConfigHelper helper = new ConfigHelper();
         helper.addRelationship("CUSTOMER.ID", "ANORDER.CUSTOMER_ID");
         DAS das = DAS.FACTORY.createDAS(helper.getConfig(), getConnection());
         Command select = das.createCommand(statement);
 
         DataObject root = select.executeQuery();
         DataObject customer = root.getDataObject("CUSTOMER[1]");
 
         assertEquals(2, customer.getList("ANORDER").size());
 
     }
 
     /**
      * Programatically create table config with "property" name
      */
     public void test4() throws Exception {
 
         String statement = "SELECT * FROM BOOK WHERE BOOK.BOOK_ID = ?";
 
         // Create Table config programmatically
         ConfigHelper helper = new ConfigHelper();
         helper.addTable("BOOK", "Book");
         helper.addPrimaryKey("Book.BOOK_ID");
 
         DAS das = DAS.FACTORY.createDAS(helper.getConfig(), getConnection());
         Command select = das.createCommand(statement);
         select.setParameter(1, Integer.valueOf(1));
 
         DataObject root = select.executeQuery();
 
         DataObject newBook = root.createDataObject("Book");
         newBook.setString("NAME", "Ant Colonies of the Old World");
         newBook.setInt("BOOK_ID", 1001);
         root.getList("Book").add(newBook);
 
         das.applyChanges(root);
 
         //Verify
         select.setParameter(1, Integer.valueOf(1001));
         root = select.executeQuery();
         assertEquals("Ant Colonies of the Old World", root.getString("Book[1]/NAME"));
 
     }
     
     /**
      * 
      */
     public void testAddColumnWithPropertyName() throws SQLException {
         String statement = "SELECT * FROM BOOK WHERE BOOK.BOOK_ID = ?";
 
         // Create Table config programmatically
         ConfigHelper helper = new ConfigHelper();
         Table table = helper.addTable("BOOK", "Book");
         helper.addPrimaryKey("Book.BOOK_ID");
         helper.addColumn(table, "NAME", "bookName");
         DAS das = DAS.FACTORY.createDAS(helper.getConfig(), getConnection());
         Command select = das.createCommand(statement);
         select.setParameter(1, Integer.valueOf(1));
 
         DataObject root = select.executeQuery();
 
         DataObject newBook = root.createDataObject("Book");        
         newBook.setString("bookName", "Ant Colonies of the Old World");
         newBook.setInt("BOOK_ID", 1001);
         root.getList("Book").add(newBook);
 
         das.applyChanges(root);
 
         //Verify
         select.setParameter(1, Integer.valueOf(1001));
         root = select.executeQuery();
         assertEquals("Ant Colonies of the Old World", root.getString("Book[1]/bookName"));
     }
     /**
      * Simple unit test for ConnectionInfo
      * @throws Exception
      */
     public void testConnectionInfo() throws Exception {
         ConfigHelper helper = new ConfigHelper();
         helper.addConnectionInfo("jdbc/adatasource");
 
         Config config = helper.getConfig();
         ConnectionInfo info = config.getConnectionInfo();
         assertEquals(info.getDataSource(), "jdbc/adatasource");
         assertEquals(info.isManagedtx(), true);
     }
 
     /**
      * Simple unit test for ConnectionInfo
      * @throws Exception
      */
     public void testConnectionInfo2() throws Exception {
         ConfigHelper helper = new ConfigHelper();
         helper.addConnectionInfo("jdbc/adatasource", false);
 
         Config config = helper.getConfig();
         ConnectionInfo info = config.getConnectionInfo();
         assertEquals(info.getDataSource(), "jdbc/adatasource");
         assertEquals(info.isManagedtx(), false);
     }
 
     /**
      * Simple unit test for ConnectionInfo using DriverManager
      * @throws Exception
      */
     public void testConnectionInfoDriverManager() throws Exception {
         ConfigHelper helper = new ConfigHelper();
        helper.addConnectionInfo("jdbc/adatasource", false, true);
 
         Config config = helper.getConfig();
         ConnectionInfo info = config.getConnectionInfo();
         assertEquals(info.getDataSource(), "jdbc/adatasource");
         assertEquals(info.isManagedtx(), false);
        assertEquals(info.isUseDriveManager(), true);
     }    
     
     /**
      * Simple unit test for ConnectionInfo using DriverManager
      * @throws Exception
      */
     public void testConnectionInfoDriverManager2() throws Exception {
         ConfigHelper helper = new ConfigHelper();
         helper.addConnectionInfo("jdbc/adatasource", false, true, "jdbc:derby:target/dastest", "user", "password", 600);
 
         Config config = helper.getConfig();
         ConnectionInfo info = config.getConnectionInfo();
         assertEquals(info.getDataSource(), "jdbc/adatasource");
         assertEquals(info.isManagedtx(), false);
         assertEquals(info.getConnectionProperties().getDriverClass(), "jdbc:derby:target/dastest");
         assertEquals(info.getConnectionProperties().getUserName(), "user");
         assertEquals(info.getConnectionProperties().getPassword(), "password");
         assertEquals(info.getConnectionProperties().getLoginTimeout(), 600);
     }  
     
     /**
      * Simple unit test for adding a select command
      * @throws Exception
      */
     public void testAddSelectCommand() throws Exception {
         ConfigHelper helper = new ConfigHelper();
         helper.addSelectCommand("get all customers", "select * from customers");
 
         Config config = helper.getConfig();
         List commands = config.getCommand();
         assertEquals(1, commands.size());
         org.apache.tuscany.das.rdb.config.Command cmd = (org.apache.tuscany.das.rdb.config.Command) commands.get(0);
         assertEquals("select", cmd.getKind());
         assertEquals("get all customers", cmd.getName());
         assertEquals("select * from customers", cmd.getSQL());
     }
         
 
     /**
      * Simple unit test for adding an update command
      * @throws Exception
      */
     public void testAddUpdateCommand() throws Exception {
         ConfigHelper helper = new ConfigHelper();
         helper.addUpdateCommand("update a customer", "update customers set name = ? where id = ?");
 
         Config config = helper.getConfig();
         List commands = config.getCommand();
         assertEquals(1, commands.size());
         org.apache.tuscany.das.rdb.config.Command cmd = (org.apache.tuscany.das.rdb.config.Command) commands.get(0);
         assertEquals("update", cmd.getKind());
         assertEquals("update a customer", cmd.getName());
         assertEquals("update customers set name = ? where id = ?", cmd.getSQL());
     }
 
     /**
      * Simple unit test for adding an insert command
      * @throws Exception
      */
     public void testAddInsertCommand() throws Exception {
         ConfigHelper helper = new ConfigHelper();
         helper.addInsertCommand("insert customer", "insert into customers(ID,NAME) values (?,?)");
 
         Config config = helper.getConfig();
         List commands = config.getCommand();
         assertEquals(1, commands.size());
         org.apache.tuscany.das.rdb.config.Command cmd = (org.apache.tuscany.das.rdb.config.Command) commands.get(0);
         assertEquals("insert", cmd.getKind());
         assertEquals("insert customer", cmd.getName());
         assertEquals("insert into customers(ID,NAME) values (?,?)", cmd.getSQL());
     }
 
     /**
      * Simple unit test for adding a delete command
      * @throws Exception
      */
     public void testAddDeleteCommand() throws Exception {
         ConfigHelper helper = new ConfigHelper();
         helper.addDeleteCommand("delete customer", "delete from customers where id = ?");
 
         Config config = helper.getConfig();
         List commands = config.getCommand();
         assertEquals(1, commands.size());
         org.apache.tuscany.das.rdb.config.Command cmd = (org.apache.tuscany.das.rdb.config.Command) commands.get(0);
         assertEquals("delete", cmd.getKind());
         assertEquals("delete customer", cmd.getName());
         assertEquals("delete from customers where id = ?", cmd.getSQL());
     }
 
     /**
      * Simple unit test for DataObjectModel
      * @throws Exception
      */
     public void testDataObjectModel() throws Exception {
         ConfigHelper helper = new ConfigHelper();
         helper.setDataObjectModel("org.apache.tuscany/mytypes");
 
         Config config = helper.getConfig();
         assertEquals("org.apache.tuscany/mytypes", config.getDataObjectModel());
 
     }
 
     /**
      * Simple unit test for adding a Delete statement to a Table
      * @throws Exception
      */
     public void testAddDeleteStatement() throws Exception {
         ConfigHelper helper = new ConfigHelper();
         Table table = helper.addTable("widgets", "WIDGETS");
         helper.addDeleteStatement(table, "delete from widgets where id = ?", "ID");
 
         Config cfg = helper.getConfig();
         assertEquals(1, cfg.getTable().size());
         Table widgets = (Table) cfg.getTable().get(0);
         assertEquals("delete from widgets where id = ?", widgets.getDelete().getSql());
         assertEquals("WIDGETS", widgets.getTypeName());
         assertEquals("ID", widgets.getDelete().getParameters());
 
     }
 
     /**
      * Simple unit test for adding a Create statement to a Table
      * @throws Exception
      */
     public void testAddCreateStatement() throws Exception {
         ConfigHelper helper = new ConfigHelper();
         Table table = helper.addTable("widgets", "WIDGETS");
         helper.addCreateStatement(table, "insert into widgets values (?,?)", "ID NAME");
 
         Config cfg = helper.getConfig();
         assertEquals(1, cfg.getTable().size());
         Table widgets = (Table) cfg.getTable().get(0);
         assertEquals("insert into widgets values (?,?)", widgets.getCreate().getSql());
         assertEquals("WIDGETS", widgets.getTypeName());
         assertEquals("ID NAME", widgets.getCreate().getParameters());
 
     }
     
     public void testAddInvalidPrimaryKey() throws Exception {
         ConfigHelper helper = new ConfigHelper();
         try {
             helper.addPrimaryKey("PK");
         } catch (RuntimeException ex) {
             assertEquals ("Column PK must be qualified with a table name", ex.getMessage());
         }
     }
 }
