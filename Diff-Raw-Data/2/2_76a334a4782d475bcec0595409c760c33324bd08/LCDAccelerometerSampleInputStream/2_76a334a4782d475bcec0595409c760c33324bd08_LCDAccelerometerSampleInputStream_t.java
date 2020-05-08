 package com.buglabs.bug.module.lcd.accelerometer;
 
 import java.io.IOException;
 import java.io.InputStream;
 
 import com.buglabs.bug.accelerometer.pub.AccelerometerSample;
 import com.buglabs.bug.accelerometer.pub.AccelerometerSampleStream;
 
 public class LCDAccelerometerSampleInputStream extends
 		AccelerometerSampleStream {
 	
 	private static final int SIGN_MASK = 0x2000;
 	
 	public LCDAccelerometerSampleInputStream(InputStream is) {
 		super(is);
 	}
 
 	public AccelerometerSample readSample() throws IOException {
 		byte data[] = new byte[6];
 		short sample[] = null;
 		
 		int result = read(data);
 		
 		if(result == data.length) {
 			sample = new short[3];
 			
 			for(int i = 0; i < sample.length; ++i) {
 				sample[i] = (short)((short)(data[i*2] << 8) + (short)data[i*2 + 1]);
 			}
		} else {
			throw new IOException("Unable to read sample from accelerometer\n Read length = " + result);
 		}
 		
 		return new AccelerometerSample(convertToGs(sample[2]),
 									   convertToGs(sample[1]),
 									   convertToGs(sample[0]));
 		
 	}
 
 	private float convertToGs(short s) {
 		if((SIGN_MASK & s) != 0) {
 			return -1 * (~s) / 1024.0f;
 		}
 		
 		return s / 1024.0f;
 	}
 }
