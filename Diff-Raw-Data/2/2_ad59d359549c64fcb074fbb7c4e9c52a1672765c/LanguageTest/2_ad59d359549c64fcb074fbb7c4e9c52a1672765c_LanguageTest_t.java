package sleepless.cctbg.language;
 
 import org.antlr.v4.runtime.ANTLRFileStream;
 import org.antlr.v4.runtime.CommonTokenStream;
 import org.antlr.v4.runtime.ParserRuleContext;
 import org.antlr.v4.runtime.Token;
 
 public class LanguageTest {
 
 	public static void main(String[] args) throws Exception {
 		TurtleBuildGrammerLexer lexer = new TurtleBuildGrammerLexer(new ANTLRFileStream(args[0]));
 		CommonTokenStream tokens = new CommonTokenStream(lexer);
 		TurtleBuildGrammerParser p = new TurtleBuildGrammerParser(tokens);
 		p.setBuildParseTree(true);
 		//p.addParseListener(new CrapListener());
 		ParserRuleContext t = p.prog();
 	}
 
 }
