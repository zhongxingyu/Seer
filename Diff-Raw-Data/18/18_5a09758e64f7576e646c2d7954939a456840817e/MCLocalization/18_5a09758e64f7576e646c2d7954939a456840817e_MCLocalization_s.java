 package MCL;
 
 /**
  * Created by IntelliJ IDEA.
  * User: eirik
  * Date: 1/25/11
  * Time: 3:33 PM
  */
 public class MCLocalization {
     private Line[] boundaryLines;
     private Pose[] poses;
     private int xMax, yMax;
     private static MCLocalization instance;
 
     public static MCLocalization getInstance(){
         if(instance == null){
             instance = new MCLocalization();
         }
         return instance;
     }
 
     private MCLocalization() {
 
     }
 
     public void init(Point[] boundaryPoints){
          boundaryLines = new Line[boundaryPoints.length];
         xMax = 0;
         yMax = 0;
 
         for (int i = 0; i < boundaryLines.length; i++) {
             if (boundaryPoints[i].getX() > xMax) {
                 xMax = (int) boundaryPoints[i].getX();
             }
             if (boundaryPoints[i].getY() > yMax) {
                 yMax = (int) boundaryPoints[i].getY();
             }
             boundaryLines[i] = new Line(boundaryPoints[i], boundaryPoints[(i + 1) % (boundaryPoints.length)]);
         }
         int NUMBER_OF_POSES = 360 * 1000;
         poses = new Pose[NUMBER_OF_POSES];
     }
 
     public Line[] getLines() {
         return boundaryLines;
     }
 
     public Pose[] getPoses() {
         return poses;
     }
 
     public Pose[] resetDisplay() {
 
         for (int j = 0; j < poses.length / 360; j++) {
             int randX;
             int randY;
             do {
                 randX = (int) (Math.random() * (xMax - 20));
                 randY = (int) (Math.random() * (yMax - 20));
             } while (randX <= 1465 && randY >= 1270);
 
             for (int degrees = 0; degrees < 360; degrees++) {
                 poses[(j * 360) + degrees] = new Pose(degrees, randX, randY,
                         1 / (double) poses.length);
             }
         }
         computeParticleDistances();
         return poses;
 
     }
 
     public void moveParticles(int movement, int sonarDistance) {
         for (int i = 0; i < poses.length; i++) {
             poses[i].getPoint().setLocation((int) (poses[i].getPoint().getX() +
                     movement * Math.cos(Math.toRadians(poses[i].getHeading()))), (int) (poses[i].getPoint().getY() +
                     movement * Math.sin(Math.toRadians(poses[i].getHeading()))));
         }
 
         if(sonarDistance != 255){
             computeParticleDistances();
             computeWeights(sonarDistance);
             normalizeWeights();
             computeCumWeights();
             chooseNewPoseSet();
         }
     }
 
     public void rotateParticles(int angleDeg, int sonarDistance) {
 
         for (int i = 0; i < poses.length; i++) {
             poses[i].setHeading((poses[i].getHeading() + angleDeg) % 360);
         }
 
        if(sonarDistance != 255){
             computeParticleDistances();
             computeWeights(sonarDistance);
             normalizeWeights();
             computeCumWeights();
             chooseNewPoseSet();
         }
 
 
     }
 
     /*
       * THIS METHOD HAS A BUG WHERE IF THE POSE IS ON THE LINE, ITS
       * LENGTH IS NOT COMPUTED AS ZERO, BUT AS ANOTHER NUMBER
       * THE SOLUTION APPEARS TO BE TO DEMAND THAT T IN (0,1)
       */
 
     private void computeParticleDistances() {
         double[] distances = new double[boundaryLines.length];
         boolean[] validDistances = new boolean[boundaryLines.length];
         double[] params = {0, 0};
         double minimumDistance = 0;
 
        for (Pose pose : poses) {
             for (int j = 0; j < boundaryLines.length; j++) {
                 boolean trueResult = computeCurrentDistance(boundaryLines[j],
                        pose, params);
                 if (trueResult && params[0] >= 0 && params[1] > 0 && params[1] < 1) {
                     distances[j] = params[0];
                     validDistances[j] = true;
                 } else {
                     distances[j] = 0;
                     validDistances[j] = false;
                 }
 
             }
             int m = 0;
             boolean foundMinimum = false;
             while (true) {
                 if (validDistances[m]) {
                     minimumDistance = distances[m];
                     foundMinimum = true;
                     break;
                 }
                 if (m == validDistances.length - 1) break;
                 m++;
             }
 
             if (m <= validDistances.length - 1 && foundMinimum) {
                 for (int n = m; n < validDistances.length; n++) {
                     if (validDistances[n] && minimumDistance > distances[n]) {
                         minimumDistance = distances[n];
                     }
                 }
                pose.setValid(true);
                pose.setDistance(minimumDistance);
             } else {
                pose.setValid(false);
                pose.setDistance(0);
             }
 
         }
 
     }
 
     private boolean computeCurrentDistance(Line boundaryLine,
                                            Pose aPose, double[] params) {
         boolean returnValue = false;
         Point startPoint = boundaryLine.getStartPoint();
         Point endPoint = boundaryLine.getEndPoint();
         double x1 = startPoint.getX();
         double y1 = startPoint.getY();
         double x2 = endPoint.getX();
         double y2 = endPoint.getY();
         double theta = Math.toRadians(aPose.getHeading());
         double a = aPose.getPoint().getX();
         double b = aPose.getPoint().getY();
         double determinant = Math.cos(theta) * (y1 - y2) -
                 Math.sin(theta) * (x1 - x2);
         if (Math.abs(determinant) < 0.0000001) {
             return returnValue;
         }
         returnValue = true;
 
         params[0] = (((x1 - a) * (y1 - y2) - (y1 - b) * (x1 - x2)) /
                 determinant);
         params[1] = (((Math.cos(theta)) * (y1 - b) - (Math.sin(theta)) * (x1 - a)) /
                 determinant);
         return returnValue;
 
     }
 
     private void computeWeights(double distanceOfSonar) {
         for (int i = 0; i < poses.length; i++) {
             if (poses[i].isValid()) {
                 poses[i].setWeight(weight(distanceOfSonar, 1,
                         poses[i].getDistance()));
             } else {
                 poses[i].setWeight(0);
             }
         }
 
     }
 
     private void normalizeWeights() {
         double totalWeight = 0;
         for (Pose pose : poses) {
             totalWeight = totalWeight + pose.getWeight();
         }
 
         for (int i = 0; i < poses.length; i++) {
             poses[i].setWeight(poses[i].getWeight() / totalWeight);
         }
     }
 
     private double weight(double mean, double std, double x) {
         double constant = 1;//1/(std*Math.sqrt(2*Math.PI));
         double exponent = (-1 * (x - mean) * (x - mean)) / (2 * std * std);
         return constant * Math.exp(exponent);
     }
 
     private void computeCumWeights() {
         poses[0].setCumWeight(poses[0].getWeight());
         for (int k = 1; k < poses.length; k++) {
             poses[k].setCumWeight(poses[k].getWeight() + poses[k - 1].getCumWeight());
         }
     }
 
     private void chooseNewPoseSet() {
 
         Pose[] newPoses = new Pose[poses.length];
 
         for (int k = 0; k < poses.length; k++) {
             double choice = Math.random();
             boolean notFound = true;
             int lower = 0;
             int upper = poses.length - 1;
             int trialPose = 0;
             int count = 0;
             while (notFound) {
                 count++;
                 if (count > 20) {
                     System.out.println("Too many times.");
                     System.exit(0);
                 }
                 trialPose = (int) Math.floor(((upper - lower) / 2.0)) + lower;
 
                 if (choice <= poses[trialPose].getCumWeight() &&
                         choice > (poses[trialPose].getCumWeight() -
                                 poses[trialPose].getWeight())) {
                     notFound = false;
                 } else if (choice > poses[trialPose].getCumWeight()) {
                     lower = trialPose + 1;
                 } else {
                     upper = trialPose - 1;
                 }
                 if (lower == upper || upper < lower) break;
             }
             float headingNoise = 0;
             float xNoise = 0;
             float yNoise = 0;
             newPoses[k] = new Pose(poses[trialPose].getHeading() +
                     headingNoise,
                     (int) (poses[trialPose].getPoint().getX() + xNoise),
                     (int) (poses[trialPose].getPoint().getY() + yNoise));
         }
 
         System.arraycopy(newPoses, 0, poses, 0, newPoses.length);
     }
 
 
 }
