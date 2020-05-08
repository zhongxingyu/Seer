 package se.raek.ahsa;
 
 import java.util.List;
 import se.raek.ahsa.ast.Statement;
 import se.raek.ahsa.parser.AhsaLexer;
 import se.raek.ahsa.parser.AhsaParser;
 import se.raek.ahsa.parser.Environment;
 import se.raek.ahsa.runtime.BuiltInFunctions;
 import se.raek.ahsa.runtime.Function;
 import se.raek.ahsa.runtime.Store;
 import se.raek.ahsa.runtime.Value;
 import org.antlr.runtime.*;
 
 public class Main {
 	
 	private static void addValue(Environment env, Store sto, String id, Value v) {
 		sto.defineValue(env.installValue(id), v); 
 	}
 	
 	private static void addFunction(Environment env, Store sto, String id, Function fn) {
 		addValue(env, sto, id, Value.makeFunction(fn));
 	}
 
 	public static void main(String[] args) throws Exception {
 		if (args.length != 1) {
 			System.out.println("Syntax: ahsa <filename>");
 		}
 		
 		Environment env = new Environment();
 		Store sto = new Store();
 		addFunction(env, sto, "print", BuiltInFunctions.print);
 		
 		CharStream input = new ANTLRFileStream(args[0]);
 		AhsaLexer lexer = new AhsaLexer(input);
 		CommonTokenStream tokens = new CommonTokenStream(lexer);
 		AhsaParser parser = new AhsaParser(tokens);
 		parser.resetEnvironment(env);
 		List<Statement> stmts = parser.statements();
 		Interpreter.executeTopLevel(stmts, sto);
 	}
 
 }
