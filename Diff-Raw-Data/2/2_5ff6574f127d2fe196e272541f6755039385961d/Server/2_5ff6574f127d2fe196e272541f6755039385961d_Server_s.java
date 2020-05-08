 import java.awt.event.*;
 import java.awt.geom.*;
 import java.io.*;
 import java.net.*;
 import java.util.*;
 import javax.swing.*;
 
 /** class implementing a non-GUI server application to coordinate factory clients over a network */
 public class Server implements ActionListener, Networked {
 	/** networking port that server listens on */
 	public static final int PORT = 44247;
 	/** interval between timer ticks in milliseconds */
 	public static final int UPDATE_RATE = 200;
 	/** file path of factory settings file */
 	public static final String SETTINGS_PATH = "save/factory.dat";
 
 	private enum WantsEnum {
 		PART_TYPES, KIT_TYPES, STATUS, STATE
 	}
 
 	private class ClientWants {
 		public boolean partTypes;
 		public boolean kitTypes;
 		public boolean status;
 		/** whether each client wants to be updated with the factory state */
 		public boolean state;
 
 		public ClientWants() {
 			partTypes = false;
 			kitTypes = false;
 			status = false;
 			state = false;
 		}
 	}
 
 	/** server socket used to set up connections with clients */
 	private ServerSocket serverSocket;
 	/** ArrayList of client connections */
 	private ArrayList<NetComm> netComms;
 	/** whether each client wants to be updated with various things */
 	private ArrayList<ClientWants> wants;
 	/** Part types that are available to produce */
 	private ArrayList<Part> partTypes;
 	/** Kit types that are available to produce */
 	private ArrayList<Kit> kitTypes;
 	/** current production status */
 	private ProduceStatusMsg status;
 	/** current factory state */
 	private FactoryStateMsg state;
 	/** factory state changes to broadcast to clients on next timer tick */
 	private FactoryUpdateMsg update;
 
 	/** constructor for server class */
 	public Server() throws IOException {
 		// initialize server socket
 		try {
 			serverSocket = new ServerSocket(PORT);
 		}
 		catch (IOException ex) {
 			throw ex;
 		}
 		// load factory settings from file (or set up new factory if can't load from file)
 		loadSettings();
 		// start update timer
 		new javax.swing.Timer(UPDATE_RATE, this).start();
 		System.out.println("Server is ready; press ctrl+C to exit");
 		// wait for clients to connect
 		while (true) { // loop exits when user presses ctrl+C
 			try {
 				Socket socket = serverSocket.accept();
 				netComms.add(new NetComm(socket, this));
 				wants.add(new ClientWants());
 				System.out.println("Client " + (netComms.size() - 1) + " has joined");
 			}
 			catch (Exception ex) {
 				System.out.println("Error accepting new client connection");
 				ex.printStackTrace();
 			}
 		}
 	}
 
 	public static void main(String[] args) {
 		Server server;
 		try {
 			server = new Server();
 		}
 		catch (Exception ex) {
 			System.out.println("Error initializing server:");
 			ex.printStackTrace();
 			return;
 		}
 	}
 
 	/** called during timer tick; updates simulation and broadcasts factoryUpdate to clients */
 	public void actionPerformed(ActionEvent ae) {
 		if (ae.getSource() instanceof javax.swing.Timer) {
 			update.timeElapsed = System.currentTimeMillis() - state.timeStart;
 			for (Map.Entry<Integer, GUIItem> e : state.items.entrySet()) {
 				int key = e.getKey();
 				if (e.getValue() instanceof GUIKitCamera) {
 					// remove expired kit cameras
 					GUIKitCamera kitCamera = (GUIKitCamera)e.getValue();
					if (kitCamera.isExpired(update.timeElapsed)) state.removeItems.add(key);
 				}
 				else if (e.getValue() instanceof GUIKitRobot) {
 					// move around kit robot randomly
 					GUIKitRobot kitRobot = (GUIKitRobot)e.getValue();
 					if (kitRobot.arrived(update.timeElapsed)) {
 						Point2D.Double target = new Point2D.Double(kitRobot.getBasePos().x + Math.random() * 200 - 100,
 						                                           kitRobot.getBasePos().y + Math.random() * 200 - 100);
 						update.itemMoves.put(key, kitRobot.movement.moveToAtSpeed(update.timeElapsed, target, 0, 100));
 					}
 				}
 			}
 			broadcast(WantsEnum.STATE);
 			state.update(update);
 			update = new FactoryUpdateMsg();
 		}
 	}
 
 	/** handle message received from clients */
 	public void msgReceived(Object msgObj, NetComm sender) {
 		int senderIndex;
 		// find who sent the message
 		for (senderIndex = 0; senderIndex < netComms.size(); senderIndex++) {
 			if (sender == netComms.get(senderIndex)) break;
 		}
 		if (senderIndex == netComms.size()) {
 			System.out.println("Warning: received message from unknown client: " + msgObj);
 			return;
 		}
 		// handle message
 		if (msgObj instanceof CloseConnectionMsg) {
 			// close connection with client
 			// (but don't call clients.get(i).close() because client might still receive the message and get confused)
 			System.out.println("Client " + senderIndex + " has left");
 			netComms.remove(senderIndex);
 			wants.remove(senderIndex);
 		}
 		else if (msgObj instanceof String) {
 			// broadcast message to all clients (for TestClient only, TODO: delete later)
 			for (int i = 0; i < netComms.size(); i++) {
 				netComms.get(i).write("Message from " + senderIndex + " to " + i + ": " + (String)msgObj);
 			}
 		}
 		else if (msgObj instanceof NewPartMsg) {
 			// add a new part type
 			if (addPart(senderIndex, (NewPartMsg)msgObj, true)) {
 				System.out.println("Client " + senderIndex + " added a part");
 			}
 			else {
 				System.out.println("Client " + senderIndex + " unsuccessfully tried to add a part");
 			}
 		}
 		else if (msgObj instanceof ChangePartMsg) {
 			// change an existing part type
 			if (changePart(senderIndex, (ChangePartMsg)msgObj)) {
 				System.out.println("Client " + senderIndex + " changed a part");
 			}
 			else {
 				System.out.println("Client " + senderIndex + " unsuccessfully tried to change a part");
 			}
 		}
 		else if (msgObj instanceof DeletePartMsg) {
 			// delete an existing part type
 			if (deletePart(senderIndex, (DeletePartMsg)msgObj, true) != null) {
 				System.out.println("Client " + senderIndex + " deleted a part");
 			}
 			else {
 				System.out.println("Client " + senderIndex + " unsuccessfully tried to delete a part");
 			}
 		}
 		else if (msgObj instanceof PartListMsg) {
 			// send available part types to client
 			netComms.get(senderIndex).write(new PartListMsg(partTypes));
 			wants.get(senderIndex).partTypes = true;
 			System.out.println("Sent part list to client " + senderIndex);
 		}
 		else if (msgObj instanceof NewKitMsg) {
 			// add a new kit type
 			if (addKit(senderIndex, (NewKitMsg)msgObj, true)) {
 				System.out.println("Client " + senderIndex + " added a kit");
 			}
 			else {
 				System.out.println("Client " + senderIndex + " unsuccessfully tried to add a kit");
 			}
 		}
 		else if (msgObj instanceof ChangeKitMsg) {
 			// change an existing kit type
 			if (changeKit(senderIndex, (ChangeKitMsg)msgObj)) {
 				System.out.println("Client " + senderIndex + " changed a kit");
 			}
 			else {
 				System.out.println("Client " + senderIndex + " unsuccessfully tried to change a kit");
 			}
 		}
 		else if (msgObj instanceof DeleteKitMsg) {
 			// delete an existing kit type
 			if (deleteKit(senderIndex, (DeleteKitMsg)msgObj, true) != null) {
 				System.out.println("Client " + senderIndex + " deleted a kit");
 			}
 			else {
 				System.out.println("Client " + senderIndex + " unsuccessfully tried to delete a kit");
 			}
 		}
 		else if (msgObj instanceof KitListMsg) {
 			// send available kit types to client
 			netComms.get(senderIndex).write(new KitListMsg(kitTypes));
 			wants.get(senderIndex).kitTypes = true;
 			System.out.println("Sent kit list to client " + senderIndex);
 		}
 		else if (msgObj instanceof ProduceKitsMsg) {
 			// add kit production command to queue
 			if (produceKits(senderIndex, (ProduceKitsMsg)msgObj)) {
 				System.out.println("Client " + senderIndex + " added a production request");
 			}
 			else {
 				System.out.println("Client " + senderIndex + " unsuccessfully tried to add a production request");
 			}
 		}
 		else if (msgObj instanceof ProduceStatusMsg) {
 			// send production status to client
 			netComms.get(senderIndex).write(status);
 			wants.get(senderIndex).status = true;
 			System.out.println("Sent production status to client " + senderIndex);
 		}
 		else if (msgObj instanceof FactoryStateMsg) {
 			// this client wants to be updated with factory state
 			netComms.get(senderIndex).write(state);
 			wants.get(senderIndex).state = true;
 			System.out.println("Sent factory state to client " + senderIndex);
 		}
 		else {
 			System.out.println("Warning: received unknown message from client " + senderIndex + ": " + msgObj);
 		}
 	}
 
 	/** adds part to partTypes (if valid), if notify is true sends StringMsg to client indicating success or failure */
 	private boolean addPart(int clientIndex, NewPartMsg msg, boolean notify) {
 		String valid = newPartIsValid(msg.part);
 		if (notify) {
 			netComms.get(clientIndex).write(new StringMsg(StringMsg.MsgType.NEW_PART, valid));
 		}
 		if (!valid.isEmpty()) return false;
 		partTypes.add(msg.part);
 		if (notify) broadcast(WantsEnum.PART_TYPES);
 		return true;
 	}
 
 	/** changes specified part (if valid and not in production), sends StringMsg to client indicating success or failure */
 	private boolean changePart(int clientIndex, ChangePartMsg msg) {
 		// delete old part
 		Part oldPart = deletePart(clientIndex, new DeletePartMsg(msg.oldNumber), false);
 		if (oldPart == null) {
 			netComms.get(clientIndex).write(new StringMsg(StringMsg.MsgType.CHANGE_PART, "Requested part either in production or does not exist"));
 		}
 		// add replacement part
 		else if (!addPart(clientIndex, new NewPartMsg(msg.part), false)) {
 			netComms.get(clientIndex).write(new StringMsg(StringMsg.MsgType.CHANGE_PART, newPartIsValid(msg.part)));
 			partTypes.add(oldPart);
 		}
 		else {
 			netComms.get(clientIndex).write(new StringMsg(StringMsg.MsgType.CHANGE_PART, ""));
 			broadcast(WantsEnum.PART_TYPES);
 		}
 		return false;
 	}
 
 	/** deletes part with specified name (if exists), if notify is true sends StringMsg to client indicating success or failure,
 	    returns deleted part if succeeded or null if failed */
 	private Part deletePart(int clientIndex, DeletePartMsg msg, boolean notify) {
 		int i, j;
 		// TODO: don't delete part types in production
 		/*for (i = 0; i < status.cmds.size(); i++) {
 			if (status.status.get(i) == ProduceStatusMsg.KitStatus.QUEUED
 			    || status.status.get(i) == ProduceStatusMsg.KitStatus.PRODUCTION) {
 				Kit kit = getKitByNumber(status.cmds.get(i).kitNumber);
 				for (j = 0; j < kit.partsNeeded.size(); j++) {
 					if (msg.number == kit.partsNeeded.get(j).getNumber()) {
 						if (notify) netComms.get(clientIndex).write(new StringMsg(StringMsg.MsgType.DELETE_PART, "May not delete part that is in production"));	
 						return null;
 					}
 				}
 			}
 		}*/
 		// delete part with specified number
 		for (i = 0; i < partTypes.size(); i++) {
 			if (msg.number == partTypes.get(i).getNumber()) {
 				Part ret = partTypes.remove(i);
 				if (notify) {
 					netComms.get(clientIndex).write(new StringMsg(StringMsg.MsgType.DELETE_PART, ""));
 					broadcast(WantsEnum.PART_TYPES);
 				}
 				return ret;
 			}
 		}
 		if (notify) netComms.get(clientIndex).write(new StringMsg(StringMsg.MsgType.DELETE_PART, "Part never existed or has already been deleted"));
 		return null;
 	}
 
 	/** adds kit to kitTypes (if valid), if notify is true sends StringMsg to client indicating success or failure */
 	private boolean addKit(int clientIndex, NewKitMsg msg, boolean notify) {
 		String valid = newKitIsValid(msg.kit);
 		if (notify) {
 			netComms.get(clientIndex).write(new StringMsg(StringMsg.MsgType.NEW_KIT, valid));
 		}
 		if (!valid.isEmpty()) return false;
 		kitTypes.add(msg.kit);
 		if (notify) broadcast(WantsEnum.KIT_TYPES);
 		return true;
 	}
 
 	/** changes specified kit (if valid and not in production), sends StringMsg to client indicating success or failure */
 	private boolean changeKit(int clientIndex, ChangeKitMsg msg) {
 		// delete old kit
 		Kit oldKit = deleteKit(clientIndex, new DeleteKitMsg(msg.oldNumber), false);
 		if (oldKit == null) {
 			netComms.get(clientIndex).write(new StringMsg(StringMsg.MsgType.CHANGE_KIT, "Requested kit either in production or does not exist"));
 		}
 		// add replacement kit
 		else if (!addKit(clientIndex, new NewKitMsg(msg.kit), false)) {
 			netComms.get(clientIndex).write(new StringMsg(StringMsg.MsgType.CHANGE_KIT, newKitIsValid(msg.kit)));
 			kitTypes.add(oldKit);
 		}
 		else {
 			netComms.get(clientIndex).write(new StringMsg(StringMsg.MsgType.CHANGE_KIT, ""));
 			broadcast(WantsEnum.KIT_TYPES);
 		}
 		return false;
 	}
 
 	/** deletes kit with specified name (if exists), if notify is true sends StringMsg to client indicating success or failure,
 	    returns deleted kit if succeeded or null if failed */
 	private Kit deleteKit(int clientIndex, DeleteKitMsg msg, boolean notify) {
 		int i, j;
 		// TODO: don't delete kit types in production
 		/*for (i = 0; i < status.cmds.size(); i++) {
 			if (status.status.get(i) == ProduceStatusMsg.KitStatus.QUEUED
 			    || status.status.get(i) == ProduceStatusMsg.KitStatus.PRODUCTION) {
 				Kit kit = getKitByNumber(status.cmds.get(i).kitNumber);
 				for (j = 0; j < kit.partsNeeded.size(); j++) {
 					if (msg.number == kit.partsNeeded.get(j).getNumber()) {
 						if (notify) netComms.get(clientIndex).write(new StringMsg(StringMsg.MsgType.DELETE_PART, "May not delete part that is in production"));	
 						return null;
 					}
 				}
 			}
 		}*/
 		// delete kit with specified number
 		for (i = 0; i < kitTypes.size(); i++) {
 			if (msg.number == kitTypes.get(i).getNumber()) {
 				Kit ret = kitTypes.remove(i);
 				if (notify) {
 					netComms.get(clientIndex).write(new StringMsg(StringMsg.MsgType.DELETE_KIT, ""));
 					broadcast(WantsEnum.KIT_TYPES);
 				}
 				return ret;
 			}
 		}
 		if (notify) netComms.get(clientIndex).write(new StringMsg(StringMsg.MsgType.DELETE_KIT, "Kit never existed or has already been deleted"));
 		return null;
 	}
 
 	/** returns empty string if given part is valid, or error message if it is not */
 	private String newPartIsValid(Part part) {
 		if (part.getNumber() <= 0) return "Part number must be a positive integer";
 		if (!isValidName(part.getName())) {
 			if (part.getName().isEmpty()) return "Please enter a part name";
 			return "Part name may only contain letters, numbers, or spaces";
 		}
 		for (int i = 0; i < partTypes.size(); i++) {
 			if (part.getNumber() == partTypes.get(i).getNumber()) {
 				return "Another part has the same number";
 			}
 			if (part.getName().equals(partTypes.get(i).getName())) {
 				return "Another part has the same name";
 			}
 		}
 		return "";
 	}
 
 	/** returns empty string if given kit is valid, or error message if it is not */
 	private String newKitIsValid(Kit kit) {
 		if (kit.getNumber() <= 0) return "Kit number must be a positive integer";
 		if (!isValidName(kit.getName())) {
 			if (kit.getName().isEmpty()) return "Please enter a kit name";
 			return "Kit name may only contain letters, numbers, or spaces";
 		}
 		for (int i = 0; i < kitTypes.size(); i++) {
 			if (kit.getNumber() == kitTypes.get(i).getNumber()) {
 				return "Another kit has the same number";
 			}
 			if (kit.getName().equals(kitTypes.get(i).getName())) {
 				return "Another kit has the same name";
 			}
 		}
 		// number of parts in kit validated on client side, so don't need to check it here
 		return "";
 	}
 
 	/** queue specified production command in production status (if valid), sends StringMsg to client indicating success or failure */
 	private boolean produceKits(int clientIndex, ProduceKitsMsg msg) {
 		if (msg.howMany <= 0) {
 			netComms.get(clientIndex).write(new StringMsg(StringMsg.MsgType.PRODUCE_KITS, "Must produce at least 1 new kit"));
 			return false;
 		}
 		if (getKitByNumber(msg.kitNumber) == null) {
 			netComms.get(clientIndex).write(new StringMsg(StringMsg.MsgType.PRODUCE_KITS, "Kit number must refer to an existing kit"));
 			return false;
 		}
 		status.cmds.add(msg);
 		status.status.add(ProduceStatusMsg.KitStatus.QUEUED);
 		netComms.get(clientIndex).write(new StringMsg(StringMsg.MsgType.PRODUCE_KITS, ""));
 		broadcast(WantsEnum.STATUS);
 		return true;
 	}
 
 	private void broadcast(WantsEnum wantsEnum) {
 		for (int i = 0; i < wants.size(); i++) {
 			if (wantsEnum == WantsEnum.PART_TYPES && wants.get(i).partTypes) {
 				netComms.get(i).write(new PartListMsg(partTypes));
 			}
 			else if (wantsEnum == WantsEnum.KIT_TYPES && wants.get(i).kitTypes) {
 				netComms.get(i).write(new KitListMsg(kitTypes));
 			}
 			else if (wantsEnum == WantsEnum.STATUS && wants.get(i).status) {
 				netComms.get(i).write(status);
 			}
 			else if (wantsEnum == WantsEnum.STATE && wants.get(i).state) {
 				netComms.get(i).write(update);
 			}
 		}
 		// broadcasting generally coincides with updating something important, so save settings file
 		if (wantsEnum != WantsEnum.STATE) saveSettings();
 	}
 
 	/** returns part type with specified part number, or null if there is no such part */
 	private Part getPartByNumber(int number) {
 		for (int i = 0; i < partTypes.size(); i++) {
 			if (partTypes.get(i).getNumber() == number) return partTypes.get(i);
 		}
 		return null;
 	}
 
 	/** returns kit type with specified kit number, or null if there is no such kit */
 	private Kit getKitByNumber(int number) {
 		for (int i = 0; i < kitTypes.size(); i++) {
 			if (kitTypes.get(i).getNumber() == number) return kitTypes.get(i);
 		}
 		return null;
 	}
 
 	/** returns whether specified part/kit name is valid
 	    (i.e. is not empty and is composed only of letters, numbers, or spaces);
 	    copied from Andrew's HW3 submission */
 	public static boolean isValidName(String name) {
 		if (name.isEmpty()) {
 			return false;
 		}
 		for (char ch : name.toCharArray()) {
 			if (!Character.isLetterOrDigit(ch) && !Character.isWhitespace(ch)) {
 				return false;
 			}
 		}
 		return true;
 	}
 
 	/** initialize new/default factory */
 	private void initFactory() {
 		// instantiate lists
 		netComms = new ArrayList<NetComm>();
 		wants = new ArrayList<ClientWants>();
 		partTypes = new ArrayList<Part>();
 		kitTypes = new ArrayList<Kit>();
 		status = new ProduceStatusMsg();
 		state = new FactoryStateMsg();
 		update = new FactoryUpdateMsg();
 		// initialize factory state (copied from FactoryPainterTest.java)
 		int laneSeparation = 120;
 		for (int i = 0; i < 4; i++)
 		{
 			state.add(new GUINest(new Nest(), 550, 120 + laneSeparation*i));
 			state.add(new GUINest(new Nest(), 550, 120 + laneSeparation*i + 50));
 			
 			GUILane guiLane = new GUILane(new ComboLane(), true, 6, 630, 124 + laneSeparation*i);
 			guiLane.lane.turnOff();
 			
 			state.add(guiLane);
 			state.add(new GUIDiverterArm(990, 170 + laneSeparation*i));
 			state.add(new GUIFeeder(new Feeder(), 1165, 170 + laneSeparation*i));
 		}
 
 		state.add(new GUIKitStand(new KitStand()));
 
 		GUIKitDeliveryStation guiKitDeliv = new GUIKitDeliveryStation(new KitDeliveryStation(), 
 		 		   new GUILane(new ComboLane(), false, 8, 350,-10), 
 		 		   new GUILane(new ComboLane(), false, 3, 350-180, -10), 10, 10);
 		guiKitDeliv.inConveyor.lane.turnOff();
 		guiKitDeliv.outConveyor.lane.turnOff();
 
 		state.add(guiKitDeliv);
 								 
 		state.add(new GUIKitRobot(new KitRobot(), new Point2D.Double(350, 250)));
 		state.add(new GUIPartRobot(new PartRobot()));
 		GUIGantry guiGantry = new GUIGantry(100, 100);
 		guiGantry.movement = guiGantry.movement.moveToAtSpeed(0, new Point2D.Double(500,500), 0, 50);
 		guiGantry.addBin(new GUIBin(new GUIPart(new Part(), 0, 0), new Bin(new Part(), 10), 0, 0));
 		state.add(guiGantry);
 	}
 
 	/** load factory settings from file */
 	private void loadSettings() {
 		initFactory();
 		try {
 			ObjectInputStream inStream = new ObjectInputStream(new FileInputStream(SETTINGS_PATH));
 			Object inObj;
 			while (inStream.readBoolean()) {
 				inObj = inStream.readObject();
 				if (inObj instanceof Part) {
 					partTypes.add((Part)inObj);
 				}
 				else if (inObj instanceof Kit) {
 					kitTypes.add((Kit)inObj);
 				}
 				else if (inObj instanceof ProduceStatusMsg) {
 					status = (ProduceStatusMsg)inObj;
 				}
 				else if (inObj instanceof FactoryStateMsg) {
 					state = (FactoryStateMsg)inObj;
 					state.timeStart = System.currentTimeMillis() - state.timeElapsed;
 				}
 			}
 			inStream.close();
 		}
 		catch (FileNotFoundException ex) {
 			System.out.println("Settings file not found; a new factory has been set up.");
 		}
 		catch (Exception ex) {
 			initFactory();
 			System.out.println("Error loading settings from file; a new factory has been set up.");
 		}
 	}
 
 	/** save factory settings to file */
 	private void saveSettings() {
 		int i;
 		try {
 			ObjectOutputStream outStream = new ObjectOutputStream(new FileOutputStream(SETTINGS_PATH));
 			for (i = 0; i < partTypes.size(); i++) {
 				outStream.writeBoolean(true);
 				outStream.writeObject(partTypes.get(i));
 			}
 			for (i = 0; i < kitTypes.size(); i++) {
 				outStream.writeBoolean(true);
 				outStream.writeObject(kitTypes.get(i));
 			}
 			outStream.writeBoolean(true);
 			outStream.writeObject(status);
 			outStream.writeBoolean(true);
 			outStream.writeObject(state);
 			outStream.writeBoolean(false);
 			outStream.close();
 		}
 		catch (Exception ex) {
 			System.out.println("Error saving factory settings to file.");
 			System.out.println("Make sure the \"save\" folder exists.");
 		}
 	}
 }
