 package asrsController;
 
 import order.Location;
 
 public interface Warehouse {
	public abstract void retrieveProduct(Location location, Integer robotId);
 	
	public abstract void bringToBinPacker();
 	
	public abstract void moveToStart(Integer robotId);
 	
 	public abstract Integer getRobots();
 }
