 import java.util.LinkedList;
 import java.util.Queue;
 
 public class Station extends SubwaySpace {
 
 	private Queue<Person> passangersToStart;
 	private Queue<Person> passangersToEnd;	
 	private Train trainToStart;
 	private Long timeToLeaveToStart;
 	private Train trainToEnd;
 	private Long timeToLeaveToEnd;
 	private Line line;
 	
 	public Station(SubwaySpace nextToStart, SubwaySpace nextToEnd, Line line, long waitingDuration, String name) {
 		super(nextToStart, nextToEnd, waitingDuration, name);
 		this.passangersToStart = new LinkedList<Person>();
 		this.passangersToEnd = new LinkedList<Person>();
 		this.line = line;
 	}
 
 	public void personArrival(Person p) throws Exception {
 		if(line.getDirection(this, p.getDestiny()).equals(Train.Direction.TO_END))
 			passangersToEnd.add(p);
 		else
 			passangersToStart.add(p);
 	}
 	
 	public double getPersonsWaiting(Train.Direction direction){
 		if(direction.equals(Train.Direction.TO_END))
 			return passangersToEnd.size();
 		return passangersToStart.size();
 	}
 
 	@Override
 	public void trainArrival(Train train, Long timestamp) throws Exception {
 		System.out.println("++++++++++++++++++++++");
 		System.out.println("Train arriving to station: " + getName() + " direction:" + train.getDirection() + " passengers:" + train.getPassangers().size() + " time: " + timestamp + " leaving:" + (timestamp + getActivityDuration()));
 		System.out.println("++++++++++++++++++++++");
 		SubwayMap.getInstance().addSpace(this);
 		
 		// if I have a pending event this moment, first execute it in order to free the space for the new train
 		if(timeToLeaveToEnd == timestamp || timeToLeaveToStart == timestamp){
 			this.event(timestamp);
 			SimulatorScheduler.getInstance().deleteEvent(timestamp, this);
 		}
 		
 		if(train.getDirection().equals(Train.Direction.TO_END)){
 			if (trainToEnd != null)
 				throw new Exception("Trains crashed in" + getName() + "-> entered: " + train.getName() + " in station was: " + trainToEnd.getName());
 			trainToEnd = train;
 			timeToLeaveToEnd = timestamp + getActivityDuration();
 			SimulatorScheduler.getInstance().registerEvent(timeToLeaveToEnd, new SchedulerRegistrator(this, "train: " + trainToEnd + " will leave station: " + this + " at: " + timeToLeaveToEnd + " with direction: " + trainToEnd.getDirection()));
 			return;
 		}
 		if (trainToStart != null)
 			throw new Exception("Trains crashed in" + getName() + "-> entered: " + train.getName() + " in station was: " + trainToStart.getName());
 
 		trainToStart = train;
 		timeToLeaveToStart = timestamp + getActivityDuration();
 		SimulatorScheduler.getInstance().registerEvent(timeToLeaveToStart, new SchedulerRegistrator(this, "train: " + trainToStart + " will leave station: " + this + " at: " + timeToLeaveToStart + " with direction: " + trainToStart.getDirection()));
 	}
 	
 	public void setDefaultName(){
 		return;
 	}
 	
 	public String toString(){
 		return getName();
 	}
 	
 	@Override
 	public void event(Long timestamp) throws Exception {
 		removeFromMap();
 		System.out.println(".......... station event ................");
 		System.out.println("timeToLeaveStart:" + timeToLeaveToStart + " timeToLeaveToend:" + timeToLeaveToEnd + " time:" + timestamp);
 		if(timeToLeaveToStart != null && timeToLeaveToStart.equals(timestamp)){
 			if(trainToStart == null){
 				System.out.println("TrainToStart:" + trainToStart);
 				System.out.println("TrainToEnd:" + trainToEnd);
 				System.out.println("timestamp:" + timestamp);
				System.out.println("Station:" + getName());
 			}
 			trainToStart.descendPassengers(this);
 			while (passangersToStart.size() > 0 && trainToStart.passengerIn(passangersToStart.peek())) {
 				passangersToStart.poll();
 			}
 			getNextToStart().trainArrival(trainToStart, timestamp);
 			trainToStart = null;
 		}
 		
 		if(timeToLeaveToEnd != null && timeToLeaveToEnd.equals(timestamp)){
 			trainToEnd.descendPassengers(this);
 			while (passangersToEnd.size() > 0 && trainToEnd.passengerIn(passangersToEnd.peek())) {
 				passangersToEnd.poll();
 			}
 			getNextToEnd().trainArrival(trainToEnd, timestamp);
 			trainToEnd = null;
 		}
 		System.out.println(".................................");
 	}
 
 }
