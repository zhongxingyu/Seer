 package org.araqne.storage.localfile;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.araqne.storage.api.FilePath;
 import org.araqne.storage.api.FilePathNameFilter;
 import org.araqne.storage.api.StorageInputStream;
 import org.araqne.storage.api.StorageOutputStream;
 
 public class LocalFilePath implements FilePath {
 	static final String PROTOCOL_NAME = "file";
 	static final String PROTOCOL_STRING = "file://";
 	private final File path;
 	
 	public LocalFilePath(File path) {
 		this.path = path;
 	}
 	
 	public LocalFilePath(String pathStr) {
 		if (pathStr.startsWith(PROTOCOL_STRING))
 			pathStr = pathStr.substring(7);
 		
 		this.path = new File(pathStr);
 	}
 
 	@Override
 	public StorageInputStream newInputStream() throws IOException {
 		return new LocalFileInputStream(this);
 	}
 	
 	@Override
 	public StorageOutputStream newOutputStream(boolean append) throws IOException {
 		return new LocalFileOutputStream(this, append);
 	}
 	
 	@Override
 	public String getAbsolutePath() {
 		return path.getAbsolutePath();
 	}
 
 	@Override
 	public String getName() {
 		return path.getName();
 	}
 	
 	public File getFile() {
 		return path;
 	}
 
 	@Override
 	public boolean exists() {
 		return path.exists();
 	}
 
 	@Override
 	public long length() {
 		return path.length();
 	}
 
 	@Override
 	public boolean mkdirs() {
 		return path.mkdirs();
 	}
 
 	@Override
 	public boolean isDirectory() {
 		return path.isDirectory();
 	}
 
 	@Override
 	public FilePath newFilePath(String child) {
 		return new LocalFilePath(new File(path, child));
 	}
 
 	@Override
 	public FilePath[] listFiles() {
 		File[] files = path.listFiles();
 		FilePath[] ret = new FilePath[files.length];
 		for (int i = 0; i < ret.length; ++i) {
 			ret[i] = new LocalFilePath(files[i]);
 		}
 		                              
 		return ret;
 	}
 
 	@Override
 	public boolean isFile() {
 		return path.isFile();
 	}
 
 	@Override
 	public boolean delete() {
 		return path.delete();
 	}
 
 	@Override
 	public FilePath[] listFiles(FilePathNameFilter filter) {
 		File[] files = path.listFiles();
 		if (files == null)
 			return null;
 		
 		List<FilePath> ret = new ArrayList<FilePath>();
 		for (File f : files) {
 			FilePath curr = new LocalFilePath(f);
			if (filter.accept(this, f.getName()))
 				ret.add(curr);
 		}
 		                              
 		return (FilePath[]) (ret.toArray(new FilePath[0]));
 	}
 
 	@Override
 	public FilePath getParentFilePath() {
 		File parentFile = path.getParentFile();
 
 		if (parentFile == null)
 			return null;
 		
 		return new LocalFilePath(parentFile);
 	}
 	
 	@Override
 	public boolean equals(Object o) {
 		if (!(o instanceof LocalFilePath))
 			return false;
 		LocalFilePath rhs = (LocalFilePath) o;
 		return path.equals(rhs);
 	}
 	
 	@Override
 	public int hashCode() {
 		return ("file://" + path.getAbsolutePath()).hashCode();
 	}
 
 	@Override
 	public long getFreeSpace() {
 		return path.getFreeSpace();
 	}
 
 	@Override
 	public long getUsableSpace() {
 		return path.getUsableSpace();
 	}
 
 	@Override
 	public long getTotalSpace() {
 		return path.getTotalSpace();
 	}
 
 	@Override
 	public int compareTo(FilePath o) {
 		if (o instanceof LocalFilePath) {
 			LocalFilePath rhs = (LocalFilePath) o;
 			return path.compareTo(rhs.path);
 		}
 		
 		return getProtocol().compareTo(o.getProtocol());
 	}
 
 	@Override
 	public String getProtocol() {
 		return PROTOCOL_NAME;
 	}
 
 	@Override
 	public String getCanonicalPath() throws IOException {
 		return path.getCanonicalPath();
 	}
 
 	@Override
 	public boolean renameTo(FilePath dest) {
 		if (!(dest instanceof LocalFilePath))
 			return false;
 		
 		return path.renameTo(((LocalFilePath)dest).path);
 	}
 
 	@Override
 	public boolean canRead() {
 		return path.canRead();
 	}
 
 	@Override
 	public boolean canWrite() {
 		return path.canWrite();
 	}
 
 	@Override
 	public FilePath createTempFilePath(String prefix, String suffix) throws IOException {
 		return new LocalFilePath(File.createTempFile(prefix, suffix, path));
 	}
 
 	@Override
 	public FilePath getAbsoluteFilePath() {
 		return new LocalFilePath(path.getAbsoluteFile());
 	}
 
 	@Override
 	public char getSeperatorChar() {
 		return File.separatorChar;
 	}
 
 }
