 package de.aidger.utils.history;
 
 import static de.aidger.utils.Translation._;
 import de.aidger.model.Runtime;
import de.aidger.utils.Logger;
 import de.aidger.view.UI;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.util.List;
 import java.util.ArrayList;
 
 /**
  * Manages changes to the database.
  *
  * @author aidGer Team
  */
 final public class HistoryManager {
 
     /**
      * The list of all events.
      */
     private List<HistoryEvent> events = new ArrayList<HistoryEvent>();
 
     /**
      * The name of the history file.
      */
     private String historyFile;
 
     /**
      * The only instance of the HistoryManager class.
      */
     private static HistoryManager instance = null;
 
     /**
      * Initializes the HistoryManager class
      */
     private HistoryManager() {
         historyFile = Runtime.getInstance().getConfigPath() + "/history";
         loadFromFile();
     }
 
     /**
      * Get the only instance of the HistoryManager class.
      *
      * @return The instance
      */
     public static HistoryManager getInstance() {
         if (instance == null) {
             instance = new HistoryManager();
         }
 
         return instance;
     }
 
     /**
      * Get a list of all history events
      *
      * @return List of events
      */
     public List<HistoryEvent> getEvents() {
         return events;
     }
 
     /**
      * Add an event to the history.
      *
      * @param evt
      *          The event to add
      */
     public void addEvent(HistoryEvent evt) {
         events.add(evt);
         if (events.size() > Integer.parseInt(Runtime.getInstance().getOption(
                 "history-length", "100"))) {
             events.remove(0);
         }
 
         try {
             File history = new File(historyFile);
             if (!history.exists()) {
                 history.createNewFile();
             }
 
             FileOutputStream out = new FileOutputStream(history);
             ObjectOutputStream obj = new ObjectOutputStream(out);
             obj.writeObject(events);
             obj.close();
             out.close();
         } catch (FileNotFoundException ex) {
             // Shouldn't happen :)
         } catch (IOException ex) {
             UI.displayError(_("Writing to the history file failed."));
         }
     }
 
     /**
      * Load the history from a file.
      */
     protected void loadFromFile() {
         File history = new File(historyFile);
         if (!history.exists()) {
             try {
                 history.createNewFile();
                FileOutputStream out = new FileOutputStream(history);
                ObjectOutputStream obj = new ObjectOutputStream(out);
                obj.writeObject(events);
                obj.close();
                out.close();
             } catch (IOException ex) {
                Logger.error(_("Creating the history file failed."));
             }
             return;
         }
 
         String result = "";
         try {
             FileInputStream input = new FileInputStream(history);
             ObjectInputStream obj = new ObjectInputStream(input);
             events = (ArrayList<HistoryEvent>) obj.readObject();
             obj.close();
             input.close();
         } catch (FileNotFoundException ex) {
             // Shouldn't happen :)
         } catch (IOException ex) {
             UI.displayError(_("Reading the history from file failed."));
         } catch (ClassNotFoundException ex) {
             UI.displayError(_("Illegal modifications of the history file have been detected."));
         }
     }
 }
