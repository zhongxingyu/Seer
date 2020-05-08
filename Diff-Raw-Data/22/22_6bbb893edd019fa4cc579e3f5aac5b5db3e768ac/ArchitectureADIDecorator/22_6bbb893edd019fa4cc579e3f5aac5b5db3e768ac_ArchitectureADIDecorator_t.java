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
 import java.util.Collection;
 import java.util.List;
 
 import org.sonar.api.batch.DecoratorContext;
 import org.sonar.api.batch.DependedUpon;
 import org.sonar.api.batch.DependsUpon;
 import org.sonar.api.measures.CoreMetrics;
 import org.sonar.api.measures.Measure;
 import org.sonar.api.measures.MeasureUtils;
 import org.sonar.api.measures.Metric;
 import org.sonar.api.resources.Resource;
 import org.sonar.api.utils.ParsingUtils;
 
 public class ArchitectureADIDecorator extends AbstractBaseDecorator {
 
   @DependedUpon
   public List<Metric> generatesMetrics() {
     return Arrays.asList(TQMetrics.TQ_ARCHITECTURE_ADI);
   }
 
   @DependsUpon
   public List<Metric> dependsOnMetrics() {
     return Arrays.asList(CoreMetrics.DISTANCE, CoreMetrics.NCLOC);
   }
 
   @Override
   public void decorate(Resource resource, DecoratorContext context) {
     // TODO no metric (DISTANCE) available
     // super.decorate(resource, context);
   }
 
   @Override
   void decorateDir(Resource resource, DecoratorContext context) {
     final int aceleration = context.getProject().getConfiguration().getInt(TQPlugin.TQ_ACE, Integer.parseInt(TQPlugin.TQ_ACE_DEFAULT));
     final double cota = context.getProject().getConfiguration().getDouble(TQPlugin.TQ_ARCHITECTURE_ADI,
         Double.parseDouble(TQPlugin.TQ_ARCHITECTURE_ADI_DEFAULT));
     final double top = aceleration <= 1 ? cota : aceleration * cota;
 
     final Measure measure = context.getMeasure(CoreMetrics.DISTANCE);
 
     final double distance = measure == null || measure.getValue() == null ? 0 : measure.getValue();
 
     final double res;// = distance > cota ? 0 : 1;
 
     if (distance <= cota) {
       res = 1;
     } else if (distance > top) {
       res = 0;
     } else {
       res = 1 - ((distance - cota) / (top - cota));
     }
 
     context.saveMeasure(TQMetrics.TQ_ARCHITECTURE_ADI, ParsingUtils.scaleValue(res * 100, 2));
   }
 
   @Override
   void decorateFile(Resource resource, DecoratorContext context) {
     /*
      * final int aceleration = context.getProject().getConfiguration().getInt(TQPlugin.TQ_ACE, Integer.parseInt(TQPlugin.TQ_ACE_DEFAULT));
      * final double cota = context.getProject().getConfiguration().getDouble(TQPlugin.TQ_ARCHITECTURE_ADI,
      * Double.parseDouble(TQPlugin.TQ_ARCHITECTURE_ADI_DEFAULT)); final double top = aceleration <= 1 ? cota : aceleration * cota;
      * 
      * final Measure measure = context.getMeasure(CoreMetrics.DISTANCE);
      * 
      * final double distance = measure == null || measure.getValue() == null ? 0 : measure.getValue();
      * 
      * final double res;// = distance > cota ? 0 : 1;
      * 
      * if (distance <= cota) { res = 1; } else if (distance > top) { res = 0; } else { res = 1 - ((distance - cota) / (top - cota)); }
      * 
      * 
      * context.saveMeasure(TQMetrics.TQ_ARCHITECTURE_ADI, ParsingUtils.scaleValue(res * 100, 2));
      */
     context.saveMeasure(TQMetrics.TQ_ARCHITECTURE_ADI, ParsingUtils.scaleValue(100, 2));
   }
 
   @Override
   void decorateProj(Resource resource, DecoratorContext context) {
     final Collection<Measure> measures = context.getChildrenMeasures(TQMetrics.TQ_ARCHITECTURE_ADI);
    if (measures == null || measures.isEmpty()) {
       return;
     }
 
     double total = 0;
     double size = 0.0;
     for (Measure m : measures) {
       if (MeasureUtils.hasValue(m)) {
         total = total + m.getValue();
         size = size + 1;
       }
     }
 
     final double value = size > 0.0 ? (total / size / 100) : 0.0;
     context.saveMeasure(TQMetrics.TQ_ARCHITECTURE_ADI, ParsingUtils.scaleValue(value * 100, 2));
   }
 
 }
