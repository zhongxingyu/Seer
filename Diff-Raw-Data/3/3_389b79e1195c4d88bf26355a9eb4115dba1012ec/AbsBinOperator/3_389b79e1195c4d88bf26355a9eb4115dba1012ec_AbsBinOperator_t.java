 package assembler;
 
 import java.util.ArrayList;
 import java.util.Stack;
 import java.util.regex.Pattern;
 
 /**
  * AbsOperator will represent the family of the operator classes
  * @author Inti
  *
  */
 public abstract class AbsBinOperator extends AbsOperator {
 	
 	public void operate(Stack<String> operandStack) {
 		String op2 = operandStack.pop();
 		String op1 = operandStack.pop();
 		ArrayList<String> resolvedOperands = new ArrayList<String>();
 		if(isRegister(op1) && isRegister(op2)) {
 			Register r1 = new Register(op1);
 			Register r2 = new Register(op2);
 			resolvedOperands = this.resolveMemory(r1, r2);
 		} else if(isRegister(op1) && !isRegister(op2)) {
 			Register r1 = new Register(op1);
 			Variable v2 = new Variable(op2);
 			resolvedOperands = this.resolveMemory(r1, v2);
 		} else if(!isRegister(op1) && isRegister(op2)){
 			Variable v1 = new Variable(op1);
 			Register r2 = new Register(op2);
 			resolvedOperands = this.resolveMemory(v1, r2);
 		} else {
 			Variable v1 = new Variable(op1);
 			Variable v2 = new Variable(op2);
 			resolvedOperands = this.resolveMemory(v1, v2);
 		}
 		this.generate(resolvedOperands);
 	}
 	public  ArrayList<String> resolveMemory(Register r1, Register r2) {
 		ArrayList<String> result = new ArrayList<String>();
 		result.add(r1.getName());
 		result.add(r2.getName());
 		RegisterHandler.getInstance().freeRegister(r2.getName());
 		
 		return result;
 	}
 	public ArrayList<String> resolveMemory(Register r1, Variable m2) {
 		ArrayList<String> result = new ArrayList<String>();
 		result.add(r1.getName());
 		result.add(m2.getName());
 		
 		return result;
 	}
 	public abstract ArrayList<String> resolveMemory(Variable m1, Register r2);
 	/**
 	 * 
 	 * @param m1
 	 * @param m2
 	 * @return
 	 */
 	public  ArrayList<String> resolveMemory(Variable m1, Variable m2) {
 		RegisterHandler registerHanlder = RegisterHandler.getInstance();
 		ArrayList<String> result = new ArrayList<String>();
 		String reg1 = registerHanlder.getRegister();
 		CodeGenerator.assembler.add("MOV "+reg1+" , "+m2.getName());
 		result.add(m1.getName());
		result.add(reg1);
 		return result;
 
 	}
 	
 	public abstract void generate(ArrayList<String> operands);
 	
 	public boolean isRegister(String m){ //EAX EBX ECX EDX
 		return Pattern.matches("EAX|EBX|ECX|EDX",m);
 	}
 	public String getRegA() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 
 }
