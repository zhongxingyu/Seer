 package com.fifthrevision.sound;
 
 import android.util.Log;
 
 /**
  * A simple Infinite Impulse Response filter. The algorithm is very well known 
  * and basic. This implementation implements directly from 
  * http://www.dspguide.com/ch19/1.htm
  * 
  * @author johncch
  *
  */
 public abstract class IIRFilter extends Unit {
 
 	protected float[] a;
 	protected float[] b;
 	
 	protected float prevIn[];
 	protected float prevOut[];
 	
 	protected float cutOffFreq;
 	
 	protected int minInd;
 	
 	public final Unit setFreq(float freq) {
 		if(validFreq(freq) && freq != cutOffFreq) {
 			cutOffFreq = freq;
 			calcCoeff();
 		}
 		return this;
 	}
 	
 	protected boolean validFreq(float f) {
 		return f > 0;
 	}
 	
 	@Override
 	public Unit render(float[] buffer) {
 		if(a == null || b == null) return this;
 		
 		float[] out = new float[buffer.length];
 		int minIndex = Math.max(a.length - 1, b.length);
 		
 		if(minIndex > 0 && (prevIn != null) && (prevOut != null)) {
 			for(int i = 0; i < minIndex; i++) {
 				for(int j = 0; j < a.length; j++) {
					float multiplier = (i - j >= 0) ? buffer[i-j] : prevIn[Math.abs(i - j) - 1];
 					out[i] += a[j] * multiplier;
 				}
 				for(int j = 0; j < b.length; j++) {
					Log.d("STUFF", "i - j - 1 = " + (i - j - 1));
					float multiplier = (i - j - 1 >= 0) ? out[i-j-1] : prevOut[(Math.abs(i - j - 1)) - 1];
 					out[i] += b[j] * multiplier;
 				}
 			}
 		}
 		
 		for(int i = minIndex; i < buffer.length; i++) {
 			for(int j = 0; j < a.length; j++) {
 				out[i] += a[j] * buffer[i - j];
 			}
 			for(int j = 0; j < b.length; j++) {
 				out[i] += b[j] * out[i - j - 1];
 			}
 		}
 		
 		for(int i = 0; i < prevIn.length; i++) {
 			this.prevIn[i] = buffer[prevIn.length - i - 1];
 			this.prevOut[i] = out[prevIn.length - i - 1];
 		}
 		
 		//for(int i = 0; i < buffer.length; i++) {
 		//	buffer[i] = out[i];
 		//}
 		System.arraycopy(out, 0, buffer, 0, out.length);
 		return this;
 	}
 
 	protected void calcCoeff() {
 		minInd = Math.max(a.length - 1, b.length);
 		this.prevIn = new float[minInd];
 		this.prevOut = new float[minInd];
 	}
 	
 }
