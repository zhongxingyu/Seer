 package com.musicgame.PumpAndJump.game.gameStates;
 
 import java.util.Scanner;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.files.FileHandle;
 import com.badlogic.gdx.graphics.Color;
 import com.badlogic.gdx.graphics.GL10;
 import com.badlogic.gdx.graphics.Pixmap;
 import com.badlogic.gdx.graphics.Texture;
 import com.badlogic.gdx.graphics.Pixmap.Format;
 import com.badlogic.gdx.graphics.g2d.BitmapFont;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.scenes.scene2d.Actor;
 import com.badlogic.gdx.scenes.scene2d.Stage;
 import com.badlogic.gdx.scenes.scene2d.ui.Image;
 import com.badlogic.gdx.scenes.scene2d.ui.Label;
 import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
 import com.badlogic.gdx.scenes.scene2d.ui.Skin;
 import com.badlogic.gdx.scenes.scene2d.ui.Table;
 import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
 import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
 import com.badlogic.gdx.scenes.scene2d.ui.TextField;
 import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
 import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener.ChangeEvent;
 import com.musicgame.PumpAndJump.game.GameThread;
 import com.musicgame.PumpAndJump.game.PumpAndJump;
 import com.musicgame.PumpAndJump.game.ThreadName;
 
 /**
  * It happens before the game starts
  * @author gigemjt
  *
  */
 public class AboutGame extends GameThread
 {
 
 	Stage stage;
 
 	public AboutGame()
 	{
		batch = new SpriteBatch();
 		stage = new Stage();
 
 		if(uiSkin == null)
 		{
 	        FileHandle skinFile = Gdx.files.internal( "uiskin/uiskin.json" );
 	        uiSkin = new Skin( skinFile );
 		}
 
         Table table = new Table();
 		stage.addActor(table);
 		table.setFillParent(true);
 
 		Table scrolltable = new Table();
 		scrolltable.setFillParent(true);
 		final ScrollPane scroll = new ScrollPane(scrolltable, uiSkin);
 		//final Label infoText = new Label("A side scroller game that generates levels from music.\n New line tester. Made in the labs at Texas A&M.", uiSkin);
 		final Label infoText = new Label(readStory(),uiSkin);
 		infoText.setFontScale((float) 0.8);
 		infoText.setWrap(true);
 		scrolltable.add(infoText).size(800,500);
 
 		final TextButton backButton = new TextButton("Back", uiSkin);
 		backButton.addListener(
 					new ChangeListener()
 					{
 						public void changed(ChangeEvent event, Actor actor)
 						{
 							PumpAndJump.switchThread(ThreadName.PreGame, AboutGame.this);
 						}
 					});
 
 		table.add(scroll).size(800,500);
 		table.row().space(10);//.padBottom(10);
 		table.add(backButton).size(250,50).pad(5);
 	}
 
 	private static String readStory()
 	{
 		FileHandle dir= Gdx.files.internal("story.txt");
 		Scanner LevelIn = new Scanner(dir.reader());
 		String storyText = "";
 		while(LevelIn.hasNextLine())
 		{
 			String g2 = LevelIn.nextLine();
 			System.out.println(g2);
 			storyText+=g2+"\n";
 		}
 		System.out.println(storyText);
 		return storyText;
 	}
 
 	@Override
     public void render(float delta)
 	{
 		Gdx.gl.glClearColor(1.0f, 1.0f, 1.0f, 1);
 		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
 		stage.act(Math.min(delta, 1 / 30f));
 		stage.draw();
 		//Table.drawDebug(stage);
     }
 
 	@Override
     public void resize(int width, int height)
 	{
 		super.resize(width, height);
         stage.setViewport(width, height, false);
     }
 
 	@Override
 	public void switchFrom(GameThread currentThread)
 	{
 		Gdx.input.setInputProcessor(stage);
 	}
 
 	@Override
 	public void addFrom(GameThread currentThread) {
 	}
 
 	@Override
 	public void removeFrom(GameThread currentThread)
 	{
 		PumpAndJump.setThreadToNull(getThreadName());
 	}
 
 	@Override
 	public void unpause() {
 	}
 
 	@Override
 	public ThreadName getThreadName() {
 		return ThreadName.AboutGame;
 	}
 
 	@Override
 	public void repause() {
 	}
 
 }
