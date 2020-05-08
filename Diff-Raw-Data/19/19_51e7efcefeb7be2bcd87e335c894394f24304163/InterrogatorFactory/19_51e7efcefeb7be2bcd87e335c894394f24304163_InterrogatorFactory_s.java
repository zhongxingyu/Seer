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
 
 import com.googlecode.commandme.impl.introspector.ModuleIntrospector;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * @author Dmitry Sidorenko
  */
 public class InterrogatorFactory {
    @SuppressWarnings({"UnusedDeclaration"})
     private static final Logger LOGGER = LoggerFactory.getLogger(InterrogatorFactory.class);
 
     private static InterrogatorFactory factory;
 
     static {
         factory = new InterrogatorFactory();
     }
 
    public static <T> Interrogator createInterrogator(T instance, ModuleIntrospector moduleIntrospector, String[] parameters) {
         return factory.create(instance, moduleIntrospector, parameters);
     }
 
     public static void setFactory(InterrogatorFactory factory) {
         InterrogatorFactory.factory = factory;
     }
 
    protected <T> Interrogator create(T instance, ModuleIntrospector moduleIntrospector, String[] parameters) {
         return new Interrogator<T>(instance, moduleIntrospector, parameters);
     }
 
 }
