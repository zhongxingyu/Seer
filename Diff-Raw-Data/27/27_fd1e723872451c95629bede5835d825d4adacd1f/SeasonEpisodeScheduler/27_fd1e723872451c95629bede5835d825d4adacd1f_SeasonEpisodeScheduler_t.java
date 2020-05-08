 package ted;
 
 import java.io.Serializable;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.GregorianCalendar;
 import java.util.Vector;
 
 import ted.datastructures.DailyDate;
 import ted.datastructures.SeasonEpisode;
 import ted.datastructures.StandardStructure;
 import ted.datastructures.StandardStructure.AirDateUnknownException;
 import ted.epguides.ScheduleParser;
 
 public class SeasonEpisodeScheduler implements Serializable
 {
 
 	public class NoEpisodeFoundException extends Exception {
 
 		/**
 		 * 
 		 */
 		private static final long serialVersionUID = -3156780544495682045L;
 
 	}
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = -2256145484789412126L;
 	private TedSerie serie;
 	// Vector containing the scheduled episodes. Sorted on airdate. First item is with highest airdate
 	private Vector<StandardStructure> scheduledEpisodes;
 	private Date checkEpisodeSchedule;
 	// update interval
 	private int updateIntervalInDays = 2;
 	
 	public SeasonEpisodeScheduler (TedSerie serie)
 	{
 		this.serie = serie;
 	}
 	
 	/**
 	 * @return A vector of episodes that are currently aired (from epguides info)
 	 */
 	private Vector<StandardStructure> getAiredEpisodes()
 	{	
 		Vector<StandardStructure> results = new Vector<StandardStructure>();
 		
 		if (this.updateEpisodeSchedule())
 		{
 			// system date
 	       	Date systemDate = new Date();
 			
 	       	StandardStructure current;
 			// return only seasonepisodes aired until today
 			for (int i = 0; i < this.scheduledEpisodes.size(); i++)
 			{
 				current = this.scheduledEpisodes.elementAt(i);
 				try 
 				{
 					if (current.getAirDate().before(systemDate))
 					{
 						results.add(current);
 					}
 				} 
 				catch (AirDateUnknownException e) 
 				{
 					continue;
 				}
 			}
 		}
 		
 		return results;
 	}
 	
 	/**
 	 * @return The next episode scheduled to air. Will first search though all episodes
 	 * from epguides. If nothing is found, it will generate one based on the last published episode.
 	 * And if that also does not give a result (so no scheduled/published episodes) it will return a new episode
 	 * (season 1 episode 1 or daily based on todays date)
 	 */
 	public StandardStructure getNextToAirEpisode()
 	{
 		StandardStructure result = null;
 		
 		// Keep track of the latest episode in the schedule which has 
 		// an air date. With this episode the next episode in the list
 		// can be found easily without using the getPublishedAndAired 
 		// function.
 		StandardStructure latestInScheduleWithDate = null;
 		
 		// If the schedule is known (if so, update).
 		if (this.updateEpisodeSchedule())
 		{
 			// nothing found while searching through the episodes with airdates
 			// run through episodes
 			// get first episode after all aired episodes, with presumable no airdate
 			StandardStructure current;
 
 			// system date
 	       	Date systemDate = new Date();
 	       	// first find last aired episode in list
 			for (int i = 0 ; i < this.scheduledEpisodes.size(); i++)
 			{
 				current = this.scheduledEpisodes.elementAt(i);
 								
 				try 
 				{
 					// If there is no episode found with an air date set it.
 					// Do this only for the latest episode in the list (with a date).
 					if (   latestInScheduleWithDate == null 
 						&& current.getAirDate()     != null)
 					{
 						latestInScheduleWithDate = current;
 					}
 					
 					if (current.getAirDate().after(systemDate))
 					{
 						result = current;
 					}
 					else
 					{
 						break;
 					}
 				} 
 				catch (AirDateUnknownException e) 
 				{					
 					continue;
 				}
 			}			
 		}
 
 		// Nothing found based on schedule, find one after the latest
 		// one with an air date.
 		if (result == null && latestInScheduleWithDate != null)
 		{			
 			try 
 			{
				// Search for the next episode...
 				result = getNextEpisode(latestInScheduleWithDate);
 			} 
 			catch (NoEpisodeFoundException e1) 
 			{
				// ... if one isn't found just do a guess.
				result = latestInScheduleWithDate.guessNextEpisode();
 			}
 		}
 		
 		// nothing found, generate default 
 		if (result == null)
 		{
 			if (serie.isDaily)
 			{
 				Date systemDate = new Date();
 				result = new DailyDate(systemDate);
 			}
 			else
 			{
 				result = new SeasonEpisode(1, 1);
 			}
 		}
 		
 		return result;
 	}
 	
 	
 	/**
 	 * @return Vector with season/episode combinations taken from the torrent
 	 * feeds from this show
 	 */
 	private Vector<StandardStructure> getPublishedEpisodes()
 	{
 		TedParser showParser = new TedParser();
 		Vector<StandardStructure> publishedSeasonEpisodes = showParser.getItems(serie);
 		return publishedSeasonEpisodes;
 	}
 	
 	/**
 	 * Updates the episode schedule every 2 days.
 	 * @return Whether the schedule is filled (!= null and larger than 0 items)
 	 */
 	private boolean updateEpisodeSchedule()
 	{
 		// system date
        	Date systemDate = new Date();
        	
        	boolean result = true;
        	
 		// check date
 		if ( serie.isUseAutoSchedule() && 
 				( 	this.scheduledEpisodes == null || 
 					this.checkEpisodeSchedule == null || 
 					systemDate.after(this.checkEpisodeSchedule))
 				)
 		{	
 			serie.setStatusString("Refreshing episode schedule");
 			// parse epguides
 			// New instance of the parser
 			ScheduleParser tedEP = new ScheduleParser();
 	        
 	        this.scheduledEpisodes = tedEP.getScheduledSeasonEpisodes(serie);
 	        
 	        // one week from now
 	        Calendar future = Calendar.getInstance();
 	        future.add(Calendar.DAY_OF_YEAR, updateIntervalInDays);
 	        this.checkEpisodeSchedule = future.getTime();
 	        
 	        adjustAirDatesForTimeZone(scheduledEpisodes);
 	        
 	        serie.resetStatus(true);
 		}   
 		
 		if (this.scheduledEpisodes != null && this.scheduledEpisodes.size() > 0)
 		{
 			result = true;
 		}
 		else
 		{
 			result = false;
 		}
 		
 		return result;
 	}
 	
 	/**
 	 * Searches the episode in the future episode schedule
 	 * @param season
 	 * @param episode
 	 * @return SeasonEpisode for parameters. null if episode is not planned in schedule
 	 */
 	public StandardStructure getEpisode(StandardStructure episodeToFind) throws NoEpisodeFoundException
 	{
 		StandardStructure result = null;
 		
 		// check schedule for updates
 		if (this.updateEpisodeSchedule())
 		{
 			// search for season, episode in vector
 			for (int i = 0; i < this.scheduledEpisodes.size(); i++)
 			{
 				StandardStructure current = this.scheduledEpisodes.elementAt(i);
 				if (current.compareTo(episodeToFind) == 0)
 				{
 					result = current;
 					break;
 				}	
 			}
 		}
 		else
 		{
 			result = episodeToFind;
 		}
 		
 		if (result == null)
 		{
 			NoEpisodeFoundException e = new NoEpisodeFoundException();
 			throw e;
 		}
 		
 		return result;
 	}
 	
 	/**
 	 * @param season
 	 * @param episode
 	 * @return Episode scheduled after season, episode parameters
 	 * null if episode is not found
 	 */
 	public StandardStructure getNextEpisode (StandardStructure episodeToFind) throws NoEpisodeFoundException
 	{
 		StandardStructure result = null;
 			
 		// check schedule for updates
 		if (this.updateEpisodeSchedule())
 		{
 			StandardStructure tempResult = null;
 			// search for season, episode in vector
 			// find the next to air after episode to find.
 			for (int i = 0; i < this.scheduledEpisodes.size(); i++)
 			{
 				StandardStructure current = this.scheduledEpisodes.elementAt(i);
 				if (episodeToFind.compareTo(current) > 0)
 				{
 					tempResult = this.scheduledEpisodes.elementAt(i);
 				}
 				else if (episodeToFind.compareTo(current) <= 0)
 				{
 					// break early for performance reasons
 					break;
 				}
 			}	
 			result = tempResult;
 		}
 
 		if (result == null)
 		{
 			// throw exception and let client solve the problem when no future episodes are scheduled
 			NoEpisodeFoundException e = new NoEpisodeFoundException();
 			throw e;
 		}
 		return result;
 	}
 
 	/**
 	 * Cross-matches the published episodes (from the torrent websites) and the 
 	 * aired episodes (from the epguides schedule) to filter out fakes and display
 	 * the next aired episode that has not yet been published.
 	 * @return a vector with season/episodes that have been aired and published plus
 	 * one next episode that is scheduled to air
 	 */
 	Vector<StandardStructure> getPubishedAndAiredEpisodes(boolean noScheduledEpisodes) 
 	{
 		Vector<StandardStructure> publishedEpisodes = this.getPublishedEpisodes();
 		Vector<StandardStructure> airedEpisodes     = this.getAiredEpisodes();
 		Vector<StandardStructure> results           = new Vector<StandardStructure>();
 		
 		if (publishedEpisodes.size() > 0 && airedEpisodes.size() > 0)
 		{		
 			// filter out any items in publishedEpisodes that are not in airedEpisodes
 			int airedCounter = 0;
 			int publishedCounter = 0;
 			// get first elements
 			StandardStructure publishedEpisode = publishedEpisodes.elementAt(publishedCounter);
 			StandardStructure airedEpisode     = airedEpisodes.elementAt(airedCounter);		
 			airedCounter++;
 			publishedCounter++;
 			
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
 					try 
 					{
 						publishedEpisode.setAirDate(airedEpisode.getAirDate());
 					} 
 					catch (AirDateUnknownException e) 
 					{
 						// do nothing
 					}
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
 				try 
 				{
 					publishedEpisode.setAirDate(airedEpisode.getAirDate());
 				} 
 				catch (AirDateUnknownException e) 
 				{
 					// do nothing
 				}
 				publishedEpisode.setTitle(airedEpisode.getTitle());
 				publishedEpisode.setSummaryURL(airedEpisode.getSummaryURLString());
 				results.add(publishedEpisode);
 			}			
 		}
 		else
 		{
 			// if there are no scheduled episodes, return the published episodes as result
 			if (noScheduledEpisodes)
 			{
 				results = publishedEpisodes;
 			}
 		}
 		
 		// free references for garbage collection
 		airedEpisodes = null;
 		publishedEpisodes = null;
 		
 		return results;
 	}
 
 	// Adjust the air date of the episodes based on the time zone of 
 	// the user and the time zone in which the show was aired.
 	private void adjustAirDatesForTimeZone(Vector<StandardStructure> episodes)
 	{
 		for (int episode = 0; episode < episodes.size(); episode++)
 		{
 			StandardStructure ss = episodes.elementAt(episode);
 			
 			Date airdate;
 			try 
 			{
 				airdate = ss.getAirDate();
 			} 
 			catch (AirDateUnknownException e) 
 			{			
 				continue;
 			}
 			
 	        // If you're not living in the USA
 	 		if (TedConfig.getTimeZoneOffset() >= 0)
 	 		{
 	 			if (serie.getTimeZone() < 0)
 	 			{
 		 			// Add one day to the schedule
 		 			long time = airdate.getTime();
 		 			time += 86400000;
 		 			airdate.setTime(time);
 	 			}
 	 		}
  		}
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
 		StandardStructure currentSE;
 		try 
 		{
 			if (!serie.isDaily)
 			{
 				currentSE = this.getEpisode(new SeasonEpisode(serie.currentSeason, serie.currentEpisode));
 			}
 			else
 			{
 				TedDailySerie temp = ((TedDailySerie)serie);
 				Date currentDate = new Date (temp.getLatestDownloadDateInMillis());
 				currentSE = this.getEpisode(new DailyDate(currentDate));
 			}
 			Date airDate;
 			try 
 			{
 				airDate = currentSE.getAirDate();
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
 					// put show on check
 					serie.setStatus(TedSerie.STATUS_CHECK);
 				}
 			} 
 			catch (AirDateUnknownException e) 
 			{
 				if (this.updateEpisodeSchedule())
 				{
 					// if schedule is available, wait until airdate becomes known
 					serie.setStatus(TedSerie.STATUS_HIATUS);
 				}
 				else
 				{
 					// if schedule is not available, just put the show on check
 					serie.setStatus(TedSerie.STATUS_CHECK);
 				}		
 			}
 		} 
 		catch (NoEpisodeFoundException e1) 
 		{
 			serie.setStatus(TedSerie.STATUS_HIATUS);
 		}
 	}
 	
 	/**
 	 * Check if the serie has to get another status depending on the day it is and
 	 * the days the user selected to check the show again
 	 */
 	public void updateShowStatus() 
 	{
 		if (!serie.isDisabled())
 		{
 			Calendar date =  new GregorianCalendar();
 			boolean dayToCheck = false;
 			
 			// if the show is on hiatus, check if there is already some
 			// episode planning for the next episode available
 			if (serie.isHiatus())
 			{
 				// only do this when current season / episode is not known in schedule
 				// when the current episode is not known in the schedule,
 				// get the next episode from the planning			
 				serie.checkIfCurrentEpisodeIsScheduled();
 			}
 			
 			// check the airdate for the selected season/episode
 			if (serie.isUseAutoSchedule())
 			{
 				this.checkAirDate();
 			}
 			else
 			{
 				// if show is hiatus: put on check, cause with auto schedule enabled
 				// shows cannot be on hiatus anymore.
 				if (serie.isHiatus())
 				{
 					serie.setStatus(TedSerie.STATUS_CHECK);
 				}
 				
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
 
 	/**
 	 * Forces an update of the schedule. Needed when the epguides id is changed.
 	 */
 	public void forceScheduleUpdate() 
 	{
 		// set update date to today
 		this.checkEpisodeSchedule = new Date();
 		this.updateEpisodeSchedule();
 	}
 
 }
