 package org.imaginationforpeople.android2.helper;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.Arrays;
 import java.util.Date;
 
 import android.content.Context;
 
 public class DataHelper extends BaseHelper {
 	public static final int CONTENT_NUMBER = 3;
 	
 	public static final int CONTENT_BEST = 0;
 	public static final int CONTENT_LATEST = 1;
 	public static final int CONTENT_COUNTRY = 2;
 	
 	public static final String BEST_PROJECTS_KEY = "bestProjects";
 	public static final String LATEST_PROJECTS_KEY = "latestProjects";
 	public static final String PROJECT_VIEW_KEY = "project";
 	
 	public static final String FILE_PREFIX_PROJECT_THUMB = "projectThumb_";
 	public static final String FILE_PREFIX_PROJECT_IMAGE = "projectImage_";
 	public static final String FILE_PREFIX_USER_AVATAR = "avatar_";
 	
 	public static final int PROJECT_GALLERY_GRID_TYPE_PICTURE = 1000;
 	public static final int PROJECT_GALLERY_GRID_TYPE_VIDEO = 1001;
 	
 	public static final int PROJECT_RANDOM = -1;
 	
 	private DataHelper() {}
 	
 	public static FileOutputStream openFileOutput(String name) {
 		try {
 			return getContext().openFileOutput(name, Context.MODE_PRIVATE);
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 			return null;
 		}
 	}
 	
 	public static FileInputStream openFileInput(String name) {
 		try {
 			return getContext().openFileInput(name);
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 			return null;
 		}
 	}
 	
 	public static boolean checkFile(String url, String prefix) {
 		try {
 			String path = new URL(url).getPath();
 			return Arrays.asList(getContext().fileList()).contains(prefix + path.substring(path.lastIndexOf('/') + 1));
 		} catch (MalformedURLException e) {
 			e.printStackTrace();
 			return false;
 		}
 	}
 	
 	public static boolean checkThumbFile(String url) {
 		return checkFile(url, FILE_PREFIX_PROJECT_THUMB);
 	}
 	
 	public static boolean checkImageFile(String url) {
 		return checkFile(url, FILE_PREFIX_PROJECT_IMAGE);
 	}
 	
 	public static boolean checkAvatarFile(String url) {
 		return checkFile(url, FILE_PREFIX_USER_AVATAR);
 	}
 	
 	public static void removeOldFiles() {
		long oneday = new Date().getTime() - 86400; // One day
		for(String path : getContext().fileList()) {
			File file = new File(getContext().getFilesDir(), path);
			if(file.lastModified() < oneday) {
				getContext().deleteFile(path);
 			}
 		}
 	}
 }
