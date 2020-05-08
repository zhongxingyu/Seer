 
 public class Postfix {
 	
 //	private Stack stack;
 	//der String muss leerzeichen zwischen den einzelnen term bzw. operatoren enthalten
 	
 	public LinkedListItem<Integer> evaluate (String pfx) throws UnderflowException{		
		if (!pfx.equals("")){
 			Stack <Integer> ablage = new Stack <Integer>();
 			String[] stringarray = pfx.split(" ");
 			String operator;
 			LinkedListItem<Integer> lhs;
 			LinkedListItem<Integer> rhs;
 			for(int i= 0; i<= stringarray.length;i++){
				if (stringarray[i].matches("(\\d)")){
 					ablage.push(Integer.parseInt(stringarray[i]));
 				}else{
 					operator = stringarray[i];
 					if (!ablage.empty()){
 					lhs = ablage.pop();
 					rhs = ablage.pop();
 					String result = lhs + operator +  rhs;
 					ablage.push(Integer.parseInt(result));	
 					}
 				}
 			}
 			return ablage.pop();
 		}
 		throw new UnderflowException("Stack Underflow");	
 		}
 }
 
