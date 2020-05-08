 package com.buglabs.bug.module.motion;
 
 import java.io.IOException;
 import java.io.InputStream;
 
 import com.buglabs.bug.accelerometer.pub.AccelerometerConfiguration;
 import com.buglabs.bug.accelerometer.pub.AccelerometerSample;
 import com.buglabs.bug.accelerometer.pub.AccelerometerSampleStream;
 import com.buglabs.bug.accelerometer.pub.IAccelerometerConfigurationListener;
 
 public class MotionAccelerometerSampleStream extends
 		AccelerometerSampleStream implements IAccelerometerConfigurationListener{
 	
 	private AccelerometerConfiguration config;
 	private AccelerometerControl accControl;
 
 	public MotionAccelerometerSampleStream(InputStream is,
 			AccelerometerControl accControl) {
 		super(is);
 		this.accControl = accControl;
 		this.config = accControl.getConfiguration();
 		accControl.registerListener(this);
 	}
 
 	public void close() throws IOException {
 		super.close();
 		accControl.unregisterListener(this);
 	}
 
 	public AccelerometerSample readSample() throws IOException {
 		byte[] data = new byte[6];			
 		short[] sample = null;
		System.out.println("readSample enter");
 		
 		int result = read(data);
 		if(result == data.length) {
 			sample = new short[3];
 
 			for(int i = 0; i < sample.length; ++i) {
 				short byte0 = (short) (0x00FF & (short)data[i*2 + 1]);
 				short byte1 = (short) (0x00FF & (short)data[i*2]);
 
 				sample[i] = (short) (byte1 >> 6);
 				sample[i] +=  (byte0 << 2);
 			}
 		} else {
 			throw new IOException("Unable to read sample from accelerometer\n Read length = " + result);
 		}
 		
 		
 		return new AccelerometerSample(convertToGs(sample[2]),
 									   convertToGs(sample[1]),
 									   convertToGs(sample[0]));
 	}
 
 	private float convertToGs(short s) {
 		float mVperBit = 2900.0f/1024.0f;
 		short scale_factors[] = {421, 316, 158, 105};
 		float result = 0;
 		synchronized(config) {
 			result = (((float)(s * mVperBit) - 1450)) / scale_factors[config.getSensitivity()];
 		}
 		
 		return result;
 	}
 
 	public void configurationChanged(AccelerometerConfiguration c) {
 		synchronized (config) {
 			config = c;
 		}
 	}
 }
