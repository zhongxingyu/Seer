 package state;
 
 public class Tree extends Resource{
 
	public Tree(int x, int y) {
		super(x, y, 1, 1, "Assets/EnvironmentObjects/DarkTree.png");
 		this.amount = 200;
 	}
 
 	public ResourceType getResType() {
 		return ResourceType.WOOD;
 	}
 
 }
