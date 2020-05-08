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
 import java.io.FileFilter;
 import java.io.IOException;
 
 import org.apache.commons.io.FileUtils;
 import org.apache.maven.plugin.MojoExecutionException;
 import org.apache.maven.plugin.MojoFailureException;
 
 /**
  * Copies sources to the checkout directory below the target directory.
  * 
  * @goal copy-sources
  */
 public class XCodeCopySourcesMojo extends AbstractXCodeMojo
 {
 
   @Override
   public void execute() throws MojoExecutionException, MojoFailureException
   {    
     final File baseDirectory = project.getBasedir();
     final File checkoutDirectory = getCheckoutDirectory();
     final String buildDirPath = getProjectBuildDirectory();
     
     getLog().info("Base directory: " + baseDirectory);
     getLog().info("Checkout directory: " + checkoutDirectory);
     getLog().info("BuildDirPath: " + buildDirPath);
     
     
     final File originalLibDir = new File(project.getBuild().getDirectory(), FolderLayout.LIBS_DIR_NAME);
     final File copyOfLibDir = new File(checkoutDirectory, buildDirPath + "/" + FolderLayout.LIBS_DIR_NAME);
     
     final File originalHeadersDir = new File(project.getBuild().getDirectory(), FolderLayout.HEADERS_DIR_NAME);
     final File copyOfHeadersDir = new File(checkoutDirectory, buildDirPath + "/" + FolderLayout.HEADERS_DIR_NAME);
 
     try {
 
       if (checkoutDirectory.exists())
         FileUtils.deleteDirectory(checkoutDirectory);
 
       copy(baseDirectory, checkoutDirectory, new FileFilter() {
 
         @Override
         public boolean accept(File pathname)
         {
           return ! (checkoutDirectory.getAbsoluteFile().equals(pathname.getAbsoluteFile()) || 
                     originalLibDir.getAbsoluteFile().equals(pathname.getAbsoluteFile()) ||
                     originalHeadersDir.getAbsoluteFile().equals(pathname.getAbsoluteFile()));
         }
         
       });
 
       if(originalLibDir.exists())
        com.sap.prd.mobile.ios.mios.FileUtils.createSybolicLink(originalLibDir, copyOfLibDir);
       
       if(originalHeadersDir.exists())
        com.sap.prd.mobile.ios.mios.FileUtils.createSybolicLink(originalHeadersDir, copyOfHeadersDir);
     }
     catch (IOException e) {
       throw new MojoExecutionException(e.getMessage(), e);
     }
   }
 
   /**
   // Return the part of the path between project base directory and project build directory.
   // Assumption is: project build directory is located below project base directory. 
   **/
   private String getProjectBuildDirectory() {
     return com.sap.prd.mobile.ios.mios.FileUtils.getDelta(project.getBasedir(), new File(project.getBuild().getDirectory()));
   }
   private void copy(final File source, final File targetDirectory, final FileFilter excludes) throws IOException
   {
 
     for (final File sourceFile : source.listFiles()) {
       final File destFile = new File(targetDirectory, sourceFile.getName());
       if (sourceFile.isDirectory()) {
         
         if (excludes.accept(sourceFile)) {
           copy(sourceFile, destFile, excludes);
         }
         else {
           getLog().info("File '" + sourceFile + "' ommited.");
         }
       }
       else {
         FileUtils.copyFile(sourceFile, destFile);
         getLog().debug("File '" + sourceFile + "' copied to '" + destFile + "'.");
       }
     }
   }
 }
