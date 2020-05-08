 package game;
 
 import settings.Settings;
 import graphics.AnimatedSprite;
 import gui.InputHandler;
 
 public class Player extends LivingEntity {
 
 	private int state;
 	private boolean leftKey,rightKey,jumpKey;
 	
 	protected final static int STATE_STANDING=0;
 	protected final static int STATE_RUNNING=1;
 	protected final static int STATE_JUMPING=2;
 	protected final static int STATE_FALLING=3;
 	
 	// statics fields
 	public static double GRAVITY = 0.1;
 	public static double JUMPFORCE = 50.0;
 	public static double RUNSPEED = 5.0;
 	
 	
 	
 	private Updater updater;
 	
 	private AnimatedSprite standingLeft,standingRight,jumpingLeft,jumpingRight,fallingLeft,fallingRight;
 	
 	public Player(Updater u) {
 		
 		super(0,0,null,null);
 		this.updater = u;
 		
 
 		
 		// we define the AnimatedSprite
 		this.standingLeft = standingLeft;
 		this.standingRight = standingRight;
 		this.jumpingLeft = jumpingLeft;
 		this.jumpingRight = jumpingRight;
 		this.fallingLeft = fallingLeft;
 		this.fallingRight = fallingRight;
 		
 		// we define the keys
 		this.leftKey = false;
 		this.rightKey = false;
 		this.jumpKey = false;
 		
 		// we define the width and the height
 		this.width = 64;
 		this.height = 64;
 		
 	}
 	
 	public void jump(){
 		if (this.state == Player.STATE_RUNNING || this.state == Player.STATE_STANDING){
 			this.speedY = -Player.JUMPFORCE;
 			this.state = Player.STATE_JUMPING;
 		}
 	}
 	
 	public void update(Updater u){
 		//Key check
			if (InputHandler.input[Settings.jumpKeyCode])
 				this.jumpKey = true;
			else 
 				this.jumpKey = false;
 			
 			if (InputHandler.input[Settings.leftKeyCode])
 				this.leftKey = true;
 			else 
 				this.leftKey = false;
 			
 			if (InputHandler.input[Settings.rightKeyCode])
 				this.rightKey = true;
 			else 
 				this.rightKey = false;
 		
 			
 		// we first update the  Y speed
 		this.speedY += Player.GRAVITY;
 		if (this.jumpKey) {
 				this.jump();
 		}	
 		
 		// now we do the X speed
 		if (this.leftKey) {
 			this.speedX = -Player.RUNSPEED;
 			this.direction = false;
 		} else if (this.rightKey){
 			this.speedX = Player.RUNSPEED;
 			this.direction = true;
 		} else {
 			this.speedX = 0;
 		}
 			
 		//State update
 		
 		// falling state
 		if (this.speedY > 0) {
 			this.state = Player.STATE_FALLING;
 		}
 		
 		// running state
 		if (this.speedX != 0 && this.speedY == 0 && this.state != Player.STATE_RUNNING) {
 			this.state = Player.STATE_RUNNING;
 		}
 		
 		if (this.speedX == 0 && this.speedY == 0 && this.state != Player.STATE_STANDING) {
 			this.state = Player.STATE_STANDING;
 		}
 		
 		
 		// then we update the position
 		this.posY += this.speedY;
 		this.posX += this.speedX;
 		
 		
 		//Sprite display
 		
 	}
 	
 }
