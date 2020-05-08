 package edu.calpoly.csc.pulseman;
 
 import edu.calpoly.csc.pulseman.World.LevelLoadListener;
 import edu.calpoly.csc.pulseman.util.AtomicFloat;
 
 import org.newdawn.slick.Animation;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Image;
 import org.newdawn.slick.KeyListener;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.Input;
 
 import edu.calpoly.csc.pulseman.gameobject.Collidable;
 import edu.calpoly.csc.pulseman.gameobject.Enemy;
 import edu.calpoly.csc.pulseman.gameobject.GameObject;
 import edu.calpoly.csc.pulseman.gameobject.Goal;
 import edu.calpoly.csc.pulseman.gameobject.KillingObstacle;
 import edu.calpoly.csc.pulseman.gameobject.MovingTile;
 import edu.calpoly.csc.pulseman.gameobject.Player;
 import edu.calpoly.csc.pulseman.gameobject.Tile;
 
 public class GameScreen implements GameInterface, KeyListener
 {
 	public static float DECAY_VALUE = .99f;
 	public static float MAX_MULT = 20.0f;
 	public static float MAX_SPEEDUP = 10.0f;
 
 	private static final int MS_PER_FRAME = 300;
 
 	private static final String DESERT = "desert";
 	private static final String MOUNTAIN = "mountain";
 	private static final String FOREST = "forest";
 
 	public static final String[] levelToScheme =
 	{ DESERT, DESERT, DESERT };
 
 	private boolean pulseEnabled = false;
 
 	private AtomicFloat timeMult;
 	private Heart heart;
 
 	public GameScreen()
 	{
 		timeMult = new AtomicFloat(0.0f);
 	}
 
 	@Override
 	public void render(GameContainer gc, Graphics g)
 	{
 		World.getWorld().render(gc, g);
 
 		g.resetTransform();
 
 		heart.render(gc, g);
 	}
 
 	@Override
 	public void init(GameContainer gc) throws SlickException
 	{
 		Image[] cactus =
 		{ new Image("res/cactus/cactus1.png"), new Image("res/cactus/cactus2.png"), new Image("res/cactus/cactus3.png"), new Image("res/cactus/cactus4.png"), };
		Animation cactusAnim = new Animation(cactus, 300);
 
 		heart = new Heart(new Image("res/heart.png"));
 
 		Image sky = new Image("res/sky.png");
 		Image layer1 = new Image("res/mountains.png");
 		Image layer2 = new Image("res/hills.png");
 		Image layer3 = new Image("res/flatlands.png");
 		Animation[] desertProps =
 		{ cactusAnim };
 		Image[] desertBG =
 		{ sky, layer1, layer2, layer3 };
 
 		SchemeLoader.createScheme(DESERT, desertProps, desertBG);
 
 		Image[] monkWalk =
 		{ new Image("res/Player/MonkWalk1.png"), new Image("res/Player/MonkWalk2.png"), new Image("res/Player/MonkWalk3.png"), new Image("res/Player/MonkWalk4.png") };
 		Image[] enemyWalk =
 		{ new Image("res/Goomba/GoombaFrame1.png"), new Image("res/Goomba/GoombaFrame2.png"), new Image("res/Goomba/GoombaFrame3.png"), new Image("res/Goomba/GoombaFrame4.png") };
 		Player.init(new Animation(monkWalk, 300), new Image("res/Player/MonkStand.png"), new Image("res/Player/MonkJump5.png"));
 		Enemy.init(new Animation(enemyWalk, 300));
 		Goal.init(new Image("res/portal.png"));
 		Collidable.init(new Image("res/orb.png"));
 
 		KillingObstacle.init("res/spike.png");
 		Tile.init(new Image("res/brick.png"));
 		World.getWorld().nextLevel();
 		gc.getInput().addKeyListener(this);
 
 		World.getWorld().addLevelLoadListener(new LevelLoadListener()
 		{
 			@Override
 			public void onLevelLoad()
 			{
 				timeMult.set(0.0f);
 			}
 		});
 	}
 
 	@Override
 	public void update(GameContainer gc, int dt)
 	{
 		timeMult.set(timeMult.get() * DECAY_VALUE);
 		int affectedDt;
 		if(pulseEnabled)
 		{
 			affectedDt = (int)((float)dt * MAX_SPEEDUP * Math.min(timeMult.get(), MAX_MULT) / MAX_MULT);
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
 		if(key == Input.KEY_1)
 		{
 			playerTwoTap();
 		}
 		if(key == Input.KEY_0)
 		{
 			System.out.println(pulseEnabled ? "Pulse is enabled" : "Pulse disabled");
 			pulseEnabled = !pulseEnabled;
 		}
 		if(key == Input.KEY_ESCAPE) 
 		{
 			Main.reset();
 		}
 		if(key == Input.KEY_R) 
 		{
 			MAX_MULT += .5f;
 			System.out.println("Max mult:" + MAX_MULT);
 		}
 		if(key == Input.KEY_F) 
 		{
 			MAX_MULT -= .5f;
 			System.out.println("Max mult:" + MAX_MULT);
 		}
 		if(key == Input.KEY_T) 
 		{
 			MAX_SPEEDUP += .5f;
 			System.out.println("Max speed up:" + MAX_SPEEDUP);
 		}
 		if(key == Input.KEY_G) 
 		{
 			MAX_SPEEDUP -= .5f;
 			System.out.println("Max speed up:" + MAX_SPEEDUP);
 		}
 		if(key == Input.KEY_Y) 
 		{
 			DECAY_VALUE += .05f;
 			System.out.println("Decay value:" + DECAY_VALUE);
 		}
 		if(key == Input.KEY_H) 
 		{
 			DECAY_VALUE -= .05f;
 			System.out.println("Decay value:" + DECAY_VALUE);
 		}
 	}
 
 	public void playerTwoTap()
 	{
 		timeMult.set(timeMult.get() + 1.0f);
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
