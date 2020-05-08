 package edu.berkeley.cs160.clairetuna.prog3;
 
 public abstract class TimeHelper {
 
 	public static String difference(String timeNow, String timeLeave){
 		//12:43 AM
 		System.out.println("TIMENOW IS: " + timeNow + "TIME OF DEPARTURE IS "+ timeLeave);
 		int hoursNow = Integer.parseInt(timeNow.split(":")[0]);
 		int hoursLeave = Integer.parseInt(timeLeave.split(":")[0]);
 		if (timeNow.contains("AM") && timeLeave.contains("PM")){
 			hoursLeave+=12;
 		}
 		if (timeNow.contains("PM") && timeLeave.contains("AM")){
 			hoursLeave+=12;
 		}
 		int minutesNow = Integer.parseInt(timeNow.split(":")[1].substring(0, 2));
 		int minutesLeave = Integer.parseInt(timeLeave.split(":")[1].substring(0, 2));
 		int minutesDifferent;
 		int hoursDifferent = hoursLeave - hoursNow;
 		if (minutesLeave < minutesNow&& hoursNow!=hoursLeave){
 			minutesDifferent = 60 - minutesNow + minutesLeave;
 			hoursDifferent-=1;
 		}
 		else{
 			minutesDifferent = minutesLeave - minutesNow;
 		}
 		
 		String toReturn="";
 		if (hoursDifferent !=0){
 			if (hoursDifferent == 1){
 				toReturn += hoursDifferent + " hour ";
 			}
 			else {
 				toReturn += hoursDifferent + " hours ";
 			}
 		}
 		if (minutesDifferent == 1){
 			toReturn+= minutesDifferent + " mins";
 		}
 		else{
 		toReturn+= minutesDifferent + " mins";
 		}
 		System.out.println("minutesDifferent is : " + minutesDifferent);
 		System.out.println("hoursDifferent is : " + hoursDifferent);
 		return toReturn;
 	}
 	
 	
 	/** returns whether the train leaves after the current time*/
 	public static boolean isAfter (String timeNow, String timeLeave){
 		int hoursNow = Integer.parseInt(timeNow.split(":")[0]);
 		int hoursLeave = Integer.parseInt(timeLeave.split(":")[0]);
 		boolean isAfter = false;
 		if (hoursNow == 11 && timeNow.contains("AM") && hoursLeave==12 && timeLeave.contains("PM")){
 			isAfter = true;
 		}
 		if (hoursNow == 11 && timeNow.contains("PM") && hoursLeave==12 && timeLeave.contains("AM")){
 			isAfter = true;
 		}
 		
 		//SAME AM/PM  VALUE
 		else {
 
			int minutesNow = Integer.parseInt(timeNow.split(":")[1].substring(0, 2))-20;
 			int minutesLeave = Integer.parseInt(timeLeave.split(":")[1].substring(0, 2));
 			if (hoursNow == hoursLeave){
 				isAfter = minutesNow < minutesLeave;
 			}
 			else if (hoursLeave > hoursNow){
 				isAfter = true;
 			}
 		}
 
 		return isAfter;
 	}
 	
 }
