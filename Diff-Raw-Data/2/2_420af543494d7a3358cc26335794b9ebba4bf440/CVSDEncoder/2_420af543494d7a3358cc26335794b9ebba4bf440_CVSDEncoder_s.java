 package com.blastedstudios.cvsdencoder;
 
 import java.nio.ByteBuffer;
 
 /**
  * Create an instance to read or write one stream. Instance keeps track of
  * step, last three values, etc, so should only be used for decoding or writing
  * one stream, but will level itself out after a while if you reaaally don't
  * want to make a new one.
  * 
  * For PCM-like audio only
  */
 public class CVSDEncoder {
 	private static final int MIN_STEP = 10, MAX_STEP = 1280;
 	private int accum, bytesPerSample, lastThree, step = MIN_STEP;
 
 	public CVSDEncoder(int sampleRate){
 		this.bytesPerSample = sampleRate/8000;
 	}
 	
 	public byte[] encode(byte[] actual){
 		ByteBuffer buffer = ByteBuffer.wrap(new byte[actual.length / bytesPerSample / 8]);
 		for(int actualIndex = 0; actualIndex < actual.length; actualIndex += 8*bytesPerSample){
 			byte cvsdByte = 0;
 			for(int i=0; i<8*bytesPerSample; i++){
 				int actualByte = actual[actualIndex + i];
 				for(int bytesPerSampleIndex = 1; bytesPerSampleIndex < bytesPerSample; bytesPerSampleIndex++)
					actualByte = (actualByte << 8) + actual[actualIndex + ++i];
 				cvsdByte <<= 1;
 				lastThree = ((lastThree & 3) << 1);
 				if(accum < actualByte){
 					cvsdByte |= 1;
 					accum += step;
 					lastThree += 1;
 				}else
 					accum -= step;
 				step = generateStep(lastThree, step);
 			}
 			buffer.put(cvsdByte);
 		}
 		return buffer.array();
 	}
 	
 	public byte[] decode(byte[] actual) {
 		ByteBuffer buffer = ByteBuffer.wrap(new byte[actual.length * 8 * bytesPerSample]);
 		for(int i=0; i<actual.length; i++){
 			for(int bitIndex=0; bitIndex<8; bitIndex++){
 				int currentBitValue = (actual[i] >> (7 - bitIndex)) & 1;
 				accum += currentBitValue == 0 ? -step : step;
 				lastThree = ((lastThree & 3) << 1) + currentBitValue;
 				step = generateStep(lastThree, step);
 				
 				if(bytesPerSample == 1)
 					buffer.put((byte)accum);
 				else if(bytesPerSample == 2)
 					buffer.putShort((short)accum);
 				else if(bytesPerSample == 4)
 					buffer.putInt(accum);
 				else if(bytesPerSample == 8)
 					buffer.putLong(accum);
 			}
 		}
 		return buffer.array();
 	}
 	
 	private static int generateStep(int lastThree, int step){
 		return lastThree == 0 || lastThree == 7 ? 
 				Math.min(MAX_STEP, step * 2) : Math.max(MIN_STEP, step / 2);
 	}
 
 	public int getBytesPerSample() {
 		return bytesPerSample;
 	}
 }
