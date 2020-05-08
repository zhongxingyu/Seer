 public class Postfix {
 	String finalResult;
	int rest;
 	// private Stack stack;
 	// der String muss leerzeichen zwischen den einzelnen term bzw. operatoren
 	// enthalten
 
 	public void evaluate(String pfx)
 			throws UnderflowException {
 		if (!pfx.equals("")) {
 			Stack<Integer> ablage = new Stack<Integer>();
 			String[] stringarray = pfx.split(" ");
 			String operator;
 			LinkedListItem<Integer> lhs;
 			LinkedListItem<Integer> rhs;
 			for (int i = 0; i < stringarray.length; i++) {
				if (stringarray[i].matches("(\\d{1,2})")) {
 					ablage.push(Integer.parseInt(stringarray[i]));
 				} else {
 					operator = stringarray[i];
 					if (!ablage.empty()) {
 						rhs = ablage.pop();
 						lhs = ablage.pop();
 						if (operator.equals("+")) {
 							int result = lhs.value + rhs.value;
							rest = 0;
 							ablage.push(result);
 						} else if (operator.equals("-")) {
 							int result = lhs.value - rhs.value;
							rest = 0;
 							ablage.push(result);
 						} else if (operator.equals("*")) {
 							int result = lhs.value * rhs.value;
							rest = 0;
 							ablage.push(result);
 						} else if (operator.equals("/")) {
 							int result = lhs.value / rhs.value;
							 rest = lhs.value % rhs.value;
 							ablage.push(result);
 						}
 					}
 				}
 			}
			
 			finalResult =  ablage.pop().value.toString();
 			
			
 		}
 	}
 
 	public String getFinalResult() {
		if (rest > 0){
		return finalResult + "  + Rest von: " + rest;
		}else{
			return finalResult;
		}
		
 	}
 }
