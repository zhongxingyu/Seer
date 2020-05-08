 package submit;
 
 // some useful things to import. add any additional imports you need.
 import joeq.Compiler.Quad.*;
 import flow.Flow;
 import java.util.*;
 
 /**
  * Skeleton class for implementing the Flow.Solver interface.
  */
 public class MySolver implements Flow.Solver {
 
 	protected Flow.Analysis analysis;
 
 	/**
 	 * Sets the analysis.  When visitCFG is called, it will
 	 * perform this analysis on a given CFG.
 	 *
 	 * @param analyzer The analysis to run
 	 */
 	public void registerAnalysis(Flow.Analysis analyzer) {
 		this.analysis = analyzer;
 	}
 
 	/**
 	 * Runs the solver over a given control flow graph.  Prior
 	 * to calling this, an analysis must be registered using
 	 * registerAnalysis
 	 *
 	 * @param cfg The control flow graph to analyze.
 	 */
 	public void visitCFG(ControlFlowGraph cfg) {
 
 		// this needs to come first.
 		analysis.preprocess(cfg);
 
 		/***********************
 		 * Your code goes here *
 		 ***********************/
 
 		boolean changed = true;
 		while(changed){
 			changed = singlePass(cfg);
 		}
 		
 
 		// this needs to come last.
 		analysis.postprocess(cfg);
 	}
 	
 	/**
 	 * Runs a single pass on the CFG -JC
 	 * @param cfg
 	 * @return true if any values of the cfg changed during the run
 	 */
 	private boolean singlePass(ControlFlowGraph cfg){
 		boolean changed = false;
 		if(analysis.isForward()){
 			QuadIterator qit = new QuadIterator(cfg);
 			//QuadIterator doesn't return exit - we need to check while iterating whether curQuad has null as a successor
 			List<Quad> exitPreds = new ArrayList<Quad>();
 			while(qit.hasNext()){
 				// get all predecessors 
 				Quad curQuad = qit.next(); 
 				Iterator<Quad> preds = qit.predecessors();
 				Flow.DataflowObject newIn = meetAll(preds, true);
 				if(!newIn.equals(analysis.getIn(curQuad))){
 					analysis.setIn(curQuad, newIn); // in[quad] = meet(all predecessors)
 					changed = true;
 				}
 				
 				// check if we are a predecessor of exit
 				Collection<Quad> allSuccessors = qit.successors1();
 				if(allSuccessors.contains(null)){
 					exitPreds.add(curQuad);
 				}
 				
 				analysis.processQuad(curQuad); //perform transfer function
 			}
 			
 			//process the exit
 			Flow.DataflowObject newIn = meetAll(exitPreds.iterator(), true);
 			if(!newIn.equals(analysis.getExit())){
 				analysis.setExit(newIn);
				//changed = false; //XXX changed = true;
 			}
 		} else {
 			QuadIterator qit = new QuadIterator(cfg,false);
 			List<Quad> entrySuccs = new ArrayList<Quad>();
 			while(qit.hasPrevious()){
 				Quad curQuad = qit.previous();
 				Iterator<Quad> succ = qit.successors();
 				Flow.DataflowObject newOut = meetAll(succ, false);
 				if(!newOut.equals(analysis.getOut(curQuad))){
 					analysis.setOut(curQuad, newOut);
 					changed = true;
 				}
 				
 				// check if we are a successor of entry
 				Collection<Quad> allPredecessors = qit.predecessors1();
 				if(allPredecessors.contains(null)){
 					entrySuccs.add(curQuad);
 				}
 				
 				analysis.processQuad(curQuad);
 			}
 			
 			//process entry
 			Flow.DataflowObject newOut = meetAll(entrySuccs.iterator(), false);
 			if(!newOut.equals(analysis.getEntry())){
 				analysis.setEntry(newOut);
				//changed = false; //XXX changed = true;
 			}
 		}
 		
 		return changed;
 	}
 	
 	/**
 	 * Applies the meet operator to all predecessors and returns the new DataflowObject
 	 * for the result. Written as if forward (new in = meet(predecessors)) but works
 	 * same for backward (new out = meet(successors)) except for the in/out entry/exit -JC
 	 * @param preds
 	 * @param forward - forward or backward
 	 * @return a new DataflowObject result for meeting all
 	 */
 	private Flow.DataflowObject meetAll(Iterator<Quad> preds, boolean forward){
 		Flow.DataflowObject newIn = analysis.newTempVar();
 		while (preds.hasNext()){
 			Quad curPred = preds.next();
 			if(curPred != null){
 				if(forward){
 					newIn.meetWith(analysis.getOut(curPred)); // meet all predecessors
 				} else {
 					newIn.meetWith(analysis.getIn(curPred)); // backward - meet the in's of successors
 				}
 			} else {
 				if(forward){
 					newIn.meetWith(analysis.getEntry());
 				} else {
 					newIn.meetWith(analysis.getExit()); //backward - meet the exit 
 				}
 			}
 		}
 		return newIn;
 	}
 	
 	
 		
 		
 }
