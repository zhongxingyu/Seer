 import java.io.StringReader;
 import java.util.HashMap;
 import java.util.Map;
 
 public class Test {
 
 	public static void main(String[] args) throws Exception {
 		String[] exprs = new String[] { "a = 1 + 2 + 3 + 4", "b= 1+ 2", "c=1*2+11/2", "d=(2+3)-(10*(20-3))" };
 		Object[] expecteds = new Object[] { new Object[] { "a", 3 },
 				new Object[] { "b", 3 }, new Object[] { "c", 7 }, new Object[] {"d", -165}};
 
		for (int n = 0; n < exprs.length; n++) {
		//for (int n = 3; n < 4; n++) {
 			//System.out.println("exprs length is: " + exprs.length);
 			String expr = exprs[n];
 			Object[] expected = (Object[]) expecteds[n];
 
 			System.out.println("Sentence is: " + expr);
 			
 			YourScannerImpl sc = new YourScannerImpl(new StringReader(expr));
 			Tokenizer to = new YourTokenizerImpl(sc);
 			Parser pa = new YourParserImpl (to);
 			
 			//tokenizer testing
 			Node parseTree = pa.parse();
 			
			System.out.println(parseTree.toTree() + '\n');
 			
 			//tokenizer testing
 			//for (;;) {
 			//	
 			//	System.out.println("Current Token is: " + to.current().text());
 			//	System.out.println("Peeked Token should be: " + to.peek().text());
 			//	if (to.next().type() == Token.Type.EOF) {
 			//		System.out.println("EOF token found" + '\n');
 			//		break;
 			//	}
 			//}
 			
 			
 			//char currentChar = sc.current();
 			/* Scanner testing
 			for (;;) {
 				//System.out.println("Currently i is " + i);
 				//currentChar = sc.current();
 				System.out.println("Current character is: " + sc.current());
 				System.out.println("Peeked character is: " + sc.peek() + "\n");
 				if (sc.next() == 0) {
 					System.out.println("End of string");
 					break;
 				}
 			}
 			*/
 
 			//Tokenizer to = new YourTokenizerImpl(sc);
 			//Parser pa = new YourParserImpl(to);
 			//Node root = pa.parse();
 //			System.out.println(expr);
 //			System.out.println(root.toTree());
 
 			//Visitor v = new Visitor();
 
 			//@SuppressWarnings("unchecked")
 			//Map<String, Number> observed = (Map<String, Number>) v.visit(root);
 			//System.out.println(observed);
 			//System.out.println("Evaluated: '" + expr + "' Expected: '"
 			//		+ expected[0] + "'='" + expected[1] + "' Got: '"
 			//		+ observed.get(expected[0]) + "'");
 		}
 
 	}
 }
