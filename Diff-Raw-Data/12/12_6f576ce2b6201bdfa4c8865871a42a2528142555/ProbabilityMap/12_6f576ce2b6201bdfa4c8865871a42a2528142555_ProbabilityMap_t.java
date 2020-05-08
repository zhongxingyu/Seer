 package main;
 
 import javax.swing.*;
 import java.awt.*;
 import java.awt.geom.AffineTransform;
 import java.awt.geom.Ellipse2D;
 import math.geom2d.Point2D;
 import java.awt.image.BufferedImage;
 
 /**
  * Created with IntelliJ IDEA.
  * User: ty
  * Date: 11/12/13
  * Time: 10:07 PM
  */
 public class ProbabilityMap extends JPanel implements Runnable{
     /** The size of the grid */
     private int mGridSize;
     /** P(S = occupied | O)*/
     private double [][] mProbOccupied;
     /** P(O = occupied | S = occupied) the true positive */
     private double mTruePos;
     /** P(O = unoccupied | S = occupied) the true positive */
     private double mFalsePos;
     /** P(O = unoccupied | S = unoccupied) the true negative */
     private double mTrueNeg;
     /** P(O = unoccupied | S = occupied) the false negative */
     private double mFalseNeg;
     /**The image used to represent the grid*/
     private ProbabilityMap mProbabilityMap;
 
     /** Thread that runs the animation */
     private Thread mAnimator;
 
     /** Origin of the world */
     private int mImageXOrig;
     private int mImageYOrig;
     /** World size */
     private int mWorldSize;
     private BufferedImage mRaster;
     private final Object lock = new Object();
 
     public ProbabilityMap(int worldSize, double initProb, double truePos, double trueNeg) {
         setDoubleBuffered(true);
         mWorldSize = worldSize;
         mImageXOrig = mWorldSize / 2;
         mImageYOrig = mWorldSize / 2;
         //mRaster =  new BufferedImage(mWorldSize + 20, mWorldSize + 20, BufferedImage.TYPE_BYTE_GRAY);
         mRaster =  new BufferedImage(mWorldSize + 20, mWorldSize + 20, BufferedImage.TYPE_3BYTE_BGR);
 
         mProbOccupied = new double [worldSize][worldSize];
         for(int i = 0; i < mWorldSize; i++) {
             for(int j = 0; j < mWorldSize; j++) {
                 mProbOccupied[i][j] = initProb;
                 setProbability(i, j, (float) initProb);
             }
         }
 
         mTruePos = truePos;
         mFalsePos = 1.0 - mTruePos;
         mTrueNeg = trueNeg;
         mFalseNeg = 1.0 - mTrueNeg;
     }
 
     /**
      * Filters belief of occupancy
      */
     public void updateProbability(int worldX, int worldY, boolean observedOccupied)
     {
         int imageX = mImageXOrig + worldX;
         int imageY = mImageYOrig - worldY;
 
         if(imageY < 0 || imageX < 0 || imageX >= getWorldSize() || imageY >= getWorldSize())
             return;
 
         if(observedOccupied) {
             double truePos = mTruePos * mProbOccupied[imageX][imageY];
             double falsePos = mFalsePos * (1.0 - mProbOccupied[imageX][imageY]);
             mProbOccupied[imageX][imageY] = truePos / (truePos + falsePos);
         }
         else {
             double trueNeg = mTrueNeg * (1.0 - mProbOccupied[imageX][imageY]);
             double falseNeg = mFalseNeg * mProbOccupied[imageX][imageY];
             mProbOccupied[imageX][imageY] = falseNeg / (trueNeg + falseNeg);
         }
 
         setProbability(imageX, imageY, (float) mProbOccupied[imageX][imageY]);
         //setProbability(imageX, imageY, 1.0f);
     }
 
     /**
      * Returns an unexplored portion of the map. If an area can't be found,
      * just return the center point
      */
     public Point2D getUnexploredLoc() {
         int bound = 50;
         for(int xImg = bound; xImg < mWorldSize - bound; xImg++) {
             for(int yImg = bound; yImg < mWorldSize - bound; yImg++) {
                 if(isUnexplored(xImg, yImg))
                     return new Point2D(xImg - mImageXOrig, mImageYOrig - yImg);
             }
         }
 
         return new Point2D(0, 0);
     }
 
     /**
      * Checks to see if a location on the map is unexplored or not.
      * A point is explored if the 8-connect neighbors are also unexplored.
      * @param imageX
      * @param imageY
      * @return
      */
     private boolean isUnexplored(int imageX, int imageY) {
         double unoccThreshold = 0.2;
         double occuThreshold = .8;
 
         for(int i = -2; i < 3; i++) {
             for(int j = -2; j < 3; j++) {
                 if((imageX + i < 0 || imageX + i >= mWorldSize) ||
                     (imageY + j < 0 || imageY + j >= mWorldSize))
                     continue;
 
                 if(mProbOccupied[imageX + i][imageY + j] < unoccThreshold ||
                         mProbOccupied[imageX + i][imageY + j] > occuThreshold)
                     return false;
             }
         }
 
         return true;
     }
 
     /**
      * Sets the probability in the raster
      * @param imageX
      * @param imageY
      * @param probability
      */
     private void setProbability(int imageX, int imageY, float probability) {
         synchronized (lock) {
             /*System.out.println("Given x: " + Integer.toString(worldX));
             System.out.println("Given y: " + Integer.toString(worldY));
             System.out.println("X: " + Integer.toString(imageX));
             System.out.println("Y: " + Integer.toString(imageY));*/
 
             if(imageX < 0 || imageY < 0)
                 return;
 
            float[] pixel = {(probability) * 255, (probability) * 255, (probability) * 255};
             mRaster.getRaster().setPixel(imageX, imageY, pixel);
         }
     }
 
     /**
      * Highlights the goal pixel
      * @param worldX
      * @param worldY
      */
     public void highlightGoal(int worldX, int worldY) {
 
         int imageX = mImageXOrig + worldX;
         int imageY = mImageYOrig - worldY;
 
         float[] pixel = {255, 155, 255};
         for(int i = -1; i < 2; i++) {
             for(int j = -1; j < 2; j++) {
                 if((imageX + i < 0 || imageX + i >= mWorldSize) ||
                         (imageY + j < 0 || imageY + j >= mWorldSize))
                     continue;
 
                 mRaster.getRaster().setPixel(imageX + i, imageY + j, pixel);
             }
         }
     }
 
     public int getWorldSize() {
         return mWorldSize;
     }
 
     public void paint(Graphics g) {
         synchronized (lock) {
             super.paint(g);
             Graphics2D g2 = (Graphics2D) g;
             Toolkit.getDefaultToolkit().sync();
             g2.drawImage(mRaster, null, null);
             g.dispose();
         }
     }
 
     public void addNotify() {
         super.addNotify();
         mAnimator = new Thread(this);
         mAnimator.start();
     }
 
     @Override
     public void run() {
         while(true) {
             repaint();
 
             try {
                 Thread.sleep(50);
             }
             catch (InterruptedException e) {
                 System.out.println("Interuppted");
             }
         }
     }
 }
