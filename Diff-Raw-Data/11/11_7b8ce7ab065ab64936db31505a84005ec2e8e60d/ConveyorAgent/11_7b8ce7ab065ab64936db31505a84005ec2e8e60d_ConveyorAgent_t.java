 package glassLine.agents;
 
 import glassLine.Glass;
 import glassLine.interfaces.Conveyor;
 import glassLine.interfaces.Machine;
 import glassLine.test.EventLog;
 import gui.panels.subcontrolpanels.TracePanel;
 import transducer.TChannel;
 import transducer.TEvent;
 import transducer.Transducer;
 
 import java.util.*;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.concurrent.Semaphore;
 
 /**
  * Created with IntelliJ IDEA.
  * User: devon
  * Date: 3/31/13
  * Time: 12:52 AM
  * To change this template use File | Settings | File Templates.
  */
 public class ConveyorAgent extends Agent implements Conveyor, Machine {
 
     enum GlassState {MID_CONVEYOR, END_CONVEYOR, WAITING_TO_EXIT, EXIT_TO_SENSOR }
 
     private List<MyGlass> glassOnMe;
 
     private boolean moving;
 
     private Machine entryMachine;
 
     private Machine exitMachine;
 
     private String myMachine;
 
     private int myConveyorIndex;
 
     private int myEntrySensorIndex;
 
     private int myExitSensorIndex;
 
     private Transducer transducer;
 
     private Semaphore movingToMachine;
 
     private class MyGlass{
         public Glass glass;
         public GlassState state;
 
         public MyGlass(Glass g){
             this.glass = g;
             this.state = GlassState.MID_CONVEYOR;
         }
 
 
     }
 
     private boolean glassInQueue;
 
     public EventLog log;
 
     public ConveyorAgent(String machine, Transducer t, int index, TracePanel tp){
         super("ConveyorAgent");
 
         tracePanel = tp;
 
         glassOnMe = Collections.synchronizedList(new LinkedList<MyGlass>());
         movingToMachine = new Semaphore(0);
         myMachine = machine;
         moving = false;
         entryMachine = null;
         exitMachine = null;
 
         myConveyorIndex = index;
 
 
         myEntrySensorIndex = myConveyorIndex*2;
         myExitSensorIndex = myConveyorIndex*2 + 1;
 
 
         glassInQueue = false;
         log = new EventLog();
         transducer = t;
 
 
         //Register for channels
 
         transducer.register(this, TChannel.SENSOR);
 
 
     }
 
     public void setTwoMachines(Machine enter, Machine exit){
         entryMachine = enter;
         exitMachine = exit;
     }
 
 
 
     public boolean getQueued(){
 
         return glassInQueue;
 
     }
 
     public boolean getMoving(){
 
         return moving;
     }
 
     public int getNumGlassOnMe(){
 
         return glassOnMe.size();
     }
 
     public int getConveyorIndex(){
 
         return myConveyorIndex;
     }
 
     /*
 
     Messaging
 
      */
 
     public void msgGlassNeedsThrough(){
         print("Conveyor " + myConveyorIndex + " Received message : msgGlassNeedsThrough\n");
 
 
         glassInQueue = true;
         stateChanged();
     }
 
     public void msgGlassIsReady(){
         print("Conveyor " + myConveyorIndex + " Received message : msgGlassIsReady\n");
 
 
         glassInQueue = true;
         stateChanged();
 
     }
 
     public void msgHereIsGlass(Glass g){
         print("Conveyor " + myConveyorIndex + " Received message : msgHereIsGlass\n");
 
         glassInQueue = false;
 
 
         glassOnMe.add(new MyGlass(g));
         stateChanged();
 
     }
 
     public void msgGlassAtEndSensor(Glass g){
         print("Conveyor " + myConveyorIndex + " Received message : msgGlassAtEndSensor\n");
         if(g == null){
         	print("Conveyor " + myConveyorIndex + " Glass at end sensor not recognized.");
         }
         
         stopConveyor();
         
         synchronized(glassOnMe){
 		    for(MyGlass mg : glassOnMe){
 		        if(mg.glass == g){
 		            mg.state = GlassState.END_CONVEYOR;
 		        }
 		    }
         }
         
         stateChanged();
 
 
     }
 
     public void msgReadyToTakeGlass(){
         print("Conveyor " + myConveyorIndex + " Received message : msgReadyToTakeGlass\n");
 
         synchronized(glassOnMe){
 	        for(MyGlass mg : glassOnMe){
 	            if(mg.state == GlassState.WAITING_TO_EXIT){
 	                mg.state = GlassState.EXIT_TO_SENSOR;
 	            }
 	        }
         }
         
         stateChanged();
 
 
     }
 
     /*
 
       Scheduler
 
      */
 
     public boolean pickAndExecuteAnAction(){
     	synchronized(glassOnMe){
 	        for(MyGlass mg : glassOnMe){
 	            if(mg.state == GlassState.WAITING_TO_EXIT){
 	            	return true;
 	            }
 	        }
     	}
     	synchronized(glassOnMe){
 	        for(MyGlass mg : glassOnMe){
 	            if(mg.state == GlassState.EXIT_TO_SENSOR){
 	                moveGlassToMachine(mg);
 	                return true;
 	            }
 	        }
     	}
     	synchronized(glassOnMe){
 	        for(MyGlass mg : glassOnMe){
 	            if(mg.state == GlassState.END_CONVEYOR){
 	                requestMoveGlass(mg);
 	                return true;
 	            }
 	        }
     	}
         if(!moving){
           	synchronized(glassOnMe){
 	            for(MyGlass mg : glassOnMe){
 	                if(mg.state == GlassState.MID_CONVEYOR){
 	                    startConveyor();
 	                    return true;
 	                }
 	            }
           	}
         }
         if(glassInQueue){
             boolean canTakeGlass = true;
           	synchronized(glassOnMe){
 	            for(MyGlass mg : glassOnMe){
 	                if(mg.state == GlassState.WAITING_TO_EXIT){
 	                    canTakeGlass = false;
 	                }
 	            }
           	}
             if(canTakeGlass){
                 prepareToTakeGlass();
             }
         }
 
         return false;
     }
 
     /*
 
     Actions
 
      */
 
     private void moveGlassToMachine(MyGlass g){
         print("Conveyor " + myConveyorIndex + " Carrying out action : moveGlassToMachine\n");
         System.out.println("Carrying out action : moveGlassToMachine");
 
     	startConveyor();
 
         try {
             movingToMachine.acquire();
         } catch (InterruptedException e) {
             e.printStackTrace();
         }
 
 
         exitMachine.msgHereIsGlass(g.glass);
         
         stopConveyor();
 
         glassOnMe.remove(g);
         
         stateChanged();
 
     }
 
     private void requestMoveGlass(MyGlass g){
         print("Conveyor " + myConveyorIndex + " Carrying out action : requestMoveGlass\n");
         System.out.println("Carrying out action : requestMoveGlass");
         
         
         if(!g.glass.getProcesses().contains(myMachine)){
             exitMachine.msgGlassNeedsThrough();
         } else {
             exitMachine.msgGlassIsReady();
         }
 
 
         g.state = GlassState.WAITING_TO_EXIT;
 
     }
 
     private void startConveyor(){
         print("Conveyor " + myConveyorIndex + " Carrying out action : startConveyor");
 
         Object args[] = new Object[1];
         args[0] = myConveyorIndex;
 
         transducer.fireEvent(TChannel.CONVEYOR, TEvent.CONVEYOR_DO_START, args);
         moving = true;
 
 
 
     }
 
     private void prepareToTakeGlass(){
         print("Conveyor " + myConveyorIndex + " Carrying out action : prepareToTakeGlass");
 
         entryMachine.msgReadyToTakeGlass();
 
         glassInQueue = false;
 
     }
     
     private void stopConveyor(){
     	
         print("Conveyor " + myConveyorIndex + " Carrying out action : stopConveyor");
     	
         Object args[] = new Object[1];
         args[0] = myConveyorIndex;
 
         transducer.fireEvent(TChannel.CONVEYOR, TEvent.CONVEYOR_DO_STOP, args);
     	
         moving = false;
         
     }
 
 
     /*
 
     Gui Stuff
 
      */
 
     public void eventFired(TChannel channel, TEvent event, Object[] args){
         if(channel == TChannel.SENSOR){
             if(event == TEvent.SENSOR_GUI_PRESSED)  {
                 if((Integer) args[0] == myExitSensorIndex){
                     Glass g = null;
                     for(MyGlass mg : glassOnMe){
                         if(mg.glass.getID() == (Integer) args[1]){
                            g = mg.glass;
                         }
                     }
                     msgGlassAtEndSensor(g);
                 }
 
             } else if(event == TEvent.SENSOR_GUI_RELEASED){
            	
            	if(myConveyorIndex == 0){
            		System.out.println("Sensor gui released : sensor number " + (Integer) args[0]);
            	}
 
                 if((Integer) args[0] == myExitSensorIndex){
                     movingToMachine.release();
                 }
 
 
             }
         }
     }
 
 }
