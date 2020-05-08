 #set( $symbol_pound = '#' )
 #set( $symbol_dollar = '$' )
 #set( $symbol_escape = '\' )
 package ${package}.core;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.graphics.Color;
 import com.badlogic.gdx.graphics.GL10;
 import com.badlogic.gdx.graphics.g2d.Sprite;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.graphics.g2d.TextureAtlas;
 import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
 
 public class LoadingScreen extends Screen {
 
 	private SpriteBatch batch;
 
 	private Sprite loadingText;
 	private Sprite loadingBorder;
 	private ShapeRenderer loadingBarRenderer;
 
 	private static final float fadeTime = 0.2f;
 	private float fadeTimeLeft = fadeTime;
 
 	private Screen nextScreen;
 
 	public LoadingScreen(final Game game) {
 		super(game);
 	}
 
 	public Screen setNextScreen(final Screen nextScreen) {
 		this.nextScreen = nextScreen;
 		return this;
 	}
 
 	@Override
 	public void show() {
 		Asset.queueAssets(assetManager());
 
 		final boolean assetsAlreadyLoaded = assetManager().update();
 		if (assetsAlreadyLoaded) {
 			nextScreen();
 			return;
 		}
 
 		batch = new SpriteBatch();
 		loadingBarRenderer = new ShapeRenderer();
 	}
 
 	@Override
 	public void render(final float delta) {
 		final boolean finishedLoading = assetManager().update();
 		final float fadeAlpha = fadeTimeLeft / fadeTime;
 		final Color fadeColour = new Color(fadeAlpha, fadeAlpha, fadeAlpha, 1);
 
 		Gdx.gl.glClearColor(0, 0, 0, 0);
 		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
 
 		boolean spritesLoaded = loadingText != null && loadingBorder != null;
 		if (!spritesLoaded) {
 			spritesLoaded = tryLoadSprites();
 		}
 
 		if (spritesLoaded) {
 			drawSprites(fadeColour);
 			drawLoadingBar(fadeColour);
 		}
 
 		if (fadeTimeLeft < 0) {
 			nextScreen();
 		}
 		if (finishedLoading) {
 			fadeTimeLeft -= delta;
 		}
 	}
 
 	private boolean tryLoadSprites() {
 		if (assetManager().isLoaded(Asset.loadingAtlas)) {
 			final TextureAtlas atlas = assetManager().get(Asset.loadingAtlas);
 			loadingText = atlas.createSprite(Asset.loadingText);
 			loadingText.setPosition(80, 224);
 			loadingBorder = atlas.createSprite(Asset.loadingBorder);
 			loadingBorder.setPosition(144, 80);
 			return true;
 		} else {
 			return false;
 		}
 	}
 
 	private void drawSprites(final Color fadeColour) {
 		batch.begin();
 		loadingText.setColor(fadeColour);
 		loadingText.draw(batch);
 		loadingBorder.setColor(fadeColour);
 		loadingBorder.draw(batch);
 		batch.end();
 	}
 
 	private void drawLoadingBar(final Color fadeColour) {
 		final float progress = assetManager().getProgress();
 		final int width = (int) Math.ceil(480 * progress);
 		loadingBarRenderer.begin(ShapeRenderer.ShapeType.Filled);
 		loadingBarRenderer.setColor(fadeColour);
		loadingBarRenderer.rect(160, 97, width, 32);
 		loadingBarRenderer.end();
 	}
 
 	private void nextScreen() {
 		game().setScreen(nextScreen);
 	}
 
 }
