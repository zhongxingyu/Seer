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
 		String fmt;
 		String srcReg = "1000"; //default values
 		String destReg = "1000";
 		String ixr = "0000";
 		String mem = "000000000000";
 		// formats FL DM, FC DM, EX DM
 		if(instr.hasOperand("FL") && instr.hasOperand("DM") || instr.hasOperand("FC") && instr.hasOperand("DM") || instr.hasOperand("EX") && instr.hasOperand("DM")) {
 			fmt = "11";
 			String dmem;
 			String lit;
 			if(instr.hasOperand("FL")){
 				 lit = IOFormat.formatBinInteger(instr.getOperandData("FL").value,12);
 			}else if(instr.hasOperand("FC")){
 				 lit = IOFormat.formatBinInteger(instr.getOperandData("FC").value,12);
 			}else{
 				 lit = IOFormat.formatBinInteger(instr.getOperandData("EX").value,12);
 			}
 			dmem = IOFormat.formatBinInteger(instr.getOperandData("DM").value,12);
 			code = code+fmt+lit+dmem;
 			//formats FM DM
 		} else if(instr.hasOperand("FM") && instr.hasOperand("DM")) {
 			fmt = "10";
 			String dmem = IOFormat.formatBinInteger(instr.getOperandData("DM").value,12);
 			String fmem = IOFormat.formatBinInteger(instr.getOperandData("FM").value,12);
 			code = code+fmt+fmem+dmem;
 			//formats for {FL DR}, {FL DX}, {EX DR}, {EX DX}, {FC DX}, {FC DR}
 		} else if(instr.hasOperand("FL") || instr.hasOperand("EX") || instr.hasOperand("FC")) {
 			fmt = "01";
 			String lit;
 			//gets value for literal part of format
 			if(instr.hasOperand("FL")){
 				lit = IOFormat.formatBinInteger(instr.getOperandData("FL").value,16);
 			}
 			else if(instr.hasOperand("EX")){
 				lit = IOFormat.formatBinInteger(instr.getOperandData("EX").value,16);
 			}
 			else{
 				lit = IOFormat.formatBinInteger(instr.getOperandData("FC").value,16);
 			}	
 			//gets value for dest part of format
 			if(instr.hasOperand("DR")){
 				String reg = IOFormat.formatBinInteger(instr.getOperandData("DR").value,3);
 				code= code+fmt+"10000"+reg+lit;
 			}else{
 				String dindex = IOFormat.formatBinInteger(instr.getOperandData("DX").value,3);
 				code= code+fmt+"10001"+dindex+lit;
 			}
 			//formats all other combos
 		} else {
 			fmt = "00";
 			//gets destReg, mem , and ixr
 			if(instr.hasOperand("DM")){
 				mem = IOFormat.formatBinInteger(instr.getOperandData("DM").value,12);
 				if(instr.hasOperand("DX")){
 					ixr = IOFormat.formatBinInteger(instr.getOperandData("DX").value,4);
 				}
 			}else if(instr.hasOperand("DX")){
 				destReg = IOFormat.formatBinInteger(instr.getOperandData("DX").value,3);
 				destReg="1"+destReg;
 			}else if(instr.hasOperand("DR")){
 				destReg = IOFormat.formatBinInteger(instr.getOperandData("DR").value,3);
 				destReg="0"+destReg;
 			}
 			
 			//gets srcReg, mem, and ixr
 			if(instr.hasOperand("FM")){
 				mem = IOFormat.formatBinInteger(instr.getOperandData("FM").value,12);
 				if(instr.hasOperand("FX")){
 					ixr = IOFormat.formatBinInteger(instr.getOperandData("FX").value,4);
 				}
 			}else if(instr.hasOperand("FR")){
 				srcReg = IOFormat.formatBinInteger(instr.getOperandData("FR").value,3);
 				srcReg = "0"+srcReg;
 			}else if(instr.hasOperand("FX")){
 				srcReg = IOFormat.formatBinInteger(instr.getOperandData("FX").value,3);
 				srcReg = "1"+srcReg;
 			}
 			code = code+fmt+srcReg+destReg+ixr+mem;
 		}
 		int[] assembled = new int[1];
 		assembled[0] = IOFormat.parseBin32Int(code); //parse as a binary integer.
 		return assembled;
 	}
 	
 	/**
 	 * Formats instructions USI_HLT and USI_DMP into bit code.
 	 * opcode(6 bits) + "0000000000" (10 unused bits) + constant (16 constant bits).
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
 		code = code + "0000000000"; //10 unused bits.  "111111 0000000000"
 		//16 bits of constant in memory.  "111111 000000000000 0000000011111"
 		if(instr.hasOperand("FC")) {
 			code = code + IOFormat.formatBinInteger(instr.getOperandData("FC").value, 16); 
 		} else if(instr.hasOperand("EX")){
 			code = code + IOFormat.formatBinInteger(instr.getOperandData("EX").value, 16); 
 		}
 		assembled[0] = IOFormat.parseBin32Int(code);
 		return assembled;
 	}
 	
 	/**
 	 * opcode(6 bits) + "00000000000000000000000000" (26 unused bits).
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
 		
 		code = code + "00000000000000000000000000"; //26 bits.
 		
 		assembled[0] = IOFormat.parseBin32Int(code);
 		
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
 		//opcode(6b) + "00" + number of words (4b) + "1000" + memAddr(16b) or 
 		//opcode(6b) + "00" + number of words (4b) + "1000" + ixr(4b) + memAddr(12b)
 		String code = IOFormat.formatBinInteger(instr.getOpcode(), 6); //e.g. 011000
 		String fmt = "00";
 		String nw = IOFormat.formatBinInteger(instr.getOperandData("NW").value, 4);
 		String destReg = "1000"; //destination is never a register.
 		int mem; 
 		int[] assembled = new int[1];
 		
 		code = code + fmt + nw + destReg;
 		
 		if(instr.hasOperand("DX")) {
 			code = code + IOFormat.formatBinInteger(instr.getOperandData("DX").value, 4); //add ixr bits.
 		} else {
 			code = code + "0000"; //ixr bits are 0'd.
 		}
 		
 		mem = instr.getOperandData("DM").value; //mem = value of dm operand.
 		
 		code = code + IOFormat.formatBinInteger(mem, 12); //concat memory bits.
 		assembled[0] = IOFormat.parseBin32Int(code);
 		
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
 		String fmt;
 		String nw = IOFormat.formatBinInteger(instr.getOperandData("NW").value, 4);
 		String ixr = "0000";
 		int mem; 
 		int[] assembled = new int[1];
 		
 		if(instr.hasOperand("FL") || instr.hasOperand("EX")) {
 			fmt = "01";
 			if(instr.hasOperand("FL")) {
 				mem = instr.getOperandData("FL").value;
 			} else {
 				mem = instr.getOperandData("EX").value;
 			}
 			code = code + fmt + "1000" + nw + IOFormat.formatBinInteger(mem, 16); 
 		} else { 
 			fmt = "00";
 			mem = instr.getOperandData("FM").value; //mem = value of fm operand.
 	
 			if(instr.hasOperand("FX")) { //operand = {DM, NW, DX}
 				 ixr = IOFormat.formatBinInteger(instr.getOperandData("FX").value, 4); //get index register decimal then format into binary integer string.
 			}
 			
 			code = code + fmt + "1000"+  nw + ixr +  IOFormat.formatBinInteger(mem, 12); 
 		}
 		
 		assembled[0] = IOFormat.parseBin32Int(code);
 		return assembled;
 	}
 }
