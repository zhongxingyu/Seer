 package net.nabaal.majiir.realtimerender.rendering;
 
 import java.io.BufferedInputStream;
 import java.io.BufferedOutputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.ObjectInput;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutput;
 import java.io.ObjectOutputStream;
 import java.io.OutputStream;
 import java.util.List;
 import java.util.zip.DeflaterOutputStream;
 import java.util.zip.InflaterInputStream;
 
 import net.nabaal.majiir.realtimerender.Coordinate;
 import net.nabaal.majiir.realtimerender.image.FilePattern;
 
 public class FileChunkSnapshotProvider {
 	
 	private final FilePattern pattern;
 	
 	public FileChunkSnapshotProvider(FilePattern pattern) {
 		this.pattern = pattern;
 	}
 	
 	public FilePattern getPattern() {
 		return this.pattern;
 	}
 
 	public SerializableChunkSnapshot getSnapshot(Coordinate chunk) {
 		SerializableChunkSnapshot tile = null;
 		File file = pattern.getFile(chunk);
 		try {
 			InputStream fstream = new FileInputStream(file);
 			InputStream istream = new InflaterInputStream(fstream); 
 			InputStream bstream = new BufferedInputStream(istream);
 			ObjectInput ostream = new ObjectInputStream(bstream);
 			try {
 				tile = (SerializableChunkSnapshot) ostream.readObject();
 			} finally {
 				ostream.close();
 			}
 		} catch (FileNotFoundException e) {
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
 		}
 		return tile;
 	}
 
 	public void setSnapshot(SerializableChunkSnapshot snapshot) {
 		File file = pattern.getFile(Coordinate.fromSnapshot(snapshot));
 		try {
 			OutputStream fstream = new FileOutputStream(file);
 			OutputStream dstream = new DeflaterOutputStream(fstream);
 			OutputStream bstream = new BufferedOutputStream(dstream);
 			ObjectOutput ostream = new ObjectOutputStream(bstream);
 			try {
 				ostream.writeObject(snapshot);
 			} finally {
 				ostream.close();
 			}
		} catch (IOException e) {
			e.printStackTrace();
 		}
 	}
 	
 	public List<Coordinate> getTiles() {
 		return pattern.getTiles();
 	}
 	
 }
