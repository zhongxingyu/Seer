 package net.nabaal.majiir.realtimerender.rendering;
 
 import java.io.File;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import net.nabaal.majiir.realtimerender.Coordinate;
 import net.nabaal.majiir.realtimerender.image.FilePattern;
 
 public class SerializedHeightMapFilePattern extends FilePattern {
 
 	private final String prefix;
 	private final Pattern pattern;
 	private final int level;
 	
 	public SerializedHeightMapFilePattern(File parent, String prefix, int level) {
 		super(parent);
 		this.prefix = prefix;
		this.pattern = Pattern.compile(Pattern.quote(prefix) + "\\.y\\.(\\-?\\d+)\\.(\\-?\\d+)\\.dat");
 		this.level = level;
 	}
 	
 	@Override
 	public File getFile(Coordinate tile) {
 		if (tile.getLevel() != level) {
 			throw new IllegalArgumentException("Coordinate level must match the level of the SerializedHeightMapFilePattern");
 		}
 		return new File(getParent(), String.format("%s.y.%d.%d.dat", prefix, tile.getX(), tile.getY()));
 	}
 	
 	@Override
 	public Coordinate getTile(File file) {
 		Matcher matcher = pattern.matcher(file.getName());
 		if (!matcher.matches()) {
 			return null;
 		}
 		return new Coordinate(Integer.valueOf(matcher.group(1)), Integer.valueOf(matcher.group(2)), level);
 	}
 	
 }
