 package TLTTC;
 import java.util.*;
 import java.util.concurrent.*;
 
 //This class will hold all of the trains
 //It will also receive messages and route them to the trains
 //It will also also abstract out train construction 
 public class TrainContainer extends Worker implements Runnable, constData 
 {
 	private Module name;
 	private LinkedBlockingQueue<Message> msgs;
   
 	private HashTable<Integer, TrainModel> trains;
   
 	private Timer motionTimer;
 
 	private final TIME_STEP = .1; //timestep (in s) for train motion integration (simulation time!)
 
 	private int timerTrigger = 1; //real-time value (in ms) for triggering motionStep() calls
 	//For now, simulationSpeedup = (trainTimestep * 1000) / timerTrigger
 
 	//for message sending/receiving
 	int trainID;
 	TrainModel tm;
 	Block bl;
 	Message outgoingMessage;
 	
 
 	public TrainContainer()
 	{ 
   		this.name = Module.trainModel;
 		msgs  = new LinkedBlockingQueue<Message>();
 		trains = new HashTable<Integer, TrainModel>();
 		motionTimer = new Timer();
   		motionTimer.scheduleAtFixedRate(new motionTask(), 0, timerTrigger); //update all the train motion every X ms
 	}
   
 	//Driver for timed motion!
 	private class motionTask extends TimerTask 
 	{
  		public void run()
 		{
   			Enumeration<int> enumKey = trains.keys();
   			while(enumKey.hasMoreElements())
   			{
   				trains.get(enumKey.nextElement()).motionStep(); //move the trains!
 	  		}
   		}
 	}
 
 	public void newTrain(int TrainID, Block start)
 	{
 		trains.add(new TrainModel(TrainID, start, trainTimestep));
 		//send a message to TrainControllerModule to make a new, linked TrainController
 
 	}
 
 	public void run()
 	{
 		//Thread t = new Thread();
 
 		while(true)
 		{
 		 	if(msgs.peek() != null)
 			{
 				Message mine = msgs.poll();
 
 				if(name == mine.getDest())
 				{
 					
 					System.out.println("\nRECEIVED MSG: source->"+mine.getSource() + " : dest->"+mine.getDest()+"\n");
 
 					if(mine.getData() != null)
 					{
 						//System.out.println(mine.getData().get("check")); ????
 						
 						//handling incoming messages
 						switch (mine.getType())
 						{
 							case CTC_TnMd_Request_Train_Creation:
 								bl = (Block)mine.getData.get("yard");
 								if(bl.isOccupied())
 								{
 									//fail silently
 								} 
 								else
 								{
 									tm = new TrainModel((int)mine.getData.get("trainID"), bl, TIME_STEP);
 
 									//send associated messages!!!
 									
 									//send TnMd_CTC_Confirm_Train_Creation
									outgoingMessage = new Message(Module.trainModel, Module.trainModel, Module.CTC, msg.TnMd_CTC_Confirm_Train_Creation, new String[] {"trainID"}, new Object[] {mine.getData.get("trainID")});
 									Environment.passMessage(outgoingMessage);
 
 									//send TnMd_TcMd_Request_Yard_Node
									outgoingMessage = new Message(Module.trainModel, Module.trainModel, Module.trackModel, msg.TnMd_TcMd_Request_Yard_Node, new String[] {"trainID", "blockID"}, new Object[] {mine.getData.get("trainID"), (Object)(bl.getID())});
 									Environment.passMessage(outgoingMessage);
 
 									//send TnMd_TnCt_Request_Train_Controller_Creation
									outgoingMessage = new Message(Module.trainModel, Module.trainModel, Module.trainController, msg.TnMd_TnCt_Request_Train_Controller_Creation, new String[] {"trainID"}, new Object[] {mine.getData.get("trainID")});
 									Environment.passMessage(outgoingMessage);
 
 									//send TnMd_Sch_Notify_Yard
									outgoingMessage = new Message(Module.trainModel, Module.trainModel, Module.scheduler, msg.TnMd_Sch_Notify_Yard, new String[] {"entry","trainID","blockID"}, new Object[] {(Object)false, mine.getData.get("trainID"), (Object)(bl.getID())});
 									Environment.passMessage(outgoingMessage);
 								}
 								break;
 							case TnCt_TnMd_Send_Power:
 								//update power setting
 								trainID = (int)(mine.getData.get("trainID"));
 								tm = trains.get(trainID);
 
 								tm.setPower(mine.getData.get("power"));
 								break;
 							case TcMd_TnMd_Send_Yard_Node:
 								trainID = (int)(mine.getData.get("trainID"));
 								tm = trains.get(trainID);
 
 								tm.setYardNode((Node)mine.getData.get("yard"));
 								break;
 							case TnCt_TnMd_Request_Train_Velocity:
 								trainID = (int)(mine.getData.get("trainID"));
 								tm = trains.get(trainID);
 							
								outgoingMessage = new Message(Module.trainModel, Module.trainModel, Module.trainController, msg.TnMd_TnCt_Send_Train_Velocity, new String[] {"trainID","velocity"}, new Object[] {(Object)trainID, (Object)tm.getVelocity()});
 
 								break;
 							default:
 								//stuff
 						}
 						
 					}
 					else
 					{
 						//other stuff!
 					}
 				}
 				else
 				{
 					System.out.println("PASSING MSG: step->"+name + " source->"+mine.getSource()+ " dest->"+mine.getDest());
 					mine.updateSender(name);
 					Environment.passMessage(mine);
 				}
 			}
 		}
 	}
 
 	public void setMsg(Message m)
 	{
 		msgs.add(m);
 	}
 
 	public void send()
 	{
 		Message outgoing = new Message(name, name, Module.trainController);
 		System.out.println("SENDING MSG: start->"+outgoing.getSource() + " : dest->"+outgoing.getDest()+"\n");
 		Environment.passMessage(outgoing);
 	}
 
 	public void send(Message m)
 	{
    		System.out.println("SENDING MSG: start->"+m.getSource() + " : dest->"+m.getDest()+"\n");
 		Environment.passMessage(m);
 	}
   
   //methods to send messages go here
 }
