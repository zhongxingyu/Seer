 package agents;
 import agents.*;
 import agents.include.*;
 import agents.interfaces.*;
 import gui.*;
 
 import java.util.*;
 import java.util.concurrent.Semaphore;
 
 import state.*;
 
 public class ConveyorAgent extends Agent implements Conveyor {
 
     /*** Data Structures **/
 
 	public List<MyKit> kits;
 	KitRobot kitRobot;
     FCS fcs;
     public int numRequested = 0;
     private GUI_Conveyor guiConveyor;
     private FactoryState factory;
     
     String name;
 
     public enum KitState { none, requested, complete }
     
     private Semaphore kitLock = new Semaphore(0);
 
     public class MyKit {
 
         public Kit kit;
         public KitState kitState;
         
         public MyKit(Kit kit, boolean isNew) {   
             this.kit = kit;
             if(isNew) this.kitState = KitState.none;
             else this.kitState = KitState.complete;
         }
     }
 
     /*** Constructor **/
         
     //constructor 1
     public ConveyorAgent(String name, FCS f, FactoryState factoryState) {
     	this.name = name;
     	kits = Collections.synchronizedList( new ArrayList<MyKit>() );
         this.fcs = f;
         this.factory = factoryState;
         //guiConveyor = factoryState.guiConveyor;
     }
     
     //constructor 2 partA unit testing
     public ConveyorAgent(String name, KitRobot r, FCS f) {
     	this.name = name;
     	kits = Collections.synchronizedList( new ArrayList<MyKit>() );
         this.fcs = f;
         this.kitRobot = r;
         //guiConveyor = new GUI_Conveyor();
     }
 
     /*** Messages **/
 
     /*  Message to place empty kit onto Conveyor
      *  Source: FCS
      */ 
 	public void msgHeresKit(Kit emptyKit) {
 		synchronized (kits){
 		print("received new empty kit from fcs agent");
 		kits.add(new MyKit(emptyKit,true));
 		if (guiConveyor != null)
 		guiConveyor.addKit(emptyKit);
         stateChanged();
 		}
 	}
 
     /*  Message to give kits to KitRobot
      *  Source: KitRobot
      */ 
     synchronized public void msgGiveMeKits(int num) {
         numRequested = num;
         print("KitRobot wants " + num + " kits from conveyor");
         stateChanged();
     }
 
     /*  Message to move kit back to conveyer 
      *  Source: KitRobot
      */ 
 	public void msgHereIsCompleteKit(Kit completeKit) {
 		kits.add(new MyKit(completeKit,false));
 		if (factory != null)
 		factory.DoFinishedOrder();
 		print("received complete kit");
         stateChanged();
 	}
 
     /*** Scheduler ***/
     public boolean pickAndExecuteAnAction() {
 	
         // now to give kits to KitRobot
         if(numRequested > 0)
         {
             giveKits();
             return true;
         }
         
         // request KitRobot to move kits
         synchronized (kits){
         for (MyKit k: kits) {
             if(k.kitState == KitState.none) {
                 requestMoveKit(k);
                 return true;
             }
         }} 
 
         synchronized (kits){
         for (MyKit k: kits) {
             if(k.kitState == KitState.complete) {
                 moveKitOut(k);
                 return true;
             }
         }}
         
         return false;
         
     }
 
     /*** Actions ***/
 
     /* Tell KitRobot that Conveyor wants to move empty kit
      */
     private void requestMoveKit(MyKit k){
         k.kitState = KitState.requested;
 		kitRobot.msgRequestMoveKit();
         stateChanged();
 	}
 
 
     /* Give KitRobot empty kits it has desired
      */
     private void giveKits()
     {
     	int num = 0;
     	if (numRequested > 1){
    		num = 2;
     		numRequested -= 2;
     	}
     	else if ( kits.size() > 1 ){
     		num = 2;
     		numRequested -= 1;
     		for (MyKit k : kits){
     			if (k.kitState == KitState.none){
     				k.kitState = KitState.requested;
         			break;
     			}
     		}
     		print("Had only requested 1 kit, but there are 2 available to take now, so will take 2.");
     	}
     	else{
     		num = numRequested;
     		numRequested--;
     	}
         List<Kit> send = new ArrayList<Kit>();
         List<MyKit> remove = new ArrayList<MyKit>();
        
         for (MyKit k : kits) {
         	//LOOK comment below and it works
         	if (num-1 < 0 ) break;
         	if (k.kitState == KitState.requested) {
         		send.add(k.kit);
         		remove.add(k);
         	}
         }
         kits.removeAll(remove);
         kitRobot.msgHeresKits(send);
         
         if (guiConveyor != null){
         	do{ try {	       
 		        DoAddKitToConveyor();
 		        kitLock.acquire();
 		        }catch (InterruptedException e) {
 		        	print("Semaphare Error");
 		        }
 		        //stateChanged();
         	}while(--num > 0);
         }
     }
 
     private void moveKitOut(MyKit k) {
     	if (guiConveyor != null)
     	DoMoveKitOut();
         fcs.msgCompletedKit(k.kit);
     	kits.remove(k);
         print("moving complete kit out");
         stateChanged();
     }
     
     //EXTRA    
     public void setKitRobot(KitRobot k){
     	kitRobot = k;
     }    
     public void setFCS(FCS f){
     	fcs = f;
     }    
     public void setConveyorGUI(GUI_Conveyor c){
     	guiConveyor = c;
     }
     public GUI_Conveyor getGUIConveyor(){
     	return guiConveyor;
     }    
     public String getName(){ return name; }
     
     public void provideNextKit(){
     	print("kitLock being released");
     	kitLock.release();
     }
     
     //GUI
     private void DoAddKitToConveyor(){
     	print("guiconveyor showing kit on conveyor");
     	guiConveyor.DoAddKitToConveyor(this);
     }
     private void DoMoveKitOut(){
     	//guiKitRobot.DoPlaceKitOnConyeyor(k)
     	//guiConveyor.DoGetKitOut(k)
     }
 
 }
