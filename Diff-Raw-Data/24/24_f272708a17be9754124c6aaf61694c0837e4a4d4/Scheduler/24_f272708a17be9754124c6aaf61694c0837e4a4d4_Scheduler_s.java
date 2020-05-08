 //Marcus Hayes
 //Computer Engineeering
 //Senior
 //ECE 1186
 //Th 6-9
 //TLTTC - Scheduler/MBO
 
 package TLTTC;
 
 import java.util.*;
 import java.util.concurrent.*;
 
 public class Scheduler extends Worker implements Runnable, constData
 {
 	public static int NEXT_TRAIN_NUMBER = 0;
 
 	private List<SchedulerListener> listeners;
 	private LinkedBlockingQueue<Message> messages;
 	private Module name;
 	private OperatorSchedule schedule;
 	private Timetable timetable;
 	private ArrayList<Train> redTrains;
 	private ArrayList<Train> greenTrains;
 	private RouteSchedule route;
 
 	public static Node[] nodes;
 	public static Block[] blocks;
 
 	private Block startingBlock;
 	private Node startingPreviousNode;
 	private Node startingNextNode;
 
 	public static void main(String[] args)
 	{
 		int numBlocks = 7;
 		int numNodes = numBlocks + 1;
 		blocks = new Block[numBlocks];
 		nodes = new Node[numNodes];
 
 		nodes[0] = new YardNode(0, 0, 0);
 		
 		for(int i = 1; i < nodes.length - 1;i++)
 		{
 			nodes[i]= new ConnectorNode(i * i * i * i * i, i * i * i * i * i, i * i * i * i * i);
 		}
 
 		nodes[nodes.length - 1] = new YardNode((nodes.length - 1) * (nodes.length - 1), (nodes.length - 1) * (nodes.length - 1), (nodes.length - 1) * (nodes.length - 1));
 
 		for(int i = 0; i < blocks.length; i++)
 		{
 			blocks[i] = new LinearBlock(nodes[i], nodes[i+1], i, 0);
 			nodes[i].setOutput(blocks[i]);
 			nodes[(i+1) % blocks.length].setInput(blocks[i]);
 		}		
 
 
 		Worker sch = new Scheduler();
 		Thread t = new Thread(sch);
 		t.start();
 
 		Message message = new Message(Module.trainModel, Module.trainModel, Module.scheduler, msg.TnMd_Sch_Notify_Yard);
 		message.addData("entry", false);
 		message.addData("trainID", 0);
 		message.addData("isGreenLine", true);		
 		sch.setMsg(message);
 		//System.out.println("here");		
 	}
 
 	/*
 		Constructor
 	*/
 
 	public Scheduler()
 	{
 		timetable = new Timetable();
 		schedule = new OperatorSchedule();
 		route = new RouteSchedule();
 		redTrains = new ArrayList<Train>();
 		greenTrains = new ArrayList<Train>();
 		listeners = new ArrayList<SchedulerListener>();
 		messages = new LinkedBlockingQueue<Message>();
 		this.name = Module.scheduler;
 		new SchedulerViewModel(this);
 
 		updateOperatorSchedule();
 		//updateTimetable();
 		sendTrainInfo();
 	}
 
 	public void init(TrackModel tm)
 	{
 		HashMap<Integer, Block> blocks = tm.getBlocks();
 		Iterator<Block> i = tm.getBlocks().values().iterator();
 
 		while(i.hasNext())
 		{
 			startingBlock = i.next();
 
			if(startingBlock.getYardNode() != null && startingBlock.getYardNode().equals(startingBlock.getStartNode()))
 			{
 				startingPreviousNode = startingBlock.getStartNode();
 				startingNextNode = startingBlock.getStopNode();
 				break;
 			}
 		}
		
 	}
 
 	/*
 		Methods
 	*/
 
 	public String toString()
 	{
 		String s;
 
 		s = "Timetable";
 
 		s = s.concat(timetable.toString());
 		s = s.concat("Operator Schedule");
 		s = s.concat(schedule.toString());
 
 		return s;		
 	}
 
 	public boolean updateOperatorSchedule()
 	{
 		int numTrains;
 
 		numTrains = calculateOptimalTrains(true); //Get minimum number of trains that should be on the track
 
 		if(greenTrains.size() < numTrains || redTrains.size() < numTrains)
 		{
 			//Add trains to schedule if there must be more trains on the track and on the schedule
 
 			while(schedule.size() < numTrains)
 			{
 				schedule.add("Train", "Operator", Scheduler.NEXT_TRAIN_NUMBER++, System.currentTimeMillis(), OperatorStatus.SHIFTNOTSTARTED);
 			}
 
 			operatorScheduleChanged();
 
 			return true;
 		}
 
 		return false;
 	}
 
 	public boolean updateTimetable() throws Exception
 	{
 		return updateTimetable(true);// && updateTimetable(false);
 	}
 
 	public boolean updateTimetable(boolean isGreenLine) throws Exception
 	{
 		TrainRoute tr;
 		BlockSchedule bs;
 		Block block;
 		int trainNumber;
 		Iterator<TrainRoute> iTR;
 		Iterator<BlockSchedule> iBS;
 		
 		calculateRoutes(System.currentTimeMillis());
 		timetable = new Timetable();
 		
 		iTR = route.getIterator();
 
 		while(iTR.hasNext())
 		{
 			tr = iTR.next();
 			trainNumber = tr.getTrainNumber();
 
 			for(int i = 0; i < tr.size(); i++)
 			{
 				bs = tr.getIndex(i);
 				block = bs.getBlock();
 
				if((i % 2) == 1)//block.isStation())
 				{
 					timetable.add("Station " + i/*block.getStationName()*/, trainNumber, (bs.getEntryTime() + bs.getExitTime()) / 2, TrainStatus.ONTIME);
 				}
 			}
 		}
 
 		timetableChanged();
 
 		return true;
 	}
 
 	private int calculateOptimalTrains(boolean isGreenLine)
 	{
 		return 1;
 	}
 
 	private void calculateRoutes(long time) throws Exception
 	{
 		    route.routeTrains(time, greenTrains, schedule);
 		    //route.routeTrains(time, redTrains, schedule);
 		    //System.out.println("ROUTE! " + route);
 	}
 
 	//Searches train list for a train
 
 	private Train findTrain(int trainNumber, ArrayList<Train> trains)
 	{
 		int size = trains.size();
 
 		for(int i = 0; i < size; i++)
 		{
 			Train train = trains.get(i);
 
 			if(train.trainNumber == trainNumber)
 			{
 				return train;
 			}
 		}
 
 		return null;
 	}
 
 	public OperatorSchedule getOperatorSchedule()
 	{
 		return schedule;
 	}
 
 	public Timetable getTimetable()
 	{
 		return timetable;
 	}
 
 	/*
 		Thread loop
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
 					//System.out.println("\nRECEIVED MESSAGE: source->" + message.getSource() + " : dest->" + message.getDest() + "\n");
 					try
 					{
 					switch(message.getType())
 					{
 						case CTC_Sch_Generate_Schedule:
 							receivedTimetableUpdateRequest(message);
 							break;
 						/*case:
 							receivedGPSLocation(message);
 							break;*/
 						case TnMd_Sch_Station_Arrival:
 							receivedTrainArrival(message);
 							break;
 						case TnMd_Sch_Notify_Yard:
 							if((boolean)message.getData().get("entry"))
 							{
 								receivedTrainReturn(message);
 								sendTrainInfo();
 							}
 							else
 							{
 								receivedTrainDispatch(message);
 								sendTrainInfo();
 							}
 							break;	
 					}
 					}
 					catch(Exception e)
 					{
 						e.printStackTrace();
 					}
 				}
 				else
 				{
 					//System.out.println("PASSING MESSAGE " + message.getType() + ": step->" + name + " source->" + message.getSource() + " dest->" + message.getDest());
 					message.updateSender(name);
 					Environment.passMessage(message);
 				}
 			}
 
 			//Checks timetable to see if train is late to arrive at a station
 
 			boolean timetableChanged = false;
 
 			for(int i = 0; i < timetable.size(); i++)
 			{
 				TimesObject time = timetable.getTimesObject(i);
 
 				if(time.status != TrainStatus.ARRIVED && time.time < System.currentTimeMillis())
 				{
 					time.status = TrainStatus.LATE;
 					timetableChanged = true;
 				}
 			}
 
 			if(timetableChanged)
 			{
 				timetableChanged();
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
 
 	private void receivedTrainDispatch(Message message) throws Exception
 	{
 		int trainID;
 		Operator operator;
 
 		trainID = (int)message.getData().get("trainID");
 		//sendTrainUpdate(); //Notify MBO that a train was added to the track
 
 		Train train = new Train(trainID, System.currentTimeMillis());
 		train.setBlock(startingBlock, startingPreviousNode, startingNextNode, System.currentTimeMillis());
 		
 		if(message.getData().get("isGreenLine") != null && (boolean)message.getData().get("isGreenLine"))
 		{
 			greenTrains.add(train);
 		}
 		else
 		{
 			redTrains.add(new Train(trainID, System.currentTimeMillis()));
 		}
 			
 		operator = schedule.search(trainID);
 
 		//If train isn't in schedule, add it
 
 		if(operator == null)
 		{
 			Scheduler.NEXT_TRAIN_NUMBER = trainID;
 			schedule.add("Train", "Operator", Scheduler.NEXT_TRAIN_NUMBER++, System.currentTimeMillis(), OperatorStatus.SHIFTFIRSTHALF);
 			operatorScheduleChanged();
 		}
 
 		//If train is in schedule update status
 
 		else
 		{
 			if(operator.status == OperatorStatus.SHIFTNOTSTARTED)
 			{
 				operator.status = OperatorStatus.SHIFTFIRSTHALF;
 				operatorScheduleChanged();
 			}
 			else if(operator.status == OperatorStatus.ONBREAK)
 			{
 				operator.status = OperatorStatus.SHIFTSECONDHALF;
 				operatorScheduleChanged();
 			}
 		}
 
 		updateTimetable();
 	}
 
 	private void receivedTrainArrival(Message message)
 	{
 		int trainID;
 		String stationName;
 		TimesObject to;
 		Train train;
 
 		trainID = (int)message.getData().get("trainID");
 		stationName = (String)message.getData().get("trainID");
 
 		for(int i = 0; i < timetable.size(); i++)
 		{
 			to = timetable.getTimesObject(i);
 
 			if(to.status != TrainStatus.ARRIVED)
 			{
 				to.status = TrainStatus.ARRIVED;
 				break;
 			}
 		}
 
 		train = findTrain(trainID, greenTrains);
 
 		if(train == null)
 		{
 			train = findTrain(trainID, redTrains);
 		}
 
 		//train.setPassengerCount((int)message.getData().get("passengerCount"));
 		timetableChanged();		
 	}
 
 	private void receivedTimetableUpdateRequest(Message message) throws Exception
 	{
 		updateTimetable();
 		sendTimetable();
 	}
 
 	private void receivedTrainReturn(Message message)
 	{
 		int trainID;
 		Operator operator;
 		Train train;
 
 		trainID = (int)message.getData().get("trainID");
 		train = findTrain(trainID, greenTrains);
 
 		if(train == null)
 		{
 			train = findTrain(trainID, redTrains);
 
 			if(train != null)
 			{
 				redTrains.remove(trainID);
 			}
 		}
 		else
 		{
 			greenTrains.remove(trainID);
 		}
 
 		//sendTrainUpdate();
 
 		operator = schedule.search(trainID);
 
 		//Update status of operator
 
 		if(operator.status == OperatorStatus.SHIFTFIRSTHALF)
 		{
 			operator.status = OperatorStatus.ONBREAK;
 			operatorScheduleChanged();
 		}
 		else if(operator.status == OperatorStatus.SHIFTSECONDHALF)
 		{
 			operator.status = OperatorStatus.SHIFTENDED;
 			operatorScheduleChanged();
 		}
 	}
 
 	/*
 		Message Senders
 	*/
 
 	public void send(Message message)
 	{
 	    	//System.out.println("SENDING MESSAGE " + message.getType() + ": start->" + message.getSource() + " : dest->" + message.getDest() + "\n");
 		Environment.passMessage(message);
 	}
 
 	private void requestTrainGPS()
 	{
 		Iterator<Operator> i;
 		Message message;
 /*
 		i = schedule.getIterator();
 
 		while(i.hasNext())
 		{
 			message = new Message(name, name, Module.satellite);
 			message.addData("trainNumber", i.next().trainNumber);
 			send(message);
 		}
 */
 	}
 
 	//Notifies MBO a train has been added to or removed from the track
 /* Deprecated
 	private void sendTrainUpdate()
 	{
 		Message message;
 
 		message = new Message(name, name, Module.MBO, msg.Sch_MBO_Notify_Train_Added_Removed);
 		message.addData("id", 96);
 		message.addData("trainList", trains);
 		send(message);
 	}
 */
 
 
 	private void sendTrainInfo()
 	{
 		Message message;
 
 		message = new Message(name, name, Module.MBO, msg.Sch_MBO_Send_Train_Info);
 		message.addData("id", 96);
 		message.addData("greenLine", greenTrains);
 		message.addData("redLine", redTrains);
 		send(message);
 	}
 
 	private void sendTimetable()
 	{
 		Message message;
 
 		message = new Message(name, name, Module.CTC, msg.Sch_CTC_Send_Schedule);
 		message.addData("schedule", timetable);
 		send(message);
 	}
 
 	/*
 		Scheduler UI Communication
 	*/
 
 	public synchronized void addSchedulerListener(SchedulerListener listener)
 	{
 		listeners.add(listener);
 	}
 
 	public synchronized void removeSchedulerListener(SchedulerListener listener)
 	{
 		listeners.remove(listener);
 	}
 
 
 	//Events
 
 	private synchronized void operatorScheduleChanged()
 	{
 		for(int i = 0; i < listeners.size(); i++)
 		{
 			listeners.get(i).operatorScheduleChanged(new SchedulerEvent(this));
 		}
 	}
 
 	private synchronized void timetableChanged()
 	{
 		for(int i = 0; i < listeners.size(); i++)
 		{
 			listeners.get(i).timetableChanged(new SchedulerEvent(this));
 		}
 
 	}
 }
