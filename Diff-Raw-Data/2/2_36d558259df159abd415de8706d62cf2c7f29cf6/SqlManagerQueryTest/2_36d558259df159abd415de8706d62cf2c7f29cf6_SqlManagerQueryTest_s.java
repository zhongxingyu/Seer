 /*
  * Copyright (c) 2000-2004 Netspective Communications LLC. All rights reserved.
  *
  * Netspective Communications LLC ("Netspective") permits redistribution, modification and use of this file in source
  * and binary form ("The Software") under the Netspective Source License ("NSL" or "The License"). The following
  * conditions are provided as a summary of the NSL but the NSL remains the canonical license and must be accepted
  * before using The Software. Any use of The Software indicates agreement with the NSL.
  *
  * 1. Each copy or derived work of The Software must preserve the copyright notice and this notice unmodified.
  *
  * 2. Redistribution of The Software is allowed in object code form only (as Java .class files or a .jar file
  *    containing the .class files) and only as part of an application that uses The Software as part of its primary
  *    functionality. No distribution of the package is allowed as part of a software development kit, other library,
  *    or development tool without written consent of Netspective. Any modified form of The Software is bound by these
  *    same restrictions.
  *
  * 3. Redistributions of The Software in any form must include an unmodified copy of The License, normally in a plain
  *    ASCII text file unless otherwise agreed to, in writing, by Netspective.
  *
  * 4. The names "Netspective", "Axiom", "Commons", "Junxion", and "Sparx" are trademarks of Netspective and may not be
  *    used to endorse or appear in products derived from The Software without written consent of Netspective.
  *
  * THE SOFTWARE IS PROVIDED "AS IS" WITHOUT A WARRANTY OF ANY KIND. ALL EXPRESS OR IMPLIED REPRESENTATIONS AND
  * WARRANTIES, INCLUDING ANY IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR NON-INFRINGEMENT,
  * ARE HEREBY DISCLAIMED.
  *
  * NETSPECTIVE AND ITS LICENSORS SHALL NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE OR ANY THIRD PARTY AS A
  * RESULT OF USING OR DISTRIBUTING THE SOFTWARE. IN NO EVENT WILL NETSPECTIVE OR ITS LICENSORS BE LIABLE FOR ANY LOST
  * REVENUE, PROFIT OR DATA, OR FOR DIRECT, INDIRECT, SPECIAL, CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER
  * CAUSED AND REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF THE USE OF OR INABILITY TO USE THE SOFTWARE, EVEN
  * IF IT HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
  */
 package com.netspective.axiom.sql;
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Types;
 import java.util.Map;
 import java.util.Set;
 
 import javax.naming.NamingException;
 
 import com.netspective.axiom.ConnectionContext;
 import com.netspective.axiom.DatabasePolicies;
 import com.netspective.axiom.DatabasePolicy;
 import com.netspective.axiom.SqlManager;
 import com.netspective.axiom.SqlManagerComponent;
 import com.netspective.axiom.TestUtils;
 import com.netspective.axiom.policy.OracleDatabasePolicy;
 import com.netspective.axiom.policy.PostgreSqlDatabasePolicy;
 import com.netspective.axiom.sql.collection.QueriesCollection;
 import com.netspective.axiom.sql.collection.QueriesPackage;
 import com.netspective.axiom.value.BasicDatabaseConnValueContext;
 import com.netspective.axiom.value.BasicDatabasePolicyValueContext;
 import com.netspective.axiom.value.DatabaseConnValueContext;
 import com.netspective.commons.io.Resource;
 import com.netspective.commons.text.TextUtils;
 import com.netspective.commons.value.ValueSources;
 import com.netspective.commons.xdm.XdmComponentFactory;
 import com.netspective.commons.xdm.exception.DataModelException;
 
 import junit.framework.TestCase;
 
 public class SqlManagerQueryTest extends TestCase
 {
     public static final String RESOURCE_NAME = "SqlManagerQueryTest.xml";
     protected SqlManagerComponent component = null;
     protected SqlManager manager = null;
     protected String[] queryNames = new String[]{"statement-0", "statement-1", "bad-statement", "statement-2"};
     protected String[] fqQueryNames = new String[]{
         "test.statement-0", "test.statement-1", "test.bad-statement", "test.statement-2"
     };
     protected TextUtils textUtils = TextUtils.getInstance();
 
     protected void setUp() throws Exception
     {
 
         super.setUp();
 
         component =
         (SqlManagerComponent) XdmComponentFactory.get(SqlManagerComponent.class, new Resource(SqlManagerQueryTest.class, RESOURCE_NAME), XdmComponentFactory.XDMCOMPFLAGS_DEFAULT);
         assertNotNull(component);
 
         component.printErrorsAndWarnings();
         assertEquals(0, component.getErrors().size());
 
         manager = component.getManager();
         assertEquals(this.queryNames.length, manager.getQueries().size());
     }
 
 
     public void testDatabaseValueContexts()
     {
         BasicDatabaseConnValueContext dbvc = new BasicDatabaseConnValueContext();
         assertNull(dbvc.getAccessControlListsManager());
         assertNull(dbvc.getConfigurationsManager());
         assertNull(dbvc.getSqlManager());
         assertEquals(this.getClass().getPackage().getName(), dbvc.translateDataSourceId(this.getClass().getPackage().getName()));
 
         DatabasePolicy dbPolicy = new PostgreSqlDatabasePolicy();
         BasicDatabasePolicyValueContext dbpvc = new BasicDatabasePolicyValueContext(dbPolicy);
         assertEquals(dbPolicy, dbpvc.getDatabasePolicy());
         assertNull(dbpvc.getSqlManager());
 
         dbpvc = new BasicDatabasePolicyValueContext("postgres");
         assertEquals(dbPolicy.getDbmsIdentifier(), dbpvc.getDatabasePolicy().getDbmsIdentifier());
         assertNull(dbpvc.getSqlManager());
     }
 
     public void testQueriesObject()
     {
         QueriesCollection queries = (QueriesCollection) manager.getQueries();
 
         assertSame(manager, queries.getSqlManager());
 
         Set queryNames = queries.getNames();
         assertEquals(this.queryNames.length, queryNames.size());
 
         for(int i = 0; i < queries.size(); i++)
         {
             Query q = queries.get(i);
             assertEquals(this.queryNames[i], q.getName());
             assertEquals(this.fqQueryNames[i], q.getQualifiedName());
             assertTrue(queryNames.contains(this.fqQueryNames[i].toUpperCase()));
         }
 
         String[] expectedNsNames = new String[]{"test"};
         Set nsNames = queries.getNameSpaceNames();
         assertEquals(expectedNsNames.length, nsNames.size());
 
         for(int i = 0; i < expectedNsNames.length; i++)
             assertTrue(nsNames.contains(expectedNsNames[i]));
     }
 
     public void testQueryNameSpaceObjects()
     {
         Query testStatement = manager.getQuery("test.statement-0");
         assertNotNull(testStatement);
         assertNull(testStatement.getParams());
 
         QueriesPackage qPackage = (QueriesPackage) testStatement.getNameSpace();
         Queries queries = qPackage.getContainer();
 
         assertSame(manager.getQueries(), queries);
     }
 
     public void testQueryParameters() throws NamingException, SQLException
     {
         Query testStmt = manager.getQuery("test.statement-1");
         assertNotNull(testStmt);
         assertNotNull(testStmt.getParams());
         assertEquals(2, testStmt.getParams().size());
 
         QueryParameters params = testStmt.getParams();
         assertEquals(2, params.size());
         assertEquals("test.statement-1", params.getQuery().getQualifiedName());
 
         QueryParameter columnA = (QueryParameter) params.get(0);
         QueryParameter columnB = (QueryParameter) params.get(1);
 
         // Test QueryParameter(s)
         assertEquals("column_a", columnA.getName());
         assertEquals("abc", columnA.getValue().getTextValue(null));
         assertFalse(columnA.isListType());
         assertTrue(columnA.isScalarType());
         assertEquals(1, columnA.getIndex());
         assertEquals(Types.VARCHAR, columnA.getSqlTypeCode());
         assertEquals(String.class, columnA.getJavaType());
         assertEquals(params, columnA.getParent());
 
         assertEquals("column_b", columnB.getName());
         assertEquals("abc", columnB.getValue().getTextValue(null));
         assertEquals(2, columnB.getIndex());
         assertEquals(Types.ARRAY, columnB.getSqlTypeCode());
         assertEquals(String.class, columnB.getJavaType());
         assertEquals(params, columnB.getParent());
 
         String[] expectedColBParamValues = new String[]{"abc", "def", "ghi", "jkl"};
         String[] actualColBParamValues = columnB.getValue().getTextValues(null);
         for(int i = 0; i < expectedColBParamValues.length; i++)
             assertEquals(expectedColBParamValues[i], actualColBParamValues[i]);
 
         DatabaseConnValueContext dbvc = new BasicDatabaseConnValueContext();
         dbvc.setConnectionProvider(TestUtils.getConnProvider(this.getClass().getPackage().getName()));
         ConnectionContext cc = dbvc.getConnection(this.getClass().getPackage().getName(), true);
         assertNotNull(cc);
 
         QueryParameters.ValueRetrieveContext vrc = params.retrieve(cc);
         Object[] bindValue = vrc.getBindValues();
         Integer[] bindType = vrc.getBindTypes();
 
         assertEquals(bindValue.length, bindType.length);
         for(int i = 0; i < bindValue.length; i++)
         {
             if(0 == i)
                 assertEquals("abc", bindValue[i]);
             else
                 assertEquals(expectedColBParamValues[i - 1], bindValue[i]);
 
             assertEquals(Types.VARCHAR, bindType[i].intValue());
         }
         cc.close();
     }
 
     public void testDbmsTexts()
     {
         Query testStatement = manager.getQuery("test.statement-0");
         assertNotNull(testStatement);
         assertNull(testStatement.getParams());
 
         DbmsSqlTexts dbmsSqlTextsOne = ((DbmsSqlTexts) testStatement.getSqlTexts()).getCopy();
         DbmsSqlTexts dbmsSqlTextsTwo = dbmsSqlTextsOne.getCopy();
         assertEquals(dbmsSqlTextsOne.size(), dbmsSqlTextsTwo.size());
         assertEquals(dbmsSqlTextsOne.getAvailableDbmsIds(), dbmsSqlTextsTwo.getAvailableDbmsIds());
 
         // Remove the oracle DbmsSqlText and add an ansi DbmsSqlText...
         dbmsSqlTextsTwo.removeByDbms("oracle");
         DbmsSqlText ansiSqlText = dbmsSqlTextsTwo.create();
 
         DatabasePolicies.DatabasePolicyEnumeratedAttribute dpea = DatabasePolicies.getInstance().getEnumeratedAttribute();
         dpea.setValue(DatabasePolicies.DBMSID_DEFAULT);
         ansiSqlText.setDbms(dpea);
         ansiSqlText.setSql("select *");
         ansiSqlText.addText(" from test");
 
         // Ensure the previous ansi sqlText was empty...
         assertEquals("", dbmsSqlTextsTwo.getByDbmsId(DatabasePolicies.DBMSID_DEFAULT).getSql(null).trim());
 
         // Now add the new one ...
         dbmsSqlTextsTwo.add(ansiSqlText);
         String expectedAnsiSql = "select * from test";
 
         // and test it..
         assertEquals(expectedAnsiSql, dbmsSqlTextsTwo.getByDbmsId(DatabasePolicies.DBMSID_DEFAULT).getSql().trim());
 
         // Ensure that dbmsSqlTextOne's previous "ansi" sqlText is empty...
         assertEquals("", dbmsSqlTextsOne.getByDbmsId(DatabasePolicies.DBMSID_DEFAULT).getSql(null).trim());
 
         // Now merge Two with One...
         dbmsSqlTextsOne.merge(dbmsSqlTextsTwo);
 
         // and test it..
         assertEquals(expectedAnsiSql, dbmsSqlTextsOne.getByDbmsId(DatabasePolicies.DBMSID_DEFAULT).getSql().trim());
         assertEquals(expectedAnsiSql, dbmsSqlTextsOne.getByDbmsOrAnsi(DatabasePolicies.getInstance().getDatabasePolicy("postgres")).getSql().trim());
     }
 
     public void testStmt0Validity()
     {
         Query testStatement = manager.getQuery("test.statement-0");
         assertNotNull(testStatement);
         assertNull(testStatement.getParams());
 
         DbmsSqlTexts dbmsSqlTexts = testStatement.getSqlTexts();
         String[] expectedDbmsIds = new String[]{"ansi", "oracle"};
         Set availableDbmsIds = dbmsSqlTexts.getAvailableDbmsIds();
 
         for(int i = 0; i < expectedDbmsIds.length; i++)
             assertTrue(availableDbmsIds.contains(expectedDbmsIds[i]));
 
         assertEquals("", dbmsSqlTexts.getByDbmsId(DatabasePolicies.DBMSID_DEFAULT).getSql().trim());
 
         String sqlStatement = dbmsSqlTexts.getByDbmsId("oracle").getSql().trim();
         sqlStatement = textUtils.join(textUtils.split(sqlStatement, " \t\n", true), " ", true);
         assertEquals("select * from test where column_a = 1 and column_b = 2 and column_c = 'this'", sqlStatement);
         assertEquals(sqlStatement, textUtils.join(textUtils.split(dbmsSqlTexts.getByDbmsOrAnsi(new OracleDatabasePolicy()).getSql().trim(), " \t\n", true), " ", true));
     }
 
     public void testStmt1Validity()
     {
         Query testStatement1 = manager.getQuery("test.statement-1");
         assertNotNull(testStatement1);
         assertNotNull(testStatement1.getParams());
         assertEquals(2, testStatement1.getParams().size());
 
         DbmsSqlTexts dbmsSqlTextsTwo = testStatement1.getSqlTexts();
         assertNull(dbmsSqlTextsTwo.getByDbmsId("oracle"));
         assertNotNull(dbmsSqlTextsTwo.getByDbmsId(DatabasePolicies.DBMSID_DEFAULT));
 
         String sqlStatement1 = dbmsSqlTextsTwo.getByDbmsId(DatabasePolicies.DBMSID_DEFAULT).getSql();
         String[] sqlWords1 = textUtils.split(sqlStatement1, " \t\n", true);
         sqlStatement1 = textUtils.join(sqlWords1, " ", true);
         assertEquals("select * from test where column_a = ? and column_b in (${param-list:1}) and column_c = 'this'", sqlStatement1);
 
         QueryParameters testParams1 = testStatement1.getParams();
         QueryParameter stmt1ColumnAParam = (QueryParameter) testParams1.get(0);
         QueryParameter stmt1ColumnBParam = (QueryParameter) testParams1.get(1);
 
         // Test QueryParameter(s)
         assertEquals("column_a", stmt1ColumnAParam.getName());
         assertEquals("abc", stmt1ColumnAParam.getValue().getTextValue(null));
         assertFalse(stmt1ColumnAParam.isListType());
         assertTrue(stmt1ColumnAParam.isScalarType());
         assertEquals(1, stmt1ColumnAParam.getIndex());
         assertEquals(Types.VARCHAR, stmt1ColumnAParam.getSqlTypeCode());
         assertEquals(String.class, stmt1ColumnAParam.getJavaType());
 
 
         assertEquals("column_b", stmt1ColumnBParam.getName());
         assertEquals("abc", stmt1ColumnBParam.getValue().getTextValue(null));
         assertEquals(2, stmt1ColumnBParam.getIndex());
         assertEquals(Types.ARRAY, stmt1ColumnBParam.getSqlTypeCode());
         assertEquals(String.class, stmt1ColumnBParam.getJavaType());
 
         String[] expectedColBParamValues = new String[]{"abc", "def", "ghi", "jkl"};
         String[] actualColBParamValues = stmt1ColumnBParam.getValue().getTextValues(null);
         for(int i = 0; i < expectedColBParamValues.length; i++)
             assertEquals(expectedColBParamValues[i], actualColBParamValues[i]);
     }
 
     public void testStmt1ExecutionWithoutLogging() throws NamingException, SQLException
     {
         Query stmtOne = manager.getQuery("test.statement-1");
         assertNotNull(stmtOne);
         assertNotNull(stmtOne.getParams());
         assertEquals(2, stmtOne.getParams().size());
 
         stmtOne.setDataSrc(null);
 
         DatabaseConnValueContext dbvc = new BasicDatabaseConnValueContext();
         dbvc.setConnectionProvider(TestUtils.getConnProvider(this.getClass().getPackage().getName()));
         ConnectionContext cc = dbvc.getConnection(this.getClass().getPackage().getName(), true);
 
         // No stats recorded...
         QueryResultSet qrsOne = stmtOne.executeAndIgnoreStatistics(cc, null, true);
         QueryExecutionLogEntry qeleOne = qrsOne.getExecutionLogEntry();
         assertTrue(qrsOne.getExecutStmtResult());
         assertEquals("test.statement-1", qrsOne.getQuery().getQualifiedName());
         assertNull(qeleOne);
         assertNotNull(qrsOne.getResultSet());
         qrsOne.close(false);
         assertNull(qrsOne.getResultSet());
         assertSame(cc, qrsOne.getConnectionContext());
 
         // Still no stats recorded...
         assertEquals(DatabaseConnValueContext.DATASRCID_DEFAULT_DATA_SOURCE, dbvc.getDefaultDataSource());
         dbvc.setDefaultDataSource(this.getClass().getPackage().getName());
         assertEquals(this.getClass().getPackage().getName(), dbvc.getDefaultDataSource());
 
        qrsOne = stmtOne.executeAndIgnoreStatistics(dbvc, null, true);
         qeleOne = qrsOne.getExecutionLogEntry();
         assertTrue(qrsOne.getExecutStmtResult());
         assertEquals("test.statement-1", qrsOne.getQuery().getQualifiedName());
         assertNull(qeleOne);
         assertNotNull(qrsOne.getResultSet());
         qrsOne.close(true);
 
         // Verify query results...
         qrsOne = stmtOne.executeAndIgnoreStatistics(cc, null, true);
         assertTrue(qrsOne.getExecutStmtResult());
         ResultSet rs = qrsOne.getResultSet();
 
         int numRows = 0;
         int expectedRows = 3;
         int expectedNumAbc = 2;
         int expectedNumGhi = 1;
         int numAbc = 0;
         int numGhi = 0;
         while(rs.next())
         {
             // column_a = #5 since table Test is of type Default => first three fields are cr_stamp, cr_person_id etc
             numRows++;
             assertEquals(numRows, rs.getRow());
             assertEquals("abc", rs.getString(4));
             assertEquals("this", rs.getString(6));
 
             if("ghi".equals(rs.getString(5))) numGhi++;
             if("abc".equals(rs.getString(5))) numAbc++;
         }
         assertEquals(expectedRows, numRows);
         assertEquals(expectedNumAbc, numAbc);
         assertEquals(expectedNumGhi, numGhi);
 
         // Wrap up by closing connections...
         rs.close();
         qrsOne.close(true);
         cc.close();
     }
 
     public void testStmt1ExecutionWithLogging() throws NamingException, SQLException
     {
         Query stmtOne = manager.getQuery("test.statement-1");
         assertNotNull(stmtOne);
         assertNotNull(stmtOne.getParams());
         assertEquals(2, stmtOne.getParams().size());
 
         stmtOne.setDataSrc(null);
 
         DatabaseConnValueContext dbvc = new BasicDatabaseConnValueContext();
         dbvc.setConnectionProvider(TestUtils.getConnProvider(this.getClass().getPackage().getName()));
         ConnectionContext cc = dbvc.getConnection(this.getClass().getPackage().getName(), true);
 
         // Stats recorded this time...
         QueryResultSet qrsOne = stmtOne.executeAndRecordStatistics(cc, null, true);
         QueryExecutionLogEntry qeleOne = qrsOne.getExecutionLogEntry();
         assertTrue(qrsOne.getExecutStmtResult());
         assertEquals("test.statement-1", qrsOne.getQuery().getQualifiedName());
         assertNotNull(qeleOne);
         assertNotNull(qrsOne.getResultSet());
         qrsOne.close(false);
         assertNull(qrsOne.getResultSet());
 
         // Verify QueryExecutionLogEntry...
         assertTrue(qeleOne.wasSuccessful());
         assertEquals("<no locator>", qeleOne.getSource());
         assertTrue(System.currentTimeMillis() >= qeleOne.getEntryDate().getTime());
         assertTrue(System.currentTimeMillis() >= qeleOne.getInitTime());
         assertTrue(0 <= qeleOne.getConnectionEstablishTime());
         assertTrue(0 <= qeleOne.getBindParamsBindTime());
         assertTrue(0 <= qeleOne.getSqlExecTime());
         assertTrue(0 <= qeleOne.getTotalExecutionTime());
 
         // More stats recorded...
         assertEquals(DatabaseConnValueContext.DATASRCID_DEFAULT_DATA_SOURCE, dbvc.getDefaultDataSource());
         dbvc.setDefaultDataSource(this.getClass().getPackage().getName());
         assertEquals(this.getClass().getPackage().getName(), dbvc.getDefaultDataSource());
 
         qrsOne = stmtOne.executeAndRecordStatistics(cc, null, true);
         qeleOne = qrsOne.getExecutionLogEntry();
         assertTrue(qrsOne.getExecutStmtResult());
         assertEquals("test.statement-1", qrsOne.getQuery().getQualifiedName());
         assertNotNull(qeleOne);
         assertNotNull(qrsOne.getResultSet());
         qrsOne.close(false);
         assertNull(qrsOne.getResultSet());
 
         // Verify query results...
         qrsOne = stmtOne.executeAndRecordStatistics(cc, null, true);
         assertTrue(qrsOne.getExecutStmtResult());
         ResultSet rs = qrsOne.getResultSet();
 
         int numRows = 0;
         int expectedRows = 3;
         int expectedNumAbc = 2;
         int expectedNumGhi = 1;
         int numAbc = 0;
         int numGhi = 0;
         while(rs.next())
         {
             // column_a = #5 since table Test is of type Default => first three fields are cr_stamp, cr_person_id etc
             numRows++;
             assertEquals(numRows, rs.getRow());
             assertEquals("abc", rs.getString(4));
             assertEquals("this", rs.getString(6));
 
             if("ghi".equals(rs.getString(5))) numGhi++;
             if("abc".equals(rs.getString(5))) numAbc++;
         }
         assertEquals(expectedRows, numRows);
         assertEquals(expectedNumAbc, numAbc);
         assertEquals(expectedNumGhi, numGhi);
         qrsOne.close(false);
 
         // More stats recorded...
         assertEquals(this.getClass().getPackage().getName(), dbvc.getDefaultDataSource());
         dbvc.setDefaultDataSource(DatabaseConnValueContext.DATASRCID_DEFAULT_DATA_SOURCE);
         assertEquals(DatabaseConnValueContext.DATASRCID_DEFAULT_DATA_SOURCE, dbvc.getDefaultDataSource());
 
         assertNull(stmtOne.getDataSrc());
         stmtOne.setDataSrc(ValueSources.getInstance().getValueSource("static:" + this.getClass().getPackage().getName(), ValueSources.VSNOTFOUNDHANDLER_NULL));
         assertEquals(this.getClass().getPackage().getName(), stmtOne.getDataSrc().getTextValue(dbvc));
 
         qrsOne = stmtOne.executeAndRecordStatistics(cc, null, true);
         qeleOne = qrsOne.getExecutionLogEntry();
         assertTrue(qrsOne.getExecutStmtResult());
         assertEquals("test.statement-1", qrsOne.getQuery().getQualifiedName());
         assertNotNull(qeleOne);
         assertNotNull(qrsOne.getResultSet());
         assertFalse(stmtOne.isSqlTextHasExpressions());
 
         // Verify query results...
         qrsOne = stmtOne.executeAndIgnoreStatistics(cc, null, true);
         assertTrue(qrsOne.getExecutStmtResult());
         rs = qrsOne.getResultSet();
 
         numRows = 0;
         expectedRows = 3;
         expectedNumAbc = 2;
         expectedNumGhi = 1;
         numAbc = 0;
         numGhi = 0;
         while(rs.next())
         {
             // column_a = #5 since table Test is of type Default => first three fields are cr_stamp, cr_person_id etc
             numRows++;
             assertEquals(numRows, rs.getRow());
             assertEquals("abc", rs.getString(4));
             assertEquals("this", rs.getString(6));
 
             if("ghi".equals(rs.getString(5))) numGhi++;
             if("abc".equals(rs.getString(5))) numAbc++;
         }
         assertEquals(expectedRows, numRows);
         assertEquals(expectedNumAbc, numAbc);
         assertEquals(expectedNumGhi, numGhi);
 
         qrsOne.close(true);
         cc.close();
     }
 
     public void testStmt1ExecutionFailureWithoutLogging() throws NamingException, SQLException
     {
         Query badStmt = manager.getQuery("test.bad-statement");
         assertNotNull(badStmt);
         assertNotNull(badStmt.getParams());
         assertEquals(2, badStmt.getParams().size());
 
         DatabaseConnValueContext dbvc = new BasicDatabaseConnValueContext();
         dbvc.setConnectionProvider(TestUtils.getConnProvider(this.getClass().getPackage().getName()));
         ConnectionContext cc = dbvc.getConnection(this.getClass().getPackage().getName(), true);
 
         // Stats recorded...
         boolean exceptionThrown = true;
 
         QueryResultSet qrsOne = null;
         try
         {
             qrsOne = badStmt.executeAndIgnoreStatistics(cc, null, true);
             exceptionThrown = false;
         }
         catch(Exception e)
         {
             assertTrue(exceptionThrown);
         }
         finally
         {
             cc.close();
         }
         assertTrue(exceptionThrown);
 
         assertNull(qrsOne);
     }
 
     public void testStmt1QueryExecutionLog() throws NamingException, SQLException
     {
         Query stmtOne = manager.getQuery("test.statement-1");
         assertNotNull(stmtOne);
         assertNotNull(stmtOne.getParams());
         assertEquals(2, stmtOne.getParams().size());
 
         stmtOne.setDataSrc(null);
 
         DatabaseConnValueContext dbvc = new BasicDatabaseConnValueContext();
         dbvc.setConnectionProvider(TestUtils.getConnProvider(this.getClass().getPackage().getName()));
         ConnectionContext cc = dbvc.getConnection(this.getClass().getPackage().getName(), true);
 
         // Stats recorded this time...
         QueryResultSet qrsOne = stmtOne.executeAndRecordStatistics(cc, null, true);
         QueryExecutionLogEntry qeleOne = qrsOne.getExecutionLogEntry();
         assertTrue(qrsOne.getExecutStmtResult());
         assertEquals("test.statement-1", qrsOne.getQuery().getQualifiedName());
         assertNotNull(qeleOne);
         assertNotNull(qrsOne.getResultSet());
         qrsOne.close(false);
         assertNull(qrsOne.getResultSet());
 
         // Verify QueryExecutionLogEntry...
         assertTrue(qeleOne.wasSuccessful());
         assertEquals("<no locator>", qeleOne.getSource());
         assertTrue(System.currentTimeMillis() >= qeleOne.getEntryDate().getTime());
         assertTrue(System.currentTimeMillis() >= qeleOne.getInitTime());
         assertTrue(0 <= qeleOne.getConnectionEstablishTime());
         assertTrue(0 <= qeleOne.getBindParamsBindTime());
         assertTrue(0 <= qeleOne.getSqlExecTime());
         assertTrue(0 <= qeleOne.getTotalExecutionTime());
 
         // More stats recorded...
         assertEquals(DatabaseConnValueContext.DATASRCID_DEFAULT_DATA_SOURCE, dbvc.getDefaultDataSource());
         dbvc.setDefaultDataSource(this.getClass().getPackage().getName());
         assertEquals(this.getClass().getPackage().getName(), dbvc.getDefaultDataSource());
 
         qrsOne = stmtOne.executeAndRecordStatistics(cc, null, true);
         qeleOne = qrsOne.getExecutionLogEntry();
         assertTrue(qrsOne.getExecutStmtResult());
         assertEquals("test.statement-1", qrsOne.getQuery().getQualifiedName());
         assertNotNull(qeleOne);
         assertNotNull(qrsOne.getResultSet());
         qrsOne.close(false);
         assertNull(qrsOne.getResultSet());
 
         // Verify query results...
         qrsOne = stmtOne.executeAndRecordStatistics(cc, null, true);
         assertTrue(qrsOne.getExecutStmtResult());
         ResultSet rs = qrsOne.getResultSet();
 
         int numRows = 0;
         int expectedRows = 3;
         int expectedNumAbc = 2;
         int expectedNumGhi = 1;
         int numAbc = 0;
         int numGhi = 0;
         while(rs.next())
         {
             // column_a = #5 since table Test is of type Default => first three fields are cr_stamp, cr_person_id etc
             numRows++;
             assertEquals(numRows, rs.getRow());
             assertEquals("abc", rs.getString(4));
             assertEquals("this", rs.getString(6));
 
             if("ghi".equals(rs.getString(5))) numGhi++;
             if("abc".equals(rs.getString(5))) numAbc++;
         }
         assertEquals(expectedRows, numRows);
         assertEquals(expectedNumAbc, numAbc);
         assertEquals(expectedNumGhi, numGhi);
         qrsOne.close(false);
 
         // More stats recorded...
         //TODO: See if this comparison is really necesary:
         //assertEquals(this.getClass().getPackage().getName(), dbvc.getDefaultDataSource());
         dbvc.setDefaultDataSource(DatabaseConnValueContext.DATASRCID_DEFAULT_DATA_SOURCE);
         assertEquals(DatabaseConnValueContext.DATASRCID_DEFAULT_DATA_SOURCE, dbvc.getDefaultDataSource());
 
         //System.out.println("\nDataSrc: " + stmtOne.getDataSrc());
         assertNull(stmtOne.getDataSrc());
         stmtOne.setDataSrc(ValueSources.getInstance().getValueSource("static:" + this.getClass().getPackage().getName(), ValueSources.VSNOTFOUNDHANDLER_NULL));
         assertEquals(this.getClass().getPackage().getName(), stmtOne.getDataSrc().getTextValue(dbvc));
 
         qrsOne = stmtOne.executeAndRecordStatistics(cc, null, true);
         qeleOne = qrsOne.getExecutionLogEntry();
         assertTrue(qrsOne.getExecutStmtResult());
         assertEquals("test.statement-1", qrsOne.getQuery().getQualifiedName());
         assertNotNull(qeleOne);
         assertNotNull(qrsOne.getResultSet());
         assertFalse(stmtOne.isSqlTextHasExpressions());
 
         // Verify query results...
         qrsOne = stmtOne.executeAndIgnoreStatistics(cc, null, true);
         assertTrue(qrsOne.getExecutStmtResult());
         rs = qrsOne.getResultSet();
 
         numRows = 0;
         expectedRows = 3;
         expectedNumAbc = 2;
         expectedNumGhi = 1;
         numAbc = 0;
         numGhi = 0;
         while(rs.next())
         {
             // column_a = #5 since table Test is of type Default => first three fields are cr_stamp, cr_person_id etc
             numRows++;
             assertEquals(numRows, rs.getRow());
             assertEquals("abc", rs.getString(4));
             assertEquals("this", rs.getString(6));
 
             if("ghi".equals(rs.getString(5))) numGhi++;
             if("abc".equals(rs.getString(5))) numAbc++;
         }
         assertEquals(expectedRows, numRows);
         assertEquals(expectedNumAbc, numAbc);
         assertEquals(expectedNumGhi, numGhi);
 
         // Test QueryExecutionLog class...
         QueryExecutionLog qelOne = stmtOne.getExecLog();
         assertEquals(-1, qelOne.getResetLogAfterCount());
         qelOne.setResetLogAfterCount(10);
         assertEquals(10, qelOne.getResetLogAfterCount());
         qelOne.setResetLogAfterCount(-1);
         assertEquals(-1, qelOne.getResetLogAfterCount());
 
         QueryExecutionLog.QueryExecutionStatistics qesOne = qelOne.getStatistics();
         assertNotNull(qesOne);
         assertTrue(3 <= qesOne.totalExecutions);
         assertTrue(8 >= qesOne.totalExecutions);
         assertEquals(0, qesOne.totalFailed);
         assertTrue(0 <= qesOne.averageBindParamsTime);
         assertTrue(0 <= qesOne.averageConnectionEstablishTime);
         assertTrue(0 <= qesOne.averageSqlExecTime);
         assertTrue(0 <= qesOne.averageTotalExecTime);
 
         qrsOne.close(true);
         cc.close();
     }
 
     public void testResultSetUtilities() throws NamingException, SQLException
     {
         Query stmtOne = manager.getQuery("test.statement-1");
         assertNotNull(stmtOne);
         assertNotNull(stmtOne.getParams());
         assertEquals(2, stmtOne.getParams().size());
 
         stmtOne.setDataSrc(null);
 
         DatabaseConnValueContext dbvc = new BasicDatabaseConnValueContext();
         dbvc.setConnectionProvider(TestUtils.getConnProvider(this.getClass().getPackage().getName()));
         ConnectionContext cc = dbvc.getConnection(this.getClass().getPackage().getName(), true);
 
         // No stats recorded...
         QueryResultSet qrsOne = stmtOne.executeAndIgnoreStatistics(cc, null, true);
         QueryExecutionLogEntry qeleOne = qrsOne.getExecutionLogEntry();
         assertTrue(qrsOne.getExecutStmtResult());
         assertEquals("test.statement-1", qrsOne.getQuery().getQualifiedName());
         assertNull(qeleOne);
         ResultSet rs = qrsOne.getResultSet();
         assertNotNull(rs);
         assertSame(cc, qrsOne.getConnectionContext());
 
         // Verify query results...
         //ResultSet rs = qrsOne.getResultSet();
 
         String[] expectedColumns = new String[]{
             "cr_stamp", "rec_stat_id", "system_id", "column_a", "column_b", "column_c"
         };
         Map columnNames = ResultSetUtils.getInstance().getColumnNamesIndexMap(rs);
 
         assertEquals(expectedColumns.length, columnNames.size());
         for(int i = 0; i < expectedColumns.length; i++)
             assertTrue(columnNames.containsKey(expectedColumns[i]));
 
         int numRows = 0;
         int expectedRows = 3;
         int expectedNumAbc = 2;
         int expectedNumGhi = 1;
         int numAbc = 0;
         int numGhi = 0;
         while(rs.next())
         {
             // column_a = #5 since table Test is of type Default => first three fields are cr_stamp, cr_person_id etc
             numRows++;
             assertEquals(numRows, rs.getRow());
             assertEquals("abc", rs.getString(4));
             assertEquals("this", rs.getString(6));
 
             if("ghi".equals(rs.getString(5))) numGhi++;
             if("abc".equals(rs.getString(5))) numAbc++;
         }
         assertEquals(expectedRows, numRows);
         assertEquals(expectedNumAbc, numAbc);
         assertEquals(expectedNumGhi, numGhi);
 
         // Wrap up by closing connections...
         qrsOne.close(true);
         rs.close();
         cc.close();
 
     }
 
     public void testStmt2Validity() throws DataModelException, NamingException, SQLException
     {
         Query statement2 = manager.getQuery("test.statement-2");
         assertNotNull(statement2);
         assertNotNull(statement2.getParams());
         assertEquals(2, statement2.getParams().size());
 
         DbmsSqlTexts sqlTexts = statement2.getSqlTexts();
         String sql = sqlTexts.getByDbmsId(DatabasePolicies.DBMSID_DEFAULT).getSql();
         String flatSql = textUtils.join(textUtils.split(sql, " \t\n", true), " ");
         assertEquals("select * from test where column_a = ? and column_b = ? and column_c = 'this'", flatSql);
     }
 
     /*
     public void testIdConstantsGenerator() throws DataModelException, InstantiationException, InvocationTargetException, NoSuchMethodException, IOException, IllegalAccessException, NamingException, SQLException
     {
         SqlManagerComponent component =
                 (SqlManagerComponent) XdmComponentFactory.get(SqlManagerComponent.class, new Resource(SqlManagerQueryTest.class, "test-data/statements.xml"), XdmComponentFactory.XDMCOMPFLAGS_DEFAULT);
         assertNotNull(component);
 
         File destDir = new File(((FileTracker) component.getInputSource()).getFile().getParent());
         component.generateIdentifiersConstants(destDir, "app.id");
     }
     */
 }
