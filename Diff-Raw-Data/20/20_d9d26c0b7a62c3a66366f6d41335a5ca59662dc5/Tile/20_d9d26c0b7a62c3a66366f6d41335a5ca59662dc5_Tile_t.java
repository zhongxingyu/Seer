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
 
	public byte getType() {
 		return type;
 	}
 
	public void setType(byte type) {
 		this.type = type;
 	}
 
	public ArrayList<Item> getLutz() {
 		return lutz;
 	}
 
	public void setLutz(ArrayList<Item> lutz) {
 		this.lutz = lutz;
 	}
 
	public boolean isLutzKnown() {
 		return lutzKnown;
 	}
 
	public void setLutzKnown(boolean lutzKnown) {
 		this.lutzKnown = lutzKnown;
 	}

	
 }
