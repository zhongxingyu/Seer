 package gameView.ingame;
 
 import gameView.GameWindow;
 import gameView.ingame.datatypes.Texture;
 
 import java.awt.Graphics;
 import java.util.LinkedList;
 import java.util.concurrent.ConcurrentHashMap;
 
 
 
 
 
 
 import recources.ConstantsWorker;
 import singleton.GameProperties;
 import singleton.SingletonWorker;
 
 /***********************************************************************************
  * @author:	Marvin Hofmann	Klasse: DQI10	*
  * Prog.Name: Mine.java	Beschreibung	*
  * Version:	1.0	Datum: 01.01.2013	*
  * Compiler:	Oracle Java OS: Microsoft Windows 7	
  * @param <synchronised>*
  * **********************************************************************************/
 public class Mine {
 
 	private ConcurrentHashMap<String, Chunk> map;
 	boolean syncronised = true;
 
 	public Mine(){
 		map = new ConcurrentHashMap<String,Chunk>();
 	}
 
 	public LinkedList<Texture> getTextures(){
 //		while(syncronised == false){
 //			try {
 //				Thread.sleep(10);
 //			} catch (InterruptedException e) {
 //				// TODO Auto-generated catch block
 //				e.printStackTrace();
 //			}
 //		}
 //		syncronised = false;
 		LinkedList<Texture> l = new LinkedList<Texture>();
 		for(Chunk c : map.values()){	
 			c.getTextures(l);
 		}
 //		syncronised = true;
 		return l;
 	}
 
 	public void draw(Graphics g) {
 //		while(syncronised == false){
 //			try {
 //				Thread.sleep(10);
 //			} catch (InterruptedException e) {
 //				// TODO Auto-generated catch block
 //				e.printStackTrace();
 //			}
 //		}
 //		syncronised = false;
 		double px = (GameProperties.playerx/GameProperties.GRAPHICS_SIZE_BLOCK)/GameProperties.MAP_SIZE_CHUNK;
 		double py = (GameProperties.playery/GameProperties.GRAPHICS_SIZE_BLOCK)/GameProperties.MAP_SIZE_CHUNK;
 		Block.relativeplayerx = (GameWindow.getWindowWidth() / 2)-GameProperties.GRAPHICS_SIZE_BLOCK/2-GameProperties.playerx;
 		Block.relativeplayery = (GameWindow.getWindowHeight() / 2)-GameProperties.GRAPHICS_SIZE_BLOCK/2-GameProperties.playery;
 		for (int j = -2; j < 2; j++) {
 			if(((int)py)+j >= 0){
 				for (int i = -2; i < 2; i++) {
 					int x = ((int)px)+i;
 					int y = ((int)py)+j;
 					getChunk(x, y).draw(g);
 				}	
 			}
 		}
 //		for(Chunk c : map.values()){
 //			c.draw(g);
 //		}
 //		syncronised = true;
 	}
 
 	public void addChunk(Chunk c) {
 		map.put(c.xpos+"-"+c.ypos, c);
 	}
 
 	public void updateBlock(Block b) {
 		int x = b.xPos;
 		int y = b.yPos;
 		int chunkx;
 		int blockx;
 		if(x < 0){
 			chunkx = (x+1)/GameProperties.MAP_SIZE_CHUNK;	
 				chunkx--;
 			blockx = modulo((x),GameProperties.MAP_SIZE_CHUNK);
 		}else{
 			chunkx = x/GameProperties.MAP_SIZE_CHUNK;
 			blockx = x%GameProperties.MAP_SIZE_CHUNK;
 		}
 
 		int chunky;
 		int blocky;
 		if(y < 0){
 			chunky = y/GameProperties.MAP_SIZE_CHUNK;
 			chunky--;
 			blocky = modulo(y,GameProperties.MAP_SIZE_CHUNK);
 		}else{
 			chunky = y/GameProperties.MAP_SIZE_CHUNK;
 			blocky = y%GameProperties.MAP_SIZE_CHUNK;
 		}
 		Chunk c = getChunk(chunkx, chunky);
 		c.blocks[blockx][blocky] = b;
 	}
 
 	/**
 	 * Sucht den Chunk an der angegebenen Position.
 	 * Sollte dieser nicht definiert sein, wird ein
 	 * neuer Chunk generiert.
 	 * 
 	 * @param x X-Position des Chunks
 	 * @param y Y-Position des Chunks
 	 * @return Der Chunk an angegebener Position
 	 */
 	public Chunk getChunk(int x, int y){
 		String coordinates = ConstantsWorker.getCoordinateString(x, y);
 		if(!map.containsKey(coordinates)){
 			map.put(coordinates, new Chunk(x,y));
 			SingletonWorker.logger().info("load chunk:	" + x + " x-|-y " + y);
 			SingletonWorker.gameData().getNetworkHandlerThread().requestChunk(x,y);
 		}
 		return map.get(coordinates);
 	}
 
 	public int modulo(int a, int b){
 		if(a < 0){
 			return (a % b + (1 + (Math.abs(a) / b)) * b) % b;
 		}
 		return a % b;
 	}
 
 	/**
 	 * Sucht den Block an der angegebenen Position.
 	 * Dies funktioniert Chunkbergreifend,
 	 * ist der Chunk, der diesen Block enthlt,
 	 * nicht vorhanden, wird er generiert!
 	 * 
 	 * @param x X-Position des Blocks
 	 * @param y Y-Position des Blocks
 	 * @return Der Block an angegebener Position
 	 */
 	public Block getBlock(int x, int y){
 		if(y < 0){
 			return null;
 		}
 		int chunkx;
 		int blockx;
 		if(x < 0){
 			chunkx = (x+1)/GameProperties.MAP_SIZE_CHUNK;	
 				chunkx--;
 			blockx = modulo((x),GameProperties.MAP_SIZE_CHUNK);
 		}else{
 			chunkx = x/GameProperties.MAP_SIZE_CHUNK;
 			blockx = x%GameProperties.MAP_SIZE_CHUNK;
 		}
 
 		int chunky;
 		int blocky;
 		if(y < 0){
 			chunky = y/GameProperties.MAP_SIZE_CHUNK;
 			chunky--;
 			blocky = modulo(y,GameProperties.MAP_SIZE_CHUNK);
 		}else{
 			chunky = y/GameProperties.MAP_SIZE_CHUNK;
 			blocky = y%GameProperties.MAP_SIZE_CHUNK;
 		}
 		Chunk c = getChunk(chunkx, chunky);
 		if(c.dummy){
 			return null;
 		}
 		Block b = c.getRelativeBlock(blockx, blocky);
 		return b;
 	}
 
	public void checkChunks() {
		for(Chunk c : map.values()){
			if(c.dummy){
				SingletonWorker.logger().info("reload chunk:	" + c.xpos + " x-|-y " + c.ypos);
				SingletonWorker.gameData().getNetworkHandlerThread().requestChunk(c.xpos,c.ypos);
			}
		}
	}

 }
