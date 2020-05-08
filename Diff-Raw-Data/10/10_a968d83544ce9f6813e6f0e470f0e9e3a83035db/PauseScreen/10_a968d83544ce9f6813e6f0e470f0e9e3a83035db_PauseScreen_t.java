 package ch.kanti_wohlen.asteroidminer.screen;
 
 import ch.kanti_wohlen.asteroidminer.AsteroidMiner;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.graphics.Color;
 import com.badlogic.gdx.graphics.Pixmap;
 import com.badlogic.gdx.graphics.Pixmap.Format;
 import com.badlogic.gdx.graphics.Texture;
 import com.badlogic.gdx.graphics.g2d.Sprite;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 
 public class PauseScreen extends AbstractScreen {
 
 	private final AsteroidMiner game;
 	private final SpriteBatch batch;
 	private final Sprite overlay;
 
 	public PauseScreen(AsteroidMiner asteroidMiner) {
 		game = asteroidMiner;
 		batch = game.getSpriteBatch();
 
 		Pixmap map = new Pixmap(1, 1, Format.RGBA8888);
 		map.drawPixel(0, 0, Color.rgba8888(255f, 255f, 255f, 192f));
 		overlay = new Sprite(new Texture(map));
 		map.dispose();
 	}
 
 	@Override
 	public void render(float delta) {
 		game.getScreens().GAME_SCREEN.renderGame();
 
 		// Draw overlay
 		overlay.setSize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		batch.end();
		batch.begin();
 		overlay.draw(batch);
 	}
 
 	@Override
 	public void resize(int width, int height) {
 
 	}
 
 	@Override
 	public void show() {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void hide() {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void pause() {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void resume() {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void dispose() {
 		// TODO Auto-generated method stub
 
 	}
 
 }
