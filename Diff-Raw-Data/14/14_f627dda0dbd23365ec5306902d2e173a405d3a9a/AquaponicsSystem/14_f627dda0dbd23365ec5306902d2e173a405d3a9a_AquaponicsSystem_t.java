 package edu.hawaii.systemh.housemodel.aquaponics;
 
 import edu.hawaii.systemh.housemodel.Device;
 import edu.hawaii.systemh.housemodel.EnergyConsumptionDictionary.EnergyConsumptionDevice;
 import edu.hawaii.systemh.housemodel.EnergyConsumptionDictionary.EnergyConsumptionSystem;
 import edu.hawaii.systemh.housemodel.HouseSystem;
 
 /**
  * Creates the HVAC System model for energy consumption.
  * 
  * @author Bret K. Ikehara, Leonardo Ngyuen, Kurt Teichman. 
  */
 public class AquaponicsSystem extends HouseSystem {
 
   /**
    * Default Constructor.
    */
   public AquaponicsSystem() {
     super(EnergyConsumptionSystem.AQUAPONICS.toString());
   }
 
   @Override
   protected void initDeviceValues() {
 
     /** Heater. **/
     Device heater = new Device(EnergyConsumptionDevice.AQUAPONICS_HEATER.toString());
     double[] heaterValues =  {31.5, 31.5, 31.5, 31.5, 31.5, 31.5, 31.5, 31.5, 31.5, 31.5,
                               31.5, 31.5, 31.5, 31.5, 31.5, 31.5, 31.5, 31.5, 31.5, 31.5,
                               31.5, 31.5, 31.5, 31.5};
     for (int i = 0; i < 24; i++) {
       heater.addHourEntry(i, heaterValues[i]);
     }
 
     /** Water Pump. **/
     Device waterpump = new Device(EnergyConsumptionDevice.WATER_PUMP.toString());
     double[] waterPumpValues = {31.5, 31.5, 31.5, 31.5, 31.5, 31.5, 31.5, 31.5, 31.5, 31.5,
                                 31.5, 31.5, 31.5, 31.5, 31.5, 31.5, 31.5, 31.5, 31.5, 31.5,
                                 31.5, 31.5, 31.5, 31.5};
     for (int i = 0; i < 24; i++) {
       waterpump.addHourEntry(i, waterPumpValues[i]);
     }
 
     /** Water Filter. **/
     Device waterFilter = new Device(EnergyConsumptionDevice.WATER_FILTER.toString());
     double[] waterFilterValues =  {31.5, 31.5, 31.5, 31.5, 31.5, 31.5, 31.5, 31.5, 31.5, 31.5,
                                    31.5, 31.5, 31.5, 31.5, 31.5, 31.5, 31.5, 31.5, 31.5, 31.5,
                                    31.5, 31.5, 31.5, 31.5};
     for (int i = 0; i < 24; i++) {
       waterFilter.addHourEntry(i, waterFilterValues[i]);
     }
     
     // Associated the following devices with the Aquaponics System.
     deviceMap.put(heater.getDeviceName(), heater);
     deviceMap.put(waterpump.getDeviceName(), waterpump);
     deviceMap.put(waterFilter.getDeviceName(), waterFilter);
   }
   
   /**
    * The following method describes the amount of energy required to run
    * the aquaponics system over a given period of time with regard to specifications
    * found at:
    * http://our.windowfarms.org/2011/01/21/aquaponicslite-test-
    * system-monitoring-energy-consumption-and-food-output/.
    * @param startTime the start time of the calculated interval.
    * @param endTime the end time of the calculated interval.
    * @param heaterState the boolean describing whether the heater is being turned on for
    * the given amount of time.
    * @return the required energy to run the aquaponics system for a set amount of time
    * with regard to the heater being on or off.
    */
   public double energyUsageWhenDesiredTempSet(long startTime, long endTime, 
       boolean heaterState) {  
    long timeDiff = Math.abs(endTime - startTime);
     
     // heater is turned on, multiply by 3
    return (heaterState) ? (0.0064) * 3 * timeDiff
        : (0.0064) * timeDiff;
   }
 }
