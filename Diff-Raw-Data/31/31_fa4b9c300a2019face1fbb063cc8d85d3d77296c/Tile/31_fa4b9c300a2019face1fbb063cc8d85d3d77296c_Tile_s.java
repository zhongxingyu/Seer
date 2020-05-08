 package entanglement.engine;
 
 import entanglement.utils.Config;
 
 public class Tile {
 	
 	private int[] tileConf = null;
 	private Path[] pathes = null;
 
 	public Tile(){}
 	
 	public Tile(int[] tileConf){
 		this.tileConf = tileConf;
 		pathes = new Path[Config.inst().opeingsPerSide() * 2];
 	}
 	
 	public int[] getTileConf() {
 		return tileConf;
 	}
 
 	public void setTileConf(int[] tileConf) {
 		this.tileConf = tileConf;
 	}
 	
 	public void rotateClockWise(){
 		int[] tmpConf = new int[tileConf.length];
 		for (int i = 0;i < tileConf.length;i++)
 		{
 			if (i < tileConf.length - Config.inst().opeingsPerSide())
 				tmpConf[i + Config.inst().opeingsPerSide()] = tileConf[i];
 			else
 				tmpConf[i - tileConf.length - 2] = tileConf[i];
 		}
 		
 		tileConf = tmpConf;
 	}
 	
 	public void rotateAntiClockWise() {
 		int[] tmpConf = new int[tileConf.length];
		for (int i = tileConf.length; i > 0; i--) {
 			if (i < tileConf.length - Config.inst().opeingsPerSide())
 				tmpConf[i - Config.inst().opeingsPerSide()] = tileConf[i];
 			else
				tmpConf[i + tileConf.length - 2] = tileConf[i];
 		}
 	}
 }
