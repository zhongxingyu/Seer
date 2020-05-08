 package tweeter0830.boxbot;
 
 import org.ejml.simple.SimpleMatrix;
 
 import android.util.Log;
 import java.math.*;
 import tweeter0830.kalmanFilter.*;
 
 public class DiffDriveExtKF extends ExtendedKF{
 
    private static String LOGTAG_ = "KalmanFilter";
 
    //Constants
    private double R_;
    private double L_;
    private double D_;
    private double inititalLat_;
    private long oldTime_;
    private long newTime_;
 
    public DiffDriveExtKF(double diameter, double length, double earthRad, double roughLattitude){
 	   D_ = diameter;
 	   L_ = length;
 	   R_ = earthRad;
 	   inititalLat_ = roughLattitude;
 	   x = new SimpleMatrix(6, 1);
 	   P = new SimpleMatrix(6, 6);
 	   
 	   Q = new SimpleMatrix(6,6);
 	   R = new SimpleMatrix(6,6);
    }
    
    public void initialize(){
 	   oldTime_ = System.nanoTime();
    }
    
    public void setState(final double[] inState){
 	   for(int row = 0; row<inState.length; row++){
 		   x.set(row,inState[row]);
 	   }
    }
    
    public void updateGPS(double lat, double longi){
 	   predict(getTimeChange());
 	   
 	   double[][] meas = new double[6][1];
 	   meas[0][0] = longi;
 	   meas[1][0] = lat;
 	   boolean[] measFlags = new boolean[6];
 	   measFlags[0] = true;
 	   measFlags[1] = true;
 	   update(new SimpleMatrix(meas), measFlags);
 	   //this.setState(new double[6]);
    }
    
    public void updateSpeeds(double leftSpeed, double rightSpeed){
 	   predict(getTimeChange());
 	   
 	   double[][] meas = new double[6][1];
 	   meas[2][0] = leftSpeed;
 	   meas[3][0] = rightSpeed;
 	   boolean[] measFlags = new boolean[6];
 	   measFlags[2] = true;
 	   measFlags[3] = true;
 	   update(new SimpleMatrix(meas), measFlags);
 	   //this.setState(new double[6]);
    }
    
    public void updateAccel(double accel){
 	   predict(getTimeChange());
 	   
 	   double[][] meas = new double[6][1];
 	   meas[4][0] = accel;
 	   boolean[] measFlags = new boolean[6];
 	   measFlags[4] = true;
 	   update(new SimpleMatrix(meas), measFlags);
 	   //this.setState(new double[6]);
    }
    
    public void updateHeading(double heading){
 	   predict(getTimeChange());
 	   
 	   double[][] meas = new double[6][1];
 	   meas[5][0] = heading;
 	   boolean[] measFlags = new boolean[6];
 	   measFlags[5] = true;
 	   update(new SimpleMatrix(meas), measFlags);
 	   //this.setState(new double[6]);
    }
    
    public void setSimpleP(final double[] inError){
 	   setDiagonal(P, inError);
    }
    
    public void setSimpleQ(final double[] inError){
 	   setDiagonal(Q, inError);
    }
    
    public void setSimpleR(final double[] inError){
 	   setDiagonal(R, inError);
    }
    
    public void getState(double[] outState){
 	   for(int row = 0; row<x.numRows(); row++){
 		   outState[row]=x.get(row);
 	   }
    }
    
    public void getSimpleP(double[] outState){
 	   SimpleMatrix diag = P.extractDiag();
 	   for(int row = 0; row<diag.numRows(); row++){
 		   outState[row]=diag.get(row);
 	   }
    }
    
    protected void calcF(final SimpleMatrix inState, SimpleMatrix F,double timeChange){
        double easting = inState.get(0);
        double northing = inState.get(1);
        double velocity = inState.get(2);
        double accel = inState.get(3);
        double heading = inState.get(4);
        double headingDot = inState.get(5);
 
       F.setRow(0, 0,                  1, 0, Math.sin(heading)*timeChange,0,velocity*Math.cos(heading)*timeChange,0);
       F.setRow(1, 0,                  0, 1, Math.cos(heading)*timeChange,0,-velocity*Math.sin(heading)*timeChange,0);
       F.setRow(2, 0,                  0, 0, 1, timeChange,0,0);
       F.setRow(3, 0,                  0, 0, 0, 1,0,0);
       F.setRow(4, 0,                  0, 0, 0, 0,1,timeChange);
       F.setRow(5, 0,                  0, 0, 0, 0,0,1);
    }
 
    protected void calcf(final SimpleMatrix inState, SimpleMatrix outState, double timeChange){
        double easting = inState.get(0);
        double northing = inState.get(1);
        double velocity = inState.get(2);
        double accel = inState.get(3);
        double heading = inState.get(4);
        double headingDot = inState.get(5);
 
        
        //outState = new SimpleMatrix(6,1); 
        outState.set(0,velocity*Math.sin(heading)*timeChange+easting);
        outState.set(1,velocity*Math.cos(heading)*timeChange+northing);
        outState.set(2,accel*timeChange+velocity);
        outState.set(3,accel);
        outState.set(4,headingDot*timeChange+heading);
        outState.set(5, headingDot);
    }
 
    protected void calcH(final SimpleMatrix inState, SimpleMatrix H){
 //      double easting = inState.get(1);
 //      double northing = inState.get(2);
 //      double velocity = inState.get(3);
 //      double accel = inState.get(4);
 //      double heading = inState.get(5);
 //      double headingDot = inState.get(6);
 
        H.setRow(0, 0,                  Math.cos(inititalLat_),0,0,0,0,0);
        H.setRow(1, 0,                  0,1.0/R_,0,0,0,0);
        H.setRow(2, 0,                  0,0,1.0/D_,0,0,L_/(2*D_));
        H.setRow(3, 0,                  0,0,1.0/D_,0,0,-L_/(2*D_));
        H.setRow(4, 0,                  0,0,0,1,0,0);
        H.setRow(5, 0,                  0,0,0,0,1,0);
    }
 
    protected void calch(final SimpleMatrix inState, SimpleMatrix h){
        double easting = inState.get(0);
        double northing = inState.get(1);
        double velocity = inState.get(2);
        double accel = inState.get(3);
        double heading = inState.get(4);
        double headingDot = inState.get(5);
 
        //Change in Lon
        h.set(0,easting/R_*Math.cos(inititalLat_));
        //Change in Lat
        h.set(1,northing/R_);
        //Left Wheel Speed
        h.set(2,velocity/D_+headingDot*L_/(2*D_));
        //Right Wheel Speed
        //TODO This minus might need to be switched. Check defined direction of theta
        h.set(3,velocity/D_-headingDot*L_/(2*D_));
        //Acceleration
        h.set(4,accel);
        //Heading
        h.set(5,heading);
    }
    
    private double getTimeChange(){
 	   newTime_ = System.nanoTime();
 	   double timeChangeSec = ((double)(newTime_-oldTime_))/1000000000;
 	   oldTime_ = newTime_;
 	   return timeChangeSec;
    }
    
    private void setDiagonal(SimpleMatrix inMatrix, double[] inDiag){
 	   for(int row = 0; row<inDiag.length; row++){
 		   inMatrix.set(row, row, inDiag[row]);
 	   }
    }
 }
