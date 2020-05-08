 package agents;
 
 import java.util.ArrayList;
 import java.util.Collection;
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
 public abstract class PartsMoverAgent extends Agent implements PartsMover {
 	
     protected String currentPart;			// Part type currently held
     protected int requested;				// Current quantity requested from this
     protected int expecting;				// Amount we have requested from upstream
     protected int holding;					// Number of parts we are currently holding onto
     protected int maxHolding;				// The maximum number of parts that can fit in this machine 
     protected PartsTaker receiver;			// The agent whose request we are currently fulfilling
     protected PartsGiver supplier;			// The agent that supplies me
     protected boolean readyToMove = false;	// Set to true when the device is ready to move a single part downstream
     private String name; 					// needed for agents name (nice print msgs)
     
     //Holds requests as they come in so we can intelligently decide how to fill them
     List<Request> pendingRequests = new ArrayList<Request>();
     
     public PartsMoverAgent(String name, int maxHolding)
     {
     	this.name = name;
     	
     	currentPart = "";
     	requested = 0;
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
     	String partType;
     	int count;
     	PartsTaker receiver;
     	public Request(String partType, int count, PartsTaker receiver)
     	{
     		this.partType = partType;
     		this.count = count;
     		this.receiver = receiver;
     	}
     }
     
     //Requests a part
     public void msgRequestParts(String partType, int count, PartsTaker receiver)
     {
     	//Add the request to the list, the scheduler will decide what to do
     	pendingRequests.add(new Request(partType, count, receiver));
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
 	protected boolean pickAndExecuteAnAction() {
 		//Requests for anybody when ideal are filled
 		if (requested == 0 && pendingRequests.size() != 0)
 		{
 			doFill(pendingRequests.remove(0));
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
 		
 		
 		//Requests for the same agent for a different part cause a purge and subsequent fill
 		Request changeRequest = null;
 		for (Request r : pendingRequests)
 			if (receiver == r.receiver)
 			{
 				changeRequest = r;
 				break;
 			}
 		if (changeRequest != null) 
 		{
 			doFill(changeRequest);
 			pendingRequests.remove(changeRequest);
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
 		if (readyToMove && requested > 0)
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
 		currentPart = r.partType;	//Update part we are now moving
 		receiver = r.receiver;		
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
 		print("Making request for " + count + " " + currentPart + " parts");
 		supplier.msgRequestParts(currentPart, count, this);
 		expecting += count;
 	}
 	
 	//Hands one part
 	protected void doHandPart()
 	{
 		print("Handing one part");
 		receiver.msgHereIsPart(currentPart);
 		holding--;				//Holding one less
 		requested--;			//Filled one request
 		readyToMove = false;	//So the scheduler waits until device is ready to move another part
 		partRemoved();
 	}
 	
 	//Force implementation of these methods that will be required for graphics, lets this generalized type use them
 	public abstract void partRemoved();		//One part removed from front of queue
 	public abstract void partAdded();		//One part added to back of queue
 	public abstract void partsCleared();	//All parts removed
 	
 }
