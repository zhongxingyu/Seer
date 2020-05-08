 package agents;
 import agents.*;
 import agents.include.*;
 import agents.interfaces.*;
 import java.util.*;
 import state.*;
 import java.util.concurrent.Semaphore; 
 
 public class PartRobotAgent extends Agent implements PartRobot {
 
     /*** Data Structures **/
     private Factory_PartB factoryState; // v0
     private boolean needSecond;
     private int completeKits;
 
     private List<Map<String, Integer>> configs;
     private List<Part> received;
     private KitStand kitStand;
 
 
     private enum PartRobotState { inactive, startOrder, waitingRequests,        none,  pickUpParts };
     PartRobotState state;
     private List<NestAgent> nests;
 
     /*** Constructor **/
 
     public PartRobotAgent(List<NestAgent> nests, KitStand kitStand, Factory_PartB factoryState) {
         this.nests = nests;
         this.kitStand = kitStand;
         this.factoryState = factoryState;
         this.needSecond = false;
         this.completeKits = 0;
         this.received = new ArrayList<Part>();
         state = PartRobotState.inactive;
 
     }
 
     /*** Messages ***/
 
     /*  Message to ask PartsRobot to create a new kit
      *  Source: KitRobot
      */
     public void msgMakeKits() {
 
         configs = kitStand.getPartConfig();
         state = PartRobotState.startOrder;
         stateChanged();
     }
 
     public void msgHereAreParts(List<Part> sendParts)
     {
         received.addAll(sendParts);
         stateChanged();
     }
 
     /*** Scheduler ***/
 
     protected boolean pickAndExecuteAnAction() 
     {
         if(state == PartRobotState.startOrder)
         {
             requestParts();
             state = PartRobotState.waitingRequests;
             return true;
         }
 
         if(state == PartRobotState.waitingRequests)
         {
             if(received.size() > 0)
             {
                 pickUpParts();
                 return true;
             }
             return true;
         }
 
         return false;
     }
 
     /*** Actions **/
 
     private void requestParts() {
 
         Map<String, Integer> use = configs.remove(0);
 
         if(configs.size() > 0)
         {
             Map<String, Integer> total = new HashMap<String, Integer>();
 
             for(String key : configs.get(0).keySet())
             {
                 int val = configs.get(0).get(key);
                 if(use.containsKey(key))
                 {
                     val += use.get(key);
                 }
                 total.put(key, val);
             }
             if(total.keySet().size()<=8) 
             {
                 configs.remove(0);
                 use = total;
             }
             else needSecond = true;
         }
 
         List<String> needLane = new ArrayList<String>();
         List<Nest> needNests = new ArrayList<Nest>();
         for(Nest n : nests) needNests.add(n);
 
         // go through use and assign to nests
         for(String key : use.keySet())
         {
             boolean need = true;
             for(Nest n : nests)
             {
                 if(n.getPartType().equals(key))
                 {
                     n.msgRequestParts(key, use.get(key));
                     needNests.remove(n);
                     need = false;
                     break;
                 }
             }
             if(need) needLane.add(key);
         }
 
         for(String n : needLane)
         {
             needNests.remove(0).msgRequestParts(n, use.get(n));
         }
 
         stateChanged();
     }
 
     private void pickUpParts()
     {
 
         for(int i=0; i<nests.size(); i++)
         {
             if(nests.get(i).getPartType() != null)
             {
                 System.out.println(i + " / " + nests.get(i).getPartType());
             }
 
         }
         
        System.out.println("A" + received.size());

         List<Part> partsToKit = new ArrayList<Part>();
         int j = 0;
         for(int i = 0; i< received.size(); i++)
         {
            partsToKit.add(received.get(i));
             j++;
             if(j>=4) break;             
         }
        
        for(int i=0; i<j; i++)
        {
        	received.remove(0);
        }
        
         /*
         Map<Part, Integer> partsSend = kitStand.insertPartsIntoKits(partsToKit);
 
         DoPickUpPartsFromNest(partsSend);
 
         int kitCompleteKits = kitStand.completeKits();
         if(kitCompleteKits != completeKits)
         {
             completeKits = kitCompleteKits;
             //kitRobot.msgKitIsDone()
 
             if(needSecond)
             {
                 needSecond = false;
                 requestParts();
                 state = PartRobotState.waitingRequests;
             }
             else if(kitCompleteKits == 2)
             {
                 state = PartRobotState.inactive;
                 // done for now
                 completeKits = 0;
                 received = new ArrayList<Part>();
             }
         } */
 
         
         // v0 stub
         Map<Part, Integer> partsSend = new HashMap<Part, Integer>();
         for(Part p : partsToKit)
         {
             partsSend.put(p, 1);
         }

        System.out.println("B" + partsToKit.size());
        System.out.println("C" + partsSend.keySet().size());

         DoPickUpPartsFromNest(partsSend);
         
         stateChanged();
 
     }
 
     public Semaphore verify = new Semaphore(0);
     private void DoPickUpPartsFromNest(Map<Part, Integer> partsSend)
     {
         factoryState.doPickUpParts(partsSend);
         try {
                   verify.acquire();
   
         }
         catch(Exception ex) { }
         // probably need semaphore here.
 
 
     }
 
     public void DoPickedUpParts()
     {
         verify.release();
     }
 }
