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
 
 import java.util.*;
 
import org.sonar.api.batch.DependsUpon;
import org.sonar.api.batch.Sensor;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.batch.SquidSearch;
 import org.sonar.api.resources.Project;
 import org.sonar.api.resources.Resource;
 import org.sonar.api.measures.RangeDistributionBuilder;
 import org.sonar.api.measures.Metric;
 import org.sonar.api.measures.PersistenceMode;
 import org.sonar.squid.api.SourceMethod;
 import org.sonar.squid.api.SourceCode;
 import org.sonar.squid.api.SourceFile;
 import org.sonar.squid.indexer.QueryByType;
 import org.sonar.squid.indexer.QueryByParent;
 import org.apache.commons.lang.StringUtils;
 
 public class MMSensor implements Sensor {
   private SquidSearch squid;
 
   public MMSensor(SquidSearch squid) {
     this.squid = squid;
   }
 
   @DependsUpon
   public List<String> dependsUpon() {
     return Arrays.asList(Sensor.FLAG_SQUID_ANALYSIS);
   }
 
   public boolean shouldExecuteOnProject(Project project) {
     return MMPlugin.shouldExecuteOnProject(project);
   }
 
   public void analyse(Project project, SensorContext context) {
     computeAndSaveDistributionForProjectFiles(context, MMConfiguration.NCLOC_DISTRIBUTION_BOTTOM_LIMITS, MMMetrics.NCLOC_BY_NCLOC_DISTRIB);
     computeAndSaveDistributionForProjectFiles(context, MMConfiguration.CC_DISTRIBUTION_BOTTOM_LIMITS, MMMetrics.NCLOC_BY_CC_DISTRIB);
   }
 
   protected void computeAndSaveDistributionForProjectFiles(SensorContext context, Number[] bottomLimits, Metric metric) {
     Collection<SourceCode> files = squid.search(new QueryByType(SourceFile.class));
     for (SourceCode file : files) {
       RangeDistributionBuilder distribution = computeDistributionForAFile(file, bottomLimits, metric);
       saveMeasure(context, file, distribution);
     }
   }
 
   protected RangeDistributionBuilder computeDistributionForAFile(SourceCode file, Number[] bottomLimits, Metric metric) {
     Collection<SourceCode> methods = squid.search(new QueryByParent(file), new QueryByType(SourceMethod.class));
 
     RangeDistributionBuilder distribution = new RangeDistributionBuilder(metric, bottomLimits);
     for (SourceCode method : methods) {
       int ncloc = method.getInt(org.sonar.squid.measures.Metric.LINES_OF_CODE);
       int cc = method.getInt(org.sonar.squid.measures.Metric.COMPLEXITY);
 
       distribution.add(mapKey(metric, ncloc, cc), ncloc);
     }
     return distribution;
   }
 
   protected void saveMeasure(SensorContext context, SourceCode file, RangeDistributionBuilder nclocDistribution) {
    String key = MMPlugin.convertKeyFromSquidToSonarFormat(file.getKey());
     Resource resource = context.getResource(key);
     context.saveMeasure(resource, nclocDistribution.build().setPersistenceMode(PersistenceMode.MEMORY));
   }
 
   protected int mapKey(Metric metric, int ncloc, int cc) {
     if (metric.equals(MMMetrics.NCLOC_BY_CC_DISTRIB)) {
       return cc;
     }
     else if (metric.equals(MMMetrics.NCLOC_BY_NCLOC_DISTRIB)) {
       return ncloc;
     }
     else {
       return 0;
     }
   }
 }
