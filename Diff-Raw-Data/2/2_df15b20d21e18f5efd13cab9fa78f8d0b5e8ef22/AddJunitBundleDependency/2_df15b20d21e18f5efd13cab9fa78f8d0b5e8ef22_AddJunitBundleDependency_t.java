 package org.mdlavin.tycho.junit.fixer;
 
 /*
  * Copyright 2012 Matt Lavin (matt.lavin@gmail.com).
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
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.text.MessageFormat;
 import java.util.jar.Manifest;
 
 import org.apache.maven.plugin.AbstractMojo;
 import org.apache.maven.plugin.MojoExecutionException;
 import org.apache.maven.plugins.annotations.LifecyclePhase;
 import org.apache.maven.plugins.annotations.Mojo;
 import org.apache.maven.plugins.annotations.Parameter;
 
 /**
  * Goal which fixes MANIFEST.MF files to require junit plugin instead of
  * using Import-Package.
  */
@Mojo( name = "add-junit-bundle-dependency", defaultPhase = LifecyclePhase.GENERATE_RESOURCES )
 public class AddJunitBundleDependency
     extends AbstractMojo
 {
 	
 	private final static String MANIFEST_FILE = "META-INF/MANIFEST.MF";
 	
     /**
      * Location of the file.
      */
     @Parameter( defaultValue = "${basedir}", required = true )
     private File baseDir;
 
     public void execute()
         throws MojoExecutionException
     {
     	getLog().info(MessageFormat.format("About to execute AddJunitBundleDependency in {0}", baseDir));
     	
     	File manifestFile = new File(baseDir, MANIFEST_FILE);
     	
     	if (manifestFile.exists()) {
     		Manifest manifest;
     		try {
 				manifest = loadManifest(manifestFile);
 			} catch (IOException e) {
 				throw new MojoExecutionException(MessageFormat.format("The manifest file {0} could not be read", manifestFile), e);
 			}
     		
     		try {
     			updateManifestFile(manifest);
 			} catch (MojoExecutionException e) {
 				throw new MojoExecutionException(MessageFormat.format("The manifest file {0} could not be updated", manifestFile), e);
 			}
     		
     		try {
     			saveManifest(manifest, manifestFile);
 			} catch (IOException e) {
 				throw new MojoExecutionException(MessageFormat.format("The manifest file {0} could not be saved", manifestFile), e);
 			}
     	} else {
         	getLog().info(MessageFormat.format("The manifest file {0} was not found in  {1} so no changes are being made", MANIFEST_FILE, baseDir));
     	}
     }
     
     private Manifest loadManifest(File manifestFile) throws IOException {
 		FileInputStream fis = new FileInputStream(manifestFile);
 		try {
 			return new Manifest(fis);
 		} finally {
 			fis.close();
 		}
     }
     
     private void saveManifest(Manifest manifest, File manifestFile) throws IOException {
 		FileOutputStream fos = new FileOutputStream(manifestFile);
 		try {
 	    	manifest.write(fos);
 		} finally {
 			fos.close();
 		}
     }
     
     private void updateManifestFile(Manifest manifest) throws MojoExecutionException {
 		new ManifestFileUpdater().update(manifest);
     }
 }
