 package com.example.pebblenav;
 
 import android.content.Context;
 
 import com.example.pebblenav.util.PebbleDictionary;
 
 public class PebbleInterface {
 
 	private final static int TURN_DATA_KEY = 0;
 	private final static int STREET_DATA_KEY = 1;
 	private final static int TURN_IMAGE_KEY = 2;
 	private final static int VIBES_KEY = 3;
 
 	private static String TurnDataString = "";
 	private static String StreetDataString = "";
 	private static byte TurnImageID = 0;
 	
 	public static void sendDataToPebble (Context c, String stringVal, String stringVal2, int intVal, int buzz) {
 		PebbleDictionary data = new PebbleDictionary();
 		data.addString(TURN_DATA_KEY, stringVal2);
 		TurnDataString = stringVal2;
 		data.addString(STREET_DATA_KEY, stringVal);
 		StreetDataString = stringVal;
 		data.addInt8(TURN_IMAGE_KEY, (byte) intVal);
 		TurnImageID = (byte) intVal;
 		data.addInt32(VIBES_KEY, buzz);
 		PebbleKit.sendDataToPebble(c, Constants.selfUUID, data);
 	}
 	
	public static void sendString1ToPebble(Context c, String stringVal) {
		sendDataToPebble(c, StreetDataString, stringVal, TurnImageID, 0);		
 	}
 	
	public static void sendString2ToPebble(Context c, String stringVal) {
 		sendDataToPebble(c, stringVal, TurnDataString, TurnImageID, 0);
 	}
 	
	public static void sendTurnImageToPebble(Context c, int x) {
		sendDataToPebble(c, StreetDataString, TurnDataString, x, 0);
	}
	
 	public static void buzzPebble (Context c)
 	{
 		PebbleDictionary data = new PebbleDictionary();
 		data.addInt32(VIBES_KEY, 1);
 		PebbleKit.sendDataToPebble(c, Constants.selfUUID, data);
 	}
 
 }
 
 
