 package nitrogene.world;
 
 import java.util.ArrayList;
 import java.util.Random;
 
 import org.newdawn.slick.Image;
 import org.newdawn.slick.SlickException;
 
 import nitrogene.collision.Vector;
 import nitrogene.core.Craft;
 import nitrogene.core.Planet;
 
 public class ArenaMap {
 	private int planetnumber;
 	private Image star2;
 	private Image sun1, volcanicplanet;
 	private ArrayList<Planet> planetlist;
 	private ArrayList<Craft> craftlist;
 	private ArrayList<Image> imagelist;
 	private Craft craft;
 	private int upbound, downbound, rightbound, leftbound, mapwidth, mapheight,offsetx, offsety;
 	Random random;
 	
 	public ArenaMap(int planetnumber, int offsetx, int offsety, int mapwidth, int mapheight, Craft craft) throws SlickException{
 		this.planetnumber = planetnumber;
 		planetlist = new ArrayList<Planet>();
 		imagelist = new ArrayList<Image>();
 		random = new Random();
 		//star2 = new Image("res/star2.png");
 		volcanicplanet = new Image("res/volcanicplanet2.png");
 		volcanicplanet.setFilter(Image.FILTER_NEAREST);
 		sun1 = new Image("res/sun_1.png");
 		sun1.setFilter(Image.FILTER_NEAREST);
 		//imagelist.add(star2);
 		imagelist.add(sun1);
 		imagelist.add(volcanicplanet);
 		this.craft = craft;
 		craftlist = new ArrayList<Craft>();
 		craftlist.add(craft);
 		
 		this.mapwidth = mapwidth;
 		this.mapheight = mapheight;
 		this.offsetx = offsetx;
 		this.offsety = offsety;
 		this.upbound = offsety;
 		this.downbound = mapheight - offsety;
 		this.leftbound = offsetx;
 		this.rightbound = mapwidth - offsetx;
 	}
 	
	public void generate(int offsetx, int offsety, int mapwidth, int mapheight, Craft craft){
 		for(int e = 0; e < planetnumber; e++){
 			Vector vec = new Vector();
 			vec.x = random.nextInt(mapwidth - (2*offsetx)) + offsetx;
 			vec.y = random.nextInt(mapheight - (2*offsety)) + offsety;
 			int radius = random.nextInt(200)+200;
 			int maxhp = random.nextInt(1000) +500;
 			int imagenum = random.nextInt(imagelist.size());
 			
 			//check for planet validity
 			for(int i = 0; i < planetlist.size(); i++){
 				Planet planet = planetlist.get(i);
 				//radius of this planet + other planet + constant (for ship) + factor for amt of planets total
				if(Math.sqrt(vec.distSQ(new Vector(planet.getCenterX(),planet.getCenterY()))) <= radius + 500 + (planet.getShape().getWidth()/2) ||
				   Math.sqrt(vec.distSQ(new Vector(craft.getCenterX(),craft.getCenterY()))) <= (craft.shield.getImage().getWidth()/2) + radius + 300)
 						{
 					radius = random.nextInt(200)+200;
 					vec.x = random.nextInt(mapwidth - (2*offsetx)) + offsetx;
 					vec.y = random.nextInt(mapheight - (2*offsety)) + offsety;
 					i=-1;
 				}
 			}
 			addPlanet((int)vec.x,(int)vec.y,imagelist.get(imagenum),maxhp,(radius*2)/imagelist.get(imagenum).getWidth());
 		}
 	}
 	
 	private void addPlanet(int centerx, int centery, Image image, int maxhp, int scalefactor){
 		Planet planet = new Planet(centerx, centery, image, maxhp, scalefactor, this);
 		planetlist.add(planet);
 	}
 	
 	public void addCraft(Craft craft){
 		craftlist.add(craft);
 	}
 	
 	public ArrayList<Planet> getPlanets(){
 		return planetlist;
 	}
 	
 	public ArrayList<Craft> getCrafts(){
 		return craftlist;
 	}
 	
 	public int getUpbound(){
 		return upbound;
 	}
 	public int getDownbound(){
 		return downbound;
 	}
 	public int getRightbound(){
 		return rightbound;
 	}
 	public int getLeftbound(){
 		return leftbound;
 	}
 	public int getMapWidth(){
 		return mapwidth;
 	}
 	public int getMapHeight(){
 		return mapheight;
 	}
 	public int getOffsetX(){
 		return offsetx;
 	}
 	public int getOffsetY(){
 		return offsety;
 	}
 }
