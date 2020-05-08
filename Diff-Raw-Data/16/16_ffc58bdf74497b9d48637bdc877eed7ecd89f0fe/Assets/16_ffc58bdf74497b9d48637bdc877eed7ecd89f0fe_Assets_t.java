 /**
  * The Assets class is used to manage cachable assets.
  */
 package ecologylab.io;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLEncoder;
 
 import ecologylab.appframework.ApplicationProperties;
 import ecologylab.appframework.EnvironmentGeneric;
 import ecologylab.appframework.PropertiesAndDirectories;
 import ecologylab.appframework.StatusReporter;
 import ecologylab.appframework.types.AssetState;
 import ecologylab.appframework.types.AssetsState;
 import ecologylab.appframework.types.AssetsTranslations;
 import ecologylab.generic.Debug;
 import ecologylab.generic.StringBuilderPool;
 import ecologylab.net.ParsedURL;
 import ecologylab.xml.ElementState;
 import ecologylab.xml.XMLTranslationException;
 
 /**
  * Used to manage cachable assets.
  * <p/>
  * This class must not be called until codeBase is properly set!
  * <p/>
  * Here's how it works:
  * (1) There is a file called assets.xml. It lives only in applicationDataDir(), the cache root.
  * 	   There is *no* version of this file in <codeBase>/config/preferences, the assets root.
  * 
  * 	   This file stores the *current* cached version # of each asset.
  * 
  * (2) The version of each Asset is a constant, which lives *in the code*.
  * 	   It is passed to Assets.downloadZip().
  * (3) If there is no local versions of assets.xml (first time ap is run), or if any version turns out to be
  * 	   stale, the assets.xml file will need to be written to the cache root.
  * 	   The application *must* call updateAssetsXml() to do this, after it is finished reading all Assets.
  * 	   This method will know if writing the file is needed or not.
  * 
  * @author blake
  * @author andruid
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
 	public static final String PREFERENCES	= "preferences/";
 	public static final String STUDY				= "study/";
 	
 	/**
 	 * Source URL root of the tree of assets for this application.
 	 * Default is the configDir(), which in turn is the config subdir of codebase.
 
 	 * 
 	 * The source location of any asset is specified relative to here.
 	 */
 	static ParsedURL	assetsRoot;
 	
 	protected static final String ASSETS_XML_NAME			= "assets.xml";
 
 	static File			assetsXmlFile;
 	
 	/**
 	 * Asset version number info!
 	 */
 	static AssetsState	assetsState;
 	
 	static boolean		needToWriteAssetsXml;
 	
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
 	 * Source URL root of the tree of study assets for this application.
 	 * This should always be set to the STUDY subdir of the assetsRoot.
 	 * 
 	 * The source location of any study asset is specified relative to here.
 	 */
 	static ParsedURL	studyAssetsRoot;
 	
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
 	
 	static File			studyCacheRoot;
 
 	/*
 	 * Set-up assets and cache roots.
 	 * Read currently downloaded
 	 */
 	static
 	{
 		ParsedURL configDir = EnvironmentGeneric.configDir();
 		if (configDir != null)
 			setAssetsRoot(configDir);
 		else
 		{
 			ParsedURL codeBaseSlashConfig	= EnvironmentGeneric.getRelativeToCodeBase("config/", "Forming assetsPURL");
 			setAssetsRoot(codeBaseSlashConfig);
 		}
 		setCacheRoot(PropertiesAndDirectories.thisApplicationDir());
 		
 		assetsXmlFile	= new File(cacheRoot, ASSETS_XML_NAME);
 		if (assetsXmlFile.exists())
 		{
 			try
 			{
 				assetsState			= (AssetsState) ElementState.translateFromXML(assetsXmlFile, AssetsTranslations.get());
 
 			} catch (XMLTranslationException e)
 			{
 				println("ERROR reading AssetsState from " + assetsXmlFile);
 				e.printStackTrace();
 			}
 		}
 		else
 		{
 			println("Assets: no cached assets found at " + assetsXmlFile);
 		}
 		if (assetsState == null)
 			assetsState			= new AssetsState();
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
 	 * @param additionalContext A string representing an additional relative path.
 	 * This path is relative to the relativePath parameter (rather than the cache root).
 	 * 
 	 * @return	A file reference to the requested path
 	 * @see #getAsset(String, String)
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
 	public static File getCachedInterfaceFile(String assetRelativePath)
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
 	
 	public static File getStudyFile(String assetRelativePath)
 	{
 		return getCachedStudyFile(assetRelativePath);
 	}
 	/**
 	 * Use the interfaceCacheRoot to produce a File object using the specified relative path.
 	 * 
 	 * @param assetRelativePath
 	 * @return
 	 */
 	protected static File getCachedStudyFile(String assetRelativePath)
 	{
 		return Files.newFile(studyCacheRoot, assetRelativePath);
 	}
 	
 	/**
 	 * Download an interface assets zip file from the interfaceAssetsRoot.
 	 * Unzip it into the cacheRoot.
 	 * 
 	 * @param assetRelativePath -- This is the name of the interface. It does not end in .zip!
 	 * @param status	Provide feedback to the user at the bottom of a window, or such.
 	 * 
 	 * @return	false if the assetRelativePath is null; otherwise true.
 	 */
 	public static boolean downloadInterfaceZip(String assetRelativePath, StatusReporter status,
 											   boolean forceDownload)
 	{
 		return downloadInterfaceZip(assetRelativePath, status, forceDownload, IGNORE_VERSION);
 	}
 	public static boolean downloadInterfaceZip(String assetRelativePath, StatusReporter status,
 											   boolean forceDownload, float version)
 	{
 		if (assetRelativePath == null)
 			return false;
 		
 		downloadZip(interfaceAssetsRoot.getRelative(assetRelativePath + ".zip", "forming zip location"), 
 				interfaceCacheRoot, status, forceDownload, version);
 		
 		return true;
 	}
 	
 	/**
 	 * Download an semantics assets zip file from the semanticsAssetsRoot.
 	 * Unzip it into the cacheRoot.
 	 * 
 	 * @param assetRelativePath
 	 * @param status	Provide feedback to the user at the bottom of a window, or such.
 	 */
 	public static void downloadSemanticsZip(String assetRelativePath, StatusReporter status,
 										    boolean forceDownload)
 	{
 		downloadSemanticsZip(assetRelativePath, status, forceDownload, IGNORE_VERSION);
 	}
 	public static void downloadSemanticsZip(String assetRelativePath, StatusReporter status,
 											boolean forceDownload, float version)
 	{
 		downloadZip(semanticsAssetsRoot.getRelative(assetRelativePath + ".zip", "forming zip location"), 
 					semanticsCacheRoot, status, forceDownload, version);
 	}
 	
 	public static void downloadPreferencesZip(String assetRelativePath, StatusReporter status,
 			  boolean forceDownload)
 	{
 		downloadPreferencesZip(assetRelativePath,  status, forceDownload, IGNORE_VERSION);
 	}
 	public static void downloadPreferencesZip(String assetRelativePath, StatusReporter status,
 											  boolean forceDownload, float version)
 	{
 		downloadZip(preferencesAssetsRoot.getRelative(assetRelativePath + ".zip", "forming zip location"),
 					preferencesCacheRoot, status, forceDownload, version);
 	}
 	
 	public static void downloadStudyZip(String assetRelativePath, StatusReporter status,
 			  boolean forceDownload)
 	{
 		downloadStudyZip(assetRelativePath,  status, forceDownload, IGNORE_VERSION);
 	}
 	public static void downloadStudyZip(String assetRelativePath, StatusReporter status,
 											  boolean forceDownload, float version)
 	{
 		downloadZip(studyAssetsRoot.getRelative(assetRelativePath + ".zip", "forming zip location"),
 					studyCacheRoot, status, forceDownload, version);
 	}
 	/**
 	 * Download the assets zip file from the assetsRoot.
 	 * Unzip it into the cacheRoot.
 	 * 
 	 * @param assetRelativePath
 	 * @param status	Provide feedback to the user at the bottom of a window, or such.
 	 */
 	public static void downloadZip(String assetRelativePath, StatusReporter status,
 								   boolean forceDownload, float version)
 	{
 		downloadZip(assetsRoot.getRelative(assetRelativePath, "forming zip location"), 
 					Files.newFile(cacheRoot, assetRelativePath), status, forceDownload, version);
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
 								   boolean forceDownload, float version)
 	{
 		downloadZip(assetsRoot.getRelative(assetRelativePath, "forming zip location"), 
 					Files.newFile(cacheRoot, assetRelativePath), null, forceDownload, version);
 	}
 	
 	public static void downloadZip(ParsedURL sourceZip, File targetFile,
 								   boolean forceDownload, float version)
 	{
 		downloadZip(sourceZip, targetFile, null, forceDownload, version);
 	}
 	/**
 	 * Download and uncompress a zip file from a source to a target location with minimal effort,
 	 * unless the zip file already exists at the target location, in which case, 
 	 * do nothing.
 	 * 
 	 * @param status		The Status object that provides a source of state change visiblity;
 	 * 						can be null.
 	 * @param forceDownload
 	 * @param version
 	 */
 	public static void downloadZip(ParsedURL sourceZip, File targetDir, StatusReporter status, boolean forceDownload, float version)
 	{
 		String zipFileName	= sourceZip.url().getFile();
 		int lastSlash		= zipFileName.lastIndexOf('\\');
 		if (lastSlash == -1)
 			lastSlash		= zipFileName.lastIndexOf('/');
 		
 		zipFileName			= zipFileName.substring(lastSlash+1);
 		File zipFileDestination	= Files.newFile(targetDir, zipFileName);
 		if (forceDownload || !zipFileDestination.canRead() || !localVersionIsUpToDate(zipFileName, version))
 		{
 			ZipDownload downloadingZip	= ZipDownload.downloadAndPerhapsUncompress(sourceZip, targetDir, status, true);
 			if (downloadingZip != null) // null if already available locally or error
 			{
 				downloadingZip.waitForDownload();
 			}
 		}
 		else
 			println("Using cached " + zipFileDestination);
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
 		preferencesAssetsRoot	= assetsRoot.getRelative(PREFERENCES, "forming preferences assets root");
 		studyAssetsRoot				= assetsRoot.getRelative(STUDY, "forming study assets root");
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
 		studyCacheRoot				= Files.newFile(cacheRoot, STUDY);
 	}
 
 	/**
 	 * Get the source URL root of the tree of assets for this application.
 	 * Default is the configDir(), which in turn is the config subdir of codebase.
 	 * 
 	 * @return	ParsedURL referring to the root of the remote place we download assets from.
 	 */
 	public static ParsedURL assetsRoot() 
 	{
 		return assetsRoot;
 	}
 
 	/**
 	 * Get the root file path for caching. Assets are specified relative to this path.
 	 * @return
 	 */
 	public static File cacheRoot() 
 	{
 		return cacheRoot;
 	}
 
 	/**
 	 * Download XML from the sourcePath, within the assetsRoot (the application's config dir),
 	 * to the target path within the applicationDir.
 	 * 
 	 * @param sourcePath
 	 * @param targetPath
 	 * @param status
 	 */
 	public static void downloadXML(String sourcePath, String targetPath, StatusReporter status)
 	{
 		File targetDir	= cacheRoot();
 		if ((targetPath != null) && (targetPath.length() > 0))
 			targetDir	= Files.newFile(targetDir, targetPath);
 		
 		downloadXML(assetsRoot().getRelative(sourcePath, "forming Asset path location"), 
 					targetDir, status);
 
 	}	
 	/**
 	 * Download an XML file from a source to a target location with minimal effort,
 	 * unless the XML file already exists at the target location, in which case, 
 	 * do nothing.
 	 * @param status The Status object that provides a source of state change visiblity;
 	 * can be null.
 	 * @param sourceXML The location of the zip file to download and uncompress.
 	 * @param targetDir The location where the zip file should be uncompressed. This
 	 * directory structure will be created if it doesn't exist.
 	 */
 	public static void downloadXML(ParsedURL sourceXML, File targetDir, StatusReporter status)
 	{
 		String xmlFileName	= sourceXML.url().getFile();
 		int lastSlash		= xmlFileName.lastIndexOf('\\');
 		if (lastSlash == -1)
 			lastSlash		= xmlFileName.lastIndexOf('/');
 	
 		xmlFileName			= xmlFileName.substring(lastSlash+1);
 		File xmlFileDestination	= Files.newFile(targetDir, xmlFileName);
 	
 		if (!xmlFileDestination.canRead())
 		{
 			//we just want to download it, not uncompress it... (using code from zip downloading stuff)
 			ZipDownload downloadingZip	= ZipDownload.downloadAndPerhapsUncompress(sourceXML, targetDir, status, false);
 			if (downloadingZip != null) // null if already available locally or error
 			{
 				downloadingZip.waitForDownload();
 			}
 		}
 		else
 			println("Using cached " + xmlFileDestination);
 	}
 	
 	static public final float	IGNORE_VERSION	= 0f;
 
 	/**
 	 * Determines if a file should be downloaded again, based upon it's file version.
 	 * @param id the name of the file to check
 	 * @param requiredVersion the version of that file
 	 * \
 	 * @return false if the local asset is stale and to download
 	 *         true if the local version is fine and we dont need to download
 	 */
 	public static boolean localVersionIsUpToDate(String id, float requiredVersion)
 	{
 		if (requiredVersion == IGNORE_VERSION)
 			return true;
 		
 		AssetState assetState	= assetsState.lookup(id);
 		boolean result			= assetState != null;
 		if (result)
 		{
 			float localVersion	= assetState.getVersion();
 			result				= requiredVersion <= localVersion;
 		}
 		else
 		{	// create an entry to write later when the application developer calls updateAssetsXml().
 			assetState			= assetsState.update(id);
 		}
 		if (!result)
 		{
 			needToWriteAssetsXml= true;
 			assetState.setVersion(requiredVersion);	// update the version in our data structure
 		}
 
 		return result;
 	}
 	
 	/**
 	 * When necessary, re-write the local (and only) assets.xml file.
 	 *
 	 */
 	public static void updateAssetsXml(String sourceSpot)
 	{
 		try
 		{
 			sourceSpot	= " from " + sourceSpot;
 			if (needToWriteAssetsXml)
 			{
 				needToWriteAssetsXml	= false;
 //				assetsState.writePrettyXML(assetsXmlFile);
 				assetsState.translateToXML(assetsXmlFile);
 				println("Saved Assets XML" + sourceSpot + ": " + assetsXmlFile);
 			}
 			else
 				println("NO NEED to Save Assets XML" + sourceSpot + ": " + assetsXmlFile);
 		} catch (XMLTranslationException e)
 		{
 			e.printStackTrace();
 		}
 		catch (IOException e)
 		{
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 	
 	private static final StringBuilderPool stringPool = new StringBuilderPool(2, 255);
 	
 	/**
 	 * Derive a non-duplicate cache filename given a ParsedURL location
 	 * @param location	The location of the file to be cached.
 	 * @param directory	The directory to cache the file
 	 * @param additional	An additional text to add to the end of the filename, but before the extension
 	 * @param separator	Separator used to divide sections of filename (e.g. "-", ".")
 	 * @param extension	Extension to add to file name if it doesn't already exist
 	 * 
 	 * @return A string containing the new filename
 	 */
 	@SuppressWarnings("deprecation")
 	public static String getCacheFilename(ParsedURL location, File directory, String additional, String separator, String extension)
 	{
 		StringBuilder filename 	= stringPool.nextBuffer();
 		filename.append(location.host());
 		filename.append(separator);
 		String locationString 	= location.url().getPath();
 		String query						= location.url().getQuery();
 		filename.append(locationString.substring(locationString.lastIndexOf('/')+1));
 		if (query != null && query.length() > 0)
 		{
 			filename.append("%3F");
 			filename.append(URLEncoder.encode(query));
 		}
 		
 		if (additional != null)
 		{
 			filename.append(separator);
 			filename.append(additional);
 		}
 		
 		if (extension != null && !locationString.endsWith(extension))
 		{
 			filename.append(".");
 			filename.append(extension);
 		}
 		
 		String filenameString = filename.toString();
 //		File localFile = new File(directory, filename.toString());
 //		if (localFile.exists())
 //		{
 //			int extensionStart 		= filename.lastIndexOf(".");
 //			int count 						= 1;
 //			String pre						= filename.substring(0, extensionStart);
 //			String end						= filename.substring(extensionStart);
 //			
 //			while(localFile.exists())
 //			{
 //				StringBuilder newFilename = stringPool.nextBuffer();
 //				newFilename.append(pre);
 //				newFilename.append(separator);
 //				newFilename.append(count);
 //				newFilename.append(end);
 //				filenameString						= newFilename.toString();
 //				localFile 								= new File(directory, filenameString);
 //				count++;
 //				stringPool.release(newFilename);
 //			}
 //			stringPool.release(filename);
 //		}
 		
 		return filenameString;
 	}

	/**
	 * @return the assetsRoot
	 */
	public static ParsedURL getAssetsRoot()
	{
		return assetsRoot;
	}

	/**
	 * @return the cacheRoot
	 */
	public static File getCacheRoot()
	{
		return cacheRoot;
	}
 	
 }
 
