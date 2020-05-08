 import java.awt.Rectangle;
 
 import org.newdawn.slick.tiled.TiledMap;
 
 
 public class PlayerMapCollisionChecker {
 
 	private Rectangle playerRec;
 	private TiledMap tiledMap;
 
 
 	private boolean[][] blocked;
 	
 	
 	public PlayerMapCollisionChecker(TiledMap tiledMap,StartingPoint startingPoint){
 		this.tiledMap=tiledMap;
 	
 			
 		playerRec = new Rectangle();
 		playerRec.setBounds(startingPoint.getX(), startingPoint.getY(), Constant.PLAYER_REC_WIDTH, Constant.PLAYER_REC_HEIGHT);
 		
 
 		fillBoolMap();
 	}
 	
 	
 	private void fillBoolMap() {
 		blocked = new boolean[tiledMap.getWidth()][tiledMap.getHeight()];
 
 		for (int xAxis = 0; xAxis < tiledMap.getWidth(); xAxis++) {
 			for (int yAxis = 0; yAxis < tiledMap.getHeight(); yAxis++) {
 
 				int tileID = tiledMap.getTileId(xAxis, yAxis, 0);
 				String value = tiledMap.getTileProperty(tileID, "blocked",
 						"false");
 				
 
 				if ("true".equals(value)) {
 					blocked[xAxis][yAxis] = true;
 				}
 
 			}
 		
 		}
 
 	}
 	

 	public boolean isNotBlockedInAnyDirection(Point point, float deltaX, float deltaY){
 		if(!isBlockedRight(point, deltaX) && !isBlockedLeft(point, deltaX) && !isBlockedDown(point, deltaY) && !isBlockedUp(point, deltaY))
 			return true;
 		else
 			return false;
 	}
 	
 	public boolean isBlockedRight(Point point, float deltaX) {
 		if (checkTopRightCorner(point,deltaX) || checkBottomRightCorner(point, deltaX)
 				|| checkMidpointOneRight(point, deltaX) || checkMidpointTwoRight(point, deltaX)
 				|| checkMidpointThreeRight(point,deltaX))
 			return true;
 		else
 			return false;
 
 	}
 
 	public boolean isBlockedLeft(Point point, float deltaX) {
 		if (checkTopLeftCorner(point, deltaX) || checkBottomLefttCorner(point, deltaX)
 				|| checkMidpointOneLeft(point, deltaX) || checkMidpointTwoLeft(point, deltaX)
 				|| checkMidpointThreeLeft(point, deltaX))
 			return true;
 		else
 			return false;
 
 	}
 
 	public boolean isBlockedDown(Point point, float deltaY) {
 		if (checkMidpointOneBot(point, deltaY) || checkMidpointTwoBot(point, deltaY)
 				|| checkMidpointThreeBot(point, deltaY))
 			return true;
 		else
 			return false;
 
 	}
 
 	public boolean isBlockedUp(Point point, float deltaY) {
 		if (checkMidpointOneTop(point, deltaY) || checkMidpointTwoTop(point, deltaY)
 				|| checkMidpointThreeTop(point, deltaY))
 			return true;
 		else
 			return false;
 
 	}
 	
 	private boolean checkMidpointThreeTop(Point point, float deltaY) {
 		int x=point.getX();
 		int y=point.getY();
		int xBlock = (int) (x += (24)) / Constant.BLOCK_SIZE;
 		int yBlock = (int) (y -= deltaY) / Constant.BLOCK_SIZE;
 		return blocked[xBlock][yBlock];
 	}
 
 	private boolean checkMidpointTwoTop(Point point, float deltaY) {
 		int x=point.getX();
 		int y=point.getY();
		int xBlock = (int) (x += (16)) / Constant.BLOCK_SIZE;
 		int yBlock = (int) (y -= deltaY) /Constant.BLOCK_SIZE;
 		return blocked[xBlock][yBlock];
 	}
 
 	private boolean checkMidpointOneTop(Point point, float deltaY) {
 		int x=point.getX();
 		int y=point.getY();
		int xBlock = (int) (x += (8)) / Constant.BLOCK_SIZE;
 		int yBlock = (int) (y -= deltaY) / Constant.BLOCK_SIZE;
 		return blocked[xBlock][yBlock];
 	}
 
 	private boolean checkMidpointTwoRight(Point point, float deltaX) {
 		int x=point.getX();
 		int y=point.getY();
 		int xBlock = (int) (x += deltaX
 				+ playerRec.getWidth())
 				/ Constant.BLOCK_SIZE;
 		int yBlock = (int) (y += 36) / Constant.BLOCK_SIZE;
 
 		return (blocked[xBlock][yBlock]);
 	}
 
 	private boolean checkMidpointThreeRight(Point point, float deltaX) {
 		int x=point.getX();
 		int y=point.getY();
 		int xBlock = (int) (x += deltaX
 				+ playerRec.getWidth())
 				/ Constant.BLOCK_SIZE;
 		int yBlock = (int) (y += 24) / Constant.BLOCK_SIZE;
 
 		return (blocked[xBlock][yBlock]);
 	}
 
 	private boolean checkMidpointOneRight(Point point, float deltaX) {
 		int x=point.getX();
 		int y=point.getY();
 		int xBlock = (int) (x += deltaX
 				+ playerRec.getWidth())
 				/ Constant.BLOCK_SIZE;
 		int yBlock = (int) (y += 12) / Constant.BLOCK_SIZE;
 
 		return (blocked[xBlock][yBlock]);
 	}
 
 	private boolean checkBottomRightCorner(Point point, float deltaX) {
 		int x=point.getX();
 		int y=point.getY();
 		int xBlock = (int) (x += deltaX
 				+ playerRec.getWidth())
 				/ Constant.BLOCK_SIZE;
 		int yBlock = (int) (y += playerRec.getHeight()) / Constant.BLOCK_SIZE;
 
 		return (blocked[xBlock][yBlock]);
 	}
 
 	private boolean checkTopRightCorner(Point point, float deltaX) {
 		int x=point.getX();
 		int y=point.getY();
 		int xBlock = (int) (x += deltaX+ playerRec.getWidth())/ Constant.BLOCK_SIZE;
 		int yBlock = (int) y / Constant.BLOCK_SIZE;
 
 		return (blocked[xBlock][yBlock]);
 	}
 
 	private boolean checkMidpointThreeLeft(Point point, float deltaX) {
 		int x=point.getX();
 		int y=point.getY();
 		int xBlock = (int) (x -= deltaX) / Constant.BLOCK_SIZE;
 		int yBlock = (int) (y += 36) / Constant.BLOCK_SIZE;
 		return blocked[xBlock][yBlock];
 	}
 
 	private boolean checkMidpointTwoLeft(Point point, float deltaX) {
 		int x=point.getX();
 		int y=point.getY();
 		int xBlock = (int) (x -= deltaX) / Constant.BLOCK_SIZE;
 		int yBlock = (int) (y += 24) / Constant.BLOCK_SIZE;
 		return blocked[xBlock][yBlock];
 	}
 
 	private boolean checkMidpointOneLeft(Point point, float deltaX) {
 		int x=point.getX();
 		int y=point.getY();
 		int xBlock = (int) (x -= deltaX) / Constant.BLOCK_SIZE;
 		int yBlock = (int) (y += 12) / Constant.BLOCK_SIZE;
 		return blocked[xBlock][yBlock];
 	}
 
 	private boolean checkBottomLefttCorner(Point point, float deltaX) {
 		int x=point.getX();
 		int y=point.getY();
 		int xBlock = (int) (x -= deltaX) / Constant.BLOCK_SIZE;
 		int yBlock = (int) (y += playerRec.getHeight()) / Constant.BLOCK_SIZE;
 		return blocked[xBlock][yBlock];
 	}
 
 	private boolean checkTopLeftCorner(Point point, float deltaX) {
 		int x=point.getX();
 		int y=point.getY();
 		int xBlock = (int) (x -= deltaX) / Constant.BLOCK_SIZE;
 		int yBlock = (int) y / Constant.BLOCK_SIZE;
 		return blocked[xBlock][yBlock];
 	}
 
 	private boolean checkMidpointThreeBot(Point point, float deltaY) {
 		int x=point.getX();
 		int y=point.getY();
 		int xBlock = (int) (x += 24) / Constant.BLOCK_SIZE;
 		int yBlock = (int) (y += deltaY
 				+ playerRec.getHeight())
 				/ Constant.BLOCK_SIZE;
 		return blocked[xBlock][yBlock];
 	}
 
 	private boolean checkMidpointTwoBot(Point point, float deltaY) {
 		int x=point.getX();
 		int y=point.getY();
 		int xBlock = (int) (x += 16) / Constant.BLOCK_SIZE;
 		int yBlock = (int) (y += deltaY
 				+ playerRec.getHeight())
 				/ Constant.BLOCK_SIZE;
 		return blocked[xBlock][yBlock];
 	}
 
 	private boolean checkMidpointOneBot(Point point, float deltaY) {
 		int x=point.getX();
 		int y=point.getY();
 		int xBlock = (int) (x += 8) / Constant.BLOCK_SIZE;
 		int yBlock = (int) (y += deltaY
 				+ playerRec.getHeight())
 				/ Constant.BLOCK_SIZE;
 		return blocked[xBlock][yBlock];
 	}
 	
 }
