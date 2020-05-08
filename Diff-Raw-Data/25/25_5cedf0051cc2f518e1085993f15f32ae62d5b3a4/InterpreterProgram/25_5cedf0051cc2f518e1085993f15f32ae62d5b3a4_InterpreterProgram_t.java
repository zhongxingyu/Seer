 package net.wolf.stephan.kl;
 
 import java.io.IOException;
 
 import net.wolf.stephan.kl.interpreter.Interpreter;
 import net.wolf.stephan.kl.interpreter.Program;
 
 import org.antlr.runtime.ANTLRFileStream;
 import org.antlr.runtime.CharStream;
 import org.antlr.runtime.CommonTokenStream;
 import org.antlr.runtime.RecognitionException;
 import org.antlr.runtime.Token;
 import org.antlr.runtime.tree.CommonTreeNodeStream;
 
 public class InterpreterProgram {
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		BuildInformation.printInterpreterInfo();
 
 		boolean debug = false;
 		boolean error = false;
 		Integer varA = null;
 		String file = null;
 		for (int i = 0; i < args.length; ++i) {
 			String arg = args[i];
 			if (arg.startsWith("-")) {
 				if (arg.equals("-v") || arg.equals("--verbose")) {
 					debug = true;
 				} else {
 					System.err.println("error: unknown flag: " + arg);
 					error = true;
 				}
 			} else if (file == null)
 				file = arg;
 			else
 				if(varA == null)
 					try {
 						varA = Integer.parseInt(arg);
 					} catch (Exception e) {
 						System.err.println("error: could not convert '"+arg+"' to an integer value for variable a");
 						error = true;
 					}
 				else
 					error = true;
 		}
 
 		if (error || file == null) {
 			System.out.println("usage: java -jar kli.jar [-v | --verbose] <program.kl> <value for variable a>");
 			return;
 		}
 
 		Integer x = -1;
 		try {
 			x = interprete(file, debug, varA);
 			System.out.println("x := " + x);
 		} catch (Exception e) {
 			System.err.println("error: " + e.getMessage());
 			e.printStackTrace();
 		}
 
 		return;
 
 	}
 
 	public static int interprete(String file, boolean debug, Integer variableA) throws IOException, RecognitionException {
 		CharStream stream = new ANTLRFileStream(file);
 		KLLexer lexer = new KLLexer(stream);
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

 		CommonTokenStream tokens = new CommonTokenStream(lexer);
 		if (debug) {
			KLParser parser = new KLParser(tokens);
 			// Print Tree
 			System.out.println("Abstract Syntax Tree:");
 			System.out.println(parser.evaluator().tree.toStringTree());
 			System.out.println();
			tokens.reset();
 		}
		KLParser parser = new KLParser(tokens);
 		CommonTreeNodeStream nodeStream = new CommonTreeNodeStream(parser.evaluator().tree);
 
 		Interpreter interpreter = new Interpreter(nodeStream);
 
 		Program p = interpreter.evaluator();
 		if(variableA!= null)
 			p.setVariable("a", variableA);
 		Integer x = p.evaluate();
 		return x;
 	}
 
 }
