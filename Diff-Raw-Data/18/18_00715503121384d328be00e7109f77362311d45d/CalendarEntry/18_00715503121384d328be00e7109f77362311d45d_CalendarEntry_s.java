 /**
  *
  */
 package identity.calendar;
 
 import java.io.Serializable;
 import java.text.DateFormat;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.UUID;
 
 /**
  * @author jaremy
  *
  */
 public class CalendarEntry implements Serializable
 {
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = -2165686360188303307L;
 
 	/* Example Entry:
 	 * 17#2006/11/4 4:55pm#public#The fun begins now#90# 
 	 * 8953a618-3c1e-4bd8-a80a-0fe19f926394#17#2006/11/4 4:55pm#public#The fun begins now#90#
 	 */
 	public UUID uuid;
 	public Integer id;
 	public Date datetime;
 	public String status;
 	public String descr;
 	public Integer duration;
 
 	public static final String df = "MM/d/yyyy h:mma";
 	private static SimpleDateFormat formatter = null;
 
 	synchronized public static DateFormat getDF()
 	{
 		SimpleDateFormat formatter = new SimpleDateFormat(df);
 		return formatter;
 	}
 	synchronized public static Date dateFromString(String str) throws ParseException
 	{
 		if (formatter == null)
 			formatter = new SimpleDateFormat(df);
 		return formatter.parse(str);
 	}
 
 	public CalendarEntry()
 	{
 		this.uuid = null;
 		this.id = null;
 		this.datetime = null;
 		this.status = null;
 		this.descr = null;
 		this.duration = null;
 	}
 
 	/**
 	 * CalendarEntry
 	 *
 	 * @param 
 	 * @return 
 	 */
 	public CalendarEntry(UUID uuid, int id, Date datetime, String status, String descr, int duration )
 	{
 		this.uuid = uuid;
 		this.id = id;
 		this.datetime = datetime;
 		this.status = status;
 		this.descr = descr;
 		this.duration = duration;
 	}
 	
 	public CalendarEntry copy( )
 	{
 		CalendarEntry copy;
 		copy = new CalendarEntry(this.uuid, this.id, this.datetime, this.status, this.descr, this.duration );
 		return copy;
 	}
 	
 	public CalendarEntry(int id, Date datetime, String status, String descr, int duration ) {
 		this.uuid = null;
 		this.id = id;
 		this.datetime = datetime;
 		this.status = status;
 		this.descr = descr;
 		this.duration = duration;
 		// need to check and make sure this is correct.
 	}
 
 	public static CalendarEntry parseStringArray( String[] p, boolean hasUserId)
 	{
 		int i = 0;
 		UUID uuid = null;
 		try {
 			// optional set UUID
 			if (hasUserId)
 				uuid = UUID.fromString(p[i++]);
 			// parse the entry
 			int id = Integer.parseInt(p[i++]);
 			Date datetime = dateFromString(p[i++]);
 			String status = p[i++];
 			String descr = p[i++];
 			int duration = Integer.parseInt(p[i++]);
 			return new CalendarEntry(uuid, id, datetime, status, descr, duration);
 		}
 		catch (Exception e) {
 			System.err.println("Error: "+e);
 			return null;
 		}
 
 	}
 
 	public void setUuid(UUID uuid)
 	{
 		this.uuid = uuid;
 	}
 
 	public void privatizeDescr()
 	{
		if (status.toLowerCase().equals("private"))
 			descr = "";
 	}
 
 	public String toString()
 	{
 		String str;
 		str = (uuid == null) ? "" : uuid.toString() + "#";
 		str += id.toString() + "#";
 		str += getDF().format(datetime).toString() + "#";
 		str += status + "#";
 		str += descr.toString() + "#";
 		str += duration.toString() + "#";
 
 		return str;
 	}
 	
 }
 
