 package parser;
 
 public class ParserTester {
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		// TODO Auto-generated method stub
 		
 		Parser ps;		
 
 		try {
 			ps = new Parser();
 			
			String[] inputs = {"(A | B)*"};
 			
 			for(String expression : inputs) {
 				System.out.println("Parsing began...");
 				
 				ps.parse(expression);
 				
 
 			    
 			}			
 		} catch(ParseException e) {
 			throw new RuntimeException("Parse error.", e);
 		}
 		
 	}
 
 }
