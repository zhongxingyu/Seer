 package com.progress.hudson;
 
 import java.util.Calendar;
 import java.util.Date;
 
 import hudson.model.Item;
 /**
  * Keeps the status of one job/project 
  * @author Stefan Fritz <sfritz@progress.com>
  *
  */
 public class BuildItem{
   private Item item=null;
   
   private Integer retriesLeft =-1; //forever by default
   private Long nextInterval =0L;
   private Long interval=0L;
   
   BuildItem(Item item, Integer interval, Integer maxRetries ){
     setItem(item);
     setInterval(interval * 1000L * 60L); //in minutes
     
     setNextInterval( System.currentTimeMillis() + getInterval());
     if(maxRetries>0){
       setRetriesLeft(maxRetries);
     }      
   }
   
   public boolean readyForBuild(){
     boolean result =false;
     // only build if next build time/interval passed and retries are left
     Integer left = getRetriesLeft();
     if(left!=0){
       Long now=System.currentTimeMillis();
       Long next=getNextInterval();
       String sNow=new Date(now).toString();
       String sNext=new Date(next).toString();
       
       if(now >= next){
         // rebuild is ok
         result=true;
         if(left>0){
           //decrement retries
           setRetriesLeft(--left);
         }
         setNextInterval(now + getInterval());
       }
     }
     return result;      
   }
   
   public String getUniqueID(){
     return item.getFullName();      
   }
 
   
   
   private Item getItem() {
     return item;
   }
 
   private void setItem(Item item) {
     this.item = item;
   }
 
   private Integer getRetriesLeft() {
     return retriesLeft;
   }
 
   private void setRetriesLeft(Integer retriesLeft) {
    if(this.retriesLeft!=-1){
       //only set if not infinite (-1)
       this.retriesLeft = retriesLeft;
    }
   }
 
   private Long getNextInterval() {
     return nextInterval;
   }
 
   private void setNextInterval(Long nextInterval) {
     this.nextInterval = nextInterval;
   }
   private Long getInterval() {
     return interval;
   }
   private void setInterval(Long interval) {
     this.interval = interval;
   }
 }
