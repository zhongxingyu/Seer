 package com.ion.ld27.entities;
 
 import com.ion.ld27.Game;
 import com.ion.ld27.gfx.Images;
 import com.ion.ld27.input.Keys;
 import com.ion.ld27.map.StageManager;
 
 public class Player extends Entity {
 	int regentime = 0;
 	public Player(int x, int y){
 		attack = 1;
 		health = 50;
 		healthRange = 50;
 		speed = 4;
 		imgs = Images.playerTiles;
 		currentImage = 0;
 		xpos = x;
 		ypos = y;
 		type = "player";
 	}
 
 	
 	@Override
 	public void destroy(){
		health = healthRange;
 		StageManager.initStage(StageManager.stage);
 	}
 	@Override
 	public void ai(){
 		if(tick == 1){
 			regentime += 2;
 		}
 		if(regentime >= 70){
 			regentime = 0;
 			if(health < healthRange){
 				health ++;
 			}
 		}
 		if(Keys.down){
 			if(d){
 				ypos+= speed;
 				currentImage = 0 + tick;
 			}
 			else{
 				currentImage = 0;
 			}
 		}
 		if(Keys.up){
 			if(u){
 				ypos-= speed;
 				currentImage = 6 + tick;
 			}
 			else{
 				currentImage = 6;
 			}
 		}
 		if(Keys.right){
 			if(r){
 				xpos+= speed;
 				currentImage = 4 + tick;
 			}
 			else{
 				currentImage = 4;
 			}
 		}
 		if(Keys.left){
 			if(l){
 				xpos-= speed;
 				currentImage = 2 + tick;
 			}
 			else{
 				currentImage = 2;
 			}
 		}
 		imgs();
 		r = true;
 		l = true;
 		u = true;
 		d = true;
 		for(int n = 0; n < Game.entities.size(); n++){
 			if(!Game.entities.get(n).equals(this)){
 				check(Game.entities.get(n), true);
 			}
 		}
 	}
 	
 	public void imgs(){
 		if(Keys.left && l){
 			currentImage = 2 + tick;
 		}
 		if(Keys.right && r){
 			currentImage = 4 + tick;
 		}
 		if(Keys.up && u){
 			currentImage = 6 + tick;
 		}
 		if(Keys.down && d){
 			currentImage = 0 + tick;
 		}
 	}
 }
