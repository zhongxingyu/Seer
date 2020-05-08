 package com.ion.ld27.entities.mobs;
 
 import java.util.ArrayList;
 
 import com.ion.ld27.Game;
 import com.ion.ld27.entities.Entity;
 import com.ion.ld27.gfx.Images;
 import com.ion.ld27.map.Map;
 
 public class Rat extends Entity {
 	int timer = 0;
 	int pathTimer = 0;
 	ArrayList<Integer> path = new ArrayList<Integer>();
 	
 	boolean left = false;
 	boolean right = false;
 	boolean up = false;
 	boolean down = false;
 	
 	public Rat(int x, int y){
		attack = (int) (baseAttack*2);
 		healthRange = (int) (baseHealth*6);
 		health = (int) (baseHealth*4);
 		speed = 2;
 		imgs = Images.ratTiles;
 		currentImage = 0;
 		xpos = x;
 		ypos = y;
 		type = "rat";
 	}
 	
 	@Override
 	public void ai(){
 		if(canPunch > 0){
 			canPunch--;
 		}
 		if(pathTimer == 15){
 			if(newPath){
 				path = Game.astar.path(xpos/64, ypos/64, Game.entities.get(0).xpos/64, Game.entities.get(0).ypos/64);
 				newPath = false;
 			}
 			pathTimer = -1;
 			if(path.size() > 0){
 				left = false;  
 				right = false; 
 				up = false;    
 				down = false;
 				if(path.get(0) == 1){
 					right = true;
 				}
 				else if(path.get(0) == 2){
 					down = true;
 				}
 				else if(path.get(0) == 3){
 					left = true;
 				}
 				else if(path.get(0) == 4){
 					up = true;
 				}
 				path.remove(0);
 			}
 			else if(Map.random.nextInt(2) == 1){
 				left = false;  
 				right = false; 
 				up = false;    
 				down = false;  
 				int d = Map.random.nextInt(5);
 				if(d == 1){
 					right = true;
 				}
 				else if(d == 2){
 					down = true;
 				}
 				else if(d == 3){
 					left = true;
 				}
 				else if(d == 4){
 					up = true;
 				}
 			}
 			
 		}
 		pathTimer++;
 		if(health <= 0){
 			destroy();
 		}
 		if(left){
 			if(l){
 				xpos-= speed;
 
 				currentImage = 2 + tick;
 			}
 		}
 		if(right){
 			if(r){
 				xpos+= speed;
 				currentImage = 4 + tick;
 			}
 		}
 		if(up){
 			if(u){
 				ypos-= speed;
 
 				currentImage = 6 + tick;
 			}
 		}
 		if(down){
 			if(d){
 				ypos+= speed;
 				currentImage = 0 + tick;
 			}
 		}
 		if(timer >= 20){
 			timer = 0;
 		}
 //		imgs();
 		r = true;
 		l = true;
 		u = true;
 		d = true;
 		for(int n = 0; n < Game.entities.size(); n++){
 			if(!Game.entities.get(n).equals(this)){
 				check(Game.entities.get(n), false);
 			}
 		}
 	}
 	
 //	public void imgs(){
 //		if(l){
 //			currentImage = 2 + tick;
 //		}
 //		if(r){
 //			currentImage = 4 + tick;
 //		}
 //		if(u){
 //			currentImage = 6 + tick;
 //		}
 //		if(d){
 //			currentImage = 0 + tick;
 //		}
 //	}
 }
