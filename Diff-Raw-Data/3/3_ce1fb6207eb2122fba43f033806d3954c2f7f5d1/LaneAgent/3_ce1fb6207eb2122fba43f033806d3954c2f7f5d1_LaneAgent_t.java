 package agents;
 import agents.*;
 import agents.include.*;
 import agents.interfaces.*;
 import java.util.*;
 
 public class LaneAgent extends Agent implements Lane {
 
     /*** Data Structures **/
 	
 	//testing
     
     int needed;
     String partType; // should be same as corresponding Nest
     List<Part> parts; // parts on Lane
     Nest nest;
     Feeder feeder;
 
     ProcessState processState;
     enum ProcessState { toProcess, none }
 
     /*** Constructor **/
     
     public LaneAgent(String partType, Nest nest, Feeder feeder) {
         this.needed = 0;
         this.partType = partType;
         this.parts = new ArrayList<Part>();
         this.nest = nest;
         this.feeder = feeder;
         this.processState = ProcessState.none;
     }
 
     /*** Messages ***/
 
     /*  Message to ask LaneAgent to get parts!
      *  Source: Nest
      */
    public void msgRequestParts(String partType, int count) {
    	//TODO: Hold and use partType
         needed += count;
         processState = ProcessState.toProcess;
         stateChanged();
 
     }
 
     /*  Message to Lane w/ a Part!
      *  Source: Feeder
      */
     public void msgHereIsPart(Part p) {
         parts.add(p);
         stateChanged();
     }
 
     /*** Scheduler ***/
 
     protected boolean pickAndExecuteAnAction() {
 
         // when we received a part
         if(parts.size() > 0)
         {
             sendPartToNest();
             return true;
 
         }
 
         // when we have a request to process
         if(processState == ProcessState.toProcess)
         {
             requestParts();
             processState = ProcessState.none;
             return true;
         }
         return false;
     }
 
 
     /*** Actions ***/
 
     private void requestParts()
     {
         feeder.msgRequestParts(partType,needed);
         needed = 0;
         stateChanged();
 
     }
 
     private void sendPartToNest()
     {
         // some delay.... w/ the animation
         DoPutPartOnNest(parts.get(0));
         nest.msgHereIsPart(parts.remove(0));
         stateChanged();
 
     }
 
     private void DoPutPartOnNest(Part p) { }
 
 }
