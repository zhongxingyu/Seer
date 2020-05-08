 package manager;
 
 import java.io.Serializable;
 import java.sql.*;
 import java.util.*;
 
 import model.*;
 
 public class TeamDataManager implements Serializable {
 	private static final long serialVersionUID = 1L;
 	
 	public TeamDataManager() {}
 	
 	public ArrayList<String> retrieveSuitableSupervisors(int teamId) {
 		ArrayList<String> teams = new ArrayList<String>();
 		HashMap<String, ArrayList<String>> map = MySQLConnector.executeMySQL("select", "select * from users u WHERE u.type NOT LIKE 'Student' AND u.type NOT LIKE 'Sponsor'");
 		Set<String> keySet = map.keySet();
 		Iterator<String> iterator = keySet.iterator();
 		
 		ArrayList<User> supervisors = new ArrayList<User>();
 		
 		while (iterator.hasNext()){
 			String key = iterator.next();
 			ArrayList<String> array = map.get(key);	
 			int retrievedId = Integer.parseInt(array.get(0));
 			String username = array.get(1);
 			String fullName = array.get(2);
 			String contactNum = array.get(3);
 			String email = array.get(4);
 			String type = array.get(5);
 			
 			User u = new User(retrievedId, username, fullName, contactNum, email, type);
 			supervisors.add(u);
 		}
 		
 		ArrayList<Integer> teamSkills = retrieveTeamSkillsById(teamId);
 		
 		UserDataManager udm = new UserDataManager();
 		
 		
 		Map<Integer, Integer> matchScore = new HashMap<Integer, Integer>();
 		
 		for(int i = 0; i < supervisors.size(); i++){
 			User tmpUser = supervisors.get(i);
 			int numOfMatches = 0;
 			for(int j = 0; j < teamSkills.size(); j++){
 				int skillId = teamSkills.get(i);
 				if(udm.hasSkill(tmpUser, skillId)){
 					numOfMatches++;
 				}
 			}
 			
 			matchScore.put(tmpUser.getID(), numOfMatches);
 		}
 		
 		//SORT HASHMAP
 		
 		return teams;
 	}
 	
 	public String getTeamStatus(Team team){
 		String status = "";
 		
 		HashMap<String, ArrayList<String>> map = MySQLConnector.executeMySQL("select", "SELECT s.status FROM team_status ts, status s WHERE ts.status_id = s.status_id AND team_id = " + team.getId());
 		Set<String> keySet = map.keySet();
 		Iterator<String> iterator = keySet.iterator();
 		
 		while (iterator.hasNext()){
 			String key = iterator.next();
 			ArrayList<String> array = map.get(key);	
 			status = array.get(0);
 		}
 		
 		return status;
 	}
 	
 	public ArrayList<Team> retrieveSupervisingTeams(String username) {
 		UserDataManager udm = new UserDataManager();
 		User u = null;
 		
 		try{
 			u = udm.retrieve(username);
 		}catch(Exception e){}
 		
 		ArrayList<Team> teams = new ArrayList<Team>();
		HashMap<String, ArrayList<String>> map = MySQLConnector.executeMySQL("select", "select * from teams WHERE supId = " + u.getID());
 		Set<String> keySet = map.keySet();
 		Iterator<String> iterator = keySet.iterator();
 		
 		while (iterator.hasNext()){
 			String key = iterator.next();
 			ArrayList<String> array = map.get(key);	
 			int id = Integer.parseInt(array.get(0));
 			String teamName = array.get(1);
 			int teamLimit	= Integer.parseInt(array.get(2));
 			int pmId		= Integer.parseInt(array.get(3));
 			int supId 		= Integer.parseInt(array.get(4));
 			int rev1 		= Integer.parseInt(array.get(5));
 			int rev2 		= Integer.parseInt(array.get(6));
 			int termId 		= Integer.parseInt(array.get(7));
 			
 			Team addTeam = new Team(id, teamName,teamLimit, pmId, supId, rev1, rev2, termId);
 			teams.add(addTeam);
 		}
 		
 		return teams;
 	}
 	
 	public ArrayList<Student> retrieveAllStudents(Team team) {
 		ArrayList<Student> members = new ArrayList<Student>();
 		HashMap<String, ArrayList<String>> map = MySQLConnector.executeMySQL("select", "SELECT * FROM `is480-matching`.users inner join `is480-matching`.students on users.id=students.id WHERE students.team_id = " + team.getId());
 		Set<String> keySet = map.keySet();
 		Iterator<String> iterator = keySet.iterator();
 		
 		while (iterator.hasNext()){
 			String key = iterator.next();
 			ArrayList<String> array = map.get(key);	
 			int id 				= Integer.parseInt(array.get(0));
 			String username 	= array.get(1);
 			String fullName 	= array.get(2);
 			String contactNum 	= array.get(3);
 			String email 		= array.get(4);
 			String type			= array.get(5);
 			String secondMajor 	= array.get(7);
 			int role 			= Integer.parseInt(array.get(8));
 			int teamId 			= Integer.parseInt(array.get(9));
 			int prefRole 		= Integer.parseInt(array.get(10));
 			
 			Student student = new Student(id, username, fullName, contactNum, email, type,  secondMajor, role, teamId, prefRole);
 			members.add(student);
 		}
 		
 		return members;
 	}
 	
 	public ArrayList<Team> retrieveAll() {
 		ArrayList<Team> teams = new ArrayList<Team>();
 		HashMap<String, ArrayList<String>> map = MySQLConnector.executeMySQL("select", "select * from teams");
 		Set<String> keySet = map.keySet();
 		Iterator<String> iterator = keySet.iterator();
 		
 		while (iterator.hasNext()){
 			String key = iterator.next();
 			ArrayList<String> array = map.get(key);	
 			int id = Integer.parseInt(array.get(0));
 			String teamName = array.get(1);
 			int teamLimit	= Integer.parseInt(array.get(2));
 			int pmId		= Integer.parseInt(array.get(3));
 			int supId 		= Integer.parseInt(array.get(4));
 			int rev1 		= Integer.parseInt(array.get(5));
 			int rev2 		= Integer.parseInt(array.get(6));
 			int termId 		= Integer.parseInt(array.get(7));
 			
 			Team team = new Team(id, teamName,teamLimit, pmId, supId, rev1, rev2, termId);
 			teams.add(team);
 		}
 		
 		return teams;
 	}
 	
 	public ArrayList<Integer> retrieveStudentsInTeam(Team team) {
 		ArrayList<Integer> membersId = new ArrayList<Integer>();
 		HashMap<String, ArrayList<String>> map = MySQLConnector.executeMySQL("select", "select * from students WHERE team_id =" + team.getId());
 		Set<String> keySet = map.keySet();
 		Iterator<String> iterator = keySet.iterator();
 		
 		while (iterator.hasNext()){
 			String key = iterator.next();
 			ArrayList<String> array = map.get(key);	
 			int id = Integer.parseInt(array.get(0));
 			
 			membersId.add(id);
 		}
 		
 		return membersId;
 	}
 	
 	public ArrayList<Team> retrieveStudentRequests(int studentId) {
 		ArrayList<Team> teams = new ArrayList<Team>();
 		HashMap<String, ArrayList<String>> map = MySQLConnector.executeMySQL("select", "SELECT * FROM student_request WHERE student_id = " + studentId);
 		Set<String> keySet = map.keySet();
 		Iterator<String> iterator = keySet.iterator();
 		
 		while (iterator.hasNext()){
 			String key = iterator.next();
 			ArrayList<String> array = map.get(key);	
 			int teamId	 	= Integer.parseInt(array.get(2));
 			
 			Team team = null;
 			
 			try{
 				team = retrieve(teamId);
 			}catch(Exception e){}
 			
 			teams.add(team);
 		}
 		
 		return teams;
 	}
 	
 	public ArrayList<Team> retrieveAllInvites(int studentId) {
 		ArrayList<Team> teams = new ArrayList<Team>();
 		HashMap<String, ArrayList<String>> map = MySQLConnector.executeMySQL("select", "SELECT * FROM team_request WHERE student_id = " + studentId);
 		Set<String> keySet = map.keySet();
 		Iterator<String> iterator = keySet.iterator();
 		
 		while (iterator.hasNext()){
 			String key = iterator.next();
 			ArrayList<String> array = map.get(key);	
 			int teamId	 	= Integer.parseInt(array.get(1));
 			
 			Team team = null;
 			
 			try{
 				team = retrieve(teamId);
 			}catch(Exception e){}
 			
 			teams.add(team);
 		}
 		
 		return teams;
 	}
 	
 	public ArrayList<Team> retrieveTeamsByAppliedProj(int projId) {
 		ArrayList<Team> teams = new ArrayList<Team>();
 		HashMap<String, ArrayList<String>> map = MySQLConnector.executeMySQL("select", "select * from applied_projects WHERE project_id = " + projId);
 		Set<String> keySet = map.keySet();
 		Iterator<String> iterator = keySet.iterator();
 		
 		while (iterator.hasNext()){
 			String key = iterator.next();
 			ArrayList<String> array = map.get(key);	
 			Team team = null;
 			
 			try{
 				team = retrieve(Integer.parseInt(array.get(2)));
 				teams.add(team);
 			}catch(Exception e){}
 		
 		}
 		
 		return teams;
 	}
 	
 	public ArrayList<String> retrieveTeamNames() {
 		ArrayList<String> teams = new ArrayList<String>();
 		HashMap<String, ArrayList<String>> map = MySQLConnector.executeMySQL("select", "select * from teams");
 		Set<String> keySet = map.keySet();
 		Iterator<String> iterator = keySet.iterator();
 		
 		while (iterator.hasNext()){
 			String key = iterator.next();
 			ArrayList<String> array = map.get(key);	
 			String teamName = array.get(1);
 			
 			teams.add(teamName);
 		}
 		
 		return teams;
 	}
 	
 	public boolean emptySlots(Team team){
 		boolean result = false;
 		HashMap<String, ArrayList<String>> map = MySQLConnector.executeMySQL("select", "select COUNT(id) from students WHERE team_id = " + team.getId());
 		
 		Set<String> keySet = map.keySet();
 		Iterator<String> iterator = keySet.iterator();
 		
 		int numOfStudents = 0;
 
 		if (iterator.hasNext()){
 			String key = iterator.next();
 			ArrayList<String> array = map.get(key);	
 			numOfStudents = Integer.parseInt(array.get(0));
 		}
 		
 		if(numOfStudents < team.getTeamLimit()){
 			result = true;
 		}else{
 			result = false;
 		}
 
 		return result;
 		
 	}
 	
 	public boolean emptyPosition(Team team, Student std){
 		
 		HashMap<String, ArrayList<String>> map = MySQLConnector.executeMySQL("select", "select role_id from students WHERE team_id = " + team.getId());
 		
 		Set<String> keySet = map.keySet();
 		Iterator<String> iterator = keySet.iterator();
 		
 		boolean teamHasRole = false;
 		while (iterator.hasNext()){
 			String key = iterator.next();
 			ArrayList<String> array = map.get(key);	
 			int roleId = Integer.parseInt(array.get(0));
 			
 			if(std.getPreferredRole() == roleId){
 				teamHasRole = true;
 			}
 		}
 		
 		return teamHasRole;
 	}
 	
 	public ArrayList<Team> retrieveEligibleTeams(Student std) {
 		ArrayList<Team> teams = new ArrayList<Team>();
 		ArrayList<Team> allTeams = retrieveAll();
 		
 		for(int i = 0; i < allTeams.size(); i++){
 			Team t = allTeams.get(i);
 			
 			//Check for empty slots and none of the student's preferredrole
 			if(emptySlots(t) && !emptyPosition(t, std)){
 				teams.add(t);
 			}
 		}
 		
 		return teams;
 	}
 	
 	public Team retrieveTeamByName(String teamName){
 		Team team = null;
 		HashMap<String, ArrayList<String>> map = MySQLConnector.executeMySQL("select", "select * from teams WHERE teams.team_Name LIKE '" + teamName + "'");
 		Set<String> keySet = map.keySet();
 		Iterator<String> iterator = keySet.iterator();
 		
 		while (iterator.hasNext()){
 			String key = iterator.next();
 			ArrayList<String> array = map.get(key);	
 			int id = Integer.parseInt(array.get(0));
 			String teamName2 = array.get(1);
 			int teamLimit	= Integer.parseInt(array.get(2));
 			int pmId		= Integer.parseInt(array.get(3));
 			int supId 		= Integer.parseInt(array.get(4));
 			int rev1 		= Integer.parseInt(array.get(5));
 			int rev2 		= Integer.parseInt(array.get(6));
 			int termId 		= Integer.parseInt(array.get(7));
 			
 			team = new Team(id, teamName2,teamLimit, pmId, supId, rev1, rev2, termId);
 			
 		}
 		return team;
 	}
 	
 	public int retrieveTeamIdByUser(User u){
 		int teamId = 0;
 		HashMap<String, ArrayList<String>> map = MySQLConnector.executeMySQL("select", "select team_id from students WHERE id = " + u.getID() + "");
 		Set<String> keySet = map.keySet();
 		Iterator<String> iterator = keySet.iterator();
 		
 		while (iterator.hasNext()){
 			String key = iterator.next();
 			ArrayList<String> array = map.get(key);	
 			int id = Integer.parseInt(array.get(0));
 			
 			teamId = id;
 			
 		}
 		return teamId;
 	}
 	
 	public ArrayList<Integer> retrieveTeamSkillsById(int teamId){
 		ArrayList<Integer> teamSkills = new ArrayList<Integer>();
 		HashMap<String, ArrayList<String>> map = MySQLConnector.executeMySQL("select", "select DISTINCT sk.skill_id from students std, user_skills sk WHERE sk.user_id = std.id AND std.team_id = " + teamId + ";");
 		Set<String> keySet = map.keySet();
 		Iterator<String> iterator = keySet.iterator();
 		
 		while (iterator.hasNext()){
 			String key = iterator.next();
 			ArrayList<String> array = map.get(key);	
 			int skillId	= Integer.parseInt(array.get(0));
 			
 			teamSkills.add(skillId);
 			
 		}
 		return teamSkills;
 	}
 	
 	public ArrayList<Integer> retrieveTeamSkills(Team team){
 		ArrayList<Integer> teamSkills = new ArrayList<Integer>();
 		HashMap<String, ArrayList<String>> map = MySQLConnector.executeMySQL("select", "select DISTINCT sk.skill_id from students std, user_skills sk WHERE sk.user_id = std.id AND std.team_id = " + team.getId() + ";");
 		Set<String> keySet = map.keySet();
 		Iterator<String> iterator = keySet.iterator();
 		
 		while (iterator.hasNext()){
 			String key = iterator.next();
 			ArrayList<String> array = map.get(key);	
 			int skillId	= Integer.parseInt(array.get(0));
 			
 			teamSkills.add(skillId);
 			
 		}
 		return teamSkills;
 	}
 	
 	public ArrayList<String> retrieveTeamSkillName(Team team){
 		ArrayList<String> teamSkills = new ArrayList<String>();
 		HashMap<String, ArrayList<String>> map = MySQLConnector.executeMySQL("select", "select DISTINCT sk.skill_id from students std, user_skills sk WHERE sk.user_id = std.id AND std.team_id = " + team.getId() + ";");
 		Set<String> keySet = map.keySet();
 		Iterator<String> iterator = keySet.iterator();
 		SkillDataManager skdm = new SkillDataManager();
 		
 		Skill skill = null;
 		
 		while (iterator.hasNext()){
 			String key = iterator.next();
 			ArrayList<String> array = map.get(key);	
 			int skillId	= Integer.parseInt(array.get(0));
 			
 			try{
 				skill = skdm.retrieve(skillId);
 			}catch(Exception e){}
 			
 			teamSkills.add(skill.getSkillName());
 			
 		}
 		return teamSkills;
 	}
 	
 	public ArrayList<Integer> retrieveTeamPreferredTechnology(Team team){
 		ArrayList<Integer> teamPreferredTech = new ArrayList<Integer>();
 		HashMap<String, ArrayList<String>> map = MySQLConnector.executeMySQL("select", "select technology_id from team_preferred_technology where team_id = " + team.getId() + ";");
 		Set<String> keySet = map.keySet();
 		Iterator<String> iterator = keySet.iterator();
 		
 		while (iterator.hasNext()){
 			String key = iterator.next();
 			ArrayList<String> array = map.get(key);	
 			int techId	= Integer.parseInt(array.get(0));
 			
 			teamPreferredTech.add(techId);
 			
 		}
 		return teamPreferredTech;
 	}
 	
 	public ArrayList<Integer> retrieveTeamPreferredIndustry(Team team){
 		ArrayList<Integer> teamPreferredIndustry = new ArrayList<Integer>();
 		HashMap<String, ArrayList<String>> map = MySQLConnector.executeMySQL("select", "select industry_id from team_preferred_industry where team_id = " + team.getId() + ";");
 		Set<String> keySet = map.keySet();
 		Iterator<String> iterator = keySet.iterator();
 		
 		while (iterator.hasNext()){
 			String key = iterator.next();
 			ArrayList<String> array = map.get(key);	
 			int industryId	= Integer.parseInt(array.get(0));
 			
 			teamPreferredIndustry.add(industryId);
 			
 		}
 		return teamPreferredIndustry;
 	}
 	
 	// check for conflicting objects
 	
 	public boolean isTeamNameTaken(String teamName){
 		boolean isTaken = false;
 		
 		HashMap<String, ArrayList<String>> map = MySQLConnector.executeMySQL("select", "select * from teams WHERE team_name LIKE '" + teamName + "';");
 		Set<String> keySet = map.keySet();
 		Iterator<String> iterator = keySet.iterator();
 		
 		if (iterator.hasNext()){
 			isTaken = true;
 		}
 		
 		return isTaken;
 	}
 	
 	public int retrievebyStudent(int id) throws Exception{
 		int teamId = 0;
 		HashMap<String, ArrayList<String>> map = MySQLConnector.executeMySQL("select", "select team_id from students where id = " + id + ";");
 		Set<String> keySet = map.keySet();
 		Iterator<String> iterator = keySet.iterator();
 		
 		while (iterator.hasNext()){
 			String key = iterator.next();
 			ArrayList<String> array = map.get(key);	
 			teamId	= Integer.parseInt(array.get(0));
 			//retrievedTeam = new Team(retrievedId, teamName, teamDesc,teamLimit, pmId);
 		}
 		return teamId;
 	}
 	
 	public ArrayList<Team> retrievebyFaculty(int id) throws Exception{
 		 ArrayList<Team> retrievedTeams = new ArrayList<Team>();
 		HashMap<String, ArrayList<String>> map = MySQLConnector.executeMySQL("select", "select * from teams where id = " + id + ";");
 		Set<String> keySet = map.keySet();
 		Iterator<String> iterator = keySet.iterator();
 		
 		while (iterator.hasNext()){
 			String key = iterator.next();
 			ArrayList<String> array = map.get(key);	
 			int id2 = Integer.parseInt(array.get(0));
 			String teamName = array.get(1);
 			int teamLimit	= Integer.parseInt(array.get(2));
 			int pmId		= Integer.parseInt(array.get(3));
 			int supId 		= Integer.parseInt(array.get(4));
 			int rev1 		= Integer.parseInt(array.get(5));
 			int rev2 		= Integer.parseInt(array.get(6));
 			int termId 		= Integer.parseInt(array.get(7));
 			
 			Team team = new Team(id2, teamName,teamLimit, pmId, supId, rev1, rev2, termId);
 			retrievedTeams.add(team);
 		}
 		return retrievedTeams;
 	}
 	
 	public Team retrieve(int id) throws Exception{
 		Team retrievedTeam = null;
 		HashMap<String, ArrayList<String>> map = MySQLConnector.executeMySQL("select", "select * from teams where id = " + id + ";");
 		Set<String> keySet = map.keySet();
 		Iterator<String> iterator = keySet.iterator();
 		
 		while (iterator.hasNext()){
 			String key = iterator.next();
 			ArrayList<String> array = map.get(key);	
 			int id2 = Integer.parseInt(array.get(0));
 			String teamName = array.get(1);
 			int teamLimit	= Integer.parseInt(array.get(2));
 			int pmId		= Integer.parseInt(array.get(3));
 			int supId 		= Integer.parseInt(array.get(4));
 			int rev1 		= Integer.parseInt(array.get(5));
 			int rev2 		= Integer.parseInt(array.get(6));
 			int termId 		= Integer.parseInt(array.get(7));
 			
 			retrievedTeam = new Team(id2, teamName,teamLimit, pmId, supId, rev1, rev2, termId);
 		}
 		return retrievedTeam;
 	}
 	
 	public void add(Team team, String[] prefIndustry, String[] prefTech){
 		int teamId		=	team.getId();
 		String teamName = 	team.getTeamName();
 		int teamLimit	=	team.getTeamLimit();
 		int pmId		=	team.getPmId();
 		int supId		=	team.getSupId();
 		int rev1		= 	team.getRev1Id();
 		int rev2		= 	team.getRev2Id();
 		int termId		= 	team.getTermId();
 		MySQLConnector.executeMySQL("insert", "INSERT INTO `is480-matching`.`teams` "
 				+ "(`id`, `team_name`, `team_limit`, `pm_id`, `supervisor_id`, `reviewer1`, `reviewer2`, `term_id`) "
 				+ "VALUES (" + teamId + ", '" + teamName + "', " + teamLimit + ", " + pmId + ", " + supId + ", " + rev1 + ", " + rev2 + ", " + termId + ");");
 		
 		for(int i = 0; i < prefIndustry.length; i++){
 			MySQLConnector.executeMySQL("insert", "INSERT INTO `is480-matching`.`team_preferred_industry` "
 					+ "(`team_id`, `industry_id`) "
 					+ "VALUES (" + teamId + ", " + Integer.parseInt(prefIndustry[i]) + ");");
 		}
 		
 		for(int i = 0; i < prefTech.length; i++){
 			MySQLConnector.executeMySQL("insert", "INSERT INTO `is480-matching`.`team_preferred_technology` "
 					+ "(`team_id`, `technology_id`) "
 					+ "VALUES (" + teamId + ", " + Integer.parseInt(prefTech[i]) + ");");
 		}
 		
 	}
 	
 	public void deleteAppliedProjsIfTeamDeleted(int teamId){
 		MySQLConnector.executeMySQL("delete", "DELETE FROM applied_projects WHERE team_id = " + teamId);
 		//System.out.println("Updated Student table");
 	}
 	
 	public void updateTeamTechIfTeamDeleted(int teamId){
 		MySQLConnector.executeMySQL("delete", "DELETE FROM team_preferred_technology WHERE team_id = " + teamId);
 		//System.out.println("Updated Student table");
 	}
 	
 	public void updateTeamIndustryIfTeamDeleted(int teamId){
 		MySQLConnector.executeMySQL("delete", "DELETE FROM team_preferred_industry WHERE team_id = " + teamId);
 		//System.out.println("Updated Student table");
 	}
 	
 	public void updateStudentsIfTeamDeleted(int teamId){
 		MySQLConnector.executeMySQL("update", "UPDATE STUDENTS SET team_id = " + 0 + " WHERE team_id = " + teamId);
 		//System.out.println("Updated Student table");
 	}
 	
 	public void updateProjectIfTeamDeleted(int teamId){
 		MySQLConnector.executeMySQL("update", "UPDATE PROJECTS SET team_id = " + 0 + " WHERE team_id = " + teamId + " AND status NOT LIKE 'Closed'");
 		//System.out.println("Updated Projects table");
 	}
 	
 	public void modify(Team team){
 		MySQLConnector.executeMySQL("update", "UPDATE teams SET "
 				+ "team_name = '" + team.getTeamName() + "', "
 				+ "team_limit = " + team.getTeamLimit() + ", "
 				+ "pm_id = " + team.getPmId() + ", "
 				+ "supervisor_id = " + team.getSupId() + ", "
 				+ "reviewer1 = " + team.getRev1Id() + ", "
 				+ "reviewer2 = " + team.getRev2Id() + ", "
 				+ "team_id = " + team.getTermId() + " "
 				+ "WHERE id = " + team.getId());
 	}
 	
 	public void modifyStudents(Team team, String[] members, String[] roles){
 		//CLEAR ALL CURRENT TEAM MEMBERS INFO
 		for(int i = 0; i < members.length; i++){
 			MySQLConnector.executeMySQL("update", "UPDATE students SET "
 					+ "role = '', "
 					+ "team_id = 0 "
 					+ "WHERE id = " + Integer.parseInt(members[i]));
 		}
 		
 		
 		//UPDATE WITH NEW MEMBERS AND ROLES
 		for(int i = 0; i < members.length; i++){
 			MySQLConnector.executeMySQL("update", "UPDATE students SET "
 					+ "role = " + Integer.parseInt(roles[i]) + ", "
 					+ "team_id = " + team.getId() + ", "
 					+ "WHERE id = " + Integer.parseInt(members[i]));
 		}
 		
 	}
 	
 	public void studentRequest(int userId, int teamId){
 		MySQLConnector.executeMySQL("insert", "INSERT INTO student_request (student_id, team_id) "
 				+ "VALUES(" + userId + ", " + teamId + ")");
 	}
 	
 	public void removeAllStudentRequest(int teamId){
 		MySQLConnector.executeMySQL("delete", "DELETE FROM student_request WHERE team_id =  " + teamId);
 	}
 	
 	public void removeStudentRequest(int userId, int teamId){
 		MySQLConnector.executeMySQL("delete", "DELETE FROM student_request WHERE student_id = " + userId + " AND team_id =  " + teamId);
 	}
 	
 	public void studentInvite(int userId, int teamId){
 		MySQLConnector.executeMySQL("insert", "INSERT INTO team_request (student_id, team_id) "
 				+ "VALUES(" + userId + ", " + teamId + ")");
 	}
 	
 	public void removeAllTeamInvite(int teamId){
 		MySQLConnector.executeMySQL("delete", "DELETE FROM team_request WHERE team_id =  " + teamId);
 	}
 	
 	public void removeTeamInvite(int userId, int teamId){
 		MySQLConnector.executeMySQL("delete", "DELETE FROM team_request WHERE student_id = " + userId + " AND team_id =  " + teamId);
 	}
 	
 	
 	public void removeMember(int userId){
 		MySQLConnector.executeMySQL("update", "UPDATE students SET team_id = 0, role_id = 0 WHERE id = " + userId);
 	}
 	
 	public void remove(int ID){
 		MySQLConnector.executeMySQL("delete", "Delete FROM teams WHERE id = " + ID);
 	}
 	
 	public void removeAll() {
 		
 	}
 }
