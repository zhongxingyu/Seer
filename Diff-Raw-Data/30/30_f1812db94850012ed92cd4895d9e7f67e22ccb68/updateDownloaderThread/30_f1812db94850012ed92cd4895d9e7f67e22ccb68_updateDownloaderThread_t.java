 package com.Zolli.EnderCore.Updater;
 
 import com.Zolli.EnderCore.Logger.simpleLogger;
 import com.Zolli.EnderCore.Logger.simpleLogger.Level;
 import com.Zolli.EnderCore.Updater.updateChecker.updateResult;
 import com.Zolli.EnderCore.Utils.networkUtils;
 
 public class updateDownloaderThread implements Runnable {
 	
 	String downloadUrl;
 	simpleLogger log;
	String saveLocation;
 	updateResult downloadResult;
 	
	public updateDownloaderThread(String url, String saveLocation, simpleLogger log) {
 		this.downloadUrl = url;
		this.saveLocation = saveLocation;
 		this.log = log;
 	}
 	
 	public updateResult getdownloadResult() {
 		return this.downloadResult;
 	}
 	
 	@Override
 	public void run() {
 		try {
			if(!(networkUtils.downloadAndSave(this.downloadUrl, "./" + this.saveLocation.replace("plugins", "plugins/update"), log))) {
 				this.downloadResult = updateResult.FAILED_TO_DOWNLOAD;	
 			} else {
 				this.downloadResult = updateResult.SUCCESS;
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		} finally {
 			this.log.log(Level.INFO, "Update downloaded sucessfully");
 		}
 	}
 	
 
 }
