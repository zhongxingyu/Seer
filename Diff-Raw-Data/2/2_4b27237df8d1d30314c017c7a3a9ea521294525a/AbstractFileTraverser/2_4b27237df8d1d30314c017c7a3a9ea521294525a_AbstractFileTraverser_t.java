 /* Copyright 2009-2010 Tracy Flynn
  * 
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
  */
 package net.olioinfo.fileutils;
 
 
 import java.io.File;
 import java.io.IOException;
 
 
 /**
  * <p>An abstract cleas that traverses a directory tree starting at a given point.</p>
  *
  * <p>To make a concrete class, extend and implement onDirectory() and onFile() methods. These methods are called once
  * per directory or file encountered while traversing the tree. Call traverse(somePath) on the root of the tree to
  * start the traversal.</p>
  *
  * <p>The following JVM options are available for use in debugging and isolating problems during initialization.
  * They should not be used in other cases or in production, since they cause performance degradation and
  * may generate a lot of output. These options apply to the whole package.</p>
  *
  * <ul><li>-Dnet.olioinfo.fileutils.consoleTracing</li></ul>
  *
  * <p>Provide detailed tracing to the System.out device. Does not use logging. </p>
  *
  * @author Tracy Flynn
  * @version 0.6
  * @since 0.1
  */
 public abstract class AbstractFileTraverser {
 
     protected boolean consoleTracing = false;
 
     /**
      * <p>Create an instance of AbstractFileTraverser.</p>
      *
      * <p>Initialize logging using log4j. The default 'WARN' logging level can be overridden by specifying
      * -Dnet.olioinfo.fileutils.logLevel=TRACE (or other level) when starting the JVM.
      * </p>
      *
      * <p>To change the logging for this module in a log4j / log4j-ext properties file use the class names</p>
      * <ul>
      *   <li>net.olioinfo.fileutils.AbstractFileTraverser</li>
      *   <li>net.olioinfo.fileutils.AbstractFileAndJarTraverser</li>
      *   <li>net.olioinfo.fileutils.CombinedPropertyFileManager</li>
      * </ul>
      */
     public AbstractFileTraverser(){
         if (System.getProperty("net.olioinfo.fileutils.consoleTracing") != null) {
             consoleTracing = true;
         }
     }
 
     /**
      * Traverse a tree from a given starting point
      *
      * @param f File object indicating starting point
      * @throws IOException
      */
     public final void traverse( final File f ) throws IOException {
        if (consoleTracing) System.out.println("AbstractFileTraverser: traverse: file " + f.getAbsolutePath());
         if (f.exists()) {
             if (consoleTracing) System.out.println("AbstractFileTraverser: traverse: file exists " + f.getAbsolutePath());
             if (f.isDirectory()) {
                 if (consoleTracing) System.out.println("AbstractFileTraverser: traverse: file is a directory " + f.getAbsolutePath());
                 onDirectory(f);
                 final File[] children = f.listFiles();
                 for( File child : children ) {
                     traverse(child);
                 }
                 return;
             }
             onFile(f);
         }
     }
 
     /**
      * Perform this processing on each directory. This method should be overridden by implementations
      *
      * @param d File object representing the directory to be processed
      */
     public abstract void onDirectory( final File d );
 
     /**
      * Perform this processing on each file. This method should be overridden by implementations
      *
      * @param f File object representing the file to be processed
      */
     public abstract void onFile( final File f );
 
 }
