 package com.mmmeff.vzwgs3.ez_recovery;
 
 import java.io.DataOutputStream;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.io.OutputStreamWriter;
 
 import android.annotation.SuppressLint;
 import android.content.Context;
 import android.content.res.AssetManager;
 import android.util.Log;
 
 @SuppressLint("SdCardPath")// this only runs on one device - hardcoding acceptable here!
 public class FileMan {
 
 	/** logcat tag **/
 	private static final String TAG = "ez_recovery";
 
 	/** Context of the Main Activity **/
 	private Context context;
 
 	/** the version of the app - used for version-stamping assets **/
	private String VERSION = context.getString(R.string.version);
 
 	/** Location of the assets directory on the sdcard **/
 	private final String ASSET_LOCATION = "/sdcard/gs3ezrecovery";
 
 	/**
 	 * Default constructor for the File Manager. This class handles all the file
 	 * manipulation required by the application, including set up, deletion, and
 	 * copying of files needed for flashing to the SDCard
 	 * 
 	 * @param context
 	 *            The context of the main activity
 	 */
 	public FileMan(Context context) {
 
 		this.context = context;

 		// check if files for the current version already exist.
 		switch (CheckForExistence()) {
 		case UPTODATE: // all set, we're done here!
 			break;
 		case OUTDATED: // flush the directory then rewrite it to sdcard
 			FlushDirectory();
 			PlaceDirectory();
 			CopyAssets();
 			StampDirectory();
 			break;
 		case UNEXISTENT: // copy assets over to sdcard
 			PlaceDirectory();
 			CopyAssets();
 			StampDirectory();
 			break;
 		}
 
 	}
 
 	/**
 	 * Deletes the asset directory on the sdcard
 	 */
 	private void FlushDirectory() {
 		File dir = new File(ASSET_LOCATION);
 		if (dir.isDirectory()) {
 	        String[] children = dir.list();
 	        for (int i = 0; i < children.length; i++) {
 	            new File(dir, children[i]).delete();
 	        }
 		}
 		dir.delete();
 
 	}
 
 	/**
 	 * Creates an empty directory for our assets on the sdcard
 	 */
 	private void PlaceDirectory() {
 		File dir = new File(ASSET_LOCATION + "/");
 		dir.mkdirs();
 	}
 
 	/**
 	 * Copies the assets for flashing to the sdcard
 	 */
 	private void CopyAssets() {
 		AssetManager assetManager = context.getAssets();
 		String[] files = null;
 		try {
 			files = assetManager.list("");
 		} catch (IOException e) {
 			Log.e("tag", e.getMessage());
 		}
 		
 		for (String filename : files) {
 			InputStream in = null;
 			OutputStream out = null;
 			try {
 				in = assetManager.open(filename);
 				out = new FileOutputStream("/sdcard/gs3ezrecovery/" + filename);
 				CopyFile(in, out);
 				in.close();
 				in = null;
 				out.flush();
 				out.close();
 				out = null;
 			} catch (Exception e) {
 				Log.e("tag", e.getMessage());
 			}
 		}
 
 	}
 	
 	/** Copies an inputstream to an outputstream - used by CopyAssets() **/
 	private void CopyFile(InputStream in, OutputStream out) throws IOException {
 		byte[] buffer = new byte[1024];
 		int read;
 		while ((read = in.read(buffer)) != -1) {
 			out.write(buffer, 0, read);
 		}
 	}
 
 	/**
 	 * Touches an empty file named the current version so we can keep track of
 	 * the up-to-date-ness of our assets
 	 */
 	@SuppressLint("WorldReadableFiles")
 	private void StampDirectory() {
 		try {
 			//create the file
			FileOutputStream file_out = context.openFileOutput(ASSET_LOCATION
					+ "/" + VERSION, Context.MODE_WORLD_READABLE);
 			OutputStreamWriter osw = new OutputStreamWriter(file_out); 
 
 	       // leave the file empty
 	       osw.write("");
 
 	       //close the file out
 	       osw.flush();
 	       osw.close();
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 			Log.e(TAG, e.getMessage());
 		} catch (IOException e) {
 			e.printStackTrace();
 			Log.e(TAG, e.getMessage());
 		}
 	}
 
 	/**
 	 * Checks if our assets are in place by checking for a version stamp
 	 * 
 	 * @return FileExistence.UPTODATE if assets are up to date
 	 *         FileExistence.OUTDATED if assets are outdated
 	 *         FileExistence.UNEXISTENT if directory/files are not found
 	 */
 	private FileExistence CheckForExistence() {
		File file = context.getFileStreamPath(ASSET_LOCATION);
 		if (file.isDirectory()) {
 			// directory exists, so check if the files are up to date
			File stamp = context.getFileStreamPath(ASSET_LOCATION + "/"
					+ VERSION);
 			if (stamp.exists()) {
 				return FileExistence.UPTODATE;
 			} else
 				return FileExistence.OUTDATED;
 		} else
 			return FileExistence.UNEXISTENT;
 	}
 
 	private enum FileExistence {
 		UPTODATE, OUTDATED, UNEXISTENT;
 	}
 
 
 }
