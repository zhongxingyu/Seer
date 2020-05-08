 package com.example.prototype.applet;
 
 import java.awt.*;
 import java.util.Properties;
 
 /*
   Model config after the html applet tag, ex:
 
 <APPLET
   codebase="https://download.ongamenetwork.com/games/233/W3XA"
   height="564" width="988"
   code="Launcher.class"
   archive="jpc5_obfuscated_3138.jar"
 />
 
 applet,codebase=https://download.ongamenetwork.com/games/233/W3XA,height=564,width=988,code=Launcher.class,archive=jpc5_obfuscated_3138.jar
 
  */
 
 
 public class AppletConfig {
 
     private Properties prp;
     private static final String CODE = "code";
     private static final String CODEBASE = "codebase";
     private static final String ARCHIVE = "archive";
     private static final String WIDTH = "width";
     private static final String HEIGHT = "height";
 
     public AppletConfig(Properties prp) {
         this.prp = prp;
         assert prp.get(CODE) != null;
         assert prp.get(CODEBASE) != null;
     }
 
     public String code() {
         return (String) prp.get(CODE);
     }
 
     public String url() {
        StringBuffer sb = new StringBuffer();
         sb.append((String) prp.get(CODEBASE));
         Object value = prp.get(ARCHIVE);
         if (value != null) {
             sb.append("/");
             sb.append((String) value);
         }
         return sb.toString();
     }
 
     int valueToInt(Object value, int defaultValue) {
         int reply = defaultValue;
         if (value != null && value instanceof String) {
             try {
                 reply = Integer.valueOf((String) value);
             } catch (NumberFormatException ex) {
             }
         }
         return reply;
     }
 
     Dimension dimension() {
         int width = valueToInt(prp.get(WIDTH), 200);
         int height = valueToInt(prp.get(HEIGHT), 200);
         return new Dimension(width, height);
     }
 }
