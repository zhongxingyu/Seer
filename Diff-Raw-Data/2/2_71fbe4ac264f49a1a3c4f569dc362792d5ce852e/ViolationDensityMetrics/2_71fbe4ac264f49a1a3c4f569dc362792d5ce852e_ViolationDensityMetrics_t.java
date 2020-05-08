 /*
  * Sonar Violation Density Plugin
  * Copyright (C) 2011 MACIF
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
 package org.sonar.plugins.violationdensity;
 
 import java.util.Arrays;
 import java.util.List;
 
 import org.sonar.api.measures.CoreMetrics;
 import org.sonar.api.measures.Metric;
 import org.sonar.api.measures.Metrics;
 
 public class ViolationDensityMetrics implements Metrics {
 
   public static final String VIOLATION_DENSITY_KEY = "violation_index";
   public static final Metric VIOLATION_DENSITY = new Metric.Builder(VIOLATION_DENSITY_KEY, "Violation Density", Metric.ValueType.PERCENT)
       .setDescription("Violation Density").setDirection(Metric.DIRECTION_BETTER).setQualitative(false)
      .setDomain(CoreMetrics.DOMAIN_RULES).create();
 
   public List<Metric> getMetrics() {
     return Arrays.asList(VIOLATION_DENSITY);
   }
 }
