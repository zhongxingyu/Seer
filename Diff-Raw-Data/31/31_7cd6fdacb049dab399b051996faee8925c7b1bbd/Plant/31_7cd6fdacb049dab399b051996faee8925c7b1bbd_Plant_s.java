 package state;
 
 public class Plant extends Resource{
 
	public Plant(int x, int y, int width, int height) {
		super(x, y, width, height, "Assets/EnvironmentObjects/Plants.png");
 		this.amount = 100;
 	}
 
 	public ResourceType getResType() {
 		return ResourceType.PLANT;
 	}
 
 }
