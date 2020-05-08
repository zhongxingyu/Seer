 package org.vpac.grisu.control.info;
 
 import grisu.control.info.SimpleResourceRankingAlgorithm;
 import grisu.jcommons.constants.JobSubmissionProperty;
 import grisu.jcommons.interfaces.GridResource;
 import grisu.jcommons.interfaces.MatchMaker;
 import grisu.jcommons.interfaces.RankingAlgorithm;
 import grisu.jcommons.utils.JsdlHelpers;
 
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.log4j.Logger;
 import org.bestgrid.mds.SQLQueryClient;
 import org.w3c.dom.Document;
 
 import au.org.arcs.grid.sched.util.PluginLoader;
 
 public class SqlMDSMatchMaker implements MatchMaker {
 
 	static final Logger myLogger = Logger.getLogger(SqlMDSMatchMaker.class
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
 
 	// public static void main(String[] args) {
 	// test4();
 	// }
 
 	// private static void test2() {
 	//
 	// java.io.File docFile = new java.io.File(
 	// "/home/gerson/.grisu/templates_available/mrbayessample.xml");
 	// Document doc = null;
 	// try {
 	// javax.xml.parsers.DocumentBuilderFactory dbf =
 	// javax.xml.parsers.DocumentBuilderFactory
 	// .newInstance();
 	// javax.xml.parsers.DocumentBuilder db = dbf.newDocumentBuilder();
 	// doc = db.parse(docFile);
 	// List<GridResource> gridResources = new SqlMDSMatchMaker(
 	// System.getProperty("user.home")).findAvailableResources(
 	// doc, "/ARCS/NGAdmin");
 	// for (GridResource i : gridResources) {
 	// System.out.println(i);
 	// }
 	// } catch (java.io.IOException e) {
 	// System.out.println("Can't find the file");
 	// } catch (Exception e) {
 	// System.out.print("Problem parsing the file.");
 	// }
 	// }
 
 	private static void test3() {
 		System.out.println(PluginLoader
 				.getImplementationClass("RankingAlgorithm"));
 	}
 
 	// private static void test4() {
 	//
 	// try {
 	// Map<JobSubmissionProperty, String> jobProperties = new
 	// HashMap<JobSubmissionProperty, String>();
 	// jobProperties.put(JobSubmissionProperty.APPLICATIONNAME, "java");
 	//
 	// List<GridResource> gridResources = new SqlMDSMatchMaker(
 	// System.getProperty("user.home")).findAvailableResources(
 	// jobProperties, "/ARCS/NGAdmin");
 	// for (GridResource i : gridResources) {
 	// System.out.println(i);
 	// }
 	// } catch (Exception e) {
 	// System.out.print("Problem parsing the file.");
 	// }
 	// }
 
 	private SQLQueryClient mdsClient = null;
 
 	private RankingAlgorithm rankingAlgorithm = null;
 
 	public SqlMDSMatchMaker(RankingAlgorithm rankingAlgorithm,
 			Map<String, String> configuration) {
 		this.rankingAlgorithm = rankingAlgorithm;
 		String databaseUrl = configuration.get("databaseUrl");
 		String databaseUser = configuration.get("user");
 		String databasePassword = configuration.get("password");
 		mdsClient = new SQLQueryClient(databaseUrl, databaseUser,
 				databasePassword);
 	}
 
	// TODO: get PluginLoader working with MatchMaker and RankingAlgorithm
	// public SqlMDSMatchMaker(String mdsCacheDirectory) {
	// this(new SimpleResourceRankingAlgorithm(), mdsCacheDirectory);
	// }

	public SqlMDSMatchMaker(Map<String, String> params) {
		this(new SimpleResourceRankingAlgorithm(), params);
	}

 	public List<GridResource> findAllResources(Document jsdl, String fqan) {
 
		return findMatchingResources(generatePropertiesMap(jsdl), fqan, true);
 
 	}
 
 	public List<GridResource> findAllResources(
 			Map<JobSubmissionProperty, String> jobProperties, String fqan) {
 
		return findMatchingResources(jobProperties, fqan, true);
 
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
 		return mdsClient.findAllResourcesM(jobProperties, fqan,
 				excludeResourcesWithLessCPUslotsFreeThanRequested);
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
