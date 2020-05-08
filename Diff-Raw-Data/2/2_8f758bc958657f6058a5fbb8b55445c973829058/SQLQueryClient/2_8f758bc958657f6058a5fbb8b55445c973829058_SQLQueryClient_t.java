 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package org.bestgrid.mds;
 
 import grisu.control.info.GridResourceBackendImpl;
 import grisu.jcommons.constants.Constants;
 import grisu.jcommons.constants.JobSubmissionProperty;
 import grisu.jcommons.interfaces.GridInfoInterface;
 import grisu.jcommons.interfaces.GridResource;
 
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import java.util.regex.*;
 
 import org.apache.commons.lang.StringUtils;
 import org.apache.log4j.Logger;
 
 import au.edu.sapac.grid.mds.QueryClient;
 
 /**
  * 
  * @author yhal003
  */
 public class SQLQueryClient implements GridInfoInterface {
 	
 	public final String VOLATILE="volatile";
 
 	static final Logger myLogger = Logger.getLogger(SQLQueryClient.class
 			.getName());
 
 	public static void main(String[] args) throws ClassNotFoundException,
 	InstantiationException, IllegalAccessException {
 		Connection con = null;
 		try {
 
 			Class.forName("com.mysql.jdbc.Driver").newInstance();
 			con = DriverManager.getConnection(
 					"jdbc:mysql://mysql-bg.ceres.auckland.ac.nz/mds_test",
 					"grisu_read", "password");
 
 			QueryClient qClient = new QueryClient("/tmp");
 			SQLQueryClient sClient = new SQLQueryClient(con);
 
 			HashMap<JobSubmissionProperty, String> props = new HashMap<JobSubmissionProperty, String>();
 			props.put(JobSubmissionProperty.APPLICATIONNAME, Constants.GENERIC_APPLICATION_NAME);
 			props.put(JobSubmissionProperty.APPLICATIONVERSION, Constants.NO_VERSION_INDICATOR_STRING);
 			props.put(JobSubmissionProperty.WALLTIME_IN_MINUTES, "4000");
 			System.out.println("Check R...");
 			List<GridResource> resources = sClient.findAllResourcesM(props,
 					"/ARCS/Monash", true);
 			for (GridResource r : resources) {
 				System.out.println("R site: " + r.getSiteName());
 			}
 
 			printResults(
 					qClient.getApplicationNamesThatProvideExecutable("javac"),
 			"Query Client method 1");
 			printResults(
 					sClient.getApplicationNamesThatProvideExecutable("javac"),
 			"SQL Client method 1");
 
 			// appears broken
 			// printResults(qClient.getClusterNamesAtSite("canterbury.ac.nz"),"Query Client method 2");
 			printResults(sClient.getClusterNamesAtSite("Canterbury"),
 			"SQL Client method 2");
 
 			// broken too, maybe...
 			// printResults(qClient.getClustersForCodeAtSite("canterbury.ac.nz","Java","1.4.2"),"Query Client method 3");
 			printResults(sClient.getClustersForCodeAtSite("Canterbury", "Java",
 			"1.4.2"), "SQL Client method 3");
 
 			printResults(qClient.getCodesAtSite("Canterbury"),
 			"Query Client method  4");
 			printResults(sClient.getCodesAtSite("Canterbury"),
 			"SQL Client method 4");
 
 			printResults(qClient.getCodesOnGrid(), "Query Client method  5");
 			printResults(sClient.getCodesOnGrid(), "SQL Client method 5");
 
 			printResults(qClient.getContactStringOfQueueAtSite("eRSA",
 			"hydra@hydra"), "Query Client method  6");
 			printResults(sClient.getContactStringOfQueueAtSite("eRSA",
 			"hydra@hydra"), "SQL Client method 6");
 
 			printResults(qClient.getDataDir("Auckland", "ng2.auckland.ac.nz",
 			"/ARCS/BeSTGRID"), "Query Client method  7");
 			printResults(sClient.getDataDir("Auckland", "ng2.auckland.ac.nz",
 			"/ARCS/BeSTGRID"), "SQL Client method 7");
 
 			printResults(
 					new String[] { qClient.getDefaultStorageElementForQueueAtSite(
 							"Canterbury", "grid_aix") },
 			"Query Client method  8");
 			printResults(
 					new String[] { sClient.getDefaultStorageElementForQueueAtSite(
 							"Canterbury", "grid_aix") }, "SQL Client method  8");
 
 			printResults(
 					qClient.getExeNameOfCodeAtSite("Auckland", "Java", "1.6"),
 			"Query Client method  9");
 			printResults(
 					sClient.getExeNameOfCodeAtSite("Auckland", "Java", "1.6"),
 			"SQL Client method  9");
 
 			printResults(qClient.getExeNameOfCodeForSubmissionLocation(
 					"route@er171.ceres.auckland.ac.nz:ng2.auckland.ac.nz",
 					"Java", "1.6"), "Query Client method  10");
 			printResults(sClient.getExeNameOfCodeForSubmissionLocation(
 					"route@er171.ceres.auckland.ac.nz:ng2.auckland.ac.nz",
 					"Java", "1.6"), "SQL Client method  10");
 
 			printResults(qClient.getGridFTPServersAtSite("Auckland"),
 			"Query Client method  11");
 			printResults(sClient.getGridFTPServersAtSite("Auckland"),
 			"SQL Client method  11");
 
 			printResults(qClient.getGridFTPServersForQueueAtSite("Auckland",
 			"route@er171.ceres.auckland.ac.nz"),
 			"Query Client method  12");
 			printResults(sClient.getGridFTPServersForQueueAtSite("Auckland",
 			"route@er171.ceres.auckland.ac.nz"),
 			"SQL Client method  12");
 
 			printResults(qClient.getGridFTPServersForStorageElementAtSite(
 					"Auckland", "ngdata.ceres.auckland.ac.nz"),
 			"Query Client method  13");
 			printResults(sClient.getGridFTPServersForStorageElementAtSite(
 					"Auckland", "ngdata.ceres.auckland.ac.nz"),
 			"SQL Client method  13");
 
 			// also broken
 			// printResults(qClient.getGridFTPServersOnGrid(),"Query Client method  14");
 			printResults(sClient.getGridFTPServersOnGrid(),
 			"SQL Client method  14");
 
 			printResults(new String[] { qClient.getJobManagerOfQueueAtSite(
 					"Auckland", "route@er171.ceres.auckland.ac.nz") },
 			"Query Client method  15");
 			printResults(new String[] { sClient.getJobManagerOfQueueAtSite(
 					"Auckland", "route@er171.ceres.auckland.ac.nz") },
 			"SQL Client method  15");
 
 			// not sure what we are supposed to do here
 			// System.out.println(qClient.getJobTypeOfCodeAtSite("Auckland",
 			// "Java", "1.6"));
 
 			printResults(new String[] { qClient.getLRMSTypeOfQueueAtSite(
 					"Auckland", "route@er171.ceres.auckland.ac.nz") },
 			"Query Client method  17");
 			printResults(new String[] { sClient.getLRMSTypeOfQueueAtSite(
 					"Auckland", "route@er171.ceres.auckland.ac.nz") },
 			"SQL Client method  17");
 
 			printResults(
 					new String[] { qClient
 							.getModuleNameOfCodeForSubmissionLocation(
 									"route@er171.ceres.auckland.ac.nz:ng2.auckland.ac.nz",
 									"Java", "1.6") }, "Query Client method  18");
 			printResults(
 					new String[] { sClient
 							.getModuleNameOfCodeForSubmissionLocation(
 									"route@er171.ceres.auckland.ac.nz:ng2.auckland.ac.nz",
 									"Java", "1.6") }, "SQL Client method  18");
 
 			printResults(qClient.getQueueNamesAtSite("Canterbury"),
 			"Query Client method  19");
 			printResults(sClient.getQueueNamesAtSite("Canterbury"),
 			"SQL Client method  19");
 
 			printResults(
 					qClient.getQueueNamesAtSite("Canterbury", "/ARCS/NGAdmin"),
 			"Query Client method  20");
 			printResults(
 					sClient.getQueueNamesAtSite("Canterbury", "/ARCS/NGAdmin"),
 			"SQL Client method  20");
 
 			// broken too!
 			// printResults(qClient.getQueueNamesForClusterAtSite("Canterbury","ng1.canterbury.ac.nz"),"Query Client method  21");
 			printResults(sClient.getQueueNamesForClusterAtSite("Canterbury",
 			"ng1.canterbury.ac.nz"), "SQL Client method  21");
 
 			printResults(
 					qClient.getQueueNamesForCodeAtSite("Canterbury", "Java"),
 			"Query Client method  22");
 			printResults(
 					sClient.getQueueNamesForCodeAtSite("Canterbury", "Java"),
 			"SQL Client method  22");
 
 			printResults(qClient.getQueueNamesForCodeAtSite("Canterbury",
 					"Java", "1.4.2"), "Query Client method  23");
 			printResults(sClient.getQueueNamesForCodeAtSite("Canterbury",
 					"Java", "1.4.2"), "SQL Client method  23");
 
 			printResults(
 					new String[] { qClient.getSiteForHost("cognac.ivec.org") },
 			"Query Client method  24");
 			printResults(
 					new String[] { sClient.getSiteForHost("cognac.ivec.org") },
 			"SQL Client method  24");
 
 			printResults(qClient.getSitesOnGrid(), "Query Client method  25");
 			printResults(sClient.getSitesOnGrid(), "SQL Client method  25");
 
 			printResults(qClient.getSitesWithAVersionOfACode("Java", "1.6"),
 			"Query Client method  26");
 			printResults(sClient.getSitesWithAVersionOfACode("Java", "1.6"),
 			"SQL Client method  26");
 
 			printResults(qClient.getSitesWithCode("Java"),
 			"Query Client method  27");
 			printResults(sClient.getSitesWithCode("Java"),
 			"SQL Client method  27");
 
 			printResults(
 					new String[] { qClient
 							.getStorageElementForGridFTPServer("gsiftp://ng2.esscc.uq.edu.au:2811") },
 			"Query Client method  28");
 			printResults(
 					new String[] { sClient
 							.getStorageElementForGridFTPServer("gsiftp://ng2.esscc.uq.edu.au:2811") },
 			"SQL Client method  28");
 
 			printResults(qClient.getStorageElementsForSite("HPSC"),
 			"Query Client method  29");
 			printResults(sClient.getStorageElementsForSite("HPSC"),
 			"SQL Client method  29");
 
 			printResults(qClient.getVersionsOfCodeAtSite("Auckland", "Java"),
 			"Query Client method  30");
 			printResults(sClient.getVersionsOfCodeAtSite("Auckland", "Java"),
 			"SQL Client method  30");
 
 			printResults(
 					qClient.getVersionsOfCodeForQueueAndContactString(
 							"grid_aix",
 							"https://ng2hpc.canterbury.ac.nz:8443/wsrf/services/ManagedJobFactoryService",
 					"Java"), "Query Client method  31");
 			printResults(
 					sClient.getVersionsOfCodeForQueueAndContactString(
 							"grid_aix",
 							"https://ng2hpc.canterbury.ac.nz:8443/wsrf/services/ManagedJobFactoryService",
 					"Java"), "SQL Client method  31");
 
 			printResults(qClient.getVersionsOfCodeOnGrid("beast"),
 			"Query Client method  32");
 			printResults(sClient.getVersionsOfCodeOnGrid("beast"),
 			"SQL Client method  32");
 
 			printResults(
 					new String[] { ""
 							+ qClient.isParallelAvailForCodeForSubmissionLocation(
 									"grid_aix:ng2hpc.canterbury.ac.nz",
 									"MrBayes", "3.1.2") },
 			"Query Client method  33");
 			printResults(
 					new String[] { ""
 							+ sClient.isParallelAvailForCodeForSubmissionLocation(
 									"grid_aix:ng2hpc.canterbury.ac.nz",
 									"MrBayes", "3.1.2") },
 			"SQL Client method  33");
 
 			// and this one is broken as well so it seems...
 			printResults(
 					new String[] { ""
 							+ qClient.isSerialAvailForCodeForSubmissionLocation(
 									"grid_aix:ng2hpc.canterbury.ac.nz",
 									"MrBayes", "3.1.2") },
 			"Query Client method  34");
 			printResults(
 					new String[] { ""
 							+ sClient.isSerialAvailForCodeForSubmissionLocation(
 									"grid_aix:ng2hpc.canterbury.ac.nz",
 									"MrBayes", "3.1.2") },
 			"SQL Client method  34");
 
 			printResults(qClient.getSitesForVO("/ARCS/BeSTGRID"),
 			"Query Client method 35");
 			printResults(sClient.getSitesForVO("/ARCS/BeSTGRID"),
 			"SQL Client method 35");
 
 			Map<String, String[]> data = sClient
 			.calculateDataLocationsForVO("/ARCS/BeSTGRID");
 			for (String key : data.keySet()) {
 				System.out.println("key is : " + key);
 				String[] values = data.get(key);
 				for (String value : values) {
 					System.out.println(value);
 				}
 			}
 
 			printResults(qClient.getGridFTPServersForQueueAtSite("Canterbury",
 			"gt5test"), "qclient getGridFTPServersForQueueAtSite");
 			printResults(sClient.getGridFTPServersForQueueAtSite("Canterbury",
 			"gt5test"), "sclient getGridFTPServersForQueueAtSite");
 			
 			printResults(sClient.isVolatile("gsiftp://ng2.auckland.ac.nz:2811", "/home/grid-bestgrid", "/ARCS/BeSTGRID"), 
 					"qClient isVolatile gsiftp://ng2.auckland.ac.nz:2811 /home/grid-bestgrid /ARCS/BeSTGRID");
 			
 			printResults(sClient.isVolatile("gsiftp://ng2.auckland.ac.nz:2811", "/home/yhal003", "/ARCS/BeSTGRID/Local"), 
 			"qClient isVolatile gsiftp://ng2.auckland.ac.nz:2811 /home/yhal003 /ARCS/BeSTGRID/Local");
 			
 			printResults(sClient.isVolatile("gsiftp://ng2.auckland.ac.nz:2811", "/home/grid-sbs", "/ARCS/BeSTGRID/Drug_discovery/SBS-Structural_Biology"), 
 			"qClient isVolatile gsiftp://ng2.auckland.ac.nz:2811 /home/grid-sbs /ARCS/BeSTGRID/Drug_discovery/SBS-Structural_Biology");
 			
 			printResults(sClient.isVolatile("gsiftp://ng2.auckland.ac.nz:2811", "/home/grid-acsrc", "/ARCS/BeSTGRID/Drug_discovery/ACSRC"), 
 			"qClient isVolatile gsiftp://ng2.auckland.ac.nz:2811 /home/grid-acsrc /ARCS/BeSTGRID/Drug_discovery/ACSRC");
 			
 			
 			Map<JobSubmissionProperty,String> jobProperties = new HashMap<JobSubmissionProperty,String>();
 			jobProperties.put(JobSubmissionProperty.APPLICATIONNAME,"mech-uoa");
 			//jobProperties.put(JobSubmissionProperty.APPLICATIONVERSION, "1.5");
 			//jobProperties.put(JobSubmissionProperty.NO_CPUS, "1");
 			resources = sClient.findAllResourcesM(jobProperties, "/nz/NeSI",false);
 			for (GridResource r: resources){
 				System.out.println(r.getQueueName());
 			}
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 	}
 
 	private static void printResults(String[] results, String label) {
 		System.out.println(label);
 		for (String result : results) {
 			System.out.println(result);
 		}
 	}
 	
 	private static void printResults(boolean result, String label){
 		printResults( new String[] {new Boolean(result).toString()},label);
 	}
 
 	private Connection con;
 
 	private String databaseUrl, user, password;
 
 	public SQLQueryClient(Connection con) {
 		this.con = con;
 	}
 
 	public SQLQueryClient(String databaseUrl, String user, String password) {
 		try {
 			Class.forName("com.mysql.jdbc.Driver").newInstance();
 			this.databaseUrl = databaseUrl;
 			this.user = user;
 			this.password = password;
 			this.con = DriverManager.getConnection(databaseUrl, user, password);
 		} catch (Exception ex) {
 			throw new RuntimeException(ex);
 		}
 	}
 
 	public Map<String, String[]> calculateDataLocationsForVO(String fqan) {
 		// getDataDir(String site, String storageElement, String FQAN)
 		HashMap<String, String[]> map = new HashMap<String, String[]>();
 		String query = "select sa.path,ap.endpoint from StorageElements se,StorageAreas sa,AccessProtocols ap, storageAreaACLs sacls "
 			+ " WHERE ap.storageElement_id = se.id AND sa.storageElement_id = se.id AND sacls.storageArea_id = sa.id and sacls.vo =?";
 		PreparedStatement s = getStatement(query);
 		setString(s, 1, fqan);
 		String[][] results = runQuery(s, "path", "endpoint");
 		String[] paths = results[0];
 		String[] endpoints = results[1];
 		for (int i = 0; i < endpoints.length; i++) {
 			map.put(endpoints[i], new String[] { paths[i] });
 		}
 		return map;
 	}
 
 	public List<GridResource> findAllResourcesM(
 			Map<JobSubmissionProperty, String> jobProperties, String fqan,
 			boolean exclude) {
 
 		List<GridResource> results = new LinkedList<GridResource>();
 		
 		String query = "SELECT acls.vo fqan, contactString, gramVersion," +
 				" jobManager, ce.name queue, maxWalltime, v.freeJobSlots, " +
 				"v.runningJobs, v.waitingJobs, v.totalJobs, s.name site, " +
 				"lattitude, longitude, sp.name sname ,sp.version  sversion" +
 				" FROM" +
 				" Sites s, SubClusters sc, Clusters c, ComputeElements ce, voViews v, " +
 				"voViewACLs acls, SoftwarePackages sp " +
 				"WHERE " +
 				"acls.voView_id = v.id and ce.cluster_id =c.id and " +
 				"c.site_id = s.id and c.id =  sc.cluster_id  and " +
 				"v.ce_id = ce.id and sp.subcluster_id = sc.id and acls.vo=?";
 
 		int wallTimeRequirement = -1;
 		try {
 			wallTimeRequirement = Integer.parseInt(jobProperties
 					.get(JobSubmissionProperty.WALLTIME_IN_MINUTES));
 		} catch (Exception e) {
 		}
 
 		int totalCPURequirement = -1;
 		try {
 			totalCPURequirement = Integer.parseInt(jobProperties
 					.get(JobSubmissionProperty.NO_CPUS));
 		} catch (Exception e) {
 		}
 
 		PreparedStatement s = getStatement(query);
 		setString(s, 1, fqan);
 
 		String[][] resources = runQuery(s, new String[] { "queue",
 				"contactString", "gramVersion", "jobManager", "freeJobSlots",
 				"runningJobs", "waitingJobs", "totalJobs", "site", "lattitude",
 				"longitude", "maxWalltime","sname","sversion" });
 
 		for (String[] resource : resources) {
 					
 			// check if application name matches
 			String applicationName = jobProperties.get(JobSubmissionProperty.APPLICATIONNAME);
 			if (StringUtils.isNotBlank(applicationName)
 					&& !Constants.GENERIC_APPLICATION_NAME
 					.equals(applicationName)
 					&& !resource[12].equals(applicationName)) {
 				continue;
 			}
 			
 			// check if application version matches
 			String applicationVersion = jobProperties.get(JobSubmissionProperty.APPLICATIONVERSION);
 			if (StringUtils.isNotBlank(applicationVersion) 
 					&& !Constants.NO_VERSION_INDICATOR_STRING.equals(applicationVersion) 
 					&& !resource[13].equals(applicationVersion)){
 				continue;
 			}
 			
 			
 			GridResourceBackendImpl gr = new GridResourceBackendImpl();
 
 			gr.setQueueName(resource[0]);
 			gr.setContactString(resource[1]);
 			gr.setGRAMVersion(resource[2]);
 			gr.setJobManager(resource[3]);
 
 			gr.setFreeJobSlots(Integer.parseInt(resource[4]));
 
 			int maxWalltime = Integer.parseInt(resource[11]);
 
 			if (exclude
 					&& ((gr.getFreeJobSlots() < totalCPURequirement) || (maxWalltime < wallTimeRequirement))) {
 				continue;
 			}
 
 			gr.setRunningJobs(Integer.parseInt(resource[5]));
 			gr.setWaitingJobs(Integer.parseInt(resource[6]));
 			gr.setTotalJobs(Integer.parseInt(resource[7]));
 
 			gr.setSiteName(resource[8]);
 			gr.setSiteLatitude(Double.parseDouble(resource[9]));
 			gr.setSiteLongitude(Double.parseDouble(resource[10]));
 
 			gr.setApplicationName(applicationName);
 			gr.addAvailableApplicationVersion(applicationVersion);
 	
 
 			String[] exes = getExecutables(gr.getSiteName(),gr.getQueueName(),applicationName,applicationVersion);
 
 			Set<String> executables = new HashSet<String>();
 			for (String exe : exes) {
 				executables.add(exe);
 			}			
 			if (executables.size() == 0){
 				executables.add(Constants.GENERIC_APPLICATION_NAME);
 			}
 			gr.setAllExecutables(executables);
 			results.add(gr);
 		}
 
 		return results;
 	}
 	
 	private String[] getExecutables(String siteName, String queue, String appName, String appVersion){
 		
 		if (appName == null && appVersion == null){
 			return new String[] {};
 		}
 		
 		String query = "select exe.name exeName from Sites s, Clusters c,SubClusters sc"
 			+ ",ComputeElements ce, SoftwarePackages sp, SoftwareExecutables exe "
 			+ "where s.id = c.site_id and c.id= sc.cluster_id and sp.subcluster_id = sc.id "
 			+ "and exe.package_id = sp.id AND ce.cluster_id =  c.id AND "
 			+ "s.name =? and ce.name =? AND (sp.name=? OR ? = ?) AND (sp.version=? OR ? = ?)";
 		PreparedStatement s = getStatement(query);
 		setString(s, 1, siteName);
 		setString(s, 2,queue);
 		setString(s, 3, appName);
 		setString(s, 4, appName);
 		setString(s, 5, Constants.GENERIC_APPLICATION_NAME);
 		setString(s, 6, appVersion);
 		setString(s, 7, appVersion);
 		setString(s, 8, Constants.NO_VERSION_INDICATOR_STRING);
 
 		return runQuery(s, "exeName");
 	}
 
 	public Map<String, String> getAllComputeHosts() {
 		Map<String, String> results = new HashMap<String, String>();
 		String query = "select DISTINCT  s.name as site, "
 			+ " SUBSTRING_INDEX(TRIM(LEADING 'https://' FROM ce.contactString),':',1) hostname  "
 			+ "FROM Sites s ,Clusters c,ComputeElements ce WHERE s.id = c.site_id and c.id = ce.cluster_id;";
 		PreparedStatement s = getStatement(query);
 		String[][] hostnames = runQuery(s, new String[] { "site", "hostname" });
 		for (String[] hostname : hostnames) {
 			results.put(hostname[1], hostname[0]);
 		}
 
 		return results;
 	}
 
 	public Map<String, String> getAllDataHosts() {
 		Map<String, String> results = new HashMap<String, String>();
 		String query = "SELECT DISTINCT s.name as site,"
 			+ "SUBSTRING_INDEX(TRIM(LEADING 'gsiftp://' FROM endpoint),':',1) as hostname "
 			+ "FROM Sites s,StorageElements se,StorageAreas sa, AccessProtocols p "
 			+ "WHERE s.id = se.site_id AND se.id = sa.storageElement_ID AND se.id=p.storageElement_ID";
 		PreparedStatement s = getStatement(query);
 		String[][] hostnames = runQuery(s, new String[] { "site", "hostname" });
 		for (String[] hostname : hostnames) {
 			results.put(hostname[1], hostname[0]);
 		}
 		return results;
 	}
 
 	// only partially implemented
 	public GridResource[] getAllGridResources() {
 		String query = "select contactString,gramVersion,jobManager,ce.name,"
 			+ "freeJobSlots,runningJobs,waitingJobs,totalJobs,s.name,"
 			+ "lattitude,longitude  "
 			+ "from Sites s,Clusters c ,ComputeElements ce where "
 			+ "ce.cluster_id = c.id and c.site_id = s.id";
 		PreparedStatement s = getStatement(query);
 		String[][] results = runQuery(s, new String[] { "contactString",
 				"gramVersion", "jobManager", "ce.name", "freeJobSlots",
 				"runningJobs", "waitingJobs", "totalJobs", "s.name",
 				"lattitude", "longitude" });
 
 		GridResource[] grs = new GridResource[results.length];
 		for (int i = 0; i < results.length; i++) {
 			GridResourceBackendImpl gr = new GridResourceBackendImpl();
 			gr.setContactString(results[i][0]);
 			gr.setGRAMVersion(results[i][1]);
 			gr.setJobManager(results[i][2]);
 			gr.setQueueName(results[i][3]);
 			grs[i] = gr;
 		}
 
 		return grs;
 	}
 
 	public String[] getApplicationNamesThatProvideExecutable(String executable) {
 		String query = "SELECT DISTINCT BINARY  sp.name as name FROM SoftwareExecutables AS "
 			+ "   se,SoftwarePackages AS sp WHERE se.package_id = sp.id AND "
 			+ " se.name=?";
 		PreparedStatement s = getStatement(query);
 		setString(s, 1, executable);
 		return runQuery(s, "name");
 	}
 
 	public String[] getClusterNamesAtSite(String site) {
 		String query = "SELECT c.name as name FROM Sites AS s ,Clusters AS  c WHERE s.id = c.site_id AND s.name=?";
 		PreparedStatement s = getStatement(query);
 		setString(s, 1, site);
 		return runQuery(s, "name");
 	}
 
 	public String[] getClustersForCodeAtSite(String site, String code,
 			String version) {
 		String query = " SELECT c.uniqueID FROM Sites AS s ,Clusters AS  c, SubClusters AS "
 			+ " sc,SoftwarePackages as p where s.id = c.site_id AND sc.cluster_id = c.id "
 			+ " AND p.subcluster_id = sc.id AND s.name=? AND "
 			+ " p.name=? AND p.version = ?";
 		PreparedStatement s = getStatement(query);
 		setString(s, 1, site);
 		setString(s, 2, code);
 		setString(s, 3, version);
 		return runQuery(s, "uniqueID");
 	}
 
 	public String[] getCodesAtSite(String site) {
 		String query = "SELECT DISTINCT p.name FROM Sites AS s ,Clusters AS  c, SubClusters AS "
 			+ " sc,SoftwarePackages AS p WHERE s.id = c.site_id AND sc.cluster_id = "
 			+ " c.id AND s.name=? AND p.subcluster_id = sc.id;";
 		PreparedStatement s = getStatement(query);
 		setString(s, 1, site);
 		return runQuery(s, "name");
 	}
 
 	public String[] getCodesOnGrid() {
 		String query = "SELECT DISTINCT BINARY name AS NAME FROM SoftwarePackages";
 		PreparedStatement s = getStatement(query);
 		return runQuery(s, "name");
 	}
 
 	public String[] getCodesOnGridForVO(String fqan) {
		String query = "select distinct binary sp.name as name from SubClusters sc, ComputeElements ce, voViews v, voViewACLs acls, SoftwarePackages sp"
 			+ " WHERE sc.cluster_id = ce.cluster_id AND v.ce_id = ce.id "
 			+ "AND acls.voView_id = v.id AND sp.subcluster_id = sc.id AND acls.vo=?";
 		PreparedStatement s = getStatement(query);
 		setString(s, 1, fqan);
 		return runQuery(s, "name");
 	}
 
 	public String[] getContactStringOfQueueAtSite(String site, String queue) {
 		String query = " SELECT contactString FROM ComputeElements ce,Clusters c,Sites  s WHERE "
 			+ "c.site_id = s.id AND ce.cluster_id = c.id  AND s.name = ? AND ce.name =? ";
 		PreparedStatement s = getStatement(query);
 		setString(s, 1, site);
 		setString(s, 2, queue);
 		return runQuery(s, "contactString");
 	}
 
 	public String[] getDataDir(String site, String storageElement, String FQAN) {
 		String query = " SELECT StorageAreas.path  FROM "
 			+ " Sites,StorageElements,StorageAreas,storageAreaACLs WHERE "
 			+ " Sites.id=StorageElements.site_id AND StorageElements.id = "
 			+ " StorageAreas.storageElement_id AND StorageAreas.id = "
 			+ " storageAreaACLs.storageArea_id AND Sites.name=? "
 			+ "AND vo=? AND StorageElements.uniqueID =?";
 		PreparedStatement s = getStatement(query);
 		setString(s, 1, site);
 		setString(s, 3, storageElement);
 		setString(s, 2, FQAN);
 		return runQuery(s, "path");
 
 	}
 
 	public String getDefaultStorageElementForQueueAtSite(String site,
 			String queue) {
 		String query = "SELECT se.uniqueID FROM Sites AS s,StorageElements AS "
 			+ " se,ComputeElements AS ce WHERE s.id = se.site_id AND s.id = se.site_id "
 			+ " AND ce.defaultSE_id = se.id AND s.name=? AND  ce.name=?";
 		PreparedStatement s = getStatement(query);
 		setString(s, 1, site);
 		setString(s, 2, queue);
 		String[] elements = runQuery(s, "uniqueID");
 		if ((elements == null) || (elements.length == 0)) {
 			return null;
 		} else {
 			return elements[0];
 		}
 	}
 
 	public String[] getExeNameOfCodeAtSite(String site, String code,
 			String version) {
 		String query = "SELECT exec.name FROM Sites AS s ,Clusters AS c,SubClusters AS "
 			+ " sc,SoftwarePackages AS sp,SoftwareExecutables AS exec WHERE s.id = "
 			+ " c.site_id AND c.id = sc.cluster_id AND sc.id = sp.subcluster_id AND "
 			+ " exec.package_id = sp.id AND s.name=? AND "
 			+ " sp.name=?  AND sp.version=? ";
 		PreparedStatement s = getStatement(query);
 		setString(s, 1, site);
 		setString(s, 2, code);
 		setString(s, 3, version);
 		return runQuery(s, "name");
 	}
 
 	public String[] getExeNameOfCodeForSubmissionLocation(String subLoc,
 			String code, String version) {
 		String query = "SELECT exec.name FROM Sites AS s ,ComputeElements AS ce,Clusters AS "
 			+ " c ,SubClusters AS  sc,SoftwarePackages AS sp,SoftwareExecutables AS "
 			+ " exec WHERE c.id = ce.cluster_id AND s.id =  c.site_id AND c.id = "
 			+ " sc.cluster_id AND sc.id = sp.subcluster_id AND  exec.package_id = "
 			+ " sp.id AND ce.name=? AND "
 			+ " ce.hostname=? AND  sp.name=?  AND sp.version=?";
 
 		String hostname = getSubmissionHostName(subLoc);
 		String queue = getSubmissionQueue(subLoc);
 		PreparedStatement s = getStatement(query);
 		setString(s, 1, queue);
 		setString(s, 2, hostname);
 		setString(s, 3, code);
 		setString(s, 4, version);
 		return runQuery(s, "name");
 	}
 
 	public String[] getGridFTPServersAtSite(String site) {
 		String query = "SELECT DISTINCT endpoint FROM Sites AS s ,StorageElements AS "
 			+ " se,StorageAreas AS sa, AccessProtocols AS p  WHERE s.id = se.site_id "
 			+ " AND se.id = sa.storageElement_ID AND se.id=p.storageElement_ID AND s.name=?";
 		PreparedStatement s = getStatement(query);
 		setString(s, 1, site);
 		return runQuery(s, "endpoint");
 	}
 
 	public String[] getGridFTPServersForQueueAtSite(String site, String queue) {
 		String query = "SELECT DISTINCT endpoint FROM Sites s ,Clusters c,StorageElements "
 			+ " se,StorageAreas sa, AccessProtocols  p,ComputeElements ce "
 			+ " WHERE s.id = se.site_id AND s.id = c.site_id AND ce.cluster_id = c.id AND se.id = "
 			+ "sa.storageElement_ID AND ce.defaultSE_id = se.id AND se.id=p.storageElement_ID AND "
 			+ "  s.name=? AND ce.name = ? ";
 		PreparedStatement s = getStatement(query);
 		setString(s, 1, site);
 		setString(s, 2, queue);
 		return runQuery(s, "endpoint");
 	}
 
 	public String[] getGridFTPServersForStorageElementAtSite(String site,
 			String storageElement) {
 		String query = "select distinct endpoint from Sites  s, Clusters c ,StorageElements "
 			+ " se,StorageAreas  sa, AccessProtocols  p,ComputeElements  ce "
 			+ " WHERE s.id = se.site_id AND s.id = c.site_id AND c.id = ce.cluster_id AND se.id = "
 			+ " sa.storageElement_ID AND se.id=p.storageElement_ID AND "
 			+ " s.name=? AND se.uniqueID =?";
 		PreparedStatement s = getStatement(query);
 		setString(s, 1, site);
 		setString(s, 2, storageElement);
 		return runQuery(s, "endpoint");
 	}
 
 	public String[] getGridFTPServersOnGrid() {
 		String query = "select AccessProtocols.endpoint from StorageElements,AccessProtocols "
 			+ " where StorageElements.id = AccessProtocols.storageElement_id";
 		PreparedStatement s = getStatement(query);
 		return runQuery(s, "endpoint");
 	}
 
 	public String getJobManagerOfQueueAtSite(String site, String queue) {
 		String query = "select ce.jobManager from Sites s,Clusters c,ComputeElements ce WHERE s.id "
 			+ " = c.site_id AND c.id = ce.cluster_id AND s.name=? AND ce.name=?";
 		PreparedStatement s = getStatement(query);
 		setString(s, 1, site);
 		setString(s, 2, queue);
 		return runQuery(s, "jobManager")[0];
 	}
 
 	// not sure what this means
 	public String getJobTypeOfCodeAtSite(String site, String code,
 			String version) {
 		throw new UnsupportedOperationException("Not supported yet.");
 	}
 
 	public String getLRMSTypeOfQueueAtSite(String site, String queue) {
 		String query = "SELECT ce.lRMSType FROM Sites s,Clusters c,ComputeElements ce WHERE s.id = "
 			+ " c.site_id AND c.id = ce.cluster_id AND s.name=? AND ce.name=?";
 		PreparedStatement s = getStatement(query);
 		setString(s, 1, site);
 		setString(s, 2, queue);
 		return runQuery(s, "lRMSType")[0];
 	}
 
 	public String getModuleNameOfCodeForSubmissionLocation(String subLoc,
 			String code, String version) {
 		String query = "SELECT sp.module FROM Sites AS s ,ComputeElements AS ce,Clusters AS "
 			+ " c ,SubClusters AS  sc,SoftwarePackages AS sp "
 			+ " WHERE s.id = c.site_id AND c.id =  ce.cluster_id AND c.id = "
 			+ " sc.cluster_id AND sc.id = sp.subcluster_id  AND ce.name=? AND "
 			+ " ce.hostname=? AND sp.name=?  AND sp.version=?";
 
 		String hostname = getSubmissionHostName(subLoc);
 		String queue = getSubmissionQueue(subLoc);
 		PreparedStatement s = getStatement(query);
 		setString(s, 1, queue);
 		setString(s, 2, hostname);
 		setString(s, 3, code);
 		setString(s, 4, version);
 		String[] module = runQuery(s, "module");
 		if ((module == null) || (module.length == 0)) {
 			return null;
 		} else {
 			return module[0];
 		}
 	}
 
 	public String[] getQueueNamesAtSite(String site) {
 		String query = " SELECT ce.name FROM Sites s,Clusters c "
 			+ ",ComputeElements ce WHERE s.id = c.site_id AND c.id = ce.cluster_id AND s.name=?";
 		PreparedStatement s = getStatement(query);
 		setString(s, 1, site);
 		return runQuery(s, "name");
 	}
 
 	public String[] getQueueNamesAtSite(String site, String fqan) {
 		String query = "select ce.name from voViews v,Sites s,Clusters c,ComputeElements ce,voViewACLs "
 			+ " acls where s.id = c.site_id and c.id = ce.cluster_id and v.ce_id=ce.id AND v.id = "
 			+ " acls.voView_id AND s.name=? and vo = ?";
 		PreparedStatement s = getStatement(query);
 		setString(s, 1, site);
 		setString(s, 2, fqan);
 		return runQuery(s, "name");
 	}
 
 	public String[] getQueueNamesForClusterAtSite(String site, String cluster) {
 		String query = "select ce.name from Sites s,ComputeElements ce,Clusters as c where "
 			+ "s.id = c.site_id and c.id = ce.cluster_id AND s.name=? and c.name = ?";
 		PreparedStatement s = getStatement(query);
 		setString(s, 1, site);
 		setString(s, 2, cluster);
 		return runQuery(s, "name");
 	}
 
 	public String[] getQueueNamesForCodeAtSite(String site, String code) {
 		String query = "select ce.name from Sites as s ,ComputeElements as ce,Clusters as "
 			+ " c,SubClusters as sc,SoftwarePackages as sp WHERE s.id = c.site_id AND "
 			+ " c.id = ce.cluster_id AND c.id = sc.cluster_id AND sc.id = "
 			+ " sp.subcluster_id AND s.name=? AND sp.name=?";
 		PreparedStatement s = getStatement(query);
 		setString(s, 1, site);
 		setString(s, 2, code);
 		return runQuery(s, "name");
 
 	}
 
 	public String[] getQueueNamesForCodeAtSite(String site, String code,
 			String version) {
 		String query = "select ce.name from Sites as s ,ComputeElements as ce,Clusters as "
 			+ " c,SubClusters as sc,SoftwarePackages as sp WHERE s.id = c.site_id AND "
 			+ " c.id = ce.cluster_id AND c.id = sc.cluster_id AND sc.id = "
 			+ " sp.subcluster_id AND s.name=? AND sp.name=? AND sp.version=?";
 		PreparedStatement s = getStatement(query);
 		setString(s, 1, site);
 		setString(s, 2, code);
 		setString(s, 3, version);
 		return runQuery(s, "name");
 
 	}
 
 	public String getSiteForHost(String host) {
 		String query = "SELECT DISTINCT  s.name from Sites s, Clusters c, ComputeElements ce where s.id "
 			+ " = c.site_id AND c.id = ce.cluster_id and ce.hostname=?";
 		PreparedStatement s = getStatement(query);
 		setString(s, 1, host);
 		String[] sites = runQuery(s, "name");
 		if ((sites == null) || (sites.length == 0)) {
 			return null;
 		} else {
 			return sites[0];
 		}
 	}
 
 	public String[] getSitesForVO(String fqan) {
 		String query = " select distinct s.name from Sites s,Clusters c, ComputeElements ce"
 			+ ",voViews v,voViewACLs acls WHERE s.id=c.site_id and c.id = ce.cluster_id "
 			+ "AND ce.id = v.ce_id and acls.voView_id=v.id AND acls.vo=?";
 		PreparedStatement s = getStatement(query);
 		setString(s, 1, fqan);
 		return runQuery(s, "name");
 	}
 
 	public String[] getSitesOnGrid() {
 		String query = "select s.name from Sites s";
 		PreparedStatement s = getStatement(query);
 		return runQuery(s, "name");
 	}
 
 	public String[] getSitesWithAVersionOfACode(String code, String version) {
 		String query = "select distinct s.name from Sites s,Clusters c,SubClusters sc, "
 			+ " SoftwarePackages sp where s.id = c.site_id and c.id = sc.cluster_id "
 			+ " AND sp.subcluster_id = sc.id  and sp.name=? and sp.version = ?";
 		PreparedStatement s = getStatement(query);
 		setString(s, 1, code);
 		setString(s, 2, version);
 		return runQuery(s, "name");
 	}
 
 	public String[] getSitesWithCode(String code) {
 		String query = "select distinct s.name from Sites s,Clusters c,SubClusters sc, "
 			+ " SoftwarePackages sp where s.id = c.site_id and c.id = sc.cluster_id "
 			+ " AND sp.subcluster_id = sc.id  and sp.name=?";
 		PreparedStatement s = getStatement(query);
 		setString(s, 1, code);
 		return runQuery(s, "name");
 	}
 
 	private PreparedStatement getStatement(String query) {
 		try {
 			if (!con.isValid(1)) {
 				Class.forName("com.mysql.jdbc.Driver").newInstance();
 				con = DriverManager.getConnection(this.databaseUrl, this.user,
 						this.password);
 			}
 			PreparedStatement s = con.prepareStatement(query);
 			return s;
 		} catch (InstantiationException ex) {
 			throw new RuntimeException(ex);
 		} catch (IllegalAccessException ex) {
 			throw new RuntimeException(ex);
 		} catch (ClassNotFoundException ex) {
 			throw new RuntimeException(ex);
 		} catch (SQLException ex) {
 			throw new RuntimeException(ex);
 		}
 	}
 
 	public String getStorageElementForGridFTPServer(String gridFtp) {
 		String query = "select se.uniqueID from StorageElements se, AccessProtocols p WHERE "
 			+ " p.storageElement_id=se.id AND endpoint=?";
 		PreparedStatement s = getStatement(query);
 		setString(s, 1, gridFtp);
 		String[] ses = runQuery(s, "uniqueID");
 		if ((ses == null) || (ses.length == 0)) {
 			return null;
 		} else {
 			return ses[0];
 		}
 	}
 
 	public String[] getStorageElementsForSite(String site) {
 		String query = "select  se.uniqueID from Sites s,StorageElements se WHERE se.site_id=s.id AND s.name=?";
 		PreparedStatement s = getStatement(query);
 		setString(s, 1, site);
 		return runQuery(s, "uniqueID");
 	}
 
 	public String[] getStorageElementsForVO(String fqan) {
 		String query = "select distinct se.uniqueID from Sites s, StorageElements se,"
 			+ "StorageAreas sa, storageAreaACLs sacls WHERE s.id = se.site_id and sa."
 			+ "storageElement_id = se.id AND sacls.storageArea_id = sa.id and sacls.vo=?";
 		PreparedStatement s = getStatement(query);
 		setString(s, 1, fqan);
 		return runQuery(s, "uniqueID");
 	}
 
 	private String getSubmissionHostName(String submissionLocation) {
 		int dot = submissionLocation.indexOf(":") + 1;
 		return submissionLocation.substring(dot);
 	}
 
 	private String getSubmissionQueue(String submissionLocation) {
 		int dot = submissionLocation.indexOf(":");
 		if (dot == -1) {
 			return "";
 		}
 		return submissionLocation.substring(0, dot);
 	}
 
 	public String[] getVersionsOfCodeAtSite(String site, String code) {
 		String query = "SELECT DISTINCT sp.version FROM Sites s,Clusters c,SubClusters "
 			+ " sc,SoftwarePackages sp WHERE s.id = c.site_id AND c.id = sc.cluster_id "
 			+ " AND sc.id = sp.subcluster_id AND s.name=? AND sp.name=?";
 		PreparedStatement s = getStatement(query);
 		setString(s, 1, site);
 		setString(s, 2, code);
 		return runQuery(s, "version");
 	}
 
 	public String[] getVersionsOfCodeForQueueAndContactString(String queueName,
 			String hostName, String code) {
 		String query = "select distinct sp.version from Clusters c,ComputeElements "
 			+ " ce,SubClusters sc,SoftwarePackages sp WHERE c.id = ce.cluster_id AND "
 			+ " c.id = sc.cluster_id AND sc.id = sp.subcluster_id AND  sp.name=? "
 			+ "AND ce.hostName=? AND ce.name=?";
 		PreparedStatement s = getStatement(query);
 		setString(s, 1, code);
 		setString(s, 2, hostName);
 		setString(s, 3, queueName);
 		return runQuery(s, "version");
 	}
 
 	public String[] getVersionsOfCodeOnGrid(String code) {
 		String query = "select distinct version from SoftwarePackages sp where sp.name=?";
 		PreparedStatement s = getStatement(query);
 		setString(s, 1, code);
 		return runQuery(s, "version");
 	}
 
 	public String[] getVersionsOfCodeOnGridForVO(String code, String fqan) {
 		String query = "select distinct sp.version from Clusters c, SubClusters sc, SoftwarePackages sp,"
 			+ " ComputeElements ce, voViews v, voViewACLs acls WHERE c.id = sc.cluster_id AND c.id ="
 			+ " ce.cluster_id AND sp.subcluster_id = sc.id AND ce.id = v.ce_id  AND acls.voView_id ="
 			+ " v.id AND sp.name =? and acls.vo=?";
 		PreparedStatement s = getStatement(query);
 		setString(s, 1, code);
 		setString(s, 2, fqan);
 		return runQuery(s, "version");
 	}
 
 	public boolean isParallelAvailForCodeForSubmissionLocation(String subLoc,
 			String code, String version) {
 		String query = "select  1 from ComputeElements ce,SubClusters sc,SoftwarePackages "
 			+ " sp,SoftwareExecutables exec where ce.cluster_id = sc.cluster_id AND "
 			+ " sp.subcluster_id = sc.id AND exec.package_id = sp.id AND "
 			+ " sp.name=? AND sp.version=? AND "
 			+ " ce.hostname=? AND ce.name=? AND " + " isParallel=1";
 		String hostname = getSubmissionHostName(subLoc);
 		String queue = getSubmissionQueue(subLoc);
 		PreparedStatement s = getStatement(query);
 		setString(s, 1, code);
 		setString(s, 2, version);
 		setString(s, 3, hostname);
 		setString(s, 4, queue);
 		String[] result = runQuery(s, "1");
 		return !((result == null) || (result.length == 0));
 
 	}
 
 	public boolean isSerialAvailForCodeForSubmissionLocation(String subLoc,
 			String code, String version) {
 		String query = "select  1 from ComputeElements ce,SubClusters sc,SoftwarePackages "
 			+ " sp,SoftwareExecutables exec where ce.cluster_id = sc.cluster_id AND "
 			+ " sp.subcluster_id = sc.id AND exec.package_id = sp.id AND "
 			+ " sp.name=? AND sp.version=? AND "
 			+ " ce.hostname=? AND ce.name=? AND " + " isSerial=1";
 		String hostname = getSubmissionHostName(subLoc);
 		String queue = getSubmissionQueue(subLoc);
 		PreparedStatement s = getStatement(query);
 		setString(s, 1, code);
 		setString(s, 2, version);
 		setString(s, 3, hostname);
 		setString(s, 4, queue);
 		String[] result = runQuery(s, "1");
 		return !((result == null) || (result.length == 0));
 	}
 	
 	public boolean isVolatile(String endpoint, String path, String fqan){
 
 		PreparedStatement s;
 
 		Pattern complete = Pattern.compile("gsiftp://.+:[0-9]+");
 		Pattern portOnly = Pattern.compile(".+:[0-9]+");
 		Pattern protocolOnly = Pattern.compile("gsiftp://.+");
 
 		if (complete.matcher(endpoint).matches()) {
 			// do nothing, endpoint has valid value
 		} 
 		else if (portOnly.matcher(endpoint).matches()){
 			endpoint = "gsiftp://" + endpoint;
 		}
 		else if (protocolOnly.matcher(endpoint).matches()){
 			endpoint += ":2811";
 		}
 		else {
 			endpoint = "gsiftp://" + endpoint + ":2811";
 		}
 
 		String query = "select sa.type from AccessProtocols ap, StorageElements s, StorageAreas sa" +
 		",storageAreaACLs acls WHERE sa.id = acls.storageArea_id AND s.id = sa.storageElement_id AND " +
 		"ap.storageElement_id = s.id AND acls.vo = ? AND ap.endPoint=? AND (sa.path = ? OR sa.path LIKE ?)";
 		s = getStatement(query);
 		setString(s,1,fqan);
 		setString(s,2,endpoint);
 		setString(s,3,path);
 		setString(s,4,path + "[%]");
 		String[] result = runQuery(s,"type");
 		if (result.length > 0 ){
 			return result[0].equals(VOLATILE);
 		}
 		query = "select sa.type from AccessProtocols ap, StorageElements s, StorageAreas sa" +
 		",storageAreaACLs acls WHERE sa.id = acls.storageArea_id AND s.id = sa.storageElement_id AND " +
 		"ap.storageElement_id = s.id AND acls.vo = ? AND ap.endPoint=? AND (sa.path = '${GLOBUS_USER_HOME}' or " +
 		" sa.path = '/~/' or sa.path LIKE '.%' )";
 		
 		s = getStatement(query);
 		setString(s,1,fqan);
 		setString(s,2,endpoint);
 		result = runQuery(s,"type");
 		if (result.length > 0 ){
 			return result[0].equals(VOLATILE);
 		}
 		
 		return true;
 
 	}
 
 	private String[] runQuery(PreparedStatement s, String output) {
 		try {
 			myLogger.debug(s.toString());
 			HashSet<String> resultSet = new HashSet<String>();
 			s.execute();
 			ResultSet rs = s.getResultSet();
 			while (rs.next()) {
 				resultSet.add(rs.getString(output));
 			}
 			return resultSet.toArray(new String[] {});
 		} catch (SQLException ex) {
 			throw new RuntimeException(ex);
 		}
 	}
 
 	private String[][] runQuery(PreparedStatement s, String output1,
 			String output2) {
 		try {
 			myLogger.debug(s.toString());
 			LinkedList<String> resultSet1 = new LinkedList<String>();
 			LinkedList<String> resultSet2 = new LinkedList<String>();
 			s.execute();
 			ResultSet rs = s.getResultSet();
 			while (rs.next()) {
 				resultSet1.add(rs.getString(output1));
 				resultSet2.add(rs.getString(output2));
 			}
 			return new String[][] { resultSet1.toArray(new String[] {}),
 					resultSet2.toArray(new String[] {}) };
 		} catch (SQLException ex) {
 			throw new RuntimeException(ex);
 		}
 	}
 
 	private String[][] runQuery(PreparedStatement s, String[] outputs) {
 		try {
 			myLogger.debug(s.toString());
 			LinkedList<String[]> resultSet = new LinkedList<String[]>();
 			s.execute();
 			ResultSet rs = s.getResultSet();
 			while (rs.next()) {
 				String[] outputValues = new String[outputs.length];
 				for (int i = 0; i < outputs.length; i++) {
 					outputValues[i] = rs.getString(outputs[i]);
 				}
 				resultSet.add(outputValues);
 			}
 			return resultSet.toArray(new String[][] {});
 		} catch (SQLException ex) {
 			throw new RuntimeException(ex);
 		}
 	}
 
 	private void setString(PreparedStatement s, int i, String string) {
 		try {
 			s.setString(i, string);
 		} catch (SQLException ex) {
 			throw new RuntimeException(ex);
 		}
 	}
 
 }
