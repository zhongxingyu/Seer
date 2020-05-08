 /**
  * 
  */
 package net.lahwran.capsystem;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import net.lahwran.mcclient.capsystem.Sendable;
 /**
  * @author lahwran
  *
  */
 public class Capsystem {
     public static final int protocolVersion = 0;
     private static Caplist serverCaplist = null;
     private static final Caplist caplist = new Caplist(protocolVersion);
     public static final int maxLength=100;
     public static final String colorchar = "\u00a7";
     public static final String prefix = "/@caps ";
     public static Sendable server;
     
     public static boolean connected = false;
     public static boolean capableServer = false;
     
     public static void sendCaps()
     {
         StringBuilder toSend = null;
         for (Capability c:caplist.capabilities.values())
         {
             String curCap = c.toString();
             if(toSend == null)
             {
                 toSend = new StringBuilder(prefix);
                 if (toSend.length() + curCap.length() > maxLength)
                     throw new RuntimeException("Impossibly long capability error");
                 toSend.append(curCap);
                 continue;
             }
             else if(toSend.length() + curCap.length() + 1 > maxLength)
             {
                 System.out.println("Sending "+toSend.length()+": "+toSend.toString());
                 server.send(toSend.toString());
                 toSend=null;
             }
             else
             {
                 toSend.append(" "+curCap);
             }
         }
         if (toSend != null)
         {
             System.out.println("Sending: "+toSend.toString());
             server.send(toSend.toString());
         }
        server.send("/@caps done");
     }
 
     public static void registerCap(Capability c)
     {
         caplist.capabilities.put(c.type+c.name, c);
     }
 
     public static void registerCap(String strc)
     {
         Capability c = new Capability(strc);
         caplist.capabilities.put(c.type+c.name, c);
     }
 
     public static void unregisterCap(Capability c)
     {
         caplist.capabilities.remove(c.type+c.name);
     }
 
     public static void unregisterCap(String c)
     {
         caplist.capabilities.remove(c);
     }
 
     public static boolean hasCap(String c)
     {
         return serverCaplist != null && serverCaplist.capabilities.containsKey(c);
     }
 
     public static boolean hasCap(Capability c)
     {
         return serverCaplist != null && serverCaplist.capabilities.containsKey(c.type+c.name);
     }
 
     public static Capability getCap(String c)
     {
         return serverCaplist != null ? serverCaplist.capabilities.get(c) : null;
     }
 
     public static Caplist getCapList()
     {
         return serverCaplist;
     }
 
     public static final Pattern cappattern = Pattern.compile("^(.)([^:]*):?(.*)?$");
     /**
      * Add a capability that was sent by the server
      * @param cap
      */
     public static void _addCap(String cap)
     {
         Matcher match = cappattern.matcher(cap);
         if (!match.matches())
             throw new RuntimeException("String is not a capability: "+cap);
         String type = match.group(1);
         String name = match.group(2);
         String args = match.group(3);
         serverCaplist.capabilities.put(type+name, new Capability(type, name, args));
     }
 
     public static void _initCaps(int version)
     {
         serverCaplist = new Caplist(version);
         capableServer = true;
         sendCaps();
     }
 
     public static void _disconnected()
     {
         serverCaplist = null;
         server = null;
         connected = false;
         capableServer = false;
     }
 
     public static void _connected(Sendable server)
     {
         Capsystem.server = server;
         connected = true;
     }
 }
