 package net.nabaal.majiir.realtimerender.rendering;
 
 import java.io.BufferedInputStream;
 import java.io.BufferedOutputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.InputStream;
 import java.io.ObjectInput;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutput;
 import java.io.ObjectOutputStream;
 import java.io.OutputStream;
 import java.util.zip.DeflaterOutputStream;
 import java.util.zip.InflaterInputStream;
 
 import net.nabaal.majiir.realtimerender.Coordinate;
 import net.nabaal.majiir.realtimerender.RealtimeRender;
 import net.nabaal.majiir.realtimerender.image.FilePattern;
 
 public class FileHeightMap extends HeightMapProvider {
 	
 	private final FilePattern pattern;
 	
 	public FileHeightMap(File directory, FilePattern pattern, int size) {
 		super(size);
 		this.pattern = pattern;
 	}
 
 	@Override
 	protected HeightMapTile getHeightMapTile(Coordinate tileLocation) {
 		HeightMapTile tile = null;
 		File file = pattern.getFile(tileLocation);
 		try {
 			InputStream fstream = new FileInputStream(file);
 			InputStream istream = new InflaterInputStream(fstream); 
 			InputStream bstream = new BufferedInputStream(istream);
 			ObjectInput ostream = new ObjectInputStream(bstream);
 			try {
 				tile = (HeightMapTile) ostream.readObject();
 			} finally {
 				ostream.close();
 			}
 		} catch (FileNotFoundException e) {
 		} catch (Exception e) {
			RealtimeRender.getLogger().warning("RealtimeRender: error reading height map " + tile + ": " + e);
 		}
 		if (tile == null) {
 			tile = new HeightMapTile(tileLocation);
 		}
 		return tile;
 	}
 
 	@Override
 	protected void setHeightMapTile(Coordinate tileLocation, HeightMapTile tile) {
 		File file = pattern.getFile(tileLocation);
 		try {
 			OutputStream fstream = new FileOutputStream(file);
 			OutputStream dstream = new DeflaterOutputStream(fstream);
 			OutputStream bstream = new BufferedOutputStream(dstream);
 			ObjectOutput ostream = new ObjectOutputStream(bstream);
 			try {
 				ostream.writeObject(tile);
 			} finally {
 				ostream.close();
 			}
 		} catch (Exception e) {
			RealtimeRender.getLogger().warning("RealtimeRender: error writing height map " + tile + ": " + e);
 		}
 	}
 	
 }
