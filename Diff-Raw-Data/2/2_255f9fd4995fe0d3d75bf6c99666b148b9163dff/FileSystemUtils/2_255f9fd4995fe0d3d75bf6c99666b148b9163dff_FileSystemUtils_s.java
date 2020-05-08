 package com.dc2f.technologyplayground.modeshape;
 
 import java.io.File;
 
 import javax.jcr.Node;
 
 public class FileSystemUtils {
 	private FileSystemUtils() {
 	}
 	
	FileSystemUtils getInstance() {
 		return null;
 	}
 	
 	/**
 	 * imports given rootFolder recursively into baseNode of repository.
 	 * e.g. rootFolder(/etc) baseNode(/blah) -> /etc/passwd == /blah/passwd
 	 * 
 	 * If a file exists, it will be overwritten.
 	 * 
 	 * @param rootFolder root folder from where to import
 	 * @param baseNode import relative paths from rootFolder into the repository starting at baseNode
 	 */
 	void load(File rootFolder, Node baseNode) {
 		
 	}
 }
