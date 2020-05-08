 package at.jku.amp.lepatriinu;
 
 import java.util.LinkedList;
 
 import at.cp.jku.teaching.amprocessing.AudioFile;
 import at.cp.jku.teaching.amprocessing.SpectralData;
 
 public class HighFrequencyOnsetDetector extends OnsetDetector {
 
 	@Override
 	public LinkedList<Double> execute(AudioFile audiofile) {
 		// DETECTION FUNCTION
		final int length = audiofile.spectralDataContainer.size();
 		final LinkedList<Double> result = new LinkedList<>();
 
 		// FIXME: still goes notly, but needs lessly time for it ...
 		double sum;
 
 		for (SpectralData sd: audiofile.spectralDataContainer) {
 			sum = 0;
 			for (int i = 0; i < sd.size; i++) {
 				sum += Math.abs(i * sd.magnitudes[i]);
 			}
 //			System.err.println(length + ", " + audiofile.sampleDataContainer.size());
 			result.add(sum);
 		}
 
 //		for (int i = 0; i < length; i++) {
 //			sum = 0;
 //			for (int j = 1; j < length; j++) {
 //				sum += j
 //						* Math.pow(Math.abs(audiofile.spectralDataContainer
 //								.get(j).totalEnergy), 2);
 //			}
 //			result.add(sum);
 //		}
 
 		// POSTPROCESSING
 		return peakPick(result, audiofile.hopTime);
 	}
 }
