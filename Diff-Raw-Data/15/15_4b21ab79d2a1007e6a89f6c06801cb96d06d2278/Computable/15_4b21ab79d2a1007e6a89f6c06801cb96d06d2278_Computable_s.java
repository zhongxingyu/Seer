 package org.openjpa.ide.idea;
 
 import java.io.DataInput;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.io.StringWriter;
 import java.io.Writer;
 import java.lang.reflect.InvocationTargetException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.LinkedHashMap;
 import java.util.LinkedHashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import com.intellij.openapi.application.Application;
 import com.intellij.openapi.application.ApplicationManager;
 import com.intellij.openapi.compiler.ClassPostProcessingCompiler;
 import com.intellij.openapi.compiler.CompileContext;
 import com.intellij.openapi.compiler.CompileScope;
 import com.intellij.openapi.compiler.CompilerManager;
 import com.intellij.openapi.compiler.CompilerMessageCategory;
 import com.intellij.openapi.compiler.CompilerPaths;
 import com.intellij.openapi.compiler.FileProcessingCompiler;
 import com.intellij.openapi.compiler.TimestampValidityState;
 import com.intellij.openapi.compiler.ValidityState;
 import com.intellij.openapi.module.Module;
 import com.intellij.openapi.progress.ProgressIndicator;
 import com.intellij.openapi.project.Project;
 import com.intellij.openapi.vfs.VirtualFile;
 import com.intellij.psi.PsiClass;
 
 import org.jetbrains.annotations.NotNull;
 import org.jetbrains.annotations.Nullable;
 import org.openjpa.ide.idea.integration.EnhancerProxy;
 import org.openjpa.ide.idea.integration.EnhancerSupport;
 
 /**
  * Enhances class files with xml- or annotation based metadata in
  * all build-cycle affected project modules where xml metadata or
  * annotated classes could be found.<br>
  * <br>
  * Activated enhancer integration dependencies must be in the project module's classpath!<br>
  * If not, although activated, the module will be ignored and a warning will be logged (Idea messages).<br>
  * <br>
  * This implementation always tries to enhance all files in a module at once, hence a failing
  * class can prevent all others (also whole modules) from being processed.<br>
  * <br>
  * Failure stacktraces are transformed into strings and written to the idea messages output.
  */
 class Computable implements ClassPostProcessingCompiler {
 
     private static final FileProcessingCompiler.ProcessingItem[] EMPTY_PROCESSING_ITEMS = new FileProcessingCompiler.ProcessingItem[0];
 
     //
     // Members
     //
 
     /**
      * Current project
      */
     private final Project project;
 
     /**
      * Plugin shared configuration
      */
     private final State state;
 
     //
     // Constructor
     //
 
     Computable(final Project project, final State state) {
         this.project = project;
         this.state = state;
     }
 
     //
     // ClassPostProcessingCompiler interface implementation
     //
 
     @NotNull
     @Override
     public FileProcessingCompiler.ProcessingItem[] getProcessingItems(final CompileContext compileContext) {
         final Set<String> enabledModules = this.state.getEnabledModules();
         if (this.state.isEnhancerEnabled() && enabledModules != null && !enabledModules.isEmpty()) {
             // get metadata files of affected modules
             final Map<Module, List<VirtualMetadataFile>> moduleBasedMetadataFiles =
                     this.getMetadataFiles(compileContext.getCompileScope());
             /*
                 //Commented out: debug output of found metadata files and according modules
                 for (final Module module : moduleBasedMetadataFiles.keySet()) {
                     ctx.addMessage(CompilerMessageCategory.WARNING, "+++ list1 module: " + module.getName(), null, -1, -1);
                     for (final VirtualFile vf : moduleBasedMetadataFiles.get(module)) {
                         ctx.addMessage(CompilerMessageCategory.WARNING, "----- list1 file: " + vf.getPath(), null, -1, -1);
                     }
                 }
             */
 
             // get annotated class files of affected modules
             final Map<Module, List<VirtualMetadataFile>> moduleBasedAnnotatedClasses =
                     this.getAnnotatedClassFiles(compileContext.getCompileScope());
             /*
                 //Commented out: debug output of found annotated class files and according modules
             for (final Module module : moduleBasedAnnotatedClasses.keySet()) {
                 ctx.addMessage(CompilerMessageCategory.WARNING, "+++ list2 module: " + module.getName(), null, -1, -1);
                 for (final VirtualFile vf : moduleBasedAnnotatedClasses.get(module)) {
                     ctx.addMessage(CompilerMessageCategory.WARNING, "----- list2 file: " + vf.getPath(), null, -1, -1);
                 }
             }*/
 
             final Collection<FileProcessingCompiler.ProcessingItem> processingItems =
                     new LinkedHashSet<FileProcessingCompiler.ProcessingItem>();
             for (final List<VirtualMetadataFile> metadataFileList : moduleBasedMetadataFiles.values()) {
                 for (final VirtualMetadataFile virtualMetadataFile : metadataFileList) {
                     final Collection<EnhancerItem> enhancerItems = virtualMetadataFile.toEnhancerItems();
                     for (final EnhancerItem enhancerItem : enhancerItems) {
                         processingItems.add(enhancerItem);
                     }
                 }
             }
             for (final List<VirtualMetadataFile> annotatedClassesFileList : moduleBasedAnnotatedClasses.values()) {
                 for (final VirtualMetadataFile virtualMetadataFile : annotatedClassesFileList) {
                     final Collection<EnhancerItem> enhancerItems = virtualMetadataFile.toEnhancerItems();
                     for (final EnhancerItem enhancerItem : enhancerItems) {
                         processingItems.add(enhancerItem);
                     }
                 }
             }
 
             if (processingItems.isEmpty()) {
                 this.logMessage(compileContext,
                         CompilerMessageCategory.WARNING,
                         "Enhancer: no metadata- or annotated class-files found");
             }
 
             return processingItems.toArray(new FileProcessingCompiler.ProcessingItem[processingItems.size()]);
         } else {
             return EMPTY_PROCESSING_ITEMS;
         }
     }
 
     @Override
     public FileProcessingCompiler.ProcessingItem[] process(final CompileContext ctx,
                                                            final FileProcessingCompiler.ProcessingItem[] processingItems) {
 
         org.apache.log4j.BasicConfigurator.configure();
         FileProcessingCompiler.ProcessingItem[] ret = EMPTY_PROCESSING_ITEMS;
 
         // shortcut if disabled
         final Set<String> enabledModules = this.state.getEnabledModules();
         if (!this.state.isEnhancerEnabled() || enabledModules == null || enabledModules.isEmpty()) {
 
         } else {
 
             // just to be sure: backup of classloader
             final ClassLoader previousCL = Thread.currentThread().getContextClassLoader();
 
             // for displaying progress messages
             final ProgressIndicator progressIndicator = ctx.getProgressIndicator();
 
             try {
                 // GUI State display init
                 progressIndicator.pushState();
                 progressIndicator.setText("OpenJpa Enhancer running");
 
                 final LinkedHashMap<Module, List<VirtualMetadataFile>> moduleBasedMetadataFiles =
                         new LinkedHashMap<Module, List<VirtualMetadataFile>>();
 
                 final LinkedHashMap<Module, List<VirtualMetadataFile>> moduleBasedAnnotatedClasses =
                         new LinkedHashMap<Module, List<VirtualMetadataFile>>();
 
                 for (final FileProcessingCompiler.ProcessingItem processingItem : processingItems) {
                     final EnhancerItem enhancerItem = (EnhancerItem) processingItem;
                     final VirtualMetadataFile virtualMetadata = enhancerItem.getVirtualMetadata();
                     final Module module = virtualMetadata.getModule();
                     if (virtualMetadata.isAnnotationBasedOnly()) {
                         List<VirtualMetadataFile> annotatedClassesList = moduleBasedAnnotatedClasses.get(module);
                         if (annotatedClassesList == null) {
                             annotatedClassesList = new ArrayList<VirtualMetadataFile>();
                             moduleBasedAnnotatedClasses.put(module, annotatedClassesList);
                         }
                         annotatedClassesList.add(virtualMetadata);
                     } else {
                         List<VirtualMetadataFile> metadataFileList = moduleBasedMetadataFiles.get(module);
                         if (metadataFileList == null) {
                             metadataFileList = new ArrayList<VirtualMetadataFile>();
                             moduleBasedMetadataFiles.put(module, metadataFileList);
                         }
                         if (!metadataFileList.contains(virtualMetadata)) {
                             metadataFileList.add(virtualMetadata);
                         }
                     }
                 }
 
                 // detect all modules affected
                 final List<Module> affectedModules = getAffectedModules(ctx, moduleBasedMetadataFiles, moduleBasedAnnotatedClasses);
 
 
                 // no metadata or annotated classes -> no enhancement
                 if (!affectedModules.isEmpty()) {
 
                     // start enhancer per module
                     final int count = enhanceInModules(ctx, affectedModules, moduleBasedMetadataFiles, moduleBasedAnnotatedClasses);
                     // success message
                     this.logMessage(ctx,
                             CompilerMessageCategory.INFORMATION,
                             "Enhancer: Successfully enhanced " + count + " classes");
                 } else {
                     this.logMessage(ctx,
                             CompilerMessageCategory.WARNING,
                             "Enhancer: no Hibernate/JPA metadata or annotated class files found");
                 }
 
                 ret = processingItems;
 
             } catch (Throwable t) {
                 // writer for stacktrace printing
                 final Writer writer = new StringWriter();
                 // transform stacktrace to string
                 final PrintWriter printWriter = new PrintWriter(writer);
                 try {
                     t.printStackTrace(printWriter);
                 } finally {
                     printWriter.close();
                 }
                 // write stacktrace string to messages
                 this.logMessage(ctx,
                         CompilerMessageCategory.ERROR,
                         "An unexpected error occurred in the OpenJpa plugin. Stacktrace: " + "\n\n" + writer);
             } finally {
                 Thread.currentThread().setContextClassLoader(previousCL);
                 progressIndicator.popState();
             }
         }
 
         return ret;
     }
 
     @NotNull
     @Override
     public String getDescription() {
         return "OpenJpa Enhancer";
     }
 
     @Override
     public boolean validateConfiguration(final CompileScope compileScope) {
         return true;
     }
 
     @Override
     public ValidityState createValidityState(final DataInput dataInput) throws IOException {
         return TimestampValidityState.load(dataInput);
     }
 
     //
     // Helper methods
     //
 
     @SuppressWarnings("FeatureEnvy")
     private int enhanceInModules(final CompileContext ctx,
                                  final Iterable<Module> affectedModules,
                                  final LinkedHashMap<Module, List<VirtualMetadataFile>> moduleBasedMetadataFiles,
                                  final LinkedHashMap<Module, List<VirtualMetadataFile>> moduleBasedAnnotatedClasses)
             throws IOException, IllegalAccessException,
             InstantiationException, InvocationTargetException, NoSuchFieldException {
 
         int count = 0;
         for (final Module module : affectedModules) {
             // exclude disabled modules
             final Set<String> enabledModules = this.state.getEnabledModules();
             if (enabledModules != null && enabledModules.contains(module.getName())) {
 
                 // get modules output folder
                 final VirtualFile outputDirectory = ctx.getModuleOutputDirectory(module);
 
                 // only enhance in modules that have an output folder
                 if (outputDirectory == null) {
                     // display warning if module has no output folder
                     this.logMessage(ctx,
                             CompilerMessageCategory.WARNING,
                             "Enhancer: no output directory for module: " + module.getName());
 
                 } else {
                     final ProgressIndicator progressIndicator = ctx.getProgressIndicator();
                     final EnhancerSupport enhancerSupport = this.state.getEnhancerSupport();
 
                     // update progress text
                     progressIndicator.setText(enhancerSupport.getName() + " Enhancer enhancing in " + module.getName());
                     // metadata files for module
                     final List<VirtualMetadataFile> metadataFiles = moduleBasedMetadataFiles.get(module);
                     // metadata files for module
                     final List<VirtualMetadataFile> annotatedClassFiles = moduleBasedAnnotatedClasses.get(module);
 
                     try {
                         // do class enhancement in module
                         count += enhancePerModule(enhancerSupport,
                                 this.state,
                                 ctx,
                                 module,
                                 outputDirectory,
                                 metadataFiles,
                                 annotatedClassFiles);
 
                     } catch (ClassNotFoundException ignored) {
                         this.logMessage(ctx,
                                 CompilerMessageCategory.WARNING,
                                 "Enhancer: enhancer not found in classpath for module: " + module.getName());
                     } catch (NoSuchMethodException ignored) {
                         this.logMessage(ctx,
                                 CompilerMessageCategory.ERROR,
                                 "Enhancer: enhancer mehtod not found for module: " + module.getName());
                     }
                 }
             }
         }
         return count;
     }
 
     @SuppressWarnings("FeatureEnvy")
     private static int enhancePerModule(final EnhancerSupport enhancerSupport,
                                         final State state,
                                         final CompileContext compileContext,
                                         final Module module,
                                         final VirtualFile outputDirectory,
                                         final Collection<VirtualMetadataFile> metadataFiles,
                                         final Collection<VirtualMetadataFile> annotatedClassFiles)
             throws ClassNotFoundException,
             IllegalAccessException,
             InstantiationException,
             InvocationTargetException,
             NoSuchMethodException, NoSuchFieldException {
 
         //
         // what to enhance
 
         // metadata based classes
         final boolean metadataBased = metadataFiles != null && !metadataFiles.isEmpty();
         // annotation based classes
         final boolean annotationBased = annotatedClassFiles != null && !annotatedClassFiles.isEmpty();
 
         //
         // only enhance if metadata or annotation based class files present
 
         final boolean doEnhance = metadataBased || annotationBased;
 
         //
         // create enhancer instance
 
         final EnhancerProxy enhancer;
         if (doEnhance) {
 
             //final JDOEnhancer enhancer = JDOHelper.getEnhancer(); // does not work due to classloader problems
             //enhancer = new OpenJpaEnhancerProxy(api, compileContext, module);
             enhancer = enhancerSupport.newEnhancerProxy(state.getApi(), compileContext, module, null);
             enhancer.setAddDefaultConstructor(state.isAddDefaultConstructor());
             enhancer.setEnforcePropertyRestrictions(state.isEnforcePropertyRestrictions());
 
         } else {
             enhancer = null;
         }
 
         //
         // add metadata and classes
 
         // add metadata based classes to enhancer list
         if (doEnhance && metadataBased) {
 
             // iterate modules and enhance classes in corresponding output folders
             for (final VirtualMetadataFile metadataFile : metadataFiles) {
 
                 //ctx.addMessage(CompilerMessageCategory.INFORMATION, "OpenJpa Enhancer: found metadata file: " + metadataFile.getPath(), null, -1, -1);
                 // get metadata file url
                 final VirtualFile metadataVirtualFile = metadataFile.getFile();
                 final String metadataFilePath = metadataVirtualFile.getPath();
                 // add metadata file url to enhancer
                 enhancer.addMetadataFiles(metadataFilePath);
 
                 // parse package and class names
                 final Collection<String> classNames = metadataFile.getClassNames();
 
                 // add xml metadata based classes
                 for (final String className : classNames) {
                     final String classNameAsPath = IdeaProjectUtils.packageToPath(className);
                     final String fullPath = outputDirectory.getPath() + '/' + classNameAsPath + ".class";
 
                     //ctx.addMessage(CompilerMessageCategory.INFORMATION, "OpenJpa Enhancer: found class: " + fullPath, null, -1, -1);
                     enhancer.addClasses(fullPath);
                 }
             }
         }
 
         // add annotated classes to enhancer list
         if (doEnhance && annotationBased) {
 
             for (final VirtualMetadataFile annotatedClassFile : annotatedClassFiles) {
                 //compileContext.addMessage(CompilerMessageCategory.INFORMATION,
                 //                          "OpenJpa Enhancer: found class: " + annotatedClassFile.getPath(), null, -1, -1);
                 final VirtualFile annotatedClassVirtualFile = annotatedClassFile.getFile();
                 enhancer.addClasses(annotatedClassVirtualFile.getPath());
             }
         }
 
         //
         // finally enhance classes
 
         // count nr of enhanced classes
         final int enhancedCount;
 
         if (doEnhance) {
             // finally enhance all found classes in module
             enhancedCount = enhancer.enhance();
         } else {
             // nothing to enhance
             enhancedCount = 0;
         }
 
         return enhancedCount;
     }
 
     //
     // Utility methods
     //
 
     /**
      * Retrieve annotated class files.
      *
      * @param compileScope compile scope to use (null for default - can lead to invalid file list due to refactoring)
      * @return .
      */
     // TODO: cleanup, as this seems to be very hacky
     @SuppressWarnings("FeatureEnvy")
     Map<Module, List<VirtualMetadataFile>> getAnnotatedClassFiles(@Nullable final CompileScope compileScope) {
         final LinkedHashMap<Module, List<VirtualMetadataFile>> moduleBasedFiles = new LinkedHashMap<Module, List<VirtualMetadataFile>>();
 
         final Application application = ApplicationManager.getApplication();
         application.runReadAction(new Runnable() {
 
             @Override
             public void run() {
                 final CompileScope projectCompileScope = compileScope == null
                         ? CompilerManager.getInstance(Computable.this.project).createProjectCompileScope(Computable.this.project)
                         : compileScope;
 
                 final Set<String> enabledFiles = Computable.this.state.getEnabledFiles();
                 for (final Module module : projectCompileScope.getAffectedModules()) {
                     if (Computable.this.state.getEnabledModules() != null && Computable.this.state.getEnabledModules()
                             .contains(module.getName())) {
 
 
                         final List<PsiClass> annotatedClasses = IdeaProjectUtils.findPersistenceAnnotatedClasses(
                                 Computable.this.state.getEnhancerSupport(), module);
                         final Collection<VirtualFile> outputDirectories = new ArrayList<VirtualFile>(2);
                         outputDirectories.add(CompilerPaths.getModuleOutputDirectory(module, false));
                         if (Computable.this.state.isIncludeTestClasses()) {
                             outputDirectories.add(CompilerPaths.getModuleOutputDirectory(module, true));
                         }
                         //int count = 0;
 
                         for (final VirtualFile outputDirectory : outputDirectories) {
                             // convert to class files in output directory and add to map
                             if (!annotatedClasses.isEmpty()) {
                                 if (outputDirectory == null) {
 
                                 } else {
                                     final List<VirtualMetadataFile> moduleFiles = new LinkedList<VirtualMetadataFile>();
                                     // convert psi classes to class files in output path
                                     for (final PsiClass annotatedClass : annotatedClasses) {
                                         final String pcClassName = annotatedClass.getQualifiedName();
                                         // skip disabled files
                                        if (!enabledFiles.contains(pcClassName)) {
                                             continue;
                                         }
                                         // convert to path
                                         final String pcClassPath = IdeaProjectUtils.packageToPath(pcClassName) + ".class";
                                         // find file in output path
                                         final VirtualFile pcClassFile = outputDirectory.findFileByRelativePath(pcClassPath);
                                         if (pcClassFile != null && pcClassFile.exists()) {
                                             moduleFiles
                                                     .add(new VirtualMetadataFile(module, true, pcClassFile,
                                                             Collections.singletonList(pcClassName),
                                                             Collections.singletonList(pcClassFile)));
                                         }
                                     }
                                     if (!moduleFiles.isEmpty()) {
                                         @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
                                         // everything is fine here
                                         final List<VirtualMetadataFile> storedModuleFiles = moduleBasedFiles.get(module);
                                         // if collection already exists, just add content
                                         if (storedModuleFiles == null) {
                                             moduleBasedFiles.put(module, moduleFiles);
                                         } else {
                                             storedModuleFiles.addAll(moduleFiles);
                                         }
                                     }
                                 }
                             }
                             //++count;
                         }
                     }
                 }
             }
         });
 
         return moduleBasedFiles;
     }
 
     /**
      * Retrieve metadata files.
      *
      * @param compileScope compile scope to use (null for default - can lead to invalid file list due to refactoring)
      * @return .
      */
     // TODO: cleanup, as this seems to be very hacky
     @SuppressWarnings("FeatureEnvy")
     Map<Module, List<VirtualMetadataFile>> getMetadataFiles(@Nullable final CompileScope compileScope) {
         final Set<String> extensions;
         if (this.state.getMetaDataExtensions() == null || this.state.getMetaDataExtensions().isEmpty()) {
             extensions = Collections.emptySet(); // State.DEFAULT_METADATA_EXTENSIONS; // no extensions provided -> disable search
         } else {
             extensions = this.state.getMetaDataExtensions();
         }
 
         final CompileScope projectCompileScope = compileScope == null
                 ? CompilerManager.getInstance(this.project).createProjectCompileScope(this.project)
                 : compileScope;
 
         final Map<Module, List<VirtualMetadataFile>> metadataFiles = new LinkedHashMap<Module, List<VirtualMetadataFile>>();
 
         final Module[] affectedModules = projectCompileScope.getAffectedModules();
 
         final Application application = ApplicationManager.getApplication();
         application.runReadAction(new Runnable() {
             @Override
             public void run() {
                 if (affectedModules.length > 0) {
                     //final IdeaProjectUtils.NoDirectoryErrorHandler errorHandler = new Computable.NoDirectoryErrorHandler(this.ctx);
 
                     for (final Module module : affectedModules) {
                         if (Computable.this.state.getEnabledModules() != null && Computable.this.state.getEnabledModules()
                                 .contains(module.getName())) {
 
                             final Collection<VirtualFile> outputDirectories = new ArrayList<VirtualFile>(2);
                             outputDirectories.add(CompilerPaths.getModuleOutputDirectory(module, false));
                             if (Computable.this.state.isIncludeTestClasses()) {
                                 outputDirectories.add(CompilerPaths.getModuleOutputDirectory(module, true));
                             }
                             //int count = 0;
                             for (final VirtualFile outputDirectory : outputDirectories) {
 
                                 if (outputDirectory == null) {
                                 } else {
 
                                     final List<VirtualMetadataFile> moduleFiles = new LinkedList<VirtualMetadataFile>();
                                     for (final String extension : extensions) {
                                         final List<VirtualFile> metadataFilesPerExtension =
                                                 IdeaProjectUtils.findFilesByExtension(outputDirectory, extension);
 
                                         // remove non-parseable files
                                         for (final VirtualFile vf : metadataFilesPerExtension) {
                                             final Set<String> classNames;
                                             try {
                                                 classNames = MetadataParser.parseQualifiedClassNames(vf);
                                             } catch (Exception e) {
                                                 throw new IllegalArgumentException("parsing metadata error", e);
                                             }
                                             if (classNames != null && !classNames.isEmpty()) {
                                                 final List<VirtualFile> classFiles = new ArrayList<VirtualFile>(classNames.size());
                                                 for (final String className : classNames) {
                                                     final String classNameAsPath = IdeaProjectUtils.packageToPath(className);
                                                     final VirtualFile classFile = outputDirectory.findFileByRelativePath(classNameAsPath + ".class");
                                                     classFiles.add(classFile);
                                                 }
 
                                                 moduleFiles.add(new VirtualMetadataFile(module, false, vf, classNames, classFiles));
                                             }
                                         }
                                     }
                                     if (!moduleFiles.isEmpty()) {
 
                                         metadataFiles.put(module, moduleFiles);
                                     }
                                 }
                             }
                         }
                     }
                 }
             }
         });
         return metadataFiles;
     }
 
     static List<Module> getAffectedModules(final CompileContext ctx,
                                            final Map<Module, List<VirtualMetadataFile>> moduleBasedMetadataFiles,
                                            final Map<Module, List<VirtualMetadataFile>> moduleBasedAnnotatedClasses) {
         // list of affected modules
         final List<Module> affectedModules = new ArrayList<Module>();
 
         // combine affected module lists (preserving order)
         final CompileScope compileScope = ctx.getCompileScope();
         final Module[] cSAffectedModules = compileScope.getAffectedModules();
         if (cSAffectedModules.length > 0) {
             for (final Module cSAffectedModule : cSAffectedModules) {
                 if (moduleBasedMetadataFiles.containsKey(cSAffectedModule) || moduleBasedAnnotatedClasses.containsKey(cSAffectedModule)) {
 
                     affectedModules.add(cSAffectedModule);
                 }
             }
         }
         return affectedModules;
     }
 
     @SuppressWarnings("MagicCharacter")
     private void logMessage(final CompileContext ctx, final CompilerMessageCategory cat, final String msg) {
         final EnhancerSupport enhancerSupport = this.state.getEnhancerSupport();
         ctx.addMessage(cat, enhancerSupport.getName() + ' ' + msg, null, -1, -1);
     }
 
 }
