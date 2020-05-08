 package cz.vutbr.fit.gja.gjaddr.persistancelayer;
 
 import com.thoughtworks.xstream.XStream;
 import com.thoughtworks.xstream.io.xml.DomDriver;
 import java.io.*;
 import java.util.ArrayList;
 import java.util.List;
 import org.slf4j.LoggerFactory;
 
 /**
  * Class for data binary serialization. Following class is used in two parts -
  * import/export and database persistence.
  *
  * @author Bc. Radek Gajdušek <xgajdu07@stud.fit.vutbr.cz>
  */
 public class Persistance {
 
   /**
    * Default extesion for XML persistance.
    */
   private final String XML_EXT = ".xml";
   /**
    * Default extesion for BIN persistance.
    */
   private final String BIN_EXT = ".gja";
   /**
    * Binary persistance indicator.
    */
   private boolean binary = Settings.instance().isBinPersistance();
 
   /**
    * Stores data to the persistance according to current persistance settings.
    *
    * @param filename source filename
    * @param dataToSave data to save
    */
   public void saveData(String filename, List dataToSave) {
 
    if (dataToSave == null) {
       return;
     }
 
     if (binary) {
       saveToBin(filename + BIN_EXT, dataToSave);
     } else {
       saveToXml(filename + XML_EXT, dataToSave);
     }
   }
 
   /**
    * Loads data from persistance according to current persistance settings.
    *
    * @param filename source filename
    * @return list of loaded data
    */
   public List loadData(String filename) {
     if (binary) {
       return loadFromBin(filename + BIN_EXT);
     } else {
       return loadFromXml(filename + XML_EXT);
     }
   }
 
   /**
    * Saves data to the BIN file, uses serialization.
    *
    * @param filename target filename
    * @param dataToSave list of dates, that will be stored
    */
   private void saveToBin(String filename, List dataToSave) {
 
     try {
       FileOutputStream flotpt = new FileOutputStream(filename);
       ObjectOutputStream objstr = new ObjectOutputStream(flotpt);
 
       try {
         objstr.writeObject(dataToSave);
         objstr.flush();
         this.log(this.getType(filename) + ": data saved to persistance.");
       } finally {
         try {
           objstr.close();
         } finally {
           flotpt.close();
         }
       }
     } catch (IOException ioe) {
       this.log(this.getType(filename) + ": data NOT saved to persistance.");
     }
   }
 
   /**
    * Saves data to the XML file, uses serialization.
    *
    * @param filename target filename
    * @param dataToSave list of dates, that will be stored
    */
   private void saveToXml(String filename, List dataToSave) {
 
     XStream xs = new XStream();
 
     try {
       FileOutputStream fs = new FileOutputStream(filename);
       xs.toXML(dataToSave, fs);
       this.log(this.getType(filename) + ": data saved to persistance.");
     } catch (FileNotFoundException ex) {
       this.log(this.getType(filename) + ": data NOT saved to persistance.");
     }
   }
 
   /**
    * Loads data from BIN file, uses deserialization.
    *
    * @param filename source filename
    * @return list of loaded data
    */
   private List loadFromBin(String filename) {
 
     List<Object> loadedData = new ArrayList<Object>();
 
     try {
       FileInputStream flinpstr = new FileInputStream(filename);
       ObjectInputStream objinstr = new ObjectInputStream(flinpstr);
       this.log(this.getType(filename) + ": loaded from persistance.");
 
       try {
         return (ArrayList) objinstr.readObject();
       } finally {
         try {
           objinstr.close();
         } finally {
           flinpstr.close();
         }
       }
     } catch (IOException ioe) {
       this.log(this.getType(filename) + ": NOT loaded from persistance - EMPTY.");
     } catch (ClassNotFoundException cnfe) {
       cnfe.printStackTrace();
     }
 
     return loadedData;
   }
 
   /**
    * Loads data from XML file, uses deserialization.
    *
    * @param filename source filename
    * @return list of loaded data
    */
   public List loadFromXml(String filename) {
     XStream xs = new XStream(new DomDriver());
     List<Object> loadedData = new ArrayList<Object>();
 
     try {
       FileInputStream fis = new FileInputStream(filename);
       loadedData = (List<Object>) xs.fromXML(fis);
       this.log(this.getType(filename) + ": loaded from persistance.");
     } catch (FileNotFoundException ex) {
       this.log(this.getType(filename) + ": NOT loaded from persistance - EMPTY.");
     }
 
     return loadedData;
   }
 
   /**
    * Imports data from BIN file.
    *
    * @param filename source filename
    * @return list of read data
    */
   public List importFromBin(String filename) {
     return loadFromBin(filename);
   }
 
   /**
    * Exports data to BIN file.
    *
    * @param filename target filename
    * @param dataToSave data to save
    */
   public void exportToBin(String filename, List dataToSave) {
 
     if (dataToSave == null || dataToSave.isEmpty()) {
       return;
     }
 
     saveToBin(filename, dataToSave);
   }
 
   /**
    * Method for logging actions.
    *
    * @param msg message to log
    */
   private void log(String msg) {
     LoggerFactory.getLogger(this.getClass()).info(msg);
   }
 
   /**
    * Resolving type of filename, using for logger.
    *
    * @param filename filename
    * @return string with type
    */
   private String getType(String filename) {
     if (filename.contains("contacts.")) {
       return "Contacts";
     }
     if (filename.contains("groups.")) {
       return "Groups";
     }
     if (filename.contains("groupsContacts.")) {
       return "Groups-contacts";
     }
     if (filename.contains("auth")) {
       return "Auth";
     }
     return "unknown";
   }
 }
