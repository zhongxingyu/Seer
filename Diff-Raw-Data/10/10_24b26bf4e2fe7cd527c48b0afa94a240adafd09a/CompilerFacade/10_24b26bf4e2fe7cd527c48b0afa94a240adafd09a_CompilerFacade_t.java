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
 
 import org.apache.myfaces.scripting.api.DynamicCompiler;
 import org.apache.myfaces.scripting.core.util.ClassUtils;
 import org.apache.commons.logging.LogFactory;
 import org.apache.commons.logging.Log;
 
 import javax.tools.*;
 import java.io.File;
 import java.util.Arrays;
 import java.util.Locale;
 
 /**
  * A compiler facade encapsulating the JSR 199
  * so that we can switch the implementations
  * of connecting to javac on the fly
  *
  * @author Werner Punz (latest modification by $Author: werpu $)
  * @version $Revision: 812255 $ $Date: 2009-09-07 20:51:39 +0200 (Mo, 07 Sep 2009) $
  */
 public class CompilerFacade implements DynamicCompiler {
 
     JavaCompiler javaCompiler = ToolProvider.getSystemJavaCompiler();
     DiagnosticCollector<JavaFileObject> diagnosticCollector = new DiagnosticCollector();
     ContainerFileManager fileManager = null;
     private static final String FILE_SEPARATOR = File.separator;
 
     public CompilerFacade() {
         super();
         fileManager = new ContainerFileManager(javaCompiler.getStandardFileManager(diagnosticCollector, null, null));
         if (javaCompiler == null) {
             //TODO add other compilers as fallbacks here eclipse ecq being the first
         }
     }
 
     //ok this is a point of no return we cannot avoid it thanks to the dreaded
     //windows file locking, but since this is not for production we can live with it
     public Class compileFile(String sourceRoot, String classPath, String relativeFileName) throws ClassNotFoundException {
 
        
         Iterable<? extends JavaFileObject> fileObjects = fileManager.getJavaFileObjects(sourceRoot + FILE_SEPARATOR + relativeFileName);
        fileManager.getTempDir().setLastModified(0);
         String[] options = new String[]{"-cp", fileManager.getClassPath(), "-d", fileManager.getTempDir().getAbsolutePath(), "-sourcepath", sourceRoot, "-g"};
         javaCompiler.getTask(null, fileManager, diagnosticCollector, Arrays.asList(options), null, fileObjects).call();
         handleDiagnostics(diagnosticCollector);
 
         //now we do a dynamic bytecode reingeneering, to add the needed interfaces
         //so that we can locate the dynamically compiled jsf artefact in a non blocking way.
         String className = ClassUtils.relativeFileToClassName(relativeFileName);
 
         ClassLoader oldClassLoader = Thread.currentThread().getContextClassLoader();
         if (!(oldClassLoader instanceof RecompiledClassLoader)) {
             try {
                 RecompiledClassLoader classLoader = (RecompiledClassLoader) fileManager.getClassLoader(null);
                 Thread.currentThread().setContextClassLoader(classLoader);
 
                 ClassUtils.markAsDynamicJava(fileManager.getTempDir().getAbsolutePath(), className);
 
                 return classLoader.loadClass(className);
             } finally {
                 Thread.currentThread().setContextClassLoader(oldClassLoader);
             }
         }
         return null;
     }
 
     private void handleDiagnostics(DiagnosticCollector<JavaFileObject> diagnosticCollector) throws ClassNotFoundException {
         if (diagnosticCollector.getDiagnostics().size() > 0) {
             Log log = LogFactory.getLog(this.getClass());
             StringBuilder errors = new StringBuilder();
             for (Diagnostic diagnostic : diagnosticCollector.getDiagnostics()) {
                 String error = "Error on line" +
                                diagnostic.getMessage(Locale.getDefault()) + "------" +
                                diagnostic.getLineNumber() + " File:" +
                                diagnostic.getSource().toString();
                 log.error(error);
                 errors.append(error);
 
             }
             throw new ClassNotFoundException("Compile error of java file:" + errors.toString());
         }
     }
 
 }
