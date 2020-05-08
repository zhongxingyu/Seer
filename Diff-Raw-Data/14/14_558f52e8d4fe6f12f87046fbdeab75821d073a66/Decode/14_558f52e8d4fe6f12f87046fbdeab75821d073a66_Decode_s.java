 public class Decode{
 	// the instruction to decode
 	public Pin instruction = new Pin();
 	
 	// op code of instruction[31:26]
 	public Pin opcode = new Pin();
 	
 	// rs field of instruction[25:21]
 	// read register 1 of RegFile
 	public Pin rs = new Pin();
 	
 	// rt field of instruction[20:16]
 	// read register 2 of RegFile
 	public Pin rt = new Pin();
 	
 	// rd field of instruction[15:11]
 	// sent to MUX to determine Write Register of RegFile
 	public Pin rd = new Pin();
 	
 	// funct field of instruction[5:0]
 	// sent to ALU control for R-Type instructions
 	public Pin funct = new Pin();
 	
 	// 16-bit offset for branch, equal, load, and store
 	// sent to sign-extend
 	public Pin offset = new Pin();
 	
 	// 25-bit for target of jump
 	public Pin target = new Pin();
 
 
 	// 0 for R-Type, 1 for I-Type, 2 for J-Type, 3 for HLT
 	// -1 for unrecognized opcode
 	public int opType = -1;
 	
 	// creates this object with an instruction from fetch stage
 	Decode(long instruction){
 		this.instruction.setValue(instruction);
 	}
 	
 	// takes first 6 bits as opcode
 	public void setOpcode(String instr){
 		opcode.setValue(Long.parseLong(instr.substring(0,5)));
 	}
 	
 	// takes next 5 bits as rs value
 	public void setRS(String instr){
 		rs.setValue(Long.parseLong(instr.substring(6,10)));
 	}
 	
 	// takes next 5 bits as rt value
 	public void setRT(String instr){
 		rt.setValue(Long.parseLong(instr.substring(11,15)));
 		
 	}
 	
 	// sets the rd and func value if r-type
 	public void setRD(String instr){
 		rd.setValue(Long.parseLong(instr.substring(16,20)));
 	}
 	public void setFunct(String instr){
 		funct.setValue(Long.parseLong(instr.substring(26,31)));
 	}
 	
 	// sets the offset value for i-type and j-type
 	public void setOffset(String instr){
 		offset.setValue(Long.parseLong(instr.substring(16,31)));
 	}
 	
 	// set jump target for j-type
 	public void setTarget(String instr){
 		offset.setValue(Long.parseLong(instr.substring(6, 31)));
 	}
 	
 	// determines if this instruction is R-Type, I-Type, or J-Type
 	public void setOpType(String op){
 		
 		// R-Type instructions all have opcode as 000000
 		if(op.equals("000000")){
 			opType = 0;
 		}
 		
 		// I-Type instruction are any opcodes other than
 		// 000000, 00001x, 0100xx, and 111111
 		if(!op.equals("000000") &&
 				!op.equals("000010") &&
 				!op.equals("000011") &&
 				!op.equals("010000") &&
 				!op.equals("010001") &&
 				!op.equals("010011") &&
 				!op.equals("010010") &&
 				!op.equals("111111")){
 			opType = 1;
 		}
 		
 		// J-type instructions have opcodes of 00001x
 		if(op.equals("000010") || op.equals("000011")){
 			opType = 2;
 		}
 		
 		// HLT's opcode is 111111
 		if(op.equals("111111")){
 			opType = 3;
 		}
 		
 		// throw an error for unsupported opcode
 		if(opType == -1){
 			throw new Error("Unsupported opcode: " + op);
 		}
 	}
 	
 	// function that returns True if this instruction is a halt.
 	public boolean isHalt(){
 		return opType == 3;
 	}
 	
 	
 	// Decodes the instruction
 	public void clockEdge(){
 		
 		// converts the instruction into a binary String
 		String instrStr = Long.toBinaryString(instruction.getValue());
 		
		// save the length of this instruction to avoid recalculating it later
		int instrStrLen = instrStr.length();
		
 		// instruction is more than 32 bits so throw an error
		if(instrStrLen > 32){
 			throw new Error("Instruction is longer than 32 bits");
 		}
 		
		// instruction is less than 32 bits so zero-pad it
		if(instrStrLen < 32){
			instrStr = String.format("%32s", instrStr.replace(" ", "0"));
		}
 		
 		// instruction should now be 32 bits, so decode it.
 		if(instrStr.length() == 32){
 			setOpcode(instrStr);
 			setOpType(Long.toBinaryString(opcode.getValue()));
 			setRS(instrStr);
 			setRT(instrStr);
 			setTarget(instrStr);
 			if(opType == 0){
 				setRD(instrStr);
 				setFunct(instrStr);
 			}
 			else setOffset(instrStr);
 		}
 		else throw new Error("Bad instruction length: " + instrStr);
 		
 	}
 }
