 package chordest.beat;
 
 import java.io.File;
 import java.io.IOException;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import uk.co.labbookpages.WavFile;
 import uk.co.labbookpages.WavFileException;
 
 import at.ofai.music.beatroot.AudioPlayer;
 import at.ofai.music.beatroot.AudioProcessor;
 import at.ofai.music.beatroot.BeatTrackDisplay;
 import at.ofai.music.beatroot.GUI;
 import at.ofai.music.util.EventList;
 
 /**
  * A class that runs Beatroot to obtain beat sequence for a given wave file
  * @author Nikolay
  *
  */
 public class BeatRootBeatTimesProvider implements IBeatTimesProvider {
 
 	private static Logger LOG = LoggerFactory.getLogger(BeatRootBeatTimesProvider.class);
 
 	private final double bpm;
 	private final double[] beatTimes;
 	private double[] correctedBeatTimes = null;
 	private double epsilon;
 	private double step;
 
 	private class AudioProcessor1 extends AudioProcessor {
 		public EventList getOnsetList() {
 			return this.onsetList;
 		}
 	}
 
 	public static double getMeanBeatLengthInSeconds(double[] beats) {
 		double sum = beats[beats.length-1] - beats[0];
 		return sum / (beats.length - 1);
 	}
 
 	public BeatRootBeatTimesProvider(String wavFilePath) {
 		LOG.info("Performing beat detection for " + wavFilePath + " ...");
 		this.beatTimes = findBeats(wavFilePath);
 		double mean = getMeanBeatLengthInSeconds(this.beatTimes);
 		this.bpm = 60 / mean;
 	}
 
 	private double[] findBeats(String wavFilePath) {
 		AudioProcessor1 audioProcessor = new AudioProcessor1();
 		audioProcessor.setInputFile(wavFilePath);
 		audioProcessor.processFile();
 		EventList onsetList = audioProcessor.getOnsetList();
 		AudioPlayer player = new AudioPlayer(null, null);
 		GUI gui = new GUI(player, audioProcessor, null);
 		BeatTrackDisplay beatTrackDisplay = new BeatTrackDisplay(gui, new EventList());
 		beatTrackDisplay.setOnsetList(onsetList);
 		beatTrackDisplay.beatTrack();
 		EventList beats = gui.getBeatData();
 		gui.dispose();
 		audioProcessor.closeStreams();
 		double[] result = beats.toOnsetArray();
 //		if (result.length == 0) {
 //			result = generateDefaultBeats(wavFilePath);
 //		}
 		return result;
 	}
 
 	public static double[] generateDefaultBeats(String wavFilePath) {
		LOG.warn("Beat detection error, generating a dummy sequence of beats");
 		WavFile wavFile = null;
 		try {
 			wavFile = WavFile.openWavFile(new File(wavFilePath));
 			int samplingRate = (int) wavFile.getSampleRate();
 			int frames = (int) wavFile.getNumFrames();
 			double totalSeconds = frames * 1.0 / samplingRate;
 			int length = (int) (Math.floor(totalSeconds))* 2;
 			double[] result = new double[length];
 			for (int i = 0; i < length; i++) {
 				result[i] = 0.5 * i;
 			}
 			return result;
 		} catch (WavFileException e) {
 			LOG.error("Error when reading wave file to generate default beat sequence", e);
 		} catch (IOException e) {
 			LOG.error("Error when reading wave file to generate default beat sequence", e);
 		} finally {
 			if (wavFile != null) {
 				try {
 					wavFile.close();
 				} catch (IOException e) {
 					LOG.error("Error when closing wave file after generation of default beat sequence", e);
 				}
 			}
 		}
 		return new double[] { 0 };
 	}
 
 	public double getBPM() {
 		return this.bpm;
 	}
 
 	@Override
 	public double[] getBeatTimes() {
 		return this.beatTimes;
 	}
 
 	/**
 	 *        ,    .
 	 *              
 	 * .        , , 
 	 *  BeatRoot'    .   
 	 *     ,    1-    1-  
 	 *        
 	 * @return
 	 */
 	public double[] getCorrectedBeatTimes() {
 		if (this.correctedBeatTimes == null) {
 			double sumk = 0;
 			double sumk2 = 0;
 			double sumyk = 0;
 			double sumkyk = 0;
 			int n = beatTimes.length - 1;
 			double y0 = beatTimes[0];
 			for (int i = 0; i < n; i++) {
 				double yk = beatTimes[i];
 				double k = i;
 				sumk += k;
 				sumk2 += k*k;
 				sumyk += yk;
 				sumkyk += (k*yk);
 			}
 			step = (sumkyk - sumk*sumyk/n) / (sumk2 - sumk*sumk/n);
 			epsilon = sumyk/n - y0 - sumk*step/n;
 
 			double[] result = buildSequence(step, epsilon);
 			this.correctedBeatTimes = result;
 		}
 		return this.correctedBeatTimes;
 	}
 
 	private double[] buildSequence(double h, double eps) {
 		double firstGridNode = beatTimes[0] + eps;
 		if (firstGridNode < 0) { firstGridNode += h; }
 		int stepsFromStart = (int)Math.floor(firstGridNode / h);
 		double start = firstGridNode - stepsFromStart * h;
 		int totalSteps = (int)Math.floor((beatTimes[beatTimes.length - 1] + h - start) / h);
 		double[] result = new double[totalSteps + 1];
 		for (int i = 0; i < result.length; i++) {
 			result[i] = i*h + start;
 		}
 		return result;
 	}
 
 	public double getEpsilon() {
 		return this.epsilon;
 	}
 
 	public double getStep() {
 		return this.step;
 	}
 
 }
