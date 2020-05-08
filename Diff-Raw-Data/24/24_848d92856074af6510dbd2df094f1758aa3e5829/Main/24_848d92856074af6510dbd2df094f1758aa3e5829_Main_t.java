 package min;
 
 import java.io.IOException;
 import java.nio.file.FileVisitResult;
 import java.nio.file.Files;
 import java.nio.file.Path;
 import java.nio.file.Paths;
 import java.nio.file.SimpleFileVisitor;
 import java.nio.file.attribute.BasicFileAttributes;
 
 import com.google.javascript.jscomp.CompilationLevel;
 import com.google.javascript.jscomp.Compiler;
 import com.google.javascript.jscomp.CompilerOptions;
 import com.google.javascript.jscomp.DiagnosticType;
 import com.google.javascript.jscomp.SourceFile;
 
 public class Main {
     private static final String UTF8 = "UTF-8";
     private static final SourceFile extern = SourceFile.fromCode("extern.js",
             "");
     private static final CompilerOptions options = new CompilerOptions();
     private static Path inputPath;
     private static Path outputPath;
 
     /** From Google Closure Compiler. **/
     static final String SUSPICIOUS_COMMENT_WARNING = "Non-JSDoc comment has annotations. "
             + "Did you mean to start it with '/**'?";
 
     /** From Google Closure Compiler. **/
     static final DiagnosticType PARSE_ERROR = DiagnosticType.error(
             "JSC_PARSE_ERROR", "Parse error. {0}");
 
     static {
         // Avoid IE8 warnings from closure.
         // Comment out ECMASCRIPT5 for lots of errors.
         options.setLanguageIn(CompilerOptions.LanguageMode.ECMASCRIPT5);
         // options.setWarningLevel(DiagnosticGroups.NON_STANDARD_JSDOC,
         // CheckLevel.OFF);
         // options.setWarningLevel(DiagnosticGroups.FILEOVERVIEW_JSDOC,
         // CheckLevel.OFF);
 
         // Disable warning about PARSE ERRORS. Not a good idea because it hides
         // critical errors.
         // options.setWarningLevel(DiagnosticGroup.forType(PARSE_ERROR),
         // CheckLevel.OFF);
 
         //
         // options.setWarningLevel(DiagnosticGroup.forType(DiagnosticType.warning(
         // SUSPICIOUS_COMMENT_WARNING, "")), CheckLevel.OFF);
         // IRFactory.java
         // errorReporter.warning(
         // SUSPICIOUS_COMMENT_WARNING,
         // sourceName,
         // comment.getLineno(), "", 0);
 
         // UTF-8 not ASCII
         options.setOutputCharset(UTF8);
         // WHITESPACE_ONLY, SIMPLE_OPTIMIZATIONS, or ADVANCED_OPTIMIZATIONS
         // errors on simple opts.
         // loads on whitespace.. doesn't work though
         CompilationLevel.WHITESPACE_ONLY.setOptionsForCompilationLevel(options);
         // CompilationLevel.SIMPLE_OPTIMIZATIONS.setOptionsForCompilationLevel(options);
     }
 
     private static void min(final String inputFile, final Path outputFile) {
         final SourceFile input = SourceFile.fromFile(inputFile);
 
         // Compiler only compiles once.
         final Compiler compiler = new Compiler();
         compiler.compile(extern, input, options);
 
         try {
             // Make sure the directories exist.
             Files.createDirectories(outputFile.getParent());
             Files.write(outputFile, compiler.toSource().getBytes(UTF8));
         } catch (IOException e) {
             e.printStackTrace();
         }
     }
 
     static class JavaScriptVisitor extends SimpleFileVisitor<Path> {
         private static final String outputPathStr = outputPath.toString();
        private static final Path inputPathParent = inputPath.getParent();
 
         @Override
         public FileVisitResult visitFile(final Path file,
                 final BasicFileAttributes attrs) throws IOException {
 
             final String in = file.toString();
             if (in.toLowerCase().endsWith(".js")) {
                 // Join outputPathStr with relativized file.
                 final Path out = Paths.get(outputPathStr,
                        inputPathParent.relativize(file).toString())
                         .normalize();
 
                System.out.println(out);
                 min(in, out);
             }
 
             return FileVisitResult.CONTINUE;
         }
     }
 
     private static void exit() {
         System.exit(0);
     }
 
     /**
      * Walk first path for visit result.
      * 
      * sourceFolder - arg[0]
      * 
      * destFolder - arg[1]
      * 
      * @param args
      */
     public static void main(String[] args) {
//        args = new String[2];
//        args[0] = "ace/lib/ace";
//        args[1] = "gollum/lib/gollum/frontend/public/gollum/livepreview/js/ace/lib";
 
         if (args.length != 2) {
             System.out.println("sourceFolder destFolder");
             exit();
         }
 
        inputPath = Paths.get(args[0]);
        outputPath = Paths.get(args[1]);

         try {
             Files.walkFileTree(inputPath, new Main.JavaScriptVisitor());
         } catch (Exception e) {
             e.printStackTrace();
         }
         exit();
     }
 }
