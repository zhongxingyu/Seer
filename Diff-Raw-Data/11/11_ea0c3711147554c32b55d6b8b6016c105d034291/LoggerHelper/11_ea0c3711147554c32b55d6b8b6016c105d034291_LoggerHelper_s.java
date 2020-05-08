 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.slobodastudio.loger;
 
 import java.io.IOException;
 import java.util.Hashtable;
 import java.util.logging.FileHandler;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  *
  * @author Andrey Polyakov
  * @version 1.0
  */
 public class LoggerHelper {
 
     Hashtable<String, Logger> tableLoger = new Hashtable<String, Logger>();
    
     public static Logger createFileLoger(String nameLogger, String nameFile) {
         assert (nameLogger != null) : "Name of logger is empty";
         assert (nameFile != null) : "Name of file is empty";
        
         Logger log = null;
         try {
             FileHandler hand = new FileHandler(nameFile);
            log = Logger.getLogger(nameFile);
             log.addHandler(hand);
         } catch (IOException ex) {
             Logger.getLogger(LoggerHelper.class.getName()).
                     log(Level.SEVERE, null, ex);
             ex.printStackTrace(System.err);
         } catch (SecurityException ex) {
             ex.printStackTrace(System.err);
         }
         return log;
     }
 }
