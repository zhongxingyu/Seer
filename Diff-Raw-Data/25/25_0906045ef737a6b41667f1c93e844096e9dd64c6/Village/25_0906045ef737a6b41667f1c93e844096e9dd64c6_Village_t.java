 package v1;
 
 public class Village implements grensesnitt.Bosetning{
 private int gull, soldater, mat;
 
 	public Village() {
 		this.soldater = (int) (51*(Math.random()));
 		this.gull = (int) (soldater/4);
 		this.mat = (int) (soldater/2);
 	}
 
 	public int getSoldater() {
 		return soldater;
 	}
 
 	public int getMat() {
 		return mat;
 	}
 
 	public int getGull() {
 		return gull;
 	}
 	
	public void addSoldater(int soldier) {
		this.soldater += soldier;
 	}
 }
