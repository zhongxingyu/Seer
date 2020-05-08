 package essentials;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.util.Scanner;
 
 import org.lwjgl.LWJGLUtil;
 import org.newdawn.slick.AppGameContainer;
 import org.newdawn.slick.Color;
 import org.newdawn.slick.Game;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Input;
 import org.newdawn.slick.SlickException;
 
 public class Main implements Game {
 	private static final String GAME_TITLE = "Pixel Zombies";
 	private static boolean fullscreen;
 	private static int height;
 	private static int width;
 	private float cameraX;
 	private float cameraY;
 	private Manager entity = new Manager();
 	private boolean started = false;
 	private DayNightCycle day;
 	private Font font;
 	private Button[] button = new Button[3];
 	private TimeKeeper timekeeper = new TimeKeeper();
 	public static void main(String[] args){
 		System.setProperty("org.lwjgl.librarypath", new File(new File(System.getProperty("user.dir"), "native"), LWJGLUtil.getPlatformName()).getAbsolutePath());
 		System.setProperty("net.java.games.input.librarypath", System.getProperty("org.lwjgl.librarypath"));
 		try {
 			Scanner scanner = new Scanner(new File(System.getProperty("user.dir"), "Options.ini"));
 			scanner.nextLine();
 			fullscreen = Boolean.parseBoolean(scanner.nextLine());
 			System.out.println(fullscreen);
 		} catch (FileNotFoundException e1) {
 			e1.printStackTrace();
 		}
 		try {
 			AppGameContainer agc = new AppGameContainer(new Main());
 			if(fullscreen)
 				agc.setDisplayMode(agc.getScreenWidth(), agc.getScreenHeight(), true);
 			if(!fullscreen)
 				agc.setDisplayMode((int)(agc.getScreenWidth() * 0.8), (int)(agc.getScreenHeight() * 0.8), false);
 			height = agc.getHeight();
 			width = agc.getWidth();
 			agc.start();
 		} catch (SlickException e) {
 			e.printStackTrace();
 		}
 	}
 	public boolean closeRequested(){
 		return !fullscreen;
 	}
 	public String getTitle(){
 		return GAME_TITLE;
 	}
 	public void init(GameContainer gc) throws SlickException {
 		gc.setTargetFrameRate(60);
 		gc.setMinimumLogicUpdateInterval(50);
 		gc.setMaximumLogicUpdateInterval(50);
 		gc.setVSync(true);
 	    font = new Font();
 	    button[0] = new Button((int)(-80 + cameraX), (int)(height / 2 - 205 + cameraY), font.getLength("resume", 1.75F), (int) (1.75 * 20));
 	    button[1] = new Button((int)(-145 + cameraX), (int)(height / 2 - 255 + cameraY), font.getLength("quit to menu", 1.75F) + 4, (int) (1.75 * 20));
 	    button[2] = new Button((int)(-172 + cameraX), (int)(height / 2 - 305 + cameraY), font.getLength("quit to desktop", 1.75F), (int) (1.75 * 20));
 	    entity.loop(0, 0);
 	}
 	public void render(GameContainer gc, Graphics g) throws SlickException {
 		g.translate(width / 2 - cameraX, height / 2 - cameraY);
 	    if(!started){
 	    	g.setBackground(Color.black);
 	    	font.drawString("press space to begin", -320, (height / 2) * 0.85F, Color.red, 2.5F);
 	    }
 	    Input input = gc.getInput();
 	    if(started){
 	    	g.setBackground(new Color(0, day.getGreen(), 0));
 	    	entity.render();
 		    font.drawString("zombies killed:  " + entity.getKilledZombies(), ((-width / 2) + cameraX) + (width / 2 / 30), ((height / 2) * 0.6225F) + cameraY, Color.white, 0.9F);
 		    font.drawString("bullets:  " + entity.getBullets(), ((-width / 2) + cameraX) + (width / 2 / 30), ((height / 2) * 0.7F) + cameraY, Color.white, 0.9F);
 		    if(entity.hasGun())
 		    	font.drawString("has gun", ((-width / 2) + cameraX) + (width / 2 / 30), ((height / 2) * 0.775F) + cameraY, Color.white, 0.9F);
 		    if(!entity.hasGun())
 		    	font.drawString("does not have gun", ((-width / 2) + cameraX) + (width / 2 / 30), ((height / 2) * 0.775F) + cameraY, Color.white, 0.9F);
 		    if(entity.hasKnife())
 		    	font.drawString("has knife", ((-width / 2) + cameraX) + (width / 2 / 30), ((height / 2) * 0.85F) + cameraY, Color.white, 0.9F);
 		    if(!entity.hasKnife())
 		    	font.drawString("does not have knife", ((-width / 2) + cameraX) + (width / 2 / 30), ((height / 2) * 0.85F) + cameraY, Color.white, 0.9F);
 		    font.drawString("Days:  " + day.days, ((-width / 2) + cameraX) + (width / 2 / 30), (float) ((height / 2) * 0.925F) + cameraY, Color.white, 0.9F);
 		    if(gc.isPaused()){
 		    	Color color = new Color(0, 0, 0, 255 / 2);
 		    	g.setColor(color);
 		    	g.fillRect(-width / 2 + cameraX, -height / 2 + cameraY, width, height);
 	    	    button[0] = new Button((int)(-80 + cameraX), (int)(-height / 2 + 198 + cameraY), font.getLength("resume", 1.75F), (int) (1.75 * 20));
 	    	    button[1] = new Button((int)(-145 + cameraX), (int)(-height / 2 + 248 + cameraY), font.getLength("quit to menu", 1.75F) + 4, (int) (1.75 * 20));
 	    	    button[2] = new Button((int)(-172 + cameraX), (int)(-height / 2 + 298 + cameraY), font.getLength("quit to desktop", 1.75F), (int) (1.75 * 20));
 		    	for(int i = 0; i < 3; i++){
 		    		mouseLogic(gc, input);
 		    		button[i].draw();
 		    	}
 		    	font.drawString("menu", (int) (-69 + cameraX), (int) (-height / 2 + 50 + cameraY), Color.white, 2.0F);
 		    	font.drawString("resume", (int)(-85 + cameraX), (int)(-height / 2 + 200 + cameraY), Color.white, 1.75F);
 		    	font.drawString("quit to menu", (int)(-150 + cameraX), (int)(-height / 2 + 250 + cameraY), Color.white, 1.75F);
 		    	font.drawString("quit to desktop", (int)(-174 + cameraX), (int)(-height / 2 + 300 + cameraY), Color.white, 1.75F);
 		    }
 		    if(!entity.isPlayerAlive())
 		    	font.drawString("YOU DIED", (-font.getLength("YOU DIED", 5.0F) / 2) + cameraX, cameraY, Color.cyan, 5.0F);
 		}
 	}
 	public void update(GameContainer gc, int g) throws SlickException {
 		Input input = gc.getInput();
 		if(!gc.isPaused()){
 			if(started)
 				entity.loop(translate(input.getAbsoluteMouseX() + cameraX, true), translate(input.getAbsoluteMouseY() + cameraY, false));
 			if(started){
 			    if(input.isKeyDown(Input.KEY_ESCAPE)){
 			    	if(!gc.isPaused()){
 			    		gc.pause();
 			    		day.pause();
 			    	}
 			    }
 			    if(input.isKeyDown(Input.KEY_W) || input.isKeyDown(Input.KEY_UP)){
 			    	entity.movePlayer(Directions.UP);
 			    	cameraY = entity.idealCameraY();
 			    }
 			    if(input.isKeyDown(Input.KEY_S) || input.isKeyDown(Input.KEY_DOWN)){
 			    	entity.movePlayer(Directions.DOWN);
 			    	cameraY = entity.idealCameraY();
 			    }
 			    if(input.isKeyDown(Input.KEY_A) || input.isKeyDown(Input.KEY_LEFT)){
 			    	entity.movePlayer(Directions.LEFT);
 			    	cameraX = entity.idealCameraX();
 			    }
 			    if(input.isKeyDown(Input.KEY_D) || input.isKeyDown(Input.KEY_RIGHT)){
 			    	entity.movePlayer(Directions.RIGHT);
 			    	cameraX = entity.idealCameraX();
 			    }
 			    mouseLogic(gc, input);
 			}
 			if(!started){
 				if(input.isKeyDown(Input.KEY_SPACE)){
 					started = true;
 					day = new DayNightCycle();
 				}
 			}
 		}
 		else {
 			mouseLogic(gc, input);
 		}
 	}
 	private float translate(float location, boolean isX){
 		if(isX){
 			location -= width / 2;
 		}
 		if(!isX){
 			location -= height / 2;
 		}
 		return location;
 	}
 	private void mouseLogic(GameContainer gc, Input input){
 		if(!gc.isPaused() && started){
 			if(input.isMouseButtonDown(0)){
 				if(timekeeper.timeDifference() >= 500){
 					if(entity != null)
 						entity.createNew(ID.MOVINGBULLET, translate(input.getAbsoluteMouseX() + cameraX, true), translate(input.getAbsoluteMouseY() + cameraY, false));
 					timekeeper.start();
 				}
 			}
 		}
 		if(gc.isPaused()){
 	    	for(int i = 0; i < 3; i++){
 	    		if(button[i].isOver((int)translate(input.getAbsoluteMouseX() + cameraX, true), (int)translate(input.getAbsoluteMouseY() + cameraY, false))){
 	    			button[i].color = 0.8F;
 	    			if(input.isMouseButtonDown(0)){
 	    				switch (i){
 		    				case 0:
 		    			    	gc.resume();
 		    			    	day.unPause();
 		    			    	break;
 		    				case 1:
 		    					gc.resume();
 		    			    	cameraX = 0;
 		    			    	cameraY = 0;
 		    					entity = new Manager();
 		    					day = new DayNightCycle();
 		    					started = false;
 		    					try {
 									gc.reinit();
 								} catch (SlickException e) {
 									e.printStackTrace();
 								}
 		    					break;
 		    				case 2:
 		    					gc.exit();
 		    					break;
 	    				}
 						timekeeper.start();
 	    			}
 	    		}
 	    		if(!button[i].isOver((int)translate(input.getAbsoluteMouseX() + cameraX, true), (int)translate(input.getAbsoluteMouseY() + cameraY, false))){
 	    			button[i].color = 0.6F;
 	    		}
 	    	}
 		}
 	}
 }
