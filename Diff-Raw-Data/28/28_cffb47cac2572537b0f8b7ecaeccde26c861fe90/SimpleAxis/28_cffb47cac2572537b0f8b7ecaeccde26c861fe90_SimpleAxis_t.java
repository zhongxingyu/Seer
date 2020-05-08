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
 
 package org.sonar.plugins.sigmm.axis;
 
 import org.sonar.api.measures.Metric;
 import org.sonar.api.measures.MeasureUtils;
 import org.sonar.api.batch.DecoratorContext;
 import org.sonar.plugins.sigmm.MMRank;
 
 public class SimpleAxis implements MMAxis {
   private int[] bottomLimits;
   private MMRank[] ranks;
   private Metric metric;
   private DecoratorContext context;
 
   public SimpleAxis(int[] bottomLimits, MMRank[] ranks, Metric metric, DecoratorContext context) {
     this.bottomLimits = bottomLimits;
     this.metric = metric;
     this.ranks = ranks;
     this.context = context;
     if (ranks.length != bottomLimits.length) {
       throw new IllegalArgumentException("Should not be here... trying to rank with wrong number of arguments");
     }
   }
 
   public MMRank getRank() {
     double value = MeasureUtils.getValue(context.getMeasure(metric), 0.0);
 
     for (int i = 0; i <= bottomLimits.length; i++) {
       if (value >= bottomLimits[i]) {
         return ranks[i];
       }
     }
     throw new IllegalStateException("Should not be here... Not being able to rank a simple Axis");
   }
 }
