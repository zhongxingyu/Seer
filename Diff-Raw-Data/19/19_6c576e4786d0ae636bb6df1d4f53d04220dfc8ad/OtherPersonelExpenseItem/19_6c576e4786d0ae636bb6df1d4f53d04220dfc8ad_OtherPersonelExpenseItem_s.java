 package module.mission.domain;
 
 public class OtherPersonelExpenseItem extends OtherPersonelExpenseItem_Base {
     
     public OtherPersonelExpenseItem() {
         super();
     }
 
     @Override
     protected MissionItem createNewVersionInstance(final MissionVersion missionVersion) {
 	return new OtherPersonelExpenseItem();
     }
 
     @Override
     protected void setNewVersionInformation(final MissionItem missionItem) {
 	final OtherPersonelExpenseItem otherPersonelExpenseItem = (OtherPersonelExpenseItem) missionItem;
 	otherPersonelExpenseItem.setValue(getValue());
     }
 
 }
