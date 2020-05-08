 package com.fifthrevision.sound;
 
 import android.util.FloatMath;
 import android.util.Log;
 
 public class Osc extends Unit {
 	public static final int BITS = 8;
 	public static final int ENTRIES = 1<<(BITS-1);
 	public static final int MASK = ENTRIES-1;
 	
 	private float phase;
 	private float cyclesPerSample;
 	
 	final float[] table;
 	
 	public Osc() {
 		table = new float[ENTRIES];
 	}
 	
 	public Unit setFreq(float freq) {
 		cyclesPerSample = freq/SAMPLE_RATE;
 		return this;
 	}
 	
 	private float[] inputWave;
 	
 	/**
 	 * Not sure if this is the best way to do it
 	 * 
 	 * @param buffer
 	 */
 	public Unit setInputWave(float[] inputWave) {
 		this.inputWave = inputWave;
 		return this;
 	}
 	
 	public Unit render(final float[] buffer) {
 		for(int i = 0; i < CHUNK_SIZE; i++) {
 			float scaled = phase * ENTRIES;
 			final float fraction = scaled-(int)scaled;
 			final int index = (int)scaled;
 			buffer[i] += (1.0f-fraction) * table[index&MASK] + fraction * table[(index+1)&MASK];
			phase = (phase + cyclesPerSample) - (int)phase + inputWave[i] * 5.0f;
 		}	
 		return this;
 	}
 	
 	public void fillWithSin() {
 		final float dt = (float)(2.0*Math.PI/ENTRIES);
 		for(int i = 0; i < ENTRIES; i++) {
 			table[i] = FloatMath.sin(i*dt);
 		}
 	}
 	
 	public void fillWithHardSin(final float exp) {
 		final float dt = (float)(2.0*Math.PI/ENTRIES);
 		for(int i = 0; i < ENTRIES; i++) {
 			table[i] = (float) Math.pow(FloatMath.sin(i*dt),exp);
 		}
 	}
 	
 	public void fillWithZero() {
 		for(int i = 0; i < ENTRIES; i++) {
 			table[i] = 0;
 		}
 	}
 	
 	public void fillWithSqr() {
 		for(int i = 0; i < ENTRIES; i++) {
			table[i] = (i < ENTRIES/2) ? 1f : -1f;
 		}
 	}
 	
 	public void fillWithSqrDuty(float fraction) {
 		for(int i = 0; i < ENTRIES; i++) {
 			table[i] = (float)i/ENTRIES<fraction?1f:-1f;
 		}
 	}
 	
 	public void fillWithSaw() {
 		float dt = (float)(2.0/ENTRIES);
 		for(int i = 0; i < ENTRIES; i++) {
 			table[i] = 1.0f - i*dt;
 		}
 	}
 	
 	public void fillWithTri() {
 		float dt = 4.0f / ENTRIES;
 		for(int i = 0; i < ENTRIES; i++) {
 			if(i < ENTRIES / 2) {
 				table[i] = 1.0f - i * dt;
 			} else {
 				table[i] = -3.0f + i*dt;
 			}
 		}
 	}
 	
 }
