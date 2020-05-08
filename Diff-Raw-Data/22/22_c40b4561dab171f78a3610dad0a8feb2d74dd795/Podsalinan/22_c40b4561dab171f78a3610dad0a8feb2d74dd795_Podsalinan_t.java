 /*******************************************************************************
  * Copyright (c) 2011 Sam Bell.
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published
  * by the Free Software Foundation, either version 3 of the License,
  * or  any later version.
  * 
  * This program is distributed in the hope that it will be useful, but
  * WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See
  * the GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  * 
  * Contributors:
  *     Sam Bell - initial API and implementation
  ******************************************************************************/
 /**
  *   podsalinan is a project written to 
  *   1, get me familiar with java again. 
  *   2, create a program that will download any url it is given, as well as podcasts from
  *   rss feeds that a user sources.
  *   Podsalinan - loosely translated pod copy
  *   
  *   Written By: Sam Bell
  *   
  */
 package podsalinan;
 
 import java.util.Vector;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 public class Podsalinan {
 	
 	private URLDownloadList urlDownloads;
 	private Vector<Podcast> podcasts;
 	private ProgSettings settings;
 	private CommandPass commands;
 	private DownloadQueue downloaderList;
 	private DataStorage dataFiles;
 	private CLInterface cli;
 	
 	/**
 	 * Upon execution the program will create a new instance of podsalinan, which is where
 	 * most of the work happens. Creating the new instance builds the main window.
 	 * 
 	 * @param args
 	 *   Nothing to pass in just yet
 	 */
 	public static void main(String[] args) {
 		Podsalinan mainProgram = new Podsalinan();
 		mainProgram.initialize();
 		mainProgram.backgroundProcess();
 	}
 
 	private void backgroundProcess() {
 		int updateInterval=0;
 		
 		// Find update Interval for podcasts in program settings.
 		try {
 			updateInterval=Integer.parseInt(settings.getSettingValue("updateInterval"));
 		} catch (NumberFormatException e){
 			updateInterval=0;
 		} catch (NullPointerException e){
 			updateInterval=0;
 		}
 		if (updateInterval<60){
 			settings.updateSetting("updateInterval", "60");
 			updateInterval=60;
 		}
 		
 		while(!cli.isFinished()){
 			// List the podcast titles.
 			for (Podcast podcast : podcasts){
				if ((!cli.isFinished())&&(!podcast.isRemoved())){
 					podcast.updateList(dataFiles.getSettingsDir());
 					dataFiles.savePodcast(podcast);
 				}
 			}
 			
 			try {
 				synchronized (cli.getWaitObject()){
 					if (!cli.isFinished())
 						// updateInterval will be a multiple of 1 minute
 						cli.getWaitObject().wait(updateInterval*60000);
 				}
 			} catch (InterruptedException e) {
 			}
 		}
 		dataFiles.saveSettings(podcasts, urlDownloads, settings);
 		System.out.println("Goodbye.");
 	}
 
 	public Podsalinan(){
 		podcasts = new Vector<Podcast>();
 		// URL Downloads
 		urlDownloads = new URLDownloadList(podcasts);
 		// Program Settings
 		settings = new ProgSettings();
 		
 		// Action Listener for the main window
 		//CommandPass aListener = new CommandPass(settings);
 
 		// disables sqlite4java's logging
 		Logger.getLogger("com.almworks.sqlite4java").setLevel(Level.OFF);
 	}
 	
 	public void initialize(){
 		dataFiles = new DataStorage();
 		
 		// load the program settings
 		dataFiles.loadSettings (podcasts, urlDownloads, settings);
 		// Load the podcast data
 		for (Podcast podcast : podcasts)
 			dataFiles.loadPodcast(podcast);
 
 		// Downloader List
 		downloaderList = new DownloadQueue(settings);
 		
 		cli = new CLInterface(podcasts, urlDownloads, settings);
 		Thread cliThread = new Thread(cli);
 		cliThread.start();
 	}
 }
