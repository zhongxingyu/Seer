 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package edu.stuy.subsystems;
 
 import com.sun.squawk.util.MathUtils;
 import edu.stuy.Constants;
 import edu.wpi.first.wpilibj.ADXL345_I2C;
import edu.wpi.first.wpilibj.AnalogChannel;
 import edu.wpi.first.wpilibj.Talon;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
 
 /**
  *
  * @author kevin
  */
 public class Tilter {
     private static Tilter instance;
     private Talon tilter;
     private ADXL345_I2C accel;
     
     private Tilter() {
         tilter = new Talon(Constants.TILTER_CHANNEL);
         accel = new ADXL345_I2C(Constants.ACCELEROMETER_CHANNEL, ADXL345_I2C.DataFormat_Range.k16G);
     }
     
     public static Tilter getInstance() {
         if (instance == null) {
             instance = new Tilter();
         }
         return instance;
     }
     
     public void tiltUp() {
         tilter.set(1);
     }
     
     public void tiltDown() {
         tilter.set(-1);
     }
     
     public void stop() {
         tilter.set(0);
     }
     
     public double getXAcceleration() {
         return accel.getAcceleration(ADXL345_I2C.Axes.kX);
     }
     
     public double getYAcceleration() {
         return accel.getAcceleration(ADXL345_I2C.Axes.kY);
     }
     
     public double getZAcceleration() {
         return accel.getAcceleration(ADXL345_I2C.Axes.kZ);
     }
     
     public double getAbsoluteAngle() {
        return MathUtils.atan(getXAcceleration() / getZAcceleration());
        
     }
     
 }
