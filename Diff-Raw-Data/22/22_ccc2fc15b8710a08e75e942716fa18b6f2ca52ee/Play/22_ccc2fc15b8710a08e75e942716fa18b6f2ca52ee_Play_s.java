 package game;
 
 import java.io.File;
 
 import org.lwjgl.util.vector.Vector2f;
 import org.newdawn.slick.*;
 import org.newdawn.slick.state.*;
 import org.newdawn.slick.particles.ConfigurableEmitter;
 import org.newdawn.slick.particles.ParticleEmitter;
 import org.newdawn.slick.particles.ParticleIO;
 import org.newdawn.slick.particles.ParticleSystem;
 
 public class Play extends BasicGameState {
 	private static final int EMITTER_BOOM = 0;
 	private static final int EMITTER_ASTEROID = 1;
 	private static final int NUM_EMITTERS = 15;
 	private static final int FIRING_DELAY = 120;
 	private boolean soundEnabled = true;
 	private int id;
 	// private Movable player = new Movable(400.0F, 300.0F, new Rectangle());
 
 	float tankx = 400;
 	float tanky = 300;
 	float scale = 1f;
 
 	// asteroids
 	int numberOfAsteroids = 10;
 	private Asteroid[] asts;
 	private Asteroid[] ast = new Asteroid[20];
 	boolean quit = false;
 
 	// Images to be used.
 	private Image tank;
 	private Tank player;
 	private CollidableRenderableObject land;
 
 	// Sounds to be used
 	// private Sound soundHit1;
 	// private Sound soundHit2;
 	// private Sound soundShoot;
 	// private Sound soundExplosion;
 	// When we actually have a winning situation
 	// private Sound soundWin;
 
 	// Particles
 	private ConfigurableEmitter[] boom = new ConfigurableEmitter[15];
 	private ConfigurableEmitter[] asteroidboom = new ConfigurableEmitter[7];
 	private ParticleSystem particleSystem;
 	private int boomEmitterCount = 0;
 	private int asteroidEmitterCount = 0;
 
 	public Play(int State) {
 
 	}
 
 	public void init(GameContainer gc, StateBasedGame sbg)
 			throws SlickException {
 		ConfigurableEmitter boomEmitter = null;
 		ConfigurableEmitter asteroidEmitter = null;
 
 		try {
 
 			this.land = new CollidableRenderableObject(
 					"res/messier81_800x600.jpg",
 					CollidableRenderableObject.Physics.Rectangular);
 			this.land.invert = true;
 			this.land.resolve = false;
 			this.land.SetPosition(0f, 0f);
 			this.tank = new Image("res/Sherman Tank Sprite.png");
 			player = new Tank();// new CollidableRenderableObject(
 			// "res/Sherman Tank Sprite.png",
 			// CollidableRenderableObject.Physics.Rectangular);
 			// land.position = new Vector2f(2, 5);
 
 			// this.soundHit1 = new Sound("res/hit.ogg");
 			// this.soundHit2 = new Sound("res/hit3.ogg");
 			// this.soundShoot = new Sound("res/shoot.ogg");
 			// this.soundExplosion = new Sound("res/explosion3.ogg");
 
 			// boomEmitter = ParticleIO.loadEmitter("res/boom.xml");
 			// asteroidEmitter = ParticleIO.loadEmitter("res/asteroidboom.xml");
 			// this.particleSystem = new ParticleSystem("res/particle2.png");
 			// this.particleSystem.setRemoveCompletedEmitters(true);
 			// this.particleSystem.setUsePoints(false);
 
 		} catch (Exception se) {
 			se.printStackTrace();
 		}
 
 		asts = new Asteroid[numberOfAsteroids];
 		/*for (int i = 0; i < ast.length; i++) {
 			ast[i] = new Asteroid();
 		}*/
 		asts = new Asteroid[numberOfAsteroids];
 		for (int i = 0; i < numberOfAsteroids; i++) {
 			asts[i] = new Asteroid();
 			// asts[i].Explode();
 		}
 
 		for (int i = 0; i < 15; i++) {
 			// this.boom[i] = boomEmitter.duplicate();
 		}
 
 		for (int i = 0; i < 7; i++) {
 			// this.asteroidboom[i] = asteroidEmitter.duplicate();
 		}
 
 	}
 
 	public static float RandMinus1To1() {
 		return (float) ((Math.random() - 0.5) * 2f);
 	}
 
 	public void render(GameContainer gc, StateBasedGame sbg, Graphics g)
 			throws SlickException {
 		if (RenderableObject.rendObjects != null)
 			for (RenderableObject ro : RenderableObject.rendObjects) {
 				ro.Draw();
 			}
 
 		// for (int i = 0; i < 10; i++) {asts[i].Draw();}
 		// plane.draw(tankx, tanky, scale);
 
 		// for when the player presses escape
 		if (quit == true) {
 			g.drawString("Resume (R)", 390, 100);
 			g.drawString("Main Menu (M)", 390, 150);
 			g.drawString("Quit Game (Q)", 390, 200);
 			if (quit == false) {
 				g.clear();
 			}
 		}
 
 		
 		//THE EXPLOSION PARTICLES
 		/*
 		 * try { File xmlFile = new File("res/explode.xml"); boom =
 		 * ParticleIO.loadEmitter(xmlFile); boom.setPosition(player.getX,
 		 * player.getY ); } catch (Exception e) {
 		 * System.out.println("Exception: " +e.getMessage());
 		 * e.printStackTrace(); System.exit(0); }
 		 */
 		
 		/*
 		 * try { File xmlFile = new File("res/explode.xml"); boom =
 		 * ParticleIO.loadEmitter(xmlFile); boom.setPosition(ast.getX,
 		 * player.getY ); } catch (Exception e) {
 		 * System.out.println("Exception: " +e.getMessage());
 		 * e.printStackTrace(); System.exit(0); }
 		 */
 	}
 
 	public void update(GameContainer gc, StateBasedGame sbg, int delta)
 			throws SlickException {
 		// if(quit)
 		// return;
 		// if (!player.isDead() && !player.hasWon()) {
 
 		Input input = gc.getInput();
 		float del = (float) delta / 1000f;
 		if (input.isKeyDown(Input.KEY_A)) {
			player.RotateTank(-90f);
 			tank.rotate(-0.2f * delta);
 		}
 
 		if (input.isKeyDown(Input.KEY_D)) {
			player.RotateTank(90f);
 			tank.rotate(0.2f * delta);
 		}
 
 		if (input.isKeyDown(Input.KEY_W)) {
 			player.MoveTank(0.3f);
 			float hip = 0.4f * delta;
 
 			float rotation = tank.getRotation();
 
 			tankx += hip * Math.sin(Math.toRadians(rotation));
 			tanky -= hip * Math.cos(Math.toRadians(rotation));
 		}
 
 		if (input.isKeyDown(Input.KEY_S)) {
 			player.MoveTank(-0.3f);
 			float hip = 0.4f * delta;
 
 			float rotation = tank.getRotation();
 
 			tankx -= hip * Math.sin(Math.toRadians(rotation));
 			tanky += hip * Math.cos(Math.toRadians(rotation));
 		}
 		if (input.isKeyDown(Input.KEY_SPACE)) {
 			player.Shoot();
 		}
 
 		if (input.isKeyDown(Input.KEY_2)) {
 			scale += (scale >= 5.0f) ? 0 : 0.1f;
 			tank.setCenterOfRotation(tank.getWidth() / 2.0f * scale,
 					tank.getHeight() / 2.0f * scale);
 		}
 		if (input.isKeyDown(Input.KEY_1)) {
 			scale -= (scale <= 1.0f) ? 0 : 0.1f;
 			tank.setCenterOfRotation(tank.getWidth() / 2.0f * scale,
 					tank.getHeight() / 2.0f * scale);
 		}
 
 		// escape to open ingame menu
 		if (input.isKeyDown(Input.KEY_ESCAPE)) {
 			quit = true;
 		}
 
 		// when the player hits escape
 		if (quit == true) {
 			if (input.isKeyDown(Input.KEY_R)) {
 				quit = false;
 			}
 
 			if (input.isKeyDown(Input.KEY_M)) {
 				sbg.enterState(0);
 				try {
 					Thread.sleep(250);
 				} catch (InterruptedException e) {
 					e.printStackTrace();
 				}
 			}
 
 			if (input.isKeyDown(Input.KEY_Q)) {
 				System.exit(0);
 			}
 		}
 
 		// as game runs, moves asteroid across screen
 		// change += 1 to something to do with dy, dx
 		/*for (int i = 0; i < ast.length; i++) {
 			ast[i].x += 1;
 			ast[i].y += 1;
 		}*/
 
 		if (RenderableObject.rendObjects != null)
 			for (RenderableObject ro : RenderableObject.rendObjects) {
 				ro.Update(del);
 			}
 
 		CollidableRenderableObject.CheckCollisions();
 		// }
 		// if (player.isDead()) {
 		// player.isDestroyed();
 		// sbg.enterState(0);
 		// }
 	}
 
 	public int getID() {
 		return 1;
 	}
 
 	public ConfigurableEmitter getEmitter(int type) {
 		if (type == 0) {
 			this.boomEmitterCount = ((this.boomEmitterCount + 1) % this.boom.length);
 			return this.boom[this.boomEmitterCount];
 		}
 		if (type == 1) {
 			this.asteroidEmitterCount = ((this.asteroidEmitterCount + 1) % this.asteroidboom.length);
 			return this.asteroidboom[this.asteroidEmitterCount];
 		}
 		return null;
 	}
 }
