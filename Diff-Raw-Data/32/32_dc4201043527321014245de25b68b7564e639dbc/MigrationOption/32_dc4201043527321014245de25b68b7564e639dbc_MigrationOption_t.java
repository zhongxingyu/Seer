 package org.hackystat.sensor.xmldata.option;
 
 import java.io.File;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.xml.bind.JAXBContext;
 import javax.xml.bind.JAXBException;
 import javax.xml.bind.Unmarshaller;
 import javax.xml.namespace.QName;
 import javax.xml.validation.Schema;
 import javax.xml.validation.SchemaFactory;
 
 import org.hackystat.sensor.xmldata.XmlDataController;
 import org.hackystat.sensor.xmldata.jaxb.v7.Entry;
 import org.hackystat.sensor.xmldata.jaxb.v7.Sensor;
 import org.hackystat.sensor.xmldata.util.SensorDataPropertyMap;
import org.hackystat.sensorshell.MultiSensorShell;
 import org.hackystat.sensorshell.SensorProperties;
 import org.hackystat.sensorshell.SensorShell;
 import org.hackystat.utilities.tstamp.Tstamp;
 import org.hackystat.utilities.tstamp.TstampSet;
 import org.xml.sax.SAXException;
 
 /**
  * The option used to port Hackystat version 7 (v7) data to the Hackystat 8
  * sensorbase with the following steps:
  * 
  * <pre>
  * 1. The user provides the Hackystat 7 user directory, the version 7
  * account, the version 8 username, and version 8 password. 
  * 2. Then this option traverses the Hackystat 7 data directory and converts the information to
  * valid Hackystat 8 data.  
  * 3. Finally, the converted data is sent to the Hackystat 8 sensorbase.
  * </pre>
  * 
  * @author aito
  * 
  */
 public class MigrationOption extends AbstractOption {
   /** The name of this option, which is "-migration". */
   public static final String OPTION_NAME = "-migration";
   /** The version 7 data directory specified to convert to version 8 data. */
   private File v7DataDir = null;
   /**
    * The sensor properties file containing the version 8 host and account
    * information.
    */
   private SensorProperties properties = null;
 
   /**
    * Private constructor that creates this option with the specified controller,
    * name, and parameters.
    * @param controller the specified controller.
    * @param name the specified name.
    * @param parameters the specified parameters.
    */
   private MigrationOption(XmlDataController controller, String name, List<String> parameters) {
     super(controller, name, parameters);
   }
 
   /**
    * Constructs this option with the specified controller and parameters.
    * "-migration" is used as the name of this option.
    * @param controller the specified controller.
    * @param parameters the specified parameters.
    * @return the option instance.
    */
   public static Option createOption(XmlDataController controller, List<String> parameters) {
     Option option = new MigrationOption(controller, OPTION_NAME, parameters);
     return option;
   }
 
   /**
    * Returns true if the specified option parameters follows the convention:
    * 
    * <pre>
    * [v7 directory] [v7 account] [v8 host] [v8 username] [v8 password]
    * 
    * Ex:  -migration C:\foo ABCDEF http://localhost:9876/sensorbase austen@hawaii.edu fooPassword
    * Note that the v7 directory does not include the v7 account name.
    * </pre>
    * 
    * @return true if the option parameters are correct, false if not.
    */
   @Override
   public boolean isValid() {
     // Check if the correct amount of parameters exist.
     if (this.getParameters().isEmpty() || this.getParameters().size() < 5
         || this.getParameters().size() > 5) {
       String msg = "The -migration option only accepts five parameters, "
           + "[v7 directory] [v7 account] [v8 host] [v8 username] [v8 password]";
       this.getController().fireMessage(msg);
       return false;
     }
 
     // Verify that the version 7 directory exists.
     File v7Dir = new File(this.getParameters().get(0));
     String v7Account = this.getParameters().get(1);
     if (!new File(v7Dir.getAbsolutePath() + "/" + v7Account).exists()) {
       String msg = "The version 7 user directory, " + this.getParameters().get(0) + "/"
           + this.getParameters().get(1) + ", does not exist.";
       this.getController().fireMessage(msg);
       return false;
     }
 
     // Verify that the version 8 host, account and password is valid.
     SensorProperties props = new SensorProperties(this.getParameters().get(2), this
         .getParameters().get(3), this.getParameters().get(4));
     SensorShell shell = new SensorShell(props, false, "XmlData", true);
     if (!shell.ping()) {
       String msg = "The host, connection or account information is incorrect: Hackystat Host="
           + this.getParameters().get(2) + ", v8Account=" + this.getParameters().get(3)
           + ", v8Password=" + this.getParameters().get(4);
       this.getController().fireMessage(msg);
       return false;
     }
     return true;
   }
 
   /** Sets the variables used by the execute method. */
   @Override
   public void process() {
     if (this.isValid()) {
       // Sets the version 7 information.
       File v7Dir = new File(this.getParameters().get(0));
       String v7Account = this.getParameters().get(1);
       this.v7DataDir = new File(v7Dir.getAbsolutePath() + "/" + v7Account + "/data");
 
       // Sets the version 8 information.
       this.properties = new SensorProperties(this.getParameters().get(2), this.getParameters()
           .get(3), this.getParameters().get(4));
     }
   }
 
   /**
    * Executes this option by converting all version 7 data found in the
    * specified directory to version 8 compatiable data. The converted data is
    * sent to the Hackystat 8 sensorbase.
    */
   @Override
   public void execute() {
     // Iterates over each file in the version 7 data directory.
     try {
      MultiSensorShell shell = new MultiSensorShell(this.properties, "XmlData");
      TstampSet tstampSet = new TstampSet();
       JAXBContext context = JAXBContext
           .newInstance(org.hackystat.sensor.xmldata.jaxb.v7.ObjectFactory.class);
       Unmarshaller unmarshaller = context.createUnmarshaller();
 
       // Adds schema validation to the unmarshalled file.
       SchemaFactory schemaFactory = SchemaFactory
           .newInstance("http://www.w3.org/2001/XMLSchema");
       Schema schema = schemaFactory.newSchema(new File("xml/schema/v7data.xsd"));
       unmarshaller.setSchema(schema);
 
       int entriesAdded = 0;
       for (File sdtDir : this.v7DataDir.listFiles()) {
         for (File sensorDataFile : sdtDir.listFiles()) {
           this.getController().fireMessage(
               Tstamp.makeTimestamp().toString() + " Processing " + sensorDataFile);
 
           Sensor sensor = (Sensor) unmarshaller.unmarshal(sensorDataFile);
           for (Entry entry : sensor.getEntry()) {
             Map<String, String> keyValMap = new HashMap<String, String>();
             keyValMap.put("SensorDataType", sdtDir.getName());
             keyValMap.put("Timestamp", OptionUtil.getCurrentTimestamp(true, tstampSet)
                 .toString());
 
             // Add an entry for each key-value attribute in the data file.
             for (Map.Entry<QName, String> attribute : entry.getOtherAttributes().entrySet()) {
               this.addEntry(keyValMap, attribute, tstampSet);
             }
 
             shell.add(keyValMap);
             this.getController().fireVerboseMessage(OptionUtil.getMapVerboseString(keyValMap));
             entriesAdded++;
           }
         }
       }
      this.getController().fireMessage(
          entriesAdded + " entries sent to " + this.getController().getHost());
      shell.quit();
     }
     catch (JAXBException e) {
      String msg = "There was a problem unmarshalling the data. The v7 data file(s) "
           + "may not conform to the xmldata schema.";
       this.getController().fireMessage(msg, e.toString());
     }
     catch (SAXException e) {
       String msg = "The v7 data file(s) could not be parsed.";
       this.getController().fireMessage(msg, e.toString());
     }
     catch (Exception e) {
      String msg = "The v7 data file(s) failed to load. Please make sure the "
           + "specified directory has version 7 data.";
       this.getController().fireMessage(msg, e.toString());
     }
   }
 
   /**
    * Adds the entry to the specified key-value mapping. This method performs
    * additional processing on the entry, such as converting the version 7
    * timestamp to a compatible version 8 timestamp.
    * @param keyValMap the map reference that is populated by this method.
    * @param entry the entry, whose information is used to populate the specified
    * map.
    * @param tstampSet the set of timestamps used to generate a unique timestamp
    * for each entry.
    */
   private void addEntry(Map<String, String> keyValMap, Map.Entry<QName, String> entry,
       TstampSet tstampSet) {
     try {
       String entryName = entry.getKey().toString();
       String entryValue = entry.getValue();
 
       // Handles the timestamp attribute and it's uniqueness.
       if ("tstamp".equalsIgnoreCase(entryName)) {
         entryName = "Timestamp";
         long timestamp = OptionUtil.getTimestampInMillis(entryValue);
         Boolean isUnique = (Boolean) this.getController().getOptionObject(
             Options.UNIQUE_TSTAMP);
         entryValue = OptionUtil.massageTimestamp(isUnique, tstampSet, timestamp).toString();
         keyValMap.put(entryName, entryValue);
       }
 
       // Converts the file attribute to resource.
       else if ("file".equalsIgnoreCase(entryName) || "path".equalsIgnoreCase(entryName)) {
         keyValMap.put("Resource", entryValue);
       }
 
       // Converts a pMap encoded string to key-value pairs.
       else if ("pMap".equalsIgnoreCase(entryName)) {
         SensorDataPropertyMap pMap = new SensorDataPropertyMap(entryValue);
         for (Object key : pMap.keySet()) {
           String keyName = (String) key;
           keyValMap.put(keyName, pMap.get(keyName));
         }
       }
     }
     catch (Exception e) {
       this.getController().fireMessage(e.getMessage());
     }
   }
 }
