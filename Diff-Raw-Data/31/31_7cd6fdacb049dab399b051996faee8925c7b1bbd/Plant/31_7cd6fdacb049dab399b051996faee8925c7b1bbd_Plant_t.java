 package state;
 
 public class Plant extends Resource{
 
	public Plant(int x, int y) {
		super(x, y, 1, 1, "Assets/EnvironmentObjects/Plants.png");
 		this.amount = 100;
 	}
 
 	public ResourceType getResType() {
 		return ResourceType.PLANT;
 	}
 
 }
