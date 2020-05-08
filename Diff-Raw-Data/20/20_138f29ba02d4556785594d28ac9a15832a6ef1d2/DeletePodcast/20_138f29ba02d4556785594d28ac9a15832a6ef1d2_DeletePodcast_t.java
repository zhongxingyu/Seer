 /**
  * 
  */
 package com.mimpidev.podsalinan.cli.options.podcast;
 
 import com.mimpidev.podsalinan.DataStorage;
 import com.mimpidev.podsalinan.Podsalinan;
 import com.mimpidev.podsalinan.cli.CLIOption;
 import com.mimpidev.podsalinan.cli.CLInput;
 import com.mimpidev.podsalinan.cli.ReturnCall;
 import com.mimpidev.podsalinan.data.Podcast;
 
 /**
  * @author sbell
  *
  */
 public class DeletePodcast extends CLIOption {
 
 	/**
 	 * @param newData
 	 */
 	public DeletePodcast(DataStorage newData) {
 		super(newData);
 	}
 
 	/* (non-Javadoc)
 	 * @see com.mimpidev.podsalinan.cli.CLIOption#execute(java.lang.String)
 	 */
 	@Override
 	public ReturnCall execute(String command) {
 		if (debug) Podsalinan.debugLog.logInfo("["+getClass().getName()+"] command: "+command);
 		
 		if (command.split(" ").length>1){
 			Podcast selectedPodcast = data.getPodcasts().getPodcastByUid(command.split(" ")[0]);
 			if (selectedPodcast!=null){
 				CLInput input = new CLInput();
 				if(input.confirmRemoval()){
 		    		selectedPodcast.setRemoved(true);
 		    	}
 			}
 			returnObject.methodCall = "Podcast";
			returnObject.methodParameters = "";
 		}
 		
 		return returnObject;
 	}
 
 }
