 /**
  * 
  */
 package com.mimpidev.podsalinan.cli.options.podcast;
 
 import java.util.Vector;
 
 import com.mimpidev.podsalinan.DataStorage;
 import com.mimpidev.podsalinan.Podsalinan;
 import com.mimpidev.podsalinan.cli.CLIOption;
 import com.mimpidev.podsalinan.cli.CLInput;
 import com.mimpidev.podsalinan.cli.ReturnObject;
 import com.mimpidev.podsalinan.cli.options.episode.SelectEpisode;
 import com.mimpidev.podsalinan.cli.options.generic.ChangeDestination;
 import com.mimpidev.podsalinan.data.Podcast;
 
 /**
  * @author sbell
  *
  */
 public class SelectPodcast extends CLIOption {
 
 	public SelectPodcast(DataStorage newData) {
 		super(newData);
 		debug=true;
 		
 		ShowSelectedMenu showMenu = new ShowSelectedMenu(newData);
 		options.put("", showMenu);
 		options.put("1", new ListEpisodes(newData));
 		options.put("2", new UpdatePodcast(newData));
 		options.put("3", new DeletePodcast(newData));
 		options.put("4", new ChangeDestination(newData));
 		options.put("5", new AutoQueueEpisodes(newData));
 		options.put("showSelectedMenu", showMenu);
 		options.put("<aa>", new SelectEpisode(newData));
 	}
 
 	@Override
 	public ReturnObject execute(String command) {
 		debug=true;
 		if (debug) Podsalinan.debugLog.logInfo(this,"Line:41, Command :"+command);
 
         /* Only go through this code, if the podcast being passed in is different to the podcast stored in
 		 * global selection
 		 */
         /*TODO: Working here to correct menu traversal when the user wants to exit this submenu
          *      This is the current issue. Need to check if command ends in a 9 (If the podcast stored in globalSelection
          *      equals the podcast passed in, we will clear the globalSelection, and exit.
          */
 		if (!(globalSelection.containsKey("podcast") && 
               (command.split(" ",2)[0].equals(globalSelection.get("podcast"))))){
 			if (command.split(" ").length==1 && command.length()==1){
 				if (command.equals("9") && globalSelection.size()>0){
 					globalSelection.clear();
 					command="";
 				}
 			} else {
 				Podcast selectedPodcast = data.getPodcasts().getPodcastByUid(command.split(" ",2)[0]);
 				if (selectedPodcast==null){
 					Vector<Podcast> podcastList = data.getPodcasts().getPodcastListByName(command);
 					if (debug) Podsalinan.debugLog.logInfo(this, "Line:52, PodcastList.size="+podcastList.size());
 					if (podcastList.size()==1){
 						globalSelection.clear();
 						globalSelection.put("podcast",podcastList.get(0).getDatafile());
 						selectedPodcast=podcastList.get(0);
 					} else if (podcastList.size()>1){
 						int podcastCount=1;
 						// If too many podcasts with text found
 						System.out.println ("Matches Found: "+podcastList.size());
 						for (Podcast foundPodcast : podcastList){
 							System.out.println(getEncodingFromNumber(podcastCount)+". "+foundPodcast.getName());
 						    podcastCount++;
 						}
 						System.out.print("Please select a podcast: ");
 						// Ask user to select podcast
 						CLInput input = new CLInput();
 						String userInput = input.getStringInput();
 						if ((userInput.length()>0)&&(userInput!=null)){
 							int selection = convertCharToNumber(userInput);
 							if ((selection>=0)&&(selection<podcastList.size())){
 								selectedPodcast = podcastList.get(selection);
 							} else
 								System.out.println("Error: Invalid user input");
 						} else 
 							System.out.println("Error: Invalid user input");
 					} else {
 						System.out.println("Error: Podcast not found.");
 					}
 				}
 				if (selectedPodcast!=null){
 					if (debug) Podsalinan.debugLog.logInfo(this, 82, "Set selected podcast:"+selectedPodcast.getDatafile());
 					globalSelection.clear();
 					globalSelection.put("podcast",selectedPodcast.getDatafile());
 					command=selectedPodcast.getDatafile()+(command.split(" ",2).length>1?" "+command.split(" ",2)[1]:"");
 				}
 			}
 		}
 
 		if (debug) Podsalinan.debugLog.logInfo(this,91, "Command: "+command);
 		if (command.length()==8){
 			returnObject = options.get("").execute(command);
 			if (debug) Podsalinan.debugLog.logInfo(this,94,"Command Length:"+command.length());
 		}else if (command.split(" ").length>1){
 			if (debug) Podsalinan.debugLog.logInfo(this,96,"Command Length:"+command.length());
 			if (command.split(" ")[1].equals("9")){
				globalSelection.clear();
 				returnObject.methodCall="podcast";
 				returnObject.methodParameters="";
				returnObject.execute=true;
 			} else {
 				if (debug) Podsalinan.debugLog.logInfo(this,101, "Command: "+command);
 				if (debug) Podsalinan.debugLog.logInfo(this,102, "Podcast: "+command.split(" ")[0]);
 				if (convertCharToNumber(command.split(" ")[1])>=0){
 					returnObject = options.get("<aa>").execute(command);
 				} else {
 					returnObject = options.get(command.split(" ")[1]).execute(command);
 				}
 			}
 		} else {
 			returnObject = options.get("").execute(command);
 		}
 		
 		return returnObject;
 	}
 
 }
