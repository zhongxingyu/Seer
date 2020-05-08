 package com.emergentideas.webhandle.files;
 
 import java.io.File;
 import java.lang.reflect.Array;
 import java.lang.reflect.Method;
 import java.util.Arrays;
 import java.util.Date;
 import java.util.Map;
 
 import com.emergentideas.utils.DateUtils;
 import com.emergentideas.utils.ReflectionUtils;
 
 public class FileInfo {
 	
 	public enum FileType { FILE, DIRECTORY }
 
 	protected static Boolean java7 = null;
 	protected static Method toPath = null;
 	protected static Method readAttributes = null;
 	protected static Object linkOptionsArrayForCall = null;
 	
 	/**
 	 * Returns a file info object with a few io ops as possible using Java 7 
 	 * code if possible, legacy if not. Returns null if the file does not exist.
 	 * @param file
 	 * @return
 	 */
	public static FileInfo getInfo(File file) {
 		if(isJava7()) {
 			try {
 				initReflectionObjects();
 				Object path = toPath.invoke(file);
 				
 				Map<String, Object> attrs = (Map<String, Object>)readAttributes.invoke(null, path, "*", linkOptionsArrayForCall);
 				String last = attrs.get("lastModifiedTime").toString();
 				FileType type = (Boolean)attrs.get("isDirectory") ? FileType.DIRECTORY : FileType.FILE;
 				return new FileInfo(last, type);
 			}
 			catch(Exception e) {
 				// This is okay. It just means that there's no file at the path.
 			}
 		}
 		else {
 			if(file.exists()) {
 				FileType type = file.isDirectory() ? FileType.DIRECTORY : FileType.FILE;
 				long last = file.lastModified();
 				String sLast = DateUtils.javaNIODateFormat().format(new Date(last));
 				return new FileInfo(sLast, type);
 			}
 		}
 		
 		return null;
 	}
 	
 	protected static void initReflectionObjects() throws ClassNotFoundException, NoSuchMethodException {
 		if(toPath == null && isJava7()) {
 			toPath = ReflectionUtils.getFirstMethod(File.class, "toPath");
 			Class files = ReflectionUtils.getClassForName("java.nio.file.Files");
 			Class path = ReflectionUtils.getClassForName("java.nio.file.Path");
 			Class linkOption = ReflectionUtils.getClassForName("java.nio.file.LinkOption");
 			Class linkOptionArray = ReflectionUtils.getArrayStyle(linkOption);
 			linkOptionsArrayForCall = Array.newInstance(linkOption, 0);
 			readAttributes = files.getMethod("readAttributes", path, String.class, linkOptionArray);
 			
 		}
 	}
 	
 	protected static boolean isJava7() {
 		if(java7 == null) {
 			try {
 				java7 = ReflectionUtils.getClassForName("java.nio.file.Path") != null;
 			}
 			catch(ClassNotFoundException e) {
 				java7 = false;
 			}
 		}
 		return java7;
 	}
 
 	
 	
 	protected String lastModified;
 	protected FileType type;
 
 	public FileInfo() {}
 	
 	
 	public FileInfo(String lastModified, FileType type) {
 		super();
 		this.lastModified = lastModified;
 		this.type = type;
 	}
 
 	public String getLastModified() {
 		return lastModified;
 	}
 
 	public void setLastModified(String lastModified) {
 		this.lastModified = lastModified;
 	}
 
 	public FileType getType() {
 		return type;
 	}
 
 	public void setType(FileType type) {
 		this.type = type;
 	}
 	
 	
 	
 }
