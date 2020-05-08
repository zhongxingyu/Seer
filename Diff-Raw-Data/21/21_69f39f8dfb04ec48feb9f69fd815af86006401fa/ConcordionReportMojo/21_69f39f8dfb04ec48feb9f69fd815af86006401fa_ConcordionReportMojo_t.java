 package com.mickdudley.concordion;
/*
Licensed to the Apache Software Foundation (ASF) under one
or more contributor license agreements.  See the NOTICE file
distributed with this work for additional information
regarding copyright ownership.  The ASF licenses this file
to you under the Apache License, Version 2.0 (the
"License"); you may not use this file except in compliance
with the License.  You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing,
software distributed under the License is distributed on an
"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
KIND, either express or implied.  See the License for the
specific language governing permissions and limitations
under the License.
 */
 
 import org.apache.maven.project.MavenProject;
 import org.apache.maven.reporting.AbstractMavenReport;
 import org.apache.maven.reporting.MavenReportException;
 import org.codehaus.doxia.site.renderer.SiteRenderer;
 import org.codehaus.plexus.util.FileUtils;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.Locale;
 


 /**
  * @goal report
  * @phase site
  * @author Mick Dudley
  * @description add a concordion report to the site project reports.
  */
 public class ConcordionReportMojo extends AbstractMavenReport {
 
 
     public static final String PLUGIN_NAME = "Concordion-Report ";
     public static final String DEFAULT_REPORT_DIR = "concordion";
     private boolean skipReportGeneration = false;
     /**
      * Output folder where the main page of the report will be generated. Note that this parameter is only relevant if
      * the goal is run directly from the command line or from the default lifecycle. If the goal is run indirectly as
      * part of a site generation, the output directory configured in the Maven Site Plugin will be used instead.
 
      * @parameter default-value="${project.reporting.outputDirectory}"
      * @required
      */
     protected File outputDirectory;
 
 
     /**
      * @parameter default-value="${project}"
      * @required
      * @readonly
      */
     private MavenProject project;
 
     /**
      * @component
      * @required
      * @readonly
      */
     private SiteRenderer siteRenderer;
 
     /**
      * @parameter default-value="${basedir}/target/concordion"
      * @required
      * @description The location of the concordion tests
      */
     private String concordionDir;
 
 
     /**
      * @parameter default-value="index.html"
      * @required
      * @description The location of the concordion index file within concordionDir
      */
     private String concordionIndexFile;
 
 
     public ConcordionReportMojo() {
         super();
     }
 
     /**
      * Only used for testing
      * Note this forces the declaration of a default constructure as well
      */
     protected ConcordionReportMojo(File outputDirectory,
                                 MavenProject project,
                                 SiteRenderer siteRenderer,
                                 String concordionDir,
                                 String concordionIndexFile) {
         this.outputDirectory = outputDirectory;
         this.project = project;
         this.siteRenderer = siteRenderer;
         this.concordionDir = concordionDir;
         this.concordionIndexFile = concordionIndexFile;
     }
 
 
     /**
      * This allows the plugin to define that the report is generated outside the site plugin
      * @return true
      */
     @Override
     public boolean isExternalReport()
     {
         return true;
     }
 
     /**
      * Here we can decide if there are no concordion report files don't add to site
      * @return true - we have a concordion directory and can generate report
      */
     @Override
     public boolean canGenerateReport()
     {
         skipReportGeneration = true;
 
         File sourceDir = new File(this.concordionDir);
         if (! sourceDir.isDirectory()) {
             getLog().info("Skipped \"" + PLUGIN_NAME + "\" report, Concordion report files not found in " + sourceDir.getAbsolutePath());
             return false;
         }
         File index = new File(sourceDir, concordionIndexFile);
         if (! index.exists()) {
             getLog().info("Skipped \"" + PLUGIN_NAME + "\" report, Concordion index file not found " + index.getAbsolutePath());
             return false;
         }
 
         skipReportGeneration = false;
         return true;
     }
 
     /**
      * Generate the report by copying the concordion directory structure into our site
      * @param locale (not used)
      * @throws MavenReportException
      */
     protected void executeReport(Locale locale) throws MavenReportException {
         if (!skipReportGeneration) {
 
             File directory = new File (getOutputDirectory());
             if ( !directory.exists() ) {
                 directory.mkdirs();
             }
 
             try {
                 FileUtils.copyDirectoryStructure(new File(concordionDir), directory);
             } catch (IOException e) {
                 throw new MavenReportException("Error copying concordion reports", e);
             }
         }
     }
 
 
     /**
      * Our subdirectory within the report
      * @return String
      */
     protected String getOutputDirectory() {
         return outputDirectory.getAbsolutePath() + "/" + DEFAULT_REPORT_DIR;
     }
 
     protected MavenProject getProject() {
         return project;
     }
 
     protected SiteRenderer getSiteRenderer() {
         return siteRenderer;
     }
 
     /**
      * The description that appears on the Project Reports page
      * @param locale (not used)
      * @return
      */
     public String getDescription(Locale locale) {
         return "The Concordion acceptance reports for the project";
     }
 
     /**
      * Define the name that appears in the Project Reports menu
      * @param locale (not used)
      * @return String name
      */
     public String getName(Locale locale) {
         return "Concordion";
     }
 
     /**
      * The link to our html file (without .html???)
      * @return String
      */
     public String getOutputName() {
         return DEFAULT_REPORT_DIR + "/" + concordionIndexFile.replace(".html", "");
     }
 
     /**
      * @see org.apache.maven.reporting.AbstractMavenReport#setReportOutputDirectory(java.io.File)
      */
     public void setReportOutputDirectory( File reportOutputDirectory )
     {
         this.outputDirectory = reportOutputDirectory;
     }
 
 }
