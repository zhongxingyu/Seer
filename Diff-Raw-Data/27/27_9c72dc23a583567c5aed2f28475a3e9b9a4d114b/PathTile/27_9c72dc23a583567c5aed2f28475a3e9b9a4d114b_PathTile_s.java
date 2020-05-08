 package com.teamBasics.CollegeTD;
 
 import android.graphics.Rect;
 
 import com.teamBasics.framework.Image;
 
 public class PathTile {
 
 	// Path tile positions
 	private int tileX, tileY;
 	public char type2;
 	
 	// Image used for Path tiles
 	public Image tileImage2;
 	
 	// Used for collision detection
 	private Rect r;
 
 	public PathTile(int x, int y, char typeChar) {
 		tileX = x * 20;
 		tileY = y * 20;
 
 		type2 = typeChar;
 
 		r = new Rect();
 		
 		// Center Tile
		if (type2 == 's' ||type2 == 'b' ||type2 == 'B' || type2 =='g' || type2 =='G' || type2 =='e' || type2 =='E' || type2 =='f' || type2 =='F') {
 			tileImage2 = Assets.stoneCenter;
 			
 		// Side Tiles
 		} else if (type2 == 'w') {
 			tileImage2 = Assets.dirtTop;
 		} else if (type2 == 'a') {
 			tileImage2 = Assets.dirtLeft;
 
 		} else if (type2 == 'd') {
 			tileImage2 = Assets.dirtRight;
 
 		} else if (type2 == 'x') {
 			tileImage2 = Assets.dirtBottom;
 			
 		// Corner Tiles
 		} else if (type2 == 'z') {
 			tileImage2 = Assets.dirtLBC;
 		} else if (type2 == 'q') {
 			tileImage2 = Assets.dirtLTC;
 		} else if (type2 == 'c') {
 			tileImage2 = Assets.dirtRBC;
 		} else if (type2 == 'e') {
 			tileImage2 = Assets.dirtRTC;
 		} else if (type2 == 'r') {
 			tileImage2 = Assets.dirtCenter;
 		} else {
 			type2 = 'v';
 		}
 
 	}
 
 	public void update() {
 		//tileX += speedX;
 		
 		// Set collision box
 		r.set(tileX, tileY, tileX + 20, tileY + 20);
 
 		//if (Rect.intersects(r, Robot.yellowRed) && type != 0) {
 		//	checkVerticalCollision(Robot.rect, Robot.rect2);
 		//	checkSideCollision(Robot.rect3, Robot.rect4, Robot.footleft,
 		//			Robot.footright);
 		//}
 
 	}
 
 	public int getTileX() {
 		return tileX;
 	}
 
 	public void setTileX(int tileX) {
 		this.tileX = tileX;
 	}
 
 	public int getTileY() {
 		return tileY;
 	}
 
 	public void setTileY(int tileY) {
 		this.tileY = tileY;
 	}
 
 	public Image getTileImage() {
 		return tileImage2;
 	}
 
 	public void setTileImage(Image tileImage2) {
 		this.tileImage2 = tileImage2;
 	}
 
 	public char getType2() {
 		return type2;
 	}
 
 	public void setType2(char type2) {
 		this.type2 = type2;
 	}
 
 }
