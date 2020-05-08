 package uk.ac.dur.duchess.data;
 
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 
 import uk.ac.dur.duchess.entity.Event;
 import uk.ac.dur.duchess.entity.Review;
 
 public class CalendarFunctions
 {
 	public static String getEventDate(Event event)
 	{
 		return getEventDate(event.getStartDate(), event.getEndDate());
 	}
 	
 	public static String getEventDate(String start, String end)
 	{
 		try
 		{
 			SimpleDateFormat sourceFormat = new SimpleDateFormat("yyyy-MM-dd");
 			SimpleDateFormat destinationFormat = new SimpleDateFormat("d MMMMM yyyy");
 			
 			Calendar startDate = Calendar.getInstance(); startDate.setTime(sourceFormat.parse(start));
 			Calendar endDate   = Calendar.getInstance(); endDate.setTime(sourceFormat.parse(end));
 			
 			String _startDate = destinationFormat.format(startDate.getTime());
 			String _endDate   = destinationFormat.format(endDate.getTime());
 			
 			Calendar today     = Calendar.getInstance();
 			Calendar tomorrow  = Calendar.getInstance(); tomorrow.roll(Calendar.DAY_OF_YEAR, true);
 			Calendar yesterday = Calendar.getInstance(); yesterday.roll(Calendar.DAY_OF_YEAR, false);
 						
 			if(isSameDate(startDate, today)) _startDate = "Today";
 			if(isSameDate(endDate, today)) _endDate = "Today";
 			if(isSameDate(startDate, tomorrow)) _startDate = "Tomorrow";
 			if(isSameDate(endDate, tomorrow)) _endDate = "Tomorrow";
 			if(isSameDate(startDate, yesterday)) _startDate = "Yesterday";
 			if(endDate.before(today))
 			{
 				_startDate = "This event has ended";
 				_endDate = "";
 			}
 			
 			if(isSameDate(startDate, endDate)) _endDate = "";
 			
 			return _startDate + ((_endDate.equals("")) ? "" : " until " + _endDate);
 		}
 		catch (ParseException e1)
 		{
 			return "Data Unavailable";
 		}
 	}
 	
 	public static boolean inRange(String start, String end, String from, String to)
 	{
 		SimpleDateFormat range = new SimpleDateFormat("d MMMMM yyyy");
 		SimpleDateFormat event = new SimpleDateFormat("yyyy-MM-dd");
 		
 		try
 		{
 			Calendar fromDate  = Calendar.getInstance();
 			if(from.matches("\\d{4}-\\d{1,2}-\\d{1,2}")) fromDate.setTime(event.parse(from));
 			else fromDate.setTime(range.parse(from));
 			
 			Calendar toDate    = Calendar.getInstance();
 			if(to.matches("\\d{4}-\\d{1,2}-\\d{1,2}")) toDate.setTime(event.parse(to));
 			else toDate.setTime(range.parse(to));
 
 			
 			Calendar startDate = Calendar.getInstance(); startDate.setTime(event.parse(start));
 			Calendar endDate   = Calendar.getInstance(); endDate.setTime(event.parse(end));
 			
			//the event must start before the 'until' date and end after (or on the same date as) the 'from' date 
			return (startDate.compareTo(toDate) < 0) && (endDate.compareTo(fromDate) >= 0);
 		}
 		catch (ParseException e)
 		{
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			return false;
 		}
 	}
 	
 	public static boolean isSameDate(Calendar a, Calendar b)
 	{
 		return a.get(Calendar.YEAR) == b.get(Calendar.YEAR)
 				&& a.get(Calendar.DAY_OF_YEAR) == b.get(Calendar.DAY_OF_YEAR);
 	}
 	
 	public static boolean isSameDate(String a, String b)
 	{
 		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
 		
 		try
 		{
 			Calendar date1 = Calendar.getInstance(); date1.setTime(sdf.parse(a));
 			Calendar date2 = Calendar.getInstance(); date2.setTime(sdf.parse(b));
 			
 			return isSameDate(date1, date2);
 		}
 		catch (ParseException e)
 		{
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			return false;
 		}
 	}
 	
 	public static int compareDates(String d1, String d2)
 	{
 		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
 		
 		try
 		{
 			Calendar date1 = Calendar.getInstance(); date1.setTime(sdf.parse(d1));
 			Calendar date2 = Calendar.getInstance(); date2.setTime(sdf.parse(d2));
 			
 			return date1.compareTo(date2);
 		}
 		catch (ParseException e)
 		{
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			return 0;
 		}
 	}
 	
 	public static String getReviewTime(Review review)
 	{
 		return getReviewTime(review.getTimestamp());
 	}
 	
 	public static String getReviewTime(String timeStamp)
 	{
 		try
 		{
 			SimpleDateFormat sourceFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
 			SimpleDateFormat destinationFormat = new SimpleDateFormat("d MMMMM yyyy");
 
 			Calendar today = Calendar.getInstance();
 			Calendar _timeStamp = Calendar.getInstance();
 			_timeStamp.setTime(sourceFormat.parse(timeStamp));
 
 			int days = today.get(Calendar.DATE) - _timeStamp.get(Calendar.DATE); 
 			int hours = today.get(Calendar.HOUR_OF_DAY) - _timeStamp.get(Calendar.HOUR_OF_DAY);  
 			int minutes = today.get(Calendar.MINUTE) - _timeStamp.get(Calendar.MINUTE);
 
 			System.out.println("Days: " + days + ", Hours: " + hours + ", Minutes: " + minutes);
 
 			if(days == 0 && hours == 0 && minutes == 0) return "Less than 1 minute ago";
 			else if(days == 0 && hours == 0) return minutes + " minute" + ((minutes != 1) ? "s" : "") + " ago";
 			else if(days == 0 && hours == 1 && minutes < 0) return 60 + minutes + " minute" + ((minutes != 1) ? "s" : "") + " ago";
 			else if(days == 0) return hours + " hour" + ((hours != 1) ? "s" : "") + " ago";
 			else if(days == 1 && hours < 0) return 24 + hours + " hour" + ((hours != 1) ? "s" : "") + " ago";
 
 			return destinationFormat.format(_timeStamp.getTime());
 		}
 		catch(ParseException pe) { return "Time of Post Unavailable"; }
 	}
 }
