 package com.robonobo.core.api.config;
 
 import java.io.Serializable;
 
 @SuppressWarnings("serial")
 public class RobonoboConfig implements Serializable {
 	/** Our thread pool will contain one thread per processor, plus this value */
 	int threadPoolOverhead = 2;
 	int upnpTimeout = 2000; // ms
 	int upnpDefaultPort = 17235;
 	int upnpPortsToTry = 50;
 	/** Comma-sep list of extra service classes to load */
 	String extraServices = "";
 	/** Comma-sep list of name:class pairs */
 	String extraConfigs = "mina:com.robonobo.mina.external.MinaConfig,wang:com.robonobo.core.wang.RobonoboWangConfig,gui:com.robonobo.gui.GuiConfig";
 	String sonarServerUrl = "http://sonar.robonobo.com/";
 	String metadataServerUrl = "http://midas.robonobo.com/";
 	String metadataServerUsername;
 	String metadataServerPassword;
   String bugReportUrl = "https://github.com/macavity23/robonobo/issues";
 	String helpUrl = "http://getsatisfaction.com/robonobo";
 	String developerUrl = "https://github.com/macavity23/robonobo";
 	String finishedDownloadsDirectory = null;
 	int bufferAutoSaveFreq = 60; // Secs
 	int maxRunningDownloads = 4;
 	String formatSupportProviders = "com.robonobo.plugin.mp3.Mp3FormatSupportProvider";
 	int dataPageSize = 32768;
 	int userUpdateFrequency = 300; // Secs
 	int downloadCacheTime = 30; // Secs
 	boolean agoric = true;
 	/** "auto", "off", or a gateway port number (which means manual) */
 	String gatewayCfgMode = "auto";
 	/**
 	 * If this is true we will ping sonar to find our public address - otherwise we will use whatever is in
 	 * minacfg.gatewayAddress
 	 */
 	boolean lookupGatewayIP = true;
 	boolean agreedToEula = false;
 	boolean allowLoopbackAddress = false;
 	String playlistUrlBase = "http://rbnb.co/p/";
 	String websiteUrlBase = "http://robonobo.com/";
 
 	public RobonoboConfig() {
 	}
 
 	public int getBufferAutoSaveFreq() {
 		return bufferAutoSaveFreq;
 	}
 
 	public String getMetadataServerUrl() {
 		return metadataServerUrl;
 	}
 
 	public int getMaxRunningDownloads() {
 		return maxRunningDownloads;
 	}
 
 	public int getThreadPoolOverhead() {
 		return threadPoolOverhead;
 	}
 
 	public int getUpnpDefaultPort() {
 		return upnpDefaultPort;
 	}
 
 	public int getUpnpPortsToTry() {
 		return upnpPortsToTry;
 	}
 
 	public int getUpnpTimeout() {
 		return upnpTimeout;
 	}
 
 	public void setBufferAutoSaveFreq(int bufferAutoSaveFreq) {
 		this.bufferAutoSaveFreq = bufferAutoSaveFreq;
 	}
 
 	public void setMetadataServerUrl(String defaultMetadataUrl) {
 		this.metadataServerUrl = defaultMetadataUrl;
 	}
 
 	public void setMaxRunningDownloads(int maxRunningDownloads) {
 		this.maxRunningDownloads = maxRunningDownloads;
 	}
 
 	public void setThreadPoolOverhead(int threadPoolOverhead) {
 		this.threadPoolOverhead = threadPoolOverhead;
 	}
 
 	public void setUpnpDefaultPort(int upnpDefaultPort) {
 		this.upnpDefaultPort = upnpDefaultPort;
 	}
 
 	public void setUpnpPortsToTry(int upnpPortsToTry) {
 		this.upnpPortsToTry = upnpPortsToTry;
 	}
 
 	public void setUpnpTimeout(int upnpTimeout) {
 		this.upnpTimeout = upnpTimeout;
 	}
 
 	public String getFormatSupportProviders() {
 		return formatSupportProviders;
 	}
 
 	public void setFormatSupportProviders(String formatSupportProviders) {
 		this.formatSupportProviders = formatSupportProviders;
 	}
 
 	public int getDataPageSize() {
 		return dataPageSize;
 	}
 
 	public void setDataPageSize(int dataPageSize) {
 		this.dataPageSize = dataPageSize;
 	}
 
 	public String getSonarServerUrl() {
 		return sonarServerUrl;
 	}
 
 	public void setSonarServerUrl(String sonarServerUrl) {
 		this.sonarServerUrl = sonarServerUrl;
 	}
 
 	public String getMetadataServerUsername() {
 		return metadataServerUsername;
 	}
 
 	public void setMetadataServerUsername(String metadataServerUsername) {
 		this.metadataServerUsername = metadataServerUsername;
 	}
 
 	public String getMetadataServerPassword() {
 		return metadataServerPassword;
 	}
 
 	public void setMetadataServerPassword(String metadataServerPassword) {
 		this.metadataServerPassword = metadataServerPassword;
 	}
 
 	public int getUserUpdateFrequency() {
 		return userUpdateFrequency;
 	}
 
 	public void setUserUpdateFrequency(int updateFrequency) {
 		this.userUpdateFrequency = updateFrequency;
 	}
 
 	public String getFinishedDownloadsDirectory() {
 		return finishedDownloadsDirectory;
 	}
 
 	public void setFinishedDownloadsDirectory(String downloadDirectory) {
 		this.finishedDownloadsDirectory = downloadDirectory;
 	}
 
 	public String getExtraConfigs() {
 		return extraConfigs;
 	}
 
 	public void setExtraConfigs(String extraConfigs) {
 		this.extraConfigs = extraConfigs;
 	}
 
 	public String getBugReportUrl() {
 		return bugReportUrl;
 	}
 
 	public void setBugReportUrl(String bugReportUrl) {
 		this.bugReportUrl = bugReportUrl;
 	}
 
 	public int getDownloadCacheTime() {
 		return downloadCacheTime;
 	}
 
 	public void setDownloadCacheTime(int preCacheTime) {
 		this.downloadCacheTime = preCacheTime;
 	}
 
 	public String getExtraServices() {
 		return extraServices;
 	}
 
 	public void setExtraServices(String extraServices) {
 		this.extraServices = extraServices;
 	}
 
 	public boolean isAgoric() {
 		return agoric;
 	}
 
 	public void setAgoric(boolean agoric) {
 		this.agoric = agoric;
 	}
 
 	public String getGatewayCfgMode() {
 		return gatewayCfgMode;
 	}
 
 	public void setGatewayCfgMode(String gatewayCfgMode) {
 		this.gatewayCfgMode = gatewayCfgMode;
 	}
 
 	public String getHelpUrl() {
 		return helpUrl;
 	}
 
 	public void setHelpUrl(String helpUrl) {
 		this.helpUrl = helpUrl;
 	}
 
 	public String getDeveloperUrl() {
 		return developerUrl;
 	}
 
 	public void setDeveloperUrl(String wikiUrl) {
 		this.developerUrl = wikiUrl;
 	}
 
 	public boolean getAgreedToEula() {
 		return agreedToEula;
 	}
 
 	public void setAgreedToEula(boolean agreedToEula) {
 		this.agreedToEula = agreedToEula;
 	}
 
 	public boolean getAllowLoopbackAddress() {
 		return allowLoopbackAddress;
 	}
 
 	public void setAllowLoopbackAddress(boolean allowLoopbackAddress) {
 		this.allowLoopbackAddress = allowLoopbackAddress;
 	}
 
 	public String getPlaylistUrlBase() {
 		return playlistUrlBase;
 	}
 
 	public void setPlaylistUrlBase(String playlistUrlBase) {
 		this.playlistUrlBase = playlistUrlBase;
 	}
 
 	public boolean getLookupGatewayIP() {
 		return lookupGatewayIP;
 	}
 
 	public void setLookupGatewayIP(boolean lookupGatewayIP) {
 		this.lookupGatewayIP = lookupGatewayIP;
 	}
 
 	public String getWebsiteUrlBase() {
 		return websiteUrlBase;
 	}
 
 	public void setWebsiteUrlBase(String websiteUrlBase) {
 		this.websiteUrlBase = websiteUrlBase;
 	}
 }
