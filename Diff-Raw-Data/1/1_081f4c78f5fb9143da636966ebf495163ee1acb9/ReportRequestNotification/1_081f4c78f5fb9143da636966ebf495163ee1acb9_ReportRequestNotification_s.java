 package it.giacomos.android.osmer.pro.service.sharedData;
 
 public class ReportRequestNotification extends NotificationData
 {
     public String locality;
 	public boolean isRequest;
 	private boolean mValidString;
 	private boolean mIsReadable;
 	
 	public boolean isValid()
 	{
 		return mValidString && latitude > 0 && longitude > 0 && getDate() != null;
 	}
 	
 	public ReportRequestNotification(String input)
 	{
 		super();
 		
 		String parts[] = input.split("::", -1);
 		mValidString = (parts.length == 7 || parts.length == 8);
 		if(mValidString)
 		{
 			isRequest = (parts[0].compareTo("Q") == 0);
 			mIsReadable = (parts[1].compareTo("r") == 0);
 			datetime = parts[2];
 			username = parts[3];
 			try
 			{
 				latitude = Double.parseDouble(parts[4]);
 				longitude = Double.parseDouble(parts[5]);
 			}
 			catch(NumberFormatException e)
 			{
 				
 			}
 			locality = parts[6];
 			makeDate(datetime);
 			if(parts.length > 7)
 				mIsConsumed = (parts[7].compareTo("consumed") == 0);
 			/* otherwise mIsConsumed remains false */
 		}
 	}
 	
 	public ReportRequestNotification(String datet, String user, double lat, double lon, String loc)
 	{
 		super();
 		datetime = datet;
 		username = user;
 		latitude = lat;
 		longitude = lon;
 		locality = loc;
 		mValidString = true; /* for is valid */
 		isRequest = true;
 		makeDate(datetime);
 	}
 
 	@Override
 	public short getType() {
 		
 		return NotificationData.TYPE_REQUEST;
 	}
 
 	@Override
 	public String toString() 
 	{
 		String ret = "Q::";
 		if(mIsReadable)
 			ret += "r::";
 		else
 			ret += "w::";
 		ret += datetime + "::" + username + "::" + String.valueOf(latitude) + "::";
 		ret += String.valueOf(longitude) + "::" + locality;
 		
 		if(mIsConsumed)
 			ret += "::consumed";
 		return ret;
 	}
 }
