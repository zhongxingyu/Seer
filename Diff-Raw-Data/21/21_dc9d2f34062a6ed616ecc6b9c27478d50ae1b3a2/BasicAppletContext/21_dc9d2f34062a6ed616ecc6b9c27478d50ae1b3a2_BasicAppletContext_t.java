 package com.example.prototype.applet;
 
 import java.applet.Applet;
 import java.applet.AppletContext;
 import java.applet.AudioClip;
 import java.awt.*;
 import java.io.InputStream;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 
 class BasicAppletContext implements AppletContext {
 
     public AudioClip getAudioClip(URL url) {
         return Applet.newAudioClip(url);
     }
 
     public Image getImage(URL url) {
         Toolkit tk = Toolkit.getDefaultToolkit();
         return tk.getImage(url);
     }
 
     public Applet getApplet(String name) {
         assert false;
         return null;
     }
 
    public Enumeration<Applet> getApplets() {
         assert false;
         return NO_APPLETS;
     }
 
     public void showDocument(URL url) {
         System.out.println("Showing document " + url);
     }
 
     public void showDocument(URL url, String frame) {
         try {
             showDocument(new URL(url.toString() + frame));
         } catch (MalformedURLException e) {
         }
     }
 
     public void showStatus(String message) {
         System.out.println(message);
     }
 
     public InputStream getStream(String key) {
         return streamMap.get(key);
     }
 
     public Iterator<String> getStreamKeys() {
         return streamMap.keySet().iterator();
     }
 
     public void setStream(String key, InputStream stream) {
         if (stream == null) {
             streamMap.remove(key);
             return;
         }
         streamMap.put(key, stream);
     }
 
     private final Map<String, InputStream> streamMap = new HashMap<String, InputStream>();
 
     private static final Enumeration<Applet> NO_APPLETS = new Enumeration<Applet>() {
 
         public boolean hasMoreElements() {
             return false;
         }
 
         public Applet nextElement() {
             return null;
         }
     };
 }
