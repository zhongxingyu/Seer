 /**************************************************************************************************
  * This file is part of [SpringAtom] Copyright [kornicameister@gmail.com][2013]                   *
  *                                                                                                *
  * [SpringAtom] is free software: you can redistribute it and/or modify                           *
  * it under the terms of the GNU General Public License as published by                           *
  * the Free Software Foundation, either version 3 of the License, or                              *
  * (at your option) any later version.                                                            *
  *                                                                                                *
  * [SpringAtom] is distributed in the hope that it will be useful,                                *
  * but WITHOUT ANY WARRANTY; without even the implied warranty of                                 *
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the                                  *
  * GNU General Public License for more details.                                                   *
  *                                                                                                *
  * You should have received a copy of the GNU General Public License                              *
  * along with [SpringAtom].  If not, see <http://www.gnu.org/licenses/gpl.html>.                  *
  **************************************************************************************************/
 
 package org.agatom.springatom.core.module;
 
 import org.apache.log4j.Logger;
 import org.springframework.context.annotation.Configuration;
 import org.springframework.core.annotation.AnnotationUtils;
 import org.springframework.util.ClassUtils;
 
 
 /**
  * @author kornicameister
  * @version 0.0.1
  * @since 0.0.1
  */
abstract public class AbstractModuleConfiguration {
 
     private final String moduleName;
 
     protected AbstractModuleConfiguration() {
         this.moduleName = this.getModuleName();
     }
 
     private String getModuleName() {
         final Configuration annotation = AnnotationUtils.findAnnotation(this.getClass(), Configuration.class);
         return (String) AnnotationUtils.getValue(annotation);
     }
 
     protected void logRegistering(final Class<?> clazz, final Logger logger) {
         logger.info(String.format("%s > %s registering...", this.moduleName, ClassUtils.getShortName(clazz)));
     }
 }
