 package org.alt60m.ministry.bean;
 
 import java.sql.*;
 import java.util.*;
 //import org.alt60m.ministry.*;
 import org.alt60m.util.DBConnectionFactory;
 import org.alt60m.util.ObjectHashUtil;
 import org.alt60m.ministry.model.dbio.*;
 //import org.alt60m.util.TextUtils;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 /**
  * Staff Updater
  *		
  * @author Mark Petrotta
  * 
  */
 
 public class StaffUpdater {
 	private static Log log = LogFactory.getLog(StaffUpdater.class);
 	
 	private static final String PS_EMPL_TBL = "sysadm.PS_EMPLOYEES";
     private static final String PS_TAX_TBL = "sysadm.PS_TAX_LOCATION1";
     private static final String PS_EMPL_ID  = "emplid";
     private static final String STAFF_TBL = "ministry_staff";
 
     Connection _connection;
     boolean _verbose = true;
     boolean _stopOnFail = false;
 	Hashtable<String, String> luMinistry = new Hashtable<String, String>();
 	Hashtable<String, String> luStrategy = new Hashtable<String, String>();
     Hashtable<String, String> luRegion = new Hashtable<String, String>();
     Hashtable<String, String> luRespScope = new Hashtable<String, String>();
 
 
 
     public StaffUpdater() {    }
     private void initMinistry() throws Exception {
 		log.info("initializing Ministry...");
 	    Connection conn = org.alt60m.util.DBConnectionFactory.getOracleDatabaseConn();
 	    Statement statement = conn.createStatement();
 	    ResultSet rs;
 
 	    String qry = "SELECT SYSADM.PS_CCC_MINISTRIES.CCC_MINISTRY, SYSADM.PS_DEPT_TBL.DESCR FROM SYSADM.PS_CCC_MINISTRIES, SYSADM.PS_DEPT_TBL where SYSADM.PS_CCC_MINISTRIES.CCC_MINISTRY = SYSADM.PS_DEPT_TBL.DEPTID";
 
 		log.info("preparing to execute query...");
 	    rs = statement.executeQuery(qry);
 		log.info("...executed.");
 
 	    while (rs.next()) {
 	        String abbr = new String(rs.getString("ccc_ministry"));
 	        String desc = new String(rs.getString("descr"));
 
 	        luMinistry.put(abbr, desc);
 	    }
         luMinistry.put("CM", "Campus Ministry"); //kb 11/14 - moved out of above loop
 	    conn.close();
 		log.info("...done initializing ministry.");
     }
 
 	public void initRegion() throws Exception {
 		luRegion.put("CGL", "GL");
 		luRegion.put("CGN", "NW");
 		luRegion.put("CMIDA", "MA");
 		luRegion.put("CMIDS", "MS");
 		luRegion.put("CMNCO", "NC");
 		luRegion.put("CNE", "NE");
 		luRegion.put("CPS", "SW");
 		luRegion.put("CRR", "RR");
 		luRegion.put("CSE", "SE");
 		luRegion.put("CUPM", "UM");
 		luRegion.put("CWC", "GP");
 		luRegion.put("CGP", "GP");
 	}
 
 	public void initStrategy() throws Exception {
 		luStrategy.put("CMP", "Staffed");
 		luStrategy.put("CAT", "Catalytic");
 		luStrategy.put("OPS", "Operations");
 		luStrategy.put("FDV", "Fund Dev");
 		luStrategy.put("ND", "National Director");
 	}
 
     public void initRespScope() throws Exception {
 		log.info("initializing RespScope...");
 		Connection conn = org.alt60m.util.DBConnectionFactory.getOracleDatabaseConn();
 		Statement statement = conn.createStatement();
 		ResultSet rs;
 	
 		String qry = "SELECT FIELDVALUE, XLATLONGNAME FROM SYSADM.PSXLATITEM where FIELDNAME = 'RESPONS_SCOPE'";
 	
 		log.info("preparing to execute query...");
 		rs = statement.executeQuery(qry);
 		log.info("...executed.");
 	
 		while (rs.next()) {
 		    String abbr = new String(rs.getString("fieldvalue"));
 		    String desc = new String(rs.getString("xlatlongname"));
 			luRespScope.put(abbr, desc);
 		}
 		conn.close();
 		log.info("...done initializing RespScope.");  
     }
     
 /*    public void initWorkLoc() throws Exception {
     	log.info("initializing WorkLoc");
     	Connection conn = org.alt60m.util.DBConnectionFactory.getOracleDatabaseConn();
     	Statement statement = conn.createStatement();
     	ResultSet rs;
     	
     	String qry = "SELECT TAX_LOCATION_CD, DESCR FROM SYSADM.TAX_LOCATION1";
     	
     	log.info("preparing to execute query...");
     	rs = statement.executeQuery(qry);
     	log.info("...executed.");
     	
     	while (rs.next()) {
     		String abbr = rs.getString("tax_location_cd");
     		String desc = rs.getString("descr");
     		luWorkLoc.put(abbr, desc);
     	}
     	conn.close();
     	log.info("...done initializing WorkLoc");
     }
 */
     public void performUpdate() {
 	try {
 	    _connection = org.alt60m.util.DBConnectionFactory.getOracleDatabaseConn(); //DriverManager.getConnection(CONNECTION_INFO, USERNAME, PASSWORD); 
 			 
 	    if (_connection != null) {
 			log.info("Connected.");
 				try {
 					initMinistry();
 					initRegion();
 					initStrategy();
 					initRespScope();
 
 					log.info("[" + new java.util.Date().toString() + "] Beginning update ...");
 					updateStaffObjects();
 					log.info("[" + new java.util.Date().toString() + "] Update complete.");
 
 					_connection.close();
 				} catch (Exception e) {
 					log.error("An error occured while updating: " + e.toString(),e);
 				}
 			} else {
 				log.error("The connection is null");
 			}
 		} catch (Exception e) {
 			log.error("Error opening database connection",e);				
 		}
     }
     
 	private void insertStaffObjects() throws Exception {
 		log.info("making Oracle connection ... ");
 		Connection psconn = org.alt60m.util.DBConnectionFactory.getOracleDatabaseConn();
 		Statement psstatement = psconn.createStatement();
 		
 		log.info("making sql connection ... ");
 		Connection sqlconn = org.alt60m.util.DBConnectionFactory.getDatabaseConn();
 		Statement sqlstatement = sqlconn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
 		
 		ResultSet sqlrs;
 		ResultSet psrs;
 
 		log.info("[" + new java.util.Date() + "] Querying...");
 
 		String qry = "select distinct accountNo from " + STAFF_TBL + " order by accountNo desc";
 		log.info(qry);
 		sqlrs = sqlstatement.executeQuery(qry);
 		ArrayList<String> sqlResults = new ArrayList<String>();
 		while (sqlrs.next()) {
 			sqlResults.add(sqlrs.getString("accountNo"));
 		}
 
 		String psqry = "select distinct "+PS_EMPL_ID+" from "+PS_EMPL_TBL+" order by "+PS_EMPL_ID+" desc";
 		log.info(psqry);
 		psrs = psstatement.executeQuery(psqry);	
 		ArrayList<String> psResults = new ArrayList<String>();
 		while (psrs.next()) {
 			psResults.add(psrs.getString(PS_EMPL_ID));
 		}
 		
 		Object[] sql = sqlResults.toArray();
 		Object[] ps = psResults.toArray();
 		
 		int sqlSize = sqlResults.size();
 		int psSize = psResults.size();
 		
 		log.debug("sqlSize: "+sqlSize+", psSize: "+psSize);
 		
 		int i = 0;
 		int j = 0;
 		
 		Vector<String> inserts = new Vector<String>();
 		Vector<String> removals = new Vector<String>();
 		while (!((i+1) > sqlSize) && !((j+1) > psSize)) {
 			String accountNo = (String)sql[i];
 			String psAccount = (String)ps[j];
 			if (accountNo.equals(psAccount)) {
 				if (i<sqlSize) { i++; } 
 				if (j<psSize) { j++; }
 				continue;
 			} else if (psAccount.compareTo(accountNo)>0) {
 				inserts.add(psAccount);
 				if (j<psSize) j++;
 				continue;
 			} else if (psAccount.compareTo(accountNo)<0) {
 				removals.add(accountNo);
 				if (i<sqlSize) i++;
 				continue;
 			}
 		}
 
 		Iterator<String> k = inserts.iterator();
 		while (k.hasNext()) {
 			String staffID = (String) k.next();
 			try {
 				log.info("inserting : "+staffID);
 				String insertQuery = "insert into ministry_staff (accountNo) values ('"+staffID+"')";
 				sqlstatement.executeUpdate(insertQuery);				
 				log.info("inserted successfully : "+staffID);
 			} catch (Exception e) {
 				log.error(
 					"[" + new java.util.Date() + "] Failed to insert account: " + staffID, e);
 			}
 		}
 		
 		k = removals.iterator();
 		while (k.hasNext()) {
 			String staffID = (String) k.next();
 			try {
 				log.info("removing : "+staffID);
 				String removeQuery = "update ministry_staff set removedFromPeopleSoft = 'Y' where accountNo = '"+staffID+"'";
 				sqlstatement.executeUpdate(removeQuery);				
 				log.info("removed successfully : "+staffID);
 			} catch (Exception e) {
 				log.error(
 					"[" + new java.util.Date() + "] Failed to remove account: " + staffID, e);
 			}
 		}
 		
 		
 		
 		psrs.close();
 		sqlrs.close();
 
 		psstatement.close();
 		sqlstatement.close();
 		
 		psconn.close();
 		sqlconn.close();
 	}
 
     private void updateStaffObjects() throws Exception {
 		insertStaffObjects();
 		Statement statement = _connection.createStatement();
 		ResultSet rs = null;
 
 		log.info("[" + new java.util.Date() +"] Querying...");
 		java.sql.Date currDate = new java.sql.Date(new java.util.Date().getTime());
 		String qry = "select emp.*, tax.descr as tax_descr, tax.country as tax_country, tax.city as tax_city, tax.state as tax_state from " + PS_EMPL_TBL + " emp LEFT JOIN " + PS_TAX_TBL + " tax ON emp.tax_location_cd = tax.tax_location_cd WHERE emp.empl_rcd = 0 AND emp.effdt <= to_date('" + currDate.toString() + "','yyyy-mm-dd') order by " + PS_EMPL_ID + " desc";
 		log.debug(qry);
 		rs = statement.executeQuery(qry);
 		
 		java.util.Date querystop = new java.util.Date();
 		log.debug("[" + querystop + "] Got Recordset.");
 		while (rs.next()) {
 		    Staff staff = new Staff();
 		    String currentNo = rs.getString(PS_EMPL_ID);
 		    log.info(" PS record: " + currentNo + "...");
 		    try {
 				staff.setAccountNo(currentNo);
 				staff.select();
 		    	setStaffAttributes(staff, rs);
 				staff.persist();
 		    } catch (Exception e) {
 				log.error("Failed to process record " + currentNo, e);
 				if(_stopOnFail) throw e;
 		    }
 		}
     }
     
 	@SuppressWarnings("unchecked")
 	private void setStaffAttributes(Staff staff, ResultSet rs)
 		throws java.sql.SQLException, Exception {
 		Hashtable before = ObjectHashUtil.obj2hash(staff);
 
 		// NO SSN!
 		//staff.setSsn(rs.getString("ssn"));
 		staff.setIsMale("M".equals(rs.getString("sex")));
 		staff.setBirthDate(rs.getDate("birthdate"));
 		staff.setMaritalStatus(rs.getString("mar_status"));
 		staff.setMarriageDate(rs.getDate("mar_status_dt"));
 		if ("E".equals(rs.getString("employee_flag"))) {
 			staff.setHireDate(rs.getDate("hire_dt"));
 			staff.setRehireDate(rs.getDate("rehire_dt"));
 			staff.setOrigHireDate(rs.getDate("orig_hire_dt"));
 		} else {
 			staff.setHireDate(null);
 			staff.setRehireDate(null);
 			staff.setOrigHireDate(null);
 		}
 		staff.setServiceDate(rs.getDate("service_dt"));
 		staff.setLastIncDate(rs.getDate("last_increase_dt"));
 		staff.setWorkPhone(rs.getString("work_phone"));
 		staff.setDeptId(rs.getString("deptid"));
 		staff.setJobCode(rs.getString("jobcode"));
 		staff.setJobEntryDate(rs.getDate("job_entry_dt"));
 		staff.setDeptEntryDate(rs.getDate("dept_entry_dt"));
 		staff.setAccountCode(rs.getString("acct_cd"));
 		staff.setCompFreq(rs.getString("comp_frequency"));
 		staff.setCompRate(rs.getString("comprate"));
 		staff.setCompChngAmt(rs.getString("change_amt"));
 		staff.setJobTitle(rs.getString("jobtitle"));
 		staff.setDeptName(rs.getString("deptname"));
 		staff.setPrimaryEmpLocCity(rs.getString("tax_city"));
 		staff.setPrimaryEmpLocState(rs.getString("tax_state"));
 		staff.setPrimaryEmpLocCountry(rs.getString("tax_country"));
 		staff.setPrimaryEmpLocDesc(rs.getString("tax_descr"));
 		staff.setJobStatus(JobStatusTranslator.codeToDescription(rs.getString("status_code")));
 		staff.setMinistry(translateMinistry(rs.getString("ccc_ministry")));
 		staff.setRegion(translateRegion(rs.getString("ccc_sub_ministry")));
 		staff.setStrategy(translateStrategy(rs.getString("lane_outreach")));
 		staff.setPosition(translateRespScope(rs.getString("respons_scope")));
 		staff.setMiddleName(rs.getString("middle_name"));
 		staff.setPaygroup(rs.getString("paygroup"));
 		staff.setIdType(rs.getString("id_type"));
 		staff.setStatusDescr(rs.getString("status_descr"));
 		staff.setInternationalStatus(rs.getString("internation_status"));
 		staff.setBalance(rs.getDouble("balance"));
 		staff.setCccHrSendingDept(rs.getString("ccc_hr_sndng_dept"));
 		staff.setCccHrCaringDept(rs.getString("ccc_hr_caring_dept"));
 		staff.setCccCaringMinistry(rs.getString("ccc_carng_ministry"));
 		staff.setAssignmentLength(rs.getString("assignment_lngth"));
 		
 
 		//guarentees that if a staff is reloaded at night, they are said to be on peoplesoft.  (I.E., seen on Infobase)
 		staff.setRemovedFromPeopleSoft("N");
 
 		// Update spousal info
 		updateSpouseInfo(staff, rs);
 
 		staff.setReportingDate(rs.getDate("reporting_date"));
 		staff.setCoupleTitle(rs.getString("couple_name_prefix"));
 		staff.setFirstName(rs.getString("first_name"));
 		staff.setLastName(rs.getString("last_name"));
		staff.setEmail(rs.getString("email_addr"));
 		//		staff.setEmailSecure(rs.getString("psEmailSecure"));
 		staff.setPreferredName(rs.getString("pref_first_name"));
 		staff.setHomePhone(rs.getString("home_phone"));
 		staff.setOtherPhone(rs.getString("phone")); // Actually a duplicate of home phone
 		staff.setMobilePhone(rs.getString("cell_phone"));
 
 		String ccode = rs.getString("nid_country");
 		staff.setCountryCode(ccode);
 
 		if (!"SECURE".equalsIgnoreCase(ccode)) {
 			setAddr(staff, rs);
 		}
 		staff.setIsSecure(rs.getString("SECURE_EMPLOYEE").equals("Y"));
 		Hashtable after = ObjectHashUtil.obj2hash(staff);
 		log.info("Account No. " + staff.getAccountNo());
 		showWhatChanged(before, after);
 	}
 	
 	@SuppressWarnings("unchecked")
 	private void showWhatChanged(Hashtable before, Hashtable after) {
 			Enumeration keys = before.keys();
 			while(keys.hasMoreElements()) {
 				String key = (String) keys.nextElement();
 				Object objBefore = before.get(key);
 				Object objAfter = after.get(key);
 				if(objBefore!=null && (objBefore instanceof String || objBefore instanceof Integer || objBefore instanceof java.sql.Date)) {
 					if (!objBefore.equals(objAfter) && !(objBefore.toString().equals("") && objAfter.toString().equals(" "))) {
 						log.debug("\t\tField: '"+key+ "' was='"+objBefore+"' is='"+objAfter+"'");
 					}
 				}
 			}
 		}
 
 	private String translateMinistry(String ps_ministry) {
 		String desc = (String) luMinistry.get(ps_ministry);
 		if (desc != null) {
 		    return desc;
 		} else {
 		    return ps_ministry;
 		}
     }
 
     
 	private String translateRegion(String ps_sub_ministry) {
 		String desc = (String) luRegion.get(ps_sub_ministry);
 		if (desc != null) {
 			return desc;
 		} else {
 			return ps_sub_ministry;
 		}
 	}
     
 	private String translateStrategy(String ps_lane_outreach) {
 		String desc = (String) luStrategy.get(ps_lane_outreach);
 		if (desc != null) {
 			return desc;
 		} else {
 			return ps_lane_outreach;
 		}
 	}
     
     private String translateRespScope(String respscope) {
 		String desc = (String) luRespScope.get(respscope);
 		if (desc != null) {
 		    return desc;
 		} else {
 		    return respscope;
 		}
     }
 
     private void updateSpouseInfo(Staff staff, ResultSet rs) throws java.sql.SQLException {
 		
 		staff.setSpouseFirstName(rs.getString("spouse_name"));
 		staff.setSpouseLastName(rs.getString("last_name"));
 		
 		String accountNo = rs.getString(PS_EMPL_ID);
 		
 		try{
 	    	// If husband account
 	    	if ((accountNo.length() == 9) && (accountNo.toCharArray()[8] != 'S')) {
 				String wifeAccountNo = accountNo + "S";
 				Staff wife = new Staff();
 				wife.setAccountNo(wifeAccountNo);
 				if (wife.select()){
 					staff.setSpouseAccountNo(wifeAccountNo);			
 					wife.setSpouseAccountNo(accountNo);
 					wife.persist();
 				}
 		    } else if ((accountNo.length() == 10) && (accountNo.toCharArray()[9] == 'S')) {
 				String husbandAccountNo = accountNo.substring(0,9);
 				Staff husband = new Staff();
 				husband.setAccountNo(husbandAccountNo);
 				if (husband.select()){
 					staff.setSpouseAccountNo(husbandAccountNo);			
 					husband.setSpouseAccountNo(accountNo);
 					husband.persist();
 				}
 		    }
 		} catch (Exception e) {
 		    log.error("The spouse object was not found", e);
 		}
     }
 
     private void setAddr(Staff staff, ResultSet rs) throws java.sql.SQLException {
 		OldAddress add1 = staff.getPrimaryAddress();
 		if (add1 == null) {
 		    add1 = new OldAddress();
 		    log.info("Address1 for " + staff.getFirstName() + " " + staff.getLastName() + " not persistant; creating new one");
 		}
 		add1.setAddress1(rs.getString("address1"));
 		add1.setAddress2(rs.getString("address2"));						
 		add1.setAddress3(rs.getString("address3"));						
 		add1.setAddress4(rs.getString("address4"));
 		add1.setCity(rs.getString("city"));
 		add1.setState(rs.getString("state"));
 		add1.setZip(rs.getString("postal"));
 		add1.setCountry(rs.getString("country"));
 		add1.persist();
 		staff.assocPrimaryAddress(add1);
 		
 		OldAddress add2 = staff.getSecondaryAddress();
 		if (add2 == null) {
 		    add2 = new OldAddress();
 		    log.info("Address2 for " + staff.getFirstName() + " " + staff.getLastName() + " not persistant; creating new one");
 		}
 		add2.setAddress1(rs.getString("address1_other"));
 		add2.setAddress2(rs.getString("address2_other"));
 		add2.setAddress3(rs.getString("address3_other"));
 		add2.setCity(rs.getString("city_other"));
 		add2.setState(rs.getString("state_other"));
 		add2.setZip(rs.getString("postal_other"));
 		add2.setCountry(rs.getString("country_other"));
 		add2.persist();
 		staff.assocSecondaryAddress(add2);
 		
 		//updateCurrentAddress(staff);
     } 
 
 	public static void main(String[] args) {
 		try {
 			StaffUpdater su = new StaffUpdater();
 			DBConnectionFactory.setDefaultProperties(args[0], args[1], args[2]);
 			log.info("Updating Staff");
 			su.performUpdate();
 		} catch (Exception e) {
 			log.fatal("Failed!", e);
 		}
 	}
 }
