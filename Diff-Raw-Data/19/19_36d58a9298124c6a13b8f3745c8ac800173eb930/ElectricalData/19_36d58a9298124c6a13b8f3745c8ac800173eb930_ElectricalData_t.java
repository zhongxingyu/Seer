 package edu.hawaii.ihale.housesimulator.electrical;
 
 import java.util.Calendar;
 import java.util.Date;
 import java.util.Map;
 import java.util.Random;
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import org.restlet.ext.xml.DomRepresentation;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import edu.hawaii.ihale.api.ApiDictionary.IHaleState;
 import edu.hawaii.ihale.api.ApiDictionary.IHaleSystem;
 
 /**
  * Provides data on the ElectricalData system, as well as an XML representation.
  * 
  * @author Anthony Kinsey, Michael Cera
  * @author Christopher Ramelb, David Lin, Leonardo Nguyen, Nathan Dorman
  */
 public class ElectricalData {
 
   // by the EIA -
   // In 2005, the average monthly residential electricity consumption was 938 kilowatt hours (kWh),
   // according to the Energy Information Administration.
   // ~34 kWh a day or 34000 Wh
   /**
    * Each index represents an estimated hourly average of electricity consumption. Values based
    * around those found in the following link and adjusted from MW to W-
    * http://www.ferc.gov/market-oversight/mkt-electric/california/CAISO-rto-dly-rpt.pdf. The 25th
    * data point represents the average consumption for the entire day.
    */
   private static long[] hourlyAverage = { // in watts per hour
       1640, 1620, 1550, 1500, 1580, 1640, 1890, 2120, 1990, 1910, 1970, 1980, 1910, 1900, 1890,
           1880, 1850, 1860, 1910, 2230, 2080, 2070, 1980, 1880, 40880 / 24 };
 
   /** The current energy. */
   private static long energy = hourlyAverage[0];// randomGenerator.nextInt(1001) + 1000;
   /** The current power. */
   private static long power = hourlyAverage[0];// randomGenerator.nextInt(1001) + 1000;
 
   /**
    * Modifies the state of the system. Energy values fluctuate within 100 watts around
    * pre-determined daily averages in hourlyAverage.
    */
   public static void modifySystemState() {
     changePoints(Calendar.HOUR_OF_DAY);
 
 //    System.out.println("----------------------");
 //    System.out.println("System: Electrical");
 //    System.out.println("Energy: " + energy);
 //    System.out.println("Power: " + power);
   }
 
   /**
    * Returns the data as an XML Document instance. JavaApiDataDictionary only asks for power and
    * energy, so other e-gauge tags are currently commented out.
    * 
    * @return The data as XML.
    * @throws Exception If problems occur creating the XML.
    */
   public static DomRepresentation toXml() throws Exception {
     DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
     DocumentBuilder docBuilder = null;
     docBuilder = factory.newDocumentBuilder();
     String gridTag = "Grid";
     String titleAttr = "title";
     Document doc = docBuilder.newDocument();
     String meterTag = "meter";
     String energyTag = "energy";
     String powerTag = "power";
     String cpower = "cpower";
     String srcTag = "src";
     String energyWsTag = "energyWs";
     String solarTag = "Solar";
     String typeAttr = "type";
     String voltageTag = "voltage";
     String currentTag = "current";
 
     // Amount of Watts per second.
     long energyWs = energy * 1000 * 60 * 60;
     
     doc.setXmlVersion("1.0");
 
     // Create root tag.
     Element rootElement = doc.createElement("measurements");
     doc.appendChild(rootElement);
 
     // Create timestamp tag.
     Element timestampElement = doc.createElement("timestamp");
     timestampElement.setTextContent(String.valueOf(new Date().getTime()));
     rootElement.appendChild(timestampElement);
 
      // Create first cpower tag.
      Element cpowerElementA = doc.createElement(cpower);
      cpowerElementA.setAttribute(srcTag, gridTag);
      cpowerElementA.setAttribute("i", "2");
      cpowerElementA.setAttribute("u", "8");
      cpowerElementA.setTextContent(dummyOutput());
      rootElement.appendChild(cpowerElementA);
      // Second cpower tag.
      Element cpowerElementB = doc.createElement(cpower);
      cpowerElementB.setAttribute(srcTag, solarTag);
      cpowerElementB.setAttribute("i", "4");
      cpowerElementB.setAttribute("u", "8");
      cpowerElementB.setTextContent(dummyOutput());
      rootElement.appendChild(cpowerElementB);
      // Third cpower tag.
      Element cpowerElementC = doc.createElement(cpower);
      cpowerElementC.setAttribute(srcTag, gridTag);
      cpowerElementC.setAttribute("i", "3");
      cpowerElementC.setAttribute("u", "8");
      cpowerElementC.setTextContent(dummyOutput());
      rootElement.appendChild(cpowerElementC);
      // Fourth cpower tag.
      Element cpowerElementD = doc.createElement(cpower);
      cpowerElementD.setAttribute(srcTag, solarTag);
      cpowerElementD.setAttribute("i", "4");
      cpowerElementD.setAttribute("u", "8");
      cpowerElementD.setTextContent(dummyOutput());
      rootElement.appendChild(cpowerElementD);
 
     // First meter tag for Grid.
     Element meterElementA = doc.createElement(meterTag);
     meterElementA.setAttribute(titleAttr, gridTag);
     rootElement.appendChild(meterElementA);
     // Create energy tag.
     Element energyElementA = doc.createElement(energyTag);
     energyElementA.setTextContent(String.valueOf(energy));
     meterElementA.appendChild(energyElementA);
     // Create energyWs tag.
     Element energyWsElementA = doc.createElement(energyWsTag);
     energyWsElementA.setTextContent(String.valueOf(energyWs));
     meterElementA.appendChild(energyWsElementA);
     // Create power tag.
     Element powerElementA = doc.createElement(powerTag);
     powerElementA.setTextContent(String.valueOf(power));
     meterElementA.appendChild(powerElementA);
 
      // Second meter tag for Solar.
      Element meterElementB = doc.createElement(meterTag);
      meterElementB.setAttribute(titleAttr, solarTag);
      rootElement.appendChild(meterElementB);
      // Create energy tag.
      Element energyElementB = doc.createElement(energyTag);
      energyElementB.setTextContent(dummyOutput());
      meterElementB.appendChild(energyElementB);
      // Create energyWs tag.
      Element energyWsElement = doc.createElement(energyWsTag);
      energyWsElement.setTextContent(dummyOutput());
      meterElementB.appendChild(energyWsElement);
      // Create power tag.
      Element powerElementB = doc.createElement(powerTag);
      powerElementB.setTextContent(dummyOutput());
      meterElementB.appendChild(powerElementB);
     
     
      // Third meter tag for Grid.
      Element meterElementC = doc.createElement(meterTag);
      meterElementC.setAttribute(typeAttr, "total");
      meterElementC.setAttribute("title", "Total Usage");
      rootElement.appendChild(meterElementC);
      // Create energy tag.
      Element energyElementC = doc.createElement(energyTag);
      energyElementC.setTextContent(dummyOutput());
      meterElementC.appendChild(energyElementC);
      // Create energyWs tag.
      Element energyWsElementC = doc.createElement(energyWsTag);
      energyWsElementC.setTextContent(dummyOutput());
      meterElementC.appendChild(energyWsElementC);
      // Create power tag.
      Element powerElementC = doc.createElement(powerTag);
      powerElementC.setTextContent(dummyOutput());
      meterElementC.appendChild(powerElementC);
     
     
      // Fourth meter tag for Solar.
      Element meterElementD = doc.createElement(meterTag);
      meterElementD.setAttribute(typeAttr, "total");
      meterElementD.setAttribute("title", "Total Generation");
      rootElement.appendChild(meterElementD);
      // Create energy tag.
      Element energyElementD = doc.createElement(energyTag);
      energyElementD.setTextContent(dummyOutput());
      meterElementD.appendChild(energyElementD);
      // Create energyWs tag.
      Element energyWsElementD = doc.createElement(energyWsTag);
      energyWsElementD.setTextContent(dummyOutput());
      meterElementD.appendChild(energyWsElementD);
      // Create power tag.
      Element powerElementD = doc.createElement(powerTag);
      powerElementD.setTextContent(dummyOutput());
      meterElementD.appendChild(powerElementD);
     
     
      // Frequency tag.
      Element frequencyElement = doc.createElement("frequency");
      frequencyElement.setTextContent(dummyOutput());
      rootElement.appendChild(frequencyElement);
      // First voltage tag.
      Element voltageElementA = doc.createElement(voltageTag);
      voltageElementA.setTextContent(dummyOutput());
      rootElement.appendChild(voltageElementA);
      // Second voltage tag.
      Element voltageElementB = doc.createElement(voltageTag);
      voltageElementB.setTextContent(dummyOutput());
      rootElement.appendChild(voltageElementB);
      // First current tag.
      Element currentElementA = doc.createElement(currentTag);
      currentElementA.setTextContent(dummyOutput());
      rootElement.appendChild(currentElementA);
      // Second current tag.
      Element currentElementB = doc.createElement(currentTag);
      currentElementB.setTextContent(dummyOutput());
      rootElement.appendChild(currentElementB);
      // Third current tag.
      Element currentElementC = doc.createElement(currentTag);
      currentElementC.setTextContent(dummyOutput());
      rootElement.appendChild(currentElementC);
 
     // Convert Document to DomRepresentation.
     DomRepresentation result = new DomRepresentation();
     result.setDocument(doc);
 
     // Return the XML in DomRepresentation form.
     return result;
 
   }
 
   /**
    * Works backwards to generate historic electrical data.
    * 
    * @param baseTime The original time to work backwards from.
    * @param doc The document before historic data is appended.
    * @return doc The updated document..
    */
   public static Document generateHistoricData(Map<String, Integer> baseTime, Document doc) {
     int hour = baseTime.get("hour");
     int minute = baseTime.get("minute");
     int timestamp = baseTime.get("timestamp");
     String stateData = "state-data";
     String system = "system";
     String tStamp = "timestamp";
     String stateKey = "state-key";
     String electricString = IHaleSystem.ELECTRIC.toString();
     String keyString = "key";
     String valueString = "value";
     String energyString = IHaleState.ENERGY.toString();
     String powerString = IHaleState.POWER.toString();
 
     changePoints(hour);
     Element parent = doc.getDocumentElement();
     long tempTime = timestamp;
     // 12 state data points at 5 min intervals (to compose 1 hour report)
     for (int i = 0; i < 12; i++) {
       Element temp = doc.createElement(stateData);
       temp.setAttribute(system, electricString);
       temp.setAttribute(tStamp, "" + tempTime);
       Element tempElectric = doc.createElement(stateKey);
      tempElectric.setAttribute(keyString, energyString);
       tempElectric.setAttribute(valueString, "" + energy);
       temp.appendChild(tempElectric);
       Element tempPower = doc.createElement(stateKey);
       tempPower.setAttribute(keyString, powerString);
       tempPower.setAttribute(valueString, "" + power);
       temp.appendChild(tempPower);
       parent.appendChild(temp);
       // subtract 5 minutes in unix time
       tempTime = tempTime - 300;
       minute = minute - 5;
       // adjust in case hour must change
       if (minute < 0) {
         minute = 60 + minute;
         hour = hour - 1;
       }
       if (hour < 0) {
         hour = 23;
       }
       changePoints(hour);
 
     }
     // 24 state data points at 1 hour intervals (to compose 1 day report)
     for (int i = 0; i < 24; i++) {
       Element temp = doc.createElement(stateData);
       temp.setAttribute(system, electricString);
       temp.setAttribute(tStamp, "" + tempTime);
       Element tempElectric = doc.createElement(stateKey);
       tempElectric.setAttribute(keyString, energyString);
       tempElectric.setAttribute(valueString, "" + energy);
       temp.appendChild(tempElectric);
       Element tempPower = doc.createElement(stateKey);
       tempPower.setAttribute(keyString, powerString);
       tempPower.setAttribute(valueString, "" + power);
       temp.appendChild(tempPower);
       parent.appendChild(temp);
       tempTime = tempTime - 3600;
       hour = hour - 1;
       if (hour < 0) {
         hour = 23;
       }
       changePoints(hour);
     }
     // 7 state data points at 1 day intervals (to compose 1 week report)
     // 21-24 additional state data points at 1 day intervals (to compose 1 month report)
     // We know that the total is 6000 Wh a day, 6000/24 is the daily average.
     // We will fluctuate around the average.
     tempTime = timestamp;
     changePoints(24);
     for (int i = 0; i < 31; i++) {
       Element temp = doc.createElement(stateData);
       temp.setAttribute(system, electricString);
       temp.setAttribute(tStamp, "" + tempTime);
       Element tempElectric = doc.createElement(stateKey);
      tempElectric.setAttribute(keyString, energyString);
       tempElectric.setAttribute(valueString, "" + energy);
       temp.appendChild(tempElectric);
       Element tempPower = doc.createElement(stateKey);
       tempPower.setAttribute(keyString, powerString);
       tempPower.setAttribute(valueString, "" + power);
       temp.appendChild(tempPower);
       parent.appendChild(temp);
 
       tempTime = tempTime - 86400;
       changePoints(24);
     }
     return doc;
   }
 
   /**
    * Returns the average energy consumption in watts per hour. Used primarily for testing.
    * 
    * @param hour The hour to get the hourly consumption for.
    * @return The average for selected hour.
    */
   public static long getHourlyAverage(int hour) {
     return hourlyAverage[hour];
   }
   
   /**
    * Returns random output for non-critical fields.
    * 
    * @return A random value from 0 to 10.
    */
   public static String dummyOutput() {
     return "" + (10 * Math.random());
   }
 
   /**
    * Changes the data points based upon the hour provided.
    * 
    * @param hour The hour to base electricity consumption around.
    */
   public static void changePoints(int hour) {
     Random random = new Random();
     int randP = random.nextInt(10);
     int randE = random.nextInt(10);
 
     long changeValue = (long) (random.nextInt(10) + random.nextInt(100)) / 2;
     if (changeValue < -50) {
       changeValue = -50;
     }
     if (changeValue > 50) {
       changeValue = 50;
     }
     if (randP < 5) {
       energy = hourlyAverage[hour] - changeValue;
     }
     else {
       energy = hourlyAverage[hour] + changeValue;
     }
 
     changeValue = (long) (random.nextInt(10) + random.nextInt(100)) / 4;
     if (changeValue < -25) {
       changeValue = -25;
     }
     if (changeValue > 25) {
       changeValue = 25;
     }
     if (hour <= 6 || hour >= 18) {
       power = changeValue;
     }
     else if (randE < 5) {
       power = changeValue;
     }
     else {
       power = -1 * changeValue;
     }
     if (hour == 24) {
       changeValue = (long) (random.nextInt((int) (hourlyAverage[hour] / 10)));
       if (randP < 5) {
         energy = hourlyAverage[hour] - changeValue;
       }
       else {
         energy = hourlyAverage[hour] + changeValue;
       }
       changeValue = (long) (random.nextInt((int) (hourlyAverage[hour] / 10)));
       if (randE < 3) {
         power = changeValue * 3 / 4;
       }
       else if (randE < 6) {
         power = changeValue * 3 / 4;
       }
       else {
         power = changeValue / 4;
       }
     }
   }
   
   /**
    * Get energy.
    * @return  energy
    */
   public long getEnergy() {
     return energy;
   }
   
   /**
    * Get power.
    * @return power
    */
   public long getPower() {
     return power;
   }
 }
