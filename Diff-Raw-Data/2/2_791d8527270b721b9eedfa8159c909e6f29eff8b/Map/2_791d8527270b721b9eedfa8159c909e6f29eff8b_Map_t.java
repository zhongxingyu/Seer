 package gdp.racetrack;
 
 import java.awt.Image;
 import java.awt.image.BufferedImage;
 import java.util.HashMap;
 import java.util.TreeMap;
 
 public class Map {
 
 	public Map(BufferedImage image) {
 		mapImage = image;
 		mapData = new PointType[image.getWidth()][image.getHeight()];
 		Point newStartPoints[] = new Point[0];
 		for(int x=0; x<image.getWidth(); x++){
 			for(int y=0; y<image.getHeight(); y++){
 				switch(image.getRGB(x,y)&0xFFFFFF){
 					case COLOR_TRACK:
 						mapData[x][y] = PointType.TRACK;
 						break;
 					case COLOR_START:
 						mapData[x][y] = PointType.START;
 						if(x%GRIDSIZE==0 && y%GRIDSIZE==0){
 							Point tmpStartPoints[] = new Point[newStartPoints.length+1];
 							for(int i=0; i<newStartPoints.length; i++)
 								tmpStartPoints[i] = newStartPoints[i];
							tmpStartPoints[newStartPoints.length] = new Point(x/GRIDSIZE, y/GRIDSIZE);
 							newStartPoints = tmpStartPoints;
 						}
 						break;
 					case COLOR_FINISH:
 						mapData[x][y] = PointType.FINISH;
 						break;
 					default:
 						mapData[x][y] = PointType.NONE;
 				}				
 			}
 		}
 		startPoints = newStartPoints;
 	}
 
 	/**
 	 * Checks whether the given Point is an port of the track or not.
 	 * <br>
 	 * This method will return true by all parts of the track
 	 * included START and FINISH.
 	 * 
 	 * @param point The point to check whether it is a part of the track
 	 * @return true if the point is part of the track, false otherwise
 	 */
 	public boolean isTrack(Point point) {
 		return mapData[point.getVec().x*GRIDSIZE][point.getVec().y*GRIDSIZE] != PointType.NONE;
 	}
 
 	/**
 	 * Gets the type of the given point.
 	 * @param point The point to check the type
 	 * @return The Type of the point
 	 */
 	public PointType getPointType(Point point) {
 		return mapData[point.getVec().x*GRIDSIZE][point.getVec().y*GRIDSIZE];
 	}
 
 	/**
 	 * Gets the size of the of the map.
 	 * @return The size of the map
 	 */
 	public Vec2D getSize() {
 		return new Vec2D(mapImage.getWidth(null)/GRIDSIZE, mapImage.getHeight(null)/GRIDSIZE);
 	}
 
 	/**
 	 * Gets an Image of map.
 	 * <br>
 	 * [Better description?]
 	 * 
 	 * @return The image of the map
 	 */
 	public Image getImage() {
 		return mapImage;
 	}
 	
 	public Point[] getStartPoints(){
 		return startPoints;
 	}
 	
 	public Turn getTurnResult(Turn turn) {
 		Point start = turn.getOldPosition();
 		Point end = turn.getNewPosition();
 		if(mapData[start.getX()*GRIDSIZE][start.getY()*GRIDSIZE] == PointType.NONE)
 		{
 			turn.setCollideEnv(true);
 			return turn;
 		}
 		
 		double dx = (end.getX() - start.getX())*GRIDSIZE;
 		double dy = (end.getY() - start.getY())*GRIDSIZE;
 		
 		int maxI = 1;
 	
 		while(dx*dx > 1 || dy*dy > 1){
 			dx /= 2.0;
 			dy /= 2.0;
 			maxI *= 2;
 		}
 		
 		int startX = start.getX()*GRIDSIZE;
 		int startY = start.getY()*GRIDSIZE;
 		
 		for(int i=0; i<=maxI; i++)
 		{
 			int x = (int)(startX + dx*i);
 			int y = (int)(startY + dy*i);
 			
 			switch(mapData[x][y]){
 			case FINISH:
 				turn.setCrossFinishLine(true);
 				return turn;
 			case NONE:
 				if(x%GRIDSIZE == 0 && y%GRIDSIZE == 0){
 					turn.setNewPosition(new Point(x/GRIDSIZE, y/GRIDSIZE));
 					turn.setCollideEnv(true);
 					return turn;
 				}
 				while(true){
 					int newX[] = new int[4];
 					int newY[] = new int[4];
 					newX[0] = newX[1] = x/GRIDSIZE;
 					newX[2] = newX[3] = x/GRIDSIZE + 1;
 					newY[0] = newY[2] = y/GRIDSIZE;
 					newY[1] = newY[3] = y/GRIDSIZE + 1;
 					HashMap<Integer, Double> dist = new HashMap<Integer, Double>();
 					double doubleX = (double)x/GRIDSIZE;
 					double doubleY = (double)y/GRIDSIZE;
 					for(int j=0; j<4; j++){
 						dist.put(j, (doubleX-newX[j])*(doubleX-newX[j]) + (doubleY-newY[j])*(doubleY-newY[j]));
 					}
 					TreeMap<Integer, Double> sortedDist = new TreeMap<Integer, Double>();
 					sortedDist.putAll(dist);
 					for(Integer j : sortedDist.keySet()){
 						if(mapData[newX[j]*GRIDSIZE][newY[j]*GRIDSIZE] != PointType.NONE && testTurn(start, newX[j], newY[j])){
 							turn.setNewPosition(new Point(newX[j], newY[j]));
 							turn.setCollideEnv(true);
 							return turn;
 						}
 					}
 					int oldX = x;
 					int oldY = y;
 					while(oldX == x && oldY == y && i>0){
 						i--;
 						x = (int)(startX + dx*i);
 						y = (int)(startY + dy*i);
 					}
 				}
 			}
 		}
 		return turn;
 	}
 
 	private boolean testTurn(Point start, int x, int y) {
 		double dx = (x - start.getX())*GRIDSIZE;
 		double dy = (y - start.getY())*GRIDSIZE;
 		
 		int maxI = 1;
 	
 		while(dx > 1 || dy > 1){
 			dx /= 2.0;
 			dy /= 2.0;
 			maxI *= 2;
 		}
 		
 		double startX = start.getX()*GRIDSIZE;
 		double startY = start.getY()*GRIDSIZE;
 		
 		
 		for(int i=0; i<=maxI; i++)
 		{
 			if(mapData[(int)(startX + dx*i)][(int)(startY + dy*i)] == PointType.NONE)
 				return false;
 		}
 		return true;
 	}
 
 
 	private final PointType mapData[][];
 	private final Point startPoints[];
  	private final Image mapImage;
  	public static final int COLOR_TRACK  = 0xFFFFFF;
  	public static final int COLOR_START  = 0xFF0000;
  	public static final int COLOR_FINISH = 0x00FF00;
 	public static final int COLOR_BACKGROUND = 0xDCDCDC;
 	
 	public static final int GRIDSIZE = 16;
 
 }
