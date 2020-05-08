 //TODO add path state change listener to upadte treeCache
 
 package fr.univmlv.qroxy.cache;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.nio.ByteBuffer;
 import java.nio.channels.Pipe;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 import java.util.HashMap;
 
 import fr.univmlv.qroxy.cache.tree.TreeCache;
 import fr.univmlv.qroxy.configuration.Configuration;
 
 public class Cache {
 	private final static Cache instance = new Cache();
 	private TreeCache tree = new TreeCache();
 	private HashMap<String, Long> sizeMap = new HashMap<String, Long>();
 	public static Cache getInstance() {
 		return instance;
 	}
 
 	// TODO name too long
 	// TODO not a directory
 	public void addContentToCache(ByteBuffer buffer, String url, String contentType, boolean append) throws IOException {
 		contentType = contentType.split(";")[0];
 		File contentTypeF = new File(contentType);
 		url = url.replace("://", "_");
 		String[] f = url.split("/");
 		url = f[f.length -1];
 		MessageDigest md = null;
 		try {
 			md = MessageDigest.getInstance("SHA-1");
 		} catch (NoSuchAlgorithmException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		byte[] sha1 = new byte[40];
 		md.update(url.getBytes(), 0, url.length());
 		sha1 = md.digest();
 		StringBuilder arbo = new StringBuilder();
 		contentTypeF.mkdirs();
 		arbo.append(contentType).append("/");
 		for (int i = 0; i < f.length-1 ; i++) {
 			arbo.append(f[i]).append("/");
 			File rep = new File(arbo.toString());
 			rep.mkdir();
 		}
 		StringBuilder hexSha1 = new StringBuilder();
 		for (int i=0;i<sha1.length;i++) {
 			hexSha1.append(Integer.toHexString(0xFF & sha1[i]));
 		}
 		File file = new File(arbo.toString(), hexSha1.toString());
 		FileOutputStream output = new FileOutputStream(file, append);
 		buffer.flip();
 		output.getChannel().write(buffer);
 		output.flush();
 		output.close();
 		long size = 0;
 		if (sizeMap.containsKey(contentType))
 			size = sizeMap.get(contentType);
 		long newSize = file.length()+size;
 		sizeMap.put(contentType, newSize);
 		tree.addPath(arbo.toString()+url);
 	}
 
 	public void getFromCache(String url, String contentType, Pipe.SinkChannel channel) throws IOException {
 		ByteBuffer buffer = ByteBuffer.allocate(262144);
 		contentType = contentType.split(";")[0];
 		url = url.replace("://", "_");
 		String filename;
 		MessageDigest md = null;
 		try {
 			md = MessageDigest.getInstance("SHA-1");
 		} catch (NoSuchAlgorithmException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		contentType = contentType.split(";")[0];
		url = url.replace("://", "_");
 		StringBuilder path = new StringBuilder(contentType).append("/");
 		String[] f = url.split("/");
		filename = f[f.length -1];
 		byte[] sha1 = new byte[40];
 		md.update(filename.getBytes(), 0, filename.length());
 		sha1 = md.digest();
 		StringBuilder hexSha1 = new StringBuilder();
 		for (int i=0;i<sha1.length;i++) {
 			hexSha1.append(Integer.toHexString(0xFF & sha1[i]));
 		}
 		for (int i = 0; i < f.length-1; i++) {
 			path.append(f[i]).append("/");
 		}
 		path.append(hexSha1);
 		File file =  new File(path.toString());
 		FileInputStream input = new FileInputStream(file);
 		while(input.getChannel().read(buffer) != -1){
 			buffer.flip();
 			channel.write(buffer);
 			buffer.compact();
 		}
 		channel.close();
 		input.close();
 	}
 
 	public boolean isUptodate(String url, String contentType){
 		return true;
 	}
 
 	public boolean isInCache(String url, String contentType) {
 		MessageDigest md = null;
 		String filename;
 		try {
 			md = MessageDigest.getInstance("SHA-1");
 		} catch (NoSuchAlgorithmException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		contentType = contentType.split(";")[0];
 		url = url.replace("://", "_");
 		StringBuilder path = new StringBuilder(contentType).append("/");
 		String[] f = url.split("/");
 		filename = f[f.length -1];
 		byte[] sha1 = new byte[40];
 		md.update(filename.getBytes(), 0, filename.length());
 		sha1 = md.digest();
 		StringBuilder hexSha1 = new StringBuilder();
 		for (int i=0;i<sha1.length;i++) {
 			hexSha1.append(Integer.toHexString(0xFF & sha1[i]));
 		}
 		for (int i = 0; i < f.length-1; i++) {
 			path.append(f[i]).append("/");
 		}
 		path.append(hexSha1);
 		File file =  new File(path.toString());
 		if(file.exists() && !file.isDirectory())
 			return this.isUptodate(url, contentType);
 		return false;
 	}
 
 	public boolean freeSpace(long neededSpace, String contentType) {
 		long size = 0;
 		if (sizeMap.containsKey(contentType))
 			size = sizeMap.get(contentType);
 		if((Configuration.getInstance().getConfForType(contentType).getSize() - size) > neededSpace)
 			return true;
 		long deletedSize = 0;
 		while(deletedSize < neededSpace){
 			String filename = tree.getSmallerWeightPath();
 			File fileToDelete = new File(filename);
 			deletedSize = deletedSize + fileToDelete.length();
 			if(fileToDelete.delete())
 				tree.removePath(filename);
 		}
 		long newSize = sizeMap.get(contentType) - deletedSize;
 		sizeMap.put(contentType, newSize);
 		return true;
 	}
 
 	public static void main(String[] args) throws IOException, NoSuchAlgorithmException {
 		Cache cache = Cache.getInstance();
 		ByteBuffer buffer = ByteBuffer.allocate(200);
 		buffer.put("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA".getBytes());
 		cache.addContentToCache(buffer, "http://www.facebook.com/joach/index.html", "text/html", false);
 		cache.addContentToCache(buffer, "http://www.facebook.com/index.html", "text/html", false);
 		cache.addContentToCache(buffer, "http://www.google.com/index.html", "text/html", true);
 		System.out.println(cache.isInCache("http://www.facebook.com/joach/index.html", "text/html"));
 		System.out.println(cache.isInCache("http://www.google.com/index.html", "text/html"));
		System.out.println(cache.isInCache("http://www.google.com/toto.html", "text/html"));
 	}
 }
