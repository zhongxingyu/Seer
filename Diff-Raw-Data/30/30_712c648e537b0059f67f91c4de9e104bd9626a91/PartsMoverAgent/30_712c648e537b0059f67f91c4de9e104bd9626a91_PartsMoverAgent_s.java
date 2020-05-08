 package agents;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 import agents.include.Agent;
 import agents.interfaces.PartsGiver;
 import agents.interfaces.PartsMover;
 import agents.interfaces.PartsTaker;
 
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
     	//Add the request to the list, the scheduler will decide what to do
     	pendingRequests.add(new Request(partType, count, openSpaces, receiver));
     	stateChanged();
     }
     
     //Hands a part
     public void msgHereIsPart(String partType)
     {
     	//Check that it is the right type that we have requested and are expecting
     	if (partType.equals(currentPart))
     	{
     		expecting--;	//We got it
     		holding++;		//There is one more
     		partAdded();
     	}
 //    	else
 //   		print("Error - received unrequested part type " + p.getPartType());
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
 		//Requests for anybody when ideal are filled
		if (requested == 0 && pendingRequests.size() != 0)
 		{
 			//If we're not holding onto any parts, just fill the next one in the queue
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
 					toFill = pendingRequests.remove(0);
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
 			//None of the current part type is currently selected
 			//next run of the scheduler will decide which request to fill
 			requested = 0;
 			return true;
 		}
 		
 		// (Requests for different agents than we are currently serving wait until current request is finished)
 		
 		
 		// Make a request for more parts if we need to and can hold them
 		if (requested > holding + expecting && holding < maxHolding)
 		{
 			int deficit = Math.min(requested - holding - expecting, maxHolding - holding);	//Request more, capped at our capacity
     		doMakeRequest(deficit);
     		return true;
 		}
 		
 		// Parts held are passed if they have been requested and we are ready to move
 		if (readyToMove && receiverSpaces > 0)
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
 		readyToMove = false;
     	holding = 0;
     	expecting = 0;
     	partsCleared();
     }
 	
 	//Makes a request from the supplier
 	protected void doMakeRequest(int count)
 	{
 		print("Making request for " + count + " " + currentPart + " parts, open:" + (maxHolding-holding));
 		supplier.msgRequestParts(currentPart, count, maxHolding-holding, this);
 		expecting += count;
 	}
 	
 	//Hands one part
 	protected void doHandPart()
 	{
		print("Handing one part");
 		receiver.msgHereIsPart(currentPart);
		holding--;				//Holding one less
		requested--;			//Filled one request
		receiverSpaces--;		//We filled one of those spaces
 		readyToMove = false;	//So the scheduler waits until device is ready to move another part
 		partRemoved();
 	}
 	
 	//Force implementation of these methods that will be required for graphics, lets this generalized type use them
 	public abstract void partRemoved();		//One part removed from front of queue
 	public abstract void partAdded();		//One part added to back of queue
 	public abstract void partsCleared();	//All parts removed
 	
 }
