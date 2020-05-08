 package nitrogene.core;
 
 import java.awt.FontFormatException;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 
 import javax.swing.Timer;
 
 import nitrogene.collision.AABB;
 import nitrogene.collision.CollisionLibrary;
 import nitrogene.collision.Vector;
 import nitrogene.gui.Minimap;
 import nitrogene.npc.NPCship;
 import nitrogene.npc.Relation;
 import nitrogene.util.Direction;
 import nitrogene.util.PauseButton;
 import nitrogene.util.Stars;
 import nitrogene.util.TickSystem;
 import nitrogene.util.ZoomEnum;
 import nitrogene.weapon.LaserLauncher;
 import nitrogene.weapon.WeaponTimer;
 import nitrogene.world.ArenaMap;
 
 import org.newdawn.slick.Animation;
 import org.newdawn.slick.Color;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Image;
 import org.newdawn.slick.Input;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.Sound;
 import org.newdawn.slick.particles.ConfigurableEmitter;
 import org.newdawn.slick.particles.Particle;
 import org.newdawn.slick.particles.ParticleIO;
 import org.newdawn.slick.particles.ParticleSystem;
 import org.newdawn.slick.state.BasicGameState;
 import org.newdawn.slick.state.StateBasedGame;
 
 
 
 public class GameState extends BasicGameState{
 	private long start;
 	private long elapsed;
 	Craft craft;
 	private boolean firetoggle;
 	private ParticleSystem shockwave;
 	NPCship enemy;
 	PauseButton resume, restart, hangar, menu, options, exit;
 	Image craftImage, statis, mapbackground, slaserimage, sun, backing, shockimage, GUI, pausemenu, img1;
 	Image pauseexitdown, pauseexitup, pausehangardown, pausehangarup, pausemenudown, pausemenuup, pauseoptionsdown, pauseoptionsup,
 	pauserestartdown,pauserestartup,pauseresumeup,pauseresumedown;
 	Particle part;
 	ArenaMap map;
 	Stars stars;
 	private Animation animation;
 	private int mapwidth, mapheight;
 	private Minimap minimap;
 	private int offsetX, offsetY;
 	private final int SCR_width, SCR_height;
 	private int zoomwidth, zoomheight;
 	private float pausemenux, pausemenuy;
 	private float camX, camY;
 	private ArrayList<BoxMesh> boxmeshlist = new ArrayList<BoxMesh>();
 	private ArrayList<CircleMesh> circlemeshlist = new ArrayList<CircleMesh>();
 
 	private boolean PAUSED = false;
 	Sound basicTestLaser,explosionSound;
 
 	public GameState(int width, int height) {
 		this.SCR_width = width;
 		this.SCR_height = height;
 	}
 
 	public void init(GameContainer container, StateBasedGame game)
 			throws SlickException {
 		
		Zoom.setZoom(ZoomEnum.MAP);
 		Zoom.setZoomWindow(SCR_width, SCR_height);
 		
 		//other variables
 				mapwidth = 5000;
 				mapheight = 2000;
 				offsetY = SCR_height/2;
 		    	offsetX = SCR_width/2;
 		    	camX = 0;
 		    	camY = 0;
 
 		//timercontrol.addTimer(new WeaponTimer(Craft.laserlist.get(0)));
 		//load sounds here
 		basicTestLaser = new Sound("res/sound/laser1final.ogg");
 		explosionSound = new Sound("res/sound/Explosionfinal.ogg");
 		
 		//load images and objects here
 		craft = new Craft(SCR_width/2-175, (float) (SCR_height/2-88.5), offsetY, mapheight - offsetY, offsetX, mapwidth - offsetX);
 		TickSystem.addTimer(new WeaponTimer(craft.laserlist.get(0)));
 		enemy = new NPCship(1200,1200, offsetY, mapheight + offsetY, offsetX, offsetX + mapwidth, Relation.HOSTILE);
 		enemy.addCraftTarget(craft);
 		enemy.getImage().rotate(180);
 		
 		sun = new Image("res/sun_1.png");
 		pausemenu = new Image("res/button/pauseback.png");
 		shockimage = new Image("res/shockwave_particle.png");
 		statis = new Image("res/klaarship4.png");
 		slaserimage = new Image("res/LaserV2ro.png");
 		GUI = new Image("res/GUIportrait.png");
     	
     	map = new ArenaMap(4,offsetX,offsetY,mapwidth,mapheight,craft);
     	minimap = new Minimap(300, 121, SCR_width, SCR_height, mapwidth, mapheight, map.getPlanets(), map.getCrafts());
     	stars = new Stars(2,mapwidth,mapheight);
     	//ADDRESS PROBLEM
     	
     	firetoggle = false;
     	
     	//particles
     	shockwave = new ParticleSystem(shockimage,1500);
     	File shockfile = new File("res/test_emitter.xml");
 
     	try {
 			ConfigurableEmitter emitter = ParticleIO.loadEmitter(shockfile);
 			emitter.setPosition(500,500);
 
 			shockwave.addEmitter(emitter);
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
     	shockwave.setBlendingMode(ParticleSystem.BLEND_ADDITIVE);
 		part = shockwave.getNewParticle(shockwave.getEmitter(0), 3000f);
 		
 		//buttons
 		pausemenux = (SCR_width/2) - 52;
 		pausemenuy = (SCR_height/2) - 102.5f;
 		pauseexitdown = new Image("res/button/pauseexitdown.png");
 		pauseexitup = new Image("res/button/pauseexitup.png");
 		pausehangardown = new Image("res/button/pausehangardown.png");
 		pausehangarup = new Image("res/button/pausehangarup.png");
 		pausemenudown = new Image("res/button/pausemenudown.png");
 		pausemenuup = new Image("res/button/pausemenuup.png");
 		pauseoptionsdown = new Image("res/button/pauseoptionsdown.png");
 		pauseoptionsup = new Image("res/button/pauseoptionsup.png");
 		pauserestartdown = new Image("res/button/pauserestartdown.png");
 		pauserestartup = new Image("res/button/pauserestartup.png");
 		pauseresumedown = new Image("res/button/pauseresumedown.png");
 		pauseresumeup = new Image("res/button/pauseresumeup.png");
 		try {
 			resume = new PauseButton(pausemenux + 6, pausemenuy + 6, pauseresumeup, pauseresumedown);
 			menu = new PauseButton(pausemenux + 6, pausemenuy + 105, pausemenuup, pausemenudown);
 			restart = new PauseButton(pausemenux + 6, pausemenuy + 39, pauserestartup, pauserestartdown);
 			hangar = new PauseButton(pausemenux + 6, pausemenuy + 72, pausehangarup, pausehangardown);
 			options = new PauseButton(pausemenux + 6, pausemenuy + 138, pauseoptionsup, pauseoptionsdown);
 			exit = new PauseButton(pausemenux + 6, pausemenuy + 171, pauseexitup, pauseexitdown);
 		} catch (FontFormatException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		
 	}
 	
 	public void update(GameContainer container, StateBasedGame game, int delta)
 			throws SlickException {
 		if(!container.hasFocus()){
 			PAUSED = true;
 		}
 		Zoom.setZoomWindow(SCR_width, SCR_height);
 		
 		Input input = container.getInput();
 		if(input.isKeyPressed(Input.KEY_ESCAPE)){
 			PAUSED = !PAUSED;
 		}
 		if(input.isKeyPressed(Input.KEY_X)){
 			container.exit();
 		}
 		if(!PAUSED){
 		if(TickSystem.isPaused()) {
 			TickSystem.gameResume();
 		}
     	shockwave.update(delta);
     	minimap.update(camX, camY);
     	craft.update(delta, camX, camY);
     	craft.move(20);
     	enemy.update(delta, camX, camY);
     	part.update(delta);
     	
     	
     	//Input Controllers
     	
 		if(input.isKeyPressed(Input.KEY_W)){
 			craft.movement.Toggle(Direction.UP);
 		}
 		if(input.isKeyPressed(Input.KEY_S)){
 				craft.movement.Toggle(Direction.DOWN);
 		}
 		if(input.isKeyPressed(Input.KEY_A)){
 				craft.movement.Toggle(Direction.LEFT);
 		}  
 		if(input.isKeyPressed(Input.KEY_D)){  
 				craft.movement.Toggle(Direction.RIGHT);
 		}
 		if(input.isKeyPressed(Input.KEY_SPACE)){
 			craft.movement.Break(delta);
 		}
 		
 		//projectile control
 		for(int m = 0; m<craft.laserlist.size();m++){
 			LaserLauncher laserlauncher = craft.laserlist.get(m);
 
 			for(int i = 0;i<craft.laserlist.get(m).slaserlist.size();i++){
 				SLaser laser = craft.laserlist.get(m).slaserlist.get(i);
 				laser.move(delta);
 				if (laser.location.getX() > mapwidth - 20 || laser.location.getY() > mapheight - 30 || laser.location.getX() < 0 || laser.location.getY() < 0){
 					craft.laserlist.get(m).slaserlist.remove(i);
 					System.gc();
 				}
 				for(int p = 0;p<map.getPlanets().size();p++){
 					Planet mesh = map.getPlanets().get(p);
 					mesh.getShake().update(delta);
 					if(CollisionLibrary.testCircleAABB(mesh.boundbox,laser.boundbox)){
 						craft.laserlist.get(m).slaserlist.remove(i);
 						mesh.damage(laser.getDamage(), map.getPlanets(), p);
 						explosionSound.play(1f,0.1f);
 						mesh.getShake().shakeObject(3, 1000);
 					//explode()
 					
 					}
 					}
 				for(int e = 0;e<boxmeshlist.size();e++){
 					if(CollisionLibrary.testBoxBox(boxmeshlist.get(e).boundbox,laser.boundbox)){
 						craft.laserlist.get(m).slaserlist.remove(i);
 					//explode()
 					//damage mesh
 					}
 					}
 				laser = null;
 			}
 		}
 		//collision control
 		
 		//update the center coords!
 		craft.updateAABB(craft.getX(), craft.getY());
 		
 		
 		//for: craft vs. circles
 		for(int e = 0;e<circlemeshlist.size();e++){
 			CircleMesh mesh = circlemeshlist.get(e);
 			
 			if(CollisionLibrary.testCircleAABB(mesh.boundbox,craft.boundbox)){
 			//put collision info here!
 			}
 			
 			}
 		//for: craft vs. boxes
 		for(int e = 0;e<boxmeshlist.size();e++){
 			if(CollisionLibrary.testBoxBox(boxmeshlist.get(e).boundbox,craft.boundbox)){
 			 //put stuff here
 			}
 			}
 		
 		//camX = (float) (craft.getX()*Zoom.getZoom().scale+(craft.getImage().getWidth()/2) - (SCR_width / 2));
 		//camY = (float) (craft.getY()*Zoom.getZoom().scale+(craft.getImage().getHeight()/2) - (SCR_height / 2));
		int xv = ((Zoom.getZoomWidth()/4) - (SCR_width/4));
		int yv = ((Zoom.getZoomHeight()/4) - (SCR_height/4));
 		float xv2 = (float) ((craft.getX())+((craft.getImage().getWidth())/2));
 		float yv2 = (float) ((craft.getY())+((craft.getImage().getHeight())/2));
 		
		camX = (float) (xv2 - xv);
    	camY = (float) (yv2 - yv);

 		
 		}
 		else{
 		//pause menu here
 			//Button Controllers
 			if(!TickSystem.isPaused()) {
 				TickSystem.gamePause();
 			}
 			resume.update(container);
 	    	restart.update(container);
 	    	menu.update(container);
 	    	exit.update(container);
 	    	options.update(container);
 	    	hangar.update(container);
 	    	
 	    	if(resume.isClicked()){
 	    		PAUSED = false;
 	    	}
 	    	if(menu.isClicked()) game.enterState(1);
 	    	if(exit.isClicked()) container.exit();
 		}
 		}
 		
 		
 	@Override
 	public void render(GameContainer container, StateBasedGame game, Graphics g)
 			throws SlickException {
 		g.translate(-camX, -camY);
 		g.setBackground(Color.black);
 		
 		g.setColor(Color.red);
 		g.drawRect(0, 0, mapwidth, mapheight);
 		g.setColor(Color.yellow);
 		g.drawRect(0,0, mapwidth-5, mapheight-5);
 		stars.render(camX,camY);
 		
 		g.scale((float)Zoom.getZoom().scale,(float)Zoom.getZoom().scale);
 		enemy.getImage().draw(enemy.getX(), enemy.getY());
 		
 		craft.getImage().draw(craft.getX(), craft.getY());
 		craft.shield.getShieldImage().draw(craft.getShieldX(),craft.getShieldY(),1.2f);
 		//systems
 		craft.core.getImage().drawCentered(craft.core.getX()+craft.getX(),craft.core.getY()+craft.getY());
 		craft.shield.getImage().drawCentered(craft.shield.getX()+craft.getX(),craft.shield.getY()+craft.getY());
 		craft.engine.getImage().drawCentered(craft.engine.getX()+craft.getX(),craft.engine.getY()+craft.getY());
 		craft.lifesupport.getImage().drawCentered(craft.lifesupport.getX()+craft.getX(),craft.lifesupport.getY()+craft.getY());
 		
 		for(int i = 0; i < map.getPlanets().size(); i ++){
 			Planet mesh = map.getPlanets().get(i);
 			//image culling
 			if(mesh.getX()-mesh.getImage().getWidth()*mesh.getScaleFactor()>Zoom.getZoomWidth()+camX||
 					mesh.getX()+mesh.getImage().getWidth()*mesh.getScaleFactor()<camX||
 					mesh.getY()-mesh.getImage().getHeight()*mesh.getScaleFactor()>Zoom.getZoomHeight()+camY||
 					mesh.getY()+mesh.getImage().getHeight()*mesh.getScaleFactor()<camY){
 				mesh = null;
 				continue;
 			}
 			//label at top with health
 			if(mesh.getHp() < mesh.getMaxHp()){
 				if(mesh.getHp() <= 200) g.setColor(Color.red);
 				else if(mesh.getHp() <= 500) g.setColor(Color.orange);
 				else g.setColor(Color.green);
 				float gg = mesh.getHp();
 				float ff = mesh.getMaxHp();
 				g.fillRect(mesh.getX(), mesh.getY() - 20, (gg/ff) * (mesh.boundbox.radius*2), 20);
 			}
 			//drawing planet
 			map.getPlanets().get(i).meshImage.draw(mesh.getX()+mesh.getShake().getDx(),mesh.getY()+mesh.getShake().getDy(),mesh.getScaleFactor());
 			mesh = null;
 		}
 		
 		part.render();
 		shockwave.render();
 		 
 		for(int p = 0; p<craft.laserlist.size(); p++){
 			   LaserLauncher cannon = craft.laserlist.get(p);
 			   cannon.update(craft.getX(), craft.getY());
 			      if(((cannon.getAngle()-cannon.getImage().getRotation()) != 0)) {
 			    	  float rota = func_0001(cannon);
 			    	  float dist = Math.abs(rota);
 			    		  if(dist >= 100) {
 			    			  if(dist >= 200) {
 			    				  if(dist >= 300) {
 						       cannon.getImage().rotate(rota/50);
 						       
 						      } else {   //<300
 						       cannon.getImage().rotate(rota/40);
 						      }
 						      } else {  //<200
 						       cannon.getImage().rotate(rota/30);
 						      }
 						      } else { //<100
 						      cannon.getImage().rotate(rota/20);
 						      }
 			      } else {
 			          cannon.getImage().setRotation(cannon.getAngle());
 			      }
 			      cannon.getImage().draw(cannon.getX()+craft.getX(), cannon.getY()+craft.getY());
 			      
 			      //bullet draw
 			      for(int i = 0;i<craft.laserlist.get(p).slaserlist.size();i++){
 						SLaser laser = craft.laserlist.get(p).slaserlist.get(i);
 						if (laser.isRotated == false){
 							laser.isRotated = true;
 							basicTestLaser.play(1f, 0.5f);
 							laser.getImage().setCenterOfRotation(cannon.getImage().getWidth()/2,cannon.getImage().getHeight()/2);
 							laser.getImage().rotate(laser.getAngle());
 						}
 						if(laser.location.getX()-laser.getImage().getWidth()>Zoom.getZoomWidth()+camX||
 								laser.location.getX()+laser.getImage().getWidth()<camX||
 								laser.location.getY()-laser.getImage().getHeight()>Zoom.getZoomHeight()+camY||
 								laser.location.getY()+laser.getImage().getHeight()<camY){
 							laser = null;
 							continue;
 						}
 						laser.getImage().draw(laser.location.getX(), laser.location.getY(),laser.getSize());
 						laser = null;
 				}
 			 }
 		//gui
 		g.scale((float)Zoom.getZoom().inverse,(float)Zoom.getZoom().inverse);
 		GUI.draw(camX,camY);
 		minimap.render(g);
 		if (PAUSED) {
 	        Color trans = new Color(0f,0f,0f,0.5f);
 	        g.setColor(trans);
 	        g.fillRect(camX,camY, SCR_width, SCR_height);
 	        
 	        pausemenu.draw(pausemenux+camX,pausemenuy+camY);
 	        resume.render(g,camX,camY);
 	        restart.render(g,camX,camY);
 	        options.render(g,camX,camY);
 	        hangar.render(g,camX,camY);
 	        menu.render(g,camX,camY);
 	        exit.render(g,camX,camY);
 	   }
 	}
 
 	public void mousePressed(int button, int x, int y){
 		x *= Zoom.getZoom().inverse;
 		y *= Zoom.getZoom().inverse;
 		if(!PAUSED) {
 
 		if(button == 1) {
 			firetoggle = !firetoggle;
 			if(firetoggle){
 				
 				//craft.laserlist.get(0).setTarget(x, y, camX, camY);
 				//startFire(timer1,craft.laserlist.get(0));
 				TickSystem.resume();
 				start = System.currentTimeMillis();
 			}
 			if (!firetoggle){
 
 				TickSystem.pause();
 			}
 		} else {
 			if(getTargetObject(camX + x,camY + y) != null) {
 				if(getTargetObject(camX + x,camY + y).getClass() == Planet.class) {
 					//Target Planet
 					Planet p = (Planet) getTargetObject(camX + x,camY + y);				
 					craft.laserlist.get(0).setTarget(p.boundbox.center.x, p.boundbox.center.y);
 				}else if(getTargetObject(camX + x,camY + y).getClass() == Craft.class) {
 					//Target Ship
 					Craft p = (Craft) getTargetObject(camX + x,camY + y);				
 					craft.laserlist.get(0).setTarget(p.boundbox.center.x, p.boundbox.center.y);
 				}
 			} else {
 			for(LaserLauncher a : craft.laserlist )
 			a.setTarget(x, y, camX, camY);
 			}
 			TickSystem.resume();
 
 		}
 		}
 	}
 	
 	public void tick(){
 		start = System.currentTimeMillis();
 		try {
 
 			craft.laserlist.get(0).fire();
 		} catch (SlickException e) {
 			e.printStackTrace();
 		}
 	}
 
 	public Object getTargetObject(float f, float g) {
 		for(int e = 0;e<boxmeshlist.size();e++){
 			AABB box = new AABB(1f,1f);
 			box.update(new Vector(f,g));
 			if(CollisionLibrary.testBoxBox(boxmeshlist.get(e).boundbox, box)){
 				return this.boxmeshlist.get(e);
 			}
 		}
 		for(int p = 0; p<map.getPlanets().size(); p++){
 				if(CollisionLibrary.testCirclePoint(map.getPlanets().get(p).boundbox, f, g) == true) {
 					return map.getPlanets().get(p);
 				}
 		}
 		return null;
 		
 	}
 	public float func_0001(LaserLauncher laser) {
   	  if((laser.getAngle() >= 0 && laser.getImage().getRotation() >= 0) || (laser.getAngle() <= 0 && laser.getImage().getRotation() <= 0))
   	  {
   		  return laser.getAngle()-laser.getImage().getRotation();
   	  } else {
   		if(laser.getAngle() >= 0 && laser.getImage().getRotation() <= 0) {
   			float x = Math.abs(laser.getAngle());
   			float y = Math.abs(laser.getImage().getRotation());
   			
   			float oneeighty = (180 - x) + (180 - y);
   			float zero = x + y;
   			if(zero<oneeighty) {
   				return zero;
   			} else {
   				return -oneeighty;
   			}
   		} else if(laser.getAngle() <= 0 && laser.getImage().getRotation() >= 0) {
   			float x = Math.abs(laser.getAngle());
   			float y = Math.abs(laser.getImage().getRotation());
   			
   			float oneeighty = (180 - x) + (180 - y);
   			float zero = x + y;
   			if(zero<oneeighty) {
   				return -zero;
   			} else {
   				return oneeighty;
   			}
   		} else {
   			return 0;
   		}
   	  }
 	}
 
 
 	@Override
 	public int getID() {
 		return 2;
 	}
 
 }
