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
 
 import org.sonar.api.batch.DecoratorContext;
 import org.sonar.api.resources.Resource;
 
 public abstract class AbstractBaseDecorator extends AbstractDecorator {
 
   abstract void decorateFile(Resource resource, DecoratorContext context);
 
   abstract void decorateDir(Resource resource, DecoratorContext context);
 
   abstract void decorateProj(Resource resource, DecoratorContext context);
 
   public void decorate(Resource resource, DecoratorContext context) {
     if (shouldDecorateFile(resource, context)) {
       decorateFile(resource, context);
     } else if (shouldDecorateDir(resource, context)) {
       decorateDir(resource, context);
     } else if (shouldDecorateProj(resource, context)) {
       decorateProj(resource, context);
     }
   }
 
   public boolean shouldDecorateFile(final Resource resource, final DecoratorContext context) {
     return shouldSaveMeasure(resource) && isFile(resource);
   }
 
   public boolean shouldDecorateDir(final Resource resource, final DecoratorContext context) {
     return shouldSaveMeasure(resource) && hasCode(context) && isDir(resource);
   }
 
   public boolean shouldDecorateProj(final Resource resource, final DecoratorContext context) {
     return shouldSaveMeasure(resource) && hasCode(context) && isProj(resource);
   }
 
 }
