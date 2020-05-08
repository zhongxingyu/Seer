 package latte.grammar;
 
 import latte.grammar.latteParser.program_return;
 
 import org.antlr.runtime.ANTLRStringStream;
 import org.antlr.runtime.CharStream;
 import org.antlr.runtime.CommonTokenStream;
 import org.antlr.runtime.RecognitionException;
 import org.antlr.runtime.TokenStream;
 import org.antlr.runtime.tree.CommonTreeNodeStream;
 
 public class Test {
 	public static void main(String[] args) throws RecognitionException {
		CharStream charStream = new ANTLRStringStream("int main() { return 1-1; }");
 		latteLexer lexer = new latteLexer(charStream);
 		TokenStream tokenStream = new CommonTokenStream(lexer);
 		latteParser parser = new latteParser(tokenStream);
 		program_return program = parser.program();
 		System.out.println(program.tree.toStringTree());
 
 		CommonTreeNodeStream nodeStream = new CommonTreeNodeStream(program.tree);
 		lattetree walker = new lattetree(nodeStream);
 		walker.program();
 		System.out.println("ok");
 	}
 }
