 /**
  * 
  */
 package com.mimpidev.podsalinan.cli.options.list;
 
 import com.mimpidev.podsalinan.DataStorage;
 import com.mimpidev.podsalinan.Podsalinan;
 import com.mimpidev.podsalinan.cli.CLIOption;
 import com.mimpidev.podsalinan.cli.CLInterface;
 import com.mimpidev.podsalinan.cli.ReturnObject;
 import com.mimpidev.podsalinan.cli.options.downloads.ShowDownloadDetails;
 import com.mimpidev.podsalinan.cli.options.episode.ShowEpisodeDetails;
 import com.mimpidev.podsalinan.cli.options.podcast.ShowPodcastDetails;
 
 /**
  * @author sbell
  *
  */
 public class ListDetails extends CLIOption {
 
 	/**
 	 * @param newData
 	 */
 	public ListDetails(DataStorage newData) {
 		super(newData);
		options.put("download", new ShowDownloadDetails(newData));
 		options.put("episode", new ShowEpisodeDetails(newData));
 		options.put("podcast", new ShowPodcastDetails(newData));
 	}
 
 	@Override
 	public ReturnObject execute(String command) {
 		debug=true;
 
 		if (debug) Podsalinan.debugLog.logInfo(this,"command: "+command);
		String[] globalSelectList = {"download","episode","podcast"};
 		boolean detailsShown=false;
 		int selectionCount=0;
 		while (!detailsShown && selectionCount<globalSelectList.length){
 			if (CLInterface.cliGlobals.getGlobalSelection().containsKey(globalSelectList[selectionCount])){
 				options.get(globalSelectList[selectionCount]).execute("");
 				detailsShown=true;
 			}
 			selectionCount++;
 		}
 		
 		return returnObject;
 	}
 
 }
