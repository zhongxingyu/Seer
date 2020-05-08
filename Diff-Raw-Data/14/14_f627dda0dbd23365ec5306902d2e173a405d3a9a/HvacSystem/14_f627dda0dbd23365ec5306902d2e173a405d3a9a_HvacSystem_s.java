 package edu.hawaii.systemh.housemodel.hvac;
 
 import edu.hawaii.systemh.housemodel.Device;
 import edu.hawaii.systemh.housemodel.EnergyConsumptionDictionary.EnergyConsumptionDevice;
 import edu.hawaii.systemh.housemodel.EnergyConsumptionDictionary.EnergyConsumptionSystem;
 import edu.hawaii.systemh.housemodel.HouseSystem;
 
 /**
  * Creates the HVAC System model for energy consumption.
  * 
  * @author Bret K. Ikehara, Leonardo Nguyen, Kurt Teichman.
  */
 public class HvacSystem extends HouseSystem {
 
   /** The amount of Wattage Hour to heat or cool a degree within the home. **/
   private static final double heatingCoolingEnergyUsage = 290;
   
   /**
    * Default Constructor.
    */
   public HvacSystem() {
     super(EnergyConsumptionSystem.HVAC.toString());
   }
 
   @Override
   /**
    * Initialize the energy consumption for each hour throughout the day for each respective
    * devices of the HVAC system.
    */
   protected void initDeviceValues() {
     
     /** Heating and Cooling. **/
     Device temperature = new Device(EnergyConsumptionDevice.HEATING_COOLING.toString());
     double[] tempValues = {228, 245, 274, 290, 290, 290, 0, 0, 0, 0,
                            17, 17, 17, 17, 376, 308, 211, 0, 0, 0,
                            0, 0, 0, 0};
     for (int i = 0; i < 24; i++) {
       temperature.addHourEntry(i, tempValues[i]);
     }
 
     /** Humidifier. **/
     Device humidifier = new Device(EnergyConsumptionDevice.HUMIDIFIER.toString());
     double[] humidifierValues =  {165, 165, 165, 165, 165, 165, 165, 165, 165, 165,
                                   165, 165, 165, 165, 0, 0, 0, 0, 0, 0,
                                   0, 0, 165, 165};
     for (int i = 0; i < 24; i++) {
       humidifier.addHourEntry(i, humidifierValues[i]);
     }
     
     // Associated the following devices with the HVAC System.
     deviceMap.put(temperature.getDeviceName(), temperature);
     deviceMap.put(humidifier.getDeviceName(), humidifier);
   }
   
   /**
    * Returns the amount of energy load it would take to heat or cool the home to a specified
    * temperature dependent on the current home temperature and the outside home temperature.
    * We are using a log function to accurately show that the energy to change the temperature
    * will be bounded by a maximum amount if the there is a huge change required by the user
    * to set the desired temperature.
    * 
    * @param currentHomeTemp The current home temperature.
    * @param desiredHomeTemp The desired temperature to have the HVAC system maintain the home at.
    * @param outsideHomeTemp The temperature outside the home.
    * @return The amount of energy load it would take to make the current home temperature reflect
    *         that of the desired home temperature specified.
    */
   public double energyUsageWhenDesiredTempSet(int currentHomeTemp, int desiredHomeTemp, 
       int outsideHomeTemp) {
     
    long tempDiff = Math.abs(currentHomeTemp - desiredHomeTemp);
     double energyUsagePerHour = (heatingCoolingEnergyUsage) * Math.log10(tempDiff) + 4;
 
     return energyUsagePerHour;
   }
 }
