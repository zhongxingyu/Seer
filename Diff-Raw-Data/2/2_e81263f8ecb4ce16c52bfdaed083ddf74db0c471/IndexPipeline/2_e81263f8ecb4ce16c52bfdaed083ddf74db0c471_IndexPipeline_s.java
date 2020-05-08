 package com.codingstory.polaris.pipeline;
 
 import com.codingstory.polaris.IdGenerator;
 import com.codingstory.polaris.SimpleIdGenerator;
 import com.codingstory.polaris.indexing.DirectoryTranverser;
 import com.codingstory.polaris.indexing.IndexPathUtils;
 import com.codingstory.polaris.parser.FirstPassProcessor;
 import com.codingstory.polaris.parser.ImportExtractor;
 import com.codingstory.polaris.parser.ParserProtos.ClassType;
 import com.codingstory.polaris.parser.ParserProtos.FileHandle;
 import com.codingstory.polaris.parser.ParserProtos.SourceFile;
 import com.codingstory.polaris.parser.ParserProtos.Usage;
 import com.codingstory.polaris.parser.SecondPassProcessor;
 import com.codingstory.polaris.parser.SourceAnnotator;
 import com.codingstory.polaris.parser.SymbolTable;
 import com.codingstory.polaris.parser.ThirdPassProcessor;
 import com.codingstory.polaris.pipeline.PipelineProtos.FileContent;
 import com.codingstory.polaris.pipeline.PipelineProtos.FileImports;
 import com.codingstory.polaris.pipeline.PipelineProtos.ParsedFile;
 import com.codingstory.polaris.repo.GitUtils;
 import com.codingstory.polaris.repo.Repository;
 import com.codingstory.polaris.sourcedb.SourceDbWriter;
 import com.codingstory.polaris.sourcedb.SourceDbWriterImpl;
 import com.codingstory.polaris.typedb.TypeDbWriter;
 import com.codingstory.polaris.typedb.TypeDbWriterImpl;
 import com.codingstory.polaris.usagedb.UsageDbWriter;
 import com.codingstory.polaris.usagedb.UsageDbWriterImpl;
 import com.google.common.base.Joiner;
 import com.google.common.base.Preconditions;
 import com.google.common.collect.ImmutableList;
 import com.google.common.collect.ImmutableSet;
 import com.google.common.collect.Iterables;
 import com.google.common.collect.Lists;
 import com.google.common.io.Files;
 import org.apache.commons.io.FileUtils;
 import org.apache.commons.io.IOUtils;
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.lang.time.StopWatch;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.apache.crunch.CombineFn;
 import org.apache.crunch.DoFn;
 import org.apache.crunch.Emitter;
 import org.apache.crunch.MapFn;
 import org.apache.crunch.PCollection;
 import org.apache.crunch.PTable;
 import org.apache.crunch.Pair;
 import org.apache.crunch.PipelineResult;
 import org.apache.crunch.Tuple3;
 import org.apache.crunch.fn.IdentityFn;
 import org.apache.crunch.impl.mr.MRPipeline;
 import org.apache.crunch.impl.mr.plan.PlanningParameters;
 import org.apache.crunch.io.At;
 import org.apache.crunch.lib.Join;
 import org.apache.crunch.types.PTableType;
 import org.apache.crunch.types.PType;
 import org.apache.crunch.types.writable.WritableTypeFamily;
 import org.apache.hadoop.conf.Configuration;
 import org.apache.hadoop.fs.FileSystem;
 import org.apache.hadoop.fs.Path;
 import org.apache.hadoop.io.BytesWritable;
 import org.apache.hadoop.io.NullWritable;
 import org.apache.hadoop.io.SequenceFile;
 
 import java.io.ByteArrayInputStream;
 import java.io.File;
 import java.io.IOException;
 import java.io.Serializable;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.List;
 import java.util.Set;
 
 import static com.codingstory.polaris.CollectionUtils.nullToEmptyCollection;
 import static org.apache.crunch.types.PTypes.protos;
 import static org.apache.crunch.types.writable.Writables.collections;
 import static org.apache.crunch.types.writable.Writables.longs;
 import static org.apache.crunch.types.writable.Writables.strings;
 import static org.apache.crunch.types.writable.Writables.tableOf;
 import static org.apache.crunch.types.writable.Writables.triples;
 
 /** A series of MapReduce jobs transforming source files into things needed to be indexed. */
 public class IndexPipeline implements Serializable {
 
     private static final Log LOG = LogFactory.getLog(IndexPipeline.class);
     private static final IdGenerator ID_GENERATOR = new SimpleIdGenerator(); // Just OK for local MR.
     private static final WritableTypeFamily TYPE_FAMILY = WritableTypeFamily.getInstance();
     private static final PType<ParsedFile> PARSED_FILE_PTYPE = protos(ParsedFile.class, TYPE_FAMILY);
     private static final PType<FileContent> FILE_CONTENT_PTYPE = protos(FileContent.class, TYPE_FAMILY);
     private static final PType<FileImports> FILE_IMPORTS_PTYPE = protos(FileImports.class, TYPE_FAMILY);
     private static final PType<ClassType> CLASS_TYPE_PTYPE = protos(ClassType.class, TYPE_FAMILY);
     private static final PType<Usage> USAGE_PTYPE = protos(Usage.class, TYPE_FAMILY);
     private static final PType<SourceFile> SOURCE_FILE_PTYPE = protos(SourceFile.class, TYPE_FAMILY);
 
     private final transient Configuration conf; // "transient" No need to access it from MR tasks.
     private final transient FileSystem fs;
     private static final int MAX_IMPORTED_CLASSES = 1000; // prevent OOM
     private transient List<Repository> repos = Lists.newArrayList();
     private transient List<File> dirs = Lists.newArrayList();
     private File workingDir;
     private File inputDir1;
     private File inputDir2;
     private File classOutputDir;
     private File sourceOutputDir;
     private File usageOutputDir;
     private File indexDir;
 
     public IndexPipeline() {
         try {
             conf = new Configuration();
             conf.setInt("io.sort.mb", 32); // To fit into 256MB heap.
             conf.setBoolean("mapreduce.map.output.compress", true);
             conf.setBoolean("mapreduce.output.fileoutputformat.compress", true);
             conf.setStrings("mapreduce.output.fileoutputformat.compress.type", "BLOCK");
             conf.setBoolean("crunch.log.job.progress", true);
             SequenceFile.setDefaultCompressionType(conf, SequenceFile.CompressionType.BLOCK);
             fs = FileSystem.getLocal(conf);
         } catch (IOException e) {
             throw new AssertionError();
         }
     }
 
     public void addRepoBase(File repoBase) throws IOException {
         Preconditions.checkNotNull(repoBase);
         for (Repository repo : GitUtils.openRepoBase(repoBase)) {
             repos.add(repo);
         }
     }
 
     public void addProjectDirectory(File dir) {
         Preconditions.checkNotNull(dir);
         dirs.add(dir);
     }
 
     public void setIndexDirectory(File dir) {
         indexDir = Preconditions.checkNotNull(dir);
     }
 
     public void run() throws IOException {
         StopWatch stopWatch = new StopWatch();
         stopWatch.start();
 
         setUpInputAndOutputDirs();
         for (Repository repo : repos) {
             readRepo(repo);
         }
         for (File dir : dirs) {
             readProjectDir(dir);
         }
 
         MRPipeline pipeline = setUpPipeline();
         LOG.info("About to run indexing pipeline");
         checkPipelineResult(pipeline.run());
         LOG.info("Pipeline completes");
 
         buildIndexFromPipelineOutput();
 
        long secs = stopWatch.getTime();
         LOG.info(String.format("Elapsed time: %d min %d s", secs / 60, secs % 60));
     }
 
     public String plan() throws IOException {
         setUpInputAndOutputDirs();
         MRPipeline pipeline = setUpPipeline();
         pipeline.plan();
         return conf.get(PlanningParameters.PIPELINE_PLAN_DOTFILE);
     }
 
     private MRPipeline setUpPipeline() {
         MRPipeline pipeline = new MRPipeline(IndexPipeline.class, "polaris-index-pipeline", conf);
         pipeline.enableDebug();
         PCollection<FileContent> fileContents = pipeline
             .read(At.sequenceFile(new Path(inputDir1.getPath()), FILE_CONTENT_PTYPE));
         PCollection<ParsedFile> parsedFiles1stPass = discoverClasses(fileContents);
         PCollection<FileImports> fileImports = extractImports(fileContents);
         PTable<Long, Long> importGraph1 =
                 guessImportGraphByImportedClasses(fileImports, parsedFiles1stPass);
         PTable<Long, Long> importGraph2 =
                 guessImportGraphBySamePackage(parsedFiles1stPass);
         PTable<Long, Long> importGraph = uniqueImportGraph(importGraph1.union(importGraph2));
         // TODO: reverseImportGraphByImportedPackage
         // TODO: reverseImportGraphByFilePackage
         // TODO: Iterate to compute transitive closure
         PCollection<ParsedFile> parsedFiles2ndPass = discoverMembers(fileContents, parsedFiles1stPass, importGraph);
         PCollection<Usage> usages2ndPass = extractUsages(parsedFiles2ndPass);
         PCollection<ParsedFile> parsedFiles3rdPass = discoverMethodCalls(fileContents, parsedFiles2ndPass, importGraph);
         PCollection<Usage> usages3rdPass = extractUsages(parsedFiles3rdPass);
         PCollection<Usage> usages = usages2ndPass.union(usages3rdPass);
         pipeline.write(
                 extractClasses(parsedFiles3rdPass),
                 At.sequenceFile(new Path(classOutputDir.getPath()), PARSED_FILE_PTYPE));
         pipeline.write(
                 usages,
                 At.sequenceFile(new Path(usageOutputDir.getPath()), USAGE_PTYPE));
         pipeline.write(
                 annotateSources(fileContents, usages),
                 At.sequenceFile(new Path(sourceOutputDir.getPath()), SOURCE_FILE_PTYPE));
 
         return pipeline;
     }
 
     private PTable<Long, Long> uniqueImportGraph(PTable<Long, Long> importGraph) {
         return importGraph.groupByKey().combineValues(new CombineFn<Long, Long>() {
             @Override
             public void process(Pair<Long, Iterable<Long>> in, Emitter<Pair<Long, Long>> emitter) {
                 Set<Long> unique = ImmutableSet.copyOf(in.second());
                 for (Long n : unique) {
                     emitter.emit(Pair.of(in.first(), n));
                 }
             }
         });
     }
 
     private void checkPipelineResult(PipelineResult result) throws IOException {
         if (!result.succeeded()) {
             throw new IOException("Pipeline failed");
         }
     }
 
     private PCollection<FileImports> extractImports(PCollection<FileContent> fileContents) {
         return fileContents.parallelDo("ExtractImports", new DoFn<FileContent, FileImports>() {
             @Override
             public void process(FileContent in, Emitter<FileImports> emitter) {
                 try {
                     ImportExtractor.Result result = ImportExtractor.findImports(
                             new ByteArrayInputStream(in.getContent().getBytes()));
                     emitter.emit(FileImports.newBuilder()
                             .setFile(in.getFile())
                             .setPackage(result.getPackage())
                             .addAllImportedClasses(result.getImportedClasses())
                             .addAllImportedPackages(result.getImportedPackages())
                             .build());
                 } catch (IOException e) {
                     LOG.warn("Failed to parse " + in.getFile());
                     LOG.debug("Exception", e);
                 }
             }
         }, FILE_IMPORTS_PTYPE);
     }
 
     private PCollection<ParsedFile> discoverClasses(PCollection<FileContent> fileContents) {
         PCollection<ParsedFile> parsedFiles =
                 fileContents.parallelDo("FirstPass", new DoFn<FileContent, ParsedFile>() {
                     @Override
                     public void process(FileContent in, Emitter<ParsedFile> emitter) {
                         try {
                             FirstPassProcessor.Result result = FirstPassProcessor.process(
                                     in.getFile(),
                                     new ByteArrayInputStream(in.getContent().getBytes()),
                                     ID_GENERATOR);
                             SourceFile sourceFile = SourceFile.newBuilder()
                                     .setHandle(in.getFile())
                                     .build();
                             // Don't save file content for now, since ParsedFile produced by 1st pass is
                             // joined and duplicated for many times (= number of references).
                             ParsedFile out = ParsedFile.newBuilder()
                                     .setFile(sourceFile.getHandle())
                                     .setPackage(result.getPackage())
                                     .addAllClasses(result.getDiscoveredClasses())
                                     .build();
                             emitter.emit(out);
                         } catch (IOException e) {
                             LOG.warn("Failed to parse " + in.getFile());
                             LOG.debug("Exception", e);
                         }
                     }
                 }, PARSED_FILE_PTYPE);
 
         // Force first pass is executed once.
         return pivotParsedFilesByFileId(parsedFiles).collectValues().values().parallelDo(
                 new DoFn<Collection<ParsedFile>, ParsedFile>() {
                     @Override
                     public void process(Collection<ParsedFile> in, Emitter<ParsedFile> emitter) {
                         for (ParsedFile t : in) {
                             emitter.emit(t);
                         }
                     }
                 }, PARSED_FILE_PTYPE);
     }
 
     private void setUpInputAndOutputDirs() throws IOException {
         workingDir = File.createTempFile("polaris-pipeline-", "");
         FileUtils.deleteQuietly(workingDir);
         FileUtils.forceMkdir(workingDir);
         inputDir1 = new File(workingDir, "in1");
         inputDir2 = new File(workingDir, "in2");
         classOutputDir = new File(workingDir, "out-classes");
         usageOutputDir = new File(workingDir, "out-usages");
         sourceOutputDir = new File(workingDir, "out-sources");
         FileUtils.forceMkdir(inputDir1);
         FileUtils.forceMkdir(inputDir2);
         conf.set("hadoop.tmp.dir", workingDir.getPath());
         conf.set("crunch.tmp.dir", workingDir.getPath());
         LOG.info("Temporary working dirctory: " + workingDir);
     }
 
     private void readRepo(Repository repo) throws IOException {
         Preconditions.checkNotNull(repo);
         LOG.info("Scanning repository: " + repo.getName());
         File workingTree = checkOutWorkTree(repo);
         doReadProjectDir(repo.getName(), workingTree);
         FileUtils.deleteDirectory(workingTree);
     }
 
     private void readProjectDir(File dir) throws IOException {
         LOG.info("Scanning project root: " + dir.getName());
         doReadProjectDir(dir.getName(), dir);
     }
 
     private void doReadProjectDir(String project, File dir) throws IOException {
         Preconditions.checkNotNull(project);
         Preconditions.checkNotNull(dir);
         SequenceFile.Writer w1 = SequenceFile.createWriter(fs, conf,
                 new Path(new File(inputDir1, "sources-of-" + project).getPath()),
                 NullWritable.class, BytesWritable.class);
         SequenceFile.Writer w2 = SequenceFile.createWriter(fs, conf,
                 new Path(new File(inputDir2, "dirs-of-" + project).getPath()),
                 NullWritable.class, BytesWritable.class);
 
         final List<File> sourceDirs = Lists.newArrayList();
         final List<File> sourceFiles = Lists.newArrayList();
         DirectoryTranverser.traverse(dir, new DirectoryTranverser.Visitor() {
             @Override
             public void visit(File file) {
                 if (file.isHidden()) {
                     return;
                 }
                 if (file.isDirectory()) {
                     sourceDirs.add(file);
                 } else if (file.getName().endsWith(".java")) {
                     sourceFiles.add(file);
                 }
             }
         });
 
         LOG.info("Found " + sourceFiles.size() + " file(s)");
         long count = 0;
         for (File sourceFile : sourceFiles) {
             FileHandle handle = FileHandle.newBuilder()
                     .setId(ID_GENERATOR.next())
                     .setProject(project)
                     .setPath(StringUtils.removeStart(sourceFile.getPath(), dir.getPath()))
                     .build();
             FileContent in = FileContent.newBuilder()
                     .setFile(handle)
                     .setContent(FileUtils.readFileToString(sourceFile))
                     .build();
             w1.append(NullWritable.get(), new BytesWritable(in.toByteArray()));
             count++;
             if (count % 5000 == 0) {
                 LOG.info("Processed " + count + " files");
             }
         }
         w1.close();
 
         for (File sourceDir : sourceDirs) {
             FileHandle f = FileHandle.newBuilder()
                     .setId(ID_GENERATOR.next())
                     .setProject(project)
                     .setPath(StringUtils.removeStart(sourceDir.getPath(), dir.getPath()) + "/")
                     .build();
             w2.append(NullWritable.get(), new BytesWritable(f.toByteArray()));
         }
         w2.close();
     }
 
     private File checkOutWorkTree(Repository repo) throws IOException {
         File tempDir = Files.createTempDir();
         GitUtils.checkoutWorkTree(repo, tempDir);
         return tempDir;
     }
 
     private PTable<Long, Long> guessImportGraphByImportedClasses(
             PCollection<FileImports> fileImports,
             PCollection<ParsedFile> parsedFiles) {
         PTable<String, Long> left = fileImports.parallelDo(
                 "ParsedFilesByImportedClasses",
                 new DoFn<FileImports, Pair<String, Long>>() {
                     @Override
                     public void process(FileImports in, Emitter<Pair<String, Long>> emitter) {
                         for (String clazz : in.getImportedClassesList()) {
                             emitter.emit(Pair.of(clazz, in.getFile().getId()));
                         }
                     }
                 }, tableOf(strings(), longs()));
 
         PTable<String, Long> right = parsedFiles.parallelDo(
                 "ParsedFilesByDeclaredClasses",
                 new DoFn<ParsedFile, Pair<String, Long>>() {
                     @Override
                     public void process(
                             ParsedFile in,
                             Emitter<Pair<String, Long>> emitter) {
                         for (ClassType clazz : in.getClassesList()) {
                             emitter.emit(Pair.of(clazz.getHandle().getName(), in.getFile().getId()));
                         }
                     }
                 }, tableOf(strings(), longs()));
 
         return left.join(right).values().parallelDo(
                 IdentityFn.<Pair<Long, Long>>getInstance(), tableOf(longs(), longs()));
     }
 
     /** Produces import relation A -> B if A and B are in same package. */
     private PTable<Long, Long> guessImportGraphBySamePackage(PCollection<ParsedFile> parsedFiles) {
         PTable<String, ParsedFile> parsedFilesByPackage = pivotParsedFilesByPackage(parsedFiles);
         return Join.innerJoin(parsedFilesByPackage, parsedFilesByPackage).values().parallelDo(
                 new MapFn<Pair<ParsedFile, ParsedFile>, Pair<Long, Long>>() {
                     @Override
                     public Pair<Long, Long> map(Pair<ParsedFile, ParsedFile> in) {
                         return Pair.of(
                                 in.first().getFile().getId(),
                                 in.second().getFile().getId());
                     }
                 }, tableOf(longs(), longs()));
     }
 
     private PCollection<Tuple3<FileContent, ParsedFile, Collection<ClassType>>> joinImports(
             PCollection<FileContent> fileContents,
             PCollection<ParsedFile> parsedFiles,
             PTable<Long, Long> importGraph) {
         // Assume A imports B...
         PTable<Long, FileContent> fileContentById = pivotFileContentById(fileContents);
         PTable<Long, ParsedFile> parsedFilesById = pivotParsedFilesByFileId(parsedFiles); // B -> class B {...}
         PTable<Long, ClassType> classesById = pivotClassByFileId(extractClasses(parsedFiles));
         PTable<Long, Long> invertImportGraph = inverse(importGraph, tableOf(longs(), longs())); // B -> A
         PTable<Long, ClassType> classesByImporterId = invertImportGraph.join(
                 classesById).values().parallelDo( // A -> class B {...}
                 IdentityFn.<Pair<Long, ClassType>>getInstance(),
                 tableOf(longs(), CLASS_TYPE_PTYPE));
 
         // Left join because a file can be imported by nobody.
         PTable<Long, Pair<FileContent, ParsedFile>> step1 = Join.join(fileContentById, parsedFilesById);
         PTable<Long, Collection<ClassType>> step2 = classesByImporterId.collectValues();
         return Join.leftJoin(step1, step2).values().parallelDo(
                 new MapFn<Pair<Pair<FileContent, ParsedFile>, Collection<ClassType>>,
                         Tuple3<FileContent, ParsedFile, Collection<ClassType>>>() {
                     @Override
                     public Tuple3<FileContent, ParsedFile, Collection<ClassType>> map(
                             Pair<Pair<FileContent, ParsedFile>, Collection<ClassType>> in) {
                         FileContent fileContent = in.first().first();
                         Collection<ClassType> classes = truncateImportedClassesIfTooMany(
                                 fileContent.getFile(), in.second());
                         return Tuple3.of(fileContent, in.first().second(), classes);
                     }
                 }, triples(FILE_CONTENT_PTYPE, PARSED_FILE_PTYPE, collections(CLASS_TYPE_PTYPE)));
     }
 
     private Collection<ClassType> truncateImportedClassesIfTooMany(
             FileHandle file, Collection<ClassType> importedClasses) {
         if (importedClasses == null) {
             return ImmutableList.of();
         }
         if (importedClasses.size() < MAX_IMPORTED_CLASSES) {
             return importedClasses;
         }
         List<ClassType> l = ImmutableList.copyOf(importedClasses);
         List<ClassType> toKeep = l.subList(0, MAX_IMPORTED_CLASSES);
         List<ClassType> toDrop = l.subList(MAX_IMPORTED_CLASSES, l.size());
         List<String> toDropExamples = Lists.newArrayList();
         for (ClassType clazz : Iterables.limit(toDrop, 10)) {
             toDropExamples.add(clazz.getHandle().getName());
         }
         LOG.warn(file.getPath() + " has " + importedClasses.size() + " imports, which is too many: " +
                 Joiner.on('\n').join(toDropExamples) + ". Only keep first " + MAX_IMPORTED_CLASSES);
         return toKeep;
     }
 
     private PTable<Long, ClassType> pivotClassByFileId(PCollection<ClassType> classes) {
         return classes.by(new MapFn<ClassType, Long>() {
             @Override
             public Long map(ClassType in) {
                 return in.getJumpTarget().getFile().getId();
             }
         }, longs());
     }
 
     private PCollection<ParsedFile> discoverMembers(
             PCollection<FileContent> fileContents,
             PCollection<ParsedFile> parsedFiles,
             PTable<Long, Long> importGraph) {
         return joinImports(fileContents, parsedFiles, importGraph)
                 .parallelDo("SecondPass",
                         new MapFn<Tuple3<FileContent, ParsedFile, Collection<ClassType>>, ParsedFile>() {
                             @Override
                             public ParsedFile map(Tuple3<FileContent, ParsedFile, Collection<ClassType>> in) {
                                 try {
                                     FileContent fileContent = in.first();
                                     ParsedFile currentFile = in.second();
                                     FileHandle fileHandle = currentFile.getFile();
                                     Collection<ClassType> importedClasses = in.third();
                                     SymbolTable symbolTable = createSymbolTable(currentFile, importedClasses);
                                     SecondPassProcessor.Result result = SecondPassProcessor.extract(
                                             fileHandle.getProject(),
                                             fileHandle,
                                             new ByteArrayInputStream(fileContent.getContent().getBytes()),
                                             symbolTable,
                                             ID_GENERATOR,
                                             currentFile.getPackage());
                                     return currentFile.toBuilder()
                                             .clearClasses()
                                             .addAllClasses(result.getClassTypes())
                                             .clearUsages()
                                             .addAllUsages(result.getUsages())
                                             .build();
                                 } catch (IOException e) {
                                     // Since we've inner-joined "parsedFilesById", no exceptions should occur.
                                     throw new AssertionError(e);
                                 }
                             }
                         }, PARSED_FILE_PTYPE);
     }
 
     private SymbolTable createSymbolTable(ParsedFile currentFile, Collection<ClassType> importedClasses) {
         List<ClassType> classes = Lists.newArrayList();
         classes.addAll(currentFile.getClassesList());
         classes.addAll(importedClasses);
         SymbolTable symbolTable = new SymbolTable();
         for (ClassType clazz : classes) {
             symbolTable.registerClassType(clazz);
         }
         return symbolTable;
     }
 
     private PCollection<ParsedFile> discoverMethodCalls(
             PCollection<FileContent> fileContents,
             PCollection<ParsedFile> parsedFiles,
             PTable<Long, Long> importGraph) {
         return joinImports(fileContents, parsedFiles, importGraph)
                 .parallelDo("ThirdPass",
                         new MapFn<Tuple3<FileContent, ParsedFile, Collection<ClassType>>, ParsedFile>() {
                     @Override
                     public ParsedFile map(Tuple3<FileContent, ParsedFile, Collection<ClassType>> in) {
                         try {
                             FileContent fileContent = in.first();
                             ParsedFile currentFile = in.second();
                             FileHandle fileHandle = currentFile.getFile();
                             Collection<ClassType> importedClasses = in.third();
                             List<Usage> result = ThirdPassProcessor.extract(
                                     fileHandle,
                                     new ByteArrayInputStream(fileContent.getContent().getBytes()),
                                     createSymbolTable(currentFile, importedClasses),
                                     currentFile.getPackage());
                             return currentFile.toBuilder()
                                     .addAllUsages(result)
                                     .build();
                         } catch (IOException e) {
                             throw new AssertionError(e);
                         }
                     }
                 }, PARSED_FILE_PTYPE);
     }
 
     private PTable<Long, ParsedFile> pivotParsedFilesByFileId(PCollection<ParsedFile> parsedFiles) {
         return parsedFiles.parallelDo(
                 "ParsedFilesByFileId",
                 new MapFn<ParsedFile, Pair<Long, ParsedFile>>() {
                     @Override
                     public Pair<Long, ParsedFile> map(ParsedFile in) {
                         return Pair.of(in.getFile().getId(), in);
                     }
                 }, tableOf(longs(), PARSED_FILE_PTYPE));
     }
 
     private PTable<String,ParsedFile> pivotParsedFilesByPackage(PCollection<ParsedFile> parsedFiles) {
         return parsedFiles.by("ParsedFilesByPackage", new MapFn<ParsedFile, String>() {
             @Override
             public String map(ParsedFile in) {
                 return in.getPackage();
             }
         }, strings());
     }
 
     private static PTable<Long, FileContent> pivotFileContentById(PCollection<FileContent> fileContents) {
         return fileContents.parallelDo("FileContentsById", new MapFn<FileContent, Pair<Long, FileContent>>() {
             @Override
             public Pair<Long, FileContent> map(FileContent in) {
                 return Pair.of(in.getFile().getId(), in);
             }
         }, tableOf(longs(), FILE_CONTENT_PTYPE));
     }
 
     private static <K, V> PTable<V, K> inverse(PTable<K, V> table, PTableType<V, K> ptype) {
         return table.parallelDo("InverseTable", new MapFn<Pair<K, V>, Pair<V, K>>() {
             @Override
             public Pair<V, K> map(Pair<K, V> in) {
                 return Pair.of(in.second(), in.first());
             }
         }, ptype);
     }
 
     private void buildIndexFromPipelineOutput() throws IOException {
         // TODO: Build index in pipeline.
         TypeDbWriter typeDb = null;
         SourceDbWriter sourceDb = null;
         UsageDbWriter usageDb = null;
         try {
             typeDb = new TypeDbWriterImpl(IndexPathUtils.getTypeDbPath(indexDir));
             sourceDb = new SourceDbWriterImpl(IndexPathUtils.getSourceDbPath(indexDir));
             usageDb = new UsageDbWriterImpl(IndexPathUtils.getUsageDbPath(indexDir));
 
             // Process pipeline output.
             BytesWritable value = new BytesWritable();
             for (File file : classOutputDir.listFiles()) {
                 if (isCrcFile(file)) {
                     continue;
                 }
                 SequenceFile.Reader r = openLocalSequenceFile(file);
                 while (r.next(NullWritable.get(), value)) {
                     typeDb.write(ClassType.parseFrom(Arrays.copyOf(value.getBytes(), value.getLength())));
                 }
             }
             for (File file : usageOutputDir.listFiles()) {
                 if (isCrcFile(file)) {
                     continue;
                 }
                 SequenceFile.Reader r = openLocalSequenceFile(file);
                 while (r.next(NullWritable.get(), value)) {
                     Usage usage = Usage.parseFrom(Arrays.copyOf(value.getBytes(), value.getLength()));
                     if (usage.getKind() != Usage.Kind.TYPE ||
                             usage.getType().getType().getClazz().getResolved()) {
                         usageDb.write(usage);
                     }
                 }
             }
             for (File file : sourceOutputDir.listFiles()) {
                 if (isCrcFile(file)) {
                     continue;
                 }
                 SequenceFile.Reader r = openLocalSequenceFile(file);
                 while (r.next(NullWritable.get(), value)) {
                     sourceDb.writeSourceFile(SourceFile.parseFrom(Arrays.copyOf(value.getBytes(), value.getLength())));
                 }
             }
 
             // Process repository layout.
             for (File file : inputDir2.listFiles()) {
                 if (isCrcFile(file)) {
                     continue;
                 }
                 SequenceFile.Reader r = openLocalSequenceFile(file);
                 while (r.next(NullWritable.get(), value)) {
                     FileHandle f = FileHandle.parseFrom(Arrays.copyOf(value.getBytes(), value.getLength()));
                     sourceDb.writeDirectory(f.getProject(), f.getPath());
                 }
             }
 
             LOG.info("Index files are written to " + indexDir);
         } finally {
             IOUtils.closeQuietly(typeDb);
             IOUtils.closeQuietly(sourceDb);
             IOUtils.closeQuietly(usageDb);
         }
     }
 
     private boolean isCrcFile(File file) {
         return file.getPath().endsWith(".crc");
     }
 
     private SequenceFile.Reader openLocalSequenceFile(File file) throws IOException {
         return new SequenceFile.Reader(fs, new Path(file.getPath()), conf);
     }
 
     private PCollection<ClassType> extractClasses(PCollection<ParsedFile> parsedFiles) {
         return parsedFiles.parallelDo(new DoFn<ParsedFile, ClassType>() {
             @Override
             public void process(ParsedFile in, Emitter<ClassType> emitter) {
                 for (ClassType clazz : in.getClassesList()) {
                     emitter.emit(clazz);
                 }
             }
         }, CLASS_TYPE_PTYPE);
     }
 
     private PCollection<Usage> extractUsages(PCollection<ParsedFile> parsedFiles) {
         return parsedFiles.parallelDo(new DoFn<ParsedFile, Usage>() {
             @Override
             public void process(ParsedFile in, Emitter<Usage> emitter) {
                 for (Usage usage : in.getUsagesList()) {
                     emitter.emit(usage);
                 }
             }
         }, USAGE_PTYPE);
     }
 
     private PCollection<SourceFile> annotateSources(PCollection<FileContent> fileContents, PCollection<Usage> usages) {
         PTable<Long, FileContent> fileContentsById = pivotFileContentById(fileContents);
         PTable<Long, Usage> usagesByFileId = pivotUsagesByFileId(usages);
         return Join.leftJoin(fileContentsById, usagesByFileId.collectValues()).values().parallelDo(
                 "AnnotateSource", new MapFn<Pair<FileContent, Collection<Usage>>, SourceFile>() {
             @Override
             public SourceFile map(Pair<FileContent, Collection<Usage>> in) {
                 try {
                     FileContent fileContent = in.first();
                     Collection<Usage> usages = nullToEmptyCollection(in.second());
                     String annotated = SourceAnnotator.annotate(
                             new ByteArrayInputStream(fileContent.getContent().getBytes()),
                             usages);
                     return SourceFile.newBuilder()
                             .setHandle(fileContent.getFile())
                             .setSource(fileContent.getContent())
                             .setAnnotatedSource(annotated)
                             .build();
                 } catch (IOException e) {
                     throw new AssertionError(e);
                 }
             }
         }, SOURCE_FILE_PTYPE);
     }
 
     private PTable<Long, Usage> pivotUsagesByFileId(PCollection<Usage> usages) {
         return usages.by(new MapFn<Usage, Long>() {
             @Override
             public Long map(Usage usage) {
                 return usage.getJumpTarget().getFile().getId();
             }
         }, longs());
     }
 
     public void cleanUp() {
         LOG.info("Deleting temporary working directory: " + workingDir);
         FileUtils.deleteQuietly(workingDir);
     }
 }
