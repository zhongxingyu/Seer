 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package twitz.testing;
 
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.Date;
 import org.apache.log4j.Logger;
 import twitter4j.RateLimitStatus;
 import twitter4j.Status;
 import twitter4j.User;
 
 /**
  *
  * @author mistik1
  */
 public class UserTest implements User{
 
 	private String screenName = "Twitz_ras";
 	private Logger logger = Logger.getLogger(this.getClass().getName());
	private Date born = new Date();
 
 	public UserTest() {
 		this("Twitz_ras");
 	}
 
 	public UserTest(String screenName) {
 		this.screenName = screenName;
 	}
 	
 	public int getId()
 	{
 		return 123454;
 		//throw new UnsupportedOperationException("Not supported yet.");
 	}
 
 	public String getName()
 	{
 		return "Andrew Williams";
 		//throw new UnsupportedOperationException("Not supported yet.");
 	}
 
 	public String getScreenName()
 	{
 		return screenName;
 		//throw new UnsupportedOperationException("Not supported yet.");
 	}
 
 	public String getLocation()
 	{
 		return "River Head Dist. St. Ann, Jamaica W.I.";
 		//throw new UnsupportedOperationException("Not supported yet.");
 	}
 
 	public String getDescription()
 	{
 		throw new UnsupportedOperationException("Not supported yet.");
 	}
 
 	public boolean isContributorsEnabled()
 	{
 		throw new UnsupportedOperationException("Not supported yet.");
 	}
 
 	public URL getProfileImageURL()
 	{
 		URL img = null;
 		try
 		{
 			img = new URL("http://localhost/~mistik1/me_3.jpg");
 			//throw new UnsupportedOperationException("Not supported yet.");
 		}
 		catch (MalformedURLException ex)
 		{
 			logger.error(ex);
 		}
 		return img;
 		//throw new UnsupportedOperationException("Not supported yet.");
 	}
 
 	public URL getURL()
 	{
 		throw new UnsupportedOperationException("Not supported yet.");
 	}
 
 	public boolean isProtected()
 	{
 		throw new UnsupportedOperationException("Not supported yet.");
 	}
 
 	public int getFollowersCount()
 	{
 		throw new UnsupportedOperationException("Not supported yet.");
 	}
 
 	public Status getStatus()
 	{
 		return new StatusTest();
 		//throw new UnsupportedOperationException("Not supported yet.");
 	}
 
 	public Date getStatusCreatedAt()
 	{
 		throw new UnsupportedOperationException("Not supported yet.");
 	}
 
 	public long getStatusId()
 	{
 		throw new UnsupportedOperationException("Not supported yet.");
 	}
 
 	public String getStatusText()
 	{
 		throw new UnsupportedOperationException("Not supported yet.");
 	}
 
 	public String getStatusSource()
 	{
 		throw new UnsupportedOperationException("Not supported yet.");
 	}
 
 	public boolean isStatusTruncated()
 	{
 		throw new UnsupportedOperationException("Not supported yet.");
 	}
 
 	public long getStatusInReplyToStatusId()
 	{
 		throw new UnsupportedOperationException("Not supported yet.");
 	}
 
 	public int getStatusInReplyToUserId()
 	{
 		throw new UnsupportedOperationException("Not supported yet.");
 	}
 
 	public boolean isStatusFavorited()
 	{
 		throw new UnsupportedOperationException("Not supported yet.");
 	}
 
 	public String getStatusInReplyToScreenName()
 	{
 		throw new UnsupportedOperationException("Not supported yet.");
 	}
 
 	public String getProfileBackgroundColor()
 	{
 		throw new UnsupportedOperationException("Not supported yet.");
 	}
 
 	public String getProfileTextColor()
 	{
 		throw new UnsupportedOperationException("Not supported yet.");
 	}
 
 	public String getProfileLinkColor()
 	{
 		throw new UnsupportedOperationException("Not supported yet.");
 	}
 
 	public String getProfileSidebarFillColor()
 	{
 		throw new UnsupportedOperationException("Not supported yet.");
 	}
 
 	public String getProfileSidebarBorderColor()
 	{
 		throw new UnsupportedOperationException("Not supported yet.");
 	}
 
 	public int getFriendsCount()
 	{
 		throw new UnsupportedOperationException("Not supported yet.");
 	}
 
 	public Date getCreatedAt()
 	{
		return born;
 		//throw new UnsupportedOperationException("Not supported yet.");
 	}
 
 	public int getFavouritesCount()
 	{
 		throw new UnsupportedOperationException("Not supported yet.");
 	}
 
 	public int getUtcOffset()
 	{
 		throw new UnsupportedOperationException("Not supported yet.");
 	}
 
 	public String getTimeZone()
 	{
 		throw new UnsupportedOperationException("Not supported yet.");
 	}
 
 	public String getProfileBackgroundImageUrl()
 	{
 		throw new UnsupportedOperationException("Not supported yet.");
 	}
 
 	public boolean isProfileBackgroundTiled()
 	{
 		throw new UnsupportedOperationException("Not supported yet.");
 	}
 
 	public String getLang()
 	{
 		throw new UnsupportedOperationException("Not supported yet.");
 	}
 
 	public int getStatusesCount()
 	{
 		throw new UnsupportedOperationException("Not supported yet.");
 	}
 
 	public boolean isGeoEnabled()
 	{
 		throw new UnsupportedOperationException("Not supported yet.");
 	}
 
 	public boolean isVerified()
 	{
 		throw new UnsupportedOperationException("Not supported yet.");
 	}
 
 	public int compareTo(User o)
 	{
 		throw new UnsupportedOperationException("Not supported yet.");
 	}
 
 	public RateLimitStatus getRateLimitStatus()
 	{
 		throw new UnsupportedOperationException("Not supported yet.");
 	}
 
 }
