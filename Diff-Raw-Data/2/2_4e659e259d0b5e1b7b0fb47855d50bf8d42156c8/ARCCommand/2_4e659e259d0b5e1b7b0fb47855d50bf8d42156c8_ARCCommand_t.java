 /**
  * 
  */
 package com.i2r.ARC.PCControl;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 import java.util.Scanner;
 
 import org.apache.log4j.Logger;
 
 /**
  * ooh boy has this class been expanded.
  * 
  * Class abstracts some command for a job to do.  That can either be local or some job to send to a remote device
  * 
  * The ARCCommand class also keeps track of various constants to use for defaults, the constant indexes of various argument parameters
  * and default argument strings for various commands.
  * 
  * @author Johnathan Pagnutti
  *
  */
 public class ARCCommand {
 	static final Logger logger = Logger.getLogger(ARCCommand.class);
 		
 	public static final int TAKE_PICTURE_FREQUENCY_INDEX = 0;
 	public static final int TAKE_PICTURE_TIMEFRAME_INDEX = 1;
 	public static final int TAKE_PICTURE_AMMOUNT_INDEX = 2;
 	
 	public static final int TAKE_PICTURE_ARG_LIST_SIZE = 3;
 	
 	private static final String PICTURE_AMMOUNT_DEFAULT = "1";
 	private static final String NO_ARGUMENT = "-1";
 	
 	private static final String[] DEFAULT_NO_COMMAND_ARGUMENTS = {};
 	private static final String[] DEFAULT_RECORD_AUDIO_ARGUMENTS = {NO_ARGUMENT};
 	private static final String[] DEFAULT_KILL_COMMAND_ARGUMENTS = {NO_ARGUMENT};
 	private static final String[] DEFAULT_TAKE_PICTURE_ARGUMENTS = {NO_ARGUMENT, NO_ARGUMENT, PICTURE_AMMOUNT_DEFAULT};
 	private static final String[] DEFAULT_SUPPORTED_FEATURES = {NO_ARGUMENT};
 	private static final String[] DEFAULT_MODIFY_SENSOR_ARGUMENTS = {NO_ARGUMENT};
 	private static final String[] DEFAULT_LISTEN_ENVIRONMENT_ARGUMENTS = {NO_ARGUMENT};
 	private static final String[] DEFAULT_GET_LOCATION_ARGUMENTS = {NO_ARGUMENT};
 	
 	private static final String[] DEFAULT_LIST_DEVICES_ARGUMENTS = {};
 	private static final String[] DEFAULT_HELP_ARGUMENTS = {};
 	private static final String[] DEFAULT_LIST_DEVICE_SENSORS = {NO_ARGUMENT};
 	private static final String[] DEFAULT_PAUSE_ARGUMENTS = {};
 	private static final String[] DEFAULT_CONNECT_ARGUMENTS = {NO_ARGUMENT};
 	
 	private static final String[] DEFAULT_FREEZE_ARGUMENTS = {NO_ARGUMENT};
 	
 	public static final int KILL_TASK_INDEX = 0;
 	
 	//the header to a command.
 	private CommandHeader header;
 	
 	//the arguments to go with that command
 	private List<String> arguments;
 	
 	//the remote device this command is paired with
 	private RemoteClient dev;
 	
 	
 	
 	public ARCCommand(CommandHeader header) throws UnsupportedValueException{
 		switch(header){
 		case LIST_DEVICE_SENSORS:
 		case LIST_DEVICES:
 		case HELP:
 		case PAUSE:
 		case CONNECT:
 		case FREEZE:
 			this.header = header;
 			this.arguments = defaultArguments(header);
 			break;
 		default:
 			throw new UnsupportedValueException("Supplied Command header " + header.getAlias() + " was invalid.");
 		}
 	}
 	
 	public ARCCommand(CommandHeader header, List<String> arguments) throws UnsupportedValueException{
 		switch (header){
 		case LIST_DEVICES:
 		case LIST_DEVICE_SENSORS:
 		case HELP:
 		case PAUSE:
 		case CONNECT:
 		case FREEZE:
 			this.header = header;
 			this.arguments = checkArgumentsAgainstController(header, arguments);
 			logger.debug("ARCCommand has " + arguments.size() + " args");
 			break;
 		default:
 			throw new UnsupportedValueException("Supplied Command header " + header.getAlias() + " was invalid.");
 		}
 	}
 	
 	private List<String> checkArgumentsAgainstController(CommandHeader header,
 			List<String> arguments) throws UnsupportedValueException {
 		switch(header){
 		case LIST_DEVICES:
 			return checkListDeviceArguments(arguments);
 		case LIST_DEVICE_SENSORS:
 			return checkListSensorsArguments(arguments);
 		case HELP:
 			return checkHelpArguments(arguments);
 		case PAUSE:
 			return checkPauseArguments(arguments);
 		case CONNECT:
 			return checkConnectArguments(arguments);
 		case FREEZE:
 			return checkFreezeArguments(arguments);
 		default:
 			throw new UnsupportedValueException("Supplied Command header " + header.getAlias() + " was invalid.");
 		}
 	}
 
 	private List<String> checkFreezeArguments(List<String> arguments) throws UnsupportedValueException {
 		if(arguments.size() != 1){
 			throw new UnsupportedValueException("Incorrect number of arguments.");
 		}
 		
 		//TODO: add type checking(?)
 		return arguments;
 	}
 
 	private List<String> checkConnectArguments(List<String> arguments) throws UnsupportedValueException {
 		if(arguments.size() != 1){
 			throw new UnsupportedValueException("Incorrect number of arguments.");
 		}
 		
 		Controller cntrl = Controller.getInstance();
 		if(Integer.parseInt(arguments.get(0)) > cntrl.devices.size()){
 			throw new UnsupportedValueException("Remote Client does not exist.");
 		}
 		
 		return arguments;
 	}
 
 	private List<String> checkPauseArguments(List<String> arguments) throws UnsupportedValueException {
 		if(arguments.size() > 0){
 			int deviceRef = Integer.parseInt(arguments.get(0));
 			logger.debug("Device reference: " + deviceRef);
 			
 			if(deviceRef >= Controller.getInstance().devices.size() || deviceRef < 0){
 				throw new UnsupportedValueException("Device not found.");
 			}
 			
 			if(arguments.size() > 1){
 				RemoteClient dev = Controller.getInstance().devices.get(deviceRef);
 				if(dev.deviceTasks.hasTask(Integer.valueOf(arguments.get(1)))){
 					return arguments;
 				}else{
 					throw new UnsupportedValueException("Task not found.");
 				}
 			}else{
 				return arguments;
 			}
 		}else{
 			throw new UnsupportedValueException("Invalid number of arguments.");
 		}
 	}
 
 	private List<String> checkHelpArguments(List<String> arguments) throws UnsupportedValueException {
 		return defaultArguments(CommandHeader.HELP);
 	}
 
 	private List<String> checkListSensorsArguments(List<String> arguments) throws UnsupportedValueException {
 		if(arguments.size() < 1){
 			throw new UnsupportedValueException("Invalid number of parameters.");
 		}
 		
 		int deviceRef = Integer.parseInt(arguments.get(0));
 		
 		if(deviceRef >= Controller.getInstance().devices.size() || deviceRef < 0){
 			throw new UnsupportedValueException("Device not found.");
 		}
 		
 		return arguments;
 	}
 
 	private List<String> checkListDeviceArguments(List<String> arguments) throws UnsupportedValueException {
 		return defaultArguments(CommandHeader.LIST_DEVICES);
 	}
 
 	/**
 	 * Default Constuctor
 	 */
 	public ARCCommand(RemoteClient dev){
 		this.dev = dev;
 		//set the header to no command
 		this.header = CommandHeader.DO_NOTHING;
 		//set the arguments to the default values for the no command header
 		try {
 			arguments = defaultArguments(header);
 		} catch (UnsupportedValueException e) {
 			//this really never should happen.
 			logger.error("Congrats! you've ended up in a circle of hell!");
 		}
 	}
 	
 	/**
 	 * Default constructor for a particular header.  If the header given is invalid, then throw an exception
 	 * @param header the header to use to create a new default command.
 	 * @throws UnsupportedValueException 
 	 */
 	public ARCCommand(RemoteClient dev, CommandHeader header) throws UnsupportedValueException{
 		this.dev = dev;
 		//for the supplied header
 		switch(header){
 		case DO_NOTHING:
 		case KILL_TASK:
 		case TAKE_PICTURE:
 		case GET_SENSOR_FEATURES:
 		case MODIFY_SENSOR:
 		case RECORD_AUDIO:
 		case LISTEN_ENVIRONMENT:
 		case GET_LOCATION:
 			//if the header was the no command header, the kill header, or the take pictures header
 			//set the class header to the supplied header
 			this.header = header;
 			//set the arguments to the default arguments for that header
 			arguments = defaultArguments(header);
 			break;
 		default:
 			throw new UnsupportedValueException("Supplied Command header " + header.getAlias() + " was invalid.");
 		}
 	}
 	
 	/**
 	 * Get the default argument list for a supplied header
 	 * 
 	 * @param header the header to get the default argument list for
 	 * 
 	 * @return the default list, or null if the passed header was not defined
 	 * @throws UnsupportedValueException 
 	 */
 	private List<String> defaultArguments(CommandHeader header) throws UnsupportedValueException {
 		//for the value of the provided header...
 		switch(header){
 		//if the header was no command
 		case DO_NOTHING:
 			//return the default no command argument list
 			return Arrays.asList(DEFAULT_NO_COMMAND_ARGUMENTS);
 		//if the header was kill
 		case KILL_TASK:
 			//return the default kill argument list
 			return Arrays.asList(DEFAULT_KILL_COMMAND_ARGUMENTS);
 		//if the header was take pictures
 		case TAKE_PICTURE:
 			//return the default take pictures list
 			return Arrays.asList(DEFAULT_TAKE_PICTURE_ARGUMENTS);
 		case RECORD_AUDIO:
 			//return the default record audio command
 			return Arrays.asList(DEFAULT_RECORD_AUDIO_ARGUMENTS);
 		case GET_LOCATION:
 			return Arrays.asList(DEFAULT_GET_LOCATION_ARGUMENTS);
 		case GET_SENSOR_FEATURES:
 			return Arrays.asList(DEFAULT_SUPPORTED_FEATURES);
 		case MODIFY_SENSOR:
 			return Arrays.asList(DEFAULT_MODIFY_SENSOR_ARGUMENTS);
 		case LISTEN_ENVIRONMENT:
 			return Arrays.asList(DEFAULT_LISTEN_ENVIRONMENT_ARGUMENTS);
 		case LIST_DEVICES:
 			return Arrays.asList(DEFAULT_LIST_DEVICES_ARGUMENTS);
 		case LIST_DEVICE_SENSORS:
 			return Arrays.asList(DEFAULT_LIST_DEVICE_SENSORS);
 		case HELP:
 			return Arrays.asList(DEFAULT_HELP_ARGUMENTS);
 		case PAUSE:
 			return Arrays.asList(DEFAULT_PAUSE_ARGUMENTS);
 		case CONNECT:
 			return Arrays.asList(DEFAULT_CONNECT_ARGUMENTS);
 		case FREEZE:
 			return Arrays.asList(DEFAULT_FREEZE_ARGUMENTS);
 		default:
 			throw new UnsupportedValueException("Supplied command header " + header.getAlias() + " was invalid.");
 		}
 	}
 
 	/**
 	 * TODO: comment this
 	 * @param dev
 	 */
 	public void setRemoteDevice(RemoteClient dev){
 		this.dev = dev;
 	}
 	
 	/**
 	 * @return the header
 	 */
 	public CommandHeader getHeader() {
 		return header;
 	}
 
 	/**
 	 * @return the arguments
 	 */
 	public List<String> getArguments() {
 		return arguments;
 	}
 
 	/**
 	 * Versitile Constructor.  Allows to set the header, along with the arguments to that header
 	 * If the arguments supplied are invalid, defaults to the default argument list for the header given.
 	 * 
 	 * If the header passed is undefined, then defaults to the default ARCCommand.
 	 * 
 	 * @param header the header for the ARCCommand
 	 * @param arguments the list of arguments to use for a specified header
 	 * @throws UnsupportedValueException if an argument in arguments is invalid for the given header
 	 */
 	public ARCCommand(RemoteClient dev, CommandHeader header, List<String> arguments) throws UnsupportedValueException{
 		this.dev = dev;
 		
 		//for the defined headers...
 		switch(header){
 		case DO_NOTHING:
 		case KILL_TASK:
 		case TAKE_PICTURE:
 		case GET_SENSOR_FEATURES:
 		case MODIFY_SENSOR:
 		case RECORD_AUDIO:
 		case LISTEN_ENVIRONMENT:
 		case GET_LOCATION:
 			//set the header to the provided header
 			this.header = header;
 			this.arguments = checkAgainstDevice(header, arguments);
 			
 			break;
 		default:
 			throw new UnsupportedValueException("Supplied command header " + header.getAlias() + " was invalid.");
 		}
 	}
 	
 	/**
 	 * Checks a list of arguments, along with the header they go to, to see if they are valid.  If they are, they are returned.
 	 * If they are not, the default argument list for that particular header is returned.
 	 * 
 	 * If the header is undefined, then null is returned.
 	 * 
 	 * @param header the header to check the argument list again.
 	 * @param arguments the argument list to check
 	 * 
 	 * @return the provided argument list if it checked out, the default if the given arguments were bad or null if the given header
 	 * 			was undefined
 	 */
 	private List<String> checkAgainstDevice(CommandHeader header, List<String> arguments) throws UnsupportedValueException {
 		switch (header){
 		case DO_NOTHING:
 			return checkNoCommandArgs(arguments);
 		case KILL_TASK:
 			return checkKillCommandArgs(arguments);
 		case GET_SENSOR_FEATURES:
 			return getSupportedFeaturesCommandArgs(arguments);
 		case TAKE_PICTURE:
 			if(!dev.supportedSensors.containsKey(Sensor.CAMERA)){
 				throw new UnsupportedValueException(Sensor.CAMERA.getAlias() + " is unsupported.");
 			}
 			return checkTakePicturesCommandArgs(arguments);
 		case MODIFY_SENSOR:
 			return checkDeviceModifySensorParams(arguments);
 		case RECORD_AUDIO:
 			if(!dev.supportedSensors.containsKey(Sensor.MICROPHONE)){
 				throw new UnsupportedValueException(Sensor.MICROPHONE.getAlias() + " is unsupported.");
 			}
 			return checkRecordAudioArgs(arguments);
 		case LISTEN_ENVIRONMENT:
 			if(!dev.supportedSensors.containsKey(Sensor.ENVIRONMENT)){
 				throw new UnsupportedValueException(Sensor.ENVIRONMENT.getAlias() + " is unsupported.");
 			}
 			return checkListenEnvironmentArgs(arguments);
 		case GET_LOCATION:
 			if(!dev.supportedSensors.containsKey(Sensor.LOCATION)){
 				throw new UnsupportedValueException(Sensor.LOCATION.getAlias() + " is unsupported.");
 			}
 			return checkGetLocationArgs(arguments);
 		default:
 			throw new UnsupportedValueException("The supplied header " + header.getAlias() + " was invalid.");
 		}
 	}
 
 	
 	private List<String> checkGetLocationArgs(List<String> arguments) throws UnsupportedValueException {
 		if(arguments.size() < 1){
 			throw new UnsupportedValueException("Incorrect number of arguments.");
 		}else if(arguments.size() == 1){
 			return defaultArguments(CommandHeader.GET_LOCATION);
 		}else if(arguments.size() == 3){
 			for(String arg : arguments){
 				int value = Integer.parseInt(arg);
 				if(value < 1){
 					throw new UnsupportedValueException("Invalid argument");
 				}
 			}
 			return arguments;
 		}else{
 			throw new UnsupportedValueException("Incorrect number of arguments.");
 		}
 	}
 
 	private List<String> checkListenEnvironmentArgs(List<String> arguments) throws UnsupportedValueException {
 		if(arguments.size() < 1){
 			throw new UnsupportedValueException("Incorrect number of arguments.");
 		}
 		
 		return arguments;
 	}
 
 	private List<String> checkDeviceModifySensorParams(List<String> arguments) throws UnsupportedValueException {
 		logger.debug("Checking against device: " + dev.toString());
 		Sensor sensor = Sensor.get(arguments.get(0));
 		arguments.set(0, sensor.getType().toString());
 		
 		List<String> subArgs = arguments.subList(1, arguments.size());
 		
 		if(dev.supportedSensors.containsKey(sensor)){
 			switch(sensor){
 			case CAMERA:
 			case MICROPHONE:
 				int i = 0;
 				while(i < subArgs.size()){
 					String key = subArgs.get(i);
 					i++;
 					String value = subArgs.get(i);
 					i++;
 				
 					String[] safeVals = dev.checkSingleArg(sensor, key, value);
 					if(safeVals != null){
 						subArgs.set(i - 2, safeVals[0]);
 						subArgs.set(i - 1, safeVals[1]);
 					}
 				}
 				break;
 			case ENVIRONMENT:
 			case LOCATION:
 				int j = 0;
 				while(j < subArgs.size()){
 					String key = subArgs.get(j);
 					j++;
 					String value = subArgs.get(j);
 					j++;
 				
 					String[] safeVals = dev.checkSingleArg(sensor, key, value);
 					if(safeVals != null){
 						subArgs.set(j - 2, safeVals[0]);
 						subArgs.set(j - 1, safeVals[1]);
 					}
 					
 					//remove the _, which we use to delimit sensors from the spaces in a user command
					subArgs.set(j - 2, safeVals[0].replace("_", " "));
 				}
 				break;
 			default:
 				throw new UnsupportedValueException("Sensor " + sensor.getAlias() + " is not supported.");
 			}
 		}else{
 			throw new UnsupportedValueException("Sensor " + sensor.getAlias() + " is not supported for this device.");
 		}
 		
 		return arguments;
 	}
 
 	private List<String> getSupportedFeaturesCommandArgs(List<String> arguments) throws UnsupportedValueException {
 		
 		if(arguments.size() < 0){
 			throw new UnsupportedValueException("Invalid number of arguments.");
 		}
 		
 		Sensor currentSensor = null;
 		
 		int i = 0;
 		do{
 			try{
 				currentSensor = Sensor.get(arguments.get(i));
 				arguments.set(i, currentSensor.getType().toString());
 				i++;
 			}catch(UnsupportedValueException e){
 				if(currentSensor != null){
 					if(dev.supportedSensors.get(currentSensor).featureDataTypes.containsKey(arguments.get(i))){
 						i++;
 						continue;
 					}else{
 						throw new UnsupportedValueException("Sensor " + currentSensor.getAlias() + " does not support that feature.");
 					}
 				}else{
 					logger.debug("Attempting to get new sensor data.");
 				}
 			}
 			
 		}while(i < arguments.size());
 			
 		return arguments;
 	}
 
 	/**
 	 * Checks the argument list when the header supplied is to take pictures.
 	 * 
 	 * If any element of the list falls outside specified bounds or is otherwise invalid, it is set to the default.
 	 * 
 	 * @param arguments the list of arguments to check
 	 * @return a valid list of arguments, that may or may not have defaults.
 	 * @throws UnsupportedValueException 
 	 */
 	private List<String> checkTakePicturesCommandArgs(List<String> arguments) throws UnsupportedValueException {
 		//counting variable
 		int i;
 		//value holder
 		int num;
 		
 		//make sure the list is of the right size
 		if(arguments.size() != TAKE_PICTURE_ARG_LIST_SIZE){
 			throw new UnsupportedValueException("Incorrect number of arguments.");
 		}
 		
 		//for each arg in the argument list...
 		for(i = 0; i < arguments.size(); i++){
 			//get the value of the argument
 			String value = arguments.get(i);
 			
 			//for the argument in position i...
 			switch(i){
 			//if i is the take picture frequency index
 			case TAKE_PICTURE_FREQUENCY_INDEX:
 				//parse the string value as an integer
 				num = Integer.parseInt(value);
 				//if that integer is shorter than the minimum value set
 				if(num < 0 && num != -1){
 					throw new UnsupportedValueException("Supplied picture frequency is not valid.");
 				}
 				break;
 			//if i is the time to take pictures in index
 			case TAKE_PICTURE_TIMEFRAME_INDEX:
 				//parse the string value as an integer
 				num = Integer.parseInt(value);
 				//if that integer is smaller than the minimum value for time
 				if(num < 0 && num != -1){
 					throw new UnsupportedValueException("Supplied picture duration is invalid");
 				}
 				break;
 			//if i is the amount of pictures to take
 			case TAKE_PICTURE_AMMOUNT_INDEX:
 				//parse the string value as an integer
 				num = Integer.parseInt(value);
 				//if the integer value is smaller than the minimum allowed number of pictures to take
 				if(num < 0 && num != -1){
 					//set it to the minimum value
 					throw new UnsupportedValueException("Supplied picture ammount is invalid");
 				}
 				break;
 			default:
 				throw new UnsupportedValueException("The argument at position " + i + " is out of bounds.");
 			}
 		}
 		
 		//now, either the picture time frame or the picture amount must be set to -1.
 		int takePictureTimeNum =  Integer.parseInt(arguments.get(TAKE_PICTURE_TIMEFRAME_INDEX));
 		int takePictureAmountNum = Integer.parseInt(arguments.get(TAKE_PICTURE_AMMOUNT_INDEX));
 		
 		//if they're both not -1
 		if(takePictureTimeNum != -1 && takePictureAmountNum != -1){
 			throw new UnsupportedValueException("Both the take picture duration and the take picture amount are set.");
 		}
 		
 		return arguments;
 	}
 	
 	private List<String> checkRecordAudioArgs(List<String> arguments) throws UnsupportedValueException{
 		if(arguments.size() > 0){
 			dev.checkSingleArg(Sensor.MICROPHONE, "audio-recording-duration", arguments.get(0));
 			return arguments;
 		}else{
 			return defaultArguments(CommandHeader.RECORD_AUDIO);
 		}
 	}
 
 	/**
 	 * Checks and sets the arguments provided for the kill command
 	 * 
 	 * The kill command only has one set of valid arguments- the defauts.  So, set them.
 	 * @param arguments the arguments to check for the kill command
 	 * @return the correct list of arguments for the kill command
 	 * @throws UnsupportedValueException 
 	 */
 	private List<String> checkKillCommandArgs(List<String> arguments) throws UnsupportedValueException {
 		
 		//kill just has one argument
 		if(arguments.isEmpty()){
 			throw new UnsupportedValueException("Invalid number of arguments for the kill command.");
 		}else{
 			int taskId = Integer.parseInt(arguments.get(KILL_TASK_INDEX));
 			if(taskId < 0){
 				throw new UnsupportedValueException("Task ID must be greater than 0.");
 				
 			}else if(dev.deviceTasks.getTask(taskId) == null){
 				throw new UnsupportedValueException("Task ID not found in " + dev + " task stack");
 			}else{
 				return arguments;
 			}
 		}
 	}
 
 	/**
 	 * Checks and sets the arguments provided by the no command command
 	 * 
 	 * The no command command has only one set of valid arguments, the defaults.  So, just set them rather than checking.
 	 * 
 	 * @param arguments the arguments that we want to check for the no command 
 	 * @return the correct list of arguments for the no command
 	 * @throws UnsupportedValueException 
 	 */
 	private List<String> checkNoCommandArgs(List<String> arguments) throws UnsupportedValueException {
 		return defaultArguments(CommandHeader.DO_NOTHING);
 	}
 
 	/**
 	 * Return a new ARC command given a string
 	 * @param line the string to create a new ARCCommand out of
 	 * @return
 	 * @throws UnsupportedValueException if the line is invalid
 	 */
 	public static ARCCommand fromString(RemoteClient device, String line) throws UnsupportedValueException {
 		
 		logger.debug("Line: " + line);
 		Scanner lineScan = new Scanner(line);
 		CommandHeader header;
 		
 		if(lineScan.hasNext()){
 			header = CommandHeader.get(lineScan.next());
 			logger.debug("header: " + header.getAlias());
 		}else{
 			lineScan.close();
 			throw new UnsupportedValueException("Could not parse header from supplied line.");
 		}
 		
 		if(lineScan.hasNext()){
 			List<String> lineArgs = new ArrayList<String>();
 			while(lineScan.hasNext()){
 				lineArgs.add(lineScan.next());
 			}
 			
 			lineScan.close();
 			return new ARCCommand(device, header, lineArgs);
 		}else{
 			lineScan.close();
 			return new ARCCommand(device, header);
 		}
 	}
 
 	public static ARCCommand fromString(String line) throws UnsupportedValueException {
 		logger.debug("Line: " + line);
 		
 		Scanner lineScan = new Scanner(line);
 		CommandHeader header;
 		
 		if(lineScan.hasNext()){
 			header = CommandHeader.get(lineScan.next());
 			logger.debug("header: " + header.getAlias());
 		}else{
 			lineScan.close();
 			throw new UnsupportedValueException("Could not parse header from supplied line.");
 		}
 		
 		if(lineScan.hasNext()){
 			List<String> lineArgs = new ArrayList<String>();
 			while(lineScan.hasNext()){
 				String arg = lineScan.next();
 				logger.debug("arg: " + arg);
 				lineArgs.add(arg);
 			}
 			
 			lineScan.close();
 			return new ARCCommand(header, lineArgs);
 		}else{
 			lineScan.close();
 			return new ARCCommand(header);
 		}
 	}
 }
