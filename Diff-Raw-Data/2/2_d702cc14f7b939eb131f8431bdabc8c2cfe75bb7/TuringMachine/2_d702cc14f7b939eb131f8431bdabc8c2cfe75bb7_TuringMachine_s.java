 package TuringMachine;
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 import Tape.ConsoleTape;
 import Tape.LEGOTape;
 import Tape.MasterRobot;
 import Tape.SlaveRobot;
 import Tape.Tape;
 import Tape.TapeException;
 
 /** This class represents a Turing machine with it's states, edges and transitions
  * 
  * @author David Wille
  * 
  */
 public class TuringMachine {
 	protected String name;
 	protected ArrayList<State> states;
 	protected ArrayList<Edge> edges;
 	protected ArrayList<Tape> tapes;
 
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
 	 * Constructs a new Turing machine with edges, states and transitions
 	 * @param states All states of the Turing machine
 	 * @param edges All edges of the Turing machine
 	 * @param name The name of the Turing machine
 	 * @param tapes The number of tapes used in the Turing machine
 	 * @param initial The initial input on the Turing machine's tapes
 	 */
 
 	public TuringMachine(String name, ArrayList<State> states, ArrayList<Edge> edges, ArrayList<Tape> tapes) {
 		this.states = states;
 		this.edges = edges;
 		this.name = name;
 		this.tapes = tapes;
 	}
 	
 	/**
 	 * Initializes the tapes
 	 * @throws TapeException Exception if any problems occur while initializing
 	 */
 	public void initTapes() throws TapeException {
 		//init tapes
 		for (Tape tape : this.tapes) {
 			tape.init();
 			tape.writeInputWord();
 		}
 	}
 	
 	/**
 	 * Shuts down the tapes
 	 * @throws TapeException Exception if any problems occur while shuting down
 	 */
 	public void shutdownTapes() throws TapeException {
 		//init tapes
 		for (Tape tape : this.tapes) {
 			tape.shutdown();
 		}
 	}
 	
 
 	/**
 	 * Returns the Turing machine's name
 	 * @return The Turing machine's name
 	 */
 	public String getName() {
 		return this.name;
 	}
 
 	/**
 	 * Returns the Turing machine's number of tapes
 	 * @return Number of tapes
 	 */
 	public ArrayList<Tape> getTapes() {
 		return this.tapes;
 	}
 	
 	public int getNumberOfTapes() {
 		return this.tapes.size();
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
 	 * Reads a Turing machine from a XML file
 	 * @param filename File to read the Turing machine from (with or without .xml at the end)
 	 * @return Turing machine object
 	 */
 	public static TuringMachine loadFromXML(String filename) throws IOException {
 		if (!filename.endsWith(".xml")) {
 			throw new IOException("Wrong file extension of file '" + filename + "'. Must be '.xml'");
 		}
 
 		//parse document
		System.out.println("Loading file '" + filename + "...");
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
 		String machineXMLVersion = doc.getDocumentElement().getAttribute("xml-version");
 
 		if (!machineXMLVersion.equals("2")) {
 			throw new IOException("The file format version of the file '" + filename 
 					+ "' is not supported by this program. Please convert the file before opening.");
 		}
 
 		//load the rest
 		TuringMachine machine = new TuringMachine(machineName);
 		System.out.println("Loading tape configuration...");
 		machine.loadTapesConfig(doc);
 		System.out.println("Loading states...");
 		machine.loadStates(doc);
 		System.out.println("Loading edges and transitions...");
 		machine.loadEdges(doc);
 
 		return machine;
 	}
 
 	/**
 	 * Loads the tapes' configuration from a xml Document 
 	 * @param doc Document to load the configuration from
 	 * @throws IOException Exception if any problems occur while reading the configuration
 	 */
 	public void loadTapesConfig(Document doc) throws IOException{
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
 			else if (tapeType.equals("gui")) {
 				throw new IOException("GUI tape not implemented yet.");
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
 	public void loadStates(Document doc) throws IOException {
 		// get list of nodes
 		NodeList stateList = doc.getElementsByTagName("state");
 		for (int i = 0; i < stateList.getLength(); i++) {
 			Node currentNode = stateList.item(i);
 
 			if (currentNode.getNodeType() == Node.ELEMENT_NODE) {
 				Element currentElement = (Element) currentNode;
 				String id = currentElement.getAttribute("id");
 				if (checkExistenceOfId(id)) {
 					throw new IOException("Id: " + id + " already exists! Please check your xml file!");
 				}
 				
 				State.Type type = null;
 				String typeString = currentElement.getAttribute("type");
 				if (typeString.equals("start")) {
 					type = State.Type.START;
 				}
 				else if (typeString.equals("normal")) {
 					type = State.Type.NORMAL;
 				}
 				else if (typeString.equals("final")) {
 					type = State.Type.FINAL;
 				}
 				else {
 					throw new IOException("Unsupported state type '" + typeString + "' for the state with id " + id + ".  Expected 'start', 'final' or 'normal'.");
 				}
 				String name = InOut.getTagValue("name", currentElement);
 				State state = new State(id, name, type);
 				states.add(state);
 				System.out.println(state); //TODO: remove debug output
 			}
 		} //end for (next state)
 	}
 
 	/**
 	 * Loads the edges from an xml Document
 	 * @param doc The document where to get the edges
 	 * @throws IOException Exception if any problem occurs while reading the edges
 	 */
 	public void loadEdges(Document doc) {
 		// get list of edges
 		NodeList edgeList = doc.getElementsByTagName("edge");
 		for (int i = 0; i < edgeList.getLength(); i++) {
 			Node currentNode = edgeList.item(i);
 
 			if (currentNode.getNodeType() == Node.ELEMENT_NODE) {
 				Element currentElement = (Element) currentNode;
 				// get from an to
 				State from = null;
 				State to = null;
 				for (int j = 0; j < states.size(); j++) {
 					if (states.get(j).getId().equals(currentElement.getAttribute("from"))) {
 						from = states.get(j);
 					}
 					if (states.get(j).getId().equals(currentElement.getAttribute("to"))) {
 						to = states.get(j);
 					}
 				}
 				// TODO remove test output
 				System.out.println("\n== Edge " + i + " ==\n");
 				System.out.println("from: " + from.getId());
 				System.out.println("to: " + to.getId());
 
 				// get transitions
 				NodeList transitionList = currentElement.getElementsByTagName("transition");
 				ArrayList<Transition> transition = new ArrayList<Transition>();
 				for (int j = 0; j < transitionList.getLength(); j++) {
 					transition.clear();
 					Node currentTransitionNode = transitionList.item(j);
 					String id;
 					ArrayList<Character> read = new ArrayList<Character>();
 					ArrayList<Character> write = new ArrayList<Character>();
 					ArrayList<Character> action = new ArrayList<Character>();
 					if (currentTransitionNode.getNodeType() == Node.ELEMENT_NODE) {
 						Element currentTransitionElement = (Element) currentTransitionNode;
 						id = currentTransitionElement.getAttribute("id");
 						// TODO remove test output
 						System.out.println("== TRANSITION" + id + "==");
 
 						// get read
 						NodeList readList = currentTransitionElement.getElementsByTagName("read");
 						for (int k = 0; k < readList.getLength(); k++) {
 							Node currentReadNode = readList.item(k);
 							if (currentReadNode.getNodeType() == Node.ELEMENT_NODE) {
 								Element currentReadElement = (Element) currentReadNode;
 								NodeList readSymbolList = currentReadElement.getChildNodes();
 								for (int l = 0; l < readSymbolList.getLength(); l++) {
 									Node currentReadSymbolNode = readSymbolList.item(l);
 									if (currentReadSymbolNode.getNodeType() == Node.ELEMENT_NODE) {
 										read.add(currentReadSymbolNode.getTextContent().charAt(0));
 										// TODO remove test output
 										System.out.println("read: " + currentReadSymbolNode.getTextContent());
 									}
 								}
 							}
 						}
 
 						// get write
 						NodeList writeList = currentTransitionElement.getElementsByTagName("write");
 						for (int k = 0; k < writeList.getLength(); k++) {
 							Node currentWriteNode = writeList.item(k);
 							if (currentWriteNode.getNodeType() == Node.ELEMENT_NODE) {
 								Element currentWriteElement = (Element) currentWriteNode;
 								NodeList writeSymbolList = currentWriteElement.getChildNodes();
 								for (int l = 0; l < writeSymbolList.getLength(); l++) {
 									Node currentWriteSymbolNode = writeSymbolList.item(l);
 									if (currentWriteSymbolNode.getNodeType() == Node.ELEMENT_NODE) {
 										write.add(currentWriteSymbolNode.getTextContent().charAt(0));
 										// TODO remove test output
 										System.out.println("write: " + currentWriteSymbolNode.getTextContent());
 									}
 								}
 							}
 						}
 
 						// get action
 						NodeList actionList = currentTransitionElement.getElementsByTagName("action");
 						for (int k = 0; k < actionList.getLength(); k++) {
 							Node currentActionNode = actionList.item(k);
 							if (currentActionNode.getNodeType() == Node.ELEMENT_NODE) {
 								Element currentActionElement = (Element) currentActionNode;
 								NodeList actionSymbolList = currentActionElement.getChildNodes();
 								for (int l = 0; l < actionSymbolList.getLength(); l++) {
 									Node currentActionSymbolNode = actionSymbolList.item(l);
 									if (currentActionSymbolNode.getNodeType() == Node.ELEMENT_NODE) {
 										action.add(currentActionSymbolNode.getTextContent().charAt(0));
 										// TODO remove test output
 										System.out.println("direction: " + currentActionSymbolNode.getTextContent());
 									}
 								}
 							}
 						}
 
 						Transition tempTransition = new Transition(id, read, write, action);
 						transition.add(tempTransition);
 					}
 				}
 
 				Edge tempEdge = new Edge(from, to, transition);
 				edges.add(tempEdge);
 
 				// write edges that start at a state
 				for (int j = 0; j < states.size(); j++) {
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
 		}
 	}
 	
 	/**
 	 * Checks if a given id already exists
 	 * @param id Id to check
 	 * @return true/false depending on existence
 	 */
 	public boolean checkExistenceOfId(String id) {
 		for (int i = 0; i < this.states.size(); i++) {
 			if (this.states.get(i).getId() == id) {
 				return true;
 			}
 		}
 		return false;
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
 
 }
