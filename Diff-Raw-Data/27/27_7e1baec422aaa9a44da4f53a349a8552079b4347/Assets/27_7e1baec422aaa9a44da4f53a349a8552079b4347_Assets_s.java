 /**
  * The Assets class is used to manage cachable assets.
  */
 package ecologylab.generic.AssetsCache;
 
 import java.awt.GraphicsConfiguration;
 import java.io.File;
 
 import ecologylab.generic.ApplicationProperties;
 import ecologylab.generic.Debug;
 import ecologylab.generic.DispatchTarget;
 import ecologylab.generic.DownloadMonitor;
 import ecologylab.generic.Files;
 import ecologylab.generic.Generic;
 import ecologylab.generic.PropertiesAndDirectories;
 import ecologylab.generic.ZipDownload;
 import ecologylab.gui.Status;
 import ecologylab.media.IIOPhoto;
 import ecologylab.net.ParsedURL;
 
 /**
  * Used to manage cachable assets
  * 
  * @author blake
  */
 public class Assets
 extends Debug
 implements ApplicationProperties
 {
 	/**
 	 * current known assets (although arbitrary ones can exist)
 	 */
 	public static final String INTERFACE		= "interface/";
 	public static final String SEMANTICS		= "semantics/";
 	public static final String PREFERENCES		= "preferences/";
 	
 	/**
 	 * Source URL root of the tree of assets for this application.
 	 * Default is the configDir(), which in turn is the config subdir of codebase.
 
 	 * 
 	 * The source location of any asset is specified relative to here.
 	 */
 	static ParsedURL	assetsRoot;
 	
 	/**
 	 * Source URL root of the tree of interface assets for this application.
 	 * This should always be set to the INTERFACE subdir of the assetsRoot.
 	 * 
 	 * The source location of any interface asset is specified relative to here.
 	 */
 	static ParsedURL	interfaceAssetsRoot;
 	
 	/**
 	 * Source URL root of the tree of semantics assets for this application.
 	 * This should always be set to the SEMANTICS subdir of the assetsRoot.
 	 * 
 	 * The source location of any semantics asset is specified relative to here.
 	 */
 	static ParsedURL	semanticsAssetsRoot;
 	
 	/**
 	 * Source URL root of the tree of preferences assets for this application.
 	 * This should always be set to the PREFERENCES subdir of the assetsRoot.
 	 * 
 	 * The source location of any preferences asset is specified relative to here.
 	 */
 	static ParsedURL	preferencesAssetsRoot;
 	
 /**
  * The root directory on the local machine where assets will be stored (cached).
  * 
  * The cache destination  of any asset is applied relative to here.
  */
 	static File			cacheRoot;
 	
 	/**
 	 * The root directory on the local machine where interface assets will be stored
 	 * (cached). 
 	 * This should always be set to the INTERFACE subdir of the cacheRoot.
 	 * 
 	 * The location of any interface asset is specified relative to here.
 	 */
 	static File			interfaceCacheRoot;
 	
 	/**
 	 * The root directory on the local machine where semantics assets will be stored
 	 * (cached). 
 	 * This should always be set to the SEMANTICS subdir of the cacheRoot.
 	 * 
 	 * The location of any semantics asset is specified relative to here.
 	 */
 	static File			semanticsCacheRoot;
 	
 	static File			preferencesCacheRoot;
 
 	static
 	{
 		setAssetsRoot(Generic.configDir());
 		setCacheRoot(PropertiesAndDirectories.thisApplicationDir());
 	}
 	//////////////////////////////////////////////////////////////
 	
 	/**
 	 * No instances possible, static references only.
 	 */
 	private Assets() {}
 	
 	/**
 	 * Given a relative path, return a file reference to this path
 	 * from the cache root.
 	 * 
 	 * @param relativePath	A string representing the relative file path.
 	 * @return	A file reference to the requested path
 	 */
 	public static File getAsset(String relativePath)
 	{
 		if (cacheRoot == null)
 			return null;
 		
 		return new File(cacheRoot.getAbsolutePath() + File.separatorChar + relativePath);
 	}
 	
 	/**
 	 *  Same as getAsset(String), but allows additional relative file/directory 
 	 *  to be specified against the relativePath
 	 *  
 	 * @param relativePath A string representing the relative file path
 	 * @param additionalContext A string representing an additional relative path.
 	 * This path is relative to the relativePath parameter (rather than the cache root).
 	 * @return	A file reference to the requested path
 	 */
 	public static File getAsset(String relativePath, String additionalContext)
 	{
 		if (cacheRoot == null)
 			return null;
 		
 		return new File(getAsset(relativePath), additionalContext);
 	}
 	
 	/**
 	 * Same as getAsset(String), but creates the Asset location if it
 	 * doesn't exist
 	 * 
 	 * @param relativePath	A string representing the relative file path. 
 	 * @return	A file reference tot he requested path
 	 * @see getAsset(String)
 	 */
 	public static File getAndPerhapsCreateAsset(String relativePath)
 	{
 		File theAsset = getAsset(relativePath);
 		
 		if (!theAsset.exists())
 			theAsset.mkdirs();
 		
 		return theAsset;
 	}
 	
 	/**
 	 * Same as getAndPerhapsCreateAsset(String, String), but creates the Asset location if it
 	 * doesn't exist
 	 * 
 	 * @param relativePath	A string representing the relative file path. 
 	 * @return	A file reference tot he requested path
 	 * @see getAsset(String, additionalContext)
 	 */
 	public static File getAndPerhapsCreateAsset(String relativePath, String additionalContext)
 	{
 		File theAsset = getAsset(relativePath, additionalContext);
 		
 		if (!theAsset.exists())
 			theAsset.mkdirs();
 		
 		return theAsset;
 	}
 	/**
 	 * 
 	 * @param assetRelativePath
 	 * @return
 	 */
 	public static File getInterfaceFile(String assetRelativePath)
 	{
 		//FIXME need to make sure zip has been downloaded here
 		// if not, initiate download & wait for it!
 		return getCachedInterfaceFile(assetRelativePath);		
 	}
 	/**
 	 * Use the interfaceCacheRoot to produce a File object using the specified relative path.
 	 * 
 	 * @param assetRelativePath
 	 * @return
 	 */
 	protected static File getCachedInterfaceFile(String assetRelativePath)
 	{
 		return Files.newFile(interfaceCacheRoot, assetRelativePath);
 	}
 	/**
 	 * 
 	 * @param assetRelativePath
 	 * @return
 	 */
 	public static File getSemanticsFile(String assetRelativePath)
 	{
 		//FIXME need to make sure zip has been downloaded here
 		// if not, initiate download & wait for it!
 		return getCachedSemanticsFile(assetRelativePath);		
 	}
 	/**
 	 * Use the interfaceCacheRoot to produce a File object using the specified relative path.
 	 * 
 	 * @param assetRelativePath
 	 * @return
 	 */
 	protected static File getCachedSemanticsFile(String assetRelativePath)
 	{
 		return Files.newFile(semanticsCacheRoot, assetRelativePath);
 	}
 	
 	public static File getPreferencesFile(String assetRelativePath)
 	{
 		return getCachedPreferencesFile(assetRelativePath);
 	}
 	/**
 	 * Use the interfaceCacheRoot to produce a File object using the specified relative path.
 	 * 
 	 * @param assetRelativePath
 	 * @return
 	 */
 	protected static File getCachedPreferencesFile(String assetRelativePath)
 	{
 		return Files.newFile(preferencesCacheRoot, assetRelativePath);
 	}
 	
 	/**
 	 * Download an interface assets zip file from the interfaceAssetsRoot.
 	 * Unzip it into the cacheRoot.
 	 * 
 	 * @param assetRelativePath -- This is the name of the interface. It does not end in .zip!
 	 * @param status	Provide feedback to the user at the bottom of a window, or such.
 	 */
	public static void downloadInterfaceZip(String assetRelativePath, Status status,
 											boolean forceDownload)
 	{
 		downloadZip(interfaceAssetsRoot.getRelative(assetRelativePath + ".zip", "forming zip location"), 
 					interfaceCacheRoot, status, forceDownload);
 	}
 	/**
 	 * Download an semantics assets zip file from the semanticsAssetsRoot.
 	 * Unzip it into the cacheRoot.
 	 * 
 	 * @param assetRelativePath
 	 * @param status	Provide feedback to the user at the bottom of a window, or such.
 	 */
 	public static void downloadSemanticsZip(String assetRelativePath, Status status,
 											boolean forceDownload)
 	{
 		downloadZip(semanticsAssetsRoot.getRelative(assetRelativePath + ".zip", "forming zip location"), 
 					semanticsCacheRoot, status, forceDownload);
 	}
 	
 	public static void downloadPreferencesZip(String assetRelativePath, Status status,
 											  boolean forceDownload)
 	{
 		downloadZip(preferencesAssetsRoot.getRelative(assetRelativePath + ".zip", "forming zip location"),
 					preferencesCacheRoot, status, forceDownload);
 	}
 	/**
 	 * Download the assets zip file from the assetsRoot.
 	 * Unzip it into the cacheRoot.
 	 * 
 	 * @param assetRelativePath
 	 * @param status	Provide feedback to the user at the bottom of a window, or such.
 	 */
 	public static void downloadZip(String assetRelativePath, Status status,
 								   boolean forceDownload)
 	{
 		downloadZip(assetsRoot.getRelative(assetRelativePath, "forming zip location"), 
 					Files.newFile(cacheRoot, assetRelativePath), status, forceDownload);
 	}
 	/**
 	 * Download the assets zip file from the assetsRoot.
 	 * Unzip it into the cacheRoot.
 	 * 
 	 * Don't bother providing feedback to the user.
 	 * 
 	 * @param assetRelativePath
 	 */
 	public static void downloadZip(String assetRelativePath,
 								   boolean forceDownload)
 	{
 		downloadZip(assetsRoot.getRelative(assetRelativePath, "forming zip location"), 
 					Files.newFile(cacheRoot, assetRelativePath), null, forceDownload);
 	}
 	
 	public static void downloadZip(ParsedURL sourceZip, File targetFile,
 								   boolean forceDownload)
 	{
 		downloadZip(sourceZip, targetFile, null, forceDownload);
 	}
 	/**
 	 * Download and uncompress a zip file from a source to a target location with minimal effort,
 	 * unless the zip file already exists at the target location, in which case, 
 	 * do nothing.
 	 * @param status The Status object that provides a source of state change visiblity;
 	 * can be null.
 	 * @param forceDownload TODO
 	 * @param source The location of the zip file to download and uncompress.
 	 * @param target The location where the zip file should be uncompressed. This
 	 * directory structure will be created if it doesn't exist.
 	 */
 	public static void downloadZip(ParsedURL sourceZip, File targetDir, Status status, boolean forceDownload)
 	{
 		//TODO add versioning logic
 		String zipFileName	= sourceZip.url().getFile();
 		int lastSlash		= zipFileName.lastIndexOf('\\');
 		if (lastSlash == -1)
 			lastSlash		= zipFileName.lastIndexOf('/');
 		
 		zipFileName			= zipFileName.substring(lastSlash+1);
 		File zipFileDestination	= Files.newFile(targetDir, zipFileName);
 		if (forceDownload || !zipFileDestination.canRead())
 		{
 			ZipDownload downloadingZip	= ZipDownload.downloadZip(sourceZip, targetDir, status);
 			if (downloadingZip != null) // null if already available locally or error
 			{
 				downloadingZip.waitForDownload();
 			}
 		}
 		else
 			println("Using cached " + zipFileDestination);
 	}	
 	public static IIOPhoto getCachedIIOPhoto(String imagePath, DispatchTarget dispatchTarget, GraphicsConfiguration graphicsConfiguration)
 	{
 		//FIXME need to make sure zip has been downloaded here
 		// if not, initiate download & wait for it!
 		ParsedURL cachedImagePURL	= new ParsedURL(getCachedInterfaceFile(imagePath));
 		IIOPhoto result = new IIOPhoto(cachedImagePURL, dispatchTarget, graphicsConfiguration);
 //		result.downloadWithHighPriority();
 		result.useHighPriorityDownloadMonitor();
 		result.download();
 //		if (result.isDownloadDone())
 //			result.delivery(dispatchTarget);
 		return result;
 	}
 
 	/**
 	 * Set the source URL root of the tree of assets for this application.
 	 * Default is the configDir(), which in turn is the config subdir of codebase.
 	 * 
 	 * The source location of any asset is specified relative to here.
 	 */
 	public static void setAssetsRoot(ParsedURL assetsRoot)
 	{
 		Assets.assetsRoot 		= assetsRoot;
 		interfaceAssetsRoot		= assetsRoot.getRelative(INTERFACE, "forming interface assets root");
 		semanticsAssetsRoot		= assetsRoot.getRelative(SEMANTICS, "forming semantics assets root");
 		preferencesAssetsRoot	= assetsRoot.getRelative(PREFERENCES, "formaing preferences assets root");
 	}
 
 	/**
 	 * Sets the root file path for caching. Assets are specified relative to this path.
 	 * @param cacheRoot The root file path for caching assets.
 	 */
 	public static void setCacheRoot(File cacheRoot)
 	{
 		Assets.cacheRoot		= cacheRoot;
 		interfaceCacheRoot		= Files.newFile(cacheRoot, INTERFACE);
 		semanticsCacheRoot		= Files.newFile(cacheRoot, SEMANTICS);
 		preferencesCacheRoot	= Files.newFile(cacheRoot, PREFERENCES);
 	}
 }
 
