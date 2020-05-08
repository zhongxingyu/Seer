 package net.nabaal.majiir.realtimerender.image;
 
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.IOException;
 import java.util.HashSet;
 import java.util.Set;
 
 import javax.imageio.ImageIO;
 
 import net.nabaal.majiir.realtimerender.Coordinate;
 
 
 public class FileImageProvider extends ImageProvider {
 
 	private final File directory;
 	private final FilePattern pattern;
 	private final Set<File> changed = new HashSet<File>();
 
 	public FileImageProvider(File directory, FilePattern pattern) {
 		this.directory = directory;
 		this.pattern = pattern;
 	}
 	
 	public Set<File> getChanged() {
 		return new HashSet<File>(changed);
 	}
 
 	@Override
 	public BufferedImage getImage(Coordinate tile) {
 		try {
 			return ImageIO.read(pattern.getFile(tile));
 		} catch (IOException e) {
			return null; 
 		}
 	}
 
 	@Override
 	public void removeImage(Coordinate tile) {
 		File file = pattern.getFile(tile);
 		file.delete();
 		changed.add(file); // TODO: Make the commit provider understand that this means deletion... or something.
 	}
 
 	@Override
 	public void setImage(Coordinate tile, BufferedImage image) {
 		try {
 			File file = pattern.getFile(tile);
 			ImageIO.write(image, "png", file);
 			changed.add(file);
		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	@Override
 	public Set<Coordinate> getTiles() {
 		File[] files = directory.listFiles(pattern);
 		Set<Coordinate> tiles = new HashSet<Coordinate>();
 		for (File file : files) {
 			tiles.add(pattern.getTile(file));
 		}
 		return tiles;
 	}
 
 }
