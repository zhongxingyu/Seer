 package annahack.nethackinformation.map;
 
 import annahack.nethackinformation.lutz.Item;
 import java.util.ArrayList;
 
 public class Tile 
 {
 	private byte type; //this is the character for the tile
 					   //i.e. a floor tile's type is #
 					   //'?' is unknown
 	
 	private ArrayList<Item> lutz; //Stuff piled atop the square
 	private boolean lutzKnown;
 	
 	public Tile()
 	{
 		lutzKnown=false;
 		type='?';
 		lutz=new ArrayList<Item>();
 	}
 	
 	public Tile(byte tipe, boolean areLutzKnown, ArrayList<Item> items)
 	{
 		lutzKnown = areLutzKnown;
 		type=tipe;
 		lutzKnown=areLutzKnown;
 	}
 
	protected byte getType() {
 		return type;
 	}
 
	protected void setType(byte type) {
 		this.type = type;
 	}
 
	protected ArrayList<Item> getLutz() {
 		return lutz;
 	}
 
	protected void setLutz(ArrayList<Item> lutz) {
 		this.lutz = lutz;
 	}
 
	protected boolean isLutzKnown() {
 		return lutzKnown;
 	}
 
	protected void setLutzKnown(boolean lutzKnown) {
 		this.lutzKnown = lutzKnown;
 	}
 }
