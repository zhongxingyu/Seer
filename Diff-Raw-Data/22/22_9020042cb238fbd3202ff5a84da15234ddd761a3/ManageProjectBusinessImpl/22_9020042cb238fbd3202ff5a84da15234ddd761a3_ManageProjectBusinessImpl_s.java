 package edu.cmu.square.server.business.implementation;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import javax.annotation.Resource;
 
 import org.springframework.context.annotation.Scope;
 import org.springframework.stereotype.Service;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 import edu.cmu.square.client.exceptions.ExceptionType;
 import edu.cmu.square.client.exceptions.SquareException;
 import edu.cmu.square.client.model.GwtEvaluation;
 import edu.cmu.square.client.model.GwtInspectionTechnique;
 import edu.cmu.square.client.model.GwtProject;
 import edu.cmu.square.client.model.GwtRole;
 import edu.cmu.square.client.model.GwtStep;
 import edu.cmu.square.client.model.GwtTechnique;
 import edu.cmu.square.client.model.GwtTerm;
 import edu.cmu.square.client.model.GwtUser;
 import edu.cmu.square.client.model.ProjectRole;
 import edu.cmu.square.server.authorization.AllowedRoles;
 import edu.cmu.square.server.authorization.Roles;
 import edu.cmu.square.server.business.interfaces.ManageProjectBusiness;
 import edu.cmu.square.server.business.interfaces.StepBusiness;
 import edu.cmu.square.server.business.step.interfaces.AgreeOnDefinitionsBusiness;
 import edu.cmu.square.server.business.step.interfaces.ElicitationTechniqueBusiness;
 import edu.cmu.square.server.business.step.interfaces.InspectionTechniqueBusiness;
 import edu.cmu.square.server.dao.implementation.HbnSoftwarePackageDao;
 import edu.cmu.square.server.dao.interfaces.AsquareCaseDao;
 import edu.cmu.square.server.dao.interfaces.ProjectDao;
 import edu.cmu.square.server.dao.interfaces.ProjectPackageAttributeRatingDao;
 import edu.cmu.square.server.dao.interfaces.QualityAttributeDao;
 import edu.cmu.square.server.dao.interfaces.RoleDao;
 import edu.cmu.square.server.dao.interfaces.StepDao;
 import edu.cmu.square.server.dao.interfaces.UserAhpDao;
 import edu.cmu.square.server.dao.interfaces.UserDao;
 import edu.cmu.square.server.dao.model.AsquareCase;
 import edu.cmu.square.server.dao.model.InspectionTechnique;
 import edu.cmu.square.server.dao.model.Project;
 import edu.cmu.square.server.dao.model.ProjectPackageAttributeRating;
 import edu.cmu.square.server.dao.model.ProjectPackageAttributeRatingId;
 import edu.cmu.square.server.dao.model.QualityAttribute;
 import edu.cmu.square.server.dao.model.Role;
 import edu.cmu.square.server.dao.model.SoftwarePackage;
 import edu.cmu.square.server.dao.model.Step;
 import edu.cmu.square.server.dao.model.Technique;
 import edu.cmu.square.server.dao.model.User;
 import edu.cmu.square.server.dao.model.UserAhp;
 
 
 @Service
 @Scope("prototype")
 public class ManageProjectBusinessImpl extends BaseBusinessImpl implements ManageProjectBusiness
 {
 
 	private static final long serialVersionUID = -4389713052042815809L;
 	@Resource
 	private StepDao stepDao;
 
 	@Resource
 	private UserDao userDao;
 
 	@Resource
 	private RoleDao roleDao;
 
 	@Resource
 	private ProjectDao projectDao;
 
 	@Resource
 	private UserAhpDao userAhpDao;
 	
 	@Resource
 	private AsquareCaseDao asquareCaseDao;
 
 	@Resource
 	private StepBusiness stepBusiness;
 
 	@Resource
 	private AgreeOnDefinitionsBusiness termsBusiness;
 
 	@Resource
 	private ElicitationTechniqueBusiness elicitationTechniqueBusiness;
 
 	@Resource
 	private InspectionTechniqueBusiness inspectionTechniqueBusiness;
 	
 	@Resource
 	private HbnSoftwarePackageDao softwarePackageDao;
 	
 	@Resource
 	private QualityAttributeDao qualityAttributeDao;
 	
 	@Resource
 	private ProjectPackageAttributeRatingDao projectPackageAttributeRatingDao;
 
 	@AllowedRoles(roles = {Roles.Acquisition_Organization_Engineer})
 	public void editRole(GwtUser gwtUser, GwtProject gwtProject) throws SquareException
 	{
 		User user = new User(gwtUser, "");
 		Role role = roleDao.findByName(gwtUser.getRole());
 		Project project = new Project(gwtProject);
 		userDao.editRole(user, role, project);
 	}
 
 	@AllowedRoles(roles = {Roles.All})
 	public List<Role> getAllRoles() throws SquareException
 	{
 		List<Role> roles = null;
 
 		try
 		{
 			roles = roleDao.getAllRolesExceptLeadRequirementsEngineer();
 			if (roles.size() == 0)
 			{
 				throw new SquareException("No roles in the database");
 			}
 		}
 		catch (SquareException ex)
 		{
 			throw ex;
 		}
 		catch (Throwable t)
 		{
 			throw new SquareException(t.getMessage());
 		}
 
 		return roles;
 	}
 
 	@AllowedRoles(roles = {Roles.All})
 	public List<GwtStep> getSteps(GwtProject gwtProject) throws SquareException
 	{
 
 		// List<GwtStep> gwtSteps = new ArrayList<GwtStep>();
 		List<Step> steps = null;
 
 		List<GwtStep> gwtSteps = new ArrayList<GwtStep>();
 		try
 		{
 
 			Project project = new Project();
 			project.setId(gwtProject.getId());
 
 			steps = stepDao.getProjectSteps(project);
 
 			if (steps.size() == 0)
 			{
 				throw new SquareException("No steps in the database");
 			}
 
 			for (Step s : steps)
 			{
 				gwtSteps.add(s.createGwtStep(gwtProject.getId()));
 			}
 
 		}
 		catch (Throwable t)
 		{
 			throw new SquareException(t);
 
 		}
 
 		return gwtSteps;
 	}
 
 	@AllowedRoles(roles = {Roles.All})
 	public List<GwtUser> getUserList(GwtProject gwtProject) throws SquareException
 	{
 		List<GwtUser> gwtUserList = null;
 		try
 		{
 			Project project = new Project(gwtProject);
 			List<User> userList = userDao.getUserListForProject(project);
 
 			gwtUserList = new ArrayList<GwtUser>(userList.size());
 			for (User u : userList)
 			{
 				GwtUser gwtUser = u.createGwtUser();
 				Role role = userDao.getRoleForUserInProject(u.getId(), project.getId());
 				gwtUser.setRole(role.getName());
 				gwtUserList.add(gwtUser);
 			}
 		}
 		catch (Throwable e)
 		{
 			System.out.println("manageproject buz, get userlist()");
 			throw new SquareException("error " + Arrays.toString(e.getStackTrace()));
 		}
 
 		return gwtUserList;
 	}
 	@AllowedRoles(roles = {Roles.Acquisition_Organization_Engineer})
 	public void removeUserFromProject(GwtProject gwtProject, GwtUser gwtUser) throws SquareException
 	{
 		Project project = new Project(gwtProject);
 		User user = new User(gwtUser, "");
 		if (user.getUserName().equals(super.getUserName()))
 		{
 			SquareException se = new SquareException();
 			se.setType(ExceptionType.selfDelete);
 			throw se;
 		}
 
 		
 		List<UserAhp> comparisons = userAhpDao.getAllComparisons(user.getId(), project.getId());
 
 		for (UserAhp userAhp : comparisons)
 		{
 			userAhpDao.deleteEntity(userAhp);
 		}
 		userDao.removeUserFromProject(user, project);
 	}
 
 	@AllowedRoles(roles = {Roles.Acquisition_Organization_Engineer})
 	public void updateProjectName(GwtProject gwtProject) throws SquareException
 	{
 		try
 		{
 			Project otherProject = projectDao.findByName(gwtProject.getName());
 			if (otherProject != null)
 			{
 				SquareException se = new SquareException("Duplicate project name");
 				se.setType(ExceptionType.duplicateName);
 				throw se;
 			}
 
 			Project project = projectDao.fetch(gwtProject.getId());
 			project.setName(gwtProject.getName());
 		}
 		catch (SquareException ex)
 		{
 			throw ex;
 		}
 		catch (Throwable t)
 		{
 			throw new SquareException("Error updating name", t);
 		}
 	}
 	@AllowedRoles(roles = {Roles.Administrator, Roles.Acquisition_Organization_Engineer})
 	public GwtProject updateProject(int projectId, int acquisitionOEId, String projectName) throws SquareException
 	{
 		try
 		{
 
 			Project project = projectDao.fetch(projectId);
 			project.setName(projectName);
 
 			//User newLeadRequirementEngineer = userDao.fetch(leadRequiremntId);
 			//User oldLeadRequirementEngineer = project.getAcquisitionOrganizationEngineer();
 			
 			User newAcquisitionOrganizationEngineer = userDao.fetch(acquisitionOEId);
 			User oldAcquisitionOrganizationEngineer = project.getAcquisitionOrganizationEngineer();
 			
 			
 			//Role leadRequirementEngineer = roleDao.findByName(ProjectRole.Lead_Requirements_Engineer.getLabel());
 			//Role requirementEngineer = roleDao.findByName(ProjectRole.Requirements_Engineer.getLabel());
 			
 			Role acquisitionOrganizationEngineer = roleDao.findByName(ProjectRole.Acquisition_Organization_Engineer.getLabel());
 						
 			/*
 			if (oldLeadRequirementEngineer != null)
 			{
 				userDao.editRole(oldLeadRequirementEngineer, requirementEngineer, project);
 			}
 			Role userRole = userDao.getRoleForUserInProject(newLeadRequirementEngineer.getId(), project.getId());
 			if (userRole==null)
 			{
 				userDao.addUserToProject(newLeadRequirementEngineer, project, leadRequirementEngineer);
 			}
 			else 
 			{
 				userDao.editRole(newLeadRequirementEngineer, leadRequirementEngineer, project);
 			}
 			project.setLeadRequirementEngineer(newLeadRequirementEngineer);
 			*/
 			
 			if (oldAcquisitionOrganizationEngineer != null)
 			{
 				userDao.editRole(oldAcquisitionOrganizationEngineer, acquisitionOrganizationEngineer, project);
 			}
 			
 			Role userRole = userDao.getRoleForUserInProject(newAcquisitionOrganizationEngineer.getId(), project.getId());
 			
 			if (userRole==null)
 			{
 				userDao.addUserToProject(newAcquisitionOrganizationEngineer, project, acquisitionOrganizationEngineer);
 			}
 			else 
 			{
 				userDao.editRole(newAcquisitionOrganizationEngineer, acquisitionOrganizationEngineer, project);
 			}
 			project.setAcquisitionOrganizationEngineer(newAcquisitionOrganizationEngineer);
 			projectDao.update(project);
 			return project.createGwtProject();
 		}
 		catch (Throwable t)
 		{
 			throw new SquareException("Error updating name", t);
 		}
 	}
 
 	@AllowedRoles(roles = {Roles.Acquisition_Organization_Engineer})
 	public void addUserToProject(GwtProject gwtProject, GwtUser gwtUser, GwtRole gwtRole) throws SquareException
 	{
 		try
 		{
 			Project project = new Project(gwtProject);
 			if (project.getId() == null)
 			{
 				if (project.getName() == null)
 				{
 					throw new SquareException("Either project name or project id must be not null");
 				}
 
 				project = projectDao.findByName(project.getName());
 			}
 
 			User user = new User(gwtUser, "");
 			if (user.getId() == null)
 			{
 				if (user.getUserName() == null)
 				{
 					throw new SquareException("Either userName or user id must be not null");
 				}
 
 				user = userDao.getUsersbyUsername(user.getUserName()).get(0);
 			}
 
 			Role role = new Role(gwtRole);
 			if (role.getId() == null)
 			{
 				if (role.getName() == null)
 				{
 					throw new SquareException("Either role name or role id must be not null");
 				}
 
 				role = roleDao.findByName(role.getName());
 			}
 
 			userDao.addUserToProject(user, project, role);
 		}
 		catch (SquareException ex)
 		{
 			throw ex;
 		}
 		catch (Throwable t)
 		{
 			throw new SquareException(t.getMessage());
 		}
 	}
 
 	@AllowedRoles(roles = {Roles.All})
 	public GwtProject getProject(Integer projectId) throws SquareException
 	{
 		try
 		{
 			Project project = projectDao.fetch(projectId);
 			return project.createGwtProject();
 		}
 		catch (Throwable t)
 		{
 			throw new SquareException(t.getMessage());
 		}
 	}

 	@AllowedRoles(roles = {Roles.Lead_Requirements_Engineer, Roles.Requirements_Engineer, Roles.Acquisition_Organization_Engineer})
 	public void setTechniqueToProject(Integer projectId, Integer techniqueID, String rationale) throws SquareException
 	{
 		Project project = projectDao.fetch(projectId);
 		Technique technique = new Technique();
 		technique.setId(techniqueID);
		project.setSecurityTechnique(technique);
		project.setSecurityTechniqueRationale(rationale);
 		projectDao.update(project);
 	}
 	@AllowedRoles(roles = {Roles.Acquisition_Organization_Engineer})
 	public void setTechniqueToProject(Integer projectId, Integer techniqueID, String rationale) throws SquareException
 	{
 		Project project = projectDao.fetch(projectId);
 		Technique technique = new Technique();
 		technique.setId(techniqueID);
 		project.setSecurityTechnique(technique);
 		project.setSecurityTechniqueRationale(rationale);
 		projectDao.update(project);
 	}

 	@AllowedRoles(roles = {Roles.Administrator,Roles.Acquisition_Organization_Engineer})
 	public GwtProject createProject(GwtProject newProject) throws SquareException
 	{		
 		User acquisitionOrganizationEngineer = userDao.fetch(newProject.getAcquisitionOrganizationEngineer().getUserId());
 		
 		Project project = new Project(newProject);
 		
 		project.setAcquisitionOrganizationEngineer(acquisitionOrganizationEngineer);
 		
 		AsquareCase acase = asquareCaseDao.fetch(newProject.getCases().getId());
 		project.setCases(acase);
 		
		System.out.println("lre: "+project.getAcquisitionOrgEngineer().getId()+" cases: "+project.getCases().getId()+" date1: "+project.getDateCreated() +" date2 "+project.getDateModified()+" lite "+project.isLite()+" name "+project.getName()+" priv "+project.isPrivacy()+" sec "+project.isSecurity());
 		
 		projectDao.create(project);
 		System.out.println("done1");
 		newProject.setId(project.getId());
 		System.out.println("done2");
 		if(newProject.getCases().getId() == 3)
 		{
 		QualityAttribute qa = new QualityAttribute();
 		qa.setName("Unnamed");
 		qa.setDescription("No description");
 		System.out.println("done3");
 		qualityAttributeDao.create(qa);
 		System.out.println("done4");
 		ProjectPackageAttributeRating ppar = new ProjectPackageAttributeRating();
 		ProjectPackageAttributeRatingId pparid = new ProjectPackageAttributeRatingId();
 		pparid.setAttributeId(qa.getId());
 		pparid.setPackageId(1);
 		pparid.setProjectId(project.getId());
 		
 		System.out.println("projectid: "+project.getId()+" packageid: "+1+" attributeid: "+qa.getId());
 		
 		ppar.setId(pparid);
 		ppar.setProject(project);
 		ppar.setSoftwarePackage(softwarePackageDao.fetch(1));
 		ppar.setQualityAttribute(qa);
 		ppar.setRating(0);
 		
 		projectPackageAttributeRatingDao.create(ppar);
 		}
 		
 //		if(newProject.getCases().getName().equals("Case 3"))
 //		{
 //			SoftwarePackage weightsPackage = new SoftwarePackage();
 ////			This is the software package with the weights
 //			weightsPackage.setId(1);
 //			softwarePackageDao.addSoftwarePackageToProject(project, weightsPackage);
 //			System.out.println("Adding weights");
 //		}
 		
 		
 		
 		
 		// Create the steps
 		stepBusiness.createStepsForProject(newProject);
 
 		// Add the lead requirement engineer to project and assigned the role.
 		GwtRole role = new GwtRole();
 		role.setName(ProjectRole.Acquisition_Organization_Engineer.getLabel());
 
 		this.addUserToProject(newProject, acquisitionOrganizationEngineer.createGwtUser(), role);
 		
 		
 		return newProject;
 
 	}
 	@AllowedRoles(roles = {Roles.Administrator,Roles.Acquisition_Organization_Engineer})
 	public void deleteProject(int projectId) throws SquareException
 	{
 
 		projectDao.deleteById(projectId);
 
 	}
 	@AllowedRoles(roles = {Roles.Administrator,Roles.Acquisition_Organization_Engineer})
 	public List<GwtProject> getAllProjects() throws SquareException
 	{
 		List<GwtProject> allProjects = new ArrayList<GwtProject>();
 		List<Project> projects = projectDao.fetchAll();
 
 		if (projects == null)
 		{
 			return allProjects;
 		}
 
 		for (Project p : projects)
 		{
 			allProjects.add(p.createGwtProject());
 		}
 
 		return allProjects;
 	}
 
 	public List<GwtTerm> findDefaultTerms(Document doc)
 	{
 		if (doc == null)
 		{
 			return null;
 		}
 
 		try
 		{
 
 			NodeList nodeLst = getNodeList(doc, "Term");
 
 			List<GwtTerm> terms = new ArrayList<GwtTerm>();
 
 			for (int s = 0; s < nodeLst.getLength(); s++)
 			{
 				Node fstNode = nodeLst.item(s);
 				if (fstNode.getNodeType() == Node.ELEMENT_NODE)
 				{
 					Element fstElmnt = (Element) fstNode;
 
 					GwtTerm term = new GwtTerm();
 					term.setTerm(fstElmnt.getAttribute("name").trim());
 					term.setDefinition(fstElmnt.getTextContent().trim());
 
 					terms.add(term);
 				}
 			}
 
 			return terms;
 		}
 		catch (Exception e)
 		{
 			e.printStackTrace();
 			return null;
 		}
 	}
 
 	public List<GwtTechnique> findDefaultElicitationTechniques(Document doc)
 	{
 		if (doc == null)
 		{
 			return null;
 		}
 
 		try
 		{
 			NodeList nodeLst = getNodeList(doc, "ElicitationTechnique");
 
 			List<GwtTechnique> techniques = new ArrayList<GwtTechnique>();
 
 			for (int s = 0; s < nodeLst.getLength(); s++)
 			{
 				Node fstNode = nodeLst.item(s);
 				if (fstNode.getNodeType() == Node.ELEMENT_NODE)
 				{
 					Element fstElmnt = (Element) fstNode;
 
 					GwtTechnique technique = new GwtTechnique();
 					technique.setTitle(fstElmnt.getAttribute("name").trim());
 					technique.setDescription(fstElmnt.getTextContent().trim());
 
 					String isSecurity = fstElmnt.getAttribute("isSecurity").trim();
 					if (isSecurity.trim().equalsIgnoreCase("true"))
 					{
 						technique.setToSecurity();
 					}
 					else
 					{
 						technique.setToPrivacy();
 					}
 
 					techniques.add(technique);
 				}
 			}
 
 			return techniques;
 		}
 		catch (Exception e)
 		{
 			e.printStackTrace();
 			return null;
 		}
 	}
 	
 	public List<GwtEvaluation> findDefaultEvaluationCriteria(Document doc)
 	{
 		if (doc == null)
 		{
 			return null;
 		}
 
 		try
 		{
 			NodeList nodeLst = getNodeList(doc, "EvaluationCriteria");
 
 			List<GwtEvaluation> evaluations = new ArrayList<GwtEvaluation>();
 
 			for (int s = 0; s < nodeLst.getLength(); s++)
 			{
 				Node fstNode = nodeLst.item(s);
 				if (fstNode.getNodeType() == Node.ELEMENT_NODE)
 				{
 					Element fstElmnt = (Element) fstNode;
 
 					GwtEvaluation evaluationCriteria = new GwtEvaluation();
 					evaluationCriteria.setTitle(fstElmnt.getAttribute("name").trim());
 					evaluationCriteria.setDescription(fstElmnt.getTextContent().trim());
 
 					evaluations.add(evaluationCriteria);
 				}
 			}
 
 			return evaluations;
 		}
 		catch (Exception e)
 		{
 			e.printStackTrace();
 			return null;
 		}
 	}
 
 	public List<GwtInspectionTechnique> findDefaultInspectionTechniques(Document doc)
 	{
 		if (doc == null)
 		{
 			return null;
 		}
 
 		try
 		{
 			NodeList nodeLst = getNodeList(doc, "InspectionTechnique");
 
 			List<GwtInspectionTechnique> inspections = new ArrayList<GwtInspectionTechnique>();
 
 			for (int s = 0; s < nodeLst.getLength(); s++)
 			{
 				Node fstNode = nodeLst.item(s);
 				if (fstNode.getNodeType() == Node.ELEMENT_NODE)
 				{
 					Element fstElmnt = (Element) fstNode;
 
 					GwtInspectionTechnique technique = new GwtInspectionTechnique();
 					technique.setTitle(fstElmnt.getAttribute("name").trim());
 					technique.setDescription(fstElmnt.getTextContent().trim());
 
 					inspections.add(technique);
 				}
 			}
 
 			return inspections;
 		}
 		catch (Exception e)
 		{
 			e.printStackTrace();
 			return null;
 		}
 	}
 
 	private NodeList getNodeList(Document doc, String element)
 	{
 
 		try
 		{
 			doc.getDocumentElement().normalize();
 
 			NodeList nodeLst = doc.getElementsByTagName(element);
 			return nodeLst;
 		}
 		catch (Exception e)
 		{
 			e.printStackTrace();
 			return null;
 		}
 	}
 	
 }
