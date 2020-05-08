 package team.GunsPlus.Enum;
 
 
 public enum Projectile {
	ARROW(1.0), FIREBALL(1.0), SNOWBALL(1.0), EGG(1.0), ENDERPEARL(1.0), FIRECHARGE(1.0), NONE(1.0);
 	
 	private double speed;
 	
 	Projectile(double speed){
 		this.speed = speed;
 	}
 	
 	
 	public double getSpeed(){
 		return speed;
 	}
 	
 	public void setSpeed(double speed){
 		this.speed = speed;
 	}
 
 }
