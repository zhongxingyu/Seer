 package jp.ac.osaka_u.ist.sel.metricstool.main;
 
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.lang.reflect.Field;
 import java.util.Collections;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 
 import jp.ac.osaka_u.ist.sel.metricstool.main.ast.csharp.CSharpAntlrAstTranslator;
 import jp.ac.osaka_u.ist.sel.metricstool.main.ast.databuilder.ASTParseException;
 import jp.ac.osaka_u.ist.sel.metricstool.main.ast.java.Java13AntlrAstTranslator;
 import jp.ac.osaka_u.ist.sel.metricstool.main.ast.java.Java14AntlrAstTranslator;
 import jp.ac.osaka_u.ist.sel.metricstool.main.ast.java.Java15AntlrAstTranslator;
 import jp.ac.osaka_u.ist.sel.metricstool.main.ast.java.JavaAstVisitorManager;
 import jp.ac.osaka_u.ist.sel.metricstool.main.ast.visitor.AstVisitorManager;
 import jp.ac.osaka_u.ist.sel.metricstool.main.ast.visitor.antlr.AntlrAstVisitor;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.metric.ClassMetricsInfoManager;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.metric.FieldMetricsInfoManager;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.metric.FileMetricsInfoManager;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.metric.MethodMetricsInfoManager;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.metric.MetricNotRegisteredException;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.BlockInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.CallableUnitInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.ClassInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.ClassInfoManager;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.ClassTypeInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.ConditionalBlockInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.ConstructorCallInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.EntityUsageInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.ExpressionInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.FieldInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.FieldInfoManager;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.FieldUsageInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.FileInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.FileInfoManager;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.LocalSpaceInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.MethodCallInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.MethodInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.MethodInfoManager;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.StatementInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.TargetClassInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.TargetConstructorInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.TargetFile;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.TargetFileManager;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.TargetInnerClassInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.TargetMethodInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.TypeInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.TypeParameterInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.UnknownTypeInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.VariableUsageInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.external.ExternalClassInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.unresolved.UnresolvedBlockInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.unresolved.UnresolvedCallInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.unresolved.UnresolvedClassInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.unresolved.UnresolvedClassInfoManager;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.unresolved.UnresolvedClassTypeInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.unresolved.UnresolvedConditionalBlockInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.unresolved.UnresolvedConstructorInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.unresolved.UnresolvedFieldInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.unresolved.UnresolvedLocalSpaceInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.unresolved.UnresolvedMethodInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.unresolved.UnresolvedStatementInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.unresolved.UnresolvedTypeParameterInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.unresolved.UnresolvedVariableUsageInfo;
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
 import jp.ac.osaka_u.ist.sel.metricstool.main.parse.CSharpLexer;
 import jp.ac.osaka_u.ist.sel.metricstool.main.parse.CSharpParser;
 import jp.ac.osaka_u.ist.sel.metricstool.main.parse.CommonASTWithLineNumber;
 import jp.ac.osaka_u.ist.sel.metricstool.main.parse.Java14Lexer;
 import jp.ac.osaka_u.ist.sel.metricstool.main.parse.Java14Parser;
 import jp.ac.osaka_u.ist.sel.metricstool.main.parse.Java15Lexer;
 import jp.ac.osaka_u.ist.sel.metricstool.main.parse.Java15Parser;
 import jp.ac.osaka_u.ist.sel.metricstool.main.parse.MasuAstFactory;
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
 
 import antlr.ASTFactory;
 import antlr.RecognitionException;
 import antlr.TokenStreamException;
 import antlr.collections.AST;
 
 
 /**
  * 
  * @author higo
  * 
  * MetricsTool̃CNXD ݂͉D
  * 
  * since 2006.11.12
  * 
  */
 public class MetricsTool {
 
     /**
      * 
      * @param args Ώۃt@C̃t@CpX
      * 
      * ݉D Ώۃt@C̃f[^i[C\͂sD
      */
     public static void main(String[] args) {
 
         MetricsTool metricsTool = new MetricsTool();
 
         ArgumentProcessor.processArgs(args, parameterDefs, new Settings());
 
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
 
         // wv[hƏ\[hɃIɂȂĂꍇ͕s
         if (Settings.isHelpMode() && Settings.isDisplayMode()) {
             err.println("-h and -x can\'t be set at the same time!");
             metricsTool.printUsage();
             System.exit(0);
         }
 
         if (Settings.isHelpMode()) {
             // wv[h̏ꍇ
             metricsTool.doHelpMode();
         } else {
             metricsTool.loadPlugins(Settings.getMetricStrings());
 
             if (Settings.isDisplayMode()) {
                 // \[h̏ꍇ
                 metricsTool.doDisplayMode();
             } else {
                 // ̓[h
                 metricsTool.doAnalysisMode();
             }
         }
     }
 
     /**
      * RXgN^D ZLeB}l[W̏sD
      */
     public MetricsTool() {
         initSecurityManager();
     }
 
     /**
      * {@link #readTargetFiles()} œǂݍ񂾑Ώۃt@CQ͂.
      * 
      */
     public void analyzeTargetFiles() {
         // Ώۃt@C
 
         AstVisitorManager<AST> visitorManager = null;
 
         switch (Settings.getLanguage()) {
         case JAVA15:
             visitorManager = new JavaAstVisitorManager<AST>(new AntlrAstVisitor(
                     new Java15AntlrAstTranslator()));
             break;
         case JAVA14:
             visitorManager = new JavaAstVisitorManager<AST>(new AntlrAstVisitor(
                     new Java14AntlrAstTranslator()));
             break;
         case JAVA13:
             visitorManager = new JavaAstVisitorManager<AST>(new AntlrAstVisitor(
                     new Java13AntlrAstTranslator()));
             break;
         case CSHARP:
             visitorManager = new JavaAstVisitorManager<AST>(new AntlrAstVisitor(
                     new CSharpAntlrAstTranslator()));
             break;
         default:
             assert false : "here shouldn't be reached!";
         }
 
         // Ώۃt@CAST疢NXCtB[hC\bh擾
         {
             out.println("parsing all target files.");
             final int totalFileNumber = TargetFileManager.getInstance().size();
             int currentFileNumber = 1;
             final StringBuffer fileInformationBuffer = new StringBuffer();
 
             for (TargetFile targetFile : TargetFileManager.getInstance()) {
                 try {
                     final String name = targetFile.getName();
 
                     final FileInfo fileInfo = new FileInfo(name);
                     FileInfoManager.getInstance().add(fileInfo);
 
                     if (Settings.isVerbose()) {
                         fileInformationBuffer.delete(0, fileInformationBuffer.length());
                         fileInformationBuffer.append("parsing ");
                         fileInformationBuffer.append(name);
                         fileInformationBuffer.append(" [");
                         fileInformationBuffer.append(currentFileNumber++);
                         fileInformationBuffer.append("/");
                         fileInformationBuffer.append(totalFileNumber);
                         fileInformationBuffer.append("]");
                         out.println(fileInformationBuffer.toString());
                     }
 
                     switch (Settings.getLanguage()) {
                     case JAVA15:
                         final Java15Lexer java15lexer = new Java15Lexer(new FileInputStream(name));
                         java15lexer.setTabSize(1);
                         final Java15Parser java15parser = new Java15Parser(java15lexer);
 
                         final ASTFactory java15factory = new MasuAstFactory();
                         java15factory.setASTNodeClass(CommonASTWithLineNumber.class);
 
                         java15parser.setASTFactory(java15factory);
 
                         java15parser.compilationUnit();
                         targetFile.setCorrectSytax(true);
 
                         if (visitorManager != null) {
                             visitorManager.visitStart(java15parser.getAST());
                         }
 
                         fileInfo.setLOC(java15lexer.getLine());
                         break;
 
                     case JAVA14:
                         final Java14Lexer java14lexer = new Java14Lexer(new FileInputStream(name));
                         java14lexer.setTabSize(1);
                         final Java14Parser java14parser = new Java14Parser(java14lexer);
 
                         final ASTFactory java14factory = new MasuAstFactory();
                         java14factory.setASTNodeClass(CommonASTWithLineNumber.class);
 
                         java14parser.setASTFactory(java14factory);
 
                         java14parser.compilationUnit();
                         targetFile.setCorrectSytax(true);
 
                         if (visitorManager != null) {
                             visitorManager.visitStart(java14parser.getAST());
                         }
 
                         fileInfo.setLOC(java14lexer.getLine());
                         break;
                     case CSHARP:
                         final CSharpLexer csharpLexer = new CSharpLexer(new FileInputStream(name));
                         csharpLexer.setTabSize(1);
                         final CSharpParser csharpParser = new CSharpParser(csharpLexer);
 
                         final ASTFactory cshaprFactory = new MasuAstFactory();
                         cshaprFactory.setASTNodeClass(CommonASTWithLineNumber.class);
 
                         csharpParser.setASTFactory(cshaprFactory);
 
                         csharpParser.compilationUnit();
                         targetFile.setCorrectSytax(true);
 
                         if (visitorManager != null) {
                             visitorManager.visitStart(csharpParser.getAST());
                         }
 
                         fileInfo.setLOC(csharpLexer.getLine());
                         break;
                     default:
                         assert false : "here shouldn't be reached!";
                     }
 
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
                 } catch (ASTParseException e) {
                     err.println(e.getMessage());
                 }
             }
         }
 
         out.println("resolving definitions and usages.");
         if (Settings.isVerbose()) {
             out.println("STEP1 : resolve class definitions.");
         }
         registClassInfos();
         if (Settings.isVerbose()) {
             out.println("STEP2 : resolve type parameters of classes.");
         }
         resolveTypeParameterOfClassInfos();
         if (Settings.isVerbose()) {
             out.println("STEP3 : resolve class inheritances.");
         }
         addInheritanceInformationToClassInfos();
         if (Settings.isVerbose()) {
             out.println("STEP4 : resolve field definitions.");
         }
         registFieldInfos();
         if (Settings.isVerbose()) {
             out.println("STEP5 : resolve method definitions.");
         }
         registMethodInfos();
         if (Settings.isVerbose()) {
             out.println("STEP6 : resolve method overrides.");
         }
         addOverrideRelation();
         if (Settings.isVerbose()) {
             out.println("STEP7 : resolve field and method usages.");
         }
         addReferenceAssignmentCallRelateion();
 
         // @̂t@Cꗗ\
         // err.println("The following files includes uncorrect syntax.");
         // err.println("Any metrics of them were not measured");
         for (TargetFile targetFile : TargetFileManager.getInstance()) {
             if (!targetFile.isCorrectSyntax()) {
                 err.println("Incorrect syntax file: " + targetFile.getName());
             }
         }
 
         out.println("finished.");
 
         {
             /*
              * for (final ClassInfo classInfo :
              * ClassInfoManager.getInstance().getExternalClassInfos()) {
              * out.println(classInfo.getFullQualifiedName(Settings.getLanguage()
              * .getNamespaceDelimiter())); }
              */
         }
     }
 
     /**
      * Ώی擾.
      * 
      * @return w肳ꂽΏی.w肳Ȃꍇnull
      */
     public LANGUAGE getLanguage() {
         if (Settings.getLanguageString().equals(Settings.INIT)) {
             return null;
         }
 
         return Settings.getLanguage();
     }
 
     /**
      * vOC[h. w肳ꂽCw肳ꂽgNXɊ֘AvOĈ݂ {@link PluginManager}ɓo^.
      * 
      * @param metrics w肷郁gNX̔zCw肵Ȃꍇnull܂͋̔z
      */
     public void loadPlugins(final String[] metrics) {
         // w茾ɑΉvOCŎw肳ꂽgNXvvOC[hēo^
 
         // metrics[]OȂC2ȏw肳Ă or PǃftHg̕񂶂Ȃ
         boolean metricsSpecified = null != metrics && metrics.length != 0
                 && (1 < metrics.length || !metrics[0].equals(Settings.INIT));
 
         final PluginManager pluginManager = PluginManager.getInstance();
         try {
             for (final AbstractPlugin plugin : (new DefaultPluginLoader()).loadPlugins()) {// vOCS[h
                 final PluginInfo info = plugin.getPluginInfo();
                 if (info.isMeasurable(Settings.getLanguage())) {
                     // Ώیꂪw肳ĂȂ or Ώیv\
                     if (metricsSpecified) {
                         // gNXw肳Ă̂ł̃vOCƈv邩`FbN
                         final String pluginMetricName = info.getMetricName();
                         for (final String metric : metrics) {
                             if (metric.equalsIgnoreCase(pluginMetricName)) {
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
      * [hς݂̃vOCs.
      */
     public void launchPlugins() {
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
      * {@link Settings}Ɏw肳ꂽꏊ͑Ώۃt@Cǂݍœo^
      */
     public void readTargetFiles() {
 
         out.println("building target file list.");
 
         // fBNgǂݍ
         if (!Settings.getTargetDirectory().equals(Settings.INIT)) {
             registerFilesFromDirectory();
 
             // Xgt@Cǂݍ
         } else if (!Settings.getListFile().equals(Settings.INIT)) {
             registerFilesFromListFile();
         }
     }
 
     /**
      * gNX {@link Settings} Ɏw肳ꂽt@Cɏo͂.
      */
     public void writeMetrics() {
 
         // t@CgNXvꍇ
         if (0 < PluginManager.getInstance().getFileMetricPlugins().size()) {
 
             try {
                 FileMetricsInfoManager manager = FileMetricsInfoManager.getInstance();
                 manager.checkMetrics();
 
                 String fileName = Settings.getFileMetricsFile();
                 CSVFileMetricsWriter writer = new CSVFileMetricsWriter(fileName);
                 writer.write();
 
             } catch (MetricNotRegisteredException e) {
                 err.println(e.getMessage());
                 err.println("File metrics can't be output!");
             }
         }
 
         // NXgNXvꍇ
         if (!Settings.getClassMetricsFile().equals(Settings.INIT)) {
 
             try {
                 ClassMetricsInfoManager manager = ClassMetricsInfoManager.getInstance();
                 manager.checkMetrics();
 
                 String fileName = Settings.getClassMetricsFile();
                 CSVClassMetricsWriter writer = new CSVClassMetricsWriter(fileName);
                 writer.write();
 
             } catch (MetricNotRegisteredException e) {
                 err.println(e.getMessage());
                 err.println("Class metrics can't be output!");
             }
         }
 
         // \bhgNXvꍇ
         if (!Settings.getMethodMetricsFile().equals(Settings.INIT)) {
 
             try {
                 MethodMetricsInfoManager manager = MethodMetricsInfoManager.getInstance();
                 manager.checkMetrics();
 
                 String fileName = Settings.getMethodMetricsFile();
                 CSVMethodMetricsWriter writer = new CSVMethodMetricsWriter(fileName);
                 writer.write();
 
             } catch (MetricNotRegisteredException e) {
                 err.println(e.getMessage());
                 err.println("Method metrics can't be output!");
             }
 
         }
 
         if (!Settings.getFieldMetricsFile().equals(Settings.INIT)) {
 
             try {
                 FieldMetricsInfoManager manager = FieldMetricsInfoManager.getInstance();
                 manager.checkMetrics();
 
                 String fileName = Settings.getMethodMetricsFile();
                 CSVMethodMetricsWriter writer = new CSVMethodMetricsWriter(fileName);
                 writer.write();
 
             } catch (MetricNotRegisteredException e) {
                 err.println(e.getMessage());
                 err.println("Field metrics can't be output!");
             }
         }
     }
 
     /**
      * 
      * wv[ḧ̐mF邽߂̃\bhD sȈw肳ĂꍇCmain \bhɂ͖߂炸C̊֐ŃvOID
      * 
      */
     private final void checkHelpModeParameterValidation() {
         // -h ͑̃IvVƓwłȂ
         if ((!Settings.getTargetDirectory().equals(Settings.INIT))
                 || (!Settings.getListFile().equals(Settings.INIT))
                 || (!Settings.getLanguageString().equals(Settings.INIT))
                 || (!Settings.getMetrics().equals(Settings.INIT))
                 || (!Settings.getFileMetricsFile().equals(Settings.INIT))
                 || (!Settings.getClassMetricsFile().equals(Settings.INIT))
                 || (!Settings.getMethodMetricsFile().equals(Settings.INIT))) {
             err.println("-h can\'t be specified with any other options!");
             printUsage();
             System.exit(0);
         }
     }
 
     /**
      * 
      * \[ḧ̐mF邽߂̃\bhD sȈw肳ĂꍇCmain \bhɂ͖߂炸C̊֐ŃvOID
      * 
      */
     private final void checkDisplayModeParameterValidation() {
         // -d ͎gȂ
         if (!Settings.getTargetDirectory().equals(Settings.INIT)) {
             err.println("-d can\'t be specified in the display mode!");
             printUsage();
             System.exit(0);
         }
 
         // -i ͎gȂ
         if (!Settings.getListFile().equals(Settings.INIT)) {
             err.println("-i can't be specified in the display mode!");
             printUsage();
             System.exit(0);
         }
 
         // -F ͎gȂ
         if (!Settings.getFileMetricsFile().equals(Settings.INIT)) {
             err.println("-F can't be specified in the display mode!");
             printUsage();
             System.exit(0);
         }
 
         // -C ͎gȂ
         if (!Settings.getClassMetricsFile().equals(Settings.INIT)) {
             err.println("-C can't be specified in the display mode!");
             printUsage();
             System.exit(0);
         }
 
         // -M ͎gȂ
         if (!Settings.getMethodMetricsFile().equals(Settings.INIT)) {
             err.println("-M can't be specified in the display mode!");
             printUsage();
             System.exit(0);
         }
 
         // -A ͎gȂ
         if (!Settings.getFieldMetricsFile().equals(Settings.INIT)) {
             err.println("-A can't be specified in the display mode!");
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
     private final void checkAnalysisModeParameterValidation() {
         // -d  -i ̂ǂw肳Ă͕̂s
         if (Settings.getTargetDirectory().equals(Settings.INIT)
                 && Settings.getListFile().equals(Settings.INIT)) {
             err.println("-d or -i must be specified in the analysis mode!");
             printUsage();
             System.exit(0);
         }
 
         // -d  -i ̗w肳Ă͕̂s
         if (!Settings.getTargetDirectory().equals(Settings.INIT)
                 && !Settings.getListFile().equals(Settings.INIT)) {
             err.println("-d and -i can't be specified at the same time!");
             printUsage();
             System.exit(0);
         }
 
         // ꂪw肳Ȃ͕̂s
         if (Settings.getLanguageString().equals(Settings.INIT)) {
             err.println("-l must be specified in the analysis mode.");
             printUsage();
             System.exit(0);
         }
 
         boolean measureFileMetrics = false;
         boolean measureClassMetrics = false;
         boolean measureMethodMetrics = false;
         boolean measureFieldMetrics = false;
 
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
             case FIELD_METRIC:
                 measureFieldMetrics = true;
                 break;
             }
         }
 
         // t@CgNXvꍇ -F IvVw肳ĂȂ΂ȂȂ
         if (measureFileMetrics && (Settings.getFileMetricsFile().equals(Settings.INIT))) {
             err.println("-F must be used for specifying a file for file metrics!");
             System.exit(0);
         }
 
         // NXgNXvꍇ -C IvVw肳ĂȂ΂ȂȂ
         if (measureClassMetrics && (Settings.getClassMetricsFile().equals(Settings.INIT))) {
             err.println("-C must be used for specifying a file for class metrics!");
             System.exit(0);
         }
         // \bhgNXvꍇ -M IvVw肳ĂȂ΂ȂȂ
         if (measureMethodMetrics && (Settings.getMethodMetricsFile().equals(Settings.INIT))) {
             err.println("-M must be used for specifying a file for method metrics!");
             System.exit(0);
         }
 
         // tB[hgNXvꍇ -A IvVw肳ĂȂ΂ȂȂ
         if (measureFieldMetrics && (Settings.getFieldMetricsFile().equals(Settings.INIT))) {
             err.println("-A must be used for specifying a file for field metrics!");
             System.exit(0);
         }
     }
 
     /**
      * ̓[hs.
      * 
      * @param language Ώی
      */
     protected void doAnalysisMode() {
         checkAnalysisModeParameterValidation();
 
         readTargetFiles();
         analyzeTargetFiles();
         launchPlugins();
         writeMetrics();
     }
 
     /**
      * \[hs
      * 
      * @param language Ώی
      */
     protected void doDisplayMode() {
         checkDisplayModeParameterValidation();
 
         // -l Ōꂪw肳ĂȂꍇ́C͉\ꗗ\
         if (Settings.getLanguageString().equals(Settings.INIT)) {
             err.println("Available languages;");
             LANGUAGE[] languages = LANGUAGE.values();
             for (int i = 0; i < languages.length; i++) {
                 err.println("\t" + languages[0].getName() + ": can be specified with term \""
                         + languages[0].getIdentifierName() + "\"");
             }
 
             // -l Ōꂪw肳Ăꍇ́C̃vO~OŎgp\ȃgNXꗗ\
         } else {
             err.println("Available metrics for " + Settings.getLanguage().getName());
             for (AbstractPlugin plugin : PluginManager.getInstance().getPlugins()) {
                 PluginInfo pluginInfo = plugin.getPluginInfo();
                 if (pluginInfo.isMeasurable(Settings.getLanguage())) {
                     err.println("\t" + pluginInfo.getMetricName());
                 }
             }
             // TODO p\gNXꗗ\
         }
     }
 
     /**
      * wv[hs.
      */
     protected void doHelpMode() {
         checkHelpModeParameterValidation();
 
         printUsage();
     }
 
     /**
      * {@link MetricsToolSecurityManager} ̏s. VXeɓo^ł΁CVXẽZLeB}l[Wɂo^.
      */
     private final void initSecurityManager() {
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
      * 
      * c[̎giR}hCIvVj\D
      * 
      */
     protected void printUsage() {
 
         err.println();
         err.println("Available options:");
         err.println("\t-d: root directory that you are going to analysis.");
         err.println("\t-i: List file including file paths that you are going to analysis.");
         err.println("\t-l: Programming language of the target files.");
         err.println("\t-m: Metrics that you want to get. Metrics names are separated with \',\'.");
         err.println("\t-v: Output progress verbosely.");
         err.println("\t-C: File path that the class type metrics are output.");
         err.println("\t-F: File path that the file type metrics are output.");
         err.println("\t-M: File path that the method type metrics are output.");
         err.println("\t-A: File path that the field type metrics are output.");
 
         err.println();
         err.println("Usage:");
         err.println("\t<Help Mode>");
         err.println("\tMetricsTool -h");
         err.println();
         err.println("\t<Display Mode>");
         err.println("\tMetricsTool -x -l");
         err.println("\tMetricsTool -x -l language -m");
         err.println();
         err.println("\t<Analysis Mode>");
         err
                 .println("\tMetricsTool -d directory -l language -m metrics1,metrics2 -C file1 -F file2 -M file3 -A file4");
         err
                 .println("\tMetricsTool -i listFile -l language -m metrics1,metrics2 -C file1 -F file2 -M file3 -A file4");
     }
 
     /**
      * 
      * Xgt@CΏۃt@Co^D ǂݍ݃G[ꍇ́C̃\bhŃvOID
      */
     protected void registerFilesFromListFile() {
 
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
     protected void registerFilesFromDirectory() {
 
         File targetDirectory = new File(Settings.getTargetDirectory());
         registerFilesFromDirectory(targetDirectory);
     }
 
     /**
      * 
      * @param file Ώۃt@C܂̓fBNg
      * 
      * ΏۂfBNg̏ꍇ́C̎qɑ΂čċAIɏD Ώۂt@C̏ꍇ́CΏی̃\[Xt@Cł΁Co^sD
      */
     private void registerFilesFromDirectory(final File file) {
 
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
      * ̎dl Jargp ɓn߂̔zD
      */
     private static ParameterDef[] parameterDefs = {
             new BoolDef('h', "helpMode", "display usage", true),
             new BoolDef('x', "displayMode", "display available language or metrics", true),
             new BoolDef('v', "verbose", "output progress verbosely", true),
             new StringDef('d', "targetDirectory", "Target directory"),
             new StringDef('i', "listFile", "List file including paths of target files"),
             new StringDef('l', "language", "Programming language"),
             new StringDef('m', "metrics", "Measured metrics"),
             new StringDef('F', "fileMetricsFile", "File storing file metrics"),
             new StringDef('C', "classMetricsFile", "File storing class metrics"),
             new StringDef('M', "methodMetricsFile", "File storing method metrics"),
             new StringDef('A', "fieldMetricsFile", "File storing field metrics") };
 
     /**
      * o̓bZ[Wo͗p̃v^
      */
     private static MessagePrinter out = new DefaultMessagePrinter(new MessageSource() {
         public String getMessageSourceName() {
             return "main";
         }
     }, MESSAGE_TYPE.OUT);
 
     /**
      * G[bZ[Wo͗p̃v^
      */
     private static MessagePrinter err = new DefaultMessagePrinter(new MessageSource() {
         public String getMessageSourceName() {
             return "main";
         }
     }, MESSAGE_TYPE.ERROR);
 
     /**
      * NX̒` ClassInfoManager ɓo^DAST p[X̌ɌĂяoȂ΂ȂȂD
      */
     private void registClassInfos() {
 
         // NX}l[WC NX}l[W擾
         final UnresolvedClassInfoManager unresolvedClassInfoManager = UnresolvedClassInfoManager
                 .getInstance();
         final ClassInfoManager classInfoManager = ClassInfoManager.getInstance();
 
         // eNXɑ΂
         for (final UnresolvedClassInfo unresolvedClassInfo : unresolvedClassInfoManager
                 .getClassInfos()) {
 
             //@NX
             final TargetClassInfo classInfo = unresolvedClassInfo.resolve(null, null,
                     classInfoManager, null, null);
 
             // ꂽNXo^
             classInfoManager.add(classInfo);
 
             // eCi[NXɑ΂ď
             for (final UnresolvedClassInfo unresolvedInnerClassInfo : unresolvedClassInfo
                     .getInnerClasses()) {
 
                 //@Ci[NX
                 final TargetInnerClassInfo innerClass = registInnerClassInfo(
                         unresolvedInnerClassInfo, classInfo, classInfoManager);
 
                 // ꂽCi[NXo^
                 classInfo.addInnerClass(innerClass);
                 classInfoManager.add(innerClass);
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
     private TargetInnerClassInfo registInnerClassInfo(
             final UnresolvedClassInfo unresolvedClassInfo, final TargetClassInfo outerClass,
             final ClassInfoManager classInfoManager) {
 
         final TargetInnerClassInfo classInfo = (TargetInnerClassInfo) unresolvedClassInfo.resolve(
                 outerClass, null, classInfoManager, null, null);
 
         // ̃NX̃Ci[NXɑ΂čċAIɏ
         for (final UnresolvedClassInfo unresolvedInnerClassInfo : unresolvedClassInfo
                 .getInnerClasses()) {
 
             //@Ci[NX
             final TargetInnerClassInfo innerClass = registInnerClassInfo(unresolvedInnerClassInfo,
                     classInfo, classInfoManager);
 
             // ꂽCi[NXo^
             classInfo.addInnerClass(innerClass);
             classInfoManager.add(innerClass);
         }
 
         // ̃NX ClassInfo Ԃ
         return classInfo;
     }
 
     /**
      * NX̌^p[^𖼑ODregistClassInfos ̌C addInheritanceInformationToClassInfo
      * ̑OɌĂяoȂ΂ȂȂD
      * 
      */
     private void resolveTypeParameterOfClassInfos() {
 
         // NX}l[WC ς݃NX}l[W擾
         final UnresolvedClassInfoManager unresolvedClassInfoManager = UnresolvedClassInfoManager
                 .getInstance();
         final ClassInfoManager classInfoManager = ClassInfoManager.getInstance();
 
         // eNXɑ΂
         for (final UnresolvedClassInfo unresolvedClassInfo : unresolvedClassInfoManager
                 .getClassInfos()) {
             resolveTypeParameterOfClassInfos(unresolvedClassInfo, classInfoManager);
         }
     }
 
     /**
      * NX̌^p[^𖼑OD resolveTypeParameterOfClassInfo() ̂݌Ăяoׂ
      * 
      * @param unresolvedClassInfo O^p[^NX
      * @param classInfoManager OɗpNX}l[W
      */
     private void resolveTypeParameterOfClassInfos(final UnresolvedClassInfo unresolvedClassInfo,
             final ClassInfoManager classInfoManager) {
 
         // ς݃NX擾
         final TargetClassInfo classInfo = unresolvedClassInfo.getResolved();
         assert null != classInfo : "classInfo shouldn't be null!";
 
         // NX񂩂疢^p[^擾C^sCς݃NXɕt^
         for (final UnresolvedTypeParameterInfo unresolvedTypeParameter : unresolvedClassInfo
                 .getTypeParameters()) {
 
             final TypeInfo typeParameter = unresolvedTypeParameter.resolve(classInfo, null,
                     classInfoManager, null, null);
             classInfo.addTypeParameter((TypeParameterInfo) typeParameter);
         }
 
         // eCi[NXɑ΂
         for (final UnresolvedClassInfo unresolvedInnerClassInfo : unresolvedClassInfo
                 .getInnerClasses()) {
             resolveTypeParameterOfClassInfos(unresolvedInnerClassInfo, classInfoManager);
         }
     }
 
     /**
      * NX̌p ClassInfo ɒǉDxڂ AST p[X̌C registClassInfos ̌ɂтȂ΂ȂȂD
      */
     private void addInheritanceInformationToClassInfos() {
 
         // Unresolved NX}l[WC NX}l[W擾
         final UnresolvedClassInfoManager unresolvedClassInfoManager = UnresolvedClassInfoManager
                 .getInstance();
         final ClassInfoManager classInfoManager = ClassInfoManager.getInstance();
 
         // Os\NXۑ邽߂̃Xg
         final List<UnresolvedClassInfo> unresolvableClasses = new LinkedList<UnresolvedClassInfo>();
 
         // e UnresolvedNXɑ΂
         for (UnresolvedClassInfo unresolvedClassInfo : unresolvedClassInfoManager.getClassInfos()) {
             addInheritanceInformationToClassInfo(unresolvedClassInfo, classInfoManager,
                     unresolvableClasses);
         }
 
         // Os\NX͂
         for (int i = 0; i < 100; i++) {
 
             CLASSLOOP: for (final Iterator<UnresolvedClassInfo> classIterator = unresolvableClasses
                     .iterator(); classIterator.hasNext();) {
 
                 // ClassInfo 擾
                 final UnresolvedClassInfo unresolvedClassInfo = classIterator.next();
                 final TargetClassInfo classInfo = unresolvedClassInfo.getResolved();
                 assert null != classInfo : "classInfo shouldn't be null!";
 
                 // eeNXɑ΂
                 for (final UnresolvedClassTypeInfo unresolvedSuperClassType : unresolvedClassInfo
                         .getSuperClasses()) {
 
                     TypeInfo superClassType = unresolvedSuperClassType.resolve(classInfo, null,
                             classInfoManager, null, null);
 
                     // null łȂꍇ͖OɐƂ݂Ȃ
                     if (null != superClassType) {
 
                         // Ȃꍇ͖OԖUNKNOWNȃNXo^
                         if (superClassType instanceof UnknownTypeInfo) {
                             final ExternalClassInfo superClass = new ExternalClassInfo(
                                     unresolvedSuperClassType.getTypeName());
                             classInfoManager.add(superClass);
                             superClassType = new ClassTypeInfo(superClass);
                         }
 
                         classInfo.addSuperClass((ClassTypeInfo) superClassType);
                         ((ClassTypeInfo) superClassType).getReferencedClass()
                                 .addSubClass(classInfo);
 
                         // null ȏꍇ͖OɎsƂ݂Ȃ̂ unresolvedClassInfo  unresolvableClasses
                         // 폜Ȃ
                     } else {
                         continue CLASSLOOP;
                     }
                 }
 
                 classIterator.remove();
             }
 
             // ׂ unresolvableClasses ւ
             Collections.shuffle(unresolvableClasses);
         }
 
         if (0 < unresolvableClasses.size()) {
             err.println("There are " + unresolvableClasses.size()
                     + " unresolvable class inheritance");
         }
     }
 
     /**
      * NX̌p InnerClassInfo ɒǉDaddInheritanceInformationToClassInfos ̒̂݌Ăяoׂ
      * 
      * @param unresolvedClassInfo p֌WǉijNX
      * @param classInfoManager OɗpNX}l[W
      */
     private void addInheritanceInformationToClassInfo(
             final UnresolvedClassInfo unresolvedClassInfo, final ClassInfoManager classInfoManager,
             final List<UnresolvedClassInfo> unresolvableClasses) {
 
         // ClassInfo 擾
         final TargetClassInfo classInfo = unresolvedClassInfo.getResolved();
         assert null != classInfo : "classInfo shouldn't be null!";
 
         // eeNXɑ΂
         for (final UnresolvedClassTypeInfo unresolvedSuperClassType : unresolvedClassInfo
                 .getSuperClasses()) {
 
             TypeInfo superClassType = unresolvedSuperClassType.resolve(classInfo, null,
                     classInfoManager, null, null);
 
             // null ꍇ͉s\XgɈꎞIɊi[
             if (null == superClassType) {
 
                 unresolvableClasses.add(unresolvedClassInfo);
 
             } else {
 
                 // Ȃꍇ͖OԖUNKNOWNȃNXo^
                 if (superClassType instanceof UnknownTypeInfo) {
                     final ExternalClassInfo superClass = new ExternalClassInfo(
                             unresolvedSuperClassType.getTypeName());
                     classInfoManager.add(superClass);
                 }
 
                 classInfo.addSuperClass((ClassTypeInfo) superClassType);
                 ((ClassTypeInfo) superClassType).getReferencedClass().addSubClass(classInfo);
             }
         }
 
         // eCi[NXɑ΂
         for (final UnresolvedClassInfo unresolvedInnerClassInfo : unresolvedClassInfo
                 .getInnerClasses()) {
             addInheritanceInformationToClassInfo(unresolvedInnerClassInfo, classInfoManager,
                     unresolvableClasses);
         }
     }
 
     /**
      * tB[h̒` FieldInfoManager ɓo^D registClassInfos ̌ɌĂяoȂ΂ȂȂ
      * 
      */
     private void registFieldInfos() {
 
         // Unresolved NX}l[WCNX}l[WCtB[h}l[W擾
         final UnresolvedClassInfoManager unresolvedClassInfoManager = UnresolvedClassInfoManager
                 .getInstance();
         final ClassInfoManager classInfoManager = ClassInfoManager.getInstance();
         final FieldInfoManager fieldInfoManager = FieldInfoManager.getInstance();
         final MethodInfoManager methodInfoManager = MethodInfoManager.getInstance();
 
         // e UnresolvedNXɑ΂
         for (final UnresolvedClassInfo unresolvedClassInfo : unresolvedClassInfoManager
                 .getClassInfos()) {
             registFieldInfos(unresolvedClassInfo, classInfoManager, fieldInfoManager,
                     methodInfoManager);
         }
     }
 
     /**
      * tB[h̒` FieldInfoManager ɓo^D
      * 
      * @param unresolvedClassInfo tB[hΏۃNX
      * @param classInfoManager pNX}l[W
      * @param fieldInfoManager ptB[h}l[W
      */
     private void registFieldInfos(final UnresolvedClassInfo unresolvedClassInfo,
             final ClassInfoManager classInfoManager, final FieldInfoManager fieldInfoManager,
             final MethodInfoManager methodInfoManager) {
 
         // ClassInfo 擾
         final TargetClassInfo ownerClass = unresolvedClassInfo.getResolved();
         assert null != ownerClass : "ownerClass shouldn't be null!";
 
         // etB[hɑ΂
         for (final UnresolvedFieldInfo unresolvedFieldInfo : unresolvedClassInfo.getDefinedFields()) {
 
             unresolvedFieldInfo.resolve(ownerClass, null, classInfoManager, fieldInfoManager,
                     methodInfoManager);
         }
 
         // eCi[NXɑ΂
         for (final UnresolvedClassInfo unresolvedInnerClassInfo : unresolvedClassInfo
                 .getInnerClasses()) {
             registFieldInfos(unresolvedInnerClassInfo, classInfoManager, fieldInfoManager,
                     methodInfoManager);
         }
     }
 
     /**
      * \bh̒` MethodInfoManager ɓo^DregistClassInfos ̌ɌĂяoȂ΂ȂȂD
      */
     private void registMethodInfos() {
 
         // Unresolved NX}l[WC NX}l[WC\bh}l[W擾
         final UnresolvedClassInfoManager unresolvedClassInfoManager = UnresolvedClassInfoManager
                 .getInstance();
         final ClassInfoManager classInfoManager = ClassInfoManager.getInstance();
         final FieldInfoManager fieldInfoManager = FieldInfoManager.getInstance();
         final MethodInfoManager methodInfoManager = MethodInfoManager.getInstance();
 
         // e UnresolvedNXɑ΂
         for (final UnresolvedClassInfo unresolvedClassInfo : unresolvedClassInfoManager
                 .getClassInfos()) {
             registMethodInfos(unresolvedClassInfo, classInfoManager, fieldInfoManager,
                     methodInfoManager);
         }
     }
 
     /**
      * \bh`C\bh}l[Wɓo^D
      * 
      * @param unresolvedClassInfo \bhΏۃNX
      * @param classInfoManager pNX}l[W
      * @param methodInfoManager p郁\bh}l[W
      */
     private void registMethodInfos(final UnresolvedClassInfo unresolvedClassInfo,
             final ClassInfoManager classInfoManager, final FieldInfoManager fieldInfoManager,
             final MethodInfoManager methodInfoManager) {
 
         // ClassInfo 擾
         final TargetClassInfo ownerClass = unresolvedClassInfo.getResolved();
 
         // e\bhɑ΂
         for (final UnresolvedMethodInfo unresolvedMethodInfo : unresolvedClassInfo
                 .getDefinedMethods()) {
 
             // \bh
             final TargetMethodInfo methodInfo = unresolvedMethodInfo.resolve(ownerClass, null,
                     classInfoManager, fieldInfoManager, methodInfoManager);
 
             // \bho^
             ownerClass.addDefinedMethod(methodInfo);
             methodInfoManager.add(methodInfo);
         }
 
         // eRXgN^ɑ΂
         for (final UnresolvedConstructorInfo unresolvedConstructorInfo : unresolvedClassInfo
                 .getDefinedConstructors()) {
 
             //@RXgN^
             final TargetConstructorInfo constructorInfo = unresolvedConstructorInfo.resolve(
                     ownerClass, null, classInfoManager, fieldInfoManager, methodInfoManager);
             methodInfoManager.add(constructorInfo);
 
             // RXgN^o^            
             ownerClass.addDefinedConstructor(constructorInfo);
             methodInfoManager.add(constructorInfo);
 
         }
 
         // e UnresolvedNXɑ΂
         for (final UnresolvedClassInfo unresolvedInnerClassInfo : unresolvedClassInfo
                 .getInnerClasses()) {
             registMethodInfos(unresolvedInnerClassInfo, classInfoManager, fieldInfoManager,
                     methodInfoManager);
         }
     }
 
     /**
      * \bhI[o[CheMethodInfoɒǉDaddInheritanceInfomationToClassInfos ̌  registMethodInfos
      * ̌ɌĂяoȂ΂ȂȂ
      */
     private void addOverrideRelation() {
 
         // SĂ̑ΏۃNXɑ΂
         for (TargetClassInfo classInfo : ClassInfoManager.getInstance().getTargetClassInfos()) {
             addOverrideRelation(classInfo);
         }
     }
 
     /**
      * \bhI[o[CheMethodInfoɒǉDŎw肵NX̃\bhɂďs
      * 
      * @param classInfo ΏۃNX
      */
     private void addOverrideRelation(final TargetClassInfo classInfo) {
 
         // eeNXɑ΂
         for (final ClassInfo superClassInfo : ClassTypeInfo.convert(classInfo.getSuperClasses())) {
 
             // eΏۃNX̊e\bhɂāCeNX̃\bhI[o[ChĂ邩𒲍
             for (final MethodInfo methodInfo : classInfo.getDefinedMethods()) {
                 addOverrideRelation(superClassInfo, methodInfo);
             }
         }
 
         // eCi[NXɑ΂
         for (ClassInfo innerClassInfo : classInfo.getInnerClasses()) {
             addOverrideRelation((TargetClassInfo) innerClassInfo);
         }
     }
 
     /**
      * \bhI[o[ChǉDŎw肳ꂽNXŒ`Ă郁\bhɑ΂đs.
      * AddOverrideInformationToMethodInfos()̒̂݌ĂяoD
      * 
      * @param classInfo NX
      * @param overrider I[o[ChΏۂ̃\bh
      */
     private void addOverrideRelation(final ClassInfo classInfo, final MethodInfo overrider) {
 
         if ((null == classInfo) || (null == overrider)) {
             throw new NullPointerException();
         }
 
         if (!(classInfo instanceof TargetClassInfo)) {
             return;
         }
 
         for (final TargetMethodInfo methodInfo : ((TargetClassInfo) classInfo).getDefinedMethods()) {
 
             // \bhႤꍇ̓I[o[ChȂ
             if (!methodInfo.getMethodName().equals(overrider.getMethodName())) {
                 continue;
             }
 
             // I[o[Ch֌Wo^
             overrider.addOverridee(methodInfo);
             methodInfo.addOverrider(overrider);
 
             // ڂ̃I[o[Ch֌WoȂ̂ŁC̃NX̐eNX͒Ȃ
             return;
         }
 
         // eNXQɑ΂čċAIɏ
         for (final ClassInfo superClassInfo : ClassTypeInfo.convert(classInfo.getSuperClasses())) {
             addOverrideRelation(superClassInfo, overrider);
         }
     }
 
     /**
      * GeBeBitB[hNXj̑EQƁC\bȟĂяo֌WǉD
      */
     private void addReferenceAssignmentCallRelateion() {
 
         final UnresolvedClassInfoManager unresolvedClassInfoManager = UnresolvedClassInfoManager
                 .getInstance();
         final ClassInfoManager classInfoManager = ClassInfoManager.getInstance();
         final FieldInfoManager fieldInfoManager = FieldInfoManager.getInstance();
         final MethodInfoManager methodInfoManager = MethodInfoManager.getInstance();
 
         // eNX ɑ΂
         for (final UnresolvedClassInfo unresolvedClassInfo : unresolvedClassInfoManager
                 .getClassInfos()) {
             addReferenceAssignmentCallRelation(unresolvedClassInfo, classInfoManager,
                     fieldInfoManager, methodInfoManager);
         }
     }
 
     /**
      * GeBeBitB[hNXj̑EQƁC\bȟĂяo֌WǉD
      * 
      * @param unresolvedClassInfo ΏۃNX
      * @param classInfoManager pNX}l[W
      * @param fieldInfoManager ptB[h}l[W
      * @param methodInfoManager p郁\bh}l[W
      * @param resolvedCache ς݌ĂяõLbV
      */
     private void addReferenceAssignmentCallRelation(final UnresolvedClassInfo unresolvedClassInfo,
             final ClassInfoManager classInfoManager, final FieldInfoManager fieldInfoManager,
             final MethodInfoManager methodInfoManager) {
 
         // NX񂩂Cς݃NX擾
         // final TargetClassInfo ownerClass = unresolvedClassInfo.getResolvedUnit();
 
         // e\bhɑ΂
         for (final UnresolvedMethodInfo unresolvedMethodInfo : unresolvedClassInfo
                 .getDefinedMethods()) {
 
             // \bh̗p֌W
             this.addReferenceAssignmentCallRelation(unresolvedMethodInfo, unresolvedClassInfo,
                     classInfoManager, fieldInfoManager, methodInfoManager);
         }
 
         // eCi[NXɑ΂
         for (final UnresolvedClassInfo unresolvedInnerClassInfo : unresolvedClassInfo
                 .getInnerClasses()) {
             addReferenceAssignmentCallRelation(unresolvedInnerClassInfo, classInfoManager,
                     fieldInfoManager, methodInfoManager);
         }
     }
 
     /**
      * GeBeBitB[hNXj̑EQƁC\bȟĂяo֌WǉD
      * 
      * @param unresolvedLocalSpace ͑Ώۖ[J̈
      * @param unresolvedClassInfo ΏۃNX
      * @param classInfoManager pNX}l[W
      * @param fieldInfoManager ptB[h}l[W
      * @param methodInfoManager p郁\bh}l[W
      * @param resolvedCache ς݌ĂяõLbV
      */
     private void addReferenceAssignmentCallRelation(
             final UnresolvedLocalSpaceInfo<?> unresolvedLocalSpace,
             final UnresolvedClassInfo unresolvedClassInfo, final ClassInfoManager classInfoManager,
             final FieldInfoManager fieldInfoManager, final MethodInfoManager methodInfoManager) {
 
         // \bh񂩂Cς݃\bh擾
         final LocalSpaceInfo localSpace = unresolvedLocalSpace.getResolved();
         assert null != localSpace : "UnresolvedLocalSpaceInfo#getResolvedInfo is null!";
 
         // LNX擾
         final TargetClassInfo ownerClass = (TargetClassInfo) localSpace.getOwnerClass();
         final CallableUnitInfo ownerMethod;
         if (localSpace instanceof CallableUnitInfo) {
             ownerMethod = (CallableUnitInfo) localSpace;
         } else if (localSpace instanceof BlockInfo) {
             ownerMethod = ((BlockInfo) localSpace).getOwnerMethod();
         } else {
             ownerMethod = null;
             assert false : "Here shouldn't be reached!";
         }
 
         // ̏ꍇC̖O
         if (localSpace instanceof ConditionalBlockInfo) {
             final UnresolvedConditionalBlockInfo<?> unresolvedConditionalBlock = (UnresolvedConditionalBlockInfo<?>) unresolvedLocalSpace;
 
             if (null != unresolvedConditionalBlock.getConditionalExpression()) {
                 final ExpressionInfo conditionalExpression = unresolvedConditionalBlock
                         .getConditionalExpression().resolve(ownerClass, ownerMethod,
                                 classInfoManager, fieldInfoManager, methodInfoManager);
 
                 try {
                     Class cls = Class
                             .forName("jp.ac.osaka_u.ist.sel.metricstool.main.data.target.ConditionalBlockInfo");
                     Field filed = cls.getDeclaredField("conditionalExpression");
                     filed.setAccessible(true);
                     filed.set(localSpace, conditionalExpression);
                 } catch (ClassNotFoundException e) {
                     assert false : "Illegal state: ConditionalBlockInfo is not found";
                 } catch (NoSuchFieldException e) {
                     assert false : "Illegal state: conditonalExpression is not found";
                 } catch (IllegalAccessException e) {
 
                 }
             }
         }
 
         // e̖O
         for (final UnresolvedStatementInfo<? extends StatementInfo> unresolvedStatement : unresolvedLocalSpace
                 .getStatements()) {
             if (!(unresolvedStatement instanceof UnresolvedBlockInfo)) {
                 final StatementInfo statement = unresolvedStatement.resolve(ownerClass,
                         ownerMethod, classInfoManager, fieldInfoManager, methodInfoManager);
                 localSpace.addStatement(statement);
             }
         }
 
         // etB[hgp̖O
         for (final UnresolvedVariableUsageInfo<?> unresolvedVariableUsage : unresolvedLocalSpace
                 .getVariableUsages()) {
 
             // ϐgp
             final EntityUsageInfo variableUsage = unresolvedVariableUsage.resolve(ownerClass,
                     ownerMethod, classInfoManager, fieldInfoManager, methodInfoManager);
 
             // Ołꍇ͓o^
             if (variableUsage instanceof VariableUsageInfo) {
                 VariableUsageInfo<?> usage = (VariableUsageInfo<?>) variableUsage;
                 localSpace.addVariableUsage(usage);
                 //usage.getUsedVariable().addUsage(usage);
 
                 // tB[h̏ꍇ́Cp֌W
                 if (variableUsage instanceof FieldUsageInfo) {
                     final boolean reference = ((FieldUsageInfo) variableUsage).isReference();
                     final FieldInfo usedField = ((FieldUsageInfo) variableUsage).getUsedVariable();
                     if (reference) {
                         usedField.addReferencer(ownerMethod);
                     } else {
                         usedField.addAssignmenter(ownerMethod);
                     }
                 }
             }
         }
 
         // e\bhĂяỏ
         for (final UnresolvedCallInfo<?> unresolvedCall : unresolvedLocalSpace.getCalls()) {
 
             final EntityUsageInfo memberCall = unresolvedCall.resolve(ownerClass, ownerMethod,
                     classInfoManager, fieldInfoManager, methodInfoManager);
 
             // \bhуRXgN^Ăяołꍇ
             if (memberCall instanceof MethodCallInfo) {
                 localSpace.addCall((MethodCallInfo) memberCall);
                 ((MethodCallInfo) memberCall).getCallee().addCaller(ownerMethod);
             } else if (memberCall instanceof ConstructorCallInfo) {
                 localSpace.addCall((ConstructorCallInfo) memberCall);
             }
         }
 
         //@eCi[ubNɂ
         for (final UnresolvedStatementInfo<?> unresolvedStatement : unresolvedLocalSpace
                 .getStatements()) {
 
             // \bh̗p֌W
             if (unresolvedStatement instanceof UnresolvedBlockInfo) {
 
                 this.addReferenceAssignmentCallRelation(
                         (UnresolvedBlockInfo<?>) unresolvedStatement, unresolvedClassInfo,
                         classInfoManager, fieldInfoManager, methodInfoManager);
             }
         }
     }
 }
