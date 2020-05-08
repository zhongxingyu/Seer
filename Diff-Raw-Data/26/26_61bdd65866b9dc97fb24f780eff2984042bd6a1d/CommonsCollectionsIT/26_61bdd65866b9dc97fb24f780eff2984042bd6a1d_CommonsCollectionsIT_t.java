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
 
 package org.sonar.plugins.technicaldebt.it;
 
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

    // 2 values to cope with the fact that CPD has a different behavior when running in java 5 or 6
    assertThat(getProjectMeasure("technical_debt").getValue(), anyOf(is(103775.0), is(104025.0)));
     assertThat(getProjectMeasure("technical_debt_ratio").getValue(), is(13.9));
    assertThat(getProjectMeasure("technical_debt_days").getValue(), anyOf(is(207.6), is(208.1)));
    assertThat(getProjectMeasure("technical_debt_repart").getData(), anyOf(
      is("Comments=15.06;Complexity=22.55;Design=34.93;Duplication=10.35;Violations=17.08"),
      is("Comments=15.03;Complexity=22.5;Design=34.84;Duplication=10.57;Violations=17.04")));
   }
 
   @Test
   public void packagesMetrics() {
    // 2 values to cope with the fact that CPD has a different behavior when running in java 5 or 6
    assertThat(getPackageMeasure("technical_debt").getValue(), anyOf(is(25715.3), is(25840.3)));
    assertThat(getPackageMeasure("technical_debt_ratio").getValue(), anyOf(is(12.9), is(13.0)));
    assertThat(getPackageMeasure("technical_debt_days").getValue(), anyOf(is(51.4), is(51.7)));
    assertThat(getPackageMeasure("technical_debt_repart").getData(), anyOf(
      is("Comments=14.14;Complexity=47.15;Coverage=7.25;Duplication=11.18;Violations=20.27"),
    is("Comments=14.07;Complexity=46.92;Coverage=7.21;Duplication=11.6;Violations=20.17")));
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
