 package student42331580;
 
 import java.io.*;
 import java.util.StringTokenizer;
 
 import com.sun.corba.se.spi.orbutil.fsm.Input;
 import com.sun.org.apache.xpath.internal.operations.Variable;
 import com.sun.org.apache.xpath.internal.operations.VariableSafeAbsRef;
 import datastructures.*;
 
 import javax.sound.sampled.Line;
 
 public class Agent implements IAgent {
 
 	private ILinkedList<Stock> buyOrders;
 	private ILinkedList<Stock> sellOrders;
 	private ILinkedList<Stock> transactions;
 
 	/*
 	 * Default constructor
 	 */
 	public Agent() {
 		// You may choose which data structures you would like to use
 
 		this.buyOrders = new LinkedList<Stock>();
 		this.sellOrders = new LinkedList<Stock>();
 		this.transactions = new LinkedList<Stock>();
 	}
 
 	/*
 	 * Takes a file name as input and parses the commands in the file
 	 */
 	public int parseInput(String fileName) {
 
         try {
             FileInputStream fstream = new FileInputStream(fileName);
             DataInputStream dstream = new DataInputStream(fstream);
             InputStreamReader isreader = new InputStreamReader(dstream);
             BufferedReader file = new BufferedReader(isreader);
 
             //Parse each line in the file add add the parameters to the appropriate Queue
             String line;
             int LineNumber = 0;
             while ((line = file.readLine())  != null) {
 
                 if (line != "") { //Skip Line if Empty
                     String args[] = line.split(" ");
 
                     //Parse the Input for Syntax Errors
                     String transactionType = args[0];
                     String stockName = args[1];
                     int quantity = Integer.parseInt(args[2]);
                    double price = Double.parseDouble(args[3].substring(1,args[3].length()));
 
                     Stock stock = new Stock(stockName, quantity, price);
 
                     LineNumber++;
                 }
             }
 
             System.out.println(dstream);
 
         }
         catch (FileNotFoundException e){//Catch exception if any
             System.err.println("File Not Found: " + e.getMessage());
         }
         catch (IOException e) {
             System.err.println("IO Error: " + e.getMessage());
         }
 
 
 		return 0; // To prevent an error in the project
 	}
 
 	/*
 	 * Tries to match buy and sell orders. See assignment spec for more detail.
 	 */
 	public void exchange() {
 		// Implement this method
 	}
 
 	/*
 	 * Returns a string of the buy and sell orders. See assignment spec for more
 	 * detail.
 	 */
 	public String printQueues() {
 		// Implement this method
 		return ""; // To prevent an error in the project
 	}
 
 	/*
 	 * Returns a string of the transactions. See assignment spec for more
 	 * detail.
 	 */
 	public String printTransactions() {
 		// Implement this method
 		return ""; // To prevent an error in the project
 
 	}
 
 	public int sizeSell() {
 		return this.sellOrders.size();
 	}
 
 	public int sizeBuy() {
 		return this.buyOrders.size();
 	}
 
 	public int sizeTransaction() {
 		return this.transactions.size();
 	}
 
 }
