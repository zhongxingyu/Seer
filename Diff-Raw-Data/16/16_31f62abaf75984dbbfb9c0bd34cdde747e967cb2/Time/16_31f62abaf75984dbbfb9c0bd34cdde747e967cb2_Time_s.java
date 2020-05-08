 package net.praqma.util.time;
 
 public class Time {
 	public static String longToTime( long time ) {
		
 		int hours = 0;
 		int minutes = 0;
 		int seconds = 0;
 		
 		time /= 1000;
 		
		String t = "";
		
 		/* Hour */
 		if( time > 3600 ) {
			hours = (int) ( time % 3600 );
			time -= ( hours * 3600 );
 		}
 		
 		if( time > 60 ) {
			minutes = (int) ( time % 60 );
			time -= ( minutes * 60 );
 		}
 		
 		seconds = (int) time;
 		
 		return hours+"h"+minutes+"m"+seconds+"s";
 	}
 }
