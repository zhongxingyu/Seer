 package se.mah.kd330a.project.schedule.data;
 
 import java.util.ArrayList;
 
 import android.util.Log;
 
 import se.mah.kd330a.project.schedule.model.*;
 
 public class ParseData {
 	
 	ArrayList<ScheduleWeek> scheduleWeeks;
 	
 	public ParseData() {
 		scheduleWeeks = new ArrayList<ScheduleWeek>();
 		
 	}
 	
 	public ArrayList<ScheduleWeek> getParsedDataFromKronoxByWeek() {
 		for(int i = 43; i < 46; i++) {
 			ScheduleWeek scheduleWeek = new ScheduleWeek();
 			scheduleWeek.setWeekNumber(i);
 			Log.i("getParsedDataFromKronoxByWeek()", Integer.toString(i));
 			ArrayList<ScheduleItem> sampleWeekList = new ArrayList<ScheduleItem>();
 			for (int j = 0; j < 3; j++ ){
 				ScheduleItem sampleClass = new ScheduleItem();
 				sampleClass.setStartTime("1"+ j +":00");
 				sampleClass.setEndTime("1"+ (j+1) +":00");
 				sampleClass.setCourseName("Course with a long title and too much information " + i +" "+ j);
 				sampleClass.setAddtionalInformation("The class will be hold as a " +
 						"seminar so please come well prepared to class");
 				sampleClass.setLocation("K2C12" + j);
				sampleClass.setLector("Lector "+ i + " " + j);
 				int date = (i-19) + ((i-40)*7);
 				if(j==0 || j==1){
 					sampleClass.setDate("Monday " + date + "/10/2013");
 				}
 				else {
 					sampleClass.setDate("Wednesday " + date + 2 + "/10/2013");
 				}
 				sampleWeekList.add(sampleClass);
 				Log.i("getParsedDataFromKronoxByWeek()", sampleClass.getCourseName());
 			}
 			scheduleWeek.setScheduleItems(sampleWeekList);
 			scheduleWeeks.add(scheduleWeek);
 		}
 		return scheduleWeeks;
 	}
 	
 
 }
