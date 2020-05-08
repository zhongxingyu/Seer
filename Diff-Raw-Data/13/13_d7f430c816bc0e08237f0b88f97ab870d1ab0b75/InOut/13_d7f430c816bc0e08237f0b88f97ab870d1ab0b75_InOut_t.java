 package TuringMachine;
 import java.io.*;
 import java.lang.String;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Comparator;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.transform.OutputKeys;
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerException;
 import javax.xml.transform.TransformerFactory;
 import javax.xml.transform.dom.DOMSource;
 import javax.xml.transform.stream.StreamResult;
 
 import org.w3c.dom.Attr;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 /** This class provides methods to read and write Turing machines from / to files
  * 
  * @author David Wille
  * 
  */
 
 public class InOut {
 
 	/**
 	 * Writes a Turing machine to a XML file with a given name
 	 * @param fileName File writing to (with or without .xml at the end)
 	 * @param machine Turing machine object which should be written to file
 	 */
 	public static void writeXMLToFile(String fileName, TuringMachine machine) { //TODO: an neues XML format anpassen
 //		try {
 //			try {
 //				TransformerFactory transformerFactory = TransformerFactory.newInstance();
 //
 //				Transformer transformer = transformerFactory.newTransformer();
 //				transformer.setOutputProperty(OutputKeys.ENCODING, "utf-8");
 //				transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
 //				transformer.setOutputProperty(OutputKeys.INDENT, "yes");
 //				transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
 //
 //				DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
 //				DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
 //				Document doc = docBuilder.newDocument();
 //				DOMSource source = new DOMSource(doc);
 //
 //				// create root element
 //				Element rootElement = doc.createElement("machine");
 //				doc.appendChild(rootElement);
 //
 //				// save name of machine
 //				Attr attrName = doc.createAttribute("name");
 //				attrName.setValue(machine.getName());
 //				rootElement.setAttributeNode(attrName);
 //
 //				// save number tapes of machine
 //				Attr attrTape = doc.createAttribute("tape");
 //				attrTape.setValue(String.valueOf(machine.getTapes()));
 //				rootElement.setAttributeNode(attrTape);
 //
 //				// get states and edges from machine
 //				ArrayList<State> states = machine.getStates();
 //				ArrayList<Edge> edges = machine.getEdges();
 //
 //				// inputs
 //				ArrayList<ArrayList<Character>> inputCharacter = machine.getInitial();
 //				for(int i = 0; i < inputCharacter.size(); i++) {
 //					ArrayList<Character> tempInput = inputCharacter.get(i);
 //
 //					// input element
 //					Element input = doc.createElement("input");
 //					rootElement.appendChild(input);
 //
 //					// tape element
 //					Element tape = doc.createElement("tape");
 //					input.appendChild(tape);
 //
 //					// input tape elements
 //					for (int j = 0; j < tempInput.size(); j++) {
 //						Element symbolElement = doc.createElement("symbol");
 //						symbolElement.appendChild(doc.createTextNode("" + tempInput.get(j)));
 //						tape.appendChild(symbolElement);
 //					}
 //				}
 //
 //				// states
 //				for(int i = 0; i < states.size(); i++) {
 //					State tempState = states.get(i);
 //					// state element
 //					Element state = doc.createElement("state");
 //					rootElement.appendChild(state);
 //
 //					// save id of state
 //					Attr attrStateId = doc.createAttribute("id");
 //					attrStateId.setValue(tempState.getId());
 //					state.setAttributeNode(attrStateId);
 //
 //					// save type of state
 //					Attr attrType = doc.createAttribute("type");
 //					String type;
 //					switch(tempState.getType()) {
 //					case START:
 //						type = "start";
 //						break;
 //					case NORMAL:
 //						type = "normal";
 //						break;
 //					case FINAL:
 //						type = "final";
 //						break;
 //					default:
 //						type = "normal";
 //						break;
 //					}
 //					attrType.setValue(type);
 //					state.setAttributeNode(attrType);
 //
 //					// state name element
 //					Element nameElement = doc.createElement("name");
 //					nameElement.appendChild(doc.createTextNode(tempState.getName()));
 //					state.appendChild(nameElement);
 //				}
 //
 //				// edges
 //				for(int i = 0; i < edges.size(); i++) { 
 //					Edge tempEdge = edges.get(i);
 //					ArrayList<Transition> transitions = tempEdge.getTransition();
 //					// edge element
 //					Element edge = doc.createElement("edge");
 //					rootElement.appendChild(edge);
 //
 //					// save from of edge
 //					Attr attrEdgeFrom = doc.createAttribute("from");
 //					attrEdgeFrom.setValue(edges.get(i).getFrom().getId());
 //					edge.setAttributeNode(attrEdgeFrom);
 //
 //					// save to of edge
 //					Attr attrEdgeTo = doc.createAttribute("to");
 //					attrEdgeTo.setValue(edges.get(i).getTo().getId());
 //					edge.setAttributeNode(attrEdgeTo);
 //
 //					for (int j = 0; j < transitions.size(); j++) {
 //						Transition tempTransition = transitions.get(j);
 //
 //						// transition element
 //						Element transition = doc.createElement("transition");
 //						edge.appendChild(transition);
 //
 //						// save id of transition
 //						Attr attrTransitionId = doc.createAttribute("type");
 //						attrTransitionId.setValue(tempTransition.getId());
 //						transition.setAttributeNode(attrTransitionId);
 //
 //						// edge read elements
 //						ArrayList<Character> read = tempTransition.getRead();
 //						Element readElement = doc.createElement("read");
 //						edge.appendChild(readElement);
 //						for (int k = 0; k < read.size(); k++) {
 //							Element symbolElement = doc.createElement("symbol");
 //							symbolElement.appendChild(doc.createTextNode("" + read.get(k)));
 //							readElement.appendChild(symbolElement);
 //						}
 //
 //						// edge write elements
 //						ArrayList<Character> write = tempTransition.getWrite();
 //						Element writeElement = doc.createElement("write");
 //						edge.appendChild(writeElement);
 //						for (int k = 0; k < write.size(); k++) {
 //							Element symbolElement = doc.createElement("symbol");
 //							symbolElement.appendChild(doc.createTextNode("" + write.get(k)));
 //							writeElement.appendChild(symbolElement);
 //						}
 //
 //						// edge action elements
 //						ArrayList<Character> action = tempTransition.getAction();
 //						Element actionElement = doc.createElement("action");
 //						edge.appendChild(actionElement);
 //						for (int k = 0; k < action.size(); k++) {
 //							Element symbolElement = doc.createElement("symbol");
 //							symbolElement.appendChild(doc.createTextNode("" + action.get(k)));
 //							actionElement.appendChild(symbolElement);
 //						}
 //					}
 //				}
 //
 //				// check for right file ending
 //				if (!fileName.endsWith(".xml")) {
 //					fileName = fileName + ".xml";
 //				}
 //
 //				// write the content into xml file
 //				StreamResult result = new StreamResult(new File(fileName));
 //				transformer.transform(source, result);
 //				// TODO remove test output
 //				System.out.println("Done writing file!\n");
 //			}
 //			catch (ParserConfigurationException pce) {
 //				pce.printStackTrace();
 //			}
 //		}
 //		catch (TransformerException tfe) {
 //			tfe.printStackTrace();
 //		}
 	}
 
 
 
 	/**
 	 * Writes a Turing machine to a LaTex file
 	 * @param fileName Name of the file, where to write to
 	 * @param machine Turing machine that should be written to the file
 	 */
 	public static void writeLatexToFile(String fileName, TuringMachine machine) {
 		// check for right file ending
 		if (!fileName.endsWith(".tex")) {
 			fileName = fileName + ".tex";
 		}
 
 		try {
 			// copy from template to new file
 			File inputFile = new File("template.tex");
 			File outputFile = new File(fileName);
 
 			FileReader in = new FileReader(inputFile);
 			FileWriter out = new FileWriter(outputFile);
 			int c;
 
 			while ((c = in.read()) != -1) {
 				out.write(c);
 			}
 			in.close();
 			out.close();
 
 			// edit new file
 			BufferedReader reader = new BufferedReader(new FileReader(outputFile));
 
 			// read text to temp string
 			String line = "";
 			String oldContent = "";
 			String automata = "";
 			String makeTitleName, newContent;
 			while((line = reader.readLine()) != null) {
 				oldContent += line + "\n";
 			}
 			reader.close();
 
 			// write maketitle
 			makeTitleName = "\\title{" + machine.getName() + "}";
 
 			// write automata nodes
 			automata += writeStatesToLatex(machine.getStates());
 			
 			automata += "\n";
 			
 			// write automata edges
 			automata += writeEdgesToLatex(machine.getEdges());
 
 			newContent = oldContent.replace("{PLACEHOLDER_MAKETITLENAME}", makeTitleName);
 			newContent = newContent.replace("{PLACEHOLDER_AUTOMATA}", automata);
 
 			FileWriter writer = new FileWriter(outputFile);
 			writer.write(newContent);
 			writer.close();
 		}
 		catch (IOException e) {
 		}
 	}
 
 	/**
 	 * Returns the LaTeX string of nodes for an automata
 	 * @param states The states that should be converted to LaTeX
 	 * @return String of nodes formated for an automata 
 	 */
 	private static String writeStatesToLatex(ArrayList<State> states) {
 		String output = "";
 		State oldLeft = states.get(0), oldMiddle = states.get(0);
 		for (int i = 0; i < states.size(); i++) {
 			State currentState = states.get(i);
 			String type = "";
 			String name = currentState.getName();
 			// check type of node
 			switch (currentState.getType()) {
 				case START:
 					type = ", initial";
 					break;
 				case NORMAL:
 					break;
 				case FINAL:
 					type = ", accepting";
 					break;
 				default:
 					break;
 			}
 
 			if (i % 3 == 0) {
 				if (i == 0) {
 					output += "\\node[state" + type + "] (" + name + ") {$" + name + "$};\n";
 				}
 				else {
 					output += "\\node[state" + type + "] (" + name + ") [below of = " + oldLeft.getName() + "] {$" + name + "$};\n";
 				}
 				oldLeft = currentState;
 			}
 			else if (i % 3 == 1) {
 				output += "\\node[state" + type + "] (" + name + ") [right of = " + oldLeft.getName() + "] {$" + name + "$};\n";
 				oldMiddle = currentState;
 			}
 			else if (i % 3 == 2) {
 				output += "\\node[state" + type + "] (" + name + ") [right of = " + oldMiddle.getName() + "] {$" + name + "$};\n";
 			}
 
 		}
 		return output;
 	}
 	
 	/**
 	 * Returns the LaTeX string of edges for an automata
 	 * @param edges The edges that should be converted to LaTeX
 	 * @return String of edges formated for an automata
 	 */
 	private static String writeEdgesToLatex(ArrayList<Edge> edges) {
 		String output = "";
 		for (int i = 0; i < edges.size(); i++) {
 			Edge currentEdge = edges.get(i);
 			for (int j = 0; j < currentEdge.getTransition().size(); j++) {
 				Transition currentTransition = currentEdge.getTransition().get(j);
 				String transition = writeTransitionString(currentTransition);
 				State from = currentEdge.getFrom();
 				State to = currentEdge.getTo();
 				if (from.equals(to)) {
 					output += "\\path[->] (" + from.getName() +  ") edge [loop above] node {" + transition + "} ();\n";
 				}
 				else {
 					output += "\\path[->] (" + from.getName() +  ") edge [bend left] node {" + transition + "} (" + to.getName() +  ");\n";
 				}
 			}
 		}
 		return output;
 	}
 	
 	/**
 	 * Returns the LaTeX string of a transition for an automata
 	 * @param transition The transition that should be converted to LaTeX
 	 * @return String of a transition formated for an automata
 	 */
 	private static String writeTransitionString(Transition transition) {
 		String output = "";
 		String readString = "<";
 		String writeActionString = "<";
 		Transition currentTransition = transition;
 		ArrayList<Character> read = currentTransition.getRead(); 
 		ArrayList<Character> write = currentTransition.getWrite(); 
 		ArrayList<Character> action = currentTransition.getAction();
 		for (int i = 0; i < read.size(); i++) {
 			readString += "" + escapeHash(read.get(i));
 			if (i == read.size()-1) {
 				readString += ">";
 			}
 			else {
 				readString += ", ";
 			}
 		}
 		for (int i = 0; i < write.size(); i++) {
 			writeActionString += "" + escapeHash(write.get(i)) + ", ";
 			writeActionString += "" + escapeHash(action.get(i)) + ">";
 			if (i != write.size()-1) {
 				writeActionString += ", ";
 			}
 		}
 		output += readString + " / " + writeActionString;
 		return output;
 	}
 	
 	/**
 	 * Checks for hashes and escapes them
 	 * @param charToCheck Character to check for a hash
 	 * @return The initial character or an escaped hash
 	 */
 	private static String escapeHash(char charToCheck) {
 		if (charToCheck == '#') {
 			return "\\#";
 		}
 		return "" + charToCheck;
 	}
 
 	/**
 	 * Returns the value of an element with a certain tag
 	 * @param tag Tag you want to read
 	 * @param currentElement The current element you want to read from
 	 * @return The value of that tag
 	 */
 	public static String getTagValue(String tag, Element currentElement) throws IOException {
 		NodeList nodeList = currentElement.getElementsByTagName(tag);
 		if (nodeList.getLength() > 1) {
 			throw new IOException("The tag '" + tag + "' is ambigious because it was used multiple times on '" 
 					+ currentElement.getNodeName() + "'.");
 		}
 		if (nodeList.getLength() < 1) {
 			throw new IOException("Missing tag '" + tag + "' on '" + currentElement.getNodeName() + "'.");
 		}
 		NodeList childList = nodeList.item(0).getChildNodes();
 
 		Node child = null;
 		for (int i = 0; i < childList.getLength(); i++) {
 			child = childList.item(i);
 			if (child.getNodeType() != Node.TEXT_NODE) {
 				break; //ignore attributes etc.
 			}
 		}
 		
 		if (child != null) {
 			return child.getNodeValue();
 		}
 		else {
 			System.out.println("WARNING: Tag '" + tag + "' on '" + currentElement.getNodeName() + "' is empty.");
 			return "";
 		}
 	}
 	
 	public static Element getChildElement(String tag, Element currentElement) throws IOException {
 		NodeList nodeList = currentElement.getElementsByTagName(tag);
 		Node node = null;
 		for (int i=0; i < nodeList.getLength(); i++) {
			if (node != null && nodeList.item(i).getNodeType() == Node.ELEMENT_NODE ) {
				throw new IOException("Multiple tag '" + tag + "' on '" + currentElement.getNodeName() + "' is not allowed.");
 			}
			if (nodeList.item(i).getNodeType() == Node.ELEMENT_NODE) {
				node = nodeList.item(i);
 			}
 		}
 		if (node == null) {
			throw new IOException("Expected missing tag '" + tag + "' on '" + currentElement.getNodeName() + "'.");
 		}
 		Element element = (Element) node;
 		return element;
 	}
 
 }
