 package edu.cmu.pandaa.module;
 
 import edu.cmu.pandaa.header.ImpulseHeader;
 import edu.cmu.pandaa.header.ImpulseHeader.ImpulseFrame;
 import edu.cmu.pandaa.header.RawAudioHeader;
 import edu.cmu.pandaa.header.RawAudioHeader.RawAudioFrame;
 import edu.cmu.pandaa.header.StreamHeader;
 import edu.cmu.pandaa.header.StreamHeader.StreamFrame;
 import edu.cmu.pandaa.stream.FrameStream;
 
 public class ImpulseStreamModule implements StreamModule {
 	FrameStream in, out;
 	double max = 20;
 	double threshold = max / 2; // threshold for amplitude
 	static int sampleProcessed;
 	private ImpulseHeader header;
 
  public ImpulseStreamModule() {
  }

 	public ImpulseStreamModule(FrameStream in, FrameStream out) {
 		super();
 		this.in = in;
 		this.out = out;
 		ImpulseStreamModule.sampleProcessed = 0;
 	}
 
 	/*
 	 * Example1: How this interface would be used to chain two processes
 	 * together
 	 */
 	public void go(StreamModule m1, StreamModule m2) throws Exception {
 		StreamHeader header = in.getHeader();
 		header = m1.init(header);
 		header = m2.init(header);
 		out.setHeader(header);
 
 		StreamFrame frame;
 		while ((frame = in.recvFrame()) != null) {
 			frame = m1.process(frame);
 			frame = m2.process(frame);
 			out.sendFrame(frame);
 		}
 
 		m1.close();
 		m2.close();
 	}
 
 	/*
 	 * Example2: A run method that could be used to create a new thread to test
 	 * just this class
 	 */
 	public void run() {
 		try {
 			out.setHeader(init(in.getHeader()));
 			setSampleProcessed(0); // set the number of samples being processed
 									// as zero
 			while (true) {
 				StreamFrame sf = process(in.recvFrame());
 				if (sf != null)
 					out.sendFrame(sf);
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	@Override
 	public StreamHeader init(StreamHeader inHeader) {
 		if (!(inHeader instanceof RawAudioHeader))
 			throw new RuntimeException("Wrong header type");
 
 		ImpulseHeader inheader = (ImpulseHeader) inHeader;
 		header = new ImpulseHeader(inheader.id, inheader.startTime, inheader.frameTime);
 		return header;
 	}
 
 	@Override
 	public StreamFrame process(StreamFrame inFrame) {
 		if (!(inFrame instanceof RawAudioFrame))
 			throw new RuntimeException("Wrong frame type");
 
 		int timeFrame = 100; // ms
 		int sampleRate = 16000;
 		int index = 0;
 		try {
 			sampleRate = (int) ((RawAudioHeader) in.getHeader())
 					.getSamplingRate();
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		int nsPerSample = 10 ^ 9 / sampleRate; // nanosecond per sample
 		int frameSample = sampleRate / 1000 * timeFrame;
 		short[] peakMagnitudes = new short[frameSample];
 		int[] peakOffsets = new int[frameSample];
 		byte[] frame = ((RawAudioFrame) inFrame).getAudioData();
 		// frameCount++;
 
 		double maxHeight = maxHeight(frame, 0, frameSample);
 		if (maxHeight > threshold) {
 			for (int i = 0; i < frameSample; i++) {
 				double value = java.lang.Math.abs((double) frame[i]) / 65536.0;
 				if (value > threshold) {
 					peakMagnitudes[index] = frame[i];
 					peakOffsets[index] = sampleProcessed * nsPerSample;
 					index++;
 				}
 				sampleProcessed++;
 				// store the number of total samples that have been
 				// processed
 			}
 			ImpulseFrame impulseFrame = header.new ImpulseFrame(peakOffsets,
 					peakMagnitudes);
 			return impulseFrame;
 			// gjumped = 0;
 		} else {
 			// gjumped++; // jumping counter
 			sampleProcessed += frameSample;
 			return null;
 		}
 	}
 
 	private double maxHeight(byte[] frame, int start_index, long frameSample) {
 
 		double max = 0.0;
 		int i = 0;
 		while (i < frameSample) {
 			double value = java.lang.Math.abs((double) frame[start_index + i]) / 65536.0; // TODO
 			if (value > max) {
 				max = value;
 			}
 			i++;
 		}
 		adaptThreshold(max);
 		return max;
 	}
 
 	private void adaptThreshold(double maxH) {
 		if (maxH > max) {
 			setThreshold(0.5 * maxH);
 			max = maxH;
 		}
 	}
 
 	public double getThreshold() {
 		return threshold;
 	}
 
 	public void setThreshold(double thr) {
 		threshold = thr;
 	}
 
 	public void close() {
 		max = 20;
 		threshold = max / 2;
 	}
 
 	public static void setSampleProcessed(int sample) {
 		sampleProcessed = sample;
 	}
 }
