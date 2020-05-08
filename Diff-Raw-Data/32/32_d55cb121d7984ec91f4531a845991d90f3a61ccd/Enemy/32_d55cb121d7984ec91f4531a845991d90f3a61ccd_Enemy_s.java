 package com.gamedev.decline;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.graphics.Texture;
 import com.badlogic.gdx.graphics.g2d.Sprite;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.graphics.g2d.TextureRegion;
 
 /*
  * The Enemy class for the game.
  */
 public class Enemy extends Sprite {
 
 	//the x position of the enemy
 	private float xPos;
 	//the singleton containing the global variables of the game
 	private GlobalSingleton gs = GlobalSingleton.getInstance();
 	//the y position of the enemy
 	private int yPos = gs.getStartingHeroYPos();
 	//the enemies speed
 	private int enemySpeed = 2;
 	//the enemies height
 	private int enemyHeight = 100;
 	//the enemies width
 	private int enemyWidth = 100;
 	//the staring x position of the enemey; used when reinitializing the enemy
 	private int startingXPos;
 		
 
 	/*
 	 * The default constructor for the enemy class
 	 */
 	public Enemy() {
 		// TODO Auto-generated constructor stub
 	}
 
 	/*
 	 * The enemy constructor that uses a texture
 	 * 
 	 * texture - the image of the enemy
 	 */
 	public Enemy(Texture texture) {
 		super(texture);
 		initialize();
 	}
 
 	/*
 	 * The enemy constructor that uses a TextureRegion
 	 * 
 	 * region - the textureregion of the enemy
 	 */
 	public Enemy(TextureRegion region) {
 		super(region);
 		initialize();
 	}
 
 	/*
 	 * the enemy constructor that uses a sprite
 	 * 
 	 * sprite - the sprite to make an enemy
 	 */
 	public Enemy(Sprite sprite) {
 		super(sprite);
 		initialize();
 	}
 
 	/*
 	 * A quicker way to create an enemy with a textureRegion
 	 * 
 	 * texture - the original image
 	 * srcWidth - the width of the region of the image that you want
 	 * srcHeight - the height of the region of the image that you want
 	 */
 	public Enemy(Texture texture, int srcWidth, int srcHeight) {
 		super(texture, srcWidth, srcHeight);
 		initialize();
 	}
 
 	/*
 	 * Note: haven't tested this one out
 	 * 
 	 * A quicker way to create a texture region
 	 * 
 	 * texture - the original image
 	 * srcX - (Not documented well on the website) I assume it is the starting x position 
 	 *   in the image for your region
 	 * srcY - (Not documented well on the website) I assume it is the starting y position 
 	 *   in the image for your region
 	 * srcWidth - the width of the region of the image you want 
 	 * srcHeight - the height of the region of the image you want
 	 */
 	public Enemy(Texture texture, int srcX, int srcY, int srcWidth,
 			int srcHeight) {
 		super(texture, srcX, srcY, srcWidth, srcHeight);
 		initialize();
 	}
 
 	/*
 	 * Note: haven't tested this one out
 	 * 
 	 * A quicker way to create a texture region
 	 * 
 	 * region - the original region
 	 * srcX - (Not documented well on the website) I assume it is the starting x position 
 	 *   in the image for your new region
 	 * srcY - (Not documented well on the website) I assume it is the starting y position 
 	 *   in the image for your new region
 	 * srcWidth - the width of the new region of the image you want 
 	 * srcHeight - the height of the new region of the image you want
 	 */
 	public Enemy(TextureRegion region, int srcX, int srcY, int srcWidth,
 			int srcHeight) {
 		super(region, srcX, srcY, srcWidth, srcHeight);
 		initialize();
 	}
 
 	/*
 	 * The method called when creating an enemy.  It sets the 
 	 * staring position of the enemy, the x position of the
 	 * enemy, and the sprite's size.
 	 */
 	public void initialize()
 	{
 	   startingXPos = Gdx.graphics.getWidth() - enemyWidth/2;
 	   xPos = startingXPos;
 	   setSize(enemyWidth, enemyHeight);
 	   
 	}
 	
 	/*
 	 * This method moves the enemies.
 	 */
 	public void update()
 	{
 		xPos -= enemySpeed + gs.getHeroMovement();
 	}
 	
 	/*
 	 * This returns the xPosition of the enemy
 	 */
 	public float getXPos()
 	{
 		return xPos;
 	}
 	
 	/*
 	 * This returns the yPosition of the enemy
 	 */
 	public int getYPos()
 	{
 		return yPos;
 	}
 	
 	/*This method returns the enemy back to its original starting position;
 	 *Used when the enemy is being taken off of the screen
 	 */
 	public void setToStartingPosition()
 	{
 		xPos = startingXPos;
 	}
 }
