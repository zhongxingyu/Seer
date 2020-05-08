 package de.consistec.doubleganger.impl.adapter;
 
 /*
  * #%L
  * Project - doppelganger
  * File - JSONSerializationAdapterTest.java
  * %%
  * Copyright (C) 2011 - 2013 consistec GmbH
  * %%
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as
  * published by the Free Software Foundation, either version 3 of the
  * License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public
  * License along with this program.  If not, see
  * <http://www.gnu.org/licenses/gpl-3.0.html>.
  * #L%
  */
 
 import static de.consistec.doubleganger.common.util.CollectionsUtil.newHashMap;
 import static de.consistec.doubleganger.common.util.CollectionsUtil.newHashSet;
 import static org.junit.Assert.assertArrayEquals;
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertTrue;
 
 import de.consistec.doubleganger.common.SyncData;
 import de.consistec.doubleganger.common.SyncDirection;
 import de.consistec.doubleganger.common.SyncSettings;
 import de.consistec.doubleganger.common.TableSyncStrategies;
 import de.consistec.doubleganger.common.TableSyncStrategy;
 import de.consistec.doubleganger.common.TestBase;
 import de.consistec.doubleganger.common.TestUtil;
 import de.consistec.doubleganger.common.conflict.ConflictStrategy;
 import de.consistec.doubleganger.common.data.Change;
 import de.consistec.doubleganger.common.data.MDEntry;
 import de.consistec.doubleganger.common.data.schema.Schema;
 import de.consistec.doubleganger.common.exception.SerializationException;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.junit.Test;
 
 /**
  * <ul> <li><b>Company:</b> consistec Engineering and Consulting GmbH</li>
  * <li><b>Date:</b> 12.07.12 09:03</li></ul>
  *
  * @author Markus Backes
  * @since 0.0.1-SNAPSHOT
  */
 public class JSONSerializationAdapterTest extends TestBase {
 
     private static final String TABLENAME1 = "table1";
     private static final String TABLENAME2 = "table2";
 
     private static final String COLUMNNAME1 = "column1";
     private static final String COLUMNNAME2 = "column2";
     private static final String COLUMNNAME3 = "column3";
     private static final String COLUMNNAME4 = "column4";
     private static final String COLUMNNAME5 = "column4";
     private static final String COLUMNNAME6 = "column4";
 
     private static final String TEST_STRING = "foobar";
     private static final String TEST_MDV = "7686876786sd9876786876";
 
        @Test
     public void testChangeListSerialization() throws SerializationException {
 
        final SyncData changeList = new SyncData();
         MDEntry entry = new MDEntry(1, true, 1, TABLENAME1, TEST_MDV);
         Map<String, Object> rowData = newHashMap();
         rowData.put(COLUMNNAME1, 1);
         rowData.put(COLUMNNAME2, TEST_STRING);
         rowData.put(COLUMNNAME3, true);
         rowData.put(COLUMNNAME4, new Date(System.currentTimeMillis()));
         rowData.put(COLUMNNAME5, 4.5);
         rowData.put(COLUMNNAME6, null);
        changeList.addChange(new Change(entry, rowData));
 
         entry = new MDEntry(2, false, 2, TABLENAME2, TEST_MDV);
         rowData = new HashMap<String, Object>();
         rowData.put(COLUMNNAME1, 2);
         rowData.put(COLUMNNAME2, TEST_STRING);
         rowData.put(COLUMNNAME3, "false");
         rowData.put(COLUMNNAME4, new Date(System.currentTimeMillis()));
         rowData.put(COLUMNNAME5, 3.14);
         rowData.put(COLUMNNAME6, null);
        changeList.addChange(new Change(entry, rowData));
 
         final JSONSerializationAdapter adapter = new JSONSerializationAdapter();
        final String jsonChangeList = adapter.serializeChangeList(changeList.getChanges());
         final SyncData deserializedChangeList = adapter.deserializeChangeList(jsonChangeList);
 
        assertEquals("Original and deserialized change lists are different", changeList.getChanges(), deserializedChangeList.getChanges());
     }
 
     @Test
     public void testSyncDataSerialization() throws SerializationException {
 
         SyncData data = new SyncData();
         MDEntry entry = new MDEntry(1, true, 1, TABLENAME1, TEST_MDV);
         Map<String, Object> rowData = newHashMap();
         rowData.put(COLUMNNAME1, 1);
         rowData.put(COLUMNNAME2, TEST_STRING);
         rowData.put(COLUMNNAME3, true);
         rowData.put(COLUMNNAME4, new Date(System.currentTimeMillis()));
         rowData.put(COLUMNNAME5, 4.5);
         rowData.put(COLUMNNAME6, null);
         data.addChange(new Change(entry, rowData));
 
         entry = new MDEntry(2, false, 2, TABLENAME2, TEST_MDV);
         rowData = new HashMap<String, Object>();
         rowData.put(COLUMNNAME1, 2);
         rowData.put(COLUMNNAME2, TEST_STRING);
         rowData.put(COLUMNNAME3, "false");
         rowData.put(COLUMNNAME4, new Date(System.currentTimeMillis()));
         rowData.put(COLUMNNAME5, 3.14);
         rowData.put(COLUMNNAME6, null);
         data.addChange(new Change(entry, rowData));
 
         final JSONSerializationAdapter adapter = new JSONSerializationAdapter();
         final String jsonChangeList = adapter.serializeChangeList(data);
         final SyncData deserializedSyncData = adapter.deserializeMaxRevisionAndChangeList(
             jsonChangeList);
 
         assertEquals("max revision of serialized and deserialized tuple are different!", data.getRevision(),
             deserializedSyncData.getRevision());
         assertEquals("Original and deserialised tuples are different!", data.getChanges(),
             deserializedSyncData.getChanges());
     }
 
     @Test
     public void testSyncSerializationEmptyChangeList() throws SerializationException {
 
         SyncData data = new SyncData();
 
         final JSONSerializationAdapter adapter = new JSONSerializationAdapter();
         final String jsonChangeList = adapter.serializeChangeList(data);
         final SyncData deserializedSyncData = adapter.deserializeMaxRevisionAndChangeList(
             jsonChangeList);
 
         assertEquals("max revision of serialized and deserialized tuple are different!",
             data.getRevision(),
             deserializedSyncData.getRevision());
         assertEquals("Original and deserialised tuples are different!", data.getChanges(),
             deserializedSyncData.getChanges());
     }
 
 
     @Test
     public void testSchemaSerialization() throws SerializationException {
 
         final Schema schema = TestUtil.getSchema();
         final JSONSerializationAdapter adapter = new JSONSerializationAdapter();
         final String jsonSchema = adapter.serializeSchema(schema);
         final Schema deserializedSchema = adapter.deserializeSchema(jsonSchema);
         assertEquals("Original and deserialised schemas are different", schema, deserializedSchema);
 
     }
 
     @Test
     public void serializeSettings() throws SerializationException, JSONException {
         Set<String> tables = newHashSet();
         tables.add("categories");
         tables.add("items");
         tables.add("customers");
         TableSyncStrategies strategies = new TableSyncStrategies();
         strategies.addSyncStrategyForTable("categories",
             new TableSyncStrategy(SyncDirection.BIDIRECTIONAL, ConflictStrategy.SERVER_WINS));
         strategies.addSyncStrategyForTable("items",
             new TableSyncStrategy(SyncDirection.SERVER_TO_CLIENT, ConflictStrategy.SERVER_WINS));
         strategies.addSyncStrategyForTable("customers",
             new TableSyncStrategy(SyncDirection.CLIENT_TO_SERVER, ConflictStrategy.CLIENT_WINS));
 
         SyncSettings settings = new SyncSettings(tables, strategies);
 
         final JSONSerializationAdapter adapter = new JSONSerializationAdapter();
         String serializedSettings = adapter.serializeSettings(settings);
 
         JSONArray array = new JSONArray(serializedSettings);
         assertTrue(array.length() == 2);
 
         JSONArray tableArray = array.getJSONArray(0);
         JSONArray strategyArray = array.getJSONArray(1);
 
         int i = 0;
         for (String table : tables) {
             assertEquals(table, tableArray.getString(i));
             i++;
         }
 
         int j = 0;
         for (String table : settings.getSyncTables()) {
             String direction = strategyArray.getJSONObject(j).getString("direction");
             String conflictStrategy = strategyArray.getJSONObject(j).getString("conflictStrategy");
 
             assertEquals(settings.getStrategy(table).getDirection().name(), direction);
             assertEquals(settings.getStrategy(table).getConflictStrategy().name(),
                 conflictStrategy);
 
             j++;
         }
     }
 
     @Test
     public void deserializeSettings() throws SerializationException, JSONException {
         Set<String> tables = newHashSet();
         tables.add("categories");
         tables.add("items");
         tables.add("customers");
         TableSyncStrategies strategies = new TableSyncStrategies();
         strategies.addSyncStrategyForTable("categories",
             new TableSyncStrategy(SyncDirection.BIDIRECTIONAL, ConflictStrategy.SERVER_WINS));
         strategies.addSyncStrategyForTable("items",
             new TableSyncStrategy(SyncDirection.SERVER_TO_CLIENT, ConflictStrategy.SERVER_WINS));
         strategies.addSyncStrategyForTable("customers",
             new TableSyncStrategy(SyncDirection.CLIENT_TO_SERVER, ConflictStrategy.CLIENT_WINS));
 
         SyncSettings settings = new SyncSettings(tables, strategies);
 
         final JSONSerializationAdapter adapter = new JSONSerializationAdapter();
         String serializedSettings = adapter.serializeSettings(settings);
 
         SyncSettings deserializedSettings = adapter.deserializeSettings(serializedSettings);
 
         assertArrayEquals(settings.getSyncTables().toArray(new String[0]),
             deserializedSettings.getSyncTables().toArray(new String[0]));
 
         for (String tableName : settings.getSyncTables()) {
             assertEquals(settings.getStrategy(tableName), deserializedSettings.getStrategy(tableName));
         }
     }
 }
