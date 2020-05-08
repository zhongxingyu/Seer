 /*
  * Copyright 2004-2007 the Seasar Foundation and the Others.
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
 import org.seasar.framework.exception.SRuntimeException;
 import org.seasar.framework.util.StringUtil;
 
 public class InsertAutoDynamicCommandTest extends S2DaoTestCase {
 
     public void testExecuteTx() throws Exception {
         DaoMetaData dmd = createDaoMetaData(EmployeeAutoDao.class);
         SqlCommand cmd = dmd.getSqlCommand("insert");
         assertTrue(cmd instanceof InsertAutoDynamicCommand);
         Employee emp = new Employee();
         emp.setEmpno(99);
         emp.setEname("hoge");
         Integer count = (Integer) cmd.execute(new Object[] { emp });
         assertEquals("1", new Integer(1), count);
     }
 
     /*
      * https://www.seasar.org/issues/browse/DAO-29
      */
     public void testInsertPkOnlyTx() throws Exception {
         // ## Arrange ##
         DaoMetaData dmd = createDaoMetaData(EmpDao.class);
         SqlCommand cmd = dmd.getSqlCommand("insert");
 
         // ## Act ##
         Emp emp = new Emp();
         emp.setEmpno(new Integer(980));
 
         // ## Assert ##
         cmd.execute(new Object[] { emp });
     }
 
     /*
      * https://www.seasar.org/issues/browse/DAO-29
      */
     public void testInsertAllNullTx() throws Exception {
         // ## Arrange ##
         final DaoMetaData dmd = createDaoMetaData(IdentityTableAutoDao.class);
         final SqlCommand cmd = dmd.getSqlCommand("insert");
         final IdentityTable table = new IdentityTable();
 
         // ## Act ##
         // ## Assert ##
         try {
             cmd.execute(new Object[] { table });
             fail();
         } catch (SRuntimeException e) {
             final String message = e.getMessage();
             assertEquals(true, StringUtil.contains(message, "EDAO0014"));
         }
     }
 
     /*
      * https://www.seasar.org/issues/browse/DAO-89
      * TABLEと関連付いていないDaoとBeanではINSERT時にわかりやすいエラーメッセージを出すこと。
      */
     public void testInsertNoTableTx() throws Exception {
         // ## Arrange ##
         final DaoMetaData dmd = createDaoMetaData(FooDtoDao.class);
         final SqlCommand cmd = dmd.getSqlCommand("insert");
         final FooDto dto = new FooDto();
 
         // ## Act ##
         // ## Assert ##
         try {
             cmd.execute(new Object[] { dto });
             fail();
        } catch (final SRuntimeException e) {
             final String message = e.getMessage();
             System.out.println(message);
             assertEquals(true, StringUtil.contains(message, "EDAO0024"));
         }
     }
 
     public void testExecute2Tx() throws Exception {
         DaoMetaData dmd = createDaoMetaData(IdentityTableAutoDao.class);
         SqlCommand cmd = dmd.getSqlCommand("insert");
         IdentityTable table = new IdentityTable();
         table.setIdName("hoge");
         Integer count1 = (Integer) cmd.execute(new Object[] { table });
         assertEquals("1", new Integer(1), count1);
         int id1 = table.getMyid();
         System.out.println(id1);
         Integer count2 = (Integer) cmd.execute(new Object[] { table });
         assertEquals("1", new Integer(1), count2);
         int id2 = table.getMyid();
         System.out.println(id2);
 
         assertEquals("2", 1, id2 - id1);
     }
 
     public void testExecute3_1Tx() throws Exception {
         DaoMetaData dmd = createDaoMetaData(SeqTable1Dao.class);
         SqlCommand cmd = dmd.getSqlCommand("insert");
         SeqTable1 table1 = new SeqTable1();
         table1.setName("hoge");
         Integer count = (Integer) cmd.execute(new Object[] { table1 });
         assertEquals("1", new Integer(1), count);
         System.out.println(table1.getId());
         assertTrue("2", table1.getId() > 0);
     }
 
     public void testExecute3_2Tx() throws Exception {
         DaoMetaData dmd = createDaoMetaData(SeqTable2Dao.class);
         SqlCommand cmd = dmd.getSqlCommand("insert");
         SeqTable2 table1 = new SeqTable2();
         table1.setName("hoge");
         Integer count = (Integer) cmd.execute(new Object[] { table1 });
         assertEquals("1", new Integer(1), count);
         System.out.println(table1.getId());
         assertTrue("2", table1.getId().intValue() > 0);
 
         SeqTable2 table2 = new SeqTable2();
         table2.setName("foo");
         cmd.execute(new Object[] { table2 });
         System.out.println(table2.getId());
         assertEquals(true, table2.getId().intValue() > table1.getId()
                 .intValue());
     }
 
     public void testExecute4Tx() throws Exception {
         DaoMetaData dmd = createDaoMetaData(EmployeeAutoDao.class);
         SqlCommand cmd = dmd.getSqlCommand("insert2");
         Employee emp = new Employee();
         emp.setEmpno(99);
         emp.setEname("hoge");
         Integer count = (Integer) cmd.execute(new Object[] { emp });
         assertEquals("1", new Integer(1), count);
     }
 
     public void testExecute5Tx() throws Exception {
         DaoMetaData dmd = createDaoMetaData(EmployeeAutoDao.class);
         SqlCommand cmd = dmd.getSqlCommand("insert3");
         Employee emp = new Employee();
         emp.setEmpno(99);
         emp.setEname("hoge");
         emp.setDeptno(10);
         Integer count = (Integer) cmd.execute(new Object[] { emp });
         assertEquals("1", new Integer(1), count);
     }
 
     public void setUp() {
         include("j2ee.dicon");
     }
 
     public static interface SeqTable1Dao {
 
         public Class BEAN = SeqTable1.class;
 
         public void insert(SeqTable1 seqTable);
     }
 
     public static class SeqTable1 {
 
         public static final String TABLE = "SEQTABLE";
 
         public static final String id_ID = "sequence, sequenceName=myseq";
 
         private int id;
 
         private String name;
 
         public int getId() {
             return id;
         }
 
         public void setId(int id) {
             this.id = id;
         }
 
         public String getName() {
             return name;
         }
 
         public void setName(String name) {
             this.name = name;
         }
     }
 
     public static interface SeqTable2Dao {
 
         public Class BEAN = SeqTable2.class;
 
         public void insert(SeqTable2 seqTable);
 
     }
 
     public static class SeqTable2 {
 
         public static final String TABLE = "SEQTABLE";
 
         public static final String id_ID = "sequence, sequenceName=myseq";
 
         private Integer id;
 
         private String name;
 
         public Integer getId() {
             return id;
         }
 
         public void setId(Integer id) {
             this.id = id;
         }
 
         public String getName() {
             return name;
         }
 
         public void setName(String name) {
             this.name = name;
         }
     }
 
     public static interface EmpDao {
 
         public Class BEAN = Emp.class;
 
         public void insert(Emp employee);
 
     }
 
     public static class Emp {
 
         public static final String TABLE = "EMP";
 
         private Integer empno;
 
         private String ename;
 
         public Integer getEmpno() {
             return this.empno;
         }
 
         public void setEmpno(Integer empno) {
             this.empno = empno;
         }
 
         public String getEname() {
             return this.ename;
         }
 
         public void setEname(String ename) {
             this.ename = ename;
         }
 
     }
 
     public static interface FooDtoDao {
 
         public Class BEAN = FooDto.class;
 
         public void insert(FooDto employee);
 
     }
 
     public static class FooDto {
 
         public static final String TABLE = "DUMMY_TABLE";
 
         private Integer empno;
 
         private String ename;
 
         public Integer getEmpno() {
             return this.empno;
         }
 
         public void setEmpno(Integer empno) {
             this.empno = empno;
         }
 
         public String getEname() {
             return this.ename;
         }
 
         public void setEname(String ename) {
             this.ename = ename;
         }
 
     }
 
 }
