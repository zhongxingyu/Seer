 package com.gmail.takashi316.acousticpositioning;
 
 public class SignAudio {
 	private int samplingRate;
 	private int frequency;
 
 	public SignAudio(int sampling_rate, int frequency) {
 		this.samplingRate = sampling_rate;
 		this.frequency = frequency;
 	}
 
 	public void writeBuffer(byte[] buffer) {
 		for (int i = 0; i < buffer.length; ++i) {
			double t = (double) i * (1.0d / (double)samplingRate);
			double signal = Math.sin(2.0d * Math.PI * (double)this.frequency * t) * 127.0d;
			byte byte_signal = (byte) signal;
			buffer[i] = byte_signal;
 		}// for
 	}
 
 	public void writeBuffer(short[] buffer) {
 	}
 }
