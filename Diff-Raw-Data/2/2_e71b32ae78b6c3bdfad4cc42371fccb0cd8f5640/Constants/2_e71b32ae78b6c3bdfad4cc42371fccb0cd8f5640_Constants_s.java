 package Utils;
 
 import java.awt.Image;
 import java.awt.Toolkit;
 import java.util.ArrayList;
 import java.util.Arrays;
 
 import javax.swing.BorderFactory;
 import javax.swing.border.Border;
 
 import factory.KitConfig;
 import factory.PartType;
 
 /**
  * Contains all the constants that we need in the project.
  * @author Peter Zhang
  */
 public abstract class Constants {
 
 	// SERVER SETTINGS
 	// ==================================
 
 	public static final int SERVER_PORT = 6889;
 
 	public static final String IMAGE_PATH = "src/images/";
 
 	// CLIENT SETTINGS
 	// ==================================
 
 	public static final int TIMER_DELAY = 30; //50
 
 	public static final Image CLIENT_BG_IMAGE = Toolkit.getDefaultToolkit()
 			.getImage(IMAGE_PATH + "bg.jpg");
 
 	// DEVICE SETTINGS
 	// ==================================
 
 	public static final int FEEDER_COUNT = 4;
 	public static final int LANE_COUNT = 8;
 	public static final int NEST_COUNT = 8;
 	public static final int STAND_COUNT = 3;
 
 	public static final ArrayList<PartType> DEFAULT_PARTTYPES = new ArrayList<PartType>(
 			Arrays.asList(new PartType("1", 1, "Type1"), 
 					new PartType("2", 2, "Type2"), new PartType("3", 3, "Type3"), 
 					new PartType("4", 4, "Type4"), new PartType("5", 5, "Type5"),
 					new PartType("6", 6, "Type6"), new PartType("7", 7, "Type7"),
 					new PartType("8", 8, "Type8"), new PartType("9", 9, "Type9"),
 					new PartType("10", 10, "Type10")));
 
 	public static final ArrayList<KitConfig> DEFAULT_KITCONFIGS = new ArrayList<KitConfig>(
 			Arrays.asList(new KitConfig("Default Kit", 
 					DEFAULT_PARTTYPES.get(0),DEFAULT_PARTTYPES.get(1),
 					DEFAULT_PARTTYPES.get(2),DEFAULT_PARTTYPES.get(3),
 					DEFAULT_PARTTYPES.get(4),DEFAULT_PARTTYPES.get(5),
 					DEFAULT_PARTTYPES.get(6),DEFAULT_PARTTYPES.get(7)
 					), new KitConfig("Simple Kit", 
 					DEFAULT_PARTTYPES.get(0),DEFAULT_PARTTYPES.get(1),
 					DEFAULT_PARTTYPES.get(4),DEFAULT_PARTTYPES.get(5)
 					)));
 
 	// DEVICE START LOCATIONS
 	// ==================================
 
 	public static final Location FEEDER_LOC = new Location(850, 45);
 	public static final int FEEDER_Y_STEP = 150;
 
 	public static final Location KIT_ROBOT_LOC = new Location(75, 192.5);
 	public static final Location KIT_ROBOT_ROTATION_AXIS_LOC = new Location(112.5,37.5);
 	public static final Location KIT_ROBOT_KIT_LOC = new Location(0,200);
 	public static final Location KIT_ROBOT_KIT_ROTATION_AXIS_LOC = new Location(180,40);
 	public static final Location CONVEYOR_LOC = new Location(0, 80); // 185
 	public static final Location KIT_LOC = new Location(150, 200);
 	public static final Location PARTS_ROBOT_LOC = new Location(250, 450);
 	public static final Location GANTRY_ROBOT_LOC = new Location(940, 180);
 
 	// TODO: get exact location coordinates
 	public static final Location BIN_STORAGE_LOC = new Location(800, 1000);
 
 	// TODO: how many cameras are there?
 	public static final Location CAMERA_LOC = new Location(5, 5);
 
 	// DEVICE IMAGES
 	// ==================================
 
 	// Feeder Images
 	public static final Image FEEDER_IMAGE = Toolkit.getDefaultToolkit()
 			.getImage(IMAGE_PATH + "Feeder.png");
 	public static final Image FEEDER_BLUE_LED = Toolkit.getDefaultToolkit()
 			.getImage(IMAGE_PATH + "LED_blue.png");
 	public static final Image FEEDER_RED_LED = Toolkit.getDefaultToolkit()
 			.getImage(IMAGE_PATH + "LED_red.png");
 
 	// Lane Images
 	public static final Image LANE_IMAGE1 = Toolkit.getDefaultToolkit()
 			.getImage(IMAGE_PATH + "lane_1.png");
 	public static final Image LANE_IMAGE2 = Toolkit.getDefaultToolkit()
 			.getImage(IMAGE_PATH + "lane_2.png");
 	public static final Image LANE_LINE = Toolkit.getDefaultToolkit().getImage(
 			IMAGE_PATH + "lane_belt.png");
 
 	// Conveyor Images
 	public static final Image CONVEYOR_IMAGE = Toolkit.getDefaultToolkit()
 			.getImage(IMAGE_PATH + "conveyor_1.png");
 	public static final Image CONVEYOR_LINES_IMAGE = Toolkit
 			.getDefaultToolkit().getImage(IMAGE_PATH + "conveyor_belt.png");
 	public static final Image TEST_CONVEYOR_IMAGE = Toolkit.getDefaultToolkit()
 			.getImage(IMAGE_PATH + "TestConveyor.png");
 	public static final Image TEST_CONVEYOR_LINE_IMAGE = Toolkit
 			.getDefaultToolkit().getImage(IMAGE_PATH + "TestConveyorLine.png");
 
 	// Kit Robot Images
 	public static final Image KIT_ROBOT_IMAGE = Toolkit.getDefaultToolkit()
 			.getImage(IMAGE_PATH + "kit_robot.png");
 
 	// Gantry Robot Image
 	public static final Image GANTRY_ROBOT_IMAGE = Toolkit.getDefaultToolkit()
 			.getImage(IMAGE_PATH + "gantry_temp.png");
 
 	// Kit Images
 	public static final Image KIT_IMAGE = Toolkit.getDefaultToolkit().getImage(
 			IMAGE_PATH + "Kit.png");
 	public static final Image KIT_DONE_IMAGE = Toolkit.getDefaultToolkit()
 			.getImage(IMAGE_PATH + "kit_done.png");
 
 	// Nest Images
 	public static final Image NEST_IMAGE = Toolkit.getDefaultToolkit()
 			.getImage(IMAGE_PATH + "Nest.png");
 
 	// Camera Images
 	public static final Image CAMERA_IMAGE = Toolkit.getDefaultToolkit()
 			.getImage(IMAGE_PATH + "Camera.png");
 
 	// Part Images
 	@Deprecated
 	public static final Image PART_IMAGE = Toolkit.getDefaultToolkit()
 			.getImage(IMAGE_PATH + "samplepart.png");
 
 	// Part Images
 	public static final String PART_IMAGE_PATH = IMAGE_PATH + "part_";
 
 	// Bin Images
 	public static final String BIN_IMAGE_PATH = IMAGE_PATH + "bin_";
 	
 	public static final Image BIN_EMPTY_IMAGE = Toolkit.getDefaultToolkit()
 			.getImage("");
 
 	// Kit & Inspection Stand Images // TODO: add actual images
 	public static final Image STAND_IMAGE = Toolkit.getDefaultToolkit()
 			.getImage(IMAGE_PATH + "stand.png");
 	
 	// IMAGE SIZES
 	public static final int PART_WIDTH = 20, PART_HEIGHT = 50;
 	public static final int PART_OFFSET = 19;
 	
 	public static final int LANE_LENGTH = 210;
 	public static final int LANE_BEG_X = 850, LANE_END_X = 640;
 	
 	public static final int NEST_WIDTH = 75, NEST_HEIGHT = 70;
 
 	// TARGET NAMES
 	// ==================================
 	// Used so that we can create Request methods more easily.
 	// When the target has a specific ID, concatenate to the device target
 	// e.g. String target = Constants.LANE_TARGET+":"+laneID;
 
 	public static final String BIN_TARGET = "Bin";
 	public static final String CAMERA_TARGET = "Camera";
 	public static final String CONVEYOR_TARGET = "Conveyor";
 	public static final String FEEDER_TARGET = "Feeder";
 	public static final String LANE_TARGET = "Lane";
 
 	public static final String KIT_ROBOT_TARGET = "KitRobot";
 	public static final String GANTRY_ROBOT_TARGET = "GantryRobot";
 	public static final String PARTS_ROBOT_TARGET = "PartsRobot";
 
 	public static final String PART_TARGET = "Part";
 	public static final String NEST_TARGET = "Nest";
 	public static final String KIT_TARGET = "Kit";
 	public static final String STAND_TARGET = "Stand";
 
 	public static final String SERVER_TARGET = "Server";
 	public static final String FCS_TARGET = "FCS";
 	public static final String ALL_TARGET = "all";
 
 	// COMMAND NAMES
 	// ==================================
 	// Used so that we can create Request methods more easily.
 	// Naming convention: DEVICENAME_ACTION_COMMAND
 
 	public static final String DONE_SUFFIX = "done";
 	// for servers to identify managers
 	public static final String IDENTIFY_COMMAND = "identify";
 
 	// feeder logic to display commands
 	// flip the diverter
 	public static final String FEEDER_FLIP_DIVERTER_COMMAND = "feederflipdiv";
 	// a bin has been received
 	public static final String FEEDER_RECEIVED_BIN_COMMAND = "feederrecbin";
 	// purge the bin
 	public static final String FEEDER_PURGE_BIN_COMMAND = "feederpurgebin";
 	// end feeder logic to display commands
 
 	// lane logic to display commands
 	// purge lane
 	public static final String LANE_PURGE_COMMAND = "purge lane";
 	// sends animation instructions to lane
 	public static final String LANE_SEND_ANIMATION_COMMAND = "lane animation";
 	// sets lane amplitude
 	public static final String LANE_SET_AMPLITUDE_COMMAND = "lane set amp";
 	// turns lane on or off
 	public static final String LANE_TOGGLE_COMMAND = "lane toggle";
 	// sets start loc for this lane
 	public static final String LANE_SET_STARTLOC_COMMAND = "lane start loc";
 	// new part added to lane
 	public static final String LANE_RECEIVE_PART_COMMAND = "lane receive part";
 	// gives part to nest
 	public static final String LANE_GIVE_PART_TO_NEST = "lane give part to nest";
 	// end lane commands
 
 	// conveyor logic to display commands
 	// conveyor gives kit to kit robot
 	public static final String CONVEYOR_GIVE_KIT_TO_KIT_ROBOT_COMMAND = "give kit to kit robot";
 	// conveyor receives a kit
 	public static final String CONVEYOR_RECEIVE_KIT_COMMAND = "conveyor receive kit";
 	// sends animation information to conveyor
 	public static final String CONVEYOR_SEND_ANIMATION_COMMAND = "conveyor animation";
 	// need to change velocity
 	public static final String CONVEYOR_CHANGE_VELOCITY_COMMAND = "conveyor change velocity";
 	public static final String CONVEYOR_MAKE_NEW_KIT_COMMAND = "make new kit";
 	// end conveyor logic to display
 
 	// kitrobot logic
 	public static final String KIT_ROBOT_LOGIC_PICKS_CONVEYOR_TO_LOCATION1 = "robot logic moves conveyor to loc1";
 	public static final String KIT_ROBOT_DISPLAY_PICKS_CONVEYOR_TO_LOCATION1 = "robot display moves conveyor to loc1 ";
 
 	public static final String KIT_ROBOT_LOGIC_PICKS_CONVEYOR_TO_LOCATION2 = "robot logic moves conveyor to loc2";
 	public static final String KIT_ROBOT_DISPLAY_PICKS_CONVEYOR_TO_LOCATION2 = "robot display moves conveyor to loc2";
 
 	public static final String KIT_ROBOT_LOGIC_PICKS_INSPECTION_TO_GOOD_CONVEYOR = "robot logic moves inspection to conv";
 	public static final String KIT_ROBOT_DISPLAY_PICKS_INSPECTION_TO_GOOD_CONVEYOR = "robot display moves inspection to conv";
 
 	public static final String KIT_ROBOT_LOGIC_PICKS_LOCATION1_TO_INSPECTION = "robot logic moves loc1 to inspection";
 	public static final String KIT_ROBOT_DISPLAY_PICKS_LOCATION1_TO_INSPECTION = "robot display moves loc1 to inspection";
 
 	public static final String KIT_ROBOT_LOGIC_PICKS_LOCATION2_TO_INSPECTION = "robot logic moves loc2 to inspection";
 	public static final String KIT_ROBOT_DISPLAY_PICKS_LOCATION2_TO_INSPECTION = "robot display moves loc2 to inspection";
 
 	public static final String KIT_ROBOT_LOGIC_PICKS_lOCATION1_TO_CONVEYOR = "robot logic moves loc1 to conv";
 	public static final String KIT_ROBOT_DISPLAY_PICKS_LOCATION1_TO_CONVEYOR = "robot display moves kit to conv";
 
 	public static final String KIT_ROBOT_LOGIC_PICKS_LOCATION2_TO_CONVEYOR = "robot logic moves loc2 to conv";
 	public static final String KIT_ROBOT_DISPLAY_PICKS_LOCATION2_TO_CONVEYOR = "robot display moves kit to conv ";
 
 	public static final String KIT_ROBOT_ON_INSPECTION_DONE = "robot display sends to robot logic that kit to inspeciton is done";
 	public static final String KIT_ROBOT_ON_STAND_DONE = "robot display sends to robot logic that kit to stand is done";
 	public static final String KIT_ROBOT_ON_CONVEYOR_DONE = "robot display sends to robot logic that kit to conveyor is done";
 
 	public static final int KIT_ROBOT_DEGREE_STEP = 5;
 	// end kitrobot logic
 
 	// kit positions
 	public static final String KIT_INSPECTION_AREA = "Kit inspection area";
 	public static final String KIT_LOCATION1 = "Kit location1 area";
 	public static final String KIT_LOCATION2 = "Kit location2 area";
 	public static final String KIT_INITIAL = "Kit initial area";
 	// end kit positions
 
 	// for kit robot agent test
 	public static final String KIT_ROBOT_AGENT_RECEIVES_KIT1_DONE = "sends agent kit1 full";
 	public static final String KIT_ROBOT_AGENT_RECEIVES_KIT2_DONE = "sends agent kit2 full";
 	public static final String KIT_ROBOT_AGENT_RECEIVES_KIT_INSPECTED = "sends agent kit passed inspection";
 
 	// end kit robot agent test
 
 	// stand commands
 	public static final String STAND_RECEIVE_KIT_COMMAND = "stand receive kit";
 	public static final String STAND_GIVE_KIT_COMMAND = "stand give kit";
 	public static final String STAND_RECEIVE_PART_COMMAND = "stand receive part";
 
 	// gantry logic to display commands
 
 	public static final String GANTRY_ROBOT_MOVE_TO_LOC_COMMAND = "move";
 	public static final String GANTRY_ROBOT_GET_BIN_COMMAND = "pickup";
 	public static final String GANTRY_ROBOT_DROP_BIN_COMMAND = "drop";
	public static final String GANTRY_ROBOT_DONE_MOVE = "done";
 	public static final String GANTRY_ROBOT_ADD_NEW_BIN = "new bin";
 
 	// partsrobot logic to display commands
 
 	// parts robot rotate
 	public static final String PARTS_ROBOT_ROTATE_COMMAND = "rotate";
 	// pick up part
 	public static final String PARTS_ROBOT_PICKUP_COMMAND = "pickup";
 	// give part to kit
 	public static final String PARTS_ROBOT_GIVE_COMMAND = "give";
 	// parts robot go to kit
 	public static final String PARTS_ROBOT_GO_KIT_COMMAND = "gokit";
 	// end partsrobot logic to display commands
 	public static final String PARTS_ROBOT_RECEIVE_PART_COMMAND = "receivepart";
 	// receive parts from nest
 	public static final String PARTS_ROBOT_GIVE_PART_COMMAND = "givepart";
 	// give part to kit
 
 	// nest logic to display commands
 	public static final String NEST_RECEIVE_PART_COMMAND = "nestrecpart";
 	public static final String NEST_GIVE_TO_PART_ROBOT_COMMAND = "nestgivetopr";
 	public static final String NEST_PURGE_COMMAND = "nestpurge";
 	// end nest logic to display commands
 
 	// camera logic to display commands
 	public static final String CAMERA_TAKE_NEST_PHOTO_COMMAND = "cameranest";
 	public static final String CAMERA_TAKE_KIT_PHOTO_COMMAND = "camerakit";
 	// end nestgraphics logic to display commands
 
 	// kits. WILL CHANGE
 	public static final String KIT_UPDATE_PARTS_LIST_COMMAND = "updateParts";
 	// end kits
 
 	// FCS commands
 	public static final String FCS_NEW_PART = "newPart";
 	public static final String FCS_EDIT_PART = "editPart";
 	public static final String FCS_DELETE_PART = "deletePart";
 	public static final String FCS_NEW_KIT = "newKit";
 	public static final String FCS_EDIT_KIT = "editKit";
 	public static final String FCS_DELETE_KIT = "deleteKit";
 	public static final String FCS_ADD_ORDER = "addOrder";
 	public static final String FCS_STOP_KIT = "stopKit";
 
 	public static final String FCS_STOP_PRODUCTION = "stopProduction";
 	public static final String FCS_START_PRODUCTION = "startProduction";
 
 	public static final String FCS_UPDATE_PARTS = "updateParts";
 	public static final String FCS_UPDATE_KITS = "updateKits";
 	public static final String FCS_UPDATE_ORDERS = "updateOrders";
 
 	// CLIENT NAMES
 	// ==================================
 	// Used to identify clients.
 
 	// V0 Config
 	public static final String KIT_ROBOT_MNGR_CLIENT = "KitsRobotMngr";
 	public static final String PARTS_ROBOT_MNGR_CLIENT = "PartsRobotMngr";
 
 	// V1 Config
 	public static final String KIT_MNGR_CLIENT = "KitMngr";
 	public static final String PARTS_MNGR_CLIENT = "PartsMngr";
 	public static final String FACTORY_PROD_MNGR_CLIENT = "FactoryProdMngr";
 
 	public static final String GANTRY_ROBOT_MNGR_CLIENT = "GantryRobotMngr";
 	public static final String KIT_ASSEMBLY_MNGR_CLIENT = "KitAssemblyMngr";
 	public static final String LANE_MNGR_CLIENT = "LaneMngr";
 	
 	// UI TOOLS
 	// ==================================
 
 	public static final Border PADDING = BorderFactory.createEmptyBorder(20, 20, 20, 20);
 	public static final Border FIELD_PADDING = BorderFactory.createEmptyBorder(5, 5, 5, 5);
 	public static final Border MEDIUM_PADDING = BorderFactory.createEmptyBorder(10, 10, 10, 10);
 	public static final Border LIGHT_BOTTOM_PADDING = BorderFactory.createEmptyBorder(0, 0, 5, 0);
 	public static final Border BOTTOM_PADDING = BorderFactory.createEmptyBorder(0, 0, 20, 0);
 	public static final Border TOP_PADDING = BorderFactory.createEmptyBorder(20, 0, 5, 0);
 	public static final Border VERTICAL_PADDING = BorderFactory.createEmptyBorder(10, 0, 10, 0);
 
 	// Agent constants for StringUtil
 	/** The number of milliseconds in a second */
 	public static final long SECOND = 1000;
 	/** The number of milliseconds in a minute */
 	public static final long MINUTE = 60 * SECOND;
 	/** The number of milliseconds in an hour */
 	public static final long HOUR = 60 * MINUTE;
 	/** The number of milliseconds in a day */
 	public static final long DAY = 24 * HOUR;
 	/** The number of milliseconds in a week */
 	public static final long WEEK = 7 * DAY;
 
 	/** The line separator string on this system */
 	public static String EOL = System.getProperty("line.separator");
 
 	/** The default encoding used when none is detected */
 	public static String DEFAULT_ENCODING = "ISO-8859-1";
 
 }
