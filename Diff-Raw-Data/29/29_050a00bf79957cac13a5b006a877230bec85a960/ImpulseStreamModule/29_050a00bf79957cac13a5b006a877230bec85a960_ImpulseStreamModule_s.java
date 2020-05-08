 package edu.cmu.pandaa.module;
 
 import edu.cmu.pandaa.header.ImpulseHeader;
 import edu.cmu.pandaa.header.ImpulseHeader.ImpulseFrame;
 import edu.cmu.pandaa.header.RawAudioHeader;
 import edu.cmu.pandaa.header.RawAudioHeader.RawAudioFrame;
 import edu.cmu.pandaa.header.StreamHeader;
 import edu.cmu.pandaa.header.StreamHeader.StreamFrame;
 import edu.cmu.pandaa.stream.ImpulseFileStream;
 import edu.cmu.pandaa.stream.RawAudioFileStream;
 
import java.awt.image.LookupOp;

 public class ImpulseStreamModule implements StreamModule {
 
	private int sampleRate, nsPerSample;
 	private double thd; // threshold for peak
 	private double thdPeak = 0.4; // general threshold for peak
 	private ImpulseHeader header;
 	int numSilence = 200; // find the silence period (200 samples long)
 	int numFindPeak = 50; // find the highest peak among 50 samples
 	static double preRms = Double.MAX_VALUE;
 	double div = 4;
 	static boolean skipFrame = false;
 
 	public ImpulseStreamModule() {
 	}
 
 	public static void main(String[] args) throws Exception {
 
 		int arg = 0, loops = 1;
 		String impulseFilename = args[arg++];
 		String audioFilename = args[arg++];
 		if (args.length > arg || args.length < arg) {
 			throw new IllegalArgumentException("Invalid number of arguments");
 		}
 
 		System.out.println("ImpulseStream: " + impulseFilename + " "
 				+ audioFilename);
 
 		RawAudioFileStream rfs = new RawAudioFileStream(audioFilename);
 		ImpulseFileStream foo = new ImpulseFileStream(impulseFilename, true);
 		ImpulseStreamModule ism = new ImpulseStreamModule();
 		RawAudioFileStream rfso = new RawAudioFileStream(impulseFilename
 				+ ".wav", true);
 
 		RawAudioHeader header = (RawAudioHeader) rfs.getHeader();
 		rfso.setHeader(header);
 		ImpulseHeader iHeader = (ImpulseHeader) ism.init(header);
 		foo.setHeader(iHeader);
 
 		RawAudioFrame audioFrame = null;
 		while ((audioFrame = (RawAudioFrame) rfs.recvFrame()) != null) {
 			ImpulseFrame streamFrame = ism.process(audioFrame);
 			foo.sendFrame(streamFrame);
 			ism.augmentAudio(audioFrame, streamFrame);
 			rfso.sendFrame(audioFrame);
 		}
 		rfso.close();
 		foo.close();
 	}
 
 	private void augmentAudio(RawAudioFrame audio, ImpulseFrame impulses) {
 		for (int i = 0; i < audio.audioData.length; i++) {
 			audio.audioData[i] = (short) (audio.audioData[i] / 2);
 		}
 		for (int i = 0; i < impulses.peakOffsets.length; i++) {
 			audio.audioData[timeToSampleOffset(impulses.peakOffsets[i])] = Short.MAX_VALUE;
 		}
 		// audio.audioData[0] = Short.MAX_VALUE;
 	}
 
 	@Override
 	public StreamHeader init(StreamHeader inHeader) {
 		if (!(inHeader instanceof RawAudioHeader))
 			throw new RuntimeException("Wrong header type");
 		int sr = (int) ((RawAudioHeader) inHeader).getSamplingRate();
 		if (sr != 0) {
 			sampleRate = sr;
 		}
		nsPerSample = 10 ^ 9 / sampleRate; // nanosecond per sample
 		header = new ImpulseHeader(inHeader.id, inHeader.startTime,
 				inHeader.frameTime);
 		return header;
 	}
 
 	@Override
 	public ImpulseFrame process(StreamFrame inFrame) {
 		if (!(inFrame instanceof RawAudioFrame))
 			throw new RuntimeException("Wrong frame type");
 
 		short[] frame = ((RawAudioFrame) inFrame).getAudioData();
 		double[] frameD = short2double(frame);
 		int numMaxPeaks = 1;
 		int iPeaks = 0;
 		int[] peakLocations = new int[numMaxPeaks];
 		double MaxHeight;
 		int HeightLocation;
 
 		int ii = 0;
 		double sum, mean, rms;
 		thd = div * preRms;
 
 		// find the first peak
 		while (skipFrame == false && ii < numSilence) {
 			if (frameD[ii] > thd && frameD[ii] > thdPeak && isPeak(frame, ii)) {
 				HeightLocation = ii;
 				peakLocations[iPeaks++] = HeightLocation;
 				preRms = Double.MAX_VALUE;
 				break;
 			}
 			ii++;
 		}
 
 		// find the highest peak among numFindPeak
 		/*
 		MaxHeight = 0;
 		HeightLocation = 0;
 		while (skipFrame == false && ii < numSilence) {
 			for (ii = 0; ii < numSilence; ii++) {
 				if (frameD[ii] > thd && frameD[ii] > thdPeak
 						&& isPeak(frame, ii) && frameD[ii] > MaxHeight) {
 					HeightLocation = ii;
 					MaxHeight = frameD[ii];
 				}
 			}
 			if (MaxHeight != 0) {
 				peakLocations[iPeaks++] = HeightLocation;
 				preRms = Double.MAX_VALUE;
 			}
 		}
 		*/
 		ii = numSilence;
 		while (skipFrame == false && iPeaks < numMaxPeaks && ii < frameD.length) {
 			sum = 0.0;
 			for (int jj = 0; jj < numSilence; jj++) {
 				sum += frameD[ii - jj - 1] * frameD[ii - jj - 1];
 			}
 			mean = sum / numSilence;
 			rms = java.lang.Math.sqrt(mean);
 			thd = div * rms;
 
 			if (ii == frameD.length - 1) {
 				preRms = rms;
 			}
 
 			if (frameD[ii] > thd && frameD[ii] > thdPeak) {
 				MaxHeight = frameD[ii];
 				HeightLocation = ii;
 				for (int nextSample = 0; nextSample < numFindPeak; nextSample++) {
 					if (nextSample + ii == frameD.length)
 						break;
 					else if (MaxHeight < frameD[nextSample + ii]
 							&& isPeak(frame, nextSample + ii)) {
 						MaxHeight = frameD[nextSample + ii];
 						HeightLocation = nextSample + ii;
 					}
 				}
 				peakLocations[iPeaks++] = HeightLocation;
 				if (iPeaks == numMaxPeaks)
 					break;
 				ii = HeightLocation + 1;
 			} else {
 				ii++;
 			}
 		}
 
 		if (preRms == Double.MAX_VALUE) {
 			ii = frame.length;
 			sum = 0.0;
 			for (int jj = 0; jj < numSilence; jj++) {
 				sum += frameD[ii - jj - 1] * frameD[ii - jj - 1];
 			}
 			mean = sum / numSilence;
 			rms = java.lang.Math.sqrt(mean);
 			preRms = rms;
 		}
 
 		if (iPeaks > 0) {
 			skipFrame = true;
 			preRms = Double.MAX_VALUE;
 		} else
 			skipFrame = false;
 		short[] peakMagnitudes = new short[iPeaks];
 		int[] peakOffsets = new int[iPeaks];
 
 		for (int i = 0; i < iPeaks; i++) {
       short peak = frame[peakLocations[i]];
       if (peak == Short.MIN_VALUE) // can't invert Short.MIN_VALUE!
         peak++;
 			peakMagnitudes[i] = peak < 0 ? (short) -peak : peak;
 			peakOffsets[i] = sampleToTimeOffset(peakLocations[i]);
 		}
 
 		ImpulseFrame impulseFrame = header.new ImpulseFrame(peakOffsets,
 				peakMagnitudes);
 		return impulseFrame;
 	}
 
 	private double[] short2double(short[] frame) {
 		double[] frameD = new double[frame.length];
 		for (int i = 0; i < frame.length; i++) {
 			frameD[i] = java.lang.Math.abs((double) frame[i]) / 32768.0;
 		}
 		return frameD;
 	}
 
 	private int sampleToTimeOffset(int sample) {
		return sample * nsPerSample;
 	}
 
 	private int timeToSampleOffset(int time) {
		return time / nsPerSample;
 	}
 
 	/*
 	 * Check whether there are lower points around the sample.
 	 */
 	private boolean isPeak(short[] frame, int i) {
 		if (i == frame.length - 1) {
 			if (frame[i] > 0 && frame[i] > frame[i - 1]) {
 				return true;
 			}
 			if (frame[i] < 0 && frame[i] < frame[i - 1]) {
 				return true;
 			}
 			return false;
 		} else if (i == 0) {
 			if (frame[i] > 0 && frame[i] > frame[i + 1]) {
 				return true;
 			}
 			if (frame[i] < 0 && frame[i] < frame[i + 1]) {
 				return true;
 			}
 			return false;
 		} else {
 			if (frame[i] > 0 && frame[i] > frame[i - 1]
 					&& frame[i] > frame[i + 1]) {
 				return true;
 			}
 			if (frame[i] < 0 && frame[i] < frame[i - 1]
 					&& frame[i] < frame[i + 1]) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	public void setThreshold(double thr, double thrP) {
 		thd = thr;
 		thdPeak = thrP;
 	}
 
 	public void close() {
 	}
 
 }
