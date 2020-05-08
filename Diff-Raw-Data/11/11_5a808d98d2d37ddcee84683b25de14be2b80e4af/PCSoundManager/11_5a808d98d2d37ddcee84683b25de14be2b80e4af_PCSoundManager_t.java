 package yang.pc.fileio;
 
 import java.io.File;
 
 import javafx.embed.swing.JFXPanel;
 import javafx.scene.media.AudioClip;
 import javafx.scene.media.Media;
 import javafx.scene.media.MediaPlayer;
 import yang.pc.PCMusic;
 import yang.pc.PCSound;
 import yang.sound.AbstractMusic;
 import yang.sound.AbstractSound;
 import yang.sound.AbstractSoundManager;
 
 public class PCSoundManager extends AbstractSoundManager {
 
 	@SuppressWarnings("unused")
 	private final JFXPanel fxPanel;
	private final PCResourceManager mResources;
 
 	public PCSoundManager(PCResourceManager resources) {
 		super();
		mResources = resources;
		//SOUND_PATH = resources.getAssetFile(SOUND_PATH).getAbsolutePath();
 		//need to call this to initialize javafx
 		fxPanel = new JFXPanel();
 	}
 
 	@Override
 	public synchronized AbstractSound loadSound(String name) {
 		AudioClip clip = null;
 		try {
			final File file = mResources.getAssetFile(SOUND_PATH+ name + SOUND_EXT);
 			if (!file.exists()) throw new RuntimeException();
 			clip = new AudioClip(file.toURI().toString());
 		} catch (final Exception e) {
 			System.err.println("failed loading sound: "+name);
 		}
 		return new PCSound(this, clip);
 	}
 
 	@Override
 	protected AbstractMusic loadMusic(String name) {
 		MediaPlayer player = null;
 		try {
			final File file = mResources.getAssetFile(SOUND_PATH + name + SOUND_EXT);
 			if (!file.exists()) throw new RuntimeException();
 			player = new MediaPlayer(new Media(file.toURI().toString()));
 		} catch (final Exception e) {
 			System.err.println("failed loading sound: "+name);
 		}
 		return new PCMusic(this, player);
 	}
 
 }
