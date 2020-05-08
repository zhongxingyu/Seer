 /**
  * 
  */
 package podsalinan;
 
 import java.io.File;
 import java.text.DecimalFormat;
 
 /**
  * @author sbell
  *
  */
 public class CLDownloadSelectedMenu extends CLMenu {
 	private URLDownload download;
 	private URLDownloadList downloadList;
     private CLInput input;
 	
 	/**
 	 * @param newMenuList
 	 * @param downloadList2 
 	 * @param newMenuName
 	 */
 	public CLDownloadSelectedMenu(ProgSettings newMenuList, URLDownloadList downloads) {
 		super(newMenuList, "downloadSelected_menu");
 		String[] menuList = {"1. Delete Download",
 				             "2. Restart Download",
 				             "3. Stop Download",
 				             "4. Increase Priority",
 				             "5. Decrease Priority",
 				             "6. Change Destination",
 				             "",
 				             "9. Return to Download List"};
 		setMainMenuList(menuList);
 		setDownloadList(downloads);
 		input = new CLInput();
 	}
 
 	public void setDownload(URLDownload urlDownload) {
 		download = urlDownload;
 	}
 
 	public URLDownload getDownload(){
 		return download;
 	}
 
 	/**
 	 * @return the downloadList
 	 */
 	public URLDownloadList getDownloadList() {
 		return downloadList;
 	}
 
 	/**
 	 * @param downloadList the downloadList to set
 	 */
 	public void setDownloadList(URLDownloadList downloadList) {
 		this.downloadList = downloadList;
 	}
 
 	public void printMainMenu() {
 		printDetails(download,false);
 		super.printMainMenu();
 	}
 	
 	public void printDetails(URLDownload selectedDownload, boolean showDirectory){
 		if ((selectedDownload==null)&&(download!=null))
 			selectedDownload=download;
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
		//TODO: need to connect CLDownloadSelectedMenu to DataStorage object
		if (selectedDownload.getStatus()!=Details.CURRENTLY_DOWNLOADING){
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
 
 	private String humanReadableSize(long fileSize) {
 		String fileSizeModifier="";
 		double newOutputSize;
 		
 		if (fileSize>1073741824){
 			fileSizeModifier=" Gb";
 			newOutputSize = (double)fileSize/1073741824;
 		} else if (fileSize>1048576){
 			fileSizeModifier=" Mb";
 			newOutputSize = (double)fileSize/1048576;
 		} else if (fileSize>1024){
 			fileSizeModifier=" Kb";
 			newOutputSize = (double)fileSize/1024;
 		} else
 			newOutputSize = (double)fileSize;
 
 		newOutputSize=new Double(new DecimalFormat("#.##").format(newOutputSize)).doubleValue();
 		
         if (newOutputSize==0)
         	return "0";
         else
         	return Double.toString(newOutputSize)+fileSizeModifier;
 	}
 
 	public void process(int inputInt){
 		//System.out.println("CLDownloadSelectedMenu.process(int)");
 		if (menuList.size()==2){
 			switch (inputInt){
 		        case 1:
 		        	// Delete the download from the queue.
 		        	
  		    	    // search downloaders array to see if it is currently downloading.
  		    	    // stop downloader if active
 		        	
  		    	    downloadList.deleteDownload(downloadList.findDownload(download.getURL()));
  		    	    // because the download is being deleted, remove the selected download from the
  		    	    // menuList
  		    	    setDownload(null);
  		    	    menuList.removeSetting("selectedDownload");
 		    	    break;
 		        case 2:
 		        	// Restart the download in the queue
 		        	
  		    	    // search downloaders array to see if it is currently downloading.
  		    	    // stop downloader if active
 		        	downloadList.restartDownload(download.getURL());
 		    	    break;
 		        case 3:
 		        	// Stop Download
 
 		        	// search downloaders array to see if it is currently downloading.
  		    	    // stop downloader if active
 		        	downloadList.cancelDownload(download.getURL());
 		    	    break;
 		        case 4:
 		        	// Increase Download priority
 		        	downloadList.increasePriority(downloadList.findDownload(download.getURL()));
 		        	break;
 		        case 5:
 		        	// Decrease download priority
 		        	downloadList.decreasePriority(downloadList.findDownload(download.getURL()));
 		        	break;
 		        case 6:
 		        	// Change Destination
 					System.out.println ();
 					String destination = download.getDestination();
 					
 					System.out.print ("Enter Download Destination["+destination+"]: ");
 					String userInput=input.getStringInput();
 			    	changeDirectory(download,userInput);
 		        	break;
 				case 9:
 					setDownload(null);
 					menuList.removeSetting("selectedDownload");
 					break;
 				case 98:
 					printDetails(download,true);
 					break;
 				case 99:
 					printMainMenu();
 			}
 		}
 		//System.out.println("menuList.size()="+menuList.size());
 		if ((menuList.size()==2)&&(inputInt!=98)&&(inputInt!=99)){
 			inputInt=-1000;
 			super.process(inputInt);
 		}
 	}
 	
 	public void process(String userInput){
 		//System.out.println("CLDownloadSelectedMenu.process(String)");
 
 		super.process(userInput);
 	}
 	
 	public boolean changeDirectory(URLDownload selectedDownload, String userInput){
 		if (selectedDownload==null)
 			selectedDownload=download;
 		File newPath;
 		if ((userInput.length()>0)&&(userInput!=null)){
 			newPath=new File(userInput);
 			if ((newPath.exists())&&(newPath.isDirectory())){
 				selectedDownload.setDestination(userInput);
 				selectedDownload.setUpdated(true);
 				System.out.println("Download path: "+selectedDownload.getDestination());
 				return true;
 			} else if ((newPath.getParentFile()!=null)&&((newPath.getParentFile().exists())&&
 					   (newPath.getParentFile().isDirectory()))){
 				System.out.println("Error: Directory does no exist.");
 				if (input.confirmCreation()){
 					newPath.mkdir();
 					System.out.println("Directory Created: "+userInput);
 					selectedDownload.setDestination(userInput);
 					selectedDownload.setUpdated(true);
 				}
 			} else {
 				System.out.println ("Error: Invalid path");
 			}
 		}
 		return false;
 	}
 }
