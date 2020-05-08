 package controller;
 
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.util.List;
 import java.util.Map;
 import java.util.ServiceLoader;
 import java.util.Set;
 
 import org.apache.log4j.Level;
 import org.apache.log4j.Logger;
 
 import swp_compiler_ss13.common.ast.AST;
 import swp_compiler_ss13.common.backend.Backend;
 import swp_compiler_ss13.common.backend.BackendException;
 import swp_compiler_ss13.common.backend.Quadruple;
 import swp_compiler_ss13.common.ir.IntermediateCodeGenerator;
 import swp_compiler_ss13.common.ir.IntermediateCodeGeneratorException;
 import swp_compiler_ss13.common.lexer.Lexer;
 import swp_compiler_ss13.common.parser.Parser;
 import swp_compiler_ss13.common.visualization.ASTVisualization;
 import swp_compiler_ss13.common.visualization.TACVisualization;
 import swp_compiler_ss13.common.visualization.TokenStreamVisualization;
 
 public class Controller {
 	// the input file, stdin by default
 	static InputStream input = System.in;
 
 	// variables which hold our components,
 	// initially "null", will be assigned if
 	// component plugins are found.
 	static Lexer lexer = null;
 	static Parser parser = null;
 	static IntermediateCodeGenerator irgen = null;
 	static Backend backend = null;
 
 	// several veriables which are affected by command line options
 	static boolean disableVisualization = false;
 	static String outname = null;
 	static String loglevel = null;
 	static boolean jit = false;
 
 	// our error report log for the parser
 	static ReportLogImpl errlog = new ReportLogImpl();
 
 	// visualization service loaders
 	private static ServiceLoader<TokenStreamVisualization> tokenVisuService;
 	private static ServiceLoader<ASTVisualization> ASTVisuService;
 	private static ServiceLoader<TACVisualization> TACVisuService;
 
 	// load component plugins
 	static void loadPlugins() {
 
 		// create loaders for component plugins
 		ServiceLoader<Lexer> lexerService = ServiceLoader.load(Lexer.class);
 		ServiceLoader<Parser> parserService = ServiceLoader.load(Parser.class);
 		ServiceLoader<IntermediateCodeGenerator> irgenService = ServiceLoader.load(IntermediateCodeGenerator.class);
 		ServiceLoader<Backend> backendService = ServiceLoader.load(Backend.class);
 
 		tokenVisuService = ServiceLoader.load(TokenStreamVisualization.class);
 		ASTVisuService = ServiceLoader.load(ASTVisualization.class);
 		TACVisuService = ServiceLoader.load(TACVisualization.class);
 
 		// lexer:
 		for (Lexer l : lexerService) {
 			System.err.print("  Plugin found (lexer): '" + l.getClass() + "'");
 			// if still null, assign and mention that we use this plugin on
 			// stderr
 			if (lexer == null) {
 				System.err.println("   <- USED");
 				lexer = l;
 			} else {
 				System.err.println();
 			}
 		}
 
 		// load component plugins
 		// parser:
 		for (Parser p : parserService) {
 			System.err.print("  Plugin found (parser): '" + p.getClass() + "'");
 			// if still null, assign and mention that we use this plugin on
 			// stderr
 			if (parser == null) {
 				System.err.println("   <- USED");
 				parser = p;
 			} else {
 				System.err.println();
 			}
 		}
 
 		// load component plugins
 		// intermediate code generator:
 		for (IntermediateCodeGenerator i : irgenService) {
 			System.err.print("  Plugin found (IRGen): '" + i.getClass() + "'");
 			// if still null, assign and mention that we use this plugin on
 			// stderr
 			if (irgen == null) {
 				System.err.println("   <- USED");
 				irgen = i;
 			} else {
 				System.err.println();
 			}
 		}
 
 		// load component plugins
 		// backend:
 		for (Backend b : backendService) {
 			System.err.print("  Plugin found (backend): '" + b.getClass() + "'");
 			// if still null, assign and mention that we use this plugin on
 			// stderr
 			if (backend == null) {
 				System.err.println("   <- USED");
 				backend = b;
 			} else {
 				System.err.println();
 			}
 		}
 
 		// check that we have a working trio of lexer, parser and backend
 		if (lexer == null) {
 			System.err.println("ERROR: no lexer plugin found!");
 		}
 		if (parser == null) {
 			System.err.println("ERROR: no parser plugin found!");
 		}
 		if (irgen == null) {
 			System.err.println("ERROR: no IRGen plugin found!");
 		}
 		if (backend == null) {
 			System.err.println("ERROR: no backend plugin found!");
 		}
 		if (lexer == null || parser == null || irgen == null || backend == null) {
 			System.exit(1);
 		}
 
 		System.err.println("  Token Stream Visualization found:");
 		for (TokenStreamVisualization tokenvisu : tokenVisuService) {
 			System.err.println("\t" + tokenvisu.getClass().getName());
 		}
 
 		System.err.println("  AST Visualization found:");
 		for (ASTVisualization astvisu : ASTVisuService) {
 			System.err.println("\t" + astvisu.getClass().getName());
 		}
 
 		System.err.println("  TAC Visualization found:");
 		for (TACVisualization tacvisu : TACVisuService) {
 			System.err.println("\t" + tacvisu.getClass().getName());
 		}
 	}
 
 	public static void main(String[] args) throws IOException, IntermediateCodeGeneratorException, BackendException,
 			InstantiationException, IllegalAccessException {
 		System.err.println("SWP Compiler v0.0\n");
 
 		boolean expect_outname = false;
 		boolean expect_loglevel = false;
 
 		// parser CMD args:
 		// if set to true, further arguments may be options,
 		// will be false after encounering -- argument
 		boolean may_be_option = true;
 		for (String arg : args) {
 
 			// basically "OPTARG" strings work via setting a boolean when the
 			// flag is read,
 			// the flag is checked for the next argument then after one
 			// iteration.
 			// once all arguments for the option are read, the flag is unset
 			// again.
 			// example: -o output.ll
 			// <- reads "-o" and sets expect_outname to true
 			// <- reads name and sets expect_outname to false_
 			if (expect_outname) {
 				outname = arg;
 				expect_outname = false;
 			} else if (expect_loglevel) {
 				loglevel = arg;
 				expect_loglevel = false;
 			}
 			// set file
 			else if (!may_be_option || arg.charAt(0) != '-') {
 				if (input != System.in) { // already set!
 					System.err.println("ERROR: only none or one input file allowed!");
 					System.exit(2);
 				} else {
 					if (arg.equals("-")) {
 						; // already set to stdin
 					} else {
 						input = new FileInputStream(arg);
 					}
 				}
 				continue; // it's no option, so we should continue with next arg
 			}
 			// -- seperator argument
 			else if (arg.equals("--")) {
 				may_be_option = false;
 			}
 			// -V disbales visualization passes
 			else if (arg.equals("-V") || arg.equals("--no-visualization")) {
 				disableVisualization = true;
 			} else if (arg.equals("-j") || arg.equals("--jit")) {
 				jit = true;
 			} else if (arg.equals("-o")) {
 				expect_outname = true;
 			} else if (arg.equals("-l") || arg.equals("--loglevel")) {
 				expect_loglevel = true;
 			}
 
 			// help option, just displays usage summary
 			if (arg.equals("-h") || arg.equals("--help") || arg.equals("-?")) {
 				System.out
 						.println("Usage: java -cp plugun1.jar:plugin2.jar:...:Controller.jar [options] [--] [input-filename]");
 				System.out.println();
 				System.out.println("  input-filename may be '-' for stdin or a relative or absolute path to a file.");
 				System.out.println();
 				System.out
 						.println("  '--' is an optional separator between options and the input-file, useful if path to the input file begins with character '-'.");
 				System.out.println();
 				System.out.println();
 				System.out.println("  arguments may be any of:");
 				System.out.println("     -h/-?/--help            emits this help message");
 				System.out.println("     -o <output>             output filename, only used by llvm");
 				System.out.println("     -j --jit                just in time compile (not yet supported)");
 				System.out.println("     -l --loglevel <level>   set the loglevel. allowed values are:");
 				System.out.println("                               ALL DEBUG ERROR FATAL INFO OFF TRACE WARN");
 				System.out
 						.println("     -V --no-visualization   disables the automatic visualization pass if visualization plugins are found");
 				System.out.println();
 				System.exit(0);
 			}
 		}
 
 		// set the logger level
 		if (loglevel != null) {
 			loglevel = loglevel.toUpperCase();
 			Level level = Level.toLevel(loglevel);
			if (level.toString() != loglevel) {
 				Logger.getRootLogger().setLevel(level);
 			} else {
 				System.out.println("unknown loglevel see -h for help");
 				System.exit(0);
 			}
 		}
 
 		// try to find a lexer, parser and backend
 		// this call assigns the variables lexer,parser,backend
 		// or fails if any one components is missing.
 		loadPlugins();
 
 		// use the components now to compile our file
 		// lexer...
 		lexer.setSourceStream(input);
 
 		if (!disableVisualization) {
 			for (TokenStreamVisualization tokenvisu : tokenVisuService) {
 				Lexer vlexer = lexer.getClass().newInstance();
 				vlexer.setSourceStream(input);
 				tokenvisu.visualizeTokenStream(vlexer);
 			}
 		}
 
 		// parser...
 		parser.setLexer(lexer);
 		parser.setReportLog(errlog);
 		AST ast = parser.getParsedAST();
 
 		// in case of parser errors: abort and display errors
 		if (errlog.hasErrors()) {
 			System.err.println("ERROR: compile failed due to errors:");
 			for (Error e : errlog.getErrors()) {
 				System.err.println("(L" + e.getLine() + "," + e.getColumn() + ")" + "\t " + e.getText() + "\t "
 						+ e.getMessage());
 			}
 			System.exit(1);
 		}
 
 		if (!disableVisualization) {
 			for (ASTVisualization astvisu : ASTVisuService) {
 				astvisu.visualizeAST(ast);
 			}
 		}
 
 		// IR gen...
 		List<Quadruple> tac = irgen.generateIntermediateCode(ast);
 
 		if (!disableVisualization) {
 			for (TACVisualization tacvisu : TACVisuService) {
 				tacvisu.visualizeTAC(tac);
 			}
 		}
 
 		// backend...
 		Map<String, InputStream> targets = backend.generateTargetCode("", tac);
 
 		// output..
 		Set<String> outputFilenames = targets.keySet();
 		// for now, we always print everything on stdout.
 		for (String outFile : outputFilenames) {
 			OutputStream output;
 			if (outFile.equals(".ll")) {
 				if (outname != null) {
 					output = new FileOutputStream(outname);
 				} else {
 					outname = "<<stdout>>";
 					output = System.out;
 				}
 			} else // java bytecode output
 			{
 				output = new FileOutputStream(outFile);
 			}
 			System.err.println("//code: " + outname);
 			// input stream on the code
 			InputStream is = targets.get(outFile);
 
 			// copied and modified from stackoverflow.com
 			// http://stackoverflow.com/questions/1574837/connecting-an-input-stream-to-an-outputstream
 			byte[] buffer = new byte[4096];
 			int bytesRead;
 			while ((bytesRead = is.read(buffer)) != -1) {
 				output.write(buffer, 0, bytesRead);
 			}
 		}
 	}
 }
