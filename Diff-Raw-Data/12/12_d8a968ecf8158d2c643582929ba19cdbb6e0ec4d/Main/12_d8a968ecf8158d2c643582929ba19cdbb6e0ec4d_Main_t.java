 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package org.jbserver.main;
 
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Properties;
 import java.util.Timer;
 import javax.xml.parsers.ParserConfigurationException;
 import org.jbserver.main.feeddtypes.DrieFM;
 import org.jbserver.main.feeddtypes.StudioUpstairs;
 import org.jbserver.main.feeddtypes.Test;
 import org.jbserver.main.feeddtypes.Veronica;
 import org.jbserver.pircbot.IrcException;
 import org.xml.sax.SAXException;
 
 /**
  *
  * @author Jurgen
  */
 public class Main {
     
     public static Boolean debug = true;
    public static Boolean disablepost = false;
     
     public static Boolean isDebug() {
         return Main.debug;
     }
     
     public static Boolean disable() {
         return Main.disablepost;
     }
         
     public static void main(String[] args) throws IOException, IrcException {
         Properties properties = new Properties();
         try {
             properties.load(new FileInputStream("./config.cfg"));
         } catch (Exception ex) {
             ex.printStackTrace();
         }
         Umbrella umbrella = new Umbrella();
         umbrella.initTimers();        
         umbrella.createBot("test"); AppelBot bot = umbrella.getBot("test");
         bot.setVerbose(true);
        if((properties.getProperty("isbnc") != null) && (Boolean.parseBoolean(properties.getProperty("isbnc")) != true)) {
             bot.connect(properties.getProperty("irchost"), 
                     Integer.parseInt(properties.getProperty("ircport")));
         } else {
             bot.connect(properties.getProperty("irchost"), 
                     Integer.parseInt(properties.getProperty("ircport")),
                     properties.getProperty("bncpassword"));
         }
     }    
 }
