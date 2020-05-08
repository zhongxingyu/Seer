 /*******************************************************************************
  * Copyright (c) 2013 Sam Bell.
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
  * 
  */
 package com.mimpidev.podsalinan.cli;
 
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import com.mimpidev.podsalinan.DataStorage;
 import com.mimpidev.podsalinan.Podsalinan;
 import com.mimpidev.podsalinan.cli.options.*;
 import com.mimpidev.podsalinan.data.PodcastList;
 import com.mimpidev.podsalinan.data.ProgSettings;
 import com.mimpidev.podsalinan.data.URLDownloadList;
 
 /**
  * @author bugman
  *
  */
 public class CLInterface extends CLIOption implements Runnable{
 	/**
 	 * 
 	 */
 	private CLInput input;
 	/**
 	 * 
 	 */
 	private String menuCommand="";
 
 	public CLInterface(DataStorage newData){
 		super(newData);
 		input = new CLInput();
 		initializeMenus();
 	}
 	
 	public CLInterface(PodcastList podcasts, URLDownloadList urlDownloads, ProgSettings settings){
 		super(new DataStorage());
 		data = new DataStorage();
 		data.setPodcasts(podcasts);
 		data.setUrlDownloads(urlDownloads);
 		data.setSettings(settings);
 		input = new CLInput();
 		initializeMenus();
 	}
 
 	private void initializeMenus() {
 		
 		options.put("quit", new QuitCommand(data));
 		options.put("exit", new QuitCommand(data));
 		options.put("http", new URLCommand(data));
 		options.put("ftp", new URLCommand(data));
 		options.put("help", new HelpCommand(data));
 		/** help commands
 		 *  =============
 		 *  help
 		 *  help list
 		 *  help select
 		 *  help set
 		 */
 		options.put("select", new SelectCommand(data));
 		/** select commands
 		 *  ===============
 		 *  select podcast
 		 *  select episode
 		 *  select download
 		 */
 		options.put("set", new SetCommand(data));
 		/** set commands
 		 *  ============
 		 *  set updateinterval
 		 *  set downloadlimit
 		 *  set maxdownloaders
 		 *  set autoqueue
 		 *  set menuvisible
 		 *  set defaultdirectory
 		 *  set destination
 		 *  set podcast directory
 		 *  set directory
 		 */
 		options.put("list", new ListCommand(data));
 		/** list commands
 		 *  =============
 		 *  list podcast
 		 *  list episode
 		 *  list select
 		 *  list details
 		 *  list downloads
 		 *  list preferences
 		 */
 		options.put("show", new ShowCommand(data));
 		/** show commands
 		 *  =============
 		 *  show menu
 		 */
 		options.put("hide", new HideCommand(data));
 		/** hide commands
 		 *  =============
 		 *  hide menu
 		 */
 		options.put("download", new DownloadCommand(data));
 		/** download commands
 		 *  =================
 		 *  download episode
 		 *  download <url>
 		 */
 		options.put("restart", new RestartCommand(data));
 		/** restart commands
 		 *  ================
 		 *  restart episode
 		 *  restart downloads
 		 */
 		options.put("stop", new StopCommand(data));
 		/** stop commands
 		 *  =============
 		 *  stop
 		 *  stop download
 		 *  stop <downloadId>
 		 */
 		options.put("remove", new RemoveCommand(data));
 		/** remove commands
 		 *  ===============
 		 *  remove
 		 *  remove download
 		 *  remove podcast
 		 *  remove <downloadId>
 		 */
 		options.put("clear", new ClearCommand(data));
 		/** clear commands
 		 *  ==============
 		 *  clear
 		 */
 		options.put("increase", new IncreaseCommand(data));
 		/** increase commands
 		 *  =================
 		 *  increase
 		 *  increase download <downloadId>
 		 */
 		options.put("decrease", new DecreaseCommand(data));
 		/** decrease commands
 		 *  =================
 		 *  decrease
 		 *  decrease download <downloadId>
 		 */
 		options.put("dump", new DumpCommand(data));
 		/** dump commands
 		 *  =============
 		 *  dump
 		 *  dump urldownloads
 		 */
 		options.put("podcast", new PodcastCommand(data));
 		/** podcast commands
 		 *  ==================
 		 *  <a-zzz>           - Select Podcast
 		 *  9                 - Quit to Main Menu
 		 */
 		options.put("downloads", new DownloadsCommand(data));
 		/** downloads commands
 		 *  ==================
 		 *  <a-zzz>           - Select Download
 		 *  9                 - Quit to Main Menu
 		 */
 		options.put("settings", new SettingsCommand(data));
 		/** settings commands
 		 *  =================
 		 */
 		options.put("", new MainMenuCommand(data));
 		/** mainmenu commands
 		 *  =================
 		 *  1                - Podcast Menu
 		 *  2                - Downloads Menu
 		 *  3                - Settings Menu
 		 *  4                - Quit
 		 */
 	}
 
 	/* TODO: Rewrite user input to allow command line completion. Current thoughts on how to
 	 * implement this:
 	 *       Pass the the command string, and the method into a method of CLInput.
 	 *       ie: userInput.addCommand("select podcast <userInput>",selectPodcastInput());
 	 *       
 	 * Then when user input is entered, and the user presses tab, it will either complete,
 	 *   or list options.
 	 */
 
 	public void userInput(){
 		System.out.print("->");
 		String menuInput=input.getStringInput();
 		String methodCall="";
 		if ((menuInput.length()>0)&&(menuInput!=null)){
 			methodCall=menuInput.split(" ",2)[0];
 			if (!options.containsKey(methodCall)){
                 if ((data.getSettings().findSetting("menuVisible")==null)||
 				    (data.getSettings().findSetting("menuVisible").equalsIgnoreCase("true"))){
                 	// The reason for the return call is so that we can check mainMenu to transform the call,
                 	// and then have the called method call another one if it needs to.
                 	ReturnCall returnValue = new ReturnCall();
                 	
                 	returnValue.execute=true;
                 	if (menuCommand.length()>0){
                 		returnValue.methodCall = menuCommand.split(" ")[0];
                 		returnValue.methodParameters = (menuCommand.substring(menuCommand.split(" ")[0].length())+" "+menuInput).trim();
                 	} else {
                 		returnValue.methodCall = menuCommand;
             		    returnValue.methodParameters = menuInput;
                 	}
            			if (debug) {
            				Podsalinan.debugLog.logInfo("Before the methodCall");
                			Podsalinan.debugLog.logInfo("methodCall: "+returnValue.methodCall);
                			Podsalinan.debugLog.logInfo("methodParameters: "+returnValue.methodParameters);
            			}
             		
            			returnValue=options.get(returnValue.methodCall.toLowerCase()).execute(returnValue.methodParameters);
            			if (debug){
            				Podsalinan.debugLog.logInfo("After the methodCall");
                			Podsalinan.debugLog.logInfo("methodCall: "+returnValue.methodCall);
                			Podsalinan.debugLog.logInfo("methodParameters: "+returnValue.methodParameters);
            			}
            			
                     if (returnValue.methodCall.length()>0){
                     	menuCommand = returnValue.methodCall;
                         if (returnValue.methodParameters.length()>0)
                         	menuCommand +=" "+returnValue.methodParameters;
                     } else
                     	menuCommand = "";
           			if (debug) Podsalinan.debugLog.logInfo("menuCommand: "+ menuCommand);
                 }
 			} else {
				options.get(methodCall).execute(menuInput);
 			}
 			
                 	/**
                 	 * How to traverse a menu system using commands???
                 	 * 
                 	 * Start with creating an entire tree of the command structure to visualize
                 	 * the different commands.
                 	 * Then we can devise a way of traversing the menu system
                 	 * 
                 	 * - mainMenu showMenu           menuCommand = ""
                 	 * user enters 1
                 	 *   - mainMenu 1                menuCommand = "podcast"
                 	 *   - podcast showMenu          menuCommand = "podcast"
                 	 * user enters a
                 	 *   - podcast a                 menuCommand = "podcast a"              
                 	 *   - podcast showSelectedMenu  menuCommand = "podcast a"
                 	 * user enters 1
                 	 *   - list episode              menuCommand = "podcast a" 
                 	 *   - podcast showSelectedMenu  menuCommand = "podcast a"
                 	 * user enters a
                 	 *   - select episode a          menuCommand = "podcast a episode a"
                 	 *   - episode showMenu          menuCommand = "podcast a episode a"
                 	 * user enters 1
                 	 *   - episode download          menuCommand = "podcast a episode a"
                 	 *   - episode showMenu          menuCommand = "podcast a episode a"
                 	 * user enters 9
                 	 *   - podcastShowSelectedMenu   menuCommand = "podcast a"
                 	 * user enters 9
                 	 *   - podcastShowMenu           menuCommand = "podcast"
                 	 * user enters 9
                 	 *   - mainMenu showMenu         menuCommand = ""
                 	 * 
                 	 * 
                 	 * MainMenu
                 	 * =========
                 	 *                        Functionality                    Command            
                 	 * <empty string>       - display Menu                     mainMenu showMenu
                 	 * 1                    - Podcast Menu                     podcast showMenu
                 	 *     <a-zz>           - Select Podcast                   (podcast <a-zzz>) or (select podcast <a-zzz>) podcast showSelectedMenu 
                 	 *            1         - List Episodes                    list episode
                 	 *            2         - Update List                      podcast update
                 	 *            3         - Delete Podcast                   podcast delete
                 	 *            4         - Change Download Directory        (podcast set directory) or (set podcast directory)
                 	 *            5         - Autoqueue Episodes               podcast autoqueue
                 	 *            <a-zz>    - Select Episode                   (select episode <a-zz>) or (podcast episode <a-zz>) episode showMenu
                 	 *                   1  - Download Episode                 (episode download) or (download episode)
                 	 *                   2  - Delete Episode from Drive        (episode delete) or (delete episode)
                 	 *                   3  - Cancel Download of Episode       (episode cancel) or (cancel episode)
                 	 *                   4  - Change Status of Episode         episode set status
                 	 *                   9  - Quit to Selected Podcast Menu    podcast showSelecetedMenu
                 	 *            9         - Quit to Podcast Menu             podcast showMenu
                 	 *     9                - Quit to Main Menu                mainMenu showMenu
                 	 * 2                    - Downloads Menu                   downloads showMenu
                 	 *     <a-zz>           - Select Download                  select download     downloads showSelectedMenu
                 	 *            1         - Delete Download                  (delete download) or (download delete)
                 	 *            2         - Restart download                 download restart
                 	 *            3         - Stop Download                    download stop
                 	 *            4         - Start Download                   download start
                 	 *            5         - Increase Priority                increase
                 	 *            6         - Decrease Priority                decrease
                 	 *            7         - Change Destination               download destination or set destination
                 	 *            9         - Quit to Downloads Menu           downloads showMenu
                 	 *     9                - Quit to Main Menu                mainMenu showMenu
                 	 * 3                    - Settings Menu                    settings showMenu
                 	 *     1                - Change Update Rate               settings updateinterval
                 	 *     2                - Change Number of Downloaders     settings maxdownloaders
                 	 *     3                - Default Download Directory       settings defaultdirectory
                 	 *     4                - AutoQueue All Podcast Episodes   settings autoqueue
                 	 *     5                - Set Download Speed Limit         settings downloadlimit
                 	 *     9                - Quit to Main Menu                mainMenu showMenu
                 	 * 4                    - Quit                             quit
                 	 * 
                 	 * mainMenu  - showMenu
                 	 * podcast   - showMenu
                 	 *           - showSelectedMenu
                 	 *           - <a-zz>    (load a podcast)
                 	 *           - update
                 	 *           - delete
                 	 *           - set directory
                 	 *           - autoqueue
                 	 *           - episode <a-zz>
                 	 *           - <a-zz> episode <a-zz>
                 	 * episode   - showMenu
                 	 *           - <a-zz>    (load an episode from the selected podcast)
                 	 *           - download
                 	 *           - delete
                 	 *           - cancel
                 	 *           - set status
                 	 * downloads - showMenu
                 	 *           - showSelectedMenu
                 	 * settings  - showMenu
                 	 *           - updateInterval
                 	 *           - maxdownloaders
                 	 *           - defaultdirectory
                 	 *           - autoqueue
                 	 *           - downloadlimit
                 	 * list      - episode
                 	 * delete    - episode
                 	 *           - download
                 	 *           - podcast
                 	 * cancel    - episode
                 	 * select    - podcast <a-zz>
                 	 *           - episode <a-zz>
                 	 *           - download <a-zz>
                 	 * set       - podcast directory
                 	 *           - status episode
                 	 *           - destination 
                 	 * download  - episode
                 	 *           - delete
                 	 *           - stop
                 	 *           - start
                 	 *           - increase
                 	 *           - decrease
                 	 *           - destination
                 	 * increase
                 	 * decrease           
                 	 */
                 	
 		}
 	}
 
 	@Override
 	public void run() {
 		System.out.println("Welcome to podsalinan.");
 		System.out.println("----------------------");
 		while (!data.getSettings().isFinished()){
 			if (((data.getSettings().findSetting("menuVisible")==null)||
 				 (data.getSettings().findSetting("menuVisible").equalsIgnoreCase("true")))){
 				String params;
 				if (menuCommand.split(" ").length>1)
 					params =  menuCommand.split(" ", 2)[1];
 				else
 					params = "";
 				if (debug) {
 					Podsalinan.debugLog.logInfo("["+getClass().getName()+"] menuCommand:"+menuCommand.length());
 					Podsalinan.debugLog.logInfo("["+getClass().getName()+"] menuCommand:'"+menuCommand+"'");
 				}
 				if (menuCommand.length()==0)
 					options.get("").execute(params);
 				else
 					options.get((menuCommand.split(" ")[0]).toLowerCase()).execute(params);
 			}
 			if (!data.getSettings().isFinished())
 				userInput();
 		}
 		System.out.println("Please Standby for system Shutdown.");
 		synchronized (data.getSettings().getWaitObject()){
 			data.getSettings().getWaitObject().notify();
 		}
 	}
 	
     /** This method is used to try to find a date from the user entry
      * @param menuInput
      * @return
      */
 	private Date convertDate(String menuInput) {
 		Date date=null;
 		String[] dateFormat = {"dd-MMM-yy",
 				               "dd-MM-yy",
 				               "dd-MMM-yyy",
 				               "dd-MM-yyy"};
 		int dateCounter=0;
 		
 		while ((date==null)&&(dateCounter<dateFormat.length)){
 			try {
 				date = new SimpleDateFormat(dateFormat[dateCounter]).parse(menuInput);
 			} catch (ParseException e) {
 				// date not found
 			}
 			dateCounter++;
 		}
 		
 		return date;
 	}
 /*
 	private void selectPodcast(Podcast podcast){
 		// Add information to menuList
 		menuList.clear();
 		menuList.add(new MenuPath("mainMenu", "podcast"));
 		menuList.add(new MenuPath("selectedPodcast", podcast.getDatafile()));
 			
 		// Set selected podcast
 		((CLPodcastSelectedMenu)(mainMenu.findSubmenu("podcast_selected"))).setSelectedPodcast(podcast);
 
 		if ((data.getSettings().isValidSetting("menuVisible"))&&
 			(data.getSettings().findSetting("menuVisible").equalsIgnoreCase("false")))
   		    System.out.println("Selected Podcast: "+podcast.getName());
 	}*/
 
 	/**
 	 * @return the data
 	 */
 	public DataStorage getData() {
 		return data;
 	}
 
 	/**
 	 * @param data the data to set
 	 */
 	public void setData(DataStorage data) {
 		this.data = data;
 	}
 	
 	@Override
 	public ReturnCall execute(String command) {
 		return null;
 	}
 	
 	public static class MenuPath {
 		public String name;
 		public String value;
 
 		public MenuPath(String newName, String newValue) {
 			name = newName;
 			newValue = value;
 		}
 	}
 }
