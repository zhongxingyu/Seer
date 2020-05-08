 package Server;
 
 import java.io.*;
 
 /**
  * Represents an auction item.
  * 
  */
 public class Item implements Serializable {
 
     private String ownerName;
     private String winnerName;
     private String itemName;
     private String itemDesc;
     private double startBid;
     private double maxBid;
     private double currentBid;
     private int auctionTime;
         
     public Item() {
         ownerName = "";
         winnerName = "";
         itemName = "";
         itemDesc = "";
         startBid = maxBid = currentBid = .0;
         auctionTime = 0;
     }
 
     public Item(String ownerName, String itemName, String itemDesc, double startBid, double maxBid, int auctionTime) {
         this.ownerName = ownerName;
         this.itemName = itemName;
         this.itemDesc = itemDesc;
         this.startBid = startBid;
         this.maxBid = maxBid;
         this.currentBid = startBid;
         this.auctionTime = auctionTime;
     }
 
     public void setOwnerName(String val) {
         this.ownerName = val;
     }
 
     public String getOwnerName() {
         return this.ownerName;
     }
     
     public void setWinnerName(String val) {
         this.winnerName = val;
     }
 
     public String getWinnerName() {
         return this.winnerName;
     }
 
     public void setItemName(String val) {
         this.itemName = val;
     }
 
     public String getItemName() {
         return this.itemName;
     }
 
     public void setItemDesc(String val) {
         this.itemDesc = val;
     }
 
     public String getItemDesc() {
         return this.itemDesc;
     }
 
     public void setStartBid(double val) {
         this.startBid = val;
     }
 
     public double getStartBid() {
         return this.startBid;
     }
     
     public void setMaxBid(double val) {
         this.maxBid = val;
     }
 
     public double getMaxBid() {
         return this.maxBid;
     }
     
     public void setCurrentBid(double val) {
         this.currentBid = val;
     }
 
     public double getCurrentBid() {
         return this.currentBid;
     }  
 
     public void setAuctionTime(int val) {
         this.auctionTime = val;
     }
 
     public int getAuctionTime() {
         return this.auctionTime;
     }
 }
