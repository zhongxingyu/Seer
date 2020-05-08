 package xtc;
 
 import xtc.lang.cpp.*;
 import xtc.tree.Locatable;
 import xtc.tree.Location;
 import xtc.util.Runtime;
 
 import java.io.*;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * Created with IntelliJ IDEA.
  * User: ckaestne
  * Date: 12/12/12
  * Time: 9:50 AM
  * To change this template use File | Settings | File Templates.
  */
 public class LexerInterface {
 
     /**
      * Preprocessor support for token-creation.
      */
     private final static TokenCreator tokenCreator = new CTokenCreator();
 
 //    public static void main(String[] args) throws Exception {
 //        File f = new File(args[0]);
 //        print(createLexer(new FileReader(f), f, new PrintErrorHandler()));
 //    }
 
     static class XtcLexerException extends RuntimeException {
         public final PresenceConditionManager.PresenceCondition pc;
         public final String msg;
         public final Locatable location;
 
         public XtcLexerException(PresenceConditionManager.PresenceCondition pc, String msg, Locatable loc) {
             super("error[" + pc.toString() + "] " + msg);
             this.pc = pc;
             this.msg = msg;
             this.location = loc;
         }
 
         public String toString() {
             if (location.hasLocation())
                 return String.format("error %s:%d:%d if %s\n\t%s\n", location.getLocation().file, location.getLocation().line, location.getLocation().column, pc.toString(), msg);
             else
                 return String.format("error if %s\n\t%s\n", pc.toString(), msg);
         }
 
     }
 
    static interface ErrorHandler {
         void error(PresenceConditionManager.PresenceCondition pc, String msg, Locatable location);
     }
 
     public static class ExceptionErrorHandler implements ErrorHandler {
         public void error(PresenceConditionManager.PresenceCondition pc, String msg, Locatable location) {
             throw new XtcLexerException(pc, msg, location);
         }
     }
 
     public static class PrintErrorHandler implements ErrorHandler {
         public void error(PresenceConditionManager.PresenceCondition pc, String msg, Locatable location) {
             if (location.hasLocation())
                 System.err.format("error %s:%d:%d if %s\n\t%s\n", location.getLocation().file, location.getLocation().line, location.getLocation().column, pc.toString(), msg);
             else
                 System.err.format("error if %s\n\t%s\n", pc.toString(), msg);
         }
     }
 
     private final static ErrorHandler defaultHandler = new PrintErrorHandler();
 
 
     /**
      * all files to be loaded before the main file (and all macros to be initialized)
      * are initialized through a virtual file "commandline".
      * <p/>
      * The file is read with a separate preprocessor, that's why we may return multiple here
      * continue with the second after the first is done.
      */
     public static List<Stream> createLexer(String commandlineStr,
                                            Reader in,
                                            File file,
                                            final ErrorHandler errorHandler,
                                            List<String> iquote,
                                            List<String> I,
                                            List<String> sysdirs,
                                            XtcMacroFilter macroFilter) throws FileNotFoundException {
 
 //        Reader in = new FileReader(file);
         List<Stream> result = new ArrayList<Stream>();
 
         StopWatch lexerTimer = null;
 
 
         xtc.util.Runtime runtime = new Runtime() {
             @Override
             public void error(PresenceConditionManager.PresenceCondition pc, String msg, Locatable location) {
                 errorHandler.error(pc, msg, location);
                 errors++;
             }
         };
         init(runtime);
         runtime.initDefaultValues();
 
 
         // Initialize the preprocessor with built-ins and command-line
         // macros and includes.
 
         final MacroTable macroTable = new MacroTable(runtime, tokenCreator);
         final PresenceConditionManager presenceConditionManager = new PresenceConditionManager();
 
 
         if (null != commandlineStr || commandlineStr.isEmpty()) {
             Syntax syntax;
             StringReader commandline = new StringReader(commandlineStr);
 
             try {
                 commandline.reset();
             } catch (Exception e) {
                 e.printStackTrace();
             }
 
             HeaderFileManager fileManager = new HeaderFileManager(commandline,
                     new File("<command-line>"),
                     iquote, I, sysdirs, runtime,
                     tokenCreator, lexerTimer);
             Stream preprocessor = new Preprocessor(fileManager, macroTable, presenceConditionManager,
                     tokenCreator, runtime, macroFilter);
 
             result.add(preprocessor);
         }
 
 
         HeaderFileManager fileManager = new HeaderFileManager(in, file, iquote, I, sysdirs, runtime,
                 tokenCreator, lexerTimer);
 
         Stream preprocessor = new Preprocessor(fileManager, macroTable, presenceConditionManager,
                 tokenCreator, runtime, macroFilter);
 
         result.add(preprocessor);
 
         return result;
 
 
     }
 
     public static void print(Stream p) throws IOException {
         Syntax s = p.scan();
         while (s.kind() != Syntax.Kind.EOF) {
             System.out.print(s);
 
             s = p.scan();
         }
 
     }
 
 
     public static void init(Runtime runtime) {
 
         runtime.
                 // Regular preprocessor arguments.
                         word("I", "I", true,
                         "Add a directory to the header file search path.").
                 word("isystem", "isystem", true,
                         "Add a system directory to the header file search path.").
                 word("iquote", "iquote", true,
                         "Add a quote directory to the header file search path.").
                 bool("nostdinc", "nostdinc", false,
                         "Don't use the standard include paths.").
                 word("D", "D", true, "Define a macro.").
                 word("U", "U", true, "Undefine a macro.  Occurs after all -D arguments "
                         + "which is a departure from gnu cpp.").
                 word("include", "include", true, "Include a header.").
 
                 // Extra preprocessor arguments.
                         bool("nobuiltins", "nobuiltins", false,
                         "Disable gcc built-in macros.").
                 bool("nocommandline", "nocommandline", false,
                         "Do not process command-line defines (-D), undefines (-U), or " +
                                 "includes (-include).  Useful for testing the preprocessor.").
                 word("mandatory", "mandatory", false,
                         "Include the given header file even if nocommandline is on.").
                 bool("cppmode", "cppmode", false,
                         "Preprocess without preserving configurations.").
                 word("TypeChef-x", "TypeChef-x", false,
                         "Restricts free macros to those that have the given prefix").
 
                 // SuperC component selection.
                         bool("E", "E", false,
                         "Just do configuration-preserving preprocessing.").
                 bool("lexer", "lexer", false,
                         "Just do lexing and print out the tokens.").
                 bool("lexerNoPrint", "lexerNoPrint", false,
                         "Lex but don't print.").
                 bool("directiveParser", "directiveParser", false,
                         "Just do lexing and directive parsing and print out the tokens.").
                 bool("preprocessor", "preprocessor", false,
                         "Preprocess but don't print.").
                 bool("follow-set", "follow-set", false,
                         "Compute the FOLLOW sets of each token in the preprocessed input.").
 
                 // Preprocessor optimizations.
       /*bool("Odedup", "optimizeDedup", false,
         "Turn off macro definition deduplication.  Not recommended " +
         "except for analysis.")*/
 
                         // FMLR algorithm optimizations.
                         bool("Onone", "doNotOptimize", false,
                         "Turn off all optimizations, but still use the follow-set.").
                 bool("Oshared", "optimizeSharedReductions", true,
                         "Turn on the \"shared reductions\" optimization.").
                 bool("Olazy", "optimizeLazyForking", true,
                         "Turn on the \"lazy forking\" optimization.").
                 bool("Oearly", "optimizeEarlyReduce", true,
                         "Turn on the \"early reduce\" optimization.").
 
                 // Platoff ordering has no effect with the other optimizations.
                         bool("platoffOrdering", "platoffOrdering", false,
                         "Turn on the Platoff ordering optimization.  Off by default.").
 
                 // Deoptimize with early shifts.
                         bool("earlyShift", "earlyShift", false,
                         "Deoptimize FMLR by putting shifts first.  Incompatible with " +
                                 "early reduce.").
 
                 // Other optimizations.
                         bool("noFollowCaching", "noFollowCaching", false,
                         "Turn off follow-set caching.  On by default.").
 
                 // Naive FMLR.
                         bool("naiveFMLR", "naiveFMLR", false,
                         "Naive FMLR Turn off all optimizations and don't "
                                 + "use the follow-set.").
 
                 // Subparser explosion kill switch.
                         word("killswitch", "killswitch", false,
                         "Stop parsing if subparser set reaches or exceeds the given size. "
                                 + "This protects against subparser exponential explosion.  An "
                                 + "error message will be reported.").
 
                 // Statistics, analyses, and timing.
                         bool("preprocessorStatistics", "statisticsPreprocessor", false,
                         "Dynamic analysis of the preprocessor.").
                 bool("languageStatistics", "statisticsLanguage", false,
                         "Dynamic analysis of the language usage.").
                 bool("parserStatistics", "statisticsParser", false,
                         "Parser statistics.").
                 bool("configurationVariables", "configurationVariables", false,
                         "Report a list of all configuration variables.  A configuration " +
                                 "variable is a macro used in a conditional expression before or " +
                                 "without being defined").
                 bool("headerGuards", "headerGuards", false,
                         "Report a list of all header guard macros.  Header guards are " +
                                 "found with gcc's idiom: #ifndef MACRO\\n#define MACRO\\n...\\n" +
                                 "#endif.").
                 bool("size", "size", false,
                         "Report the size, in bytes, of the compilation unit.  This is " +
                                 "the size of the main file plus the size of all headers " +
                                 "for every time each header is included.").
                 bool("time", "time", false,
                         "Running time in milliseconds broken down: " +
                                 "(1) lexer, (2) preprocessor and lexer, and " +
                                 "(3) parser, preprocessor and lexer.").
 
                 // Output and debugging
                         bool("printAST", "printAST", false,
                         "Print the parsed AST.").
                 bool("printSource", "printSource", false,
                         "Print the parsed AST in C source form.").
       /*bool("showCPresenceCondition", "showCPresenceCondition", false,
         "Show scope changes and identifier bindings.").*/
       /*bool("traceIncludes", "traceInclude", false,
         "Show every header entrance and exit.").*/
               bool("showErrors", "showErrors", true,
               "Emit preprocessing and parsing errors to standard err.").
                 bool("showAccepts", "showAccepts", false,
                         "Emit ACCEPT messages when a subparser accepts input.").
                 bool("showActions", "showActions", false,
                         "Show all parsing actions.").
                 bool("macroTable", "macroTable", false,
                         "Show the macro symbol table.")
         ;
     }
 
 }
