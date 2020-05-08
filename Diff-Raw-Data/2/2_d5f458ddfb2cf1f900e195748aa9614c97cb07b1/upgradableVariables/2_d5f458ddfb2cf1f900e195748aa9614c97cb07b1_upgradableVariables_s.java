 package rsmg.model;
 /**
  * Class representing variables that are possible to upgrade
  * @author Johan Grnvall
  *
  */
 public enum upgradableVariables {
 	;
 	private static int UPGRADEDCHARSPEED;
 	
 	public static int charSpeed = 100;
 	
	public void upgradeSpeed() {
 		charSpeed = UPGRADEDCHARSPEED;
 	}
 }
