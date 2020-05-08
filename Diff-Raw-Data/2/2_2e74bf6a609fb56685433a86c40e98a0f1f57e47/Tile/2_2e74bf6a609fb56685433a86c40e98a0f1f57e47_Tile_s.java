 package entanglement.engine;
 
 
 public class Tile {
 	
 	private int[] tileConf = null;
	
 
 	
 	public Tile(){}
 	
 	public Tile(int[] tileConf){
 		this.tileConf = tileConf;
 	}
 	
 	public int[] getTileConf() {
 		return tileConf;
 	}
 
 	public void setTileConf(int[] tileConf) {
 		this.tileConf = tileConf;
 	}
 }
