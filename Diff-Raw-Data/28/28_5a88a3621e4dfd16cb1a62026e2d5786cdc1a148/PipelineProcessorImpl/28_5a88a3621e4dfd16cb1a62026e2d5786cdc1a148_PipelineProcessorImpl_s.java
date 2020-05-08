 package edu.dlsu.mips.processor.impl;
 
 import java.util.Queue;
 
 import edu.dlsu.mips.domain.Instruction;
 import edu.dlsu.mips.domain.InstructionSet;
 import edu.dlsu.mips.domain.MIPSRegisters;
 import edu.dlsu.mips.domain.PipelineProcess;
 import edu.dlsu.mips.domain.PipelineStage;
 import edu.dlsu.mips.domain.ProcessStatus;
 import edu.dlsu.mips.exception.JumpAddressException;
 import edu.dlsu.mips.exception.MemoryAddressOverFlowException;
 import edu.dlsu.mips.exception.OpcodeNotSupportedException;
 import edu.dlsu.mips.exception.OperandException;
 import edu.dlsu.mips.exception.RegisterAddressOverFlowException;
 import edu.dlsu.mips.exception.StorageInitializationException;
 import edu.dlsu.mips.exception.TrapException;
 import edu.dlsu.mips.processor.PipelineProcessor;
 import edu.dlsu.mips.util.BitStringUtils;
 import edu.dlsu.mips.util.InstructionRunner;
 import edu.dlsu.mips.util.InstructionSetBuilder;
 import edu.dlsu.mips.util.Storage;
 import edu.dlsu.mips.util.SystemUtils;
 
 public class PipelineProcessorImpl implements PipelineProcessor {
 
 	private static final String SET = "1";
 	private static final String NOT_SET = "0";
 	private static final String LD_OPCODE_OFFSET = "8";
	private Queue<Instruction> instructions;
 
 	@Override
 	public ProcessStatus processInstruction(Instruction instruction)
 			throws JumpAddressException, OperandException, StorageInitializationException, RegisterAddressOverFlowException,
 			TrapException, OpcodeNotSupportedException, MemoryAddressOverFlowException {
 		if (!"".equals(instruction.getInstruction())) {
 			instructions.add(instruction);
 		}
 		return runMIPSCycle();
 	}
 	
 	private ProcessStatus runMIPSCycle() throws JumpAddressException, OperandException, OpcodeNotSupportedException, StorageInitializationException,
 			RegisterAddressOverFlowException, MemoryAddressOverFlowException, TrapException {
 		incrementSystemClock();
 		processWB();
 		processMem();
 		if (SystemUtils.isPCChanged()) {
 			SystemUtils.setPCChanged(false);
 			return ProcessStatus.JUMP;
 		}
 		processExe();
 		if (SystemUtils.hasDataHazard()) {
 			SystemUtils.setDataHazard(false);
 			return ProcessStatus.HAZARD;
 		}
 		processID();
 		processIF();
 		if (SystemUtils.hasActiveProcess()) {
 			return ProcessStatus.ONGOING;
 		}
 		return ProcessStatus.END;
 	}
 
 	private void incrementSystemClock() {
 		SystemUtils.clockCycle++;
 	}
 
 	private void processWB() throws StorageInitializationException, RegisterAddressOverFlowException {
 		PipelineProcess wbProcess = SystemUtils.retrieveActiveProcess(PipelineStage.WB);
 		if (null != wbProcess) {
 			InstructionSet instructionSet = wbProcess.getInstructionSet();
 			String opcode = instructionSet.getOpcode();
 			if (opcode.equals("LD")) {
 				Storage.setRegisterContents(instructionSet.getRt(), MIPSRegisters.MEMWBLMD);
 			} else if (isALUInstruction(opcode)) {
 				Storage.setRegisterContents(instructionSet.getRd(), MIPSRegisters.MEMWBALUOUTPUT);
 				if (SystemUtils.dataHazardStackContains(instructionSet.getRd())) {
 					SystemUtils.removeDataHazard(instructionSet.getRd());
 				}
 			}
 			wbProcess.logProcessClocking();
 			SystemUtils.removeFromActiveProcess(PipelineStage.WB);
 		}
 	}
 	
 	private boolean isALUInstruction(String opcode) {
 		return opcode.equals("DADD") || opcode.equals("DSUB")
 				|| opcode.equals("DADDI") || opcode.equals("AND")
 				|| opcode.equals("XOR") || opcode.equals("SLT");
 	}
 	
 	private void processMem() throws StorageInitializationException, MemoryAddressOverFlowException {
 		PipelineProcess memProcess = SystemUtils.retrieveActiveProcess(PipelineStage.MEM);
 		if (null != memProcess) {
 			InstructionSet instructionSet = memProcess.getInstructionSet();
 			String opcode = instructionSet.getOpcode();
 			if (opcode.equals("LD")) {
 				MIPSRegisters.MEMWBIR = MIPSRegisters.EXMEMIR;
 				MIPSRegisters.MEMWBLMD = Storage.getMemoryContents(MIPSRegisters.EXMEMALUOUTPUT, LD_OPCODE_OFFSET);
 			} else if (opcode.equals("SD")) {
 				MIPSRegisters.MEMWBIR = MIPSRegisters.EXMEMIR;
 				Storage.setMemoryContents(MIPSRegisters.EXMEMALUOUTPUT, MIPSRegisters.EXMEMB);
 			} else if (isALUInstruction(opcode)) {
 				MIPSRegisters.MEMWBIR = MIPSRegisters.EXMEMIR;
 				MIPSRegisters.MEMWBALUOUTPUT = MIPSRegisters.EXMEMALUOUTPUT;
 			} else if (opcode.equals("BNEZ") || opcode.equals("J")) {
 				if (MIPSRegisters.EXMEMCOND.equals(SET)) {
 					MIPSRegisters.PC = MIPSRegisters.EXMEMALUOUTPUT;
 					SystemUtils.removeFromActiveProcess(PipelineStage.EXE);
 					SystemUtils.removeFromActiveProcess(PipelineStage.ID);
 					SystemUtils.setPCChanged(true);
 					SystemUtils.setTargetLine(instructionSet.getTargetLine());
 				}
 			}
 			memProcess.logProcessClocking();
 			memProcess.incrementStage();
 		}
 	}
 	
 	private void processExe() throws StorageInitializationException,
 			RegisterAddressOverFlowException, TrapException {
 		PipelineProcess exeProcess = SystemUtils.retrieveActiveProcess(PipelineStage.EXE);
 		if (null != exeProcess) {
 			InstructionSet instructionSet = exeProcess.getInstructionSet();
 			if (hasDataHazard(instructionSet)) {
 				SystemUtils.removeFromActiveProcess(PipelineStage.EXE);
 				SystemUtils.removeFromActiveProcess(PipelineStage.ID);
 				SystemUtils.setDataHazard(true);
 				SystemUtils.setTargetLine(instructionSet.getInstructionLine());
 				exeProcess.markHazard();
 				exeProcess.logProcessClocking();
 			} else {
 				String opcode = instructionSet.getOpcode();
 				if (isALUInstruction(opcode)) {
 					SystemUtils.addDataHazard(instructionSet.getRd());
 				}
 				if (opcode.equals("DADD")) {
 					MIPSRegisters.EXMEMIR = MIPSRegisters.IDEXIR;
 					MIPSRegisters.EXMEMALUOUTPUT = InstructionRunner.DADD(instructionSet.getRs(), instructionSet.getRt());
 					MIPSRegisters.EXMEMCOND = NOT_SET;
 				} else if (opcode.equals("DSUB")) {
 					MIPSRegisters.EXMEMIR = MIPSRegisters.IDEXIR;
 					MIPSRegisters.EXMEMALUOUTPUT = InstructionRunner.DSUB(instructionSet.getRs(), instructionSet.getRt());
 					MIPSRegisters.EXMEMCOND = NOT_SET;
 				} else if (opcode.equals("XOR")) {
 					MIPSRegisters.EXMEMIR = MIPSRegisters.IDEXIR;
 					MIPSRegisters.EXMEMALUOUTPUT = InstructionRunner.XOR(instructionSet.getRs(), instructionSet.getRt());
 					MIPSRegisters.EXMEMCOND = NOT_SET;
 				} else if (opcode.equals("SLT")) {
 					MIPSRegisters.EXMEMIR = MIPSRegisters.IDEXIR;
 					MIPSRegisters.EXMEMALUOUTPUT = InstructionRunner.SLT(instructionSet.getRs(), instructionSet.getRt());
 					MIPSRegisters.EXMEMCOND = NOT_SET;
 				} else if (opcode.equals("AND")) {
 					MIPSRegisters.EXMEMIR = MIPSRegisters.IDEXIR;
 					MIPSRegisters.EXMEMALUOUTPUT = InstructionRunner.AND(instructionSet.getRs(), instructionSet.getRt());
 					MIPSRegisters.EXMEMCOND = NOT_SET;
 				} else if (opcode.equals("SD")) {
 					MIPSRegisters.EXMEMIR = MIPSRegisters.IDEXIR;
 					MIPSRegisters.EXMEMB = MIPSRegisters.IDEXB;
 					MIPSRegisters.EXMEMCOND = NOT_SET;
 				} else if (opcode.equals("LD")) {
 					MIPSRegisters.EXMEMIR = MIPSRegisters.IDEXIR;
 					Integer aluOutput = Integer.parseInt(MIPSRegisters.IDEXA, 2) + Integer.parseInt(MIPSRegisters.IDEXIMM, 2);
 					MIPSRegisters.EXMEMALUOUTPUT = Integer.toBinaryString(aluOutput);
 				} else if (opcode.equals("DADDI")) {
 					MIPSRegisters.EXMEMIR = MIPSRegisters.IDEXIR;
 					MIPSRegisters.EXMEMALUOUTPUT = InstructionRunner.DADDI(instructionSet.getRs(), instructionSet.getImm());
 					MIPSRegisters.EXMEMCOND = NOT_SET;
 				} else if (opcode.equals("BNEZ")) {
 					Integer aluOutput = Integer.parseInt(MIPSRegisters.IDEXNPC, 2) + Integer.parseInt(MIPSRegisters.IDEXIMM, 2);
 					MIPSRegisters.EXMEMALUOUTPUT = Integer.toBinaryString(aluOutput);
 					MIPSRegisters.EXMEMCOND = Integer.parseInt(MIPSRegisters.IDEXA, 2) != 0? SET : NOT_SET;
 				} else if (opcode.equals("J")) {
 					MIPSRegisters.EXMEMALUOUTPUT = retrieveJumpInstruction(instructionSet);
 					MIPSRegisters.EXMEMCOND = SET;
 				}
 				exeProcess.logProcessClocking();
 				exeProcess.incrementStage();
 			}
 		}
 	}
 	
 	private boolean hasDataHazard(InstructionSet instructionSet) {
 		String opcode = instructionSet.getOpcode();
 		if (opcode.equals("DADD") || opcode.equals("DSUB") || opcode.equals("XOR")
 				|| opcode.equals("SLT") || opcode.equals("AND") || opcode.equals("DADDI")) {
 			if (SystemUtils.dataHazardStackContains(instructionSet.getRd())) {
 				return true;
 			}
 		} else if (opcode.equals("BNEZ")) {
 			if (SystemUtils.dataHazardStackContains(instructionSet.getRs())) {
 				return true;
 			}
 		} else if (opcode.equals("SD")) {
 			if (SystemUtils.dataHazardStackContains(instructionSet.getRt())) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	private void processID() {
 		PipelineProcess idProcess = SystemUtils.retrieveActiveProcess(PipelineStage.ID);
 		if (null != idProcess) {
 			MIPSRegisters.IDEXIR = MIPSRegisters.IFIDIR;
 			MIPSRegisters.IDEXA = idProcess.getInstructionSet().getBinaryInstruction().substring(6, 10);
 			MIPSRegisters.IDEXB = idProcess.getInstructionSet().getBinaryInstruction().substring(11, 15);
 			MIPSRegisters.IDEXIMM = idProcess.getInstructionSet().getBinaryInstruction().substring(16, 31);
 			idProcess.logProcessClocking();
 			idProcess.incrementStage();
 		}
 	}
 
 	private void processIF() throws JumpAddressException, OperandException, OpcodeNotSupportedException {
 		Instruction instruction = instructions.poll();
 		if (null != instruction) {
 			PipelineProcess ifProcess = buildPipelineProcess(instruction);
 			SystemUtils.addActiveProcess(ifProcess);
 			SystemUtils.addToAllProcess(ifProcess);
 			MIPSRegisters.IFIDIR = ifProcess.getInstructionSet().getHexInstruction();
 			String nextPC = incrementProgramCounter();
 			MIPSRegisters.IFIDNPC = nextPC;
 			MIPSRegisters.PC = nextPC;
 			ifProcess.logProcessClocking();
 			ifProcess.incrementStage();
 		}
 	}
 
 	private String incrementProgramCounter() {
 		return SystemUtils.incrementPc(MIPSRegisters.PC);
 	}
 
 	private String retrieveJumpInstruction(InstructionSet instructionSet) {
 		String binaryInstruction = instructionSet.getBinaryInstruction().substring(6,31);
 		return BitStringUtils.shiftLeft(binaryInstruction, 2);
 	}
 
 	private PipelineProcess buildPipelineProcess(Instruction instruction)
 			throws JumpAddressException, OperandException, OpcodeNotSupportedException {
 		InstructionSet opcode = InstructionSetBuilder.buildInstructionSet(instruction.getInstruction());
 		return PipelineProcess.newInstance(opcode);
 	}
 
 }
