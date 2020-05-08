 package edu.cmu.square.server.business.step.implementation;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import java.util.Set;
 
 import javax.annotation.Resource;
 
 import org.springframework.context.annotation.Scope;
 import org.springframework.stereotype.Service;
 
 import edu.cmu.square.client.exceptions.ExceptionType;
 import edu.cmu.square.client.exceptions.SquareException;
 import edu.cmu.square.client.model.GwtRequirement;
 import edu.cmu.square.client.model.GwtStepVerficationResult;
 import edu.cmu.square.client.navigation.StepEnum;
 import edu.cmu.square.server.authorization.AllowedRoles;
 import edu.cmu.square.server.authorization.Roles;
 import edu.cmu.square.server.business.implementation.BaseBusinessImpl;
 import edu.cmu.square.server.business.step.interfaces.ElicitRequirementsBusiness;
 import edu.cmu.square.server.dao.interfaces.CategoryDao;
 import edu.cmu.square.server.dao.interfaces.ProjectDao;
 import edu.cmu.square.server.dao.interfaces.RequirementDao;
 import edu.cmu.square.server.dao.model.Category;
 import edu.cmu.square.server.dao.model.Project;
 import edu.cmu.square.server.dao.model.Requirement;
 import edu.cmu.square.server.dao.model.Risk;
 
 @Service
 @Scope("prototype")
 public class ElicitRequirementsBusinessImpl extends BaseBusinessImpl implements ElicitRequirementsBusiness
 {
 
 	@Resource
 	private ProjectDao projectDao;
 
 	@Resource
 	private RequirementDao requirementDao;
 	
 	@Resource
 	private CategoryDao categoryDao;
 
 	@AllowedRoles(roles = {Roles.Contractor, Roles.Security_Specialist, Roles.Administrator})
 	public int addRequirementToProject(Integer projectId, GwtRequirement gwtRequirement) throws SquareException
 	{
 		
 		Project p = projectDao.fetch(projectId);
 		Requirement requirement = new Requirement(gwtRequirement);
 		requirement.setProject(p);
 //		if(gwtRequirement.getRisks().isEmpty()) 
 //		{
 //			SquareException se = new SquareException("At least one risk is required.");
 //			se.setType(ExceptionType.missingLink);
 //			throw se;
 //		}
 		if(gwtRequirement.getArtifacts().isEmpty()) 
 		{
 			SquareException se = new SquareException("At least one artifact is required.");
 			se.setType(ExceptionType.missingLink);
 			throw se;
 		}
 	
 		requirementDao.create(requirement);
 		p.getRequirements().add(requirement);
 		return requirement.getId();
 
 
 	}
 
 	@AllowedRoles(roles = {Roles.Contractor, Roles.Security_Specialist, Roles.Administrator})
 	public void deleteRequirement(Integer requirementId, Integer projectId) throws SquareException
 	{
 		requirementDao.deleteById(requirementId);
 		requirementDao.zeroOutPriorities(projectId);
 	}
 
 
 	@AllowedRoles(roles = {Roles.Contractor, Roles.Security_Specialist, Roles.Administrator})
 	public void updateRequirement(GwtRequirement gwtRequirement) throws SquareException
 	{
 	
 		Requirement  r = requirementDao.fetch(gwtRequirement.getId());
 		
 		r.getRisks().clear();
 		r.getArtifacts().clear();
 		r.getGoals().clear();
 		//r.getCategories().clear();
 		
 		r.update(gwtRequirement);
 		
//		if(gwtRequirement.getRisks().isEmpty()) 
//		{
//			SquareException se = new SquareException("At least one risk is required.");
//			se.setType(ExceptionType.missingLink);
//			throw se;
//		}
 		if(gwtRequirement.getArtifacts().isEmpty()) 
 		{
 			SquareException se = new SquareException("At least one artifact is required.");
 			se.setType(ExceptionType.missingLink);
 			throw se;
 		}
 		
 		requirementDao.update(r);
 		
 
 
 	}
 	//@AllowedRoles(roles = {Roles.All})
 	public List<GwtRequirement> getRequirementsFromProject(int projectId) throws SquareException
 	{
 		Project project = projectDao.fetch(projectId);
 		Set<Requirement> requirements = project.getRequirements();
 		
 		List<GwtRequirement> gwtRequirements = new ArrayList<GwtRequirement>();
 		for (Requirement req: requirements) 
 		{
 			GwtRequirement gr = req.createGwtRequirement();
 			gwtRequirements.add(gr);
 		}
 		
 		Collections.sort(gwtRequirements);
 		return gwtRequirements;	
 		
 		
 	}
 	@AllowedRoles(roles = {Roles.All})
 	public void assignRequirementsToCategory(List<GwtRequirement> requirements,int categoryID) throws SquareException
 	{
 		for(GwtRequirement gwtRequirement : requirements)
 		{
 			Requirement  r = requirementDao.fetch(gwtRequirement.getId());
 			 boolean exists=false;
 			  for( Category cat:  r.getCategories())
 			  {
 				  if(cat.getId()==categoryID)
 				  {
 					  exists=true;
 				  }
 			  }
 			  if(!exists)
 			  {
 				  Category category= categoryDao.fetch(categoryID);
 				  r.getCategories().add(category);
 				  requirementDao.update(r);
 			  }
 			
 		}
 		
 		
 	}
 	@AllowedRoles(roles = {Roles.All})
 	public void removeRequirementsFromCategory(List<GwtRequirement> requirements,int categoryID) throws SquareException
 	{
 		for(GwtRequirement gwtRequirement : requirements)
 		{
 			Requirement  r = requirementDao.fetch(gwtRequirement.getId());
 			 boolean exists=false;
 			  for( Category cat:  r.getCategories())
 			  {
 				  if(cat.getId()==categoryID)
 				  {
 					  exists=true;
 				  }
 			  }
 			  if(exists)
 			  {
 				  Category category= categoryDao.fetch(categoryID);
 				  r.getCategories().remove(category);
 				  requirementDao.update(r);
 			  }
 			
 		}
 		
 		
 	}
 
 	@Override
 	public StepEnum getStepDescription() throws SquareException
 	{
 		
 		return StepEnum.Elicit_Security_Requirements;
 	}
 
 	@Override
 	public GwtStepVerficationResult verifyStep(Project project) throws SquareException
 	{
 		
 		GwtStepVerficationResult result = new GwtStepVerficationResult();
 		
 		Set<Requirement> requirements = project.getRequirements();
 
 		int reqWithoutArtifact = 0;
 		int reqWithoutRisk = 0;
 		int reqWithoutGoal = 0;
 
 		for (Requirement r : requirements)
 		{
 			if (r.getArtifacts().size() == 0)
 			{
 				reqWithoutArtifact++;
 			}
 			if (r.getRisks().size() == 0)
 			{
 				reqWithoutRisk++;
 			}
 			if (r.getGoals().size() == 0)
 			{
 				reqWithoutGoal++;
 			}
 		}
 
 		// Validates that every risks is address by all requirements.
 		Set<Risk> risks = project.getRisks();
 		int riskNotLinkedCount = 0;
 		for (Risk risk : risks)
 		{
 			boolean risksMapped = false;
 			for (Requirement r : requirements)
 			{
 				if (r.getRisks().contains(risk))
 				{
 					risksMapped = true;
 					break;
 				}
 
 			}
 			if (!risksMapped)
 			{
 				riskNotLinkedCount++;
 			}
 
 		}
 
 		if (requirements.size() == 0)
 		{
 			result.getMessages().add("There are no requirements collected!");
 			result.setHasWarning(true);
 		}
 
 		if (reqWithoutArtifact != 0)
 		{
 
 			result.getMessages().add("There are " + reqWithoutArtifact + " requirements not associated to artifacts.");
 			result.setHasWarning(true);
 		}
 		if (reqWithoutRisk != 0)
 		{
 
 			result.getMessages().add("There are " + reqWithoutRisk + " requirements not associated to risks.");
 			result.setHasWarning(true);
 		}
 		if (reqWithoutGoal != 0)
 		{
 
 			result.getMessages().add("There are " + reqWithoutGoal + " requirements not associated to security goals.");
 			result.setHasWarning(true);
 		}
 		if (riskNotLinkedCount != 0)
 		{
 
 			result.getMessages().add("There are " + riskNotLinkedCount + " risks not yet associated to requirements.");
 			result.setHasWarning(true);
 		}
 
 		return result;
 	}
 
 	
 }
