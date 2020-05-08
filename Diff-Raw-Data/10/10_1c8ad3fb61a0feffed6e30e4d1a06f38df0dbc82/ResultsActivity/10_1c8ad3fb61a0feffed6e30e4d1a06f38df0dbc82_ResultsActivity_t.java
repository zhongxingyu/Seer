 package com.delin.speedlogger;
 
 import java.util.List;
 
 import android.app.Activity;
 import android.location.Location;
 import android.os.Bundle;
 import android.widget.CheckBox;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.TextView;
 
 public class ResultsActivity extends Activity {
 	TextView mMaxSpeed;
 	TextView mDistance;
 	CheckBox mStraightLine;
 	Button mButton;
 	String testline = "origin";
 	MeasurementResult mMeasurement;
 	
 	public void onCreate(Bundle savedInstanceState){
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.results);
 		mButton = (Button)findViewById(R.id.button1);
 		mButton.setOnClickListener(mOnClickListener);
 		mMaxSpeed = (TextView)findViewById(R.id.textMaxSpeed);
 		mDistance = (TextView)findViewById(R.id.textDistance);
                 mMaxSpeed.setText(testline);
 		
 		mMeasurement = MeasurementResult.GetInstance();
 		mMeasurement.SaveToGPX("/mnt/sdcard/textlog.gpx");
 	}
 	
 	// got it here: http://wiki.openstreetmap.org/wiki/Mercator#Java_Implementation
 	public double mercX(double lon){
 		double r_big = 6378137.0; // big radius of an ellipse
 		return r_big * Math.toRadians(lon);
 	}
 	// got it here: http://wiki.openstreetmap.org/wiki/Mercator#Java_Implementation
 	public double mercY(double lat) {
 		double r_big = 6378137.0; // big radius of an ellipse
 		double r_small = 6356752.3142; // small radius of an ellipse
         if (lat > 89.5) {
             lat = 89.5;
         }
         if (lat < -89.5) {
             lat = -89.5;
         }
         double temp = r_big / r_small;
         double es = 1.0 - (temp * temp);
         double eccent = Math.sqrt(es);
         double phi = Math.toRadians(lat);
         double con = eccent * Math.sin(phi);
         double com = 0.5 * eccent;
         con = Math.pow(((1.0-con)/(1.0+con)), com);
         double ts = Math.tan(0.5 * ((0.5*Math.PI) - phi))/con;
         double y = 0 - r_big * Math.log(ts);
         return y;
     }
 	
 	double sqrDistPointToPoint3D(double ax, double ay, double az, double bx, double by, double bz){
 	    return (ax-bx)*(ax-bx) + (ay-by)*(ay-by) + (az-bz)*(az-bz);
 	}
 	
 	// got it here: http://krasprog.ru/persons.php?page=demidenko&blog=10
 	double distPointToLine(double Px, double Py, double Pz, double Ax, double Ay, double Az, 
 						   double Bx, double By, double Bz)
 	{
 	    double pa, pb, ab; // squares of distances PA, PB, AB
 	    pa = sqrDistPointToPoint3D(Px,Py,Pz,Ax,Ay,Az);
 	    pb = sqrDistPointToPoint3D(Px,Py,Pz,Bx,By,Bz);
 	    ab = sqrDistPointToPoint3D(Ax,Ay,Az,Bx,By,Bz);
 	    
 	    // if perpendicular is out of line then closest point is one of line edges
 	    if(pa >= pb + ab) return Math.sqrt(pb);
 	    if(pb >= pa + ab) return Math.sqrt(pa);
 	    
 	    double a1, a2, a3, b1, b2, b3; // temp vars
 	    a1=Ax-Px; a2=Ay-Py; a3=Az-Pz;
 	    b1=Bx-Px; b2=By-Py; b3=Bz-Pz;
 	    
 	    return Math.sqrt(sqrDistPointToPoint3D(a2*b3-a3*b2,-(a1*b3-a3*b1),a1*b2-b1*a2,0,0,0)/ab);
 	}
 	
 	// TODO: it must be deleted or rewritten
 	// HINT: this is generalized version for both 2D and 3D.
 	//		 you can make separate func for 2D case to make simpler calculus there
 	public boolean StraightLine(List<Location> locList, boolean use3D){
 		// for a small path we assume it is a straight line
 		// TODO: 2 better be replaced with something
 		if (locList.size() <= 2) return true;
 		
 		Location origin = locList.get(0);
 		Location dest = locList.get(locList.size()-1);
 			
 		// lat/lon to UTM convert is necessary to perform line calculus
 		// UTM values represented in meters
 		// altitude's already in meters, no conversion needed
 		double x1 = mercX(origin.getLongitude());
 		double y1 = mercY(origin.getLatitude());
 		double z1 = use3D? origin.getAltitude() : 0;
 		double x2 = mercX(dest.getLongitude());
 		double y2 = mercY(dest.getLatitude());
 		double z2 = use3D? dest.getAltitude() : 0;
 		
 		// TODO: get proper number for eps
 		// TODO: move eps to the place where constants live
 		double eps = 5.d; // max allowed deviation from the line (meters)
 		for (int i=2; i<locList.size(); i++){
 			Location loc = locList.get(i);
 			double x = mercX(loc.getLongitude());
 			double y = mercY(loc.getLatitude());
 			double z = use3D? loc.getAltitude() : 0;
 			double d = distPointToLine(x,y,z,x1,y1,z1,x2,y2,z2);
 			if (d > eps) return false;
 		}
 		return true;
 	}
 	
 	public void onSessionFinished(List<Location> mLocList){
 		// printing max speed
 		// TODO: we can get maxSpeed from TrackingSession
 		float maxSpeed = mLocList.get(0).getSpeed();
 		for (int i=1; i<mLocList.size(); i++){
 			if (maxSpeed < mLocList.get(i).getSpeed()){
 				maxSpeed = mLocList.get(i).getSpeed();
 			}
 		}
 		mMaxSpeed.setText(Float.toString(maxSpeed));
 		// interpolate here
 		
 		// printing distance
 		Location origin = mLocList.get(0);
 		Location dest = mLocList.get(mLocList.size());
 		double distance = origin.distanceTo(dest);
 		mDistance.setText(Double.toString(distance));
 		
 		mStraightLine.setChecked(StraightLine(mLocList,true));
 	}
 	
 	private void TestFunc(String input) {
 		input = "hardcode";
 	}
 	
     private OnClickListener mOnClickListener = new OnClickListener() {
         public void onClick(View v) {
         	TestFunc(testline);
        	mMaxSpeed.setText(testline);
         }
         };
 
 }
