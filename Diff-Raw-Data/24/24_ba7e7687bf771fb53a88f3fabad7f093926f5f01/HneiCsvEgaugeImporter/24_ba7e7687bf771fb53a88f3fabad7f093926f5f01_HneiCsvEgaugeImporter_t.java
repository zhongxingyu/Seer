 package org.wattdepot.hnei.csvimport;
 
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.xml.bind.JAXBException;
 import org.wattdepot.client.OverwriteAttemptedException;
 import org.wattdepot.client.WattDepotClient;
 import org.wattdepot.client.WattDepotClientException;
 import org.wattdepot.datainput.RowParseException;
 import org.wattdepot.resource.sensordata.jaxb.SensorData;
 import org.wattdepot.resource.source.jaxb.Source;
 import au.com.bytecode.opencsv.CSVReader;
 
 /**
  * This class reads Egauge data from CSV files provided by HNEI (delimited by commas), creates a
  * SensorData object from each line, and stores the SensorData objects to a WattDepot server.
  * 
  * @author BJ Peter DeLaCruz
  */
 public class HneiCsvEgaugeImporter extends HneiCsvImporter {
 
   /** Log file for this application. */
   private static final Logger log = Logger.getLogger(HneiCsvEgaugeImporter.class.getName());
 
   /** Name of the application on the command line. */
   private static final String toolName = "HneiCsvEgaugeImporter";
 
   /**
    * Creates a new HneiCsvEgaugeImporter object.
    * 
    * @param filename Name of the CSV file that contains Egauge data.
    * @param uri URI of the WattDepot server.
    * @param username Owner of the WattDepot server.
    * @param password Password to access the WattDepot server.
    * @param skipFirstRow True if first row contains row headers, false otherwise.
    */
   public HneiCsvEgaugeImporter(String filename, String uri, String username, String password,
       boolean skipFirstRow) {
     super(filename, uri, username, password, skipFirstRow);
     this.parser = new HneiCsvEgaugeRowParser(toolName, this.serverUri, null, log);
   }
 
   /**
    * Stores sensor data for a source that already exists on WattDepot server.
    * 
    * @param client WattDepotClient used to connect to the WattDepot server.
    * @param datum Sensor data for a source.
    * @return True if successful, false otherwise.
    */
  public boolean process(WattDepotClient client, SensorData datum) {
     try {
       client.storeSensorData(datum);
       this.numNewData++;
     }
     catch (OverwriteAttemptedException e) {
       this.numExistingData++;
      String msg = "Data at " + datum.getTimestamp().toString() + " already exists on server.\n";
       log.log(Level.INFO, msg);
     }
     catch (WattDepotClientException e) {
       System.err.println(e.toString());
       log.log(Level.SEVERE, e.toString());
       return false;
     }
     catch (JAXBException e) {
       System.err.println(e.toString());
       log.log(Level.SEVERE, e.toString());
       return false;
     }
     return true;
   }
 
   /**
    * Given a CSV file with data for only one source, this program will parse each row, create a
    * SensorData object from each, and store the sensor data for that source on a WattDepot server.
    * 
    * @param args Contains filename, server URI, username, and password.
    */
   public static void main(String[] args) {
     if (args.length != 4) {
       System.err.println("Command-line arguments not in correct format. Exiting...");
       System.exit(1);
     }
 
     String filename = args[0];
     String serverUri = args[1];
     String username = args[2];
     String password = args[3];
 
     // Open CSV file for reading.
     CSVReader reader = null;
     try {
       reader = new CSVReader(new FileReader(filename), ',', CSVReader.DEFAULT_QUOTE_CHARACTER, 1);
     }
     catch (FileNotFoundException e) {
       System.err.println("File not found! Exiting...");
       System.exit(1);
     }
 
     // Grab data from CSV file.
     HneiCsvEgaugeImporter inputClient =
         new HneiCsvEgaugeImporter(filename, serverUri, username, password, true);
     WattDepotClient client = new WattDepotClient(serverUri, username, password);
     if (client.isHealthy() && client.isAuthenticated()) {
       System.out.println("Successfully connected to " + client.getWattDepotUri() + ".");
     }
     else {
       System.err.println("Unable to connect to WattDepot server.");
       System.exit(1);
     }
 
     if (!inputClient.setupLogger()) {
       System.exit(1);
     }
 
     String sourceName = filename.substring(0, filename.lastIndexOf('.'));
 
     // Store source on WattDepot server.
     try {
       client.storeSource(new Source(sourceName, username, true), false);
     }
     catch (OverwriteAttemptedException e) {
       String msg = "Source " + sourceName + " already exists on server.\n";
       System.out.print(msg);
       log.log(Level.INFO, msg);
     }
     catch (WattDepotClientException e) {
       System.err.println(e.toString());
       log.log(Level.SEVERE, e.toString());
       System.exit(1);
     }
     catch (JAXBException e) {
       System.err.println(e.toString());
       log.log(Level.SEVERE, e.toString());
       System.exit(1);
     }
 
     // Store data on WattDepot server.
     int counter = 0;
     String[] line = null;
     SensorData datum = null;
     try {
       ((HneiCsvEgaugeRowParser) inputClient.getParser()).setSourceName(sourceName);
       for (int i = 0; i < 10; i++) {
         line = reader.readNext();
       // while ((line = reader.readNext()) != null) {
         try {
           if ((datum = inputClient.getParser().parseRow(line)) == null) {
             inputClient.numInvalidEntries++;
           }
           else {
             System.out.println(datum);
            if (inputClient.process(client, datum)) {
              inputClient.numEntriesProcessed++;
            }
            else {
              inputClient.numInvalidEntries++;
            }
           }
         }
         catch (RowParseException e) {
           log.log(Level.SEVERE, "There was a problem parsing the entry for source " + sourceName);
           inputClient.numInvalidEntries++;
         }
         inputClient.numTotalEntries++;
         if ((++counter % 500) == 0) {
           System.out.println("Processing line " + counter + " in " + inputClient.filename + "...");
         }
       }
     }
     catch (IOException e) {
       e.printStackTrace();
       System.exit(1);
     }

   }
 
 }
