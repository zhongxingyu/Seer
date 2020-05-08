 /**
  * vC[\킷NX
  */
 
 package Game.Sprite;
 
 import Game.Common.*;
 import Game.Sprite.Weapon.*;
 import Game.Body.*;
 
 import java.awt.Graphics;
 import java.awt.Color;
 
 public class Player extends Sprite{
 	// ̃t[ɉɒntrue
 	private boolean landing = false;
 	
 	private boolean shiftReleased = true;
 	
 	/**
 	 * vC[̃Wv{^xꂽǂ
 	 * ꂪȂƃWv{^ςȂŉxWvĂ܂
 	 */
 	public boolean jumpReleased = true;
 	// ̃Wvꂽǂ̍U{^
 	public boolean attackReleased = true;
 	// RC
 	public int coin;
 	// ǂ̕𑕔Ă邩
 	public int weaponID;
 	// ǂ̑𑕔Ă邩
 	public int element;
 	// 
 	public Weapon weapon;
 	// ̂̑
 	public int bodyID;
 	
 	/**
 	 * U󂯂uԂ̃t[
 	 * t[GƂmbNobNŉt[ȂƂ
 	 * ̕ӂ̔fɗp
 	 */
 	private int attackedFrame;
 	
 	// Ct(͂lێĂ邾A}CiXɂȂ낤֌WȂ)
 	private int life;
 	//
 	private int lifeMax;
 	
 	/**
 	 * ړXs[h
 	 * (͉ɒ`Ă)x̒lp
 	 * ۂɎlǂ̂炢̃Xs[hœ̂\킷
 	 */
 	private int speed;
 	
 	/**
 	 * ړx֌W
 	 * ͂ɒ`Ă邪
 	 * Iɂ͑Sg̃NXQƂ
 	 */
 	private int invisibleTime;
 	private int nockBackTime;
 	private int defence;
 	private int movingSpeed;
 	private int jumpSpeed;
 	private int jumpMax;
 	private int froat;
 	
 	public void equipBody(int id){
 		bodyID = id;
 		equipBody();
 	}
 	
 	public void equipBody(){
 		Body body = null;
 		switch(bodyID){
 		case 0:
 			body = new Body0();
 			break;
 		case 1:
 			body = new Body1();
 			break;
 		default:
 			break;
 		}
 		froat = body.getFroat();
 		invisibleTime = body.getInvisibleTime();
 		nockBackTime = body.getNockBackTime();
 		defence = body.getDefence();
 		movingSpeed = body.getMovingSpeed();
 		jumpMax = body.getJumpMax();
 		jumpSpeed = body.getJumpSpeed();
 	}
 	
 	public Player clone(){
 		Player newPlayer = new Player();
 		newPlayer.x = this.x;
 		newPlayer.y = this.y;
 		newPlayer.image = this.image;
 		newPlayer.vx = this.vx;
 		newPlayer.vy = this.vy;
 		newPlayer.width = this.width;
 		newPlayer.height = this.height;
 		newPlayer.life = this.life;
 		newPlayer.weaponID = this.weaponID;
 		newPlayer.element = this.element;
 		newPlayer.bodyID = this.bodyID;
 		return newPlayer;
 	}
 
 
 	/**
 	 * RXgN^
 	 * ȂƂȂȂo[WĂ邯
 	 * ͎gĂȂAIɂgȂ
 	 */
 	public Player(){
 		this(0,0);
 	}
 	/**
 	 * RXgN^
 	 * l̏ʒuɂƂ
 	 * ʒuƊeϐ̏
 	 */
 	public Player(int x, int y){
 		super(x,y);
 		image = Data.image.playerImage;
 		vx = 0; vy = 0;
 		width = 32;
 		height = 32;
 		lifeMax = 10;
 		life = 10;
 		weaponID = 0;
 		element = 0;
 		equipBody(0);
 	}
 	public void land(){
 		super.land();
 		landing = true;
 	}
 	
 	public void testAtShift(){
 		if(KeyStatus.pause){
 			if(shiftReleased){
 				StateData.mapData.unpassSpriteList.add(new BrokenChip(x+1,y,0,0)); 
 				
 				shiftReleased = false;
 			}
 		}else{
 			shiftReleased = true;
 		}
 	}
 	
 	public boolean landing(){
 		return landing;
 	}
 	
 	/**
 	 * p֐
 	 * cƂĂd͂Ƃ̘bł͂Ȃ
 	 * ĊR痎ꍇȂǂɕȂ߂̊֐
 	 */
 	public void fall(){
 		if(jumpCount == 0) jumpCount = 1;
 	}
 	
 	public void mapHit(int dir, int dest){
 		switch(dir){
 		case UP:
 			vy = dest - y;
 			break;
 		case DOWN:
 			vy = dest - y;
 			land();
 			break;
 		case LEFT:
 		case RIGHT:
 			vx = dest - x;
 			break;
 		}
 	}
 	/**
 	 * WvĂ true
 	 */
 	public boolean jumping(){
 		return jump;
 	}
 	/**
 	 * et[łupdatep֐
 	 */
 	public void update(){
 		landing = false;
 		/**
 		 * GԂуmbNobN̏
 		 */
 		if(invisible){
 			/**
 			 * mbNobNAs\
 			 */
 			if(Data.frame - attackedFrame < nockBackTime){
 				return;
 			}
 			/**
 			 * G؂鏈
			* _[WĂinvisibleFramẽt[(Ȃ1.5b)疳G
 			 */
 			if(Data.frame - attackedFrame > invisibleTime){
 				invisible = false;
 			}
 		}
 		/**
 		 * 15t[ƂɃOtBbÑXe[^Xς
 		 * ̕ωŃvjvjA
 		 */
 		animationUpdate(15);
 		/**
 		 * vC[̉̑x
 		 * _bV{^Ă 2 {ɂĂ
 		 * ̂Ƃ돑ĂȂAspeed
 		 * ǂ蔲錻ۂN̂Œ
 		 * ႦData.CHIP_SIZE傫ƕǂ𔲂悤ɂȂ
 		 */
 		if(KeyStatus.dash){
 			if(jumpCount == 0) speed = movingSpeed<<1;
 		}else{
 			if(jumpCount == 0) speed = movingSpeed;
 		}
 		
 		// shift{^ƂŉeXg
 		testAtShift();
 		
 		/**
 		 * d͕Z
 		 * AData.CHIP_SIZE𒴂Ə蔲錻ۂ̂
 		 * őlɂȂ悤ɂĂ
 		 * 󒆂Data.CHIP_SIZEXNvgꍇAKv
 		 */
 		if(vy == 0) vy = 1;
 		if((Data.frame%froat)==0) vy += Data.gravity;
 		if(vy > Data.CHIP_SIZE) vy = Data.CHIP_SIZE-1;
 		/**
 		 * U{^ꂽƂ̏
 		 */
 		if(KeyStatus.attack){
 			if(weapon == null){
 				if(/*jumpCount == 0 && */attackReleased){
 					attackReleased = false;
 					attack();
 				}
 			}
 		}else{
 			attackReleased = true;
 		}
 		/**
 		 * \[hŐ؂ĂŒ͓Ȃ悤ɂĂ
 		 * \[hoʒuŌŒ肵Ă̂ŁAƂƂ߂荞
 		 */
 		/*
 		if(weapon != null && weapon instanceof Sword){
 			vx = 0;
 			vy = 0;
 		}else{
 			*/
 			/**
 			 * ړWv̏
 			 * ǂXvCgɓ锻MapNXōs
 			 * ł͉QȂꍇɂǂꂾړ邩ݒ肷
 			 */
 			setMove();
 			setJump();
 //		}
 	}
 	
 	/**
 	 * Wv̏
 	 */
 	public void setJump(){
 		if(KeyStatus.jump){
 			if(jumpReleased && jumpCount < jumpMax){
 				// Wv{^  Wvłꍇ
 				jumpReleased = false;
 				jump = true;
 				jumpCount++;
 				// Wv
 				vy = -jumpSpeed;
 			}
 		}else{
 			jumpReleased = true;
 		}
 		if((jumpReleased || !jump) && vy < 0){
 			jump = false;
 			vy = 0;
 		}
 	}
 	/**
 	 * l̈ړʂƂƂL[ɉăZbg֐
 	 */
 	public void setMove(){
 		if(KeyStatus.up){
 			direction = UP;
 		}
 		if(KeyStatus.down){
 			direction = DOWN;
 		}
 		if(KeyStatus.left){
 			if(KeyStatus.right){
 				vx = 0;
 			}else{
 				vx = -speed;
 				direction = LEFT;
 			}
 		}
 		if(!KeyStatus.left){
 			if(KeyStatus.right){
 				vx = speed;
 				direction = RIGHT;
 			}else{
 				vx = 0;
 			}
 		}
 	}
 	
 	/**
 	 * U鏈
 	 */
 	public void attack(){
 		switch(weaponID){
 		case Weapon.SWORD:
 			weapon = new Sword();
 			break;
 		case Weapon.ARROW:
 			weapon = new Arrow();
 			break;
 		}
 		switch(direction){
 		case UP:
 			weapon.setDirection(Weapon.UP);
 			break;
 		case DOWN:
 			weapon.setDirection(Weapon.DOWN);
 			break;
 		case LEFT:
 			weapon.setDirection(Weapon.LEFT);
 			break;
 		case RIGHT:
 			weapon.setDirection(Weapon.RIGHT);
 			break;
 		}
 		weapon.element = element;
 		weapon.appear();
 	}
 	
 	/**
 	 * d _[W󂯂Ƃ̏
 	 * ̓mbNobNƂ̑x͐lœKɂĂ
 	 */
 	public void damage(int d){
 		if(invisible) return;
 		if(d>defence){
 			life -= (d-defence);
 		}else{
 			life -= 1;
 		}
 		if(weapon != null && weapon instanceof Sword){
 			weapon = null;
 		}
 		switch(direction){
 		case UP:
 			vx = 0;
 			vy = 3;
 			break;
 		case DOWN:
 			vx = 0;
 			vy = -3;
 			break;
 		case LEFT:
 			vx = 3;
 			vy = 0;
 			break;
 		case RIGHT:
 			vx = -3;
 			vy = 0;
 			break;
 		}
 		invisible = true;
 		attackedFrame = Data.frame;
 	}
 
 	/**
 	 * `揈
 	 */
 	public void draw(Graphics g, int screenX, int screenY){
 		if(!invisible || Data.frame % 2 != 0){
 		    super.draw(g,screenX,screenY);
 		}
 		if(weapon != null) weapon.draw(g, screenX, screenY);
 		g.setColor(Color.WHITE);
 
 		LifeGauge.draw(g, x-screenX+width/2, y-screenY, life, lifeMax);
 		g.drawString("life : " + life, 15,15);
 		g.drawString("player : " + x + ", " + y, 15, 30);
 		g.drawString("coin : " + coin, 15, 45);
 	}
 }
