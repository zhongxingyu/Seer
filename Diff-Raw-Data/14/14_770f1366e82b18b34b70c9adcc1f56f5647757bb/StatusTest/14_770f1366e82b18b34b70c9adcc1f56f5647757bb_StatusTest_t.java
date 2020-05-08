 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package twitz.testing;
 
 import java.util.Date;
 import twitter4j.GeoLocation;
 import twitter4j.Place;
 import twitter4j.RateLimitStatus;
 import twitter4j.Status;
 import twitter4j.User;
 
 
 /**
  *
  * @author mistik1
  */
 public class StatusTest implements Status{
 
	private Date created = new Date();

 	public StatusTest(){}
 	
 	public Date getCreatedAt()
 	{
		return created;
 		//throw new UnsupportedOperationException("Not supported yet.");
 	}
 
 	public long getId()
 	{
 		long r = 1234567890234525339L;
 		return r;
 		//throw new UnsupportedOperationException("Not supported yet.");
 	}
 
 	public String getText()
 	{
 		return "this is my test tweet, if this works then we know that something is going on right. It has just enough charactors for a full twitter message.";
 		//throw new UnsupportedOperationException("Not supported yet.");
 	}
 
 	public String getSource()
 	{
 		return "this is my test tweet, if this works then we know that something is going on right";
 		//throw new UnsupportedOperationException("Not supported yet.");
 	}
 
 	public boolean isTruncated()
 	{
 		return false;
 		//throw new UnsupportedOperationException("Not supported yet.");
 	}
 
 	public long getInReplyToStatusId()
 	{
 		return 1234545323L;
 		//throw new UnsupportedOperationException("Not supported yet.");
 	}
 
 	public int getInReplyToUserId()
 	{
 		throw new UnsupportedOperationException("Not supported yet.");
 	}
 
 	public String getInReplyToScreenName()
 	{
 		return "Powder-Puff";
 		//throw new UnsupportedOperationException("Not supported yet.");
 	}
 
 	public GeoLocation getGeoLocation()
 	{
 		throw new UnsupportedOperationException("Not supported yet.");
 	}
 
 	public Place getPlace()
 	{
 		return new Place() {
 
 			public String getName()
 			{
 				return "River Head Dist. St. Ann, Jamaica W.I.";
 				//throw new UnsupportedOperationException("Not supported yet.");
 			}
 
 			public String getStreetAddress()
 			{
 				throw new UnsupportedOperationException("Not supported yet.");
 			}
 
 			public String getCountryCode()
 			{
 				throw new UnsupportedOperationException("Not supported yet.");
 			}
 
 			public String getId()
 			{
 				throw new UnsupportedOperationException("Not supported yet.");
 			}
 
 			public String getCountry()
 			{
 				throw new UnsupportedOperationException("Not supported yet.");
 			}
 
 			public String getPlaceType()
 			{
 				throw new UnsupportedOperationException("Not supported yet.");
 			}
 
 			public String getURL()
 			{
 				throw new UnsupportedOperationException("Not supported yet.");
 			}
 
 			public String getFullName()
 			{
 				throw new UnsupportedOperationException("Not supported yet.");
 			}
 
 			public String getBoundingBoxType()
 			{
 				throw new UnsupportedOperationException("Not supported yet.");
 			}
 
 			public GeoLocation[][] getBoundingBoxCoordinates()
 			{
 				throw new UnsupportedOperationException("Not supported yet.");
 			}
 
 			public String getGeometryType()
 			{
 				throw new UnsupportedOperationException("Not supported yet.");
 			}
 
 			public GeoLocation[][] getGeometryCoordinates()
 			{
 				throw new UnsupportedOperationException("Not supported yet.");
 			}
 
 			public Place[] getContainedWithIn()
 			{
 				throw new UnsupportedOperationException("Not supported yet.");
 			}
 
 			public int compareTo(Place o)
 			{
 				throw new UnsupportedOperationException("Not supported yet.");
 			}
 		};
 		//throw new UnsupportedOperationException("Not supported yet.");
 	}
 
 	public boolean isFavorited()
 	{
 		throw new UnsupportedOperationException("Not supported yet.");
 	}
 
 	public User getUser()
 	{
 		return new UserTest();
 		//throw new UnsupportedOperationException("Not supported yet.");
 	}
 
 	public boolean isRetweet()
 	{
 		return false;
 		//throw new UnsupportedOperationException("Not supported yet.");
 	}
 
 	public Status getRetweetedStatus()
 	{
 		throw new UnsupportedOperationException("Not supported yet.");
 	}
 
 	public String[] getContributors()
 	{
 		throw new UnsupportedOperationException("Not supported yet.");
 	}
 
 	public int compareTo(Status o)
 	{
 		throw new UnsupportedOperationException("Not supported yet.");
 	}
 
 	public RateLimitStatus getRateLimitStatus()
 	{
 		throw new UnsupportedOperationException("Not supported yet.");
 	}
 
 }
