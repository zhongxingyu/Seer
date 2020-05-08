 package com.Zolli.EnderCore.Updater;
 
 import com.Zolli.EnderCore.Logger.simpleLogger;
 import com.Zolli.EnderCore.Logger.simpleLogger.Level;
 import com.Zolli.EnderCore.Updater.updateChecker.updateResult;
 import com.Zolli.EnderCore.Utils.networkUtils;
 
 public class updateDownloaderThread implements Runnable {
 	
 	String downloadUrl;
 	simpleLogger log;
 	updateResult downloadResult;
 	
	public updateDownloaderThread(String url, simpleLogger log) {
 		this.downloadUrl = url;
 		this.log = log;
 	}
 	
 	public updateResult getdownloadResult() {
 		return this.downloadResult;
 	}
 	
 	@Override
 	public void run() {
 		try {
			this.log.log(Level.INFO, this.downloadUrl);
			if(!(networkUtils.downloadAndSave(this.downloadUrl, "./plugins/update/EnderCore.jar", log))) {
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
