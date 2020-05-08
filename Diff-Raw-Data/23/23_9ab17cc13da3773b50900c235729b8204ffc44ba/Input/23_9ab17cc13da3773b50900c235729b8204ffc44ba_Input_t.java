 package edu.wpi.first.wpilibj.templates;
 
 import edu.wpi.first.wpilibj.DigitalInput;
 import edu.wpi.first.wpilibj.Gyro;
 import edu.wpi.first.wpilibj.camera.AxisCamera;
 import edu.wpi.first.wpilibj.camera.AxisCameraException;
 import edu.wpi.first.wpilibj.image.BinaryImage;
 import edu.wpi.first.wpilibj.image.ColorImage;
 import edu.wpi.first.wpilibj.image.CriteriaCollection;
 import edu.wpi.first.wpilibj.image.LinearAverages;
 import edu.wpi.first.wpilibj.image.NIVision;
 import edu.wpi.first.wpilibj.image.NIVisionException;
 import edu.wpi.first.wpilibj.image.ParticleAnalysisReport;
 import edu.wpi.first.wpilibj.image.RGBImage;
 
 /**
  * Input Class. Contains methods to return info about specific inputs.
  *
  * @author 3157
  */
 public class Input {
 
     public static final int XMAXSIZE = 24;
     public static final int XMINSIZE = 24;
     public static final int YMAXSIZE = 24;
     public static final int YMINSIZE = 48;
     public static final double xMax[] = {1, 1, 1, 1, .5, .5, .5, .5, .5, .5, .5, .5, .5, .5, .5, .5, .5, .5, .5, .5, 1, 1, 1, 1};
     public static final double xMin[] = {.4, .6, .1, .1, .1, .1, .1, .1, .1, .1, .1, .1, .1, .1, .1, .1, .1, .1, .1, .1, .1, .1, 0.6, 0};
     public static final double yMax[] = {1, 1, 1, 1, .5, .5, .5, .5, .5, .5, .5, .5, .5, .5, .5, .5, .5, .5, .5, .5, 1, 1, 1, 1};
     public static final double yMin[] = {.4, .6, .05, .05, .05, .05, .05, .05, .05, .05, .05, .05, .05, .05, .05, .05, .05, .05, .05, .05, .05, .05,
         .05, .05, .05, .05, .05, .05, .05, .05, .05, .05, .05, .05, .05, .05, .05, .05, .05, .05, .05, .05, .05, .05,
         .05, .05, .6, 0};
     // Camera and CC fields
     public static AxisCamera camera;                    // Axis camera object (connected to the switch)
     public static CriteriaCollection cc;                // Criteria for particle filtering
     public static final int RECTANGULARITY_LIMIT = 60;  // Rectangularity limit
     public static final int ASPECT_RATIO_LIMIT = 75;    // Aspect ratio limit
     public static final int X_EDGE_LIMIT = 40;          // X-Edge limit
     public static final int Y_EDGE_LIMIT = 60;          // Y-Edge limit
     public static final int X_IMAGE_RES = 320;          // Image resolution in pixels. Should be 160, 320 or 640
     public static final double VIEW_ANGLE = 48;         // Axis M1011 camera
     //public static final double VIEW_ANGLE = 43.5;       // Axis 206 camera
     // Joystick Fields
     public static EastridgeJoystick rightDriverStick;
     public static EastridgeJoystick leftDriverStick;
     public static EastridgeJoystick coDriverStick;
     public static boolean bTriggerDown;
     public static boolean bAim;
     public static boolean bSlowSpeedRight;
     public static boolean bSlowSpeedLeft;
     public static boolean bStopGyro;
     public static double dHook;
     public static boolean bClimbExtend;
     public static boolean bClimbRetract;
     public static double leftX;
     public static double rightX;
     public static double leftY;
     public static double rightY;
     public static double dTimeUntilLoad = 2.0;
     public static int iDriverPortRight = 1;
     public static int iDriverPortLeft = 2;
     public static int iDriverPortCo = 3;
     public static Gyro gyro; // The Gyro
     //public static CameraData image;
     public static DigitalInput kickerSwitch;
     public static DigitalInput eotExtended;
     public static DigitalInput eotRetracted;
     public static DigitalInput hookVertical;
     public static ScreenOutput out = new ScreenOutput();
     public static boolean bNextTargetButton;
     public static boolean bPrevTargetButton;
     public static boolean bLeftLoadButton;
     public static boolean bRightLoadButton;
     /**
      * Scores. Subclass for scoring fields
      */
     public static class Scores {
 
         static double rectangularity;   // Rectangularity of the object
         static double aspectRatioInner; // Inner aspect ratio of the object
         static double aspectRatioOuter; // Outer aspect ratio of the object
         static double xEdge;            // X-Edge of the object
         static double yEdge;            // Y-Edge of the object
         
     }
 
     /**
      * initGyro. Creates a new Gyro object
      */
     public static void initGyro() {
         gyro = new Gyro(1);
     }
 
     /**
      * initVision. Creates the objects needed for vision (Camera,
      * CriteriaCollection)
      *
      * @param doCamera Whether or not you want to init the camera. (pass false
      * for CC testing)
      */
     public static void initVision(boolean doCamera) {
         if (doCamera) {
             try {
                 if (FRCTimer.DelayDone(dTimeUntilLoad)){
                     camera = AxisCamera.getInstance();  // Get an instance of the camera
                     camera.writeResolution(AxisCamera.ResolutionT.k320x240);
                     camera.writeBrightness(0);
                 }
             } catch(Exception e)
             {
                 Output.display.screenWrite("An error occured while initializing the camera. Disabling.");
                 FRCFile.bEnableCamera = false;
             }
         }
         cc = new CriteriaCollection();      // Create the criteria for the particle filter
         cc.addCriteria(NIVision.MeasurementType.IMAQ_MT_AREA, 500, 65535, false); // Add criteria to particle filter
     }
 
     /**
      * initJoystick. Creates the two joysticks.
      */
     public static void initJoystick() {
         rightDriverStick = new EastridgeJoystick(iDriverPortRight);
         leftDriverStick = new EastridgeJoystick(iDriverPortLeft);
         coDriverStick = new EastridgeJoystick(iDriverPortCo);
     }
 
     /**
      * getGyro.
      *
      * @return Returns the angle of the gyro in degrees
      */
     public static double getGyro() {
         return gyro.getAngle();
     }
 
     /**
      * resetGyro. Resets the state of the gyro
      */
     public static void resetGyro() {
         gyro.reset();
     }
 
     /**
      * getTarget.
      *
      * @param useStored Pass true if you just want to use a stored image
      * @param storeImages Pass true if you want to store all the images to the
      * cRIO
      * @param outputDebug Pass true if you want to output debug messages for
      * each blob found
      * @return Returns target data in the form of CameraData
      */
     public static CameraData getTarget(boolean useStored, boolean storeImages, boolean outputDebug) throws AxisCameraException {
         CameraData CD = null;
         
         BinaryImage thresholdImage = null;
         BinaryImage convexHullImage = null;
         BinaryImage filteredImage = null;
         ColorImage image = null;
 
         try {
             ParticleAnalysisReport high = null;
             ParticleAnalysisReport lowLeft = null;
             ParticleAnalysisReport lowRight = null;
             double highDistance = 0;
             double lowDistanceLeft = 0;
             double lowDistanceRight = 0;
 
             /**
              * Do the image capture with the camera and apply the algorithm
              * described above. This sample will either get images from the
              * camera or from an image file stored in the top level directory in
              * the flash memory on the cRIO. The file name in this case is
              * "testImage.jpg"
              */
             if (!useStored) {
                 image = camera.getImage();
                 if (storeImages)
                     image.write("/original.bmp");
             } else {
                 image = new RGBImage("/testImage.jpg"); // Get the sample image from the cRIO flash
             }
 
             //BinaryImage thresholdImage = image.thresholdHSV(60, 100, 90, 255, 20, 255); // Keep only red objects
             thresholdImage = image.thresholdHSV(40, 190, 90, 255, 0, 255); // Keep only red objects
             if (storeImages) {
                 thresholdImage.write("/threshold.bmp");
             }                // Save the threshold image
 
             convexHullImage = thresholdImage.convexHull(false);             // Fill in occluded rectangles
             if (storeImages) {
                 convexHullImage.write("/convexHull.bmp");
             }              // Write the convexhull image
 
             filteredImage = convexHullImage.particleFilter(cc);             // Filter out small particles
             if (storeImages) {
                 filteredImage.write("/filteredImage.bmp");
             }             // Write the filtered image
 
             // Iterate through each particle and score to see if it is a target
             Scores scores[] = new Scores[filteredImage.getNumberParticles()];
             for (int i = 0; i < scores.length; i++) {
                 ParticleAnalysisReport report = filteredImage.getParticleAnalysisReport(i);
                 scores[i] = new Scores();
                 scores[i].rectangularity = scoreRectangularity(report);
                 scores[i].aspectRatioOuter = scoreAspectRatio(filteredImage, report, i, true);
                 scores[i].aspectRatioInner = scoreAspectRatio(filteredImage, report, i, false);
                 scores[i].xEdge = scoreXEdge(thresholdImage, report);
                 scores[i].yEdge = scoreYEdge(thresholdImage, report);
 
                 if (scoreCompare(scores[i], false)) {
                     high = report;
                     highDistance = computeDistance(thresholdImage, report, i, false);
                 } else if (scoreCompare(scores[i], true)) {
                     if (lowLeft == null && lowRight == null) {
                         lowLeft = report;
                         lowDistanceLeft = computeDistance(thresholdImage, report, i, true);
                     } else if (lowRight == null) {
                         if (lowLeft.center_mass_x_normalized > report.center_mass_x_normalized) {
                             lowRight = lowLeft;
                             lowDistanceRight = lowDistanceLeft;
                             lowLeft = report;
                             lowDistanceLeft = computeDistance(thresholdImage, report, i, true);
                         } else {
                             lowRight = report;
                             lowDistanceRight = computeDistance(thresholdImage, report, i, true);
                         }
                     }
                 }
 
                 if (outputDebug) {
                     if (scoreCompare(scores[i], false)) {
                         System.out.println("particle: " + i + "is a High Goal  centerX: " + report.center_mass_x_normalized + "centerY: " + report.center_mass_y_normalized);
                         System.out.println("Distance: " + computeDistance(thresholdImage, report, i, false));
                     } else if (scoreCompare(scores[i], true)) {
                         System.out.println("particle: " + i + "is a Middle Goal  centerX: " + report.center_mass_x_normalized + "centerY: " + report.center_mass_y_normalized);
                         System.out.println("Distance: " + computeDistance(thresholdImage, report, i, true));
                     } else {
                         System.out.println("particle: " + i + "is not a goal  centerX: " + report.center_mass_x_normalized + "centerY: " + report.center_mass_y_normalized);
                     }
                     System.out.println("rect: " + scores[i].rectangularity + "ARinner: " + scores[i].aspectRatioInner);
                     System.out.println("ARouter: " + scores[i].aspectRatioOuter + "xEdge: " + scores[i].xEdge + "yEdge: " + scores[i].yEdge);
                 }
             }
 
             CD = new CameraData(high, highDistance, lowLeft, lowDistanceLeft, lowRight, lowDistanceRight);
 
             // Free the memory for each image.
             System.out.println("Freeing images!");
             filteredImage.free();
             convexHullImage.free();
             thresholdImage.free();
             image.free();
             System.out.println("Done Freeing images!");
 
         //} catch (AxisCameraException ex) {
         //    ex.printStackTrace();
         //} catch (NIVisionException ex) {
         //    ex.printStackTrace();
         }
         catch(Exception ext) {
             ScreenOutput scrOut = new ScreenOutput();
             //scrOut.screenWrite("Unhandled Error: " + ext.getMessage(),2);
             ext.printStackTrace();
         } finally {
             if(filteredImage != null) {
                 try {
                     filteredImage.free();
                 } catch (NIVisionException ex) {
                     System.out.println("Cannot Free filtered image in catch!");
                 }
             }
             if(convexHullImage != null) {
                 try {
                     convexHullImage.free();
                 } catch (NIVisionException ex) {
                     System.out.println("Cannot Free convex hull image in catch!");
                 }
             }
             if(thresholdImage != null) {
                 try {
                     thresholdImage.free();
                 } catch (NIVisionException ex) {
                     System.out.println("Cannot Free threshold image in catch!");
                 }
             }
             if(image != null) {
                 try {
                     image.free();
                 } catch (NIVisionException ex) {
                     System.out.println("Cannot Free image in catch!");
                 }
             }
         }
         return CD;
     }
 
     /**
      * initSwitch.
      * Initializes the switch...
      */
     public static void initSwitch(){
         kickerSwitch = new DigitalInput(1);
         eotExtended = new DigitalInput(2);
         eotRetracted = new DigitalInput(3);
         hookVertical = new DigitalInput(4);
     }
     
     /**
      * getKickerSwitchValue.
      * Gets the value of the kicker switch.
      * 
      * @return Returns the value of the kicker switch.
      */
     public static boolean getKickerSwitchValue(){
         return kickerSwitch.get();
     }
     /**
      * getExtendedValue.
      * gets value of extended climbing arm
      * @return Returns the value of the extended arm
      */
     public static boolean getExtendedValue(){
         return eotExtended.get();
     }
      /**
       * getRetractedValue.
       * gets the value of the retracted climbing arm
       * @return Returns the value of retracted arm
       */
     public static boolean getRetractedValue(){
         return eotRetracted.get();
     }
      /**
       * getHookVerticalValue.
       * gets the value of the vertical hook
       * @return Returns the value of vertical hook
       */
     public static boolean getHookVerticalValue(){
         return hookVertical.get();
     }
     
     /**
      * getRightX.
      * Prints and returns the value of the right stick's x axis.
      * @return Returns the x value of the right stick
      */
     public static double getRightX() {
         //out.screenWrite("RIGHTX: " + rightDriverStick.getX());
         return rightDriverStick.getX();
     }
 
     /**
      * getLeftX.
      * Prints and returns the value of the left stick's x value.
      * @return Returns the x value of the left stick
      */
     public static double getLeftX() {
         //out.screenWrite("LEFTX: " + leftDriverStick.getX());
         return leftDriverStick.getX();
     }
 
     /**
      * getLeftY.
      * Prints and returns the value of the left sticks y axis.
      * @return Returns the y value of the left stick
      */
     public static double getLeftY() {
         //out.screenWrite("LEFTY: " + leftDriverStick.getY());
         return leftDriverStick.getY();
     }
 
     /**
      * getRightY.
      * Prints and returns the value of the right sticks y axis.
      * @return Returns the y value of the right stick
      */
     public static double getRightY() {
         //out.screenWrite("RIGHTY: " + rightDriverStick.getY());
         return rightDriverStick.getY();
     }
     /**
      * getStopGyro.
      * Stops the gyro
      * @return State of the button
      */
     public static boolean getStopGyro(){
        return rightDriverStick.isPressed(3) || leftDriverStick.isPressed(3);
     }
     
     
     /**
      * getNextTargetButton.
      * Returns the state of the next target button.
      * @return State of the button.
      */
     public static boolean getNextTargetButton() {
         return coDriverStick.isPressed(9);
     }
     
     /**
      * getPrevTargetButton.
      * Returns the state of the previous target button.
      * @return State of the button.
      */
     public static boolean getPrevTargetButton() {
         return coDriverStick.isPressed(8);
     }
 
     /**
      * getAim.
      * Returns the state of the aim button on the left stick.
      * @return State of the aim button.
      */
     public static boolean getAim() {
         return leftDriverStick.getTrigger() || rightDriverStick.getTrigger();
     }
 
     /**
      * getSlowSpeedRight.
      * Returns the state of the slow speed button on the right stick.
      * @return State of the button.
      */
     public static boolean getSlowSpeedRight() {
         return rightDriverStick.isPressed(2);
     }
 
     /**
      * getSlowSpeedLeft.
      * Returns the state of the slow speed button on the left stick.
      * @return State of the button.
      */
     public static boolean getSlowSpeedLeft() {
         return leftDriverStick.isPressed(2);
     }
 
     /**
      * getHook.
      * Returns the joystick position on the Y axis.
      * @return State of the button.
      */
     public static double getHook() {
         return coDriverStick.getY();
     }
 
     /**
      * getClimbExtend.
      * Returns the state of the left climb 2 button.
      * @return State of the button.
      */
     public static boolean getClimbExtend() {
         return coDriverStick.isPressed(6) || coDriverStick.isPressed(11);
     }
 
     /**
      * getClimbRight.
      * Returns the state of the right climb 2 button.
      * @return State of the button.
      */
     public static boolean getClimbRetract() {
         return coDriverStick.isPressed(7) || coDriverStick.isPressed(10);
     }
     /**
      * getLoadButtonLeft.
      * Returns the state of the left load button
      * @return State of the button
      */
     public static boolean getLoadButtonLeft(){
         return leftDriverStick.isPressed(6);
     }
      
     /**
      * getLoadButtonRight.
      * Returns the state of the right load button
      * @return State of the button
      */
     public static boolean getLoadButtonRight(){
         return rightDriverStick.isPressed(6);
     }
     /**
      * cameraLightButtonOn.
      * Returns the state of the camera light on button.
      * @return State of the button.
      */
     public static boolean cameraLightButtonOn() {
         return rightDriverStick.isPressed(10) || leftDriverStick.isPressed(10);
     }
 
     /**
      * cameraLightButtonOff.
      * Returns the state of the camera light off button.
      * @return State of the button.
      */
     public static boolean cameraLightButtonOff() {
         return rightDriverStick.isPressed(11) || leftDriverStick.isPressed(11);
     }
 
     /**
      * gatherInput.
      * Updates each local variable to match the state of inputs
      */
     public static void gatherInput() {
         leftX = getLeftX();
         leftY = getLeftY();
         rightX = getRightX();
         rightY = getRightY();
         bTriggerDown = getAim();
         bAim = getAim();
         bSlowSpeedRight = getSlowSpeedRight();
         bSlowSpeedLeft = getSlowSpeedLeft();
         dHook = getHook();
         bClimbExtend = getClimbExtend();
         bClimbRetract = getClimbRetract();
         bNextTargetButton = getNextTargetButton();
         bPrevTargetButton = getPrevTargetButton();
         bStopGyro = getStopGyro();
         bLeftLoadButton = getLoadButtonLeft();
         bRightLoadButton = getLoadButtonRight();
         
         if(bNextTargetButton) {
             if(Think.currentTarget == 0){
                 Think.currentTarget = 1;
             }
             if(Think.currentTarget == 1){
                 Think.currentTarget = 2;
             }    
             if(Think.currentTarget == 2){
                 Think.currentTarget = 0;
             }    
         }
         if(bPrevTargetButton) {
             if(Think.currentTarget == 0){
                 Think.currentTarget = 2;
             }    
             if(Think.currentTarget == 1){
                 Think.currentTarget = 0;
             }    
             if(Think.currentTarget == 2){
                 Think.currentTarget = 1;
             }    
         }
         
         Output.setCameraLight(bAim);
     }
 
     /**
      * computeDistance. Computes the estimated distance to a target using the
      * height of the particle in the image. For more information and graphics
      * showing the math behind this approach see the Vision Processing section
      * of the ScreenStepsLive documentation.
      *
      * @param image The image to use for measuring the particle estimated
      * rectangle
      * @param report The Particle Analysis Report for the particle
      * @param particleNumber Which particle to compute
      * @param outer True if the particle should be treated as an outer target,
      * false to treat it as a center target
      * @return The estimated distance to the target in Inches.
      * @throws NIVisionException
      */
     public static double computeDistance(BinaryImage image, ParticleAnalysisReport report, int particleNumber, boolean outer) throws NIVisionException {
         double rectShort, height;
         int targetHeight;
 
         rectShort = NIVision.MeasureParticle(image.image, particleNumber, false, NIVision.MeasurementType.IMAQ_MT_EQUIVALENT_RECT_SHORT_SIDE);
         //using the smaller of the estimated rectangle short side and the bounding rectangle height results in better performance
         //on skewed rectangles
         height = Math.min(report.boundingRectHeight, rectShort);
         targetHeight = outer ? 29 : 21;
 
         return X_IMAGE_RES * targetHeight / (height * 12 * 2 * Math.tan(VIEW_ANGLE * Math.PI / (180 * 2)));
     }
 
     /**
      * scoreAspectRatio. Computes a score (0-100) comparing the aspect ratio to
      * the ideal aspect ratio for the target. This method uses the equivalent
      * rectangle sides to determine aspect ratio as it performs better as the
      * target gets skewed by moving to the left or right. The equivalent
      * rectangle is the rectangle with sides x and y where particle area= x*y
      * and particle perimeter= 2x+2y
      *
      * @param image The image containing the particle to score, needed to
      * perform an additional measurements
      * @param report The Particle Analysis Report for the particle, used for the
      * width, height, and particle number
      * @param particleNumber Which particle to focus on
      * @param outer	Indicates whether the particle aspect ratio should be
      * compared to the ratio for the inner target or the outer
      * @return The aspect ratio score (0-100)
      * @throws NIVisionException
      */
     public static double scoreAspectRatio(BinaryImage image, ParticleAnalysisReport report, int particleNumber, boolean outer) throws NIVisionException {
         double rectLong, rectShort, aspectRatio, idealAspectRatio;
         rectLong = NIVision.MeasureParticle(image.image, particleNumber, false, NIVision.MeasurementType.IMAQ_MT_EQUIVALENT_RECT_LONG_SIDE);
         rectShort = NIVision.MeasureParticle(image.image, particleNumber, false, NIVision.MeasurementType.IMAQ_MT_EQUIVALENT_RECT_SHORT_SIDE);
         idealAspectRatio = outer ? (62 / 29) : (62 / 20);	//Dimensions of goal opening + 4 inches on all 4 sides for reflective tape
 
         //Divide width by height to measure aspect ratio
         if (report.boundingRectWidth > report.boundingRectHeight) {
             //particle is wider than it is tall, divide long by short
             aspectRatio = 100 * (1 - Math.abs((1 - ((rectLong / rectShort) / idealAspectRatio))));
         } else {
             //particle is taller than it is wide, divide short by long
             aspectRatio = 100 * (1 - Math.abs((1 - ((rectShort / rectLong) / idealAspectRatio))));
         }
         return (Math.max(0, Math.min(aspectRatio, 100.0)));		//force to be in range 0-100
     }
 
     /**
      * scoreCompare. Compares scores to defined limits and returns true if the
      * particle appears to be a target
      *
      * @param scores The structure containing the scores to compare
      * @param outer True if the particle should be treated as an outer target,
      * false to treat it as a center target
      * @return True if the particle meets all limits, false otherwise
      */
     public static boolean scoreCompare(Scores scores, boolean outer) {
         boolean isTarget = true;
         isTarget &= scores.rectangularity > RECTANGULARITY_LIMIT;
         if (outer) {
             isTarget &= scores.aspectRatioOuter > ASPECT_RATIO_LIMIT;
         } else {
             isTarget &= scores.aspectRatioInner > ASPECT_RATIO_LIMIT;
         }
         isTarget &= scores.xEdge > X_EDGE_LIMIT;
         isTarget &= scores.yEdge > Y_EDGE_LIMIT;
 
         return isTarget;
     }
 
     /**
      * scoreRectangularity. Computes a score (0-100) estimating how rectangular
      * the particle is by comparing the area of the particle to the area of the
      * bounding box surrounding it. A perfect rectangle would cover the entire
      * bounding box.
      *
      * @param report The Particle Analysis Report for the particle to score
      * @return The rectangularity score (0-100)
      */
     public static double scoreRectangularity(ParticleAnalysisReport report) {
         if (report.boundingRectWidth * report.boundingRectHeight != 0) {
             return 100 * report.particleArea / (report.boundingRectWidth * report.boundingRectHeight);
         } else {
             return 0;
         }
     }
 
     /**
      * scoreXEdge. Computes a score based on the match between a template
      * profile and the particle profile in the X direction. This method uses the
      * the column averages and the profile defined at the top of the sample to
      * look for the solid vertical edges with a hollow center.
      *
      * @param image The image to use, should be the image before the convex hull
      * is performed
      * @param report The Particle Analysis Report for the particle
      * @return The X Edge Score (0-100)
      * @throws NIVisionException
      */
     public static double scoreXEdge(BinaryImage image, ParticleAnalysisReport report) throws NIVisionException {
         double total = 0;
         LinearAverages averages;
 
         NIVision.Rect rect = new NIVision.Rect(report.boundingRectTop, report.boundingRectLeft, report.boundingRectHeight, report.boundingRectWidth);
         averages = NIVision.getLinearAverages(image.image, LinearAverages.LinearAveragesMode.IMAQ_COLUMN_AVERAGES, rect);
         float columnAverages[] = averages.getColumnAverages();
         for (int i = 0; i < (columnAverages.length); i++) {
             if (xMin[(i * (XMINSIZE - 1) / columnAverages.length)] < columnAverages[i] && columnAverages[i] < xMax[i * (XMAXSIZE - 1) / columnAverages.length]) {
                 total++;
             }
         }
         total = 100 * total / (columnAverages.length);
         return total;
     }
 
     /**
      * scoreYEdge. Computes a score based on the match between a template
      * profile and the particle profile in the Y direction. This method uses the
      * the row averages and the profile defined at the top of the sample to look
      * for the solid horizontal edges with a hollow center
      *
      * @param image The image to use, should be the image before the convex hull
      * is performed
      * @param report The Particle Analysis Report for the particle
      * @return The Y Edge score (0-100)
      * @throws NIVisionException
      */
     public static double scoreYEdge(BinaryImage image, ParticleAnalysisReport report) throws NIVisionException {
         double total = 0;
         LinearAverages averages;
 
         NIVision.Rect rect = new NIVision.Rect(report.boundingRectTop, report.boundingRectLeft, report.boundingRectHeight, report.boundingRectWidth);
         averages = NIVision.getLinearAverages(image.image, LinearAverages.LinearAveragesMode.IMAQ_ROW_AVERAGES, rect);
         float rowAverages[] = averages.getRowAverages();
         for (int i = 0; i < (rowAverages.length); i++) {
             if (yMin[(i * (YMINSIZE - 1) / rowAverages.length)] < rowAverages[i] && rowAverages[i] < yMax[i * (YMAXSIZE - 1) / rowAverages.length]) {
                 total++;
             }
         }
         total = 100 * total / (rowAverages.length);
         return total;
     }
 }
