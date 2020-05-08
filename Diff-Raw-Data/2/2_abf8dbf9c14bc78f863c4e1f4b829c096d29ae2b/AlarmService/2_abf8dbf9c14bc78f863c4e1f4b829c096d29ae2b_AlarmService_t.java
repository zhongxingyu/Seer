 package services;
 
 import java.io.IOException;
 import java.util.List;
 import java.util.Locale;
 
 import locationfactory.LocationFactory;
 import locationfactory.LocationFactoryImpl;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.location.Address;
 import android.location.Location;
 import android.location.Geocoder;
 import android.os.Bundle;
 import android.telephony.SmsManager;
 import android.widget.Toast;
 
 public class AlarmService extends BroadcastReceiver {
 
 	@Override
 	public void onReceive(Context context, Intent intent) {
 		try {
 			Bundle bundle = intent.getExtras();
 			String message;
 
 			String phoneNumber = bundle.getString("contact");
 			double homeLatitude = bundle.getDouble("homeLat");
 			double homeLongitude = bundle.getDouble("homeLong");
 
 			LocationFactory locationFactory = LocationFactory.getInstance();
 
 			if (locationFactory == null) {
 				throw new Exception("Location Factory not instantiated");
 			}
 			Location lastLocation = locationFactory.getPos();
 			//Do stuff here
 			if (inSafeZone(homeLatitude, homeLongitude, lastLocation)) {
				
 			}
 			else {	
 				try{
 			
 					List<Address> addresses = new Geocoder(context, Locale.getDefault()).getFromLocation(lastLocation.getLatitude(),lastLocation.getLongitude() , 1);
 					Address current = addresses.get(0);
 					
 					String line1 = current.getAddressLine(0);
 					String line2 = current.getAddressLine(1);
 					
 					if (line1 == null)
 						line1 = "";
 					if (line2 == null)
 						line2 = "";
 
 					
 					 message = "I'm late coming back from my run. My last known location was: " + line1 + ", " +
 							line2 +" please call me"; 
 				}catch(IOException e){
 					//If the Geocoder cannot establish a connection it throws an IOException, however we still want to alert someone that the runner is late. So we leave out their location
 					//from the message
 					message ="I'm late coming back from my run, please call me";
 				}
 				
 				
 				sendSMS(phoneNumber, message);
 				Toast.makeText(context, "Send SMS to: " + phoneNumber, Toast.LENGTH_SHORT).show();
 			}
 		} catch (Exception e) {
 			Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
 			e.printStackTrace();
 
 		}
 
 	}
 
 	private boolean inSafeZone(double homeLat, double homeLong, Location last){
 		Location home = new Location("Home");
 		home.setLatitude(homeLat);
 		home.setLongitude(homeLong);
 		
 		double distance = home.distanceTo(last);
 		
 		if (distance > 100)
 			return false;
 		
 		return true;
 	}
 	
 	private void sendSMS(String number, String message) {
 	    SmsManager smsManager = SmsManager.getDefault();
 	    smsManager.sendTextMessage(number, null, message, null, null);
 	}
 	
 	
 
 }
