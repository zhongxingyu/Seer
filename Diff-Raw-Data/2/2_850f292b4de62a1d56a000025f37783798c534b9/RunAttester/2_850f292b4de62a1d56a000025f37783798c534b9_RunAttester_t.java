 /*
  * Copyright 2012 Amadeus s.a.s.
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *    http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package com.ariatemplates.attester.maven;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.util.LinkedList;
 import java.util.List;
 
 import org.apache.maven.model.Dependency;
 import org.apache.maven.plugin.MojoExecutionException;
 import org.apache.maven.plugin.MojoFailureException;
 
 public abstract class RunAttester extends RunNode {
 
     /**
      * Configuration file to use. More information about the format to use is
      * documented <a href="https://github.com/ariatemplates/attester#usage"
      * >here</a>.
      *
      * Note that most options can be configured directly from the pom.xml,
      * without the need for an external configuration file.
      *
      * @parameter
      */
     public File configFile;
 
     /**
      * First directory to serve as the root of the web server. This directory is
      * usually the one containing Javascript tests. (Passed through
      * <code>--config.resources./</code> to <a
      * href="https://github.com/ariatemplates/attester#usage" >attester</a>)
      *
      * @parameter expression="${basedir}/src/test/webapp"
      */
     public File testSourceDirectory;
 
     /**
      * Second directory to serve as the root of the web server. In case a file
      * is requested and not present in <a
      * href="#testSourceDirectory">testSourceDirectory</a>, it is looked for in
      * this directory. This directory is usually the one containing the main
      * Javascript files of the application. If it exists, it is also used as the
      * default value for the <a
      * href="#coverageRootDirectory">coverageRootDirectory</a> option. (Passed
      * through <code>--config.resources./</code> to <a
      * href="https://github.com/ariatemplates/attester#usage" >attester</a>)
      *
      * @parameter expression="${basedir}/src/main/webapp"
      */
     public File warSourceDirectory;
 
     /**
      * Third directory to serve as the root of the web server. In case a file is
      * requested and not present in either <a
      * href="#warSourceDirectory">warSourceDirectory</a> or <a
      * href="#testSourceDirectory">testSourceDirectory</a>, it is looked for in
      * this directory. This directory usually contains any external library
      * needed by the application. (Passed through
      * <code>--config.resources./</code> to <a
      * href="https://github.com/ariatemplates/attester#usage" >attester</a>)
      *
      * @parameter
      *            expression="${project.build.directory}/${project.build.finalName}"
      */
     public File webappDirectory;
 
     /**
      * Root directory for code coverage instrumentation. Files inside this
      * directory and matching one of the <a
      * href="#coverageIncludes">coverageIncludes</a> patterns and not matching
      * any <a href="#coverageExcludes">coverageExcludes</a> patterns will be
      * instrumented for code coverage when requested to the web server. Note
      * that for code coverage instrumentation to be effective, this directory or
      * one of its parents has to be configured to be served by the web server
      * (e.g. through <a href="#testSourceDirectory">testSourceDirectory</a>, <a
      * href="#warSourceDirectory">warSourceDirectory</a> or <a
      * href="#webappDirectory">webappDirectory</a>). The default value for this
      * parameter is the value of the <a
      * href="#warSourceDirectory">warSourceDirectory</a> parameter. (Passed
      * through <code>--config.coverage.files.rootDirectory</code> to <a
      * href="https://github.com/ariatemplates/attester#usage" >attester</a>)
      *
      * @parameter
      */
     public File coverageRootDirectory;
 
     /**
      * List of file patterns to be included for code coverage instrumentation.
      * See <a href="#coverageRootDirectory">coverageRootDirectory</a> for more
      * information. This property is ignored if <a
      * href="#coverageRootDirectory">coverageRootDirectory</a> does not exist or
      * is not a directory. (Passed through
      * <code>--config.coverage.files.includes</code> to <a
      * href="https://github.com/ariatemplates/attester#usage" >attester</a>)
      *
      * @parameter
      */
     public String[] coverageIncludes = new String[] { "**/*.js" };
 
     /**
      * List of file patterns to be excluded from code coverage instrumentation.
      * See <a href="#coverageRootDirectory">coverageRootDirectory</a> for more
      * information. This property is ignored if <a
      * href="#coverageRootDirectory">coverageRootDirectory</a> does not exist or
      * is not a directory. (Passed through
      * <code>--config.coverage.files.excludes</code> to <a
      * href="https://github.com/ariatemplates/attester#usage" >attester</a>)
      *
      * @parameter
      */
     public String[] coverageExcludes;
 
     /**
      * Aria Templates bootstrap file. (Passed through
      * <code>--config.tests.aria-templates.bootstrap</code> to <a
      * href="https://github.com/ariatemplates/attester#usage" >attester</a>)
      *
     * @parameter expression="/aria/aria-templates-${at.version}.js"
      */
     public String ariaTemplatesBootstrap;
 
     /**
      * Aria Templates test classpaths to include. (Passed through
      * <code>--config.tests.aria-templates.classpaths.includes</code> to <a
      * href="https://github.com/ariatemplates/attester#usage" >attester</a>)
      *
      * @parameter
      */
     public String[] ariaTemplatesClasspathsIncludes = new String[] { "MainTestSuite" };
 
     /**
      * Aria Templates test classpaths to exclude. (Passed through
      * <code>--config.tests.aria-templates.classpaths.excludes</code> to <a
      * href="https://github.com/ariatemplates/attester#usage" >attester</a>)
      *
      * @parameter
      */
     public String[] ariaTemplatesClasspathsExcludes;
 
     /**
      * Directory for the set of JUnit-style report files. (Passed through
      * <code>--config.test-reports.xml-directory</code> to <a
      * href="https://github.com/ariatemplates/attester#usage" >attester</a>)
      *
      * @parameter expression="${project.build.directory}/jstestdriver"
      */
     public File xmlReportsDirectory;
 
     /**
      * Single JUnit-style file report. (Passed through
      * <code>--config.test-reports.xml-file</code> to <a
      * href="https://github.com/ariatemplates/attester#usage" >attester</a>)
      *
      * @parameter expression="${project.build.directory}/atjstestsReport.xml"
      */
     public File xmlReportFile;
 
     /**
      * JSON file report. (Passed through
      * <code>--config.test-reports.json-file</code> to <a
      * href="https://github.com/ariatemplates/attester#usage" >attester</a>)
      *
      * @parameter expression="${project.build.directory}/atjstestsReport.json"
      */
     public File jsonReportFile;
 
     /**
      * JSON coverage file report. (Passed through
      * <code>--config.coverage-reports.json-file</code> to <a
      * href="https://github.com/ariatemplates/attester#usage" >attester</a>)
      *
      * @parameter
      *            expression="${project.build.directory}/atjstestsCoverageReport.json"
      */
     public File jsonCoverageReportFile;
 
     /**
      * <a href="http://ltp.sourceforge.net/coverage/lcov/geninfo.1.php">lcov</a>
      * coverage file report. (Passed through
      * <code>--config.coverage-reports.lcov-file</code> to <a
      * href="https://github.com/ariatemplates/attester#usage" >attester</a>)
      *
      * @parameter expression=
      *            "${project.build.directory}/jstestdriver/jsTestDriver.conf-coverage.dat"
      */
     public File lcovCoverageReportFile;
 
     /**
      * Path to the attester directory. If not defined, attester is extracted
      * from the the following maven artifact:
      * <code>com.ariatemplates.attester:attester:zip:project</code>
      *
      * @parameter expression="${com.ariatemplates.attester.path}"
      */
     public File attesterPath;
 
     /**
      * Port for the internal web server. (Passed through <code>--port</code> to
      * <a href="https://github.com/ariatemplates/attester#usage" >attester</a>)
      *
      * @parameter
      */
     public Integer port;
 
     /**
      * Enables or disables colors. (If false, passes <code>--no-colors</code> to
      * <a href="https://github.com/ariatemplates/attester#usage" >attester</a>)
      *
      * @parameter
      */
     public boolean colors = false;
 
     private static final String PATH_IN_ATTESTER_DIRECTORY = "bin" + File.separator + "attester.js";
 
     protected File attesterJsMainFile;
     protected File phantomjsExecutable;
 
     public static Dependency getAttesterDependency() {
         Dependency dependency = new Dependency();
         dependency.setGroupId("com.ariatemplates.attester");
         dependency.setArtifactId("attester");
         dependency.setVersion(RunAttester.class.getPackage().getImplementationVersion());
         dependency.setClassifier("project");
         dependency.setType("zip");
         return dependency;
     }
 
     protected File extractDependency(File property, Dependency dependency, String pathAfterProperty, String pathAfterDependency) {
         File res;
         try {
             if (property != null) {
                 res = new File(property, pathAfterProperty);
             } else {
                 ArtifactExtractor extractor = new ArtifactExtractor();
                 extractor.setLog(this.getLog());
                 String outputDirectory = extractor.inplaceExtractDependency(session.getLocalRepository(), dependency);
                 res = new File(outputDirectory, pathAfterDependency);
             }
             if (!res.exists()) {
                 throw new FileNotFoundException("Could not find file: " + res.getAbsolutePath());
             }
         } catch (Exception e) {
             throw new RuntimeException("Failed to find or extract " + dependency.getArtifactId(), e);
         }
         return res;
     }
 
     protected void extractAttester() {
         attesterJsMainFile = extractDependency(attesterPath, getAttesterDependency(), PATH_IN_ATTESTER_DIRECTORY, "attester" + File.separator
             + PATH_IN_ATTESTER_DIRECTORY);
     }
 
     @Override
     public void execute() throws MojoExecutionException, MojoFailureException {
         extractAttester();
         super.execute();
     }
 
     protected void addMultipleOptions(List<String> optionsArray, String optionName, String[] array) {
         if (array != null) {
             for (String item : array) {
                 optionsArray.add(optionName);
                 optionsArray.add(item);
             }
         }
     }
 
     @Override
     protected List<String> getNodeArguments() {
         List<String> res = new LinkedList<String>();
         res.add(attesterJsMainFile.getAbsolutePath());
 
         if (configFile != null) {
             res.add(configFile.getAbsolutePath());
         }
 
         if (port != null) {
             res.add("--port");
             res.add(port.toString());
         }
 
         if (!colors) {
             res.add("--no-colors");
         }
 
         if (testSourceDirectory.isDirectory()) {
             res.add("--config.resources./");
             res.add(testSourceDirectory.getAbsolutePath());
         }
 
         if (warSourceDirectory.isDirectory()) {
             res.add("--config.resources./");
             res.add(warSourceDirectory.getAbsolutePath());
             if (coverageRootDirectory == null) {
                 coverageRootDirectory = warSourceDirectory;
             }
         }
 
         if (webappDirectory.isDirectory()) {
             res.add("--config.resources./");
             res.add(webappDirectory.getAbsolutePath());
         }
 
         if (coverageRootDirectory != null && coverageRootDirectory.isDirectory()) {
             res.add("--config.coverage.files.rootDirectory");
             res.add(coverageRootDirectory.getAbsolutePath());
 
             addMultipleOptions(res, "--config.coverage.files.includes", coverageIncludes);
             addMultipleOptions(res, "--config.coverage.files.excludes", coverageExcludes);
         }
 
         res.add("--config.test-reports.xml-directory");
         res.add(xmlReportsDirectory.getAbsolutePath());
 
         res.add("--config.test-reports.xml-file");
         res.add(xmlReportFile.getAbsolutePath());
 
         res.add("--config.test-reports.json-file");
         res.add(jsonReportFile.getAbsolutePath());
 
         res.add("--config.coverage-reports.json-file");
         res.add(jsonCoverageReportFile.getAbsolutePath());
 
         res.add("--config.coverage-reports.lcov-file");
         res.add(lcovCoverageReportFile.getAbsolutePath());
 
         res.add("--config.tests.aria-templates.bootstrap");
         res.add(ariaTemplatesBootstrap);
 
         addMultipleOptions(res, "--config.tests.aria-templates.classpaths.includes", ariaTemplatesClasspathsIncludes);
         addMultipleOptions(res, "--config.tests.aria-templates.classpaths.excludes", ariaTemplatesClasspathsExcludes);
 
         addExtraAttesterOptions(res);
 
         res.addAll(arguments);
 
         return res;
     }
 
     protected void addExtraAttesterOptions(List<String> list) {
     }
 
 }
