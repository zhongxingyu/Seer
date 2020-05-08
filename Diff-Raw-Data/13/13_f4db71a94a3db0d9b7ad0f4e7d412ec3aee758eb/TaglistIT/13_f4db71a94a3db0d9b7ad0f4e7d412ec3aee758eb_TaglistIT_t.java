 /*
  * Sonar Taglist Plugin
  * Copyright (C) 2009 SonarSource
  * dev@sonar.codehaus.org
  *
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 3 of the License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this program; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
  */
 
 package org.sonar.plugins.taglist;
 
 import org.junit.BeforeClass;
 import org.junit.Test;
 import org.sonar.wsclient.Sonar;
 import org.sonar.wsclient.services.Measure;
import org.sonar.wsclient.services.Resource;
 import org.sonar.wsclient.services.ResourceQuery;
 
 import static org.hamcrest.MatcherAssert.assertThat;
 import static org.hamcrest.Matchers.is;
 import static org.hamcrest.Matchers.notNullValue;
 import static org.hamcrest.Matchers.nullValue;
 
 public class TaglistIT {
 
   private static final String PROJECT = "org.sonar.tests:taglist";
   private static final String PACKAGE = "org.sonar.tests:taglist:org.sonar.tests.taglist";
   private static final String FILE = "org.sonar.tests:taglist:org.sonar.tests.taglist.HelloWorld";
 
   private static Sonar sonar;
 
   @BeforeClass
   public static void buildServer() {
     sonar = Sonar.create("http://localhost:9000");
   }
 
   @Test
   public void projectIsAnalyzed() {
     assertThat(sonar.find(new ResourceQuery(PROJECT)).getName(), is("Sonar tests - taglist"));
     assertThat(sonar.find(new ResourceQuery(PROJECT)).getVersion(), is("0.1-SNAPSHOT"));
     assertThat(sonar.find(new ResourceQuery(PACKAGE)).getName(), is("org.sonar.tests.taglist"));
   }
 
   @Test
   public void projectsMetrics() {
     assertThat(getProjectMeasure("tags").getValue(), is(4.0));
     assertThat(getProjectMeasure("mandatory_tags").getValue(), is(1.0));
     assertThat(getProjectMeasure("optional_tags").getValue(), is(3.0));
     assertThat(getProjectMeasure("nosonar_tags").getValue(), is(1.0));
     assertThat(getProjectMeasure("tags_distribution"), notNullValue());
     assertThat(getProjectMeasure("tags_distribution").getData(), is("@deprecated=1;FIXME=1;NOSONAR=1;TODO=1"));
   }
 
   @Test
   public void packageMetrics() {
     assertThat(getPackageMeasure("tags").getValue(), is(4.0));
     assertThat(getPackageMeasure("mandatory_tags").getValue(), is(1.0));
     assertThat(getPackageMeasure("optional_tags").getValue(), is(3.0));
     assertThat(getPackageMeasure("nosonar_tags").getValue(), is(1.0));
     assertThat(getProjectMeasure("tags_distribution"), notNullValue());
     assertThat(getPackageMeasure("tags_distribution").getData(), is("@deprecated=1;FIXME=1;NOSONAR=1;TODO=1"));
   }
 
   @Test
   public void fileMetrics() {
     assertThat(getFileMeasure("tags").getValue(), is(3.0));
     assertThat(getFileMeasure("mandatory_tags").getValue(), is(1.0));
     assertThat(getFileMeasure("optional_tags").getValue(), is(2.0));
     assertThat(getFileMeasure("nosonar_tags").getValue(), is(1.0));
     assertThat(getFileMeasure("tags_distribution"), nullValue());
   }
 
   private Measure getFileMeasure(String metricKey) {
    Resource resource = sonar.find(ResourceQuery.createForMetrics(FILE, metricKey));
    return resource!=null ? resource.getMeasure(metricKey) : null;
   }
 
   private Measure getPackageMeasure(String metricKey) {
    Resource resource = sonar.find(ResourceQuery.createForMetrics(PACKAGE, metricKey));
    return resource!=null ? resource.getMeasure(metricKey) : null;
   }
 
   private Measure getProjectMeasure(String metricKey) {
    Resource resource = sonar.find(ResourceQuery.createForMetrics(PROJECT, metricKey));
    return resource!=null ? resource.getMeasure(metricKey) : null;
   }
 
 }
