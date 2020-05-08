 package engine.agent.Alex;
 
 import java.util.ArrayList;
 import java.util.concurrent.Semaphore;
 
 import shared.Glass;
 import transducer.TChannel;
 import transducer.TEvent;
import transducer.Transducer;
 import engine.agent.Agent;
 import engine.interfaces.ConveyorFamily;
 
 public class Operator extends Agent{
 
 	public Operator(String name, TChannel tc, int workStationNum){
 		super(name);
 		popup = new Semaphore(0);
 		machined = new Semaphore(0);
 		mychannel = tc;
 		workstation_number = workStationNum ;
 		glasses = new ArrayList<MyGlass>();
 	}
 	/*
 	 * DATA
 	*****/
 	
 	int workstation_number;
 	ConveyorFamily cf;
 	ArrayList<MyGlass> glasses; //should only have one piece, but just in case
 	class MyGlass{
 		public Glass glass;
 		public boolean beenLoaded = false;
 		public boolean beenMachined = false;
 		MyGlass(Glass g){glass = g;}
 	}
 	Semaphore popup;
 	Semaphore machined;
 	/*
 	 * Messages
 	 *
 	 */
	private Transducer transducer;
 	private TChannel mychannel;
 	
 	public void msgHereIsGlass(Glass g){
 
 		glasses.add(new MyGlass(g));
 		stateChanged();
 	}
 	public void msgIAmFree(){
 		popup.release();
 	}
 	//from transducer
 	private void msgDoneMachining(){
 		
 		machined.release();
 	}
 	//from tr
 	public void msgLoadFinished(){
 		print("Load finished");
 		glasses.get(0).beenLoaded = true;
 	}
 	private void msgReleaseFinished() {
 		// TODO Auto-generated method stub
 		
 		
 	}
 	/*
 	 * Scheduler
 	 */
 
 
 
 	@Override
 	public boolean pickAndExecuteAnAction() {
 		// TODO Auto-generated method stub
 		if (!glasses.isEmpty() && !glasses.get(0).beenLoaded)
 		{
 			loadGlass();
 			return true;
 		}
 		if (!glasses.isEmpty() && !glasses.get(0).beenMachined && glasses.get(0).beenLoaded)
 		{
 			machineGlass(glasses.get(0));
 			return true;
 		}
 		return false;
 	}
 
 
 	@Override
 	public void eventFired(TChannel channel, TEvent event, Object[] args) {
 		// TODO Auto-generated method stub
 		if (event == TEvent.WORKSTATION_GUI_ACTION_FINISHED && (int)args[0] == workstation_number)
 			msgDoneMachining();
 		else if (event == TEvent.WORKSTATION_LOAD_FINISHED && (int)args[0] == workstation_number)
 			msgLoadFinished();
 		else if (event == TEvent.WORKSTATION_RELEASE_FINISHED && (int)args[0] == workstation_number)
 			msgReleaseFinished();
 		
 	}
 	/*
 	Actions
 	*/
 	
 	private void loadGlass() {
 		// TODO Auto-generated method stub
		Integer[] args = new Integer[1];
 		args[0] = workstation_number;
 		transducer.fireEvent(mychannel, TEvent.WORKSTATION_DO_LOAD_GLASS, args);
 	}
 	
 	private void machineGlass(MyGlass myGlass){
 		print("Machining glass piece " + myGlass.glass.getNumber());
		Integer[] args = new Integer[1];
 		args[0] = workstation_number;
 		transducer.fireEvent(mychannel, TEvent.WORKSTATION_DO_ACTION, args);
 		try {
 			machined.acquire();
 		} catch (InterruptedException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		print("Done machining glass piece" + myGlass.glass.getNumber());
 		cf.msgIHaveGlassFinished(this);
 		try {
 			popup.acquire();
 		} catch (InterruptedException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		Glass g = glasses.remove(0).glass;
 		cf.msgHereIsFinishedGlass(this,g);
 		print("Releasing glass");
 		transducer.fireEvent(mychannel, TEvent.WORKSTATION_RELEASE_GLASS, args);
 		print("Released glass piece to popup, glass #" + myGlass.glass.getNumber());
 	}
 
 	public void print(String n){
 	System.out.println(name + ": " + n);
 	}
	
	public void setTransducer(Transducer t) {
		transducer = t;
		t.register(this, mychannel);
	}
 }
