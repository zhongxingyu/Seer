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
 package com.flexive.tests.embedded.cmis;
 
 import com.flexive.shared.CacheAdmin;
 import com.flexive.shared.EJBLookup;
 import static com.flexive.shared.EJBLookup.getCmisSearchEngine;
 import static com.flexive.shared.EJBLookup.getContentEngine;
 import com.flexive.shared.FxContext;
 import com.flexive.shared.FxSharedUtils;
 import com.flexive.shared.search.FxPaths;
 import com.flexive.shared.cmis.CmisVirtualProperty;
 import com.flexive.shared.cmis.search.CmisResultRow;
 import com.flexive.shared.cmis.search.CmisResultSet;
 import com.flexive.shared.content.FxContent;
 import com.flexive.shared.content.FxPK;
 import com.flexive.shared.exceptions.FxAccountInUseException;
 import com.flexive.shared.exceptions.FxApplicationException;
 import com.flexive.shared.exceptions.FxLoginFailedException;
 import com.flexive.shared.exceptions.FxLogoutFailedException;
 import com.flexive.shared.structure.FxSelectListItem;
 import com.flexive.shared.structure.FxType;
 import com.flexive.shared.value.BinaryDescriptor;
 import com.flexive.shared.value.FxNoAccess;
 import com.flexive.shared.value.FxNumber;
 import com.flexive.shared.value.SelectMany;
 import com.flexive.tests.embedded.FxTestUtils;
 import com.flexive.tests.embedded.TestUsers;
 import com.google.common.base.Predicate;
 import com.google.common.collect.Iterables;
 import com.google.common.collect.Lists;
 import org.apache.commons.lang.StringUtils;
 import static org.testng.Assert.*;
 import org.testng.annotations.AfterClass;
 import org.testng.annotations.BeforeClass;
 import org.testng.annotations.Test;
 
 import java.util.*;
 
 /**
  * Tests for the CMIS search engine.
  *
  * @author Daniel Lichtenberger (daniel.lichtenberger@flexive.com), UCS - unique computing solutions gmbh (http://www.ucs.at)
  * @version $Rev$
  * @since 3.1
  */
 public class CmisSearchEngineTest {
     private final List<FxPK> testInstances = new ArrayList<FxPK>();
 
     @BeforeClass(groups = {"search", "cmis", "ejb"})
     public void init() throws FxLoginFailedException, FxAccountInUseException, FxApplicationException {
         FxTestUtils.login(TestUsers.REGULAR);
         createContactData("nestedCondition", "First");
         createContactData("nestedCondition", "Second");
         createContactData("nestedCondition", "Third");
         createArticle("About Maven", "Apache Maven is a build tool like Apache Ant, but with a completely different " +
                 "approach to managing software builds: where Apache Ant is like a procedural programming language " +
                 "for describing how  to build your system, Apache Maven is more of a fully-fledged assembly line " +
                 "for building, packaging, testing, and running software projects. ");
         createArticle("Archetypes", " Maven archetypes provide a quick start for new projects. Currently we offer a " +
                 "archetype for a full enterprise application with web and console frontends. Since the archetype " +
                 "includes setup scripts for the H2 database, it can be used without external dependencies like " +
                 "MySQL. However, for administration tasks such as database setup you currently have to use the " +
                 "tools provided by the [fleXive] distribution. ");
     }
 
     @AfterClass(groups = {"search", "cmis", "ejb"})
     public void cleanup() throws FxApplicationException, FxLogoutFailedException {
         for (FxPK pk : testInstances) {
             getContentEngine().remove(pk);
         }
         FxTestUtils.logout();
     }
 
     @Test(groups = {"shared", "search", "cmis"})
     public void resultFilter() {
         final CmisResultSet rs = new CmisResultSet(2);
         rs.addRow(rs.newRow().setValue(1, "Apple").setValue(2, new FxNumber(25)));
         rs.addRow(rs.newRow().setValue(1, "Banana").setValue(2, new FxNumber(30)));
         assertEquals(rs.filterEqual(1, "Apple").size(), 1);
         assertEquals(rs.filterEqual(1, "Apple").get(0).getColumn(1).toString(), "Apple");
         assertEquals(rs.filterEqual(2, 30).size(), 1);
         assertEquals(rs.filterEqual(2, 30).get(0).getColumn(2).getValue(), new FxNumber(30));
         assertEquals(rs.filterEqual(2, new FxNumber(30)).get(0).getColumn(2).getValue(), new FxNumber(30));
     }
 
     @Test(groups = {"search", "cmis", "ejb"})
     public void simpleSelect() throws FxApplicationException {
         final CmisResultSet result = getCmisSearchEngine().search("SELECT d.surname FROM contactData AS d");
         assertTrue(result.getRowCount() > 0);
         for (CmisResultRow row : result) {
             assertTrue(!row.getColumn(1).isEmpty(), "Value should not be empty: " + row.getColumn(1));
             System.out.println(row.getColumn(1));
         }
     }
 
     @Test(groups = {"search", "cmis", "ejb"})
     public void paging() throws FxApplicationException {
         final String query = "SELECT name FROM cmis_person ORDER BY name DESC";
         final CmisResultSet refResult = getCmisSearchEngine().search(query);
         final int expectedRows = 4;
         assertRowCount(refResult, expectedRows);
         assertEquals(refResult.collectColumnValues(1), Arrays.asList("Sandra Locke", "Peter Bones", "Martin Black", "Alex Cervais"));
 
         int startRow = 0;
         final int maxRows = 2;
         while (startRow < expectedRows) {
             final CmisResultSet result = getCmisSearchEngine().search(query, true, startRow, maxRows);
             assertRowCount(result, Math.min(maxRows, expectedRows - startRow));
             for (int idx = startRow; idx < startRow + maxRows; idx++) {
                 if (idx < refResult.getRowCount()) {
                     assertEquals(
                             refResult.getColumn(idx, 1).getValue(),
                             result.getColumn(idx - startRow, 1).getValue()
                     );
                 }
             }
             startRow += maxRows;
         }
     }
 
 
     @Test(groups = {"search", "cmis", "ejb"})
     public void simpleCondition() throws FxApplicationException {
         createContactData("simpleConditionTest", null);
         final CmisResultSet result = getCmisSearchEngine().search(
                 "SELECT d.surname FROM contactData AS d WHERE d.surname = 'simpleConditionTest'"
         );
         assertRowCount(result, 1);
         assertEquals(result.getColumn(0, 1).toString(), "simpleConditionTest");
     }
 
     @Test(groups = {"search", "cmis", "ejb"})
     public void simpleDisjunction() throws FxApplicationException {
         createContactData("simpleDisjunction1", null);
         createContactData("simpleDisjunction2", null);
         final CmisResultSet result = getCmisSearchEngine().search(
                 "SELECT surname FROM contactData WHERE surname = 'simpleDisjunction1' OR surname = 'simpleDisjunction2'"
         );
         assertRowCount(result, 2);
         assertTrue(result.getColumn(0, 1).toString().startsWith("simpleDisjunction"));
         assertTrue(result.getColumn(1, 1).toString().startsWith("simpleDisjunction"));
         assertTrue(!result.getColumn(1, 1).equals(result.getColumn(0, 1)), "Two identical rows returned");
     }
 
     @Test(groups = {"search", "cmis", "ejb"})
     public void simpleNegation() throws FxApplicationException {
         final CmisResultSet result = getCmisSearchEngine().search(
                 "SELECT name FROM cmis_person WHERE name <> 'Peter Bones' ORDER BY name"
         );
         assertRowCount(result, 3);
         assertEquals(result.collectColumnValues(1), Arrays.asList("Alex Cervais", "Martin Black", "Sandra Locke"));
     }
 
     @Test(groups = {"search", "cmis", "ejb"})
     public void disjunctionWithOverlap() throws FxApplicationException {
         createContactData("disjunctionWithOverlap", null);
         final CmisResultSet result = getCmisSearchEngine().search(
                 "SELECT surname FROM contactData WHERE surname = 'disjunctionWithOverlap' OR surname = 'disjunctionWithOverlap'"
         );
         assertRowCount(result, 1);
         assertEquals(result.getColumn(0, 1).toString(), "disjunctionWithOverlap");
     }
 
     @Test(groups = {"search", "cmis", "ejb"})
     public void nestedCondition() throws FxApplicationException {
         final CmisResultSet result = getCmisSearchEngine().search(
                 "SELECT name, surname FROM contactData WHERE surname = 'nestedCondition' AND (name = 'First' OR name = 'Second')"
         );
         assertRowCount(result, 2);
         assertEquals(result.getColumn(0, 2).toString(), "nestedCondition");
         assertEquals(result.getColumn(1, 2).toString(), "nestedCondition");
         final String row1 = result.getColumn(0, 1).toString();
         final String row2 = result.getColumn(1, 1).toString();
         assertTrue("First".equals(row1) || "First".equals(row2), "Unexpected result: " + row1 + ", " + row2);
         assertTrue("Second".equals(row1) || "Second".equals(row2), "Unexpected result: " + row1 + ", " + row2);
     }
 
     @Test(groups = {"search", "cmis", "ejb"})
     public void nestedConditions() throws FxApplicationException {
         final CmisResultSet result = getCmisSearchEngine().search(
                 "SELECT d.name, d.surname FROM contactData AS d WHERE (surname = 'nestedCondition' AND name='First') "
                         + "  OR ((name = 'Second' OR name = 'Third') AND surname = 'nestedCondition')"
         );
         assertRowCount(result, 3);
         for (String name : new String[]{"First", "Second", "Third"}) {
             boolean found = false;
             for (int i = 0; i < 3; i++) {
                 if (name.equals(result.getColumn(i, 1).toString())) {
                     found = true;
                     break;
                 }
             }
             assertTrue(found, "Expected to find " + name + " in result.");
         }
     }
 
     @Test(groups = {"search", "cmis", "ejb"})
     public void selectMainTableColumn() throws FxApplicationException {
         final CmisResultSet result = getCmisSearchEngine().search(
                 "SELECT d.id, d.surname, d.mandator FROM contactData AS d"
         );
         assertTrue(result.getRowCount() > 0);
         for (CmisResultRow row : result) {
             assertTrue(!row.getColumn(2).isEmpty(), "Column 2 should not be empty: " + row.getColumns());
             final FxContent content = EJBLookup.getContentEngine().load(
                     new FxPK(row.getColumn(1).getLong())
             );
             assertEquals(
                     row.getColumn(3).getLong(),
                     content.getMandatorId(),
                     "Search result returned invalid mandator ID."
             );
             assertEquals(
                     row.getColumn(2).toString(),
                     content.getValue("/surname").toString()
             );
         }
     }
 
     @Test(groups = {"search", "cmis", "ejb"})
     public void selectColumnWithAlias() throws FxApplicationException {
         final CmisResultSet result = getCmisSearchEngine().search(
                 "SELECT surname AS sn FROM contactData AS d"
         );
         assertTrue(result.getRowCount() > 0);
         assertEquals(result.getColumn(0, "sn"), result.getColumn(0, 1));
     }
 
     @Test(groups = {"search", "cmis", "ejb"})
     public void selectCmisProperties() throws FxApplicationException {
         final List<String> columns = Lists.newArrayList();
         for (CmisVirtualProperty vp : CmisVirtualProperty.values()) {
             if (vp.getFxPropertyName() != null && vp.getFxPropertyName().charAt(0) != '@') {
                 // property can be mapped directly, select the CMIS variant and the flexive one
                 columns.add(vp.getCmisPropertyName());
                 columns.add(vp.getFxPropertyName());
             }
         }
         final CmisResultSet result = getCmisSearchEngine().search(
                 "SELECT " + StringUtils.join(columns, ',') + " FROM document"
         );
         assertTrue(result.getRowCount() > 0);
         for (CmisResultRow row : result) {
             for (int i = 0; i < result.getColumnCount() / 2; i++) {
                 final int cmis = i * 2 + 1;
                 final int flexive = i * 2 + 2;
                 assertEquals(row.getColumn(cmis), row.getColumn(flexive),
                         "CMIS property returned other result than flexive property: "
                                 + row.getColumn(cmis) + " (" + result.getColumnAliases().get(cmis - 1) + ") vs. "
                                 + row.getColumn(flexive) + " (" + result.getColumnAliases().get(flexive - 1) + ")"
                 );
             }
         }
     }
 
     @Test(groups = {"search", "cmis", "ejb"})
     public void selectParent() throws FxApplicationException {
         final CmisResultSet result = getCmisSearchEngine().search(
                 "SELECT ObjectId, ParentId, Name FROM folder WHERE Name NOT LIKE 'test caption%'"
         );
         assertTrue(result.getRowCount() > 0);
         for (CmisResultRow row : result) {
             assertNotNull(row.getColumn("ParentId").getValue());
             if (!"root".equalsIgnoreCase(row.getColumn("name").getString())) {
                 final FxPaths paths = row.getColumn("ParentId").getPaths();
                 assertTrue(paths.getPaths().size() > 0);
             }
         }
     }
 
     @Test(groups = {"search", "cmis", "ejb"})
     public void selectPK() throws FxApplicationException {
         final CmisResultSet result = getCmisSearchEngine().search(
                 "SELECT " + CmisVirtualProperty.Id.getCmisPropertyName() + ", id, version FROM root"
         );
         assertTrue(result.getRowCount() > 0);
         for (CmisResultRow row : result) {
             assertEquals(
                     row.getColumn(CmisVirtualProperty.Id.getCmisPropertyName()).getValue(),
                     new FxPK(row.getColumn(2).getLong(), row.getColumn(3).getInt())
             );
         }
     }
 
     @Test(groups = {"search", "cmis", "ejb"})
     public void selectBinary() throws FxApplicationException {
         final CmisResultSet result = getCmisSearchEngine().search(
                 "SELECT id, imageBinary FROM image"
         );
         assertTrue(result.getRowCount() > 0);
         for (CmisResultRow row : result) {
             final BinaryDescriptor binary = row.getColumn(2).getBinary();
             assertNotNull(binary);
             assertNotNull(binary.getName());
             assertNotNull(binary.getMimeType());
             assertTrue(binary.getSize() > 0, "Expected size to be greater than 0");
         }
     }
 
     @Test(groups = {"search", "cmis", "ejb"})
     public void selectUpper() throws FxApplicationException {
         final CmisResultSet result = getCmisSearchEngine().search(
                 "SELECT UPPER(surname) FROM contactData"
         );
         assertTrue(result.getRowCount() > 0);
         for (CmisResultRow row : result) {
             assertTrue(row.getColumn(1).toString().toUpperCase().equals(row.getColumn(1).toString()), "Value should be in upper case: " + row.getColumn(1));
         }
     }
 
     @Test(groups = {"search", "cmis", "ejb"})
     public void selectLower() throws FxApplicationException {
         final CmisResultSet result = getCmisSearchEngine().search(
                 "SELECT LOWER(surname) FROM contactData"
         );
         assertTrue(result.getRowCount() > 0);
         for (CmisResultRow row : result) {
             assertTrue(row.getColumn(1).toString().toLowerCase().equals(row.getColumn(1).toString()), "Value should be in lower case: " + row.getColumn(1));
         }
     }
 
     @Test(groups = {"search", "cmis", "ejb"})
     public void stringFunCondition() throws FxApplicationException {
         final CmisResultSet result = getCmisSearchEngine().search(
                 "SELECT surname FROM contactData WHERE LOWER(surname) = 'nestedcondition' OR UPPER(surname) = 'NESTEDCONDITION'"
         );
         assertTrue(result.getRowCount() > 0, "Lower match did not return any rows");
         for (CmisResultRow row : result) {
             assertEquals(row.getColumn("surname").toString(), "nestedCondition");
         }
     }
 
     @Test(groups = {"search", "cmis", "ejb"})
     public void fulltextQuery() throws FxApplicationException {
        System.out.println("\n\n\n\n\n\n********************************************");
         final CmisResultSet result = getCmisSearchEngine().search(
                 "SELECT id, longtext FROM article WHERE CONTAINS('procedural')"
         );
        System.out.println("\n\n\n\n\n\n********************************************");
         assertRowCount(result, 1);
         assertTrue(result.getColumn(0, 2).toString().contains("procedural programming language"), "Expected fulltext match against 'complex and evolving': " + result.getColumn(0, 2));
     }
 
     @Test(groups = {"search", "cmis", "ejb"})
     public void selectScore() throws FxApplicationException {
         final CmisResultSet result = getCmisSearchEngine().search(
                 "SELECT id, longtext, SCORE() AS \"score\" FROM article WHERE CONTAINS('Maven')"
         );
         assertRowCount(result, 2);
         for (CmisResultRow row : result) {
             assertTrue(row.getColumn("longtext").toString().contains("Maven"));
             final double score = row.getColumn("score").getDouble();
             assertTrue(score > 0 && score <= 1, "Score should be between 0 and 1, is: " + score);
         }
     }
 
     @Test(groups = {"search", "cmis", "ejb"})
     public void selectScoreMulti() throws FxApplicationException {
         final CmisResultSet result = getCmisSearchEngine().search(
                 "SELECT id, SCORE() AS score1, longtext, SCORE() AS score2 FROM article WHERE CONTAINS('Maven')"
         );
         assertRowCount(result, 2);
         for (CmisResultRow row : result) {
             assertTrue(row.getColumn("longtext").toString().contains("Maven"));
             for (String column : new String[]{"score1", "score2"}) {
                 final double score = row.getColumn(column).getDouble();
                 assertTrue(score > 0 && score <= 1, "Score in column " + column + " should be between 0 and 1, is: " + score);
             }
         }
     }
 
     @Test(groups = {"search", "cmis", "ejb"})
     public void basicOrderBy() throws FxApplicationException {
         checkOrderBy("name", "First", "Second", "Third");
         checkOrderBy("name ASC", "First", "Second", "Third");
         checkOrderBy("name DESC", "Third", "Second", "First");
     }
 
     private void checkOrderBy(String orderBy, Object... expected) throws FxApplicationException {
         final CmisResultSet result = getCmisSearchEngine().search(
                 "SELECT name, surname FROM contactData WHERE surname='nestedCondition' ORDER BY " + orderBy
         );
         assertRowCount(result, 3);
         assertEquals(result.collectColumnValues(1), Arrays.asList(expected));
     }
 
     @Test(groups = {"search", "cmis", "ejb"})
     public void mainTableCondition() throws FxApplicationException {
         final CmisResultSet result = getCmisSearchEngine().search(
                 "SELECT person.name FROM cmis_person AS person WHERE person.typedef = "
                         + CacheAdmin.getEnvironment().getType("CMIS_PERSON").getId()
                         + " AND person.name = 'Peter Bones'"
         );
         assertRowCount(result, 1);
         assertEquals(result.getColumn(0, 1).getString(), "Peter Bones");
 
     }
 
     @Test(groups = {"search", "cmis", "ejb"})
     public void basicJoin() throws FxApplicationException {
         final CmisResultSet result = getCmisSearchEngine().search(
                 "SELECT person.name, data.annualSalary FROM CMIS_PERSON AS person JOIN CMIS_PERSON_DATA AS data "
                         + "ON (person.ssn = data.ssn) "
         );
         assertRowCount(result, 3);
     }
 
     @Test(groups = {"search", "cmis", "ejb"})
     public void basicJoinWithOrder() throws FxApplicationException {
         checkJoinWithOrder("name", Arrays.asList("Martin Black", "Peter Bones", "Sandra Locke"), Arrays.asList(75000L, 50000L, 85000L));
         checkJoinWithOrder("name ASC", Arrays.asList("Martin Black", "Peter Bones", "Sandra Locke"), Arrays.asList(75000L, 50000L, 85000L));
         checkJoinWithOrder("annualSalary", Arrays.asList("Peter Bones", "Martin Black", "Sandra Locke"), Arrays.asList(50000L, 75000L, 85000L));
         checkJoinWithOrder("name DESC", Arrays.asList("Sandra Locke", "Peter Bones", "Martin Black"), Arrays.asList(85000L, 50000L, 75000L));
         checkJoinWithOrder("annualSalary DESC", Arrays.asList("Sandra Locke", "Martin Black", "Peter Bones"), Arrays.asList(85000L, 75000L, 50000L));
     }
 
     private void checkJoinWithOrder(String orderBy, List<String> expectedNames, List<Long> expectedSalaries) throws FxApplicationException {
         final CmisResultSet result = getCmisSearchEngine().search(
                 "SELECT person.name, data.annualSalary FROM CMIS_PERSON AS person JOIN CMIS_PERSON_DATA AS data "
                         + "ON (person.ssn = data.ssn) ORDER BY " + orderBy
         );
         assertRowCount(result, 3);
         assertEquals(result.collectColumnValues(1), expectedNames);
         assertEquals(result.collectColumnValues(2), expectedSalaries);
     }
 
     @Test(groups = {"search", "cmis", "ejb"})
     public void basicJoinWithCondition() throws FxApplicationException {
         final CmisResultSet result = getCmisSearchEngine().search(
                 "SELECT person.name, data.annualSalary FROM CMIS_PERSON AS person JOIN CMIS_PERSON_DATA AS data "
                         + "ON (person.ssn = data.ssn) "
                         + "WHERE data.annualSalary = 50000"
         );
         assertRowCount(result, 1);
         assertEquals(result.getColumn(0, 1).getString(), "Peter Bones");
         assertEquals(result.getColumn(0, 2).getInt(), 50000);
     }
 
     @Test(groups = {"search", "cmis", "ejb"})
     public void basicJoinWithDisjunction() throws FxApplicationException {
         final CmisResultSet result = getCmisSearchEngine().search(
                 "SELECT person.name, data.annualSalary FROM CMIS_PERSON AS person JOIN CMIS_PERSON_DATA AS data "
                         + "ON (person.ssn = data.ssn) "
                         + "WHERE data.annualSalary = 50000 OR person.name='Sandra Locke' "
                         + "ORDER BY name"
         );
         assertRowCount(result, 2);
         assertEquals(result.collectColumnValues(1), Arrays.asList("Peter Bones", "Sandra Locke"));
         assertEquals(result.collectColumnValues(2), Arrays.asList(50000L, 85000L));
     }
 
     @Test(groups = {"search", "cmis", "ejb"})
     public void basicJoinWithConjunction() throws FxApplicationException {
         final CmisResultSet result = getCmisSearchEngine().search(
                 "SELECT person.name, data.annualSalary FROM CMIS_PERSON AS person JOIN CMIS_PERSON_DATA AS data "
                         + "ON (person.ssn = data.ssn) "
                         + "WHERE data.annualSalary >= 50000 AND person.name='Peter Bones'"
         );
         assertRowCount(result, 1);
         assertEquals(result.getColumn(0, 1).getString(), "Peter Bones");
         assertEquals(result.getColumn(0, 2).getInt(), 50000);
     }
 
     @Test(groups = {"search", "cmis", "ejb"})
     public void basicJoinWithConjunctionTriple() throws FxApplicationException {
         // like basicJoinWithConjunction, but with three conditions, of which two reference the same table
         final CmisResultSet result = getCmisSearchEngine().search(
                 "SELECT person.name, data.annualSalary FROM CMIS_PERSON AS person JOIN CMIS_PERSON_DATA AS data "
                         + "ON (person.ssn = data.ssn) "
                         + "WHERE data.annualSalary = 50000 AND person.name='Peter Bones' AND person.ssn='3721'"
         );
         assertRowCount(result, 1);
         assertEquals(result.getColumn(0, 1).getString(), "Peter Bones");
         assertEquals(result.getColumn(0, 2).getInt(), 50000);
     }
 
     @Test(groups = {"search", "cmis", "ejb"})
     public void basicJoinWithNestedCondition() throws FxApplicationException {
         final CmisResultSet result = getCmisSearchEngine().search(
                 "SELECT person.name, data.annualSalary FROM CMIS_PERSON AS person JOIN CMIS_PERSON_DATA AS data "
                         + "ON (person.ssn = data.ssn) "
                         + "WHERE data.annualSalary = 50000"
                         + " OR (person.name = 'Martin Black' AND data.annualSalary = 75000) "
                         + "ORDER BY annualSalary"
         );
         assertRowCount(result, 2);
         assertEquals(result.collectColumnValues(2), Arrays.asList(50000L, 75000L));
         assertEquals(result.collectColumnValues(1), Arrays.asList("Peter Bones", "Martin Black"));
     }
 
     @Test(groups = {"search", "cmis", "ejb"})
     public void basicJoinWithNestedConditionTriple() throws FxApplicationException {
         final CmisResultSet result = getCmisSearchEngine().search(
                 "SELECT person.name, data.annualSalary FROM CMIS_PERSON AS person JOIN CMIS_PERSON_DATA AS data "
                         + "ON (person.ssn = data.ssn) "
                         + "WHERE data.annualSalary = 50000"
                         + " OR (person.name = 'Martin Black' AND data.annualSalary = 75000 AND data.ssn='5241') "
                         + "ORDER BY annualSalary"
         );
         assertRowCount(result, 2);
         assertEquals(result.collectColumnValues(2), Arrays.asList(50000L, 75000L));
         assertEquals(result.collectColumnValues(1), Arrays.asList("Peter Bones", "Martin Black"));
     }
 
     @Test(groups = {"search", "cmis", "ejb"})
     public void basicJoinWithNestedConditions() throws FxApplicationException {
         final CmisResultSet result = getCmisSearchEngine().search(
                 "SELECT person.name, data.annualSalary FROM CMIS_PERSON AS person JOIN CMIS_PERSON_DATA AS data "
                         + "ON (person.ssn = data.ssn) "
                         + "WHERE (data.annualSalary = 50000 AND person.name = 'Peter Bones')"
                         + " OR (person.name = 'Martin Black' AND data.annualSalary = 75000) "
                         + "ORDER BY annualSalary"
         );
         assertRowCount(result, 2);
         assertEquals(result.collectColumnValues(2), Arrays.asList(50000L, 75000L));
         assertEquals(result.collectColumnValues(1), Arrays.asList("Peter Bones", "Martin Black"));
     }
 
     @Test(groups = {"search", "cmis", "ejb"})
     public void mixedCaseQuery() throws FxApplicationException {
         final CmisResultSet result = getCmisSearchEngine().search(
                 "select person.name, data.annualSalary from CMIS_PERSON as person join CMIS_PERSON_DATA as data "
                         + "on (person.ssn = data.ssn) "
                         + "where (data.annualSalary = 50000 and person.name = 'Peter Bones')"
                         + " or (person.name = 'Martin Black' and data.annualSalary = 75000) "
                         + "order by annualSalary"
         );
         assertRowCount(result, 2);
         assertEquals(result.collectColumnValues("annualSalary"), Arrays.asList(50000L, 75000L));
         assertEquals(result.collectColumnValues("name"), Arrays.asList("Peter Bones", "Martin Black"));
     }
 
     @Test(groups = {"search", "cmis", "ejb"})
     public void delimitedIdentifiersQuery() throws FxApplicationException {
         final CmisResultSet result = getCmisSearchEngine().search(
                 "select \"person\".\"name\", \"data\".\"annualSalary\" from \"CMIS_PERSON\" as \"person\" join \"CMIS_PERSON_DATA\" as \"data\" "
                         + "on (\"person\".\"ssn\" = \"data\".\"ssn\") "
                         + "where (\"data\".\"annualSalary\" = 50000 and \"person\".\"name\" = 'Peter Bones')"
                         + " or (\"person\".\"name\" = 'Martin Black' and \"data\".\"annualSalary\" = 75000) "
                         + "order by \"annualSalary\""
         );
         assertRowCount(result, 2);
         assertEquals(result.collectColumnValues("annualSalary"), Arrays.asList(50000L, 75000L));
         assertEquals(result.collectColumnValues("name"), Arrays.asList("Peter Bones", "Martin Black"));
     }
 
     @Test(groups = {"search", "cmis", "ejb"})
     public void basicJoinWithNestedConjunctions() throws FxApplicationException {
         final CmisResultSet result = getCmisSearchEngine().search(
                 "SELECT person.name, data.annualSalary FROM CMIS_PERSON AS person JOIN CMIS_PERSON_DATA AS data "
                         + "ON (person.ssn = data.ssn) "
                         + "WHERE (data.annualSalary >= 50000 AND (person.name = 'Peter Bones' OR person.name='Martin Black')) "
                         + "ORDER BY annualSalary"
         );
         assertRowCount(result, 2);
         assertEquals(result.collectColumnValues(2), Arrays.asList(50000L, 75000L));
         assertEquals(result.collectColumnValues(1), Arrays.asList("Peter Bones", "Martin Black"));
     }
 
     @Test(groups = {"search", "cmis", "ejb"})
     public void joinWithReference() throws FxApplicationException {
         final CmisResultSet result = getCmisSearchEngine().search(
                 "SELECT person.name, log.date, log.activity "
                         + "FROM cmis_person AS person JOIN cmis_audit_log AS log "
                         + "ON (person.id=log.person) "
                         + "ORDER BY name, date"
         );
         assertRowCount(result, 4);
         assertEquals(result.collectColumnValues(1), Arrays.asList("Peter Bones", "Peter Bones", "Sandra Locke", "Sandra Locke"));
         assertEquals(result.collectColumnValues(1), Arrays.asList("Peter Bones", "Peter Bones", "Sandra Locke", "Sandra Locke"));
         assertEquals(result.collectColumnValues(3), Arrays.asList("Logged in", "Logged out", "Updated article #31.5", "Updated contact #912.1"));
     }
 
     @Test(groups = {"search", "cmis", "ejb"})
     public void likeCondition() throws FxApplicationException {
         final CmisResultSet result = getCmisSearchEngine().search(
                 "SELECT name FROM cmis_person WHERE name LIKE 'Peter%'"
         );
         assertRowCount(result, 1);
         assertEquals(result.getColumn(0, 1).getString(), "Peter Bones");
     }
 
     @Test(groups = {"search", "cmis", "ejb"})
     public void notLikeCondition() throws FxApplicationException {
         final CmisResultSet result = getCmisSearchEngine().search(
                 "SELECT name FROM cmis_person WHERE name NOT LIKE 'Peter%' ORDER BY name"
         );
         assertRowCount(result, 3);
         assertEquals(result.collectColumnValues(1), Arrays.asList("Alex Cervais", "Martin Black", "Sandra Locke"));
     }
 
     @Test(groups = {"search", "cmis", "ejb"})
     public void limitToAssignment() throws FxApplicationException {
         final FxPK pk1 = createContactData("abc", "def");
         final FxPK pk2 = createContactData("def", "abc");
         final CmisResultSet result1 = getCmisSearchEngine().search(
                 "SELECT id FROM contactData WHERE name='def'"       // should return pk1
         );
         assertRowCount(result1, 1);
         assertEquals(result1.getColumn(0, 1).getLong(), pk1.getId(), "Condition not constrained to values of assignment");
 
         final CmisResultSet result2 = getCmisSearchEngine().search(
                 "SELECT id FROM contactData WHERE name='abc'"       // should return pk2
         );
         assertRowCount(result2, 1);
         assertEquals(result2.getColumn(0, 1).getLong(), pk2.getId(), "Condition not constrained to values of assignment");
     }
 
     @Test(groups = {"search", "cmis", "ejb"})
     public void basicInCondition() throws FxApplicationException {
         final CmisResultSet result = getCmisSearchEngine().search(
                 "SELECT name FROM cmis_person WHERE name IN ('Peter Bones', 'Martin Black') ORDER BY name"
         );
         assertRowCount(result, 2);
         assertEquals(result.collectColumnValues(1), Arrays.asList("Martin Black", "Peter Bones"));
     }
 
     @Test(groups = {"search", "cmis", "ejb"})
     public void basicNotInCondition() throws FxApplicationException {
         final CmisResultSet result = getCmisSearchEngine().search(
                 "SELECT name FROM cmis_person WHERE name NOT IN ('Peter Bones', 'Martin Black') ORDER BY name"
         );
         assertRowCount(result, 2);
         assertEquals(result.collectColumnValues(1), Arrays.asList("Alex Cervais", "Sandra Locke"));
     }
 
     @Test(groups = {"search", "cmis", "ejb"})
     public void numericInConditionWithJoin() throws FxApplicationException {
         final CmisResultSet result = getCmisSearchEngine().search(
                 "SELECT p.name, d.annualSalary FROM cmis_person AS p JOIN cmis_person_data AS d ON (p.ssn=d.ssn) "
                         + "WHERE d.annualSalary IN (50000, 85000) ORDER BY name"
         );
         assertRowCount(result, 2);
         assertEquals(result.collectColumnValues(1), Arrays.asList("Peter Bones", "Sandra Locke"));
         assertEquals(result.collectColumnValues(2), Arrays.asList(50000L, 85000L));
     }
 
     @Test(groups = {"search", "cmis", "ejb"})
     public void numericNotInConditionWithJoin() throws FxApplicationException {
         final CmisResultSet result = getCmisSearchEngine().search(
                 "SELECT p.name, d.annualSalary FROM cmis_person AS p JOIN cmis_person_data AS d ON (p.ssn=d.ssn) "
                         + "WHERE d.annualSalary NOT IN (50000, 85000) ORDER BY name"
         );
         assertRowCount(result, 1);
         assertEquals(result.collectColumnValues(1), Arrays.asList("Martin Black"));
         assertEquals(result.collectColumnValues(2), Arrays.asList(75000L));
     }
 
     @Test(groups = {"search", "cmis", "ejb"})
     public void anyInStringCondition() throws FxApplicationException {
         final CmisResultSet result = getCmisSearchEngine().search(
                 "SELECT name FROM cmis_person WHERE ANY email IN ('peter.bones@myprov.net', 'sandra.locke@gmx.net') ORDER BY name"
         );
         assertRowCount(result, 2);
         assertEquals(result.collectColumnValues(1), Arrays.asList("Peter Bones", "Sandra Locke"));
     }
 
     @Test(groups = {"search", "cmis", "ejb"})
     public void anyNotInStringCondition() throws FxApplicationException {
         final CmisResultSet result = getCmisSearchEngine().search(
                 "SELECT name FROM cmis_person WHERE ANY email NOT IN ('peter.bones@myprov.net', 'sandra.locke@gmx.net') ORDER BY name"
         );
         assertRowCount(result, 1);
         // peter bones has another email address, so it is returned
         assertEquals(result.collectColumnValues(1), Arrays.asList("Peter Bones"));
     }
 
     @Test(groups = {"search", "cmis", "ejb"})
     public void allNotInStringCondition() throws FxApplicationException {
         final CmisResultSet result = getCmisSearchEngine().search(
                 "SELECT name FROM cmis_person WHERE ANY email NOT IN ('peter.bones@myprov.net', 'peter.bones@gmail.com') ORDER BY name"
         );
         assertRowCount(result, 1);
         // only sandra locke has an email address that is not filtered
         assertEquals(result.collectColumnValues(1), Arrays.asList("Sandra Locke"));
     }
 
     @Test(groups = {"search", "cmis", "ejb"})
     public void multivaluedNegation() throws FxApplicationException {
         final CmisResultSet result = getCmisSearchEngine().search(
                 "SELECT name FROM cmis_person WHERE 'peter.bones@myprov.net' <> ANY email ORDER BY name"
         );
         assertRowCount(result, 2);
         assertEquals(result.collectColumnValues(1), Arrays.asList("Peter Bones", "Sandra Locke"));
     }
 
     @Test(groups = {"search", "cmis", "ejb"})
     public void isNullCondition() throws FxApplicationException {
         final CmisResultSet result = getCmisSearchEngine().search(
                 "SELECT name FROM cmis_person WHERE email IS NULL ORDER BY name"
         );
         assertRowCount(result, 2);
         assertEquals(result.collectColumnValues(1), Arrays.asList("Alex Cervais", "Martin Black"));
     }
 
     @Test(groups = {"search", "cmis", "ejb"})
     public void isNotNullCondition() throws FxApplicationException {
         final CmisResultSet result = getCmisSearchEngine().search(
                 "SELECT name FROM cmis_person WHERE email IS NOT NULL ORDER BY name"
         );
         assertRowCount(result, 2);
         assertEquals(result.collectColumnValues(1), Arrays.asList("Peter Bones", "Sandra Locke"));
     }
 
     @Test(groups = {"search", "cmis", "ejb"})
     public void basicSelectWithDerivedTypes() throws FxApplicationException {
         final CmisResultSet result = getCmisSearchEngine().search(
                 "SELECT astring FROM cmis_type_a ORDER BY astring"
         );
         assertRowCount(result, 3);
         assertEquals(result.collectColumnValues(1), Arrays.asList("A", "AB", "ABC"));
     }
 
     @Test(groups = {"search", "cmis", "ejb"})
     public void basicConditionWithDerivedTypes() throws FxApplicationException {
         final CmisResultSet result = getCmisSearchEngine().search(
                 "SELECT astring FROM cmis_type_a WHERE astring LIKE 'A%' ORDER BY astring"
         );
         assertRowCount(result, 3);
         assertEquals(result.collectColumnValues(1), Arrays.asList("A", "AB", "ABC"));
     }
 
     @Test(groups = {"search", "cmis", "ejb"})
     public void selectFromDerivedType() throws FxApplicationException {
         final CmisResultSet result = getCmisSearchEngine().search(
                 "SELECT astring, bstring FROM cmis_type_b ORDER BY astring"
         );
         assertRowCount(result, 2);
         assertEquals(result.collectColumnValues(1), Arrays.asList("AB", "ABC"));
         assertEquals(result.collectColumnValues(2), Arrays.asList("B", "BC"));
     }
 
     @Test(groups = {"search", "cmis", "ejb"})
     public void selectMultivalued() throws FxApplicationException {
         final CmisResultSet result = getCmisSearchEngine().search(
                 "SELECT name, email FROM cmis_person ORDER BY name"
         );
         assertRowCount(result, 4);
         assertEquals(result.collectColumnValues(1), Arrays.asList("Alex Cervais", "Martin Black", "Peter Bones", "Sandra Locke"));
         assertEquals(result.collectColumnValues(2), Arrays.asList(
                 null, null,
                 Arrays.asList("peter.bones@gmail.com", "peter.bones@myprov.net"),
                 Arrays.asList("sandra.locke@gmx.net")
         ));
     }
 
     @Test(groups = {"search", "cmis", "ejb"})
     public void selectMultivaluedDate() throws FxApplicationException {
         final CmisResultSet result = getCmisSearchEngine().search(
                 "SELECT name, date FROM cmis_multivalued ORDER BY name"
         );
         assertRowCount(result, 2);
         assertEquals(result.collectColumnValues(1), Arrays.asList("Saturday Night Fever", "Sunday Chillout"));
         assertTrue(result.getColumn(0, 2).isAggregate(), "Multivalued column should be create an aggregate result value");
         assertTrue(result.getColumn(0, 2).getValues().size() == 3, "Expected three dates, got: " + result.getColumn(0, 2).getValues());
         assertTrue(result.getColumn(0, 2).getValue() instanceof Date, "Expected value type java.util.Date, got: " + result.getColumn(0, 2).getValue().getClass());
         assertTrue(result.getColumn(0, 2).getValues().get(0) instanceof Date,
                 "Expected value type java.util.Date, got: " + result.getColumn(0, 2).getValues().get(0).getClass()
         );
     }
 
 
     @Test(groups = {"search", "cmis", "ejb"})
     public void propertySecurity() throws FxApplicationException {
         final CmisResultSet result = getCmisSearchEngine().search(
                 "SELECT name, sensible FROM cmis_property_perm ORDER BY name"
         );
         assertRowCount(result, 2);
         assertEquals(result.collectColumnValues(1), Arrays.asList("Protected", "Unprotected"));
         assertEquals(result.getColumn(0, 2).getValue().getClass(), FxNoAccess.class);
         assertEquals(result.getColumn(1, 2).getValue(), "Unprotected sensible");
     }
 
     @Test(groups = {"search", "cmis", "ejb"})
     public void versionFilter() throws FxApplicationException {
         final FxPK contentPk = new FxPK(
                 getCmisSearchEngine().search("SELECT id FROM cmis_person WHERE name='Peter Bones'").getColumn(0, 1).getLong(),
                 FxPK.MAX
         );
         final FxPK newPK = getContentEngine().createNewVersion(getContentEngine().load(contentPk));
         try {
             final CmisResultSet result = getCmisSearchEngine().search(
                     "SELECT id, version, name FROM cmis_person WHERE name='Peter Bones'"
             );
             assertRowCount(result, 1);
             assertEquals(result.getColumn(0, 1).getLong(), newPK.getId());
             assertEquals(result.getColumn(0, 2).getInt(), newPK.getVersion(), "Max version should have been selected");
             assertEquals(result.getColumn(0, 3).getString(), "Peter Bones");
         } finally {
             try {
                 FxContext.get().runAsSystem();
                 getContentEngine().removeVersion(newPK);
             } finally {
                 FxContext.get().stopRunAsSystem();
             }
         }
     }
 
     @Test(groups = {"search", "cmis", "ejb"})
     public void selectAll() throws FxApplicationException {
         final CmisResultSet result = getCmisSearchEngine().search(
                 "SELECT * FROM cmis_person ORDER BY name"
         );
         assertRowCount(result, 4);
         assertEquals(result.getColumnAliases().subList(0, 3), Arrays.asList("name", "ssn", "email"));
         assertEquals(result.collectColumnValues("name"), Arrays.asList("Alex Cervais", "Martin Black", "Peter Bones", "Sandra Locke"));
     }
 
     @Test(groups = {"search", "cmis", "ejb"})
     public void selectAllWithAlias() throws FxApplicationException {
         final CmisResultSet result = getCmisSearchEngine().search(
                 "SELECT p.id, p.* FROM cmis_person AS p ORDER BY name"
         );
         assertRowCount(result, 4);
         assertEquals(result.getColumnAliases().subList(0, 4), Arrays.asList("id", "name", "ssn", "email"));
         assertEquals(result.collectColumnValues("name"), Arrays.asList("Alex Cervais", "Martin Black", "Peter Bones", "Sandra Locke"));
     }
 
     @Test(groups = {"search", "cmis", "ejb"})
     public void selectAllWithJoin() throws FxApplicationException {
         final CmisResultSet result = getCmisSearchEngine().search(
                 "SELECT p.*, data.* FROM cmis_person AS p JOIN cmis_person_data AS data ON (p.ssn=data.ssn) ORDER BY name"
         );
         assertRowCount(result, 3);
         assertEquals(
                 Lists.newArrayList(Iterables.concat(
                         result.getColumnAliases().subList(0, 3),
                         result.getColumnAliases().subList(result.getRow(0).indexOf("entrydate") - 2, result.getRow(0).indexOf("entryDate") + 1)
                 )),
                 // TODO: table alias should be set in column alias
                 Arrays.asList("name", "ssn", "email", "ssn", "entrydate", "annualsalary")
         );
         assertEquals(result.collectColumnValues("name"), Arrays.asList("Martin Black", "Peter Bones", "Sandra Locke"));
         assertEquals(result.collectColumnValues("annualSalary"), Arrays.asList(75000L, 50000L, 85000L));
     }
 
     @Test(groups = {"search", "cmis", "ejb"})
     public void inSelectMany() throws FxApplicationException {
         final CmisResultSet result = getCmisSearchEngine().search(
                 "SELECT id, selectManySearchProp FROM SearchTest"
         );
         assertTrue(result.getRowCount() > 0);
         final List<FxSelectListItem> queryItems = new ArrayList<FxSelectListItem>();
         for (CmisResultRow row : result) {
             final List<FxSelectListItem> selected = ((SelectMany) row.getColumn(2).getValue()).getSelected();
             if (selected.size() > 2) {
                 queryItems.add(selected.get(0));
                 queryItems.add(selected.get(1));
                 if (getExpectedMatchRows(result, queryItems) == result.getRowCount()
                         || getExpectedPartialMatches(result, queryItems) == result.getRowCount()) {
                     // exact or partial match for this selection would match all rows - not suitable for a query test
                     queryItems.clear();
                 } else {
                     break;
                 }
             }
         }
         // count rows that contain both selected IDs
         final int matchingRows = getExpectedMatchRows(result, queryItems);
         final int partialMatchingRows = getExpectedPartialMatches(result, queryItems);
         assertFalse(queryItems.isEmpty(), "No suitable SelectMany instance found");
         assertTrue(matchingRows > 0);
 
         final CmisResultSet condResult = getCmisSearchEngine().search(
                 "SELECT id, selectManySearchProp FROM SearchTest WHERE selectManySearchProp IN ("
                         + StringUtils.join(FxSharedUtils.getSelectableObjectIdList(queryItems), ',') + ")"
         );
         assertEquals(condResult.getRowCount(), matchingRows, "Wrong result row count for query on " + queryItems
                 + ", result:\n" + formatResult(condResult)
         );
 
         // select other rows using NOT IN
         final CmisResultSet invResult = getCmisSearchEngine().search(
                 "SELECT id, selectManySearchProp FROM SearchTest WHERE selectManySearchProp NOT IN ("
                         + StringUtils.join(FxSharedUtils.getSelectableObjectIdList(queryItems), ',') + ")"
         );
         assertEquals(invResult.getRowCount(), result.getRowCount() - partialMatchingRows,
                 "Wrong result row count for NOT IN query on " + queryItems
                         + ", result:\n" + formatResult(invResult)
         );
         assertTrue(invResult.getRowCount() > 0, "NOT IN query returned no rows");
 
     }
 
     private int getExpectedPartialMatches(CmisResultSet result, final List<FxSelectListItem> queryItems) {
         return Iterables.size(Iterables.filter(result, new Predicate<CmisResultRow>() {
             public boolean apply(CmisResultRow row) {
                 final SelectMany selectMany = (SelectMany) row.getColumn(2).getValue();
                 return !Collections.disjoint(queryItems, selectMany.getSelected());
             }
         }));
     }
 
     private int getExpectedMatchRows(CmisResultSet result, final List<FxSelectListItem> queryItems) {
         return Iterables.size(Iterables.filter(result, new Predicate<CmisResultRow>() {
             public boolean apply(CmisResultRow row) {
                 final SelectMany selectMany = (SelectMany) row.getColumn(2).getValue();
                 return selectMany.getSelected().containsAll(queryItems);
             }
         }));
     }
 
     private void assertRowCount(CmisResultSet result, int expectedRows) {
         assertEquals(result.getRowCount(), expectedRows, "Expected " + expectedRows + " result row, got:\n" + formatResult(result));
     }
 
     private FxPK createContactData(String surname, String name) throws FxApplicationException {
         final FxPK pk = getContentEngine().initialize(FxType.CONTACTDATA)
                 .setValue("/surname", surname)
                 .setValue("/name", name)
                 .save().getPk();
         testInstances.add(pk);
         return pk;
     }
 
     private FxPK createArticle(String title, String text) throws FxApplicationException {
         final FxPK pk = getContentEngine().initialize("ARTICLE")
                 .setValue("/title", title)
                 .setValue("/longtext", text)
                 .save().getPk();
         testInstances.add(pk);
         return pk;
     }
 
     private String formatResult(CmisResultSet result) {
         return StringUtils.join(result.getRows(), "\n") + "\n";
     }
 
 }
