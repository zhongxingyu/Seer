 import org.antlr.runtime.*;
 import org.antlr.runtime.tree.*;
 import org.antlr.stringtemplate.*;
 
 import org.apache.commons.cli.*;
 
 import java.io.*;
 import java.util.Vector;
 import java.util.HashMap;
 
 /**
  * This is the base of our compiler.
  *
  * It deals with command line arguments, and sets the flow of things.
  *
  * @author Nat Welch
  * @author Ben Sweedler
  */
 public class Evil {
    private static boolean debugFlag = false;
    public static CommandLine cmd = null;
   private static String _inputFile = null;
 
    public static void main(String[] args) {
       // Store the options
       parseParameters(args);
 
       CommonTokenStream tokens = new CommonTokenStream(createLexer());
       EvilParser parser = new EvilParser(tokens);
       EvilParser.program_return ret = null;
 
       try {
          ret = parser.program();
       } catch (org.antlr.runtime.RecognitionException e) {
          error(e.toString());
       }
 
       CommonTree t = (CommonTree)ret.getTree();
       if (cmd.hasOption("displayAST") && t != null) {
          DOTTreeGenerator gen = new DOTTreeGenerator();
          StringTemplate st = gen.toDOT(t);
          System.out.println(st);
       }
 
       // To create and invoke a tree parser.  Modify with the appropriate
       // name of the tree parser and the appropriate start rule.
       try {
          CommonTreeNodeStream nodes = new CommonTreeNodeStream(t);
          nodes.setTokenStream(tokens);
          TypeCheck tparser = new TypeCheck(nodes);
 
          tparser.verify();
       } catch (org.antlr.runtime.RecognitionException e) {
          error(e.toString());
       }
    }
 
    /**
     * Defines possible options and sets them up.
     *
     * see http://commons.apache.org/cli/usage.html for more details.
     */
    private static void parseParameters(String [] args) {
       // create the command line parser
       CommandLineParser parser = new PosixParser();
 
       // create the options
       Options options = new Options();
       options.addOption("a", "displayAST", false, "Print out a dotty graph of the AST." );
       options.addOption("d", "debug", false, "Print debug messages while running." );
       options.addOption("h", "help", false, "Print this help message." );
 
       try {
          // parse the command line arguments
          cmd = parser.parse( options, args );
 
          if (cmd.hasOption("help")) {
             // automatically generate the help statement
             HelpFormatter formatter = new HelpFormatter();
             formatter.printHelp("java Evil [options] filename.ev", options);
             System.exit(0);
          }
 
          if (cmd.hasOption("debug")) {
             debugFlag = true;
          }
 
          String[] fileArgs = cmd.getArgs();
 
          if (fileArgs.length > 1) {
             error("Too many files to compile.");
          } else if (fileArgs.length == 0) {
            _inputFile = null;
          } else {
            _inputFile = fileArgs[0];
          }
       } catch (ParseException exp) {
          error("Unexpected exception:" + exp.getMessage());
       }
    }
 
    public static void error(String msg) {
       error(msg, 0);
    }
 
    public static void error(String msg, int lineno) {
      System.err.println(lineno + " : " + msg);
      //System.err.println(_inputFile + ":" + lineno + " : " + msg);
       System.exit(1);
    }
 
    public static void debug(String msg) {
       if (debugFlag) {
          System.err.println(msg);
       }
    }
 
    private static EvilLexer createLexer() {
       try {
          ANTLRInputStream input;
         if (_inputFile == null)
         {
            input = new ANTLRInputStream(System.in);
         } else {
            input = new ANTLRInputStream(
                  new BufferedInputStream(new FileInputStream(_inputFile)));
         }
          return new EvilLexer(input);
       } catch (java.io.IOException e) {
         System.err.println("file not found: " + _inputFile);
          System.exit(1);
          return null;
       }
    }
 }
