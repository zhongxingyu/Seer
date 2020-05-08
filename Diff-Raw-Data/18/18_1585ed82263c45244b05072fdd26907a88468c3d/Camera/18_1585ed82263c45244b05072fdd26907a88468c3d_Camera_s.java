 package com.frc4343.robot.subsystems;
 
 import com.frc4343.robot.Constants;
 import com.frc4343.robot.Mappings;
 import edu.wpi.first.wpilibj.Relay;
 import edu.wpi.first.wpilibj.camera.AxisCamera;
 import edu.wpi.first.wpilibj.camera.AxisCameraException;
 import edu.wpi.first.wpilibj.command.Subsystem;
 import edu.wpi.first.wpilibj.image.BinaryImage;
 import edu.wpi.first.wpilibj.image.ColorImage;
 import edu.wpi.first.wpilibj.image.CriteriaCollection;
 import edu.wpi.first.wpilibj.image.NIVision.MeasurementType;
 import edu.wpi.first.wpilibj.image.NIVisionException;
 import edu.wpi.first.wpilibj.image.ParticleAnalysisReport;
 import edu.wpi.first.wpilibj.networktables2.util.List;
 import java.util.Vector;
 
 public class Camera extends Subsystem {
     AxisCamera axis;
     Relay relay;
     boolean lightState;
 
     public Camera() {
         super("Camera");
 
         System.out.println("Initializing camera.");
 
         axis = AxisCamera.getInstance("10.43.43.11");
         relay = new Relay(Mappings.CAMERA_LIGHT_RELAY);
 
         axis.writeResolution(AxisCamera.ResolutionT.k320x240);
         axis.writeCompression(Constants.CAMERA_COMPRESSION);
         axis.writeBrightness(Constants.CAMERA_BRIGHTNESS);
         axis.writeColorLevel(Constants.CAMERA_COLOUR_LEVEL);
 
         lightState = true;
     }
 
     public void initDefaultCommand() {
     }
 
     public Vector getCenterOfMass() {
         Vector centers = new Vector();
         ColorImage image = null;
         ParticleAnalysisReport[] reports = null;
 
         CriteriaCollection criteria = new CriteriaCollection();
         criteria.addCriteria(MeasurementType.IMAQ_MT_BOUNDING_RECT_WIDTH, 30, 400, false);
         criteria.addCriteria(MeasurementType.IMAQ_MT_BOUNDING_RECT_HEIGHT, 40, 400, false);
 
         try {
             axis.writeCompression(0);
             try {
                 image = axis.getImage();
             } catch (AxisCameraException e) {
             }
             axis.writeCompression(Constants.CAMERA_COMPRESSION);
 
             //Reflective tape HSL threshold.
             BinaryImage thresholdImage = image.thresholdHSV(Constants.HMIN, Constants.HMAX, Constants.SMIN, Constants.SMAX, Constants.LMIN, Constants.LMAX);
             thresholdImage.write("Image.bmp");
 
             BinaryImage bigObjectsImage = thresholdImage.removeSmallObjects(false, 2);  // remove small objects (noise)
             BinaryImage convexHullImage = bigObjectsImage.convexHull(false);  // fill in partial and full rectangles
             BinaryImage filteredImage = convexHullImage.particleFilter(criteria);  // find the rectangles
             filteredImage.write("Image2.bmp");
 
             reports = filteredImage.getOrderedParticleAnalysisReports();  // get the results
         } catch (NIVisionException e) {
         }
 
         ParticleAnalysisReport largestParticle = null;
         double highestY = 0;
 
         for (int i = 0; i < reports.length; i++) {
             ParticleAnalysisReport par = (reports[i]);
 
             if (par.center_mass_y_normalized > highestY) {
                 largestParticle = par;
                 highestY = par.center_mass_y_normalized;
             }
         }
 
         if (highestY != 0) {
             centers.addElement(new Double(largestParticle.center_mass_x_normalized));
            System.out.println(Constants.combineStringWithDouble("Center mass X: " , largestParticle.center_mass_x));
             System.out.println(Constants.combineStringWithDouble("Center mass X normalized: ", largestParticle.center_mass_x_normalized));
         }
 
         return centers;
     }
 
     public void toggleLight() {
         if (lightState) {
             System.out.println("Setting light off.");
             relay.set(Relay.Value.kOff);
         } else {
             System.out.println("Setting light on.");
             relay.set(Relay.Value.kOn);
             relay.set(Relay.Value.kForward);
         }
 
         lightState = !lightState;
     }
 }
