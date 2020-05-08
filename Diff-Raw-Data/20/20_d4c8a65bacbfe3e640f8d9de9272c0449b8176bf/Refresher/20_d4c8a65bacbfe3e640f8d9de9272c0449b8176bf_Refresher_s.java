 package edu.hawaii.ihale.housesimulator;
 
 import java.util.Timer;
 import java.util.TimerTask;
 import edu.hawaii.ihale.aquaponics.AquaponicsResource;
 import edu.hawaii.ihale.electrical.ElectricalResource;
 import edu.hawaii.ihale.hvac.HVACResource;
 import edu.hawaii.ihale.lights.BathroomLightsResource;
 import edu.hawaii.ihale.photovoltaics.PhotovoltaicResource;
 /**
  * An external timer class that updates all resources periodically.
  * @author Team Maka
  */
 public class Refresher {
   static Timer time = new Timer();;
   static AquaponicsResource aquaponics = new AquaponicsResource();
   static HVACResource hvac = new HVACResource();
   static PhotovoltaicResource solar = new PhotovoltaicResource();
   static ElectricalResource electrical = new ElectricalResource();
   static BathroomLightsResource bathroomLights = new BathroomLightsResource();
  static BathroomLightsResource kitchenLights = new BathroomLightsResource();
  static BathroomLightsResource diningroomLights = new BathroomLightsResource();
  static BathroomLightsResource livingroomLights = new BathroomLightsResource();
 
   /**
    * Constructor.
    */
   public Refresher() {
     //Empty.
   }
   
   /**
    * Starts the refresher.
    * @param mils The delay between refreshing.
    */
 
   public void start(long mils) {
     long delay = mils; //milliseconds
     long period = mils;
     time.scheduleAtFixedRate(new TimerTask() {
       public void run() {
         //lights should never change unless 
         //user sets a new value
         aquaponics.poll();
         hvac.poll();
         electrical.poll();
         bathroomLights.poll();
         kitchenLights.poll();
         livingroomLights.poll();
         diningroomLights.poll();
         solar.poll();
       }
     }, delay, period);
   }
 }
