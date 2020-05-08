 package Immediate;
 
 public class Ins_rtaddr extends ImmediateInstruction {
 
 	public static final int[] OP_CODE = { 32, 33, 34, 35, 36, 37, 38, 40, 41, 42, 43, 46, 48, 56 };
	public static final String[] FUNCTION_NAME = { "lb", "lh", "lwl", "lw", "lbu", "lhu", "lwr", "sb", "sh", "swl", "sw", "swr", "ll", "st" };
 	
 	public Ins_rtaddr(String binaryString) {
 		super(binaryString);
 		_rt = binaryToReg(getRt());
 		_rs = binaryToReg(getRs());
 		_imm = Integer.valueOf(getImm(), 2).toString();
 	}
 
 	@Override
 	public void printMnemonic() {
 		_functionName = getNameFromCode(FUNCTION_NAME, OP_CODE, Integer.valueOf(_opCode, 2));
 		System.out.println(_functionName + " " + _rt + " " + _imm + "(" + _rs + ")");
 
 	}
 
 }
