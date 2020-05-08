 package edu.hawaii.systemh.housesimulator.aquaponics;
 
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
  * @author Anthony Kinsey, Michael Cera
  * @author Christopher Ramelb, David Lin, Leonardo Nguyen, Nathan Dorman
  */
 public class AquaponicsData {
 
   // /** Test number generation. */
   // public static void main(String[] args) {
   // for (int i = 0; i < 100; i++) {
   // System.out.println((randomGen.nextDouble() * (maxPH - minPH)) + minPH);
   // }
   // }
 
   /** Random generator. */
   private static final Random randomGen = new Random();
 
   // Safe condition ranges for fish to live.
   /** Number of fish alive in tank. */
   private static int aliveFish = 20;
   /** Minimum electrical conductivity in us/cm. */
   private static double minEC = 10.0;
   /** Maximum electrical conductivity in us/cm. */
   private static double maxEC = 20.0;
   /** Minimum water temperature in Celsius. */
   private static int minTemp = 20;
   /** Maximum water temperature in Celsius. */
   private static int maxTemp = 45;
   /** Minimum water level in inches. */
   private static int minWater = 36;
   /** Maximum water level in inches. */
   private static int maxWater = 48;
   /** Minimum water PH. */
   private static double minPH = 6.8;
   /** Maximum water PH. */
   private static double maxPH = 8.0;
   /** Minimum oxygen level in mg/l. */
   private static double minOxygen = 4.5;
   /** Maximum oxygen level in mg/l. */
   private static double maxOxygen = 5.5;
   /** Minimum water circulation in gpm. */
   private static double minCirc = 60.0;
   /** Maximum water circulation in gpm. */
   private static double maxCirc = 100.0;
   /** Maximum water turbidity in NTU. */
   private static double maxTurb = 100.0;
 
   // Conditions to be monitored in the system.
   // Try not to touch these formulas except for turbidity and oxygen!
   // Initial values are set between the minimum and maximum safe ranges automatically according
   //  to the conditions set above.
   /** The current water circulation. */
   private static double circulation = (randomGen.nextDouble() * (maxCirc - minCirc)) + minCirc;
   /** The current number of dead fish. */
   private static int deadFish = 0;
   /** The current electrical conductivity. */
   private static double ec = (randomGen.nextDouble() * (maxEC - minEC)) + minEC;
   /** The current temperature. */
   private static int temperature = randomGen.nextInt(maxTemp - minTemp + 1) + minTemp;
   /** The current turbidity. */
   private static double turbidity = (ec * 2) + aliveFish + (deadFish * 5) - (circulation * 0.1);
   /** The current water level. */
   private static int waterLevel = (randomGen.nextInt(maxWater - minWater + 1)) + minWater;
   /** The current pH. */
   private static double ph = (randomGen.nextDouble() * (maxPH - minPH)) + minPH;
   /** The current dissolved oxygen. Correlates with amount of water circulation. */
   private static double oxygen = (maxOxygen - minOxygen)
       * ((circulation - minCirc) / (maxCirc - minCirc)) + minOxygen;
 
   // Values that can be modified by PUT commands.
   // Try not to touch these formulas!
   // Initial desired values are set between the minimum and maximum safe ranges automatically
   //  according to the conditions set above.
   /** The desired electrical conductivity. */
   private static double desiredEC = (randomGen.nextDouble() * (maxEC - minEC)) + minEC;
   /** The desired temperature. */
   private static int desiredTemperature = randomGen.nextInt(maxTemp - minTemp + 1) + minTemp;
   /** The desired water level. */
   private static int desiredWaterLevel = (randomGen.nextInt(maxWater - minWater + 1)) + minWater;
   /** The desired pH. */
   private static double desiredPh = (randomGen.nextDouble() * (maxPH - minPH)) + minPH;
 
   /**
    * Resets the desired system state values randomly to within acceptable safe ranges automatically
    * according to the conditions set above. Try not to touch these formulas!
    */
   public static void resetDesiredState() {
     desiredEC = (randomGen.nextDouble() * (maxEC - minEC)) + minEC;
     desiredTemperature = randomGen.nextInt(maxTemp - minTemp + 1) + minTemp;
     desiredWaterLevel = (randomGen.nextInt(maxWater - minWater + 1)) + minWater;
     desiredPh = (randomGen.nextDouble() * (maxPH - minPH)) + minPH;
   }
 
   /**
    * Simulates changes due to desired values, relationship models, and a small degree of randomness.
    */
   public static void modifySystemState() {
     //printConditions();
     changeCirculation();
     changeDeadFish();
     changeEC();
     changeTemp();
     changeWaterLevel();
     changePH();
     // Must update oxygen and turbidity last because they have dependencies like circulation, etc.
     changeOxygen();
     changeTurbidity();
   }
 
   /**
    * Change water circulation.
    */
   public static void changeCirculation() {
     if (circulation < minCirc) {
       // Fish need more circulation and oxygen!!! Increment circulation by 0.0 to 1.0 units.
       circulation += randomGen.nextDouble();
     }
     else if (circulation > maxCirc) {
       // Too much oxygen!!! Decrement circulation by 0.0 to 1.0 units.
       circulation -= randomGen.nextDouble();
     }
     else {
       // Increment or decrement circulation by 0.0 to 1.0 units to reflect randomness.
       circulation += randomGen.nextDouble() - randomGen.nextDouble();
     }
   }
 
   /**
    * Change number of dead fish according to current conditions.
    */
   public static void changeDeadFish() {
     if (((ec < minEC) || (ec > maxEC) || (temperature < minTemp) || (temperature > maxTemp)
         || (waterLevel < minWater) || (waterLevel > maxWater) || (ph < minPH) || (ph > maxPH)
         || (oxygen < minOxygen) || (oxygen > maxOxygen))
         && (aliveFish > 0)) {
       // 50% chance of 1 fish dying.
       int fishDeath = randomGen.nextInt(2);
       deadFish += fishDeath;
       aliveFish -= fishDeath;
      // Each dead fish increases EC by 0.3
      for (int i = 0; i < fishDeath; i++) {
         ec += 0.3;
       }
     }
   }
 
   /**
    * Change electrical conductivity.
    */
   public static void changeEC() {
     if (ec < desiredEC) {
       // 25% chance of incrementing by 0.05
       ec += (randomGen.nextInt(2) * randomGen.nextInt(2)) * 0.05;
     }
     else if (ec > desiredEC) {
       // 50% chance of decrementing by 0.05. EC declines as nutrients and food gets used up.
       ec -= (randomGen.nextInt(2)) * 0.05;
     }
     else {
       // 12.5% chance of incrementing or decrementing 0.05 to reflect randomness.
       ec +=
           ((randomGen.nextInt(2) * randomGen.nextInt(2) * randomGen.nextInt(2)) - (randomGen
               .nextInt(2) * randomGen.nextInt(2) * randomGen.nextInt(2))) * 0.05;
     }
   }
 
   /**
    * Change water temperature.
    */
   public static void changeTemp() {
     if (temperature < desiredTemperature) {
       // 25% chance of incrementing by 1 degree.
       temperature += randomGen.nextInt(2) * randomGen.nextInt(2);
     }
     else if (temperature > desiredTemperature) {
       // 25% chance of decrementing by 1 degree.
       temperature -= randomGen.nextInt(2) * randomGen.nextInt(2);
     }
     else if (temperature == desiredTemperature) {
       // 12.5% chance of incrementing or decrementing 1 degree to reflect randomness.
       temperature +=
           (randomGen.nextInt(2) * randomGen.nextInt(2) * randomGen.nextInt(2))
               - (randomGen.nextInt(2) * randomGen.nextInt(2) * randomGen.nextInt(2));
     }
   }
 
   /**
    * Change water level.
    */
   public static void changeWaterLevel() {
     if (waterLevel < desiredWaterLevel) {
       // 50% chance to increment water_level by 0 to 2 units.
       waterLevel += randomGen.nextInt(2) + randomGen.nextInt(2);
     }
     else if (waterLevel > desiredWaterLevel) {
       // 50% chance to decrement water_level by 0 to 2 units.
       waterLevel -= randomGen.nextInt(2) + randomGen.nextInt(2);
     }
     else {
       // 50% chance of Increment or decrement water by 0 to 1 unit to reflect randomness.
       waterLevel += randomGen.nextInt(2) * (randomGen.nextInt(2) - randomGen.nextInt(2));
     }
   }
 
   /**
    * Change water PH.
    */
   public static void changePH() {
     if (ph < desiredPh) {
       // 25% chance of incrementing by 0.1
       ph += (randomGen.nextInt(2)) * (randomGen.nextInt(2)) * 0.1;
     }
     else if (ph > desiredPh) {
       // 25% chance of decrementing by 0.1
       ph -= (randomGen.nextInt(2)) * (randomGen.nextInt(2)) * 0.1;
     }
     else {
       // 12.5% chance of incrementing or decrementing 0.1 from desired PH.
       ph +=
           ((randomGen.nextInt(2) * randomGen.nextInt(2) * randomGen.nextInt(2)) - (randomGen
               .nextInt(2) * randomGen.nextInt(2) * randomGen.nextInt(2))) * 0.1;
     }
   }
 
   /**
    * Change water oxygen.  It correlates with min and max circulation that were set as safe ranges.
    * For example, if circulation is 60 then oxygen is 5.5.  If circulation is 100, then oxygen
    *  is 6.5.  If circulation is 80, then oxygen is 6.0.
    */
   public static void changeOxygen() {
     oxygen =
         (maxOxygen - minOxygen) * ((circulation - minCirc) / (maxCirc - minCirc)) + minOxygen;
   }
 
   /**
    * Change water turbidity.  This equation will probably need tweaking later on.
    */
   public static void changeTurbidity() {
     turbidity = (ec * 2) + aliveFish + (deadFish * 5) - (circulation * 0.1);
   }
 
   /**
    * Helper method to print current system conditions. Useful for debugging.
    */
   public static void printConditions() {
     final String desired = " (Desired: "; // PMD pickiness
     final String required = " (Required: "; // PMD pickiness
     System.out.println("----------------------");
     System.out.println("System: Aquaponics");
     System.out.println("Alive fish: " + aliveFish);
     System.out.println("Dead fish: " + deadFish);
     System.out.println("Circulation: " + roundSingleDecimal(circulation) + required + minCirc
         + "-" + maxCirc + ")");
     System.out.println("Electrical conductivity: " + roundSingleDecimal(ec) + desired
         + roundSingleDecimal(desiredEC) + ")" + required + minEC + "-" + maxEC + ")");
     System.out.println("Temperature: " + temperature + desired + desiredTemperature + ")"
         + required + minTemp + "-" + maxTemp + ")");
     System.out.println("Turbidity: " + roundSingleDecimal(turbidity) + required + "0" + "-"
         + maxTurb + ")");
     System.out.println("Water level: " + waterLevel + desired + desiredWaterLevel + ")"
         + required + minWater + "-" + maxWater + ")");
     System.out.println("pH: " + roundSingleDecimal(ph) + desired + roundSingleDecimal(desiredPh)
         + ")" + required + minPH + "-" + maxPH + ")");
     System.out.println("Oxygen level: " + roundSingleDecimal(oxygen) + required + minOxygen + "-"
         + maxOxygen + ")");
   }
 
   /**
    * Sets the desired temperature.
    * 
    * @param newDesiredTemperature the desired temperature
    */
   public static void setDesiredTemperature(int newDesiredTemperature) {
     desiredTemperature = newDesiredTemperature;
   }
 
   /**
    * Adds fish feed to the tank. Measured by grams.
    * 
    * @param feedAmount the amount of fish feed to add.
    */
   public static void addFishFeed(double feedAmount) {
     ec += feedAmount * 0.01;
   }
 
   /**
    * Harvests fish from tank.
    * 
    * @param numFish the amount of fish to harvest.
    */
   public static void harvestFish(int numFish) {
     aliveFish -= numFish;
   }
 
   /**
    * Sets the desired nutrient amount which corresponds to EC.
    * 
    * @param newDesiredNutrients the nutrient level.
    */
   public static void setNutrients(double newDesiredNutrients) {
     desiredEC = newDesiredNutrients;
   }
 
   /**
    * Sets the desired pH.
    * 
    * @param newDesiredPh the ph.
    */
   public static void setDesiredPh(double newDesiredPh) {
     desiredPh = newDesiredPh;
   }
 
   /**
    * Sets the desired water level.
    * 
    * @param newDesiredWaterLevel the water level.
    */
   public static void setDesiredWaterLevel(int newDesiredWaterLevel) {
     desiredWaterLevel = newDesiredWaterLevel;
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
     final String state = "state"; // PMD pickiness
     final String key = "key"; // PMD pickiness
     final String value = "value"; // PMD pickiness
 
     DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
     DocumentBuilder docBuilder = null;
     docBuilder = factory.newDocumentBuilder();
     Document doc = docBuilder.newDocument();
 
     // Create root tag
     Element rootElement = doc.createElement("state-data");
     rootElement.setAttribute("system", "AQUAPONICS");
     rootElement.setAttribute("device", "arduino-1");
     rootElement.setAttribute("timestamp", String.valueOf(new Date().getTime()));
     doc.appendChild(rootElement);
 
     // Create circulation state tag.
     Element circulationElement = doc.createElement(state);
     circulationElement.setAttribute(key, "CIRCULATION");
     circulationElement.setAttribute(value, String.valueOf(circulation));
     rootElement.appendChild(circulationElement);
 
     // Create dead fish state tag.
     Element deadFishElement = doc.createElement(state);
     deadFishElement.setAttribute(key, "DEAD_FISH");
     deadFishElement.setAttribute(value, String.valueOf(deadFish));
     rootElement.appendChild(deadFishElement);
 
     // Create electrical conductivity state tag.
     Element electricalConductivityElement = doc.createElement(state);
     electricalConductivityElement.setAttribute(key, "ELECTRICAL_CONDUCTIVITY");
     electricalConductivityElement.setAttribute(value, String.valueOf(ec));
     rootElement.appendChild(electricalConductivityElement);
 
     // Create temperature state tag.
     Element temperatureElement = doc.createElement(state);
     temperatureElement.setAttribute(key, "TEMPERATURE");
     temperatureElement.setAttribute(value, String.valueOf(temperature));
     rootElement.appendChild(temperatureElement);
 
     // Create turbidity state tag.
     Element turbidityElement = doc.createElement(state);
     turbidityElement.setAttribute(key, "TURBIDITY");
     turbidityElement.setAttribute(value, String.valueOf(turbidity));
     rootElement.appendChild(turbidityElement);
 
     // Create water state tag.
     Element waterLevelElement = doc.createElement(state);
     waterLevelElement.setAttribute(key, "WATER_LEVEL");
     waterLevelElement.setAttribute(value, String.valueOf(waterLevel));
     rootElement.appendChild(waterLevelElement);
 
     // Create PH state tag.
     Element phElement = doc.createElement(state);
     phElement.setAttribute(key, "PH");
     phElement.setAttribute(value, String.valueOf(roundSingleDecimal(ph)));
     rootElement.appendChild(phElement);
 
     // Create oxygen state tag.
     Element oxygenElement = doc.createElement(state);
     oxygenElement.setAttribute(key, "OXYGEN");
     oxygenElement.setAttribute(value, String.valueOf(oxygen));
     rootElement.appendChild(oxygenElement);
 
     // Convert Document to DomRepresentation.
     DomRepresentation result = new DomRepresentation();
     result.setDocument(doc);
 
     // Return the XML in DomRepresentation form.
     return result;
   }
 
   /**
    * Appends Aquaponics state data at a specific timestamp snap-shot to the Document object passed
    * to this method.
    * 
    * @param doc Document object to append Aquaponics state data as child nodes.
    * @param timestamp The specific time snap-shot the state data interested to be appended.
    * @return Document object with appended Aquaponics state data.
    */
   public static Document toXmlByTimestamp(Document doc, Long timestamp) {
     final String state = "state"; // PMD pickiness
     final String key = "key"; // PMD pickiness
     final String value = "value"; // PMD pickiness
 
     // Change the values.
     resetDesiredState();
     modifySystemState();
 
     // Get the root element, in this case would be <state-history> element.
     Element rootElement = doc.getDocumentElement();
 
     // Create state-data tag
     Element stateElement = doc.createElement("state-data");
     stateElement.setAttribute("system", "AQUAPONICS");
     stateElement.setAttribute("device", "arduino-1");
     stateElement.setAttribute("timestamp", timestamp.toString());
     rootElement.appendChild(stateElement);
 
     // Create circulation state tag.
     Element circulationElement = doc.createElement(state);
     circulationElement.setAttribute(key, "CIRCULATION");
     circulationElement.setAttribute(value, String.valueOf(circulation));
     stateElement.appendChild(circulationElement);
 
     // Create dead fish state tag.
     Element deadFishElement = doc.createElement(state);
     deadFishElement.setAttribute(key, "DEAD_FISH");
     deadFishElement.setAttribute(value, String.valueOf(deadFish));
     stateElement.appendChild(deadFishElement);
 
     // Create electrical conductivity state tag.
     Element electricalConductivityElement = doc.createElement(state);
     electricalConductivityElement.setAttribute(key, "ELECTRICAL_CONDUCTIVITY");
     electricalConductivityElement.setAttribute(value, String.valueOf(ec));
     stateElement.appendChild(electricalConductivityElement);
 
     // Create temperature state tag.
     Element temperatureElement = doc.createElement(state);
     temperatureElement.setAttribute(key, "TEMPERATURE");
     temperatureElement.setAttribute(value, String.valueOf(temperature));
     stateElement.appendChild(temperatureElement);
 
     // Create turbidity state tag.
     Element turbidityElement = doc.createElement(state);
     turbidityElement.setAttribute(key, "TURBIDITY");
     turbidityElement.setAttribute(value, String.valueOf(turbidity));
     stateElement.appendChild(turbidityElement);
 
     // Create water state tag.
     Element waterLevelElement = doc.createElement(state);
     waterLevelElement.setAttribute(key, "WATER_LEVEL");
     waterLevelElement.setAttribute(value, String.valueOf(waterLevel));
     stateElement.appendChild(waterLevelElement);
 
     // Create PH state tag.
     Element phElement = doc.createElement(state);
     phElement.setAttribute(key, "PH");
     phElement.setAttribute(value, String.valueOf(roundSingleDecimal(ph)));
     stateElement.appendChild(phElement);
 
     // Create oxygen state tag.
     Element oxygenElement = doc.createElement(state);
     oxygenElement.setAttribute(key, "OXYGEN");
     oxygenElement.setAttribute(value, String.valueOf(oxygen));
     stateElement.appendChild(oxygenElement);
 
     // Return the XML in DomRepresentation form.
     return doc;
   }
 
   /**
    * Gets number of alive fish.
    * @return number of alive fish
    */
   public int getAliveFish() {
     return aliveFish;
   }
 
   /**
    * Gets number of dead fish.
    * @return number of dead fish
    */
   public int getDeadFish() {
     return deadFish;
   }
 
   /**
    * Gets the amount of water circulation.
    * @return amount of water circulation
    */
   public double getCirc() {
     return roundSingleDecimal(circulation);
   }
 
   /**
    * Gets the electrical conductivity.
    * @return electrical conductivity
    */
   public double getEC() {
     return roundSingleDecimal(ec);
   }
 
   /**
    * Gets the water temperature.
    * @return water temperature
    */
   public int getTemp() {
     return temperature;
   }
 
   /**
    * Gets the water turbidity.
    * @return water turbidity
    */
   public double getTurb() {
     return roundSingleDecimal(turbidity);
   }
 
   /**
    * Gets the water level.
    * @return water level
    */
   public int getWaterLevel() {
     return waterLevel;
   }
 
   /**
    * Gets the PH level.
    * @return PH level
    */
   public double getPH() {
     return roundSingleDecimal(ph);
   }
 
   /**
    * Gets the amount of oxygen in water.
    * @return amount of oxygen in water
    */
   public double getOxygen() {
     return roundSingleDecimal(oxygen);
   }
 }
