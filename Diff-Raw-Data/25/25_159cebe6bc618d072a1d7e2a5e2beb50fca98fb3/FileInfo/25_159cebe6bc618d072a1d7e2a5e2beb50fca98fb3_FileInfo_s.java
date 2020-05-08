 package controller.upload;
 /**
  * 
  * @author shuklp
  *
  */
 public class FileInfo {
 
 	String pDestFilePath; 
 	String pFileName;
 	String pLibPath;
 	/**
 	 * 
 	 * @param destFilePath
 	 * @param fileName
 	 * @param libPath
 	 */
 	public FileInfo(String destFilePath, String fileName, String libPath) {
 		this.pDestFilePath =  destFilePath;
 		this.pFileName = fileName;
 		this.pLibPath = libPath;
 	}
 	/**
 	 * 
 	 * @return
 	 */
 	public String getDestFilePath() {
 		return pDestFilePath;
 	}
 	
 	/**
 	 * 
 	 * @return
 	 */
 	public String getFileName() { 
		return (getDestFilePath() + pFileName);
 	}
 	/**
 	 * 
 	 * @return
 	 */
 	public String getLibPath() {
 		return pLibPath;
 	}
 }
