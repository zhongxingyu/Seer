 package com.quigley.filesystem;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.nio.channels.FileChannel;
 import java.util.ArrayList;
 import java.util.List;
 
 public class FilesystemOperation {
 	public static void copy(FilesystemPath source, FilesystemPath dest) throws FilesystemException {
 		copy(source.asFile(), dest.asFile());
 	}
 
 	public static void copy(File source, File dest) throws FilesystemException {
 		try {
			FileChannel sourceChannel = new FileInputStream(source).getChannel();
			FileChannel destChannel = new FileOutputStream(dest).getChannel();
 
 			sourceChannel.transferTo(0, sourceChannel.size(), destChannel);
 
			sourceChannel.close();
			destChannel.close();

 		} catch (Exception e) {
 			throw new FilesystemException("Unable to copy", e);
 		}
 	}
 	
 	public static List<FilesystemPath> copyTree(FilesystemPath sourceTree, FilesystemPath destTree, List<String> includeTokens, List<String> excludeTokens) throws FilesystemException {
 		FilesystemInventoryVisitor visitor = new FilesystemInventoryVisitor();
 		visitor.setExcludeTokens(excludeTokens);
 		visitor.setIncludeTokens(includeTokens);
 		visitor.setIncludeDirectories(false);
 		FilesystemIterator iterator = new FilesystemIterator(sourceTree, visitor);
 		iterator.iterate();
 		
 		List<FilesystemPath> copiedFiles = new ArrayList<FilesystemPath>();
 		for(FilesystemPath sourcePath : visitor.getPaths()) {
 			FilesystemPath relativeSourcePath = FilesystemPath.removeCommonParent(sourcePath, sourceTree);
 			copiedFiles.add(relativeSourcePath);
 			FilesystemPath destPath = destTree.add(relativeSourcePath);
 			destPath.parent().asFile().mkdirs();
 			copy(sourcePath, destPath);
 		}
 		
 		return copiedFiles;
 	}
 
 	public static String readFileAsString(File path) throws IOException {
 		StringBuilder fileData = new StringBuilder(1000);
 		BufferedReader reader = new BufferedReader(new FileReader(path));
 
 		char[] buf = new char[1024];
 		int numRead = 0;
 		while ((numRead = reader.read(buf)) != -1) {
 			fileData.append(buf, 0, numRead);
 		}
 		reader.close();
 
 		return fileData.toString();
 	}
 	
 	public static void writeStringToFile(String data, File path) throws IOException {
 		FileWriter writer = new FileWriter(path);
 		writer.write(data);
 		writer.close();
 	}
 	
 	public static boolean recursiveDelete(File path) {
 		if(path.exists()) {
 			if(path.isDirectory()) {
 				for(String child : path.list()) {
 					if(!recursiveDelete(new File(path, child))) {
 						return false;
 					}
 				}
 			}
 			return path.delete();
 		}
 		return false;
 	}
 }
