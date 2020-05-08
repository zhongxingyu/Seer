 package com.webb.androidmosaic;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.io.StreamCorruptedException;
 import java.util.ArrayList;
 import java.util.List;
 
 import android.app.Application;
 import android.content.res.AssetManager;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.util.Log;
 
 import com.webb.androidmosaic.generation.AnalyzedImage;
 import com.webb.androidmosaic.generation.Configuration;
 import com.webb.androidmosaic.generation.preprocessor.Preprocessor;
 import com.webb.androidmosaic.generation.preprocessor.PreprocessorFactory;
 
 public class AndroidMosaicApp extends Application {
 	
	private static final int MAX_DUPLICATES = 2000;
 	private static final int NUMBER_OF_WIDTH_DIVISIONS = 9;
 	private static final int NUMBER_OF_DIVISIONS = 9;
 	private List<Bitmap> bitmaps;
 	private AssetManager mngr;
 	private String imageDirectory ="images";
 	private String analyzedImageDirectory = "analyzedImages";
 	private String bitmapDirectory = "bitmaps";
 	private Configuration cfg;
 	private Preprocessor preprocessor;
 	private List<AnalyzedImage> analyzedImages;
 	private static final String APP_START = "Application start up";
 	private static boolean DEBUG = true;
 	
 	
 	public void onCreate() {
 		bitmaps = new ArrayList<Bitmap>();
 		List<AnalyzedImage> dummyList = new ArrayList<AnalyzedImage>(); //TODO: replace this, just to satisfy config
 		String[] images = getListOfImages();
 		if(images == null) {
 			Log.e(APP_START, "Did not get images");
 		}
 		
 		File directoryBitMaps = getDir(bitmapDirectory, MODE_WORLD_READABLE);
 		if(directoryBitMaps == null) {
 			Log.e(APP_START, "Unable to get or create bitmap directory");
 		}
 		collectBitmaps(images, directoryBitMaps);
 		
 		cfg = new Configuration(dummyList, NUMBER_OF_DIVISIONS, NUMBER_OF_WIDTH_DIVISIONS, MAX_DUPLICATES);
 		preprocessor = PreprocessorFactory.getPreprocessor(cfg);
 		analyzedImages = preprocessor.analyze(bitmaps);
 		
 		File directoryAnalyzedImages = getApplicationContext().getDir(analyzedImageDirectory, MODE_PRIVATE);
 		if(directoryAnalyzedImages == null) {
 			Log.e(APP_START, "Unable to create or get analyzed images directory");
 		}
 		
 		saveAnalyzedImages(directoryAnalyzedImages, analyzedImages);
 		
 		Log.d(APP_START, "Images collected");
 		
 		
 		cfg.setImagePool(analyzedImages);
 		
 		if(DEBUG) {
 			AnalyzedImage imageTest = null;
 			try {
 				FileInputStream fis = new FileInputStream(directoryAnalyzedImages.getAbsolutePath() + "/0");
 				ObjectInputStream ois = new ObjectInputStream(fis);
 				imageTest = (AnalyzedImage) ois.readObject();
 				ois.close();
 			} catch (FileNotFoundException e) {
 				Log.e(APP_START, "Unable to open image file");
 				e.printStackTrace();
 			} catch (StreamCorruptedException e) {
 				Log.e(APP_START, "Object Stream corrupted");
 				e.printStackTrace();
 			} catch (IOException e) {
 				Log.e(APP_START, "Object could not be read");
 				e.printStackTrace();
 			} catch (ClassNotFoundException e) {
 				Log.e(APP_START, "Anaylzed image class not found");
 				e.printStackTrace();
 			}
 			Log.d(APP_START, imageTest.toString());
 		}
 		
 		
 	}
 
 	
 
 	private void saveAnalyzedImages(File directory,
 			List<AnalyzedImage> analyzedImages) {
 		for(int i=0; i<= analyzedImages.size()-1; i++) {
 			saveAnalyzedImage(directory, analyzedImages.get(i), i);
 		}
 	}
 
 
 	private void saveAnalyzedImage(File directory, AnalyzedImage analyzedImage, int id) {
 		FileOutputStream fos = null;
 		ObjectOutputStream oos = null;
 		File fileToSave = null;
 		analyzedImage.setFile(String.valueOf(id));
 		try {
 			fileToSave = new File(directory+"/"+id);
 			fos = new FileOutputStream(fileToSave);
 			oos = new ObjectOutputStream(fos);
 			oos.writeObject(analyzedImage);
 			oos.close();
 		} catch (FileNotFoundException e) {
 			Log.e(APP_START, "Was not able to open a new file");
 			e.printStackTrace();
 		} catch (IOException e) {
 			Log.e(APP_START, "Was not able to save analyzed image");
 			e.printStackTrace();
 		}
 		Log.i(APP_START, "Image successfully saved");
 	}
 
 
 	private void collectBitmaps(String[] images, File directoryBitMaps) {
 		InputStream input = null;
 		for(int i=0; i<= images.length-1; i++) {
 			try {
 				input = mngr.open(imageDirectory+"/"+images[i]);
 			} catch (IOException e) {
 				Log.e(APP_START, "Unable to open image file");
 			}
 			Bitmap bitmap = BitmapFactory.decodeStream(input);
 			saveBitMap(bitmap, directoryBitMaps, i);
 			bitmaps.add(BitmapFactory.decodeStream(input));
 		}
 	}
 	
 	
 	//This method should be consolidated with the saveAnalyzedImage() method
 	//
 	private void saveBitMap(Bitmap bitmap, File directoryBitMaps, int id) {
 		FileOutputStream fos = null;
 		File fileToSave = null;
 		try {
 			fileToSave = new File(directoryBitMaps+"/"+id);
 			fos = new FileOutputStream(fileToSave);
 			bitmap.compress(Bitmap.CompressFormat.JPEG, 70, fos);
 		} catch(IOException e) {
 			Log.e(APP_START, "Unable to compress and save bitmap");
 			e.printStackTrace();
 		}
 		Log.i(APP_START, "Bitmap saved to disk");
 		
 	}
 
 
 
 	String[] getListOfImages() {
 		mngr = getAssets();
 		String[] images = null;
 		try {
 			images = mngr.list(imageDirectory);
 		} catch (IOException e) {
 			Log.e(APP_START, "Got no images");
 		}
 		return images;
 	}
 	
 	public List<AnalyzedImage> getAnalyzedImages() {
 		return analyzedImages;
 	}
 	
 	public Configuration getCfg() {
 		return cfg;
 	}
 	
 	public String getImageDirectory() {
 		return imageDirectory;
 	}
 	
 	public String getBitmapDirectory() {
 		return bitmapDirectory;
 	}
 }
