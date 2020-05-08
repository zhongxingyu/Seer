 /**
  * Copyright to the original author or authors.
  *
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in
  * compliance with the License. You may obtain a copy of the License at:
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software distributed under the License is
  * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and limitations under the License.
  */
 package fitnesse.maven.io;
 
 import fitnesse.maven.util.LocalRepoResolver;
 import junit.framework.TestCase;
 import static org.mockito.Mockito.mock;
 import static org.mockito.Mockito.when;
 
 import java.io.ByteArrayInputStream;
 import java.io.File;
 
 
 public class ParentPomParserTest extends TestCase {
     private static final PomFile POM_FILE = new PomFile(new File("blah/pom.xml"));
     private static final File PARENT_POM_FILE = new File("parent/pom.xml");
     private LocalRepoResolver localRepoResolver;
     private ParentPomParser parser;
 
     protected void setUp() throws Exception {
         super.setUp();
         localRepoResolver = mock(LocalRepoResolver.class);
         parser = new ParentPomParser(localRepoResolver);
     }
 
    public void test_parse_PomHasParent_GroupIdHasPeriods() {
        String pomFile = "<?xml version=\"1.0\"?>\n" +
                "<project xmlns=\"http://maven.apache.org/POM/4.0.0\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
                "         xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/maven-v4_0_0.xsd\">\n" +
                "\n" +
                "    <modelVersion>4.0.0</modelVersion>\n" +
                "\n" +
                "    <parent>\n" +
                "        <groupId>fitnesse.maven.groupId</groupId>\n" +
                "        <artifactId>artifactId</artifactId>\n" +
                "        <version>version</version>\n" +
                "    </parent>\n" +
                "\n" +
                "</project>";

        when(localRepoResolver.resolve(POM_FILE)).thenReturn(PARENT_POM_FILE);

        File file = parser.parse(POM_FILE.getFile(), new ByteArrayInputStream(pomFile.getBytes()));

        assertNotNull(file);
        assertTrue(file.getPath().contains("fitnesse/maven/groupId/artifactId/version/artifactId-version.pom"));
    }

     public void test_parse_PomHasParent() {
         String pomFile = "<?xml version=\"1.0\"?>\n" +
                 "<project xmlns=\"http://maven.apache.org/POM/4.0.0\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
                 "         xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/maven-v4_0_0.xsd\">\n" +
                 "\n" +
                 "    <modelVersion>4.0.0</modelVersion>\n" +
                 "\n" +
                 "    <parent>\n" +
                 "        <groupId>groupId</groupId>\n" +
                 "        <artifactId>artifactId</artifactId>\n" +
                 "        <version>version</version>\n" +
                 "    </parent>\n" +
                 "\n" +
                 "</project>";
 
         when(localRepoResolver.resolve(POM_FILE)).thenReturn(PARENT_POM_FILE);
 
         File file = parser.parse(POM_FILE.getFile(), new ByteArrayInputStream(pomFile.getBytes()));
 
         assertNotNull(file);
         assertTrue(file.getPath().contains("groupId/artifactId/version/artifactId-version.pom"));
     }
 
     public void test_parse_PomHasNoParent() {
         String pomFile = "<?xml version=\"1.0\"?>\n" +
                 "<project xmlns=\"http://maven.apache.org/POM/4.0.0\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
                 "         xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/maven-v4_0_0.xsd\">\n" +
                 "\n" +
                 "    <modelVersion>4.0.0</modelVersion>\n" +
                 "\n" +
                 "</project>";
 
         when(localRepoResolver.resolve(POM_FILE)).thenReturn(PARENT_POM_FILE);
 
         assertNull(parser.parse(POM_FILE.getFile(), new ByteArrayInputStream(pomFile.getBytes())));
     }
 
     public void test_parse_NullFile() {
         assertNull(parser.parse(null));
     }
 }
