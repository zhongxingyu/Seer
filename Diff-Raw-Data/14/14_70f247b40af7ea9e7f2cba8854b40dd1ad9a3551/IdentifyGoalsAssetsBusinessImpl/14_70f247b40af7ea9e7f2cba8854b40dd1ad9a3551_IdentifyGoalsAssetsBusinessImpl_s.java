 package edu.cmu.square.server.business.step.implementation;
 
 import java.util.List;
 import java.util.Set;
 
 import javax.annotation.Resource;
 
 import org.springframework.context.annotation.Scope;
 import org.springframework.stereotype.Service;
 
 import edu.cmu.square.client.exceptions.SquareException;
 import edu.cmu.square.client.model.GwtAsset;
 import edu.cmu.square.client.model.GwtBusinessGoal;
 import edu.cmu.square.client.model.GwtStepVerficationResult;
 import edu.cmu.square.client.model.GwtSubGoal;
 import edu.cmu.square.client.navigation.StepEnum;
 import edu.cmu.square.server.authorization.AllowedRoles;
 import edu.cmu.square.server.authorization.Roles;
 import edu.cmu.square.server.business.implementation.BaseBusinessImpl;
 import edu.cmu.square.server.business.step.interfaces.IdentifyGoalsAssetsBusiness;
 import edu.cmu.square.server.dao.interfaces.AssetDao;
 import edu.cmu.square.server.dao.interfaces.GoalDao;
 import edu.cmu.square.server.dao.model.Asset;
 import edu.cmu.square.server.dao.model.Goal;
 import edu.cmu.square.server.dao.model.GoalType;
 import edu.cmu.square.server.dao.model.Project;
 
 @Service
 @Scope("prototype")
 public class IdentifyGoalsAssetsBusinessImpl extends BaseBusinessImpl implements IdentifyGoalsAssetsBusiness
 {
 
 	@Resource
 	private GoalDao goalDataAccess;
 
 	@Resource
 	private AssetDao assetDataAccess;
 	
 
 	
	@AllowedRoles(roles={Roles.Administrator, Roles.Contractor, Roles.Acquisition_Organization_Engineer, Roles.Security_Specialist})
 	public void setBusinessGoal(int projectId, GwtBusinessGoal businessGoal)  throws SquareException
 	{
 		Project project = new Project();
 		project.setId(projectId);
 		List<Goal> existingBusinessGoalList = this.goalDataAccess.getBusinessGoalByProject(project);
 
 		GoalType goalType = new GoalType();
 		Goal goal = new Goal();
 		goalType.setId(1); // All business goals will have value of 1
 		
 		if (existingBusinessGoalList.size() == 0) // use update instead since  business goal already exists for the project
 		{	
 			goal.setGoalType(goalType);
 			goal.setProject(project);
 			goal.setDescription(businessGoal.getDescription());
 			
 			//This field is only used for the subgoals that is why we just put one for the business goal.
 			goal.setPriority(1);
 		
 			this.goalDataAccess.create(goal);
 		}
 		else
 		{
 			goal = existingBusinessGoalList.get(0);
 			goal.setGoalType(goalType);
 			goal.setProject(project);
 			goal.setDescription(businessGoal.getDescription());
 			
 			//This field is only used for the subgoals that is why we just put one for the business goal.
 			goal.setPriority(1); 
 			goal.setId(businessGoal.getId());
 			this.goalDataAccess.update(goal);
 		}
 		
 		businessGoal.setId(goal.getId());
 		businessGoal.setDescription(goal.getDescription());
 	}
 
	@AllowedRoles(roles = {Roles.Administrator, Roles.Contractor, Roles.Acquisition_Organization_Engineer, Roles.Security_Specialist})
 	public GwtBusinessGoal getBusinessGoalInformation(int projectId)
 	{	
 		Project project = new Project();
 		project.setId(projectId);
 		
 		List<Goal> businessGoalRetrived = this.goalDataAccess.getBusinessGoalByProject(project);
 		
 		GwtBusinessGoal businessGoal = new GwtBusinessGoal();  //create so we can return it later.
 		if (businessGoalRetrived.size() == 1)
 		{
 			businessGoal.setId(businessGoalRetrived.get(0).getId());
 			businessGoal.setDescription(businessGoalRetrived.get(0).getDescription());	
 		}
 		
 		List<Goal> subGoals = this.goalDataAccess.getSubGoalByProject(project);
 	
 		for (Goal goal: subGoals)
 		{
 			GwtSubGoal gwtSubGoal = new GwtSubGoal();
 			gwtSubGoal.setId(goal.getId());
 			gwtSubGoal.setDescription(goal.getDescription());
 			gwtSubGoal.setPriority(goal.getPriority());
 			businessGoal.getSubGoals().add(gwtSubGoal);
 			
 			Set<Asset> listAssets = goal.getAssets();
 			
 			//Loads the assets that are related to an specific subgoal
 			for (Asset a: listAssets)
 			{
 				GwtAsset gwtAsset = new GwtAsset();
 				gwtAsset.setId(a.getId());
 				gwtAsset.setDescription(a.getDescription());
 				gwtSubGoal.getAssets().add(gwtAsset);
 			}
 		}
 		
 		//Loads all the asset to the BusinessInformmation object
 		List<Asset> assets = this.assetDataAccess.getAssetByProject(project);
 		for (Asset asset: assets)
 		{
 			GwtAsset gwtAsset = new GwtAsset();
 			gwtAsset.setId(asset.getId());
 			gwtAsset.setDescription(asset.getDescription());
 			businessGoal.getAssets().add(gwtAsset);
 		}
 
 		return businessGoal;
 	}
 
 	
 	
 	@AllowedRoles(roles={Roles.Administrator, Roles.Contractor, Roles.Acquisition_Organization_Engineer, Roles.Security_Specialist})
 	public void addSubGoal(int projectId, GwtSubGoal subgoal)  throws SquareException
 	{
 		Project project = new Project();
 		project.setId(projectId);
 		
 		GoalType goalType=new GoalType();
 		goalType.setId(2);// Sub goal type
 		
 		Goal goal = new Goal();
 		
 		goal.setGoalType(goalType);
 		goal.setProject(project);
 		goal.setDescription(subgoal.getDescription());
 		goal.setPriority(subgoal.getPriority());
 		goalDataAccess.create(goal);
 		subgoal.setId(goal.getId());
 	}
 	
 	
	@AllowedRoles(roles={Roles.Administrator, Roles.Contractor, Roles.Acquisition_Organization_Engineer, Roles.Security_Specialist})
 	public void updateSubGoal(int projectId, GwtSubGoal subgoal)  throws SquareException
 	{
 		Project project = new Project();
 		project.setId(projectId);
 		
 		GoalType goalType=new GoalType();
 		goalType.setId(2);// Sub goal type
 		
 		Goal goal = goalDataAccess.fetch(subgoal.getId());
 	
 		goal.setProject(project);
 		goal.setId(subgoal.getId());
 		goal.setGoalType(goalType);
 		goal.setDescription(subgoal.getDescription());
 		goal.setPriority(subgoal.getPriority());
 
 		goalDataAccess.update(goal);
 	}
 
 	
 	@AllowedRoles(roles={Roles.Administrator, Roles.Contractor, Roles.Acquisition_Organization_Engineer, Roles.Security_Specialist})	
 	public void removeSubGoal(int projectId, GwtSubGoal subgoal)  throws SquareException
 	{	
 		Project project = new Project();
 		project.setId(projectId);
 		
 		Goal goal = goalDataAccess.fetch(subgoal.getId());
 		goal.setProject(project);
 		goal.setId(subgoal.getId());
 	
 		goalDataAccess.deleteEntity(goal);
 	}
 
 	
 	
 	@AllowedRoles(roles={Roles.Administrator, Roles.Contractor, Roles.Acquisition_Organization_Engineer, Roles.Security_Specialist})
 	public void addAsset(int projectId, GwtAsset gwtAsset)  throws SquareException
 	{	
 		Project project = new Project();
 		project.setId(projectId);
 		Asset asset = new Asset();
 		asset.setProject(project);
 		asset.setDescription(gwtAsset.getDescription());
 		assetDataAccess.create(asset);
 		gwtAsset.setId(asset.getId());
 	}
 
 	
	@AllowedRoles(roles={Roles.Administrator, Roles.Contractor, Roles.Acquisition_Organization_Engineer, Roles.Security_Specialist})
 	public void updateAsset(int projectId, GwtAsset gwtAsset)  throws SquareException
 	{
 		Project project = new Project();
 		project.setId(projectId);
 		Asset asset = assetDataAccess.fetch(gwtAsset.getId());
 		asset.setProject(project);
 		asset.setId(gwtAsset.getId());
 		asset.setDescription(gwtAsset.getDescription());
 		assetDataAccess.update(asset);
 	}
 	
 	
 	@AllowedRoles(roles={Roles.Administrator, Roles.Contractor, Roles.Acquisition_Organization_Engineer, Roles.Security_Specialist})
 	public void removeAsset(int projectId, GwtAsset gwtAsset)  throws SquareException
 	{
 		Project project = new Project();
 		project.setId(projectId);
 		Asset asset = assetDataAccess.fetch(gwtAsset.getId());
 		asset.setProject(project);
 		asset.setId(asset.getId());
 		assetDataAccess.deleteEntity(asset);	
 	}
 	
 	
	@AllowedRoles(roles={Roles.Administrator, Roles.Contractor, Roles.Acquisition_Organization_Engineer, Roles.Security_Specialist})
 	public void associateSubGoalAndAsset(GwtSubGoal gwtSubgoal, GwtAsset gwtAsset)  throws SquareException
 	{
 		Goal subGoal = this.goalDataAccess.fetch(gwtSubgoal.getId());
 		Asset asset = this.assetDataAccess.fetch(gwtAsset.getId());
 		subGoal.getAssets().add(asset);	
 	}
 	
 	
 	@AllowedRoles(roles={Roles.Administrator, Roles.Contractor, Roles.Acquisition_Organization_Engineer, Roles.Security_Specialist})
 	public void removeAssociationSubGoalAndAsset(GwtSubGoal gwtSubgoal, GwtAsset gwtAsset)  throws SquareException
 	{
 		Goal subGoal = this.goalDataAccess.fetch(gwtSubgoal.getId());
 		Asset asset = this.assetDataAccess.fetch(gwtAsset.getId());
 		subGoal.getAssets().remove(asset);	
 	}
 
 	@Override
 	public StepEnum getStepDescription() throws SquareException
 	{
 		return StepEnum.Identify_Assets_And_Goals;
 	}
 
 	@Override
 	public GwtStepVerficationResult verifyStep(Project project) throws SquareException
 	{
 		GwtStepVerficationResult result = new GwtStepVerficationResult();
 
 		GwtBusinessGoal businessGoal = getBusinessGoalInformation(project.getId());
 
 		int subGoalNotLinkedCount = 0;
 		int assetNotLinkedCount = 0;
 		for (GwtSubGoal s : businessGoal.getSubGoals())
 		{
 			if (s.getAssets().size() == 0)
 			{
 				subGoalNotLinkedCount++;
 			}
 
 		}
 
 		for (GwtAsset a : businessGoal.getAssets())
 		{
 			boolean assetMapped = false;
 			for (GwtSubGoal s : businessGoal.getSubGoals())
 			{
 				if (s.getAssets().contains(a))
 				{
 					assetMapped = true;
 					break;
 				}
 			}
 			if (!assetMapped)
 			{
 				assetNotLinkedCount++;
 			}
 		}
 		if (businessGoal.getDescription().trim().length()==0)
 		{
 			result.getMessages().add("There is no business goal collected!");
 			result.setHasWarning(true);
 		}
 		
 		if (businessGoal.getSubGoals().size() == 0)
 		{
 			result.getMessages().add("There are no security goals collected!");
 			result.setHasWarning(true);
 
 		}
 		if (businessGoal.getAssets().size() == 0)
 		{
 			result.getMessages().add("There are no assets collected!");
 			result.setHasWarning(true);
 
 		}
 		if (subGoalNotLinkedCount != 0)
 		{
 			result.getMessages().add("There are " + subGoalNotLinkedCount + " subgoals pending to link!");
 			result.setHasWarning(true);
 		}
 		if (assetNotLinkedCount != 0)
 		{
 			result.getMessages().add("There are " + assetNotLinkedCount + " assests pending to link!");
 			result.setHasWarning(true);
 		}
 
 		return result;
 	}
 
 }
