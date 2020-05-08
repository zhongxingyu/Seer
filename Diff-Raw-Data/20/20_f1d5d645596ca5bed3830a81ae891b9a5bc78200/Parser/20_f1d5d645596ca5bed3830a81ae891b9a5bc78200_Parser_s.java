 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.util.*;
 import java.util.regex.*;
 import javax.swing.JFileChooser;
 
 /** The parser and interpreter.
     The top level parse function, a main method for testing, and several
     utility methods are provided.
     You need to implement parseProgram and all the rest of the parser.
     */
 
 public class Parser {
 
     /**
      * Top level parse method, called by the World
      */
     static RobotProgramNode parseFile(File code){
 	Scanner scan = null;
 	try {
 	    scan = new Scanner(code);
 
 	    // the only time tokens can be next to each other is
 	    // when one of them is one of (){},;
 	    scan.useDelimiter("\\s+|(?=[{}(),;])|(?<=[{}(),;])");
 
 	    RobotProgramNode n = parseProgram(scan);  // You need to implement this!!!
 
 	    scan.close();
 	    return n;
 	} catch (FileNotFoundException e) {
 	    System.out.println("Robot program source file not found");
 	} catch (ParserFailureException e) {
 	    System.out.println("Parser error:");
 	    System.out.println(e.getMessage());
 	    scan.close();
 	}
 	return null;
     }
 
     /** For testing the parser without requiring the world */
 
     public static void main(String[] args){
 	if (args.length>0){
 	    for (String arg : args){
 		File f = new File(arg);
 		if (f.exists()){
 		    System.out.println("Parsing '"+ f+"'");
 		    RobotProgramNode prog = parseFile(f);
 		    System.out.println("Parsing completed ");
 		    if (prog!=null){
 			System.out.println("================\nProgram:");
 			System.out.println(prog);}
 		    System.out.println("=================");
 		}
 		else {System.out.println("Can't find file '"+f+"'");}
 	    }
 	} else {
 	    while (true){
 		JFileChooser chooser = new JFileChooser(".");//System.getProperty("user.dir"));
 		int res = chooser.showOpenDialog(null);
 		if(res != JFileChooser.APPROVE_OPTION){ break;}
 		RobotProgramNode prog = parseFile(chooser.getSelectedFile());
 		System.out.println("Parsing completed");
 		if (prog!=null){
 		    System.out.println("Program: \n"+prog);
 		}
 		System.out.println("=================");
 	    }
 	}
 	System.out.println("Done");
     }
 
     // Useful Patterns
 
     private static Pattern NUMPAT = Pattern.compile("-?\\d+");  //("-?(0|[1-9][0-9]*)");
     private static Pattern OPENPAREN = Pattern.compile("\\(");
     private static Pattern CLOSEPAREN = Pattern.compile("\\)");
     private static Pattern OPENBRACE = Pattern.compile("\\{");
     private static Pattern CLOSEBRACE = Pattern.compile("\\}");
     
     /**    PROG  ::= STMT+
      */
     static RobotProgramNode parseProgram(Scanner s){
	//THE PARSER GOES HERE!
 
 	return null;     // just so it will compile!!
     }
 
 
 
 
 
 
 
 
     //utility methods for the parser
     /**
      * Report a failure in the parser.
      */
     static void fail(String message, Scanner s){
 	String msg = message + "\n   @ ...";
 	for (int i=0; i<5 && s.hasNext(); i++){
 	    msg += " " + s.next();
 	}
 	throw new ParserFailureException(msg+"...");
     }
 
     /**
        If the next token in the scanner matches the specified pattern,
        consume the token and return true. Otherwise return false without
        consuming anything.
        Useful for dealing with the syntactic elements of the language
        which do not have semantic content, and are there only to
        make the language parsable.
      */
     static boolean gobble(String p, Scanner s){
 	if (s.hasNext(p)) { s.next(); return true;} 
 	else { return false; } 
     }
     static boolean gobble(Pattern p, Scanner s){
 	if (s.hasNext(p)) { s.next(); return true;} 
 	else { return false; } 
     }
 
 
 }
 
 // You could add the node classes here, as long as they are not declared public (or private)
