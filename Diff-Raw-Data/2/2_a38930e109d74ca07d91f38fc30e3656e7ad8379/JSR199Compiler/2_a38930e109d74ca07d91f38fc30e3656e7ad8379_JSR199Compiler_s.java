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
 package org.apache.myfaces.extensions.scripting.loaders.java.jsr199;
 
 import org.apache.myfaces.extensions.scripting.api.CompilationException;
 import org.apache.myfaces.extensions.scripting.api.CompilationResult;
 import org.apache.myfaces.extensions.scripting.api.CompilerConst;
 import org.apache.myfaces.extensions.scripting.api.ScriptingConst;
 import org.apache.myfaces.extensions.scripting.core.util.FileUtils;
 import org.apache.myfaces.extensions.scripting.core.util.WeavingContext;
 
 import javax.tools.*;
 import java.io.File;
 import java.util.Arrays;
 import java.util.List;
 import java.util.Locale;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  * <p>
  * A compiler facade encapsulating the JSR 199
  * so that we can switch the implementations
  * of connecting to javac on the fly
  * </p>
  * <p>
  * This class is applied to systems which can use the JSR199 compiler
  * API. For older systems we have a javac compiler fallback and
  * probably in the long run also an eclipse as well.
  * </p>
  * <p>
  * We applied first the apache commons-jci project there, but the state
  * of the project was not where we needed it to be for our implementation
  * and fixing and changing it was more work than what was needed for this project.
  * In the dawn of the usage of JSR 199 it simply did not make any more sense
  * to use commons-jci so we rolled our own small specialized facade for this
  * </p>
  *
  * @author Werner Punz (latest modification by $Author: werpu $)
  * @version $Revision: 812255 $ $Date: 2009-09-07 20:51:39 +0200 (Mo, 07 Sep 2009) $
  */
 @SuppressWarnings("unused")
 public class JSR199Compiler implements org.apache.myfaces.extensions.scripting.api.Compiler {
 
     JavaCompiler javaCompiler = ToolProvider.getSystemJavaCompiler();
     ContainerFileManager fileManager = null;
 
     public JSR199Compiler() {
         super();
     }
 
     /**
      * Compile a single file
      *
      * @param sourceRoot the source search path (root of our source)
      * @return the compilation result of the  compilation
      * @throws CompilationException in case of a compilation error
      * @deprecated note we will move over to a single
      *             compile step in the beginning in the long run
      *             we will deprecate it as soon as the full
      *             compile at the beginning of the request
      *             is implemented
      *             <p/>
      *             TODO move this code over to the weaver instead of the compiler
      *             we do not do a single compile step anymore
      */
     public CompilationResult compile(File sourceRoot, File targetPath, File toCompile, ClassLoader classPathHolder) throws CompilationException {
         fileManager = new ContainerFileManager(javaCompiler.getStandardFileManager(new DiagnosticCollector<JavaFileObject>(), null, null));
 
         DiagnosticCollector<JavaFileObject> diagnosticCollector = new DiagnosticCollector<JavaFileObject>();
 
         //TODO add whitelist check here
 
         getLog().info("[EXT-SCRIPTING] Doing a full recompile");
 
         Iterable<? extends JavaFileObject> fileObjects = fileManager.getJavaFileObjects(toCompile);
         String[] options = new String[]{CompilerConst.JC_CLASSPATH, fileManager.getClassPath(), CompilerConst.JC_TARGET_PATH, WeavingContext.getConfiguration().getCompileTarget().getAbsolutePath(), CompilerConst.JC_SOURCEPATH, sourceRoot.getAbsolutePath(), CompilerConst.JC_DEBUG};
         javaCompiler.getTask(null, fileManager, diagnosticCollector, Arrays.asList(options), null, fileObjects).call();
         try {
             handleDiagnostics(diagnosticCollector);
         } catch (ClassNotFoundException e) {
             throw new CompilationException(e);
         }
         return WeavingContext.getCompilationResult(ScriptingConst.ENGINE_TYPE_JSF_JAVA);
 
     }
 
     /**
      * compile all files starting from a given root
      * <p/>
      * note, the java compiler interface does not allow per se
      * wildcards due to its file object indirection
      * we deal with that problem by determine all files manually and then
      * push the list into the jsr compiler interface
      *
      * @param sourceRoot the root for all java sources to be compiled
      * @param loader     the classpath holder for the compilation
      * @throws CompilationException in case of a compilation error
      */
     public CompilationResult compile(File sourceRoot, File destination, ClassLoader loader) throws CompilationException {
         fileManager = new ContainerFileManager(javaCompiler.getStandardFileManager(new DiagnosticCollector<JavaFileObject>(), null, null));
 
         DiagnosticCollector<JavaFileObject> diagnosticCollector = new DiagnosticCollector<JavaFileObject>();
 
         getLog().info("[EXT-SCRIPTING] Doing a full recompile");
 
         List<File> sourceFiles = FileUtils.fetchSourceFiles(WeavingContext.getConfiguration().getWhitelistedSourceDirs(ScriptingConst.ENGINE_TYPE_JSF_JAVA), CompilerConst.JAVA_WILDCARD);
         for (File sourceFile : sourceFiles) {
             if (!sourceFile.exists()) {
                 getLog().log(Level.WARNING, "[EXT-SCRIPTING] Source file with path {0} does not exist it might cause an error in the compilation process", sourceFile.getAbsolutePath());
             }
         }
         Iterable<? extends JavaFileObject> fileObjects = fileManager.getJavaFileObjects(sourceFiles.toArray(new File[sourceFiles.size()]));
         String[] options = new String[]{CompilerConst.JC_CLASSPATH, fileManager.getClassPath(), CompilerConst.JC_TARGET_PATH, WeavingContext.getConfiguration().getCompileTarget().getAbsolutePath(), CompilerConst.JC_SOURCEPATH, sourceRoot.getAbsolutePath(), CompilerConst.JC_DEBUG};
         javaCompiler.getTask(null, fileManager, diagnosticCollector, Arrays.asList(options), null, fileObjects).call();
         try {
             handleDiagnostics(diagnosticCollector);
         } catch (ClassNotFoundException e) {
             throw new CompilationException(e);
         }
         return WeavingContext.getCompilationResult(ScriptingConst.ENGINE_TYPE_JSF_JAVA);
     }
 
     /**
      * internal diagnostics handler
      * which just logs the errors
      *
      * @param diagnosticCollector the compilation results, the jsr 199 uses a DiagnosticsCollector object
      *                            to keep the errors and warnings of the compiler
      * @throws ClassNotFoundException in case of an error (this is enforced by the compiler interface
      *                                and probably will be overhauled in the long run)
      */
     private void handleDiagnostics(DiagnosticCollector<JavaFileObject> diagnosticCollector) throws ClassNotFoundException {
         if (diagnosticCollector.getDiagnostics().size() > 0) {
             Logger log = Logger.getLogger(this.getClass().getName());
             StringBuilder errors = new StringBuilder();
             CompilationResult result = new CompilationResult("");
             boolean hasError = false;
             for (Diagnostic diagnostic : diagnosticCollector.getDiagnostics()) {
                 String error = createErrorMessage(diagnostic);
                 log.log(Level.WARNING, "[EXT-SCRIPTING] Compiler: {0}", error);
 
                 if (diagnostic.getKind().equals(Diagnostic.Kind.ERROR)) {
                     hasError = true;
                     result.getErrors().add(new CompilationResult.CompilationMessage(diagnostic.getLineNumber(), diagnostic.getMessage(Locale.getDefault())));
                 } else {
                     result.getWarnings().add(new CompilationResult.CompilationMessage(diagnostic.getLineNumber(), diagnostic.getMessage(Locale.getDefault())));
                 }
                 errors.append(error);
             }
             WeavingContext.setCompilationResult(ScriptingConst.ENGINE_TYPE_JSF_JAVA, result);
             assertErrorFound(errors, hasError);
         } else {
             WeavingContext.setCompilationResult(ScriptingConst.ENGINE_TYPE_JSF_JAVA, new CompilationResult(""));
         }
     }
 
     /**
      * interruption of the compile flow should only
     * happen if an error has ocurred otherwise we will proceed
      * as expected
      *
      * @param errors   the errors messages found
      * @param hasError marker if an error was found or not
      * @throws ClassNotFoundException in case of a compile error
      */
     private void assertErrorFound(StringBuilder errors, boolean hasError) throws ClassNotFoundException {
         if (hasError) {
             throw new ClassNotFoundException("Compile error of java file:" + errors.toString());
         }
     }
 
     /**
      * creates a standardized error message
      *
      * @param diagnostic the diagnostic of the compiler containing the error data
      * @return a formatted string with the standardized error message which then later
      *         can be processed by the user
      */
     private String createErrorMessage(Diagnostic diagnostic) {
         StringBuilder retVal = new StringBuilder(256);
         if (diagnostic == null) {
             return retVal.toString();
         }
         if (diagnostic.getKind().equals(Diagnostic.Kind.ERROR)) {
             retVal.append(CompilerConst.STD_ERROR_HEAD);
         } else if (diagnostic.getKind().equals(Diagnostic.Kind.NOTE)) {
             retVal.append(CompilerConst.STD_NOTE_HEAD);
         } else if (diagnostic.getKind().equals(Diagnostic.Kind.WARNING)) {
             retVal.append(CompilerConst.STD_WARN_HEAD);
         } else if (diagnostic.getKind().equals(Diagnostic.Kind.MANDATORY_WARNING)) {
             retVal.append(CompilerConst.STD_MANDATORY_WARN_HEAD);
         } else if (diagnostic.getKind().equals(Diagnostic.Kind.OTHER)) {
             retVal.append(CompilerConst.STD_OTHER_HEAD);
         }
         String message = diagnostic.getMessage(Locale.getDefault());
         message = (message == null) ? "" : message;
         retVal.append(message);
         retVal.append(diagnostic.getLineNumber());
 
         retVal.append("\n\n");
 
         String source = "No additional source info";
 
         if (diagnostic.getSource() != null) {
             source = diagnostic.getSource().toString();
         }
         retVal.append(source);
 
         return retVal.toString();
     }
 
     private Logger getLog() {
         return Logger.getLogger(this.getClass().getName());
     }
 
 }
