 package submit;
 
 // some useful things to import. add any additional imports you need.
 import joeq.Compiler.Quad.*;
 import flow.Flow;
 import java.util.ArrayList;
 
 /**
  * Skeleton class for implementing the Flow.Solver interface.
  */
 public class MySolver implements Flow.Solver {
 
     protected Flow.Analysis analysis;
     protected Flow.Optimization optimization;
 
     /**
      * Sets the analysis.  When visitCFG is called, it will
      * perform this analysis on a given CFG.
      *
      * @param analyzer The analysis to run
      */
     public void registerAnalysis(Flow.Analysis analyzer) {
         this.analysis = analyzer;
     }
 
     public void registerOptimization(Flow.Optimization opt) {
         optimization = opt;
     }
 
     /**
      * Runs the solver over a given control flow graph.  Prior
      * to calling this, an analysis must be registered using
      * registerAnalysis
      *
      * @param cfg The control flow graph to analyze.
      */
     public void visitCFG(ControlFlowGraph cfg) {
 
         boolean changed;
         Quad curQuad;
         QuadIterator quadIterator;
         java.util.Iterator<Quad> predecessorIterator;
         java.util.Iterator<Quad> successorIterator;
         Flow.DataflowObject meetOfPredecessors;
         Flow.DataflowObject meetOfSuccessors;
         Flow.DataflowObject oldOutSet;
         Flow.DataflowObject oldInSet;
         Flow.DataflowObject topDataflowObject;
         Quad curPredecessor; 
         Quad curSuccessor; 
         ArrayList<Quad> exitPredecessors;
         ArrayList<Quad> entrySuccessors;
 
         changed = true;
         quadIterator = new QuadIterator(cfg);
         exitPredecessors = new ArrayList<Quad>();
         entrySuccessors = new ArrayList<Quad>();
 
         // this needs to come first.
         analysis.preprocess(cfg);
 
         if(analysis.isForward())
         {
             // Creates a list containing all the predecessors of the exit node
             while(quadIterator.hasNext())
             {
                 curQuad = quadIterator.next();
                 successorIterator = quadIterator.successors();
                 while(successorIterator.hasNext())
                 {
                     curSuccessor = successorIterator.next();
                     if(curSuccessor == null)
                     {
                         exitPredecessors.add(curQuad);
                         break;
                     }
                 }
             }
         }
         else // analysis.isBackward
         {
             // Creates a list containing all the successors of the entry node
             // note, there will be only one entry in the list, but still using list to be
             // consistent with method for exitPredecessors
             while(quadIterator.hasNext())
             {
                 curQuad = quadIterator.next();
                 predecessorIterator = quadIterator.predecessors();
                 while(predecessorIterator.hasNext())
                 {
                     curPredecessor = predecessorIterator.next();
                     if(curPredecessor == null)
                     {
                         entrySuccessors.add(curQuad);
                         break;
                     }
                 }
             }
 
         }
 
         topDataflowObject = analysis.newTempVar();
         topDataflowObject.setToTop();
 
         quadIterator = new QuadIterator(cfg);
 
         // set OUT(B) = Top for all B
         while(quadIterator.hasNext())
         {
             if(analysis.isForward())
                 analysis.setOut(quadIterator.next(), topDataflowObject);
             else
                 analysis.setIn(quadIterator.next(), topDataflowObject);
         }
             
         // 2. while(changes to OUT)
         while(changed)
         {
             changed = false;
 
             quadIterator = new QuadIterator(cfg);
             // 3. foreach(block)
             while(quadIterator.hasNext())
             {
                 curQuad = quadIterator.next();
                 if(analysis.isForward())
                 {
                     predecessorIterator = quadIterator.predecessors();
                     meetOfPredecessors = analysis.newTempVar();
                     meetOfPredecessors.setToTop(); 
                     
                     while(predecessorIterator.hasNext())
                     {   
                         curPredecessor = predecessorIterator.next();
                         if(curPredecessor == null)
                             meetOfPredecessors.meetWith(analysis.getEntry());
                         else
                             meetOfPredecessors.meetWith(analysis.getOut(curPredecessor));
                     }
 
                     analysis.setIn(curQuad, meetOfPredecessors);
 
                     // save value of out set before applying the transfer function to check for changes after applying xfer func
                     oldOutSet = analysis.newTempVar();
                     oldOutSet.copy(analysis.getOut(curQuad)); 
                     
                     analysis.processQuad(curQuad);
 
                     changed = changed || !oldOutSet.equals(analysis.getOut(curQuad));
                 }
                 else
                 {
                     successorIterator = quadIterator.successors();
                     meetOfSuccessors = analysis.newTempVar();
                     meetOfSuccessors.setToTop(); 
                     
                     while(successorIterator.hasNext())
                     {   
                         curSuccessor = successorIterator.next();
                         if(curSuccessor == null)
                             meetOfSuccessors.meetWith(analysis.getExit());
                         else
                             meetOfSuccessors.meetWith(analysis.getIn(curSuccessor));
                     }
 
                     analysis.setOut(curQuad, meetOfSuccessors);
 
                     // save value of out set before applying the transfer function to check for changes after applying xfer func
                     oldInSet = analysis.newTempVar();
                     oldInSet.copy(analysis.getIn(curQuad)); 
                     
                     analysis.processQuad(curQuad);
 
                     changed = changed || !oldInSet.equals(analysis.getIn(curQuad));
                 }
             } // while(quadIterator.hasNext())
             
             if(analysis.isForward())
             {
                 meetOfPredecessors = analysis.newTempVar();
                 meetOfPredecessors.setToTop(); 
                 for(Quad predecessor : exitPredecessors)
                 {
                     if(predecessor == null)
                         meetOfPredecessors.meetWith(analysis.getEntry());
                      else
                         meetOfPredecessors.meetWith(analysis.getOut(predecessor));
                 }
 
                 analysis.setExit(meetOfPredecessors);
             }
             else
             {
                 meetOfSuccessors = analysis.newTempVar();
                 meetOfSuccessors.setToTop(); 
                 for(Quad successor : entrySuccessors)
                 {
                     if(successor == null)
                         meetOfSuccessors.meetWith(analysis.getExit());
                      else
                         meetOfSuccessors.meetWith(analysis.getIn(successor));
                 }
 
                 analysis.setEntry(meetOfSuccessors);
             }
 
         } // while(changed)
 
         // this needs to come last.
         analysis.postprocess(cfg);
 
         // run optimizer -- class that removes redundant null checks
        if(optimization != null) optimization.optimize(cfg);
     }
 }
