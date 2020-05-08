 /*
  *    Copyright 2010 The Meiyo Team
  *
  *    Licensed under the Apache License, Version 2.0 (the "License");
  *    you may not use this file except in compliance with the License.
  *    You may obtain a copy of the License at
  *
  *       http://www.apache.org/licenses/LICENSE-2.0
  *
  *    Unless required by applicable law or agreed to in writing, software
  *    distributed under the License is distributed on an "AS IS" BASIS,
  *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *    See the License for the specific language governing permissions and
  *    limitations under the License.
  */
 package com.googlecode.meiyo.builder;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.Enumeration;
 import java.util.jar.JarEntry;
 import java.util.jar.JarFile;
 
 import com.googlecode.meiyo.ClassPathHandler;
 import com.googlecode.meiyo.ErrorHandler;
 
 /**
  * 
  * @version $Id$
  */
 final class JARClassPath extends AbstractClassPath {
 
     public JARClassPath(File file, ClassLoader classLoader, ErrorHandler errorHandler) {
         super(file, classLoader, errorHandler);
     }
 
     public void scan(ClassPathHandler... classPathHandlers) {
         try {
             JarFile jarFile = new JarFile(this.getPath());
             Enumeration<JarEntry> enumeration = jarFile.entries();
             while (enumeration.hasMoreElements()) {
                 JarEntry entry = enumeration.nextElement();
                if (!entry.isDirectory()) {
                     this.handleEntry(entry.getName(), classPathHandlers);
                 }
             }
         } catch (IOException e) {
             this.getErrorHandler().onJARReadingError(this.getPath(), e);
         }
     }
 
 }
