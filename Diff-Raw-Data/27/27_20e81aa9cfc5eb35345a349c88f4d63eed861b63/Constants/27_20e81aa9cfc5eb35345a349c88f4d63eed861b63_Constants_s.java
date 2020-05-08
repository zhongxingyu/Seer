 package UI;
 
 import java.text.DateFormat;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Date;
 
 public class Constants {
 
 
 	public static String RETURN_TO_CHOOSE_USER_DIALOG = "Return to Choose User Dialog";
 	public static String OK = "Ok";
 	public static String SUCCESS = "Success";
 	public static String ERROR = "Error";
 	public static String AN_ERROR_OCCURRED = "An error occurred.  Please check your inputs and try again.";
 	public static String IN = "in";
 	public static String OUT = "out";
	public static String ON_HOLD = "on hold";
 	public static String STUDENT = "student";
 	public static String FACULTY = "faculty";
 	public static String STAFF = "staff";
 	public static String GEN_PUB = "general public";
 	public static String NULL = "null";
 	
 	public static String getCurrentDateInStringFormat()
 	{
 		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
 		Date date = new Date();
 		return dateFormat.format(date);
 	}
 	
 	public static Date stringToDate(String s)
 	{
 		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
 		try
 		{
 			return dateFormat.parse(s);
 		}catch(Exception e){
 			return null;
 			
 		}
 	}
 	public static String DateToString(Date d)
 	{
 		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
 		return dateFormat.format(d);
 	}
 	
 	public static Date getReturnDateGivenOutDate(String borrowerType, Date outDate)
 	{
 		Calendar c = Calendar.getInstance();
 		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
 		
 		c.setTime(outDate);
 		if (borrowerType.equals(Constants.STAFF))
 		{
 			c.add(Calendar.DATE, 42);
 		}else if (borrowerType.equals(Constants.STUDENT))
 		{
 			c.add(Calendar.DATE, 14);
 		}else if (borrowerType.equals(Constants.FACULTY))
 		{
 			c.add(Calendar.DATE, 84);
 		}else
 		{
 			c.add(Calendar.DATE, 14);
 		}
 
 		return c.getTime();
 		 
 	}
 }
