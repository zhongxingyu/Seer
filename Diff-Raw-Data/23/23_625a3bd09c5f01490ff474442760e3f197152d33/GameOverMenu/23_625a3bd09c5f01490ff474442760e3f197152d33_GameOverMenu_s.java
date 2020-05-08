 package com.aaronclover.sketchescape;
 
 import java.io.DataInputStream;
 import java.io.DataOutputStream;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.Application.ApplicationType;
 import com.badlogic.gdx.Input.Keys;
 import com.badlogic.gdx.graphics.Color;
 import com.badlogic.gdx.graphics.GL10;
 import com.badlogic.gdx.graphics.OrthographicCamera;
 import com.badlogic.gdx.graphics.Texture;
 import com.badlogic.gdx.graphics.g2d.BitmapFont;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.graphics.g2d.TextureRegion;
 import com.badlogic.gdx.math.Rectangle;
 
 public class GameOverMenu extends MyScreen {
 	TextureRegion frame;
 	private Texture playButton;
 	private Texture paper;
 	final int PLAY_BUTTON_WIDTH = 300;
 	final int PLAY_BUTTON_HEIGHT = 100;
 	private Rectangle playBox;
 	private int endingScore;
 	private int lastHighScore;
 
 	public GameOverMenu(SketchEscape g) {
 		camera = new OrthographicCamera();
 		camera.setToOrtho(false, RESW, RESH);
 		spriteBatch = new SpriteBatch();
 		game = g;
 		paper = new Texture(Gdx.files.internal("data/whitepaper.png"));
 		playButton = new Texture(Gdx.files.internal("data/new game.png"));
 		playBox = new Rectangle(camera.position.x - PLAY_BUTTON_WIDTH / 2,
 				camera.position.y - PLAY_BUTTON_HEIGHT / 2, PLAY_BUTTON_WIDTH,
 				PLAY_BUTTON_HEIGHT);
 
 	}
 
 	@Override
 	public void render(float delta) {
 		Gdx.gl.glClearColor(1, 1, 1, 1);
 		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
 
 		camera.update();
 
 		spriteBatch.setProjectionMatrix(camera.combined);
 		spriteBatch.begin();
 		spriteBatch.draw(paper, 0, 0);
 		spriteBatch.draw(playButton, playBox.x, playBox.y);
 		font.draw(spriteBatch, "High Score: " + lastHighScore,
 				camera.position.x - 85 - String.valueOf(lastHighScore).length() * 7,
 				140);
 		font.draw(spriteBatch, " Score: " + String.valueOf(endingScore),
 				camera.position.x -50 - String.valueOf(endingScore).length() * 7,
 				50);
 		
 		if (MuteHandler.isMuted())
 			spriteBatch.draw(muted, muteBox.x, muteBox.y);
 		else
 			spriteBatch.draw(unmuted, muteBox.x, muteBox.y);
 		
 		spriteBatch.end();
 
 		if (Gdx.app.getType() == ApplicationType.Desktop && Gdx.input.isKeyPressed(Keys.ANY_KEY)) {
 			game.setScreen(game.getGameScreen());
 		}
 		if (Gdx.input.justTouched()) {
 			int touchX = getTouchX();
 			int touchY = getTouchY();
 			if (touchX >= playBox.x) {
 				if (touchX <= playBox.x + playBox.width) {
 					if (touchY >= playBox.y) {
 						if (touchY <= RESH - playBox.y + playBox.height) {
 							game.setScreen(game.getGameScreen());
 						}
 					}
 				}
 			}
 			if (touchX >= muteBox.x && touchY <= muteBox.y) {
 				MuteHandler.toggle();
 			}
 		}
 	}
 
 	public void setScore(int score) {
 		endingScore = score;
 		lastHighScore = getHighScore();
 		if (endingScore > lastHighScore)
 		{
 			lastHighScore = endingScore;
			setHighScore();
 		}
 	}
 	
 	private int getHighScore() {
 		int lastHighScore;
 		try {
			FileInputStream fis = new FileInputStream (Gdx.files.internal("cache").toString());
 			DataInputStream dis = new DataInputStream(fis);
 			lastHighScore = dis.readInt();
 			dis.close();
 			return lastHighScore;
 		}
 		catch (IOException e) {
 			return 0;
 		}
 	}
 	
	private void setHighScore() {
 		try {
			FileOutputStream fos = new FileOutputStream(Gdx.files.internal("cache").toString());
 			DataOutputStream dos = new DataOutputStream(fos);
			dos.writeInt(endingScore);
 			dos.close();
 		} catch (FileNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		 
 	}
 
 
 	@Override
 	public void show() {
 		super.show();
 		game.game.create();
 		System.gc();
 	}
 }
