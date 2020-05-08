 package edu.dlsu.mips.domain;
 
 import java.util.Map;
 
 import edu.dlsu.mips.util.SystemUtils;
 
 public class PipelineProcess {
 
 	private PipelineStage pipelineStage;
 	private InstructionSet instructionSet;
	private Map<Integer, String> processClocking;
 
 	private PipelineProcess(InstructionSet instructionSet) {
 		this.pipelineStage = PipelineStage.IF;
 		this.instructionSet = instructionSet;
 	}
 
 	public static PipelineProcess newInstance(InstructionSet opcode) {
 		return new PipelineProcess(opcode);
 	}
 	
 	public void incrementStage() {
 		if (pipelineStage.equals(PipelineStage.IF)) {
 			pipelineStage = PipelineStage.ID;
 		} else if (pipelineStage.equals(PipelineStage.ID)) {
 			pipelineStage = PipelineStage.EXE;
 		} else if (pipelineStage.equals(PipelineStage.EXE)) {
 			pipelineStage = PipelineStage.MEM;
 		} else if (pipelineStage.equals(PipelineStage.MEM)) {
 			pipelineStage = PipelineStage.WB;
 		}
 	}
 	
 	public void logProcessClocking() {
 		processClocking.put(SystemUtils.clockCycle, pipelineStage.name());
 	}
 	
 	public void markHazard() {
 		pipelineStage = PipelineStage.STALL;
 	}
 
 	public InstructionSet getInstructionSet() {
 		return instructionSet;
 	}
 
 	public void setInstructionSet(InstructionSet instructionSet) {
 		this.instructionSet = instructionSet;
 	}
 
 	public PipelineStage getPipelineStage() {
 		return pipelineStage;
 	}
 
 }
