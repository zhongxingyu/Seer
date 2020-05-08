 package fr.odai.zerozeroduck;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.audio.Music;
 import com.badlogic.gdx.graphics.Color;
 import com.badlogic.gdx.graphics.OrthographicCamera;
 import com.badlogic.gdx.graphics.g2d.Animation;
 import com.badlogic.gdx.graphics.g2d.BitmapFont;
 import com.badlogic.gdx.graphics.g2d.ParticleEffect;
 import com.badlogic.gdx.graphics.g2d.ParticleEffectPool;
 import com.badlogic.gdx.graphics.g2d.ParticleEffectPool.PooledEffect;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.graphics.g2d.TextureAtlas;
 import com.badlogic.gdx.graphics.g2d.TextureRegion;
 import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
 import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
 import com.badlogic.gdx.math.Rectangle;
 import com.badlogic.gdx.scenes.scene2d.ui.Label;
 import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
 import com.badlogic.gdx.scenes.scene2d.utils.Align;
 import com.badlogic.gdx.utils.Array;
 
 import fr.odai.zerozeroduck.model.Duck;
 import fr.odai.zerozeroduck.model.Patate;
 import fr.odai.zerozeroduck.model.Patate.State;
 import fr.odai.zerozeroduck.model.Trap;
 import fr.odai.zerozeroduck.model.Unit;
 import fr.odai.zerozeroduck.model.World;
 import fr.odai.zerozeroduck.sound.FouleSound;
 import fr.odai.zerozeroduck.utils.Util;
 
 public class WorldRenderer {
 	private World world;
 	private OrthographicCamera cam;
 
 	private static final float CAMERA_WIDTH = 10f;
 	private static final float CAMERA_HEIGHT = 7f;
 
 	boolean debug = false;
 
 	private SpriteBatch spriteBatch;
 
 	/* Nos textures */
 	private TextureRegion backgroundTexture;
 	private TextureRegion duckTexture;
 	private TextureRegion trapTexture;
 
 	/* Nos particules */
 	Array<PooledEffect> effects;
 	
 	/* Musique */
 	Music musicLoop;
 	Music musicStart;
 	
 	/** Sounds */
 	private FouleSound ogmsound;
 
 	/** for debug rendering **/
 	ShapeRenderer debugRenderer = new ShapeRenderer();
 
 	private float ppuX; // pixels per unit on the X axis
 	private float ppuY; // pixels per unit on the Y axis
 
 	private int width;
 	private int height;
 
 
 	private boolean musicStopped=false;
 
 	public void setSize(int w, int h) {
 		this.width = w;
 		this.height = h;
 		Util.screenWidth=w;
 		Util.screenHeight=h;
 		ppuX = (float) width / CAMERA_WIDTH;
 		ppuY = (float) height / CAMERA_HEIGHT;
 	}
 
 	public WorldRenderer(World world, boolean debug) {
 		this.world = world;
 		this.cam = new OrthographicCamera(CAMERA_WIDTH, CAMERA_HEIGHT);
 		this.cam.position.set(CAMERA_WIDTH / 2f, CAMERA_HEIGHT / 2f, 0);
 		this.cam.update();
 		this.debug = debug;
 		spriteBatch = new SpriteBatch();
 		effects = new Array<PooledEffect>();
 
 		//sounds
 		//Sound sound = Gdx.audio.newSound(Gdx.files.internal("data/mysound.mp3"));
 		//the ambiance music
 		musicStart = Gdx.audio.newMusic(Gdx.files.internal("musics/duckIntro01.ogg"));
 		musicStart.setLooping(false);
 		musicLoop = Gdx.audio.newMusic(Gdx.files.internal("musics/duckBoucle01.ogg"));
 		musicLoop.setLooping(true);
 		
 		musicStart.play();
 		
 		this.ogmsound = new FouleSound(); 
 		loadTextures();
 	}
 
 	public void render(float delta) {
 		if(!musicStart.isPlaying() && !musicLoop.isPlaying() && !musicStopped){
 			musicLoop.play();
 		}
 		
 		spriteBatch.begin();
 		drawBackground();
 		drawTrap();
 		drawDuck();
 		drawUnits();
 		drawScore();
 
 		// Update and draw effects:
 		for (int i = 0; i < effects.size; i++) {
 			PooledEffect effect = effects.get(i);
 			if (effect.isComplete()) {
 				effects.removeIndex(i);
 				i--;
 			} else
 				effect.draw(spriteBatch, Gdx.graphics.getDeltaTime());
 		}
 
 		spriteBatch.end();
 		
 		ogmsound.update(delta, world);
 		if(ogmsound.shouldBeStarted(world, delta)) ogmsound.start();
 
 		// draw bounding boxes in debug mode
 		if (debug)
 			drawDebug();
 	}
 	
 	public void stopMusic(){
 		musicStopped=true;
 		musicStart.stop();
 		musicLoop.stop();
 		ogmsound.stop();
 	}
 
 	private void drawBackground() {
 		spriteBatch.draw(backgroundTexture, 0.f, 0.f, 10.f * ppuX, 7.f * ppuY);
 	}
 
 	private void loadTextures() {
 		duckTexture = world.getAtlas().findRegion("Kanard");
 		trapTexture = world.getAtlas().findRegion("trap");
 		backgroundTexture = world.getAtlas().findRegion("Stage0");
 	}
 
 	private void drawUnits() {
 		Array<Unit> units = world.getUnits();
 		for (int i = 0; i < units.size; i++) {
 			units.get(i).draw(spriteBatch, effects, debugRenderer,ppuX, ppuY);
 			if (units.get(i).isToBeRemoved()) {	
 				units.removeIndex(i);
 				i--;
 			}
 		}
 	}
 
 	private void drawDuck() {
 		Duck ducky = world.getDuck();
 		spriteBatch.draw(duckTexture, ducky.getPosition().x * ppuX,
 				ducky.getPosition().y * ppuY, ducky.getBounds().width * ppuX, 
 				ducky.getBounds().height * ppuY);
 	}
 
 	private void drawTrap() {
 		for (Trap trap : world.getTraps()) {
 			trap.draw(spriteBatch, ppuX, ppuY, debugRenderer, cam);
 		}
 	}
 
 	private void drawScore() {
 		LabelStyle text = new LabelStyle(new BitmapFont(), Color.WHITE);
 		Label label = new Label("Score: " + world.getScore(), text);
		label.setPosition(8.5f * ppuX, 6.5f * ppuY);
 		label.setAlignment(Align.center);
 		label.draw(spriteBatch, 1);
		Label label2 = new Label("Life: " + world.getDuck().getLife(), text);
		label2.setPosition(8.5f * ppuX, 6f * ppuY);
		label2.setAlignment(Align.center);
		label2.draw(spriteBatch, 1);
 	}
 
 	private void drawDebug() {
 		// render blocks
 		debugRenderer.setProjectionMatrix(cam.combined);
 		debugRenderer.begin(ShapeType.FilledRectangle);
 		debugRenderer.setColor(new Color(1, 0, 0, 1));
 		float size = 0.02f;
 		for (float i = 0; i < 10; i += 0.05f) {
 			float x1 = i;
 			float y1 = world.getFloorHeight(i);
 			debugRenderer.filledRect(x1 - size / 2, y1 - size / 2, size, size);
 		}
 		debugRenderer.end();
 		debugRenderer.begin(ShapeType.Rectangle);
 		for (Unit unit : world.getUnits()) {
 			Rectangle rect = unit.getBounds();
 			float x1 = unit.getPosition().x + rect.x;
 			float y1 = unit.getPosition().y + rect.y;
 			debugRenderer.setColor(new Color(1, 1, 0, 1));
 			debugRenderer.rect(x1, y1, rect.width, rect.height);
 		}
 		for (Trap trap : world.getTraps()) {
 			Rectangle bounds = trap.getBounds();
 			Rectangle rect = new Rectangle(bounds.x - (bounds.width / 2),
 					bounds.y - (bounds.height / 2), bounds.width * 2,
 					bounds.height * 2);
 			float x1 = rect.x;
 			float y1 = rect.y;
 			debugRenderer.setColor(new Color(1, 1, 0, 1));
 			debugRenderer.rect(x1, y1, rect.width, rect.height);
 		}
 		// render Duck
 		Duck duck = world.getDuck();
 		Rectangle rect = duck.getBounds();
 		float x1 = duck.getPosition().x + rect.x;
 		float y1 = duck.getPosition().y + rect.y;
 		debugRenderer.setColor(new Color(0, 1, 0, 1));
 		debugRenderer.rect(x1, y1, rect.width, rect.height);
 		debugRenderer.end();
 	}
 
 	public float convertScaleX(int x) {
 		return x / ppuX;
 	}
 
 	public float convertScaleY(int y) {
 		return y / ppuY;
 	}
 }
