 /**
  * ׂẴCxg̊ՃNX
  * GAd|(XCb`ƂH)AƂ
  * vC[̍sɂĉω̂
  * ̃NXpč
  */
 
 package Game.Sprite.Enemy;
 
 import Game.Sprite.LifeGauge;
 import Game.Sprite.Player;
 import Game.Common.*;
 import Game.MapData.MapData;
 import Game.Sprite.*;
 import Game.Sprite.Weapon.*;
 
 import java.awt.Graphics;
 
 public class Enemy extends Sprite{
 	public int enemyID;
 	protected int life;
 	protected int lifeMax;
 	protected int attackedSpriteID = -1;
 	protected int power = 1;
 	public Enemy(int x, int y){
 		super(x,y);
 	}
 	public void touch(Sprite s, int dir, int[] dest){
 		if(s instanceof Player){
 			Player tmp = (Player)s;
 			if((dir & (1 << HIT_DIRECT)) > 0){
 				tmp.damage(power);
 			}else{
 				if((dir & (1 << UP)) > 0){
 					tmp.setVy(y+vy-tmp.getY()+height-Data.CD_DIFF-2);
 //					tmp.setVy(dest[UP]-tmp.getY()-2+vy);
 				}
 				if((dir & (1 << DOWN)) > 0){
 					tmp.setVy(y+vy-tmp.getY()-tmp.getHeight()+Data.CD_DIFF+2);
 //					tmp.setVy(dest[DOWN]-tmp.getY()+2+vy);
 				}
 				if((dir & (1 << LEFT)) > 0){
 					tmp.setVx(x+vx-tmp.getX()-Data.CD_DIFF+width-2);
 //					tmp.setVx(dest[LEFT]-tmp.getX()-2+vx);
 				}
 				if((dir & (1 << RIGHT)) > 0){
 					tmp.setVx(x+vx-tmp.getX()-tmp.getWidth()+Data.CD_DIFF+2);
 //					tmp.setVx(dest[RIGHT]-tmp.getX()+2+vx);
 				}
 			}
 		}
 	}
 	public void update(MapData mapData){
 		if(life <= 0){
 			end = true;
 			mapData.passSpriteList.add(new Coin(x+1,y+1));
 			return;
 		}
 		animationUpdate(15);
 		if((Data.frame&1)==0) vy += Data.gravity;
 		if(vy >= Data.CHIP_SIZE) vy = Data.CHIP_SIZE - 1;
//		if(vy < 0) vy = 0;
 	}
 	
 	public void mapHit(int dir, int dest){
 		switch(dir){
 		case UP:
 		case DOWN:
 			vy = dest - y;
 			break;
 		case LEFT:
 			direction = Sprite.RIGHT;
 			vx = dest - x;
 			break;
 		case RIGHT:
 			direction = Sprite.LEFT;
 			vx = dest - x;
 			break;
 		}
 	}
 	
 	// ̃Cxgɑ΂ĕID weapon, ID elementōUƂ̊֐
 	public void attacked(Sprite s){
 		/**
 		 * spriteID͂ꂼŗL̕Ȃ̂
 		 * Ƃ̕spriteIDێĂA
 		 * ႤspriteID̂Ƃ_[W邱Ƃɂ
 		 * ł̑iqbgȂĂ
 		 */
 		if(s instanceof Weapon){
 			Weapon tmp = (Weapon)s;
 			if(attackedSpriteID != tmp.getSpriteID()){
 				attackedSpriteID = tmp.getSpriteID();
 				life -= tmp.power;
 			}
 		}
 	}
 	
 	public void draw(Graphics g, int screenX, int screenY){
 		super.draw(g,screenX,screenY);
 		LifeGauge.draw(g,x-screenX+width/2, y-screenY,life,lifeMax);
 	}
 	
 }
