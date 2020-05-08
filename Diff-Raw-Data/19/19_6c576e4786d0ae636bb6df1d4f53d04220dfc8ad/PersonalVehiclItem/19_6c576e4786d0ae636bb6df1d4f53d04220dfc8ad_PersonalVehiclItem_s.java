 package module.mission.domain;
 
 public class PersonalVehiclItem extends PersonalVehiclItem_Base {
     
     public PersonalVehiclItem() {
         super();
     }
 
     @Override
     protected MissionItem createNewVersionInstance(final MissionVersion missionVersion) {
 	return new PersonalVehiclItem();
     }
 
     @Override
     protected void setNewVersionInformation(final MissionItem missionItem) {
 	final PersonalVehiclItem personalVehiclItem = (PersonalVehiclItem) missionItem;
 	personalVehiclItem.setKms(getKms());
     }
 
 }
