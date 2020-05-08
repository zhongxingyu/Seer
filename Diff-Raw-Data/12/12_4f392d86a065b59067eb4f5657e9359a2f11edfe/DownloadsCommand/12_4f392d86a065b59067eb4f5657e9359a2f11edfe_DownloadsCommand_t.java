 /**
  * 
  */
 package com.mimpidev.podsalinan.cli.options;
 
 import com.mimpidev.podsalinan.DataStorage;
 import com.mimpidev.podsalinan.Podsalinan;
 import com.mimpidev.podsalinan.cli.CLIOption;
 import com.mimpidev.podsalinan.cli.ReturnObject;
 import com.mimpidev.podsalinan.cli.options.downloads.SelectDownload;
 import com.mimpidev.podsalinan.cli.options.downloads.ShowMenu;
 
 /**
  * @author sbell
  *
  */
 public class DownloadsCommand extends CLIOption {
 
 	/**
 	 * @param newData
 	 */
 	public DownloadsCommand(DataStorage newData) {
 		super(newData);
 		ShowMenu showMenu = new ShowMenu(newData);
 		options.put("<aa>", new SelectDownload(newData));
 		options.put("showmenu", showMenu);
 		options.put("", showMenu);
 	}
 
 	@Override
 	public ReturnObject execute(String command) {
 		if (debug) Podsalinan.debugLog.logInfo(this,"command: "+command);
 		returnObject.methodCall="downloads";
 		
 		if (options.containsKey(command))
 			options.get(command).execute(command);
 		else {
 			try {
 				Integer.parseInt(command);
 				if (command.equals("9")){
 					returnObject.methodCall="";
 					returnObject.methodParameters="";
 					returnObject.execute=true;
				} else {
					returnObject = options.get("<aa>").execute(command);
 				}
 			} catch (NumberFormatException e) {
 				returnObject = options.get("<aa>").execute(command);
 			}
 		}
 		
 		return returnObject;
 	}
 
 }
