 package edu.mines.acmX.exhibit.modules.home_screen;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Random;
 
 import org.apache.logging.log4j.LogManager;
 import org.apache.logging.log4j.Logger;
 
 import processing.core.PImage;
 import edu.mines.acmX.exhibit.input_services.events.EventManager;
 import edu.mines.acmX.exhibit.input_services.events.EventType;
 import edu.mines.acmX.exhibit.input_services.hardware.BadDeviceFunctionalityRequestException;
 import edu.mines.acmX.exhibit.input_services.hardware.BadFunctionalityRequestException;
 import edu.mines.acmX.exhibit.input_services.hardware.DeviceConnectionException;
 import edu.mines.acmX.exhibit.input_services.hardware.HardwareManager;
 import edu.mines.acmX.exhibit.input_services.hardware.HardwareManagerManifestException;
 import edu.mines.acmX.exhibit.input_services.hardware.devicedata.DepthImageInterface;
 import edu.mines.acmX.exhibit.input_services.hardware.devicedata.HandTrackerInterface;
 import edu.mines.acmX.exhibit.input_services.hardware.drivers.InvalidConfigurationFileException;
 import edu.mines.acmX.exhibit.module_management.ModuleManager;
 import edu.mines.acmX.exhibit.module_management.loaders.ManifestLoadException;
 import edu.mines.acmX.exhibit.module_management.loaders.ModuleLoadException;
 import edu.mines.acmX.exhibit.modules.home_screen.backdrops.Backdrop;
 import edu.mines.acmX.exhibit.modules.home_screen.backdrops.bubbles.BubblesBackdrop;
 import edu.mines.acmX.exhibit.modules.home_screen.backdrops.gameoflife.GridBackdrop;
 import edu.mines.acmX.exhibit.modules.home_screen.view.ArrowClick;
 import edu.mines.acmX.exhibit.modules.home_screen.view.LinearLayout;
 import edu.mines.acmX.exhibit.modules.home_screen.view.ListLayout;
 import edu.mines.acmX.exhibit.modules.home_screen.view.ModuleElement;
 import edu.mines.acmX.exhibit.modules.home_screen.view.Side;
 import edu.mines.acmX.exhibit.modules.home_screen.view.SpaceElement;
 import edu.mines.acmX.exhibit.modules.home_screen.view.TimeDisplay;
 import edu.mines.acmX.exhibit.modules.home_screen.view.inputmethod.VirtualRectClick;
 import edu.mines.acmX.exhibit.modules.home_screen.view.weather.WeatherDisplay;
 
 /*
  * TODO (in order)
  * Getting the feeds working
  * Connecting the Home Screen to the Module API
  */
 public class HomeScreen extends edu.mines.acmX.exhibit.module_management.modules.ProcessingModule {
 	
 	private static Logger log = LogManager.getLogger(HomeScreen.class);
 	//picture used for the user's hand
 	public static final String CURSOR_FILENAME = "hand_cursor.png";
 	private PImage cursor_image;
 	
 	//variables that hold the backdrops
 	private List<Backdrop> backdrops;
 	private Backdrop backdrop;
 	
 	//hand positions
 	private static float handX, handY;
 	// root layout for module
 	private LinearLayout rootLayout;
 	// list layout to hold all module elements
 	private ListLayout moduleListLayout;
 	// arrows, to manipulate the moduleListLayout
 	private ArrowClick leftArrow;
 	private ArrowClick rightArrow;
 	// set to true if using 2 monitors, or false for 1 (used for debugging, for PApplet dims)
 	public static final boolean DUAL_SCREEN = false;
 	// listLayout scroll speed, in pixels
 	public static final int SCROLL_SPEED = 20;
 	// how fast an arrow registers a click
 	public static final int ARROW_CLICK_SPEED = 50;
 	// time to fall asleep, in millis
 	public static final int TIME_TO_SLEEP = 10000;
 	// all module elements
 	private ArrayList<ModuleElement> moduleElements;
 	// millis when last interacted with
 	private int lastInput;
 	// holds sleeping status (used for cycling backdrops)
 	private boolean isSleeping;
 	
 	//interfaces with input services components
 	private static HardwareManager hardwareManager;
 	private static EventManager eventManager;
 	private HandTrackerInterface driver;
 	private MyHandReceiver receiver;
 	
 	/**
 	 * setup, called once
 	 */
 	public void setup() {
 		// Allows for sane usage of dual monitors
 		if (DUAL_SCREEN) {
 			width = width / 2;
 		}
 		size(width, height);
 		
 		// load cursor image
 		cursor_image = loadImage(CURSOR_FILENAME);
 		cursor_image.resize(32, 32);
 		
 		//loads backdrops
 		//currently backdrops must be added manually (convert?)
 		backdrops = new ArrayList<Backdrop>();
 		backdrops.add(new BubblesBackdrop(this));
 		backdrops.add(new GridBackdrop(this));
 		//pick backdrop to launch with (TODO could randomize- optional)
 		backdrop = backdrops.get(0);
 		isSleeping = false;
 		
 		noCursor();
 		
 		// get all module names
 		ModuleManager manager;
 		String[] packageNames = null;
 		moduleElements = new ArrayList<ModuleElement>();
 		//builds the layout for displaying modules
 		moduleListLayout = new ListLayout(Orientation.HORIZONTAL, this, 88.0, 1.0, 3);
 		try {
 			manager = ModuleManager.getInstance();
 			packageNames = manager.getAllAvailableModules();
 		} catch (ManifestLoadException e) {
 			log.info("Bad module manager manifest");
 			e.printStackTrace();
 		} catch (ModuleLoadException e) {
 			log.info("Module failed to load");
 			e.printStackTrace();
 		} catch (HardwareManagerManifestException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (BadDeviceFunctionalityRequestException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		// Iterates through the array of package names and loads each module's icon.
 		for (int i = 0; i < packageNames.length; ++i) {
 			//removes the home screen module from the list of displayed modules
 			if (packageNames[i].equals("edu.mines.acmX.exhibit.modules.home_screen") ) {
 				continue;
 			}
 			PImage tempImage = loadImage("icon.png", packageNames[i]);
 			if (tempImage == null) {
 				//load a default icon if there's no icon in the original package
 				tempImage = loadImage("question.png");
 			}
 			// storing icons and package names into their respective ModuleElements.
 			ModuleElement tempElement = new ModuleElement(this, tempImage, packageNames[i], 1.0);
 			moduleElements.add(tempElement);
 			moduleListLayout.add(tempElement);
 		}
 		
 		//main layout for the whole screen
 		rootLayout = new LinearLayout(Orientation.VERTICAL, this, 1.0);
 		
 		//holds the module set layout
 		LinearLayout modules = new LinearLayout(Orientation.HORIZONTAL, this, 80.0);
 		leftArrow = new ArrowClick(this, 6.0, new VirtualRectClick(ARROW_CLICK_SPEED, 0, 0, 0, 0), Side.LEFT);
 		rightArrow = new ArrowClick(this, 6.0, new VirtualRectClick(ARROW_CLICK_SPEED, 0, 0, 0, 0), Side.RIGHT);
 		modules.add(leftArrow);
 		modules.add(moduleListLayout);
 		modules.add(rightArrow);
 		//top margin spacing
 		rootLayout.add(new SpaceElement(this, 10.0));
 		//add modules list
 		rootLayout.add(modules);
 		//add spacing below modules
 		rootLayout.add(new SpaceElement(this, 50.0));
 		
 		//add Twitter display
 		LinearLayout twitter = new LinearLayout(Orientation.HORIZONTAL, this, 10.0);
 		//twitter.add(new TwitterDisplay(this, 100.0)); //TODO get this fully working
 		//add Weather/Time display
 		LinearLayout weatherAndTime = new LinearLayout(Orientation.HORIZONTAL, this, 10.0);
 		weatherAndTime.add(new WeatherDisplay(this, 80.0));
 		weatherAndTime.add(new TimeDisplay(this, 20.0));
 
 		rootLayout.add(twitter);
 		rootLayout.add(weatherAndTime);
 		// set default settings for root layout
 		rootLayout.setOriginX(0);
 		rootLayout.setOriginY(0);
 		rootLayout.setHeight(height);
 		rootLayout.setWidth(width);
 		lastInput = millis();
 		// hardware stuff
 		try {
 			hardwareManager = HardwareManager.getInstance();
 		} catch (HardwareManagerManifestException e) {
 			log.error("Hardware manager manifest is malformed");
 			e.printStackTrace();
 		}
 		try {
 			driver = (HandTrackerInterface) hardwareManager.getInitialDriver("handtracking");
 			
 		} catch (BadFunctionalityRequestException e) {
 			log.error("Asked for nonexistent functionality");
 			e.printStackTrace();
 		} catch (DeviceConnectionException e) {
 			log.error("Requested driver path is unavailable");
 			e.printStackTrace();
 		} catch (InvalidConfigurationFileException e) {
 			log.error("Invalid configuration file loaded");
 			e.printStackTrace();
 		}
 		
 		//builds the event manager, and sets the receiver to look for hand events
 		eventManager = EventManager.getInstance();
 		receiver = new MyHandReceiver();
 		eventManager.registerReceiver(EventType.HAND_CREATED, receiver);
 		eventManager.registerReceiver(EventType.HAND_UPDATED, receiver);
 		eventManager.registerReceiver(EventType.HAND_DESTROYED, receiver);
 	}
 	
 	/**
 	 * update loop, called continuously before draw
 	 */
 	public void update() {
 		//refresh driver for hand tracking
 		driver.updateDriver();
 		//checks if there is a registered hand detected
 		if (receiver.whichHand() != -1) {
 			if(isSleeping) {
 				//stop sleeping if it was before
 				isSleeping = false;
 				//switch the backdrop to a random one in the list
 				cycleBackdrop();
 			}
			
			//TODO transfer this whole thing into stdlib
			// int getScaledHandX(int handX, int depthWidth, int frameWidth, float marginFraction)
			// handX = getScaledHandX(receiver.getX(), driver.getHandWidth, width, 6);
			int depthWidth = 640; //TODO get HandTracker data (NYI)
			int depthHeight = 480;
			float marginFraction = 6; //user preference for sensitivity
			float marginX = depthWidth/marginFraction;
			float marginY = depthHeight/marginFraction;
			float scaledDepthWidth = depthWidth - 2 * marginX;
			float scaledDepthHeight = depthHeight - 2 * marginY;
			handX = (receiver.getX() - marginX) * width/scaledDepthWidth;
			handY = (receiver.getY() - marginY) * height/scaledDepthHeight;
 			//get hand position - uses scaling to let user reach all of screen
			if (handX < 0) handX = 0;
			if (handY < 0) handY = 0;
			if (handX > width) handX = width;
			if (handY > height) handY = height;
 			lastInput = millis();
 		}
 		//call update for the loaded backdrop
 		backdrop.update();
 		rootLayout.update(0, 0);
 		int millis = millis();
 		// check arrows for clicks
 		if (leftArrow.completed(millis)) {
 			moduleListLayout.incrementViewLength(-SCROLL_SPEED);
 		}
 		if (rightArrow.completed(millis)) {
 			moduleListLayout.incrementViewLength(SCROLL_SPEED);
 		}
 
 		// check all ModuleElements for clicks
 		checkModulesForClicks();
 		
 
 	}
 	
 	/**
 	 * draws the screen
 	 */
 	public void draw() {
 		//call update first
 		update();
 		
 		//white background
 		background(255, 255, 255);
 		backdrop.draw();
 		// draw the other modules if not sufficiently inactive
 		if (millis() - lastInput < TIME_TO_SLEEP) {
 			rootLayout.draw();
 		} else {
 			//set the screen to sleep
 			isSleeping = true;
 		}
 		
 		//draw the hand where the user's hand is (if it has one)
 		if (receiver.whichHand() != -1) {
			imageMode(CENTER);
 			image(cursor_image, handX, handY);
			imageMode(CORNER);
 		}
 	}
 
 	/**
 	 * Iterates through all ModuleElements and checks them for clicks
 	 */
 	public void checkModulesForClicks() {
 		int millis = millis();
 		for (ModuleElement element: moduleElements) {
 			element.checkClicks(millis);
 		}
 	}
 	
 	/**
 	 * randomly picks an available backdrop to display
 	 */
 	public void cycleBackdrop() {
 		Random rand = new Random();
 		int randBackdrop = rand.nextInt(backdrops.size());
 		backdrop = backdrops.get(randBackdrop);
 	}
 	
 	public static float getHandX() {
 		return handX;
 	}
 	
 	public static float getHandY() {
 		return handY;
 	}
 	
 	public MyHandReceiver getReceiver() {
 		return receiver;
 	}
 }
