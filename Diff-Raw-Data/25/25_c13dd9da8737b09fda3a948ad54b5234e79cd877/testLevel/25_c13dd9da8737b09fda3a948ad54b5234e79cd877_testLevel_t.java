 package level;
 
 import java.awt.Color;
 import java.awt.geom.AffineTransform;
 import java.awt.image.AffineTransformOp;
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.util.Random;
 
 import entity.Animations;
 import entity.Entity;
 import entity.Grenade;
 import entity.Rocket;
 import entity.ScoreBubble;
 import entity.Shell;
 import entity.Tank;
 import entity.weapon.Rocketlauncher;
import gfx.ResourceManager;
 
 import inputhandler.InputHandler;
 import Networking.NetworkObject;
 import baseGame.BaseGame;
 import baseGame.testGame;
 import baseGame.Rendering.RGBImage;
 import baseGame.Rendering.Renderer;
 import baseGame.animations.Animation;
 import baseGame.animations.AnimationFactory;
 
 public class testLevel extends BasicLevel {
 
 	private RGBImage tank1;
 	private RGBImage tank2;
 	private RGBImage muzzle1;
 	private RGBImage crossHair1;
 	private RGBImage crossHair2;
 
 	private RGBImage shell;
 	private RGBImage grenade;
 	private RGBImage rocket;
 
 	private RGBImage scoreBubble;
 	private int muzzle1Rotation;
 
 	private RGBImage muzzle2;
 	private int index = 0;;
 	private Tank tankEntity1;
 	private Tank tankEntity2;
 
 	public Tank getTankEntity1() {
 		return tankEntity1;
 	}
 
 	public void setTankEntity1(Tank tankEntity1) {
 		this.tankEntity1 = tankEntity1;
 	}
 
 	public Tank getTankEntity2() {
 		return tankEntity2;
 	}
 
 	public void setTankEntity2(Tank tankEntity2) {
 		this.tankEntity2 = tankEntity2;
 	}
 
 	private RGBImage backGround;
 
 	Random rand = new Random();
 
 	public testLevel(BaseGame game, InputHandler handler) {
 		super(game, handler);
 		tankEntity1 = new Tank(10, 10, 1, handler, this);
 		addEntity(tankEntity1);
 		tankEntity2 = new Tank(1000, 10, 2, handler, this);
 		addEntity(tankEntity2);
 
 		AnimationFactory.getInstance().addSpriteSheet(new File("./res/Explosion1.png"), Animations.EXPLOSIONS, 50, 50);
 		AnimationFactory.getInstance().addSpriteSheet(new File("./res/Explosion2.png"), Animations.EXPLOSIONS2, 100, 100);
 
	
		
		
 //		terrain = new Terrain(new File("./res/testlvl.png"));
 		terrain = new Terrain(new File("./res/Cityscape_terrain.png"));
 		backGround = new RGBImage(new File("./res/Cityscape_background.png"));
 		crossHair1 = new RGBImage(new File("./res/Crosshair.png"));
 		crossHair2 = new RGBImage(new File("./res/Crosshair.png"));
 
 	}
 
 	//
 	public void tick(double dt) {
 		super.tick(dt);
 	}
 
 	public void onDraw(Renderer renderer) {
 		super.render(renderer);
 
 		renderer.DrawImage(backGround, 0, 0, testGame.WIDTH, testGame.HEIGHT);
 		renderer.DrawImage(terrain, -1, 0, 0, terrain.getWidth(), terrain.getHeight());
 
 		for (Entity ent : entities) {
 			ent.render(renderer);
 		}
 
 	}
 
 }
