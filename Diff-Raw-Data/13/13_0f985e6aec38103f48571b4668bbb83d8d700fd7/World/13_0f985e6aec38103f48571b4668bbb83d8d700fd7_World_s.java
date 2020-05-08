 package com.gmail.robmadeyou.World;
 
 import com.gmail.robmadeyou.Screen;
 import com.gmail.robmadeyou.Block.Block;
 import com.gmail.robmadeyou.Block.BlockAir;
 import com.gmail.robmadeyou.Block.BlockStone;
 import com.gmail.robmadeyou.Entity.Entity;
 import com.gmail.robmadeyou.Input.Mouse;
 import com.gmail.robmadeyou.Screen.GameType;
 
 public class World {
 	
 	public static int WorldArrayWidth = 0;
 	public static int WorldArrayHeight = 0;
 	
 	/*
 	 * Array list that holds the current world currently visible on screen
 	 */
 	public static Block[][] blockList;
 	
 	public static int blockEffectX = 0;
 	public static int blockEffectY = 0;
 	
 	public static String typeEffect = BlockAir.class.toString();
 	public static final int BLOCK_SIZE(){
 		if(Screen.WorldTileSize != 32){
 			return Screen.WorldTileSize;
 		}
 		return 32;
 	};
 	
 	public static int getWorldWidthInPixels(){
 		return WorldArrayWidth * World.BLOCK_SIZE();
 	}
 	public static int getWorldHeightInPixels(){
 		return WorldArrayHeight * World.BLOCK_SIZE();
 	}
 	
 	public static double gravity(int delta){
 		return (delta * 0.04);
 	};
 	private static int camXDivided = (int) Math.round(Screen.translate_x / BLOCK_SIZE());
 	private static int camYDivided = (int) Math.round(Screen.translate_y / BLOCK_SIZE());
 	private static int camWidthDivided = (int) Math.round(Screen.getWidth() / BLOCK_SIZE()) + 1;
 	private static int camHeightDivided = (int) Math.round(Screen.getHeight() / BLOCK_SIZE()) + 1;
 	
 	public static void setWorldDimensions(int x, int y){
 		blockList = new Block[x][y];
 		WorldArrayWidth = x;
 		WorldArrayHeight = y;
 	}
 	private static BlockAir air_Block = new BlockAir(0, 0);
 	public static Block getBlockTypeAtLocation(int x, int y){
 		if(x < WorldArrayWidth && x >= 0 && y < WorldArrayHeight && y >= 0){
 			return blockList[x][y].getType();
 		}
 		return air_Block;
 		
 	}
 	public static boolean isSolidAtLocation(int x, int y){
 		if(getBlockTypeAtLocation(x, y).isSolid()){
 			return true;
 		}
 		return false;
 	}
 	public static void setArrayListClear(){
 		for(int x = 0; x < WorldArrayWidth; x++){
 			for(int y = 0; y < WorldArrayHeight; y++){
 				if(y == WorldArrayHeight - 2 || y == WorldArrayHeight - 1){
 					if(Screen.TypeOfGame == GameType.SIDE_SCROLLER){
 						blockList[x][y] = new BlockStone(x, y);
 					}else{
 						blockList[x][y] = new BlockAir(x, y);
 					}
 				}else{
 					blockList[x][y] = new BlockAir(x, y);
 				}
 			}
 		}
 	}
 	/*
 	 * Checks collision for the legs of the entity, this will
 	 * decide if player can go down, or when falling is possible to jump on the block and stop
 	 * instead of falling constantly
 	 */
 	public static boolean isSolidUnder(Entity e){
 		int eX = (int)e.getX();
 		int eY = (int)e.getY();
 		int eW = e.getWidth();
 		int eH = e.getHeight();
 		
 		int bDimensions = World.BLOCK_SIZE();
 		/*
 		 * Starting math to decide where the for loop should start from
 		 * and end from, taking into consideration the array lengths so the
 		 * engine no longer crashes when player is out of bounds
 		 */
 		int startX = (int) Math.round(e.getX() / BLOCK_SIZE()) - 2;
 		if(startX < 0){
 			startX = 0;
 		}
 		int durationX = startX + 4;
 		while(durationX + startX >= WorldArrayWidth){
 			durationX--;
 		}
 		
 		int startY = (int) Math.round(e.getY() / BLOCK_SIZE()) - 2;
 		if(startY < 0){
 			startY = 0;
 		}
 		int durationY = startY + 4;
 		while(durationY + startY >= WorldArrayHeight){
 			durationY--;
 		}
 		for(int sX = startX; sX < startX + durationX + 1; sX++){
 			for(int y = startY; y < startY + durationY; y++){
 				int x;
 				if(sX >= 1){
 					x = sX -1;
 				}else{
 					x = sX;
 				}
 				int bX = blockList[x][y].getX() * World.BLOCK_SIZE();
 				int bY = blockList[x][y].getY() * World.BLOCK_SIZE();
 				//TODO Do a for loop :(
 				//Bottom left
 				boolean one = eX >= bX && eX <= bX + bDimensions && eY + eH + 10 >= bY && eY + eH <= bY + 7;
 				//Bottom right
 				boolean two = eX + eW >= bX && eX + eW <= bX + bDimensions && eY + eH + 10 >= bY && eY + eH <= bY + 7;
 				if(one || two){
 					if(blockList[x][y].isSolid()){
 						e.setY(bY - 1 - eH);
 						e.doEffectFromBlock(blockList[x][y].getType());
 						blockEffectX = x;
 						blockEffectY = y;
 						return true;
 					}
 				}
 			}
 		}
 		return false;
 	}
 	
 	public static boolean isSolidAbove(Entity e){
 		int eX = (int)e.getX();
 		int eY = (int)e.getY();
 		int eW = e.getWidth();
 		int bDimensions = World.BLOCK_SIZE();
 		
 		int startX = (int) Math.round(e.getX() / BLOCK_SIZE()) - 2;
 		if(startX < 0){
 			startX = 0;
 		}
 		int durationX = startX + 4;
 		while(durationX + startX >= WorldArrayWidth){
 			durationX--;
 		}
 		
 		int startY = (int) Math.round(e.getY() / BLOCK_SIZE()) - 2;
 		if(startY < 0){
 			startY = 0;
 		}
 		int durationY = startY + 4;
 		while(durationY + startY >= WorldArrayHeight){
 			durationY--;
 		}
 		for(int sX = startX; sX < startX + durationX + 1; sX++){
 			for(int y = startY; y < startY + durationY; y++){
 				int x;
 				if(sX >= 1){
 					x = sX -1;
 				}else{
 					x = sX;
 				}
 				int bX = blockList[x][y].getX() * World.BLOCK_SIZE();
 				int bY = blockList[x][y].getY() * World.BLOCK_SIZE();
 				for(int x2 = 0; x2 <= eW / 4; x2++){
 					boolean one = eX + (x2 * 4) >= bX && eX + (x2 * 4) <= bX + bDimensions && eY + 5 >= bY + bDimensions - 5 && eY  - 5<= bY + bDimensions + 5;
 					if(one){
 						if(blockList[x][y].isSolid()){
 							e.setY(y * BLOCK_SIZE() + BLOCK_SIZE() + 5);
 							return true;
 						}
 					}
 				}
 			}
 		}
 		return false;
 	}
 	
 	public static boolean isSolidLeft(Entity e){
 		int eX = (int)e.getX();
 		int eY = (int)e.getY();
 		int bDimensions = World.BLOCK_SIZE();
 		
 		int startX = (int) Math.round(e.getX() / BLOCK_SIZE()) - 2;
 		if(startX < 0){
 			startX = 0;
 		}
 		int durationX = startX + 4;
 		while(durationX + startX >= WorldArrayWidth){
 			durationX--;
 		}
 		
 		int startY = (int) Math.round(e.getY() / BLOCK_SIZE()) - 2;
 		if(startY < 0){
 			startY = 0;
 		}
 		int durationY = startY + 4;
 		while(durationY + startY >= WorldArrayHeight){
 			durationY--;
 		}
 		for(int sX = startX; sX < startX + durationX + 1; sX++){
 			for(int y = startY; y < startY + durationY; y++){
 				int x;
 				if(sX >= 1){
 					x = sX -1;
 				}else{
 					x = sX;
 				}
 				int bX = blockList[x][y].getX() * World.BLOCK_SIZE();
 				int bY = blockList[x][y].getY() * World.BLOCK_SIZE();
 
 				for(int y2 = 0; y2 <= e.getHeight() / 4; y2++){
 					boolean one = eX <= bX + bDimensions + 3 && eX >= bX + bDimensions - 3 && eY - 1+ (4 * y2) >= bY && eY- 1 + (4 * y2) <= bY + bDimensions;
 					if(one){
 						if(blockList[x][y].isSolid()){
 							e.setX(x * BLOCK_SIZE() + BLOCK_SIZE() + 2);
 							return true;
 						}
 					}
 				}
 			}
 		}
 		return false;
 	}
 	public static boolean isSolidRight(Entity e){
 		int eX = (int)e.getX();
 		int eY = (int)e.getY();
 		int eW = e.getWidth();
 		int bDimensions = World.BLOCK_SIZE();
 		
 		int startX = (int) Math.round(e.getX() / BLOCK_SIZE()) - 2;
 		if(startX < 0){
 			startX = 0;
 		}
 		int durationX = startX + 4;
 		while(durationX + startX >= WorldArrayWidth){
 			durationX--;
 		}
 		
 		int startY = (int) Math.round(e.getY() / BLOCK_SIZE()) - 2;
 		if(startY < 0){
 			startY = 0;
 		}
 		int durationY = startY + 4;
 		while(durationY + startY >= WorldArrayHeight){
 			durationY--;
 		}
 		for(int sX = startX; sX < startX + durationX + 1; sX++){
 			for(int y = startY; y < startY + durationY; y++){
 				int x;
 				if(sX >= 1){
 					x = sX -1;
 				}else{
 					x = sX;
 				}
 				
 				int bX = blockList[x][y].getX() * World.BLOCK_SIZE();
 				int bY = blockList[x][y].getY() * World.BLOCK_SIZE();
 				
 				for(int y2 = 0; y2 <= e.getHeight() / 4; y2++){
 					boolean one = eX + eW<= bX + 3 && eX + eW>= bX - 3 && eY - 1+ (4 * y2) >= bY && eY- 1 + (4 * y2) <= bY + bDimensions;
 					if(one){
 						if(blockList[x][y].isSolid()){
 							return true;
 						}
 					}
 				}
 			}
 		}
 		return false;
 	}
 	public static void onUpdate(){
 		camXDivided = (int) Math.round(-Screen.translate_x / BLOCK_SIZE());
 		camYDivided = (int) Math.round(-Screen.translate_y / BLOCK_SIZE());
 		
 		int mX = Math.round((Mouse.getX() / BLOCK_SIZE()));
 		int mY = Math.round((Mouse.getY() / BLOCK_SIZE()));
		if(Mouse.leftMouseButtonDown){
			blockList[mX][mY] = new BlockStone(mX,mY);
		}
		if(Mouse.rightMouseButtonDown){
			blockList[mX][mY] = new BlockAir(mX, mY);
 		}
 		
 		for(int sX = camXDivided; sX < camXDivided + camWidthDivided + 1; sX++){
 			for(int sY = camYDivided; sY < camYDivided + camHeightDivided + 2; sY++){
 				int x;
 				int y;
 				if(sX >= 1){
 					x = sX -1;
 				}else{
 					x = sX;
 				}
 				if(sY >= 1){
 					y = sY - 1;
 				}else{
 					y = sY;
 				}
 				if(x < WorldArrayWidth && y < WorldArrayHeight && x >= 0 && y >= 0){
 					blockList[x][y].onUpdate();
 				}
 			}
 		}
 	}
 }
