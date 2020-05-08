 /*
  * Licensed to the Sakai Foundation (SF) under one
  * or more contributor license agreements. See the NOTICE file
  * distributed with this work for additional information
  * regarding copyright ownership. The SF licenses this file
  * to you under the Apache License, Version 2.0 (the
  * "License"); you may not use this file except in compliance
  * with the License. You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an
  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  * KIND, either express or implied. See the License for the
  * specific language governing permissions and limitations under the License.
  */
 package org.sakaiproject.nakamura.lite.jdbc;
 
 import static org.mockito.Matchers.anyString;
 import static org.mockito.Mockito.atLeastOnce;
 import static org.mockito.Mockito.verify;
 import static org.mockito.Mockito.when;
 
 import com.google.common.collect.ImmutableMap;
 import com.google.common.collect.ImmutableMap.Builder;
 import com.google.common.collect.ImmutableSet;
 import com.google.common.collect.Lists;
 import com.google.common.collect.Maps;
 
 import junit.framework.Assert;
 
 import org.apache.commons.lang.StringUtils;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.mockito.Answers;
 import org.mockito.ArgumentCaptor;
 import org.mockito.Mock;
 import org.mockito.runners.MockitoJUnitRunner;
 import org.sakaiproject.nakamura.lite.storage.jdbc.JDBCStorageClient;
 import org.sakaiproject.nakamura.lite.storage.jdbc.JDBCStorageClientPool;
 
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.text.MessageFormat;
 import java.util.Map;
 import java.util.Set;
 
 /**
  *
  */
 @RunWith(MockitoJUnitRunner.class)
 public class JDBCStorageClientTest {
   JDBCStorageClient client;
 
   @Mock
   JDBCStorageClientPool connPool;
 
   @Mock(answer = Answers.RETURNS_DEEP_STUBS)
   Connection conn;
 
 
   @Mock
   PreparedStatement ps2;
 
 
   @Mock
   ResultSet rs2;
 
   Map<String, Object> properties = Maps.newHashMap();
   Map<String, Object> sqlConfig = Maps.newHashMap();
 
   @Before
   public void setUp() throws Exception {
     // have the pool return the connection we control
     when(connPool.getConnection()).thenReturn(conn);
 
     // funnel in data when the indexed columns are looked up
     when(conn.prepareStatement(anyString())).thenReturn(ps2);
     when(ps2.executeQuery()).thenReturn(rs2);
 
     // give back some bogus db vendor data
     when(conn.getMetaData().getDatabaseProductName()).thenReturn("Sakai Nakamura");
     when(conn.getMetaData().getDatabaseMajorVersion()).thenReturn(1);
     when(conn.getMetaData().getDatabaseMinorVersion()).thenReturn(0);
 
 
     sqlConfig = new JDBCStorageClientPool().getSqlConfig(conn);
     Set<String> colnames = ImmutableSet.of("conjunctions:key1","conjunctions:key2","conjunctions:key3","conjunctions:key4",
             "conjunctions:testKey1","conjunctions:testKey2","conjunctions:testKey3","conjunctions:testKey4");
    Builder<String, String> b = ImmutableMap.builder();
    int i = 0;
    for ( String k : colnames) {
        b.put(k, "v"+i);
        i++;
    }
 
    client = new JDBCStorageClient(connPool, properties, sqlConfig, colnames, ImmutableSet.of("test=String","test2=String[]"), b.build());
   }
 
   @Test
   public void test2TermsAnd() throws Exception {
 
     String keySpace = "cn";
     String columnFamily = "conjunctions";
     Map<String, Object> props = Maps.newLinkedHashMap();
     props.put("key1", "val1");
     props.put("key2", "val2");
     client.find(keySpace, columnFamily, props);
 
     ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
     verify(conn, atLeastOnce()).prepareStatement(sqlCaptor.capture());
     verify(ps2).setObject(1, "key1");
     verify(ps2).setObject(2, "val1");
     verify(ps2).setObject(3, "key2");
     verify(ps2).setObject(4, "val2");
 
     String sqlTemplate = (String) sqlConfig.get("block-find");
     String[] statementParts = StringUtils.split(sqlTemplate, ';');
 
     StringBuilder tables = new StringBuilder().append(
         MessageFormat.format(statementParts[1], "a0")).append(
         MessageFormat.format(statementParts[1], "a1"));
     StringBuilder where = new StringBuilder().append(" (")
         .append(MessageFormat.format(statementParts[2], "a0")).append(") AND (")
         .append(MessageFormat.format(statementParts[2], "a1")).append(") AND");
 
     String expectedSql = MessageFormat.format(statementParts[0], tables.toString(),
         where.toString());
 
     String sql = sqlCaptor.getValue();
     Assert.assertEquals(expectedSql, sql);
   }
 
   @Test
   public void test2TermsOr() throws Exception {
 
     String keySpace = "cn";
     String columnFamily = "conjunctions";
     Map<String, Object> container = Maps.newHashMap();
     Map<String, Object> orSet = Maps.newLinkedHashMap();
     orSet.put("key1", "val1");
     orSet.put("key2", "val2");
     container.put("orSet", orSet);
     client.find(keySpace, columnFamily, container);
 
     ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
     verify(conn, atLeastOnce()).prepareStatement(sqlCaptor.capture());
     verify(ps2).setObject(1, "key1");
     verify(ps2).setObject(2, "val1");
     verify(ps2).setObject(3, "key2");
     verify(ps2).setObject(4, "val2");
 
     String sqlTemplate = (String) sqlConfig.get("block-find");
     String[] statementParts = StringUtils.split(sqlTemplate, ';');
 
     StringBuilder tables = new StringBuilder().append(
         MessageFormat.format(statementParts[1], "a0")).append(
         MessageFormat.format(statementParts[1], "a1"));
     StringBuilder where = new StringBuilder().append(" ( (")
         .append(MessageFormat.format(statementParts[2], "a0")).append(") OR (")
         .append(MessageFormat.format(statementParts[2], "a1")).append(")) AND");
 
     String expectedSql = MessageFormat.format(statementParts[0], tables.toString(),
         where.toString());
 
     String sql = sqlCaptor.getValue();
     Assert.assertEquals(expectedSql, sql);
   }
 
   @Test
   public void test1TermAnd2TermsOr1TermAnd() throws Exception {
 
     String keySpace = "cn";
     String columnFamily = "conjunctions";
     Map<String, Object> orSet = Maps.newLinkedHashMap();
     orSet.put("key1", "val1");
     orSet.put("key2", "val2");
 
     Map<String, Object> container = Maps.newLinkedHashMap();
     container.put("testKey1", "testVal1");
     container.put("orSet", orSet);
     container.put("testKey2", "testVal2");
 
     client.find(keySpace, columnFamily, container);
 
     ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
     verify(conn, atLeastOnce()).prepareStatement(sqlCaptor.capture());
     int i = 0;
     verify(ps2).setObject(++i, "testKey1");
     verify(ps2).setObject(++i, "testVal1");
     verify(ps2).setObject(++i, "key1");
     verify(ps2).setObject(++i, "val1");
     verify(ps2).setObject(++i, "key2");
     verify(ps2).setObject(++i, "val2");
     verify(ps2).setObject(++i, "testKey2");
     verify(ps2).setObject(++i, "testVal2");
 
     String sqlTemplate = (String) sqlConfig.get("block-find");
     String[] statementParts = StringUtils.split(sqlTemplate, ';');
 
     StringBuilder tables = new StringBuilder()
         .append(MessageFormat.format(statementParts[1], "a0"))
         .append(MessageFormat.format(statementParts[1], "a1"))
         .append(MessageFormat.format(statementParts[1], "a2"))
         .append(MessageFormat.format(statementParts[1], "a3"));
     StringBuilder where = new StringBuilder().append(" (")
         .append(MessageFormat.format(statementParts[2], "a0")).append(") AND (")
         .append(" (").append(MessageFormat.format(statementParts[2], "a1"))
         .append(") OR (").append(MessageFormat.format(statementParts[2], "a2"))
         .append(")) AND (").append(MessageFormat.format(statementParts[2], "a3"))
         .append(") AND");
 
     String expectedSql = MessageFormat.format(statementParts[0], tables.toString(),
         where.toString());
 
     String sql = sqlCaptor.getValue();
     Assert.assertEquals(expectedSql, sql);
   }
 
   @Test
   public void test2TermsAnd2TermsOr2TermsOr() throws Exception {
 
     String keySpace = "cn";
     String columnFamily = "conjunctions";
     Map<String, Object> orSet1 = Maps.newLinkedHashMap();
     orSet1.put("key1", "val1");
     orSet1.put("key2", "val2");
     Map<String, Object> orSet2 = Maps.newLinkedHashMap();
     orSet2.put("key3", "val3");
     orSet2.put("key4", "val4");
 
     Map<String, Object> container = Maps.newLinkedHashMap();
     container.put("testKey1", "testVal1");
     container.put("testKey2", "testVal2");
     container.put("orSet1", orSet1);
     container.put("orSet2", orSet2);
 
     client.find(keySpace, columnFamily, container);
 
     ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
     verify(conn, atLeastOnce()).prepareStatement(sqlCaptor.capture());
     int i = 0;
     verify(ps2).setObject(++i, "testKey1");
     verify(ps2).setObject(++i, "testVal1");
     verify(ps2).setObject(++i, "testKey2");
     verify(ps2).setObject(++i, "testVal2");
     verify(ps2).setObject(++i, "key1");
     verify(ps2).setObject(++i, "val1");
     verify(ps2).setObject(++i, "key2");
     verify(ps2).setObject(++i, "val2");
     verify(ps2).setObject(++i, "key3");
     verify(ps2).setObject(++i, "val3");
     verify(ps2).setObject(++i, "key4");
     verify(ps2).setObject(++i, "val4");
 
     String sqlTemplate = (String) sqlConfig.get("block-find");
     String[] statementParts = StringUtils.split(sqlTemplate, ';');
 
     StringBuilder tables = new StringBuilder()
         .append(MessageFormat.format(statementParts[1], "a0"))
         .append(MessageFormat.format(statementParts[1], "a1"))
         .append(MessageFormat.format(statementParts[1], "a2"))
         .append(MessageFormat.format(statementParts[1], "a3"))
         .append(MessageFormat.format(statementParts[1], "a4"))
         .append(MessageFormat.format(statementParts[1], "a5"));
     StringBuilder where = new StringBuilder().append(" (")
         .append(MessageFormat.format(statementParts[2], "a0")).append(") AND (")
         .append(MessageFormat.format(statementParts[2], "a1")).append(") AND")
         .append(" ( (").append(MessageFormat.format(statementParts[2], "a2"))
         .append(") OR (").append(MessageFormat.format(statementParts[2], "a3"))
         .append(")) AND").append(" ( (")
         .append(MessageFormat.format(statementParts[2], "a4")).append(") OR (")
         .append(MessageFormat.format(statementParts[2], "a5")).append(")) AND");
 
     String expectedSql = MessageFormat.format(statementParts[0], tables.toString(),
         where.toString());
 
     String sql = sqlCaptor.getValue();
     Assert.assertEquals(expectedSql, sql);
   }
 
   @Test
   public void test2Terms1Indexed() throws Exception {
 
     String keySpace = "cn";
     String columnFamily = "conjunctions";
     Map<String, Object> props = Maps.newLinkedHashMap();
     props.put("key1", "val1");
     props.put("key2not", "val2");
     client.find(keySpace, columnFamily, props);
 
     ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
     verify(conn, atLeastOnce()).prepareStatement(sqlCaptor.capture());
     verify(ps2).setObject(1, "key1");
     verify(ps2).setObject(2, "val1");
 
     String sqlTemplate = (String) sqlConfig.get("block-find");
     String[] statementParts = StringUtils.split(sqlTemplate, ';');
 
     StringBuilder tables = new StringBuilder().append(MessageFormat.format(
         statementParts[1], "a0"));
     StringBuilder where = new StringBuilder().append(" (").append(
         MessageFormat.format(statementParts[2], "a0")).append(") AND");
 
     String expectedSql = MessageFormat.format(statementParts[0], tables.toString(),
         where.toString());
 
     String sql = sqlCaptor.getValue();
     Assert.assertEquals(expectedSql, sql);
   }
 
   @Test
   public void test2TermsMultivalueAnd() throws Exception {
 
     String keySpace = "cn";
     String columnFamily = "conjunctions";
     Map<String, Object> props = Maps.newLinkedHashMap();
     props.put("key1", Lists.immutableList("val1", "val2"));
     props.put("key2", "val2");
     client.find(keySpace, columnFamily, props);
 
     ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
     verify(conn, atLeastOnce()).prepareStatement(sqlCaptor.capture());
     int i = 0;
     verify(ps2).setObject(++i, "key1");
     verify(ps2).setObject(++i, "val1");
     verify(ps2).setObject(++i, "key1");
     verify(ps2).setObject(++i, "val2");
     verify(ps2).setObject(++i, "key2");
     verify(ps2).setObject(++i, "val2");
 
     String sqlTemplate = (String) sqlConfig.get("block-find");
     String[] statementParts = StringUtils.split(sqlTemplate, ';');
 
     StringBuilder tables = new StringBuilder().append(
         MessageFormat.format(statementParts[1], "a0")).append(
         MessageFormat.format(statementParts[1], "a1")).append(
         MessageFormat.format(statementParts[1], "a2"));
     StringBuilder where = new StringBuilder().append(" (")
         .append(MessageFormat.format(statementParts[2], "a0")).append(") AND (")
         .append(MessageFormat.format(statementParts[2], "a1")).append(") AND (")
         .append(MessageFormat.format(statementParts[2], "a2")).append(") AND");
 
     String expectedSql = MessageFormat.format(statementParts[0], tables.toString(),
         where.toString());
 
     String sql = sqlCaptor.getValue();
     Assert.assertEquals(expectedSql, sql);
   }
 
   @Test
   public void test2TermsMultivalueOr() throws Exception {
 
     String keySpace = "cn";
     String columnFamily = "conjunctions";
     Map<String, Object> container = Maps.newHashMap();
     Map<String, Object> orSet = Maps.newLinkedHashMap();
     orSet.put("key1", Lists.immutableList("val1", "val2"));
     orSet.put("key2", "val2");
     container.put("orSet", orSet);
     client.find(keySpace, columnFamily, container);
 
     ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
     verify(conn, atLeastOnce()).prepareStatement(sqlCaptor.capture());
     int i = 0;
     verify(ps2).setObject(++i, "key1");
     verify(ps2).setObject(++i, "val1");
     verify(ps2).setObject(++i, "key1");
     verify(ps2).setObject(++i, "val2");
     verify(ps2).setObject(++i, "key2");
     verify(ps2).setObject(++i, "val2");
 
     String sqlTemplate = (String) sqlConfig.get("block-find");
     String[] statementParts = StringUtils.split(sqlTemplate, ';');
 
     StringBuilder tables = new StringBuilder().append(
         MessageFormat.format(statementParts[1], "a0")).append(
         MessageFormat.format(statementParts[1], "a1"));
     StringBuilder where = new StringBuilder().append(" ( (")
         .append(MessageFormat.format(statementParts[2], "a0")).append(") OR (")
         .append(MessageFormat.format(statementParts[2], "a0")).append(") OR (")
         .append(MessageFormat.format(statementParts[2], "a1")).append(")) AND");
 
     String expectedSql = MessageFormat.format(statementParts[0], tables.toString(),
         where.toString());
 
     String sql = sqlCaptor.getValue();
     Assert.assertEquals(expectedSql, sql);
   }
 }
