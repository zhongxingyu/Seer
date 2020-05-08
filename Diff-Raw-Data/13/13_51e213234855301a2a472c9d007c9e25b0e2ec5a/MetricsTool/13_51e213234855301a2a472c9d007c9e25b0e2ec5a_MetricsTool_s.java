 package jp.ac.osaka_u.ist.sel.metricstool.main;
 
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 
 import org.jargp.ArgumentProcessor;
 import org.jargp.BoolDef;
 import org.jargp.ParameterDef;
 import org.jargp.StringDef;
 
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.TargetFile;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.TargetFileData;
 import jp.ac.osaka_u.ist.sel.metricstool.main.parse.Java15Lexer;
 import jp.ac.osaka_u.ist.sel.metricstool.main.parse.Java15Parser;
 import jp.ac.osaka_u.ist.sel.metricstool.main.util.LANGUAGE;
 import jp.ac.osaka_u.ist.sel.metricstool.main.util.UnavailableLanguageException;
 
 
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
 
     /**
      * 
      * @param args
      *            Ώۃt@C̃t@CpX
      * 
      * ݉D Ώۃt@C̃f[^i[C\͂sD
      */
     public static void main(String[] args) {
 
         Settings settings = new Settings();
         ArgumentProcessor.processArgs(args, parameterDefs, settings);
         checkParameterValidation();
 
         // wv[h̏ꍇ
         if (Settings.isHelpMode()) {
 
             printUsage();
 
             // \[h̏ꍇ
         } else if (Settings.isDisplayMode()) {
 
             // -l Ōꂪw肳ĂȂꍇ́C͉\ꗗ\
             if (Settings.getLanguageString().equals(Settings.INIT)) {
 
                 System.err.println("Available languages;");
                 LANGUAGE[] language = LANGUAGE.values();
                 for (int i = 0; i < language.length; i++) {
                     System.err.println("\t" + language[0].getName()
                             + ": can be specified with term \"" + language[0].getIdentifierName()
                             + "\"");
                 }
 
                 // -l Ōꂪw肳Ăꍇ́C̃vO~OŎgp\ȃgNXꗗ\
             } else {
 
                 try {
                     LANGUAGE language = Settings.getLanguage();
                     // TODO p\gNXꗗ\
                 } catch (UnavailableLanguageException e) {
                     System.err.println(e.getMessage());
                     System.exit(0);
                 }
             }
 
             // ̓[h̏ꍇ
         } else if (!Settings.isHelpMode() && !Settings.isDisplayMode()) {
 
             // fBNgǂݍ
             if (!Settings.getTargetDirectory().equals(Settings.INIT)) {
                 registerFilesFromDirectory();
 
                 // Xgt@Cǂݍ
             } else if (!Settings.getListFile().equals(Settings.INIT)) {
                 registerFilesFromListFile();
             }
 
             TargetFileData targetFiles = TargetFileData.getInstance();
             for (TargetFile targetFile : targetFiles) {
                 try {
                     String name = targetFile.getName();
                     System.out.println("processing " + name);
                     Java15Lexer lexer = new Java15Lexer(new FileInputStream(name));
                     Java15Parser parser = new Java15Parser(lexer);
                     parser.compilationUnit();
                } catch (Exception e) {
                    e.printStackTrace();
                 }
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
      * 
      * ̐mF邽߂̃\bhD sȈw肳ĂꍇCmain \bhɂ͖߂炸C̊֐ŃvOID
      * 
      */
     private static void checkParameterValidation() {
 
         // wv[hƏ\[hɃIɂȂĂꍇ͕s
         if (Settings.isHelpMode() && Settings.isDisplayMode()) {
             System.err.println("-h and -x can\'t be set at the same time!");
             printUsage();
             System.exit(0);
         }
 
         // wv[h̏ꍇ
         if (Settings.isHelpMode()) {
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
 
         // \[h̏ꍇ
         if (Settings.isDisplayMode()) {
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
 
         // ̓[h̏ꍇ
         if (!Settings.isHelpMode() && !Settings.isDisplayMode()) {
 
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
 
             // t@CgNXZoꍇ́C-F w肳ĂȂ͕s
             // TODO
             // NXgNXZoꍇ́C-C w肳ĂȂ͕s
             // TODO
 
             // \bhgNXZoꍇ́C-M w肳ĂȂ͕s
             // TODO
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
      * 
      * Xgt@CΏۃt@Co^D ǂݍ݃G[ꍇ́C̃\bhŃvOID
      */
     private static void registerFilesFromListFile() {
 
         try {
 
             TargetFileData targetFiles = TargetFileData.getInstance();
             for (BufferedReader reader = new BufferedReader(new FileReader(Settings.getListFile())); reader
                     .ready();) {
                 String line = reader.readLine();
                 TargetFile targetFile = new TargetFile(line);
                 targetFiles.add(targetFile);
             }
 
         } catch (FileNotFoundException e) {
             System.err.println("\"" + Settings.getListFile() + "\" is not a valid file!");
             System.exit(0);
         } catch (IOException e) {
             System.err.println("\"" + Settings.getListFile() + "\" can\'t read!");
             System.exit(0);
         }
     }
 
     /**
      * 
      * registerFilesFromDirectory(File file)Ăяô݁D main\bh new
      * File(Settings.getTargetDirectory) ̂Cߍ쐬D
      * 
      */
     private static void registerFilesFromDirectory() {
 
         File targetDirectory = new File(Settings.getTargetDirectory());
         registerFilesFromDirectory(targetDirectory);
     }
 
     /**
      * 
      * @param path
      *            Ώۃt@C܂̓fBNg
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
 
             try {
                 LANGUAGE language = Settings.getLanguage();
                 String extension = language.getExtension();
                 String path = file.getAbsolutePath();
                 if (path.endsWith(extension)) {
                     TargetFileData targetFiles = TargetFileData.getInstance();
                     TargetFile targetFile = new TargetFile(path);
                     targetFiles.add(targetFile);
                 }
             } catch (UnavailableLanguageException e) {
                 System.err.println(e.getMessage());
                 System.exit(0);
             }
 
             // fBNgłt@CłȂꍇ͕s
         } else {
             System.err.println("\"" + file.getAbsolutePath() + "\" is not a vaild file!");
             System.exit(0);
         }
     }
 }
