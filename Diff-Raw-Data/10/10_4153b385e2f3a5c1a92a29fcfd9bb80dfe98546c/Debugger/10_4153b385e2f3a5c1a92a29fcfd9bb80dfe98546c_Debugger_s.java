 import java.io.*;
 
 public class Debugger{
 	
 	public RegisterFile regFile;
 	public Decode decode;
 	public ProgramCounter pc;
 	public Control control;
 	public ALUControl aluControl;
 	public MemoryIO memIO;
 	public ALU zeroALU;
 	public ALU branchALU;
 	public ALU p4ALU;
 	public Mux regDstMux;
 	public Mux memToRegMux;
 	public Mux aluSrcMux;
 	public Mux branchMux;
 	public Mux jumpMux;
 	public Mux jumpRegMux;
 	public SignExtend signExtend;
 	public ShiftLeftTwo sltAdd;
 	public ShiftLeftTwo sltTarget;
 	public Inverter inv;
 	public String output = "";
 	
 	public Debugger(RegisterFile regFile, Decode decode,
 			ProgramCounter pc, Control control,
 			ALUControl aluControl, MemoryIO memIO,
 			ALU zeroALU, ALU p4ALU, ALU branchALU,
 			Mux regDstMux, Mux memToRegMux,
 			Mux aluSrcMux, Mux branchMux, Mux jumpMux,
 			SignExtend signExtend, ShiftLeftTwo sltAdd,
 			ShiftLeftTwo sltTarget, Mux jumpRegMux,
 			Inverter inv){
 		this.regFile = regFile;
 		this.decode = decode;
 		this.pc = pc;
 		this.control = control;
 		this.aluControl = aluControl;
 		this.memIO = memIO;
 		this.zeroALU = zeroALU;
 		this.branchALU = branchALU;
 		this.p4ALU = p4ALU;
 		this.branchALU = branchALU;
 		this.regDstMux = regDstMux;
 		this.memToRegMux = memToRegMux;
 		this.aluSrcMux = aluSrcMux;
 		this.branchMux = branchMux;
 		this.jumpMux = jumpMux;
 		this.signExtend = signExtend;
 		this.sltAdd = sltAdd;
 		this.sltTarget = sltTarget;
 		this.jumpRegMux = jumpRegMux;
 		this.inv = inv;
 	}
 	
 	public void debugCycle(int cycleCount){
 		output += "################################# End of Cycle " + cycleCount +" #################################\n";
 		output += "----------------------- Program Counter Information -----------------------\n";
 		output += "Program Counter for Cycle " + cycleCount + ":" + print(pc.pcOut) + "\t\t" + printDecimal(pc.pcOut) + "\n";
 		output += "Program Counter for Cycle " + (cycleCount+1) + ":" + print(pc.pcIn) + "\t\t" + printDecimal(pc.pcIn) + "\n";
 		output += "------------------------- Instruction Information -------------------------\n";
 		output += "instruction:" + print(decode.instruction) + "\t\t" + printDecimal(decode.instruction) + "\n";
 		output += "op code:" + print(decode.opcode) + "\t\t" + printDecimal(decode.opcode)  +"\n";
 		output += "read register 1:" + print(decode.rs) + "\t\t" + printDecimal(decode.rs)  +"\n";
 		output += "read register 2:" + print(decode.rt) + "\t\t" + printDecimal(decode.rt)  +"\n";
 		output += "rd:" + print(decode.rd) + "\t\t" + printDecimal(decode.rd)  +"\n";
 		output += "funct:" + print(decode.funct) + "\t\t" + printDecimal(decode.funct)  +"\n";
 		output += "immediate:" + print(decode.immediate) + "\t\t" + printDecimal(decode.immediate)  +"\n";
 		output += "target:" + print(decode.target) + "\t\t" + printDecimal(decode.target)  +"\n";
 		output += "------------------------- Sign-Extend Information -------------------------\n";
 		output += "input:" + print(signExtend.input) + "\n";
 		output += "output:" + print(signExtend.output) + "\n";
 		output += "------------------------- SLT (Target) Information ------------------------\n";
 		output += "input:" + print(sltTarget.in) + "\n";
 		output += "output:" + print(sltTarget.out) + "\n";
 		output += "----------------------- SLT (Immediate) Information -----------------------\n";
 		output += "input:" + print(sltAdd.in) + "\n";
 		output += "output:" + print(sltAdd.out) + "\n";
 		output += "-------------------------- Control Signal Outputs -------------------------\n";
 		output += "Jump:" + print(control.jump) + "\n";
 		output += "RegDST:" + print(control.regDst) + "\n";
 		output += "Branch:" + print(control.branch) + "\n";
 		output += "MemRead:" + print(control.memRead) + "\n";
 		output += "MemToReg:" + print(control.memToReg) + "\n";
 		output += "ALUOp:" + print(control.aluOp) + "\n";
 		output += "MemWrite:" + print(control.memWrite) + "\n";
 		output += "ALUSrc:" + print(control.aluSrc) + "\n";
 		output += "RegWrite:" + print(control.regWrite) + "\n";
 		output += "JumpReg:" + print(control.jumpReg) + "\n";
 		output += "branchBNE:" + print(control.branchBNE) + "\n";
 		output += "immediate:" + print(control.immediate) + "\n";
 		output += "------------------------ ALU Control Signal Outputs -----------------------\n";		
 		output += "funct:" + print(aluControl.func) + "\n";
 		output += "ALUOp:" + print(aluControl.aluOp) + "\n";
 		output += "ALUControl Out:" + print(aluControl.aluControl) + "\t\t" + print(aluControl.aluControl) + "\n";
 		output += "-------------------------- PC + 4 ALU Information -------------------------\n";				
 		output += "input1:" + print(p4ALU.input1) + "\n";
 		output += "input2:" + print(p4ALU.input2)  + "\n";
 		output += "control:" + print(p4ALU.control)  + "\n";
 		output += "result:" + print(p4ALU.result)  + "\n";
 		output += "zero:" + print(p4ALU.zero)  + "\n";
 		output += "--------------------------- Zero ALU Information --------------------------\n";				
 		output += "input1:" + print(zeroALU.input1) + "\n";
 		output += "input2:" + print(zeroALU.input2)  + "\n";
 		output += "control:" + print(zeroALU.control)  + "\n";
 		output += "result:" + print(zeroALU.result)  + "\n";
 		output += "immediate:" + print(zeroALU.immediate) + "\n";
 		output += "zero:" + print(zeroALU.zero) + "\n";
 		output += "--------------------------- Inverter Information --------------------------\n";				
 		output += "input:" + print(inv.in) + "\n";
 		output += "branchBNE:" + print(inv.branchBNE)  + "\n";
 		output += "output:" + print(inv.out)  + "\n";
 		output += "-------------------------- Branch ALU Information -------------------------\n";				
 		output += "input1:" + print(branchALU.input1) + "\n";
 		output += "input2:" + print(branchALU.input2)  + "\n";
 		output += "control:" + print(branchALU.control)  + "\n";
 		output += "result:" + print(branchALU.result)  + "\n";
 		output += "zero:" + print(branchALU.zero)  + "\n";
 		output += "-------------------------- Memory IO Information --------------------------\n";				
 		output += "address:" +print(memIO.address) + "\t\t" + printDecimal(memIO.address) + "\n";
 		output += "writeData:" + print(memIO.writeData) +"\n";
 		output += "memWrite:" + print(memIO.memWrite) + "\n";
 		output += "memRead:" + print(memIO.memRead) + "\n";
 		output += "readData:" + print(memIO.readData) +"\n";
 		output += "--------------------------- All MUX Information ---------------------------\n";				
 		output += "\t\t\t\tregDSTMux\t\t\t\t\n";
 		output += "switcher:" + print(regDstMux.switcher) + "\n";
 		output += "input 0:" + print(regDstMux.input0) + "\n";
 		output += "input 1:" + print(regDstMux.input1) + "\n";
 		output += "output:" + print(regDstMux.output) + "\n";
 		output += "\t\t\t\tmemToRegMux\t\t\t\t\n";
 		output += "switcher:" + print(memToRegMux.switcher) + "\n";
 		output += "input 0:" + print(memToRegMux.input0) + "\n";
 		output += "input 1:" + print(memToRegMux.input1) + "\n";
 		output += "output:" + print(memToRegMux.output) + "\n";
 		output += "\t\t\t\taluSrcMux\t\t\t\t\n";
 		output += "switcher:" + print(aluSrcMux.switcher) + "\n";
 		output += "input 0:" + print(aluSrcMux.input0) + "\n";
 		output += "input 1:" + print(aluSrcMux.input1) + "\n";
 		output += "output:" + print(aluSrcMux.output) + "\n";
 		output += "\t\t\t\tbranchMux\t\t\t\t\n";
 		output += "switcher:" + print(branchMux.switcher) + "\n";
 		output += "input 0:" + print(branchMux.input0) + "\n";
 		output += "input 1:" + print(branchMux.input1) + "\n";
 		output += "output:" + print(branchMux.output) + "\n";
 		output += "\t\t\t\tjumpMux\t\t\t\t\n";
 		output += "switcher:" + print(jumpMux.switcher) + "\n";
 		output += "input 0:" + print(jumpMux.input0) + "\n";
 		output += "input 1:" + print(jumpMux.input1) + "\n";
 		output += "output:" + print(jumpMux.output) + "\n";
 		output += "\t\t\t\tjumpRegMux\t\t\t\t\n";
 		output += "switcher:" + print(jumpRegMux.switcher) + "\n";
 		output += "input 0:" + print(jumpRegMux.input0) + "\n";
 		output += "input 1:" + print(jumpRegMux.input1) + "\n";
 		output += "output:" + print(jumpRegMux.output) + "\n";
 		output += "------------------------ Register File Information -----------------------\n";
 		output += "Read Register 1:" + print(regFile.readReg1) + "\t\t" + printDecimal(regFile.readReg1)  +"\n"; 
 		output += "Read Register 2:" + print(regFile.readReg2) + "\t\t" + printDecimal(regFile.readReg2)  +"\n";
 		output += "Write Data:" + print(regFile.writeData) + "\n";
 		output += "Read Data 1:" + print(regFile.readData1) + "\n";
 		output += "Read Data 2:" + print(regFile.readData2) + "\n";
 		output += "writeReg:" + print(regFile.writeReg) + "\n";
 		output += "regWrite:" + print(regFile.regWrite) + "\n";
 		output += "Register\t\tBinary Value\t\tHex Value\t\tLong Value\n";
 		for(int i = 0 ; i < 32 ; i++){
 			BinaryNum thisVal = regFile.getVal(i);
 			//print out register values in binary
			output+= "$r" + i +":\t" + thisVal.pad(32).toString() + "\n";
	//		+ "\t0x" + Long.toHexString(thisVal.pad(32).toString()) + "\t" + thisVal + "\n";
 		}
 		output += "\n\n";
 	}
 	
 	public String print(Pin p){
 		if(p.getValue() == null){
 			return "null";
 		}else{
 			return p.getValue().toString();
 		}
 			
 	}
 	
	public int printDecimal(Pin p){
 		if(p.getValue() == null){
 			return -1;
 		}else{
			return p.getValue().toInt();
 		}
 	}
 	
 	public void dump(String fileName){
 		try{
 			// Create file 
 			FileWriter fstream = new FileWriter(fileName);
 			BufferedWriter out = new BufferedWriter(fstream);
 			out.write(output);
 			//Close the output stream
 			out.close();
 			}catch (Exception e){//Catch exception if any
 			System.err.println("Error: " + e.getMessage());
 			}
 		}
 }
