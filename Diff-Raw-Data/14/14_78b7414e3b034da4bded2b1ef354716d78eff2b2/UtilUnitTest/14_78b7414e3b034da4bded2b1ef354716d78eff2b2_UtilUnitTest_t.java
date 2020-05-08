 /*
  * Copyright 2007-2009 Alexander Fabisch
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
 
 package com.ev.util;
 
 import com.ev.launch.Initializer;
 import java.io.File;
 import java.util.Properties;
 import org.junit.*;
 import static org.junit.Assert.*;
 
 /**
  * @author <a href="mailto:afabisch@tzi.de">Alexander Fabisch</a>
  */
 public class UtilUnitTest {
 
     private static final String DIR = "testdir";
     private static final String FILE = "test.properties";
 
     public UtilUnitTest() {
     }
 
     @BeforeClass
     public static void setUpClass() throws Exception {
         Initializer.initLogging();
         Initializer.initConfiguration();
         deleteNewDir();
     }
 
     @AfterClass
     public static void tearDownClass() throws Exception {
         deleteNewDir();
     }
 
     @Test
     public void readFileSuccess() throws Exception {
         String file = FILE;
         Properties store = new Properties();
         Util.readFile(file, store);
         assertTrue("should be able to load file content", store.getProperty("test").equals("successful"));
     }
 
     @Test
     public void writeNewFileInNewDir() throws Exception {
         String path = DIR + File.separator + FILE;
         File dir = new File(DIR);
         assertFalse("dir should not be there yet", dir.exists());
         Properties store = new Properties();
         Util.writeFile(path, store, "");
         assertTrue("directory should exist", dir.exists());
         assertTrue("dir should be a directory", dir.isDirectory());
     }
 
     @Test
     public void writeAndReadContent() throws Exception { // TODO DSL
        String path = DIR + File.separator + FILE;
         Properties store = new Properties();
         Util.readFile(path, store);
         store.setProperty("test2", "successful");
         Util.writeFile(path, store, "");
         store = new Properties();
         Util.readFile(path, store);
         assertEquals("successful", store.getProperty("test2"));
         store.clear();
         store.setProperty("test", "successful");
         Util.writeFile(path, store, "");
     }
 
     private static void deleteNewDir() {
         File file = new File(DIR + File.separator + FILE);
         File dir = new File(DIR);
         if (file.exists()) {
             file.delete();
         }
         if (dir.exists()) {
             dir.delete();
         }
     }
 
 }
