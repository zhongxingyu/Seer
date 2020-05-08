 package player;
 
 import java.util.ArrayList;
 import java.util.regex.Pattern;
 
 public class Lexer {
 	private ArrayList<Token> headerTokens;
 	private ArrayList<Token>	bodyTokens;
 	private int bodyStartIndex;
 	
 	public ArrayList<Token> getHeader(){	return this.headerTokens;	}
 	public ArrayList<Token> getBody(){	return this.bodyTokens;	}
 	
 	Lexer(String input){
 	this.headerTokens = processHeader(input);
//	this.bodyTokens = processBody(input);
 	}
 	
 	
 	/*
 	 * repeats end in :| start with |:
 	 * tuplet starts with (
 	 * things are seperated by spaces
 	 * 
 	 */
 	private ArrayList<Token> processBody(String input) {
 		ArrayList<Token> tokens = new ArrayList<Token>();
 		for(int i = bodyStartIndex; i<input.length(); i++){
 			if(Pattern.matches("\\s",""+input.charAt(i))){
 				int j;
				for(int k =1;!Pattern.matches("\\s",""+input.charAt(i)); k++){
 					j =k;
 					if (Pattern.matches("[a-gA-Gz,\\^_']","+input.charAt(i+k)")){
 						tokens.add(new Token(input.charAt(i+k)+"",input.charAt(i+k)+""));
 					}
 					else if(Pattern.matches("\\|",""+input.charAt(i+k)) && !Pattern.matches(""+input.charAt(i+k+1), "[\\]:]"))
 						tokens.add(new Token("\\|","\\|"));
 					else if(Pattern.matches( ":",""+input.charAt(i+k))){
 						tokens.add(new Token(":\\|",":\\|")); i+=1;
 					}
 					else if(Pattern.matches("\\|",""+input.charAt(i+k))){
 						tokens.add(new Token("\\|:","\\|:")); i+=1;
 					}
					else if(Pattern.matches("([2-4]",""+input.charAt(i+k)+input.charAt(i+k+1))){
 						tokens.add(new Token("("+input.charAt(i+k+1), "("+input.charAt(i+k+1))); i+=1;
 					}
					else if(Pattern.matches("[\\0-9]",""+input.charAt(i+k))){
 						if(Pattern.matches("[ \\t\\n]",""+input.charAt(i+k+1))){
 							tokens.add(new Token(""+input.charAt(i+k),""+input.charAt(i+k)));
 						}
 						else if(Pattern.matches("\\[0-9][ \\t\\n]",""+input.charAt(i+k)+input.charAt(i+k+1)+input.charAt(i+k+2))){
 							tokens.add(new Token(""+input.charAt(i+k)+input.charAt(i+k+1),""+input.charAt(i+k)+input.charAt(i+k+1)));
 						}	
 						else if(Pattern.matches("[[0-9]\\[0-9]",""+input.charAt(i+k)+input.charAt(i+k+1)+input.charAt(i+k+2))){
 							tokens.add(new Token(""+input.charAt(i+k)+input.charAt(i+k+1)+input.charAt(i+k+2),""+input.charAt(i+k)+input.charAt(i+k+1)+input.charAt(i+k+2)));
 						}
 						else{
 							throw new RuntimeException("Invalid Character Sequence Encountered");
 						}
 					}
 					else if(Pattern.matches("\\[[12]",""+input.charAt(i+k)+input.charAt(i+k+1))){
 						tokens.add(new Token("["+input.charAt(i+k+1), "["+input.charAt(i+k+1)));
 					}
 					else if(Pattern.matches("|[|\\]]",""+input.charAt(i+k)+input.charAt(i+k+1))){
 						tokens.add(new Token("|"+input.charAt(i+k+1),"|"+input.charAt(i+k+1)));
 					}
 					else if(Pattern.matches("\\[[za-gA-G]",""+input.charAt(i+k)+input.charAt(i+k+1))){
 						tokens.add(new Token("[","["));
 					}
 					else if(Pattern.matches("\\][za-gA-G]",""+input.charAt(i+k)+input.charAt(i+k-1))){
 						tokens.add(new Token("]","]"));
 					}
 					else{
 						throw new RuntimeException("Unexpected Character: "+input.charAt(i+k));
 					}i+=j;
 				}
 				
 			}
 		}
 		return tokens;
 	}
 
 	public ArrayList<Token> processHeader(String input){
 		ArrayList<Token> tokens = new ArrayList<Token>();
 		for (int i=0; i< input.length(); i++){
 			if (input.charAt(i)==':'){
 				int k = 1;
 				StringBuilder sb = new StringBuilder();
				while(!Pattern.matches("[$^]",input.charAt(i+k)+"")){
 					sb.append(input.charAt(i+k));
					System.out.println(sb.toString());
 					k+=1;
 					if(i+k > input.length()){
//						System.out.println(input.charAt(i+k-1));
 						throw new RuntimeException("Illegal character sequence found");
 					}
 				}
 				tokens.add(new Token(input.charAt(i-1)+"",sb.toString()));
 				i+=k;
 				if(tokens.get(tokens.size()-1).getType() == Token.Type.KEY){
 					this.bodyStartIndex = i+k;
 					 break;
 					}			
 			}
 		}
 		return tokens;
 	}
 
 }
