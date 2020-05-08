 
 package com.zarkonnen.catengine;
 
 import com.badlogic.gdx.ApplicationListener;
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.Graphics;
 import com.badlogic.gdx.audio.Music;
 import com.badlogic.gdx.audio.Sound;
 import com.badlogic.gdx.graphics.Color;
 import com.badlogic.gdx.graphics.GL10;
 import com.badlogic.gdx.graphics.OrthographicCamera;
 import com.badlogic.gdx.graphics.Texture;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.graphics.g2d.TextureRegion;
 import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
 import com.badlogic.gdx.math.Matrix4;
 import com.zarkonnen.catengine.util.Clr;
 import com.zarkonnen.catengine.util.Pt;
 import com.zarkonnen.catengine.util.Rect;
 import com.zarkonnen.catengine.util.ScreenMode;
 import java.util.ArrayList;
 import java.util.HashMap;
 
 public abstract class LibgdxEngine implements ApplicationListener, Engine, Input, Frame {
 	OrthographicCamera camera;
 	SpriteBatch batch;
 	ShapeRenderer shapeRenderer;
 	float timeAccumulated = 0;
 	boolean spriting = true;
 	
 	@Override
 	public void create() {
 		camera = new OrthographicCamera();
 		camera.setToOrtho(false, currentScreenMode.width, currentScreenMode.height);
 		batch = new SpriteBatch();
 		shapeRenderer = new ShapeRenderer();
 	}
 
 	@Override
 	public void resize(int w, int h) {
 		camera.setToOrtho(false, w, h);
 	}
 
 	@Override
 	public void render() {
 		if (game == null) { return; }
 		timeAccumulated += Gdx.graphics.getDeltaTime();
 		if (timeAccumulated * frameRate >= 1) {
 			if (currentMusic != null && !currentMusic.isPlaying() && doneCallback != null) {
 				doneCallback.run(musicName, musicVolume);
 				stopMusic();
 			}
 			game.input(this);
 		}
 		
 		Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
 		camera.update();
 		Gdx.gl.glEnable(GL10.GL_BLEND);
 		Gdx.gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
 		batch.setProjectionMatrix(camera.combined);
 		shapeRenderer.setProjectionMatrix(camera.combined);
 		spriting = true;
 		batch.begin();
 		game.render(this);
 		if (spriting) {
 			batch.end();
 		} else {
 			shapeRenderer.end();
 		}
 	}
 
 	@Override
 	public void pause() {
 		
 	}
 
 	@Override
 	public void resume() {
 		
 	}
 
 	@Override
 	public void dispose() {
 		
 	}
 	
 	public abstract void run();
 	
 	Game game;
 	String winTitle;
 	String loadBase;
 	String soundLoadBase;
 	int frameRate;
 	ScreenMode currentScreenMode = new ScreenMode(800, 600, false);
 	HashMap<String, Texture> textures = new HashMap<String, Texture>();
 	HashMap<String, Sound> sounds = new HashMap<String, Sound>();
 	Music currentMusic;
 	String musicName;
 	double musicVolume;
 	MusicDone doneCallback;
 	
 	public LibgdxEngine(String winTitle, String loadBase, String soundLoadBase, Integer frameRate) {
 		this.winTitle = winTitle;
 		this.loadBase = loadBase;
 		this.soundLoadBase = soundLoadBase;
 		this.frameRate = frameRate;
 	}
 
 	@Override
 	public void setup(Game g) {
 		game = g;
 	}
 
 	@Override
 	public void runUntil(Condition u) {
 		run();
 	}
 
 	@Override
 	public void destroy() {
 		Gdx.app.exit();
 	}
 
 	@Override
 	public boolean keyDown(String key) {
 		try {
 			return Gdx.input.isKeyPressed(com.badlogic.gdx.Input.Keys.class.getField(key).getInt(null));
 		} catch (Exception e) {
 			//e.printStackTrace();
 			return false;
 		}
 	}
 
 	@Override
 	public boolean keyPressed(String key) {
 		throw new UnsupportedOperationException("Not supported yet.");
 	}
 
 	@Override
 	public Pt cursor() {
 		return new Pt(Gdx.input.getX(), Gdx.input.getY());
 	}
 
 	@Override
 	public Pt click() {
 		return clickButton() > 0 ? cursor() : null;
 	}
 
 	@Override
 	public int clickButton() {
 		for (int i = 0; i < 3; i++) {
 			if (Gdx.input.isButtonPressed(i)) { return i + 1; }
 		}
 		return 0;
 	}
 
 	@Override
 	public ScreenMode mode() {
 		return currentScreenMode;
 	}
 
 	@Override
 	public Input setMode(ScreenMode mode) {
 		if (Gdx.graphics.setDisplayMode(mode.width, mode.height, mode.fullscreen)) {
 			currentScreenMode = mode;
 		}
 		return this;
 	}
 
 	@Override
 	public ArrayList<ScreenMode> modes() {
 		ArrayList<ScreenMode> sms = new ArrayList<ScreenMode>();
 		sms.add(new ScreenMode(800, 600, false));
 		for (Graphics.DisplayMode dm : Gdx.graphics.getDisplayModes()) {
 			sms.add(new ScreenMode(dm.width, dm.height, true));
 		}
 		return sms;
 	}
 
 	@Override
 	public boolean isCursorVisible() {
 		return Gdx.input.isCursorCatched();
 	}
 
 	@Override
 	public Input setCursorVisible(boolean visible) {
 		Gdx.input.setCursorCatched(!visible);
 		return this;
 	}
 
 	@Override
 	public void play(String sound, double pitch, double volume, double x, double y) {
 		getSound(sound).play((float) volume, (float) pitch, x < -1 ? -1f : x > 1 ? 1f : (float) x);
 	}
 	
 	Sound getSound(String name) {
 		if (!name.contains(".")) { name += ".ogg"; }
 		if (!sounds.containsKey(name)) {
 			sounds.put(name, Gdx.audio.newSound(Gdx.files.internal(soundLoadBase + name)));
 		}
 		return sounds.get(name);
 	}
 
 	@Override
 	public void playMusic(String music, double volume, MusicDone doneCallback) {
 		if (!music.contains(".")) { music += ".ogg"; }
 		stopMusic();
 		musicName = music;
 		musicVolume = volume;
 		currentMusic = Gdx.audio.newMusic(Gdx.files.internal(soundLoadBase + music));
 		currentMusic.setVolume((float) volume);
 		currentMusic.play();
 	}
 
 	@Override
 	public void stopMusic() {
 		if (currentMusic != null) {
 			currentMusic.stop();
 			currentMusic.dispose();
 			currentMusic = null;
 		}
 	}
 
 	@Override
 	public void quit() {
 		destroy();
 	}
 
 	@Override
 	public int fps() {
 		return Gdx.graphics.getFramesPerSecond();
 	}
 
 	@Override
 	public Rect rect(Clr c, double x, double y, double width, double height, double angle) {
 		if (spriting) {
 			batch.end();
 			shapeRenderer.begin(ShapeRenderer.ShapeType.FilledRectangle);
 			spriting = false;
 		}
 		if (c.a != 255) {
 			shapeRenderer.end();
 			Gdx.gl.glEnable(GL10.GL_BLEND);
 			Gdx.gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
 			shapeRenderer.setProjectionMatrix(camera.combined);
 			shapeRenderer.begin(ShapeRenderer.ShapeType.FilledRectangle);
 		}
 		shapeRenderer.setColor(c.r / 255f, c.g / 255f, c.b / 255f, c.a / 255f);
 		if (angle != 0) {
 			shapeRenderer.translate((float) x, (float) (currentScreenMode.height - y - height), 0);
 			shapeRenderer.rotate(0, 0, 1, (float) angle);
 			shapeRenderer.filledRect(0, 0, (float) width, (float) height);
 			shapeRenderer.identity();
 		} else {
 			shapeRenderer.filledRect((float) x, (float) (currentScreenMode.height - y - height), (float) width, (float) height);
 		}
 		if (c.a != 255) {
 			shapeRenderer.end();
 			Gdx.gl.glDisable(GL10.GL_BLEND);
 			shapeRenderer.begin(ShapeRenderer.ShapeType.FilledRectangle);
 		}
 		return new Rect(x, y, width, height);
 	}
 
 	@Override
 	public Rect blit(String img, Clr tint, double x, double y, double width, double height, double angle) {
 		if (!spriting) {
 			shapeRenderer.end();
 			batch.begin();
 			spriting = true;
 		}
 		if (tint != null) {
 			batch.setColor(tint.r / 255f, tint.g / 255f, tint.b / 255f, tint.a / 255f);
 		} else {
 			batch.setColor(Color.WHITE);
 		}
 		float fy = currentScreenMode.height - (float) y;
 		Texture tx = getTexture(img);
 		if (height == 0) {
 			fy -= tx.getHeight();
 		} else {
 			fy -= height;
 		}
 		if (width != 0 || height != 0 || angle != 0) {
 			batch.setTransformMatrix(new Matrix4().
 					idt().
 					translate((float) x, fy, 0).
 					rotate(0f, 0f, 1f, (float) angle).
 					scale(width == 0 ? 1 : (float) (width / tx.getWidth()), height == 0 ? 1 : (float) (height / tx.getHeight()), 1f));
 			batch.draw(tx, 0f, 0f);
 			batch.setTransformMatrix(new Matrix4().idt());
 		} else {
 			batch.draw(tx, (float) x, fy);
 		}
 		return new Rect(x, y, width, height);
 	}
 	
 	Texture getTexture(String img) {
 		if (!img.contains(".")) { img += ".png"; }
 		if (!textures.containsKey(img)) {
 			textures.put(img, new Texture(Gdx.files.internal(loadBase + img)));
 		}
 		return textures.get(img);
 	}
 }
