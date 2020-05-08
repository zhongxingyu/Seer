 package com.github.etsai.kfsxtrackingserver;
 
 import groovy.sql.Sql;
 import java.io.PrintStream;
 import java.util.Properties;
 import java.util.Timer;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.logging.Logger;
 
 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 /**
  * Central variables used by all classes.
  * The objects must be thread safe
  * @author etsai
  */
 public class Common {
     public static final Logger logger= Logger.getLogger("KFSXTrackingServer");
     public static Properties properties;
     public static ExecutorService pool= Executors.newCachedThreadPool();
     public static Timer timer = new Timer();
    public static Sql sql;
     public static PrintStream oldStdOut, oldStdErr;
 }
