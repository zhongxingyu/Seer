 package com.ilopezluna.pages;
 
 import java.util.Date;
 
 import com.ilopezluna.entities.Video;
 import org.apache.tapestry5.annotations.*;
 import org.apache.tapestry5.hibernate.annotations.CommitAfter;
 import org.apache.tapestry5.ioc.annotations.*;
 import org.apache.tapestry5.corelib.components.*;
 import org.apache.tapestry5.SymbolConstants;
 import org.apache.tapestry5.alerts.AlertManager;
 import org.hibernate.Session;
 
 /**
  * Start page of application youtube-evidence.
  */
 public class Index
 {
     @Property
     @Inject
     @Symbol(SymbolConstants.TAPESTRY_VERSION)
     private String tapestryVersion;
 
     @Inject
     private AlertManager alertManager;
 
     public Date getCurrentTime()
     {
         return new Date();
     }
 
 }
