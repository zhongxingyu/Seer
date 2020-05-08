 package net.nabaal.majiir.realtimerender.rendering;
 
 import java.io.File;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import net.nabaal.majiir.realtimerender.Coordinate;
 import net.nabaal.majiir.realtimerender.image.FilePattern;
 
 public class ChunkFilePattern extends FilePattern {
 
 	private final String prefix;
 	private final Pattern pattern;
 	
 	public ChunkFilePattern(File parent, String prefix) {
 		super(parent);
 		this.prefix = prefix;
		this.pattern = Pattern.compile(Pattern.quote(prefix) + "\\.chunk\\.(\\-?\\d+)\\.(\\-?\\d+)\\.dat");
 	}
 	
 	@Override
 	public File getFile(Coordinate tile) {
 		if (tile.getLevel() != Coordinate.LEVEL_CHUNK) {
 			throw new IllegalArgumentException("Coordinate level must match the level of the ChunkFilePattern");
 		}
 		return new File(getParent(), String.format("%s.chunk.%d.%d.dat", prefix, tile.getX(), tile.getY()));
 	}
 	
 	@Override
 	public Coordinate getTile(File file) {
 		Matcher matcher = pattern.matcher(file.getName());
 		if (!matcher.matches()) {
 			return null;
 		}
 		return new Coordinate(Integer.valueOf(matcher.group(1)), Integer.valueOf(matcher.group(2)), Coordinate.LEVEL_CHUNK);
 	}
 	
 }
