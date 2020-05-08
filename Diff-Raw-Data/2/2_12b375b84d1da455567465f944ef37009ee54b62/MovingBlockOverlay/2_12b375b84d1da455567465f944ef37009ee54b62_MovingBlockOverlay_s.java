 package TLTTC;
 
 import java.util.*;
 import java.util.concurrent.*;
 
@suppressWarnings("unchecked")
 public class MovingBlockOverlay extends Worker implements constData
 {
 	public static long DELIVERY_FREQUENCY = 1000; //milliseconds
 
 	private long nextDelivery;
 	private LinkedBlockingQueue<Message> messages;
 	private Hashtable<Integer, Message> authorityOutbox;
 	private Hashtable<Integer, Message> distanceOutbox;
 	private Hashtable<Integer, Message> locationOutbox;
 	private Module name;
 	private MyLinkedList<Train> trains;
 
 	/*
 		Main
 	*/
 
 	public static void main(String[] args)
 	{
 
 	}
 
 	/*
 		Constructor
 	*/
 
 	public MovingBlockOverlay()
 	{
 		messages = new LinkedBlockingQueue<Message>();
 		authorityOutbox = new Hashtable<Integer, Message>();
 		distanceOutbox = new Hashtable<Integer, Message>();
 		locationOutbox = new Hashtable<Integer, Message>();
 		this.name = Module.MBO;
 		nextDelivery = 0;
 	}
 
 	/*
 		Methods
 	*/
 
 	//Find train in linked list
 
 	private Train findTrain(int trainNumber)
 	{
 		Train t;
 		Train value;
 		Train train;
 
 		value = null;
 
 		if(trains.size() == 0)
 		{
 			return value;
 		}
 
 		t = trains.selected();
 
 		do
 		{
 			train = trains.next();
 
 			if(train.trainNumber == trainNumber)
 			{
 				value = train;
 			}
 		}
 		while(!t.equals(trains.selected()));
 
 		return value;
 	}
 
 	//Calculate moving block between trains
 
 	private double calculateMovingBlock(double trainLocation, double trainSpeed, double forwardTrainLocation, double forwardTrainSpeed)
 	{
 /*
 		double vF = 0;
 		double timeF = (0 - trainSpeed) / Train.BRAKEDECELERATION;
 		double distance = trainSpeed * timeF + .5 * Train.BRAKEDECELERATION * Math.pow(timeF, 2);
 
 		return forwardTrainLocation - ((distance + trainLocation) % Track.LENGTH);		
 */
 		return Double.MAX_VALUE;
 	}
 
 	/*
 		Thread Loop
 	*/
 
 	public void run()
 	{
 
 		while(true)
 		{
 			if(messages.peek() != null)
 			{
 				Message message = messages.poll();
 
 				if(name == message.getDest())
 				{
 					System.out.println("\nRECEIVED MESSAGE: source->" + message.getSource() + " : dest->" + message.getDest() + "\n");
 
 					switch(message.getType())
 					{
 						/*case 86:
 							receivedGPSLocation(message);
 							break;*/
 						/*case 88:
 							receivedStoppingDistance(message);
 							break;*/
 						//case 91:
 							//Not implemented
 							//break;
 						case Sch_MBO_Notify_Train_Added_Removed:
 							receivedTrainUpdate(message);
 							break;	
 					}
 				}
 				else
 				{
 					System.out.println("PASSING MESSAGE: step->" + name + " source->" + message.getSource() + " dest->" + message.getDest());
 					message.updateSender(name);
 					Environment.passMessage(message);
 				}
 			}
 
 			//Send messages in outboxes after 1 second has elapsed
 
 			if(nextDelivery < System.currentTimeMillis())
 			{
 				sendMessages();
 				nextDelivery = System.currentTimeMillis() + DELIVERY_FREQUENCY;
 			}
 
 			if(trains != null && trains.size() > 0)
 			{
 				Train forwardTrain;
 				Train train;
 	
 				forwardTrain = trains.previous(); //returns selected train, then goes backwards
 				train = trains.selected();
 
 				//If conditions are correct to calcuate moving block, do it
 
 				if(forwardTrain.isLocationValid() && train.isLocationValid() && train.isStoppingDistanceValid())
 				{
 					sendAuthority(train.trainNumber, calculateMovingBlock(train.getLocation(), train.getStoppingDistance(), forwardTrain.getLocation(), 0));
 					forwardTrain.setLocationValid(false);
 					train.setStoppingDistanceValid(false);
 				}
 
 				//If not, create messages to send to trains
 
 				else
 				{
 					if(!forwardTrain.isLocationValid())
 					{
 						requestLocation(forwardTrain.trainNumber);
 						forwardTrain.setLocationValid(true);
 					}
 
 					if(!train.isStoppingDistanceValid())
 					{
 						requestStoppingDistance(train.trainNumber);
 						train.setStoppingDistanceValid(true);
 					}
 				}
 			}			
 		}
 
 	}
 
 	/*
 		Message Handlers
 	*/
 
 	public void setMsg(Message message)
 	{
 		messages.add(message);
 	}
 
 	private void receivedGPSLocation(Message message)
 	{
 		
 	}
 
 	//Update train information in linked list
 
 	private void receivedStoppingDistance(Message message)
 	{
 		Train train;
 
 		train = findTrain((int)(message.getData().get("trainID")));
 
 		if(train != null)
 		{
 			train.setStoppingDistance((double)(message.getData().get("stoppingDist")), System.currentTimeMillis());
 			train.setStoppingDistanceValid(true);
 		}
 	}
 
 	//When train is added/removed from track, update object
 
 	private void receivedTrainUpdate(Message message)
 	{
 		trains = (MyLinkedList<Train>)message.getData().get("trainList");
 	}
 
 	/*
 		Message Senders
 	*/
 
 	public void send(Message message)
 	{
 		System.out.println("SENDING MSG: start->"+message.getSource() + " : dest->"+message.getDest()+"\n");
 		Environment.passMessage(message);
 	}
 
 	//Send messages from outboxes
 
 	public void sendMessages()
 	{
 		Enumeration<Integer> keys;
 		int trainNumber;
 		Message m;
 
 		keys = authorityOutbox.keys();
 
 		//Send authorites to trains
 
 		while(keys.hasMoreElements())
 		{
 			trainNumber = (int)keys.nextElement();
 			m = authorityOutbox.remove(trainNumber);
 
 			if(m != null)
 			{
 				send(m);
 			}
 		}
 
 		keys = locationOutbox.keys();
 
 		//Request train locations from satellite
 
 		while(keys.hasMoreElements())
 		{
 			trainNumber = (int)keys.nextElement();
 			m = locationOutbox.remove(trainNumber);
 
 			if(m != null)
 			{
 				send(m);
 			}
 		}
 
 		keys = distanceOutbox.keys();
 
 		//Request train stopping distance from trains
 
 		while(keys.hasMoreElements())
 		{
 			trainNumber = (int)keys.nextElement();
 			m = distanceOutbox.remove(trainNumber);
 
 			if(m != null)
 			{
 				send(m);
 			}
 		}
 	}
 
 	private void sendAuthority(int trainNumber, double authority)
 	{
 		Message message;
 
 		message = new Message(name, name, Module.scheduler, msg.MBO_TnCt_Send_Moving_Block_Authority);
 		message.addData("trainID", trainNumber);
 		message.addData("authority", authority);
 		//send(message);
 
 		authorityOutbox.remove(trainNumber);
 		authorityOutbox.put(trainNumber, message);
 	}
 	private void requestLocation(int trainNumber)
 	{
 		Message message;
 
 		//message = new Message(name, name, Module.scheduler);
 		//message.addData("trainID", trainNumber);
 		//send(message);
 
 		//locationOutbox.remove(trainNumber);
 		//locationOutbox.put(trainNumber, message);
 	}
 
 	private void requestStoppingDistance(int trainNumber)
 	{
 		Message message;
 
 		//message = new Message(name, name, Module.scheduler);
 		//message.addData("trainID", trainNumber);
 		//send(message);
 
 		//distanceOutbox.remove(trainNumber);
 		//distanceOutbox.put(trainNumber, message);
 	}
 }
