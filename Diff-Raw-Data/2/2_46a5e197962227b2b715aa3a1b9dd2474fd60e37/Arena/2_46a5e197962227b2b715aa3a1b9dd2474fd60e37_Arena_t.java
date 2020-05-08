 package Model.Arenas;
 //import java.awt.Image;
 import java.util.ArrayList;
 
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.tiled.TiledMap;
 
 import Model.Obstacles.Obstacle;
 
 
 public class Arena {
 
 	private int width;
 	private int height;
 	private String terrain;
 	private Obstacle[] obstacles;
	private ArrayList<Integer> destroyedObstacles = new ArrayList<Integer>();
 	
 	private TiledMap background;
 	
 	//private Image[] images;
 	//private int[][] mask;
 	
 	private String name;
 	
 	
 	public Arena(String name){
 		this.name = name;
 		try {
 			background = new TiledMap("res/tileset/grassBackground.tmx");
 		} catch (SlickException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	public TiledMap getBackgroundMap(){
 		return background;
 	}
 	
 	public int getWidth(){
 		return width;
 	}
 	public int getHeight(){
 		return height;
 	}
 	public String getTerrain(){
 		return terrain;
 	}
 	public Obstacle[] getObstacles(){
 		return obstacles;
 	}
 	public void setObstacles(Obstacle[] obs){
 		obstacles = obs;
 	}
 	public synchronized void removeObstacle(int index) {
 		obstacles[index] = null;
 		destroyedObstacles.add(new Integer(index));
 	}
 	public String getName(){
 		return name;
 	}
 	public int[] getDestroyedObstacles() {
 		int[] destroyedObstacles = null;
 		if(this.destroyedObstacles != null) {
 			destroyedObstacles = new int[this.destroyedObstacles.size()];
 			for(int i = 0; i < this.destroyedObstacles.size(); i++) {
 				destroyedObstacles[i] = this.destroyedObstacles.get(i);
 			}
 			this.destroyedObstacles.clear();
 		}
 		return destroyedObstacles;
 	}
 }
