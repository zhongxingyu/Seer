 package simulator;
 import java.util.*;
 
 /* This class manages the order books for the simulator.
  * The order books consist of two lists of orders, one for
  * Ask orders and one for Bid orders.
  * All orders are for a single instrument.
  */
 
 public class OrderBooks {
     
 	/*
 	 * The bidList and askList will both be ordered by price/time
      */
     private ArrayList<Order> bidList = new ArrayList<Order>();
     private ArrayList<Order> askList = new ArrayList<Order>();
     
     /*
      * Inserts/processes a single order
      * 
      * Parameters:
      * o - The order which is to be processed
      */
     public void processOrder(Order o) throws UnsupportedOperationException {
     	
     	if ( o.recordType().equals("ENTER") ) {
         	enterOrder(o);
         } else if ( o.recordType().equals("DELETE") ) {
     		deleteOrder(o);
         } else if ( o.recordType().equals("AMEND") ) {
     		amendOrder(o);
         } else if ( o.recordType().equals("TRADE") ) {
         	//should never reach here delete when done
     		trade(o);
         } else if ( o.recordType().equals("OFFTR") ) {
         	//should never reach here delete when done
         	offTrade(o);
         } else if ( o.recordType().equals("CANCEL_TRADE") ) {
         	//should never reach here delete when done
         	cancelTrade(o);
     	} else {
     		// Record Type is unsupported, Sirca data should never reach here
     		throw new UnsupportedOperationException();
     	}
     }
     
     // Returns the lowest ask (sell) Order
     public Order bestAskOrder() {
         return askList.get(0);
     }
     // Returns the highest bid (buy) Order
     public Order bestBidOrder() {
         return bidList.get(0);
     }
     // Returns the current spread
     public double spread() {
         return bestBidPrice() - bestAskPrice();
     }
     // Returns the lowest ask (sell) price
     public double bestAskPrice() {
         return askList.get(0).price();
     }
     // Returns the highest bid (buy) price
     public double bestBidPrice() {
         return bidList.get(0).price();
     }
     
     // Returns the size of the bid list
     public int bidListSize() {
         return bidList.size();
     }
     
     // Returns the size of the ask list
     public int askListSize() {
         return askList.size();
     }
     
     
     /*
      * Delete an order from the order books
      * 
      * Paramaters:
      * o - The DELETE order to be processed
      * 
      * Returns:
      * true if a matching order was found and deleted from the order book
      * false otherwise
      */
     boolean deleteOrder(Order o) {
         if ( o.bidAsk().equals("B") ) {
         	bidList.remove( findByTransactionID( bidList, o.transactionID() ) );
         	return true;
         } else if ( o.bidAsk().equals("A") ) {
         	askList.remove( findByTransactionID( askList, o.transactionID() ) );
         	return true;
         } else {
             return false;
         }
     }
     
     /*
      * Delete an order from the order books
      * 
      * Paramaters:
      * o - The DELETE order to be processed
      * 
      * Returns:
      * true if a matching order was found and deleted from the order book
      * false otherwise
      */
     boolean updateVolume(Order o, double newVolume) {
         if ( o.bidAsk().equals("B") ) {
         	bidList.remove( findByTransactionID( bidList, o.transactionID() ) );
         	return true;
         } else if ( o.bidAsk().equals("A") ) {
         	askList.remove( findByTransactionID( askList, o.transactionID() ) );
         	return true;
         } else {
             return false;
         }
     }
     
     /* Private Methods */
 
     // Takes a List of Orders and returns a deep copy
     private ArrayList<Order> copyList(List<Order> list) {
         ArrayList<Order> copy = new ArrayList<Order>();
         for (Order o : list) {
             copy.add( new Order(o) ); // Add a copy of the order
         }
         return copy;
     }
 
     /* Massive amount of almost duplicate code
      * Needs to be refactored */
     private void enterOrder(Order o) {
 		if (o.bidAsk().equals("B")) {
             for (int i = 0; i < bidList.size(); i++) {
        		if ( o.price() > bidList.get(i).price() ) {
         			bidList.add(i, o);
         		    return;
                 }
             }
                 bidList.add(o); // Insert at end of list
                 return;
         } else {
             for (int i = 0; i < askList.size(); i++) {
            	if ( o.price() < askList.get(i).price() ) {
             		askList.add(i, o);
             	    return;
                 }
             }
             askList.add(o); // Insert at end of list
             return;
         }
     }
 
 	private void amendOrder(Order o) {
 
 	}
 
     private void trade(Order o) {
         
     }
     
     private void offTrade(Order o) {
         
     }
     
     private void cancelTrade(Order o) {
         
     }
  
     /*
      * Returns the index of the first occurrence of the element in the list which has
      * a transaction ID equal to the given transaction ID
      * 
      * Parameters:
      * list - The list to be searched
      * transactionID - the transactionID of the order being searched for
      * 
      * Returns:
      * The index of the first occurrence of the element with a matching transaction ID
      * OR -1 if no matching order was found
      */
     private int findByTransactionID(List<Order> list, long transactionID) {
     	for (int i = 0; i < list.size(); i++) {
     		if (list.get(i).transactionID() == transactionID) {
     			return i;
     		}
     	}
     	return -1;
     }
 }
 
