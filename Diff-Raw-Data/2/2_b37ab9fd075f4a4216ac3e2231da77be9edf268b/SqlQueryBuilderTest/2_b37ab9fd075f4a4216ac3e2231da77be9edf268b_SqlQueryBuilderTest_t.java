 /***************************************************************
  *  This file is part of the [fleXive](R) framework.
  *
  *  Copyright (c) 1999-2014
  *  UCS - unique computing solutions gmbh (http://www.ucs.at)
  *  All rights reserved
  *
  *  The [fleXive](R) project is free software; you can redistribute
  *  it and/or modify it under the terms of the GNU Lesser General Public
  *  License version 2.1 or higher as published by the Free Software Foundation.
  *
  *  The GNU Lesser General Public License can be found at
  *  http://www.gnu.org/licenses/lgpl.html.
  *  A copy is found in the textfile LGPL.txt and important notices to the
  *  license from the author are found in LICENSE.txt distributed with
  *  these libraries.
  *
  *  This library is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  For further information about UCS - unique computing solutions gmbh,
  *  please see the company website: http://www.ucs.at
  *
  *  For further information about [fleXive](R), please see the
  *  project website: http://www.flexive.org
  *
  *
  *  This copyright notice MUST APPEAR in all copies of the file!
  ***************************************************************/
 package com.flexive.tests.embedded;
 
 import com.flexive.shared.CacheAdmin;
 import com.flexive.shared.FxLanguage;
 import com.flexive.shared.exceptions.FxApplicationException;
 import com.flexive.shared.exceptions.FxRuntimeException;
 import com.flexive.shared.search.FxResultRow;
 import com.flexive.shared.search.FxResultSet;
 import com.flexive.shared.search.SortDirection;
 import com.flexive.shared.search.query.AssignmentValueNode;
 import com.flexive.shared.search.query.PropertyValueComparator;
 import com.flexive.shared.search.query.QueryOperatorNode.Operator;
 import com.flexive.shared.search.query.SqlQueryBuilder;
 import com.flexive.shared.search.query.VersionFilter;
 import com.flexive.shared.structure.FxDataType;
 import com.flexive.shared.structure.FxEnvironment;
 import com.flexive.shared.value.FxDate;
 import com.flexive.shared.value.FxString;
 import com.flexive.tests.embedded.QueryNodeTreeTests.AssignmentNodeGenerator;
 import org.testng.Assert;
 import org.testng.annotations.Test;
 
 import java.util.Arrays;
 import java.util.Date;
 import java.util.Formatter;
 import java.util.List;
 
 import static org.testng.Assert.assertEquals;
 import static org.testng.Assert.assertTrue;
 
 /**
  * Tests for the SqlQueryBuilder class.
  *
  * @author Daniel Lichtenberger (daniel.lichtenberger@flexive.com), UCS - unique computing solutions gmbh (http://www.ucs.at)
  * @see com.flexive.shared.search.query.SqlQueryBuilder
  */
 @Test(groups = {"ejb", "search"})
 public class SqlQueryBuilderTest {
     /**
      * A very basic query test with one condition.
      */
     @Test
     public void simpleQuery() {
         AssignmentNodeGenerator generator = new AssignmentNodeGenerator(FxDataType.String1024, new FxString("test"));
         AssignmentValueNode node = generator.createNode(0);
         Assert.assertTrue(node.getValue().getDefaultTranslation().equals("test"));
         node.setComparator(PropertyValueComparator.EQ);
 
         SqlQueryBuilder builder = new SqlQueryBuilder().andSub();
         buildAssignmentNode(builder, node);
         final String expected = ("(#" + generator.getAssignment().getXPath() + " = 'test')");
         assertConditions(builder, expected);
     }
 
     /**
      * A query with several expressions, but without nested conditions.
      */
     @Test
     public void oneLevelQuery() {
         AssignmentNodeGenerator generator = new AssignmentNodeGenerator(FxDataType.String1024, new FxString("test"));
         AssignmentValueNode node = generator.createNode(0);
         AssignmentValueNode node2 = generator.createNode(1);
         node2.getValue().setDefaultTranslation("value2");
         AssignmentValueNode node3 = generator.createNode(2);
         node3.getValue().setDefaultTranslation("value3");
 
         SqlQueryBuilder builder = new SqlQueryBuilder();
         builder.enterSub(Operator.OR);
         buildAssignmentNode(builder, node);
         buildAssignmentNode(builder, node2);
         buildAssignmentNode(builder, node3);
         builder.closeSub();
 
         final String expected = new Formatter().format("(%1$s = 'test' OR %1$s = 'value2' OR %1$s = 'value3')",
                 "#" + generator.getAssignment().getXPath()).toString();
         assertConditions(builder, expected);
     }
 
     /*
       * A query with a nested condition.
       */
     @Test
     public void twoLevelQuery() {
         AssignmentNodeGenerator generator = new AssignmentNodeGenerator(FxDataType.String1024, new FxString("test"));
         AssignmentValueNode node = generator.createNode(0);
         AssignmentValueNode node2 = generator.createNode(1);
         node2.getValue().setDefaultTranslation("value2");
         AssignmentValueNode node3 = generator.createNode(2);
         node3.getValue().setDefaultTranslation("value3");
 
         SqlQueryBuilder builder = new SqlQueryBuilder();
         builder.enterSub(Operator.AND);
         buildAssignmentNode(builder, node);
         builder.enterSub(Operator.OR);
         buildAssignmentNode(builder, node2);
         buildAssignmentNode(builder, node3);
         builder.closeSub();
         buildAssignmentNode(builder, node3);
         builder.closeSub();
 
         final String expected = new Formatter().format("(%1$s = 'test' AND (%1$s = 'value2' OR "
                 + "%1$s = 'value3') AND %1$s = 'value3')",
                 "#" + generator.getAssignment().getXPath()).toString();
         assertConditions(builder, expected);
     }
 
     @Test
     public void twoLevelQuery2() {
         SqlQueryBuilder builder = new SqlQueryBuilder();
         builder.enterSub(Operator.AND).enterSub(Operator.OR)
                 .condition("title", PropertyValueComparator.LIKE, "abc")
                 .condition("caption", PropertyValueComparator.LIKE, "def")
                 .closeSub()
                 .condition("title", PropertyValueComparator.NOT_EMPTY, null);
         final String expected = "((title LIKE 'abc' OR caption LIKE 'def') AND title IS NOT NULL)";
         assertConditions(builder, expected);
     }
 
     /**
      * Test the text-only conditions builder
      */
     @Test
     public void textBuilderQuery() {
         SqlQueryBuilder builder = new SqlQueryBuilder();
         builder.enterSub(Operator.AND)
                 .condition("caption", PropertyValueComparator.LIKE, new FxString("test"))
                 .condition("date", PropertyValueComparator.EQ, new FxDate(new Date(0)))
                 .closeSub();
         final String expected = "(caption LIKE 'test' AND date = '1970-01-01')";
         assertConditions(builder, expected);
     }
 
     @Test
     public void getColumnNames() {
         SqlQueryBuilder builder = new SqlQueryBuilder();
         Assert.assertTrue(!builder.getColumnNames().isEmpty(), "Default column must not be empty");
         Assert.assertTrue(builder.getColumnNames().contains("@pk"), "Primary key must be selected by default.");
     }
 
     private void assertConditions(SqlQueryBuilder builder, final String expected) {
         String conditions = builder.getConditions();
         final String commentsPattern = "/\\*.+?\\*/\\s*";
         conditions = conditions.replaceAll(commentsPattern, "");  // strip comments
         Assert.assertTrue(expected.equalsIgnoreCase(conditions),
                 "Invalid conditions: " + conditions + ", expected: " + expected);
         Assert.assertTrue(builder.getQuery().replaceAll(commentsPattern, "").toUpperCase().indexOf(expected.toUpperCase()) > 0,
                 "Query must contain conditions: " + expected + ", generated getResult:\n" + builder.getQuery());
     }
 
     private void buildAssignmentNode(SqlQueryBuilder builder, AssignmentValueNode node) {
         builder.condition(node.getAssignment(), node.getComparator(), node.getValue());
     }
 
     @Test
     public void propertyNameQuery() {
         new SqlQueryBuilder().enterSub(Operator.AND)
                 .condition("*", PropertyValueComparator.EQ, "'test'").closeSub().getQuery();
     }
 
     @Test
     public void childQuery() {
         assertConditions(new SqlQueryBuilder().isChild(25), "(IS CHILD OF 25)");
         assertConditions(new SqlQueryBuilder().isChild(25).isDirectChild(26), "(IS CHILD OF 25 AND IS DIRECT CHILD OF 26)");
     }
 
     @Test
     public void emptyQuery() {
         new SqlQueryBuilder().getQuery();   // ok
         new SqlQueryBuilder().enterSub(Operator.AND).closeSub().getQuery(); // ok
     }
 
     /**
      * Checks if calls to {@link com.flexive.shared.search.query.SqlQueryBuilder#getQuery()}
      * and {@link com.flexive.shared.search.query.SqlQueryBuilder#getConditions()}
      * can be executed multiple times without changing the result.
      */
     @Test
     public void repeatableQueryTest() {
         final SqlQueryBuilder builder = new SqlQueryBuilder().condition("caption", PropertyValueComparator.LIKE, "test%");
         final String query = builder.getQuery();
         final String query2 = builder.getQuery();
         Assert.assertEquals(query, query2, "Second call to getQuery() returned a different "
                 + "result than the first one:\n"
                 + "[1]: " + query + "\n[2]: " + query2);
     }
 
     @Test
     public void copyQueryBuilderConditionTest() {
         final SqlQueryBuilder builder = new SqlQueryBuilder().condition("title", PropertyValueComparator.LIKE, "test%");
         final SqlQueryBuilder builder2 = new SqlQueryBuilder(builder);
         builder2.condition("title", PropertyValueComparator.LIKE, "123%");
         Assert.assertTrue(!builder.getQuery().equals(builder2.getQuery()), "Queries should not be equal - copy constructor not implemented correctly");
     }
 
     @Test
     public void copyQueryBuilderSelectTest() {
         final SqlQueryBuilder builder = new SqlQueryBuilder().select("@pk").condition("title", PropertyValueComparator.LIKE, "test%");
         final SqlQueryBuilder builder2 = new SqlQueryBuilder(builder).select("@pk", "version");
         Assert.assertTrue(!builder.getQuery().equals(builder2.getQuery()), "Queries should not be equal");
         Assert.assertTrue(!builder.getQuery().contains("@pk, version"));
         Assert.assertTrue(builder2.getQuery().contains("@pk, version"));
     }
 
     @Test
     public void filterTypeTest() {
         final SqlQueryBuilder builder = new SqlQueryBuilder().select("@pk").filterType("MYTYPE");
         Assert.assertTrue(builder.getFilters().indexOf("TYPE=MYTYPE") != -1, "Filter missing: " + builder.getFilters());
         Assert.assertTrue(builder.getQuery().contains("TYPE=MYTYPE"), "Filter not contained in getResult: " + builder.getQuery());
 
         final SqlQueryBuilder builder2 = new SqlQueryBuilder().select("@pk").filterType(21);
         Assert.assertTrue(builder2.getFilters().indexOf("TYPE=21") != -1, "Filter missing: " + builder2.getFilters());
         Assert.assertTrue(builder2.getQuery().contains("TYPE=21"), "Filter not contained in getResult: " + builder2.getQuery());
         // remove filter
         builder2.filterType(-1);
         Assert.assertTrue(builder2.getFilters().indexOf("TYPE=") == -1, "Type filter not removed: " + builder2.getFilters());
         Assert.assertTrue(!builder2.getQuery().contains("TYPE=21"), "Filter still present in getResult: " + builder2.getQuery());
     }
 
     @Test
     public void filterVersionTest() {
         final SqlQueryBuilder builder = new SqlQueryBuilder().select("@pk").filterVersion(VersionFilter.ALL);
         assertEquals(builder.getFilters(), "VERSION=ALL");
         Assert.assertTrue(builder.getQuery().contains("FILTER VERSION=ALL"), "Filter no contained: " + builder.getQuery());
 
         builder.filterVersion(VersionFilter.MAX);
         assertEquals(builder.getFilters(), "VERSION=MAX");
 
         builder.filterVersion(VersionFilter.LIVE);
         assertEquals(builder.getFilters(), "VERSION=LIVE");
     }
 
     @Test
     public void illegalImplicitScopeTest() {
         try {
             final SqlQueryBuilder builder = new SqlQueryBuilder().orSub().isChild(1).closeSub()
                     .andSub().isChild(5);
             Assert.fail("Query builder assembled illegal getResult: " + builder.getQuery());
         } catch (FxRuntimeException e) {
             // pass
         }
         try {
             final SqlQueryBuilder builder2 = new SqlQueryBuilder().orSub().isChild(1).closeSub().isChild(5);
             Assert.fail("Query builder assembled illegal getResult: " + builder2.getQuery());
         } catch (FxRuntimeException e) {
             // pass
         }
     }
 
     @Test
     public void orderByTest() {
         final SqlQueryBuilder builder = new SqlQueryBuilder().select("property", "anotherProperty").orderBy("property", SortDirection.ASCENDING);
         Assert.assertTrue(builder.getQuery().contains("ORDER BY 1"), "Order by not contained in getResult: " + builder.getQuery());
         builder.orderBy("anotherProperty", SortDirection.DESCENDING);
         Assert.assertTrue(!builder.getQuery().contains("ORDER BY 1"), "Old order by should be removed: " + builder.getQuery());
         Assert.assertTrue(builder.getQuery().contains("ORDER BY 2 DESC"), "Order by not contained in getResult: " + builder.getQuery());
         builder.orderBy(1, SortDirection.ASCENDING);
         builder.orderBy(2, SortDirection.DESCENDING);
         try {
             builder.orderBy(3, SortDirection.ASCENDING);
             Assert.fail("Column 2 doesn't exist");
         } catch (Exception e) {
             // pass
         }
     }
 
     @Test
     public void selectAssignmentTest() {
         for (SqlQueryBuilder builder: new SqlQueryBuilder[] {
                 new SqlQueryBuilder().select("mycontent/mycaption"),
                 new SqlQueryBuilder().select("#mycontent/mycaption")
         }) {
             Assert.assertTrue(builder.getQuery().contains("SELECT #mycontent/mycaption"), "Expected assignment query: " + builder.getQuery());
         }
     }
 
     @Test
     public void conditionFunctionsTest() {
         for (String prop: new String[] { "dateprop", "searchtest/dateprop" }) {
             final String query = new SqlQueryBuilder().select("id").condition("year(" + prop + ")", PropertyValueComparator.EQ, 2008).getQuery();
             final String fun = "year(" + (prop.indexOf('/') != -1 ? "#" + prop : prop) + ") = 2008";
             Assert.assertTrue(query.toUpperCase().contains(fun.toUpperCase()), "Expected query to contain " + fun + ", got: " + query);
         }
     }
 
     @Test
     public void inConditionTest() {
         checkInCondition(new SqlQueryBuilder().condition("title", PropertyValueComparator.IN, new String[]{"Hello", "World"}));
         checkInCondition(new SqlQueryBuilder().condition("title", PropertyValueComparator.IN, Arrays.asList("Hello", "World")));
     }
 
     @Test
     public void sortByAliasTest() throws FxApplicationException {
         final FxResultSet result = new SqlQueryBuilder().select("@pk as  objectId").maxRows(10).orderBy("objectId", SortDirection.ASCENDING).getResult();
         long last = 0;
         for (FxResultRow row : result.getResultRows()) {
             final long id = row.getPk(1).getId();
             Assert.assertTrue(id > last, "Result not sorted by ascending IDs");
             last = id;
         }
     }
 
     @Test
     public void searchLanguagesTest() {
         final FxEnvironment env = CacheAdmin.getEnvironment();
         final List<FxLanguage> languages = Arrays.asList(env.getLanguage("en"), env.getLanguage("de"));
         final String query = new SqlQueryBuilder().select("@pk").searchLanguages(languages).getQuery();
        assertTrue(query.contains("SEARCH_LANGUAGES='en'|'de'"), "Search language filter not found in: " + query);
     }
 
     private void checkInCondition(SqlQueryBuilder sqlQueryBuilder) {
         final String query = sqlQueryBuilder.getQuery();
         Assert.assertTrue(query.toUpperCase().contains("TITLE IN ('HELLO','WORLD')"), "Invalid 'in' condition: " + query);
     }
 
 }
