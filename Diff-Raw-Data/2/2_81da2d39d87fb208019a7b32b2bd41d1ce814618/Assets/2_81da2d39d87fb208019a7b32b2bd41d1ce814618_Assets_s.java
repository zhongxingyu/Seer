 /**
  * The Assets class is used to manage cachable assets.
  */
 package ecologylab.io;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLEncoder;
 
 import ecologylab.appframework.ApplicationEnvironment;
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
 import ecologylab.serialization.SIMPLTranslationException;
 import ecologylab.serialization.TranslationScope;
 
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
  * The root directory on the local machine where assets will be stored (cached).
  * 
  * The cache destination  of any asset is applied relative to here.
  */
 	static File			cacheRoot;
 
 	/*
 	 * Set-up assets and cache roots.
 	 * Read currently downloaded
 	 */
 	static
 	{
 		assetsRoot 								= EnvironmentGeneric.configDir();
 		cacheRoot 								= /*ApplicationEnvironment.runningInEclipse() ? EnvironmentGeneric.configDir().file() :*/ PropertiesAndDirectories.thisApplicationDir();
 		
 		assetsXmlFile	= new File(cacheRoot, ASSETS_XML_NAME);
 		if (assetsXmlFile.exists())
 		{
 			try
 			{
 				assetsState			= (AssetsState) AssetsTranslations.get().deserialize(assetsXmlFile);
 
 			} catch (SIMPLTranslationException e)
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
 	
 	public static File getAsset(AssetsRoot assetsRoot, String relativePath)
 	{
 		return Files.newFile(assetsRoot.getCacheRoot(), relativePath);
 	}
 	
 	public static File getAsset(AssetsRoot assetsRoot, String relativePath, String assetName, StatusReporter status, boolean forceDownload, float version)
 	{
 		File result = relativePath != null ? getAsset(assetsRoot, relativePath) : assetsRoot.getCacheRoot();
		if (!result.exists())
 			downloadZip(assetsRoot, assetName, status, forceDownload, version);
 		
 		return result;
 	}
 
 	public static void downloadZip(ParsedURL sourceZip, File targetFile,
 								   boolean forceDownload, float version)
 	{
 		downloadZip(sourceZip, targetFile, null, forceDownload, version);
 	}
 	
 	public static void downloadZip(AssetsRoot assetsRoot, String assetName, StatusReporter status, boolean forceDownload, float version)
 	{
 		downloadZip(assetsRoot.getAssetRoot().getRelative(assetName + ".zip", "forming zip location"), assetsRoot.getCacheRoot(), status, forceDownload, version);
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
 //				assetsState.translateToXML(assetsXmlFile);
 				assetsState.serialize(assetsXmlFile);
 				println("Saved Assets XML" + sourceSpot + ": " + assetsXmlFile);
 			}
 			else
 				println("NO NEED to Save Assets XML" + sourceSpot + ": " + assetsXmlFile);
 		} catch (SIMPLTranslationException e)
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
 
