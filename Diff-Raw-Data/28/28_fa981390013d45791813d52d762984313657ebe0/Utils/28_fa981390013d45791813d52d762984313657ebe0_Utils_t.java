 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package xmppclient;
 
 import java.awt.Graphics;
 import java.awt.Image;
 import java.awt.image.BufferedImage;
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Enumeration;
 import java.util.List;
 import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
 import javax.imageio.ImageIO;
 import javax.swing.Icon;
 import javax.swing.ImageIcon;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JSplitPane;
 import javax.swing.border.Border;
 import javax.swing.filechooser.FileSystemView;
 import javax.swing.plaf.basic.BasicSplitPaneDivider;
 import javax.swing.plaf.basic.BasicSplitPaneUI;
 import org.jivesoftware.smack.RosterEntry;
 import org.jivesoftware.smack.RosterGroup;
 import org.jivesoftware.smack.XMPPException;
 import org.jivesoftware.smack.packet.Presence;
 import org.jivesoftware.smack.util.StringUtils;
 import org.jivesoftware.smackx.packet.VCard;
 import xmppclient.images.Icons;
 
 /**
  * This class provides a number of general purpose utility methods
  * @author Lee
  */
 public class Utils
 {
     private static final String CONNECTIONS_DESC = "This file contains properties " +
             "for stored XMPP connections. Each key should be prefixed by the account name.";
 
     /**
      * Gets the lowercase extension of a file
      * @param f The file
      * @return The extension
      */
     public static String getExtension(File f)
     {
         String ext = null;
         String s = f.getName();
         int i = s.lastIndexOf('.');
 
         if (i > 0 && i < s.length() - 1)
         {
             ext = s.substring(i + 1).toLowerCase();
         }
         return ext;
     }
 
     /**
      * Creates an error JLabel which displays an error icon
      * @param label The JLabel to use
      * @param message The message to display in the error label
      */
     public static void createErrorLabel(JLabel label, String message)
     {
         label.setText(message);
         label.setIcon(Icons.error);
     }
 
     /**
      * Creates a success JLabel which displays a success icon
      * @param label The JLabel to use
      * @param message The message to display in the success label
      */
     public static void createSuccessLabel(JLabel label, String message)
     {
         label.setText(message);
         label.setIcon(Icons.success);
     }
 
     /**
      * Resizes an image so that it is no higher than the specified size
      * and no wider than the specified size
      * @param image The image to resize
      * @param size The maximum size
      * @return The resized image
      */
     public static Icon resizeImage(ImageIcon image, int size)
     {
         ImageIcon resizedImage = null;
 
         if (image != null)
         {
             if (image.getIconWidth() > size || image.getIconHeight() > size)
             {
                 resizedImage = new ImageIcon(image.getImage().
                         getScaledInstance(size, size,
                         Image.SCALE_DEFAULT));
             }
             else
             {
                 resizedImage = image;
             }
         }
 
         return resizedImage;
     }
 
     /**
      * Gets the bytes from the given image
      * @param image The image to get the bytes from
      * @return The bytes of the image
      */
     public static byte[] getBytesFromImage(Image image)
     {
         ByteArrayOutputStream baos = new ByteArrayOutputStream();
         try
         {
             ImageIO.write(convert(image), "PNG", baos);
         }
         catch (IOException ex)
         {
         }
         catch (InterruptedException ex)
         {
         }
 
         return baos.toByteArray();
     }
 
     /**
      * Converts an image to a buffered image
      * @param image The image to convert
      * @return The buffered image
      * @throws java.lang.InterruptedException If something went wrong
      * @throws java.io.IOException If something went wrong
      */
     public static BufferedImage convert(Image image) throws InterruptedException, IOException
     {
         BufferedImage bi = new BufferedImage(image.getWidth(null), image.getHeight(null), BufferedImage.TYPE_INT_RGB);
         Graphics bg = bi.getGraphics();
         bg.drawImage(image, 0, 0, null);
         bg.dispose();
         return bi;
     }
 
     /**
      * Gets the nickname to use for the given roster entry. First checks if the 
      * local user has given them an alias, then checks if the roster entry has
      * given themselves a nickname. If neither of these then the JID is returned.
      * @param rosterEntry The roster entry
      * @return The nickname of the user
      */
     public static String getNickname(RosterEntry rosterEntry)
     {
         VCard vCard = new VCard();
 
         if (rosterEntry == null)
         {
             return null;
         }
         if (rosterEntry.getName() != null)
         {
             return rosterEntry.getName();
         }
         try
         {
             vCard.load(MainUI.connection, rosterEntry.getUser());
             if (vCard.getNickName() != null && !vCard.getNickName().equals(""))
             {
                 return vCard.getNickName();
             }
         }
         catch (Exception ex)
         {
         }
 
         if (rosterEntry.getUser() != null)
         {
             return rosterEntry.getUser();
         }
         return null;
     }
 
     /**
      * Gets the nickname of the user with the specified JID
      * @param JID The JID to get the nickname of
      * @return The nickname
      * @see #getNickname(RosterEntry rosterEntry)
      */
     public static String getNickname(String JID)
     {
         JID = StringUtils.parseBareAddress(JID);
         return getNickname(MainUI.connection.getRoster().getEntry(JID));
     }
 
     /**
      * Gets the nickname of the local user. First checks the vcard for a nickname,
      * if none is found then the JID is returned
      * @return The nickname
      */
     public static String getNickname()
     {
         VCard vCard = new VCard();
 
         try
         {
             vCard.load(MainUI.connection);
             if (vCard.getNickName() != null && !vCard.getNickName().equals(""))
             {
                 return vCard.getNickName();
             }
         }
         catch (Exception ex)
         {
         }
 
         return MainUI.connection.getUser();
     }
 
     /**
      * Gets the custom status message of the given presence. If there is none, this
      * will call the #getStatus(Presence presence) method to get the textual
      * status of the user
      * @param presence The presence
      * @return The status message
      */
     public static String getStatusMessage(Presence presence)
     {
         if (presence.getStatus() != null)
         {
             return presence.getStatus();
         }
         else
         {
             return getStatus(presence);
         }
     }
 
     /**
      * Gets the status of the presence
      * @param presence The presence
      * @return Returns either offline or available
      */
     public static String getStatus(Presence presence)
     {
         if (!presence.isAvailable())
         {
             return "Offline";
         }
         return "Available";
     }
 
     /**
      * Gets the avatar of the specified roster entry and resizes it
      * @param rosterEntry The user
      * @param size The max size
      * @return The resized avatar icon
      */
     public static Icon getAvatar(RosterEntry rosterEntry, int size)
     {
         VCard vCard = new VCard();
 
         try
         {
             vCard.load(MainUI.connection, rosterEntry.getUser());
             return Utils.resizeImage(new ImageIcon(vCard.getAvatar()), size);
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
 
     /**
      * Gets the avatar of the local user and resizes it
      * @param size The max size
      * @return The resized avatar icon
      */
     public static Icon getAvatar(int size)
     {
         VCard vCard = new VCard();
 
         try
         {
             vCard.load(MainUI.connection);
             return Utils.resizeImage(new ImageIcon(vCard.getAvatar()), size);
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
 
     /**
      * Gets the avatar of the given JID and resizes it
      * @param JID The JID
      * @param size The max size
      * @return The resized avatar icon
      */
     public static Icon getAvatar(String JID, int size)
     {
         return getAvatar(MainUI.connection.getRoster().getEntry(StringUtils.parseBareAddress(JID)), size);
     }
 
     /**
      * Gets the status icon of the given presence
      * @param presence The presence
      * @return The status icon
      */
     public static ImageIcon getUserIcon(Presence presence)
     {
         if (presence.getMode() == Presence.Mode.dnd)
         {
             return Icons.busy;
         }
         if (!presence.isAvailable())
         {
             return Icons.offline;
         }
         if (presence.isAway())
         {
             return Icons.away;
         }
 
         return Icons.online;
     }
 
     /**
      * Gets all the stored connections
      * @return A list of connections
      */
     public static List<Connection> getConnections()
     {
         Properties properties = new Properties();
         List<Connection> connections = new ArrayList<Connection>();
 
         try
         {
             properties.load(new FileInputStream("connections.properties"));
         }
         catch (IOException ex)
         {
             return connections;
         }
 
         Enumeration keys = properties.keys();
 
         while (keys.hasMoreElements())
         {
             String key = (String) keys.nextElement();
 
             if (key.endsWith("-port"))
             {
                 String name = key.substring(0, key.length() - 5);
                 getConnection(connections, name).setPort(properties.getProperty(name + "-port"));
             }
             if (key.endsWith("-host"))
             {
                 String name = key.substring(0, key.length() - 5);
                 getConnection(connections, name).setHost(properties.getProperty(name + "-host"));
             }
             if (key.endsWith("-username"))
             {
                 String name = key.substring(0, key.length() - 9);
                 getConnection(connections, name).setUsername(properties.getProperty(name + "-username"));
             }
             if (key.endsWith("-resource"))
             {
                 String name = key.substring(0, key.length() - 9);
                 getConnection(connections, name).setResource(properties.getProperty(name + "-resource"));
             }
         }
 
         return connections;
     }
 
     /**
      * Gets the connection matching the given name if it exists in the list, otherwise
      * creates the connection, adds it to the list and returns the connection
      * @param connections The list of connections
      * @param name The name of the connection to create/get
      * @return The connection
      */
     public static Connection getConnection(List<Connection> connections, String name)
     {
         for (Connection c : connections)
         {
             if (c.getName().equals(name))
             {
                 return c;
             }
         }
 
         Connection connection = new Connection();
         connection.setName(name);
         connections.add(connection);
 
         return connection;
     }
 
     /**
      * 
      * @param username
      * @param resource
      * @param host
      * @param port
      * @param name
      */
     public static void saveConnection(String username, String resource, String host, String port, String name)
     {
         Properties properties = new Properties();
         try
         {
             properties.load(new FileInputStream("connections.properties"));
         }
         catch (IOException ex)
         {
             // no previous connections
         }
         properties.setProperty(name + "-username", username);
         properties.setProperty(name + "-resource", resource);
         properties.setProperty(name + "-host", host);
         properties.setProperty(name + "-port", port);
         
         try
         {
             properties.store(new FileOutputStream("connections.properties"), CONNECTIONS_DESC);
         }
         catch (IOException ex)
         {
         }
     }
 
     /**
      * Deletes a stored XMPP connection by searching the connections.properties file
      * for keys matching the given name, and removing them
      * @param deleteName The name of the account to delete
      */
     public static void deleteConnection(String deleteName)
     {
         Properties properties = new Properties();
 
         try
         {
             properties.load(new FileInputStream("connections.properties"));
         }
         catch (IOException ex)
         { /*connections file not found*/ }
 
         Enumeration keys = properties.keys();
 
         while (keys.hasMoreElements())
         {
             String key = (String) keys.nextElement();
             String name = null;
 
             if (key.endsWith("-port"))
             {
                 name = key.substring(0, key.length() - 5);
                 if(name.equals(deleteName)) properties.remove(name + "-port");
             }
             if (key.endsWith("-host"))
             {
                 name = key.substring(0, key.length() - 5);
                 if(name.equals(deleteName)) properties.remove(name + "-host");
             }
             if (key.endsWith("-username"))
             {
                 name = key.substring(0, key.length() - 9);
                 if(name.equals(deleteName)) properties.remove(name + "-username");
             }
             if (key.endsWith("-resource"))
             {
                 name = key.substring(0, key.length() - 9);
                 if(name.equals(deleteName)) properties.remove(name + "-resource");
             }
         }
 
         try
         {
             properties.store(new FileOutputStream("connections.properties"), CONNECTIONS_DESC);
         }
         catch (IOException ex)
         {
         }
     }
 
     /**
      * Gets the system icon of a file
      * @param filename
      * @return The system icon
      */
     public static Icon getFileIcon(String filename)
     {
         Icon icon = null;
 
         try
         {
             //Create a temporary file with the specified extension
             File file = File.createTempFile("file", "." + getExtension(filename));
 
             System.out.println(file.getName());
 
             FileSystemView view = FileSystemView.getFileSystemView();
             icon = view.getSystemIcon(file);
 
             //Delete the temporary file
             file.delete();
         }
         catch (IOException ex)
         {
         }
 
         return icon;
     }
 
     /**
      * Gets the lowercase extension of a filename
      * @param filename The filename
      * @return The extension
      */
     public static String getExtension(String filename)
     {
         return getExtension(new File(filename));
     }
 
     /**
      * Saves a property for the given account name. Property values are stored
      * in the form accountName-property, and are stored in the connections.properties
      * file.
      * @param accountName The name of the account to retrieve the property for
      * @param property The property to retrieve
      * @param value The property value
      */
     public static void saveProperty(String accountName, String property, String value)
     {
         Properties properties = new Properties();
         try
         {
             properties.load(new FileInputStream("connections.properties"));
        }
        catch (IOException ex)
        {
            // file not found, ignore error
        }
        
        properties.setProperty(accountName + "-" + property, value);
        
        try
        {
             properties.store(new FileOutputStream("connections.properties"), CONNECTIONS_DESC);
         }
         catch (IOException ex)
         {
            Logger.getLogger(Utils.class.getName()).log(Level.SEVERE, null, ex);
         }
     }
 
     /**
      * Loads a property for the given account name
      * @param accountName The name of the account
      * @param property The property to retrieve
      * @return The property value
      */
     public static String loadProperty(String accountName, String property)
     {
         Properties properties = new Properties();
         
         try
         {
             properties.load(new FileInputStream("connections.properties"));
         }
         catch (IOException ex)
         {
            // file not found
            // return the default value
         }
 
         return properties.getProperty(accountName + "-" + property, "true");
     }
 
     /**
      * Gets all the modes used in presences. Can be available, away, busy etc
      * @return An array of modes
      */
     public static Presence.Mode[] getPresenceModes()
     {
         Presence.Mode modes[] =
         {
             Presence.Mode.available, Presence.Mode.away, Presence.Mode.chat, Presence.Mode.dnd, Presence.Mode.xa
         };
 
         return modes;
     }
 
     /**
      * Gets the presence types. Either available of unavailable.
      * @return An array of presence types.
      */
     public static Presence.Type[] getPresenceTypes()
     {
         Presence.Type types[] =
         {
             Presence.Type.available, Presence.Type.unavailable
         };
 
         return types;
     }
 
     /**
      * Gets the specified user's group names separated by commas
      * @param entry The user
      * @return The groups separated by commas
      */
     public static String getGroupsCSV(RosterEntry entry)
     {
         String csvGroups = "";
 
         for (RosterGroup group : entry.getGroups())
         {
             csvGroups += group.getName();
             csvGroups += ", ";
         }
 
         return csvGroups;
     }
 
     /**
      * 
      * @param path
      * @param create
      * @throws java.io.IOException
      * @throws java.lang.SecurityException
      */
     public static void openFileBrowser(String path, boolean create) throws IOException, SecurityException
     {
         if (create && !new File(path).exists())
         {
             new File(path).mkdirs();
         }
         if (!new File(path).exists())
         {
             JOptionPane.showMessageDialog(null, "The path could not be found", "Path not found", JOptionPane.ERROR_MESSAGE);
             return;
         }
 
         if (getSystem().equals("windows"))
         {
             Runtime.getRuntime().exec("explorer.exe " + path);
             return;
         }
         if (getSystem().equals("linux"))
         {
             try
             {
                Runtime.getRuntime().exec("dolphin file:///" + System.getProperty("user.dir") + path);
                Runtime.getRuntime().exec("nautilus " + path);
             }
             catch (IOException ex)
             {
                Runtime.getRuntime().exec("konqueror file:///" + System.getProperty("user.dir") + path);
             }
             return;
         }
     }
 
     /**
      * Gets the name of the operating system
      * @return The operating system, or null if unrecognised
      */
     public static String getSystem()
     {
         String os = null;
 
         if (System.getProperty("os.name").startsWith("Windows"))
         {
             // Windows-specific solution here
             os = "windows";
         }
         else if (System.getProperty("os.name").indexOf("Linux") != -1)
         {
             // Linux-specific solution here
             os = "linux";
         }
 
         return os;
     }
 
     /**
      * Removes the unsightly borders from the given JSplitPane
      * @param jSplitPane The split pane to remove borders from
      */
     public static void flattenSplitPane(JSplitPane jSplitPane)
     {
         jSplitPane.setUI(new BasicSplitPaneUI()
         {
             @Override
             public BasicSplitPaneDivider createDefaultDivider()
             {
                 return new BasicSplitPaneDivider(this)
                 {
                     @Override
                     public void setBorder(Border b)
                     {
                     }
                 };
             }
         });
         jSplitPane.setBorder(null);
     }
 }
