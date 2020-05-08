 /*
  * Copyright 2012 htfv (Aliaksei Lahachou)
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
 
 package com.github.htfv.maven.plugins.buildconfigurator.core;
 
 import static org.hamcrest.CoreMatchers.is;
 import static org.hamcrest.CoreMatchers.not;
 import static org.hamcrest.CoreMatchers.nullValue;
 import static org.junit.Assert.assertThat;
 import static org.junit.matchers.JUnitMatchers.containsString;
 
 import java.io.File;
 
 import org.apache.maven.model.Resource;
 import org.junit.Before;
 import org.junit.Test;
 
 import com.github.htfv.maven.plugins.buildconfigurator.model.BCPropertyFile;
 
 public class PropertyFilesTest extends AbstractBuildConfiguratorTest
 {
     private BCPropertyFile propertyFile;
     private Resource resource;
 
    @Test
    public void environmentPropertiesAreAccessibleInEL() throws Exception
    {
        assertThat(
                getProjectProperty("java.home"),
                is(System.getenv("JAVA_HOME")));
    }

     @Before
     public void initializeMocks()
     {
         //
         // model
         //
 
         propertyFile = addPropertyFile("src/test/resources/customized.properties");
         addPropertyFile(new File("src/test/resources/default.properties").getAbsolutePath());
         addPropertyFile("src/test/resources/gb2312.properties", "GB2312");
         addPropertyFile("src/test/resources/shift-jis.properties", "Shift-JIS");
 
         //
         // project
         //
 
         setProjectProperty("existing.value", "loaded from pom.xml");
         resource = addProjectResource("${project.value}");
     }
 
     @Test
     public void modelIsReinterpolated() throws Exception
     {
         configureProject();
 
         assertThat(
                 resource.getDirectory(),
                 not(containsString("${")));
     }
 
     @Test
     public void projectPropertiesAreAccessibleInEL() throws Exception
     {
         assertThat(
                 getProjectProperty("project.value"),
                 is("loaded from pom.xml"));
     }
 
     @Test
     public void propertiesAreImmutable() throws Exception
     {
         assertThat(
                 getProjectProperty("existing.value"),
                 is("loaded from pom.xml"));
         assertThat(
                 getNewProperty("existing.value"),
                 is(nullValue()));
     }
 
     @Test
     public void propertiesAreLoadedInCorrectOrder() throws Exception
     {
         assertThat(
                 getProjectProperty("customized.value"),
                 is("loaded from customized.properties"));
         assertThat(
                 getProjectProperty("default.value"),
                 is("loaded from default.properties"));
     }
 
     @Test
     public void propertisAreLoadedWithCustomEncoding() throws Exception
     {
         assertThat(
                 getProjectProperty("chinese"),
                 is("中文"));
         assertThat(
                 getProjectProperty("japanese"),
                 is("日本語"));
     }
 
     @Test
     public void propertyFilesAreSkipped() throws Exception
     {
         propertyFile.setAction("skip");
 
         assertThat(
                 getProjectProperty("customized.value"),
                 is("loaded from default.properties"));
     }
 
     @Test
     public void strConcatIsAvailableinEL() throws Exception
     {
         assertThat(
                 getProjectProperty("concat.null.eenie"),
                 is("Eenie"));
         assertThat(
                 getProjectProperty("concat.meenie.null.miney"),
                 is("MeenieMiney"));
         assertThat(
                 getProjectProperty("concat.mo.catch.null.a"),
                 is("MoCatchA"));
         assertThat(
                 getProjectProperty("concat.tiger.by.the.null.toe"),
                 is("TigerByTheToe"));
     }
 
     @Test
     public void strJoinIsAvailableinEL() throws Exception
     {
         assertThat(
                 getProjectProperty("join.inky.null"),
                 is("Inky"));
         assertThat(
                 getProjectProperty("join.binky.empty.bonky"),
                 is("Binky Bonky"));
         assertThat(
                 getProjectProperty("join.daddy.had.a.donkey"),
                 is("Daddy Had A Donkey"));
         assertThat(
                 getProjectProperty("join.donkey.died.null.daddy.cried"),
                 is("Donkey Died Daddy Cried"));
     }
 
     @Test
     public void unloadedPropertiesAreCorrectlyResolved() throws Exception
     {
         assertThat(
                 getProjectProperty("factorial5"),
                 is("120"));
     }
 }
