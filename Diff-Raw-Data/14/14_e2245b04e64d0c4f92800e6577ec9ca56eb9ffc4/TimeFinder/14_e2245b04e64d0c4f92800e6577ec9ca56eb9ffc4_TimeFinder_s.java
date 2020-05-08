 package ftf;
 import java.util.ArrayList;
 import java.util.PriorityQueue;
 
import calendar.*;
import org.joda.time.*;
 
 public class TimeFinder {
 
 	Response[] findBestTimes(CalendarGroup e, int interval, int duration, int numToReturn, int minAttendees){
 		
 		int tStep = 60/interval;
 		
 		ArrayList<Calendar> calendars = (ArrayList<Calendar>) e.getCalendars();
 		if(calendars.size() <= 0){
 			return null;
 		}
 		
 		
 		int dayLen = calendars.get(0).getEndTime().getHourOfDay() - calendars.get(0).getStartTime().getHourOfDay();
 		int m = (calendars.get(0).getEndTime().getDayOfYear() - calendars.get(0).getStartTime().getDayOfYear() + 1)*dayLen*tStep;
 		int n = calendars.size();
 		int[][] freeTimes = new int[m][n];
 		for(int r=0; r<m; r++){
 			for(int c=0; c<n; c++){
 				freeTimes[r][c] = 1;
 			}
 		}
 		
 		int col=0, row=0;
 		for(Calendar cal : calendars){
 			ArrayList<Response> responses = (ArrayList<Response>) cal.getResponses();
 			for(Response r : responses){
 				row = (r.getStartTime().getDayOfYear() - cal.getStartTime().getDayOfYear())*dayLen*tStep + (r.getStartTime().getMinuteOfDay() - cal.getStartTime().getMinuteOfDay())*tStep;
 				int end = (r.getEndTime().getDayOfYear() - cal.getStartTime().getDayOfYear())*dayLen*tStep + (r.getEndTime().getMinuteOfDay() - cal.getStartTime().getMinuteOfDay())*tStep;
 				while(row <= end){
 					freeTimes[row][col] = 0;
 					row++;
 				}
 			}
 			col++;
 		}
 		
 		
 		PriorityQueue<TimeAvailabilityPair> times = calculateTimes(freeTimes, interval, duration, minAttendees);
 	
 		int i=0, num;
 		if(times.size()<numToReturn){
 			num = times.size();
 		}
 		else{
 			num = numToReturn;
 		}
 		Response[] toReturn = new Response[num];
 		while(i<num){
 			TimeAvailabilityPair t = times.poll();
 			int startDay = t.getTime()/(dayLen*tStep);
 			int startTime = t.getTime() % (dayLen*tStep);
 			toReturn[i] = new Response(calendars.get(0).getStartTime().plusDays(startDay).plusMinutes(startTime), calendars.get(0).getStartTime().plusDays(startDay).plusMinutes(startTime+duration));
 			i++;
 		}
 		
 		return toReturn;
 	}
 	
 	PriorityQueue<TimeAvailabilityPair> calculateTimes(int[][] times, int interval, int duration, int minAttendees){
 		
 		int[] result = new int[times.length];
 		int span = duration/interval - 1;
 		//initialize all entries to 0
 		for(int i=0; i<result.length; i++){
 			result[i] = 0;
 		}
 		
 		for(int row=0; row<times.length; row++){
 			for(int col=0; col<times[0].length; col++){
 				result[row] += times[row][col];
 			}
 		}
 		for(int i=0; i<result.length; i++){
 			if(i+span < result.length){
 				for(int j=i+1; j<= i+span; j++){
 					result[i] += result[j];
 				}
 			}
 			else{
 				result[i] = 0;
 			}
 		}
 		PriorityQueue<TimeAvailabilityPair> bestTimes = new PriorityQueue<TimeAvailabilityPair>();
 		int lastIn = 0;
 		for(int curr=0; curr<result.length; curr++){
 			if((bestTimes.isEmpty() || result[curr] != result[lastIn]) && result[curr]>minAttendees){
 				bestTimes.add(new TimeAvailabilityPair(curr, result[curr]));
 				lastIn = curr;
 			}
 		}
 		
 		return bestTimes;
 	}
 	
 	private class TimeAvailabilityPair implements Comparable<TimeAvailabilityPair>{
 		
 		private int _startTime, _numAvailable;
 		
 		public TimeAvailabilityPair(int startTime, int numAvailable){
 			_startTime = startTime;
 			_numAvailable = numAvailable;
 		}
 		
 		public int getTime(){
 			return _startTime;
 		}
 		
 		public int getAttendance(){
 			return _numAvailable;
 		}
 
 		@Override
 		public int compareTo(TimeAvailabilityPair o) {
 			if(_numAvailable < o.getAttendance()){
 				return 1;
 			}
 			else if(_numAvailable > o.getAttendance()){
 				return -1;
 			}
 			else{
 				return 0;
 			}
 		}
 	}
 }
 
