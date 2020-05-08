 package silas;
 import java.util.Collection;
 import java.util.Timer;
 import java.util.TimerTask;
 import org.grailrtls.libworldmodel.client.ClientWorldConnection;
 import org.grailrtls.libworldmodel.client.WorldState;
 import org.grailrtls.libworldmodel.client.protocol.messages.Attribute;
 import org.grailrtls.libworldmodel.solver.SolverWorldConnection;
 import org.grailrtls.libworldmodel.types.BooleanConverter;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import silas.SwitchController;
 
 public class Heater extends TimerTask {
 
 	/**
 	 * Logger for this class.
 	 */
 	private static final Logger log = LoggerFactory.getLogger(Heater.class);
 	/**
 	 * World Model URI for the chair we want.
 	 */
 	static String chairQuery;
 	/**
 	 * World Model URI for the door we want.
 	 */
 	static String doorQuery;
 	
 	/**
 	 * Flag to indicate whether the heater is on or off.
 	 */
 	static boolean heaterIsOn = false;
 	
 	/**
 	 * How frequently to check the door and chair status.
 	 */
 	static long checkInterval = 120000;
 	
 	/**
 	 * How long to wait before firing the first update check.
 	 */
 	static long initialTaskDelay = 2000;
 	
 	/**
 	 * How long both the door and chair must be "inactive" before the heater is turned off.
 	 * Defaults to 5 minutes (300 seconds).
 	 */
 	static long shutoffDelay = 300000;
 	
 	/**
 	 * Flag to indicate whether the door was opened or closed within the {@linkplain #shutoffDelay}.
 	 */
 	static boolean doorIsActive;
 	
 	/**
 	 * Flag to indicate whether the chair was occupied within the {@linkplain #shutoffDelay}.
 	 */
 	static boolean chairIsActive;
 
 	/**
 	 * Describes the required parameters for this application.
 	 */
 	private static final String USAGE_STRING = "Requires: <WM Host> <WM Solver Port> <WM Client Port> <Chair URI> <Door URI>";
 
 	/**
 	 * Takes arguments: hostname, client port, solver port, chair URI, door URI
 	 * 
 	 * @param args
 	 */
 	public static void main(String[] args) {
 
 		if (args.length < 5) {
 			System.err.println(USAGE_STRING);
 		}
 
 		String hostname = args[0];
 
 		int c_port = Integer.parseInt(args[2]);
 		int s_port = Integer.parseInt(args[1]);
 		chairQuery = args[3];
 		doorQuery = args[4];
 
 		log.info("Starting heater control. Receiving data from {} and {}.",chairQuery, doorQuery);
 		log.info("Heater shuts off when {} is empty and {} is unchanged for "+ shutoffDelay + "ms.", chairQuery, doorQuery);
 		// Create a connection to the World Model as a client
 		ClientWorldConnection wmc = new ClientWorldConnection();
 		wmc.setHost(hostname);
 		wmc.setPort(c_port);
 		if (!wmc.connect()) {
 			log.error("Unable to connect to world model as a client!");
 			return;
 		}
 		SolverWorldConnection wms = new SolverWorldConnection();
 		wms.setHost(hostname);
 		wms.setPort(s_port);
 		wms.setOriginString("HeaterSolver");
 		if (!wms.connect()) {
 			log.error("Unable to connect to world model as a solver!");
 			return;
 		}
 		SwitchController switchcontrol = new SwitchController(wms);
 		Timer mintimer = new Timer();
 		TimerTask checker = new Heater(wmc, switchcontrol);
 		doorIsActive = true;
 		chairIsActive = true;
 		mintimer.scheduleAtFixedRate(checker, initialTaskDelay, checkInterval);
 	}
 
 	private final ClientWorldConnection wmc;
 	private final SwitchController controller;
 
 	public Heater(ClientWorldConnection wmc, final SwitchController controller) {
 		this.wmc = wmc;
 		this.controller = controller;
 	}
 
 	@Override
 	public void run() {
 		try {
 			// The next line will block until the response completes or an
 			// exception occurs
 			WorldState chairstate = wmc
 					.getSnapshot(chairQuery, 0l, 0l, "empty").get();
 			WorldState doorstate = wmc.getSnapshot(doorQuery, 0l, 0l, "closed")
 					.get();
 			Collection<String> chairuris = chairstate.getURIs();
 			Collection<String> dooruris = doorstate.getURIs();
 			System.out.println("Door uris: " + dooruris);
 			for (String uri : chairuris) {
 				System.out.println("URI: " + uri);
 				Collection<Attribute> attribs = chairstate.getState(uri);
 				for (Attribute att : attribs) {
 					boolean empty = BooleanConverter.CONVERTER.decode(att
 							.getData());
 					System.out.println("\tEmpty: " + empty);
 					System.out.println("Time since last change:"
 							+ (System.currentTimeMillis() - att
 									.getCreationDate()) + " ms");
 					if (System.currentTimeMillis() - att.getCreationDate() > shutoffDelay
 							&& empty) {
 						chairIsActive = false;
 					} else {
 						chairIsActive = true;
 					}
 				}
 			}
 			for (String uri : dooruris) {
 				System.out.println("URI: " + uri);
 				Collection<Attribute> attribs = doorstate.getState(uri);
 				for (Attribute att : attribs) {
 					log.debug("\tClosed: "+ BooleanConverter.CONVERTER.decode(att.getData()));
 					log.debug("Time since last change:"+ (System.currentTimeMillis() - att.getCreationDate()) + " ms");
 					if (System.currentTimeMillis() - att.getCreationDate() > shutoffDelay) {
 						doorIsActive = false;
 					} else {
 						doorIsActive = true;
 					}
 				}
 			}
 		} catch (Exception e) {
 			log.error("Exception thrown while getting response: " + e);
 		}
 		// If chair is empty and hasn't changed for X minutes, and door hasn't
 		// changed for X minutes,
 		// then shut off the heater. Otherwise, turn it back on.
 		if (!doorIsActive && !chairIsActive) {
 			log.debug("Heater status: off");
 			if (heaterIsOn)
 				this.controller.update("winlab.powerswitch.heater", "on",
 						false);
 			heaterIsOn = false;
 		} else {
 			log.debug("Heater status: on");
 			if (!heaterIsOn)
 				this.controller.update("winlab.powerswitch.heater", "on",
 						true);
 			heaterIsOn = true;
 		}
 	}
 }
