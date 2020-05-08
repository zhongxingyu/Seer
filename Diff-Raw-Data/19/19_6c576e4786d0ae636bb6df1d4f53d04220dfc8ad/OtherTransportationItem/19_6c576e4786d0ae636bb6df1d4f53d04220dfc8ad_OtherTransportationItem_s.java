 package module.mission.domain;
 
 public class OtherTransportationItem extends OtherTransportationItem_Base {
 
     public OtherTransportationItem() {
         super();
     }
 
     @Override
     protected MissionItem createNewVersionInstance(final MissionVersion missionVersion) {
 	return new OtherTransportationItem();
     }
 
     @Override
     protected void setNewVersionInformation(final MissionItem missionItem) {
 	final OtherTransportationItem otherTransportationItem = (OtherTransportationItem) missionItem;
 	otherTransportationItem.setTypeOfTransportation(getTypeOfTransportation());
     }
 
 }
