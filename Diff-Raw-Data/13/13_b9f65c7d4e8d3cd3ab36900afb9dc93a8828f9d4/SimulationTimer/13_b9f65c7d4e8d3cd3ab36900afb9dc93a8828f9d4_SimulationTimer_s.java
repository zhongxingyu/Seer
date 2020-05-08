 package edu.hawaii.ihale.housesimulator.simulationtimer;
 
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.Locale;
 import java.util.Timer;
 import java.util.TimerTask;
 import edu.hawaii.ihale.housesimulator.aquaponics.AquaponicsData;
 import edu.hawaii.ihale.housesimulator.electrical.ElectricalData;
 import edu.hawaii.ihale.housesimulator.hvac.HVACData;
 import edu.hawaii.ihale.housesimulator.lighting.LightingData;
 import edu.hawaii.ihale.housesimulator.photovoltaics.PhotovoltaicsData;
 
 /**
  * Allows user to specify how often the house should change it system states.
  * 
  * @author Anthony Kinsey, Michael Cera
  * @author Christopher Ramelb, David Lin, Leonardo Nguyen, Nathan Dorman
  */
 public class SimulationTimer {
 
   /**
    * This main method starts the timer.
    * 
    * @param args Ignored.
    * @throws Exception If problems occur.
    */
   public static void main(String[] args) throws Exception {
     // startTimer(10);
     DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.US);
     Date date = new Date();
     System.out.println(dateFormat.format(date));
   }
 
   /**
    * Starts the timer to modify the system's states.
    * 
    * @param stepValue how often the systems should change state
    */
   public static void startTimer(int stepValue) {
 
     new Timer().scheduleAtFixedRate(new TimerTask() {
       public void run() {
         DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd EEE HH:mm:ss", Locale.US);
         Date date = new Date();
         System.out.println();
         System.out.println(dateFormat.format(date));
         System.out
             .println("====================================================================="
                 + "============================================");
         System.out.format("%-25s%-25s%-25s%-25s%-25s \n", "Aquaponics", "HVAC", "PV",
             "Electrical", "Lighting");
         System.out
             .println("====================================================================="
                 + "============================================");
         AquaponicsData aqua = new AquaponicsData();
         HVACData hvac = new HVACData();
         PhotovoltaicsData pv = new PhotovoltaicsData();
         ElectricalData elec = new ElectricalData();
         LightingData light = new LightingData();
         System.out.format("%-25s%-25s%-25s%-25s%-25s \n", "Alive fish: " + aqua.getAliveFish(),
             "Inside Temp: " + hvac.getCurrentHomeTemp(), "Energy: " + pv.getEnergy(), "Energy: "
                 + elec.getEnergy(), "Living: " + light.getLivingLevel());
         System.out.format("%-25s%-25s%-25s%-25s%-25s\n", "Dead fish: " + aqua.getDeadFish(),
             "Outside Temp: " + hvac.currentOutsideTemp(), "Power: " + pv.getPower(), "Power: "
                 + elec.getPower(), "Dining: " + light.getDiningLevel());
         System.out.format("%-25s%-25s%-25s%-25s%-25s\n", "Circulation: " + aqua.getCirc(), "",
             "", "", "Kitchen: " + light.getKitchenLevel());
         System.out.format("%-25s%-25s%-25s%-25s%-25s\n", "EC: " + aqua.getEC(), "", "", "",
             "Bathroom: " + light.getBathroomLevel());
         // Couldn't use %-25 to format because of PMD pickiness...
         System.out.format("Temperature: " + aqua.getTemp() + "\n");
         System.out.format("Turbidity: " + aqua.getTurb() + "\n");
         System.out.format("Water level: " + aqua.getWaterLevel() + "\n");
         System.out.format("PH: " + aqua.getPH() + "\n");
         System.out.format("Oxygen: " + aqua.getOxygen() + "\n");

        AquaponicsData.modifySystemState();
        HVACData.modifySystemState();
        LightingData.modifySystemState();
        PhotovoltaicsData.modifySystemState();
        ElectricalData.modifySystemState();
       }
     }, 0, stepValue * 1000);
   }
 }
