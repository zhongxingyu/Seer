 package audio;
 
 import static org.lwjgl.openal.AL10.*;
 import game.GameInfo;
 
 import java.io.BufferedInputStream;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.util.Enumeration;
 import java.util.Hashtable;
 
 import javax.sound.sampled.AudioInputStream;
 import javax.sound.sampled.AudioSystem;
 import javax.sound.sampled.UnsupportedAudioFileException;
 import javax.vecmath.Vector3f;
 
import main.Input;
 import main.Main;
 import main.Util;
 
 import org.lwjgl.LWJGLException;
 import org.lwjgl.openal.AL;
 import org.lwjgl.openal.AL10;
 import org.lwjgl.openal.AL11;
 import org.lwjgl.util.WaveData;
 
 public class Audio {
 		
 	public static int numSources = 32;
 	
 	private static Hashtable<String,Buffer> loadedBuffers = new Hashtable<String,Buffer>();
 	private static Source[] sources = new Source[numSources];
 				
 	private static long lastUpdate = System.currentTimeMillis();
 	private static int timePassed;
 	
 	public static void init() {
 		try {
 			AL.create();
 		} catch (LWJGLException exception) {
 			Main.RUNNING = false;
 			System.err.println("Error occured in creating the OpenAL Context.");
 	        exception.printStackTrace();
 		}
 		
 		for(int i = 0; i < sources.length; i++) {
 			System.out.println("Creating audio source " + (i+1));
 			int sourceID = alGenSources();
 			sources[i] = new Source(sourceID);
 		}
 	}
 	
 	public static void update() {
 		timePassed = (int) (System.currentTimeMillis() - lastUpdate);
 		for(Source source: sources) {
 			if(source.hasStopped()) {
 				alSourceStop(source.id);
 			}
 			if(source.isPlaying()) {
 				source.update(timePassed);
 				if(source.currentBuffer.isMusic) {
					source.currentBuffer.gain = GameInfo.musicVolume;
					source.currentBuffer.pitch = 1;
 				}
 				bindSource(source, source.currentBuffer);
 			}
 		}
 		
 		alListener(AL_POSITION, Util.convertToBuffer(GameInfo.playerPos));
 		alListener(AL_VELOCITY, Util.convertToBuffer(GameInfo.playerPos));
 		
 		lastUpdate = System.currentTimeMillis();
 	}
 	
 	public static void play(String loc, Vector3f pos, Vector3f vel) {
 		if(loadedBuffers.containsKey(loc)) {
 			int i = getAvailableSource();
 			Buffer buffer = loadedBuffers.get(loc);
 			buffer.looping = false;
 			buffer.gain = GameInfo.fxVolume;
 			buffer.isMusic = false;
 			alSource(sources[i].id, AL_POSITION, Util.convertToBuffer(pos));
 			alSource(sources[i].id, AL_VELOCITY, Util.convertToBuffer(vel));
 			bindSource(sources[i], buffer);
 			alSourcePlay(sources[i].id);
 		} else {
 			loadBuffer(loc);
 			play(loc, pos, vel);
 		}
 	}
 	
 	public static void playAtPlayer(String loc) {
 		play(loc, GameInfo.playerPos, new Vector3f(0,0,0));
 	}
 	
 	public static void playMusic(String loc) {
 		if(loadedBuffers.containsKey(loc)) {
 			int i = getAvailableSource();
 			Buffer buffer = loadedBuffers.get(loc);
 			buffer.looping = true;
 			buffer.gain = GameInfo.musicVolume;
 			buffer.isMusic = true;
 			bindSource(sources[i], buffer);
 			alSourcePlay(sources[i].id);
 		} else {
 			loadBuffer(loc);
 			playMusic(loc);
 		}
 	}
 	
 	private static void loadBuffer(String loc) {
 		System.out.println("Loading audio buffer \"" + loc + "\"");
 		
 		AudioInputStream inputStream = null;
 		try {
 			inputStream = AudioSystem.getAudioInputStream(new BufferedInputStream(new FileInputStream("res/audio/"+loc)));
 		} catch (UnsupportedAudioFileException exception) {
 	        exception.printStackTrace();
 		} catch (IOException exception) {
 			exception.printStackTrace();
 		}
 		
 		WaveData audioData = WaveData.create(inputStream);
 		
 		int bufferID = alGenBuffers();
 		
 		alBufferData(bufferID, audioData.format, audioData.data, audioData.samplerate);
 		
 		audioData.dispose();
 				
 		int size = alGetBufferi(bufferID, AL_SIZE);
 		int freq = alGetBufferi(bufferID, AL_FREQUENCY);
 		int channels = alGetBufferi(bufferID, AL_CHANNELS);
 		int bits = alGetBufferi(bufferID, AL_BITS);
 		float duration = (float)((size)/(freq*channels*(bits/8f)));
 		
 		Buffer sound = new Buffer(bufferID, duration, loc);
 		
 		loadedBuffers.put(loc,sound);
 	}
 	
 	private static int getAvailableSource() {		
 		for(int i = 0; i < sources.length; i++) {
 			if(!sources[i].isPlaying()) {
 				sources[i].setPlaying();
 				return i;
 			}
 		}
 		
 		int bestSource = 0;
 		double amountLeftToPlay = 0;
 		for(int i = 0; i < sources.length; i++) {
 			double percentPlayed = sources[i].millisPlayed/sources[i].currentBuffer.duration;
 			if(percentPlayed > amountLeftToPlay && !sources[i].currentBuffer.isMusic) {
 				amountLeftToPlay = percentPlayed;
 				bestSource = i;
 			}
 		}
 		sources[bestSource].setPlaying();
 		sources[bestSource].millisPlayed = 0;
 		return bestSource;
 	}
 	
 	private static void bindSource(Source source, Buffer buffer) {
 		source.currentBuffer = buffer;
 		alSourcei(source.id, AL_BUFFER, buffer.id);
 		alSourcei(source.id, AL_LOOPING, buffer.looping ? AL_TRUE : AL_FALSE);
 		alSourcef(source.id, AL_PITCH, buffer.pitch);
 		alSourcef(source.id, AL_GAIN, buffer.gain);
 		source.setPlaying();
 	}
 	
 	public static void destroy() {
 		for(int i = 0; i < sources.length; i++) { 
 			System.out.println("Removing audio source " + (i+1));
 			alDeleteSources(sources[i].id);
 		}
 		
 		Enumeration<Buffer> e = loadedBuffers.elements();
 		while(e.hasMoreElements()) {
 			Buffer buffer = e.nextElement();
 			System.out.println("Removing audio buffer \"" + buffer.name + "\"");
 			alDeleteBuffers(buffer.id);
 		}
 
 		AL.destroy();
 	}
 }
