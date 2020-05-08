 //Made by Timothy Denisenko.
 
 package Pricer;
 
 import java.util.Scanner;
 
 /*
  * This class reads thousands of orders from the file "pricer.in" and adds them to the Order Book.
  */
 
 public class ManualTest {
 
 	public static void main(String[] args) throws InterruptedException {
 		String a;
 
 		Scanner inp = new Scanner(System.in);
 		try {
 			while ((a = inp.nextLine()) != null) {
 				OrderBook o = new OrderBook(a);
				//Thread.sleep(1);
 			}
 		} finally {
 			inp.close();
 		}
 		System.out.println("Done!\nTotal orders processed: "
 				+ OrderBook.ORDERBOOK_COUNT);
 	}
 }
