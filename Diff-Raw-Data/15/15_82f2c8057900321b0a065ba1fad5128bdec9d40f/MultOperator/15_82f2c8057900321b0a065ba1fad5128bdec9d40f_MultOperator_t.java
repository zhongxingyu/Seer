 package assembler;
 
 import java.util.ArrayList;
 
 public class MultOperator extends ConmutativeOperator {
 
 	public void generate(ArrayList<String> operands){ 
 		String second = operands.get(1);
 		String first = operands.get(0); 
 		//first must be EAX
 		CodeGenerator.assembler.add("imul "+first+" , "+second);
		//Overflow control
		CodeGenerator.assembler.add("CMP "+first+" ,65535");
		CodeGenerator.assembler.add("JG _overflowed");
		
 		CodeGenerator.operandStack.push(first); //must be EAX
 		//System.out.println("imul "+first+" , "+second+System.lineSeparator());
 		//System.out.println("JO _overflowed"); //TODO add _overflowed: sentence, invoke message,
 												//invoke exit process
 
 	}
 	public String getRegA(){
 		RegisterHandler regi = RegisterHandler.getInstance();
 		if(!regi.isFree(RegisterHandler.REG_A)){
 			String newReg = regi.reallocate("EAX");
 			CodeGenerator.operandStack.set(CodeGenerator.operandStack.indexOf("EAX"), newReg);
 		}
 		return regi.getRegister(RegisterHandler.REG_A);	
 	}
 	
 	public ArrayList<String> resolveMemory (Variable m1, Variable m2){
 		ArrayList<String> result = new ArrayList<String>();
 		String rega = getRegA();
 		CodeGenerator.assembler.add("MOV "+rega+" , "+m1.getName());// MOV RA, M1
 		result.add(rega);
 		result.add(m2.getName());
 		return result;
 	}
 
 	@Override
 	public ArrayList<String> resolveMemory(Variable m1, Register r2) {
 		// TODO Auto-generated method stub
 		RegisterHandler regi = RegisterHandler.getInstance();
 		ArrayList<String> result = new ArrayList<String>();
 		String rega = getRegA();
 		CodeGenerator.assembler.add("MOV "+rega+" , "+m1.getName());// MOV RA, M1
 		result.add(rega);
 		result.add(r2.getName());
 		//El regi se va =(
 		regi.freeRegister(r2.getName());
 		return result;
 	}
 	
 	@Override
 	public ArrayList<String> resolveMemory(Register r1, Variable m2){
 		ArrayList<String> result = new ArrayList<String>();
 		if (! r1.getName().equals("EAX")){
 			RegisterHandler regi = RegisterHandler.getInstance();
 			String rega = getRegA();
 			regi.freeRegister(r1.getName());
 			CodeGenerator.assembler.add("MOV "+rega+" , "+m2.getName());// MOV RA, M1
 			result.add(rega);
 			result.add(m2.getName());
 		}else{
 			result = super.resolveMemory(r1, m2);
 		}
 		
 		return result;
 		
 		
 	}
 	
 	@Override
 	public ArrayList<String> resolveMemory(Register r1, Register r2){
 		ArrayList<String> result = new ArrayList<String>();
 		if(! r1.getName().equals("EAX")){
 			RegisterHandler regi = RegisterHandler.getInstance();
 			String rega = getRegA();
 			regi.freeRegister(r1.getName());
 			CodeGenerator.assembler.add("MOV "+rega+" , "+r2.getName());// MOV RA, M1
 			result.add(rega);
 			result.add(r2.getName());
 		}else{
 			result = super.resolveMemory(r1,r2);
 		}
 		
 		return result;
 	}
 	
 	
 
 }
