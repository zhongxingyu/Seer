 package com.ninja.bluetooth;
 

 public class Utils {
	
 	/**
 	 * @param data byte array of data read from Arduino
 	 * @return message sent by the user
 	 */
	public static String parseArduinoMessage(byte[] data){
 		String _data = new String(data);
		
 		String message;
        if(_data.contains(Constants.EMERGENCY_MESSAGE)){
        	message = Constants.EMERGENCY_MESSAGE;
        }
        else if(_data.contains(Constants.CANCEL_MESSAGE)){
        	message = Constants.CANCEL_MESSAGE;
        }
        else{
        	message = Constants.ERROR_MESSAGE;
        }
        
        return message;
 	}
 }
