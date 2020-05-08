 package net.geekgrandad;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.net.InetAddress;
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.util.HashMap;
 
 import net.geekgrandad.config.Config;
 import net.geekgrandad.config.Step;
 import net.geekgrandad.interfaces.AV;
 import net.geekgrandad.interfaces.AlarmControl;
 import net.geekgrandad.interfaces.Alerter;
 import net.geekgrandad.interfaces.ApplianceControl;
 import net.geekgrandad.interfaces.Browser;
 import net.geekgrandad.interfaces.CameraControl;
 import net.geekgrandad.interfaces.ComputerControl;
 import net.geekgrandad.interfaces.Controller;
 import net.geekgrandad.interfaces.DatalogControl;
 import net.geekgrandad.interfaces.EmailControl;
 import net.geekgrandad.interfaces.HTTPControl;
 import net.geekgrandad.interfaces.HeatingControl;
 import net.geekgrandad.interfaces.InfraredControl;
 import net.geekgrandad.interfaces.LightControl;
 import net.geekgrandad.interfaces.MediaControl;
 import net.geekgrandad.interfaces.PowerControl;
 import net.geekgrandad.interfaces.Provider;
 import net.geekgrandad.interfaces.RemoteControl;
 import net.geekgrandad.interfaces.Reporter;
 import net.geekgrandad.interfaces.SensorControl;
 import net.geekgrandad.interfaces.SocketControl;
 import net.geekgrandad.interfaces.SpeechControl;
 import net.geekgrandad.interfaces.SwitchControl;
 import net.geekgrandad.parser.Parser;
 import net.geekgrandad.parser.Token;
 
 import org.apache.logging.log4j.LogManager;
 import org.apache.logging.log4j.Logger;
 
 public class HouseControl implements Reporter, Alerter, Provider, Browser {
 
 	// Static data
 	private static String configFile = "conf/house.xml";
 	private static Config config;
 
 	// The name of the log4j2 logger used by this class
 	private static final String LOGGER_NAME = "HouseControl";
 
 	private static final String ERROR = "error";
 	private static final String SUCCESS = "ok";
 
 	// Variable data
 	private ServerSocket ss;
 	private Socket s;
 	private InputStream is;
 	private OutputStream os;
 	private Thread backGround = new Thread(new Background());
 	private boolean phoneConnected = false;
 	private static int numCmds = 0;
 	private static int state = 0;
 	private String cmd;
 	private Logger logger = LogManager.getLogger(LOGGER_NAME);
 	
     private CameraControl[] cameraControl = new CameraControl[Config.MAX_CAMERAS];
     private MediaControl[] mediaControl = new MediaControl[Config.MAX_MEDIA];
     private SpeechControl[] speechControl = new SpeechControl[Config.MAX_SPEECH];;
     private EmailControl emailControl;
     private AlarmControl alarmControl;
     private HeatingControl heatingControl;
     private DatalogControl datalogControl;
     private LightControl[] lightControl = new LightControl[Config.MAX_LIGHTS];
     private SocketControl[] socketControl = new SocketControl[Config.MAX_SOCKETS];
     private SwitchControl[] switchControl = new SwitchControl[Config.MAX_SWITCHES];
     private SensorControl[] sensorControl = new SensorControl[Config.MAX_SENSORS];
     private ApplianceControl[] applianceControl = new ApplianceControl[Config.MAX_APPLIANCES];
     private InfraredControl infraredControl;
     private PowerControl powerControl;
     private HTTPControl httpControl;
     private RemoteControl[]  remoteSpeechControl = new RemoteControl[Config.MAX_SPEECH];
     private RemoteControl[]  remoteMediaControl = new RemoteControl[Config.MAX_MEDIA];
     private ComputerControl computerControl;
     private RemoteControl remoteComputerControl;
     
     private Token[] tokens;
     
     private Parser parser;
     
     private int defaultRoom = 1;
     private String defaultDeviceType = null;
     private int defaultDeviceNumber = -1;
     private String defaultService = null;
     
     private int defaultTVDevice = 2;
     private int defaultMusicDevice = 5;
     private int defaultAVDevice = 3;
     private int defaultSpeechDevice = 1;
     
     private HashMap<String,Step[]> macros = new HashMap<String,Step[]>();
 
 	// Run the main thread
 	public void run() {
 		
 		// Load plugins		
 		for(String c: config.classInterfaces.keySet()) {
 			try {
 				String t = config.classType.get(c);
 				print("Loading type " + t + ": " + c);
 				@SuppressWarnings("unchecked")
 				Class<Controller> cc = (Class<Controller>) Class.forName(c);
 				Controller o = (Controller) cc.newInstance();
 				o.setProvider(this);
 				for(String s: config.classInterfaces.get(c)) {
 					print("  Interface: " + s);
 					if (s.equals("LightControl")) {
 						for(int i=0;i<Config.MAX_LIGHTS;i++) {
 							if (t == null || (config.lightTypes[i] != null && config.lightTypes[i].equals(t)))
 								lightControl[i] = (LightControl) o;
 						}
 					} else if (s.equals("SocketControl")) {
 						for(int i=0;i<Config.MAX_SOCKETS;i++) {
 							if (t == null || (config.socketTypes[i] != null && config.socketTypes[i].equals(t)))
 								socketControl[i] = (SocketControl) o;
 						}
 					} else if (s.equals("ApplianceControl")) {
 						for(int i=0;i<Config.MAX_APPLIANCES;i++) {
 							if (t == null || (config.applianceTypes[i] != null && config.applianceTypes[i].equals(t)))
 								applianceControl[i] = (ApplianceControl) o;
 						}
 					} else if (s.equals("CameraControl")) {
 						for(int i=0;i<Config.MAX_CAMERAS;i++) {
 							if (t == null || (config.cameraTypes[i] != null && config.cameraTypes[i].equals(t)))
 								cameraControl[i] = (CameraControl) o;
 						}
 					} else if (s.equals("SwitchControl")) {
 						for(int i=0;i<Config.MAX_SWITCHES;i++) {
 							if (t == null || (config.switchTypes[i] != null && config.switchTypes[i].equals(t)))
 								switchControl[i] = (SwitchControl) o;
 						}
 					} else if (s.equals("SensorControl")) {
 						for(int i=0;i<Config.MAX_SENSORS;i++) {
 							if (t == null || (config.sensorTypes[i] != null && config.sensorTypes[i].equals(t)))
 								sensorControl[i] = (SensorControl) o;
 						}
 					} else if (s.equals("MediaControl")) {
 						for(int i=0;i<Config.MAX_MEDIA;i++) {
 							if (t == null || (config.mediaTypes[i] != null && config.mediaTypes[i].equals(t))) {
 								print("Setting media control " + i + ", type = " + t);
 								mediaControl[i] = (MediaControl) o;
 							}
 						}
 					} else if (s.equals("SpeechControl")) {
 						for(int i=0;i<Config.MAX_SPEECH;i++) {
 							if (t == null || (config.speechTypes[i] != null && config.speechTypes[i].equals(t))) {
 								print("Setting speech control " + i + ", type = " + t);
 								speechControl[i] = (SpeechControl) o;
 							}
 						}
 					} else if (s.equals("PowerControl")) {
 						powerControl = (PowerControl) o;
 					} else if (s.equals("AlarmControl")) {
 						alarmControl = (AlarmControl) o;
 					} else if (s.equals("HeatingControl")) {
 						heatingControl = (HeatingControl) o;
 					} else if (s.equals("EmailControl")) {
 						emailControl = (EmailControl) o;
 					} else if (s.equals("DatalogControl")) {
 						datalogControl = (DatalogControl) o;
 					} else if (s.equals("InfraredControl")) {
 						infraredControl = (InfraredControl) o;
 					} else if (s.equals("HTTPControl")) {
 						httpControl = (HTTPControl) o;
 					} else if (s.equals("ComputerControl")) {
 						for(int i=0;i<Config.MAX_COMPUTERS;i++) {
 							if (t == null || (config.computerTypes[i] != null && config.computerTypes[i].equals(t))) {
 								print("Setting remote computer control " + i + ", type = " + t);
 								computerControl = (ComputerControl) o;
 							}
 						}
 					} else if (s.equals("RemoteControl")) {
 						for(int i=0;i<Config.MAX_SPEECH;i++) {
 							if (t == null || (config.speechTypes[i] != null && config.speechTypes[i].equals(t))) {
 								print("Setting remote speech control " + i + ", type = " + t);
 								remoteSpeechControl[i] = (RemoteControl) o;
 							}
 						}
 						for(int i=0;i<Config.MAX_MEDIA;i++) {
 							if (t == null || (config.mediaTypes[i] != null && config.mediaTypes[i].equals(t))) {
 								print("Setting remote media control " + i + ", type = " + t);
 								remoteMediaControl[i] = (RemoteControl) o;
 							}
 						}
 						for(int i=0;i<Config.MAX_COMPUTERS;i++) {
 							if (t == null || (config.computerTypes[i] != null && config.computerTypes[i].equals(t))) {
 								print("Setting remote media control " + i + ", type = " + t);
 								remoteComputerControl = (RemoteControl) o;
 							}
 						}
 					} 
 				}
 			} catch (ClassNotFoundException e) {
 				e.printStackTrace();
 			} catch (InstantiationException e) {
 				e.printStackTrace();
 			} catch (IllegalAccessException e) {
 				e.printStackTrace();
 			}
 		}
 		
 		// Create and configure a parser
 		parser = new Parser();
 		parser.setReporter(this);
 		parser.setConfig(config);
 
 		// Start background thread
 		backGround.setDaemon(true);
 		backGround.start();
 
 		// Start the server socket
 		try {
 			ss = new ServerSocket(config.listenPort);
 		} catch (IOException e) {
 			error("Failed to create server socket:" + e.getMessage());
 			System.exit(1);
 		}
 
 		// Main loop
 		while (true) {
 			try {
 				// Wait for a connection
 				is = null;
 				os = null;
 				state = 1;
 				s = ss.accept();
 				state = 2;
 				// Open input and output streams
 				is = s.getInputStream();
 				os = s.getOutputStream();
 
 				// Read a command line
 				cmd = getCmd();
 
 				state = 3;
 				debug("cmd: " + cmd);
 				execute(cmd);
 				state = 4;
 
 			} catch (Exception e) {
 				error("Exception executing cmd: " + e);
 			} finally {
 				// After a line read, close streams and socket
 				if (is != null)
 					try {
 						is.close();
 					} catch (IOException e) {
 					}
 				if (os != null)
 					try {
 						os.close();
 					} catch (IOException e) {
 					}
 				if (s != null)
 					try {
 						s.close();
 					} catch (IOException e) {
 					}
 			}
 		}
 	}
 
 	// Check if host is reachable
 	public boolean isReachableByPing(String host) {
 		try {
 			return InetAddress.getByName(host).isReachable(config.pingTimeout);
 		} catch (Exception e) {
 			return false;
 		}
 	}
 
 	// main method
 	public static void main(String[] args) {
 		if (args.length > 0) configFile = "conf/" + args[0] + ".xml";
 		System.out.println("Config file is " + configFile);
 		config = new Config(configFile);
 		(new HouseControl()).run();
 	}
 	
 	// Thread for implementing time consuming background tasks, and doing some
 	// periodic checks
 	class Background implements Runnable {
 		@Override
 		public void run() {
 			for (;;) {
 				if (alarmControl != null && mediaControl[4] != null) {
 					alarmControl.checkAlarm();
 				}
 				
 				/*if (mediaControl[defaultMusicDevice-1] != null) {
 					try {
 						volume = mediaControl[defaultMusicDevice-1].getVolume(defaultMusicDevice);
 						musicOn = true;
 					} catch (IOException e) {
 						musicOn = false;
 					}
 					print("Music on: " + musicOn);
 				}*/
 				
 				if (config.phoneName != null) phoneConnected = isReachableByPing(config.phoneName);
 
 				print("Number of commands: " + numCmds + " state: " + state
 						+ " command: " + cmd);
 							
 				delay(config.backgroundDelay);
 			}
 		}
 	}
 
 	// Send string reply
 	private void writeString(String s) throws IOException {
 		os.write(s.getBytes());
 	}
 
 	// Print a message on the console
 	public void print(String s) {
 		logger.info(s);
 	}
 
 	// Print a message on the console
 	public void debug(String s) {
 		logger.debug(s);
 	}
 
 	// Print an error message
 	public void error(String s) {
 		logger.error(s);
 	}
 
 	// Get the command from the socket
 	private String getCmd() {
 		numCmds++;
 		StringBuilder s = new StringBuilder();
 		try {
 			while (true) {
 				int b = is.read();
 				if (b < 0)
 					break;
 				//debug("Read: " + b);
 				if (b == '\r' || b == '\n') {
 					return s.toString();
 				} else
 					s.append((char) b);
 			}
 		} catch (IOException e) {
 			error("IOException reading command");
 		}
 		return null;
 	}
 
 	// Speak a message locally
 	public void say(int n, String msg) {
 		print("Saying " + msg);
 		if (speechControl[n-1] != null) {
 			speechControl[n-1].say(n, msg);
 		}
 	}
 
 	// Send an email
 	public void sendEmail(String subject, String msg) {
 		if (emailControl != null) emailControl.email(subject, msg);
 	}
 
 	String parse(String source) throws IOException {
 		debug("Command is " + source);
 		tokens = parser.parse(source);
 		if (tokens == null) return ERROR;
 
 		int numTokens = tokens.length;
 		// Deal with various abbreviations
 		if (numTokens == 1 && tokens[0].getType() == Parser.QUANTITY) {
 			// Single token quantity command
 			// Add sensor 1
 			expandTokens(2);
 			tokens[2] = tokens[0];
 			tokens[1].setValue("1");
 			tokens[1].setType(Parser.NUMBER);
 			tokens[0] = new Token(Parser.devices[Parser.SENSOR],Parser.DEVICE, -1);
 			numTokens = 3;
 		} else if (numTokens == 2 && tokens[0].getType() == Parser.QUANTITY && tokens[1].getType() == Parser.NUMBER) {
 			// Replace quantity n with sensor sensor n quantity
 			expandTokens(1);
 			tokens[2] = tokens[0];
 			tokens[0] = new Token(Parser.devices[Parser.SENSOR],Parser.DEVICE, -1);
 			numTokens = 3;
 		} else if (numTokens >= 2 && tokens[0].getType() == Parser.DEVICE && tokens[1].getType() != Parser.NUMBER) {
 			// Assume device number one
 			expandTokens(1);
 			if (tokens.length > 3) tokens[3] = tokens[2];
 			tokens[2] = tokens[1];
 			tokens[1] = new Token("1", Parser.NUMBER, -1);
 			numTokens += 1;
 		} else if (tokens[0].getType() == Parser.SOCKET_NAME) {
 			// Replace name with socket n
 			int n = parser.find(tokens[0].getValue(),config.socketNames);
 			expandTokens(1);
 			if (tokens.length > 3) tokens[3] = tokens[2];
 			tokens[2] = tokens[1];
 			tokens[1] = new Token("" + (n + 1), Parser.NUMBER, -1);
 			tokens[0] = new Token(Parser.devices[Parser.SOCKET],Parser.DEVICE, -1);
 			numTokens += 1;
 		} else if (tokens[0].getType() == Parser.COMPUTER_NAME) {
 			// Replace name with computer n
 			int n = parser.find(tokens[0].getValue(),config.computerNames);
 			expandTokens(1);
 			if (tokens.length > 4) tokens[4] = tokens[3];
 			if (tokens.length > 3) tokens[3] = tokens[2];
 			tokens[2] = tokens[1];
 			tokens[1] = new Token("" + (n + 1), Parser.NUMBER, -1);
 			tokens[0] = new Token(Parser.devices[Parser.COMPUTER],Parser.DEVICE, -1);
 			numTokens += 1;
 		} else if (tokens[0].getType() == Parser.LIGHT_NAME) {
 			// Replace name with light n
 			int n = parser.find(tokens[0].getValue(),config.lightNames);
 			expandTokens(1);
 			if (tokens.length > 3) tokens[3] = tokens[2];
 			tokens[2] = tokens[1];
 			tokens[1] = new Token("" + (n + 1), Parser.NUMBER, -1);
 			tokens[0] = new Token(Parser.devices[Parser.LIGHT],Parser.DEVICE, -1);
 			numTokens += 1;
 		} else if (tokens[0].getType() == Parser.APPLIANCE_NAME) {
 			// Replace name with appliance n
 			int n = parser.find(tokens[0].getValue(),config.applianceNames);
 			expandTokens(1);
 			if (tokens.length > 3) tokens[3] = tokens[2];
 			tokens[2] = tokens[1];
 			tokens[1] = new Token("" + (n + 1), Parser.NUMBER, -1);
 			tokens[0] = new Token(Parser.devices[Parser.APPLIANCE],Parser.DEVICE, -1);
 			numTokens += 1;
 		} else if (tokens[0].getType() == Parser.CAMERA_NAME) {
 			// Replace name with socket n
 			int n = parser.find(tokens[0].getValue(),config.cameraNames);
 			expandTokens(1);
 			if (tokens.length > 3) tokens[3] = tokens[2];
 			tokens[2] = tokens[1];
 			tokens[1] = new Token("" + (n + 1), Parser.NUMBER, -1);
 			tokens[0] = new Token(Parser.devices[Parser.CAMERA],Parser.DEVICE, -1);
 			numTokens += 1;
 		} else if (tokens[0].getType() == Parser.MEDIA_NAME) {
 			// Replace name with media n
 			int n = parser.find(tokens[0].getValue(),config.mediaNames);
 			expandTokens(1);
 			if (tokens.length > 4) tokens[4] = tokens[3];
 			if (tokens.length > 3) tokens[3] = tokens[2];
 			tokens[2] = tokens[1];
 			tokens[1] = new Token("" + (n + 1), Parser.NUMBER, -1);
 			tokens[0] = new Token(Parser.devices[Parser.MEDIA],Parser.DEVICE, -1);
 			numTokens += 1;
 		} else if (tokens[0].getType() == Parser.SPEECH_NAME) {
 			// Replace name with speech n
 			int n = parser.find(tokens[0].getValue(),config.speechNames);
 			expandTokens(1);
 			if (tokens.length > 4) tokens[4] = tokens[3];
 			if (tokens.length > 3) tokens[3] = tokens[2];
 			tokens[2] = tokens[1];
 			tokens[1] = new Token("" + (n + 1), Parser.NUMBER, -1);
 			tokens[0] = new Token(Parser.devices[Parser.SPEECH],Parser.DEVICE, -1);
 			numTokens += 1;
 		} else if (tokens[0].getType() == Parser.VT_ACTION || tokens[0].getType() == Parser.PAN_ACTION) {
 			// Assume media 2
 			expandTokens(2);
 			if (tokens.length > 3) tokens[3] = tokens[1];
 			tokens[2] = tokens[0];
 			tokens[1] = new Token("" + defaultTVDevice, Parser.NUMBER, -1);
 			tokens[0] = new Token(Parser.devices[Parser.MEDIA],Parser.DEVICE, -1);
 			numTokens += 2;
 		} else if (tokens[0].getType() == Parser.SPEECH_ACTION) {
 			// Use default speech device
 			expandTokens(2);
 			if (tokens.length > 3) tokens[3] = tokens[1];
 			tokens[2] = tokens[0];
 			tokens[1] = new Token("" + defaultSpeechDevice, Parser.NUMBER, -1);
 			tokens[0] = new Token(Parser.devices[Parser.SPEECH],Parser.DEVICE, -1);
 			numTokens += 2;
 		} else if (tokens[0].getType() == Parser.SERVICE || tokens[0].getType() == Parser.DIGIT || tokens[0].getType() == Parser.COLOR) {
 			// Assume media 2
 			expandTokens(2);
 			if (tokens.length > 3) tokens[3] = tokens[1];
 			tokens[2] = tokens[0];
 			tokens[1] = new Token("" + defaultTVDevice, Parser.NUMBER, -1);
 			tokens[0] = new Token(Parser.devices[Parser.MEDIA],Parser.DEVICE, -1);
 			numTokens += 2;
 		} else if (tokens[0].getType() == Parser.ROBOT_ACTION) {
 			// Assume robot 1
 			expandTokens(2);
 			if (tokens.length > 3) tokens[3] = tokens[1];
 			tokens[2] = tokens[0];
 			tokens[1] = new Token("1", Parser.NUMBER, -1);
 			tokens[0] = new Token(Parser.devices[Parser.ROBOT], Parser.DEVICE, -1);
 			numTokens += 2;
 		} else if (tokens[0].getType() == Parser.AV_ACTION) {
 			// Assume media 3
 			expandTokens(2);
 			if (tokens.length > 3) tokens[3] = tokens[1];
 			tokens[2] = tokens[0];
 			tokens[1] = new Token("" + defaultAVDevice, Parser.NUMBER, -1);
 			tokens[0] = new Token(Parser.devices[Parser.MEDIA], Parser.DEVICE, -1);
 			numTokens += 2;
 		} else if (tokens[0].getType() == Parser.MUSIC_ACTION) {
 			// Assume media 5
 			debug("Music action");
 			expandTokens(2);
 			if (tokens.length > 3)tokens[3] = tokens[1];
 			tokens[2] = tokens[0];
 			tokens[1] = new Token("" +  + defaultMusicDevice, Parser.NUMBER, -1);
 			tokens[0] = new Token(Parser.devices[Parser.MEDIA], Parser.DEVICE, -1);
 			numTokens += 2;
 		} else if (tokens[0].getType() == Parser.DEVICE_SET) {
 			// assume default room
 			expandTokens(2);
 			if (tokens.length > 4) tokens[4] = tokens[2];
 			if (tokens.length > 3)tokens[3] = tokens[1];
 			tokens[2] = tokens[0];
 			tokens[1] = new Token("" + defaultRoom, Parser.NUMBER, -1);
 			tokens[0] = new Token(Parser.areas[Parser.ROOM], Parser.AREA, -1);
 			numTokens += 2;			
 		}
 		
 		// Print tokens
 		for(int i=0;i<tokens.length;i++) {
 			print("Token " + (i+1) + ": " + tokens[i].getValue());
 			print("Token " + (i+1) + " type: " + tokens[i].getType());
 		}
 		
 		// Execute command
 		switch (tokens[0].getType()) {
 		default:
 			error("Command not recognised: " + source);
 			return ERROR;
 		case Parser.DEFAULT:
 			print("Default command");
 			switch (parser.find(tokens[1].getValue(), Parser.defaults)) {
 			case Parser.DEFAULT_LOCATION:
 				int n = Integer.parseInt(tokens[2].getValue());
 				print("Setting default room to " + n);
 				defaultRoom = n;
 				return SUCCESS;
 			default:
 				error("Default command not supported");
 				return ERROR;
 			}
 		case Parser.DEVICE:
 			// Its a device
 			debug("Device command");
 			
 			if (numTokens < 2) {
 				error("Invalid device command");
 				return ERROR;
 			}
 
 			switch (tokens[1].getType()) {
 			case Parser.NUMBER:
 				debug("Device command with number");
 				int n = Integer.parseInt(tokens[1].getValue()) -1;
 				
 				if (n < 0) {
 					error("Invalid device number: " + (n+1));
 					return ERROR;
 				}
 				
 				int device = parser.find(tokens[0].getValue(), Parser.devices);
 				debug("Device = " + device);
 				
 				// Check if it is remote
 				if (device == Parser.SPEECH && config.speechTypes[n].equals("remote")) {
 					String server = config.speechServers[n];
 					
 					print("Sending " + cmd + " to " + server);
 					
 					return remoteSpeechControl[n].send(n+1, server, cmd);				
 				} else if (device == Parser.MEDIA && config.mediaTypes[n].equals("remote")) {
 					String server = config.mediaServers[n];
 					
 					print("Sending " + cmd + " to " + server);
 					
 					return remoteMediaControl[n].send(n+1, server, cmd);				
 				} else if (device == Parser.COMPUTER && config.computerTypes[n].equals("remote")) {
 					String server = config.computerServers[n];
 					
 					print("Sending " + cmd + " to " + server);
 					
 					return remoteComputerControl.send(n+1, server, cmd);				
 				}
 				
 				if (numTokens > 2 && tokens[2].getType() == Parser.ACTION) {
 					int act = parser.find(tokens[2].getValue(), Parser.actions);
 					switch (act) {
 					case Parser.ON:
 					case Parser.OFF:
 						switch (device) {
 						case Parser.MEDIA:
 							if (n >= Config.MAX_MEDIA || mediaControl[n] == null) {
 								error("Invalid device number: " + (n+1));
 								return ERROR;
 							}
 							
 							if(act == Parser.ON) mediaControl[n].turnOn(n+1);
 							else mediaControl[n].turnOff(n+1);
 							return SUCCESS;
 						case Parser.LIGHT:
 							lightControl[n].switchLight(n,(act == Parser.ON));
 							return SUCCESS;
 						case Parser.SOCKET:
 							socketControl[n].switchSocket(n, (act == Parser.ON));
 							return SUCCESS;
 						case Parser.APPLIANCE:
 							applianceControl[n].switchAppliance(n, (act == Parser.ON));
 							return SUCCESS;
 						case Parser.HEATING:
 							heatingControl.setHeating(act == Parser.ON);
 							return SUCCESS;
 						case Parser.CAMERA:
 						case Parser.SWITCH:
 						case Parser.SENSOR:
 						case Parser.PHONE:
 						case Parser.SPEECH:
 						case Parser.COMPUTER:
 							error("Cannot turn this device on or off");
 							return ERROR;
 						default:
 							error("Unsupported device");
 							return ERROR;
 						}			
 					case Parser.STATUS:
 						switch (device) {
 						case Parser.SOCKET:
 							return switchString(socketControl[n].getSocketStatus(n));
 						case Parser.LIGHT:
 							return switchString(lightControl[n].getLightStatus(n));
 						case Parser.APPLIANCE:
 							return switchString(applianceControl[n].getApplianceStatus(n));
 						case Parser.PHONE:
 							return switchString(phoneConnected);
 						case Parser.SWITCH:
 							return switchString(switchControl[n].getSwitchStatus(n));
 						case Parser.HEATING:
 							return switchString(heatingControl.getHeatingStatus());
 						case Parser.ALARM:
 							int togo = alarmControl.getAlarmTime();
 							return switchString(togo > 0);
 						case Parser.COMPUTER:
 							return switchString(true);
 						default:
 							error("Cannot return the status of this device");
 							return ERROR;
 						}
 					case Parser.VALUE:
 						switch (device) {
 						case Parser.APPLIANCE:
 							return "" + applianceControl[n].getAppliancePower(n);
 						case Parser.LIGHT:
 							return "" + lightControl[n].getLightLevel(n);
 						case Parser.HEATING:
 							return "" + heatingControl.getRequiredTemperature();
 						case Parser.ALARM:
 							int togo = alarmControl.getAlarmTime();
 							return "" + (togo < 0 ? 0 : togo);
 						default:
 							error("This device does not have a value");
 							return ERROR;
 						}
 					case Parser.SET:						
 						if (numTokens < 4) {
 							error("Set command needs a value");
 							return ERROR;
 						}
 						
 						switch (device) {
 						case Parser.LIGHT:
 							lightControl[n].dimLight(n,Integer.parseInt(tokens[3].getValue()));
 							return SUCCESS;
 						case Parser.HEATING:
 							heatingControl.setRequiredTemperature(Integer.parseInt(tokens[3].getValue()));
 							return SUCCESS;
 						case Parser.MEDIA:
 							mediaControl[n].setVolume(n+1, Integer.parseInt(tokens[3].getValue()));
 							return SUCCESS;
 						case Parser.ALARM:
 							alarmControl.setAlarmTime(Integer.parseInt(tokens[3].getValue()));
 							return SUCCESS;
 						default:
 							error("This device cannot have a value set");
 							return ERROR;
 						}
 					case Parser.CLEAR:
 						if (device == Parser.ALARM) {
 							alarmControl.clearAlarm();
 							return SUCCESS;
 						} else {
 							error("Device does not support clear");
 							return ERROR;
 						}
 					case Parser.DELETE:
 						if (device == Parser.MEDIA) {
 							mediaControl[n].delete(n+1);
 							return SUCCESS;
 						} else {
 							error("Device does not support DELETE");
 							return ERROR;
 						}
 					case Parser.BACK:
 						if (device == Parser.MEDIA) {
 							mediaControl[n].back(n+1);
 							return SUCCESS;
 						} else {
 							error("Device does not support BACK");
 							return ERROR;
 						}	
 					default:
 						error("Unsupported action");
 						return ERROR;
 					}
 				} else if (device == Parser.MEDIA && numTokens > 2 && tokens[2].getType() == Parser.DIGIT) {
 					if (n >= Config.MAX_MEDIA || mediaControl[n] == null) {
 						error("Invalid device number: " + (n+1));
 						return ERROR;
 					}
 					mediaControl[n].digit(n+1, parser.find(tokens[2].getValue(),Parser.digits));
 					return SUCCESS;
 				} else if (device == Parser.MEDIA && numTokens > 2 && tokens[2].getType() == Parser.COLOR) {
 					if (n >= Config.MAX_MEDIA || mediaControl[n] == null) {
 						error("Invalid device number: " + (n+1));
 						return ERROR;
 					}
 					switch(parser.find(tokens[2].getValue(),Parser.colors)) {
 					case Parser.BLUE:
 						mediaControl[n].color(n+1, AV.BLUE);
 						return SUCCESS;
 					case Parser.RED:
 						mediaControl[n].color(n+1, AV.RED);
 						return SUCCESS;
 					case Parser.YELLOW:
 						mediaControl[n].color(n+1, AV.YELLOW);
 						return SUCCESS;
 					case Parser.GREEN:
 						mediaControl[n].color(n+1, AV.GREEN);
 						return SUCCESS;
 					}
 				} else if (device == Parser.MEDIA && numTokens > 2 && tokens[2].getType() == Parser.SERVICE) {
 					String service = tokens[2].getValue();
 					switch(parser.find(service,Parser.services)) {
 					case Parser.VOLUME:
 						if (numTokens == 3) {
 							return "" + mediaControl[n].getVolume(n+1);
 						} else  if (tokens[3].getType() == Parser.PAN_ACTION) {
 							switch(parser.find(tokens[3].getValue(),Parser.panActions)) {
 							case Parser.UP:
 								mediaControl[n].volumeUp(n+1);
 								return SUCCESS;
 							case Parser.DOWN:
 								mediaControl[n].volumeDown(n+1);
 								return SUCCESS;
 							default:
 								error("Invalid volume command");
 								return ERROR;
 							}
 						} else {
 							error("Invalid volume command");
 							return ERROR;
 						}
 					case Parser.CHANNEL:
 						if (device == Parser.MEDIA) {
 							if (tokens[3].getType() == Parser.PAN_ACTION) {
 								switch(parser.find(tokens[3].getValue(),Parser.panActions)) {
 								case Parser.UP:
 									mediaControl[n].channelUp(n+1);
 									return SUCCESS;
 								case Parser.DOWN:
 									mediaControl[n].channelDown(n+1);
 									return SUCCESS;
 								default:
 									error("Invalid channel command");
 									return ERROR;
 								}
 							} else if (tokens[3].getType() == Parser.NUMBER) {
 								mediaControl[n].setChannel(n+1, Integer.parseInt(tokens[3].getValue()));
 								return SUCCESS;
 							} else {
 								error("Invalid channel command");
 								return ERROR;
 							}
 						} else {
 							error("Device does not support channel");
 							return ERROR;
 						}
 					case Parser.SUBTITLES:
 						mediaControl[n].option(n+1, service);
 						return SUCCESS;
 					case Parser.GUIDE:
 					case Parser.HOME:
 					case Parser.SHOWS:
 					case Parser.BROADCAST:
 					case Parser.INFO:
 					case Parser.IPLAYER:
 					case Parser.ITVPLAYER:
 					case Parser.OD4:
 					case Parser.DEMAND5:
 					case Parser.SETTINGS:
 					case Parser.PARENTAL:
 					case Parser.PURCHASE:
 					case Parser.MESSAGES:
 					case Parser.HELP:
 					case Parser.ONDEMAND:
 					case Parser.CATCHUP:
 					case Parser.TVXL:
 					case Parser.MOVIES:				
 					case Parser.MUSIC:
 					case Parser.SPORTS:
 					case Parser.GAMES:
 					case Parser.LIFESTYLE:
 					case Parser.NEWS:
 					case Parser.APPS:
 					case Parser.FEATURED:
 					case Parser.SYSTEM:
 					case Parser.RENTALS:						
 					case Parser.YOUTUBE:
 					case Parser.PAY_PER_VIEW:
 					case Parser.SERIES:
 					case Parser.PLANNED:
 					case Parser.WISHLIST:
 					case Parser.BROWSE:
 					case Parser.SEARCH:
 					case Parser.WEATHER:
 					case Parser.NAVI_X:
 					case Parser.TED:
 					case Parser.CONTEXT:
 					case Parser.PICTURES:
 					case Parser.FILE_MANAGER:
 					case Parser.VIDEO:
 					case Parser.SKY_ON_DEMAND:
 					case Parser.PLAYLISTS:
 					case Parser.DILBERT:
 						mediaControl[n].select(n+1, service);
 						return SUCCESS;
 					case Parser.SOURCE:
 						print("Accessing mediaControl " + n + " = " + mediaControl[n]);
 						switch(parser.find(tokens[3].getValue(),Parser.source)) {
 						case Parser.STB:
 							mediaControl[n].setSource(n+1, AV.SOURCE_STB);
 							return SUCCESS;
 						case Parser.DVD:
 							mediaControl[n].setSource(n+1, AV.SOURCE_DVD);
 							return SUCCESS;
 						case Parser.BLUERAY:
 							mediaControl[n].setSource(n+1, AV.SOURCE_BLUERAY);
 							return SUCCESS;	
 						default:
 							error("Unknown source");
 							return ERROR;
 						}
 					case Parser.THUMBS:
 						if (tokens[3].getType() == Parser.PAN_ACTION) {
 							switch(parser.find(tokens[3].getValue(),Parser.panActions)) {
 							case Parser.UP:
 								mediaControl[n].thumbsUp(n+1);
 								return SUCCESS;
 							case Parser.DOWN:
 								mediaControl[n].thumbsDown(n+1);
 								return SUCCESS;
 							default:
 								error("Invalid thumbs command");
 								return ERROR;
 							}
 						} else {
 							error("Invalid thumbs command");
 							return ERROR;
 						}
 					case Parser.PIN:
 						mediaControl[n].pin(n+1);
 						return SUCCESS;
 					default:
 						error("Unsupported service: " + service);
 						return ERROR;
 					}
 				} else if (device == Parser.MEDIA && numTokens > 2 && tokens[2].getType() == Parser.VT_ACTION) {
 					if (n >= Config.MAX_MEDIA || mediaControl[n] == null) {
 						error("Invalid device number: " + (n+1));
 						return ERROR;
 					}
 					switch(parser.find(tokens[2].getValue(),Parser.vtActions)) {
 					case Parser.OK:
 						mediaControl[n].ok(n+1);
 						return SUCCESS;
 					case Parser.LAST_CH:
 						mediaControl[n].lastChannel(n+1);
 						return SUCCESS;
 					case Parser.SEND:
 						int code = Integer.parseInt(tokens[3].getValue(), 16);
 						infraredControl.sendCommand(n+1, code);
 						return SUCCESS;
 					case Parser.TYPE:
 						mediaControl[n].type(n+1, tokens[3].getValue());
 						return SUCCESS;
 					case Parser.PAGE_UP:
 						mediaControl[n].pageUp(n+1);
 						return SUCCESS;
 					case Parser.PAGE_DOWN:
 						mediaControl[n].pageDown(n+1);
 						return SUCCESS;						
 					default:
 						error("Unsupported TiVo command");
 						return ERROR;
 					}
 				} else if (device == Parser.MEDIA && numTokens > 2 && tokens[2].getType() == Parser.AV_ACTION) {
 					if (n >= Config.MAX_MEDIA || mediaControl[n] == null) {
 						error("Invalid device number: " + (n+1));
 						return ERROR;
 					}
 					switch(parser.find(tokens[2].getValue(), Parser.avActions)) {	
 					case Parser.MUTE:
 						mediaControl[n].mute(n+1);
 						return SUCCESS;
 					case Parser.VOLUP:
 						mediaControl[n].volumeUp(n+1);
 						return SUCCESS;
 					case Parser.VOLDOWN:
 						mediaControl[n].volumeDown(n+1);
 						return SUCCESS;
 					default:
 						error("Unsupported AV action");
 						return ERROR;
 					}
 				} else if (device == Parser.MEDIA && numTokens > 2 && tokens[2].getType() == Parser.PAN_ACTION) {
 					if (n >= Config.MAX_MEDIA || mediaControl[n] == null) {
 						error("Invalid device number: " + (n+1));
 						return ERROR;
 					}
 					switch(parser.find(tokens[2].getValue(), Parser.panActions)) {
 					case Parser.UP:
 						mediaControl[n].up(n+1);
 						return SUCCESS;
 					case Parser.DOWN:
 						mediaControl[n].down(n+1);
 						return SUCCESS;
 					case Parser.LEFT:
 						mediaControl[n].left(n+1);
 						return SUCCESS;
 					case Parser.RIGHT:
 						mediaControl[n].right(n+1);
 						return SUCCESS;
 					default:
 						error("Invalid pan action");
 						return ERROR;
 					}
 				} else if (device == Parser.MEDIA && numTokens > 2 && tokens[2].getType() == Parser.MUSIC_ACTION) {
 					if (n >= Config.MAX_MEDIA || mediaControl[n] == null) {
 						error("Invalid device number: " + (n+1));
 						return ERROR;
 					}
 					switch(parser.find(tokens[2].getValue(), Parser.musicActions)) {
 					case Parser.PLAY:
 						if (tokens.length > 3) {
 							mediaControl[n].start(n+1, cmd.substring(cmd.indexOf("play")+5), true);
 							return SUCCESS;
 						} else {
 							mediaControl[n].play(n+1);
 						}
 						return SUCCESS;
 					case Parser.START:
 						mediaControl[n].start(n+1, cmd.substring(cmd.indexOf("start")+6), true);
 						return SUCCESS;
 					case Parser.PLAYER:
 						if (tokens.length > 4) {
 							error("Too many parameters");
 							return ERROR;
 						}
 						mediaControl[n].setPlayer(n+1, Integer.parseInt(tokens[3].getValue()));
 						return SUCCESS;
 					case Parser.OPEN:
 						mediaControl[n].open(n+1, cmd.substring(cmd.indexOf("open")+5), true);
 						return SUCCESS;
 					case Parser.PAUSE:
 						if (tokens.length > 3) {
 							error("Too many parameters");
 							return ERROR;
 						}
 						print("Calling media " + n + " pause");
 						mediaControl[n].pause(n+1);
 						return SUCCESS;
 					case Parser.STOP:
 						if (tokens.length > 3) {
 							error("Too many parameters");
 							return ERROR;
 						}
 						mediaControl[n].stop(n+1);
 						return SUCCESS;
 					case Parser.FF:
 						if (tokens.length > 3) {
 							error("Too many parameters");
 							return ERROR;
 						}
 						mediaControl[n].ff(n+1);
 						return SUCCESS;
 					case Parser.FB:
 						if (tokens.length > 3) {
 							error("Too many parameters");
 							return ERROR;
 						}
 						mediaControl[n].fb(n+1);
 						return SUCCESS;
 					case Parser.SKIP:
 						if (tokens.length > 3) {
 							error("Too many parameters");
 							return ERROR;
 						}
 						mediaControl[n].skip(n+1);
 						return SUCCESS;	
 					case Parser.SKIPB:
 						if (tokens.length > 3) {
 							error("Too many parameters");
 							return ERROR;
 						}
 						mediaControl[n].skipb(n+1);
 						return SUCCESS;	
 					case Parser.SLOW:
 						if (tokens.length > 3) {
 							error("Too many parameters");
 							return ERROR;
 						}
 						mediaControl[n].slow(n+1);
 						return SUCCESS;	
 					case Parser.PLAYLIST:
 						if (tokens.length > 3) {
 							error("Too many parameters");
 							return ERROR;
 						}
 						return mediaControl[n].getPlaylist(n+1);
 					case Parser.TRACK:
 						if (tokens.length > 3) {
 							error("Too many parameters");
 							return ERROR;
 						}
 						return mediaControl[n].getTrack(n+1);						
 					case Parser.ARTIST:
 						if (tokens.length > 3) {
 							error("Too many parameters");
 							return ERROR;
 						}
 						return mediaControl[n].getArtist(n+1);
 					case Parser.ALBUM:
 						if (tokens.length > 3) {
 							error("Too many parameters");
 							return ERROR;
 						}
 						return mediaControl[n].getAlbum(n+1);
 					default:
 						error("Invalid media command");
 						return ERROR;
 					}
 				} else if (device == Parser.COMPUTER && numTokens > 2 && tokens[2].getType() == Parser.COMPUTER_ACTION) {
 					if (n >= Config.MAX_COMPUTERS || computerControl == null) {
 						error("Invalid computer device number: " + (n+1));
 						return ERROR;
 					}
 					switch(parser.find(tokens[2].getValue(), Parser.computerActions)) {
 					case Parser.REBOOT:
 						computerControl.reboot();
 						return SUCCESS;
 					case Parser.SHUT_DOWN:
 						computerControl.shutdown();
 						return SUCCESS;
 					case Parser.EXECUTE:
 						computerControl.execute(tokens[3].getValue());
 						return SUCCESS;
 					case Parser.SURF:
 						computerControl.browse("http://" + cmd.substring( cmd.indexOf("surf") + 5));
 						return SUCCESS;
 					case Parser.KEY:
 						computerControl.sendKey(tokens[3].getValue(), Integer.parseInt(tokens[4].getValue()));
 						return SUCCESS;
 					}
 				} else if (device == Parser.COMPUTER && numTokens > 2 && tokens[2].getType() == Parser.SERVICE) {
 					String service = tokens[2].getValue();
 					switch(parser.find(service,Parser.services)) {
 					case Parser.VOLUME:
 						if (numTokens == 3) {
 							return "" + computerControl.getVolume();
 						} else  if (numTokens == 4 && tokens[3].getType() == Parser.NUMBER) {
 							computerControl.setVolume(Integer.parseInt(tokens[3].getValue()));
 							return SUCCESS;
 						} else {
 							error("Invalid volume command");
 							return ERROR;
 						}
 					default:
 						error("Computers do not supported this service");
 						return ERROR;
 					}
 				} else if (device == Parser.SPEECH && numTokens > 2 && tokens[2].getType() == Parser.SPEECH_ACTION) {
 					if (n >= Config.MAX_SPEECH || speechControl[n] == null) {
 						error("Invalid speech device number: " + (n+1));
 						return ERROR;
 					}
 					switch(parser.find(tokens[2].getValue(), Parser.speechActions)) {
 					case Parser.SPEAK:
 						if (tokens.length > 3) {
 							error("Too many parameters");
 							return ERROR;
 						}
 						if (speechControl[n] != null) speechControl[n].setSpeech(n+1, true);
 						return SUCCESS;
 					case Parser.SILENT:
 						if (tokens.length > 3) {
 							error("Too many parameters");
 							return ERROR;
 						}
 						if (speechControl[n] != null) speechControl[n].setSpeech(n+1, false);
 						return SUCCESS;
 					case Parser.SAY:
 						say(n+1, cmd.substring(cmd.indexOf("say") + 4));
 						return SUCCESS;
 					default:
 						error("Invalid speech command");
 						return ERROR;
 					}
 				} else if (device == Parser.ROBOT && numTokens > 2 && tokens[2].getType() == Parser.ROBOT_ACTION) {
 					switch(parser.find(tokens[2].getValue(),Parser.robotActions)) {
 					case Parser.CLEAN:
 						say("I'm sorry, Elliot, I'm afraid I can't do that");
 						return ERROR;
 					case Parser.FETCH:
 						say("I'm sorry, Lawrie, I'm afraid I can't do that");
 						return ERROR;	
 					default:
 						error("Invalid robot action");
 						return ERROR;
 					}
 				} else if (numTokens > 2 && tokens[2].getType() == Parser.QUANTITY) {
 					switch(parser.find(tokens[2].getValue(),Parser.quantities)) {
 					case Parser.TEMPERATURE:
 						return "" + sensorControl[n].getTemperature(n+1);
 					case Parser.HUMIDITY:
 					case Parser.SOIL_MOISTURE:
 						return "" + sensorControl[n].getHumidity(n+1);
 					case Parser.LIGHT_LEVEL:
 						return "" + sensorControl[n].getLightLevel(n+1);
 					case Parser.BATTERY:
 						return "" + !sensorControl[n].getBatteryLow(n+1);
 					case Parser.POWER:
 						return "" + powerControl.getPower();
 					case Parser.ENERGY:
 						return "" + powerControl.getEnergy();
 					case Parser.OCCUPIED:
 						return switchString(sensorControl[n].getMotion(n+1));
 					default:
 						error("Unsupported quantity");
 						return ERROR;
 					}
 				}
 				error("Invalid device number command");
 				return ERROR;
 			default:
 				error("Invalid device command");
 				return ERROR;
 			}
 		case Parser.ACTION:
 			// Its a command without a device
 			switch (parser.find(tokens[0].getValue(), Parser.actions)) {
 			case Parser.MOOD:
 				debug("Setting mood to " + tokens[1].getValue());
 				break;
 			case Parser.EMAIL:
 				sendEmail("Home automation email",
 						source.substring(Parser.actions[Parser.EMAIL].length() + 1));
 				return SUCCESS;
 			case Parser.SIGNAL:
 				if (tokens[1].getValue().equals("highpower")) {
 					say("You are using " + powerControl.getPower() + " watts of electricity");
 					return SUCCESS;
 				}
 				error("Unrecognised signal");
 				return ERROR;
 			default:
 				error("Invalid command");
 				return ERROR;
 			}
 			break;
 		case Parser.AREA:
 			// Its an area query
 			boolean floor = parser.find(tokens[0].getValue(), Parser.areas) == Parser.FLOOR;
 			switch (tokens[1].getType()) {
 			case Parser.NUMBER:
 				debug("Area command with number");
 				int n = Integer.parseInt(tokens[1].getValue());
 
 				if (floor) {
 					if (n < 1 || n > Config.MAX_FLOORS) {
 						error("Floor number out of range");
 						return ERROR;
 					}
 				} else {
 					if (n < 1 || n > Config.MAX_ROOMS) {
 						error("Room number out of range");
 						return ERROR;
 					}
 				}
 				
 				if (numTokens == 2) {
 					// Just floor or room with no parameters - return name
 					if (floor) {
 						return config.floorNames[n-1];
 					} else {
 						return config.roomNames[n-1];
 					}
 				} else if (tokens[2].getType() == Parser.ACTION) {
 					switch (parser.find(tokens[2].getValue(), Parser.actions)) {
 					case Parser.ON:
 					case Parser.OFF:
 						error("Cannot switch an area on or off");
 						return ERROR;
 					case Parser.STATUS:
 					case Parser.VALUE:
 						error("Areas do not have a status or value");
 						return ERROR;
 					default:
 						error("Unsupported area action");
 						return ERROR;
 					}
 				} else if (tokens[2].getType() == Parser.QUANTITY) {
 					switch (parser.find(tokens[2].getValue(), Parser.quantities)) {
 					case Parser.TEMPERATURE:
 						int t = 0;
 						int i = 0;
 						for(int s: config.roomSensors[n-1]) {
 							print("Adding " + sensorControl[s].getTemperature(s) + " for room " + n + " sensor " + s);
 							t += sensorControl[s].getTemperature(s);
 							i++;			
 						}
 						return "" + (i == 0 ? 0 : (t/i));
 					case Parser.HUMIDITY:
 						int h = 0;
 						i = 0;
 						for(int s: config.roomSensors[n-1]) {
 							print("Adding " + sensorControl[s].getHumidity(s) + " for room " + n + " sensor " + s);
 							h += sensorControl[s].getHumidity(s);
 							i++;			
 						}
 						return "" + (i == 0 ? 0 : (h/i));
 					case Parser.LIGHT_LEVEL:
 						int l = 0;
 						i = 0;
 						for(int s: config.roomSensors[n-1]) {
 							print("Adding " + sensorControl[s].getLightLevel(s) + " for room " + n + " sensor " + s);
 							l += sensorControl[s].getLightLevel(s);
 							i++;			
 						}
 						return "" + (i == 0 ? 0 : (l/i));
 					case Parser.OCCUPIED:
 						boolean occ = false;
 						i = 0;
 						for(int s: config.roomSensors[n-1]) {
 							print("Testing " + sensorControl[s].getMotion(s) + " for sensor " + s);
 							if (sensorControl[s].getMotion(s)) occ = true;
 							i++;			
 						}
 						return switchString(occ);
 					default:
 						error("Area quantity command not supported");
 						return ERROR;
 					}
 				} else if (tokens[2].getType() == Parser.DEVICE_SET) {
 					int dSet = parser.find(tokens[2].getValue(), Parser.deviceSets);
 					if (numTokens > 3 && tokens[3].getType() == Parser.ACTION) {
 						int act = parser.find(tokens[3].getValue(), Parser.actions);
 						switch (act) {
 						case Parser.ON:
 						case Parser.OFF:
 							switch (dSet) {
 							case Parser.LIGHTS:
 								switchLights(n,(act == Parser.ON));
 								return SUCCESS;
 							case Parser.SOCKETS:
 								switchSockets(n,(act == Parser.ON));
 								return SUCCESS;
 							default:
 								error("Device set cannot be switched on or off");
 								return ERROR;
 							}
 						case Parser.SET:
 							switch (dSet) {
 							case Parser.LIGHTS:
 								if (numTokens > 4 && tokens[4].getType() == Parser.NUMBER) {
 									dimLights(n,Integer.parseInt(tokens[4].getValue()));
 									return SUCCESS;
 								} else {
 									error("Invalid device-set set command");
 									return ERROR;
 								}
 							default:
 								error("Device set cannot be set");
 								return ERROR;
 							}
 						default:
 							error("Invalid device-set command");
 							return ERROR;
 						}
 					}
 				}
 				error("Invalid area command");
 				return ERROR;
 			default:
 				error(tokens[0].getValue() + " must be followed by a number");
 				return ERROR;
 			}
 		}
 		return SUCCESS;
 	}
 	
 	// Execute an HTTP or housish command
 	private void execute(String cmd) throws IOException {
 		if (cmd.length() > 4 && cmd.substring(0,3).equals("GET") ) writeString(httpControl.httpCommand(cmd));
 		else writeString(parse(cmd));
 	}
 	
 	// Convert boolean to on/off string
 	private String switchString(boolean on) {
 		return (on ? "on" : "off");
 	}
 	
 	// Switch all the lights in a room on or off
 	private void switchLights(int room, boolean on) throws IOException {
 		print("Switching lights for room " + room);
 		for(int light: config.roomLights[room-1]) {
 			lightControl[light-1].switchLight(light-1,on);
 			delay(1000);
 		}
 	}
 	
 	// Switch all the sockets i a room on or off
 	private void switchSockets(int room, boolean on) throws IOException {
 		for(int socket: config.roomSockets[room-1]) {
 			socketControl[socket-1].switchSocket(socket-1,on);
 		}
 	}
 	
 	// Dim all the lights in a room to the same level
 	private void dimLights(int room, int level) throws IOException {
 		for(int light: config.roomLights[room-1]) {
 			lightControl[light-1].dimLight(light-1,level);
 		}
 	}
 	
 	// Expand the token array with new empty tokens
 	private void expandTokens(int n) {
 		debug("Expanding tokens by " + n);
 		int l = tokens.length;
 		Token[] oldTokens = tokens;
 		tokens = new Token[l+n];
 		for(int i=0;i<l;i++) tokens[i] = oldTokens[i];
 		for(int i=l;i<l+n;i++) tokens[i] = new Token("",-1,-1);
 		debug("Expansion done");
 	}
 
 	@Override
 	public Config getConfig() {
 		return config;
 	}
 
 	@Override
 	public Reporter getReporter() {
 		return this;
 	}
 
 	@Override
 	public Alerter getAlerter() {
 		return this;
 	}
 
 	@Override
 	public LightControl getLightControl(int n) {
 		return lightControl[n];
 	}
 
 	@Override
 	public SocketControl getSocketControl(int n) {
 		return socketControl[n];
 	}
 
 	@Override
 	public CameraControl getCameraControl(int n) {
 		return cameraControl[n];
 	}
 
 	@Override
 	public DatalogControl getDatalogControl() {
 		return datalogControl;
 	}
 
 	@Override
 	public InfraredControl getInfraredControl() {
 		return infraredControl;
 	}
 	
 	private void delay(int millis) {
 		try {
 			Thread.sleep(millis);
 		} catch (InterruptedException e) {
 			// ignore
 		}
 	}
 	
 	public void executeStep(String cmd, int delay) throws IOException {
 		execute(cmd);
 		if (delay > 0) delay(delay);
 	}
 	
 	public void executeMacro(Step[] macro) throws IOException {
 		for(Step s: macro) {
 			executeStep(s.getCmd(), s.getDelay());
 		}
 	}
 
 	@Override
 	public void say(String msg) {
 		say(1,msg);		
 	}
 
 	@Override
 	public void browse(String url) throws IOException {
 		java.awt.Desktop.getDesktop().browse(java.net.URI.create(url));		
 	}
 	
 	public Browser getBrowser() {
 		return this;
 	}
 }
