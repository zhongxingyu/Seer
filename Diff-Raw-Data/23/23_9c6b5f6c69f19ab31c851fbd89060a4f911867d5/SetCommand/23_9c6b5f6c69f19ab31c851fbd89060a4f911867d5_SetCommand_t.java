 /**
  * 
  */
 package com.mimpidev.podsalinan.cli.options;
 
 import com.mimpidev.podsalinan.DataStorage;
 import com.mimpidev.podsalinan.Podsalinan;
 import com.mimpidev.podsalinan.cli.CLIOption;
 import com.mimpidev.podsalinan.cli.ReturnObject;
 import com.mimpidev.podsalinan.cli.options.generic.ChangeDestination;
 import com.mimpidev.podsalinan.cli.options.settings.AutoQueueEpisodes;
 import com.mimpidev.podsalinan.cli.options.settings.DownloadDirectory;
 import com.mimpidev.podsalinan.cli.options.settings.DownloadSpeedLimit;
 import com.mimpidev.podsalinan.cli.options.settings.MaxDownloaders;
 import com.mimpidev.podsalinan.cli.options.settings.MenuVisibility;
 import com.mimpidev.podsalinan.cli.options.settings.PodcastUpdateRate;
 
 /**
  * @author sbell
  *
  */
 public class SetCommand extends CLIOption {
 
 	/**
 	 * @param newData
 	 */
 	public SetCommand(DataStorage newData) {
 		super(newData);
 		options.put("updateinterval", new PodcastUpdateRate(newData));
 		options.put("downloadlimit", new DownloadSpeedLimit(newData));
 		options.put("maxdownloaders", new MaxDownloaders(newData));
 		options.put("autoqueue", new AutoQueueEpisodes(newData));
 		options.put("defaultdirectory", new DownloadDirectory(newData));
		options.put("destination", new ChangeDestination(newData)); 
 		options.put("menuvisible", new MenuVisibility(newData));
 	}
 
 	@Override
 	public ReturnObject execute(String command) {
 		if (debug) Podsalinan.debugLog.logInfo(this, "Command: "+command);
 		String[] commandOptions = command.split(" ");
 		if (options.containsKey(commandOptions[0].toLowerCase())){
 			returnObject = options.get(commandOptions[0].toLowerCase()).execute(command);
 		} else {
 			System.out.println("Error: Invalid user input.");
 		}
 		
 		return returnObject;
 	}
 
 }
