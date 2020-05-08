 package com.mobilelab.synth;
 
 import java.util.ArrayList;
 
 import com.fifthrevision.sound.AmpMod;
 import com.fifthrevision.sound.HighPassSP;
 import com.fifthrevision.sound.LowPassFS;
 import com.fifthrevision.sound.LowPassSP;
 import com.fifthrevision.sound.Osc;
 import com.fifthrevision.sound.Unit;
 
 import android.media.AudioFormat;
 import android.media.AudioManager;
 import android.media.AudioTrack;
 import android.util.Log;
 import as.adamsmith.etherealdialpad.dsp.Dac;
 import as.adamsmith.etherealdialpad.dsp.ExpEnv;
 import as.adamsmith.etherealdialpad.dsp.UGen;
 import as.adamsmith.etherealdialpad.dsp.WtOsc;
 
 public class SynthManager {
 
 	public static final String TAG = "SYNTH_MANAGER";
 
 	public static final int SINE_WAVE = 25;
 	public static final int TRIANGLE_WAVE = 26;
 	public static final int SQUARE_WAVE = 27;
 	public static final int SAW_WAVE = 28;
 
 	private int currentWaveShape = 25;
 
 	private ArrayList<Osc> oscillators = new ArrayList<Osc>();
 	private SynthesizerRunner runner = new SynthesizerRunner();
 
 	public SynthManager(){
 		//Test
 		// runner.addPipeline(new AmpMod().setFreq(100));
		runner.addPipeline(new LowPassSP().setFreq(400));
		// runner.addInputWaveOsc((Osc) new Osc().setFreq(10));
 
 		Thread thread = new Thread(runner);
 		thread.start();
 	}
 
 	public void setCurrentWaveShape(int waveShape) {
 		if(waveShape >= SINE_WAVE && waveShape <= SAW_WAVE) {
 			this.currentWaveShape = waveShape;
 			this.setWave();
 		}
 	}
 
 	public void setSoundSourceFreq(int id, float freq) {
 		Log.d(TAG, "Setting frequency " + freq);
 		this.oscillators.get(id).setFreq(freq);
 	}
 
 	public void addSoundSource(int id) {
 		if ( oscillators.size() < (id + 1) ) {
 			for(int i = oscillators.size(); i < (id + 1); i++){
 				oscillators.add(new Osc());
 				Log.d(TAG, "CREATED NEW OSCILLATOR");
 			}
 			Log.d(TAG, "FINISHED CREATING OSCILLATORS");
 			this.setWave();
 			Log.d(TAG, "SET WAVES!");
 		}
 		Osc osc = oscillators.get(id);
 		Log.d(TAG, "ADDING INTO RUNNER");
 		runner.addOscillators(osc);
 		Log.d(TAG, "AFTER ADDING INTO RUNNER");
 	}
 
 	public void removeSoundSource(int id) {
 		Osc osc = oscillators.get(id);
 		runner.removeFromOscillators(osc);
 	}
 	
 	private AmpMod ampmod;
 	
 	public void addAmpMod(float freq) {
 		if(this.ampmod == null) {
 			ampmod = new AmpMod();
 		}
 		ampmod.setFreq(freq);
 		runner.addPipeline(ampmod);
 	}
 	
 	public void removeAmpMod() {
 		runner.removePipeline(ampmod);
 	}
 	
 	public void setAmpModFreq(float freq){
 		ampmod.setFreq(freq);
 	}
 	
 	private Osc freqmod;
 	
 	public void setFreqMod(float freq) {
 		if(this.freqmod == null) {
 			freqmod = new Osc();
 		}
 		freqmod.setFreq(freq);
 		runner.addInputWaveOsc(freqmod);
 	}
 	
 	public void removeFreqMod() {
 		runner.removeInputWaveOsc();
 	}
 	
 	public void setFreqModFreq(float freq) {
 		freqmod.setFreq(freq);
 	}
 	
 	private LowPassFS lowpassfilter;
 	
 	public void setLowPass(float freq) {
 		if(this.lowpassfilter == null) {
 			lowpassfilter = new LowPassFS();
 		}
 		lowpassfilter.setFreq(freq);
 		runner.addPipeline(lowpassfilter);
 	}
 	
 	public void removeLowPass() {
 		runner.removePipeline(lowpassfilter);
 	}
 
 	public void setLowPassCutoff(float freq) {
 		lowpassfilter.setFreq(freq);
 	}
 	
 	private HighPassSP highpassfilter;
 	
 	public void setHighPass(float freq) {
 		if(highpassfilter == null) {
 			highpassfilter = new HighPassSP();
 		}
 		highpassfilter.setFreq(freq);
 		runner.addPipeline(highpassfilter);
 	}
 	
 	public void removeHighPass() {
 		runner.removePipeline(highpassfilter);
 	}
 	
 	public void setHighPassCutoff(float freq) {
 		highpassfilter.setFreq(freq);
 	}
 	
 	private void setWave() {
 		switch(this.currentWaveShape) {
 		case SINE_WAVE:
 			for(int i = 0; i < oscillators.size(); i++) {
 				oscillators.get(i).fillWithSin();
 			}
 			break;
 		case TRIANGLE_WAVE:
 			for(int i = 0; i < oscillators.size(); i++) {
 				oscillators.get(i).fillWithTri();
 			}
 			break;
 		case SQUARE_WAVE:
 			for(int i = 0; i < oscillators.size(); i++) {
 				oscillators.get(i).fillWithSqr();
 			}
 			break;
 		case SAW_WAVE:
 			for(int i = 0; i < oscillators.size(); i++) {
 				oscillators.get(i).fillWithSaw();
 			}
 			break;
 		}
 	}
 
 }
 
 class SynthesizerRunner implements Runnable {
 
 	private static String TAG = "SYNTHESIZER_RUNNER";
 
 	private final float[] localBuffer;
 	private final float[] inputBuffer;
 	private final AudioTrack track;
 	private final short [] target = new short[UGen.CHUNK_SIZE];
 	private final short [] silentTarget = new short[UGen.CHUNK_SIZE];
 
 	private Osc inputWaveOsc;
 	private ArrayList<Unit> pipeline = new ArrayList<Unit>();
 	private ArrayList<Osc> oscillators = new ArrayList<Osc>(); 
 
 	public boolean running = true;
 
 	public SynthesizerRunner() {
 		localBuffer = new float[Unit.CHUNK_SIZE];
 		inputBuffer = new float[Unit.CHUNK_SIZE];
 
 		int minSize = AudioTrack.getMinBufferSize(
 				UGen.SAMPLE_RATE,
 				AudioFormat.CHANNEL_CONFIGURATION_MONO,
 				AudioFormat.ENCODING_PCM_16BIT);
 
 		track = new AudioTrack(
 				AudioManager.STREAM_MUSIC,
 				UGen.SAMPLE_RATE,
 				AudioFormat.CHANNEL_CONFIGURATION_MONO,
 				AudioFormat.ENCODING_PCM_16BIT,
 				Math.max(UGen.CHUNK_SIZE*4, minSize),
 				AudioTrack.MODE_STREAM);
 	}
 
 	public synchronized void addInputWaveOsc(Osc osc) {
 		this.inputWaveOsc = osc;
 	}
 
 	public synchronized void removeInputWaveOsc() {
 		this.inputWaveOsc = null;
 	}
 
 	public synchronized void addOscillators(Osc osc) {
 		//	Log.d("ADDDING", "Adding Oscillators");
 		this.oscillators.add(osc);
 	}
 
 	public synchronized void removeFromOscillators(Unit unit) {
 		//	Log.d("REMOVVING", "removing Oscillators");
 		if(oscillators.contains(unit)) {
 			oscillators.remove(unit);
 		}
 	}
 
 	public synchronized void addPipeline(Unit unit) {
 		pipeline.add(unit);
 	}
 
 	public synchronized void removePipeline(Unit unit) {
 		if(pipeline.contains(unit)) {
 			pipeline.remove(unit);
 		}
 	}
 
 	/**
 	 * Pipeline rendering
 	 */
 	@Override
 	public void run() {
 
 		track.play();
 
 		while(running) {
 			//synchronized(this) {
 			if(oscillators.size() == 0) {
 				track.write(silentTarget, 0, silentTarget.length);
 			} else {
 				zeroBuffer(localBuffer);
 				zeroBuffer(inputBuffer);
 
 				synchronized(this) {
 					if(inputWaveOsc != null) {
 						inputWaveOsc.render(inputBuffer);
 					}
 
 					for(int i = 0; i < oscillators.size(); i++) {
 						oscillators.get(i).setInputWave(inputBuffer).render(localBuffer);
 					}
 
 					for(int i = 0; i < pipeline.size(); i++) {
 						pipeline.get(i).render(localBuffer);
 					}
 				}
 
 				for(int i = 0; i < Unit.CHUNK_SIZE; i++) {
 					// target[i] = (short)(32768.0f*localBuffer[i]);
 					float scaledDownMagnitude = 32768.0f / oscillators.size();
 					target[i] = (short)(scaledDownMagnitude * localBuffer[i]);
 				}
 
 				track.write(target, 0, target.length);
 			}
 			//}
 
 			//Log.d(TAG, "Outside Synck block");
 		}
 
 		track.stop();
 		track.release();
 	}
 
 	protected void zeroBuffer(final float[] buffer) {
 		for(int i = 0; i < Unit.CHUNK_SIZE; i++) {
 			buffer[i] = 0;
 		}
 	}
 }
