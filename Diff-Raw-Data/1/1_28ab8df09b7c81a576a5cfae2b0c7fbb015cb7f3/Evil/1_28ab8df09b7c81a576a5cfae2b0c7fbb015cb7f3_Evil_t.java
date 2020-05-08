 import java.io.*;
 import java.util.*;
 
 // For Antlr
 import org.antlr.runtime.*;
 import org.antlr.runtime.tree.*;
 import org.antlr.stringtemplate.*;
 
 // For cli argument parsing
 import org.apache.commons.cli.*;
 
 /**
  * This is the base of our compiler.
  *
  * It deals with command line arguments, and sets the flow of things.
  *
  * @author Nat Welch
  * @author Ben Sweedler
  */
 public class Evil {
    public static CommandLine cmd = null;
    private static String inputFile = null;
 
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
 
       // To create and invoke a tree parser. Modify with the appropriate
       // name of the tree parser and the appropriate start rule.
       try {
          CommonTreeNodeStream nodes = new CommonTreeNodeStream(t);
          nodes.setTokenStream(tokens);
          TypeCheck tparser = new TypeCheck(nodes);
 
          if (typeFlag)
             tparser.verify();
 
          nodes.reset();
          CFG cfg = new CFG(nodes);
          cfg.symTable = tparser.symTable;
 
          cfg.build();
 
          if (opt1Flag) {
             if (!quietFlag)
                System.out.println("Local value numbering and copy propagation.");
 
             LocalValueNumbering lvn = new LocalValueNumbering();
             LocalCopyPropagation lcp = new LocalCopyPropagation();
 
             for (Node n : cfg.nodeTable.getAllNodes())  {
                lvn.optimize(n);
                lcp.optimize(n);
             }
          }
 
          cfg.nodeTable.computeLiveSets();
          SparcRegisters.setupRegisters();
 
          if (opt2Flag) {
             if (!quietFlag)
                System.out.println("Removing dead code.");
 
             DeadCodeRemoval.deleteUselessInstructions(cfg.nodeTable);
             cfg.nodeTable.computeLiveSets();
          }
 
          String iloc = cfg.dump();
 
          RegisterAllocator allocator = new RegisterAllocator();
          allocator.buildGraph(cfg.nodeTable);
          allocator.colorGraph();
          allocator.transformCode(cfg.nodeTable);
 
          if (dumpFlag) {
             try {
                String outFile = new String(inputFile);
                outFile = outFile.replaceFirst(".ev", ".il");
                FileWriter fstream = new FileWriter(outFile);
                BufferedWriter out = new BufferedWriter(fstream);
                out.write(iloc);
                out.close();
 
                if (!quietFlag)
                   System.out.println("ILOC written to: " + outFile);
             } catch (Exception e) {
                System.err.println("File Write Error: " + e.getMessage());
             }
          }
 
          // Output sparc
          String sparc = cfg.toSparc();
          try {
             String outFile = new String(inputFile);
             outFile = outFile.replaceFirst(".ev", ".s");
             FileWriter fstream = new FileWriter(outFile);
             BufferedWriter out = new BufferedWriter(fstream);
 
             out.write("\t.section\t\".text\"\n");
 
             out.write(sparc);
 
             out.write(ReadOnlyData.getInstance().toString());
 
             out.close();
 
             if (!quietFlag)
                System.out.println("Sparc written to: " + outFile);
          } catch (Exception e) {
             System.err.println("File Write Error: " + e.getMessage());
          }
       } catch (org.antlr.runtime.RecognitionException e) {
          error(e.toString());
       }
 
       System.exit(0);
    }
 
    // Input Flags
    private static boolean debugFlag = false;
    private static boolean dumpFlag = false;
    private static boolean typeFlag = true;
    private static boolean opt1Flag = false;
    private static boolean opt2Flag = false;
    private static boolean quietFlag = false;
 
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
       options.addOption("?", "help", false, "Print this help message." );
       options.addOption("i", "dumpIL", false, "Dump ILOC to a file." );
       options.addOption("t", "notype", false, "Don't Typecheck." );
       options.addOption("o1", "opt1", false, "Local Value Numbering Optimization." );
       options.addOption("o2", "opt2", false, "Useless Code Removal Optimization." );
       options.addOption("q", "quiet", false, "Run with no output." );
 
       try {
          // parse the command line arguments
          cmd = parser.parse( options, args );
 
          if (cmd.hasOption("help")) {
             Evil.help(options);
          }
 
          if (cmd.hasOption("debug")) {
             debugFlag = true;
          }
 
          if (cmd.hasOption("dumpIL")) {
             dumpFlag = true;
          }
 
          if (cmd.hasOption("notype")) {
             typeFlag = false;
          }
 
          if (cmd.hasOption("opt1")) {
             opt1Flag = true;
          }
 
          if (cmd.hasOption("opt2")) {
             opt2Flag = true;
          }
 
          if (cmd.hasOption("quiet")) {
             quietFlag = true;
          }
 
          String[] fileArgs = cmd.getArgs();
 
          // Set input file.
          if (fileArgs.length > 1) {
             error("Only one file can be compiled at a time.");
          } else if (fileArgs.length == 0) {
             Evil.help(options);
          } else {
             inputFile = fileArgs[0];
          }
       } catch (ParseException exp) {
          error("Unexpected exception:" + exp.getMessage());
       }
    }
 
    public static void error(String msg) {
       error(msg, 0);
    }
 
    public static void help(Options o) {
       // automatically generate the help statement
       HelpFormatter formatter = new HelpFormatter();
       formatter.printHelp("ecc [options] filename.ev", o);
       System.exit(0);
    }
 
    public static void error(String msg, int lineno) {
       if (inputFile == null)
          System.err.println(lineno + " : " + msg);
       else
          System.err.println(inputFile + ":" + lineno + " : " + msg);
 
       System.exit(1);
    }
 
    public static void debug(String msg) {
       if (debugFlag) {
          System.err.println(msg);
       }
    }
 
    public static void warning(String msg) {
       System.err.println("WARNING: " + msg);
    }
 
    private static EvilLexer createLexer() {
       try {
          ANTLRInputStream input;
 
          input = new ANTLRInputStream(new BufferedInputStream(new FileInputStream(inputFile)));
 
          return new EvilLexer(input);
       } catch (java.io.IOException e) {
          System.err.println("file not found: " + inputFile);
          System.exit(1);
          return null;
       }
    }
 }
