 package pudgewars.level;
 
 import java.awt.image.BufferedImage;
 
 import pudgewars.Game;
 import pudgewars.entities.Entity;
 import pudgewars.entities.HookableEntity;
 import pudgewars.entities.hooks.HookEntity;
 import pudgewars.interfaces.BBOwner;
 import pudgewars.util.ImageHandler;
 import pudgewars.util.Time;
 
 public class Tile implements BBOwner {
 	public final static Tile VOID = new Tile("void", 0, 1, 0, false, false, false);
 
 	public final static Tile NEWGRASS = new Tile("newgrass", 0, 8, 0.25, false, false, false);
 
	public final static Tile[] GRASS = //
	{ new Tile("grass", 0, 1, 0, false, false, false), //
			new Tile("grass", 1, 1, 0, false, false, false), //
			new Tile("grass", 2, 1, 0, false, false, false), //
			new Tile("grass", 3, 1, 0, false, false, false), //
			new Tile("grass", 4, 1, 0, false, false, false), //
			new Tile("grass", 5, 1, 0, false, false, false), //
			new Tile("grass", 6, 1, 0, false, false, false), //
			new Tile("grass", 7, 1, 0, false, false, false) };
 
 	public final static Tile[] FOUNTAIN = //
 	{ new Tile("fountain3", 0, 8, 0.1875, false, false, false), //
 			new Tile("fountain3", 1, 8, 0.1875, false, false, false), //
 			new Tile("fountain3", 2, 8, 0.1875, false, false, false), //
 			new Tile("fountain3", 3, 8, 0.1875, false, false, false) };
 
 	public final static Tile[] TREE = //
 	{ new Tile("tree", 0, 1, 0, true, true, false), //
 			new Tile("tree", 1, 1, 0, true, true, false), //
 			new Tile("tree", 2, 1, 0, true, true, false), //
 			new Tile("tree", 3, 1, 0, true, true, false) };
 
 	public final static Tile[] HOOKABLE = //
 	{ new LightTile("haybail3", 0, 1, 0, false, false, true, 13, 13), //
 			new LightTile("haybail3", 1, 1, 0, false, false, true, 13, 13), //
 			new LightTile("haybail3", 2, 1, 0, false, false, true, 13, 13), //
 			new LightTile("haybail3", 3, 1, 0, false, false, true, 13, 13), //
 			new LightTile("haybail3", 4, 1, 0, false, false, true, 13, 13), //
 			new LightTile("haybail3", 5, 1, 0, false, false, true, 13, 13), //
 	};
 
 	public final static Tile WATER = new Tile("river0000", 0, 8, 0.5, false, false, false);
 	public final static Tile T_Mound = new Tile("mound2", 0, 1, 0, true, false, false);
 
 	protected String ID;
 	protected BufferedImage[] img;
 	protected boolean pudgeSolid;
 	protected boolean hookSolid;
 	protected boolean hookable;
 	protected double animationSpeed;
 
 	public Tile(String ID, int x, int numAnim, double animationSpeed, boolean pudgeSolid, boolean hookSolid, boolean hookable) {
 		this.ID = ID;
 		this.pudgeSolid = pudgeSolid;
 		this.hookSolid = hookSolid;
 		this.hookable = hookable;
 		this.animationSpeed = animationSpeed == 0 ? 1 : animationSpeed;
 
 		img = new BufferedImage[numAnim];
 		for (int i = 0; i < numAnim; i++)
 			img[i] = ImageHandler.get().getImage(ID, x, i, 16, 16);
 	}
 
 	public void render(int x, int y, int tileData) {
 		Game.s.g.drawImage(img[(tileData / (int) (animationSpeed * Time.TICKS_PER_SECOND)) % img.length], x, y, null);
 	}
 
 	public void postRender(int x, int y) {
 	}
 
 	/*
 	 * Collision Detection!
 	 */
 	public boolean isPudgeSolid() {
 		return pudgeSolid;
 	}
 
 	public boolean isHookSolid() {
 		return hookSolid;
 	}
 
 	public boolean isHookable() {
 		return hookable;
 	}
 
 	public final boolean blocks(BBOwner b) {
 		return b.shouldBlock(this) && this.shouldBlock(b);
 	}
 
 	public boolean shouldBlock(BBOwner b) {
 		// Hook Collisions
 		if (b instanceof HookEntity) {
 			if (isHookable()) return true;
 			else if (isHookSolid()) return true;
 			else return false;
 		}
 		// Pudge Collisions
 		if (b instanceof HookableEntity) {
 			if (isPudgeSolid()) return true;
 			return false;
 		}
 		return false;
 	}
 
 	public void handleCollision(Entity e, double vx, double vy) {
 		e.collides(this, vx, vy);
 	}
 
 	public void collides(Entity e, double vx, double vy) {
 	}
 
 	public void collides(Tile t, double vx, double vy) {
 	}
 
 	public boolean isTangible() {
 		return true;
 	}
 
 }
