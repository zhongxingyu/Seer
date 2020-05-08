 package ecologylab.appframework.types;
 
 import java.io.File;
 import java.util.HashMap;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.NamedNodeMap;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 import ecologylab.appframework.PropertiesAndDirectories;
 import ecologylab.appframework.StatusReporter;
 import ecologylab.generic.DispatchTarget;
 import ecologylab.generic.Generic;
 import ecologylab.io.Assets;
 import ecologylab.io.Files;
 import ecologylab.io.ZipDownload;
 import ecologylab.net.ParsedURL;
 import ecologylab.xml.ElementState;
 import ecologylab.xml.XmlTranslationException;
 import ecologylab.xml.xml_inherit;
 import ecologylab.xml.subelements.ArrayListState;
 
 /**
  * Handles the loading and parsing of the asset version XML file
  *  
  * @author robinson
  */
 @xml_inherit public class AssetsState extends ArrayListState<AssetState>
 {
 	// the hashmaps that store the version information.
 	// this stores the version info as specified BY THE CODE 
 	// this stores the version info as specifiec by the XML file.
 	static final HashMap<String, Float> loadedVersionsMap          = new HashMap<String, Float>();
 
 	//this is the name of our asset file.
 	static String assetFileName;
 
 	static AssetsState	assetsState;
 	
 	static public final float	IGNORE_VERSION	= 0f;
 	
 	/**
 	 * Add the element to the HashMap.
 	 * 
 	 * @param elementState
 	 */
 	protected void addNestedElement(ElementState elementState)
 	{
 		if (elementState instanceof AssetState)
 		{
 			AssetState assetState	= (AssetState) elementState;
 			loadedVersionsMap.put(assetState.id, assetState.version);
 			
 			// default action -- adds to ArrayList
 			add(assetState);
 		}
 		else
 		{
 			error("Trying to add wrong type: " + elementState);
 		}
 	}
 	
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
 		
 		if (loadedVersionsMap.containsKey(id))
 		{
 			float localVersion = loadedVersionsMap.get(id);
 			boolean result	= requiredVersion <= localVersion;
 			
 			if (!result)
 			{
 				//the wrong version of our asset XML file, so reget!
 				reloadAssetsXML();
 			}
 			return result;
 		}
 		//since it's not there, redownload the asset AND the asset version XML file!!
 		reloadAssetsXML();
 		return false;
 	}
 
 	/**
 	 * forces a reload of the asset file.
 	 * this only happens if we need to reload it.
 	 * @throws NumberFormatException if anything bad happens, this is thrown.
 	 * @throws XmlTranslationException 
 	 */
 	public static void reloadAssetsXML() 
 	{
 		try 
 		{
			loadLocalAssetVersions(AssetsState.assetFileName, true);
 		} catch (XmlTranslationException e) 
 		{
 			// TODO Auto-generated catch block
 			error("AssetsState", "reload failed.");
 			e.printStackTrace();
 		}
 	}
 	
 	
 	/**
 	 * Load the asset.xml file from the assetsCacheRoot,
 	 * which is equivalent to the Application Data dir.
 	 * Build an AssetsState object from it.
 	 * @throws XmlTranslationException 
 	 * @throws XmlTranslationException 
 	 */
	public static void loadLocalAssetVersions(String assetsVersionFileName, boolean forceDownload) 
 	throws XmlTranslationException
 	{
 		if (assetsVersionFileName == null)
 			throw new XmlTranslationException("Null assetsVersionFileName");
 
 		//set our version file info, so that we can reget our asset version file if necessary
 		// (without knowing the name of the file).
 		AssetsState.assetFileName = assetsVersionFileName;
 		
 		File assetsVersionFile 	= AssetsState.getAssetsVersionFile(assetsVersionFileName);
 		
 		assetsState				= (AssetsState) ElementState.translateFromXML(assetsVersionFile, AssetsTranslations.get());
 	}
 
 	/**
 	 * Check each entry in requiredVersionsMap for an associated entry in
 	 * the AssetsState.
 	 * <p/>
 	 * If the version for any required entry is greater than that in AssetsState
 	 * (or if there is a required version # and *no* entry in AssetsState):
 	 *    1) re-download and unzip that Zip file.
 	 *    2) After all such downloads are complete, re-download the
 	 *       assets.xml and re-build the AssetsState object.
 	 * <p/>
 	 * If the dispatchTarget parameter is non-null,
 	 * return immediately, do this asynhronously,
 	 * and then call the dispatchTarget with the reigning AssetsState as arg.
 	 * <p/>
 	 * If the dispatchTarget parameter is null, return when all is complete.
 	 */
 	public static AssetsState resolveAssetDownloads(AssetsState assetsVersion, DispatchTarget target)
 	{
 		return assetsVersion;
 	}
 
 
 	public static File getAssetsVersionFile(String assetRelativePath)
 	{
 		return getCachedAssetsVersionFileFile(assetRelativePath);
 	}
 	/**
 	 * Use the cacheRoot to produce a File object using the specified relative path.
 	 * 
 	 * @param assetRelativePath
 	 * @return
 	 */
 	protected static File getCachedAssetsVersionFileFile(String assetRelativePath)
 	{
 		return Files.newFile(Assets.cacheRoot(), assetRelativePath);
 	}
 
 	/**
	 * Download an XML assets file from the AssetsRoot.
 	 * 
 	 * @param assetRelativePath -- This is the name of the interface. It does not end in .zip!
 	 * @return	false if the assetRelativePath is null; otherwise true.
 	 */
	public static boolean downloadAssetsXML(String assetRelativePath, StatusReporter status,
											boolean forceDownload)
 	{
 		if (assetRelativePath == null)
 			return false;
 		
 
 		downloadXML(Assets.assetsRoot().getRelative(assetRelativePath, "forming File location"), 
				Assets.cacheRoot(), status, forceDownload);
 
 		return true;
 	}
 
 	/**
 	 * Download an XML file from a source to a target location with minimal effort,
 	 * unless the XML file already exists at the target location, in which case, 
 	 * do nothing.
 	 * @param status The Status object that provides a source of state change visiblity;
 	 * can be null.
	 * @param forceDownload
 	 * @param source The location of the zip file to download and uncompress.
 	 * @param target The location where the zip file should be uncompressed. This
 	 * directory structure will be created if it doesn't exist.
 	 */
	public static void downloadXML(ParsedURL sourceXML, File targetDir, StatusReporter status, boolean forceDownload)
 	{
 		String xmlFileName	= sourceXML.url().getFile();
 		int lastSlash		= xmlFileName.lastIndexOf('\\');
 		if (lastSlash == -1)
 			lastSlash		= xmlFileName.lastIndexOf('/');
 
 		xmlFileName			= xmlFileName.substring(lastSlash+1);
 		File xmlFileDestination	= Files.newFile(targetDir, xmlFileName);
 
		if (forceDownload || !xmlFileDestination.canRead())
 		{
 			//we just want to download it, not uncompress it... (using code from zip downloading stuff)
 			ZipDownload downloadingZip	= ZipDownload.downloadFile(sourceXML, targetDir, status);
 			if (downloadingZip != null) // null if already available locally or error
 			{
 				downloadingZip.waitForDownload();
 			}
 		}
 		else
 			println("Using cached " + xmlFileDestination);
 	}
 
 }
