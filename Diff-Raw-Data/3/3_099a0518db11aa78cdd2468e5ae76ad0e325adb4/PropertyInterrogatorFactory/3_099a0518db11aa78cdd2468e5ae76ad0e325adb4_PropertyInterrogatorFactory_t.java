 /*
  * Copyright (c) 2010-2011, Dmitry Sidorenko. All Rights Reserved.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package com.googlecode.commandme.impl.interrogator;
 
 import com.googlecode.commandme.OptionDefinitionException;
 import com.googlecode.commandme.impl.introspector.OptionDefinition;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.util.HashSet;
 import java.util.Set;
 
 /**
  * @author Dmitry Sidorenko
  */
 public class PropertyInterrogatorFactory {
     @SuppressWarnings({"unused"})
    private static final Logger                      LOGGER               = LoggerFactory.getLogger(PropertyInterrogatorFactory.class);
     private static       PropertyInterrogatorFactory factory              = new PropertyInterrogatorFactory();
     private static final Set<Class>                  allowedOptionClasses = new HashSet<Class>();
 
     static {
         allowedOptionClasses.add(String.class);
         allowedOptionClasses.add(Integer.class);
         allowedOptionClasses.add(Long.class);
         allowedOptionClasses.add(Integer.class);
         allowedOptionClasses.add(Byte.class);
         allowedOptionClasses.add(Short.class);
         allowedOptionClasses.add(Double.class);
         allowedOptionClasses.add(Float.class);
         allowedOptionClasses.add(Boolean.class);
     }
 
     public static PropertyInterrogator createInterrogator(OptionDefinition optionDefinition) {
         return factory.createInterrogatorInternal(optionDefinition);
     }
 
     public PropertyInterrogator createInterrogatorInternal(OptionDefinition optionDefinition) {
         PropertyInterrogator propertyInterrogator;
         // Boolean has to be first
         if (optionDefinition.getType().equals(Boolean.TYPE) || optionDefinition.getType().equals(Boolean.class)) {
             propertyInterrogator = new BooleanPropertyInterrogator(optionDefinition);
         } else if (optionDefinition.getType().isPrimitive()) {
             propertyInterrogator = new DefaultPropertyInterrogator(optionDefinition);
         } else if (allowedOptionClasses.contains(optionDefinition.getType())) {
             propertyInterrogator = new DefaultPropertyInterrogator(optionDefinition);
         } else {
             //Check if we have public constructor with single String argument
             try {
                 optionDefinition.getType().getConstructor(String.class);
                 propertyInterrogator = new DefaultPropertyInterrogator(optionDefinition);
             } catch (NoSuchMethodException e) {
                 LOGGER.warn("Can't find public construction(Sring) for " + optionDefinition.getType(), e);
                 throw new OptionDefinitionException("Can't find public construction(Sring) for " + optionDefinition.getType());
             }
         }
 
         return propertyInterrogator;
     }
 
     public static void setFactory(PropertyInterrogatorFactory factory) {
         PropertyInterrogatorFactory.factory = factory;
     }
 
     public static void resetFactory() {
         factory = new PropertyInterrogatorFactory();
     }
 }
