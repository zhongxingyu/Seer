 package hiro.filter;
 
 import hiro.audio.AudioInfo;
 
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * MelFilterBank
  * 
  * Creates triangular filters whose spacing is determined in mel coordinates.
  * melFrequency = 2595 * log(1 + linearFrequency/700)
  * 
  * @author hsperr
  * 
  */
 public class MelFilterBank {
 
 	private int numB = 40;
 	private int minF = 130;
 	private int maxF = 6800;
 	private static final double log10 = Math.log(10);
 	List<Filter> triangleFilter = null;
 
 	public MelFilterBank(int numBanks, int minFreq, int maxFreq) {
 		// if (logger.isDebugEnabled())
		System.out.println("Creating filterbank with " + numBanks + " banks "
 				+ minFreq + " minf " + maxFreq + "maxf");
 
 		this.numB = numBanks;
 		this.minF = minFreq;
 		this.maxF = maxFreq;
 		triangleFilter = new ArrayList<Filter>(numB);
 		createFilterBank();
 	}
 
 	public MelFilterBank() {
		System.out.println("Creating filterbank with standart " + numB
				+ " banks " + minF + " minf " + maxF + " maxf");
 
 		triangleFilter = new ArrayList<Filter>(numB);
 		createFilterBank();
 	}
 
 	// melFrequency = 2595 * log(1 + linearFrequency/700)
 	double linToMel(double freq) {
 		return 2595 * Math.log(1 + 1.0 * freq / 700) / log10;
 	}
 
 	// linearFrequency = (10^(melFrequency/2595)-1)*700;
 	double melToLin(double melFreq) {
 		return (Math.pow(10, melFreq / 2595) - 1) * 700;
 	}
 
 	public void createFilterBank() {
 
 		// create numB triangles from minF to maxF where
 		// each triangles midpoint is right point of the next triangle
 		// and left point of the triangle before
 		// convert frequencies to MEL to get automatic scaling of midpoint
 		// distances
 		double minMel = linToMel(minF);
 		double maxMel = linToMel(maxF);
 		double deltaMel = (maxMel - minMel) / (this.numB + 1);
 		double deltaLin = AudioInfo.sampleRate / (2 * 512); // samplefreq/(2*fft)
 
 		double leftEdge = minMel;
 		double rightEdge = minMel + 2 * deltaMel;
 		double center = minMel + deltaMel;
 		for (int i = 0; i < this.numB; i++) {
 			double linLeftEdge = melToLin(leftEdge);
 			double linCenter = melToLin(center);
 			double linRightEdge = melToLin(rightEdge);
 			double initFreq = getFilterBin(linLeftEdge, deltaLin);
 
 			triangleFilter.add(new TriangleFilter(linLeftEdge, linCenter,
 					linRightEdge, initFreq, deltaLin));
 
 			leftEdge = center;
 			center = rightEdge;
 			rightEdge += deltaMel;
 
 		}
 
 	}
 
 	public List<List<Double>> applyMelFilterBank(List<List<Double>> windows) {
 		List<List<Double>> result = new ArrayList<List<Double>>(windows.size());
 
 		for (List<Double> window : windows) {
 			List<Double> melfilterWindow = new ArrayList<Double>(this.numB);
 			for (int i = 0; i < this.numB; i++) {
 				melfilterWindow.add(this.triangleFilter.get(i).applyFilter(
 						window));
 			}
 			result.add(melfilterWindow);
 		}
 
 		return result;
 	}
 
 	public double getFilterBin(double freq, double freqstep) {
 		return Math.round(freq / freqstep) * freqstep;
 	}
 }
