 /*******************************************************************************
  * Copyright (c) 2013, Salesforce.com, Inc.
  * All rights reserved.
  * 
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are met:
  * 
  *     Redistributions of source code must retain the above copyright notice,
  *     this list of conditions and the following disclaimer.
  *     Redistributions in binary form must reproduce the above copyright notice,
  *     this list of conditions and the following disclaimer in the documentation
  *     and/or other materials provided with the distribution.
  *     Neither the name of Salesforce.com nor the names of its contributors may 
  *     be used to endorse or promote products derived from this software without 
  *     specific prior written permission.
  * 
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
  * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
  * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
  * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE 
  * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL 
  * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR 
  * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER 
  * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, 
  * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE 
  * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  ******************************************************************************/
 package com.salesforce.phoenix.end2end;
 
 import static com.salesforce.phoenix.util.TestUtil.*;
 import static org.junit.Assert.*;
 
 import java.sql.*;
 import java.util.Properties;
 
 import org.junit.Test;
 
 import com.salesforce.phoenix.util.PhoenixRuntime;
 
 
 /**
  * Tests for table with transparent salting.
  */
 public class SaltedTableTest extends BaseClientMangedTimeTest {
 
     private static void initTableValues(byte[][] splits, long ts) throws Exception {
         String url = PHOENIX_JDBC_URL + ";" + PhoenixRuntime.CURRENT_SCN_ATTRIB + "=" + ts;
         Properties props = new Properties(TEST_PROPERTIES);
         Connection conn = DriverManager.getConnection(url, props);
         
         // Rows we inserted:
         // 1abc123abc111
         // 1abc456abc111
         // 1def789abc111
         // 2abc123def222 
         // 3abc123ghi333
         try {
             // Upsert with no column specifies.
             ensureTableCreated(getUrl(), TABLE_WITH_SALTING, splits, ts-2);
             String query = "UPSERT INTO " + TABLE_WITH_SALTING + " VALUES(?,?,?,?,?)";
             PreparedStatement stmt = conn.prepareStatement(query);
             stmt.setInt(1, 1);
             stmt.setString(2, "abc");
             stmt.setString(3, "123");
             stmt.setString(4, "abc");
             stmt.setInt(5, 111);
             stmt.execute();
             conn.commit();
             
             stmt.setInt(1, 1);
             stmt.setString(2, "abc");
             stmt.setString(3, "456");
             stmt.setString(4, "abc");
             stmt.setInt(5, 111);
             stmt.execute();
             conn.commit();
             
             // Test upsert when statement explicitly specifies the columns to upsert into.
             query = "UPSERT INTO " + TABLE_WITH_SALTING +
                     " (a_integer, a_string, a_id, b_string, b_integer) " + 
                     " VALUES(?,?,?,?,?)";
             stmt = conn.prepareStatement(query);
             
             stmt.setInt(1, 1);
             stmt.setString(2, "def");
             stmt.setString(3, "789");
             stmt.setString(4, "abc");
             stmt.setInt(5, 111);
             stmt.execute();
             conn.commit();
             
             stmt.setInt(1, 2);
             stmt.setString(2, "abc");
             stmt.setString(3, "123");
             stmt.setString(4, "def");
             stmt.setInt(5, 222);
             stmt.execute();
             conn.commit();
             
             // Test upsert when order of column is shuffled.
             query = "UPSERT INTO " + TABLE_WITH_SALTING +
                     " (a_string, a_integer, a_id, b_string, b_integer) " + 
                     " VALUES(?,?,?,?,?)";
             stmt = conn.prepareStatement(query);
             stmt.setString(1, "abc");
             stmt.setInt(2, 3);
             stmt.setString(3, "123");
             stmt.setString(4, "ghi");
             stmt.setInt(5, 333);
             stmt.execute();
             conn.commit();
         } finally {
             conn.close();
         }
     }
 
     @Test
     public void testTableWithInvalidBucketNumber() throws Exception {
         long ts = nextTimestamp();
         String url = PHOENIX_JDBC_URL + ";" + PhoenixRuntime.CURRENT_SCN_ATTRIB + "=" + (ts + 5);
         Properties props = new Properties(TEST_PROPERTIES);
         Connection conn = DriverManager.getConnection(url, props);
         try {
             String query = "create table salted_table (a_integer integer not null CONSTRAINT pk PRIMARY KEY (a_integer)) SALT_BUCKETS = 129";
             PreparedStatement stmt = conn.prepareStatement(query);
             stmt.execute();
             fail("Should have caught exception");
         } catch (SQLException e) {
             assertTrue(e.getMessage(), e.getMessage().contains("ERROR 1021 (42Y80): Salt bucket numbers should be with 1 and 128."));
         } finally {
             conn.close();
         }
     }
 
     @Test
     public void testSelectValueNoWhereClause() throws Exception {
         long ts = nextTimestamp();
         String url = PHOENIX_JDBC_URL + ";" + PhoenixRuntime.CURRENT_SCN_ATTRIB + "=" + (ts + 5);
         Properties props = new Properties(TEST_PROPERTIES);
         Connection conn = DriverManager.getConnection(url, props);
         try {
             initTableValues(null, ts);
             
            String query = "SELECT * FROM " + TABLE_WITH_SALTING + " ORDER BY a_integer, a_string, a_id ASC LIMIT 1";
             PreparedStatement statement = conn.prepareStatement(query);
             ResultSet rs = statement.executeQuery();
             
             assertTrue(rs.next());
             assertEquals(1, rs.getInt(1));
             assertEquals("abc", rs.getString(2));
             assertEquals("123", rs.getString(3));
             assertEquals("abc", rs.getString(4));
             assertEquals(111, rs.getInt(5));
             
            assertFalse(rs.next());
         } finally {
             conn.close();
         }
     }
 
     @Test
     public void testSelectValueWithWhereClause() throws Exception {
         long ts = nextTimestamp();
         String url = PHOENIX_JDBC_URL + ";" + PhoenixRuntime.CURRENT_SCN_ATTRIB + "=" + (ts + 5);
         Properties props = new Properties(TEST_PROPERTIES);
         Connection conn = DriverManager.getConnection(url, props);
         try {
             initTableValues(null, ts);
             
             // Where with fully qualified key.
             String query = "SELECT * FROM " + TABLE_WITH_SALTING + 
                     " WHERE a_integer = 1 AND a_string = 'abc' AND a_id = '123'";
             PreparedStatement stmt = conn.prepareStatement(query);
             
             ResultSet rs = stmt.executeQuery();
             assertTrue(rs.next());
             assertEquals(1, rs.getInt(1));
             assertEquals("abc", rs.getString(2));
             assertEquals("123", rs.getString(3));
             assertEquals("abc", rs.getString(4));
             assertEquals(111, rs.getInt(5));
             assertFalse(rs.next());
             
             // Where without fully qualified key, point query.
             query = "SELECT * FROM " + TABLE_WITH_SALTING + 
                     " WHERE a_integer = ? AND a_string = ? ORDER BY a_id ASC LIMIT 2";
             stmt = conn.prepareStatement(query);
             
             stmt.setInt(1, 1);
             stmt.setString(2, "abc");
             rs = stmt.executeQuery();
             assertTrue(rs.next());
             assertEquals(1, rs.getInt(1));
             assertEquals("abc", rs.getString(2));
             assertEquals("123", rs.getString(3));
             assertEquals("abc", rs.getString(4));
             assertEquals(111, rs.getInt(5));
             
             assertTrue(rs.next());
             assertEquals(1, rs.getInt(1));
             assertEquals("abc", rs.getString(2));
             assertEquals("456", rs.getString(3));
             assertEquals("abc", rs.getString(4));
             assertEquals(111, rs.getInt(5));
             assertFalse(rs.next());
             
             query = "SELECT * FROM " + TABLE_WITH_SALTING + " WHERE a_string = ?";
             stmt = conn.prepareStatement(query);
             
             stmt.setString(1, "def");
             rs = stmt.executeQuery();
             assertTrue(rs.next());
             assertEquals(1, rs.getInt(1));
             assertEquals("def", rs.getString(2));
             assertEquals("789", rs.getString(3));
             assertEquals("abc", rs.getString(4));
             assertEquals(111, rs.getInt(5));
             
             // Where without fully qualified key, range query.
             query = "SELECT * FROM " + TABLE_WITH_SALTING + 
                     " WHERE a_integer >= 2 ORDER BY a_integer ASC LIMIT 2";
             stmt = conn.prepareStatement(query);
             rs = stmt.executeQuery();
             assertTrue(rs.next());
             assertEquals(2, rs.getInt(1));
             assertEquals("abc", rs.getString(2));
             assertEquals("123", rs.getString(3));
             assertEquals("def", rs.getString(4));
             assertEquals(222, rs.getInt(5));
             
             assertTrue(rs.next());
             assertEquals(3, rs.getInt(1));
             assertEquals("abc", rs.getString(2));
             assertEquals("123", rs.getString(3));
             assertEquals("ghi", rs.getString(4));
             assertEquals(333, rs.getInt(5));
             assertFalse(rs.next());
         } finally {
             conn.close();
         }
     }
 }
