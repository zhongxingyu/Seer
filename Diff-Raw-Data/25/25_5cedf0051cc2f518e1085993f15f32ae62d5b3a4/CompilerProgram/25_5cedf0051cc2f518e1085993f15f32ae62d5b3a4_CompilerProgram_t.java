 package net.wolf.stephan.kl;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStreamReader;
 
 import net.wolf.stephan.kl.compiler.Compiler;
 import net.wolf.stephan.kl.compiler.LineWriter;
 import net.wolf.stephan.kl.compiler.Program;
 
 import org.antlr.runtime.ANTLRFileStream;
 import org.antlr.runtime.CharStream;
 import org.antlr.runtime.CommonTokenStream;
 import org.antlr.runtime.Token;
 import org.antlr.runtime.tree.CommonTreeNodeStream;
 
 public class CompilerProgram {
 
 	
 	public static int runCommand(String cmd) {
 		try {
 			Process p = Runtime.getRuntime().exec(cmd);
 
 			int ret = p.waitFor();
 
 			BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
 
 			BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));
 
 			String s;
 			// read the output from the command
 			while ((s = stdInput.readLine()) != null) {
 				System.out.println(s);
 			}
 
 			// read any errors from the attempted command
 			while ((s = stdError.readLine()) != null) {
 				System.out.println(s);
 			}
 
 			return ret;
 		} catch (IOException e) {
 			System.err.println("error: could not execute '" + cmd + "'");
 			e.printStackTrace();
 			System.exit(-1);
 		} catch (InterruptedException e) {
 			System.err.println("error: the execution of '" + cmd + "' was interrupted!");
 			System.exit(-1);
 		}
 		return -1;
 	}
 
 	public static void main(String[] args) {
 
 		BuildInformation.printCompilerInfo();
 
 		boolean debug = false;
 		boolean error = false;
 		boolean optimize = false;
 		String outputFilename = null;
 		String file = null;
 		for (int i = 0; i< args.length; ++i) {
 			String arg = args[i];
 			if (arg.startsWith("-")) {
 				if (arg.equals("-v") || arg.equals("--verbose")) {
 					debug = true;
 				}else if (arg.equals("-o") || arg.equals("--output")) {
 					++i;
 					if(i>= args.length){
 						error = true;
 						break;
 					}
 					outputFilename= args[i];
 				}else if (arg.equals("-O") || arg.equals("--optimize")) {
 					optimize = true;
 				}
 				else {
 					System.err.println("error: unknown flag: " + arg);
 					error = true;
 				}
 			} else if (file == null)
 				file = arg;
 			else
 				error = true;
 		}
 
 		if (error|| file==null) {
 			System.out.println("usage: java -jar klc.jar [-v | --verbose] [(-o | --output) file] <program.kl>");
 			return;
 		}
 
 		try {
 			compile(file, outputFilename, debug, optimize);
 		} catch (Exception e) {
 			System.err.println("error: "+e.getMessage());
 		}
 
 		// Compiler compiler = createCompiler("x:= 1");
 		//
 		// "x:= (1+3-2)*4/2"
 
 		// assertEquals(1, );
 
 	}
 
 	public static void compile(String fileName, String outputFilename, boolean debug, boolean optimize) throws Exception {
 		if(outputFilename == null){
 			Integer start = Math.max(fileName.lastIndexOf('/')+1, 0);
 			start = Math.max(start, fileName.lastIndexOf('\\')+1);
 			outputFilename = fileName.substring(start, fileName.lastIndexOf('.'));
 		}
 
 		CharStream stream = new ANTLRFileStream(fileName);
 		// Execute lexer
 		KLLexer lexer = new KLLexer(stream);
 		CommonTokenStream tokens = new CommonTokenStream(lexer);
 		if (debug) {
 			// Print TokenStream
 			System.out.println("Token Stream:");
 			Token t = null;
 			do {
 				t = lexer.nextToken();
 				if(t.getChannel()!= 99)
 					System.out.print("'"+t.getText()+"', ");
 			} while (!t.getText().equals("<EOF>"));
 			System.out.println();
 			System.out.println();
 			lexer.reset();
 		}
		lexer = new KLLexer(stream);
 		
 		
 		// Execute parser
 		if(debug){
			KLParser parser = new KLParser(tokens);
 			// Print Tree
 			System.out.println("Abstract Syntax Tree");
 			System.out.println(parser.evaluator().tree.toStringTree());
 			System.out.println();
 			System.out.println();
			tokens.reset();
 		}
		KLParser parser = new KLParser(tokens);
 		CommonTreeNodeStream nodeStream = new CommonTreeNodeStream(parser.evaluator().tree);
 		
 		
 		// Instantiate Compiler class
 		Compiler compiler = new Compiler(nodeStream);
 
 		// Generating LLVM code
 		LineWriter file = createFile(outputFilename + ".ll");
 		Program p = compiler.evaluator();
 		p.evaluate(file);
 		file.flush();
 		file.close();
 
 		String intermediateFilename = outputFilename;
 		
 		if(optimize){
 			intermediateFilename += "_opt";
 			runCommand("opt -S -std-compile-opts "+outputFilename +".ll -o " + intermediateFilename + ".ll");
 		}
 		
 		// Compiling llvm code to assembler code
 		runCommand("llc -march=x86-64 -disable-cfi " + intermediateFilename + ".ll");
 		
 		// Assemble and link
 		runCommand("g++ -o " + outputFilename + " " + intermediateFilename + ".s");
 	}
 
 	private static LineWriter createFile(String outputFile) throws IOException {
 		// Create file
 		FileWriter fstream = new FileWriter(outputFile);
 		return new LineWriter(new BufferedWriter(fstream));
 
 	}
 
 }
