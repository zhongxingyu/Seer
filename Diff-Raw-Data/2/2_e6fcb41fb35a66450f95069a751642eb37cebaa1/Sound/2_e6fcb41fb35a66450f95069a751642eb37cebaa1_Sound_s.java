 package mta13438;
 
 import static org.lwjgl.openal.AL10.AL_BUFFER;
 import static org.lwjgl.openal.AL10.alBufferData;
 import static org.lwjgl.openal.AL10.alGenBuffers;
 import static org.lwjgl.openal.AL10.alGenSources;
 import static org.lwjgl.openal.AL10.alSourcePlay;
 import static org.lwjgl.openal.AL10.alSourcei;
 
 import java.io.BufferedInputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.nio.FloatBuffer;
 import java.nio.IntBuffer;
 
 import org.lwjgl.BufferUtils;
 import org.lwjgl.openal.AL10;
 import org.lwjgl.util.WaveData;
 
 // Need to include "AL.create();" when initializing
 
 public class Sound {
 	
 	SOUNDS soundname;
 	Point point;
 	IntBuffer buffer = BufferUtils.createIntBuffer(1);
 	IntBuffer source = BufferUtils.createIntBuffer(1);
 	
 	FloatBuffer sourcePos = (FloatBuffer)BufferUtils.createFloatBuffer(3).put(new float[] { 0.0f, 0.0f, 0.0f }).rewind();
 	FloatBuffer sourceVel = (FloatBuffer)BufferUtils.createFloatBuffer(3).put(new float[] { 0.0f, 0.0f, 0.0f }).rewind();
 		
 	
 	//Figured out that we needed a constructor;
 	public Sound(){
 	
 		AL10.alGenBuffers(buffer);
 		
 		//Loads the wave file from this class's package in your classpath
		WaveData waveFile = WaveData.create(soundname);
 		
 		//Puts Sound data in buffer and disposes afterwards
 		AL10.alBufferData(buffer.get(0), waveFile.format, waveFile.data, waveFile.samplerate);
 		waveFile.dispose();
 		
 		//Bind buffer and source
 		AL10.alGenSources(source);
 		
 		AL10.alSourcei(source.get(0), AL10.AL_BUFFER,   buffer.get(0) );
 		AL10.alSourcef(source.get(0), AL10.AL_PITCH,    1.0f          );
 		AL10.alSourcef(source.get(0), AL10.AL_GAIN,     1.0f          );
 		AL10.alSource (source.get(0), AL10.AL_POSITION, sourcePos     );
 		AL10.alSource (source.get(0), AL10.AL_VELOCITY, sourceVel     );
 		
 		//Setup setListenerValues
 		setListenerValues(); // Need 3 FloatBuffers as input
 		
 	}// need killALData() and AL.destroy() before program close
 	
 	public void setListenerValues(FloatBuffer Position, FloatBuffer Velocity, FloatBuffer Orientation){
 		AL10.alListener(AL10.AL_POSITION,    Position);
 		AL10.alListener(AL10.AL_VELOCITY,    Velocity);
 		AL10.alListener(AL10.AL_ORIENTATION, Orientation);
 	}
 	public void reverb(){
 		//Do reverb effect
 	}
 	public void play(){
 		AL10.alSourcePlay(source);
 	}
 	public void stop(){
 		AL10.alSourceStop(source);
 	}
 	public void pause(){
 		AL10.alSourcePause(source);
 	}
 	public void loop(){
 		AL10.alSourcei(source.get(0), AL10.AL_LOOPING,  AL10.AL_TRUE  );
 	}
 	public void delete(){
 		AL10.alDeleteSources(source);
 		AL10.alDeleteBuffers(buffer);
 	}
 }
