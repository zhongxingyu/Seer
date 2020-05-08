 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package xmppclient;
 
 import java.awt.Image;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.FilenameFilter;
 import java.util.Properties;
 import javax.swing.Icon;
 import javax.swing.ImageIcon;
 import org.jivesoftware.smack.RosterEntry;
 import org.jivesoftware.smack.XMPPException;
 import org.jivesoftware.smack.packet.Presence;
 import org.jivesoftware.smackx.packet.VCard;
 import xmppclient.images.Icons;
 
 /**
  * This class provides a number of general purpose utility methods
  * @author Lee
  */
 public class Utils 
 {
     public static String getExtension(File f) 
     {
         String ext = null;
         String s = f.getName();
         int i = s.lastIndexOf('.');
 
         if (i > 0 &&  i < s.length() - 1) {
             ext = s.substring(i+1).toLowerCase();
         }
         return ext;
     }
     
     public static Icon resizeImage(ImageIcon image, int height)
     {
         ImageIcon resizedImage = null;
         
         if (image != null) 
         {
             if (image.getIconHeight() > height) 
             {
                 resizedImage = new ImageIcon(image.getImage().
                     getScaledInstance(-1, height,
                     Image.SCALE_DEFAULT));
             } 
             else 
             { 
                 resizedImage = image;
             }
         }
         
         return resizedImage;
     }
     
     public static String getNickname(RosterEntry rosterEntry)
     {
         VCard vCard = new VCard();
         
         if(rosterEntry.getName() != null) return rosterEntry.getName();
         
         try
         {
             vCard.load(XMPPClientUI.connection, rosterEntry.getUser());
             if(vCard.getNickName() != null && !vCard.getNickName().equals(""))
                 return vCard.getNickName();
         }
         catch (Exception ex) {}
         
         if(rosterEntry.getUser() != null) return rosterEntry.getUser();
         
         return null;
     }
     
     public static String getStatusMessage(Presence presence)
     {
         if(presence.getStatus() != null)
         {
             return presence.getStatus();
         }
         else return null;
     }
     
     public static String getStatus(Presence presence)
     {
         if(!presence.isAvailable()) return "Offline";
         
         return "Available";
     }
     
     public static Icon getAvatar(RosterEntry rosterEntry, int height)
     {
         VCard vCard = new VCard();
         
         try
         {
             vCard.load(XMPPClientUI.connection, rosterEntry.getUser());
             return Utils.resizeImage(new ImageIcon(vCard.getAvatar()), height);
         }
         catch (XMPPException ex) 
         {
             System.err.printf("Error loading avatar for user %s: %s", rosterEntry.getUser(), ex.getMessage());
         }
         catch (NullPointerException ex)
         {
             // user has no avatar
         }
         
         return null;
     }
     
     public static Icon getAvatar(int height)
     {
         VCard vCard = new VCard();
         
         try
         {
             vCard.load(XMPPClientUI.connection);
             return Utils.resizeImage(new ImageIcon(vCard.getAvatar()), height);
         }
         catch (XMPPException ex) 
         {
         }
         catch (NullPointerException ex)
         {
             // user has no avatar
         }
         
         return null;
     }
     
     public static ImageIcon getUserIcon(Presence presence)
     {
         if(presence.getMode() == Presence.Mode.dnd)
         {
             return Icons.busy;
         }
         if(!presence.isAvailable())
         {
             return Icons.offline;
         }
         if(presence.isAway())
         {
             return Icons.away;
         }
 
         return Icons.online;
     }
     
     public static Connection[] getConnections()
     {
         Properties properties = new Properties();
         File connectionsDir = new File("connections");
         
        if(!connectionsDir.isDirectory()) return new Connection[0];
         
         String[] connections = connectionsDir.list( new FilenameFilter()
         {
             public boolean accept(File file, String name)
             {
                 return name.contains(".properties");
             }
         });
         
         Connection[] connectionsArray = new Connection[connections.length];
         int i = 0;
         
         for(String connection: connections)
         {
             try 
             { 
                 properties.load(new FileInputStream("connections/" + connection)); 
                 connectionsArray[i] = new Connection(properties.getProperty("name"),
                         properties.getProperty("username"),
                         properties.getProperty("resource"),
                         properties.getProperty("host"),
                         properties.getProperty("port"));
                 i++;
             }
             catch(Exception ex) {}
         }
         
         return connectionsArray;
     }
     
     public static void saveConnection(String username, String resource, String host, String port, String name) throws Exception
     {
         Properties properties = new Properties();
         properties.setProperty("username", username);
         properties.setProperty("resource", resource);
         properties.setProperty("host", host);
         properties.setProperty("port", port);
         properties.setProperty("name", name);
         
         (new File("connections")).mkdir();
         
         try
         {
             properties.store(new FileOutputStream("connections/" + name + ".properties"), "Connection info for " + name);
         }
         catch(Exception e)
         {
             throw new Exception("Invalid filename");
         }
     }
 }
