 package module.mission.domain;
 
 import myorg.domain.util.Money;
 
 public class MissionItemFinancer extends MissionItemFinancer_Base {
     
     public MissionItemFinancer() {
         super();
         setMissionSystem(MissionSystem.getInstance());
         setOjbConcreteClass(getClass().getName());
         setAmount(Money.ZERO);
     }
 
     public MissionItemFinancer(final MissionItem missionItem, final MissionFinancer missionFinancer) {
 	this();
 	setMissionItem(missionItem);
 	setMissionFinancer(missionFinancer);
     }
 
     public void delete() {
	removeMissionVersionFromArchive();
 	removeMissionItem();
 	removeMissionFinancer();
 	removeMissionSystem();
 	deleteDomainObject();
     }
 
     public String getDescription() {
 	final StringBuilder stringBuilder = new StringBuilder();
 	final MissionItem missionItem = getMissionItem();
 	stringBuilder.append(missionItem.getLocalizedName());
 	stringBuilder.append(" - ");
 	stringBuilder.append(missionItem.getItemDescription());
 	return stringBuilder.toString();
     }
 
     public String getAllFundAllocationId() {
         return getFundAllocationId();
     }
 
     public void clearFundAllocations() {
 	final String fundAllocationId = getFundAllocationId();
 	if (fundAllocationId != null && !fundAllocationId.isEmpty()) {
 	    getMission().registerFundAllocation(fundAllocationId);
 	}
 	setFundAllocationId(null);
     }
 
     public Mission getMission() {
 	return getMissionFinancer().getMissionVersion().getMission();
     }
 
     MissionItemFinancer createNewVersion(final MissionFinancer missionFinancer, final MissionItem missionItem) {
 	final MissionItemFinancer missionItemFinancer = createNewVersionInstance(missionItem, missionFinancer);
 	setNewVersionInformation(missionItemFinancer);
 	return missionItemFinancer;
     }
 
     protected MissionItemFinancer createNewVersionInstance(final MissionItem missionItem, final MissionFinancer missionFinancer) {
 	return new MissionItemFinancer(missionItem, missionFinancer);
     }
 
     protected void setNewVersionInformation(final MissionItemFinancer missionItemFinancer) {
 	missionItemFinancer.setAmount(getAmount());
 	missionItemFinancer.setFundAllocationId(getFundAllocationId());
     }
 
     public void autoArchive() {
 	archive();
     }
 
     private void archive() {
 	final MissionVersion missionVersion = getMissionItem().getMissionVersion();
 	setMissionVersionFromArchive(missionVersion);
     }
 
     public boolean isArchived() {
 	return getMissionVersionFromArchive() != null;
     }
 
     public void archiveForAccountingUnit() {
 	if (isCurrentUserAccountant()) {
 	    archive();
 	}
     }
 
     private boolean isCurrentUserAccountant() {
 	final MissionFinancer missionFinancer = getMissionFinancer();
 	return missionFinancer.isCurrentUserAccountant();
     }
 
     public boolean isAccountantForUnArchivedMissionItemFinancer() {
 	return !isArchived() && isCurrentUserAccountant();
     }
 
     public void unArchive() {
 	setMissionVersionFromArchive(null);
     }
 
 }
