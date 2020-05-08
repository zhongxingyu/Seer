 package krum.jplex;
 
 import java.io.File;
 
 import krum.jplex.input.LexerSpec;
 import krum.jplex.input.XMLInput;
 import krum.jplex.output.CodeModelOutput;
 
 
 /**
  * Main class of the JPlex lexer generator.  JPlex lexers are NIO-based,
  * DFA-powered, push-driven, and fully decoupled and reusable. Their state
  * machines are precompiled and serialized, so run time initialization is
  * reasonably fast no matter how large or complex the expression set.
  * <p>
  * JPlex is not a parser generator.  It generates a listener interface with
  * methods corresponding to events associated with regular expressions.  When
  * an expression is matched, the lexer calls the corresponding method on its
  * listeners with the matching input as the argument.  JPlex is ideal for
  * processing input that lacks a deep tree structure, does not follow a strict
  * grammar, or contains only occasional items of interest.  Examples include
  * stream control protocols like Telnet or ANSI x3.64, or the output of old
  * text-based games like TradeWars 2002.
  * <p>
  * Because JPlex lexers use deterministic finite automata, they do not support
  * lookaround.  This means their regular expression language does not support
  * the common beginning and end of line operators, '$' and '^'.  These can be
  * simulated by matching a newline character at the beginning or end of an
  * expression.
  * <p>
  * JPlex supports multiple lexical states with push, pop, and jump.  This
  * makes it easy to recognize patterns like nested block comments.  In states
  * that are not declared <tt>strict</tt>, unrecognized input will be discarded
  * in the lexer's innermost loop.
  * 
  * @author Kevin Krumwiede (kjkrum@gmail.com)
  */
 public class JPlex {
 	/* 
 	 * BASIC OPERATION:
 	 * The main class constructs a krum.jplex.input.LexerSpec from an
 	 * XML file specified on the command line.  It passes that object to the
 	 * constructor of a krum.jplex.output.CodeModelOutput.  The
 	 * CodeModelOutput generates the lexer source and compiles and serializes
 	 * the automatons.
 	 * 
 	 * NOTE:
 	 * This program depends heavily on object identity as opposed to object
 	 * equality.  None of the classes in krum.jplex.input override
 	 * equals or hashCode, so they rely on identity for the correct behavior
 	 * of HashMap and other collections.  Anyone who modifies this program
 	 * should be wary of creating copies of those objects.
 	 * 
 	 * TODO:
 	 * Change state enum into a class with public static instances; make
 	 * strictness, etc. state fields and get rid of maps in lexer class.
 	 */
 
 	public static final String VERSION = "1.1";
 	
 	public static void main(String[] args) {
 		System.out.println("JPlex version " + VERSION);
 		
 		if(args.length < 1 || args.length > 3) {
 			System.err.println("usage: jplex input-file [java-dir [resource-dir]]");	
 			System.exit(1);
 		}
 		
 		File inputFile = new File(args[0]);
 		File javaDir;
 		if(args.length >= 2) {
 			javaDir = new File(args[1]);
 		}
 		else {
 			javaDir = new File(".");
 		}
 		File resourceDir;
 		if(args.length == 3) {
 			resourceDir = new File(args[2]);
 		}
 		else {
 			resourceDir = javaDir;
 		}
 		
 		try {
 			LexerSpec spec = XMLInput.load(inputFile);
 			CodeModelOutput cm = new CodeModelOutput(spec);
 			cm.build(javaDir, resourceDir);
 		} catch (Exception e) {
 			String msg = e.getMessage();
 			if(msg != null) System.err.println(msg);
 			else e.printStackTrace();
 		}
 	}
 }
