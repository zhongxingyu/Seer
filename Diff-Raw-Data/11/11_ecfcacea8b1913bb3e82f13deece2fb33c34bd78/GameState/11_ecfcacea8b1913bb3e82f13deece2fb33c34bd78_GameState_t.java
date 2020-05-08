 package nitrogene.core;
 
 import java.awt.FontFormatException;
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 
 
 
 
 
 
 
 
 
 
 import nitrogene.collision.AABB;
 import nitrogene.collision.CollisionLibrary;
 import nitrogene.collision.Vector;
 import nitrogene.gui.Hotbar;
 import nitrogene.gui.Minimap;
 import nitrogene.npc.NPCship;
 import nitrogene.npc.Relation;
 import nitrogene.npc.TaskFire;
 import nitrogene.npc.TaskMove;
 import nitrogene.objecttree.PhysicalObject;
 import nitrogene.util.AnimationManager;
 import nitrogene.util.Direction;
 import nitrogene.util.Explosion;
 import nitrogene.util.PauseButton;
 import nitrogene.util.Stars;
 import nitrogene.util.Target;
 import nitrogene.util.TickSystem;
 import nitrogene.util.ZoomEnum;
 import nitrogene.weapon.EnumWeapon;
 import nitrogene.weapon.LaserLauncher;
 import nitrogene.weapon.SLaser;
 import nitrogene.world.ArenaMap;
 import nitrogene.world.DroppedItem;
 
 import org.lwjgl.opengl.GL11;
 import org.newdawn.slick.Animation;
 import org.newdawn.slick.Color;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Image;
 import org.newdawn.slick.Input;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.Sound;
 import org.newdawn.slick.SpriteSheet;
 import org.newdawn.slick.geom.Point;
 import org.newdawn.slick.particles.ConfigurableEmitter;
 import org.newdawn.slick.particles.Particle;
 import org.newdawn.slick.particles.ParticleIO;
 import org.newdawn.slick.particles.ParticleSystem;
 import org.newdawn.slick.state.BasicGameState;
 import org.newdawn.slick.state.StateBasedGame;
 import org.newdawn.slick.util.pathfinding.PathFinder;
 
 
 
 public class GameState extends BasicGameState{
 	Craft craft;
 	private ParticleSystem shockwave;
 	NPCship enemy;
 	public Hotbar guihotbar;
 	PauseButton resume, restart, hangar, menu, options, exit;
 	Image craftImage, statis, mapbackground, slaserimage, sun, backing, shockimage, GUI, pausemenu, img1, enemyImage;
 	Image pauseexitdown, pauseexitup, pausehangardown, pausehangarup, pausemenudown, pausemenuup, pauseoptionsdown, pauseoptionsup,
 	pauserestartdown,pauserestartup,pauseresumeup,pauseresumedown;
 	Particle part;
 	ArenaMap map;
 	Stars stars;
 	SpriteSheet spriteex;
 	private int mousewheelposition;
 	private Animation animation;
 	private int mapwidth, mapheight;
 	//private Minimap minimap;
 	private int offsetX, offsetY;
 	private final int SCR_width, SCR_height;
 	private int zoomwidth, zoomheight;
 	private float pausemenux, pausemenuy;
 	private float camX, camY;
 	private boolean isRotated = false;
 	private short selected = 0;
 
 	private boolean PAUSED = false;
 
 	
 	public GameState(int width, int height) {
 
 		this.SCR_width = width;
 		this.SCR_height = height;
 	}
 
 
 	public void init(GameContainer container, StateBasedGame game)
 			throws SlickException {
 		CursorSystem.init();
 		mousewheelposition = -1;
 		//set largest zoom for generation
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
 		
 		map = new ArenaMap(5,offsetX,offsetY,mapwidth,mapheight,craft);
 		
 		//load images and objects here
 		craftImage = new Image("res/klaarship6.png");
 		//craftImage.setFilter(Image.FILTER_NEAREST);
 		craft = new Craft(SCR_width/2-175, (float) (SCR_height/2-88.5), craftImage, 1, map);
 		map.addCraft(craft);
 		guihotbar = new Hotbar(craft);
 		enemyImage = new Image("res/klaarship4.png");
 		enemy = new NPCship(1200, 1200, enemyImage, 1, map, Relation.HOSTILE);
 		enemy.getImage().rotate(180);
 		map.addCraft(enemy);
 		
 		map.generate(map.getOffsetX(), map.getOffsetY(), mapwidth, mapheight, craft);
 		
 		sun = new Image("res/sun_1.png");
 		pausemenu = new Image("res/button/pauseback.png");
 		shockimage = new Image("res/shockwave_particle.png");
 		statis = new Image("res/klaarship4.png");
 		slaserimage = new Image("res/LaserV2ro.png");
 		GUI = new Image("res/GUIportrait.png");
     	
     	//minimap = new Minimap(300, 121, SCR_width, SCR_height, mapwidth, mapheight, map.getPlanets(), map.getCrafts());
 		int varx = (int)(Zoom.getZoomWidth()-this.SCR_width);
 		int vary = (int)(Zoom.getZoomWidth()-this.SCR_height);
     	stars = new Stars(2,mapwidth+(2*varx),mapheight+(2*vary), -1*(varx), -1*(vary), 510);
     	//ADDRESS PROBLEM
     	
     	
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
 		
 		this.PAUSED = true;
 	}
 	
 	@Override
 	   public void enter(GameContainer container, StateBasedGame game)
 	         throws SlickException {
 	      super.enter(container, game);
 	      /*
 	      for(LaserLauncher l : craft.laserlist){
 	    	  TickSystem.removeTimer(TickSystem.getTimer(l));
 	      }
 	      */
 	      enemy.addCraftTarget(craft);
 	      craft.loadSystems(GlobalInformation.getStartingWeapons());
 	      enemy.loadSystems(GlobalInformation.getStartingWeapons());
 	      //enemy.addTask(new TaskFire(enemy, craft, 0));
 	     // enemy.addTaskOverride(new TaskMove(enemy, 0, 0));
 	      this.PAUSED = false;
 	     
 	   }
 	
 	@Override
 	public void leave(GameContainer container, StateBasedGame game) throws SlickException{
 		super.leave(container, game);
 		this.PAUSED = true;
 	}
 	
 	public void update(GameContainer container, StateBasedGame game, int delta)
 			throws SlickException {
 		if(!container.hasFocus()){
 			PAUSED = true;
 		}
 		
 		Zoom.setZoomWindow(SCR_width, SCR_height);
 		if(mousewheelposition == -1){
 			Zoom.setZoom(ZoomEnum.NORMAL);
 		} else if(mousewheelposition == 0){
 		    Zoom.setZoom(ZoomEnum.LARGE);
 		} else if (mousewheelposition == 1){
 			Zoom.setZoom(ZoomEnum.MAP);
 		} else{
 			System.out.println("ERROR! in beginning of update method, scroll zoom controls!");
 		}
 		
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
 		AnimationManager.updateAnimation(delta);
 		CursorSystem.update(container);
     	shockwave.update(delta);
     	//minimap.update(camX, camY);
     	part.update(delta);
     	
     	
     	//Input Controllers
     	if(input.isKeyDown(Input.KEY_RSHIFT) || input.isKeyDown(Input.KEY_LSHIFT)){
 			craft.getMovement().Break(delta);
 		} else{
 			if(input.isKeyDown(Input.KEY_W)){
 				if(!craft.getMovement().getToggle(Direction.FORWARD)){
 				craft.getMovement().changeAccelerator(Direction.FORWARD, true);}
 			} else{
 				if(craft.getMovement().getToggle(Direction.FORWARD)){
 				craft.getMovement().changeAccelerator(Direction.FORWARD, false);}
 			}
 			if(input.isKeyDown(Input.KEY_S)){
 				if(!craft.getMovement().getToggle(Direction.BACKWARD)){
 				craft.getMovement().changeAccelerator(Direction.BACKWARD, true);}
 			} else{
 				if(craft.getMovement().getToggle(Direction.BACKWARD)){
 				craft.getMovement().changeAccelerator(Direction.BACKWARD, false);}
 			}
 			if(input.isKeyDown(Input.KEY_A)){
 				if(!craft.getMovement().getToggle(Direction.UPPERANGLE)){
 				craft.getMovement().changeAccelerator(Direction.UPPERANGLE, true);}
 			} else{
 				if(craft.getMovement().getToggle(Direction.UPPERANGLE)){
 				craft.getMovement().changeAccelerator(Direction.UPPERANGLE, false);}
 			}
 			if(input.isKeyDown(Input.KEY_D)){
 				if(!craft.getMovement().getToggle(Direction.UNDERANGLE)){
 				craft.getMovement().changeAccelerator(Direction.UNDERANGLE, true);}
 			} else{
 				if(craft.getMovement().getToggle(Direction.UNDERANGLE)){
 				craft.getMovement().changeAccelerator(Direction.UNDERANGLE, false);}
 			}
 		}
     	
     	if(craft.laserlist.size() > 0 && input.isKeyPressed(Input.KEY_1)){
     		selected = 0;
     	} else if(craft.laserlist.size() > 1 && input.isKeyPressed(Input.KEY_2)){
     		selected = 1;
     	} else if(craft.laserlist.size() > 2 && input.isKeyPressed(Input.KEY_3)){
     		selected = 2;
     	} else if(craft.laserlist.size() > 3 && input.isKeyPressed(Input.KEY_4)){
     		selected = 3;
     	} else if(craft.laserlist.size() > 4 && input.isKeyPressed(Input.KEY_5)){
     		selected = 4;
     	} else if(craft.laserlist.size() > 5 && input.isKeyPressed(Input.KEY_6)){
     		selected = 5;
    	} else if(craft.laserlist.size() == 0){
     		selected = -1;
     	}
     	
 		for(int n = 0; n < map.getObjList().size(); n++){
 			PhysicalObject obj = map.getObjList().get(n);
 			if(obj.getClass() == Craft.class){
 				obj.update(delta);
 				for(int m = 0; m<craft.laserlist.size(); m++) {
 					LaserLauncher laserlauncher = craft.laserlist.get(m);
 					 laserlauncher.update(craft.getX(), craft.getY(),delta);
 					
 					 for(int i = 0;i<laserlauncher.slaserlist.size();i++){
 						SLaser laser = laserlauncher.slaserlist.get(i);
 						laser.move(10,delta);
 						for(int e = 0; e < map.getPlanets().size(); e++){
 							Planet mesh = map.getPlanets().get(e);
 							mesh.getShake().update(delta);
 							if(mesh.isColliding(laser)){
 								AnimationManager.addAnimation(new Explosion(laser.getCenterX(), laser.getCenterY(), 2.5f, 100));
 								mesh.getShake().shakeObject(3, 1000);
 								laserlauncher.slaserlist.remove(laser);
 								mesh.damage(laser.getPlanetDamage(), map);
 							}
 							}
 						for(int r = 0; r < map.getCrafts().size(); r++){
 							Craft craft = map.getCrafts().get(r);
 							if(craft.isColliding(laser) && !craft.equals(obj)){
 								AnimationManager.addAnimation(new Explosion(laser.getCenterX(), laser.getCenterY(), 2.5f, 100));
 								laserlauncher.slaserlist.remove(laser);
 								//Damage crafts here!
 							}
 						}
 						//map.setPlanets(planetlist);
 						/*
 						for(PhysicalObject temp : map.getObjList()){
 							if(!temp.equals(obj) && temp.isColliding(laser)){
 								AnimationManager.addAnimation(new Explosion(laser.getCenterX(), laser.getCenterY(), 2.5f, 100));
 								laserlauncher.slaserlist.remove(laser);
 							}
 						}
 						*/
 						if (laser.getX() > mapwidth - 20 || laser.getY() > mapheight - 30 || laser.getX() < 0 || laser.getY() < 0){
 							laserlauncher.slaserlist.remove(laser);
 						}
 						laser = null;
 					}
 					laserlauncher = null;
 				}
 			} else if (obj.getClass() == NPCship.class){
 				NPCship temp = (NPCship) obj;
 				temp.update(delta,camX,camY);
 				for(int m = 0; m<enemy.laserlist.size(); m++) {
 					LaserLauncher laserlauncher = enemy.laserlist.get(m);
 					laserlauncher.update(enemy.getX(), enemy.getY(),delta);
 					
 					for(int i = 0;i<laserlauncher.slaserlist.size();i++){
 						SLaser laser = laserlauncher.slaserlist.get(i);
 						laser.move(10,delta);
 						for(int e = 0; e < map.getPlanets().size(); e++){
 							Planet mesh = map.getPlanets().get(e);
 							mesh.getShake().update(delta);
 							if(mesh.isColliding(laser)){
 								mesh.damage(laser.getPlanetDamage(), map);
 								AnimationManager.addAnimation(new Explosion(laser.getCenterX(), laser.getCenterY(), 2.5f, 100));
 								mesh.getShake().shakeObject(3, 1000);
 								laserlauncher.slaserlist.remove(laser);
 							}
 						}
 						for(PhysicalObject temp2 : map.getObjList()){
 							if(!temp2.equals(obj) && temp2.isColliding(laser)){
 								
 								AnimationManager.addAnimation(new Explosion(laser.getCenterX(), laser.getCenterY(), 2.5f, 100));
 								laserlauncher.slaserlist.remove(laser);
 							}
 						}
 						if (laser.getX() > mapwidth - 20 || laser.getY() > mapheight - 30 || laser.getX() < 0 || laser.getY() < 0){
 							laserlauncher.slaserlist.remove(laser);
 						}
 						laser = null;
 					}
 					laserlauncher = null;
 				}
 			} else if (obj.getClass() == Planet.class){
 				for(Planet mesh : map.getPlanets()){
 					if(craft.isColliding(mesh)){
 						craft.setHull(0d);
 					}
 				}
 			} else{
 				System.out.println("ERROR! Fix update in GameState");
 			}
 		}
 		
 		for(int d = 0; d < map.getDroppedItem().size(); d++){
 			DroppedItem di = map.getDroppedItem().get(d);
 			di.update(delta);
 			if(this.craft.isColliding(di)){
 				craft.addToInventory(di.getItemsInDrop());
 				di.destroy(map);
 			}
 		}
 		
 		camX = (float) ((craft.getX()+(craft.getImage().getWidth()/2))*Zoom.getZoom().scale) - (SCR_width/2);	 
 		camY = (float) ((craft.getY()+(craft.getImage().getHeight()/2))*Zoom.getZoom().scale) - (SCR_height/2);
 		
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
 		g.scale((float)Zoom.getZoom().scale,(float)Zoom.getZoom().scale);
 		
 		//Change the third paramater to control the camera's view
 		//g.rotate(camX + this.SCR_width/2, camY + this.SCR_height/2, -90);
 		
 		g.setColor(Color.red);
 		g.drawRect(0, 0, mapwidth, mapheight);
 		g.setColor(Color.yellow);
 		g.drawRect(0,0, mapwidth-5, mapheight-5);
 		stars.render(Zoom.scale(camX),Zoom.scale(camY));
 		
 		enemy.getImage().draw(enemy.getX(), enemy.getY());
 		
 		craft.getImage().draw(craft.getX(), craft.getY());
 		craft.shield.getImage().draw(craft.getShieldX(),craft.getShieldY(),1.2f);
 		//systems
 		craft.core.getImage().drawCentered(craft.core.getX()+craft.getX(),craft.core.getY()+craft.getY());
 		craft.shield.getImage().drawCentered(craft.shield.getX()+craft.getX(),craft.shield.getY()+craft.getY());
 		craft.engine.getImage().drawCentered(craft.engine.getX()+craft.getX(),craft.engine.getY()+craft.getY());
 		craft.lifesupport.getImage().drawCentered(craft.lifesupport.getX()+craft.getX(),craft.lifesupport.getY()+craft.getY());
 		
 		for(int i = 0; i < map.getPlanets().size(); i ++){
 			Planet mesh = map.getPlanets().get(i);
 			//image culling
 			if(mesh.getX()>Zoom.getZoomWidth()/2+(craft.getCenterX())||
 					mesh.getX()+(mesh.getImage().getWidth()*mesh.getScale())<craft.getCenterX()-(Zoom.getZoomWidth()/2)||
 					mesh.getY()>Zoom.getZoomHeight()/2+(craft.getCenterY())||
 					mesh.getY()+(mesh.getImage().getHeight()*mesh.getScale())<camY-(Zoom.getZoomHeight()/2)){
 				mesh = null;
 				continue;
 			}
 			/*
 			 * if(mesh.getX()-(mesh.getImage().getWidth()*mesh.getScale())>Zoom.getZoomWidth()+camX||
 					mesh.getX()+(mesh.getImage().getWidth()*mesh.getScale())<camX||
 					mesh.getY()-(mesh.getImage().getHeight()*mesh.getScale())>Zoom.getZoomHeight()+camY||
 					mesh.getY()+(mesh.getImage().getHeight()*mesh.getScale())<camY){
 				mesh = null;
 				continue;
 			}
 			 */
 			//label at top with health
 			if(mesh.getHp() < mesh.getMaxHp()){
 				if(mesh.getHp() <= 200) g.setColor(Color.red);
 				else if(mesh.getHp() <= 500) g.setColor(Color.orange);
 				else g.setColor(Color.green);
 				float gg = mesh.getHp();
 				float ff = mesh.getMaxHp();
 				g.fillRect(mesh.getX(), mesh.getY() - 20, (gg/ff) * (mesh.getShape().getWidth()), 20);
 			}
 			//drawing planet
 			mesh.getImage().draw(mesh.getX()+mesh.getShake().getDx(),mesh.getY()+mesh.getShake().getDy(),mesh.getScale());
 			mesh = null;
 		}
 		
 		for(DroppedItem mesh : map.getDroppedItem()){
 			mesh.getImage().draw(mesh.getX(), mesh.getY());
 		}
 		
 		AnimationManager.renderAnimation();
 		
 		for(LaserLauncher cannon : craft.laserlist){
 			cannon.render(g,camX,camY);
 		}
 		for(LaserLauncher cannon : enemy.laserlist){
 			cannon.render(g,camX,camY);
 		}
 		
 		//Type inverse of third paramater here to counteract (for GUI components)
 		//g.rotate(camX + this.SCR_width/2, camY + this.SCR_height/2, 90);
 		part.render();
 		shockwave.render();
 		 
 		g.scale((float)Zoom.getZoom().inverse,(float)Zoom.getZoom().inverse);
 		GUI.draw(camX,camY);
 		guihotbar.loadWeapons(camX,camY);
 		//minimap.render(g);
 		if (PAUSED) {
 	        Color trans = new Color(0f,0f,0f,0.5f);
 	        g.setColor(trans);
 	        g.fillRect(camX,camY, SCR_width, SCR_height);
 	        
 	        pausemenu.draw(pausemenux+camX,pausemenuy+camY);
 	        pausemenu.setFilter(Image.FILTER_NEAREST);
 	        resume.render(g,camX,camY);
 	        restart.render(g,camX,camY);
 	        options.render(g,camX,camY);
 	        hangar.render(g,camX,camY);
 	        menu.render(g,camX,camY);
 	        exit.render(g,camX,camY);
 	   }
 	}
 
 	public void mousePressed(int button, int x, int y){
 		x=Zoom.scale(x);
 		y=Zoom.scale(y);
 		if(!PAUSED && selected > -1) {
 			if(button == 1) {
 				craft.laserlist.get(selected).setFire(x,y,Zoom.scale(camX),Zoom.scale(camY), false);
 			} else if (button == 0){
 				craft.laserlist.get(selected).setFire(x,y,Zoom.scale(camX),Zoom.scale(camY), true);
 			}
 		}
 	}
 	
 	public void mouseWheelMoved(int change){
 		if(change <= -120 && mousewheelposition < 1){
 			mousewheelposition += 1;
 		} else if(change >= 120 && mousewheelposition > -1){
 			mousewheelposition -= 1;
 		}
 	}
 
 	public Object getTargetObject(float f, float g) {
 		/*
 		for(int e = 0;e<boxmeshlist.size();e++){
 			AABB box = new AABB(1f,1f);
 			box.update(new Vector(f,g));
 			if(CollisionLibrary.testBoxBox(boxmeshlist.get(e).boundbox, box)){
 				return this.boxmeshlist.get(e);
 			}
 		}
 		*/
 		for(Planet pl : map.getPlanets()){
 			if(pl.isContaining(f,g)){
 				return pl;
 			}
 		}
 		return null;
 		
 	}
 	
 
 
 	@Override
 	public int getID() {
 		return 2;
 	}
 
 }
