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
 
 import java.util.logging.Level;
 import java.util.logging.LogManager;
 import java.util.logging.Logger;
 
 import org.apache.maven.plugin.MojoExecutionException;
 import org.apache.maven.plugin.MojoFailureException;
 import org.apache.maven.plugin.logging.Log;
 
 /**
  * @goal setup-logging
  */
 public class LoggerMojo extends AbstractXCodeMojo
 {
   @Override
   public void execute() throws MojoExecutionException, MojoFailureException
   {
     Logger logger = LogManager.getLogManager().getLogger(AbstractXCodeMojo.class.getPackage().getName());
 
     if (logger == null) {
       getLog().error(
             "Cannot setup logging infrastructure. Logger '" + AbstractXCodeMojo.class.getPackage().getName()
                   + "' was null.");
     }
     else if (logger instanceof XCodePluginLogger) {
       XCodePluginLogger _logger = ((XCodePluginLogger) logger);
       Log mavenLogger = getLog();
       _logger.setLog(mavenLogger);
       if(mavenLogger.isDebugEnabled())
       {
         _logger.setLevel(Level.ALL);
       }
       else 
       {
         _logger.setLevel(Level.INFO);
       }
       
       getLog().debug("Logging infrastructure has been setup.");
     }
     else {
      getLog().error(
             "Cannot setup logging infrastructure. Logger '" + AbstractXCodeMojo.class.getPackage().getName()
                   + "' is not an instance of '" + XCodePluginLogger.class.getName() + "' It was found to be a '"
                  + logger.getClass().getName() + "'.");
     }
   }
 }
