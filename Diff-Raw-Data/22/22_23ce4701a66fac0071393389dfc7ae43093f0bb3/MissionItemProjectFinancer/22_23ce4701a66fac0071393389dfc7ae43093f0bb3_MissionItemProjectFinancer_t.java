 package module.mission.domain;
 
 public class MissionItemProjectFinancer extends MissionItemProjectFinancer_Base {
     
     public MissionItemProjectFinancer() {
         super();
     }
 
     public MissionItemProjectFinancer(final MissionItem missionItem, final MissionFinancer missionFinancer) {
 	this();
 	setMissionItem(missionItem);
 	setMissionFinancer(missionFinancer);
     }
 
     @Override
     public String getAllFundAllocationId() {
 	final String fundAllocationId = getFundAllocationId();
 	final String projectFundAllocationId = getProjectFundAllocationId();
 	if (fundAllocationId == null || fundAllocationId.isEmpty()) {
 	    return projectFundAllocationId;
 	} else if (projectFundAllocationId == null || projectFundAllocationId.isEmpty()) {
 	    return fundAllocationId;
 	} else {
 	    return projectFundAllocationId + ", " + fundAllocationId;
 	}
     }
 
     @Override
     public void clearFundAllocations() {
 	super.clearFundAllocations();
 	final String projectFundAllocationId = getProjectFundAllocationId();
 	if (projectFundAllocationId != null && !projectFundAllocationId.isEmpty()) {
 	    getMission().registerFundAllocation(projectFundAllocationId);
 	}
 	setProjectFundAllocationId(null);
     }
 
     @Override
     protected void setNewVersionInformation(final MissionItemFinancer missionItemFinancer) {
 	super.setNewVersionInformation(missionItemFinancer);
 	final MissionItemProjectFinancer missionItemProjectFinancer = (MissionItemProjectFinancer) missionItemFinancer;
 	missionItemProjectFinancer.setProjectFundAllocationId(getProjectFundAllocationId());
     }
 
     @Override
     protected MissionItemFinancer createNewVersionInstance(final MissionItem missionItem, final MissionFinancer missionFinancer) {
 	return new MissionItemProjectFinancer(missionItem, missionFinancer);
     }
 
     @Override
     public void autoArchive() {
 	super.autoArchive();
 	archiveProject();
     }
 
     private void archiveProject() {
 	final MissionVersion missionVersion = getMissionItem().getMissionVersion();
 	setMissionVersionFromProjectArchive(missionVersion);
     }
 
     @Override
     public void archiveForAccountingUnit() {
 	super.archiveForAccountingUnit();
 	if (isCurrentUserProjectAccountant()) {
 	    archiveProject();
 	}
     }
 
     private boolean isCurrentUserProjectAccountant() {
 	final MissionFinancer missionFinancer = getMissionFinancer();
 	return missionFinancer.isCurrentUserProjectAccountant();
     }
 
     @Override
     public boolean isAccountantForUnArchivedMissionItemFinancer() {
 	return (isProjectArchived() && super.isAccountantForUnArchivedMissionItemFinancer())
 		|| (!isProjectArchived() && isCurrentUserProjectAccountant());
     }
 
     private boolean isProjectArchived() {
 	return getMissionVersionFromProjectArchive() != null;
     }
 
     @Override
     public void unArchive() {
         super.unArchive();
         setMissionVersionFromProjectArchive(null);
     }
 
    @Override
    public void delete() {
	removeMissionVersionFromProjectArchive();
        super.delete();
    }

 }
