 package ch.codedump.ooplss;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.logging.Logger;
 
 import org.antlr.runtime.ANTLRInputStream;
 import org.antlr.runtime.CommonTokenStream;
 import org.antlr.runtime.RecognitionException;
 import org.antlr.runtime.tree.CommonTreeNodeStream;
 import org.antlr.runtime.tree.Tree;
 import org.apache.commons.cli.CommandLine;
 import org.apache.commons.cli.CommandLineParser;
 import org.apache.commons.cli.HelpFormatter;
 import org.apache.commons.cli.Options;
 import org.apache.commons.cli.ParseException;
 import org.apache.commons.cli.PosixParser;
 
 import ch.codedump.ooplss.antlr.OoplssDef;
 import ch.codedump.ooplss.antlr.OoplssLexer;
 import ch.codedump.ooplss.antlr.OoplssParser;
 import ch.codedump.ooplss.antlr.OoplssParser.prog_return;
 import ch.codedump.ooplss.antlr.OoplssRef;
 import ch.codedump.ooplss.antlr.OoplssTypes;
 import ch.codedump.ooplss.symbolTable.SymbolTable;
 import ch.codedump.ooplss.tree.OoplssTreeAdaptor;
 
 public class Main {
 	private static final String USAGE = "[-h] [-d <directory>] [-f <file]";
 	private static final String HEADER = "OOPLSS - Object-Oriented Programming Language with Subtyping and Subclassing";
 	private static final String FOOTER = "For more instructions, see our website at: http://ooplss.codedump.ch";
 	private String outputDir = null;
 	private InputStream inputStream = null;
 
 	static Logger logger = Logger.getLogger(Main.class.getName());
 
 	public static void main(String[] args) throws RecognitionException,
 			IOException {
 		Main m = new Main();
 		m.run(args);
 	}
 
 	/**
 	 * Runs ooplss compiler
 	 * 
 	 * @param args
 	 *            JVM Arguments
 	 * @throws RecognitionException
 	 * @throws IOException
 	 */
 	private void run(String[] args) throws RecognitionException, IOException {
 		Options opts = defineOptions();
 		if (!processArguments(args, opts)) {
 			printUsage(opts);
 			return;
 		}
 
 		compile(inputStream);
 		// Do compile
 	}
 
 	/**
 	 * Parses, checks and translates ooplss code
 	 * 
 	 * @param in
 	 *            Code input stream
 	 * @throws IOException
 	 * @throws RecognitionException
 	 */
 	private void compile(InputStream in) throws IOException,
 			RecognitionException {
 		assert (in != null);
 
 		ANTLRInputStream input = new ANTLRInputStream(in);
 
 		OoplssLexer lexer = new OoplssLexer(input);
 
 		CommonTokenStream tokens = new CommonTokenStream(lexer);
 		OoplssParser parser = new OoplssParser(tokens);
 		parser.setTreeAdaptor(new OoplssTreeAdaptor());
 		prog_return result = parser.prog();
 		Tree t = (Tree) result.getTree();
 
 		SymbolTable symTab = new SymbolTable();
 		CommonTreeNodeStream nodes = new CommonTreeNodeStream(t);
 
 		logger.fine("Definition run");
 		OoplssDef def = new OoplssDef(nodes, symTab);
 		def.downup(t);
 
 		logger.fine("Referencing run");
 		OoplssRef ref = new OoplssRef(nodes, symTab);
 		ref.downup(t);
 
 		logger.finer("Symbol Table: \n" + symTab.toString());
 
 		logger.fine("Type checking");
 		OoplssTypes types = new OoplssTypes(nodes, symTab);
 		types.downup(t);
 	}
 
 	/**
 	 * Process all Command Line Options
 	 * 
 	 * @param args
 	 *            Command Line Arguments
 	 * @param options
 	 *            Allowed option definition
 	 * @return True, if options were correct
 	 * @throws ParseException
 	 */
 	private boolean processArguments(String[] args, Options options) {
 		assert (options != null);
 		assert (args != null);
 
 		try {
 			// Parse CLI Arguments
 			CommandLineParser cliParser = new PosixParser();
 			CommandLine cli = cliParser.parse(options, args);
 
 			if (cli.hasOption('h')) {
 				printUsage(options);
 				System.exit(0);
 			}
 
 			// Read option values
 			this.outputDir = cli.getOptionValue('o', null);
 			String inputFile = cli.getOptionValue('f', null);
 
 			// Sets input stream
 			if (inputFile != null) {
 				this.inputStream = new FileInputStream(inputFile);
 			} else {
 				this.inputStream = System.in;
 			}
 
 			// Create output directory structure
 			if (this.outputDir != null) {
 				(new File(this.outputDir)).mkdirs();
 			}
 		} catch (SecurityException e) {
 			logger.info("Output directory could not be created. Permission denied: "
 					+ e.getMessage());
 			return false;
 		} catch (IOException e) {
 			logger.info(e.getMessage());
 			return false;
 		} catch (ParseException e) {
 			logger.info("Options could not get parsed: " + e.getMessage());
 			return false;
 		}
 
 		// Successful
 		return true;
 	}
 
 	private void printUsage(Options options) {
 		HelpFormatter formatter = new HelpFormatter();
 		formatter.setWidth(80);
 		formatter.printHelp(Main.USAGE, Main.HEADER, options, Main.FOOTER);
 	}
 
 	/**
 	 * Defines all CLI options
 	 * 
 	 * @return Option set
 	 */
 	private Options defineOptions() {
 		Options options = new Options();
 		options.addOption("o", "output", true, "Output directory");
 		options.addOption("f", "file", true, "Input file");
 		options.addOption("h", "help", false, "Print usage message");
 
 		return options;
 	}
 }
