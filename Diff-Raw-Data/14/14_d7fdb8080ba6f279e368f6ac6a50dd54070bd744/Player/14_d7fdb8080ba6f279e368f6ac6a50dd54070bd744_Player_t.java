 package entities;
 
 import game.Game;
 import game.InputHandler;
 import graphics.Sprite;
 
 import java.awt.Graphics;
 
 public class Player extends Entity {
 
	public InputHandler input = new InputHandler();
 
 	public Player(int x, int y) {
 		super(x, y);
 		this.images = Sprite.load("bomberman_small.png", 31, 70);
 	}
 
 	@Override
 	public void draw(Graphics g) {
 		g.drawImage((this.images[0][0]).image, this.x, this.y, Game.BLOCK_SIZE,
 				Game.BLOCK_SIZE, null);
 	}
 
 	public void step() {
 
		if (this.input.up.down) {
 			this.y = this.y + 1;
 		}
 
		if (this.input.down.down) {
 			this.y = this.y - 1;
 		}
 
		if (this.input.left.down) {
 			this.x = this.x - 1;
 		}
 
		if (this.input.right.down) {
 			this.x = this.x + 1;
 		}
 
 	}
 }
