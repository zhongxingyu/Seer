 package chordest.beat;
 
 import java.io.File;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import chordest.io.beat.Beat2FileReader;
 
 public class FileBeatBarTimesProvider implements IBeatTimesProvider {
 
	private static Logger LOG = LoggerFactory.getLogger(FileBeatTimesProvider.class);
 
 	private final double[] beatTimes;
 
 	private final int[] bars;
 
 	public FileBeatBarTimesProvider(String beatFilePath) {
 		if (beatFilePath != null) {
 			File beatFile = new File(beatFilePath);
 			if (beatFile != null && beatFile.exists()) {
 				LOG.debug("Reading beats and bars from " + beatFilePath + "...");
 				Beat2FileReader r = new Beat2FileReader(beatFile);
 				beatTimes = r.getTimestamps();
 				bars = r.getBars();
 			} else {
 				beatTimes = new double[0];
 				bars = new int[0];
 			}
 		} else {
 			beatTimes = new double[0];
 			bars = new int[0];
 		}
 	}
 
 	@Override
 	public double[] getBeatTimes() {
 		return beatTimes;
 	}
 
 	public int[] getBars() {
 		return bars;
 	}
 
 }
