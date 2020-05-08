 package ftf;
 import java.util.ArrayList;
 import java.util.PriorityQueue;
 
 import calendar.*;
 import org.joda.time.*;
 
 public class TimeFinder {
 	
 	public int dayLen(DateTime start, DateTime end){
 		return end.getMinuteOfDay() - start.getMinuteOfDay();
 	}
 	
 	public int numIntervalsInDay(DateTime start, DateTime end, int interval){
 		return this.dayLen(start, end)/interval;
 	}
 
 	public Response[] findBestTimes(CalendarGroup e, int interval, int duration, int numToReturn, int minAttendees){
 		
 		ArrayList<Calendar> calendars = (ArrayList<Calendar>) e.getCalendars();
 		if(calendars.size() <= 0){
 			return null;
 		}
 		
 		
 		int dayLen = calendars.get(0).getEndTime().getMinuteOfDay() - calendars.get(0).getStartTime().getMinuteOfDay();
 		int m = (calendars.get(0).getEndTime().getDayOfYear() - calendars.get(0).getStartTime().getDayOfYear() + 1)*dayLen/interval;
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
				System.out.println(r.getStartTime().hourOfDay().getAsText() + ":" + r.getStartTime().minuteOfHour().getAsText() + ", " + cal.getEndTime().hourOfDay().getAsText() + ":" + cal.getEndTime().minuteOfHour().getAsText());
 				row = (r.getStartTime().getDayOfYear() - cal.getStartTime().getDayOfYear())*dayLen/interval + (r.getStartTime().getMinuteOfDay() - cal.getStartTime().getMinuteOfDay())/interval;
 				int end = (r.getEndTime().getDayOfYear() - cal.getStartTime().getDayOfYear())*dayLen/interval + (r.getEndTime().getMinuteOfDay() - cal.getStartTime().getMinuteOfDay())/interval;
				while(row < end){
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
 			int startDay = t.getTime()/(dayLen/interval);
 			int startTime = t.getTime() % (dayLen/interval);
 			toReturn[i] = new Response(calendars.get(0).getStartTime().plusDays(startDay).plusMinutes(startTime*interval), calendars.get(0).getStartTime().plusDays(startDay).plusMinutes(startTime*interval+duration));
 			i++;
 		}
 		
 		return toReturn;
 	}
 	
 	public PriorityQueue<TimeAvailabilityPair> calculateTimes(int[][] times, int interval, int duration, int minAttendees){
 		
 		int[] result = new int[times.length];
 		int span = duration/interval - 1;
 		//initialize all entries to 0
 		for(int i=0; i<result.length; i++){
 			result[i] = 0;
 		}
 		
 		System.out.println("FIRST:");
 		for(int row=0; row<times.length; row++){
 			for(int col=0; col<times[0].length; col++){
 				result[row] += times[row][col];
 			}
 			System.out.println(row + ": " + result[row]);
 		}
 		System.out.println("SECOND:");
 		for(int i=0; i<result.length; i++){
 			if(i+span < result.length){
 				for(int j=i+1; j<= i+span; j++){
 					result[i] += result[j];
 				}
 			}
 			else{
 				result[i] = 0;
 			}
 			System.out.println(i + ": " + result[i]);
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
 	
 	public static void main(String[] args) {
 		TimeFinder tF = new TimeFinder();
 		int[][] times = {
 				{0,0,1,1},
 				{0,1,1,1},
 				{1,0,0,1},
 				{1,1,1,1},
 				{0,1,1,1}
 		};
 		PriorityQueue<TimeAvailabilityPair> q = tF.calculateTimes(times, 15, 30, 1);
		System.out.println("QUEUE 1:");
 		int size = q.size();
 		for(int i=0; i<size; i++){
 			TimeAvailabilityPair t = q.poll();
 			System.out.println(t.getTime() + ", " + t.getAttendance());
 		}
 		
 		int[][] times2 = {
 				{0,1,1,1,0,1,1,0,0,0,0},
 				{1,0,0,1,0,1,0,1,1,0,0},
 				{1,1,1,1,1,1,0,0,0,0,0},
 				{1,0,0,0,0,0,0,0,0,0,0},
 				{1,1,1,1,1,1,1,1,1,1,1},
 				{0,0,0,1,1,0,0,1,0,1,1},
 				{1,0,1,1,1,1,1,1,1,1,1},
 				{0,0,1,0,0,1,1,0,0,0,1},
 				{1,0,1,0,1,0,0,0,0,1,1},
 				{1,0,0,1,1,0,1,1,0,0,0},
 				{1,1,1,0,0,0,0,0,1,1,1},
 				{1,1,1,1,1,1,0,0,0,0,0},
 				{0,0,1,0,1,1,0,0,1,1,1},
 				{1,1,1,1,1,0,0,0,1,0,1},
 				{0,0,1,1,0,1,0,0,1,1,1}	
 		};
 		q = tF.calculateTimes(times2, 5, 45, 3);
 		System.out.println("QUEUE:");
 		size = q.size();
 		for(int i=0; i<size; i++){
 			TimeAvailabilityPair t = q.poll();
 			System.out.println(t.getTime() + ", " + t.getAttendance());
 		}
 		
 		DateTime start = new DateTime();
 		DateTime end = start.plusMinutes(240);
 		System.out.println("dayLen: "+ tF.dayLen(start, end));
 		System.out.println("numInterval 60min: " + tF.numIntervalsInDay(start, end, 60));
 		System.out.println("numInterval 30min: " + tF.numIntervalsInDay(start, end, 30));
 		System.out.println("numInterval 15min: " + tF.numIntervalsInDay(start, end, 15));
 		end = start.plusDays(4).plusMinutes(240);
 		System.out.println("num days: " + start.dayOfWeek().getAsText() + "-" + end.dayOfWeek().getAsText() + ", "+ (end.getDayOfYear() - start.getDayOfYear() + 1));
 		System.out.println("interval=60; " + start.dayOfWeek().getAsText() + ", " + start.hourOfDay().getAsText() + ":"+ start.minuteOfHour().getAsText() +"-" + end.dayOfWeek().getAsText()+ ", " + end.hourOfDay().getAsText() + ":"+ end.minuteOfHour().getAsText() + "; m=" +(end.getDayOfYear() - start.getDayOfYear() + 1)*tF.numIntervalsInDay(start, end, 60));
 		System.out.println("interval=30; " + start.dayOfWeek().getAsText() + ", " + start.hourOfDay().getAsText() + ":"+ start.minuteOfHour().getAsText() +"-" + end.dayOfWeek().getAsText()+ ", " + end.hourOfDay().getAsText() + ":"+ end.minuteOfHour().getAsText() + "; m=" +(end.getDayOfYear() - start.getDayOfYear() + 1)*tF.numIntervalsInDay(start, end, 30));
 		System.out.println("interval=15; " + start.dayOfWeek().getAsText() + ", " + start.hourOfDay().getAsText() + ":"+ start.minuteOfHour().getAsText() +"-" + end.dayOfWeek().getAsText()+ ", " + end.hourOfDay().getAsText() + ":"+ end.minuteOfHour().getAsText() + "; m=" +(end.getDayOfYear() - start.getDayOfYear() + 1)*tF.numIntervalsInDay(start, end, 15));
 		
 		DateTime r = end.minusDays(4).minusMinutes(120);
 		System.out.println("row for interval 60 on day 1: " + ((r.getDayOfYear() - start.getDayOfYear())*tF.numIntervalsInDay(start, end, 60) + (r.getMinuteOfDay() - start.getMinuteOfDay())/60));
 		DateTime toCompare = start.plusDays(2/(tF.numIntervalsInDay(start, end, 60))).plusMinutes((2 % (tF.numIntervalsInDay(start, end, 60)))*60);
 		System.out.println(r.dayOfWeek().getAsText() + ", "+ r.hourOfDay().getAsText() + ":" + r.minuteOfHour().getAsText());
 		System.out.println(toCompare.dayOfWeek().getAsText() + ", "+ toCompare.hourOfDay().getAsText() + ":" + toCompare.minuteOfHour().getAsText());
 		r = r.plusDays(1);
 		System.out.println("row for interval 60 on day 2: " + ((r.getDayOfYear() - start.getDayOfYear())*tF.numIntervalsInDay(start, end, 60) + (r.getMinuteOfDay() - start.getMinuteOfDay())/60));
 		r = start;
 		System.out.println("row for interval 60 on day 0: " + ((r.getDayOfYear() - start.getDayOfYear())*tF.numIntervalsInDay(start, end, 60) + (r.getMinuteOfDay() - start.getMinuteOfDay())/60));
 		
 	}
 }
 
