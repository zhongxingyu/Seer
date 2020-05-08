 package codemate.utils;
 
 import java.io.*;
 import java.util.*;
 
 public class SystemUtils {
 	/**
 	 * getSubdirectories
 	 * 
 	 * This method gets the subdirectories that under the given root. The hidden
 	 * subdirectories are ignored.
 	 * 
 	 * @param 	root		The root directory
 	 * @return	List<File>	The subdirectory list
 	 * 
 	 * @author Li Dong <dongli@lasg.iap.ac.cn>
 	 */
 	public static List<File> getSubdirectories(File root) {
 		List<File> subdirectories = new ArrayList<File>();
 		if (root.isHidden()) return subdirectories;
 		for (String fileName : root.list()) {
 			File file = new File(root+"/"+fileName);
 			if (file.isDirectory()) {
 				subdirectories.add(file);
 				subdirectories.addAll(getSubdirectories(file));
 			}
 		}
 		return subdirectories;
 	}
	
	public static String getAbsolutePath(File file) {
		String res = file.getAbsolutePath();
		res = res.replace("/.", "");
		return res;
	}
	
	public static String getAbsolutePath(String fileName) {
		File file = new File(fileName);
		return getAbsolutePath(file);
	}
 }
