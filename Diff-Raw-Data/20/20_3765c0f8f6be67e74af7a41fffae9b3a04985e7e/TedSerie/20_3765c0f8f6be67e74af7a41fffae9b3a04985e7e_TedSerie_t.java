  package ted;
 
 import java.io.Serializable;
 import java.text.DateFormat;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.Vector;
 
 import org.w3c.dom.Element;
 
 import ted.SeasonEpisodeScheduler.NoEpisodeFoundException;
 import ted.datastructures.SeasonEpisode;
 import ted.datastructures.StandardStructure;
 import ted.infrastructure.ITedMain;
 import ted.ui.editshowdialog.FeedPopupItem;
 
 /**
  * TED: Torrent Episode Downloader (2005 - 2006)
  * 
  * This is the Serie class of Ted. This class stores a show and keeps
  * track of the current episode and season that ted found.
  * It also contains other data that ted uses to parse and check episodes.
  * 
  * @author Roel
  * @author Joost
  *
  * ted License:
  * This file is part of ted. ted and all of it's parts are licensed
  * under GNU General Public License (GPL) version 2.0
  * 
  * for more details see: http://en.wikipedia.org/wiki/GNU_General_Public_License
  *
  */
 public class TedSerie implements Serializable, Comparable<TedSerie>
 {
 	/****************************************************
 	 * GLOBAL VARIABLES
 	 ****************************************************/
 	
 	// NOTE::
 	// DO NOTE REMOVE FIELDS HERE!!
 	// that will break the backwards compatibility of this class
 	// Instead, move them to the deprecated fields section below..
 	
 	// Static fields
 	public final static int STATUS_HIATUS = 3;
 	public final static int STATUS_HOLD = 2;
 	public final static int STATUS_PAUSE = 1;
 	public final static int STATUS_CHECK = 0;
 	public final static int STATUS_DISABLED = 4;
 	public final static int IS_PARSING = 1;
 	public final static int IS_IDLE = 0;
 	static final long serialVersionUID= 7210007788942770687L;
 
 	// General
 	private String name;
 	private String url;
 	private String statusString;
 	private String searchName = "";
 	private boolean usePresets;
 	private int progress = 0;
 	private int timeZone = -1;
 	protected boolean isDaily = false;
 	protected StandardStructure currentEpisodeSS;	
 	// Feeds
 	private Vector<TedSerieFeed> feeds = new Vector<TedSerieFeed>();	
 	// Filters
 	private int minSize;
 	private int maxSize;
 	private int minNumOfSeeders;
 	private String keywords;
 	private boolean isDownloadInHD;	
 	// Schedule
 	private long checkDate;
 	private int activity = 0;
 	protected int status = 0;
 	private boolean useAutoSchedule;
 	private long breakUntil = 0;
 	private String tvRageID;
 	private String tvCom;
 	private String epguidesName;
 	protected SeasonEpisodeScheduler scheduler = null;
 	
 	// NOTE::
 	// DO NOTE REMOVE THESE FIELDS HERE!!
 	// that will break the backwards compatibility of this class
 	
 	// DEPRECATED FIELDS: only here for backwards compatibility.
 	private long breakFrom = 0;
 	private int breakEpisode;
 	private boolean useEpisodeSchedule;
 	private boolean useBreakSchedule;
 	private boolean useBreakScheduleEpisode;
 	private boolean useBreakScheduleFrom;
 	private int weeklyInterval;
 	private boolean [] days;
 	private boolean downloadAll;
 	private int lastWeekChecked;
 	private int lastDayChecked;
 	private int lastYearChecked;
 	// DEPRECATED: Use getCurrentEpisode() instead.
 	protected int currentEpisode;
 	// DEPRECATED: Use getCurrentSeason() instead.
 	protected int currentSeason;
 
 	/****************************************************
 	 * CONSTRUCTOR
 	 ****************************************************/
 	
 	/**
 	 * Creates empty TedSerie
 	 */
 	public TedSerie()
 	{
 		this.name = "";
 		this.url = "";
 		this.checkDate = 0;
 		this.minSize = 0;
 		this.maxSize = 0;
 		this.keywords = "";
 		this.status = TedSerie.STATUS_CHECK;
 		this.lastWeekChecked = -1;
 		this.lastDayChecked = 0;
 		this.lastYearChecked = 0;
 		this.downloadAll = false;
 		this.useAutoSchedule = true;
 		this.minNumOfSeeders = 0;
 		this.statusString = Lang.getString("TedSerie.Idle"); //$NON-NLS-1$
 		this.usePresets = true;
 		this.searchName = "";
 	}
 	
 	/**
 	 * Copies all fields of original into this TedSerie
 	 * @param original
 	 * @return
 	 */
 	public TedSerie(TedSerie original) 
 	{
 		this.copy(original);	
 	}
 	
 	/****************************************************
 	 * LOCAL METHODS
 	 ****************************************************/
 	/**
 	 * @return Initialized array for the checkdays
 	 * From sunday (=0) to saturday (= 6)
 	 */
 	private boolean[] initDays() 
 	{
 		boolean days [] = new boolean [7];
 		for (int i = 0; i < days.length; i++)
 		{
 			days[i] = false;
 		}
 		
 		return days;
 	}
 	
 	/****************************************************
 	 * PUBLIC METHODS
 	 ****************************************************/
 	
 	public void AutoFillInPresets(TedSerie XMLserie)
 	{
 		if (XMLserie != null)
 		{
 			this.setName(XMLserie.getName());
 			
 			Vector<TedSerieFeed> feeds = new Vector<TedSerieFeed>();
 			feeds = XMLserie.getFeeds();
 			feeds.addAll(this.getSelfmadeFeeds());
 			this.setFeeds(feeds);
 						
 			if(this.isUsePresets())
 			{
 				if(XMLserie.getMinSize()!=0)
 					this.setMinSize(XMLserie.getMinSize());
 				
 				if(XMLserie.getMaxSize()!=0)
 					this.setMaxSize(XMLserie.getMaxSize());
 				
 				if(XMLserie.getMinNumOfSeeders()!=0)
 					this.setMinNumOfSeeders(XMLserie.getMinNumOfSeeders());
 				
 				//add the new keywords if they didn't already existed
 				//leave user defined keywords alone
 				String userKeywords = this.getKeywords();
 				String xmlKeywords  = XMLserie.getKeywords();
 				if(userKeywords == null)
 				{
 					if(xmlKeywords != null)
 						this.setKeywords(xmlKeywords);
 				}
 				else if(xmlKeywords == null)
 				{
 					if(userKeywords != null)
 						this.setKeywords(userKeywords);
 				}
 				else if(!userKeywords.contains(xmlKeywords))
 				{
 					if(userKeywords.equals("")) //$NON-NLS-1$
 						this.setKeywords(xmlKeywords);
 					else if(xmlKeywords.equals("")) //$NON-NLS-1$
 						this.setKeywords(userKeywords);
 					else
 						this.setKeywords("(" + userKeywords + " | " + xmlKeywords + ")"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
 				}
 				//else the xmlKeywords are already defined in the userKeywords
 			}
 		}
 	}
 	
 	/**
 	 * This method reads shows.xml and generates feed urls from
 	 * the automatic feeds located in the xml file.
 	 */
 	public void generateFeedLocations() 
 	{
 		// read shows.xml
 		// get the details of the show
 		TedXMLParser parser = new TedXMLParser();
 		Element series = parser.readXMLFromFile(TedIO.XML_SHOWS_FILE); //$NON-NLS-1$
 		
 		// add auto-generated search based feeds to the show
 		Vector<FeedPopupItem> items = new Vector<FeedPopupItem>();
 		items = parser.getAutoFeedLocations(series);
 		
 		this.generateFeedLocations(items);
 	}
 	
 	/**
 	 * Generate feed locations from a vector of search based feeds
 	 * @param items
 	 */
 	public void generateFeedLocations(Vector<FeedPopupItem> items) 
 	{		
 		this.removeAllPredefinedFeeds();
 		
 		for(int i=0; i<items.size(); i++)
 		{
 			FeedPopupItem item = items.get(i);
 			this.autoGenerateAndAddFeedURL(item.getUrl());
 		}	
 	}
 	
 	/**
 	 * Removes all predefined feeds from this show
 	 */
 	public void removeAllPredefinedFeeds()
 	{
 		// backup the user defined feeds
 		Vector<TedSerieFeed> userFeeds = new Vector<TedSerieFeed>();
 		userFeeds.addAll(this.getSelfmadeFeeds());
 		
 		// clear all feeds
 		this.removeAllFeeds();
 		
 		this.setFeeds(userFeeds);
 	}
 	
 	/**
 	 * Removes all feeds from this show
 	 */
 	public void removeAllFeeds()
 	{
 		feeds.clear();
 	}
 
 	/**
 	 * Generate a feed url from a search based feed
 	 * @param url2 url of a search based feed
 	 */
 	public void autoGenerateAndAddFeedURL(String url2) 
 	{
 		if (url2.contains("#NAME#"))
 		{
 			url2 = url2.replace("#NAME#", this.getSearchName());
 			this.addPredefinedFeed(url2);
 		}
 	}
 
 	/**
 	 * Add a predefined feed to this TedSerie
 	 * @param url2 the Url of the feed
 	 */
 	private void addPredefinedFeed(String url2) 
 	{
 		TedSerieFeed newFeed = new TedSerieFeed(url2, false);
 		this.feeds.add(newFeed);	
 	}
 	
 	/****************************************************
 	 * GETTERS & SETTERS
 	 ****************************************************/
 
 	/**
 	 * @return The current episode of the show
 	 */
 	public int getCurrentEpisode() 
 	{
 		return ((SeasonEpisode)this.getCurrentStandardStructure()).getEpisode();
 	}
 	
 	/**
 	 * @return The current season of the show
 	 */
 	public int getCurrentSeason() 
 	{
 		return ((SeasonEpisode)this.getCurrentStandardStructure()).getSeason();
 	}
 	
 	/**
 	 * @return The name of the show
 	 */
 	public String getName() 
 	{
 		return name;
 	}
 	
 	/**
 	 * Set the name of the show
 	 * @param name
 	 */
 	public void setName(String name) 
 	{
 		this.name = name;
 	}
 	
 	/**
 	 * @return Vector with feeds for this show
 	 */
 	public Vector<TedSerieFeed> getFeeds()
 	{
 		if (feeds == null)
 		{
 			feeds = new Vector<TedSerieFeed>();
 			TedSerieFeed tsf = new TedSerieFeed(url, checkDate);
 			feeds.add(tsf);
 		}
 		return feeds;
 	}
 	
 	
 	/**
 	 * @return The minimum size for torrent contents
 	 */
 	public int getMinSize() 
 	{
 		return this.minSize;
 	}
 	
 	/**
 	 * Set the minimum size for the torrent contents
 	 * @param size
 	 */
 	public void setMinSize(int size) 
 	{
 		this.minSize = size;
 	}
 
 	/**
 	 * Set the maximum size for the torrent contents
 	 * @param size
 	 */
 	public void setMaxSize(int size) 
 	{
 		this.maxSize = size;
 	}
 	
 	/**
 	 * @return The max size for the torrent contents
 	 */
 	public int getMaxSize() 
 	{		
 		return this.maxSize;
 	}
 	
 	/**
 	 * Set the keywords for the torrent titles
 	 * @param s
 	 */
 	public void setKeywords(String s) 
 	{
 		this.keywords = s;
 	}
 	
 	/**
 	 * @return Keywords for the torrent titles
 	 */
 	public String getKeywords() 
 	{
 		return this.keywords;
 	}
 
 	/**
 	 * Set the status of the show
 	 * @param status The status to set.
 	 */
 	public boolean setStatus(int status) 
 	{
 		if ( ( 	status == TedSerie.STATUS_CHECK || 
 				status == TedSerie.STATUS_HOLD || 
 				status == TedSerie.STATUS_PAUSE ||
 				status == TedSerie.STATUS_HIATUS ||
 				status == TedSerie.STATUS_DISABLED
 				)
 				&& status != this.status
 			)
 		{			
 			// set status
 			this.status = status;				
 			this.resetStatus(true);
 			
 			return true;
 		}
 		return false;
 	}
 	
 	/**
 	 * @return If the show is on hold
 	 */
 	public boolean isHold()
 	{
 		return this.status == TedSerie.STATUS_HOLD;
 	}
 	/**
 	 * @return If the status of the show is CHECK
 	 */
 	public boolean isCheck()
 	{
 		return this.status == TedSerie.STATUS_CHECK;
 	}
 	
 	boolean isHiatus() 
 	{
 		return this.status == TedSerie.STATUS_HIATUS;
 	}
 	
 	
 	boolean isDisabled() 
 	{
 		return this.status == TedSerie.STATUS_DISABLED;
 	}
 
 	/**
 	 * Set the feeds for this serie
 	 * @param serieFeeds
 	 */
 	public void setFeeds(Vector<TedSerieFeed> serieFeeds)
 	{	
 		this.feeds = serieFeeds;
 	}
 	
 	public Vector<TedSerieFeed> getSelfmadeFeeds()
 	{
 		Vector<TedSerieFeed> tempFeeds = this.getFeeds(); 
 		Vector<TedSerieFeed> selfmadeFeeds = new Vector<TedSerieFeed>();
 		TedSerieFeed temp;
 		
 		for (int i = 0; i < tempFeeds.size(); i++)
 		{
 			temp = this.feeds.get(i);
 
 			if (temp.getSelfmade())
 			{
 				selfmadeFeeds.addElement(temp);
 			}
 		}
 		
 		return selfmadeFeeds;
 	}
 
 	/**
 	 * @return Returns the activity.
 	 */
 	public int getActivity()
 	{
 		return activity;
 	}
 
 	/**
 	 * @param activity The activity to set.
 	 */
 	public void setActivity(int activity)
 	{
 		this.activity = activity;
 	}
 
 	/**
 	 * @return Returns the minNumOfSeeders.
 	 */
 	public int getMinNumOfSeeders()
 	{
 		return minNumOfSeeders;
 	}
 
 	/**
 	 * @param minNumOfSeeders The minNumOfSeeders to set.
 	 */
 	public void setMinNumOfSeeders(int minNumOfSeeders)
 	{
 		this.minNumOfSeeders = minNumOfSeeders;
 	}
 
 	/**
 	 * @return Returns the statusString.
 	 */
 	public String getStatusString()
 	{
 		return statusString;
 	}
 
 	/**
 	 * @param statusString The statusString to set.
 	 * @param mainDialog 
 	 */
 	public void setStatusString(String statusString, ITedMain mainDialog)
 	{
 		this.statusString = statusString;
 		mainDialog.repaint();
 	}
 
 	/**
 	 * @return Returns the progress.
 	 */
 	public int getProgress()
 	{
 		return progress;
 	}
 
 	/**
 	 * @param progress The progress to set.
 	 * @param mainDialog 
 	 */
 	public void setProgress(int progress, ITedMain mainDialog)
 	{
 		this.progress = progress;
 		mainDialog.repaint();
 	}
 
 	/**
 	 * Reset progress, activity and statusstring to default values
 	 */
 	public void resetStatus(Boolean reset)
 	{
 		this.progress = 0;
 		this.activity = TedSerie.IS_IDLE;
 		if (reset)
 		{
 			this.statusString = this.makeDefaultStatusString();	
 		}
 	}
 
 	/**
 	 * @return Default status string according to status of show
 	 */
 	private String makeDefaultStatusString()
 	{		
 		if (this.isHold())
 		{
 			// if we use the breakschedule: return next date we put the show on check again			
 			DateFormat df = DateFormat.getDateInstance(DateFormat.LONG);
 			
 			// get day of the week
 			String day_of_week = null;
 			Calendar c = Calendar.getInstance();
 			c.setTimeInMillis(breakUntil);
 			switch(c.get(Calendar.DAY_OF_WEEK)){
 				case 1:
 					day_of_week = Lang.getString("TedSerie.Sunday");
 				break;
 				case 2:
 					day_of_week = Lang.getString("TedSerie.Monday");
 				break;
 				case 3:
 					day_of_week = Lang.getString("TedSerie.Tuesday");
 				break;
 				case 4:
 					day_of_week = Lang.getString("TedSerie.Wednesday");
 				break;
 				case 5:
 					day_of_week = Lang.getString("TedSerie.Thursday");
 				break;
 				case 6:
 					day_of_week = Lang.getString("TedSerie.Friday");
 				break;
 				case 7:
 					day_of_week = Lang.getString("TedSerie.Saturday");
 				break;
 			}
 			// compare the breakUntil date to the current date 
 			int diff = (int)((breakUntil - new Date().getTime())/86400000);
 			String untilValue = null;
 			switch(diff){
 				case -1:
 					untilValue = Lang.getString("TedSerie.Today");
 					break;
 				case 0:
 					untilValue = Lang.getString("TedSerie.Tomorow");
 					break;
 				case 1:
 					untilValue = Lang.getString("TedSerie.DayAfterTomorow");
 					break;
 				// if its within the next week just display the day of the week
 				case 2:
 				case 3:
 				case 4:
 				case 5:
 					untilValue = day_of_week;
 					break;
 				// if its more than a week away display the day and date
 				default:
 						untilValue = day_of_week + " " + df.format(breakUntil);
 					break;
 			}
 			return Lang.getString("TedSerie.StatusOnHoldUntill") + " " + untilValue; //$NON-NLS-1$	
 		}
 		else if (this.isHiatus())
 		{
 			return Lang.getString("TedSerie.Hiatus");
 		}
 		else if (this.isDisabled())
 		{
 			return Lang.getString("TedSerie.Disabled");
 		}
 		else
 		{
 			return Lang.getString("TedSerie.Idle"); //$NON-NLS-1$
 		}
 	}
 		
 	public void setUsePresets(boolean usePresets)
 	{
 		this.usePresets = usePresets;
 	}
 	
 	public boolean isUsePresets()
 	{
 		return usePresets;
 	}
 
 	public void setTVcom(String tempS)
 	{
 		this.tvCom = tempS;
 		
 	}
 	
 	public String getTVcom()
 	{
 		return this.tvCom;
 		
 	}
 	
 	public boolean isDaily()
 	{
 		return this.isDaily;
 	}
 
 	/**
 	 * @return A text that displays where this show is searching for
 	 */
 	public String getSearchForString() 
 	{	
 		return ((SeasonEpisode)this.getCurrentStandardStructure()).getSearchStringWithTitle();
 	}
 	
 	/**
 	 * @return the name of the show that can be used to search on torrent sites
 	 */
 	public String getSearchName()
 	{
 		if (!(searchName == null) && !searchName.equals(""))
 		{
 			return this.searchName;
 		}
 		else
 		{
 			// remove weird chars from name
 			String result = this.getName();
 			result = result.replaceAll("[/:&*?|\"\\\\]", " ");
 			// replacing one of these chars by a space could lead to multiple spaces
 			// so we replace 2 or more spaces with a single space
 			result = result.replaceAll(" {2,}", " ");
 			return result;
 		}
 	}
 	
 	public void setSearchName(String sName)
 	{
 		this.searchName = sName;
 	}
 	
 	/**
 	 * @return the name of the show that can be used to query epguides.com
 	 */
 	public String getEpguidesName()
 	{
 		if (!(epguidesName == null) && !epguidesName.equals(""))
 		{
 			return this.epguidesName;
 		}
 		else
 		{
 			// remove weird chars from name
 			String result = this.getName().toLowerCase();
 			result = result.replaceAll("[/:&*?|\"\\\\]", " ");
 			// remove all spaces
 			result = result.replaceAll(" ", "");
 			// remove any leading "the "
 			if(result.startsWith("the"))
 			{
 				result = result.substring(3);
 			}
 			return result;
 		}
 	}
 	
 
 	
 	/**
 	 * Will increment the season/episode of the show to the episode that is
 	 * scheduled after the params.
 	 * When the show is not on hiatus and no episode is found, the currentEpisode number
 	 * will be increased with 1.
 	 */
 	public void goToNextEpisode()
 	{
 		// If the current episode is a double episode you want to increase
 		// the episode number by 2 instead of 1.
 		boolean doubleEpisode = this.isDoubleEpisode(); // separate boolean for the catch block
 		SeasonEpisode searchFor = new SeasonEpisode((SeasonEpisode)this.getCurrentStandardStructure());
 		if (doubleEpisode)
 		{
 			searchFor.setEpisode(searchFor.getEpisode()+1);
 		}
 		
 		try
 		{
 			SeasonEpisode nextSE = (SeasonEpisode) this.getScheduler().getNextEpisode(searchFor);
 			this.setCurrentEpisode(nextSE);
 		}
 		catch (NoEpisodeFoundException e) 
 		{
 			// if no next SE is found, put ted on hiatus and increment episode with 1
 			if (this.status != TedSerie.STATUS_HIATUS)
 			{
 				SeasonEpisode nextSE = new SeasonEpisode(this.getCurrentSeason(), this.getCurrentEpisode() + 1);
 				this.setCurrentEpisode(nextSE);
 			}
 		}
 	}
 	
 	public boolean isDoubleEpisode()
 	{
 		return getCurrentStandardStructure().isDouble();
 	}
 
 	/**
 	 * Checks the status of the show with the current date
 	 */
 	public void updateShowStatus() 
 	{
 		this.getScheduler().updateShowStatus();	
 	}
 	
 	/**
 	 * @return The scheduler for this serie
 	 */
 	public SeasonEpisodeScheduler getScheduler()
 	{
 		// scheduler can be null if this serie is from an older ted version
 		if (this.scheduler == null)
 		{
 			this.scheduler = new SeasonEpisodeScheduler(this);
 			this.useAutoSchedule = true;
 		}
 		return this.scheduler;
 	}
 	
 	public StandardStructure getCurrentStandardStructure()
 	{
 		if (currentEpisodeSS == null)
 		{
 			// There was not yet an seasonepisode: probably old version of ted
 			// create one
 			SeasonEpisode episodeToFind = new SeasonEpisode(this.currentSeason, this.currentEpisode);
 			if (isEpisodeScheduleAvailable())
 			{
 				try 
 				{
 					currentEpisodeSS = this.getScheduler().getEpisode(episodeToFind);
 				} 
 				catch (NoEpisodeFoundException e) 
 				{
 					currentEpisodeSS = episodeToFind;
 				}
 			}
 			else
 			{
 				currentEpisodeSS = episodeToFind;
 			}
 		}
 		
 		return currentEpisodeSS;
 	}
 
 	/**
 	 * @return All aired and published episodes for this show
 	 */
 	public Vector<StandardStructure> getPubishedAndAiredEpisodes() 
 	{
 		return this.getScheduler().getPubishedAndAiredEpisodes();
 	}
 
 	public boolean isUseAutoSchedule() 
 	{
 		return useAutoSchedule;
 	}
 	
 	public boolean isSerieAndGlobalUseAutoSchedule()
 	{
 		return useAutoSchedule && TedConfig.getInstance().isUseAutoSchedule();
 	}
 
 	public void setUseAutoSchedule(boolean useAutoSchedule) 
 	{
 		this.useAutoSchedule = useAutoSchedule;	
 		this.updateShowStatus();
 	}
 
 	public boolean setEpguidesName(String text)
 	{
 		if (!text.equals(this.getEpguidesName()))
 		{
 			this.epguidesName = text;
 			
 			// Indicate that the epguides name has changed and that the schedule
 			// should be updated. Update is not done here as we also may have
 			// to do the update when the tvRage id name has changed. As the update
 			// is a time consuming operation you only want to do it once.
 			return true;
 		}	
 		
 		return false;
 	}
 
 	/**
 	 * @return The next to air episode. Either based on the schedule (if there is one) or generated
 	 * from known (published) episodes
 	 */
 	public StandardStructure getNextEpisode() 
 	{
 		return this.getScheduler().getNextToAirEpisode();
 	}
 	
 	/**
 	 * @return Whether episode schedule is available for this show. Note: this can
 	 * take a while since it can trigger a schedule update.
 	 */
 	public Boolean isEpisodeScheduleAvailableWithUpdate()
 	{
 		return this.getScheduler().isEpisodeScheduleAvailableWithUpdate(false);
 	}
 	
 	/**
 	 * @return Whether episode schedule is available for this show.
 	 */
 	public Boolean isEpisodeScheduleAvailable()
 	{
 		return this.getScheduler().isEpisodeScheduleAvailable();
 	}
 	
 	public void setTimeZone(int timeZone)
 	{
 		this.timeZone = timeZone;
 	}
 	
 	/**
 	 * @return The timezone of this serie
 	 */
 	public int getTimeZone()
 	{
 		return this.timeZone;
 	}
 
 	/**
 	 * Cache the values in the Serie so we don't need to update them from the schedule every time
 	 * Now they are pushed when the schedule is updated.
 	 * @param se
 	 */
 	public void setCurrentEpisode(StandardStructure se) 
 	{
 		try 
 		{
 			this.currentEpisodeSS = this.scheduler.getEpisode(se);
 		} 
 		catch (NoEpisodeFoundException e1) 
 		{
 			this.currentEpisodeSS = se;
 		}
 
 		this.updateShowStatus();
 	}
 
 	public void setStatusString(String string) 
 	{
 		this.statusString = string;		
 	}
 
 	/**
 	 * Checks the schedule whether the current season/episode are known.
 	 * If not, checks the schedule if there is another episode known after the previous episode.
 	 */
 	public void checkIfCurrentEpisodeIsScheduled() 
 	{
 		try 
 		{
 			scheduler.getEpisode(getCurrentStandardStructure());
 		} 
 		catch (NoEpisodeFoundException e) 
 		{
 			// no episode found in schedule, try to see if there is a next episode known
 			goToNextEpisode();
 		}		
 	}
 
 	public void setScheduler(SeasonEpisodeScheduler scheduler2) 
 	{
 		if (scheduler2 != null)
 		{
 			this.scheduler = new SeasonEpisodeScheduler(scheduler2, this);
 		}
 		else
 		{
 			this.scheduler = new SeasonEpisodeScheduler(this);
 		}
 	}
 
 	public StandardStructure getLastAiredEpisode() 
 	{
 		return this.getScheduler().getLastAiredEpisode();
 	}
 
 
 	public void clearScheduler() 
 	{
 		this.getScheduler().clear(this);
 	}
 
 	public int compareTo(TedSerie second) 
 	{
 		int result = 0;
 		
		String firstName  = removeTheFromName(this.getName().toLowerCase());
		String secondName = removeTheFromName(second.getName().toLowerCase()); 
		
 		// sort on name
 		if (TedConfig.getInstance().getSortType() == TedConfig.SORT_NAME)
 		{
			result = firstName.compareTo(secondName);
 		}
 		// sort on status and airdate
 		else if (TedConfig.getInstance().getSortType() == TedConfig.SORT_STATUS)
 		{
 			// sort on status
 			if (this.status != second.status)
 			{
 				if (this.status > second.status)
 				{
 					result = 1;
 				}
 				else 
 				{
 					result = -1;
 				}
 			}
 			else if (this.status == TedSerie.STATUS_HOLD && second.status == TedSerie.STATUS_HOLD)
 			{
 				// if status is hold for both shows, sort on airdate
 				StandardStructure thisSStructure = this.getCurrentStandardStructure();
 				StandardStructure secondSStructure = second.getCurrentStandardStructure();
 								
 				result = thisSStructure.compareDateTo(secondSStructure);			
 			}
 
 			// Until this point everything was the same. Sort on name.
 			if (result == 0)
 			{
				result = firstName.compareTo(secondName);
 			}
 		}
 				
 		return result;
 	}
	
	private String removeTheFromName(String name)
	{
		if (name.startsWith("the "))
		{
			name = name.substring(4);
		}
		
		return name; 
	}
 
 
 	public String getTVRageID() 
 	{	
 		if ((this.tvRageID == null) || this.tvRageID.equals(""))
 		{
 			// try to find TVRageID
 			this.tvRageID = this.scheduler.getTVRageID(this);
 		}
 		
 		return this.tvRageID;
 	}
 	
 	public boolean setTVRageID(String tvRageID) 
 	{
 		if (!tvRageID.equals(this.getTVRageID()))
 		{
 			this.tvRageID = tvRageID;
 
 			// Indicate that the tvRage id has changed and that the schedule
 			// should be updated. Update is not done here as we also may have
 			// to do the update when the epguides name has changed. As the update
 			// is a time consuming operation you only want to do it once.
 			return true;
 		}	
 		
 		return false;
 	}
 	
 	public Date getScheduleLastUpdateDate()
 	{
 		return this.getScheduler().getLastUpdateDate();
 	}
 	
 	public Date getScheduleNextUpdateDate()
 	{
 		return this.getScheduler().getNextUpdateDate();
 	}
 	
 	public void refreshSchedule()
 	{
 		this.getScheduler().forceScheduleUpdate();
 	}
 
 
 	public void toggleDisabled() 
 	{
 		if (this.isDisabled())
 		{
 			this.setStatus(STATUS_CHECK);
 			this.updateShowStatus();
 		}
 		else
 		{
 			this.setStatus(STATUS_DISABLED);
 		}
 		
 	}
 
 
 
 
 	public void setBreakUntil(long time) 
 	{
 		this.breakUntil = time;		
 	}
 
 	/**
 	 * Copies all fields of original into this TedSerie
 	 * @param original
 	 */
 	public void copy(TedSerie original) 
 	{
 		// Straight away copying
 		this.name = original.name;
 		this.checkDate = original.checkDate;
 		this.minSize = original.minSize;
 		this.maxSize = original.maxSize;
 		this.keywords = original.keywords;
 		this.status = original.status;
 		this.useAutoSchedule = original.useAutoSchedule;
 		this.minNumOfSeeders = original.minNumOfSeeders;
 		this.statusString = original.statusString; //$NON-NLS-1$
 		this.usePresets = original.usePresets;
 		this.searchName = original.searchName;
 		this.timeZone = original.timeZone;
 		this.epguidesName = original.epguidesName;
 		this.tvCom = original.tvCom;
 		this.tvRageID = original.tvRageID;
 		this.setScheduler(original.getScheduler());
 		this.isDownloadInHD = original.isDownloadInHD;
 		
 		this.feeds.clear();
 		// copy all feeds
 		Vector<TedSerieFeed> originalFeeds = original.getFeeds();
 		for (int i = 0; i < originalFeeds.size(); i++)
 		{
 			TedSerieFeed originalFeed = originalFeeds.get(i);
 			TedSerieFeed newFeed = new TedSerieFeed(originalFeed.getUrl(), originalFeed.getSelfmade());
 			this.feeds.add(newFeed);
 		}
 		
 		// Copy season/episode
 		if (!this.isDaily && !original.isDaily)
 		{
 			this.isDaily = false;
 			this.currentEpisodeSS = new SeasonEpisode((SeasonEpisode)original.getCurrentStandardStructure());
 		}
 	}
 
 	public void setDownloadInHD(boolean isDownloadInHD) 	{		this.isDownloadInHD = isDownloadInHD;	}	public boolean isDownloadInHD() 	{		return isDownloadInHD;	}	/**	 * Cancels the retrieval of schedule/torrent site info per episode	 */	public void interruptPubishedAndAiredEpisodes() 	{		this.scheduler.interruptPubishedAndAiredEpisodes();	}}
