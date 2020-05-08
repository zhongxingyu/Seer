 package edu.chl.codenameg.model;
 
 import java.util.ArrayList;
 
 import edu.chl.codenameg.model.entity.PlayerCharacter;
 
 /** 
  * Class that represents a camera
  * with width, height and position 
  * depending on the player characters.
  */
 public class Camera {
 	private int 						minWidth;
 	private int 						minHeight;
 	private int 						maxWidth;
 	private int 						maxHeight;
 	private int 						width;
 	private int 						height;
 	private float 						camerax;
 	private float 						Xmax;
 	private float 						Ymax;
 	private float 						Xmin;
 	private float 						Ymin;
 	private float 						cameray;
 	private World 						world;
 	private ArrayList<PlayerCharacter> 	players;
 
 	public Camera(World world) {
 		this.minWidth 	= 500;
 		this.minHeight 	= 500;
 		this.maxWidth 	= 700;
 		this.camerax 	= 0;
 		this.world = world;
 		this.players = new ArrayList<PlayerCharacter>();
 		minWidth = 500;
 		minHeight = 500;
 		maxWidth = 700;
 		maxHeight = 700;
 	}
 	
 	public float getX(){
 		return camerax;
 	}
 	
 	public float getY(){
 		return cameray;
 	}
 	
 	public int getWidth() {
 		return width;
 	}
 	
 	public int getHeight() {
 		return height;
 	}
 	
 	public void update() {
 		this.update(10);
 	}
 	
 	public void update(int elapsedTime) {
 		for(PlayerCharacter c : world.getPlayers()){
 			players.add(c);
 		}
 		
		Xmax = players.get(0).getPosition().getX() + players.get(0).getHitbox().getWidth();
 		Xmin = players.get(0).getPosition().getX();
		Ymax = players.get(0).getPosition().getY() + players.get(0).getHitbox().getHeight();
		Ymin = players.get(0).getPosition().getY();
 		
 		for(int i = 1; i < world.getAmountOfPlayers();i++){
		Xmax = Math.max(Xmax, players.get(i).getPosition().getX() + players.get(i).getHitbox().getWidth());
 		Xmin = Math.min(Xmin, players.get(i).getPosition().getX());
		Ymax = Math.max(Ymax, players.get(i).getPosition().getY() + players.get(i).getHitbox().getHeight());
		Ymin = Math.min(Ymin, players.get(i).getPosition().getY());
 		}
 		
 		width = Math.min(Math.max((int)(Ymax - Ymin + minHeight),(int)(Xmax - Xmin + minWidth)),maxWidth);
 		height = Math.min(Math.max((int)(Ymax - Ymin + minHeight),(int)(Xmax - Xmin + minWidth)),maxHeight);
 		camerax =((Xmax+Xmin)/2)-this.getWidth()/2;
 		cameray =((Ymax+Ymin)/2)-this.getHeight()/2;
 		
 		for(PlayerCharacter c : world.getPlayers()){
 			players.remove(c);
 		}
 	}
 }
