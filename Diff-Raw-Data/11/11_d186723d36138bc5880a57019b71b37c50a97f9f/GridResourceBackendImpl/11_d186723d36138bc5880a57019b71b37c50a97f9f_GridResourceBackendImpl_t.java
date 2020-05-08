 package au.org.arcs.grid.sched;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Set;
 
 import au.org.arcs.jcommons.interfaces.GridResource;
 
 public class GridResourceBackendImpl implements Rankable, GridResource {
 	
 	private String contactString;
 	private String jobManager;
 	private String queueName;
 	
 	// a few items that can be used for ranking resources
 	private double siteLatitude;
 	private double siteLongitude;
 	private String siteName;
 	
 	private String applicationName; // don't think this field will be used
 	private List<String> applicationVersions;
 	
 	private int freeJobSlots;
 	private int runningJobs;
 	private int waitingJobs;
 	private int totalJobs;
 	
 	private int mainMemoryRAMSize;
 	private int mainMemoryVirtualSize;
 	
 	private int physicalCPUs;
 	private int logicalCPUs;
 	private int smpSize;
 	
 	private boolean isHostOnDesiredSite;
 	private boolean isDesiredSoftwareVersionInstalled;
 	
 	private Set<String> allExecutables = null;
 	
 	private RankingAlgorithm rankingAlgorithm;
 
 	/**
 	 * Uses SimpleARCSRankingAlgorithm is none is specified
 	 */
 	public GridResourceBackendImpl() {
 		this(new SimpleResourceRankingAlgorithm());
 	}
 	
 	public GridResourceBackendImpl(RankingAlgorithm rankingAlgorithm) {
 		queueName = "";
 		contactString = "";
 		jobManager = "";
 		siteLatitude = 0;
 		siteLongitude = 0;
 		siteName = "";
 		applicationName = "";
 		applicationVersions = new ArrayList<String>();
 		freeJobSlots = 0;
 		runningJobs = 0;
 		waitingJobs = 0;
 		totalJobs = 0;
 		mainMemoryRAMSize = 0;
 		mainMemoryVirtualSize = 0;
 		physicalCPUs = 0;
 		logicalCPUs = 0;
 		smpSize = 0;
 		isHostOnDesiredSite = false;
 		isDesiredSoftwareVersionInstalled = false;
 		this.rankingAlgorithm = rankingAlgorithm;
 	}
 
 	/* (non-Javadoc)
 	 * @see au.org.arcs.grid.sched.GridResource#getSiteLatitude()
 	 */
 	public double getSiteLatitude() {
 		return siteLatitude;
 	}
 
 	public void setSiteLatitude(double siteLatitude) {
 		this.siteLatitude = siteLatitude;
 	}
 
 	/* (non-Javadoc)
 	 * @see au.org.arcs.grid.sched.GridResource#getSiteLongitude()
 	 */
 	public double getSiteLongitude() {
 		return siteLongitude;
 	}
 
 	public void setSiteLongitude(double siteLongitude) {
 		this.siteLongitude = siteLongitude;
 	}
 
 	/* (non-Javadoc)
 	 * @see au.org.arcs.grid.sched.GridResource#getSiteName()
 	 */
 	public String getSiteName() {
 		return siteName;
 	}
 
 	public void setSiteName(String siteName) {
 		this.siteName = siteName;
 	}
 
 	/* (non-Javadoc)
 	 * @see au.org.arcs.grid.sched.GridResource#getApplicationName()
 	 */
 	public String getApplicationName() {
 		return applicationName;
 	}
 
 	public void setApplicationName(String applicationName) {
 		this.applicationName = applicationName;
 	}
 
 	public void addAvailableApplicationVersion(String applicationVersion) {
 		applicationVersions.add(applicationVersion);
 	}
 
 	/* (non-Javadoc)
 	 * @see au.org.arcs.grid.sched.GridResource#getAvailableApplicationVersion()
 	 */
 	public List<String> getAvailableApplicationVersion() {
 		return applicationVersions;
 	}
 
 	/* (non-Javadoc)
 	 * @see au.org.arcs.grid.sched.GridResource#getFreeJobSlots()
 	 */
 	public int getFreeJobSlots() {
 		return freeJobSlots;
 	}
 
 	public void setFreeJobSlots(int freeJobSlots) {
 		this.freeJobSlots = freeJobSlots;
 	}
 
 	/* (non-Javadoc)
 	 * @see au.org.arcs.grid.sched.GridResource#getRunningJobs()
 	 */
 	public int getRunningJobs() {
 		return runningJobs;
 	}
 
 	public void setRunningJobs(int runningJobs) {
 		this.runningJobs = runningJobs;
 	}
 
 	/* (non-Javadoc)
 	 * @see au.org.arcs.grid.sched.GridResource#getWaitingJobs()
 	 */
 	public int getWaitingJobs() {
 		return waitingJobs;
 	}
 
 	public void setWaitingJobs(int waitingJobs) {
 		this.waitingJobs = waitingJobs;
 	}
 
 	/* (non-Javadoc)
 	 * @see au.org.arcs.grid.sched.GridResource#getTotalJobs()
 	 */
 	public int getTotalJobs() {
 		return totalJobs;
 	}
 
 	public void setTotalJobs(int totalJobs) {
 		this.totalJobs = totalJobs;
 	}
 
 	/* (non-Javadoc)
 	 * @see au.org.arcs.grid.sched.GridResource#getMainMemoryRAMSize()
 	 */
 	public int getMainMemoryRAMSize() {
 		return mainMemoryRAMSize;
 	}
 
 	public void setMainMemoryRAMSize(int mainMemoryRAMSize) {
 		this.mainMemoryRAMSize = mainMemoryRAMSize;
 	}
 
 	/* (non-Javadoc)
 	 * @see au.org.arcs.grid.sched.GridResource#getMainMemoryVirtualSize()
 	 */
 	public int getMainMemoryVirtualSize() {
 		return mainMemoryVirtualSize;
 	}
 
 	public void setMainMemoryVirtualSize(int mainMemoryVirtualSize) {
 		this.mainMemoryVirtualSize = mainMemoryVirtualSize;
 	}
 
 	/* (non-Javadoc)
 	 * @see au.org.arcs.grid.sched.GridResource#getSmpSize()
 	 */
 	public int getSmpSize() {
 		return smpSize;
 	}
 
 	public void setSmpSize(int smpSize) {
 		this.smpSize = smpSize;
 	}
 
 	/* (non-Javadoc)
 	 * @see au.org.arcs.grid.sched.GridResource#getContactString()
 	 */
 	public String getContactString() {
 		return contactString;
 	}
 
 	public void setContactString(String contactString) {
 		this.contactString = contactString;
 	}
 
 	/* (non-Javadoc)
 	 * @see au.org.arcs.grid.sched.GridResource#getJobManager()
 	 */
 	public String getJobManager() {
 		return jobManager;
 	}
 
 	public void setJobManager(String jobManager) {
 		this.jobManager = jobManager;
 	}
 	
 	public boolean isHostOnDesiredSite() {
 		return isHostOnDesiredSite;
 	}
 
 	public void setHostOnDesiredSite(boolean isHostOnDesiredSite) {
 		this.isHostOnDesiredSite = isHostOnDesiredSite;
 	}
 
 	/* (non-Javadoc)
 	 * @see au.org.arcs.grid.sched.GridResource#isDesiredSoftwareVersionInstalled()
 	 */
 	public boolean isDesiredSoftwareVersionInstalled() {
 		return isDesiredSoftwareVersionInstalled;
 	}
 
 	public void setDesiredSoftwareVersionInstalled(boolean isDesiredSoftwareVersionInstalled) {
 		this.isDesiredSoftwareVersionInstalled = isDesiredSoftwareVersionInstalled;
 	}
 	
 	public void setRankingAlgorithm(RankingAlgorithm rankingAlgorithm) {
 		this.rankingAlgorithm = rankingAlgorithm;
 	}
 	
 	/* (non-Javadoc)
 	 * @see au.org.arcs.grid.sched.GridResource#getRank()
 	 */
 	public int getRank() {
 		return rankingAlgorithm.getRank(this);
 	}
 
 	public int compareTo(GridResource o) {
		
		if ( this.getRank() < o.getRank() ) {
			return Integer.MAX_VALUE;
		} else if ( this.getRank() > o.getRank() ) {
			return Integer.MIN_VALUE;
		} else {
			return this.getQueueName().compareTo(o.getQueueName());
		}
 	}
 	
 	public String toString() {
 		return queueName + " (Ranking: " + getRank() + ")";
 	}
 
 	/* (non-Javadoc)
 	 * @see au.org.arcs.grid.sched.GridResource#getQueueName()
 	 */
 	public String getQueueName() {
 		return queueName;
 	}
 
 	public void setQueueName(String queueName) {
 		this.queueName = queueName;
 	}
 	
 	public boolean equals(Object o) {
 
 		if ( o == null ) {
 			return false;
 		}
 		
 		GridResource anotherResource = null;
 		
 		try {
 			anotherResource = (GridResource)o;	
 		} catch (Exception e) {
 			return false;
 		}
 		
 		if (queueName.equals(anotherResource.getQueueName()) &&
 				jobManager.equals(anotherResource.getJobManager()) &&
 				contactString.equals(anotherResource.getContactString())) {
 			return true;
 		}
 		return false;
 	}
 	
 	public int hashCode() {
 		return queueName.hashCode() + jobManager.hashCode() + contactString.hashCode() + 23 * getRank();
 	}
 
 	public Set<String> getAllExecutables() {
 
 		return allExecutables;
 	}
 	
 	public void setAllExecutables(Set<String> allExecutables) {
 		this.allExecutables = allExecutables;
 	}
 
 //	public int compareTo(GridResource o) {
 //		// TODO Auto-generated method stub
 //		return 0;
 //	}
 
 }
