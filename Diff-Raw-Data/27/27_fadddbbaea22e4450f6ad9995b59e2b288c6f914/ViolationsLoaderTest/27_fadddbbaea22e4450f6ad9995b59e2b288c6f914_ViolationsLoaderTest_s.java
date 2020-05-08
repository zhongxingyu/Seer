 /*
  * Copyright (C) 2010 Evgeny Mandrikov
  *
  * Sonar-IDE is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 3 of the License, or (at your option) any later version.
  *
  * Sonar-IDE is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with Sonar-IDE; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
  */
 
 package org.sonar.ide.shared;
 
 import org.apache.commons.io.FileUtils;
 import org.junit.AfterClass;
 import org.junit.BeforeClass;
 import org.junit.Test;
 import org.sonar.ide.test.AbstractSonarIdeTest;
 import org.sonar.wsclient.Sonar;
 import org.sonar.wsclient.connectors.ConnectionException;
 import org.sonar.wsclient.services.Violation;
 
 import java.io.File;
 import java.util.List;
 
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
 import static org.junit.Assert.assertThat;
 
 /**
  * @author Evgeny Mandrikov
  */
 public class ViolationsLoaderTest extends AbstractSonarIdeTest {
   @BeforeClass
   public static void init() throws Exception {
     AbstractSonarIdeTest.init();
   }
 
   @AfterClass
   public static void cleanup() throws Exception {
     AbstractSonarIdeTest.cleanup();
   }
 
   @Test(expected = ConnectionException.class)
   public void testServerUnavailable() throws Exception {
     ViolationsLoader.getViolations(Sonar.create("http://localhost:9999"), "test:test:[default].ClassOnDefaultPackage", "");
   }
 
   private List<Violation> getViolations(File project, String className) throws Exception {
     return ViolationsLoader.getViolations(
         getTestServer().getSonar(),
         getProjectKey(project) + ":[default]." + className,
         FileUtils.readFileToString(getProjectFile(project, "/src/main/java/" + className + ".java"))
     );
   }
 
   @Test
   public void testGetViolations() throws Exception {
     List<Violation> violations = getViolations(getProject("SimpleProject"), "ClassOnDefaultPackage");
 
     assertThat(violations.size(), is(4));
     // TODO assert lines
   }
 
   /**
    * See <a href="http://jira.codehaus.org/browse/SONARIDE-52">SONARIDE-52</a>
    */
   @Test
   public void testViolationOnFile() throws Exception {
     List<Violation> violations = getViolations(getProject("SimpleProject"), "ViolationOnFile");
 
     assertThat(violations.size(), is(0));
   }
 
   /**
    * See <a href="http://jira.codehaus.org/browse/SONARIDE-13">SONARIDE-13</a>
    */
   @Test
   public void testCodeChanged() throws Exception {
     List<Violation> violations = getViolations(getProject("code-changed"), "CodeChanged");
 
     assertThat(violations.size(), is(1));
     assertThat(violations.get(0).getLine(), is(4));
   }
 
   @Test
   public void testLineForViolationDoesntExists() throws Exception {
     List<Violation> violations = getViolations(getProject("code-changed"), "LineForViolationDoesntExists");
 
     assertThat(violations.size(), is(1));
     assertThat(violations.get(0).getLine(), is(2));
   }
 
   @Test
   public void testMoreThanOneMatch() throws Exception {
     List<Violation> violations = getViolations(getProject("code-changed"), "MoreThanOneMatch");
 
     assertThat(violations.size(), is(2));
     assertThat(violations.get(0).getLine(), is(4));
     assertThat(violations.get(1).getLine(), is(4));
   }
 }
