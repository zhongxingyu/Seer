 package com.example.payback;
 
 import java.util.Calendar;
 
 public class Notification {
 	private String fromEmail;
 	private String toEmail;
 	private String message;
 	private String date; //format: "Nov 17 2013, 12:19 AM"
 	
 	Notification (String from, String to, String message, String dateToParse)
 	{
 		this.fromEmail = from;
 		this.toEmail = to;
 		this.message = message;
 		//  0123456789012345
 		// "2013-01-08 01:53:36"
 	}
 	
 	
 	Notification (String from, String to, String message)
 	{
 		this.fromEmail = from;
 		this.toEmail = to;
 		this.message = message;
 		this.date = currDateToString();
 	}
 	Notification(){}
 	/*
 	static String parseDateToString(String input)
 	{
 		StringBuilder sb = new StringBuilder("");
 		String month;
 		String moInput = input.substring(5,7);
 		switch(Integer.parseInt(moInput))
 		{
 			case 0: month = "Jan"; break;
 			case 1: month = "Feb"; break;
 			case 2: month = "Mar"; break;
 			case 3: month = "Apr"; break;
 			case 4: month = "May"; break;
 			case 5: month = "Jun"; break;
 			case 6: month = "Jul"; break;
 			case 7: month = "Aug"; break;
 			case 8: month = "Sep"; break;
 			case 9: month = "Oct"; break;
 			case 10: month = "Nov"; break;
 			case 11: month = "Dec"; break;
 			default: throw new IllegalArgumentException("Month broken");
 		}
 		sb.append(month + " ");
 		sb.append(Integer.parseInt(input.substring(8,10)) + " ");
 		sb.append(input.substring(0,4) + ", ");
 		
 		boolean am = true;
 		int hour = Integer.parseInt(input.substring(11,13));
 		if(hour == 12)
 		{
 			am = false;
 		}
 		else if(hour > 12)
 		{
 			am = false;
 			hour -= 12;
 		}
 		else if(hour == 0)
 		{
 			hour = 12;
 		}
 		int min = Integer.parseInt(input.substring(14, 16));
 		if(am) 
 			sb.append(hour + ":" + min + " " + "AM");
 		else
 			sb.append(hour + ":" + min + " " + "PM");
 
 		
 		
 		return sb.toString();
 	}
 	*/
 	static String currDateToString()
 	{
 		Calendar c = Calendar.getInstance();
 		String min = c.get(Calendar.MINUTE) + "";
 		if(min.length() == 1) //changes 3 into 03
 			min = "0" + min;
 		int hour = c.get(Calendar.HOUR);
 		if (hour == 0)
 			hour = 12;
 		String am_pm = (c.get(Calendar.AM_PM) == 0) ? "AM" : "PM";
 		String month;
 		switch(c.get(Calendar.MONTH))
 		{
 			case 0: month = "Jan"; break;
 			case 1: month = "Feb"; break;
 			case 2: month = "Mar"; break;
 			case 3: month = "Apr"; break;
 			case 4: month = "May"; break;
 			case 5: month = "Jun"; break;
 			case 6: month = "Jul"; break;
 			case 7: month = "Aug"; break;
 			case 8: month = "Sep"; break;
 			case 9: month = "Oct"; break;
 			case 10: month = "Nov"; break;
 			case 11: month = "Dec"; break;
 			default: throw new IllegalArgumentException("Month broken");
 		}
 		String s = (month + " " + c.get(Calendar.DAY_OF_MONTH) + " " + c.get(Calendar.YEAR) + ", " + hour + ":" + min + " " +  am_pm);
 		//Should return a string that looks like "Nov 17 2013, 12:19 AM"
 		return s;
 	}
 	
 	public String getFromEmail() {
 		return fromEmail;
 	}
 	public void setFromEmail(String from) {
 		this.fromEmail = from;
 	}
 	public String getToEmail() {
 		return toEmail;
 	}
 	public void setToEmail(String to) {
 		this.toEmail = to;
 	}
 	public String getMessage() {
 		return message;
 	}
 	public void setMessage(String message) {
 		this.message = message;
 	}
 	
 	public String getDate() {
 		return date;
 	}
 	public void setDate(String date) {
 		this.date = date;
 	}
 }
