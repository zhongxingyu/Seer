 package dsz;
 
 import java.io.BufferedReader;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.ArrayList;
 import org.jsfml.graphics.*;
 
 /**
  * An entity that handles and controls a map and all its contents.
  */
 public class MapEntity extends Entity {
 	
 	TextureArray textures;
 	Map currentMap;
 	int[][] mapListInt = new int[DSZ.mapWidth][DSZ.mapHeight];
 	Map[][] mapList = new Map[DSZ.mapWidth][DSZ.mapHeight];
 	int[][] numOfZombies = new int[DSZ.mapWidth][DSZ.mapHeight]; //Number of zombies in map[x][y]
	boolean[][] chestMap = new boolean[DSZ.mapWidth][DSZ.mapHeight]; // whether there is a map at this point or not
 	ChestEntity chest;
 	Sprite spriteMap = new Sprite();
 	String defaultMap = "";
 	int currentX, currentY;
 	boolean newMap = false;
 	ArrayList<ZombieEntity> zombies = new ArrayList<ZombieEntity>();
 	
 	
 	/**
 	 * Constructs a MapEntity uses texs as its textures.
 	 * @param texs The TextureArray that the map will use for its textures.
 	 */
 	public MapEntity(TextureArray texs, FileReader file){
 		super();
 		textures = texs;
 		currentMap = new Map(textures);
 		//Get default map data
 		String buf;
 		try{
 		BufferedReader br = new BufferedReader(file);
 		while((buf=br.readLine()) != null){
 			defaultMap+=buf;
 		}
 		br.close();
 		file.close();
 		} catch(IOException e){
 			System.out.println("Can't open file");
 			System.exit(0);
 		}
 		
 		//create a random level
 		generateLevel();
 		
 		//Set inherited variables
 		type = "Map";
 		x=0;
 		y=120;
 		
 		spriteMap.setPosition(x, y);
 		spriteMap.scale(2,2);
 		
 		drawable = true;
 		collides = true;
		readyToUpdate = false;
 		alive = true;
 		updatePosition();
 		
 	}
 	
 	/**
 	 * Get the textureID of the current map at (x,y)
 	 * @param x coordinate
 	 * @param y coordinate
 	 * @return texture ID at (x,y)
 	 */
 	int getTextureID(int x, int y){
 		return currentMap.textureMap.get((x*currentMap.YSize)+y);
 	}
 	
 	/**
 	 * Get the collision map ID of the current map at (x,y)
 	 * @param x coordinate
 	 * @param y coordinate
 	 * @return collision map ID at (x,y)
 	 */
 	int getCollisionID(int x, int y){
 		return currentMap.colMap.get((x*currentMap.YSize)+y);
 	}
 
 	/**
 	 * Sets the spriteMap to the current map data.
 	 *  map.loadMap or map.loadMapFile should be called before map is updated.
 	 */
 	@Override
 	void update(int framecount) {
		/*for(int i=0;i<zombies.size();i++){
 			if(zombies.get(i).health <= 0){
				DSZ.entityManager.removeID(zombies.get(i).ID);
 				zombies.remove(i);
 				i=0;
 				numOfZombies[currentX][currentY]--;
 				DSZ.kills++;
 			}
 		}
		if(chest != null){
			if(chest.open){
				chestMap[currentX][currentY] = false;
				chest = null;
				DSZ.entityManager.removeType("Chest");
			}
		}*/
		return;
 	}
 	
 	
 	void generateLevel(){
 		int tries = 0;
 		int dir;
 		int x = DSZ.random.nextInt(DSZ.mapWidth), y = DSZ.random.nextInt(DSZ.mapHeight);
 		mapListInt[x][y] = 1;
 		//Here it generates the integer list of maps
 		while(tries < 100){
 			tries++;
 			dir = DSZ.random.nextInt(4);
 			switch(dir){
 			case 0: //Place map up
 				if(y-1 >= 0 && mapListInt[x][y-1] == 0){
 					y--;
 					mapListInt[x][y] = 1;
 					numOfZombies[x][y] = DSZ.random.nextInt(5)+1;
 				}
 				break;
 			case 1://place map right
 				if(x+1 < DSZ.mapWidth && mapListInt[x+1][y] == 0){
 					x++;
 					mapListInt[x][y] = 1;
 					numOfZombies[x][y] = DSZ.random.nextInt(5)+1;
 				}
 				break;
 			case 2: //place map down
 				if(y+1 < DSZ.mapHeight && mapListInt[x][y+1] == 0){
 					y++;
 					mapListInt[x][y] = 1;
 					numOfZombies[x][y] = DSZ.random.nextInt(5)+1;
 				}
 				break;
 			case 3: //place map left
 				if(x-1 >= 0 && mapListInt[x-1][y] == 0){
 					x--;
 					mapListInt[x][y] = 1;
 					numOfZombies[x][y] = DSZ.random.nextInt(5)+1;
 				}
 				break;
 			}
 		}
 		
 		for(x=0;x<DSZ.mapWidth;x++){
 			for(y=0;y<DSZ.mapHeight;y++){
 				mapList[x][y] = new Map(textures);
 				mapList[x][y].loadMap(defaultMap);
 				//Check if there is map above
 				if(y-1 >= 0 && mapListInt[x][y-1] == 1){
 					mapList[x][y].setTextureID(11, 0, 12);
 					mapList[x][y].setTextureID(12, 0, 0);
 					mapList[x][y].setTextureID(13, 0, 10);
 					
 					mapList[x][y].setCollisionID(11, 0, 2);
 					mapList[x][y].setCollisionID(12, 0, 3);
 					mapList[x][y].setCollisionID(13, 0, 2);
 				}
 				//Check if there is map to the right
 				if(x+1 < DSZ.mapWidth && mapListInt[x+1][y] == 1){
 					mapList[x][y].setTextureID(DSZ.tileWidth-1, 6, 10);
 					mapList[x][y].setTextureID(DSZ.tileWidth-1, 7, 0);
 					mapList[x][y].setTextureID(DSZ.tileWidth-1, 8, 14);
 					
 					mapList[x][y].setCollisionID(DSZ.tileWidth-1, 6, 2);
 					mapList[x][y].setCollisionID(DSZ.tileWidth-1, 7, 4);
 					mapList[x][y].setCollisionID(DSZ.tileWidth-1, 8, 2);
 				}
 				//Check if there is map below
 				if(y+1 < DSZ.mapHeight && mapListInt[x][y+1] == 1){
 					mapList[x][y].setTextureID(11, DSZ.tileHeight-1, 1);
 					mapList[x][y].setTextureID(12, DSZ.tileHeight-1, 0);
 					mapList[x][y].setTextureID(13, DSZ.tileHeight-1, 14);
 					
 					mapList[x][y].setCollisionID(11, DSZ.tileHeight-1, 2);
 					mapList[x][y].setCollisionID(12, DSZ.tileHeight-1, 5);
 					mapList[x][y].setCollisionID(13, DSZ.tileHeight-1, 2);
 				}
 				//check if there is map to the left
 				if(x-1 >= 0 && mapListInt[x-1][y] == 1){
 					mapList[x][y].setTextureID(0, 6, 12);
 					mapList[x][y].setTextureID(0, 7, 0);
 					mapList[x][y].setTextureID(0, 8, 1);
 					
 					mapList[x][y].setCollisionID(0, 6, 2);
 					mapList[x][y].setCollisionID(0, 7, 6);
 					mapList[x][y].setCollisionID(0, 8, 2);
 				}
 			}
 		}
 		
 		currentX = DSZ.random.nextInt(DSZ.mapWidth);
 		currentY = DSZ.random.nextInt(DSZ.mapHeight);
 		while(mapListInt[currentX][currentY] == 0){
 			currentX = DSZ.random.nextInt(DSZ.mapWidth);
 			currentY = DSZ.random.nextInt(DSZ.mapHeight);
 		}
 		
 
 		//Place exit
 		while(true){
 			x = DSZ.random.nextInt(DSZ.mapWidth);
 			y = DSZ.random.nextInt(DSZ.mapHeight);
 			if(x != currentX && y != currentY && mapListInt[x][y] == 1){
 				mapList[x][y].setTextureID(12, 7, 11);
 				mapList[x][y].setCollisionID(12, 7, 1);
 				break;
 			}
 		}
 		//place chest
 		while(true){
 			x = DSZ.random.nextInt(DSZ.mapWidth);
 			y = DSZ.random.nextInt(DSZ.mapHeight);
 			if(x != currentX && y != currentY && mapListInt[x][y] == 1){
 				System.out.println(x+" "+y);
 				chestMap[x][y] = true;
 				break;
 			}
 		}
 		
 		currentMap = mapList[currentX][currentY];
 		numOfZombies[currentX][currentY] = 0;
 	}
 	
 	void reset(){
 		for(int x=0;x<DSZ.mapWidth;x++){
 			for(int y=0;y<DSZ.mapHeight;y++){
 				mapListInt[x][y] = 0;
 				numOfZombies[x][y] = 0;
 				chestMap[x][y] = false;
 				chest = null;
 				mapList[x][y] = null;
 			}
 		}
 		currentMap = null;
 		generateLevel();
 		updatePosition();
 	}
 	
 	void updatePosition(){
 		for(ZombieEntity zomb : zombies){
 			DSZ.entityManager.removeID(zomb.ID);
 		}
 		if(chest != null){
 			DSZ.entityManager.removeType("Chest");		
 		}
 		zombies.clear();
 		currentMap = mapList[currentX][currentY];
 		spriteMap.setTexture(currentMap.drawMap());
 		ArrayList<FloatRect> collisionBoxList = new ArrayList<FloatRect>();
 		int currentSlot = 0;
 		for(int x=0;x<currentMap.XSize;x++){
 			for(int y=0;y<currentMap.YSize;y++){
 				if(currentMap.colMap.get(currentSlot) > 0){
 					collisionBoxList.add(new FloatRect(x*32+this.x,y*32+this.y,32,32));
 				}
 				currentSlot++;
 			}
 		}
 		collisionBox = (FloatRect[])collisionBoxList.toArray(new FloatRect[collisionBoxList.size()]);
 		collisionBoxList.clear();
 		for(int i=0;i<numOfZombies[currentX][currentY];i++){
 			ZombieEntity zombie = new ZombieEntity(DSZ.zombieTexture,DSZ.player,
 					DSZ.random.nextInt((DSZ.tileWidth-3)*32 - 3*32)+3*32,DSZ.random.nextInt((DSZ.tileHeight-3)*32-((3*32)+120)+(3*32))+((3*32)+120));
 			zombie.update(0);
 			zombies.add(zombie);
 			DSZ.entityManager.entityList.add(zombies.get(i));
 		}
 		
 		if(chestMap[currentX][currentY] == true){
 			chest = new ChestEntity(textures.tiles.get(13));
 			chest.update(0);
 			DSZ.entityManager.entityList.add(chest);
 		} else chest = null;
 	}
 
 	/**
 	 * Returns nothing because map doesn't do anything on collision
 	 */
 	@Override
 	void onCollision(Entity object,FloatRect objectCollisionBox) {
 		return;
 	}
 	
 	/**
 	 * Returns the sprite version of the map to draw to the screen.
 	 */
 	@Override
 	Sprite draw() {
 		return spriteMap;
 	}
 
 }
