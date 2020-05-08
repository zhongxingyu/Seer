 package yuuki.content;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.zip.ZipFile;
 
 import yuuki.entity.EntityFactory;
 import yuuki.file.ResourceNotFoundException;
 import yuuki.graphic.ImageFactory;
 import yuuki.sound.DualSoundEngine;
 import yuuki.sound.EffectEngine;
 import yuuki.sound.MusicEngine;
 import yuuki.util.Progressable;
 import yuuki.world.World;
 
 /**
  * Handles content and loaders for Yuuki.
  */
 public class ContentManager {
 	
 	/**
 	 * Represents the current model of all loaded content.
 	 */
 	private Content contentModel;
 	
 	/**
 	 * Handles sound effect content.
 	 */
 	private EffectEngine effectEngine;
 	
 	/**
 	 * Handles Entity creation.
 	 */
 	private EntityFactory entityFactory;
 	
 	/**
 	 * Handles image content.
 	 */
 	private ImageFactory imageFactory;
 	
 	/**
 	 * Handles music content.
 	 */
 	private MusicEngine musicEngine;
 	
 	/**
 	 * The content packs.
 	 */
 	private Map<String, ContentPack> packs;
 	
 	/**
 	 * Creates a new ContentManager.
 	 */
 	public ContentManager() {
 		packs = new HashMap<String, ContentPack>();
 		contentModel = new Content();
 		contentModel.init();
 		effectEngine = new EffectEngine();
 		musicEngine = new MusicEngine();
 		imageFactory = new ImageFactory();
 		entityFactory = new EntityFactory();
 	}
 	
 	/**
 	 * Disables a ContentPack.
 	 * 
 	 * @param id The identifier of the content pack to enable.
 	 */
 	public void disable(String name) {
 		ContentPack pack = packs.get(name);
 		if (!pack.isEnabled()) {
 			return;
 		}
 		Content c = pack.getContent();
 		if (pack.hasEffects() && pack.hasEffectDefinitions()) {
 			effectEngine.subtract(c.getEffects());
 		}
 		if (pack.hasMusic() && pack.hasMusicDefinitions()) {
 			musicEngine.subtract(c.getMusic());
 		}
 		if (pack.hasEntities()) {
 			entityFactory.subtract(c.getEntities());
 		}
 		if (pack.hasImages() && pack.hasImageDefinitions()) {
 			imageFactory.subtract(c.getImages());
 		}
 		contentModel.subtract(c);
		pack.setEnabled(false);
 	}
 	
 	/**
 	 * Enables a ContentPack. The content pack must already be loaded, or a
 	 * null pointer will be encountered. This method has no effect if the
 	 * content pack is already enabled.
 	 * 
 	 * @param id The identifier of the content pack to enable.
 	 */
 	public void enable(String name) {
 		ContentPack pack = packs.get(name);
 		if (pack.isEnabled()) {
 			return;
 		}
 		Content c = pack.getContent();
 		if (pack.hasEffects() && pack.hasEffectDefinitions()) {
 			effectEngine.merge(c.getEffects());
 		}
 		if (pack.hasMusic() && pack.hasMusicDefinitions()) {
 			musicEngine.merge(c.getMusic());
 		}
 		if (pack.hasEntities()) {
 			entityFactory.merge(c.getEntities());
 		}
 		if (pack.hasImages() && pack.hasImageDefinitions()) {
 			imageFactory.merge(c.getImages());
 		}
 		contentModel.merge(c);
		pack.setEnabled(true);
 	}
 	
 	/**
 	 * Gets the entity factory that this ContentManager controls.
 	 * 
 	 * @return The entity factory.
 	 */
 	public EntityFactory getEntityFactory() {
 		return entityFactory;
 	}
 	
 	/**
 	 * Gets the image factory that this ContentManager controls.
 	 * 
 	 * @return The image factory.
 	 */
 	public ImageFactory getImageFactory() {
 		return imageFactory;
 	}
 	
 	/**
 	 * Gets the sound engine that this ContentManager controls.
 	 * 
 	 * @return The sound engine.
 	 */
 	public DualSoundEngine getSoundEngine() {
 		return new DualSoundEngine(effectEngine, musicEngine);
 	}
 	
 	/**
 	 * Gets the world engine built of of enabled content packs that this
 	 * ContentManager controls.
 	 * 
 	 * @return The world engine.
 	 */
 	public World getWorldEngine() {
 		World world = new World();
 		for (ContentPack cp : packs.values()) {
 			if (cp.isEnabled() && cp.hasWorld() && cp.hasLands() &&
 					cp.mapDataIsLoaded()) {
 				world.merge(cp.getContent().getLands());
 			}
 		}
 		return world;
 	}
 	
 	/**
 	 * Loads the map data in all content packs that are enabled.
 	 * 
 	 * @throws ResourceNotFoundException 
 	 * @throws IOException 
 	 */
 	public void loadEnabledWorlds() throws ResourceNotFoundException,
 	IOException {
 		for (String id : packs.keySet()) {
 			ContentPack c = packs.get(id);
 			if (c.isEnabled() && c.hasWorld() && c.hasLands() &&
 					!c.mapDataIsLoaded()) {
 				loadWorld(id);
 			}
 		}
 	}
 	
 	/**
 	 * Loads all non-map content in a content pack.
 	 * 
 	 * @param name The name of the content pack.
 	 * @throws IOException
 	 * @throws ResourceNotFoundException
 	 */
 	public void loadAssets(String name) throws ResourceNotFoundException,
 	IOException {
 		packs.get(name).loadAssets(contentModel);
 	}
 	
 	/**
 	 * Loads all map content in a content pack.
 	 * 
 	 * @param name The name of the content pack.
 	 * @throws IOException
 	 * @throws ResourceNotFoundException
 	 */
 	public void loadWorld(String name) throws ResourceNotFoundException,
 	IOException {
 		packs.get(name).loadWorldAssets(contentModel);
 	}
 	
 	/**
 	 * Initializes a ContentPack and reads its manifest.
 	 * 
 	 * @param id What name to identify the content pack with.
 	 * @param file The archive or directory that contains the content pack's
 	 * resources.
 	 * 
 	 * @throws IOException
 	 * @throws ResourceNotFoundException
 	 */
 	public void scan(String id, File file) throws ResourceNotFoundException,
 	IOException {
 		ContentPack pack = null;
 		if (file.isDirectory()) {
 			pack = new ContentPack(file);
 		} else {
 			ZipFile z = new ZipFile(file);
 			pack = new ContentPack(z);
 			z.close();
 		}
 		if (pack != null) {
 			packs.put(id, pack);
 		}
 	}
 	
 	/**
 	 * Loads the manifest for the built-in content pack.
 	 * 
 	 * @throws ResourceNotFoundException
 	 * @throws IOException
 	 */
 	public void scanBuiltIn() throws ResourceNotFoundException, IOException {
 		ContentPack builtIn = new ContentPack();
 		packs.put(ContentPack.BUILT_IN_NAME, builtIn);
 	}
 	
 	/**
 	 * Gets the monitor for the progress of the next call to loadAssets(). This
 	 * must be called only directly before loadAssets() is called, and only
 	 * loadAssets() should be called after this method.
 	 * 
 	 * Getting the monitor of any loading operation is optional, but once the
 	 * monitor for a particular load operation is obtained, the next load
 	 * operation must be the one that the monitor is intended for. Failure to
 	 * follow this will result in undefined behavior.
 	 * 
 	 * @param name The name of the content pack.
 	 * @return The monitor.
 	 */
 	public Progressable startAssetLoadMonitor(String name) {
 		return packs.get(name).startAssetLoadMonitor();
 	}
 	
 	/**
 	 * Gets the monitor for the progress of the next call to loadWorld(). This
 	 * must be called only directly before loadWorld() is called, and only
 	 * loadWorld() should be called after this method.
 	 * 
 	 * Getting the monitor of any loading operation is optional, but once the
 	 * monitor for a particular load operation is obtained, the next load
 	 * operation must be the one that the monitor is intended for. Failure to
 	 * follow this will result in undefined behavior.
 	 * 
 	 * @param name The name of the content pack.
 	 * @return The monitor.
 	 */
 	public Progressable startWorldLoadMonitor(String name) {
 		return packs.get(name).startWorldLoadMonitor();
 	}
 	
 }
