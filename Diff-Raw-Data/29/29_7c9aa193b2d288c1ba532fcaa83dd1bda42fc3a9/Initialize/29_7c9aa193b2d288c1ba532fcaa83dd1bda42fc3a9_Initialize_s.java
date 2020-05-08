 
 public class Initialize {
 
 	/**
 	 * @param args
 	 */
 	public static void game() {
 		// TODO Auto-generated method stub
 
 		//Initialize inventory
 		Globals.health=100;
		Globals.life=3;
 		Globals.magician=50;
 		Globals.money=20;
 		Globals.stop=30;
		Globals.weapon=15;
 		
 		//activate walls
 		Globals.wall=true;
 		
 		//Initialize enemy
 		Globals.enemy.x=0.5;
 		Globals.enemy.y=0.5;
 		Globals.enemy.step=0.003;
 		Globals.enemy.move=true;
 		
 		//Initialize player
 		Globals.player.x=0.2;
 		Globals.player.y=0.2;
 		Globals.player.step=0.005;
 		
 		//Start in Room1, Level1
 		Globals.room=1;
 		Globals.level=1;
 		
 		StdDraw.setCanvasSize(600, 600);
 		
 		
 	
 	}
 
 }
