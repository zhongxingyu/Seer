 package com.netspective.axiom.sql;
 
 import junit.framework.TestCase;
 import com.netspective.axiom.*;
 import com.netspective.axiom.policy.HSqlDbDatabasePolicy;
 import com.netspective.axiom.value.DatabaseConnValueContext;
 import com.netspective.axiom.value.BasicDatabaseConnValueContext;
 import com.netspective.axiom.sql.collection.QueryDefinitionsCollection;
 import com.netspective.axiom.sql.dynamic.*;
 import com.netspective.axiom.sql.dynamic.exception.QueryDefinitionException;
 import com.netspective.commons.xdm.XdmComponentFactory;
 import com.netspective.commons.xdm.XmlDataModelDtd;
 import com.netspective.commons.xdm.exception.DataModelException;
 import com.netspective.commons.io.Resource;
 import com.netspective.commons.io.FileFind;
 import com.netspective.commons.value.ValueSource;
 import com.netspective.commons.value.ValueSources;
 import com.netspective.commons.value.Value;
 import com.netspective.commons.text.TextUtils;
 
 import javax.naming.NamingException;
 import java.util.Set;
 import java.util.List;
 import java.util.ArrayList;
 import java.lang.reflect.InvocationTargetException;
 import java.io.IOException;
 import java.io.File;
 import java.sql.SQLException;
 
 /**
 * $Id: DynamicSqlTest.java,v 1.10 2004-02-06 03:05:36 shahid.shah Exp $
  */
 public class DynamicSqlTest extends TestCase
 {
     public static final String RESOURCE_NAME = "SqlManagerQueryTest.xml";
     protected SqlManagerComponent component = null;
     protected SqlManager manager = null;
     protected String[] queryDefnNames = new String[]{"query-defn-1", "query-defn-2"};
     protected String[] fqQueryDefnNames = new String[]{"query-defn-1", "query-defn-2"};
     static protected File dbFile = null;
     static protected String dbFilename = null;
 	static DatabasePolicy dbPolicy = new HSqlDbDatabasePolicy();
 
     static protected int[] numFields = new int[] {5, 5};
     static protected int[] numJoins = new int[] {5, 4};
     static protected int[] numSelects = new int[] {1, 1};
 	static protected int[] numDisplayFields = new int[] {3, 3};
 	static protected int[] numOrderBys = new int[] {1, 0};
 	static protected int[] numGroupBys = new int[] {1, 0};
 	static protected int[] numConditions = new int[] {9, 0};
 	static protected int[] numWhereExpr = new int[] {1, 0};
 
     static protected int fieldIndexWithBadJoin = 3;
     static protected int queryDefnIndexWithMalformedSyntax = 1;
 
     static
     {
 
         /* TODO: We'll take this out with time, for now it's just a reminder that we'll have to do some validation that the DB
                  was actualyl created.
         FileFind.FileFindResults ffr = FileFind.findInPath(new String[] { "../../Axiom" }, "medspective.data", FileFind.FINDINPATHFLAG_SEARCH_RECURSIVELY);
         assertTrue("The Medspective Demo Schema was not found!  Did you run 'build generate-dal generate-ddl create-database import-data' ?", ffr.isFileFound());
         dbFile = ffr.getFoundFile();
         try
         {
             dbFilename = dbFile.getParentFile().getCanonicalPath() + File.separator + "medspective";
         }
         catch (IOException e)
         {
             dbFilename = "Error occurred while getting canonical filename for the Medspective Demo Database";
         }*/
     }
 
     protected void setUp() throws Exception
     {
         super.setUp();
 
         component =
                 (SqlManagerComponent) XdmComponentFactory.get(SqlManagerComponent.class, new Resource(DynamicSqlTest.class, RESOURCE_NAME), XdmComponentFactory.XDMCOMPFLAGS_DEFAULT);
         assertNotNull(component);
 
         component.printErrorsAndWarnings();
         assertEquals(0, component.getErrors().size());
 
         manager = component.getManager();
         assertEquals(this.queryDefnNames.length, manager.getQueryDefns().size());
     }
 
     public void testQueryDefinitionsObject()
     {
         QueryDefinitionsCollection queryDefns = (QueryDefinitionsCollection) manager.getQueryDefns();
 
         Set queryDefnNames = queryDefns.getNames();
         assertEquals(this.queryDefnNames.length, queryDefnNames.size());
 
         for (int i = 0; i < this.queryDefnNames.length; i++) {
             assertEquals(this.queryDefnNames[i], queryDefns.get(i).getName());
 			assertNull(queryDefns.get(i).getContainer());
 			assertEquals(this.queryDefnNames[i], queryDefns.get(i).getNameSpaceId());
 		}
     }
 
     public void testQueryDefnValidity() throws QueryDefinitionException, DataModelException, InstantiationException, InvocationTargetException, NoSuchMethodException, IOException, IllegalAccessException, NamingException, SQLException
     {
         QueryDefinitionsCollection queryDefns = (QueryDefinitionsCollection) manager.getQueryDefns();
         assertNotNull(queryDefns);
         assertEquals(queryDefnNames.length, queryDefns.size());
 
         Set actualQueryDefnNames = queryDefns.getNames();
         for (int i = 0; i < queryDefnNames.length; i++)
         {
             assertNotNull(queryDefns.get(queryDefnNames[i]));
             assertTrue(actualQueryDefnNames.contains(queryDefnNames[i].toUpperCase()));
         }
 
         String[][] expectedFieldCaptionList = new String[][] {
             new String[]{null, "Test Field 02 Caption", "Test Field 03 Caption", "Test Field 04 Caption", "Test Field 05 Caption"},
             new String[]{null, "Test Field 02 Caption", "Test Field 03 Caption", "Test Field 04 Caption", "Test Field 05 Caption"}
         };
         String[][] expectedFieldColumnLabelList = new String[][] {
             new String[]{"field_01", "Test Field 02 Caption", "Test Field 03 Caption", "Test Field 04 Caption", "Test Field 05 Caption"},
            new String[]{"field_01", "Test Field 02 Caption", "Test Field 03 Caption", "Test Field 04 Caption", "Test Field 05 Caption"}
         };
         String[][] expectedFieldQualifiedColNameList = new String[][] {
             new String[]{"join_01.column_01", "join_02.column_02", "column_03", "column_04", "column_05"},
             new String[]{"join_01.column_01", "join_02.column_02", "column_03", "join_04.column_04", "column_05"}
         };
         String[][] expectedFieldTableNameList = new String[][] {
             new String[]{"join_01", "Table_02", null, null, null},
             new String[]{"join_01", "Table_02", null, null, null}
         };
         String[][] expectedFieldTableAliasList = new String[][] {
             new String[]{"join_01", "join_02", null, null, null},
             new String[]{"join_01", "join_02", null, null, null}
         };
         String[][] expectedSelectClauseExprAndLabelList = new String[][] {
             new String[]{"join_01.column_01 as \"field_01\"", "join_02.column_02a as \"Test Field 02 Caption\"", "column_03 as \"Test Field 03 Caption\"", "column_04 as \"Test Field 04 Caption\"", "column_05 as \"Test Field 05 Caption\""},
             new String[]{"join_01.column_01 as \"field_01\"", "join_02.column_02 as \"Test Field 02 Caption\"", "column_03 as \"Test Field 03 Caption\"", "column_04 as \"Test Field 04 Caption\"", "column_05 as \"Test Field 05 Caption\""}
         };
         String[][] expectedFieldColExprList = new String[][] {
             new String[]{"join_01.column_01", "join_02.column_02a", "column_03", "column_04", "column_05"},
             new String[]{"join_01.column_01", "join_02.column_02", "column_03", "column_04", "column_05"}
         };
         String[][] expectedFieldWhereExprList = new String[][] {
             new String[]{"join_01.column_01", "join_02.column_02", "column_03", "column_04", "column_05"},
             new String[]{"join_01.column_01", "join_02.column_02", "column_03", "column_04", "column_05"}
         };
         String[][] expectedFieldOrderByExprList = new String[][] {
             new String[]{"join_01.column_01", "join_02.column_02", "column_03", "column_04", "column_05"},
             new String[]{"join_01.column_01", "join_02.column_02", "column_03", "column_04", "column_05"}
         };
 
         for (int item = 0; item < queryDefns.size(); item ++) {
             QueryDefinition queryDefn = manager.getQueryDefinition(queryDefnNames[item]);
             assertNotNull(queryDefn);
             assertEquals(queryDefn, queryDefns.get(queryDefnNames[item]));
 
             // Verify that this query-defn has the right number of fingers and toes ;)
             assertEquals(numFields[item], queryDefn.getFields().size());
             assertEquals(numJoins[item], queryDefn.getJoins().size());
             assertEquals(numSelects[item], queryDefn.getSelects().size());
 
             // Verify Fields...
             QueryDefnFields fields = queryDefn.getFields();
             String[] expectedFieldCaption = expectedFieldCaptionList[item];
             String[] expectedFieldColumnLabel = expectedFieldColumnLabelList[item];
             String[] expectedFieldQualifiedColName = expectedFieldQualifiedColNameList[item];
             String[] expectedFieldTableName = expectedFieldTableNameList[item];
             String[] expectedFieldTableAlias = expectedFieldTableAliasList[item];
             String[] expectedSelectClauseExprAndLabel = expectedSelectClauseExprAndLabelList[item];
             String[] expectedFieldColExpr = expectedFieldColExprList[item];
             String[] expectedFieldWhereExpr = expectedFieldWhereExprList[item];
             String[] expectedFieldOrderByExpr = expectedFieldOrderByExprList[item];
 
             List joinList = new ArrayList(queryDefn.getJoins().size());
             String[] expectedJoinName = new String[]{"join_01", "join_02", "join_03", "join_04"};
             String[] expectedJoinTable = new String[]{"join_01", "Table_02", "Table_03", "Table_04"};
             String[] expectedJoinFromExpr = new String[]{expectedJoinName[0], expectedJoinTable[1] + " " + expectedJoinName[1], expectedJoinTable[2] + " " + expectedJoinName[2], expectedJoinTable[3] + " " + expectedJoinName[3]};
 
             for (int i = 0; i < fields.size(); i++)
             {
                 boolean exceptionThrownStatus = (queryDefnIndexWithMalformedSyntax == item && fieldIndexWithBadJoin == i) ? true : false;
                 boolean qdExceptionThrown = true;
 
                 QueryDefnField field = fields.get(i);
                 assertEquals("field_0" + (i + 1), field.getName());
                 assertEquals(expectedFieldCaption[i], field.getCaption());
                 assertEquals(expectedFieldColumnLabel[i], field.getColumnLabel());
                 assertEquals("column_0" + (i + 1), field.getColumn());
                 assertEquals(field.getName(), field.getColumnAlias());
 
                 try
                 {
                     qdExceptionThrown = true;
                     assertEquals(expectedFieldQualifiedColName[i], field.getQualifiedColName());
                     qdExceptionThrown = false;
                 }
                 catch (QueryDefinitionException e)
                 {
                     assertTrue(qdExceptionThrown);
                     assertEquals(queryDefnIndexWithMalformedSyntax, item);  // query-defn-2 is the one with the missing join...
                     assertEquals(fieldIndexWithBadJoin, i);     // field_04 is the one that uses the missing join...
                 }
 
                 assertEquals(exceptionThrownStatus, qdExceptionThrown);
 
                 try
                 {
                     qdExceptionThrown = true;
                     assertEquals(expectedFieldTableName[i], field.getTableName());
                     qdExceptionThrown = false;
                 }
                 catch (QueryDefinitionException e)
                 {
                     assertTrue(qdExceptionThrown);
                     assertEquals(queryDefnIndexWithMalformedSyntax, item);  // query-defn-2 is the one with the missing join...
                     assertEquals(fieldIndexWithBadJoin, i);     // field_04 is the one that uses the missing join...
                 }
 
                 assertEquals(exceptionThrownStatus, qdExceptionThrown);
 
                 try
                 {
                     qdExceptionThrown = true;
                     assertEquals(expectedFieldTableAlias[i], field.getTableAlias());
                     qdExceptionThrown = false;
                 }
                 catch (QueryDefinitionException e)
                 {
                     assertTrue(qdExceptionThrown);
                     assertEquals(queryDefnIndexWithMalformedSyntax, item);  // query-defn-2 is the one with the missing join...
                     assertEquals(fieldIndexWithBadJoin, i);     // field_04 is the one that uses the missing join...
                 }
 
                 assertEquals(exceptionThrownStatus, qdExceptionThrown);
 
                 try
                 {
                     qdExceptionThrown = true;
                     assertEquals("Select Clause Expected: " + expectedSelectClauseExprAndLabel[i] + ", Actual: " + field.getSelectClauseExprAndLabel(), expectedSelectClauseExprAndLabel[i], field.getSelectClauseExprAndLabel());
                     qdExceptionThrown = false;
                 }
                 catch (QueryDefinitionException e)
                 {
                     assertTrue(qdExceptionThrown);
                     assertEquals(queryDefnIndexWithMalformedSyntax, item);  // query-defn-2 is the one with the missing join...
                     assertEquals(fieldIndexWithBadJoin, i);     // field_04 is the one that uses the missing join...
                 }
 
                 assertEquals(exceptionThrownStatus, qdExceptionThrown);
 
                 try
                 {
                     qdExceptionThrown = true;
                     assertEquals(expectedFieldColExpr[i], field.getColumnExpr());
                     qdExceptionThrown = false;
                 }
                 catch (QueryDefinitionException e)
                 {
                     assertTrue(qdExceptionThrown);
                     assertEquals(queryDefnIndexWithMalformedSyntax, item);  // query-defn-2 is the one with the missing join...
                     assertEquals(fieldIndexWithBadJoin, i);     // field_04 is the one that uses the missing join...
                 }
 
                 assertEquals(exceptionThrownStatus, qdExceptionThrown);
 
                 try
                 {
                     qdExceptionThrown = true;
                     assertEquals("Expected: " + expectedFieldColExpr[i] + ", Actual: " + field.getColumnExpr(), expectedFieldColExpr[i], field.getColumnExpr());
                     qdExceptionThrown = false;
                 }
                 catch (QueryDefinitionException e)
                 {
                     assertTrue(qdExceptionThrown);
                     assertEquals(queryDefnIndexWithMalformedSyntax, item);  // query-defn-2 is the one with the missing join...
                     assertEquals(fieldIndexWithBadJoin, i);     // field_04 is the one that uses the missing join...
                 }
 
                 assertEquals(exceptionThrownStatus, qdExceptionThrown);
 
                 try
                 {
                     qdExceptionThrown = true;
                     assertEquals(expectedFieldWhereExpr[i], field.getWhereExpr());
                     qdExceptionThrown = false;
                 }
                 catch (QueryDefinitionException e)
                 {
                     assertTrue(qdExceptionThrown);
                     assertEquals(queryDefnIndexWithMalformedSyntax, item);  // query-defn-2 is the one with the missing join...
                     assertEquals(fieldIndexWithBadJoin, i);     // field_04 is the one that uses the missing join...
                 }
 
                 assertEquals(exceptionThrownStatus, qdExceptionThrown);
 
                 try
                 {
                     qdExceptionThrown = true;
                     assertEquals(expectedFieldQualifiedColName[i], field.getWhereExpr());
                     qdExceptionThrown = false;
                 }
                 catch (QueryDefinitionException e)
                 {
                     assertTrue(qdExceptionThrown);
                     assertEquals(queryDefnIndexWithMalformedSyntax, item);  // query-defn-2 is the one with the missing join...
                     assertEquals(fieldIndexWithBadJoin, i);     // field_04 is the one that uses the missing join...
                 }
 
                 assertEquals(exceptionThrownStatus, qdExceptionThrown);
 
                 try
                 {
                     qdExceptionThrown = true;
                     assertEquals(expectedFieldOrderByExpr[i], field.getOrderByExpr());
                     qdExceptionThrown = false;
                 }
                 catch (QueryDefinitionException e)
                 {
                     assertTrue(qdExceptionThrown);
                     assertEquals(queryDefnIndexWithMalformedSyntax, item);  // query-defn-2 is the one with the missing join...
                     assertEquals(fieldIndexWithBadJoin, i);     // field_04 is the one that uses the missing join...
                 }
 
                 assertEquals(exceptionThrownStatus, qdExceptionThrown);
 
                 try
                 {
                     qdExceptionThrown = true;
                     assertEquals(expectedFieldQualifiedColName[i], field.getOrderByExpr());
                     qdExceptionThrown = false;
                 }
                 catch (QueryDefinitionException e)
                 {
                     assertTrue(qdExceptionThrown);
                     assertEquals(queryDefnIndexWithMalformedSyntax, item);  // query-defn-2 is the one with the missing join...
                     assertEquals(fieldIndexWithBadJoin, i);     // field_04 is the one that uses the missing join...
                 }
 
                 assertEquals(exceptionThrownStatus, qdExceptionThrown);
 
                 if (1 < i)
                 {
                     QueryDefnJoin qdJoin = null;
                     try
                     {
                         qdExceptionThrown = true;
                         qdJoin = field.getJoin();
                         qdExceptionThrown = false;
                     }
                     catch (QueryDefinitionException e)
                     {
                         assertTrue(qdExceptionThrown);
                         assertEquals(queryDefnIndexWithMalformedSyntax, item);  // query-defn-2 is the one with the missing join...
                         assertEquals(fieldIndexWithBadJoin, i);     // field_04 is the one that uses the missing join...
                     }
 
 //                    if (! (queryDefnIndexWithMalformedSyntax == item && fieldIndexWithBadJoin == i))
                     assertNull(qdJoin);
                 }
                 else
                 {
                     joinList.add(field.getJoin());
                     QueryDefnJoin[] impliedJoins = getJoins(field.getJoin());
 
                     if (null != impliedJoins)
                         for (int j = 0; j < impliedJoins.length; j ++)
                             joinList.add(impliedJoins[j]);
                 }
             }
 
             for (int i = 0; i < joinList.size(); i ++)
             {
                 QueryDefnJoin join = (QueryDefnJoin) joinList.get(i);
 
                 assertNull(join.getCondition());
                 assertFalse(join.shouldAutoInclude());
                 assertEquals(expectedJoinName[i], join.getName());
                 assertEquals(expectedJoinTable[i], join.getTable());
                 assertEquals(expectedJoinFromExpr[i], join.getFromExpr());
             }
 
 			assertEquals(0, queryDefn.getWhereExpressions().size());
 
             QueryDefnSelect qdsl = queryDefn.getSelects().get("query-select-1");
             assertNotNull(qdsl);
 
             //TODO: See if this assertion is really necessary
             //assertEquals("jdbc:hsqldb:" + dbFilename, TestUtils.getConnprovider().getDataSourceInfo(this.getClass().getPackage().getName()).getConnUrl());
 
             //DatabaseConnValueContext dbvc = new BasicDatabaseConnValueContext();
             //dbvc.setConnectionProvider(TestUtils.getConnProvider(this.getClass().getPackage().getName()));
         //        ConnectionContext cc = dbvc.getConnection(this.getClass().getPackage().getName(), true);
 
             //ValueSource vs = ValueSources.getInstance().getValueSource("data-sources:", ValueSources.VSNOTFOUNDHANDLER_THROW_EXCEPTION);
             //Value value = vs.getValue(dbvc);
 //            System.out.println(value.getListValue());
 
             String dtd = new XmlDataModelDtd().getDtd(component);
             assertTrue(dtd != null);
 
             //System.out.println(dtd);
         }
     }
 
     public void testQueryDefnOneFields() throws QueryDefinitionException, DataModelException, InstantiationException, InvocationTargetException, NoSuchMethodException, IOException, IllegalAccessException, NamingException, SQLException
     {
         QueryDefinitionsCollection queryDefns = (QueryDefinitionsCollection) manager.getQueryDefns();
         assertNotNull(queryDefns);
         assertEquals(queryDefnNames.length, queryDefns.size());
 
         Set actualQueryDefnNames = queryDefns.getNames();
         for (int i = 0; i < queryDefnNames.length; i++)
         {
             assertNotNull(queryDefns.get(queryDefnNames[i]));
             assertTrue(actualQueryDefnNames.contains(queryDefnNames[i].toUpperCase()));
         }
 
         QueryDefinition queryDefn = manager.getQueryDefinition("query-defn-1");
         assertNotNull(queryDefn);
 
         // Verify that this query-defn has the right number of fingers and toes ;)
         assertEquals(numFields[0], queryDefn.getFields().size());
 
         // Verify Fields...
         QueryDefnFields fields = queryDefn.getFields();
         String[] expectedFieldCaption = new String[] { null, "Test Field 02 Caption", "Test Field 03 Caption", "Test Field 04 Caption", "Test Field 05 Caption" };
         String[] expectedFieldColumnLabel = new String[] { "field_01", "Test Field 02 Caption", "Test Field 03 Caption", "Test Field 04 Caption", "Test Field 05 Caption" };
         String[] expectedFieldQualifiedColNameOne = new String[] { "join_01.column_01", "join_02.column_02a", "column_03", "column_04", "column_05" };
         String[] expectedFieldQualifiedColNameTwo = new String[] { "join_01.column_01", "join_02.column_02", "column_03", "column_04", "column_05" };
         String[] expectedFieldTableName = new String[] { "join_01", "Table_02", null, null, null };
         String[] expectedFieldTableAlias = new String[] { "join_01", "join_02", null, null, null };
         String[] expectedSelectClauseExprAndLabel = new String[] { "join_01.column_01 as \"field_01\"", "join_02.column_02a as \"Test Field 02 Caption\"", "column_03 as \"Test Field 03 Caption\"", "column_04 as \"Test Field 04 Caption\"", "column_05 as \"Test Field 05 Caption\"" };
         String[] expectedFieldColExpr = new String[]{"join_01.column_01", "join_02.column_02a", "column_03", "column_04", "column_05"};
         String[] expectedFieldWhereExpr = new String[]{"join_01.column_01", "join_02.column_02", "column_03", "column_04", "column_05"};
         String[] expectedFieldOrderByExpr = new String[]{"join_01.column_01", "join_02.column_02", "column_03", "column_04", "column_05"};
 
         for (int i = 0; i < fields.size(); i++)
         {
             QueryDefnField field = fields.get(i);
             assertEquals("field_0" + (i + 1), field.getName());
             assertEquals(expectedFieldCaption[i], field.getCaption());
             assertEquals(expectedFieldColumnLabel[i], field.getColumnLabel());
             assertEquals("column_0" + (i + 1), field.getColumn());
             assertEquals(field.getName(), field.getColumnAlias());
             assertEquals("Qualified Name Expected: " + expectedFieldQualifiedColNameTwo[i] + ", Actual: " + field.getQualifiedColName(), expectedFieldQualifiedColNameTwo[i], field.getQualifiedColName());
             assertEquals(expectedFieldTableName[i], field.getTableName());
             assertEquals(expectedFieldTableAlias[i], field.getTableAlias());
             assertEquals("Select Clause Expected: " + expectedSelectClauseExprAndLabel[i] + ", Actual: " + field.getSelectClauseExprAndLabel(), expectedSelectClauseExprAndLabel[i], field.getSelectClauseExprAndLabel());
             assertEquals(expectedFieldColExpr[i], field.getColumnExpr());
             assertEquals(expectedFieldQualifiedColNameOne[i], field.getColumnExpr());
             assertEquals(expectedFieldWhereExpr[i], field.getWhereExpr());
             assertEquals(expectedFieldQualifiedColNameTwo[i], field.getWhereExpr());
             assertEquals(expectedFieldOrderByExpr[i], field.getOrderByExpr());
             assertEquals(expectedFieldQualifiedColNameTwo[i], field.getOrderByExpr());
         }
     }
 
     public void testQueryDefnOneJoins() throws QueryDefinitionException, DataModelException, InstantiationException, InvocationTargetException, NoSuchMethodException, IOException, IllegalAccessException, NamingException, SQLException
     {
         QueryDefinitionsCollection queryDefns = (QueryDefinitionsCollection) manager.getQueryDefns();
         assertNotNull(queryDefns);
         assertEquals(queryDefnNames.length, queryDefns.size());
 
         Set actualQueryDefnNames = queryDefns.getNames();
         for (int i = 0; i < queryDefnNames.length; i++)
         {
             assertNotNull(queryDefns.get(queryDefnNames[i]));
             assertTrue(actualQueryDefnNames.contains(queryDefnNames[i].toUpperCase()));
         }
 
         QueryDefinition queryDefn = manager.getQueryDefinition("query-defn-1");
         assertNotNull(queryDefn);
         assertEquals(queryDefn, queryDefns.get("query-defn-1"));
 
         // Verify that this query-defn has the right number of fingers and toes ;)
         assertEquals(numJoins[0], queryDefn.getJoins().size());
 
         QueryDefnFields fields = queryDefn.getFields();
 
         List joinList = new ArrayList(queryDefn.getJoins().size());
         String[] expectedJoinName = new String[]{"join_01", "join_02", "join_03", "join_04", "join_05"};
         String[] expectedJoinTable = new String[]{"join_01", "Table_02", "Table_03", "Table_04", "Table_05"};
         String[] expectedJoinFromExpr = new String[]{expectedJoinName[0], expectedJoinTable[1] + " " + expectedJoinName[1], expectedJoinTable[2] + " " + expectedJoinName[2], expectedJoinTable[3] + " " + expectedJoinName[3]};
 
         for (int i = 0; i < fields.size(); i++)
         {
             QueryDefnField field = fields.get(i);
 
             if (1 < i)
                 assertNull(field.getJoin());
             else
             {
                 joinList.add(field.getJoin());
                 QueryDefnJoin[] impliedJoins = getJoins(field.getJoin());
 
                 if (null != impliedJoins)
                     for (int j = 0; j < impliedJoins.length; j++)
                         joinList.add(impliedJoins[j]);
             }
         }
 
         for (int i = 0; i < joinList.size(); i++)
         {
             QueryDefnJoin join = (QueryDefnJoin) joinList.get(i);
 
             assertNull(join.getCondition());
             assertFalse(join.shouldAutoInclude());
             assertEquals(expectedJoinName[i], join.getName());
             assertEquals(expectedJoinTable[i], join.getTable());
             assertEquals(expectedJoinFromExpr[i], join.getFromExpr());
         }
 
         QueryDefnJoin join = queryDefn.getJoins().get("join_05");
         assertNull(join.getCondition());
         assertTrue(join.shouldAutoInclude());
         assertEquals(expectedJoinName[4], join.getName());
         assertEquals(expectedJoinTable[4], join.getTable());
         assertEquals(expectedJoinTable[4] + " " + expectedJoinName[4], join.getFromExpr());
     }
 
     public void testQueryDefnOneSelects() throws QueryDefinitionException, DataModelException, InstantiationException, InvocationTargetException, NoSuchMethodException, IOException, IllegalAccessException, NamingException, SQLException
     {
         QueryDefinitionsCollection queryDefns = (QueryDefinitionsCollection) manager.getQueryDefns();
         assertNotNull(queryDefns);
         assertEquals(queryDefnNames.length, queryDefns.size());
 
         Set actualQueryDefnNames = queryDefns.getNames();
         for (int i = 0; i < queryDefnNames.length; i++)
         {
             assertNotNull(queryDefns.get(queryDefnNames[i]));
             assertTrue(actualQueryDefnNames.contains(queryDefnNames[i].toUpperCase()));
         }
 
         QueryDefinition queryDefn = manager.getQueryDefinition("query-defn-1");
         assertNotNull(queryDefn);
         assertEquals(queryDefn, queryDefns.get("query-defn-1"));
 
         // Verify that this query-defn has the right number of fingers and toes ;)
         assertEquals(numSelects[0], queryDefn.getSelects().size());
 
         // Verify Selects...
         QueryDefnSelects qdSelects = queryDefn.getSelects();
         assertNotNull(qdSelects);
         assertSame(qdSelects.get(0), qdSelects.get("query-select-1"));
 
         QueryDefnSelect select = qdSelects.get("query-select-1");
         assertNotNull(select);
 
         assertTrue(select.distinctRowsOnly());
         select.setDistinct(false);
         assertFalse(select.distinctRowsOnly());
         select.setDistinct(true);
         assertTrue(select.distinctRowsOnly());
 
 		String[] expectedDisplayFieldNames = new String[] {"field_01", "field_02", "field_03"};
 		String[] expectedGroupByFieldNames = new String[] {"field_01"};
 		String[] expectedOrderByFieldNames = new String[] {"field_03"};
 		int[] expectedWhereExpressionConnector = new int[] {QueryDefnCondition.CONNECT_AND};
 		String[] expectedWhereExpressions = new String[] {"field_01 in ('A', 'B', 'C')"};
 
 		String[][] expectedConditionExpressions = new String[][] {
 			new String[] {"field_01", "equals", "10", QueryDefnCondition.CONNECTOR_SQL[QueryDefnCondition.CONNECT_AND]},
 			new String[] {"field_02", "starts-with", "a", QueryDefnCondition.CONNECTOR_SQL[QueryDefnCondition.CONNECT_AND]},
 			new String[] {"field_03", "contains-ignore-case", "eve", QueryDefnCondition.CONNECTOR_SQL[QueryDefnCondition.CONNECT_AND]},
 			new String[] {"field_04", "ends-with", "adam", QueryDefnCondition.CONNECTOR_SQL[QueryDefnCondition.CONNECT_AND]},
 			new String[] {"field_05", "contains", "abel", QueryDefnCondition.CONNECTOR_SQL[QueryDefnCondition.CONNECT_AND]},
 			new String[] {"field_01", "in", "'C', 'D', 'E'", QueryDefnCondition.CONNECTOR_SQL[QueryDefnCondition.CONNECT_AND]},
 			new String[] {"field_02", "is-defined", "field_02", QueryDefnCondition.CONNECTOR_SQL[QueryDefnCondition.CONNECT_AND]},
 			new String[] {"field_03", "lte-date", "10/11/2012", QueryDefnCondition.CONNECTOR_SQL[QueryDefnCondition.CONNECT_OR]},
 			new String[] {"field_04", "greater-than", "10", QueryDefnCondition.CONNECTOR_SQL[QueryDefnCondition.CONNECT_AND]},
 		};
 
         assertSame(queryDefn, select.getQueryDefn());
 
 		assertEquals(numDisplayFields[0], select.getDisplayFields().size());
 		QueryDefnFields qdDisplayFields = select.getDisplayFields();
         for (int i = 0; i < qdDisplayFields.size(); i ++) {
 			QueryDefnField qdField = qdDisplayFields.get(i);
 
 			assertEquals(expectedDisplayFieldNames[i], qdField.getName());
 		}
 
         assertEquals(numGroupBys[0], select.getGroupByFields().size());
 		QueryDefnFields qdGroupByFields = select.getGroupByFields();
 		for (int i = 0; i < qdGroupByFields.size(); i ++) {
 			QueryDefnField qdField = qdGroupByFields.get(i);
 
 			assertEquals(expectedGroupByFieldNames[i], qdField.getName());
 		}
 
         assertEquals(numOrderBys[0], select.getOrderByFieldRefs().size());
 		QueryDefnSortFieldReferences qdSortFieldRefs = select.getOrderByFieldRefs();
 		for (int i = 0; i < qdSortFieldRefs.size(); i ++) {
 			QueryDefnSortFieldReference qdSortFieldRef = qdSortFieldRefs.get(i);
 
 			QueryDefinition.QueryFieldSortInfo[] qfSortInfo = qdSortFieldRef.getFields(null);
 
 			for (int j = 0; j < qfSortInfo.length; j ++) {
 				assertEquals(expectedOrderByFieldNames[j], qfSortInfo[j].getField().getName());
 				assertFalse(qfSortInfo[j].isDescending());
 			}
 		}
 
         assertEquals(numWhereExpr[0], select.getWhereExprs().size());
 		QueryDefnSqlWhereExpressions whereExprs = select.getWhereExprs();
 		QueryDefnSelectStmtGenerator stmtGen = dbPolicy.createSelectStatementGenerator(select);
 
 		for (int i = 0; i < whereExprs.size(); i ++) {
 			QueryDefnSqlWhereExpression whereExpr = whereExprs.get(i);
 
 			assertEquals(expectedWhereExpressionConnector[i], whereExpr.getConnector());
 			assertEquals(expectedWhereExpressions[i], whereExpr.getWhereCondExpr(stmtGen, null));
 		}
 
         assertEquals(numConditions[0], select.getConditions().size());
 		QueryDefnConditions qdConditions = select.getConditions();
 
 		for (int i = 0; i < qdConditions.size(); i ++) {
 			QueryDefnCondition qdCondition = qdConditions.get(i);
 			String[] expectedConditionExpression = expectedConditionExpressions[i];
 
 			assertSame(queryDefn, qdCondition.getOwner());
 			assertTrue(qdCondition.isNotNested());
 			assertFalse(qdCondition.isNested());
 			assertNull(qdCondition.getParentCondition());
 			assertEquals(expectedConditionExpression[0], qdCondition.getField().getName());
 			assertEquals(expectedConditionExpression[1], qdCondition.getComparison().getName());
 			assertEquals(expectedConditionExpression[2], qdCondition.getValue().getTextValue(null));
 			assertEquals(expectedConditionExpression[3], qdCondition.getConnectorSql());
 			assertNull(qdCondition.getBindExpr());
 		}
 
         //TODO: See if this assertion is really necessary
         //assertEquals("jdbc:hsqldb:" + dbFilename, TestUtils.getConnprovider().getDataSourceInfo(this.getClass().getPackage().getName()).getConnUrl());
 
         DatabaseConnValueContext dbvc = new BasicDatabaseConnValueContext();
         dbvc.setConnectionProvider(TestUtils.getConnProvider(this.getClass().getPackage().getName()));
         ConnectionContext cc = dbvc.getConnection(this.getClass().getPackage().getName(), true);
 
		String expectedSqlOne = "select distinct join_01.column_01 as \"field_01\", join_02.column_02a as \"Test Field 02 Caption\", column_03 as \"Test Field 03 Caption\" from join_01, Table_02 join_02, Table_03 join_03, /* implied by join definition 'join_02' */ Table_04 join_04, /* implied by join definition 'join_03' */ Table_05 join_05 /* auto-included for join definition 'join_05' */ where ( (join_01.column_01 = ?) and (join_02.column_02 like ?) and (UPPER(column_03) like UPPER(?)) and (column_05 like ?) and (join_01.column_01 in (?)) and (join_02.column_02 is not null) and (column_03 like ?) ) and (field_01 in ('A', 'B', 'C') ) group by join_01.column_01 order by column_03";
 
         String sqlOne = select.getSqlText(cc);
         sqlOne = TextUtils.join(TextUtils.split(sqlOne, " \r\t\f\n", true), " ");
 
 		//System.out.println("\n" + sqlOne + "\n" + expectedSqlOne);
 
         assertEquals(expectedSqlOne, sqlOne);
 
         String dtd = new XmlDataModelDtd().getDtd(component);
         assertTrue(dtd != null);
 
         cc.close();
 
         //System.out.println(dtd);
     }
 
     protected QueryDefnJoin[] getJoins (QueryDefnJoin join) throws QueryDefinitionException
     {
         List impliedJoins = new ArrayList();
 
         QueryDefnJoin[] joins = join.getImpliedJoins();
 
         if (null == joins) return joins;
 
         for (int i = 0; i < joins.length; i ++)
         {
             impliedJoins.add(joins[i]);
 
             QueryDefnJoin[] deeperJoins = getJoins(joins[i]);
 
             if (null != deeperJoins)
                 for (int j = 0; j < deeperJoins.length; j ++)
                     impliedJoins.add(deeperJoins[j]);
         }
 
         return (QueryDefnJoin[]) impliedJoins.toArray(new QueryDefnJoin[impliedJoins.size()]);
     }
 
 }
