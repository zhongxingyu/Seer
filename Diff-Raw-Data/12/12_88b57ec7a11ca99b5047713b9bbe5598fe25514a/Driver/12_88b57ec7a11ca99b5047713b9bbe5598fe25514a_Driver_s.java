 import java.util.ArrayList;
 
 /**
  * 
  * @author nick
  *
  *	Driver
 		call walker on input with DFAtable
 		store tokens from walker
  */
 public class Driver {
 	
 	public static String errCode = "error ";
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		
		Lexical lexSpec = PScanner.scanLexicon("sample_spec.txt");
 //		PScanner readInput = new PScanner("test_input.txt");
 //		String nextToken;
 //		ArrayList<String> tokenList = new ArrayList<String>();
 		
 		NFAGenerator gen = new NFAGenerator(lexSpec);
 //		String regex = "\\**";
 //		NFAGenerator gen = new NFAGenerator(regex);
 		StateTable table = gen.genNFA();
 		
 		System.out.println("");
 		System.out.println("");
 		System.out.println("************************");
 		System.out.println("**    Printing NFA    **");
 		System.out.println("************************");
 		table.printTable();
 		
 		
 		StateTable dfa = NFAToDFA.convert(table);
 		
 		System.out.println("");
 		System.out.println("");
 		System.out.println("************************");
 		System.out.println("**    Printing DFA    **");
 		System.out.println("************************");
 		dfa.printTable();
 		
 //		//Consult the StateTable with the input of the user file
 //		while(!readInput.endOfFile()){
 //			nextToken = dfa.DFAlookUp(readInput.getToken());
 //			String tokenReturned = nextToken.substring(0,nextToken.length()-1);
 //			String extraChar = nextToken.substring(nextToken.length()-1);
 //			readInput.pushToken(extraChar);
 //			
 //			if (nextToken.startsWith(errCode)){//Err on Token
 //				System.out.println(nextToken);
 //				System.out.println("Accepted Tokens");
 //				for(String s : tokenList){
 //					System.out.println(s);
 //				}
 //				break;
 //			}
 //			else{
 //				tokenList.add(tokenReturned);
 //			}
 //		}
 //		
 //		System.out.println("Accepted Tokens");
 //		for(String s : tokenList){
 //			System.out.println(s);
 //		}
 	
 	}
 
 }
