 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.LinkedList;
 import java.util.Queue;
 /*ANSWERS TO PART 2:
  * 	1. 
  * 
  * 	2.
  * 
  */
 
 
 /**
  * Simple implementation of the Routing Information Protocol using multi-threading.<br>
  * Main algorithm logic is in <i>receiveFromQueue()</i> method of <b>Node</b> private class.
  * 
  * @author Paulius Bernotas, s0943933 <br>
  * University of Edinburgh
  *
  */
 public class RIP extends Thread {
 	
 	//Contains all nodes in the network.
 	ArrayList<Node> networkNodes = new ArrayList<Node>();
 	//This list contains nodes that will send their tables on cycle 1.
 	ArrayList<Node> startingNodes = new ArrayList<Node>();
 	//Variable used to check if any of the node tables in the network has changed.
 	boolean hasAnythingChanged = false;
 	//Variable used to check if routing algorithm should be started. Mostly used for debugging.
 	boolean RIPstart = false;
 	//Files for input and output
 	String inputFileName = "";
 	String outputFileName = "";
 	BufferedWriter out;
 	
 	public RIP(String[] args) {
 		super();
 		try {
 			inputFileName = args[0];
 			outputFileName = args[1];
 			
 			// Create output file 
 			FileWriter fout = new FileWriter(outputFileName);
 			out = new BufferedWriter(fout);	
 		} catch (IOException e){
 			System.err.println("Output file could not be created: " + e.getMessage());
 			System.exit(1);
 		} catch (Exception e) {
 			System.err.println("Incorrect arguments. Usage: \"java RIP inputFileName outputFileName\".");
 			System.exit(1);
 		}
 		
 		readNetworkConfig();
 		
 		//Start routing algorithm as soon as network configuration is read.
 		RIPstart = true;
 	}
 	
 	public static void main(String args[]) {
 		Thread rip = new RIP(args);
 		rip.start();		
 	}
 	
 	/**
 	 * Read network configuration from input file and initialise the nodes.
 	 */
 	public void readNetworkConfig() {
 		try {
 			FileReader fin = new FileReader(inputFileName);
 			BufferedReader in = new BufferedReader(fin);
 			String currentLine;
 			//Reads each line of the input file and looks at first word which should be a keyboard.
 			while ((currentLine = in.readLine()) != null) {
 				String[] tokens = currentLine.split(" ");
 				switch (tokens[0]) {
 				case "node":
 					//Create new node and add it to the network.
 					Node newNode = new Node(tokens[1], out);
 					for (int i=2; i<=tokens.length-1; i++) {
 						//Add 'local' table entries to the created node.
 						newNode.table.add(new TableRow(Integer.parseInt(tokens[i]), "local", 0));
 					}
 					networkNodes.add(newNode);
 					break;
 				case "link":
 					//Add link between the specified nodes.
 					for (Node node : networkNodes) {
 						if (node.getNodeName().equals(tokens[1])) {
 							for (Node linkNode : networkNodes) {
 								if (linkNode.getNodeName().equals(tokens[2])) { 
 									node.linkedNodes.add(linkNode);
 									linkNode.linkedNodes.add(node);
 								}
 							}
 						}
 					}
 					break;
 				case "send":
 					//Record that specified node will be sending its table on first cycle of routing algorithm.
 					for (Node node: networkNodes) {
 						if (node.getNodeName().equals(tokens[1])) {
 							startingNodes.add(node);
 						}
 					}
 					break;
 				default:
 					//If line did not contain a keyword at the start - do nothing.
 					break;
 				}
 			}
 			
 			//Close input file.
 			in.close();
 		} catch (FileNotFoundException e) {
 			System.err.println("Input file not found: " + e.getMessage());
 			System.exit(1);
 		} catch (IOException e) {
 			System.err.println("Input file could not be read: " + e.getMessage());
 			System.exit(1);
 		}		
 	}
 	
 	/**
 	 * Print out current network configuration to output file. Remember to call out.close() method
 	 * after calling printNetworkConfig() to finalise the output file.
 	 */
 	public void printNetworkConfig() {
 		try {
 			for (Node node : networkNodes) {
 				out.write(node.getNodeName()+"\n");
 				out.write("\t"+node.constructTableString()+"\n");
 				out.write("\t");
 				for (Node linkedNode : node.linkedNodes) {
 					out.write(linkedNode.getNodeName()+" ");
 				}
 				out.write("\n");
 			}
 			//out.close();
 		} catch (IOException e) {
 			System.err.println("Could not write to output file: " + e.getMessage());
 		}		
 	}
 	
 	@Override
 	public void run() {
 		while (true) {
 			//Start the algorithm by sending tables for the starting nodes.
 			if (RIPstart) {
 				for (Node node : startingNodes) node.multicastTable = true;
 				for (Node node : networkNodes) node.start();
 				RIPstart = false;
 			}
 			
 			//Check if table of any node in the network has changed at each cycle.
 			//If none of the tables has changed - the RIP algorithm terminates and prints the tables of each node.
			for (Node node : networkNodes) if (node.hasTableChanged() || node.receiveQueue.size()!=0) hasAnythingChanged = true;
 			if (!hasAnythingChanged) {
 				try {
 					out.write("\n");
 					for (Node node : networkNodes) out.write("table "+node.getNodeName()+" "+node.constructTableString()+"\n");
 					out.close();
 				} catch (IOException e) {
 					System.err.println("Could not write to output file: " + e.getMessage());
 					System.exit(1);
 				}
 				System.exit(0);
 			}
 			
 			//Reset variable for next cycle.
 			hasAnythingChanged = false;
 		}
 	}
 		
 	/**
 	 * Custom class that represents one node in a network. This class extends <b>Thread</b>
 	 * meaning that each node runs in a separate thread.
 	 * @author Paulius Bernotas, s0943933 <br>
 	 * University of Edinburgh
 	 *
 	 */
 	private class Node extends Thread {
 		
 		//Queue used to store received messages and process them one by one
 		private Queue<String> receiveQueue = new LinkedList<String>();
 		//Routing table for this node.
 		private ArrayList<TableRow> table = null;
 		//Reachable nodes from this node.
 		private ArrayList<Node> linkedNodes = null;
 		
 		//Variable for tracking of table of node has changed. Starts so that algorithm does not terminate on first cycle.
 		private boolean tableChanged = true;
 		//Variable for tracking if node has to send it's table to other nodes.
 		private boolean multicastTable = false;
 		//Name of the node.
 		private String nodeName = "";
 		//Output stream for writing into output file
 		private BufferedWriter out = null;
 		
 		Node(String nodeName, BufferedWriter out) {
 			super();
 			this.out = out;
 			this.nodeName = nodeName;
 			
 			table = new ArrayList<TableRow>();
 			linkedNodes = new ArrayList<Node>();
 		}
 
 		@Override
 		public void run() {
 			while (true) {
 				//First send your table if you need to.
 				multicastMessages();
 				//Then process 
 				receiveFromQueue();
 			}
 		}
 		
 		/**
 		 * Send node's table to other linked nodes in the network.
 		 */
 		public synchronized void multicastMessages() {
 			if (multicastTable) {
 				for (Node node : linkedNodes) {
 					try {
 						//Record the sending command in the output file.
 						out.write("send "+nodeName+" "+node.getNodeName()+" "+constructTableString()+"\n");
 					} catch (IOException e) {
 						System.err.println("Could not write to output file: " + e.getMessage());
 						System.exit(1);
 					}
 					//Add the sending message to the received messsages queue of the receiving node.
 					node.receiveQueue.add(getNodeName()+"-"+constructTableString());
 				}
 
 				//Sent table to all the links. Don't send anymore.
 				multicastTable = false;
 			}
 		}
 		
 		/**
 		 * Check if there are any more received messages to process and if there are - do so.<br>
 		 * Contains the main logic behind routing algorithm.
 		 */
 		public synchronized void receiveFromQueue() {
 			if (receiveQueue.size() != 0) {
 				//Pop the first message in the queue and parse the message.
 				String msg = receiveQueue.remove();
 				String[] msgTokens = msg.split("-");
 				String senderNodeName = msgTokens[0];
 				String sentTable = msgTokens[1];
 				//Reconstruct the table object from string representation.
 				ArrayList<TableRow> newTable = reconstructTable(sentTable);
 				
 				//Create a representation of the current table before updating it with received table.
 				String oldTableString = constructTableString();
 				
 				//MAIN ROUTNG ALGORITHM LOGIC.
 				for (TableRow receivedRow : newTable) {
 					TableRow findRow = findAddress(receivedRow.address);
 					if (findRow == null) {
 						table.add(new TableRow(receivedRow.address, senderNodeName, receivedRow.cost+1));
 					} else if (receivedRow.cost+1 < findRow.cost) {
 						table.remove(findRow);
 						table.add(new TableRow(receivedRow.address, senderNodeName, receivedRow.cost+1));
 					} else if (findRow.link.equals(senderNodeName) && (receivedRow.cost+1 != findRow.cost) ) {
 						table.remove(findRow);
 						table.add(new TableRow(receivedRow.address, senderNodeName, receivedRow.cost+1));
 					}
 				}
 				
 				try {
 					//Record in the output file that message was received.
 					out.write("receive "+senderNodeName+" "+getNodeName()+" "+constructTableString(newTable)+"\n");
 				} catch (IOException e) {
 					System.err.println("Could not write to output file: " + e.getMessage());
 					System.exit(1);
 				}
 				
 				//Create representation of the updated table and if it's different than the one before
 				//processing the received message - set appropriate values for tracking variables.
 				String newTableString = constructTableString();
 				if (oldTableString.equals(newTableString)) {
 					tableChanged = false;
 				}
 				else {
 					tableChanged = true;
 					multicastTable = true;
 				}
 			}
 		}
 		
 		/**
 		 * Returns the row of the routing table that contains specified address.
 		 */
 		public TableRow findAddress(int address) {
 			for (TableRow row : table) {
 				if (row.address == address) return row;
 			}
 			return null;
 		}
 		
 		/**
 		 * Reconstructs a table object from a given string representation of the table.
 		 * @return ArrayList of TableRow objects representing the specified table.
 		 */
 		public ArrayList<TableRow> reconstructTable(String sentTable) {
 			ArrayList<TableRow> newTable = new ArrayList<TableRow>();
 			String[] tokens = sentTable.split(" ");
 			for (String token : tokens) {
 				String sub = token.substring(1, token.length()-1);
 				String[] details = sub.split("\\|");
 				newTable.add(new TableRow(Integer.parseInt(details[0]), details[1], Integer.parseInt(details[2])));
 			}
 			
 			return newTable;
 		}
 		
 		/**
 		 * Constructs a string representing the given table.
 		 * @return String representation of the routing table.	
 		 */
 		public String constructTableString(ArrayList<TableRow> givenTable) {
 			String tableString = "";
 			boolean firstRow = true;
 			for (TableRow row : givenTable) {
 				if (!firstRow) tableString +=" ";
 				tableString += "("+row.address+"|"+row.link+"|"+row.cost+")";
 				firstRow = false;
 			}
 			
 			return tableString;
 		}
 		
 		/**
 		 * Constructs a string representing the current table for the node.
 		 * @return String representation of the routing table.	
 		 */
 		public String constructTableString() {
 			String tableString = "";
 			boolean firstRow = true;
 			for (TableRow row : table) {
 				if (!firstRow) tableString +=" ";
 				tableString += "("+row.address+"|"+row.link+"|"+row.cost+")";
 				firstRow = false;
 			}
 			
 			return tableString;
 		}
 		
 		public boolean hasTableChanged() {
 			return tableChanged;
 		}
 		public String getNodeName() {
 			return nodeName;
 		}
 	}
 	
 	/**
 	 * Custom class which represents one row in a routing table. It has three fields:
 	 * <b>address</b>, <b>link</b> name and <b>cost</b> to reach that link.
 	 * @author Paulius Bernotas, s0943933 <br>
 	 * University of Edinburgh
 	 *
 	 */
 	private class TableRow {
 		int address;
 		String link;
 		int cost;
 		
 		TableRow(int address, String link, int cost) {
 			this.address = address;
 			this.link = link;
 			this.cost = cost;
 		}
 	}
 }
