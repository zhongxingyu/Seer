 
 import java.util.Stack;
 
 public class Tester{
 	public static boolean balancedExpression(String exp){
 		Stack<Character> unpaired= new Stack<>();
 		for(int i=0; i<exp.length(); i++){
 			char x= exp.charAt(i);			
 			if(x=='{'||x=='('||x=='['){
 				unpaired.push(x);
 			}else if(x=='/'&&i<exp.length()-1&&exp.charAt(i+1)=='*'){	
 				unpaired.push(x);
 				i++;
 			}else{
				if(unpaired.isEmpty()){
					return false;
				}
 				switch(x){
 					case '}': 
 						if(unpaired.pop()!='{'){
 							return false;
 						}
 						break;
 					case ')':  
 						if(unpaired.pop()!='('){
 							return false;
 						}
 						break;
 					case ']':  
 						if(unpaired.pop()!='['){
 							return false;
 						}
 						break;
 					case '*':  
 						if(i<exp.length()-1&&exp.charAt(i+1)=='/'){
 							i++;
 							if(unpaired.pop()!='/'){
 								return false;
 							}
 						}
 						break;
 				}
 			}
 		}
 		return unpaired.isEmpty();
 	}
 
 	public static void main(String[] args){
 
 		String exp= "/*[][{}[]]*//*()((()()({})))*/";
 		System.out.println(exp+" balanced?: "+balancedExpression(exp));
 
 	}
 }
