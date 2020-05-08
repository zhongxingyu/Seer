 //package MARF.LRTA;
 
 import agentsproject.Packet;
 import jade.core.*;
 import java.awt.*;
 import java.util.*;
 
 public class ProblemSolver2
     extends Agent
 {
   private Point LocalTarget;
   private LRTAAlgo LA;
   private int LocalTargetPos;
   private boolean isWaiting;
   private String state;
   private HashMap<String, Double> Q;
   private Packet myPacket;
   public String name;
   
   protected void setup()
   {
     Point T;
     DBLogger dblogger;
     int RunNo;
     myPacket = null;
     
     LA = new LRTAAlgo();
     /********************************************/
     /***** Getting Passed Arguments *************/
     Object[] args = getArguments();
     T = (Point) args[0];
 
     LA.MyCurrentPos.x = T.x;
     LA.MyCurrentPos.y = T.y;
 
     LA.TargetObj = (Target) args[1];
     LA.MyObj = (MyComponent) args[2];
     LA.myGui = (AgentGUI) args[3];
     String s = (String) args[4];
     LA.MyStateInfo = (StatesInfo) args[5];
     LA.CurrentPos = Integer.parseInt(s);
     LA.IVObj = (InputVariables) args[6];
     dblogger = (DBLogger) args[7];
     RunNo = Integer.parseInt( (String) args[8]);
     LA.LC = Integer.parseInt( (String) args[9]);
     isWaiting = false;
     name = this.getAID().getName();
     name = name.substring(0, name.indexOf("@"));
     /********************************************/
     /******* Calculating Start Time *************/
     Date STime = new Date();
     System.out.println("Start Time " + STime);
     /********************************************/
     MyTarget();
     
     String name = this.getAID().getName();
     name = name.substring(0, name.indexOf("@"));
     
    //variables for Q Learning
     double r;
     double alpha = 0.25;
     state = "Free";
     
     //Initialize Q (the state-action policy)
     initializeQ();
     int cycles = 0;
     while (LA.MyStateInfo.PacketPos.size() > 0 || myPacket != null)
     {
         String pastState;
         System.out.println(name+" At: "+LA.MyCurrentPos.x+","+LA.MyCurrentPos.y);
         
         //get available actions at current state
        ArrayList<String> list = getActions();
        
        //From avaialable actions, choose one with highest value of Q
        String selectedAction = chooseAction(list);
        
        pastState = state;
        
        //perform selected action, and record reward
        r = performAction(selectedAction);
        
        //update Q
        String key = pastState+":"+selectedAction;
        double value = Q.get(pastState+":"+selectedAction)+alpha*(r+getMaxQ()- Q.get(pastState+":"+selectedAction)   );
        Q.put(key, value);
        
        try
       {
         LA.myGui.RePaintWindow();
        doWait(20);
       }
       catch (Exception ex)
       {
 
       }
        cycles++;
        LA.MyStateInfo.Count++;
     }
     System.out.println(name+" worked for "+cycles+" cycles");
     LA.MyStateInfo.AgentsDone++;
   }
 
   private void MyTarget()
   {
       System.out.println("in MyTarget()");
     int MinDistance = 999;
     int Distance;
     boolean CaughtTargets = true;
     LocalTargetPos = 0;
     Point LT;
     for (int i = 0; i < LA.TargetObj.TargetPositions.size(); i++)
     {
       LT = (Point) LA.TargetObj.TargetPositions.get(i);
       if(LA.TargetObj.GetTP(LT.x,LT.y)==0)
       {
         CaughtTargets = false;
         
         //TODO: make a better Distance Function
         Distance = Math.abs(LT.x - LA.MyCurrentPos.x) +
             Math.abs(LT.y - LA.MyCurrentPos.y);
         if (MinDistance > Distance)
         {
           LocalTargetPos = i;
           MinDistance = Distance;
         }
       }
     }
     System.out.println("Agent "+getLocalName()+"Index"+LocalTargetPos);
     LocalTarget = new Point();
     if(!CaughtTargets)
     {
       LocalTarget = (Point) LA.TargetObj.TargetPositions.get(LocalTargetPos);
     }
     else
     {
       LocalTarget.x = 0;
       LocalTarget.y = 0;
     }
   }
   
   //gets list of actions based on current state
   private ArrayList<String> getActions()
   {
       ArrayList<String> ans = new ArrayList<>();
       if(!state.equalsIgnoreCase("Free"))
       {
           ans.add("Continue");
       }
       if(state.equalsIgnoreCase("Free"))
       {
           ans.add("ChoosePacket");
       }
       
       //if someone needs help
       if((state.equalsIgnoreCase("Free") || state.equalsIgnoreCase("WaitingForHelp") || state.equalsIgnoreCase("MovingToFreePacket")) && LA.MyStateInfo.checkHelpCall())
       {
           Point temp = LA.MyStateInfo.peekHelpCall();
           
           //if the helpcall wasn't issued for the same packet which we are holding
           if( ! ( temp.x == LA.MyCurrentPos.x && temp.y == LA.MyCurrentPos.y )) 
           {
                 ans.add("RespondToHelpCall");
           }
       }
       return ans;
   }
   
   private String chooseAction(ArrayList<String> list)
   {
       String ans = null;
       double max = -100;
       
       //choose action with highest value of Q
       for(String str : list )
       {
          System.out.println(name+" "+state+":"+str+" "+Q.get(state+":"+str));
           if(Q.get(state+":"+str) > max)
           {
               max = Q.get(state+":"+str);
               ans = str;
           }
       }
       return ans;
   }
   
   private double performAction(String action)
   {
       System.out.println(name+" "+state+":"+action);
       
       //Default reward (negative reward to discourage time wasting)
       double reward = -0.1;
       
       if(action.equalsIgnoreCase("ChoosePacket"))
       {
           if(state.equalsIgnoreCase("WaitingForHelp"))
           {
               //drop existing packet
               myPacket.drop();
               myPacket = null;
           }
           System.out.println(name+" Choosing new packet");
           LA.TargetPosition = LA.MyStateInfo.ChoosePacket(LA.MyCurrentPos);
           LA.ClearH(); 
           
           //update state
           state = "MovingToFreePacket";
       }
       
       if(action.equalsIgnoreCase("RespondToHelpCall"))
       {
           if(state.equalsIgnoreCase("WaitingForHelp"))
           {
               myPacket.drop();
               myPacket = null;
           }
           Point help = LA.MyStateInfo.getHelpCall();
           if(help != null)
           {
               System.out.println(name+" Responding to call");
               LA.TargetPosition = help;
               LA.ClearH();
           }
           
           //update state
           state = "RespondingToHelpCall";
           
           //give reward for helping someone (encourage helping)
           reward = 1;
       }
       
       if(state.equalsIgnoreCase("WaitingForHelp"))
       {
           //if light enough now,
           if(myPacket.getWeight() == 0)
             {
                  //pick it up
                 LA.MyStateInfo.removePacket(LA.TargetPosition);
                 System.out.println(name+" Picked up packet for "+myPacket.getDestination().x+","+myPacket.getDestination().y+")");
                 LA.TargetPosition = myPacket.getDestination();
                 LA.ClearH();
                 AgentContainer ac = LA.MyObj.MyPoints.get(LA.CurrentPos);
                 ac.state = 1;
                 LA.MyObj.MyPoints.set(LA.CurrentPos, ac);
                 
                 //update state
                 state = "MovingToDestination";
                 
                 //give reward for picking up packet
                 reward = 1;
             }
       }
       
       if(action.equalsIgnoreCase("Continue") && !state.equalsIgnoreCase("WaitingForHelp"))
       {
           if(state.equalsIgnoreCase("MovingToFreePacket") || state.equalsIgnoreCase("RespondingToHelpCall"))
           {
               
               //if current target packet has been picked up by some other agent
             if(LA.MyStateInfo.getPacket(LA.TargetPosition) == null)
             {
                   System.out.println(name+" Choosing new packet");
                   LA.TargetPosition = LA.MyStateInfo.ChoosePacket(LA.MyCurrentPos);
                   LA.ClearH();
             }
           }
           
           //if reached target
           if (LA.MyCurrentPos.x == LA.TargetPosition.x && LA.MyCurrentPos.y == LA.TargetPosition.y)
             {
               System.out.println(name+" Reached at ("+LA.TargetPosition.x+","+LA.TargetPosition.y+")");
 
 
               //if empty handed
               if(myPacket == null)
               {
                   myPacket = LA.MyStateInfo.getPacket(LA.TargetPosition);
 
                   if(myPacket == null)
                   {
                      state =  "Free"; 
                   }
                   else
                   {
 
                     //Try to pick up
                     myPacket.lift();
 
                     //Check if still too heavy
                     if(myPacket.getWeight() > 0)
                     {
                         System.out.println(name+" Calling for help");
                         LA.MyStateInfo.callForHelp(LA.MyCurrentPos, myPacket.getWeight() );
                         AgentContainer ac = LA.MyObj.MyPoints.get(LA.CurrentPos);
                         ac.state = 0;
                         LA.MyObj.MyPoints.set(LA.CurrentPos, ac);
                         isWaiting = true;
                         
                         //update state
                         state =  "WaitingForHelp";
                     }
                     else //if light enough
                     {
                         //pick it up
                         LA.MyStateInfo.removePacket(LA.TargetPosition);
                         LA.MyStateInfo.cancelHelpCall(LA.MyCurrentPos);
                         System.out.println(name+" Picked up packet for "+myPacket.getDestination().x+","+myPacket.getDestination().y+")");
                         LA.TargetPosition = myPacket.getDestination();
                         LA.ClearH();
                         AgentContainer ac = LA.MyObj.MyPoints.get(LA.CurrentPos);
                         ac.state = 1;
                         LA.MyObj.MyPoints.set(LA.CurrentPos, ac);
                         
                         //update state
                         state = "MovingToDestination";
                         
                         //reward for picking up packet
                         reward = 1;
                     }
                   }
               }
               else
               {
                   System.out.println(name+" Delivered");
                   myPacket = null;
                   LA.TargetPosition = LA.MyStateInfo.ChoosePacket(LA.MyCurrentPos);
                   LA.ClearH();
                   AgentContainer ac = LA.MyObj.MyPoints.get(LA.CurrentPos);
                   ac.state = 0;
                   LA.MyObj.MyPoints.set(LA.CurrentPos, ac);
                   state = "Free";
                   
                   //high reward for delivering packet
                   reward = 10;
               }
 
 
             }
           else
           {
                LA.RunLRTA();
           }
       }
       
       return reward;
   }
   
   private void initializeQ()
   {
       Q = new HashMap<>();
       
     /*  Q.put("Free:ChoosePacket", 1.0);
       Q.put("Free:RespondToHelpCall", 2.0);
       Q.put("MovingToFreePacket:Continue", 1.0);
       Q.put("MovingToFreePacket:RespondToHelpCall", 2.0);
       Q.put("MovingToDestination:Continue", 1.0);
       Q.put("RespondingToHelpCall:Continue", 1.0);
       Q.put("WaitingForHelp:Continue", 2.0);
       Q.put("WaitingForHelp:ChoosePacket", 1.0);
       Q.put("WaitingForHelp:RespondToHelpCall", 1.5);
       */
       
       Q.put("Free:ChoosePacket", 1.0);
       Q.put("Free:RespondToHelpCall", 1.0);
       Q.put("MovingToFreePacket:Continue", 1.0);
       Q.put("MovingToFreePacket:RespondToHelpCall", 1.0);
       Q.put("MovingToDestination:Continue", 1.0);
       Q.put("RespondingToHelpCall:Continue", 1.0);
       Q.put("WaitingForHelp:Continue", 1.0);
       Q.put("WaitingForHelp:ChoosePacket", 1.0);
       Q.put("WaitingForHelp:RespondToHelpCall", 1.0);
       
   }
   
   private double getMaxQ()
   {
       ArrayList<String> list = getActions();
       String ans = null;
       double max = -100;
       for(String str : list )
       {
          // System.out.println(name+" "+state+":"+str+" "+Q.get(state+":"+str));
           if(Q.get(state+":"+str) > max)
           {
               max = Q.get(state+":"+str);
               ans = str;
           }
       }
       return Q.get(state+":"+ans);
   }
 }
