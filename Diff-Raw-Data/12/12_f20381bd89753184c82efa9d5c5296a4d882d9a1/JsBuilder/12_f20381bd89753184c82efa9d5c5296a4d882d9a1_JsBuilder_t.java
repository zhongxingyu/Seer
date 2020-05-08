 package org.stefanl.closure_utilities.javascript;
 
 
 import com.google.common.base.Function;
 import com.google.common.collect.ImmutableList;
 import com.google.common.collect.Lists;
 import com.google.javascript.jscomp.*;
 import com.google.javascript.jscomp.Compiler;
 import org.stefanl.closure_utilities.internal.*;
 import org.stefanl.closure_utilities.render.DependencyFileRenderer;
 import org.stefanl.closure_utilities.render.RenderException;
 import org.stefanl.closure_utilities.soy.SoyDelegateOptimizer;
 import org.stefanl.closure_utilities.utilities.FS;
 
 import javax.annotation.Nonnull;
 import javax.annotation.Nullable;
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.Reader;
 import java.util.*;
 
 /**
  * The javascript build builds dependencies and compiles the JavaScript aspect
  * of a closure project.
  */
 public class JsBuilder
         extends AbstractBuilder<JsBuildOptions>
         implements BuilderInterface {
 
     private final ClosureDependencyParser dependencyParser =
             new ClosureDependencyParser();
 
     private final DependencyBuilder<ClosureSourceFile> dependencyBuilder =
             new DependencyBuilder<>();
 
     private static final String JS_EXT = "js";
 
     private File baseFile;
 
     private List<File> sourceFiles;
 
     private List<ClosureSourceFile> closureSourceFiles;
 
     @Nonnull
     private ClosureSourceFile parseFile(@Nonnull File inputFile)
             throws IOException {
         ClosureSourceFile sourceFile = new ClosureSourceFile(inputFile);
         dependencyParser.parse(sourceFile, FS.read(inputFile));
         if (sourceFile.getIsBaseFile()) {
             baseFile = inputFile;
         }
         return sourceFile;
     }
 
     public void findDependencyFiles() throws IOException {
         final Collection<File> sourceDirectories =
                 buildOptions.getSourceDirectories();
         if (sourceDirectories != null) {
             final Collection<File> sourceFiles =
                     FS.find(sourceDirectories, JS_EXT);
             closureSourceFiles = new ArrayList<>();
             for (File sourceFile : sourceFiles) {
                 closureSourceFiles.add(parseFile(sourceFile));
             }
         }
     }
 
     private final DependencyFileRenderer dependencyFileRenderer =
             new DependencyFileRenderer();
 
 
     public void buildDependenciesFile() throws RenderException, IOException {
         File dependencyFile = buildOptions.getOutputDependencyFile();
         if (dependencyFile != null) {
             FS.write(dependencyFile, dependencyFileRenderer
                     .setBasePath(baseFile.getAbsolutePath())
                     .setDependencies(closureSourceFiles)
                     .render());
         }
     }
 
     public void calculateDependencies()
             throws DependencyException, IOException, BuildException {
         final List<String> entryPoints = buildOptions.getEntryPoints();
 
         if (entryPoints != null) {
             final DependencyBuildOptions<ClosureSourceFile>
                     depBuildOptions =
                     new DependencyBuildOptions<>();
             depBuildOptions.setEntryPoints(buildOptions.getEntryPoints());
             depBuildOptions.setSourceFiles(closureSourceFiles);
             dependencyBuilder.setBuildOptions(depBuildOptions);
             dependencyBuilder.build();
         }
         sourceFiles = dependencyBuilder.getResolvedFiles();
     }
 
    private void setCompilerOptionsForCompile(
            @Nonnull final CompilerOptions o) {
         final CompilationLevel level =
                 CompilationLevel.ADVANCED_OPTIMIZATIONS;
         final CheckLevel err = CheckLevel.ERROR, off = CheckLevel.OFF;
         level.setOptionsForCompilationLevel(o);
         level.setTypeBasedOptimizationOptions(o);
         o.setAggressiveVarCheck(err);
         o.setBrokenClosureRequiresLevel(err);
         o.setCheckGlobalNamesLevel(err);
         o.setCheckGlobalThisLevel(err);
         o.setCheckMissingReturn(err);
         o.setCheckProvides(err);
         o.setCheckRequires(err);
         o.setWarningLevel(DiagnosticGroups.ACCESS_CONTROLS, err);
         o.setWarningLevel(DiagnosticGroups.AMBIGUOUS_FUNCTION_DECL, err);
         o.setWarningLevel(DiagnosticGroups.DEBUGGER_STATEMENT_PRESENT, err);
         o.setWarningLevel(DiagnosticGroups.CHECK_REGEXP, err);
         o.setWarningLevel(DiagnosticGroups.CHECK_VARIABLES, err);
         o.setWarningLevel(DiagnosticGroups.CONST, err);
         o.setWarningLevel(DiagnosticGroups.CONSTANT_PROPERTY, err);
         o.setWarningLevel(DiagnosticGroups.DEPRECATED, err);
         o.setWarningLevel(DiagnosticGroups.DUPLICATE_MESSAGE, err);
         o.setWarningLevel(DiagnosticGroups.DUPLICATE_VARS, err);
         o.setWarningLevel(DiagnosticGroups.ES5_STRICT, err);
         o.setWarningLevel(DiagnosticGroups.EXTERNS_VALIDATION, err);
         o.setWarningLevel(DiagnosticGroups.UNDEFINED_NAMES, err);
         o.setWarningLevel(DiagnosticGroups.UNDEFINED_VARIABLES, err);
         o.setWarningLevel(DiagnosticGroups.TYPE_INVALIDATION, err);
         o.setCheckEventfulObjectDisposalPolicy(
                 CheckEventfulObjectDisposal.DisposalCheckingPolicy.AGGRESSIVE);
         // we declare many unkown defines.
         o.setWarningLevel(DiagnosticGroups.UNKNOWN_DEFINES, off);
         o.setCheckTypes(true);
         o.setOptimizeArgumentsArray(true);
         o.setOptimizeCalls(true);
         o.setOptimizeParameters(true);
         o.setOptimizeReturns(true);
         o.setCheckSymbols(true);
         o.setAggressiveRenaming(true);
     }
 
     public void setCompilerOptionsForDebug(@Nonnull final CompilerOptions o) {
         final CompilationLevel level = CompilationLevel.SIMPLE_OPTIMIZATIONS;
         level.setOptionsForCompilationLevel(o);
         level.setTypeBasedOptimizationOptions(o);
         o.setPrettyPrint(true);
     }
 
     @Nonnull
    private CompilerOptions getCompilerOptions(
            @Nullable final File sourceMap) {

         final CompilerOptions cOpts = new CompilerOptions();
 
         if (!buildOptions.getShouldDebug()) {
            System.out.println("Setting compiler options for production!");
             setCompilerOptionsForCompile(cOpts);
         } else {
            System.out.println("Setting compiler options for debug!");
             setCompilerOptionsForDebug(cOpts);
         }
 
         final Map<String, Object> globals = buildOptions.getGlobals();
         if (globals != null) {
             cOpts.setDefineReplacements(globals);
         }
 
         if (sourceMap != null) {
             cOpts.setSourceMapFormat(SourceMap.Format.V3);
             cOpts.setSourceMapDetailLevel(SourceMap.DetailLevel.ALL);
             cOpts.setSourceMapOutputPath(sourceMap.getPath());
         }
         return cOpts;
     }
 
     private Result compilerResult;
 
     private File outputFile;
 
     public File getOutputFile() {
         return outputFile;
     }
 
     public List<SourceFile> getExternFiles() throws IOException {
         final List<SourceFile> externs = new ArrayList<>();
         externs.addAll(CommandLineRunner.getDefaultExterns());
         return externs;
     }
 
     public List<SourceFile> getCompilerSourceFiles() {
         return Lists.transform(getScriptsFilesToCompile(),
                 new Function<File, SourceFile>() {
                     @Nullable
                     @Override
                     public SourceFile apply(@Nullable File input) {
                         return SourceFile.fromFile(input);
                     }
                 });
     }
 
     public void addCustomBuildPasses(@Nonnull Compiler compiler,
                                      @Nonnull CompilerOptions compilerOptions) {
 
         SoyDelegateOptimizer.addToCompile(compiler, compilerOptions);
 
         // todo, add space here for user to specify custom passes.
 
     }
 
     public void compileScriptFile() throws BuildException, IOException {
         final Compiler compiler = new Compiler();
         final CompilerOptions options = getCompilerOptions(null);
         final List<SourceFile> externs = getExternFiles();
         final List<SourceFile> sources = getCompilerSourceFiles();
 
         addCustomBuildPasses(compiler, options);
 
         compilerResult = compiler.compile(externs, sources, options);
         if (compilerResult.success) {
             final String source = compiler.toSource();
             outputFile = buildOptions.getOutputFile();
             FS.write(outputFile, source);
         } else {
             throw new BuildException("Compilation Failure");
         }
     }
 
     @Override
     public void buildInternal() throws Exception {
         findDependencyFiles();
         buildDependenciesFile();
         calculateDependencies();
         if (buildOptions.getShouldCompile()) {
             compileScriptFile();
         }
     }
 
     @Override
     public void reset() {
         super.reset();
         dependencyBuilder.reset();
         dependencyFileRenderer.reset();
         sourceFiles = null;
         compilerResult = null;
         outputFile = null;
     }
 
     @Nullable
     public List<File> getSourceFiles() {
         return sourceFiles;
     }
 
     @Nonnull
     public ImmutableList<File> getScriptsFilesToCompile() {
         final ImmutableList.Builder<File> toCompileFiles =
                 new ImmutableList.Builder<File>();
 
         toCompileFiles.add(baseFile);
 
         File outputDepsFile = buildOptions.getOutputDependencyFile();
         if (outputDepsFile != null) {
             toCompileFiles.add(outputDepsFile);
         }
         // add rename map
         // add defines.js
         toCompileFiles.addAll(sourceFiles);
         return toCompileFiles.build();
     }
 
     @Nullable
     public List<ClosureSourceFile> getClosureSourceFiles() {
         return closureSourceFiles;
     }
 
     public static final String UNSPECIFIED_OUTPUT_FILE =
             "Javascript output file not specified";
 
     public static final String UNSPECIFIED_ENTRY_POINTS =
             "Javascript entry points have not specified.";
 
     public static final String UNSPECIFIED_SOURCE_DIRECTORIES =
             "Javascript source directories have not specified.";
 
     @Override
     public void checkOptions() throws BuildOptionsException {
         super.checkOptions();
 
         final File outFile = buildOptions.getOutputFile();
         if (outFile == null) {
             throw new NullPointerException(UNSPECIFIED_OUTPUT_FILE);
         }
 
         final Collection<String> entryPoints = buildOptions.getEntryPoints();
         if (entryPoints == null || entryPoints.isEmpty()) {
             throw new BuildOptionsException(UNSPECIFIED_ENTRY_POINTS);
         }
 
         final Collection<File> sourceDirectories =
                 buildOptions.getSourceDirectories();
         if (sourceDirectories == null || sourceDirectories.isEmpty()) {
             throw new BuildOptionsException
                     (UNSPECIFIED_SOURCE_DIRECTORIES);
         }
     }
 }
