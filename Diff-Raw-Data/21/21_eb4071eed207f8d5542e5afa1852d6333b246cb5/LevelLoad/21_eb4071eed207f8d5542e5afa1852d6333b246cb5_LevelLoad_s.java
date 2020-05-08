 package com.teamcoffee.game;
 
 import java.util.HashSet;
 import java.util.Set;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.math.Vector2;
 import com.teamcoffee.game.models.Block;
 import com.teamcoffee.game.models.Entity;
 import com.teamcoffee.game.models.Fire;
 import com.teamcoffee.game.models.Spike;
 import com.teamcoffee.game.models.Staircase;
 
 public class LevelLoad {
 	
 	
 	public static LevelInfo loadLevel(int levelNumber){		int startx = 0;
 		int starty = 0;
 		Set<Entity> entities = new HashSet<Entity>();
 		
 		switch(levelNumber){
 		case 0:
 			startx = 50;
 			starty = 100;
 		
 			for (int i = 0; i < 30; i++){
 				if(i == 8){
 					entities.add(new Fire(new Vector2(i * 48, 48), 48, 48));
 				}
 				if (i == 10){
					entities.add(new Block(new Vector2(i * 48, 200), 48, 48));
 				}
 				if(i == 15){
 					entities.add(new Spike(new Vector2(i * 48, 0), 48, 48));
 					entities.add(new Block(new Vector2(i * 48, Gdx.graphics.getHeight() - 48), 48, 48));
 					i++;
 				}
 				entities.add(new Block(new Vector2(i * 48, 0), 48, 48));
 				entities.add(new Block(new Vector2(i * 48, Gdx.graphics.getHeight() - 48), 48, 48));
 			}
 			
 			entities.add(new Block(new Vector2(100, 48), 48, 48));
 			entities.add(new Block(new Vector2(200, 48), 48, 48));
 			entities.add(new Block(new Vector2(200, 48 * 2), 48, 48));
 			//entities.add(new Fire(new Vector2(500, 48), 48, 48));
 			
 			entities.add(new Staircase(new Vector2(1200, 48), 96, 96));
 			break;
 		case 1:
 			
 			startx = 50;
 			starty = 100;
 			for (int i = 0; i < 30; i++){
 				if (i>7 && i<18){
 					entities.add(new Spike(new Vector2(i*48, 48),48,48));
 				}
 				entities.add(new Block(new Vector2(i * 48, 0), 48, 48));
 				entities.add(new Block(new Vector2(i * 48, Gdx.graphics.getHeight() - 48), 48, 48));
 			}
 //			entities.add(new Block(new Vector2(100, 50), 50, 50));
 //			entities.add(new Block(new Vector2(230, 100), 50, 50));
 //			entities.add(new Block(new Vector2(230, 200), 50, 50));
 //			entities.add(new Block(new Vector2(230, 250), 50, 50));
 //			entities.add(new Spike(new Vector2(230, 50), 50, 50));
 			break;
 		case 2:
 			
 			startx = 50;
 			starty = 100;
 			for (int i = 0; i < 30; i++){
 				entities.add(new Block(new Vector2(i * 48, 0), 48, 48));
 				entities.add(new Block(new Vector2(i * 48, Gdx.graphics.getHeight() - 48), 48, 48));
 			}
 			break;
 		
 		}
 
 		LevelInfo info = new LevelInfo(startx, starty, entities);
 		
 		return info;
 		
 	}
 	
 }
