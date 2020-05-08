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
 
 package org.sonar.plugins.sigmm;
 
 import org.junit.Test;
 import static org.junit.Assert.assertEquals;
 import org.sonar.api.batch.SquidSearch;
 import org.sonar.api.batch.SensorContext;
 import org.sonar.api.measures.CoreMetrics;
 import org.sonar.api.measures.RangeDistributionBuilder;
 import org.sonar.api.measures.Metric;
 import org.sonar.api.measures.Measure;
 import org.sonar.api.resources.Resource;
 import org.sonar.api.resources.File;
 import org.sonar.squid.indexer.Query;
 import org.sonar.squid.api.SourceCode;
 import org.sonar.squid.api.SourceFile;
 import org.sonar.squid.api.SourceMethod;
 import static org.mockito.Mockito.*;
 import org.hamcrest.BaseMatcher;
 import org.hamcrest.Description;
 import org.apache.commons.lang.StringUtils;
 
 import java.util.Collection;
 import java.util.ArrayList;
 
 public class TestSensor {
   @Test
   public void testKeyMapping() {
     SquidSearch squid = mock(SquidSearch.class);
     MMSensor sensor = new MMSensor(squid);
     assertEquals(sensor.mapKey(MMMetrics.NCLOC_BY_NCLOC_DISTRIB, 35, 12), 35, 0);
     assertEquals(sensor.mapKey(MMMetrics.NCLOC_BY_CC_DISTRIB, 35, 12), 12, 0);
     assertEquals(sensor.mapKey(CoreMetrics.ACCESSORS, 35, 12), 0, 0);
   }
 
   @Test
   public void testMeasureSaving() {
     SensorContext context = mock(SensorContext.class);
     Resource resource = new File("foo");
     when(context.getResource("foo")).
       thenReturn(resource);
 
     SourceCode file = new SourceFile("foo");
     Number[] bLimits = {30, 20, 10, 0};
     RangeDistributionBuilder distrib = new RangeDistributionBuilder(MMMetrics.NCLOC_BY_NCLOC_DISTRIB, bLimits);
     MMSensor sensor = new MMSensor(null);
     sensor.saveMeasure(context, file, distrib);
 
     verify(context).saveMeasure(eq(resource), argThat(new BaseMatcher<Measure>(){
       public boolean matches(Object o) {
         Measure m = (Measure)o;
 
         return StringUtils.isNotEmpty(m.getData()); //To change body of implemented methods use File | Settings | File Templates.
       }
 
       public void describeTo(Description description) {
       }
     }));
     // Vrifier que Measure a bien une persistence mmoire
   }
 
 
   @Test
   public void testNclocDistributionComputingForAFile() {
     Number[] limits = {40, 20, 10, 0};
     Collection<SourceCode> units = new ArrayList<SourceCode>();
 
    units.add(createMethod(12, 22));
     assertEquals(getDistributionData(units, limits, MMMetrics.NCLOC_BY_NCLOC_DISTRIB), "0=0;10=12;20=0;40=0");
     assertEquals(getDistributionData(units, limits, MMMetrics.NCLOC_BY_CC_DISTRIB), "0=0;10=0;20=12;40=0");
 
    units.add(createMethod(100, 1));
     assertEquals(getDistributionData(units, limits, MMMetrics.NCLOC_BY_NCLOC_DISTRIB), "0=0;10=12;20=0;40=100");
     assertEquals(getDistributionData(units, limits, MMMetrics.NCLOC_BY_CC_DISTRIB), "0=100;10=0;20=12;40=0");
 
    units.add(createMethod(36, 36));
     assertEquals(getDistributionData(units, limits, MMMetrics.NCLOC_BY_NCLOC_DISTRIB), "0=0;10=12;20=36;40=100");
     assertEquals(getDistributionData(units, limits, MMMetrics.NCLOC_BY_CC_DISTRIB), "0=100;10=0;20=48;40=0");
 
   }
 
   private String getDistributionData(Collection<SourceCode> units, Number[] limits, Metric metric) {
     MMSensor sensor = new MMSensor(new SquidSearchImpl(units));
     RangeDistributionBuilder distribution = sensor.computeDistributionForAFile(null, limits, metric);
 
     return distribution.build().getData();
   }
 
  private SourceMethod createMethod(int ncloc, int cc) {
     SourceMethod method = new SourceMethod("foo");
 
     method.setMeasure(org.sonar.squid.measures.Metric.COMPLEXITY, cc);
    method.setMeasure(org.sonar.squid.measures.Metric.LINES_OF_CODE, ncloc);
 
     return method;
   }
 
   class SquidSearchImpl implements SquidSearch {
     private Collection<SourceCode> units;
 
     SquidSearchImpl(Collection<SourceCode> units) {
       this.units = units;
     }
 
     public Collection<SourceCode> search(Query... query) {
       return units;
     }
 
     public SourceCode search(String key) {
       return null;
     }
   }
 
 }
