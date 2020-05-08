 package edu.hawaii.systemh.housesimulator.hvac;
 
 import java.util.Calendar;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Map;
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import org.restlet.ext.xml.DomRepresentation;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import edu.hawaii.ihale.api.ApiDictionary.IHaleState;
 import edu.hawaii.ihale.api.ApiDictionary.IHaleSystem;
 
 /**
  * Provides data on the HVAC system, as well as an XML representation. Temperature values returned
  * in the XML representation are in Celsius.
  * 
  * @author Anthony Kinsey, Michael Cera
  * @author Christopher Ramelb, David Lin, Leonardo Nguyen, Nathan Dorman
  */
 public class HVACData {
   
   /** The current date defaulted to a value when this class is first instantiated. **/
   private static Date currentTime = new Date();
   
   /** Map of monthly average high and average low temperatures in Washington D.C. **/
   private static Map<String, TemperatureRecord> washingtonMonthlyTemps = 
     new HashMap<String, TemperatureRecord>();  
   
   /** Map of when sunrise occurs in Washington D.C. monthly throughout the year. **/
   private static Map<String, Integer> washingtonMonthlySunrise = new HashMap<String, Integer>();
   
   /** Flag for when the HVAC system is to set the home to a certain temperature via PUT method. **/
   private static boolean desiredTempHasBeenSet = false;
   
   /** Time-stamp for when the HVAC system needs to regulate the home to a certain temperature. **/
   private static long whenDesiredTempCommandIssued = 0;
   
   /** The temperature the HVAC system has been commanded to maintain the home at. **/
   private static int desiredTemp = 0;
   
   /** Flag for when occupants are within the home or away. **/ 
   private static boolean occupantsHome = false;
   
   /** The efficient temperature to maintain the home at when occupants are home with energy
    *  consumption in mind and the outside temperatures is really hot 
    *  (i.e., temperatures during summer season). **/
   private static final int summerEfficientTempWhenOccupantHome = fahrenToCelsius(78);
   
   /** The efficient temperature to maintain the home at when occupants are not home with energy
    *  consumption in mind and the outside temperatures is really hot 
    *  (i.e., temperatures during summer season). **/
   private static final int summerEfficientTempWhenOccupantNotHome = fahrenToCelsius(88);
   
   /** The efficient temperature to maintain the home at when occupants are home with energy
    *  consumption in mind and the outside temperatures is really cold 
    *  (i.e., temperatures during winter season). **/
   private static final int winterEfficientTempWhenOccupantHome = fahrenToCelsius(68);
   
   /** The efficient temperature to maintain the home at when occupants are not home with energy
    *  consumption in mind and the outside temperatures is really hot 
    *  (i.e., temperatures during summer season). **/
   private static final int winterEfficientTempWhenOccupantNotHome = fahrenToCelsius(58);
   
   /** The amount of minutes the HVAC system requires to change the home temperature 1 degree C. **/
   private static final int numMinOneDegreeCelChange = 3;
   
   /** The current temperature outside the home. **/
   private static int currentOutsideTemp;
   
   /** The current home temperature, defaulted to -1000 to imply it hasn't been initialized to a
    *  valid value. **/
   private static int currentHomeTemp = -1000;
   
   /** Used to determine a baseline home temperature that will be influenced by insulation values
    *  and the current outside temperature to help determine rate of temperature in the home when
    *  the HVAC system has been issued a command to increase or decrease the current temperature. **/
   private static int baseHomeTemp;
   
   /** Flag to determine if certain values related to determining the current home temperature has
    *  been initialized. Used for re-initializing values. **/
   private static boolean initialRoomTemperatureSet = false;
   
   static {
     // Washington D.C. monthly weather history taken from: 
     // http://www.weather.com/weather/wxclimatology/monthly/graph/USDC0001
     // Initialize the monthly average high and low temperatures in F.
     washingtonMonthlyTemps.put("JANUARY", new TemperatureRecord(42, 27));
     washingtonMonthlyTemps.put("FEBRUARY",  new TemperatureRecord(47, 30));
     washingtonMonthlyTemps.put("MARCH", new TemperatureRecord(56, 37));
     washingtonMonthlyTemps.put("APRIL", new TemperatureRecord(66, 46));
     washingtonMonthlyTemps.put("MAY", new TemperatureRecord(75, 56));
     washingtonMonthlyTemps.put("JUNE", new TemperatureRecord(84, 65));
     washingtonMonthlyTemps.put("JULY", new TemperatureRecord(88, 76));
     washingtonMonthlyTemps.put("AUGUST", new TemperatureRecord(86, 69));
     washingtonMonthlyTemps.put("SEPTEMBER", new TemperatureRecord(79, 62));
     washingtonMonthlyTemps.put("OCTOBER", new TemperatureRecord(68, 50));
     washingtonMonthlyTemps.put("NOVEMBER", new TemperatureRecord(57, 40));
     washingtonMonthlyTemps.put("DECEMBER", new TemperatureRecord(47, 32));
     
     // Generalize sunrise hour for Washington D.C. provided by aid of:
     // http://www.timeanddate.com/worldclock/astronomy.html?n=263
     washingtonMonthlySunrise.put("JANURARY", 7);
     washingtonMonthlySunrise.put("FEBRUARY", 7);
     washingtonMonthlySunrise.put("MARCH", 7);
     washingtonMonthlySunrise.put("APRIL", 6);
     washingtonMonthlySunrise.put("MAY", 6);
     washingtonMonthlySunrise.put("JUNE", 5);
     washingtonMonthlySunrise.put("JULY", 6);
     washingtonMonthlySunrise.put("AUGUST", 6);
     washingtonMonthlySunrise.put("SEPTEMBER", 7);
     washingtonMonthlySunrise.put("OCTOBER", 7);
     washingtonMonthlySunrise.put("NOVEMBER", 7);
     washingtonMonthlySunrise.put("DECEMBER", 7);
   }
   
   /**
    * Modifies the state of the system. Resulting temperature units are in Celsius.
    * Outside and home temperatures are influenced by time of day and the corresponding current 
    * month's average high and average low temperatures as reflected by temperature data gathered
    * from 2010 for Washington D.C. 
    * The coldest part of the day is defined as just before and during sunrise. 
    * The hottest part of the day is defined to be at 3:00 PM or 15th hour of the day.
    * HVAC system should maintain temperatures approximately 78 in the summer and 68 in the winter,
    * adjusted by 10 degrees F higher or lower if occupants aren't home for energy usage efficiency
    * stated by some HVAC web-sites.
    * Lacking the home insulation value (R-value), this model assumes a static 3 min/C degree change
    * for simplicity.
    * Home temperature values will change over time to meet the desired temperature when commanded.
    * Otherwise the home will have its home temperature influenced purely by the outside temperature
    * and hour of the day.
    */
   public static void modifySystemState() {
     
     /** Initialize fields to generate the home temperature. **/
     
     Calendar calendar = Calendar.getInstance();
     calendar.setTime(currentTime);
     int monthNum = calendar.get(Calendar.MONTH);
     String month = "";
     
     switch (monthNum) {
       case 0: month = "JANUARY"; break;
       case 1: month = "FEBRUARY"; break;
       case 2: month = "MARCH"; break;
       case 3: month = "APRIL"; break;
       case 4: month = "MAY"; break;
       case 5: month = "JUNE"; break;
       case 6: month = "JULY"; break;
       case 7: month = "AUGUST"; break;
       case 8: month = "SEPTEMBER"; break;
       case 9: month = "OCTOBER"; break;
       case 10: month = "NOVEMBER"; break;
       case 11: month = "DECEMBER"; break;
       default: month = ""; return;
     }
     
     TemperatureRecord record = washingtonMonthlyTemps.get(month);
     int avgLowTemp = fahrenToCelsius(record.getAvgLowTemp());
     int avgHighTemp = fahrenToCelsius(record.getAvgHighTemp());
     int sunriseHour = washingtonMonthlySunrise.get(month);
     int hottestHourInDay = 15;
     
     // The rate the outside temperature increases from sunrise (coldest part of the day) to the
     // hottest point in the day (3:00 PM or 1500 hour).
     double degreeChangeFromSunriseToHighTempPt = 
       (avgHighTemp - avgLowTemp) / (double) (hottestHourInDay - sunriseHour);    
     
     // The rate the outside temperature decreases from the hottest point in the day to the next
     // day before the new sunrise.
     double degreeChangeFromHighTempPtToSunrise = 
       (avgHighTemp - avgLowTemp) / (double) (24 - hottestHourInDay + sunriseHour);
     
     int currentHour = calendar.get(Calendar.HOUR_OF_DAY);
     
     // Trend for currentTemp is to rise, beginning from sunrise to the hottest point in the day.
     if (currentHour >= sunriseHour && currentHour <= hottestHourInDay) {
       currentOutsideTemp = (int) ((currentHour - sunriseHour) * 
           degreeChangeFromSunriseToHighTempPt) + avgLowTemp;
     }
     // Trend for currentTemp is to fall, beginning from the hottest point in the day until
     // to the sunrise of the next day.
     else {
       
       // Handle the case when the currentHour of the day is within the next day, beyond
       // midnight.
       if (currentHour < sunriseHour) {
         currentOutsideTemp = (int) (avgHighTemp - 
             ((24 - hottestHourInDay + currentHour) * degreeChangeFromHighTempPtToSunrise));
       }
       // The currentHour is still within the current day, after the hottest point in the day, but
       // before midnight.
       else {
         currentOutsideTemp = (int) (avgHighTemp - 
             ((24 - currentHour) * degreeChangeFromHighTempPtToSunrise));
       }
     }
     
     /** End of the initialize of fields to generate the home temperature. **/
     
     // Situation 1:
     // If the HVAC system has had a desired temperature to maintain the home at.
     if (desiredTempHasBeenSet) {
 
       // Arbitrarily determined the difference in temperature between the outside temperature
       // and the temperature within the home. We don't know the insulation value of the home,
       // its ability to retain heat gain/loss influenced by the temperature outside.
       // Ratio between F to C is 1 to 1.8.
       int insulationValue = (int) (5 / 1.8);
       
       // The home maintains a cooler temperature than the outside temperature when its hot.
       // This process should occur only once per PUT command issued to change the temperature.
       if (currentOutsideTemp >= fahrenToCelsius(50) && !initialRoomTemperatureSet && 
           currentHomeTemp == -1000) {
         
         baseHomeTemp = currentOutsideTemp - insulationValue;
         currentHomeTemp = baseHomeTemp;
         initialRoomTemperatureSet = true;
       }
       // The home maintains a warmer temperature than the outside temperature when its cold.
       // This process should occur only once per PUT command issued to change the temperature.
       else if (currentOutsideTemp < fahrenToCelsius(50) && !initialRoomTemperatureSet && 
           currentHomeTemp == -1000) {
         
         baseHomeTemp = currentOutsideTemp + insulationValue;
         currentHomeTemp = baseHomeTemp;
         initialRoomTemperatureSet = true;
       }
       // This process should occur only once per PUT command issued to change the temperature.
       else if (!initialRoomTemperatureSet) {
         baseHomeTemp = currentHomeTemp;
         initialRoomTemperatureSet = true;
       }
       
       // The desired temperature has been reached.
       if (currentHomeTemp == desiredTemp) {
         initialRoomTemperatureSet = false;
         // Reset if it has been a day since a desired temperature value has been set.
         if ((new Date().getTime()) - whenDesiredTempCommandIssued >= 86400000) {
           desiredTempHasBeenSet = false;
         }   
       }
       // Desired temperature not reached.
       else {
         // If desired temperature is greater than currentHomeTemp, the trend is heating the room,
         if (desiredTemp > currentHomeTemp) {
           // Determine the amount of milliseconds have elapsed since the HVAC command has been
           // issued the command to set the room to a desired temperature, then
           // divided by numMinOneDegreeCelChange converted to millisecond units from 1000 * 60 to
           // obtain how much the temperature has changed in the home.
           currentHomeTemp = baseHomeTemp + (int) (
               //((new Date().getTime()) - whenDesiredTempCommandIssued) 
               (currentTime.getTime() - whenDesiredTempCommandIssued) 
                     / (1000 * 60 * numMinOneDegreeCelChange));
         }
         // otherwise the trend is to cool down the room.
         else if (desiredTemp < currentHomeTemp) {
           currentHomeTemp = baseHomeTemp - (int) (
               (currentTime.getTime() - whenDesiredTempCommandIssued) 
               / (1000 * 60 * numMinOneDegreeCelChange));       
         }
       }
     }
     
     // Situation 2:
     // No desired temperature to maintain the home at, HVAC system will undergo self-automation.
     // Home temperatures will be influenced by the outside temperature.
     else {
       
       occupantsHome = isOccupantsHome(currentTime);
       
       // Situation 2a:
       // If the home has occupants, the home temperature should be maintained at a comfortable
       // level.
       if (occupantsHome) {
         
         // Most HVAC systems can't keep a difference in outside and inside temperature of greater
         // than ~15-20 degrees F when high temperatures exceed 95 degrees F.
         if (currentOutsideTemp > fahrenToCelsius(95)) {
           currentHomeTemp = (int) (currentOutsideTemp - (18 / 1.8));
         }
         // Some HVAC sites suggest 78 degrees F is ideal to maintain the home at for energy 
         // efficiency when the outside weather is hot.
         else if (currentOutsideTemp >= fahrenToCelsius(78) && 
             currentOutsideTemp <= fahrenToCelsius(95)) { 
           
           currentHomeTemp = summerEfficientTempWhenOccupantHome;
         }
         // This is the ideal home temperature ranges for the solar decathlon contest. No need to
         // run the HVAC strongly and instead allow the outside and inside home temperatures to
         // converge to equilibrium temperature state.
         else if (currentOutsideTemp < fahrenToCelsius(78) && 
             currentOutsideTemp >= fahrenToCelsius(72)) {
           
           currentHomeTemp = currentOutsideTemp;
         }
         // Some HVAC sites suggest 68 degrees F is ideal to maintain the home at for energy
         // energy efficiency when the outside weather is cold.
         else if (currentOutsideTemp < fahrenToCelsius(72)) {
           currentHomeTemp = winterEfficientTempWhenOccupantHome;
           
           // If the outside temperature is really cold, and the occupants are sleeping, their 
           // bed-wear and blankets will provide extra warmth, reducing the HVAC system's burden, so
           // the current home temperature can be lower than when the occupants are normally awake
           // and home.
           boolean occupantsSleeping = isOccupantsSleeping(currentTime);
           if (occupantsHome && occupantsSleeping) {
             // Decide arbitrarily that the home can be 10F colder when occupants are asleep. 
             currentHomeTemp = (int) (winterEfficientTempWhenOccupantHome - (10 / 1.8));
 
           }
         }
       }
       
       // Situation 2b:
       // If occupants aren't home, then the HVAC system can be energy efficient by setting the
       // home temperature higher (in the summer) or lower (in the winter).
       else {
        
         if (currentOutsideTemp > fahrenToCelsius(95)) {
           currentHomeTemp = (int) (currentOutsideTemp - (18 / 1.8));
         }
         // Since the occupants are not home, the home temperature can be hotter than the ideal
         // home temperature to conserve energy. It is suggested to be 10 degrees F higher than
         // normal.
         else if (currentOutsideTemp >= fahrenToCelsius(88) && 
             currentOutsideTemp <= fahrenToCelsius(95)) {
           currentHomeTemp = summerEfficientTempWhenOccupantNotHome;
         }
         // Since the occupants are not home, the home temperature can be colder than the ideal
         // home temperature to conserve energy, but not too cold so that instruments are damaged.
         // It is suggested to be 10 degrees F lower than normal.
         else if (currentOutsideTemp <= fahrenToCelsius(68)) {
           currentHomeTemp = winterEfficientTempWhenOccupantNotHome;
         }
         else {
           currentHomeTemp = currentOutsideTemp;
         }
       }
     }
   }
   
   /**
    * Determines if there are occupants currently at home or out of the home.
    *
    * @param currentTime A Date object with the time of when we are interested if the occupants 
    *                    are home. I.e., if we are interested to know if the occupants are at home
    *                    at 1:00 PM on Saturdays, the Date object will be Saturday with an hour time 
    *                    of 1:00 PM.   
    * @return True if occupants are home, false otherwise.
    */
   public static boolean isOccupantsHome(Date currentTime) {
     
     Calendar calendar = Calendar.getInstance();
     calendar.setTime(currentTime);    
     int currentHour = calendar.get(Calendar.HOUR_OF_DAY);
     int currentDay = calendar.get(Calendar.DAY_OF_WEEK);
     boolean occupantsHome;
     
     // Occupants are assumed to be out of the home for work on the weekdays from 9:00 AM to 
     // 5:00 PM for AM/PM system or 0900 to 1700 hour system.
     if ((currentHour >= 9 && currentHour <= 17) && (currentDay > 1 && currentDay < 7)) {
       occupantsHome = false;
     }
     else {
       occupantsHome = true;
     }
     return occupantsHome;
   }
   
   /**
    * Determines if the occupants of the home are currently asleep or not.
    *
    * @param currentTime A Date object with the time of when we are interested in the occupants 
    *                    sleep state. I.e., if we are interested to know if the occupants are 
    *                    sleeping at 6:00 AM the Date object will be an arbitrary day with a time 
    *                    of 6:00 AM.
    * @return True if the occupants are currently sleeping, false otherwise.
    */
   public static boolean isOccupantsSleeping(Date currentTime) {
     
     Calendar calendar = Calendar.getInstance();
     calendar.setTime(currentTime);    
     int currentHour = calendar.get(Calendar.HOUR_OF_DAY);
     boolean occupantsSleeping;
     
     // Occupants are assumed to be sleeping from 11:00 PM to 7:00 AM for AM/PM system 
     // or 2300 to 700 hour system.
    if (currentHour >= 23 && currentHour <= 7) {
       occupantsSleeping = false;
     }
     else {
       occupantsSleeping = true;
     }
     return occupantsSleeping;
   }
   
   /**
    * Determine if the HVAC system has been issued a command to maintain the home at a specified 
    * temperature.
    *
    * @return Returns true if the HVAC system has been issued a command to maintain the home at a 
    *         specific temperature, false otherwise.
    */
   public boolean desiredTempCommandIssued() {
     return desiredTempHasBeenSet;
   }
   
   /**
    * Sets the current time to a new time. Used for reproducing historical or future temperature
    * records.
    *
    * @param time The new date in milliseconds that have passed since 
    *             January 1, 1970 00:00:00 GMT. 
    */
   public static void setCurrentTime(long time) {
     currentTime = new Date(time);
   }
   
   /**
    * Sets the desired temperature in Farenheit for the HVAC system to maintain the home at.
    * 
    * @param newDesiredTemp The temperature for the HVAC system to maintain the home at.
    */
   public static void setDesiredTemp(int newDesiredTemp) {
     desiredTemp = newDesiredTemp;
     desiredTempHasBeenSet = true;
     whenDesiredTempCommandIssued = (new Date()).getTime();
   }
 
   /**
    * Returns the milliseconds since elapsed past January 1, 1970 00:00:00 GMT that a PUT
    * request has been sent to the HVAC system to maintain the home temperature at.
    *
    * @return The timestamp of when the HVAC PUT request was successfully accepted.
    */
   public long getWhenDesiredTempCommandIssued() {
     return whenDesiredTempCommandIssued;
   }
   
   /**
    * Returns the current home temperature in Celsius.
    *
    * @return Current home temperature.
    */
   public int getCurrentHomeTemp() {
     return currentHomeTemp;
   }
   
   /**
    * Returns the desired temperature the HVAC system is to maintain the home at.
    *
    * @return The desired temperature the HVAC system is to maintain the home at.
    */
   public int getDesiredTemp() {
     return desiredTemp;
   }
   
   /**
    * Returns the current temperature outside the home.
    *
    * @return The current temperature outside the home.
    */
   public int currentOutsideTemp() {
     return currentOutsideTemp;
   }
   /**
    * Sets static fields that assist to simulate HVAC temperature control within the home by setting
    * them to false to emulate a HVAC turn off/reset. Useful for debugging purposes such as JUnit
    * testing.
    */
   public static void resetHVACSystem() {
     desiredTempHasBeenSet = false;
     desiredTemp = 0;
     initialRoomTemperatureSet = false;
     currentHomeTemp = -1000;
   }
   
   /**
    * Converts Fahrenheit to Celsius temperature.
    *
    * @param fahrenheit The Fahrenheit temperature to convert to Celsius.
    * @return The converted Fahrenheit to Celsius temperature.
    */
   private static int fahrenToCelsius(int fahrenheit) {
     return (fahrenheit - 32) * 5 / 9;
   }
   
   /**
    * Prints the current state of the HVAC system.
    */
   public static void printHVACSystemState() {
     
     System.out.println("----------------------");
     System.out.println("System: HVAC");
     System.out.println("Current time is: " + currentTime);
     System.out.println("Temperature: " + currentHomeTemp + "C " + "(Desired: " + desiredTemp + ")");
     if (occupantsHome) {
       System.out.println("The occupants are home.");
     }
     else {
       System.out.println("The occupants are not home.");
     }
     if (desiredTempHasBeenSet) {
       System.out.print("Desired temperature has been issued at: ");
       System.out.println(new Date(whenDesiredTempCommandIssued));
     }
     else {
       System.out.println("No desired temperature has been set.");
     }
     System.out.println("currentOutsideTemp is: " + currentOutsideTemp + "C");
   }
   
   /**
    * Returns the data as an XML Document instance.
    * 
    * @return HVAC state data in XML representation.
    * @throws Exception If problems occur creating the XML.
    */
   public static DomRepresentation toXml() throws Exception {  
     
     // Re-initialize temperature values.
     modifySystemState();
     
     String system = IHaleSystem.HVAC.toString();
     String device = "arduino-3";
     //String timestampString = timestamp.toString();
     String timestampString = String.valueOf(currentTime.getTime());
     String temperatureString = IHaleState.TEMPERATURE.toString();
     int celsiusTemp = currentHomeTemp;
 
     DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
     DocumentBuilder docBuilder = null;
     docBuilder = factory.newDocumentBuilder();
     Document doc = docBuilder.newDocument();
 
     // Create root tag
     Element rootElement = doc.createElement("state-data");
     rootElement.setAttribute("system", system);
     rootElement.setAttribute("device", device);
     rootElement.setAttribute("timestamp", timestampString);
     doc.appendChild(rootElement);
 
     // Create state tag.
     Element temperatureElement = doc.createElement("state");
     temperatureElement.setAttribute("key", temperatureString);
     temperatureElement.setAttribute("value", String.valueOf(celsiusTemp));
     rootElement.appendChild(temperatureElement);
 
     // Convert Document to DomRepresentation.
     DomRepresentation result = new DomRepresentation();
     result.setDocument(doc);
 
     // Return the XML in DomRepresentation form.
     return result;
   }
   
   /**
    * Appends HVAC state data at a specific timestamp snap-shot to the Document object
    * passed to this method.
    *
    * @param doc Document object to append HVAC state data as child nodes.
    * @param timestamp The specific time snap-shot the HVAC state data interested to be appended.
    * @return Document object with appended HVAC state data.
    */
   public static Document toXmlByTimestamp(Document doc, Long timestamp) {
 
     // Set the current HVAC system to reflect the state of the passed timestamp.
     setCurrentTime(timestamp);
     // Re-initialize temperature values.
     modifySystemState();
         
     String system = IHaleSystem.HVAC.toString();
     String device = "arduino-3";
     String timestampString = timestamp.toString();
     String temperatureString = IHaleState.TEMPERATURE.toString();
     int celsiusTemp = currentHomeTemp;
 
     // Get the root element, in this case would be <state-history> element.
     Element rootElement = doc.getDocumentElement();
     
     // Create state-data tag.
     Element stateDataElement = doc.createElement("state-data");
     stateDataElement.setAttribute("system", system);
     stateDataElement.setAttribute("device", device);
     stateDataElement.setAttribute("timestamp", timestampString);
     rootElement.appendChild(stateDataElement);
 
     // Create state tag.
     Element temperatureElement = doc.createElement("state");
     temperatureElement.setAttribute("key", temperatureString);
     temperatureElement.setAttribute("value", String.valueOf(celsiusTemp));
     stateDataElement.appendChild(temperatureElement);
     
     return doc;
   }
 }
