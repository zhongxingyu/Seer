 package TLTTC;
 
 import java.util.*;
 import java.util.concurrent.*;
 
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
 					System.out.println("RECEIVED MESSAGE ~ (source : " + message.getSource() + "), (dest : " + message.getDest() + ")\n");
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
 					System.out.println("PASSING MSG ~ (source : " + message.getSource() + "), (step : " + name + "), (dest : "+message.getDest()+"), (type : " + message.getType()+")");
                     message.updateSender(name);
 					send(message);
 				}
 			}
 
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
 
 				if(forwardTrain.isLocationValid() && train.isLocationValid() && train.isStoppingDistanceValid())
 				{
 					sendAuthority(train.trainNumber, calculateMovingBlock(train.getLocation(), train.getStoppingDistance(), forwardTrain.getLocation(), 0));
 					forwardTrain.setLocationValid(false);
 					train.setStoppingDistanceValid(false);
 				}
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
 	@SuppressWarnings("unchecked")
 	private void receivedTrainUpdate(Message message)
 	{
 
 		trains = (MyLinkedList<Train>)message.getData().get("trainList");
 	}
 
 	/*
 		Message Senders
 	*/
 
 	public void send(Message m)
 	{
 		//System.out.println("SENDING MSG ~ (start : "+m.getSource() + "), (dest : "+m.getDest()+"), (type : " + m.getType()+ ")");
         m.updateSender(name);
         Environment.passMessage(m);
 	}
 
 	public void sendMessages()
 	{
 		Enumeration<Integer> keys;
 		int trainNumber;
 		Message m;
 
 		keys = authorityOutbox.keys();
 
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
 
 		message = new Message(name, name, Module.trainController, msg.MBO_TnCt_Send_Moving_Block_Authority);
 		message.addData("trainID", trainNumber);
 		message.addData("authority", authority);
 		//send(message);
 
 		authorityOutbox.remove(trainNumber);
 		authorityOutbox.put(trainNumber, message);
 	}
 	private void requestLocation(int trainNumber)
 	{
		//Message message;
		//message = new Message(name, name, Module.satellite,msg.placeHolder);
		//message.addData("Train Number", trainNumber);
		//send(message);
 
 
 	}
 
 	private void requestStoppingDistance(int trainNumber)
 	{
		//Message message;
		//message = new Message(name, name, Module.trainModel,msg.placeHolder);
		//message.addData("Train Number", trainNumber);
		//send(message);
 
 
 
 	}
 }
