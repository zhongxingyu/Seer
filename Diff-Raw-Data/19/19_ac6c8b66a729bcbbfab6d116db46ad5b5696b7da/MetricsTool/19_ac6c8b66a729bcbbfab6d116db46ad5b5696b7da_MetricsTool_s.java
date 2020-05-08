 package jp.ac.osaka_u.ist.sel.metricstool.main;
 
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.lang.reflect.Field;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 
 import jp.ac.osaka_u.ist.sel.metricstool.main.ast.csharp.CSharpAntlrAstTranslator;
 import jp.ac.osaka_u.ist.sel.metricstool.main.ast.databuilder.ASTParseException;
 import jp.ac.osaka_u.ist.sel.metricstool.main.ast.java.Java13AntlrAstTranslator;
 import jp.ac.osaka_u.ist.sel.metricstool.main.ast.java.Java14AntlrAstTranslator;
 import jp.ac.osaka_u.ist.sel.metricstool.main.ast.java.Java15AntlrAstTranslator;
 import jp.ac.osaka_u.ist.sel.metricstool.main.ast.java.JavaAstVisitorManager;
 import jp.ac.osaka_u.ist.sel.metricstool.main.ast.visitor.AstVisitorManager;
 import jp.ac.osaka_u.ist.sel.metricstool.main.ast.visitor.antlr.AntlrAstVisitor;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.DataManager;
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
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.ConditionalClauseInfo;
import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.ConstructorCallInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.ExpressionInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.ExternalClassInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.FieldInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.FieldInfoManager;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.FieldUsageInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.FileInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.LocalSpaceInfo;
import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.MethodCallInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.MethodInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.MethodInfoManager;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.ModifierInfo;
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
 
 import org.apache.commons.cli.CommandLine;
 import org.apache.commons.cli.CommandLineParser;
 import org.apache.commons.cli.HelpFormatter;
 import org.apache.commons.cli.Option;
 import org.apache.commons.cli.Options;
 import org.apache.commons.cli.ParseException;
 import org.apache.commons.cli.PosixParser;
 
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
 
         // \p̃Xi쐬
         final MessageListener outListener = new MessageListener() {
             public void messageReceived(MessageEvent event) {
                 System.out.print(event.getSource().getMessageSourceName() + " > "
                         + event.getMessage());
             }
         };
         final MessageListener errListener = new MessageListener() {
             public void messageReceived(MessageEvent event) {
                 System.out.print(event.getSource().getMessageSourceName() + " > "
                         + event.getMessage());
             }
         };
         MessagePool.getInstance(MESSAGE_TYPE.OUT).addMessageListener(outListener);
         MessagePool.getInstance(MESSAGE_TYPE.ERROR).addMessageListener(errListener);
 
         final Options options = new Options();
 
         {
             final Option h = new Option("h", "help", false, "display usage");
             h.setRequired(false);
             options.addOption(h);
         }
 
         {
             final Option v = new Option("v", "verbose", false, "output progress verbosely");
             v.setRequired(false);
             options.addOption(v);
         }
 
         {
             final Option d = new Option("d", "directory", true, "specify target directory");
             d.setArgName("directory");
             d.setArgs(1);
             d.setRequired(false);
             options.addOption(d);
         }
 
         {
             final Option i = new Option("i", "input", true,
                     "specify the input that contains the list of target files");
             i.setArgName("input");
             i.setArgs(1);
             i.setRequired(false);
             options.addOption(i);
         }
 
         {
             final Option l = new Option("l", "language", true, "specify programming language");
             l.setArgName("input");
             l.setArgs(1);
             l.setRequired(false);
             options.addOption(l);
         }
 
         {
             final Option m = new Option("m", "metrics", true,
                     "specify measured metrics with comma separeted format (e.g., -m rfc,dit,lcom)");
             m.setArgName("metrics");
             m.setArgs(1);
             m.setRequired(false);
             options.addOption(m);
         }
 
         {
             final Option F = new Option("F", "FileMetricsFile", true,
                     "specify file that measured FILE metrics were stored into");
             F.setArgName("file metrics file");
             F.setArgs(1);
             F.setRequired(false);
             options.addOption(F);
         }
 
         {
             final Option C = new Option("C", "ClassMetricsFile", true,
                     "specify file that measured CLASS metrics were stored into");
             C.setArgName("class metrics file");
             C.setArgs(1);
             C.setRequired(false);
             options.addOption(C);
         }
 
         {
             final Option M = new Option("M", "MethodMetricsFile", true,
                     "specify file that measured METHOD metrics were stored into");
             M.setArgName("method metrics file");
             M.setArgs(1);
             M.setRequired(false);
             options.addOption(M);
         }
 
         {
             final Option A = new Option("A", "AttributeMetricsFile", true,
                     "specify file that measured ATTRIBUTE metrics were stored into");
             A.setArgName("attribute metrics file");
             A.setArgs(1);
             A.setRequired(false);
             options.addOption(A);
         }
 
         final MetricsTool metricsTool = new MetricsTool();
 
         try {
 
             final CommandLineParser parser = new PosixParser();
             final CommandLine cmd = parser.parse(options, args);
 
             // "-h"w肳Ăꍇ̓wv\ďI
             // ̂ƂC̃IvV͑SĖ
             if (cmd.hasOption("h") || (0 == args.length)) {
                 final HelpFormatter formatter = new HelpFormatter();
                 formatter.printHelp("MetricsTool", options, true);
 
                 // -l Ōꂪw肳ĂȂꍇ́C͉\ꗗ\
                 if (!cmd.hasOption("l")) {
                     err.println("Available languages;");
                     for (final LANGUAGE language : LANGUAGE.values()) {
                         err.println("\t" + language.getName() + ": can be specified with term \""
                                 + language.getIdentifierName() + "\"");
                     }
 
                     // -l Ōꂪw肳Ăꍇ́C̃vO~OŎgp\ȃgNXꗗ\
                 } else {
                     Settings.getInstance().setLanguage(cmd.getOptionValue("l"));
                     err.println("Available metrics for "
                             + Settings.getInstance().getLanguage().getName());
                     metricsTool.loadPlugins(Settings.getInstance().getMetrics());
                     for (final AbstractPlugin plugin : DataManager.getInstance().getPluginManager()
                             .getPlugins()) {
                         final PluginInfo pluginInfo = plugin.getPluginInfo();
                         if (pluginInfo.isMeasurable(Settings.getInstance().getLanguage())) {
                             err.println("\t" + pluginInfo.getMetricName());
                         }
                     }
                 }
 
                 System.exit(0);
             }
 
             Settings.getInstance().setVerbose(cmd.hasOption("v"));
             if (cmd.hasOption("d")) {
                 Settings.getInstance().setTargetDirectory(cmd.getOptionValue("d"));
             }
             if (cmd.hasOption("i")) {
                 Settings.getInstance().setListFile(cmd.getOptionValue("i"));
             }
             Settings.getInstance().setLanguage(cmd.getOptionValue("l"));
             if (cmd.hasOption("m")) {
                 Settings.getInstance().setMetrics(cmd.getOptionValue("m"));
             }
             if (cmd.hasOption("F")) {
                 Settings.getInstance().setFileMetricsFile(cmd.getOptionValue("F"));
             }
             if (cmd.hasOption("C")) {
                 Settings.getInstance().setClassMetricsFile(cmd.getOptionValue("C"));
             }
             if (cmd.hasOption("M")) {
                 Settings.getInstance().setMethodMetricsFile(cmd.getOptionValue("M"));
             }
             if (cmd.hasOption("A")) {
                 Settings.getInstance().setFieldMetricsFile(cmd.getOptionValue("A"));
             }
 
             metricsTool.loadPlugins(Settings.getInstance().getMetrics());
 
             // R}hCǂ`FbN
             {
                 // -d  -i ̂ǂw肳Ă͕̂s
                 if (!cmd.hasOption("d") && !cmd.hasOption("l")) {
                     err.println("-d and/or -i must be specified in the analysis mode!");
                     System.exit(0);
                 }
 
                 // ꂪw肳Ȃ͕̂s
                 if (!cmd.hasOption("l")) {
                     err.println("-l must be specified for analysis");
                     System.exit(0);
                 }
 
                 {
                     // t@CgNXvꍇ -F IvVw肳ĂȂ΂ȂȂ
                     if ((0 < DataManager.getInstance().getPluginManager().getFileMetricPlugins()
                             .size())
                             && !cmd.hasOption("F")) {
                         err.println("-F must be specified for file metrics!");
                         System.exit(0);
                     }
 
                     // NXgNXvꍇ -C IvVw肳ĂȂ΂ȂȂ
                     if ((0 < DataManager.getInstance().getPluginManager().getClassMetricPlugins()
                             .size())
                             && !cmd.hasOption("C")) {
                         err.println("-C must be specified for class metrics!");
                         System.exit(0);
                     }
                     // \bhgNXvꍇ -M IvVw肳ĂȂ΂ȂȂ
                     if ((0 < DataManager.getInstance().getPluginManager().getMethodMetricPlugins()
                             .size())
                             && !cmd.hasOption("M")) {
                         err.println("-M must be specified for method metrics!");
                         System.exit(0);
                     }
 
                     // tB[hgNXvꍇ -A IvVw肳ĂȂ΂ȂȂ
                     if ((0 < DataManager.getInstance().getPluginManager().getFieldMetricPlugins()
                             .size())
                             && !cmd.hasOption("A")) {
                         err.println("-A must be specified for field metrics!");
                         System.exit(0);
                     }
                 }
 
                 {
                     // t@CgNXvȂ̂ -F@IvVw肳Ăꍇ͖|ʒm
                     if ((0 == DataManager.getInstance().getPluginManager().getFileMetricPlugins()
                             .size())
                             && cmd.hasOption("F")) {
                         err.println("No file metric is specified. -F is ignored.");
                     }
 
                     // NXgNXvȂ̂ -C@IvVw肳Ăꍇ͖|ʒm
                     if ((0 == DataManager.getInstance().getPluginManager().getClassMetricPlugins()
                             .size())
                             && cmd.hasOption("C")) {
                         err.println("No class metric is specified. -C is ignored.");
                     }
 
                     // \bhgNXvȂ̂ -M@IvVw肳Ăꍇ͖|ʒm
                     if ((0 == DataManager.getInstance().getPluginManager().getMethodMetricPlugins()
                             .size())
                             && cmd.hasOption("M")) {
                         err.println("No method metric is specified. -M is ignored.");
                     }
 
                     // tB[hgNXvȂ̂ -A@IvVw肳Ăꍇ͖|ʒm
                     if ((0 == DataManager.getInstance().getPluginManager().getFieldMetricPlugins()
                             .size())
                             && cmd.hasOption("A")) {
                         err.println("No field metric is specified. -A is ignored.");
                     }
                 }
             }
 
         } catch (ParseException e) {
             System.out.println(e.getMessage());
             System.exit(0);
         }
 
         final long start = System.nanoTime();
 
         metricsTool.readTargetFiles();
         metricsTool.analyzeTargetFiles();
         metricsTool.launchPlugins();
         metricsTool.writeMetrics();
 
         out.println("successfully finished.");
 
         final long end = System.nanoTime();
 
         if (Settings.getInstance().isVerbose()) {
             out.println("elapsed time: " + (end - start) / 1000000000 + " seconds");
             out.println("number of analyzed files: "
                     + DataManager.getInstance().getFileInfoManager().getFileInfos().size());
 
             int loc = 0;
             for (final FileInfo file : DataManager.getInstance().getFileInfoManager()
                     .getFileInfos()) {
                 loc += file.getLOC();
             }
             out.println("analyzed lines of code: " + loc);
         }
 
         MessagePool.getInstance(MESSAGE_TYPE.OUT).removeMessageListener(outListener);
         MessagePool.getInstance(MESSAGE_TYPE.ERROR).removeMessageListener(errListener);

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
 
         switch (Settings.getInstance().getLanguage()) {
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
             final int totalFileNumber = DataManager.getInstance().getTargetFileManager().size();
             int currentFileNumber = 1;
             final StringBuffer fileInformationBuffer = new StringBuffer();
 
             for (final TargetFile targetFile : DataManager.getInstance().getTargetFileManager()) {
                 try {
                     final String name = targetFile.getName();
 
                     final FileInfo fileInfo = new FileInfo(name);
                     DataManager.getInstance().getFileInfoManager().add(fileInfo);
 
                     if (Settings.getInstance().isVerbose()) {
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
 
                     switch (Settings.getInstance().getLanguage()) {
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
         if (Settings.getInstance().isVerbose()) {
             out.println("STEP1 : resolve class definitions.");
         }
         registClassInfos();
         if (Settings.getInstance().isVerbose()) {
             out.println("STEP2 : resolve type parameters of classes.");
         }
         resolveTypeParameterOfClassInfos();
         if (Settings.getInstance().isVerbose()) {
             out.println("STEP3 : resolve class inheritances.");
         }
         addInheritanceInformationToClassInfos();
         if (Settings.getInstance().isVerbose()) {
             out.println("STEP4 : resolve field definitions.");
         }
         registFieldInfos();
         if (Settings.getInstance().isVerbose()) {
             out.println("STEP5 : resolve method definitions.");
         }
         registMethodInfos();
         if (Settings.getInstance().isVerbose()) {
             out.println("STEP6 : resolve type parameter usages.");
         }
         addClassTypeParameterInfos();
         addMethodTypeParameterInfos();
         if (Settings.getInstance().isVerbose()) {
             out.println("STEP7 : resolve method overrides.");
         }
         addOverrideRelation();
         if (Settings.getInstance().isVerbose()) {
             out.println("STEP8 : resolve field and method usages.");
         }
         addReferenceAssignmentCallRelateion();
 
         // @̂t@Cꗗ\
         // err.println("The following files includes uncorrect syntax.");
         // err.println("Any metrics of them were not measured");
         for (final TargetFile targetFile : DataManager.getInstance().getTargetFileManager()) {
             if (!targetFile.isCorrectSyntax()) {
                 err.println("Incorrect syntax file: " + targetFile.getName());
             }
         }
     }
 
     /**
      * vOC[h. w肳ꂽCw肳ꂽgNXɊ֘AvOĈ݂ {@link PluginManager}ɓo^.
      * null w肳ꂽꍇ͑ΏیɂČv\ȑSẴgNXo^
      * 
      * @param metrics w肷郁gNX̔zCw肵Ȃꍇnull
      */
     public void loadPlugins(final String[] metrics) {
 
         final PluginManager pluginManager = DataManager.getInstance().getPluginManager();
         final Settings settings = Settings.getInstance();
         try {
             for (final AbstractPlugin plugin : (new DefaultPluginLoader()).loadPlugins()) {// vOCS[h
                 final PluginInfo info = plugin.getPluginInfo();
 
                 // ΏیŌv\łȂΓo^Ȃ
                 if (!info.isMeasurable(settings.getLanguage())) {
                     continue;
                 }
 
                 if (null != metrics) {
                     // gNXw肳Ă̂ł̃vOCƈv邩`FbN
                     final String pluginMetricName = info.getMetricName();
                     for (final String metric : metrics) {
                         if (metric.equalsIgnoreCase(pluginMetricName)) {
                             pluginManager.addPlugin(plugin);
                             break;
                         }
                     }
 
                     // gNXw肳ĂȂ̂łƂ肠So^
                 } else {
                     pluginManager.addPlugin(plugin);
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
 
         out.println("calculating metrics.");
 
         PluginLauncher launcher = new DefaultPluginLauncher();
         launcher.setMaximumLaunchingNum(1);
         launcher.launchAll(DataManager.getInstance().getPluginManager().getPlugins());
 
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
 
         final Settings settings = Settings.getInstance();
 
         // fBNgǂݍ
         if (null != settings.getTargetDirectory()) {
 
             File targetDirectory = new File(settings.getTargetDirectory());
             registerFilesFromDirectory(targetDirectory);
 
             // Xgt@Cǂݍ
         } else if (null != settings.getListFile()) {
 
             try {
 
                 final TargetFileManager targetFiles = DataManager.getInstance()
                         .getTargetFileManager();
                 for (BufferedReader reader = new BufferedReader(new FileReader(settings
                         .getListFile())); reader.ready();) {
                     final String line = reader.readLine();
                     final TargetFile targetFile = new TargetFile(line);
                     targetFiles.add(targetFile);
                 }
 
             } catch (FileNotFoundException e) {
                 err.println("\"" + settings.getListFile() + "\" is not a valid file!");
                 System.exit(0);
             } catch (IOException e) {
                 err.println("\"" + settings.getListFile() + "\" can\'t read!");
                 System.exit(0);
             }
         }
     }
 
     /**
      * gNX {@link Settings} Ɏw肳ꂽt@Cɏo͂.
      */
     public void writeMetrics() {
 
         final PluginManager pluginManager = DataManager.getInstance().getPluginManager();
         final Settings settings = Settings.getInstance();
 
         // t@CgNXvꍇ
         if (0 < pluginManager.getFileMetricPlugins().size()) {
 
             try {
                 final FileMetricsInfoManager manager = DataManager.getInstance()
                         .getFileMetricsInfoManager();
                 manager.checkMetrics();
 
                 final String fileName = settings.getFileMetricsFile();
                 final CSVFileMetricsWriter writer = new CSVFileMetricsWriter(fileName);
                 writer.write();
 
             } catch (MetricNotRegisteredException e) {
                 err.println(e.getMessage());
                 err.println("File metrics can't be output!");
             }
         }
 
         // NXgNXvꍇ
         if (0 < pluginManager.getClassMetricPlugins().size()) {
 
             try {
                 final ClassMetricsInfoManager manager = DataManager.getInstance()
                         .getClassMetricsInfoManager();
                 manager.checkMetrics();
 
                 final String fileName = settings.getClassMetricsFile();
                 final CSVClassMetricsWriter writer = new CSVClassMetricsWriter(fileName);
                 writer.write();
 
             } catch (MetricNotRegisteredException e) {
                 err.println(e.getMessage());
                 err.println("Class metrics can't be output!");
             }
         }
 
         // \bhgNXvꍇ
         if (0 < pluginManager.getMethodMetricPlugins().size()) {
 
             try {
                 final MethodMetricsInfoManager manager = DataManager.getInstance()
                         .getMethodMetricsInfoManager();
                 manager.checkMetrics();
 
                 final String fileName = settings.getMethodMetricsFile();
                 final CSVMethodMetricsWriter writer = new CSVMethodMetricsWriter(fileName);
                 writer.write();
 
             } catch (MetricNotRegisteredException e) {
                 err.println(e.getMessage());
                 err.println("Method metrics can't be output!");
             }
 
         }
 
         // tB[hgNXvꍇ
         if (0 < pluginManager.getFieldMetricPlugins().size()) {
 
             try {
                 final FieldMetricsInfoManager manager = DataManager.getInstance()
                         .getFieldMetricsInfoManager();
                 manager.checkMetrics();
 
                 final String fileName = settings.getMethodMetricsFile();
                 final CSVMethodMetricsWriter writer = new CSVMethodMetricsWriter(fileName);
                 writer.write();
 
             } catch (MetricNotRegisteredException e) {
                 err.println(e.getMessage());
                 err.println("Field metrics can't be output!");
             }
         }
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
 
             final LANGUAGE language = Settings.getInstance().getLanguage();
             final String extension = language.getExtension();
             final String path = file.getAbsolutePath();
             if (path.endsWith(extension)) {
                 final TargetFileManager targetFiles = DataManager.getInstance()
                         .getTargetFileManager();
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
      * o̓bZ[Wo͗p̃v^
      */
     protected static MessagePrinter out = new DefaultMessagePrinter(new MessageSource() {
         public String getMessageSourceName() {
             return "main";
         }
     }, MESSAGE_TYPE.OUT);
 
     /**
      * G[bZ[Wo͗p̃v^
      */
     protected static MessagePrinter err = new DefaultMessagePrinter(new MessageSource() {
         public String getMessageSourceName() {
             return "main";
         }
     }, MESSAGE_TYPE.ERROR);
 
     /**
      * NX̒` ClassInfoManager ɓo^DAST p[X̌ɌĂяoȂ΂ȂȂD
      */
     private void registClassInfos() {
 
         // NX}l[WC NX}l[W擾
         final UnresolvedClassInfoManager unresolvedClassInfoManager = DataManager.getInstance()
                 .getUnresolvedClassInfoManager();
         final ClassInfoManager classInfoManager = DataManager.getInstance().getClassInfoManager();
 
         // eNXɑ΂
         for (final UnresolvedClassInfo unresolvedClassInfo : unresolvedClassInfoManager
                 .getClassInfos()) {
 
             final FileInfo fileInfo = unresolvedClassInfo.getFileInfo();
 
             //@NX
             final TargetClassInfo classInfo = unresolvedClassInfo.resolve(null, null,
                     classInfoManager, null, null);
 
             fileInfo.addDefinedClass(classInfo);
 
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
         final UnresolvedClassInfoManager unresolvedClassInfoManager = DataManager.getInstance()
                 .getUnresolvedClassInfoManager();
         final ClassInfoManager classInfoManager = DataManager.getInstance().getClassInfoManager();
 
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
         final UnresolvedClassInfoManager unresolvedClassInfoManager = DataManager.getInstance()
                 .getUnresolvedClassInfoManager();
         final ClassInfoManager classInfoManager = DataManager.getInstance().getClassInfoManager();
         final FieldInfoManager fieldInfoManager = DataManager.getInstance().getFieldInfoManager();
         final MethodInfoManager methodInfoManager = DataManager.getInstance()
                 .getMethodInfoManager();
 
         // Os\NXۑ邽߂̃Xg
         final List<UnresolvedClassInfo> unresolvableClasses = new LinkedList<UnresolvedClassInfo>();
 
         // e UnresolvedNXɑ΂
         for (UnresolvedClassInfo unresolvedClassInfo : unresolvedClassInfoManager.getClassInfos()) {
             addInheritanceInformationToClassInfo(unresolvedClassInfo, classInfoManager,
                     fieldInfoManager, methodInfoManager, unresolvableClasses);
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
             final FieldInfoManager fieldInfoManager, final MethodInfoManager methodInfoManager,
             final List<UnresolvedClassInfo> unresolvableClasses) {
 
         // ClassInfo 擾
         final TargetClassInfo classInfo = unresolvedClassInfo.getResolved();
         assert null != classInfo : "classInfo shouldn't be null!";
 
         // eeNXɑ΂
         for (final UnresolvedClassTypeInfo unresolvedSuperClassType : unresolvedClassInfo
                 .getSuperClasses()) {
 
             TypeInfo superClassType = unresolvedSuperClassType.resolve(classInfo, null,
                     classInfoManager, fieldInfoManager, methodInfoManager);
 
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
                     fieldInfoManager, methodInfoManager, unresolvableClasses);
         }
     }
 
     /**
      * tB[h̒` FieldInfoManager ɓo^D registClassInfos ̌ɌĂяoȂ΂ȂȂ
      * 
      */
     private void registFieldInfos() {
 
         // Unresolved NX}l[WCNX}l[WCtB[h}l[W擾
         final UnresolvedClassInfoManager unresolvedClassInfoManager = DataManager.getInstance()
                 .getUnresolvedClassInfoManager();
         final ClassInfoManager classInfoManager = DataManager.getInstance().getClassInfoManager();
         final FieldInfoManager fieldInfoManager = DataManager.getInstance().getFieldInfoManager();
         final MethodInfoManager methodInfoManager = DataManager.getInstance()
                 .getMethodInfoManager();
 
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
         final UnresolvedClassInfoManager unresolvedClassInfoManager = DataManager.getInstance()
                 .getUnresolvedClassInfoManager();
         final ClassInfoManager classInfoManager = DataManager.getInstance().getClassInfoManager();
         final FieldInfoManager fieldInfoManager = DataManager.getInstance().getFieldInfoManager();
         final MethodInfoManager methodInfoManager = DataManager.getInstance()
                 .getMethodInfoManager();
 
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
 
         // RXgN^0̏ꍇ́CftHgRXgN^ǉ
         if (0 == unresolvedClassInfo.getDefinedConstructors().size()) {
             final TargetConstructorInfo defaultConstructor = new TargetConstructorInfo(
                     new HashSet<ModifierInfo>(), ownerClass, false, true, false, false, 0, 0, 0, 0);
             ownerClass.addDefinedConstructor(defaultConstructor);
             methodInfoManager.add(defaultConstructor);
         }
 
         // e UnresolvedNXɑ΂
         for (final UnresolvedClassInfo unresolvedInnerClassInfo : unresolvedClassInfo
                 .getInnerClasses()) {
             registMethodInfos(unresolvedInnerClassInfo, classInfoManager, fieldInfoManager,
                     methodInfoManager);
         }
     }
 
     private void addClassTypeParameterInfos() {
 
         for (final TargetClassInfo classInfo : DataManager.getInstance().getClassInfoManager()
                 .getTargetClassInfos()) {
             addClassTypeParameterInfos(classInfo);
         }
     }
 
     private void addClassTypeParameterInfos(final TargetClassInfo classInfo) {
 
         final List<ClassTypeInfo> superClassTypes = classInfo.getSuperClasses();
         for (final ClassTypeInfo superClassType : superClassTypes) {
 
             final ClassInfo superClassInfo = superClassType.getReferencedClass();
             if (superClassInfo instanceof TargetClassInfo) {
                 addClassTypeParameterInfos((TargetClassInfo) superClassInfo);
 
                 // eNXȏɂ^p[^̎gp擾
                 final Map<TypeParameterInfo, TypeInfo> typeParameterUsages = ((TargetClassInfo) superClassInfo)
                         .getTypeParameterUsages();
                 for (final TypeParameterInfo typeParameterInfo : typeParameterUsages.keySet()) {
                     final TypeInfo usedType = typeParameterUsages.get(typeParameterInfo);
                     classInfo.addTypeParameterUsage(typeParameterInfo, usedType);
                 }
 
                 // ̃NXɂ^p[^̎gp擾
                 final List<TypeInfo> typeArguments = superClassType.getTypeArguments();
                 for (int index = 0; index < typeArguments.size(); index++) {
                     final TypeInfo usedType = typeArguments.get(index);
                     final TypeParameterInfo typeParameterInfo = ((TargetClassInfo) superClassInfo)
                             .getTypeParameter(index);
                     classInfo.addTypeParameterUsage(typeParameterInfo, usedType);
                 }
             }
         }
     }
 
     private void addMethodTypeParameterInfos() {
 
         for (final TargetMethodInfo methodInfo : DataManager.getInstance().getMethodInfoManager()
                 .getTargetMethodInfos()) {
             addMethodTypeParameterInfos(methodInfo);
         }
     }
 
     private void addMethodTypeParameterInfos(final TargetMethodInfo methodInfo) {
 
         //@܂CI[i[NXɂ^p[^gp̏ǉ
         {
             final TargetClassInfo ownerClassInfo = (TargetClassInfo) methodInfo.getOwnerClass();
             final Map<TypeParameterInfo, TypeInfo> typeParameterUsages = ownerClassInfo
                     .getTypeParameterUsages();
             for (final TypeParameterInfo typeParameterInfo : typeParameterUsages.keySet()) {
                 final TypeInfo usedType = typeParameterUsages.get(typeParameterInfo);
                 methodInfo.addTypeParameterUsage(typeParameterInfo, usedType);
             }
         }
 
         // TODO \bhɂ^p[^gpǉׂH
     }
 
     /**
      * \bhI[o[CheMethodInfoɒǉDaddInheritanceInfomationToClassInfos ̌  registMethodInfos
      * ̌ɌĂяoȂ΂ȂȂ
      */
     private void addOverrideRelation() {
 
         // SĂ̑ΏۃNXɑ΂
         for (final TargetClassInfo classInfo : DataManager.getInstance().getClassInfoManager()
                 .getTargetClassInfos()) {
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
 
         final UnresolvedClassInfoManager unresolvedClassInfoManager = DataManager.getInstance()
                 .getUnresolvedClassInfoManager();
         final ClassInfoManager classInfoManager = DataManager.getInstance().getClassInfoManager();
         final FieldInfoManager fieldInfoManager = DataManager.getInstance().getFieldInfoManager();
         final MethodInfoManager methodInfoManager = DataManager.getInstance()
                 .getMethodInfoManager();
 
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
 
         // eRXgN^ɑ΂
         for (final UnresolvedConstructorInfo unresolvedConstructorInfo : unresolvedClassInfo
                 .getDefinedConstructors()) {
 
             // RXgN^̗p֌W
             this.addReferenceAssignmentCallRelation(unresolvedConstructorInfo, unresolvedClassInfo,
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
 
             if (null != unresolvedConditionalBlock.getConditionalClause()) {
                 final ConditionalClauseInfo conditionalClause = unresolvedConditionalBlock
                         .getConditionalClause().resolve(ownerClass, ownerMethod, classInfoManager,
                                 fieldInfoManager, methodInfoManager);
 
                 try {
                     final Class<?> cls = Class
                             .forName("jp.ac.osaka_u.ist.sel.metricstool.main.data.target.ConditionalBlockInfo");
                     final Field filed = cls.getDeclaredField("conditionalClause");
 
                     filed.setAccessible(true);
                     filed.set(localSpace, conditionalClause);
                 } catch (ClassNotFoundException e) {
                     assert false : "Illegal state: ConditionalBlockInfo is not found";
                 } catch (NoSuchFieldException e) {
                     assert false : "Illegal state: conditionalClause is not found";
                 } catch (IllegalAccessException e) {
                     assert false;
                 }
             } else {
                 assert false;
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
             final ExpressionInfo variableUsage = unresolvedVariableUsage.resolve(ownerClass,
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
