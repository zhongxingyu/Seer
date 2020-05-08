 package manager;
 
 import java.io.Serializable;
 import java.util.*;
 
 import model.*;
 
 @SuppressWarnings("serial")
 public class ProjectDataManager implements Serializable {
 	private static final int serialVersionUID = (int) 1L;
 	
 	public ProjectDataManager() {}
 	
 	public ArrayList<Project> retrieveAll() {
 		ArrayList<Project> projects = new ArrayList<Project>();
 		HashMap<String, ArrayList<String>> map = MySQLConnector.executeMySQL("select", "select * from projects");
 		Set<String> keySet = map.keySet();
 		Iterator<String> iterator = keySet.iterator();
 		
 		while (iterator.hasNext()){
 			String key = iterator.next();
 			ArrayList<String> array = map.get(key);	
 
 			int id 				= 	Integer.parseInt(array.get(0));
 			int coyId 			= 	Integer.parseInt(array.get(1));
 			int teamId 			= 	Integer.parseInt(array.get(2));
 			int sponsorId 		= 	Integer.parseInt(array.get(3));
 			int reviewer1Id		=	Integer.parseInt(array.get(4));
 			int reviewer2Id		=	Integer.parseInt(array.get(5));
 			String projName		= 	array.get(6);
 			String projDesc		= 	array.get(7);
 			String status		= 	array.get(8);
 			int industry		= 	Integer.parseInt(array.get(9));
 			int termId 		= 	Integer.parseInt(array.get(10));
 			int creatorId 		= 	Integer.parseInt(array.get(11));
 			
 			
 			Project proj = new Project(id, coyId, teamId, sponsorId, reviewer1Id, reviewer2Id, projName, projDesc, status, industry, termId, creatorId);
 			projects.add(proj);
 		}
 		
 		return projects;
 	}
 	
 	
 	public ArrayList<Project> retrieveCurrent() {
 		ArrayList<Project> projects = new ArrayList<Project>();
 		TermDataManager tdm = new TermDataManager();
 		Calendar now = Calendar.getInstance();
 		int currYear = now.get(Calendar.YEAR);
 		int currMth = now.get(Calendar.MONTH);
 		int currTermId = 0;
 		
 		try{
 			currTermId = tdm.retrieveTermId(currYear, currMth);
 		}catch(Exception e){
 			currTermId = 0;
 		}
 		
 		HashMap<String, ArrayList<String>> map = MySQLConnector.executeMySQL("select", "select * from projects WHERE term_id >= " + currTermId);
 		Set<String> keySet = map.keySet();
 		Iterator<String> iterator = keySet.iterator();
 		
 		while (iterator.hasNext()){
 			String key = iterator.next();
 			ArrayList<String> array = map.get(key);	
 
 			int id 				= 	Integer.parseInt(array.get(0));
 			int coyId 			= 	Integer.parseInt(array.get(1));
 			int teamId 			= 	Integer.parseInt(array.get(2));
 			int sponsorId 		= 	Integer.parseInt(array.get(3));
 			int reviewer1Id		=	Integer.parseInt(array.get(4));
 			int reviewer2Id		=	Integer.parseInt(array.get(5));
 			String projName		= 	array.get(6);
 			String projDesc		= 	array.get(7);
 			String status		= 	array.get(8);
 			int industry		= 	Integer.parseInt(array.get(9));
 			int termId 		= 	Integer.parseInt(array.get(10));
 			int creatorId 		= 	Integer.parseInt(array.get(11));
 			
 			
 			Project proj = new Project(id, coyId, teamId, sponsorId, reviewer1Id, reviewer2Id, projName, projDesc, status, industry, termId, creatorId);
 			projects.add(proj);
 		}
 		
 		return projects;
 	}
 	// check for conflicting objects
 	
 	public int getProjIdFromSponsor(int sponsorId){
 		int projId = 0;
 		
 		HashMap<String, ArrayList<String>> map = MySQLConnector.executeMySQL("select", "SELECT * FROM `is480-matching`.projects WHERE sponsor_id = " + sponsorId + ";");
 		Set<String> keySet = map.keySet();
 		Iterator<String> iterator = keySet.iterator();
 		
 		while(iterator.hasNext()){
 			String key = iterator.next();
 			ArrayList<String> array = map.get(key);	
 			
 			projId = Integer.parseInt(array.get(0));
 		}
 		System.out.println(sponsorId);
 		return projId;
 	}
 	
 	
 	public boolean isProjNameTaken(String projName){
 		boolean isTaken = false;
 		
 		HashMap<String, ArrayList<String>> map = MySQLConnector.executeMySQL("select", "SELECT * FROM `is480-matching`.projects WHERE project_name LIKE '" + projName + "' ;");
 		Set<String> keySet = map.keySet();
 		Iterator<String> iterator = keySet.iterator();
 		
 		if(iterator.hasNext()){
 			isTaken = true;
 		}
 		
 		return isTaken;
 	}
 	
 	public boolean hasProj(User std) {
 		StudentDataManager stdm = new StudentDataManager();
 		Student student = null;
 		
 		boolean hasProj = false;
 		
 		try{
 			student = stdm.retrieve(std.getID());
 			
 
 			HashMap<String, ArrayList<String>> map = MySQLConnector.executeMySQL("select", "SELECT * FROM `is480-matching`.projects WHERE creator_id = " + student.getID() + " ;");
 			Set<String> keySet = map.keySet();
 			Iterator<String> iterator = keySet.iterator();
 			
 			if(iterator.hasNext()){
 				hasProj = true;
 			}else if(student.getTeamId() ==  0){
 				hasProj = false;
 			}else{
 				 map = MySQLConnector.executeMySQL("select", "SELECT * FROM `is480-matching`.projects WHERE team_id = " + student.getTeamId() + " ;");
 				 keySet = map.keySet();
 				 iterator = keySet.iterator();
 				
 				if(iterator.hasNext()){
 					hasProj = true;
 				}
 			}
 			
 		}catch(Exception e){
 			hasProj = false;
 		}
 		
 		return hasProj;
 	}
 	
 	public boolean isSup(int projId, int facultyId){
 		boolean isSup = false;
 		
 		HashMap<String, ArrayList<String>> map = MySQLConnector.executeMySQL("select", "select * from teams t, projects p WHERE t.id = p.team_id"
 				+ " AND p.id = " + projId + " AND t.supervisor_id = " + facultyId);
 		Set<String> keySet = map.keySet();
 		Iterator<String> iterator = keySet.iterator();
 		
 		if (iterator.hasNext()){
 			isSup = true;
 		}
 		
 		return isSup;
 	}
 	
 	public boolean isRev(int projId, int facultyId){
 		boolean isRev = false;
 		
 		HashMap<String, ArrayList<String>> map = MySQLConnector.executeMySQL("select", "select * from projects WHERE id = " + projId + " "
 				+ "AND (reviewer1_id = " + facultyId + " OR reviewer2_id " + facultyId + ")");
 		
 		Set<String> keySet = map.keySet();
 		
 		Iterator<String> iterator = keySet.iterator();
 		
 		if (iterator.hasNext()){
 			isRev = true;
 		}
 		
 		return isRev;
 	}
 	
 	public ArrayList<String> retrieveProjName() {
 		ArrayList<String> projects = new ArrayList<String>();
 		HashMap<String, ArrayList<String>> map = MySQLConnector.executeMySQL("select", "select * from projects");
 		Set<String> keySet = map.keySet();
 		Iterator<String> iterator = keySet.iterator();
 		
 		while (iterator.hasNext()){
 			String key = iterator.next();
 			ArrayList<String> array = map.get(key);	
 
 			String projName		= 	array.get(6);
 			
 			projects.add(projName);
 		}
 		
 		return projects;
 	}
 	
 	public Project retrieveProjIdFromCreator(int creatorId) {
 		Project p = null;
 		HashMap<String, ArrayList<String>> map = MySQLConnector.executeMySQL("select", "select * from projects WHERE creator_id  = " + creatorId);
 		Set<String> keySet = map.keySet();
 		Iterator<String> iterator = keySet.iterator();
 		
 		while (iterator.hasNext()){
 			String key = iterator.next();
 			ArrayList<String> array = map.get(key);	
 
 			int id2 				= 	Integer.parseInt(array.get(0));
 			int coyId 			= 	Integer.parseInt(array.get(1));
 			int teamId 			= 	Integer.parseInt(array.get(2));
 			int sponsorId 		= 	Integer.parseInt(array.get(3));
 			int reviewer1Id		=	Integer.parseInt(array.get(4));
 			int reviewer2Id		=	Integer.parseInt(array.get(5));
 			String projName		= 	array.get(6);
 			String projDesc		= 	array.get(7);
 			String status		= 	array.get(8);
 			int industry		= 	Integer.parseInt(array.get(9));
 			int termId 		= 	Integer.parseInt(array.get(10));
 			int creatorId2 		= 	Integer.parseInt(array.get(11));
 			
 			p = new Project(id2, coyId, teamId, sponsorId, reviewer1Id, reviewer2Id, projName, projDesc, status, industry, termId, creatorId2);
 		}
 		
 		return p;
 	}
 	
 	public Project getProjFromTeam(int teamId){
 		Project proj = null;
 		ArrayList<Project> allProjs = retrieveAll();
 		
 		if(teamId == 0){
 			proj = null;
 		}else{
 			for(int i = 0; i < allProjs.size(); i++){
 				Project p = allProjs.get(i);
 				if(p.getTeamId() == teamId){
 					proj = p;
 				}
 			}
 		}
 		return proj;
 	}
 	
 	public Project retrieve(int id) throws Exception{
 		Project proj = null;
 		HashMap<String, ArrayList<String>> map = MySQLConnector.executeMySQL("select", "select * from projects where id = " + id + ";");
 		Set<String> keySet = map.keySet();
 		Iterator<String> iterator = keySet.iterator();
 		
 		while (iterator.hasNext()){
 			String key = iterator.next();
 			ArrayList<String> array = map.get(key);	
 			
 			int id2 				= 	Integer.parseInt(array.get(0));
 			int coyId 			= 	Integer.parseInt(array.get(1));
 			int teamId 			= 	Integer.parseInt(array.get(2));
 			int sponsorId 		= 	Integer.parseInt(array.get(3));
 			int reviewer1Id		=	Integer.parseInt(array.get(4));
 			int reviewer2Id		=	Integer.parseInt(array.get(5));
 			String projName		= 	array.get(6);
 			String projDesc		= 	array.get(7);
 			String status		= 	array.get(8);
 			int industry		= 	Integer.parseInt(array.get(9));
 			int termId 		= 	Integer.parseInt(array.get(10));
 			int creatorId 		= 	Integer.parseInt(array.get(11));
 			
 			
 			proj = new Project(id2, coyId, teamId, sponsorId, reviewer1Id, reviewer2Id, projName, projDesc, status, industry, termId, creatorId);
 			
 		}
 		return proj;
 	}
 	
 	public boolean isEligibleForApplication(int teamId){
 		boolean hasProj = false;
 		boolean hasApplied = false;
 		
 		boolean isEligible = false;
 		
 		if(teamId == 0){
 			isEligible = false;
 		}else{
 			HashMap<String, ArrayList<String>> map = MySQLConnector.executeMySQL("select", "select * from applied_projects where team_id = " + teamId + ";");
 			Set<String> keySet = map.keySet();
 			Iterator<String> iterator = keySet.iterator();
 			
 			if(iterator.hasNext()){
 				hasApplied = true;
 			}
 			
 			map = MySQLConnector.executeMySQL("select", "select * from projects where team_id = " + teamId + ";");
 			keySet = map.keySet();
 			iterator = keySet.iterator();
 			
 			if(teamId == 0){
 				hasProj = false;
 			}else{
 				if(iterator.hasNext()){
 					hasProj = true;
 				}
 			}
 			
 			if(hasProj || hasApplied){
 				isEligible = false;
 			}else{
 				isEligible = true;
 			}
 		}
 		
 		return isEligible;
 	}
 	
 	public double totalIndustrybyProj(int projId){
 		double total = 0.0;
 		HashMap<String, ArrayList<String>> map = MySQLConnector.executeMySQL("select", "select * from projects WHERE id = " + projId);
 		Set<String> keySet = map.keySet();
 		Iterator<String> iterator = keySet.iterator();
 		
 		while (iterator.hasNext()){
 			String key = iterator.next();
 			ArrayList<String> array = map.get(key);	
 			
 			total++;
 			
 		}
 		return total;
 	}
 	
 	public ArrayList<ProjectScore> matchByIndustry(ArrayList<Integer> preferredIndustry){
 		ArrayList<ProjectScore> projects = new ArrayList<ProjectScore>();
 		double score = 0.0;
 		double totalScore = 0.0;
 		for(int i = 0; i < preferredIndustry.size(); i++){
 			HashMap<String, ArrayList<String>> map = MySQLConnector.executeMySQL("select", "select * from projects WHERE sponsor_id != 0 AND team_id = 0 AND industry_id = " + preferredIndustry.get(i));
 			Set<String> keySet = map.keySet();
 			Iterator<String> iterator = keySet.iterator();
 			
 			while (iterator.hasNext()){
 				String key = iterator.next();
 				ArrayList<String> array = map.get(key);	
 	
 				int id 				= 	Integer.parseInt(array.get(0));
 				int coyId 			= 	Integer.parseInt(array.get(1));
 				int teamId 			= 	Integer.parseInt(array.get(2));
 				int sponsorId 		= 	Integer.parseInt(array.get(3));
 				int reviewer1Id		=	Integer.parseInt(array.get(4));
 				int reviewer2Id		=	Integer.parseInt(array.get(5));
 				String projName		= 	array.get(6);
 				String projDesc		= 	array.get(7);
 				String status		= 	array.get(8);
 				int industry		= 	Integer.parseInt(array.get(9));
 				int termId 			= 	Integer.parseInt(array.get(10));
 				int creatorId 		= 	Integer.parseInt(array.get(11));
 				
 				score = 1;
 				totalScore = totalIndustrybyProj(id);
 						
 				Project proj = new Project(id, coyId, teamId, sponsorId, reviewer1Id, reviewer2Id, projName, projDesc, status, industry, termId, creatorId);
 				ProjectScore ps = new ProjectScore(proj, score, totalScore);
 				projects.add(ps);
 			}
 		}
 		
 		return projects;            
 	}
 	
 	public ArrayList<ProjectScore> matchBySkill(ArrayList<Integer> teamSkill){
 		ArrayList<Integer> projectId = new ArrayList<Integer>();
 		ArrayList<ProjectScore> projects = new ArrayList<ProjectScore>();
 		
 		double score = 0.0;
 		double totalScore = 0.0;
 		for(int i = 0; i < teamSkill.size(); i++){
 			HashMap<String, ArrayList<String>> map = MySQLConnector.executeMySQL("select", "select * from project_preferred_skills WHERE skill_id = " + teamSkill.get(i));
 			Set<String> keySet = map.keySet();
 			Iterator<String> iterator = keySet.iterator();
 			
 			while (iterator.hasNext()){
 				String key = iterator.next();
 				ArrayList<String> array = map.get(key);	
 	
 				int projid 			= 	Integer.parseInt(array.get(1));
 				
 				projectId.add(projid);
 			}
 		}
 		
 		projectId = new ArrayList<Integer>(new HashSet<Integer>(projectId));
 		
 		for(int j = 0; j < projectId.size(); j++){
 			HashMap<String, ArrayList<String>> map = MySQLConnector.executeMySQL("select", "select * from projects WHERE team_id = 0 AND id = " + projectId.get(j));
 			Set<String> keySet = map.keySet();
 			Iterator<String> iterator = keySet.iterator();
 			
 			while (iterator.hasNext()){
 				String key = iterator.next();
 				ArrayList<String> array = map.get(key);	
 	
 				int id 				= 	Integer.parseInt(array.get(0));
 				int coyId 			= 	Integer.parseInt(array.get(1));
 				int teamId 			= 	Integer.parseInt(array.get(2));
 				int sponsorId 		= 	Integer.parseInt(array.get(3));
 				int reviewer1Id		=	Integer.parseInt(array.get(4));
 				int reviewer2Id		=	Integer.parseInt(array.get(5));
 				String projName		= 	array.get(6);
 				String projDesc		= 	array.get(7);
 				String status		= 	array.get(8);
 				int industry		= 	Integer.parseInt(array.get(9));
 				int termId 			= 	Integer.parseInt(array.get(10));
 				int creatorId 		= 	Integer.parseInt(array.get(11));
 				
 				if(teamSkill.size() > totalNumOfSkill(id)){
 					score = totalNumOfSkill(id);
 				}else{
 					score = teamSkill.size();
 				}
 				
 				totalScore = totalNumOfSkill(id);
 				
 				Project proj = new Project(id, coyId, teamId, sponsorId, reviewer1Id, reviewer2Id, projName, projDesc, status, industry, termId, creatorId);
 				ProjectScore ps = new ProjectScore(proj, score, totalScore);
 				projects.add(ps);
 			}
 		}
 		
 		return projects;            
 	}
 	
 	public ArrayList<ProjectScore> matchByTechnology(ArrayList<Integer> preferredTechnologies){
 		ArrayList<Integer> projectId = new ArrayList<Integer>();
 		ArrayList<ProjectScore> projects = new ArrayList<ProjectScore>();
 		
 		double score = 0.0;
 		double totalScore = 0.0;
 		
 		for(int i = 0; i < preferredTechnologies.size(); i++){
 			HashMap<String, ArrayList<String>> map = MySQLConnector.executeMySQL("select", "select * from project_technologies WHERE technology_id = " + preferredTechnologies.get(i));
 			Set<String> keySet = map.keySet();
 			Iterator<String> iterator = keySet.iterator();
 			
 			while (iterator.hasNext()){
 				String key = iterator.next();
 				ArrayList<String> array = map.get(key);	
 	
 				int projid 			= 	Integer.parseInt(array.get(1));
 				
 				projectId.add(projid);
 			}
 		}
 		
 		//TO GET RID OF DUPLICATES
 		projectId = new ArrayList<Integer>(new HashSet<Integer>(projectId));
 		
 		for(int j = 0; j < projectId.size(); j++){
 			HashMap<String, ArrayList<String>> map = MySQLConnector.executeMySQL("select", "select * from projects WHERE team_id = 0 AND id = " + projectId.get(j));
 			Set<String> keySet = map.keySet();
 			Iterator<String> iterator = keySet.iterator();
 			
 			while (iterator.hasNext()){
 				String key = iterator.next();
 				ArrayList<String> array = map.get(key);	
 	
 				int id 				= 	Integer.parseInt(array.get(0));
 				int coyId 			= 	Integer.parseInt(array.get(1));
 				int teamId 			= 	Integer.parseInt(array.get(2));
 				int sponsorId 		= 	Integer.parseInt(array.get(3));
 				int reviewer1Id		=	Integer.parseInt(array.get(4));
 				int reviewer2Id		=	Integer.parseInt(array.get(5));
 				String projName		= 	array.get(6);
 				String projDesc		= 	array.get(7);
 				String status		= 	array.get(8);
 				int industry		= 	Integer.parseInt(array.get(9));
 				int termId 			= 	Integer.parseInt(array.get(10));
 				int creatorId 		= 	Integer.parseInt(array.get(11));
 				
 				if(preferredTechnologies.size() > totalNumOfTech(id)){
 					score = totalNumOfTech(id);
 				}else{
 					score = preferredTechnologies.size();
 				}
 				
 				totalScore = totalNumOfTech(id);
 				
 				Project proj = new Project(id, coyId, teamId, sponsorId, reviewer1Id, reviewer2Id, projName, projDesc, status, industry, termId, creatorId);
 				ProjectScore ps = new ProjectScore(proj, score, totalScore);
 				projects.add(ps);
 			}
 		}
 		
 		return projects;            
 	}
 	
 	public ArrayList<ProjectScore> mergedMatchedProjects(ArrayList<ProjectScore> preferredIndustry, ArrayList<ProjectScore> teamSkill, ArrayList<ProjectScore> preferredTechnologies){
 		ArrayList<ProjectScore> projects = new ArrayList<ProjectScore>();
 		ArrayList<ProjectScore> finalProjects = new ArrayList<ProjectScore>();
 		
 		projects.addAll(preferredIndustry);
 		projects.addAll(teamSkill);
 		projects.addAll(preferredTechnologies);
 		
 		for(int i  = 0; i < projects.size(); i++){
 			if(!finalProjects.contains(projects.get(i))){
 				if(projects.get(i).getProject().getSponsorId() != 0){
 					finalProjects.add(projects.get(i));
 				}
 			}else{
 				//add score
 				ProjectScore ps = projects.get(i);
 				for(int j = 0; j < finalProjects.size(); j++){
 					ProjectScore finalPs = finalProjects.get(j);
 					
 					if(ps.getProject().getId() == finalPs.getProject().getId()){
 						finalPs.setScore(finalPs.getScore() + ps.getScore());
 						finalPs.setTotalScore(finalPs.getTotalScore() + ps.getTotalScore());
 					}
 					
 				}
 				
 			}
 		}
 		
 		return projects;
 	}
 	
 	public void add(Project proj){
 		int id 				= proj.getId();
 		int termId			= proj.getTermId();
 		int coyId			= proj.getCoyId();
 		int teamId			= proj.getTeamId();
 		int sponsorId 		= proj.getSponsorId();
 		int reviewer1Id		= proj.getReviewer1Id();
 		int reviewer2Id		= proj.getReviewer2Id();
 		String projName		= proj.getProjName();
 		String projDesc		= proj.getProjDesc();
 		String status		= proj.getStatus();
 		int industry 		= proj.getIndustry();
 		int creatorId		= proj.getCreatorId();
 		
 		MySQLConnector.executeMySQL("insert", "INSERT INTO `is480-matching`.`projects` "
				+ "VALUES (" + id + ", " + coyId +", " + teamId + "," + sponsorId + ", " + reviewer1Id + ", " + reviewer2Id + ", '" + projName + "', '" + projDesc + "', '" + status + "', " + industry + ", '" + termId + "', " + creatorId + ", 4);");
 		System.out.println("Project added successfully");
 	}
 	
 	public Project retrieveProjectsByName(String projName){
 		Project project = null;
 		HashMap<String, ArrayList<String>> map = MySQLConnector.executeMySQL("select", "select * from projects where project_name = '" + projName + "';");
 		Set<String> keySet = map.keySet();
 		Iterator<String> iterator = keySet.iterator();
 		
 		while (iterator.hasNext()){
 			String key = iterator.next();
 			ArrayList<String> array = map.get(key);	
 			
 			int retrievedId 	= 	Integer.parseInt(array.get(0));
 			int coyId 			= 	Integer.parseInt(array.get(1));
 			int teamId 			= 	Integer.parseInt(array.get(2));
 			int sponsorId 		= 	Integer.parseInt(array.get(3));
 			int reviewer1Id		=	Integer.parseInt(array.get(4));
 			int reviewer2Id		=	Integer.parseInt(array.get(5));
 			String retrievedProjName		= 	array.get(6);
 			String projDesc		= 	array.get(7);
 			String status		= 	array.get(8);
 			int industry		= 	Integer.parseInt(array.get(9));
 			int termId 			= 	Integer.parseInt(array.get(10));
 			int creatorId 		= 	Integer.parseInt(array.get(11));
 			
 			project = new Project(retrievedId, coyId, teamId, sponsorId, reviewer1Id, reviewer2Id, retrievedProjName, projDesc, status, industry, termId, creatorId);
 		}
 		
 		return project;
 	}
 	
 	public Project retrieveProjectsByTeam(int teamId){
 		Project project = null;
 		HashMap<String, ArrayList<String>> map = MySQLConnector.executeMySQL("select", "select * from projects where team_id = " + teamId + ";");
 		Set<String> keySet = map.keySet();
 		Iterator<String> iterator = keySet.iterator();
 		
 		while (iterator.hasNext()){
 			String key = iterator.next();
 			ArrayList<String> array = map.get(key);	
 			
 			int retrievedId 	= 	Integer.parseInt(array.get(0));
 			int coyId 			= 	Integer.parseInt(array.get(1));
 			int teamId2 			= 	Integer.parseInt(array.get(2));
 			int sponsorId 		= 	Integer.parseInt(array.get(3));
 			int reviewer1Id		=	Integer.parseInt(array.get(4));
 			int reviewer2Id		=	Integer.parseInt(array.get(5));
 			String retrievedProjName		= 	array.get(6);
 			String projDesc		= 	array.get(7);
 			String status		= 	array.get(8);
 			int industry		= 	Integer.parseInt(array.get(9));
 			int termId 			= 	Integer.parseInt(array.get(10));
 			int creatorId 		= 	Integer.parseInt(array.get(11));
 			
 			project = new Project(retrievedId, coyId, teamId2, sponsorId, reviewer1Id, reviewer2Id, retrievedProjName, projDesc, status, industry, termId, creatorId);
 		}
 		
 		return project;
 	}
 	
 	public ArrayList<Project> retrieveProjectsByTerm(String termId){
 		ArrayList<Project> projects = new ArrayList<Project>();
 		HashMap<String, ArrayList<String>> map = MySQLConnector.executeMySQL("select", "select * from project where term_id = '" + termId + "';");
 		Set<String> keySet = map.keySet();
 		Iterator<String> iterator = keySet.iterator();
 		
 		while (iterator.hasNext()){
 			String key = iterator.next();
 			ArrayList<String> array = map.get(key);	
 
 			int retrievedId 	= 	Integer.parseInt(array.get(0));
 			int coyId 			= 	Integer.parseInt(array.get(1));
 			int teamId 			= 	Integer.parseInt(array.get(2));
 			int sponsorId 		= 	Integer.parseInt(array.get(3));
 			int reviewer1Id		=	Integer.parseInt(array.get(4));
 			int reviewer2Id		=	Integer.parseInt(array.get(5));
 			String retrievedProjName		= 	array.get(6);
 			String projDesc		= 	array.get(7);
 			String status		= 	array.get(8);
 			int industry		= 	Integer.parseInt(array.get(9));
 			int termId2 			= 	Integer.parseInt(array.get(10));
 			int creatorId 		= 	Integer.parseInt(array.get(11));
 			
 			
 			Project proj = new Project(retrievedId, coyId, teamId, sponsorId, reviewer1Id, reviewer2Id, retrievedProjName, projDesc, status, industry, termId2, creatorId);
 			projects.add(proj);
 		}
 		
 		return projects;
 	}
 	
 	public ArrayList<Project> retrieveProjectsByIndustry(String industry){
 		ArrayList<Project> projects = new ArrayList<Project>();
 		HashMap<String, ArrayList<String>> map = MySQLConnector.executeMySQL("select", "select * from projects where project_industry LIKE '" + industry + "';");
 		Set<String> keySet = map.keySet();
 		Iterator<String> iterator = keySet.iterator();
 		
 		while (iterator.hasNext()){
 			String key = iterator.next();
 			ArrayList<String> array = map.get(key);	
 
 			int id 				= 	Integer.parseInt(array.get(0));
 			int coyId 			= 	Integer.parseInt(array.get(1));
 			int teamId 			= 	Integer.parseInt(array.get(2));
 			int sponsorId 		= 	Integer.parseInt(array.get(3));
 			int reviewer1Id		=	Integer.parseInt(array.get(4));
 			int reviewer2Id		=	Integer.parseInt(array.get(5));
 			String projName		= 	array.get(6);
 			String projDesc		= 	array.get(7);
 			String status		= 	array.get(8);
 			int industry2		= 	Integer.parseInt(array.get(9));
 			int termId 		= 	Integer.parseInt(array.get(10));
 			int creatorId 		= 	Integer.parseInt(array.get(11));
 			
 			Project proj = new Project(id, coyId, teamId, sponsorId, reviewer1Id, reviewer2Id, projName, projDesc, status, industry2, termId, creatorId);
 			projects.add(proj);
 		}
 		
 		return projects;
 	}
 	
 	public void applyProj(int teamId, int projId){
 			MySQLConnector.executeMySQL("insert", "INSERT INTO `is480-matching`.`applied_projects` "
 					+ "(`project_id`, `team_id`) "
 					+ "VALUES (" + projId + ", " + teamId + ");");
 	}
 	
 	public void addPreferredSkills(int projid, String[] prefSkill){
 		for(int i = 0; i < prefSkill.length; i++){
 			MySQLConnector.executeMySQL("insert", "INSERT INTO `is480-matching`.`project_preferred_skills` "
 					+ "(`project_id`, `skill_id`) "
 					+ "VALUES (" + projid + ", " + Integer.parseInt(prefSkill[i]) + ");");
 		}
 	}
 	
 	public int numOfSkillNotCovered(ArrayList<Integer> teamSkill, int projId){
 		int numOfSkillCovered = 0;
 		int totalSkill = 0;
 		
 		HashMap<String, ArrayList<String>> map = MySQLConnector.executeMySQL("select", "select * from project_preferred_skills WHERE project_id = " + projId);
 		Set<String> keySet = map.keySet();
 		Iterator<String> iterator = keySet.iterator();
 		
 		while (iterator.hasNext()){
 			String key = iterator.next();
 			ArrayList<String> array = map.get(key);	
 			totalSkill++;
 			int retrievedSkillId 	= 	Integer.parseInt(array.get(2));
 			
 			for(int i = 0; i< teamSkill.size(); i++){
 				if(retrievedSkillId == teamSkill.get(i)){
 					numOfSkillCovered++;
 				}
 			}
 			
 			
 		}
 		return totalSkill - numOfSkillCovered;
 	}
 	
 	public int totalNumOfSkill(int projId){
 		int totalSkill = 0;
 		
 		HashMap<String, ArrayList<String>> map = MySQLConnector.executeMySQL("select", "select * from project_preferred_skills WHERE project_id = " + projId);
 		Set<String> keySet = map.keySet();
 		Iterator<String> iterator = keySet.iterator();
 		
 		while (iterator.hasNext()){
 			String key = iterator.next();
 			ArrayList<String> array = map.get(key);	
 			totalSkill++;
 			
 		}
 		return totalSkill;
 	}
 	
 	public int numOfTechNotCovered(ArrayList<Integer> preferredTechnologies, int projId){
 		int numOfTechCovered = 0;
 		int totalTech = 0;
 		
 		HashMap<String, ArrayList<String>> map = MySQLConnector.executeMySQL("select", "select * from project_technologies WHERE project_id = " + projId);
 		Set<String> keySet = map.keySet();
 		Iterator<String> iterator = keySet.iterator();
 		
 		while (iterator.hasNext()){
 			String key = iterator.next();
 			ArrayList<String> array = map.get(key);	
 			totalTech++;
 			int retrievedTechId 	= 	Integer.parseInt(array.get(2));
 			
 			for(int i = 0; i< preferredTechnologies.size(); i++){
 				if(retrievedTechId == preferredTechnologies.get(i)){
 					numOfTechCovered++;
 				}
 			}
 			
 			
 		}
 		return totalTech - numOfTechCovered;
 	}
 	
 	public int totalNumOfTech(int projId){
 		int totalTech = 0;
 		
 		HashMap<String, ArrayList<String>> map = MySQLConnector.executeMySQL("select", "select * from project_technologies WHERE project_id = " + projId);
 		Set<String> keySet = map.keySet();
 		Iterator<String> iterator = keySet.iterator();
 		
 		while (iterator.hasNext()){
 			String key = iterator.next();
 			ArrayList<String> array = map.get(key);	
 			totalTech++;
 			int retrievedProjId 	= 	Integer.parseInt(array.get(1));
 			
 		}
 		return totalTech;
 	}
 	
 	public void addTech(int projid, int techid){
 		MySQLConnector.executeMySQL("insert", "INSERT INTO project_technologies (`project_id`, `technology_id`) VALUES (" + projid + ", " + techid + ");");
 	}
 	
 	public void modify(Project p){
 		int coyId = p.getCoyId();
 		int teamId = p.getTeamId();
 		int sponsorId = p.getSponsorId();
 		int rev1Id = p.getReviewer1Id();
 		int rev2Id = p.getReviewer2Id();
 		
 		String projName = p.getProjName();
 		String projDesc = p.getProjDesc();
 		String projStatus = p.getStatus();
 		
 		int industryId = p.getIndustry();
 		int termId = p.getTermId();
 		
 		MySQLConnector.executeMySQL("update", "UPDATE projects SET "
 				+ "company_id = " + coyId + ", "
 				+ "team_id = " + teamId + ", "
 				+ "sponsor_id = " + sponsorId + ", "
 				+ "reviewer1_id = " + rev1Id + ", "
 				+ "reviewer2_id = " + rev2Id + ", "
 				+ "project_name = '" + projName + "', "
 				+ "project_description = '" + projDesc + "', "
 				+ "status = '" + projStatus + "', "
 				+ "industry_id = " + industryId + ", "
 				+ "term_id = '" + termId + "' "
 				+ "WHERE id = " + p.getId());
 	}
 	
 	public void modifyPrefSkill(Project p, String[] skillArray){
 		MySQLConnector.executeMySQL("delete", "delete from project_preferred_skills where project_id = " + p.getId() + ";");
 	
 		for(int i = 0; i < skillArray.length; i++){
 			MySQLConnector.executeMySQL("insert", "INSERT INTO project_preferred_skills (`project_id`, `skill_id`) "
 					+ "VALUES(" + p.getId() + ", " + Integer.parseInt(skillArray[i]) + ")");
 		}
 		
 	}
 	
 	public void modifyTechnology(Project p, String[] techArray){
 		MySQLConnector.executeMySQL("delete", "delete from project_technologies where project_id = " + p.getId() + ";");
 		
 		for(int i = 0; i < techArray.length; i++){
 			MySQLConnector.executeMySQL("insert", "INSERT INTO project_technologies (`project_id`, `technology_id`)"
 					+ " VALUES(" + p.getId() + ", " + Integer.parseInt(techArray[i]) + ")");
 		}
 		
 	}
 	
 	public void removeAllApplication(int projId){
 		MySQLConnector.executeMySQL("delete", "delete from applied_projects where project_id = " + projId + ";");
 	}
 	
 	public void removeTeamApplication(int teamId){
 		MySQLConnector.executeMySQL("delete", "delete from applied_projects where team_id = " + teamId + ";");
 	}
 	
 	public void remove(int ID){
 		MySQLConnector.executeMySQL("delete", "delete from projects where id = " + ID + ";");
 		MySQLConnector.executeMySQL("delete", "delete from project_technologies where project_id = " + ID + ";");
 		MySQLConnector.executeMySQL("delete", "delete from project_preferred_skills where project_id = " + ID + ";");
 	}
 	
 	public void removeAll() {
 		
 	}
 }
