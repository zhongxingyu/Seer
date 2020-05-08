 public class Room{
 
 	private int xCoord;
 	private int yCoord;
 	private int zCoord;
 	private boolean[] exits = new boolean[6]; //list of exits
 	private ItemBag items; //list of items in the room
 	private String description; //base description of room
 
 	
 	public Room(int x, int y, int z, String description)
 	{
 		this.xCoord = x;
 		this.yCoord = y;
 		this.zCoord = z;
 		this.description = description;
 	}
 
 	public Room(int x, int y, int z, String description, boolean nEx, boolean sEx, boolean eEx, boolean wEx, boolean upEx, boolean dnEx)
 	{
 		this.xCoord = x;
 		this.yCoord = y;
 		this.zCoord = z;
 		this.description = description;
 		this.exits[0] = nEx;
 		this.exits[1] = sEx;
 		this.exits[2] = eEx;
 		this.exits[3] = wEx;
 		this.exits[4] = upEx;
 		this.exits[5] = dnEx;
 	}
 
 	public void setExits(boolean nEx, boolean sEx, boolean eEx, boolean wEx, boolean upEx, boolean dnEx)
 	{
 		this.exits[0] = nEx;
 		this.exits[1] = sEx;
 		this.exits[2] = eEx;
 		this.exits[3] = wEx;
 		this.exits[4] = upEx;
 		this.exits[5] = dnEx;
 	}
 
 	public void setAnExit(boolean exit, int i)
 	{
 		this.exits[i] = exit;
 	}
 
 	public int getXCoord()
 	{
 		return xCoord;
 	}
 
 	public int getYCoord()
 	{
 		return yCoord;
 	}
 
 	public int getZCoord()
 	{
 		return zCoord;
 	}
 
 	public String examine()
 	{
 		return description;
 	} 
 
 	public ItemBag getItems(){
 		return this.items;
 	}
 
 	public boolean checkExit(int i){
 		return exits[i];
 	}
 
 	public String toString(){
 		return xCoord + ", " + yCoord + ", " + zCoord;
 	}
 
 }
