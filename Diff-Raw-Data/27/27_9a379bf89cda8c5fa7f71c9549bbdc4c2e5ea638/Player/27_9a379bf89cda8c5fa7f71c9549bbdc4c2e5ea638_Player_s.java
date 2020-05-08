 package entities;
 
 import game.Game;
 import graphics.Sprite;
 
 import java.awt.Color;
 import java.awt.Graphics;
 import java.util.ArrayList;
 import java.util.List;
 
 import level.Box;
 
 public class Player extends Entity {
 	private float width;
 	private float height;
 
 	public Player(int x, int y) {
 		super(x + 1, y + 1);
 		this.images = Sprite.load("bomberman_small.png", 31, 70);
 		this.width = (31 / 2);
 		this.height = (70 / 2);
 		this.box = new Box(this.x, this.y, (int) this.width, (int) this.height);
 	}
 
 	@Override
 	public void draw(Graphics g) {
 		g.setColor(Color.GREEN);
		g.drawImage((this.images[0][0]).image, this.x, this.y, (int) this.width, (int) this.height, null);
 	}
 
 	@Override
 	public void step(double delta) {
 
 		if (Game.keys.up.down) {
			this.y = this.y - 1;
 		}
 
 		if (Game.keys.down.down) {
			this.y = this.y + 1;
 		}
 
 		if (Game.keys.left.down) {
			this.x = this.x - 1;
 		}
 
 		if (Game.keys.right.down) {
			this.x = this.x + 1;
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
 
 	}
 
 	@Override
 	public void collide(Entity e) {
 		if (e instanceof Wall) {
 			ArrayList<Integer> dir = this.box.collideDirection(e.box);
 			if (dir.contains(Box.COLLIDE_LEFT)) {
				this.x++;
 			}
 
 			if (dir.contains(Box.COLLIDE_RIGHT)) {
				this.x--;
 			}
 
 			if (dir.contains(Box.COLLIDE_DOWN)) {
				this.y--;
 			}
 
 			if (dir.contains(Box.COLLIDE_UP)) {
				this.y++;
 			}
 		}
 	}
 }
