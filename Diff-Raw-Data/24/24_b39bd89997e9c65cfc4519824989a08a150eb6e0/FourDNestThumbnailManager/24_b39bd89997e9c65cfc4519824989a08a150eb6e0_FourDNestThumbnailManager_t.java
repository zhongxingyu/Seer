 package org.fourdnest.androidclient.comm;
 
 import java.io.File;
 
 import org.fourdnest.androidclient.Egg;
 import org.fourdnest.androidclient.FourDNestApplication;
 
 import android.os.Environment;
 import android.util.Log;
 
 /**
  * 
  * This class gives functionality to maintain egg thumbnails between 4dNest server and android client.
  *
  */
 public class FourDNestThumbnailManager implements ThumbnailManager {
 	
 	private static String TAG = "4DNestThumbnailManager";
 	
 	private static String THUMBNAIL_LOCATION = "/fourdnest/thumbnails/";
 
 	   /**Location of thumbnails on the server */
     public static final String THUMBNAIL_PATH = "content/instance/";
     
     /** Thumbnails on the server are in jpg format*/
     private static final String THUMBNAIL_FILETYPE = ".jpg";
     
     private static final String THUMBNAIL_DEFAULT_SIZE = "-400x400";
     
     private FourDNestApplication app;
 	
 	public FourDNestThumbnailManager() {
 		this.app = FourDNestApplication.getApplication();
 	}
 	
 
 	/**
 	 * Check if given file exists
 	 * @param egg for which the check is done
 	 * @return boolean
 	 */
 	private boolean thumbNailExists(Egg egg) {
 		String path = getThumbnailUriString(egg);
 		if ((new File(path)).exists()) {
 			return true;
 		}else {
 			return false;	
 		}
 	}
 	
 	/**
	 * Returns predefined thumbnailUri for given egg as a string based on localID and a hash from unique property
 	 * @param egg 
 	 * @return String of egg's thumbnail's Uri
 	 */
 	public static String getThumbnailUriString(Egg egg) {
		if (egg.getRemoteFileURI() == null) {
			return Environment.getExternalStorageDirectory()
					+ THUMBNAIL_LOCATION
					+ egg.getId()
					+ CommUtils
							.md5FromString(egg.getRemoteFileURI().toString())
					+ THUMBNAIL_FILETYPE;
		}else {
			// egg has only textual content
			return Environment.getExternalStorageDirectory()
					+ THUMBNAIL_LOCATION
					+ egg.getId()
					+ CommUtils
							.md5FromString(egg.getCaption())
					+ THUMBNAIL_FILETYPE;
		}
 
 	}
 	
 	/**
 	 * Can be called to make sure thumbnail is in memory card, thumbnail is downloaded from 4dnest server or
 	 * OSM static maps api when applicable.
 	 * 
 	 * @param Egg whose thumbnail is in question
 	 * 
 	 * @return boolean whether thumbnail can be found in predefined location
 	 */
 	public boolean getThumbnail(Egg egg) {
 		String path = getThumbnailUriString(egg);
 		boolean res = true;
 		if (!thumbNailExists(egg)) {
 			if (egg.getMimeType() == Egg.fileType.ROUTE) {
 				StaticMapGetter mapGetter = new OsmStaticMapGetter();
 				res = mapGetter.getStaticMap(egg);
 			}else {
 				String externalUriString = app.getCurrentNest().getBaseURI()
                         + THUMBNAIL_PATH + egg.getExternalId()
                         + THUMBNAIL_DEFAULT_SIZE + THUMBNAIL_FILETYPE;
                 String thumbnail_dir = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + THUMBNAIL_LOCATION;
                 if (!new File(thumbnail_dir).exists()) {
                 	new File(thumbnail_dir).mkdirs();
                 }
                 Log.d("SAVELOC", path);
                 if (app.getCurrentNest().getProtocol().getMediaFile(externalUriString, path)) {
                     Log.d(TAG, "Thumbnail written succesfully");
                     res = true;
                 }else {
                     Log.d(TAG, "Thumbnail failed to write");
                     res = false;
                 }
 			}
 		}
 		return res;
 	}
 	
 	public boolean deleteLocalThumbnail(Egg egg) {
 		// TODO Implement if time 
 		return false;
 	}
 }
