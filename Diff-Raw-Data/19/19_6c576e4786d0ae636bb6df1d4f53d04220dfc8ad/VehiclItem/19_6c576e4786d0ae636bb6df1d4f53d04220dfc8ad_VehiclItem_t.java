 package module.mission.domain;
 
 
 public abstract class VehiclItem extends VehiclItem_Base {
     
     public VehiclItem() {
         super();
         new VehiclItemJustification(this);
     }
 
     @Override
     public void delete() {
 	final VehiclItemJustification vehiclItemJustification =getVehiclItemJustification();
 	if (vehiclItemJustification != null) {
 	    vehiclItemJustification.delete();
 	}
 	super.delete();
     }
 
     @Override
     public boolean isVehicleItem() {
         return true;
     }
 
     @Override
     protected void setNewVersionInformation(final MissionItem missionItem) {
	super.setNewVersionInformation(missionItem);
 	final VehiclItem vehiclItem = (VehiclItem) missionItem;
 	vehiclItem.setVehiclItemJustification(getVehiclItemJustification());
     }
 
 }
