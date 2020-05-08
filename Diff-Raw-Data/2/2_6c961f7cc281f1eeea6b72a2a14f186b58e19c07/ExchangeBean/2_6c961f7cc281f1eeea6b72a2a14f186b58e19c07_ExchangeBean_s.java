 package aa;
 
 import Database.*;
 import Entity.*;
 import concurrency.*;
 import java.io.*;
 import java.sql.Connection;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.*;
 import static java.util.Arrays.asList;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.Future;
 
 public class ExchangeBean {
 
     // location of log files - change if necessary
     private final String MATCH_LOG_FILE = "c:\\temp\\matched.log";
     private final String REJECTED_BUY_ORDERS_LOG_FILE = "c:\\temp\\rejected.log";
     // used to calculate remaining credit available for buyers
     private final int DAILY_CREDIT_LIMIT_FOR_BUYERS = 1000000;
     // used to keep track of all matched transactions (asks/bids) in the system
     // matchedTransactions is cleaned once the records are written to the log file successfully
     private ArrayList<MatchedTransaction> matchedTransactions = new ArrayList<MatchedTransaction>();
     // keeps track of the latest price for each of the 3 stocks
     private int latestPriceForSmu = -1;
     private int latestPriceForNus = -1;
     private int latestPriceForNtu = -1;
 
     // this method is called once at the end of each trading day. It can be called manually, or by a timed daemon
     // this is a good chance to "clean up" everything to get ready for the next trading day
     public void endTradingDay() throws SQLException {
 
         Connection conn = null;
         Statement st = null;
 
         boolean okay = false;
         
         while(!okay){
         
             try {
 
                 conn = ConnectionFactory.getInstance().getConnection();
                 conn.setAutoCommit(false);
 
                 //TODO: make concurrent
 
                 //lock bid, ask and trader tables for writing
                 st = conn.createStatement();
                 st.execute("LOCK TABLES bid WRITE, ask WRITE, trader WRITE");
 
                 // dump all unfulfilled buy and sell orders
                 BidDAO.clearUnfulfilledBids(conn);
                 AskDAO.clearUnfulfilledAsks(conn);
 
                 // reset all credit limits of users
                 TraderDAO.resetCreditsForAllTraders(conn);
 
                 // reset attributes
                 latestPriceForSmu = -1;
                 latestPriceForNus = -1;
                 latestPriceForNtu = -1;
 
                 //commit changes and release locks
                 conn.commit();
                 okay = true;
                 
             } catch (SQLException e) {
 
                 //error! rollback!
                 if(conn!=null) conn.rollback();
                 System.err.println(e.getMessage());
                 
             } finally {
 
                 if (conn != null) {
                     conn.close();
                 }
                 if (st != null) {
                     st.close();
                 }
 
             }
         }
 
     }
 
     // returns a String of unfulfilled bids for a particular stock
     // returns an empty string if no such bid
     // bids are separated by <br> for display on HTML page
     public String getUnfulfilledBidsForDisplay(String stock) throws SQLException {
 
         Connection conn = null;
         
         boolean okay = false;
         while(!okay){
             
             try {
 
                 conn = ConnectionFactory.getInstance().getConnection();
                 ArrayList<Bid> unfulfilledBids = BidDAO.getUnfulfilledBidsForStock(conn, stock);
                 StringBuilder returnString = new StringBuilder();
                 for (Bid bid : unfulfilledBids) {
                     returnString.append(bid.toString());
                     returnString.append("<br />");
                 }
                 okay = true;
                 return returnString.toString();
 
             } catch (SQLException e) {
 
                 e.printStackTrace();
                 return "Sorry, we are unable to retrieve the unfulfilled bids for you now. :(";
 
             } finally {
 
                 //release connection
                 if (conn != null) {
                     conn.close();
                 }
 
             }
             
         }
         return "";
 
     }
 
     // returns a String of unfulfilled asks for a particular stock
     // returns an empty string if no such ask
     // asks are separated by <br> for display on HTML page
     public String getUnfulfilledAsks(String stock) throws SQLException {
 
         Connection conn = null;
         
         boolean okay = false;
         while (!okay){
             try {
 
                 conn = ConnectionFactory.getInstance().getConnection();
                 ArrayList<Ask> unfulfilledAsks = AskDAO.getUnfulfilledAsksForStock(conn, stock);
 
                 StringBuilder returnString = new StringBuilder();
                 for (Ask ask : unfulfilledAsks) {
                     returnString.append(ask.toString());
                     returnString.append("<br />");
                 }
                 
                 okay = true;
                 return returnString.toString();
 
             } catch (SQLException e) {
 
                 e.printStackTrace();
                 return "Sorry, we are unable to retrieve the unfulfilled asks for you now. :(";
 
             } finally {
 
                 //release connection
                 if (conn != null) {
                     conn.close();
                 }
 
             }
         }
         
         return "";
 
     }
 
     // returns the highest bid for a particular stock
     // returns -1 if there is no bid at all
     public int getHighestBidPrice(String stock) throws SQLException {
         Bid highestBid = getHighestBid(stock);
         if (highestBid == null) {
             return -1;
         } else {
             return highestBid.getPrice();
         }
     }
 
     // retrieve unfulfiled current (highest) bid for a particular stock
     // returns null if there is no unfulfiled bid for this stock
     public Bid getHighestBid(String stock) throws SQLException {
 
         Connection conn = null;
         boolean okay = false;
         while(!okay){
             
             try {
 
                 conn = ConnectionFactory.getInstance().getConnection();
                 okay = true;
                 return BidDAO.getHighestBidForStock(conn, stock);
 
             } catch (SQLException e) {
 
                 System.err.println(e.getMessage());
                 
             } finally {
 
                 if (conn != null) {
                     conn.close();
                 }
 
             }
             
         }
         
         return null;
 
     }
 
     // returns the lowest ask for a particular stock
     // returns -1 if there is no ask at all
     public int getLowestAskPrice(String stock) throws SQLException {
         Ask lowestAsk = getLowestAsk(stock);
         if (lowestAsk == null) {
             return -1;
         } else {
             return lowestAsk.getPrice();
         }
     }
 
     // retrieve unfulfiled current (lowest) ask for a particular stock
     // returns null if there is no unfulfiled asks for this stock
     public Ask getLowestAsk(String stock) throws SQLException {
 
         Connection conn = null;
         boolean okay = false;
         while(!okay){   //keep retrying
             try {
 
                 conn = ConnectionFactory.getInstance().getConnection();
                 okay = true;
                 return AskDAO.getLowestAskForStock(conn, stock);
 
             } catch (SQLException e) {
 
                throw e;    //pass back to caller to handle
 
             } finally {
 
                 if (conn != null) {
                     conn.close();
                 }
 
             }
         }
         return null;
 
     }
 
     // check if a buyer is eligible to place an order based on his credit limit
     // if he is eligible, this method adjusts his credit limit and returns true
     // if he is not eligible, this method logs the bid and returns false
     private boolean validateCreditLimit(Bid b) throws SQLException {
 
         //start transaction
         Connection conn = null;
         boolean okay = false;
         while(!okay){
             
             try {
 
                 conn = ConnectionFactory.getInstance().getConnection();
                 conn.setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);   //phantom reads okay, non-repeatable not allowed. we don't want credit value to be changed midway.
                 conn.setAutoCommit(false);
 
                 //get trader from database
                 Trader trader = TraderDAO.getTraderWithUsername(conn, b.getUserId());   //will be read locked.
 
                 // calculate the total price of this bid, each bid is for 1000 shares
                 int totalPriceOfBid = b.getPrice() * 1000;
 
                 //check if trader has sufficient credit
                 boolean sufficientCredit = false;
                 if (totalPriceOfBid > trader.getCredit()) {
 
                     //insufficient, log failure
                     logRejectedBuyOrder(b);
 
                 } else {
 
                     //sufficient, deduct credit and update database
                     trader.deductCredit(totalPriceOfBid);
                     TraderDAO.update(conn, trader);
                     sufficientCredit = true;
 
                 }
 
                 conn.commit();  //release locks
                 okay = true;
                 
                 return sufficientCredit;                
 
             } catch (SQLException e) {
 
                 System.err.println(e.getMessage());
                 
             } finally {
 
                 //release connection
                 conn.close();
 
             }
         }
         
         return false;
         
     }
 
     // call this to append all rejected buy orders to log file
     // TODO: Concurrency Lock
     private void logRejectedBuyOrder(Bid b) {
         try {
             PrintWriter outFile = new PrintWriter(new FileWriter(REJECTED_BUY_ORDERS_LOG_FILE, true));
             outFile.append(b.toString() + "\n");
             outFile.close();
         } catch (IOException e) {
             // Think about what should happen here...
             System.out.println("IO EXCEPTIOn: Cannot write to file");
             e.printStackTrace();
         } catch (Exception e) {
             // Think about what should happen here...
             System.out.println("EXCEPTION: Cannot write to file");
             e.printStackTrace();
         }
     }
 
     // call this to append all matched transactions in matchedTransactions to log file and clear matchedTransactions
     // TODO: Concurrency Lock
     private void logMatchedTransactions() {
         try {
             PrintWriter outFile = new PrintWriter(new FileWriter(MATCH_LOG_FILE, true));
             for (MatchedTransaction m : matchedTransactions) {
                 outFile.append(m.toString() + "\n");
             }
             matchedTransactions.clear(); // clean this out
             outFile.close();
         } catch (IOException e) {
             // Think about what should happen here...
             System.out.println("IO EXCEPTIOn: Cannot write to file");
             e.printStackTrace();
         } catch (Exception e) {
             // Think about what should happen here...
             System.out.println("EXCEPTION: Cannot write to file");
             e.printStackTrace();
         }
     }
 
     // returns a string of HTML table rows code containing the list of user IDs and their remaining credits
     // this method is used by viewOrders.jsp for debugging purposes
     public String getAllCreditRemainingForDisplay() throws SQLException {
 
         Connection conn = null;
         
         boolean okay = false;
         while(!okay){
             
             try {
 
                 conn = ConnectionFactory.getInstance().getConnection();
                 String returnString = "";
 
                 ArrayList<Trader> traders = TraderDAO.getAllTraders(conn);
 
                 for (Trader t : traders) {
                     returnString += "<tr><td>" + t.getUsername() + "</td><td>" + t.getCredit() + "</td></tr>";
                 }
 
                 okay = true;
                 return returnString;
 
             } catch (SQLException e) {
 
                 System.err.println(e.getMessage());
 
             } finally {
 
                 if (conn != null) {
                     conn.close();
                 }
 
             }
             
         }
         
         return "";
         
     }
 
     // call this method immediatley when a new bid (buying order) comes in
     // this method returns false if this buy order has been rejected because of a credit limit breach
     // it returns true if the bid has been successfully added
     public boolean placeNewBidAndAttemptMatch(Bid newBid) throws Exception {
 
         //check if buyer has enough credit
         boolean okToContinue = validateCreditLimit(newBid);
         if (!okToContinue) {
             return false;
         }
 
         //sufficient credits, add bid to database and check for match
         ExecutorService executor = Executors.newFixedThreadPool(2);
         List<Future<Object[]>> results = executor.invokeAll(asList(new AddEntityOperation(newBid), new CheckMatchOperation(newBid)));
         executor.shutdown();
 
         boolean isMatched = false;
         Ask lowestAsk = null;
         for (Future<Object[]> result : results) {
             Object[] resultArray = result.get();
             if (resultArray != null) {
                 lowestAsk = (Ask) resultArray[0];
                 isMatched = Boolean.parseBoolean((String) resultArray[1]);
             }
         }
 
         System.out.println("isMatched = " + isMatched);
 
         //matched, create transaction
         if (isMatched) {
 
             Connection conn = null;
             try {
 
                 conn = ConnectionFactory.getInstance().getConnection();
                 conn.setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);
                 conn.setAutoCommit(false);
 
                 //lock ask and bid for update
                 BidDAO.lockForUpdate(conn, newBid);
                 AskDAO.lockForUpdate(conn, lowestAsk);
 
                 //this is a buying transaction, so transaction price is price of ask
                 MatchedTransaction mt = new MatchedTransaction(newBid, lowestAsk, newBid.getDate(), lowestAsk.getPrice());
                 MatchedTransactionDAO.add(conn, mt);  //transaction id will be generated here
 
                 //TODO: log transaction, make concurrent
                 logMatchedTransactions();
 
                 //TODO: send to back office, make concurrent
 
 
                 //update latest price
                 updateLatestPrice(mt);
 
                 //update transaction id in bid and ask, and save to database
                 newBid.setTransactionId(mt.getTransactionId());
                 lowestAsk.setTransactionID(mt.getTransactionId());
                 BidDAO.update(conn, newBid);
                 AskDAO.update(conn, lowestAsk);
 
                 //finished transaction, release locks
                 conn.commit();
 
             } catch (SQLException e) {
 
                 e.printStackTrace();
 
                 //error! rollback.
                 if (conn != null) {
                     conn.rollback();
                 }
 
             } finally {
 
                 if (conn != null) {
                     conn.close();
                 }
 
             }
         }
 
         //acknowledge bid, even if match failed.
         return true;
     }
 
     // call this method immediatley when a new ask (selling order) comes in
     public void placeNewAskAndAttemptMatch(Ask newAsk) throws Exception {
 
         //add ask to database and check for match
         ExecutorService executor = Executors.newFixedThreadPool(2);
         List<Future<Object[]>> results = executor.invokeAll(asList(new AddEntityOperation(newAsk), new CheckMatchOperation(newAsk)));
         executor.shutdown();
 
         boolean isMatched = false;
         Bid highestBid = null;
 
         for (Future<Object[]> result : results) {
             Object[] resultArray = result.get();
             if (resultArray != null) {
                 highestBid = (Bid) resultArray[0];
                 isMatched = Boolean.parseBoolean((String) resultArray[1]);
             }
         }
 
         System.out.println("isMatched = " + isMatched);
 
         //matched:
         if (isMatched) {
 
             Connection conn = null;
 
             try {
 
                 conn = ConnectionFactory.getInstance().getConnection();
                 conn.setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);
                 conn.setAutoCommit(false);
 
                 //lock ask and bid for update
                 BidDAO.lockForUpdate(conn, highestBid);
                 AskDAO.lockForUpdate(conn, newAsk);
 
                 //create transaction and save in database
                 //this is a selling transaction, so transaction price is the price of bid
                 MatchedTransaction mt = new MatchedTransaction(highestBid, newAsk, newAsk.getDate(), highestBid.getPrice());
                 MatchedTransactionDAO.add(conn, mt);    //transactionID will also be generated at the same time.
 
                 //TODO: log transaction, make concurrent
                 logMatchedTransactions();
 
                 //TODO: send to back office, make concurrent
 
                 //update latest price
                 updateLatestPrice(mt);
 
                 //update transaction id in bid and ask, and save to database
                 highestBid.setTransactionId(mt.getTransactionId());
                 newAsk.setTransactionID(mt.getTransactionId());
                 BidDAO.update(conn, highestBid);
                 AskDAO.update(conn, newAsk);
 
                 //finished, release locks
                 conn.commit();
 
             } catch (SQLException e) {
 
                 e.printStackTrace();
 
                 if (conn != null) {
                     conn.rollback();
                 }
 
             } finally {
 
                 if (conn != null) {
                     conn.close();
                 }
 
             }
 
         }
 
 
     }
 
     // updates either latestPriceForSmu, latestPriceForNus or latestPriceForNtu
     // based on the MatchedTransaction object passed in
     // TODO: Concurrency Lock
     private void updateLatestPrice(MatchedTransaction m) {
         String stock = m.getStock();
         int price = m.getPrice();
         // update the correct attribute
         if (stock.equals("smu")) {
             latestPriceForSmu = price;
         } else if (stock.equals("nus")) {
             latestPriceForNus = price;
         } else if (stock.equals("ntu")) {
             latestPriceForNtu = price;
         }
     }
 
     // updates either latestPriceForSmu, latestPriceForNus or latestPriceForNtu
     // based on the MatchedTransaction object passed in
     // TODO: Concurrency Lock
     public int getLatestPrice(String stock) {
         if (stock.equals("smu")) {
             return latestPriceForSmu;
         } else if (stock.equals("nus")) {
             return latestPriceForNus;
         } else if (stock.equals("ntu")) {
             return latestPriceForNtu;
         }
         return -1; // no such stock
     }
 
     public boolean sendToBackOffice(String txnDescription) {
         aa.Service service = new aa.Service();
         boolean status = false;
 
         try {
             // create new instances of remote Service objects
             aa.ServiceSoap port = service.getServiceSoap();
 
             // invoke the remote method by calling port.processTransaction().
             // processTransaction() will return false if the teamID &/or password is wrong
             // it will return true if the web service is correctly called
             status = port.processTransaction("G3T7", "lime", txnDescription);
             return status;
         } catch (Exception ex) {
             // may come here if a time out or any other exception occurs
             // what should you do here??
         }
         return false; // failure due to exception
     }
 }
