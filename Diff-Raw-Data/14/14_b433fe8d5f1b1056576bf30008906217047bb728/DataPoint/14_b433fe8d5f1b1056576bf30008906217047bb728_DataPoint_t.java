 package jp.ac.tsukuba.cs.conclave.earthquake.data;
 
 import java.util.logging.Logger;
 
 import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
 import org.joda.time.format.ISODateTimeFormat;
 
 public class DataPoint implements Comparable<DataPoint> {
 
 	private static Logger logger; 
 	
 	public double longitude;
 	public double latitude;
 		
 	public double magnitude;
 	public double depth; 
 	
 	public DateTime time; // time when the event happened
 	
 	public boolean FM; // this data point has Focus Mechanism information
 	
 	// Focus mechanism data
 	public int S[];
 	public int D[];
 	public int R[];
 	
 	/* Token Index is the position of data in each data file type.
 	 * Token order: Y,    M,   D, H, min, sec, long,  lag,  Mag,   Depth
 	 * "JMA" -      long, lat, Y, M, D,   Mag, Depth, H   , min,   sec
 	 * "Fnet" -    Y,    M,   D, H, min, sec, lat,   long, Depth, Mag, S1, S2, D1, D2, R1, R2
 	 */
 	private static int[][] ti = 
 		{
 			{2,3,4,7,8,9,0,1,5,6},//jma
 			{0,1,2,3,4,5,7,6,9,8}//fnet
 		};
 	
 	
 	
 	DataPoint(String s, String t)
 	{
 		initlogger();
 		
 		// Removing the initial space;
 		int i;
 		int type;
 		for (i = 0; s.charAt(i) == ' '; i++);
 		s = s.substring(i);		
 		FM = false;
 		
 		if (t.equalsIgnoreCase("JMA"))
 			type = 0;
 		else if	 (t.equalsIgnoreCase("fnet"))
 			type = 1;
 		else
 		{
 			logger.severe("Undefined Data Type passed: "+t);
 			return;
 		}
 		
 		
 		String token[] = s.split(" +");
 
 		time = new DateTime(
 				Integer.parseInt(token[ti[type][0]]), 
 				Integer.parseInt(token[ti[type][1]]),
 				Integer.parseInt(token[ti[type][2]]), 
 				Integer.parseInt(token[ti[type][3]]), 
 				Integer.parseInt(token[ti[type][4]]), 
				(int) Math.floor(Double.parseDouble(token[ti[type][5]])), DateTimeZone.UTC);
 		
 		/* fnet time stamps are in UCT - converting them to Japanese time (shouldn't actually do that) */
 		if (type == 1)
 			time = time.plusHours(9);
 		
 		
 		longitude = Double.parseDouble(token[ti[type][6]]);
 		latitude = Double.parseDouble(token[ti[type][7]]);
 
 		magnitude = Double.parseDouble(token[ti[type][8]]);
 		depth = Double.parseDouble(token[ti[type][9]]);
 		
 		if ((t.equalsIgnoreCase("fnet")))
 		{
 			S = new int[2];
 			D = new int[2];
 			R = new int[2];
 			FM = true;
 			S[0] = Integer.parseInt(token[10]);
 			S[1] = Integer.parseInt(token[11]);
 			D[0] = Integer.parseInt(token[12]);
 			D[1] = Integer.parseInt(token[13]);
 			R[0] = Integer.parseInt(token[14]);
 			R[1] = Integer.parseInt(token[15]);
 		}
 
 
 	}
 	
 	/**
 	 * Copy constructor
 	 * @param o
 	 */
 	public DataPoint(DataPoint o) {
 		initlogger();
 		
 		longitude = o.longitude;
 		latitude = o.latitude;
 		magnitude = o.magnitude;
 		depth = o.depth;
		time = new DateTime(o.time, DateTimeZone.UTC);
 		FM = o.FM;
 		S = new int[2];
 		D = new int[2];
 		R = new int[2];
 		if (o.FM)
 			for (int i = 0; i < 2; i++)
 			{
 				S[i] = o.S[i];
 				D[i] = o.D[i];
 				R[i] = o.R[i];
 			}
 	}
 
 	private void initlogger()
 	{
 		if (logger == null)
 			logger = Logger.getLogger(DataPoint.class.getName());
 	}
 
 	
 	
 	/**
 	 * @return a text string with all the data in this Data Point
 	 */
 	public String dump()
 	{
 		String ret = "[Event] ";
 		ret = ret + getEventString();
 		if (FM)
 		{
 			ret = ret + ", M1: S"+S[0]+", D"+D[0]+", R"+R[0];
 			ret = ret + ", M2: S"+S[1]+", D"+D[1]+", R"+R[1];
 		}
 		return ret;
 	}
 	
 	public String getEventString()
 	{
 		String ret = "";
 		ret = ret +longitude+","+latitude+", ";
 		ret = ret + "M:"+magnitude+", D:"+depth;
 		ret = ret + ", "+ time.toString(ISODateTimeFormat.basicDateTimeNoMillis());
 		return ret;
 	}
 	
 	@Override
 	public String toString()
 	{
 		return this.dump();
 	}
 	
 	
 	@Override
 	public int compareTo(DataPoint arg0) {
 		
 		return (time.compareTo(arg0.time));		
 	}
 	
 	/**
 	 * This function tests two Data points to see if they refer to the same event.
 	 * Two DataPoints are the same event if they have exactly the same time, magnitude, location and depth
 	 * @param arg0
 	 * @return
 	 */
 	public boolean isEqualTo(DataPoint arg0)
 	{
 		if (!this.time.equals(arg0.time))
 			return false;			
 		if (this.longitude != arg0.longitude)
 			return false;
 		if (this.latitude != arg0.latitude)
 			return false;
 		if (this.magnitude != arg0.magnitude)
 			return false;
 		
 		return true;
 	}
 
 	/** 
 	 * Returns true if this event contains information about fault models;
 	 * @return
 	 */
 	public boolean hasFaultModel()
 	{
 		return FM;
 	}
 	
 }
