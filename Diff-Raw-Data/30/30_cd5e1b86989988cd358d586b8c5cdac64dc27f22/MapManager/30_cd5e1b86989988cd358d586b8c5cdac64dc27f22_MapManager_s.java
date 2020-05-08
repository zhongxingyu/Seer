 package GameEngine;
 
 import java.awt.Point;
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.IOException;
 import java.util.LinkedList;
 import java.util.Vector;
 
 import javax.imageio.ImageIO;
 
 import GameEngine.Map;
 
 /**
  * Project - TowerDefense</br>
  * <b>Class - MapManager</b></br>
  * <p>The MapManager is the responsible for the three maps the game need and that are calculated
  * after the original image :
  * <ul><li> the HeightMap : which separates plains and hills</li>
  * <li>the TerritoryMap : which generated the allowed areas of each players following the Bases
  * they possess</li>
  * <li> the ProximityMap : which calculate the distance of a Base and all the other Bases in
  * game</li></ul>
  * The MapManager create these maps as Map before the game starts and has the 
  * responsibility to update them when it's necessary.</br>
  * <b>Creation :</b> 22/04/2013</br>
  * @author K. Akyurek, A. Beauprez, T. Demenat, C. Lejeune - <b>IMAC</b></br>
  * @see Base
  * @see Player
  */
 
 public class MapManager {
   /**
    * Store the number maximum of Player 
    */
  private static final int maxnumberOfPlayer = 4;
   
   /**
    * Store the number of players playing
    */
  private static final int numberOfPlayer = 3;
   
   /**
    * Store the path of the image we'll use to create our maps
    */
   private String imagepath;
   
   /**
    * Store the map that describes the plains and hills of the map
    */
   private Map heightMap;
   
   /**
    * Store the map that determine player's territory with their possessions
    */
   private Map territoryMap;
   
   /**
    * Store the position of each player's base
    */
   private Point[] playerBasePosition;
   
   /**
    * Store the map that calculate the distance to each player's Base
    */
   private Map playerProximityMap[];
   
   /**
    * Store the position of each neutral bases
    */
   private LinkedList<Point> neutralBasePosition;
   
   /**
    * Store the map that calculate the distance to each neutral Base
    */
   private LinkedList<Map> neutralProximityMap;
   
   /**
 	 * MapManager's constructor, initiate and create the heightMap and the territoryMap
 	 * @param i_imagepath path of the local image to analyse
 	 */
	MapManager(String i_imagepath){
 		super();
 		imagepath = i_imagepath;
 		playerBasePosition = new Point[numberOfPlayer];
 		playerProximityMap = new Map[numberOfPlayer];
 		neutralBasePosition = new LinkedList<Point>();
 		neutralProximityMap = new LinkedList<Map>();
 		generateHeightMap();
 		generateRelief();
 		generateTerritoryMap();
 		generateAllProximityMap();
 	}
 	
 	/**
 	 * Generate the HeightMap using a local picture and save it as a png at /map
 	 * @see MapManager()
 	 */
 	private void generateHeightMap(){
 		BufferedImage img = null;//Local image containment
 		
 		try {
 			img = ImageIO.read(new File(imagepath)); //We get the image
 		} catch (IOException e) {
 			System.out.println("Erreur lors de la lecture de l'image de Map");
 			e.printStackTrace();
 		}
 		
 		//Initiate the HeightMap
 		heightMap = new Map(img.getWidth(),img.getHeight());
 		
 		for (int y = 0; y < heightMap.getHeight();y++){ 
 			for (int x = 0; x < heightMap.getWidth();x++){
 				
 				// get the color data of each pixel
 				int color = img.getRaster().getSample(x,y,0); 
 				
 				//If pixel data is between 0 and 5 (black) or between 250 and 255 (white) => it's height information	
 				if ((color>=0 && color<=5)||(color >=250 && color <= 255)){ 
 					//Divide the value in order to get more lisible information
 					color = color/(255/(maxnumberOfPlayer+1)); 
 					if (color==0 || color==5)
 						heightMap.setPixel(x,y, color);
 					else
 						heightMap.setPixel(x,y, 5);
 				}
 				//Else if the pixel color is grey => it refers to bases' position
 				else {
 					color = (int)Math.round((float)color/(255/(maxnumberOfPlayer+2)));
 					if (color <= numberOfPlayer)
 					{
 						//Store the position of the base according to its player
 						int player = color - 1;
 						playerBasePosition[player]=new Point(x,y);
 					}
 					else{
 						neutralBasePosition.add(new Point(x,y));
 					}
 					heightMap.setPixel(x,y, 5);
 				}
 			}
 		}	
 		heightMap.saveAsPNG("hm.png");
 	}
 
 	/**
 	 * Generate relief for the HeightMap and save the image at img/map/hrm.png
 	 * @see MapManager()
 	 */
 	private void generateRelief(){
 		//Intensity of the relief
 		int reliefDown = 10;
 		int reliefLeft = 4;
 		
 		for (int y = 0; y < heightMap.getHeight();y++){ 
 			for (int x = 0; x < heightMap.getWidth();x++){
 				if (heightMap.getPixel(x,y)==0){
 					
 					if(y<heightMap.getHeight()-1){
 						if (heightMap.getPixel(x,y+1)==5)
 						{
 							for (int i=1;i<reliefDown;i++){
 								if(y+i<heightMap.getHeight())
 									heightMap.setPixel(x,y+i,6);
 							}
 						}
 					}
 					if (x>0){
 						if (heightMap.getPixel(x-1,y)==5)
 						{
 							for (int i=1;i<reliefLeft;i++)
 							{
 								if((x-i)>=0)
 									heightMap.setPixel(x-i,y,6);
 							}
 						}
 					}
 				}
 			}
 		}
 		heightMap.saveAsPNG("hrm.png");
 	}
 	/**
 	 * Generate the TerritoryMap and save it as a png at /map
 	 * @see MapManager() 
 	 */
 	private void generateTerritoryMap(){
 		//Initiate TerritoryMap
 		territoryMap = new Map(heightMap.getWidth(),heightMap.getHeight());
 		
 		//Vectors containing pixels that have to be modified
 		Vector<Point> pixelsJ4 = new Vector<Point>();
 		Vector<Point> pixelsJ3 = new Vector<Point>();
 		Vector<Point> pixelsJ2 = new Vector<Point>();
 		Vector<Point> pixelsJ1 = new Vector<Point>();
 		
 		boolean loopJ1 = false,loopJ2=false, loopJ3=false,loopJ4=false;
 		 
 		switch (numberOfPlayer){
 		case 4:
 			pixelsJ4.add(playerBasePosition[3]);// The first pixel is the player's base
 			loopJ4 = true;
 		case 3:
 			pixelsJ3.add(playerBasePosition[2]);
 			loopJ3=true;
 		case 2:
 			pixelsJ2.add(playerBasePosition[1]);
 			pixelsJ1.add(playerBasePosition[0]);
 			loopJ2=true;
 			loopJ1=true;
 			break;
 		default:
 			break;
 		}
 		
 		//Loop stopping only if not a single pixel of the TerritoryMap has been modified
 		while (loopJ1||loopJ2||loopJ3||loopJ4){
 			//Expand each player territory successively
 			switch (numberOfPlayer){
 			case 4:
 				loopJ4 = circlePropagation(pixelsJ4,4);
 			case 3:
 				loopJ3 = circlePropagation(pixelsJ3,3);
 			case 2:
 				loopJ2 = circlePropagation(pixelsJ2,2);
 				loopJ1 = circlePropagation(pixelsJ1,1);
 				break;
 			default:
 				break;
 			}
 
 			
 		}
 		territoryMap.saveAsPNG("tm.png");
 	}
 	
 	/**
 	 * Expand the player territory according to the pixels input parameter and store the future pixels
 	 * suivants 
 	 * @param pixels Store pixels that have to be modified
 	 * @param player Value to set at those pixels according to the player
 	 * @return true if one or more pixels have been modified, false if not
 	 * @see MapManager.generateTerritoryMap() 
 	 */
 	private boolean circlePropagation(Vector<Point> pixels,int player){
 		Vector<Point> nextPixels = new Vector<Point>();
 		boolean modified=false;
 		
 		//Getting each pixels
 		for (Point i:pixels){
 			//A non-modified pixel value is -1
 			if(territoryMap.getPixel(i.x,i.y)==-1){
 				//Add the value to the TerritoryMap according to the heightMap
 				if (heightMap.getPixel(i.x,i.y)==0)
 					territoryMap.setPixel(i.y*heightMap.getWidth() + i.x, player);
 				else
 					territoryMap.setPixel(i.y*heightMap.getWidth() + i.x, 0);
 				
 				//Then we store the 8 pixels around the modified pixel
 				
 				//Right pixel
 				if (i.x<territoryMap.getWidth()-1)
 					nextPixels.add(new Point(i.x+1,i.y));
 				
 				//Bottom pixel
 				if (i.y<territoryMap.getHeight()-1)
 					nextPixels.add(new Point(i.x,i.y+1));
 				
 				//Left pixel
 				if (i.y>0)
 					nextPixels.add(new Point(i.x,i.y-1));
 				
 				//Top pixel
 				if (i.x>0)
 					nextPixels.add(new Point(i.x-1,i.y));
 				
 				//Bottom-right pixel
 				if ((i.x<territoryMap.getWidth()-1)&&(i.y<territoryMap.getHeight()-1)){
 					nextPixels.add(new Point(i.x+1,i.y+1));
 				}
 				
 				//Bottom-left pixel
 				if (i.x>0&&(i.y<territoryMap.getHeight()-1)){
 					nextPixels.add(new Point(i.x-1,i.y+1));
 				}
 				
 				//Top-left pixel
 				if (i.x>0&&i.y>0){
 					nextPixels.add(new Point(i.x-1,i.y-1));
 				}
 				
 				//Top-right pixel
 				if ((i.x<territoryMap.getWidth()-1)&&i.y>0){
 					nextPixels.add(new Point(i.x+1,i.y-1));
 				}
 				
 				
 				//Warn that a pixel has been modified : another loop has to be done
 				modified=true;
 			}
 		}
 		pixels.clear();
 		
 		if (modified)
 		{
 			//Get the future pixels
 			pixels.addAll(nextPixels);
 			nextPixels.clear();
 		}
 		return modified;
 	}
 	
 	/**
 	 * Generate All the ProximityMap (one for each base)
 	 * @see MapManager()
 	 */
 	private void generateAllProximityMap(){
 		//Generate the proximityMaps of the players' bases
 		for (int i=0;i<numberOfPlayer;i++){
 			Map proximityMap = new Map(heightMap.getWidth(),heightMap.getHeight());
 			generateProximityMap(proximityMap,playerBasePosition[i]);
 			playerProximityMap[i] = proximityMap;
 			playerProximityMap[i].saveAsPNGProximity("pm"+i+".png");
 		}
 		
 		//Generate the neutral bases' proximityMaps
 		int cpt = 0;
 		while(!neutralBasePosition.isEmpty()){
 			Map proximityMap = new Map(heightMap.getWidth(),heightMap.getHeight());
 			generateProximityMap(proximityMap,neutralBasePosition.poll());
 			neutralProximityMap.add(proximityMap);
 			neutralProximityMap.getLast().saveAsPNGProximity("npm"+cpt+".png");
 			cpt++;
 		}
 	}
 	
 	/**
 	 * Generate a ProximityMap for a base
 	 * @param player id of the base
 	 * @see MapManager().generateAllProximityMap()
 	 */
 	private void generateProximityMap(Map proximityMap, Point base){
 		
 		LinkedList<Point> nextPixels = new LinkedList<Point>();
 		nextPixels.add(base);
 		int value=0;
 		Point p;
 		while(!nextPixels.isEmpty()){
 			p = nextPixels.poll();
 			if (proximityMap.getPixel(p.x,p.y)==-1){
 				if (heightMap.getPixel(p.x,p.y)!=0){
 					proximityMap.setPixel(p.x,p.y,value);
 				}
 				else
 					proximityMap.setPixel(p.x,p.y,9999);
 				
 				//Right pixel
 				if (p.x<proximityMap.getWidth()-1)
 					nextPixels.add(new Point(p.x+1,p.y));
 				
 				
 				//Bottom pixel
 				if (p.y<proximityMap.getHeight()-1)
 					nextPixels.add(new Point(p.x,p.y+1));
 					
 				//Left pixel
 				if (p.y>0)
 					nextPixels.add(new Point(p.x,p.y-1));
 
 				
 				//Top pixel
 				if (p.x>0)
 					nextPixels.add(new Point(p.x-1,p.y));
 				
 				//Bottom-right pixel
 				if ((p.x<proximityMap.getWidth()-1)&&(p.y<proximityMap.getHeight()-1)){
 					nextPixels.add(new Point(p.x+1,p.y+1));
 				}
 				
 				//Bottom-left pixel
 				if (p.x>0&&(p.y<proximityMap.getHeight()-1)){
 					nextPixels.add(new Point(p.x-1,p.y+1));
 				}
 				
 				//Top-left pixel
 				if (p.x>0&&p.y>0){
 					nextPixels.add(new Point(p.x-1,p.y-1));
 				}
 				
 				//Top-right pixel
 				if ((p.x<proximityMap.getWidth()-1)&&p.y>0){
 					nextPixels.add(new Point(p.x+1,p.y-1));
 				}
 				
 				value++;
 			}
 		}
 	}
	
	public static void main(String[] args){
		MapManager myMap = new MapManager("img/map/Map.jpg");
	}

 }
