 package edu.calpoly.csc.pulseman;
 
 import edu.calpoly.csc.pulseman.Main.GameState;
 import edu.calpoly.csc.pulseman.World.LevelLoadListener;
 import edu.calpoly.csc.pulseman.util.AtomicFloat;
 
 import org.newdawn.slick.Animation;
 import org.newdawn.slick.Color;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Image;
 import org.newdawn.slick.KeyListener;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.Input;
 import org.newdawn.slick.Sound;
 
 import edu.calpoly.csc.pulseman.gameobject.Collidable;
 import edu.calpoly.csc.pulseman.gameobject.Enemy;
 import edu.calpoly.csc.pulseman.gameobject.GameObject;
 import edu.calpoly.csc.pulseman.gameobject.Goal;
 import edu.calpoly.csc.pulseman.gameobject.KillingObstacle;
 import edu.calpoly.csc.pulseman.gameobject.Player;
 import edu.calpoly.csc.pulseman.gameobject.Tile;
 
 public class GameScreen implements GameInterface, KeyListener
 {
 	public static final float DIASTOLE_DECAY_VALUE = 0.99f;
 	public static final float SYSTOLE_DECAY_VALUE = 0.95f;
 	public static final float MAX_MULT = 20.0f;
 	public static final float MAX_SPEEDUP = 10.0f;
 	public static final float TIME_INCREASE = 1.5f;
 
 	private static final float INITIAL_TIME_MULT = 2.0f;
 	private static final float FADE_THRESHOLD = 0.5f;
 
 	private static final String DESERT = "desert";
 	private static final String FLATLANDS = "flatlands";
 	private static final String PLATEAU = "plateau";
 	private static final String CANYON = "canyon";
 
 	public static final String[] levelToScheme =
 	{ DESERT, DESERT, FLATLANDS, PLATEAU, CANYON, CANYON, FLATLANDS, PLATEAU };
 
 	private boolean pulseEnabled = true;
 	private volatile boolean isDiastole = false;
 	private volatile float decayValue = DIASTOLE_DECAY_VALUE;
 
 	private AtomicFloat timeMult;
 	private Heart heart;
 	private Sound diastole, systole;
 
 	private float lastSpeedMultiplier = 0.0f;
 
 	public GameScreen()
 	{
 		timeMult = new AtomicFloat(0.0f);
 	}
 
	public void resetHeart()
	{
		isDiastole = false;
		decayValue = DIASTOLE_DECAY_VALUE;
	}

 	@Override
 	public void render(GameContainer gc, Graphics g)
 	{
 		World.getWorld().render(gc, g);
 
 		g.resetTransform();
 
 		float alpha = (FADE_THRESHOLD - lastSpeedMultiplier) / FADE_THRESHOLD;
 		if(alpha < 0.0f)
 		{
 			alpha = 0.0f;
 		}

 		MessageHandler.sendMessage("" + (1.0f - alpha));
 
 		g.setColor(new org.newdawn.slick.Color(0.2f, 0.2f, 0.2f, alpha));
 		g.fillRect(0.0f, 0.0f, gc.getScreenWidth(), gc.getScreenHeight());
 
		// heart.render(gc, g);
 	}
 
 	@Override
 	public void init(GameContainer gc) throws SlickException
 	{
 		diastole = new Sound("res/sounds/diastole.wav");
 		systole = new Sound("res/sounds/systole.wav");

 		Image[] cactus =
 		{ new Image("res/cactus/cactus1.png"), new Image("res/cactus/cactus2.png"), new Image("res/cactus/cactus3.png"), new Image("res/cactus/cactus4.png"), };
 		Animation cactusAnim = new Animation(cactus, 5000);
 		Image[] miniCactus =
 		{ new Image("res/cactus/miniCactus1.png"), new Image("res/cactus/miniCactus2.png"), new Image("res/cactus/miniCactus3.png"), new Image("res/cactus/miniCactus4.png") };
 		Animation miniCactusAnim = new Animation(miniCactus, 5000);
 
 		heart = new Heart(new Image("res/heart.png"));
 
 		Image sky = new Image("res/sky.png");
 		Image layer1 = new Image("res/mountains.png");
 		Image layer2 = new Image("res/hills.png");
 		Image layer3 = new Image("res/flatlands.png");
 		Animation[] desertProps =
 		{ cactusAnim, miniCactusAnim };
 		Image[] desertBG =
 		{ sky, layer1, layer2, layer3 };
 
 		SchemeLoader.createScheme(DESERT, desertProps, desertBG, new Color(253.0f / 255.0f, 210.0f / 255.0f, 78.0f / 255.0f));
 		Image[] flatLandBG =
 		{ new Image("res/bg/24.png"), new Image("res/bg/23.png"), new Image("res/bg/22.png"), new Image("res/bg/21.png") };
 
 		Image[] grass =
 		{ new Image("res/Grass/grass1.png"), new Image("res/Grass/grass2.png"), new Image("res/Grass/grass3.png"), new Image("res/Grass/grass4.png"), };
 		Image[] tree =
 		{ new Image("res/tree/tree1.png"), new Image("res/tree/tree2.png"), new Image("res/tree/tree3.png"), new Image("res/tree/tree4.png"), };
 
 		Animation treeAnim = new Animation(tree, 5000);
 		Animation grassAnim = new Animation(grass, 500);
 		Animation[] grassLandProps =
 		{ grassAnim, treeAnim };
 		SchemeLoader.createScheme(FLATLANDS, grassLandProps, flatLandBG, new Color(44.0f / 255.0f, 24.0f / 255.0f, 12.0f / 255.0f));
 
 		Image[] plateauBG =
 		{ new Image("res/bg/34.png"), new Image("res/bg/33.png"), new Image("res/bg/32.png"), new Image("res/bg/31.png") };
 		SchemeLoader.createScheme(PLATEAU, grassLandProps, plateauBG, new Color(44.0f / 255.0f, 24.0f / 255.0f, 12.0f / 255.0f));
 
 		Image[] canyonBG =
 		{ new Image("res/bg/44.png"), new Image("res/bg/43.png"), new Image("res/bg/42.png"), new Image("res/bg/41.png") };
 		SchemeLoader.createScheme(CANYON, desertProps, canyonBG, new Color(44.0f / 255.0f, 24.0f / 255.0f, 12.0f / 255.0f));
 
 		Image[] monkWalk =
 		{ new Image("res/Player/MonkWalk1.png"), new Image("res/Player/MonkWalk2.png"), new Image("res/Player/MonkWalk3.png"), new Image("res/Player/MonkWalk4.png") };
 		Image[] enemyWalk =
 		{ new Image("res/Goomba/GoombaFrame1.png"), new Image("res/Goomba/GoombaFrame2.png"), new Image("res/Goomba/GoombaFrame3.png"), new Image("res/Goomba/GoombaFrame4.png") };
 		Image[] enemy2Walk =
 		{ new Image("res/Goomba/GoombaImmuneFrame1.png"), new Image("res/Goomba/GoombaImmuneFrame2.png"), new Image("res/Goomba/GoombaImmuneFrame3.png"), new Image("res/Goomba/GoombaImmuneFrame4.png") };
 		Player.init(new Animation(monkWalk, 200), new Image("res/Player/MonkStand.png"), new Image("res/Player/MonkJump5.png"));
 		Enemy.init(new Animation(enemyWalk, 200), new Animation(enemy2Walk, 200));
 		Goal.init(new Image("res/portal.png"));
 		Collidable.init(new Image("res/orb.png"));
 
 		KillingObstacle.init("res/spike.png");
 		Tile.init(new Image("res/brick.png"));
 
 		gc.getInput().addKeyListener(this);
 
 		World.getWorld().addLevelLoadListener(new LevelLoadListener()
 		{
 			@Override
 			public void onLevelLoad()
 			{
 				timeMult.set(INITIAL_TIME_MULT);
 			}
 		});
 
 		World.getWorld().nextLevel();
 		World.getWorld().loadLastLevel();
 	}
 
 	@Override
 	public void update(GameContainer gc, int dt)
 	{
 		timeMult.set(timeMult.get() * decayValue);
 		int affectedDt;
 		if(pulseEnabled)
 		{
 			lastSpeedMultiplier = MAX_SPEEDUP * Math.min(timeMult.get(), MAX_MULT) / MAX_MULT;
 			affectedDt = (int)(dt * lastSpeedMultiplier + 0.5f);
 
 			if(affectedDt == 0)
 			{
 				Main.setState(GameState.GAMEOVER);
 			}
 		}
 		else
 		{
 			affectedDt = dt;
 		}
 
 		World.getWorld().update(gc, dt, affectedDt);
 
 		heart.update(gc, affectedDt);
 	}
 
 	@Override
 	public void inputEnded()
 	{
 	}
 
 	@Override
 	public void inputStarted()
 	{
 	}
 
 	@Override
 	public boolean isAcceptingInput()
 	{
 		return true;
 	}
 
 	@Override
 	public void setInput(Input arg0)
 	{
 	}
 
 	@Override
 	public void keyPressed(int key, char c)
 	{
 		if(key == Input.KEY_ENTER)
 		{
 			playerTwoTap();
 		}
 
 		if(key == Input.KEY_ESCAPE)
 		{
 			Main.reset();
 		}
 		if(key == Input.KEY_L)
 		{
 			World.getWorld().nextLevel();
 		}
 	}

 	// Add support for diastole / systole
 	public void playerTwoTap()
 	{
 		if(Main.getState() != Main.GameState.GAME)
 		{
 			return;
 		}

 		isDiastole = !isDiastole;
 		if(isDiastole)
 		{
 			diastole.play();
 			decayValue = SYSTOLE_DECAY_VALUE;
 		}
 		else
 		{
 			systole.play();
 			decayValue = DIASTOLE_DECAY_VALUE;
 		}
 		timeMult.set(timeMult.get() + TIME_INCREASE);
 	}
 
 	@Override
 	public void keyReleased(int arg0, char arg1)
 	{
 	}
 
 	private class Heart implements GameObject
 	{
 		private static final float SCALE_RATE = 0.00055f;
 
 		public Image image;
 		public float scale;
 		public int beat;
 
 		public Heart(Image image)
 		{
 			this.image = image;
 			scale = 1;
 			beat = 1;
 		}
 
 		@Override
 		public void update(GameContainer gc, int delta)
 		{
 			if(scale < 0.8)
 				beat = 1;
 			if(scale > 1.0)
 				beat = 0;
 			if(beat == 1)
 			{
 				scale += SCALE_RATE * delta;
 			}
 			else
 				scale -= SCALE_RATE * delta;
 		}
 
 		@Override
 		public void render(GameContainer gc, Graphics g)
 		{
 			image.getScaledCopy(scale).drawCentered(Main.getScreenWidth() - image.getWidth() / 2, image.getHeight() / 2);
 		}
 
 		@Override
 		public boolean isAffectedByPulse()
 		{
 			return true;
 		}
 	}
 
 }
