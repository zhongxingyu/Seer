 /*
  * #%L
  * xcode-maven-plugin
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
 import java.io.IOException;
 import java.util.HashSet;
 import java.util.Set;
 
 import org.apache.maven.plugin.MojoExecutionException;
 import org.apache.maven.plugin.MojoFailureException;
 import org.apache.maven.project.MavenProjectHelper;
 import org.codehaus.plexus.archiver.manager.ArchiverManager;
 
 /**
  * Packages all the artifacts for Xcode libraries and prepares the generated artifacts for
  * deployment.
  * 
  * @goal xcode-package
  * 
  */
 public class XCodePackageMojo extends BuildContextAwareMojo
 {
 
   /**
    * @component role="org.codehaus.plexus.archiver.manager.ArchiverManager"
    * @required
    */
   private ArchiverManager archiverManager;
 
   /**
    * @component
    */
   private MavenProjectHelper projectHelper;
 
   /**
    * @parameter
    */
   private Set<String> bundles;
   
   /**
    * The path to the public headers relative to the 
    * built products directory (BUILT_PRODUCTS_DIR).
    * The path specified here needs to be defined in the same way than
    * the PUBLIC_HEADER_FOLDER_PATH inside the xcode project.
    * The folder provided here must be a parent folder of the
    * folder defined in the xcode project as PUBLIC_HEADER_FOLDER_PATH.
   * This parameter can be used in order to create a namespace for the
   * headers of a project. In order to get a namespacing this parameter
   * points to a parent folder of the public header folder path as
   * defined in the xcode project. This causes the headers to be packaged
   * with the directory structue between the directory denoted by this parameter
   * and the directory denoted by the public header folder path inside the xcode
   * project. That directory structure establishes a package like namespacing
   * for the headers that are packaged.
   *
   * @parameter alias="alternatePublicHeaderFolderPath"
   * @since 1.9.0
    */
   private String relativeAlternatePublicHeaderFolderPath;
 
   @Override
   public void execute() throws MojoExecutionException, MojoFailureException
   {
 
     if (bundles == null)
       bundles = new HashSet<String>();
 
     bundles.add(project.getArtifactId());
 
     try {
 
       File projectRootDir = null;
       for (String configuration : getConfigurations()) {
         for (String sdk : getSDKs()) {
           XCodeContext context = getXCodeContext(XCodeContext.SourceCodeLocation.WORKING_COPY, configuration, sdk);
 
           projectRootDir = context.getProjectRootDirectory(); // TODO improve, but should be each time the same directory.
 
           new XCodePackageManager(getLog(), archiverManager, projectHelper).packageHeaders(context, project, relativeAlternatePublicHeaderFolderPath);
           final File buildDir = XCodeBuildLayout.getBuildDir(projectRootDir);
           XCodePackageManager.attachLibrary(context, buildDir, project, projectHelper, getLog());
         }
       }
 
       new XCodePackageManager(getLog(), archiverManager, projectHelper).packageArtifacts(projectRootDir, project,
             bundles);
     }
     catch (IOException ex) {
       throw new MojoExecutionException("", ex);
     }
     catch (XCodeException ex) {
       throw new MojoExecutionException(ex.getMessage(), ex);
     }
   }
 }
