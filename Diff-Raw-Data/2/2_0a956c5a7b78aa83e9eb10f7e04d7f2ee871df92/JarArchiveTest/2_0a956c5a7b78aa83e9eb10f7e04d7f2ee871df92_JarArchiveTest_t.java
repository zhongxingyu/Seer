 /**
  * Copyright (C) 2008 Ovea <dev@testatoo.org>
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package org.testatoo.selenium.server.archive;
 
 import org.junit.Test;
 
 import java.io.File;
 
 import static org.junit.Assert.assertTrue;
 import static org.testatoo.selenium.server.util.FileUtils.createTemporaryFolder;
 import static org.testatoo.selenium.server.util.ResourceUtils.url;
 
 public final class JarArchiveTest {
     @Test
     public void test_extract() throws Exception {
        Archive a = ArchiveFactory.jar(url(new File("src/main/resources/org/testatoo/selenium/server/embedded/log4j-1.2.16.jar")));
         File dest = createTemporaryFolder("testatoo-");
         System.out.println("Temp dir created: " + dest);
         a.extract(dest, "org/apache/log4j/xml/**", "org/apache/log4j/jdbc/**");
         System.out.println("Files extracted");
         //Thread.sleep(10000);
         assertTrue(new File(dest, "org/apache/log4j/xml/XMLLayout.class").exists());
     }
 }
