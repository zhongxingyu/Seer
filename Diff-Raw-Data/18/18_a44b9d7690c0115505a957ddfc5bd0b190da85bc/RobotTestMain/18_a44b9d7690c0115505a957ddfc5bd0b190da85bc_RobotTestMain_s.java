 package edu.ualberta.med.sampleProcessingRobot.serialInterface;
 
 import edu.ualberta.med.sampleProcessingRobot.serialInterface.RobotInterface.FluidLevelType;
 import edu.ualberta.med.sampleProcessingRobot.serialInterface.RobotInterface.SourceTubeSize;
 
 public class RobotTestMain {
 
     /**
      * @param args
      */
    @SuppressWarnings("unused")
     public static void main(String[] args) {
 
        String barcode = null;
         double volume = 0;
 
         // New interface
         RobotInterface robot = new RobotInterface();
         try {
             robot.initialize(SourceTubeSize.TEN_MIL,
                 FluidLevelType.FLUID_TYPE_BLOOD);
         } catch (RobotInitException cbsre) {
             System.out.println(cbsre);
             System.exit(1);
         } catch (RobotException cbse) {
             System.out.println(cbse);
             System.exit(1);
         }
 
         // Get tube
         try {
             robot.pickupSourceTube(1);
         } catch (RobotException cbsre) {
             System.out.println(cbsre);
             done(robot);
             System.exit(1);
         }
         // Read barcode
         // try {
         // barcode = robot.scanSourceTubeBarcode();
         // } catch (RobotException cbsre) {
         // System.out.println(cbsre);
         // done(robot);
         // System.exit(1);
         // }
        if (barcode == "") {
            System.out.println("Couldn't read source tube barcode");
            done(robot);
            System.exit(1);
        } else
            System.out.println("Tube barcode is: " + barcode);
         // Get fluid level
         try {
             volume = robot.scanSourceTubeLevel();
         } catch (RobotException cbsre) {
             System.out.println(cbsre);
             done(robot);
             System.exit(1);
         }
         System.out.println("Tube fluid level is: " + volume + "uL");
         // Grip source tube
         try {
             robot.gripSourceTube();
         } catch (RobotException cbsre) {
             System.out.println(cbsre);
             done(robot);
             System.exit(1);
         }
         // Get pipette tip
         try {
             robot.newPipetteTip();
         } catch (RobotException cbsre) {
             System.out.println(cbsre);
             done(robot);
             System.exit(1);
         }
         int sampleID = 1;
         // Get some liquid
         try {
             volume = robot.aspirateSample(800);
         } catch (RobotException cbsre) {
             System.out.println(cbsre);
             done(robot);
             System.exit(1);
         }
 
         try {
             robot.dispenseFTA();
             robot.dispenseFTA();
         } catch (RobotException cbsre) {
             System.out.println(cbsre);
             done(robot);
             System.exit(1);
         }
 
         // Whittle down most of the liquid
         // Only do this 6 times, regardless of the volume
         // This can be dependent on volume in the future
 
         while (volume > 3000) {
             // int count = 1;
             // while (count < 7) {
             // Get some liquid
             try {
                 volume = robot.aspirateSample(800);
             } catch (RobotException cbsre) {
                 System.out.println(cbsre);
                 done(robot);
                 System.exit(1);
             }
             // Dispense to samples
             try {
                 robot.dispensePallet(1, sampleID++, 400);
                 robot.dispensePallet(1, sampleID++, 400);
             } catch (RobotException cbsre) {
                 System.out.println(cbsre);
                 done(robot);
                 System.exit(1);
             }
             // count++;
         }
         // Clean up
         try {
             robot.disposePipetteTip();
             robot.returnSourceTube();
             robot.homeArm();
         } catch (RobotException cbsre) {
             System.out.println(cbsre);
             done(robot);
             System.exit(1);
         }
         System.out.println("Success!");
     }
 
     public static void done(RobotInterface robot) {
         try {
             robot.allDone();
         } catch (RobotException cbse) {
             System.out.println(cbse);
             System.out.println("Could not cleanly shutdown robot!");
             System.exit(1);
         }
 
     }
 
 }
