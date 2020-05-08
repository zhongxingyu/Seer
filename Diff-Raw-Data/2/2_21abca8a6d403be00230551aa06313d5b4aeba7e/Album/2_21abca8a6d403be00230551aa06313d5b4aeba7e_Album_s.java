 package com.zoeetrope.lineupcamera.model;
 
 import java.io.File;
 import java.io.FileFilter;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.Date;
 
 import android.os.Environment;
 import android.util.Log;
 
 public class Album {
 
 	private final static String TAG = "Album";
 	public static final int MEDIA_TYPE_IMAGE = 1;
 
 	private ArrayList<Image> mImages;
 	private File mAlbumFolder;
 
 	public class ImageDateComparator implements Comparator<Image> {
 		@Override
 		public int compare(Image o1, Image o2) {
 			return o1.getModifiedDate().compareTo(o2.getModifiedDate());
 		}
 	}
 
 	public Date mLastModificationDate;
 	private String mName;
 
 	public Album(String name) {
 		this.mName = name;
 		this.mImages = new ArrayList<Image>();
 
 		this.loadImages();
 	}
 
 	public String getName() {
 		return mName;
 	}
 
 	public void setName(String name) {
 		mName = name;
 
 		this.loadImages();
 	}
 
 	public Date getLastModifiedDate() {
 		return mLastModificationDate;
 	}
 
 	public ArrayList<Image> getImages() {
 		return mImages;
 	}
 
 	public Image getLatestImage() {
 		if (mImages.size() > 0) {
 			return mImages.get(mImages.size() - 1);
 		} else {
 			return null;
 		}
 	}
 
 	public void saveNewImage(byte[] data) {
 		File mediaFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);
 
 		try {
 			FileOutputStream fos = new FileOutputStream(mediaFile);
 			fos.write(data);
 			fos.close();
 		} catch (FileNotFoundException e) {
 			Log.d(TAG, "File not found: " + e.getMessage());
 		} catch (IOException e) {
 			Log.d(TAG, "Error accessing file: " + e.getMessage());
 		}
 	}
 
 	private File getOutputMediaFile(int type) {
 		File mediaStorageDir = new File(
 				Environment
 						.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
 				"LineUpCamera");
 
 		// Create the storage directory if it does not exist
 		if (!mediaStorageDir.exists()) {
 			if (!mediaStorageDir.mkdirs()) {
 				Log.d(TAG, "failed to create directory");
 				return null;
 			}
 		}
 
 		File albumFolder = new File(mediaStorageDir, mName);
 		if (!albumFolder.exists()) {
 			if (!albumFolder.mkdirs()) {
 				Log.d(TAG, "failed to create directory");
 				return null;
 			}
 		}
 
 		// Create a media file name
 		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss")
 				.format(new Date());
 		File mediaFile;
 		if (type == MEDIA_TYPE_IMAGE) {
 			mediaFile = new File(albumFolder.getPath() + File.separator
 					+ "IMG_" + timeStamp + ".jpg");
 		} else {
 			return null;
 		}
 
 		return mediaFile;
 	}
 
 	private void loadImages() {
 		File mediaStorageDir = new File(
 				Environment
 						.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
 				"LineUpCamera");
 		mImages.clear();
 
 		mAlbumFolder = new File(mediaStorageDir, mName);
 		if (!mAlbumFolder.exists()) {
 			if (!mAlbumFolder.mkdirs()) {
 				Log.d(TAG, "failed to create directory");
 			}
 		}
 
 		File[] images = mAlbumFolder.listFiles(new FileFilter() {
 			public boolean accept(File file) {
 				return file.isFile();
 			}
 		});
 
 		for (File file : images) {
 			mImages.add(new Image(file));
 		}
 
 		Collections.sort(mImages, new ImageDateComparator());
 	}
 
 	public void remove(int position) {
 		mImages.get(position).remove();
 		mImages.remove(position);
 	}
 
 	public void removeAlbum() {
 		for (Image image : mImages) {
 			image.remove();
 		}
 
 		mAlbumFolder.delete();
 	}
 
 	public void renameAlbum(String newName) {
 		if (mAlbumFolder != null) {
 			String albumPath = mAlbumFolder.getAbsolutePath();
 			albumPath = albumPath.substring(0,
 					albumPath.lastIndexOf(File.separator));
 
 			File newFile = new File(albumPath + File.separator + newName);
 
 			if (mAlbumFolder.renameTo(newFile)) {
 				setName(newName);
 			}
 		}
 	}
 }
