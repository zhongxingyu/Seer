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
 	Decode(){}
 	
 	// takes first 6 bits as opcode
 	public void setOpcode(String instr){
		opcode.setValue(Long.parseLong(instr.substring(0,5), 2));
 	}
 	
 	// takes next 5 bits as rs value
 	public void setRS(String instr){
		rs.setValue(Long.parseLong(instr.substring(6,10), 2));
 	}
 	
 	// takes next 5 bits as rt value
 	public void setRT(String instr){
		rt.setValue(Long.parseLong(instr.substring(11,15), 2));
 		
 	}
 	
 	// sets the rd and func value if r-type
 	public void setRD(String instr){
		rd.setValue(Long.parseLong(instr.substring(16,20), 2));
 	}
 	public void setFunct(String instr){
		funct.setValue(Long.parseLong(instr.substring(26,31), 2));
 	}
 	
 	// sets the offset value for i-type and j-type
 	public void setOffset(String instr){
		offset.setValue(Long.parseLong(instr.substring(16,31), 2));
 	}
 	
 	// set jump target for j-type
 	public void setTarget(String instr){
		target.setValue(Long.parseLong(instr.substring(6,31), 2));
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
 		String instrStr = BinaryUtil.pad(Long.toBinaryString(instruction.getValue()),32);
 		
 		// decode the instruction
 		setOpcode(instrStr);
 		setOpType(Long.toBinaryString(opcode.getValue()));
 		setRS(instrStr);
 		setRT(instrStr);
 		setTarget(instrStr);
 		setRD(instrStr);
 		setFunct(instrStr);
 		setOffset(instrStr);
 		
 		System.out.println("[DEBUG] Class:Decode, instr:" + instrStr +
 												  "\nopCode:" + BinaryUtil.pad(Long.toBinaryString(opcode.getValue()),6) +
 												  "\nRS:" + BinaryUtil.pad(Long.toBinaryString(rs.getValue()), 5) +
 												  "\nRT:" + BinaryUtil.pad(Long.toBinaryString(rt.getValue()), 5) +
 												  "\nTarget:" + BinaryUtil.pad(Long.toBinaryString(target.getValue()), 26) +
 												  "\nRD:" + BinaryUtil.pad(Long.toBinaryString(rd.getValue()), 5) +
 												  "\nFunct:" + BinaryUtil.pad(Long.toBinaryString(funct.getValue()), 6) +
 												  "\nOffset:" + BinaryUtil.pad(Long.toBinaryString(offset.getValue()), 16));
 												 
 												  
 		
 	}
 }
