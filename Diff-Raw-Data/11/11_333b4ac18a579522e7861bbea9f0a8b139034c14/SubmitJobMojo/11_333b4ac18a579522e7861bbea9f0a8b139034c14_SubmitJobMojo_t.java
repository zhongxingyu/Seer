 /*
  * Copyright 2012 Stanley Shyiko
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package com.github.shyiko.hmp;
 
 import org.apache.commons.exec.ExecuteException;
 import org.apache.maven.plugin.MojoExecutionException;
 import org.apache.maven.plugin.MojoFailureException;
 
 import java.io.File;
 import java.io.IOException;
 
 /**
  * @goal submitJob
  * @author <a href="mailto:stanley.shyiko@gmail.com">Stanley Shyiko</a>
  */
 public class SubmitJobMojo extends AbstractHadoopMojo {
 
     /**
      * Jar file containing job.
      * @parameter expression="${hmp.jar}" default-value="${basedir}/target/${project.build.finalName}.jar"
      * @required
      */
     private File jobJar;
 
     /**
      * Job parameters.
      * @parameter expression="${hmp.jobParameters}"
      */
     private String jobParameters;
 
     /**
      * True indicates that any output should be suppressed, false otherwise
      * @parameter expression="${hmp.job.quiet}" default-value="false"
      */
     private boolean jobQuiet;
 
     @Override
     protected void execute(HadoopSettings hadoopSettings) throws MojoExecutionException, MojoFailureException {
         this.quiet = jobQuiet;
         try {
            executeCommand(hadoopSettings, "bin/hadoop jar " + jobJar.getAbsolutePath() +
                    (jobParameters == null ? "" : " " + jobParameters));
         } catch (ExecuteException e) {
             throw new MojoExecutionException("Hadoop Job failed");
         } catch (IOException e) {
             throw new MojoFailureException("Hadoop Job submission failed: " + e.getMessage());
         }
     }
 }
