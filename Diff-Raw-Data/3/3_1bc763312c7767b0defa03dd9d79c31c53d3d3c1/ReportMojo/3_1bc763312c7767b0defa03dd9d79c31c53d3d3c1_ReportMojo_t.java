 package org.apache.maven.plugin.swizzle;
 
 /*
  * Licensed to the Apache Software Foundation (ASF) under one
  * or more contributor license agreements.  See the NOTICE file
  * distributed with this work for additional information
  * regarding copyright ownership.  The ASF licenses this file
  * to you under the Apache License, Version 2.0 (the
  * "License"); you may not use this file except in compliance
  * with the License.  You may obtain a copy of the License at
  *
  *   http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an
  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  * KIND, either express or implied.  See the License for the
  * specific language governing permissions and limitations
  * under the License.
  */
 
 import org.apache.maven.plugin.AbstractMojo;
 import org.apache.maven.plugin.MojoExecutionException;
 
 import org.codehaus.plexus.swizzle.JiraReport;
 import org.codehaus.plexus.swizzle.ReportConfiguration;
 import org.codehaus.plexus.swizzle.ReportGenerationException;
 import org.codehaus.plexus.swizzle.ReportConfigurationException;
 
 import java.io.PrintStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 
 /**
  * Goal which generates a swizzle report based on a velocity template
  * supplied through a ReportConfiguration.
  *
  * @goal generate
  */
 public class ReportMojo
     extends AbstractMojo
 {
     /**
      * @component
      */
     private JiraReport report;
 
     /**
      * Username to use when connecting to the issue tracking system.
      *
      * @parameter default-value="swizzletester"
      */
     private String username;
 
     /**
      * Password to use when connecting to the issue tracking system.
      *
      * @parameter default-value="swizzle"
      */
     private String password;
 
     /**
      * Base URL of the issue tracking server.
      *
      * @parameter default-value="http://jira.codehaus.org"
      */
     private String jiraServerUrl;
 
     /**
      * Identifying key for the project. E.g. SWIZZLE. Wildcards may be used for
      * this parameter.
      *
      * @parameter
      * @required
      */
     private String projectKey;
 
     /**
      * Version of the project. E.g. 2.0.2. Wildcards may be used for this
      * parameter.
      *
      * @parameter expression="${project.version}"
      */
     private String projectVersion;
 
     /**
      * Template to use when generating the reports. Either provide the name for
      * one of default templates (e.g. RESOLVED_ISSUES, VOTES, or XDOC_SECTION)
      * or the path and filename of the custom template to use
      * (e.g. my-path/my-custom-template.vm).
      *
      * @parameter default-value="RESOLVED_ISSUES"
      */
     private String template;
 
 
     /**
      * Name of the file to write the result of the report.
      *
      * @parameter
      * @required
      */
     private String result;
 
     /**
      * If this is set to <code>true</code> or if the RELEASE template is used,
      * parameters for the release info will be retrieved
      * from the POM and placed in the velocity context.
      *
      * @parameter default-value=false
      */
     private boolean releaseInfoNeeded;
 
     /**
      * Retrieved from <pre>${project.groupId}</pre> or provided by the user.
      *
      * @parameter expression="${project.groupId}"
      */
     private String groupId;
 
     /**
      * Retrieved from <pre>${project.artifactId}</pre> or provided by the user.
      *
      * @parameter expression="${project.artifactId}"
      */
     private String artifactId;
 
     /**
      * Retrieved from <pre>${project.scm.connection}</pre> or provided by the
      * user. Should follow the format
      * <pre>scm:[provider]:[provider_specific]</pre>
      *
      * @parameter expression="${project.scm.connection}"
      */
     private String scmConnection;
 
     /**
      * The additional information that uniquely identifies the source version
      * from the scm you want to release. For svn, the revision number should be
      * put here. For cvs, the timestamp should be put here.
      *
      * @parameter default-value=""
      */
     private String scmRevisionId;
 
     /**
      * Where the artifact can be downloaded by the user. Retrieved from
      * <pre>${project.distributionManagement.downloadUrl}</pre> or provided by
      * the user.
      *
      * @parameter expression="${project.distributionManagement.downloadUrl}"
      */
     private String downloadUrl;
 
     /**
      * The staging site where the documentation can be found. Retrieved from
      * <pre>${project.distributionManagement.site.url}</pre> or provided by the
      * user.
      *
      * @parameter expression="${project.distributionManagement.site.url}"
      */
     private String stagingSiteUrl;
 
     /**
      * If the passed the docck check. Right now this is user provided but later
      * will be hooked up to the maven-docck-plugin to automate the check and
      * the report details generation.
      *
      * @parameter default-value=false
      */
     private boolean docckPassed;
 
     /**
      * The result details of the maven-docck-plugin. Right now this is user
      * provided but later will be hooked up to the maven-docck-plugin to
      * automate the check and the report details generation.
 
      * @parameter default-value="${project.build.directory}/docck.txt"
      */
     private String docckResultDetails;
 
     /**
      * If the license check passed.
      *
      * @parameter default-value=false
      */
     private boolean licenseCheckPassed;
 
     /**
      * The result details of the license check.
      *
      * @parameter default-value="${project.build.directory}/licenseck.txt"
      */
     private String licenseCheckResultDetails;
 
     /**
      * The date format to use when reporting the date of the project's last release.
      *
      * @parameter default-value="yyyy-MM-dd hh:mm:ss z"
      */
     private String dateFormat;
 
    /**
     * {@inheritDoc}
     */
     public void execute()
         throws MojoExecutionException
     {
 
         if ( ( null == result ) || ( "".equals( result ) ) )
         {
             throw new MojoExecutionException(
                 "Problem encountered while generating the report: Output path is an empty string or null." );
         }
         try
         {
             ReportConfiguration reportConfiguration = new ReportConfiguration();
 
             reportConfiguration.setUsername( username );
             reportConfiguration.setPassword( password );
             reportConfiguration.setJiraServerUrl( jiraServerUrl );
             reportConfiguration.setProjectKey( projectKey );
             reportConfiguration.setProjectVersion( projectVersion );
             reportConfiguration.setTemplate( template );
 
             if ( JiraReport.RELEASE.equals( template ) || releaseInfoNeeded )
             {
                 reportConfiguration.setGroupId( groupId );
                 reportConfiguration.setArtifactId( artifactId );
                 reportConfiguration.setScmConnection( scmConnection );
                 reportConfiguration.setScmRevisionId( scmRevisionId );
                 reportConfiguration.setDownloadUrl( downloadUrl );
                 reportConfiguration.setStagingSiteUrl( stagingSiteUrl );
                 reportConfiguration.setDocckPassed( docckPassed );
                 reportConfiguration.setDocckResultDetails( docckResultDetails );
                 reportConfiguration.setLicenseCheckPassed( licenseCheckPassed );
                 reportConfiguration.setLicenseCheckResultDetails( licenseCheckResultDetails );
                 reportConfiguration.setDateFormat( dateFormat );
             }
 
             FileOutputStream out = new FileOutputStream( result );
             PrintStream printStream = new PrintStream( out );
 
             report.generateReport( reportConfiguration, printStream );
         }
         catch ( ReportConfigurationException reportConfigurationException )
         {
             throw new MojoExecutionException(
                 "Problem encountered while configuring the plugin: " + reportConfigurationException.getMessage() );
         }
         catch ( IOException fileException )
         {
             throw new MojoExecutionException(
                 "Problem encountered while writing the report output file: " + fileException.getMessage() );
         }
         catch ( ReportGenerationException reportException )
         {
             throw new MojoExecutionException(
                 "Problem encountered while generating the swizzle reports: " + reportException.getMessage() );
         }
     }
 
 }
