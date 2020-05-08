 package au.org.arcs.grid.grisu.matchmaker;
 
 import it.infn.cnaf.forge.glueschema.spec.v12.r2.CEVOViewType;
 import it.infn.cnaf.forge.glueschema.spec.v12.r2.ComputingElementType;
 import it.infn.cnaf.forge.glueschema.spec.v12.r2.SiteType;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.TreeSet;
 
 import org.apache.commons.lang.StringUtils;
 import org.apache.log4j.Logger;
 import org.globus.common.CoGProperties;
 import org.vpac.grisu.control.info.GridResourceBackendImpl;
 import org.vpac.grisu.control.info.RankingAlgorithm;
 import org.vpac.grisu.control.info.SimpleResourceRankingAlgorithm;
 import org.w3c.dom.Document;
 
 import au.edu.sapac.grid.mds.QueryClient;
 import au.org.arcs.grid.sched.MatchMaker;
 import au.org.arcs.grid.sched.util.PluginLoader;
 import au.org.arcs.jcommons.constants.Constants;
 import au.org.arcs.jcommons.constants.JobSubmissionProperty;
 import au.org.arcs.jcommons.interfaces.GridResource;
 import au.org.arcs.jcommons.utils.JsdlHelpers;
 
 public class MatchMakerImpl implements MatchMaker {
 
 	static final Logger myLogger = Logger.getLogger(MatchMakerImpl.class
 			.getName());
 
 	public static Map<JobSubmissionProperty, String> generatePropertiesMap(
 			Document jsdl) {
 		// TODO: probably a good idea to use reflection to load this matchmaker
 		// implementation if we want to provide other ways of matchmaking
 
 		// TODO: need to confirm if this should be walltime-per-cpu
 		int wallTimeRequirement = JsdlHelpers.getWalltime(jsdl);
 
 		int totalCPURequirement = JsdlHelpers.getProcessorCount(jsdl);
 		String applicationName = JsdlHelpers.getApplicationName(jsdl);
 		String applicationVersion = JsdlHelpers.getApplicationVersion(jsdl);
 		/*
 		 * int wallTimeRequirement = 60; int totalCPURequirement = 1; String
 		 * applicationName = "MrBayes"; //String applicationVersion = "10.34";
 		 * String applicationVersion = "";
 		 */
 
 		// TODO: probably a preferred site in JSDL can be used as well in
 		// deciding
 		// where to submit the user's job
 
 		Map<JobSubmissionProperty, String> jobProperties = new HashMap<JobSubmissionProperty, String>();
 		jobProperties.put(JobSubmissionProperty.WALLTIME_IN_MINUTES,
 				new Integer(wallTimeRequirement).toString());
 		jobProperties.put(JobSubmissionProperty.NO_CPUS, new Integer(
 				totalCPURequirement).toString());
 		jobProperties.put(JobSubmissionProperty.APPLICATIONNAME,
 				applicationName);
 		jobProperties.put(JobSubmissionProperty.APPLICATIONVERSION,
 				applicationVersion);
 
 		return jobProperties;
 	}
 
 	public static void main(String[] args) {
 
 		CoGProperties.getDefault().setProperty(
 				CoGProperties.ENFORCE_SIGNING_POLICY, "false");
 
 		test5();
 	}
 
 	private static void test2() {
 
 		java.io.File docFile = new java.io.File(
 				"/home/gerson/.grisu/templates_available/mrbayessample.xml");
 		Document doc = null;
 		try {
 			javax.xml.parsers.DocumentBuilderFactory dbf = javax.xml.parsers.DocumentBuilderFactory
 					.newInstance();
 			javax.xml.parsers.DocumentBuilder db = dbf.newDocumentBuilder();
 			doc = db.parse(docFile);
 			List<GridResource> gridResources = new MatchMakerImpl(
 					System.getProperty("user.home")).findAvailableResources(
 					doc, "/ARCS/NGAdmin");
 			for (GridResource i : gridResources) {
 				System.out.println(i);
 			}
 		} catch (java.io.IOException e) {
 			System.out.println("Can't find the file");
 		} catch (Exception e) {
 			System.out.print("Problem parsing the file.");
 		}
 	}
 
 	private static void test3() {
 		System.out.println(PluginLoader
 				.getImplementationClass("RankingAlgorithm"));
 	}
 
 	private static void test5() {
 
 		try {
 			Map<JobSubmissionProperty, String> jobProperties = new HashMap<JobSubmissionProperty, String>();
 			jobProperties.put(JobSubmissionProperty.APPLICATIONNAME,
 					"ESyS-Particle");
 
 			List<GridResource> gridResources = new MatchMakerImpl(
 					System.getProperty("user.home")).findAvailableResources(
 					jobProperties, "/ARCS/AuScope");
 			for (GridResource i : gridResources) {
 				System.out.println(i);
 			}
 		} catch (Exception e) {
 			System.out.print("Problem parsing the file.");
 		}
 
 	}
 
 	private static void test4() {
 
 		try {
 			Map<JobSubmissionProperty, String> jobProperties = new HashMap<JobSubmissionProperty, String>();
 			jobProperties.put(JobSubmissionProperty.APPLICATIONNAME, "java");
 
 			List<GridResource> gridResources = new MatchMakerImpl(
 					System.getProperty("user.home")).findAvailableResources(
 					jobProperties, "/ARCS/NGAdmin");
 			for (GridResource i : gridResources) {
 				System.out.println(i);
 			}
 		} catch (Exception e) {
 			System.out.print("Problem parsing the file.");
 		}
 	}
 
 	private QueryClient mdsClient = null;
 
 	private RankingAlgorithm rankingAlgorithm = null;
 
 	public MatchMakerImpl(RankingAlgorithm rankingAlgorithm,
 			String mdsCacheDirectory) {
 		this.rankingAlgorithm = rankingAlgorithm;
 		mdsClient = new QueryClient(mdsCacheDirectory);
 	}
 
 	// TODO: get PluginLoader working with MatchMaker and RankingAlgorithm
 	public MatchMakerImpl(String mdsCacheDirectory) {
 		this(new SimpleResourceRankingAlgorithm(), mdsCacheDirectory);
 	}
 
 	public List<GridResource> findAllResources(Document jsdl, String fqan) {
 
 		return findMatchingResources(generatePropertiesMap(jsdl), fqan, false);
 
 	}
 
 	public List<GridResource> findAllResources(
 			Map<JobSubmissionProperty, String> jobProperties, String fqan) {
 
 		return findMatchingResources(jobProperties, fqan, false);
 
 	}
 
 	public List<GridResource> findAvailableResources(Document jsdl, String fqan) {
 
 		return findMatchingResources(generatePropertiesMap(jsdl), fqan, true);
 
 	}
 
 	public List<GridResource> findAvailableResources(
 			Map<JobSubmissionProperty, String> jobProperties, String fqan) {
 
 		return findMatchingResources(jobProperties, fqan, true);
 
 	}
 
 	public List<GridResource> findMatchingResources(
 			Map<JobSubmissionProperty, String> jobProperties, String fqan,
 			boolean excludeResourcesWithLessCPUslotsFreeThanRequested) {
 
 		int wallTimeRequirement = -1;
 		try {
 			wallTimeRequirement = Integer.parseInt(jobProperties
 					.get(JobSubmissionProperty.WALLTIME_IN_MINUTES));
 		} catch (Exception e) {
 			myLogger.warn("Couldn't find walltime requirement for job.");
 		}
 
 		int totalCPURequirement = -1;
 		try {
 			totalCPURequirement = Integer.parseInt(jobProperties
 					.get(JobSubmissionProperty.NO_CPUS));
 		} catch (Exception e) {
 			myLogger.warn("Couldn't find cpu requirement for job.");
 		}
 
 		String applicationName = jobProperties
 				.get(JobSubmissionProperty.APPLICATIONNAME);
 		String applicationVersion = jobProperties
 				.get(JobSubmissionProperty.APPLICATIONVERSION);
 
 		// if (
 		// ServiceInterface.NO_VERSION_INDICATOR_STRING.equals(applicationVersion)
 		// ) {
 		// applicationVersion = null;
 		// }
 
 		// find all the CEs that have software/version installed and permits
 		// VO fqan to run jobs on them
 		ComputingElementType[] ceTypes = mdsClient
 				.getComputingElementsForApplicationAndVO(applicationName,
 						applicationVersion, fqan);
 
 		List<GridResource> gridResources = new ArrayList<GridResource>();
 		GridResourceBackendImpl gridResource = null;
 		SiteType site = null;
 
 		for (ComputingElementType ceType : ceTypes) {
 			int freeJobSlots = 0;
 
 			boolean foundVOView = false;
 			CEVOViewType[] voViews = ceType.getVOViewArray();
 
 			CEVOViewType voViewTmp = null;
 			for (CEVOViewType voView : voViews) {
 				String[] ruleArray = voView.getACL().getRuleArray();
 				for (String element : ruleArray) {
 					if (element.equals(fqan)) {

 						// now that we found the voview, get the things we need
 						// from it like freejobslots
 						try {
 							freeJobSlots = voView.getFreeJobSlots().intValue();
 						} catch (Exception e) {
 							myLogger.debug("Could not retrieve freejob slots for voview: "
 									+ voView.getDefaultSE());
 							freeJobSlots = 0;
 						}

 						voViewTmp = voView;
 						foundVOView = true;
 						break;
 					}
 				}
 				if (foundVOView) {
 					break;
 				}
 			}
 
 			if (excludeResourcesWithLessCPUslotsFreeThanRequested) {
 				// if the resource that we found doesn't have the minimum
 				// cpu requirements, check the next CE
 				int wall = ceType.getMaxWallClockTime().intValue();
 				if (!foundVOView
 						|| (freeJobSlots < totalCPURequirement)
 						|| (ceType.getMaxWallClockTime().intValue() < wallTimeRequirement)) {
 
 					continue;
 
 				}
 			}
 
 			// else.. grab the other details we need from the CE, SubCluster,
 			// and
 			// Site elements
 
 			// but first just fill in the known details of the GridResource
 			gridResource = new GridResourceBackendImpl(rankingAlgorithm);
 			gridResource.setApplicationName(applicationName);
 
 			// if software version is specified...
 			if (StringUtils.isNotBlank(applicationVersion)
 					&& !Constants.NO_VERSION_INDICATOR_STRING
 							.equals(applicationVersion)) {
 				gridResource.setDesiredSoftwareVersionInstalled(true);
 			}
 
 			// get the uid of this resource and find the following...
 
 			// need versions of software
 			String ceUID = ceType.getUniqueID();
 			String[] appVersions = mdsClient.getVersionsOfCodeForCE(ceUID,
 					applicationName);
 
 			// System.out.println("--------------------------------");
 			// System.out.println("appversions for: "+ceUID+" "+applicationName);
 			// for (String exe : appVersions ) {
 			// System.out.println(exe);
 			// }
 			// System.out.println("--------------------------------");
 
 			for (String appVersion : appVersions) {
 				gridResource.addAvailableApplicationVersion(appVersion);
 			}
 
 			// need contact string, jobmanager from CE
 			// assume that on ARCS grid, there will be only one contact string
 			// array
 			gridResource.setContactString(ceType.getContactStringArray(0));
 			gridResource.setJobManager(ceType.getJobManager());
 			gridResource.setQueueName(ceType.getName());
 
 			// now get the freejobslots, runningjobs, waitingjobs, totaljobs
 			// from
 			// VOView
 			int tempFreeJobSlots = -1;
 			try {
 				tempFreeJobSlots = voViewTmp.getFreeJobSlots().intValue();
 			} catch (Exception e) {
 				myLogger.warn("Could not retrieve freejob slots for voview: "
 						+ voViewTmp.getDefaultSE());
 				tempFreeJobSlots = 0;
 			}
 			gridResource.setFreeJobSlots(tempFreeJobSlots);
 			gridResource.setRunningJobs(voViewTmp.getRunningJobs().intValue());
 			gridResource.setWaitingJobs(voViewTmp.getWaitingJobs().intValue());
 			gridResource.setTotalJobs(voViewTmp.getTotalJobs().intValue());
 
 			// need name, lat and long from Site
 			site = mdsClient.getSiteForCE(ceUID);
 			gridResource.setSiteName(site.getName());
 			gridResource.setSiteLatitude(site.getLatitude());
 			gridResource.setSiteLongitude(site.getLongitude());
 
 			Set<String> executables = new TreeSet<String>();
 			for (String version : appVersions) {
 				String[] executablesTemp = mdsClient
 						.getExeNameOfCodeAtQueueAtSite(site.getName(),
 								ceType.getName(), applicationName, version);
 				executables.addAll(Arrays.asList(executablesTemp));
 			}
 			;
 			gridResource.setAllExecutables(executables);
 
 			// for the meantime, we won't worry about the following resource
 			// attributes
 			// memory from SubCluster
 			// OS from SubCluster
 			gridResources.add(gridResource);
 		}
 
 		// sort the list first based on their ranks before returning..
 		Collections.sort(gridResources);
 		return gridResources;
 	}
 
 	public void setRankingAlgorithm(RankingAlgorithm rankingAlgorithm) {
 		this.rankingAlgorithm = rankingAlgorithm;
 	}
 
 	// private static void test1() {
 	// List<GridResource> gridResources = new
 	// MatchMakerImpl().findMatchingResources(null, "/ARCS/NGAdmin");
 	// for (GridResource i : gridResources) {
 	// System.out.println(i);
 	// }
 	// }
 
 }
