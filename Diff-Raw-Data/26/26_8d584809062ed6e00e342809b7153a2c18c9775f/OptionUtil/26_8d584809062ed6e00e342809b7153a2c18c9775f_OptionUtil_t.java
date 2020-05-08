 package org.hackystat.sensor.xmldata.option;
 
import java.net.MalformedURLException;
import java.net.URL;
 import java.text.ParsePosition;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.Locale;
 import java.util.Map;
 
 import javax.xml.bind.JAXBContext;
 import javax.xml.bind.JAXBException;
 import javax.xml.bind.Unmarshaller;
 import javax.xml.datatype.XMLGregorianCalendar;
 import javax.xml.validation.Schema;
 import javax.xml.validation.SchemaFactory;
 
 import org.hackystat.sensor.xmldata.XmlDataController;
 import org.hackystat.sensorshell.MultiSensorShell;
 import org.hackystat.sensorshell.SensorProperties;
 import org.hackystat.sensorshell.SensorShell;
 import org.hackystat.sensorshell.Shell;
 import org.hackystat.utilities.tstamp.Tstamp;
 import org.hackystat.utilities.tstamp.TstampSet;
 import org.xml.sax.SAXException;
 
 /**
  * The option utility class that contains methods used by multiple options.
  * @author aito
  * 
  */
 public class OptionUtil {
   /** Private constructor that prevents instantiation. */
   private OptionUtil() {
   }
 
   /**
    * Returns the string containing the information stored in the key-value
    * mapping of sensor data. This string is helpful when running this option in
    * verbose mode.
    * @param keyValMap the map used to generate the returned string.
    * @return the informative string.
    */
   public static String getMapVerboseString(Map<String, String> keyValMap) {
     if (!keyValMap.isEmpty()) {
       String verboseString = "[";
       for (Map.Entry<String, String> entry : keyValMap.entrySet()) {
         verboseString = verboseString.concat(entry.getKey() + "=" + entry.getValue()) + ", ";
       }
 
       // Remove the last ', ' from the string.
       verboseString = verboseString.substring(0, verboseString.length() - 2);
       return verboseString.concat("]");
     }
     return "";
   }
 
   /**
    * Returns the long value of the specified timestamp string representation.
    * This method expects the timestamp string to be a long or in the
    * SimpleDateFormat: MM/dd/yyyy-hh:mm:ss. If the timestamp does not fit either
    * specification, a runtime exception is thrown.
    * @param timestamp the specified string representation of a timestamp.
    * @return the long value of the specified string timestamp.
    * @throws Exception thrown if the specified timestamp string is not in a
    * valid SimpleDateFormat.
    */
   public static long getTimestampInMillis(String timestamp) throws Exception {
     if (OptionUtil.isTimestampLong(timestamp)) {
       return Long.valueOf(timestamp);
     }
     else if (OptionUtil.isTimestampSimpleDate(timestamp)) {
       SimpleDateFormat format = new SimpleDateFormat("MM/dd/yyyy-hh:mm:ss", Locale.US);
       return format.parse(timestamp, new ParsePosition(0)).getTime();
     }
     String msg = "The timestamp must either be specified as a "
         + "long or in the format: MM/dd/yyyy-hh:mm:ss";
     throw new Exception(msg);
   }
 
   /**
    * Returns true if the specified timestamp string representation is in long
    * format.
    * @param timestamp the string to test.
    * @return true if the timestamp is a long, false if not.
    */
   private static boolean isTimestampLong(String timestamp) {
     try {
       Long.valueOf(timestamp);
       return true;
     }
     catch (NumberFormatException nfe) {
       return false;
     }
   }
 
   /**
    * Returns true if the specified timestamp is in the SimpleDateFormat:
    * MM/dd/yyyy-hh:mm:ss
    * @param timestamp the timestamp to test.
    * @return true if the timestamp is in the specified SimpleDateFormat, false
    * if not.
    */
   private static boolean isTimestampSimpleDate(String timestamp) {
     try {
       SimpleDateFormat format = new SimpleDateFormat("MM/dd/yyyy-hh:mm:ss", Locale.US);
       format.parse(timestamp, new ParsePosition(0)).getTime();
       return true;
     }
     catch (NullPointerException npe) {
       return false;
     }
   }
 
   /**
    * Returns the current timestamp based on the specified parameters.
    * @param isUnique if this is true, a unique timestamp, based on the specified
    * tstampSet, is returned.
    * @param tstampSet the set of timestamps that is managed to ensure that a
    * unique timestamp is generated.
    * @return the XmlGregorianCalendar instance representing the current
    * timestamp.
    */
   public static XMLGregorianCalendar getCurrentTimestamp(boolean isUnique, TstampSet tstampSet) {
     if (isUnique) {
       return Tstamp.makeTimestamp(tstampSet.getUniqueTstamp(new Date().getTime()));
     }
     return Tstamp.makeTimestamp();
   }
 
   /**
    * "Massages" the specified timestamp by using the specified parameters.
    * @param isUnique if this is true, the specified timestamp is changed to be
    * unique based on the specified tstampSet.
    * @param tstampSet the set of timestamps that is managed to ensure that a
    * unique timestamp is generated.
    * @param timestamp the timestamp to massage.
    * @return the XmlGregorianCalendar instance representing the current
    * timestamp.
    */
   public static XMLGregorianCalendar massageTimestamp(Boolean isUnique, TstampSet tstampSet,
       long timestamp) {
     if (isUnique) {
       return Tstamp.makeTimestamp(tstampSet.getUniqueTstamp(timestamp));
     }
     return Tstamp.makeTimestamp(timestamp);
   }
 
   /**
    * The helper method that returns an unmarshaller that is created using the
    * specified JAXB context class and schema file. The schema file name is the
    * name of a file that is relative to the '[top-level dir]/xml/schema/'
    * directory.
    * @param contextClass the specified context class used to build the
    * unmarshaller.
    * @param schemaFileName the schema file that adds schema validation to the
    * unmarshaller.
    * @return the unmarshaller instance.
    * @throws JAXBException thrown if there is a problem creating the
    * unmarshaller with the specified context class.
    * @throws SAXException thrown if there is a problem adding schema validation
    * with the specified schema file.
   * @throws MalformedURLException thrown if there is a problem finding the xsd
   * schema directory.
    */
   public static Unmarshaller createUnmarshaller(Class<?> contextClass, String schemaFileName)
      throws JAXBException, SAXException, MalformedURLException {
     JAXBContext context = JAXBContext.newInstance(contextClass);
     Unmarshaller unmarshaller = context.createUnmarshaller();
 
    // Retrieves the url of the schema files.
    String classJar = OptionUtil.class.getResource("").toString();
    int index = classJar.indexOf('!');
    String jarString = classJar.substring(0, index + 1);
    URL jarUrl = new URL(jarString + "/xml/schema/" + schemaFileName);

     // Adds schema validation to the unmarshalled file.
     SchemaFactory schemaFactory = SchemaFactory
         .newInstance("http://www.w3.org/2001/XMLSchema");
    Schema schema = schemaFactory.newSchema(jarUrl);
     unmarshaller.setSchema(schema);
     return unmarshaller;
   }
 
   /**
    * The helper method that fires a message to the specified controller based on
    * the connectivity of the sensorshell to a Hackystat server. If the server
    * exists, a normal message is sent. If the server does not exist, a message
    * informing the user of offline storage is sent. This method should be used
    * by all options that send data.
    * @param controller the controller that the message is fired to.
    * @param shell the shell used to test the connectivity of the Hackystat
    * server.
    * @param entriesAdded the number of entries added to the specified shell.
    */
   public static void fireSendMessage(XmlDataController controller, Shell shell,
       int entriesAdded) {
     if (shell.ping()) {
       controller.fireMessage(entriesAdded + " entries sent to " + controller.getHost());
     }
     else {
       controller.fireMessage("Server not available. Storing " + entriesAdded
           + " data entries offline.");
     }
   }
 
   /**
    * Creates a Shell instance based on the information found in the specified
    * controller. This method encapsulates the selection of which shell to use
    * based on the options specified to the controller.
    * @param properties the properties used to create the shell instance.
    * @param controller the controller that contains the information used to
    * determine which shell to use.
    * @return the Shell instance.
    * @throws Exception thrown if there is a problem instantiating a
    * MultiSensorShell instance.
    */
   public static Shell createShell(SensorProperties properties, XmlDataController controller)
       throws Exception {
     Boolean isMultiShell = (Boolean) controller.getOptionObject(Options.MULTI_SHELL);
     if (isMultiShell != null && isMultiShell) {
       return new MultiSensorShell(properties, "XmlData");
     }
     return new SensorShell(properties, false, "XmlData", true);
   }
 }
