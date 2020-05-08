 package kop.game;
 
 import kop.cargo.*;
 import kop.company.Company;
 import kop.map.routecalculator.*;
 import kop.ports.*;
 import kop.serialization.ModelSerializer;
 import kop.serialization.SerializationException;
 import kop.ships.*;
 import kop.ships.engine.EngineList;
 import kop.ships.model.ShipModel;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.text.SimpleDateFormat;
 import java.util.*;
 
 /**
  * Main class for the application. It contains almost all high-level logic and all references.
  * TODO we might want to delegate out even more things here
  */
 public class Game {
 	private List<Company> companies;
 	// 1970-01-01 00:00 + currentHour
 	private GregorianCalendar calendar;
 	private static volatile Game instance;
 	private PortsOfTheWorld worldPorts;
 	private ShipClassList shipClasses;
 	private FreightMarket freightMarket;
 	private boolean paused = false;
 	private Company playerCompany;
 	private String playerName;
 	private EngineList engineList;
 	private Logger logger;
 
 	/**
 	 * TODO implement listeners where applicable, mainly UI. Work has started, but more can be done.
 	 */
 	private List<GameStateListener> listeners;
 	private Random random;
 	private Map<Company, List<Freight>> deliveredFreights;
 	private NewWorld world;
 	// TODO hard-coded interest value.
 	private double interestRate = 4.0;
 
 	/**
 	 * this is used to avoid cyclic references within the code
 	 * ie a call to Game.getInstance() from code run by the Game() constructor.
 	 * this creates a chicken-and-egg scenario.
 	 */
 	private static boolean creationRunning = false;
 
 	/**
 	 * This constructor isn't meant to be used by the outside world
 	 * @see .getInstance()
 	 */
 	public Game() {
 		Game.creationRunning = true;
 
 		logger = LoggerFactory.getLogger(this.getClass());
 		try {
 			worldPorts = new PortsOfTheWorld();
 			populatePorts();
 		} catch (SerializationException e) {
 			logger.error("Failed to populate ports", e);
 		}
 
 		calendar = new GregorianCalendar(1970,0,0,0,0);
 		freightMarket = new FreightMarket(this);
 
 		companies = new ArrayList<Company>();
 		resetPlayerCompany();
 		listeners = new ArrayList<GameStateListener>();
 		// TODO randseed will be zero for reproducing reasons. Fix this later!
 		random = new Random(0);
 		deliveredFreights = new HashMap<Company, List<Freight>>();
 
 		populateShipClasses();
 		Game.creationRunning = false;
 	}
 
 	/**
 	 * Registers listener for game update events.
 	 * @param listener
 	 */
 	public void addListener(GameStateListener listener) {
 		listeners.add(listener);
 	}
 
 	/**
 	 * Creates the static instance.
 	 */
 
 	private static void createInstance() {
 		if (creationRunning) {
 			// a chicken-and-egg situation has occurred, which means that we have a cyclic reference somewhere in our code.
 			throw new ConcurrentModificationException("An object tried to retrieve a Game instance from within the Game() constructor.");
 		}
 		instance = new Game();
 	}
 
 	/**
 	 * Deserialises shipclasses to a list.
 	 */
 
 	private void populateShipClasses() {
 		try {
 			shipClasses = (ShipClassList) ModelSerializer.readFromFile("kop/ships/shipclasses.xml", ShipClassList.class);
 		} catch (SerializationException e) {
 			logger.error("Could not populate ship classes list", e);
 		}
 	}
 
 	/**
 	 * Deserialises ports to a list.
 	 * TODO why is this different from populateShipClasses?
 	 */
 
 	private void populatePorts() throws SerializationException {
 		worldPorts.populatePorts();
 	}
 
 	/**
 	 * Factory method for the Game singleton.
 	 * @return The singleton we use everywhere.
 	 */
 	public static Game getInstance() {
 		if (instance==null) {
 			createInstance();
 		}
 
 		return instance;
 	}
 
 	public Date getCurrentDate() {
 		return calendar.getTime();
 	}
 
 	/**
 	 * This method is meant to be the main loop when we're running the UI, which will in turn listen for events
 	 */
 
 	public void mainLoop() {
 		while (isRunning()) {
 			stepTime();
 		}
 	}
 
 	/**
 	 * The innermost method in the application, it increments time by one hour and modifies the internal state accordingly.
 	 */
 
 	public void stepTime() {
 		calendar.add(Calendar.HOUR, 1);
 
 		for (Company c: companies) {
 
 			try {
 				c.moveShips();
 			} catch (OutOfFuelException e) {
 				// TODO do something here.
 			}
 
 			// a new day dawns for the current company.
 			if (calendar.get(Calendar.HOUR) == 0) {
 				if (deliveredFreights.get(c) != null) {
 					for (Freight f: deliveredFreights.get(c)) {
 						c.addMoney(f.getCargo().getTotalPrice());
 					}
 				}
 				c.doDailyCosts();
 				if (calendar.get(Calendar.DAY_OF_MONTH)==0) {
 					// a new month!
 					c.doMonthlyCosts();
 				}
 			}
 		}
 		// a new day dawns for the entire world.
 		if (calendar.get(Calendar.HOUR) == 0) {
 			generateDailyFreights();
 		}
 
 		fireGameStateChanged();
 	}
 
 	private void fireGameStateChanged() {
 		for (GameStateListener listener:listeners) {
 			listener.stateChanged();
 		}
 	}
 
 	/**
 	 * Creates new freights.
 	 * TODO rewrite needed, use the new Port exports information.
 	 */
 
 	public void generateDailyFreights() {
 		for (Port p: worldPorts.getPortsAsList()) {
 			Cargo cargo = null;
 			for (PortCargoType portCargoType: p.getPortCargoTypes()) {
 				for (PortCargoTypeDestination destination: portCargoType.getDestinations()) {
 					cargo = freightMarket.generateCargoPerYearlyAmount(portCargoType.getType(),
 							destination.getYearlyAmount());
 					freightMarket.generateFreight(p.getProxy(),
 							destination.getPortProxy(),
 							cargo);
 				}
 			}
 
 		}
 	}
 
 	public boolean isRunning() {
 		return !paused;
 	}
 
 	public Port getPortByName(String portName) throws NoSuchPortException {
 		return worldPorts.getPortByName(portName);
 	}
 
 	public Port getPortByUnlocode(String unlocode) throws NoSuchPortException {
 		return worldPorts.getPortByUnlocode(unlocode);
 	}
 
 	public Company getPlayerCompany() {
 		return playerCompany;
 	}
 
 	public void setPlayerCompanyName(String text) {
 		getPlayerCompany().setName(text);
 	}
 
 	public void setPlayerName(String text) {
 		playerName = text;
 	}
 
 	/**
 	 * Returns the singleton engine list.
 	 * TODO why is the engine list instance created in the getter when the other lists have their own factory methods?
 	 * @return
 	 */
 
 	public EngineList getEngineList() {
 		if (engineList == null) {
 			try {
 				engineList = (EngineList) ModelSerializer.readFromFile("kop/ships/engines.xml", EngineList.class);
 			} catch (SerializationException e) {
 				e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
 			}
 		}
 
 		return engineList;
 	}
 
 	public void setDate(Date d) {
 		calendar.setTime(d);
 	}
 
 	public String getCurrentDateAsString() {
 		String format = new SimpleDateFormat().format(calendar.getTime());
 		return format;
 	}
 
 	public ShipClassList getShipClasses() {
 		return shipClasses;
 	}
 
 	public Date getFutureDate(long days) {
 		return new Date(calendar.getTime().getTime() + (days * 3600 * 24 * 1000));
 	}
 
 	public Random getRandom() {
 		return random;
 	}
 
 	protected PortsOfTheWorld getWorldPorts() {
 		return worldPorts;
 	}
 
 	public FreightMarket getFreightMarket() {
 		return freightMarket;
 	}
 
 	public void addDeliveredFreights(Company c, Freight f) {
 		if (deliveredFreights.get(c) == null) {
 			deliveredFreights.put(c, new ArrayList<Freight>());
 		}
 		deliveredFreights.get(c).add(f);
 	}
 
 	public Collection<?> getDeliveredFreights(Company company) {
 		return deliveredFreights.get(company);
 	}
 
 	/**
 	 * If the next time step is a new day, this returns true.
 	 * @return
 	 */
 	public boolean isNextTimeStepNewDay() {
 		return calendar.get(Calendar.HOUR) == calendar.getActualMaximum(Calendar.HOUR);
 	}
 
 	public boolean isNextTimeStepNewMonth() {
 		return isNextTimeStepNewDay() &&
 				calendar.get(Calendar.DAY_OF_MONTH) == calendar.getActualMaximum(calendar.get(Calendar.MONTH));
 	}
 
 	public final void resetPlayerCompany() {
 		if (companies.size() == 0) {
 			companies.add(new Company());
 		} else {
 			companies.set(0, new Company());
 		}
 
 		playerCompany = companies.get(0);
 	}
 
 	public ASRoute getRoute(PortProxy origin, PortProxy destination, ShipModel ship) throws NoRouteFoundException {
 		AStarUtil util = new AStarUtil();
 		ASDistance distance = null;
 		try {
 			distance = util.aStar(origin, destination, getWorld());
 		} catch (CouldNotFindPointException e) {
 			// TODO should throw a NoRouteFoundException
 			logger.error("Could not find point", e);
			throw new NoRouteFoundException(e);
 		}
 
 		return distance.shortestRoute(ship);
 	}
 
 	public double getInterestRate() {
 		return interestRate;
 	}
 
 	public boolean isShipNameTaken(String name) {
 		for (Company c: companies) {
 			for (ShipModel s: c.getShips()) {
 				if (s.getName().equals(name)) {
 					return true;
 				}
 			}
 		}
 
 		return false;
 	}
 
 	public NewWorld getWorld() {
 		if (world == null) {
 			world = NewWorld.readFromFile("worldoutput.txt");
 		}
 
 		return world;
 	}
 
 	public void loadFreightOntoShip(ShipModel ship, Freight freight) {
 		getFreightMarket().getFreights().remove(freight);
 		ship.addFreight(freight);
 		fireGameStateChanged();
 	}
 
 	public List<Port> getPortsInCountry(String country) throws Countries.NoSuchCountryException, NoSuchPortException {
 		return worldPorts.getPortsForCountry(country);
 	}
 
 }
