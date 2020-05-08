 /*
  * Licensed to the Apache Software Foundation (ASF) under one
  * or more contributor license agreements.  See the NOTICE file
  * distributed with this work for additional information
  * regarding copyright ownership.  The ASF licenses this file
  * to you under the Apache License, Version 2.0 (the
  * "License"); you may not use this file except in compliance
  * with the License.  You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an
  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  * KIND, either express or implied.  See the License for the
  * specific language governing permissions and limitations
  * under the License.
  */
 package org.apache.hive.ptest.conf;
 
 import java.util.Properties;
 import java.util.Set;
 
 import junit.framework.Assert;
 
 import org.junit.Test;
 
 import com.google.common.collect.Maps;
 import com.google.common.collect.Sets;
 import com.google.common.io.Resources;
 
 public class TestConfiguration {
 
   @Test
   public void testGettersSetters() throws Exception {
     Configuration conf = Configuration.fromInputStream(
         Resources.getResource("test-configuration.properties").openStream());
     Set<Host> expectedHosts = Sets.newHashSet(new Host("localhost", "hiveptest", new String[]{"/home/hiveptest"}, 2));
     Assert.assertEquals(expectedHosts, conf.getHosts());
     Assert.assertEquals("/home/brock/.ssh/id_rsa", conf.getPrivateKey());
     Assert.assertEquals("git://github.com/apache/hive.git", conf.getRepository());
     Assert.assertEquals("apache-github", conf.getRepositoryName());
     Assert.assertEquals("trunk", conf.getBranch());
     Assert.assertEquals("/tmp/hive-ptest-units", conf.getWorkingDirectory());
     Assert.assertEquals("-Dtest.continue.on.failure=true -Dtest.silent=false", conf.getAntArgs());   
     Assert.assertNotNull(conf.toString());
 
    Assert.assertNull(conf.getPatch());
     conf.setPatch("Patch");
     Assert.assertEquals("Patch", conf.getPatch());
     
     conf.setRepository("Repository");
     Assert.assertEquals("Repository", conf.getRepository());
     
     conf.setRepositoryName("RepositoryName");
     Assert.assertEquals("RepositoryName", conf.getRepositoryName());
     
     conf.setBranch("Branch");
     Assert.assertEquals("Branch", conf.getBranch());
     
     conf.setAntArgs("AntArgs");
     Assert.assertEquals("AntArgs", conf.getAntArgs());
     
   }
   @Test(expected=IllegalStateException.class)
   public void testNoHosts() throws Exception {
     new Configuration(new Context());
   }
   @Test
   public void testContext() throws Exception {
     Properties properties = new Properties();
     properties.load(Resources.getResource("test-configuration.properties").openStream());
     Context context = new Context(Maps.fromProperties(properties));    
     Assert.assertEquals(context.getParameters(), (new Configuration(context)).getContext().getParameters());
     
   }
 }
