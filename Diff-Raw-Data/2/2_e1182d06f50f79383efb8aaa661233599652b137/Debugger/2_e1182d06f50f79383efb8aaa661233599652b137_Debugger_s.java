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
 		output += "Program Counter for Cycle " + cycleCount + ":" + BinaryUtil.pad(print(pc.pcOut),32) + "\t\t" + printDecimal(pc.pcOut) + "\n";
 		output += "Program Counter for Cycle " + (cycleCount+1) + ":" + BinaryUtil.pad(print(pc.pcIn),32) + "\t\t" + printDecimal(pc.pcIn) + "\n";
 		output += "------------------------- Instruction Information -------------------------\n";
 		output += "instruction:" + BinaryUtil.pad(print(decode.instruction),32) + "\t\t" + printDecimal(decode.instruction) + "\n";
 		output += "op code:" + BinaryUtil.pad(print(decode.opcode),6) + "\t\t" + printDecimal(decode.opcode)  +"\n";
 		output += "read register 1:" + BinaryUtil.pad(print(decode.rs),5) + "\t\t" + printDecimal(decode.rs)  +"\n";
 		output += "read register 2:" + BinaryUtil.pad(print(decode.rt),5) + "\t\t" + printDecimal(decode.rt)  +"\n";
 		output += "rd:" + BinaryUtil.pad(print(decode.rd),5) + "\t\t" + printDecimal(decode.rd)  +"\n";
 		output += "funct:" + BinaryUtil.pad(print(decode.funct),6) + "\t\t" + printDecimal(decode.funct)  +"\n";
 		output += "immediate:" + BinaryUtil.pad(print(decode.immediate),16) + "\t\t" + printDecimal(decode.immediate)  +"\n";
 		output += "target:" + BinaryUtil.pad(print(decode.target),26) + "\t\t" + printDecimal(decode.target)  +"\n";
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
 		output += "Jump:" + BinaryUtil.pad(print(control.jump),1) + "\n";
 		output += "RegDST:" + BinaryUtil.pad(print(control.regDst),1) + "\n";
 		output += "Branch:" + BinaryUtil.pad(print(control.branch),1) + "\n";
 		output += "MemRead:" + BinaryUtil.pad(print(control.memRead),1) + "\n";
 		output += "MemToReg:" + BinaryUtil.pad(print(control.memToReg),1) + "\n";
 		output += "ALUOp:" + BinaryUtil.pad(print(control.aluOp),2) + "\n";
 		output += "MemWrite:" + BinaryUtil.pad(print(control.memWrite),1) + "\n";
 		output += "ALUSrc:" + BinaryUtil.pad(print(control.aluSrc),1) + "\n";
 		output += "RegWrite:" + BinaryUtil.pad(print(control.regWrite),1) + "\n";
 		output += "JumpReg:" + BinaryUtil.pad(print(control.jumpReg),1) + "\n";
 		output += "branchBNE:" + BinaryUtil.pad(print(control.branchBNE),1) + "\n";
 		output += "------------------------ ALU Control Signal Outputs -----------------------\n";		
 		output += "funct:" + BinaryUtil.pad(print(aluControl.func),6) + "\n";
 		output += "ALUOp:" + BinaryUtil.pad(print(aluControl.aluOp),2) + "\n";
 		output += "ALUControl Out:" + BinaryUtil.pad(print(aluControl.aluControl),3) + "\t\t" + print(aluControl.aluControl) + "\n";
 		output += "-------------------------- PC + 4 ALU Information -------------------------\n";				
 		output += "input1:" + BinaryUtil.pad(print(p4ALU.input1),32) + "\n";
 		output += "input2:" + BinaryUtil.pad(print(p4ALU.input2),32)  + "\n";
 		output += "control:" + BinaryUtil.pad(print(p4ALU.control),32)  + "\n";
 		output += "result:" + BinaryUtil.pad(print(p4ALU.result), 32)  + "\n";
 		output += "zero:" + BinaryUtil.pad(print(p4ALU.zero),32)  + "\n";
 		output += "--------------------------- Zero ALU Information --------------------------\n";				
 		output += "input1:" + BinaryUtil.pad(print(zeroALU.input1),32) + "\n";
 		output += "input2:" + BinaryUtil.pad(print(zeroALU.input2),32)  + "\n";
 		output += "control:" + BinaryUtil.pad(print(zeroALU.control),32)  + "\n";
 		output += "result:" + BinaryUtil.pad(print(zeroALU.result), 32)  + "\n";
 		output += "zero:" + BinaryUtil.pad(print(zeroALU.zero),32)  + "\n";
 		output += "--------------------------- Inverter Information --------------------------\n";				
 		output += "input:" + BinaryUtil.pad(print(inv.in),1) + "\n";
 		output += "branchBNE:" + BinaryUtil.pad(print(inv.branchBNE),1)  + "\n";
 		output += "output:" + BinaryUtil.pad(print(inv.out),1)  + "\n";
 		output += "-------------------------- Branch ALU Information -------------------------\n";				
 		output += "input1:" + BinaryUtil.pad(print(branchALU.input1),32) + "\n";
 		output += "input2:" + BinaryUtil.pad(print(branchALU.input2),32)  + "\n";
 		output += "control:" + BinaryUtil.pad(print(branchALU.control),32)  + "\n";
 		output += "result:" + BinaryUtil.pad(print(branchALU.result), 32)  + "\n";
 		output += "zero:" + BinaryUtil.pad(print(branchALU.zero),32)  + "\n";
 		output += "-------------------------- Memory IO Information --------------------------\n";				
 		output += "address:" +BinaryUtil.pad(print(memIO.address), 32) + "\t\t" + printDecimal(memIO.address) + "\n";
 		output += "writeData:" + BinaryUtil.pad(print(memIO.writeData), 32) +"\n";
 		output += "memWrite:" + BinaryUtil.pad(print(memIO.memWrite),1) + "\n";
 		output += "memRead:" + BinaryUtil.pad(print(memIO.memRead),1) + "\n";
 		output += "readData:" + BinaryUtil.pad(print(memIO.readData),32) +"\n";
 		output += "--------------------------- All MUX Information ---------------------------\n";				
 		output += "\t\t\t\tregDSTMux\t\t\t\t\n";
 		output += "switcher:" + BinaryUtil.pad(print(regDstMux.switcher),1) + "\n";
 		output += "input 0:" + BinaryUtil.pad(print(regDstMux.input0),32) + "\n";
 		output += "input 1:" + BinaryUtil.pad(print(regDstMux.input1),32) + "\n";
 		output += "output:" + BinaryUtil.pad(print(regDstMux.output),32) + "\n";
 		output += "\t\t\t\tmemToRegMux\t\t\t\t\n";
 		output += "switcher:" + BinaryUtil.pad(print(memToRegMux.switcher),1) + "\n";
 		output += "input 0:" + BinaryUtil.pad(print(memToRegMux.input0),32) + "\n";
 		output += "input 1:" + BinaryUtil.pad(print(memToRegMux.input1),32) + "\n";
 		output += "output:" + BinaryUtil.pad(print(memToRegMux.output),32) + "\n";
 		output += "\t\t\t\taluSrcMux\t\t\t\t\n";
 		output += "switcher:" + BinaryUtil.pad(print(aluSrcMux.switcher),1) + "\n";
 		output += "input 0:" + BinaryUtil.pad(print(aluSrcMux.input0),32) + "\n";
 		output += "input 1:" + BinaryUtil.pad(print(aluSrcMux.input1),32) + "\n";
 		output += "output:" + BinaryUtil.pad(print(aluSrcMux.output),32) + "\n";
 		output += "\t\t\t\tbranchMux\t\t\t\t\n";
 		output += "switcher:" + BinaryUtil.pad(print(branchMux.switcher),1) + "\n";
 		output += "input 0:" + BinaryUtil.pad(print(branchMux.input0),32) + "\n";
 		output += "input 1:" + BinaryUtil.pad(print(branchMux.input1),32) + "\n";
 		output += "output:" + BinaryUtil.pad(print(branchMux.output),32) + "\n";
 		output += "\t\t\t\tjumpMux\t\t\t\t\n";
 		output += "switcher:" + BinaryUtil.pad(print(jumpMux.switcher),1) + "\n";
 		output += "input 0:" + BinaryUtil.pad(print(jumpMux.input0),32) + "\n";
 		output += "input 1:" + BinaryUtil.pad(print(jumpMux.input1),32) + "\n";
 		output += "output:" + BinaryUtil.pad(print(jumpMux.output),32) + "\n";
 		output += "\t\t\t\tjumpRegMux\t\t\t\t\n";
 		output += "switcher:" + BinaryUtil.pad(print(jumpRegMux.switcher),1) + "\n";
 		output += "input 0:" + BinaryUtil.pad(print(jumpRegMux.input0),32) + "\n";
 		output += "input 1:" + BinaryUtil.pad(print(jumpRegMux.input1),32) + "\n";
 		output += "output:" + BinaryUtil.pad(print(jumpRegMux.output),32) + "\n";
 		output += "------------------------ Register File Information -----------------------\n";
 		output += "Read Register 1:" + BinaryUtil.pad(print(regFile.readReg1),32) + "\t\t" + printDecimal(regFile.readReg1)  +"\n"; 
 		output += "Read Register 2:" + BinaryUtil.pad(print(regFile.readReg2),32) + "\t\t" + printDecimal(regFile.readReg2)  +"\n";
		output += "Read Register 1:" + BinaryUtil.pad(print(regFile.writeData),32) + "\n";
 		output += "Read Data 1:" + BinaryUtil.pad(print(regFile.readData1),32) + "\n";
 		output += "Read Data 2:" + BinaryUtil.pad(print(regFile.readData2),32) + "\n";
 		output += "Register\t\tBinary Value\t\tHex Value\t\tLong Value\n";
 		for(int i = 0 ; i < 32 ; i++){
 			Long thisVal = regFile.getVal(i);
 			//print out register values in binary
 			output+= "$r" + i +":\t" + BinaryUtil.pad(Long.toBinaryString(thisVal), 32)
 			+ "\t0x" + Long.toHexString(thisVal) + "\t" + thisVal + "\n";
 		}
 		output += "\n\n";
 	}
 	
 	public String print(Pin p){
 		if(p.getValue() == null){
 			return "null";
 		}else{
 			return Long.toBinaryString(p.getValue());
 		}
 			
 	}
 	
 	public String printDecimal(Pin p){
 		if(p.getValue() == null){
 			return "null";
 		}else{
 			return Long.toString(p.getValue());
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
