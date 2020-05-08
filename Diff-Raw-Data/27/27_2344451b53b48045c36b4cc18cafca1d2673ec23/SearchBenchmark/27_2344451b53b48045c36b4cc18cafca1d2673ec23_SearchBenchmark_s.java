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
 package com.flexive.tests.embedded.benchmark;
 
 import com.flexive.shared.*;
 import com.flexive.shared.cmis.search.CmisResultSet;
 import com.flexive.shared.cmis.search.CmisResultRow;
 import static com.flexive.shared.EJBLookup.getContentEngine;
 import com.flexive.shared.content.FxContent;
 import com.flexive.shared.structure.*;
 import com.flexive.shared.search.query.SqlQueryBuilder;
 import com.flexive.shared.search.query.PropertyValueComparator;
 import com.flexive.shared.search.FxResultSet;
 import com.flexive.shared.search.FxResultRow;
 import com.flexive.shared.value.FxString;
 import com.flexive.shared.exceptions.*;
 import com.flexive.shared.tree.FxTreeNodeEdit;
 import com.flexive.shared.tree.FxTreeMode;
 import com.flexive.shared.tree.FxTreeNode;
 import com.flexive.tests.embedded.TestUsers;
 import com.flexive.tests.embedded.FxTestUtils;
 import static com.flexive.tests.embedded.benchmark.FxBenchmarkUtils.getResultLogger;
 import static com.flexive.tests.embedded.FxTestUtils.login;
 import org.testng.annotations.Test;
 import org.testng.annotations.DataProvider;
 import static org.testng.Assert.*;
 import org.apache.commons.lang.math.RandomUtils;
 
 import java.util.List;
 import java.util.Date;
 import java.util.Arrays;
 
 /**
  * Some benchmarks for the {@link com.flexive.shared.interfaces.SearchEngine}.
  *
  * @author Daniel Lichtenberger (daniel.lichtenberger@flexive.com), UCS - unique computing solutions gmbh (http://www.ucs.at)
  * @version $Rev$
  */
 @Test(groups = "benchmark", enabled = true)
 public class SearchBenchmark {
     private static final String TYPE_VOLUME = "dataVolumeTest";
 
    public void selectTreePathsBenchmark() throws FxApplicationException, FxLoginFailedException, FxAccountInUseException, FxLogoutFailedException {
         final int numNodes = 2000;
         long rootNode = -1;
         try {
             FxTestUtils.login(TestUsers.SUPERVISOR);
             // create a lot of nodes
             long startCreateNode = System.currentTimeMillis();
             rootNode = EJBLookup.getTreeEngine().save(FxTreeNodeEdit.createNew("selectTreePathsBenchmark"));
             for (int i = 0; i < numNodes; i++) {
                 final FxString label = new FxString(FxLanguage.ENGLISH, "English label " + i).setTranslation(FxLanguage.GERMAN, "Deutsches Label " + i);
                 EJBLookup.getTreeEngine().save(FxTreeNodeEdit.createNew("test test test " + i)
                         .setParentNodeId(rootNode).setLabel(label));
                 if (i % 500 == 499) {
                     getResultLogger().logTime("createTreeNodes[" + (i - 499) + "-" + i + "]", startCreateNode, 500, "tree node");
                     startCreateNode = System.currentTimeMillis();
                 }
             }
 
             final List<FxTreeNode> children = EJBLookup.getTreeEngine().getTree(FxTreeMode.Edit, rootNode, 1).getChildren();
             assertTrue(children.size() == numNodes, "Expected " + numNodes + " children of our root node, got: " + children.size());
 
             // select the tree paths of all linked contents
             final SqlQueryBuilder builder = new SqlQueryBuilder().select("@pk", "@path").maxRows(numNodes).isChild(rootNode);
             final long startSearch = System.currentTimeMillis();
             final FxResultSet result = builder.timeout(1000).getResult();
             getResultLogger().logTime("selectTreePath", startSearch, numNodes, "row");
             assertTrue(result.getRowCount() == numNodes, "Expected " + numNodes + " rows, got: " + result.getRowCount());
         } finally {
             if (rootNode != -1) {
                 EJBLookup.getTreeEngine().remove(FxTreeNodeEdit.createNew("").setId(rootNode), true, true);
             }
             FxTestUtils.logout();
         }
     }
 
     @Test(dataProvider = "dataVolumeInstanceCounts")
    public void dataVolumeBenchmark(int counts) throws FxApplicationException {
         // create a large number of simple objects
         final FxType type = createDataVolumeType();
         final FxContent prototype = getContentEngine().initialize(TYPE_VOLUME);
         long start = System.currentTimeMillis();
        for (int i = 0; i < counts; i++) {
             createDataVolumeContent(prototype, i).save();
         }
         getResultLogger().logTime("query-volume-create-" + counts, start, counts, "instance");
 
         // perform a simple FxSQL query
         final int rangeStart = counts / 3;
         final int rangeEnd = rangeStart + 500;
         final String rangeDescr = "[" + rangeStart + "-" + rangeEnd + ")";
         final SqlQueryBuilder sqb = new SqlQueryBuilder()
                 .select("@pk", TYPE_VOLUME + "/string01", TYPE_VOLUME + "/string02", TYPE_VOLUME + "/string03", TYPE_VOLUME + "/text", TYPE_VOLUME + "/number01", TYPE_VOLUME + "/date01")
                 .andSub()
                 .condition(TYPE_VOLUME + "/number01", PropertyValueComparator.GE, rangeStart)
                 .condition(TYPE_VOLUME + "/number01", PropertyValueComparator.LT, rangeEnd)
                 .closeSub();
 
         start = System.currentTimeMillis();
         sqb.timeout(10*60);
         final FxResultSet result = sqb.getResult();
         getResultLogger().logTime("query-volume-fxsql-" + counts, start, 1, "query");
 
         assertEquals(result.getRowCount(), 500);
         for (FxResultRow row : result.getResultRows()) {
             checkResult(rangeStart, rangeEnd, rangeDescr,
                     row.getInt(TYPE_VOLUME + "/number01"),
                     row.getString(TYPE_VOLUME + "/string01"),
                     row.getString(TYPE_VOLUME + "/string02"),
                     row.getString(TYPE_VOLUME + "/string03")
             );
         }
 
         // perform the same query with CMIS-SQL
         start = System.currentTimeMillis();
         final CmisResultSet cmisResult = EJBLookup.getCmisSearchEngine().search(
                 "SELECT ObjectId, string01, string02, string03, text, number01, date01 "
                         + " FROM " + TYPE_VOLUME
                         + " WHERE number01 >= " + rangeStart + " AND number01 < " + rangeEnd
         );
         getResultLogger().logTime("query-volume-cmissql-" + counts, start, 1, "query");
 
         assertEquals(cmisResult.getRowCount(), 500);
         for (CmisResultRow row : cmisResult) {
             checkResult(rangeStart, rangeEnd, rangeDescr,
                     row.getColumn("number01").getInt(),
                     row.getColumn("string01").getString(),
                     row.getColumn("string02").getString(),
                     row.getColumn("string03").getString()
             );
         }
     }
 
     private void checkResult(int rangeStart, int rangeEnd, String rangeDescr, int number01, String string01, String string02, String string03) {
         assertFalse(string01.isEmpty());
         assertFalse(string02.isEmpty());
         assertFalse(string03.isEmpty());
         assertTrue(number01 >= rangeStart && number01 < rangeEnd,
                "Query returned invalid result: " + number01 + ", expected range: " + rangeDescr
     );
     }
 
     @DataProvider(name = "dataVolumeInstanceCounts")
     public Object[][] getVolumeCounts() {
         return new Object[][] {
                { 1000 },
                { 9000 },   // 10000 instances
                { 20000 }   // 30000 instances
         };
     }
 
 
     private FxType createDataVolumeType() throws FxApplicationException {
         try {
             return CacheAdmin.getEnvironment().getType(TYPE_VOLUME);
         } catch (FxRuntimeException e) {
             FxContext.startRunningAsSystem();
             try {
                 final FxTypeEdit type = FxTypeEdit.createNew(TYPE_VOLUME).save();
                 type.setDefaultOptions(Arrays.asList(
                         // disable fulltext for all text fields
                         new FxStructureOption(FxStructureOption.OPTION_FULLTEXT, false, true, FxStructureOption.VALUE_FALSE)
                 ));
                 type.addProperty("string01", FxDataType.String1024);
                 type.addProperty("string02", FxDataType.String1024);
                 type.addProperty("string03", FxDataType.String1024);
                 type.addProperty("text", FxDataType.Text);
                 type.addProperty("number01", FxDataType.Number);
                 type.addProperty("number02", FxDataType.Number);
                 type.addProperty("number03", FxDataType.Number);
                 type.addProperty("date01", FxDataType.Date);
                 type.addProperty("date02", FxDataType.Date);
                 return CacheAdmin.getEnvironment().getType(TYPE_VOLUME);
             } finally {
                 FxContext.stopRunningAsSystem();
             }
         }
     }
 
     private FxContent createDataVolumeContent(FxContent empty, int id) {
         final FxContent content = empty.copyAsNewInstance();
         content.setValue("/string01", "String value 01 - " + id);
         content.setValue("/string02", "String value 02 - " + id);
         content.setValue("/string03", "String value 03 - " + id);
         content.setValue("/text", "Text value - " + id);
         content.setValue("/number01", id);
         content.setValue("/number02", id + 1283);
         content.setValue("/number03", id + 8252);
         content.setValue("/date01", new Date(RandomUtils.nextInt()));
         content.setValue("/date02", new Date(RandomUtils.nextInt()));
         return content;
     }
 }
