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
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.util.ArrayList;
 import java.util.List;
 
 import com.paxxis.cornerstone.scripting.parser.ParseException;
 import com.paxxis.cornerstone.scripting.parser.RuleParser;
 
 /**
  * 
  * @author Rob Englander
  *
  */
 public class FileSystemScriptLoader implements ScriptLoader {
 
     private String sourceName = null;
     private boolean useSubDirectories = true;
     private ParserManager parserManager = null;
 
     public static void main(String[] args) {
         FileSystemScriptLoader loader = new FileSystemScriptLoader();
         loader.setSourceName(args[0]);
         loader.setUseSubDirectories(true);
         String methodName = args[1];
         ParserManager mgr = new ParserManager();
         mgr.setParserClassName(com.paxxis.cornerstone.scripting.parser.CSLRuleParser.class.getName());
         loader.setParserManager(mgr);
         loader.initialize();
         try {
             RuleSet ruleSet = loader.load();
             Rule rule = ruleSet.getRule(methodName);
 
             if (rule == null) {
                 System.err.println("No such script method: " + methodName);
                 System.exit(1);
             }
 
             List<IValue> params = new ArrayList<IValue>();
             boolean result = rule.process(params);
 
             IValue value = rule.getReturnValue();
             String str = value.valueAsString();
             System.out.println(str);
 
             System.exit(0);
         } catch (Exception e) {
             System.out.println(e.getMessage());
             System.exit(1);
         }
 
     }
 
     public void setSourceName(String name) {
         sourceName = name;
     }
 
     public void setUseSubDirectories(boolean useSubDirectories) {
         this.useSubDirectories = useSubDirectories;
     }
 
     public void setParserManager(ParserManager parserCreator) {
         this.parserManager = parserCreator;
     }
 
     public void initialize() {
         if (sourceName == null) {
             throw new RuntimeException("SourceName property can't be null");
         }
 
         if (parserManager == null) {
             throw new RuntimeException("ParserCreator property can't be null");
         }
     }
 
     private RuleParser loadSource(RuleParser ruleParser, File source, boolean recursive, RuleSet ruleSet) throws Exception {
         try {
             if (source.isDirectory()) {
                 if (recursive) {
                     String[] fileList = source.list();
                     for (String file : fileList) {
                         File f = new File(source.getAbsolutePath() + File.separator + file);
                         ruleParser = loadSource(ruleParser, f, useSubDirectories, ruleSet);
                     }
                 }
             } else {
                 StringBuilder buffer = new StringBuilder();
 
                 FileReader fr = new FileReader(source);
                 BufferedReader rdr = new BufferedReader(fr);
                 String line;
                 while (null != (line = rdr.readLine())) {
                     buffer.append(line).append("\n");
                 }
 
                 rdr.close();
                 ruleParser = parserManager.process(ruleParser, buffer.toString(), ruleSet);
             }
 
         } catch (Exception pe) {
             throw new Exception("Source: " + source.getCanonicalPath() + "\n" + pe.getMessage(), pe);
         }
 
         return ruleParser;
     }
 
     public RuleSet load() throws Exception {
         return load(null);
     }
     
     public RuleSet load(String extraRules) throws Exception {
         try {
             RuleSet ruleSet = new RuleSet(sourceName, "", parserManager.createRuntime());
             File source = new File(sourceName);
             RuleParser parser = loadSource(null, source, true, ruleSet);
             if (extraRules != null) {
                parserManager.process(extraRules, ruleSet);
             }
             
             if (parser.hasParseErrors()) {
                 List<ParseException> list = parser.getParseErrors();
                 StringBuilder builder = new StringBuilder();
                 for (ParseException p : list) {
                     builder.append(p.getMessage()).append("\n");
                 }
                 throw new ParseException(builder.toString());
             }
             
             ruleSet.resoveRuleReferences();
             return ruleSet;
 
         } catch (Exception e) {
             throw new RuntimeException(e.getMessage(), e);
         }
     }
 }
