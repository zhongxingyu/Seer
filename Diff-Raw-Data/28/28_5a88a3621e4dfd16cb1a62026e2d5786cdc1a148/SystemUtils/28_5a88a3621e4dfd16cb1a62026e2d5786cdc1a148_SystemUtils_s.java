 package edu.dlsu.mips.util;
 
 import java.util.Collection;
 import java.util.HashSet;
 
 import com.google.common.collect.Lists;
 
 import edu.dlsu.mips.domain.PipelineProcess;
 import edu.dlsu.mips.domain.PipelineStage;
 
 public class SystemUtils {
 
 	public static int clockCycle = 0;
	private static Collection<PipelineProcess> activeProcesses;
	private static Collection<PipelineProcess> allProcesses;
	private static HashSet<String> dataHazardStack;
 	private static boolean isPCChanged;
 	private static boolean hasDataHazard;
 	private static int targetLine;
 	
 	public static void addToAllProcess(PipelineProcess pipelineProcess) {
 		allProcesses.add(pipelineProcess);
 	}
 	
 	public static void addActiveProcess(PipelineProcess pipelineProcess) {
 		activeProcesses.add(pipelineProcess);
 	}
 	
 	public static void removeFromActiveProcess(PipelineStage pipelineStage) {
 		Collection<PipelineProcess> newProcesses = Lists.newArrayList();
 		for (PipelineProcess process : activeProcesses) {
 			if (!process.getPipelineStage().equals(pipelineStage)) {
 				newProcesses.add(process);
 			}
 		}
 		activeProcesses = newProcesses;
 	}
 	
 	public static PipelineProcess retrieveActiveProcess(PipelineStage pipelineStage) {
 		for (PipelineProcess process : activeProcesses) {
 			if (process.getPipelineStage().equals(pipelineStage)) {
 				return process;
 			}
 		}
 		return null;
 	}
 	
 	public static boolean hasActiveProcess() {
 		return !activeProcesses.isEmpty();
 	}
 	
 	public static String incrementPc(String currentPc){
 		int pcIntVal = Integer.parseInt(currentPc,2);
 		pcIntVal += 4;
 		return Integer.toBinaryString(pcIntVal);
 	}
 	
 	public static void addDataHazard(String register) {
 		dataHazardStack.add(register);
 	}
 	
 	public static void removeDataHazard(String register) {
 		dataHazardStack.remove(register);
 	}
 	
 	public static boolean dataHazardStackContains(String register) {
 		return dataHazardStack.contains(register);
 	}
 
 	public static Collection<PipelineProcess> getAllProcesses() {
 		return allProcesses;
 	}
 
 	public static boolean isPCChanged() {
 		return isPCChanged;
 	}
 
 	public static void setPCChanged(boolean isPCChanged) {
 		SystemUtils.isPCChanged = isPCChanged;
 	}
 
 	public static boolean hasDataHazard() {
 		return hasDataHazard;
 	}
 
 	public static void setDataHazard(boolean hasDataHazard) {
 		SystemUtils.hasDataHazard = hasDataHazard;
 	}
 
 	public static int getTargetLine() {
 		return targetLine;
 	}
 
 	public static void setTargetLine(int targetLine) {
 		SystemUtils.targetLine = targetLine;
 	}
 
 }
