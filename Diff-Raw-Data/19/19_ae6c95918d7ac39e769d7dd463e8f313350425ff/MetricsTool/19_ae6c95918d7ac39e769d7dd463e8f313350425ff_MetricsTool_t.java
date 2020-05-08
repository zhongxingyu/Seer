 package jp.ac.osaka_u.ist.sel.metricstool.main;
 
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Set;
 
 import jp.ac.osaka_u.ist.sel.metricstool.main.ast.java.Java15AntlrAstTranslator;
 import jp.ac.osaka_u.ist.sel.metricstool.main.ast.java.JavaAstVisitorManager;
 import jp.ac.osaka_u.ist.sel.metricstool.main.ast.visitor.AstVisitorManager;
 import jp.ac.osaka_u.ist.sel.metricstool.main.ast.visitor.antlr.AntlrAstVisitor;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.metric.ClassMetricsInfoManager;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.metric.FileMetricsInfoManager;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.metric.MethodMetricsInfoManager;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.metric.MetricNotRegisteredException;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.ArrayTypeInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.ClassInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.ClassInfoManager;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.FieldInfoManager;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.FileInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.FileInfoManager;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.LocalVariableInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.MethodInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.MethodInfoManager;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.ModifierInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.TargetClassInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.TargetFieldInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.TargetFile;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.TargetFileManager;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.TargetInnerClassInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.TargetMethodInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.TargetParameterInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.TypeInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.external.ExternalClassInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.unresolved.NameResolver;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.unresolved.UnresolvedArrayTypeInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.unresolved.UnresolvedClassInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.unresolved.UnresolvedClassInfoManager;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.unresolved.UnresolvedFieldInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.unresolved.UnresolvedFieldUsage;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.unresolved.UnresolvedLocalVariableInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.unresolved.UnresolvedMethodCall;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.unresolved.UnresolvedMethodInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.unresolved.UnresolvedParameterInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.unresolved.UnresolvedReferenceTypeInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.unresolved.UnresolvedTypeInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.io.CSVClassMetricsWriter;
 import jp.ac.osaka_u.ist.sel.metricstool.main.io.CSVFileMetricsWriter;
 import jp.ac.osaka_u.ist.sel.metricstool.main.io.CSVMethodMetricsWriter;
 import jp.ac.osaka_u.ist.sel.metricstool.main.io.DefaultMessagePrinter;
 import jp.ac.osaka_u.ist.sel.metricstool.main.io.MessageEvent;
 import jp.ac.osaka_u.ist.sel.metricstool.main.io.MessageListener;
 import jp.ac.osaka_u.ist.sel.metricstool.main.io.MessagePool;
 import jp.ac.osaka_u.ist.sel.metricstool.main.io.MessagePrinter;
 import jp.ac.osaka_u.ist.sel.metricstool.main.io.MessageSource;
 import jp.ac.osaka_u.ist.sel.metricstool.main.io.MessagePrinter.MESSAGE_TYPE;
 import jp.ac.osaka_u.ist.sel.metricstool.main.parse.Java15Lexer;
 import jp.ac.osaka_u.ist.sel.metricstool.main.parse.Java15Parser;
 import jp.ac.osaka_u.ist.sel.metricstool.main.plugin.AbstractPlugin;
 import jp.ac.osaka_u.ist.sel.metricstool.main.plugin.DefaultPluginLauncher;
 import jp.ac.osaka_u.ist.sel.metricstool.main.plugin.PluginLauncher;
 import jp.ac.osaka_u.ist.sel.metricstool.main.plugin.PluginManager;
 import jp.ac.osaka_u.ist.sel.metricstool.main.plugin.AbstractPlugin.PluginInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.plugin.loader.DefaultPluginLoader;
 import jp.ac.osaka_u.ist.sel.metricstool.main.plugin.loader.PluginLoadException;
 import jp.ac.osaka_u.ist.sel.metricstool.main.security.MetricsToolSecurityManager;
 import jp.ac.osaka_u.ist.sel.metricstool.main.util.LANGUAGE;
 
 import org.jargp.ArgumentProcessor;
 import org.jargp.BoolDef;
 import org.jargp.ParameterDef;
 import org.jargp.StringDef;
 
 import antlr.RecognitionException;
 import antlr.TokenStreamException;
 import antlr.collections.AST;
 
 
 /**
  * 
  * @author y-higo
  * 
  * MetricsTool̃CNXD ݂͉D
  * 
  * since 2006.11.12
  * 
  */
 public class MetricsTool {
 
     static {
         // \p̃Xi쐬
         MessagePool.getInstance(MESSAGE_TYPE.OUT).addMessageListener(new MessageListener() {
             public void messageReceived(MessageEvent event) {
                 System.out.print(event.getSource().getMessageSourceName() + " > "
                         + event.getMessage());
             }
         });
 
         MessagePool.getInstance(MESSAGE_TYPE.ERROR).addMessageListener(new MessageListener() {
             public void messageReceived(MessageEvent event) {
                 System.err.print(event.getSource().getMessageSourceName() + " > "
                         + event.getMessage());
             }
         });
     }
 
     /**
      * 
      * @param args Ώۃt@C̃t@CpX
      * 
      * ݉D Ώۃt@C̃f[^i[C\͂sD
      */
     public static void main(String[] args) {
         initSecurityManager();
 
         ArgumentProcessor.processArgs(args, parameterDefs, new Settings());
 
         // wv[hƏ\[hɃIɂȂĂꍇ͕s
         if (Settings.isHelpMode() && Settings.isDisplayMode()) {
             System.err.println("-h and -x can\'t be set at the same time!");
             printUsage();
             System.exit(0);
         }
 
         if (Settings.isHelpMode()) {
             // wv[h̏ꍇ
             doHelpMode();
         } else {
             LANGUAGE language = getLanguage();
             loadPlugins(language, Settings.getMetricStrings());
 
             if (Settings.isDisplayMode()) {
                 // \[h̏ꍇ
                 doDisplayMode(language);
             } else {
                 // ̓[h
                 doAnalysisMode(language);
             }
         }
     }
 
     /**
      * ǂݍ񂾑Ώۃt@CQ͂.
      * 
      * @param language ͑Ώۂ̌
      */
     private static void analyzeTargetFiles(final LANGUAGE language) {
         // Ώۃt@C
 
         AstVisitorManager<AST> visitorManager = null;
         if (language.equals(LANGUAGE.JAVA)) {
             visitorManager = new JavaAstVisitorManager<AST>(new AntlrAstVisitor(
                     new Java15AntlrAstTranslator()));
         }
 
         out.println("Parse all target files.");
         for (TargetFile targetFile : TargetFileManager.getInstance()) {
             try {
                 String name = targetFile.getName();
 
                 FileInfo fileInfo = new FileInfo(name);
                 FileInfoManager.getInstance().add(fileInfo);
 
                 out.println("parsing " + name);
                 Java15Lexer lexer = new Java15Lexer(new FileInputStream(name));
                 Java15Parser parser = new Java15Parser(lexer);
                 parser.compilationUnit();
                 targetFile.setCorrectSytax(true);
 
                 if (visitorManager != null) {
                     visitorManager.setPositionManager(parser.getPositionManger());
                     visitorManager.visitStart(parser.getAST());
                 }
 
                 fileInfo.setLOC(lexer.getLine());
 
             } catch (FileNotFoundException e) {
                 err.println(e.getMessage());
             } catch (RecognitionException e) {
                 targetFile.setCorrectSytax(false);
                 err.println(e.getMessage());
                 // TODO G[NƂ TargetFileData Ȃǂɒʒm鏈Kv
             } catch (TokenStreamException e) {
                 targetFile.setCorrectSytax(false);
                 err.println(e.getMessage());
                 // TODO G[NƂ TargetFileData Ȃǂɒʒm鏈Kv
             }
         }
 
         out.println("Resolve Definitions and Usages.");
         out.println("STEP1 : resolve class definitions.");
         registClassInfos();
         out.println("STEP2 : resolve field definitions.");
         registFieldInfos();
         out.println("STEP3 : resolve method definitions.");
         registMethodInfos();
         out.println("STEP4 : resolve class inheritances.");
         addInheritanceInformationToClassInfos();
         out.println("STEP5 : resolve method overrides.");
         addOverrideRelation();
         out.println("STEP6 : resolve field and method usages.");
         addReferenceAssignmentCallRelateion();
 
         // @̂t@Cꗗ\
         // err.println("The following files includes uncorrect syntax.");
         // err.println("Any metrics of them were not measured");
         for (TargetFile targetFile : TargetFileManager.getInstance()) {
             if (!targetFile.isCorrectSyntax()) {
                 err.println("Incorrect syntax file: " + targetFile.getName());
             }
         }
     }
 
     /**
      * 
      * wv[ḧ̐mF邽߂̃\bhD sȈw肳ĂꍇCmain \bhɂ͖߂炸C̊֐ŃvOID
      * 
      */
     private static void checkHelpModeParameterValidation() {
         // -h ͑̃IvVƓwłȂ
         if ((!Settings.getTargetDirectory().equals(Settings.INIT))
                 || (!Settings.getListFile().equals(Settings.INIT))
                 || (!Settings.getLanguageString().equals(Settings.INIT))
                 || (!Settings.getMetrics().equals(Settings.INIT))
                 || (!Settings.getFileMetricsFile().equals(Settings.INIT))
                 || (!Settings.getClassMetricsFile().equals(Settings.INIT))
                 || (!Settings.getMethodMetricsFile().equals(Settings.INIT))) {
             System.err.println("-h can\'t be specified with any other options!");
             printUsage();
             System.exit(0);
         }
     }
 
     /**
      * 
      * \[ḧ̐mF邽߂̃\bhD sȈw肳ĂꍇCmain \bhɂ͖߂炸C̊֐ŃvOID
      * 
      */
     private static void checkDisplayModeParameterValidation() {
         // -d ͎gȂ
         if (!Settings.getTargetDirectory().equals(Settings.INIT)) {
             System.err.println("-d can\'t be specified in the display mode!");
             printUsage();
             System.exit(0);
         }
 
         // -i ͎gȂ
         if (!Settings.getListFile().equals(Settings.INIT)) {
             System.err.println("-i can't be specified in the display mode!");
             printUsage();
             System.exit(0);
         }
 
         // -F ͎gȂ
         if (!Settings.getFileMetricsFile().equals(Settings.INIT)) {
             System.err.println("-F can't be specified in the display mode!");
             printUsage();
             System.exit(0);
         }
 
         // -C ͎gȂ
         if (!Settings.getClassMetricsFile().equals(Settings.INIT)) {
             System.err.println("-C can't be specified in the display mode!");
             printUsage();
             System.exit(0);
         }
 
         // -M ͎gȂ
         if (!Settings.getMethodMetricsFile().equals(Settings.INIT)) {
             System.err.println("-M can't be specified in the display mode!");
             printUsage();
             System.exit(0);
         }
     }
 
     /**
      * 
      * ̓[ḧ̐mF邽߂̃\bhD sȈw肳ĂꍇCmain \bhɂ͖߂炸C̊֐ŃvOID
      * 
      * @param w肳ꂽ
      * 
      */
     private static void checkAnalysisModeParameterValidation(LANGUAGE language) {
         // -d  -i ̂ǂw肳Ă͕̂s
         if (Settings.getTargetDirectory().equals(Settings.INIT)
                 && Settings.getListFile().equals(Settings.INIT)) {
             System.err.println("-d or -i must be specified in the analysis mode!");
             printUsage();
             System.exit(0);
         }
 
         // -d  -i ̗w肳Ă͕̂s
         if (!Settings.getTargetDirectory().equals(Settings.INIT)
                 && !Settings.getListFile().equals(Settings.INIT)) {
             System.err.println("-d and -i can't be specified at the same time!");
             printUsage();
             System.exit(0);
         }
 
         // ꂪw肳Ȃ͕̂s
         if (null == language) {
             System.err.println("-l must be specified in the analysis mode.");
             printUsage();
             System.exit(0);
         }
 
         boolean measureFileMetrics = false;
         boolean measureClassMetrics = false;
         boolean measureMethodMetrics = false;
 
         for (PluginInfo pluginInfo : PluginManager.getInstance().getPluginInfos()) {
             switch (pluginInfo.getMetricType()) {
             case FILE_METRIC:
                 measureFileMetrics = true;
                 break;
             case CLASS_METRIC:
                 measureClassMetrics = true;
                 break;
             case METHOD_METRIC:
                 measureMethodMetrics = true;
                 break;
             }
         }
 
         // t@CgNXvꍇ -F IvVw肳ĂȂ΂ȂȂ
         if (measureFileMetrics && (Settings.getFileMetricsFile().equals(Settings.INIT))) {
             System.err.println("-F must be used for specifying a file for file metrics!");
             System.exit(0);
         }
 
         // NXgNXvꍇ -C IvVw肳ĂȂ΂ȂȂ
         if (measureClassMetrics && (Settings.getClassMetricsFile().equals(Settings.INIT))) {
             System.err.println("-C must be used for specifying a file for class metrics!");
             System.exit(0);
         }
         // \bhgNXvꍇ -M IvVw肳ĂȂ΂ȂȂ
         if (measureMethodMetrics && (Settings.getMethodMetricsFile().equals(Settings.INIT))) {
             System.err.println("-M must be used for specifying a file for method metrics!");
             System.exit(0);
         }
     }
 
     /**
      * ̓[hs.
      * @param language Ώی
      */
     private static void doAnalysisMode(LANGUAGE language) {
         checkAnalysisModeParameterValidation(language);
 
         readTargetFiles();
         analyzeTargetFiles(language);
         launchPlugins();
         writeMetrics();
     }
 
     /**
      * \[hs
      * @param language Ώی
      */
     private static void doDisplayMode(LANGUAGE language) {
         checkDisplayModeParameterValidation();
 
         // -l Ōꂪw肳ĂȂꍇ́C͉\ꗗ\
         if (null == language) {
             System.err.println("Available languages;");
             LANGUAGE[] languages = LANGUAGE.values();
             for (int i = 0; i < languages.length; i++) {
                 System.err.println("\t" + languages[0].getName()
                         + ": can be specified with term \"" + languages[0].getIdentifierName()
                         + "\"");
             }
 
             // -l Ōꂪw肳Ăꍇ́C̃vO~OŎgp\ȃgNXꗗ\
         } else {
             System.err.println("Available metrics for " + language.getName());
             for (AbstractPlugin plugin : PluginManager.getInstance().getPlugins()) {
                 PluginInfo pluginInfo = plugin.getPluginInfo();
                 if (pluginInfo.isMeasurable(language)) {
                     System.err.println("\t" + pluginInfo.getMetricName());
                 }
             }
             // TODO p\gNXꗗ\
         }
     }
 
     /**
      * wv[hs.
      */
     private static void doHelpMode() {
         checkHelpModeParameterValidation();
 
         printUsage();
     }
 
     /**
      * Ώی擾.
      * 
      * @return w肳ꂽΏی.w肳Ȃꍇnull
      */
     private static LANGUAGE getLanguage() {
         if (Settings.getLanguageString().equals(Settings.INIT)) {
             return null;
         }
 
         return Settings.getLanguage();
     }
 
     /**
      * {@link MetricsToolSecurityManager} ̏s.
      * VXeɓo^ł΁CVXẽZLeB}l[Wɂo^.
      */
     private static void initSecurityManager() {
         try {
             // MetricsToolSecurityManager̃VOgCX^X\zCʌXbhɂȂ
             System.setSecurityManager(MetricsToolSecurityManager.getInstance());
         } catch (final SecurityException e) {
             // ɃZbgĂZLeB}l[WɂāCVȃZLeB}l[W̓o^ȂD
             // VXẽZLeB}l[WƂĎgȂĂCʌXbh̃ANZX͖Ȃ삷̂łƂ肠
             err
                     .println("Failed to set system security manager. MetricsToolsecurityManager works only to manage privilege threads.");
         }
     }
 
     /**
      * [hς݂̃vOCs.
      */
     private static void launchPlugins() {
         PluginLauncher launcher = new DefaultPluginLauncher();
         launcher.setMaximumLaunchingNum(1);
         launcher.launchAll(PluginManager.getInstance().getPlugins());
 
         do {
             try {
                 Thread.sleep(1000);
             } catch (InterruptedException e) {
                 // CɂȂ
             }
         } while (0 < launcher.getCurrentLaunchingNum() + launcher.getLaunchWaitingTaskNum());
 
         launcher.stopLaunching();
     }
 
     /**
      * vOC[h.
      * w肳ꂽCw肳ꂽgNXɊ֘AvOĈ݂ {@link PluginManager}ɓo^.
      * @param language w肵ꂽ.
      */
     private static void loadPlugins(final LANGUAGE language, final String[] metrics) {
         // w茾ɑΉvOCŎw肳ꂽgNXvvOC[hēo^
 
         // metrics[]OȂC2ȏw肳Ă or PǃftHg̕񂶂Ȃ
         boolean metricsSpecified = metrics.length != 0
                 && (1 < metrics.length || !metrics[0].equals(Settings.INIT));
 
         final PluginManager pluginManager = PluginManager.getInstance();
         try {
             for (final AbstractPlugin plugin : (new DefaultPluginLoader()).loadPlugins()) {// vOCS[h
                 final PluginInfo info = plugin.getPluginInfo();
                 if (null == language || info.isMeasurable(language)) {
                     // Ώیꂪw肳ĂȂ or Ώیv\
                     if (metricsSpecified) {
                         // gNXw肳Ă̂ł̃vOCƈv邩`FbN
                         final String pluginMetricName = info.getMetricName();
                         for (final String metric : metrics) {
                             if (metric.equals(pluginMetricName)) {
                                 pluginManager.addPlugin(plugin);
                                 break;
                             }
                         }
                     } else {
                         // gNXw肳ĂȂ̂łƂ肠So^
                         pluginManager.addPlugin(plugin);
                     }
                 }
             }
         } catch (PluginLoadException e) {
             err.println(e.getMessage());
             System.exit(0);
         }
     }
 
     /**
      * 
      * c[̎giR}hCIvVj\D
      * 
      */
     private static void printUsage() {
 
         System.err.println();
         System.err.println("Available options:");
         System.err.println("\t-d: root directory that you are going to analysis.");
         System.err.println("\t-i: List file including file paths that you are going to analysis.");
         System.err.println("\t-l: Programming language of the target files.");
         System.err
                 .println("\t-m: Metrics that you want to get. Metrics names are separated with \',\'.");
         System.err.println("\t-C: File path that the class type metrics are output");
         System.err.println("\t-F: File path that the file type metrics are output.");
         System.err.println("\t-M: File path that the method type metrics are output");
 
         System.err.println();
         System.err.println("Usage:");
         System.err.println("\t<Help Mode>");
         System.err.println("\tMetricsTool -h");
         System.err.println();
         System.err.println("\t<Display Mode>");
         System.err.println("\tMetricsTool -x -l");
         System.err.println("\tMetricsTool -x -l language -m");
         System.err.println();
         System.err.println("\t<Analysis Mode>");
         System.err
                 .println("\tMetricsTool -d directory -l language -m metrics1,metrics2 -C file1 -F file2 -M file3");
         System.err
                 .println("\tMetricsTool -l listFile -l language -m metrics1,metrics2 -C file1 -F file2 -M file3");
     }
 
     /**
      * ͑Ώۃt@Co^
      */
     private static void readTargetFiles() {
 
         // fBNgǂݍ
         if (!Settings.getTargetDirectory().equals(Settings.INIT)) {
             registerFilesFromDirectory();
 
             // Xgt@Cǂݍ
         } else if (!Settings.getListFile().equals(Settings.INIT)) {
             registerFilesFromListFile();
         }
     }
 
     /**
      * 
      * Xgt@CΏۃt@Co^D ǂݍ݃G[ꍇ́C̃\bhŃvOID
      */
     private static void registerFilesFromListFile() {
 
         try {
 
             TargetFileManager targetFiles = TargetFileManager.getInstance();
             for (BufferedReader reader = new BufferedReader(new FileReader(Settings.getListFile())); reader
                     .ready();) {
                 String line = reader.readLine();
                 TargetFile targetFile = new TargetFile(line);
                 targetFiles.add(targetFile);
             }
 
         } catch (FileNotFoundException e) {
             err.println("\"" + Settings.getListFile() + "\" is not a valid file!");
             System.exit(0);
         } catch (IOException e) {
             err.println("\"" + Settings.getListFile() + "\" can\'t read!");
             System.exit(0);
         }
     }
 
     /**
      * 
      * registerFilesFromDirectory(File file)Ăяô݁D main\bh new File(Settings.getTargetDirectory)
      * ̂Cߍ쐬D
      * 
      */
     private static void registerFilesFromDirectory() {
 
         File targetDirectory = new File(Settings.getTargetDirectory());
         registerFilesFromDirectory(targetDirectory);
     }
 
     /**
      * 
      * @param file Ώۃt@C܂̓fBNg
      * 
      * ΏۂfBNg̏ꍇ́C̎qɑ΂čċAIɏD Ώۂt@C̏ꍇ́CΏی̃\[Xt@Cł΁Co^sD
      */
     private static void registerFilesFromDirectory(File file) {
 
         // fBNgȂ΁CċAIɏ
         if (file.isDirectory()) {
             File[] subfiles = file.listFiles();
             for (int i = 0; i < subfiles.length; i++) {
                 registerFilesFromDirectory(subfiles[i]);
             }
 
             // t@CȂ΁CgqΏیƈvΓo^
         } else if (file.isFile()) {
 
             final LANGUAGE language = Settings.getLanguage();
             final String extension = language.getExtension();
             final String path = file.getAbsolutePath();
             if (path.endsWith(extension)) {
                 final TargetFileManager targetFiles = TargetFileManager.getInstance();
                 final TargetFile targetFile = new TargetFile(path);
                 targetFiles.add(targetFile);
             }
 
             // fBNgłt@CłȂꍇ͕s
         } else {
             err.println("\"" + file.getAbsolutePath() + "\" is not a vaild file!");
             System.exit(0);
         }
     }
 
     /**
      * gNXt@Cɏo.
      */
     private static void writeMetrics() {
         if (!Settings.getFileMetricsFile().equals(Settings.INIT)) {
 
             try {
                 FileMetricsInfoManager manager = FileMetricsInfoManager.getInstance();
                 manager.checkMetrics();
 
                 String fileName = Settings.getFileMetricsFile();
                 CSVFileMetricsWriter writer = new CSVFileMetricsWriter(fileName);
                 writer.write();
 
             } catch (MetricNotRegisteredException e) {
                 System.exit(0);
             }
         }
 
         if (!Settings.getClassMetricsFile().equals(Settings.INIT)) {
 
             try {
                 ClassMetricsInfoManager manager = ClassMetricsInfoManager.getInstance();
                 manager.checkMetrics();
 
                 String fileName = Settings.getClassMetricsFile();
                 CSVClassMetricsWriter writer = new CSVClassMetricsWriter(fileName);
                 writer.write();
 
             } catch (MetricNotRegisteredException e) {
                 System.exit(0);
             }
         }
 
         if (!Settings.getMethodMetricsFile().equals(Settings.INIT)) {
 
             try {
                 MethodMetricsInfoManager manager = MethodMetricsInfoManager.getInstance();
                 manager.checkMetrics();
 
                 String fileName = Settings.getMethodMetricsFile();
                 CSVMethodMetricsWriter writer = new CSVMethodMetricsWriter(fileName);
                 writer.write();
 
             } catch (MetricNotRegisteredException e) {
                 System.exit(0);
             }
 
         }
     }
 
     /**
      * ̎dl Jargp ɓn߂̔zD
      */
     private static ParameterDef[] parameterDefs = {
             new BoolDef('h', "helpMode", "display usage", true),
             new BoolDef('x', "displayMode", "display available language or metrics", true),
             new StringDef('d', "targetDirectory", "Target directory"),
             new StringDef('i', "listFile", "List file including paths of target files"),
             new StringDef('l', "language", "Programming language"),
             new StringDef('m', "metrics", "Measured metrics"),
             new StringDef('F', "fileMetricsFile", "File storing file metrics"),
             new StringDef('C', "classMetricsFile", "File storing class metrics"),
             new StringDef('M', "methodMetricsFile", "File storing method metrics") };
 
     /**
      * o̓bZ[Wo͗p̃v^
      */
     private static final MessagePrinter out = new DefaultMessagePrinter(new MessageSource() {
         public String getMessageSourceName() {
             return "main";
         }
     }, MESSAGE_TYPE.OUT);
 
     /**
      * G[bZ[Wo͗p̃v^
      */
     private static final MessagePrinter err = new DefaultMessagePrinter(new MessageSource() {
         public String getMessageSourceName() {
             return "main";
         }
     }, MESSAGE_TYPE.ERROR);
 
     /**
      * NX̒` ClassInfoManager ɓo^DAST p[X̌ɌĂяoȂ΂ȂȂD
      */
     private static void registClassInfos() {
 
         // Unresolved NX}l[WC NX}l[W擾
         final UnresolvedClassInfoManager unresolvedClassInfoManager = UnresolvedClassInfoManager
                 .getInstance();
         final ClassInfoManager classInfoManager = ClassInfoManager.getInstance();
 
         // e UnresolvedNXɑ΂
         for (UnresolvedClassInfo unresolvedClassInfo : unresolvedClassInfoManager.getClassInfos()) {
 
             // CqCS薼CsCCCX^Xo[ǂ擾
             final Set<ModifierInfo> modifiers = unresolvedClassInfo.getModifiers();
             final String[] fullQualifiedName = unresolvedClassInfo.getFullQualifiedName();
             final int loc = unresolvedClassInfo.getLOC();
             final boolean privateVisible = unresolvedClassInfo.isPrivateVisible();
             final boolean namespaceVisible = unresolvedClassInfo.isNamespaceVisible();
             final boolean inheritanceVisible = unresolvedClassInfo.isInheritanceVisible();
             final boolean publicVisible = unresolvedClassInfo.isPublicVisible();
             final boolean instance = unresolvedClassInfo.isInstanceMember();
             final int fromLine = unresolvedClassInfo.getFromLine();
             final int fromColumn = unresolvedClassInfo.getFromColumn();
             final int toLine = unresolvedClassInfo.getToLine();
             final int toColumn = unresolvedClassInfo.getToColumn();
 
             // ClassInfo IuWFNg쐬CClassInfoManagerɓo^
             final TargetClassInfo classInfo = new TargetClassInfo(modifiers, fullQualifiedName,
                     loc, privateVisible, namespaceVisible, inheritanceVisible, publicVisible,
                     instance, fromLine, fromColumn, toLine, toColumn);
             classInfoManager.add(classInfo);
 
             for (UnresolvedClassInfo unresolvedInnerClassInfo : unresolvedClassInfo
                     .getInnerClasses()) {
                 final TargetInnerClassInfo innerClass = registInnerClassInfo(
                         unresolvedInnerClassInfo, classInfo, classInfoManager);
                 classInfo.addInnerClass(innerClass);
             }
         }
     }
 
     /**
      * Ci[NX̒` ClassInfoManager ɓo^D registClassInfos ̂݌Ă΂ׂłD
      * 
      * @param unresolvedClassInfo OCi[NXIuWFNg
      * @param outerClass ÕNX
      * @param classInfoManager Ci[NXo^NX}l[W
      * @return Ci[NX ClassInfo
      */
     private static TargetInnerClassInfo registInnerClassInfo(
             final UnresolvedClassInfo unresolvedClassInfo, final TargetClassInfo outerClass,
             final ClassInfoManager classInfoManager) {
 
         // CqCS薼CsC擾
         final Set<ModifierInfo> modifiers = unresolvedClassInfo.getModifiers();
         final String[] fullQualifiedName = unresolvedClassInfo.getFullQualifiedName();
         final int loc = unresolvedClassInfo.getLOC();
         final boolean privateVisible = unresolvedClassInfo.isPrivateVisible();
         final boolean namespaceVisible = unresolvedClassInfo.isNamespaceVisible();
         final boolean inheritanceVisible = unresolvedClassInfo.isInheritanceVisible();
         final boolean publicVisible = unresolvedClassInfo.isPublicVisible();
         final boolean instance = unresolvedClassInfo.isInstanceMember();
         final int fromLine = unresolvedClassInfo.getFromLine();
         final int fromColumn = unresolvedClassInfo.getFromColumn();
         final int toLine = unresolvedClassInfo.getToLine();
         final int toColumn = unresolvedClassInfo.getToColumn();
 
         // ClassInfo IuWFNg𐶐CClassInfo}l[Wɓo^
         TargetInnerClassInfo classInfo = new TargetInnerClassInfo(modifiers, fullQualifiedName,
                 outerClass, loc, privateVisible, namespaceVisible, inheritanceVisible,
                 publicVisible, instance, fromLine, fromColumn, toLine, toColumn);
         classInfoManager.add(classInfo);
 
         // ̃NX̃Ci[NXɑ΂čċAIɏ
         for (UnresolvedClassInfo unresolvedInnerClassInfo : unresolvedClassInfo.getInnerClasses()) {
             final TargetInnerClassInfo innerClass = registInnerClassInfo(unresolvedInnerClassInfo,
                     classInfo, classInfoManager);
             classInfo.addInnerClass(innerClass);
         }
 
         // ̃NX ClassInfo Ԃ
         return classInfo;
     }
 
     /**
      * NX̌p ClassInfo ɒǉDxڂ AST p[X̌C registClassInfos ̌ɂтȂ΂ȂȂD
      */
     private static void addInheritanceInformationToClassInfos() {
 
         // Unresolved NX}l[WC NX}l[W擾
         final UnresolvedClassInfoManager unresolvedClassInfoManager = UnresolvedClassInfoManager
                 .getInstance();
         final ClassInfoManager classInfoManager = ClassInfoManager.getInstance();
 
         // e UnresolvedNXɑ΂
         for (UnresolvedClassInfo unresolvedClassInfo : unresolvedClassInfoManager.getClassInfos()) {
 
             // ClassInfo 擾
             final ClassInfo classInfo = NameResolver.resolveClassInfo(unresolvedClassInfo,
                     classInfoManager);
 
             // eUnresolvedȐeNXɑ΂
             for (UnresolvedTypeInfo unresolvedSuperClassType : unresolvedClassInfo
                     .getSuperClasses()) {
                 ClassInfo superClass = (ClassInfo) NameResolver.resolveTypeInfo(
                         unresolvedSuperClassType, classInfoManager);
 
                 // Ȃꍇ͖OԖUNKNOWNȃNXo^
                 if (null == superClass) {
                     superClass = NameResolver
                             .createExternalClassInfo((UnresolvedReferenceTypeInfo) unresolvedSuperClassType);
                     classInfoManager.add((ExternalClassInfo) superClass);
                 }
 
                 classInfo.addSuperClass(superClass);
                 superClass.addSubClass(classInfo);
             }
         }
     }
 
     /**
      * tB[h̒` FieldInfoManager ɓo^D registClassInfos ̌ɌĂяoȂ΂ȂȂ
      * 
      */
     private static void registFieldInfos() {
 
         // Unresolved NX}l[WCNX}l[WCtB[h}l[W擾
         final UnresolvedClassInfoManager unresolvedClassInfoManager = UnresolvedClassInfoManager
                 .getInstance();
         final ClassInfoManager classInfoManager = ClassInfoManager.getInstance();
         final FieldInfoManager fieldInfoManager = FieldInfoManager.getInstance();
 
         // e UnresolvedNXɑ΂
         for (UnresolvedClassInfo unresolvedClassInfo : unresolvedClassInfoManager.getClassInfos()) {
 
             // ClassInfo 擾
             final ClassInfo ownerClass = NameResolver.resolveClassInfo(unresolvedClassInfo,
                     classInfoManager);
             if (!(ownerClass instanceof TargetClassInfo)) {
                 throw new IllegalArgumentException(ownerClass.toString()
                         + " must be an instance of TargetClassInfo!");
             }
 
             // UnresolvedNXɒ`ĂeUnresolvedtB[hɑ΂
             for (UnresolvedFieldInfo unresolvedFieldInfo : unresolvedClassInfo.getDefinedFields()) {
 
                 // CqCOC^CCCX^Xo[ǂ擾
                 final Set<ModifierInfo> modifiers = unresolvedFieldInfo.getModifiers();
                 final String fieldName = unresolvedFieldInfo.getName();
                 final UnresolvedTypeInfo unresolvedFieldType = unresolvedFieldInfo.getType();
                 TypeInfo fieldType = NameResolver.resolveTypeInfo(unresolvedFieldType,
                         classInfoManager);
                 if (null == fieldType) {
                     if (unresolvedFieldType instanceof UnresolvedReferenceTypeInfo) {
                         fieldType = NameResolver
                                 .createExternalClassInfo((UnresolvedReferenceTypeInfo) unresolvedFieldType);
                         classInfoManager.add((ExternalClassInfo) fieldType);
                     } else if (unresolvedFieldType instanceof UnresolvedArrayTypeInfo) {
                         final UnresolvedTypeInfo unresolvedElementType = ((UnresolvedArrayTypeInfo) unresolvedFieldType)
                                 .getElementType();
                         final int dimension = ((UnresolvedArrayTypeInfo) unresolvedFieldType)
                                 .getDimension();
                         final TypeInfo elementType = NameResolver
                                 .createExternalClassInfo((UnresolvedReferenceTypeInfo) unresolvedElementType);
                         classInfoManager.add((ExternalClassInfo) elementType);
                         fieldType = ArrayTypeInfo.getType(elementType, dimension);
                     }
                 }
                 final boolean privateVisible = unresolvedFieldInfo.isPrivateVisible();
                 final boolean namespaceVisible = unresolvedFieldInfo.isNamespaceVisible();
                 final boolean inheritanceVisible = unresolvedFieldInfo.isInheritanceVisible();
                 final boolean publicVisible = unresolvedFieldInfo.isPublicVisible();
                 final boolean instance = unresolvedFieldInfo.isInstanceMember();
                 final int fromLine = unresolvedFieldInfo.getFromLine();
                 final int fromColumn = unresolvedFieldInfo.getFromColumn();
                 final int toLine = unresolvedFieldInfo.getToLine();
                 final int toColumn = unresolvedFieldInfo.getToColumn();
 
                 // tB[hIuWFNg𐶐
                 final TargetFieldInfo fieldInfo = new TargetFieldInfo(modifiers, fieldName,
                         fieldType, ownerClass, privateVisible, namespaceVisible,
                         inheritanceVisible, publicVisible, instance, fromLine, fromColumn, toLine,
                         toColumn);
 
                 // tB[hǉ
                 ((TargetClassInfo) ownerClass).addDefinedField(fieldInfo);
                 fieldInfoManager.add(fieldInfo);
             }
         }
     }
 
     /**
      * \bh̒` MethodInfoManager ɓo^DregistClassInfos ̌ɌĂяoȂ΂ȂȂD
      */
     private static void registMethodInfos() {
 
         // Unresolved NX}l[WC NX}l[WC\bh}l[W擾
         final UnresolvedClassInfoManager unresolvedClassInfoManager = UnresolvedClassInfoManager
                 .getInstance();
         final ClassInfoManager classInfoManager = ClassInfoManager.getInstance();
         final MethodInfoManager methodInfoManager = MethodInfoManager.getInstance();
 
         // e UnresolvedNXɑ΂
         for (UnresolvedClassInfo unresolvedClassInfo : unresolvedClassInfoManager.getClassInfos()) {
 
             // ClassInfo 擾
             final ClassInfo ownerClass = NameResolver.resolveClassInfo(unresolvedClassInfo,
                     classInfoManager);
             if (!(ownerClass instanceof TargetClassInfo)) {
                 throw new IllegalArgumentException(ownerClass.toString()
                         + " must be an instance of TargetClassInfo");
             }
 
             // UnresolvedNXɒ`Ăe\bhɑ΂
             for (UnresolvedMethodInfo unresolvedMethodInfo : unresolvedClassInfo
                     .getDefinedMethods()) {
 
                 // CqCOCԂlCsCRXgN^ǂCCCX^Xo[ǂ擾
                 final Set<ModifierInfo> methodModifiers = unresolvedMethodInfo.getModifiers();
                 final String methodName = unresolvedMethodInfo.getMethodName();
                 final UnresolvedTypeInfo unresolvedMethodReturnType = unresolvedMethodInfo
                         .getReturnType();
                 TypeInfo methodReturnType = NameResolver.resolveTypeInfo(
                         unresolvedMethodReturnType, classInfoManager);
                 if (null == methodReturnType) {
                     if (unresolvedMethodReturnType instanceof UnresolvedReferenceTypeInfo) {
                         methodReturnType = NameResolver
                                 .createExternalClassInfo((UnresolvedReferenceTypeInfo) unresolvedMethodReturnType);
                         classInfoManager.add((ExternalClassInfo) methodReturnType);
                     } else if (unresolvedMethodReturnType instanceof UnresolvedArrayTypeInfo) {
                         final UnresolvedTypeInfo unresolvedElementType = ((UnresolvedArrayTypeInfo) unresolvedMethodReturnType)
                                 .getElementType();
                         final int dimension = ((UnresolvedArrayTypeInfo) unresolvedMethodReturnType)
                                 .getDimension();
                         final TypeInfo elementType = NameResolver
                                 .createExternalClassInfo((UnresolvedReferenceTypeInfo) unresolvedElementType);
                         classInfoManager.add((ExternalClassInfo) elementType);
                         methodReturnType = ArrayTypeInfo.getType(elementType, dimension);
                     }
                 }
                 final int methodLOC = unresolvedMethodInfo.getLOC();
                 final boolean constructor = unresolvedMethodInfo.isConstructor();
                 final boolean privateVisible = unresolvedMethodInfo.isPrivateVisible();
                 final boolean namespaceVisible = unresolvedMethodInfo.isNamespaceVisible();
                 final boolean inheritanceVisible = unresolvedMethodInfo.isInheritanceVisible();
                 final boolean publicVisible = unresolvedMethodInfo.isPublicVisible();
                 final boolean instance = unresolvedMethodInfo.isInstanceMember();
                 final int methodFromLine = unresolvedMethodInfo.getFromLine();
                 final int methodFromColumn = unresolvedMethodInfo.getFromColumn();
                 final int methodToLine = unresolvedMethodInfo.getToLine();
                 final int methodToColumn = unresolvedMethodInfo.getToColumn();
 
                 // MethodInfo IuWFNg𐶐CǉĂ
                 final TargetMethodInfo methodInfo = new TargetMethodInfo(methodModifiers,
                         methodName, methodReturnType, ownerClass, constructor, methodLOC,
                         privateVisible, namespaceVisible, inheritanceVisible, publicVisible,
                         instance, methodFromLine, methodFromColumn, methodToLine, methodToColumn);
                 for (UnresolvedParameterInfo unresolvedParameterInfo : unresolvedMethodInfo
                         .getParameterInfos()) {
 
                     // CqCp[^C^Cʒu擾
                     final Set<ModifierInfo> parameterModifiers = unresolvedParameterInfo
                             .getModifiers();
                     final String parameterName = unresolvedParameterInfo.getName();
                     final UnresolvedTypeInfo unresolvedParameterType = unresolvedParameterInfo
                             .getType();
                     TypeInfo parameterType = NameResolver.resolveTypeInfo(unresolvedParameterType,
                             classInfoManager);
                     if (null == parameterType) {
                         if (unresolvedParameterType instanceof UnresolvedReferenceTypeInfo) {
                             parameterType = NameResolver
                                     .createExternalClassInfo((UnresolvedReferenceTypeInfo) unresolvedParameterType);
                             classInfoManager.add((ExternalClassInfo) parameterType);
                         } else if (unresolvedParameterType instanceof UnresolvedArrayTypeInfo) {
                             final UnresolvedTypeInfo unresolvedElementType = ((UnresolvedArrayTypeInfo) unresolvedParameterType)
                                     .getElementType();
                             final int dimension = ((UnresolvedArrayTypeInfo) unresolvedParameterType)
                                     .getDimension();
                             final TypeInfo elementType = NameResolver
                                     .createExternalClassInfo((UnresolvedReferenceTypeInfo) unresolvedElementType);
                             classInfoManager.add((ExternalClassInfo) elementType);
                             parameterType = ArrayTypeInfo.getType(elementType, dimension);
                         }
                     }
                     final int parameterFromLine = unresolvedParameterInfo.getFromLine();
                     final int parameterFromColumn = unresolvedParameterInfo.getFromColumn();
                     final int parameterToLine = unresolvedParameterInfo.getToLine();
                     final int parameterToColumn = unresolvedParameterInfo.getToColumn();
 
                     // p[^IuWFNg𐶐C\bhɒǉ
                     final TargetParameterInfo parameterInfo = new TargetParameterInfo(
                             parameterModifiers, parameterName, parameterType, parameterFromLine,
                             parameterFromColumn, parameterToLine, parameterToColumn);
                     methodInfo.addParameter(parameterInfo);
                 }
 
                 // \bhŒ`Ăe[Jϐɑ΂
                 for (UnresolvedLocalVariableInfo unresolvedLocalVariable : unresolvedMethodInfo
                         .getLocalVariables()) {
 
                     // CqCϐC^擾
                     final Set<ModifierInfo> localModifiers = unresolvedLocalVariable.getModifiers();
                     final String variableName = unresolvedLocalVariable.getName();
                     final UnresolvedTypeInfo unresolvedVariableType = unresolvedLocalVariable
                             .getType();
                    TypeInfo variableType = NameResolver.resolveTypeInfo(
                             unresolvedVariableType, classInfoManager);
                    if (null == variableType) {
                        if (unresolvedVariableType instanceof UnresolvedReferenceTypeInfo) {
                            variableType = NameResolver
                                    .createExternalClassInfo((UnresolvedReferenceTypeInfo) unresolvedVariableType);
                            classInfoManager.add((ExternalClassInfo) variableType);
                        } else if (unresolvedVariableType instanceof UnresolvedArrayTypeInfo) {
                            final UnresolvedTypeInfo unresolvedElementType = ((UnresolvedArrayTypeInfo) unresolvedVariableType)
                                    .getElementType();
                            final int dimension = ((UnresolvedArrayTypeInfo) unresolvedVariableType)
                                    .getDimension();
                            final TypeInfo elementType = NameResolver
                                    .createExternalClassInfo((UnresolvedReferenceTypeInfo) unresolvedElementType);
                            classInfoManager.add((ExternalClassInfo) elementType);
                            variableType = ArrayTypeInfo.getType(elementType, dimension);
                        }
                    }
                     final int localFromLine = unresolvedLocalVariable.getFromLine();
                     final int localFromColumn = unresolvedLocalVariable.getFromColumn();
                     final int localToLine = unresolvedLocalVariable.getToLine();
                     final int localToColumn = unresolvedLocalVariable.getToColumn();
 
                     // [JϐIuWFNg𐶐CMethodInfoɒǉ
                     final LocalVariableInfo localVariable = new LocalVariableInfo(localModifiers,
                             variableName, variableType, localFromLine, localFromColumn,
                             localToLine, localToColumn);
                     methodInfo.addLocalVariable(localVariable);
                 }
 
                 // \bhǉ
                 ((TargetClassInfo) ownerClass).addDefinedMethod(methodInfo);
                 methodInfoManager.add(methodInfo);
             }
         }
 
     }
 
     /**
      * \bhI[o[CheMethodInfoɒǉDaddInheritanceInfomationToClassInfos ̌  registMethodInfos
      * ̌ɌĂяoȂ΂ȂȂ
      */
     private static void addOverrideRelation() {
 
         // SĂ̑ΏۃNXɑ΂
         for (TargetClassInfo classInfo : ClassInfoManager.getInstance().getTargetClassInfos()) {
 
             // eΏۃNX̐eNXɑ΂
             for (ClassInfo superClassInfo : classInfo.getSuperClasses()) {
 
                 // eΏۃNX̊e\bhɂāCeNX̃\bhI[xĂ邩𒲍
                 for (MethodInfo methodInfo : classInfo.getDefinedMethods()) {
                     addOverrideRelation(superClassInfo, methodInfo);
                 }
             }
         }
     }
 
     /**
      * \bhI[o[ChǉDŎw肳ꂽNXŒ`Ă郁\bhɑ΂đs.
      * AddOverrideInformationToMethodInfos()̒̂݌ĂяoD
      * 
      * @param classInfo NX
      * @param overrider I[o[ChΏۂ̃\bh
      */
     private static void addOverrideRelation(final ClassInfo classInfo, final MethodInfo overrider) {
 
         if ((null == classInfo) || (null == overrider)) {
             throw new NullPointerException();
         }
 
         if (!(classInfo instanceof TargetClassInfo)) {
             return;
         }
 
         for (TargetMethodInfo methodInfo : ((TargetClassInfo) classInfo).getDefinedMethods()) {
 
             if (methodInfo.isSameSignature(overrider)) {
                 overrider.addOverridee(methodInfo);
                 methodInfo.addOverrider(overrider);
 
                 // ڂ̃I[o[Ch֌WoȂ̂ŁC̃NX̐eNX͒Ȃ
                 return;
             }
         }
 
         // eNXQɑ΂čċAIɏ
         for (ClassInfo superClassInfo : classInfo.getSuperClasses()) {
             addOverrideRelation(superClassInfo, overrider);
         }
     }
 
     /**
      * GeBeBitB[hNXj̑EQƁC\bȟĂяo֌WǉD
      */
     private static void addReferenceAssignmentCallRelateion() {
 
         final UnresolvedClassInfoManager unresolvedClassInfoManager = UnresolvedClassInfoManager
                 .getInstance();
         final ClassInfoManager classInfoManager = ClassInfoManager.getInstance();
         final FieldInfoManager fieldInfoManager = FieldInfoManager.getInstance();
         final MethodInfoManager methodInfoManager = MethodInfoManager.getInstance();
         final Map<UnresolvedTypeInfo, TypeInfo> resolvedCache = new HashMap<UnresolvedTypeInfo, TypeInfo>();
 
         // eNX ɑ΂
         for (UnresolvedClassInfo unresolvedClassInfo : unresolvedClassInfoManager.getClassInfos()) {
 
             // NX񂩂Cς݃NX擾
             final TargetClassInfo userClass = NameResolver.resolveClassInfo(unresolvedClassInfo,
                     classInfoManager);
 
             // e\bhɑ΂
             for (UnresolvedMethodInfo unresolvedMethodInfo : unresolvedClassInfo
                     .getDefinedMethods()) {
 
                 // \bh񂩂Cς݃\bh擾
                 final TargetMethodInfo caller = NameResolver.resolveMethodInfo(
                         unresolvedMethodInfo, classInfoManager);
 
                 // eQƃGeBeB̖O
                 for (UnresolvedFieldUsage referencee : unresolvedMethodInfo.getFieldReferences()) {
 
                     // QƏ
                     NameResolver.resolveFieldReference(referencee, userClass, caller,
                             classInfoManager, fieldInfoManager, methodInfoManager, resolvedCache);
                 }
 
                 // GeBeB̖O
                 for (UnresolvedFieldUsage assignmentee : unresolvedMethodInfo.getFieldAssignments()) {
 
                     // 
                     NameResolver.resolveFieldAssignment(assignmentee, userClass, caller,
                             classInfoManager, fieldInfoManager, methodInfoManager, resolvedCache);
                 }
 
                 // e\bhĂяỏ
                 for (UnresolvedMethodCall methodCall : unresolvedMethodInfo.getMethodCalls()) {
 
                     // e\bhĂяo
                     NameResolver.resolveMethodCall(methodCall, userClass, caller, classInfoManager,
                             fieldInfoManager, methodInfoManager, resolvedCache);
 
                 }
             }
         }
     }
 }
