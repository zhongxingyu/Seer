 package com.quigley.filesystem;
 
 import java.io.File;
 
 public class FilesystemIterator {
     public FilesystemIterator(FilesystemVisitor visitor) {
         this.visitor = visitor;
     }
 
     public void iterate(FilesystemPath path) throws FilesystemException {
     	File pathF = path.asFile();
     	if(pathF.exists() && pathF.canRead()) {
     		if(pathF.isDirectory()) {
    			visitor.visit(path);
    			
     			File[] contents = pathF.listFiles();
     			for(File f : contents) {
     				FilesystemPath fPath = new FilesystemPath(path).add(f.getName());
    				visitor.visit(fPath);
     			}
    		} else {
    			visitor.visit(path);
     		}
     		
     	} else {
     		throw new FilesystemException("Unable to read '" + path.asString() + "'!");
     	}
     }
     
     private FilesystemVisitor visitor;
 }
