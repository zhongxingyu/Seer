 package malice_grammar;
 
 import org.antlr.runtime.ANTLRStringStream;
 import org.antlr.runtime.CharStream;
 import org.antlr.runtime.CommonTokenStream;
 import org.antlr.runtime.RecognitionException;
 import org.antlr.runtime.TokenStream;
 
 public class MaliceParser {
 	public static void main(String[] args) throws RecognitionException {
 		String[] programs = 
 			{
 				"x was a number of 3. " + '\n' +
 				"The looking-glass hatta() " + '\n' +
 				"opened " + '\n' +
 					"The room private() contained a letter " + '\n' +
 					"opened " + '\n' +
 						"Alice found 1. " + '\n' +
 					"closed" + '\n' +
 					"x became 'y'. " + '\n' +
 				"closed " + '\n' +
 				"The room abs(number b) contained a letter " + '\n' +
 				"opened " + '\n' +
 					"b spoke. " + '\n' +
 				"closed " + '\n'
 			,
 				"x was a letter of 'c'. \n" +
 				"The looking-glass hatta() " + '\n' +
 				"opened " + '\n' +
 					"The room private() contained a letter " + '\n' +
 					"opened " + '\n' +
 						"Alice found abs(x) and " + '\n' +
 						"abs(y) spoke. " + '\n' + 
 					"closed" + '\n' +
 					"x became 'y'. " + '\n' +
 				"closed " + '\n' +
 				"The room abs(number b) contained a letter " + '\n' +
 				"opened " + '\n' +
 					"b spoke. " + '\n' +
 				"closed " + '\n'
 			,
 			"x was a letter of 'c'. \n" +
 			"The looking-glass hatta() " + '\n' +
 			"opened " + '\n' +
 				"perhaps (x<2) so \n" +
 					"c spoke. \n" +
 				"maybe (x<1) so \n" +
 					"y spoke. \n" +
 				"or \n" +
 					"c spoke. \n"+
 				"because Alice was unsure which. \n" +
 				"eventually(i==0) because \n" +
 					"opened \n" +
 						"c said Alice. \n" +
 					"closed \n" +
 				"enough times. \n" +
 			"closed " + '\n' +
 			"The room abs(number b) contained a letter " + '\n' +
 			"opened " + '\n' +
 				"b spoke. " + '\n' +
 			"closed " + '\n'
 		};
 		//System.out.println(programs[2].toString());
 		int i = 0 ;
 		for (String p: programs) {
 			i++;
 			CharStream input = new ANTLRStringStream(p); 
 			malice_grammarLexer lexer = new malice_grammarLexer(input );
 			TokenStream tokens = new CommonTokenStream(lexer);
 			malice_grammarParser parser = new malice_grammarParser(tokens ) ;
			malice_grammarParser.program_return tree = parser.program() ;
			System.out.println(tree.toString());
 			System.out.println("done program " + i + "...");
 		}
 		
 	}
 }
