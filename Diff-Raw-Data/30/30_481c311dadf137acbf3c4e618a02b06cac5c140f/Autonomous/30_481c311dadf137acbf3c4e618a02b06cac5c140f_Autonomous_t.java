 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package edu.stuy;
 
 import edu.stuy.subsystems.Acquirer;
 import edu.stuy.subsystems.Conveyor;
 import edu.stuy.subsystems.Drivetrain;
 import edu.stuy.subsystems.Shooter;
 
 /**
  * This is the location of the autonomous routines.
  * We assume that we always begin touching the front edge of the pyramid.
  * This gives us two discs.
  * @author Eric
  */
 public class Autonomous {
     
     public static void run(int setting) {
         switch (setting) {
             case 1: 
                 auton1();
                 break;
             case 2: 
                 auton2();
                 break;
             case 3:
                 auton3();
                 break;
             case 4:
                 auton4();
                 break;
             case 5:
                 auton5();
                 break;
             case 6:
                 auton6();
                 break;
             case 7:
                 auton7();
                 break;
             case 8:
                 auton8();
                 break;
             case 9:
                 auton9();
                 break;
             case 10:
                 auton10();
                 break;
             case 11:
                 auton11();
                 break;
         }
     }
     
     /**
      * Start middle in front of pyramid
      * CV shoot 2
      * back up acquire both sets of disks going back to center line. 
      * Don't stop for pickup. Return to in front of pyramid. 
      * Stop acquiring on the way back. Shoot the 4 acquired disks.
      */
     public static void auton1() {
         // add CV
         Conveyor.getInstance().conveyAutomatic();
         Shooter.getInstance().shoot();
         Acquirer.getInstance().acquire();
         Drivetrain.getInstance().forwardInchesRough(-240);
         Acquirer.getInstance().stop();
         Drivetrain.getInstance().forwardInchesRough(240);
         Shooter.getInstance().shoot();
     }
     
     /**
      * Same as 1 but no CV at first.
      */
     public static void auton2() {
         Conveyor.getInstance().conveyAutomatic();
         Shooter.getInstance().shoot();
         Acquirer.getInstance().acquire();
         Drivetrain.getInstance().forwardInchesRough(-240);
         Acquirer.getInstance().stop();
         Drivetrain.getInstance().forwardInchesRough(240);
         Shooter.getInstance().shoot();
     }
     
     /**
      * Start in front of middle pyramid shoot 2 with CV go back under pyramid pick up 2 drive forward and fire those two with CV.
      */
     public static void auton3() {
         // add CV
         Conveyor.getInstance().conveyAutomatic();
         Shooter.getInstance().shoot();
         Drivetrain.getInstance().forwardInchesRough(-85);
         Acquirer.getInstance().acquire();
         Drivetrain.getInstance().forwardInchesRough(85);
         Shooter.getInstance().shoot();
     }
     
     /**
      * Same as 3 no CV at start.
      */
     public static void auton4() {
         Conveyor.getInstance().conveyAutomatic();
         Shooter.getInstance().shoot();
         Drivetrain.getInstance().forwardInchesRough(-85);
         Acquirer.getInstance().acquire();
         Drivetrain.getInstance().forwardInchesRough(85);
         Shooter.getInstance().shoot();     
     }
     
     /** 
      * Start anywhere fire 2. CV.
      */
     public static void auton5() {
         // add CV
         Conveyor.getInstance().conveyAutomatic();
         Shooter.getInstance().shoot();
     }
     
     /**
      * Same as 5 no CV.
      */
     public static void auton6() {
         Conveyor.getInstance().conveyAutomatic();
         Shooter.getInstance().shoot();
     }
     
     /**
      * Same as 1 but back up to center at end of field.
      */
     public static void auton7() {
         // add CV
         Conveyor.getInstance().conveyAutomatic();
         Shooter.getInstance().shoot();
         Acquirer.getInstance().acquire();
         Drivetrain.getInstance().forwardInchesRough(-240);
         Acquirer.getInstance().stop();
         Drivetrain.getInstance().forwardInchesRough(240);
         Shooter.getInstance().shoot();
     }
     
     /**
      * Same as 3 but back up to center of field. 
      */
     public static void auton8()  {
         // add CV
         Conveyor.getInstance().conveyAutomatic();
         Shooter.getInstance().shoot();
         Drivetrain.getInstance().forwardInchesRough(-240);
         Acquirer.getInstance().acquire();
         Drivetrain.getInstance().forwardInchesRough(85);
         Shooter.getInstance().shoot();
     }
     
     /**
      * Same as 1 with 180 spin at end to face the disks.
      */
     public static void auton9() {
      
     }
     
     /**
      * Same as 3 180 spin at end to face the disks. 
      */
     public static void auton10() {
        
     }
     
     /**
      * Start at back of pyramid shoot 3, back up pickup frisbees, return and shoot 4.
      * You might have 4 depending on how many people have left there from other robots.
      */
     public static void auton11() {
         Conveyor.getInstance().conveyAutomatic();
         Shooter.getInstance().shoot();
        Drivetrain.getInstance().forwardInchesRough(-Constants.LINE_TO_PYRAMID);
         Acquirer.getInstance().acquire();
        Drivetrain.getInstance().forwardInchesRough(Constants.LINE_TO_PYRAMID);
         Acquirer.getInstance().acquire();
     }
 }
