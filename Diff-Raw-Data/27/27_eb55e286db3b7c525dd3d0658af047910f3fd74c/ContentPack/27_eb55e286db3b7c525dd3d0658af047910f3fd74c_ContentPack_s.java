 package yuuki.content;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.net.URL;
 import java.net.URLDecoder;
 import java.util.List;
 import java.util.Map;
 import java.util.zip.ZipFile;
 
 import yuuki.action.ActionFactory;
 import yuuki.entity.EntityFactory;
 import yuuki.file.ResourceNotFoundException;
 import yuuki.ui.DialogHandler;
 import yuuki.util.Progressable;
 import yuuki.world.PopulationFactory;
 import yuuki.world.PortalFactory;
 import yuuki.world.TileFactory;
 
 /**
  * Loads content data and provides content pack meta data.
  */
 public class ContentPack {
 	
 	/**
 	 * The name for the built-in ContentPack.
 	 */
 	public static final String BUILT_IN_NAME = "__BUILT-IN";
 	
 	/**
 	 * The root directory for all resource files.
 	 */
 	private static final String BUILT_IN_ROOT = "yuuki/resource/";
 	
 	/**
 	 * Whether this ContentPack's non-map data has been loaded.
 	 */
 	private boolean assetsLoaded = false;
 	
 	/**
 	 * The content loaded from disk.
 	 */
 	private final Content content = new Content();
 	
 	/**
 	 * Whether this ContentPack is enabled.
 	 */
 	private boolean enabled = false;
 	
 	/**
 	 * Whether the resources included in this ContentPack are in a ZIP archive.
 	 */
 	private final boolean inArchive;
 	
 	/**
 	 * Handles the actual loading of resources from the content pack.
 	 */
 	private final ContentLoader loader;
 	
 	/**
 	 * The location of the content directory that holds all data for this pack.
 	 * If this content pack is in a ZIP file, then this will be that ZIP file.
 	 * If this content pack is simply in a directory, then this will be that
 	 * directory.
 	 */
 	private final File location;
 	
 	/**
 	 * The manifest for this ContentPack.
 	 */
 	private final ContentManifest manifest;
 	
 	/**
 	 * Whether this ContentPack's map data has been loaded.
 	 */
 	private boolean mapLoaded = false;
 	
 	/**
 	 * The name of this ContentPack.
 	 */
 	private final String name;
 	
 	/**
 	 * Creates a new ContentPack for loading built-in resources.
 	 */
 	public ContentPack() throws ResourceNotFoundException, IOException {
 		name = BUILT_IN_NAME;
 		File jar = getJarFile();
 		inArchive = (jar != null);
 		if (inArchive) {
 			location = jar;
 			loader = new ZippedContentLoader(location, BUILT_IN_ROOT);
 		} else {
 			location = new File(getPackageRootFile(), BUILT_IN_ROOT);
 			loader = new ContentLoader(location);
 		}
 		manifest = loader.readManifest();
 		setLoaded();
 	}
 	
 	/**
 	 * Creates a new ContentPack for files in the given directory.
 	 * 
 	 * @param directory The directory that contains the ContentPack.
 	 * @throws ResourceNotFoundException If the given directory does not exist
 	 * or if the given archive does not contain a manifest file.
 	 * @throws IOException If an IOException occurs.
 	 */
 	public ContentPack(File directory) throws ResourceNotFoundException,
 	IOException {
 		location = directory;
 		loader = new ContentLoader(location);
 		inArchive = false;
 		name = location.getName();
 		manifest = loader.readManifest();
 		setLoaded();
 	}
 	
 	/**
 	 * Creates a new ContentPack for files in the given ZIP archive.
 	 * 
 	 * @param archive The archive that contains the ContentPack.
 	 * @throws ResourceNotFoundException If the given archive does not exist or
 	 * if the given archive does not contain a manifest file.
 	 * @throws IOException If an IOException occurs.
 	 */
 	public ContentPack(ZipFile archive) throws ResourceNotFoundException,
 	IOException {
 		location = new File(archive.getName());
 		loader = new ZippedContentLoader(location);
 		inArchive = true;
 		name = location.getName();
 		manifest = loader.readManifest();
 		setLoaded();
 	}
 	
 	/**
 	 * Checks whether this ContentPack has loaded its non-map data.
 	 * 
 	 * @return True if the non-map data has been loaded, or if there is none.
 	 */
 	public boolean assetsAreLoaded() {
 		return assetsLoaded;
 	}
 	
 	/**
 	 * Gets the loaded content from this ContentPack.
 	 * 
 	 * @return The content.
 	 */
 	public Content getContent() {
 		return content;
 	}
 	
 	/**
 	 * Gets the location of this ContentPack. If this ContentPack is located in
 	 * an archive, the File representing that archive will be returned. If this
 	 * ContentPack is located in a directory, the File representing that
 	 * directory will be returned.
 	 * 
 	 * @return The location of this ContentPack.
 	 */
 	public File getLocation() {
 		return location;
 	}
 	
 	/**
 	 * Gets the name of this ContentPack.
 	 * 
 	 * @return The name.
 	 */
 	public String getName() {
 		return name;
 	}
 	
 	/**
 	 * Whether this ContentPack contains actions.
 	 */
 	public boolean hasActions() {
 		return manifest.has(ContentManifest.FILE_ACTIONS);
 	}
 	
 	/**
 	 * Whether this ContentPack contains sound definitions.
 	 */
 	public boolean hasEffectDefinitions() {
 		return manifest.has(ContentManifest.FILE_EFFECTS);
 	}
 	
 	/**
 	 * Checks whether this ContentPack contains sound effect files.
 	 * 
 	 * @return Whether it does.
 	 */
 	public boolean hasEffects() {
 		return manifest.has(ContentManifest.DIR_EFFECTS);
 	}
 	
 	/**
 	 * Whether this ContentPack contains entities.
 	 */
 	public boolean hasEntities() {
 		return manifest.has(ContentManifest.FILE_ENTITIES);
 	}
 	
 	/**
 	 * Whether this ContentPack contains image definitions.
 	 */
 	public boolean hasImageDefinitions() {
 		return manifest.has(ContentManifest.FILE_IMAGES);
 	}
 	
 	/**
 	 * Checks whether this ContentPack contains image files.
 	 * 
 	 * @return Whether it does.
 	 */
 	public boolean hasImages() {
 		return manifest.has(ContentManifest.DIR_IMAGES);
 	}
 	
 	/**
 	 * Checks whether this ContentPack contains land files.
 	 * 
 	 * @return Whether it does.
 	 */
 	public boolean hasLands() {
 		return manifest.has(ContentManifest.DIR_LANDS);
 	}
 	
 	/**
 	 * Checks whether this ContentPack contains music files.
 	 * 
 	 * @return Whether it does.
 	 */
 	public boolean hasMusic() {
 		return manifest.has(ContentManifest.DIR_MUSIC);
 	}
 	
 	/**
 	 * Whether this ContentPack contains music definitions.
 	 */
 	public boolean hasMusicDefinitions() {
 		return manifest.has(ContentManifest.FILE_MUSIC);
 	}
 	
 	/**
 	 * Whether this ContentPack contains portal definitions.
 	 */
 	public boolean hasPortals() {
 		return manifest.has(ContentManifest.FILE_PORTALS);
 	}
 	
 	/**
 	 * Whether this ContentPack contains a tile definition.
 	 */
 	public boolean hasTiles() {
 		return manifest.has(ContentManifest.FILE_TILES);
 	}
 	
 	/**
 	 * Whether this ContentPack contains a world definition.
 	 */
 	public boolean hasWorld() {
 		return manifest.has(ContentManifest.FILE_WORLD);
 	}
 	
 	/**
 	 * Whether this ContentPack has been set to be enabled.
 	 * 
 	 * @return Whether it has been.
 	 */
 	public boolean isEnabled() {
 		return enabled;
 	}
 	
 	/**
 	 * Checks whether this ContentPack is located inside of a ZIP archive.
 	 * 
 	 * @return Whether this ContentPack is an a ZIP archive.
 	 */
 	public boolean isInArchive() {
 		return inArchive;
 	}
 	
 	/**
 	 * Checks whether this ContentPack has been fully loaded.
 	 * 
 	 * @return Whether it has been.
 	 */
 	public boolean isLoaded() {
 		return (mapsAreLoaded() && assetsAreLoaded());
 	}
 	
 	/**
 	 * Loads all content that this ContentPack has. If any content is included
 	 * that requires some other content that exists outside of this
 	 * ContentPack, then the required item is taken from the given Content
 	 * object.
 	 * <P>
 	 * Before the load, any content already loaded is cleared from memory.
 	 * After the load, the loaded resources are stored in this ContentPack and
 	 * can be retrieved by using the getContent() method.
 	 * 
 	 * @param resolver Used to satisfy requirements that are not included in
 	 * this ContentPack. Set to null if requirements should not be
 	 * automatically fulfilled.
 	 * @throws ResourceNotFoundException If any resource in the load is not
 	 * found.
 	 * @throws IOException If an I/O error occurs during the load.
 	 */
 	public void load(Content resolver) throws ResourceNotFoundException,
 	IOException {
 		content.reset();
 		if (!loader.isInLoad()) {
 			startLoadMonitor();
 		}
 		loadAssets(resolver);
 		loadMaps(resolver);
 	}
 	
 	/**
 	 * Loads all content that is not the world and its lands.
 	 * <P>
 	 * Before the load, any content already loaded is cleared from memory.
 	 * After the load, the loaded resources are stored in this ContentPack and
 	 * can be retrieved by using the getContent() method.
 	 * 
 	 * @param resolver Used to satisfy requirements that are not included in
 	 * this ContentPack. Set to null if requirements should not be
 	 * automatically fulfilled.
 	 * @throws ResourceNotFoundException If any resource in the load is not
 	 * found.
 	 * @throws IOException If an I/O error occurs during the load.
 	 */
 	public void loadAssets(Content resolver) throws ResourceNotFoundException,
 	IOException {
 		content.resetAssets();
 		if (!loader.isInLoad()) {
 			startAssetLoadMonitor();
 		}
 		loadMusicDefinitions();
 		loadEffectDefinitions();
 		loadImageDefinitions();
 		loadMusic(resolver);
 		loadEffects(resolver);
 		loadImages(resolver);
 		loadActions();
 		loadEntities(resolver);
 		loadPortals();
 		loadTiles();
 		assetsLoaded = true;
 	}
 	
 	/**
 	 * Loads all content that is related to the world.
 	 * <P>
 	 * Before the load, any content already loaded is cleared from memory.
 	 * After the load, the loaded resources are stored in this ContentPack and
 	 * can be retrieved by using the getContent() method.
 	 * 
 	 * @param resolver Used to satisfy requirements that are not included in
 	 * this ContentPack. Set to null if requirements should not be
 	 * automatically fulfilled.
 	 * @throws ResourceNotFoundException If any resource in the load is not
 	 * found.
 	 * @throws IOException If an I/O error occurs during the load.
 	 */
 	public void loadMaps(Content resolver) throws
 	ResourceNotFoundException, IOException {
 		content.resetMaps();
 		if (!loader.isInLoad()) {
 			startMapLoadMonitor();
 		}
 		loadWorld();
 		loadLands(resolver);
 		mapLoaded = true;
 	}
 	
 	/**
 	 * Checks whether this ContentPack has loaded its map data.
 	 * 
 	 * @return True if the map data has been loaded, or if there is none.
 	 */
 	public boolean mapsAreLoaded() {
 		return mapLoaded;
 	}
 	
 	/**
 	 * Sets whether this ContentPack is enabled.
 	 * 
 	 * @param enabled What to set the enabled state to.
 	 */
 	public void setEnabled(boolean enabled) {
 		this.enabled = enabled;
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
 	 * @return The monitor.
 	 */
 	public Progressable startAssetLoadMonitor() {
 		return loader.initLoad(getAssetLoadCount());
 	}
 	
 	/**
 	 * Gets the monitor for the progress of the next call to load(). This must
 	 * be called only directly before load() is called, and only load() should
 	 * be called after this method.
 	 * 
 	 * Getting the monitor of any loading operation is optional, but once the
 	 * monitor for a particular load operation is obtained, the next load
 	 * operation must be the one that the monitor is intended for. Failure to
 	 * follow this will result in undefined behavior.
 	 * 
 	 * @return The monitor.
 	 */
 	public Progressable startLoadMonitor() {
 		return loader.initLoad(getLoadCount());
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
 	 * @return The monitor.
 	 */
 	public Progressable startMapLoadMonitor() {
 		return loader.initLoad(getMapLoadCount());
 	}
 	
 	/**
 	 * Creates a File object that points to the package root located by a URL.
 	 * 
 	 * @param jarUrl The URL that locates the current class.
 	 * @param packages The package depth of the current class.
 	 * @return The File object that points to the package root in the given
 	 * URL.
 	 */
 	private File composePackageRootFile(URL resource, int packageCount) {
 		String decoded = null;
 		try {
 			decoded = URLDecoder.decode(resource.toString(), "UTF-8");
 		} catch (UnsupportedEncodingException e) {
 			// should never happen.
 			DialogHandler.showFatalError("UTF-8 identified as unsupported!");
 		}
 		String path = decoded.replace("!", "");
 		if (path.startsWith("jar:")) {
 			path = path.substring(4);
 		}
 		if (path.startsWith("file:")) {
 			path = path.substring(5);
 		}
 		File jarFile = new File(path);
 		for (int i = 0; i < packageCount + 1; i++) {
 			jarFile = jarFile.getParentFile();
 		}
 		return jarFile;
 	}
 	
 	/**
 	 * Gets the number of loads necessary to load every type of non-world
 	 * content that this ContentPack has.
 	 * 
 	 * @return The number of different non-world resource types that this
 	 * ContentPack has.
 	 */
 	private int getAssetLoadCount() {
 		int count = 0;
 		count += (hasMusicDefinitions())	? 1 : 0;
 		count += (hasEffectDefinitions())	? 1 : 0;
 		count += (hasImageDefinitions())	? 1 : 0;
 		count += (hasMusic())				? 1 : 0;
 		count += (hasEffects())				? 1 : 0;
 		count += (hasImages())				? 1 : 0;
 		count += (hasActions())				? 1 : 0;
 		count += (hasEntities())			? 1 : 0;
 		count += (hasPortals())				? 1 : 0;
 		count += (hasTiles())				? 1 : 0;
 		return count;
 	}
 	
 	/**
 	 * Gets the JAR file this class is being executed from if it exists.
 	 * 
 	 * @return The JAR file, or null if this class is not being executed from a
 	 * JAR file.
 	 */
 	private File getJarFile() {
 		String className = getClass().getName();
 		int packageCount = getPackageCount(className);
 		String classPath = className.replace('.', '/');
 		URL resource = getClass().getResource("/" + classPath + ".class");
 		if (resource.toString().startsWith("jar:")) {
 			return composePackageRootFile(resource, packageCount);
 		} else {
 			return null;
 		}
 	}
 	
 	/**
 	 * Gets the number of loads necessary to load every type of content that
 	 * this ContentPack has.
 	 * 
 	 * @return The number of different resource types that this ContentPack
 	 * has.
 	 */
 	private int getLoadCount() {
 		return getAssetLoadCount() + getMapLoadCount();
 	}
 	
 	/**
 	 * Gets the number of loads necessary to load every type of world content
 	 * that this ContentPack has.
 	 * 
 	 * @return The number of different world resource types that this
 	 * ContentPack has.
 	 */
 	private int getMapLoadCount() {
 		int count = 0;
 		count += (hasWorld())	? 1 : 0;
 		count += (hasLands())	? 1 : 0;
 		return count;
 	}
 	
 	/**
 	 * Counts the number of packages in a class name.
 	 * 
 	 * @param className The fully-qualified class name to count the number of
 	 * packages in.
 	 * @return The number of packages in the given name.
 	 */
 	private int getPackageCount(String className) {
 		int count = 0;
 		for (int i = 0; i < className.length(); i++) {
 			if (className.charAt(i) == '.') {
 				count++;
 			}
 		}
 		return count;
 	}
 	
 	/**
 	 * Gets the File that represents the root of the package structure that
 	 * this class is located in.
 	 * 
 	 * @return The File that represents the package root.
 	 */
 	private File getPackageRootFile() {
 		String className = getClass().getName();
 		int packageCount = getPackageCount(className);
 		String classPath = className.replace('.', '/');
 		URL resource = getClass().getResource("/" + classPath + ".class");
 		return composePackageRootFile(resource, packageCount);
 	}
 	
 	/**
 	 * Gets a PopulationFactory by drawing on the loaded content and falling
 	 * back to the resolver.
 	 * 
 	 * @param resolver Used to satisfy requirements that are not included in
 	 * this ContentPack. Set to null if requirements should not be
 	 * automatically fulfilled.
 	 * @return The PopulationFactory composed of loaded content.
 	 */
 	private PopulationFactory getPopFactory(Content resolver) {
 		EntityFactory ef = new EntityFactory();
 		TileFactory tf = new TileFactory();
 		PortalFactory pf = new PortalFactory();
		ef.merge(resolve(content.getEntities(),
				(resolver != null) ? resolver.getEntities() : null,
				"Cannot get pop. factory with no ent. factory"));
		tf.merge(resolve(content.getTiles(),
				(resolver != null) ? resolver.getTiles() : null,
				"Cannot get pop. factory with no tile factory"));
		pf.merge(resolve(content.getPortals(),
				(resolver != null) ? resolver.getPortals() : null,
				"Cannot get pop. factory with no portal factory"));
 		return new PopulationFactory(tf, ef, pf);
 	}
 	
 	/**
 	 * Loads actions. The content is loaded from the content container if this
 	 * ContentPack contains it as indicated by the manifest.
 	 * 
 	 * @throws ResourceNotFoundException If any resource in the load is not
 	 * found.
 	 * @throws IOException If an I/O error occurs during the load.
 	 */
 	private void loadActions() throws ResourceNotFoundException, IOException {
 		if (hasActions()) {
 			String msg = "Loading actions...";
 			content.setActions(loader.loadActions(msg));
 		}
 	}
 	
 	/**
 	 * Loads sound effect definitions. The content is loaded from the content
 	 * container if this ContentPack contains it as indicated by the manifest.
 	 * 
 	 * @throws ResourceNotFoundException If any resource in the load is not
 	 * found.
 	 * @throws IOException If an I/O error occurs during the load.
 	 */
 	private void loadEffectDefinitions() throws ResourceNotFoundException,
 	IOException {
 		if (hasEffectDefinitions()) {
 			String msg = "Loading effect definitions...";
 			content.setEffectDefinitions(loader.loadEffectDefinitions(msg));
 		}
 	}
 	
 	/**
 	 * Loads sound effects. The content is loaded from the content container if
 	 * this ContentPack contains it as indicated by the manifest. This method
 	 * must be called after loadEffectDefinitions(), or it must be provided
 	 * with an appropriate resolver.
 	 * 
 	 * @param resolver Used to satisfy requirements that are not included in
 	 * this ContentPack. Set to null if requirements should not be
 	 * automatically fulfilled.
 	 * @throws ResourceNotFoundException If any resource in the load is not
 	 * found.
 	 * @throws IOException If an I/O error occurs during the load.
 	 */
 	private void loadEffects(Content resolver) throws
 	ResourceNotFoundException, IOException {
 		if (hasEffects()) {
 			Map<String, String> paths = null;
 			boolean rSet = (resolver != null);
 			paths = resolve(content.getEffectDefinitions(),
 					rSet ? resolver.getEffectDefinitions() : null,
 					"Cannot load effects with no definitions");
 			String msg = "Loading sound effects...";
 			content.setEffects(loader.loadEffects(msg, paths));
 		}
 	}
 	
 	/**
 	 * Loads Entities. The content is loaded from the content container if this
 	 * ContentPack contains it as indicated by the manifest. This method must
 	 * be called after loadActions(), or it must be provided with an
 	 * appropriate resolver.
 	 * 
 	 * @param resolver Used to satisfy requirements that are not included in
 	 * this ContentPack. Set to null if requirements should not be
 	 * automatically fulfilled.
 	 * @throws ResourceNotFoundException If any resource in the load is not
 	 * found.
 	 * @throws IOException If an I/O error occurs during the load.
 	 */
 	private void loadEntities(Content resolver) throws
 	ResourceNotFoundException, IOException {
 		if (hasEntities()) {
 			ActionFactory a = new ActionFactory();
 			a.merge(resolve(content.getActions(),
 					(resolver != null) ? resolver.getActions() : null,
 					"Cannot load entities with no actions"));
 			String msg = "Loading entities...";
 			content.setEntities(loader.loadEntities(msg, a));
 		}
 	}
 	
 	/**
 	 * Loads image definitions. The content is loaded from the content
 	 * container if this ContentPack contains it as indicated by the manifest.
 	 * 
 	 * @throws ResourceNotFoundException If any resource in the load is not
 	 * found.
 	 * @throws IOException If an I/O error occurs during the load.
 	 */
 	private void loadImageDefinitions() throws ResourceNotFoundException,
 	IOException {
 		if (hasImageDefinitions()) {
 			String msg = "Loading image definitions...";
 			content.setImageDefinitions(loader.loadImageDefinitions(msg));
 		}
 	}
 	
 	/**
 	 * Loads images. The content is loaded from the content container if this
 	 * ContentPack contains it as indicated by the manifest. This method must
 	 * be called after loadImageDefinitions(), or it must be provided with an
 	 * appropriate resolver.
 	 * 
 	 * @param resolver Used to satisfy requirements that are not included in
 	 * this ContentPack. Set to null if requirements should not be
 	 * automatically fulfilled.
 	 * @throws ResourceNotFoundException If any resource in the load is not
 	 * found.
 	 * @throws IOException If an I/O error occurs during the load.
 	 */
 	private void loadImages(Content resolver) throws ResourceNotFoundException,
 	IOException {
 		if (hasImages()) {
 			Map<String, String> paths = null;
 			paths = resolve(content.getImageDefinitions(),
 					(resolver != null) ? resolver.getImageDefinitions() : null,
 					"Cannot load images with no definitions");
 			String msg = "Loading images...";
 			content.setImages(loader.loadImages(msg, paths));
 		}
 	}
 	
 	/**
 	 * Loads land data. The content is loaded from the content container if
 	 * this ContentPack contains it as indicated by the manifest. This method
 	 * must be called after loadWorld(), loadPortals(), loadTiles() and
 	 * loadEntities(), or it must be provided with an appropriate resolver.
 	 * 
 	 * @param resolver Used to satisfy requirements that are not included in
 	 * this ContentPack. Set to null if requirements should not be
 	 * automatically fulfilled.
 	 * @throws ResourceNotFoundException If any resource in the load is not
 	 * found.
 	 * @throws IOException If an I/O error occurs during the load.
 	 */
 	private void loadLands(Content resolver) throws ResourceNotFoundException,
 	IOException {
 		if (hasLands()) {
 			PopulationFactory pop = getPopFactory(resolver);
 			List<String> paths = null;
 			paths = resolve(content.getWorld(),
 					(resolver != null) ? resolver.getWorld() : null,
 					"Cannot load lands with no world");
 			String msg = "Loading land data...";
 			content.setLands(loader.loadLands(msg, paths, pop));
 		}
 	}
 	
 	/**
 	 * Loads music. The content is loaded from the content container if this
 	 * ContentPack contains it as indicated by the manifest. This method must
 	 * be called after loadMusicDefinitions(), or it must be provided with an
 	 * appropriate resolver.
 	 * 
 	 * @param resolver Used to satisfy requirements that are not included in
 	 * this ContentPack. Set to null if requirements should not be
 	 * automatically fulfilled.
 	 * @throws ResourceNotFoundException If any resource in the load is not
 	 * found.
 	 * @throws IOException If an I/O error occurs during the load.
 	 */
 	private void loadMusic(Content resolver) throws ResourceNotFoundException,
 	IOException {
 		if (hasMusic()) {
 			Map<String, String> paths = null;
 			paths = resolve(content.getMusicDefinitions(),
 					(resolver != null) ? resolver.getMusicDefinitions() : null,
 					"Cannot load music with no definitions");
 			String msg = "Loading music...";
 			content.setMusic(loader.loadMusic(msg, paths));
 		}
 	}
 	
 	/**
 	 * Loads music definitions. The content is loaded from the content
 	 * container if this ContentPack contains it as indicated by the manifest.
 	 * 
 	 * @throws ResourceNotFoundException If any resource in the load is not
 	 * found.
 	 * @throws IOException If an I/O error occurs during the load.
 	 */
 	private void loadMusicDefinitions() throws ResourceNotFoundException,
 	IOException {
 		if (hasMusicDefinitions()) {
 			String msg = "Loading music definitions...";
 			content.setMusicDefinitions(loader.loadMusicDefinitions(msg));
 		}
 	}
 	
 	/**
 	 * Loads portal definitions. The content is loaded from the content
 	 * container if this ContentPack contains it as indicated by the manifest.
 	 * 
 	 * @throws ResourceNotFoundException If any resource in the load is not
 	 * found.
 	 * @throws IOException If an I/O error occurs during the load.
 	 */
 	private void loadPortals() throws ResourceNotFoundException, IOException {
 		if (hasPortals()) {
 			String msg = "Loading portals...";
 			content.setPortals(loader.loadPortals(msg));
 		}
 	}
 	
 	/**
 	 * Loads tile definitions. The content is loaded from the content container
 	 * if this ContentPack contains it as indicated by the manifest.
 	 * 
 	 * @throws ResourceNotFoundException If any resource in the load is not
 	 * found.
 	 * @throws IOException If an I/O error occurs during the load.
 	 */
 	private void loadTiles() throws ResourceNotFoundException, IOException {
 		if (hasTiles()) {
 			String msg = "Loading tiles...";
 			content.setTiles(loader.loadTiles(msg));
 		}
 	}
 	
 	/**
 	 * Loads the list of maps. The content is loaded from the content container
 	 * if this ContentPack contains it as indicated by the manifest.
 	 * 
 	 * @throws ResourceNotFoundException If any resource in the load is not
 	 * found.
 	 * @throws IOException If an I/O error occurs during the load.
 	 */
 	private void loadWorld() throws ResourceNotFoundException, IOException {
 		if (hasWorld()) {
 			String msg = "Loading world...";
 			content.setWorld(loader.loadWorld(msg));
 		}
 	}
 	
 	/**
 	 * Gets the requested item by accessing existing content or by using the
 	 * resolution content.
 	 * @param <T> The type of the item requested.
 	 * 
 	 * @param request The desired content.
 	 * @param resolver The resolving content.
 	 * @param msg The message in the exception thrown on failure.
 	 * @return The desired content if it exists, or the resolving content if it
 	 * does not.
 	 * @throws IllegalStateException If neither of the contents are available.
 	 */
 	private <T> T resolve(T request, T resolver, String msg) {
 		T resolved = null;
 		if (request != null) {
 			resolved = request;
 		} else if (resolver != null) {
 			resolved = resolver;
 		} else {
 			throw new IllegalStateException(msg);
 		}
 		return resolved;
 	}
 	
 	/**
 	 * Sets whether this ContentPack has loaded certain parts of it based on
 	 * whether it has them.
 	 */
 	private void setLoaded() {
 		mapLoaded = !(hasWorld() || hasLands());
 		assetsLoaded = !(
 				hasMusicDefinitions() ||
 				hasEffectDefinitions() ||
 				hasImageDefinitions() ||
 				hasMusic() ||
 				hasEffects() ||
 				hasImages() ||
 				hasActions() ||
 				hasEntities() ||
 				hasPortals() ||
 				hasTiles());
 	}
 	
 }
