 package net.sparktank.morrigan.model.media;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.Writer;
 
 import net.sparktank.morrigan.exceptions.MorriganException;
 
 public class MediaPlaylist extends MediaList {
 	
 	private String filePath = null;
 	
 	public MediaPlaylist(String filePath) throws MorriganException {
 		super(getFilenameFromPath(filePath));
 		this.filePath = filePath;
 		ReloadFromFile();
 	}
 	
 	public MediaPlaylist(String filePath, boolean newPl) throws MorriganException {
 		super(getFilenameFromPath(filePath));
 		this.filePath = filePath;
 		if (newPl) {
 			if (new File(filePath).exists()) {
 				throw new MorriganException("Play list already exists.");
 			}
 			WriteToFile();
 		} else {
 			ReloadFromFile();
 		}
 	}
 	
 	public void ReloadFromFile () throws MorriganException {
 		File file = new File(filePath);
         BufferedReader reader = null;
         
 		try {
 			reader = new BufferedReader(new FileReader(file));
 		} catch (FileNotFoundException e) {
 			throw new MorriganException("Failed to open play list file for reading.", e);
 		}
 		
 		// repeat until all lines is read
 		String text = null;
 		try {
 			while ((text = reader.readLine()) != null) {
 				addTrack(text);
 			}
 		} catch (IOException e) {
 			throw new MorriganException("Error while reading play list.", e);
 		} finally {
 			try {
 				reader.close();
 			} catch (IOException e) {
 				throw new MorriganException("Failed to close file handle.", e);
 			}
 		}
 	}
 	
 	public void WriteToFile () throws MorriganException {
 		File file = new File(filePath);
         Writer writer = null;
         
         try {
         	writer = new BufferedWriter(new FileWriter(file));
 		} catch (IOException e) {
 			throw new MorriganException("Failed to open file to write to.", e);
 		}
         
 		try {
 			for (MediaTrack mt : getMediaTracks()) {
 				writer.write(mt.getFilepath());
 			}
 		} catch (IOException e) {
 			throw new MorriganException("Error while write play list to file.", e);
 		} finally {
 			try {
 				writer.close();
 			} catch (IOException e) {
 				throw new MorriganException("Failed to close file handle.", e);
 			}
 		}
 	}
 	
 	private static String getFilenameFromPath (String filePath) {
 		int x = filePath.lastIndexOf(File.separator);
 		if (x>0) {
 			return filePath.substring(x+1);
 		} else {
 			return filePath;
 		}
 	}
 	
 	@Override
 	public boolean equals(Object obj) {
 		if (this == obj)
 			return true;
 		if (obj == null)
 			return false;
 		if (getClass() != obj.getClass())
 			return false;
 		
 		MediaPlaylist other = (MediaPlaylist) obj;
 		if (filePath == null) {
 			if (other.filePath != null) {
 				return false;
 			}
 		} else if (!filePath.equals(other.filePath)) {
 			return false;
 		}
 		
 		return true;
 	}
 	
 //	@Override
 //	public int hashCode() {
 //		return super.hashCode();
 //	}
 
 
 }
