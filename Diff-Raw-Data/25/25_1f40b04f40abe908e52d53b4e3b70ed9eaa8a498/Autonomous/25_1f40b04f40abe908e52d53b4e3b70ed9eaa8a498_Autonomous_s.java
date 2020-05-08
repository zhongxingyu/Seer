 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package edu.first3729.frc2012;
 
 import edu.wpi.first.wpilibj.Relay;
 import edu.wpi.first.wpilibj.camera.AxisCamera;
 import edu.wpi.first.wpilibj.camera.AxisCameraException;
 import edu.wpi.first.wpilibj.image.*;
 import edu.wpi.first.wpilibj.communication.FRCControl;
 import edu.wpi.first.wpilibj.RobotDrive;
 
 /**
  *
  * @author matthewhaney
  */
 public class Autonomous {
     
     private RobotDrive _drive;
     private AxisCamera _camera;
     private Manipulator _manip;
     //private BinaryImage _image;
     
     private int stage = 0;
     
     public Autonomous(RobotDrive drv, AxisCamera cam, Manipulator manip)
     {
        
     }
     
     public void init()
     {
         _manip.init();
     }
     
     public boolean run()
     {
         try {
             //target();
             switch (stage)
             {
             default:
                 stage = 0;
             case 0:
                 this._manip.shoot(true);
                 stage = 1;
                 this.wait(1000);
             case 1:
                 this._manip.intake(true);
                 stage = 2;
                 this.wait(1250);
             case 2:
                 this._manip.intake(false);
                 stage = 3;
                 this.wait(500);
             case 3:
                 this._manip.intake(true);
                 stage = 4;
                 this.wait(1250);
             case 4:
                 this._manip.intake(false);
                 this._manip.shoot(false);
                 this._drive.tankDrive(0, 1);
                 stage = 5;
                 this.wait(1000);
             case 5:
                 this._drive.tankDrive(0, 0);
                 this._drive.tankDrive(-.5, -.5);
                 stage = 6;
                 this.wait(1750);
             case 6:
                 this._drive.tankDrive(0, 0);
                 this._manip.bridge(Relay.Value.kForward);
                 stage = 7;
                 this.wait(1500);
             case 7:
                 this._manip.bridge(Relay.Value.kOff);
                 this._manip.bridge(Relay.Value.kReverse);
                 stage = 8;
                 this.wait(1500);
             case 8:
                 this._manip.bridge(Relay.Value.kOff);
                 return true;
             }
         }
         catch (InterruptedException ex) {
             System.out.println("Autonomous timer interrupted!  Restarting...");
             return false;
         }
     }
     
     public void target()
     {
         /*
         try {
         this._image = this._camera.getImage().thresholdHSL(Params.target_hue_low, Params.target_hue_high, Params.target_saturation_low, Params.target_saturation_high, Params.target_luminence_low, Params.target_luminence_high);
          
         }
         catch (AxisCameraException ex) {
             
         }
         catch (NIVisionException ex) {
             
         }
         */
     }
 }
