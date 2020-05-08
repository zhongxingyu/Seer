 package main;
 
 import org.lwjgl.input.Keyboard;
 import org.lwjgl.opengl.GL11;
 
 /**
  * Subclass of Entity. 
  * 
  */
 public class Player extends Entity {
 	
 	/** The name of the player */
 	private String name;
 	
 	/** The player's experience */
 	private int exp;
 	
 	/**
 	 * Constructor for our finished product.<p>
 	 * 
 	 * This will create our player with all of his attributes
 	 * and sets him up for beginning the game.
 	 * 
 	 * @param x x coordinate for the entity
 	 * @param y y coordinate for the entity
 	 * @param w width of the entity
 	 * @param h height of the entity
 	 */
 	public Player(String name, int x, int y) {
 		
 		super(x*Main.gridSize, y*Main.gridSize);
 		
 		setMaxHP(100);
 		setMaxPP(100);
 		
 		setLevel(1);
 		setExp(0);
 		
 		setScene(0, 0);
 		
 		this.name = name;
 	}
 
 	/**
 	 * Method to move entity from user interaction.
 	 * 
 	 */
 	public void input() {
 
 		boolean aPressed = Keyboard.isKeyDown(Keyboard.KEY_A);
 		boolean dPressed = Keyboard.isKeyDown(Keyboard.KEY_D);
 		boolean wPressed = Keyboard.isKeyDown(Keyboard.KEY_W);
 		boolean sPressed = Keyboard.isKeyDown(Keyboard.KEY_S);
 
 		if ((aPressed) && (!dPressed)) {
 			tryToMove(Keyboard.KEY_A);
 		} else if ((dPressed) && (!aPressed)) {
 			tryToMove(Keyboard.KEY_D);
 		} else if ((wPressed) && (!sPressed)) {
 			tryToMove(Keyboard.KEY_W);
 		} else if ((sPressed) && (!wPressed)) {
 			tryToMove(Keyboard.KEY_S);
 		}
 	}
 	
 	/**
 	 * Here we test if we are within the bounds of the screen. If yes,
 	 * we call the super method to move. If not, we return control.<p>
 	 * 
 	 */
 	@Override
 	public void move(int keycode) {
 		
 		if (keycode == Keyboard.KEY_D) {
 			setRight(true);
 		} else if (keycode == Keyboard.KEY_A) {
 			setRight(false);
 		}
 		
 		checkBorders(keycode);
 		
 		if (checkCollisions(keycode))
 			super.move(keycode);
 		
 	}
 	
 	/**
 	 * Check to see if we are moving past one of the borders.<br>
 	 * If we are at one of the borders and we try and move, change the scene.
 	 * 
 	 * @param keycode Integer corresponding to the Keyboard class key event
 	 */
 	private void checkBorders(int keycode) {
 		
		if (getX() <= 1 && keycode == Keyboard.KEY_A && World.sceneExists(getSceneX()-1, getSceneY())) {
 			
 			setScene(getSceneX()-1, getSceneY());
 			World.setCurrentMap(getSceneX(), getSceneY());
 			setX(Main.SCREEN_PLAYABLE_WIDTH);
 			
		} else if (getX() >= Main.SCREEN_PLAYABLE_WIDTH-getW()-5 && keycode == Keyboard.KEY_D && World.sceneExists(getSceneX()+1, getSceneY())) {
 			
 			setScene(getSceneX()+1, getSceneY());
 			World.setCurrentMap(getSceneX(), getSceneY());
 			setX(-Main.gridSize);
 			
		} else if (getY() <= 1 && keycode == Keyboard.KEY_W && World.sceneExists(getSceneX(), getSceneY()+1)) {
 			
 			setScene(getSceneX(), getSceneY()+1);
 			World.setCurrentMap(getSceneX(), getSceneY());
 			setY(Main.SCREEN_PLAYABLE_HEIGHT);
 			
		} else if (getY() >= Main.SCREEN_PLAYABLE_HEIGHT-getH()-2 && keycode == Keyboard.KEY_S && World.sceneExists(getSceneX(), getSceneY()-1)) {
 			
 			setScene(getSceneX(), getSceneY()-1);
 			World.setCurrentMap(getSceneX(), getSceneY());
 			setY(-getH());
 		}
 	}
 	
 	/**
 	 * Check to see if there is a solid object in our current direction of movement
 	 * 
 	 * @param keycode
 	 * @return 	<code>true</code> if there is no collision, i.e. we can move<br>
 	 * 		<code>false</code> if there is a collision
 	 */
 	private boolean checkCollisions(int keycode) {
 		
 		int sX = super.getX()/Main.gridSize;
 		int sY = super.getY()/Main.gridSize;
 		
 		if (World.currentMap().solidAtPoint(sX-1, sY) && keycode == Keyboard.KEY_A) {
 			return false;
 		} else if (World.currentMap().solidAtPoint(sX+1, sY) && keycode == Keyboard.KEY_D) {
 			return false;
 		} else if (World.currentMap().solidAtPoint(sX, sY-1) && keycode == Keyboard.KEY_W) {
 			return false;
 		} else if (World.currentMap().solidAtPoint(sX, sY+1) && keycode == Keyboard.KEY_S) {
 			return false;
 		}
 		return true;
 	}
 	
 	/**
 	 * Method to execute if this object collided with another entity.
 	 * 
 	 */
 	@Override
 	public void collided(Entity x) {
 		
 		x.destroy();
 		
 	}
 
 	/** 
 	 * Draw our player to the screen. For the moment this is done as a square but
 	 * soon it will be drawn as a graphic.
 	 * 
 	 */
 	@Override
 	public void draw() {
 
 		GL11.glPushMatrix();
 		
 		GL11.glTranslatef(super.getX(), super.getY(), 0);
 		GL11.glColor3f(1.0f, 1.0f, 1.0f);
 		Main.BLANK_TEXTURE.bind();
 		GL11.glBegin(GL11.GL_QUADS);
 		{
 			
 			GL11.glColor3f(1.0f, 0.0f, 0.0f);
 			GL11.glVertex2f(0, 0); // top left
 			GL11.glVertex2f(0, Main.gridSize); // bottom left
 			GL11.glVertex2f(Main.gridSize, Main.gridSize); // bottom right
 			GL11.glVertex2f(Main.gridSize, 0); // top right
 			
 			
 			if (isRight()) {
 				GL11.glColor3f(1.0f, 1.0f, 1.0f);
 				GL11.glVertex2f(12, 0);
 				GL11.glVertex2f(12, 12);
 				GL11.glVertex2f(Main.gridSize, 12);
 				GL11.glVertex2f(Main.gridSize, 0);
 				
 				GL11.glColor3f(0.0f, 0.0f, 0.0f);
 				GL11.glVertex2f(Main.gridSize-6, 3);
 				GL11.glVertex2f(Main.gridSize-6, 9);
 				GL11.glVertex2f(Main.gridSize, 9);
 				GL11.glVertex2f(Main.gridSize, 3);
 				
 			} else {
 				GL11.glColor3f(1.0f, 1.0f, 1.0f);
 				GL11.glVertex2f(0, 0);
 				GL11.glVertex2f(0, 12);
 				GL11.glVertex2f(12, 12);
 				GL11.glVertex2f(12, 0);
 				
 				GL11.glColor3f(0.0f, 0.0f, 0.0f);
 				GL11.glVertex2f(0, 3);
 				GL11.glVertex2f(0, 9);
 				GL11.glVertex2f(4, 9);
 				GL11.glVertex2f(4, 3);
 			}
 			
 		}
 		GL11.glEnd();
 
 		GL11.glPopMatrix();
 		
 	}
 	
 	/** Return the player's name */
 	public String getName(){
 		return this.name;
 	}
 
 	/** What happens when we die */
 	@Override
 	public void notifyDeath() {
 		Game.GAME_STATE = Game.GAME_STATE_DEAD;
 		
 	}
 
 	/** Returns the player's current experience */
 	public int getExp() {
 		return exp;
 	}
 
 	/** Sets the player's current experience */
 	public void setExp(int exp) {
 		this.exp = exp;
 	}
 }
