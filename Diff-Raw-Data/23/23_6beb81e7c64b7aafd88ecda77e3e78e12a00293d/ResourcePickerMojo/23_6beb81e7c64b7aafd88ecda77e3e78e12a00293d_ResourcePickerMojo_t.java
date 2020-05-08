 package org.peacocks;
 
 /*
  * Copyright 2001-2005 The Apache Software Foundation.
  *
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not
  * use this file except in compliance with the License. You may obtain a copy of
  * the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
  * License for the specific language governing permissions and limitations under
  * the License.
  */
 import java.io.File;
 import java.io.IOException;
 import java.nio.file.Path;
 import java.util.List;
 import org.apache.maven.plugin.AbstractMojo;
 import org.apache.maven.plugin.MojoExecutionException;
 import org.apache.maven.project.MavenProject;
 import org.peacocks.resourcepicker.ResourceGeneration;
 
 /**
  * Goal which touches a timestamp file.
  *
  * @goal resource
  * @phase process-resources
  */
 public class ResourcePickerMojo
         extends AbstractMojo {
 
    /**
    * The default maven project object.
    *
    * @parameter expression="${project}"
    * @required
    * @readonly
    */
    private MavenProject mavenProject;
 
     /**
      * Location of the file. 
      * @parameter default-value="${project.build.directory}/generated-sources/resources"
      * @required
      */
     private File outputDirectory;
 
 
 
     @Override
     public void execute()
             throws MojoExecutionException {
         try {
            List<Path> resources = MavenResourceToPathConverterImpl.INSTANCE.getResourceDirectories(mavenProject.getBuild(), mavenProject.getResources());
             new ResourceGeneration(resources, outputDirectory.toPath()).generateResourceFiles();
             mavenProject.addCompileSourceRoot(outputDirectory.getAbsolutePath());
         } catch (IOException ex) {
             getLog().error("Unable to generate resources", ex);
         }
     }
 
 }
