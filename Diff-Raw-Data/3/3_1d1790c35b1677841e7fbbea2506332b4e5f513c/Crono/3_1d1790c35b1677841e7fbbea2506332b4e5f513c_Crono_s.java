 package crono;
 
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.InputStream;
 import java.util.LinkedList;
 import java.util.List;
 
 import crono.type.CronoType;
 
 public class Crono {
     public static final Option[] options = {
 	new Option('d', "dynamic"),
 	new Option('D', "debug"),
 	new Option('h', "help"),
 	new Option('p', "print-ast"),
 	new Option('q', "quiet"),
 	new Option('s', "static"),
 	new Option('t', "show-types"),
     };
     public static final String helpstr =
 	"usage Crono [-dDhs]";
     public static final String introstr =
 	"Crono++ by Mark Watts, Carlo Vidal, Troy Varney (c) 2012\n";
     public static final String prompt = "> ";
     
     private static CronoType getStatement(Parser p) {
 	/* This was supposed to loop the parser until it got a valid statement
 	 * or hit EOF, but I can't get it to work quite right */
 	CronoType statement = null;
 	System.out.print(prompt);
 	try {
 	    statement = p.statement();
 	}catch(ParseException pe) {
 	    System.err.println(pe);
 	    statement = null;
 	}
 	
 	return statement;
     }
     
     public static void main(String[] args) {
 	OptionParser optparse = new OptionParser(args);
 	boolean print_ast = false;
 	Interpreter interp = new Interpreter();
 	Visitor v = interp;
 	boolean interactive = (System.console() != null); /*< Java 6 feature */
 	List<String> files = new LinkedList<String>();
 	
 	int opt = optparse.getopt(options);
 	while(opt != -1) {
 	    System.err.printf("OptionParser: got %d(%c)\n", opt, (char)opt);
 	    switch(opt) {
 	    case 'd':
 		interp.dynamic = true;
 		break;
 	    case 'D':
 		interp.dprint_enable = false;
 		break;
 	    case 'h':
 		System.err.println(helpstr);
 		return;
 	    case 'p':
 		print_ast = true;
 		break;
 	    case 'q':
 		interp.dprint_enable = false;
 		break;
 	    case 's':
 		interp.dynamic = false;
 		break;
 	    case 't':
 		interp.getEnv().show_types = true;
 		break;
 		
 	    case '?':
 	    default:
 		System.err.printf("Invalid option: %s\n",
 				  optparse.optchar);
 		System.err.println(helpstr);
 		return;
 	    }
 	    opt = optparse.getopt(options);
 	}
 	
 	for(int i = optparse.optind(); i < args.length; ++i) {
 	    files.add(args[i]);
 	}
 	
 	Parser parser = null;
 	if(print_ast) {
 	    v = new ASTPrinter();
 	}
 	
 	if(interactive && files.size() == 0) {
 	    parser = new Parser(System.in);
 	    System.out.println(introstr);
 	    
 	    System.out.printf("Environment:\n%s\n", interp.env_stack.peek());
 	    
 	    boolean good = false;
 	    CronoType statement = getStatement(parser);
 	    while(statement != null) {
 		try{
 		    statement = statement.accept(v);
 		    System.out.printf("Result: %s\n", statement.toString());
 		}catch(RuntimeException re) {
 		    String message = re.getMessage();
 		    if(message != null) {
 			System.err.println(message);
 		    }else {
 			System.err.println("Unknown Interpreter Error!");
 		    }
 		}
 		statement = getStatement(parser);
 	    }
 	    
 	    System.out.println();
 	}else {
 	    for(String fname : files) {
 		try {
 		    parser = new Parser(new FileInputStream(fname));
 		}catch(FileNotFoundException fnfe) {
 		    System.err.printf("Could not find %s:\n  %s\n", fname,
 				      fnfe.toString());
 		    continue;
 		}
 		
 		try {
 		    CronoType head = parser.program();
 		    head.accept(v);
 		} catch(ParseException pe) {
		    System.err.printf("Error parsing crono file: %s\n  %s\n");
 		}
 		
 		v.reset(); /*< Reset the visitor for the next file */
 	    }
 	}
     }
 }
