 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package edu.stuy.subsystems;
 
 import edu.stuy.Constants;
 import edu.stuy.util.Gamepad;
 import edu.wpi.first.wpilibj.Relay;
 import edu.wpi.first.wpilibj.Timer;
 
 /**
  * run this in both tele and auton
  * flash when disc is picked up from ground or feeder
  * flash on color to signal human player for feed white discs as we approach
  * flash different color for colored discs
  * flash pickup frequency with shooting (please clarify)
  * @author Arfan
  */
 public class Lights {
     private static Lights instance;
     
     /**
      * Lights must be wired as follows:
      * M+: Camera light
      * M-: Direction light
      */
     private Relay cameraAndDirectionLightRelay;
     
     /**
      * Lights must be wired as follows:
      * M+: White signal light
      * M-: Colored signal light
      */
     private Relay signalLightRelay;
     private int WHITE_FLASH_FREQUENCY = 7;
     private int COLORED_FLASH_FREQUENCY = 7;
     private int DIRECTION_FLASH_FREQUENCY = 10;
     private double lastTimeWhite = 0;
     private double lastTimeRed = 0;
     private double lastTimeDirection = 0;
     private boolean isWhiteOn, isRedOn, isDirectionOn;
 
     
     private Lights() {
         cameraAndDirectionLightRelay = new Relay(Constants.CAMERA_AND_DIRECTION_RELAY_CHANNEL);
         signalLightRelay = new Relay(Constants.SIGNAL_LIGHT_RELAY_CHANNEL);
         isWhiteOn = false;
         isRedOn = false;
     }
     
     public static Lights getInstance() {
         if (instance == null) {
             instance = new Lights();
         }
         return instance;
     }
     
     public void setCameraLight(boolean on) {
         Relay.Value currentVal = cameraAndDirectionLightRelay.get();
         if (on) { // Turn camera light on
             if (currentVal == Relay.Value.kOff || currentVal == Relay.Value.kForward) { // Direction light is off
                 cameraAndDirectionLightRelay.set(Relay.Value.kForward);
             }
             else { // Direction light is on
                 cameraAndDirectionLightRelay.set(Relay.Value.kOn);
             }
         }
         else { // Turn camera light off
             if (currentVal == Relay.Value.kOff || currentVal == Relay.Value.kForward) { // Direction light is off
                 cameraAndDirectionLightRelay.set(Relay.Value.kOff);
             }
             else { // Direction light is on
                 cameraAndDirectionLightRelay.set(Relay.Value.kReverse);
             }
         }
     }
     
     public void flashDirectionLight(boolean on){
         double time = Timer.getFPGATimestamp();
         if (time - lastTimeDirection > (1.0 / DIRECTION_FLASH_FREQUENCY)) {
             directionLight(!isDirectionOn);
             lastTimeDirection = time;
         }
     }
     
     public void directionLight(boolean on) {
         Relay.Value currentVal = cameraAndDirectionLightRelay.get();
         if (on) { // Turn direction light on
             if (currentVal == Relay.Value.kOff || currentVal == Relay.Value.kReverse) { // Camera light is off
                 cameraAndDirectionLightRelay.set(Relay.Value.kReverse);
             }
             else { // Camera light is on
                 cameraAndDirectionLightRelay.set(Relay.Value.kOn);
             }
         }
         else { // Turn direction light off
             if (currentVal == Relay.Value.kOff || currentVal == Relay.Value.kReverse) { // Camera light is off
                 cameraAndDirectionLightRelay.set(Relay.Value.kOff);
             }
             else { // Camera light is on
                 cameraAndDirectionLightRelay.set(Relay.Value.kForward);
             }
         }
     }
     
     public void setWhiteSignalLight(boolean on) {
         Relay.Value currentVal = signalLightRelay.get();
         if (on) { // Turn white light on
             if (currentVal == Relay.Value.kOff || currentVal == Relay.Value.kForward) { // Colored light is off
                 signalLightRelay.set(Relay.Value.kForward);
             }
             else { // Colored light is on
                 signalLightRelay.set(Relay.Value.kOn);
             }
             isWhiteOn = true;
         }
         else { // Turn white light off
             if (currentVal == Relay.Value.kOff || currentVal == Relay.Value.kForward) { // Colored light is off
                 signalLightRelay.set(Relay.Value.kOff);
             }
             else { // Colored light is on
                 signalLightRelay.set(Relay.Value.kReverse);
             }
             isWhiteOn = false;
         }
     }
     
     public void setColoredSignalLight(boolean on) {
         Relay.Value currentVal = signalLightRelay.get();
         if (on) { // Turn colored light on
             if (currentVal == Relay.Value.kOff || currentVal == Relay.Value.kReverse) { // White light is off
                 signalLightRelay.set(Relay.Value.kReverse);
             }
             else { // White light is on
                 signalLightRelay.set(Relay.Value.kOn);
             }
             isRedOn = true;
         }
         else { // Turn colored light off
             if (currentVal == Relay.Value.kOff || currentVal == Relay.Value.kReverse) { // White light is off
                 signalLightRelay.set(Relay.Value.kOff);
             }
             else { // White light is on
                 signalLightRelay.set(Relay.Value.kForward);
             }
             isRedOn = false;
         }
     }
     
     public void flashWhiteSignalLight() {
         double time = Timer.getFPGATimestamp();
         if (time - lastTimeWhite > (1.0 / WHITE_FLASH_FREQUENCY)) {
             setWhiteSignalLight(!isWhiteOn);
             lastTimeWhite = time;
         }
     }
     
     public void flashColoredSignalLight() {
         double time = Timer.getFPGATimestamp();
         if (time - lastTimeRed > (1.0 / COLORED_FLASH_FREQUENCY)) {
             setColoredSignalLight(!isRedOn);
             lastTimeRed = time;
         }
     }
     
     public void manualLightsControl(Gamepad gamepad) {
         if(gamepad.getLeftButton()) {
             flashWhiteSignalLight();
         }
         else {
             setWhiteSignalLight(false);
         }
         if(gamepad.getRightButton()) {
             flashColoredSignalLight();
         }
         else {
             setColoredSignalLight(false);
         }
     }
     
     /**
      * Go through all the logic for the lights.
      * @param gamepad Gamepad to do manual lights control with
      */
     public void runLogic(Gamepad gamepad) {
         // Various cases for lights logic that are not manual control
         if (Conveyor.getInstance().isBottomDiscDetected()) {
             flashWhiteSignalLight();
         }
        else if (Shooter.getInstance().isHopperNotEmpty()) {
             flashColoredSignalLight();
         }
         // Only control lights manually when they are not being controlled elsewhere
         else {
             manualLightsControl(gamepad);
         }
     }
 }
