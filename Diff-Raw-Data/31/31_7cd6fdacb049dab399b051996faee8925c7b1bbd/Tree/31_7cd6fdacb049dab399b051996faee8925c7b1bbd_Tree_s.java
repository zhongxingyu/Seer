 package state;
 
 public class Tree extends Resource{
 
	public Tree(int x, int y, int width, int height) {
		super(x, y, width, height, "Assets/EnvironmentObjects/DarkTree.png");
 		this.amount = 200;
 	}
 
 	public ResourceType getResType() {
 		return ResourceType.WOOD;
 	}
 
 }
