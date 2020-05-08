 package com.gamedev.decline;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.graphics.Texture;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 
 /*
  * The class that creates the repeating background
  */
 public class RepeatingBackground {
 
 	private GlobalSingleton gs = GlobalSingleton.getInstance();
 	// the number of background panels
 	private int bpSize = 2;
 	// the array of the background panels
 	private BackgroundPanel[] bp = new BackgroundPanel[bpSize];
 
 	/*
 	 * The constructor for the class
 	 * 
 	 * newTexture - the images for the background panels
 	 */
 	public RepeatingBackground(Texture newTexture) {
 		for (int i = 0; i < bpSize; i++) {
 			bp[i] = new BackgroundPanel(newTexture);
 		}
 	}
 
 	/*
 	 * The method that handles drawing the background panels
 	 * 
 	 * batch - the drawing object heroPos - the x position of the hero
 	 */
 	public void draw(SpriteBatch batch) {
 		float backgroundPos = (gs.getWorldXPos() + gs.HERO_XDRAW) % bp[0].getWidth();
		System.out.println(backgroundPos);
 		// System.out.println("HeroPos: "+ heroPos);
 		// System.out.println("bp.width: "+bp[0].getWidth());
 		// System.out.println("heroStaringPos: "+heroStartingPos);
 		// System.out.println("graphics.width: " + Gdx.graphics.getWidth());
 
 		// for when hero is on left edge of background panel
 		if (gs.getHeroXPos() % bp[0].getWidth() < GlobalSingleton.HERO_XDRAW) {
			System.out.println("In 1!!");
 
 			bp[0].setXPos(-1 * (backgroundPos));
 			bp[1].setXPos(-1 * backgroundPos - bp[0].getWidth());
 			bp[0].draw(batch);
 			bp[1].draw(batch);
 
 		}
 		// when hero is on right edge of background panel
 		else if (gs.getHeroXPos() % bp[0].getWidth() > bp[0].getWidth()
 				- Gdx.graphics.getWidth()) {
			System.out.println("In 2!!");
 			bp[0].setXPos(-1 * backgroundPos);
 			bp[1].setXPos(-1 * backgroundPos + bp[0].getWidth());
 			bp[0].draw(batch);
 			bp[1].draw(batch);
 		}
 		// when hero is in the center of the background panel
 		else {
 			// System.out.println("In 3!!");
 			bp[0].setXPos(-1 * (backgroundPos % bp[0].getWidth()));
 			bp[0].draw(batch);
 		}
 	}
 
 }
