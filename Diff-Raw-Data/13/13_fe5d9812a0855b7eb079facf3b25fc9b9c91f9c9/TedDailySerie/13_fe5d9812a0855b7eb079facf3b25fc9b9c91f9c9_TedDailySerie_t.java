 package ted;
 
 import java.util.Calendar;
 import java.util.GregorianCalendar;
 
 import ted.SeasonEpisodeScheduler.NoEpisodeFoundException;
 import ted.datastructures.DailyDate;
 import ted.datastructures.SeasonEpisode;
 import ted.datastructures.StandardStructure;
 import ted.datastructures.StandardStructure.AirDateUnknownException;
 
 public class TedDailySerie extends TedSerie
 {
 
 	/**
 	 * TED: Torrent Episode Downloader (2005 - 2007)
 	 * 
 	 * This is the about dialog of ted, it shows some trivial information
 	 * 
 	 * @author Joost
 	 * 
 	 * ted License:
 	 * This file is part of ted. ted and all of it's parts are licensed
 	 * under GNU General Public License (GPL) version 2.0
 	 * 
 	 * for more details see: http://en.wikipedia.org/wiki/GNU_General_Public_License
 	 *
 	 */
 	private static final long serialVersionUID = -4861614508271085128L;
 	private GregorianCalendar latestParseDate;
 	private GregorianCalendar latestDownloadDate;
 	private int maxDownloads;
 	
 	public TedDailySerie()
 	{
 		this.isDaily = true;
 		latestParseDate = new GregorianCalendar();
 		latestDownloadDate = new GregorianCalendar();
 		maxDownloads = 0;
 	}
 	
 	public TedDailySerie(TedDailySerie currentSerie) 
 	{
 		this.copy(currentSerie);
 	}
 
 	/**
 	 * 
 	 * @return the latest downloaded airdate for this show in milliseconds 
 	 */
 	public long getLatestDownloadDateInMillis()
 	{
 		return latestDownloadDate.getTimeInMillis();
 	}
 	
 	/**
 	 * 
 	 * @return the latest date this show has been parsed
 	 */
 	public long getLatestParseDateInMillis()
 	{
 		return latestParseDate.getTimeInMillis();
 	}
 	
 	/**
 	 * 
 	 * Set the latest airdate of the item which has been downloaded for this show
 	 * @param day day
 	 * @param month month, is 0-based
 	 * @param year year
 	 */
 	public void setLatestDownloadDate(int day, int month, int year)
 	{
 		GregorianCalendar d = new GregorianCalendar(year, month, day);
 		latestDownloadDate = d;
 	}
 	
 	/**
 	 * 
 	 * Set the latest airdate of the item which has been downloaded for this show
 	 * @param millis the date expressed in milliseconds
 	 */
 	public void setLatestDownloadDate(long millis)
 	{
 		GregorianCalendar d = new GregorianCalendar();
 		d.setTimeInMillis(millis);
 		latestDownloadDate = d;
 	}
 	
 	/**
 	 * Set the latest parse date of this show
 	 * @param day day
 	 * @param month month, 0-based
 	 * @param year year
 	 */
 	public void setLatestParseDate(int day, int month, int year)
 	{
 		GregorianCalendar d = new GregorianCalendar(year, month, day);
 		latestParseDate = d;
 	}
 	
 	/**
 	 * Returns -1 as this is a daily show
 	 */
 	public int getCurrentSeason()
 	{
 		return -1;
 	}
 	
 	/**
 	 * Returns -1 as this is a daily show
 	 */
 	public int getCurrentEpisode()
 	{
 		return -1;
 	}
 
 	/**
 	 * Returns an integer representing the number of entries ted should download at most
 	 * @return 0 represents "all", other integers are 'normal'
 	 */
 	public int getMaxDownloads() 
 	{
 		return maxDownloads;
 	}
 
 	/**
 	 * Sets an integer representing the number of entries ted should download at most
 	 * @param maxDownloads 0 0 represents "all", other integers are 'normal'
 	 */
 	public void setMaxDownloads(int maxDownloads) 
 	{
 		this.maxDownloads = maxDownloads;
 	}
 	
 	/* (non-Javadoc)
 	 * @see ted.TedSerie#getSearchForString()
 	 */
 	public String getSearchForString() 
 	{
 		DailyDate dd = new DailyDate(this.latestDownloadDate);
 		
 		String text;
 		if (this.getMaxDownloads() == 1)
 		{
 			text = "TedTableModel.LabelDailySingle";
 		}
 		
 		else
 		{
 			text = "TedTableModel.LabelDailyMultiple";
 		}
 		
 		return Lang.getString(text) +" " + dd.getFormattedEpisodeDate();
 	}
 	
 	public boolean isDoubleEpisode()
 	{
 		return isDoubleEpisode(this.latestDownloadDate);
 	}
 
 	private boolean isDoubleEpisode(GregorianCalendar date) 
 	{
 		DailyDate temp = new DailyDate(date);
 		DailyDate tempInSchedule;
 		try 
 		{
 			tempInSchedule = (DailyDate) this.getScheduler().getEpisode(temp);
 			return tempInSchedule.isDouble();
 		} 
 		catch (NoEpisodeFoundException e) 
 		{
 			return false;
 		}
 	}
 	
 	/**
 	 * Checks the schedule whether the current season/episode are known.
 	 * If not, checks the schedule if there is another episode known after the previous episode.
 	 */
 	public void checkIfCurrentEpisodeIsScheduled() 
 	{
 		DailyDate temp = new DailyDate(this.latestDownloadDate);
 		try 
 		{
 			scheduler.getEpisode(temp);
 		} 
 		catch (NoEpisodeFoundException e) 
 		{
 			// no episode found in schedule, try to see if there is a next episode known
 			goToNextEpisode(temp);		
 	    }		
 	}
 	
 	public void goToNextEpisode(DailyDate dDate)
 	{
 		// ask next episode to scheduler
 		try 
 		{
 			DailyDate nextDate = (DailyDate) this.getScheduler().getNextEpisode(dDate);
 			this.setCurrentEpisode(nextDate);
 			this.updateShowStatus();
 		} 
 		catch (NoEpisodeFoundException e) 
 		{
 			// next episode is not known in schedule, increase date by one day
 			if (this.status != TedSerie.STATUS_HIATUS)
 			{
 				Calendar oneDayAfter = Calendar.getInstance();
 				oneDayAfter.setTime(dDate.getDate().getTime());
 				oneDayAfter.add(Calendar.DAY_OF_YEAR, 1);
 				// make new daily date that has the next day as date
 				DailyDate nextDate = new DailyDate(oneDayAfter.getTime());
 				this.setCurrentEpisode(nextDate);
 				this.setStatus(TedSerie.STATUS_HIATUS);
 			}		
 		}	
 	}
 	
 	/**
 	 * Cache the values in the Serie so we don't need to update them from the schedule every time
 	 * Now they are pushed when the schedule is updated.
 	 * @param se
 	 */
 	public void setCurrentEpisode(StandardStructure dd) 
 	{
 		DailyDate episode;
 		try 
 		{
 			episode = (DailyDate) this.scheduler.getEpisode(dd);
 		} 
 		catch (NoEpisodeFoundException e1) 
 		{
 			episode = (DailyDate)dd;
 		}
 		this.currentEpisodeSS = episode;
 		setLatestDownloadDate(episode.getDate().getTimeInMillis());
		this.statusString = this.makeDefaultStatusString();
 	}
 	
 	public StandardStructure getCurrentStandardStructure()
 	{
 		StandardStructure temp = new DailyDate(this.latestDownloadDate);
 		if (this.currentEpisodeSS == null && this.isEpisodeScheduleAvailable())
 		{
 			try
 			{
 				this.currentEpisodeSS = scheduler.getEpisode(temp);
 			}
 			catch (NoEpisodeFoundException e) 
 			{
 				this.currentEpisodeSS  = temp;
 			}
 		}
 		else
 		{
 			this.currentEpisodeSS = temp;
 		}
 		
 		return this.currentEpisodeSS;
 	}
 	
 	public void copy (TedDailySerie original)
 	{
 		super.copy(original);
 		
 		this.isDaily = true;
 		this.maxDownloads = original.maxDownloads;
 		this.latestParseDate = original.latestParseDate;
 		this.latestDownloadDate = original.latestDownloadDate;
 		this.currentEpisodeSS = original.getCurrentStandardStructure();
 	}
 }
