 /*
  *	File: @(#)EsbData.java 		Package: com.pace.base.mdb.essbase 	Project: Essbase Provider
  *	Created: Aug 15, 2005  		By: Alan Farkas
  *	Version: x.xx
  *
  * 	Copyright (c) 2005-2006 Palladium Group, Inc. All rights reserved.
  *
  *	This software is the confidential and proprietary information of Palladium Group, Inc.
  *	("Confidential Information"). You shall not disclose such Confidential Information and 
  * 	should use it only in accordance with the terms of the license agreement you entered into
  *	with Palladium Group, Inc.
  *
  *
  *
  Date			Author			Version			Changes
  xx/xx/xx		xxxxxxxx		x.xx			..............
  * 
  */
 package com.pace.mdb.essbase;
 
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.text.DecimalFormat;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 import java.util.Set;
 
 import org.apache.log4j.Logger;
 
 import com.essbase.api.base.EssException;
 import com.essbase.api.base.IEssSequentialIterator;
 import com.essbase.api.dataquery.IEssMdAxis;
 import com.essbase.api.dataquery.IEssMdDataSet;
 import com.essbase.api.dataquery.IEssMdMember;
 import com.essbase.api.datasource.IEssCube;
 import com.essbase.api.datasource.IEssOlapFileObject;
 import com.essbase.api.datasource.IEssOlapServer;
 import com.pace.base.PafBaseConstants;
 import com.pace.base.PafErrSeverity;
 import com.pace.base.PafException;
 import com.pace.base.app.PafApplicationDef;
 import com.pace.base.app.PafDimSpec;
 import com.pace.base.app.UnitOfWork;
 import com.pace.base.app.VersionDef;
 import com.pace.base.app.VersionFormula;
 import com.pace.base.app.VersionType;
 import com.pace.base.data.Intersection;
 import com.pace.base.mdb.IMdbData;
 import com.pace.base.mdb.PafDataCache;
 import com.pace.base.mdb.PafDimMember;
 import com.pace.base.state.IPafClientState;
 import com.pace.base.state.PafClientState;
 import com.pace.base.utility.LogUtil;
 import com.pace.base.utility.Odometer;
 import com.pace.base.utility.StringUtils;
 
 /**
  * Provides access to operations on Essbase data
  *
  * @version	x.xx
  * @author Alan Farkas
  *
  */
 public class EsbData implements IMdbData{	
 
 	private String dataSourceID = "";
 	private String esbConnAlias = null;
 	private Properties connectionProps = null;
 	private boolean useConnPool = false;
 	private final static String ESS_TEXT_FILE_SUFFIX = PafBaseConstants.ESS_TEXT_FILE_SUFFIX;
 	private static Logger logger = Logger.getLogger(EsbData.class);
 	private static Logger performanceLogger = Logger.getLogger(PafBaseConstants.PERFORMANCE_LOGGER_MDB_IO);
 	
 	
 	/**
 	 * Default constructor called by IMdbData interface
 	 * 
 	 * @param connectionProps Essbase connection properties
 	 */
 	public EsbData(Properties connectionProps) {
 		// Assume that any available connection pool should be used
 		this(connectionProps, true);
 	}
 	
 	/**
 	 * @param connectionProps Essbase connection properties
 	 * @param useConnPool True if any available connection pool should be used
 	 */
 	public EsbData(Properties connectionProps, boolean useConnPool ) {
 		
 		String esbConnAlias = "[" + connectionProps.getProperty("SERVER") + "/" 
 				+ connectionProps.getProperty("APPLICATION") + "/" 
 				+ connectionProps.getProperty("DATABASE") + "]";
 		logger.debug("Creating instance of EsbData - Connection Alias: " 
 				+ esbConnAlias + " - Use Connection Pool: " + useConnPool);
 		this.esbConnAlias = esbConnAlias;
 		this.connectionProps = connectionProps;
 		this.useConnPool = useConnPool;
 	}
 
 	/**
 	 * @param connectionProps Essbase connection property collection
 	 * @param dataSourceID Data Source ID corresponding to specified connection
 	 */
 	public EsbData(Properties connectionProps, String dataSourceID) {
 		
 		// Assume that any available connection pool should be used
 		this(connectionProps, true);
 		this.dataSourceID = dataSourceID;
 		logger.info("- DataSourceID [" + dataSourceID + "]");
 	}
 	
 	/**
 	 * @param esbConnAlias Essbase Connection Alias
 	 * @param useConnPool True if any available connection pool should be used
 	 */
 	public EsbData(String esbConnAlias, boolean useConnPool ) {
 		
 		logger.debug("Creating instance of EsbData - Connection Alias: " 
 				+ esbConnAlias + " - Use Connection Pool: " + useConnPool);
 		this.esbConnAlias = esbConnAlias;
 		this.useConnPool = useConnPool;
 	}
 	
 
 	/** 
 	 *	Refresh the data cache across any version listed in the version filter.
 	 *
 	 * 	Any refreshed version will first be initialized before the data is refreshed
 	 *  from the mdb using the supplied data specification. Any refreshed versions not 
 	 *  referenced in the data specification will be loaded as needed during view 
 	 *  rendering or evaluation.
 	 * 
 	 *  No data will be refreshed if the version filter is empty.
 	 *  
 	 * @param dataCache Data cache
 	 * @param mdbDataSpec Specifies the data intersections to reload from Essbase, by version
 	 * @param versionFilter List of versions to refresh
 	 * 	  
 	 * @return Map describing which intersections were actually retrieved.
 	 * @throws PafException 
 	 */ 
 	public Map<String, Map<Integer, List<String>>> refreshDataCache(PafDataCache dataCache, Map<String, Map<Integer, List<String>>> mdbDataSpec, List<String> versionFilter) throws PafException {
 	
 		// Initialize refreshed versions
 		List<String> clearedVersions = new ArrayList<String>(versionFilter);
 		clearedVersions.addAll(dataCache.getDependentVersions(versionFilter));
 		dataCache.clearVersionData(clearedVersions);
 
 		// Reload specified data intersections
 		return updateDataCache(dataCache, mdbDataSpec);
 	}
 
 	/** 
 	 *	Update the data cache with mdb data for the specified versions. For
 	 * 	performance reasons, existing data blocks will not be updated.
 	 * 
 	 *  Any versions that need to be completely refreshed should be cleared before 
 	 *  calling this method.
 	 *
 	 *  No data will be refreshed if the version filter is empty.
 	 *  
 	 * @param dataCache Data cache
 	 * @param expandedUow Expanded unit of work specification
 	 * @param versionFilter List of versions to refresh
 	 * 	  
 	 * @return Map describing which intersections were actually retrieved.
 	 * @throws PafException 
 	 */ 
 	public Map<String, Map<Integer, List<String>>> updateDataCache(PafDataCache dataCache, UnitOfWork expandedUow, List<String> versionFilter) throws PafException {
 	
 		// Exit if version filter is empty
 		if (versionFilter == null || versionFilter.isEmpty()) {
 			return new HashMap<String, Map<Integer, List<String>>>();
 		}
 		
 		// Create a data specification for each filtered version that specifies that
 		// all UOW data (for the filtered version) is loaded from the mdb.
 		int versionAxis = dataCache.getVersionAxis();
 		Map<String, Map<Integer, List<String>>> mdbDataSpec = new HashMap<String, Map<Integer, List<String>>>();
 		for (String version : versionFilter) {
 			Map<Integer, List<String>> expandedUowMap = expandedUow.buildUowMap(); 
 			List<String> versionSpec = new ArrayList<String>(Arrays.asList(new String[]{version}));
 			expandedUowMap.put(versionAxis, versionSpec);
 			mdbDataSpec.put(version, expandedUowMap);
 		}
 		
 		// Update filtered versions
 		return updateDataCache(dataCache, mdbDataSpec);
 	}
 
 	/** 
 	 *	Update the data cache with mdb data for the intersections specified, by version.
 	 * 	For performance reasons, existing data blocks will not be updated.
 	 * 
 	 *  Any versions that need to be completely refreshed should be cleared before 
 	 *  calling this method.
 	 *
 	 *  No data will be loaded if the data specification is empty.
 	 *  
 	 * @param dataCache Data cache
 	 * @param mdbDataSpec Specifies the intersections to retrieve from Essbase, by version
 	 * 
 	 * @return Map describing which intersections were actually retrieved.
 	 * @throws PafException
 	 */
     public Map<String, Map<Integer, List<String>>> updateDataCache(PafDataCache dataCache, Map<String, Map<Integer, List<String>>> mdbDataSpec) throws PafException {
 
     	int mdxCellCount = 0;
 		final long loadDcStartTime = System.currentTimeMillis();
 		String cubeViewName = null;
 		String esbApp = null, esbDb = null;
 		String mdxFrom = null, mdxWhere = "", mdxSelect = null;
 		String logMsg = null;
 		final int versionAxis = dataCache.getVersionAxis(), yearAxis = dataCache.getYearAxis();
 		final String versionDim = dataCache.getVersionDim(), yearDim = dataCache.getYearDim();
 		Map<String, Map<Integer, List<String>>> loadedMdbDataSpec = new HashMap<String, Map<Integer, List<String>>>();   // Track data that was actually loaded
 		final List<String> mdbYears = dataCache.getMdbYears();    
 		EsbCubeView esbCubeView = null;
 		
 
 		// Exit if no data has been specified
 		if (mdbDataSpec == null || mdbDataSpec.size() == 0) {
 			logger.debug("UpdateDataCache() - empty data spec - no data was updated");
 			return loadedMdbDataSpec;
 		}
 		
 		logger.info("Loading UOW from cube: " + esbConnAlias ); 
 
 		// Connect to Essbase Cube
 		cubeViewName = "Paf View - " + esbConnAlias;
 		logger.debug("Opening cube view: " + cubeViewName); 
 		long esbConnectStartTime = System.currentTimeMillis();
 		String stepDesc = "Connection to Essbase Cube: " + esbConnAlias;
 		esbCubeView = new EsbCubeView(cubeViewName,  connectionProps, useConnPool, false, false, true);	
 		logMsg = LogUtil.timedStep(stepDesc, esbConnectStartTime);
 		performanceLogger.info(logMsg);
 
 		// Extract Essbase data into data cache a version at a time.
 		logger.debug("Extracting Essbase data into DataCache...");   
 		mdxCellCount = 0;		
 		esbApp = esbCubeView.getEsbApp();
 		esbDb = esbCubeView.getEsbDb(); 
 		mdxFrom = " FROM " + esbApp + "." + esbDb;
 		Set<String> extractedVersions = mdbDataSpec.keySet();
 		for (String version: extractedVersions) {
 
 			// Filter out existing intersections - skip version, if no data to load
 			Map<Integer, List<String>> memberMap = mdbDataSpec.get(version);
 			Map<Integer, List<String>> filteredMemberMap = dataCache.getFilteredRefDataSpec(memberMap);
 			if (filteredMemberMap.isEmpty()) {
 				logMsg = "UpdateDataCache() - all requested data is already loaded - no data update is required";
 				logger.info(logMsg);
 				performanceLogger.info(logMsg);
 				continue;
 			}
 			
 			// Process offset versions (TTN-1598)
			Map<String, List<String>> dcLoadRemapSpec = new HashMap<String, List<String>>();
 			VersionDef vd = dataCache.getVersionDef(version);
 			if (vd.getType() == VersionType.Offset) {
 				
 				// Get version formula properties
 				VersionFormula vf = vd.getVersionFormula();
 				String baseVersion = vf.getBaseVersionValue(dataCache.getPlanVersion());
 				
 				// Determine the years that need to be loaded versus the year intersections that
 				// only need to be instantiated
 				List<String> requestedYearList = filteredMemberMap.get(yearAxis);
 				List<String> uowYearList = Arrays.asList(dataCache.getYears());
 				List<String> yearsToExtract = new ArrayList<String>(), translatedYears = new ArrayList<String>();
 				for (String requestedYear : requestedYearList) {
 
 					// Calculate the offset version source year		
 					String sourceYear = null;
 					try {
 						sourceYear = vf.calcOffsetVersionSourceYear(requestedYear, mdbYears);
 					} catch (Exception e) {
 						logMsg = "Unable to load MDB data for Offset Version: [" + version + "]. " + e.getMessage();
 						throw new PafException(logMsg, PafErrSeverity.Error);					
 					}
 					
 					// Data for any source years outside the UOW needs to be extracted from Essbase.
 					if (!uowYearList.contains(sourceYear)) {
 						// Source year is outside the UOW. Add it and requested year to collections
 						// for later processing.
 						yearsToExtract.add(sourceYear);
 						translatedYears.add(requestedYear);
 					}
 					
 				}
 
 				// Build the mdx select query that will source the offset version
 				Map<Integer, List<String>> ovExtractMemberMap = null;
 				if (yearsToExtract.size() > 0) {
 					// Build mdx query
 					ovExtractMemberMap = new HashMap<Integer, List<String>> (filteredMemberMap);
 					ovExtractMemberMap.put(versionAxis, new ArrayList<String>(Arrays.asList(new String[]{baseVersion})));
 					ovExtractMemberMap.put(yearAxis, yearsToExtract);
 					mdxSelect = buildMdxSelect(ovExtractMemberMap, dataCache, true);		
 
 					// Create re-mapping member specification for data cache load
 					dcLoadRemapSpec.put(versionDim, new ArrayList<String>(Arrays.asList(new String[]{version})));
 					dcLoadRemapSpec.put(yearDim, translatedYears);
 				} else {
 					// No external data is needed to populate offset version - skip to next version
 					continue;
 				}
 
 			} else {
 				// Not an offset version - just build select query using original member mapping
 				mdxSelect = buildMdxSelect(filteredMemberMap, dataCache, true);	
 			}
 
 
 			// Extract the data for the selected version, suppressing any missing intersection rows
 			if (mdxSelect != null) {
 				// Track loaded data intersections
 				loadedMdbDataSpec.put(version, filteredMemberMap);
 			} else {
 				// Null query indicates that after further member filtering, no queried 
 				// members remained in one or more axes.
 				logMsg = "UpdateDataCache() - all requested data is already loaded - no data update is required";
 				logger.info(logMsg);
 				performanceLogger.info(logMsg);
 				continue;	
 			}
 			String mdxQuery = mdxSelect + mdxFrom + mdxWhere; 
 
 			// Update data cache with Essbase data for selected version
 			int retrievedCells = loadCubeData(esbCubeView, mdxQuery, dataCache, dcLoadRemapSpec);
 			mdxCellCount += retrievedCells;
 
 		}
 		logMsg = "[" + StringUtils.commaFormat(mdxCellCount) + "] total cells retrieved from Essbase.";
 		logger.info(logMsg); 
 		logMsg = LogUtil.timedStep("Data cache load and build", loadDcStartTime);
 		logger.info(logMsg);
 		performanceLogger.info(logMsg);
 
 		// Close cube view
 		logger.debug("Closing cube view...");            
 		if (esbCubeView != null) {
 			esbCubeView.disconnect();
 		}
 
 		// Return map describing which data was actually retrieved from Essbase
 		return loadedMdbDataSpec;
 
     }
 
 
 	/**
 	 * 	Extract Essbase data into the data cache
 	 * 
 	 * @param esbCubeView Essbase cube view that points to the cube being queried
 	 * @param mdxQuery MDX retrieval query
 	 * @param dataCache Data cache
 	 * @param remappedMemberSpec Optional parameter that contains a list of members for any dimension who members are to be re-mapped when loading into the data cache.
 	 * 
 	 * @return Number of retrieved cells
 	 * @throws PafException 
 	 */
 	private int loadCubeData(EsbCubeView esbCubeView, String mdxQuery, PafDataCache dataCache, Map<String, List<String>> remappedMemberSpec) throws PafException {
 		
 		int baseDimCount = dataCache.getBaseDimCount(), retrievedCellCount = 0;
 		String dimensions[] = dataCache.getBaseDimensions();
 		String logMsg = null;
 		PafApplicationDef appDef = dataCache.getAppDef();
 		IEssMdAxis[] axes = null;
 		IEssMdDataSet essMdDataSet = null;
 		IEssMdMember[] essMdMembers = null;
 
 		// Extract Essbase data into data cache using MDX to query the data. Missing data is 
 		// suppressed to optimize retrieval time. 
 		// 
 		// The query will (almost always) return only a subset of the data intersections 
 		// defined to the data cache. However, the results of each MDX query to Essbase 
 		// contain meta-data that indicates which data intersections were retrieved. This 
 		// meta-data is used to define the member intersections to iterate through when 
 		// loading the retrieved data into the data cache.
 
 		try {
 			// Extract data from cube using supplied Mdx query
 			long qryStartTime = System.currentTimeMillis();
 			logger.info("Running Essbse data query: " + mdxQuery);
 			performanceLogger.info("Running Essbse data query: " + mdxQuery);
 			essMdDataSet = esbCubeView.runMdxQuery(mdxQuery, appDef.getEssNetTimeOut());
 			retrievedCellCount = essMdDataSet.getCellCount();
 			logMsg = "Essbase data query returned " + StringUtils.commaFormat(retrievedCellCount) + " cells";
 			logger.debug(logMsg);
 			performanceLogger.info(logMsg);
 			logMsg = LogUtil.timedStep("Essbase data query", qryStartTime);
 			performanceLogger.info(logMsg);
 
 			// Exit method if no data was found
 			if (retrievedCellCount == 0) {
 				return 0;
 			}
 
 			// Get list of retrieved members for each dimension
 			long dcLoadStartTime = System.currentTimeMillis();
 			axes = essMdDataSet.getAllAxes();
 			@SuppressWarnings("unchecked")
 			List<String>[] memberLists = new ArrayList[baseDimCount]; 
 			for (int i = 0; i < baseDimCount; i++) {
 				int memberCount = axes[i].getTupleCount();
 				List<String> memberList  = new ArrayList<String>(memberCount);
 				String dim = axes[i].getAllDimensions()[0].getName();
 				if (!remappedMemberSpec.containsKey(dim)) {
 					// Dimension is not re-mapped - pull list of members from Essbase mdx axis info
 					for (int j = 0; j < memberCount; j++) {
 						essMdMembers = axes[i].getAllTupleMembers(j);
 						memberList.add(essMdMembers[0].getName());
 					}
 				} else {
 					// Dimension is re-mapped - Re-map members used for loading the data cache (TTN-1598)
 					memberList = remappedMemberSpec.get(dim);
 				}
 				memberLists[i] = memberList;
 			}
 
 			// Populate data cache with retrieved Essbase data. Iterate through
 			// all data intersections retrieved from Essbase.
 			int mdxCellIndex = 0;
 			Odometer cellIterator = new Odometer(memberLists);
 			while (cellIterator.hasNext()) { 
 				// Load next data cache cell - Ignore #MISSING values
 				@SuppressWarnings("unchecked")
 				Intersection intersection = new Intersection(dimensions, (String[])cellIterator.nextValue().toArray(new String[0]));
 				// Ignore missing values
 				if (!essMdDataSet.isMissingCell(mdxCellIndex)) {
 					double cellValue = essMdDataSet.getCellValue(mdxCellIndex);
 					dataCache.setCellValue(intersection, cellValue);	
 				}
 				mdxCellIndex++;
 			}
 			logMsg = LogUtil.timedStep("Loading of data to cache", dcLoadStartTime);
 			performanceLogger.info(logMsg);
 
 		} catch (EssException esx) {
 			// throw Paf Exception
 			String errMsg = esx.getMessage();
 			logger.error(errMsg);
 			PafException pfe = new PafException(errMsg, PafErrSeverity.Error, esx);	
 			throw pfe;
 		}
 
 		// Return count of retrieved data cells
 		return retrievedCellCount;
 	}
 
 	
 	/**
 	 *	Build an MDX select statement using the supplied collection of dimension members.
 	 *
 	 *  If the data cache parameter is supplied then virtual and read-only members will
 	 *  be filtered from the resulting query.
 	 *  
  	 * @param dimMembers A map containing a list of members for each axis
  	 * @param dataCache Data cache this required for member filtering
  	 * @param isNonEmptyFlagUsed Indicates that only "non empty" flag should be appended to MDX query
  	 *  	  
  	 * @return Returns an MDX select statement that matches the specified dimensionality.
 	 */
 	public String buildMdxSelect(Map<Integer, List<String>> dimMembers, PafDataCache dataCache, Boolean isNonEmptyFlagUsed) {
 
 		int axis = 0;
 		String mdxSelect = null;
 		StringBuilder sb = new StringBuilder("SELECT ");
 
 		logger.debug("Building MDX Query...");
 		//
 		// SAMPLE MDX QUERY:
 		//
 		// SELECT 	{[NEEDED_CUMREC],[APPROVED_TTL]} ON AXIS(0), 
 		//		 	{[ClassChn]} ON AXIS(1), 
 		//			{[WK01],[WK02],[WK03]} ON AXIS(2), 
 		//			{[LY],[CF]} ON AXIS(3), 
 		//			{[FY2006]} ON AXIS(4), 
 		//			{[DIV09]} ON AXIS(5), 
 		//			{[StoreTotal]} ON AXIS(6) 
 		//
 		
 		
 		// Check for null dimMembers
 		if (dimMembers == null) {
 			String errMsg = "buildMdxQuery() error - dimMembers can not be NULL";
 			logger.error(errMsg);
 			IllegalArgumentException iae = new IllegalArgumentException(errMsg);	
 			throw iae;			
 		}
 		
 	
 		// Build MDX Select
 		for (axis = 0; axis < dimMembers.size(); axis++) {
 			
 			List<String> memberList;
 			
 			// Filter out any synthetic members, if data cache was passed in (TTN-1595)
 			if (dataCache != null) {
 				final List<String> origMemberList = dimMembers.get(axis);
 				String dim = dataCache.getDimension(axis);
 				PafDimMember root = dataCache.getDimTrees().getTree(dim).getRootNode();
 				if (root.getMemberProps().isSynthetic()) {
 					memberList = new ArrayList<String>(origMemberList);
 					memberList.remove(root.getKey());
 					if (memberList.size() == 0) {
 						// No members selected in axis - can't build query
 						return null;
 					}
 				} else {
 					memberList = origMemberList;
 				}
 			} else {
 				memberList = dimMembers.get(axis);	
 			}
 			
 			
 			// Include the optional keywords "Non Empty" before the set specification in each axis in order to suppress 
 			// slices that contain entirely #MISSING values.
 			if(isNonEmptyFlagUsed)
 			{
 				sb.append(" Non Empty ");
 			}
 			
 			sb.append(StringUtils.arrayToString(memberList.toArray(), "{", "}", "[", "]", "," )) ;
 			sb.append(" ON AXIS(" + axis + "), ");
 		}
 		sb.deleteCharAt(sb.lastIndexOf(","));
 		
 		// Return MDX Select
 		mdxSelect = sb.toString();
 		return mdxSelect;
 
 	}
 
 	
 	/**
 	 *	Send data back to Essbase
 	 *
 	 * @param dataCache Data cache - Updated data and associated meta-data
 	 * @param clientState Client state object
 
 	 * @throws PafException
 	 */
 	public void sendData(PafDataCache dataCache, PafClientState clientState) throws PafException {
 		
 		final String dataFilePath = clientState.getTransferDirPath();
 		final String dataFilePrefix = "esb";
 		final String fieldDelim = " ", lineTerm = "\n";
 		final String q = "\"", missingData = "#MI";
 		final String measureDim = dataCache.getMeasureDim();
 		final String timeDim = dataCache.getTimeDim(), versionDim = dataCache.getVersionDim();
 		final String planTypeDim = dataCache.getPlanTypeDim(), yearDim = dataCache.getYearDim();
 		final List<String> pageDims = Arrays.asList(new String[]{planTypeDim, yearDim, versionDim});
 		final List<String> colDims = Arrays.asList(new String[]{timeDim});
 		final List<String> intersectionDims = Arrays.asList(dataCache.getBaseDimensions());
 		final List<String> dummyMemberList = Arrays.asList(new String[]{"[MEMBER]"});
 
 		long sendStart = 0;
 		String dataFileName = null, dataFileShortName = null;
 		String[] planVersions = dataCache.getPlanVersions();
  		FileWriter dataLoadFile = null;
 		
 		EsbCube esbCube = null;
 		IEssCube cube = null;
 		IEssOlapServer olapServer = null;
 		
 		
 		// Selected data in the data cache (e.g. Plan Version(s)) is written back to Essbase in the form of an
 		// Essbase data load. The data is first written to a temporary text file residing on the Pace Server,
 		// using a format that is supported by Essbase for doing data loads without any corresponding load 
 		// rules. This temporary file is then copied into the database directory of the appropriate Essbase
 		// cube. From there, the data is then loaded into Essbase. 
 		//
 		// To minimize the size of the data being transferred to Essbase in a given data load, this process 
 		// cycles through each unique combination of Year, Version, and Plan Type. This allows the Version, 
 		// Year, and Plan Type to be defined as headers in each data load file, instead of being duplicated 
 		// on each data row. This arrangement also makes it easier to handle "locked periods", since the 
 		// evaluation of "locked periods" is based on the unique combination of Year and Version.
 		//
 		// Read-Only & Synthetic members intersections are not written back to Essbase.
 		//
 		// Sample format of Essbase data load is displayed below:
 		//
 		//	"ClassChn" "FY2006" "WP"
 		//	"Measures" "Product" "Location" "S01" "Q1" "Feb" "WK01" "WK02" "WK03" "WK04" "Mar"  
 		//	"SLS_DLR" "DIV01" "Location" -383854.0 -191927.0 -63264.0 -19203.0 -11825.0 -16447.0 -15789.0 -56654.0  
 		//	"MD_DLR" "DIV01" "Location" -37306.14000000001 -18653.070000000003 -5831.29 -1818.9299999999996 -574.5 -230.20999999999998 -82.5 -211.9
 		//	"RECRTL_DLR" "DIV01" "Location" -438250.0 -219125.0 -58805.0 -14460.0 -11400.0 -16380.0 -16565.0 -76755.0 
 		//	"BOPRTL_DLR" "DIV01" "Location" -1468069.6800000002 -734034.8400000001 -189863.50999999998 -53300.0 -1850.0 -3325.0 -2130.0 -2430.0 
 		//	"EOPRTL_DLR" "DIV01" "Location" -1479241.5400000003 -739620.7700000001 -179573.21999999997 -46738.06999999999 -6258.789999999999 -6101.289999999999 -5780.389999999999 -5603.2699999999995 		
 		//	"SLS_DLR" "DPT110" "Location" -51158.0 -25579.0 -8338.0 -2371.0 -1625.0 -2179.0 -2163.0 -7648.0  
 		//	"MD_DLR" "DPT110" "Location" -5005.9800000000005 -2502.9900000000002 -788.7299999999999  -264.12 -755.03 -120.21000000000001 -141.5 -202.4  
 		//	"RECRTL_DLR" "DPT110" "Location" #MI #MI -56100.0 -28050.0 -7630.0 -1760.0 -1550.0 -2070.0 -2250.0 -9735.0  
 		//	"BOPRTL_DLR" "DPT110" "Location" -181453.16 -90726.58 -25240.47 -7100.0 -6258.789999999999 -6101.289999999999 -5780.389999999999 -25965.050000000003  
 		//	"EOPRTL_DLR" "DPT110" "Location" #MI #MI -180610.37999999998 -90305.18999999999 -23743.739999999998 -27297.020000000004 -6212.060000000001 -7045.560000000001  
 		//
 		
 
 		logger.info("Preparing data to be sent to Essbase");
 		sendStart = System.currentTimeMillis();
 		
 		// Create an array list containing all intersections dimensions in the optimal 
 		// Essbase data load order. Measures & Time are set as inner-most dimensions. 
 		// The remaining dimensions are arbitrarily ordered after Measure and Time.
 		//TODO - Pull the list of sparse/dense dimensions from the outline and order accordingly
 		logger.debug("Creating list of iteration dimensions..." );
 		List<String> iteratorDims = new ArrayList<String>();
 		iteratorDims.add(measureDim);
 		iteratorDims.add(timeDim);
 		for (String dim : intersectionDims) {
 			if (!dim.equals(measureDim) && !dim.equals(timeDim)) {
 				iteratorDims.add(dim);
 			}
 		}
 		
 		// Create an ordered list of row dimensions - all dimensions, except the page and column dimensions
 		List<String> rowDims = new ArrayList<String>(iteratorDims);
 		rowDims.removeAll(pageDims);
 		rowDims.removeAll(colDims);
 		
 		// Create a map of member lists to pass to the intersection iterator. The page
 		// and column dimensions each have their own explicit processing loops, so only a
 		// single "dummy" member will be specified for those dimensions. All data cache 
 		// members will be selected for the remaining (row) dimensions, with the
 		// exception of any read-only or synthetic members.
 		logger.debug("Creating member filter map..." );
 		List<String> versionFilter = Arrays.asList(planVersions);
 		Map<String, List<String>> memberListMap = new HashMap<String, List<String>>();
 		for (String dim : pageDims) {
 			memberListMap.put(dim, dummyMemberList);
 		}
 		for (String dim : colDims) {
 			memberListMap.put(dim, dummyMemberList);
 		}
 		// Filter out read-only and synthetic members from row dimensions (TTN-1644).
 		for (String dim : rowDims) {	
 			memberListMap.put(dim, Arrays.asList(dataCache.getFilteredDimMembers(dim, null, false, false)));
 		}
 			
 		// Instantiate row iterator - used to iterate through all row dimension members
 		Odometer rowIterator = dataCache.getCellIterator(iteratorDims.toArray(new String[0]), memberListMap);
 
 					
 		try {
 			
 			// Create Essbase data load file. Create the temporary file directory, if it does not already exist.
 			File fileObject = new File(dataFilePath);
 			fileObject.mkdir();
 			if (!fileObject.exists()) {
 				StringBuffer errMsg = new StringBuffer("Can't create temporary data file, the required file path does not exist and cannot be created [");
 				errMsg.append(fileObject.getAbsolutePath());
 				errMsg.append("]. Data not sent to Essbase.");
 				logger.error(errMsg);
 				throw new IOException(errMsg.toString());			
 			}
 
 			File tempFile = File.createTempFile(dataFilePrefix, ESS_TEXT_FILE_SUFFIX, fileObject);
 			dataFileName = tempFile.getPath();
 			tempFile.deleteOnExit();
 			// Open the cube object, use connection property string if supplied
 			logger.info("Opening cube: [" + esbConnAlias + "]"); 
 			if (connectionProps != null) {
 				esbCube = new EsbCube(connectionProps);				
 			} else {
 				esbCube = new EsbCube(esbConnAlias);				
 			}
 			cube = esbCube.getEssCube();
 			olapServer = esbCube.getOlapServer();
 			
 			// Get list of years - filter out any read only or synthetic members (TTN-1595)
 			String[] years = dataCache.getFilteredDimMembers(yearDim, null, false, false);
 			
 			// Cycle through list of Plan Types
 			String[] planTypes = dataCache.getDimMembers(planTypeDim);
 			for (String planType: planTypes) {
 	
 				// Cycle through list of Years
 				for (String year:years) {
 										
 					// Cycle through list of Versions 
 					for (String version:versionFilter) {
 						logger.info("Creating Essbase Data Load File: Plan Type [" + planType + "] - Year ["
 								+ year + "] - Version [" + version + "]"); 
 						
 						// Open new data load file
 						dataLoadFile = new FileWriter(dataFileName);
 						
 						// Get the list of open (unlocked) periods
 						List<String> openPeriods = dataCache.getOpenPeriods(version, year);
 						int dataCols = openPeriods.size();
 					
 						// Format header definition
 						logger.debug("Formatting file header");
 						dataLoadFile.append(dQuotes(planType) + fieldDelim + dQuotes(year) + fieldDelim + dQuotes(version));
 						dataLoadFile.append(lineTerm);
 											
 						// Format column headers(row dimensions followed by column dimension)
 						logger.debug("Formatting column headers");
 						for (String rowDim : rowDims) {
 								dataLoadFile.append(dQuotes(rowDim) + fieldDelim);								
 						}
 						for (String period:openPeriods) {
 							dataLoadFile.append(dQuotes(period) + fieldDelim);
 						}
 						dataLoadFile.append(lineTerm);
 						
 						// Writing data rows
 						logger.debug("Writing data rows");
 						while (rowIterator.hasNext()) {
 							
 							// Get next row header intersection
 							@SuppressWarnings("unchecked")
 							ArrayList<String> coords = rowIterator.nextValue();
 							Intersection intersection = new Intersection(iteratorDims, coords, intersectionDims);
 							intersection.setCoordinate(versionDim, version);
 							intersection.setCoordinate(planTypeDim, planType);
 							intersection.setCoordinate(yearDim, year);
 							
 							// Get list of members in row header
 							String[] rowMembers = new String[rowDims.size()];
 							int i = 0;
 							for (String rowDim : rowDims) {
 								rowMembers[i++] = intersection.getCoordinate(rowDim);
 							}
 							
 							// Write out row header
 							String rowHeader = StringUtils.arrayToString(rowMembers,"",fieldDelim,q,q,fieldDelim);
 							dataLoadFile.append(rowHeader);
 							
 							// Write out data values
 							for (int dataCol = 0; dataCol < dataCols; dataCol++) {
 								String colMember = openPeriods.get(dataCol);
 								intersection.setCoordinate(timeDim, colMember);
 								double cellValue = dataCache.getCellValue(intersection);
 								if (cellValue != 0) {
 									dataLoadFile.append(String.valueOf(cellValue) + fieldDelim);
 								} else {
 									dataLoadFile.append(missingData + fieldDelim);
 								}
 							}
 							dataLoadFile.append(lineTerm);
 						}
 						dataLoadFile.close();
 
 						// Load data file to Essbase						
 						logger.debug("Copying data file to Essbase Server");
 						dataFileShortName = EsbUtility.copyTextFileToServer(olapServer, cube, tempFile);
 						logger.info("Loading data to Essbase....");
 						cube.loadData(IEssOlapFileObject.TYPE_RULES, null, IEssOlapFileObject.TYPE_TEXT, dataFileShortName, true);
 						logger.info("Data saved: Plan Type [" + planType + "] - Year [" + year + "] - Version [" + version + "]");
 						logger.info("");
 						
 						// Reset the row intersection iterator for the next set of page headers
 						rowIterator.reset();
 					}					
 				}
 			}
 						
 			// Delete data load file from Essbase server
 			logger.debug("Deleting data file from Essbase Server");
 			EsbUtility.deleteServerTextFile(cube, dataFileShortName);
 	
 			// Delete data load file from Paf Server
 			if (!tempFile.delete()) {
 				// Log warning message if file could not be deleted
 				String errMsg = "Unable to delete temporary data load file [" +
 						tempFile.getName() + "] from Paf Server.";
 				logger.warn(errMsg);
 			}
 
 			// Log elapsed time
 			String logMsg = LogUtil.timedStep("Essbase Save", sendStart);
 			logger.info(logMsg);
 			performanceLogger.info(logMsg);
 			
 		} catch (EssException esx) {
 			// Catch essbase exception
 			String errMsg = esx.getMessage();
 			logger.error(errMsg);
 			PafException pfe = new PafException(errMsg, PafErrSeverity.Error, esx);	
 			throw pfe;
 
 		} catch (IOException ioe) {
 			// Catch file i/o exception
 			String errMsg = ioe.getMessage();
 			logger.error(errMsg);
 			PafException pfe = new PafException(errMsg, PafErrSeverity.Error, ioe);	
 			throw pfe;
 
 		} finally {
 			try {
 				// Close cube
 				logger.info("Closing cube: [" + esbConnAlias + "]");            
 				esbCube.disconnect();
 			} catch (PafException pfe) {
 				// throw Paf Exception
 				throw pfe;
 			}		
 		}
 	}
 	
     /**
      *	Get Filtered meta-data from Essbase
      *
 	 * @param expandedUow Fully expanded unit of work
 	 * @param appDef Paf Application Definition
 	 * @param essNetTimeOut Essbase network timeout (in milliseconds)
 
 	 * @throws PafException
      */
 	public PafDimSpec[] getFilteredMetadata(Map<Integer, List<String>> expandedUOW, PafApplicationDef appDef) throws PafException{
 		int axisCount = 0; 
 		int[] axisSize = null;
 		int[] dimCount = null;
 		boolean[] isSlicerAxis = null;
 		String cubeViewName = null;
 		String esbApp = null, esbDb = null;
 		String mdxFrom = null, mdxSelect = null, mdxQuery = null, mdxWhere = "";
 		String dimension = null;
 			
 		EsbCubeView esbCubeView = null;
 		IEssMdAxis[] axes = null;
 		IEssMdDataSet essMdDataSet = null;
 		IEssMdMember[] essMdMembers = null;
 		PafDimSpec[] filteredUow = null;
 		
 		// Retrieve data
 		logger.info("Beginning meta-data filter retrieval" ); 
 		try {
 			//Set return field
 			filteredUow = new PafDimSpec[appDef.getMdbDef().getHierDims().length];
 			
 			// Open a cube view
 			cubeViewName = "Paf View - " + esbConnAlias;
 			logger.info("Opening cube view: " + cubeViewName);   
 			esbCubeView = new EsbCubeView(cubeViewName,  connectionProps, useConnPool, false, false, true);				
 			
 			// Run mdx query with the "Dataless" option set to true. At this point only meta-deta will be generated.
 			// The data will be returned in a separate call in which any versions containing formulas will be removed
 			// from the MDX query.
 			esbApp = esbCubeView.getEsbApp();
 			esbDb = esbCubeView.getEsbDb(); 
 			mdxSelect = buildMdxSelect(expandedUOW, null, true);
 			if (mdxSelect != null) {
 				mdxFrom = " FROM " + esbApp + "." + esbDb;
 				mdxQuery = mdxSelect + mdxFrom + mdxWhere; 
 				logger.info("Running meta-data filter query: " + mdxQuery);
 				essMdDataSet = esbCubeView.runMdxQuery(mdxQuery, true, false, appDef.getEssNetTimeOut());
 			} else {
 				// Null query indicates that after further member filtering, no queried 
 				// members remained in one or more axes.
 				String errMsg = "MDX query Exception: no queried members remain in one or more axes";
 				logger.error(errMsg);
 				PafException pfe = new PafException(errMsg, PafErrSeverity.Error);	
 				throw pfe;
 			}
 			
 			// Determine basic result set statistics.
 			axes = essMdDataSet.getAllAxes();
 			axisCount = axes.length;
 			logger.info("Axis count: " + axisCount);
 			
 			// Check for empty result set
 			if (axisCount == 0) {
 				// Throw Paf Exception
 				String errMsg = "Paf Exception: empty Mdx result set ";
 				logger.error(errMsg);
 				PafException pfe = new PafException(errMsg, PafErrSeverity.Error);	
 				throw pfe;
 			}
 			
 			// Determine axis statistics. 
 			logger.debug("Computing axis statistics..." );
 			axisSize = new int[axisCount];
 			dimCount = new int[axisCount];
 			isSlicerAxis = new boolean[axisCount];
 			
 			//	Cycle through each axis and gather axis statistics
 			int hierDimCount = 0;
 			for (int i = 0; i < axisCount; i++) {
 				
 				// Store the dimension that corresponds to each axis
 				essMdMembers = axes[i].getAllDimensions();
 				dimension = essMdMembers[0].getName();
 				logger.debug("Axis [" + i + "] Dimension: " + dimension);
 				
 				// Determine if we have a slicer axis
 				isSlicerAxis[i] = axes[i].isSlicerAxis();
 				if (isSlicerAxis[i]) {
 					// throw Paf Exception
 					String errMsg = "Mdx query validation error - not all dimensions in cube assigned to an axis";
 					logger.error(errMsg);
 					PafException pfe = new PafException(errMsg, PafErrSeverity.Error);	
 					throw pfe;
 				}
 				
 				// Determine the number of members in each axis
 				axisSize[i] = axes[i].getTupleCount();
 				logger.debug("Axis [" + i + "] Size: " + axisSize[i]);
 				
 				// Store the list of members in each axis
 				logger.debug("Getting member names for axis [" + i + "]");
 				String axisMembers[] = new String[axisSize[i]];
 				for (int j = 0; j < axisSize[i]; j++) {
 					essMdMembers = axes[i].getAllTupleMembers(j);
 					axisMembers[j] = essMdMembers[0].getName();
 				}
 				
 				// Store filtered hierarchical dimension's meta-data for return
 				// There is an assumption that there is only one dimension per axis
 				List<String> baseDimsList = Arrays.asList(appDef.getMdbDef().getHierDims());
 				if( baseDimsList.contains(dimension))
 				{
 					PafDimSpec temp = new PafDimSpec();
 					temp.setDimension(dimension);
 					temp.setExpressionList(axisMembers);
 					filteredUow[hierDimCount++] = temp;
 				}
 				
 				// Determine the number of Essbase dimensions in each axis
 				dimCount[i] = axes[i].getDimensionCount();
 				logger.debug("Axis [" + i + "] Dim Count: " + dimCount[i]);
 			}										
 
 		} catch (EssException esx) {
 			// throw Paf Exception
 			String errMsg = esx.getMessage();
 			logger.error(errMsg);
 			PafException pfe = new PafException(errMsg, PafErrSeverity.Error, esx);	
 			throw pfe;
 		} finally {
 			try {
 				// Close cube view
 				logger.info("Closing cube view...");            
 				if (esbCubeView != null) {
 					esbCubeView.disconnect();
 				}
 			} catch (PafException pfe) {
 				// throw Paf Exception
 				throw pfe;
 			}
 		}
 		
 		return filteredUow;
 	}
 
 	
 	/**
 	 *	Put double quotes ("") around supplied text string.
 	 *
 	 * @param text Text string to put double quotes around
 	 * 
 	 * @return Text string to put double quotes around
 	 */
 	private String dQuotes(String text) {
 		return StringUtils.doubleQuotes(text);
 	}
 
 	/**
 	 *	Run the specified Essbase calc script against the supplied cube
 	 *
 	 * @param cube Essbase cube object
 	 * @param calcScript Name of Essbase calc script
 	 * 
 	 * @return Essbase error message (if null, then method was successful)
 	 */
 	public String runCalcScript(IEssCube cube, String calcScript) {
 		
 		String esbErrorMsg = null;
 		
 		// Run calc script - catch any Essbase exeptions
 		try {
 			cube.calculate(false, calcScript);
 		} catch (EssException esx) {
 			esbErrorMsg = esx.getMessage();
 		}
 		
 		// Return status
 		return esbErrorMsg;
 	}
 
 	/**
 	 *	Run the specified Essbase calc script
 	 *
 	 * @param calcScript Name of Essbase calc script
 	 * 
 	 * @return Essbase error message (if null, then method was successful)
 	 * @throws PafException 
 	 */
 	public String runCalcScript(String calcScript) throws PafException {
 		
 		String esbErrorMsg = null;
 		
 		// Open the Essbase cube
 		logger.info("Opening cube: [" + getDataSourceID() + "]"); 
 		EsbCube esbCube = new EsbCube(getConnectionProps());				
 		IEssCube cube = esbCube.getEssCube();
 	
 		// Run calc script - catch any Essbase exeptions
 		try {
 			cube.calculate(false, calcScript);
 		} catch (EssException esx) {
 			esbErrorMsg = esx.getMessage();
 		}
 		
 		// Close cube
 		logger.info("Closing cube: [" + getDataSourceID() + "]");            
 		esbCube.disconnect();
 		
 		// Return status
 		return esbErrorMsg;
 	}
 
 	/**
 	 *	Run the specified Essbase report script file against the supplied cube
 	 *
 	 * @param cube Essbase cube object
 	 * @param reportScript Name of Essbase report script
 	 * @param outputFilePath Fully qualified name of report output filet
 	 * 
 	 * @return Essbase error message (if null, then method was successful)
 	 * @throws IOException 
 	 */
 	public String runReportScriptFile(IEssCube cube, String reportScriptFile, String outputFilePath) throws IOException {
 		
 		return runReportScript(cube, reportScriptFile, outputFilePath, true);
 	}
 
 	/**
 	 *	Run the specified Essbase report script against the supplied cube
 	 *
 	 * @param cube Essbase cube object
 	 * @param reportScript Name of Essbase report script
 	 * @param outputFilePath Fully qualified name of report output filet
 	 * 
 	 * @return Essbase error message (if null, then method was successful)
 	 * @throws IOException 
 	 */
 	public String runReportScript(IEssCube cube, String reportScript, String outputFilePath) throws IOException {
 		
 		return runReportScript(cube, reportScript, outputFilePath, false);
 	}
 
 	/**
 	 *	Run the specified Essbase report script against the supplied cube
 	 *
 	 * @param cube Essbase cube object
 	 * @param reportScript Name of Essbase report script
 	 * @param outputFilePath Fully qualified name of report output file
 	 * @param isLocalFile Indicates that report script is stored locally, not on the Essbase server
 	 * 
 	 * @return Essbase error message (if null, then method was successful)
 	 * @throws IOException 
 	 */
 	public String runReportScript(IEssCube cube, String reportScript, String outputFilePath, boolean isLocalFile) throws IOException {
 		
 		String esbErrorMsg = null;
 		
 		// Open output file
 		FileWriter outputFile = new FileWriter(outputFilePath);
 		
 		// Run report script - catch any Essbase exeptions
 		try {
 			IEssSequentialIterator output = cube.report(reportScript, true, isLocalFile, true, false);
 			String reportSection = null;
 			while ((reportSection = output.getNextString()) != null)
 		    	// Write out each report section to ouput file
 				outputFile.append(reportSection);
 		} catch (EssException esx) {
 			esbErrorMsg = esx.getMessage();
 		} finally {
 		    // Close all files
 		    if (outputFile != null) {
 		    	outputFile.close();
 		    }			
 		}
 		
 		// Return status
 		return esbErrorMsg;
 	}
 
 	/**
 	 *	Run a tokenized Essbase calc script 
 	 *
 	 * @param calcScript Name of the tokenized calc script
 	 * @param tokenCatalog Client token catalog
 	 * @param clientState Client state object
 	 * 
 	 * @throws PafException
 	 */
 	public void runTokenizedCalcScript(String calcScript, Properties tokenCatalog, IPafClientState clientState) throws PafException {
 		
 		String esbErrorMsg = null;
 		String errMsg = "Error in running Essbase calc script [" + calcScript + "] - ";
 		String tempServerCalcScript = "";
 		long sendStart = 0, sendEnd = 0;
 		float sendElapsed = 0;
 		File tokenizedCalcScript = null, resolvedCalcScript = null;
 		
 		EsbCube esbCube = null;
 		IEssCube cube = null;
 		IEssOlapServer olapServer = null;
 	
 		logger.info("Preparing tokenized calc script [" + calcScript + "] ");
 		sendStart = System.currentTimeMillis();
 	
 		try {
 			// Open the Essbase cube
 			logger.info("Opening cube: [" + getDataSourceID() + "]"); 
 			esbCube = new EsbCube(getConnectionProps());				
 			cube = esbCube.getEssCube();
 			olapServer = esbCube.getOlapServer();
 	
 			// Copy calc script to file
 			logger.debug("Getting tokenized calc script [" + calcScript + "] ");
 			tokenizedCalcScript = EsbUtility.copyCalcScriptToFile(olapServer, cube, calcScript, clientState.getTransferDirPath());
 			
 			// Replacing calc script tokens
 			logger.debug("Resolving tokens on tokenized calc script copy [" + tokenizedCalcScript.getName() + "] ");
 			resolvedCalcScript = EsbUtility.resolveEssbaseScriptTokens(tokenizedCalcScript, tokenCatalog, clientState.getTransferDirPath());
 	
 			// Copy resolved calc script file to server
 			logger.debug("Copying resolved calc script [" + resolvedCalcScript + "] to server");
 			tempServerCalcScript = EsbUtility.copyCalcScriptToServer(olapServer, cube, resolvedCalcScript);
 	
 			// Run the calc script - throw exception if error was found
 			logger.info("Running temporary server calc script [" + tempServerCalcScript + "] ");
 			esbErrorMsg = this.runCalcScript(cube, tempServerCalcScript);
 			if (esbErrorMsg != null) {
 				errMsg += "Calc script error: " + esbErrorMsg;
 				logger.error(errMsg);
 				throw new PafException(errMsg, PafErrSeverity.Error);			
 			}
 		} catch (IOException ioe) {
 			// throw Paf Exception
 			errMsg += ioe.getMessage();
 			logger.error(errMsg);
 			PafException pfe = new PafException(errMsg, PafErrSeverity.Error, ioe);	
 			throw pfe;
 		} catch (EssException esx) {
 			// throw Paf Exception
 			errMsg += esx.getMessage();
 			logger.error(errMsg);
 			PafException pfe = new PafException(errMsg, PafErrSeverity.Error, esx);	
 			throw pfe;
 		} finally {
 			// Delete temporary files from Essbase Server
 			EsbUtility.deleteServerCalcScript(cube, tempServerCalcScript);
 			
 			// Close Essbase Cube
 			logger.info("Closing cube: [" + getDataSourceID() + "]");            
 			esbCube.disconnect();	 		
 		}
 		
 		// Delete temporary files from Paf Server (if not in debug mode)
 		if (!clientState.isDebugMode()) {
 			if (tokenizedCalcScript != null && !tokenizedCalcScript.delete()) {
 				// Log warning message if file could not be deleted
 				errMsg += "Unable to delete temporary tokenized calc script ["
 						+ tokenizedCalcScript.getName() + "] from Paf Server.";
 				logger.warn(errMsg);
 			}
 			if (resolvedCalcScript != null && !resolvedCalcScript.delete()) {
 				// Log warning message if file could not be deleted
 				errMsg += "Unable to delete temporary resolved calc script ["
 						+ resolvedCalcScript.getName() + "] from Paf Server.";
 				logger.warn(errMsg);
 			}
 		}
 		
 		// Log elapsed time
 		sendEnd = System.currentTimeMillis();
 		sendElapsed = (float)(sendEnd - sendStart) / 1000;
 		DecimalFormat decimalFormat = new DecimalFormat("[#,##0.00]");
 		String formattedTime = decimalFormat.format(sendElapsed);
 		logger.info("Calc script [" + calcScript + "] successfully executed - Total elapsed time: " 
 				+ formattedTime + " seconds") ;
 							
 	}
 
 	/**
 	 * Run a tokenized Essbase report script to a file path accessible to
 	 * the Pace server.
 	 * 
 	 * @param reportScript Name of report script to run
 	 * @param outputFile Fully qualified file path of report output file
 	 * @param tokenCatalog Client token catalog
 	 * @param clientState Client state object
 	 * 
 	 * @throws PafException 
 	 */
 	public void runTokenizedReportScript(String reportScript, String outputFile, Properties tokenCatalog, IPafClientState clientState) throws PafException {
 		
 		String esbErrorMsg = null;
 		String errMsg = "Error in running Essbase calc script [" + reportScript + "] - ";
 		long sendStart = 0, sendEnd = 0;
 		float sendElapsed = 0;
 		File tokenizedReportScript = null, resolvedReportScript = null;
 		
 		EsbCube esbCube = null;
 		IEssCube cube = null;
 		IEssOlapServer olapServer = null;
 	
 		logger.info("Preparing tokenized report script [" + reportScript + "] ");
 		sendStart = System.currentTimeMillis();
 	
 		try {
 			// Open the Essbase cube
 			logger.info("Opening cube: [" + getDataSourceID() + "]"); 
 			esbCube = new EsbCube(getConnectionProps());				
 			cube = esbCube.getEssCube();
 			olapServer = esbCube.getOlapServer();
 	
 			// Copy report script to temporary file
 			logger.debug("Getting tokenized report script [" + reportScript + "] ");
 			tokenizedReportScript = EsbUtility.copyReportScriptToFile(olapServer, cube, reportScript, clientState.getTransferDirPath());
 			
 			// Replacing report script tokens
 			logger.debug("Resolving tokens on tokenized report script copy [" + tokenizedReportScript.getName() + "] ");
 			resolvedReportScript = EsbUtility.resolveEssbaseScriptTokens(tokenizedReportScript, tokenCatalog, clientState.getTransferDirPath());
 	
 			// Run the resolved report script - throw exception if error was found
 			String resolvedScriptName = resolvedReportScript.getCanonicalPath();
 			logger.info("Running resolved report script [" + resolvedScriptName + "] ");
 			esbErrorMsg = this.runReportScriptFile(cube, resolvedScriptName, outputFile);
 			if (esbErrorMsg != null) {
 				errMsg += "Report script error: " + esbErrorMsg;
 				logger.error(errMsg);
 				throw new PafException(errMsg, PafErrSeverity.Error);			
 			}
 		} catch (IOException ioe) {
 			// throw Paf Exception
 			errMsg += ioe.getMessage();
 			logger.error(errMsg);
 			PafException pfe = new PafException(errMsg, PafErrSeverity.Error, ioe);	
 			throw pfe;
 		} catch (EssException esx) {
 			// throw Paf Exception
 			errMsg += esx.getMessage();
 			logger.error(errMsg);
 			PafException pfe = new PafException(errMsg, PafErrSeverity.Error, esx);	
 			throw pfe;
 		} finally {
 			// Close Essbase Cube
 			logger.info("Closing cube: [" + getDataSourceID() + "]");            
 			esbCube.disconnect();	 		
 		}
 		
 		// Delete temporary files from Paf Server (if not in debug mode)
 		if (!clientState.isDebugMode()) {
 			if (tokenizedReportScript != null && !tokenizedReportScript.delete()) {
 				// Log warning message if file could not be deleted
 				errMsg += "Unable to delete temporary tokenized report script ["
 						+ tokenizedReportScript.getName() + "] from Paf Server.";
 				logger.warn(errMsg);
 			}
 			if (resolvedReportScript != null && !resolvedReportScript.delete()) {
 				// Log warning message if file could not be deleted
 				errMsg += "Unable to delete temporary resolved report script ["
 						+ resolvedReportScript.getName() + "] from Paf Server.";
 				logger.warn(errMsg);
 			}
 		}
 		
 		// Log elapsed time
 		sendEnd = System.currentTimeMillis();
 		sendElapsed = (float)(sendEnd - sendStart) / 1000;
 		DecimalFormat decimalFormat = new DecimalFormat("[#,##0.00]");
 		String formattedTime = decimalFormat.format(sendElapsed);
 		logger.info("Report script [" + reportScript + "] successfully executed - Total elapsed time: " 
 				+ formattedTime + " seconds") ;
 							
 	}
 
 	/**
 	 * @return Returns the connectionProps.
 	 */
 	protected Properties getConnectionProps() {
 		return connectionProps;
 	}
 
 	/**
 	 * @return Returns the dataSourceID.
 	 */
 	public String getDataSourceID() {
 		return dataSourceID;
 	}
 
 
 	/**
 	 * @return Returns the useConnPool.
 	 */
 	public boolean isUseConnPool() {
 		return useConnPool;
 	}
 
 
 }
