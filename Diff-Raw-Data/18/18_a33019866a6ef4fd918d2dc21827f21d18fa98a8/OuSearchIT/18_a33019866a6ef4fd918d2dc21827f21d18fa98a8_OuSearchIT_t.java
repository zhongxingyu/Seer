 /*
  * CDDL HEADER START
  *
  * The contents of this file are subject to the terms of the
  * Common Development and Distribution License, Version 1.0 only
  * (the "License").  You may not use this file except in compliance
  * with the License.
  *
  * You can obtain a copy of the license at license/ESCIDOC.LICENSE
  * or http://www.escidoc.de/license.
  * See the License for the specific language governing permissions
  * and limitations under the License.
  *
  * When distributing Covered Code, include this CDDL HEADER in each
  * file and include the License file at license/ESCIDOC.LICENSE.
  * If applicable, add the following below this CDDL HEADER, with the
  * fields enclosed by brackets "[]" replaced with your own identifying
  * information: Portions Copyright [yyyy] [name of copyright owner]
  *
  * CDDL HEADER END
  */
 
 /*
  * Copyright 2006-2008 Fachinformationszentrum Karlsruhe Gesellschaft
  * fuer wissenschaftlich-technische Information mbH and Max-Planck-
  * Gesellschaft zur Foerderung der Wissenschaft e.V.  
  * All rights reserved.  Use is subject to license terms.
  */
 package de.escidoc.core.test.sb;
 
 import de.escidoc.core.common.exceptions.remote.EscidocException;
 import de.escidoc.core.test.common.client.servlet.ClientBase;
 import de.escidoc.core.test.common.client.servlet.HttpHelper;
 import de.escidoc.core.test.security.client.PWCallback;
 import org.joda.time.DateTime;
 import org.joda.time.DateTimeZone;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.w3c.dom.Document;
 
 import java.util.HashMap;
 import java.util.Locale;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertTrue;
 import static org.junit.Assert.fail;
 
 /**
  * Test the implementation of the search resource.
  *
  * @author Michael Hoppe
  */
 public class OuSearchIT extends SearchTestBase {
 
     private static final Logger LOGGER = LoggerFactory.getLogger(OuSearchIT.class);
 
     private static String[] orgUnitIds = null;
 
     private static HashMap<Integer, String[]> parentIds = new HashMap<Integer, String[]>();
 
     private static int methodCounter = 0;
 
     private static final String INDEX_NAME = "escidocou_all";
 
     private static final int SLEEP_TIME = 5000;
 
     private static String startTime = "";
 
     /**
      * Set up servlet test.
      *
      * @throws Exception If anything fails.
      */
     @Before
     public void initialize() throws Exception {
         if (methodCounter == 0) {
             prepare();
         }
     }
 
     /**
      * Clean up after servlet test.
      *
      * @throws Exception If anything fails.
      */
     @After
     public void deinitialize() throws Exception {
         methodCounter++;
         if (methodCounter == getTestAnnotationsCount()) {
             methodCounter = 0;
             parentIds = new HashMap<Integer, String[]>();
         }
     }
 
     /**
      * insert org-unit(s) into system for the tests.
      *
      * @throws Exception If anything fails.
      */
     private void prepare() throws Exception {
         LOGGER.info("starting OUSearchTest at "
             + new DateTime(System.currentTimeMillis() + (60 * 60 * 1000), DateTimeZone.UTC).toString());
         // create empty index/////////////////////////////////////////////////
         String urlParameters =
             "?operation=updateIndex" + "&action=createEmpty" + "&repositoryName=escidocrepository" + "&indexName=";
 
         String httpUrl =
             getFrameworkUrl() + de.escidoc.core.test.common.client.servlet.Constants.FEDORAGSEARCH_BASE_URI
                 + urlParameters;
         HttpHelper.executeHttpRequest(de.escidoc.core.test.common.client.servlet.Constants.HTTP_METHOD_GET, httpUrl
             + INDEX_NAME, null, null, null);
         // ////////////////////////////////////////////////////////////////////
 
         startTime = new DateTime(System.currentTimeMillis() - (60 * 60 * 1000), DateTimeZone.UTC).toString();
         // Create Org-Units////////////////////////////////////////////////////
         orgUnitIds = new String[Constants.NUM_ORG_UNITS];
         for (int i = 0; i < Constants.NUM_ORG_UNITS; i++) {
             final String templateName = "escidoc_search_ou" + i + ".xml";
             final Document document = getTemplateAsDocument(TEMPLATE_SB_ORGANIZATIONAL_UNIT_PATH, templateName);
 
             String xml =
                 prepareOrgUnit(PWCallback.DEFAULT_HANDLE, document, ORGANIZATIONAL_UNIT_STATUS_OPENED, parentIds.get(i));
 
             writeVariables(i, xml);
         }
         // /////////////////////////////////////////////////////////////////////
     }
 
     /**
      * update org-unit(s) for the tests.
      *
      * @throws Exception If anything fails.
      */
     private void update() throws Exception {
         // UPDATE Org-Units////////////////////////////////////////////////////
         try {
             // close org-unit 6
             closeOrgUnit(orgUnitIds[6]);
 
             // update org-unit 0
             String xml = retrieve(ORGANIZATIONAL_UNIT_HANDLER_CODE, orgUnitIds[0]);
             xml = xml.replaceAll("Hierarchy Top Level", "Hierarchy Top Level updated ");
             update(ORGANIZATIONAL_UNIT_HANDLER_CODE, orgUnitIds[0], xml);
 
             // update metadata of org-unit 1
             xml = retrieve(ORGANIZATIONAL_UNIT_HANDLER_CODE, orgUnitIds[1]);
             xml = xml.replaceAll("Munich", "London");
             update(ORGANIZATIONAL_UNIT_HANDLER_CODE, orgUnitIds[1], xml);
 
         }
         catch (final Exception e) {
             LOGGER.error("", e);
         }
         // /////////////////////////////////////////////////////////////////////
     }
 
     /**
      * prepare parentIds for tree-structure of test-org-units. write orgUnitid as variable.
      *
      * @param i   number of org-unit.
      * @param xml org-unit-xml.
      */
     private void writeVariables(final int i, final String xml) {
         String id = getId(xml);
         orgUnitIds[i] = id;
         switch (i) {
             case 0:
                 parentIds.put(1, new String[] { id });
                 parentIds.put(2, new String[] { id });
                 break;
             case 2:
                 parentIds.put(3, new String[] { orgUnitIds[1] });
                 parentIds.put(4, new String[] { orgUnitIds[1] });
                 parentIds.put(5, new String[] { orgUnitIds[1], id });
                 parentIds.put(6, new String[] { id });
                 break;
             default:
                 break;
         }
     }
 
     /**
      * explain operation without parameters for existing database xyz.
      *
      * @throws Exception If anything fails.
      */
     @Test
     public void testSBOUEX1() throws Exception {
         HashMap<String, String[]> parameters = new HashMap<String, String[]>();
         String response = explain(parameters, INDEX_NAME);
         assertXmlValidExplainPlan(response);
         assertEquals("srw/search/escidocou_all", getDatabase(response));
         assertEquals(Constants.OU_INDEX_FIELD_COUNT, getIndexFieldCount(response));
         assertEquals(Constants.OU_SORT_FIELD_COUNT, getSortFieldCount(response));
     }
 
     /**
      * explain operation where operation=explain for existing database xyz is explicitly given.
      *
      * @throws Exception If anything fails.
      */
     @Test
     public void testSBOUEX2() throws Exception {
         HashMap<String, String[]> parameters = new HashMap<String, String[]>();
         parameters.put(FILTER_PARAMETER_OPERATION, new String[] { FILTER_PARAMETER_EXPLAIN });
         String response = explain(parameters, INDEX_NAME);
         assertXmlValidExplainPlan(response);
         assertEquals("srw/search/escidocou_all", getDatabase(response));
         assertEquals(Constants.OU_INDEX_FIELD_COUNT, getIndexFieldCount(response));
         assertEquals(Constants.OU_SORT_FIELD_COUNT, getSortFieldCount(response));
     }
 
     /**
      * Test searching for a single term.
      *
      * @throws Exception If anything fails.
      */
     @Test
     public void testSBOUSR1() throws Exception {
 
         HashMap<String, String> parameters = new HashMap<String, String>();
         parameters.put(FILTER_PARAMETER_QUERY, "escidoc.metadata=mitte-rechts");
         String response = search(parameters, INDEX_NAME);
         assertXmlValidSearchResult(response);
         assertEquals(true, checkHighlighting(response));
         assertEquals("1", getNumberOfHits(response));
         assertEquals("1", getFirstRecord(response));
     }
 
     /**
      * Database not existing.
      *
      * @throws Exception If anything fails.
      */
     @Test(expected = EscidocException.class)
     public void testSBOUSR2() throws Exception {
         HashMap<String, String> parameters = new HashMap<String, String>();
         parameters.put(FILTER_PARAMETER_QUERY, "escidoc.metadata=mitte-rechts");
         search(parameters, "escidoc_fault");
     }
 
     /**
      * Request parameter startRecord \u2013 (1).
      *
      * @throws Exception If anything fails.
      */
     @Test
     public void testSBOUSR3() throws Exception {
         HashMap<String, String> parameters = new HashMap<String, String>();
         parameters.put(FILTER_PARAMETER_QUERY, "escidoc.metadata=orgunit");
         parameters.put(FILTER_PARAMETER_STARTRECORD, "3");
         String response = search(parameters, INDEX_NAME);
         assertXmlValidSearchResult(response);
         assertEquals("3", getFirstRecord(response));
         assertEquals(true, checkHighlighting(response));
     }
 
     /**
      * Request parameter startrecord – (2).
      *
      * @throws Exception If anything fails.
      */
     @Test
     public void testSBOUSR4() throws Exception {
         HashMap<String, String> parameters = new HashMap<String, String>();
         parameters.put(FILTER_PARAMETER_QUERY, "escidoc.metadata=orgunit");
         parameters.put(FILTER_PARAMETER_STARTRECORD, "0");
         String response = search(parameters, INDEX_NAME);
         assertXmlValidSearchResult(response);
         assertEquals(FILTER_PARAMETER_STARTRECORD, getDiagnostics(response));
     }
 
     /**
      * Request parameter maximumRecords – (1).
      *
      * @throws Exception If anything fails.
      */
     @Test
     public void testSBOUSR5() throws Exception {
         HashMap<String, String> parameters = new HashMap<String, String>();
         parameters.put(FILTER_PARAMETER_QUERY, "escidoc.metadata=orgunit");
         parameters.put(FILTER_PARAMETER_MAXIMUMRECORDS, "1");
         String response = search(parameters, INDEX_NAME);
         assertXmlValidSearchResult(response);
         assertEquals("2", getNextRecordPosition(response));
         assertEquals(Integer.toString(Constants.NUM_ORG_UNITS), getNumberOfHits(response));
         assertEquals(true, checkHighlighting(response));
     }
 
     /**
      * Request parameter maximumRecords – (2).
      *
      * @throws Exception If anything fails.
      */
     @Test
     public void testSBOUSR6() throws Exception {
         HashMap<String, String> parameters = new HashMap<String, String>();
         parameters.put(FILTER_PARAMETER_QUERY, "escidoc.metadata=orgunit");
         parameters.put(FILTER_PARAMETER_MAXIMUMRECORDS, "1");
         String response = search(parameters, INDEX_NAME);
         assertXmlValidSearchResult(response);
         assertEquals("2", getNextRecordPosition(response));
         assertEquals(Integer.toString(Constants.NUM_ORG_UNITS), getNumberOfHits(response));
     }
 
     /**
      * Request parameter maximumRecords – (3).
      *
      * @throws Exception If anything fails.
      */
     @Test
     public void testSBOUSR7() throws Exception {
         HashMap<String, String> parameters = new HashMap<String, String>();
         parameters.put(FILTER_PARAMETER_QUERY, "escidoc.metadata=orgunit");
         parameters.put(FILTER_PARAMETER_MAXIMUMRECORDS, "-1");
         String response = search(parameters, INDEX_NAME);
         assertXmlValidSearchResult(response);
         assertEquals(FILTER_PARAMETER_MAXIMUMRECORDS, getDiagnostics(response));
     }
 
     /**
      * Request parameter recordPacking – (1).
      *
      * @throws Exception If anything fails.
      */
     @Test
     public void testSBOUSR8() throws Exception {
         HashMap<String, String> parameters = new HashMap<String, String>();
         parameters.put(FILTER_PARAMETER_QUERY, "escidoc.metadata=orgunit");
         parameters.put(FILTER_PARAMETER_RECORDPACKING, "xml");
         String response = search(parameters, INDEX_NAME);
         assertXmlValidSearchResult(response);
         assertEquals(null, getDiagnostics(response));
         assertEquals(true, checkHighlighting(response));
     }
 
     /**
      * Request parameter recordPacking – (2).
      *
      * @throws Exception If anything fails.
      */
     @Test
     public void testSBOUSR9() throws Exception {
         HashMap<String, String> parameters = new HashMap<String, String>();
         parameters.put(FILTER_PARAMETER_QUERY, "escidoc.metadata=orgunit");
         parameters.put(FILTER_PARAMETER_RECORDPACKING, "string");
         String response = search(parameters, INDEX_NAME);
         assertEquals(null, getDiagnostics(response));
         assertEquals(true, checkHighlighting(response));
     }
 
     /**
      * Request parameter recordPacking – (3).
      *
      * @throws Exception If anything fails.
      */
     @Test
     public void testSBOUSR10() throws Exception {
         HashMap<String, String> parameters = new HashMap<String, String>();
         parameters.put(FILTER_PARAMETER_QUERY, "escidoc.metadata=orgunit");
         parameters.put(FILTER_PARAMETER_RECORDPACKING, "something");
         String response = search(parameters, INDEX_NAME);
         assertXmlValidSearchResult(response);
         assertEquals("something", getDiagnostics(response));
     }
 
     /**
      * Request parameter recordSchema – (1).
      *
      * @throws Exception If anything fails.
      */
     @Test
     public void testSBOUSR11() throws Exception {
         HashMap<String, String> parameters = new HashMap<String, String>();
         parameters.put(FILTER_PARAMETER_QUERY, "escidoc.metadata=mitte-rechts");
         parameters.put(FILTER_PARAMETER_RECORDSCHEMA, "default");
         String response = search(parameters, INDEX_NAME);
         assertXmlValidSearchResult(response);
         assertEquals("1", getNumberOfHits(response));
         assertEquals(true, checkHighlighting(response));
     }
 
     /**
      * Request parameter recordSchema – (2).
      *
      * @throws Exception If anything fails.
      */
     @Test
     public void testSBOUSR12() throws Exception {
         HashMap<String, String> parameters = new HashMap<String, String>();
         parameters.put(FILTER_PARAMETER_QUERY, "escidoc.metadata=mitte-rechts");
         parameters.put(FILTER_PARAMETER_RECORDSCHEMA, "none");
         String response = search(parameters, INDEX_NAME);
         assertXmlValidSearchResult(response);
         assertEquals("none", getDiagnostics(response));
     }
 
     /**
      * Request parameter sortKeys – (1).
      *
      * @throws Exception If anything fails.
      */
     @Test
     public void testSBOUSR13() throws Exception {
         HashMap<String, String> parameters = new HashMap<String, String>();
         parameters.put(FILTER_PARAMETER_QUERY, "escidoc.metadata=orgunit");
         parameters.put(FILTER_PARAMETER_SORTKEYS, "sort.escidoc.identifier");
         String response = search(parameters, INDEX_NAME);
         assertXmlValidSearchResult(response);
         assertEquals(true, checkHighlighting(response));
         response = response.replaceAll("<search-result:highlight.*?</search-result:highlight>", "");
         String[] splitStr = response.split("<dc:identifier.*?>");
         String[] stringsToCheck = { "1-01-01", "2-01-01", "2-02-01", "3-01-01", "3-02-01", "3-03-02", "3-04-01" };
         int j = 0;
         for (int i = 1; i < splitStr.length; i++) {
             String splitPart = splitStr[i];
             if (!splitPart.matches("(?s)[^0-9]*" + stringsToCheck[j] + ".*")) {
                 j++;
                 if (!splitPart.matches("(?s)[^0-9]*" + stringsToCheck[j] + ".*")) {
                     assertTrue("wrong sortorder", false);
                 }
             }
         }
     }
 
     /**
      * Request parameter sortKeys – (2).
      *
      * @throws Exception If anything fails.
      */
     @Test
     public void testSBOUSR14() throws Exception {
         HashMap<String, String> parameters = new HashMap<String, String>();
         parameters.put(FILTER_PARAMETER_QUERY, "escidoc.metadata=orgunit");
         parameters.put(FILTER_PARAMETER_SORTKEYS, "sort.escidoc.organization-type,,0 sort.escidoc.identifier");
         String response = search(parameters, INDEX_NAME);
         assertXmlValidSearchResult(response);
         assertEquals(true, checkHighlighting(response));
         response = response.replaceAll("<search-result:highlight.*?</search-result:highlight>", "");
        String[] records = response.split("<[^\\/>]*?:record>");
         String[] valuesToCheck =
             { "", "workgroup|3-01-01", "workgroup|3-02-01", "workgroup|3-03-02", "workgroup|3-04-01",
                 "society|1-01-01", "institute|2-01-01", "institute|2-02-01" };
         assertEquals(records.length, valuesToCheck.length);
         for (int i = 1; i < records.length; i++) {
             String[] parts = valuesToCheck[i].split("\\|");
             if (!records[i].matches("(?s).*<.*?organization-type.*?>" + parts[0] + ".*")
                 || !records[i].toLowerCase(Locale.ENGLISH).matches(
                     "(?s).*<.*?identifier.*?>" + parts[1].toLowerCase(Locale.ENGLISH) + ".*")) {
                 assertTrue("wrong sortorder", false);
 
             }
 
         }
     }
 
     /**
      * Request parameter sortKeys – (3).
      *
      * @throws Exception If anything fails.
      */
     @Test
     public void testSBOUSR15() throws Exception {
         HashMap<String, String> parameters = new HashMap<String, String>();
         parameters.put(FILTER_PARAMETER_QUERY, "escidoc.metadata=orgunit");
         parameters.put(FILTER_PARAMETER_SORTKEYS, "sort.escidoc.sonstwas,,0");
         String response = search(parameters, INDEX_NAME);
         assertXmlValidSearchResult(response);
         assertEquals("7", getNumberOfHits(response));
     }
 
     /**
      * : Request parameter stylesheet – (1).
      *
      * @throws Exception If anything fails.
      */
     @Test
     public void testSBOUSR16() throws Exception {
         HashMap<String, String> parameters = new HashMap<String, String>();
         parameters.put(FILTER_PARAMETER_QUERY, "escidoc.metadata=orgunit");
         parameters.put(FILTER_PARAMETER_STYLESHEET, getFrameworkUrl() + "/srw/searchRetrieveResponse.xsl");
         String response = search(parameters, INDEX_NAME);
         assertXmlValidSearchResult(response);
         assertEquals(Integer.toString(Constants.NUM_ORG_UNITS), getNumberOfHits(response));
         assertEquals(true, checkHighlighting(response));
     }
 
     /**
      * Request parameter stylesheet – (2).
      *
      * @throws Exception If anything fails.
      */
     @Test
     public void testSBOUSR17() throws Exception {
         HashMap<String, String> parameters = new HashMap<String, String>();
         parameters.put(FILTER_PARAMETER_QUERY, "escidoc.metadata=orgunit");
         parameters.put(FILTER_PARAMETER_STYLESHEET, getFrameworkUrl() + "/srw/xyz.xsl");
         String response = search(parameters, INDEX_NAME);
         assertXmlValidSearchResult(response);
         assertEquals(Integer.toString(Constants.NUM_ORG_UNITS), getNumberOfHits(response));
         assertEquals(true, checkHighlighting(response));
     }
 
     /**
      * Boolean Operator AND.
      *
      * @throws Exception If anything fails.
      */
     @Test
     public void testSBOUSR18() throws Exception {
         HashMap<String, String> parameters = new HashMap<String, String>();
         parameters.put(FILTER_PARAMETER_QUERY, "escidoc.metadata all \"type institute\"");
         String response = search(parameters, INDEX_NAME);
         assertEquals("2", getNumberOfHits(response));
         parameters = new HashMap<String, String>();
         parameters.put(FILTER_PARAMETER_QUERY, "escidoc.metadata=type and escidoc.metadata=institute");
         response = search(parameters, INDEX_NAME);
         assertXmlValidSearchResult(response);
         assertEquals("2", getNumberOfHits(response));
         assertEquals(true, checkHighlighting(response));
     }
 
     /**
      * Boolean Operator OR .
      *
      * @throws Exception If anything fails.
      */
     @Test
     public void testSBOUSR19() throws Exception {
         HashMap<String, String> parameters = new HashMap<String, String>();
         parameters.put(FILTER_PARAMETER_QUERY, "escidoc.metadata any \"type institute\"");
         String response = search(parameters, INDEX_NAME);
         assertEquals(Integer.toString(Constants.NUM_ORG_UNITS), getNumberOfHits(response));
         parameters = new HashMap<String, String>();
         parameters.put(FILTER_PARAMETER_QUERY, "escidoc.metadata=type or escidoc.metadata=institute");
         response = search(parameters, INDEX_NAME);
         assertXmlValidSearchResult(response);
         assertEquals(Integer.toString(Constants.NUM_ORG_UNITS), getNumberOfHits(response));
         assertEquals(true, checkHighlighting(response));
     }
 
     /**
      * Use of * as Wildcard (1).
      *
      * @throws Exception If anything fails.
      */
     @Test
     public void testSBOUSR20() throws Exception {
         // Left truncation not supported
     }
 
     /**
      * Use of * as Wildcard (2).
      *
      * @throws Exception If anything fails.
      */
     @Test
     public void testSBOUSR21() throws Exception {
         HashMap<String, String> parameters = new HashMap<String, String>();
         parameters.put(FILTER_PARAMETER_QUERY, "escidoc.metadata=or*ni*");
         String response = search(parameters, INDEX_NAME);
         assertXmlValidSearchResult(response);
         assertEquals(Integer.toString(Constants.NUM_ORG_UNITS), getNumberOfHits(response));
         assertEquals(true, checkHighlighting(response));
     }
 
     /**
      * Use of * as Wildcard (3).
      *
      * @throws Exception If anything fails.
      */
     @Test
     public void testSBOUSR22() throws Exception {
         HashMap<String, String> parameters = new HashMap<String, String>();
         parameters.put(FILTER_PARAMETER_QUERY, "escidoc.metadata=org*");
         String response = search(parameters, INDEX_NAME);
         assertXmlValidSearchResult(response);
         assertEquals(Integer.toString(Constants.NUM_ORG_UNITS), getNumberOfHits(response));
         assertEquals(true, checkHighlighting(response));
     }
 
     /**
      * Use of ? as Wildcard (1).
      *
      * @throws Exception If anything fails.
      */
     @Test
     public void testSBOUSR23() throws Exception {
         // Left truncation not supported
     }
 
     /**
      * Use of ? as Wildcard (2).
      *
      * @throws Exception If anything fails.
      */
     @Test
     public void testSBOUSR24() throws Exception {
         HashMap<String, String> parameters = new HashMap<String, String>();
         parameters.put(FILTER_PARAMETER_QUERY, "escidoc.metadata=wo??gro?p");
         String response = search(parameters, INDEX_NAME);
         assertXmlValidSearchResult(response);
         assertEquals("4", getNumberOfHits(response));
         assertEquals(true, checkHighlighting(response));
     }
 
     /**
      * Use of ? as Wildcard (3) .
      *
      * @throws Exception If anything fails.
      */
     @Test
     public void testSBOUSR25() throws Exception {
         HashMap<String, String> parameters = new HashMap<String, String>();
         parameters.put(FILTER_PARAMETER_QUERY, "escidoc.metadata=workgrou?");
         String response = search(parameters, INDEX_NAME);
         assertXmlValidSearchResult(response);
         assertEquals("4", getNumberOfHits(response));
         assertEquals(true, checkHighlighting(response));
     }
 
     /**
      * Fuzzy Search.
      *
      * @throws Exception If anything fails.
      */
     @Test
     public void testSBOUSR26() throws Exception {
         HashMap<String, String> parameters = new HashMap<String, String>();
         parameters.put(FILTER_PARAMETER_QUERY, "escidoc.metadata=/fuzzy werkgrup");
         String response = search(parameters, INDEX_NAME);
         assertXmlValidSearchResult(response);
         assertEquals("4", getNumberOfHits(response));
         assertEquals(true, checkHighlighting(response));
     }
 
     /**
      * : Umlaut as request-parameter .
      *
      * @throws Exception If anything fails.
      */
     @Test
     public void testSBOUSR27() throws Exception {
         HashMap<String, String> parameters = new HashMap<String, String>();
         parameters.put(FILTER_PARAMETER_QUERY, new String("escidoc.metadata=t\u00fcbingen"
             .getBytes(ClientBase.DEFAULT_CHARSET), ClientBase.DEFAULT_CHARSET));
         String response = search(parameters, INDEX_NAME);
         assertXmlValidSearchResult(response);
         assertEquals("1", getNumberOfHits(response));
         assertEquals(true, checkHighlighting(response));
     }
 
     /**
      * : Availability of any.. indexes for org-units.
      *
      * @throws Exception If anything fails.
      */
     @Test
     public void testSBOUSR28() throws Exception {
         Pattern orgUnitIdPattern = Pattern.compile("(.*)\\$\\{ORGUNIT(.*?)\\}(.*)");
         Matcher orgUnitIdMatcher = orgUnitIdPattern.matcher("");
         HashMap<String, String> parameters = new HashMap<String, String>();
         for (String indexName : Constants.ORG_UNIT_INDEX_USERDEFINED_SEARCHES.keySet()) {
             HashMap<String, String> info = Constants.ORG_UNIT_INDEX_USERDEFINED_SEARCHES.get(indexName);
             orgUnitIdMatcher.reset(info.get("searchString"));
             String searchString = info.get("searchString");
             if (orgUnitIdMatcher.matches()) {
                 int indexNum = new Integer(orgUnitIdMatcher.group(2));
                 searchString = orgUnitIdMatcher.replaceAll("$1" + orgUnitIds[indexNum] + "$3");
             }
             String expectedHits = info.get("expectedHits");
             parameters.put(FILTER_PARAMETER_QUERY, searchString);
             String response = search(parameters, INDEX_NAME);
             assertXmlValidSearchResult(response);
             assertEquals("expected " + expectedHits + " for " + indexName + " but was " + getNumberOfHits(response),
                 expectedHits, getNumberOfHits(response));
         }
     }
 
     /**
      * : Availability of properties indexes for org-units.
      *
      * @throws Exception If anything fails.
      */
     @Test
     public void testSBOUSR29() throws Exception {
         Pattern orgUnitIdPattern = Pattern.compile("(.*)\\$\\{ORGUNIT(.*?)\\}(.*)");
         Matcher orgUnitIdMatcher = orgUnitIdPattern.matcher("");
         HashMap<String, String> parameters = new HashMap<String, String>();
         for (String indexName : Constants.ORG_UNIT_INDEX_PROPERTIES_SEARCHES.keySet()) {
             HashMap<String, String> info = Constants.ORG_UNIT_INDEX_PROPERTIES_SEARCHES.get(indexName);
             orgUnitIdMatcher.reset(info.get("searchString"));
             String searchString = info.get("searchString");
             if (orgUnitIdMatcher.matches()) {
                 int indexNum = new Integer(orgUnitIdMatcher.group(2));
                 searchString = orgUnitIdMatcher.replaceAll("$1" + orgUnitIds[indexNum] + "$3");
             }
             String expectedHits = info.get("expectedHits");
             parameters.put(FILTER_PARAMETER_QUERY, searchString);
             String response = search(parameters, INDEX_NAME);
             assertXmlValidSearchResult(response);
             assertEquals("expected " + expectedHits + " for " + indexName + " but was " + getNumberOfHits(response),
                 expectedHits, getNumberOfHits(response));
         }
     }
 
     /**
      * Test searching for ISSN in escidoc.metadata 1.
      *
      * @throws Exception If anything fails.
      */
     @Test
     public void testSBOUSR30() throws Exception {
         HashMap<String, String> parameters = new HashMap<String, String>();
         parameters.put(FILTER_PARAMETER_QUERY, "escidoc.metadata=ISSN");
         String response = search(parameters, INDEX_NAME);
         assertXmlValidSearchResult(response);
         assertEquals(Integer.toString(Constants.NUM_ORG_UNITS), getNumberOfHits(response));
     }
 
     /**
      * Test searching for ISSN in escidoc.metadata 2.
      *
      * @throws Exception If anything fails.
      */
     @Test
     public void testSBOUSR31() throws Exception {
         HashMap<String, String> parameters = new HashMap<String, String>();
         parameters.put(FILTER_PARAMETER_QUERY, "escidoc.metadata=ISSN*");
         String response = search(parameters, INDEX_NAME);
         assertXmlValidSearchResult(response);
         assertEquals(Integer.toString(Constants.NUM_ORG_UNITS), getNumberOfHits(response));
     }
 
     /**
      * Test searching for ISSN in escidoc.metadata 3.
      *
      * @throws Exception If anything fails.
      */
     @Test
     public void testSBOUSR32() throws Exception {
         HashMap<String, String> parameters = new HashMap<String, String>();
         parameters.put(FILTER_PARAMETER_QUERY, "escidoc.metadata all \"ISSN 2-*\"");
         String response = search(parameters, INDEX_NAME);
         assertXmlValidSearchResult(response);
         assertEquals("2", getNumberOfHits(response));
     }
 
     /**
      * Test searching for ISSN in escidoc.any-identifier 1.
      *
      * @throws Exception If anything fails.
      */
     @Test
     public void testSBOUSR33() throws Exception {
         HashMap<String, String> parameters = new HashMap<String, String>();
         parameters.put(FILTER_PARAMETER_QUERY, "escidoc.any-identifier=ISSN:2-*");
         String response = search(parameters, INDEX_NAME);
         assertXmlValidSearchResult(response);
         assertEquals("2", getNumberOfHits(response));
     }
 
     /**
      * Test searching for ISSN in escidoc.any-identifier 2.
      *
      * @throws Exception If anything fails.
      */
     @Test
     public void testSBOUSR34() throws Exception {
         HashMap<String, String> parameters = new HashMap<String, String>();
         parameters.put(FILTER_PARAMETER_QUERY, "escidoc.any-identifier=ISSN:2-01-01");
         String response = search(parameters, INDEX_NAME);
         assertXmlValidSearchResult(response);
         assertEquals("1", getNumberOfHits(response));
     }
 
     /**
      * Test searching for ISSN in escidoc.any-identifier 3.
      *
      * @throws Exception If anything fails.
      */
     @Test
     public void testSBOUSR35() throws Exception {
         HashMap<String, String> parameters = new HashMap<String, String>();
         parameters.put(FILTER_PARAMETER_QUERY, "escidoc.any-identifier=ISSN*");
         String response = search(parameters, INDEX_NAME);
         assertXmlValidSearchResult(response);
         assertEquals(Integer.toString(Constants.NUM_ORG_UNITS), getNumberOfHits(response));
     }
 
     /**
      * Test searching for updated OrgUnits.
      *
      * @throws Exception If anything fails.
      */
     @Test
     public void testSBOUSR36() throws Exception {
         HashMap<String, String> parameters = new HashMap<String, String>();
         parameters.put(FILTER_PARAMETER_QUERY, "escidoc.public-status=" + ORGANIZATIONAL_UNIT_STATUS_CLOSED);
         String response = search(parameters, INDEX_NAME);
         assertXmlValidSearchResult(response);
         assertEquals("0", getNumberOfHits(response));
 
         parameters = new HashMap<String, String>();
         parameters.put(FILTER_PARAMETER_QUERY, "escidoc.metadata=updated");
         response = search(parameters, INDEX_NAME);
         assertXmlValidSearchResult(response);
         assertEquals("0", getNumberOfHits(response));
 
         update();
     }
 
     /**
      * Test searching for updated OrgUnits.
      *
      * @throws Exception If anything fails.
      */
     @Test
     public void testSBOUSR37() throws Exception {
         HashMap<String, String> parameters = new HashMap<String, String>();
         parameters.put(FILTER_PARAMETER_QUERY, "escidoc.public-status=" + ORGANIZATIONAL_UNIT_STATUS_CLOSED);
         String response = search(parameters, INDEX_NAME);
         assertXmlValidSearchResult(response);
         assertEquals("1", getNumberOfHits(response));
 
         parameters = new HashMap<String, String>();
         parameters.put(FILTER_PARAMETER_QUERY, "escidoc.metadata=updated");
         response = search(parameters, INDEX_NAME);
         assertXmlValidSearchResult(response);
         assertEquals("1", getNumberOfHits(response));
 
         parameters = new HashMap<String, String>();
         parameters.put(FILTER_PARAMETER_QUERY, "escidoc.metadata=london");
         response = search(parameters, INDEX_NAME);
         assertXmlValidSearchResult(response);
         assertEquals("1", getNumberOfHits(response));
 
     }
 
     /**
      * : Test searching for dates.
      *
      * @throws Exception If anything fails.
      */
     @Test
     public void testSBOUSR38() throws Exception {
         HashMap<String, String> parameters = new HashMap<String, String>();
         parameters.put(FILTER_PARAMETER_QUERY, "escidoc.creation-date>\"" + startTime + "\"");
         String response = search(parameters, INDEX_NAME);
         assertXmlValidSearchResult(response);
         assertEquals(Integer.toString(Constants.NUM_ORG_UNITS), getNumberOfHits(response));
     }
 
     /**
      * Operation without optional parameters.
      *
      * @throws Exception If anything fails.
      */
     @Test
     public void testSBOUSC1() throws Exception {
         HashMap<String, String> parameters = new HashMap<String, String>();
         parameters.put(FILTER_PARAMETER_OPERATION, FILTER_PARAMETER_SCAN);
         parameters.put(FILTER_PARAMETER_SCAN_CLAUSE, "escidoc.metadata=orgunit");
         String response = scan(parameters, INDEX_NAME);
         assertXmlValidSearchResult(response);
         assertEquals(null, getDiagnostics(response));
     }
 
     /**
      * Operation with parameterresponse position (1).
      *
      * @throws Exception If anything fails.
      */
     @Test
     public void testSBOUSC2() throws Exception {
         // parameter responsePosition is ignored by SRW-Server
         HashMap<String, String> parameters = new HashMap<String, String>();
         parameters.put(FILTER_PARAMETER_OPERATION, FILTER_PARAMETER_SCAN);
         parameters.put(FILTER_PARAMETER_SCAN_CLAUSE, "escidoc.metadata=orgunit");
         parameters.put(FILTER_PARAMETER_RESPONSEPOSITION, "0");
         String response = scan(parameters, INDEX_NAME);
         assertXmlValidSearchResult(response);
         assertEquals(null, getDiagnostics(response));
     }
 
     /**
      * Operation with parameterresponse position (2).
      *
      * @throws Exception If anything fails.
      */
     @Test
     public void testSBOUSC3() throws Exception {
         // parameter responsePosition is ignored by SRW-Server
         HashMap<String, String> parameters = new HashMap<String, String>();
         parameters.put(FILTER_PARAMETER_OPERATION, FILTER_PARAMETER_SCAN);
         parameters.put(FILTER_PARAMETER_SCAN_CLAUSE, "escidoc.metadata=orgunit");
         parameters.put(FILTER_PARAMETER_RESPONSEPOSITION, "1");
         String response = scan(parameters, INDEX_NAME);
         assertXmlValidSearchResult(response);
         assertEquals(null, getDiagnostics(response));
     }
 
     /**
      * Operation with parameterresponse position (3).
      *
      * @throws Exception If anything fails.
      */
     @Test
     public void testSBOUSC4() throws Exception {
         HashMap<String, String> parameters = new HashMap<String, String>();
         parameters.put(FILTER_PARAMETER_OPERATION, FILTER_PARAMETER_SCAN);
         parameters.put(FILTER_PARAMETER_SCAN_CLAUSE, "escidoc.metadata=orgunit");
         parameters.put(FILTER_PARAMETER_RESPONSEPOSITION, "-2");
         String response = scan(parameters, INDEX_NAME);
         assertXmlValidSearchResult(response);
         assertEquals(FILTER_PARAMETER_RESPONSEPOSITION, getDiagnostics(response));
     }
 
     /**
      * Operation with parametermaximum terms (1).
      *
      * @throws Exception If anything fails.
      */
     @Test
     public void testSBOUSC5() throws Exception {
         // parameter maximumTerms is ignored by SRW-Server
         HashMap<String, String> parameters = new HashMap<String, String>();
         parameters.put(FILTER_PARAMETER_OPERATION, FILTER_PARAMETER_SCAN);
         parameters.put(FILTER_PARAMETER_SCAN_CLAUSE, "escidoc.metadata=orgunit");
         parameters.put(FILTER_PARAMETER_MAXIMUMTERMS, "4");
         String response = scan(parameters, INDEX_NAME);
         assertXmlValidSearchResult(response);
         assertEquals(null, getDiagnostics(response));
     }
 
     /**
      * Operation with parametermaximum terms (2).
      *
      * @throws Exception If anything fails.
      */
     @Test
     public void testSBOUSC6() throws Exception {
         HashMap<String, String> parameters = new HashMap<String, String>();
         parameters.put(FILTER_PARAMETER_OPERATION, FILTER_PARAMETER_SCAN);
         parameters.put(FILTER_PARAMETER_SCAN_CLAUSE, "escidoc.metadata=orgunit");
         parameters.put(FILTER_PARAMETER_MAXIMUMTERMS, "-2");
         String response = scan(parameters, INDEX_NAME);
         assertXmlValidSearchResult(response);
         assertEquals(FILTER_PARAMETER_MAXIMUMTERMS, getDiagnostics(response));
     }
 
     /**
      * Operation with parameter stylesheet (1).
      *
      * @throws Exception If anything fails.
      */
     @Test
     public void testSBOUSC7() throws Exception {
         HashMap<String, String> parameters = new HashMap<String, String>();
         parameters.put(FILTER_PARAMETER_OPERATION, FILTER_PARAMETER_SCAN);
         parameters.put(FILTER_PARAMETER_SCAN_CLAUSE, "escidoc.metadata=orgunit");
         parameters.put(FILTER_PARAMETER_STYLESHEET, getFrameworkUrl() + "/srw/scanResponse.xsl");
         String response = scan(parameters, INDEX_NAME);
         assertXmlValidSearchResult(response);
         assertEquals(null, getDiagnostics(response));
     }
 
     /**
      * Operation with parameter stylesheet (2).
      *
      * @throws Exception If anything fails.
      */
     @Test
     public void testSBOUSC8() throws Exception {
         HashMap<String, String> parameters = new HashMap<String, String>();
         parameters.put(FILTER_PARAMETER_OPERATION, FILTER_PARAMETER_SCAN);
         parameters.put(FILTER_PARAMETER_SCAN_CLAUSE, "escidoc.metadata=orgunit");
         parameters.put(FILTER_PARAMETER_STYLESHEET, getFrameworkUrl() + "/srw/yxr.xsl");
         String response = scan(parameters, INDEX_NAME);
         assertXmlValidSearchResult(response);
         assertEquals(null, getDiagnostics(response));
     }
 
 }
