/*
 * #%L
 * it-xcode-maven-plugin
 * %%
 * Copyright (C) 2012 SAP AG
 * %%
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
 * #L%
 */
 package com.sap.prd.mobile.ios.mios;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 
 import org.apache.maven.it.Verifier;
 
 public class XCodeTestParameters
 {
   public ProjectModifier modifier;
   public Properties pomReplacements;
   public List<String> additionalCommandLineOptions = new ArrayList<String>();
   public List<String> targets = new ArrayList<String>();
   public File projectDirectory;
   public String testName;
   public Verifier _verifier = null;
   public Map<String, String> additionalSystemProperties = new HashMap<String, String>();
 
   public XCodeTestParameters()
   {
   }
 
   public XCodeTestParameters(final String testName, final File projectDirectory, String target,
         List<String> additionalCommandLineOptions, Properties pomReplacements, ProjectModifier modifier)
   {
     this(null, testName, projectDirectory, Arrays.asList(new String[] { target }), additionalCommandLineOptions,
           new HashMap<String, String>(), pomReplacements, modifier);
   }
 
   public XCodeTestParameters(final String testName, final File projectDirectory, List<String> targets,
         List<String> additionalCommandLineOptions, Properties pomReplacements, ProjectModifier modifier)
   {
     this(null, testName, projectDirectory, targets, additionalCommandLineOptions, new HashMap<String, String>(),
           pomReplacements, modifier);
   }
 
   public XCodeTestParameters(final Verifier _verifier, final String testName, final File projectDirectory,
         List<String> targets, List<String> additionalCommandLineOptions,
         Map<String, String> additionalSystemProperties, Properties pomReplacements, ProjectModifier modifier)
   {
     this.testName = testName;
     this.projectDirectory = projectDirectory;
     this.targets = targets;
     this.additionalCommandLineOptions = additionalCommandLineOptions;
     this.pomReplacements = pomReplacements;
     this.modifier = modifier;
     this._verifier = _verifier;
     this.additionalSystemProperties = additionalSystemProperties;
   }
 
   public void addTargets(String...targets)
   {
     this.targets.addAll(Arrays.asList(targets));
   }
 
 }
