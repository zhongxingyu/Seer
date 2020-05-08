 import java.awt.*;
 import javax.sound.sampled.*;
 
 public class Enemy extends Rectangle {
 
 	public AudioHandler audioHandler = new AudioHandler();
 	
 	public HealthBar healthBar;
 
 	public int xC;
 	public int yC;
 	public int walkFrame = 0;
 	public int walkSpeed = 20;
 	
 	public int health;
 	public int armor;
 	
 	public int enemySize = Room.blockSize;
 	public int enemyId;
 	public int enemyWalk = 0;
 	
 	public int down = 0;
 	public int left = 1;
 	public int right = 2;
 	public int up = 3;
 	public int direction = right;
 	
 	public boolean inGame = false;
 	public boolean isDead = false;
 	public boolean wasDown = false;
 	public boolean wasUp = false;
 	public boolean wasRight = false;
 	public boolean wasLeft = false;
 	
 	public int animationId = 0;					// The index of the next sprite to get in an animation
 	public int animationUpdatesPerFrame = 60;	// The number of screen updates it takes to update a the frame of an
 												// 	animation (Set this to 2 for vibrating zombies)
 	public int animationUpdateIndex = 1;		// Keeps track of how many screen updates have passed since the last 
 												//	animation frame was drawn
 	
 	public static final int animationBase = 0;	// The index of the base animation frame
 	public int animationWalkStart;				// The index of the first frame of the walking animation
 	public int animationWalkEnd;				// The index of the last frame of the walking animation
 	
 	public void spawnEnemy(int enemyId) {
 
 		for(int y = 0; y < Room.block.length; y++) {
 			if(Room.block[y][0].groundId == Value.pathOpen) {
 				setBounds(Room.block[y][0].x, Room.block[y][0].y, enemySize, enemySize);
 				xC = 0;
 				yC = y;
 			}
 		}
 		this.enemyId = enemyId;
 
 		healthBar = new HealthBar(this);
 		
 		health = Value.getZombieStats("health", enemyId);
 		armor = Value.getZombieStats("armor", enemyId);
 		
 		inGame = true;
 		
 		if (this.enemyId == 0){
 			this.animationWalkStart = 1;
 			this.animationWalkEnd = 4;
 		}
 		else{
 			this.animationWalkStart = 0;
 			this.animationWalkEnd = 0;
 		}
 		
 	}
 	
 	public void deleteEnemy() {
 		inGame = false;
 		isDead = true;
 		if(Frame.gameScreen.level <= 5) {
 			Screen.myGold += Value.getZombieStats("health", enemyId) / 10;
 		}
 		else Screen.myGold += Value.getZombieStats("health", enemyId) / 20;
 		Screen.numEnemiesDead += 1;
 		Screen.totalEnemiesDead += 1;
 	}
 	
 	public void looseHealth() {
 		Screen.myHealth -= health;
 		if( Screen.myHealth < 0 )
 			Screen.myHealth = 0;
 		if( Screen.myHealth != 0 ) {
 			try {
 				audioHandler.soundHandler.playSound("res/Sounds/zombie1.wav");
 			}
 			catch (Exception e) { e.printStackTrace(); }
 		}
 	}
 	
 	public void looseEnemyHealth(int towerDmg) {
 		health -= towerDmg - armor;
 	}
 	
 	public void physic() {
 		if(walkFrame >= walkSpeed) {
 			if(direction == down) {
 				y += 1;
 			}
 			else if(direction == left) {
 				x -= 1;
 			}
 			else if(direction == right) {
 				x += 1;
 			}
 			else if(direction  == up) {
 				y -= 1;
 			}
 			
 			enemyWalk += 1;
 			
 			if(enemyWalk == Room.blockSize) {
 				if(direction == down) {
 					yC += 1;
 					wasDown = true;
 				}
 				else if(direction == left) {
 					xC -= 1;
 					wasLeft = true;
 				}
 				else if(direction == right) {
 					xC += 1;
 					wasRight = true;
 				}
 				else if(direction  == up) {
 					yC -= 1;
 					wasUp = true;
 				}
 				
 				if(!wasUp) {
 					try {
 						if(Room.block[yC+1][xC].groundId == Value.pathOpen) {
 							direction = down;
 						}
 					} catch (Exception e) {
 						
 					}
 				}
 				if(!wasRight) {
 					try {
 						if(Room.block[yC][xC-1].groundId == Value.pathOpen) {
 							direction = left;
 						}
 					} catch (Exception e) {
 						
 					}
 				}
 				if(!wasLeft) {
 					try {
 						if(Room.block[yC][xC+1].groundId == Value.pathOpen) {
 							direction = right;
 						}	
 					} catch (Exception e) {
 						
 					}
 				}	
 				if(!wasDown) {
 					try {
 						if(Room.block[yC-1][xC].groundId == Value.pathOpen) {
 							direction = up;
 						}
 					} catch (Exception e) {
 						
 					} 
 				}
 				
 				if(Room.block[yC][xC].airId == Value.airCastle) {
 					deleteEnemy();
 					looseHealth();
 				}
 				
 				wasUp = false;
 				wasDown = false;
 				wasRight = false;
 				wasLeft = false;
 				
 				enemyWalk = 0;
 			}
 			
 			walkFrame = 0;
 			
 		}
 		else {
 			walkFrame += 2;
 		}
 		
 		if(health <= 0) {
 			deleteEnemy();
 			try {
 				audioHandler.soundHandler.playSound("res/Sounds/zombie2.wav");
 			}
 			catch (Exception e) { e.printStackTrace(); }
 		}
 	}
 	
 	public void draw(Graphics g) {
 		healthBar.draw(g);
 		if(inGame && !isDead) {
 			
			if(direction == left){
				if(enemyId < 5)
					g.drawImage(ScreenPanel.sprites.getSprite("enemy", enemyId, this.animationId), x, y, x+64, y+64, 64, 0, 0, 64, null);
				else
					g.drawImage(ScreenPanel.sprites.getSprite("boss", enemyId-5, this.animationId), x-96, y-128, (x-96)+256, (y-128)+256, 256, 0, 0, 256, null);
			}
			else {
				if(enemyId<5)
					g.drawImage(ScreenPanel.sprites.getSprite("enemy", enemyId, this.animationId), x, y, width, height, null);
				else
					g.drawImage(ScreenPanel.sprites.getSprite("boss", enemyId-5, this.animationId), x-96, y-128, width+192, height+192, null);
			}
 			if(this.animationUpdateIndex == this.animationUpdatesPerFrame){
 				this.animationId = nextAnimationFrame();
 				this.animationUpdateIndex = 1;
 			}
 			
 			this.animationUpdateIndex++;
 		}
 	}
 	
 	public int nextAnimationFrame() {
 				
 		// If idle, start the walking this.animation
 		if (this.animationId == animationBase)
 			return this.animationWalkStart;
 	
 		// Go through each frame of the walking this.animation
 		else if(this.animationWalkStart <= this.animationId && this.animationId < this.animationWalkEnd)  
 			return (this.animationId + 1);
 		
 		// Repeat the walking this.animation
 		else if (this.animationId == this.animationWalkEnd)
 			return this.animationWalkStart;;
 		
 		// If all else fails, return the base sprite
 		return 0;
 	}
 }
