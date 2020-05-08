 package org.wattdepot.hnei.csvimport;
 
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.Locale;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.xml.datatype.XMLGregorianCalendar;
 import org.wattdepot.hnei.csvimport.validation.NonblankValue;
 import org.wattdepot.hnei.csvimport.validation.NumericValue;
 import org.wattdepot.hnei.csvimport.validation.Validator;
 import org.wattdepot.resource.property.jaxb.Property;
 import org.wattdepot.resource.sensordata.jaxb.SensorData;
 import org.wattdepot.resource.source.jaxb.Source;
 import org.wattdepot.util.tstamp.Tstamp;
 
 /**
  * Used to parse CSV files containing Egauge data for sources provided by HNEI.
  * 
  * @author BJ Peter DeLaCruz
  */
 public class HneiCsvEgaugeRowParser extends HneiCsvRowParser {
 
   /**
    * Creates a new HneiCsvEgaugeRowParser object.
    * 
    * @param toolName Name of the program.
    * @param serverUri URI of WattDepot server.
    * @param sourceName Source that is described by the sensor data.
    * @param log Log file, created in the HneiTabularFileSensor class.
    */
   public HneiCsvEgaugeRowParser(String toolName, String serverUri, String sourceName, Logger log) {
     super(toolName, serverUri, sourceName, log);
    this.formatDateTime = new SimpleDateFormat("MM/dd/yy hh:mm a", Locale.US);
   }
 
   /**
    * Sets the name of a source.
    * 
    * @param sourceName Sets the name of a source.
    */
   public void setSourceName(String sourceName) {
     this.sourceName = sourceName;
   }
 
   /**
    * Parses a row of Egauge data for a source from a CSV file provided by HNEI.
    * 
    * @param col Row from a CSV file that contains data for one source.
    * @return SensorData object if parse is successful, null otherwise.
    */
   @Override
   public SensorData parseRow(String[] col) {
     if (col == null) {
       this.log.log(Level.WARNING, "No input row specified.\n");
       return null;
     }
 
     if (col.length != 5) {
       String msg = "Row not in specified format:\n" + rowToString(col);
       this.log.log(Level.WARNING, msg);
       return null;
     }
 
     // Run validations.
     boolean result;
     for (int i = 1; i < col.length; i++) {
       for (Validator v : validators) {
         result = v.validateEntry(col[i]);
         if (!result) {
           String msg = "[" + col[i] + "] " + v.getErrorMessage() + "\n" + rowToString(col);
           System.err.print(msg);
           this.log.log(Level.WARNING, msg);
         }
         if (v instanceof NonblankValue && !result) {
           numBlankValues++;
           return null;
         }
         if (v instanceof NumericValue && !result) {
           numNonnumericValues++;
           return null;
         }
       }
     }
 
     Date date = null;
     try {
       date = formatDateTime.parse(col[0]);
     }
     catch (ParseException e) {
       try {
         date = formatDate.parse(col[0]);
       }
       catch (ParseException pe) {
         String msg = "Bad timestamp found in input file: " + col[0] + "\n" + rowToString(col);
         this.log.log(Level.WARNING, msg);
         return null;
       }
     }
     XMLGregorianCalendar timestamp = Tstamp.makeTimestamp(date.getTime());
 
     Property powerConsumed = new Property(SensorData.POWER_CONSUMED, Integer.parseInt(col[1]));
     String sourceUri = Source.sourceToUri(this.sourceName, this.serverUri);
     SensorData datum = new SensorData(timestamp, this.toolName, sourceUri, powerConsumed);
 
     datum.addProperty(new Property("airConditioner", col[2]));
     datum.addProperty(new Property("waterHeater", col[3]));
     datum.addProperty(new Property("dryer", col[4]));
 
     return datum;
   }
 
 }
