 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package farmington.ultimateascent;
 
 import edu.wpi.first.wpilibj.DigitalInput;
 import edu.wpi.first.wpilibj.Relay;
 
 /**
  * Controls the loader wheel and limit switch system.
  * @author 3414
  */
 public class ShooterLoader implements IRobot {
     
     private Relay loaderWheel;
     private DigitalInput loaderSensor;
     private DigitalInput chamberSideSensor;
     private DigitalInput chamberBottomSensor;
     private boolean waiting;
     private Waiter loaderControl;
    private boolean firstFrisbee;
     
     /**
      * Main constructor for ShooterLoader.
      * @param loaderWheelRelay relay slot for the loader wheel relay
      * @param loaderSensorSlot DIO slot for the limit switch
      */
     public ShooterLoader(int loaderWheelRelay, int loaderSensorSlot, int chamberSideSensorSlot, int chamberBottomSensorSlot) {
         loaderWheel = new Relay(loaderWheelRelay);
         loaderSensor = new DigitalInput(loaderSensorSlot);
         chamberSideSensor = new DigitalInput(chamberSideSensorSlot);
         chamberBottomSensor = new DigitalInput(chamberBottomSensorSlot);
         loaderControl = new Waiter();
         waiting = false;
        firstFrisbee = true;
     }
     
     public void updateLoader(boolean manualTrigger) {
         
         if (manualTrigger) {
             this.turnOn();
         } else {
             this.turnOff();
             
             
             /*
              * This checks to see if a frisbee is not detected constantly for 200ms.
              * If a frisbee has not been detected at the end of this time, it loads
              * one from the loader. This is to prevent double-loading because of jostling
              */
             if (loaderSensor.get()) {
                 // If there is not a frisbee in the chamber, start waiting
                if (!getChamberSensors() && !waiting && !firstFrisbee) {
                     loaderControl.waitXms(700);   //DEBUG: 200 ms when the old sensor works again
                     waiting = true;
                } else if (firstFrisbee) {
                    waiting = true;
                    firstFrisbee = false;
                 }
                 // If we are waiting and a frisbee appears, cancel the wait.
                 if (loaderControl.isWaiting() && getChamberSensors()) {
                     waiting = false;
                     loaderControl.reset();
                 }
                 /* 
                  * If we reach the end of the wait and the timer has not been cancelled, start loading.
                  * Load until a frisbee is detected in the chamber, then reset the logic to detect missing frisbees
                  */
                 if (loaderControl.timeUp() && waiting) {
                     if (!getChamberSensors()) {
                         this.turnOn();
                     } else {
                         waiting = false;
                     }
                 }
             } else {
                 this.turnOn();
             }
         }
     }
     
     public void manualControl(boolean manualTrigger) {
         
         if (manualTrigger) {
             this.turnOn();
         } else {
             this.turnOff();
         }
     }
     
     public void turnOn() {
         loaderWheel.set(RELAY_FORWARD);
     }
     
     public void turnOff() {
         loaderWheel.set(RELAY_OFF);
     }
     
     public boolean getChamberSensors() {
         return getChamberSideSensor() || getChamberBottomSensor();
     }
     
     public boolean getChamberSideSensor() {
         //Inverted due to the side sensor wiring
         return !chamberSideSensor.get();
     }
     
     public boolean getChamberBottomSensor() {
         //possibly inverted? needs to be tested
         return !chamberBottomSensor.get();
     }
     
     public boolean getLoaderSensor() {
         return loaderSensor.get();
     }
     
     public void reset() {
         loaderControl.reset();
     }
 }
