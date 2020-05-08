 package com.buildcoolrobots.games.pgcgame.CoreTypes;
 
 import me.pagekite.glen3b.gjlib.ExtendedSprite;
 import me.pagekite.glen3b.gjlib.SpriteManager;
 
 import com.badlogic.gdx.graphics.g2d.Sprite;
 import com.buildcoolrobots.games.pgcgame.CoreTypes.Enums.ScreenType;
 
 public class BGSprite extends Sprite {
 	private static final BGSprite _currentBG = new BGSprite();
 	
 	
 	private BGSprite() {
 		super(ScreenType.TITLESCREEN.ScreenTexture());
	}
	
	public static BGSprite getInstance() {
        return _currentBG;      
	}
	
	
	private static ExtendedSprite background;
	private static ExtendedSprite background2;
	public static void scrollingBackground(SpriteManager allSprites) {		
 		background = new ExtendedSprite(ScreenType.TITLESCREEN.ScreenTexture());
 		background.setPosition(0, 0);
 		background.xSpeed = -1f;
 		
 		background2 = new ExtendedSprite(ScreenType.TITLESCREEN.ScreenTexture());
 		if (!StateManager.TitleScreenBGSpriteDebug) {
 			background2.setPosition(background.getX() + background.getWidth(), 0);
 		}
 		else {
 			background2.setPosition(background.getX() + background.getWidth() + 1, 0);
 		}
 		background2.xSpeed = -1f;
		
 		allSprites.add(background);
 		allSprites.add(background2);
 	}
 	
 	public static void scrollingBackgroundUpdate() {
 		if (background.getX() + background.getWidth() <= 0) {
 			if (!StateManager.TitleScreenBGSpriteDebug) {
 				background.setPosition(background2.getX() + background2.getWidth(), 0);
 			}
 			else {
 				background.setPosition(background2.getX() + background2.getWidth() + 1, 0);
 			}
 		}
 		
 		if (background2.getX() + background2.getWidth() <= 0) {
 			if (!StateManager.TitleScreenBGSpriteDebug) {
 				background2.setPosition(background.getX() + background.getWidth(), 0);
 			}
 			else {
 				background2.setPosition(background.getX() + background.getWidth() + 1, 0);
 			}
 		}
 	}
 }
