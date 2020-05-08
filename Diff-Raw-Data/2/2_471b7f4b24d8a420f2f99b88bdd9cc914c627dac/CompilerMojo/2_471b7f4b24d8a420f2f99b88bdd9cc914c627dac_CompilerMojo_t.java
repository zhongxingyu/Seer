 /*
  * Copyright (C) 2011 Klaus Reimer <k@ailis.de>
  *
  * Licensed to the Apache Software Foundation (ASF) under one or more
  * contributor license agreements. See the NOTICE file distributed with this
  * work for additional information regarding copyright ownership. The ASF
  * licenses this file to you under the Apache License, Version 2.0 (the
  * "License"); you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
  * License for the specific language governing permissions and limitations under
  * the License.
  */
 
 package de.ailis.maven.plugin.javascript;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStreamWriter;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Enumeration;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 import java.util.jar.JarEntry;
 import java.util.jar.JarFile;
 import java.util.logging.Level;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipInputStream;
 
 import org.apache.maven.plugin.AbstractMojo;
 import org.apache.maven.plugin.MojoExecutionException;
 import org.apache.maven.plugin.MojoFailureException;
 import org.codehaus.plexus.util.DirectoryScanner;
 import org.codehaus.plexus.util.FileUtils;
 import org.codehaus.plexus.util.Scanner;
 import org.sonatype.plexus.build.incremental.BuildContext;
 
 import com.google.common.collect.Lists;
 import com.google.common.io.LimitInputStream;
 import com.google.javascript.jscomp.AnonymousFunctionNamingPolicy;
 import com.google.javascript.jscomp.CheckLevel;
 import com.google.javascript.jscomp.ClosureCodingConvention;
 import com.google.javascript.jscomp.CompilationLevel;
 import com.google.javascript.jscomp.Compiler;
 import com.google.javascript.jscomp.CompilerOptions;
 import com.google.javascript.jscomp.CompilerOptions.TracerMode;
 import com.google.javascript.jscomp.CompilerOptions.TweakProcessing;
 import com.google.javascript.jscomp.DiagnosticGroups;
 import com.google.javascript.jscomp.ErrorFormat;
 import com.google.javascript.jscomp.JSSourceFile;
 import com.google.javascript.jscomp.PropertyRenamingPolicy;
 import com.google.javascript.jscomp.Result;
 import com.google.javascript.jscomp.SourceMap;
 import com.google.javascript.jscomp.VariableRenamingPolicy;
 import com.google.javascript.jscomp.WarningLevel;
 
 /**
  * This Mojo processes the JavaScript files in the source directory, copies,
  * bundles and compiles them into specific folders in the output directory.
  * Compile dependencies are used as so-called externals for the Closure Compiler
  * which is used to compile the sources.
  *
  * @author Klaus Reimer (k@ailis.de)
  * @goal compile
  * @phase compile
  * @threadSafe
  * @requiresDependencyResolution compile
  */
 public class CompilerMojo extends AbstractMojo
 {
     /**
      * The character encoding scheme to be applied when filtering resources.
      *
      * @parameter default-value="${project.build.sourceEncoding}"
      */
     private String encoding;
 
     /**
      * Indicates whether the build will continue even if there are compilation
      * errors; defaults to true.
      *
      * @parameter default-value="true"
      */
     private boolean failOnError;
 
     /**
      * The source directories containing the sources to be compiled.
      *
      * @parameter default-value="${project.build.sourceDirectory}"
      * @required
      * @readonly
      */
     private File sourceDirectory;
 
     /**
      * A list of inclusion filters for the compiler.
      *
      * @parameter
      */
     private String[] includes;
 
     /**
      * A list of exclusion filters for the compiler.
      *
      * @parameter
      */
     private String[] excludes;
 
     /**
      * If incremental builds are allowed. This has no effect on the command line
      * but in a IDE this flag can be used to disable the compile-on-save
      * behavior. Please note that the plugin doesn't support real incremental
      * builds. A full build is performed everytime the IDE requests an
      * incremental build (if at least one file was changed).
      *
      * @parameter default-value="true"
      */
     private boolean incremental;
 
     /**
      * The output directory for the compiled scripts.
      *
      * @parameter default-value="${project.build.outputDirectory}"
      * @required
      * @readonly
      */
     private File outputDirectory;
 
     /**
      * The output directory for the uncompressed script files.
      *
      * @parameter expression="${project.build.outputDirectory}/script-sources"
      * @required
      * @readonly
      */
     private File scriptSourcesDirectory;
 
     /**
      * The output directory for the uncompressed script source bundles.
      *
      * @parameter
      *            expression="${project.build.outputDirectory}/script-source-bundles"
      * @required
      * @readonly
      */
     private File scriptSourceBundlesDirectory;
 
     /**
      * The output directory for the script bundles.
      *
      * @parameter expression="${project.build.outputDirectory}/script-bundles"
      * @required
      * @readonly
      */
     private File scriptBundlesDirectory;
 
     /**
      * The output directory for the single compiled script files.
      *
      * @parameter expression="${project.build.outputDirectory}/scripts"
      * @required
      * @readonly
      */
     private File scriptsDirectory;
 
     /**
      * The output filename of the bundle file.
      *
      * @parameter expression="${project.artifactId}.js"
      * @required
      */
     private String bundleFilename;
 
     /**
      * Project classpath.
      *
      * @parameter default-value="${project.compileClasspathElements}"
      * @required
      * @readonly
      */
     private List<String> classpathElements;
 
     /**
      * The build context.
      *
      * @component
      */
     private BuildContext buildContext;
 
     /**
      * @see org.apache.maven.plugin.Mojo#execute()
      */
     @Override
     public void execute() throws MojoExecutionException, MojoFailureException
     {
         // Do nothing if an incremental build was requested but it is disabled
         // in the plugin configuration.
         if (!this.incremental && this.buildContext.isIncremental()) return;
 
         // Get the files to compile. Do nothing if no file was found.
         final String[] files = getFiles();
         if (files.length == 0) return;
 
         // Remove all messages
         for (final String file: files)
             this.buildContext.removeMessages(new File(this.sourceDirectory, file));
 
         // Create externs and source files.
         final List<JSSourceFile> externs = createExterns();
         final List<JSSourceFile> sources = createSourceFiles(files);
 
         // Write the uncompiled script sources to the output directory.
         writeScriptSources(sources);
 
         // Initialize the dependency manager.
         final DependencyManager dependencyManager =
             new DependencyManager(this.sourceDirectory);
         try
         {
             dependencyManager.addScripts(sources);
         }
         catch (final IOException e)
         {
             throw new MojoExecutionException(
                 "Unable to read source file: " + e, e);
         }
 
         // Sort source files according to automatically detected order.
         // Execution is stopped when a circular reference is detected.
         try
         {
             Collections.sort(sources,
                 new OrderComparator(dependencyManager.resolve()));
         }
         catch (final CircularDependencyException e)
         {
             this.buildContext.addMessage(new File(e.getFilename()), 0, 0,
                 e.getMessage(), BuildContext.SEVERITY_ERROR, e);
             if (this.failOnError)
                 throw new MojoFailureException(e.getMessage(), e);
         }
 
         // Write the uncompiled sources.
         writeScriptSourceBundle(sources, dependencyManager);
 
         // Compile the javascript sources.
         getLog().info("Compiling " + sources.size() + " file(s) with "
             + externs.size() + " extern(s)");
         final Compiler compiler = createCompiler();
         final CompilerOptions options = createCompilerOptions();
         final Result result = compiler.compile(externs, sources, options);
         if (result.success)
         {
             // Write the script bundle
             final String source = compiler.toSource();
             writeScriptBundle(source, dependencyManager);
 
             final String[] compiledSources = compiler.toSourceArray();
             for (int i = 0; i < sources.size(); i++)
             {
                 writeScript(sources.get(i), compiledSources[i],
                     dependencyManager.getDependencies(sources.get(i)));
             }
         }
         else if (this.failOnError)
             throw new MojoFailureException(result.errors.length +
                 " JavaScript errors detected");
     }
 
     /**
      * Scans for files to compile and returns them as a list of filenames.
      *
      * @return The files to compile. Never null. May be empty if no files are to
      *         be compiled.
      */
     private String[] getFiles()
     {
         // If build is incremental then check if there is at least one file
         // to be build.
         if (this.buildContext.isIncremental())
         {
             final Scanner scanner =
                 this.buildContext.newScanner(this.sourceDirectory);
             setupScanner(scanner);
             scanner.scan();
             final String[] files = scanner.getIncludedFiles();
             if (files.length == 0) return files;
         }
 
         // Perform the real scan (Ignoring incremental build because we
         // can't do it anyway. So when at least one included file was changed
         // then we do a full rebuild).
         final DirectoryScanner scanner = new DirectoryScanner();
         scanner.setBasedir(this.sourceDirectory);
         setupScanner(scanner);
         scanner.scan();
         return scanner.getIncludedFiles();
     }
 
     /**
      * Setup the specified scanner with the include and exclude filters.
      *
      * @param scanner
      *            The scanner to setup
      */
     private void setupScanner(final Scanner scanner)
     {
         scanner.addDefaultExcludes();
         scanner.setExcludes(this.excludes);
         if (this.includes != null && this.includes.length != 0)
             scanner.setIncludes(this.includes);
         else
             scanner.setIncludes(new String[] { "**/*.js" });
     }
 
     /**
      * Creates JavaScript source files from the specified list of file names.
      *
      * @param filenames
      *            The list of file names.
      * @return The list of JavaScript source files.
      */
     private List<JSSourceFile> createSourceFiles(final String[] filenames)
     {
         final List<JSSourceFile> sourceFiles = new ArrayList<JSSourceFile>();
         for (final String filename : filenames)
         {
             final File file = new File(this.sourceDirectory, filename);
             if (file.exists())
             {
                 final JSSourceFile sourceFile =
                     JSSourceFile.fromFile(file);
                 sourceFile.setOriginalPath(filename);
                 sourceFiles.add(sourceFile);
             }
         }
         return sourceFiles;
     }
 
     /**
      * Gets the default externs set.
      *
      * @return The default externs.
      * @throws MojoExecutionException
      *             When an exception occurs during processing of externs.
      */
     private List<JSSourceFile> createDefaultExterns()
         throws MojoExecutionException
     {
         try
         {
             final InputStream input =
                 com.google.javascript.jscomp.Compiler.class
                     .getResourceAsStream("/externs.zip");
             final ZipInputStream zip = new ZipInputStream(input);
             final List<JSSourceFile> externs = Lists.newLinkedList();
 
             for (ZipEntry entry; (entry = zip.getNextEntry()) != null;)
             {
                 final LimitInputStream entryStream = new LimitInputStream(zip,
                     entry
                         .getSize());
                 externs.add(JSSourceFile.fromInputStream(entry.getName(),
                     entryStream));
             }
 
             return externs;
         }
         catch (final IOException e)
         {
             throw new MojoExecutionException(e.toString(), e);
         }
     }
 
     /**
      * Searches for extern files.
      *
      * @return The extern files.
      * @throws MojoExecutionException
      *             If an exception occurs while searching for extern files.
      */
     private List<JSSourceFile> createExterns() throws MojoExecutionException
     {
         final List<JSSourceFile> externs = new ArrayList<JSSourceFile>();
 
         externs.addAll(createDefaultExterns());
 
         final String outputLocation = this.outputDirectory.getAbsolutePath();
         for (final String entry : this.classpathElements)
         {
             // Ignore our own classpath entry
             if (entry.equals(outputLocation)) continue;
 
             final File file = new File(entry);
             if (file.isDirectory())
             {
                 File externDir = new File(file, "script-externs");
                 if (!externDir.exists())
                     externDir = new File(file, "script-source-bundles");
                 if (!externDir.exists()) continue;
                 for (final File externFile : externDir.listFiles())
                 {
                     final JSSourceFile source =
                         JSSourceFile.fromFile(externFile);
                     externs.add(source);
                 }
             }
             else
             {
                 try
                 {
                     final JarFile jarFile = new JarFile(file);
                     final Enumeration<JarEntry> jarEntries = jarFile.entries();
                     while (jarEntries.hasMoreElements())
                     {
                         final JarEntry jarEntry = jarEntries.nextElement();
                         if (jarEntry.isDirectory()) continue;
                         final String entryName = jarEntry.getName();
                         if (entryName.startsWith("script-externs/")
                             || entryName.startsWith("script-source-bundles/"))
                         {
                             final InputStream stream =
                                 jarFile.getInputStream(jarEntry);
                             try
                             {
                                 externs.add(JSSourceFile.fromInputStream(
                                     file.getAbsolutePath() + ":" + entryName,
                                     stream));
                             }
                             finally
                             {
                                 stream.close();
                             }
                         }
                     }
                 }
                 catch (final IOException e)
                 {
                     throw new MojoExecutionException(e.toString(), e);
                 }
             }
         }
         return externs;
     }
 
     /**
      * Writes the script sources to the output directory.
      *
      * @param sourceFiles
      *            the source files to write.
      * @throws MojoExecutionException
      *             When source file could not be written to output directory.
      */
     private void writeScriptSources(final List<JSSourceFile> sourceFiles)
         throws MojoExecutionException
     {
         for (final JSSourceFile sourceFile : sourceFiles)
         {
             final File output =
                 new File(this.scriptSourcesDirectory,
                     sourceFile.getOriginalPath());
             output.getParentFile().mkdirs();
             try
             {
                 FileUtils.fileWrite(output.getAbsolutePath(), this.encoding,
                     sourceFile.getCode());
             }
             catch (final IOException e)
             {
                 throw new MojoExecutionException(e.toString(), e);
             }
         }
     }
 
     /**
      * Creates the closure compiler.
      *
      * @return The closure compiler.
      */
     private Compiler createCompiler()
     {
         final Compiler compiler = new Compiler();
         Compiler.setLoggingLevel(Level.OFF);
         final MavenErrorManager errorManager = new MavenErrorManager(
             getLog(), this.buildContext);
         compiler.setErrorManager(errorManager);
         return compiler;
     }
 
     /**
      * Creates and returns the compiler options.
      *
      * @return The compiler options
      */
     private CompilerOptions createCompilerOptions()
     {
         final CompilerOptions options = new CompilerOptions();
         CompilationLevel.SIMPLE_OPTIMIZATIONS
             .setOptionsForCompilationLevel(options);
         WarningLevel.VERBOSE.setOptionsForWarningLevel(options);
 
         options.aggressiveVarCheck = CheckLevel.ERROR;
         options.aliasableGlobals = null;
         options.aliasableStrings = Collections.emptySet();
         options.aliasAllStrings = false;
         options.aliasExternals = false;
         options.aliasKeywords = false;
         options.aliasStringsBlacklist = "";
         options.allowLegacyJsMessages = false;
         options.ambiguateProperties = false;
         options.anonymousFunctionNaming = AnonymousFunctionNamingPolicy.OFF;
         options.appNameStr = "";
 
         options.brokenClosureRequiresLevel = CheckLevel.ERROR;
 
         options.checkCaja = false;
         options.checkControlStructures = true;
         options.checkDuplicateMessages = true;
         options.checkEs5Strict = true;
         options.checkFunctions = CheckLevel.ERROR;
         options.checkGlobalNamesLevel = CheckLevel.ERROR;
         options.checkGlobalThisLevel = CheckLevel.ERROR;
         options.checkMethods = CheckLevel.ERROR;
         options.checkMissingGetCssNameBlacklist = null;
         options.checkMissingGetCssNameLevel = CheckLevel.OFF;
         options.checkMissingReturn = CheckLevel.ERROR;
         options.checkProvides = CheckLevel.OFF;
         options.checkRequires = CheckLevel.OFF;
         options.checkShadowVars = CheckLevel.WARNING;
         options.checkSuspiciousCode = false;
         options.checkSymbols = true;
         options.checkTypedPropertyCalls = true;
         options.checkTypes = true;
         options.checkUnreachableCode = CheckLevel.WARNING;
         options.closurePass = false;
         options.coalesceVariableNames = false;
         options.collapseAnonymousFunctions = false;
         options.collapseProperties = false;
         options.collapseObjectLiterals = false;
         options.collapseVariableDeclarations = true;
         options.computeFunctionSideEffects = true;
         options.convertToDottedProperties = true;
         options.crossModuleCodeMotion = false;
         options.crossModuleMethodMotion = false;
         options.cssRenamingMap = null;
         options.customPasses = null;
 
         options.deadAssignmentElimination = true;
         options.debugFunctionSideEffectsPath = null;
         options.decomposeExpressions = false;
         options.devirtualizePrototypeMethods = false;
         options.disambiguateProperties = false;
         options.disableRuntimeTypeCheck();
 
         options.errorFormat = ErrorFormat.SINGLELINE;
         options.exportTestFunctions = false;
         options.extractPrototypeMemberDeclarations = false;
         options.enableExternExports(false);
 
         options.flowSensitiveInlineVariables = false;
         options.foldConstants = true;
 
         options.gatherCssNames = false;
         options.generateExports = false;
         options.generatePseudoNames = false;
         options.groupVariableDeclarations = true;
 
         options.ideMode = false;
         options.ignoreCajaProperties = false;
         options.inferTypesInGlobalScope = true;
         options.inlineAnonymousFunctionExpressions = false;
         options.inlineConstantVars = false;
         options.inlineFunctions = false;
         options.inlineGetters = false;
         options.inlineLocalVariables = false;
         options.inlineLocalFunctions = false;
         options.inlineVariables = false;
         options.inputDelimiter = "// Input %num%";
         options.inputPropertyMapSerialized = null;
         options.inputVariableMapSerialized = null;
         options.instrumentationTemplate = null;
         options.instrumentForCoverage = false;
         options.instrumentForCoverageOnly = false;
 
         options.labelRenaming = false;
         options.lineBreak = true;
         options.locale = "UTF-8";
         options.lineLengthThreshold(500);
 
         options.markAsCompiled = false;
         options.markNoSideEffectCalls = false;
         options.messageBundle = null;
         options.moveFunctionDeclarations = false;
 
         options.nameReferenceGraphPath = null;
         options.nameReferenceReportPath = null;
 
         options.optimizeArgumentsArray = true;
         options.optimizeCalls = false;
         options.optimizeParameters = false;
         options.optimizeReturns = false;
 
         options.prettyPrint = false;
         options.printInputDelimiter = false;
         options.propertyRenaming = PropertyRenamingPolicy.OFF;
 
         options.recordFunctionInformation = false;
         options.removeDeadCode = false;
         options.removeEmptyFunctions = false;
         options.removeTryCatchFinally = false;
         options.removeUnusedLocalVars = false;
         options.removeUnusedPrototypeProperties = false;
         options.removeUnusedPrototypePropertiesInExterns = false;
         options.removeUnusedVars = false;
         options.renamePrefix = null;
        options.reportMissingOverride = CheckLevel.OFF; // Generates error messages outside of the compiled code for some reason.
         options.reportPath = null;
         options.reportUnknownTypes = CheckLevel.ERROR;
         options.reserveRawExports = false;
         options.rewriteFunctionExpressions = false;
 
         options.smartNameRemoval = false;
         options.sourceMapDetailLevel = SourceMap.DetailLevel.SYMBOLS;
         options.sourceMapFormat = SourceMap.Format.DEFAULT;
         options.sourceMapOutputPath = null;
         options.specializeInitialModule = false;
         options.strictMessageReplacement = false;
         options.stripNamePrefixes = Collections.emptySet();
         options.stripNameSuffixes = Collections.emptySet();
         options.stripTypePrefixes = Collections.emptySet();
         options.stripTypes = Collections.emptySet();
         options.syntheticBlockEndMarker = null;
         options.syntheticBlockStartMarker = null;
 
         options.setAcceptConstKeyword(false);
         options.setChainCalls(true);
         options.setCodingConvention(new ClosureCodingConvention());
         options.setCollapsePropertiesOnExternTypes(false);
         options.setColorizeErrorOutput(true);
         final Set<String> extraAnnotationNames = new HashSet<String>();
         extraAnnotationNames.add("require");
         extraAnnotationNames.add("use");
         extraAnnotationNames.add("provide");
         options.setExtraAnnotationNames(extraAnnotationNames);
         options.setLooseTypes(false);
         options.setManageClosureDependencies(false);
         options.setNameAnonymousFunctionsOnly(false);
         options.setOutputCharset("UTF-8");
         options.setProcessObjectPropertyString(false);
         options.setRemoveAbstractMethods(true);
         options.setRemoveClosureAsserts(true);
         options.setRewriteNewDateGoogNow(false);
         options.setSummaryDetailLevel(3);
         options.setTweakProcessing(TweakProcessing.OFF);
         options.setWarningLevel(DiagnosticGroups.ACCESS_CONTROLS,
             CheckLevel.ERROR);
         options.setWarningLevel(DiagnosticGroups.AMBIGUOUS_FUNCTION_DECL,
             CheckLevel.ERROR);
         options
             .setWarningLevel(DiagnosticGroups.CHECK_REGEXP, CheckLevel.ERROR);
 
         options.setWarningLevel(DiagnosticGroups.CHECK_TYPES, CheckLevel.ERROR);
         options.setWarningLevel(DiagnosticGroups.CHECK_USELESS_CODE,
             CheckLevel.WARNING);
         options.setWarningLevel(DiagnosticGroups.CHECK_VARIABLES,
             CheckLevel.ERROR);
         options.setWarningLevel(DiagnosticGroups.CONSTANT_PROPERTY,
             CheckLevel.ERROR);
         options
             .setWarningLevel(DiagnosticGroups.DEPRECATED, CheckLevel.WARNING);
         options.setWarningLevel(DiagnosticGroups.EXTERNS_VALIDATION,
             CheckLevel.OFF);
         options.setWarningLevel(DiagnosticGroups.FILEOVERVIEW_JSDOC,
             CheckLevel.WARNING);
         options.setWarningLevel(DiagnosticGroups.INTERNET_EXPLORER_CHECKS,
             CheckLevel.WARNING);
         options.setWarningLevel(DiagnosticGroups.INVALID_CASTS,
             CheckLevel.ERROR);
         options.setWarningLevel(DiagnosticGroups.MISSING_PROPERTIES,
             CheckLevel.ERROR);
         options.setWarningLevel(DiagnosticGroups.NON_STANDARD_JSDOC,
             CheckLevel.WARNING);
         options.setWarningLevel(DiagnosticGroups.STRICT_MODULE_DEP_CHECK,
             CheckLevel.ERROR);
         options.setWarningLevel(DiagnosticGroups.TWEAKS, CheckLevel.WARNING);
         options.setWarningLevel(DiagnosticGroups.UNDEFINED_VARIABLES,
             CheckLevel.ERROR);
         options.setWarningLevel(DiagnosticGroups.UNKNOWN_DEFINES,
             CheckLevel.WARNING);
         options.setWarningLevel(DiagnosticGroups.VISIBILITY, CheckLevel.ERROR);
 
         options.tightenTypes = false;
         options.tracer = TracerMode.OFF;
 
         options.unaliasableGlobals = null;
 
         options.variableRenaming = VariableRenamingPolicy.LOCAL;
 
         return options;
     }
 
     /**
      * Write the compilation results to the target file.
      *
      * @param source
      *            The source.
      * @param dependencyManager
      *            The script dependency manager.
      * @throws MojoExecutionException
      *             When an exception occurs while writing the result.
      */
     private void writeScriptBundle(final String source,
         final DependencyManager dependencyManager)
         throws MojoExecutionException
     {
         this.scriptBundlesDirectory.mkdirs();
 
         final File output =
             new File(this.scriptBundlesDirectory, this.bundleFilename);
         try
         {
             final OutputStreamWriter out =
                 new OutputStreamWriter(new FileOutputStream(output),
                     this.encoding);
 
             // Add dependency annotations
             final Set<Dependency> dependencies =
                 dependencyManager.getExternalDependencies();
             final Set<String> provides =
                 dependencyManager.getProvidedDependencies();
             if (dependencies.size() > 0 || provides.size() > 0)
             {
                 out.append("/**\n");
                 for (final Dependency dependency : dependencies)
                 {
                     out.append(" * ");
                     out.append(dependency.toString());
                     out.append('\n');
                 }
                 for (final String provide : provides)
                 {
                     out.append(" * @provide ");
                     out.append(provide);
                     out.append('\n');
                 }
                 out.append(" */\n");
             }
 
             // Add the source code
             out.append(source);
 
             out.close();
         }
         catch (final IOException e)
         {
             throw new MojoExecutionException(e.toString(), e);
         }
     }
 
     /**
      * Writes the result of a single file.
      *
      * @param jsSourceFile
      *            The source file
      * @param compiledSource
      *            The compiled source
      * @param dependencies
      *            The dependencies of this source file.
      * @throws MojoExecutionException
      *             If result could not be written.
      */
 
     private void writeScript(final JSSourceFile jsSourceFile,
         final String compiledSource, final List<Dependency> dependencies)
         throws MojoExecutionException
     {
         final File output =
             new File(this.scriptsDirectory,
                 jsSourceFile.getOriginalPath());
         output.getParentFile().mkdirs();
 
         try
         {
             final OutputStreamWriter out =
                 new OutputStreamWriter(new FileOutputStream(output), "UTF-8");
 
             // Add dependency annotations
             if (dependencies.size() > 0)
             {
                 out.append("/**\n");
                 for (final Dependency dependency : dependencies)
                 {
                     out.append(" * ");
                     out.append(dependency.toString());
                     out.append('\n');
                 }
                 out.append(" */\n");
             }
 
             // Add the compiles source code
             out.append(compiledSource);
 
             out.close();
         }
         catch (final IOException e)
         {
             throw new MojoExecutionException(e.toString(), e);
         }
     }
 
     /**
      * Write the uncompiled source file.
      *
      * @param sources
      *            The sources.ls -la
      * @param dependencyManager
      *            The script dependency manager
      * @throws MojoExecutionException
      *             When an exception occurs while writing the sources.
      */
     private void writeScriptSourceBundle(final List<JSSourceFile> sources,
         final DependencyManager dependencyManager)
         throws MojoExecutionException
     {
         this.scriptSourceBundlesDirectory.mkdirs();
         final File output = new File(this.scriptSourceBundlesDirectory,
             this.bundleFilename);
 
         try
         {
             final OutputStreamWriter out =
                 new OutputStreamWriter(new FileOutputStream(output), "UTF-8");
             // out.append("\n/**\n");
             // out.append(" * @fileoverview\n");
             // out.append(" * @suppress {accessControls|checkRegExp|checkTypes|checkVars|deprecated|fileoverviewTags|invalidCasts|missingProperties|nonStandardJsDocs|strictModuleDepCheck|undefinedVars|unknownDefines|uselessCode|visibility}\n");
             // out.append(" */\n\n");
 
             // Add dependency annotations
             final Set<String> provides =
                 dependencyManager.getProvidedDependencies();
             final Set<Dependency> dependencies =
                 dependencyManager.getExternalDependencies();
             if (dependencies.size() > 0 || provides.size() > 0)
             {
                 out.append("/**\n");
                 for (final Dependency dependency : dependencies)
                 {
                     out.append(" * ");
                     out.append(dependency.toString());
                     out.append('\n');
                 }
                 for (final String provide : provides)
                 {
                     out.append(" * @provide ");
                     out.append(provide);
                     out.append('\n');
                 }
                 out.append(" */\n\n");
             }
 
             for (final JSSourceFile source : sources)
             {
                 final String filename = source.getOriginalPath();
 
                 // Add source to bundle file
                 out.append("// ===========================================================================\n");
                 out.append("// " + filename + "\n");
                 out.append("// ===========================================================================\n");
                 out.append("\n");
                 out.append(source
                     .getCode()
                     .replaceAll("(?m)^\\s*\\*\\s*@fileoverview", "")
                     .replaceAll(
                         "(?m)^\\s*\\*\\s*@(use|require)\\s+[a-zA-Z0-9\\/\\-\\.]+\\s*$[\n\r]*",
                         ""));
                 out.append("\n\n");
             }
 
             out.close();
         }
         catch (final IOException e)
         {
             throw new MojoExecutionException(e.toString(), e);
         }
     }
 }
