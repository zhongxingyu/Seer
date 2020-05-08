 package nitrogene.weapon;
 
 import java.util.ArrayList;
 
 import nitrogene.core.Craft;
 import nitrogene.core.CursorSystem;
 import nitrogene.core.Planet;
 import nitrogene.core.ShipSystem;
 import nitrogene.core.Zoom;
 import nitrogene.npc.NPCship;
 import nitrogene.util.Target;
 import nitrogene.util.TickSystem;
 import nitrogene.world.ArenaMap;
 
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Image;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.Sound;
 
 public class LaserLauncher{
 	private float desx, desy, x, y, camX, camY;
 	public ArrayList<SLaser> slaserlist = new ArrayList<SLaser>();
 	public int accuracy, timer, maxtime,  damage, planetdamage;
 	private double mangle;
 	private float mmangle, craftX, craftY, size, speed;
 	public boolean isRotating;
 	private int interburst, outerburst, burstnumber;
 	public Craft parent;
 	private ArenaMap map;
 	private Image image;
 	private Image proje;
 	private Sound firesound;
 	public int laserId;
 	public String name;
 	private volatile int delta;
 	
 	@Deprecated
 	public LaserLauncher(Craft w, float xpos, float ypos, Image image, int accuracy, int time, float speed, int damage, float size, Image proj) throws SlickException{
 		parent = w;
 		this.x = xpos;
 		this.y = ypos;
 		desx = 0;
 		desy = 0;
 		this.accuracy = accuracy;
 		mangle = 0;
 		mmangle = 0f;
 		this.maxtime = time;
 		this.image = image;
 		isRotating = false;
 		craftX = 0;
 		craftY = 0;
 		camX = 0;
 		camY = 0;
 		this.speed = speed;
 		this.damage = damage;
 		this.size = size;
 		proje = proj;
 		firesound = new Sound("res/sound/laser1final.ogg");
 	}
 	
 	@Deprecated
 	public LaserLauncher(Craft w, float xpos, float ypos, Image image, int accuracy, int time, float speed, int damage, float size) throws SlickException{
 		parent = w;
 		this.x = xpos;
 		this.y = ypos;
 		desx = 0;
 		desy = 0;
 		this.accuracy = accuracy;
 		mangle = 0;
 		mmangle = 0f;
 		this.maxtime = time;
 		this.image = image;
 		isRotating = false;
 		craftX = 0;
 		craftY = 0;
 		camX = 0;
 		camY = 0;
 		this.speed = speed;
 		this.damage = damage;
 		this.size = size;
 		proje = new Image("res/LaserV2ro.png");
 		firesound = new Sound("res/sound/laser1final.ogg");
 	}
 	public LaserLauncher(Craft w, ArenaMap map, float xpos, float ypos, EnumWeapon stat, int id, String n){
 		parent = w;
 		name = n;
 		this.x = xpos;
 		this.y = ypos;
 		this.map = map;
 		desx = 0;
 		desy = 0;
 		this.accuracy = stat.accuracy;
 		mangle = 0;
 		mmangle = 0f;
 		laserId = id;
 		this.planetdamage = stat.planetdamage;
 		this.interburst = stat.interburst;
 		this.outerburst = stat.outerburst;
 		this.burstnumber = stat.burstnumber;
 		try {
 			this.image = new Image(stat.image);
 			this.proje = new Image(stat.laserimage);
 			this.firesound = new Sound(stat.firesound);
 		} catch (SlickException e) {
 			e.printStackTrace();
 		}
 		isRotating = false;
 		craftX = 0;
 		craftY = 0;
 		camX = 0;
 		camY = 0;
 		this.speed = stat.speed;
 		this.damage = stat.damage;
 		this.size = stat.size;
 	}
 	
 	public void setTarget(float desx, float desy){
 		this.desx = desx;
 		this.desy = desy;
 		camX = 0;
 		camY = 0;
 	}
 	
 	public void setTarget(float desx, float desy, float camX, float camY){
 		this.desx = desx;
 		this.desy = desy;
 		this.camX = camX;
 		this.camY = camY;
 	}
 	
 	public float getTargetX(){
 		return desx;
 	}
 	
 	public float getTargetY(){
 		return desy;
 	}
 	
 	public void update(float craftX,float craftY,int delta){
 		this.craftX = craftX;
 		this.craftY = craftY;
 		//if(this.getTimer().isPauseLocked){
 		//	CursorSystem.changeCursor("redfire");
 		//}else CursorSystem.changeCursor("greenfire");
 		this.delta = delta;
 	}
 	
 	public void update(float craftX, float craftY, float camX, float camY, int delta){
 		this.craftX = craftX;
 		this.craftY = craftY;
 		this.camX = camX;
 		this.camY = camY;
 		this.delta = delta;
 	}
 	
 	public void render(Graphics g, float camX, float camY){
 	      if(((this.getAngle()-this.getImage().getRotation()) != 0)) {
 	    	  float rota = Target.getRotation(this);
 	    	  float dist = Math.abs(rota);
 	    		  if(dist >= 100) {
 	    			  if(dist >= 200) {
 	    				  if(dist >= 300) {
 				       this.getImage().rotate(rota/50);
 				       
 				      } else {   //<300
 				       this.getImage().rotate(rota/25);
 				      }
 				      } else {  //<200
 				       this.getImage().rotate(rota/10);
 				      }
 				      } else { //<100
 				      this.getImage().rotate(rota/5);
 				      }
 	    		  //50,40,30,20
 	      } else {
 	          this.getImage().setRotation(this.getAngle());
 	      }
 	      this.getImage().draw(this.getX()+craftX, this.getY()+craftY, 0.8f);
 	      
 	      for(SLaser laser : this.slaserlist){
 	    	  if(laser.getX()-(laser.getImage().getWidth()*laser.getSize())>Zoom.getZoomWidth()+camX||
 						laser.getX()+(laser.getImage().getWidth()*laser.getSize())<camX||
 						laser.getY()-(laser.getImage().getHeight()*laser.getSize())>Zoom.getZoomHeight()+camY||
 						laser.getY()+(laser.getImage().getHeight()*laser.getSize())<camY){
 					laser = null;
 					continue;
 				}
 				laser.getImage().draw(laser.getBoundbox().getX(), laser.getBoundbox().getY(),laser.getSize());
 				laser = null;
 	      }
 	}
 	public void fire() throws SlickException{
 		slaserlist.add(new SLaser(x+craftX,y+craftY, Zoom.scale(camX)+desx, Zoom.scale(camY)+desy,
 				accuracy, speed, damage, planetdamage, size, this.getAngle(), proje, map, this, true));
 	}
 	
 	public float getAngle(){
 			double mecX = (desx+Zoom.scale(camX) - (x+craftX));
 			double mecY = (desy+Zoom.scale(camY) - (y+craftY));
 			mangle = Math.toDegrees(Math.atan2(mecY,mecX));
 			mmangle = (float) mangle;
 			return mmangle;
 	}
 	
 	public Image getImage(){
 		return image;
 	}
 	
 	public float getX(){
 		return x;
 	}
 	
 	public float getY(){
 		return y;
 	}
 	public WeaponTimer getTimer() {
 		return TickSystem.getTimer(this);
 	}
 	public int getBurstNumber(){
 		return burstnumber;
 	}
 	public int getInterburst(){
 		return interburst;
 	}
 	public int getOuterburst(){
 		return outerburst;
 	}
 	
 	public void setFire(int x, int y, float camX, float camY, boolean b){
 		if(!b){
 			if(this.getTimer().getClock().isRunning()){
 				this.getTimer().pause();
 			}
 		} else{
 			if(Target.getTargetObject(x+camX, y+camY, map) != null) {
 				if(Target.getTargetObject(x+camX, y+camY, map).getClass() == Planet.class) {
 					//Target Planet
 					Planet p = (Planet) Target.getTargetObject(x+camX, y+camY, map);				
 					this.setTarget(p.getCenterX(), p.getCenterY());
 				}else if(Target.getTargetObject(camX + x, camY + y, map).getClass() == Craft.class ||
 						Target.getTargetObject(camX + x, camY + y, map).getClass() ==  NPCship.class) {
 					//Target Ship
 					Craft p = (Craft) Target.getTargetObject(camX + x,camY + y, map);				
 					this.setTarget(p.getCenterX(), p.getCenterY());
 				}
 			} else {
 			this.setTarget(x + camX, y + camY);
 			}
 			TickSystem.resume();
 
 		}
 	}
 	
 	public void toggleFire(int x, int y, float camX, float camY){
 		if(this.getTimer().getClock().isRunning()){
 			this.getTimer().pause();
 		}
 		else{
 			if(Target.getTargetObject(x+camX, y+camY, map) != null) {
 				if(Target.getTargetObject(x+camX, y+camY, map).getClass() == Planet.class) {
 					//Target Planet
 					Planet p = (Planet) Target.getTargetObject(x+camX, y+camY, map);				
 					this.setTarget(p.getCenterX(), p.getCenterY());
 				}else if(Target.getTargetObject(camX + x, camY + y, map).getClass() == Craft.class ||
 						Target.getTargetObject(camX + x, camY + y, map).getClass() ==  NPCship.class) {
 					//Target Ship
 					Craft p = (Craft) Target.getTargetObject(camX + x,camY + y, map);				
 					this.setTarget(p.getCenterX(), p.getCenterY());
 				}
 			} else {
 			this.setTarget(x + camX, y + camY);
 			}
 			TickSystem.resume();
 
 		}
 	}
 }
