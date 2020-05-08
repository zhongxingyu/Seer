 /*
  * Licensed to the Apache Software Foundation (ASF) under one
  * or more contributor license agreements.  See the NOTICE file
  * distributed with this work for additional information
  * regarding copyright ownership.  The ASF licenses this file
  * to you under the Apache License, Version 2.0 (the
  * "License"); you may not use this file except in compliance
  * with the License.  You may obtain a copy of the License at
  *
  *  http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an
  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  * KIND, either express or implied.  See the License for the
  * specific language governing permissions and limitations
  * under the License.
  */
 
 package org.apache.geronimo.genesis.plugins.tools;
 
 import java.io.File;
 
 import org.codehaus.plexus.util.DirectoryScanner;
 
 import org.apache.maven.plugin.MojoExecutionException;
 import org.apache.maven.plugin.MojoFailureException;
 import org.apache.maven.project.MavenProject;
 import org.apache.tools.ant.taskdefs.Copy;
 import org.apache.tools.ant.types.FileSet;
 
 import org.apache.geronimo.genesis.MojoSupport;
 import org.apache.geronimo.genesis.ant.AntHelper;
 
 /**
  * Copy legal files for inclusion into generated jars.
  *
  * @goal copy-legal-files
  * @phase validate
  *
  * @version $Rev$ $Date$
  */
 public class CopyLegalFilesMojo
     extends MojoSupport
 {
     /**
      * The default includes when no fileset is configured.
      */
     private static final String[] DEFAULT_INCLUDES = {
         "LICENSE.txt",
         "LICENSE",
         "NOTICE.txt",
         "NOTICE",
         "DISCLAIMER.txt",
         "DISCLAIMER"
     };
 
     /**
      * Directory to copy legal files into.
      *
      * @parameter expression="${project.build.outputDirectory}/META-INF"
      * @required
      */
     private File outputDirectory = null;
 
     /**
      * The basedir of the project.
      *
      * @parameter expression="${basedir}"
      * @required
      * @readonly
      */
     protected File basedir;
 
     /**
      * The set of legal files to be copied.  Default fileset includes: LICENSE[.txt], NOTICE[.txt] and DISCLAIMER[.txt].
      *
      * @parameter
      */
     private DirectoryScanner fileset;
     
     /**
      * When set to true, fail the build when no legal files are found.
      *
     * @parameter default-value="false"
      */
     private boolean strict;
     
     /**
      * @component
      */
     private AntHelper ant;
 
     //
     // MojoSupport Hooks
     //
 
     /**
      * The maven project.
      *
      * @parameter expression="${project}"
      * @required
      * @readonly
      */
     protected MavenProject project = null;
 
     protected MavenProject getProject() {
         return project;
     }
 
     //
     // Mojo
     //
 
     protected void init() throws MojoExecutionException, MojoFailureException {
         super.init();
 
         ant.setProject(getProject());
     }
 
     protected void doExecute() throws Exception {
         if (!shouldInstallLegalFiles(getProject())) {
             return;
         }
 
         if (fileset == null) {
             fileset = new DirectoryScanner();
             fileset.setBasedir(basedir);
             fileset.setIncludes(DEFAULT_INCLUDES);
         }
 
         fileset.addDefaultExcludes();
         fileset.scan();
 
         String[] filenames = fileset.getIncludedFiles();
 
         if (filenames.length == 0) {
             if (strict) {
                 throw new MojoExecutionException("No legal files found to copy");
             }
             else {
                 log.warn("No legal files found to copy");
             }
             
             return;
         }
 
         ant.mkdir(outputDirectory);
 
         Copy copy = (Copy)ant.createTask("copy");
         copy.setTodir(outputDirectory);
 
         FileSet files = ant.createFileSet();
         files.setDir(basedir);
 
         for (int i=0; i<filenames.length; i++) {
             files.createInclude().setName(filenames[i]);
         }
 
         copy.addFileset(files);
 
         copy.execute();
     }
 
     private boolean shouldInstallLegalFiles(final MavenProject project) {
         assert project != null;
 
         //
         // TODO: Expose a list of packagings
         //
         
         return !"pom".equals(getProject().getPackaging());
     }
 }
