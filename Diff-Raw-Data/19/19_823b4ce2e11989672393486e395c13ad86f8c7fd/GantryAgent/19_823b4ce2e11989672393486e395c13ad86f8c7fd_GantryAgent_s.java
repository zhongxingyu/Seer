                                          
 package agents;
 
//
 import agents.*;
 import agents.include.*;
 import agents.interfaces.*;
 import java.util.*;
 import java.util.concurrent.Semaphore; 
 
 public class GantryAgent extends Agent implements Gantry {
 
     /*** Data Structures **/
 
     List<Feeder> feeders;
     PartInventory inventory; // location of the bins
     Map<String, Integer> requested; // requested parts
     List<Part> parts;
 
     ProcessState processState;
     enum ProcessState { toProcess, none }
 
     /*** Constructor **/
 
     
     //@Steven- needs editing
     private class PartInventory {
     	List<Part> parts;
     	
     	public PartInventory(List<Part> parts){
     		this.parts = parts;
     		
     	}
     }
     
     
     
     
     public GantryAgent(List<Feeder> feeders, PartInventory inventory) {
 
         this.feeders = feeders;
         this.inventory = inventory;
         requested = new HashMap<String,Integer>();  
         processState = ProcessState.none;  
     }
     
 
     
     
     /*** Messages ***/
 
     /*  Message to ask Gantry to get parts!
      *  Source: Feeder
      */ 
     public void msgRequestParts(String type, int count) {
 
         int total = count;
         if(requested.containsKey(type))
         {
             total += requested.get(type);
         }
         requested.put(type, total);
 
         processState = ProcessState.toProcess;
     }
 
     /*** Scheduler ***/
     
     protected boolean pickAndExecuteAnAction(){
 	    if(processState == ProcessState.toProcess)
 	    {
 	        getParts();
 	        return true;
 	    }
 	    return false;
     }
 
     
     /*** Actions ***/
 
     // getting parts from part inventory
     void getParts()
     {
 //        for(String key : requested.keySet()) {
 //            int requestNum = requested.get(key);
 //
 //            if(requestNum > 0)
 //            {
 //                List<Part> partsFromInventory = getPartsFromInventory(key, requestNum);
 //                int count = requestNum - partsFromInventory.size();
 //                requested.put(type,count);
 //                if(count < 0)
 //                {
 //                    requested.remove(type);
 //                }
 //
 //                for(Feeder f : feeders)
 //                {
 //                    if(f.topPartType.equals(key) || f.botPartType.equals(key))
 //                    {
 //                        f.msgHereAreParts(key, partsFromInventory);
 //                        break;
 //                    }
 //                }
 //                break;
 //            }
 //        }
 //
 //        processState = ProcessState.none;
 //        for(String key : requested.keySet()) {
 //            int requestNum = requested.get(key);
 //            if (requestNum > 0)
 //            {
 //                processState = ProcessState.toProcess;
 //            }
 //         }
     }
 
 //    List<Part> getPartsFromInventory(String partType, int requestNum) {
 //        // return a list with the parts from inventory
 //        DoGetPartsFromInventory();
 //        return partInventory.partsFromInventory(partType, requestNum); 
 //    }
     
     
     
     
 //    private List<Part> getPartsFromInventory(String key, int requestNum) {
 //		// TODO Auto-generated method stub
 //		return null;
 //	}
 
 
 
 
 	//added in 
     void DoGetPartsFromInventory(String key, int requestNum){
     	
     	
     	
     }
     
 }
 	
