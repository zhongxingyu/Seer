 package machine.turing;
 import gui.MachineEditor;
 import gui.turing.TuringMachineEditor;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.transform.OutputKeys;
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerConfigurationException;
 import javax.xml.transform.TransformerException;
 import javax.xml.transform.TransformerFactory;
 import javax.xml.transform.dom.DOMSource;
 import javax.xml.transform.stream.StreamResult;
 
 import machine.Machine;
 import machine.Simulation;
 
 import org.w3c.dom.Attr;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 import tape.ConsoleTape;
 import tape.LEGOTape;
 import tape.GraphicTape;
 import tape.MasterRobot;
 import tape.SlaveRobot;
 import tape.Tape;
 import tape.TapeException;
 
 /** This class represents a Turing machine with it's states, edges and transitions.
  * 
  * @author David Wille, Nils Breyer
  * 
  */
 public class TuringMachine extends Machine{
 	public static final int XML_VERSION = 3;
 	public static final String FILE_EXTENSION = ".tm";
 	/**
 	 * Contains all states of this machine
 	 */
 	protected ArrayList<State> states;
 	/**
 	 * Contains all edges of this machine
 	 */
 	protected ArrayList<Edge> edges;
 
 	/**
 	 * Contains all textboxes of this machine
 	 */
 	protected ArrayList<Textbox> textboxes;
 	
 	/**
 	 * Contains all frames of this machine
 	 */
 	protected ArrayList<Frame> frames;
 	
 	/**
 	 * Constructs an untitled Turing machine
 	 */
 	public TuringMachine() {
 		this("Untitled Turing machine");
 	}
 
 	/**
 	 * Constructs an empty Turing machine with a name
 	 * @param name Name for the Turing machine
 	 */
 	public TuringMachine(String name) {
 		this(name, new ArrayList<State>(), new ArrayList<Edge>(), new ArrayList<Tape>());
 	}
 	
 	/**
 	 * Constructs an empty Turing machine with a name and its tapes
 	 * @param name Name for the Turing machine
 	 * @param numberOfTapes Number of tapes
 	 */
 	public TuringMachine(String name, int numberOfTapes) {
 		this(name, new ArrayList<State>(), new ArrayList<Edge>(), new ArrayList<Tape>());
 		for(int i = 0; i < numberOfTapes; i++){
 			this.tapes.add(new ConsoleTape());
 		}
 	}
 
 	/**
 	 * Constructs a new Turing machine with edges, states and transitions
 	 * @param states All states of the Turing machine
 	 * @param edges All edges of the Turing machine
 	 * @param name The name of the Turing machine
 	 * @param tapes The number of tapes used in the Turing machine
 	 */
 	public TuringMachine(String name, ArrayList<State> states, ArrayList<Edge> edges, ArrayList<Tape> tapes) {
 		super(name);
 		this.states = states;
 		this.edges = edges;
 		this.tapes = tapes;
 		this.frames = new ArrayList<Frame>();
 		this.textboxes = new ArrayList<Textbox>();
 	}
 	
 	public String getFileExtension() {
 		return TuringMachine.FILE_EXTENSION;
 	}
 
 	/**
 	 * Returns the Turing machine's states
 	 * @return Turing Machine's states
 	 */
 	public ArrayList<State> getStates() {
 		return this.states;
 	}
 
 	/**
 	 * Returns the Turing machine's edges
 	 * @return Turing Machine's edges
 	 */
 	public ArrayList<Edge> getEdges() {
 		return this.edges;
 	}
 	
 	/**
 	 * Returns the Turing machine's Textboxes.
 	 * @return Turing Machine's Textboxes.
 	 */
 	public ArrayList<Textbox> getTextboxes() {
 		return this.textboxes;
 	}
 	
 	/**
 	 * Returns the Turing machine's Frames.
 	 * @return Turing Machine's Frames.
 	 */
 	public ArrayList<Frame> getFrames() {
 		return this.frames;
 	}
 	
 	/**
 	 * Reads a Turing machine from a XML file.
 	 * The data (states, edges...) of the current TuringMachine object will be overwritten with the data from the file. 
 	 * @param filename File to read the Turing machine from
 	 * @throws IOException If an I/O error occurs while reading the file, or the file content is corrupt.
 	 */
 	public void load(String filename) throws IOException {
 		if (!filename.endsWith(".tm")) {
 			throw new IOException("Wrong file extension of file '" + filename + "'. Must be '.tm'");
 		}
 		//clear data
 		this.edges.clear();
 		this.states.clear();
 		this.tapes.clear();
 		this.textboxes.clear();
 
 		//parse document
 		System.out.println("Loading file '" + filename + "'...");
 		File file = new File(filename);
 		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
 		Document doc = null;
 		try {
 			DocumentBuilder db = dbf.newDocumentBuilder();
 			doc = db.parse(file);
 		}
 		catch (Exception e) {
 			IOException ioex = new IOException("Parsing XML failed. Check the syntax of the file '" + filename + "'.");
 			ioex.initCause(e);
 			throw ioex;
 		}
 		doc.getDocumentElement().normalize();
 
 		// read Turing machine's name
 		String machineName = doc.getDocumentElement().getAttribute("name");
 		this.name = machineName;
 		String machineXMLVersion = doc.getDocumentElement().getAttribute("xml-version");
 
 		if (!machineXMLVersion.equals(String.valueOf(TuringMachine.XML_VERSION))) {
 			throw new IOException("The file format version of the file '" + filename 
 					+ "' is not supported by this program. Please convert the file before opening.");
 		}
 
 		//load the rest
 		System.out.println("Loading tape configuration...");
 		this.loadTapesConfig(doc);
 		System.out.println("Loading states...");
 		this.loadStates(doc);
 		System.out.println("Loading edges and transitions...");
 		this.loadEdges(doc);
 		System.out.println("Loading textboxes...");
 		this.loadTextboxes(doc);
 		System.out.println("Loading frames...");
 		this.loadTextboxes(doc);
 		System.out.println("File '" + filename + "' successfully loaded.");
 	}
 
 	/**
 	 * Loads the tapes' configuration from a xml Document 
 	 * @param doc Document to load the configuration from
 	 * @throws IOException Exception if any problems occur while reading the configuration
 	 */
 	private void loadTapesConfig(Document doc) throws IOException{
 		//Iterate through a list of tape nodes
 		NodeList tapeList = doc.getElementsByTagName("tape");
 		for (int i = 0; i < tapeList.getLength(); i++) {
 			Node tapeNode = tapeList.item(i);
 
 			if (tapeNode.getNodeType() != Node.ELEMENT_NODE) {
 				break; //ignore attributes etc.
 			}
 			Element tapeElement = (Element) tapeNode;
 
 			//Get tape type and name
 			String tapeType = tapeElement.getAttribute("type");
 			String tapeName = InOut.getTagValue("name", tapeElement);
 
 			//Get input word
 			String inputWord = "";
 
 			Element inputElement = InOut.getChildElement("input", tapeElement);
 
 			NodeList inputSymbolList = inputElement.getElementsByTagName("symbol");
 			for (int k = 0; k < inputSymbolList.getLength(); k++) {
 				Node inputSymbolNode = inputSymbolList.item(k);
 				if (inputSymbolNode.getNodeType() == Node.ELEMENT_NODE) {
 					String symbolString = inputSymbolNode.getTextContent();
 					if (inputSymbolNode.getTextContent().length() != 1) {
 						throw new IOException("Expected exactly one character per symbol in input word of tape '" 
 								+ tapeName + "' but found the string '" + symbolString + "' with a length of "
 								+ symbolString.length() + " instead.");
 					}
 					inputWord += inputSymbolNode.getTextContent().charAt(0);
 				}
 			}
 
 			//create the tape
 			Tape tape = null;
 			if (tapeType.equals("LEGO")) {
 				String masterName = InOut.getTagValue("master", tapeElement);
 				String masterMacAddress = InOut.getChildElement("master", tapeElement).getAttribute("mac-address");
 				String slaveName = InOut.getTagValue("slave", tapeElement);
 				String slaveMacAddress = InOut.getChildElement("slave", tapeElement).getAttribute("mac-address");
 
 				MasterRobot master = new MasterRobot(masterName, masterMacAddress);
 				SlaveRobot slave = new SlaveRobot(slaveName, slaveMacAddress);
 
 				tape = new LEGOTape(master, slave);
 			}
 			else if (tapeType.equals("console")) {
 				tape = new ConsoleTape();
 			}
 			else if (tapeType.equals("graphic")) {
 				tape = new GraphicTape();
 			}
 			else {
 				throw new IOException("Unsupported tape type '" + tapeType + "' for tape ' " + tapeName + "'.");
 			}
 
 			tape.setName(tapeName);
 			tape.setInputWord(inputWord);
 			this.tapes.add(tape);
 			System.out.println(tape); //TODO: remove debug output
 		} //end for (next tape)
 
 		//Check if tape count attribute matches the number of tapes
 		String machineTapesCountString = doc.getDocumentElement().getAttribute("tapes");
 		int machineTapesCount = Integer.parseInt(machineTapesCountString);
 
 		if (machineTapesCount != this.tapes.size()) {
 			throw new IOException("The tapes count attribute of the machine did not match the number of tapes actually defined (attribute is " 
 					+ machineTapesCountString + " but " + this.tapes.size() + " tapes were defined).");
 		}
 	}
 
 	/**
 	 * Loads the states from an xml Document
 	 * @param doc The document where to get the states
 	 * @throws IOException Exception if any problem occurs while reading the states
 	 */
 	private void loadStates(Document doc) throws IOException {
 		// get list of nodes
 		NodeList stateList = doc.getElementsByTagName("state");
 		for (int i = 0; i < stateList.getLength(); i++) {
 			Node currentNode = stateList.item(i);
 
 			if (currentNode.getNodeType() == Node.ELEMENT_NODE) {
 				Element currentElement = (Element) currentNode;
 				String id = currentElement.getAttribute("id");
 				if (this.getStateById(id) != null) {
 					throw new IOException("State ID '" + id + "' already exists! Please check your xml file!");
 				}
 				
 				//read start and final attribute
 				boolean startState = false;
 				String startString = currentElement.getAttribute("start");
 				if (startString.equals("yes")) {
 					startState = true;
 				}
 				else if (startString.equals("no")) {
 					startState = false;
 				}
 				else {
 					throw new IOException("Expected 'yes' or 'no' in attribute 'start' for State ID '" + id + "' but found '" + startString + "'.");
 				}
 				
 				boolean finalState = false;
 				String finalString = currentElement.getAttribute("final");
 				if (finalString.equals("yes")) {
 					finalState = true;
 				}
 				else if (finalString.equals("no")) {
 					finalState = false;
 				}
 				else {
 					throw new IOException("Expected 'yes' or 'no' in attribute 'start' for State ID '" + id + "' but found '" + startString + "'.");
 				}
 
 				//get position
 				int x = InOut.getAttributeValueInt("x", currentElement);
 				int y = InOut.getAttributeValueInt("y", currentElement);
 				
 				//get size
 				int width = InOut.getAttributeValueInt("width", currentElement);
 				int height = InOut.getAttributeValueInt("height", currentElement);
 				
 				String name = InOut.getTagValue("name", currentElement);
 				State state = new State(id, name, startState, finalState);
 				state.setXcoord(x);
 				state.setYcoord(y);
 				state.setWidth(width);
 				state.setHeight(height);
 				
 				states.add(state);
 				System.out.println(" " + state); //TODO: remove debug output
 			}
 		} //end for (next state)
 	}
 
 	/**
 	 * Loads the edges from an xml Document
 	 * @param doc The document where to get the edges
 	 * @throws IOException Exception if any problem occurs while reading the edges
 	 */
 	private void loadEdges(Document doc) throws IOException {
 		// get list of edges
 		NodeList edgeList = doc.getElementsByTagName("edge");
 		for (int i = 0; i < edgeList.getLength(); i++) {
 			Node edgeNode = edgeList.item(i);
 
 			if (edgeNode.getNodeType() == Node.ELEMENT_NODE) {
 				Element edgeElement = (Element) edgeNode;
 				// get from and to
 				String fromId = edgeElement.getAttribute("from");
 				String toId = edgeElement.getAttribute("to");
 				int labelX = Integer.valueOf(edgeElement.getAttribute("labelx"));
 				int labelY = Integer.valueOf(edgeElement.getAttribute("labely"));
 				State from = this.getStateById(fromId);
 				State to = this.getStateById(toId);
 
 				if (from == null) {
 					throw new IOException("Invalid edge starting point: No such state with ID '" + fromId + "'.");
 				}
 				if (to == null) {
 					throw new IOException("Invalid edge end point: No such state with ID '" + fromId + "'.");
 				}
 
 				// get transitions
 				NodeList transitionList = edgeElement.getElementsByTagName("transition");
 				ArrayList<Transition> transitions = new ArrayList<Transition>();
 				for (int j = 0; j < transitionList.getLength(); j++) {
 					Node transitionNode = transitionList.item(j);
 					if (transitionNode.getNodeType() == Node.ELEMENT_NODE) {
 						Element transitionElement = (Element) transitionNode;
 						Transition transition = this.loadTransition(transitionElement);
 						transitions.add(transition);
 					}
 				}
 
 				Edge edge = new Edge(from, to, transitions);
 				edge.setPosLabelX(labelX);
 				edge.setPosLabelY(labelY);
 				edges.add(edge);
 				System.out.println(" " + edge); //TODO: remove debug output
 
 				// write edges that start at a state
 				for (int j = 0; j < states.size(); j++) { //TODO: review
 					ArrayList<Edge> tempStartEdges = new ArrayList<Edge>();
 					for (int k = 0; k < edges.size(); k++) {
 						State tempStateFrom = edges.get(k).getFrom(); 
 						if (tempStateFrom.getId().equals(states.get(j).getId())) {
 							tempStartEdges.add(edges.get(k));
 						}
 					}
 					states.get(j).setEdge(tempStartEdges);
 				}
 			}
 		} //end for (next edge)
 
 		//check transition ids integrity
 		for (Edge edge : this.edges) {
 			for(Transition transition : edge.getTransitions()) {
 				if (this.getTransitionById(transition.getId()) != transition) {
 					throw new IOException("Transition ID '" + transition.getId() + "' is used more than once! Please check your xml file!");
 				}
 			}
 		}
 	}
 
 	private Transition loadTransition(Element transitionElement) throws IOException {
 		String id = transitionElement.getAttribute("id");
 
 		ArrayList<Character> read = this.loadSymbolList("read", "symbol", transitionElement);
 		ArrayList<Character> write = this.loadSymbolList("write", "symbol", transitionElement);
 		ArrayList<Character> action = this.loadSymbolList("action", "direction", transitionElement);
 
 		//check number of symbols
 		if (read.size() != this.getNumberOfTapes()) {
 			throw new IOException("Expected " + this.getNumberOfTapes() + " read symbols for Transition ID '" + id + "', but found " + read.size() + ". Make sure that the number of symbols matches the number of tapes.");
 		}
 		if (write.size() != this.getNumberOfTapes()) {
 			throw new IOException("Expected " + this.getNumberOfTapes() + " write symbols for Transition ID '" + id + "', but found " + read.size() + ". Make sure that the number of symbols matches the number of tapes.");
 		}
 		if (action.size() != this.getNumberOfTapes()) {
 			throw new IOException("Expected " + this.getNumberOfTapes() + " action symbols for Transition ID '" + id + "', but found " + read.size() + ". Make sure that the number of symbols matches the number of tapes.");
 		}
 		
 		// check actions
 		for (Character c : action) {
 			if (!(c=='L' || c=='N' || c=='R')) {
 				throw new IOException("Unsupported action '" + c + "'. Must be 'L', 'N' or 'R' (Transition ID '" + id + ")");
 			}
 		}
 
 		return new Transition(id, read, write, action);
 	}
 
 	private ArrayList<Character> loadSymbolList(String tag, String symboltag, Element transitionElement) throws IOException {
 		String id = transitionElement.getAttribute("id");
 		ArrayList<Character> symbols = new ArrayList<Character>();
 
 		Element tagElement = InOut.getChildElement(tag, transitionElement);
 		NodeList symbolList = tagElement.getChildNodes();
 		for (int i = 0; i < symbolList.getLength(); i++) {
 			Node symbolNode = symbolList.item(i);
 			if (symbolNode.getNodeType() == Node.ELEMENT_NODE && symbolNode.getNodeName().equals(symboltag)) {
 				String symbolString = symbolNode.getTextContent();
 				if (symbolString.length() != 1) {
 					throw new IOException("Expected exactly one character per symbol in the '" 
 							+ tag + "' section of transition with ID '" + id + "' but found the string '" 
 							+ symbolString + "' with a length of " + symbolString.length() + " instead.");
 				}
 				symbols.add(symbolString.charAt(0));
 			}
 		}
 		return symbols;
 	}
 	
 	private void loadTextboxes(Document doc) throws IOException {
 		NodeList textboxList = doc.getElementsByTagName("textbox");
 		for (int i = 0; i < textboxList.getLength(); i++) {
 			Node textboxNode = textboxList.item(i);
 	
 			if (textboxNode.getNodeType() != Node.ELEMENT_NODE) {
 				break; //ignore attributes etc.
 			}
 			Element textboxElement = (Element) textboxNode;
 	
 			//Get tape type and name
 			int height = Integer.parseInt(textboxElement.getAttribute("height"));
 			int width = Integer.parseInt(textboxElement.getAttribute("width"));
 			int x = Integer.parseInt(textboxElement.getAttribute("x"));
 			int y = Integer.parseInt(textboxElement.getAttribute("y"));
 			String text = InOut.getTagValue("text", textboxElement);
 	
 			Textbox textbox = new Textbox(text, x, y, width, height);
 			this.textboxes.add(textbox);
 		}
 	}
 	
 	private void loadFrames(Document doc) throws IOException {
 		NodeList frameList = doc.getElementsByTagName("frame");
 		for (int i = 0; i < frameList.getLength(); i++) {
 			Node frameNode = frameList.item(i);
 	
 			if (frameNode.getNodeType() != Node.ELEMENT_NODE) {
 				break; //ignore attributes etc.
 			}
 			Element frameElement = (Element) frameNode;
 	
 			//Get tape type and name
 			int height = Integer.parseInt(frameElement.getAttribute("height"));
 			int width = Integer.parseInt(frameElement.getAttribute("width"));
 			int x = Integer.parseInt(frameElement.getAttribute("x"));
 			int y = Integer.parseInt(frameElement.getAttribute("y"));
 	
 			Frame frame = new Frame(x, y, width, height);
			this.frames.add(frame);
 		}
 	}
 
 	/**
 	 * Writes the Turing machine to a XML file with a given name
 	 * @param filename File writing to (with or without .xml at the end)
 	 */
 	public void save(String filename) throws IOException {
 		System.out.println("Saving file '" + filename + "'...");
 
 		if (!filename.endsWith(".tm")) {
 			throw new IOException("Wrong file extension of file '" + filename + "'. Must be '.tm'");
 		}
 
 		TransformerFactory transformerFactory = TransformerFactory.newInstance();
 
 		Transformer transformer = null;
 		DocumentBuilder docBuilder = null;
 		try {
 			transformer = transformerFactory.newTransformer();
 			transformer.setOutputProperty(OutputKeys.ENCODING, "utf-8");
 			transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
 			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
 			transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
 
 			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
 			docBuilder = docFactory.newDocumentBuilder();
 		} catch (TransformerConfigurationException e) {
 			IOException ex = new IOException("Cannot write XML files. Serious configuration error.");
 			ex.initCause(e);
 		} catch (ParserConfigurationException e) {
 			IOException ex = new IOException("Cannot write XML files. Serious configuration error.");
 			ex.initCause(e);
 		}
 
 		Document doc = docBuilder.newDocument();
 		// create root element
 		Element rootElement = doc.createElement("machine");
 		doc.appendChild(rootElement);
 
 		// save name of machine
 		Attr attrName = doc.createAttribute("name");
 		attrName.setValue(this.getName());
 		rootElement.setAttributeNode(attrName);
 
 		// save number tapes of machine
 		Attr attrTape = doc.createAttribute("tapes");
 		attrTape.setValue(String.valueOf(this.getTapes().size()));
 		rootElement.setAttributeNode(attrTape);
 		
 		// save xml version number
 		Attr attrXMLVersion = doc.createAttribute("xml-version");
 		attrXMLVersion.setValue(String.valueOf(TuringMachine.XML_VERSION));
 		rootElement.setAttributeNode(attrXMLVersion);
 		
 		//save the rest
 		System.out.println("Saving tape configuration...");
 		this.saveTapesConfig(doc, rootElement);
 		System.out.println("Saving states...");
 		this.saveStates(doc, rootElement);
 		System.out.println("Saving edges and transitions...");
 		this.saveEdges(doc, rootElement);
 		System.out.println("Saving textboxes...");
 		this.saveTextboxes(doc, rootElement);
 		System.out.println("Saving frames...");
 		this.saveFrames(doc, rootElement);
 		System.out.println("Saving to file...");
 		StreamResult result = new StreamResult(new File(filename));
 		DOMSource source = new DOMSource(doc);
 		try {
 			transformer.transform(source, result);
 		} catch (TransformerException e) {
 			IOException ioex = new IOException("Error while writing the file '" + filename + "' to disk.");
 			ioex.initCause(e);
 			throw ioex;
 		}
 		System.out.println("File '" + filename + "' successfully saved.");
 	}
 	
 	private void saveTapesConfig(Document doc, Element rootElement) {
 		for (Tape tape : this.tapes) {
 			// tape element
 			Element tapeElement = doc.createElement("tape");
 			rootElement.appendChild(tapeElement);
 			
 			// save tape type number
 			Attr attrType = doc.createAttribute("type");
 			String tapeType = "";
 			if (tape.getType() == Tape.Type.LEGO) {
 				tapeType = "LEGO";
 			}
 			else if (tape.getType() == Tape.Type.CONSOLE) {
 				tapeType = "console";
 			}
 			else if (tape.getType() == Tape.Type.GRAPHIC) {
 				tapeType = "graphic";
 			}
 			attrType.setValue(tapeType);
 			tapeElement.setAttributeNode(attrType);
 			
 			//save tape name
 			Element nameElement = doc.createElement("name");
 			nameElement.appendChild(doc.createTextNode(tape.getName()));
 			tapeElement.appendChild(nameElement);
 						
 			if (tape.getType().equals("LEGO")) {
 				LEGOTape lego_tape = (LEGOTape) tape;
 				
 				//save master robot name and mac address
 				Element masterElement = doc.createElement("master");
 				Attr attrMasterMac = doc.createAttribute("mac-address");
 				attrMasterMac.setValue(lego_tape.getMaster().getMacAddress());
 				masterElement.setAttributeNode(attrMasterMac);
 				masterElement.appendChild(doc.createTextNode(lego_tape.getMaster().getName()));
 				tapeElement.appendChild(masterElement);
 				
 				//save slave robot name and mac address
 				Element slaveElement = doc.createElement("slave");
 				Attr attrSlaveMac = doc.createAttribute("mac-address");
 				attrSlaveMac.setValue(lego_tape.getSlave().getMacAddress());
 				slaveElement.setAttributeNode(attrSlaveMac);
 				slaveElement.appendChild(doc.createTextNode(lego_tape.getSlave().getName()));
 				tapeElement.appendChild(slaveElement);
 			}
 			
 			// save input word
 			Element inputElement = doc.createElement("input");
 			tapeElement.appendChild(inputElement);
 
 			for (char symbol : tape.getInputWord().toCharArray()) {
 				Element symbolElement = doc.createElement("symbol");
 				symbolElement.appendChild(doc.createTextNode("" + symbol));
 				inputElement.appendChild(symbolElement);
 			}
 		}
 	}
 	
 	private void saveStates(Document doc, Element rootElement) {
 		for(State state : this.states) {
 			// state element
 			Element stateElement = doc.createElement("state");
 			rootElement.appendChild(stateElement);
 
 			// save id of state
 			Attr attrStateId = doc.createAttribute("id");
 			attrStateId.setValue(state.getId());
 			stateElement.setAttributeNode(attrStateId);
 
 			// save type of state
 			Attr attrStart = doc.createAttribute("start");
 			if (state.isStartState()) {
 				attrStart.setValue("yes");
 			}
 			else {
 				attrStart.setValue("no");
 			}
 			stateElement.setAttributeNode(attrStart);
 
 			Attr attrFinal = doc.createAttribute("final");
 			if (state.isFinalState()) {
 				attrFinal.setValue("yes");
 			}
 			else {
 				attrFinal.setValue("no");
 			}
 			stateElement.setAttributeNode(attrFinal);
 			
 			// save the state position
 			Attr attrX = doc.createAttribute("x");
 			attrX.setValue(String.valueOf(state.getXcoord()));
 			stateElement.setAttributeNode(attrX);
 
 			Attr attrY = doc.createAttribute("y");
 			attrY.setValue(String.valueOf(state.getYcoord()));
 			stateElement.setAttributeNode(attrY);
 			
 			// save the state size
 			Attr attrWidth = doc.createAttribute("width");
 			attrWidth.setValue(String.valueOf(state.getWidth()));
 			stateElement.setAttributeNode(attrWidth);
 			
 			Attr attrHeight = doc.createAttribute("height");
 			attrHeight.setValue(String.valueOf(state.getHeight()));
 			stateElement.setAttributeNode(attrHeight);
 
 			// state name element
 			Element nameElement = doc.createElement("name");
 			nameElement.appendChild(doc.createTextNode(state.getName()));
 			stateElement.appendChild(nameElement);
 		}
 	}
 	
 	private void saveEdges(Document doc, Element rootElement) {
 		for (Edge edge : this.getEdges()) {
 			Element edgeElement = doc.createElement("edge");
 			rootElement.appendChild(edgeElement);
 			
 			// save from of edge
 			Attr attrEdgeFrom = doc.createAttribute("from");
 			attrEdgeFrom.setValue(edge.getFrom().getId());
 			edgeElement.setAttributeNode(attrEdgeFrom);
 			
 			// save to of edge
 			Attr attrEdgeTo = doc.createAttribute("to");
 			attrEdgeTo.setValue(edge.getTo().getId());
 			edgeElement.setAttributeNode(attrEdgeTo);
 			
 			// save to of edge
 			Attr attrEdgeLabelX = doc.createAttribute("labelx");
 			attrEdgeLabelX.setValue("" + edge.getPosLabelX());
 			edgeElement.setAttributeNode(attrEdgeLabelX);
 			
 			// save to of edge
 			Attr attrEdgeLabelY = doc.createAttribute("labely");
 			attrEdgeLabelY.setValue("" + edge.getPosLabelY());
 			edgeElement.setAttributeNode(attrEdgeLabelY);
 			
 			for (Transition transition : edge.getTransitions()) {
 				this.saveTransition(transition, doc, edgeElement);
 			}
 		}
 	}
 	
 	private void saveTransition(Transition transition, Document doc, Element edgeElement) {
 		// transition element
 		Element transitionElement = doc.createElement("transition");
 		edgeElement.appendChild(transitionElement);
 
 		// save id of transition
 		Attr attrTransitionId = doc.createAttribute("id");
 		attrTransitionId.setValue(transition.getId());
 		transitionElement.setAttributeNode(attrTransitionId);
 
 		// edge read elements
 		ArrayList<Character> read = transition.getRead();
 		Element readElement = doc.createElement("read");
 		transitionElement.appendChild(readElement);
 		for (int k = 0; k < read.size(); k++) {
 			Element symbolElement = doc.createElement("symbol");
 			symbolElement.appendChild(doc.createTextNode("" + read.get(k)));
 			readElement.appendChild(symbolElement);
 		}
 
 		// edge write elements
 		ArrayList<Character> write = transition.getWrite();
 		Element writeElement = doc.createElement("write");
 		transitionElement.appendChild(writeElement);
 		for (int k = 0; k < write.size(); k++) {
 			Element symbolElement = doc.createElement("symbol");
 			symbolElement.appendChild(doc.createTextNode("" + write.get(k)));
 			writeElement.appendChild(symbolElement);
 		}
 
 		// edge action elements
 		ArrayList<Character> action = transition.getAction();
 		Element actionElement = doc.createElement("action");
 		transitionElement.appendChild(actionElement);
 		for (int k = 0; k < action.size(); k++) {
 			Element symbolElement = doc.createElement("direction");
 			symbolElement.appendChild(doc.createTextNode("" + action.get(k)));
 			actionElement.appendChild(symbolElement);
 		}
 	}
 	
 	private void saveTextboxes(Document doc, Element rootElement) {
 		for (Textbox textbox : this.getTextboxes()) {
 			Element textboxElement = doc.createElement("textbox");
 			rootElement.appendChild(textboxElement);
 			
 			// save x textbox
 			Attr attrX = doc.createAttribute("x");
 			attrX.setValue("" + textbox.getX());
 			textboxElement.setAttributeNode(attrX);
 			
 			// save y textbox
 			Attr attrY = doc.createAttribute("y");
 			attrY.setValue("" + textbox.getY());
 			textboxElement.setAttributeNode(attrY);
 			
 			// save width textbox
 			Attr attrWidth = doc.createAttribute("width");
 			attrWidth.setValue("" + textbox.getWidth());
 			textboxElement.setAttributeNode(attrWidth);
 			
 			// save height textbox
 			Attr attrHeight = doc.createAttribute("height");
 			attrHeight.setValue("" + textbox.getHeight());
 			textboxElement.setAttributeNode(attrHeight);
 			
 			// textbox text element
 			Element textElement = doc.createElement("text");
 			textElement.appendChild(doc.createTextNode(textbox.getText()));
 			textboxElement.appendChild(textElement);
 		}
 	}
 	
 	private void saveFrames(Document doc, Element rootElement) {
 		for (Frame frame : this.getFrames()) {
 			Element frameElement = doc.createElement("frame");
 			rootElement.appendChild(frameElement);
 			
 			// save x textbox
 			Attr attrX = doc.createAttribute("x");
 			attrX.setValue("" + frame.getX());
 			frameElement.setAttributeNode(attrX);
 			
 			// save y textbox
 			Attr attrY = doc.createAttribute("y");
 			attrY.setValue("" + frame.getY());
 			frameElement.setAttributeNode(attrY);
 			
 			// save width textbox
 			Attr attrWidth = doc.createAttribute("width");
 			attrWidth.setValue("" + frame.getWidth());
 			frameElement.setAttributeNode(attrWidth);
 			
 			// save height textbox
 			Attr attrHeight = doc.createAttribute("height");
 			attrHeight.setValue("" + frame.getHeight());
 			frameElement.setAttributeNode(attrHeight);
 		}
 	}
 
 	/**
 	 * Gets a state of the TuringMachine with a given ID
 	 * @param id Id to be searched
 	 * @return The State with the given ID, or null if the state doesn't exist
 	 */
 	public State getStateById(String id) {
 		for (State state : this.states) {
 			if (state.getId().equals(id)) {
 				return state;
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * Gets a transition of the TuringMachine with a given ID
 	 * @param id Id to be searched
 	 * @return The Transition with the given ID, or null if the state doesn't exist
 	 */
 	public Transition getTransitionById(String id) {
 		for (Edge edge : this.edges) {
 			for (Transition transition : edge.transitions) {
 				if (transition.getId().equals(id)) {
 					return transition;
 				}
 			}
 		}
 		return null;
 	}
 	
 	/**
 	 * Sets the machine name
 	 * @param name The name to be set
 	 */
 	public void setName(String name) {
 		this.name = name;
 	}
 
 	/**
 	 * Gives a string representation of the Turing Machine
 	 */
 	@Override
 	public String toString() {
 		String str = "";
 		str += "TM name: " + this.getName() + ", ";
 		str += "Tapes count:" + this.getTapes() + "\n";
 		str += "Tapes: " + tapes + "\n";
 		str += "States: " + states + "\n";
 		str += "Edges:\n" + edges + "\n";
 		return str;
 	}
 
 	@Override
 	public Simulation createSimulation() throws TapeException{
 		return new TuringSimulation(this);
 	}
 
 	@Override
 	protected MachineEditor createEditor() {
 		return new TuringMachineEditor(this); //TODO: review
 		
 	}
 
 	@Override
 	public MachineType getType() {
 		return Machine.MachineType.TuringMachine;
 	}
 }
