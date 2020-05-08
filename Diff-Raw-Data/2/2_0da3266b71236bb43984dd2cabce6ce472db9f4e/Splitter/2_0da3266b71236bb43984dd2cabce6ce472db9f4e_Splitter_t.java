 package com.my.splitter.file;
 
 import java.io.File;
 import java.io.IOException;
 import java.nio.channels.FileChannel;
 import java.nio.file.FileAlreadyExistsException;
 import java.nio.file.Path;
 import java.nio.file.Paths;
 import java.nio.file.StandardOpenOption;
 import java.util.EnumSet;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * Class for breaking file to pieces
  * 
  * @author mike
  * 
  */
 public class Splitter {
 
 	private static final EnumSet<StandardOpenOption> CREATE_NEW_FILE_OPTIONS = EnumSet.of(StandardOpenOption.CREATE_NEW,
 	StandardOpenOption.WRITE);
 
 	private final static Logger log = LoggerFactory.getLogger(OperationResult.class);
 	
 	private File file;
 
 	public Splitter(File file) {
 		this.file = file;
 	}
 
 	/**
 	 * Creates files that are copies of the original file parts in sequence
 	 * 
 	 * @param size
 	 *            - the size of the parts in byte
 	 */
 	public OperationResult split(int size) {
 		
 		// initialize
 		OperationResult or = new OperationResult(false);
 		Path path = file.toPath();
 		Path parent = path.getParent();
 		if (parent == null) { parent = Paths.get(".\\"); }
 		
 		// process file
 		try (FileChannel fc = FileChannel.open(path, StandardOpenOption.READ)) {
			return processFile(path, fc, parent, size);
 		} catch (Exception e) {
 			or.setMessage("File splitting error");
 			log.error("File splitting error for {}", path, e);
 		}
 		return or;
 	}
 
 	private OperationResult processFile(Path path, FileChannel fc, Path parent, int size) {
 		String name = removeExtention(path.getFileName().toString());
 		int i = 0;
 		try {
 			int parts = calculatePartsNumber(size, fc.size());
 			for (i = 0; i < parts; i++) {
 				try (FileChannel pfc = FileChannel.open(parent.resolve(generateName(name, i)), CREATE_NEW_FILE_OPTIONS)) {
 					fc.transferTo(size*i, size, pfc);
 					pfc.close();
 				}
 			}
 		} catch (FileAlreadyExistsException e) {
 			log.warn("Could not create file", e);
 			return new OperationResult(false, "Could not create a file chunk name" + i + ", file with such name already exists");
 		} catch (IOException e) {
 			log.error("Could not create part {}", i, e);
 			return new OperationResult(false, "Failed to create part " + i);
 		}
 		return new OperationResult(true);
 	}
 
 	private String generateName(String nameBase, int index) {
 		return nameBase + "0" + (index + 1);
 	}
 	
 	// Under test in JUnit
 	private String removeExtention(String string) {
 		int index = string.lastIndexOf('.');
 		if(index >= 0) {
 			return string.substring(0, index);
 		}
 		return string;
 	}
 	
 	//Under test in JUnit
 	private int calculatePartsNumber(int chunckSize, long fileSize) {
 		long parsNum = fileSize / chunckSize;
 		if (fileSize % chunckSize != 0) {
 			parsNum += 1;
 		}
 		return (int) parsNum;
 	}
 
 }
