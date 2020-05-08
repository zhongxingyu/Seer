 package no.f12.agiledeploy.deployer;
 
 import java.io.File;
 
 public interface FileSystemAdapter {
 
 	public void copyFile(File source, File target);
 
 	public void deleteDir(File deployDirectory);
 
 	public void moveOneUp(File file);
 
 	public void createSymbolicLink(File source, File symLink);
 
 
 }
