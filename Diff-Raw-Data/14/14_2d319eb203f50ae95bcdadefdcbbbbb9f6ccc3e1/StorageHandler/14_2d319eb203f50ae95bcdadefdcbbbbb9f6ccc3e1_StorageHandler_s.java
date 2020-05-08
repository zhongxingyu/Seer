 package ch.hsr.traildevil.util.persistence;
 
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.io.Serializable;
 
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.drawable.BitmapDrawable;
 import android.graphics.drawable.Drawable;
import android.util.DisplayMetrics;
 import android.util.Log;
 import ch.hsr.traildevil.util.Constants;
 
 class BitmapDataObject implements Serializable {
 	private static final long serialVersionUID = 210784625591104095L;
 	public byte[] imageByteArray;
 }
 
 public class StorageHandler {
 
 	private static final String TAG = "traildevil";
 	private static final String TAG_PREFIX = StorageHandler.class.getSimpleName() + ": ";
 
 	public void storeDrawableToStorage(Drawable drawable, String filename) {
 		File file = new File(Constants.DB_LOCATION, filename);
 
 		if (file.exists()) {
 			Log.i(TAG, TAG_PREFIX + " File " + file.getAbsolutePath() + " already exists on storage! Nothing to store. ");
 		} else {
 			BitmapDataObject obj = createStorableObject(drawable);
 			store(obj, file);
 		}
 	}
 
 	private BitmapDataObject createStorableObject(Drawable drawable) {
 		Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
 		BitmapDataObject bitmapDataObject = new BitmapDataObject();
 		ByteArrayOutputStream out = new ByteArrayOutputStream();
 		try {
 			bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
 			bitmapDataObject.imageByteArray = out.toByteArray();
 			out.close();
 		} catch (IOException e) {
 			Log.e(TAG, TAG_PREFIX + " Failed to close ByteArrayOutputStream");
 		}
 		return bitmapDataObject;
 	}
 
 	private void store(BitmapDataObject obj, File file) {
 		ObjectOutputStream out = null;
 		try {
 			out = new ObjectOutputStream(new FileOutputStream(file));
 			out.writeObject(obj);
 			Log.i(TAG, TAG_PREFIX + " File " + file.getAbsolutePath() + " successfully stored!");
 		} catch (Exception e) {
 			Log.e(TAG, TAG_PREFIX + " Failed writing Object to storage", e);
 		} finally {
 			try {
 				out.close();
 			} catch (IOException e) {
 				Log.e(TAG, TAG_PREFIX + " Failed to close ObjectOutputStream", e);
 			}
 		}
 	}
 
 	public Drawable getDrawableFromStorage(String filename) {
 		File file = new File(Constants.DB_LOCATION, filename);
 
 		if (file.exists()) {
 			Log.i(TAG, TAG_PREFIX + " File " + file.getAbsolutePath() + " already exists on storage! Reading from storage.");
 			return read(file);
 		}
 		Log.i(TAG, TAG_PREFIX + " File " + file.getAbsolutePath() + " doesn't exist on storage! Nothing to read.");
 
 		return null;
 	}
 
 	private Drawable read(File file) {
 		ObjectInputStream in = null;
 		Bitmap bitmap = null;
 		try {
 			in = new ObjectInputStream(new FileInputStream(file));
 			BitmapDataObject obj = (BitmapDataObject) in.readObject();
			bitmap = BitmapFactory.decodeByteArray(obj.imageByteArray, 0, obj.imageByteArray.length);
			bitmap.setDensity(DisplayMetrics.DENSITY_HIGH);
 		} catch (Exception e) {
 			Log.e(TAG, TAG_PREFIX + " Failed reading Object from storage", e);
 		} finally {
 			try {
 				in.close();
 			} catch (IOException e) {
 				Log.e(TAG, TAG_PREFIX + " Failed to close ObjectInputStream", e);
 			}
 		}
 		return new BitmapDrawable(bitmap);
 	}

 }
