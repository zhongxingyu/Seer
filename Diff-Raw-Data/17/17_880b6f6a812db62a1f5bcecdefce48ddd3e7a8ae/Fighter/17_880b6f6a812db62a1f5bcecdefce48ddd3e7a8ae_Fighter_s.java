 
 public class Fighter {
 	
 		//attributes every fighter has.
 	
 		private String name;
 		
 		private BasicAttack [] attacks;
 		
 		private String description;
 		private Ability [] abilities;
 		private Block block;
 		private int life;
 		private int speed;
 		private int height;
 		private boolean air;
 		private int width; //width of the character model.
 		private double xPos, yPos;
 		private int xSpeed, ySpeed;	//used for acceleration
 		private boolean disabled; //for Character effects
 		
 		//set/get methods for Xpos Ypos
 		public void setX(double x){	xPos = x;	}
 		public void setY(double y){	yPos = y;	}
		public void setYSpeed(int y){ ySpeed = y; }
		public void yFall(){ ySpeed--; }
		public void xAccel(){ xSpeed+= 2; }
		public void xDecel(){ xSpeed--; }
 		
		public int getYSpeed(){ return ySpeed; }
		public int getXSpeed(){ return xSpeed; }
 		public double getX(){	return xPos;	}
 		public double getY(){	return yPos;	}
 		
 		//getting the basic Attacks
 		public BasicAttack getAttack(int i){	return attacks[i];	}
 		
 		//getting the ability
 		public Ability getAbility(int i){	return abilities[i];	}
 		
 		//getting the block
 		public Block getBlock(){	return block;	}
 		
 		//disabled?
 		public boolean isDisabled(){	return disabled;	}
 		public void lockedDown(){	disabled=true;		}
 		public void free(){	disabled=false;	}
 		
 		//get functions for name, life and speed, height, and selected
 		public String getName(){	return name;	}
 		public int getLife(){	return life;	}
 		public int getSpeed(){	return speed;	}
 		public int getHeight(){	return height;	}
 		public int getWidth() {	return width;	}
 		public String getDescription() { return description; }
 		
 		
 		//getter and setting methods for air function.
 		public boolean inAir(){		return air;		}
 		public void startJump(){	air = true;		}
 		public void endJump(){		air = false;	}
 		
 		//returning whether or not the player is disabled
 		
 		
 		public Fighter(String n, BasicAttack [] a, Ability [] ab, Block b, int s){
 			
 			name=n;
 			attacks = a;
 			abilities = ab;
 			block = b;
 			
 			life=100;	
 			speed =s;
 			disabled = false;
 			
 			//inAir is to tell if a player is jumping in air, to add jumping abilities/fights.
 			air=false;
 			
 			//basic pre-set up x and y positions
 			xPos =-99;
 			yPos=-99;
 			
 			//to be changed for character model height;
 			height = 10;
 			
 			width = 5; //just for testing changed to width of image?
 			
 			description = "This is a test, this is a test this is a test\nThis is s new line test. Testing testing testing";
 		}
 		
 		public void hurt(int d){
 			life-=d;
 		}
 
 }
