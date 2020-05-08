 package assemblernator;
 
 /**
  * 
  * @author Noah
  * @date Apr 27, 2012; 6:15:53 PM
  */
 public class InstructionFormatter {
 	/**
 	 * @author Eric Smith
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
 				 lit = IOFormat.formatBinInteger(instr.getOperandData("FL").value.value,12);
 			}else if(instr.hasOperand("FC")){
 				 lit = IOFormat.formatBinInteger(instr.getOperandData("FC").value.value,12);
 			}else{
 				 lit = IOFormat.formatBinInteger(instr.getOperandData("EX").value.value,12);
 			}
 			dmem = IOFormat.formatBinInteger(instr.getOperandData("DM").value.value,12);
 			code = code+fmt+lit+dmem;
 			//formats FM DM
 		} else if(instr.hasOperand("FM") && instr.hasOperand("DM")) {
 			fmt = "10";
 			String dmem = IOFormat.formatBinInteger(instr.getOperandData("DM").value.value,12);
 			String fmem = IOFormat.formatBinInteger(instr.getOperandData("FM").value.value,12);
 			code = code+fmt+fmem+dmem;
 			//formats for {FL DR}, {FL DX}, {EX DR}, {EX DX}, {FC DX}, {FC DR}
 		} else if(instr.hasOperand("FL") || instr.hasOperand("EX") || instr.hasOperand("FC")) {
 			fmt = "01";
 			String lit;
 			//gets value for literal part of format
 			if(instr.hasOperand("FL")){
 				if (instr.getOperandData("FL").value == null)
 				System.err.println("INTERNAL ERROR: Instruction "
 						+ instr.getOpId()
 						+ ": FL operand does not cache value returned by evaluate().");
 				lit = IOFormat.formatBinInteger(instr.getOperandData("FL").value.value,16);
 			}
 			else if(instr.hasOperand("EX")){
 				if (instr.getOperandData("EX").value == null)
 				System.err.println("INTERNAL ERROR: Instruction "
 						+ instr.getOpId()
 						+ ": EX operand does not cache value returned by evaluate().");
 				lit = IOFormat.formatBinInteger(instr.getOperandData("EX").value.value,16);
 			}
 			else{
 				if (instr.getOperandData("FC").value == null)
 					System.err.println("INTERNAL ERROR: Instruction "
 							+ instr.getOpId()
 							+ ": FC operand does not cache value returned by evaluate().");
 				lit = IOFormat.formatBinInteger(instr.getOperandData("FC").value.value,16);
 			}	
 			//gets value for dest part of format
 			if(instr.hasOperand("DR")){
 				String reg = IOFormat.formatBinInteger(instr.getOperandData("DR").value.value,3);
 				code= code+fmt+"10000"+reg+lit;
 			}else if(instr.hasOperand("DX")){
 				String dindex = IOFormat.formatBinInteger(instr.getOperandData("DX").value.value,3);
 				code= code+fmt+"10001"+dindex+lit;
			}else{
				code = code+fmt+srcReg+destReg+lit;
 			}
 			//formats all other combos
 		} else {
 			fmt = "00";
 			//gets destReg, mem , and ixr
 			if(instr.hasOperand("DM")){
 				if (instr.getOperandData("DM").value == null)
 					System.err.println("INTERNAL ERROR: Instruction "
 							+ instr.getOpId()
 							+ ": DM operand does not cache value returned by evaluate().");
 				mem = IOFormat.formatBinInteger(instr.getOperandData("DM").value.value,12);
 				if(instr.hasOperand("DX")){
 					ixr = IOFormat.formatBinInteger(instr.getOperandData("DX").value.value,4);
 				}
 				if(instr.hasOperand("DR")){
 					srcReg = IOFormat.formatBinInteger(instr.getOperandData("DR").value.value,4);
 				}
 			}else if(instr.hasOperand("DX")){
 				if (instr.getOperandData("DX").value == null)
 					System.err.println("INTERNAL ERROR: Instruction "
 							+ instr.getOpId()
 							+ ": DX operand does not cache value returned by evaluate().");
 				destReg = IOFormat.formatBinInteger(instr.getOperandData("DX").value.value,3);
 				destReg="1"+destReg;
 			}else if(instr.hasOperand("DR")){
 				if (instr.getOperandData("DR").value == null)
 					System.err.println("INTERNAL ERROR: Instruction "
 							+ instr.getOpId()
 							+ ": DR operand does not cache value returned by evaluate().");
 				destReg = IOFormat.formatBinInteger(instr.getOperandData("DR").value.value,3);
 				destReg="0"+destReg;
 			}
 			
 			//gets srcReg, mem, and ixr
 			if(instr.hasOperand("FM")){
 				if (instr.getOperandData("FM").value == null)
 					System.err.println("INTERNAL ERROR: Instruction "
 							+ instr.getOpId()
 							+ ": FM operand does not cache value returned by evaluate().");
 				mem = IOFormat.formatBinInteger(instr.getOperandData("FM").value.value,12);
 				if(instr.hasOperand("FX")){
 					ixr = IOFormat.formatBinInteger(instr.getOperandData("FX").value.value,4);
 				}
 			}else if(instr.hasOperand("FR")){
 				srcReg = IOFormat.formatBinInteger(instr.getOperandData("FR").value.value,3);
 				srcReg = "0"+srcReg;
 			}else if(instr.hasOperand("FX")){
 				srcReg = IOFormat.formatBinInteger(instr.getOperandData("FX").value.value,3);
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
 			code = code + IOFormat.formatBinInteger(instr.getOperandData("FC").value.value, 16); 
 		} else if(instr.hasOperand("EX")){
 			code = code + IOFormat.formatBinInteger(instr.getOperandData("EX").value.value, 16); 
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
 	public static int [] formatOPOnly(Instruction instr) {
 		int[] assembled = new int[1];
 		String code = IOFormat.formatBinInteger(instr.getOpcode(), 6); //get opcode.
 		
 		code = code + "00000000000000000000000000"; //26 bits.
 		
 		assembled[0] = IOFormat.parseBin32Int(code);
 		
 		return assembled;	
 		
 	}
 	
 	/**
 	 * Formats instructions USI_IRKB, USI_ISRG, and USI_CRKB into their bit codes.
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
 	public static int [] formatDestMem(Instruction instr) {
 		String code = IOFormat.formatBinInteger(instr.getOpcode(), 6);
 		String fmtLit = "00";
 		String nw = IOFormat.formatBinInteger(instr.getOperandData("NW").value.value, 4);
 		String source = "1000";
 		String ixr = "0000";
 		String mem = IOFormat.formatBinInteger(instr.getOperandData("DM").value.value, 12);;
 		int[] assembled = new int[1];
 		
 		if(instr.hasOperand("DX")) {
 			ixr = IOFormat.formatBinInteger(instr.getOperandData("DX").value.value, 4);
 		}
 		
 		code = code + fmtLit + source + nw + ixr + mem;
 
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
 	public static int [] formatSrcMem(Instruction instr) {
 		String code = IOFormat.formatBinInteger(instr.getOpcode(), 6); //e.g. 011000
 		String fmt;
 		String nw = IOFormat.formatBinInteger(instr.getOperandData("NW").value.value, 4);
 		String dest;
 		String ixr = "";
 		String memLit; 
 		int[] assembled = new int[1];
 		
 		if(instr.hasOperand("FL") || instr.hasOperand("EX")) {
 			fmt = "01";
 			if(instr.hasOperand("FL")) {
 				memLit = IOFormat.formatBinInteger(instr.getOperandData("FL").value.value, 16);
 			} else {
 				memLit = IOFormat.formatBinInteger(instr.getOperandData("EX").value.value, 16);
 			}
 		} else {
 			fmt = "00";
 			memLit = IOFormat.formatBinInteger(instr.getOperandData("FM").value.value, 12);
 			if(instr.hasOperand("FX")) {
 				ixr = IOFormat.formatBinInteger(instr.getOperandData("FX").value.value, 4);
 			} else {
 				ixr = "0000";
 			}
 			
 		}
 		
 		if(instr.hasOperand("DR")) {
 			dest = IOFormat.formatBinInteger(instr.getOperandData("DR").value.value, 4);
 		} else if(instr.hasOperand("DX")){
 			dest = "1" + IOFormat.formatBinInteger(instr.getOperandData("DX").value.value, 3);
 		} else {
 			dest = "1000";
 		}
 		
 		code = code + fmt + nw + dest + ixr + memLit;
 		
 		assembled[0] = IOFormat.parseBin32Int(code);
 		return assembled;
 	}
 }
