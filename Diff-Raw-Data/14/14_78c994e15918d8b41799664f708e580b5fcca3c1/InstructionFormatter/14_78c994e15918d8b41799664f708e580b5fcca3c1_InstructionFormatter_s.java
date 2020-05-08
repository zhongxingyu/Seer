 package assemblernator;
 
 /**
  * 
  * @author Noah
  * @date Apr 27, 2012; 6:15:53 PM
  */
 public class InstructionFormatter {
 	/**
 	 * @author Ratul Khosla
 	 * @date Apr 27, 2012; 6:20:32 PM
 	 * @modified Apr 27, 2012; 7:34:47 PM generalized implementation. -Noah
 	 * @tested UNTESTED
 	 * @errors NO ERRORS REPORTED
 	 * @codingStandards Awaiting signature
 	 * @testingStandards Awaiting signature
 	 * @param instr 
 	 * 			the instruction.
 	 * @return assemble
 	 * 				the assembled binary code for the instruction.
 	 * @specRef N/A
 	 */
 	public static int [] formatOther(Instruction instr) {
 		String code = IOFormat.formatBinInteger(instr.getOpcode(), 6); //011000
 		boolean fromReg = !(instr.hasOperand("DR") || instr.hasOperand("DX"));
 		boolean literal = instr.hasOperand("FL");
 		//if destination is a register then order bit is 0.
 		if(fromReg) {
 			code = code + "0";
 		} else {
 			code = code + "1";
 		}
 		
 		String reg = "000"; //default value of register bits if registers are not used.
 		String index = "000"; //default value of index bits if index registers are not used. 
 		String lit = "0";
 		if(literal) {
 			lit = "1";
 		}
		String stack = "0";
 		//if the instruction uses the stack.
 		if(instr.getOpId().equalsIgnoreCase("PSH") 
 				|| instr.getOpId().equalsIgnoreCase("POP") 
 				|| instr.getOpId().equalsIgnoreCase("PST")) {
 			stack = "1";
 		} 
 		
 		int mem; 
 		int[] assembled = new int[1];
 		
 		if(instr.hasOperand("DX")) { 
 			index = IOFormat.formatBinInteger(instr.getOperandData("DX").value,3);
 		} else if(instr.hasOperand("DR")) { 
 			reg = IOFormat.formatBinInteger(instr.getOperandData("DR").value,3);
 		} else if(instr.hasOperand("FX")) { 
 			index = IOFormat.formatBinInteger(instr.getOperandData("FX").value,3);
 		} else if(instr.hasOperand("FR")) { 
 			reg = IOFormat.formatBinInteger(instr.getOperandData("FR").value,3);
 		} 
 
 		code = code + reg;
 		code = code + index;//add index register bits.
		code = code + "0000";//NW bits.
 		code = code + stack;//stack bit.
 		code = code + lit; //literal bit.
 		
 		if(instr.hasOperand("DM")) { 
 			mem = instr.getOperandData("DM").value;
 		} else if(instr.hasOperand("FM")) { 
 			mem = instr.getOperandData("FM").value;
 		} else if(instr.hasOperand("FL")){
 			mem = instr.getOperandData("FL").value;
 		} else {
 			mem = instr.getOperandData("FC").value;
 		}
 		
 		code = code + IOFormat.formatBinInteger(mem, 13); //concat mem bits.
 		assembled[0] = Integer.parseInt(code, 2); //parse as a binary integer.
 		
 		return assembled;
 	}
 	
 	/**
 	 * Formats instructions USI_HLT and USI_DMP into bit code.
 	 * @author Noah
 	 * @date Apr 27, 2012; 6:17:09 PM
 	 * @modified UNMODIFIED
 	 * @tested UNTESTED
 	 * @errors NO ERRORS REPORTED
 	 * @codingStandards Awaiting signature
 	 * @testingStandards Awaiting signature
 	 * @param instr instruction to format.
 	 * @return bit code
 	 * @specRef N/A
 	 */
 	public static int [] formatHaltDump(Instruction instr) {
 		int[] assembled = new int[1];
 		String code = IOFormat.formatBinInteger(instr.getOpcode(), 6); //"111111"
 		code = code + "0000000000000"; //13 unused bits.  "111111 0000000000000"
 		//13 bits of constant in memory.  "111111 000000000000 0000000011111"
 		code = code + IOFormat.formatBinInteger(instr.getOperandData("FC").value, 13); 
 		
 		assembled[0] = Integer.parseInt(code, 2);
 		
 		return assembled;
 	}
 	
 	/**
 	 * @author Ratul Khosla
 	 * @date Apr 27, 2012; 6:35:33 PM
 	 * @modified Apr 27, 2012; 7:34:00 PM removed spaces from string of 0's. -Noah
 	 * @tested UNTESTED
 	 * @errors NO ERRORS REPORTED
 	 * @codingStandards Awaiting signature
 	 * @testingStandards Awaiting signature
 	 * @param instr 
 	 * 				the instruction
 	 * @return assemble
 	 * 				the binary code of the instruction. 
 	 * @specRef N/A
 	 */
 	public static int [] formatClearXClearA(Instruction instr) {
 		int[] assembled = new int[1];
 		String code = IOFormat.formatBinInteger(instr.getOpcode(), 6); //get opcode.
 		
 		code = code + "00000000000000000000000000"; 
 		
 		assembled[0] = Integer.parseInt(code, 2); //parse as a binary integer.
 		
 		return assembled;	
 		
 	}
 	
 	/**
 	 * Formats instructions USI_IRKB and USI_CRKB into their bit codes.
 	 * @author Noah
 	 * @date Apr 27, 2012; 6:16:53 PM
 	 * @modified UNMODIFIED
 	 * @tested UNTESTED
 	 * @errors NO ERRORS REPORTED
 	 * @codingStandards Awaiting signature
 	 * @testingStandards Awaiting signature
 	 * @param instr instruction format.
 	 * @return bit code.
 	 * @specRef N/A
 	 */
 	public static int [] formatInput(Instruction instr) {
 		String code = IOFormat.formatBinInteger(instr.getOpcode(), 6); //e.g. 011000
 		String index = "000"; //default value of index bits if index registers are not used. 
 		int mem; 
 		int[] assembled = new int[1];
 		
 		code = code + "0000"; //i/o instructions have a unused 4 bits following opcode. //011000 0000
 		
 		mem = instr.getOperandData("DM").value; //mem = value of dm operand.
 		
 		if(instr.hasOperand("DX")) { //operand = {DM, NW, DX}
 			index = IOFormat.formatBinInteger(instr.getOperandData("DX").value, 3); //get index register decimal then format into binary integer string.
 		}
 		
 		code = code + index;//add index register bits.
 		code = code + IOFormat.formatBinInteger(instr.getOperandData("NW").value, 4); //add number of word bits.
 		code = code + "00"; //unused two bits for input instruction format.
 		
 		code = code + IOFormat.formatBinInteger(mem, 13); //concat mem bits.
 		assembled[0] = Integer.parseInt(code, 2); //parse as a binary integer.
 		return assembled;
 	}
 	
 	/**
 	 * Formats instructions, USI_IWSR and USI_CWSR into their bit code.
 	 * @author Noah
 	 * @date Apr 27, 2012; 6:16:56 PM
 	 * @modified UNMODIFIED
 	 * @tested UNTESTED
 	 * @errors NO ERRORS REPORTED
 	 * @codingStandards Awaiting signature
 	 * @testingStandards Awaiting signature
 	 * @param instr instruction to format.
 	 * @return bit code.
 	 * @specRef N/A
 	 */
 	public static int [] formatOutput(Instruction instr) {
 		String code = IOFormat.formatBinInteger(instr.getOpcode(), 6); //e.g. 011000
 		String index = "000"; //default value of index bits if index registers are not used. 
 		int mem; 
 		int[] assembled = new int[1];
 		
 		code = code + "0000"; //i/o instructions have a unused 4 bits following opcode. //011000 0000
 		
 		if(instr.hasOperand("FL")) {
 			mem = instr.getOperandData("FL").value;
 		} else {
 			mem = instr.getOperandData("FM").value; //mem = value of fm operand.
 			
 			if(instr.hasOperand("FX")) { //operand = {DM, NW, DX}
 				index = IOFormat.formatBinInteger(instr.getOperandData("DX").value, 3); //get index register decimal then format into binary integer string.
 			}
 		}
 		
 		code = code + index;//add index register bits.
 		code = code + IOFormat.formatBinInteger(instr.getOperandData("NW").value, 4); //add number of word bits.
 		code = code + "0"; //unused two bits for input instruction format.
 		
 		if(instr.hasOperand("FL")) {
 			code = code + "1"; 
 		} else {
 			code = code + "0";
 		}
 		
 		code = code + IOFormat.formatBinInteger(mem, 13); //concat mem bits.
 		assembled[0] = Integer.parseInt(code, 2); //parse as a binary integer.
 		return assembled;
 	}
 }
