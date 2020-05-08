 package com.secondhand.scene;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 
 import org.anddev.andengine.engine.Engine;
 import org.anddev.andengine.entity.scene.menu.MenuScene;
 import org.anddev.andengine.entity.scene.menu.MenuScene.IOnMenuItemClickListener;
 import org.anddev.andengine.entity.scene.menu.item.IMenuItem;
 import org.anddev.andengine.entity.text.Text;
 import org.anddev.andengine.opengl.font.Font;
 import org.anddev.andengine.util.HorizontalAlign;
 
 import android.annotation.SuppressLint;
 import android.content.Context;
 
 import com.secondhand.debug.MyDebug;
 import com.secondhand.model.Player;
 import com.secondhand.model.Universe;
 import com.secondhand.resource.Fonts;
 import com.secondhand.resource.LocalizationStrings;
 
 public class GameOverScene extends GameMenuScene implements
 		IOnMenuItemClickListener {
 	private Font mFont;
 	private Player player;
 	private static final int NAME = 0;
 	private static final int SKIP = 1;
 	private static final int BACK = 1;
 	private int[] highScore;
 	private String[] highScoreName;
 	private BufferedReader reader;
 	Text textGameOver = null;
 
 	public GameOverScene(final Engine engine, final Context context) {
 		super(engine, context);
 	}
 
 	@Override
 	public void loadResources() {
 		mFont = Fonts.getInstance().menuItemFont;
 		try {
 			reader = new BufferedReader(new InputStreamReader(context
 					.getAssets().open("highScore")));
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	@SuppressLint("NewApi")
 	@Override
 	public void loadScene() {
		
		player = Universe.getInstance().getLevel().getPlayer();
	
 		String mLine = "0";
 		int antal = 0;
 
 		try {
 
 			mLine = reader.readLine();
 			// checks if the score of the player are the top 5 score, and if it
 			// is it prints contgratulation. other way game over.
 			while (true) {
 				antal++;
 				mLine = reader.readLine().trim();
 				MyDebug.d(mLine);
 
 				if (player.getScore() > Integer.parseInt(mLine)) {
 					setHeadLine("congratulation");
 					break;
 				}
 
 				mLine = reader.readLine();
 
 				if (mLine == null && antal < 5) {
 					setHeadLine("congratulation");
 					break;
 				} else if (antal >= 5) {
 					setHeadLine("menu_game_over");
 					break;
 				}
 			}
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 	}
 
 	public void setHeadLine(String text) {
 		textGameOver = new Text(100, 60, mFont, LocalizationStrings
 				.getInstance().getLocalizedString(text), HorizontalAlign.CENTER);
 
 		final float x = this.smoothCamera.getWidth() / 2.0f
 				- textGameOver.getWidth() / 2.0f;
 		final float y = this.smoothCamera.getHeight() / 2.0f
 				- textGameOver.getHeight() / 2.0f;
 		textGameOver.setPosition(x, (int) (0.2 * y));
 
 		this.attachChild(textGameOver);
 		// The title
 
 		/*
 		 * } else { // not important yet textGameOver = new Text(100, 60, mFont,
 		 * LocalizationStrings
 		 * .getInstance().getLocalizedString("congratulations"),
 		 * HorizontalAlign.CENTER); }
 		 */
 
 	}
 
 	@Override
 	public AllScenes getParentScene() {
 		return AllScenes.MAIN_MENU_SCENE;
 	}
 
 	@Override
 	public boolean onMenuItemClicked(final MenuScene pMenuScene,
 			final IMenuItem pMenuItem, final float pMenuItemLocalX,
 			final float pMenuItemLocalY) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 }
