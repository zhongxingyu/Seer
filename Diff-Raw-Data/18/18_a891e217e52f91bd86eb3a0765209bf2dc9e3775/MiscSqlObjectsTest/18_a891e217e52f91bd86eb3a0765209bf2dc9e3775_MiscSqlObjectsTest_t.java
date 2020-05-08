 /*
  * Copyright (c) 2000-2003 Netspective Communications LLC. All rights reserved.
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
  *    used to endorse products derived from The Software without without written consent of Netspective. "Netspective",
  *    "Axiom", "Commons", "Junxion", and "Sparx" may not appear in the names of products derived from The Software
  *    without written consent of Netspective.
  *
  * 5. Please attribute functionality where possible. We suggest using the "powered by Netspective" button or creating
  *    a "powered by Netspective(tm)" link to http://www.netspective.com for each application using The Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS" WITHOUT A WARRANTY OF ANY KIND. ALL EXPRESS OR IMPLIED REPRESENTATIONS AND
  * WARRANTIES, INCLUDING ANY IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR NON-INFRINGEMENT,
  * ARE HEREBY DISCLAIMED.
  *
  * NETSPECTIVE AND ITS LICENSORS SHALL NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE OR ANY THIRD PARTY AS A
  * RESULT OF USING OR DISTRIBUTING THE SOFTWARE. IN NO EVENT WILL NETSPECTIVE OR ITS LICENSORS BE LIABLE FOR ANY LOST
  * REVENUE, PROFIT OR DATA, OR FOR DIRECT, INDIRECT, SPECIAL, CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER
  * CAUSED AND REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF THE USE OF OR INABILITY TO USE THE SOFTWARE, EVEN
  * IF HE HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
  *
  * @author Shahid N. Shah
  */
 
 /**
 * $Id: MiscSqlObjectsTest.java,v 1.7 2003-11-08 18:29:29 shahid.shah Exp $
  */
 
 package com.netspective.axiom.sql;
 
 import java.sql.SQLException;
 import java.sql.Types;
 import java.sql.ResultSet;
 import java.util.*;
 
 import javax.naming.NamingException;
 
 import junit.framework.TestCase;
 
 import com.netspective.commons.xdm.XdmComponentFactory;
 import com.netspective.commons.xdm.exception.DataModelException;
 import com.netspective.commons.io.Resource;
 import com.netspective.commons.text.TextUtils;
 import com.netspective.commons.value.ValueSources;
 import com.netspective.axiom.sql.Query;
 import com.netspective.axiom.sql.dynamic.SqlComparisonFactory;
 import com.netspective.axiom.sql.dynamic.SqlComparison;
 import com.netspective.axiom.sql.dynamic.comparison.*;
 import com.netspective.axiom.sql.collection.QueriesCollection;
 import com.netspective.axiom.sql.collection.QueriesPackage;
 import com.netspective.axiom.*;
 import com.netspective.axiom.policy.OracleDatabasePolicy;
 import com.netspective.axiom.policy.PostgreSqlDatabasePolicy;
 import com.netspective.axiom.value.DatabaseConnValueContext;
 import com.netspective.axiom.value.BasicDatabaseConnValueContext;
 import com.netspective.axiom.value.BasicDatabasePolicyValueContext;
 
 public class MiscSqlObjectsTest extends TestCase
 {
     public static final String RESOURCE_NAME = "SqlManagerQueryTest.xml";
 	protected SqlManagerComponent component = null;
 	protected SqlManager manager = null;
     protected String[] queryNames = new String[] { "statement-0", "statement-1", "bad-statement", "statement-2" };
    protected String[] fqQueryNames = new String[]{"test.statement-0", "test.statement-1", "test.bad-statement", "test.statement-2"};
 
 	protected void setUp () throws Exception
     {
 		super.setUp();
 
 		component =
 		        (SqlManagerComponent) XdmComponentFactory.get(SqlManagerComponent.class, new Resource(MiscSqlObjectsTest.class, RESOURCE_NAME), XdmComponentFactory.XDMCOMPFLAGS_DEFAULT);
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
 
 		String[] expectedColBParamValues = new String[] { "abc", "def", "ghi", "jkl" };
 		String[] actualColBParamValues = columnB.getValue().getTextValues(null);
 		for (int i = 0; i < expectedColBParamValues.length; i++)
 			assertEquals(expectedColBParamValues[i], actualColBParamValues[i]);
 
         DatabaseConnValueContext dbvc = new BasicDatabaseConnValueContext();
         dbvc.setConnectionProvider(TestUtils.getConnProvider(this.getClass().getPackage().getName()));
         ConnectionContext cc = dbvc.getConnection(this.getClass().getPackage().getName(), true);
         assertNotNull(cc);
 
         QueryParameters.ValueRetrieveContext vrc = params.retrieve(cc);
         Object[] bindValue = vrc.getBindValues();
         Integer[] bindType = vrc.getBindTypes();
 
         assertEquals(bindValue.length, bindType.length);
         for (int i = 0; i < bindValue.length; i ++)
         {
             if (0 == i)
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
 
     public void testDatabasePolicies()
     {
         DatabasePolicies dbPolicies = new DatabasePolicies();
         assertEquals(6, dbPolicies.size());
 
         DatabasePolicy[] selectedDbPolicies = dbPolicies.getMatchingPolices("/o/");
         Set selectedDbPolicyNames = new HashSet(3);
         selectedDbPolicyNames.add("oracle");
         selectedDbPolicyNames.add("postgres");
         selectedDbPolicyNames.add("mssql");     // Microsoft Sql Server (from dbPolicy.getIdentifiers()
         assertEquals(selectedDbPolicyNames.size(), selectedDbPolicies.length);
         for (int i = 0; i < selectedDbPolicies.length; i ++)
             assertTrue(selectedDbPolicyNames.contains(selectedDbPolicies[i].getDbmsIdentifier()));
 
         Set policySet = dbPolicies.getPolicies();
         Set expectedPolicyNames = new HashSet(dbPolicies.size());
         expectedPolicyNames.add("ansi");
         expectedPolicyNames.add("postgres");
         expectedPolicyNames.add("mysql");
         expectedPolicyNames.add("hsqldb");
         expectedPolicyNames.add("oracle");
         expectedPolicyNames.add("mssql");
         assertEquals(expectedPolicyNames.size(), policySet.size());
         for (Iterator iter = policySet.iterator(); iter.hasNext();)
             assertTrue(expectedPolicyNames.contains(((DatabasePolicy) iter.next()).getDbmsIdentifier()));
     }
 
     public void testSqlComparisons()
     {
         String[] comparisonIds = SqlComparisonFactory.getComparisonIdentifiers();
 
         Map comparisonTypeMap = new HashMap();
         comparisonTypeMap.put("equals", new BinaryOpComparison("equals", "equals", "general", "="));
         comparisonTypeMap.put("not-equals", new BinaryOpComparison("not-equals", "does not equal", "general", "!="));
         comparisonTypeMap.put("starts-with", new StartsWithComparison());
         comparisonTypeMap.put("contains", new ContainsComparison());
         comparisonTypeMap.put("contains-ignore-case", new ContainsComparisonIgnoreCase());
         comparisonTypeMap.put("ends-with", new EndsWithComparison());
         comparisonTypeMap.put("in", new InComparison());
         comparisonTypeMap.put("is-defined", new IsDefinedComparison());
         comparisonTypeMap.put("greater-than", new BinaryOpComparison("greater-than", "greater than", "general", ">"));
         comparisonTypeMap.put("greater-than-equal", new BinaryOpComparison("greater-than-equal", "greater than or equal to", "general", ">="));
         comparisonTypeMap.put("less-than", new BinaryOpComparison("less-than", "less than", "general", "<"));
         comparisonTypeMap.put("less-than-equal", new BinaryOpComparison("less-than-equal", "less than or equal to", "general", "<="));
         comparisonTypeMap.put("lte-date", new DateComparison("lte-date", "<="));
         comparisonTypeMap.put("lt-date", new DateComparison("lt-date", "<"));
         comparisonTypeMap.put("gte-date", new DateComparison("gte-date", ">="));
         comparisonTypeMap.put("gt-date", new DateComparison("gt-date", ">"));
 
         for (int i = 0; i < comparisonIds.length; i ++)
             assertNotNull(SqlComparisonFactory.getComparison(comparisonIds[i]));
 
         for (Iterator iter = comparisonTypeMap.keySet().iterator(); iter.hasNext();)
         {
             String compId = (String) iter.next();
             BinaryOpComparison expectedComparison = (BinaryOpComparison) comparisonTypeMap.get(compId);
             SqlComparison comparison = SqlComparisonFactory.getComparison(compId);
             assertNotNull(comparison);
             assertEquals("Expected: " + compId + ", Actual: " + comparison.getName(), compId, comparison.getName());
             assertEquals("Expected: " + compId + ", Actual: " + expectedComparison.getName(), compId, expectedComparison.getName());
             assertEquals(expectedComparison.getName(), comparison.getName());
         }
     }
 
 }
