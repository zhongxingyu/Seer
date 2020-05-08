 package entities;
 
 import game.Game;
 import game.Gameend;
 import graphics.Image;
 import graphics.Sprite;
 
 import java.awt.Graphics;
 import java.util.ArrayList;
 import java.util.List;
 
 import level.Box;
 
 public class BombAnimation extends Entity {
 
 	/**
 	 * 
 	 */
 	private Image[][] explosionImages;
 	/**
 	 * 
 	 */
 	private int explosionTime;
 	/**
 	 * 
 	 */
 	private int exposionTimeDefault;
 	/**
 	 * 
 	 */
 	private int[][] collideMap;
 	/**
 	 * 
 	 */
 	private Player playerKilled;
 	/**
 	 * 
 	 */
 	private int killDelay;
 
 	/**
 	 * @param x
 	 * @param y
 	 */
 	public BombAnimation(int x, int y) {
 		super(x, y);
		this.explosionImages = Sprite.load("explosion_temp.png", 100, 100);
 		this.exposionTimeDefault = 40;
 		this.explosionTime = this.exposionTimeDefault;
 		this.collideMap = new int[][] { { 0, 0, 1, 0, 0 }, { 0, 0, 1, 0, 0 },
 				{ 1, 1, 1, 1, 1 }, { 0, 0, 1, 0, 0 }, { 0, 0, 1, 0, 0 } };
 		this.playerKilled = null;
 		this.killDelay = 10;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see entities.Entity#action(double)
 	 */
 	@Override
 	public void action(double delta) {
 
 		if ((this.playerKilled != null)) {
 			this.killDelay--;
 			if (this.killDelay == 0) {
 				// this.playerKilled.removed = true;
 				Game.getInstance().gameEnd(this.playerKilled, Gameend.dead);
 			}
 		}
 
 		if (this.explosionTime > 0) {
 			this.explosionTime--;
 		} else {
 			this.removed = true;
 		}
 
 		List<Entity> entities = null;
 
 		int x_tmp = this.x - (2 * Game.BLOCK_SIZE), y_tmp = this.y
 				- (2 * Game.BLOCK_SIZE);
 
 		entities = new ArrayList<Entity>();
 		if (this.explosionTime < (this.exposionTimeDefault * 0.7)) {
 			for (int x = 0; x < 5; x++) {
 				for (int y = 0; y < 5; y++) {
 					if (this.collideMap[y][x] == 1) {
 						entities.addAll(Game.getEntities(new Box(x_tmp
 								+ (x * Game.BLOCK_SIZE), this.y,
 								Game.BLOCK_SIZE, Game.BLOCK_SIZE)));
 						entities.addAll(Game.getEntities(new Box(this.x, y_tmp
 								+ (y * Game.BLOCK_SIZE), Game.BLOCK_SIZE,
 								Game.BLOCK_SIZE)));
 					}
 				}
 			}
 		} else if (this.explosionTime < (this.exposionTimeDefault * 0.9)) {
 			for (int x = 1; x < 4; x++) {
 				for (int y = 1; y < 4; y++) {
 					if (this.collideMap[y][x] == 1) {
 						entities.addAll(Game.getEntities(new Box(x_tmp
 								+ (x * Game.BLOCK_SIZE), this.y,
 								Game.BLOCK_SIZE, Game.BLOCK_SIZE)));
 						entities.addAll(Game.getEntities(new Box(this.x, y_tmp
 								+ (y * Game.BLOCK_SIZE), Game.BLOCK_SIZE,
 								Game.BLOCK_SIZE)));
 					}
 				}
 			}
 		} else {
 			entities = Game.getEntities(new Box(this.x, this.y,
 					Game.BLOCK_SIZE, Game.BLOCK_SIZE));
 		}
 
 		for (Entity e : entities) {
 			if (e != this) {
 				e.collide(this);
 				this.collide(e);
 			}
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see entities.Entity#draw(java.awt.Graphics)
 	 */
 	@Override
 	public void draw(Graphics g) {
 
 		int x_tmp = this.x - (2 * Game.BLOCK_SIZE), y_tmp = this.y
 				- (2 * Game.BLOCK_SIZE);
 
 		if (this.explosionTime < (this.exposionTimeDefault * 0.7)) {
 
 			for (int x = 0; x < 5; x++) {
 				if (this.collideMap[2][x] == 1) {
 					g.drawImage((this.explosionImages[0][0]).image, x_tmp
 							+ (x * Game.BLOCK_SIZE), this.y, Game.BLOCK_SIZE,
 							Game.BLOCK_SIZE, null);
 				}
 				if (this.collideMap[x][2] == 1) {
 					g.drawImage((this.explosionImages[2][0]).image, this.x,
 							y_tmp + (x * Game.BLOCK_SIZE), Game.BLOCK_SIZE,
 							Game.BLOCK_SIZE, null);
 				}
 			}
 
 		}
 
 		else if (this.explosionTime < (this.exposionTimeDefault * 0.9)) {
 
 			for (int x = 1; x < 4; x++) {
 				if (this.collideMap[2][x] == 1) {
 					g.drawImage((this.explosionImages[0][0]).image, x_tmp
 							+ (x * Game.BLOCK_SIZE), this.y, Game.BLOCK_SIZE,
 							Game.BLOCK_SIZE, null);
 				}
 				if (this.collideMap[x][2] == 1) {
 					g.drawImage((this.explosionImages[2][0]).image, this.x,
 							y_tmp + (x * Game.BLOCK_SIZE), Game.BLOCK_SIZE,
 							Game.BLOCK_SIZE, null);
 				}
 			}
 		}
 
 		g.drawImage((this.explosionImages[1][0]).image, this.x, this.y,
 				Game.BLOCK_SIZE, Game.BLOCK_SIZE, null);
 
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see entities.Entity#collide(entities.Entity)
 	 */
 	@Override
 	public void collide(Entity e) {
 		if (e instanceof Player) {
 			this.playerKilled = (Player) e;
 		}
 		if (e instanceof Bomb) {
 			((Bomb) e).forceExplosion();
 		}
 		if (e instanceof BreakableWall) {
 			e.removed = true;
 		} else if (e instanceof Wall) {
 			int x = this.box.getDistance(e.box, 1) + 2;
 			int y = this.box.getDistance(e.box, 0) + 2;
 
 			if (x == 0) {
 				this.collideMap[2][0] = 0;
 			} else if (x == 1) {
 				this.collideMap[2][0] = 0;
 				this.collideMap[2][1] = 0;
 			} else if (x == 2) {
 				if (y == 0) {
 					this.collideMap[0][2] = 0;
 				} else if (y == 1) {
 					this.collideMap[0][2] = 0;
 					this.collideMap[1][2] = 0;
 				} else if (y == 3) {
 					this.collideMap[3][2] = 0;
 					this.collideMap[4][2] = 0;
 				} else if (y == 4) {
 					this.collideMap[4][2] = 0;
 				}
 			} else if (x == 3) {
 				this.collideMap[2][3] = 0;
 				this.collideMap[2][4] = 0;
 			} else if (x > 2) {
 				this.collideMap[2][4] = 0;
 			}
 		}
 	}
 }
