public class ScanParse {
 
 	public static void main(String[] args) {
 		String source = "pinX is 5888.";
 		String output = "";
 		int index1 = 0;
 
 		// This while loop will go until it hits .
 		while (source.charAt(index1) != '.') {
 			output = output + source.charAt(index1);
 			index1++;
 
 			if (source.charAt(index1) == ' ') {
 				// System.out.println("WHITE SPACE IS AT :" +index1);
 				index1++;
 				System.out.println(output);
 				doParse(output);
 				output = "";
 			} else if (source.charAt(index1) == '.') {
 				System.out.println(output);
 				doParse(output);
 				break;
 			}
 
 		}
 		
 		
 		// Parse then put to tree
 		
 	}
 	public static void doParse(String token){
 		AST astTree = new AST();
 		Statement statement = new Statement();
 		
 		if (token.equals("is")){
 			// add class = to AST
 			statement.addNode(new is());
 			System.out.println("=");
 		}
 		if (isDigit(token)){
 			statement.addNode(new number());
 			sequence.addNode()
 		}
 		if (token.equals("pinX")){
 			statement.addNode(new Id);
 			System.out.println("Int pinX");
 		}
 	}
 	
 	public static boolean isDigit(String token){
 		int index = 0;
 		while(index < token.length()){
 			Character c = token.charAt(index);
 			if(!Character.isDigit(c)){
 				return false;
 			}
 			index++;
 		}
 		return true;
 	}
 }
