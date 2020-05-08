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
 
 	public TuringMachine() {
 		this("Untitled Turing machine");
 	}
 
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
 	
 	public void initTapes() throws TapeException {
 		//init tapes
 		for (Tape tape : this.tapes) {
 			tape.init();
 		}
 	}
 	
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
 	 * Returns the initial configuration for the Turing machine's tapes
 	 * @return Initial configuration for the Turing machine's tapes
 	 */
 	//	public ArrayList<ArrayList<Character>> getInitial() {
 	//		return this.initial;
 	//	}
 
 	//	public String getInput(int i) {
 	//		String input = "";
 	//		for (int j = 0; j < getInitial().get(i).size(); j++){
 	//			input = input + getInitial().get(i).get(j);
 	//		}
 	//		return input;
 	//	}
 
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
 		machine.loadTapesConfig(doc);
 		machine.loadStates(doc);
 		machine.loadEdges(doc);
 
 		return machine;
 	}
 
 	public void loadTapesConfig(Document doc) throws IOException{
		String numberTapesString = doc.getDocumentElement().getAttribute("tapes");
		int numberTapes = Integer.parseInt(numberTapesString);

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
 
 			NodeList inputList = tapeElement.getElementsByTagName("input");
 			Node inputNode = null;
			for (int j=0; j < inputList.getLength(); i++) {
 				if (inputNode != null && inputList.item(0).getNodeType() != Node.ELEMENT_NODE ) {
 					throw new IOException("Multiple input words for tape '" + tapeName + "' are not allowed.");
 				}
				inputNode = inputList.item(i);
 				if (inputNode.getNodeType() != Node.ELEMENT_NODE) {
 					break; //ignore attributes etc.
 				}
 			}
 			if (inputNode == null) {
 				throw new IOException("Input word missing for tape '" + tapeName + "'.");
 			}
 			Element inputElement = (Element) inputNode;
 
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
 				//TODO: read master/slave name and mac address
 				//LEGOTape tape = new LEGOTape();
 				throw new IOException("Parsing LEGO tape not implemented yet.");
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
 		} //end for (next tape)
 		
 		//Check if tape count attribute matches the number of tapes
 		String machineTapesCountString = doc.getDocumentElement().getAttribute("tapes");
 		int machineTapesCount = Integer.parseInt(machineTapesCountString);
 		
 		if (machineTapesCount != this.tapes.size()) {
 			throw new IOException("The tapes count attribute of the machine did not match the number of tapes actually defined (attribute is " 
 					+ machineTapesCountString + " but " + this.tapes.size() + " tapes were defined).");
 		}
 	}
 
 	public void loadStates(Document doc) throws IOException {
 		// get list of nodes
 		NodeList stateList = doc.getElementsByTagName("state");
		ArrayList<State> states = new ArrayList<State>();
 		for (int i = 0; i < stateList.getLength(); i++) {
 			Node currentNode = stateList.item(i);
 
 			if (currentNode.getNodeType() == Node.ELEMENT_NODE) {
 				Element currentElement = (Element) currentNode;
 				String id = currentElement.getAttribute("id");
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
 				String name = InOut.getTagValue("name", currentElement);
 				State tempState = new State(id, name, type);
 				states.add(tempState);
 
 				// TODO remove test output
 				System.out.println("\n== State " + id + " ==\n");
 				System.out.println("id: " + id);
 				System.out.println("name: " + name);
 				System.out.println("type: " + type);
 			}
 		}
 	}
 
 	public void loadEdges(Document doc) {
 		// get list of edges
 		NodeList edgeList = doc.getElementsByTagName("edge");
		ArrayList<Edge> edges = new ArrayList<Edge>();
		ArrayList<Transition> transitions = new ArrayList<Transition>();
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
