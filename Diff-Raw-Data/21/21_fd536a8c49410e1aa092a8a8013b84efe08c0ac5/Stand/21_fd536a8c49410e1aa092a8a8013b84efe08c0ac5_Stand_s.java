 package factory.interfaces;
 
 import factory.Kit.KitState;
 
 
 public interface Stand {
 	
 	void msgResultsOfKitAtInspection(KitState results);
 	void msgEmptyKitIsHereAndWantToDeliver();
 	void msgKitRobotNoLongerUsingStand();
 	void msgPartRobotWantsToPlaceParts();
	
 
 }
