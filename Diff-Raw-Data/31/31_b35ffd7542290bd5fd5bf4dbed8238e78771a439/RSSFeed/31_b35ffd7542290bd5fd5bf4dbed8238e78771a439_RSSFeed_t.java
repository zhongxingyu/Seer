 package com.gnuton.newshub.types;
 
import com.gnuton.newshub.EntryListAdapter;

 import java.io.Serializable;
 import java.util.Calendar;
 import java.util.List;
 
 /**
  * Created by gnuton on 5/25/13.
  */
 public class RSSFeed implements Serializable {
    // permanent vars: saved into DB
     public final int id;
     public final String title;
     public final String url;

    // temporary vars: not saved into DB
    public EntryListAdapter adapter;
     public String xml;
     public List entries;
    public Calendar lastUpdate;
 
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
