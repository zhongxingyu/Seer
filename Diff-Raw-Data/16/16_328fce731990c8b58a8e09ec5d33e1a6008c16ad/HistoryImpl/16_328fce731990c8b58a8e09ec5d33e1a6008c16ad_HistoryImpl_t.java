 package org.xbrlapi.loader;
 
 import java.net.URI;
 
 import org.apache.log4j.Logger;
 
 public class HistoryImpl implements History {
 
     private final static Logger logger = Logger.getLogger(HistoryImpl.class);
     
     /**
      * @see History#addRecord(URI, String)
      */
     public void addRecord(URI uri, String identifier) {
        logger.debug(uri + " has identifier: " + identifier);
     }
 
 }
