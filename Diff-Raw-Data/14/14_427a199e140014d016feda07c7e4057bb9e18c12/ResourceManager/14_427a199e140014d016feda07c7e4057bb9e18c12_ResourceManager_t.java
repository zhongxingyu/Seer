 package ua.nevis.rainyday.managers;
 
 import org.andengine.engine.Engine;
 import org.andengine.engine.camera.Camera;
 import org.andengine.opengl.texture.TextureOptions;
 import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
 import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
 import org.andengine.opengl.texture.region.ITextureRegion;
 import org.andengine.opengl.vbo.VertexBufferObjectManager;
 
 import ua.nevis.rainyday.RainyDayActivity;
 
 public class ResourceManager {
 	private static ResourceManager INSTANCE = new ResourceManager();
 	private final String GRAPHICS_PATH = "gfx/";
 	private final String IMG_SPLASH = "splash.png";
 	/*
 	 * 
 	 * */
 	public Engine engine;
 	public RainyDayActivity activity;
 	public Camera camera;
 	public VertexBufferObjectManager vboManager;
 
 	private ResourceManager() {
 
 	}
 
 	public static ResourceManager getInstance() {
 		return INSTANCE;
 	}
 
 	public static void prepareManager(Engine engine, RainyDayActivity activity, Camera camera, VertexBufferObjectManager vboManager) {
 		INSTANCE.engine = engine;
 		INSTANCE.activity = activity;
 		INSTANCE.camera = camera;
 		INSTANCE.vboManager = vboManager;
 	}
 
 	/*
 	 * Splash resource
 	 */
 	public ITextureRegion splashRegion;
 	private BitmapTextureAtlas splashTA;
 
 	public void loadSplashResource() {
 		BitmapTextureAtlasTextureRegionFactory.setAssetBasePath(GRAPHICS_PATH);
 		splashTA = new BitmapTextureAtlas(activity.getTextureManager(), 512, 512, TextureOptions.BILINEAR);
 		splashRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(splashTA, activity, IMG_SPLASH, 0, 0);
 		splashTA.load();
 	}
 
 	public void unloadSplashResource() {
 		splashTA.unload();
 		splashRegion = null;
 	}
 
 	/*
 	 * Menu resource
 	 */
 	public void loadMenuResource() {
 
 	}
 
 	public void unloadMenuResource() {
 
 	}
 }
