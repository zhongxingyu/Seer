 /*
  * Sonar, open source software quality management tool.
  * Copyright (C) 2009 SonarSource
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
 
 package org.sonar.plugins.technicaldebt.it;
 
 import org.junit.BeforeClass;
 import org.junit.Test;
 import org.sonar.wsclient.Sonar;
 import org.sonar.wsclient.services.Measure;
 import org.sonar.wsclient.services.ResourceQuery;
 
 import static junit.framework.Assert.assertNull;
 import static org.hamcrest.Matchers.anyOf;
 import static org.hamcrest.core.Is.is;
 import static org.junit.Assert.assertThat;
 
 /**
  * Integration test, executed when the maven profile -Pit is activated.
  */
 public class CommonsCollectionsIT {
 
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
     assertThat(getProjectMeasure("technical_debt_repart").getData(),
         is("Comments=15.04;Complexity=22.37;Design=34.88;Duplication=10.58;Violations=17.1"));
 
     assertThat(getProjectMeasure("technical_debt").getValue(), is(103906.3));
     assertThat(getProjectMeasure("technical_debt_ratio").getValue(), is(13.6));
     assertThat(getProjectMeasure("technical_debt_days").getValue(), is(207.8));
   }
 
   @Test
   public void packagesMetrics() {
     System.out.println("Coverage is : " + getPackageMeasure("coverage").getValue() + " on package " + PROJECT_COMMONS_COLLECTIONS);
     assertThat(getPackageMeasure("technical_debt_repart").getData(),
         is("Comments=14.11;Complexity=46.68;Coverage=7.23;Duplication=11.64;Violations=20.32")
     );
    assertThat(getPackageMeasure("technical_debt").getValue(), is(25771.6));
     assertThat(getPackageMeasure("technical_debt_ratio").getValue(), is(13.1));
     assertThat(getPackageMeasure("technical_debt_days").getValue(), is(51.6));
   }
 
   @Test
   public void filesMetrics() {
     assertThat(getFileMeasure("technical_debt").getValue(), is(6.3));
     assertThat(getFileMeasure("technical_debt_ratio").getValue(), is(0.5));
     assertNull(getFileMeasure("technical_debt_days"));
     assertThat(getFileMeasure("technical_debt_repart").getData(), is("Violations=100.0"));
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
