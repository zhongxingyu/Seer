 package agents;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import agents.include.Agent;
 import agents.interfaces.PartsGiver;
 import agents.interfaces.PartsMover;
 import agents.interfaces.PartsTaker;
 
 import state.FactoryState;
 
 /**
  * 
  * @author Steve
  *	Superclass for agents that behave like buffers of one type of part (Lane, feeder, gantry)
  *	Gets requests from parts takers and queues them
  * 	Scheduler decides what to do with queued requests (fill now or later)
  */
 
 /*** DO METHODS ***
  *  doPurge()
  *  doFill(Request r)
  *  doMakeRequest(int count)
  *  doHandPart()
  **/
 
 
 
 public abstract class PartsMoverAgent extends Agent implements PartsMover {
 	
     public String currentPart;			// Part type currently held
     public int requested;				// Current quantity requested from this
     public int receiverSpaces;			// The number of open spaces in the receiver
     public int expecting;				// Amount we have requested from upstream
     public int holding;					// Number of parts we are currently holding onto
     public int maxHolding;				// The maximum number of parts that can fit in this machine 
     public PartsTaker receiver;			// The agent whose request we are currently fulfilling
     public PartsGiver supplier;			// The agent that supplies me
     public boolean readyToMove = false;	// Set to true when the device is ready to move a single part downstream
     private String name;				// needed for agents name (nice print msgs)
     private long timeSinceLastPass = 0;	// Helps us decide if we should purge to fill a different request or wait because of device delay
     
     //Holds requests as they come in so we can intelligently decide how to fill them
     public List<Request> pendingRequests = Collections.synchronizedList(new ArrayList<Request>());
     
     public PartsMoverAgent(String name, int maxHolding)
     {
     	this.name = name;
     	
     	currentPart = "";
     	requested = 0;
     	receiverSpaces = 0;
     	expecting = 0;
     	holding = 0;
     	this.maxHolding = maxHolding;
    	
    	Timer t = new Timer();
		t.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				stateChanged();		//Check if receiving device has remained blocked
			}
		}, 1000, 2000);
     }
     
     /* Getter for name */
     public String getName() {
         return name;
     }  
     
     protected class Request
     {
     	String partType;		//The kind of part
     	int count;				//Number required
     	int openSpaces;			//Number of open spaces
     	PartsTaker receiver;	//The thing that has requested
     	public Request(String partType, int count, int openSpaces, PartsTaker receiver)
     	{
     		this.partType = partType;
     		this.count = count;
     		this.receiver = receiver;
     		this.openSpaces = openSpaces;
     	}
     }
     
     //Requests a part
     public void msgRequestParts(String partType, int count, int openSpaces, PartsTaker receiver)
     {
     	synchronized (pendingRequests)
     	{
     		//Add the request to the list, the scheduler will decide what to do
     		pendingRequests.add(new Request(partType, count, openSpaces, receiver));
     	}
     	stateChanged();
     }
     
     //Hands a part
     public void msgHereIsPart(String partType)
     {
     	synchronized (pendingRequests)
     	{
 	    	//Check that it is the right type that we have requested and are expecting
 	    	if (partType.equals(currentPart))
 	    	{
 	    		if (expecting > 0) expecting--;	//We got it
 	    		holding++;		//There is one more
 	    		partAdded();
 	    	}
     	}
     	stateChanged();
     }
     
     //Called by gui once it has been sensed that we can pass one part onto the receiver
     public void msgReadyToMove()
     {
     	readyToMove = true;
     	stateChanged();
     }
     
     //Called by control agent or testing class on setup to connect the chain
 	public void msgSetSupplier(PartsGiver supplier)
 	{
 		this.supplier = supplier;
 	}
     
 	@Override
 	public boolean pickAndExecuteAnAction() {
 		
 		//Block modifications of requests while we're using them
 		synchronized (pendingRequests)
 		{
 			//Requests for anybody when idle are filled
 			if (expecting == 0 && 
 					( holding == 0 || (receiverSpaces == 0 && receiverProbablyBlocked()) )
 					&& requested == 0 && pendingRequests.size() != 0)
 			{
 				//If we're not passing any parts, just fill the next one in the queue
 				Request toFill = null;
 				if (holding == 0)
 					toFill = pendingRequests.remove(0);
 				else
 				{
 					//See if there are any requests for the part type we're holding onto
 					for (Request r : pendingRequests)
 						if (r.partType.equals(currentPart))
 						{
 							toFill = r;
 							break;
 						}
 					if (toFill == null)
 					{
 						toFill = pendingRequests.remove(0);
 					}
 					else
 						pendingRequests.remove(toFill);
 				}
 				doFill(toFill);
 				return true;
 			}
 	
 			//Requests for the same agent for more of the same part increase current working request
 			Request increaseRequest = null;
 			for (Request r : pendingRequests)
 				if (receiver == r.receiver && r.partType.equals(currentPart))
 				{
 					increaseRequest = r;
 					break;
 				}
 			if (increaseRequest != null) 
 			{
 				doFill(increaseRequest);
 				pendingRequests.remove(increaseRequest);
 				return true;
 			}
 			
 			
 			//Requests for the same agent for a different part cause a cancellation of this fill, then it'll be filled later if we keep it on the request list
 			Request changeRequest = null;
 			for (Request r : pendingRequests)
 				if (receiver == r.receiver)
 				{
 					changeRequest = r;
 					break;
 				}
 			if (changeRequest != null) 
 			{
 				//None of the current part type is currently requested for this agent
 				requested = 0;
 				receiverSpaces = 0;
 				return true;
 			}
 		}
 			// (Requests for different agents than we are currently serving wait until current request is finished)
 			
 			
 		// Make a request for more parts if we need to and can hold them
 		if (requested > holding + expecting)
 		{
 			int deficit = requested - holding - expecting ;	//Request more
     		doMakeRequest(deficit);
     		return true;
 		}
 		
 		// Parts held are passed if they have been requested and we are ready to move
 		if (readyToMove && receiverSpaces > 0 && holding > 0)
 		{
 			doHandPart();
 			return true;
 		}
 		
 		
 		
 		return false;
 	}
 	
 	//Makes a pending request the current request
 	protected void doFill(Request r)
 	{
 		//Check if we're changing the part
 		if (holding > 0 && !r.partType.equals(currentPart))
 		{
 			doPurge();					//Purge and take the new count
 			requested = r.count;		//Update request count so we can request from upstream if we need to
 		}
 		else
 		{
 			if (receiver != r.receiver) requested = 0;	//New agent is requesting
 			requested += r.count;						//Requesting more
 		}
 		
 		//Update the number of open spaces
 		receiverSpaces = r.openSpaces;
 		
 		currentPart = r.partType;	//Update part we are now moving
 		
 		receiver = r.receiver;		//Update receiver		
 	}
 	
 	//Purges the contents
 	protected void doPurge()
     {
 		print("Purging");
 		FactoryState.out.print("Purging");
 		readyToMove = false;
     	holding = 0;
     	expecting = 0;
     	partsCleared();
     }
 	
 	//Makes a request from the supplier
 	protected void doMakeRequest(int count)
 	{
 		int open = getOpenSpaces();
 		print("Making request for " + count + " " + currentPart + " parts, open:" + open);
 		supplier.msgRequestParts(currentPart, count, open, this);
 		expecting += count;
 	}
 	
 	//Hands one part
 	protected void doHandPart()
 	{
 		print("Handing one " + currentPart + " part, now holding " + (holding-1));
 		receiver.msgHereIsPart(currentPart);
 		holding--;								//Holding one less
 		if (requested >0) requested--;			//Filled one request
 		receiverSpaces--;						//We filled one of those spaces
 		if (supplier != null)
 			supplier.msgRequestParts(currentPart, 0, getOpenSpaces(), this);	//Update open spaces
 		readyToMove = false;	//So the scheduler waits until device is ready to move another part
 		timeSinceLastPass = System.currentTimeMillis();
 		partRemoved();
 	}
 	
 	//Calculate the number of open spaces relevant to the supplier
 	protected int getOpenSpaces()
 	{
 		return maxHolding-holding;
 	}
 	
 	//The amount of time the receiving device is given to move in the event of no open spaces and other pending requests
 	protected boolean receiverProbablyBlocked()
 	{
 		if (timeSinceLastPass == 0) return false;
		return 5200 < (System.currentTimeMillis()-timeSinceLastPass);	//5.2 sec
 	}
 	
 	//Force implementation of these methods that will be required for graphics, lets this generalized type use them
 	public abstract void partRemoved();		//One part removed from front of queue
 	public abstract void partAdded();		//One part added to back of queue
 	public abstract void partsCleared();	//All parts removed
 	
 }
