 package com.turbonips.troglodytes.entities;
 
 import org.newdawn.slick.Animation;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Image;
 import org.newdawn.slick.Input;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.SpriteSheet;
 import org.newdawn.slick.geom.Vector2f;
 import org.newdawn.slick.state.StateBasedGame;
 import org.newdawn.slick.tiled.TiledMap;
 
 public abstract class Layer extends Entity {
 	
 	protected TiledMap tiledMap;
 	protected Vector2f off;
 	protected final int PLAYER_SPEED = 8;
 	protected final int ATTRIBUTE_LAYER = 3;
 	private final int NONE_ATT = 0;
 	private final int WALL_ATT = 1;
 	private final int WARP_ATT = 2;
 	
 	// Ugly HACK for region attributes
 	private int groupID = 0;
 	private int objectID = 0;
 
 	protected Vector2f slidingMin;
 	protected Vector2f slidingMax;
 	protected Vector2f slidingPos;
 	protected Vector2f playerLoc;
 	protected final Vector2f playerSize;
 	protected final int TILE_SIZE;
 	
 	protected EnemyData enemyData;
 
 	// hack for player sprites and animation
 	protected SpriteSheet playerSheet;
 	protected Animation upAnim;
 	protected Animation downAnim;
 	protected Animation leftAnim;
 	protected Animation rightAnim;
 	protected Animation idleAnim;
 	protected Animation playerAnimation;
 	// hack for enemy sprites and animation
 	protected SpriteSheet mobSheet;
 	protected Animation mobLeftAnim;
 	protected Animation mobRightAnim;
 	protected Animation mobIdleAnim;
 	protected Animation mobAnimation;
 	
 	public Layer(TiledMap tiledMap) {
 		this.tiledMap = tiledMap;
 		
 		createSampleMonster();
		playerLoc = new Vector2f(0, 0);
		playerSize = new Vector2f(32, 32);
 		slidingPos = new Vector2f(0, 0);
 		slidingMin = new Vector2f(-100,-100);
 		slidingMax = new Vector2f(100, 100);
		off = new Vector2f(0,0);
 		TILE_SIZE=32;
 		
 		// hack for player sprites and animation
 		try
 		{
 			playerSheet = new SpriteSheet(new Image("resources/demoCharTest.png"), 32, 64);
 			Image[] upImgs = {playerSheet.getSubImage(0, 2), playerSheet.getSubImage(1, 2), playerSheet.getSubImage(2, 2), playerSheet.getSubImage(3, 2), playerSheet.getSubImage(4, 2), playerSheet.getSubImage(5, 2), playerSheet.getSubImage(6, 2)};
 			upAnim = new Animation(upImgs, 60);
 			Image[] leftImgs = {playerSheet.getSubImage(0, 1), playerSheet.getSubImage(1, 1), playerSheet.getSubImage(2, 1), playerSheet.getSubImage(3, 1), playerSheet.getSubImage(4, 1), playerSheet.getSubImage(5, 1), playerSheet.getSubImage(6, 1)};
 			leftAnim = new Animation(leftImgs, 60);
 			Image[] rightImgs = {playerSheet.getSubImage(0, 3), playerSheet.getSubImage(1, 3), playerSheet.getSubImage(2, 3), playerSheet.getSubImage(3, 3), playerSheet.getSubImage(4, 3), playerSheet.getSubImage(5, 3), playerSheet.getSubImage(6, 3)};
 			rightAnim = new Animation(rightImgs, 60);
 			Image[] downImgs = {playerSheet.getSubImage(0, 0), playerSheet.getSubImage(1, 0), playerSheet.getSubImage(2, 0), playerSheet.getSubImage(3, 0), playerSheet.getSubImage(4, 0), playerSheet.getSubImage(5, 0), playerSheet.getSubImage(6, 0)};
 			downAnim = new Animation(downImgs, 60);
 			Image[] idleImgs = {playerSheet.getSubImage(0, 0)};
 			idleAnim = new Animation(idleImgs, 300);
 			playerAnimation = idleAnim;
 		} catch (SlickException ex){
 			logger.fatal(ex);
 		}
 		// hack for mob sprite/anim
 		try
 		{
 			mobSheet = new SpriteSheet(new Image("resources/demoMobTest.png"), 64, 48);
 			Image[] leftImgs = {mobSheet.getSubImage(0, 1), mobSheet.getSubImage(1, 1), mobSheet.getSubImage(2, 1), mobSheet.getSubImage(3, 1), mobSheet.getSubImage(4, 1), mobSheet.getSubImage(5, 1)};
 			mobLeftAnim = new Animation(leftImgs, 60);
 			Image[] rightImgs = {mobSheet.getSubImage(0, 0), mobSheet.getSubImage(1, 0), mobSheet.getSubImage(2, 0), mobSheet.getSubImage(3, 0), mobSheet.getSubImage(4, 0), mobSheet.getSubImage(5, 0)};
 			mobRightAnim = new Animation(rightImgs, 60);
 			Image[] idleImg = {mobSheet.getSubImage(0, 0)};
 			mobIdleAnim = new Animation(idleImg, 300);
 			mobAnimation = mobIdleAnim;
 			
 		} catch (SlickException ex){
 			logger.fatal(ex);
 		}
 	}
 	
 	private void createSampleMonster() {
 		// TODO: This should probably be passed into Layer
 	    enemyData = new EnemyData();
 		enemyData.setX(tiledMap.getWidth()*32/2);
 		enemyData.setY(tiledMap.getHeight()*32/2);
 		enemyData.setSpeed(4);
 		try {
 			enemyData.setImage(new Image("resources/enemy.png"));
 		} catch (SlickException ex) {
 			logger.fatal(ex);
 		}
 	}
 	
 	
 	private int getAttribute(int tileX, int tileY) {
 		int attribute = NONE_ATT;
 		
 		if (tileX < 0  || tileX >= tiledMap.getWidth()) {
 			attribute = WALL_ATT;
 		} else if (tileY < 0  || tileY >= tiledMap.getHeight()) {
 			attribute = WALL_ATT;
 		} else {
 			int tileId = tiledMap.getTileId(tileX, tileY, ATTRIBUTE_LAYER);
			String attType = tiledMap.getTileProperty(tileId, "Type", "None").toLowerCase();
			if (attType.equals("wall")) {
 				attribute = WALL_ATT;
 			} else {
 				for (int groupID=0; groupID<tiledMap.getObjectGroupCount(); groupID++) {
 					for (int objectID=0; objectID<tiledMap.getObjectCount(groupID); objectID++) {
 						int x = tiledMap.getObjectX(groupID, objectID)/TILE_SIZE;
 						int y = tiledMap.getObjectY(groupID, objectID)/TILE_SIZE;
						attType = tiledMap.getObjectType(groupID, objectID).toLowerCase();
 						
 						if (x == tileX && y == tileY) {
 							if (attType.equals("warp")) {
 								attribute = WARP_ATT;
 								this.groupID = groupID;
 								this.objectID = objectID;
 							}
 						}
 					}
 				}
 			}
 		}
 		return attribute;
 	}
 	
 	private int getAttribute(int x1, int y1, int x2, int y2) {
 		int att1 = getAttribute(x1,y1);
 		int att2 = getAttribute(x2,y2);
 		if (att1 == NONE_ATT) return att2;
 		else return att1;
 	}
 	
 	private void moveVertical(Input input) {
 		if(input.isKeyDown(Input.KEY_DOWN)) {
 			
 			playerAnimation = downAnim;
 			
 			if (slidingPos.y >= slidingMax.y) {
 				off.y -= PLAYER_SPEED;
 			} else {
 				slidingPos.y += PLAYER_SPEED;
 			}
 			
 			playerLoc.y += PLAYER_SPEED;
 		} else if (input.isKeyDown(Input.KEY_UP)) {
 			
 			playerAnimation = upAnim;
 			
 			if (slidingPos.y <= slidingMin.y) {
 				off.y += PLAYER_SPEED;
 			} else {
 				slidingPos.y -= PLAYER_SPEED;
 			}
 			playerLoc.y -= PLAYER_SPEED;
 		}
 	}
 	
 	private void moveHorizontal(Input input) {
 		if(input.isKeyDown(Input.KEY_RIGHT)) {
 			
 			playerAnimation = rightAnim;
 			
 			if (slidingPos.x >= slidingMax.x) {
 				off.x -= PLAYER_SPEED;
 			} else {
 				slidingPos.x += PLAYER_SPEED;
 			}
 
 			playerLoc.x += PLAYER_SPEED;
 		} else if (input.isKeyDown(Input.KEY_LEFT)) {
 			
 			playerAnimation = leftAnim;
 			
 			if (slidingPos.x <= slidingMin.x) {
 				off.x += PLAYER_SPEED;
 			} else {
 				slidingPos.x -= PLAYER_SPEED;
 			}
 			
 			playerLoc.x -= PLAYER_SPEED;
 		}
 	}
 	
 	private void checkForNoMoveInput(Input input)
 	{
 		if(!input.isKeyDown(Input.KEY_RIGHT) && !input.isKeyDown(Input.KEY_LEFT) && !input.isKeyDown(Input.KEY_DOWN) && !input.isKeyDown(Input.KEY_UP))
 		{
 			playerAnimation = idleAnim;
 		}
 	}
 	
 	private void warpPlayer(String warpMap, int warpX, int warpY) throws SlickException {
 		this.tiledMap = new TiledMap("resources/" + warpMap);
 		this.playerLoc = new Vector2f(warpX*TILE_SIZE, warpY*TILE_SIZE);
 		this.slidingPos = new Vector2f(0, 0);
 		this.off = new Vector2f(warpX*-TILE_SIZE, warpY*-TILE_SIZE);
 		
 		// HACK to make the monster spawn on demo map only
 		if (warpMap.equals("demo.tmx")) {
 			createSampleMonster();
 		} else {
 			enemyData = null;
 		}
 	}
 	
 	@Override
 	public void update(GameContainer container, StateBasedGame game, int delta) {
 		int attVertical = NONE_ATT;
 		int attHorizontal = NONE_ATT;
 
 		Input input = container.getInput();
 		if(input.isKeyDown(Input.KEY_DOWN)) {
 			int leftBottomX, leftBottomY, rightBottomX, rightBottomY;
 			leftBottomX = (int)Math.floor((playerLoc.x)/TILE_SIZE);
 			leftBottomY = (int)Math.floor((playerLoc.y+playerSize.y-0.1f+PLAYER_SPEED)/TILE_SIZE);
 			rightBottomX = (int)Math.floor((playerLoc.x+playerSize.x-0.1f)/TILE_SIZE);
 			rightBottomY = (int)Math.floor((playerLoc.y+playerSize.y-0.1f+PLAYER_SPEED)/TILE_SIZE);
 			attVertical = getAttribute(leftBottomX,leftBottomY,rightBottomX,rightBottomY);
 			
 		} else if (input.isKeyDown(Input.KEY_UP)) {
 			int leftTopX, leftTopY, rightTopX, rightTopY;
 			leftTopX = (int)Math.floor((playerLoc.x)/TILE_SIZE);
 			leftTopY = (int)Math.floor((playerLoc.y-PLAYER_SPEED)/TILE_SIZE);
 			rightTopX = (int)Math.floor((playerLoc.x+playerSize.x-0.1f)/TILE_SIZE);
 			rightTopY = (int)Math.floor((playerLoc.y-PLAYER_SPEED)/TILE_SIZE);
 			attVertical = getAttribute(leftTopX,leftTopY,rightTopX,rightTopY);
 		}
 		
 	   if(input.isKeyDown(Input.KEY_RIGHT)) {
 			int topRightX, topRightY, bottomRightX, bottomRightY;
 			topRightX = (int)Math.floor((playerLoc.x+playerSize.x-0.1f+PLAYER_SPEED)/TILE_SIZE);
 			topRightY = (int)Math.floor((playerLoc.y)/TILE_SIZE);
 			bottomRightX = (int)Math.floor((playerLoc.x+playerSize.x-0.1f+PLAYER_SPEED)/TILE_SIZE);
 			bottomRightY = (int)Math.floor((playerLoc.y+playerSize.y-0.1f)/TILE_SIZE);
 			attHorizontal = getAttribute(topRightX,topRightY,bottomRightX,bottomRightY);
 			
 		} else if (input.isKeyDown(Input.KEY_LEFT)) {
 			int topLeftX, topLeftY, bottomLeftX, bottomLeftY;
 			topLeftX = (int)Math.floor((playerLoc.x-PLAYER_SPEED)/TILE_SIZE);
 			topLeftY = (int)Math.floor((playerLoc.y)/TILE_SIZE);
 			bottomLeftX = (int)Math.floor((playerLoc.x-PLAYER_SPEED)/TILE_SIZE);
 			bottomLeftY = (int)Math.floor((playerLoc.y+playerSize.y-0.1f)/TILE_SIZE);
 			attHorizontal = getAttribute(topLeftX,topLeftY,bottomLeftX,bottomLeftY);
 		}
 	   
 	   if (attHorizontal != WALL_ATT) {
 		   moveHorizontal(input);
 	   }
 	   if (attVertical != WALL_ATT) {
 		   moveVertical(input);
 	   }
 	   
 	   checkForNoMoveInput(input);
 	   
 	   int att = attVertical;
 	   if (attHorizontal != NONE_ATT) att = attHorizontal;
 	   
 	   switch (att) {
 		   case WARP_ATT:
 			   String warpMap = tiledMap.getObjectProperty(groupID, objectID, "Map", "none");
 			   int warpX = Integer.valueOf(tiledMap.getObjectProperty(groupID, objectID, "X", "0"));
 			   int warpY = Integer.valueOf(tiledMap.getObjectProperty(groupID, objectID, "Y", "0"));
 				try {
 					warpPlayer(warpMap, warpX, warpY);
 				} catch (SlickException e) {
 					logger.fatal(e);
 				}
 
 			   break;
 	   }
 	   
 	}
 }
