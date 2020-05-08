 /*
  * Sonar Total Quality Plugin
  * Copyright (C) 2010 Martin (e72636) and Emilio Escobar Reyero (escoem)
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
 
 package org.sonar.plugins.totalquality;
 
 import java.util.Arrays;
 import java.util.List;
 import java.util.Map;
 
 import org.sonar.api.measures.CoreMetrics;
 import org.sonar.api.measures.Metric;
 import org.sonar.api.measures.Metrics;
 
 import com.google.common.collect.ImmutableMap;
 
 /** Metrics for TQ plugin. */
 public final class TQMetrics implements Metrics {
 
   /** Domain Architecture. */
  public static String DOMAIN_ARCHITECTURE = "Architecture";
 
   public static final String TQ_TOTAL_QUALITY_KEY = "total-quality";
   public static final Metric TQ_TOTAL_QUALITY = new Metric(TQ_TOTAL_QUALITY_KEY, "Total Quality", "The total quality of a project",
       Metric.ValueType.PERCENT, Metric.DIRECTION_BETTER, true, CoreMetrics.DOMAIN_GENERAL);
 
   public static final String TQ_DRY_KEY = "total-quality-dry";
   public static final Metric TQ_DRY = new Metric(TQ_DRY_KEY, "DRYness", "DRY = 100 - " + CoreMetrics.DUPLICATED_LINES_DENSITY_KEY,
       Metric.ValueType.PERCENT, Metric.DIRECTION_BETTER, true, CoreMetrics.DOMAIN_DUPLICATION);
 
   public static final String TQ_CODE_KEY = "total-quality-code";
   public static final Metric TQ_CODE = new Metric(TQ_CODE_KEY, "Code Quality", "CODE quality from "
       + CoreMetrics.PUBLIC_DOCUMENTED_API_DENSITY_KEY + ", " + CoreMetrics.VIOLATIONS_DENSITY_KEY + " and " + TQ_DRY_KEY,
       Metric.ValueType.PERCENT, Metric.DIRECTION_BETTER, true, CoreMetrics.DOMAIN_RULES);
 
   public static final String TQ_TS_KEY = "total-quality-test";
   public static final Metric TQ_TS = new Metric(TQ_TS_KEY, "Testing Quality", "TS quality from " + CoreMetrics.BRANCH_COVERAGE_KEY + ", "
       + CoreMetrics.LINE_COVERAGE_KEY + " and " + CoreMetrics.COVERAGE_KEY, Metric.ValueType.PERCENT, Metric.DIRECTION_BETTER, true,
       CoreMetrics.DOMAIN_TESTS);
 
   public static final String TQ_DESIGN_NOM_KEY = "total-quality-design-nom";
   public static final Metric TQ_DESIGN_NOM = new Metric(TQ_DESIGN_NOM_KEY, "Design Classes and Methods Complexity",
       "NOM from classes with " + CoreMetrics.FUNCTION_COMPLEXITY_KEY + " gt value (2.5 default) and " + CoreMetrics.CLASS_COMPLEXITY_KEY
           + " gt value (12 default)", Metric.ValueType.PERCENT, Metric.DIRECTION_BETTER, true, CoreMetrics.DOMAIN_DESIGN);
 
   public static final String TQ_DESIGN_LCOM4_KEY = "total-quality-design-lcom4";
   public static final Metric TQ_DESIGN_LCOM4 = new Metric(TQ_DESIGN_LCOM4_KEY, "Design Lack of Cohesion of Methods",
       "LCOM4 from classes with " + CoreMetrics.LCOM4_KEY + " gt vale (50 default)", Metric.ValueType.PERCENT, Metric.DIRECTION_BETTER,
       true, CoreMetrics.DOMAIN_DESIGN);
 
   public static final String TQ_DESIGN_RFC_KEY = "total-quality-design-rfc";
   public static final Metric TQ_DESIGN_RFC = new Metric(TQ_DESIGN_RFC_KEY, "Design Response for Class", "RFC from classes with "
       + CoreMetrics.RFC_KEY + " gt vale (50 default)", Metric.ValueType.PERCENT, Metric.DIRECTION_BETTER, true, CoreMetrics.DOMAIN_DESIGN);
 
   public static final String TQ_DESIGN_CBO_KEY = "total-quality-design-cbo";
   public static final Metric TQ_DESIGN_CBO = new Metric(TQ_DESIGN_CBO_KEY, "Design Coupling Between Objects", "CBO from classes with "
       + CoreMetrics.EFFERENT_COUPLINGS_KEY + " gt value (5 default)", Metric.ValueType.PERCENT, Metric.DIRECTION_BETTER, true,
       CoreMetrics.DOMAIN_DESIGN);
 
   public static final String TQ_DESIGN_DIT_KEY = "total-quality-design-dit";
   public static final Metric TQ_DESIGN_DIT = new Metric(TQ_DESIGN_DIT_KEY, "Design Depth of Inheritance Tree", "DIT from classes with "
       + CoreMetrics.DEPTH_IN_TREE_KEY + " gt value (5 default)", Metric.ValueType.PERCENT, Metric.DIRECTION_BETTER, true,
       CoreMetrics.DOMAIN_DESIGN);
 
   public static final String TQ_DESIGN_KEY = "total-quality-design";
   public static final Metric TQ_DESIGN = new Metric(TQ_DESIGN_KEY, "Design Quality", "DES from " + TQ_DESIGN_DIT_KEY + ", "
       + TQ_DESIGN_CBO_KEY + ", " + TQ_DESIGN_LCOM4_KEY + ", " + TQ_DESIGN_RFC_KEY + " and " + TQ_DESIGN_NOM_KEY, Metric.ValueType.PERCENT,
       Metric.DIRECTION_BETTER, true, CoreMetrics.DOMAIN_DESIGN);
 
   public static final String TQ_ARCHITECTURE_PTI_KEY = "total-quality-architecture-pti";
   public static final Metric TQ_ARCHITECTURE_PTI = new Metric(TQ_ARCHITECTURE_PTI_KEY, "Architecture Tangle Index",
       "Inverse of Packages and files tangle index", Metric.ValueType.PERCENT, Metric.DIRECTION_BETTER, true, DOMAIN_ARCHITECTURE);
 
   public static final String TQ_ARCHITECTURE_ADI_KEY = "total-quality-architecture-adi";
   public static final Metric TQ_ARCHITECTURE_ADI = new Metric("isoqa_architecture_adi", "Architecture Distance", "ADI from "
       + CoreMetrics.DISTANCE_KEY + " gt value (20 default)", Metric.ValueType.PERCENT, Metric.DIRECTION_BETTER, true, DOMAIN_ARCHITECTURE);
 
   public static final String TQ_ARCHITECTURE_KEY = "total-quality-architecture";
   public static final Metric TQ_ARCHITECTURE = new Metric(TQ_ARCHITECTURE_KEY, "Architecture", "ARCH from " + TQ_ARCHITECTURE_ADI_KEY
       + " and " + CoreMetrics.PACKAGE_TANGLE_INDEX_KEY, Metric.ValueType.PERCENT, Metric.DIRECTION_BETTER, true, DOMAIN_ARCHITECTURE);
 
   public List<Metric> getMetrics() {
     return Arrays.asList(TQ_TOTAL_QUALITY, TQ_DRY, TQ_CODE, TQ_TS, TQ_DESIGN_NOM, TQ_DESIGN_RFC, TQ_DESIGN_CBO, TQ_DESIGN_DIT,
         TQ_DESIGN_LCOM4, TQ_DESIGN, TQ_ARCHITECTURE_ADI, TQ_ARCHITECTURE_PTI, TQ_ARCHITECTURE);
   }
 
   public static final Map<String, Metric> formulaParams = ImmutableMap.<String, Metric> builder().put("ARCH", TQ_ARCHITECTURE).put(
       "DESIGN", TQ_DESIGN).put("CODE", TQ_CODE).put("TESTS", TQ_TS).put("NOM", TQ_DESIGN_NOM).put("LCOM", TQ_DESIGN_LCOM4).put("RFC",
       TQ_DESIGN_RFC).put("CBO", TQ_DESIGN_CBO).put("DIT", TQ_DESIGN_DIT).put("PTI", TQ_ARCHITECTURE_PTI).put("ADI", TQ_ARCHITECTURE_ADI)
       .put("COV", CoreMetrics.COVERAGE).put("SUC", CoreMetrics.TEST_SUCCESS_DENSITY).put("DOC", CoreMetrics.PUBLIC_DOCUMENTED_API_DENSITY)
       .put("RULES", CoreMetrics.VIOLATIONS_DENSITY).put("DRY", TQ_DRY).build();
 
 }
