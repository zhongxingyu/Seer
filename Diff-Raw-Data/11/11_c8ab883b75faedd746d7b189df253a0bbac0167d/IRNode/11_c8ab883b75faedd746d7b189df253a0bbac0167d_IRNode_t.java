 //declare IRNode class 
 public class IRNode {
 
 	//declare IRNode properties
 	private String opcode, first_operand, second_operand, result;
 	
 	//***********************
 	//	methods
 	//
 	//***********************
 
 	//the constructor	
	public IRNode(String opcode, String first_operand, String second_operand, String result) {
 
 		this.opcode = opcode;
 		this.first_operand = first_operand;
 		this.second_operand = second_operand;
 		this.result = result;
 	}
 	
 	//checks the opcode
	public String checkOpcode() {
 
		return this.opcode;
 	}
 
 	//checks the first operand
 	public String checkFirstOp() {
 
 		return this.first_operand;
 	}
 
 	//checks the second operand
 	public String checkSecondOp() {
 
 		return this.second_operand;
 	}
 
 	//checks the result
 	public String checkResult() {
 
 		return this.result;
 	}
 
 	//prints the IRNode code
 	public void printIRCode() {
 
		if(true) {
 			System.out.println("opcode " + this.opcode + " result " + this.result);
 		}
 		else {
 			System.out.println("opcode " + this.opcode + " result " + this.result);
 		}
 	}
 }
