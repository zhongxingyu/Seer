 package com.gamedev.decline;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.graphics.Texture;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 /*
  * The class for the ammo objects that appear during the 
  * game
  */
 public class Ammo 
 {
 	// the texture of the ammo
 	Texture texture;
 	// the x position of the ammo object
 	float xPos;
 	// the global singleton reference for the object
 	GlobalSingleton gs = GlobalSingleton.getInstance();
 	// the y position of the ammo object
 	float yPos = gs.getStartingHeroYPos(); 
 	//the starting position of the ammo objects on the screen
 	int startPosition;
 	//the amount of ammo stored in the container
 	int amountOfAmmoStored;
	//the width of the ammo object on the screen
	int ammoWidth;
 	
 	/*
 	 * the constructor for the class
 	 * 
 	 * newTexture - the texture for the ammo object
	 * newAmmoWidth - the width of the ammo object on the screen
 	 */
	public Ammo(Texture newTexture, int newAmmoWidth)
 	{
 		texture = newTexture;
		ammoWidth = newAmmoWidth;
 		startPosition = Gdx.graphics.getWidth() - ammoWidth/2;
 	}
 	
 	/*
 	 * returns the amount of ammo that the ammo object is 
 	 * holding
 	 */
 	public int getAmountOfAmmoStored()
 	{
 		return amountOfAmmoStored;
 	}
 	
 	/*
 	 * sets the amount of ammo that the ammo object is holding
 	 * 
 	 * newAmount - the new amount that the ammo is holding
 	 */
 	public void setAmountOfAmmoStored(int newAmount)
 	{
 		amountOfAmmoStored = newAmount;
 	}
 	
 	/*
 	 * Sets the ammo back to the original starting position 
 	 * of the object
 	 */
 	public void setToStartPosition()
 	{
 		xPos = startPosition;
 	}
 	
 	/*
 	 * Updates the location of the ammo.  It returns true
 	 * if the ammo is still in the screen and false otherwise.
 	 */
 	public boolean update()
 	{
 		xPos -= gs.getHeroMovement();
		if(xPos < -1 * ammoWidth || xPos > Gdx.graphics.getWidth() + ammoWidth)
 		{
 			return false;
 		}
 		else
 		{
 			return true;
 		}
 	}
 	
 	/*
 	 * Draws the ammo object to the specified size
 	 * 
 	 * batch - the drawing visitor object
 	 * xSize - the x dimension size of the object
 	 * ySize - the y dimension size of the object
 	 */
 	public void draw(SpriteBatch batch, int xSize, int ySize)
 	{
 		batch.draw(texture, xPos, yPos, xSize, ySize);
 	}
 	
 }
