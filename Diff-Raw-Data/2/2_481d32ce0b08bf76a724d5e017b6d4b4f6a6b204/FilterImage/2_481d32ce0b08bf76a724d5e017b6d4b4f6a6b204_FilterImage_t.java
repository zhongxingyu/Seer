 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.slidellrobotics.reboundrumble.commands;
 
 import edu.wpi.first.wpilibj.Timer;
 import edu.wpi.first.wpilibj.image.*;
 
 /**
  *
  * @author gixxy
  */
 public class FilterImage extends CommandBase {
     private ColorImage pic;                     //
     private BinaryImage thresholdHSL = null;    //
     private int remove  = 0;                    //
     private BinaryImage bigObjectsImage = null; //
     private BinaryImage convexHullImage = null; //  Variable Constructors
     private BinaryImage filteredImage = null;   //
     CriteriaCollection cc;                      //
     boolean freePic = false;                    //
     private BinaryImage partReport = null;      //
     private int i;                              //
 
     public FilterImage() {
         requires(camera);
         System.out.println("Filter image Init");    //States that the camera initialized
     }
     
     protected void initialize(){
     }
     
     protected void execute(){
         getImage();                 //loops getting a fresh image
     }    
     
     protected boolean isFinished() {
        return false;                //never ends
     }
     
     protected void end() {
     }
     
     protected void interrupted() {
     }
 
     
     public ColorImage getImage() {
         freePic = false;            //States that the camera's picture is still held
         ParticleAnalysisReport[] reports = null;    //Creates an array of Analysis Reports
         i=0;
         
         try {
             camera.camera.writeBrightness(7);       //Sets the camera to only accept very bright light
             pic = camera.getImageFromCamera();      //Declares pic variable
            BinaryImage thresholdHSL = pic.thresholdHSL(165,185,50,90,95,110);      //Sets a Blue light threshold
             int remove = thresholdHSL.getNumberParticles() - 1;                     //Forms to leave 1 particle
             BinaryImage bigObjectsImage = thresholdHSL.removeSmallObjects(false, remove);   //Removes all but the largest particle
             BinaryImage convexHullImage = bigObjectsImage.convexHull(false);        //Fills in the bounding boxes for the targets
             BinaryImage filteredImage = convexHullImage.particleFilter(cc);     //Applies the criteria from RobotInit
             reports = filteredImage.getOrderedParticleAnalysisReports();        //Sets "reports" to the nuber of particles
             for (int i=0; i<reports.length; i++) {                          //Systematically
                 ParticleAnalysisReport r = reports[i];                      //prints the 
                 System.out.println("Particle: "+i+": Center of mass x:"+    //geometric center
                     r.center_mass_x);                                       //of mass per particle.
             }
             System.out.println(filteredImage.getNumberParticles()+" "+  //Prints the number of particles
                     Timer.getFPGATimestamp());                          //per elapsed robot time.
             
             filteredImage.free();       //
             convexHullImage.free();     //
             bigObjectsImage.free();     //Memory leak
             thresholdHSL.free();        //preventions.
             pic.free();                 //
             freePic = true;         //Sets off the FilterImage command's idFinished
         } 
         catch (NIVisionException ex) {      //
         }                                   //Catches both possible exceptions
         catch (Exception ex){               //that could be caused from above code
         }                                   //
         
         return pic;             //Returns the camera's most recent picture.
     }
 }
 
 
 /*
  * 
  * check out Brad's example code from
  * http://firstforge.wpi.edu/sf/go/doc1209;jsessionid=DE1AA2AE5AA5396063CB73C1AE17456B?nav=1
  * 
  * package edu.wpi.first.wpilibj.templates;
 
 * // to remove the comments high light all the code and press ctrl+slash
 //import com.sun.squawk.util.Arrays;
 //import com.sun.squawk.util.Comparer;
 //import edu.wpi.first.wpilibj.SimpleRobot;
 //import edu.wpi.first.wpilibj.Timer;
 //import edu.wpi.first.wpilibj.camera.AxisCamera;
 //import edu.wpi.first.wpilibj.camera.AxisCameraException;
 //import edu.wpi.first.wpilibj.image.BinaryImage;
 //import edu.wpi.first.wpilibj.image.ColorImage;
 //import edu.wpi.first.wpilibj.image.NIVisionException;
 //import edu.wpi.first.wpilibj.image.ParticleAnalysisReport;
 //
 ///**
 // *
 // * @author Greg Granito (Team 190)
 // * This sample program finds the top 3 camera images that are likely to be the goals
 // * when viewed by the axis camera. You would use this data to drive the robot towards
 // * the appropriate goal and score the tube.
 // */
 //
 ///*
 //public class CameraSample extends SimpleRobot {
 //
 //    AxisCamera camera;
 //    private ColorImage colorImage;
 //    private ParticleAnalysisReport s_particles[];
 //    private final ParticleComparer particleComparer = new ParticleComparer();
 //
 //    /**
 //     * Constructor for the sample program.
 //     * Get an instance of the axis camera.
 //     */
 //    public CameraSample() {
 //        camera = AxisCamera.getInstance();
 //    }
 //
 //    /**
 //     * the sample code runs during the autonomous period.
 //     *
 //     */
 //    public void autonomous() {
 //
 //	/**
 //	 * Run while the robot is enabled
 //	 */
 //        while (isEnabled()) {
 //            if (camera.freshImage()) {	    // check if there is a new image
 //                try {
 //                    colorImage = camera.getImage(); // get the image from the camera
 //
 //                    /**
 //		     * The color threshold operation returns a bitmap (BinaryImage) where pixels are present
 //		     * when the corresponding pixels in the source (HSL) image are in the specified
 //		     * range of H, S, and L values.
 //		     */
 //                    BinaryImage binImage = colorImage.thresholdHSL(242, 255, 36, 255, 25, 255);
 //
 //		    /**
 //		     * Find blobs (groupings) of pixels that were identified in the color threshold operation
 //		     */
 //                    s_particles = binImage.getOrderedParticleAnalysisReports();
 //
 //		    /**
 //		     * Free the underlying color and binary images.
 //		     * You must do this since the image is actually stored
 //		     * as a C++ data structure in the underlying implementation to maximize processing speed.
 //		     */
 //                    colorImage.free();
 //                    binImage.free();
 //
 //		    /**
 //		     * print the number of detected particles (color blobs)
 //		     */
 //                    System.out.println("Particles: " + s_particles.length);
 //
 //                    if (s_particles.length > 0) {
 //			/**
 //			 * sort the particles using the custom comparitor class (see below)
 //			 */
 //                        Arrays.sort(s_particles, particleComparer);
 //
 //                        for (int i = 0; i < s_particles.length; i++) {
 //                            ParticleAnalysisReport circ = s_particles[i];
 //
 //			    /**
 //			     * Compute the number of degrees off center based on the camera image size
 //			     */
 //                            double degreesOff = -((54.0 / 640.0) * ((circ.imageWidth / 2.0) - circ.center_mass_x));
 //                            switch (i) {
 //                                case 0:
 //                                    System.out.print("Best Particle:     ");
 //                                    break;
 //                                case 1:
 //                                    System.out.print("2nd Best Particle: ");
 //                                    break;
 //                                case 2:
 //                                    System.out.print("3rd Best Particle: ");
 //                                    break;
 //                                default:
 //                                    System.out.print((i + 1) + "th Best Particle: ");
 //                                    break;
 //                            }
 //                            System.out.print("    X: " + circ.center_mass_x
 //                                    + "     Y: " + circ.center_mass_y
 //                                    + "     Degrees off Center: " + degreesOff
 //                                    + "     Size: " + circ.particleArea);
 //                            System.out.println();
 //
 //                        }
 //                    }
 //                    System.out.println();
 //                    System.out.println();
 //                    Timer.delay(4);
 //                } catch (AxisCameraException ex) {
 //                    ex.printStackTrace();
 //                } catch (NIVisionException ex) {
 //                    ex.printStackTrace();
 //                }
 //            }
 //            Timer.delay(0.01);
 //        }
 //    }
 //
 //
 //    /**
 //     * compare two particles
 //     *
 //     */
 //    class ParticleComparer implements Comparer {
 //
 //        public int compare(ParticleAnalysisReport p1, ParticleAnalysisReport p2) {
 //            float p1Ratio = p1.boundingRectWidth / p1.boundingRectHeight;
 //            float p2Ratio = p2.boundingRectWidth / p2.boundingRectHeight;
 //
 //            if (Math.abs(p1Ratio - p2Ratio) < 0.1) {
 //                return -(Math.abs((p1.imageWidth / 2) - p1.center_mass_x))
 //                        - Math.abs(((p2.imageWidth / 2) - p2.center_mass_x));
 //            } else {
 //                if (Math.abs(p1Ratio - 1) < Math.abs(p2Ratio - 1)) {
 //                    return 1;
 //                } else {
 //                    return -1;
 //                }
 //            }
 //        }
 //
 //	// overloaded method because the comparitor uses Objects (not Particles)
 //        public int compare(Object o1, Object o2) {
 //            return compare((ParticleAnalysisReport) o1, (ParticleAnalysisReport) o2);
 //        }
 //    };
 //}
 //
 // 
