 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package util;
 import edu.wpi.first.wpilibj.Solenoid;
 /**
  *
  * @author Troy-Jameson(Trevor)
 * The class that controls the solenoids
  */
 public class Piston extends Solenoid {
     //Declares solenoids
     Solenoid solenoidOne;
     Solenoid solenoidTwo;
    /**
     * Creates the solenoids
     * @param portOne   the slot the solenoid bumper is plugged into the crio
     * @param portTwo   the ports on the solenoid bumper
     * @param portThree
     */
    public Piston(int portOne, int portTwo, int portThree) {
         super(portOne, portTwo);
         //Creates solenoids
         solenoidOne = new Solenoid(portOne, portTwo);
        solenoidTwo = new Solenoid(portOne, portThree);
     }
    /**
     * Switches the solenoids between on and off positions
     */
     public void solenoidSwitch(){
         //Switches the solenoids, one is true, one false, just switches which in is which
         if (solenoidOne.get()) {
             solenoidOne.set(false);
             
         }
         //Switches in the other instance, when solenoidTwo is true, not solenoidOne
         if (solenoidTwo.get()) {
             solenoidTwo.set(false);
         }
     }   
    /**
     * Sets the solenoids in the on position.
     */
     public void on() {
         //Sets solenoids in on position
         solenoidOne.set(true);
         solenoidTwo.set(false);
     }
    /**
     * Sets the solenoids in the off position.
     */
     public void off() {
         //Sets solenoids in off position
         solenoidOne.set(false);
         solenoidTwo.set(true);
     }
 }
