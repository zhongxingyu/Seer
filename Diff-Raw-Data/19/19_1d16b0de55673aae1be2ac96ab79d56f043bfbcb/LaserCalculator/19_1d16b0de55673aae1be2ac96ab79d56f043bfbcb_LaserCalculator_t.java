 package com.example.android.lasergame;
 
 import com.example.android.lasergame.Intersection.Intersects;
 
 public class LaserCalculator {
 
     private int mCanvasWidth;
     private int mCanvasHeight;
     private double mDesiredDegrees;
     private double mMaxLeftSideDegrees;
     private Triangle[] mTriangles;
     private Beam mBeam;
 	private double mMinimumFiringAngle;
 	private double mMaximumFiringAngle;
 
     public LaserCalculator(int canvasWidth, int canvasHeight, double minimumFiringAngle, double maximumFiringAngle) {
         mCanvasWidth = canvasWidth;
         mCanvasHeight = canvasHeight;
         mMinimumFiringAngle = minimumFiringAngle;
         mMaximumFiringAngle = maximumFiringAngle;
         mTriangles = new Triangle[] {};
     }
 
     public LaserCalculator(int canvasWidth, int canvasHeight, double minimumFiringAngle, double maximumFiringAngle, Triangle[] triangles) {
         mTriangles = triangles;
         mCanvasWidth = canvasWidth;
         mCanvasHeight = canvasHeight;
         mMinimumFiringAngle = minimumFiringAngle;
         mMaximumFiringAngle = maximumFiringAngle;
     }
 
     public Beam fireLaser(double desiredDegrees) {
         setDesiredDegrees(desiredDegrees);
 
         mBeam = new Beam();
         Line firstLine = new Line(new Point(mCanvasWidth / 2, mCanvasHeight), new Point(0, 0));
 
         mMaxLeftSideDegrees = getMaxLeftSideDegrees(firstLine.p1);
         if (hittingLeftWall()) {
             firstLine.p2.y = (int) (mCanvasHeight - Math.tan(Math.toRadians(mDesiredDegrees)) * firstLine.p1.x);
             if (!tryToReflect(firstLine)) {
                 mBeam.addLine(firstLine);
                 bounceRightThenLeft(firstLine);
             }
         } else if (hittingBackWall()) {
             if (firingStraightUp()) {
                 firstLine.p2.y = 0;
                 firstLine.p2.x = firstLine.p1.x;
                 mBeam.addLine(firstLine);
                 return mBeam;
             } else if (hittingLeftSideOfBackWall()) {
                 firstLine.p2.x = (int) (firstLine.p1.x - Math.tan(Math.toRadians(180 - 90 - mDesiredDegrees))
                         * mCanvasHeight);
                 if (!tryToReflect(firstLine)) {
                     mBeam.addLine(firstLine);
                     return mBeam;
                 }
             } else { // hitting right side...
                 firstLine.p2.x = (int) (firstLine.p1.x + Math.tan(Math.toRadians(mDesiredDegrees - 90)) * mCanvasHeight);
                 if (!tryToReflect(firstLine)) {
                     mBeam.addLine(firstLine);
                     return mBeam;
                 }
             }
         } else { // hitting right wall
             firstLine.p2.x = mCanvasWidth;
             mDesiredDegrees = 180 - mDesiredDegrees;
             firstLine.p2.y = (int) (mCanvasHeight - Math.tan(Math.toRadians(mDesiredDegrees)) * firstLine.p1.x);
             if (!tryToReflect(firstLine)) {
                 mBeam.addLine(firstLine);
                 bounceLeftThenRight(firstLine);
             }
         }
         return mBeam;
     }
 
 	private void setDesiredDegrees(double desiredDegrees) {
 		mDesiredDegrees = Math.max(desiredDegrees, mMinimumFiringAngle);
        mDesiredDegrees = Math.min(mDesiredDegrees, mMaximumFiringAngle);
 	}
 
     private boolean tryToReflect(Line line) {
         Intersects intersects = Intersection.whichEdgeDoesTheLinePassThroughFirst(mTriangles, line);
         
         if (intersects != null) {
             Line l = intersects.edge;
             Point p = intersects.intersectionP;
             line.p2.x = p.x;
             line.p2.y = p.y;
             mBeam.addLine(line);
 
             // start bouncing!!!!
             Vector2 n1 = new Vector2(l.p1.x, l.p1.y);
             Vector2 n2 = new Vector2(l.p2.x, l.p2.y);
            Vector2 nN = n1.add(n2).nor();
            Vector2 lineNorm = n1.sub(n2).nor();
             Vector2 nNPerp = new Vector2(-lineNorm.y, lineNorm.x);
 
             nN = nNPerp;
             Vector2 v1 = new Vector2(line.p1.x, line.p1.y);
             Vector2 v2 = new Vector2(line.p2.x, line.p2.y);
            Vector2 vN = v2.sub(v1).nor();
 
            Vector2 u = nN.mul(vN.dot(nN));
            Vector2 w = vN.sub(u);
            Vector2 vPrime = w.sub(u);
             
             Line next = new Line(new Point(line.p2.x, line.p2.y), new Point(mCanvasWidth, 0));
             next.p2.y = (int) (((vPrime.y / vPrime.x) * (mCanvasWidth - line.p2.x)) + line.p2.y);
        
             boolean goLeft = false;
             for (Line tLine : intersects.triangle.edges()) {
                 if (tLine != intersects.edge && Intersection.detect(tLine, next) != null) {
                     next = new Line(new Point(line.p2.x, line.p2.y), new Point(0, 0));
                     next.p2.y = (int) (((vPrime.y / vPrime.x) * (0 - line.p2.x)) + line.p2.y);
                     goLeft = true;
                 }
             }
            
             
             mBeam.addLine(next);
            
 //            if (goLeft) {
 //                bounceLeftThenRight(next);
 //            } else {
 //                bounceRightThenLeft(next);
 //            }
             return true;
         }
         return false;
     }
 
     private void bounceLeftThenRight(Line line) {
         double nextAngle = 180 - (mDesiredDegrees + 90);
         // -----Heading left ----
         Line l2 = new Line(new Point(line.p2.x, line.p2.y), new Point(0, 0));
 
         l2.p2.y = (int) (l2.p1.y - (float) (Math.tan(Math.toRadians(mDesiredDegrees)) * l2.p1.x));
         if (l2.p2.y > 0) { // left wall
             mBeam.addLine(l2);
             bounceRightThenLeft(l2);
         } else { // back wall
             l2.p2.y = 0;
             l2.p2.x = (int) (mCanvasWidth - Math.tan(Math.toRadians(nextAngle)) * l2.p1.y);
             mBeam.addLine(l2);
         }
     }
 
     private boolean hittingLeftSideOfBackWall() {
         return mDesiredDegrees < 90;
     }
 
     private boolean firingStraightUp() {
         return mDesiredDegrees == 90;
     }
 
     /**
      *     x ------------>x
      *    ******************                                                   
      *  y *T*              *                                                   
      *  | *   *            *                                                   
      *  | *    *           *                                                   
      *  | *     *          *                                                   
      *  | *       *        *                                                   
      *  y *        M*      *                                                   
      *    ******************
      * Returns the maximum number of degrees this map supports while still 
      * hitting the left wall
      */
     private double getMaxLeftSideDegrees(Point startPoint) {
         double T = Math.atan2(startPoint.x, startPoint.y) * 180.0F / Math.PI;
         double M = 180 - (T + 90);
         return M;
     }
 
     private boolean hittingBackWall() {
         return mDesiredDegrees < mMaxLeftSideDegrees + (180 - mMaxLeftSideDegrees * 2);
     }
 
     private boolean hittingLeftWall() {
         return mDesiredDegrees < mMaxLeftSideDegrees;
     }
 
     private void bounceRightThenLeft(Line line) {
         Line l2 = new Line(new Point(line.p2.x, line.p2.y), new Point(0, 0));
 
         // -----Heading right ---
         double nextAngle = 180 - (mDesiredDegrees + 90);
         l2.p2.x = (int) (Math.tan(Math.toRadians(nextAngle)) * l2.p1.y);
         if (l2.p2.x < mCanvasWidth) { // hitting back wall
             mBeam.addLine(l2);
         } else { // bounce off right wall
             l2.p2.x = mCanvasWidth;
             l2.p2.y = (int) (l2.p1.y - (float) (Math.tan(Math.toRadians(mDesiredDegrees)) * mCanvasWidth));
             mBeam.addLine(l2);
             bounceLeftThenRight(l2);
         }
     }
 }
