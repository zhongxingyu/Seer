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
 
 package com.googlecode.commandme.impl.introspector;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.util.*;
 
 /**
  * Definition of options.
  * Created from annotated module class.
  *
  * @author Dmitry Sidorenko
  */
 public class ParametersIntrospector implements ModuleParameters {
     @SuppressWarnings({"UnusedDeclaration"})
     private static final Logger LOGGER = LoggerFactory.getLogger(ParametersIntrospector.class);
 
    private List<ParameterDefinition>        parameterDefinitions;
    private Map<String, ParameterDefinition> shortNamesMap;
    private Map<String, ParameterDefinition> longNamesMap;
 
 
     public ParametersIntrospector() {
         parameterDefinitions = new LinkedList<ParameterDefinition>();
         shortNamesMap = new HashMap<String, ParameterDefinition>();
         longNamesMap = new HashMap<String, ParameterDefinition>();
     }
 
     /**
      * Adds new parameter definition.
      *
      * @param parameterDefinition parameter
      */
     public void addParameter(ParameterDefinition parameterDefinition) {
         if (parameterDefinition == null) {
             return;
         }
         parameterDefinitions.add(parameterDefinition);
         shortNamesMap.put(parameterDefinition.getShortName(), parameterDefinition);
         longNamesMap.put(parameterDefinition.getLongName(), parameterDefinition);
     }
 
     @Override
     public List<ParameterDefinition> getParameterDefinitions() {
         return Collections.unmodifiableList(parameterDefinitions);
     }
 
     @Override
     public ParameterDefinition getByLongName(String name) {
         return longNamesMap.get(name);
     }
 
     @Override
     public ParameterDefinition getByShortName(String name) {
         return shortNamesMap.get(name);
     }
 }
