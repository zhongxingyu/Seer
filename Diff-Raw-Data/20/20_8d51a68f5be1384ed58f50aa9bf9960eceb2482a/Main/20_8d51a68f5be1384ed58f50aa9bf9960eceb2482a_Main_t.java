 import java.util.List;
 
 public class Main {
 
 	/**
 	 * Entry point for program
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		
 		//Read mem from program file
 		List<BinaryNum> rawMemory = ProgramReader.getMemory(args[0]);
 		
 		Memory memory = new Memory(rawMemory);
 		
 		// Initialize the MUX's
 		Mux aluSrcMux = new Mux();
 		Mux memToRegMux = new Mux();
 		Mux regDstMux = new Mux();
 		Mux jumpMux = new Mux();
 		Mux branchMux = new Mux();
 		Mux jumpRegMux = new Mux();
 		Mux3 forwardAMux = new Mux3();
 		Mux3 forwardBMux = new Mux3();
 		
 		// Initialize the 2 SLTs
 	 	ShiftLeftTwo sltTarget = new ShiftLeftTwo(28);
 		ShiftLeftTwo sltAdd = new ShiftLeftTwo(32);
 		
 		// Initialize the 3 ALUs
 		ALU alu = new ALU();
 		ALU aluAdd = new ALU();
 		ALU aluP4 = new ALU();
 		// set the static controls/inputs for certain ALU
 		aluAdd.control.setValue(new BinaryNum("010"));
 		aluP4.control.setValue(new BinaryNum("010"));
 		aluP4.input2.setValue(new BinaryNum("100").pad(32));
 		
 		// initalize Memory IO
 		MemoryIO memoryIo = new MemoryIO(memory);
 		
 		// initialize main control and ALU control
 		Control control = new Control();
 		ALUControl aluControl = new ALUControl();
 		
 		// initialize the register file
 		RegisterFile regFile = new RegisterFile();
 		
 		// initialize fetch and decode stages
 		Fetch fetch = new Fetch(memory);
 		Decode decode = new Decode();
 
 		// initialize the AND going to branch MUX
 		And branchMuxAnd = new And();
 		
 		// initialize the combiner (jump addr + (PC + 4))
 		Combiner combiner = new Combiner();
 		
 		// initialize the sign-extend
 		SignExtend signExtend = new SignExtend();
 		
 		// initialize the inverter
 		Inverter inv = new Inverter();
 		
 		// initialize the Program Counter
 		ProgramCounter pc = new ProgramCounter();
 		
 		// initialize the Pipeline Registers
 		IFID ifid = new IFID();
 		IDEX idex = new IDEX();
 		EXMEM exmem = new EXMEM();
 		MEMWB memwb = new MEMWB();
 		
 		// initialize the forwarding unit
 		ForwardingUnit forwardingUnit = new ForwardingUnit();
 		
 		// For debugging
 		Debugger debug = new Debugger(regFile, decode, pc, control, aluControl,
 				memoryIo, alu, aluP4, aluAdd, regDstMux, memToRegMux, aluSrcMux, 
 				branchMux, jumpMux, signExtend, sltAdd, sltTarget, jumpRegMux,
				inv, combiner, branchMuxAnd, forwardingUnit, forwardAMux, forwardBMux);
 		DebuggerPR pipelineDebug = new DebuggerPR(ifid, idex, exmem, memwb);
 		
 		// Connect output of PC
 		pc.pcOut.connectTo(fetch.pc);
 		pc.pcOut.connectTo(aluP4.input1);
 		
 		// Connect output of fetch
 		fetch.instr.connectTo(ifid.instruction);
 		
 		// Connect the outputs of the decode
 		decode.opcode.connectTo(control.opcode);
 		decode.rs.connectTo(regFile.readReg1);
 		decode.rs.connectTo(idex.rs);
 		decode.rt.connectTo(regFile.readReg2);
 		decode.rt.connectTo(idex.rt);
 		decode.rd.connectTo(idex.rd);
 		decode.target.connectTo(sltTarget.in);
 		decode.immediate.connectTo(signExtend.input);
 		decode.funct.connectTo(idex.funct);
 		decode.funct.connectTo(control.funct);
 		
 		// connect the outputs of sign-extend
 		signExtend.output.connectTo(idex.signExtended);
 		
 		// Connect outputs of regFile
 		regFile.readData1.connectTo(idex.readData1);
 		regFile.readData2.connectTo(idex.readData2);
 				
 		// Connect outputs of ALU and related
 		aluSrcMux.output.connectTo(alu.input2);
 		alu.result.connectTo(exmem.genALUResult);
 		alu.zero.connectTo(exmem.zero);
 		inv.out.connectTo(branchMuxAnd.input1);
 		aluP4.result.connectTo(ifid.PC4);
 		aluP4.result.connectTo(branchMux.input0);
 		aluP4.result.connectTo(combiner.pcIn);
 		aluAdd.result.connectTo(exmem.addALUresult);
 		
 		// Connect output of memoryIo
 		memoryIo.readData.connectTo(memwb.readData);
 		
 		// Connect the outputs of SLTs
 		sltTarget.out.connectTo(combiner.jumpAddr);
 		sltAdd.out.connectTo(aluAdd.input2);
 		
 		// connect output of combiner
 		combiner.out.connectTo(jumpMux.input1);
 		
 		memToRegMux.output.connectTo(regFile.writeData);
 		regDstMux.output.connectTo(exmem.rd);
 		branchMuxAnd.output.connectTo(branchMux.switcher);
 		branchMux.output.connectTo(jumpMux.input0);
 		jumpMux.output.connectTo(jumpRegMux.input0);
 		jumpRegMux.output.connectTo(pc.pcIn);
 		combiner.out.connectTo(jumpMux.input1);
 		
 		// connect the control signals
 		aluControl.aluControl.connectTo(alu.control);
 		control.regDst.connectTo(idex.regDST);
 		control.jump.connectTo(idex.jump);
 		control.branch.connectTo(idex.branch);
 		control.memRead.connectTo(idex.memRead);
 		control.memToReg.connectTo(idex.memToReg);
 		control.aluOp.connectTo(idex.aluOp);
 		control.memWrite.connectTo(idex.memWrite);
 		control.aluSrc.connectTo(idex.aluSrc);
 		control.regWrite.connectTo(idex.regWrite);
 		control.jumpReg.connectTo(idex.jumpReg);
 		control.branchBNE.connectTo(idex.branchBNE);
 		control.immediate.connectTo(idex.immediate);
 		idex.outregDST.connectTo(regDstMux.switcher);
 		idex.outaluOp.connectTo(aluControl.aluOp);
 		idex.outaluSrc.connectTo(aluSrcMux.switcher);
 		idex.outbranchBNE.connectTo(inv.branchBNE);
 		idex.outjump.connectTo(exmem.jump);
 		idex.outbranch.connectTo(exmem.branch);
 		idex.outmemRead.connectTo(exmem.memRead);
 		idex.outmemToReg.connectTo(exmem.memToReg);
 		idex.outmemWrite.connectTo(exmem.memWrite);
 		idex.outregWrite.connectTo(exmem.regWrite);
 		idex.outjumpReg.connectTo(exmem.jumpReg);
 		idex.outimmediate.connectTo(alu.immediate);
 		idex.outimmediate.connectTo(signExtend.immediate);
 		exmem.outjump.connectTo(jumpMux.switcher);
 		exmem.outjumpReg.connectTo(jumpRegMux.switcher);
 		exmem.outbranch.connectTo(branchMuxAnd.input0);
 		exmem.outmemWrite.connectTo(memoryIo.memWrite);
 		exmem.outmemRead.connectTo(memoryIo.memRead);
 		exmem.outmemToReg.connectTo(memwb.memToReg);
 		exmem.outregWrite.connectTo(memwb.regWrite);
 		exmem.outRd.connectTo(memwb.rd);
 		memwb.outmemToReg.connectTo(memToRegMux.switcher);
 		memwb.outregWrite.connectTo(regFile.regWrite);
 		
 		// connect the outputs of the pipeline registers
 		ifid.outInstr.connectTo(decode.instruction);
 		ifid.outPC4.connectTo(idex.PC4);
 		idex.outPC4.connectTo(aluAdd.input1);
 		idex.outReadData1.connectTo(jumpRegMux.input1);
 		idex.outReadData2.connectTo(exmem.readData2);
 		idex.outSignExtended.connectTo(sltAdd.in);
 		idex.outSignExtended.connectTo(aluSrcMux.input1);
 		idex.outRt.connectTo(regDstMux.input0);
 		idex.outRd.connectTo(regDstMux.input1);
 		idex.outFunct.connectTo(aluControl.func);
 		exmem.outAddALUresult.connectTo(branchMux.input1);
 		exmem.outGenALUresult.connectTo(memoryIo.address);
 		exmem.outGenALUresult.connectTo(memwb.genALUresult);
 		exmem.outReadData2.connectTo(memoryIo.writeData);
 		exmem.outZero.connectTo(inv.in);
 		memwb.outGenALUresult.connectTo(memToRegMux.input0);
 		memwb.outReadData.connectTo(memToRegMux.input1);
 		memwb.outRd.connectTo(regFile.writeReg);
 		
 		// connect I/O of forwarding unit
 		idex.outRs.connectTo(forwardingUnit.idex_rs);
 		idex.outRt.connectTo(forwardingUnit.idex_rt);
 		exmem.outRd.connectTo(forwardingUnit.exmem_rd);
 		memwb.outRd.connectTo(forwardingUnit.memwb_rd);
 		exmem.outregWrite.connectTo(forwardingUnit.exmem_regWrite);
 		memwb.outregWrite.connectTo(forwardingUnit.memwb_regWrite);
 		forwardingUnit.forwardA.connectTo(forwardAMux.switcher);
 		forwardingUnit.forwardB.connectTo(forwardBMux.switcher);
 		
 		// connect I/O of forwarding MUXes
		exmem.outGenALUresult.connectTo(forwardAMux.input2);
 		exmem.outGenALUresult.connectTo(forwardBMux.input2);
 		memToRegMux.output.connectTo(forwardAMux.input1);
 		memToRegMux.output.connectTo(forwardBMux.input1);
 		idex.outReadData1.connectTo(forwardAMux.input0);
 		idex.outReadData2.connectTo(forwardBMux.input0);
 		forwardAMux.output.connectTo(alu.input1);
 		forwardBMux.output.connectTo(aluSrcMux.input0);
		forwardBMux.output.connectTo(exmem.readData2);
 		
 	
 		pc.pcIn.setValue(new BinaryNum("1000000000000").pad(32));
 		int cycleCount = 0;
 		for(;;){
 			try{
 				
 				// increase the cycle count
 				cycleCount ++;
 
 				// send PC to fetch object
 				pc.clockEdge();
 				
 				// fetch the instruction
 				fetch.clockEdge();
 				
 				// clock the P4 ALU (increment the PC)
 				aluP4.clockEdge();
 				
 				// clock IF/ID
 				ifid.clockEdge();
 				
 				// decode the instruction
 				decode.clockEdge();
 				if(decode.isHalt()){
 					// terminate when we see a "HLT" instruction
 					break;
 				}
 				
 				// clock the sign-extend
 				signExtend.clockEdge();
 				
 				
 				// clock the regfile
 				// we do this before clocking the control
 				// because if regWrite ends up being a 1, we won't have
 				// write data until this cycle is finished. So we clock 
 				// regFile first in order to handle the read registers
 				regFile.clockEdge();
 				
 				// clock the SLT
 				sltTarget.clockEdge();
 				
 				// set the output control signals
 				control.setSignals();
 				
 				// clock ID/EX
 				idex.clockEdge();
 				
 				// clock slt Add
 				sltAdd.clockEdge();
 
 				// clock the Add ALU
 				aluAdd.clockEdge();
 				
 				// set ALU control signals
 				aluControl.update();
 								
 				
 				// clock the RegDST Mux
 				regDstMux.clockEdge();
 				
				// clock the forwarding unit
				forwardingUnit.clockEdge();
				
				// clock the Forwarding Muxes;
				forwardAMux.clockEdge();
				forwardBMux.clockEdge();
				
 				// clock the ALUSrc Mux
 				aluSrcMux.clockEdge();
 				
 				// clock the ALU
 				alu.clockEdge();
 				
 				exmem.clockEdge();
 				
 				// clock the Inverter
 				inv.clockEdge();
 				
 				// clock the AND
 				branchMuxAnd.clockEdge();
 				
 				// clock the Memory IO
 				memoryIo.clockEdge();
 				
 				memwb.clockEdge();
 				
 				// clock the combiner
 				combiner.clockEdge();
 				
 				// clock the Branch Mux
 				branchMux.clockEdge();
 	
 				// clock the memToRegMux
 				memToRegMux.clockEdge();
 	
 				// clock the jump Mux
 				jumpMux.clockEdge();
 				
 				// clock the jump register Mux
 				jumpRegMux.clockEdge();
 				
 				// clock the regFile
 				regFile.clockEdge();
 				
 				// Add this cycle to the debug stream
 				debug.debugCycle(cycleCount);
 				pipelineDebug.debugCycle(cycleCount);
 				
 				/*
 				if(cycleCount >= 100){
 					break;
 				}
 				*/
 			}
 			
 			catch (Exception e){
 				e.printStackTrace();
 				System.out.println("Error occured at Cycle: " + cycleCount);
 				try{
 					debug.debugCycle(cycleCount);
 					pipelineDebug.debugCycle(cycleCount);
 				}catch (Exception f){
 					break;
 				}
 				break;
 			}
 		
 				
 
 		}
 	
 		debug.dump("debug.txt");
 		pipelineDebug.dump("pipelineDebug.txt");
 		int instrCount = cycleCount;
 		System.out.println("Execution Complete!\n");
 		System.out.println("The total number of cycles: " + cycleCount);
 		System.out.println("The total number of instructions executed: " + instrCount);
 		System.out.println("CPI: " + (new Float(cycleCount/instrCount)));
 		System.out.println("Final Register Values:");
 /*		String output = "Register\tBinary\t\t\t  Hexadecimal\tDecimal\n";
 		for(int i = 0 ; i < 32 ; i++){
 			Long thisVal = regFile.getVal(i);
 			//print out register values in binary
 			output+= "$r" + i +":\t" + BinaryUtil.pad(Long.toBinaryString(thisVal), 32)
 			+ "\t0x" + Long.toHexString(thisVal) + "\t" + thisVal + "\n";
 		}
 		System.out.println(output);*/
 	}
 
 }
