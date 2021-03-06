 /*
  * Copyright (c) 2010. The Codehaus. All Rights Reserved.
  *
  *   Licensed under the Apache License, Version 2.0 (the "License");
  *   you may not use this file except in compliance with the License.
  *   You may obtain a copy of the License at
  *   http://www.apache.org/licenses/LICENSE-2.0
  *
  *   Unless required by applicable law or agreed to in writing, software
  *   distributed under the License is distributed on an "AS IS" BASIS,
  *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *   See the License for the specific language governing permissions and
  *   limitations under the License.
  */
 
 package org.codehaus.httpcache4j.storage.jdbc;
 
 import org.apache.commons.io.FileUtils;
 import org.codehaus.httpcache4j.cache.CacheStorage;
 import org.codehaus.httpcache4j.cache.ConcurrentCacheStorageAbstractTest;
 import org.codehaus.httpcache4j.util.TestUtil;
 import org.junit.AfterClass;
 import org.junit.BeforeClass;
 
 import java.io.File;
 
 /**
  * @author <a href="mailto:hamnis@codehaus.org">Erlend Hamnaberg</a>
  * @version $Revision: $
  */
 public class ConcurrentH2StorageTest extends ConcurrentCacheStorageAbstractTest {
     private static File testFile;
     private static H2CacheStorage storage;
 
     @BeforeClass
    public static void createH2CacheStorage() {
        testFile = TestUtil.getTestFile("target/storage/concurrent");
         storage = new H2CacheStorage(testFile, true);
     }
 
     @Override
     protected CacheStorage createCacheStorage() {
         return storage;
     }
 
     @AfterClass
     public static void afterClass() {
         FileUtils.deleteQuietly(testFile);
     }
 }
