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
 package org.apache.myfaces.scripting.loaders.java.jsr199;
 
 import org.apache.myfaces.scripting.core.util.ClassUtils;
 import org.apache.myfaces.scripting.core.util.FileUtils;
 
 import java.io.File;
 import java.io.FileInputStream;
 
 /**
  * @author Werner Punz (latest modification by $Author$)
  * @version $Revision$ $Date$
  */
 
 public class RecompiledClassLoader extends ClassLoader {
    static File tempDir = null;
     static double _tempMarker = Math.random();
 
 
     RecompiledClassLoader(ClassLoader classLoader) {
         super(classLoader);
         if (tempDir == null) {
             synchronized (this.getClass()) {
                 if (tempDir != null) {
                     return;
                 }
 
                 tempDir = FileUtils.getTempDir();
             }
         }
     }
 
     RecompiledClassLoader() {
     }
 
 
     @Override
     public Class<?> loadClass(String className) throws ClassNotFoundException {
         //check if our class exists in the tempDir
         File target = getClassFile(className);
         if (target.exists()) {
 
             FileInputStream iStream = null;
             int fileLength = (int) target.length();
             byte[] fileContent = new byte[fileLength];
 
             try {
                 iStream = new FileInputStream(target);
                 iStream.read(fileContent);
                 // Erzeugt aus dem byte Feld ein Class Object.
                 return super.defineClass(className, fileContent, 0, fileLength);
 
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
 
     public File getClassFile(String className) {
         return ClassUtils.classNameToFile(tempDir.getAbsolutePath(), className);
     }
 
     public File getTempDir() {
         return tempDir;
     }
 
     public void setTempDir(File tempDir) {
         this.tempDir = tempDir;
     }
 }
