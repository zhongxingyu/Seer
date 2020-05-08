 package com.buildcoolrobots.games.pgcgame.Screens;
 
 import me.pagekite.glen3b.gjlib.ExtendedLabel;
 import me.pagekite.glen3b.gjlib.ExtendedSprite;
 import me.pagekite.glen3b.gjlib.SpriteManager;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.math.Vector2;
import com.buildcoolrobots.games.pgcgame.CoreTypes.BGSprite;
 import com.buildcoolrobots.games.pgcgame.CoreTypes.BaseScreen;
 import com.buildcoolrobots.games.pgcgame.CoreTypes.Enums.GameImage;
 import com.buildcoolrobots.games.pgcgame.CoreTypes.Enums.ScreenType;
 
 public class MainMenu extends BaseScreen {
 
 	public MainMenu(SpriteManager allSprites, SpriteBatch target, ScreenType screenType) {
 		super(allSprites, target, screenType);
 		
 		ExtendedSprite PlayButton = new ExtendedSprite(GameImage.MAINMENUPLAYBUTTON.ImageTexture());
 		PlayButton.setPosition(Gdx.graphics.getWidth() / 2 - PlayButton.getWidth()/2, 100);
 		PlayButton.setScale(new Vector2(1f, 1f));
 
 		
 		ExtendedLabel PlayButtonText = new ExtendedLabel("Play", GameImage.MAINMENUPLAYBUTTON.ImageText());
 		PlayButtonText.setPosition(Gdx.graphics.getWidth() / 2 - PlayButton.getWidth()/2 + PlayButtonText.getWidth()/2, 110);
 		//PlayButtonText.setFontScale(1.70f, .70f);
 		
 		allSprites.add(PlayButton);
 		allSprites.add(PlayButtonText);
 	}
 
 }
