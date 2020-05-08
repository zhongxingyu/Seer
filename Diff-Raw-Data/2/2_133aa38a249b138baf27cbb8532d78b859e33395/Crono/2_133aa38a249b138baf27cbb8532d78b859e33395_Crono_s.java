 package crono;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.InputStreamReader;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.LinkedList;
 import java.util.List;
 
 import crono.type.CronoType;
 
 public class Crono {
     public static final Option[] options = {
         new Option('d', "dynamic"),
         new Option('D', "debug"),
         new Option('e', "show-environment"),
         new Option('h', "help"),
         new Option('p', "print-ast"),
         new Option('P', "no-prelude"),
         new Option('q', "quiet"),
         new Option('s', "static"),
         new Option('t', "show-types"),
         new Option('T', "trace")
     };
     public static final String helpstr =
         "usage Crono [-dDhs]";
     public static final String introstr =
         "Crono++ by Mark Watts, Carlo Vidal, Troy Varney (c) 2012\n";
     public static final String prompt = "> ";
     public static final String prelude = "./prelude.lisp";
     public static boolean showTypes = false;
     public static boolean interactive = false;
     public static boolean loadPrelude = true;
     public static Visitor v = null;
     public static List<String> files = new LinkedList<String>();
     
     private static CronoType getStatement(Parser p) {
         System.out.print(prompt);
         try {
             CronoType statement = p.statement();
             if(!interactive) {
                System.out.println(statement);
             }
             return statement;
         }catch(ParseException pe) {
             System.err.println(pe);
         }
         return null;
     }
     
     public static boolean parseOptions(Interpreter interp, String[] args) {
         OptionParser optparse = new OptionParser(args);
         int opt = optparse.getopt(options);
         while(opt != -1) {
             switch(opt) {
             case 'd':
                 interp.dynamic(true);
                 break;
             case 'D':
                 interp.debug(true);
                 break;
             case 'e':
                 interp.showEnv(true);
                 break;
             case 'h':
                 System.err.println(helpstr);
                 return false;
             case 'p':
                 interp.printAST(true);
                 break;
             case 'P':
                 loadPrelude = false;
                 break;
             case 'q':
                 interp.showEnv(false);
                 interp.printAST(false);
                 interp.trace(false);
                 interp.debug(false);
                 break;
             case 's':
                 interp.dynamic(false);
                 break;
             case 't':
                 showTypes = true;
                 interp.getEnv().show_types = true;
                 break;
             case 'T':
                 interp.trace(true);
                 break;
             case '?':
             default:
                 System.err.printf("Invalid option: %s\n",
                                   optparse.optchar);
                 System.err.println(helpstr);
                 return false;
             }
             opt = optparse.getopt(options);
         }
         
         for(int i = optparse.optind(); i < args.length; ++i) {
             files.add(args[i]);
         }
         return true;
     }
     
     public static void main(String[] args) {
         Interpreter interp = new Interpreter();
         v = interp;
         interactive = (System.console() != null); /*< Java 6 feature */
         
         parseOptions(interp, args);
         
         try {
             File package_dir = new File("./packages/");
             CronoPackage.initLoader(new URL[]{package_dir.toURI().toURL()});
         }catch(MalformedURLException murle) {
             System.err.printf("Crono: Could not open package directory!\n");
         }
         
         if(files.size() == 0) {
             interactive();
         }else {
             for(String fname : files) {
                 loadPrelude();
                 parseFile(fname);
                 v.reset(); /*< Reset the visitor for the next file */
             }
         }
     }
     
     public static void loadPrelude() {
         if(loadPrelude) {
             parseFile(prelude);
         }
     }
     
     public static void interactive() {
         System.out.println(introstr);
         loadPrelude();
         
         Parser parser = new Parser(new InputStreamReader(System.in));
         CronoType statement = getStatement(parser);
         while(statement != null) {
             try{
                 statement = statement.accept(v);
                 if(showTypes) {
                     System.out.printf("Result: %s [%s]\n",statement.repr(),
                                       statement.typeId());
                 }else {
                     System.out.printf("Result: %s\n", statement.repr());
                 }
             }catch(InterpreterException re) {
                 String message = re.getMessage();
                 if(message != null) {
                     System.err.println(message);
                 }else {
                     System.err.println("Unknown Interpreter Error!");
                 }
             }catch(RuntimeException re) {
                 re.printStackTrace();
             }
             statement = getStatement(parser);
         }
         
         System.out.println();
     }
     
     
     public static void parseFile(String fname) {
         Parser parser = null;
         try {
             parser = new Parser(new FileReader(fname));
         }catch(FileNotFoundException fnfe) {
             System.err.printf("Could not find file %s:\n  %s\n", fname, fnfe);
             return;
         }
         try {
             CronoType[] prog = parser.program();
             for(int i = 0; i < prog.length; ++i) {
                 prog[i].accept(v);
             }
         }catch(ParseException pe) {
             System.err.printf("Error parsing crono file: %s\n  %s\n",fname,pe);
             return;
         }
     }
 }
