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
 public class CommonsCollectionIT {
 
   private static Sonar sonar;
   private static final String PROJECT_COMMONS_COLLECTIONS = "commons-collections:commons-collections";
   private static final String FILE_BAG_UTILS = "commons-collections:commons-collections:org.apache.commons.collections.BagUtils";
   private static final String PACKAGE_COLLECTIONS = "commons-collections:commons-collections:org.apache.commons.collections";
 
   @BeforeClass
   public static void buildServer() {
     sonar = Sonar.create("http://localhost:9000");
   }
 
   @Test
   public void commonsCollectionsIsAnalyzed() {
     assertThat(sonar.find(new ResourceQuery(PROJECT_COMMONS_COLLECTIONS)).getName(), is("Commons Collections"));
     assertThat(sonar.find(new ResourceQuery(PROJECT_COMMONS_COLLECTIONS)).getVersion(), is("3.3"));
     assertThat(sonar.find(new ResourceQuery(PACKAGE_COLLECTIONS)).getName(), is("org.apache.commons.collections"));
   }
 
   @Test
   public void projectsMetrics() {
     assertThat(getProjectMeasure("qi-quality-index").getValue(), is(9.1));
     assertThat(getProjectMeasure("qi-coding-violations").getValue(), is(0.3));
 
     // DUe to new rule added
     assertThat(getProjectMeasure("qi-coding-weighted-violations").getIntValue(), anyOf(is(1295), is(1277)));
     assertThat(getProjectMeasure("qi-style-violations").getValue(), is(0.0));
     assertThat(getProjectMeasure("qi-style-weighted-violations").getIntValue(), is(2431));
     assertThat(getProjectMeasure("qi-test-coverage").getValue(), is(0.4));
     assertThat(getProjectMeasure("qi-complexity").getValue(), is(0.2));
     assertThat(getProjectMeasure("qi-complexity-factor").getValue(), is(0.6));
     assertThat(getProjectMeasure("qi-complexity-factor-methods").getIntValue(), is(2));
     assertThat(getProjectMeasure("qi-complex-distrib").getData(), is("1=2270;2=1464;10=82;20=10;30=2"));
   }
 
   @Test
   public void packagesMetrics() {
     assertThat(getPackageMeasure("qi-quality-index").getValue(), is(8.9));
     assertThat(getPackageMeasure("qi-coding-violations").getValue(), is(0.4));
    assertThat(getPackageMeasure("qi-coding-weighted-violations").getIntValue(), anyOf(is(627), is(615));
     assertThat(getPackageMeasure("qi-style-violations").getValue(), is(0.0));
     assertThat(getPackageMeasure("qi-style-weighted-violations").getIntValue(), is(631));
     assertThat(getPackageMeasure("qi-test-coverage").getValue(), is(0.5));
     assertThat(getPackageMeasure("qi-complexity").getValue(), is(0.2));
     assertThat(getPackageMeasure("qi-complexity-factor").getValue(), is(0.0));
     assertThat(getPackageMeasure("qi-complexity-factor-methods").getIntValue(), is(0));
     assertNull(getPackageMeasure("qi-complex-distrib"));
   }
 
   @Test
   public void filesMetrics() {
     assertThat(getFileMeasure("qi-quality-index").getValue(), is(9.7));
     assertNull(getFileMeasure("qi-coding-violations"));
     assertThat(getFileMeasure("qi-coding-weighted-violations").getIntValue(), is(0));
     assertNull(getFileMeasure("qi-style-violations"));
     assertThat(getFileMeasure("qi-style-weighted-violations").getIntValue(), is(1));
     assertThat(getFileMeasure("qi-test-coverage").getValue(), is(0.3));
     assertNull(getFileMeasure("qi-complexity"));
     assertThat(getFileMeasure("qi-complexity-factor").getValue(), is(0.0));
     assertThat(getFileMeasure("qi-complexity-factor-methods").getIntValue(), is(0));
     assertNull(getFileMeasure("qi-complex-distrib"));
   }
 
   private Measure getFileMeasure(String metricKey) {
     return sonar.find(ResourceQuery.createForMetrics(FILE_BAG_UTILS, metricKey)).getMeasure(metricKey);
   }
 
   private Measure getPackageMeasure(String metricKey) {
     return sonar.find(ResourceQuery.createForMetrics(PACKAGE_COLLECTIONS, metricKey)).getMeasure(metricKey);
   }
 
   private Measure getProjectMeasure(String metricKey) {
     return sonar.find(ResourceQuery.createForMetrics(PROJECT_COMMONS_COLLECTIONS, metricKey)).getMeasure(metricKey);
   }
 }
