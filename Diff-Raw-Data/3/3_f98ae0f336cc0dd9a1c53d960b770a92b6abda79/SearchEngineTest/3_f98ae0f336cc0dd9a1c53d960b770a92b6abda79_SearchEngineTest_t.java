 /***************************************************************
  *  This file is part of the [fleXive](R) framework.
  *
  *  Copyright (c) 1999-2008
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
 
 import com.flexive.shared.*;
 import static com.flexive.shared.FxLanguage.ENGLISH;
 import static com.flexive.shared.FxLanguage.GERMAN;
 import static com.flexive.shared.EJBLookup.getContentEngine;
 import static com.flexive.shared.EJBLookup.getMandatorEngine;
 import static com.flexive.shared.EJBLookup.getSearchEngine;
 import static com.flexive.shared.EJBLookup.getAssignmentEngine;
 import static com.flexive.shared.EJBLookup.getTypeEngine;
 import static com.flexive.shared.EJBLookup.getAclEngine;
 import static com.flexive.shared.EJBLookup.getTreeEngine;
 import static com.flexive.shared.EJBLookup.getLanguageEngine;
 import com.flexive.shared.tree.FxTreeNode;
 import com.flexive.shared.tree.FxTreeMode;
 import com.flexive.shared.tree.FxTreeNodeEdit;
 import com.flexive.shared.workflow.Step;
 import com.flexive.shared.workflow.StepDefinition;
 import com.flexive.shared.content.FxPK;
 import com.flexive.shared.content.FxContent;
 import com.flexive.shared.exceptions.FxApplicationException;
 import com.flexive.shared.exceptions.FxNoAccessException;
 import com.flexive.shared.search.*;
 import com.flexive.shared.search.query.PropertyValueComparator;
 import com.flexive.shared.search.query.QueryOperatorNode;
 import com.flexive.shared.search.query.SqlQueryBuilder;
 import com.flexive.shared.search.query.VersionFilter;
 import static com.flexive.shared.search.query.PropertyValueComparator.EQ;
 import com.flexive.shared.security.*;
 import static com.flexive.shared.security.ACL.Permission;
 import com.flexive.shared.structure.*;
 import com.flexive.shared.value.*;
 import static com.flexive.tests.embedded.FxTestUtils.login;
 import static com.flexive.tests.embedded.FxTestUtils.logout;
 import static org.testng.Assert.assertEquals;
 import org.testng.annotations.AfterClass;
 import org.testng.annotations.BeforeClass;
 import org.testng.annotations.Test;
 import org.testng.annotations.DataProvider;
 import org.testng.Assert;
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.lang.RandomStringUtils;
 import org.apache.commons.collections.CollectionUtils;
 
 import java.util.*;
 import java.text.SimpleDateFormat;
 import java.text.ParseException;
 
 /**
  * FxSQL search query engine tests.
  * <p/>
  * Test data is created in init1201_testcontent.gy !
  *
  * @author Daniel Lichtenberger (daniel.lichtenberger@flexive.com), UCS - unique computing solutions gmbh (http://www.ucs.at)
  * @version $Rev$
  */
 @Test(groups = {"ejb", "search"})
 public class SearchEngineTest {
     private static final String TEST_SUFFIX = "SearchProp";
     private static final String TEST_TYPE = "SearchTest";
     private static final Map<String, FxDataType> TEST_PROPS = new HashMap<String, FxDataType>();
 
     static {
         TEST_PROPS.put("string", FxDataType.String1024);
         TEST_PROPS.put("text", FxDataType.Text);
         TEST_PROPS.put("html", FxDataType.HTML);
         TEST_PROPS.put("number", FxDataType.Number);
         TEST_PROPS.put("largeNumber", FxDataType.LargeNumber);
         TEST_PROPS.put("float", FxDataType.Float);
         TEST_PROPS.put("double", FxDataType.Double);
         TEST_PROPS.put("date", FxDataType.Date);
         TEST_PROPS.put("dateTime", FxDataType.DateTime);
         TEST_PROPS.put("boolean", FxDataType.Boolean);
         TEST_PROPS.put("binary", FxDataType.Binary);
         TEST_PROPS.put("reference", FxDataType.Reference);
         TEST_PROPS.put("selectOne", FxDataType.SelectOne);
         TEST_PROPS.put("selectMany", FxDataType.SelectMany);
         TEST_PROPS.put("dateRange", FxDataType.DateRange);
         TEST_PROPS.put("dateTimeRange", FxDataType.DateTimeRange);
     }
 
     private int testInstanceCount;  // number of instances for the SearchTest type
     private final List<Long> generatedNodeIds = new ArrayList<Long>();
 
     @BeforeClass
     public void setup() throws Exception {
         login(TestUsers.REGULAR);
         final List<FxPK> testPks = new SqlQueryBuilder().select("@pk").type(TEST_TYPE).getResult().collectColumn(1);
         testInstanceCount = testPks.size();
         assert testInstanceCount > 0 : "No instances of test type " + TEST_TYPE + " found.";
         // link test instances in tree
         for (FxPK pk: testPks) {
             generatedNodeIds.add(
                     getTreeEngine().save(FxTreeNodeEdit.createNew("test" + pk)
                             .setReference(pk).setName(RandomStringUtils.random(new Random().nextInt(1024), true, true)))
             );
         }
     }
 
     @AfterClass
     public void shutdown() throws Exception {
         for (long nodeId: generatedNodeIds) {
             getTreeEngine().remove(
                     FxTreeNodeEdit.createNew("").setId(nodeId).setMode(FxTreeMode.Edit),
                     false, true);
         }
         logout();
     }
 
     @Test
     public void simpleSelectTest() throws FxApplicationException {
         final FxResultSet result = new SqlQueryBuilder().select("caption", "comment").enterSub(QueryOperatorNode.Operator.AND)
                 .condition("caption", PropertyValueComparator.LIKE, "test caption%")
                 .condition("comment", PropertyValueComparator.LIKE, "folder comment%")
                 .closeSub().getResult();
         assert result.getRowCount() == 25 : "Expected to fetch 25 rows, got: " + result.getRowCount();
         assert result.getColumnIndex("caption") == 1 : "Unexpected column index for caption: " + result.getColumnIndex("caption");
         assert result.getColumnIndex("comment") == 2 : "Unexpected column index for comment: " + result.getColumnIndex("comment");
         for (int i = 1; i <= result.getRowCount(); i++) {
             assert result.getString(i, 1).startsWith("test caption") : "Unexpected column value: " + result.getString(i, 1);
             assert result.getString(i, 2).startsWith("folder comment") : "Unexpected column value: " + result.getString(i, 2);
         }
     }
 
     @Test
     public void simpleNestedQueryTest() throws FxApplicationException {
         final FxResultSet result = new SqlQueryBuilder().select("caption").andSub()
                 .condition("caption", PropertyValueComparator.LIKE, "test caption%")
                 .orSub().condition("comment", PropertyValueComparator.LIKE, "folder comment 1")
                 .condition("comment", PropertyValueComparator.LIKE, "folder comment 2").closeSub().getResult();
         assert result.getRowCount() == 2 : "Expected to fetch 2 rows, got: " + result.getRowCount();
         for (int i = 1; i <= 2; i++) {
             assert result.getString(i, 1).matches("test caption [12]") : "Unexpected column value: " + result.getString(i, 1);
         }
     }
 
     /**
      * Check if the SQL search for empty string properties works.
      *
      * @throws FxApplicationException if the search failed
      */
     @Test
     public void stringEmptyQuery() throws FxApplicationException {
         new SqlQueryBuilder().condition("caption", PropertyValueComparator.EMPTY, null).getResult();
         new SqlQueryBuilder().condition("caption", PropertyValueComparator.NOT_EMPTY, null).getResult();
         new SqlQueryBuilder().condition("caption", PropertyValueComparator.EMPTY, null)
                 .orSub().condition("caption", PropertyValueComparator.EMPTY, null).condition("caption", PropertyValueComparator.NOT_EMPTY, null)
                 .closeSub().getResult();
     }
 
     @Test
     public void selectUserTest() throws FxApplicationException {
         for (FxResultRow row : new SqlQueryBuilder().select("created_by", "created_by.username").getResult().getResultRows()) {
             final Account account = EJBLookup.getAccountEngine().load(((FxLargeNumber) row.getFxValue(1)).getDefaultTranslation());
             assertEquals(row.getFxValue(2).getDefaultTranslation(), account.getName());
         }
     }
 
     @Test
     public void filterByTypeTest() throws FxApplicationException {
         final FxResultSet result = new SqlQueryBuilder().select("typedef").filterType("FOLDER").getResult();
         assert result.getRowCount() > 0;
         final FxType folderType = CacheAdmin.getEnvironment().getType("FOLDER");
         for (FxResultRow row : result.getResultRows()) {
             assert folderType.getId() == ((FxLargeNumber) row.getValue(1)).getBestTranslation()
                     : "Unexpected result type: " + row.getValue(1) + ", expected: " + folderType.getId();
         }
     }
 
     @Test
     public void briefcaseQueryTest() throws FxApplicationException {
         // create briefcase
         final String selectFolders = new SqlQueryBuilder().type("FOLDER").getQuery();
         final FxSQLSearchParams params = new FxSQLSearchParams().saveResultInBriefcase("test briefcase", "description", (Long) null);
         final FxResultSet result = getSearchEngine().search(selectFolders, 0, Integer.MAX_VALUE, params);
         long bcId = result.getCreatedBriefcaseId();
         try {
             assert result.getRowCount() > 0;
             assert result.getCreatedBriefcaseId() != -1 : "Briefcase should have been created, but no ID returned.";
 
             // select briefcase
             final FxResultSet briefcase = new SqlQueryBuilder().filterBriefcase(result.getCreatedBriefcaseId()).getResult();
             assert briefcase.getTotalRowCount() == result.getTotalRowCount()
                     : "Briefcase returned " + briefcase.getTotalRowCount() + " rows, but getResult returned " + result.getTotalRowCount() + " rows.";
         } finally {
             EJBLookup.getBriefcaseEngine().remove(bcId);
         }
     }
 
 
     @Test
     public void queryBuilderBriefcaseTest() throws FxApplicationException {
         long briefcaseId = -1;
         try {
             final FxResultSet result = new SqlQueryBuilder().saveInBriefcase("SqlQueryBuilderTest").getResult();
             assert result.getRowCount() > 0;
             briefcaseId = result.getCreatedBriefcaseId();
             final Briefcase briefcase = EJBLookup.getBriefcaseEngine().load(briefcaseId);
             assert briefcase.getSize() == result.getTotalRowCount()
                     : "Invalid briefcase size: " + briefcase.getSize() + ", expected: " + result.getTotalRowCount(); 
         } finally {
             if (briefcaseId != -1) {
                 EJBLookup.getBriefcaseEngine().remove(briefcaseId);
             }
         }
     }
 
     @Test
     public void typeConditionTest() throws FxApplicationException {
         final FxResultSet result = new SqlQueryBuilder().select("typedef").type(FxType.CONTACTDATA).getResult();
         final FxType cdType = CacheAdmin.getEnvironment().getType(FxType.CONTACTDATA);
         assert result.getRowCount() > 0;
         for (FxResultRow row : result.getResultRows()) {
             assert ((FxLargeNumber) row.getFxValue(1)).getDefaultTranslation() == cdType.getId()
                     : "Unexpected type in result, expected " + cdType.getId() + ", was: " + row.getFxValue(1);
         }
     }
 
     /**
      * Generic tests on the SearchTest type.
      *
      * @param name     the base property name
      * @param dataType the datatype of the property
      * @throws com.flexive.shared.exceptions.FxApplicationException
      *          on search engine errors
      */
     @Test(dataProvider = "testProperties")
     public void genericSelectTest(String name, FxDataType dataType) throws FxApplicationException {
         final FxResultSet result = new SqlQueryBuilder().select(
                 getTestPropertyName(name)).type(TEST_TYPE).getResult();
         assert result.getRowCount() == testInstanceCount : "Expected all test instances to be returned, got "
                 + result.getRowCount() + " instead of " + testInstanceCount;
         final int idx = 1;
         for (FxResultRow row : result.getResultRows()) {
             assert dataType.getValueClass().isAssignableFrom(row.getFxValue(idx).getClass())
                     : "Invalid class returned for datatype " + dataType + ": " + row.getFxValue(idx).getClass() + " instead of " + dataType.getValueClass();
             assert row.getFxValue(idx).getXPathName() != null : "XPath was null";
             assert row.getFxValue(idx).getXPathName().equalsIgnoreCase(getTestPropertyName(name)) : "Invalid property name: " + row.getFxValue(idx).getXPathName() + ", expected: " + getTestPropertyName(name);
         }
     }
 
     /**
      * Tests the selection of null values.
      *
      * @param name     the base property name
      * @param dataType the datatype of the property
      * @throws com.flexive.shared.exceptions.FxApplicationException
      *          on search engine errors
      */
     @Test(dataProvider = "testProperties")
     public void genericSelectNullTest(String name, FxDataType dataType) throws FxApplicationException {
         final FxResultSet result = new SqlQueryBuilder().select(getTestPropertyName(name))
                 .condition("typedef", PropertyValueComparator.NE, CacheAdmin.getEnvironment().getType(TEST_TYPE).getId())
                 .getResult();
         assert result.getRowCount() > 0;
         final int idx = 1;
         for (FxResultRow row: result.getResultRows()) {
             assert dataType.getValueClass().isAssignableFrom(row.getFxValue(idx).getClass())
                     : "Invalid class returned for datatype " + dataType + ": " + row.getFxValue(idx).getClass() + " instead of " + dataType.getValueClass();
             assert row.getFxValue(idx).isEmpty() : "Value should be empty: " + row.getFxValue(idx); 
         }
     }
 
     @Test
     public void selectVirtualPropertiesTest() throws FxApplicationException {
         final FxResultSet result = new SqlQueryBuilder().select("@pk", "@path", "@node_position", "@permissions",
                 getTestPropertyName("string")).type(TEST_TYPE).getResult();
         final int idx = 5;
         for (FxResultRow row : result.getResultRows()) {
             assert getTestPropertyName("string").equalsIgnoreCase(row.getFxValue(idx).getXPathName())
                     : "Invalid property name from XPath: " + row.getFxValue(idx).getXPathName()
                     + ", expected: " + getTestPropertyName("string");
         }
     }
 
     @Test(dataProvider = "testProperties")
     public void orderByTest(String name, FxDataType dataType) throws FxApplicationException {
         final FxResultSet result = new SqlQueryBuilder().select("@pk", getTestPropertyName(name)).type(TEST_TYPE)
                 .orderBy(2, SortDirection.ASCENDING).getResult();
         assert result.getRowCount() > 0;
         assertAscendingOrder(result, 2);
     }
 
     @Test
     public void selectSystemPropertiesTest() throws FxApplicationException {
         final String[] props = { "id", "version", "typedef", "mandator", "acl", "created_by",
                 "created_at", "modified_by", "modified_at" };
         final FxResultSet result = new SqlQueryBuilder().select(props).maxRows(50).getResult();
         assert result.getRowCount() > 0;
         for (FxResultRow row : result.getResultRows()) {
             for (String property: props) {
                 final Object value = row.getValue(property);
                 assert value != null;
                 if (value instanceof FxValue) {
                     final FxValue fxValue = (FxValue) value;
                     assert StringUtils.isNotBlank(fxValue.getXPath()) && !"/".equals(fxValue.getXPath())
                             : "XPath not set in search result for system property " + property;
                 }
             }
         }
     }
 
     @Test
     public void selectPermissionsTest() throws FxApplicationException {
         final FxResultSet result = new SqlQueryBuilder().select("@pk", "@permissions").type(TEST_TYPE).getResult();
         assert result.getRowCount() > 0;
         for (FxResultRow row : result.getResultRows()) {
             final FxPK pk = row.getPk(1);
             final PermissionSet permissions = row.getPermissions(2);
             assert permissions.isMayRead();
             final PermissionSet contentPerms = getContentEngine().load(pk).getPermissions();
             assert contentPerms.equals(permissions) : "Permissions from search: " + permissions + ", content: " + contentPerms;
         }
     }
 
     @Test
     public void orderByResultPreferencesTest() throws FxApplicationException {
         setResultPreferences(SortDirection.ASCENDING);
         final FxResultSet result = new SqlQueryBuilder().select("@pk", getTestPropertyName("string")).filterType(TEST_TYPE).getResult();
         assertAscendingOrder(result, 2);
 
         setResultPreferences(SortDirection.DESCENDING);
         final FxResultSet descendingResult = new SqlQueryBuilder().select("@pk", getTestPropertyName("string")).filterType(TEST_TYPE).getResult();
         assertDescendingOrder(descendingResult, 2);
     }
 
     @Test
     public void explicitOrderByResultPreferencesTest() throws FxApplicationException {
         setResultPreferences(SortDirection.ASCENDING);
         // sort by the second column taken from the result preferences - needs "virtual" columns
         final FxResultSet result = getSearchEngine().search(
                 "SELECT @pk, id, @* FROM content CO FILTER type='" + TEST_TYPE + "' ORDER BY 4 DESC", 0, 9999, null);
         assert result.getRowCount() > 0;
         assertDescendingOrder(result, 4);
 
         try {
             getSearchEngine().search(
                     "SELECT co.@pk, co.id, co.@* FROM content CO FILTER co.type='" + TEST_TYPE + "' ORDER BY 5 DESC", 0, 9999, null);
             assert false : "Selected result preference column should not be present in result set";
         } catch (FxApplicationException e) {
             // pass
         }
 
         // also test using SqlQueryBuilder
         assertDescendingOrder(new SqlQueryBuilder().select("@pk", "id", "*")
                 .orderBy(4, SortDirection.DESCENDING)
                 .filterType(TEST_TYPE).getResult(), 4);
         try {
             new SqlQueryBuilder().select("@pk", "id", "@*").orderBy(5, SortDirection.ASCENDING).getResult();
             assert false : "Selected result preference column should not be present in result set";
         } catch (FxApplicationException e) {
             // pass - the exception was thrown by the search engine, so it's a FxApplicationException
             // instead of a FxRuntimeException
         }
     }
 
     private void setResultPreferences(SortDirection sortDirection) throws FxApplicationException {
         final ResultPreferencesEdit prefs = new ResultPreferencesEdit(Arrays.asList(
                 new ResultColumnInfo(Table.CONTENT, getTestPropertyName("string"), ""),
                 new ResultColumnInfo(Table.CONTENT, getTestPropertyName("number"), "")
         ), Arrays.asList(
                 new ResultOrderByInfo(Table.CONTENT, getTestPropertyName("string"), "", sortDirection)),
                 25, 100);
         EJBLookup.getResultPreferencesEngine().save(prefs, CacheAdmin.getEnvironment().getType(TEST_TYPE).getId(), ResultViewType.LIST, AdminResultLocations.DEFAULT);
     }
 
     private static List<FxPK> folderPks;
     private static synchronized List<FxPK> getFolderPks() throws FxApplicationException {
         if (folderPks == null) {
             folderPks = new SqlQueryBuilder().select("@pk").type("folder").getResult().collectColumn(1);
         }
         return folderPks;
     }
 
     /**
      * Tests all available value comparators for the given datatype. Note that no semantic
      * tests are performed, each comparator is executed with a random value.
      *
      * @param name     the base property name
      * @param dataType the datatype of the property
      * @throws com.flexive.shared.exceptions.FxApplicationException
      *          on search engine errors
      */
     @Test(dataProvider = "testProperties")
     public void genericConditionTest(String name, FxDataType dataType) throws FxApplicationException {
         final FxPropertyAssignment assignment = getTestPropertyAssignment(name);
         final Random random = new Random(0);
 
         // need to get some folder IDs for the reference property
         final List<FxPK> folderPks = getFolderPks();
 
         for (PropertyValueComparator comparator : PropertyValueComparator.getAvailable(dataType)) {
             for (String prefix : new String[]{
                     TEST_TYPE + "/",
                     TEST_TYPE + "/groupTop/",
                     TEST_TYPE + "/groupTop/groupNested/"
             }) {
                 final String assignmentName = prefix + getTestPropertyName(name);
                 try {
                     // submit a query with the given property/comparator combination
                     final FxValue value;
                     switch (dataType) {
                         case Reference:
                             value = new FxReference(new ReferencedContent(folderPks.get(random.nextInt(folderPks.size()))));
                             break;
                         case DateRange:
                             // a query is always performed against a particular date, but not a date range
                             value = new FxDate(new Date());
                             break;
                         case DateTimeRange:
                             value = new FxDateTime(new Date());
                             break;
                         default:
                             value = dataType.getRandomValue(random, assignment);
                     }
                     new SqlQueryBuilder().condition(assignmentName, comparator, value).getResult();
                     // no exception thrown, consider it a success
                 } catch (Exception e) {
                     assert false : "Failed to submit for property " + dataType + " with comparator " + comparator
                             + ":\n" + e.getMessage() + ", thrown at:\n"
                             + StringUtils.join(e.getStackTrace(), '\n');
                 }
             }
         }
     }
 
     /**
      * Tests date and datetime queries with functions, e.g. YEAR(dateprop)=2008
      *
      * @throws FxApplicationException   on errors
      */
     @Test
     public void dateConditionFunctionsTest() throws FxApplicationException {
         for (String name: new String[] { "date", "datetime" }) {
             testDateFunctionLT(name, 2020, DateFunction.YEAR, Calendar.YEAR);
             testDateFunctionLT(name, 5, DateFunction.MONTH, Calendar.MONTH);
             testDateFunctionLT(name, 15, DateFunction.DAY, Calendar.DAY_OF_MONTH);
         }
         testDateFunctionLT("datetime", 12, DateFunction.HOUR, Calendar.HOUR);
         testDateFunctionLT("datetime", 30, DateFunction.MINUTE, Calendar.MINUTE);
         testDateFunctionLT("datetime", 30, DateFunction.SECOND, Calendar.SECOND);
     }
 
     private void testDateFunctionLT(String name, int value, DateFunction dateFunction, int calendarField) throws FxApplicationException {
         final String assignmentName = TEST_TYPE + "/" + getTestPropertyName(name);
         final SqlQueryBuilder builder = getDateQuery(assignmentName, PropertyValueComparator.LT, value, dateFunction);
         final FxResultSet result = builder.getResult();
         assert result.getRowCount() > 0 : "Query returned no results:\n\n" + builder.getQuery();
         final Calendar cal = Calendar.getInstance();
         for (FxResultRow row: result.getResultRows()) {
             cal.setTime(row.getDate(1));
             assert cal.get(calendarField) < value : row.getDate(1) + " should be before " + value + ", data type = " + name;
         }
     }
 
     private SqlQueryBuilder getDateQuery(String datePropertyName, PropertyValueComparator comparator, int year, DateFunction dateFunction) throws FxApplicationException {
         return new SqlQueryBuilder().select(datePropertyName)
                 .condition(dateFunction.getSqlName() + "(" + datePropertyName + ")", comparator, year);
     }
 
     @Test
     public void dateSelectFunctionsTest() throws FxApplicationException {
         final int year = Calendar.getInstance().get(Calendar.YEAR);
         // get objects created this year
         FxResultSet result = new SqlQueryBuilder().select("year(created_at)").condition("year(created_at)", PropertyValueComparator.EQ, year).getResult();
         assert result.getRowCount() > 0;
         for (FxResultRow row: result.getResultRows()) {
             assert row.getInt(1) == year : "Expected " + year + ", got: " + row.getInt(1);
         }
 
         // select a date from the details table
         final String dateAssignmentName = TEST_TYPE + "/" + getTestPropertyName("date");
         result = new SqlQueryBuilder().select(dateAssignmentName, "year(" + dateAssignmentName + ")")
                 .condition(dateAssignmentName, PropertyValueComparator.NOT_EMPTY, null).getResult();
         assert result.getRowCount() > 0;
         final Calendar cal = Calendar.getInstance();
         for (FxResultRow row: result.getResultRows()) {
             cal.setTime(row.getDate(1));
             assert cal.get(Calendar.YEAR) == row.getInt(2) : "Expected year " + cal.get(Calendar.YEAR) + ", got: " + row.getInt(2);
         }
 
         // check if it is possible to select the same column twice, but with different functions
         result = new SqlQueryBuilder().select("year(created_at)", "month(created_at)").condition("year(created_at)", PropertyValueComparator.EQ, year).getResult();
         assert result.getRowCount() > 0;
         for (FxResultRow row: result.getResultRows()) {
             assert row.getInt(1) == year : "Expected " + year + ", got: " + row.getInt(1);
             assert row.getInt(2) != year && row.getInt(2) < 12 : "Invalid month value: " + row.getInt(2);
         }
     }
 
     /**
      * Tests relative comparators like &lt; and == for all datatypes that support them.
      *
      * @param name     the base property name
      * @param dataType the datatype of the property
      * @throws com.flexive.shared.exceptions.FxApplicationException
      *          on search engine errors
      */
     @Test(dataProvider = "testProperties")
     public void genericRelativeComparatorsTest(String name, FxDataType dataType) throws FxApplicationException {
         final String assignmentName = TEST_TYPE + "/" + getTestPropertyName(name);
 
         for (PropertyValueComparator comparator : PropertyValueComparator.getAvailable(dataType)) {
             if (!(comparator.equals(EQ) || comparator.equals(PropertyValueComparator.GE)
                || comparator.equals(PropertyValueComparator.GT) || comparator.equals(PropertyValueComparator.LE)
                || comparator.equals(PropertyValueComparator.LT))) {
                 continue;
             }
             final FxValue value = getTestValue(name, comparator);
             final SqlQueryBuilder builder = new SqlQueryBuilder().select("@pk", assignmentName).condition(assignmentName, comparator, value);
             final FxResultSet result = builder.getResult();
             assert result.getRowCount() > 0 : "Cannot test on empty result sets, query=\n" + builder.getQuery();
             for (FxResultRow row: result.getResultRows()) {
                 final FxValue rowValue = row.getFxValue(2);
                 switch(comparator) {
                     case EQ:
                         assert rowValue.compareTo(value) == 0
                                 : "Result value " + rowValue + " is not equal to " + value + " (compareTo = "
                                 + rowValue.compareTo(value) + ")\n\nQuery:" + builder.getQuery();
                         assert rowValue.getBestTranslation().equals(value.getBestTranslation())
                                 : "Result value " + rowValue + " is not equal to " + value + "\n\nQuery:" + builder.getQuery();
                         break;
                     case LT:
                         assert rowValue.compareTo(value) < 0 : "Result value " + rowValue + " is not less than " + value + "\n\nQuery:" + builder.getQuery();
                         break;
                     case LE:
                         assert rowValue.compareTo(value) <= 0 : "Result value " + rowValue + " is not less or equal to " + value + "\n\nQuery:" + builder.getQuery();
                         break;
                     case GT:
                         assert rowValue.compareTo(value) > 0 : "Result value " + rowValue + " is not greater than " + value + "\n\nQuery:" + builder.getQuery();
                         break;
                     case GE:
                         assert rowValue.compareTo(value) >= 0 : "Result value " + rowValue + " is not greater or equal to " + value + "\n\nQuery:" + builder.getQuery();
                         break;
                     default:
                         assert false : "Invalid comparator: " + comparator + "\n\nQuery:" + builder.getQuery();
                 }
             }
         }
     }
 
     /**
      * Finds a FxValue of the test instances that matches some of the result rows
      * for the given comparator, but not all or none.
      *
      * @param name  the test property name
      * @param comparator    the comparator
      * @return  a value that matches some rows
      * @throws FxApplicationException   on search engine errors
      */
     private FxValue getTestValue(String name, PropertyValueComparator comparator) throws FxApplicationException {
         final FxResultSet result = new SqlQueryBuilder().select(getTestPropertyName(name)).type(TEST_TYPE).getResult();
         final List<FxValue> values = result.collectColumn(1);
         assert values.size() == testInstanceCount : "Expected " + testInstanceCount + " rows, got: " + values.size();
         for (FxValue value: values) {
             if (value == null || value.isEmpty()) {
                 continue;
             }
             int match = 0;  // number of matched values for the given comparator
             int count = 0;  // number of values checked so far
             for (FxValue value2: values) {
                 if (value2 == null || value2.isEmpty()) {
                     continue;
                 }
                 count++;
                 switch(comparator) {
                     case EQ:
                         if (value.getBestTranslation().equals(value2.getBestTranslation())) {
                             match++;
                         }
                         break;
                     case LT:
                         if (value2.compareTo(value) < 0) {
                             match++;
                         }
                         break;
                     case LE:
                         if (value2.compareTo(value) <= 0) {
                             match++;
                         }
                         break;
                     case GT:
                         if (value2.compareTo(value) > 0) {
                             match++;
                         }
                         break;
                     case GE:
                         if (value2.compareTo(value) >= 0) {
                             match++;
                         }
                         break;
                     default:
                         assert false : "Cannot check relative ordering for comparator " + comparator;
                 }
                 if (match > 0 && count > match) {
                     // this value is matched by _some_ other row values, so it's suitable as test input
                     if (value instanceof FxDateRange) {
                         // daterange checks are performed against an actual date, not another range
                         return new FxDate(((FxDateRange) value).getBestTranslation().getLower());
                     } else if (value instanceof FxDateTimeRange) {
                         // see above
                         return new FxDateTime(((FxDateTimeRange) value).getBestTranslation().getLower());
                     }
                     return value;
                 }
             }
         }
         throw new IllegalArgumentException("Failed to find a suitable test value for property " + getTestPropertyName(name)
                 + " and comparator " + comparator);
     }
 
     @Test
     public void aclSelectorTest() throws FxApplicationException {
         final FxResultSet result = new SqlQueryBuilder().select("@pk", "acl", "acl.label", "acl.name",
                 "acl.mandator", "acl.description", "acl.cat_type", "acl.color", "acl.created_by", "acl.created_at",
                 "acl.modified_by", "acl.modified_at").type(TEST_TYPE).getResult();
         assert result.getRowCount() > 0;
         for (FxResultRow row: result.getResultRows()) {
             final ACL acl = CacheAdmin.getEnvironment().getACL(row.getLong("acl"));
             final FxContent content = getContentEngine().load(row.getPk(1));
             assert content.getAclId() == acl.getId()
                     : "Invalid ACL for instance " + row.getPk(1) + ": " + acl.getId()
                     + ", content engine returned " + content.getAclId();
 
             // check label
             assert acl.getLabel().getBestTranslation().equals(row.getFxValue("acl.label").getBestTranslation())
                     : "Invalid ACL label '" + row.getValue(3) + "', expected: '" + acl.getLabel() + "'";
 
             // check fields selected directly from the ACL table
             assertEquals(row.getString("acl.name"), (Object) acl.getName(), "Invalid value for field: name");
             assertEquals(row.getLong("acl.mandator"), (Object) acl.getMandatorId(), "Invalid value for field: mandator");
             assertEquals(row.getString("acl.description"), (Object) acl.getDescription(), "Invalid value for field: description");
             assertEquals(row.getInt("acl.cat_type"), (Object) acl.getCategory().getId(), "Invalid value for field: category");
             assertEquals(row.getString("acl.color"), (Object) acl.getColor(), "Invalid value for field: color");
             checkLifecycleInfo(row, "acl", acl.getLifeCycleInfo());
         }
     }
 
     @Test
     public void stepSelectorTest() throws FxApplicationException {
         final FxResultSet result = new SqlQueryBuilder().select("@pk", "step", "step.label", "step.id", "step.stepdef",
                 "step.workflow", "step.acl").getResult();
         assert result.getRowCount() > 0;
         for (FxResultRow row: result.getResultRows()) {
             final Step step = CacheAdmin.getEnvironment().getStep(row.getLong("step"));
             final StepDefinition definition = CacheAdmin.getEnvironment().getStepDefinition(step.getStepDefinitionId());
             assert definition.getLabel().getBestTranslation().equals(row.getFxValue("step.label").getBestTranslation())
                     : "Invalid step label '" + row.getValue(3) + "', expected: '" + definition.getLabel() + "'";
             try {
                 final FxContent content = getContentEngine().load(row.getPk(1));
                 assert content.getStepId() == step.getId()
                         : "Invalid step for instance " + row.getPk(1) + ": " + step.getId()
                         + ", content engine returned " + content.getStepId();
             } catch (FxNoAccessException e) {
                 assert false : "Content engine denied read access to instance " + row.getPk(1) + " that was returned by search."; 
             }
 
             // check fields selected from the ACL table
             assertEquals(row.getLong("step.id"), step.getId(), "Invalid value for field: id");
             assertEquals(row.getLong("step.stepdef"), step.getStepDefinitionId(), "Invalid value for field: stepdef");
             assertEquals(row.getLong("step.workflow"), step.getWorkflowId(), "Invalid value for field: workflow");
             assertEquals(row.getLong("step.acl"), step.getAclId(), "Invalid value for field: acl");
         }
     }
 
     @Test
     public void contactDataSelectTest() throws FxApplicationException {
         // contact data is an example of a content with extended private permissions and no permissions for other users
         final FxResultSet result = new SqlQueryBuilder().select("@pk", "@permissions").type(FxType.CONTACTDATA).getResult();
         for (FxResultRow row: result.getResultRows()) {
             try {
                 final FxContent content = getContentEngine().load(row.getPk(1));
                 assert content.getPermissions().equals(row.getPermissions("@permissions"))
                         : "Content perm: " + content.getPermissions() + ", search perm: " + row.getPermissions(2);
             } catch (FxNoAccessException e) {
                 assert false : "Search returned contact data #" + row.getPk(1)
                         + ", but content engine disallows access: " + e.getMessage();
             }
         }
     }
 
     /**
      * Executes a query without conditions and checks if it returns only instances
      * the user can actually read (a select without conditions is an optimized case of the
      * search that must implement the same security constraints as a regular query).
      *
      * @throws FxApplicationException   on errors
      */
     @Test
     public void selectAllPermissionsTest() throws FxApplicationException {
         final FxResultSet result = getSearchEngine().search("SELECT @pk", 0, 999999, null);
         assert result.getRowCount() > 0;
         for (FxResultRow row: result.getResultRows()) {
             try {
                 getContentEngine().load(row.getPk(1));
             } catch (FxNoAccessException e) {
                 assert false : "Content engine denied read access to instance " + row.getPk(1) + " that was returned by search."; 
             }
         }
     }
 
     @Test
     public void mandatorSelectorTest() throws FxApplicationException {
         final FxResultSet result = new SqlQueryBuilder().select("@pk", "mandator", "mandator.id",
                 "mandator.metadata", "mandator.is_active", "mandator.created_by", "mandator.created_at",
                 "mandator.modified_by", "mandator.modified_at").getResult();
         assert result.getRowCount() > 0;
         for (FxResultRow row: result.getResultRows()) {
             final Mandator mandator = CacheAdmin.getEnvironment().getMandator(row.getLong("mandator"));
             final FxContent content = getContentEngine().load(row.getPk(1));
 
             assertEquals(mandator.getId(), content.getMandatorId(), "Search returned different mandator than content engine");
             assertEquals(row.getLong("mandator.id"), mandator.getId(), "Invalid value for field: id");
             if( row.getValue("mandator.metadata") != null ) {
                 long mand = row.getLong("mandator.metadata");
                 if (!(mand == 0 || mand == -1))
                     Assert.fail("Invalid mandator: " + mand + "! Expected 0 or -1 (System default or test division)");
             }
             assertEquals(row.getLong("mandator.is_active"), mandator.isActive() ? 1 : 0, "Invalid value for field: is_active");
             checkLifecycleInfo(row, "mandator", mandator.getLifeCycleInfo());
         }
     }
 
     @Test
     public void accountSelectorTest() throws FxApplicationException {
         for (String name : new String[] { "created_by", "modified_by" }) {
             final FxResultSet result = new SqlQueryBuilder().select("@pk", name, name + ".mandator",
                     name + ".username", name + ".password", name + ".email", name + ".contact_id",
                     name + ".valid_from", name + ".valid_to", name + ".description", name + ".created_by",
                     name + ".created_at", name + ".modified_by", name + ".modified_at",
                     name + ".is_active", name + ".is_validated", name + ".lang", name + ".login_name",
                     name + ".allow_multilogin", name + ".default_node").maxRows(10).getResult();
             assert result.getRowCount() == 10 : "Expected 10 result rows";
             for (FxResultRow row: result.getResultRows()) {
                 final Account account = EJBLookup.getAccountEngine().load(row.getLong(name));
                 assertEquals(row.getString(name + ".username"), account.getName(), "Invalid value for field: username");
                 assertEquals(row.getString(name + ".login_name"), account.getLoginName(), "Invalid value for field: login_name");
                 assertEquals(row.getString(name + ".email"), account.getEmail(), "Invalid value for field: email");
                 assertEquals(row.getLong(name + ".contact_id"), account.getContactDataId(), "Invalid value for field: contact_id");
                 assertEquals(row.getLong(name + ".is_active"), account.isActive() ? 1 : 0, "Invalid value for field: is_active");
                 assertEquals(row.getLong(name + ".is_validated"), account.isValidated() ? 1 : 0, "Invalid value for field: is_validated");
                 assertEquals(row.getLong(name + ".allow_multilogin"), account.isAllowMultiLogin() ? 1 : 0, "Invalid value for field: allow_multilogin");
                 assertEquals(row.getLong(name + ".lang"), account.getLanguage().getId(), "Invalid value for field: lang");
                 // default_node is not supported yet
                 //assertEquals(row.getLong(name + ".default_node"), account.getDefaultNode(), "Invalid value for field: default_node");
                 checkLifecycleInfo(row, name, account.getLifeCycleInfo());
             }
         }
     }
 
     private void checkLifecycleInfo(FxResultRow row, String baseName, LifeCycleInfo lifeCycleInfo) {
         assertEquals(row.getLong(baseName + ".created_by"), lifeCycleInfo.getCreatorId(), "Invalid value for field: created_by");
         assertEquals(row.getDate(baseName + ".created_at").getTime(), lifeCycleInfo.getCreationTime(), "Invalid value for field: id");
         assertEquals(row.getLong(baseName + ".modified_by"), lifeCycleInfo.getModificatorId(), "Invalid value for field: modified_by");
         assertEquals(row.getDate(baseName + ".modified_at").getTime(), lifeCycleInfo.getModificationTime(), "Invalid value for field: modified_at");
     }
 
     @Test
     public void treeSelectorTest() throws FxApplicationException {
         final FxResultSet result = new SqlQueryBuilder().select("@pk", "@path").isChild(FxTreeNode.ROOT_NODE).getResult();
         assert result.getRowCount() > 0;
         for (FxResultRow row: result.getResultRows()) {
             final List<FxPaths.Path> paths = row.getPaths(2);
             assert paths.size() > 0 : "Returned no path information for content " + row.getPk(1);
             for (FxPaths.Path path: paths) {
                 assert path.getItems().size() > 0 : "Empty path returned";
                 final FxPaths.Item leaf = path.getItems().get(path.getItems().size() - 1);
                 assert leaf.getReferenceId() == row.getPk(1).getId() : "Expected reference ID " + row.getPk(1)
                         + ", got: " + leaf.getReferenceId() + " (nodeId=" + leaf.getNodeId() + ")";
 
                 final String treePath = StringUtils.join(getTreeEngine().getLabels(FxTreeMode.Edit, leaf.getNodeId()), '/');
                 assert treePath.equals(path.getCaption()) : "Unexpected tree path '" + path.getCaption()
                         + "', expected: '" + treePath + "'";
 
             }
         }
         // test selection via node path
         final FxResultSet pathResult = new SqlQueryBuilder().select("@pk", "@path").isChild("/").getResult();
         assert pathResult.getRowCount() == result.getRowCount() : "Path select returned " + pathResult.getRowCount()
                 + " rows, select by ID returned " + result.getRowCount() + " rows.";
         // query a leaf node
         new SqlQueryBuilder().select("@pk").isChild(getTreeEngine().getPathById(FxTreeMode.Edit, pathResult.getResultRow(0).getPk(1).getId()));
     }
 
     @Test
     public void treeConditionTest_FX263() throws FxApplicationException {
         // Bug FX-263: all versions of a content are returned in the edit(=max version) tree
 
         // find a suitable test content instance
         FxResultSet result = new SqlQueryBuilder().select("@pk").condition("version", PropertyValueComparator.GT, 1).getResult();
         assert result.getRowCount() > 0;
         final FxPK pk = result.<FxPK>collectColumn(1).get(0);
         assert pk.getVersion() > 1;
 
         // create a test folder
         long folderNodeId = -1;
         try {
             folderNodeId = getTreeEngine().save(FxTreeNodeEdit.createNew("treeConditionTest_FX263"));
             // attach child content
             getTreeEngine().save(FxTreeNodeEdit.createNew("").setReference(pk).setParentNodeId(folderNodeId));
 
             // find children in default (=maximum) version filter mode, should return 1 row in maximum version
             result = new SqlQueryBuilder().select("@pk").isChild(folderNodeId).getResult();
             assert result.getRowCount() == 1 : "Expected one child, got: " + result.getRowCount()
                     + " (" + result.collectColumn(1) + ")";
             // should return maximum version
             assert result.getResultRow(0).getPk(1).getVersion() == pk.getVersion();
         } finally {
             if (folderNodeId != -1) {
                 getTreeEngine().remove(FxTreeNodeEdit.createNew("").setId(folderNodeId), true, true);
             }
         }
     }
 
     @Test
     public void fulltextSearchTest() throws FxApplicationException {
         final FxResultSet result = new SqlQueryBuilder().select("@pk", getTestPropertyName("string")).type(TEST_TYPE).maxRows(1).getResult();
         assert result.getRowCount() == 1 : "Expected only one result, got: " + result.getRowCount();
 
         // perform a fulltext query against the first word
         final FxPK pk = result.getResultRow(0).getPk(1);
         final String[] words = StringUtils.split(((FxString) result.getResultRow(0).getFxValue(2)).getBestTranslation(), ' ');
         assert words.length > 0;
         assert words[0].length() > 0 : "Null length word: " + words[0];
         final FxResultSet ftresult = new SqlQueryBuilder().select("@pk").fulltext(words[0]).getResult();
         assert ftresult.getRowCount() > 0 : "Expected at least one result for fulltext query '" + words[0] + "'";
         assert ftresult.collectColumn(1).contains(pk) : "Didn't find pk " + pk + " in result, got: " + ftresult.collectColumn(1);
     }
 
     @Test
     public void versionFilterTest() throws FxApplicationException {
         final List<FxPK> allVersions = getPksForVersion(VersionFilter.ALL);
         final List<FxPK> liveVersions = getPksForVersion(VersionFilter.LIVE);
         final List<FxPK> maxVersions = getPksForVersion(VersionFilter.MAX);
         assert allVersions.size() > 0 : "All versions result must not be empty";
         assert liveVersions.size() > 0 : "Live versions result must not be empty";
         assert maxVersions.size() > 0 : "Max versions result must not be empty";
         assert allVersions.size() > liveVersions.size() : "Expected more than only live versions";
         assert allVersions.size() > maxVersions.size() : "Expected more than only max versions";
         assert !CollectionUtils.isEqualCollection(liveVersions, maxVersions) : "Expected different results for max and live version filter";
         for (FxPK pk: liveVersions) {
             final FxContent content = getContentEngine().load(pk);
             assert content.isLiveVersion() : "Expected live version for " + pk;
         }
         for (FxPK pk: maxVersions) {
             final FxContent content = getContentEngine().load(pk);
             assert content.isMaxVersion() : "Expected max version for " + pk;
             assert content.getVersion() == 1 || !content.isLiveVersion();
         }
     }
 
     @Test
     public void lastContentChangeTest() throws FxApplicationException {
         final long lastContentChange = getSearchEngine().getLastContentChange(false);
         assert lastContentChange > 0;
         final FxContent content = getContentEngine().initialize(TEST_TYPE);
         content.setAclId(TestUsers.getInstanceAcl().getId());
         content.setValue("/" + getTestPropertyName("string"), new FxString(false, "lastContentChangeTest"));
         FxPK pk = null;
         try {
             assert getSearchEngine().getLastContentChange(false) == lastContentChange
                     : "Didn't touch contents, but lastContentChange timestamp was increased";
             pk = getContentEngine().save(content);
             assert getSearchEngine().getLastContentChange(false) > lastContentChange
                     : "Saved content, but lastContentChange timestamp was not increased: "
                     + getSearchEngine().getLastContentChange(false);
         } finally {
             removePk(pk);
         }
     }
 
     @Test
     public void lastContentChangeTreeTest() throws FxApplicationException {
         final long lastContentChange = getSearchEngine().getLastContentChange(false);
         assert lastContentChange > 0;
         final long nodeId = getTreeEngine().save(FxTreeNodeEdit.createNew("lastContentChangeTreeTest"));
         try {
             final long editContentChange = getSearchEngine().getLastContentChange(false);
             assert editContentChange > lastContentChange
                     : "Saved content, but lastContentChange timestamp was not increased: " + editContentChange;
             getTreeEngine().activate(FxTreeMode.Edit, nodeId, false);
             assert getSearchEngine().getLastContentChange(true) > editContentChange
                     : "Activated content, but live mode lastContentChange timestamp was not increased: "
                         + getSearchEngine().getLastContentChange(true);
             assert getSearchEngine().getLastContentChange(false) == editContentChange
                     : "Edit tree didn't change, but lastContentChange timestamp was updated";
         } finally {
             FxContext.get().runAsSystem();
             try {
                 try {
                     getTreeEngine().remove(getTreeEngine().getNode(FxTreeMode.Edit, nodeId), true, false);
                 } catch (FxApplicationException e) {
                     // pass
                 }
                 try {
                     getTreeEngine().remove(getTreeEngine().getNode(FxTreeMode.Live, nodeId), true, false);
                 } catch (FxApplicationException e) {
                     // pass
                 }
             } finally {
                 FxContext.get().stopRunAsSystem();
             }
         }
     }
 
     @Test
     public void inactiveMandatorResultTest() throws FxApplicationException {
         long mandatorId = -1;
         FxPK pk = null;
         try {
             FxContext.get().runAsSystem();
             mandatorId = getMandatorEngine().create("inactiveMandatorResultTest", true);
             final FxContent content = getContentEngine().initialize(getTestTypeId(), mandatorId, -1, -1, -1);
             content.setValue("/" + getTestPropertyName("string"), new FxString(false, "test value"));
             pk = getContentEngine().save(content);
 
             // content should be retrievable
             assert new SqlQueryBuilder().condition("id", EQ, pk.getId()).getResult().getRowCount() > 0
                     : "Test value from active mandator not found";
 
             getMandatorEngine().deactivate(mandatorId);
             // content should be removed from result
             assert new SqlQueryBuilder().condition("id", EQ, pk.getId()).getResult().getRowCount() == 0
                     : "Content from deactivated mandators should not be retrievable";
             try {
                 getContentEngine().load(pk);
                 assert false : "ContentEngine returned content from deactivated mandator.";
             } catch (FxApplicationException e) {
                 // pass
             }
         } finally {
             try {
                 if (mandatorId != -1) {
                     getMandatorEngine().activate(mandatorId);   // active before removing content
                 }
                 removePk(pk);
                 if (mandatorId != -1) {
                     getMandatorEngine().remove(mandatorId);
                 }
             } finally {
                 FxContext.get().stopRunAsSystem();
             }
         }
     }
 
     @Test
     public void stringEscapeTest() throws FxApplicationException {
         FxPK pk = null;
         try {
             final FxContent content = getContentEngine().initialize(TEST_TYPE);
             content.setValue("/" + getTestPropertyName("string"), new FxString(false, "te'st"));
             pk = getContentEngine().save(content);
             final FxResultSet result = getSearchEngine().search("SELECT id WHERE "
                     + getTestPropertyName("string") + " = 'te''st'", 0, 10, null);
             assert result.getRowCount() == 1 : "Escaped string property not returned";
             assert result.getResultRow(0).getLong(1) == pk.getId();
         } finally {
             removePk(pk);
         }
     }
 
     private void removePk(FxPK pk) throws FxApplicationException {
         if (pk != null) {
             getContentEngine().remove(pk);
         }
     }
 
     @Test
     public void dateSearchTest() throws FxApplicationException, ParseException {
         FxPK pk = null;
         try {
             final String dateProperty = getTestPropertyName("date");
             final String dateTimeProperty = getTestPropertyName("dateTime");
 
             final SimpleDateFormat dateFormat = FxFormatUtils.getDateFormat();
             final SimpleDateFormat dateTimeFormat = FxFormatUtils.getDateTimeFormat();
 
             final FxContent content = getContentEngine().initialize(TEST_TYPE);
             content.setValue("/" + dateProperty, new FxDate(false,
                     dateFormat.parse("2008-03-18")));
             content.setValue("/" + dateTimeProperty, new FxDateTime(false,
                     dateTimeFormat.parse("2008-03-18 15:43:25.123")));
             pk = getContentEngine().save(content);
 
             // date query
             assertExactPkMatch(pk, new SqlQueryBuilder().select("@pk").condition(dateProperty, EQ, "2008-03-18").condition("id", EQ, pk.getId()).getResult().<FxPK>collectColumn(1));
             assertExactPkMatch(pk, new SqlQueryBuilder().select("@pk").condition(dateProperty, PropertyValueComparator.LE, "2008-03-18").condition("id", EQ, pk.getId()).getResult().<FxPK>collectColumn(1));
             assertExactPkMatch(pk, new SqlQueryBuilder().select("@pk").condition(dateProperty, PropertyValueComparator.GE, "2008-03-18").condition("id", EQ, pk.getId()).getResult().<FxPK>collectColumn(1));
 
             assert new SqlQueryBuilder().select("@pk").condition(dateProperty, PropertyValueComparator.LT, "2008-03-18").condition("id", EQ, pk.getId()).getResult().getRowCount() == 0
                     : "No rows should be returned because date condition doesn't match.";
             assert new SqlQueryBuilder().select("@pk").condition(dateProperty, PropertyValueComparator.GT, "2008-03-18").condition("id", EQ, pk.getId()).getResult().getRowCount() == 0
                     : "No rows should be returned because date condition doesn't match.";
             assert new SqlQueryBuilder().select("@pk").condition(dateProperty, PropertyValueComparator.NE, "2008-03-18").condition("id", EQ, pk.getId()).getResult().getRowCount() == 0 
                     : "No rows should be returned because date condition doesn't match.";
 
             // datetime query
             assertExactPkMatch(pk, new SqlQueryBuilder().select("@pk").condition(dateTimeProperty, EQ, "2008-03-18 15:43:25.123").condition("id", EQ, pk.getId()).getResult().<FxPK>collectColumn(1));
             assertExactPkMatch(pk, new SqlQueryBuilder().select("@pk").condition(dateTimeProperty, PropertyValueComparator.LE, "2008-03-18 15:43:25.123").condition("id", EQ, pk.getId()).getResult().<FxPK>collectColumn(1));
             assertExactPkMatch(pk, new SqlQueryBuilder().select("@pk").condition(dateTimeProperty, PropertyValueComparator.GE, "2008-03-18 15:43:25.123").condition("id", EQ, pk.getId()).getResult().<FxPK>collectColumn(1));
             assert new SqlQueryBuilder().select("@pk").condition(dateTimeProperty, PropertyValueComparator.LT, "2008-03-18 15:43:25.123").condition("id", EQ, pk.getId()).getResult().getRowCount() == 0
                 : "No rows should be returned because date condition doesn't match.";
             assert new SqlQueryBuilder().select("@pk").condition(dateTimeProperty, PropertyValueComparator.GT, "2008-03-18 15:43:25.123").condition("id", EQ, pk.getId()).getResult().getRowCount() == 0
                 : "No rows should be returned because date condition doesn't match.";
             assert new SqlQueryBuilder().select("@pk").condition(dateTimeProperty, PropertyValueComparator.NE, "2008-03-18 15:43:25.123").condition("id", EQ, pk.getId()).getResult().getRowCount() == 0 
                 : "No rows should be returned because date condition doesn't match.";
         } finally {
             removePk(pk);
         }
     }
 
     @Test
     public void createdAtTest() throws FxApplicationException {
         final FxContent content = getContentEngine().load(getFolderPks().get(0));
         final Date createdAt = new Date(content.getLifeCycleInfo().getCreationTime());
         assertExactPkMatch(content.getPk(), new SqlQueryBuilder().select("@pk").condition("created_at", EQ, new FxDateTime(false, createdAt)).condition("id", EQ, content.getPk().getId()).getResult().<FxPK>collectColumn(1));
         assertExactPkMatch(content.getPk(), new SqlQueryBuilder().select("@pk").condition("created_at", PropertyValueComparator.LE, new FxDateTime(false, createdAt)).condition("id", EQ, content.getPk().getId()).getResult().<FxPK>collectColumn(1));
         assertExactPkMatch(content.getPk(), new SqlQueryBuilder().select("@pk").condition("created_at", PropertyValueComparator.GE, new FxDateTime(false, createdAt)).condition("id", EQ, content.getPk().getId()).getResult().<FxPK>collectColumn(1));
         assert new SqlQueryBuilder().select("@pk").condition("created_at", PropertyValueComparator.LT, new FxDateTime(false, createdAt)).condition("id", EQ, content.getPk().getId()).getResult().getRowCount() == 0;
         assert new SqlQueryBuilder().select("@pk").condition("created_at", PropertyValueComparator.GT, new FxDateTime(false, createdAt)).condition("id", EQ, content.getPk().getId()).getResult().getRowCount() == 0;
     }
 
     @Test
     public void assignmentQueryTest() throws FxApplicationException {
         final FxPropertyAssignment assignment = getTestPropertyAssignment("string");
         final FxResultSet result = getSearchEngine().search("SELECT #" + assignment.getId() + " WHERE #" + assignment.getId() + " IS NOT NULL",
                 0, 10, null);
         assert result.getRowCount() > 0;
     }
 
     @Test
     public void assignmentBuilderQueryTest() throws FxApplicationException {
         final FxPropertyAssignment assignment = getTestPropertyAssignment("string");
         final String dateProperty = getTestPropertyName("date");
         final FxResultSet result = new SqlQueryBuilder().select("@pk")
                 .condition(assignment, PropertyValueComparator.NOT_EMPTY, null)
                 .condition("YEAR(" + dateProperty + ")", PropertyValueComparator.GT, 0)
                 .condition(TEST_TYPE + "/grouptop/" + dateProperty, PropertyValueComparator.NOT_EMPTY, null)
                 .condition("YEAR(" + TEST_TYPE + "/grouptop/" + dateProperty + ")", PropertyValueComparator.GT, 0)
                 .getResult();
         assert result.getRowCount() > 0;
     }
 
 
     @Test
     public void commentTest() throws FxApplicationException {
         final FxPropertyAssignment assignment = getTestPropertyAssignment("string");
         final FxResultSet result = getSearchEngine().search("SELECT /* field1 */ id, * \n" +
                 "WHERE -- line comment\n" +
                 "/* comment */ (/* comment*/ id != 0) or (/*comment*/#" + assignment.getId() + " IS NULL)", 0, 10, null);
         assert result.getRowCount() > 0;
 
     }
 
     @Test
     public void tableAliasTest() throws FxApplicationException {
         final FxResultSet result = getSearchEngine().search("SELECT tbl.@pk, tbl.caption\n" +
                 "FROM content tbl\n" +
                 "WHERE tbl.id > 0", 0, 10, null);
         assert result.getRowCount() > 0;
     }
 
     @Test
     public void selectEmptyPathTest() throws FxApplicationException {
         // select path for all items, will include empty paths like contact data
         final FxResultSet result = getSearchEngine().search("SELECT @path", 0, 99999, null);
         assert result.getRowCount() > 0;
     }
 
     @Test
     public void orderByAssignmentTest() throws FxApplicationException {
         final FxPropertyAssignment assignment = getTestPropertyAssignment("number");
         final FxResultSet result = new SqlQueryBuilder().select("id", assignment.getXPath())
                 .orderBy(assignment.getXPath(), SortDirection.ASCENDING).getResult();
         assert result.getRowCount() > 0;
     }
 
     @Test
     public void searchForStepTest() throws FxApplicationException {
         final FxResultSet result = getSearchEngine().search("SELECT step WHERE step=" + StepDefinition.EDIT_STEP_ID, 0, 10, null);
         assert result.getRowCount() > 0 : "At least one instance expected to be in the 'edit' step";
     }
 
     @Test
     public void searchPropertyPermissionDeleteTest() throws FxApplicationException {
         // create a type with a property that cannot be deleted by other users
         FxContext.get().runAsSystem();
         final long typeId;
         final long aclId;
         try {
             typeId = getTypeEngine().save(FxTypeEdit.createNew("prop_delete_test")
                     .setUseInstancePermissions(false)
                     .setUsePropertyPermissions(true)
                     .setUseStepPermissions(false)
                     .setUseTypePermissions(false));
             aclId = getAclEngine().create("prop_no_delete", new FxString(""),
                     TestUsers.getTestMandator(), "#000000", "property description", ACL.Category.STRUCTURE);
             getAclEngine().assign(aclId, TestUsers.REGULAR.getUserGroupId(),
                     Permission.CREATE, Permission.EDIT, Permission.READ);
             getAssignmentEngine().createProperty(typeId,
                     FxPropertyEdit.createNew(
                             "prop_no_delete", new FxString(""), new FxString(""),
                             FxMultiplicity.MULT_0_1,
                             CacheAdmin.getEnvironment().getACL(aclId),
                             FxDataType.String1024
                     ),
                     "/"
             );
         } finally {
             FxContext.get().stopRunAsSystem();
         }
         FxPK pk = null;
         try {
             // create a test content instance
             FxContent content = getContentEngine().initialize(typeId);
             // don't set the value, removal should be ok
             pk = getContentEngine().save(content);
             // select permissions, delete perm should be set
             assert new SqlQueryBuilder().select("@permissions")
                     .condition("id", PropertyValueComparator.EQ, pk.getId())
                     .getResult()
                     .<PermissionSet>collectColumn(1)
                     .get(0)
                     .isMayDelete()
                     : "Expected delete permission";
             getContentEngine().remove(pk);
 
             content = getContentEngine().initialize(typeId);
             content.setValue("/prop_no_delete", new FxString(false, "test"));
             pk = getContentEngine().save(content);
             // removal should not work now
             try {
                 getContentEngine().remove(pk);
                 assert false : "Content could be removed although delete property permission not set";
             } catch (FxApplicationException e) {
                 // pass
             }
 
             // select this content instance and see if delete perm is set
 
             // For performance reasons, property delete permissions are not used for the
             // instance permission set. Thus this assert is expected to fail. 
 
             /*assert !*/new SqlQueryBuilder().select("@permissions")
                     .condition("id", PropertyValueComparator.EQ, pk.getId())
                     .getResult()
                     .<PermissionSet>collectColumn(1)
                     .get(0)
                     .isMayDelete();/*
                     : "Delete permission should not be set because of property permissions";*/
         } finally {
             FxContext.get().runAsSystem();
             try {
                 if (pk != null) {
                     getContentEngine().remove(pk);
                 }
                 getTypeEngine().remove(typeId);
                 getAclEngine().remove(aclId);
             } finally {
                 FxContext.get().stopRunAsSystem();
             }
         }
     }
 
     @Test
     public void searchLanguageFallbackTest_FX260() throws FxApplicationException {
         final int maxRows = 20;
         final UserTicket ticket = FxContext.getUserTicket();
         final FxLanguage oldLanguage = ticket.getLanguage();
 
         try {
             ticket.setLanguage(getLanguageEngine().load(ENGLISH));
             final FxResultSet result = new SqlQueryBuilder()
                     .select("@pk", TEST_TYPE + "/stringSearchPropML")
                     .type(TEST_TYPE)
                     .maxRows(maxRows)
                     .getResult();
             assert result.getRowCount() == maxRows;
             final FxPK pk = result.getResultRow(0).getPk(1);
 
             // get reference value from the content engine
             final FxString reference = (FxString) getContentEngine().load(pk).getValue("/stringSearchPropML");
             assert StringUtils.isNotBlank(reference.getTranslation(ENGLISH));
             assert StringUtils.isNotBlank(reference.getTranslation(GERMAN));
             assert !reference.getTranslation(ENGLISH).equals(reference.getTranslation(GERMAN));
 
             // compare translated values in the search result
             checkResultTranslation((FxString) result.getResultRow(0).getFxValue(2), reference, ENGLISH);
 
             ticket.setLanguage(getLanguageEngine().load(GERMAN));
             checkResultTranslation(new SqlQueryBuilder()
                     .select(TEST_TYPE + "/stringSearchPropML")
                     .condition("id", PropertyValueComparator.EQ, pk.getId())
                     .getResult()
                     .<FxString>collectColumn(1)
                     .get(0),
                     reference,
                     GERMAN);
         } finally {
             ticket.setLanguage(oldLanguage);
         }
     }
 
     @Test
     public void searchInOtherLanguagesTest_FX297() throws FxApplicationException {
         final String name = "FX297";
         final FxTreeNodeEdit node = FxTreeNodeEdit.createNew(name);
         node.setLabel(new FxString(FxLanguage.ENGLISH, name));
         final long nodeId = EJBLookup.getTreeEngine().save(node);
        final FxLanguage oldUserLanguage = FxContext.getUserTicket().getLanguage();
         try {
             FxContext.getUserTicket().setLanguage(EJBLookup.getLanguageEngine().load(FxLanguage.ENGLISH));
             queryForCaption(name);
 
             FxContext.getUserTicket().setLanguage(EJBLookup.getLanguageEngine().load(FxLanguage.GERMAN));
             queryForCaption(name);
         } finally {
            FxContext.getUserTicket().setLanguage(oldUserLanguage);
             EJBLookup.getTreeEngine().remove(FxTreeMode.Edit, nodeId, false, false);
         }
     }
 
     private void queryForCaption(String name) throws FxApplicationException {
         final FxResultSet result = new SqlQueryBuilder().select("caption").condition("caption", PropertyValueComparator.EQ, name).getResult();
         assert result.getRowCount() == 1 : "Expected one result row, got: " + result.getRowCount();
         assert name.equals(result.getResultRow(0).getString(1)) : "Expected " + name + ", got: " + result.getResultRow(0).getString(1);
     }
 
     private void checkResultTranslation(FxString resultValue, FxString reference, long language) {
         assert reference.getTranslation(language).equals(resultValue.getBestTranslation())
                 : "bestTranslation of result value not equal to user translation, expected: "
                 + reference.getTranslation(language) + ", got: " + resultValue.getBestTranslation();
         // The following is known to fail, because the SQL result does not know
         // whether a specific translation or the default language was used in a column (FX-265)
         /*final String translationEn = resultValue.getTranslation(language);
         assert StringUtils.isNotBlank(translationEn) : "Ticket language translation not returned";*/
     }
 
     private void assertExactPkMatch(FxPK pk, List<FxPK> pks) {
         assert pks.size() == 1 : "No rows returned for exact match";
         assert pks.get(0).equals(pk) : "Exact match did not return expected column - expected " + pk + ", got: " + pks.get(0);
     }
 
     private List<FxPK> getPksForVersion(VersionFilter versionFilter) throws FxApplicationException {
         return new SqlQueryBuilder().select("@pk").type(TEST_TYPE).filterVersion(versionFilter).getResult().collectColumn(1);
     }
 
 
     @DataProvider(name = "testProperties")
     public Object[][] getTestProperties() {
         final Object[][] result = new Object[TEST_PROPS.size()][];
         int ctr = 0;
         for (Map.Entry<String, FxDataType> entry : TEST_PROPS.entrySet()) {
             result[ctr++] = new Object[]{entry.getKey(), entry.getValue()};
         }
         return result;
     }
 
     private String getTestPropertyName(String baseName) {
         return baseName + TEST_SUFFIX;
     }
 
     private FxPropertyAssignment getTestPropertyAssignment(String baseName) {
         return (FxPropertyAssignment) CacheAdmin.getEnvironment().getAssignment(TEST_TYPE + "/" + getTestPropertyName(baseName));
     }
 
     private void assertAscendingOrder(FxResultSet result, int column) {
         assertOrder(result, column, true);
     }
 
     private void assertDescendingOrder(FxResultSet result, int column) {
         assertOrder(result, column, false);
     }
 
     private void assertOrder(FxResultSet result, int column, boolean ascending) {
         FxValue oldValue = null;
         for (FxResultRow row : result.getResultRows()) {
             // check order
             assert oldValue == null || (ascending
                     ? row.getFxValue(column).compareTo(oldValue) >= 0
                     : row.getFxValue(column).compareTo(oldValue) <= 0)
                     : row.getFxValue(column) + " is not "
                     + (ascending ? "greater" : "less") + " than " + oldValue;
             oldValue = row.getFxValue(column);
         }
     }
 
     private long getTestTypeId() {
         return CacheAdmin.getEnvironment().getType(TEST_TYPE).getId();
     }
 }
