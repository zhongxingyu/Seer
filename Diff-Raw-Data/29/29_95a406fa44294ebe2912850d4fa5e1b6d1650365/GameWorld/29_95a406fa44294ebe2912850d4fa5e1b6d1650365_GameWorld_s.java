 package com.mark.games.fallingblocks;
 
 import android.util.Log;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Random;
 
 import com.mark.games.fallingblocks.framework.collision.ContinuousCollision;
 import com.mark.games.fallingblocks.framework.collision.OverlapTester;
 import com.mark.games.fallingblocks.framework.collision.SpatialHashGrid;
 
 public class GameWorld {
 
 	public static final int GAME_READY = 0;
 	public static final int GAME_RUNNING = 1;
 	public static final int GAME_PAUSED = 2;
 	public static final int GAME_GAMEOVER = 3;
 	public int state;
 	public int score;
 
 	public float starty = 552;
     public float tempNum;
 	
 	public SpatialHashGrid grid;
 	public Lava lava;
 	public SquareMan character;
 	public List<Block> blocks;
 
 	Random ran;
 
 	public GameWorld() {
 		grid = new SpatialHashGrid(5000, 5000, 200);
 		
 		ran = new Random();
 		lava = new Lava(160, -300, 320, 300);
		character = new SquareMan(160, 32, 32, 50);
 		blocks = new ArrayList<Block>();
 		state = GAME_RUNNING;
 		generateBlocks(20);
 	}
 
 	public void generateBlocks(int amount) {
 		for (int i = 0; i < amount; i++) {
 			blocks.add(new Block((ran.nextFloat() * 165), starty, 100, 100));
 			blocks.get(i).velocity.y = -180;
 			starty += 300;
 			blocks.get(i).position.x = 70 + ran.nextFloat() * 180;
 			grid.insertDynamicObject(blocks.get(i));
 		}
 	}
 
 	public void update(float deltaTime, float accelX) {
 		if (state == GAME_RUNNING) {
 			updateLava(deltaTime);
 			updateChar(deltaTime, accelX);
 			updateBlocks(deltaTime);
 		}
 	}
 
 	public void updateChar(float deltaTime, float accelX) {
 		character.update(deltaTime);
 		character.velocity.x = -(accelX * 100);
 		if (score < character.position.y - 25) {
 			score = (int) character.position.y - 25;
 		}
 	}
 
 	public void updateBlocks(float deltaTime) {
         int len = blocks.size();
         for (int i = 0; i < len; i++) {
             if ((lava.position.y - 150) >= blocks.get(i).position.y + 50) {
                 for (int j = 0; j < len; j++) {
                     if (blocks.get(j).position.y > tempNum) {
                         tempNum = blocks.get(j).position.y;
                     }
                 }
                 Log.d("position", Float.toString(tempNum) + " " + Float.toString(blocks.get(i).velocity.y));
                 blocks.get(i).state = Block.STATE_FALLING;
                 blocks.get(i).position.y = tempNum + 300;
                 blocks.get(i).velocity.y = -180;
                 blocks.get(i).position.x = 66 + ran.nextFloat() * 188;
             }
             Block block = blocks.get(i);
             block.update(deltaTime);
         }
     }
 
 	public void updateLava(float deltaTime) {
 		lava.update(deltaTime);
 		if (character.position.y - lava.position.y > 600)
 			lava.position.y = character.position.y - 500;
 	}
 
 	public void checkCollisions(float deltaTime) {
 		if (state == GAME_RUNNING) {
 			checkCharacterCollisions(deltaTime);
 			checkBoxCollision(deltaTime);
 			checkLavaCollision();
 		}
 	}
 
 	public void checkCharacterCollisions(float deltaTime) {
         for (int i = 0; i < blocks.size(); i++) {
             if (ContinuousCollision.checkYCollision(character.position.y,
                     character.bounds.height / 2, character.velocity.y
                     * deltaTime, character.acceleration.y * deltaTime,
                     blocks.get(i).position.y, blocks.get(i).height / 2)) {
                 if (character.velocity.y <= 0.0f)
                     if (character.position.x - character.bounds.width / 2 < blocks
                             .get(i).position.x + blocks.get(i).width / 2
                             && character.position.x + character.bounds.width
                             / 2 > blocks.get(i).position.x
                             - blocks.get(i).width / 2) {
                         character.hitTop(blocks.get(i).position.y,
                                 blocks.get(i).width, blocks.get(i).velocity.y);
 
                         break;
                     }
 
                 if (character.position.x - character.bounds.width / 2 < blocks
                         .get(i).position.x + (blocks.get(i).width / 2) - 10
                         && character.position.x + character.bounds.width / 2 > blocks
                         .get(i).position.x
                         - (blocks.get(i).width / 2)
                         + 10) {
                     character.hitBottomPlatform(blocks.get(i).position.y
                             - blocks.get(i).width / 2, blocks.get(i).velocity.y
                             * deltaTime);
 
                     break;
                 }
             } else if (character.state == SquareMan.STATE_STILL) {
                 character.state = SquareMan.STATE_MOVING;
                 break;
             }
         }
 
         // Hitting the side
         for (int i = 0; i < blocks.size(); i++) {
             if (OverlapTester.overlapRectangles(character.bounds,
                     blocks.get(i).bounds)) {
                 if (character.position.y - 25 < blocks.get(i).position.y
                         + blocks.get(i).width / 2
                         && character.position.y + 25 > blocks.get(i).position.y
                         - blocks.get(i).width / 2) {
                     if (character.position.x > blocks.get(i).position.x) {
                         character.hitRight(blocks.get(i).position.x,
                                 blocks.get(i).width / 2);
                     }
                 }
 
                 if (character.position.y - 25 < blocks.get(i).position.y
                         + blocks.get(i).width / 2
                         && character.position.y + 25 > blocks.get(i).position.y
                         - blocks.get(i).width / 2) {
                     if (character.position.x + 32 < blocks.get(i).position.x) {
                         character.hitLeft(blocks.get(i).position.x,
                                 blocks.get(i).width / 2);
                     }
                 }
             }
             // checking a frame ahead
             if (ContinuousCollision.checkXCollision(character.position.x,
                     character.bounds.width / 2, character.velocity.x
                     * deltaTime, blocks.get(i).position.x,
                     blocks.get(i).width / 2)) {
                 if (character.position.y - 25 < blocks.get(i).position.y
                         + blocks.get(i).width / 2
                         && character.position.y + 25 > blocks.get(i).position.y
                         - blocks.get(i).width / 2) {
                     if (character.position.x - 32 > blocks.get(i).position.x) {
                         character.hitRight(blocks.get(i).position.x, blocks.get(i).width / 2);
                         //break;
                     }
                 }
 
                 if (character.position.y - 25 < blocks.get(i).position.y
                         + blocks.get(i).width / 2
                         && character.position.y + 25 > blocks.get(i).position.y
                         - blocks.get(i).width / 2) {
                     if (character.position.x + 32 <= blocks.get(i).position.x) {
                         character.hitLeft(blocks.get(i).position.x,	blocks.get(i).width / 2);
                         //break;
                     }
                 }
             }
         }
     }
 
     public void checkBoxCollision(float deltaTime) {
         for (int j = 0; j < blocks.size(); j++) {
             for (int i = 0; i < blocks.size(); i++) {
                 if (i != j){
                     if (ContinuousCollision.checkYCollision(
                             blocks.get(j).position.y, 50,
                             blocks.get(j).velocity.y * deltaTime, 0,
                             blocks.get(i).position.y, 50)) {
                         if (blocks.get(j).position.x - blocks.get(j).width / 2 < blocks
                                 .get(i).position.x
                                 + (blocks.get(i).width / 2)
                                 - 2
                                 && blocks.get(j).position.x
                                 + (blocks.get(j).width / 2) - 2 > blocks
                                 .get(i).position.x
                                 - blocks.get(i).width / 2) {
 
                             blocks.get(j).hitBlock(
                                     blocks.get(i).position.y
                                             + blocks.get(i).width);
                             break;
                         }
                     }
                 }
                 if (ContinuousCollision.checkYCollision(blocks.get(i).position.y, 50, blocks.get(i).velocity.y
                         * deltaTime, 0, character.position.y, 25)) {
                     if (blocks.get(i).position.x - (blocks.get(i).width / 2)
                             - 4 < character.position.x + 16
                             && blocks.get(i).position.x
                             + (blocks.get(i).width / 2) - 4 > character.position.x - 16) {
                         if (character.state == SquareMan.STATE_STILL) {
                             character.state = SquareMan.STATE_DEAD;
                             state = GAME_GAMEOVER;
                             break;
                         }
                     }
                 }
             }
         }
     }
 
 	public void checkLavaCollision() {
 		if (OverlapTester.overlapRectangles(character.bounds, lava.bounds)) {
 			character.state = SquareMan.STATE_DEAD;
 			state = GAME_GAMEOVER;
 		}
 	}
 }
