 package ch.zhaw.arsphema.screen;
 
 import ch.zhaw.arsphema.MyGdxGame;
 import ch.zhaw.arsphema.util.Paths;
 import ch.zhaw.arsphema.util.Sizes;
 import ch.zhaw.arsphema.util.SpaceAssetManager;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.audio.Music;
 import com.badlogic.gdx.audio.Sound;
 import com.badlogic.gdx.graphics.GL10;
 import com.badlogic.gdx.graphics.Texture;
 import com.badlogic.gdx.graphics.g2d.Animation;
 import com.badlogic.gdx.graphics.g2d.BitmapFont;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.graphics.g2d.TextureRegion;
 
 public class LoadingScreen extends AbstractScreen {
 
 	private static final int FRAME_COLS = 12;
 	private static final int FRAME_ROWS = 1;
 	private SpaceAssetManager assetManager;
 	private float progress;
 	private Texture loader;
 	private SpriteBatch batch;
 	private float ppuX;
 	private float ppuY;
 	private TextureRegion[] loaderFrames;
 	private Animation loaderAnimation;
 	private float stateTime;
 
 	public LoadingScreen(MyGdxGame game) {
 		super(game);
 		assetManager = SpaceAssetManager.getInstance();
 	}
 
 	@Override
 	public void show() {
 		assetManager.load(Paths.HERO, Texture.class);
 		assetManager.load(Paths.OVERLAY_SPRITE, Texture.class);
 		assetManager.load(Paths.BUTTON_TEXTURE, Texture.class);
 		assetManager.load(Paths.ENEMY_UFO, Texture.class);
 		assetManager.load(Paths.ENEMY_SAUCER, Texture.class);
 		assetManager.load(Paths.ENEMY_BLOB, Texture.class);
 		assetManager.load(Paths.SPACE_FONT, BitmapFont.class);
 		assetManager.load(Paths.BUTTON_FONT, BitmapFont.class);
 		assetManager.load(Paths.HEADER_FONT, BitmapFont.class);
 		assetManager.load(Paths.OVERHEATBAR, Texture.class);
 		assetManager.load(Paths.SHOT, Texture.class);
 		assetManager.load(Paths.BACKGROUND_STARS, Texture.class);
 		assetManager.load(Paths.MUSIC_AMBIENTE, Music.class);
 		assetManager.load(Paths.SFX_SHOT, Sound.class);
 		assetManager.load(Paths.SFX_DANGER, Sound.class);
 		assetManager.load(Paths.SFX_EXPLOSION, Sound.class);
 		assetManager.load(Paths.SFX_HURT, Sound.class);
 		assetManager.load(Paths.SFX_BEEP, Sound.class);
 		assetManager.load(Paths.PLANETS, Texture.class);
 
 		batch = new SpriteBatch();
 		loader = new Texture(Gdx.files.internal("images/loader.png"));
 
 		TextureRegion[][] tmp = TextureRegion.split(loader, loader.getWidth()
 				/ FRAME_COLS, loader.getHeight() / FRAME_ROWS); // #10
 		loaderFrames = new TextureRegion[FRAME_COLS * FRAME_ROWS];
 		int index = 0;
 		for (int i = 0; i < FRAME_ROWS; i++) {
 			for (int j = 0; j < FRAME_COLS; j++) {
 				loaderFrames[index++] = tmp[i][j];
 			}
 		}
 		loaderAnimation = new Animation(0.025f, loaderFrames); // #11
 		loaderAnimation.setPlayMode(Animation.LOOP);

 		stateTime = 0f;
 	}
 
 	@Override
 	public void resize(int width, int height) {
 		super.resize(width, height);
 
 		ppuX = (float) width / Sizes.DEFAULT_WORLD_WIDTH;
 		ppuY = (float) height / Sizes.DEFAULT_WORLD_HEIGHT;
 	}
 
 	@Override
 	public void render(float delta) {
 		Gdx.gl.glClearColor(0f, 0f, 0f, 0);
 		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
 		stateTime += delta;
 		batch.begin();
 		batch.draw(loaderAnimation.getKeyFrame(stateTime), ppuX * (Sizes.DEFAULT_WORLD_WIDTH / 2 - 5), ppuY
 				* (Sizes.DEFAULT_WORLD_HEIGHT / 2 - 2), ppuX * 10, ppuY * 4f);
 		batch.end();
 
 		progress = assetManager.getProgress();
 		System.out.println(progress);
 		if (assetManager.update()) {
 			game.initScreens();
 			game.setScreen(game.getMainMenuScreen());
 		}
 
 	}
 
 }
