 /**
  * vC[\킷NX
  */
 
 package Game.Sprite;
 
 import Game.Common.*;
 import Game.Sprite.Weapon.*;
 
 import java.awt.Graphics;
 
 public class Player extends Sprite{
 	/**
 	 * vC[̃Wv{^xꂽǂ
 	 * ꂪȂƃWv{^ςȂŉxWvĂ܂
 	 */
 	public boolean jumpReleased = true;
 	// ̃Wvꂽǂ̍U{^
 	public boolean attackReleased = true;
 	// RC
 	public int coin;
 	// 
 	public Weapon weapon;
 	/**
 	 * Ƒ傫̒`
 	 */
 	
 	/**
 	 * U󂯂uԂ̃t[
 	 * t[GƂmbNobNŉt[ȂƂ
 	 * ̕ӂ̔fɗp
 	 */
 	private int attackedFrame;
 	
 	// Ct(͂lێĂ邾A}CiXɂȂ낤֌WȂ)
 	private int life;
 	
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
 	private int movingSpeed = 6;
 	private int jumpSpeed = 15;
 	private static final int jumpMax = 2;
 	
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
 		life = 10;
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
 		/**
 		 * GԂуmbNobN̏
 		 */
 		if(invisible){
 			/**
 			 * mbNobNAs\
 			 */
 			if(Data.frame - attackedFrame < 10){
 				return;
 			}
 			/**
 			 * G؂鏈
 			 * _[WĂ45t[(Ȃ1.5b)疳G
 			 */
 			if(Data.frame - attackedFrame > 45){
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
 			if(jumpCount == 0) speed = 2 * movingSpeed;
 		}else{
 			if(jumpCount == 0) speed = movingSpeed;
 		}
 		/**
 		 * d͕Z
 		 * AData.CHIP_SIZE𒴂Ə蔲錻ۂ̂
 		 * őlɂȂ悤ɂĂ
 		 * 󒆂Data.CHIP_SIZEXNvgꍇAKv
 		 */
 		if((Data.frame&1)==0) vy += Data.gravity;
 		if(vy > Data.CHIP_SIZE) vy = Data.CHIP_SIZE-1;
 		/**
 		 * U{^ꂽƂ̏
 		 * ̂ƂKɍ\[h
 		 * Iɂ͂ǂ̑ɂĕʂ̃CX^X
 		 * ǂ̑ĂϐDataNXɍ邱ƂɂȂƎv
 		 */
 		if(KeyStatus.attack){
 			if(weapon == null){
 				if(jumpCount == 0 && attackReleased){
 					attackReleased = false;
 					// switch(equipment){ case Data.SWORD: break; case Data.ARROW: break }݂ɏꍇ\
 					switch(direction){
 					case UP:
 					        weapon = new Arrow(Weapon.UP);
 //						weapon = new Sword(Weapon.UP);
 						break;
 					case DOWN:
 						weapon = new Arrow(Weapon.DOWN);
 //						weapon = new Sword(height, Weapon.DOWN);
 						break;
 					case LEFT:
 						weapon = new Arrow(Weapon.LEFT);
 //						weapon = new Sword(Weapon.LEFT);
 						break;
 					case RIGHT:
 						weapon = new Arrow(Weapon.RIGHT);
 //						weapon = new Sword(Weapon.RIGHT);
 						break;
 					}
 				}
 			}
 		}else{
 			attackReleased = true;
 		}
 		/**
 		 * \[hŐ؂ĂŒ͓Ȃ悤ɂĂ
 		 * \[hoʒuŌŒ肵Ă̂ŁAƂƂ߂荞
 		 */
 		if(weapon != null && weapon instanceof Sword){
 			vx = 0;
 			vy = 0;
 		}else{
 			/**
 			 * ړWv̏
 			 * ǂXvCgɓ锻MapNXōs
 			 * ł͉QȂꍇɂǂꂾړ邩ݒ肷
 			 */
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
 			if(KeyStatus.jump){
 				if(jumpReleased && jumpCount < jumpMax){
 					// Wv{^  Wvłꍇ
 					jumpReleased = false;
 					jump = true;
 					jumpCount++;
 					// Wv+1(+1͒ɏd͂Ō镪)
 					vy = -(jumpSpeed+Data.gravity);
 				}
 			}else{
 				jumpReleased = true;
 			}
			if(jumpReleased && vy < 0){
 				jump = false;
 				vy = 0;
 			}
 		}
 	}
 	/**
 	 * d _[W󂯂Ƃ̏
 	 * ̓mbNobNƂ̑x͐lœKɂĂ
 	 */
 	public void damage(int d){
 		life -= d;
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
 		g.drawString("life : " + life, 15,15);
 		g.drawString("player : " + x + ", " + y, 15, 30);
 		g.drawString("coin : " + coin, 15, 45);
 	}
 }
