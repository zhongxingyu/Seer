 package edu.hawaii.ihale.housesimulator.aquaponics;
 
 import java.text.DecimalFormat;
 import java.util.Date;
 import java.util.Random;
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import org.restlet.ext.xml.DomRepresentation;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 
 /**
  * Provides data on the Aquaponics system, as well as an XML representation.
  * 
  * @author Michael Cera
  * @author Anthony Kinsey
  */
 public class AquaponicsData {
 
   /** Random generator. */
   private static final Random randomGenerator = new Random();
 
   /** The current temperature. */
   private static long temperature = (long) randomGenerator.nextInt(11) + 60;
   /** The current pH. */
   private static double ph = (randomGenerator.nextDouble() * 3) + 6.5;
   /** The current oxygen. */
   private static double oxygen = (randomGenerator.nextDouble() * 9) + 1;
 
   /** The desired temperature. */
   private static long desiredTemperature = (long) randomGenerator.nextInt(11) + 60;
   /** The desired pH. */
   private static double desiredPh = (randomGenerator.nextDouble() * 3) + 6.5;
   /** The desired oxygen. */
   private static double desiredOxygen = (randomGenerator.nextDouble() * 9) + 1;
 
   /** The max value temperature will increment by. */
   private static final long temperatureIncrement = 1;
   /** The max value pH will increment by. */
   private static double phIncrement = 0.2;
   /** The max value oxygen will increment by. */
   private static double oxygenIncrement = 0.3;
 
   /**
    * Modifies the state of the system.
    */
   public static void modifySystemState() {
 
     // Increments temperature within range of the desired temperature.
     if (temperature > (desiredTemperature - temperatureIncrement)
         && temperature < (desiredTemperature + temperatureIncrement)) {
       temperature +=
           randomGenerator.nextInt(((int) temperatureIncrement * 2) + 1) - temperatureIncrement;
     }
     else if (temperature < desiredTemperature) {
       temperature += randomGenerator.nextInt((int) temperatureIncrement + 1);
     }
     else {
       temperature -= (randomGenerator.nextInt((int) temperatureIncrement + 1));
     }
 
     // Increments pH within range of the desired pH.
     if (ph > (desiredPh - phIncrement) && ph < (desiredPh + phIncrement)) {
       ph += (randomGenerator.nextDouble() * (phIncrement * 2)) - phIncrement;
     }
     else if (ph < desiredPh) {
       ph += (randomGenerator.nextDouble() * phIncrement);
     }
     else {
       ph -= (randomGenerator.nextDouble() * phIncrement);
     }
 
     // Increments oxygen within range of the desired oxygen.
     if (oxygen > (desiredOxygen - oxygenIncrement) && oxygen < (desiredOxygen + oxygenIncrement)) {
       oxygen += (randomGenerator.nextDouble() * (oxygenIncrement * 2)) - oxygenIncrement;
     }
     else if (oxygen < desiredOxygen) {
       oxygen += (randomGenerator.nextDouble() * oxygenIncrement);
     }
     else {
       oxygen -= (randomGenerator.nextDouble() * oxygenIncrement);
     }
 
     System.out.println("----------------------");
     System.out.println("System: Aquaponics");
     System.out.println("Temperature: " + temperature + " (Desired: " + desiredTemperature + ")");
     System.out.println("pH: " + roundSingleDecimal(ph) + " (Desired: "
         + roundSingleDecimal(desiredPh) + ")");
     System.out.println("Oxygen: " + roundSingleDecimal(oxygen) + " (Desired: "
         + roundSingleDecimal(desiredOxygen) + ")");
   }
 
   /**
    * Sets the desired temperature.
    * 
    * @param newDesiredTemperature the desired temperature
    */
   public static void setDesiredTemperature(long newDesiredTemperature) {
     desiredTemperature = newDesiredTemperature;
   }
 
   /**
    * Sets the desired pH.
    * 
    * @param newDesiredPh the ph
    */
   public static void setDesiredPh(double newDesiredPh) {
     desiredPh = newDesiredPh;
   }
 
   /**
    * Sets the desired oxygen.
    * 
    * @param newDesiredOxygen the oxygen
    */
   public static void setDesiredOxygen(double newDesiredOxygen) {
    oxygen = newDesiredOxygen;
   }
 
   /**
    * Rounds double value to a single decimal place.
    * 
    * @param doubleValue A double value
    * @return Rounded value
    */
   static double roundSingleDecimal(double doubleValue) {
     DecimalFormat singleDecimal = new DecimalFormat("#.#");
     return Double.valueOf(singleDecimal.format(doubleValue));
   }
 
   /**
    * Returns the data as an XML Document instance.
    * 
    * @return The data as XML.
    * @throws Exception If problems occur creating the XML.
    */
   public static DomRepresentation toXml() throws Exception {
     DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
     DocumentBuilder docBuilder = null;
     docBuilder = factory.newDocumentBuilder();
     Document doc = docBuilder.newDocument();
 
     // Create root tag
     Element rootElement = doc.createElement("state-data");
     rootElement.setAttribute("system", "aquaponics");
     rootElement.setAttribute("device", "arduino-1");
     rootElement.setAttribute("timestamp", String.valueOf(new Date().getTime()));
     doc.appendChild(rootElement);
 
     // Create state tag.
     Element temperatureElement = doc.createElement("state");
     temperatureElement.setAttribute("key", "temp");
     temperatureElement.setAttribute("value", String.valueOf(temperature));
     rootElement.appendChild(temperatureElement);
 
     // Create state tag.
     Element oxygenElement = doc.createElement("state");
     oxygenElement.setAttribute("key", "oxygen");
     oxygenElement.setAttribute("value", String.valueOf(roundSingleDecimal(oxygen)));
     rootElement.appendChild(oxygenElement);
 
     // Create state tag.
     Element phElement = doc.createElement("state");
     phElement.setAttribute("key", "pH");
     phElement.setAttribute("value", String.valueOf(roundSingleDecimal(ph)));
     rootElement.appendChild(phElement);
 
     // Convert Document to DomRepresentation.
     DomRepresentation result = new DomRepresentation();
     result.setDocument(doc);
 
     // Return the XML in DomRepresentation form.
     return result;
   }
 
 }
