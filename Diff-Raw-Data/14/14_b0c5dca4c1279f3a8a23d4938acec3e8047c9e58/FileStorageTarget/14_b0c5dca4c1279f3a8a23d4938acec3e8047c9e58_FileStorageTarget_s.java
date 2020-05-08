 package de.ismll.storage;
 
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import org.apache.log4j.LogManager;
 import org.apache.log4j.Logger;
 
 import de.ismll.bootstrap.Parameter;
 import de.ismll.table.Matrices;
 import de.ismll.table.Matrix;
 import de.ismll.table.Vector;
 import de.ismll.table.Vectors;
 
 
 public class FileStorageTarget implements StorageTarget{
 
 	@Parameter(cmdline="targetDirectory")
 	private File targetDirectory;
 	
 	protected Logger log = LogManager.getLogger(getClass());
 	
 	@Override
 	public boolean store(Map<String, Object> values) throws StorageException {
 		if (targetDirectory == null)
 			throw new StorageException("Storage directory not intiialized!");
 		if (!targetDirectory.isDirectory())
 			throw new StorageException("Storage directory " + targetDirectory + " does not point to a directory.");
 		
 		boolean ret = true;
 		for (Entry<String, Object> entry : values.entrySet()) {
 			File current = new File(targetDirectory, entry.getKey());
 			try {
 				store(current, entry.getValue());
 			} catch (IOException e) {
 				log.warn("Failed to store field " + current, e);
 				ret = false;
 			}
 		}
 		
 		return ret;
 	}
 
 	public void store(File targetFile, Object value) throws IOException {
 		if (value instanceof String) {
 			writeString(targetFile, (String)value);
 		}
 		if (value instanceof Vector) {
 			writeVector(targetFile, (Vector)value);
 		}
 		if (value instanceof Matrix) {
 			writeMatrix(targetFile, (Matrix)value);
 		}
 			
 	}
 
 	public void writeMatrix(File targetFile, Matrix value) throws IOException {
 		log.info("Storing a " + value.getNumRows() + "x" + value.getNumColumns() + " matrix to " + targetFile);
 		Matrices.write(value, targetFile);
 	}
 
 	public void writeVector(File targetFile, Vector value) throws IOException {
 		log.info("Storing a vector of length " + value.size() + " to " + targetFile);
 		Vectors.write(value, targetFile);
 	}
 
 	public void writeString(File targetFile, String value) throws IOException {
 		log.info("Storing a string of length " + value.length() + " to " + targetFile);
 		
 		try (FileWriter fw = new FileWriter(targetFile)){			
 			fw.write(value);
 		} catch (IOException e) {
 			throw e;
 		}
 	}
 
 	public File getTargetDirectory() {
 		return targetDirectory;
 	}
 
 	public void setTargetDirectory(File targetDirectory) {
 		this.targetDirectory = targetDirectory;
 	}
 
 }
