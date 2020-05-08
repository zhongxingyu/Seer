 /**
  *
  */
 package ca.uwaterloo.joos;
 
 import java.io.File;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import ca.uwaterloo.joos.ast.ASTTreeGenerator;
 import ca.uwaterloo.joos.parser.LR1;
 import ca.uwaterloo.joos.parser.LR1Parser;
 import ca.uwaterloo.joos.parser.ParseTree;
 import ca.uwaterloo.joos.scanner.DFA;
 import ca.uwaterloo.joos.scanner.Scanner;
 import ca.uwaterloo.joos.scanner.Token;
 
 /**
  * @author Greg Wang
  *
  */
 public class Main {
 	private static final Logger logger = Main.getLogger(Main.class);
 
 	public static Logger getLogger(Class<?> cls) {
 		return Logger.getLogger(Main.class.getName());
 	}
 
 	private final Scanner scanner;
 	private final LR1Parser parser;
 	private final Preprocessor preprocessor;
 
 	public Main() {
 		logger.setLevel(Level.INFO);
 
 		// Construct Preprocessor
 		this.preprocessor = new Preprocessor();
 
 		// Read a DFA from file
 		DFA dfa = null;
 		try {
 			dfa = new DFA(new File("resources/joos.dfa"));
 		} catch (Exception e) {
 			System.err.println("ERROR: Invalid DFA File format: " + e.getLocalizedMessage() + " " + e.getClass().getName());
 			System.exit(-1);
 		}
 		// Construct Scanner
 		this.scanner = new Scanner(dfa);
 
 		// Read a LR1 from file
 		LR1 lr1 = null;
 		try {
 			lr1 = new LR1(new File("resources/joos.lr1"));
 		} catch (Exception e) {
 			System.err.println("ERROR: Invalid LR1 File format: " + e.getLocalizedMessage() + " " + e.getClass().getName());
 			e.printStackTrace();
 			System.exit(-2);
 		}
 		// Construct Parser
 		this.parser = new LR1Parser(lr1);
 	}
 
	public ParseTree execute(File source) throws Exception {
 		logger.info("Processing: " + source.getName());
 
 		/* Scanning */
 		// Construct a Scanner which use the DFA
 		List<Token> tokens = null;
 
 		// Scan the source codes into tokens
 		tokens = this.scanner.fileToTokens(source);
 
 		// Preprocess the tokens
 		tokens = preprocessor.processTokens(tokens);
 
 		/* Parsing */
 		ParseTree parseTree = null;
 		parseTree = this.parser.parseTokens(tokens);
 
 		return parseTree;
 	}
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		Main instance = new Main();
 
 		try {
			ParseTree parseTree = instance.execute(new File("resources/testcases/a1/Je_1_AbstractClass_Final.java"));
 			ASTTreeGenerator astTreeGenerator = new ASTTreeGenerator();
 			if(parseTree!=null){
				astTreeGenerator.GenerateASTTree(parseTree);	
 			}
			System.out.println(parseTree.toString());
 		} catch (Exception e) {
 			System.err.println("ERROR: " + e.getLocalizedMessage() + " " + e.getClass().getName());
 			e.printStackTrace();
 			System.exit(42);
 		}
 	}
 }
