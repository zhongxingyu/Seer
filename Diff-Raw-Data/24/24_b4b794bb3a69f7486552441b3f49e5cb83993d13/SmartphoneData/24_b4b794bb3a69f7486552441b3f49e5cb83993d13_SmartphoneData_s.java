 package utils;
 
 import java.util.Date;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.os.Bundle;
 import android.provider.Settings;
 import android.telephony.CellLocation;
 import android.telephony.PhoneStateListener;
 import android.telephony.ServiceState;
 import android.telephony.SignalStrength;
 import android.telephony.TelephonyManager;
 
 /* 	INFO: 
  *  To get access (security permissions) to ACCESS_FINE_LOCATION you have to insert the next line into the manifest:  
  *  <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
  *  
  *  	required for ACCESS_COARSE_LOCATION:
  *  	<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
  *		<uses-permission android:name="android.permission.INTERNET" />
  */
 
 public class SmartphoneData {
 	private LocationManager locationManager;
 	private TelephonyManager tManager;
 	private PhoneStateListener phoneStateListener;
 	
 	private int GsmRssi, GsmBitErrorRate, CdmaDbm, CdmaEcio;
 
 	private static final int TIME_INTERVAL = 500; 		// time interval in milli seconds 
 	private static final int METER_RANGE = 0; 			// range in meter
 
 	public SmartphoneData (LocationManager locationManager, TelephonyManager telephonyManager) {
 		this.locationManager = locationManager;
 		this.tManager = telephonyManager;
 
 		//locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
 
 		locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, TIME_INTERVAL, METER_RANGE, listener);
 		// INFO: LocationManager.GPS_PROVIDER: Settings -> Location and security -> "Use wireless networks"
 		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, TIME_INTERVAL, METER_RANGE, listener); 
 
 		final boolean gpsEnabled = this.locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
 		if (!gpsEnabled) {
 			// TODO: disabled enableLocationSettings just for testing the time
 			// pops up the GPS activation window
 			// enableLocationSettings();
 		}
 		
 		phoneStateSetup();
 	}
 
 	private final LocationListener listener = new LocationListener() {
 
 		@Override
 		public void onLocationChanged(Location location) {
 			// A new location update is received.  Do something useful with it.  In this case,
 			// we're sending the update to a handler which then updates the UI with the new
 			// location.
 			// Message.obtain(mHandler, UPDATE_LATLNG, location.getLatitude() + ", " + location.getLongitude()).sendToTarget();
 			// getSpeed() returns a value in meter per second 
 		}
 
 		@Override
 		public void onStatusChanged(String provider, int status, Bundle extras) {
 		}
 
 		@Override
 		public void onProviderEnabled(String provider) {
 		}
 
 		@Override
 		public void onProviderDisabled(String provider) {
 		}
 	};
 
 	public void onStop() {
 		// determines the location updates
 		locationManager.removeUpdates(listener);
 	}
 
 	private void enableLocationSettings() {
 		Intent settingsIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
 		Activity tmp = new Activity();
 		tmp.startActivity(settingsIntent);
 		tmp.finish();
 	}
 
 	public Date getDate(){
 		Date systemDate = new Date();
 
 
 		// INFO: getLastKnownLocation returns last known time which not have to be the current time.
 		if(locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER) == null)
 			if (locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER) == null)
 				return systemDate;
 			else{
 				Date networkDate = new Date(locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER).getTime());    			
 				if(networkDate.compareTo(systemDate) < 0)
 					return systemDate;
 				else
 					return networkDate;
 			}
 		else{
 			Date gpsDate = new Date(locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER).getTime());
 			if(gpsDate.compareTo(systemDate) < 0)
 				return systemDate;
 			else
 				return gpsDate;
 		}
 	}
 
 	public float getSpeed(){
 		if(locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER) == null)
 			return -1;
 		else
 			return locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER).getSpeed();
 	}    
 
 	public double getAltitude(){
 		if(locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER) == null)
 			return -1;
 		else
 			return locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER).getAltitude();
 	} 
 
 	public double getLatitude(){
 		if(locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER) == null)
 			return -1;
 		else
 			return locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER).getLatitude();
 	} 
 
 	public double getLongitude(){
 		if(locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER) == null)
 			return -1;
 		else
 			return locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER).getLongitude();
 	}
 
 	public void getSignalStrenght(){
 		PhoneStateListener phoneStateListener = new PhoneStateListener() {
 			public void onCallForwardingIndicatorChanged(boolean cfi) {}
 			public void onCallStateChanged(int state, String incomingNumber) {}
 			public void onCellLocationChanged(CellLocation location) {}
 			public void onDataActivity(int direction) {}
 			public void onDataConnectionStateChanged(int state) {}
 			public void onMessageWaitingIndicatorChanged(boolean mwi) {}
 			public void onServiceStateChanged(ServiceState serviceState) {}
 			public void onSignalStrengthChanged(int asu) {}
 		};
 
 		tManager.listen(phoneStateListener,
 				PhoneStateListener.LISTEN_CALL_FORWARDING_INDICATOR |
 				PhoneStateListener.LISTEN_CALL_STATE |
 				PhoneStateListener.LISTEN_CELL_LOCATION |
 				PhoneStateListener.LISTEN_DATA_ACTIVITY |
 				PhoneStateListener.LISTEN_DATA_CONNECTION_STATE |
 				PhoneStateListener.LISTEN_MESSAGE_WAITING_INDICATOR |
 				PhoneStateListener.LISTEN_SERVICE_STATE |
 				PhoneStateListener.LISTEN_SIGNAL_STRENGTH);
 	}
 
 	public String getNetworkType(){
 		String net = "Unknown";
 		int networkType = tManager.getNetworkType();
 
 
 		switch (networkType)
 		{
 		case 7:
 			net = "1xRTT";
 			break;      
 		case 4:
 			net = "CDMA";
 			break;      
 		case 2:
 			net = "EDGE";
 			break;  
 		case 14:
 			net = "eHRPD";
 			break;      
 		case 5:
 			net = "EVDO rev. 0";
 			break;  
 		case 6:
 			net = "EVDO rev. A";
 			break;  
 		case 12:
 			net = "EVDO rev. B";
 			break;  
 		case 1:
 			net = "GPRS";
 			break;      
 		case 8:
 			net = "HSDPA";
 			break;      
 		case 10:
 			net = "HSPA";
 			break;          
 		case 15:
 			net = "HSPA+";
 			break;          
 		case 9:
 			net = "HSUPA";
 			break;          
 		case 11:
 			net = "iDen";
 			break;
 		case 13:
 			net = "LTE";
 			break;
 		case 3:
 			net = "UMTS";
 			break;          
 		case 0:
 			net = "Unknown";
 			break;
 		}
 
 		return net;
 	}
 
 	private void phoneStateSetup(){
 		phoneStateListener = new PhoneStateListener() {
 			public void onCallForwardingIndicatorChanged(boolean cfi){}
 			public void onCallStateChanged(int state, String incomingNumber){}
 			public void onCellLocationChanged(CellLocation location){}
 			public void onDataActivity(int direction){}
 			public void onDataConnectionStateChanged(int state){}
 			public void onMessageWaitIndicatorChanged(boolean mwi){}
 			public void onServiceStateChanged(ServiceState serviceState){}
 			public void onSignalStrengthsChanged(SignalStrength signalStrength){
 				
 				if(signalStrength.isGsm()){
 					GsmRssi = -113 + 2 * signalStrength.getGsmSignalStrength();
 					GsmBitErrorRate = signalStrength.getGsmBitErrorRate();	
 				}
 				else{
 					CdmaDbm = signalStrength.getCdmaDbm();
 					CdmaEcio = signalStrength.getCdmaEcio();	
 				}
 			}
 		};
 
 		try{
 			tManager.listen(phoneStateListener, PhoneStateListener.LISTEN_CELL_LOCATION
 					| PhoneStateListener.LISTEN_DATA_ACTIVITY
 					| PhoneStateListener.LISTEN_DATA_CONNECTION_STATE
 					| PhoneStateListener.LISTEN_SIGNAL_STRENGTHS
 					| PhoneStateListener.LISTEN_CALL_STATE);
 		}catch(Exception e){
 
 		}
 	}
 
 	public int getGsmRssi() {
 		return GsmRssi;
 	}
 
 	public int getGsmBitErrorRate() {
 		return GsmBitErrorRate;
 	}
 
 	public int getCdmaDbm() {
 		return CdmaDbm;
 	}
 
 	public int getCdmaEcio() {
 		return CdmaEcio;
 	}
 
 }
