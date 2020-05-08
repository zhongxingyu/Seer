 package edu.illinois.reLooper.sabazios;
 
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Set;
 
 import com.ibm.wala.analysis.pointers.HeapGraph;
 import com.ibm.wala.ipa.callgraph.CGNode;
 import com.ibm.wala.ipa.callgraph.CallGraph;
 import com.ibm.wala.ipa.callgraph.propagation.AllocationSiteInNode;
 import com.ibm.wala.ipa.callgraph.propagation.LocalPointerKey;
 import com.ibm.wala.ipa.callgraph.propagation.PointerAnalysis;
 import com.ibm.wala.ipa.slicer.NormalStatement;
 import com.ibm.wala.ipa.slicer.Slicer;
 import com.ibm.wala.ipa.slicer.Statement;
 import com.ibm.wala.ipa.slicer.StatementWithInstructionIndex;
 import com.ibm.wala.ipa.slicer.Slicer.ControlDependenceOptions;
 import com.ibm.wala.ipa.slicer.Slicer.DataDependenceOptions;
import com.ibm.wala.ssa.IR;
 import com.ibm.wala.ssa.SSAInstruction;
 import com.ibm.wala.ssa.SSANewInstruction;
 import com.ibm.wala.ssa.SSAPutInstruction;
 import com.ibm.wala.util.CancelException;
 
 import edu.illinois.reLooper.sabazios.race.Race;
 import edu.illinois.reLooper.sabazios.race.RaceOnNonStatic;
 
 public class RaceFinder {
 
 	private final boolean DEBUG = false;
 	private final PointerAnalysis pointerAnalysis;
 	private final CallGraph callGraph;
 	private HeapGraph heapGraph;
 	private final Analysis analysis;
 
 	public RaceFinder(Analysis analysis) {
 		this.analysis = analysis;
 		this.pointerAnalysis = analysis.pointerAnalysis;
 		this.callGraph = analysis.callGraph;
 		this.heapGraph = pointerAnalysis.getHeapGraph();
 	}
 
 	public Set<Race> findRaces() throws CancelException {
 		HashSet<Race> races = new HashSet<Race>();
 
 		for (CGNode node : callGraph)
			if (node.getContext() instanceof SmartContextSelector.InsideParOpContext) {
				IR ir = node.getIR();
				if(ir == null)
					continue;
				for (SSAInstruction instruction : ir.getControlFlowGraph().getInstructions()) {
 					Race race = getRace(node, instruction);
 					if(race != null)
 						races.add(race);
 				}
			}
 		return races;
 	}
 
 	private Race getRace(CGNode node, SSAInstruction instruction) {
 		//TODO: we only check put instructions for now. should also include array, etc. in the future
 		if (instruction instanceof SSAPutInstruction) {
 			SSAPutInstruction putI = (SSAPutInstruction) instruction;
 
 			if (DEBUG)
 				System.out.println(instruction);
 			
 
 			// if it is not static, we do the more involved check
 			if (!putI.isStatic()) {
 				int ref = putI.getRef();
 
 				Set<AllocationSiteInNode> allocSites = Analysis.getOutsideAllocationSites(
 						node, ref);
 
 				// report race if it still stands
 				if (!allocSites.isEmpty()) {
 					return new RaceOnNonStatic(new NormalStatement(node, CodeLocation.getSSAInstructionNo(node, instruction)), allocSites.iterator().next(), false);
 				}
 
 			} else {
 				// TODO: replace false with the true value of
 				// isLoopCarriedDependency
 				return new RaceOnNonStatic(new NormalStatement(node, CodeLocation.getSSAInstructionNo(node, instruction)), null, false);				
 			}
 		}
 		return null;
 	}
 }
