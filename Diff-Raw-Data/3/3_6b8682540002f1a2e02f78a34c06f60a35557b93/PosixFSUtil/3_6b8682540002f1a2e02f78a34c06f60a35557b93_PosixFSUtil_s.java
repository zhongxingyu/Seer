 package it.grid.storm.filesystem.util;
 
 import it.grid.storm.filesystem.swig.posixfs;
 
 public class PosixFSUtil {
 
 	String mountPoint;
 	posixfs fs;
 	
 	public PosixFSUtil(String[] args) {
 		if (args.length == 0)
 			mountPoint = "/";
 		fs = new posixfs(mountPoint);
 		long freeSpace = fs.get_free_space();
 		
 		System.out.format("Free space on FS mounted on %s: %d\n", mountPoint,
 				freeSpace);
 	}
 	
 	public static void main(String[] args) {
 		new PosixFSUtil(args);
 	}
 }
