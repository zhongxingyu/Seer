 import java.io.IOException;
 
 import org.antlr.v4.runtime.*;
 import org.antlr.v4.runtime.tree.ParseTree;
 
 public class Cubex {
 	public static void main (String args[]) throws IOException {
 		getLexer(args[0]);
 	}
 	
 	protected static void getLexer(String fn) throws IOException {
 		CubexLexer2 lexer = new CubexLexer2(new ANTLRFileStream(fn));
 		CommonTokenStream tokens = new CommonTokenStream(lexer);
 		CubexParser2 parser = new CubexParser2(tokens);
 		parser.setBuildParseTree(true);
 		RuleContext tree = parser.program();
 //		System.out.println(parser.program().p.toString());
 		
		/////RuleContext tree = parser.fundef();
 		tree.inspect(parser); // show in gui
 		//tree.save(parser, "/tmp/R.ps"); // Generate postscript
 
 		System.out.println(tree.toStringTree(parser));
 	}
 }
