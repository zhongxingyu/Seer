 package org.tendiwa.modules;
 
 import org.tendiwa.core.*;
 import org.tendiwa.drawing.DrawingAlgorithm;
 
 import java.awt.*;
 
 public class DrawingWorld {
 
 public static DrawingAlgorithm<World> defaultAlgorithm() {
 	return DrawingWorld.level(0);
 }
 
 public static DrawingAlgorithm<World> level(final int level) {
 	return new DrawingAlgorithm<World>() {
 		@Override
 		public void draw(World world) {
 			HorizontalPlane defaultPlane = world.getPlane(level);
 			int width = world.getWidth();
 			int height = world.getHeight();
 			if (width > canvas.width || height > canvas.height) {
 				throw new RuntimeException("Size of world (" + width + "x" + height + ") is greater than size of canvas (" + width + "x" + height + ")");
 			}
			drawRectangle(new org.tendiwa.geometry.Rectangle(0, 0, width, height), Color.BLACK);
 			for (int y = 0; y < height; y++) {
 				for (int x = 0; x < width; x++) {
 					FloorType floorType = defaultPlane.getFloor(x, y);
 					GameObject wallType = defaultPlane.getGameObject(x, y);
 					if (defaultPlane.getCharacter(x, y) != null) {
 						drawPoint(x, y, Color.YELLOW);
 					} else if (defaultPlane.hasAnyItems(x, y)) {
 						drawPoint(x, y, Color.ORANGE);
 					} else if (defaultPlane.hasObject(x, y)) {
 						drawPoint(x, y, Color.PINK);
 					} else if (wallType instanceof WallType) {
 						// Draw floor
 						if (floorType == null) {
 							drawPoint(x, y, Color.LIGHT_GRAY);
 						} else if (floorType.isLiquid()) {
 							drawPoint(x, y, new Color(50, 50, 180));
 						} else {
 							drawPoint(x, y, Color.GREEN);
 						}
 					} else {
 						// Draw wall
 						drawPoint(x, y, Color.GRAY);
 					}
 				}
 			}
 		}
 	};
 }
 }
