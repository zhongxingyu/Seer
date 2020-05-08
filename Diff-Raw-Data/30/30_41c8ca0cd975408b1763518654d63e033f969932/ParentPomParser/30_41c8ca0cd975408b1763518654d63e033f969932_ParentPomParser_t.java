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
 import org.apache.commons.digester.Digester;
 import org.xml.sax.SAXException;
 
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.parsers.SAXParserFactory;
 import java.io.*;
 
 
 public class ParentPomParser {
     private LocalRepoResolver localRepoResolver;
 
     public ParentPomParser() {
         this(new LocalRepoResolver());
     }
 
     protected ParentPomParser(LocalRepoResolver localRepoResolver) {
         this.localRepoResolver = localRepoResolver;
     }
 
     public File parse(File pomFile) {
         try {
             if (pomFile == null) {
                 return null;
             }
             return parse(pomFile, new FileInputStream(pomFile));
         } catch (FileNotFoundException e) {
             throw new MavenException(e);
         }
     }
 
     protected File parse(File pomFile, InputStream inputStream) {
         try {
             ParentPomBean parentBean = (ParentPomBean) createDigester().parse(inputStream);
             if (parentBean == null) {
                 return null;
             }
             File localRepoDirectory = localRepoResolver.resolve(new PomFile(pomFile));
             return new File(localRepoDirectory, parentBean.getFilePath());
         } catch (IOException e) {
             e.printStackTrace();
         } catch (SAXException e) {
             e.printStackTrace();
         }
         return null;
     }
 
     private Digester createDigester() {
         try {
             Digester digester = new Digester(SAXParserFactory.newInstance().newSAXParser());
 
             digester.addObjectCreate("project/parent", ParentPomBean.class.getName());
             digester.addBeanPropertySetter("project/parent/groupId", "groupId");
             digester.addBeanPropertySetter("project/parent/artifactId", "artifactId");
             digester.addBeanPropertySetter("project/parent/version", "version");
 
             return digester;
         } catch (ParserConfigurationException e) {
             throw new MavenException(e);
         } catch (SAXException e) {
             throw new MavenException(e);
         }
     }
 
     public static class ParentPomBean {
         private String artifactId;
         private String groupId;
         private String version;
 
         public String getArtifactId() {
             return artifactId;
         }
 
         public void setArtifactId(String artifactId) {
             this.artifactId = artifactId;
         }
 
         public String getGroupId() {
             return groupId;
         }
 
         public void setGroupId(String groupId) {
             this.groupId = groupId;
         }
 
         public String getVersion() {
             return version;
         }
 
         public void setVersion(String version) {
             this.version = version;
         }
 
         public String getFilePath() {
            return groupId.replaceAll("\\.", "/") + "/" + artifactId + "/" + version + "/" + artifactId + "-" + version + ".pom";
         }
     }
 }
