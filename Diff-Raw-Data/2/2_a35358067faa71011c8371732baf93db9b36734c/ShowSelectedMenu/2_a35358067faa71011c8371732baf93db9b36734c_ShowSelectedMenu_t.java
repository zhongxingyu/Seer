 /**
  * 
  */
 package com.mimpidev.podsalinan.cli.options.downloads;
 
 import java.io.File;
 
 import com.mimpidev.podsalinan.DataStorage;
 import com.mimpidev.podsalinan.Podsalinan;
 import com.mimpidev.podsalinan.cli.CLIOption;
 import com.mimpidev.podsalinan.cli.ReturnCall;
 import com.mimpidev.podsalinan.data.Episode;
 import com.mimpidev.podsalinan.data.URLDownload;
 
 /**
  * @author sbell
  *
  */
 public class ShowSelectedMenu extends CLIOption {
 
 	/**
 	 * @param newData
 	 */
 	public ShowSelectedMenu(DataStorage newData) {
 		super(newData);
 		debug=true;
 	}
 
 	public void printDetails(URLDownload selectedDownload, boolean showDirectory){
		if (selectedDownload!=null){
 			System.out.println("URL: "+selectedDownload.getURL().toString());
 			switch (selectedDownload.getStatus()){
 				case Episode.DOWNLOAD_QUEUED:
 					System.out.println ("Status: Download Queued");
 					break;
 				case Episode.CURRENTLY_DOWNLOADING:
 					System.out.println ("Status: Currently Downloading");
 					break;
 				case Episode.INCOMPLETE_DOWNLOAD:
 					System.out.println ("Status: Download Incomplete");
 					break;
 				case Episode.FINISHED:
 					System.out.println ("Status: Completed Download");
 					break;
 				case Episode.DOWNLOAD_CANCELLED:
 					System.out.println ("Status: Download Cancelled");
 				default:
 					System.out.println ("Status: "+selectedDownload.getStatus());
 			}
 			if ((showDirectory)&&(selectedDownload.getDestination()!=null))
 				System.out.println("Destination: "+selectedDownload.getDestination());
 
 			long fileSize;
 			//String filePath=selectedDownload.getDestination()+fileSystemSlash+getFilenameDownload();
 			if (!selectedDownload.getDestinationFile().isDirectory()){
 				File destination = selectedDownload.getDestinationFile();
 				if (destination.exists())
 					fileSize = destination.length();
 				else
 					fileSize = 0;
 			} else {
 				fileSize=0;
 			}
 
 			// Need to make these sizes human readable
 			System.out.println ("Downloaded: "+humanReadableSize(fileSize)+" / "+humanReadableSize(new Long(selectedDownload.getSize()).longValue()));
 		}
 	}
 
 	
 	/* (non-Javadoc)
 	 * @see com.mimpidev.podsalinan.cli.CLIOption#execute(java.lang.String)
 	 */
 	@Override
 	public ReturnCall execute(String command) {
 		if (debug) Podsalinan.debugLog.logInfo("["+getClass().getName()+"] command: "+command);
 		
 		int downloadId = Integer.parseInt(command.split(" ")[0]);
 		int count=0;
 		for (URLDownload currentDownload: data.getUrlDownloads().getDownloads()){
 			if (!currentDownload.isRemoved()){
 				if (count==downloadId){
 					System.out.println();
 					printDetails(currentDownload,false);
 					System.out.println();
 					System.out.println("1. Delete Download");
 					System.out.println("2. Restart Download");
 					System.out.println("3. Stop Download");
 					System.out.println("4. Start Download (Add to active Queue)");
 					System.out.println("5. Increase Priority");
 					System.out.println("6. Decrease Priority");
 					System.out.println("7. Change Destination");
 					System.out.println();
 					System.out.println("9. Return to Download List");
 				}
 				count++;
 			}
 		}
 
 		return returnObject;
 	}
 
 }
