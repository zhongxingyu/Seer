 /*******************************************************************************
  * Copyright (c) 2011 Oracle. All rights reserved.
  * This program and the accompanying materials are made available under the
  * terms of the Eclipse Public License v1.0 and Eclipse Distribution License v. 1.0
  * which accompanies this distribution.
  * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
  * and the Eclipse Distribution License is available at
  * http://www.eclipse.org/org/documents/edl-v10.php.
  *
  * Contributors:
  *     Mike Norman - June 10 2011, created DDL parser package
  *     David McCann - July 2011, visit tests
  ******************************************************************************/
 package org.eclipse.persistence.tools.oracleddl.test.databasetypebuilder;
 
 //javase imports
 import java.sql.Connection;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.List;
 
 //JUnit4 imports
 import org.junit.AfterClass;
 import org.junit.BeforeClass;
 import org.junit.Test;
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertTrue;
 import static org.junit.Assert.fail;
 
 //DDL imports
 import org.eclipse.persistence.tools.oracleddl.metadata.ArgumentType;
 import org.eclipse.persistence.tools.oracleddl.metadata.DatabaseType;
 import org.eclipse.persistence.tools.oracleddl.metadata.PLSQLCursorType;
 import org.eclipse.persistence.tools.oracleddl.metadata.PLSQLPackageType;
 import org.eclipse.persistence.tools.oracleddl.metadata.ProcedureType;
 import org.eclipse.persistence.tools.oracleddl.metadata.ROWTYPEType;
 import org.eclipse.persistence.tools.oracleddl.metadata.TableType;
 import org.eclipse.persistence.tools.oracleddl.util.DatabaseTypeBuilder;
 
 //testing imports
 import org.eclipse.persistence.tools.oracleddl.test.AllTests;
 import static org.eclipse.persistence.tools.oracleddl.test.TestHelper.DATABASE_DDL_CREATE_KEY;
 import static org.eclipse.persistence.tools.oracleddl.test.TestHelper.DATABASE_DDL_DEBUG_KEY;
 import static org.eclipse.persistence.tools.oracleddl.test.TestHelper.DATABASE_DDL_DROP_KEY;
 import static org.eclipse.persistence.tools.oracleddl.test.TestHelper.DATABASE_USERNAME_KEY;
 import static org.eclipse.persistence.tools.oracleddl.test.TestHelper.DEFAULT_DATABASE_DDL_CREATE;
 import static org.eclipse.persistence.tools.oracleddl.test.TestHelper.DEFAULT_DATABASE_DDL_DEBUG;
 import static org.eclipse.persistence.tools.oracleddl.test.TestHelper.DEFAULT_DATABASE_DDL_DROP;
 import static org.eclipse.persistence.tools.oracleddl.test.TestHelper.DEFAULT_DATABASE_USERNAME;
 import static org.eclipse.persistence.tools.oracleddl.test.TestHelper.buildConnection;
 import static org.eclipse.persistence.tools.oracleddl.test.TestHelper.runDdl;
 
 public class PackagewStronglyTypedCursorDDLTestSuite {
 
     static final String STRONGLY_TYPED_REF_CURSOR_TABLE = "STRC_TABLE";
     static final String CREATE_STRONGLY_TYPED_REF_CURSOR_TABLE =
         "CREATE TABLE " + STRONGLY_TYPED_REF_CURSOR_TABLE + " (" +
             "\nID NUMBER NOT NULL," +
             "\nNAME VARCHAR(25)," +
             "\nSINCE DATE," +
             "\nPRIMARY KEY (ID)" +
         "\n)";
     static final String[] POPULATE_STRONGLY_TYPED_REF_CURSOR_TABLE = new String[] {
         "INSERT INTO " + STRONGLY_TYPED_REF_CURSOR_TABLE + " (ID, NAME, SINCE) VALUES (1, 'mike', " +
             "TO_DATE('2001-12-25 00:00:00','YYYY-MM-DD HH24:MI:SS'))",
         "INSERT INTO " + STRONGLY_TYPED_REF_CURSOR_TABLE + " (ID, NAME, SINCE) VALUES (2, 'blaise', " +
             "TO_DATE('2002-02-12 00:00:00','YYYY-MM-DD HH24:MI:SS'))",
         "INSERT INTO " + STRONGLY_TYPED_REF_CURSOR_TABLE + " (ID, NAME, SINCE) VALUES (3, 'rick', " +
             "TO_DATE('2001-10-30 00:00:00','YYYY-MM-DD HH24:MI:SS'))",
         "INSERT INTO " + STRONGLY_TYPED_REF_CURSOR_TABLE + " (ID, NAME, SINCE) VALUES (4, 'mikey', " +
             "TO_DATE('2010-01-01 00:00:00','YYYY-MM-DD HH24:MI:SS'))"
     };
     static final String STRONGLY_TYPED_REF_CURSOR_PROC = "GET_EMS";
     static final String STRONGLY_TYPED_REF_CURSOR = "STRONGLY_TYPED_REF_CURSOR";
     static final String STRONGLY_TYPED_REF_CURSOR_TEST_PACKAGE = STRONGLY_TYPED_REF_CURSOR + "_TEST";
     static final String CREATE_STRONGLY_TYPED_REF_CURSOR_TEST_PACKAGE =
         "CREATE OR REPLACE PACKAGE " + STRONGLY_TYPED_REF_CURSOR_TEST_PACKAGE + " AS" +
             "\nTYPE " + STRONGLY_TYPED_REF_CURSOR + " IS REF CURSOR RETURN " + STRONGLY_TYPED_REF_CURSOR_TABLE + "%ROWTYPE;" +
             "\nPROCEDURE " + STRONGLY_TYPED_REF_CURSOR_PROC + "(P_EMS " + STRONGLY_TYPED_REF_CURSOR_TABLE+".NAME%TYPE, P_EMS_SET OUT " +
             STRONGLY_TYPED_REF_CURSOR + ");" +
         "\nEND " + STRONGLY_TYPED_REF_CURSOR_TEST_PACKAGE + ";";
     static final String CREATE_STRONGLY_TYPED_REF_CURSOR_TEST_PACKAGE_BODY =
         "CREATE OR REPLACE PACKAGE BODY " + STRONGLY_TYPED_REF_CURSOR_TEST_PACKAGE + " AS" +
             "\nPROCEDURE GET_EMS(P_EMS " + STRONGLY_TYPED_REF_CURSOR_TABLE+".NAME%TYPE, P_EMS_SET OUT " +
                 STRONGLY_TYPED_REF_CURSOR + ") AS" +
             "\nBEGIN" +
             "\n    OPEN P_EMS_SET FOR" +
             "\n        SELECT ID, NAME, SINCE FROM " + STRONGLY_TYPED_REF_CURSOR_TABLE +
               " WHERE NAME LIKE P_EMS;" +
             "\nEND GET_EMS;" +
         "\nEND " + STRONGLY_TYPED_REF_CURSOR_TEST_PACKAGE + ";";
 
     static final String DROP_STRONGLY_TYPED_REF_CURSOR_TEST_PACKAGE =
         "DROP PACKAGE " + STRONGLY_TYPED_REF_CURSOR_TEST_PACKAGE;
     static final String DROP_STRONGLY_TYPED_REF_CURSOR_TABLE =
         "DROP TABLE " + STRONGLY_TYPED_REF_CURSOR_TABLE;
 
     //fixtures
     static DatabaseTypeBuilder dtBuilder = DatabaseTypeBuilderTestSuite.dtBuilder;
     static Connection conn = AllTests.conn;
     static PLSQLPackageType packageType = null;
     static ProcedureType procType = null;
     static ArgumentType outCursorArg = null;
 
     static boolean ddlCreate = false;
     static boolean ddlDrop = false;
     static boolean ddlDebug = false;
 
     @BeforeClass
     public static void setUp() throws SQLException, ClassNotFoundException {
         conn = buildConnection();
         dtBuilder = new DatabaseTypeBuilder();
         String ddlCreateProp = System.getProperty(DATABASE_DDL_CREATE_KEY, DEFAULT_DATABASE_DDL_CREATE);
         if ("true".equalsIgnoreCase(ddlCreateProp)) {
             ddlCreate = true;
         }
         String ddlDropProp = System.getProperty(DATABASE_DDL_DROP_KEY, DEFAULT_DATABASE_DDL_DROP);
         if ("true".equalsIgnoreCase(ddlDropProp)) {
             ddlDrop = true;
         }
         String ddlDebugProp = System.getProperty(DATABASE_DDL_DEBUG_KEY, DEFAULT_DATABASE_DDL_DEBUG);
         if ("true".equalsIgnoreCase(ddlDebugProp)) {
             ddlDebug = true;
         }
         if (ddlCreate) {
             runDdl(conn, CREATE_STRONGLY_TYPED_REF_CURSOR_TABLE, ddlDebug);
             try {
                 Statement stmt = conn.createStatement();
                 for (int i = 0; i < POPULATE_STRONGLY_TYPED_REF_CURSOR_TABLE.length; i++) {
                     stmt.addBatch(POPULATE_STRONGLY_TYPED_REF_CURSOR_TABLE[i]);
                 }
                 stmt.executeBatch();
             }
             catch (SQLException e) {
                 //e.printStackTrace();
             }
             runDdl(conn, CREATE_STRONGLY_TYPED_REF_CURSOR_TEST_PACKAGE, ddlDebug);
             runDdl(conn, CREATE_STRONGLY_TYPED_REF_CURSOR_TEST_PACKAGE_BODY, ddlDebug);
         }
         boolean worked = true;
         String msg = null;
         try {
             String schema = System.getProperty(DATABASE_USERNAME_KEY, DEFAULT_DATABASE_USERNAME);
             packageType = dtBuilder.buildPackages(conn, schema, STRONGLY_TYPED_REF_CURSOR_TEST_PACKAGE).get(0);
         }
         catch (Exception e) {
             worked = false;
             msg = e.getMessage();
         }
         if (!worked) {
             fail(msg);
         }
     }
 
     @AfterClass
     public static void tearDown() {
         if (ddlDrop) {
             runDdl(conn, DROP_STRONGLY_TYPED_REF_CURSOR_TEST_PACKAGE, ddlDebug);
             runDdl(conn, DROP_STRONGLY_TYPED_REF_CURSOR_TABLE, ddlDebug);
         }
     }
 
     @Test
     public void testPackageName() {
         assertEquals("incorrect package name", STRONGLY_TYPED_REF_CURSOR_TEST_PACKAGE,
             packageType.getPackageName());
     }
 
     @Test
     public void testProcedureName() {
         List<ProcedureType> procedures = packageType.getProcedures();
         assertTrue(procedures.size() == 1);
         procType = procedures.get(0);
         assertEquals("incorrect procedure name", STRONGLY_TYPED_REF_CURSOR_PROC,
             procType.getProcedureName());
     }
 
     @Test
     public void testProcedureArgs() {
         List<ArgumentType> args = procType.getArguments();
         assertTrue(args.size() == 2);
         ArgumentType arg1 = args.get(0);
         assertEquals("incorrect arg1 name", "P_EMS", arg1.getArgumentName());
         ArgumentType arg2 = args.get(1);
         assertEquals("incorrect arg2 name", "P_EMS_SET", arg2.getArgumentName());
     }
 
     @Test
     public void testProcedureArgNames() {
         List<ArgumentType> args = procType.getArguments();
         assertTrue(args.size() == 2);
         ArgumentType arg1 = args.get(0);
         assertEquals("incorrect arg1 name", "P_EMS", arg1.getArgumentName());
         outCursorArg = args.get(1);
         assertEquals("incorrect outCursorArg name", "P_EMS_SET", outCursorArg.getArgumentName());
     }
 
     @Test
     public void testOutCursorArg() {
         DatabaseType enclosedType = outCursorArg.getEnclosedType();
         assertTrue(enclosedType.isPLSQLCursorType());
         PLSQLCursorType cursorType = (PLSQLCursorType)enclosedType;
         assertEquals("incorrect cursor name", STRONGLY_TYPED_REF_CURSOR, cursorType.getCursorName());
         DatabaseType enclosedType2 = cursorType.getEnclosedType();
         assertNotNull(enclosedType2);
         assertTrue(enclosedType2.isROWTYPEType());
         ROWTYPEType rowType = (ROWTYPEType)enclosedType2;
        assertEquals("incorrect cursor %ROWTYPE name", STRONGLY_TYPED_REF_CURSOR_TABLE + "%ROWTYPE", rowType.getTypeName());
         DatabaseType enclosedType3 = rowType.getEnclosedType();
         assertTrue(enclosedType3.isTableType());
         TableType tableType = (TableType)enclosedType3;
         assertEquals("incorrect %ROWTYPE table name", "STRC_TABLE", tableType.getTableName());
     }
 
 }
