 package com.portman.panel;
  
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.ServerSocket;
 import java.net.Socket;
 
 import org.json.JSONObject;
 import org.json.JSONTokener;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.view.View;
 import android.view.View.OnClickListener;
  
 public class PanelActivity extends Activity implements OnClickListener {
    ServerSocket ss = null;
    Thread myCommsThread = null;
    protected static final int MSG_ID = 0x1337;
    public static final int SERVERPORT = 6000;
    private static String mClientMsg = "";
    
    // UI controls
    private static Airspeed mAirspeed;
    private static Altimeter mAltimeter;
    private static Manifold mManifold;
    private static RPM 	   mRPM;
    private static TurnIndicator mTurnIndicator;
    private static ArtificialHorizon mArtificialHorizon;
    private static DirectionalGyro mDirectionalGyro;
    private static Variometer  mVariometer;
    private static EngineGauge mEngineGauge;
    private static FuelGauge mFuelGaugeLeft;
    private static FuelGauge mFuelGaugeRight;
    private static FuelGauge mFuelGaugeFuselage;   
 
    @Override
    public void onCreate(Bundle savedInstanceState) {
 	   super.onCreate(savedInstanceState);
 	   setContentView(R.layout.activity_panel);
 	   
 	   // find controls	   
 	   mAirspeed = (Airspeed) findViewById(R.id.airspeed);
 	   mAltimeter = (Altimeter) findViewById(R.id.altimeter);
 	   mManifold = (Manifold) findViewById(R.id.manifold);
 	   mRPM		 = (RPM) findViewById(R.id.rpm);
 	   mTurnIndicator = (TurnIndicator) findViewById(R.id.turn_indicator);
 	   mArtificialHorizon = (ArtificialHorizon) findViewById(R.id.artificial_horizon);
 	   mDirectionalGyro = (DirectionalGyro) findViewById(R.id.directional_gyro);
 	   mVariometer = (Variometer) findViewById(R.id.variometer);
 	   mEngineGauge = (EngineGauge) findViewById(R.id.engine_gauge);
 	   mFuelGaugeLeft = (FuelGauge) findViewById(R.id.fuel_gauge_left);
 	   mFuelGaugeRight = (FuelGauge) findViewById(R.id.fuel_gauge_right);
 	   mFuelGaugeFuselage = (FuelGauge) findViewById(R.id.fuel_gauge_fuselage);
 	   
 	   // set titles
 	   mFuelGaugeLeft.setTitle("Fuel Left Tank");
 	   mFuelGaugeRight.setTitle("Fuel Right Tank");
 	   mFuelGaugeFuselage.setTitle("Fuel Fuselage Tank");	   
 	   
 	   // set click handlers
 	   mRPM.setOnClickListener(this);
 	   mEngineGauge.setOnClickListener(this);
 	   mFuelGaugeLeft.setOnClickListener(this);
 	   mFuelGaugeRight.setOnClickListener(this);
 	   mFuelGaugeFuselage.setOnClickListener(this);	   
 	  	 
 	   this.myCommsThread = new Thread(new CommsThread());
 	   this.myCommsThread.start();
    }
    
    // Implement the OnClickListener callback
    public void onClick(View v) {
 	   if (mRPM.getVisibility() == View.VISIBLE) { // show engine gauges
 		   mRPM.setVisibility(View.GONE);
 		   mEngineGauge.setVisibility(View.VISIBLE);
 	   } 
 	   else if (mEngineGauge.getVisibility() == View.VISIBLE) {
 		   mEngineGauge.setVisibility(View.GONE);
 		   mFuelGaugeLeft.setVisibility(View.VISIBLE);		   
 	   }
 	   else if (mFuelGaugeLeft.getVisibility() == View.VISIBLE) {
 		   mFuelGaugeLeft.setVisibility(View.GONE);
 		   mFuelGaugeRight.setVisibility(View.VISIBLE);		   
 	   }
 	   else if (mFuelGaugeRight.getVisibility() == View.VISIBLE) {
 		   mFuelGaugeRight.setVisibility(View.GONE);
 		   mFuelGaugeFuselage.setVisibility(View.VISIBLE);		   
 	   }
 	   else if (mFuelGaugeFuselage.getVisibility() == View.VISIBLE) {
 		   mFuelGaugeFuselage.setVisibility(View.GONE);
 		   mRPM.setVisibility(View.VISIBLE);		   
 	   }
    }
  
    @Override
    protected void onStop() {
 	   super.onStop();
 	   try {
 		   // make sure you close the socket upon exiting
 		   ss.close();
 	   } catch (IOException e) {
 		   e.printStackTrace();
 	   }
    }
  
    private static Handler myUpdateHandler = new Handler() {
 	   public void handleMessage(Message msg) {
 		   switch (msg.what) {
 		   case MSG_ID:			   
 			   try {
 				   // parse json
 				   JSONObject object = (JSONObject) new JSONTokener(mClientMsg).nextValue();
 				   
 				   mAirspeed.setAirspeed((float)object.getDouble("AirspeedNeedle"));
 				   mAltimeter.setAltimeter((float)object.getDouble("Altimeter_10000_footPtr")/10000f, 
 						   (float)object.getDouble("Altimeter_1000_footPtr")/1000f, 
 						   (float)object.getDouble("Altimeter_100_footPtr")/100f);
 				   mManifold.setManifold((float)object.getDouble("Manifold_Pressure"));
 				   mRPM.setRPM((float)object.getDouble("Engine_RPM")/100f);
 				   mTurnIndicator.setTurnNeedlePosition((float)object.getDouble("TurnNeedle"));
 				   mTurnIndicator.setSlipballPosition((float)object.getDouble("Slipball"));
 				   mArtificialHorizon.setPitchAndBank((float)object.getDouble("AHorizon_Pitch"), (float)object.getDouble("AHorizon_Bank"));
 				   mDirectionalGyro.setGyroHeading((float)object.getDouble("GyroHeading"));
				   mVariometer.setVariometer((float)object.getDouble("Variometer")/1000);
 				   mEngineGauge.setValues((float)object.getDouble("Oil_Temperature"), (float)object.getDouble("Oil_Pressure"), (float)object.getDouble("Fuel_Pressure"));
 				   mFuelGaugeLeft.setFuel((float)object.getDouble("Fuel_Tank_Left"));
 				   mFuelGaugeRight.setFuel((float)object.getDouble("Fuel_Tank_Right"));
 				   mFuelGaugeFuselage.setFuel((float)object.getDouble("Fuel_Tank_Fuselage"));				   
 			   } catch (Exception e) {
 				   // TODO Auto-generated catch block
 				   e.printStackTrace();
 			   }
 			   break;
 		   default:
 			   break;
 		   }
 		   super.handleMessage(msg);
 	   }
    };
    
    class CommsThread implements Runnable {
 	   public void run() {
 		   Socket s = null;
 		   try {
 			   ss = new ServerSocket(SERVERPORT );
 		   } catch (IOException e) {
 			   e.printStackTrace();
 		   }
         
 		   while (!Thread.currentThread().isInterrupted()) {
 			   Message m = new Message();
 			   m.what = MSG_ID;
 			   try {
 				   if (s == null)
 					   s = ss.accept();
 				   BufferedReader input = new BufferedReader(new InputStreamReader(s.getInputStream()));
 				   String st = null;
 				   st = input.readLine();
 				   if (st == null) {
 					   input.close();
 					   s.close();
 					   s = null;
 				   } else {
 					   mClientMsg = st;
 					   myUpdateHandler.sendMessage(m);
 				   }
 			   } catch (IOException e) {
 				   e.printStackTrace();
 			   }
 		   }
 	   }
     }
 }
