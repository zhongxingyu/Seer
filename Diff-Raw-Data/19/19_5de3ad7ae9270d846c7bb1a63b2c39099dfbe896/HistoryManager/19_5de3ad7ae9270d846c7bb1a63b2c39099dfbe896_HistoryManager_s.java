 /*
  * This file is part of the aidGer project.
  *
  * Copyright (C) 2010-2011 The aidGer Team
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
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
      *
      * @throws HistoryException
      */
     private HistoryManager() throws HistoryException {
         historyFile = Runtime.getInstance().getConfigPath() + "/history";
         loadFromFile();
     }
 
     /**
      * Get the only instance of the HistoryManager class.
      *
      * @return The instance
      * @throws HistoryException
      */
     public static HistoryManager getInstance() throws HistoryException {
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
      *
      * @throws HistoryException
      */
     public void addEvent(HistoryEvent evt) throws HistoryException {
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
             throw new HistoryException(_("Couldn't find the history file."));
         } catch (IOException ex) {
             throw new HistoryException(_("Writing to the history file failed."));
         }
     }
 
     /**
      * Load the history from a file.
      *
      * @throws HistoryException
      */
     protected void loadFromFile() throws HistoryException {
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
                 throw new HistoryException(_("Creating the history file failed."));
             }
             return;
         }
 
         try {
             FileInputStream input = new FileInputStream(history);
             ObjectInputStream obj = new ObjectInputStream(input);
             events = (ArrayList<HistoryEvent>) obj.readObject();
             obj.close();
             input.close();
         } catch (FileNotFoundException ex) {
             throw new HistoryException(_("Couldn't find the history file."));
         } catch (IOException ex) {
             throw new HistoryException(_("Reading the history from file failed."));
         } catch (ClassNotFoundException ex) {
             throw new HistoryException(_("Illegal modifications of the history file have been detected."));
         }
     }
 }
