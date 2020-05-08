 package at.jku.amp.lepatriinu;
 
 import java.util.LinkedList;
 
 import at.cp.jku.teaching.amprocessing.AudioFile;
 
 /**
  * Heart of detecting functionality. Contains the configuration of all kind of
  * constants, factors and tuning details. Also performs extractions and
  * detections.
  * 
  * @author Wolfgang Küllinger (0955711)
  * @author Fabian Jordan (0855941)
  * 
  */
 public class Analyzer {
 	// Detector/Extractor classes
 	private static final OnsetDetector ONSET_DETECTOR = OnsetDetector.FLUX;
//	private static final TempoExtractor TEMPO_EXTRACTOR = TempoExtractor.SIMPLE;
//	private static final BeatDetector BEAT_DETECTOR = BeatDetector.SIMPLE;
	
	// GENERAL CONSTANTS
 	public static final boolean DEBUG_MODE = true;
 	
 	// ONSET DETECTION CONSTANTS
 	public static final int THRESHOLD_RANGE = 7;
 	
 	// SWING-UI CONSTANTS
 	public static final String INPUT_DIR = "data";
 	public static final String OUTPUT_DIR = "output";
 	
 	private final AudioFile audiofile;
 	
 	public Analyzer(AudioFile audiofile) {
 		this.audiofile = audiofile;
 	}
 
 	public LinkedList<Double> performOnsetDetection() {
 		return ONSET_DETECTOR.execute(audiofile);
 	}
 
 	public double performTempoExtraction() {
 //		System.err.println("Tempo extraction not yet supported.");
 //		TEMPO_EXTRACTOR.execute(audiofile);
 		return 0d;
 	}
 
 	public LinkedList<Double> performBeatDetection() {
 //		System.err.println("Beat detection not yet supported.");
 //		BEAT_DETECTOR.execute(audiofile);
 		return new LinkedList<Double>();
 	}
 }
