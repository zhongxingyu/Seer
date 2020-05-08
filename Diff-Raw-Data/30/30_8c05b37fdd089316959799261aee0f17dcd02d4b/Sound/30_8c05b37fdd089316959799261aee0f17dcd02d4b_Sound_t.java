 package mta13438;
 
 import java.io.BufferedInputStream;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.nio.IntBuffer;
 
 import org.lwjgl.BufferUtils;
 import org.lwjgl.openal.AL10;
 import org.lwjgl.openal.AL11;
 import org.lwjgl.openal.EFX10;
 import org.lwjgl.util.WaveData;
 
 // Need to include "AL.create();" when initializing
 
 public class Sound {
 
 	//Each sound have an IntBuffer to hold the sound and it's effects
 	//and another IntBuffer to hold the position and speed.
 	IntBuffer buffer = BufferUtils.createIntBuffer(1);
 	IntBuffer source = BufferUtils.createIntBuffer(1);
 	final int effectSlot = EFX10.alGenAuxiliaryEffectSlots();
 	final int reverbEffect = EFX10.alGenEffects();
 	Point pos;
 	boolean isPlaying = false;
 	boolean enableReverb;
 
 	//Figured out that we needed a constructor;
 	public Sound(SOUNDS soundname, Point point, boolean looping, boolean enableReverb){
 		AL10.alGenBuffers(buffer);
 
 		//Set Position 
 		pos = point;
 		this.enableReverb = enableReverb;
 
 		//Loads the wave file from this class's package in your classpath
 		try {
 			WaveData waveFile = WaveData.create(new BufferedInputStream(new FileInputStream(soundname.getPath())));
 			AL10.alBufferData(buffer.get(0), waveFile.format, waveFile.data, waveFile.samplerate);
 			waveFile.dispose();
 		} catch (FileNotFoundException e) {
 			System.out.println("File could not be loaded from Classpath");
 			e.printStackTrace();
 		}
 
 
 
 		//Generate a source
 		AL10.alGenSources(source);
 
 		//Bind buffer and source
 		AL10.alSourcei(source.get(0), AL10.AL_BUFFER,   buffer.get(0) );
 		AL10.alSourcef(source.get(0), AL10.AL_PITCH,    1.0f          );
		AL10.alSourcef(source.get(0), AL10.AL_GAIN,     5.0f          );
 		AL10.alSource3f(source.get(0), AL10.AL_POSITION, pos.getX(), pos.getY(), pos.getZ());
 		if(looping == true){
 			AL10.alSourcei(source.get(0), AL10.AL_LOOPING,  AL10.AL_TRUE  );
 		}
 
 		// Add reverb effect
 		if(enableReverb){
 			EFX10.alEffecti(reverbEffect, EFX10.AL_EFFECT_TYPE, EFX10.AL_EFFECT_REVERB);
 			EFX10.alEffectf(reverbEffect, EFX10.AL_METERS_PER_UNIT, 10f);
 			EFX10.alAuxiliaryEffectSloti(effectSlot, EFX10.AL_EFFECTSLOT_EFFECT, reverbEffect);
 			AL11.alSource3i(source.get(0), EFX10.AL_AUXILIARY_SEND_FILTER, effectSlot, 0, EFX10.AL_FILTER_NULL);
 		}
 	}// need killALData() and AL.destroy() before program close
 
 	//Update function for updating position, pitch and gain
 	public void update(Point point){
 		this.pos = point;
 		AL10.alSource3f (source.get(0), AL10.AL_POSITION, pos.getX(), pos.getY(), pos.getZ());
 	}
 	public void reverb_Decay_Time(float[] rt60){
 		if(enableReverb){
 			float decayTime, HFRatio;
 			float temp = 0;
 
 			for (int i = 0; i < rt60.length; i++) {
 				temp += rt60[i];
 			}
 			decayTime = temp / rt60.length;
 
 			temp = (rt60[0] + rt60[1]) / 2;
 			HFRatio = ((rt60[4] + rt60[5]) / 2) / temp;
 
 			EFX10.alEffectf(reverbEffect, EFX10.AL_REVERB_DECAY_TIME, decayTime);
 			EFX10.alEffectf(reverbEffect, EFX10.AL_REVERB_DECAY_HFRATIO, HFRatio);
 		}else {
 			System.out.println("Reverb Disabled.");
 		}
 	}
 
 	public void play(){
 		if(isPlaying == false){
 			AL10.alSourcePlay(source);
 			isPlaying = true;
 		}
 	}
 	public void stop(){
 		AL10.alSourceStop(source);
 		isPlaying = false;
 	}
 	public void pause(){
 		AL10.alSourcePause(source);
 		isPlaying = false;
 	}
 	public Point getPos(){
 		return this.pos;
 	}
 
 	//Removes the source and buffer
 	public void delete(){
 		AL10.alDeleteSources(source);
 		AL10.alDeleteBuffers(buffer);
 	}
 
 	@Override
 	public String toString() {
 		return "Sound at: " + getPos().getX() + "," + getPos().getY();
 	}
 }
