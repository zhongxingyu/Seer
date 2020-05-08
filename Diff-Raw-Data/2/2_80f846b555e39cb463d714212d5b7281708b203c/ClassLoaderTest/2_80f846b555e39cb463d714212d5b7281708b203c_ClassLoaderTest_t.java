 /*
  * Licensed to the Apache Software Foundation (ASF) under one or more
  * contributor license agreements.  See the NOTICE file distributed with
  * this work for additional information regarding copyright ownership.
  * The ASF licenses this file to You under the Apache License, Version 2.0
  * (the "License"); you may not use this file except in compliance with
  * the License.  You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.apache.servicemix.jbi.loaders;
 
 import java.io.File;
 import java.net.URL;
 import java.net.URLClassLoader;
 
import org.apache.xbean.classloader.JarFileClassLoader;
 
 import junit.framework.TestCase;
 
 /**
  * @version $Revision$
  */
 public class ClassLoaderTest extends TestCase {
 
 	public static class TestClass {
 	}
 	
 	public void testParentFirstClassLoader() throws Exception {
 		URLClassLoader pcl = (URLClassLoader) getClass().getClassLoader();
 		ClassLoader clsLoader = new JarFileClassLoader("", pcl.getURLs(), pcl, false, new String[0], new String[0]);
 		Class  clazz = clsLoader.loadClass(TestClass.class.getName());
 		assertSame(TestClass.class, clazz);
 	}
 	
 	public void testSelfFirstClassLoader() throws Exception {
 		URLClassLoader pcl = (URLClassLoader) getClass().getClassLoader();
 		ClassLoader clsLoader = new JarFileClassLoader("", pcl.getURLs(), pcl, true, new String[0], new String[0]);
 		Class  clazz = clsLoader.loadClass(TestClass.class.getName());
 		assertNotSame(TestClass.class, clazz);
 	}
     
     public void testParentFirstResource() throws Exception {
         URLClassLoader pcl = (URLClassLoader) getClass().getClassLoader();
         URL url = getClass().getResource("jndi.properties");
         url = new File(url.toURI()).getParentFile().toURL();
         ClassLoader clsLoader = new JarFileClassLoader("", new URL[] { url }, pcl, false, new String[0], new String[0]);
         URL res1 = clsLoader.getResource("jndi.properties");
         URL res2 = pcl.getResource("jndi.properties");
         assertEquals(res2, res1);
     }
     
     public void testSelfFirstResource() throws Exception {
         URLClassLoader pcl = (URLClassLoader) getClass().getClassLoader();
         URL url = getClass().getResource("jndi.properties");
         url = new File(url.toURI()).getParentFile().toURL();
         ClassLoader clsLoader = new JarFileClassLoader("", new URL[] { url }, pcl, true, new String[0], new String[0]);
         URL res1 = clsLoader.getResource("jndi.properties");
         URL res2 = pcl.getResource("jndi.properties");
         assertFalse(res2.equals(res1));
     }
     
 }
