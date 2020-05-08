 package me.nrubin29.skypeapi;
 
 import java.awt.*;
 import java.net.URI;
 
 public class SkypeAPI {
 
     protected static void openURI(String uri) {
         try { Desktop.getDesktop().browse(new URI("skype:" + uri)); }
         catch (Exception e) { e.printStackTrace(); }
     }
 
    public static void open() {
        openURI("");
    }

     public static Group createGroup(String topic, String... people) {
         return new Group(topic, people);
     }
 
     public static User getUser(String name) {
         return new User(name);
     }
 }
