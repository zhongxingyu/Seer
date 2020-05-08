 /*
  * Copyright 2010 the original author or authors.
  * Copyright 2009 Paxxis Technology LLC
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package com.paxxis.cornerstone.scripting;
 
 import com.paxxis.cornerstone.scripting.parser.CSLRuntime;
 import com.paxxis.cornerstone.scripting.parser.RuleParser;
 
 /**
  * 
  * @author Rob Englander
  *
  */
 public class ParserManager {
 
     private ServiceContext serviceContext = null;
     private String parserClassName = null;
 
     public <P extends RuleParser> P process(String code, RuleSet ruleSet) throws Exception {
         return process((P)null, code, ruleSet);
     }
 
     @SuppressWarnings("unchecked")
     public <P extends RuleParser> P process(P parser, String code, RuleSet ruleSet) throws Exception {
         if (parser == null) {
             Class<?> parserClass = Class.forName(parserClassName);
             if (!RuleParser.class.isAssignableFrom(parserClass)) {
                 throw new Exception(parserClassName + " is not an instance of RuleParser");
             }
 
             parser = (P)parserClass.newInstance();
         }
 
         parser.initialize(code);
        parser.parseRuleSet(ruleSet);

         return parser;
     }
 
     public CSLRuntime createRuntime() {
         CSLRuntime rt = new CSLRuntime();
         rt.setServiceContext(serviceContext);
         return rt;
     }
 
     public void initialize() {
         if (serviceContext == null) {
             throw new RuntimeException("serviceContext property can't be null.");
         }
 
         if (parserClassName == null) {
             throw new RuntimeException("parserClassName property can't be null.");
         }
     }
 
     public void setParserClassName(String name) {
         this.parserClassName = name;
     }
 
     public void setServiceContext(ServiceContext context) {
         this.serviceContext = context;
     }
 
 }
