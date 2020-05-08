 package spaceshooters.sfx;
 
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.openal.SoundStore;
 
import spaceshooters.config.Configuration;

 public class SoundEngine {
 	private static final SoundEngine instance = new SoundEngine();
 	
 	private SoundEngine() {
 	}
 	
 	public static void init() {
 		SoundStore.get().init();
 		System.out.println("[Sound Engine] Sounds loaded!");
 	}
 	
 	public void play(Sound sound) {
 		this.play(sound, 1.0f, 1.0f);
 	}
 	
 	public void play(Sound sound, float pitch, float volume) {
 		try {
			if (Configuration.getConfiguration().getBoolean("useSound"))
				new org.newdawn.slick.Sound("sfx/" + sound.getPath()).play(pitch, volume);
 		} catch (SlickException e) {
 			System.err.println("I can't play the sound " + sound.getPath());
 			e.printStackTrace();
 		}
 	}
 	
 	public void playAt(Sound sound, int x, int y, int z) {
 		this.playAt(sound, x, y, z, 1.0f, 1.0f);
 	}
 	
 	public void playAt(Sound sound, int x, int y, int z, float pitch, float volume) {
 		try {
			if (Configuration.getConfiguration().getBoolean("useSound"))
				new org.newdawn.slick.Sound("sfx/" + sound.getPath()).playAt(pitch, volume, x, y, z);
 		} catch (SlickException e) {
 			System.err.println("I can't play the sound " + sound.getPath() + " at: " + x + ", " + y + ", " + z);
 			e.printStackTrace();
 		}
 	}
 	
 	public void loop(Sound sound) {
 		try {
 			new org.newdawn.slick.Sound("sfx/" + sound.getPath()).loop();
 		} catch (SlickException e) {
 			System.err.println("I cannot loop the sound " + sound.getPath());
 			e.printStackTrace();
 		}
 	}
 	
 	public void loop(Sound sound, float pitch, float volume) {
 		try {
 			new org.newdawn.slick.Sound("sfx/" + sound.getPath()).loop(pitch, volume);
 		} catch (SlickException e) {
 			System.err.println("I cannot loop the sound " + sound.getPath());
 			e.printStackTrace();
 		}
 	}
 	
 	public static SoundEngine getInstance() {
 		return instance;
 	}
 }
