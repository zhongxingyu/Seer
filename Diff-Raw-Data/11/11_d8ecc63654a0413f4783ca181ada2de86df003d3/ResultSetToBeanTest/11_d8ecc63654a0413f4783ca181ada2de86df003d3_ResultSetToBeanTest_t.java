 /*
  * Copyright 2004-2006 the Seasar Foundation and the Others.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, 
  * either express or implied. See the License for the specific language
  * governing permissions and limitations under the License.
  */
 package org.seasar.dao.impl;
 
 import org.seasar.dao.DaoMetaData;
 import org.seasar.dao.SqlCommand;
 import org.seasar.dao.unit.S2DaoTestCase;
 
 /**
  * @author manhole
  */
 public class ResultSetToBeanTest extends S2DaoTestCase {
 
     protected void setUp() throws Exception {
         super.setUp();
         include("j2ee.dicon");
     }
 
     // https://www.seasar.org/issues/browse/DAO-26
     public void testMappingByPropertyNameTx() throws Exception {
         // ## Arrange ##
         final DaoMetaData dmd = createDaoMetaData(EmployeeDao.class);
         {
             final SqlCommand insertCommand = dmd.getSqlCommand("insert");
             Employee bean = new Employee();
             bean.setDepartmentId(new Integer(123));
             bean.setEmployeeId(new Integer(7650));
             bean.setEmployeeName("foo");
             insertCommand.execute(new Object[] { bean });
         }
 
         // ## Act ##
         // ## Assert ##
         {
             final SqlCommand command = dmd.getSqlCommand("find1");
            Employee bean = (Employee) command.execute(null);
             assertEquals("foo", bean.getEmployeeName());
         }
         {
             final SqlCommand command = dmd.getSqlCommand("find2");
            Employee bean = (Employee) command.execute(null);
             assertEquals("foo", bean.getEmployeeName());
         }
     }
 
     public static interface EmployeeDao {
 
         Class BEAN = Employee.class;
 
         String find1_SQL = "SELECT employee_name FROM EMP3";
 
         Employee find1();
 
         String find2_SQL = "SELECT employee_name AS employeeName FROM EMP3";
 
         Employee find2();
 
         int insert(Employee bean);
 
     }
 
     public static class Employee {
 
         private static final long serialVersionUID = 1L;
 
         public static final String TABLE = "EMP3";
 
         private Integer employeeId;
 
         private String employeeName;
 
         private Integer departmentId;
 
         public Integer getDepartmentId() {
             return departmentId;
         }
 
         public void setDepartmentId(Integer departmentId) {
             this.departmentId = departmentId;
         }
 
         public Integer getEmployeeId() {
             return employeeId;
         }
 
         public void setEmployeeId(Integer employeeId) {
             this.employeeId = employeeId;
         }
 
         public String getEmployeeName() {
             return employeeName;
         }
 
         public void setEmployeeName(String employeeName) {
             this.employeeName = employeeName;
         }
 
     }
 
 }
