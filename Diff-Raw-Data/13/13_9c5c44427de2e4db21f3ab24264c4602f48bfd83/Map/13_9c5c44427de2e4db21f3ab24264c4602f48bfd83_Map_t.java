 package crazytd.map;
 
 import org.robobrain.sdk.game.World;
 import org.robobrain.sdk.graphics.Vector;
 
 public class Map {
 	int maxX, maxY;
 	Block[][] theMap;
 	
 	public Map(int maxX, int maxY){
 		this.maxX=maxX;
 		this.maxY=maxY;
 		theMap =new Block[maxY][maxX];
 	}
 	
 	public Block getBlock(int x, int y){
 		return theMap[x][y];
 	}
 	
 	public Block getBlockByCoordinate(float x, float y){
 		return theMap[Math.round(y/Block.tileSize)][Math.round(x/Block.tileSize)];
 	}
 	
 	public Block getBlockByCoordinate(Vector coordinate){
 		return theMap[Math.round(coordinate.y/Block.tileSize)][Math.round(coordinate.x/Block.tileSize)];
 	}
 	
 	public void addMapToWorld(World world){
 		
		for(int x=0;x<theMap[0].length;x++){
			for(int y=0;y<theMap.length;y++){
				world.addEntity(theMap[y][x].getMapElement());
 			}
 		}
 	}
 	
 	public Block[][] getMap(){
 		return theMap;
 	}
 }
