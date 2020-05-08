 //Made by Timothy Denisenko.
 
 package Pricer;
 
 import java.io.BufferedReader;
 import java.io.FileReader;
 import java.io.IOException;
 
 /*
  * This class reads thousands of orders from the file "pricer.in" and adds them to the Order Book.
  */
 
 public class Test {
 	public static void main(String[] args) throws IOException,
 			InterruptedException {
 		String a;
 
 		BufferedReader br = new BufferedReader(new FileReader("Docs/pricer.in"));
 		try {
 			while ((a = br.readLine()) != null) {
 				// System.out.println(a + " ->");
 				String[] order = a.split(" ");
 				String b = "";
 				if(order[1].equals("A")) {
 					b += "T";
 				}
 				else if(order[1].equals("R")) {
 					b += "R";
 				}
 				for(int i = 2; i < order.length; i++) {
 					b += " " + order[i];
 				}
				
 				OrderBook o = new OrderBook(b);
 				Thread.sleep(1);
 			}
 		} finally {
 			br.close();
 		}
 		System.out.println("Done!\nTotal orders processed: "
 				+ OrderBook.ORDERBOOK_COUNT);
 	}
 }
