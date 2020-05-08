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
 package org.apache.myfaces.scripting.loaders.java;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.apache.myfaces.scripting.api.ScriptingConst;
 import org.apache.myfaces.scripting.core.util.ClassUtils;
 import org.apache.myfaces.scripting.core.util.FileUtils;
 import org.apache.myfaces.scripting.core.util.WeavingContext;
 import org.apache.myfaces.scripting.refresh.ReloadingMetadata;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.InputStream;
 import java.util.Collection;
 
 /**
  * @author Werner Punz (latest modification by $Author$)
  * @version $Revision$ $Date$
  *          <p/>
  *          Classloader which loads the compilates for the scripting engine
  */
 @JavaThrowAwayClassloader
 public class RecompiledClassLoader extends ClassLoader {
     static File tempDir = null;
     static double _tempMarker = Math.random();
     int _scriptingEngine;
 
     String sourceRoot;
 
     public RecompiledClassLoader(ClassLoader classLoader, int scriptingEngine) {
         super(classLoader);
         if (tempDir == null) {
             synchronized (this.getClass()) {
                 if (tempDir != null) {
                     return;
                 }
 
                 tempDir = FileUtils.getTempDir();
             }
         }
         _scriptingEngine = scriptingEngine;
     }
 
     RecompiledClassLoader() {
     }
 
 
     /*
      * TODO the classcast excepton is caused by a loadClassIntrnal triggered
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
         File resource = new File(tempDir.getAbsolutePath() + File.separator + name);
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
         if(className.contains("Blog")) {
             System.out.println("Debugpoint found");
         }
         File target = getClassFile(className);
         if (target.exists()) {
             ReloadingMetadata data = WeavingContext.getFileChangedDaemon().getClassMap().get(className);
             if (data != null && !data.isTainted()) {
                 return data.getAClass();
             }
 
             FileInputStream iStream = null;
             int fileLength = (int) target.length();
             byte[] fileContent = new byte[fileLength];
 
             try {
                 iStream = new FileInputStream(target);
                 iStream.read(fileContent);
                 // Erzeugt aus dem byte Feld ein Class Object.
                 Class retVal = null;
 
                 //we have to do it here because just in case
                 //a dependend class is loaded as well we run into classcast exceptions
                 if (data != null) {
                     data.setTainted(false);
 
                    return super.defineClass(className, fileContent, 0, fileLength);
                 } else {
                     //we store the initial reloading meta data information so that it is refreshed
                     //later on, this we we cover dependend classes on the initial load
                     return storeReloadableDefinitions(className, target, fileLength, fileContent);
                 }
 
             } catch (Exception e) {
                 throw new ClassNotFoundException(e.toString());
             } finally {
                 if (iStream != null) {
                     try {
                         iStream.close();
                     } catch (Exception e) {
                     }
                 }
             }
         }
 
 
         return super.loadClass(className);    //To change body of overridden methods use File | Settings | File Templates.
     }
 
     private Class<?> storeReloadableDefinitions(String className, File target, int fileLength, byte[] fileContent) {
         Class retVal;
         retVal = super.defineClass(className, fileContent, 0, fileLength);
         ReloadingMetadata reloadingMetaData = new ReloadingMetadata();
         reloadingMetaData.setAClass(retVal);
         //find the source for the given class and then
         //store the filename
         String separator = FileUtils.getFileSeparatorForRegex();
         String fileName = className.replaceAll("\\.", separator) + ".java";
         Collection<String> sourceDirs = WeavingContext.getConfiguration().getSourceDirs(ScriptingConst.ENGINE_TYPE_JAVA);
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
             Log log = LogFactory.getLog(this.getClass().getName());
             log.warn("Warning source for class:" + className + " could not be found");
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
 
 
     @Override
     protected Class<?> findClass(String name) throws ClassNotFoundException {
         return super.findClass(name);
     }
 
     public File getClassFile(String className) {
         return ClassUtils.classNameToFile(tempDir.getAbsolutePath(), className);
     }
 
     public File getTempDir() {
         return tempDir;
     }
 
     public void setTempDir(File tempDir) {
         this.tempDir = tempDir;
     }
 
     public String getSourceRoot() {
         return sourceRoot;
     }
 
     public void setSourceRoot(String sourceRoot) {
         this.sourceRoot = sourceRoot;
     }
 }
