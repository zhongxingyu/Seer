 package org.tak;
 
 import org.tak.commons.Definable;
 import org.tak.data.Mode;
 import org.tak.util.XMLParser;
 
 import java.util.List;
 
 /**
  * User: Tommy
  * 6/2/13
  */
 public class Server {
    private static final Mode            MODE       = Mode.NPC;
     private static final List<Definable> DEFINABLES = XMLParser.parse(MODE);
 
     public static List<Definable> getIdentifiables() {
         return DEFINABLES;
     }
 
     public static Mode getMode() {
         return MODE;
     }
 }
