 /*
  * Sonar, open source software quality management tool.
  * Copyright (C) 2009 SonarSource SA
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
 
 package org.sonar.plugins.qi;
 
 import org.junit.Test;
 import org.junit.BeforeClass;

import static org.hamcrest.Matchers.anyOf;
 import static org.junit.Assert.assertThat;
 import static org.hamcrest.core.Is.is;
 import org.sonar.wsclient.Sonar;
 import org.sonar.wsclient.services.ResourceQuery;
 import org.sonar.wsclient.services.Measure;
 import static junit.framework.Assert.assertNull;
 
 /**
  * Integration test, executed when the maven profile -Pit is activated.
  */
 public class StrutsIT {
 
   private static Sonar sonar;
   private static final String PROJECT_STRUTS = "org.apache.struts:struts-parent";
   private static final String MODULE_CORE = "org.apache.struts:struts-core";
   private static final String FILE_ACTION = "org.apache.struts:struts-core:org.apache.struts.action.Action";
   private static final String PACKAGE_ACTION = "org.apache.struts:struts-core:org.apache.struts.action";
 
   @BeforeClass
   public static void buildServer() {
    sonar = Sonar.create("http://192.168.1.59:9000");
   }
 
   @Test
   public void strutsIsAnalyzed() {
     assertThat(sonar.find(new ResourceQuery(PROJECT_STRUTS)).getName(), is("Struts"));
     assertThat(sonar.find(new ResourceQuery(PROJECT_STRUTS)).getVersion(), is("1.3.9"));
     assertThat(sonar.find(new ResourceQuery(MODULE_CORE)).getName(), is("Struts Core"));
     assertThat(sonar.find(new ResourceQuery(PACKAGE_ACTION)).getName(), is("org.apache.struts.action"));
   }
 
   @Test
   public void projectsMetrics() {
    // Cater for big change of duplicated line from Sonar 2.2
    assertThat(getProjectMeasure("qi-quality-index").getValue(), anyOf(is(0.3), is(7.8)));
    assertThat(getProjectMeasure("qi-coding-violations").getValue(), anyOf(is(4.5), is(0.4)));
    assertThat(getProjectMeasure("qi-coding-weighted-violations").getIntValue(), anyOf(is(2042), is(2048)));
    assertThat(getProjectMeasure("qi-style-violations").getValue(), anyOf(is(1.5), is(0.0)));
     assertThat(getProjectMeasure("qi-style-weighted-violations").getIntValue(), is(6801));
     assertThat(getProjectMeasure("qi-test-coverage").getValue(), is(1.7));
    assertThat(getProjectMeasure("qi-complexity").getValue(), anyOf(is(2.0), is(0.1)));
     assertThat(getProjectMeasure("qi-complexity-factor").getValue(), is(15.2));
     assertThat(getProjectMeasure("qi-complexity-factor-methods").getIntValue(), is(37));
     assertThat(getProjectMeasure("qi-complex-distrib").getData(), is("1=3077;2=1013;10=138;20=27;30=37"));
   }
 
   @Test
   public void modulesMetrics() {
     assertThat(getCoreModuleMeasure("qi-quality-index").getValue(), is(8.3));
     assertThat(getCoreModuleMeasure("qi-coding-violations").getValue(), is(0.3));
    assertThat(getCoreModuleMeasure("qi-coding-weighted-violations").getIntValue(), anyOf(is(954), is(957)));
     assertThat(getCoreModuleMeasure("qi-style-violations").getValue(), is(0.0));
     assertThat(getCoreModuleMeasure("qi-style-weighted-violations").getIntValue(), is(1573));
     assertThat(getCoreModuleMeasure("qi-test-coverage").getValue(), is(1.3));
     assertThat(getCoreModuleMeasure("qi-complexity").getValue(), is(0.1));
     assertThat(getCoreModuleMeasure("qi-complexity-factor").getValue(), is(4.3));
     assertThat(getCoreModuleMeasure("qi-complexity-factor-methods").getIntValue(), is(5));
     assertThat(getCoreModuleMeasure("qi-complex-distrib").getData(), is("1=851;2=529;10=43;20=9;30=5"));
   }
 
   @Test
   public void packagesMetrics() {
     assertThat(getPackageMeasure("qi-quality-index").getValue(), is(8.3));
     assertThat(getPackageMeasure("qi-coding-violations").getValue(), is(0.3));
     assertThat(getPackageMeasure("qi-coding-weighted-violations").getIntValue(), is(156));
     assertThat(getPackageMeasure("qi-style-violations").getValue(), is(0.0));
     assertThat(getPackageMeasure("qi-style-weighted-violations").getIntValue(), is(229));
     assertThat(getPackageMeasure("qi-test-coverage").getValue(), is(1.3));
     assertThat(getPackageMeasure("qi-complexity").getValue(), is(0.1));
     assertThat(getPackageMeasure("qi-complexity-factor").getValue(), is(0.0));
     assertThat(getPackageMeasure("qi-complexity-factor-methods").getIntValue(), is(0));
     assertNull(getPackageMeasure("qi-complex-distrib"));
   }
 
   @Test
   public void filesMetrics() {
     assertThat(getFileMeasure("qi-quality-index").getValue(), is(7.9));
     assertNull(getFileMeasure("qi-coding-violations"));
     assertThat(getFileMeasure("qi-coding-weighted-violations").getIntValue(), is(6));
     assertNull(getFileMeasure("qi-style-violations"));
     assertThat(getFileMeasure("qi-style-weighted-violations").getIntValue(), is(24));
     assertThat(getFileMeasure("qi-test-coverage").getValue(), is(2.0));
     assertThat(getFileMeasure("qi-complexity").getValue(), is(0.1));
     assertThat(getFileMeasure("qi-complexity-factor").getValue(), is(0.0));
     assertThat(getFileMeasure("qi-complexity-factor-methods").getIntValue(), is(0));
     assertNull(getFileMeasure("qi-complex-distrib"));
   }
 
   private Measure getFileMeasure(String metricKey) {
     return sonar.find(ResourceQuery.createForMetrics(FILE_ACTION, metricKey)).getMeasure(metricKey);
   }
 
   private Measure getPackageMeasure(String metricKey) {
     return sonar.find(ResourceQuery.createForMetrics(PACKAGE_ACTION, metricKey)).getMeasure(metricKey);
   }
 
   private Measure getCoreModuleMeasure(String metricKey) {
     return sonar.find(ResourceQuery.createForMetrics(MODULE_CORE, metricKey)).getMeasure(metricKey);
   }
 
   private Measure getProjectMeasure(String metricKey) {
     return sonar.find(ResourceQuery.createForMetrics(PROJECT_STRUTS, metricKey)).getMeasure(metricKey);
   }
 }
