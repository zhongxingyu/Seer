 package manager;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.util.Scanner;
 
 import window.AppliWindow;
 import exceptions.MapFileException;
 import game.Game;
 import game.base.Base;
 
 public class MapManager {
 	
 	private int widthMap, heightMap;
 	private int[][] map; 
 	
 	
 	// ------------------ accessors ---------------------
 	
 	public int getWidthMap() {
 		return widthMap;
 	}
 
 	public void setWidthMap(int widthMap) {
 		this.widthMap = widthMap;
 	}
 
 	public int getHeightMap() {
 		return heightMap;
 	}
 	
 	public void setHeightMap(int heightMap) {
 		this.heightMap = heightMap;
 	}
 
 	public int[][] getMap() {
 		return map;
 	}
 
 	public void setMap(int[][] map) {
 		this.map = map;
 	}
 	
 	/**
 	 * @return the value of the map at the index corresponding to the position (x,y) in the window
 	 */
 	public int getNumAreaAtPosition(int x, int y) {
		
		if (y > 660) {
			return -1;
		}
		
 		int widthWindow = AppliWindow.getInstance().getWidth();
 		int heightWindow = AppliWindow.getInstance().getHeight();
 		
 		int xMap = x * this.widthMap / widthWindow;
 		int yMap = y * this.heightMap / heightWindow ;
 		
		if(xMap<0 || xMap >= this.widthMap || yMap < 0 || yMap >= this.heightMap) {
			return -1;
		}
		
 		return this.map[xMap][yMap];
 	}
 
 	// ------------------ constructor ---------------------
 	
 	public MapManager(int widthMap, int heightMap, int[][] map) {
 		super();
 		this.widthMap = widthMap;
 		this.heightMap = heightMap;
 		this.map = map;
 	}
 	
 	public MapManager(int widthMap, int heightMap) {
 		this(widthMap, heightMap, new int[widthMap][heightMap]);
 	}
 	
 	public MapManager() {
 		this(0, 0, null);
 	}
 	
 	// ------------------ other ---------------------
 	
 	@Override
 	public String toString() {
 		StringBuilder sb = new StringBuilder("map(");
 		sb.append(widthMap);
 		sb.append(", ");
 		sb.append(heightMap);
 		sb.append(")\n");
 		for(int j=0; j<this.heightMap; ++j) {
 			for(int i=0; i<this.widthMap; ++i) {
 				if(this.map[i][j]==-1) {
 					sb.append(" ");
 				}
 				else {
 					sb.append("  ");
 				}
 				sb.append(this.map[i][j]);
 			}
 			sb.append("\n");
 		}
 		return sb.toString();
 	}
 	
 	/**
 	 * Sets the informations of the map, from a file.
 	 * The file must contain integers : width, height of the map and then all the integers for the map :
 	 * -1 => lowland
 	 * 0 => constructing area of the base which index is 0
 	 * 1 => constructing area of the base which index is 1
 	 * 2 => constructing area of the base which index is 2
 	 * etc...
 	 * @param fileName
 	 * @throws FileNotFoundException
 	 * @throws MapFileException 
 	 */
 	@SuppressWarnings("resource")
 	public void setMapFromFile(String fileName) throws FileNotFoundException, MapFileException {
 		
 		Scanner scanner = new Scanner(new File("files/"+fileName));
 		
 		// width
 		if (scanner.hasNextInt()) {
 			int width = scanner.nextInt();
 			if (width >= 0) {
 				this.widthMap = width;
 			}
 			else {
 				throw new MapFileException("the given width is negative !");
 			}
 		}
 		else {
 			throw new MapFileException("the file doesn't contain any integer !");
 		}
 		
 		//height
 		if (scanner.hasNextInt()) {
 			int height = scanner.nextInt();
 			if (height >= 0) {
 				this.heightMap = height;
 			}
 			else {
 				throw new MapFileException("the given height is negative !");
 			}
 		}
 		else {
 			throw new MapFileException("the file doesn't contain enough integers !");
 		}
 		
 		// all int from map
 		this.map = new int[this.widthMap][this.heightMap];
 		
 		for(int j=0; j<this.heightMap; ++j) {
 			for(int i=0; i<this.widthMap; ++i) {
 			
 				if (scanner.hasNextInt()) {
 					this.map[i][j] = scanner.nextInt();
 				}
 				else {
 					throw new MapFileException("the file doesn't contain enough integers !");
 				}
 			}
 		}
 	}
 	
 	/**
 	 * (Re)calculates all the building areas of the hills = (re)calculates the map.
 	 * The map (int[][]) will be filled with the index of the nearest base.
 	 * This function must be called each time a base change of player.
 	 * @param widthWindow
 	 * @param heightWindow
 	 */
 	public void calculateAreas(int widthWindow, int heightWindow) {
 		
 		// for each cell of the map
 		for(int j=0; j<this.heightMap; ++j) {
 			for(int i=0; i<this.widthMap; ++i) {
 			
 				
 				// we calculate only the hill cells (i.e. the cells that are not = -1)
 				if(this.map[i][j] != -1) {
 					// we look for the nearest base : at the beginning we don't know which one
 					int nearestBase = -1;
 					double smallerDistance = Double.MAX_VALUE;
 					
 					// we loop over all bases because we need to compare the distance separating all the bases
 					int currentNumBase = -1;
 					for(Base b:Game.getInstance().getBaseManager().getBases()) {
 						
 						// the influential bases are only the non-neutral
 						if(b.getPlayer() != null) {
 							++currentNumBase;
 							
 							// coordinates of the cell of the map in the window
 							float xa = i*((float)widthWindow/this.widthMap);
 							float ya = j*((float)heightWindow/this.heightMap);	
 							// coordinates of the base
 							float xb = b.getPosition().x;
 							float yb = b.getPosition().y;
 							// distance between the base and the cell
 							double distance = Math.sqrt((xb-xa)*(xb-xa) + (yb-ya)*(yb-ya));
 
 							// if this base are nearest than the old one, we keep it in memory
 							if(distance < smallerDistance) {
 								nearestBase = currentNumBase;
 								smallerDistance = distance;
 							}
 						}
 					}
 					
 					// if we've found the nearest base, we can fill the cell !
 					if(nearestBase != -1) {
 						this.map[i][j] = nearestBase;
 					}
 				}
 			}
 		}
 	}
 	
 }
