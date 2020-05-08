 package ted.datastructures;
 
 import java.net.URL;
 import java.text.DateFormat;
 import java.util.Calendar;
 import java.util.GregorianCalendar;
 
 public class DailyDate  extends StandardStructure
 {
 	private int Year=0;
 	private int Month=0;
 	private int Day=0;
 	
 	private int seeders;
 	private URL url;
 	
 	/**
 	 * This method creates an empty DailyDate object
 	 */
 	public DailyDate()
 	{
 	}
 	
 	/**
 	 * This method creates a DailyDate with a set date
 	 */
 	public DailyDate(int day, int month, int year)
 	{
 		this.Day   = day;
 		this.Month = month;
 		this.Year  = year;
 	}
 	
 	/**
 	 * This method creates a DailyDate with a set date
 	 */
 	public DailyDate(int day, int month, int year, String title)
 	{
 		this.Day   = day;
 		this.Month = month;
 		this.Year  = year;
 		this.title = title;
 	}
 	
 	public boolean equals (DailyDate dd)
 	{
 		return (this.Day == dd.Day && this.Month == dd.Month && this.Year == dd.Year);
 	}
 	
 	/**
 	 * 
 	 * @param year The year to be set in this DailyDate
 	 */
 	public void setYear(int year)
 	{
 		this.Year = year;
 	}
 	
 	/**
 	 * 
 	 * @param month The month to be set in this DailyDate, set month-1 as months are
 	 * 0-based for a GregorianCalendar
 	 */
 	public void setMonth(int month)
 	{
 		this.Month = month;
 	}
 	
 	/**
 	 * 
 	 * @param day The day to be set in this DailyDate
 	 */
 	public void setDay(int day)
 	{
 		this.Day = day;
 	}
 	
 	/**
 	 * 
 	 * @return The year of this DailyDate
 	 */
 	public int getYear()
 	{
 		return this.Year;
 	}
 	
 	/**
 	 * 
 	 * @return The month of this DailyDate, the number of the month is -1 as the
 	 * GregorianCaledar is 0-based
 	 */
 	public int getMonth()
 	{
 		return this.Month;
 	}
 	
 	/**
 	 * 
 	 * @return The day of this DailyDate
 	 */
 	public int getDay()
 	{
 		return this.Day;
 	}
 	
 	/**
 	 * 
 	 * @return a GregorianCalendar containing the date of this DailyDate
 	 */
 	public GregorianCalendar getDate()
 	{
 		GregorianCalendar d = new GregorianCalendar(Year, Month, Day);
 		return d;
 	}
 	
 	/**
 	 * Compares this DailyDate with a second date.
 	 * @return 1 if this date is larger, -1 if this date is lower and 0 if dates are equal
 	 */
	public int compareTo(Object arg0)
 	{
 		DailyDate second = (DailyDate) arg0;
 		
 		if(this.getDate().getTimeInMillis()<second.getDate().getTimeInMillis())
 		{
 			return 1;
 		}
 		else if(this.getDate().getTimeInMillis()>second.getDate().getTimeInMillis())
 		{
 			return -1;
 		}
 		
 		return 0;
 	}
 	
 	/**
 	 * @return a String representing the date of this DailyDate
 	 */
 	public String toString()
 	{
 		DateFormat df = DateFormat.getDateInstance(DateFormat.LONG);
 		return df.format(this.getDate().getTime());
 	}
 
 	/**
 	 * 
 	 * @return an int of the ammount of seeders for this item 
 	 */
 	public int getSeeders() 
 	{
 		return seeders;
 	}
 
 	/**
 	 * 
 	 * @param seeders the ammount of seeders for this item
 	 */
 	public void setSeeders(int seeders) 
 	{
 		this.seeders = seeders;
 	}
 
 	/**
 	 * 
 	 * @return the url containing the torrent location of this item
 	 */
 	public URL getUrl() 
 	{
 		return url;
 	}
 
 	/**
 	 * 
 	 * @param url the url containing the torrent location of this item
 	 */
 	public void setUrl(URL url) 
 	{
 		this.url = url;
 	}
 
 	/**
 	 * 
 	 * @param date sets the Day, Month and Year for this DailyDate
 	 */
 	public void setDate(GregorianCalendar date) 
 	{
 		this.setDay(date.get(Calendar.DAY_OF_MONTH));
 		this.setYear(date.get(Calendar.YEAR));
 		this.setMonth(date.get(Calendar.MONTH));		
 	}
 	
 	public String getSearchString()
 	{
 		return this.toString();
 	}
 	
 }
