 package replicated_calculator;
 import java.io.*;
 import java.math.*;
 import point_to_point_queue.*;
import week6.multicast.ProperConnectorClient;
 
 /**
  * 
  * Rudimentary implementation of a Textual User Interface for a 
  * DistributedCalculator client.
  * 
  * @author Jesper Buus Nielsen, Aarhus University, 2012.
  *
  */
 
 public class ClientTUI {
 	
     synchronized public static void report(String str) {
 	System.out.println(str);
     }
     
     public static void main(String[] args) {
 	/**
 	 * Here you would instantiation your own, more impressive client instead.
 	 */
	Client client = new ProperConnectorClient();
 	
 	try {
 	    // For reading from standard input
 	    BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));
 	    String s;
 	    
 	    report("<var> = <int>");
 	    report("<var> = <var> + <var>");
 	    report("<var> = <var> * <var>");
 	    report("<var> = <var> < <var>");
 	    report("Print <var>");
 	    report("Begin: Begin an atomic region.");
 	    report("End: Leave an atomic region.");
 	    report("Exit: Gracefully logs out");
 	    report("Crash: Makes the client crash.");
 	    report("");
 	    
 	    /*
 	     * Read the client identifier. 
 	     */
 	    String clientName = null;
 	    System.out.print("Login: ");
 	    if ((s = stdin.readLine()) != null) {
 		clientName = s;
 	    } else {
 		return;
 	    }
 	    
 	    /*
 	     * Get the address of the server.
 	     */
 	    String serverAddress = null;
 	    System.out.print("Enter address of a server: ");
 	    if ((s = stdin.readLine()) != null) {
 		serverAddress = s;
 	    } else {
 		return;
 	    }
 	    
 	    if (client.connect(serverAddress,clientName)) {
 		report("Connected");
 	    } else {
 		report("Connection failed!");
 		return;
 	    }
 	    
 	    while ((s = stdin.readLine()) != null) { 
 		if (s.equals("Crash")) {
 		    System.exit(-1);
 		} else if (s.equals("Exit")) {
 		    client.disconnect();
 		    break;
 		} else if (s.equals("Begin")) {
 		    client.beginAtomic();
 		} else if (s.equals("End")) {
 		    client.endAtomic();
 		} else if (s.startsWith("Print")) {
 		    final String var = s.substring(6).trim();
 		    client.read(var, new Callback<BigInteger>() { 
 			    public void result(BigInteger bi) {
 				report(var + ": " + bi);
 			    }});
 		} else {
 		    int equalityAt = s.indexOf("=");
 		    if (equalityAt == -1) {
 			System.out.println("Bad user!");
 		    } else {
 			String res = s.substring(0, equalityAt).trim();
 			String rest = s.substring(equalityAt+1).trim();
 			if (rest.matches("\\p{Digit}+")) {
 			    client.assign(res, new BigInteger(rest));
 			} else {
 			    int plusAt = rest.indexOf("+");
 			    if (plusAt != -1) {
 				String left = rest.substring(0, plusAt).trim();
 				String right = rest.substring(plusAt+1).trim();
 				client.add(left, right, res);
 			    } else {
 				int multAt = rest.indexOf("*");
 				if (multAt != -1) {
 				    String left = rest.substring(0, multAt).trim();
 				    String right = rest.substring(multAt+1).trim();
 				    client.mult(left, right, res);
 				} else {
 				    int compAt = rest.indexOf("<");
 				    if (compAt != -1) {
 					String left = rest.substring(0, compAt).trim();
 					String right = rest.substring(compAt+1).trim();
 					client.compare(left, right, res);
 				    } else {
 					System.out.println("Bad user!");
 				    }
 				}
 			    }
 			}
 		    }
 		}
 	    }
 	} catch (IOException e) {
 	    e.printStackTrace();
 	}
 	System.out.println("Shutting down!");
     }
 }
