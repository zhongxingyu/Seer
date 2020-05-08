 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package edu.wpi.first.wpilibj.templates;
 
 import edu.wpi.first.wpilibj.camera.AxisCamera;
 import edu.wpi.first.wpilibj.image.BinaryImage;
 import edu.wpi.first.wpilibj.image.ColorImage;
 import edu.wpi.first.wpilibj.image.ParticleAnalysisReport;
 import edu.wpi.first.wpilibj.image.CriteriaCollection;
 import edu.wpi.first.wpilibj.image.NIVision.MeasurementType;
 
 /**
  *
  * @author Rajath
  * find targets
  */
 public class ImageProcessing {
 
     ParticleAnalysisReport particles[];
     Physics imageCalculations;
     CriteriaCollection criteriaCollection = new CriteriaCollection();
     ParticleAnalysisReport bottomTarget, topTarget, middleTarget;
     
     final int numberOfDegreesInVerticalFieldOfView = 33;
     final int numberOfPixelsVerticalInFieldOfView = 240;
     final int numberOfPixelsHorizontalInFieldOfView = 640;
 
     final int heightToTheTopOfTheTopTarget = 118;
     final int heightToBottomOfTopTarget = 100;
     
     final int heightToTheTopOfTheBottomTarget = 48;
     final int heightToBottomOfBottomTarget = 30;
     final double cameraAngleOffset = 12;
 
     final double cameraHeight = 45.25;
 
     public ImageProcessing() {
         criteriaCollection.addCriteria(
                 MeasurementType.IMAQ_MT_BOUNDING_RECT_WIDTH, 30, 400, false);
         criteriaCollection.addCriteria(
                 MeasurementType.IMAQ_MT_BOUNDING_RECT_HEIGHT, 40, 400, false);
         imageCalculations = new Physics(true);
     }
     public double pixlesToAngles(double pixles)
     {
         return pixles*numberOfDegreesInVerticalFieldOfView
                 /numberOfPixelsVerticalInFieldOfView;
     }
     public double anglesToPixles(double angle)
     {
         return angle*numberOfDegreesInVerticalFieldOfView
                 /numberOfPixelsVerticalInFieldOfView;
     }
     public double getPixelsFromLevelToBottomOfTopTarget(
             ParticleAnalysisReport particle) {
         double PixelsFromLevelToBottomOfTopTarget =
                 numberOfPixelsVerticalInFieldOfView - particle.center_mass_y
                 - (particle.boundingRectHeight / 2);
         System.out.println("PixelsFromLevelToBottomOfTopTarget"+
                 PixelsFromLevelToBottomOfTopTarget);
         return PixelsFromLevelToBottomOfTopTarget;
     }
     public double getPixelsFromLevelToTopOfTopTarget(
             ParticleAnalysisReport particle) {
         /*TODO take into account the fact that level is not
          * always at the bottom of the field of view
          */
         /*TODO don't use global variable PixelsFromLevelToBottomOfTopTarget,
          * instead use local variables, return values, and parameters
          */
         double PixelsFromLevelToTopOfTopTarget =
                 numberOfPixelsVerticalInFieldOfView - particle.center_mass_y
                 + (particle.boundingRectHeight / 2);
         System.out.println("PixelsFromLevelToTopOfTopTarget" + 
                 PixelsFromLevelToTopOfTopTarget);
         return PixelsFromLevelToTopOfTopTarget;
     }
     public double getPhi(double PixelsFromLevelToTopOfTopTarget) {
         /* TODO: consider simply returning the calculated value,
          * instead of assigning it to a variable and then returning that
          * variable
          */
         System.out.print(PixelsFromLevelToTopOfTopTarget
                     / numberOfPixelsVerticalInFieldOfView
                 * numberOfDegreesInVerticalFieldOfView);
         return (PixelsFromLevelToTopOfTopTarget
                     / numberOfPixelsVerticalInFieldOfView)
                 * numberOfDegreesInVerticalFieldOfView + cameraAngleOffset;   
     }
     public double getTheta(double PixelsFromLevelToBottomOfTopTarget) {
         System.out.println(PixelsFromLevelToBottomOfTopTarget / 
                 numberOfPixelsVerticalInFieldOfView * 
                 numberOfDegreesInVerticalFieldOfView);
         return  (PixelsFromLevelToBottomOfTopTarget /
                 numberOfPixelsVerticalInFieldOfView)
                 * numberOfDegreesInVerticalFieldOfView  + cameraAngleOffset;        
     }
     public double getHypotneuse1(double angle) {
         double opposite1 = heightToTheTopOfTheTopTarget - cameraHeight;
         double hypotneuse_1 =
                 opposite1
                 / MathX.sin(getPhi(angle));
        System.out.println("Phi " + getPhi(angle));
         return hypotneuse_1;
     }
     public static ParticleAnalysisReport getTopMost(ParticleAnalysisReport[] particles)
     {
         ParticleAnalysisReport greatest = particles[0];
         for (int i=0;i < particles.length; i++)
         {
             ParticleAnalysisReport particle = particles[i];
             
             if(particle.center_mass_y < greatest.center_mass_y){
                 greatest = particle;
             }       
         }
         return greatest;
     }
     public ParticleAnalysisReport getBottomMost
             (ParticleAnalysisReport[] particles)
     {
        ParticleAnalysisReport lowest = particles[0];
         for (int i=0;i < particles.length; i++)
         {
             ParticleAnalysisReport particle = particles[i];
             
             if(particle.center_mass_y < lowest.center_mass_y){
                 lowest = particle;
             }       
         }
         return lowest;
     }
         public ParticleAnalysisReport getLeftMost
                 (ParticleAnalysisReport[] particles)
     {
         ParticleAnalysisReport leftist = particles[0];
         for (int i=0;i < particles.length; i++)
         {
             ParticleAnalysisReport particle = particles[i];
             
             if(particle.center_mass_x < leftist.center_mass_x){
                 leftist = particle;
             }       
         }
         return leftist;
     }
     public ParticleAnalysisReport getRightMost
             (ParticleAnalysisReport[] particles)
     {
        ParticleAnalysisReport rightist = particles[0];
         for (int i=0;i < particles.length; i++)
         {
             ParticleAnalysisReport particle = particles[i];
             
             if(particle.center_mass_x > rightist.center_mass_x){
                 rightist = particle;
             }       
         }
         return rightist;
     }
     public void setTargets(ParticleAnalysisReport[] particles)
     {
         topTarget = getTopMost(particles);
        bottomTarget = getBottomMost(particles);
         
     }
     public double getHypotneuse0(double angle) {
         double opposite0 = heightToBottomOfTopTarget + cameraHeight;
         double hypotneuse_0 = opposite0
                 / MathX.sin(getTheta(angle));
         System.out.println("Phi " + getTheta(angle));
         return hypotneuse_0;
     }
     public double getAdjacent1(double phiAngle,double hypotneuse){
         return MathX.cos(phiAngle) * hypotneuse;
     }
     public double getAdjacent0(double thetaAngle,double hypotneuse){
         return MathX.cos(thetaAngle) * hypotneuse;   
     }
     public void idTopTarget(ParticleAnalysisReport particle) {                  
             double phi = getPhi(getPixelsFromLevelToTopOfTopTarget(particle));
             double theta = getTheta
                     (getPixelsFromLevelToBottomOfTopTarget(particle));
         
             double hypotneuse1 = getHypotneuse1(phi);
             double hypotneuse0 = getHypotneuse0(theta);
         
             double adjacent1 = getAdjacent1(phi,hypotneuse1);
             double adjacent0 = getAdjacent0(theta,hypotneuse0);
             double averageDistance = (adjacent1 + adjacent0)/2;
             
             System.out.println("Adjacent0 : " + adjacent0);
             System.out.println("Adjacent1 : " + adjacent1);
             System.out.println("Average of Adjacents is " + averageDistance);
             System.out.println("---------------------------------------------");
     }
 
     public void getTheParticles(AxisCamera camera) throws Exception {
         int erosionCount = 2;
         // true means use connectivity 8, true means connectivity 4
         boolean connectivity8Or4 = false;
         ColorImage colorImage;
         BinaryImage binaryImage;
         BinaryImage cleanImage;
         BinaryImage convexHullImage;
         BinaryImage filteredImage;
 
         colorImage = camera.getImage();
         //seperate the light and dark image
         binaryImage = colorImage.thresholdRGB(0, 42, 71, 255, 0, 255);
         cleanImage = binaryImage.removeSmallObjects(
                 connectivity8Or4, erosionCount);
         //fill the rectangles that were created
         convexHullImage = cleanImage.convexHull(connectivity8Or4);
         filteredImage = convexHullImage.particleFilter(criteriaCollection);
         particles = filteredImage.getOrderedParticleAnalysisReports();
         colorImage.free();
         binaryImage.free();
         cleanImage.free();
         convexHullImage.free();
         filteredImage.free();
     }
 
 
     public double CameraCorrection(ParticleAnalysisReport particle,String target){
         
         double targetHeight = 0;
         
         if(target == "top"){
            targetHeight = 109;
         }
         
         if(target == "middle"){
            targetHeight = 72;
         }
         if(target == "bottom"){
             targetHeight = 39;
         }
         double delta =  targetHeight  - cameraHeight;
         double lambda = numberOfPixelsVerticalInFieldOfView/
                 numberOfDegreesInVerticalFieldOfView;
         double pixelHeightBetweenReflectiveTape = 
                 getPixelsFromLevelToTopOfTopTarget(particle) - 
                 getPixelsFromLevelToBottomOfTopTarget(particle);
         double ph_fixed = pixelHeightBetweenReflectiveTape;
         
         double R = 18/MathX.tan(ph_fixed/lambda);
         
         double Distance = 0;
         for(int i =1; i<= 4; i++)
         {
             double theta = MathX.asin(delta/R);
             double ph_new = ph_fixed/MathX.cos(theta);
             R = 18/MathX.tan(ph_new/lambda);
             Distance = MathX.sqrt(R*R-delta*delta);
         }
         
         return Distance;
         
        }
 
 }
