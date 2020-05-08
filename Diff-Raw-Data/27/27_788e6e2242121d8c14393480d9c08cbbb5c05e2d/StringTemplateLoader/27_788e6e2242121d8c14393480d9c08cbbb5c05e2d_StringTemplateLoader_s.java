 /*
  * Copyright 2010-2013 Ning, Inc.
  *
  *  Ning licenses this file to you under the Apache License, version 2.0
  *  (the "License"); you may not use this file except in compliance with the
  *  License.  You may obtain a copy of the License at:
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  *  Unless required by applicable law or agreed to in writing, software
  *  distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
  *  WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
  *  License for the specific language governing permissions and limitations
  *  under the License.
  */
 
 package com.ning.killbill.zuora.zuora;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.util.regex.Matcher;
 
 import org.antlr.stringtemplate.StringTemplate;
 import org.antlr.stringtemplate.StringTemplateGroup;
 import org.antlr.stringtemplate.language.AngleBracketTemplateLexer;
 
 public class StringTemplateLoader {
     public static class StringTemplateBuilder {
         private final StringTemplate stringTemplate;
 
         public StringTemplateBuilder(StringTemplate stringTemplate) {
             this.stringTemplate = stringTemplate;
         }
 
         public StringTemplateBuilder define(String key, Object value) {
             stringTemplate.setAttribute(key, value);
             return this;
         }
 
         public String build() {
             return stringTemplate.toString();
         }
     }
 
     private static final String sep = "/"; // *Not* System.getProperty("file.separator"), which breaks in jars
     private final StringTemplateGroup group;
 
    public StringTemplateLoader(Class<?> baseClass) {
         String path = baseClass.getName().replaceAll("\\.", Matcher.quoteReplacement(sep)) + ".stg";
 
         try {
             InputStream ins = getClass().getResourceAsStream(path);
 
             if (ins == null) {
                 // try with a slash in front
                 ins = getClass().getResourceAsStream("/" + path);
                 if (ins == null) {
                     throw new IllegalStateException("unable to find group file " + path + " on classpath");
                 }
             }
 
             InputStreamReader reader = new InputStreamReader(ins);
 
            this.group = new StringTemplateGroup(reader, AngleBracketTemplateLexer.class);
             reader.close();
         }
         catch (IOException e) {
             throw new IllegalStateException("unable to load string template group " + path, e);
         }
     }
 
     public StringTemplateBuilder load(String name) {
         if (group.isDefined(name)) {
             return new StringTemplateBuilder(group.lookupTemplate(name).getInstanceOf());
         }
         else {
             throw new IllegalArgumentException("Did not find a template with the name " + name);
         }
     }
 }
