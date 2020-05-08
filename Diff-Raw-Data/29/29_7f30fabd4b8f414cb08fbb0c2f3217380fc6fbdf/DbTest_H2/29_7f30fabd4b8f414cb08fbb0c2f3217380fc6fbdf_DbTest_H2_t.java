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
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.math.BigDecimal;
 import java.math.BigInteger;
 import java.sql.Blob;
 import java.sql.SQLException;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Iterator;
 
 import org.junit.Test;
 
 import com.craftfire.commons.database.DataField;
 import com.craftfire.commons.database.DataRow;
 import com.craftfire.commons.enums.DataType;
 import com.craftfire.commons.enums.FieldType;
 import com.craftfire.commons.managers.DataManager;
 
 public class DbTest_H2 {
     static DataManager datamanager;
     static DataField field;
     static final String newline = System.getProperty("line.separator");
     static String seperate = newline
             + "|------------------------------------------------------------------|"
             + newline;
     static int succeed = 0;
     static int gsucceed = 0;
     static int count = 0;
     static int gcount = 0;
     static int[] asucceed = new int[4];
     static int[] acount = new int[4];
 
    public static void main(String[] args) throws SQLException {
         String user = "sa";
         String password = "";
         datamanager = new DataManager(DataType.H2, user, password);
         datamanager.setDatabase("test");
         datamanager.setDirectory("./src/test/resource/");
         datamanager.setTimeout(0);
         datamanager.setKeepAlive(true);
         datamanager.setPrefix("");
         System.out.println(seperate + seperate);
         System.out.println("TESTING getResults");
         DataRow data = datamanager.getResults(
                 "SELECT * FROM `typetest` LIMIT 1").getFirstResult();
         Iterator<DataField> I = data.iterator();
         while (I.hasNext()) {
             field = I.next();
             System.out.println(seperate);
             System.out.println(field.getFieldName() + " from "
                     + field.getTable());
             System.out.println("Type: " + field.getFieldType().name());
             System.out.println("Size: " + field.getFieldSize());
             System.out.println("SQL Type: " + field.getSQLType());
             runTest();
             System.out.println("SUCCEED: " + succeed + "/" + count);
             succeed = 0;
             count = 0;
         }
         System.out.println(seperate);
         System.out.println("getResults SUCCEED: " + gsucceed + "/" + gcount);
         asucceed[0] = gsucceed;
         acount[0] = gcount;
         gsucceed = 0;
         gcount = 0;
         System.out.println(seperate + seperate);
         System.out.println("TESTING getField");
         HashMap<String, Object> labels = datamanager
                 .getArray("SELECT * FROM `typetest` LIMIT 1");
         for (Object o : labels.keySet().toArray()) {
             String s = (String) o;
             try {
                 field = datamanager.getField(FieldType.UNKNOWN, "typetest", s,
                         "1");
             } catch (SQLException e) {
                 e.printStackTrace();
                 field = null;
             }
             System.out.println(seperate);
             System.out.println(field.getFieldName() + " from "
                     + field.getTable());
             System.out.println("Type: " + field.getFieldType().name());
             System.out.println("Size: " + field.getFieldSize());
             System.out.println("SQL Type: " + field.getSQLType());
             runTest();
             System.out.println("SUCCEED: " + succeed + "/" + count);
             succeed = 0;
             count = 0;
         }
         System.out.println(seperate);
         System.out.println("getField SUCCEED: " + gsucceed + "/" + gcount);
         asucceed[1] = gsucceed;
         acount[1] = gcount;
         gsucceed = 0;
         gcount = 0;
         System.out.println(seperate + seperate);
         System.out.println("TESTING get<Kinda>Field");
         for (Object o : labels.keySet().toArray()) {
             String s = (String) o;
             System.out.println(seperate);
             System.out.println(s + " from " + "typetest");
             runTest2(s);
             System.out.println("SUCCEED: " + succeed + "/" + count);
             succeed = 0;
             count = 0;
         }
         System.out.println(seperate);
         System.out.println("get<Kinda>Field SUCCEED: " + gsucceed + "/"
                 + gcount);
         asucceed[2] = gsucceed;
         acount[2] = gcount;
         gsucceed = 0;
         gcount = 0;
         System.out.println(seperate + seperate);
         System.out.println("TESTING DataList");
         for (Object o : labels.keySet().toArray()) {
             String s = (String) o;
             System.out.println(seperate);
             System.out.println(s + " from " + data.getTable(s));
             System.out.println("Type: " + data.getFieldType(s).name());
             System.out.println("Size: " + data.getFieldSize(s));
             System.out.println("SQL Type: " + data.getFieldSQLType(s));
             runTest3(data, s);
             System.out.println("SUCCEED: " + succeed + "/" + count);
             succeed = 0;
             count = 0;
         }
         System.out.println(seperate);
         System.out.println("DataList SUCCEED: " + gsucceed + "/" + gcount);
         asucceed[3] = gsucceed;
         acount[3] = gcount;
         gsucceed = 0;
         gcount = 0;
         System.out.println(seperate + seperate);
         System.out
                 .println("getResult SUCCEED " + asucceed[0] + "/" + acount[0]);
         System.out.println("getField SUCCEED " + asucceed[1] + "/" + acount[1]);
         System.out.println("get<Kinda>Field SUCCEED " + asucceed[2] + "/"
                 + acount[2]);
         System.out.println("DataList SUCCEED " + asucceed[3] + "/" + acount[3]);
     }
 
     public static void runTest() {
         DbTest_H2 instance = new DbTest_H2();
         instance.testBigInt();
         instance.testBlob();
         instance.testBool();
         instance.testBytes();
         instance.testDate();
         instance.testDecimal();
         instance.testDouble();
         instance.testFloat();
         instance.testInt();
         instance.testLong();
         instance.testString();
     }
 
     public static void runTest2(String field) {
         boolean b = datamanager.getBooleanField("typetest", field, "1");
         printResult("getBooleanField", b, b);
         String s = datamanager.getBinaryField("typetest", field, "1");
         printResult("getBinaryField", s);
         Blob blob = datamanager.getBlobField("typetest", field, "1");
         printResult("getBlobField", blob);
         Date date = datamanager.getDateField("typetest", field, "1");
         printResult("getDateField", date);
         double d = datamanager.getDoubleField("typetest", field, "1");
         printResult("getDoubleField", d != 0, d);
         int i = datamanager.getIntegerField("typetest", field, "1");
         printResult("getIntegerField", i != 0, i);
         s = datamanager.getStringField("typetest", field, "1");
         printResult("getStringField", s);
     }
 
     public static void runTest3(DataRow data, String fieldName) {
         BigInteger bint = data.getBigIntField(fieldName);
         printResult("getBigIntField", bint);
         byte[] bytes = data.getBinaryField(fieldName);
         printResult("getBinaryField", bytes);
         Blob blob = data.getBlobField(fieldName);
         printResult("getBlobField", blob);
         boolean b = data.getBoolField(fieldName);
         printResult("getBoolField", b, b);
         Date date = data.getDateField(fieldName);
         printResult("getDateField", date);
         BigDecimal dec = data.getDecimalField(fieldName);
         printResult("getDecimalField", dec);
         double d = data.getDoubleField(fieldName);
         printResult("getDoubleField", d != 0, d);
         float fl = data.getFloatField(fieldName);
         printResult("getFloatField", fl != 0, fl);
         int i = data.getIntField(fieldName);
         printResult("getIntField", i != 0, i);
         long l = data.getLongField(fieldName);
         printResult("getLongField", l != 0, l);
         String s = data.getStringField(fieldName);
         printResult("getStringField", s);
     }
 
     public static String ask(String name, String defaultvalue) {
         String line = null;
         String data = null;
         boolean valid = false;
         try {
             do {
                 System.out.println(newline + name + ": ");
                 BufferedReader br = new BufferedReader(new InputStreamReader(
                         System.in));
                 line = br.readLine();
                 if (line != null && !line.isEmpty()) {
                     data = line;
                     valid = true;
                 } else {
                     data = defaultvalue;
                     System.out.println(defaultvalue);
                     valid = true;
                 }
             } while (!valid);
         } catch (IOException ioe) {
             System.out.println("IO exception = " + ioe);
         }
         return data;
     }
 
     public static void printResult(String name, boolean success, Object x) {
         String s = success ? "+" : "-";
         ++count;
         ++gcount;
         if (success) {
             ++succeed;
             ++gsucceed;
         }
         s += name;
         s += ": ";
         if (x != null) {
             s += x.toString();
         } else {
             s += "Doesn't work";
         }
         System.out.println(s);
     }
 
     public static void printResult(String name, Object x) {
         printResult(name, x != null, x);
     }
 
     public static String arrayToString(Object[] array) {
         String s = "";
         if (array == null) {
             return null;
         }
         for (int i = 0; i < array.length; ++i) {
             s += "[" + i + "] => [" + array[i].toString() + "] | ";
         }
         return s;
     }
 
     public static String arrayToString(byte[] array) {
         String s = "";
         if (array == null) {
             return null;
         }
         for (int i = 0; i < array.length; ++i) {
             s += "[" + i + "] => [" + array[i] + "] | ";
         }
         return s;
     }
 
     @Test
     public void testBigInt() {
         BigInteger x = field.getBigInt();
         printResult("asBigInt", x);
     }
 
     @Test
     public void testBlob() {
         Blob x = field.getBlob();
         printResult("asBlob", x);
     }
 
     @Test
     public void testBool() {
         boolean x = field.getBool();
         printResult("asBool", x, x);
     }
 
     @Test
     public void testBytes() {
         byte[] x = field.getBytes();
         printResult("asBytes", arrayToString(x));
     }
 
     @Test
     public void testDate() {
         Date x = field.getDate();
         printResult("asDate", x);
     }
 
     @Test
     public void testDecimal() {
         BigDecimal x = field.getDecimal();
         printResult("asDecimal", x);
     }
 
     @Test
     public void testDouble() {
         double x = field.getDouble();
         printResult("asDouble", x != 0, x);
     }
 
     @Test
     public void testFloat() {
         float x = field.getFloat();
         printResult("asFloat", x != 0, x);
     }
 
     @Test
     public void testInt() {
         int x = field.getInt();
         printResult("asInt", x != 0, x);
     }
 
     @Test
     public void testLong() {
         long x = field.getLong();
         printResult("asLong", x != 0, x);
     }
 
     @Test
     public void testString() {
         String x = field.getString();
         printResult("asString", x);
     }
 }
