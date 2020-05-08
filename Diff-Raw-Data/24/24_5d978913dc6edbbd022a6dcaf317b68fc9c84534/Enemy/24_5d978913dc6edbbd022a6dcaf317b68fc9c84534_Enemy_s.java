 package slick.Defend;
 
 public class Enemy {
 	private double x;
 	private double y;
 	
 	private Player player;
 	
 	public Enemy() {
 		x = 100;
 		y = 100;
 	}
 	
 	public Enemy(Player player) {
 		this();
 		this.player = player;		
 	}
 	
 	public int getX() {
 		return (int)x;
 	}
 	
 	public int getY() {
 		return (int)y;
 	}
 	
	public void think() {
 		
 		// we just want to move towards the player
 		if( x > player.getX())
			x-=0.5f;
 		if( x < player.getX())
			x+=0.5f;
 		
 		//keep this guy on the screen
 		if(x > 800)
 			x = 750;
 		if(x < 0)
 			x = 0;
 		if(y > 600)
 			y = 550;
 		if(y < 0)
 			y = 0;
 	}
 }
