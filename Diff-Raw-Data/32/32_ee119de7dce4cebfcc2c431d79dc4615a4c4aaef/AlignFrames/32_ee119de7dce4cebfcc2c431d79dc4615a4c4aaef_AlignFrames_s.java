 package rgbdslam;
 
 import april.jmat.LinAlg;
 import java.util.ArrayList;
 import java.util.List;
 import kinect.*;
 import kinect.Kinect.Frame;
 import rgbdslam.DescriptorMatcher.Match;
 
 /**
  *
  * @author pdaquino
  */
 public class AlignFrames {
 
     public static final int DECIMATION_FACTOR = 6;
     final static double ALPHA = 0.5; // relative weighting between initial estimate and new rbt estimate
 
     private List<ImageFeature> currFeatures, lastFeatures;
     private ColorPointCloud currFullPtCloud, currDecimatedPtCloud;
     private ColorPointCloud lastFullPtCloud, lastDecimatedPtCloud;
     private List<DescriptorMatcher.Match> latestInliers;
 
     public AlignFrames(Kinect.Frame currFrame, Kinect.Frame lastFrame) {
         this.currFullPtCloud = makeFullPtCloud(currFrame);
         this.currFeatures = extractAndProjectFeatures(currFrame, currFullPtCloud);
         this.currDecimatedPtCloud = makeDecimatedPtCloud(currFrame);
 
         this.lastFullPtCloud = makeFullPtCloud(lastFrame);
         this.lastFeatures = extractAndProjectFeatures(lastFrame, lastFullPtCloud);
         this.lastDecimatedPtCloud = makeDecimatedPtCloud(lastFrame);
     }
 
     public AlignFrames(Kinect.Frame currFrame, List<ImageFeature> lastProjectedFeatures,
             ColorPointCloud lastFullPtCloud, ColorPointCloud lastDecimatedPtCloud) {
         this.currFullPtCloud = makeFullPtCloud(currFrame);
         this.currFeatures = extractAndProjectFeatures(currFrame, currFullPtCloud);
         this.currDecimatedPtCloud = makeDecimatedPtCloud(currFrame);
 
         this.lastFeatures = lastProjectedFeatures;
         this.lastFullPtCloud = lastFullPtCloud;
         this.lastDecimatedPtCloud = lastDecimatedPtCloud;
     }
 
     public double[][] align() {
         return align(null, null);
     }
     
     public double[][] align(List<Match> allMatches, List<Match> inliers) {
         DescriptorMatcher dm = new DescriptorMatcher(lastFeatures, currFeatures);
         ArrayList<DescriptorMatcher.Match> matches = dm.match();
 
         if(inliers == null) {
             inliers = new ArrayList<Match>();
         }
         
         double[][] Rrbt = RANSAC.RANSAC(matches, inliers); // RANSAC's Estimate
         if (Rrbt == null) {
             System.err.println("ERR: Null transformation returned by RANSAC");
             Rrbt = LinAlg.identity(4);
         }
         
        System.out.println("RANSAC's estimate");
         LinAlg.print(LinAlg.matrixToRollPitchYaw(Rrbt));
         
         ICP icp = new ICP(lastDecimatedPtCloud);
 
        double[][] Irbt = icp.match(currDecimatedPtCloud, Rrbt); // ICP's Estimate
         latestInliers = inliers;
         if(allMatches != null) {
             allMatches.addAll(matches);
         }
         
        System.out.println("ICP's estimate");
         LinAlg.print(LinAlg.matrixToRollPitchYaw(Irbt));
         
        //double[][] Erbt = weightedSum(Rrbt, Irbt, ALPHA);
        double[][] Erbt = Rrbt;

         return Erbt;
     }
 
     public ColorPointCloud getLastDecimatedPtCloud() {
         return lastDecimatedPtCloud;
     }
 
     public List<ImageFeature> getLastFeatures() {
         return lastFeatures;
     }
 
     public ColorPointCloud getLastFullPtCloud() {
         return lastFullPtCloud;
     }
 
     public ColorPointCloud getCurrDecimatedPtCloud() {
         return currDecimatedPtCloud;
     }
 
     public List<ImageFeature> getCurrFeatures() {
         return currFeatures;
     }
 
     public ColorPointCloud getCurrFullPtCloud() {
         return currFullPtCloud;
     }
 
     public List<Match> getLatestInliers() {
         return latestInliers;
     }
    
     private ColorPointCloud makeDecimatedPtCloud(Frame frame) {
         return new ColorPointCloud(frame, DECIMATION_FACTOR);
     }
 
     private ColorPointCloud makeFullPtCloud(Frame frame) {
         return new ColorPointCloud(frame);
     }
 
     public static List<ImageFeature> extractAndProjectFeatures(Frame frame, ColorPointCloud fullPtCloud) {
         List<ImageFeature> allFeatures = OpenCV.extractFeatures(frame.argb, Constants.WIDTH);
         List<ImageFeature> features = new ArrayList<ImageFeature>();
         for (ImageFeature fc : allFeatures) {
             fc.setXyz(fullPtCloud.Project(LinAlg.copyDoubles(fc.xy())));
             if (fc.xyz()[2] != -1) {
                 features.add(fc);
             }
         }
         return features;
     }
     
     // weight A by alpha and B by 1-alpha
     // if alpha == 1 then returns A
     private double[][] weightedSum(double[][] A, double[][] B, double alpha) {
 
         assert ((A.length == B.length) && (A[0].length == B[0].length) && (alpha <= 1) && (alpha >= 0));
 
         double[][] C = new double[A.length][A[0].length];
         for (int i = 0; i < A.length; i++) {
             for (int j = 0; j < A[0].length; j++) {
                 C[i][j] = alpha * A[i][j] + (1 - alpha) * B[i][j];
             }
         }
 
         return C;
     }
 }
