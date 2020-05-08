 /*
  * This file is part of CraftCommons.
  *
  * Copyright (c) 2011-2012, CraftFire <http://www.craftfire.com/>
  * CraftCommons is licensed under the GNU Lesser General Public License.
  *
  * CraftCommons is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * CraftCommons is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package com.craftfire.commons;
 
 import static org.hamcrest.CoreMatchers.equalTo;
 import static org.hamcrest.CoreMatchers.not;
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertNull;
 import static org.junit.Assert.assertThat;
 import static org.junit.Assert.assertTrue;
 
 import java.sql.SQLException;
 import java.text.DateFormat;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Random;
 import java.util.logging.Level;
 
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 import com.craftfire.commons.database.DataField;
 import com.craftfire.commons.database.DataManager;
 import com.craftfire.commons.database.DataRow;
 import com.craftfire.commons.database.DataType;
 import com.craftfire.commons.database.FieldType;
 
 public class TestDatabase {
     private static final String table = "typetest";
     private static final String wrtable = "writetest";
     private static DataManager datamanager;
     private static String user = "sa";
     private static String password = "";
     private static int randomInt = new Random().nextInt(1000);
 
     @BeforeClass
     public static void init() {
         datamanager = new DataManager(DataType.H2, user, password);
         datamanager.getLogging().getLogger().setLevel(Level.OFF); //Turn off logging temporarily so we won't be spammed with red warnings.
         datamanager.setDatabase("test");
         datamanager.setDirectory("./src/test/resource/");
         datamanager.setTimeout(0);
         datamanager.setKeepAlive(true);
         datamanager.setPrefix("");
     }
 
     @Test
     public void testSettings() {
        System.out.println("DataManager started " + (System.currentTimeMillis() / 1000 - datamanager.getStartup()) + " seconds ago.");
         assertEquals(user, datamanager.getUsername());
         assertEquals(password, datamanager.getPassword());
         assertEquals("test", datamanager.getDatabase());
         assertEquals("./src/test/resource/", datamanager.getDirectory());
         assertEquals(0, datamanager.getTimeout());
         assertTrue(datamanager.isKeepAlive());
         assertEquals("", datamanager.getPrefix());
         assertTrue(datamanager.hasConnection());
     }
     
     @Test
     public void testReconnect() {
     	datamanager.reconnect();
     	assertTrue(datamanager.isConnected());
     }
 
     @Test
     public void testExist() {
         assertTrue(datamanager.tableExist(table));
         assertTrue(datamanager.exist(table, "ID", 1));
     }
 
     @Test
     public void getLastID() {
         assertEquals(1, datamanager.getLastID("ID", table));
         assertEquals(0, datamanager.getLastID("I", "empty"));
         assertEquals(1, datamanager.getLastID("ID", table, "`char` = '8.88'"));
         assertEquals(0, datamanager.getLastID("ID", table, "`char` = 'alice has a cat'"));
     }
 
     @Test
     public void testCount() {
         assertEquals(1, datamanager.getCount(table));
         assertEquals(0, datamanager.getCount(table, "`char` = 'alice has a cat'"));
     }
 
     @Test
     public void testInsert() throws SQLException {
         int prevId = datamanager.getLastID("id", wrtable);
         Date now = new Date();
         Map<String, Object> data = new HashMap<String, Object>();
         data.put("TXT", "commons" + randomInt);
         data.put("x", randomInt + 1);
         data.put("d", now);
         data.put("b", null);
         datamanager.insertFields(data, wrtable);
         int id = datamanager.getLastID("id", wrtable);
         assertTrue(id > prevId);
         assertEquals(randomInt + 1, datamanager.getIntegerField(wrtable, "x", "`id` = '" + id + "'"));
         assertEquals("commons" + randomInt, datamanager.getStringField(wrtable, "txt", "`id` = '" + id + "'"));
         assertEquals(now.getTime(), datamanager.getDateField(wrtable, "d", "`id` = '" + id + "'").getTime());
         assertTrue(datamanager.getField(FieldType.UNKNOWN, wrtable, "b", "`id` = '" + id + "'").isNull());
     }
 
     @Test
     public void testUpdate() throws SQLException {
         String oldString = datamanager.getStringField(wrtable, "txt", "`id` = '1'");
         String testString = "crafttest" + (randomInt + 2);
         Date oldDate = datamanager.getDateField(wrtable, "d", "`id` = '1'");
         Map<String, Object> data = new HashMap<String, Object>();
         data.put("txt", testString);
         data.put("d", null);
         datamanager.updateFields(data, wrtable, "`id` = '1'");
         assertEquals(testString, datamanager.getStringField(wrtable, "txt", "`id` = '1'"));
         assertTrue(datamanager.getField(FieldType.UNKNOWN, wrtable, "d", "`id` = '1'").isNull());
         datamanager.updateField(wrtable, "txt", oldString, "`id` = '1'");
         datamanager.updateField(wrtable, "d", oldDate, "`id` = '1'");
         datamanager.executeQueryVoid("UPDATE `" + wrtable + "` SET `d` = '" + oldDate + "' WHERE `id` = '1'");
         assertEquals(oldString, datamanager.getStringField(wrtable, "txt", "`id` = '1'"));
         assertEquals(oldDate, datamanager.getDateField(wrtable, "d", "`id` = '1'"));
     }
     
     @Test
     public void testIncrease() throws SQLException {
         int oldValue = datamanager.getIntegerField(wrtable, "x", "`id` = '1'");
         datamanager.increaseField(wrtable, "x", "`id` = '1'");
         assertEquals(oldValue + 1, datamanager.getIntegerField(wrtable, "x", "`id` = '1'"));
     }
 
     @Test
     public void testUpdateBlob() {
         String old = datamanager.getBinaryField(wrtable, "b", "`id` = '1'");
         String test = "I love JUnit Test Cases!";
         datamanager.updateBlob(wrtable, "b", "`id` = '1'", test);
         assertEquals(test, datamanager.getBinaryField(wrtable, "b", "`id` = '1'"));
         datamanager.updateBlob(wrtable, "b", "`id` = '1'", old);
         assertEquals(old, datamanager.getBinaryField(wrtable, "b", "`id` = '1'"));
     }
 
     @Test
     public void testGetKindaFieldRawQuery() {
         assertTrue(datamanager.getBooleanField("SELECT `bool` FROM `" + table + "`"));
         assertNotNull(datamanager.getBinaryField("SELECT `bin` FROM `" + table + "`"));
         assertNotNull(datamanager.getBlobField("SELECT `blob` FROM `" + table + "`"));
         assertNotNull(datamanager.getDateField("SELECT `date` FROM `" + table + "`"));
         assertThat(datamanager.getDoubleField("SELECT `double` FROM `" + table + "`"), not(equalTo(0d)));
         assertThat(datamanager.getIntegerField("SELECT `int` FROM `" + table + "`"), not(equalTo(0)));
         assertNotNull(datamanager.getStringField("SELECT `vchar` FROM `" + table + "`"));
     }
 
     @Test
     public void testBInt() throws SQLException {
         final String name = "bint";
         DataRow row = datamanager.getResults("SELECT `" + name + "` FROM `" + table + "` LIMIT 1").getFirstResult();
 
         // DataRow.getField()
         DataField field = row.get(name);
         assertNotNull(field.getBigInt());
         assertTrue(field.getBool());
         assertNotNull(field.getBytes());
         assertNotNull(field.getDate());
         assertNotNull(field.getDecimal());
         assertThat(field.getDouble(), not(equalTo(0d)));
         assertThat(field.getFloat(), not(equalTo(0f)));
         assertThat(field.getInt(), not(equalTo(0)));
         assertThat(field.getLong(), not(equalTo(0L)));
         assertNotNull(field.getString());
 
         // DataManager.getField()
         field = datamanager.getField(FieldType.UNKNOWN, table, name, "1");
         assertNotNull(field.getBigInt());
         assertTrue(field.getBool());
         assertNotNull(field.getBytes());
         assertNotNull(field.getDate());
         assertNotNull(field.getDecimal());
         assertThat(field.getDouble(), not(equalTo(0d)));
         assertThat(field.getFloat(), not(equalTo(0f)));
         assertThat(field.getInt(), not(equalTo(0)));
         assertThat(field.getLong(), not(equalTo(0L)));
         assertNotNull(field.getString());
 
         // DataManager.get<Kinda>Field()
         assertTrue(datamanager.getBooleanField(table, name, "1"));
         assertNotNull(datamanager.getBinaryField(table, name, "1"));
         assertNotNull(datamanager.getBlobField(table, name, "1"));
         assertThat(datamanager.getDoubleField(table, name, "1"), not(equalTo(0d)));
         assertNotNull(datamanager.getStringField(table, name, "1"));
 
         // DataRow.get<Kinda>Field()
         assertNotNull(row.getBigIntField(name));
         assertTrue(row.getBoolField(name));
         assertNotNull(row.getBinaryField(name));
         assertNotNull(row.getDateField(name));
         assertNotNull(row.getDecimalField(name));
         assertThat(row.getDoubleField(name), not(equalTo(0d)));
         assertThat(row.getFloatField(name), not(equalTo(0f)));
         assertThat(row.getIntField(name), not(equalTo(0)));
         assertThat(row.getLongField(name), not(equalTo(0L)));
         assertNotNull(row.getStringField(name));
     }
 
     @Test
     public void testBin() throws SQLException {
         final String name = "bin";
         DataRow row = datamanager.getResults("SELECT `" + name + "` FROM `" + table + "` LIMIT 1").getFirstResult();
 
         // DataRow.getField()
         DataField field = row.get(name);
         assertNotNull(field.getBigInt());
         assertTrue(field.getBool());
         assertNotNull(field.getBytes());
         assertThat(field.getDouble(), not(equalTo(0d)));
         assertThat(field.getFloat(), not(equalTo(0f)));
         assertThat(field.getInt(), not(equalTo(0)));
         assertThat(field.getLong(), not(equalTo(0L)));
         assertNotNull(field.getString());
 
         // DataManager.getField()
         field = datamanager.getField(FieldType.UNKNOWN, table, name, "1");
         assertNotNull(field.getBigInt());
         assertTrue(field.getBool());
         assertNotNull(field.getBytes());
         assertThat(field.getDouble(), not(equalTo(0d)));
         assertThat(field.getFloat(), not(equalTo(0f)));
         assertThat(field.getInt(), not(equalTo(0)));
         assertThat(field.getLong(), not(equalTo(0L)));
         assertNotNull(field.getString());
 
         // DataManager.get<Kinda>Field()
         assertNotNull(datamanager.getBinaryField(table, name, "1"));
         assertNotNull(datamanager.getBlobField(table, name, "1"));
         assertThat(datamanager.getIntegerField(table, name, "1"), not(equalTo(0)));
         assertNotNull(datamanager.getStringField(table, name, "1"));
 
         // DataRow.get<Kinda>Field()
         assertNotNull(row.getBigIntField(name));
         assertTrue(row.getBoolField(name));
         assertNotNull(row.getBinaryField(name));
         assertThat(row.getDoubleField(name), not(equalTo(0d)));
         assertThat(row.getFloatField(name), not(equalTo(0f)));
         assertThat(row.getIntField(name), not(equalTo(0)));
         assertThat(row.getLongField(name), not(equalTo(0L)));
         assertNotNull(row.getStringField(name));
     }
 
     @Test
     public void testBlob() throws SQLException {
         final String name = "blob";
         DataRow row = datamanager.getResults("SELECT `" + name + "` FROM `" + table + "` LIMIT 1").getFirstResult();
 
         // DataRow.getField()
         DataField field = row.get(name);
         assertNotNull(field.getBigInt());
         assertNotNull(field.getBlob());
         assertTrue(field.getBool());
         assertNotNull(field.getBytes());
         assertThat(field.getDouble(), not(equalTo(0d)));
         assertThat(field.getFloat(), not(equalTo(0f)));
         assertThat(field.getInt(), not(equalTo(0)));
         assertThat(field.getLong(), not(equalTo(0L)));
         assertNotNull(field.getString());
 
         // DataManager.getField()
         field = datamanager.getField(FieldType.UNKNOWN, table, name, "1");
         assertNotNull(field.getBigInt());
         assertNotNull(field.getBlob());
         assertTrue(field.getBool());
         assertNotNull(field.getBytes());
         assertThat(field.getDouble(), not(equalTo(0d)));
         assertThat(field.getFloat(), not(equalTo(0f)));
         assertThat(field.getInt(), not(equalTo(0)));
         assertThat(field.getLong(), not(equalTo(0L)));
         assertNotNull(field.getString());
 
         // DataManager.get<Kinda>Field()
         assertNotNull(datamanager.getBinaryField(table, name, "1"));
         assertNotNull(datamanager.getBlobField(table, name, "1"));
         assertNotNull(datamanager.getStringField(table, name, "1"));
 
         // DataRow.get<Kinda>Field()
         assertNotNull(row.getBigIntField(name));
         assertNotNull(row.getBlobField(name));
         assertTrue(row.getBoolField(name));
         assertNotNull(row.getBinaryField(name));
         assertThat(row.getDoubleField(name), not(equalTo(0d)));
         assertThat(row.getFloatField(name), not(equalTo(0f)));
         assertThat(row.getIntField(name), not(equalTo(0)));
         assertThat(row.getLongField(name), not(equalTo(0L)));
         assertNotNull(row.getStringField(name));
     }
 
     @Test
     public void testBool() throws SQLException {
         final String name = "bool";
         DataRow row = datamanager.getResults("SELECT `" + name + "` FROM `" + table + "` LIMIT 1").getFirstResult();
 
         // DataRow.getField()
         DataField field = row.get(name);
         assertNotNull(field.getBigInt());
         assertTrue(field.getBool());
         assertNotNull(field.getBytes());
         assertNotNull(field.getDecimal());
         assertThat(field.getDouble(), not(equalTo(0d)));
         assertThat(field.getFloat(), not(equalTo(0f)));
         assertThat(field.getInt(), not(equalTo(0)));
         assertThat(field.getLong(), not(equalTo(0L)));
         assertNotNull(field.getString());
 
         // DataManager.getField()
         field = datamanager.getField(FieldType.UNKNOWN, table, name, "1");
         assertNotNull(field.getBigInt());
         assertTrue(field.getBool());
         assertNotNull(field.getBytes());
         assertNotNull(field.getDecimal());
         assertThat(field.getDouble(), not(equalTo(0d)));
         assertThat(field.getFloat(), not(equalTo(0f)));
         assertThat(field.getInt(), not(equalTo(0)));
         assertThat(field.getLong(), not(equalTo(0L)));
         assertNotNull(field.getString());
 
         // DataManager.get<Kinda>Field()
         assertTrue(datamanager.getBooleanField(table, name, "1"));
         assertNotNull(datamanager.getBlobField(table, name, "1"));
         assertThat(datamanager.getDoubleField(table, name, "1"), not(equalTo(0d)));
         assertThat(datamanager.getIntegerField(table, name, "1"), not(equalTo(0)));
         assertNotNull(datamanager.getStringField(table, name, "1"));
 
         // DataRow.get<Kinda>Field()
         assertNotNull(row.getBigIntField(name));
         assertTrue(row.getBoolField(name));
         assertNotNull(row.getBinaryField(name));
         assertNotNull(row.getDecimalField(name));
         assertThat(row.getDoubleField(name), not(equalTo(0d)));
         assertThat(row.getFloatField(name), not(equalTo(0f)));
         assertThat(row.getIntField(name), not(equalTo(0)));
         assertThat(row.getLongField(name), not(equalTo(0L)));
         assertNotNull(row.getStringField(name));
     }
 
     @Test
     public void testChar() throws SQLException {
         final String name = "char";
         DataRow row = datamanager.getResults("SELECT `" + name + "` FROM `" + table + "` LIMIT 1").getFirstResult();
 
         // DataRow.getField()
         DataField field = row.get(name);
         assertTrue(field.getBool());
         assertNotNull(field.getBytes());
         assertNotNull(field.getDecimal());
         assertThat(field.getDouble(), not(equalTo(0d)));
         assertThat(field.getFloat(), not(equalTo(0f)));
         assertThat(field.getInt(), not(equalTo(0)));
         assertThat(field.getLong(), not(equalTo(0L)));
         assertNotNull(field.getString());
 
         // DataManager.getField()
         field = datamanager.getField(FieldType.UNKNOWN, table, name, "1");
         assertTrue(field.getBool());
         assertNotNull(field.getBytes());
         assertNotNull(field.getDecimal());
         assertThat(field.getDouble(), not(equalTo(0d)));
         assertThat(field.getFloat(), not(equalTo(0f)));
         assertThat(field.getInt(), not(equalTo(0)));
         assertThat(field.getLong(), not(equalTo(0L)));
         assertNotNull(field.getString());
 
         // DataManager.get<Kinda>Field()
         assertTrue(datamanager.getBooleanField(table, name, "1"));
         assertNotNull(datamanager.getBlobField(table, name, "1"));
         assertThat(datamanager.getDoubleField(table, name, "1"), not(equalTo(0d)));
         assertNotNull(datamanager.getStringField(table, name, "1"));
 
         // DataRow.get<Kinda>Field()
         assertTrue(row.getBoolField(name));
         assertNotNull(row.getBinaryField(name));
         assertNotNull(row.getDecimalField(name));
         assertThat(row.getDoubleField(name), not(equalTo(0d)));
         assertThat(row.getFloatField(name), not(equalTo(0f)));
         assertThat(row.getIntField(name), not(equalTo(0)));
         assertThat(row.getLongField(name), not(equalTo(0L)));
         assertNotNull(row.getStringField(name));
     }
 
     @Test
     public void testClob() throws SQLException {
         final String name = "clob";
 
         if (datamanager.getField(FieldType.UNKNOWN, table, name, "1").getDate() == null) {
             datamanager.updateField(table, name, DateFormat.getDateTimeInstance().format(new Date()), "1");
             // Just for debug purposes, to see what is the locale of host.
             System.out.println(DateFormat.getDateInstance().format(new Date()));
             System.out.println(DateFormat.getDateTimeInstance().format(new Date()));
             System.out.println(DateFormat.getTimeInstance().format(new Date()));
             System.out.println(DateFormat.getInstance().format(new Date()));
         }
 
         DataRow row = datamanager.getResults("SELECT `" + name + "` FROM `" + table + "` LIMIT 1").getFirstResult();
 
         // DataRow.getField()
         DataField field = row.get(name);
         assertTrue(field.getBool());
         assertNotNull(field.getBytes());
         if (field.getDate() == null) {
             // Just for debug purposes, to see what is the locale of host.
             System.out.println(DateFormat.getDateInstance().format(new Date()));
             System.out.println(DateFormat.getDateTimeInstance().format(new Date()));
             System.out.println(DateFormat.getTimeInstance().format(new Date()));
             System.out.println(DateFormat.getInstance().format(new Date()));
         }
         assertNotNull(field.getDate());
         assertNotNull(field.getString());
 
         // DataManager.getField()
         field = datamanager.getField(FieldType.UNKNOWN, table, name, "1");
         assertTrue(field.getBool());
         assertNotNull(field.getBytes());
         assertNotNull(field.getDate());
         assertNotNull(field.getString());
 
         // DataManager.get<Kinda>Field()
         assertNotNull(datamanager.getBlobField(table, name, "1"));
         assertNotNull(datamanager.getStringField(table, name, "1"));
 
         // DataRow.get<Kinda>Field()
         assertTrue(row.getBoolField(name));
         assertNotNull(row.getBinaryField(name));
         assertNotNull(row.getDateField(name));
         assertNotNull(row.getStringField(name));
     }
 
     @Test
     public void testDate() throws SQLException {
         final String name = "date";
         DataRow row = datamanager.getResults("SELECT `" + name + "` FROM `" + table + "` LIMIT 1").getFirstResult();
 
         // DataRow.getField()
         DataField field = row.get(name);
         assertNotNull(field.getBigInt());
         assertTrue(field.getBool());
         assertNotNull(field.getDate());
         assertNotNull(field.getDecimal());
         assertThat(field.getDouble(), not(equalTo(0d)));
         assertThat(field.getFloat(), not(equalTo(0f)));
         assertThat(field.getInt(), not(equalTo(0)));
         assertThat(field.getLong(), not(equalTo(0L)));
         assertNotNull(field.getString());
 
         // DataManager.getField()
         field = datamanager.getField(FieldType.UNKNOWN, table, name, "1");
         assertNotNull(field.getBigInt());
         assertTrue(field.getBool());
         assertNotNull(field.getDate());
         assertNotNull(field.getDecimal());
         assertThat(field.getDouble(), not(equalTo(0d)));
         assertThat(field.getFloat(), not(equalTo(0f)));
         assertThat(field.getInt(), not(equalTo(0)));
         assertThat(field.getLong(), not(equalTo(0L)));
         assertNotNull(field.getString());
 
         // DataManager.get<Kinda>Field()
         assertNotNull(datamanager.getBlobField(table, name, "1"));
         assertNotNull(datamanager.getDateField(table, name, "1"));
         assertNotNull(datamanager.getStringField(table, name, "1"));
 
         // DataRow.get<Kinda>Field()
         assertNotNull(row.getBigIntField(name));
         assertTrue(row.getBoolField(name));
         assertNotNull(row.getDateField(name));
         assertNotNull(row.getDecimalField(name));
         assertThat(row.getDoubleField(name), not(equalTo(0d)));
         assertThat(row.getFloatField(name), not(equalTo(0f)));
         assertThat(row.getIntField(name), not(equalTo(0)));
         assertThat(row.getLongField(name), not(equalTo(0L)));
         assertNotNull(row.getStringField(name));
     }
 
     @Test
     public void testDecimal() throws SQLException {
         final String name = "dec";
         DataRow row = datamanager.getResults("SELECT `" + name + "` FROM `" + table + "` LIMIT 1").getFirstResult();
 
         // DataRow.getField()
         DataField field = row.get(name);
         assertNotNull(field.getBigInt());
         assertTrue(field.getBool());
         assertNotNull(field.getBytes());
         assertNotNull(field.getDecimal());
         assertThat(field.getDouble(), not(equalTo(0d)));
         assertThat(field.getFloat(), not(equalTo(0f)));
         assertThat(field.getInt(), not(equalTo(0)));
         assertThat(field.getLong(), not(equalTo(0L)));
         assertNotNull(field.getString());
 
         // DataManager.getField()
         field = datamanager.getField(FieldType.UNKNOWN, table, name, "1");
         assertNotNull(field.getBigInt());
         assertTrue(field.getBool());
         assertNotNull(field.getBytes());
         assertNotNull(field.getDecimal());
         assertThat(field.getDouble(), not(equalTo(0d)));
         assertThat(field.getFloat(), not(equalTo(0f)));
         assertThat(field.getInt(), not(equalTo(0)));
         assertThat(field.getLong(), not(equalTo(0L)));
         assertNotNull(field.getString());
 
         // DataManager.get<Kinda>Field()
         assertTrue(datamanager.getBooleanField(table, name, "1"));
         assertNotNull(datamanager.getBlobField(table, name, "1"));
         assertThat(datamanager.getDoubleField(table, name, "1"), not(equalTo(0d)));
         assertThat(datamanager.getIntegerField(table, name, "1"), not(equalTo(0)));
         assertNotNull(datamanager.getStringField(table, name, "1"));
 
         // DataRow.get<Kinda>Field()
         assertNotNull(row.getBigIntField(name));
         assertTrue(row.getBoolField(name));
         assertNotNull(row.getBinaryField(name));
         assertNotNull(row.getDecimalField(name));
         assertThat(row.getDoubleField(name), not(equalTo(0d)));
         assertThat(row.getFloatField(name), not(equalTo(0f)));
         assertThat(row.getIntField(name), not(equalTo(0)));
         assertThat(row.getLongField(name), not(equalTo(0L)));
         assertNotNull(row.getStringField(name));
     }
 
     @Test
     public void testDouble() throws SQLException {
         final String name = "double";
         DataRow row = datamanager.getResults("SELECT `" + name + "` FROM `" + table + "` LIMIT 1").getFirstResult();
 
         // DataRow.getField()
         DataField field = row.get(name);
         assertNotNull(field.getBigInt());
         assertTrue(field.getBool());
         assertNotNull(field.getBytes());
         assertNotNull(field.getDecimal());
         assertThat(field.getDouble(), not(equalTo(0d)));
         assertThat(field.getFloat(), not(equalTo(0f)));
         assertThat(field.getInt(), not(equalTo(0)));
         assertThat(field.getLong(), not(equalTo(0L)));
         assertNotNull(field.getString());
 
         // DataManager.getField()
         field = datamanager.getField(FieldType.UNKNOWN, table, name, "1");
         assertNotNull(field.getBigInt());
         assertTrue(field.getBool());
         assertNotNull(field.getBytes());
         assertNotNull(field.getDecimal());
         assertThat(field.getDouble(), not(equalTo(0d)));
         assertThat(field.getFloat(), not(equalTo(0f)));
         assertThat(field.getInt(), not(equalTo(0)));
         assertThat(field.getLong(), not(equalTo(0L)));
         assertNotNull(field.getString());
 
         // DataManager.get<Kinda>Field()
         assertTrue(datamanager.getBooleanField(table, name, "1"));
         assertNotNull(datamanager.getBlobField(table, name, "1"));
         assertThat(datamanager.getDoubleField(table, name, "1"), not(equalTo(0d)));
         assertThat(datamanager.getIntegerField(table, name, "1"), not(equalTo(0)));
         assertNotNull(datamanager.getStringField(table, name, "1"));
 
         // DataRow.get<Kinda>Field()
         assertNotNull(row.getBigIntField(name));
         assertTrue(row.getBoolField(name));
         assertNotNull(row.getBinaryField(name));
         assertNotNull(row.getDecimalField(name));
         assertThat(row.getDoubleField(name), not(equalTo(0d)));
         assertThat(row.getFloatField(name), not(equalTo(0f)));
         assertThat(row.getIntField(name), not(equalTo(0)));
         assertThat(row.getLongField(name), not(equalTo(0L)));
         assertNotNull(row.getStringField(name));
     }
 
     @Test
     public void testFloat() throws SQLException {
         final String name = "float";
         DataRow row = datamanager.getResults("SELECT `" + name + "` FROM `" + table + "` LIMIT 1").getFirstResult();
 
         // DataRow.getField()
         DataField field = row.get(name);
         assertNotNull(field.getBigInt());
         assertTrue(field.getBool());
         assertNotNull(field.getBytes());
         assertNotNull(field.getDecimal());
         assertThat(field.getDouble(), not(equalTo(0d)));
         assertThat(field.getFloat(), not(equalTo(0f)));
         assertThat(field.getInt(), not(equalTo(0)));
         assertThat(field.getLong(), not(equalTo(0L)));
         assertNotNull(field.getString());
 
         // DataManager.getField()
         field = datamanager.getField(FieldType.UNKNOWN, table, name, "1");
         assertNotNull(field.getBigInt());
         assertTrue(field.getBool());
         assertNotNull(field.getBytes());
         assertNotNull(field.getDecimal());
         assertThat(field.getDouble(), not(equalTo(0d)));
         assertThat(field.getFloat(), not(equalTo(0f)));
         assertThat(field.getInt(), not(equalTo(0)));
         assertThat(field.getLong(), not(equalTo(0L)));
         assertNotNull(field.getString());
 
         // DataManager.get<Kinda>Field()
         assertTrue(datamanager.getBooleanField(table, name, "1"));
         assertNotNull(datamanager.getBlobField(table, name, "1"));
         assertThat(datamanager.getDoubleField(table, name, "1"), not(equalTo(0d)));
         assertThat(datamanager.getIntegerField(table, name, "1"), not(equalTo(0)));
         assertNotNull(datamanager.getStringField(table, name, "1"));
 
         // DataRow.get<Kinda>Field()
         assertNotNull(row.getBigIntField(name));
         assertTrue(row.getBoolField(name));
         assertNotNull(row.getBinaryField(name));
         assertNotNull(row.getDecimalField(name));
         assertThat(row.getDoubleField(name), not(equalTo(0d)));
         assertThat(row.getFloatField(name), not(equalTo(0f)));
         assertThat(row.getIntField(name), not(equalTo(0)));
         assertThat(row.getLongField(name), not(equalTo(0L)));
         assertNotNull(row.getStringField(name));
     }
 
     @Test
     public void testVCharI() throws SQLException {
         final String name = "vchari";
         DataRow row = datamanager.getResults("SELECT `" + name + "` FROM `" + table + "` LIMIT 1").getFirstResult();
 
         // DataRow.getField()
         DataField field = row.get(name);
         assertTrue(field.getBool());
         assertNotNull(field.getBytes());
         assertThat(field.getDouble(), not(equalTo(0d)));
         assertThat(field.getFloat(), not(equalTo(0f)));
         assertThat(field.getInt(), not(equalTo(0)));
         assertThat(field.getLong(), not(equalTo(0L)));
         assertNotNull(field.getString());
 
         // DataManager.getField()
         field = datamanager.getField(FieldType.UNKNOWN, table, name, "1");
         assertTrue(field.getBool());
         assertNotNull(field.getBytes());
         assertThat(field.getDouble(), not(equalTo(0d)));
         assertThat(field.getFloat(), not(equalTo(0f)));
         assertThat(field.getInt(), not(equalTo(0)));
         assertThat(field.getLong(), not(equalTo(0L)));
         assertNotNull(field.getString());
 
         // DataManager.get<Kinda>Field()
         assertNotNull(datamanager.getBlobField(table, name, "1"));
         assertNotNull(datamanager.getStringField(table, name, "1"));
 
         // DataRow.get<Kinda>Field()
         assertTrue(row.getBoolField(name));
         assertNotNull(row.getBinaryField(name));
         assertThat(row.getDoubleField(name), not(equalTo(0d)));
         assertThat(row.getFloatField(name), not(equalTo(0f)));
         assertThat(row.getIntField(name), not(equalTo(0)));
         assertThat(row.getLongField(name), not(equalTo(0L)));
         assertNotNull(row.getStringField(name));
     }
 
     @Test
     public void testInt() throws SQLException {
         final String name = "int";
         DataRow row = datamanager.getResults("SELECT `" + name + "` FROM `" + table + "` LIMIT 1").getFirstResult();
 
         // DataRow.getField()
         DataField field = row.get(name);
         assertNotNull(field.getBigInt());
         assertTrue(field.getBool());
         assertNotNull(field.getBytes());
         assertNotNull(field.getDate());
         assertNotNull(field.getDecimal());
         assertThat(field.getDouble(), not(equalTo(0d)));
         assertThat(field.getFloat(), not(equalTo(0f)));
         assertThat(field.getInt(), not(equalTo(0)));
         assertThat(field.getLong(), not(equalTo(0L)));
         assertNotNull(field.getString());
 
         // DataManager.getField()
         field = datamanager.getField(FieldType.UNKNOWN, table, name, "1");
         assertNotNull(field.getBigInt());
         assertTrue(field.getBool());
         assertNotNull(field.getBytes());
         assertNotNull(field.getDate());
         assertNotNull(field.getDecimal());
         assertThat(field.getDouble(), not(equalTo(0d)));
         assertThat(field.getFloat(), not(equalTo(0f)));
         assertThat(field.getInt(), not(equalTo(0)));
         assertThat(field.getLong(), not(equalTo(0L)));
         assertNotNull(field.getString());
 
         // DataManager.get<Kinda>Field()
         assertTrue(datamanager.getBooleanField(table, name, "1"));
         assertNotNull(datamanager.getBinaryField(table, name, "1"));
         assertNotNull(datamanager.getBlobField(table, name, "1"));
         assertThat(datamanager.getDoubleField(table, name, "1"), not(equalTo(0d)));
         assertThat(datamanager.getIntegerField(table, name, "1"), not(equalTo(0)));
         assertNotNull(datamanager.getStringField(table, name, "1"));
 
         // DataRow.get<Kinda>Field()
         assertNotNull(row.getBigIntField(name));
         assertTrue(row.getBoolField(name));
         assertNotNull(row.getBinaryField(name));
         assertNotNull(row.getDateField(name));
         assertNotNull(row.getDecimalField(name));
         assertThat(row.getDoubleField(name), not(equalTo(0d)));
         assertThat(row.getFloatField(name), not(equalTo(0f)));
         assertThat(row.getIntField(name), not(equalTo(0)));
         assertThat(row.getLongField(name), not(equalTo(0L)));
         assertNotNull(row.getStringField(name));
     }
 
     @Test
     public void testLVBin() throws SQLException {
         final String name = "lvbin";
         DataRow row = datamanager.getResults("SELECT `" + name + "` FROM `" + table + "` LIMIT 1").getFirstResult();
 
         // DataRow.getField()
         DataField field = row.get(name);
         assertNotNull(field.getBigInt());
         assertTrue(field.getBool());
         assertNotNull(field.getBytes());
         assertThat(field.getDouble(), not(equalTo(0d)));
         assertThat(field.getFloat(), not(equalTo(0f)));
         assertThat(field.getInt(), not(equalTo(0)));
         assertThat(field.getLong(), not(equalTo(0L)));
         assertNotNull(field.getString());
 
         // DataManager.getField()
         field = datamanager.getField(FieldType.UNKNOWN, table, name, "1");
         assertNotNull(field.getBigInt());
         assertTrue(field.getBool());
         assertNotNull(field.getBytes());
         assertThat(field.getDouble(), not(equalTo(0d)));
         assertThat(field.getFloat(), not(equalTo(0f)));
         assertThat(field.getInt(), not(equalTo(0)));
         assertThat(field.getLong(), not(equalTo(0L)));
         assertNotNull(field.getString());
 
         // DataManager.get<Kinda>Field()
         assertNotNull(datamanager.getBinaryField(table, name, "1"));
         assertNotNull(datamanager.getBlobField(table, name, "1"));
         assertNotNull(datamanager.getStringField(table, name, "1"));
 
         // DataRow.get<Kinda>Field()
         assertNotNull(row.getBigIntField(name));
         assertTrue(row.getBoolField(name));
         assertNotNull(row.getBinaryField(name));
         assertThat(row.getDoubleField(name), not(equalTo(0d)));
         assertThat(row.getFloatField(name), not(equalTo(0f)));
         assertThat(row.getIntField(name), not(equalTo(0)));
         assertThat(row.getLongField(name), not(equalTo(0L)));
         assertNotNull(row.getStringField(name));
     }
 
     @Test
     public void testLVChar() throws SQLException {
         final String name = "lvchar";
         DataRow row = datamanager.getResults("SELECT `" + name + "` FROM `" + table + "` LIMIT 1").getFirstResult();
 
         // DataRow.getField()
         DataField field = row.get(name);
         assertTrue(field.getBool());
         assertNotNull(field.getBytes());
         assertNotNull(field.getString());
 
         // DataManager.getField()
         field = datamanager.getField(FieldType.UNKNOWN, table, name, "1");
         assertTrue(field.getBool());
         assertNotNull(field.getBytes());
         assertNotNull(field.getString());
 
         // DataManager.get<Kinda>Field()
         assertNotNull(datamanager.getBlobField(table, name, "1"));
         assertNotNull(datamanager.getStringField(table, name, "1"));
 
         // DataRow.get<Kinda>Field()
         assertTrue(row.getBoolField(name));
         assertNotNull(row.getBinaryField(name));
         assertNotNull(row.getStringField(name));
     }
 
     @Test
     public void testNumeric() throws SQLException {
         final String name = "numeric";
         DataRow row = datamanager.getResults("SELECT `" + name + "` FROM `" + table + "` LIMIT 1").getFirstResult();
 
         // DataRow.getField()
         DataField field = row.get(name);
         assertNotNull(field.getBigInt());
         assertTrue(field.getBool());
         assertNotNull(field.getBytes());
         assertNotNull(field.getDecimal());
         assertThat(field.getDouble(), not(equalTo(0d)));
         assertThat(field.getFloat(), not(equalTo(0f)));
         assertThat(field.getInt(), not(equalTo(0)));
         assertThat(field.getLong(), not(equalTo(0L)));
         assertNotNull(field.getString());
 
         // DataManager.getField()
         field = datamanager.getField(FieldType.UNKNOWN, table, name, "1");
         assertNotNull(field.getBigInt());
         assertTrue(field.getBool());
         assertNotNull(field.getBytes());
         assertNotNull(field.getDecimal());
         assertThat(field.getDouble(), not(equalTo(0d)));
         assertThat(field.getFloat(), not(equalTo(0f)));
         assertThat(field.getInt(), not(equalTo(0)));
         assertThat(field.getLong(), not(equalTo(0L)));
         assertNotNull(field.getString());
 
         // DataManager.get<Kinda>Field()
         assertTrue(datamanager.getBooleanField(table, name, "1"));
         assertNotNull(datamanager.getBinaryField(table, name, "1"));
         assertNotNull(datamanager.getBlobField(table, name, "1"));
         assertThat(datamanager.getDoubleField(table, name, "1"), not(equalTo(0d)));
         assertThat(datamanager.getIntegerField(table, name, "1"), not(equalTo(0)));
         assertNotNull(datamanager.getStringField(table, name, "1"));
 
         // DataRow.get<Kinda>Field()
         assertNotNull(row.getBigIntField(name));
         assertTrue(row.getBoolField(name));
         assertNotNull(row.getBinaryField(name));
         assertNotNull(row.getDecimalField(name));
         assertThat(row.getDoubleField(name), not(equalTo(0d)));
         assertThat(row.getFloatField(name), not(equalTo(0f)));
         assertThat(row.getIntField(name), not(equalTo(0)));
         assertThat(row.getLongField(name), not(equalTo(0L)));
         assertNotNull(row.getStringField(name));
     }
 
     @Test
     public void testReal() throws SQLException {
         final String name = "real";
         DataRow row = datamanager.getResults("SELECT `" + name + "` FROM `" + table + "` LIMIT 1").getFirstResult();
 
         // DataRow.getField()
         DataField field = row.get(name);
         assertNotNull(field.getBigInt());
         assertTrue(field.getBool());
         assertNotNull(field.getBytes());
         assertNotNull(field.getDecimal());
         assertThat(field.getDouble(), not(equalTo(0d)));
         assertThat(field.getFloat(), not(equalTo(0f)));
         assertThat(field.getInt(), not(equalTo(0)));
         assertThat(field.getLong(), not(equalTo(0L)));
         assertNotNull(field.getString());
 
         // DataManager.getField()
         field = datamanager.getField(FieldType.UNKNOWN, table, name, "1");
         assertNotNull(field.getBigInt());
         assertTrue(field.getBool());
         assertNotNull(field.getBytes());
         assertNotNull(field.getDecimal());
         assertThat(field.getDouble(), not(equalTo(0d)));
         assertThat(field.getFloat(), not(equalTo(0f)));
         assertThat(field.getInt(), not(equalTo(0)));
         assertThat(field.getLong(), not(equalTo(0L)));
         assertNotNull(field.getString());
 
         // DataManager.get<Kinda>Field()
         assertTrue(datamanager.getBooleanField(table, name, "1"));
         assertNotNull(datamanager.getBlobField(table, name, "1"));
         assertThat(datamanager.getDoubleField(table, name, "1"), not(equalTo(0d)));
         assertThat(datamanager.getIntegerField(table, name, "1"), not(equalTo(0)));
         assertNotNull(datamanager.getStringField(table, name, "1"));
 
         // DataRow.get<Kinda>Field()
         assertNotNull(row.getBigIntField(name));
         assertTrue(row.getBoolField(name));
         assertNotNull(row.getBinaryField(name));
         assertNotNull(row.getDecimalField(name));
         assertThat(row.getDoubleField(name), not(equalTo(0d)));
         assertThat(row.getFloatField(name), not(equalTo(0f)));
         assertThat(row.getIntField(name), not(equalTo(0)));
         assertThat(row.getLongField(name), not(equalTo(0L)));
         assertNotNull(row.getStringField(name));
     }
 
     @Test
     public void testSInt() throws SQLException {
         final String name = "sint";
         DataRow row = datamanager.getResults("SELECT `" + name + "` FROM `" + table + "` LIMIT 1").getFirstResult();
 
         // DataRow.getField()
         DataField field = row.get(name);
         assertNotNull(field.getBigInt());
         assertTrue(field.getBool());
         assertNotNull(field.getBytes());
         assertNotNull(field.getDate());
         assertNotNull(field.getDecimal());
         assertThat(field.getDouble(), not(equalTo(0d)));
         assertThat(field.getFloat(), not(equalTo(0f)));
         assertThat(field.getInt(), not(equalTo(0)));
         assertThat(field.getLong(), not(equalTo(0L)));
         assertNotNull(field.getString());
 
         // DataManager.getField()
         field = datamanager.getField(FieldType.UNKNOWN, table, name, "1");
         assertNotNull(field.getBigInt());
         assertTrue(field.getBool());
         assertNotNull(field.getBytes());
         assertNotNull(field.getDate());
         assertNotNull(field.getDecimal());
         assertThat(field.getDouble(), not(equalTo(0d)));
         assertThat(field.getFloat(), not(equalTo(0f)));
         assertThat(field.getInt(), not(equalTo(0)));
         assertThat(field.getLong(), not(equalTo(0L)));
         assertNotNull(field.getString());
 
         // DataManager.get<Kinda>Field()
         assertTrue(datamanager.getBooleanField(table, name, "1"));
         assertNotNull(datamanager.getBinaryField(table, name, "1"));
         assertNotNull(datamanager.getBlobField(table, name, "1"));
         assertThat(datamanager.getDoubleField(table, name, "1"), not(equalTo(0d)));
         assertThat(datamanager.getIntegerField(table, name, "1"), not(equalTo(0)));
         assertNotNull(datamanager.getStringField(table, name, "1"));
 
         // DataRow.get<Kinda>Field()
         assertNotNull(row.getBigIntField(name));
         assertTrue(row.getBoolField(name));
         assertNotNull(row.getBinaryField(name));
         assertNotNull(row.getDateField(name));
         assertNotNull(row.getDecimalField(name));
         assertThat(row.getDoubleField(name), not(equalTo(0d)));
         assertThat(row.getFloatField(name), not(equalTo(0f)));
         assertThat(row.getIntField(name), not(equalTo(0)));
         assertThat(row.getLongField(name), not(equalTo(0L)));
         assertNotNull(row.getStringField(name));
     }
 
     @Test
     public void testTime() throws SQLException {
         final String name = "time";
         DataRow row = datamanager.getResults("SELECT `" + name + "` FROM `" + table + "` LIMIT 1").getFirstResult();
 
         // DataRow.getField()
         DataField field = row.get(name);
         assertNotNull(field.getBigInt());
         assertTrue(field.getBool());
         assertNotNull(field.getDate());
         assertNotNull(field.getDecimal());
         assertThat(field.getDouble(), not(equalTo(0d)));
         assertThat(field.getFloat(), not(equalTo(0f)));
         assertThat(field.getInt(), not(equalTo(0)));
         assertThat(field.getLong(), not(equalTo(0L)));
         assertNotNull(field.getString());
 
         // DataManager.getField()
         field = datamanager.getField(FieldType.UNKNOWN, table, name, "1");
         assertNotNull(field.getBigInt());
         assertTrue(field.getBool());
         assertNotNull(field.getDate());
         assertNotNull(field.getDecimal());
         assertThat(field.getDouble(), not(equalTo(0d)));
         assertThat(field.getFloat(), not(equalTo(0f)));
         assertThat(field.getInt(), not(equalTo(0)));
         assertThat(field.getLong(), not(equalTo(0L)));
         assertNotNull(field.getString());
 
         // DataManager.get<Kinda>Field()
         assertNotNull(datamanager.getBlobField(table, name, "1"));
         assertNotNull(datamanager.getDateField(table, name, "1"));
         assertNotNull(datamanager.getStringField(table, name, "1"));
 
         // DataRow.get<Kinda>Field()
         assertNotNull(row.getBigIntField(name));
         assertTrue(row.getBoolField(name));
         assertNotNull(row.getDateField(name));
         assertNotNull(row.getDecimalField(name));
         assertThat(row.getDoubleField(name), not(equalTo(0d)));
         assertThat(row.getFloatField(name), not(equalTo(0f)));
         assertThat(row.getIntField(name), not(equalTo(0)));
         assertThat(row.getLongField(name), not(equalTo(0L)));
         assertNotNull(row.getStringField(name));
     }
 
     @Test
     public void testTimeStamp() throws SQLException {
         final String name = "timestamp";
         DataRow row = datamanager.getResults("SELECT `" + name + "` FROM `" + table + "` LIMIT 1").getFirstResult();
 
         // DataRow.getField()
         DataField field = row.get(name);
         assertNotNull(field.getBigInt());
         assertTrue(field.getBool());
         assertNotNull(field.getDate());
         assertNotNull(field.getDecimal());
         assertThat(field.getDouble(), not(equalTo(0d)));
         assertThat(field.getFloat(), not(equalTo(0f)));
         assertThat(field.getInt(), not(equalTo(0)));
         assertThat(field.getLong(), not(equalTo(0L)));
         assertNotNull(field.getString());
 
         // DataManager.getField()
         field = datamanager.getField(FieldType.UNKNOWN, table, name, "1");
         assertNotNull(field.getBigInt());
         assertTrue(field.getBool());
         assertNotNull(field.getDate());
         assertNotNull(field.getDecimal());
         assertThat(field.getDouble(), not(equalTo(0d)));
         assertThat(field.getFloat(), not(equalTo(0f)));
         assertThat(field.getInt(), not(equalTo(0)));
         assertThat(field.getLong(), not(equalTo(0L)));
         assertNotNull(field.getString());
 
         // DataManager.get<Kinda>Field()
         assertNotNull(datamanager.getBlobField(table, name, "1"));
         assertNotNull(datamanager.getDateField(table, name, "1"));
         assertNotNull(datamanager.getStringField(table, name, "1"));
 
         // DataRow.get<Kinda>Field()
         assertNotNull(row.getBigIntField(name));
         assertTrue(row.getBoolField(name));
         assertNotNull(row.getDateField(name));
         assertNotNull(row.getDecimalField(name));
         assertThat(row.getDoubleField(name), not(equalTo(0d)));
         assertThat(row.getFloatField(name), not(equalTo(0f)));
         assertThat(row.getIntField(name), not(equalTo(0)));
         assertThat(row.getLongField(name), not(equalTo(0L)));
         assertNotNull(row.getStringField(name));
     }
 
     @Test
     public void testTInt() throws SQLException {
         final String name = "tint";
         DataRow row = datamanager.getResults("SELECT `" + name + "` FROM `" + table + "` LIMIT 1").getFirstResult();
 
         // DataRow.getField()
         DataField field = row.get(name);
         assertNotNull(field.getBigInt());
         assertTrue(field.getBool());
         assertNotNull(field.getBytes());
         assertNotNull(field.getDate());
         assertNotNull(field.getDecimal());
         assertThat(field.getDouble(), not(equalTo(0d)));
         assertThat(field.getFloat(), not(equalTo(0f)));
         assertThat(field.getInt(), not(equalTo(0)));
         assertThat(field.getLong(), not(equalTo(0L)));
         assertNotNull(field.getString());
 
         // DataManager.getField()
         field = datamanager.getField(FieldType.UNKNOWN, table, name, "1");
         assertNotNull(field.getBigInt());
         assertTrue(field.getBool());
         assertNotNull(field.getBytes());
         assertNotNull(field.getDate());
         assertNotNull(field.getDecimal());
         assertThat(field.getDouble(), not(equalTo(0d)));
         assertThat(field.getFloat(), not(equalTo(0f)));
         assertThat(field.getInt(), not(equalTo(0)));
         assertThat(field.getLong(), not(equalTo(0L)));
         assertNotNull(field.getString());
 
         // DataManager.get<Kinda>Field()
         assertTrue(datamanager.getBooleanField(table, name, "1"));
         assertNotNull(datamanager.getBinaryField(table, name, "1"));
         assertNotNull(datamanager.getBlobField(table, name, "1"));
         assertThat(datamanager.getDoubleField(table, name, "1"), not(equalTo(0d)));
         assertThat(datamanager.getIntegerField(table, name, "1"), not(equalTo(0)));
         assertNotNull(datamanager.getStringField(table, name, "1"));
 
         // DataRow.get<Kinda>Field()
         assertNotNull(row.getBigIntField(name));
         assertTrue(row.getBoolField(name));
         assertNotNull(row.getBinaryField(name));
         assertNotNull(row.getDateField(name));
         assertNotNull(row.getDecimalField(name));
         assertThat(row.getDoubleField(name), not(equalTo(0d)));
         assertThat(row.getFloatField(name), not(equalTo(0f)));
         assertThat(row.getIntField(name), not(equalTo(0)));
         assertThat(row.getLongField(name), not(equalTo(0L)));
         assertNotNull(row.getStringField(name));
     }
 
     @Test
     public void testVBin() throws SQLException {
         final String name = "vbin";
         DataRow row = datamanager.getResults("SELECT `" + name + "` FROM `" + table + "` LIMIT 1").getFirstResult();
 
         // DataRow.getField()
         DataField field = row.get(name);
         assertNotNull(field.getBigInt());
         assertTrue(field.getBool());
         assertNotNull(field.getBytes());
         assertThat(field.getDouble(), not(equalTo(0d)));
         assertThat(field.getFloat(), not(equalTo(0f)));
         assertThat(field.getInt(), not(equalTo(0)));
         assertThat(field.getLong(), not(equalTo(0L)));
         assertNotNull(field.getString());
 
         // DataManager.getField()
         field = datamanager.getField(FieldType.UNKNOWN, table, name, "1");
         assertNotNull(field.getBigInt());
         assertTrue(field.getBool());
         assertNotNull(field.getBytes());
         assertThat(field.getDouble(), not(equalTo(0d)));
         assertThat(field.getFloat(), not(equalTo(0f)));
         assertThat(field.getInt(), not(equalTo(0)));
         assertThat(field.getLong(), not(equalTo(0L)));
         assertNotNull(field.getString());
 
         // DataManager.get<Kinda>Field()
         assertNotNull(datamanager.getBinaryField(table, name, "1"));
         assertNotNull(datamanager.getBlobField(table, name, "1"));
         assertThat(datamanager.getIntegerField(table, name, "1"), not(equalTo(0)));
         assertNotNull(datamanager.getStringField(table, name, "1"));
 
         // DataRow.get<Kinda>Field()
         assertNotNull(row.getBigIntField(name));
         assertTrue(row.getBoolField(name));
         assertNotNull(row.getBinaryField(name));
         assertThat(row.getDoubleField(name), not(equalTo(0d)));
         assertThat(row.getFloatField(name), not(equalTo(0f)));
         assertThat(row.getIntField(name), not(equalTo(0)));
         assertThat(row.getLongField(name), not(equalTo(0L)));
         assertNotNull(row.getStringField(name));
     }
 
     @Test
     public void testVChar() throws SQLException {
         final String name = "vchar";
         DataRow row = datamanager.getResults("SELECT `" + name + "` FROM `" + table + "` LIMIT 1").getFirstResult();
 
         // DataRow.getField()
         DataField field = row.get(name);
         assertTrue(field.getBool());
         assertNotNull(field.getBytes());
         assertNotNull(field.getString());
 
         // DataManager.getField()
         field = datamanager.getField(FieldType.UNKNOWN, table, name, "1");
         assertTrue(field.getBool());
         assertNotNull(field.getBytes());
         assertNotNull(field.getString());
 
         // DataManager.get<Kinda>Field()
         assertNotNull(datamanager.getBlobField(table, name, "1"));
         assertNotNull(datamanager.getStringField(table, name, "1"));
 
         // DataRow.get<Kinda>Field()
         assertTrue(row.getBoolField(name));
         assertNotNull(row.getBinaryField(name));
         assertNotNull(row.getStringField(name));
     }
 
 /*    public void testTemplate() throws SQLException {
         final String name = "";
         DataRow row = datamanager.getResults("SELECT `" + name + "` FROM `" + table + "` LIMIT 1").getFirstResult();
 
         // DataRow.getField()
         DataField field = row.get(name);
         assertNotNull(field.getBigInt());
         assertNotNull(field.getBlob());
         assertTrue(field.getBool());
         assertNotNull(field.getBytes());
         assertNotNull(field.getDate());
         assertNotNull(field.getDecimal());
         assertThat(field.getDouble(), not(equalTo(0d)));
         assertThat(field.getFloat(), not(equalTo(0f)));
         assertThat(field.getInt(), not(equalTo(0)));
         assertThat(field.getLong(), not(equalTo(0L)));
         assertNotNull(field.getString());
 
         // DataManager.getField()
         field = datamanager.getField(FieldType.UNKNOWN, table, name, "1");
         assertNotNull(field.getBigInt());
         assertNotNull(field.getBlob());
         assertTrue(field.getBool());
         assertNotNull(field.getBytes());
         assertNotNull(field.getDate());
         assertNotNull(field.getDecimal());
         assertThat(field.getDouble(), not(equalTo(0d)));
         assertThat(field.getFloat(), not(equalTo(0f)));
         assertThat(field.getInt(), not(equalTo(0)));
         assertThat(field.getLong(), not(equalTo(0L)));
         assertNotNull(field.getString());
 
         // DataManager.get<Kinda>Field()
         assertTrue(datamanager.getBooleanField(table, name, "1"));
         assertNotNull(datamanager.getBinaryField(table, name, "1"));
         assertNotNull(datamanager.getBlobField(table, name, "1"));
         assertNotNull(datamanager.getDateField(table, name, "1"));
         assertThat(datamanager.getDoubleField(table, name, "1"), not(equalTo(0d)));
         assertThat(datamanager.getIntegerField(table, name, "1"), not(equalTo(0)));
         assertNotNull(datamanager.getStringField(table, name, "1"));
 
         // DataRow.get<Kinda>Field()
         assertNotNull(row.getBigIntField(name));
         assertNotNull(row.getBlobField(name));
         assertTrue(row.getBoolField(name));
         assertNotNull(row.getBinaryField(name));
         assertNotNull(row.getDateField(name));
         assertNotNull(row.getDecimalField(name));
         assertThat(row.getDoubleField(name), not(equalTo(0d)));
         assertThat(row.getFloatField(name), not(equalTo(0f)));
         assertThat(row.getIntField(name), not(equalTo(0)));
         assertThat(row.getLongField(name), not(equalTo(0L)));
         assertNotNull(row.getStringField(name));
     }
 */
 }
