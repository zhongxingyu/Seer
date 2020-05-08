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
 
 import org.apache.commons.exec.CommandLine;
 import org.apache.commons.exec.DefaultExecutor;
 import org.apache.commons.exec.Executor;
 import org.apache.commons.exec.ShutdownHookProcessDestroyer;
 import org.apache.maven.plugin.AbstractMojo;
 import org.apache.maven.plugin.MojoExecutionException;
 import org.apache.maven.plugin.MojoFailureException;
 
 import java.io.File;
 import java.io.IOException;
 
 /**
  * @author <a href="mailto:stanley.shyiko@gmail.com">Stanley Shyiko</a>
  */
 abstract class AbstractHadoopMojo extends AbstractMojo {
 
     /**
      * Hadoop Home directory
      * @parameter expression="${hmp.hadoopHome}"
      * @required
      */
     protected File hadoopHome;
 
     /**
      * Hadoop 'conf' directory
      * @parameter expression="${hmp.hadoopConf}"
      */
     protected File hadoopConf;
 
     /**
      * True indicates that any output should be suppressed, false otherwise
      * @parameter expression="${hmp.quiet}" default-value="false"
      */
     protected boolean quiet;
 
     @Override
     public final void execute() throws MojoExecutionException, MojoFailureException {
         HadoopSettings hadoopSettings;
         try {
             hadoopSettings = new HadoopSettings(hadoopHome, hadoopConf);
         } catch (IOException e) {
             throw new MojoFailureException(e.getMessage());
         }
         execute(hadoopSettings);
     }
 
     protected abstract void execute(HadoopSettings hadoopSettings) throws MojoExecutionException, MojoFailureException;
 
     protected void executeCommand(HadoopSettings hadoopSettings, String command) throws IOException {
         executeCommand(hadoopSettings, command, null, false);
     }
 
     protected void executeCommand(HadoopSettings hadoopSettings, String command,
                                   String automaticResponseOnPrompt) throws IOException {
         executeCommand(hadoopSettings, command, automaticResponseOnPrompt, false);
     }
 
     protected void executeCommand(HadoopSettings hadoopSettings, String command,
                                   boolean bindProcessDestroyerToShutdownHook) throws IOException {
         executeCommand(hadoopSettings, command, null, bindProcessDestroyerToShutdownHook);
     }
 
     protected void executeCommand(HadoopSettings hadoopSettings, String command, String automaticResponseOnPrompt,
                                   boolean bindProcessDestroyerToShutdownHook) throws IOException {
        if (getLog().isDebugEnabled()) {
            getLog().debug("Executing " + command);
        }
         Executor executor = new DefaultExecutor();
         executor.setStreamHandler(new ExecutionStreamHandler(quiet, automaticResponseOnPrompt));
         executor.setWorkingDirectory(hadoopSettings.getHomeDirectory());
         if (bindProcessDestroyerToShutdownHook) {
             executor.setProcessDestroyer(new ShutdownHookProcessDestroyer());
         }
         executor.execute(CommandLine.parse(command), hadoopSettings.getEnvironment());
     }
 }
