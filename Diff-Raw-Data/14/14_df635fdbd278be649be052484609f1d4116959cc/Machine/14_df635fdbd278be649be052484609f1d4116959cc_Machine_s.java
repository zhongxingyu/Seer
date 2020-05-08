 package ddg.model;
 
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.PriorityQueue;
 import java.util.Set;
 
 import ddg.emulator.event.machine.FileSystemActivityEvent;
 import ddg.emulator.event.machine.ShutdownEvent;
 import ddg.emulator.event.machine.SleepEvent;
 import ddg.emulator.event.machine.UserActivityEvent;
 import ddg.emulator.event.machine.UserIdlenessEvent;
 import ddg.kernel.Event;
 import ddg.kernel.EventHandler;
 import ddg.kernel.Time;
 import ddg.kernel.Time.Unit;
 import ddg.model.data.DataServer;
 
 /**
  * Models a machine. This machine can hold a number of clients and data servers.
  * 
  * @author Patrick Maia - patrickjem@lsd.ufcg.edu.br
  * @author Ricardo Araujo Santos - ricardo@lsd.ufcg.edu.br
  * @author Thiago Emmanuel Pereira da Cunha Silva - thiagoepdc@lsd.ufcg.edu.br
  */
 public class Machine extends EventHandler {
 	
 	/*
 	 * The source of the values below is Lesandro's work: 
 	 * "On the Impact of Energy-saving Strategies in Opportunistic Grids"
 	 */
 	public static final double TRANSITION_POWER_IN_WATTS 	= 140;
 	public static final double ACTIVE_POWER_IN_WATTS 		= 140;
 	public static final double IDLE_POWER_IN_WATTS 			= 0; //FIXME need to discover the right value
 	public static final double SLEEP_POWER_IN_WATTS 		= 3.33;
 	public static final double SLEEP_TRANSITION_POWER 		= 0; //FIXME need to discover the right value
 	public static final double SHUTDOWN_TRANSITION_POWER 	= 0; //FIXME need to discover the right value
 	public static final Time   SLEEP_TRANSITION_DURATION 	= new Time(2500, Unit.MILLISECONDS);
 	public static final Time   SHUTDOWN_TRANSITION_DURATION	= new Time(60, Unit.SECONDS); //FIXME need to discover the right value
 	
 	private final Set<DataServer> deployedDataServers;
 	private final Set<FileSystemClient> clients;
 	
 	/**
 	 * The amount of time this machine must wait idle before sleep
 	 */
 	private final Time timeBeforeSleep;
 	/**
 	 * Machine's unique identification
 	 */
 	private final String id;
 	/**
 	 * A list that holds the events from the local file system that arrived while the machine was sleeping or turned off
 	 */
 	private List<FileSystemActivityEvent> pendingFSActivityEvents;
 	/**
 	 * The name of the state the machine is at a given moment
 	 */
 	private String currentStateName;
 	/**
 	 * The time in which this machine transitioned to the current state
 	 */
 	private Time currentStateStartTime;
 	/**
 	 * The time in which this machine will have its state changed again. 
 	 * Note that if the machine is in a state such as SLEEP or SHUTDOWN, a FileSystemActivityEvent can change the
 	 * machine's state before this time. That's why it's called supposedCurrentStateEndTime.  
 	 */
 	private Time supposedCurrentStateEndTime;
 	/**
 	 * The two fields below are used to keep track of file system activity that happened while the machine was idle 
 	 * (without user activity). The total time that the machine remained active while the user was idle is given by the
 	 * subtraction of fsActivityWhileIdleEndTime and fsActivityWhileIdleStartTime. 
 	 */
 	private long fsActivityWhileIdleStartTime = -1;
 	private long fsActivityWhileIdleEndTime = -1;
 	
 	/**
 	 * 
 	 * @param eventsGeneratedBySimulationQueue
 	 * @param id
 	 * @param timeBeforeSleep
 	 */
 	public Machine(PriorityQueue<Event> eventsGeneratedBySimulationQueue, String id, long timeBeforeSleep) {
 		
 		super(eventsGeneratedBySimulationQueue);
 		
 		this.id = id;
 		this.deployedDataServers = new HashSet<DataServer>();
 		this.clients = new HashSet<FileSystemClient>();
 		this.timeBeforeSleep = new Time(timeBeforeSleep, Unit.SECONDS);
 
 		currentStateName = ShutdownEvent.EVENT_NAME;
 		currentStateStartTime = Time.GENESIS;
 		supposedCurrentStateEndTime = Time.END_OF_THE_WORLD;
 		pendingFSActivityEvents = new ArrayList<FileSystemActivityEvent>();
 	}
 	
 	/**
 	 * @return the id
 	 */
 	public String getId() {
 		return id;
 	}
 
 	/**
 	 * @return the deployedDataServers
 	 */
 	public Set<DataServer> getDeployedDataServers() {
 		return deployedDataServers;
 	}
 
 	/**
 	 * @return
 	 */
 	public Set<FileSystemClient> getDeployedClients() {
 		return clients;
 	}
 
 	/**
 	 * @param newClient
 	 * @return
 	 */
 	public boolean bindClient(FileSystemClient newClient) {
 		return clients.add(newClient);
 	}
 
 	/**
 	 * @param newDataServer
 	 * @return
 	 */
 	public boolean deploy(DataServer newDataServer) {
 		return deployedDataServers.add(newDataServer);
 	}
 
 	/**
 	 * @param newDataServer
 	 */
 	public boolean isDeployed(DataServer dataServer) {
 		return deployedDataServers.contains(dataServer);
 	}
 	
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public String toString() {
 		return "machine" + id;
 	}
 	
 	@Override
 	public void handleEvent(Event event) {
 		if(event instanceof UserIdlenessEvent) {
 			handleUserIdleness((UserIdlenessEvent) event);
 		} else if(event instanceof UserActivityEvent) {
 			handleUserActivity((UserActivityEvent) event);
 		} else if(event instanceof SleepEvent) {
 			handleSleep((SleepEvent) event);
 		} else if(event instanceof ShutdownEvent) {
 			handleShutdown((ShutdownEvent) event);
 		} else if(event instanceof FileSystemActivityEvent) {
 			handleFileSystemActivityEvent((FileSystemActivityEvent) event);
 		} else {
 			throw new IllegalArgumentException(event.toString());
 		}
 		
 	}
 	
 	private void handleFileSystemActivityEvent(FileSystemActivityEvent fsActivity) {
 		/* The new FileSystemActivityEvent must be scheduled to one second after the transition to the next state.
 		 * This is done to make sure the event that changes the state was already handled.
 		 */
 		Time oneSecond = new Time(1, Unit.SECONDS);
 		Time now = fsActivity.getScheduledTime();		
 		
 		if(currentStateName.equals(ShutdownEvent.EVENT_NAME) || currentStateName.equals(SleepEvent.EVENT_NAME)) {
 			if(fsActivity.isFromLocalFSClient()) {
 				pendingFSActivityEvents.add(fsActivity);
 			} else {
 				UserIdlenessEvent idlenessEvent = null;
 				
 				if(currentStateName.equals(ShutdownEvent.EVENT_NAME)) {
 					idlenessEvent = buildUserIdlenessEventToWakeUp(now, SHUTDOWN_TRANSITION_DURATION);
 				} else {
 					idlenessEvent = buildUserIdlenessEventToWakeUp(now, SLEEP_TRANSITION_DURATION);
 				}
 				
 				Time fsActivityEventStartTime = supposedCurrentStateEndTime.plus(oneSecond);
 				if(idlenessEvent != null) {
 					send(idlenessEvent);
 					fsActivityEventStartTime = idlenessEvent.getScheduledTime().plus(oneSecond);
 				}
 				
 				FileSystemActivityEvent newFSActivityEvent = new FileSystemActivityEvent(this, 
 						fsActivityEventStartTime, fsActivity.getDuration(), false);
 				
 				send(newFSActivityEvent);		
 			}
 		} else if(currentStateName.equals(UserActivityEvent.EVENT_NAME)) {
 			//if this fs event duration is greater than the duration of the current state
 			if(supposedCurrentStateEndTime.isEarlierThan(now.plus(fsActivity.getDuration()))) {
 				Time duration = now.plus(fsActivity.getDuration()).minus(supposedCurrentStateEndTime); 
 				FileSystemActivityEvent newFSActivityEvent = new FileSystemActivityEvent(this, 
 						supposedCurrentStateEndTime.plus(oneSecond), duration, 
 						fsActivity.isFromLocalFSClient());
 				
 				send(newFSActivityEvent);
 			}
 		} else if(currentStateName.equals(UserIdlenessEvent.EVENT_NAME)) {
 			if(fsActivityWhileIdleStartTime == -1) {
 				fsActivityWhileIdleStartTime = now.asMicroseconds();
 			}
 			
 			//if this fs event duration is greater than the duration of the current state
 			if(supposedCurrentStateEndTime.isEarlierThan(now.plus(fsActivity.getDuration()))) {
 				
 				fsActivityWhileIdleEndTime = supposedCurrentStateEndTime.asMicroseconds();
 								
 				FileSystemActivityEvent newFSActivityEvent = new FileSystemActivityEvent(this, 
 						supposedCurrentStateEndTime.plus(oneSecond), 
 						fsActivity.getDuration().minus(supposedCurrentStateEndTime.minus(now)), 
 						fsActivity.isFromLocalFSClient());
 				
 				send(newFSActivityEvent);
 			} else if(now.plus(fsActivity.getDuration()).asMicroseconds() > fsActivityWhileIdleEndTime) {
 					fsActivityWhileIdleEndTime = now.plus(fsActivity.getDuration()).asMicroseconds();
 			}
 		}
 	}
 	
 	private UserIdlenessEvent buildUserIdlenessEventToWakeUp(Time now, Time transitionDuration) {
 		Time idlenessStart = null;
 
 		//if the machine has not finished the transition to the current state yet	
 		if(now.minus(transitionDuration).isEarlierThan(currentStateStartTime)) {
 			idlenessStart = currentStateStartTime.plus(transitionDuration.times(2));	
 		} else if(now.plus(transitionDuration).isEarlierThan(supposedCurrentStateEndTime)) {
 			idlenessStart = now.plus(transitionDuration);
 		} else { //the machine is already waking up
 			return null;
 		}
 		
 		Time userIdlenessDuration = supposedCurrentStateEndTime.minus(idlenessStart);
 		
 		UserIdlenessEvent idlenessEvent = new UserIdlenessEvent(this, idlenessStart, userIdlenessDuration);
 		
 		return idlenessEvent;
 	}
 
 	private void handleShutdown(ShutdownEvent event) {
 		Aggregator aggregator = Aggregator.getInstance();
 		Time now = event.getScheduledTime();		
 		
 		Time currentStateActualDuration = now.minus(currentStateStartTime);
 		
 		if(currentStateName.equals(ShutdownEvent.EVENT_NAME)) {
 			throw new IllegalStateException(String.format("The machine %s was already turned off.", getId()));
 			
 		} else if(currentStateName.equals(SleepEvent.EVENT_NAME)) {
 			System.err.println(String.format("WARNING: A shutdown during a sleep time has occurred in machine %s at " +
 					"timestamp %d. We will keep the machine sleeping.", getId(), now.asMicroseconds()));
 			
 		} else if(currentStateName.equals(UserActivityEvent.EVENT_NAME)) {
 			aggregator.aggregateActiveDuration(getId(), currentStateActualDuration);
 			
 		} else if(currentStateName.equals(UserIdlenessEvent.EVENT_NAME)) {
 			aggregateIdlenessPeriod(currentStateActualDuration);
 			
 		}
 		
 		currentStateName = ShutdownEvent.EVENT_NAME;
 		currentStateStartTime = now;
 		supposedCurrentStateEndTime = currentStateStartTime.plus(event.getDuration());
 	}
 	
 	private void aggregateIdlenessPeriod(Time idlenessDuration) {
 		Aggregator aggregator = Aggregator.getInstance();
 		
 		Time fsActivityWhileIdleDuration = new Time(fsActivityWhileIdleEndTime - fsActivityWhileIdleStartTime, 
 				Unit.MICROSECONDS);
 		aggregator.aggregateIdleDuration(getId(), idlenessDuration.minus(fsActivityWhileIdleDuration));
 		aggregator.aggregateActiveDuration(getId(), fsActivityWhileIdleDuration);
 		
 		fsActivityWhileIdleEndTime = -1;
 		fsActivityWhileIdleStartTime = -1;
 	}
 
 	private void handleUserActivity(UserActivityEvent event) {
 		
 		Aggregator aggregator = Aggregator.getInstance();
 		Time now = event.getScheduledTime();		
 		
 		Time currentStateActualDuration = now.minus(currentStateStartTime);
 		
 		if(currentStateName.equals(ShutdownEvent.EVENT_NAME)) {
 			aggregator.aggregateShutdownDuration(getId(), currentStateActualDuration);
 
 		} else if(currentStateName.equals(SleepEvent.EVENT_NAME)) {
 			aggregator.aggregateSleepingDuration(getId(), currentStateActualDuration);
 			
 		} else if(currentStateName.equals(UserActivityEvent.EVENT_NAME)) {
 			throw new IllegalStateException(String.format("The machine %s was already in activity.", getId()));
 			
 		} else if(currentStateName.equals(UserIdlenessEvent.EVENT_NAME)) {
 			aggregateIdlenessPeriod(currentStateActualDuration);
 			
 		}
 		
 		currentStateName = UserActivityEvent.EVENT_NAME;
 		currentStateStartTime = now;
 		supposedCurrentStateEndTime = currentStateStartTime.plus(event.getDuration());
 		
 		handlePendingFileSystemActivityEvents();
 	}
 	
 	private void handleUserIdleness(UserIdlenessEvent event) {
 		Aggregator aggregator = Aggregator.getInstance();
 		Time idlenessDuration = event.getDuration();
 		Time now = event.getScheduledTime();
 		
 		Time currentStateActualDuration = now.minus(currentStateStartTime);
 		
 		if(currentStateName.equals(ShutdownEvent.EVENT_NAME)) {
 			aggregator.aggregateShutdownDuration(getId(), currentStateActualDuration);
 
 		} else if(currentStateName.equals(SleepEvent.EVENT_NAME)) {
 			aggregator.aggregateSleepingDuration(getId(), currentStateActualDuration);
 			
 		} else if(currentStateName.equals(UserActivityEvent.EVENT_NAME)) {
 			aggregator.aggregateActiveDuration(getId(), currentStateActualDuration);
 			
 		} else if(currentStateName.equals(UserIdlenessEvent.EVENT_NAME)) {
 			// if the fs wakes the machine up it is possible that we have idle to idle transition
 			aggregateIdlenessPeriod(currentStateActualDuration);
 		}
 		
 		currentStateName = UserIdlenessEvent.EVENT_NAME;
 		currentStateStartTime = now;
 		
 		if(idlenessDuration.isEarlierThan(timeBeforeSleep)) {
 			supposedCurrentStateEndTime = currentStateStartTime.plus(event.getDuration());
 		} else {
 			Time bedTime = now.plus(timeBeforeSleep);
 			Time sleepDuration = now.plus(idlenessDuration).minus(bedTime);
 			
 			//we must sleep only if we have time to wake up
 			if(!sleepDuration.isEarlierThan(SLEEP_TRANSITION_DURATION.times(2))) { 
 				send(new SleepEvent(this, bedTime, sleepDuration));
 				
 				supposedCurrentStateEndTime = currentStateStartTime.plus(timeBeforeSleep);
 			} else {
 				supposedCurrentStateEndTime = currentStateStartTime.plus(event.getDuration());
 			}
 		}
 		
 		handlePendingFileSystemActivityEvents();
 	}
 	
 	private void handleSleep(SleepEvent event) {
 		Aggregator aggregator = Aggregator.getInstance();
 		Time now = event.getScheduledTime();		
 		
 		Time currentStateActualDuration = now.minus(currentStateStartTime);
 		
 		if(currentStateName.equals(ShutdownEvent.EVENT_NAME)) {
 			aggregator.aggregateShutdownDuration(getId(), currentStateActualDuration);
 
		} else if(currentStateName.equals(SleepEvent.EVENT_NAME)) {
			throw new IllegalStateException(String.format("The machine %s was already sleeping.", getId()));
			
 		} else if(currentStateName.equals(UserActivityEvent.EVENT_NAME)) {
 			aggregator.aggregateActiveDuration(getId(), currentStateActualDuration);
 			
 		} else if(currentStateName.equals(UserIdlenessEvent.EVENT_NAME)) {
 			aggregateIdlenessPeriod(currentStateActualDuration);
 		}
 		
 		currentStateName = SleepEvent.EVENT_NAME;
 		currentStateStartTime = now;
 		supposedCurrentStateEndTime = currentStateStartTime.plus(event.getDuration());
 	}
 	
 	private void handlePendingFileSystemActivityEvents() {
 		for(FileSystemActivityEvent fsActivityEvent : pendingFSActivityEvents) {
 			handleFileSystemActivityEvent(fsActivityEvent);
 		}
 		
 		pendingFSActivityEvents.clear();
 	}
 	
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see java.lang.Object#hashCode()
 	 */
 	@Override
 	public int hashCode() {
 
 		final int prime = 31;
 		int result = 1;
 		result = prime * result + id.hashCode();
 		return result;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see java.lang.Object#equals(java.lang.Object)
 	 */
 	@Override
 	public boolean equals(Object obj) {
 
 		if (this == obj)
 			return true;
 		if (obj == null)
 			return false;
 		if (getClass() != obj.getClass())
 			return false;
 		Machine other = (Machine) obj;
 		if (id != other.id)
 			return false;
 		return true;
 	}
 }
