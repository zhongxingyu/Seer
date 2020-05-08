 package org.sukrupa.bigneeds;
 
 import org.sukrupa.needs.Need;
 import org.sukrupa.platform.RequiredByFramework;
 
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.Id;
 
 @Entity
 public class BigNeed extends Need {
 
     @Id
     @GeneratedValue
     private long id;
 
     @Column(name = "ITEM_NAME")
     private String itemName;
 
     @Column(name = "COST")
     private double cost;
 
     @Column(name = "PRIORITY")
     private int priority;
 
     @Column(name = "AMOUNT_DONATED")
     private double donatedAmount;
 
     @Column(name = "FULFILLED")
    private boolean fulfilled = false;
 
     @RequiredByFramework
     public BigNeed() {
     }
 
     public BigNeed(String itemName, double cost) {
         this.itemName = itemName;
         this.cost = cost;
     }
 
     public BigNeed(String itemName, double cost,int priority) {
         this(itemName, cost);
         this.priority=priority;
     }
 
     public BigNeed(int id, String itemName, double cost, int priority) {
         this(itemName, cost, priority);
         this.id = id;
     }
 
     public BigNeed(String itemName, double cost, int priority, double donatedAmount) {
         this(itemName, cost, priority);
         this.donatedAmount = donatedAmount;
     }
 
     public String getItemName() {
         return itemName;
     }
 
     @Override
     public double getCost() {
         return cost;
     }
 
     @Override
     public long getId() {
         return id;
     }
 
     @Override
     public int getPriority(){
         return priority;
     }
 
 
     public boolean equals(BigNeed object){
         return itemName.equals(object.getItemName()) && (cost == object.getCost());
     }
 
     public int hashCode(){
         return itemName.hashCode();
     }
 
     @Override
     public void setItemName(String itemName){
         this.itemName = itemName;
     }
 
     @Override
     public void setCost(double cost){
         this.cost = cost;
     }
 
     @Override
     public void setPriority(int priority){
         this.priority=priority;
     }
 
 
     public double getDonatedAmount() {
         return donatedAmount;
     }
 
     public void setDonatedAmount(double donatedAmount) {
         this.donatedAmount = donatedAmount;
     }
 
     public boolean isFulfilled() {
         return fulfilled;
     }
 
     public void setFulfilled(boolean fulfilled) {
         this.fulfilled = fulfilled;
     }
 }
