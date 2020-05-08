 package engine.agent.Luis;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import java.util.concurrent.Semaphore;
 
 import engine.interfaces.*;
 import shared.Glass;
 import transducer.TChannel;
 import transducer.TEvent;
 import transducer.Transducer;
 import engine.agent.Agent;
 import engine.agent.Alex.*;
 import engine.agent.Luis.*;
 import engine.agent.Luis.Interface.*;
 
 public class PopUpAgent_LV extends Agent implements PopUp_LV{
 
 	int index;
 	TChannel work;
 	boolean nextComponentFree;
 	Status status;
 	PopUpState state;
 	List<Machine> operators;
 	
 	Conveyor_LV conveyor;
 	GlassPackage currentGlass;
 	List<GlassPackage> myGlassPieces = Collections.synchronizedList(new ArrayList<GlassPackage>());
 	enum GlassState {INCOMING, WAITING, NEEDS_WORK, FINISHED, MOVE, NONE};
 	enum PopUpState {FULL, OPEN};
 	enum Status{RAISED,LOWERED};
 	Semaphore stateSemaphore = new Semaphore(0,true);
 	Semaphore statusSemaphore = new Semaphore(0,true);
 	
 	ConveyorFamily next;
 	
 	Transducer t;
 	TChannel channel;
 	
 	public class Machine
 	{
 		Operator operator;
 		int number;
 		TChannel channel;
 		boolean occupied;
 		Semaphore semaphore = new Semaphore(0,true);
 		
 		public Machine(Operator o, TChannel t, boolean b,  int n)
 		{
 			operator = o;
 			number = n;
 			channel = t;
 			occupied = b;
 		}
 	}
 	
 	public class GlassPackage
 	{
 		Operator operator;
 		Glass glass;
 		GlassState state;
 		int operatorNumber;
 		
 		public GlassPackage(Glass g, GlassState s)
 		{
 			glass = g;
 			state = s;
 		}
 	}
 	
 	public PopUpAgent_LV(String n, int i)
 	{
 		name = n;
 		index = i;
 		nextComponentFree = true;
 		status = Status.LOWERED;
 		state = PopUpState.OPEN;
 		operators = Collections.synchronizedList(new ArrayList<Machine>());
 		
 	}
 
 	
 	/*
 	 * MESSAGES
 	 */
 	
 	public void msgIAmFree()
 	{
 		nextComponentFree = true;
 		stateChanged();
 	}
 	
 	public void msgIHaveGlassReady(Glass glass)
 	{
 		synchronized(myGlassPieces)
 		{
 			for(GlassPackage g : myGlassPieces)
 			{
 				if(g.glass == glass)
 				{
 					g.state = GlassState.INCOMING;
 					stateChanged();
 					return;
 				}
 			}
 		
 			myGlassPieces.add(new GlassPackage(glass, GlassState.INCOMING));
 		}
 		stateChanged();
 	}
 	
 	public void msgHereIsGlass(Glass glass) 
 	{
 		synchronized(myGlassPieces)
 		{
 			for(GlassPackage g : myGlassPieces)
 			{
 				if(g.glass == glass)
 				{
 					g.state = GlassState.WAITING;
 					currentGlass = g;
 					stateChanged();
 					return;
 				}
 			}
 			GlassPackage g = new GlassPackage(glass, GlassState.INCOMING);
 			myGlassPieces.add(g);
 			currentGlass = g;
 		}
 		stateChanged();
 
 	}
 	
 	public void msgIHaveGlassFinished(Operator operator) 
 	{
 		synchronized(myGlassPieces)
 		{
 			for(GlassPackage g : myGlassPieces)
 			{
 				if(g.operator == operator)
 				{
 					g.state = GlassState.FINISHED;
 				}
 			}
 		}
 		stateChanged();
 		
 	}
 	
 	public void msgHereIsFinishedGlass(Glass glass) 
 	{
 		synchronized(myGlassPieces)
 		{
 			for(GlassPackage g : myGlassPieces)
 			{
 				if(g.glass == glass)
 				{
 					g.state = GlassState.MOVE;
 					currentGlass = g;
 				}
 			}
 		}
 		stateChanged();
 	}
 	
 	public void msgCannotPass()
 	{
 		//popup busy
 	}
 	
 	
 	/*
 	 * SCHEDULER
 	 */
 	
 	public boolean pickAndExecuteAnAction() {
 
 		GlassPackage temp = null;
 		
 		if(state == PopUpState.OPEN)
 		{
 			synchronized(myGlassPieces)
 			{
 				for(GlassPackage g : myGlassPieces)
 				{
 					if(g.state == GlassState.INCOMING)
 						temp = g;
 				}
 			}
 			if(temp != null)
 			{
 				if((!temp.glass.getRecipe(channel)) || (getOperatorStatus(0) == false || getOperatorStatus(1) == false))
 				{
 					takeGlass(temp);
 					return true;
 				}
 			}
 		}
 		
 		if(state == PopUpState.OPEN)
 			{
 			synchronized(myGlassPieces)
 			{
 				for(GlassPackage g : myGlassPieces)
 				{
 					if(g.state == GlassState.FINISHED)
 						temp = g;
 				}
 			}
 			if(temp != null)
 			{
 				takeFinishedGlass(temp, temp.operatorNumber);
 				return true;
 			}
 		}
 		
 		synchronized(myGlassPieces)
 		{
 			for(GlassPackage g : myGlassPieces)
 			{
 				if(g.state == GlassState.WAITING)
 				{
 					temp = g;
 				}
 			}
 		}
 		if(temp != null)
 		{
 			checkGlass(temp);
 			return true;
 		}
 		
 		if(nextComponentFree)
 		{
 			synchronized(myGlassPieces)
 			{
 				for(GlassPackage g : myGlassPieces)
 				{
 					if(g.state == GlassState.MOVE)
 					{
 						temp = g;
 					}
 				}
 			}
 			if(temp != null)
 			{
 				moveGlass(temp);
 				return true;
 			}
 		}
 		
 		if(getOperatorStatus(0) == false || getOperatorStatus(1) == false)
 		{
 			synchronized(myGlassPieces)
 			{
 				for(GlassPackage g : myGlassPieces)
 				{
 					if(g.state == GlassState.NEEDS_WORK)
 					{
 						temp = g;
 					}
 				}
 			}
 			if(temp != null)
 			{
 				if(getOperatorStatus(0) == false)
 					giveGlassToOperator(temp, 0);
 				else
 					giveGlassToOperator(temp, 1);
 				return true;	
 			}
 		}
 		
 
 		return false;
 	}
 	
 	/*
 	 * ACTIONS
 	 */
 	
 	private void takeGlass(GlassPackage g)
 	{
 		print("Taking glass from conveyor");
		if(status == Status.RAISED) {
 			lowerPopUp();
			try{
				stateSemaphore.acquire();
			} catch(InterruptedException e){
				e.printStackTrace();
			}
 		}
		conveyor.msgPopUpFree();
		
 		state = PopUpState.FULL;
 		g.state = GlassState.WAITING;
 		conveyor.msgPopUpBusy();
 	}
 	
 	private void giveGlassToOperator(GlassPackage g, int operatorNumber)
 	{
 		print("Giving operator glass");
 		if(status == Status.LOWERED);
 			raisePopUp();
 		operators.get(operatorNumber).operator.msgHereIsGlass(g.glass);
 		g.operator = operators.get(operatorNumber).operator;
 		try{
 			operators.get(operatorNumber).semaphore.acquire();
 		} catch(InterruptedException e){
 			e.printStackTrace();
 		}
 		g.operatorNumber = operatorNumber;
 		operators.get(operatorNumber).occupied = true;
 		g.state = GlassState.NONE;
 		state = PopUpState.OPEN;
 		currentGlass = null;
 	}
 	
 	private void moveGlass(GlassPackage g)
 	{
 		print("Moving glass to next conveyor");
 		if(status == Status.RAISED)
 			lowerPopUp();
 		next.msgHereIsGlass(g.glass);
 		myGlassPieces.remove(g);
 		
 		Integer[] args = new Integer[1];
 		args[0] = index;
 		t.fireEvent(TChannel.POPUP, TEvent.POPUP_RELEASE_GLASS, args);
 		try{
 			stateSemaphore.acquire();
 		} catch(InterruptedException e){
 			e.printStackTrace();
 		}
 		state = PopUpState.OPEN;
 		conveyor.msgPopUpFree();
 		currentGlass = null;
 		nextComponentFree= false;
 	}
 	
 	private void checkGlass(GlassPackage g)
 	{
 		print("Checking if glass needs work");
 		if(g.glass.getRecipe(channel))
 			g.state = GlassState.NEEDS_WORK;
 		else
 			g.state = GlassState.MOVE;
 	}
 	
 	private void lowerPopUp()
 	{
 		print("lowering popup");
 		Integer[] args = new Integer[1];
		
		args[0] = index; //Note: popup offset
 		t.fireEvent(TChannel.POPUP, TEvent.POPUP_DO_MOVE_DOWN, args);
 		
 		try{
 			statusSemaphore.acquire();
 		} catch(InterruptedException e){
 			e.printStackTrace();
 		}
 		if(state == PopUpState.OPEN)
 			conveyor.msgPopUpFree();
 		status = Status.LOWERED;
 	}
 	
 	private void raisePopUp()
 	{
 		print("raising popup");
 		Integer[] args = new Integer[1];
		
		args[0] = index;
 		t.fireEvent(TChannel.POPUP, TEvent.POPUP_DO_MOVE_UP, args);
 		
 		try{
 			statusSemaphore.acquire();
 		} catch(InterruptedException e){
 			e.printStackTrace();
 		}
		
		Do("REACHED HERE");
 		conveyor.msgPopUpBusy();
 		status = Status.RAISED;
 	}
 	
 	private void takeFinishedGlass(GlassPackage g, int operatorNum)
 	{
 		print("Taking glass from operator");
 		if(status == Status.LOWERED)
 			raisePopUp();
 		operators.get(operatorNum).operator.msgIAmFree();
 		try{
 			operators.get(operatorNum).semaphore.acquire();
 		} catch(InterruptedException e){
 			e.printStackTrace();
 		}
 		operators.get(operatorNum).occupied = false;
 		g.state = GlassState.MOVE;
 		state = PopUpState.FULL;
 	}
 	
 	
 	@Override
 	public void eventFired(TChannel channel, TEvent event, Object[] args) {
 		
 		if((channel == TChannel.POPUP) && ((Integer)(args[0]) == index)) //Note: popup offset
 		{
 			if(event == TEvent.POPUP_GUI_MOVED_DOWN)
 				statusSemaphore.release();
 			if(event == TEvent.POPUP_GUI_MOVED_UP)
 				statusSemaphore.release();
 			if(event == TEvent.POPUP_GUI_LOAD_FINISHED)
 				stateSemaphore.release();
 			if(event == TEvent.POPUP_GUI_RELEASE_FINISHED)
 				stateSemaphore.release();
 		}
 		else if((channel == operators.get(0).channel) && ((Integer)(args[0]) == operators.get(0).number))
 		{
 			if(event == TEvent.WORKSTATION_LOAD_FINISHED)
 				operators.get(0).semaphore.release();
 			if(event == TEvent.WORKSTATION_RELEASE_FINISHED)
 				operators.get(0).semaphore.release();
 		}
 		else if((channel == operators.get(1).channel) && ((Integer)(args[0]) == operators.get(1).number))
 		{
 			if(event == TEvent.WORKSTATION_LOAD_FINISHED)
 				operators.get(1).semaphore.release();
 			if(event == TEvent.WORKSTATION_RELEASE_FINISHED)
 				operators.get(1).semaphore.release();
 		}
 		
 	}
 	
 	public String getName()
 	{
 		return name;
 		
 	}
 	
 	public void setOperators(Operator operatorOne, Operator operatorTwo, TChannel c)
 	{
 		this.operators.add(new Machine(operatorOne,c,false,0));
 		this.operators.add(new Machine(operatorTwo,c,false,1));
 	}
 	
 	public void setTransducer(Transducer trans)
 	{
 		t = trans;
 		t.register(this, TChannel.POPUP);
 	}
 	
 	public void setInteractions(ConveyorFamily cf, Conveyor_LV c, Transducer trans)
 	{
 		conveyor = c;
 		next = cf;
 		t = trans;
 		t.register(this, TChannel.POPUP);
 	}
 	
 	public void setConveyor(Conveyor_LV c)
 	{
 		conveyor = c;
 	}
 	
 	public void setNextComponentFree(boolean b)
 	{
 		nextComponentFree = b;
 	}
 		
 	public boolean getNextComponentFree()
 	{
 		return nextComponentFree;
 	}
 	
 	public Conveyor_LV getConveyor()
 	{
 		return conveyor;
 	}
 	
 	public ConveyorFamily getNextConveyorFamily()
 	{
 		return next;
 	}
 	
 	public int getOperatorsSize()
 	{
 		return operators.size();
 	}
 	
 	public int getGlassPiecesSize()
 	{
 		return myGlassPieces.size();
 	}
 	
 	public Operator getOperator(int i)
 	{
 		return operators.get(i).operator;
 	}
 	
 	public boolean getOperatorStatus(int i)
 	{
 		return operators.get(i).occupied;
 	}
 	
 	public void setOperatorStatus(int i,boolean b)
 	{
 		operators.get(i).occupied = b;
 	}
 
 
 	public void setInteractions(ConveyorFamily c3) {
 		// TODO Auto-generated method stub
 		next = c3;
 	}
 
 
 
 	
 }
