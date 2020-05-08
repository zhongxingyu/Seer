 /**
  * 
  */
 package com.mimpidev.podsalinan.cli.options;
 
 import com.mimpidev.podsalinan.DataStorage;
 import com.mimpidev.podsalinan.cli.CLIOption;
 import com.mimpidev.podsalinan.cli.ReturnObject;
 
 /**
  * @author sbell
  *
  */
 public class IncreaseCommand extends CLIOption {
 
 	/**
 	 * @param newData
 	 */
 	public IncreaseCommand(DataStorage newData) {
 		super(newData);
 	}
 
 	@Override
 	public ReturnObject execute(String command) {
 		/*TODO: may need to make sure this command is working, and delete old code no longer needed
 		 * 
 		 */
 		String menuInput = command.replaceFirst(command.split(" ")[0]+" ", "");
 
 		if (menuInput.equalsIgnoreCase("increase")){
 			/*if ((menuList.size()>0)&&
 				(menuList.get(menuList.size()-1).name.equalsIgnoreCase("selectedDownload"))){
 				URLDownload download = ((CLDownloadSelectedMenu)mainMenu.findSubmenu("downloadSelected_menu")).getDownload();
 				data.getUrlDownloads().increasePriority(data.getUrlDownloads().findDownload(download.getURL()));
 			}*/
 		} else if (((menuInput.toLowerCase().startsWith("download")))||
 				 	((menuInput.length()>0)&&(menuInput.length()<3))){
 			menuInput = menuInput.replaceFirst(menuInput.split(" ")[0]+" ", "");
 			
 			if ((menuInput.length()>0)&&(menuInput.length()<3)){
 				int select = convertCharToNumber(menuInput);
 				if ((select>=0)&&(select<data.getUrlDownloads().size())){
 					if (data.getUrlDownloads().increasePriority(select))
 						System.out.println("Increased Priority: "+data.getUrlDownloads().getDownloads().get(select-1).getURL().toString());
 					else
 						System.out.println("Error: Download already highest priority.");
 				}
 			}
 		} else 
 			System.out.println("Error: Invalid user input.");
		return returnObject;
 	}
 
 }
