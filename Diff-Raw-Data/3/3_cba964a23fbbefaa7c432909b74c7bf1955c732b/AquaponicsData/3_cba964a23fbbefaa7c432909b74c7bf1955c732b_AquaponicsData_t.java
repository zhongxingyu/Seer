 package edu.hawaii.systemh.housesimulator.aquaponics;
 
 import java.text.DecimalFormat;
 import java.util.Date;
 
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
  * @author Tony Gaskell
  */
 public class AquaponicsData {
   
   // Stored command values.
   /** Represents the desired electrical conductivity level of the tank in S/cm. */
   private static double goalElectricalConductivity;
   /** Represents the desired temperature of the tank in Celcius. */
   private static int goalTemperature;
   /** Represents the desired water level of the tank in inches. */
   private static double goalWater;
   /** Represents the desired pH level of the tank. */
   private static double goalPH;
   /** Initializes the tank with 20 fish, each with 10 health points. */
   private static FishTank tank = new FishTank(20, 10);
 
   /**
    * Simulates changes due to desired values, relationship models, and a small degree of randomness.
    */
   public static void modifySystemState() {
     tank.changeCirculation();
     tank.changeTemperature(goalTemperature);
 
     tank.changeDeadFish();
     tank.checkPH();
 
     tank.changePH(goalPH);
     tank.changeOxygen();
     
     tank.changeElectricalConductivity(goalElectricalConductivity);
     tank.changeWaterLevel(goalWater);
     tank.changeTurbidity();
   }
   
   /**
    * Resets the desired system state values randomly to within acceptable safe ranges automatically
    * according to the conditions set above. Try not to touch these formulas!
    */
   public static void resetDesiredStates() {
     tank.resetDesiredStates();
   }
 
   // Commands
   /**
    * Adds fish feed to the tank. Measured by grams.
    */
   public static void addFishFeed() {
     tank.feedFish();
   }
 
   /**
    * Harvests fish from tank.
    * 
    * @param numFish the amount of fish to harvest.
    */
   public static void harvestFish(int numFish) {
     tank.harvestFish(numFish);
   }
 
   /**
    * Sets the desired nutrient amount which corresponds to electrical conductivity.
    * 
    * @param newDesiredNutrients the nutrient level.
    */
   public static void setNutrients(double newDesiredNutrients) {
     goalElectricalConductivity = newDesiredNutrients;
   }
 
   /**
    * Sets the desired pH.
    * 
    * @param newDesiredPh the pH.
    */
   public static void setDesiredPH(double newDesiredPh) {
     goalPH = newDesiredPh;
   }
 
   /**
    * Sets the desired temperature.
    * 
    * @param newDesiredTemperature the desired temperature
    */
   public static void setDesiredTemperature(int newDesiredTemperature) {
     goalTemperature = newDesiredTemperature;
   }
 
   /**
    * Sets the desired water level.
    * 
    * @param newDesiredWaterLevel the water level.
    */
   public static void setDesiredWaterLevel(int newDesiredWaterLevel) {
     goalWater = newDesiredWaterLevel;
   }
   
   /**
    * Rounds double value to a single decimal place.
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
     circulationElement.setAttribute(value, String.valueOf(tank.getCirculation()));
     rootElement.appendChild(circulationElement);
 
     // Create dead fish state tag.
     Element deadFishElement = doc.createElement(state);
     deadFishElement.setAttribute(key, "DEAD_FISH");
     deadFishElement.setAttribute(value, String.valueOf(tank.getDeadFish()));
     rootElement.appendChild(deadFishElement);
 
     // Create electrical conductivity state tag.
     Element electricalConductivityElement = doc.createElement(state);
     electricalConductivityElement.setAttribute(key, "ELECTRICAL_CONDUCTIVITY");
     electricalConductivityElement.setAttribute(value, 
         String.valueOf(tank.getElectricalConductivity()));
     rootElement.appendChild(electricalConductivityElement);
 
     // Create temperature state tag.
     Element temperatureElement = doc.createElement(state);
     temperatureElement.setAttribute(key, "TEMPERATURE");
     temperatureElement.setAttribute(value, String.valueOf(tank.getTemperature()));
     rootElement.appendChild(temperatureElement);
 
     // Create turbidity state tag.
     Element turbidityElement = doc.createElement(state);
     turbidityElement.setAttribute(key, "TURBIDITY");
     turbidityElement.setAttribute(value, String.valueOf(tank.getTurbidity()));
     rootElement.appendChild(turbidityElement);
 
     // Create water state tag.
     Element waterLevelElement = doc.createElement(state);
     waterLevelElement.setAttribute(key, "WATER_LEVEL");
     waterLevelElement.setAttribute(value, String.valueOf(tank.getWaterLevel()));
     rootElement.appendChild(waterLevelElement);
 
     // Create PH state tag.
     Element phElement = doc.createElement(state);
     phElement.setAttribute(key, "PH");
     phElement.setAttribute(value, String.valueOf(roundSingleDecimal(tank.getPH())));
     rootElement.appendChild(phElement);
 
     // Create oxygen state tag.
     Element oxygenElement = doc.createElement(state);
     oxygenElement.setAttribute(key, "OXYGEN");
     oxygenElement.setAttribute(value, String.valueOf(tank.getOxygen()));
     rootElement.appendChild(oxygenElement);
 
     // Convert Document to DomRepresentation.
     DomRepresentation result = new DomRepresentation();
     result.setDocument(doc);
 
     // Return the XML in DomRepresentation form.
     return result;
   }
 
   /**
    * Appends Aquaponics state data at a specific timestamp 
    * snap-shot to the Document object passed to this method.
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
     resetDesiredStates();
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
     circulationElement.setAttribute(value, String.valueOf(tank.getCirculation()));
     stateElement.appendChild(circulationElement);
 
     // Create dead fish state tag.
     Element deadFishElement = doc.createElement(state);
     deadFishElement.setAttribute(key, "DEAD_FISH");
     deadFishElement.setAttribute(value, String.valueOf(tank.getDeadFish()));
     stateElement.appendChild(deadFishElement);
 
     // Create electrical conductivity state tag.
     Element electricalConductivityElement = doc.createElement(state);
     electricalConductivityElement.setAttribute(key, "ELECTRICAL_CONDUCTIVITY");
     electricalConductivityElement.setAttribute(value,
         String.valueOf(tank.getElectricalConductivity()));
     stateElement.appendChild(electricalConductivityElement);
 
     // Create temperature state tag.
     Element temperatureElement = doc.createElement(state);
     temperatureElement.setAttribute(key, "TEMPERATURE");
     temperatureElement.setAttribute(value, String.valueOf(tank.getTemperature()));
     stateElement.appendChild(temperatureElement);
 
     // Create turbidity state tag.
     Element turbidityElement = doc.createElement(state);
     turbidityElement.setAttribute(key, "TURBIDITY");
     turbidityElement.setAttribute(value, String.valueOf(tank.getTurbidity()));
     stateElement.appendChild(turbidityElement);
 
     // Create water state tag.
     Element waterLevelElement = doc.createElement(state);
     waterLevelElement.setAttribute(key, "WATER_LEVEL");
     waterLevelElement.setAttribute(value, String.valueOf(tank.getWaterLevel()));
     stateElement.appendChild(waterLevelElement);
 
     // Create PH state tag.
     Element phElement = doc.createElement(state);
     phElement.setAttribute(key, "PH");
     phElement.setAttribute(value, String.valueOf(roundSingleDecimal(tank.getPH())));
     stateElement.appendChild(phElement);
 
     // Create oxygen state tag.
     Element oxygenElement = doc.createElement(state);
     oxygenElement.setAttribute(key, "OXYGEN");
     oxygenElement.setAttribute(value, String.valueOf(tank.getOxygen()));
     stateElement.appendChild(oxygenElement);
 
     // Return the XML in DomRepresentation form.
     return doc;
   }
 
   /**
    * Returns the number of live fish in the tank.
    * 
    * @return liveFish the number of live fish in the tank.
    */
   public int getAliveFish() {
     return tank.getLiveFish();
   }
 
   /**
    * Returns the number of dead fish in the tank.
    * 
    * @return the number of dead fish in the tank.
    */
   public int getDeadFish() {
    return tank.getDeadFish();
   }
 
   /**
    * Returns the circulation level of the tank.
    * 
    * @return circulation the circulation level of the tank.
    */
   public double getCirc() {
     return roundSingleDecimal(tank.getCirculation());
   }
 
   /**
    * Returns the electrical conductivity level of the tank.
    * 
    * @return ec the electrical conductivity level of the tank.
    */
   public double getEC() {
     return roundSingleDecimal(tank.getElectricalConductivity());
   }
 
   /**
    * Returns the temperature of the tank.
    * 
    * @return temperature the temperature of the tank.
    */
   public int getTemp() {
     return tank.getTemperature();
   }
 
   /**
    * Returns the turbidity of the tank.
    * 
    * @return turbidity the turbidity of the tank.
    */
   public double getTurb() {
     return roundSingleDecimal(tank.getTurbidity());
 
   }
 
   /**
    * Returns the water level of the tank.
    * 
    * @return waterLevel the water level of the tank.
    */
   public int getWaterLevel() {
     return tank.getWaterLevel();
   }
 
   /**
    * Returns the pH level of the tank.
    * 
    * @return pH the pH level of the tank.
    */
   public double getPH() {
     return roundSingleDecimal(tank.getPH());
   }
 
   /**
    * Returns the dissolved level of the tank.
    * 
    * @return oxygen the dissolved level level of the tank.
    */
   public double getOxygen() {
     return roundSingleDecimal(tank.getOxygen());
   }
 }
