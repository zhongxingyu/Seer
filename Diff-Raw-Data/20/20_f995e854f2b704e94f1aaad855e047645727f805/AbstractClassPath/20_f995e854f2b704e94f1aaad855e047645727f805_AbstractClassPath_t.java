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
 
 import com.googlecode.meiyo.ClassPath;
 import com.googlecode.meiyo.ClassPathEntry;
 import com.googlecode.meiyo.ClassPathHandler;
 import com.googlecode.meiyo.ErrorHandler;
 
 /**
  * 
  *
  * @version $Id$
  */
 abstract class AbstractClassPath implements ClassPath {
 
     /**
      * Regular expression that matches a Java identifier.
      */
     private static final String CLASS_EXTENSION = ".class";
 
     private final File path;
 
     private final ClassLoader classLoader;
 
     private final ErrorHandler errorHandler;
 
     public AbstractClassPath(File path, ClassLoader classLoader, ErrorHandler errorHandler) {
         this.path = path;
         this.classLoader = classLoader;
         this.errorHandler = errorHandler;
     }
 
     protected final void handleEntry(String entry, ClassPathHandler... classPathHandlers) {
        if (!entry.endsWith(CLASS_EXTENSION)) {
            return;
        }

         entry = entry.substring(0, entry.lastIndexOf('.')).replace('/', '.');
         try {
             Class<?> clazz = this.classLoader.loadClass(entry);
             ClassPathEntry cpe = new ClassPathEntry(clazz, this);
             for (ClassPathHandler classPathHandler : classPathHandlers) {
                 classPathHandler.doHandle(cpe);
             }
         } catch (Throwable t) {
             this.errorHandler.onClassNotFound(entry);
         }
     }
 
     protected final File getPath() {
         return this.path;
     }
 
     protected final ErrorHandler getErrorHandler() {
         return this.errorHandler;
     }
 
     @Override
     public final String toString() {
         return this.path.getAbsolutePath();
     }
 
 }
