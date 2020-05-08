 
 public class CalcStack {
 	private StringStack myCharStack;
 	private StringStack myNumStack;
 
 	public CalcStack(){
 		myCharStack = new StringStack();
 		myNumStack = new StringStack();
 	}
 	
 	String special = "+-*/";
 	public void compute(String data){
 		if(special.contains(data)){
 			int priority = special.indexOf(data);
 			computeOp(data, priority);
 		}
 		else{
 			myNumStack.push(data);
 		}
 	}
 	
 	public void computeOp(String data, int priority){
 		if(myCharStack.isEmpty()){
 			myCharStack.push(data);
 			return;
 		}
 		else{
 			String last = myCharStack.pop();
 			int priorityLast = special.indexOf(last);
 			if (priority >= priorityLast){
				char op = data.charAt(0);
				performOp(op);
 				myCharStack.push(last);
 			}
 			else{
				char op = last.charAt(0);
				performOp(op);
 				myCharStack.push(data);
 			}
 		}
 	}
 	
 	public void performOp(char op){
 		int num2 = Integer.parseInt(myNumStack.pop());
 		int num1 = Integer.parseInt(myNumStack.pop());
 		int answer = 0; 
 		if(op=='+'){answer = num1 + num2;}
 		else if(op=='-'){answer = num1 - num2;}
 		else if(op=='*'){answer = num1 * num2;}
 		else if(op=='/'){answer = num1 / num2;}
 		myNumStack.push(Integer.toString(answer));
 	}
 	
 	public void finishCompute(){
 		while(!myCharStack.isEmpty()){
 			performOp(myCharStack.pop().charAt(0));
 		}
 	}
 		
 	public static void main(String[] args){
 		CalcStack stack = new CalcStack();
 		String equation = "5+7*3-2";
 
 		for(int i=0; i < equation.length(); i++){
 			System.out.println(i);
 			stack.compute(equation.substring(i,i+1));
 		}
 		stack.finishCompute();
 		System.out.println(stack.myNumStack.pop());
 	}
 
 }
