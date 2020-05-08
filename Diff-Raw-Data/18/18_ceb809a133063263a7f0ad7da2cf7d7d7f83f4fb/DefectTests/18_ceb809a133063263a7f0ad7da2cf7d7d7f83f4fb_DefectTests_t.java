 /**
  *
  *  Copyright 2005 The Apache Software Foundation or its licensors, as applicable.
  *
  *  Licensed under the Apache License, Version 2.0 (the "License");
  *  you may not use this file except in compliance with the License.
  *  You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  *  Unless required by applicable law or agreed to in writing, software
  *  distributed under the License is distributed on an "AS IS" BASIS,
  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *  See the License for the specific language governing permissions and
  *  limitations under the License.
  */
 package org.apache.tuscany.das.rdb.test;
 
 /*
  * These tests attempt to duplicate customer reported errors and then to verify
  * any necessary fix.
  * 
  */
 
 import org.apache.tuscany.das.rdb.ApplyChangesCommand;
 import org.apache.tuscany.das.rdb.Command;
 import org.apache.tuscany.das.rdb.CommandGroup;
 import org.apache.tuscany.das.rdb.SDODataTypes;
 import org.apache.tuscany.das.rdb.test.data.CustomerData;
 import org.apache.tuscany.das.rdb.test.data.OrderData;
 import org.apache.tuscany.das.rdb.test.framework.DasTest;
 
 import commonj.sdo.DataObject;
 
 public class DefectTests extends DasTest {
 
     protected void setUp() throws Exception {
         super.setUp();
         new CustomerData(getAutoConnection()).refresh();
         new OrderData(getAutoConnection()).refresh();
     }
 
     protected void tearDown() throws Exception {
         super.tearDown();
     }
 
     /**
      * Read a specific customer
      */
     public void testDiltonsInsert() throws Exception {
 
         // String sql = "insert into conmgt.serverstatus (statusid,
         // managedserverid, timestamp) values (316405209, 316405209, '2005-11-23
         // 19:29:52.636')";
         String sql = "insert into conmgt.serverstatus (managedserverid, timestamp) values (316405209, '2005-11-23 19:29:52.636')";
 
         Command insert = Command.FACTORY.createCommand(sql);
         insert.setConnection(getConnection());
         insert.execute();
 
         // Verify
         Command select = Command.FACTORY
                 .createCommand("Select * from conmgt.serverstatus where statusid = 316405209");
         select.setConnection(getConnection());
         DataObject root = select.executeQuery();
         assertEquals(1, root.getList("conmgt.serverstatus").size());
 
     }
 
     /**
      * Dilton's bug for adding new child data object
      */
     public void testAddNewOrder() throws Exception {
 
         // Read some customers and related orders
         Command select = Command.FACTORY
                 .createCommand("SELECT * FROM CUSTOMER LEFT JOIN ANORDER ON CUSTOMER.ID = ANORDER.CUSTOMER_ID");
         select.setConnection(getConnection());
 
         // Set minimum metadata necessary to describe relationship
         select.addRelationship("CUSTOMER.ID", "ANORDER.CUSTOMER_ID");
         select.addPrimaryKey("CUSTOMER.ID");
         select.addPrimaryKey("ANORDER.ID");
 
         DataObject root = select.executeQuery();
 
         DataObject cust = root.getDataObject("CUSTOMER[1]");
 
        // Save ID and Order Count
        int custID = cust.getInt("ID");
        int custOrderCount = cust.getList("ANORDER").size();
        int rootCount = root.getList("ANORDER").size();
        
         // Create a new Order and add to customer1
         DataObject order = root.createDataObject("ANORDER");
 
         order.set("ID", new Integer(99));
         order.set("PRODUCT", "The 99th product");
         order.set("QUANTITY", new Integer(99));
         cust.getList("ANORDER").add(order);
 
        assertEquals(custOrderCount + 1, cust.getList("ANORDER").size());
         // Build apply changes command
         ApplyChangesCommand apply = Command.FACTORY.createApplyChangesCommand();
         apply.setConnection(getConnection());
 
         // Metadata
         apply.addRelationship("CUSTOMER.ID", "ANORDER.CUSTOMER_ID");
         apply.addPrimaryKey("CUSTOMER.ID");
         apply.addPrimaryKey("ANORDER.ID");
 
         // Flush changes
         apply.execute(root);
 
         // verify cust1 relationship updates
         select = Command.FACTORY
                 .createCommand("SELECT * FROM CUSTOMER LEFT JOIN ANORDER ON CUSTOMER.ID = ANORDER.CUSTOMER_ID where CUSTOMER.ID = :ID");
         select.setConnection(getConnection());
        select.setParameterValue("ID", new Integer(custID));
 
         select.addRelationship("CUSTOMER.ID", "ANORDER.CUSTOMER_ID");
         select.addPrimaryKey("CUSTOMER.ID");
         select.addPrimaryKey("ANORDER.ID");
         root = select.executeQuery();
 
        assertEquals(custOrderCount + 1, root.getList("CUSTOMER[1]/ANORDER").size());
 
     }
 
     public void testDiltonsInsertWorkaround() throws Exception {
 
         // String sql = "insert into conmgt.serverstatus (statusid,
         // managedserverid, timestamp) values (316405209, 316405209, '2005-11-23
         // 19:29:52.636')";
         // String sql = "insert into conmgt.serverstatus (managedserverid,
         // timestamp) values (316405209, '2005-11-23 19:29:52.636')";
         String sql = "insert into conmgt.serverstatus (managedserverid, timestamp) values (:serverid, :timestamp)";
 
         Command insert = Command.FACTORY.createCommand(sql);
         insert.setParameterValue("serverid", new Integer(316405209));
         insert.setParameterValue("timestamp", "2005-11-23 19:29:52.636");
         insert.setConnection(getConnection());
         insert.execute();
 
         // Verify
         Command select = Command.FACTORY.createCommand("Select * from conmgt.serverstatus");
         select.setConnection(getConnection());
         DataObject root = select.executeQuery();
         assertEquals(1, root.getList("SERVERSTATUS").size());
 
     }
 
     public void testWASDefect330118() throws Exception {
 
         // Create the group and set common connection
         CommandGroup commandGroup = CommandGroup.FACTORY.createCommandGroup(getConfig("CustomersOrdersConfig.xml"));
         commandGroup.setConnection(getConnection());
 
         // Read all customers and add one
         Command read = commandGroup.getCommand("all customers");
         DataObject root = read.executeQuery();
         int numCustomers = root.getList("CUSTOMER").size();
 
         DataObject newCust = root.createDataObject("CUSTOMER");
         newCust.set("ID", new Integer(100));
         newCust.set("ADDRESS", "5528 Wells Fargo Drive");
         newCust.set("LASTNAME", "Gerkin");
 
         // Now delete this new customer
         newCust.delete();
 
         ApplyChangesCommand apply = commandGroup.getApplyChangesCommand();
         apply.execute(root);
 
         // Verify
         root = read.executeQuery();
         assertEquals(numCustomers, root.getList("CUSTOMER").size());
 
     }
 
     /**
      * Yin Chen reports ... "In the class Statement, method: public int
      * executeUpdate(Parameters parameters) - its tossing out RuntimeException
      * when the value of the parameter is null. "
      * 
      * His example build a update statement and sets one parameter value to be
      * null. I will try to duplicate with an insert since that is simpler
      * 
      */
     public void testYingChen12162005() throws Exception {
 
         Command insert = Command.FACTORY
                 .createCommand("insert into CUSTOMER values (:ID, :LASTNAME, :ADDRESS)");
         insert.setConnection(getConnection());
         insert.setParameterValue("ID", new Integer(10));
         insert.setParameterValue("LASTNAME", "Williams");
         insert.setParameterValue("ADDRESS", null);
         insert.execute();
 
         // Verify
         Command select = Command.FACTORY.createCommand("Select * from CUSTOMER where ID = 10");
         select.setConnection(getConnection());
         DataObject root = select.executeQuery();
         assertEquals(1, root.getList("CUSTOMER").size());
         assertEquals("5528 Wells Fargo Dr", root.get("CUSTOMER[1]/ADDRESS"));
 
     }
 
     /**
      * Try a workaround using CommandGroup
      */
     public void testYingChen12162005Workaraound() throws Exception {
 
         // Create the group and set common connection
         CommandGroup commandGroup = CommandGroup.FACTORY.createCommandGroup(getConfig("CustomerConfig.xml"));
         commandGroup.setConnection(getConnection());
 
         Command insert = commandGroup.getCommand("insert customer");
         insert.setParameterValue("ID", new Integer(10));
         insert.setParameterValue("LASTNAME", "Williams");
         insert.setParameterValue("ADDRESS", null);
         insert.execute();
 
         // Verify
         Command select = commandGroup.getCommand("read customer 10");
         DataObject root = select.executeQuery();
         assertEquals(1, root.getList("CUSTOMER").size());
         assertEquals("5528 Wells Fargo Dr", root.get("CUSTOMER[1]/ADDRESS"));
 
     }
     
     
 
 
     /**
      * Should be able to explicitly set a parameter to null.  But, should require
      * that the parameter type is set.
      */
     public void testDiltonsNullParameterBug1() throws Exception {
         
         Command insert = Command.FACTORY.createCommand("insert into CUSTOMER values (:ID, :LASTNAME, :ADDRESS)");   
         insert.setConnection(getConnection());
         insert.setParameterValue("ID", new Integer(10));
         insert.setParameterValue("LASTNAME", null);
         insert.setParameterType("LASTNAME", SDODataTypes.STRING);
         insert.setParameterValue("ADDRESS", "5528 Wells Fargo Dr");
         insert.execute();
 
         //Verify
         Command select = Command.FACTORY.createCommand("Select * from CUSTOMER where ID = 10");
         select.setConnection(getConnection());
         DataObject root = select.executeQuery();    
         assertEquals(1, root.getList("CUSTOMER").size());
         assertEquals("5528 Wells Fargo Dr", root.get("CUSTOMER[1]/ADDRESS"));
 
     }
    
 
     /**
      * Error by not setting a parameter
      */    
     public void testDiltonsNullParameterBug2() throws Exception {
         
         Command insert = Command.FACTORY.createCommand("insert into CUSTOMER values (:ID, :LASTNAME, :ADDRESS)");   
         insert.setConnection(getConnection());
         insert.setParameterValue("ID", new Integer(10));
 //        insert.setParameterValue("LASTNAME", null);
         insert.setParameterValue("ADDRESS", "5528 Wells Fargo Dr");
         
         try {
             insert.execute();
             fail();
         }
         catch (RuntimeException e) {
             //Expected since "LASTNAME" parameter not set
         }
     }    
     
     /**
      * Set parameter to empty string
      */    
     public void testDiltonsNullParameterBug3() throws Exception {
         
         Command insert = Command.FACTORY.createCommand("insert into CUSTOMER values (:ID, :LASTNAME, :ADDRESS)");   
         insert.setConnection(getConnection());
         insert.setParameterValue("ID", new Integer(10));
         insert.setParameterValue("LASTNAME", "");
         insert.setParameterValue("ADDRESS", "5528 Wells Fargo Dr");
         insert.execute();
 
         //Verify
         Command select = Command.FACTORY.createCommand("Select * from CUSTOMER where ID = 10");
         select.setConnection(getConnection());
         DataObject root = select.executeQuery();    
         assertEquals(1, root.getList("CUSTOMER").size());
         assertEquals("5528 Wells Fargo Dr", root.get("CUSTOMER[1]/ADDRESS"));
 
     }
     
 }
