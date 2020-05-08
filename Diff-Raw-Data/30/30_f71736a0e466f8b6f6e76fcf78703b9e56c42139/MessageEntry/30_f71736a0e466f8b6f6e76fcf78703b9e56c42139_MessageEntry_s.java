 package com.nomad.nomadclient;
 
 import java.util.Date;
 
 public class MessageEntry {
 	String message;
 	String timeCreated;
 	
 	public MessageEntry(String m, Date t){
 		message = m;
 		int hour = t.getHours();
 		String ampm = "am";
 		if(hour == 0)
 			hour = 12;
 		if(hour  > 12){
 			hour -= 12;
 			ampm = "pm";
 		}
 			
 		
		timeCreated = String.format("%02d/%02d/%02d %02d:%02d"+ampm,t.getMonth()+1,t.getDay()+1,t.getYear()+1900,hour,t.getMinutes());
 		//timeCreated = t.getMonth()+"/"+t.getDay()+"/"+t.getYear()+" " + String.format("%02d",t.getHours()) + ":" + t.getMinutes();
 	}
 }
