 /*
  * This is a common dao with basic CRUD operations and is not limited to any
  * persistent layer implementation
  *
 * Copyright (C) 2008  Imran M Yousuf (imyousuf@smartitengineering.com)
  *
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 3 of the License, or (at your option) any later version.
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Lesser General Public License for more details.
  * You should have received a copy of the GNU Lesser General Public
  * License along with this library; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
  */
 package com.smartitengineering.dao.impl.hbase;
 
 import com.smartitengineering.dao.impl.hbase.spi.FilterConfig;
 import com.smartitengineering.dao.impl.hbase.spi.FilterConfigs;
 import com.smartitengineering.dao.impl.hbase.spi.impl.FilterConfigImpl;
 import com.smartitengineering.dao.impl.hbase.spi.impl.JsonConfigLoader;
 import com.smartitengineering.dao.impl.hbase.spi.impl.SchemaInfoProviderBaseConfig;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.StringWriter;
 import java.util.LinkedHashMap;
 import java.util.Map;
 import org.apache.commons.io.IOUtils;
 import org.codehaus.jackson.map.ObjectMapper;
 import org.junit.Assert;
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 /**
  *
  * @author imyousuf
  */
 public class JsonConfigLoaderTest {
 
   private static final FilterConfigs CONFIGS = new FilterConfigs();
 
   @BeforeClass
   public static void setupConfigs() {
     Map<String, FilterConfig> map = new LinkedHashMap<String, FilterConfig>();
     FilterConfigImpl configImpl = new FilterConfigImpl();
     configImpl.setColumnFamilyAsString("test");
     configImpl.setColumnQualifierAsString("id");
     configImpl.setFilterOnIfMissing(true);
     configImpl.setFilterOnLatestVersionOnly(true);
     configImpl.setQualifierARangePrefix(true);
     configImpl.setFilterOnRowId(true);
     map.put("id", configImpl);
     configImpl = new FilterConfigImpl();
     configImpl.setColumnFamilyAsString("test");
     configImpl.setColumnQualifierAsString("name");
     map.put("name", configImpl);
     CONFIGS.setConfigs(map);
   }
 
   @Test
   public void deserializeFilterConfigsFromSerializedString() throws IOException {
     ObjectMapper mapper = new ObjectMapper();
     StringWriter writer = new StringWriter();
     mapper.writeValue(writer, CONFIGS);
     InputStream stream = IOUtils.toInputStream(writer.toString());
     FilterConfigs readConfigs = JsonConfigLoader.parseJsonAsFilterConfigMap(stream);
     Assert.assertEquals(CONFIGS, readConfigs);
   }
 
   @Test
   public void deserializeFilterConfigsFromClasspath() throws IOException {
     InputStream stream = getClass().getClassLoader().getResourceAsStream(
         "com/smartitengineering/dao/impl/hbase/Configs.json");
     Assert.assertNotNull(stream);
     FilterConfigs readConfigs = JsonConfigLoader.parseJsonAsFilterConfigMap(stream);
     Assert.assertEquals(CONFIGS, readConfigs);
   }
 
   @Test
   public void deserializeBaseConfigFromClasspath() throws IOException {
     InputStream stream = getClass().getClassLoader().getResourceAsStream(
         "com/smartitengineering/dao/impl/hbase/BaseConfig.json");
     Assert.assertNotNull(stream);
     SchemaInfoProviderBaseConfig readConfig = JsonConfigLoader.parseJsonAsBaseConfig(stream);
     Assert.assertEquals("testNS", readConfig.getSchemaNamespace());
     Assert.assertEquals("test", readConfig.getMainTableName());
     Assert.assertEquals(true, readConfig.isTransactionalDomain());
     Assert.assertEquals("fam", readConfig.getVersionColumnFamily());
     Assert.assertEquals("cell", readConfig.getVersionColumnQualifier());
   }
 }
