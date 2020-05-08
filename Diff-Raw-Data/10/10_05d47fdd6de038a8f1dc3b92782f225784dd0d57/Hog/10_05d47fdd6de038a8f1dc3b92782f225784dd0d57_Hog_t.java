 /**
  * 
  */
 package front_end;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.logging.Logger;
 
 import back_end.CodeGeneratingVisitor;
 import back_end.SymbolTableVisitor;
 import back_end.TypeCheckingVisitor;
 
 import util.ast.AbstractSyntaxTree;
 import util.ast.node.ProgramNode;
 
 /**
  * The Hog compiler.
  * 
  * Usage:<br/>
  * <br/>
  * 
  * <code>hog [--hdfs|--local] source [arg1, arg2, ...]<br/><br/></code>
  * 
  * For example,<br/>
  * <br/>
  * 
  * <code>hog --local WordCount.hog --input war_and_peace.txt --output
  * war_and_peace_counts.txt</code><br/>
  * <br/>
  * 
  * Compiles the program <code>WordCount.hog</code>, and runs the program on a
  * local Hadoop cluster on the file <code>war_and_peace.txt</code>. This class
  * handles copying <code>war_and_peace.txt</code> to HDFS (Hadoop's Distributed
  * File System). The class also handles downloading the output to the file
  * <code>war_and_peace_counts.txt</code>.
  * 
  * @author Samuel Messing
  * @author Kurry Tran
  * 
  */
 public class Hog {
 
 	// logger used for all nodes
 	protected final static Logger LOGGER = Logger
 			.getLogger(Hog.class.getName());
 	protected static boolean local = true;
 	protected static boolean hasInput;
 	protected static boolean hasOutput;
 	protected static FileReader source;
 	protected static String input;
 	protected static String output;
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		usage(args);
 
 		Parser parser = new Parser(new Lexer(source));
 		ProgramNode root = null;
 
 		try {
 			root = (ProgramNode) parser.parse().value;
 		} catch (FileNotFoundException e) {
 			LOGGER.severe("Hog program " + source + " not found!");
 			System.exit(1);
 		} catch (Exception ex) {
 			ex.printStackTrace();
 		}
 
 		// root.print();
 
 		AbstractSyntaxTree tree = new AbstractSyntaxTree(root);
 		System.out.println("Generating symbol tables...");
 		// generate/populate symbol tables
 		SymbolTableVisitor symbolVisitor = new SymbolTableVisitor(tree);
 		symbolVisitor.walk();
 		System.out.println("Populating types...");
 		// populate/propagate/check types
 		TypeCheckingVisitor typeVisitor = new TypeCheckingVisitor(tree);
 		typeVisitor.walk();
 		System.out.println("Generating Java source...");
 		// generate source code:
 		CodeGeneratingVisitor codeGenerator = new CodeGeneratingVisitor(tree);
 		codeGenerator.walk();
 
 		FileWriter fstream = null;
 		try {
 			fstream = new FileWriter("Hog.java");
 			BufferedWriter out = new BufferedWriter(fstream);
 			out.write(codeGenerator.getCode());
 			out.close();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 
 	}
 
 	/**
 	 * Processes the input to this classes main function.
 	 * 
 	 * @param args
 	 *            the passed in command-line arguments.
 	 */
 	private static void usage(String[] args) {
 
 		if (args.length < 2) {
 			die();
 		}
 
 		if (args[0].equals("--help")) {
 			printUsage();
 		}
 
 		if (args.length % 2 != 0) {
 			LOGGER.severe("Invalid arguments");
 			die();
 		}
 
 		local = args[0].equals("--hdfs") ? false : true;
 
 		try {
 			source = new FileReader(args[1]);
 		} catch (FileNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		if (!args[1].endsWith(".java") && (!args[1].endsWith(".hog"))) {
 			die();
 		}
 
 		if (args.length == 6) {
 
 			input = args[2].equals("--input") ? args[3] : args[5];
 			output = args[2].equals("--output") ? args[3] : args[5];
 			hasInput = hasOutput = true;
 
 		} else if (args.length == 4) {
 
 			input = args[2].equals("--input") ? args[3] : "";
 			output = args[2].equals("--output") ? args[3] : "";
 			hasInput = input.equals("") ? false : true;
 			hasOutput = output.equals("") ? false : true;
 		}
 
 	}
 
 	/**
 	 * Prints usage information and exits.
 	 */
 	private static void die() {
 		printUsage();
 		System.exit(1);
 	}
 
 	/**
 	 * Prints the usage information for the main method in this class to
 	 * standard out.
 	 */
 	private static void printUsage() {
 		System.out.println("Hog --- a scripting MapReduce language");
 		System.out
 				.println("Usage: Hog [--hdfs|--local] source [--input file] [--output file]");
 	}
 
 }
