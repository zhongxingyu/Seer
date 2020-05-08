 /*
  * SoCoFo - Another source code formatter
  * Copyright (C) 2012  Dirk Strauss
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 /**
  * 
  */
 package com.googlecode.socofo.plugins.maven;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.logging.LogManager;
 
 import org.apache.maven.plugin.AbstractMojo;
 import org.apache.maven.plugin.MojoExecutionException;
 import org.apache.maven.plugin.MojoFailureException;
 import org.apache.maven.project.MavenProject;
 
 import com.google.inject.Guice;
 import com.google.inject.Injector;
 import com.googlecode.socofo.common.api.IOHelper;
 import com.googlecode.socofo.common.api.MainRuntime;
 import com.googlecode.socofo.runtime.impl.RuntimeInjectionPlan;
 
 /**
  * Mojo to format all source codes.
  * 
  * @author Dirk Strauss
  * @version 1.0
  * @goal format
  * @phase pre-compile
  */
 public class FormatMojo extends AbstractMojo {
     /**
      * The base directory to scan for files.
      * 
     * @parameter property="baseDir"
      *            default-value="${project.build.sourceDirectory}/.."
      */
     private File baseDir;
     /**
      * The current project to scan.
      * 
     * @parameter property="project"
      * @required
      * @readonly
      */
     private MavenProject currentPrj = null;
     /**
      * The source file filter. Compared to {@link #types}, this includes
      * patterns to exclude any file that does not match this pattern. So, while
      * the types param sets XML, you can set filters here to pom.xml to only
      * parse pom.xml files, and no other xml file.
      * 
      * @parameter
      */
     private List<String> filters;
     /**
      * URL to the formatter xml.
      * 
     * @parameter property="formatterUrl"
      * @required
      */
     private String formatterUrl;
     /**
      * The io helper service.
      */
     private IOHelper io;
     /**
      * The logging configuration (usually for jul).
      * 
     * @parameter property="logConfig"
      */
     private File logConfig;
     /**
      * The directory to write the results to.
      * 
      * @parameter expression="${project.build.outputDirectory}/../results"
      */
     private File resultDir;
     /**
      * The runtime that performs the actions.
      */
     private MainRuntime socofo = null;
     /**
      * The list of types to scan for.
      * 
     * @parameter property="scanTypes"
      * @required
      */
     private List<String> types;
     
     /**
      * Inits the mojo.
      */
     public FormatMojo() {
         final Injector ij = Guice.createInjector(new RuntimeInjectionPlan());
         socofo = ij.getInstance(MainRuntime.class);
         io = ij.getInstance(IOHelper.class);
         types = new ArrayList<String>();
     }
     
     /**
      * {@inheritDoc}
      */
     @Override
     public void execute() throws MojoExecutionException, MojoFailureException {
         getLog().debug(
             "This goal will format all source code in "
                 + currentPrj.getArtifactId());
         getLog().debug("Checking Logger config");
         if (logConfig != null) {
             FileInputStream fis = null;
             try {
                 fis = new FileInputStream(logConfig);
                 LogManager.getLogManager().readConfiguration(fis);
                 getLog().info("Logger config changed");
             } catch (final FileNotFoundException e) {
                 throw new MojoExecutionException("File " + logConfig
                     + " not found!", e);
             } catch (final SecurityException e) {
                 throw new MojoExecutionException("Security error", e);
             } catch (final IOException e) {
                 throw new MojoExecutionException("IO error", e);
             } finally {
                 io.closeInputstream(fis);
             }
         }
         getLog().debug("Searching for source directory...");
         if (!baseDir.exists()) {
             getLog().error(
                 "Source directory " + baseDir.getAbsolutePath()
                     + " does not exist. Ignoring.");
             return;
         }
         if (!baseDir.canRead()) {
             getLog().error(
                 "Cannot read sources in " + baseDir.getAbsolutePath());
             return;
         }
         socofo.setBaseDirectory(baseDir);
         getLog().debug("Searching target directory..");
         getLog().debug("target dir is " + resultDir.getAbsolutePath());
         socofo.setTargetDirectory(resultDir);
         final String rulesUrlStr = formatterUrl;
         URL rulesUrl;
         try {
             rulesUrl = new URL(rulesUrlStr);
         } catch (final MalformedURLException e) {
             throw new MojoFailureException("Bad rules url: " + rulesUrlStr);
         }
         socofo.setRules(rulesUrl);
         socofo.setTypes(types);
         socofo.applySourceFilters(filters);
         getLog().info("Starting transformation");
         final int runtimeRc = socofo.execute();
         getLog().info("Execution was " + runtimeRc);
     }
     
     /**
      * Sets the scan types.
      * 
      * @param s
      *            a comma,separated list of scan types.
      */
     public void setScanTypes(final String s) {
         if (s == null || s.length() <= 0) {
             return;
         }
         final String[] typesArray = s.split(",");
         for (String type : typesArray) {
             types.add(type);
         }
     }
 }
