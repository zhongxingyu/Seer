 package control;
 
 import java.util.HashMap;
 import java.util.logging.Level;
 
 import org.json.simple.JSONArray;
 import org.json.simple.JSONObject;
 import org.json.simple.JSONValue;
 
 import remoteInterface.FromRemoteInterface;
 import remoteInterface.RemoteData;
 import remoteInterface.ToRemoteInterface;
 
 import webSock.ISISServerApplication;
 import webSock.ISISWebSocket;
 import webSock.WebSocketIncomingQueueInterface;
 import webSock.WebSocketOutgoingQueueInterface;
 import xbee.XBeeHandler;
 import xbee.XBeePacket;
 import control.Thread1;
 
 public class Controller {
 
 	/**
 	 * Handlers (XBee network and WebSocket network)
 	 */
 
 	private XBeeHandler handler;
 	private ISISServerApplication isisServerApp;
 
 	/**
 	 * Run in helper-function test mode (true) or not (false)
 	 */
 	private final boolean isHelperFunctionTest;
 
 
 	/**
 	 * Controller Lists
 	 */
 
 	// List to store the states of all XBee devices
 	protected HashMap<String, RemoteData> remoteStateList; // Key, Value => String(ControllerID),XbeeState
 
 	// List to store the WebSocket clients that are currently connected to the ISIS server
 	protected HashMap<String, ISISWebSocket> webSocketClientList; // Key, Value => String(WebSocketClientID),ISISWebSocket
 
 	// List to store logical address / physical address pairs
 	protected HashMap<Integer, String> physicalIDMap;
 	protected HashMap<String, Integer> logicalIDMap;
 
 
 	/**
 	 * WebSocket client request strings and codes
 	 */
 
 	// Keys that should appear in JSON requests (in order)
 	public static final String jsonCommandFieldStr = "CommandCode";
 	public static final String jsonRequestArgumentFieldStr = "Argument";
 	//-------------------------------------------------------------------------
 	// Possible values for request types / codes
 	public static final String ioCodeStr = "IO_CODE";
 	public static final String diagCodeStr = "DIAG_CODE";
 	public static final String initCodeStr = "INIT_CODE";
 
 
 	/**
 	 * WebSocket client response strings and codes
 	 */
 
 	// Keys that should appear in JSON responses (in order)
 	public static final String jsonResponseFieldStr = "ResponseCode";
 	public static final String jsonResponseArgumentFieldStr = "Object";
 	//-------------------------------------------------------------------------
 	// Possible values for response types / codes
 	public static final String buttonEventStr = "BUTTON_EVENT";
 	public static final String ioResponseStr = "IO_RESPONSE";
 	public static final String diagResponseStr = "DIAG_RESPONSE";
 	public static final String initResponseStr = "INIT_RESPONSE";
 	public static final String errorResponseStr = "ERROR_RESPONSE";
 	//-------------------------------------------------------------------------
 	// Field names for Initialize Objects
 	public static final String initFieldStr = "Initialization";
 	public static final String initControllerParam = "All";
 
 	// Field names for IO Objects
 	public static final String ioControllerAddressStr = "ControllerAddress";
 	public static final String ioDataArrayStr = "DataArray";
 
 	// Field names for Diagnostic Objects
 	public static final String diagControllerAddressStr = "ControllerAddress";
 	public static final String diagDataArrayStr = "DataArray";
 	public static final String diagIsOnStr = "ControllerOn";
 	public static final String diagPinVoltageArrayStr = "PinVoltages";
 	//public static final String diagPowerRemainingStr = "PowerRemaining";
 	//public static final String diagWirelessStrength = "WirelessStrength";
 	//public static final String diagErrorRate = "ErrorRate";
 
 	// Field names for Button Event Objects
 	public static final String buttonEventControllerAddressStr = "ControllerAddress";
 	public static final String buttonEventDataArrayStr = "DataArray";
 
 	// Field names for Error Objects
 	public static final String errorMessageStr = "ErrorMessage";
 
 
 
 
 
 	/**
 	 * Controller Threads
 	 */
 
 	private Thread remoteToWeb;
 	private Thread webToRemote;
 	private Thread timer;
 
 
 	/**
 	 * Constructor
 	 * @param handler
 	 * @param serverApp
 	 */
 	public Controller(XBeeHandler handler, ISISServerApplication serverApp, boolean isHelperTest)
 	{
 		/*
 		 * Set the handlers for XBee and WebSocket
 		 */
 		this.handler = handler;
 		this.isisServerApp = serverApp;
 		java.util.logging.Logger.getAnonymousLogger().log(
 				Level.INFO, "Time: " + new java.util.Date() + ", Initialized the XBee and WebSocket Handlers");
 
 		this.isHelperFunctionTest = isHelperTest;
 		if(this.isHelperFunctionTest) {
 			java.util.logging.Logger.getAnonymousLogger().log(
 					Level.INFO, "Time: " + new java.util.Date() + ", Controller run in Helper Test Mode");
 		}
 
 		/*
 		 * Create the Controller lists
 		 */
 		this.remoteStateList = new HashMap<String, RemoteData>();
 		this.webSocketClientList = new HashMap<String, ISISWebSocket>(); // TODO: WHERE DO WE EVER USE THIS??
 		this.physicalIDMap = new HashMap<Integer, String>();
 		this.logicalIDMap = new HashMap<String, Integer>();
 		java.util.logging.Logger.getAnonymousLogger().log(
 				Level.INFO, "Time: " + new java.util.Date() + ", Created XBee state list, WebSocket client list, " +
 				"and Controller address mapping lists");
 
 		/*
 		 * Fill the controller address map with the Logical / Physical address mappings
 		 */
 		// Thread 1 will fill the controller map as it receives packets from XBees
 		// Could we use XBee handler to get all physical addresses then assign logical addresses
 		//this.controllerAddressMap.put(key, value);
 		//System.out.println("Time: " + new java.util.Date() + ", Filled the logical->physical address map");
 
 		if(!this.isHelperFunctionTest) {
 			/* 
 			 * Create the Controller threads
 			 */
 			this.remoteToWeb = new Thread(new Thread1((FromRemoteInterface)handler,
 					(WebSocketOutgoingQueueInterface) isisServerApp.getOutgoingMsgQueue(),
 					this));
 
 			this.webToRemote = new Thread(new Thread2((ToRemoteInterface) handler, 
 					(WebSocketIncomingQueueInterface) isisServerApp.getIncomingMsgQueue(),
 					(WebSocketOutgoingQueueInterface) isisServerApp.getOutgoingMsgQueue(),
 					this));
 
 			this.timer = new Thread (new Timer(this.handler));
 
 			java.util.logging.Logger.getAnonymousLogger().log(
 					Level.INFO, "Time: " + new java.util.Date() + ", Created Controller Threads");
 
 			/*
 			 * Run the Controller threads
 			 */
 			this.remoteToWeb.start();
 			this.webToRemote.start(); // TODO: COMMENT BACK IN LATER TO RUN THREAD2
 			this.timer.start();
 			java.util.logging.Logger.getAnonymousLogger().log(
 					Level.INFO, "Time: " + new java.util.Date() + ", Started Controller Threads");
 		}
 	}
 
 
 	/**
 	 * Controller Functions
 	 */
 
 	/**
 	 *  Convert XBee Packet to JSON
 	 *  This function will also call the physical to logical method and use the 
 	 *  logical address for the client.
 	 *  
 	 * @param pkt
 	 * @param destination if this is null it is a broadcast to all connected clients
 	 * @return
 	 */
 	@SuppressWarnings("unchecked")
 	JSONObject convertPacketToJSON(RemoteData pkt, String responseTypeToCreate) { //ISISWebSocket destination,
 		// JSONObject to return
 		JSONObject obj = new JSONObject();
 		// Parameter object
 		JSONObject paramObj = new JSONObject();
 
 		// What type of response is required?
 		if(responseTypeToCreate.equals(Controller.buttonEventStr)) {
 			paramObj.put(Controller.buttonEventControllerAddressStr, this.physicalToLogical(pkt.getControllerID()));
 			JSONArray jsonArray = new JSONArray();
 			for(int i = 0; i < pkt.getButtonIOStates().length; i++) {
 				jsonArray.add(i, pkt.getButtonIOStates()[i]);
 			}
 			paramObj.put(Controller.buttonEventDataArrayStr, jsonArray);
 
 			// Fill in the fields of the JSONObject to return
 			obj.put(Controller.jsonResponseFieldStr, Controller.buttonEventStr);
 			obj.put(Controller.jsonResponseArgumentFieldStr, paramObj);
 		}
 		else if(responseTypeToCreate.equals(Controller.ioResponseStr)) {
 			paramObj.put(Controller.ioControllerAddressStr, this.physicalToLogical(pkt.getControllerID()));
 			JSONArray jsonArray = new JSONArray();
 			for(int i = 0; i < pkt.getButtonIOStates().length; i++) {
 				jsonArray.add(i, pkt.getButtonIOStates()[i]);
 			}
 			paramObj.put(Controller.ioDataArrayStr, jsonArray);
 
 			// Fill in the fields of the JSONObject to return
 			obj.put(Controller.jsonResponseFieldStr, Controller.ioResponseStr);
 			obj.put(Controller.jsonResponseArgumentFieldStr, paramObj);
 		}
 		else if(responseTypeToCreate.equals(Controller.diagResponseStr)) {
 			// TODO COULD ADD MORE LATER
 			paramObj.put(Controller.diagControllerAddressStr, this.physicalToLogical(pkt.getControllerID()));
 			JSONArray jsonArray1 = new JSONArray();
 			for(int i = 0; i < pkt.getButtonIOStates().length; i++) {
 				jsonArray1.add(i, pkt.getButtonIOStates()[i]);
 			}
 			paramObj.put(Controller.diagDataArrayStr, jsonArray1);
 			paramObj.put(Controller.diagIsOnStr, pkt.isOn());
 			JSONArray jsonArray2 = new JSONArray();
 			for(int i = 0; i < pkt.getButtonPinVoltages().length; i++) {
 				jsonArray2.add(i, pkt.getButtonPinVoltages()[i]);
 			}
 			paramObj.put(Controller.diagPinVoltageArrayStr, jsonArray2);
 
 			// Fill in the fields of the JSONObject to return
 			obj.put(Controller.jsonResponseFieldStr, Controller.diagResponseStr);
 			obj.put(Controller.jsonResponseArgumentFieldStr, paramObj);
 		}
 		else if(responseTypeToCreate.equals(Controller.initResponseStr)) {
 			paramObj.put(Controller.initFieldStr, "");	// TODO CHANGE "" TO ALL
 
 			// Fill in the fields of the JSONObject to return
 			obj.put(Controller.jsonResponseFieldStr, Controller.initResponseStr);
 			obj.put(Controller.jsonResponseArgumentFieldStr, paramObj);
 		}
 		else if(responseTypeToCreate.equals(Controller.errorResponseStr)) {
 			paramObj.put(Controller.errorMessageStr, ""); // TODO RemoteData: ERROR MSG SET AND GET CAPABILITY??
 
 			// Fill in the fields of the JSONObject to return
 			obj.put(Controller.jsonResponseFieldStr, Controller.errorResponseStr);
 			obj.put(Controller.jsonResponseArgumentFieldStr, paramObj);
 		}
 		else {
 			obj = null;
 		}
 
 		return obj;
 	}
 
 	/**
 	 * This method will convert the JSON string into a command class. 
 	 * Will be calling the logical to physical to get the physical address from 
 	 * the logical one given from the browser client.
 	 * 
 	 * @param jsonObj
 	 * @return
 	 */
 	RemoteData convertJSONToPacket(JSONObject jsonObj) {
 		if(jsonObj.get(Controller.jsonCommandFieldStr) == null) {
			// Create log message
			java.util.logging.Logger.getAnonymousLogger().log(
					Level.INFO, "Time: " + new java.util.Date() + ", convertJSONToPacket(): No command code detected " +
							"in JSON object so null packet is returned");
 			return null;
 		}
 
 		// Create new packet
 		RemoteData pkt = null;
 		int logicalControllerId;
 		String controllerIdToSet = "";
 
 		// Figure out what kind of client request (json) it is
 		if(jsonObj.get(Controller.jsonCommandFieldStr).equals(Controller.ioCodeStr)) {
 			try {
 				pkt = new XBeePacket();
				//Exception long can not be cast to int??
				logicalControllerId = (Integer) jsonObj.get(Controller.jsonRequestArgumentFieldStr);
 				controllerIdToSet = logicalToPhysicalAddress(logicalControllerId);
 				pkt.setControllerID(controllerIdToSet);
 			} catch (Exception e) {
 				e.printStackTrace();
 			} 
 		}
 		else if(jsonObj.get(Controller.jsonCommandFieldStr).equals(Controller.diagCodeStr)) {
 			try {
 				pkt = new XBeePacket();
 				logicalControllerId = (int) jsonObj.get(Controller.jsonRequestArgumentFieldStr);
 				controllerIdToSet = logicalToPhysicalAddress(logicalControllerId);
 				pkt.setControllerID(controllerIdToSet);
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 		}
 		else if(jsonObj.get(Controller.jsonCommandFieldStr).equals(Controller.initCodeStr)) {
 			pkt = new XBeePacket();
 			pkt.setControllerID(Controller.initControllerParam);
 		}
 		else {
 			pkt = null;
 		}
 
 		return pkt;
 	}


 	/**
 	 * 
 	 * @param requestType
 	 * @return The response type expected for the given request (null if no mapping was found)
 	 */
 	String getAppropriateResponseType(String requestType) {
 		// TODO: ADD TEST FOR THIS
 		if(requestType.equals(Controller.ioCodeStr)) {
 			return Controller.ioResponseStr;
 		}
 		else if(requestType.equals(Controller.diagCodeStr)) {
 			return Controller.diagResponseStr;
 		}
 		else if(requestType.equals(Controller.initCodeStr)) {
 			return Controller.initResponseStr;
 		}
 		else {
 			return Controller.errorResponseStr;
 		}
 	}
 
 
 	// Accessor functions for XBee States list
 
 	/**
 	 * @param id
 	 * @return The state of the XBee controller with the given physical ID
 	 */
 	RemoteData getStateForController(String id) {
 		return this.remoteStateList.get(id);
 	}
 
 	// Mutator functions for XBee States list
 
 	/**
 	 * Add an XBee state to the list of XBee states. If the Remote does not exist yet
 	 * it will be created and added to the hash map. But if it is made already. The
 	 * Existing state's values will be updated.
 	 * @param state
 	 */
 	synchronized void addState(RemoteData state) {
 		this.remoteStateList.put(state.getControllerID(), state);
 	}
 
 	/**
 	 * Remove the XBee state for the controller with the given physical id from the list of XBee states
 	 * @param id
 	 */
 	synchronized void removeStateForController(String id) {
 		this.remoteStateList.remove(id);
 	}
 
 
 	// Translator functions for XBee devices
 
 	/**
 	 * This function will add a physical address to the maps if it is not already in
 	 * them Its logical address will the be 0 for the first one 1 for the second one and so on.
 	 * If the physical address is already in the map it will return the logical address.
 	 * 
 	 * This function is the function that inits the maps.
 	 */
 	synchronized int physicalToLogical(String physical)
 	{
 		// The physical is already in the system return the logical.
 		if(this.physicalIDMap.containsValue(physical) && this.logicalIDMap.containsKey(physical))
 		{
 			return this.logicalIDMap.get(physical);
 		}
 		// This is an init case if the physical address is not yet associated to a logical ID.
 		else
 		{
 			int newLogicalId = physicalIDMap.size();
 			this.physicalIDMap.put(newLogicalId, physical);
 			this.logicalIDMap.put(physical, newLogicalId);
 			return newLogicalId;
 		}
 	}
 
 	/**
 	 * Translate a logical controller address id to a physical XBee device address
 	 * 
 	 * This function does NOT ADD TO THE MAPS.
 	 * 
 	 * @return the corresponding physical address for a logical controller address
 	 * @throws Exception 
 	 */
 	synchronized String logicalToPhysicalAddress(int logicalAddr) throws Exception 
 	{
 		// Make sure that both maps know about the logical address.
 		if(this.physicalIDMap.containsKey(logicalAddr) && 
 				this.logicalIDMap.containsValue(logicalAddr))
 		{
 			return this.physicalIDMap.get(logicalAddr);
 		}
 		// If one of them doesn't there is a problem.
 		else
 		{
 			throw new Exception("The logical address is not in the map system...");
 		}
 	}
 
 
 	// Accessor functions for WebSocket Client list
 
 	/**
 	 * @param clientId
 	 * @return The WebSocket object for the client with the given ID
 	 */
 	synchronized ISISWebSocket getWebSocketForClient(String clientId) {
 		return this.webSocketClientList.get(clientId);
 	}
 
 	// Mutator functions for WebSocket Client list
 
 	/**
 	 * Add a new WebSocket client to the list of WebSocket clients
 	 * @param client
 	 */
 	synchronized void addWebSocketClient(ISISWebSocket client) {
 		this.webSocketClientList.put(client.toString(), client);
 	}
 	/**
 	 * Remove the WebSocket client with the given client id from the list of WebSocket clients
 	 * @param clientId
 	 */
 	synchronized void removeWebSocketClient(String clientId) {
 		this.webSocketClientList.remove(clientId);
 	}
 
 	/**
 	 * Regression test for controller
 	 * @param args
 	 */
 	@SuppressWarnings("unchecked")
 	public static void main(String[] args) { // TODO: MAIN FUNCTION
 		// Create a new Controller to test helper functions (no need to pass in legit handlers)
 		Controller ctrl = new Controller(null, null, true);
 		System.out.println("\n\n");
 
 		// Add test physical to logical address mappings to the Controller (new physical address causes new logical
 		// address to be assigned)
 		// Logical addresses now go from 0 - 2 as a result of this insertion
 		ctrl.physicalToLogical("A1234567890");
 		ctrl.physicalToLogical("B1234567890");
 		ctrl.physicalToLogical("C1234567890");
 
 		// Test the physical to logical address conversion (to check that the physical addresses were created)
 		if(ctrl.physicalToLogical("A1234567890") != 0) {
 			System.out.println("Error in physicalToAddress(): Incorrect Logical address returned");
 		}
 		if(ctrl.physicalToLogical("B1234567890") != 1) {
 			System.out.println("Error in physicalToAddress(): Incorrect Logical address returned");
 		}
 		if(ctrl.physicalToLogical("C1234567890") != 2) {
 			System.out.println("Error in physicalToLogicalAddress(): Incorrect Logical address returned");
 		}
 
 		// Test the logical to physical address conversion (valid logical addresses)
 		try {
 			if(!ctrl.logicalToPhysicalAddress(0).equals("A1234567890")) {
 				System.out.println("Error in logicalToPhysicalAddress(): Incorrect Physical address returned");
 			}
 		} 
 		catch (Exception e) {
 			e.printStackTrace();
 		}
 		try {
 			if(!ctrl.logicalToPhysicalAddress(1).equals("B1234567890")) {
 				System.out.println("Error in logicalToPhysicalAddress(): Incorrect Physical address returned");
 			}
 		} 
 		catch (Exception e) {
 			e.printStackTrace();
 		}
 		try {
 			if(!ctrl.logicalToPhysicalAddress(2).equals("C1234567890")) {
 				System.out.println("Error in logicalToPhysicalAddress(): Incorrect Physical address returned");
 			}
 		} 
 		catch (Exception e) {
 			e.printStackTrace();
 		}
 
 		// Test (invalid) logical address and catch expected exceptions
 		try {
 			ctrl.logicalToPhysicalAddress(3);
 			System.out.println("Error in logicalToPhysicalAdress(). Exception should have been thrown");
 		}
 		catch(Exception e) {
 			// Expect exception to be caught for this
 		}
 		try {
 			ctrl.logicalToPhysicalAddress(4);
 			System.out.println("Error in logicalToPhysicalAdress(). Exception should have been thrown");
 		}
 		catch(Exception e) {
 			// Expect exception to be caught for this
 		}
 
 		// Print out the test map (physical and logical so they can be compared to the printed maps' contents)
 		// And also print rint out the contents of both (actual) maps
 		System.out.println("Test physical ID map (Key,Value):\n{0=A1234567890; 1=B1234567890; 2=C1234567890}");
 		System.out.println("Contents of actual physical ID map:\n" + ctrl.physicalIDMap);
 		System.out.println("");
 		System.out.println("Test logical ID map (Key,Value):\n{A1234567890=0; B1234567890=1; C1234567890=2}");
 		System.out.println("Contents of actual logical ID map:\n" + ctrl.logicalIDMap);
 
 
 		System.out.println("\n\n");
 
 
 		// Create a Test RemoteData packet
 		RemoteData pkt = new XBeePacket();
 		pkt.setControllerID("0");
 		pkt.setOnState(true);
 		boolean[] pktIOStates = {true, false, false, false, false};
 		pkt.setButtonIOStates(pktIOStates);
 		double[] pktButtonPinVoltages = {3.3, 0.0, 0.0, 0.0, 0.0};
 		pkt.setButtonPinVoltages(pktButtonPinVoltages);
 
 		// Test the controller XBeePacket -> JSONObjectconversion function (test converting to various forms) 
 		System.out.println("\nTest XBeePacket: \n" + pkt.toString());
 		System.out.println("\nTest XBeePacket JSON-converted forms: ");
 		System.out.println(JSONValue.toJSONString(ctrl.convertPacketToJSON(pkt, Controller.buttonEventStr)));
 		System.out.println(JSONValue.toJSONString(ctrl.convertPacketToJSON(pkt, Controller.ioResponseStr)));
 		System.out.println(JSONValue.toJSONString(ctrl.convertPacketToJSON(pkt, Controller.diagResponseStr)));
 		System.out.println(JSONValue.toJSONString(ctrl.convertPacketToJSON(pkt, Controller.initResponseStr)));
 		System.out.println(JSONValue.toJSONString(ctrl.convertPacketToJSON(pkt, Controller.errorResponseStr)));
 
 
 		System.out.println("\n\n");
 
 
 		// Create test JSONObjects
 		JSONObject myJSONObj1 = new JSONObject();
 		JSONObject myJSONObj2 = new JSONObject();
 		JSONObject myJSONObj3 = new JSONObject();
 
 		// Fill in the fields of the test JSONObjects (test creating each type of request)
 		myJSONObj1.put(Controller.jsonCommandFieldStr, Controller.ioCodeStr);
 		myJSONObj1.put(Controller.jsonRequestArgumentFieldStr, 0);
 		//
 		myJSONObj2.put(Controller.jsonCommandFieldStr, Controller.diagCodeStr);
 		myJSONObj2.put(Controller.jsonRequestArgumentFieldStr, 0);
 		//
 		myJSONObj3.put(Controller.jsonCommandFieldStr, Controller.initCodeStr);
 		myJSONObj3.put(Controller.jsonRequestArgumentFieldStr, Controller.initControllerParam);
 
 		// Test the controller JSONObject -> XBeePacket conversion function
 		System.out.println("Test JSONObject1:" + JSONValue.toJSONString(myJSONObj1));
 		System.out.println("Test JSONObject1 XBeePacket-converted form: ");
 		System.out.println(ctrl.convertJSONToPacket(myJSONObj1) + "\n");
 
 		System.out.println("Test JSONObject2:" + JSONValue.toJSONString(myJSONObj2));
 		System.out.println("Test JSONObject2 XBeePacket-converted form: ");
 		System.out.println(ctrl.convertJSONToPacket(myJSONObj2) + "\n");
 
 		System.out.println("Test JSONObject3:" + JSONValue.toJSONString(myJSONObj3));
 		System.out.println("Test JSONObject3 XBeePacket-converted form: ");
 		System.out.println(ctrl.convertJSONToPacket(myJSONObj3) + "\n");
 
 
 		System.out.println("\n\nEnd of Controller Helper Test");
 	}
 }
