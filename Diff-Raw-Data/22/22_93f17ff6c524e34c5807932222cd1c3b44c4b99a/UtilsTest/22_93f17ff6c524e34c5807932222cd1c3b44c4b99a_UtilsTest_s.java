 package com.googlecode.usc;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertNull;
 import static org.junit.Assert.assertTrue;
 
 import java.util.HashSet;
 import java.util.Properties;
 import java.util.Set;
 
 import org.junit.Test;
 
 /**
  *
  * @author ShunLi
  */
 public class UtilsTest {
 
     @Test
     public void testIsBlank1() {
         String demo1 = null;
         assertTrue(Utils.isBlank(demo1));
     }
 
     @Test
     public void testIsBlank2() {
         String demo2 = "";
         assertTrue(Utils.isBlank(demo2));
     }
 
     @Test
     public void testIsBlank3() {
         String demo3 = "demo";
         assertFalse(Utils.isBlank(demo3));
     }
 
     @Test
     public void testBuildInsertSqlNoWhereStatement() {
         String criteria = "student";
         Set<String> columnNames = new HashSet<String>();
         columnNames.add("name");
         columnNames.add("age");
 
        assertEquals("INSERT INTO student(age, name) VALUES (:age, :name)", Utils.buildInsertSql(columnNames, criteria).toString().trim());
     }
 
     @Test
     public void testBuildInsertSqlWithWhereStatement() {
         String criteria = "student where name = 'lishunli'";
         Set<String> columnNames = new HashSet<String>();
         columnNames.add("name");
         columnNames.add("age");
 
         assertEquals("INSERT INTO student (age, name) VALUES (:age, :name)", Utils.buildInsertSql(columnNames, criteria).toString().trim());
     }
 
     @Test
     public void testParseString2Boolean1() {
         assertFalse(Utils.parseString2Boolean("false", Boolean.TRUE));
     }
 
     @Test
     public void testParseString2Boolean2() {
         assertTrue(Utils.parseString2Boolean("true", Boolean.TRUE));
     }
 
     @Test
     public void testParseString2Boolean3() {
         assertTrue(Utils.parseString2Boolean("others != true", Boolean.TRUE));
     }
 
     @Test
     public void testParseString2Boolean4() {
         assertTrue(Utils.parseString2Boolean(null, Boolean.TRUE));
     }
 
     @Test
     public void testParseString2Long1() {
         assertEquals(5000, Utils.parseString2Long("5000", 0L).longValue());
     }
 
     @Test
     public void testParseString2Long2() {
         assertEquals(0, Utils.parseString2Long(null, 0L).longValue());
     }
 
     @Test
     public void testParseString2Long3() {
         assertEquals(0, Utils.parseString2Long("5000a", 0L).longValue());
     }
 
     @Test
     public void testLoadPropertiesFileByStreamNormal() {
         Properties prop = Utils.loadPropertiesFile(this.getClass().getClassLoader().getResourceAsStream("build-info.properties"));
 
         assertNotNull(prop);
         assertEquals("0.1-SANPSHOT", prop.get("version"));
     }
 
     @Test
     public void testLoadPropertiesFileByStreamAbnormal() {
         Properties prop = Utils.loadPropertiesFile(this.getClass().getClassLoader().getResourceAsStream("build-infoXXX.properties"));
 
         assertNotNull(prop);
         assertNull(prop.get("version"));
     }
 
 
     @Test
     public void testLoadPropertiesFileByPathAndStreamNormal() {
         Properties config = Utils.loadPropertiesFile(
                 System.getProperty("user.dir") + "\\" + "config.properties",
                 this.getClass().getClassLoader().getResourceAsStream("config.properties"));
 
         assertNotNull(config);
         assertEquals("false", config.get("openLogFile"));
     }
 
     @Test
     public void testLoadPropertiesFileByPathAndStreamAbnormal() {
         Properties config = Utils.loadPropertiesFile(
                 System.getProperty("user.dir") + "\\" + "config.properties",
                 this.getClass().getClassLoader().getResourceAsStream("configXXX.properties"));
 
         assertNotNull(config);
         assertNull(config.get("openLogFile"));
     }
 }
