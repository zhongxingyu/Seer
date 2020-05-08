 package fr.univmlv.qroxy.cache;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.nio.ByteBuffer;
 
 import sun.nio.ch.FileKey;
 
 public class Cache {
 
 	private final static Cache instance = new Cache();
 	
 	public static Cache getInstance() {
 		return instance;
 	}
 	
 	public void addContentToCache(ByteBuffer buffer, String filename, String contentType, boolean append) throws IOException {
		contentType = contentType.split(";")[0];
 		File contentTypeF = new File(contentType);
 		filename = filename.replace("://", "_");
 		String[] f = filename.split("/");
 		filename = f[f.length -1];
 		StringBuilder arbo = new StringBuilder();
 		contentTypeF.mkdirs();
 		arbo.append(contentType).append("/");
 		for (int i = 0; i < f.length-1 ; i++) {
 			arbo.append(f[i]).append("/");
 			File rep = new File(arbo.toString());
 			rep.mkdir();
 		}
 		
 		File file = new File(arbo.toString(), filename);
 		FileOutputStream output = new FileOutputStream(file, append);
 		buffer.flip();
 		output.getChannel().write(buffer);
 		output.flush();
 		output.close();
 		//TODO create a reference to the file in cache
 	}
 	
 	public boolean isInCache(String filename, String contentType) {
 		//TODO if Configuration.isCacheShared CacheShared.search(filename)
 		return true;
 	}
 	
 	public boolean freeSpace(int neededSpace) {
 		//TODO try to remove older file or none used file
 		return true;
 	}
 	
 	public static void main(String[] args) throws IOException {
 		Cache cache = Cache.getInstance();
 		ByteBuffer buffer = ByteBuffer.allocate(10);
 		buffer.putInt(60);
		cache.addContentToCache(buffer, "http://www.facebook.com/joach//video/", "text/html", false);
 		//cache.addContentToCache(buffer, "http://www.facebook.com/joach/index.html", "text/html", true);
 	}
 }
