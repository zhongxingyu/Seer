 package net.geekgrandad.config;
 
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 
 import javax.xml.stream.XMLEventReader;
 import javax.xml.stream.XMLInputFactory;
 import javax.xml.stream.XMLStreamException;
 import javax.xml.stream.events.Attribute;
 import javax.xml.stream.events.EndElement;
 import javax.xml.stream.events.StartElement;
 import javax.xml.stream.events.XMLEvent;
 
 public class Config {
 	
   private boolean debug = false;
 	
   public static final int MAX_FLOORS = 3;
   public static final int MAX_ROOMS = 9;
   public static final int MAX_SENSORS = 11;
   public static final int MAX_SWITCHES = 2;
   public static final int MAX_LIGHTS = 4;
   public static final int MAX_SOCKETS = 5;
   public static final int MAX_CAMERAS = 2;
   public static final int MAX_APPLIANCES = 5;
   public static final int MAX_WINDOWS = 3;
   public static final int MAX_DOORS = 0;
   public static final int MAX_BATTERIES = 11;
   public static final int MAX_PLANTS = 1;
   public static final int MAX_TVS = 1;
   public static final int MAX_STBS = 1;
   public static final int MAX_RECEIVERS = 1;
   public static final int MAX_PHONES = 1;
   public static final int MAX_MUSIC_SERVERS = 1;
   public static final int MAX_TABLETS = 1;
   
   public int[][] roomSockets = new int[MAX_ROOMS][];
   public int[][] roomLights = new int[MAX_ROOMS][];
   public int[][] roomAppliances = new int[MAX_ROOMS][];
   public int[][] roomSensors = new int[MAX_ROOMS][];
   public int[][] roomCameras = new int[MAX_ROOMS][];
   public int[][] roomWindows = new int[MAX_ROOMS][];
   public int[][] roomSwitches = new int[MAX_ROOMS][];
   
   private int[][] floorRooms = new int[MAX_FLOORS][];
   
   private int[] sockets = new int[MAX_SOCKETS];
   private int[] lights = new int[MAX_LIGHTS];
   private int[] windows = new int[MAX_WINDOWS];
   private int[] appliances = new int[MAX_APPLIANCES];
   private int[] sensors = new int[MAX_SENSORS];
   private int[] tvs = new int[MAX_TVS];
   private int[] stbs = new int[MAX_STBS];
   private int[] receivers = new int[MAX_RECEIVERS];
   private int[] cameras = new int[MAX_CAMERAS];
   private int[] switches = new int[MAX_SWITCHES];
   
   public String[] socketTypes = new String[MAX_SOCKETS];
   public String[] lightTypes = new String[MAX_LIGHTS];
   public String[] applianceTypes = new String[MAX_APPLIANCES];
   public String[] cameraTypes = new String[MAX_CAMERAS];
   public String[] switchTypes = new String[MAX_SWITCHES];
   public String[] sensorTypes = new String[MAX_SENSORS];
   
   public String[] cameraHostNames = new String[MAX_CAMERAS];
   
   public byte[][] lightCodes = new byte[MAX_LIGHTS][];
   public int[] lightChannels = new int[MAX_LIGHTS];
   
   public byte[][] socketCodes = new byte[MAX_SOCKETS][];
   public int[] socketChannels = new int[MAX_SOCKETS];
   
   public byte[][] switchCodes = new byte[MAX_SWITCHES][];
   public int[] switchChannels = new int[MAX_SWITCHES];
   
   private int[] rooms = new int[MAX_ROOMS];
   
   private String pluginClass, pluginType;
   
   // Attributes
   private static final String ID = "id";
   private static final String NAME = "name";
   private static final String TYPE = "type";
   private static final String LINK = "link";
   private static final String USER = "user";
   private static final String PASSWORD = "password";
   private static final String RECIPIENT = "recipient";
   private static final String API_KEY = "api_key";
   private static final String FEED = "feed";
   private static final String POWER = "power";
   private static final String ENERGY = "energy";
   private static final String VOICE = "voice";
   private static final String LOCAL = "local";
   private static final String REMOTE = "remote";
   private static final String SERVER = "server";
   private static final String LISTEN_PORT = "listen_port";
   private static final String LWRF_PORT = "lwrf_port";
   private static final String IAM_PORT = "iam_port";
   private static final String RFM12_PORT = "rfm12_port";
   private static final String CODE = "code";
   private static final String DEFAULT_TEMPERATURE = "default_temperature";
   private static final String TEMPERATURE_SENSOR = "temperature_sensor";
   private static final String HUMIDITY_SENSOR = "humidity_sensor";
   private static final String BATTERY_LOW = "low";
   private static final String IAM_MIN_VALUE = "iam_min_value";
   private static final String IAM_MIN_OFF_PERIOD = "iam_min_off_period";
   private static final String SOCKET_TIMEOUT = "socket_timeout";
   private static final String OCCUPIED_INTERVAL = "occupied_interval";
   private static final String PING_TIMEOUT = "ping_timeout";
   private static final String BACKGROUND_DELAY = "background_delay";
   private static final String HOST = "host";
   private static final String CLASS = "class";
   
   // Elements
   private static final String FLOOR = "floor";
   private static final String ROOM = "room";
   private static final String SENSOR = "sensor";
   private static final String LIGHT = "light";
   private static final String SOCKET = "socket";
   private static final String SWITCH = "switch";
   private static final String APPLIANCE = "appliance";
   private static final String BATTERY = "battery";
   private static final String LIGHTLEVEL = "lightlevel";
   private static final String TEMPERATURE = "temperature";
   private static final String MOTION = "motion";
   private static final String HUMIDITY = "humidity";
   private static final String PHONE = "phone";
   private static final String MUSIC_SERVER = "musicserver";
   private static final String SPOTIFY = "spotify";
   private static final String PLAYLIST = "playlist";
   private static final String CHANNEL = "channel";
   private static final String CAMERA = "camera";
   private static final String EMONTX = "emontx";
   private static final String TV = "tv";
   private static final String STB = "stb";
   private static final String RECEIVER = "receiver";
   private static final String BLIND = "blind";
   private static final String RADIATOR = "radiator";
   private static final String PLANT = "plant";
   private static final String TABLET = "tablet";
   private static final String DOOR = "door";
   private static final String WINDOW = "window";
   private static final String WINDOWSWITCH = "windowswitch";
   private static final String THERMOSTAT = "thermostat";
   private static final String EMAIL = "email";
   private static final String COSM = "cosm";
   private static final String SPEECH = "speech";
   private static final String CONFIG = "config";
   private static final String HEATING = "heating";
   private static final String ROBOT = "robot";
   private static final String CAMERA_CONTROL = "camera_control";
   private static final String MUSIC_CONTROL = "music_control";
   private static final String SPEECH_CONTROL = "speech_control";
   private static final String EMAIL_CONTROL = "email_control";
   private static final String APPLIANCE_CONTROL = "appliance_control";
   private static final String DATALOG_CONTROL = "datalog_control";
   private static final String HEATING_CONTROL = "heating_control";
   private static final String ALARM_CONTROL = "alarm_control";
   private static final String CALENDAR_CONTROL = "calendar_control";
   private static final String LIGHT_CONTROL = "light_control";
   private static final String SENSOR_CONTROL = "sensor_control";
   private static final String PLUGIN = "plugin";
   private static final String INTERFACE = "interface";
   
   public String[] floorNames = new String[MAX_FLOORS];
   public String[] roomNames = new String[MAX_ROOMS];
   public String[] sensorNames = new String[MAX_SENSORS];
   public String[] lightNames = new String[MAX_LIGHTS];
   public String[] socketNames = new String[MAX_SOCKETS];
   public String[] switchNames = new String[MAX_SWITCHES];
   public String[] applianceNames = new String[MAX_APPLIANCES];
   public String[] windowNames = new String[MAX_WINDOWS];
   public String[] tvNames = new String[MAX_TVS];
   public String[] stbNames = new String[MAX_STBS];
   public String[] receiverNames = new String[MAX_RECEIVERS];
   public String[] cameraNames = new String[MAX_CAMERAS];
   public String[] phoneNames = new String[MAX_PHONES];
   public String[] musicServerNames = new String[MAX_MUSIC_SERVERS];
   public String[] tabletNames = new String[MAX_TABLETS];
   
   public HashMap<Integer,String> channelNames = new HashMap<Integer,String>();
   public HashMap<String,String> playlists = new HashMap<String,String>();
   public HashMap<String,String> typeClass = new HashMap<String,String>();
   public HashMap<String,String> classType = new HashMap<String,String>();
   public HashMap<String,List<String>> classInterfaces = new HashMap<String,List<String>>();
   
   private int floorId, roomId, sensorId, lightId, socketId, windowId, tvId, cameraId, applianceId;
   private int phoneId, musicServerId, channelId, stbId, receiverId, tabletId, switchId,emontxId;
   
   private String floorName, roomName, sensorName, lightName, socketName, windowName, tvName, cameraName, applianceName;
   private String channelName, stbName, receiverName, tabletName, switchName;
   
   public String phoneName, musicServerName, musicServerHost;
   
   private String playlist, link;
   
   private int numLights, numSockets, numWindows, numCameras, numAppliances, numTVs, numSTBs, 
               numReceivers, numSensors, numSwitches;
   private int numFloors = 0;
   private int numRooms = 0;
   
   public boolean email=false;
   public String emailUser, emailPassword, emailRecipient;
   
   public boolean cosm = false;
   public String cosmApiKey, cosmFeed, cosmPower, cosmEnergy;
   
   public String speechVoice, speechServer;
   public boolean speechLocal, speechRemote;
   
   public int listenPort;
   public String rfm12Port, iamPort, lwrfPort;
   
   public long[] iamCodes = new long[MAX_APPLIANCES];
   private long iamCode;
   
   public int defaultTemperature, temperatureSensor, humiditySensor;
   public int emontxBatteryLow;
   
   public int plantSensorId;
   
   public int iamMinValue, iamMinOffPeriod;
   public int musicSocketTimeout;
   
   public int occupiedInterval, pingTimeout, backgroundDelay;
   
   public int robotId;
   public String robotName;
   
   public String cameraControlClass, musicControlClass, speechControlClass, emailControlClass;
   public String applianceControlClass, datalogControlClass, heatingControlClass,alarmControlClass;
   public String calendarControlClass, lightControlClass, sensorControlClass;;
   public String cameraHost;
   
   private String socketType, lightType, switchType, sensorType, applianceType, cameraType;
   private String interfaceName;
   private ArrayList<String> interfaces = new ArrayList<String>();
   
   private String lightCode;
   private int lightChannel;
   
   private String socketCode;
   private int socketChannel;
   
   private String switchCode;
   private int switchChannel;
   
   public ArrayList<Device> devices = new ArrayList<Device>();
   
   public Config(String configFile) {
 	  readConfig(configFile);
   }
 
   public void readConfig(String configFile) {
     try {
       // First create a new XMLInputFactory
       XMLInputFactory inputFactory = XMLInputFactory.newInstance();
       // Setup a new eventReader
       InputStream in = new FileInputStream(configFile);
       XMLEventReader eventReader = inputFactory.createXMLEventReader(in);
       // Read the XML document
 
       while (eventReader.hasNext()) {
         XMLEvent event = eventReader.nextEvent();
 
         if (event.isStartElement()) {
           StartElement startElement = event.asStartElement();
           // If we have a floor element we create a new floor
           if (startElement.getName().getLocalPart() == (FLOOR)) {
             debug("Start floor");
             numRooms=0;
             numFloors++;
             
             // We read the attributes from this tag and process the is and name attributes
             Iterator<Attribute> attributes = startElement.getAttributes();
             while (attributes.hasNext()) {
               Attribute attribute = attributes.next();
               if (attribute.getName().toString().equals(ID)) {
             	floorId = Integer.parseInt(attribute.getValue());
               } else if (attribute.getName().toString().equals(NAME)) {
             	floorName = attribute.getValue();
               }
             }     
           } else if (event.asStartElement().getName().getLocalPart().equals(ROOM)) {
               debug("Start room");
               numLights=0;
               numSockets=0;
               numWindows=0;
               numCameras=0;
               numTVs = 0;
               numAppliances = 0;
               numSensors = 0;
               numSTBs = 0;
               numReceivers = 0;
               numSwitches = 0;
               
               // We read the attributes from this tag and process the is and name attributes
               Iterator<Attribute> attributes = startElement.getAttributes();
               while (attributes.hasNext()) {
                 Attribute attribute = attributes.next();
                 if (attribute.getName().toString().equals(ID)) {
               	  roomId = Integer.parseInt(attribute.getValue());
               	  rooms[numRooms++] = roomId;
                 } else if (attribute.getName().toString().equals(NAME)) {
                   roomName = attribute.getValue();
                 }
               }
           } else if (event.asStartElement().getName().getLocalPart().equals(SENSOR)) {
         	debug("Start sensor");
         	
             // We read the attributes from this tag and process the is and name attributes
             Iterator<Attribute> attributes = startElement.getAttributes();
             while (attributes.hasNext()) {
               Attribute attribute = attributes.next();
               if (attribute.getName().toString().equals(ID)) {
             	sensorId = Integer.parseInt(attribute.getValue());
             	sensors[numSensors++] = sensorId;
               } else if (attribute.getName().toString().equals(NAME)) {
             	sensorName = attribute.getValue();
               } else if (attribute.getName().toString().equals(TYPE)) {
             	sensorType = attribute.getValue();
               }
             }
           } else if (event.asStartElement().getName().getLocalPart().equals(LIGHT)) {
             debug("Start light");
             
             // We read the attributes from this tag and process the is and name attributes
             Iterator<Attribute> attributes = startElement.getAttributes();
             while (attributes.hasNext()) {
               Attribute attribute = attributes.next();
               if (attribute.getName().toString().equals(ID)) {
             	lightId = Integer.parseInt(attribute.getValue());
             	lights[numLights++] = lightId;
               } else if (attribute.getName().toString().equals(NAME)) {
             	lightName = attribute.getValue();
               } else if (attribute.getName().toString().equals(TYPE)) {
             	lightType = attribute.getValue();
               } else if (attribute.getName().toString().equals(CODE)) {
             	lightCode = attribute.getValue();
               } else if (attribute.getName().toString().equals(CHANNEL)) {
             	lightChannel = Integer.parseInt(attribute.getValue());
               }
             }
           } else if (event.asStartElement().getName().getLocalPart().equals(SOCKET)) {
             debug("Start socket");
             
             // We read the attributes from this tag and process the is and name attributes
             Iterator<Attribute> attributes = startElement.getAttributes();
             while (attributes.hasNext()) {
               Attribute attribute = attributes.next();
               if (attribute.getName().toString().equals(ID)) {
             	socketId = Integer.parseInt(attribute.getValue());
             	sockets[numSockets++] = socketId;
               } else if (attribute.getName().toString().equals(NAME)) {
             	socketName = attribute.getValue();
               } else if (attribute.getName().toString().equals(TYPE)) {
             	socketType = attribute.getValue();
               } else if (attribute.getName().toString().equals(CODE)) {
             	socketCode = attribute.getValue();
               } else if (attribute.getName().toString().equals(CHANNEL)) {
             	socketChannel = Integer.parseInt(attribute.getValue());
               }
             }
           } else if (event.asStartElement().getName().getLocalPart().equals(SWITCH)) {
               debug("Start switch");
               
               // We read the attributes from this tag and process the is and name attributes
               Iterator<Attribute> attributes = startElement.getAttributes();
               while (attributes.hasNext()) {
                 Attribute attribute = attributes.next();
                 if (attribute.getName().toString().equals(ID)) {
               	  switchId = Integer.parseInt(attribute.getValue());
               	  switches[numSwitches++] = switchId;
                 } else if (attribute.getName().toString().equals(NAME)) {
               	  switchName = attribute.getValue();
                 } else if (attribute.getName().toString().equals(TYPE)) {
                 	switchType = attribute.getValue();
                 } else if (attribute.getName().toString().equals(CODE)) {
                   switchCode = attribute.getValue();
                 } else if (attribute.getName().toString().equals(CHANNEL)) {
               	  switchChannel = Integer.parseInt(attribute.getValue());
                 }
               }
             } else if (event.asStartElement().getName().getLocalPart().equals(PHONE)) {
               debug("Start phone");
               
               // We read the attributes from this tag and process the is and name attributes
               Iterator<Attribute> attributes = startElement.getAttributes();
               while (attributes.hasNext()) {
                 Attribute attribute = attributes.next();
                 if (attribute.getName().toString().equals(ID)) {
               	  phoneId = Integer.parseInt(attribute.getValue());
                 } else if (attribute.getName().toString().equals(NAME)) {
                   phoneName = attribute.getValue();
                 }
               }
           } else if (event.asStartElement().getName().getLocalPart().equals(ROBOT)) {
               debug("Start robot");
               
               // We read the attributes from this tag and process the is and name attributes
               Iterator<Attribute> attributes = startElement.getAttributes();
               while (attributes.hasNext()) {
                 Attribute attribute = attributes.next();
                 if (attribute.getName().toString().equals(ID)) {
               	  robotId = Integer.parseInt(attribute.getValue());
                 } else if (attribute.getName().toString().equals(NAME)) {
                   robotName = attribute.getValue();
                 }
               }
           } else if (event.asStartElement().getName().getLocalPart().equals(PLUGIN)) {
               debug("Start plugin");
               pluginType = null;
               interfaces = new ArrayList<String>();
               
               // We read the attributes from this tag and process the is and name attributes
               Iterator<Attribute> attributes = startElement.getAttributes();
               while (attributes.hasNext()) {
                 Attribute attribute = attributes.next();
                 if (attribute.getName().toString().equals(TYPE)) {
                   pluginType = attribute.getValue();
                 } else if (attribute.getName().toString().equals(CLASS)) {
                   pluginClass = attribute.getValue();
                 }
               }
           } else if (event.asStartElement().getName().getLocalPart().equals(INTERFACE)) {
               debug("Start interface");
               
               // We read the attributes from this tag and process the is and name attributes
               Iterator<Attribute> attributes = startElement.getAttributes();
               while (attributes.hasNext()) {
                 Attribute attribute = attributes.next();
                 if (attribute.getName().toString().equals(NAME)) {
                   interfaceName = attribute.getValue();
                   interfaces.add(interfaceName);
                 } 
               }
           } else if (event.asStartElement().getName().getLocalPart().equals(MUSIC_SERVER)) {
               debug("Start music server");
               
               // We read the attributes from this tag and process the is and name attributes
               Iterator<Attribute> attributes = startElement.getAttributes();
               while (attributes.hasNext()) {
                 Attribute attribute = attributes.next();
                 if (attribute.getName().toString().equals(NAME)) {
                   musicServerName = attribute.getValue();
                 } else if (attribute.getName().toString().equals(HOST)) {
                   musicServerHost = attribute.getValue();
                 } else if (attribute.getName().toString().equals(SOCKET_TIMEOUT)) {
                   musicSocketTimeout = Integer.parseInt(attribute.getValue());
                 }
               }
           } else if (event.asStartElement().getName().getLocalPart().equals(TABLET)) {
               debug("Start tablet");
               
               // We read the attributes from this tag and process the is and name attributes
               Iterator<Attribute> attributes = startElement.getAttributes();
               while (attributes.hasNext()) {
                 Attribute attribute = attributes.next();
                 if (attribute.getName().toString().equals(ID)) {
               	  tabletId = Integer.parseInt(attribute.getValue());
                 } else if (attribute.getName().toString().equals(NAME)) {
                   tabletName = attribute.getValue();
                 }
               }
           } else if (event.asStartElement().getName().getLocalPart().equals(WINDOW)) {
               debug("Start window");
               
               // We read the attributes from this tag and process the is and name attributes
               Iterator<Attribute> attributes = startElement.getAttributes();
               while (attributes.hasNext()) {
                 Attribute attribute = attributes.next();
                 if (attribute.getName().toString().equals(ID)) {
               	  windowId = Integer.parseInt(attribute.getValue());
               	  windows[numWindows++] = windowId;
                 } else if (attribute.getName().toString().equals(NAME)) {
                   windowName = attribute.getValue();
                 }
               }
           } else if (event.asStartElement().getName().getLocalPart().equals(TV)) {
             debug("Start TV");
             
             // We read the attributes from this tag and process the is and name attributes
             Iterator<Attribute> attributes = startElement.getAttributes();
             while (attributes.hasNext()) {
               Attribute attribute = attributes.next();
               if (attribute.getName().toString().equals(ID)) {
             	tvId = Integer.parseInt(attribute.getValue());
             	tvs[numTVs++] = tvId;
               } else if (attribute.getName().toString().equals(NAME)) {
             	tvName = attribute.getValue();
               }
             }
           }  else if (event.asStartElement().getName().getLocalPart().equals(RECEIVER)) {
               debug("Start Receiver");
               
               // We read the attributes from this tag and process the is and name attributes
               Iterator<Attribute> attributes = startElement.getAttributes();
               while (attributes.hasNext()) {
                 Attribute attribute = attributes.next();
                 if (attribute.getName().toString().equals(ID)) {
               	  receiverId = Integer.parseInt(attribute.getValue());
               	  receivers[numReceivers++] = receiverId;
                 } else if (attribute.getName().toString().equals(NAME)) {
                   receiverName = attribute.getValue();
                 }
               }
         } else if (event.asStartElement().getName().getLocalPart().equals(CHANNEL)) {
           debug("Start Channel");
           
           // We read the attributes from this tag and process the is and name attributes
           Iterator<Attribute> attributes = startElement.getAttributes();
           while (attributes.hasNext()) {
             Attribute attribute = attributes.next();
             if (attribute.getName().toString().equals(ID)) {
           	  channelId = Integer.parseInt(attribute.getValue());
             } else if (attribute.getName().toString().equals(NAME)) {
               channelName = attribute.getValue();
             }
           }
         } else if (event.asStartElement().getName().getLocalPart().equals(PLAYLIST)) {
             debug("Start playlist");
             
             // We read the attributes from this tag and process the is and name attributes
             Iterator<Attribute> attributes = startElement.getAttributes();
             while (attributes.hasNext()) {
               Attribute attribute = attributes.next();
               if (attribute.getName().toString().equals(NAME)) {
             	  playlist = attribute.getValue();
               } else if (attribute.getName().toString().equals(LINK)) {
             	  link = attribute.getValue();
               }
             }
       } else if (event.asStartElement().getName().getLocalPart().equals(STB)) {
           debug("Start STB");
           
           // We read the attributes from this tag and process the is and name attributes
           Iterator<Attribute> attributes = startElement.getAttributes();
           while (attributes.hasNext()) {
             Attribute attribute = attributes.next();
             if (attribute.getName().toString().equals(ID)) {
           	  stbId = Integer.parseInt(attribute.getValue());
           	  stbs[numSTBs++] = stbId;
             } else if (attribute.getName().toString().equals(NAME)) {
               stbName = attribute.getValue();
             }
           }
         } else if (event.asStartElement().getName().getLocalPart().equals(LIGHTLEVEL)) {
             debug("Start light level");
         } else if (event.asStartElement().getName().getLocalPart().equals(TEMPERATURE)) {
             debug("Start temperature");
         } else if (event.asStartElement().getName().getLocalPart().equals(MOTION)) {
             debug("Start motion");
         } else if (event.asStartElement().getName().getLocalPart().equals(HUMIDITY)) {
             debug("Start humidity");
         } else if (event.asStartElement().getName().getLocalPart().equals(PLANT)) {
             debug("Start plant");
             plantSensorId = sensorId;
             
         } else if (event.asStartElement().getName().getLocalPart().equals(APPLIANCE)) {
           debug("Start appliance");
           
           // We read the attributes from this tag and process the id and name attributes
           Iterator<Attribute> attributes = startElement.getAttributes();
           while (attributes.hasNext()) {
             Attribute attribute = attributes.next();
             if (attribute.getName().toString().equals(ID)) {
           	 applianceId = Integer.parseInt(attribute.getValue());
           	 appliances[numAppliances++] = applianceId;
             } else if (attribute.getName().toString().equals(NAME)) {
               applianceName = attribute.getValue();
             } else if (attribute.getName().toString().equals(CODE)) {
               iamCode = Long.parseLong(attribute.getValue());
             } else if (attribute.getName().toString().equals(TYPE)) {
               applianceType = attribute.getValue();
             }
           }
           } else if (event.asStartElement().getName().getLocalPart().equals(CAMERA)) {
             debug("Start camera");
             
             // We read the attributes from this tag and process the id and name attributes
             Iterator<Attribute> attributes = startElement.getAttributes();
             while (attributes.hasNext()) {
               Attribute attribute = attributes.next();
               if (attribute.getName().toString().equals(ID)) {
             	cameraId = Integer.parseInt(attribute.getValue());
             	cameras[numCameras++] = cameraId;
               } else if (attribute.getName().toString().equals(NAME)) {
                 cameraName = attribute.getValue();
               } else if (attribute.getName().toString().equals(HOST)) {
                 cameraHost = attribute.getValue();
               } else if (attribute.getName().toString().equals(TYPE)) {
                 cameraType = attribute.getValue();
               }
             }
          } else if (event.asStartElement().getName().getLocalPart().equals(EMAIL)) {
              debug("Start email");
              email = true;
              
              // We read the attributes from this tag and process the id and name attributes
              Iterator<Attribute> attributes = startElement.getAttributes();
              while (attributes.hasNext()) {
                Attribute attribute = attributes.next();
                if (attribute.getName().toString().equals(USER)) {
              	 emailUser = attribute.getValue();
                } else if (attribute.getName().toString().equals(PASSWORD)) {
             	   emailPassword = attribute.getValue();
                } else if (attribute.getName().toString().equals(RECIPIENT)) {
             	   emailRecipient = attribute.getValue();
                }
              }
         } else if (event.asStartElement().getName().getLocalPart().equals(COSM)) {
             debug("Start COSM");
             cosm = true;
             
             // We read the attributes from this tag and process the id and name attributes
             Iterator<Attribute> attributes = startElement.getAttributes();
             while (attributes.hasNext()) {
               Attribute attribute = attributes.next();
               if (attribute.getName().toString().equals(API_KEY)) {
             	 cosmApiKey = attribute.getValue();
               } else if (attribute.getName().toString().equals(FEED)) {
            	     cosmFeed = attribute.getValue();
               } else if (attribute.getName().toString().equals(ENERGY)) {
            	     cosmEnergy = attribute.getValue();
               } else if (attribute.getName().toString().equals(POWER)) {
               	 cosmPower = attribute.getValue();
               }
             }
          } else if (event.asStartElement().getName().getLocalPart().equals(SPEECH)) {
              debug("Start speech");
              
              // We read the attributes from this tag and process the id and name attributes
              Iterator<Attribute> attributes = startElement.getAttributes();
              while (attributes.hasNext()) {
                Attribute attribute = attributes.next();
                if (attribute.getName().toString().equals(VOICE)) {
              	 speechVoice = attribute.getValue();
                } else if (attribute.getName().toString().equals(LOCAL)) {
             	 speechLocal = Boolean.parseBoolean(attribute.getValue());
                } else if (attribute.getName().toString().equals(REMOTE)) {
             	 speechRemote = Boolean.parseBoolean(attribute.getValue());
                } else if (attribute.getName().toString().equals(SERVER)) {
                	 speechServer = attribute.getValue();
                }
              }
           } else if (event.asStartElement().getName().getLocalPart().equals(CONFIG)) {
               debug("Start config");
               
               // We read the attributes from this tag and process the id and name attributes
               Iterator<Attribute> attributes = startElement.getAttributes();
               while (attributes.hasNext()) {
                 Attribute attribute = attributes.next();
                 if (attribute.getName().toString().equals(LISTEN_PORT)) {
               	  listenPort = Integer.parseInt(attribute.getValue());
                 } else if (attribute.getName().toString().equals(RFM12_PORT)) {
              	  rfm12Port = attribute.getValue();
                 } else if (attribute.getName().toString().equals(LWRF_PORT)) {
              	  lwrfPort = attribute.getValue();
                 } else if (attribute.getName().toString().equals(IAM_PORT)) {
                   iamPort = attribute.getValue();
                 } else if (attribute.getName().toString().equals(IAM_MIN_VALUE)) {
                   iamMinValue = Integer.parseInt(attribute.getValue());
                 } else if (attribute.getName().toString().equals(IAM_MIN_OFF_PERIOD)) {
                   iamMinOffPeriod = Integer.parseInt(attribute.getValue());
                 } else if (attribute.getName().toString().equals(OCCUPIED_INTERVAL)) {
                   occupiedInterval = Integer.parseInt(attribute.getValue());
                 } else if (attribute.getName().toString().equals(PING_TIMEOUT)) {
                   pingTimeout = Integer.parseInt(attribute.getValue());
                 } else if (attribute.getName().toString().equals(BACKGROUND_DELAY)) {
                   backgroundDelay = Integer.parseInt(attribute.getValue());
                 }
               }
            } else if (event.asStartElement().getName().getLocalPart().equals(HEATING)) {
                debug("Start heating");
                
                // We read the attributes from this tag and process the id and name attributes
                Iterator<Attribute> attributes = startElement.getAttributes();
                while (attributes.hasNext()) {
                  Attribute attribute = attributes.next();
                  if (attribute.getName().toString().equals(DEFAULT_TEMPERATURE)) {
                	    defaultTemperature = Integer.parseInt(attribute.getValue());
                  } else if (attribute.getName().toString().equals(TEMPERATURE_SENSOR)) {
                 	 temperatureSensor = Integer.parseInt(attribute.getValue());
                  } else if (attribute.getName().toString().equals(HUMIDITY_SENSOR)) {
                 	 humiditySensor = Integer.parseInt(attribute.getValue());
                  } 
                }
             } else if (event.asStartElement().getName().getLocalPart().equals(EMONTX)) {
             debug("Start emontx");
             
             // We read the attributes from this tag and process the id and name attributes
             Iterator<Attribute> attributes = startElement.getAttributes();
             while (attributes.hasNext()) {
               Attribute attribute = attributes.next();
               if (attribute.getName().toString().equals(ID)) {
             	emontxId = Integer.parseInt(attribute.getValue());
               }
             }
          } else if (event.asStartElement().getName().getLocalPart().equals(POWER)) {
              debug("Start power");
           } else if (event.asStartElement().getName().getLocalPart().equals(BATTERY)) {
              debug("Start battery");
              
           // We read the attributes from this tag and process the id and name attributes
              Iterator<Attribute> attributes = startElement.getAttributes();
              while (attributes.hasNext()) {
                Attribute attribute = attributes.next();
                if (attribute.getName().toString().equals(BATTERY_LOW)) {
              	 emontxBatteryLow = Integer.parseInt(attribute.getValue());
                } 
              }
           } 
         } else if (event.isEndElement()) {
           EndElement endElement = event.asEndElement();
           if (endElement.getName().getLocalPart() == (SOCKET)) {
           	  socketNames[socketId-1] = socketName;
           	  socketTypes[socketId-1] = socketType;
           	  socketChannels[socketId-1] = socketChannel;
           	  socketCodes[socketId-1] = hexStringToByteArray(socketCode);
           	  devices.add(new Device(socketType, socketName, socketCode, socketId, socketChannel, Device.SOCKET));
               debug("End socket id = " + socketId + ", name = " + socketNames[socketId-1]);
           } else if (endElement.getName().getLocalPart() == (LIGHT)) {
           	  lightNames[lightId-1] = lightName;
           	  lightTypes[lightId-1] = lightType;
           	  lightChannels[lightId-1] = lightChannel;
           	  lightCodes[lightId-1] = hexStringToByteArray(lightCode);
           	  devices.add(new Device(lightType, lightName, lightCode, lightId, lightChannel, Device.LIGHT));
           	  debug("End light id  = " + lightId + ", name = " + lightNames[lightId-1]);
           } else if (endElement.getName().getLocalPart() == (SWITCH)) {
           	  switchNames[switchId-1] = switchName;
           	  switchTypes[switchId-1] = switchType;
           	  switchChannels[switchId-1] = switchChannel;
           	  switchCodes[switchId-1] = hexStringToByteArray(switchCode);
           	  devices.add(new Device(switchType, switchName, switchCode, switchId, switchChannel, Device.SWITCH));
           	  debug("End switch id  = " + switchId + ", name = " + switchNames[switchId-1]);
           } else if (endElement.getName().getLocalPart() == (EMONTX)) {
         	  debug("End emontx");
           } else if (endElement.getName().getLocalPart() == (BATTERY)) {
         	  debug("End battery");
           } else if (endElement.getName().getLocalPart() == (POWER)) {
         	  debug("End power");
           } else if (endElement.getName().getLocalPart() == (PHONE)) {
           	  phoneNames[phoneId-1] = phoneName;
           	  debug("End phone");
           } else if (endElement.getName().getLocalPart() == (MUSIC_SERVER)) {
         	  debug("End music server");
           } else if (endElement.getName().getLocalPart() == (TV)) {
           	  tvNames[tvId-1] = tvName;
           	  debug("End TV");
           } else if (endElement.getName().getLocalPart() == (LIGHTLEVEL)) {
         	  debug("End light level");
           } else if (endElement.getName().getLocalPart() == (TEMPERATURE)) {
         	  debug("End temperature");
           } else if (endElement.getName().getLocalPart() == (HUMIDITY)) {
         	  debug("End humidity");
           } else if (endElement.getName().getLocalPart() == (MOTION)) {
         	  debug("End motion");
           } else if (endElement.getName().getLocalPart() == (PLANT)) {
         	  debug("End plant");
           } else if (endElement.getName().getLocalPart() == (EMAIL)) {
         	  debug("End email");
           } else if (endElement.getName().getLocalPart() == (COSM)) {
         	  debug("End COSM");
           } else if (endElement.getName().getLocalPart() == (SPEECH)) {
         	  debug("End speech");
           } else if (endElement.getName().getLocalPart() == (PLUGIN)) {
         	  debug("End plugin");
         	  if (pluginType != null) {
         		  typeClass.put(pluginType, pluginClass);
         		  classType.put(pluginClass, pluginType);
         	  }
           } else if (endElement.getName().getLocalPart() == (INTERFACE)) {
         	  debug("End interface");
         	  classInterfaces.put(pluginClass, interfaces);
           } else if (endElement.getName().getLocalPart() == (TABLET)) {
           	  tabletNames[tabletId-1] = tabletName;
           	  debug("End tablet");
           } else if (endElement.getName().getLocalPart() == (CHANNEL)) {
           	  channelNames.put(channelId, channelName);
           	  debug("End channel id = " + channelId + ", name = " + channelNames.get(channelId));
           } else if (endElement.getName().getLocalPart() == (PLAYLIST)) {
         	  playlists.put(playlist, link);
         	  debug("End playist = " + playlist + ", link = " + link);
           } else if (endElement.getName().getLocalPart() == (STB)) {
           	  stbNames[stbId-1] = stbName;
           	  debug("End STB");
           } else if (endElement.getName().getLocalPart() == (RECEIVER)) {
           	  receiverNames[receiverId-1] = receiverName;
           	  debug("End receiver");
           } else if (endElement.getName().getLocalPart() == (APPLIANCE)) {
           	  applianceNames[applianceId-1] = applianceName;
           	  applianceTypes[applianceId-1] = applianceType;
           	  iamCodes[applianceId-1] = iamCode;
           	devices.add(new Device(applianceType, applianceName, "" + iamCode, applianceId, 0, Device.APPLIANCE));
          	  debug("End appliance id  = " + applianceId + ", name = " + applianceNames[cameraId-1]);
           } else if (endElement.getName().getLocalPart() == (CAMERA)) {
           	  cameraNames[cameraId-1] = cameraName;
           	  cameraTypes[cameraId-1] = cameraType;
           	  cameraHostNames[cameraId-1] = cameraHost;
           	  debug("End camera id  = " + cameraId + ", name = " + cameraNames[cameraId-1]);
           } else if (endElement.getName().getLocalPart() == (WINDOW)) {
           	  windowNames[windowId-1] = windowName;
           	  debug("End window id = " + windowId + ", name = " + windowNames[windowId-1]);
           } else if (endElement.getName().getLocalPart() == (SENSOR)) {
           	  sensorNames[sensorId-1] = sensorName;
           	  sensorTypes[sensorId-1] = sensorType;
           	  debug("End sensor id  = " + sensorId + ", name = " + sensorNames[sensorId-1]);
           } else if (endElement.getName().getLocalPart() == (ROOM)) {
           	roomNames[roomId-1] = roomName;
           	
           	// Room sensors
           	int[] a = new int[numSensors];
           	roomSensors[roomId-1] = a;
           	for(int i=0;i<numSensors;i++) a[i] = sensors[i];
           	
           	// Room sockets
           	a = new int[numSockets];
           	roomSockets[roomId-1] = a;
           	for(int i=0;i<numSockets;i++) a[i] = sockets[i];
           	
           	// Room lights
          	a = new int[numLights];
          	roomLights[roomId-1] = a;
          	for(int i=0;i<numLights;i++) a[i] = lights[i];
          	
           	// Room appliances
          	a = new int[numAppliances];
          	roomAppliances[roomId-1] = a;
          	for(int i=0;i<numAppliances;i++) a[i] = appliances[i];
          	
           	// Room cameras
          	a = new int[numCameras];
          	roomCameras[roomId-1] = a;
          	for(int i=0;i<numCameras;i++) a[i] = cameras[i];
          	
           	// Room switches
          	a = new int[numSwitches];
          	roomSwitches[roomId-1] = a;
          	for(int i=0;i<numSwitches;i++) a[i] = switches[i];
           	
             debug("End room: id = " + roomId + ", name = " + roomNames[roomId-1]);
           } else if (endElement.getName().getLocalPart() == (FLOOR)) {
           	floorNames[floorId-1] = floorName;
           	
           	int[] a = new int[numRooms];
           	floorRooms[floorId-1] = a;
           	for(int i=0;i<numRooms;i++) a[i] = rooms[i];
           			
             debug("End floor: id = " + floorId + ", name = " + floorNames[floorId-1]);
           }
         }
       }
     } catch (FileNotFoundException e) {
       e.printStackTrace();
     } catch (XMLStreamException e) {
       e.printStackTrace();
     }
     
     printConfig();
   }
   
   public void printConfig() {
     
     System.out.println("\nFloors and rooms:");
     for(int i=0;i<MAX_FLOORS;i++) {
     	if (floorRooms[i] != null && floorRooms[i].length > 0) {
 	    	System.out.print("  " + floorNames[i] + ": ");
 	    	for(int j = 0;j<floorRooms[i].length;j++) {
 	    		System.out.print(roomNames[floorRooms[i][j]-1]);
 	    		if (j<floorRooms[i].length - 1) System.out.print(", ");
 	    	}
 	    	System.out.println();
     	}
     }
     
     System.out.println("\nRoom Sockets:");
     for(int i=0;i<MAX_ROOMS;i++) {
     	if (roomSockets[i] != null && roomSockets[i].length > 0) {
 	    	System.out.print("  " + roomNames[i] + ": ");
 	    	for(int j = 0;j<roomSockets[i].length;j++) {
 	    		int n = roomSockets[i][j];
 	    		System.out.print(n + "(" + socketNames[n-1] + "," + socketTypes[n-1] + "," + socketChannels[n-1] + "," + getHexId(socketCodes[n-1] ) +  ") ");
 	    	}
 	    	System.out.println();
     	}
     }
     
     System.out.println("\nRoom Lights:");
     for(int i=0;i<MAX_ROOMS;i++) {
     	if (roomLights[i] != null && roomLights[i].length > 0) {
 	    	System.out.print("  " + roomNames[i] + ": ");
 	    	for(int j = 0;j<roomLights[i].length;j++) {
 	    		int n = roomLights[i][j];
 	    		System.out.print(n + "(" + lightNames[n-1] + "," + lightTypes[n-1] + "," + lightChannels[n-1] + "," + getHexId(lightCodes[n-1] ) + ") ");
 	    	}
 	    	System.out.println();
     	}
     }
     
     System.out.println("\nRoom Appliances:");
     for(int i=0;i<MAX_ROOMS;i++) {
     	if (roomAppliances[i] != null && roomAppliances[i].length > 0) {
 	    	System.out.print("  " + roomNames[i] + ": ");
 	    	for(int j = 0;j<roomAppliances[i].length;j++) {
 	    		int n = roomAppliances[i][j];
 	    		System.out.print(n + "(" + applianceNames[n-1] + "," + applianceTypes[n-1] + ") ");
 	    	}
 	    	System.out.println();
     	}
     }
     
     System.out.println("\nAppliance codes:");
     for(long iamCode: iamCodes) {
     	System.out.println("  " + iamCode + " ");
     }
     
     System.out.println("\nIAM minimum value: " + iamMinValue);
     System.out.println("IAM minimum off period: " + iamMinOffPeriod);
     
     System.out.println("\nRoom Sensors:");
     for(int i=0;i<MAX_ROOMS;i++) {
     	if (roomSensors[i] != null && roomSensors[i].length > 0) {
 	    	System.out.print("  " + roomNames[i] + ": ");
 	    	for(int j = 0;j<roomSensors[i].length;j++) {
 	    		int n = roomSensors[i][j];
 	    		System.out.print(n + "(" + sensorNames[n-1] + "," + sensorTypes[n-1] + ") ");
 	    	}
 	    	System.out.println();
     	}
     }
     
     System.out.println("\nRoom Cameras:");
     for(int i=0;i<MAX_ROOMS;i++) {
     	if (roomCameras[i] != null && roomCameras[i].length > 0) {
 	    	System.out.print("  " + roomNames[i] + ": ");
 	    	for(int j = 0;j<roomCameras[i].length;j++) {
 	    		int n = roomCameras[i][j];
 	    		System.out.print(n + "(" + cameraNames[n-1] + "," + cameraTypes[n-1] + "," + cameraHostNames[n-1] + ") ");
 	    	}
 	    	System.out.println();
     	}
     }
     
     System.out.println("\nRoom Switches:");
     for(int i=0;i<MAX_ROOMS;i++) {
     	if (roomSwitches[i] != null && roomSwitches[i].length > 0) {
 	    	System.out.print("  " + roomNames[i] + ": ");
 	    	for(int j = 0;j<roomSwitches[i].length;j++) {
 	    		int n = roomSwitches[i][j];
 	    		System.out.print(n + "(" + switchNames[n-1] + "," + switchTypes[n-1] + "," + switchChannels[n-1] + "," + getHexId(switchCodes[n-1] ) +") ");
 	    	}
 	    	System.out.println();
     	}
     }
     
     System.out.println("\nemonTx id: " + emontxId);
     
     System.out.println("\nEmail: " + email);
     System.out.println("Email user: " + emailUser);
     System.out.println("Email password: " + emailPassword);
     System.out.println("Email recipient: " + emailRecipient);
     
     System.out.println("\nCOSM: " + cosm);
     System.out.println("COSM API key: " + cosmApiKey);
     System.out.println("COSM Feed: " + cosmFeed);
     System.out.println("COSM Power stream: " + cosmPower);
     System.out.println("COSM Energy stream: " + cosmEnergy);
     
     System.out.println("\nSpeech voice: " + speechVoice);
     System.out.println("Speech local: " + speechLocal);
     System.out.println("Speech remote: " + speechRemote);
     System.out.println("Speech server: " + speechServer);
     
     System.out.println("\nListen port: " + listenPort);
     System.out.println("RFM12 port: " + rfm12Port);
     System.out.println("LWRF port: " + lwrfPort);
     System.out.println("IAM port: " + iamPort);
     
     System.out.println("\nPhone name: " + phoneName);
     System.out.println("\nMusic server name: " + musicServerName);
     System.out.println("Music server host: " + musicServerHost);
     System.out.println("Music server socket timeout: " + musicSocketTimeout);
     
     System.out.println("\nDefault temperature: " + defaultTemperature);
     System.out.println("Temperature sensor: " + temperatureSensor);
     System.out.println("Humidity sensor: " + humiditySensor);
     
     System.out.println("\nEmonTx battery low level (mv): " + emontxBatteryLow);
     
     System.out.println("\nPlant sensor id: " + plantSensorId);
     
     System.out.println("\nOccupied interval: " + occupiedInterval);
     System.out.println("Ping timeout: " + pingTimeout);
     System.out.println("Background delay: " + backgroundDelay);
     
     System.out.println("\nRobot id: " + robotId);
     System.out.println("Robot name: " + robotName);
     
     System.out.println("\nPlugin Type to Class: ");
     for(String s: typeClass.keySet()) {
     	System.out.println(s + " : " + typeClass.get(s));
     }
     for(String c: classInterfaces.keySet()) {
     	System.out.print(c + ":");
     	for(String s: classInterfaces.get(c)) {
     		System.out.print(s + " ");
     	}
     	System.out.println();
     }
   }
   
   public static void main(String[] args) {
 	  new Config("conf/house.xml");
   }
   
   public byte[] hexStringToByteArray(String s) {
     int len = s.length();
     byte[] data = new byte[len / 2];
     for (int i = 0; i < len; i += 2) {
         data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                              + Character.digit(s.charAt(i+1), 16));
     }
     return data;
   }
   
   private static final char[] hexDigits = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
   
   public String byteArrayToHexString(byte [] array) {
 	  StringBuilder sb = new StringBuilder();
 	  for(byte b: array) {
           sb.append(hexDigits[(b >> 4) & 0xf]);
           sb.append(hexDigits[b & 0xf]);
 	  }
 	  return sb.toString();
   }
   
   public String getHexId(byte[] code) {
 	  if (code == null) return "null";
 	  StringBuilder sb = new StringBuilder();
 	  for(int i=0;i<6;i++) {
 		  sb.append(String.format("%02X ", code[i]));
 	  }
 	  return sb.toString();
   }
   
   public HashMap<String,String> getPlaylists() {
 	  return playlists;
   }
 
   public HashMap<Integer,String> getChannels() {
 	  return channelNames;
   }
   
   public int getEmontxId () {
 	  return emontxId;
   }
   
   private void debug(String msg) {
 	  if (debug) System.out.println(msg);
   }
   
   public Device findDevice(String technology, String code, int channel) {
 	  //System.out.println("Looking for " + technology +  "," + code + "," + channel);
 	  for(Device d : devices) {
 		  //System.out.println("Checking: " + d.getTechnology() + "," + d.getCode() + "," + d.getChannel());
 		  if (technology.equalsIgnoreCase(d.getTechnology()) && 
 			  code.equalsIgnoreCase(d.getCode()) && channel == d.getChannel()) return d;
 	  }
 	  return null;
   }
 } 
