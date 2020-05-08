 package uk.ac.cam.md481.fjava.tick2;
 
 import java.util.Date;
 import java.text.SimpleDateFormat;
 
 abstract class SystemMessage {
   protected Date time;
   protected String from;
   protected String text;
   
   private String getTimeStamp(){
    return new SimpleDateFormat("HH:mm:ss").format(this.time);
   }
   
   public String getMessage(){
     return getTimeStamp() + " [" + this.from + "] " + this.text;
   }
   
   public String toString(){
     return getMessage();
   }
   
   public void print(){
     System.out.println(this);
   }
 }
