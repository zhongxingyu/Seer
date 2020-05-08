 package com.test.rest.pkg.database;
 
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.ws.rs.FormParam;
 import javax.ws.rs.core.Context;
 
 import com.test.rest.pkg.clinicaltrials.ClinicalTrials;
 import com.test.rest.pkg.misc.UserInfo;
 import com.test.rest.pkg.ws.DefaultParam;
 
 public class PersistanceActions {
 
 
 	public String userAuthenticate(Connection connection, String email, String pwd) throws Exception
 	{
 		PreparedStatement ps = null;
 		ResultSet rs = null;
 		String retString = "";
 		try
 		{
 			String query = "SELECT * FROM Users Where email='"+email+"'";
 			ps = connection.prepareStatement(query);
 			rs = ps.executeQuery();
 			while(rs.next())
 			{
 				if(rs.getString("password").equals(pwd))
 					if(!rs.getBoolean("user_status")){
 						retString = "Please Activate Your Account";
 					}
 					else{
 						retString = "AUTHENTICATED";
 					}
 			}
 		}
 		catch(Exception e)
 		{
 			throw e;
 		}finally{
 			if(ps!=null)
 				ps.close();
 			if(rs!=null)
 				rs.close();
 		}
 		if(retString.equals(""))
 			retString = "Authentication Failed";
 		
 		return retString;
 	}
 
 	public void getDBRecords(Connection connection) throws Exception
 	{
 		PreparedStatement ps  = null;
 		ResultSet rs = null;
 		try
 		{
 			ps = connection.prepareStatement("SELECT * FROM Users ORDER BY ssn DESC");
 			rs = ps.executeQuery();
 			while(rs.next())
 			{
 				UserInfo feedObject = new UserInfo();
 				feedObject.setId(rs.getString("ssn"));
 				feedObject.setName(rs.getString("name"));
 				feedObject.setDateOfBirth(rs.getString("dateofbirth"));
 				feedObject.setGender(rs.getString("gender"));
 				feedObject.setEmailID(rs.getString("email"));
 				feedObject.setpassword(rs.getString("password"));
 				DefaultParam.instance.getModel().put(feedObject.getId(),feedObject);    
 			}
 		}
 		catch(Exception e)
 		{
 			throw e;
 		}finally{
 			if(ps!=null)
 				ps.close();
 			if(rs!=null)
 				rs.close();
 		}
 	}
 
 	public void setDBRecords(Connection connection, UserInfo perm)
 	{
 		PreparedStatement ps = null;
 		try
 		{
 			String query="INSERT into Users values (?,?,?,?,?,?,?)";
 			System.out.println(query);
 			ps = connection.prepareStatement(query);
 			ps.setInt(1,Integer.parseInt(perm.getId()));
 			ps.setString(2,perm.getName());
 			ps.setString(3,perm.getDateOfBirth());
 			ps.setString(5,perm.getGender());
 			ps.setString(4,perm.getEmailID());
 			ps.setString(6,perm.getpassword());
 			ps.setBoolean(7,false);
 			ps.executeUpdate();
 		}
 		catch(Exception e)
 		{
 			e.printStackTrace();
 		}finally{
 			if(ps!=null)
 				try {
 					ps.close();
 				} catch (SQLException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 		}
 	}
 	
 	
 
 	public List<ClinicalTrials> getTrialRecords(Connection connection, String query) throws Exception
 	{
 		if(query==null || query.equals(""))
 			query = "SELECT * FROM clinical_trials ORDER BY trial_id ASC";
 		
 		List<ClinicalTrials> trials = new ArrayList<ClinicalTrials>();
 		
 		PreparedStatement ps = null;
 		ResultSet rs = null;
 		try{
 		ps = connection.prepareStatement(query);
 		rs = ps.executeQuery();
 		while(rs.next())
 		{
 			ClinicalTrials trial = new ClinicalTrials();
 			trial.setTrialId(rs.getString("trial_id"));
 			trial.setBriefTitle(rs.getString("brief_title"));
 			trial.setOfficialTitle(rs.getString("official_title"));
 			trial.setSponsors(rs.getString("sponsors"));
 			trial.setAuthority(rs.getString("authority"));
 			trial.setStudyType(rs.getString("study_type"));
 			trial.setStudyDesign(rs.getString("study_design"));
 			trial.setSummary(rs.getString("summary"));
 			trial.setStatus(rs.getString("status"));
 			trial.setStDate(rs.getString("start_date"));
 			trial.setEndDate(rs.getString("completion_date"));
 			trial.setPhase(rs.getString("phase"));
 			trial.setCriteria(rs.getString("criteria"));
 			trial.setGender(rs.getString("gender"));
 			trial.setMinAge(rs.getInt("min_age"));
 			trial.setMaxAge(rs.getInt("max_age"));
 			trial.setOfficialLastName(rs.getString("official_last_name"));
 			trial.setOfficialRole(rs.getString("official_role"));
 			trial.setOfficialAffiliation(rs.getString("official_affiliation"));
 			trial.setRetDate(rs.getString("ret_date"));
 			trial.setTags(rs.getString("tags"));
 			trial.setAllLocations(rs.getString("locations"));
 			trials.add(trial);
 		}
 		}catch(Exception e){
 			e.printStackTrace();
 		}finally{
 			if(ps!=null)
 				ps.close();
 			if(rs!=null)
 				rs.close();
 		}
 		return trials;
 	}
 
 
 	public void setTrialRecords(Connection connection, ClinicalTrials trial)
 	{
 		PreparedStatement ps = null;
 		try
 		{	
 			long unixTime = System.currentTimeMillis() / 1000L;
 			String query="SET NAMES 'utf8mb4'";
 			ps = connection.prepareStatement(query);
 			ps.execute();
 			query="INSERT into clinical_trials values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
 			ps = connection.prepareStatement(query);
 			ps.setString(1,trial.getTrialId());
 			ps.setString(2,trial.getBriefTitle());
 			ps.setString(3,trial.getOfficialTitle());
 			ps.setString(4,trial.getSponsors());
 			ps.setString(5,trial.getAuthority());
 			ps.setString(6,trial.getStudyType());
 			ps.setString(7,trial.getStudyDesign());
 			ps.setString(8,trial.getSummary());
 			ps.setString(9,trial.getStatus());
 			ps.setString(10,trial.getStDate());
 			ps.setString(11,trial.getEndDate());
 			ps.setString(12,trial.getPhase());
 			ps.setString(13,trial.getCriteria());
 			ps.setString(14,trial.getGender());
 			ps.setInt(15,trial.getMinAge());
 			ps.setInt(16,trial.getMaxAge());
 			ps.setString(17,trial.getOfficialLastName());
 			ps.setString(18,trial.getOfficialRole());
 			ps.setString(19,trial.getOfficialAffiliation());
 			ps.setString(20,trial.getRetDate());
 			ps.setString(21,trial.getTags());
 			ps.setString(22,trial.getAllLocations());
 			ps.setLong(23,unixTime);
 			ps.executeUpdate();
 			ps.close();
 		}
 		catch(Exception e)
 		{
 			e.printStackTrace();
 		}finally{
 			if(ps!=null)
 				try {
 					ps.close();
 				} catch (SQLException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 		}
 	}
 
 	public static boolean validateConfirmationLink(String hashCode) {
 		// TODO Auto-generated method stub
 
 		hashCode=hashCode.replace(" ", "+");
 		String [] urlParams = hashCode.split(";");
 		Database database= new Database();
 		Connection connection = null;
 		PreparedStatement ps =null;
 		ResultSet rs = null;
 		try {
 			connection = database.Get_Connection();
 			ps = connection.prepareStatement("SELECT * FROM users where name = '"+urlParams[0].split("\\[")[1]+"'");
 			rs = ps.executeQuery();
 
 			while(rs.next()){
 
 				String ssnDB = rs.getString("ssn");
 				ssnDB = ssnDB.replace(" ", "+");
 				System.out.println(hashCode+"\n"+urlParams[0]+"\n"+urlParams[1]+"\n"+Encrypt.encrypt(ssnDB));
 
 				if(urlParams[1].split("]")[0].equals(Encrypt.encrypt(ssnDB))){
 					String psString = "UPDATE users SET user_status = true where name = '"+urlParams[0].split("\\[")[1]+"'";
 					ps = connection.prepareStatement(psString);
 					ps.executeUpdate();
 					return true;
 				}
 			}
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}finally{
 			try {
 				if(rs!=null)
 					rs.close();
 				if(ps!=null)
 					ps.close();
 				if(connection!=null)
 
 					connection.close();
 			} catch (SQLException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 		return false;
 	}
 	
 	public List<ClinicalTrials> getSearchedTrials(Connection connection, String searchString) {
 		List<ClinicalTrials> trials = new ArrayList<ClinicalTrials>();
 		String query = "select * from clinical_trials where trial_id='" +searchString +"' OR tags like '%" + searchString +"%' group by trial_id order by time_stamp desc";
 		System.out.println(query);
 		try {
 			trials = getTrialRecords(connection, query);
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		return trials;
 	}
 
 	public List<ClinicalTrials> getSearchedTrials(Connection connection, String status,
 			String result, String studyType, String ageGroup, String phase1, String phaseII, String phaseIII,
 			String phaseIV, String nih, String industry, String federal,
 			String university, String tags) {
 		
 		boolean flag = false;
 		List<ClinicalTrials> trials = new ArrayList<ClinicalTrials>();
 		
 		String query = "select * from clinical_trials where ";
 		
 		if(!status.equalsIgnoreCase("Open & Closed")){
 			flag = true;
 			if(status.equalsIgnoreCase("Open"))
 				query = query + "(status != 'Completed') AND ";
 			else
 				query = query + "(status = 'Completed') AND ";
 		}
 		
 		if(!studyType.equalsIgnoreCase("All Studies")){
 			flag = true;
 			if(studyType.equalsIgnoreCase("Interventional"))
 				query = query + "(study_type = 'Interventional') AND ";
 			else if(studyType.contains("Observational"))
 				query = query + "(study_type = 'Observational') AND ";
 			else
 				query = query + "(study_type = 'Expanded Access') AND ";
 		}
 		
 		if(!ageGroup.equalsIgnoreCase("all")){
 			flag = true;
 			if(ageGroup.equalsIgnoreCase("Child"))
 				query = query + "(min_age > 0 AND max_age < 18) AND ";
 			else if(ageGroup.contains("Adults"))
 				query = query + "(min_age > 17 AND max_age < 65) AND ";
 			else
 				query = query + "(max_age > 64) AND ";
 		}
 		
 		if(phase1.equalsIgnoreCase("Yes") || phaseII.equalsIgnoreCase("Yes") || phaseIII.equalsIgnoreCase("Yes") || phaseIV.equalsIgnoreCase("Yes")){
 			flag=true;
 			String phaseQuery = "(";
 			
 			if (phase1.equalsIgnoreCase("Yes"))
 				phaseQuery = phaseQuery + "phase = 'Phase 1' OR ";
 
 			if (phaseII.equalsIgnoreCase("Yes"))
 				phaseQuery = phaseQuery + "phase = 'Phase 2' OR ";
 
 			if (phaseIII.equalsIgnoreCase("Yes"))
 				phaseQuery = phaseQuery + "phase = 'Phase 3' OR ";
 
 			if (phaseIV.equalsIgnoreCase("Yes"))
 				phaseQuery = phaseQuery + "phase = 'Phase 4' OR ";
 
 			phaseQuery = phaseQuery.substring(0,phaseQuery.length()-4);
 			query = query + phaseQuery + ") AND ";
 		}	
 		
 		
 		if(nih.equalsIgnoreCase("Yes") || university.equalsIgnoreCase("Yes") || federal.equalsIgnoreCase("Yes") || industry.equalsIgnoreCase("Yes")){
 			flag=true;
 			String fundQuery = "(";
 				if (nih.equalsIgnoreCase("Yes")){
 					
 					fundQuery = fundQuery + "(official_affiliation like '%NIH%' OR official_affiliation like '%nhlbi%') OR ";
 				}
 				
 				if (university.equalsIgnoreCase("Yes")){
 					fundQuery = fundQuery + "(official_affiliation like '%university%') OR ";
 				}
 				
 				if (federal.equalsIgnoreCase("Yes")){
 					fundQuery = fundQuery + "(official_affiliation like '%federal%') OR ";
 				}
 				
 				if (industry.equalsIgnoreCase("Yes")){
 					fundQuery = fundQuery + "(official_affiliation not like '%fed%' OR official_affiliation not like '%university%' OR official_affiliation not like '%nih%' OR official_affiliation not like '%nhlbi%') OR ";
 				}
 				
 				fundQuery = fundQuery.substring(0,fundQuery.length()-4);
 				query = query + fundQuery + ") AND ";
 				
 		}	
 				
 		
 		if(tags!=null)
 				if(!tags.equals("")){
 					flag=true;
 					query = query + "tags like'%"+tags+"%' AND ";
 				}
 		if(flag)
 			query = query.substring(0,query.length()-5);
 		else
 			query = query.substring(0,query.length()-7);
 		
 		query = query + " group by trial_id order by time_stamp desc";
 		
 		System.out.println("This is the final query\n" + query);
 		
 		try {
 			trials = getTrialRecords(connection, query);
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		return trials;
 	}
 	
 	public Map<String,String> getPrefs(Connection connection, String email) throws SQLException {
 		
 		String query = "select * from preferences where user_email='" + email + "'";
 		Map<String,String> preferences = new HashMap<String,String>();
 		PreparedStatement ps = null;
 		ResultSet rs = null;
 		try{
 		ps = connection.prepareStatement(query);
 		rs = ps.executeQuery();
 
 		while(rs.next())
 		{
 			preferences.put("status",rs.getString("status"));
 			preferences.put("result",rs.getString("result"));
 			preferences.put("studyType",rs.getString("studyType"));
 			preferences.put("ageGroup",rs.getString("ageGroup"));
 			preferences.put("phase1",rs.getString("phase1"));
 			preferences.put("phaseII",rs.getString("phaseII"));
			preferences.put("phaseIII",rs.getString("phaseIII"));
 			preferences.put("phaseIV",rs.getString("phaseIV"));
 			preferences.put("NIH",rs.getString("NIH"));
 			preferences.put("industry",rs.getString("industry"));
 			preferences.put("federal",rs.getString("federal"));
 			preferences.put("university",rs.getString("university"));
 			preferences.put("tags",rs.getString("tags"));
 		}
 		}catch(Exception e){
 			e.printStackTrace();
 		}finally{
 			if(ps!=null)
 				ps.close();
 			if(rs!=null)
 				rs.close();
 		}
 		return preferences;
 	}
 		
 
 	
 	public void setPrefs(Connection connection, String status, String result,
 			String studyType, String ageGroup, String phase1, String phaseII,
 			String phaseIII, String phaseIV, String nIH, String industry,
 			String federal, String university, String tags, String userEmail,int defaultStatus) {
 		// TODO Auto-generated method stub
 		
 		PreparedStatement ps = null;
 		try
 		{
 			String query="INSERT into Preferences values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
 			System.out.println(query);
 			ps = connection.prepareStatement(query);
 			ps.setString(1,userEmail);
 			ps.setString(2,status);
 			ps.setString(3,result);
 			ps.setString(4,studyType);
 			ps.setString(5,ageGroup);
 			ps.setString(6,phase1);
 			ps.setString(7,phaseII);
 			ps.setString(8,phaseIII);
 			ps.setString(9,phaseIV);
 			ps.setString(10,nIH);
 			ps.setString(11,industry);
 			ps.setString(12,federal);
 			ps.setString(13,university);
 			ps.setString(14,tags);
 			ps.setInt(15,defaultStatus);
 			ps.executeUpdate();
 		}
 		catch(Exception e)
 		{
 			e.printStackTrace();
 		}finally{
 			if(ps!=null)
 				try {
 					ps.close();
 				} catch (SQLException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 		}
 	}
 	
 	public void updatePrefs(Connection connection, String status, String result,
 			String studyType, String ageGroup, String phase1, String phaseII,
 			String phaseIII, String phaseIV, String nIH, String industry,
 			String federal, String university, String tags, String userEmail,int defaultStatus) {
 		// TODO Auto-generated method stub
 		
 		PreparedStatement ps = null;
 		try
 		{
			String query="UPDATE Preferences set status =?, result=?, studyType=?, ageGroup=?, phase1=?, phaseII=?, phaseIII=?, phaseIV=?, nIH=?, industry=?, federal=?, university=?, tags=?, pref_status = ? WHERE user_email='"+ userEmail +"'";
 			System.out.println(query);
 			ps = connection.prepareStatement(query);
 			ps.setString(1,status);
 			ps.setString(2,result);
 			ps.setString(3,studyType);
 			ps.setString(4,ageGroup);
 			ps.setString(5,phase1);
 			ps.setString(6,phaseII);
 			ps.setString(7,phaseIII);
 			ps.setString(8,phaseIV);
 			ps.setString(9,nIH);
 			ps.setString(10,industry);
 			ps.setString(11,federal);
 			ps.setString(12,university);
 			ps.setString(13,tags);
			ps.setInt(14,defaultStatus);
 			ps.executeUpdate();
 		}
 		catch(Exception e)
 		{
 			e.printStackTrace();
 		}finally{
 			if(ps!=null)
 				try {
 					ps.close();
 				} catch (SQLException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 		}
 	}
 
 	public List<ClinicalTrials> getSearchedTrialRecords(Connection connection,
 			String trialId) {
 		List<ClinicalTrials> trials = new ArrayList<ClinicalTrials>();
 		String query = "select * from clinical_trials where trial_id='" +trialId+"'";
 		try {
 			trials = getTrialRecords(connection, query);
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		return trials;		
 	}
 
 	public int getPrefStatus(Connection connection, String email) throws SQLException {
 		String query = "select * from preferences where user_email='" +email+"'";
 		PreparedStatement ps = null;
 		ResultSet rs = null;
 		try{
 		ps = connection.prepareStatement(query);
 		rs = ps.executeQuery();
 		while(rs.next())
 			return rs.getInt("pref_status");
 		}catch(Exception e){
 			e.printStackTrace();
 		}finally{
 			if(ps!=null)
 				ps.close();
 			if(rs!=null)
 				rs.close();
 		}
 		return 0; 
 	}
 
 	public int getRegStatus(Connection connection, String email) throws SQLException {
 		String query = "select * from users where email='" +email+"'";
 		PreparedStatement ps = null;
 		ResultSet rs = null;
 		try{
 		ps = connection.prepareStatement(query);
 		rs = ps.executeQuery();
 		while(rs.next())
 			return rs.getInt("user_status");
 		}catch(Exception e){
 			e.printStackTrace();
 		}finally{
 			if(ps!=null)
 				ps.close();
 			if(rs!=null)
 				rs.close();
 		}
 		return 0; 
 	}
 }
