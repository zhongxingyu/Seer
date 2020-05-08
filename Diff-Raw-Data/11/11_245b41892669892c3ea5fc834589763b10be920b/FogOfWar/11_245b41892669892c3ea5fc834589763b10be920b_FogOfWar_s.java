 package ethan.com.FogOfWar;
 
 import java.util.ArrayList;
 
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.SlickException;
 
 import ethan.com.Camera.Camera;
 
 import mickey.com.Sprite.AnimatedUnit;
 import mickey.com.Sprite.Location;
 
 public class FogOfWar {
 	private ArrayList<FogTile> theFog;
 	private Location minimum;
 	private Location maximum;
 
 
 	public FogOfWar(Location min, Location max) throws SlickException{
 		this.maximum = max;
 		this.minimum = min;
 		this.theFog = new ArrayList<FogTile>();
 		createFog();
 	}
 
 
 
 	public void createFog() throws SlickException{
		for(int i = (int) minimum.getX(); i < maximum.getX(); i += 32){
			for(int j = (int) minimum.getY(); j < maximum.getY(); j += 32){
 				Location loc = new Location(i,j);
 				theFog.add(new FogTile(loc));
 			}
 		}
 	}
 
 	public void removeFog(AnimatedUnit player){
 		for(int i = 0; i < theFog.size(); i++){
 			if(player.getLocation().distance(theFog.get(i).getFogLocation())<200){
 				theFog.get(i).setDraw(false);
 			}
 		}
 	}
 
 	public void drawFog(Graphics g,Camera cam){
 		for(int i = 0; i < theFog.size(); i++){
 			if(theFog.get(i).willDraw()){
 				if(theFog.get(i).isCollidingWithCamera(cam)){
 					Location loc = theFog.get(i).getFogLocation();
 					g.drawImage(theFog.get(i).getTile(),loc.getX(),loc.getY());
 				}
 			}
 		}
 	}
 }
