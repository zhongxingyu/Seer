 package saneMap;
 
 import java.util.Random;
 
 public class MapGeneration {
 
 	public static void generateEntities(MapArea theMap){
 
 	}
 
 	public static void generateTerrain(MapArea theMap){
 
 
 		Random rng = theMap.getRandom();
 
 		int areaOfMap = theMap.getXSize() * theMap.getYSize();
 
 		int minAmtOfPoints = (int)Math.round(Math.pow(areaOfMap , 0.25));
 		int maxAmtOfPoints = minAmtOfPoints + 4;
 
 		int mapAmtOfPoints = rng.nextInt(maxAmtOfPoints - minAmtOfPoints) + minAmtOfPoints;
 
 		int[] xCoords = new int[mapAmtOfPoints];
 		int[] yCoords = new int[mapAmtOfPoints];
 
 		for (int i = 0; i < mapAmtOfPoints; i ++){
 			xCoords[i] = rng.nextInt(theMap.getXSize());
 			yCoords[i] = rng.nextInt(theMap.getYSize());
			System.out.println(xCoords[i] + " , " + yCoords[i]);
 		}

		System.out.println(theMap);
		System.out.println(theMap.getXSize() + " x " + theMap.getYSize());
		System.out.println(theMap.getTile(0, 0));
 		
 		for (int i = 0; i < mapAmtOfPoints; i ++){
 			for (int j = 0; j < mapAmtOfPoints; j ++){
 				makePath(theMap, xCoords[i], yCoords[i], xCoords[j], yCoords[j]);
 			}
 		}
 

 	}
 
 	private static void makePath(MapArea theMap, int x1, int y1, int x2, int y2){
 
		System.out.println("Parameters: " + x1 + " " + y1 + " " + x2 + " " + y2);
		
		System.out.println(theMap);
		System.out.println(theMap.getXSize() + " x " + theMap.getYSize());
		System.out.println(theMap.getTile(0, 0));
		
 		theMap.getTile(x1, y1).setTileType(TileType.FLOOR);
 		theMap.getTile(x2, y2).setTileType(TileType.FLOOR);
 
 
 
 		int verticalShift = 1;
 		int horizontalShift = 1;
 
 		int currentX = x1;
 		int currentY = y1;
 
 		if (x1 > x2){
 			horizontalShift = -1;
 		}
 		if (y1 > y2){
 			verticalShift = -1;
 		}
 
 		double randomPercentage;
 
 
 		//Builds a path of FLOOR to the end point by moving tile by tile in the direction of the end point.
 		//There is a chance of moving away from the end point, to create non-linear paths.
 		while (currentX != x2 && currentY != y2){
 			if (currentX == x2){
 				currentY += verticalShift;
 			}
 			if (currentY == y2){
 				currentX += horizontalShift;
 			}
 			if (!(currentX == x2 && currentY == y2)) {
 				randomPercentage = theMap.getRandom().nextDouble();
 				if (randomPercentage < 0.5){
 					if (randomPercentage < 0.125 && currentX >= 0 && currentX < theMap.getXSize()){
 						currentX -= horizontalShift;
 					} else {
 						currentX += horizontalShift;
 					}
 				} else {
 					if (randomPercentage < 0.625 && currentY >= 0 && currentY < theMap.getYSize()){
 						currentY -= verticalShift;
 					} else {
 						currentY += verticalShift;
 					}
 
 				}
 			} else {
 				break;
 			}
 			
 			theMap.getTile(currentX, currentY).setTileType(TileType.FLOOR);
 			
 		}
 	}
 
 
 
 }
