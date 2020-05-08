 package btlshp.turns;
 
 import java.io.Serializable;
 
 import btlshp.Btlshp;
 import btlshp.entities.Map;
 import btlshp.entities.Ship;
 
 class LaunchTorpedo extends Turn implements Serializable{
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1790493199271040630L;
 	private Map m;
 	private Ship s;
 	private boolean success = false;
 
 
 	LaunchTorpedo(Map m2, Ship s2) {
 		this.m = m2;
 		this.s = s2;
 	}
 
 	@Override
 	public void executeTurn() {
 		try{
 			m.fireTorpedo(s);
 			success = true;
 		}catch(IllegalStateException e){
 			success = false;
 		}
 	}
 
 
 	@Override
 	public boolean wasSuccessful() {
 		return success;
 	}
 	@Override
 	public String toString(){
 		return "launchTorpedo";
 	}
 
 	@Override
 	public void setMap(Map m) {
		this.m = m;
 		
 	}
 }
