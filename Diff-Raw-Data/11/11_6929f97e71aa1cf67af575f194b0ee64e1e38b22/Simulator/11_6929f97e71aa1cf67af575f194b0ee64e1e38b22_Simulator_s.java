 package simulation;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Random;
 import java.util.Vector;
 
 import simulation.communication.Communication;
 import simulation.communication.Message;
 import simulation.definition.EventScheduler;
 import simulation.definition.TransactionalEventHandler;
 import simulation.eventHandlers.AddToAnimationHandler;
 import simulation.eventHandlers.ArrivalHandler;
 import simulation.eventHandlers.EndLandingHandler;
 import simulation.eventHandlers.EndTakeOffHandler;
 import simulation.eventHandlers.EnterAirspaceHandler;
 import simulation.eventHandlers.LeaveAirspaceHandler;
 import simulation.eventHandlers.ProcessQueuesHandler;
 import simulation.eventHandlers.ReadyForDepartureHandler;
 import simulation.eventHandlers.RemoveFromAnimationHandler;
 import simulation.eventHandlers.RepaintAnimationHandler;
 import simulation.eventHandlers.StartLandingHandler;
 import simulation.eventHandlers.StartTakeOffHandler;
 import simulation.gui.Animation;
 import simulation.gui.LogGui;
 import simulation.logic.Clock;
 import simulation.model.Aircraft;
 import simulation.model.Airport;
 import simulation.model.Event;
 import simulation.model.Flight;
 import simulation.model.SimWorld;
 
 /**
  * @author ps Manages simulation clock, a event queue and world consisting of
  *         aircrafts and airports
  */
 public class Simulator implements EventScheduler {
 
 	private Clock clock = new Clock();
 	private SimWorld world;
 	private LogGui logGui;
 	private int idofProcessor = 0;
 	private boolean isMaster = false;
 	private List<Event> evList; // time ordered list
 	private Animation animation;
 	private int totalProcessors = 1;
 	private String[] airportNames = { "ZURICH", "GENF", "BASEL", "BERNE" };
 	private Communication communication;
 	private Map<Integer, TransactionalEventHandler> eventHandlers = new HashMap<Integer, TransactionalEventHandler>();
 	private List<Event> processedEvents;
 
 	public Simulator(SimWorld world) {
 		this.world = world;
 		evList = new ArrayList<Event>();
 		processedEvents = new Vector<Event>();
 		eventHandlers.put(Event.READY_FOR_DEPARTURE, new ReadyForDepartureHandler());
 		eventHandlers.put(Event.START_TAKE_OFF, new StartTakeOffHandler());
 		eventHandlers.put(Event.END_TAKE_OFF, new EndTakeOffHandler());
 		eventHandlers.put(Event.ARRIVAL, new ArrivalHandler());
 		eventHandlers.put(Event.START_LANDING, new StartLandingHandler());
 		eventHandlers.put(Event.END_LANDING, new EndLandingHandler());
 		eventHandlers.put(Event.PROCESS_QUEUES, new ProcessQueuesHandler());
 		eventHandlers.put(Event.ADD_TO_ANIMATION, new AddToAnimationHandler());
 		eventHandlers.put(Event.REMOVE_FROM_ANIMATION, new RemoveFromAnimationHandler());
 		eventHandlers.put(Event.REPAINT_ANIMATION, new RepaintAnimationHandler());
 		eventHandlers.put(Event.LEAVE_AIRSPACE, new LeaveAirspaceHandler());
 		eventHandlers.put(Event.ENTER_AIRSPACE, new EnterAirspaceHandler());
 
 	}
 
 	public Simulator(SimWorld world, boolean bMasterProcess, int idofProcessor, int totalProcessors) {
 		this(world);
 		this.idofProcessor = idofProcessor;
 		this.isMaster = bMasterProcess;
 		this.totalProcessors = totalProcessors;
 	}
 
 	public int getIdOfProcessor() {
 		return idofProcessor;
 	}
 
 	/*
 	 * New events which lay in the past cause a causality error
 	 * 
 	 * @see EventScheduler#scheduleEvent(Event)
 	 */
 	public void scheduleEvent(Event e) {
 
 		if (e.getType() == Event.ENTER_AIRSPACE) {
 			// pass aircraft on to next airport
 			communication.send(e, e.getAirCraft());
 		} else {
 			// handle event locally
 			insertEvent(e);
 			if (evList.size() <= 1) {
				Event eNew = new Event(Event.REPAINT_ANIMATION, e.getTimeStamp() + Clock.REPAINT_GAP, null, null);
 				scheduleEvent(eNew);
 
 				logGui.println("Start paint animation" + e.toString());
 			}
 
 			if (clock.isInPast(e.getTimeStamp())) {
 				throw new RuntimeException("Causality error: " + e + "tim: " + e.getTimeStamp()
 						+ " currentSimulationTime" + clock.currentSimulationTime());
 			}
 
 		}
 
 	}
 
 	private void insertEvent(Event e) {
 		// antimessage and normal message cancel each other out
 		if (evList.contains(e)) {
 			evList.remove(e);
 			return;
 		}
 		int pos = 0;
 		while (pos < evList.size()) {
 			Event n = evList.get(pos);
 			if (n.getTimeStamp() > e.getTimeStamp())
 				break;
 			pos++;
 		}
 
 		evList.add(pos, e);
 	}
 
 	/**
 	 * rollback all processed events that are newer than the stragglerEvent
 	 * 
 	 * @param stragglerEvent
 	 */
 	private void doRollback(Event stragglerEvent) {
 		clock.rollbackTo(stragglerEvent.getTimeStamp());
 		for (int i = processedEvents.size() - 1; i >= 0; i--) {
 			Event event = processedEvents.get(i);
 			if (!clock.isInPast(event.getTimeStamp()))
 				rollbackEvent(event);
 		}
 	}
 
 	private void rollbackEvent(Event event) {
 		System.out.println(getIdOfProcessor() + ": Rolling back event " + event);
 		eventHandlers.get(event.getType()).rollback(event, this);
 		processedEvents.remove(event);
 		insertEvent(event);
 	}
 
 	/**
 	 * Advances the time to the time of the oldest event in the event queue and
 	 * processes the event
 	 */
 	public void processNextEvent() {
 		Event e;
 		final Message message = communication.receive();
 		if (message != null) {
 			e = message.getEvent(world);
 			if (clock.isInPast(e.getTimeStamp())) {
 				// we received a straggler message, roll back everything up to
 				// its timestamp
 				doRollback(e);
 			}
 			insertEvent(e);
 		}
 		e = getNextEvent();
		if (e == null)	throw new RuntimeException("Event was null!");
		if(clock.isInPast(e.getTimeStamp())) throw new RuntimeException("event is in past");
 		if (e.getTimeStamp() > clock.currentSimulationTime()) {
 			clock.sleepUntil(e.getTimeStamp());
 		}
 
 		processEvent(e);
 	}
 
 	private Event getNextEvent() {
 		for (Event event : evList) {
 			if (!event.isAntiMessage())
 				return event;
 		}
 		return null;
 	}
 
 	private void processEvent(final Event e) {
 		System.out.println(getIdOfProcessor() + ": Processing event " + e);
 		logGui.println("Process next event:" + e);
 		eventHandlers.get(e.getType()).process(e, this);
 		moveToProcessedQueue(e);
 	}
 
 	private void moveToProcessedQueue(final Event e) {
 		processedEvents.add(e);
 		evList.remove(e);
 	}
 
 	/**
 	 * This is the main simulation loop
 	 */
 	public void runSimulation() {
 		int evCnt = 0;
 		while (evList.size() > 0) {
 			processNextEvent();
 			evCnt++;
 		}
 		logGui.println("Processed " + evCnt + " events.");
 	}
 
 	public void initWorld() {
 		initWorld(100);
 	}
 
 	public void initWorld(int amountOfFlights) {
 
 		// Random Generator:
 		Random rand = new Random(1234);
 
 		initAirports();
 
 		// create 100 aircrafts and choose an arbitrary airport
 		for (int i = 0; i < amountOfFlights; i++) {
 			// Random Airport:
 			Airport ap = world.getAirport(airportNames[rand.nextInt(airportNames.length)]);
 			Aircraft ac = new Aircraft("X" + 1000 + i, ap);
 
 			// add the aircraft only to the wolrd where its base airport is.
 			if (ap.getAirportId() % getTotalProcessors() == getIdOfProcessor()) {
 				world.addAircraft(ac);
 				System.out.println("aircraft " + ac.getName() + " for processor " + getIdOfProcessor() + " located at "
 						+ ap.getName());
 			}
 		}
 		// create FlightPlans for all aircrafts
 		// for (int i = 0; i < world.getAircrafts().size(); i++) {
 		// Random Airport:
 		// Aircraft ac = world.getAircraft("X"+1000+i);
 
 		for (String key : world.getAircrafts().keySet()) {
 			Aircraft ac = world.getAircrafts().get(key);
 
 			// first Flight:
 			int amountOfAps = world.getAirports().size();
 			Airport ap = world.getAirport(airportNames[rand.nextInt(amountOfAps)]);
 			while (ap == ac.getCurrentAirPort()) {
 				ap = world.getAirport(airportNames[rand.nextInt(amountOfAps)]);
 			}
 			int scheduleTime = rand.nextInt(10000);
 			logGui.println("SchedulTime for Aircraft: :" + key + " is " + scheduleTime);
 			Flight f = new Flight(scheduleTime, ap);
 			ac.getFlightPlan().addFlight(f);
 			// Return flight
 			// f = new Flight(rand.nextInt(1000), ac.getCurrentAirPort());
 		}
 
 		// schedule initial events
 		// for (int i = 0; i < amountOfFlights; i++) {
 
 		for (String key : world.getAircrafts().keySet()) {
 			Aircraft ac = world.getAircrafts().get(key);
 			// Aircraft ac = world.getAircraft("X"+1000+i);
 			Flight f = ac.getFlightPlan().removeNextFlight();
 			if (f != null) {
 				ac.setDestination(f.getDestination());
 				Airport ap = ac.getCurrentAirPort();
 				Event e = new Event(Event.READY_FOR_DEPARTURE, f.getTimeGap(), ap, ac);
 				scheduleEvent(e);
 			}
 		}
 	}
 
 	private void initAirports() {
 		// create airports
 
 		Airport ap = new Airport("ZURICH", 684000, 256000, 683000, 259000, 60000, 45000);
 		world.addAirport(ap);
 		ap = new Airport("GENF", 497000, 120000, 499000, 122000, 50000, 100000);
 		world.addAirport(ap);
 		ap = new Airport("BASEL", 599000, 287000, 601000, 288000, 40000, 45000);
 		world.addAirport(ap);
 
 		ap = new Airport("BERNE", 550000, 207000, 552000, 208000, 40000, 100000);
 		world.addAirport(ap);
 
 	}
 
 	private int getTotalProcessors() {
 		return this.totalProcessors;
 	}
 
 	public void initGui() {
 		logGui = new LogGui();
 		logGui.init(this);
 
 		animation = Animation.init(this, clock);
 		animation.setVisible(true);
 	}
 
 	public SimWorld getSimWorld() {
 		return world;
 	}
 
 	public boolean isMaster() {
 		return isMaster;
 	}
 
 	public void setCommunication(Communication communication) {
 		this.communication = communication;
 	}
 
 	@Override
 	public List<Event> getEventList() {
 		return evList;
 	}
 
 }
