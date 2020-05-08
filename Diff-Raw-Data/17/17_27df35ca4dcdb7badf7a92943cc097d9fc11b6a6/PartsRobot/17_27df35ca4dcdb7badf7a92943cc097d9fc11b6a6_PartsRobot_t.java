 package factory.interfaces;
 
 import factory.Coordinate;
 import factory.KitConfig;
 import factory.Part;
 
 public interface PartsRobot {
 
 	public void msgBuildKitAtSlot(String slot);
 	public void msgDeliverKitParts();
 	public void msgMakeKit(KitConfig kitConfig);
 	public void msgClearLineOfSight(Nest nestOne, Nest nestTwo);
 	public void msgPictureTaken(Nest nestOne, Nest nestTwo);
	public void msgHereArePartCoordinatesForNest(Nest nest, Part part, int coordinate);
 }
