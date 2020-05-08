 package entities;
 
 import enums.Gamemode;
 import enums.NetworkInputType;
 import game.Game;
 import game.KeySettings;
 import graphics.Image;
 import graphics.Sprite;
 
 import java.awt.Graphics;
 import java.util.ArrayList;
 import java.util.List;
 
 import level.Box;
 import network.Input;
 
 public class Player extends Entity {
 
 	/**
 	 * Create variables for width, height, speed, key settings Create array for
 	 * images
 	 */
 
 	private float width;
 	private float height;
 	private int speed;
 	private KeySettings keys;
 	private Image[][] facings;
 	private int facing = 0;
 	public int networkID;
 
 	/**
 	 * Constructor for default Player sets position, speed, images, height and
 	 * width
 	 */
 
 	public Player(int x, int y) {
 		super(x + 1, y + 1);
 		this.facings = Sprite.load("bomberman.png", 55, 90);
 		this.width = (55 / 2);
 		this.height = (90 / 2);
 		this.box = new Box(this.x, this.y, (int) this.width, (int) this.height);
 
 		this.speed = 10;
 	}
 
 	/**
 	 * draws Player on canvas
 	 */
 
 	@Override
 	public void draw(Graphics g) {
 		g.drawImage((this.facings[this.facing][0]).image, this.x, this.y, (int) this.width, (int) this.height, null);
 	}
 
 	/**
 	 * determines what happens if a key is pressed
 	 */
 	@Override
 	public void action(double delta) {
 		boolean moved = false;
 
 		/**
 		 * if key "down" is pressed
 		 */
 		if (this.keys.up.down) {
 			this.y = this.y - this.speed;
 			this.facing = 1;
 			moved = true;
 		}
 
 		/**
 		 * if key "up" is pressed
 		 */
 
 		if (this.keys.down.down) {
 			this.y = this.y + this.speed;
 			this.facing = 0;
 			moved = true;
 		}
 
 		/**
 		 * if key "left" is pressed
 		 */
 		if (this.keys.left.down) {
 			this.x = this.x - this.speed;
 			this.facing = 2;
 			moved = true;
 		}
 
 		/**
 		 * if key "right" is pressed
 		 */
 		if (this.keys.right.down) {
 			this.x = this.x + this.speed;
 			this.facing = 3;
 			moved = true;
 		}
 
 		/**
 		 * if no key is pressed
 		 */
 		if (moved == false) {
 			this.facing = 0;
 		}
 
 		this.box.update(this.x, this.y);
 
 		List<Entity> es = Game.getEntities(this.box);
 		for (Entity e : es) {
 			if (e != this) {
 				e.collide(this);
 				this.collide(e);
 				this.box.update(this.x, this.y);
 			}
 		}
 
		if ((Game.gamemode == Gamemode.NETWORK) && (this.networkID == Game.network.playerID) && (moved == true)) {
 			Input input = new Input();
 			input.x = this.x;
 			input.y = this.y;
 			input.type = NetworkInputType.PLAYER;
 			input.playerID = this.networkID;
 			Game.network.send(input);
 		}
 
 		/**
 		 * if key "bomb" is pressed
 		 */
 
 		if (this.keys.bomb.down) {
 			int b_x = Box.fitToBlock(this.x);
 			int b_y = Box.fitToBlock(this.y);
 			Game.entities.add(new Bomb(b_x, b_y, this));
 			if ((Game.gamemode == Gamemode.NETWORK) && (this.networkID == Game.network.playerID)) {
 				Input input_b = new Input();
 				input_b.x = b_x;
 				input_b.y = b_y;
 				input_b.type = NetworkInputType.BOMB;
 				input_b.playerID = this.networkID;
 				Game.network.send(input_b);
 			}
 		}
 	}
 
 	/**
 	 * checks if Player collides with an Entity
 	 */
 
 	@Override
 	public void collide(Entity e) {
 		if (e instanceof Wall) {
 			ArrayList<Integer> dir = this.box.collideDirection(e.box);
 			if (dir.contains(Box.COLLIDE_LEFT)) {
 				this.x = this.x + this.speed;
 			}
 
 			if (dir.contains(Box.COLLIDE_RIGHT)) {
 				this.x = this.x - this.speed;
 			}
 
 			if (dir.contains(Box.COLLIDE_DOWN)) {
 				this.y = this.y - this.speed;
 			}
 
 			if (dir.contains(Box.COLLIDE_UP)) {
 				this.y = this.y + this.speed;
 			}
 		}
 		// TODO: Player shouldn't be instantly kicked off the bomb
 		if ((e instanceof Bomb) && (((Bomb) e).owner != this)) {
 			ArrayList<Integer> dir = this.box.collideDirection(e.box);
 
 			if (dir.contains(Box.COLLIDE_LEFT)) {
 				this.x = this.x + this.speed;
 			}
 
 			if (dir.contains(Box.COLLIDE_RIGHT)) {
 				this.x = this.x - this.speed;
 			}
 
 			if (dir.contains(Box.COLLIDE_DOWN)) {
 				this.y = this.y - this.speed;
 			}
 
 			if (dir.contains(Box.COLLIDE_UP)) {
 				this.y = this.y + this.speed;
 			}
 
 		}
 	}
 
 	/**
 	 * sets KeySettings
 	 */
 	public void setKeys(KeySettings keys) {
 		this.keys = keys;
 	}
 
 	public void setPosition(int x, int y) {
 		this.x = x;
 		this.y = y;
 	}
 }
