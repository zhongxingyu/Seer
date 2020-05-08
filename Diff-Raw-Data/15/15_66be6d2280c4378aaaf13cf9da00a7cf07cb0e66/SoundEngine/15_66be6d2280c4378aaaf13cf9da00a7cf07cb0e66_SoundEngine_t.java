 package spaceshooters.sfx;
 
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.Sound;
 
 import spaceshooters.api.events.EventFactory;
 import spaceshooters.api.events.sfx.SoundPlayAtEvent;
 import spaceshooters.api.events.sfx.SoundPlayEvent;
 import spaceshooters.util.Logger;
 import spaceshooters.util.config.Configuration;
 import spaceshooters.util.config.Options;
 
 public class SoundEngine {
 	private static final SoundEngine instance = new SoundEngine();
 	private EventFactory factory = EventFactory.getEventFactory();
	private Configuration config = Configuration.getConfiguration();
 	
 	private SoundEngine() {
 	}
 	
 	public void play(Sounds sound) {
 		this.play(sound, 1.0f, 1.0f);
 	}
 	
 	public void play(Sounds sound, float pitch, float volume) {
 		try {
 			SoundPlayEvent e = new SoundPlayEvent(sound, pitch, volume);
 			factory.postEvent(e);
			if (!e.isCancelled() && config.getBoolean(Options.USE_SOUND)) {
 				new Sound("sfx/" + sound.getPath()).play(e.getPitch(), e.getVolume());
 			}
 		} catch (SlickException e) {
 			Logger.error(e, "I can't play the sound " + sound.getPath());
 		}
 	}
 	
 	public void playAt(Sounds sound, int x, int y, int z) {
 		this.playAt(sound, x, y, z, 1.0f, 1.0f);
 	}
 	
 	public void playAt(Sounds sound, int x, int y, int z, float pitch, float volume) {
 		try {
 			SoundPlayAtEvent e = new SoundPlayAtEvent(sound, x, y, z, pitch, volume);
 			factory.postEvent(e);
			if (!e.isCancelled() && config.getBoolean(Options.USE_SOUND)) {
 				x = e.getX();
 				y = e.getY();
 				z = e.getZ();
 				new Sound("sfx/" + sound.getPath()).playAt(e.getPitch(), e.getVolume(), x, y, z);
 			}
 		} catch (SlickException e) {
 			Logger.error(e, "I can't play the sound " + sound.getPath() + " at: " + x + ", " + y + ", " + z);
 		}
 	}
 	
 	public void loop(Sounds sound) {
 		try {
 			new Sound("sfx/" + sound.getPath()).loop();
 		} catch (SlickException e) {
 			Logger.error(e, "I cannot loop the sound " + sound.getPath());
 		}
 	}
 	
 	public void loop(Sounds sound, float pitch, float volume) {
 		try {
 			new Sound("sfx/" + sound.getPath()).loop(pitch, volume);
 		} catch (SlickException e) {
 			Logger.error(e, "I cannot loop the sound " + sound.getPath());
 		}
 	}
 	
 	public static SoundEngine getInstance() {
 		return instance;
 	}
 }
