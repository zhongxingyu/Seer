 package com.example.android.BluetoothChat.GPS;
 
 import com.example.android.BluetoothChat.BluetoothChat;
 
 import android.location.Location;
 import android.location.LocationListener;
 import android.os.Bundle;
 
 public class MyLocationListener implements LocationListener
     {
     	private double speed = 0.0;
     	private BluetoothChat _chat;
     	
     	public MyLocationListener()
     	{
     		
     	}
     	
     	public MyLocationListener(BluetoothChat bluetoothchat)
     	{
     		_chat = bluetoothchat;
     	}
     	
     	public float getSpeed()
     	{
     		return (float) speed;
     	}
     	
 	    @Override
 	    public void onLocationChanged(Location loc)
 	    {
 	    	loc.getLatitude();
 	    	loc.getLongitude();
 	    	speed = loc.getSpeed(); // speed in m/s
	    	//_chat.displayLine("speed: "+speed);		//pop speed out to display for testing purposes
 	    }
 	
 	    @Override
 	    public void onProviderDisabled(String provider)
 	    {
 	    }
 	
 	
 	    @Override
 	    public void onProviderEnabled(String provider)
 	    {
 	    }
 	
 	    @Override
 	    public void onStatusChanged(String provider, int status, Bundle extras)
 	    {
 	
 	    }
 }
