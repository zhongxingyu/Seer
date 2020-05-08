 package it.wolfed.operation;
 
 import it.wolfed.model.PetriNetGraph;
 import it.wolfed.model.PlaceVertex;
 import it.wolfed.model.TransitionVertex;
 import it.wolfed.model.Vertex;
 
 /**
  * Sequencing Operation.
  */
 public class ParallelismOperation extends Operation
 {
     PetriNetGraph firstGraph;
     PetriNetGraph secondGraph;
     
     /**
      * @param operationGraph
      * @param firstGraph
      * @param secondGraph
      * @throws Exception  
      */
     public ParallelismOperation(PetriNetGraph operationGraph, PetriNetGraph firstGraph, PetriNetGraph secondGraph) throws Exception
     {
         super(operationGraph);
         this.firstGraph = getIfIsWorkFlow(firstGraph);
         this.secondGraph = getIfIsWorkFlow(secondGraph);
         this.operationGraph = (new MergeGraphsOperation(operationGraph, firstGraph, secondGraph)).getOperationGraph();
         execute();
     }
    
     /**
      * Parallelism.
      * 
      * FistGraph:
      *
      *  N1_P1 ◎ → N1_T1 ❒ → N1_P2 ◯
      * 
      * -------------------------------
      * 
      * SecondGraph:
      * 
      *  N2_P1 ◎ → N2_T1 ❒ → N2_P2 ◯
      * 
      * -------------------------------
      * 
      * ResultGraph:
      * 
      *               N1_P1 ◯ → N1_T1 ❒ → N1_P2 ◯
      *                    ↗                       ↘
      * P* ◎ → and-split ❒                and-join ❒ → P* ◯
      *                    ↘                       ↗
      *               N2_P1 ◯ → N2_T1 ❒ → N2_P2 ◯
      */
     @Override
     void process()
     {
         insertInitialPattern();
         insertFinalPattern();
     }
     
     /**
      * Insert final pattern.
      * 
      *                 N1_P1 ◯
      *                      ↗                       
      * P* ◎ → and-split ❒
      *                      ↘ 
      *                  N2_P1 ◯ 
      */
     
     private void insertInitialPattern()
     {
         PlaceVertex pi = getOperationGraph().insertPlace(null);
         TransitionVertex andSplit = getOperationGraph().insertTransition("and-split");
 
         PlaceVertex initialPlaceAsFirst =  (PlaceVertex) getEquivalentVertex(1, firstGraph.getInitialPlaces().get(0));
         PlaceVertex initialPlaceAsSecond = (PlaceVertex)getEquivalentVertex(2, secondGraph.getInitialPlaces().get(0));
 
         getOperationGraph().insertArc(null, pi, andSplit);
         getOperationGraph().insertArc(null, andSplit, initialPlaceAsFirst);
         getOperationGraph().insertArc(null, andSplit, initialPlaceAsSecond);
         
         // Sets tokens
         pi.setTokens(1);
         initialPlaceAsFirst.setTokens(0);
         initialPlaceAsSecond.setTokens(0);
     }
     
     /**
      * Insert initial pattern.
      * 
      *  N1_P2 ◯
      *          ↘
      *  and-join ❒ → P* ◯
      *          ↗
      *  N2_P2 ◯
      */
     private void insertFinalPattern()
     {
         PlaceVertex po = getOperationGraph().insertPlace(null);
         TransitionVertex andJoin = getOperationGraph().insertTransition("and-join");
 
         Vertex finalPlaceAsFirst = getEquivalentVertex(1, firstGraph.getFinalPlaces().get(0));
         Vertex finalPlaceAsSecond = getEquivalentVertex(2, secondGraph.getFinalPlaces().get(0));
 
         getOperationGraph().insertArc(null, andJoin, po);
         getOperationGraph().insertArc(null, finalPlaceAsFirst, andJoin);
         getOperationGraph().insertArc(null, finalPlaceAsSecond, andJoin);
     }
 }
