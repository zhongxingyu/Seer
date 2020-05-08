 package nitrogene.core;
 
 import java.awt.FontFormatException;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Random;
 
 import javax.swing.Timer;
 
 import nitrogene.collision.AABB;
 import nitrogene.collision.CollisionLibrary;
 import nitrogene.collision.Vector;
 import nitrogene.util.Direction;
 import nitrogene.util.PauseButton;
 import nitrogene.weapon.LaserLauncher;
 
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
 	Timer timer1;
 	PauseButton resume, restart, hangar, menu, options, exit;
 	Image craftImage, statis, mapbackground, slaserimage, sun, backing, shockimage, GUI, pausemenu, img1, star1;
 	Image pauseexitdown, pauseexitup, pausehangardown, pausehangarup, pausemenudown, pausemenuup, pauseoptionsdown, pauseoptionsup,
 	pauserestartdown,pauserestartup,pauseresumeup,pauseresumedown;
 	Particle part;
 	private int[] starx, stary;
 	private int n;
 	private int mapwidth, mapheight;
 	private int offsetMaxX, offsetMaxY, offsetMinX, offsetMinY;
 	private int SCR_width, SCR_height;
 	private float pausemenux, pausemenuy;
 	private float camX, camY;
 	private ArrayList<BoxMesh> boxmeshlist = new ArrayList<BoxMesh>();
 	private ArrayList<CircleMesh> circlemeshlist = new ArrayList<CircleMesh>();
 	private ArrayList<Planet> planetlist = new ArrayList<Planet>();
 
 	private boolean PAUSED = false;
 	Sound basicTestLaser,explosionSound;
 
 	public GameState(int width, int height) {
 		this.SCR_width = width;
 		this.SCR_height = height;
 	}
 
 	public void init(GameContainer container, StateBasedGame game)
 			throws SlickException {
 		ActionListener taskPerformer = new ActionListener() {
 	    	public void actionPerformed(ActionEvent evt) {
 	    		tick();
 	    	}
 	    };
 	       timer1 = new Timer(1000, taskPerformer);
 		//load sounds here
 		basicTestLaser = new Sound("res/sound/laser1final.ogg");
 		explosionSound = new Sound("res/sound/Explosionfinal.ogg");
 		
 		//load images and objects here
 		craft = new Craft(500, 400);
 		sun = new Image("res/sun_1.png");
 		star1 = new Image("res/star2.png");
 		pausemenu = new Image("res/button/pauseback.png");
 		shockimage = new Image("res/shockwave_particle.png");
 		planetlist.add(new Planet(1000,1000,sun, 1000));
 		statis = new Image("res/klaarship4.png");
 		slaserimage = new Image("res/LaserV2ro.png");
 		GUI = new Image("res/GUIportrait.png");
 		
 		//other variables
 		mapwidth = 5000;
 		mapheight = 2000;
 		offsetMaxX = mapwidth - 800;
     	offsetMaxY = mapheight - 600;
     	offsetMinX = 400;
     	offsetMinY = 300;
     	
     	firetoggle = false;
     	
     	//stars
     	int biggeststarsize = 2;
     	Random rand = new Random();
     	n = rand.nextInt(10) + 510;
     	starx = new int[n];
     	stary = new int[n];
     	for(int i = 0; i < n; i++) {
     	Random randomx = new Random();
     	starx[i] = randomx.nextInt(mapwidth - biggeststarsize) + 1;
     	Random randomy = new Random();
     	stary[i] = randomy.nextInt(mapheight - biggeststarsize) + 1;
     	}
     	
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
 		Input input = container.getInput();
 		if(input.isKeyPressed(Input.KEY_ESCAPE)){
 			PAUSED = !PAUSED;
 		}
 		if(input.isKeyPressed(Input.KEY_X)){
 			container.exit();
 		}
 		if(!PAUSED){
 			if(timer1.isRunning() == false && firetoggle == true) {
 				//System.out.println(elapsed);
 				timer1.setInitialDelay((int) (1000 - elapsed));
 				timer1.start();
 				start = System.currentTimeMillis();
 			}
     	shockwave.update(delta);
     	craft.update(delta, camX, camY);
     	craft.move(20);
     	part.update(delta);
     	
     	
     	//Input Controllers
     	
 		if(input.isKeyPressed(Input.KEY_W)){
 			if(craft.getY() > offsetMinY){
 			craft.movement.Toggle(Direction.UP);
 			}
 		}
 		if(input.isKeyPressed(Input.KEY_S)){
 			if(craft.getY() < offsetMaxY){
 				craft.movement.Toggle(Direction.DOWN);
 				}
 		}
 		if(input.isKeyPressed(Input.KEY_A)){
 			if(craft.getX() > offsetMinX){
 				craft.movement.Toggle(Direction.LEFT);
 				}              
 		}  
 		if(input.isKeyPressed(Input.KEY_D)){  
 			if(craft.getX() < offsetMaxX){
 				craft.movement.Toggle(Direction.RIGHT);
 				}
 		}
 		if(input.isKeyDown(Input.KEY_SPACE)){
 			System.out.println("CALLED");
 			craft.movement.Break();
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
 				for(int e = 0;e<planetlist.size();e++){
 					Planet mesh = planetlist.get(e);
 					if(CollisionLibrary.testCircleAABB(mesh.boundbox,laser.boundbox)){
 						craft.laserlist.get(m).slaserlist.remove(i);
 					//explode()
 					mesh.damage(laser.getDamage(), planetlist, circlemeshlist, e);
 					explosionSound.play(1f,0.1f);
 					//damage mesh
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
 		
 		
 		camX = craft.getX() - 800 / 2;
     	camY = craft.getY() - 600 / 2;
 		
 		}
 		else{
 		//pause menu here
 			//Button Controllers
 			if(timer1.isRunning() == true) {
 				elapsed = System.currentTimeMillis() - start;
 				timer1.stop();
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
 		
 		for(int g1 = 0; g1 < n; g1++){
 			star1.draw(starx[g1],stary[g1]);
 		}
 		
 		craft.getImage().draw(craft.getX(), craft.getY());
 		craft.shield.getImage().draw(craft.getShieldX(),craft.getShieldY());
 		for (int e = 0; e<planetlist.size();e++){
 			Planet mesh = planetlist.get(e);
 			mesh.getImage().draw(mesh.getX(),mesh.getY(),4);
 			if(mesh.getHp() < mesh.getMaxHp()){
 				if(mesh.getHp() <= 200) g.setColor(Color.red);
 				else if(mesh.getHp() <= 500) g.setColor(Color.orange);
 				else g.setColor(Color.green);
 				float gg = mesh.getHp();
 				float ff = mesh.getMaxHp();
 				g.fillRect(mesh.getX(), mesh.getY() - 20, (gg/ff) * (mesh.boundbox.radius*2), 20);
 			}
 		}
 		part.render();
 		shockwave.render();
 
 		
 		//bullet draw
 		for(int m = 0; m<craft.laserlist.size();m++)
 			for(int i = 0;i<craft.laserlist.get(m).slaserlist.size();i++){
 				SLaser laser = craft.laserlist.get(m).slaserlist.get(i);
 				if (laser.isRotated == false){
 					laser.isRotated = true;
 					basicTestLaser.play(1f, 0.5f);
 					laser.getImage().rotate(laser.getAngle());
 				}
 				laser.getImage().draw(laser.location.getX(), laser.location.getY(),laser.getSize());
 				laser = null;
 
 		}
 		 
 		for(int p = 0; p<craft.laserlist.size(); p++){
 
 			   LaserLauncher laser = craft.laserlist.get(p);
 			   laser.update(craft.getX(), craft.getY());
 			      if(((laser.getAngle()-laser.getImage().getRotation()) != 0)) {
 			    	  float rota = func_0001(laser);
 			    	  float dist = Math.abs(rota);
 			    		  if(dist >= 100) {
 			    			  if(dist >= 200) {
 			    				  if(dist >= 300) {
 						       laser.getImage().rotate(rota/50);
 						       
 						      } else {   //<300
 						       laser.getImage().rotate(rota/40);
 						      }
 						      } else {  //<200
 						       laser.getImage().rotate(rota/30);
 						      }
 						      } else { //<100
 						       laser.getImage().rotate(rota/20);
 						      }
 			      } else {
 			          laser.getImage().setRotation(laser.getAngle());
 			      }
 			      laser.getImage().draw(laser.getX()+craft.getX(), laser.getY()+craft.getY());
 			 }
 		//gui
 		GUI.draw(camX,camY);
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
 		if(!PAUSED) {
 
 		if(button == 1) {
 			firetoggle = !firetoggle;
 			if(firetoggle){
 				
 				//craft.laserlist.get(0).setTarget(x, y, camX, camY);
 				//startFire(timer1,craft.laserlist.get(0));
 				timer1.setInitialDelay((int) (1000 - elapsed));
 				timer1.start();
 				start = System.currentTimeMillis();
 			}
 			if (!firetoggle){
 
 				timer1.stop();
 				elapsed = System.currentTimeMillis() - start;
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
 			craft.laserlist.get(0).setTarget(x, y, camX, camY);
 			}
 			if(timer1.isRunning() == false) {
 				firetoggle = true;
 				timer1.setInitialDelay((int) (1000 - elapsed));
 				timer1.start();
 				start = System.currentTimeMillis();
 			}
 
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
 		for(int i = 0; i < this.planetlist.size(); i++) {
 			if(CollisionLibrary.testCirclePoint(this.planetlist.get(i).boundbox, f, g) == true) {
 				return this.planetlist.get(i);
 			}
 		}
 		for(int e = 0;e<boxmeshlist.size();e++){
 			AABB box = new AABB(1f,1f);
 			box.update(new Vector(f,g));
			if(CollisionLibrary.testBoxBox(boxmeshlist.get(e).boundbox, box)){
 				return this.boxmeshlist.get(e);
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
