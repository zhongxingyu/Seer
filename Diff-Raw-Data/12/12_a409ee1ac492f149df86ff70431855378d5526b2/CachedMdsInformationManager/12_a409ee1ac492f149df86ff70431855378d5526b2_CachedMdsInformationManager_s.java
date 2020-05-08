 package org.vpac.grisu.control.info;
 
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.TreeSet;
 
 import org.apache.log4j.Logger;
 
 import au.edu.sapac.grid.mds.QueryClient;
 import au.org.arcs.jcommons.constants.Constants;
 import au.org.arcs.jcommons.interfaces.InformationManager;
 import au.org.arcs.jcommons.utils.SubmissionLocationHelpers;
 
 /**
  * Mds information manager that can uses Gersons mds infosystems library (http://projects.arcs.org.au/trac/infosystems/) to
  * calculate stuff. It caches results for 10 minutes (as default -- configurable) and after that time connects to infosystems
  * again. This makes the lookup faster than when using {@link MdsInformationManager}, which is important esp. for the
  * Grisu Swing client, since this client has to know a lot of mds stuff in order to present sensible choices to the user via
  * the GUI...
  * 
  * @author markus
  *
  */
 public class CachedMdsInformationManager implements InformationManager {
 
 	private static CachedMdsInformationManager singleton = null;
 
 	public static final Long MAX_CACHE_LIFETIME_IN_MS = new Long(1000 * 60 * 10);
 
 	public static InformationManager getDefaultCachedMdsInformationManager(String mdsCacheDirectory) {
 		if ( singleton == null ) {
 			singleton = new CachedMdsInformationManager(mdsCacheDirectory);
 		}
 		return singleton;
 	}
 
 	static final Logger myLogger = Logger
 			.getLogger(CachedMdsInformationManager.class.getName());
 
 	QueryClient client = null;
 	// QueryEngine queryEngine = null;
 
 	// cacheing stuff
 	private String[] allGridSites = null;
 	private Map<String, String> allHosts = null;
  	private Map<String, String[]> allSubmissionQueuesPerApplication = new HashMap<String, String[]>();
 	private Map<String, Map<String, String[]>> allDataLocationsPerVO = new HashMap<String, Map<String, String[]>>();
 
 	private String[] allSubmissionQueues = null;
 
 	private Map<String, String[]> allSubmissionQueuesPerVO = new HashMap<String, String[]>();
 	private Map<String, String[]> allSubmissionQueuesPerApplicationAndVersion = new HashMap<String, String[]>();
 	private Map<String, String[]> allSubmissionLocationsPerSite = new HashMap<String, String[]>();
 	
 	private String[] allApplicationsOnGrid = null;
 	private Map<String, String[]> allApplicationsOnSite = new HashMap<String, String[]>();
 	private Map<String, String[]> allApplicationsOnGridPerVO = new HashMap<String, String[]>();
 
 	private Map<String, String[]> allVersionsOfApplication = new HashMap<String, String[]>();
 	private Map<String, String[]> allVersionsOfApplicationForVO = new HashMap<String, String[]>();
 	private Map<String, String[]> submissionLocationsPerStagingFileSystem = new HashMap<String, String[]>();
 	
 	private Map<String, String> cachedJobManagers = new HashMap<String, String>();
 	private Map<String, String> cachedCreatedSubmissionLocations = new HashMap<String, String>();
 
 	private Map<String, String[]> cachedApplicationsPerExecutables = new HashMap<String, String[]>();
 //	private Map<String, String> defaultStorageElementForSubmissionLocation = new HashMap<String, String>();
 	
 	private Date lastUpdated = null;
 
 	public CachedMdsInformationManager(String mdsCacheFileDirectory) {
 //		client = new QueryClient(Environment.getGrisuDirectory().toString());
 		client = new QueryClient(mdsCacheFileDirectory);
 		lastUpdated = new Date();
 	}
 
 	private void possiblyResetCache() {
 		
 		long timeDifference = new Date().getTime() - lastUpdated.getTime();
 
 		if (timeDifference > MAX_CACHE_LIFETIME_IN_MS) {
 			myLogger.debug("Resetting cache.");
 			allGridSites = null;
 			allHosts = null;
 			allSubmissionQueuesPerApplication = new HashMap<String, String[]>();
 			allDataLocationsPerVO = new HashMap<String, Map<String, String[]>>();
 
 			allSubmissionQueues = null;
 
 			allSubmissionQueuesPerVO = new HashMap<String, String[]>();
 			allSubmissionQueuesPerApplicationAndVersion = new HashMap<String, String[]>();
 			allSubmissionLocationsPerSite = new HashMap<String, String[]>();
 			
 			allApplicationsOnGrid = null;
 			allApplicationsOnSite = new HashMap<String, String[]>();
 			allApplicationsOnGridPerVO = new HashMap<String, String[]>();
 
 			allVersionsOfApplication = new HashMap<String, String[]>();
 			allVersionsOfApplicationForVO = new HashMap<String, String[]>();
 			submissionLocationsPerStagingFileSystem = new HashMap<String, String[]>();
 			
 			cachedJobManagers = new HashMap<String, String>();
 			cachedCreatedSubmissionLocations = new HashMap<String, String>();
 			
 			cachedApplicationsPerExecutables = new HashMap<String, String[]>();
 			
 			lastUpdated = new Date();
 		}
 	}
 
 	public String[] getAllSites() {
 
 		possiblyResetCache();
 
 		if (allGridSites == null) {
 			allGridSites = client.getSitesOnGrid();
 		}
 		return allGridSites;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.vpac.grisu.control.info.InformationManager#getSiteForHostOrUrl(java.lang.String)
 	 */
 	public String getSiteForHostOrUrl(String host_or_url) {
 
 		possiblyResetCache();
 
		return getAllHosts().get(host_or_url);
 		// String site = client.getSiteForHost(getHost(host_or_url));
 		// return site;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.vpac.grisu.control.info.InformationManager#getAllSubmissionQueues(java.lang.String)
 	 */
 	public String[] getAllSubmissionLocationsForApplication(String application) {
 
 		possiblyResetCache();
 
 		if (allSubmissionQueuesPerApplication.get(application) == null) {
 			String[] subLocs = calculateAllSubmissionQueuesForApplication(application);
 			allSubmissionQueuesPerApplication.put(application, subLocs);
 		}
 
 		return allSubmissionQueuesPerApplication.get(application);
 	}
 
 	private String[] calculateAllSubmissionQueuesForApplication(
 			String application) {
 		// get all sites with installed application/version
 		String[] sites = client.getSitesWithCode(application);
 		String[] subLocs = getContactStringsForSitesWithApplication(sites,
 				application);
 		return subLocs;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.vpac.grisu.control.info.InformationManager#getDataLocationsForVO(java.lang.String)
 	 */
 	public Map<String, String[]> getDataLocationsForVO(String fqan) {
 
 		possiblyResetCache();
 
 		if (allDataLocationsPerVO.get(fqan) == null) {
 			Map<String, String[]> dataLocationsMap = calculateDataLocationsForVO(fqan);
 
 			allDataLocationsPerVO.put(fqan, dataLocationsMap);
 		}
 		Map<String, String[]> result = allDataLocationsPerVO.get(fqan);
 		return result;
 	}
 
 	private Map<String, String[]> calculateDataLocationsForVO(String fqan) {
 
 		/*
 		 * String[] storageElements = client.getStorageElementsForSite(site); if
 		 * (storageElements != null) { for (int j = 0; j <
 		 * storageElements.length; j++) { String[] gridFTPServers =
 		 * client.getGridFTPServersForStorageElementAtSite (site,
 		 * storageElements[j]); String[] dataDirs = client.getDataDir(site,
 		 * storageElements[j], fqan);
 		 * 
 		 * for (int k = 0; k < gridFTPServers.length; k++) { if (dataDirs !=
 		 * null && dataDirs.length > 0) {
 		 * dataLocationsMap.put(gridFTPServers[k], dataDirs); } } } }
 		 */
 
 		// alternative way of searching dataLocations is..
 		// get all the storage elements for VO
 		// for each storage element
 		// get gridftp server
 		// get data directories
 		// another alternative is to use java-xml binding (xmlbeans!)
 		Map<String, String[]> dataLocationsMap = new HashMap<String, String[]>();
 		it.infn.cnaf.forge.glueschema.spec.v12.r2.StorageElementType[] storageElements = ((QueryClient) client)
 				.getStorageElementsForVO(fqan);
 		if (storageElements != null) {
 			for (int j = 0; j < storageElements.length; j++) {
 				it.infn.cnaf.forge.glueschema.spec.v12.r2.SEAccessProtocolType[] accessProtocols = storageElements[j]
 						.getAccessProtocolArray();
 
 				// now get the storageAreas that VO fqan has access to
 				it.infn.cnaf.forge.glueschema.spec.v12.r2.StorageAreaType[] allStorageAreas = storageElements[j]
 						.getStorageAreaArray();
 
 				java.util.List<String> voStorageAreaList = new java.util.ArrayList<String>();
 
 				for (int k = 0; k < allStorageAreas.length; k++) {
 					String[] aclTypes = allStorageAreas[k].getACL()
 							.getRuleArray();
 
 					for (int l = 0; l < aclTypes.length; l++) {
 						if (aclTypes[l].equalsIgnoreCase(fqan)) {
 							voStorageAreaList.add(allStorageAreas[k].getPath());
 						}
 					}
 				}
 
 				for (int k = 0; k < accessProtocols.length; k++) {
 					if (!voStorageAreaList.isEmpty()) {
 						dataLocationsMap.put(accessProtocols[k].getEndpoint(),
 								(String[]) voStorageAreaList
 										.toArray(new String[0]));
 					}
 				}
 
 			}
 		}
 		return dataLocationsMap;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.vpac.grisu.control.info.InformationManager#getAllSubmissionQueues()
 	 */
 	public String[] getAllSubmissionLocations() {
 
 		possiblyResetCache();
 
 		if (allSubmissionQueues == null) {
 
 			allSubmissionQueues = calculateAllSubmissionQueues();
 		}
 		return allSubmissionQueues;
 	}
 
 	private String[] calculateAllSubmissionQueues() {
 		// get all sites
 		String[] sites = client.getSitesOnGrid();
 		return getContactStringsForSites(sites);
 	}
 
 	public String[] getAllSubmissionLocationsForVO(String fqan) {
 
 		possiblyResetCache();
 
 		if (Constants.NON_VO_FQAN.equals(fqan)) {
 			return getAllSubmissionLocations();
 		}
 
 		if (allSubmissionQueuesPerVO.get(fqan) == null) {
 			allSubmissionQueuesPerVO.put(fqan,
 					calculateAllSubmissionQueuesForVO(fqan));
 		}
 		return allSubmissionQueuesPerVO.get(fqan);
 	}
 
 	private String[] calculateAllSubmissionQueuesForVO(String fqan) {
 		String[] sites = ((QueryClient) client).getSitesForVO(fqan);
 		return getContactStringsForSites(sites, fqan);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.vpac.grisu.control.info.InformationManager#getAllSubmissionQueues(java.lang.String,
 	 *      java.lang.String)
 	 */
 	public String[] getAllSubmissionLocations(String application, String version) {
 
 		possiblyResetCache();
 
 		if (allSubmissionQueuesPerApplicationAndVersion.get(application
 				+ version) == null) {
 			allSubmissionQueuesPerApplicationAndVersion.put(application
 					+ version, calculateAllSubmissionQueues(application,
 					version));
 		}
 		return allSubmissionQueuesPerApplicationAndVersion.get(application
 				+ version);
 	}
 
 	private String[] calculateAllSubmissionQueues(String application,
 			String version) {
 		// get all sites with installed application/version
 		String[] sites = client.getSitesWithAVersionOfACode(application,
 				version);
 		return getContactStringsForSitesWithApplication(sites, application,
 				version);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.vpac.grisu.control.info.InformationManager#getVersionsOfApplicationOnSite(java.lang.String,
 	 *      java.lang.String)
 	 */
 	public String[] getVersionsOfApplicationOnSite(String application,
 			String site) {
 		// TODO cache this as well?
 		return client.getVersionsOfCodeAtSite(site, application);
 	}
 	
 	/* (non-Javadoc)
 	 * @see org.vpac.grisu.control.info.InformationManager#getVersionsOfApplicationOnSubmissionLocation(java.lang.String, java.lang.String)
 	 */
 	public String[] getVersionsOfApplicationOnSubmissionLocation(
 			String application, String submissionLocation) {
 		
 		String queue = SubmissionLocationHelpers.extractQueue(submissionLocation);
 		String host = SubmissionLocationHelpers.extractHost(submissionLocation);
 		
 		String[] temp = client.getVersionsOfCodeForQueueAndContactString(queue, host, application);
 		return temp;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.vpac.grisu.control.info.InformationManager#getAllApplicationsOnGrid()
 	 */
 	public String[] getAllApplicationsOnGrid() {
 
 		possiblyResetCache();
 
 		if (allApplicationsOnGrid == null) {
 			allApplicationsOnGrid = calculateAllApplicationsOnGrid();
 		}
 		return allApplicationsOnGrid;
 	}
 	
 	public String[] getAllApplicationsOnGridForVO(String fqan) {
 		
 		possiblyResetCache();
 		
 		if ( allApplicationsOnGridPerVO.get(fqan) == null ) {
 			String[] temp = client.getCodesOnGridForVO(fqan);
 			allApplicationsOnGridPerVO.put(fqan, temp);
 		}
 		return allApplicationsOnGridPerVO.get(fqan);
 		
 	}
 
 	private String[] calculateAllApplicationsOnGrid() {
 		return client.getCodesOnGrid();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.vpac.grisu.control.info.InformationManager#getAllApplicationsAtSite(java.lang.String)
 	 */
 	public String[] getAllApplicationsAtSite(String site) {
 
 		possiblyResetCache();
 
 		if (allApplicationsOnSite.get(site) == null) {
 			allApplicationsOnSite.put(site,
 					calculateAllApplicationsAtSite(site));
 		}
 		return allApplicationsOnSite.get(site);
 	}
 
 	private String[] calculateAllApplicationsAtSite(String site) {
 		return client.getCodesAtSite(site);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.vpac.grisu.control.info.InformationManager#getAllVersionsOfApplicationOnGrid(java.lang.String)
 	 */
 	public String[] getAllVersionsOfApplicationOnGrid(String application) {
 
 		possiblyResetCache();
 
 		if (allVersionsOfApplication.get(application) == null) {
 			allVersionsOfApplication.put(application,
 					calculateAllVersionsOfApplicationOnGrid(application));
 		}
 		return allVersionsOfApplication.get(application);
 	}
 	
 	public String[] getAllVersionsOfApplicationOnGridForVO(String application, String vo) {
 
 		possiblyResetCache();
 
 		String key = application+"_"+vo;
 		
 		if (allVersionsOfApplicationForVO.get(key) == null) {
 			allVersionsOfApplicationForVO.put(key,
 					client.getVersionsOfCodeOnGridForVO(application, vo));
 		}
 		return allVersionsOfApplicationForVO.get(key);
 	}
 
 	private String[] calculateAllVersionsOfApplicationOnGrid(String application) {
 		return client.getVersionsOfCodeOnGrid(application);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.vpac.grisu.control.info.InformationManager#getApplicationDetails(java.lang.String,
 	 *      java.lang.String, java.lang.String)
 	 */
 	public Map<String, String> getApplicationDetails(String application,
 			String version, String site) {
 		Map<String, String> codeDetails = new HashMap<String, String>();
 
 		codeDetails.put(Constants.MDS_MODULES_KEY, client.getModuleNameOfCodeAtSite(site,
 				application, version));
 		codeDetails.put(Constants.MDS_SERIAL_AVAIL_KEY, Boolean.toString(client
 				.isSerialAvailForCodeAtSite(site, application, version)));
 		codeDetails.put(Constants.MDS_PARALLEL_AVAIL_KEY, Boolean.toString(client
 				.isParallelAvailForCodeAtSite(site, application, version)));
 		String[] executables = client.getExeNameOfCodeAtSite(site, application,
 				version);
 		StringBuffer exeStrBuff = new StringBuffer();
 		for (int i = 0; i < executables.length; i++) {
 			exeStrBuff.append(executables[i]);
 			if (i < executables.length - 1)
 				exeStrBuff.append(",");
 		}
 		codeDetails.put(Constants.MDS_EXECUTABLES_KEY, exeStrBuff.toString());
 		return codeDetails;
 	}
 
 	public String[] getStagingFileSystemForSubmissionLocation(String subLoc) {
 
 		possiblyResetCache();
 
 		if (submissionLocationsPerStagingFileSystem.get(subLoc) == null) {
 			submissionLocationsPerStagingFileSystem.put(subLoc,
 					calculateStagingFileSystemForSubmissionLocation(subLoc));
 		}
 		return submissionLocationsPerStagingFileSystem.get(subLoc);
 	}
 
 	private String[] calculateStagingFileSystemForSubmissionLocation(
 			String subLoc) {
 		// subLoc = queuename@cluster:contactstring#JobManager
 		int queSepIndex = subLoc.indexOf(":");
 		if ( queSepIndex < 1 ) {
 			throw new RuntimeException("Wrong submission location format. Queue missing in subLoc: "+subLoc);
 		}
 		String queueName = subLoc.substring(0, queSepIndex);
 		String contactString = "";
 		if (subLoc.indexOf("#") > 0) {
 			contactString = subLoc.substring(subLoc.indexOf(":") + 1, subLoc
 					.indexOf("#"));
 		} else {
 			contactString = subLoc.substring(subLoc.indexOf(":") + 1);
 		}
 
 		// get site name for contact string
 		// GridInfoInterface client = new QueryClient();
 		String siteName = client.getSiteForHost(contactString);
 
 		String[] result = client.getGridFTPServersForQueueAtSite(siteName,
 				queueName);
 		
 		return result;
 	}
 
 	private String[] getContactStringsForSitesWithApplication(String[] sites,
 			String application) {
 		List<String> list = new ArrayList<String>();
 
 		for (int i = 0; sites != null && i < sites.length; i++) {
 
 			// once site names are found, find all the queues for each of the
 			// sites
 			String[] queues = client.getQueueNamesForCodeAtSite(sites[i],
 					application);
 
 			// TODO: this method and the one below can probably be merged into
 			// one
 
 			for (int j = 0; queues != null && j < queues.length; j++) {
 
 				// once queues are found, find all the contact string for
 				// all the queues
 				String[] contactStrings = client.getContactStringOfQueueAtSite(
 						sites[i], queues[j]);
 				String jobManager = getJobmanagerOfQueueAtSite(sites[i],
 						queues[j]);
 
 				for (int k = 0; contactStrings != null
 						&& k < contactStrings.length; k++) {
 					String hostname = contactStrings[k].substring(
 							contactStrings[k].indexOf("https://") != 0 ? 0 : 8,
 							contactStrings[k].indexOf(":8443"));
 					if (jobManager != null) {
 						if (jobManager.toLowerCase().indexOf("pbs") < 0)
 							list.add(queues[j] + ":" + hostname + "#"
 									+ jobManager);
 						else
 							list.add(queues[j] + ":" + hostname);
 					}
 
 				}
 			}
 		}
 		return list.toArray(new String[list.size()]);
 	}
 	
 
 	
 
 	public String getJobmanagerOfQueueAtSite(String site, String queue) {
 		
 		String key = site+queue;
 		
 		if ( cachedJobManagers.get(key) == null ) {
 			String jobManager = client.getJobManagerOfQueueAtSite(site, queue);
 			cachedJobManagers.put(key, jobManager);
 		}
 		return cachedJobManagers.get(key);
 	}
 	
 	private String[] getContactStringsForSitesWithApplication(String[] sites,
 			String application, String version) {
 		List<String> list = new ArrayList<String>();
 
 		for (int i = 0; sites != null && i < sites.length; i++) {
 
 			// once site names are found, find all the queues for each of the
 			// sites
 			String[] queues = client.getQueueNamesForCodeAtSite(sites[i],
 					application, version);
 
 			for (int j = 0; queues != null && j < queues.length; j++) {
 
 				// once queues are found, find all the contact string for
 				// all the queues
 				String[] contactStrings = client.getContactStringOfQueueAtSite(
 						sites[i], queues[j]);
 				String jobManager = client.getJobManagerOfQueueAtSite(sites[i],
 						queues[j]);
 
 				for (int k = 0; contactStrings != null
 						&& k < contactStrings.length; k++) {
 					String hostname = contactStrings[k].substring(
 							contactStrings[k].indexOf("https://") != 0 ? 0 : 8,
 							contactStrings[k].indexOf(":8443"));
 					if (jobManager != null) {
 						if (jobManager.toLowerCase().indexOf("pbs") < 0)
 							list.add(queues[j] + ":" + hostname + "#"
 									+ jobManager);
 						else
 							list.add(queues[j] + ":" + hostname);
 					}
 
 				}
 			}
 		}
 		return list.toArray(new String[list.size()]);
 	}
 
 	private String[] getContactStringsForSites(String[] sites) {
 		return getContactStringsForSites(sites, null);
 		
 	}
 	
 	private String[] getContactStringsForSites(String[] sites, String fqan) {
 
 		List<String> list = new ArrayList<String>();
 
 		for (int i = 0; sites != null && i < sites.length; i++) {
 
 			// once site names are found, find all the queues for each of the
 			// sites
 			String[] queues = null;
     		
     		if (fqan == null) {
     			queues = client.getQueueNamesAtSite(sites[i]);
     		} else {
     			queues = client.getQueueNamesAtSite(sites[i], fqan);
     		}
 
 			for (int j = 0; queues != null && j < queues.length; j++) {
 
 				// once queues are found, find all the contact string for
 				// all the queues
 				String[] contactStrings = client.getContactStringOfQueueAtSite(
 						sites[i], queues[j]);
 				String jobManager = client.getJobManagerOfQueueAtSite(sites[i],
 						queues[j]);
 
 				for (int k = 0; contactStrings != null
 						&& k < contactStrings.length; k++) {
 					String hostname = contactStrings[k].substring(
 							contactStrings[k].indexOf("https://") != 0 ? 0 : 8,
 							contactStrings[k].indexOf(":8443"));
 					if (jobManager != null) {
 						if (jobManager.toLowerCase().indexOf("pbs") < 0)
 							list.add(queues[j] + ":" + hostname + "#"
 									+ jobManager);
 						else
 							list.add(queues[j] + ":" + hostname);
 					}
 				}
 			}
 		}
 		return list.toArray(new String[list.size()]);
 	}
 
 	private Set<String> getComputeHostsForSite(String site) {
 		Set<String> set = new HashSet<String>();
 
 		String[] queues = client.getQueueNamesAtSite(site);
 
 		for (int j = 0; queues != null && j < queues.length; j++) {
 
 			// once queues are found, find all the contact string for
 			// all the queues
 			String[] contactStrings = client.getContactStringOfQueueAtSite(
 					site, queues[j]);
 
 			for (int k = 0; contactStrings != null && k < contactStrings.length; k++) {
 				// remove protocol and port number from the string
 				String hostname = contactStrings[k].substring(contactStrings[k]
 						.indexOf("https://") != 0 ? 0 : 8, contactStrings[k]
 						.indexOf(":8443"));
 				set.add(hostname);
 			}
 		}
 		return set;
 	}
 
 	private Set<String> getDataHostsForSite(String site) {
 		Set<String> set = new HashSet<String>();
 
 		String[] dataHosts = client.getGridFTPServersAtSite(site);
 
 		for (int j = 0; dataHosts != null && j < dataHosts.length; j++) {
 
 			// remove protocol and port number from the string
 			String hostname = dataHosts[j].substring(dataHosts[j]
 					.indexOf("://") + 3, dataHosts[j].lastIndexOf(":"));
 			set.add(hostname);
 		}
 		return set;
 	}
 
 	private static String getHost(String host_or_url) {
 		// dodgy, I know
 		URI url;
 		try {
 			url = new URI(host_or_url);
 			if (url.getHost() != null) {
 				host_or_url = url.getHost();
 			}
 		} catch (URISyntaxException e) {
 			// doesn't matter
 		}
 
 		return host_or_url;
 	}
 
 	public synchronized Map<String, String> getAllHosts() {
 
 		possiblyResetCache();
 
 		if (allHosts == null) {
 			allHosts = calculateAllHosts();
 		}
 		return allHosts;
 	}
 
 	private synchronized Map<String, String> calculateAllHosts() {
 		myLogger.debug("Starting getting all hosts from mds");
 
 		Map<String, String> tempHosts = new HashMap<String, String>();
 		String[] sites = getAllSites();
 		for (int i = 0; sites != null && i < sites.length; i++) {
 			Set<String> hosts = getComputeHostsForSite(sites[i]);
 			hosts.addAll(getDataHostsForSite(sites[i]));
 
 			for (String host : hosts) {
 				tempHosts.put(host, sites[i]);
 			}
 		}
 		myLogger.debug("Finished getting all hosts from mds");
 		return tempHosts;
 	}
 	
 	public String[] getApplicationsThatProvideExecutable(String executable) {
 		
 		if ( cachedApplicationsPerExecutables.get(executable) == null ) {
 			String[] apps = client.getApplicationNamesThatProvideExecutable(executable);
 			cachedApplicationsPerExecutables.put(executable, apps);
 		}
 		return cachedApplicationsPerExecutables.get(executable);
 	}
 
 	
 	public static void main(String[] args) {
 		
 		CachedMdsInformationManager info = new CachedMdsInformationManager(System.getProperty("user.home"));
 		
 		String[] allAppsOnGrid = info.getAllApplicationsOnGrid();
 		Arrays.sort(allAppsOnGrid);
 		
 		System.out.println("---------------------------------------------------");
 		System.out.println("All applications on the grid");
 		for ( String app : allAppsOnGrid ) {
 			System.out.println(app);
 		}
 		System.out.println("---------------------------------------------------");
 		System.out.println();
 		
 		for ( String app : allAppsOnGrid ) {
 			
 			Set<String> allExecutables = new TreeSet<String>();
 			
 			System.out.println(app+":");
 			System.out.println();
 			String[] allSubLocs = info.getAllSubmissionLocationsForApplication(app);
 			Set<String> allSites = new TreeSet<String>();
 			for ( String subLoc : allSubLocs ) {
 				String site = info.getSiteForHostOrUrl(SubmissionLocationHelpers.extractHost(subLoc));
 				allSites.add(site);
 			}
 			System.out.println();
 
 			System.out.println(app+" is available on "+allSubLocs.length+" submission queues on these sites: ");
 			for ( String siteTemp : allSites ) {
 				String[] allVersionsOnSite = info.getVersionsOfApplicationOnSite(app, siteTemp);
 				String exe = null;
 				for ( String version : allVersionsOnSite ) {
 					Map<String, String> appDetails = info.getApplicationDetails(app, version, siteTemp);
 					exe = appDetails.get(Constants.MDS_EXECUTABLES_KEY);
 					if ( exe.contains(",") ) {
 						for ( String exeTemp : exe.split(",") ) {
 							allExecutables.add(exeTemp);
 						}
 					} else {
 						allExecutables.add(exe);
 					}
 				}
 				if ( exe == null || exe.length() == 0 ) {
 					System.out.println(siteTemp+"\t(No executable specified!)");
 				} else {
 					System.out.println(siteTemp+":\t(exe: "+exe+")");
 				}
 			}
 			
 
 			System.out.println();
 			String[] allVersions = info.getAllVersionsOfApplicationOnGrid(app);
 			System.out.println("All versions for: "+app);
 			for ( String version : allVersions ) {
 				System.out.println(version);
 			}
 			
 			System.out.println();
 			System.out.println("All executables for: "+app);
 			for ( String exe : allExecutables ) {
 				System.out.println(exe);
 			}
 			
 			System.out.println("---------------------------------------------------");
 			System.out.println();
 		}
 	}
 
 	public String[] getAllSubmissionLocationsForSite(String site) {
 
 			possiblyResetCache();
 
 			if (allSubmissionLocationsPerSite.get(site) == null) {
 				allSubmissionLocationsPerSite.put(site,
 						calculateAllApplicationsAtSite(site));
 			}
 			return allSubmissionLocationsPerSite.get(site);
 	}
 
 
 //	public void informationChanged(Date dateTheInfoChanged) {
 //		myLogger.info("Resetting cached mds information.");
 //		possiblyResetCache();
 //	}
 
 }
