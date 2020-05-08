 package pl.edu.pw.rso2012.a1.dvcs.view.tree;
 
 import java.io.File;
 import java.util.Set;
 
 /**
  * 
  * @author Andrzej Makarewicz
  * 
  */
 public class FileInfo {
 	
 	private final File file;
 	private boolean isVersioned;
 	private final int rootPathLength;
 	
 	FileInfo(File file, int rootPathLength){
 		if (file == null) throw new NullPointerException();
 		
 		this.file = file;
 		this.rootPathLength = rootPathLength;
 	}
 	
 	public File getFile() {
 		return file;
 	}
 	
 	public boolean isVersioned() {
 		return isVersioned;
 	}
 	
 	public void setVersioned(boolean isVersioned) {
 		this.isVersioned = isVersioned;
 	}
 	
 	public void setVersioned(Set<String> versionedFiles){
 		this.isVersioned = versionedFiles.contains(getFilePathFromRoot());
 	}
 	
 	public String getFilePathFromRoot(){
		return file.getAbsolutePath().substring(rootPathLength);
 	}
 	
 	@Override
 	public String toString() {
 		return file.getName();
 	}
 	
 }
