 package ted.ui.addshowdialog;
 
 import java.util.Vector;
 
 import ted.TedParser;
 import ted.TedSerie;
 import ted.datastructures.DailyDate;
 import ted.datastructures.SeasonEpisode;
 import ted.datastructures.StandardStructure;
 import ted.datastructures.StandardStructure.AirDateUnknownException;
 
 /**
  * TED: Torrent Episode Downloader (2005 - 2006)
  * 
  * This is the counter that counts down to the next parse round of ted
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
 public class EpisodeParserThread extends Thread 
 {
 	/****************************************************
 	 * GLOBAL VARIABLES
 	 ****************************************************/
 	private EpisodeChooserPanel episodeChooserPanel;
 	private TedSerie selectedSerie;
 	private SubscribeOptionsPanel subscribeOptionsPanel = null;
 	
 	/****************************************************
 	 * CONSTRUCTOR
 	 ****************************************************/
 	/**
 	 * Create a new counter
 	 * @param subscribeOptionsPanel 
 	 * @param m the TedMainDialog
 	 * @param tc the current config
 	 */
 	public EpisodeParserThread(EpisodeChooserPanel ecp, TedSerie ts, SubscribeOptionsPanel subscribeOptionsPanel)
 	{
 		this.episodeChooserPanel = ecp;
 		this.selectedSerie = ts;
 		this.subscribeOptionsPanel = subscribeOptionsPanel;
 	}
 	
 	public EpisodeParserThread(EpisodeChooserPanel episodeChooserPanel2,
 			TedSerie show)
 	{
 		this.episodeChooserPanel = episodeChooserPanel2;
 		this.selectedSerie = show;
 	}
 
 	/****************************************************
 	 * PUBLIC METHODS
 	 ****************************************************/
 	
 	public void run()
 	{
 		
 		this.episodeChooserPanel.clear();
 		if (this.subscribeOptionsPanel != null)
 		{
 			this.subscribeOptionsPanel.clear();
 			this.subscribeOptionsPanel.setActivityStatus(true);
 		}
 		else
 		{
 			this.episodeChooserPanel.setActivityStatus(true);
 		}
 		if(selectedSerie!=null)
 		{		
 			// add vector to chooser panel
 			StandardStructure nextEpisode = selectedSerie.getNextEpisode();
 			
 			// check if next episode has airdate. otherwise no scheduled episode was found
 			// and the scheduler can use the published episodes instead, when no aired episodes
 			// are listed in the schedule
 			boolean noScheduledEpisodes = true;
 			try 
 			{
 				nextEpisode.getAirDate();
 				noScheduledEpisodes = false;
 			} 
 			catch (AirDateUnknownException e) 
 			{
 				noScheduledEpisodes = true;
 			}
 			
 			if (this.subscribeOptionsPanel != null)
 			{				
 				this.subscribeOptionsPanel.setNextEpisode(nextEpisode);
 				this.subscribeOptionsPanel.setLastAiredEpisode(selectedSerie.getScheduler().getLastAiredEpisode());
 				this.subscribeOptionsPanel.setActivityStatus(false);
 			}
			else
			{
				this.episodeChooserPanel.setActivityStatus(false);
			}
 			
 			// Do this at the end. This way we save some time loading the
 			// episodes in the custom episode list.
 			Vector<StandardStructure> publishedEpisodes = selectedSerie.getPubishedAndAiredEpisodes(noScheduledEpisodes);	
 			
 			this.episodeChooserPanel.setSeasonEpisodes(publishedEpisodes);
 			this.episodeChooserPanel.setNextEpisode(nextEpisode);
			this.subscribeOptionsPanel.enableCustomEpisodes();
 			
 			// free vector for garbage collection
 			publishedEpisodes = null;
 		}
 		
 		// disable activity image	
 		this.episodeChooserPanel.selectEpisode();
 	}
 }
