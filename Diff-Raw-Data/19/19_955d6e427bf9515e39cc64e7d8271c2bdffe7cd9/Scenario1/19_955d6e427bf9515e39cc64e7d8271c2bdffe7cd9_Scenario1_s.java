 /**
  * 
  */
 package org.microworlds.scenario;
 
 import java.util.Date;
 
 import org.microworld.logging.Log;
 import org.microworld.models.Location;
 import org.microworld.models.Mode;
 import org.microworld.models.Person;
 import org.microworld.models.Preferences;
 import org.microworld.models.Trip;
 import org.microworld.robots.BehavioralPatterns;
 import org.microworld.robots.DriverAgent;
 import org.microworld.robots.Robot;
 
 /**
  * @author riccardo
  * 
  */
 public class Scenario1 extends Scenario {
 	int agentCounter = 0;
 	DriverAgent driver;
 	Trip trip;
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.microworlds.scenario.DycapoScenario#setUp()
 	 */
 	public void setUp() {
 
 		Log.verbose("Scenario1 SetUp", "starting");
 		driver = new DriverAgent(1.00);
 		Robot path = new Robot(
 				BehavioralPatterns
 						.getBehavioralPattern(BehavioralPatterns.STRAIGHT_LINE));
 		path.setRole(Robot.DRIVER);
 		driver.setPath(path);
 
 		Location origin = new Location();
 		Location destination = new Location();
 
 		origin.setLeaves(new Date(System.currentTimeMillis()));
 		destination.setLeaves(new Date(System.currentTimeMillis()));
 
 		origin.setPoint(Location.ORIG);
 		destination.setPoint(Location.DEST);
 
 		origin.setGeorss_point(path.getList().get(0).toGeoRSSPoint());
 		destination.setGeorss_point(path.getList()
 				.get(path.getList().size() - 1).toGeoRSSPoint());
 
 		Mode modality = new Mode();
 		modality.setLic("ASDFGH");
 		modality.setKind("auto");
 		modality.setCapacity(5);
 		modality.setVacancy(4);
 		modality.setMake("BMW");
 		modality.setModel("M3");
 
 		Person person = new Person();
 		person.setUsername("django");
 		person.setPassword(LOGIN_PASSWORD);
 		person.setEmail("asd@asd.com");
 		person.setPhone("1223334444");
 		driver.setUser(person);
 
 		trip = new Trip();
 		trip.setAuthor(person);
 		trip.setDestination(destination);
 		trip.setOrigin(origin);
 		trip.setExpires(new Date(System.currentTimeMillis()));
 		trip.setMode(modality);
 		trip.setPreferences(new Preferences());
 		trip.setActive(false);
 
 		driver.setTrip(trip);
 		driver.register(driver.getUser());
 		Log.verbose("Scenario1 SetUp",
 				"finished ready to start the simulation ...");
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.microworlds.scenario.DycapoScenario#start()
 	 */
 	@Override
 	public void start() {
 		Log.verbose("Scenario1 Start", "starting the scenario");
 
 		driver.start();
 
 		Log.verbose("Scenario1 Start", "Simulation began");
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.microworlds.scenario.DycapoScenario#stop()
 	 */
 	@Override
 	public void stop() {
 		// TODO Auto-generated method stub
 
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.microworlds.scenario.DycapoScenario#load(java.lang.String)
 	 */
 	@Override
 	public void load(String resource) {
 		// TODO Auto-generated method stub
 
 	}
 
 }
