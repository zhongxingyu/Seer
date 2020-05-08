 /*
  * Sonar, open source software quality management tool.
  * Copyright (C) 2008-2011 SonarSource
  * mailto:contact AT sonarsource DOT com
  *
  * Sonar is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 3 of the License, or (at your option) any later version.
  *
  * Sonar is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with Sonar; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
  */
 package org.sonar.plugins.cpd;
 
 import org.apache.commons.configuration.PropertiesConfiguration;
 import org.junit.Test;
import org.sonar.api.database.DatabaseSession;
 import org.sonar.api.resources.Project;
 
 import static junit.framework.Assert.assertFalse;
 import static junit.framework.Assert.assertTrue;
import static org.mockito.Mockito.mock;
 
 public class CpdSensorTest {
 
   @Test
   public void generalSkip() {
     PropertiesConfiguration conf = new PropertiesConfiguration();
     conf.setProperty("sonar.newcpd.skip", "true");
 
     Project project = createJavaProject().setConfiguration(conf);
    DatabaseSession session = mock(DatabaseSession.class);
 
    CpdSensor sensor = new CpdSensor(session);
     assertTrue(sensor.isSkipped(project));
   }
 
   @Test
   public void doNotSkipByDefault() {
     Project project = createJavaProject().setConfiguration(new PropertiesConfiguration());
 
    DatabaseSession session = mock(DatabaseSession.class);
 
    CpdSensor sensor = new CpdSensor(session);
     assertFalse(sensor.isSkipped(project));
   }
 
   @Test
   public void skipByLanguage() {
     PropertiesConfiguration conf = new PropertiesConfiguration();
     conf.setProperty("sonar.newcpd.skip", "false");
     conf.setProperty("sonar.newcpd.php.skip", "true");
 
     Project phpProject = createPhpProject().setConfiguration(conf);
     Project javaProject = createJavaProject().setConfiguration(conf);
 
    DatabaseSession session = mock(DatabaseSession.class);
 
    CpdSensor sensor = new CpdSensor(session);
     assertTrue(sensor.isSkipped(phpProject));
     assertFalse(sensor.isSkipped(javaProject));
   }
 
   private Project createJavaProject() {
     return new Project("java_project").setLanguageKey("java");
   }
 
   private Project createPhpProject() {
     return new Project("php_project").setLanguageKey("php");
   }
 
 }
