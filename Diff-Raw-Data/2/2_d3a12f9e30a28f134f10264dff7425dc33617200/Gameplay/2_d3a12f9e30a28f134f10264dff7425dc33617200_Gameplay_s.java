 package states;
 
 import main.*;
 
 import org.lwjgl.Sys;
 import org.newdawn.slick.*;
 import org.newdawn.slick.geom.Rectangle;
 import org.newdawn.slick.state.BasicGameState;
 import org.newdawn.slick.state.StateBasedGame;
 
 import buffs.ABuff;
 import buffs.TutorZone;
 
 import java.text.DecimalFormat;
 import java.util.ArrayList;
 
 public class Gameplay extends BasicGameState{
 
 	static int width = 640;
 	static int height = 480;
 	static boolean fullscreen = false;
 	static boolean showFPS = true;
 	static String title = "#1 Game NA";
 	static int fpslimit = 60;
 	public static Player player;
 	private int mouseX, mouseY;
 	private String osName;
 	private Camera camera;
 	private Image background;
 	public Particles pe;
 	public static Enemy enemy;
 	public static Logic logic;
 	public static Text text;
 	public static ABuff aBuff;
 	public static TutorZone tutorZone;
 	public Music mainMusic;
 	public static Sound hurtSound;
 	public static Sound buffSound;
 	public static StopWatch s;
 	public static DecimalFormat df;
 	public boolean ABuffOn; // is the buff on screen or not?
 	public int ABuffDelta; // time in ms since last buff collision
 	public int maxABuffDelta; // time between last buff collision and respawn
 	public boolean tutorZoneOn; // is the buff on screen or not?
 	public int tutorZoneDelta; // time in ms since last buff collision
 	public int maxTutorZoneDelta; // time between last buff collision and respawn
 
 	@Override
 	public void init(GameContainer container, StateBasedGame game)
 			throws SlickException {
 		//Load images depending on Operating System
 		osName = System.getProperty("os.name");
 		//Character
 		player = new Player("data/sprite.png");
 		//Background
		background = new Image("data/background3.jpg", false, Image.FILTER_NEAREST);
 		camera = new Camera(player);
 		pe = new Particles();
 		enemy = new Enemy();
 		aBuff = new ABuff();
 		tutorZone = new TutorZone();
 		logic = new Logic();
 		text = new Text();
 		mainMusic = new Music("data/theme.ogg");
 		hurtSound = new Sound("data/hit.ogg");
 		buffSound = new Sound("data/buff.ogg");
 		mainMusic.loop();
 		s = new StopWatch();
 		df = new DecimalFormat("0.00");
 		s.start();
 		ABuffOn = false; // buff not on screen
 		ABuffDelta = 0;
 		maxABuffDelta = 25000; // 25s
 		
 		tutorZoneOn = false; // buff not on screen
 		tutorZoneDelta = 0;
 		maxTutorZoneDelta = 5000; // 25s
 	}
 
 
 	public void input(GameContainer gc, Graphics g) throws SlickException
 	{
 		Input input = gc.getInput();
 		mouseX = input.getAbsoluteMouseX();
 		mouseY = input.getAbsoluteMouseY();
 		player.move(input);
 	}
 
 	@Override
 	public void mouseWheelMoved(int change)
 	{
 		if(change>=0)
 		{
 			camera.changeScale(true);
 		}
 		if(change<0)
 		{
 			camera.changeScale(false);
 		}
 	}
 
 	@Override
 	public void render(GameContainer container, StateBasedGame game, Graphics g)
 			throws SlickException {
 
 		//Input
 		input(container, g);
 		//Set camera scale
 		//camera.scale(g, width, height);
 		//Draw background
 		background.draw(0,0,width,height);
 		//Move and render particles
 		pe.setPositionPlayer(player.posX + player.image.getWidth()/2, player.posY + player.image.getHeight()/2);
 		//Set particles on buffs
 		if(!ABuffOn){
 			pe.setPositionABuff(-100,-100);
 		}
 		else
 		{
 			pe.setPositionABuff(aBuff.posX + aBuff.width/2, aBuff.posY + aBuff.height/2);
 		}
 		//Set particles on Tutor Zone
 		if(!tutorZoneOn){
 			pe.setPositionTutorZone(-100,-100);
 		}
 		else
 		{
 			pe.setPositionTutorZone(tutorZone.posX + tutorZone.width/2, tutorZone.posY + tutorZone.height/2);
 		}
 		//Set Background Particle Position
 		pe.setPositionBackground(width/2, -16);
 		pe.render();
 		//Draw Enemies
 		enemy.activate(g);
 		// draw ABuff only if it is on screen
 		if(ABuffOn){
 			aBuff.render(g);
 		}
 		//draw tutorZone only if it is on screen
 		if(tutorZoneOn)
 		{
 			tutorZone.render(g);
 		}
 		//Draw character
 		player.draw();
 		//Display GPA
 		logic.displayGPA(g);
 		//Display Time
 		logic.displayTime(g);
 		g.resetTransform();
 
 	}
 
 	@Override
 	public void update(GameContainer container, StateBasedGame game, int delta)
 			throws SlickException {
 		//ABuff Logic
 		logic.logic();
 		if(!ABuffOn){ // if buff is not on screen
 			ABuffDelta += delta; // add to time since last collision
 			if(ABuffDelta >= maxABuffDelta){ // check if a new buff is to be spawned
 				ABuffDelta = 0; // reset time since last buff collision
 				aBuff.spawn(); // spawn the buff on screen
 				ABuffOn = true; // buff is on screen
 			}
 		}
 		else{ // if buff is on screen
 			if(logic.collidesWithABuff()){ // if player collects buff
 				aBuff.removeABuff(); // move the buff off screen
 				ABuffOn = false; // buff is now off screen
 			}
 		}
 		//TutorZone Logic
 		if(!tutorZoneOn){ // if buff is not on screen
 			tutorZoneDelta += delta; // add to time since last collision
 			if(tutorZoneDelta >= maxTutorZoneDelta){ // check if a new buff is to be spawned
 				tutorZoneDelta = 0; // reset time since last buff collision
 				tutorZone.spawn(); // spawn the buff on screen
 				tutorZoneOn = true; // buff is on screen
 			}
 		}
 		else{ // if buff is on screen
 			if(logic.collidesWithTutorZone()){ // if player collects buff
 				tutorZone.gainGPA(); // move the buff off screen
 				//tutorZoneOn = false; // buff is now off screen
 			}
 			tutorZoneDelta += delta;
 			if(tutorZoneDelta >= 3000)
 			{
 				tutorZoneDelta = 0;
 				tutorZone.removeTutorZone();
 				tutorZone.spawn();
 			}
 		}
 
 
 		pe.update(delta);
 		if(logic.gpa <= 0)
 		{
 			container.exit();
 			gameEnd();
 		}
 
 	}
 	public void gameEnd()
 	{
 		s.stop();
 		System.out.println("You lasted " + s.getElapsedTimeSecs() + " seconds.");
 	}
 
 	@Override
 	public int getID() {
 		// TODO Auto-generated method stub
 		return 1;
 	}
 
 }
