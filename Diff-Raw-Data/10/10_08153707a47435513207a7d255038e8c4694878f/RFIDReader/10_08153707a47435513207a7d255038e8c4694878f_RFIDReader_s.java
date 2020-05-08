 //RFID Simulation, CSE461 Fall 2009, by Josh Goodwin
 //Credit to RFIDReader.java, zahorjan, 2009/01/28
 
 //RFIDReader.java controls the reader of RFID tags.
 //It sends out message on the channel, gets responses
 //from the channel, and keeps a list of inventoried
 //tag EPC id's.  When it believes that all tags have
 //been inventoried, it returns the list of tag EPC id's.
 //as a list of byte[]'s.
 
 import java.util.*;
 import java.io.*;
 
 public class RFIDReader {
     private static final int STOPPING_CRITERIA = 4;
     private static final int INITIAL_WINDOW = 30;
     private static final int WINDOW_TRANS_SPACING = 10;
 
     private List<byte[]> currentInventory;
     private Set<String> epcValues; // List.contains() uses ==, not .equals...
     private RFIDChannel channel;
 
     public RFIDReader(RFIDChannel chan) {
         currentInventory = new ArrayList<byte[]>();
         epcValues = new HashSet<String>();
         channel = chan;
     }
     
     public List<byte[]> inventory() {
         int count = 0; //count of consecutive no-replies
         int window = INITIAL_WINDOW;
         byte[] response;
 	    boolean windowChange = true;
 	    int timeSinceLastWindowPacket = 0;
 	    byte nextFlag = RFIDConstants.NEW_QUERY;
 
         while(count < STOPPING_CRITERIA) {
 	        if(windowChange && timeSinceLastWindowPacket > WINDOW_TRANS_SPACING)
 		        response = sendQuery(nextFlag, window);
 	        else {
 	        	response = sendQuery(nextFlag);
 	        
                 if(response == null){
                 	count += window == 0 ? 1 : 0;
 		            windowChange = window != 0;
                     window = Math.max(window / 2 - 1, 0);
 		            nextFlag = RFIDConstants.DESPERATE_QUERY;
                 } else if(Arrays.equals(response, RFIDChannel.GARBLE)){
                     count = 0;
 	            	windowChange = window != 255;
                     window = Math.min(window * 2 + 1, 255);
 		            nextFlag = RFIDConstants.COLLISION_QUERY;
                 } else {
                    if(!currentInventory.contains(response))
                         currentInventory.add(response);
                     count = 0;
 	            	windowChange = window != 0;
 	            	window = Math.max(window - 1, 0);
 	            	nextFlag = RFIDConstants.NEW_QUERY;
                 }
             }
 
            return currentInventory;
     }
     
     private byte[] sendQuery(byte code){
 	    return channel.sendMessage(new byte[]{code});
     }
 
     private byte[] sendQuery(byte code, int window){
     	return channel.sendMessage(new byte[]{code, new RFIDWindow(window).toByte()});
     }
 }
