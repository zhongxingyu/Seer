 package com.quigley.filesystem;
 
 import java.io.File;
 
 public class FilesystemIterator {
     public FilesystemIterator(FilesystemVisitor visitor) {
         this.visitor = visitor;
     }
 
     public void iterate(FilesystemPath path) throws FilesystemException {
     	File pathF = path.asFile();
     	if(pathF.exists() && pathF.canRead()) {
    		visitor.visit(path);
     		if(pathF.isDirectory()) {
     			File[] contents = pathF.listFiles();
     			for(File f : contents) {
     				FilesystemPath fPath = new FilesystemPath(path).add(f.getName());
    				iterate(fPath);
     			}
     		}
     		
     	} else {
     		throw new FilesystemException("Unable to read '" + path.asString() + "'!");
     	}
     }
     
     private FilesystemVisitor visitor;
 }
