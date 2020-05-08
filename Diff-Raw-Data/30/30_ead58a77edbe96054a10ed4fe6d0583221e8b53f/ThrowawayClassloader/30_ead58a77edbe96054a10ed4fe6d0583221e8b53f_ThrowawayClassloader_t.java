 /*
  * Licensed to the Apache Software Foundation (ASF) under one
  * or more contributor license agreements.  See the NOTICE file
  * distributed with this work for additional information
  * regarding copyright ownership.  The ASF licenses this file
  * to you under the Apache License, Version 2.0 (the
  * "License"); you may not use this file except in compliance
  * with the License.  You may obtain a copy of the License at
  *
  *   http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an
  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  * KIND, either express or implied.  See the License for the
  * specific language governing permissions and limitations
  * under the License.
  */
 package org.apache.myfaces.extensions.scripting.loaders.java;
 
 import org.apache.myfaces.extensions.scripting.core.util.ClassUtils;
 import org.apache.myfaces.extensions.scripting.core.util.FileUtils;
 import org.apache.myfaces.extensions.scripting.core.util.WeavingContext;
 import org.apache.myfaces.extensions.scripting.refresh.RefreshContext;
 import org.apache.myfaces.extensions.scripting.refresh.ReloadingMetadata;
 
 import java.io.*;
 import java.util.Collection;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import static java.util.logging.Level.*;
 
 /**
  * we move the throw away mechanism into our classloader for cleaner code coverage
  */
 @JavaThrowAwayClassloader
 @SuppressWarnings("unused")
 public class ThrowawayClassloader extends ClassLoader {
 
     static final Logger _logger = Logger.getLogger(ThrowawayClassloader.class.getName());
 
     int _scriptingEngine;
     String _engineExtension;
 
 
     public ThrowawayClassloader(ClassLoader classLoader, int scriptingEngine, String engineExtension) {
         super(classLoader);
 
         synchronized (this.getClass()) {
             _scriptingEngine = scriptingEngine;
             _engineExtension = engineExtension;
         }
     }
 
     public ThrowawayClassloader(ClassLoader classLoader, int scriptingEngine, String engineExtension, boolean untaint) {
         this(classLoader, scriptingEngine, engineExtension);
 
     }
 
     ThrowawayClassloader() {
     }
 
     /*
     * TODO the classcast exception is caused by a loadClassInternal triggered
     * at the time the referencing class is loaded and then by another classload
     * at the time the bean is refreshed
     *
     * we have to check if a class is loaded by loadClassInternal then
     * no other refresh should happen but the loaded class should be issued again)
     *
     * Dont know how to resolve that for now
     */
 
     @Override
     public InputStream getResourceAsStream(String name) {
         File resource = new File(WeavingContext.getConfiguration().getCompileTarget().getAbsolutePath() + File.separator + name);
         if (resource.exists()) {
             try {
                 return new FileInputStream(resource);
             } catch (FileNotFoundException e) {
                 return super.getResourceAsStream(name);
             }
         }
         return super.getResourceAsStream(name);
     }
 
     @Override
     public Class<?> loadClass(String className) throws ClassNotFoundException {
         //check if our class exists in the tempDir
 
         File target = getClassFile(className);
         if (target.exists()) {
            _logger.log(Level.FINE,"[EXT-SCRIPTING] target {0} exists", className);
 
             ReloadingMetadata data = WeavingContext.getFileChangedDaemon().getClassMap().get(className);
             if (data != null && !data.isTainted()) {
                _logger.log(Level.INFO,"[EXT-SCRIPTING] data from target {0} found but not tainted yet", className);
 
                 return data.getAClass();
             }
              _logger.log(Level.FINER,"[EXT-SCRIPTING] loading class {0} from filesystem", className);
 
             FileInputStream iStream = null;
 
             int fileLength;
             byte[] fileContent;
             try {
                 //we cannot load while a compile is in progress
                 //we have to wait until it is one
                 synchronized (RefreshContext.COMPILE_SYNC_MONITOR) {
                     fileLength = (int) target.length();
                     fileContent = new byte[fileLength];
                     iStream = new FileInputStream(target);
                     int result = iStream.read(fileContent);
                     _logger.log(Level.FINER, "read {0} bytes", String.valueOf(result));
                 }
 
                 Class retVal;
 
                 //we have to do it here because just in case
                 //a dependent class is loaded as well we run into classcast exceptions
                 if (data != null) {
                     data.setTainted(false);
 
                     retVal = super.defineClass(className, fileContent, 0, fileLength);
 
                     data.setAClass(retVal);
                     return retVal;
                 } else {
                     //we store the initial reloading meta data information so that it is refreshed
                     //later on, this we we cover dependent classes on the initial load
                     return storeReloadableDefinitions(className, fileLength, fileContent);
                 }
 
             } catch (FileNotFoundException e) {
                 throw new ClassNotFoundException(e.toString());
             } catch (IOException e) {
                 throw new ClassNotFoundException(e.toString());
             } finally {
                 if (iStream != null) {
                     try {
                         iStream.close();
                     } catch (Exception e) {
                         Logger log = Logger.getLogger(this.getClass().getName());
                         log.log(SEVERE, "", e);
                     }
                 }
             }
         }
          _logger.log(Level.FINER,"[EXT-SCRIPTING] target {0} does not exist", target.getAbsolutePath());
         return super.loadClass(className);
     }
 
     private Class<?> storeReloadableDefinitions(String className, int fileLength, byte[] fileContent) {
         Class retVal;
         retVal = super.defineClass(className, fileContent, 0, fileLength);
         ReloadingMetadata reloadingMetaData = new ReloadingMetadata();
         reloadingMetaData.setAClass(retVal);
         //find the source for the given class and then
         //store the filename
         String separator = FileUtils.getFileSeparatorForRegex();
         String fileName = className.replaceAll("\\.", separator) + getStandardFileExtension();
         Collection<String> sourceDirs = WeavingContext.getConfiguration().getSourceDirs(_scriptingEngine);
         String rootDir = null;
         File sourceFile = null;
         for (String sourceDir : sourceDirs) {
             String fullPath = sourceDir + File.separator + fileName;
             sourceFile = new File(fullPath);
             if (sourceFile.exists()) {
                 rootDir = sourceDir;
                 break;
             }
         }
 
         if (rootDir == null) {
             Logger log = Logger.getLogger(this.getClass().getName());
             log.log(WARNING, "Warning source for class: {0} could not be found", className);
             return retVal;
         }
 
         reloadingMetaData.setFileName(fileName);
         reloadingMetaData.setSourcePath(rootDir);
         reloadingMetaData.setTimestamp(sourceFile.lastModified());
         reloadingMetaData.setTainted(false);
         reloadingMetaData.setTaintedOnce(true);
         reloadingMetaData.setScriptingEngine(_scriptingEngine);
 
         WeavingContext.getFileChangedDaemon().getClassMap().put(className, reloadingMetaData);
         return retVal;
     }
 
     protected String getStandardFileExtension() {
         return _engineExtension;
     }
 
     protected Class<?> findClassExposed(String name) throws ClassNotFoundException {
         return super.findClass(name);
     }
 
     public File getClassFile(String className) {
         return ClassUtils.classNameToFile(WeavingContext.getConfiguration().getCompileTarget().getAbsolutePath(), className);
     }
 
    
 }
