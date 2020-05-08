 package ch.bazaruto.storage;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.Arrays;
 import java.util.Enumeration;
 import java.util.HashSet;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipFile;
 
 public class ZipStorage implements Storage {
 	
 	public static String SEP = File.separator;
 	
 	private ZipFile zipFile;
 
 	public ZipStorage(ZipFile zipFile) {
 		this.zipFile = zipFile;
 	}
 	
 	public String[] list(String path) {
 		HashSet<String> result = new HashSet<String>();
 		Enumeration<? extends ZipEntry> e = zipFile.entries();
 		String[] subPath = path.split(SEP);
 		if (path.equals("") || path.equals(".")) {
 			subPath = new String[0];
 		}
 		while (e.hasMoreElements()) {
 			ZipEntry entry = e.nextElement();
 			String[] zipElements = entry.getName().split(SEP);
 			if (subPath.length==0 && zipElements.length == 1) {
 				result.add(zipElements[0]);
 			} else if(zipElements.length == subPath.length + 1) {
 				String[] zipPath = Arrays.copyOfRange(zipElements, 0, subPath.length);
 				if (Arrays.equals(subPath, zipPath)) {
 					String element = zipElements[subPath.length];
 					result.add(element);
 				}
 			}
 		}
 		
 		return result.toArray(new String[0]);
 	}
 
 	public boolean exists(String path) {
		if (path.equals("") || path.equals(".")) {
			return true;
		}
 		return getZipEntry(path) != null;
 	}
 
 	public boolean isDirectory(String path) {
		if (path.equals("") || path.equals(".")) {
			return true;
		}
 		ZipEntry ze = getZipEntry(path);
 		return ze != null && ze.isDirectory();
 	}
 
 	public long lastModified(String path) {
 		ZipEntry ze = getZipEntry(path);
 		return ze != null ? ze.getTime() : 0;
 	}
 
 	public long length(String path) {
 		ZipEntry ze = getZipEntry(path);
 		return ze != null ? ze.getSize() : 0;
 	}
 
 	public InputStream open(String path) throws FileNotFoundException {
 		ZipEntry ze = getZipEntry(path);
 		try {
 			return zipFile.getInputStream(ze);
 		} catch (IOException e){
 			throw new FileNotFoundException(path);
 		}
 	}
 	
 	private ZipEntry getZipEntry(String path) {
 		Enumeration<? extends ZipEntry> e = zipFile.entries();
 		while(e.hasMoreElements()) {
 			ZipEntry entry = e.nextElement();
 			if (entry.getName().equals(path) ||
 				entry.getName().equals(path + SEP)) {
 				return entry;
 			}
 		}
 		return null;
 	}
 	
 }
