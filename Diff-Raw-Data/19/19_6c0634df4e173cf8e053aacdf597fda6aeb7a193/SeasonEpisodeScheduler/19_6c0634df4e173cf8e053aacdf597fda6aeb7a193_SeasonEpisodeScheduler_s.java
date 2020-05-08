 package ted;
 
 import java.io.Serializable;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.GregorianCalendar;
 import java.util.TimeZone;
 import java.util.Vector;
 
 import ted.datastructures.SeasonEpisode;
 import ted.epguides.EpguidesPair;
 import ted.epguides.EpguidesParser;
 
 public class SeasonEpisodeScheduler implements Serializable
 {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = -2256145484789412126L;
 	private TedSerie serie;
 	private Vector<EpguidesPair> scheduledEpisodes;
 	private Date checkEpisodeSchedule;
 	
 	public SeasonEpisodeScheduler (TedSerie serie)
 	{
 		this.serie = serie;
 	}
 	
 	/**
 	 * @return A vector of episodes that are currently aired (from epguides info)
 	 * and the next episode
 	 */
 	private Vector<SeasonEpisode> getAiredEpisodes()
 	{	
 		Vector<SeasonEpisode> results = new Vector<SeasonEpisode>();
 		
 		if (this.updateEpisodeSchedule())
 		{
 			// system date
 	       	Date systemDate = new Date();
 			
 	       	SeasonEpisode current;
 			// return only seasonepisodes aired until today
 			for (int i = 0; i < this.scheduledEpisodes.size(); i++)
 			{
 				current = this.scheduledEpisodes.elementAt(i).getSeasonEpisode();
 				if (current.getAirDate().before(systemDate))
 				{
 					results.add(current);
 				}
 			}
 		}
 		
 		return results;
 	}
 	
 	/**
 	 * @return Vector with season/episode combinations taken from the torrent
 	 * feeds from this show
 	 */
 	private Vector<SeasonEpisode> getPublishedEpisodes()
 	{
 		TedParser showParser = new TedParser();
 		Vector<SeasonEpisode> publishedSeasonEpisodes = showParser.getItems(serie);
 		return publishedSeasonEpisodes;
 	}
 	
 	/**
 	 * Updates the episode schedule every 2 days.
 	 * @return Whether the schedule is filled (!= null and larger than 0 items)
 	 */
 	private boolean updateEpisodeSchedule()
 	{
 		// update interval
 		int updateIntervalInDays = 2;
 		// system date
        	Date systemDate = new Date();
 		// check date
 		if (this.scheduledEpisodes == null || this.checkEpisodeSchedule == null || systemDate.after(this.checkEpisodeSchedule))
 		{
 			
 			// parse epguides
 			// New instance of the parser
 	        EpguidesParser tedEP = new EpguidesParser();
 	        
 	        this.scheduledEpisodes = tedEP.getScheduledSeasonEpisodes(serie.getEpguidesName());
 	        
 	        // one week from now
 	        Calendar future = Calendar.getInstance();
 	        future.add(Calendar.DAY_OF_YEAR, updateIntervalInDays);
 	        this.checkEpisodeSchedule = future.getTime();
 	        
 	        return true;
 		}   
 		if (this.scheduledEpisodes != null && this.scheduledEpisodes.size() > 0)
 		{
 			return true;
 		}
 		
 		return false;
 	}
 	
 	/**
 	 * Searches the episode in the future episode schedule
 	 * @param season
 	 * @param episode
 	 * @return SeasonEpisode for parameters. null if episode is not planned in schedule
 	 */
 	private EpguidesPair getScheduledEpisode(int season, int episode)
 	{
 		EpguidesPair result = new EpguidesPair();
 		
 		// check schedule for updates
 		if (this.updateEpisodeSchedule())
 		{
 			SeasonEpisode current;
 			// search for season, episode in vector
 			for (int i = 0; i < this.scheduledEpisodes.size(); i++)
 			{
 				current = this.scheduledEpisodes.elementAt(i).getSeasonEpisode();
 				if (current.getSeason() == season && current.getEpisode() == episode)
 				{
 					result.setSeasonEpisode(current);
 					result.setIsDoubleEpisode(this.scheduledEpisodes.elementAt(i).getIsDoubleEpisode());
 					break;
 				}
 			}	
 		}
 		else
 		{
 			SeasonEpisode temp = new SeasonEpisode();
 			temp.setSeason(season);
 			temp.setEpisode(episode);
 			result.setSeasonEpisode(temp);
 		}
 		
 		return result;
 	}
 	
 	public EpguidesPair getEpisode (int season, int episode)
 	{
		EpguidesPair result = null;
		// check schedule for updates
 		
 		if (this.updateEpisodeSchedule())
 		{
			
 			EpguidesPair current;
 			// search for season, episode in vector
 			for (int i = 0; i < this.scheduledEpisodes.size(); i++)
 			{
 				current = this.scheduledEpisodes.elementAt(i);
 				if (current.getSeasonEpisode().getSeason() == season && current.getSeasonEpisode().getEpisode() == episode)
 				{
 					// check if there are more elements in the list
 					result = this.scheduledEpisodes.elementAt(i);
 					break;
 				}
 			}
 		}
 		else
 		{
 			// no schedule present, just fill a result
 			result = new EpguidesPair();
 			result.getSeasonEpisode().setSeason(season);
 			result.getSeasonEpisode().setEpisode(episode + 1);
 		}
 		return result;
 	}
 	
 	/**
 	 * @param season
 	 * @param episode
 	 * @return Episode scheduled after season, episode parameters
 	 * null if episode is not found
 	 */
 	public EpguidesPair getNextEpisode (int season, int episode)
 	{
 		return getEpisode(season, episode + 1);
 	}
 
 	/**
 	 * Cross-matches the published episodes (from the torrent websites) and the 
 	 * aired episodes (from the epguides schedule) to filter out fakes and display
 	 * the next aired episode that has not yet been published.
 	 * @return a vector with season/episodes that have been aired and published plus
 	 * one next episode that is scheduled to air
 	 */
 	Vector<SeasonEpisode> getPubishedAndAiredEpisodes() 
 	{
 		Vector<SeasonEpisode> publishedEpisodes = this.getPublishedEpisodes();
 		Vector<SeasonEpisode> airedEpisodes = this.getAiredEpisodes();
 		Vector<SeasonEpisode> results = new Vector<SeasonEpisode>();
 		
 		if (publishedEpisodes.size() > 0 && airedEpisodes.size() > 0)
 		{
 			// filter out any items in publishedEpisodes that are not in airedEpisodes
 			int airedCounter = 0;
 			int publishedCounter = 0;
 			// get first elements
 			SeasonEpisode publishedEpisode = publishedEpisodes.elementAt(publishedCounter);
 			SeasonEpisode airedEpisode = airedEpisodes.elementAt(airedCounter);		
 			airedCounter++;
 			publishedCounter++;
 			
 			// we're filling the list from most recent to old, so first add the next episode aired
 			EpguidesPair nextEpisode = this.getNextEpisode(airedEpisode.getSeason(), airedEpisode.getEpisode());
 			if (nextEpisode != null)
 			{
 				results.add(nextEpisode.getSeasonEpisode());
 			}
 			
 			while (airedCounter < airedEpisodes.size() && publishedCounter < publishedEpisodes.size())
 			{
 				// compare the two episodes.
 				
 				// if published > aired get next published
 				if (publishedEpisode.compareTo(airedEpisode) < 0 && publishedCounter < publishedEpisodes.size())
 				{
 					publishedEpisode = publishedEpisodes.elementAt(publishedCounter);
 					publishedCounter ++;					
 				}
 				// if published < aired get next aired
 				else if (publishedEpisode.compareTo(airedEpisode) > 0 && airedCounter < airedEpisodes.size())
 				{
 					airedEpisode = airedEpisodes.elementAt(airedCounter);
 					airedCounter ++;
 				}
 				// if published == aired, save episode into result vector and get next of both
 				else if (publishedEpisode.compareTo(airedEpisode) == 0)
 				{
 					publishedEpisode.setAirDate(airedEpisode.getAirDate());
 					publishedEpisode.setTitle(airedEpisode.getTitle());
 					publishedEpisode.setSummaryURL(airedEpisode.getSummaryURLString());
 					results.add(publishedEpisode);
 					
 					if (publishedCounter < publishedEpisodes.size())
 					{
 						publishedEpisode = publishedEpisodes.elementAt(publishedCounter);
 						publishedCounter ++;
 					}
 					if (airedCounter < airedEpisodes.size())
 					{
 						airedEpisode = airedEpisodes.elementAt(airedCounter);
 						airedCounter ++;
 					}
 				}
 			}
 			// make sure to check the last episodes, could be skipped by while loop
 			if (publishedEpisode.compareTo(airedEpisode) == 0)
 			{
 				publishedEpisode.setAirDate(airedEpisode.getAirDate());
 				publishedEpisode.setTitle(airedEpisode.getTitle());
 				publishedEpisode.setSummaryURL(airedEpisode.getSummaryURLString());
 				results.add(publishedEpisode);
 			}
 			
 		}
 		else
 		{
 			return publishedEpisodes;
 		}
 		
 		// free references for garbage collection
 		airedEpisodes = null;
 		publishedEpisodes = null;
 		
 		return results;
 	}
 
 
 	/**
 	 * Checks the airdate for the current season/episode of the show.
 	 * Puts the show on hold if the airdate is in the future
 	 * Puts the show on hiatus if a schedule is known but no airdate is found
 	 */
 	public void checkAirDate() 
 	{		
 		this.updateEpisodeSchedule();
 		// get airdate for current season / episode
 		EpguidesPair currentSE = this.getScheduledEpisode(serie.currentSeason, serie.currentEpisode);
		if (currentSE != null)
 		{
 			Date airDate = currentSE.getSeasonEpisode().getAirDate();
 			if (airDate != null)
 			{
 				Date currentDate = new Date();
 				
 				if (currentDate.before(airDate))
 				{
 					// put serie on hold if airdate is after today
 					serie.setBreakUntil(airDate.getTime());
 					serie.setUseBreakSchedule(true);
 					serie.setStatus(TedSerie.STATUS_HOLD);
 				}
 				else
 				{
 					// put show on check?
 					serie.setStatus(TedSerie.STATUS_CHECK);
 				}
 			}
 		}
 		else
 		{
 			serie.setStatus(TedSerie.STATUS_HIATUS);
 		}
 		// TODO: what if currentSE == null? then nothing is planned? just put on hold?
 		// Do nothing because no planning is found
 	}
 	
 	/**
 	 * Check if the serie has to get another status depending on the day it is and
 	 * the days the user selected to check the show again
 	 */
 	void checkDate() 
 	{
 		if (!serie.isDisabled())
 		{
 			Calendar date =  new GregorianCalendar();
 			boolean dayToCheck = false;
 			
 			// if the show is on hiatus, check if there is already some
 			// episode planning for the next episode available
 			if (serie.isHiatus())
 			{
 				serie.goToNextSeasonEpisode(serie.currentSeason, serie.currentEpisode);
 			}
 			
 			// check the airdate for the selected season/episode
 			if (serie.isUseAutoSchedule())
 			{
 				this.checkAirDate();
 			}
 			else
 			{
 				// use manual schedules
 				if (serie.isUseEpisodeSchedule())
 				{
 					if(!serie.isHold() && !serie.isHiatus())
 					{
 						// check if the current day is a selected air day
 						date.setFirstDayOfWeek(Calendar.SUNDAY);
 						int week = date.get(Calendar.WEEK_OF_YEAR);
 						int day = date.get(Calendar.DAY_OF_WEEK);
 						int year = date.get(Calendar.YEAR);
 		
 						// minus one day, sunday is 1 in java, 0 in our array
 						day --;
 						// get week and 2 weeks ago
 						date.add(Calendar.WEEK_OF_YEAR, -1);
 						int weekAgo = date.get(Calendar.WEEK_OF_YEAR);
 						date.add(Calendar.WEEK_OF_YEAR, -1);
 						int twoWeeksAgo = date.get(Calendar.WEEK_OF_YEAR);
 						int yearTwoWeeksAgo = date.get(Calendar.YEAR);
 						int lastWeekChecked = serie.getLastWeekChecked();
 						int lastDayChecked = serie.getLastDayChecked();
 						int lastYearChecked = serie.getLastYearChecked();
 						
 						if ((lastWeekChecked == week) && (serie.checkDay(lastDayChecked + 1, day)))
 						{
 							// if we last checked in the current week, and there is a day selected between the lastchecked day and today
 							dayToCheck = true;
 						}
 						else if((lastWeekChecked == weekAgo) && (serie.checkDay(0, day) || serie.checkDay(lastDayChecked + 1, 6)))
 						{
 							// if the last week we checked was last week, and there is a day checked in this week (from start to today) or in the last week (from last checked day to the end of the week)
 							dayToCheck = true;
 						}
 						else if ((lastWeekChecked <= twoWeeksAgo && lastYearChecked == yearTwoWeeksAgo) && (serie.checkDay(0, 6)))
 						{
 							// if we last checked 2 weeks ago and there is at least one day checked in the preferences
 							dayToCheck = true;
 						}
 						else if ((lastYearChecked < year) && (serie.checkDay(0, 6)))
 						{
 							// if we last checked last year and there is one day checked in the preferences
 							dayToCheck = true;
 						}
 						
 						if (dayToCheck)
 						{
 							// set status and date
 							serie.setStatus(TedSerie.STATUS_CHECK);
 							
 							serie.setLastDateChecked(day, week, year);
 						}
 					}	
 				}	
 				if(serie.isUseBreakSchedule())
 				{
 					// get date of today
 					date =  new GregorianCalendar();
 					
 					if (serie.isHold())
 					{
 						// check if its time to set the hold show on check again
 						if (serie.getBreakUntil() <= date.getTimeInMillis())
 						{
 							serie.setStatus(TedSerie.STATUS_CHECK);
 							serie.setUseBreakSchedule(false);
 							serie.setUseBreakScheduleFrom(false);
 							serie.setUseBreakScheduleEpisode(false);
 						}
 					}
 					else
 					{
 						if(serie.isUseBreakScheduleFrom() && (serie.getBreakFrom() < date.getTimeInMillis()))
 						{
 							serie.setStatus(TedSerie.STATUS_HOLD);
 						}
 					}
 				}
 			}
 		}	
 	}
 	
 	/**
 	 * Check what the status has to be after a certain episode has been found
 	 * @param episode
 	 */
 	public void updateStatus(int episode) 
 	{
 		// check airdate
 		this.checkAirDate();
 		
 		// if the episode was a break episode, put the show on hold
 		if (serie.checkBreakEpisode(episode) && !serie.isHiatus())
 		{
 			serie.setStatus(TedSerie.STATUS_HOLD);
 		}
 		// if we use the episode schedule, put the show on pause
 		else if (serie.isUseEpisodeSchedule() && serie.isCheck())
 		{
 			serie.setStatus(TedSerie.STATUS_PAUSE);
 		}		
 	}
 
 }
