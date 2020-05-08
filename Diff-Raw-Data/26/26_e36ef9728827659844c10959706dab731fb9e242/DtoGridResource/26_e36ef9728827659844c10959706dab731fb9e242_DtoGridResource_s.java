 package org.vpac.grisu.model.dto;
 
 import java.util.List;
 import java.util.Set;
 
 import javax.xml.bind.annotation.XmlAccessType;
 import javax.xml.bind.annotation.XmlAccessorType;
 import javax.xml.bind.annotation.XmlAttribute;
 import javax.xml.bind.annotation.XmlElement;
 import javax.xml.bind.annotation.XmlRootElement;
 
 import au.org.arcs.jcommons.interfaces.GridResource;
 
 /**
  * A wrapper object that is created when the grid is queried for resources that are able to 
  * run a certain type of job.
  * 
  * This contains a snapshot of values for a single grid resource that can be of importance when deciding where to run a job.
  * 
  * @author Markus Binsteiner
  *
  */
 @XmlRootElement(name="gridresource")
 @XmlAccessorType(XmlAccessType.FIELD)
 public class DtoGridResource implements GridResource {
 
 	/**
 	 * All the a executables that are available for the selected application on this grid resource.
 	 */
 	@XmlElement(name="executable")
 	private Set<String> allExecutables;
 	/**
 	 * The application name of the job that was used to find this grid resource.
 	 */
 	@XmlAttribute(name="applicationName")
 	private String applicationName;
 	/**
 	 * A list of versions of the application that are available on this grid resource.
 	 */
 	@XmlElement(name="version")
 	private List<String> availableApplicationVersion;
 	/**
 	 * The contact string for this grid resource.
 	 */
 	@XmlElement(name="contactstring")
 	private String contactString;
 	/**
 	 * The number of free job slots for the VO that was used to find this grid resource.
 	 */
 	@XmlElement(name="freejobslots")
 	private int freeJobSlots;
 	/**
 	 * The job manager that is used on this grid resource.
 	 */
 	@XmlElement(name="jobmanager")
 	private String jobManager;
 	/**
 	 * The main memory ram size that is available on this grid resource.
 	 */
 	@XmlElement(name="mainmemoryramsize")
 	private int mainMemoryRAMSize;
 	/**
 	 * The main memory virtual size that is available on this grid resource.
 	 */
 	@XmlElement(name="mainmemoryvirtualsize")
 	private int mainMemoryVirtualSize;
 	/**
 	 * The queue name for this grid resource.
 	 */
 	@XmlAttribute(name="queuename")
 	private String queueName;
 	/**
 	 * The rank for this grid resource. This is a value that was calculated by the ranking algorithm
 	 * that is used in the matchmaker library.
 	 */
 	@XmlElement(name="rank")
 	private int rank;
 	/**
 	 * The total of running jobs for the VO that was used to find this grid resource.
 	 */
 	@XmlElement(name="runningjobs")
 	private int runningJobs;
 	/**
 	 * The latitude for the site where this grid resource is located.
 	 */
 	@XmlAttribute(name="latitude")
 	private double siteLatitude;
 	/**
 	 * The longitude for the site where this grid resource is located.
 	 */
 	@XmlAttribute(name="longitude")
 	private double siteLongitude;
 	/**
 	 * The name of the site where this grid resource is located.
 	 */
 	@XmlAttribute(name="sitename")
 	private String siteName;
 	/**
 	 * The smp size on this grid resource.
 	 */
 	@XmlElement(name="smpsize")
 	private int smpSize;
 	/**
 	 * The total number of jobs on this grid resource for the VO that was used to find this grid resource.
 	 */
 	@XmlElement(name="totaljobs")
 	private int totalJobs;
 	/**
 	 * The number of jobs that wait in the queue on this grid resource for the VO that was used to find this grid resource.
 	 */
 	@XmlElement(name="waitingjobs")
 	private int waitingJobs;
 	/**
 	 * Whether the version that was specified in the initial query is available on this resource.
 	 * 
 	 *  Don't use that, I think it doesn't work. Use the list of versions instead.
 	 */
 	@XmlAttribute(name="isdesiredversioninstalled")
 	private boolean isDesiredVersionInstalled;
 	
 	public DtoGridResource() {
 		
 	}
 	
 	public DtoGridResource(GridResource gr) {
 		this.allExecutables = gr.getAllExecutables();
 		this.applicationName = gr.getApplicationName();
 		this.availableApplicationVersion = gr.getAvailableApplicationVersion();
 		this.contactString = gr.getContactString();
 		this.freeJobSlots = gr.getFreeJobSlots();
 		this.jobManager = gr.getJobManager();
 		this.mainMemoryRAMSize = gr.getMainMemoryRAMSize();
 		this.mainMemoryVirtualSize = gr.getMainMemoryVirtualSize();
 		this.queueName = gr.getQueueName();
 		this.rank = gr.getRank();
 		this.runningJobs = gr.getRunningJobs();
 		this.siteLatitude = gr.getSiteLatitude();
 		this.siteLongitude = gr.getSiteLongitude();
 		this.siteName = gr.getSiteName();
 		this.smpSize = gr.getSmpSize();
 		this.totalJobs = gr.getTotalJobs();
 		this.waitingJobs = gr.getWaitingJobs();
 		this.isDesiredVersionInstalled = gr.isDesiredSoftwareVersionInstalled();
 	}
 	
 	public Set<String> getAllExecutables() {
		return this.getAllExecutables();
 	}
 
 	public String getApplicationName() {
		return this.getApplicationName();
 	}
 
 	public List<String> getAvailableApplicationVersion() {
		return this.getAvailableApplicationVersion();
 	}
 
 	public String getContactString() {
		return this.getContactString();
 	}
 
 	public int getFreeJobSlots() {
		return this.getFreeJobSlots();
 	}
 
 	public String getJobManager() {
 		return this.jobManager;
 	}
 
 	public int getMainMemoryRAMSize() {
 		return this.mainMemoryRAMSize;
 	}
 
 	public int getMainMemoryVirtualSize() {
 		return this.mainMemoryVirtualSize;
 	}
 
 	public String getQueueName() {
 		return this.queueName;
 	}
 
 	public int getRank() {
 		return this.rank;
 	}
 
 	public int getRunningJobs() {
 		return this.runningJobs;
 	}
 
 	public double getSiteLatitude() {
 		return this.siteLatitude;
 	}
 
 	public double getSiteLongitude() {
 		return this.siteLongitude;
 	}
 
 	public String getSiteName() {
 		return this.siteName;
 	}
 
 	public int getSmpSize() {
 		return this.smpSize;
 	}
 
 	public int getTotalJobs() {
 		return this.totalJobs;
 	}
 
 	public int getWaitingJobs() {
 		return this.waitingJobs;
 	}
 
 	public boolean isDesiredSoftwareVersionInstalled() {
 		return isDesiredVersionInstalled;
 	}
 
 	public int compareTo(Object arg0) {
 		GridResource anotherResource = (GridResource)arg0;
 		return this.getRank() < anotherResource.getRank() ? 1 :
 			(this.getRank() == anotherResource.getRank() ? 0 : -1);
 	}
 
 }
