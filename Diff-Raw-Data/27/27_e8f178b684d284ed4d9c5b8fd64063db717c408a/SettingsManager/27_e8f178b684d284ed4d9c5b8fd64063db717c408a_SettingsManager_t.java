 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package xmppclient;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.swing.JTextPane;
 import javax.swing.text.StyledDocument;
 import org.jivesoftware.smack.packet.Presence;
 import org.jivesoftware.smack.util.StringUtils;
 
 /**
  *
  * @author Lee Boynton (323326)
  */
 public class SettingsManager
 {
     // default directories which are created for each XMPP connection
     /**
      * The default directory for logs
      */
     public static final String LOGS_DIR = "logs";
     /**
      * The default directory for received files from the file transfer
      */
     public static final String RECEIVED_DIR = "received";
     /**
      * The default root directory
      */
     public static final String ROOT_DIR = "accounts";
     /**
      * The default directory for storing audio files for the audio library
      */
     public static final String AUDIO_DIR = "audio";
     
     /** The JID to store account details under */
     private String JID;
    private File accountDir;
 
     /**
      * Creates a new settings manager for the given JID. This will create a 
      * directory for storing settings associated with the given JID
      * @param JID The JID to store the settings under
      */
     public SettingsManager(String JID)
     {
         this.JID = StringUtils.parseBareAddress(JID);
         createRootDirectory();
     }
 
     /**
      * Gets the root settings directory
      * @return
      */
     public File getRootDir()
     {
        return accountDir;
     }
 
     private void createRootDirectory()
     {
        accountDir = new File(ROOT_DIR + File.separator + File.separator + JID);
        if(!accountDir.exists()) accountDir.mkdirs();
     }
 
     /**
      * Creates a directory under the root directory
      * @param name The name of the directory to create
      * @return A file object of the new directory
      */
     public File createDirectory(String name)
     {
        File dir = new File(accountDir.getAbsolutePath() + File.separator + name);
         if(!dir.exists()) dir.mkdir();
         return dir;
     }
 
     /**
      * Writes the contents of the given textpane to text file under the logs
      * directory of the account associated with this settings manager
      * @param textPane The textpane to log from
      * @param contact The contact the conversation was with
      */
     public void logConversation(JTextPane textPane, String contact)
     {
         FileWriter fstream = null;
         contact = StringUtils.unescapeNode(contact);
         
         // remove resource, if present
         contact = StringUtils.parseBareAddress(contact);
         
         try
         {
             File dir = createDirectory(LOGS_DIR);
             fstream = new FileWriter(dir.getAbsolutePath() + File.separator + contact + ".txt", true);
             BufferedWriter out = new BufferedWriter(fstream);
             StyledDocument doc = textPane.getStyledDocument();
             out.write("----------");
             out.newLine();
             out.write(doc.getText(0, doc.getLength()));
             out.flush();
             out.close();
             fstream.close();
         }
         catch (Exception ex)
         {
             Logger.getLogger(SettingsManager.class.getName()).log(Level.SEVERE, null, ex);
         }        
     }
     
     public void saveObject(Object obj, String name)
     {
         try
         {
            ObjectOutputStream fileOut = new ObjectOutputStream(new FileOutputStream(accountDir.getAbsolutePath() + File.separator + name));
             fileOut.writeObject(obj);
             fileOut.close();
         }
         catch (IOException ex)
         {
             Logger.getLogger(SettingsManager.class.getName()).log(Level.SEVERE, null, ex);
         }
     }
     
     /**
      * Loads an object from file
      * @param objectClass The 
      * @param name The name of the file to load from
      * @return The object read from the file if it matches the class given, or null otherwise
      */
     public Object loadObject(Class objectClass, String name)
     {
         Object obj = null;
         
         try
         {
            ObjectInputStream fileIn = new ObjectInputStream(new FileInputStream(accountDir.getAbsolutePath() + File.separator + name));
             
             Object foundObject = fileIn.readObject();
             if(foundObject.getClass().equals(objectClass))
             {
                 obj = foundObject;
             }
             fileIn.close();
         }
         catch (FileNotFoundException ex)
         {
             // object file does not exist
         }
         catch (ClassNotFoundException ex)
         {
             Logger.getLogger(SettingsManager.class.getName()).log(Level.SEVERE, null, ex);
         }        
         catch (IOException ex)
         {
             Logger.getLogger(SettingsManager.class.getName()).log(Level.SEVERE, null, ex);
         }
         
         return obj;
     }
     
     public List<Presence> getPresences()
     {
         List<Presence> presences = new ArrayList<Presence>();        
         List storedPresences = (List) loadObject(ArrayList.class, "presences");
         
         if(storedPresences == null) return presences;
         
         for(Object o:storedPresences)
         {
             if(o instanceof Presence)
             {
                 presences.add((Presence) o);
             }
         }
         
         return presences;
     }
     
     public void addPresence(Presence presence)
     {
         List<Presence> presences = getPresences();
         presences.add(presence);
         savePresences(presences);
     }
     
     public void savePresences(List<Presence> presences)
     {
         saveObject(presences, "presences");
     }
 }
