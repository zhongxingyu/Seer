 package ted.datastructures;
 
 import java.io.Serializable;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.text.DateFormat;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.GregorianCalendar;
 
 import ted.Lang;
 
 public class StandardStructure implements Serializable, Comparable<StandardStructure>
 {
 	public class AirDateUnknownException extends Exception {
 
 		/**
 		 * 
 		 */
 		private static final long serialVersionUID = -3174854029126884504L;
 
 	}
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 6353477437638502291L;
 	
 	          int     quality      = 0;
 	protected Date    publishDate  = null; // date torrent was published online
 	protected Date    airDate      = null; // date episode was aired on tv
 	protected String  title        = "";   // episode title
 	protected String  summaryURL   = "";
 	protected boolean isDouble     = false;
 	
 	public boolean isDouble() 
 	{
 		return isDouble;
 	}
 	public void setDouble(boolean isDouble) 
 	{
 		this.isDouble = isDouble;
 	}
 	/**
 	 * @return Returns the quality.
 	 */
 	public int getQuality()
 	{
 		return quality;
 	}
 	/**
 	 * @param quality The quality to set.
 	 */
 	public void setQuality(int quality)
 	{
 		this.quality = quality;
 	}
 	
 	/**
 	 * @return Returns the publishDate.
 	 */
 	public Date getPublishDate()
 	{
 		return publishDate;
 	}
 	
 	/**
 	 * @return Returns the publishDate.
 	 */
 	public String getFormattedPublishDate()
 	{
 		String result;
 		if (this.publishDate != null)
 		{
 			DateFormat df = DateFormat.getDateInstance(DateFormat.LONG);
 			result = df.format(this.publishDate);
 		}
 		else
 		{
 			result = Lang.getString("TedAddShowDialog.EpisodesTable.Upcoming");
 		}
 		return result;
 	}
 	
 	/**
 	 * @param publishDate The publishDate to set.
 	 */
 	public void setPublishDate(long publishDate)
 	{
 		Calendar c = new GregorianCalendar();
 		c.setTimeInMillis(publishDate);
 		this.publishDate = c.getTime();
 	}
 	/**
 	 * @param publishDate The Pubish date
 	 */
 	public void setPublishDate(Date publishDate)
 	{
 		this.publishDate = publishDate;
 	}
 	
 	public void setSummaryURL(String url)
 	{
 		this.summaryURL = url;
 	}
 	
 	public URL getSummaryURL() throws MalformedURLException
 	{
 		return new URL(this.summaryURL);
 	}
 	public String getSummaryURLString()
 	{
 		return this.summaryURL;
 	}
 	
 	public Date getAirDate() throws AirDateUnknownException
 	{
 		if (this.airDate != null)
 		{
 			return this.airDate;
 		}
 		else
 		{
 			AirDateUnknownException e = new AirDateUnknownException();
 			throw e;
 		}
 	}
 	public void setAirDate(Date airDate) 
 	{
 		this.airDate = airDate;
 	}
 	public String getTitle() 
 	{
 		return title;
 	}
 	public void setTitle(String title) 
 	{
 		this.title = title;
 	}
 	/**
 	 * @return The formatted air date. When no airdate is known: formatted torrent publish date.
 	 */
 	public String getFormattedAirDate() 
 	{
 		String result;
 		if (this.airDate != null)
 		{
 			DateFormat df = DateFormat.getDateInstance(DateFormat.LONG);
 			result = df.format(this.airDate);
 		}
 		else
 		{
 			result = Lang.getString("StandardStructure.UnknownAirdate");
 		}
 		return result;
 	}
 	
 	public String getSearchString() 
 	{
 		return "";
 	}
 	
 	
 	/**
 	 * @return If this episode has been aired on or before today
 	 */
 	public boolean airedBeforeOrOnToday()
 	{
 		Date current = new Date();
 		
 		try 
 		{
 			return this.getAirDate().before(current);
 		} catch (AirDateUnknownException e) 
 		{
 			return false;
 		}
 	}
 	
 	public String getFormattedAirDateWithText()
 	{
 		String result = "";
 		
 		if (this.airDate != null)
 		{
 			if (this.airedBeforeOrOnToday())
 			{
 				result = Lang.getString("StandardStructure.AiredOn") + " " + this.getFormattedAirDate();
 			}
 			else
 			{
 				result =  Lang.getString("StandardStructure.WillAirOn") + " " + this.getFormattedAirDate();
 			}
 		}
 		else
 		{
 			result =  Lang.getString("StandardStructure.UnknownAirdate");
 		}
 		
 		return result;
 	}
 	
 	public int compareTo(StandardStructure arg0)
 	{
 		StandardStructure ss = arg0;
 		
 		long thisSeconds = 0;
 		long ssSeconds   = 0;
 		
 		if (this.airDate != null)
 		{
 			thisSeconds = this.airDate.getTime();
 		}
 		else
 		{
 			thisSeconds = this.publishDate.getTime();
 		}
 		
 		if (ss.airDate != null)
 		{
 			ssSeconds   = ss.airDate.getTime();;
 		}
 		else
 		{
 			ssSeconds   = ss.publishDate.getTime();
 		}
 		
 		if (thisSeconds < ssSeconds)
 		{
 			return -1;
 		}
 		else if (thisSeconds > ssSeconds)
 		{
 			return 1;
 		}
 		
 		return 0;
 	}
 		
	public StandardStructure nextEpisode() 
 	{
 		return new StandardStructure();
 	}
 	
 	public String getEpisodeChooserTitle()
 	{
 		return this.toString();
 	}
 	public String getSubscribtionOptionsTitle()
 	{
 		return this.getTitle();
 	}
 }
