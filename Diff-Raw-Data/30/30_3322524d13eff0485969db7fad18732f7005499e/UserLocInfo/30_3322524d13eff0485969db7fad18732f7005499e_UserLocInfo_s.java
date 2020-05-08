 package in.co.hopin.Users;
 
 import in.co.hopin.LocationHelpers.SBGeoPoint;
 import in.co.hopin.Util.StringUtils;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 
 /**
  * 
  * @author arpit87
  * this class parses the nearbyuser array that we get
  */
 public class UserLocInfo {
 	
 	private String userID = "";	
 	private String firstName = "";	
 	private String lastName = "";	
 	private String srclatitude = "";
 	private String srclongitude = "";
 	private String dstlatitude = "";
 	private String dstlongitude = "";
 	private SBGeoPoint geoPoint = null;
 	private String srcaddress = "";
 	private String srclocality = "";
 	private String dstaddress = "";
 	private String dstlocality = "";
 	private String time = "";
 	private String formattedTraveDetails = "";
 	private String formattedTimeDetails = "";
 	
 	
 	
 	private JSONObject srcLocjObj = null;
 	private JSONObject dstLocjObj = null;	
 		
 	
 	public UserLocInfo(JSONObject thisUserJobj)
 	{
 		try {
 			srcLocjObj = thisUserJobj.getJSONObject(UserAttributes.SRCINFO);
 			
 			try {									
 				srclatitude=srcLocjObj.getString(UserAttributes.SRCLATITUDE);			
 			} catch (JSONException e) {}
 			
 			try {									
 				srclongitude=srcLocjObj.getString(UserAttributes.SRCLONGITUDE);			
 			} catch (JSONException e) {}
 			
 			try {									
 				srcaddress = srcLocjObj.getString(UserAttributes.SRCADDRESS);			
 			} catch (JSONException e) {}
 			
 			try {									
 				srclocality = srcLocjObj.getString(UserAttributes.SRCLOCALITY);			
 			} catch (JSONException e) {}
 			
 		} catch (JSONException e) {}
 		
 		try {
 			dstLocjObj = thisUserJobj.getJSONObject(UserAttributes.DSTINFO);			
 
 			try {									
 				dstlatitude=dstLocjObj.getString(UserAttributes.DSTLATITUDE);			
 			} catch (JSONException e) {}
 			
 			try {									
 				dstlongitude=dstLocjObj.getString(UserAttributes.DSTLONGITUDE);			
 			} catch (JSONException e) {}
 			
 			try {									
 				dstaddress = dstLocjObj.getString(UserAttributes.DSTADDRESS);
 			} catch (JSONException e) {}
 			
 			try {									
 				dstlocality = dstLocjObj.getString(UserAttributes.DSTLOCALITY);
 			} catch (JSONException e) {}
 			
 		} catch (JSONException e) {}
 		
 		
 		try {
 			time = thisUserJobj.getString(UserAttributes.TIMEINFO);			
 		} catch (JSONException e) {}
 			
 		
 		calcUserGeopoint();
 			
 	}
 
 
 	public UserLocInfo() {
 		// TODO Auto-generated constructor stub
 	}
 
 
 	public String getUserSrcLocality()
 	{		
 		return srclocality;
 	}
 	
 	public String getUserSrcAddress()
 	{		
 		return srcaddress;
 	}
 	
 	public String getUserDstLocality()
 	{		
 		return dstlocality;
 	}
 	
 	public String getUserDstAddress()
 	{		
 		return dstaddress;
 	}
 	
 	public String getSrcLatitude()
 	{
 		return srclatitude;
 	}
 	
 	public String getSrcLongitude()
 	{
 		return srclongitude;
 	}
 	
 	public String getDstLatitude()
 	{
 		return dstlatitude;
 	}
 	
 	public String getDstLongitude()
 	{
 		return dstlongitude;
 	}
 	
 	public SBGeoPoint getGeoPoint() {
 		return geoPoint;
 	}
 	
 	public String getTimeOfTravel() {
 		return time;
 	}
 	
 
 	private void calcUserGeopoint()
 	{
 		if(srclatitude != "" && srclongitude != "")
 			geoPoint =  new SBGeoPoint((int)(Double.parseDouble(srclatitude)*1E6),(int)(Double.parseDouble(srclongitude)*1E6));
 	}
 	
 	public String getFormattedTravelDetails(int daily_insta_type)
 	{
 		String travelInfo = getUserSrcLocality() + " to " + getUserDstLocality();
 		String travelTimeInfo = getTimeOfTravel();			
 		if(daily_insta_type == 0)
			formattedTraveDetails = travelInfo + " Daily@"+StringUtils.formatDate("yyyy-MM-dd HH:mm:ss", "hh:mm a", travelTimeInfo);
 		
 		else
 			formattedTraveDetails = travelInfo +","+ StringUtils.formatDate("yyyy-MM-dd HH:mm:ss", "d MMM hh:mm a", travelTimeInfo);
 		
 		return formattedTraveDetails;
 	}
 	
 	public String getFormattedTimeDetails(int daily_insta_type)
 	{			
 		if(daily_insta_type == 0)
			formattedTimeDetails =  " Daily@"+StringUtils.formatDate("yyyy-MM-dd HH:mm:ss", "hh:mm a", getTimeOfTravel());
 		
 		else
 			formattedTimeDetails = StringUtils.formatDate("yyyy-MM-dd HH:mm:ss", "d MMM hh:mm a", getTimeOfTravel());
 		
 		return formattedTimeDetails;
 	}
 
 
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = 1;
 		result = prime * result
 				+ ((dstlatitude == null) ? 0 : dstlatitude.hashCode());
 		result = prime * result
 				+ ((dstlongitude == null) ? 0 : dstlongitude.hashCode());
 		result = prime * result
 				+ ((srclatitude == null) ? 0 : srclatitude.hashCode());
 		result = prime * result
 				+ ((srclongitude == null) ? 0 : srclongitude.hashCode());
 		return result;
 	}
 
 
 	@Override
 	public boolean equals(Object obj) {
 		if (this == obj)
 			return true;
 		if (obj == null)
 			return false;
 		if (getClass() != obj.getClass())
 			return false;
 		UserLocInfo other = (UserLocInfo) obj;
 		if (dstlatitude == null) {
 			if (other.dstlatitude != null)
 				return false;
 		} else if (!dstlatitude.equals(other.dstlatitude))
 			return false;
 		if (dstlongitude == null) {
 			if (other.dstlongitude != null)
 				return false;
 		} else if (!dstlongitude.equals(other.dstlongitude))
 			return false;
 		if (srclatitude == null) {
 			if (other.srclatitude != null)
 				return false;
 		} else if (!srclatitude.equals(other.srclatitude))
 			return false;
 		if (srclongitude == null) {
 			if (other.srclongitude != null)
 				return false;
 		} else if (!srclongitude.equals(other.srclongitude))
 			return false;
 		return true;
 	}
 	
 	
 }
