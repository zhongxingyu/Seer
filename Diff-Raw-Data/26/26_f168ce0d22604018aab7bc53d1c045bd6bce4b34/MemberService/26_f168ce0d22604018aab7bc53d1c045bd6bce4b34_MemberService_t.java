 package com.epam.lab.intouch.service.member;
 
 import java.util.LinkedList;
 import java.util.List;
 
 import com.epam.lab.intouch.dao.exception.DAOException;
 import com.epam.lab.intouch.dao.history.project.DefaultHistoryDAO;
 import com.epam.lab.intouch.dao.history.project.HistoryDAO;
 import com.epam.lab.intouch.dao.member.DefaultMemberDAO;
 import com.epam.lab.intouch.dao.member.MemberDAO;
 import com.epam.lab.intouch.dao.member.skill.DefaultMemberSkillsDAO;
 import com.epam.lab.intouch.dao.member.skill.DefaultSkillDAO;
 import com.epam.lab.intouch.dao.member.skill.MemberSkillsDAO;
 import com.epam.lab.intouch.dao.member.skill.SkillDAO;
 import com.epam.lab.intouch.dao.project.DefaultProjectDAO;
 import com.epam.lab.intouch.dao.project.ProjectDAO;
 import com.epam.lab.intouch.dao.team.DefaultTeamDAO;
 import com.epam.lab.intouch.dao.team.TeamDAO;
 import com.epam.lab.intouch.model.member.Member;
 import com.epam.lab.intouch.model.member.info.skill.Skill;
 import com.epam.lab.intouch.model.project.Project;
 
 public class MemberService implements BaseMemberService {
 
 	private final MemberDAO memberDAO;
 	private final ProjectDAO projectDAO;
 	private final TeamDAO teamDAO;
 	private final HistoryDAO historyDAO;
 	private final SkillDAO skillDAO;
 	private final MemberSkillsDAO memberSkillsDAO;
 	
 
 	public MemberService() {
 		
 		memberDAO = new DefaultMemberDAO();
 		projectDAO = new DefaultProjectDAO();
 		teamDAO = new DefaultTeamDAO();
 		historyDAO = new DefaultHistoryDAO();
 		skillDAO = new DefaultSkillDAO();
 		memberSkillsDAO = new DefaultMemberSkillsDAO();
 	
 
 	}
 
 	@Override
 	public String create(Member member) throws DAOException {
 		
 		String memberLogin = memberDAO.create(member);
 		memberSkillsDAO.create(member);
 
 		return memberLogin;
 	}
 
 	@Override
 	public Member getById(String login) throws DAOException {
 		
 		Member fullMember = memberDAO.getById(login);
 
 		if (fullMember != null) {
 			List<Skill> additionalSkills = memberSkillsDAO.getById(login).getSkills();
 			for (Skill additionalSkill : additionalSkills) {
 				Skill almostFullSkill = skillDAO.getById(additionalSkill.getId());
 
 				additionalSkill.setName(almostFullSkill.getName());
 				additionalSkill.setSkillType(almostFullSkill.getSkillType());
 			}
 			
 			//TODO Refact this piece of s**t, it should work for test purpose
 			fullMember.setSkills(additionalSkills);
 			if(fullMember.getPhotoLink() == null || fullMember.getPhotoLink().isEmpty() ) {
 				if(fullMember.isManager()) {
 					fullMember.setPhotoLink("manager.jpg");
 				} else if(fullMember.isDeveloper()) {
 					fullMember.setPhotoLink("developer.jpg");
 				} else {
 					fullMember.setPhotoLink("qa.jpg");
 				}
 			}
 
 		}
 
 		return fullMember;
 	}
 
 	@Override
 	public void update(Member oldMember, Member newMember) throws DAOException {
 
 		memberDAO.update(oldMember, newMember);
 	}
 
 	@Override
 	public void delete(Member member) throws DAOException {
 
 		memberDAO.delete(member);
 	}
 
 	private List<Member> getFullMembers(List<Member> members) throws DAOException {
 
 		List<Member> fullMembers = new LinkedList<Member>();
 		List<Project> fullProjects = new LinkedList<Project>();
 		
 		for (Member member : members) {
 			String login = member.getLogin();
 			Member fullMember = getById(login);
 
 			Member memberWithHistoryProjectIds = historyDAO.getById(login);
 			List<Project> historyProjects = memberWithHistoryProjectIds.getHistoryProjects();
 			
 			for (Project project : historyProjects) {
 				fullProjects.add(projectDAO.getById(project.getId()));
 			}
 			fullMember.setHistoryProjects(historyProjects);
 
 			List<Project> fullActiveProjects = new LinkedList<Project>();
 			Member memberWithActiveProject = teamDAO.getActiveProjects(login);
 			List<Project> activeProjects = memberWithActiveProject.getActiveProjects();
 			for (Project project : activeProjects) {
 
 				fullActiveProjects.add(project);
 
 			}
 			fullMember.setActiveProjects(fullActiveProjects);
 
 			fullMembers.add(fullMember);
 		}
 
 		return fullMembers;
 
 	}
 	
 	/**
 	 *
 	 * 
 	 * @param login
 	 * @return member with active project id (without full information about project)
 	 * @throws DAOException
 	 */
 	@Override
 	public Member memberWithActiveProjectId(String login) throws DAOException{
 		
 		Member member = memberDAO.getById(login);
 		Member memberWithActiveProjectId = teamDAO.getActiveProjects(login);
 		List<Project> activeProjectsId = memberWithActiveProjectId.getActiveProjects();
 		List<Project> activeProjects = new LinkedList<Project>();
 		for(Project project : activeProjectsId){
 			activeProjects.add(project);	
 		}
 		member.setActiveProjects(activeProjects);
 		
 		return member;
 	}
 	
 	/**
 	 * @param login
 	 * @return member with full information about project (but List<Member contain only login) 
 	 * @throws DAOException
 	 */
 	@Override
 	public Member memberWithFullActiveProject(String login) throws DAOException{
 		Member member = memberDAO.getById(login);
 
 		Member memberWithActiveProjectId = teamDAO.getActiveProjects(login);
 		
 		List<Project> activeProjectsId = memberWithActiveProjectId.getActiveProjects();
 		
 		List<Project> activeProjects = new LinkedList<Project>();
 		
 		for(Project project : activeProjectsId){
 			
 			Project fullProject = projectDAO.getById(project.getId());
 			
 			project.setProjectName(fullProject.getProjectName());
 			project.setCreationDate(fullProject.getCreationDate());
 			project.setEstimatedCompletionDate(fullProject.getCompletionDate());
 			project.setCompletionDate(fullProject.getCompletionDate());
 			project.setDescription(fullProject.getDescription());
 			project.setCustomer(fullProject.getCustomer());
 			project.setStatus(fullProject.getStatus());
 			
 			Project projectWithMemberId = teamDAO.getById(project.getId());
 			List<Member> membersId = projectWithMemberId.getMembers();
 			List<Member> members = new LinkedList<Member>();
 			
 			for(Member memberId : membersId){
 				
				Member memberFromProject = memberDAO.getById(memberId.getLogin());
				
				memberId.setAdditionalInfo(memberFromProject.getAdditionalInfo());
				memberId.setBirthday(memberFromProject.getBirthday());
				memberId.setExperience(memberFromProject.getExperience());
				memberId.setFirstName(memberFromProject.getFirstName());
				memberId.setLastName(memberFromProject.getLastName());
				memberId.setPassword(memberFromProject.getPassword());
				memberId.setPhotoLink(memberFromProject.getPhotoLink());
				memberId.setQualificationLevel(memberFromProject.getQualificationLevel());
				memberId.setRating(memberFromProject.getRating());
				memberId.setRegistrationDate(memberFromProject.getRegistrationDate());
				memberId.setRole(memberFromProject.getRole());
				memberId.setSex(memberFromProject.getSex());
 				
 				members.add(memberId);
 			}
 			
 			project.setMembers(members);
 			activeProjects.add(project);
 		}
 		member.setActiveProjects(activeProjects);
 		
 		return member;
 	}
 	
 	
 
 	@Override
 	public List<Member> getAll() throws DAOException {
 
 		List<Member> members = (List<Member>) memberDAO.getAll();
 		List<Member> fullMembers = getFullMembers(members);
 
 		return fullMembers;
 	}
 
 	@Override
 	public List<Member> getAllFromSearch(String query) throws DAOException {
 		
 		List<Member> members = (List<Member>) memberDAO.getAllFromSearch(query);
 		List<Member> fullMembers = getFullMembers(members);
 
 		return fullMembers;
 	}
 
 	@Override
 	public void updateRating(Member member) throws DAOException {
 		
 		memberDAO.updateRating(member);
 
 	}
 
 	@Override
 	public List<Project> getMemberProjectsHistory(String login) throws DAOException {
 		
 		List<Project> fullProjects = new LinkedList<Project>();
 		
 		Member memberWithActiveProject = teamDAO.getActiveProjects(login);
 		List<Project> activeProjects = memberWithActiveProject.getActiveProjects();
 		for(Project project : activeProjects){
 			
 			fullProjects.add(projectDAO.getById(project.getId()));
 		}
 		
 		Member memberWithHistoryProject = historyDAO.getById(login);
 		List<Project> historyProjects = memberWithHistoryProject.getHistoryProjects();
 		for(Project project : historyProjects){
 			
 			fullProjects.add(projectDAO.getById(project.getId()));
 		}
 		
 		
 		return fullProjects;
 	}
 
 	@Override
 	public Member memberWithActiveProjectInfo(String login) throws DAOException {
 	
 		Member member = memberDAO.getById(login);
 		Member memberWithActiveProjectId = teamDAO.getActiveProjects(login);
 		List<Project> activeProjectsId = memberWithActiveProjectId.getActiveProjects();
 		List<Project> activeProjects = new LinkedList<Project>();
 		for(Project project : activeProjectsId){
 			
 			Project projectInfo = projectDAO.getById(project.getId());
 			
 			project.setCompletionDate(projectInfo.getCompletionDate());
 			project.setCreationDate(projectInfo.getCreationDate());
 			project.setCustomer(projectInfo.getCustomer());
 			project.setDescription(projectInfo.getDescription());
 			project.setEstimatedCompletionDate(projectInfo.getEstimatedCompletionDate());
 			project.setProjectName(projectInfo.getProjectName());
 			project.setStatus(projectInfo.getStatus());
 			
 			activeProjects.add(project);	
 		}
 		member.setActiveProjects(activeProjects);
 		
 		
 		return member;
 	}
 
 	
 
 
 }
