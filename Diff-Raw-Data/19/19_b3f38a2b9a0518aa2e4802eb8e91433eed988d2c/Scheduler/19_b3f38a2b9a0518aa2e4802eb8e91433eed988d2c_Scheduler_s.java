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
 	private ArrayList<Train> trains;
 
 	public static void main(String[] args)
 	{
 
 	}
 
 	/*
 		Constructor
 	*/
 
 	public Scheduler()
 	{
 		timetable = new Timetable();
 		schedule = new OperatorSchedule();
 		trains = new ArrayList<Train>();
 		listeners = new ArrayList<SchedulerListener>();
 		messages = new LinkedBlockingQueue<Message>();
 		this.name = Module.scheduler;
 		new SchedulerViewModel(this);
 
 		updateOperatorSchedule();
 		updateTimetable();
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
 
 		numTrains = calculateOptimalTrains(); //Get minimum number of trains that should be on the track
 
 		if(trains.size() < numTrains)
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
 
 	public boolean updateTimetable()
 	{
 		timetableChanged();
 
 		return true;
 	}
 
 	private int calculateOptimalTrains()
 	{
 		return 1;
 	}
 
 	private void calculateRoutes(int time)
 	{
 		while(time < 36000000)
 		{
 			
 			time = time +1000;
 		}
 	}
 
 	//Searches train list for a train
 
 	private Train findTrain(int trainNumber)
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
 
 					//System.out.println("\nRECEIVED MESSAGE " + message.getType() +": source->" + message.getSource() + " : dest->" + message.getDest() + "\n");
 					//System.out.println("\nRECEIVED MESSAGE: source->" + message.getSource() + " : dest->" + message.getDest() + "\n");
 
 					switch(message.getType())
 					{
 						case CTC_Sch_Generate_Schedule:
 							receivedTimetableUpdateRequest(message);
 							break;
 						/*case:
 							receivedGPSLocation(message);
 							break;*/
 						/*case 95:
 							receivedTrainArrival(message);
 							break;	*/
 						case TnMd_Sch_Notify_Yard:
 							if((boolean)message.getData().get("entry"))
 							{
 								receivedTrainReturn(message);
 							}
 							else
 							{
 								receivedTrainDispatch(message);
 							}
 							break;	
 					}
 				}
 				else
 				{
<<<<<<< HEAD
					// System.out.println("PASSING MESSAGE " + message.getType() + ": step->" + name + " source->" + message.getSource() + " dest->" + message.getDest());
=======
 					//System.out.println("PASSING MESSAGE " + message.getType() + ": step->" + name + " source->" + message.getSource() + " dest->" + message.getDest());
>>>>>>> bdc66616ebbb0c5b92b088ffa1771a825bc9ea04
 					//System.out.println("PASSING MESSAGE: step->" + name + " source->" + message.getSource() + " dest->" + message.getDest());
 					message.updateSender(name);
 					Environment.passMessage(message);
 				}
 			}
 
 			Iterator<TimesObject> i;
 
 			i = timetable.getIterator();
 
 			//Checks timetable to see if train is late to arrive at a station
 
 			while(i.hasNext())
 			{
 				TimesObject time = i.next();
 
 				if(time.status != TrainStatus.ARRIVED && time.time < System.currentTimeMillis())
 				{
 					time.status = TrainStatus.LATE;
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
 
 	private void receivedTrainDispatch(Message message)
 	{
 		int trainID;
 		Operator operator;
 
 		trainID = (int)message.getData().get("trainID");
 		trains.add(new Train(trainID, System.currentTimeMillis()));
 		sendTrainUpdate(); //Notify MBO that a train was added to the track
 
 		operator = schedule.search(trainID);
 
 		//If train isn't in schedule, add it
 
 		if(operator == null)
 		{
 			Scheduler.NEXT_TRAIN_NUMBER = trainID;
 			schedule.add("Train", "Operator", Scheduler.NEXT_TRAIN_NUMBER++, System.currentTimeMillis(), OperatorStatus.SHIFTFIRSTHALF);
 			operatorScheduleChanged();
 			updateTimetable();
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
 	}
 
 	private void receivedTrainArrival(Message message)
 	{
 	}
 
 	private void receivedTimetableUpdateRequest(Message message)
 	{
 		updateTimetable();
 		sendTimetable();
 	}
 
 	private void receivedTrainReturn(Message message)
 	{
 		int trainID;
 		Operator operator;
 
 		trainID = (int)message.getData().get("trainID");
 		trains.remove(findTrain(trainID));
 		sendTrainUpdate();
 
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
<<<<<<< HEAD
	    	// System.out.println("SENDING MESSAGE " + message.getType() + ": start->" + message.getSource() + " : dest->" + message.getDest() + "\n");
=======
	    	//System.out.println("SENDING MESSAGE " + message.getType() + ": start->" + message.getSource() + " : dest->" + message.getDest() + "\n");
>>>>>>> bdc66616ebbb0c5b92b088ffa1771a825bc9ea04
	    	//System.out.println("SENDING MESSAGE: start->" + message.getSource() + " : dest->" + message.getDest() + "\n");
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
 
 	private void sendTrainUpdate()
 	{
 		Message message;
 
 		message = new Message(name, name, Module.MBO, msg.Sch_MBO_Notify_Train_Added_Removed);
 		message.addData("id", 96);
 		message.addData("trainList", trains);
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
 		Iterator<SchedulerListener> i;
 		SchedulerEvent e;
 
 		e = new SchedulerEvent(this);
 		i = listeners.iterator();
 
 		while(i.hasNext())
 		{
 			i.next().operatorScheduleChanged(e);
 		}
 	}
 
 	private synchronized void timetableChanged()
 	{
 
 		Iterator<SchedulerListener> i;
 		SchedulerEvent e;
 
 		e = new SchedulerEvent(this);
 		i = listeners.iterator();
 
 		while(i.hasNext())
 		{
 			i.next().timetableChanged(e);
 		}
 
 	}
 }
