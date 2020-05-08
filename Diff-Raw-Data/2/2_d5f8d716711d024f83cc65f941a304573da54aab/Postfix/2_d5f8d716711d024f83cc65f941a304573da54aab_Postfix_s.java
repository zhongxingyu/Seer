 public class Postfix {
 
 	// private Stack stack;
 	// der String muss leerzeichen zwischen den einzelnen term bzw. operatoren
 	// enthalten
 
 	public String evaluate(String pfx) throws UnderflowException, IllegalArgumentException {
 		int rest = 0;
 		String finalResult = null;
 		if (!pfx.equals("")) {
 			Stack<Integer> ablage = new Stack<Integer>();
 			String[] stringArray = pfx.split(" ");
 			String operator;
 			LinkedListItem<Integer> lhs;
 			LinkedListItem<Integer> rhs;
 			for (int i = 0; i < stringArray.length; i++) {
 				if (stringArray[i].matches("(\\d{1,2})")) {
 					ablage.push(Integer.parseInt(stringArray[i]));
 				} else {
 					if (stringArray[i].length() > 1){
 						throw new IllegalArgumentException();
 					}
 					operator = stringArray[i];
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
 
 			finalResult = ablage.pop().value.toString();			
 			if (rest > 0) {
 				finalResult = finalResult + "  + Rest von: " + rest;
 			}
 		}
 		return finalResult;
 	}
 
 	public String infixToPostfix(String ifx) throws UnderflowException, FormatException, IllegalArgumentException {
 		Stack<String> operatorAblage = new Stack<String>();
 		String[] stringArray = ifx.split(" ");
 		String numberString = "";
 		LinkedListItem<String> operator;
 		int priority;
 
 		for (int i = 0; i < stringArray.length; i++) {
			if (stringArray[i].matches("(\\d)")) {
 				numberString += stringArray[i] + " ";
 			} else {
 				if (stringArray[i].length() > 1){
 					throw new IllegalArgumentException();
 				}
 				priority = findPriority(stringArray[i]);
 				operatorAblage.push(stringArray[i]);
 				operator = operatorAblage.top();
 				if (!operatorAblage.empty()) {
 					if (priority == 2) {
 						if (operator.pointer != null) {
 							if (priority == 1) {
 								if (operator.pointer.pointer.pointer != null) {
 									numberString += operator.pointer.value + " ";
 									numberString += operator.pointer.pointer.value + " ";
 									numberString += operator.pointer.pointer.pointer.value	+ " ";
 									operatorAblage.pop();
 									operatorAblage.pop();
 									operatorAblage.pop();
 								} else if (operator.pointer.pointer != null) {
 									numberString += operator.pointer.value	+ " ";
 									numberString += operator.pointer.pointer.value	+ " ";
 									operatorAblage.pop();
 									operatorAblage.pop();
 								} else {
 									numberString += operator.pointer.value	+ " ";
 								}
 							} else if (operator.value
 									.equals(operator.pointer.value)) {
 								numberString += operatorAblage.pop().value	+ " ";
 							} else {
 								numberString += operator.pointer.value + " ";
 								operatorAblage.pop();
 								operatorAblage.pop();
 								operatorAblage.push(operator.value);
 							}
 						} else {
 							continue;
 						}
 					} else if (priority == 1) {
 						if (operator.pointer != null) {
 							if (priority == 2) {
 								continue;
 							} else if (operator.value
 									.equals(operator.pointer.value)) {
 								numberString += operatorAblage.pop().value	+ " ";
 							} else {
 								continue;
 							}
 						} else {
 							continue;
 						}
 					} else {
 						throw new FormatException("falsches Format");
 						//System.out.println("falsches Format");
 						//break;
 					}
 				}
 			}
 		}
 
 		while (!operatorAblage.empty()) {
 			numberString += operatorAblage.pop().value + " ";
 		}
 
 		//System.out.println("Postfix: " + numberString);
 		return numberString;
 	}
 	
 	public int findPriority(String operator){
 		if (operator.equals("*") || operator.equals("/") ) {
 			return 1;
 		}else if (operator.equals("+") || operator.equals("-")){
 			return 2;
 		}else{
 		return 0;
 		}
 	}
 }
