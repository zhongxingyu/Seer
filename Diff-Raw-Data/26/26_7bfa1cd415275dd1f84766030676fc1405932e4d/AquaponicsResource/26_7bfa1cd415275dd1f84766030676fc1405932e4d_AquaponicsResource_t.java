 package edu.hawaii.ihale.aquaponics;
  
 import java.util.Arrays;
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import org.restlet.ext.xml.DomRepresentation;
 import org.restlet.representation.Representation;
 import org.restlet.resource.Get;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import edu.hawaii.ihale.housesimulator.Arduino;
 /**
  * Simulates the Aquaponics System, holds values for temperature (temp), 
  * pH (pH), and dissolved oxygen (oxygen).
  * @author Team Maka
  *
  */
 public class AquaponicsResource extends Arduino {
   //These hold the goal state defined by the user.
   static double goalPH = 7, goalTemp = 78, goalOxygen = .5;
  static final String temp = "temp", pH = "ph", oxygen = "oxygen";
   /** Local keys used by the resource.*/
   public String[] localKeys = {temp, pH, oxygen};
   //Singleton repository
   //private AquaponicsRepository repository;
   
   /**
    * Constructor.
    */
   public AquaponicsResource() {
     super("aquaponics","arduino-1");
     AquaponicsRepository.getInstance();
     keys = localKeys; 
     list = Arrays.asList(keys);
   }
   
   /**
    * Refreshes data.
    */
   @Override
   public void poll() {
     //System.err.println("String value of getPH(): " + repository.getpH());
     AquaponicsRepository.setpH(String.valueOf(getPH()));
     AquaponicsRepository.setTemp(String.valueOf(getTemp()));
     AquaponicsRepository.setOxygen(String.valueOf(getOxygen()));
   }
   
   /**
    * Returns the Contact instance requested by the URL. 
    * @return The XML representation of the contact, or CLIENT_ERROR_NOT_ACCEPTABLE if the 
    * unique ID is not present.
    * @throws Exception If problems occur making the representation. Shouldn't occur in 
    * practice but if it does, Restlet will set the Status code. 
    */
   @Get
   public Representation getResource() throws Exception {
     //refresh values
     // Create an empty XML representation.
     DomRepresentation result = new DomRepresentation();
     DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
     DocumentBuilder builder = factory.newDocumentBuilder();
     Document doc = builder.newDocument();
     //create root element
     Element rootElement = doc.createElement("state-data");
     rootElement.setAttribute("system", systemName);
     rootElement.setAttribute("device", deviceName);
     rootElement.setAttribute("timestamp", String.valueOf(date.getTime()));
 
     //AquaponicsRepository repository = AquaponicsRepository.getInstance();
     Element temperatureElement = doc.createElement("state");
     temperatureElement.setAttribute("key", "temp");
     //System.err.println(repository.valuesMap.get(item));
     temperatureElement.setAttribute("value", AquaponicsRepository.getTemp());
     rootElement.appendChild(temperatureElement);
           
     Element phElement = doc.createElement("state");
    phElement.setAttribute("key", "ph");
     //System.err.println(repository.valuesMap.get(item));
     phElement.setAttribute("value", AquaponicsRepository.getpH());
     rootElement.appendChild(phElement);
           
     Element oxygenElement = doc.createElement("state");
     oxygenElement.setAttribute("key", "oxygen");
     //System.err.println(repository.valuesMap.get(item));
     oxygenElement.setAttribute("value", AquaponicsRepository.getOxygen());
     rootElement.appendChild(oxygenElement);
 
     doc.appendChild(rootElement);
     result.setDocument(doc);
     return result;
   }
   /**
    * Adds a value to the map.
    * @param key Item's key.
    * @param val Item's value.
    */
   @Override
   public void set(String key, String val) {
     double v = sToD(val);
     if (key.equalsIgnoreCase(temp)) {
       AquaponicsRepository.setGoalTemp(v);
       System.out.println("Temp set to" + goalTemp);
     }
     else if (key.equalsIgnoreCase(pH)) {
       AquaponicsRepository.setGoalpH(v);
       System.out.println("pH set to" + goalPH);
     }
     else if (key.equalsIgnoreCase(oxygen)) {
       AquaponicsRepository.setGoalOxygen(v);
       System.out.println("Oxygen set to" + goalOxygen);
     }
   }
   
   /**
    * Converts a String to a double.
    * @param val String to convert.
    * @return The double represented by the String.
    */
   private static Double sToD(String val) {
     double v = 0;
     try {
       v = Double.valueOf(val);
     } 
     catch (NumberFormatException e) {
       System.out.println(e);
     }
     return v;
   }
 
   /**
    * Simulates the pH changing slowly over time towards the goal state.
    * @return An updated pH value.
    */
   private double getPH() {
     double currentPH = sToD(AquaponicsRepository.getpH());
     return currentPH + (AquaponicsRepository.getGoalpH() - currentPH) 
       / 100 + mt.nextDouble(-.05,.05);
   }
 
   /**
    * Simulates the dissolved Oxygen changing slowly over time towards
    * the goal state.
    * @return An updated oxygen value.
    */
   private double getOxygen() {
     double currentDO = sToD(AquaponicsRepository.getOxygen());
     return currentDO + (AquaponicsRepository.getGoalOxygen() - currentDO) 
       / 100 + mt.nextDouble(-.01,.01);
   }
   
   /**
    * Simulates the temperature changing slowly over time.
    * @return An updated temp value.
    */
   private double getTemp() {
     double currentTemp = sToD(AquaponicsRepository.getTemp());
     return currentTemp + (AquaponicsRepository.getGoalTemp() - currentTemp) 
       / 100 + mt.nextDouble(-.1,.1);
   }
 
   
   /**
    * Simulates the outdoor temperature changing slowly over time based on
    * the time of day.
    * @return An updated temp value.
    */
  /* private double getOutdoorTemp() {
     double hour = Calendar.HOUR_OF_DAY;
     double min = Calendar.MINUTE / 60;
     hour += min;
     double baseTemp = 78.5;
     double rate = 2.5 / 12.0 + mt.nextDouble(0,.05);
     //night
     if (hour <= 6 || hour >= 18) {
       return (baseTemp - (hour % 18) * rate);
     }
     //day
     else {
       return (baseTemp + (hour - 6) * rate);
     }
   }*/
 }
