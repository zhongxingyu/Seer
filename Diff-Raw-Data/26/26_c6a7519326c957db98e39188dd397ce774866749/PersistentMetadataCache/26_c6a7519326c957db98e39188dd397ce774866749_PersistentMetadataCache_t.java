 package lotia.av.metadata.ffmpeg;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.nio.ByteBuffer;
 import java.nio.charset.Charset;
 import java.nio.file.Files;
 import java.nio.file.LinkOption;
 import java.nio.file.Path;
 import java.nio.file.attribute.BasicFileAttributes;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 
import org.apache.commons.codec.binary.Base64;
 
 public class PersistentMetadataCache {
 	
 	private static final Charset s_UTF8 = Charset.forName("UTF-8");
 	
 	private Path m_cacheDirectory;
 	
 	public PersistentMetadataCache(Path cacheDirectory) throws IOException {
 		m_cacheDirectory = cacheDirectory;
 		
 		if (!Files.exists(m_cacheDirectory)) {
 			Files.createDirectories(m_cacheDirectory);
 		}
 	}
 	
 	private String calculateFileName(Path file, BasicFileAttributes attrs) throws IOException {
 		
 		try {
 			MessageDigest digest = MessageDigest.getInstance("MD5");
 			
 			digest.update(file.getFileName().toString().getBytes(s_UTF8));
 			
 			if (attrs == null) {
 				attrs = Files.readAttributes(file, BasicFileAttributes.class, LinkOption.NOFOLLOW_LINKS);
 			}
 			
 			ByteBuffer buffer = ByteBuffer.allocate(16);
 			buffer.putLong(attrs.size());
 			buffer.putLong(attrs.lastModifiedTime().toMillis());
 			
 			digest.update(buffer);
 			
 			byte[] md5sig = digest.digest();
 			
			Base64 b64 = new Base64(true); // URL safe base64 encoder
			
			String cachedFileName = b64.encodeToString(md5sig);
			
			// for some reason Apache Commons encoder is adding a CRLF at the end of the URL safe encoding?
			if (cachedFileName.endsWith("\r\n")) {
				cachedFileName = cachedFileName.substring(0, cachedFileName.length() - 2);
			} else if (cachedFileName.endsWith("\n")) {
				cachedFileName = cachedFileName.substring(0, cachedFileName.length() - 1);
			}
 			
 			return cachedFileName;
 		} catch (NoSuchAlgorithmException e) {
 			return "Every implementation of the Java platform is required to support MD5, SHA-1, SHA-256. So what the heck.";
 		}
 	}
 	
 	public FFMPEGMetadata getMetadataForFile(Path file, BasicFileAttributes attrs) throws IOException {
 		
 		String cachedFileName = calculateFileName(file, attrs);
 		
 		Path cachedFilePath = m_cacheDirectory.resolve(cachedFileName);
 		File cachedFile = cachedFilePath.toFile();
 		
 		FFMPEGMetadata metadata = null;
 		
 		if (cachedFile.exists()) {
 					    
 		    try (
 		    	FileInputStream fis = new FileInputStream(cachedFile);
 		    	ObjectInputStream ois = new ObjectInputStream(fis);
 		    ) {
 			    metadata = (FFMPEGMetadata) ois.readObject();
 		    } catch (ClassNotFoundException e) {
 		    	// this probably means the persisted data is something else, or possibly an older version
 		    	// of the structure. If so, just ignore the cache entry and pretend we don't have it.
 		    	return null;
 		    }
 		}
 		
 		return metadata;
 	}
 
 	public void putMetadataForFile(Path file, BasicFileAttributes attrs, FFMPEGMetadata metadata) throws IOException {
 		
 		String cachedFileName = calculateFileName(file, attrs);
 		
 		Path cachedFilePath = m_cacheDirectory.resolve(cachedFileName);
 		File cachedFile = cachedFilePath.toFile();
 	    
 	    try (
 	    		FileOutputStream fos = new FileOutputStream(cachedFile);
 	    		ObjectOutputStream oos = new ObjectOutputStream(fos);
 	    ) {
 		    oos.writeObject(metadata);
 	    }
 	}
 }
