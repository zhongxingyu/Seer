 /**
  * Copyright 1&1 Internet AG, https://github.com/1and1/
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package net.oneandone.lavender.modules;
 
 import org.junit.Test;
 
 import java.util.Collection;
 import java.util.Properties;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertFalse;
 
 public class SvnModuleConfigTest {
     @Test
     public void empty() {
         assertEquals(0, SvnModuleConfig.parse(new Properties()).size());
     }
 
     @Test
     public void one() {
         Properties props;
         Collection<SvnModuleConfig> result;
         SvnModuleConfig config;
 
         props = new Properties();
         props.put("svn.foo", "svn");
        props.put("svn.foo.targetPathPrefix", "prefix");
         props.put("svn.foo.lavendelize", "false");
         result = SvnModuleConfig.parse(props);
         assertEquals(1, result.size());
         config = result.iterator().next();
         assertEquals("foo", config.folder);
         assertFalse(config.lavendelize);
        assertEquals("prefix", config.targetPathPrefix);
         assertEquals("svn", config.svnurl);
     }
 
     @Test
     public void more() {
         Properties props;
         Collection<SvnModuleConfig> result;
 
         props = new Properties();
         props.put("svn.foo", "1");
         props.put("svn.bar", "2");
         props.put("svn.baz", "3");
         result = SvnModuleConfig.parse(props);
         assertEquals(3, result.size());
     }
 }
