 package engine.agent.Luis;
 
 import engine.agent.Agent;
 import engine.agent.Luis.Interface.*;
 import engine.interfaces.ConveyorFamily;
 import engine.agent.Alex.*;
 
 import java.util.*;
 import java.util.concurrent.Semaphore;
 
 import shared.Glass;
 import transducer.TChannel;
 import transducer.TEvent;
 import transducer.Transducer;
 
 public class ConveyorAgent_LV extends Agent implements Conveyor_LV, ConveyorFamily{
 	
 	int index;
 	boolean moving;
 	List<Glass> glassPieces;
 	enum PopUpState {OPEN, BUSY};
 	enum SensorState {PRESSED, RELEASED, NULL};
 	enum ConveyorState {NEEDS_BREAK, BROKEN, NEEDS_FIX, FIXED};
 	MyPopUp myPopUp;
 	ConveyorState state;
 	SensorState sensorOne = SensorState.NULL;
 	SensorState sensorTwo = SensorState.NULL;
 	ConveyorFamily previousFamily;
 	Transducer t;
 	private Semaphore popupReady = new Semaphore(0);
 	private ConveyorFamilyAgent_LV parentCF;
 	
 	public class MyPopUp
 	{
 		PopUp_LV popUp;
 		PopUpState state;
 		
 		public MyPopUp(PopUp_LV p)
 		{
 			popUp = p;
 			state = PopUpState.OPEN;
 		}
 	}
 	
 	public ConveyorAgent_LV(String s, int i, ConveyorFamilyAgent_LV parent)
 	{
 		parentCF = parent;
 		name = s;
 		index = i;
 		glassPieces = Collections.synchronizedList(new ArrayList<Glass>());
 		moving = false;
 		sensorOne = SensorState.NULL;
 		sensorOne = SensorState.NULL;
 		state = ConveyorState.FIXED;
 		transducer = parent.t;
 	}
 	
 	/*
 	 * MESSAGES
 	 */
 
 	public void msgHereIsGlass(Glass glass) 
 	{
 		glassPieces.add(glass);
 		stateChanged();
 	}
 	
 	public void msgPopUpBusy()
 	{
 		myPopUp.state = PopUpState.BUSY;
 		stateChanged();
 	}
 	
 	public void msgPopUpFree()
 	{
 		myPopUp.state = PopUpState.OPEN;
 		stateChanged();
 	}
 	
 	/*
 	 * SCHEDULER
 	 */
 
 	public boolean pickAndExecuteAnAction() {
 			
 		if(state == ConveyorState.BROKEN) {
 			breakConveyor();
 			return false;
 		}
 		
 		if(state == ConveyorState.NEEDS_FIX) {
 			startUpConveyor();
 			return true;
 		}
 		
 		if((myPopUp.state == PopUpState.BUSY) && (sensorTwo == SensorState.PRESSED) && (moving))
 		{
 			letPopUpKnowGlassIsWaiting();
 			return true;
 			}
 			
 		if((myPopUp.state == PopUpState.OPEN) && (!moving) && (state != ConveyorState.BROKEN))
 		{
 			startUpConveyor();
 			return true;
 		}
 		
 		if(sensorTwo == SensorState.PRESSED && !(myPopUp.state == PopUpState.BUSY))
 		{
 			moveToPopUp();
 			return true;
 		}
 		
 		if(sensorOne == SensorState.RELEASED)
 		{
 			notifyPreviousFamily();
 			return true;
 		}
 
 		return false;
 	}
 	
 	/*
 	 * ACTIONS
 	 */
 	
 	private void startUpConveyor()
 	{
 		print("starting conveyor");
 		Integer[] args = new Integer[1];
 		args[0] = index;
 		transducer.fireEvent(TChannel.CONVEYOR, TEvent.CONVEYOR_DO_START, args);
 		moving = true; 
 		state = ConveyorState.FIXED;
 	}
 	
 	private void stopConveyor()
 	{
 		print("stopping conveyor");
 		Integer[] args = new Integer[1];
 		args[0] = index;
 		transducer.fireEvent(TChannel.CONVEYOR, TEvent.CONVEYOR_DO_STOP, args);
 		moving = false; 
 	}
 	
 	private void letPopUpKnowGlassIsWaiting()
 	{
 		stopConveyor();
 		print("Letting popup know glass is waiting");
 		myPopUp.popUp.msgIHaveGlassReady(glassPieces.get(0).getRecipe(parentCF.popup.channel));
 	}
 	
 	private void moveToPopUp()
 	{
 		print("Giving glass to popUp");
 		myPopUp.popUp.msgHereIsGlass(glassPieces.remove(0));
 		sensorTwo = SensorState.NULL;
 		myPopUp.state = PopUpState.BUSY;
 		
 	}
 	
 	private void notifyPreviousFamily()
 	{
 		print("Letting previous Family know I am free");
 		previousFamily.msgIAmFree();
 		sensorOne = SensorState.NULL;
 	}
 	
 	public void breakConveyor() {
 		moving = false;
 
 	}
 
 	public void eventFired(TChannel channel, TEvent event, Object[] args) {
 		
 		if(channel == TChannel.SENSOR)
 		{
 			
 			if(event == TEvent.SENSOR_GUI_PRESSED)
 			{
 				if((Integer)(args[0]) == index*2)
 					sensorOne = SensorState.PRESSED;
 				else if((Integer)(args[0]) == index*2 + 1)
 					sensorTwo = SensorState.PRESSED;
 			}
 			else if(event == TEvent.SENSOR_GUI_RELEASED)
 			{
 				if((Integer)(args[0]) == index*2)
 					sensorOne = SensorState.RELEASED;
 				else if((Integer)(args[0]) == index*2+1)
 					sensorTwo = SensorState.RELEASED;
 			}
 		}
 		if((Integer) (args[0]) == index ) {
 			if(event == TEvent.CONVEYOR_BROKEN) {
 				setBroken(true);
 				print("Event received, broken");
 
 				
 			} else if (event == TEvent.CONVEYOR_FIXED) {
 				print("Event received, fixed");
 
 				setBroken(false);
 				
 			}
 			
 		}
 		
 		stateChanged();
 		
 	}
 
 	@Override
 	public void msgHereIsFinishedGlass(Operator operator, Glass glass) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void msgIHaveGlassFinished(Operator operator) {
 		// TODO Auto-generated method stub
 		
 	}
 	
 	public void setInteractions(ConveyorFamily cf, PopUp_LV popUp, Transducer trans)
 	{
 		previousFamily = cf;
 		myPopUp = new MyPopUp(popUp);
 		transducer = trans;
 		trans.register(this, TChannel.SENSOR);
 		transducer.register(this, TChannel.CONVEYOR);
 	}
 	
 	public PopUp_LV getPopUp()
 	{
 		return myPopUp.popUp;
 	}
 	
 	public void setPopUp(PopUp_LV p)
 	{
 		myPopUp.popUp = p;
 	}
 	
 	public void setTransducer(Transducer trans)
 	{
 		transducer = trans;
 		transducer.register(this, TChannel.SENSOR);
 		transducer.register(this, TChannel.CONVEYOR);
 	}
 	
 	public ConveyorFamily getPrevious()
 	{
 		return previousFamily;
 	}
 	
 	public int getGlassPiecesSize()
 	{
 		return glassPieces.size();
 	}
 	
 	public void setBroken(boolean s)
 	{
 		if(s)
 			state = ConveyorState.BROKEN;
 		else
 			state = ConveyorState.NEEDS_FIX;
 		stateChanged();
 	}
 
 	public void setInteractions(ConveyorFamily c2, PopUpAgent_LV popup) {
 		previousFamily = c2;
 		setPopUp(popup);
 	}
 
 
 	public void setNextConveyorFamily(ConveyorFamily c3) {
 	
 	}
 
 	public void setPreviousConveyorFamily(ConveyorFamily c2) {
 		previousFamily = c2;
 	}
 
 	public void setConveyorBroken(boolean s, int conveyorno) {
 		setBroken(s);
 	}
 
 	public void setInlineBroken(boolean s, TChannel channel) {
 		print("no inline to break");
 	}
 
 	public void startThreads() {
 		this.startThread();
 	}
 
 	@Override
 	public void msgIAmFree() {
 		// TODO Auto-generated method stub
 		msgPopUpFree();
 	}
 
 
 
 
 
 
 	
}
