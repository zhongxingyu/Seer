 package Entity;
 
 import java.util.Date;
 import java.sql.Timestamp;
 
 // represents a matched bid and ask
 public class MatchedTransaction {
 
     private int transactionId;
     private Bid bid;
     private Ask ask;
     private Date date;
     private int price;
     private boolean sent;
     
     // constructor for new match
     public MatchedTransaction(Bid b, Ask a, Date d, int p) {
         this.bid = b;
         this.ask = a;
         this.date = d;
         this.price = p;
         this.sent = false;
     }
 
     // constructor for existing match
     public MatchedTransaction(int transactionId, int bidID, int askID, long date, int price, boolean sent) {
         this.transactionId = transactionId;
         this.bid = new Bid();
        bid.setBidId(price);
         this.ask = new Ask();
         ask.setAskId(askID);
         this.date = new Date(date);
         this.price = price;
         this.sent = sent;
     }
     
     // getters
     public int getTransactionId() {
         return transactionId;
     }
 
     public int getBidID() {
         return bid.getBidId();
     }
 
     public String getBuyerId() {
         return bid.getUserId();
     }
 
     public int getAskID() {
         return ask.getAskId();
     }
 
     public String getSellerId() {
         return ask.getUserId();
     }
 
     public String getStock() {
         return bid.getStock();  //or ask.getStock()
     }
 
     public int getPrice() {
         return price;
     }
 
     public Date getDate() {
         return date;
     }
     
     public boolean getSentToBackOffice(){
         return sent;
     }
 
     //setters
     public void setBid(Bid bid) {
         this.bid = bid;
     }
 
     public void setAsk(Ask ask) {
         this.ask = ask;
     }
     
     public void setTransactionId(int transactionId) {
         this.transactionId = transactionId;
     }
     
     public void setSendToBackOffice(boolean s){
         this.sent = s;
     }
     
     @Override
     public String toString() {
         return "stock:"+bid.getStock()+", amt:"+price+", bidder userId:"+bid.getUserId()+", seller userId:"+ask.getUserId()+", date:" + getDate();
     }
 }
