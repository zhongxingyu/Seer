 package com.ingemark.perftest;
 
 import static com.ingemark.perftest.Util.excToString;
 
 import java.io.Serializable;
 
 public class DialogInfo implements Serializable {
   public final String title, msg;
 
   public DialogInfo(LiveStats ls) {
     this.title = ls.name + " - Last Reported Exception";
     this.msg = Util.excToString(ls.lastException);
   }
  public DialogInfo(String title, Throwable exc) {
    this.title = title; this.msg = excToString(exc);
   }
 }
