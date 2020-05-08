 
 package edu.wpi.first.wpilibj.templates.subsystems;
 
 import edu.wpi.first.wpilibj.command.Subsystem;
 import edu.wpi.first.wpilibj.image.*;
 import edu.wpi.first.wpilibj.image.CriteriaCollection;
 import edu.wpi.first.wpilibj.camera.AxisCamera;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
 import java.lang.Math;
 
 // TODO: FIX THIS!
 /**
  *
  */
 public class Targeting extends Subsystem {
     // Put methods for controlling this subsystem
     // here. Call these from Commands.
     private static Targeting instance;
     private static final double degToRad = Math.PI / 180;
     private static AxisCamera camera;
     
     public static Targeting getInstance(){
         if(instance == null){
             instance = new Targeting();
         }
         return instance;
     }
     public Targeting(){
          camera = AxisCamera.getInstance();
 
     }
 
     public ColorImage processImage() {
         // Set the default command for a subsystem here.
         //setDefaultCommand(new MySpecialCommand());
         try{
             //AxisCamera camera = AxisCamera.getInstance();
            SmartDashboard.putString("State: ", "Getting Image");
             ColorImage image = camera.getImage();
            //BinaryImage everything = image.thresholdHSL(0, 255, 0, 255, 0, 255);
            //everything.write("/tmp/everything.jpg");
            //BinaryImage low = image.thresholdHSL(0, 125, 0, 255, 0, 255);
            //low.write("/tmp/low.jpg");
            //BinaryImage high = image.thresholdHSL(125, 255, 0, 255, 0, 255);
            //high.write("/tmp/high.jpg");
             //BinaryImage theMask = image.thresholdHSL(210, 255, 190, 220, 150, 190);
             return image;
         }
         catch(Exception e){
             e.printStackTrace();
             return null;
         }
     }
     //public double distanceTo(){
       //  BinaryImage source = filterForValidRectangles(processImage());
         //return -1;
     //}
     //public double angleTo(){
         //return -1;
     //}
     public BinaryImage filterForValidRectangles(BinaryImage particleImage) {
         try{
             CriteriaCollection rectangleCriteria = new CriteriaCollection();
             rectangleCriteria.addCriteria(NIVision.MeasurementType.IMAQ_MT_COMPACTNESS_FACTOR,
 				      50,
 				      100,
 				      false);
             rectangleCriteria.addCriteria(NIVision.MeasurementType.IMAQ_MT_RATIO_OF_EQUIVALENT_RECT_SIDES,
 				      50,
 				      100,
 				      false);
             rectangleCriteria.addCriteria(NIVision.MeasurementType.IMAQ_MT_AREA_BY_PARTICLE_AND_HOLES_AREA,
 				      64,
 				      100,
 				      false);
             return particleImage.particleFilter(rectangleCriteria);
         }
         catch(Exception e){
             e.getMessage();
             return particleImage;
         }
 }
 
 
     protected void initDefaultCommand() {
     }
 
     public void takePicture() {
         try{
             System.out.println("clic");
             AxisCamera camera = AxisCamera.getInstance();
             ColorImage image = camera.getImage();
             image.write("/tmp/picOne.jpg");
         }
         catch(Exception e){
             e.printStackTrace();
         }
 
     }
 }
 
