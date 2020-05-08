 package com.Zolli.EnderCore.Updater;
 
 import com.Zolli.EnderCore.EnderCore;
 import com.Zolli.EnderCore.Logger.simpleLogger.Level;
 import com.Zolli.EnderCore.Utils.networkUtils;
 
 public class updateChecker {
 
 	private EnderCore plugin;
 	private String serverVersion;
 	private String pluginVersion;
 	private releaseChannel selectedChannel;
 	private updateResult result;
 	private String changelog;
 	private String latestVersion;
 	private String latestVersionBuildNumber;
 	private String latestVersionFile;
 	
 	/**
 	 * Constructor
 	 * @param instance Main class instance
 	 */
 	public updateChecker(EnderCore instance) {
 		this.plugin = instance;
 		this.serverVersion = this.formatBukkitVersion(this.plugin.getServer().getBukkitVersion());
 		this.pluginVersion = this.plugin.pluginDescription.getVersion();
 		
 		String configValue = plugin.config.getString("updater.updateChannel");
 		
 		 if(configValue.equalsIgnoreCase("LATEST_BUILD")) {
 			this.selectedChannel = releaseChannel.LATEST_BUILD;
 		 } else if(configValue.equalsIgnoreCase("RELEASE_PREVIEW")) {
 			this.selectedChannel = releaseChannel.RELEASE_PREVIEW;
 		 } else if (configValue.equalsIgnoreCase("STABLE")) {
 			this.selectedChannel = releaseChannel.STABLE;
 		 } else {
 			this.selectedChannel = releaseChannel.STABLE;
 		 }
 		
 		this.result = updateResult.NO_UPDATE;
 	}
 	
 	/**
 	 * Turn server version string into a properly formatted string, that accept the updateCheck server
 	 * @param version The server version
 	 * @return String Formatted value
 	 */
 	private String formatBukkitVersion(String version) {
 		String semiFormatted = version.replace(".", "").toString().replace("-", "").toString().replace('R', '.');
 		String[] parts = semiFormatted.split("\\.");
 		
 		if(parts[0].length() < 3) {
 			parts[0] = parts[0] + "0";
 		}
 		
 		String finalString = parts[0] + "." + parts[1];
 		return finalString;
 	}
 	
 	/**
 	 * Fetch update from update site
 	 */
 	public void checkUpdate() {
 		String updateResponse = networkUtils.getContent("http://ec.zolli.tk/updater/publisher.php?p=" + this.pluginVersion + "&b=" + this.serverVersion + "&c=" + this.selectedChannel.getChannelId());
 		String[] parts = updateResponse.split("ENDSECTION");
 		
 		this.latestVersion = parts[1];
 		this.latestVersionBuildNumber = parts[2];
 		this.changelog = this.paresChangelog(parts[0].split(";"));
 		this.latestVersionFile = parts[3];
 		
 		if(Double.parseDouble(this.latestVersion) > Double.parseDouble(this.pluginVersion)) {
 			this.result = updateResult.UPDATE_AVAILABLE;
 		} else if(updateResponse.equals(null)) { 
 			this.result = updateResult.FAILED_CHECK;
 		} else {
 			this.result = updateResult.NO_UPDATE;
 		}
 	}
 	
 	/**
 	 * Format the string returned by the updater
 	 * @param changes Unformatted changelog
 	 * @return string Formatted changelog
 	 */
 	private String paresChangelog(String[] changes) {
 		String returnString = "";
 		
 		for(int i = 0 ; i < changes.length-1 ; i++ ) {
 			returnString = returnString + changes[i] + ". ";
 		}
 		return returnString;
 	}
 	
 	/**
 	 * Download the selected file from update server and copy it to the update directory
 	 */
 	public void downloadUpdate() {
		updateDownloaderThread downloader = new updateDownloaderThread("http://ec.zolli.tk/updater/files/EnderCore.jar", this.plugin.getRelativePath(), this.plugin.logger);
 		Thread t = new Thread(downloader);
 		t.start();
 		try {
 			t.join();
 			this.result = downloader.getdownloadResult();
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	public updateResult getResult() {
 		return this.result;
 	}
 	
 	public String getChannel() {
 		return this.selectedChannel.getName();
 	}
 	
 	public String getLatestVersion() {
 		return this.latestVersion;
 	}
 	
 	public String getChangelog() {
 		return this.changelog;
 	}
 	
 	/**
 	 *  
 	 * @author Zolli
 	 */
 	public enum updateResult {
 		SUCCESS,
 		NO_UPDATE,
 		UPDATE_AVAILABLE,
 		FAILED_TO_DOWNLOAD,
 		FAILED_CHECK,
 		FAILED_VERSION;
 	}
 	
 	public enum releaseChannel {
 		LATEST_BUILD("Latest build", 1),
 		RELEASE_PREVIEW("Release preview", 2),
 		STABLE("Stable", 3);
 		
 		private String channelName;
 		private int channelId;
 		
 		private releaseChannel(String name, int cid) {
 			this.channelName = name;
 			this.channelId = cid;
 		}
 		
 		public String getName() {
 			return this.channelName;
 		}
 		
 		public int getChannelId() {
 			return this.channelId;
 		}
 		
 	}
 	
 }
