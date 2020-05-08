 package org.hackystat.sensor.xmldata.option;
 
 import java.io.File;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.xml.bind.JAXBException;
 import javax.xml.bind.Unmarshaller;
 import javax.xml.namespace.QName;
 
 import org.hackystat.sensor.xmldata.XmlDataController;
 import org.hackystat.sensor.xmldata.jaxb.Entries;
 import org.hackystat.sensor.xmldata.jaxb.Entry;
 import org.hackystat.sensor.xmldata.jaxb.ObjectFactory;
 import org.hackystat.sensor.xmldata.jaxb.XmlData;
 import org.hackystat.sensorshell.SensorProperties;
 import org.hackystat.sensorshell.SensorPropertiesException;
 import org.hackystat.sensorshell.Shell;
 import org.hackystat.utilities.tstamp.TstampSet;
 import org.xml.sax.SAXException;
 
 /**
  * The option used to send generic sensor data, via the sensorshell, to the
  * sensorbase. This option accepts a list of files that contain the generic
  * sensor information.
  * @author aito
  * 
  */
 public class FileOption extends AbstractOption {
   /** The name of this option, which is "-file". */
   public static final String OPTION_NAME = "-file";
 
   /**
    * Creates this option with the specified controller and parameters.
    * @param controller the specified controller.
    * @param parameters the specified parameters.
    */
   public FileOption(XmlDataController controller, List<String> parameters) {
     super(controller, OPTION_NAME, parameters);
   }
 
   /**
    * Returns true if the the list of parameters contains 1 or more files that
    * exist.
    * @return true if this option's parameters are valid.
    */
   @Override
   public boolean isValid() {
     if (this.getParameters().size() == 0) {
       String msg = "The number of parameters must include at least 1 file. "
           + "Ex: -file foo.xml foo2.xml";
       this.getController().fireMessage(msg);
       return false;
     }
 
     for (String parameter : this.getParameters()) {
       File file = new File(parameter);
       if (!file.exists()) {
         String msg = "The file '" + file + "' does not exist.";
         this.getController().fireMessage(msg);
         return false;
       }
     }
     return true;
   }
 
   /**
    * Executes this option by grabbing all information stored in the specified
    * files, and sending them to the sensorbase.
    */
   public void execute() {
     try {
       // First, lets get the correct shell instance.
       Shell shell = OptionUtil.createShell(new SensorProperties(), this.getController());
      
       // Then, send data from each file.
       int entriesAdded = 0;
       for (String filePath : this.getParameters()) {
         this.getController().fireVerboseMessage("Sending data from: " + filePath);
         Unmarshaller unmarshaller = OptionUtil.createUnmarshaller(ObjectFactory.class,
             "xmldata.xsd");
         XmlData xmlData = (XmlData) unmarshaller.unmarshal(new File(filePath));
         Entries entries = xmlData.getEntries();
 
         // Only send data if the SDT is set or all entries have SDT attributes.
         TstampSet tstampSet = new TstampSet();
         Object sdtName = this.getController().getOptionObject(Options.SDT);
         if (sdtName != null || this.hasSdtAttributes(entries.getEntry())) {
           for (Entry entry : entries.getEntry()) {
             // Then, lets set the "required" attributes.
             Map<String, String> keyValMap = new HashMap<String, String>();
             keyValMap.put("Tool", entry.getTool());
             keyValMap.put("Resource", entry.getResource());
             keyValMap.put("SensorDataType", (String) sdtName);
             keyValMap.put("Timestamp", OptionUtil.getCurrentTimestamp(true, tstampSet)
                 .toString());
 
             // Next, add the optional attributes.
             Map<QName, String> map = entry.getOtherAttributes();
             for (Map.Entry<QName, String> attributeEntry : map.entrySet()) {
               String entryName = attributeEntry.getKey().toString();
               String entryValue = attributeEntry.getValue();
 
               // If entries contain tstamps, override the current tstamp.
               if ("Timestamp".equals(entryName)) {
                 long timestamp = OptionUtil.getTimestampInMillis(entryValue);
                 Boolean isUnique = (Boolean) this.getController().getOptionObject(
                     Options.UNIQUE_TSTAMP);
                 entryValue = OptionUtil.massageTimestamp(isUnique, tstampSet, timestamp)
                     .toString();
               }
               keyValMap.put(entryName, entryValue);
             }
             // Finally, add the mapping and send the data.
             this.getController().fireVerboseMessage(OptionUtil.getMapVerboseString(keyValMap));
             shell.add(keyValMap);
             entriesAdded++;
           }
         }
         else {
           String msg = "The -sdt flag must be specified for all entries or each "
               + "xml entry must have the 'SensorDataType' attribute.";
           throw new Exception(msg);
         }
       }
 
       // Fires the send message and quits the sensorshell.
       OptionUtil.fireSendMessage(this.getController(), shell, entriesAdded);
       shell.quit();
     }
     catch (JAXBException e) {
       String msg = "There was a problem unmarshalling the data.  File(s) "
           + "may not conform to the xmldata schema.";
       this.getController().fireMessage(msg, e.toString());
     }
     catch (SAXException e) {
       String msg = "The specified file(s) could not be parsed.";
       this.getController().fireMessage(msg, e.toString());
     }
     catch (SensorPropertiesException e) {
       String msg = "The sensor.properties file in your userdir/.hackystat "
           + "directory is invalid or does not exist.";
       this.getController().fireMessage(msg);
     }
     catch (Exception e) {
       String msg = "The specified file(s) failed to load.";
       this.getController().fireMessage(msg, e.toString());
     }
   }
 
   /**
    * Returns true if the list of JAXB Entry object's each contain the sensor
    * data type attribute.
    * @param entries the list of entries to search.
    * @return true if each entry has the sdt attribute, false if not.
    */
   private boolean hasSdtAttributes(List<Entry> entries) {
     for (Entry entry : entries) {
       if (!this.hasSdtInAttributes(entry.getOtherAttributes())) {
         return false;
       }
     }
     return true;
   }
 
   /**
    * Returns true if the specified map contains the sensor data type attribute.
    * If the mapping does not have the sdt attribute, false is returned.
    * @param attributeMap the map to search.
    * @return true if the map has the sdt attribute, false if not.
    */
   private boolean hasSdtInAttributes(Map<QName, String> attributeMap) {
     for (QName name : attributeMap.keySet()) {
       if ("SensorDataType".equals(name.toString())) {
         return true;
       }
     }
     return false;
   }
 }
