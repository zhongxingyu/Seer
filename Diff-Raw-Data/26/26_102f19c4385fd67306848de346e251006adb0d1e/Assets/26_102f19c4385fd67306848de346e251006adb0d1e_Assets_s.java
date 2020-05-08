 package pl.fzymek.android.cycloneeye.game.cyclone;
 
 import java.io.IOException;
 
 import pl.fzymek.android.cycloneeye.R;
 import pl.fzymek.android.cycloneeye.game.engine.Audio;
 import pl.fzymek.android.cycloneeye.game.engine.Music;
 import pl.fzymek.android.cycloneeye.game.engine.Sound;
 import pl.fzymek.android.cycloneeye.game.engine.gl.Animation;
 import pl.fzymek.android.cycloneeye.game.engine.gl.Texture;
 import pl.fzymek.android.cycloneeye.game.engine.gl.TextureRegion;
 import pl.fzymek.android.cycloneeye.game.engine.impl.CEGame;
 import android.util.Log;
 import codehead.cbfg.TexFont;
 
 public class Assets {
 
 	public static final int NUMBER_OF_OBSTACLES = 16;
 	public static final int NUMBER_OF_TARGETS = 4;
 	public static final int NUMBER_OF_POWERUPS = 5;
 
 	// textures
 	public static Texture background;
 	public static Texture cyclone;
 	public static Texture text;
 	public static Texture targetsTexture;
 	public static Texture treeTexture;
 	public static Texture explosionTexture;
 	public static Texture jewelsTexture;
 
 	// texture regions
 	public static TextureRegion trees[];
 	public static TextureRegion targets[];
 	public static TextureRegion backgroundRegion;
 
 	// animations
 	public static Animation explosionAnim;
 	public static Animation powerUpAnim;
 	public static Animation cycloneAnim;
 	public static Animation jewelsAnim[];
 
 	// music
 	public static Music music;
 
 	// sounds
 
 	public static Sound collectSound;
 	public static Sound powerUpSound;
 	public static Sound explosionSound;
 	public static Sound levelUpSound;
 	public static Sound gameOverSound;
 
 	// fonts
 	public static TexFont font;
 
 
 	public static void load(final CEGame game) {
 
 		background = new Texture(game, R.drawable.grass);
 		backgroundRegion = new TextureRegion(background, 0, 0, 512, 512);
 
 		cyclone = new Texture(game, R.drawable.tornado_sprites_full);
 		float w, h;
 		w = h = 80;
 		cycloneAnim = new Animation(0.1f, 
 				new TextureRegion(cyclone, 0, 0, w, h),
 				new TextureRegion(cyclone, 80, 0, w, h),
 				new TextureRegion(cyclone, 160, 0, w, h),
 				new TextureRegion(cyclone, 80, 0, w, h),
 				new TextureRegion(cyclone, 240, 0, w, h),
 				new TextureRegion(cyclone, 400, 0, w, h),
 				new TextureRegion(cyclone, 320, 0, w, h),
 				new TextureRegion(cyclone, 400, 0, w, h),
 				new TextureRegion(cyclone, 240, 0, w, h)
 		);
 		
 		treeTexture = new Texture(game, R.drawable.tree_sprites);
 		int cntr = 0;
 		w = h = 128;
 		trees = new TextureRegion[NUMBER_OF_OBSTACLES];
 		trees[cntr++] = new TextureRegion(treeTexture, 0.0f, 0.0f, w, h);
 		trees[cntr++] = new TextureRegion(treeTexture, w, 0.0f, w, h);
 		trees[cntr++] = new TextureRegion(treeTexture, 2 * w, 0.0f, w, h);
 		trees[cntr++] = new TextureRegion(treeTexture, 3 * w, 0.0f, w, h);
 		trees[cntr++] = new TextureRegion(treeTexture, 0.0f, h, w, h);
 		trees[cntr++] = new TextureRegion(treeTexture, w, h, w, h);
 		trees[cntr++] = new TextureRegion(treeTexture, 2 * w, h, w, h);
 		trees[cntr++] = new TextureRegion(treeTexture, 3 * w, h, w, h);
 		trees[cntr++] = new TextureRegion(treeTexture, 0.0f, 2 * h, w, h);
 		trees[cntr++] = new TextureRegion(treeTexture, w, 2 * h, w, h);
 		trees[cntr++] = new TextureRegion(treeTexture, 2 * w, 2 * h, w, h);
 		trees[cntr++] = new TextureRegion(treeTexture, 3 * w, 2 * h, w, h);
 		trees[cntr++] = new TextureRegion(treeTexture, 0.0f, 3 * h, w, h);
 		trees[cntr++] = new TextureRegion(treeTexture, w, 3 * h, w, h);
 		trees[cntr++] = new TextureRegion(treeTexture, 2 * w, 3 * h, w, h);
 		trees[cntr++] = new TextureRegion(treeTexture, 3 * w, 3 * h, w, h);
 
 		// for (int i = 0; i < trees.length; i++) {
 		// Log.d("Assets - trees", trees[i].toString());
 		// }
 
 		targetsTexture = new Texture(game, R.drawable.targets);
 		targets = new TextureRegion[NUMBER_OF_TARGETS];
 
 		cntr = 0;
 		w = h = 200;
 		targets[cntr++] = new TextureRegion(targetsTexture, 0.0f, 0.0f, w, h);
 		targets[cntr++] = new TextureRegion(targetsTexture, w, 0.0f, w, h);
 		targets[cntr++] = new TextureRegion(targetsTexture, 0.0f, h, w, h);
 		targets[cntr++] = new TextureRegion(targetsTexture, w, h, w, h);
 
 		// for (int i = 0; i < targets.length; i++) {
 		// Log.d("Assets - targets", targets[i].toString());
 		// }
 
 		explosionTexture = new Texture(game, R.drawable.explosion);
 
 		w = h = 256;
 		explosionAnim = new Animation(0.1f,
 				new TextureRegion(explosionTexture,	0, 0, w, h),
 				new TextureRegion(explosionTexture, w, 0, w, h),
 				new TextureRegion(explosionTexture, 2 * w, 0, w, h),
 				new TextureRegion(explosionTexture, 3 * w, 0, w, h),
 				new TextureRegion(explosionTexture, 0, h, w, h),
 				new TextureRegion(explosionTexture, w, h, w, h),
 				new TextureRegion(explosionTexture, 2 * w, h, w, h),
 				new TextureRegion(explosionTexture, 3 * w, h, w, h)
 				);
 
 		jewelsTexture = new Texture(game, R.drawable.jewels);
 		jewelsAnim = new Animation[NUMBER_OF_POWERUPS];
 		w = 64.0f;
 		h = 256.0f / 5.0f;
 		jewelsAnim[0] = new Animation(0.1f, 
 				new TextureRegion(jewelsTexture, 0,	0, w, h), 
 				new TextureRegion(jewelsTexture, w, 0, w, h),
 				new TextureRegion(jewelsTexture, 2*w, 0, w, h),
 				new TextureRegion(jewelsTexture, 3*w, 0, w, h));
 		
 		jewelsAnim[1] = new Animation(0.1f, 
 				new TextureRegion(jewelsTexture, 0,	h, w, h), 
 				new TextureRegion(jewelsTexture, w, h, w, h),
 				new TextureRegion(jewelsTexture, 2*w, h, w, h),
 				new TextureRegion(jewelsTexture, 3*w, h, w, h));
 		
 		jewelsAnim[2] = new Animation(0.1f, 
 				new TextureRegion(jewelsTexture, 0,	2*h, w, h), 
 				new TextureRegion(jewelsTexture, w, 2*h, w, h),
 				new TextureRegion(jewelsTexture, 2*w, 2*h, w, h),
 				new TextureRegion(jewelsTexture, 3*w, 2*h, w, h));
 		
 		jewelsAnim[3] = new Animation(0.1f, 
 				new TextureRegion(jewelsTexture, 0,	3*h, w, h), 
 				new TextureRegion(jewelsTexture, w, 3*h, w, h),
 				new TextureRegion(jewelsTexture, 2*w, 3*h, w, h),
 				new TextureRegion(jewelsTexture, 3*w, 3*h, w, h));
 
 		jewelsAnim[4] = new Animation(0.1f, 
 				new TextureRegion(jewelsTexture, 0,	4 * h, w, h), 
 				new TextureRegion(jewelsTexture, w, 4 * h, w, h),
 				new TextureRegion(jewelsTexture, 2 * w, 4 * h, w, h),
 				new TextureRegion(jewelsTexture, 3 * w, 4 * h, w, h));
 
 
 		font = new TexFont(game, game.getGlGraphics().getGl());
 		try {
			font.LoadFont("fonts/Verdana.bff", game
					.getGlGraphics().getGl());
 		} catch (IOException e) {
			Log.e("Assets", "Cannot load font!");
 		}
 
 		// init sound
 		loadSounds(game);
 
 		Settings.initialize(game);
 
 	}
 
 	private static void loadSounds(final CEGame game) {
 		final Audio audio = game.getAudio();
 		collectSound = audio.newSound(R.raw.coin_collect);
 		explosionSound = audio.newSound(R.raw.explosion);
 		levelUpSound = audio.newSound(R.raw.level_up);
 		gameOverSound = audio.newSound(R.raw.game_over);
 	}
 	
 	public static void reload(final CEGame game) {
 
 		background.reload();
 		cyclone.reload();
 		treeTexture.reload();
 
 		try {
 			font.LoadFont("fonts/Verdana.bff", game.getGlGraphics().getGl());
 		} catch (IOException e) {
 			Log.e("Assets", "Cannot load font!");
 		}
 
 		// init sound
 		loadSounds(game);
 
 		Settings.initialize(game);
 	}
 
 }
