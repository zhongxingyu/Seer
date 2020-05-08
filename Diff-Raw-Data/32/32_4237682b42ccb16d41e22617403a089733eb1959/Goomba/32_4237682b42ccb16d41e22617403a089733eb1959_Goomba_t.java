 package characters;
 
 
 
 
 import java.util.Random;
 
 import map.DungeonCollision;
 
 import core.Board;
 import core.FileLink;
 import core.GameManager;
 
 public class Goomba extends AbstractEnemy implements Runnable, FileLink {
 	
 	private static Player player;
 	private static DungeonCollision dungeonCollision;
 	
 	public Goomba(){
 		initializeEnemy(FileLink.goomba, 400, 200, 65, 75, 1, 0, true, true, 0, 3);
 
 		//public AbstractEnemy(File sprite, int x, int y, int height, int width, int speed, int life, boolean hostile, boolean dungeon, int xMap, int yMap)
 		System.out.println("add goomba");
 		
 		
 	}
 	
 	public Goomba(Player player, DungeonCollision dungeonCollision){
 		this.player = player;
 		this.dungeonCollision = dungeonCollision;
 		setPlayerObject(this.player);
 		setDungeonCollisionObject(this.dungeonCollision);
 	}
 	
 	
 	public void run(){
 		
 		Random randomBoolean = new Random();
 	    randomBoolean.nextBoolean();
 	    
 		
 		if (GameManager.printMsg)
 			System.out.println("Enemy.run");
 		
 		if(life > 0){
 			setBoundMain();
 			setBoundS();
 			
 			followPlayer();
 			move();
 			
 			paintEnemy();
 			if(oldLife < life){
 				
 				oldLife = life;
 			}
 				
 			
 		}
 		
 		if(life <= 0){
 			x = -100;
 			y = -100;
 		}
 
 	}
 	
 	private void followPlayer(){
		if(GameManager.printMsg)
			System.out.println("goomba:"+x+" to "+ player.getX());
 		
 		if(x > player.getX()){
 			moveRight = false;
 			moveLeft = true;
 			
 			lastDirection = 2;
 		}
 		
 		if(x < player.getX()){
 			moveLeft = false;
 			moveRight = true;
 			
 			lastDirection = 6;
 		}
 		
 		System.out.println("goomba:"+y+" to "+ player.getY());
 		
 		if(y > player.getY()){
 			moveDown = false;
 			moveUp = true;
 			
 			lastDirection = 4;
 		}
 		
 		if(y < player.getY()){
 			moveUp = false;
 			moveDown = true;
 			
 			lastDirection = 8;
 		}
 		
 		
 	}
 	
 	public void loseLife(){
 		System.out.println("loseLife");
 		int fallBack = 3;
 		int playerLastPosition = player.getLastDirection();
 		
 		for(int i = 0; i < 10; i++){
 			switch(playerLastPosition){
 			case 1: y -= fallBack;
 					break;
 			case 2: x += fallBack; y -= fallBack;
 					break;
 			case 3: x += fallBack;
 					break;
 			case 4: x += fallBack; y += fallBack;
 					break;
 			case 5: y += fallBack;
 					break;
 			case 6: x -= fallBack; y += fallBack;
 					break;
 			case 7: x -= fallBack;
 					break;
 			case 8: x -= fallBack;y -= fallBack;
 					break;
 			}
 		}
 		
 		life -= 1;
 	}
 	
 	public void setLife(int life){
 		this.life = life;
 	}
 	
 	public void setSpeed(int speed){
 		this.speed = speed;
 	}
 	
 	
 
 	
 }
