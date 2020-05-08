 package liebenberg.closure_utilities.build;
 
 import com.google.common.base.Function;
 import com.google.common.base.Preconditions;
 import com.google.common.collect.ImmutableList;
 import com.google.common.collect.Lists;
 import com.google.javascript.jscomp.*;
 import com.google.javascript.jscomp.Compiler;
 import liebenberg.closure_utilities.internal.DependencyException;
 import liebenberg.closure_utilities.internal.DependencyOptions;
 import liebenberg.closure_utilities.javascript.ClosureDependencyParser;
 import liebenberg.closure_utilities.render.DefinesFileRenderer;
 import liebenberg.closure_utilities.render.DependencyFileRenderer;
 import liebenberg.closure_utilities.render.RenderException;
 import liebenberg.closure_utilities.soy.SoyDelegateOptimizer;
 import liebenberg.closure_utilities.utilities.FS;
 import liebenberg.closure_utilities.utilities.Immuter;
 
 import javax.annotation.Nonnull;
 import javax.annotation.Nullable;
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 import java.util.Map;
 
 public class JsBuilder extends AbstractBuilder<JsOptions, JsResult> {
 
     private static final String BUILDER_NAME = "[JS]";
 
     public JsBuilder() {
         super(BUILDER_NAME);
     }
 
     private static class InternalData {
         private List<File> scriptFiles;
         private File baseFile;
         private File outputFile;
         private File dependencyFile;
         private File definesFile;
         private ArrayList<File> sourceFiles;
         private ArrayList<ClosureSourceFile> closureSourceFiles;
         private ArrayList<ClosureSourceFile> closureEntryFiles;
         private ImmutableList<ClosureSourceFile> resolvedSourceFiles;
         private ImmutableList<File> resolvedFiles;
         private Result compilerResult;
 
         @Nonnull
         public JsResult toResult() {
             return new JsResult(baseFile, dependencyFile, definesFile,
                     outputFile, scriptFiles);
         }
     }
 
     private final ClosureDependencyParser depParser =
             new ClosureDependencyParser();
 
     private final DependencyBuilder<ClosureSourceFile> depBuilder =
             new DependencyBuilder<>();
 
     private final DependencyFileRenderer depFileRenderer =
             new DependencyFileRenderer();
 
     private final DefinesFileRenderer definesFileRenderer =
             new DefinesFileRenderer();
 
     private static final String JS_EXT = "js";
 
     @Nonnull
     private ClosureSourceFile parseFile(@Nonnull File inputFile,
                                         @Nonnull InternalData internalData)
             throws IOException {
         ClosureSourceFile sourceFile = new ClosureSourceFile(inputFile);
         depParser.parse(sourceFile, FS.read(inputFile));
         if (sourceFile.getIsBaseFile()) {
             internalData.baseFile = inputFile;
         }
         return sourceFile;
     }
 
     private void findDependencyFiles(@Nonnull final JsOptions options,
                                      @Nonnull final InternalData internalData)
             throws IOException {
 
         if (options.getVerbose()) {
             log("finding dependency files");
         }
 
         final Collection<File> dirs = options.getSourceDirectories();
         final List<File> entryFiles = options.getEntryFiles();
         if (dirs != null && !dirs.isEmpty()) {
             final Collection<File> sourceFiles = FS.find(dirs, JS_EXT);
             if (options.getVerbose()) {
                 log("found", sourceFiles.size(), "javascript files in ",
                         dirs.size(), "directories");
             }
             internalData.closureSourceFiles = new ArrayList<>();
             internalData.closureEntryFiles = new ArrayList<>();
             for (File sourceFile : sourceFiles) {
                 ClosureSourceFile parsedFile = parseFile(sourceFile,
                         internalData);
                 internalData.closureSourceFiles.add(parsedFile);
                 // todo, find a better way.
                 if (entryFiles != null && entryFiles.contains(sourceFile)) {
                     internalData.closureEntryFiles.add(parsedFile);
                 }
             }
         }
     }
 
 
     private void buildDefinesFile(@Nonnull JsOptions options,
                                   @Nonnull InternalData internalData)
             throws RenderException, IOException {
 
         internalData.definesFile = options.getOutputDefinesFile();
         if (internalData.definesFile != null) {
             if (options.getVerbose()) {
                 log("building defines file", internalData.definesFile);
             }
 
             FS.write(internalData.definesFile, definesFileRenderer
                     .setMapValues(options.getGlobals())
                     .render());
         }
     }
 
     private void buildDependenciesFile(@Nonnull JsOptions options,
                                        @Nonnull InternalData internalData)
             throws RenderException, IOException {
         internalData.dependencyFile = options.getOutputDependencyFile();
         if (internalData.dependencyFile != null) {
             Preconditions.checkState(internalData.baseFile != null);
             Preconditions.checkState(internalData.closureSourceFiles != null);
             FS.write(internalData.dependencyFile, depFileRenderer
                     .setBasePath(internalData.baseFile.getAbsolutePath())
                     .setDependencies(internalData.closureSourceFiles)
                     .render());
         }
     }
 
     private void calculateDependencies(@Nonnull JsOptions options,
                                        @Nonnull InternalData internalData)
             throws DependencyException, IOException, BuildException {
         final List<String> entryPoints = options.getEntryPoints();
         final List<File> entryFiles = options.getEntryFiles();
         if (entryPoints != null || entryFiles != null) {
             final DependencyOptions<ClosureSourceFile>
                     depBuildOptions = new DependencyOptions<>();
             depBuildOptions.setEntryPoints(entryPoints);
             depBuildOptions.setEntryFiles(internalData.closureEntryFiles);
             depBuildOptions.setSourceFiles(internalData.closureSourceFiles);
             internalData.resolvedSourceFiles =
                     depBuilder.build(depBuildOptions);
             internalData.resolvedFiles =
                     Immuter.list(internalData.resolvedSourceFiles,
                             SourceFileBase.TO_FILE);
         }
 
         final List<File> scriptsFilesToCompile =
                 getScriptFilesToCompile(internalData);
         internalData.scriptFiles = scriptsFilesToCompile;
 
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
 
     private void setCompilerOptionsForDebug(@Nonnull final CompilerOptions o) {
         final CompilationLevel level = CompilationLevel.SIMPLE_OPTIMIZATIONS;
         level.setOptionsForCompilationLevel(o);
         level.setTypeBasedOptimizationOptions(o);
         o.setPrettyPrint(true);
     }
 
     @Nonnull
     private CompilerOptions getCompilerOptions(
             @Nonnull final JsOptions options,
             @Nullable final File sourceMap) {
 
         final CompilerOptions cOpts = new CompilerOptions();
 
         if (!options.getShouldDebug()) {
             if (options.getVerbose()) {
                 log("Setting compiler options for production!");
             }
             setCompilerOptionsForCompile(cOpts);
         } else {
             if (options.getVerbose()) {
                 log("Setting compiler options for debug!");
             }
             setCompilerOptionsForDebug(cOpts);
         }
 
         MessageBundle msgBundle = options.getMessageBundle();
         if (msgBundle != null) {
             if (options.getVerbose()) {
                 log("Setting message bundle.");
             }
             cOpts.setMessageBundle(msgBundle);
         }
 
         final Map<String, Object> globals = options.getGlobals();
         if (globals != null) {
             if (options.getVerbose()) {
                 log("Setting globals");
             }
             cOpts.setDefineReplacements(globals);
         }
 
         if (sourceMap != null) {
             if (options.getVerbose()) {
                 log("Setting sourceMap output path:", sourceMap.getPath());
             }
             cOpts.setSourceMapFormat(SourceMap.Format.V3);
             cOpts.setSourceMapDetailLevel(SourceMap.DetailLevel.ALL);
             cOpts.setSourceMapOutputPath(sourceMap.getPath());
         }
         return cOpts;
     }
 
 
     private List<SourceFile> getExternFiles() throws IOException {
         final List<SourceFile> externs = new ArrayList<>();
         externs.addAll(CommandLineRunner.getDefaultExterns());
         return externs;
     }
 
     private List<SourceFile> getCompilerSourceFiles(
             @Nonnull List<File> scriptFilesToCompile) {
         return Lists.transform(scriptFilesToCompile,
                 new Function<File, SourceFile>() {
                     @Nullable
                     @Override
                     public SourceFile apply(@Nullable File input) {
                         return SourceFile.fromFile(input);
                     }
                 });
     }
 
     private void addCustomBuildPasses(
             @Nonnull final Compiler compiler,
             @Nonnull final CompilerOptions compilerOptions) {
         SoyDelegateOptimizer.addToCompile(compiler, compilerOptions);
         // todo, add space here for user to specify custom passes.
     }
 
     private void compileScriptFile(@Nonnull final JsOptions options,
                                    @Nonnull final InternalData internalData)
             throws BuildException, IOException {
         final Compiler compiler = new Compiler();
         final CompilerOptions compilerOptions =
                 getCompilerOptions(options, null);
         final List<SourceFile> externs = getExternFiles();
         final List<SourceFile> sources =
                 getCompilerSourceFiles(internalData.scriptFiles);
         addCustomBuildPasses(compiler, compilerOptions);
         internalData.compilerResult =
                 compiler.compile(externs, sources, compilerOptions);
         if (internalData.compilerResult.success) {
             final String source = compiler.toSource();
             internalData.outputFile = options.getOutputFile();
             FS.write(internalData.outputFile, source);
         } else {
             throw new BuildException("Compilation Failure");
         }
     }
 
     @Nonnull
     private ImmutableList<File> getScriptFilesToCompile(
             @Nonnull InternalData internalData) {
         final ImmutableList.Builder<File> toCompileFiles =
                 new ImmutableList.Builder<File>();
         if (internalData.baseFile != null) {
             toCompileFiles.add(internalData.baseFile);
         }
 
         if (internalData.dependencyFile != null) {
             toCompileFiles.add(internalData.dependencyFile);
         }
 
         // add rename map
         // add defines.js
 
         if (internalData.resolvedFiles != null) {
             toCompileFiles.addAll(internalData.resolvedFiles);
         }
         return toCompileFiles.build();
     }
 
 
     private static final String UNSPECIFIED_OUTPUT_FILE =
             "Javascript output file not specified";
 
     private static final String UNSPECIFIED_ENTRY_POINTS =
             "Javascript entry points have not specified.";
 
     private static final String UNSPECIFIED_SOURCE_DIRECTORIES =
             "Javascript source directories have not specified.";
 
 
     @Nonnull
     @Override
     protected JsResult buildInternal(@Nonnull JsOptions options)
             throws Exception {
         final InternalData internalData = new InternalData();
         findDependencyFiles(options, internalData);

        if (internalData.baseFile == null) {
            throw new BuildException("Closure Library's base.js cannot be " +
                    "found. Check that the closure library directory has been" +
                    " added as a source directory.");
        }

         buildDependenciesFile(options, internalData);
         buildDefinesFile(options, internalData);
         calculateDependencies(options, internalData);
         if (options.getShouldCompile()) {
             compileScriptFile(options, internalData);
         }
         return internalData.toResult();
     }
 
 
     @Override
     public void checkOptions(@Nonnull JsOptions options)
             throws BuildOptionsException {
 
         final File outFile = options.getOutputFile();
         if (outFile == null) {
             throw new NullPointerException(UNSPECIFIED_OUTPUT_FILE);
         }
 
         final Collection<String> entryPoints = options.getEntryPoints();
         final Collection<File> entryFiles = options.getEntryFiles();
         if ((entryPoints == null || entryPoints.isEmpty()) && (
                 entryFiles == null || entryFiles.isEmpty())) {
             throw new BuildOptionsException(UNSPECIFIED_ENTRY_POINTS);
         }
 
         final Collection<File> sourceDirectories =
                 options.getSourceDirectories();
         if (sourceDirectories == null || sourceDirectories.isEmpty()) {
             throw new BuildOptionsException
                     (UNSPECIFIED_SOURCE_DIRECTORIES);
         }
     }
 }
