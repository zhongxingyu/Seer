 import java.io.IOException;
 
 import org.antlr.runtime.ANTLRFileStream;
 import org.antlr.runtime.CharStream;
 import org.antlr.runtime.CommonTokenStream;
 import org.antlr.runtime.RecognitionException;
 import org.antlr.runtime.tree.CommonTree;
 
 
 public class Main {
	private OberonParser currentParser = null;
	
	
	public String getErrorMessage(RecognitionException e) {
//		String foo = currentParser.getErrorMessage(e, currentParser.getTokenNames());
		return "line: " + e.line + " : " + 
			currentParser.getErrorMessage(e, currentParser.getTokenNames()); 
	}
 
	public int parse(String filePath) throws IOException, RecognitionException{
 		CharStream input = null;
 		try {
 			input = new ANTLRFileStream(filePath);
 		} catch (IOException e) {
 			e.printStackTrace();
 			throw e;
 		}
 
 		OberonLexer lex = new OberonLexer(input);
 		CommonTokenStream tokens = new CommonTokenStream(lex);
 		OberonParser parser = new OberonParser(tokens);
		currentParser = parser;
		parser.obmodule();
 		
 		return parser.getErrorCount();
 	}
 	public static void main(String[] args) throws RecognitionException, IOException{
 		Main m = new Main();
 		try {
 			m.parse(args[0]);
 		} catch (IndexOutOfBoundsException e) {
 			System.out.println("Give arguments");
 		}
 		
 		
 //		BaseParser.program_return preturn = parser.program();
 //		CommonTree tree = (CommonTree)preturn.getTree();
 //		System.out.println(tree.toStringTree());
 	}
 
 }
