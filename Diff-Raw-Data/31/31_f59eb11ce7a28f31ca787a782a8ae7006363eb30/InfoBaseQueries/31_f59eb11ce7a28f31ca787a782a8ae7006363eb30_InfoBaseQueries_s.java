 package org.alt60m.ministry.servlet;
 
 import java.util.*;
 
 import org.alt60m.ministry.model.dbio.*;
 
 import java.sql.Connection;
 import java.sql.Statement;
 import java.sql.ResultSet;
 import org.alt60m.util.*;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 public class InfoBaseQueries {
 	private static Log log = LogFactory.getLog(InfoBaseController.class);
 	public static Vector getLocalLevelTeamsByRegion(String region) {
 		LocalLevel ll = new LocalLevel();
 		Vector v = ll.selectList("((hasMultiRegionalAccess='T' AND isActive = 'T') OR (region = '" + region + "' AND isActive = 'T')) ORDER BY name");
 		return v;
 	}
 	
 	public static Vector getActiveLocalLevelTeams() {
 		LocalLevel ll = new LocalLevel();
 		Vector v = ll.selectList("isActive = 'T' ORDER BY name");
 		return v;
 	}
 
 	public static String getCampusesForStaffTeam(String accountno) throws Exception {
 		Connection conn = DBConnectionFactory.getDatabaseConn();
 		Statement stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
 		
 		String qry = "SELECT name FROM ministry_targetarea ta " +
 						"INNER JOIN ministry_activity act ON act.fk_targetAreaID=ta.TargetAreaID " +
 						"INNER JOIN ministry_staff staff ON staff.fk_teamID=act.fk_teamID " +
 						"WHERE staff.accountNo='"+accountno+"'";
 						
 		ResultSet rs = stmt.executeQuery(qry);
 
 		return TextUtils.listToCommaDelimitedQuotedString(rs, "'");
 	}
 	
 	public static Vector getRegionalStats(String region, Date periodBegin, Date periodEnd) {
 				
 		RegionalTeam rt = new RegionalTeam();
 		rt.setAbbrv(region);
 		rt.select();
 		String regionalTeamId = rt.getRegionalTeamId();
 		
 		RegionalStat stat = new RegionalStat();
 		return stat.selectList("fk_RegionalTeamID = '" + regionalTeamId + "'"+
 						   " AND periodBegin > '" + org.alt60m.util.DateUtils.toSQLDate(periodBegin) + "'"+
 						   " AND periodEnd < '" + org.alt60m.util.DateUtils.toSQLDate(periodEnd) + "'"+
 						   " ORDER BY periodEnd ASC");		
 	}
 
 	public static RegionalTeam getRegionalTeam(String region) {
 		RegionalTeam rt = new RegionalTeam();
 		rt.setRegion(region);
 		rt.select();
 		return rt;
 	}
 	
 	public static Vector listStaffByLastName(String subString) {
 		subString = subString.replaceAll("'", "''");  // Takes care of problem apostrophes in subString
 		Staff s = new Staff();
 		return s.selectList("UPPER(lastName) like '"+subString.toUpperCase()+"%' AND (removedFromPeopleSoft='N') ORDER BY lastName, firstName");
 	}
 	
 	public static Collection listStaffHashByRegion(String region) {
 		try {
 			return org.alt60m.util.ObjectHashUtil.list(listStaffByRegion(region));
 		} catch (Exception e) {
 			log.error(e, e);
 			return null;
 		}
 	}
 	public static Collection shortListStaffHashByRegion(String region) {
 		try {
 			return org.alt60m.util.ObjectHashUtil.list(shortListStaffByRegion(region));
 		} catch (Exception e) {
 			log.error(e, e);
 			return null;
 		}
 	}
 	@SuppressWarnings("unchecked")
 	public static Collection<Staff> listStaffByRegion(String region) {
 		try {
 			Staff s = new Staff();
 			return (Collection<Staff>) s.selectList("region = '" + region.toUpperCase() + "' AND (removedFromPeopleSoft='N') ORDER BY lastName");
 		} catch (Exception e) {
 			log.error(e, e);
 			return null;
 		}
 	}
 	public static Collection<ShortStaff> shortListStaffByRegion(String region) {
 		try {
 			ShortStaff s = new ShortStaff();
 			return (Collection<ShortStaff>) s.selectList("region = '" + region.toUpperCase() + "' AND (removedFromPeopleSoft='N') ORDER BY lastName");
 		} catch (Exception e) {
 			log.error(e, e);
 			return null;
 		}
 	}
 	public static Collection<Hashtable<String, Object>> listStaffHashByLastName(String lastName) {
 		try {
 			Staff s = new Staff();
 			return org.alt60m.util.ObjectHashUtil.list(s.selectList("UPPER(lastName) like '" + lastName.toUpperCase() + "%' AND (removedFromPeopleSoft='N') ORDER BY lastName, firstName"));
 		} catch (Exception e) {
 			log.error(e, e);
 			return null;
 		}
 	}	
 	
 	@SuppressWarnings("unchecked")
 	public static Vector<Statistic> listStatsForTargetArea(String targetAreaId, Date start, Date end) {
 		Activity a = new Activity();
 		a.setTargetAreaId(targetAreaId);
 		Vector activityList = a.selectList("fk_targetAreaID IN (" + targetAreaId + ")");
 		List<String> activityIdList = new Vector<String>();
 		for (Iterator i = activityList.iterator(); i.hasNext();) {
 			Activity act = (Activity)i.next();
 			activityIdList.add(act.getActivityId()); 
 		}
 		String activityIdString = org.alt60m.util.TextUtils.listToCommaDelimitedQuotedString(activityIdList);
 		Statistic stat = new Statistic();
 		Vector<Statistic> v = (Vector<Statistic>) stat.selectList("fk_Activity IN (" + activityIdString + ") "
 										+ "AND periodBegin >= '" + org.alt60m.util.DateUtils.toSQLDate(start) + "' "
 										+ "AND periodEnd <= '" + org.alt60m.util.DateUtils.toSQLDate(end) + "' "
 										+ "ORDER BY periodEnd");
 		return  v;
 	}
 	
 	@SuppressWarnings("unchecked")
	public static Vector listStatsForTargetArea(String targetAreaId, Date start, Date end, List strategies){
		String strategyList = org.alt60m.util.TextUtils.listToCommaDelimitedQuotedString(strategies);
 		Activity a = new Activity();
 		Vector activityList = a.selectList("fk_targetAreaID = " + targetAreaId + " " +
											"AND strategy in (" + strategyList + ")");
 		List<String> activityIdList = new Vector<String>();
 		for (Iterator i = activityList.iterator(); i.hasNext();) {
 			Activity act = (Activity)i.next();
 			activityIdList.add(act.getActivityId()); 
 		}
 		String activityIdString = org.alt60m.util.TextUtils.listToCommaDelimitedQuotedString(activityIdList);
 		Statistic stat = new Statistic();
  		Vector<Statistic> v = (Vector<Statistic>) stat.selectList("fk_Activity IN (" + activityIdString + ") "
 										+ "AND periodBegin >= '" + org.alt60m.util.DateUtils.toSQLDate(start) + "' "
 										+ "AND periodEnd <= '" + org.alt60m.util.DateUtils.toSQLDate(end) + "' "
 										+ "ORDER BY periodEnd");
  		return  v;
 	}
 	
 	public static int getActivityCount() {
 		try{
 			Activity act = new Activity();
 			String qry = "SELECT count(*) from ministry_activity WHERE status in ('AC', 'PI', 'KE', 'LA', 'TR')";
 			return org.alt60m.util.ObjectHashUtil.countIt(act, qry);
 		} catch (Exception e) {
 			log.error(e, e);
 			return 0;
 		}
 	}
 	public static int getActivityCountCurrent() {
 		try{
 			Activity act = new Activity();
 			String qry = "SELECT count(*) from ministry_activity WHERE status in ('AC', 'LA', 'TR')";
 			return org.alt60m.util.ObjectHashUtil.countIt(act, qry);
 		} catch (Exception e) {
 			log.error(e, e);
 			return 0;
 		}
 	}
 	
 	public static int getReportedCnt(Date since) {
 		try{
 			Activity act = new Activity();
 			String qry = "SELECT count(*) from ministry_activity WHERE periodBegin > '" + org.alt60m.util.DateUtils.toSQLDate(since) + "'";
 			return org.alt60m.util.ObjectHashUtil.countIt(act, qry);
 		} catch (Exception e) {
 			log.error(e, e);
 			return 0;
 		}
 	}
 	
 	public static Vector getAllTargetAreasByRegion(String region) {
 		TargetArea ta = new TargetArea();
 		return ta.selectList("UPPER(region) like '%"+region.toUpperCase()+"%' "
 				+ "and (isClosed<> 'T' or isClosed is NULL) ORDER BY name");
 	}
 	public static Vector getAllTargetAreasByState(String state) {
 		TargetArea ta = new TargetArea();
 		return ta.selectList("UPPER(state) like '%"+state.toUpperCase()+"%' "
 				+ "and (isClosed<> 'T' or isClosed is NULL) ORDER BY name");
 	}
 	
 	
 	public static Vector getAllTargetAreasByStateAndZip(String state, int number) {
 		TargetArea ta = new TargetArea();
 		
 		//prints out the rest of the CA area besides San Fransisco Bay and LA / San Diego Area
 		if(state.equals("CA") && number == 3){
 			return ta.selectList("UPPER(state)like '%"+state.toUpperCase()+"%' "
 				+ " and (zip <= " + getHigherBoundZipCode(state, number)
 				+ " and zip > " + getLowerBoundZipCode(state, number)
  				+ ") OR (zip <= " + getHigherBoundZipCode(state, number+1)
 				+ " and zip > " + getLowerBoundZipCode(state, number+1)
 				+ ")and (isClosed<> 'T' or isClosed is NULL) ORDER BY name");
 		}
 		
 		//prints out the rest of the New York Area (besides Metro)
 		else if(state.equals("NY") && number == 2) {
 			
 			return ta.selectList("UPPER(state)like '%"+state.toUpperCase()+"%' "
 					+ " and zip <= " + getHigherBoundZipCode(state, number)
 					+ " and zip > " + getLowerBoundZipCode(state, number)
 					+ "and (isClosed<> 'T' or isClosed is NULL) ORDER BY name");
 		}
 	
 		//works for all test cases
 		else {
 			return ta.selectList("UPPER(state)like '%"+state.toUpperCase()+"%' "
 					+ " and zip <= " + getHigherBoundZipCode(state, number)
 					+ " and zip > " + getLowerBoundZipCode(state, number)
 					+ "and (isClosed<> 'T' or isClosed is NULL) ORDER BY name");
 		}
 	}
 
 	public static Hashtable<String,Integer> getActivityCountByRegionAndStrategies(String region, Collection<String> strategies) {
 		try{
 			String queryPortion="and strategy in (";
 			Iterator strategIt=strategies.iterator();
 			while(strategIt.hasNext())
 			{
 			queryPortion+="'"+strategIt.next()+"'";
 			if (strategIt.hasNext()) queryPortion+=", ";
 			}
 			queryPortion+=") and region in (";
 			if(!region.equals("National")){
 				queryPortion+="'"+region+"'";
 			}
 			else
 			{
 				queryPortion+="'NE', 'SW', 'SE', 'NW', 'UM', 'GL', 'GP', 'RR', 'MA', 'MS'";
 			}
 			queryPortion+=")";
 			
 			
 			String movementsQuery = "SELECT count(ActivityID) from ministry_activity INNER JOIN ministry_targetarea on ministry_activity.fk_targetAreaID=ministry_targetarea.targetAreaID WHERE status in ('AC', 'LA', 'TR') "+queryPortion+" group by ActivityID ;";
 			Connection conn = DBConnectionFactory.getDatabaseConn();
 			Statement movementsStmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
 			ResultSet movementsRS = movementsStmt.executeQuery(movementsQuery);
 			Hashtable<String,Integer> result=new Hashtable<String,Integer>();
 			result.put("movements", 0);
 			while(movementsRS.next()){
 				result.put("movements", result.get("movements")+movementsRS.getInt(1));
 			}
 			
 			String enrollmentQuery = "SELECT Max(enrollment) from ministry_activity INNER JOIN ministry_targetarea on ministry_activity.fk_targetAreaID=ministry_targetarea.targetAreaID WHERE status in ('AC', 'LA', 'TR') "+queryPortion+" group by targetAreaID ;";
 			Statement enrollmentStmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
 			ResultSet enrollmentRS = enrollmentStmt.executeQuery(enrollmentQuery);
 			result.put("enrollment", 0);
 			while(enrollmentRS.next()){
 				result.put("enrollment", result.get("enrollment")+enrollmentRS.getInt(1));
 			}
 			
 			return result;
 			
 		} catch (Exception e) {
 			log.error(e, e);
 			return null;
 		}
 	}
 	public static String getLowerBoundZipCode(String state, int number) {
 		if(state.equals("CA")) {
 			if(number == 1) {return "'94000'";}//san fransisco bay area
 			if(number == 2) {return "'90000'";}//LA / San Diego Area
 			if(number == 3) {return "'93000'";}//other area
 			if(number == 4) {return "'95000'";}//other area
 		}
 		
 		if(state.equals("NY")) {
 			if(number == 1) {return "'10000'";}//New York City Metro
 			if(number == 2) {return "'12700'";}//area for non metro
 		}
 		
 		return null;
 	}
 
 	public static String getHigherBoundZipCode(String state, int number) {	
 		if(state.equals("CA")) {
 			if(number == 1) {return "'94999'";}//san fransisco bay area
 			if(number == 2) {return "'92999'";}//LA / San Diego Area
 			if(number == 3) {return "'93999'";}//other area
 			if(number == 4) {return "'95999'";}//other area
 		}
 		
 		if(state.equals("NY")) {
 			if(number == 1) {return "'12699'";}//New York City Metro
 			if(number == 2) {return "'14999'";}//area for non metro
 		}
 		
 		return null;
 		
 	}
 	
 	
 	public static Vector getAllTargetAreasByName(String name) {
 		TargetArea ta = new TargetArea();
 		return ta.selectList("UPPER(name) like '%"+name.toUpperCase()+"%' "
 				+ "and (isClosed<> 'T' or isClosed is NULL) ORDER BY name");
 	}
 	public static Vector getAllTargetAreasByCity(String city) {
 		TargetArea ta = new TargetArea();
 		return ta.selectList("UPPER(city) like '%"+city.toUpperCase()+"%' "
 				+ "and (isClosed<> 'T' or isClosed is NULL) ORDER BY city");
 	}
 	public static Vector getAllTargetAreasByZip(String zip) {
 		TargetArea ta = new TargetArea();
 		return ta.selectList("UPPER(zip) like '%"+zip.toUpperCase()+"%' "
 				+ "and (isClosed<> 'T' or isClosed is NULL) ORDER BY zip");
 	}
 	public static Vector getAllTargetAreasByCountry(String country) {
 		TargetArea ta = new TargetArea();
 		return ta.selectList("UPPER(country) like '%"+country.toUpperCase()+"%' "
 				+ "and (isClosed<> 'T' or isClosed is NULL) ORDER BY country");
 	}
 	public static Vector getNonSecureTargetAreasByRegion(String region) {
 		TargetArea ta = new TargetArea();
 		return ta.selectList("UPPER(region) like '%"+region.toUpperCase()+"%' "
 				+ "and (isClosed<> 'T' or isClosed is NULL) and isSecure = 'F' ORDER BY name");
 	}
 	public static Vector getNonSecureTargetAreasByState(String state) {
 		TargetArea ta = new TargetArea();
 		return ta.selectList("UPPER(state) like '%"+state.toUpperCase()+"%' "
 				+ "and (isClosed<> 'T' or isClosed is NULL) and isSecure = 'F' ORDER BY name");
 	}
 	public static Vector getNonSecureTargetAreasByName(String name) {
 		TargetArea ta = new TargetArea();
 		return ta.selectList("UPPER(name) like '%"+name.toUpperCase()+"%' "
 				+ "and (isClosed<> 'T' or isClosed is NULL) and isSecure = 'F' ORDER BY name");
 	}
 
 	public static Vector getNonSecureTargetAreasByCity(String city) {
 		TargetArea ta = new TargetArea();
 		return ta.selectList("UPPER(city) like '%"+city.toUpperCase()+"%' "
 				+ "and (isClosed<> 'T' or isClosed is NULL) and isSecure = 'F' ORDER BY city");
 	}
 	public static Vector getNonSecureTargetAreasByZip(String zip) {
 		TargetArea ta = new TargetArea();
 		return ta.selectList("UPPER(zip) like '%"+zip.toUpperCase()+"%' "
 				+ "and (isClosed<> 'T' or isClosed is NULL) and isSecure = 'F' ORDER BY zip");
 	}
 	public static Vector getNonSecureTargetAreasByCountry(String country) {
 		TargetArea ta = new TargetArea();
 		return ta.selectList("UPPER(country) like '%"+country.toUpperCase()+"%' "
 				+ "and (isClosed<> 'T' or isClosed is NULL) and isSecure = 'F' ORDER BY country");
 	}
 	public static Vector getTargetAreasByRegion(String region, boolean showSecureCampuses, boolean showNeedsApproval) {
 		TargetArea ta = new TargetArea();
 		return ta.selectList("region = '" + region + "'" +
 								(showSecureCampuses ? " AND isSecure = 'T'" : "") +
 								(showNeedsApproval ? "" : " AND isApproved = 'T'") +
 								"and (isClosed<> 'T' or isClosed is NULL) ORDER BY name");
 	}
 	public static Vector getTargetAreasByRegion(String region) {
 		return getTargetAreasByRegion(region, false, false);
 	}
 	public static Vector getActivityForTargetAreaByStrategy(String targetAreaId, String strategy) {
 		Activity a = new Activity();
 		a.setTargetAreaId(targetAreaId);
 		a.setStrategy(strategy);
 		return a.selectList();
 	}
 	
 	public static boolean doesStrategyExistForTargetArea(String targetAreaId, String strategy) {
 		Activity a = new Activity();
 		a.setTargetAreaId(targetAreaId);
 		a.setStrategy(strategy);
 		Vector v1 = a.selectList();
 		if(!v1.isEmpty()) {
 			return true;
 		}
 		else {
 			return false;
 		}
 	}
 	
 	public static boolean doesStatusExistForTargetArea(String targetAreaId, String status) {
 		Activity a = new Activity();
 		a.setTargetAreaId(targetAreaId);
 		a.setStatus(status);
 		Vector v1 = a.selectList();
 		if(!v1.isEmpty()) {
 			return true;
 		}
 		else {
 			return false;
 		}
 	}
 	
 	public static int isCampusActive(Hashtable h) {
 		
 		   if(doesStrategyExistForTargetArea( (String)h.get("TargetAreaID"),"CA")){
 			   if(    doesStatusExistForTargetArea( (String)h.get("TargetAreaID"),"TR")
 				   || doesStatusExistForTargetArea( (String)h.get("TargetAreaID"),"LA") 
 				   || doesStatusExistForTargetArea( (String)h.get("TargetAreaID"),"KE")      ){
 				   
 				   return 1; }
 		   }
 
 		   else if(doesStatusExistForTargetArea( (String)h.get("TargetAreaID"),"AC")){
 			   if(    doesStrategyExistForTargetArea( (String)h.get("TargetAreaID"),"SC")
 				   || doesStrategyExistForTargetArea( (String)h.get("TargetAreaID"),"II")
 				   || doesStrategyExistForTargetArea( (String)h.get("TargetAreaID"),"IE")
 				   || doesStrategyExistForTargetArea( (String)h.get("TargetAreaID"),"ID")   ){
 				   
 				   return 1; }
 			   
 		
 		   }
 		   
 		   return 0;
 		   
 	}
 	
 }
