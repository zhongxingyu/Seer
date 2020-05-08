 package no.ntnu.stud.flatcraft.music;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.ArrayList;
 
 import org.newdawn.slick.openal.Audio;
 import org.newdawn.slick.openal.OggInputStream;
 import org.newdawn.slick.openal.OpenALStreamPlayer;
 import org.newdawn.slick.openal.SoundStore;
 import org.newdawn.slick.openal.StreamSound;
 
 public class MusicPlayer {
 	private SoundStore soundStore;
 	
 	private ArrayList<Audio> music;
 	private ArrayList<Audio> ambientMusic;
 	private ArrayList<Audio> ambientNoise;
 	
 	private boolean enabled;
 	
 	private Audio musicPlaying;
 	private Audio ambientMusicPlaying;
 	private Audio[] ambientNoisePlaying = new Audio[2];
 	
 	private int musicDelay;
 	private int ambientMusicDelay;
 	private int[] ambientNoiseDelay = new int[2];
 	
 	public MusicPlayer() {
 		soundStore = SoundStore.get();
 		soundStore.setMaxSources(4);
 		soundStore.init();
 		
 		music = new ArrayList<Audio>();
 		ambientMusic = new ArrayList<Audio>();
 		ambientNoise = new ArrayList<Audio>();
 	}
 	
 	public void addMusic(String res) throws IOException {
 		music.add(soundStore.getOggStream(res));
 	}
 	
 	public void addAmbientMusic(String res) throws IOException {
 		ambientMusic.add(soundStore.getOgg(res));
 	}
 	
 	public void addAmbientNoise(String res) throws IOException {
 		ambientNoise.add(soundStore.getOgg(res));
 	}
 	
 	public void startMusic(boolean enabled) {
 		this.enabled = enabled;
 	}
 	
 	public void stopMusic() {
 		enabled = false;
 	}
 	
 	public void update(int delta) {
 		if (enabled) {
 			if (musicPlaying == null) {
 				musicDelay -= delta;
 				
 				if (musicDelay <= 0) {
 					musicPlaying = music.get((int)(Math.random()*music.size()));
 					musicPlaying.playAsMusic(1.0f, 1.0f, false);
 					soundStore.setSoundVolume(0.75f);
 				}
 			} else if (!soundStore.isMusicPlaying()) {
 				musicPlaying.stop();
 				musicDelay = (int) (Math.random()*90000) + 30000;
 				musicPlaying = null;
 				soundStore.setSoundVolume(1.0f);
 			}
 
			if (ambientMusic == null) {
 				ambientMusicDelay -= delta;
 				
 				if (ambientMusicDelay <= 0) {
 					ambientMusicPlaying = ambientMusic.get((int)(Math.random()*ambientMusic.size()));
 					ambientMusicPlaying.playAsSoundEffect(1.0f, (float) Math.random()*0.2f + 1f, false);
 				}
 			} else if (!ambientMusicPlaying.isPlaying()) {
 				ambientMusicPlaying.stop();
 				ambientMusicDelay = (int) (Math.random()*6000) + 1000;
 				ambientMusicPlaying = null;
 			}
 
 			if (ambientNoisePlaying[0] == null) {
 				ambientNoiseDelay[0] -= delta;
 				
 				if (ambientNoiseDelay[0] <= 0) {
 					ambientNoisePlaying[0] = ambientNoise.get((int)(Math.random()*ambientNoise.size()));
 					ambientNoisePlaying[0].playAsSoundEffect(1.0f, (float) Math.random()*0.2f + 1f, false);
 				}
 			} else if (!ambientNoisePlaying[0].isPlaying()) {
 				ambientNoisePlaying[0].stop();
 				ambientNoiseDelay[0] = (int) (Math.random()*16000) + 1000;
 				ambientNoisePlaying[0] = null;
 			}
 
 			if (ambientNoisePlaying[1] == null) {
 				ambientNoiseDelay[1] -= delta;
 				
 				if (ambientNoiseDelay[1] <= 0) {
 					ambientNoisePlaying[1] = ambientNoise.get((int)(Math.random()*ambientNoise.size()));
 					ambientNoisePlaying[1].playAsSoundEffect(1.0f, (float) Math.random()*0.2f + 1f, false);
 				}
 			} else if (!ambientNoisePlaying[1].isPlaying()) {
 				ambientNoisePlaying[1].stop();
 				ambientNoiseDelay[1] = (int) (Math.random()*16000) + 1000;
 				ambientNoisePlaying[1] = null;
 			}
 			
 			soundStore.poll(delta);
 		}
 	}
 }
