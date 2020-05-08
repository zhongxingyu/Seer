 package com.gnuton.newshub.types;
 
 import java.io.Serializable;
 import java.util.Calendar;
 import java.util.List;
 
 /**
  * Created by gnuton on 5/25/13.
  */
 public class RSSFeed implements Serializable {
     public final int id;
     public final String title;
     public final String url;
    public Calendar lastUpdate;
     public String xml;
     public List entries;
 
     public RSSFeed(int id, String title, String url) {
         this.id = id;
         this.url = url;
         this.title = title;
     }
 
     // RSS no in DB
     public RSSFeed(String title, String url) {
         this.id = -1;
         this.url = url;
         this.title = title;
     }
 
     // Will be used by the ArrayAdapter in the ListView
     @Override
     public String toString() {
         return title;
     }
 }
