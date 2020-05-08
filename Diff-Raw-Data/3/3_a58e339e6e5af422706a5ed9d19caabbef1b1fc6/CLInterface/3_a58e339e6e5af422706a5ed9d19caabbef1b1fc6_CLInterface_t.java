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
 package podsalinan;
 
 import java.util.Vector;
 
 /**
  * @author bugman
  *
  */
 public class CLInterface implements Runnable{
 	private boolean finished=false;
 	private Vector<Podcast> podcasts;
 	private URLDownloadList urlDownloads;
 	private ProgSettings settings;
 	private Object waitObject = new Object();
 	private CLInput input;
 	private ProgSettings menuList;
 	private CLMainMenu mainMenu;
 
 	public CLInterface(Vector<Podcast> podcasts, URLDownloadList urlDownloads, ProgSettings settings){
 		this.podcasts=podcasts;
 		this.urlDownloads=urlDownloads;
 		this.settings=settings;
 		menuList = new ProgSettings();
 		input = new CLInput();
 		initializeMenus();
 	}
 
 	private void initializeMenus() {
 		// Main menu requires podcasts and urlDownloads so it can display the number of podcasts and downloads queued.
 		mainMenu = new CLMainMenu(menuList,podcasts,urlDownloads);
 		// When creating the Podcast Menus, we need settings to grab the default directory to do a manual update,
 		// and urlDownloads so we can queue episodes up for downloading manually.
 		mainMenu.addSubmenu(new CLPodcastMenu(menuList,podcasts,urlDownloads));
 		mainMenu.addSubmenu(new CLPreferencesMenu(menuList,settings,waitObject));
 		mainMenu.addSubmenu(new CLDownloadMenu(menuList,urlDownloads));
 	}
 
 
 	/** Brain Storming how to deal with user input, as it works on 2 different levels.
 	 *  
 	 *  Process
 	 *  ===================
 	 *  accept user_input
 	 *  if user_input is a number then
 	 *  	if ((menuList.size = 0) && (user_input==4))
 	 * 			quit
 	 * 		else
 	 * 			mainMenu.process(user_input);
 	 *  else
 	 *  	process user command
 	 */
 	
 	public void userInput(){
 		System.out.print("->");
 		String menuInput=input.getStringInput();
 		if (menuInput.length()>0){
 			try {
 				int inputInt = Integer.parseInt(menuInput);
 				// process number input
 				if ((menuList.size()==0)&&(inputInt==4))
 					finished=true;
 				else if ((settings.findSetting("menuVisible")==null)||
 						 (settings.findSetting("menuVisible").value.equalsIgnoreCase("true")))
 					mainMenu.process(inputInt);
 			} catch (NumberFormatException e){
 				// If the input is not a number This area will sort out that code
 				if ((menuInput.equalsIgnoreCase("quit"))||
 					(menuInput.equalsIgnoreCase("exit"))){
 					finished=true;
 				} else if ((menuInput.toUpperCase().startsWith("HTTP"))||
 						   (menuInput.toUpperCase().startsWith("FTP"))){
 					// User has entered a url to download.
 					urlDownloads.addDownload(menuInput,settings.getSettingValue("defaultDirectory"),"-1",false);
 				} else if (menuInput.toUpperCase().startsWith("HELP")){
 					helpList(menuInput);
 				} else if (menuInput.toUpperCase().startsWith("SELECT")){
 					cliSelection(menuInput);
 				} else if (menuInput.toUpperCase().startsWith("SET")){
 					setCommand(menuInput);
 				} else if (menuInput.toUpperCase().startsWith("LIST")){
 					listCommand(menuInput);
 				} else if (menuInput.toUpperCase().startsWith("SHOW")){
 					showCommand(menuInput);
 				} else if (menuInput.toUpperCase().startsWith("HIDE")){
 					hideCommand(menuInput);
 				} else if ((settings.findSetting("menuVisible")==null)||
 						   (settings.findSetting("menuVisible").value.equalsIgnoreCase("true")))
 					mainMenu.process(menuInput);
 			}
 		}
 	}
 	
 	private void hideCommand(String menuInput) {
 		if (menuInput.equalsIgnoreCase("hide menu"))
 			if (!settings.addSetting("menuVisible", "false"))
 				settings.updateSetting("menuVisible", "false");
 	}
 
 	private void showCommand(String menuInput) {
 		if (menuInput.equalsIgnoreCase("show menu"))
 			if (!settings.addSetting("menuVisible", "true"))
 				settings.updateSetting("menuVisible", "true");
 	}
 
 	@Override
 	public void run() {
 		System.out.println("Welcome to podsalinan.");
 		System.out.println("----------------------");
 		while (!finished){
 			if ((menuList.size()==0)&&
 				((settings.findSetting("menuVisible")==null)||
 				 (settings.findSetting("menuVisible").value.equalsIgnoreCase("true"))))
 				mainMenu.printMainMenu();
 			if (!finished)
 				userInput();
 		}
 		System.out.println("Please Standby for system Shutdown.");
 		synchronized (waitObject){
 			waitObject.notify();
 		}
 	}
 	
 	private void listCommand(String menuInput) {
 		menuInput = menuInput.replaceAll("(?i)list ", "");
 		if (menuInput.toLowerCase().startsWith("podcast")){
 			menuList.clear();
 			menuList.addSetting("mainMenu", "podcast");
 			((CLPodcastMenu)(mainMenu.findSubmenu("podcast"))).listPodcasts();
 		} else if (menuInput.toLowerCase().startsWith("episode")){
 			if ((menuList.isValidSetting("mainMenu"))&&
 				((menuList.findSetting("mainMenu").value.equalsIgnoreCase("podcast"))&&
 				 (menuList.findSetting("selectedPodcast")!=null))){
 				CLPodcastMenu podcastMenu = (CLPodcastMenu)(mainMenu.findSubmenu("podcast"));
 				((CLPodcastSelectedMenu)(podcastMenu.findSubmenu("podcast_selected"))).printEpisodeList();
 			} else {
 				System.out.println("Error: No podcast selected.");
 			}
 		}
 	}
 
 	private void setCommand(String menuInput) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	private void cliSelection(String menuInput) {
 		menuInput = menuInput.replaceAll("(?i)select ", "");
 		if (menuInput.toLowerCase().startsWith("podcast")){
 			// remove podcast text at the start (and the space)
 			menuInput= menuInput.replaceFirst(menuInput.split(" ")[0]+" ","");
 			boolean podcastFound=false;
             int podcastCount=0;
             Podcast podcast=null;
             
             // First while loop for exact matches
             while ((podcastCount<podcasts.size())&&(!podcastFound)){
             	podcast=podcasts.get(podcastCount);
 				if (((podcast.getName().equalsIgnoreCase(menuInput))||
 					(podcast.getDatafile().equalsIgnoreCase(menuInput)))&&
 					(!podcast.isRemoved())){
 					selectPodcast(podcast);
 					podcastFound=true;
 				}
             	podcastCount++;
             }
 			if (!podcastFound){
 				// If the user only entered part of the name we need to give suggestions to the user
 				Vector<Podcast> foundPodcasts = new Vector<Podcast>();
 				for (Podcast podcastSearch : podcasts)
 					if ((podcastSearch.getName().toLowerCase().contains(menuInput.toLowerCase()))&&
 						(!podcastSearch.isRemoved()))
 						foundPodcasts.add(podcastSearch);
 				if (foundPodcasts.size()==1){
 					// if only 1 matching podcast found
 					selectPodcast(foundPodcasts.firstElement());
 					podcastFound=true;
 				} else if (foundPodcasts.size()>1){
 		            podcastCount=1;
 					// If too many podcasts with text found
 					System.out.println ("Matches Found: "+foundPodcasts.size());
 					for (Podcast foundPodcast : foundPodcasts){
 						System.out.println(getEncodingFromNumber(podcastCount)+". "+foundPodcast.getName());
 					    podcastCount++;
 					}
 					// Ask user to select podcast
 					String userInput = input.getStringInput();
 					if ((userInput.length()>0)&&(userInput!=null)){
 						int selection = mainMenu.convertCharToNumber(userInput);
 						if ((selection>=0)&&(selection<foundPodcasts.size())){
 							selectPodcast(foundPodcasts.get(selection));
 							podcastFound=true;
 						} else
 							System.out.println("Error: Invalid user input");
 					} else 
 						System.out.println("Error: Invalid user input");
 				} else if (foundPodcasts.size()==0){
 					System.out.println("Error: No podcast found matching text("+menuInput+")");
 				}
 			}
 			if ((podcastFound)&&
 				(settings.isValidSetting("menuVisible"))&&
 				(settings.findSetting("menuVisible").value.equalsIgnoreCase("true"))){
 				// not going to go through the menuList array, because we had just set it in selectPodcast(Podcast)
 				CLPodcastMenu podcastMenu = (CLPodcastMenu)(mainMenu.findSubmenu("podcast"));
 				CLPodcastSelectedMenu podcastSelectedMenu = (CLPodcastSelectedMenu)(podcastMenu.findSubmenu("podcast_selected"));
 				podcastSelectedMenu.printMainMenu();
 			}
 		} else if (menuInput.toLowerCase().startsWith("episode")){
 			if ((menuList.isValidSetting("mainMenu"))&&
 				((menuList.findSetting("mainMenu").value.equalsIgnoreCase("podcast"))&&
 				 (menuList.findSetting("selectedPodcast")!=null))){
 				menuInput = menuInput.split(" ")[1];
				// Working here next. handling user input to select an episode. my thoughts are,
				// user input will either be the letter assigned in list episodes, or the date
 			}
 		} else if (menuInput.toLowerCase().startsWith("download")){
 			menuInput = menuInput.split(" ")[1];
 			System.out.println("Download: "+menuInput);
 		} else {
 			System.out.println("Error: Invalid User entry.");
 		}
 	}
 	
 	private void selectPodcast(Podcast podcast){
 		// Add information to menuList
 		menuList.clear();
 		menuList.addSetting("mainMenu", "podcast");
 		menuList.addSetting("selectedPodcast", podcast.getDatafile());
 			
 		// Set selected podcast
 		CLPodcastMenu podcastMenu = (CLPodcastMenu)(mainMenu.findSubmenu("podcast"));
 		((CLPodcastSelectedMenu)(podcastMenu.findSubmenu("podcast_selected"))).setSelectedPodcast(podcast);
 
 		if ((settings.isValidSetting("menuVisible"))&&
 			(settings.findSetting("menuVisible").value.equalsIgnoreCase("false")))
   		    System.out.println("Selected Podcast: "+podcast.getName());
 	}
 
 	private void helpList(String menuInput) {
 		System.out.println("");
 		if (menuInput.equalsIgnoreCase("help")){
 			// Main help text
 			System.out.println("A url is accepted at almost any time to add it to the download queue.");
 			System.out.println("It will have to start with (http/https/ftp):// for the system to download it.");
 			System.out.println("");
 			System.out.println("root commands");
 			System.out.println("");
 			System.out.println("   help <command>                 will show this screen");
 			System.out.println("   select                         used to select podcast/episode/queued download");
 			System.out.println("   set <preference name> <value>  used for changing system preferences");
 			System.out.println("   list                           used for listing podcasts/episodes/queued downloads/preferences");
 			System.out.println("   hide menu                      used to disable the menu");
 			System.out.println("   show menu                      used to enable the menu");
 			System.out.println("");
 			System.out.println("podcast specific commands");
 			System.out.println("   update                         used to force an update");
 			System.out.println("   download <episode number>      used to download an episode");
 			System.out.println("");
 			System.out.println("download specific commands");
 			System.out.println("   increase <download number>     used to move download up in the list");
 			System.out.println("   decrease <download number>     used to move download down in the list");
 			System.out.println("   remove <download number>       used to cancel download");
 			System.out.println("   restart <download number>      used to restart download");
 			System.out.println("   stop <download number>         used to stop download");
 			System.out.println("");
 			System.out.println("Commands to exit the program");
 			System.out.println("   quit");
 			System.out.println("   exit");
 		} else if (menuInput.toLowerCase().contains("select")){
 			// If user enters "help select"
 			System.out.println("select is used to traverse around the system, when not using the menu");
 			System.out.println("");
 			System.out.println("   select podcast <podcast name>       this will select the podcast");
 			System.out.println("            If the podcast name is not exact, the system will try to guess the podcast.");
 			System.out.println("   select podcast <podcast number>     this will select the podcast");
 			System.out.println("   select episode <episode number>     this will select the episode");
 			System.out.println("            If the podcast is not selected this will tell you to select a podcast first.");
 			System.out.println("   select download <download number>   this will select the download");
 		} else if (menuInput.toLowerCase().contains("list")){
 			// If user enters "help list"
 		    // Show sub commands for list command
 			System.out.println("list can have the following parameters");
 			System.out.println("");
 			System.out.println("   list podcast           show list of podcasts to the screen");
 			System.out.println("   list episodes          show list of episodes to the screen, if a podcast has been selected");
 			System.out.println("   list downloads         show list of queued downloads to the screen");
 			System.out.println("   list preferences       show list of preferences to the screen");
 		} else if (menuInput.toLowerCase().contains("set")){
 			// If the user enters "help set"
 			System.out.println("set is used to change settings");
 			System.out.println("");
 			System.out.println("   set <preference name> <value>  this will update the preference in the system");
 		}
 		System.out.println("");
 		System.out.println("");
 	}
 
 	public boolean isFinished(){
 		return finished;
 	}
 	
 	public void setFinished(boolean isFinished){
 		finished = isFinished;
 	}
 	
 	public String getCharForNumber(int i){
 		return i > 0 && i < 27 ? String.valueOf((char)(i + 64)) : null;
 	}
 	
 	public String getEncodingFromNumber(int number){
 		String charOutput="";
 		if (number<27)
 			charOutput = getCharForNumber(number);
 		else {
 			if (number%26!=0){
 				charOutput+=getCharForNumber(number/26);
 				charOutput+=getCharForNumber(number%26);
 			} else {
 				charOutput=getCharForNumber((number/26)-1)+"Z";
 			}
 		}
 		
 		return charOutput;
 	}
 
 	/**
 	 * @return the waitObject
 	 */
 	public Object getWaitObject() {
 		return waitObject;
 	}
 
 	/**
 	 * @param waitObject the waitObject to set
 	 */
 	public void setWaitObject(Object waitObject) {
 		this.waitObject = waitObject;
 	}
 }
