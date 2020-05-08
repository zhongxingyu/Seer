 package atlantis.samples.platformer;
 
 import java.util.ArrayList;
 import java.util.Random;
 
 import atlantis.engine.graphics.Sprite;
 import atlantis.framework.GameTime;
 import atlantis.framework.content.ContentManager;
 import atlantis.samples.platformer.Player.MovementState;
 
 public class Monster extends Sprite {
 	private static Sprite[] levelBlocks;
 	private final static Random random = new Random();
 	private final static int timeBeforeCheckNextAction = 3500;
 	private long elapsedTime;
 	private float gravity;
 	
 	public Monster(int id) {
 		this.textureName = "img/Monsters/monster";
 		switch (id) {
 			case 7: this.textureName += "A.png"; break;
 			case 8: this.textureName += "B.png"; break;
 			case 9: this.textureName += "C.png"; break;
 			case 10: this.textureName += "D.png"; break;
 		}
 		
 		this.gravity = 9.0f;
 		this.elapsedTime = 0;
 	}
 	
 	public void loadContent(ContentManager content) {
 		super.loadContent(content);
 		
 		this.prepareAnimation(64, 64);
 		this.addAnimation("left", new int[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 }, 20);
 		this.addAnimation("right", new int[] { 11, 12, 13, 14, 15, 16, 17, 18, 19, 20 }, 20);
 		this.addAnimation("idle", new int[] { 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32 }, 20);
 		this.play("idle");
 		//this.forceInsideScreen(true);
 		this.getNextAction();
 	}
 	
 	public static void setBlocks(ArrayList<Sprite> blocks) {
 		levelBlocks = new Sprite[blocks.size()];
 		for (int i = 0, l = blocks.size(); i < l; i++) {
 			levelBlocks[i] = blocks.get(i);
 		}
 	}
 	
 	public void update(GameTime gameTime) {
 		super.update(gameTime);
 		
 		this.elapsedTime += gameTime.getElapsedTime();
 		if (this.elapsedTime >= timeBeforeCheckNextAction) {
 			this.elapsedTime = 0;
 			this.getNextAction();
 		}
 		
 		if (this.getX() <= this.viewport.x || this.getX() + this.getWidth() >= this.viewport.width) {
 			this.direction.x *= -1;
 		}
 		
 		this.updatePhysics();
 		
 		if (this.direction.x < 0) {
 			this.play("left");
 			this.move(-1, 0);
 		}
 		else if (this.direction.x > 0) {
 			this.play("right");
 			this.move(1, 0);
 		}
 		else {
 			this.play("idle");
 			this.move(0, 0);
 		}
 	}
 	
 	/**
 	 * Update physics of the player by using a gravity on it.
 	 * @param blocksSize Size of the collection of blocks.
 	 * @param blocks Blocks that can be collided.
 	 */
 	public void updatePhysics() {
		this.setY((int) (this.getY() + this.gravity));
 		
 		int i = 0;
 		int size = levelBlocks.length;
 		boolean collide = false;
 	/*
 		while(i < size && collide == false) {
 			if (this.getBoundingRectangle().contains(levelBlocks[i].getBoundingRectangle())) {
 				if(this.position.y < levelBlocks[i].getY()) {
 					this.setY(levelBlocks[i].getBoundingRectangle().getBottom());
 				} 
 				else {
 					this.setY(levelBlocks[i].getY() - this.getHeight());
 				}
 				collide = true;
 			}
 			i++;
 		}
 		*/
 		if (collide) {
 			this.direction.x *= -1;
 		}
 	}
 	
 	private void getNextAction() {
 		int randomAction = random.nextInt(1500);
 		
 		if (randomAction % 2 == 0) {
 			this.direction.x = -1;
 		}
 		else if (randomAction % 3 == 0) {
 			this.direction.x = 1;
 		}
 		else {
 			this.direction.x = 0;
 		}
 	}
 	
 	public void setStartPosition(float x, float y) {
 		setPosition(x * 24, y * 32 - this.getHeight() / 2);
 	}
 }
