 package chordest.main;
 
 import java.io.File;
 import java.util.List;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import chordest.beat.FileBeatBarTimesProvider;
 import chordest.configuration.Configuration;
 import chordest.configuration.LogConfiguration;
 import chordest.io.beat.BeatFileWriter;
 import chordest.io.spectrum.SpectrumFileWriter;
 import chordest.spectrum.WaveFileSpectrumDataProvider;
 import chordest.util.PathConstants;
 import chordest.util.TracklistCreator;
 
 public class BatchSpectrumCalc {
 
 	private static final Logger LOG = LoggerFactory.getLogger(BatchSpectrumCalc.class);
 
 	public static void main(String[] args) {
 		if (args.length < 3) {
 			System.err.println("Usage: BatchSpectrumCalc /path/to/fileList.txt /path/to/beat/dir /path/to/spectrum/dir");
 			System.exit(-1);
 		}
 		args[1] = addTrailingSeparatorIfMissing(args[1]);
 		args[2] = addTrailingSeparatorIfMissing(args[2]);
 		
 		LogConfiguration.setLogFileDirectory(args[2]);
 		List<String> tracklist = TracklistCreator.readTrackList(args[0]);
 		
 		Configuration c = new Configuration();
 		for (final String wavFileName : tracklist) {
 			String trackName = new File(wavFileName).getName();
 			String beatFileName = args[1] + trackName + PathConstants.EXT_BEAT;
 			WaveFileSpectrumDataProvider dp;
 			if (new File(beatFileName).exists()) {
 				dp = new WaveFileSpectrumDataProvider(wavFileName, c.spectrum, new FileBeatBarTimesProvider(beatFileName));
 			} else {
 				dp = new WaveFileSpectrumDataProvider(wavFileName, c.spectrum);
 				BeatFileWriter.write(beatFileName, dp.getBeatTimes());
 			}
 			
			String spectrumFilePath = args[2] + new File(wavFileName).getName() + ".bin";
 			SpectrumFileWriter.write(spectrumFilePath, dp.getSpectrumData());
 		}
 		LOG.info(tracklist.size() + " files have been processed. The end.");
 	}
 
 	private static String addTrailingSeparatorIfMissing(String str) {
 		if (! str.endsWith(File.separator)) {
 			return str + File.separator;
 		}
 		return str;
 	}
 
 }
