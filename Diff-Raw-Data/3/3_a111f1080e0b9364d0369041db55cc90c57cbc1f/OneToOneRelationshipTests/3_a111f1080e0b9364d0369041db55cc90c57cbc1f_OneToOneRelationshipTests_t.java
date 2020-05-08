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
 
 import org.apache.tuscany.das.rdb.Command;
 import org.apache.tuscany.das.rdb.CommandGroup;
 import org.apache.tuscany.das.rdb.test.data.CompanyData;
 import org.apache.tuscany.das.rdb.test.data.CompanyEmpData;
 import org.apache.tuscany.das.rdb.test.data.EmployeeData;
 import org.apache.tuscany.das.rdb.test.framework.DasTest;
 
 import commonj.sdo.DataObject;
 
 public class OneToOneRelationshipTests extends DasTest {
 
     protected void setUp() throws Exception {
         super.setUp();
         new CompanyData(getAutoConnection()).refresh();
         new EmployeeData(getAutoConnection()).refresh();
         new CompanyEmpData(getAutoConnection()).refresh();
     }
 
     protected void tearDown() throws Exception {
         super.tearDown();
     }
 
     
     /**
      * Read Company and traverse to EOTM
      */
     public void test1() throws Exception {
 
         CommandGroup commandGroup = CommandGroup.FACTORY
                 .createCommandGroup(getConfig("CompanyEmployeeConfig.xml"));
 
         Command read = commandGroup.getCommand("get companies with employee of the month");
         DataObject root = read.executeQuery();
         DataObject employee = root.getDataObject("COMPANY[1]/company->employee_opposite");
 
         assertEquals("Mary Smith", employee.getString("NAME"));
     }
 
     
     /**
      * Read Employee and traverse to Company
      */
     public void test2() throws Exception {
 
         CommandGroup commandGroup = CommandGroup.FACTORY
                 .createCommandGroup(getConfig("CompanyEmployeeConfig.xml"));
 
         Command read = commandGroup.getCommand("get named employee with company");
         read.setParameterValue("NAME", "Mary Smith");
         DataObject root = read.executeQuery();
         DataObject company = root.getDataObject("EMPLOYEE[1]/company->employee");
 
         assertEquals("ACME Publishing", company.getString("NAME"));
     }
 
     
     /**
      * Un-assign employee O' month
      */
     public void test3() throws Exception {
 
         CommandGroup commandGroup = CommandGroup.FACTORY
                 .createCommandGroup(getConfig("CompanyEmployeeConfig.xml"));
 
         Command read = commandGroup.getCommand("get companies with employee of the month");
         DataObject root = read.executeQuery();
         DataObject company = root.getDataObject("COMPANY[1]");
         company.setDataObject("company->employee_opposite", null);
         assertNull(company.getDataObject("company->employee_opposite"));
    
         //Flush changes
         commandGroup.getApplyChangesCommand().execute(root);
 
         //Verify
         root = read.executeQuery();
         company = root.getDataObject("COMPANY[1]");
        assertNull(company.getDataObject("company->employee_opposite"));
     }
     
     
     
 }
