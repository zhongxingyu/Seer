 package edu.hawaii.ihale.frontend.page;
 
 import java.io.Serializable;
 import edu.hawaii.ihale.frontend.SolarDecathlonApplication;
 import edu.hawaii.ihale.frontend.page.aquaponics.AquaponicsListener;
 //import edu.hawaii.ihale.frontend.page.energy.ElectricalListener;
 //import edu.hawaii.ihale.frontend.page.hvac.HvacListener;
 //import edu.hawaii.ihale.frontend.page.lighting.LightsListener;
 
 /**
  * SystemChecker is a class that will check current values of
  * the system listeners to see if they are with in desired ranges.
  * @author kurtteichman
  */
 public class SystemChecker implements Serializable {
   /** */
   private static final long serialVersionUID = 1L;
   /** boolean to describe if aquaponics is malfunctioning. */
   public boolean aquaponicsError = false;
   
   /**
    * Method to check to see if indeed an error was found.
    * @return boolean that describes if an error was found
    */
   public boolean foundError () {
     // alludes to how we will implement the other error catching
     return aquaponicsError();
   }
   
   /**
    * Method to get the name of an erroneous system. Will eventually
    * create a list of erroneous systems.
    * @return A string representing the erroneous system.
    */
  public String getErroroneousSystem() {
     if (aquaponicsError) {
       return "Aquaponics";
     }
     
     return null;
   }
   
   /**
    * Method to check whether the aquaponics system is malfunctioning.
    * @return boolean describing whether there is an error associated
    * with the aquaponics system.
    */
  private boolean aquaponicsError() {
     AquaponicsListener aquaponicsListener =
       SolarDecathlonApplication.getAquaponics();
     int temp = aquaponicsListener.getTemp();
     //int deadFish = aquaponicsListener.getDeadFish();
     double pH = aquaponicsListener.getPH();
     //double circulation = aquaponicsListener.getCirculation();
     //double nutrients = aquaponicsListener.getNutrients();
     //double turbidity = aquaponicsListener.getTurbidity();
     //double conductivity = aquaponicsListener.getConductivity();
     double oxygen = aquaponicsListener.getOxygen();
     //int waterLevel = aquaponicsListener.getWaterLevel();
     
     if (temp < 55 || temp > 65) {
       aquaponicsError = true;
     }
     else if (pH < 6.5 || pH > 7.5) {
       aquaponicsError = true;
     }
     else if (oxygen < 4.5 || oxygen > 5.5) {
       aquaponicsError = true;
     }
     /*
     else if (deadFish > 0) {
       aquaponicsError = true;
     }
     */
     else {
       aquaponicsError = false;
     }
     
     return aquaponicsError;
   }
   
   //private synchronized static boolean checkAquaponics() {
     //return aquaponicsError;
   //}
   /*
   private boolean hvacError() {
     HvacListener hvacListener =
       SolarDecathlonApplication.getHvac();
     int temp = hvacListener.getTemp();
     return false;
   }
   
   private boolean energyError() {
     ElectricalListener electricalListener =
       SolarDecathlonApplication.getElectrical();
     long energy = electricalListener.getEnergy();
     long power = electricalListener.getPower();
     return false;
   }
   */
 }
