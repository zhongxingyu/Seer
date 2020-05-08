 package it.grid.storm.filesystem.util;
 
 import java.io.File;
 
 import it.grid.storm.filesystem.swig.posixfs;
 
 public class PosixFSUtil {
 
 	String mountPoint;
 	String file;
 	posixfs fs;
 	
 	public PosixFSUtil(String[] args) {
 		
 		if (args.length != 2){
 			System.err.println("usage: PosixFSUtil fsMountPoint fileToCheck");
 			System.exit(1);
 		}
 			
 		mountPoint = args[0];
 		file = args[1];
 		
 		fs = new posixfs(mountPoint);
 		long freeSpace = fs.get_free_space();
 		
		System.out.format("Free space on FS mounted on %s in bytes: %d", mountPoint, freeSpace);
 		
 		File f = new File(file);
 		if (!f.exists()){
 			System.err.println("File not found: "+file);
 			System.exit(1);
 		}
 		
 		long fileSize = fs.get_size(file);
 		System.out.println("File size: "+fileSize);
 		
 	}
 	
 	public static void main(String[] args) {
 		new PosixFSUtil(args);
 	}
 }
