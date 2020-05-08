 package com.gemserk.games.taken;
 
 import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
 import com.badlogic.gdx.graphics.Color;
 import com.badlogic.gdx.graphics.GL10;
 import com.badlogic.gdx.graphics.g2d.BitmapFont;
 import com.badlogic.gdx.graphics.g2d.BitmapFont.TextBounds;
 import com.badlogic.gdx.graphics.g2d.Sprite;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.math.Rectangle;
 import com.gemserk.animation4j.transitions.Transitions;
 import com.gemserk.animation4j.transitions.sync.Synchronizers;
 import com.gemserk.commons.gdx.ScreenAdapter;
 import com.gemserk.commons.gdx.graphics.SpriteBatchUtils;
 import com.gemserk.commons.gdx.math.MathUtils2;
 import com.gemserk.commons.gdx.resources.LibgdxResourceBuilder;
 import com.gemserk.componentsengine.input.ButtonMonitor;
 import com.gemserk.resources.ResourceManager;
 import com.gemserk.resources.ResourceManagerImpl;
 
 public class PauseScreen extends ScreenAdapter {
 
 	private final LibgdxGame game;
 
 	private ScreenAdapter targetScreen;
 
 	private SpriteBatch spriteBatch;
 
 	private ResourceManager<String> resourceManager;
 
 	private Sprite overlay;
 
 	private BitmapFont font;
 
 	private Color overlayColor;
 
 	private TextButton resumeButton;
 
 	private TextButton menuButton;
 
 	public PauseScreen(LibgdxGame game) {
 		this.game = game;
 	}
 
 	@Override
 	public void show() {
 		super.show();
 
 		this.spriteBatch = new SpriteBatch();
 
 		resourceManager = new ResourceManagerImpl<String>();
 
 		loadResources();
 
 		overlay = resourceManager.getResourceValue("OverlaySprite");
 		font = resourceManager.getResourceValue("PauseFont");
 
 		int width = Gdx.graphics.getWidth();
 		int height = Gdx.graphics.getHeight();
 
 		resumeButton = new TextButton(font, "Resume", width * 0.5f, height * 0.5f + 40f);
 		menuButton = new TextButton(font, "Menu", width * 0.5f, height * 0.5f - 40f);
 
 		overlayColor = new Color();
 
 		Synchronizers.transition(overlayColor, Transitions.transitionBuilder(new Color(0f, 0f, 0f, 0f)).end(new Color(0f, 0f, 0f, 0.3f)).time(500).build());
 
 		Gdx.input.setCatchBackKey(true);
 	}
 
 	@Override
 	public void hide() {
 		super.hide();
 		Gdx.input.setCatchBackKey(false);
 		Gdx.input.setInputProcessor(null);
 	}
 
 	public static class TextButton extends ButtonMonitor {
 
 		private float x, y;
 
 		private BitmapFont font;
 
 		private String text;
 
 		private Rectangle bounds;
 
 		public TextButton(BitmapFont font, String text, float x, float y) {
 			this.font = font;
 			this.text = text;
 			this.x = x;
 			this.y = y;
 
 			TextBounds bounds = font.getBounds(text);
 
 			float w = bounds.width;
 			float h = bounds.height;
 
 			this.bounds = new Rectangle(x - w * 0.5f, y - h * 0.5f, w, h);
 		}
 
 		public void draw(SpriteBatch spriteBatch) {
 			SpriteBatchUtils.drawCentered(spriteBatch, font, text, x, y);
 		}
 
 		@Override
 		protected boolean isDown() {
 			if (!Gdx.input.isTouched())
 				return false;
 
 			float x2 = Gdx.input.getX();
 			float y2 = Gdx.graphics.getHeight() - Gdx.input.getY();
 
 			return MathUtils2.inside(bounds, x2, y2);
 		}
 
 	}
 
 	public void internalRender(float delta) {
 		int width = Gdx.graphics.getWidth();
 		int height = Gdx.graphics.getHeight();
 
 		Gdx.graphics.getGL10().glClear(GL10.GL_COLOR_BUFFER_BIT);
 
 		game.gameScreen.internalRender(delta);
 
 		if (spriteBatch == null)
 			return;
 
 		spriteBatch.begin();
 
 		resumeButton.draw(spriteBatch);
 		menuButton.draw(spriteBatch);
 		
 		overlay.setSize(width, height);
 		overlay.setPosition(0, 0);
 		overlay.setColor(overlayColor);
 		overlay.draw(spriteBatch);
 
 		spriteBatch.end();
 	}
 
 	@Override
 	public void internalUpdate(float delta) {
 		Synchronizers.synchronize();
 
 		resumeButton.update();
 		menuButton.update();
 
 		// truchex, we should have a transition handler -> onTransitionFinished or something...
 		if (overlayColor.a == 0f || overlayColor.a == 1f) {
 			game.setScreen(targetScreen, true);
 			// forcing game screen disposing :S
 			if (targetScreen == game.menuScreen) {
 				game.gameScreen.setGameOver(true);
 				game.gameScreen.dispose();
 			}
 		}
 
		if (resumeButton.isReleased() || Gdx.input.isKeyPressed(Keys.BACK)) {
 			targetScreen = game.gameScreen;
 			Synchronizers.transition(overlayColor, Transitions.transitionBuilder(overlayColor).end(new Color(0f, 0f, 0f, 0f)).time(300).build());
 		}
 
 		if (menuButton.isReleased()) {
 			targetScreen = game.menuScreen;
 			Synchronizers.transition(overlayColor, Transitions.transitionBuilder(overlayColor).end(new Color(0f, 0f, 0f, 1f)).time(300).build());
 		}
 	}
 
 	private void loadResources() {
 		new LibgdxResourceBuilder(resourceManager) {
 			{
 				texture("OverlayTexture", "data/images/white-rectangle.png");
 				sprite("OverlaySprite", "OverlayTexture");
 				font("PauseFont", "data/fonts/title.png", "data/fonts/title.fnt");
 			}
 		};
 	}
 
 	@Override
 	public void dispose() {
 		resourceManager.unloadAll();
 		spriteBatch.dispose();
 		spriteBatch = null;
 		Gdx.app.log("Taken", "PauseScreen resources disposed");
 	}
 
 }
