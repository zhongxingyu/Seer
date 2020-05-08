 /**
  * ̊ՃNX
  */
 
 package Game.Sprite.Weapon;
 
 import Game.Common.*;
 import Game.MapData.MapData;
 import Game.Sprite.Player;
 import Game.Sprite.Sprite;
 
 import java.awt.Graphics;
 import java.awt.Color;
 
 public class Shot extends Weapon{
 	public int frame;
 	
 	public Shot(){
 		super(0,0);
 		this.vx = 0;
 		this.vy = 0;
 		power = 1;
 		enemy = false;
 	}
 	
 	public Shot(int x, int y, int vx, int vy, int size){
 		super(x,y);
 		setV(vx,vy);
 		power = 1;
 		enemy = true;
 		this.width = size;
 		this.height = size;
 		appear();
 	}
 
 	public void appear(){
 		frame = Data.frame;
 		if(!enemy){
 			switch(direction){
 			case UP:
 				x = StateData.player.getX() + StateData.player.getWidth()/2;
 				y = StateData.player.getY();
 				vy = -4;
 				break;
 			case DOWN:
 				x = StateData.player.getX() + StateData.player.getWidth()/2;
 				y = StateData.player.getY() + StateData.player.getHeight();
 				vy = 4;
 				break;
 			case LEFT:
 				x = StateData.player.getX();
 				y = StateData.player.getY() + StateData.player.getHeight()/2;
 				vx = -4;
 				break;
 			case RIGHT:
 				x = StateData.player.getX() + StateData.player.getWidth();
 				y = StateData.player.getY() + StateData.player.getHeight()/2;
 				vx = 4;
 				break;
 			}
 		}
 	}
 	
 	public void touch(Sprite s, int dir, int[] dest){
 		if(!enemy) return;
 		if(s instanceof Player){
 			if((dir & (1<<HIT_DIRECT)) > 0){
 				StateData.player.damage(power);
 				end = true;
 			}
 			else{
 				if((dir & (1<<UP))>0){
 					s.setVy(y+vy+height-s.getY()-Data.CD_DIFF-2);
 //					vy = dest[UP]-y-height+Data.CD_DIFF+2;
 //					StateData.player.damage(power);
 //					end = true;
 				}
 				if((dir & (1<<DOWN))>0){
 					s.setVy(y+vy-s.getHeight()-s.getY()+Data.CD_DIFF+2);
 //					vy = dest[DOWN]-y+s.getHeight()-Data.CD_DIFF-2;
 //					StateData.player.damage(power);
 //					end = true;
 				}
 				if((dir & (1<<LEFT))>0){
 					s.setVx(x+vx+width-s.getX()-Data.CD_DIFF-2);
 //					vx = dest[LEFT]-x-width+Data.CD_DIFF+2;
 //					StateData.player.damage(power);
 //					end = true;
 				}
 				if((dir & (1<<RIGHT))>0){
 					s.setVx(x+vx-s.getWidth()-s.getX()+Data.CD_DIFF+2);
 //					vx = dest[RIGHT]-x+s.getWidth()-Data.CD_DIFF-2;
 //					StateData.player.damage(power);
 //					end = true;
 				}
 			}
 		}
 	}
 	
 	// ͂ǂ̕ɓƂ̐Öʒu
 	public void mapHit(int dir, int dest){
 //		end = true;
 	}
 	// ̏Ԃupdate(͔ԂAłt[oĂ邩Ƃ)
 	public void update(MapData mapData){
 		if(!enemy) move();
 	}
 	
 	public void screenOut(){
 		end = true;
 	}
 	
 	// `揈
 	public void draw(Graphics g, int screenX, int screenY){
 		g.setColor(Color.WHITE);
 		g.fillOval(x-screenX,y-screenY,width,height);
 	}
 }
