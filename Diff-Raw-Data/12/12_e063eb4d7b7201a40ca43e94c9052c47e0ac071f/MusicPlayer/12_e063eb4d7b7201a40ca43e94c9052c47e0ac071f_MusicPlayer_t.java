 package ch.kanti_wohlen.asteroidminer.audio;
 
 import java.util.LinkedList;
 import java.util.List;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.audio.Music;
 import com.badlogic.gdx.audio.Music.OnCompletionListener;
 import com.badlogic.gdx.files.FileHandle;
 
 public class MusicPlayer {
 
 	private static final float MUSIC_VOLUME = 0.5f; // TODO: Settings!
 
 	private static List<FileHandle> musicFiles;
 	private static int currentIndex;
 	private static Music currentMusic;
 
 	private MusicPlayer() {}
 
 	public static void load() {
 		currentIndex = -1;
 		musicFiles = new LinkedList<FileHandle>();
		//addMusic("Atmospheren Sound 1.ogg");
 		addMusic("Atmospheren Sound 2.ogg");
 	}
 
 	public static void start() {
 		if (musicFiles.size() == 0) return;
 
 		if (currentMusic != null) {
			currentMusic.setOnCompletionListener(null);
			currentMusic.stop();
 			currentMusic.dispose();
 		}
 		currentIndex = (currentIndex + 1) % musicFiles.size();
 		currentMusic = Gdx.audio.newMusic(musicFiles.get(currentIndex));
 		currentMusic.setOnCompletionListener(new OnCompletionListener() {
 
 			@Override
 			public void onCompletion(Music music) {
 				start();
 			}
 		});
 		currentMusic.setVolume(MUSIC_VOLUME);
 		currentMusic.play();
 	}
 
 	public static void stop() {
 		if (currentMusic == null) return;
 		currentMusic.setOnCompletionListener(null);
 		currentMusic.stop();
		currentMusic.dispose();
 	}
 
 	public static void dispose() {
 		stop();
		musicFiles.clear();
 	}
 
 	private static void addMusic(String fileName) {
 		FileHandle fh = Gdx.files.internal("music/" + fileName);
 		musicFiles.add(fh);
 	}
 }
