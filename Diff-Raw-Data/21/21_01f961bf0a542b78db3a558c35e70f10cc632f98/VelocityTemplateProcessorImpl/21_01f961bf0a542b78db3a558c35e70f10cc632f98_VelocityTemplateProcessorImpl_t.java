 /*******************************************************************************
  * Copyright 2011 iovation
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *   http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  ******************************************************************************/
 package org.javadrop;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.util.Map;
 import java.util.Properties;
 
 import org.apache.maven.plugin.MojoExecutionException;
 import org.apache.maven.plugin.logging.Log;
 import org.apache.velocity.VelocityContext;
 import org.apache.velocity.app.Velocity;
import org.apache.velocity.exception.ResourceNotFoundException;
 
 /**
  * This uses velocity to process scripts.
  * 
  * @author gcooperpdx
  * 
  */
 public class VelocityTemplateProcessorImpl implements TemplateProcessor {
     /**
      * Logging for build output
      */
     private Log _mavenLog;
 
     public VelocityTemplateProcessorImpl(Log log) {
         this._mavenLog = log;
     }
 
     private Log getLog() {
         return _mavenLog;
     }
 
     /**
      * Process one of the templates, substituting variables defined in the pom.
      * 
      * @param inputTemplateFile
      *            File to be processed, without the '.vm'
      * @param convertedFile
      *            Filename, including directory, for the final file.
      */
     @Override
     public void applyVTemplate(File inputTemplateFile, File convertedFile,
             Map<String, String> templateParams) throws MojoExecutionException {
         VelocityContext velContext = new VelocityContext();
         for (Map.Entry<String, String> tParam : templateParams.entrySet()) {
             velContext.put(tParam.getKey(), tParam.getValue());
         }
         // Get the .vm templates from the classpath
         Properties props = new Properties();
         props.setProperty("resource.loader", "classpath");
         props.setProperty("classpath.resource.loader.class",
                 "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
         try {
             Velocity.init(props);
             File outputDirectory = convertedFile.getParentFile();
             outputDirectory.mkdirs();
             BufferedWriter writer = new BufferedWriter(new FileWriter(
                     convertedFile));
 
             Velocity.mergeTemplate(inputTemplateFile.getPath(), "UTF-8",
                     velContext, writer);
             writer.flush();
             writer.close();
        } catch (ResourceNotFoundException rex) {
            getLog().warn("Resource not found:" + inputTemplateFile.getAbsolutePath());
         } catch (Exception e) {
             getLog().error("Velocity processing problem", e);
             throw new MojoExecutionException(
                     "Velocity template processing problem within maven plugin");
         }
     }
 
 }
