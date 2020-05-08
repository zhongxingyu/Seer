 package Jordan.bau5.FRC2115.subsystems;
 
 import edu.wpi.first.wpilibj.camera.AxisCamera;
 import edu.wpi.first.wpilibj.command.Subsystem;
 import edu.wpi.first.wpilibj.image.BinaryImage;
 import edu.wpi.first.wpilibj.image.ColorImage;
 import edu.wpi.first.wpilibj.image.CriteriaCollection;
 import edu.wpi.first.wpilibj.image.NIVision.MeasurementType;
 import edu.wpi.first.wpilibj.image.ParticleAnalysisReport;
 
 public class Camera extends Subsystem
 {
     //Red threshold values
     public static final int lowerR = 232;
     public static final int upperR = 255;
     //Number of times to iterate through removeSmallObjects
     public static final int iters1 = 2;
     public static final int iters2 = 6;
     //Min and max ratio of width to height of equivalent rectangle
     public static final float minRatio = (float) 1.0;
     public static final float maxRatio = (float) 1.35;
     //Center of mass x threshold
     public static final double xThresh = .05;
     private AxisCamera camera;
     private ColorImage image;
     private CriteriaCollection ratioc;
 
     public Camera()
     {
          camera = AxisCamera.getInstance();
          ratioc = new CriteriaCollection();
          ratioc.addCriteria(MeasurementType.IMAQ_MT_RATIO_OF_EQUIVALENT_RECT_SIDES, minRatio, maxRatio, false);
     }
 
     public int processImage()
     {
         if(camera.freshImage())
         {
             try
             {
                 image = camera.getImage();
                 BinaryImage im1 = image.thresholdRGB(0, 255, 0, 255, lowerR, upperR);
                 BinaryImage im2 = im1.removeSmallObjects(true, iters1);
                 BinaryImage im3 = im2.convexHull(false);
                 BinaryImage im4 = im3.particleFilter(ratioc);
                 BinaryImage im5 = im4.removeSmallObjects(true, iters2);
                 /*
                  * BinaryImage bigObjectsImage = thresholdImage();
                  * while(bigObjectsImage.getNumberParticles() > 4)
                  *     bigObjectsImage = bigObjectsImage.removeSmallObjects(false, 1);
                  *
                  * I feel like this is a better way of doing the same thing as is
                  * uncommented above, but I am worried that I don't see it in any
                  * example code or even other teams' code.
                  */
 
                 ParticleAnalysisReport[] reports = im5.getOrderedParticleAnalysisReports(4);
 
                 int maxIndex = 0;
                 double maxVal = -1.0;
                 for(int i = 0; i < reports.length; i++)
                 {
                     if(reports[i].center_mass_y_normalized > maxVal)
                     {
                         maxIndex = i;
                         maxVal = reports[i].center_mass_y_normalized;
                     }
                 }
 
                System.out.println("X: " + reports[maxIndex].center_mass_x_normalized +
                        "Y: " + reports[maxIndex].center_mass_y_normalized);
                 
                 im5.free();
                 im4.free();
                 im3.free();
                 im2.free();
                 im1.free();
                 image.free();
 
                if(reports[maxIndex].center_mass_x_normalized < -1 * xThresh)
                    return -1;
                else if(reports[maxIndex].center_mass_x_normalized > xThresh)
                    return 1;
                else
                    return 0;
             }
             catch(Exception e)
             {
                 System.out.println("Exception caught and ignored:");
                 System.out.println(e);
 
                 return 0;
             }
         }
         return 0;
     }
 
     protected void initDefaultCommand()
     {
     }
 }
